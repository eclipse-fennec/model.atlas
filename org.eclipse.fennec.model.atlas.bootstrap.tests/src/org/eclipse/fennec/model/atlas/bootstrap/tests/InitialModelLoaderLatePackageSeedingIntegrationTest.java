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
package org.eclipse.fennec.model.atlas.bootstrap.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.emf.common.configurator.DynamicEPackageConfigurator;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.condition.Condition;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * OSGi integration test for seeding an instance whose EPackage arrives
 * <em>after</em> the {@code InitialModelLoader} is configured (issue #351).
 *
 * <p>
 * {@link InitialModelLoaderRegistrySeedingIntegrationTest} covers the case where
 * the package is boot-seeded from an {@code .ecore} in the same folder, so the
 * loader itself puts it into the resource set before reading the instances. This
 * test covers the other production case: the package ships in a bundle. Nothing
 * orders that bundle ahead of the bootstrap — in the resolved runtimes the start
 * levels follow the alphabet, and in the hand-written ones the model bundles get
 * no start level at all — so without help the instance would be read before its
 * package exists.
 * </p>
 *
 * <p>
 * The deployment names the package in the {@code InitialModelLoaderRequiredModels}
 * configuration, and the loader only starts once the package's bundle has
 * registered the {@link Condition} with its nsURI, as every Fennec EMF model
 * bundle does after registering its EPackage.
 * </p>
 *
 * <p>
 * The fixture therefore seeds {@code carol.xmi} alone, with no {@code .ecore}
 * beside it, and registers the package and its condition only after the loader
 * has been configured. The assertion is the seeded object, not the loader's own
 * state: a component that activates and quietly drops the file would otherwise
 * look healthy.
 * </p>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(InitialModelLoaderLatePackageSeedingIntegrationTest.TempDirPropertyExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@WithFactoryConfiguration(factoryPid = "LuceneEObjectRegistryService", name = "shared-registry", location = "?", properties = {
        @Property(key = "registry.workspace.folder", value = "%s/shared-registry", templateArguments = {
                @Property.TemplateArgument(source = Property.ValueSource.SystemProperty, value = CommonTestAnnotations.PROP_TEMP_DIR) }),
        @Property(key = "registry", value = "main") })
@WithFactoryConfiguration(factoryPid = "FileObjectStorage", name = "file-storage", location = "?", properties = {
        @Property(key = "workspace.folder", value = "%s/file-storage", templateArguments = {
                @Property.TemplateArgument(source = Property.ValueSource.SystemProperty, value = CommonTestAnnotations.PROP_TEMP_DIR) }),
        @Property(key = "storage.type", value = "file"),
        @Property(key = "registry.target", value = "(registry=main)") })
@WithFactoryConfiguration(factoryPid = "EPackageLuceneIndex", name = "epackage-index", location = "?", properties = {
        @Property(key = "index.folder", value = "%s/epackage-index", templateArguments = {
                @Property.TemplateArgument(source = Property.ValueSource.SystemProperty, value = CommonTestAnnotations.PROP_TEMP_DIR) }) })
// The instance registry: schemaPackage.target names the package the test registers late, so the
// registry cannot activate before it — which is the whole point of issue #169 and the reason the
// scope service doubles as the 'the package has arrived' signal below.
@WithFactoryConfiguration(factoryPid = "RegistryService", name = InitialModelLoaderLatePackageSeedingIntegrationTest.REGISTRY_NAME, location = "?", properties = {
        @Property(key = "registry.name", value = InitialModelLoaderLatePackageSeedingIntegrationTest.REGISTRY_NAME),
        @Property(key = "registry.type", value = "OTHER"),
        @Property(key = "root.eclass.uri", value = InitialModelLoaderLatePackageSeedingIntegrationTest.NS_URI
                + "#//Person"),
        @Property(key = "schemaPackage.target", value = "(emf.nsURI="
                + InitialModelLoaderLatePackageSeedingIntegrationTest.NS_URI + ")"),
        @Property(key = "resourceSet.target", value = "(emf.name=latePerson)"),
        @Property(key = "storageService.target", value = "(storage.type=file)"),
        @Property(key = "storageService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
        @Property(key = "registry.target", value = "(registry=main)"),
        @Property(key = "stages", type = Type.Array, value = {
                "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
        @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
        @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
@WithFactoryConfiguration(factoryPid = "ScopeService", name = InitialModelLoaderLatePackageSeedingIntegrationTest.SCOPE_NAME, location = "?", properties = {
        @Property(key = "atlas.scope", value = InitialModelLoaderLatePackageSeedingIntegrationTest.SCOPE_NAME),
        @Property(key = "scope.name", value = InitialModelLoaderLatePackageSeedingIntegrationTest.SCOPE_NAME),
        @Property(key = "registryService.target", value = "(registry.name="
                + InitialModelLoaderLatePackageSeedingIntegrationTest.REGISTRY_NAME + ")"),
        @Property(key = "registryService.cardinality.minimum", value = "1", scalar = Scalar.Integer) })
@DisplayName("InitialModelLoader Late Package Seeding Integration Tests")
public class InitialModelLoaderLatePackageSeedingIntegrationTest {

    static final String SCOPE_NAME = "bootstrap-late-scope";
    static final String REGISTRY_NAME = "objects";
    static final String NS_URI = "http://test.fennec.eclipse.org/bootstrap/late/person/1.0.0";

    private static final String LOADER_PID = "InitialModelLoader";
    private static final String REQUIRED_MODELS_PID = "InitialModelLoaderRequiredModels";
    private static final String READY_CONDITION = "atlas.initial.models.ready";
    private static final String STAGE = CommonTestAnnotations.STAGE_RELEASE;
    private static final String MODEL_ENTRY = "/test-data/late-package-seeding/late-person.ecore";

    /**
     * The tests.common configuration annotations template their storage folders
     * from the 'tempDir' system property; this extension must be registered BEFORE
     * the {@link ConfigurationExtension} so the property exists when the
     * class-level configurations are created.
     */
    public static class TempDirPropertyExtension implements BeforeAllCallback, AfterAllCallback {

        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            System.setProperty(CommonTestAnnotations.PROP_TEMP_DIR,
                    Files.createTempDirectory("bootstrap-late-test-").toString());
        }

        @Override
        public void afterAll(ExtensionContext context) {
            // leave the folder for the OS to clean up - Lucene may still hold
            // file locks on Windows
            System.clearProperty(CommonTestAnnotations.PROP_TEMP_DIR);
        }
    }

    @TempDir
    Path tempDir;

    private Configuration loaderConfiguration;
    private Configuration requiredModelsConfiguration;
    private ServiceRegistration<EPackage> packageRegistration;
    private ServiceRegistration<EPackageConfigurator> configuratorRegistration;
    private ServiceRegistration<Condition> conditionRegistration;

    @AfterEach
    void resetConfiguration() throws IOException {
        if (requiredModelsConfiguration != null) {
            requiredModelsConfiguration.delete();
            requiredModelsConfiguration = null;
        }
        if (loaderConfiguration != null) {
            loaderConfiguration.delete();
            loaderConfiguration = null;
        }
        unregisterQuietly(packageRegistration);
        packageRegistration = null;
        unregisterQuietly(configuratorRegistration);
        configuratorRegistration = null;
        unregisterQuietly(conditionRegistration);
        conditionRegistration = null;
    }

    @Test
    @DisplayName("An instance is seeded although its EPackage only arrives after the loader has read the folder")
    public void instanceIsSeededWhenItsPackageArrivesLate(@InjectBundleContext BundleContext context,
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, timeout = 30000,
                    filter = "(atlas.scope=" + SCOPE_NAME + ")") ServiceAware<WritableScopeService> scopeAware,
            @InjectService(cardinality = 0, filter = "(osgi.condition.id=" + READY_CONDITION
                    + ")") ServiceAware<Condition> readyAware)
            throws Exception {

        copyTestData(context, "late-package-seeding/seed", tempDir);
        ConfigurationAdmin cm = cmAware.waitForService(5000);
        applyRequiredModelsConfiguration(cm);
        applyLoaderConfiguration(cm, tempDir);

        // The package is not registered yet: the loader has to wait for it, and neither the
        // registry nor the scope can come up.
        assertNull(scopeAware.waitForService(5000),
                "The scope must not come up before the package its registry is rooted in");
        assertNull(readyAware.getService(),
                "The loader's condition must not be there before the required package");

        // The model bundle starts.
        registerLatePackage(context);

        assertNotNull(readyAware.waitForService(10000),
                "The loader's condition should come up once the required package is registered");

        assertNotNull(scopeAware.waitForService(30000),
                "The scope service should appear once the package its registry needs is registered");

        ObjectMetadata carol = awaitObject(scopeAware, "carol", 30000);
        assertNotNull(carol, "The instance must be seeded once its EPackage is registered");
        assertEquals(SCOPE_NAME, carol.getScope());
        assertEquals(REGISTRY_NAME, carol.getRegistry());
        assertEquals(STAGE, carol.getStage());
        assertEquals(NS_URI + "#//Person", carol.getObjectType(),
                "The object type should be the instance's EClass URI");
    }

    // ---- helpers (mirrors InitialModelLoaderRegistrySeedingIntegrationTest) ----

    /**
     * Registers the fixture package exactly as a model bundle would: an
     * {@link EPackageConfigurator} first, so an aggregating registry sees it
     * before the {@link EPackage} service itself is looked up.
     */
    private void registerLatePackage(BundleContext context) throws IOException {
        URL entry = context.getBundle().getEntry(MODEL_ENTRY);
        assertNotNull(entry, "Fixture " + MODEL_ENTRY + " not found - check -includeresource in bnd.bnd");

        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
                new XMIResourceFactoryImpl());
        Resource resource = resourceSet.createResource(URI.createURI(entry.toString()));
        resource.load(null);
        EPackage ePackage = (EPackage) resource.getContents().get(0);
        // A registered package is addressed by its identity, not by the file it was read from -
        // the same alignment InitialModelLoader performs on the packages it seeds. Without it the
        // package would keep the bundle:// URL the fixture was read from and the seeded object
        // would record that location as its type.
        resource.setURI(URI.createURI(ePackage.getNsURI()));

        Dictionary<String, String> properties = new Hashtable<>();
        properties.put(EMFNamespaces.EMF_NAME, ePackage.getName());
        properties.put(EMFNamespaces.EMF_MODEL_NSURI, ePackage.getNsURI());
        properties.put(EMFNamespaces.EMF_MODEL_REGISTRATION, EMFNamespaces.MODEL_REGISTRATION_DYNAMIC);
        // the EPackage registries track configurators with (emf.model.scope=resourceset); without
        // it the package never reaches the ResourceSet services a registry targets by emf.name
        properties.put(EMFNamespaces.EMF_MODEL_SCOPE, EMFNamespaces.EMF_MODEL_SCOPE_RESOURCE_SET);

        configuratorRegistration = context.registerService(EPackageConfigurator.class,
                new DynamicEPackageConfigurator(ePackage), properties);
        packageRegistration = context.registerService(EPackage.class, ePackage, properties);
        // last, as a generated model bundle does: the package is there once its condition is
        Dictionary<String, Object> conditionProperties = new Hashtable<>();
        conditionProperties.put(Condition.CONDITION_ID, ePackage.getNsURI());
        conditionRegistration = context.registerService(Condition.class, Condition.INSTANCE, conditionProperties);
    }

    private static void unregisterQuietly(ServiceRegistration<?> registration) {
        if (registration == null) {
            return;
        }
        try {
            registration.unregister();
        } catch (IllegalStateException e) {
            // already gone with the framework
        }
    }

    private ObjectMetadata awaitObject(ServiceAware<WritableScopeService> scopeAware, String objectId, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            WritableScopeService<?> scopeService = scopeAware.waitForService(1000);
            if (scopeService != null) {
                try {
                    ObjectMetadata metadata = scopeService.getMetadataFromStageForRegistry(REGISTRY_NAME, STAGE,
                            objectId);
                    if (metadata != null) {
                        return metadata;
                    }
                } catch (IllegalArgumentException e) {
                    // the registry is not (re)bound to the scope yet - keep polling
                }
            }
            Thread.sleep(250);
        }
        return null;
    }

    private void applyRequiredModelsConfiguration(ConfigurationAdmin cm) throws IOException {
        requiredModelsConfiguration = cm.getConfiguration(REQUIRED_MODELS_PID, "?");
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("required.models", new String[] { NS_URI });
        requiredModelsConfiguration.update(properties);
    }

    private void applyLoaderConfiguration(ConfigurationAdmin cm, Path folder) throws IOException {
        loaderConfiguration = cm.getConfiguration(LOADER_PID, "?");
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("initial.models.folder", folder.toAbsolutePath().toString());
        // never stop the test framework when a deployment fails
        properties.put("halt.on.error", false);
        loaderConfiguration.update(properties);
    }

    private static void copyTestData(BundleContext context, String scenario, Path target) throws IOException {
        Bundle bundle = context.getBundle();
        String root = "/test-data/" + scenario;
        Enumeration<URL> entries = bundle.findEntries(root, "*", true);
        assertNotNull(entries, "No bundle entries found at " + root + " - check -includeresource in bnd.bnd");

        String prefix = root + "/";
        int copied = 0;
        while (entries.hasMoreElements()) {
            URL entry = entries.nextElement();
            String path = entry.getPath();
            if (path.endsWith("/")) {
                continue;
            }
            int idx = path.indexOf(prefix);
            if (idx < 0) {
                continue;
            }
            String relative = path.substring(idx + prefix.length());
            Path dest = target.resolve(relative);
            Files.createDirectories(dest.getParent());
            try (InputStream in = entry.openStream()) {
                Files.copy(in, dest);
            }
            copied++;
        }
        assertTrue(copied > 0, "Expected at least one fixture file in " + root);
    }
}
