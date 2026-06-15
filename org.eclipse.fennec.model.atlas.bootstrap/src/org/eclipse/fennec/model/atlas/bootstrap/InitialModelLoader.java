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
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
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
 * Duplicate {@code nsURI} entries (already in the package registry) cause the
 * activation to fail fast.
 * </p>
 *
 * @author Juergen Albert
 */
@Component(name = InitialModelLoader.PID, immediate = true, configurationPolicy = ConfigurationPolicy.OPTIONAL)
@Designate(ocd = InitialModelLoader.Config.class)
public class InitialModelLoader {

    public static final String PID = "InitialModelLoader";

    private static final Logger LOG = System.getLogger(InitialModelLoader.class.getName());

    @ObjectClassDefinition(name = "Atlas Initial Model Loader Configuration")
    public @interface Config {

        @AttributeDefinition(name = "Initial Models Folder",
                description = "Folder that is scanned once on startup for .ecore, .jsonschema and .qvto files. "
                        + "To honour the INITIAL_MODELS_FOLDER environment variable, supply a configuration "
                        + "with value \"$[env:INITIAL_MODELS_FOLDER;default=/initial-models]\" via a runtime "
                        + "configuration file picked up by the Felix configadmin interpolation plugin.")
        String initial_models_folder() default "/initial-models";
    }

    private final BundleContext bundleContext;
    private final ResourceSet resourceSet;
    private final ConfigurationAdmin configAdmin;

    private final List<ServiceRegistration<?>> registrations = new ArrayList<>();
    private final List<Configuration> qvtConfigurations = new ArrayList<>();
    private final List<String> registeredNsUris = new ArrayList<>();

    @Activate
    public InitialModelLoader(BundleContext bundleContext,
            @Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED,
                    target = "(" + EMFNamespaces.EMF_MODEL_FILE_EXT + "=jsonschema)") ResourceSet resourceSet,
            @Reference ConfigurationAdmin configAdmin, Config config) {
        this.bundleContext = bundleContext;
        this.resourceSet = resourceSet;
        this.configAdmin = configAdmin;
        loadInitial(config.initial_models_folder());
    }

    @Deactivate
    void deactivate() {
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();
        registeredNsUris.forEach(EPackageRegistryImpl.INSTANCE::remove);
        registeredNsUris.clear();
        qvtConfigurations.forEach(c -> {
            try {
                c.delete();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to delete QVT configuration", e);
            }
        });
        qvtConfigurations.clear();
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
            LOG.log(Level.ERROR, "InitialModelLoader: unable to walk folder " + folder.toAbsolutePath(), e);
            return;
        }

        List<Resource> ePackageResources = new ArrayList<>();
        List<Path> qvtFiles = new ArrayList<>();

        for (Path file : files) {
            String name = file.getFileName().toString();
            int dot = name.lastIndexOf('.');
            if (dot < 0) {
                continue;
            }
            String ext = name.substring(dot + 1).toLowerCase();
            String uri = file.toAbsolutePath().normalize().toUri().toString();
            switch (ext) {
            case "jsonschema" -> {
                Resource resource = loadJsonschema(uri);
                if (resource != null && !resource.getContents().isEmpty()) {
                    ePackageResources.add(resource);
                }
            }
            case "ecore" -> {
                if (resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey(ext)) {
                    Resource resource = resourceSet.createResource(URI.createURI(uri));
                    ePackageResources.add(resource);
                } else {
                    LOG.log(Level.WARNING,
                            () -> "InitialModelLoader: no resource factory registered for extension 'ecore', skipping "
                                    + uri);
                }
            }
            case "qvto" -> qvtFiles.add(file);
            default -> {
                // ignore other file types
            }
            }
        }

        loadFromDisk(ePackageResources);
        // Align each Resource's URI to the EPackage's nsURI and seed the registries
        // BEFORE resolveAll, so cross-package references between the loaded files
        // (which use nsURI-based hrefs) can be resolved via the ResourceSet.
        List<EPackage> ePackages = prepareEPackages(ePackageResources);
        ePackageResources.forEach(r -> r.getContents().forEach(EcoreUtil::resolveAll));

        // Register QVT factory configurations first so that anything watching
        // for the resulting EPackage services as a "ready" signal also sees
        // the QVT configurations.
        registerQvtConfigurations(qvtFiles);
        registerEPackageServices(ePackages);

        LOG.log(Level.INFO, () -> "InitialModelLoader: registered " + registeredNsUris.size() + " EPackage(s) and "
                + qvtConfigurations.size() + " QVT transformation(s).");
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
            LOG.log(Level.ERROR, "InitialModelLoader: unable to load json schema " + uri, e);
            resourceSet.getResources().remove(resource);
            return null;
        }
        return resource;
    }

    private void loadFromDisk(List<Resource> resources) {
        for (Iterator<Resource> iterator = resources.iterator(); iterator.hasNext();) {
            Resource resource = iterator.next();
            if (!resource.isLoaded()) {
                try {
                    resource.load(null);
                } catch (IOException e) {
                    LOG.log(Level.ERROR, "InitialModelLoader: unable to load " + resource.getURI(), e);
                }
            }
            if (resource.getContents().isEmpty()) {
                resourceSet.getResources().remove(resource);
                iterator.remove();
            }
        }
    }

    /**
     * Validates the loaded resources, sets each root EPackage's factory, realigns the
     * Resource URI to the EPackage's nsURI and seeds the EMF package registries so
     * that subsequent {@code EcoreUtil.resolveAll} calls can resolve cross-package
     * references between the loaded files.
     */
    private List<EPackage> prepareEPackages(List<Resource> resources) {
        List<EPackage> all = new ArrayList<>();
        for (Resource resource : resources) {
            EObject root = resource.getContents().get(0);
            if (root instanceof EPackage ePackage) {
                failOnDuplicate(ePackage);
                ePackage.setEFactoryInstance(new EClassResolvingDynamicEFactory());
                resource.setURI(URI.createURI(ePackage.getNsURI()));
                resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
                EPackageRegistryImpl.INSTANCE.put(ePackage.getNsURI(), ePackage);
                all.add(ePackage);
                collectSubPackages(ePackage, all);
            } else {
                LOG.log(Level.WARNING, () -> "InitialModelLoader: resource " + resource.getURI()
                        + " does not contain an EPackage root, skipping.");
            }
        }
        return all;
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
            registeredNsUris.add(ePackage.getNsURI());
        }
    }

    private void failOnDuplicate(EPackage ePackage) {
        String nsURI = ePackage.getNsURI();
        if (nsURI == null || nsURI.isBlank()) {
            throw new IllegalStateException(
                    "InitialModelLoader: encountered EPackage without nsURI in " + ePackage);
        }
        if (resourceSet.getPackageRegistry().containsKey(nsURI)
                || EPackageRegistryImpl.INSTANCE.containsKey(nsURI)) {
            throw new IllegalStateException(
                    "InitialModelLoader: duplicate EPackage nsURI '" + nsURI + "' is already registered.");
        }
    }

    private void collectSubPackages(EPackage parent, List<EPackage> all) {
        for (EPackage sub : parent.getESubpackages()) {
            failOnDuplicate(sub);
            all.add(sub);
            collectSubPackages(sub, all);
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
                LOG.log(Level.ERROR, "InitialModelLoader: unable to register QVT transformation " + uri, e);
            }
        }
    }
}
