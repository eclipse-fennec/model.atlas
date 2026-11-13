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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
 * An EPackage is always persisted in the classic Ecore form, whatever factory
 * the storage URI or the content type would select (follow-up to issue #382).
 *
 * <p>
 * A package written through a plain XMI resource gets the element form for a
 * supertype list that mixes another document with its own package
 * ({@code <eSuperTypes href="#//Provider"/>}), which loads back as lazy proxies
 * bound to the storage location. The Ecore resource writes the encoded attribute
 * form ({@code eSuperTypes="... #//Provider"}), which resolves during load.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class EPackageEcoreFormStorageTest {

    private static final String TEST_SCOPE = "ecore_form_scope";
    private static final String TEST_REGISTRY = "schemas";
    private static final String TEST_STAGE = "release";
    private static final String OBJECT_ID = "mixed";
    private static final String NS_URI = "https://eclipse.org/fennec/model/atlas/tests/ecoreform/1.0.0";

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
    public void testEPackageIsWrittenInEcoreFormUnderAnXmiExtension(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // .xmi selects the plain XMI factory - the one that writes the element form.
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel("TEST");
        metadata.getProperties().put("file.extension", ".xmi");
        metadata.getProperties().put("content.type", "application/xmi");

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, OBJECT_ID, createPackage(), metadata)
                .getValue();

        Path storedFile = tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE)
                .resolve(TEST_REGISTRY).resolve(TEST_STAGE).resolve(OBJECT_ID + ".xmi");
        assertTrue(Files.exists(storedFile), "Package should be stored as " + storedFile);
        String stored = Files.readString(storedFile, StandardCharsets.UTF_8);

        assertTrue(stored.contains("eSuperTypes=\"http://www.eclipse.org/emf/2002/Ecore#//EModelElement #//Provider\""),
                "the supertypes must be written in the Ecore attribute form:\n" + stored);
        assertFalse(stored.contains("<eSuperTypes"), "no element-form supertype may be written:\n" + stored);
    }

    /**
     * {@code Service} extends {@code EModelElement} (another document) and
     * {@code Provider} (same package).
     */
    private static EPackage createPackage() {
        EcoreFactory factory = EcoreFactory.eINSTANCE;
        EPackage ePackage = factory.createEPackage();
        ePackage.setName("ecoreform");
        ePackage.setNsPrefix("ecoreform");
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
