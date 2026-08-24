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

import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
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
 * OSGi integration tests for the per-registry seeding of the
 * {@code InitialModelLoader} (issue #198): files below
 * {@code scopes/<scopeName>/<registryName>/} are uploaded as instances into
 * that registry, validated against the registry's root EClass.
 *
 * <p>
 * The fixture mirrors the production case that motivated the issue: the domain
 * {@code .ecore} is boot-seeded at the top level of the initial-models folder,
 * and the target registry's {@code root.eclass.uri} points into that package —
 * so the registry can only activate after the bootstrap has registered the
 * package (its {@code resourceSet.target} names the boot-seeded model). The
 * loader must therefore hold the instance seeding back until the registry
 * appears.
 * </p>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(InitialModelLoaderRegistrySeedingIntegrationTest.TempDirPropertyExtension.class)
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
// The instance registry: its root EClass lives in the boot-seeded person package, so the
// resourceSet.target names that model — the registry only activates once the bootstrap
// has registered the package globally (production: issue #198, sensinactmapping).
@WithFactoryConfiguration(factoryPid = "RegistryService", name = InitialModelLoaderRegistrySeedingIntegrationTest.REGISTRY_NAME, location = "?", properties = {
        @Property(key = "registry.name", value = InitialModelLoaderRegistrySeedingIntegrationTest.REGISTRY_NAME),
        @Property(key = "registry.type", value = "OTHER"),
        @Property(key = "schema.uri", value = InitialModelLoaderRegistrySeedingIntegrationTest.NS_URI),
        @Property(key = "root.eclass.uri", value = InitialModelLoaderRegistrySeedingIntegrationTest.NS_URI
                + "#//Person"),
        @Property(key = "resourceSet.target", value = "(emf.name=registryPerson)"),
        @Property(key = "storageService.target", value = "(storage.type=file)"),
        @Property(key = "storageService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
        @Property(key = "registry.target", value = "(registry=main)"),
        @Property(key = "stages", type = Type.Array, value = {
                "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
        @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
        @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
@WithFactoryConfiguration(factoryPid = "ScopeService", name = InitialModelLoaderRegistrySeedingIntegrationTest.SCOPE_NAME, location = "?", properties = {
        @Property(key = "atlas.scope", value = InitialModelLoaderRegistrySeedingIntegrationTest.SCOPE_NAME),
        @Property(key = "scope.name", value = InitialModelLoaderRegistrySeedingIntegrationTest.SCOPE_NAME),
        @Property(key = "registryService.target", value = "(registry.name="
                + InitialModelLoaderRegistrySeedingIntegrationTest.REGISTRY_NAME + ")"),
        @Property(key = "registryService.cardinality.minimum", value = "1", scalar = Scalar.Integer) })
@DisplayName("InitialModelLoader Registry Seeding Integration Tests")
public class InitialModelLoaderRegistrySeedingIntegrationTest {

    static final String SCOPE_NAME = "bootstrap-registry-scope";
    static final String REGISTRY_NAME = "objects";
    static final String NS_URI = "http://test.fennec.eclipse.org/bootstrap/registry/person/1.0.0";

    private static final String LOADER_PID = "InitialModelLoader";
    private static final String STAGE = CommonTestAnnotations.STAGE_RELEASE;

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
                    Files.createTempDirectory("bootstrap-registry-test-").toString());
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
    @DisplayName("XMI files below scopes/<scope>/<registry>/ are seeded as instances into that registry (default release stage)")
    public void registryFolderIsSeededAsInstances(@InjectBundleContext BundleContext context,
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, timeout = 30000,
                    filter = "(atlas.scope=" + SCOPE_NAME + ")") ServiceAware<WritableScopeService> scopeAware)
            throws Exception {

        copyTestData(context, "registry-seeding", tempDir);
        applyLoaderConfiguration(cmAware.waitForService(5000), tempDir);

        // the scope only activates once the 'objects' registry does, which in turn
        // only activates once the bootstrap has registered the person package
        assertNotNull(scopeAware.waitForService(30000), "The test scope service should be available");

        // the object id comes from the instance's EMF ID attribute ('pid')
        ObjectMetadata alice = awaitObject(scopeAware, "alice", 30000);
        assertNotNull(alice, "The instance with an ID attribute should be seeded under its id");
        assertEquals(SCOPE_NAME, alice.getScope());
        assertEquals(REGISTRY_NAME, alice.getRegistry());
        assertEquals(STAGE, alice.getStage());
        assertEquals("alice", alice.getObjectName());
        assertTrue(alice.getObjectType().endsWith("#//Person"),
                "The object type should be the instance's EClass URI but was " + alice.getObjectType());

        // an instance without an ID value falls back to the file name
        ObjectMetadata bob = awaitObject(scopeAware, "bob-file", 10000);
        assertNotNull(bob, "The instance without an ID value should be seeded under its file name");

        // Restart the loader with the same folder: the object ids are already
        // present in the stage, so re-seeding must skip them (upload time
        // unchanged). Deleting the loader config unregisters the person package,
        // which bounces the registry and with it the scope service - awaitObject
        // rides that out by re-fetching the service on every poll.
        Instant firstUpload = alice.getUploadTime();
        loaderConfiguration.delete();
        loaderConfiguration = null;
        Thread.sleep(1000);
        applyLoaderConfiguration(cmAware.waitForService(5000), tempDir);
        ObjectMetadata reread = awaitObject(scopeAware, "alice", 30000);
        assertNotNull(reread, "The seeded instance must survive a loader restart");
        assertEquals(firstUpload, reread.getUploadTime(),
                "Re-seeding the same folder must not overwrite the existing instance");
    }

    // ---- helpers (mirrors InitialModelLoaderScopeSeedingIntegrationTest) ----

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

    private void applyLoaderConfiguration(ConfigurationAdmin cm, Path folder) throws IOException {
        loaderConfiguration = cm.getConfiguration(LOADER_PID, "?");
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("initial.models.folder", folder.toAbsolutePath().toString());
        // never stop the test framework when a deployment fails; the stage is
        // deliberately NOT set - this test covers the 'release' default
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
