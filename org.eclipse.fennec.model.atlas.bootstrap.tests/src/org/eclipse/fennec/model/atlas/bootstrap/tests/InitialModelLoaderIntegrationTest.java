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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * OSGi integration tests for the {@code InitialModelLoader} component.
 *
 * <p>
 * Each test copies a pre-built fixture from {@code test-data/<scenario>/} (bundled
 * into this test bundle via {@code -includeresource}) into a fresh
 * {@link TempDir}, then drives a configuration update of the
 * {@code InitialModelLoader} singleton PID via {@link ConfigurationAdmin}.
 * Ordering is therefore deterministic: copy fixture → (re)configure → wait for
 * service.
 * </p>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("InitialModelLoader OSGi Integration Tests")
public class InitialModelLoaderIntegrationTest {

    private static final String LOADER_PID = "InitialModelLoader";
    private static final String NS_BASE = "http://test.fennec.eclipse.org/bootstrap";

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
    @DisplayName("An .ecore in the configured folder is registered as EPackage + EPackageConfigurator")
    public void ecoreFileGetsRegistered(@InjectBundleContext BundleContext context,
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, timeout = 15000,
                    filter = "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + NS_BASE + "/single)") ServiceAware<EPackage> ePackageAware,
            @InjectService(cardinality = 0, timeout = 15000,
                    filter = "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + NS_BASE + "/single)") ServiceAware<EPackageConfigurator> configuratorAware)
            throws Exception {

        copyTestData(context, "single", tempDir);
        applyLoaderConfiguration(cmAware.waitForService(5000), tempDir);

        EPackage ePackage = ePackageAware.waitForService(15000);
        assertNotNull(ePackage, "Loader should publish an EPackage service for the staged .ecore");
        assertEquals("single", ePackage.getName());
        assertEquals(NS_BASE + "/single", ePackage.getNsURI());

        EPackageConfigurator configurator = configuratorAware.waitForService(15000);
        assertNotNull(configurator, "Loader should publish an EPackageConfigurator for the staged .ecore");
    }

    @Test
    @DisplayName("An .ecore in a subfolder is also discovered")
    public void ecoreInSubFolderIsRegistered(@InjectBundleContext BundleContext context,
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, timeout = 15000,
                    filter = "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + NS_BASE + "/sub)") ServiceAware<EPackage> ePackageAware)
            throws Exception {

        copyTestData(context, "subfolder", tempDir);
        applyLoaderConfiguration(cmAware.waitForService(5000), tempDir);

        EPackage ePackage = ePackageAware.waitForService(15000);
        assertNotNull(ePackage, "Loader should also pick up models from subfolders");
        assertEquals("subTest", ePackage.getName());
    }

    @Test
    @DisplayName("Subpackages of a loaded EPackage are registered as separate services")
    public void subPackageIsRegistered(@InjectBundleContext BundleContext context,
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, timeout = 15000,
                    filter = "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + NS_BASE + "/parent)") ServiceAware<EPackage> parentAware,
            @InjectService(cardinality = 0, timeout = 15000,
                    filter = "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + NS_BASE + "/parent/child)") ServiceAware<EPackage> childAware)
            throws Exception {

        copyTestData(context, "nested", tempDir);
        applyLoaderConfiguration(cmAware.waitForService(5000), tempDir);

        assertNotNull(parentAware.waitForService(15000), "Parent EPackage should be registered");
        assertNotNull(childAware.waitForService(15000), "Subpackage EPackage should be registered");
    }

    @Test
    @DisplayName("A .qvto file becomes a QVTModelTransformator factory configuration")
    public void qvtoFileBecomesFactoryConfiguration(@InjectBundleContext BundleContext context,
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, timeout = 15000,
                    filter = "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + NS_BASE + "/qvtMarker)") ServiceAware<EPackage> ePackageAware)
            throws Exception {

        // The QVT engine is not in this runtime, so we cannot wait for a real
        // QVTModelTransformator service. Instead we wait for the marker EPackage
        // (which the loader publishes after the QVT registration step) and then
        // verify the QVTModelTransformator factory configuration was created.
        copyTestData(context, "qvt", tempDir);
        ConfigurationAdmin cm = cmAware.waitForService(5000);
        applyLoaderConfiguration(cm, tempDir);

        assertNotNull(ePackageAware.waitForService(15000), "Sibling .ecore should be registered");

        Configuration[] qvtConfigs = cm.listConfigurations("(transformator.id=transform-test)");
        assertNotNull(qvtConfigs, "Loader should have created a QVTModelTransformator factory configuration");
        assertEquals(1, qvtConfigs.length, "Exactly one QVT factory configuration expected");
        assertEquals("transform-test", qvtConfigs[0].getProperties().get("transformator.id"));
        assertTrue(qvtConfigs[0].getProperties().get("qvt.template.uri").toString().endsWith("transform.qvto"));
    }

