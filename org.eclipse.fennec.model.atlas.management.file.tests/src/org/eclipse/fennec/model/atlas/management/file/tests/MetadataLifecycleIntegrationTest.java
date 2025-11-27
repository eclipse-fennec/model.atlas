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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.file.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.management.file.tests.annotations.FileTestAnnotations.DefaultFileStorageSetup;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;

/**
 * Comprehensive integration tests for metadata lifecycle integrity throughout storage operations.
 * 
 * <p>This test class validates that ObjectMetadata fields are correctly maintained
 * throughout the complete lifecycle of storage operations:</p>
 * 
 * <ul>
 * <li><strong>Store Operations</strong> - ObjectId generation/setting, timestamps, role assignment</li>
 * <li><strong>Retrieve Operations</strong> - Metadata field preservation and integrity</li>
 * <li><strong>Update Operations</strong> - Proper timestamp updates, field modifications</li>
 * <li><strong>Delete Operations</strong> - Metadata cleanup and unavailability</li>
 * </ul>
 * 
 * <h3>Key Test Scenarios</h3>
 * <ul>
 * <li>Store with provided objectId vs auto-generated UUID</li>
 * <li>Metadata field preservation across retrieve operations</li>
 * <li>Timestamp accuracy and automatic updates</li>
 * <li>Role-based metadata handling</li>
 * <li>Concurrent operations and metadata consistency</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class MetadataLifecycleIntegrationTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Set system property for template argument resolution
        System.setProperty("tempDir", tempDir.toString());
    }

    /**
     * Tests the complete metadata lifecycle with provided objectId.
     * 
     * <p>Verifies that when an objectId is explicitly provided:</p>
     * <ul>
     * <li>The provided objectId is used and set in caller's metadata</li>
     * <li>All metadata fields are preserved during store/retrieve cycles</li>
     * <li>Timestamps are correctly maintained</li>
     * <li>Role is automatically assigned based on storage configuration</li>
     * </ul>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
    @DefaultFileStorageSetup
    public void testMetadataLifecycleWithProvidedObjectId(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)")
            ServiceAware<EObjectStorageService> serviceAware) throws Exception {
        
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // === PHASE 1: Create test object and metadata ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("ProvidedIdTestPackage");
        testPackage.setNsPrefix("provided");
        testPackage.setNsURI("http://provided.test/1.0");

        ObjectMetadata originalMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        originalMetadata.setObjectName("ProvidedIdTestPackage");
        originalMetadata.setVersion("1.0.0");
        originalMetadata.setUploadUser("test-user");
        originalMetadata.setSourceChannel("INTEGRATION_TEST");
        originalMetadata.setStatus(ObjectStatus.DRAFT);
        originalMetadata.setContentHash("provided-hash-123");
        
        Instant beforeStore = Instant.now();
        originalMetadata.setUploadTime(beforeStore);
        originalMetadata.getProperties().put("test.property", "test-value");
        originalMetadata.getProperties().put("file.extension", ".ecore");
        
        // Verify objectId is null initially
        assertNull(originalMetadata.getObjectId(), "ObjectId should be null initially");
        
        // === PHASE 2: Store with provided objectId ===
        
        String providedObjectId = "provided-id-12345";
        Promise<String> storePromise = storageService.storeObject(providedObjectId, testPackage, originalMetadata);
        String returnedObjectId = storePromise.getValue();
        
        // Verify store operation results
        assertEquals(providedObjectId, returnedObjectId, "Should return the provided objectId");
        assertEquals(providedObjectId, originalMetadata.getObjectId(), "ObjectId should be set in caller's metadata");
        assertNotNull(originalMetadata.getRole(), "Role should be set automatically");
        
        // === PHASE 3: Retrieve and verify complete metadata integrity ===
        
        Promise<ObjectMetadata> retrievePromise = storageService.retrieveMetadata(providedObjectId);
        ObjectMetadata retrievedMetadata = retrievePromise.getValue();
        
        // Verify core identification fields
        assertNotNull(retrievedMetadata, "Retrieved metadata should not be null");
        assertEquals(providedObjectId, retrievedMetadata.getObjectId(), "ObjectId should be preserved");
        assertEquals("ProvidedIdTestPackage", retrievedMetadata.getObjectName(), "Object name should be preserved");
        assertEquals("1.0.0", retrievedMetadata.getVersion(), "Version should be preserved");
        
        // Verify user and source fields
        assertEquals("test-user", retrievedMetadata.getUploadUser(), "Upload user should be preserved");
        assertEquals("INTEGRATION_TEST", retrievedMetadata.getSourceChannel(), "Source channel should be preserved");
        assertEquals("provided-hash-123", retrievedMetadata.getContentHash(), "Content hash should be preserved");
        
        // Verify status and role
        assertEquals(ObjectStatus.DRAFT, retrievedMetadata.getStatus(), "Status should be preserved");
        assertNotNull(retrievedMetadata.getRole(), "Role should be preserved");
        
        // Verify timestamp preservation
        assertNotNull(retrievedMetadata.getUploadTime(), "Upload time should be preserved");
        assertTrue(retrievedMetadata.getUploadTime().equals(beforeStore) || 
                  Math.abs(retrievedMetadata.getUploadTime().toEpochMilli() - beforeStore.toEpochMilli()) < 1000, 
                  "Upload time should be approximately correct");
        
        // Verify properties preservation
        assertEquals("test-value", retrievedMetadata.getProperties().get("test.property"), "Custom properties should be preserved");
        assertEquals(".ecore", retrievedMetadata.getProperties().get("file.extension"), "File extension should be preserved");
        
        // Verify object content is also retrievable
        Promise<EObject> objectPromise = storageService.retrieveObject(providedObjectId);
        EObject retrievedObject = objectPromise.getValue();
        assertNotNull(retrievedObject, "Object content should be retrievable");
        assertTrue(retrievedObject instanceof EPackage, "Retrieved object should be EPackage");
        assertEquals("ProvidedIdTestPackage", ((EPackage) retrievedObject).getName(), "Object content should be preserved");
    }

    /**
     * Tests the complete metadata lifecycle with auto-generated objectId.
     * 
     * <p>Verifies that when objectId is null (auto-generation):</p>
     * <ul>
     * <li>A valid UUID is generated and set in caller's metadata</li>
     * <li>The generated UUID is returned from store operation</li>
     * <li>All metadata fields are preserved correctly</li>
     * <li>Generated objectId is consistently used for subsequent operations</li>
     * </ul>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @DefaultFileStorageSetup
    public void testMetadataLifecycleWithGeneratedObjectId(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)")
            ServiceAware<EObjectStorageService> serviceAware) throws Exception {
        
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // === PHASE 1: Create test object and metadata ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("GeneratedIdTestPackage");
        testPackage.setNsPrefix("generated");
        testPackage.setNsURI("http://generated.test/1.0");

        ObjectMetadata originalMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        originalMetadata.setObjectName("GeneratedIdTestPackage");
        originalMetadata.setVersion("2.0.0");
        originalMetadata.setUploadUser("generator-user");
        originalMetadata.setSourceChannel("AUTO_GENERATOR");
        originalMetadata.setStatus(ObjectStatus.DRAFT);
        originalMetadata.setUploadTime(Instant.now());
        
        // Verify objectId is null for auto-generation
        assertNull(originalMetadata.getObjectId(), "ObjectId should be null for auto-generation");
        
        // === PHASE 2: Store with auto-generated objectId ===
        
        Promise<String> storePromise = storageService.storeObject(null, testPackage, originalMetadata);
        String generatedObjectId = storePromise.getValue();
        
        // Verify auto-generation results
        assertNotNull(generatedObjectId, "Generated objectId should not be null");
        assertEquals(generatedObjectId, originalMetadata.getObjectId(), "Generated objectId should be set in caller's metadata");
        
        // Verify it's a valid UUID
        assertDoesNotThrow(() -> UUID.fromString(generatedObjectId), "Generated objectId should be valid UUID");
        
        // === PHASE 3: Retrieve and verify metadata with generated objectId ===
        
        Promise<ObjectMetadata> retrievePromise = storageService.retrieveMetadata(generatedObjectId);
        ObjectMetadata retrievedMetadata = retrievePromise.getValue();
        
        assertNotNull(retrievedMetadata, "Retrieved metadata should not be null");
        assertEquals(generatedObjectId, retrievedMetadata.getObjectId(), "Generated objectId should be preserved");
        assertEquals("GeneratedIdTestPackage", retrievedMetadata.getObjectName(), "Object name should be preserved");
        assertEquals("2.0.0", retrievedMetadata.getVersion(), "Version should be preserved");
        assertEquals("generator-user", retrievedMetadata.getUploadUser(), "Upload user should be preserved");
        assertEquals("AUTO_GENERATOR", retrievedMetadata.getSourceChannel(), "Source channel should be preserved");
        
        // === PHASE 4: Verify multiple auto-generated IDs are unique ===
        
        ObjectMetadata secondMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        secondMetadata.setObjectName("SecondGeneratedPackage");
        secondMetadata.setUploadUser("second-user");
        secondMetadata.setStatus(ObjectStatus.DRAFT);
        
        Promise<String> secondStorePromise = storageService.storeObject(null, testPackage, secondMetadata);
        String secondGeneratedId = secondStorePromise.getValue();
        
        assertNotEquals(generatedObjectId, secondGeneratedId, "Multiple generated IDs should be unique");
        assertEquals(secondGeneratedId, secondMetadata.getObjectId(), "Second metadata should have its generated ID");
        
        // Verify both objects are independently retrievable
        assertTrue(storageService.exists(generatedObjectId), "First generated object should exist");
        assertTrue(storageService.exists(secondGeneratedId), "Second generated object should exist");
    }

    /**
     * Tests metadata updates and timestamp management.
     * 
     * <p>Verifies that update operations:</p>
     * <ul>
     * <li>Preserve objectId and core immutable fields</li>
     * <li>Update modifiable fields correctly</li>
     * <li>Set lastChangeTime automatically</li>
     * <li>Maintain role assignments</li>
     * </ul>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @DefaultFileStorageSetup
    public void testMetadataUpdateAndTimestamps(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)")
            ServiceAware<EObjectStorageService> serviceAware) throws Exception {
        
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // === PHASE 1: Store initial object ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("UpdateTestPackage");

        ObjectMetadata initialMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        initialMetadata.setObjectName("UpdateTestPackage");
        initialMetadata.setVersion("1.0.0");
        initialMetadata.setUploadUser("initial-user");
        initialMetadata.setStatus(ObjectStatus.DRAFT);
        initialMetadata.setUploadTime(Instant.now());
        
        String objectId = "update-test-id";
        Promise<String> storePromise = storageService.storeObject(objectId, testPackage, initialMetadata);
        storePromise.getValue();
        
        // Verify initial state
        Promise<ObjectMetadata> initialRetrievePromise = storageService.retrieveMetadata(objectId);
        ObjectMetadata initialRetrievedMetadata = initialRetrievePromise.getValue();
        assertNull(initialRetrievedMetadata.getLastChangeTime(), "LastChangeTime should be null initially");
        assertNull(initialRetrievedMetadata.getReviewUser(), "ReviewUser should be null initially");
        
        // === PHASE 2: Update metadata and verify timestamp behavior ===
        
        Instant beforeUpdate = Instant.now();
        
        ObjectMetadata updateMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        updateMetadata.setObjectName("UpdatedTestPackage");
        updateMetadata.setVersion("2.0.0");
        updateMetadata.setStatus(ObjectStatus.APPROVED);
        updateMetadata.setReviewUser("reviewer-user");
        updateMetadata.setReviewReason("Updated for testing");
        updateMetadata.getProperties().put("update.note", "Modified during test");
        
        Promise<Boolean> updatePromise = storageService.updateMetadata(objectId, updateMetadata);
        Boolean updateResult = updatePromise.getValue();
        Instant afterUpdate = Instant.now();
        
        assertTrue(updateResult, "Update should succeed");
        // Note: updateMetadata object should NOT be modified (copy-based approach)
        
        // === PHASE 3: Verify update results and timestamp accuracy ===
        
        Promise<ObjectMetadata> updatedRetrievePromise = storageService.retrieveMetadata(objectId);
        ObjectMetadata updatedMetadata = updatedRetrievePromise.getValue();
        
        // Verify immutable fields are preserved
        assertEquals(objectId, updatedMetadata.getObjectId(), "ObjectId should remain unchanged");
        assertEquals("initial-user", updatedMetadata.getUploadUser(), "Upload user should be preserved");
        assertNotNull(updatedMetadata.getUploadTime(), "Upload time should be preserved");
        
        // Verify updated fields
        assertEquals("UpdatedTestPackage", updatedMetadata.getObjectName(), "Object name should be updated");
        assertEquals("2.0.0", updatedMetadata.getVersion(), "Version should be updated");
        assertEquals(ObjectStatus.APPROVED, updatedMetadata.getStatus(), "Status should be updated");
        assertEquals("reviewer-user", updatedMetadata.getReviewUser(), "Review user should be set");
        assertEquals("Updated for testing", updatedMetadata.getReviewReason(), "Review reason should be set");
        
        // Verify lastChangeTime was set correctly
        assertNotNull(updatedMetadata.getLastChangeTime(), "LastChangeTime should be set after update");
        assertTrue(updatedMetadata.getLastChangeTime().isAfter(beforeUpdate.minusSeconds(1)) && 
                  updatedMetadata.getLastChangeTime().isBefore(afterUpdate.plusSeconds(1)),
                  "LastChangeTime should be within update timeframe");
        
        // Verify properties were updated
        assertEquals("Modified during test", updatedMetadata.getProperties().get("update.note"), 
                    "Updated properties should be preserved");
    }

    /**
     * Tests delete operations and metadata cleanup.
     * 
     * <p>Verifies that delete operations:</p>
     * <ul>
     * <li>Remove objects from storage completely</li>
     * <li>Make metadata unavailable after deletion</li>
     * <li>Clean up both object content and metadata</li>
     * <li>Return appropriate results for existence checks</li>
     * </ul>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @DefaultFileStorageSetup
    public void testDeleteOperationAndMetadataCleanup(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)")
            ServiceAware<EObjectStorageService> serviceAware) throws Exception {
        
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // === PHASE 1: Store test object ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("DeleteTestPackage");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("DeleteTestPackage");
        metadata.setUploadUser("delete-user");
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.setUploadTime(Instant.now());
        
        String objectId = "delete-test-id";
        Promise<String> storePromise = storageService.storeObject(objectId, testPackage, metadata);
        storePromise.getValue();
        
        // === PHASE 2: Verify object exists before deletion ===
        
        assertTrue(storageService.exists(objectId), "Object should exist before deletion");
        
        Promise<ObjectMetadata> beforeDeletePromise = storageService.retrieveMetadata(objectId);
        ObjectMetadata beforeDeleteMetadata = beforeDeletePromise.getValue();
        assertNotNull(beforeDeleteMetadata, "Metadata should be retrievable before deletion");
        
        Promise<EObject> beforeDeleteObjectPromise = storageService.retrieveObject(objectId);
        EObject beforeDeleteObject = beforeDeleteObjectPromise.getValue();
        assertNotNull(beforeDeleteObject, "Object content should be retrievable before deletion");
        
        // === PHASE 3: Delete object and verify cleanup ===
        
        Promise<Boolean> deletePromise = storageService.deleteObject(objectId);
        Boolean deleteResult = deletePromise.getValue();
        
        assertTrue(deleteResult, "Delete operation should succeed");
        
        // === PHASE 4: Verify complete cleanup ===
        
        assertFalse(storageService.exists(objectId), "Object should not exist after deletion");
        
        Promise<ObjectMetadata> afterDeletePromise = storageService.retrieveMetadata(objectId);
        ObjectMetadata afterDeleteMetadata = afterDeletePromise.getValue();
        assertNull(afterDeleteMetadata, "Metadata should not be retrievable after deletion");
        
        Promise<EObject> afterDeleteObjectPromise = storageService.retrieveObject(objectId);
        EObject afterDeleteObject = afterDeleteObjectPromise.getValue();
        assertNull(afterDeleteObject, "Object content should not be retrievable after deletion");
        
        // === PHASE 5: Verify delete on non-existent object ===
        
        Promise<Boolean> nonExistentDeletePromise = storageService.deleteObject("non-existent-id");
        Boolean nonExistentDeleteResult = nonExistentDeletePromise.getValue();
        assertFalse(nonExistentDeleteResult, "Delete of non-existent object should return false");
    }
}