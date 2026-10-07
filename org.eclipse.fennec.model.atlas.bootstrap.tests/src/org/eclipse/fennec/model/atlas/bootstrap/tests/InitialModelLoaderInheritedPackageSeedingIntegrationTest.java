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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
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
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
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
 * Issue #339: an instance the {@code InitialModelLoader} seeds records the object type a
 * REST upload of it records - its class by nsURI - so a later update of it can be
 * promoted through the stage workflow.
 *
 * <p>
 * The fixture follows the reported layout: the {@code platform} scope holds the
 * configuration model and an instance of it; its child scope {@code dimcity} holds a model
 * extending it - by a relative href into the platform folder - and instances of both.
 * </p>
 *
 * <p>
 * Two things used to make an instance record a file instead of an nsURI. The loader worked
 * through the folder piece by piece, so a package could be read a second time, through a
 * relative href, after its own scope had already been seeded - and a scope seeded before
 * its parent did not find the parent's package at all. And storing a package moved it into
 * the storage resource, so every instance seeded after it named the storage file of its
 * package ({@code .../platform/schema/release/<uuid>.xmi#//Configuration}). Promoting a new
 * version of such an object into the stage holding the seeded one was then refused as a
 * different object (409). The loader now loads and resolves the whole folder first and only
 * then gives the packages their nsURIs, and storing an object leaves it where it was.
 * </p>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(InitialModelLoaderInheritedPackageSeedingIntegrationTest.TempDirPropertyExtension.class)
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
@WithFactoryConfiguration(factoryPid = "EPackageStageActionService", name = "inherited-stage-action", location = "?", properties = {
        @Property(key = "storageService.target", value = "(storage.type=file)"),
        @Property(key = "trigger.stages", scalar = Scalar.String, type = Type.Array, value = { "draft", "release" }) })
@WithFactoryConfiguration(factoryPid = "RegistryService", name = CommonTestAnnotations.SCHEMA_REGISTRY_NAME, location = "?", properties = {
        @Property(key = "registry.name", value = CommonTestAnnotations.SCHEMA_REGISTRY_NAME),
        @Property(key = "registry.type", value = "SCHEMA"),
        @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
        @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
        @Property(key = "storageService.target", value = "(storage.type=file)"),
        @Property(key = "storageService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
        @Property(key = "stageActionService.target", value = "(component.name=EPackageStageActionService)"),
        @Property(key = "stageActionService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
        @Property(key = "registry.target", value = "(registry=main)"),
        @Property(key = "stages", type = Type.Array, value = {
                "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
        @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
        @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
@WithFactoryConfiguration(factoryPid = "RegistryService", name = InitialModelLoaderInheritedPackageSeedingIntegrationTest.REGISTRY, location = "?", properties = {
        @Property(key = "registry.name", value = InitialModelLoaderInheritedPackageSeedingIntegrationTest.REGISTRY),
        @Property(key = "registry.type", value = "OTHER"),
        @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EObject"),
        @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
        @Property(key = "storageService.target", value = "(storage.type=file)"),
        @Property(key = "storageService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
        @Property(key = "registry.target", value = "(registry=main)"),
        @Property(key = "stages", type = Type.Array, value = {
                "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
        @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
        @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
@WithFactoryConfiguration(factoryPid = "ScopeService", name = InitialModelLoaderInheritedPackageSeedingIntegrationTest.PARENT_SCOPE, location = "?", properties = {
        @Property(key = "atlas.scope", value = InitialModelLoaderInheritedPackageSeedingIntegrationTest.PARENT_SCOPE),
        @Property(key = "scope.name", value = InitialModelLoaderInheritedPackageSeedingIntegrationTest.PARENT_SCOPE),
        @Property(key = "registryService.target", value = "(|(registry.name=" + CommonTestAnnotations.SCHEMA_REGISTRY_NAME
                + ")(registry.name=" + InitialModelLoaderInheritedPackageSeedingIntegrationTest.REGISTRY + "))"),
        @Property(key = "registryService.cardinality.minimum", value = "2", scalar = Scalar.Integer) })
@WithFactoryConfiguration(factoryPid = "ScopeService", name = InitialModelLoaderInheritedPackageSeedingIntegrationTest.SCOPE, location = "?", properties = {
        @Property(key = "atlas.scope", value = InitialModelLoaderInheritedPackageSeedingIntegrationTest.SCOPE),
        @Property(key = "scope.name", value = InitialModelLoaderInheritedPackageSeedingIntegrationTest.SCOPE),
        @Property(key = "scope.parent", value = InitialModelLoaderInheritedPackageSeedingIntegrationTest.PARENT_SCOPE),
        @Property(key = "registryService.target", value = "(|(registry.name=" + CommonTestAnnotations.SCHEMA_REGISTRY_NAME
                + ")(registry.name=" + InitialModelLoaderInheritedPackageSeedingIntegrationTest.REGISTRY + "))"),
        @Property(key = "registryService.cardinality.minimum", value = "2", scalar = Scalar.Integer) })
@DisplayName("InitialModelLoader: seeded instances of an inherited package (issue #339)")
public class InitialModelLoaderInheritedPackageSeedingIntegrationTest {

    static final String PARENT_SCOPE = "platform";
    static final String SCOPE = "dimcity";
    static final String REGISTRY = "configurations";

    private static final String CONFIGURATION_NS = "http://test.fennec.eclipse.org/bootstrap/inherited/configuration/1.0.0";
    private static final String AREA_NS = "http://test.fennec.eclipse.org/bootstrap/inherited/area/1.0.0";
    private static final String LOADER_PID = "InitialModelLoader";
    private static final String DRAFT = "draft";
    private static final String RELEASE = "release";

    /** Sets the 'tempDir' property the storage configurations are templated from, before they are created. */
    public static class TempDirPropertyExtension implements BeforeAllCallback, AfterAllCallback {

        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            System.setProperty(CommonTestAnnotations.PROP_TEMP_DIR,
                    Files.createTempDirectory("bootstrap-inherited-test-").toString());
        }

        @Override
        public void afterAll(ExtensionContext context) {
            // the folder is left for the OS to clean up - Lucene may still hold file locks on Windows
            System.clearProperty(CommonTestAnnotations.PROP_TEMP_DIR);
        }
    }

    @TempDir
    Path tempDir;

    private Configuration loaderConfiguration;

    @AfterEach
    void resetConfiguration() throws IOException {
        if (loaderConfiguration != null) {
            loaderConfiguration.delete();
            loaderConfiguration = null;
        }
    }

    @Test
    @DisplayName("Seeded instances record their class by nsURI, and a new version can be promoted over one")
    public void seededInstancesRecordTheirClassByNsUri(@InjectBundleContext BundleContext context,
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, timeout = 30000,
                    filter = "(atlas.scope=" + PARENT_SCOPE + ")") ServiceAware<WritableScopeService> platformAware,
            @InjectService(cardinality = 0, timeout = 30000,
                    filter = "(atlas.scope=" + SCOPE + ")") ServiceAware<WritableScopeService> dimcityAware)
            throws Exception {

        copyTestData(context, "inherited-seeding", tempDir);
        applyLoaderConfiguration(cmAware.waitForService(5000), tempDir);

        // An instance next to its package: seeded after the package was stored.
        ObjectMetadata dataAtlas = awaitObject(platformAware, "dataatlas", 30000);
        assertNotNull(dataAtlas, "The instance should be seeded into " + PARENT_SCOPE);
        assertEquals(CONFIGURATION_NS + "#//Configuration", dataAtlas.getObjectType(),
                "The object type names the class by its package's nsURI, as a REST upload records it");

        // The issue's case: an instance in the child scope of a package of the parent scope.
        ObjectMetadata inherited = awaitObject(dimcityAware, "inherited", 30000);
        assertNotNull(inherited, "The instance of the parent's model should be seeded into " + SCOPE);
        assertEquals(CONFIGURATION_NS + "#//Configuration", inherited.getObjectType(),
                "The object type of an instance of the parent's model names it by nsURI");

        // A class of the child scope whose super type lives in the parent's folder.
        ObjectMetadata north = awaitObject(dimcityAware, "north", 10000);
        assertNotNull(north, "The instance of the child's model should be seeded into " + SCOPE);
        assertEquals(AREA_NS + "#//AreaConfiguration", north.getObjectType(),
                "The object type of a class extending another folder's class names it by nsURI");

        // The workflow of the issue: a new version of the seeded object goes into draft -
        // recorded as a REST upload records it - and is promoted over the seeded one.
        @SuppressWarnings("unchecked")
        WritableScopeService<EObject> scopeService = platformAware.waitForService(5000);
        EObject seeded = scopeService.getContentFromStageForRegistry(REGISTRY, RELEASE, "dataatlas");
        assertNotNull(seeded, "The seeded instance can be read back");
        EObject update = EcoreUtil.copy(seeded);
        update.eSet(update.eClass().getEStructuralFeature("name"), "Data Atlas, updated");
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId("dataatlas");
        metadata.setObjectName("dataatlas");
        metadata.setUploadTime(Instant.now());
        metadata.setScope(PARENT_SCOPE);
        metadata.setRegistry(REGISTRY);
        metadata.setStage(DRAFT);
        metadata.setObjectType(EcoreUtil.getURI(update.eClass()).toString());
        assertEquals(dataAtlas.getObjectType(), metadata.getObjectType(),
                "The update is recorded with the type the seeded object carries");
        scopeService.uploadToStageForRegistry(REGISTRY, DRAFT, update, metadata).getValue();

        ObjectMetadata promoted = scopeService.transitionToStageForRegistry(REGISTRY, "dataatlas", DRAFT, RELEASE);
        assertNotNull(promoted, "The new version is promoted over the seeded one");
        EObject released = scopeService.getContentFromStageForRegistry(REGISTRY, RELEASE, "dataatlas");
        assertEquals("Data Atlas, updated", released.eGet(released.eClass().getEStructuralFeature("name")));
    }

    private ObjectMetadata awaitObject(ServiceAware<WritableScopeService> scopeAware, String objectId, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            WritableScopeService<?> scopeService = scopeAware.waitForService(1000);
            if (scopeService != null) {
                try {
                    ObjectMetadata metadata = scopeService.getMetadataFromStageForRegistry(REGISTRY, RELEASE,
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

    private void applyLoaderConfiguration(ConfigurationAdmin cm, Path folder) throws IOException {
        loaderConfiguration = cm.getConfiguration(LOADER_PID, "?");
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("initial.models.folder", folder.toAbsolutePath().toString());
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
            Path dest = target.resolve(path.substring(idx + prefix.length()));
            Files.createDirectories(dest.getParent());
            try (InputStream in = entry.openStream()) {
                Files.copy(in, dest);
            }
            copied++;
        }
        assertTrue(copied > 0, "Expected at least one fixture file in " + root);
    }
}