    @Test
    @DisplayName("A .jsonschema is registered as an EPackage via the Fennec codec")
    public void jsonSchemaIsRegisteredAsEPackage(@InjectBundleContext BundleContext context,
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, timeout = 15000,
                    filter = "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + NS_BASE + "/jsonschema)") ServiceAware<EPackage> ePackageAware)
            throws Exception {

        copyTestData(context, "jsonschema", tempDir);
        applyLoaderConfiguration(cmAware.waitForService(5000), tempDir);

        EPackage ePackage = ePackageAware.waitForService(15000);
        assertNotNull(ePackage, "Loader should publish an EPackage service for the staged .jsonschema");
        assertEquals(NS_BASE + "/jsonschema", ePackage.getNsURI());

        assertFalse(ePackage.getEClassifiers().isEmpty(),
                "EPackage derived from the json schema should expose at least one EClassifier");
        EClassifier classifier = ePackage.getEClassifier("JsonSchemaEntity");
        assertNotNull(classifier, "EPackage derived from the json schema should contain a 'JsonSchemaEntity' EClass");
        assertTrue(classifier instanceof EClass, "JsonSchemaEntity should be an EClass");
    }

    @Test
    @DisplayName("Three .ecores referencing each other have all cross-references resolved")
    public void crossReferencingEcoresAreFullyResolved(@InjectBundleContext BundleContext context,
            @InjectService(cardinality = 0) ServiceAware<ConfigurationAdmin> cmAware,
            @InjectService(cardinality = 0, timeout = 15000,
                    filter = "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + NS_BASE + "/crossref/a)") ServiceAware<EPackage> aAware,
            @InjectService(cardinality = 0, timeout = 15000,
                    filter = "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + NS_BASE + "/crossref/b)") ServiceAware<EPackage> bAware,
            @InjectService(cardinality = 0, timeout = 15000,
                    filter = "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + NS_BASE + "/crossref/c)") ServiceAware<EPackage> cAware)
            throws Exception {

        copyTestData(context, "crossref", tempDir);
        applyLoaderConfiguration(cmAware.waitForService(5000), tempDir);

        EPackage a = aAware.waitForService(15000);
        EPackage b = bAware.waitForService(15000);
        EPackage c = cAware.waitForService(15000);
        assertNotNull(a, "EPackage a should be registered");
        assertNotNull(b, "EPackage b should be registered");
        assertNotNull(c, "EPackage c should be registered");

        EClass aThing = (EClass) a.getEClassifier("AThing");
        EClass bThing = (EClass) b.getEClassifier("BThing");
        EClass cThing = (EClass) c.getEClassifier("CThing");
        assertNotNull(aThing, "AThing should exist in package a");
        assertNotNull(bThing, "BThing should exist in package b");
        assertNotNull(cThing, "CThing should exist in package c");

        assertReferenceResolvesTo(aThing, "b", bThing);
        assertReferenceResolvesTo(bThing, "c", cThing);
        assertReferenceResolvesTo(cThing, "a", aThing);
    }

    private static void assertReferenceResolvesTo(EClass owner, String referenceName, EClass expectedTarget) {
        EReference reference = (EReference) owner.getEStructuralFeature(referenceName);
        assertNotNull(reference,
                () -> owner.getName() + " should declare a '" + referenceName + "' reference");
        EClass actualTarget = reference.getEReferenceType();
        assertNotNull(actualTarget, () -> owner.getName() + "." + referenceName + " should have a resolved eType");
        assertFalse(((InternalEObject) actualTarget).eIsProxy(),
                () -> owner.getName() + "." + referenceName + " should not be a proxy");
        assertEquals(expectedTarget.getName(), actualTarget.getName(),
                () -> owner.getName() + "." + referenceName + " should point to " + expectedTarget.getName());
        assertSame(expectedTarget, actualTarget,
                () -> owner.getName() + "." + referenceName
                        + " should be the exact EClass instance from the registered EPackage");
    }

    // ---- helpers ----

    private void applyLoaderConfiguration(ConfigurationAdmin cm, Path folder) throws IOException {
        loaderConfiguration = cm.getConfiguration(LOADER_PID, "?");
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("initial.models.folder", folder.toAbsolutePath().toString());
        loaderConfiguration.update(properties);
    }

    /**
     * Copies the {@code test-data/<scenario>/} subtree bundled with this test bundle
     * into {@code target}, preserving the relative directory structure.
     */
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
                continue; // directory entry, contents will be enumerated separately
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
