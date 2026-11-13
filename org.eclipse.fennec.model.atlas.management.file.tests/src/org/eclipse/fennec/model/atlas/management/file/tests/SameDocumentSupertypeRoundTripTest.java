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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
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
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Regression test for issue #382: an EClass with one supertype in another
 * document and one in its own package must keep both after a store/retrieve
 * round trip.
 *
 * <p>
 * The stores in #382 serialize the mixed supertype list in element form, the
 * local entry as the document-relative {@code <eSuperTypes href="#//Provider"/>}.
 * On load that
 * becomes a proxy bound to the storage URI. The read path without a leased
 * per-stage ResourceSet - the one this test runtime uses, since no
 * {@code StageResourceSetProvider} is deployed - detached the resource without
 * resolving it, so the proxy never resolved and the features inherited from the
 * local supertype were gone.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class SameDocumentSupertypeRoundTripTest {

    private static final String TEST_SCOPE = "issue382_scope";
    private static final String TEST_REGISTRY = "schemas";
    private static final String TEST_STAGE = "release";
    private static final String OBJECT_ID = "configuration";
    private static final String NS_URI = "https://eclipse.org/fennec/model/atlas/tests/issue382/1.0.0";

    /** The package of {@link #createPackage()} as the #382 stores serialize it. */
    private static final String ELEMENT_FORM_PACKAGE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore" name="issue382" nsURI="%s" nsPrefix="issue382">
              <eClassifiers xsi:type="ecore:EClass" name="Provider" abstract="true">
                <eStructuralFeatures xsi:type="ecore:EAttribute" name="publication" eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
              </eClassifiers>
              <eClassifiers xsi:type="ecore:EClass" name="Service">
                <eSuperTypes href="http://www.eclipse.org/emf/2002/Ecore#//EModelElement"/>
                <eSuperTypes href="#//Provider"/>
              </eClassifiers>
            </ecore:EPackage>
            """.formatted(NS_URI);

    @InjectBundleContext
    BundleContext context;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        assertNotNull(context, "BundleContext should not be null");
        System.setProperty(CommonTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testLocalSupertypeSurvivesRoundTripNextToForeignSupertype(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel("TEST");
        metadata.getProperties().put("file.extension", ".ecore");

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, OBJECT_ID, createPackage(), metadata)
                .getValue();

        // This runtime stores the package through an Ecore resource, whose save options
        // produce the classic attribute form (eSuperTypes="... #//Provider") that resolves
        // during load. A package written through a plain XMI resource - as in the #382
        // stores - gets the element form instead, which loads as lazy proxies. Put exactly
        // that shape into the store.
        Path storedFile = tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE)
                .resolve(TEST_REGISTRY).resolve(TEST_STAGE).resolve(OBJECT_ID + ".ecore");
        assertTrue(Files.exists(storedFile), "Package should be stored as " + storedFile);
        Files.writeString(storedFile, ELEMENT_FORM_PACKAGE, StandardCharsets.UTF_8);

        EPackage retrieved = (EPackage) storageService
                .retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, OBJECT_ID).getValue();
        assertNotNull(retrieved);
        assertNotNull(retrieved.eResource());
        assertNull(retrieved.eResource().getResourceSet(),
                "the retrieved package is expected to come back detached from the management ResourceSet");

        EClass service = (EClass) retrieved.getEClassifier("Service");
        assertNotNull(service);
        assertEquals(2, service.getESuperTypes().size());
        service.getESuperTypes().forEach(superType -> assertFalse(superType.eIsProxy(),
                "supertype must be resolved, was proxy " + superType));

        EClass provider = (EClass) retrieved.getEClassifier("Provider");
        assertTrue(service.getESuperTypes().contains(provider),
                "the local supertype must be the Provider class of the same package");
        assertNotNull(service.getEStructuralFeature("publication"),
                "the feature inherited from the local supertype must be present");
    }

    /**
     * {@code Service} extends {@code EModelElement} (another document) and
     * {@code Provider} (same package).
     */
    private static EPackage createPackage() {
        EcoreFactory factory = EcoreFactory.eINSTANCE;
        EPackage ePackage = factory.createEPackage();
        ePackage.setName("issue382");
        ePackage.setNsPrefix("issue382");
        ePackage.setNsURI(NS_URI);

        EClass provider = factory.createEClass();
        provider.setName("Provider");
        provider.setAbstract(true);
        EAttribute publication = factory.createEAttribute();
        publication.setName("publication");
        publication.setEType(EcorePackage.Literals.ESTRING);
        provider.getEStructuralFeatures().add(publication);

        EClass service = factory.createEClass();
        service.setName("Service");
        service.getESuperTypes().add(EcorePackage.Literals.EMODEL_ELEMENT);
        service.getESuperTypes().add(provider);

        ePackage.getEClassifiers().add(provider);
        ePackage.getEClassifiers().add(service);
        return ePackage;
    }
}
