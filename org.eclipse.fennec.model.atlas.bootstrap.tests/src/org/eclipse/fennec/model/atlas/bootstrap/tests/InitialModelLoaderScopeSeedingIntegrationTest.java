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
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
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
 * OSGi integration tests for the per-scope seeding of the
 * {@code InitialModelLoader} (issue #175, D3): models below
 * {@code scopes/<scopeName>/} are uploaded into that scope's schema registry
 * instead of being registered globally.
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(InitialModelLoaderScopeSeedingIntegrationTest.TempDirPropertyExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
// The full scope stack, mirroring runtime.config.docker.file/configs/workflow.json. This
// duplicates parts of CommonTestAnnotations.SchemaRegistryServiceSetup because the registry
// here must additionally target the stage action service WITH a minimum cardinality —
// otherwise the RegistryService's static stageActionService list races the
// EPackageStageActionService activation and uploads silently skip the EPackage registration.
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
@WithFactoryConfiguration(factoryPid = "EPackageStageActionService", name = "bootstrap-stage-action", location = "?", properties = {
        @Property(key = "storageService.target", value = "(storage.type=file)"),
        @Property(key = "trigger.stages", scalar = Scalar.String, type = Type.Array, value = { "draft", "approved",
                "release" }) })
@WithFactoryConfiguration(factoryPid = "RegistryService", name = CommonTestAnnotations.SCHEMA_REGISTRY_NAME, location = "?", properties = {
        @Property(key = "registry.name", value = CommonTestAnnotations.SCHEMA_REGISTRY_NAME),
        @Property(key = "registry.type", value = "SCHEMA"),
        @Property(key = "schema.uri", value = "http://www.eclipse.org/emf/2002/Ecore"),
        @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
        @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
        @Property(key = "storageService.target", value = "(storage.type=file)"),
        @Property(key = "storageService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
        @Property(key = "stageActionService.target", value = "(component.name=EPackageStageActionService)"),
        @Property(key = "stageActionService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
        @Property(key = "registry.target", value = "(registry=main)"),
        @Property(key = "stages", type = Type.Array, value = {
                "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                "{ \"name\" : \"approved\", \"writable\" : true, \"final\": false}",
                "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
        @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:approved", "approved:release" }),
        @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "approved:file",
                "release:file" }) })
@WithFactoryConfiguration(factoryPid = "ScopeService", name = InitialModelLoaderScopeSeedingIntegrationTest.SCOPE_NAME, location = "?", properties = {
        @Property(key = "atlas.scope", value = InitialModelLoaderScopeSeedingIntegrationTest.SCOPE_NAME),
        @Property(key = "scope.name", value = InitialModelLoaderScopeSeedingIntegrationTest.SCOPE_NAME),
        @Property(key = "registryService.target", value = "(registry.name=" + CommonTestAnnotations.SCHEMA_REGISTRY_NAME
                + ")"),
        @Property(key = "registryService.cardinality.minimum", value = "1", scalar = Scalar.Integer) })
@DisplayName("InitialModelLoader Scope Seeding Integration Tests")
public class InitialModelLoaderScopeSeedingIntegrationTest {

    static final String SCOPE_NAME = "bootstrap-scope";

    private static final String LOADER_PID = "InitialModelLoader";
    private static final String NS_URI = "http://test.fennec.eclipse.org/bootstrap/scoped/person/1.0.0";

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
                    Files.createTempDirectory("bootstrap-scope-test-").toString());
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

    @AfterEach
    void resetConfiguration() throws IOException {
        if (loaderConfiguration != null) {
            loaderConfiguration.delete();
            loaderConfiguration = null;
        }
    }

    @Test
    @DisplayName("Models below scopes/<scope>/ are uploaded into the scope's schema registry and seeding is idempotent")
    public void scopeFolderIsSeededIntoScopeRegistry(@InjectBundleContext BundleContext context,
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, timeout = 30000,
                    filter = "(atlas.scope=" + SCOPE_NAME + ")") ServiceAware<WritableScopeService> scopeAware,
            @InjectService(cardinality = 0, timeout = 30000,
                    filter = "(&(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + NS_URI + ")("
                            + WorkflowConstants.ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY + "="
                            + CommonTestAnnotations.STAGE_DRAFT + "))") ServiceAware<EPackage> ePackageAware)
            throws Exception {

        copyTestData(context, "scope-seeding", tempDir);
        applyLoaderConfiguration(cmAware.waitForService(5000), tempDir);

        // the loader waits for the scope service and then uploads through the same
        // chain a REST upload takes; the ENTER stage action registers the EPackage
        EPackage ePackage = ePackageAware.waitForService(30000);
        assertNotNull(ePackage, "The seeded EPackage should be registered for the scope's draft stage");
        assertEquals("scopedPerson", ePackage.getName());

        WritableScopeService<?> scopeService = scopeAware.waitForService(5000);
        assertNotNull(scopeService, "The test scope service should be available");
        List<ObjectMetadata> metadata = scopeService.getMetadataByPropertyFromStageForRegistry(
                CommonTestAnnotations.SCHEMA_REGISTRY_NAME, CommonTestAnnotations.STAGE_DRAFT, "nsUri", NS_URI);
        assertEquals(1, metadata.size(), "The seeded package should be stored in the scope's schema registry");
        ObjectMetadata seeded = metadata.get(0);
        assertEquals(SCOPE_NAME, seeded.getScope());
        assertEquals(CommonTestAnnotations.SCHEMA_REGISTRY_NAME, seeded.getRegistry());
        assertEquals(CommonTestAnnotations.STAGE_DRAFT, seeded.getStage());
        assertEquals("1.0.0", seeded.getVersion(), "The version should be derived from the nsURI");

        // restart the loader with the same folder: the nsURI is already present in
        // the stage, so the seeding must skip it instead of uploading a duplicate
        loaderConfiguration.delete();
        loaderConfiguration = null;
        Thread.sleep(1000);
        applyLoaderConfiguration(cmAware.waitForService(5000), tempDir);
        Thread.sleep(2000);
        assertEquals(1,
                scopeService.getMetadataByPropertyFromStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                        CommonTestAnnotations.STAGE_DRAFT, "nsUri", NS_URI).size(),
                "Re-seeding the same folder must not create a duplicate");
    }

    // ---- helpers (mirrors InitialModelLoaderIntegrationTest) ----

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
