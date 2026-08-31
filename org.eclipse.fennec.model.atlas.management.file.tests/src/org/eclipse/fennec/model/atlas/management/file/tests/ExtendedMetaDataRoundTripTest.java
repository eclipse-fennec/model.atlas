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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.file.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.StorageSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Regression test for issue #213: an instance of a model that carries
 * {@code ExtendedMetaData} XML names must survive the store/retrieve round trip
 * of the file storage.
 *
 * <p>
 * Before the fix the write path selected the resource factory by the (default)
 * content type {@code application/xml} — an ExtendedMetaData-aware XML factory
 * that wrote {@code columnDefinition} as {@code column-definition} — while the
 * read path selected it by the {@code .xmi} extension, i.e. plain XMI, which
 * then failed with {@code FeatureNotFoundException: Feature 'column-definition'
 * not found}.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class ExtendedMetaDataRoundTripTest {

    private static final String TEST_SCOPE = "emd_scope";
    private static final String TEST_REGISTRY = "configurations";
    private static final String TEST_STAGE = "release";
    private static final String OBJECT_ID = "myobject";

    /** Mirrors the shape of eorm's EntityMappings/Column (issue #213). */
    private static final String NS_URI = "https://eclipse.org/fennec/model/atlas/tests/emd/1.0.0";

    @InjectBundleContext
    BundleContext context;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        assertNotNull(context, "BundleContext should not be null");
        assertNotNull(tempDir, "TempDir should not be null");
        System.setProperty(CommonTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testExtendedMetaDataInstanceRoundTrip(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        EPackage ePackage = createExtendedMetaDataPackage();
        // Make the dynamic model resolvable for the read path the way the workflow
        // bundle does for uploaded schemas: as an EPackageConfigurator service, which
        // the default ResourceSet EPackage.Registry applies to every ResourceSet it
        // backs — including the management ResourceSet the storage reads through when
        // no StageResourceSetProvider is deployed (as in this test runtime).
        ServiceRegistration<EPackageConfigurator> configuratorRegistration = context.registerService(
                EPackageConfigurator.class, new TestEPackageConfigurator(ePackage), configuratorProperties(ePackage));

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");
        try {
            EClass mappingsClass = (EClass) ePackage.getEClassifier("EntityMappings");
            EClass columnClass = (EClass) ePackage.getEClassifier("Column");

            EObject column = ePackage.getEFactoryInstance().create(columnClass);
            column.eSet(columnClass.getEStructuralFeature("name"), "time");
            column.eSet(columnClass.getEStructuralFeature("columnDefinition"), "TIMESTAMPTZ");
            EObject mappings = ePackage.getEFactoryInstance().create(mappingsClass);
            ((List<EObject>) mappings.eGet(mappingsClass.getEStructuralFeature("columns"))).add(column);

            // Deliberately neither file.extension nor content.type: the defaults
            // (.xmi / application/xml) are exactly what an upload through the REST
            // API produces — the configuration under which #213 was observed.
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setUploadUser("testUser");
            metadata.setUploadTime(Instant.now());
            metadata.setSourceChannel("TEST");

            storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, OBJECT_ID, mappings, metadata)
                    .getValue();

            Path storedFile = tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE)
                    .resolve(TEST_REGISTRY).resolve(TEST_STAGE).resolve(OBJECT_ID + ".xmi");
            assertTrue(Files.exists(storedFile), "Object should be stored as " + storedFile);

            // Before the fix this failed with "Failed to retrieve object", caused by
            // FeatureNotFoundException: Feature 'column-definition' not found.
            EObject retrieved = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, OBJECT_ID)
                    .getValue();
            assertNotNull(retrieved, "Stored object must be readable again");
            assertEquals("EntityMappings", retrieved.eClass().getName());

            List<EObject> columns = (List<EObject>) retrieved
                    .eGet(retrieved.eClass().getEStructuralFeature("columns"));
            assertEquals(1, columns.size(), "Containment written under its XML element name must be read back");
            EObject retrievedColumn = columns.get(0);
            assertEquals("time", retrievedColumn.eGet(retrievedColumn.eClass().getEStructuralFeature("name")));
            assertEquals("TIMESTAMPTZ",
                    retrievedColumn.eGet(retrievedColumn.eClass().getEStructuralFeature("columnDefinition")),
                    "Attribute with an ExtendedMetaData name must round-trip");
        } finally {
            storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, OBJECT_ID).getValue();
            configuratorRegistration.unregister();
        }
    }

    /** Publishes the test model into the default ResourceSet EPackage.Registry. */
    private static final class TestEPackageConfigurator implements EPackageConfigurator {

        private final EPackage ePackage;

        TestEPackageConfigurator(EPackage ePackage) {
            this.ePackage = ePackage;
        }

        @Override
        public void configureEPackage(EPackage.Registry registry) {
            registry.put(ePackage.getNsURI(), ePackage);
        }

        @Override
        public void unconfigureEPackage(EPackage.Registry registry) {
            registry.remove(ePackage.getNsURI());
        }
    }

    private static Dictionary<String, Object> configuratorProperties(EPackage ePackage) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(EMFNamespaces.EMF_NAME, ePackage.getName());
        properties.put(EMFNamespaces.EMF_MODEL_NSURI, ePackage.getNsURI());
        properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, "emd");
        properties.put(EMFNamespaces.EMF_MODEL_VERSION, "1.0.0");
        // Selects the registry behind the (default) ResourceSetFactory.
        properties.put(EMFNamespaces.EMF_MODEL_SCOPE, EMFNamespaces.EMF_MODEL_SCOPE_RESOURCE_SET);
        return properties;
    }

    /**
     * A minimal model in the style of eorm's {@code EntityMappings}: features
     * whose XML names differ from their EMF names, both an attribute
     * ({@code columnDefinition} -> {@code column-definition}) and a qualified
     * containment element ({@code columns} -> {@code emd:column}).
     */
    private static EPackage createExtendedMetaDataPackage() {
        EcoreFactory factory = EcoreFactory.eINSTANCE;
        ExtendedMetaData emd = ExtendedMetaData.INSTANCE;

        EPackage ePackage = factory.createEPackage();
        ePackage.setName("emd");
        ePackage.setNsPrefix("emd_1.0.0");
        ePackage.setNsURI(NS_URI);

        EClass column = factory.createEClass();
        column.setName("Column");
        EAttribute name = factory.createEAttribute();
        name.setName("name");
        name.setEType(EcorePackage.Literals.ESTRING);
        column.getEStructuralFeatures().add(name);
        EAttribute columnDefinition = factory.createEAttribute();
        columnDefinition.setName("columnDefinition");
        columnDefinition.setEType(EcorePackage.Literals.ESTRING);
        column.getEStructuralFeatures().add(columnDefinition);
        emd.setName(columnDefinition, "column-definition");
        emd.setFeatureKind(columnDefinition, ExtendedMetaData.ATTRIBUTE_FEATURE);

        EClass mappings = factory.createEClass();
        mappings.setName("EntityMappings");
        EReference columns = factory.createEReference();
        columns.setName("columns");
        columns.setEType(column);
        columns.setContainment(true);
        columns.setUpperBound(-1);
        mappings.getEStructuralFeatures().add(columns);
        emd.setName(columns, "column");
        emd.setFeatureKind(columns, ExtendedMetaData.ELEMENT_FEATURE);
        emd.setNamespace(columns, NS_URI);

        ePackage.getEClassifiers().add(column);
        ePackage.getEClassifiers().add(mappings);
        return ePackage;
    }
}
