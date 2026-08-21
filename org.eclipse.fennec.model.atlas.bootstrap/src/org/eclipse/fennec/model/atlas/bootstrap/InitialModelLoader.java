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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
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
import org.eclipse.fennec.codec.jsonschema.v2.constants.CodecJsonSchemaOptions;
import org.eclipse.fennec.codec.resource.CodecResource;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.emf.common.configurator.DynamicEPackageConfigurator;
import org.eclipse.fennec.model.atlas.emf.common.ecore.EClassResolvingDynamicEFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
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
 * scope's {@link WritableScopeService} appears — exactly as if they had been
 * uploaded through the REST API, including persistence in the scope's storage
 * backend. A namespace URI that is already present in the target stage is
 * skipped, so the seeding is idempotent across restarts. The built-in
 * {@code atlas} scope cannot be targeted this way; its content is the set of
 * globally registered packages, i.e. the top-level files.
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
                description = "Name of the registry within a scope that models from scopes/<scopeName>/ are "
                        + "uploaded into.")
        String initial_models_registry() default "schema";

        @AttributeDefinition(name = "Initial Models Stage",
                description = "Stage that models from scopes/<scopeName>/ are uploaded into. Must be a writable "
                        + "stage of the target registry; to have the packages registered as EPackage services it "
                        + "must also be one of the EPackageStageActionService trigger stages.")
        String initial_models_stage() default "draft";

        @AttributeDefinition(name = "Scope Wait Seconds",
                description = "How long to wait for the scope service of a scopes/<scopeName>/ folder to appear "
                        + "before the seeding is considered failed.")
        long scope_wait_seconds() default 60;
    }

    private final BundleContext bundleContext;
    private final ResourceSet resourceSet;
    private final ConfigurationAdmin configAdmin;

    private final List<ServiceRegistration<?>> registrations = new ArrayList<>();
    private final List<Configuration> qvtConfigurations = new ArrayList<>();
    private final List<String> seededNsUris = new ArrayList<>();

    /** Scope folders still waiting for their {@link WritableScopeService} to appear. */
    private final Map<String, List<Path>> pendingScopes = new HashMap<>();
    private final ScheduledExecutorService scopeWatchdogExecutor = Executors
            .newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "InitialModelLoader-scope-watchdog");
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
     * Seeds the scope's folder as soon as its {@link WritableScopeService} shows
     * up. The built-in atlas scope only implements the deprecated
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
        List<Path> files;
        synchronized (pendingScopes) {
            files = pendingScopes.remove(name);
        }
        if (files == null) {
            return;
        }
        try {
            seedScope(name, scopeService, files);
            synchronized (pendingScopes) {
                if (pendingScopes.isEmpty() && scopeWatchdog != null) {
                    scopeWatchdog.cancel(false);
                    scopeWatchdog = null;
                }
            }
        } catch (RuntimeException e) {
            LOG.log(Level.ERROR,
                    "InitialModelLoader: seeding scope '" + name + "' failed: " + e.getMessage(), e);
            if (config.halt_on_error()) {
                haltFramework();
            }
            throw e;
        }
    }

    void removeWritableScopeService(WritableScopeService<EObject> scopeService, Map<String, Object> properties) {
        // one-shot seeding: nothing to undo when a scope goes away
    }

    @Deactivate
    void deactivate() {
        synchronized (pendingScopes) {
            if (scopeWatchdog != null) {
                scopeWatchdog.cancel(false);
                scopeWatchdog = null;
            }
            pendingScopes.clear();
        }
        scopeWatchdogExecutor.shutdownNow();
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
                        + "s for the scope service(s) of " + pendingScopes.keySet() + ".");
                scopeWatchdog = scopeWatchdogExecutor.schedule(this::scopeWaitExpired,
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
        switch (ext) {
        case "ecore", "jsonschema" ->
            pendingScopes.computeIfAbsent(scopeName, s -> new ArrayList<>()).add(file);
        case "qvto" -> throw new IllegalStateException("InitialModelLoader: " + file
                + " - QVT transformations are not scope-bound, place them outside '" + SCOPES_FOLDER + "'.");
        default -> {
            // ignore other file types
        }
        }
    }

    private void scopeWaitExpired() {
        Set<String> missing;
        synchronized (pendingScopes) {
            missing = new HashSet<>(pendingScopes.keySet());
            pendingScopes.clear();
            scopeWatchdog = null;
        }
        if (missing.isEmpty()) {
            return;
        }
        LOG.log(Level.ERROR, "InitialModelLoader: no writable scope service appeared within "
                + config.scope_wait_seconds() + "s for the scope folder(s) " + missing
                + ". Are these scopes configured? Their initial models are NOT deployed.");
        if (config.halt_on_error()) {
            haltFramework();
        }
    }

    /**
     * Uploads the models of one {@code scopes/<scopeName>/} folder into the
     * scope's registry, exactly like a REST upload would: through
     * {@link WritableScopeService#uploadToStageForRegistry}, which persists the
     * package in the scope's storage backend and triggers the stage ENTER action
     * that registers the EPackage services. Namespace URIs already present in the
     * target stage are skipped, making the seeding idempotent across restarts —
     * initial models never overwrite what a scope already contains. A failure
     * deletes what this run has uploaded before rethrowing.
     */
    private void seedScope(String scopeName, WritableScopeService<EObject> scopeService, List<Path> files) {
        String registry = config.initial_models_registry();
        String stage = config.initial_models_stage();
        LOG.log(Level.INFO, () -> "InitialModelLoader: seeding scope '" + scopeName + "' (registry '" + registry
                + "', stage '" + stage + "') with " + files.size() + " file(s).");

        List<Resource> resources = new ArrayList<>();
        for (Path file : files) {
            String name = file.getFileName().toString();
            String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
            String uri = file.toUri().toString();
            if ("jsonschema".equals(ext)) {
                resources.add(loadJsonschema(uri));
            } else {
                resources.add(resourceSet.createResource(URI.createURI(uri)));
            }
        }
        loadFromDisk(resources);

        Set<String> batchNsUris = new HashSet<>();
        List<EPackage> roots = new ArrayList<>();
        for (Resource resource : resources) {
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
            roots.add(ePackage);
        }

        // Align the Resource URIs and seed only the component-private ResourceSet
        // registry (NOT the global one), so nsURI-based cross-references between the
        // scope's files resolve. The prototype ResourceSet dies with this component.
        for (int i = 0; i < resources.size(); i++) {
            Resource resource = resources.get(i);
            EPackage ePackage = roots.get(i);
            resource.setURI(URI.createURI(ePackage.getNsURI()));
            resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
        }
        resources.forEach(r -> r.getContents().forEach(EcoreUtil::resolveAll));

        List<ObjectMetadata> uploaded = new ArrayList<>();
        int skipped = 0;
        try {
            for (EPackage ePackage : roots) {
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
        } catch (Exception e) {
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
        LOG.log(Level.INFO, () -> "InitialModelLoader: scope '" + scopeName + "': uploaded " + uploadedCount
                + " EPackage(s), skipped " + skippedCount + " already present.");
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
