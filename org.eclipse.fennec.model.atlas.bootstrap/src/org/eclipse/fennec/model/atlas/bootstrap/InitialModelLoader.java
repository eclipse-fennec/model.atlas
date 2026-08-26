/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.bootstrap;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.codec.jsonschema.v2.constants.CodecJsonSchemaOptions;
import org.eclipse.fennec.codec.resource.CodecResource;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.emf.common.configurator.DynamicEPackageConfigurator;
import org.eclipse.fennec.model.atlas.emf.common.ecore.EClassResolvingDynamicEFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * One-shot loader that scans a configured folder once on activation and
 * registers the EPackages found there as OSGi services so they become available
 * via the Atlas scope.
 *
 * <p>
 * Intended for an initial file mount baked into a docker image:
 * </p>
 * <ul>
 * <li>{@code .ecore} files are loaded as Ecore models</li>
 * <li>{@code .jsonschema} files are converted to EPackages via the Fennec
 * codec</li>
 * <li>{@code .qvto} files are registered as
 * {@code QVTModelTransformator} factory configurations</li>
 * </ul>
 *
 * <p>
 * Models placed below {@code scopes/<scopeName>/} are not registered globally.
 * Instead they are uploaded into that scope's schema registry (configurable via
 * {@code initial.models.registry} / {@code initial.models.stage}) as soon as the
 * scope's {@link WritableScopeService} and the target {@link RegistryService}
 * appear — exactly as if they had been uploaded through the REST API, including
 * persistence in the scope's storage backend. A namespace URI that is already
 * present in the target stage is skipped, so the seeding is idempotent across
 * restarts. The built-in {@code atlas} scope cannot be targeted this way; its
 * content is the set of globally registered packages, i.e. the top-level files.
 * </p>
 *
 * <p>
 * A sub folder of a scope folder names one of the scope's registries (issue
 * #198): files below {@code scopes/<scopeName>/<registryName>/} are uploaded
 * into that registry instead of the configured default registry. {@code .xmi}
 * files there are seeded as model <em>instances</em> — validated against the
 * registry's root EClass, exactly like the Object Storage REST API validates an
 * upload — while {@code .ecore}/{@code .jsonschema} files keep their EPackage
 * semantics, just targeted at the named registry. Instance object ids are
 * derived from the object's EMF ID attribute ({@link EcoreUtil#getID}) and fall
 * back to the file name; an object id already present in the target stage is
 * skipped. Each registry folder is seeded as soon as its
 * {@link RegistryService} appears, after the scope's schema files — so
 * instances may reference packages seeded by the very same bootstrap.
 * </p>
 *
 * <p>
 * The deployment is atomic: every file is loaded and validated before anything
 * is registered, and any failure rolls back everything that was already seeded
 * or registered. Duplicate or blank {@code nsURI} entries — against the package
 * registries or within the deployed batch — abort the whole deployment
 * (issue #175, D2: one bad file must not result in a partial deployment). By
 * default a failed deployment also stops the OSGi framework so a container
 * startup fails visibly instead of running without its models; set
 * {@code halt.on.error=false} to only fail the component activation.
 * </p>
 *
 * @author Juergen Albert
 */
@Component(name = InitialModelLoader.PID, immediate = true, configurationPolicy = ConfigurationPolicy.OPTIONAL)
@Designate(ocd = InitialModelLoader.Config.class)
public class InitialModelLoader {

    public static final String PID = "InitialModelLoader";

    /** Sub folder of the initial models folder whose first-level children are scope names. */
    public static final String SCOPES_FOLDER = "scopes";

    private static final String ATLAS_SCOPE_PROPERTY = AtlasProperties.ATLAS_SCOPE;
    /** Legacy fallback service property carrying the scope name. */
    private static final String SCOPE_NAME_PROPERTY = "scope.name";
    /** Service property of a {@link RegistryService} carrying the registry name. */
    private static final String REGISTRY_NAME_PROPERTY = "registry.name";

    private static final Logger LOG = System.getLogger(InitialModelLoader.class.getName());

    @ObjectClassDefinition(name = "Atlas Initial Model Loader Configuration")
    public @interface Config {

        @AttributeDefinition(name = "Initial Models Folder",
                description = "Folder that is scanned once on startup for .ecore, .jsonschema and .qvto files. "
                        + "To honour the INITIAL_MODELS_FOLDER environment variable, supply a configuration "
                        + "with value \"$[env:INITIAL_MODELS_FOLDER;default=/initial-models]\" via a runtime "
                        + "configuration file picked up by the Felix configadmin interpolation plugin.")
        String initial_models_folder() default "/initial-models";

        @AttributeDefinition(name = "Halt on Error",
                description = "If the initial deployment fails, stop the OSGi framework after rolling back, so "
                        + "the process exits and a container startup fails visibly. If false, only the component "
                        + "activation fails and the runtime continues without the initial models.")
        boolean halt_on_error() default true;

        @AttributeDefinition(name = "Initial Models Registry",
                description = "Name of the registry within a scope that models placed directly below "
                        + "scopes/<scopeName>/ are uploaded into. Files in a scopes/<scopeName>/<registryName>/ "
                        + "sub folder target the registry named by that folder instead.")
        String initial_models_registry() default "schema";

        @AttributeDefinition(name = "Initial Models Stage",
                description = "Stage that models from scopes/<scopeName>/ are uploaded into. Must be a writable "
                        + "stage of the target registry; to have the packages registered as EPackage services it "
                        + "must also be one of the EPackageStageActionService trigger stages.")
        String initial_models_stage() default "release";

        @AttributeDefinition(name = "Scope Wait Seconds",
                description = "How long to wait for the scope service and the target registry services of a "
                        + "scopes/<scopeName>/ folder to appear before the seeding is considered failed.")
        long scope_wait_seconds() default 60;
    }

    private final BundleContext bundleContext;
    private final ResourceSet resourceSet;
    private final ConfigurationAdmin configAdmin;

    private final List<ServiceRegistration<?>> registrations = new ArrayList<>();
    private final List<Configuration> qvtConfigurations = new ArrayList<>();
    private final List<String> seededNsUris = new ArrayList<>();

    /**
     * The files of one {@code scopes/<scopeName>/} folder, split into the schema
     * files placed directly in the folder and the per-registry sub folders.
     */
    private static final class ScopeSeed {
        final List<Path> schemaFiles = new ArrayList<>();
        final Map<String, List<Path>> registryFolders = new LinkedHashMap<>();
        boolean schemasSeeded;
    }

    /** Scope folders with parts still waiting for their services to appear. Guards itself, {@link #scopeServices} and {@link #registryServices}. */
    private final Map<String, ScopeSeed> pendingScopes = new HashMap<>();
    /** The {@link WritableScopeService}s currently bound, by scope name. */
    private final Map<String, WritableScopeService<EObject>> scopeServices = new HashMap<>();
    /** The {@link RegistryService}s currently bound, by their {@code registry.name} property. */
    private final Map<String, RegistryService<?>> registryServices = new HashMap<>();
    /**
     * Single thread executing all scope seeding and the watchdog, so seeding
     * units never run concurrently and SCR bind threads are never blocked on
     * storage uploads.
     */
    private final ScheduledExecutorService scopeSeedingExecutor = Executors
            .newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "InitialModelLoader-scope-seeding");
                t.setDaemon(true);
                return t;
            });
    private ScheduledFuture<?> scopeWatchdog;
    private final Config config;

    @Activate
    public InitialModelLoader(BundleContext bundleContext,
            @Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED,
                    target = "(" + EMFNamespaces.EMF_MODEL_FILE_EXT + "=jsonschema)") ResourceSet resourceSet,
            @Reference ConfigurationAdmin configAdmin, Config config) {
        this.bundleContext = bundleContext;
        this.resourceSet = resourceSet;
        this.configAdmin = configAdmin;
        this.config = config;
        try {
            loadInitial(config.initial_models_folder());
        } catch (RuntimeException e) {
            LOG.log(Level.ERROR,
                    "InitialModelLoader: initial model deployment failed, rolling back: " + e.getMessage(), e);
            rollback();
            if (config.halt_on_error()) {
                haltFramework();
            }
            throw e;
        }
    }

    /**
     * Tracks the scope services a {@code scopes/<scopeName>/} folder may be
     * waiting for. The built-in atlas scope only implements the deprecated
     * {@code ScopeService} marker, so it is never offered here — which is
     * intended, its schema registry is read-only.
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY)
    void addWritableScopeService(WritableScopeService<EObject> scopeService, Map<String, Object> properties) {
        Object scopeName = properties.getOrDefault(ATLAS_SCOPE_PROPERTY, properties.get(SCOPE_NAME_PROPERTY));
        if (!(scopeName instanceof String name)) {
            return;
        }
        synchronized (pendingScopes) {
            scopeServices.put(name, scopeService);
        }
        triggerSeeding();
    }

    void removeWritableScopeService(WritableScopeService<EObject> scopeService, Map<String, Object> properties) {
        // one-shot seeding: nothing to undo when a scope goes away, but stop
        // offering the gone service to still-pending seeding units
        synchronized (pendingScopes) {
            scopeServices.values().remove(scopeService);
        }
    }

    /**
     * Tracks the registry services: a seeding unit only runs once its target
     * registry is bound, so the root-EClass validation and the upload cannot
     * race the registry's activation. Registries whose root EClass comes from a
     * package this very bootstrap registers appear late by construction.
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY)
    void addRegistryService(RegistryService<?> registryService, Map<String, Object> properties) {
        if (!(properties.get(REGISTRY_NAME_PROPERTY) instanceof String name)) {
            return;
        }
        synchronized (pendingScopes) {
            registryServices.put(name, registryService);
        }
        triggerSeeding();
    }

    void removeRegistryService(RegistryService<?> registryService, Map<String, Object> properties) {
        synchronized (pendingScopes) {
            registryServices.values().remove(registryService);
        }
    }

    private void triggerSeeding() {
        try {
            scopeSeedingExecutor.execute(this::seedReadyUnits);
        } catch (RejectedExecutionException e) {
            // component is deactivating
        }
    }

    /**
     * Executes every seeding unit whose services are available, repeating until
     * nothing new became ready: a scope's schema files run once the scope service
     * and the default registry are bound, each registry sub folder runs once its
     * registry is additionally bound — but never before the scope's schema files,
     * whose packages the instances may reference. Runs on the single seeding
     * thread only.
     */
    private void seedReadyUnits() {
        while (true) {
            String scopeName = null;
            WritableScopeService<EObject> scopeService = null;
            RegistryService<?> registryService = null;
            String registryName = null;
            List<Path> files = null;
            boolean schemaUnit = false;
            synchronized (pendingScopes) {
                for (Entry<String, ScopeSeed> entry : pendingScopes.entrySet()) {
                    ScopeSeed seed = entry.getValue();
                    WritableScopeService<EObject> scope = scopeServices.get(entry.getKey());
                    if (scope == null) {
                        continue;
                    }
                    if (!seed.schemasSeeded && seed.schemaFiles.isEmpty()) {
                        seed.schemasSeeded = true;
                    }
                    if (!seed.schemasSeeded) {
                        RegistryService<?> registry = registryServices.get(config.initial_models_registry());
                        if (registry == null) {
                            continue;
                        }
                        schemaUnit = true;
                        registryName = config.initial_models_registry();
                        registryService = registry;
                        files = seed.schemaFiles;
                    } else {
                        for (Entry<String, List<Path>> folder : seed.registryFolders.entrySet()) {
                            RegistryService<?> registry = registryServices.get(folder.getKey());
                            if (registry != null) {
                                registryName = folder.getKey();
                                registryService = registry;
                                files = folder.getValue();
                                break;
                            }
                        }
                        if (files == null) {
                            continue;
                        }
                    }
                    scopeName = entry.getKey();
                    scopeService = scope;
                    break;
                }
                if (scopeName == null) {
                    return;
                }
            }
            try {
                seedFilesIntoRegistry(scopeName, scopeService, registryService, registryName, files);
            } catch (SeedingInterrupted e) {
                LOG.log(Level.INFO, "InitialModelLoader: seeding of scope '" + scopeName + "' (registry '"
                        + registryName + "') was interrupted by deactivation; the next activation completes it.");
                return;
            } catch (RuntimeException e) {
                boolean servicesReplaced;
                synchronized (pendingScopes) {
                    servicesReplaced = scopeServices.get(scopeName) != scopeService
                            || registryServices.get(registryName) != registryService;
                }
                if (servicesReplaced) {
                    // The scope or registry service bounced mid-seed (e.g. because
                    // registering the global packages restarted parts of the EMF
                    // stack). The unit is still pending - retry with the current
                    // services, or when they reappear.
                    String failedScope = scopeName;
                    String failedRegistry = registryName;
                    LOG.log(Level.INFO, () -> "InitialModelLoader: seeding of scope '" + failedScope
                            + "' (registry '" + failedRegistry
                            + "') failed because its services were replaced mid-seed; retrying: " + e.getMessage());
                    continue;
                }
                LOG.log(Level.ERROR, "InitialModelLoader: seeding scope '" + scopeName + "' (registry '"
                        + registryName + "') failed: " + e.getMessage(), e);
                synchronized (pendingScopes) {
                    // drop the whole scope so the failure is not retried endlessly
                    pendingScopes.remove(scopeName);
                    cancelWatchdogIfDone();
                }
                if (config.halt_on_error()) {
                    haltFramework();
                }
                return;
            }
            synchronized (pendingScopes) {
                ScopeSeed seed = pendingScopes.get(scopeName);
                if (seed != null) {
                    if (schemaUnit) {
                        seed.schemasSeeded = true;
                    } else {
                        seed.registryFolders.remove(registryName);
                    }
                    if (seed.schemasSeeded && seed.registryFolders.isEmpty()) {
                        pendingScopes.remove(scopeName);
                    }
                    cancelWatchdogIfDone();
                }
            }
        }
    }

    /** Cancels the watchdog when nothing is pending anymore. Caller holds the {@link #pendingScopes} lock. */
    private void cancelWatchdogIfDone() {
        if (pendingScopes.isEmpty() && scopeWatchdog != null) {
            scopeWatchdog.cancel(false);
            scopeWatchdog = null;
        }
    }

    @Deactivate
    void deactivate() {
        synchronized (pendingScopes) {
            if (scopeWatchdog != null) {
                scopeWatchdog.cancel(false);
                scopeWatchdog = null;
            }
            pendingScopes.clear();
            scopeServices.clear();
            registryServices.clear();
        }
        scopeSeedingExecutor.shutdownNow();
        rollback();
    }

    /**
     * Removes everything a (possibly partial) deployment has put into the world:
     * registered services, seeded package-registry entries and QVT factory
     * configurations. Used both for normal deactivation and to compensate a
     * failed activation, so a retry does not trip over this attempt's leftovers.
     */
    private void rollback() {
        registrations.forEach(r -> {
            try {
                r.unregister();
            } catch (IllegalStateException e) {
                // already unregistered
            }
        });
        registrations.clear();
        seededNsUris.forEach(EPackageRegistryImpl.INSTANCE::remove);
        seededNsUris.forEach(resourceSet.getPackageRegistry()::remove);
        seededNsUris.clear();
        qvtConfigurations.forEach(c -> {
            try {
                c.delete();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to delete QVT configuration", e);
            }
        });
        qvtConfigurations.clear();
    }

    private void haltFramework() {
        LOG.log(Level.ERROR,
                "InitialModelLoader: halt.on.error is set, stopping the OSGi framework so the startup fails visibly.");
        Bundle systemBundle = bundleContext.getBundle(Constants.SYSTEM_BUNDLE_LOCATION);
        // stop asynchronously: stopping the framework from within the SCR activation
        // would deadlock on the very component activation that is failing here
        Thread halter = new Thread(() -> {
            try {
                systemBundle.stop();
            } catch (BundleException e) {
                LOG.log(Level.ERROR, "InitialModelLoader: failed to stop the framework", e);
            }
        }, "InitialModelLoader-halt");
        halter.setDaemon(false);
        halter.start();
    }

    private void loadInitial(String folderPath) {
        if (folderPath == null || folderPath.isBlank() || folderPath.contains("$[")) {
            // Blank, missing or an un-interpolated configadmin template
            // (e.g. "$[env:INITIAL_MODELS_FOLDER;default=/initial-models]" when the
            // Felix configadmin interpolation plugin is not present) -> nothing to do.
            LOG.log(Level.INFO, "InitialModelLoader: no folder configured, skipping initial deployment.");
            return;
        }
        Path folder;
        try {
            folder = Paths.get(folderPath);
        } catch (java.nio.file.InvalidPathException e) {
            LOG.log(Level.WARNING,
                    "InitialModelLoader: configured folder is not a valid path, skipping initial deployment: "
                            + folderPath);
            return;
        }
        if (!Files.isDirectory(folder)) {
            LOG.log(Level.INFO, () -> "InitialModelLoader: folder '" + folder.toAbsolutePath()
                    + "' does not exist or is not a directory. Skipping initial deployment.");
            return;
        }
        LOG.log(Level.INFO, () -> "InitialModelLoader: scanning '" + folder.toAbsolutePath() + "' for initial models.");

        List<Path> files;
        try (Stream<Path> stream = Files.walk(folder)) {
            files = stream.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "InitialModelLoader: unable to walk folder " + folder.toAbsolutePath(), e);
        }

        Path scopesRoot = folder.toAbsolutePath().normalize().resolve(SCOPES_FOLDER);

        List<Resource> ePackageResources = new ArrayList<>();
        List<Path> qvtFiles = new ArrayList<>();

        for (Path file : files) {
            String name = file.getFileName().toString();
            int dot = name.lastIndexOf('.');
            if (dot < 0) {
                continue;
            }
            String ext = name.substring(dot + 1).toLowerCase();
            Path normalized = file.toAbsolutePath().normalize();
            if (normalized.startsWith(scopesRoot)) {
                collectScopeFile(scopesRoot, normalized, ext);
                continue;
            }
            String uri = normalized.toUri().toString();
            switch (ext) {
            case "jsonschema" -> ePackageResources.add(loadJsonschema(uri));
            case "ecore" -> {
                if (!resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey(ext)) {
                    throw new IllegalStateException(
                            "InitialModelLoader: no resource factory registered for extension 'ecore', cannot load "
                                    + uri);
                }
                ePackageResources.add(resourceSet.createResource(URI.createURI(uri)));
            }
            case "qvto" -> qvtFiles.add(file);
            default -> {
                // ignore other file types
            }
            }
        }

        loadFromDisk(ePackageResources);
        // Validate the WHOLE batch before anything is seeded or registered, so a
        // bad file cannot leave a half-deployed state behind (issue #175 / F39).
        List<EPackage> ePackages = validateEPackages(ePackageResources);
        // Align each Resource's URI to the EPackage's nsURI and seed the registries
        // BEFORE resolveAll, so cross-package references between the loaded files
        // (which use nsURI-based hrefs) can be resolved via the ResourceSet.
        seedEPackages(ePackageResources);
        ePackageResources.forEach(r -> r.getContents().forEach(EcoreUtil::resolveAll));

        // Register QVT factory configurations first so that anything watching
        // for the resulting EPackage services as a "ready" signal also sees
        // the QVT configurations.
        registerQvtConfigurations(qvtFiles);
        registerEPackageServices(ePackages);

        LOG.log(Level.INFO, () -> "InitialModelLoader: registered " + ePackages.size() + " EPackage(s) and "
                + qvtConfigurations.size() + " QVT transformation(s).");

        synchronized (pendingScopes) {
            if (!pendingScopes.isEmpty()) {
                LOG.log(Level.INFO, () -> "InitialModelLoader: waiting up to " + config.scope_wait_seconds()
                        + "s for the scope and registry service(s) of " + pendingScopes.keySet() + ".");
                scopeWatchdog = scopeSeedingExecutor.schedule(this::scopeWaitExpired,
                        config.scope_wait_seconds(), TimeUnit.SECONDS);
            }
        }
    }

    private void collectScopeFile(Path scopesRoot, Path file, String ext) {
        Path relative = scopesRoot.relativize(file);
        if (relative.getNameCount() < 2) {
            throw new IllegalStateException("InitialModelLoader: " + file + " lies directly in '" + SCOPES_FOLDER
                    + "'. Models must be placed in a sub folder named after the scope they belong to.");
        }
        String scopeName = relative.getName(0).toString();
        if (WorkflowConstants.ATLAS_SCOPE_NAME.equals(scopeName)) {
            throw new IllegalStateException("InitialModelLoader: the '" + WorkflowConstants.ATLAS_SCOPE_NAME
                    + "' scope cannot be seeded via a scope folder - its schema registry is read-only. "
                    + "Place the models at the top level of the initial models folder instead.");
        }
        if ("qvto".equals(ext)) {
            throw new IllegalStateException("InitialModelLoader: " + file
                    + " - QVT transformations are not scope-bound, place them outside '" + SCOPES_FOLDER + "'.");
        }
        if (relative.getNameCount() == 2) {
            // directly in the scope folder: EPackages for the configured default registry
            switch (ext) {
            case "ecore", "jsonschema" ->
                pendingScopes.computeIfAbsent(scopeName, s -> new ScopeSeed()).schemaFiles.add(file);
            default -> {
                // ignore other file types
            }
            }
            return;
        }
        // in a sub folder: the folder names the target registry (issue #198)
        String registryName = relative.getName(1).toString();
        switch (ext) {
        case "ecore", "jsonschema", "xmi" -> pendingScopes.computeIfAbsent(scopeName, s -> new ScopeSeed())
                .registryFolders.computeIfAbsent(registryName, r -> new ArrayList<>()).add(file);
        default -> {
            // ignore other file types
        }
        }
    }

    private void scopeWaitExpired() {
        List<String> missing = new ArrayList<>();
        synchronized (pendingScopes) {
            for (Entry<String, ScopeSeed> entry : pendingScopes.entrySet()) {
                String scopeName = entry.getKey();
                ScopeSeed seed = entry.getValue();
                if (!scopeServices.containsKey(scopeName)) {
                    missing.add("scope service '" + scopeName + "'");
                }
                if (!seed.schemasSeeded && !seed.schemaFiles.isEmpty()
                        && !registryServices.containsKey(config.initial_models_registry())) {
                    missing.add("registry '" + config.initial_models_registry() + "' of scope '" + scopeName + "'");
                }
                for (String registryName : seed.registryFolders.keySet()) {
                    if (!registryServices.containsKey(registryName)) {
                        missing.add("registry '" + registryName + "' of scope '" + scopeName + "'");
                    }
                }
            }
            pendingScopes.clear();
            scopeWatchdog = null;
        }
        if (missing.isEmpty()) {
            return;
        }
        LOG.log(Level.ERROR, "InitialModelLoader: the following services did not appear within "
                + config.scope_wait_seconds() + "s: " + missing
                + ". Are these scopes and registries configured? Their initial models are NOT deployed.");
        if (config.halt_on_error()) {
            haltFramework();
        }
    }

    /**
     * Uploads the models of one seeding unit — the files directly in a
     * {@code scopes/<scopeName>/} folder, or one of its registry sub folders —
     * into the scope's registry, exactly like a REST upload would: through
     * {@link WritableScopeService#uploadToStageForRegistry}, which persists the
     * object in the scope's storage backend and triggers the stage ENTER
     * actions (for EPackages: the service registration). Every root object is
     * validated against the registry's root EClass, the same check the Object
     * Storage REST API performs. EPackages whose namespace URI — and instances
     * whose object id — are already present in the target stage are skipped,
     * making the seeding idempotent across restarts: initial models never
     * overwrite what a scope already contains. A failure deletes what this run
     * has uploaded before rethrowing.
     */
    private void seedFilesIntoRegistry(String scopeName, WritableScopeService<EObject> scopeService,
            RegistryService<?> registryService, String registry, List<Path> files) {
        String stage = config.initial_models_stage();
        LOG.log(Level.INFO, () -> "InitialModelLoader: seeding scope '" + scopeName + "' (registry '" + registry
                + "', stage '" + stage + "') with " + files.size() + " file(s).");

        List<Resource> packageResources = new ArrayList<>();
        List<Resource> instanceResources = new ArrayList<>();
        List<Path> instanceFiles = new ArrayList<>();
        for (Path file : files) {
            String name = file.getFileName().toString();
            String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
            String uri = file.toUri().toString();
            switch (ext) {
            case "jsonschema" -> packageResources.add(loadJsonschema(uri));
            case "xmi" -> {
                instanceResources.add(createXmiResource(uri));
                instanceFiles.add(file);
            }
            default -> packageResources.add(resourceSet.createResource(URI.createURI(uri)));
            }
        }
        // EPackages first: the instance files may reference them by nsURI
        loadFromDisk(packageResources);

        Set<String> batchNsUris = new HashSet<>();
        List<EPackage> packageRoots = new ArrayList<>();
        for (Resource resource : packageResources) {
            EObject root = resource.getContents().get(0);
            if (!(root instanceof EPackage ePackage)) {
                throw new IllegalStateException("InitialModelLoader: resource " + resource.getURI()
                        + " does not contain an EPackage root.");
            }
            String nsURI = ePackage.getNsURI();
            if (nsURI == null || nsURI.isBlank()) {
                throw new IllegalStateException(
                        "InitialModelLoader: encountered EPackage without nsURI in " + ePackage);
            }
            if (!batchNsUris.add(nsURI)) {
                throw new IllegalStateException("InitialModelLoader: duplicate EPackage nsURI '" + nsURI
                        + "' within the scope folder '" + scopeName + "'.");
            }
            packageRoots.add(ePackage);
        }

        // Align the Resource URIs and seed only the component-private ResourceSet
        // registry (NOT the global one), so nsURI-based cross-references between the
        // scope's files resolve. The prototype ResourceSet dies with this component.
        for (int i = 0; i < packageResources.size(); i++) {
            Resource resource = packageResources.get(i);
            EPackage ePackage = packageRoots.get(i);
            resource.setURI(URI.createURI(ePackage.getNsURI()));
            resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
        }
        packageResources.forEach(r -> r.getContents().forEach(EcoreUtil::resolveAll));

        loadFromDisk(instanceResources);
        instanceResources.forEach(r -> r.getContents().forEach(EcoreUtil::resolveAll));

        // Validate every root against the registry's root EClass — the same check
        // the Object Storage REST API performs on an upload.
        packageRoots.forEach(ePackage -> checkCompatibleWithRegistry(registryService, registry, ePackage));
        List<EObject> instanceRoots = new ArrayList<>();
        List<String> instanceIds = new ArrayList<>();
        Set<String> batchObjectIds = new HashSet<>();
        for (int i = 0; i < instanceResources.size(); i++) {
            EObject root = instanceResources.get(i).getContents().get(0);
            checkCompatibleWithRegistry(registryService, registry, root);
            String objectId = deriveObjectId(root, instanceFiles.get(i));
            if (!batchObjectIds.add(objectId)) {
                throw new IllegalStateException("InitialModelLoader: duplicate object id '" + objectId
                        + "' within the registry folder '" + registry + "' of scope '" + scopeName + "'.");
            }
            instanceRoots.add(root);
            instanceIds.add(objectId);
        }

        List<ObjectMetadata> uploaded = new ArrayList<>();
        int skipped = 0;
        try {
            for (EPackage ePackage : packageRoots) {
                String nsURI = ePackage.getNsURI();
                if (!scopeService.getMetadataByPropertyFromStageForRegistry(registry, stage,
                        WorkflowConstants.NS_URI_METADATA_PROPERTY, nsURI).isEmpty()) {
                    LOG.log(Level.INFO, () -> "InitialModelLoader: '" + nsURI + "' is already present in scope '"
                            + scopeName + "' stage '" + stage + "', skipping.");
                    skipped++;
                    continue;
                }
                ObjectMetadata metadata = createScopeMetadata(scopeName, registry, stage, ePackage);
                uploaded.add(scopeService.uploadToStageForRegistry(registry, stage, ePackage, metadata).getValue());
            }
            for (int i = 0; i < instanceRoots.size(); i++) {
                String objectId = instanceIds.get(i);
                if (scopeService.getMetadataFromStageForRegistry(registry, stage, objectId) != null) {
                    LOG.log(Level.INFO, () -> "InitialModelLoader: object '" + objectId + "' is already present in "
                            + "scope '" + scopeName + "' registry '" + registry + "' stage '" + stage
                            + "', skipping.");
                    skipped++;
                    continue;
                }
                ObjectMetadata metadata = createInstanceMetadata(scopeName, registry, stage, objectId,
                        instanceFiles.get(i), instanceRoots.get(i));
                uploaded.add(scopeService
                        .uploadToStageForRegistry(registry, stage, instanceRoots.get(i), metadata).getValue());
            }
        } catch (Exception e) {
            if (isCausedByInterrupt(e)) {
                // The component is deactivating mid-seed (its executor was shut
                // down, e.g. because registering the global packages bounced the
                // injected ResourceSet and SCR restarts the component). Leave the
                // uploads in place: the seeding is idempotent per object, so the
                // next activation skips them and completes the rest.
                Thread.currentThread().interrupt();
                throw new SeedingInterrupted();
            }
            for (ObjectMetadata metadata : uploaded) {
                try {
                    scopeService.deleteFromStageForRegistry(registry, stage, metadata.getObjectId()).getValue();
                } catch (Exception cleanupFailure) {
                    LOG.log(Level.WARNING, "InitialModelLoader: failed to delete '" + metadata.getObjectId()
                            + "' while compensating the failed seeding of scope '" + scopeName + "'.",
                            cleanupFailure);
                }
            }
            throw new IllegalStateException(
                    "InitialModelLoader: seeding scope '" + scopeName + "' failed.", e);
        }
        int uploadedCount = uploaded.size();
        int skippedCount = skipped;
        LOG.log(Level.INFO, () -> "InitialModelLoader: scope '" + scopeName + "' registry '" + registry
                + "': uploaded " + uploadedCount + " object(s), skipped " + skippedCount + " already present.");
    }

    /** Thrown when an in-flight seeding was interrupted by the component's own deactivation. */
    private static final class SeedingInterrupted extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private static boolean isCausedByInterrupt(Throwable t) {
        for (Throwable cause = t; cause != null; cause = cause.getCause()) {
            if (cause instanceof InterruptedException) {
                return true;
            }
        }
        return false;
    }

    private void checkCompatibleWithRegistry(RegistryService<?> registryService, String registry, EObject object) {
        if (!registryService.isEClassCompatibleWithRegistry(object.eClass())) {
            throw new IllegalStateException(String.format(
                    "InitialModelLoader: object type %s not compatible with registry %s (expects %s)",
                    EcoreUtil.getURI(object.eClass()), registry,
                    EcoreUtil.getURI(registryService.getRootEClass())));
        }
    }

    /**
     * The natural key of an instance is its EMF ID attribute value; files whose
     * model does not declare one (or leaves it unset) fall back to the file name
     * without extension.
     */
    private String deriveObjectId(EObject root, Path file) {
        String id = EcoreUtil.getID(root);
        if (id != null && !id.isBlank()) {
            return id;
        }
        return fileBaseName(file);
    }

    private String fileBaseName(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    /**
     * Builds instance metadata the same way {@code ObjectRegistryResource.createObject}
     * does for a REST upload, so seeded and uploaded objects are
     * indistinguishable downstream.
     */
    private ObjectMetadata createInstanceMetadata(String scopeName, String registry, String stage, String objectId,
            Path file, EObject root) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName(fileBaseName(file));
        metadata.setUploadTime(Instant.now());
        metadata.setStage(stage);
        metadata.setScope(scopeName);
        metadata.setRegistry(registry);
        metadata.setObjectType(EcoreUtil.getURI(root.eClass()).toString());
        return metadata;
    }

    /**
     * Creates a Resource for an instance {@code .xmi} file, pinning the standard
     * XMI factory on the component-private ResourceSet so a wildcard codec
     * factory can never claim the extension.
     */
    private Resource createXmiResource(String uri) {
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .putIfAbsent("xmi", new XMIResourceFactoryImpl());
        return resourceSet.createResource(URI.createURI(uri));
    }

    /**
     * Builds the metadata the same way {@code SchemaPackagesResource.createPackage}
     * does for a REST upload, so seeded and uploaded packages are
     * indistinguishable downstream.
     */
    private ObjectMetadata createScopeMetadata(String scopeName, String registry, String stage, EPackage ePackage) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(UUID.randomUUID().toString());
        metadata.setObjectName(ePackage.getName());
        metadata.setUploadTime(Instant.now());
        metadata.setStage(stage);
        metadata.setScope(scopeName);
        metadata.setRegistry(registry);
        metadata.setVersion(extractVersion(ePackage.getNsURI()));
        metadata.setObjectType(EcoreUtil.getURI(ePackage.eClass()).toString());
        metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, ePackage.getNsURI());
        return metadata;
    }

    /**
     * Returns the last URI segment that parses as an OSGi version, or {@code null}
     * — the same convention the REST upload uses to derive a version from an
     * nsURI.
     */
    private String extractVersion(String nsUri) {
        Version version = null;
        for (String segment : URI.createURI(nsUri).segments()) {
            try {
                version = Version.parseVersion(segment);
            } catch (IllegalArgumentException e) {
                // not a version segment
            }
        }
        return version == null ? null : version.toString();
    }

    private Resource loadJsonschema(String uri) {
        Resource resource = resourceSet.createResource(URI.createURI(uri), "application/schema+json");
        Map<String, Object> options = new HashMap<>();
        options.put(CodecResource.CODEC_ROOT_TYPE, EcorePackage.Literals.EPACKAGE);
        // Tell the json schema codec to expand "definitions" into EClassifiers.
        // Without this, only the EPackage shell is created from $id / title.
        options.put(CodecJsonSchemaOptions.OPTION_SCHEMA_FEATURE, "definitions");
        try {
            resource.load(options);
        } catch (IOException e) {
            resourceSet.getResources().remove(resource);
            throw new IllegalStateException("InitialModelLoader: unable to load json schema " + uri, e);
        }
        return resource;
    }

    private void loadFromDisk(List<Resource> resources) {
        for (Resource resource : resources) {
            if (!resource.isLoaded()) {
                try {
                    resource.load(null);
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "InitialModelLoader: unable to load " + resource.getURI(), e);
                }
            }
            if (resource.getContents().isEmpty()) {
                throw new IllegalStateException(
                        "InitialModelLoader: resource " + resource.getURI() + " is empty.");
            }
        }
    }

    /**
     * Validates every loaded resource — EPackage root, non-blank nsURI, no
     * duplicate against the package registries or within this batch — WITHOUT
     * touching any registry, so a validation failure leaves nothing behind.
     *
     * @return all EPackages of the batch, root packages and subpackages alike
     */
    private List<EPackage> validateEPackages(List<Resource> resources) {
        List<EPackage> all = new ArrayList<>();
        Set<String> batchNsUris = new HashSet<>();
        for (Resource resource : resources) {
            EObject root = resource.getContents().get(0);
            if (root instanceof EPackage ePackage) {
                failOnDuplicate(ePackage, batchNsUris);
                all.add(ePackage);
                collectSubPackages(ePackage, all, batchNsUris);
            } else {
                throw new IllegalStateException("InitialModelLoader: resource " + resource.getURI()
                        + " does not contain an EPackage root.");
            }
        }
        return all;
    }

    /**
     * Sets each root EPackage's factory, realigns the Resource URI to the
     * EPackage's nsURI and seeds the EMF package registries so that subsequent
     * {@code EcoreUtil.resolveAll} calls can resolve cross-package references
     * between the loaded files. Everything seeded is tracked in
     * {@link #seededNsUris} so {@link #rollback()} can compensate.
     */
    private void seedEPackages(List<Resource> resources) {
        for (Resource resource : resources) {
            EPackage ePackage = (EPackage) resource.getContents().get(0);
            ePackage.setEFactoryInstance(new EClassResolvingDynamicEFactory());
            resource.setURI(URI.createURI(ePackage.getNsURI()));
            resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
            EPackageRegistryImpl.INSTANCE.put(ePackage.getNsURI(), ePackage);
            seededNsUris.add(ePackage.getNsURI());
        }
    }

    private void registerEPackageServices(List<EPackage> ePackages) {
        // Register configurators first so any aggregating EPackage.Registry picks them
        // up before the EPackage services are looked up directly.
        for (EPackage ePackage : ePackages) {
            Dictionary<String, String> props = serviceProperties(ePackage);
            DynamicEPackageConfigurator configurator = new DynamicEPackageConfigurator(ePackage);
            registrations
                    .add(bundleContext.registerService(EPackageConfigurator.class, configurator, props));
        }
        for (EPackage ePackage : ePackages) {
            Dictionary<String, String> props = serviceProperties(ePackage);
            registrations.add(bundleContext.registerService(EPackage.class, ePackage, props));
        }
    }

    private void failOnDuplicate(EPackage ePackage, Set<String> batchNsUris) {
        String nsURI = ePackage.getNsURI();
        if (nsURI == null || nsURI.isBlank()) {
            throw new IllegalStateException(
                    "InitialModelLoader: encountered EPackage without nsURI in " + ePackage);
        }
        if (!batchNsUris.add(nsURI)) {
            throw new IllegalStateException("InitialModelLoader: duplicate EPackage nsURI '" + nsURI
                    + "' within the initial models folder.");
        }
        if (resourceSet.getPackageRegistry().containsKey(nsURI)
                || EPackageRegistryImpl.INSTANCE.containsKey(nsURI)) {
            throw new IllegalStateException(
                    "InitialModelLoader: duplicate EPackage nsURI '" + nsURI + "' is already registered.");
        }
    }

    private void collectSubPackages(EPackage parent, List<EPackage> all, Set<String> batchNsUris) {
        for (EPackage sub : parent.getESubpackages()) {
            failOnDuplicate(sub, batchNsUris);
            all.add(sub);
            collectSubPackages(sub, all, batchNsUris);
        }
    }

    private Dictionary<String, String> serviceProperties(EPackage ePackage) {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(EMFNamespaces.EMF_NAME, ePackage.getName());
        props.put(EMFNamespaces.EMF_MODEL_NSURI, ePackage.getNsURI());
        props.put(EMFNamespaces.EMF_MODEL_REGISTRATION, EMFNamespaces.MODEL_REGISTRATION_DYNAMIC);
        EAnnotation annotation = ePackage.getEAnnotation("properties");
        if (annotation != null) {
            for (Entry<String, String> entry : annotation.getDetails()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return props;
    }

    private void registerQvtConfigurations(List<Path> qvtFiles) {
        for (Path file : qvtFiles) {
            String uri = file.toAbsolutePath().normalize().toUri().toString();
            Path parent = file.toAbsolutePath().normalize().getParent();
            String id = parent != null ? parent.getFileName().toString() : file.getFileName().toString();
            try {
                Configuration configuration = configAdmin.getFactoryConfiguration("QVTModelTransformator", id, "?");
                Dictionary<String, String> props = new Hashtable<>();
                props.put("transformator.id", id);
                props.put("qvt.template.uri", uri);
                configuration.update(props);
                qvtConfigurations.add(configuration);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "InitialModelLoader: unable to register QVT transformation " + uri, e);
            }
        }
    }
}
