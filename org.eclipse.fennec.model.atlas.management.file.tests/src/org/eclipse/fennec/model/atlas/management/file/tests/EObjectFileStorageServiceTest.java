/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.RegistryConfiguration;
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
import org.osgi.util.promise.Promise;

/**
 * Integration tests for EObjectFileStorageService
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class EObjectFileStorageServiceTest {

    private static final String TEST_SCOPE = "test_scope";
    private static final String TEST_REGISTRY = "test_registry";
    private static final String TEST_STAGE = "test_stage";

    @InjectBundleContext
    BundleContext context;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        assertNotNull(context, "BundleContext should not be null");
        assertNotNull(tempDir, "TempDir should not be null");

        // Set system property for template argument resolution
        System.setProperty(CommonTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testServiceActivation(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Verify backend type
        assertEquals(StorageBackendType.FILE, storageService.getBackendType());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testStoreAndRetrieveEPackage(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Create test EPackage
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("TestPackage");
        testPackage.setNsPrefix("test");
        testPackage.setNsURI("http://test/1.0");

        // Create test metadata
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.getProperties().put("file.extension", ".ecore");
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel("testChannel");
        // Specify .ecore extension for EPackage
        metadata.getProperties().put("file.extension", ".ecore");

        // Store the package
        Promise<ObjectMetadata> storePromise = storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "test-id-123", testPackage, metadata);
        storePromise.getValue();
        String storageId = metadata.getObjectId();

        assertNotNull(storageId);
        assertEquals("test-id-123", storageId);

        // Verify files were created
        File ecoreFile = new File(tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE).resolve(TEST_REGISTRY).resolve(TEST_STAGE).toFile(),
                storageId + ".ecore");
        File metadataFile = new File(tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE).resolve(TEST_REGISTRY).resolve(TEST_STAGE).toFile(),
                storageId + ".metadata.xmi");
        assertTrue(ecoreFile.exists(), "Ecore file should exist");
        assertTrue(metadataFile.exists(), "Metadata file should exist");

        // Retrieve the package
        Promise<EObject> retrievePromise = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        EPackage retrievedPackage = (EPackage) retrievePromise.getValue();

        assertNotEquals(testPackage, retrievedPackage);
        assertNotNull(retrievedPackage);
        assertEquals("TestPackage", retrievedPackage.getName());
        assertEquals("test", retrievedPackage.getNsPrefix());
        assertEquals("http://test/1.0", retrievedPackage.getNsURI());

        // Retrieve metadata
        Promise<ObjectMetadata> metadataPromise = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        ObjectMetadata retrievedMetadata = metadataPromise.getValue();

        assertNotNull(retrievedMetadata);
        assertEquals("testUser", retrievedMetadata.getUploadUser());
        assertEquals("testChannel", retrievedMetadata.getSourceChannel());
        assertNotNull(retrievedMetadata.getContentHash());

        // F4 producer: storing an EPackage computes and PERSISTS the model fingerprint
        assertNotNull(retrievedMetadata.getFingerprint(), "stored EPackage metadata must carry the model fingerprint");
        assertTrue(retrievedMetadata.getFingerprint().startsWith("fp1:"),
                "fingerprint should use the current scheme tag, was: " + retrievedMetadata.getFingerprint());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testFingerprintComputedServerSide_neverAdopted(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // 1) A client-supplied fingerprint on an EPackage upload is OVERWRITTEN by the
        // server-side computation ("computed, never trusted").
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("FingerprintPackage");
        testPackage.setNsPrefix("fp");
        testPackage.setNsURI("http://test/fingerprint/1.0");

        ObjectMetadata packageMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        packageMetadata.setUploadUser("testUser");
        packageMetadata.setUploadTime(Instant.now());
        packageMetadata.setSourceChannel("testChannel");
        packageMetadata.getProperties().put("file.extension", ".ecore");
        packageMetadata.setFingerprint("fp1:attacker-controlled-value");

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "fp-pkg-1", testPackage, packageMetadata)
                .getValue();
        ObjectMetadata storedPackageMetadata = storageService
                .retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "fp-pkg-1").getValue();
        assertNotNull(storedPackageMetadata.getFingerprint());
        assertTrue(storedPackageMetadata.getFingerprint().startsWith("fp1:"));
        assertNotEquals("fp1:attacker-controlled-value", storedPackageMetadata.getFingerprint(),
                "a client-supplied fingerprint must never be adopted");

        // 2) For a non-EPackage object a client-supplied fingerprint is CLEARED.
        EObject nonPackage = EcoreFactory.eINSTANCE.createEClass();
        ObjectMetadata objectMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        objectMetadata.setUploadUser("testUser");
        objectMetadata.setUploadTime(Instant.now());
        objectMetadata.setSourceChannel("testChannel");
        objectMetadata.setFingerprint("fp1:attacker-controlled-value");

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "fp-obj-1", nonPackage, objectMetadata)
                .getValue();
        ObjectMetadata storedObjectMetadata = storageService
                .retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "fp-obj-1").getValue();
        assertNull(storedObjectMetadata.getFingerprint(),
                "non-EPackage objects have no model fingerprint, client-supplied values are cleared");

        // 3) Identical content re-stored under another id yields the SAME fingerprint
        // (reproducible), diverging content a different one.
        EPackage samePackage = EcoreFactory.eINSTANCE.createEPackage();
        samePackage.setName("FingerprintPackage");
        samePackage.setNsPrefix("fp");
        samePackage.setNsURI("http://test/fingerprint/1.0");
        ObjectMetadata sameMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        sameMetadata.setUploadUser("testUser");
        sameMetadata.setUploadTime(Instant.now());
        sameMetadata.setSourceChannel("testChannel");
        sameMetadata.getProperties().put("file.extension", ".ecore");
        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "fp-pkg-2", samePackage, sameMetadata)
                .getValue();
        assertEquals(storedPackageMetadata.getFingerprint(), sameMetadata.getFingerprint(),
                "identical content must yield the identical fingerprint");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testListObjectIds(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Store multiple packages
        for (int i = 0; i < 3; i++) {
            EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
            pkg.setName("Package" + i);
            pkg.setNsPrefix("pkg" + i);
            pkg.setNsURI("http://test/" + i);

            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setUploadUser("testUser");
            metadata.setUploadTime(Instant.now());
            metadata.getProperties().put("file.extension", ".ecore");

            Promise<ObjectMetadata> storePromise = storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                    "test-pkg-" + i, pkg, metadata);
            storePromise.getValue();
        }

        // List all object IDs
        Promise<List<String>> listPromise = storageService.listObjectIds(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE);
        List<String> objectIds = listPromise.getValue();

        assertNotNull(objectIds);
        assertEquals(3, objectIds.size());
        assertTrue(objectIds.contains("test-pkg-0"));
        assertTrue(objectIds.contains("test-pkg-1"));
        assertTrue(objectIds.contains("test-pkg-2"));

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testDeleteObject(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Store a package
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("DeleteTest");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.getProperties().put("file.extension", ".ecore");

        Promise<ObjectMetadata> storePromise = storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "delete-test-id", testPackage, metadata);
        storePromise.getValue();
        String storageId = metadata.getObjectId();

        // Verify files exist
        File ecoreFile = new File(tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE).resolve(TEST_REGISTRY).resolve(TEST_STAGE).toFile(),
                storageId + ".ecore");
        File metadataFile = new File(tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE).resolve(TEST_REGISTRY).resolve(TEST_STAGE).toFile(),
                storageId + ".metadata.xmi");
        assertTrue(ecoreFile.exists());
        assertTrue(metadataFile.exists());

        // Delete the object
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        Boolean deleted = deletePromise.getValue();

        assertTrue(deleted);
        assertFalse(ecoreFile.exists(), "Ecore file should be deleted");
        assertFalse(metadataFile.exists(), "Metadata file should be deleted");

        // Try to retrieve deleted object
        Promise<EObject> retrievePromise = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        EObject retrievedPackage = retrievePromise.getValue();
        assertNull(retrievedPackage, "Should not find deleted package");

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testAutoGeneratedId(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Store package without providing ID
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("AutoIdTest");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.getProperties().put("file.extension", ".ecore");

        Promise<ObjectMetadata> storePromise = storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, null,
                testPackage, metadata);
        storePromise.getValue();
        String storageId = metadata.getObjectId();

        assertNotNull(storageId);
        assertFalse(storageId.isEmpty());

        // Should be able to retrieve with auto-generated ID
        Promise<EObject> retrievePromise = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        EPackage retrievedPackage = (EPackage) retrievePromise.getValue();

        assertNotNull(retrievedPackage);
        assertEquals("AutoIdTest", retrievedPackage.getName());

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testCustomFileExtension(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Test with custom extension
        EPackage testPackage1 = EcoreFactory.eINSTANCE.createEPackage();
        testPackage1.setName("CustomExtTest1");

        ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata1.setUploadUser("testUser");
        metadata1.getProperties().put("file.extension", "ecore"); // without dot

        Promise<ObjectMetadata> storePromise1 = storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "custom-ext-1", testPackage1, metadata1);
        storePromise1.getValue();
        String storageId1 = metadata1.getObjectId();

        // Verify file with .ecore extension exists
        File ecoreFile = new File(tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE).resolve(TEST_REGISTRY).resolve(TEST_STAGE).toFile(),
                storageId1 + ".ecore");
        assertTrue(ecoreFile.exists(), "File with .ecore extension should exist");

        // Test with default extension (no property set)
        EPackage testPackage2 = EcoreFactory.eINSTANCE.createEPackage();
        testPackage2.setName("DefaultExtTest");

        ObjectMetadata metadata2 = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata2.setUploadUser("testUser");
        // No file.extension property set - should use default .xmi

        Promise<ObjectMetadata> storePromise2 = storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "default-ext", testPackage2, metadata2);
        storePromise2.getValue();
        String storageId2 = metadata2.getObjectId();

        // Verify file with .xmi extension exists
        File xmiFile = new File(tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE).resolve(TEST_REGISTRY).resolve(TEST_STAGE).toFile(),
                storageId2 + ".xmi");
        assertTrue(xmiFile.exists(), "File with .xmi extension should exist");

        // Verify both can be retrieved
        Promise<EObject> retrievePromise1 = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId1);
        EObject retrieved1 = retrievePromise1.getValue();
        assertNotNull(retrieved1);
        assertTrue(retrieved1 instanceof EPackage);
        assertEquals("CustomExtTest1", ((EPackage) retrieved1).getName());

        Promise<EObject> retrievePromise2 = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId2);
        EObject retrieved2 = retrievePromise2.getValue();
        assertNotNull(retrieved2);
        assertTrue(retrieved2 instanceof EPackage);
        assertEquals("DefaultExtTest", ((EPackage) retrieved2).getName());

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testContentType(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Test with content type for Ecore
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("ContentTypeTest");
        testPackage.setNsPrefix("cttest");
        testPackage.setNsURI("http://test/contenttype/1.0");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        // Set both file extension and content type
        metadata.getProperties().put("file.extension", ".ecore");
        metadata.getProperties().put("content.type", "org.eclipse.emf.ecore");

        Promise<ObjectMetadata> storePromise = storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "content-type-test", testPackage, metadata);
        storePromise.getValue();
        String storageId = metadata.getObjectId();

        // Verify file exists
        File ecoreFile = new File(tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE).resolve(TEST_REGISTRY).resolve(TEST_STAGE).toFile(),
                storageId + ".ecore");
        assertTrue(ecoreFile.exists(), "File with .ecore extension should exist");

        // Retrieve and verify
        Promise<EObject> retrievePromise = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        EObject retrieved = retrievePromise.getValue();
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof EPackage);
        assertEquals("ContentTypeTest", ((EPackage) retrieved).getName());

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testUpdateMetadata(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Store initial object
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("UpdateMetadataTest");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("originalUser");
        metadata.setSourceChannel("originalChannel");
        metadata.setContentHash("originalHash");
        metadata.setObjectType("EPackage");
        metadata.setUploadTime(Instant.now());
        metadata.getProperties().put("file.extension", ".ecore");

        Promise<ObjectMetadata> storePromise = storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "update-metadata-test", testPackage, metadata);
        storePromise.getValue();
        String storageId = metadata.getObjectId();

        // Create updated metadata
        ObjectMetadata updatedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        updatedMetadata.setUploadUser("updatedUser");
        updatedMetadata.setSourceChannel("updatedChannel");
        updatedMetadata.setContentHash("updatedHash");
        updatedMetadata.setObjectType("EPackage");
        updatedMetadata.setUploadTime(Instant.now());
        updatedMetadata.setReviewUser("reviewUser");
        updatedMetadata.setReviewTime(Instant.now());
        updatedMetadata.setReviewReason("Updated for testing");
        updatedMetadata.getProperties().put("file.extension", ".ecore");
        updatedMetadata.getProperties().put("custom.property", "customValue");

        // Update metadata
        Promise<Boolean> updatePromise = storageService.updateMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId,
                updatedMetadata);
        Boolean updateResult = updatePromise.getValue();
        assertTrue(updateResult, "Metadata update should succeed");

        // Retrieve and verify updated metadata
        Promise<ObjectMetadata> retrievePromise = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        ObjectMetadata retrievedMetadata = retrievePromise.getValue();

        assertNotNull(retrievedMetadata);
        assertEquals("originalUser", retrievedMetadata.getUploadUser(),
                "Upload user should be immutable and preserved");
        assertEquals("updatedChannel", retrievedMetadata.getSourceChannel());
        assertEquals("updatedHash", retrievedMetadata.getContentHash());
        assertEquals("reviewUser", retrievedMetadata.getReviewUser());
        assertEquals("Updated for testing", retrievedMetadata.getReviewReason());
        assertEquals("customValue", retrievedMetadata.getProperties().get("custom.property"));

        // Verify object itself is unchanged
        Promise<EObject> objectPromise = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        EPackage retrievedPackage = (EPackage) objectPromise.getValue();
        assertNotNull(retrievedPackage);
        assertEquals("UpdateMetadataTest", retrievedPackage.getName());

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testUpdateStatus(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Store initial object with DRAFT status
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("UpdateStatusTest");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setSourceChannel("testChannel");
        metadata.setContentHash("testHash");
        metadata.setObjectType("EPackage");
        metadata.setUploadTime(Instant.now());
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.getProperties().put("file.extension", ".ecore");

        Promise<ObjectMetadata> storePromise = storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "update-status-test", testPackage, metadata);
        storePromise.getValue();
        String storageId = metadata.getObjectId();

        // Verify initial status
        Promise<ObjectMetadata> initialPromise = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        ObjectMetadata initialMetadata = initialPromise.getValue();
        assertEquals(ObjectStatus.DRAFT, initialMetadata.getStatus());
        assertNull(initialMetadata.getLastChangeUser());
        assertNull(initialMetadata.getLastChangeTime());

        // Update status to APPROVED with change user
        Instant beforeUpdate = Instant.now();
        Promise<Boolean> updatePromise = storageService.updateStatus(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId,
                ObjectStatus.APPROVED, "approverUser");
        Boolean updateResult = updatePromise.getValue();
        assertTrue(updateResult, "Status update should succeed");
        Instant afterUpdate = Instant.now();

        // Verify status update
        Promise<ObjectMetadata> updatedPromise = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        ObjectMetadata updatedMetadata = updatedPromise.getValue();

        assertNotNull(updatedMetadata);
        assertEquals(ObjectStatus.APPROVED, updatedMetadata.getStatus());
        assertEquals("approverUser", updatedMetadata.getLastChangeUser());
        assertNotNull(updatedMetadata.getLastChangeTime());
        assertTrue(updatedMetadata.getLastChangeTime().isAfter(beforeUpdate)
                || updatedMetadata.getLastChangeTime().equals(beforeUpdate));
        assertTrue(updatedMetadata.getLastChangeTime().isBefore(afterUpdate)
                || updatedMetadata.getLastChangeTime().equals(afterUpdate));

        // Update status again without change user
        Promise<Boolean> updatePromise2 = storageService.updateStatus(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId,
                ObjectStatus.DEPLOYED, null);
        Boolean updateResult2 = updatePromise2.getValue();
        assertTrue(updateResult2, "Second status update should succeed");

        // Verify second status update
        Promise<ObjectMetadata> finalPromise = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        ObjectMetadata finalMetadata = finalPromise.getValue();

        assertEquals(ObjectStatus.DEPLOYED, finalMetadata.getStatus());
        // Change user should remain as previous value when null is passed
        assertEquals("approverUser", finalMetadata.getLastChangeUser());
        assertNotNull(finalMetadata.getLastChangeTime());

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testExists(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Test non-existent object
        Boolean existsBefore = storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "non-existent-id");
        assertFalse(existsBefore, "Non-existent object should not exist");

        // Store an object
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("ExistsTest");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setSourceChannel("testChannel");
        metadata.setContentHash("testHash");
        metadata.setObjectType("EPackage");
        metadata.setUploadTime(Instant.now());
        metadata.getProperties().put("file.extension", ".ecore");

        Promise<ObjectMetadata> storePromise = storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "exists-test-id", testPackage, metadata);
        storePromise.getValue();
        String storageId = metadata.getObjectId();

        // Test existing object
        Boolean existsAfter = storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        assertTrue(existsAfter, "Stored object should exist");

        // Delete the object
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        deletePromise.getValue();

        // Test after deletion
        Boolean existsAfterDelete = storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        assertFalse(existsAfterDelete, "Deleted object should not exist");

        // Test with null and empty IDs
        Boolean existsNull = storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, null);
        assertFalse(existsNull, "Null ID should return false");

        Boolean existsEmpty = storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "");
        assertFalse(existsEmpty, "Empty ID should return false");

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testGetObjectCount(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Initial count should be 0
        long initialCount = storageService.getObjectCount();
        assertEquals(0, initialCount, "Initial object count should be 0");

        // Store multiple objects
        for (int i = 0; i < 5; i++) {
            EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
            pkg.setName("CountTest" + i);

            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setUploadUser("testUser");
            metadata.setSourceChannel("testChannel");
            metadata.setContentHash("testHash" + i);
            metadata.setObjectType("EPackage");
            metadata.setUploadTime(Instant.now());
            metadata.getProperties().put("file.extension", ".ecore");

            Promise<ObjectMetadata> storePromise = storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                    "count-test-" + i, pkg, metadata);
            storePromise.getValue();
        }

        // Count after storing 5 objects
        long countAfterStore = storageService.getObjectCount();
        assertEquals(5, countAfterStore, "Object count should be 5 after storing 5 objects");

        // Verify metadata files exist before deletion
        File metadataFile1 = new File(tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE).resolve(TEST_REGISTRY).resolve(TEST_STAGE).toFile(),
                "count-test-1.metadata.xmi");
        File metadataFile3 = new File(tempDir.resolve(CommonTestAnnotations.FILE_STORAGE_FOLDER).resolve(TEST_SCOPE).resolve(TEST_REGISTRY).resolve(TEST_STAGE).toFile(),
                "count-test-3.metadata.xmi");
        assertTrue(metadataFile1.exists(), "Metadata file 1 should exist before deletion");
        assertTrue(metadataFile3.exists(), "Metadata file 3 should exist before deletion");

        // Delete 2 objects
        Promise<Boolean> deletePromise1 = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "count-test-1");
        deletePromise1.getValue();
        Promise<Boolean> deletePromise2 = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "count-test-3");
        deletePromise2.getValue();

        // Verify metadata files are deleted
        assertFalse(metadataFile1.exists(), "Metadata file 1 should be deleted");
        assertFalse(metadataFile3.exists(), "Metadata file 3 should be deleted");

        // Verify metadata retrieval fails for deleted objects
        Promise<ObjectMetadata> metadataPromise1 = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY,
                TEST_STAGE, "count-test-1");
        assertNull(metadataPromise1.getValue(), "Should not retrieve metadata for deleted object 1");
        Promise<ObjectMetadata> metadataPromise3 = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY,
                TEST_STAGE, "count-test-3");
        assertNull(metadataPromise3.getValue(), "Should not retrieve metadata for deleted object 3");

        // Count after deleting 2 objects
        long countAfterDelete = storageService.getObjectCount();
        assertEquals(3, countAfterDelete, "Object count should be 3 after deleting 2 objects");

        // Verify count matches listObjectIds size
        Promise<List<String>> listPromise = storageService.listObjectIds(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE);
        List<String> objectIds = listPromise.getValue();
        assertEquals(countAfterDelete, objectIds.size(), "Object count should match listObjectIds size");

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testUpdateMetadataErrorHandling(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Test updating metadata for non-existent object
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setSourceChannel("testChannel");
        metadata.setContentHash("testHash");
        metadata.setObjectType("EPackage");
        metadata.setUploadTime(Instant.now());

        Promise<Boolean> updatePromise = storageService.updateMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "non-existent-id", metadata);
        Boolean updateResult = updatePromise.getValue();
        assertFalse(updateResult, "Updating metadata for non-existent object should return false");

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @StorageSetup
    public void testUpdateStatusErrorHandling(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {
        assertNotNull(serviceAware);

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // Test updating status for non-existent object
        Promise<Boolean> updatePromise = storageService.updateStatus(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "non-existent-id", ObjectStatus.APPROVED, "testUser");
        Boolean updateResult = updatePromise.getValue();
        assertFalse(updateResult, "Updating status for non-existent object should return false");

        // Clean up configuration
    }

    /**
     * Integration test for storage-registry interaction.
     * 
     * Tests that objects stored via storage service can be found using registry
     * service findByStatus. This verifies the registry is properly updated when
     * objects are stored.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @RegistryConfiguration
    @StorageSetup
    public void testStorageRegistryIntegration(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> serviceAware,
            @InjectService(cardinality = 0, filter = "(registry.type=shared)") ServiceAware<EObjectRegistryService> registryAware)
            throws Exception {

        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");
        // Registry service should be available (configured by annotation)
        EObjectRegistryService<EObject> registryService = (EObjectRegistryService<EObject>) registryAware
                .waitForService(5000L);
        assertNotNull(registryService, "Registry service should be available");
        // Create test EPackage
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("TestRegistryPackage");
        testPackage.setNsURI("https://test.registry/1.0");
        testPackage.setNsPrefix("testreg");

        // Create metadata with DRAFT status
        ObjectMetadata draftMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        draftMetadata.setObjectName("Draft Test Object");
        draftMetadata.setObjectType("EPackage");
        draftMetadata.setVersion("1.0.0");
        draftMetadata.setStatus(ObjectStatus.DRAFT);
        draftMetadata.setUploadUser("TestUser");
        draftMetadata.setUploadTime(Instant.now());
        draftMetadata.setSourceChannel("TEST");

        // Store object with DRAFT status
        storageService
                .storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "registry-test-draft", testPackage, draftMetadata)
                .getValue();
        String draftObjectId = draftMetadata.getObjectId();
        assertNotNull(draftObjectId, "Draft object ID should not be null");

        // Wait a moment for registry update (if async)
        Thread.sleep(100);

        // Verify we can find DRAFT objects using registry
        List<ObjectMetadata> draftObjects = registryService.findByStatus(ObjectStatus.DRAFT);
        assertNotNull(draftObjects, "Draft objects list should not be null");
        assertTrue(draftObjects.stream().anyMatch(obj -> draftObjectId.equals(obj.getObjectId())),
                "Registry should find the stored draft object by status");

        // Update object to APPROVED status
        draftMetadata.setStatus(ObjectStatus.APPROVED);
        draftMetadata.setLastChangeTime(Instant.now());
        storageService.updateMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, draftObjectId, draftMetadata).getValue();

        // Wait a moment for registry update (if async)
        Thread.sleep(100);

        // Verify object no longer appears in DRAFT list
        List<ObjectMetadata> updatedDraftObjects = registryService.findByStatus(ObjectStatus.DRAFT);
        assertFalse(updatedDraftObjects.stream().anyMatch(obj -> draftObjectId.equals(obj.getObjectId())),
                "Object should no longer appear in draft list after status update");

        // Verify object appears in APPROVED list
        List<ObjectMetadata> approvedObjects = registryService.findByStatus(ObjectStatus.APPROVED);
        assertNotNull(approvedObjects, "Approved objects list should not be null");
        assertTrue(approvedObjects.stream().anyMatch(obj -> draftObjectId.equals(obj.getObjectId())),
                "Registry should find the object in approved list after status update");

        // Test with REJECTED status
        EPackage rejectedPackage = EcoreFactory.eINSTANCE.createEPackage();
        rejectedPackage.setName("RejectedTestPackage");
        rejectedPackage.setNsURI("https://test.rejected/1.0");
        rejectedPackage.setNsPrefix("testrej");

        ObjectMetadata rejectedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        rejectedMetadata.setObjectName("Rejected Test Object");
        rejectedMetadata.setObjectType("EPackage");
        rejectedMetadata.setVersion("1.0.0");
        rejectedMetadata.setStatus(ObjectStatus.REJECTED);
        rejectedMetadata.setUploadUser("TestUser");
        rejectedMetadata.setUploadTime(Instant.now());
        rejectedMetadata.setSourceChannel("TEST");

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "registry-test-rejected", rejectedPackage,
                rejectedMetadata).getValue();
        String rejectedObjectId = rejectedMetadata.getObjectId();
        assertNotNull(rejectedObjectId, "Rejected object ID should not be null");

        // Wait a moment for registry update (if async)
        Thread.sleep(100);

        // Verify we can find REJECTED objects using registry
        List<ObjectMetadata> rejectedObjects = registryService.findByStatus(ObjectStatus.REJECTED);
        assertNotNull(rejectedObjects, "Rejected objects list should not be null");
        assertTrue(rejectedObjects.stream().anyMatch(obj -> rejectedObjectId.equals(obj.getObjectId())),
                "Registry should find the stored rejected object by status");

        // Clean up
        storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, draftObjectId);
        storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, rejectedObjectId);
    }
}