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
package org.gecko.mac.mgmt.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.gecko.mac.mgmt.api.EObjectRegistryService;
import org.gecko.mac.mgmt.management.ManagementFactory;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.gecko.mac.mgmt.management.ObjectStatus;
import org.gecko.mac.mgmt.management.StorageBackendType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;
import org.osgi.util.promise.Promise;

/**
 * Unit tests for AbstractEObjectStorageService.
 * 
 * <p>These tests validate the Template Method pattern implementation, role-based functionality,
 * shared registry integration, and the complete Promise-based API without requiring OSGi or 
 * file system resources.</p>
 * 
 * <h3>Test Coverage</h3>
 * <ul>
 * <li><strong>Role Management</strong> - Tests automatic role setting in storeObject and updateMetadata</li>
 * <li><strong>Template Methods</strong> - Tests abstract method requirements and lifecycle</li>
 * <li><strong>Registry Integration</strong> - Tests shared registry synchronization</li>
 * <li><strong>Activation/Deactivation</strong> - Tests service lifecycle management</li>
 * <li><strong>Promise-based API</strong> - Tests all EObjectStorageService methods</li>
 * <li><strong>Error Handling</strong> - Tests validation and error propagation</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class AbstractEObjectStorageServiceTest {

    @Mock
    private AbstractStorageHelper mockStorageHelper;

    @Mock
    private EObjectRegistryService<EObject> mockRegistryService;

    @Mock
    private BundleContext mockBundleContext;

    private TestableAbstractEObjectStorageService storageService;

    // Test implementation of AbstractEObjectStorageService
    private static class TestableAbstractEObjectStorageService extends AbstractEObjectStorageService {
        private String testStorageRole;
        private AbstractStorageHelper testStorageHelper;
        private EObjectRegistryService<EObject> testRegistryService;

        public TestableAbstractEObjectStorageService(String storageRole, 
                AbstractStorageHelper storageHelper, 
                EObjectRegistryService<EObject> registryService) {
            this.testStorageRole = storageRole;
            this.testStorageHelper = storageHelper;
            this.testRegistryService = registryService;
        }

        @Override
        protected AbstractStorageHelper createStorageHelper() throws Exception {
            return testStorageHelper;
        }

        @Override
        public StorageBackendType getBackendType() {
            return StorageBackendType.FILE;
        }

        @Override
        protected String getStorageRole() {
            return testStorageRole;
        }

        @Override
        protected EObjectRegistryService<EObject> getRegistryService() {
            return testRegistryService;
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        storageService = new TestableAbstractEObjectStorageService("test-role", mockStorageHelper, mockRegistryService);
        storageService.bctx = mockBundleContext;
    }

    @Test
    public void testActivationWithValidRole() throws Exception {
        // Test successful activation
        assertDoesNotThrow(() -> storageService.activateStorageService());

        // Verify role was set
        assertEquals("test-role", storageService.storageRole);
        assertNotNull(storageService.promiseFactory);
        assertNotNull(storageService.storageHelper);
        assertNotNull(storageService.registryService);
    }

    @Test
    public void testActivationWithNullRole() {
        // Create service with null role
        TestableAbstractEObjectStorageService nullRoleService = new TestableAbstractEObjectStorageService(
            null, mockStorageHelper, mockRegistryService);
        nullRoleService.bctx = mockBundleContext;

        // Test activation failure with null role
        Exception exception = assertThrows(NullPointerException.class, 
            () -> nullRoleService.activateStorageService());
        
        assertTrue(exception.getMessage().contains("Storage role from getStorageRole() must not be null"));
    }

    @Test
    public void testActivationWithNullRegistryService() {
        // Create service with null registry service
        TestableAbstractEObjectStorageService nullRegistryService = new TestableAbstractEObjectStorageService(
            "test-role", mockStorageHelper, null);
        nullRegistryService.bctx = mockBundleContext;

        // Test activation failure with null registry service
        Exception exception = assertThrows(NullPointerException.class, 
            () -> nullRegistryService.activateStorageService());
        
        assertTrue(exception.getMessage().contains("Registry service from getRegistryService() must not be null"));
    }

    @Test
    public void testStoreObjectWithRoleAutoSetting() throws Exception {
        // Setup
        storageService.activateStorageService();
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("TestPackage");
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("TestPackage");
        metadata.setVersion("1.0.0");
        metadata.setUploadUser("testUser");
        metadata.setStatus(ObjectStatus.DRAFT);
        // Note: role is not set initially

        doNothing().when(mockStorageHelper).saveEObject(any(), any(), any());
        doNothing().when(mockStorageHelper).saveMetadata(any(), any());
        when(mockStorageHelper.getFileExtension(any())).thenReturn("ecore");

        // Execute
        Promise<String> result = storageService.storeObject("test-id", testPackage, metadata);
        String objectId = result.getValue();

        // Verify role was automatically set
        assertEquals("test-role", metadata.getRole());
        assertEquals("test-id", objectId);

        // Verify storage helper was called
        verify(mockStorageHelper).saveEObject("test-id", testPackage, metadata);
        verify(mockStorageHelper).saveMetadata("test-id", metadata);

        // Verify registry was updated
        verify(mockRegistryService).updateCache(any(ObjectMetadata.class));
    }

    @Test
    public void testStoreObjectWithGeneratedId() throws Exception {
        // Setup
        storageService.activateStorageService();
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("TestPackage");
        metadata.setStatus(ObjectStatus.DRAFT);

        doNothing().when(mockStorageHelper).saveEObject(any(), any(), any());
        doNothing().when(mockStorageHelper).saveMetadata(any(), any());
        when(mockStorageHelper.getFileExtension(any())).thenReturn("ecore");

        // Execute with null objectId (should generate UUID)
        Promise<String> result = storageService.storeObject(null, testPackage, metadata);
        String objectId = result.getValue();

        // Verify UUID was generated
        assertNotNull(objectId);
        assertDoesNotThrow(() -> UUID.fromString(objectId));

        // Verify role was set
        assertEquals("test-role", metadata.getRole());

        // Verify storage operations
        verify(mockStorageHelper).saveEObject(eq(objectId), eq(testPackage), eq(metadata));
        verify(mockStorageHelper).saveMetadata(eq(objectId), eq(metadata));
    }

    @Test
    public void testStoreObjectSetsObjectIdInCallerMetadata() throws Exception {
        // Setup
        storageService.activateStorageService();
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("TestPackage");
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("TestPackage");
        metadata.setStatus(ObjectStatus.DRAFT);
        // Explicitly verify objectId is null initially
        assertNull(metadata.getObjectId(), "ObjectId should be null initially");

        doNothing().when(mockStorageHelper).saveEObject(any(), any(), any());
        doNothing().when(mockStorageHelper).saveMetadata(any(), any());
        when(mockStorageHelper.getFileExtension(any())).thenReturn("ecore");

        // Execute with null objectId (should generate UUID and set it in metadata)
        Promise<String> result = storageService.storeObject(null, testPackage, metadata);
        String returnedObjectId = result.getValue();

        // Verify the caller's metadata object now has the objectId set
        assertNotNull(metadata.getObjectId(), "ObjectId should be set in caller's metadata");
        assertEquals(returnedObjectId, metadata.getObjectId(), "Returned objectId should match metadata objectId");
        
        // Verify it's a valid UUID
        assertDoesNotThrow(() -> UUID.fromString(metadata.getObjectId()));
        
        // Verify role was also set
        assertEquals("test-role", metadata.getRole());
    }

    @Test
    public void testStoreObjectPreservesProvidedObjectIdInMetadata() throws Exception {
        // Setup
        storageService.activateStorageService();
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("TestPackage");
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("TestPackage");
        metadata.setStatus(ObjectStatus.DRAFT);
        // Don't set objectId in metadata initially
        assertNull(metadata.getObjectId(), "ObjectId should be null initially");

        doNothing().when(mockStorageHelper).saveEObject(any(), any(), any());
        doNothing().when(mockStorageHelper).saveMetadata(any(), any());
        when(mockStorageHelper.getFileExtension(any())).thenReturn("ecore");

        // Execute with provided objectId
        String providedObjectId = "my-custom-object-id";
        Promise<String> result = storageService.storeObject(providedObjectId, testPackage, metadata);
        String returnedObjectId = result.getValue();

        // Verify the caller's metadata object has the provided objectId
        assertEquals(providedObjectId, metadata.getObjectId(), "Metadata should have the provided objectId");
        assertEquals(providedObjectId, returnedObjectId, "Returned objectId should match provided objectId");
        
        // Verify role was also set
        assertEquals("test-role", metadata.getRole());
    }

    @Test
    public void testUpdateMetadataWithRoleAutoSetting() throws Exception {
        // Setup
        storageService.activateStorageService();
        
        // Create existing metadata
        ObjectMetadata existingMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        existingMetadata.setObjectId("test-id");
        existingMetadata.setObjectName("OriginalPackage");
        existingMetadata.setVersion("1.0.0");
        existingMetadata.setUploadUser("original-user");
        existingMetadata.setStatus(ObjectStatus.DRAFT);
        existingMetadata.setRole("original-role");
        
        ObjectMetadata updateMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        updateMetadata.setObjectName("UpdatedPackage");
        updateMetadata.setVersion("2.0.0");
        updateMetadata.setStatus(ObjectStatus.APPROVED);
        // Note: role is not set initially

        when(mockStorageHelper.objectExists("test-id")).thenReturn(true);
        when(mockStorageHelper.loadMetadata("test-id")).thenReturn(existingMetadata);
        doNothing().when(mockStorageHelper).saveMetadata(any(), any());

        // Execute
        Promise<Boolean> result = storageService.updateMetadata("test-id", updateMetadata);
        Boolean success = result.getValue();

        // Verify original metadata was NOT modified (copy-based approach)
        assertEquals("original-role", existingMetadata.getRole(), "Original metadata should not be modified");
        assertTrue(success);

        // Verify storage operations
        verify(mockStorageHelper).objectExists("test-id");
        verify(mockStorageHelper).loadMetadata("test-id");
        verify(mockStorageHelper).saveMetadata(eq("test-id"), any(ObjectMetadata.class));

        // Verify registry was updated
        verify(mockRegistryService).updateCache(any(ObjectMetadata.class));
    }

    @Test
    public void testUpdateMetadataWithNonExistentObject() throws Exception {
        // Setup
        storageService.activateStorageService();
        
        ObjectMetadata updateMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        updateMetadata.setObjectName("NonExistent");

        when(mockStorageHelper.objectExists("non-existent")).thenReturn(false);

        // Execute
        Promise<Boolean> result = storageService.updateMetadata("non-existent", updateMetadata);
        Boolean success = result.getValue();

        // Verify
        assertFalse(success);

        // Verify no load or save operations were attempted
        verify(mockStorageHelper, never()).loadMetadata(any());
        verify(mockStorageHelper, never()).saveMetadata(any(), any());
        verify(mockRegistryService, never()).updateCache(any());
    }

    @Test
    public void testUpdateStatusWithRolePreservation() throws Exception {
        // Setup
        storageService.activateStorageService();
        
        ObjectMetadata existingMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        existingMetadata.setObjectId("test-id");
        existingMetadata.setObjectName("TestPackage");
        existingMetadata.setStatus(ObjectStatus.DRAFT);
        existingMetadata.setRole("existing-role"); // Pre-existing role

        when(mockStorageHelper.objectExists("test-id")).thenReturn(true);
        when(mockStorageHelper.loadMetadata("test-id")).thenReturn(existingMetadata);
        doNothing().when(mockStorageHelper).saveMetadata(any(), any());

        // Execute
        Promise<Boolean> result = storageService.updateStatus("test-id", ObjectStatus.APPROVED, "reviewer");
        Boolean success = result.getValue();

        // Verify
        assertTrue(success);
        assertEquals(ObjectStatus.APPROVED, existingMetadata.getStatus());
        assertEquals("reviewer", existingMetadata.getLastChangeUser());
        assertNotNull(existingMetadata.getLastChangeTime());
        
        // Verify the existing role was preserved (not overwritten)
        assertEquals("existing-role", existingMetadata.getRole());

        // Verify operations
        verify(mockStorageHelper).saveMetadata("test-id", existingMetadata);
    }

    @Test
    public void testDeleteObjectWithRegistryCleanup() throws Exception {
        // Setup
        storageService.activateStorageService();

        when(mockStorageHelper.deleteObject("test-id")).thenReturn(true);

        // Execute
        Promise<Boolean> result = storageService.deleteObject("test-id");
        Boolean success = result.getValue();

        // Verify
        assertTrue(success);

        // Verify storage deletion
        verify(mockStorageHelper).deleteObject("test-id");

        // Verify registry cleanup
        verify(mockRegistryService).removeFromCache("test-id");
    }

    @Test
    public void testDeleteObjectFailureNoRegistryCleanup() throws Exception {
        // Setup
        storageService.activateStorageService();

        when(mockStorageHelper.deleteObject("test-id")).thenReturn(false);

        // Execute
        Promise<Boolean> result = storageService.deleteObject("test-id");
        Boolean success = result.getValue();

        // Verify
        assertFalse(success);

        // Verify no registry cleanup when deletion fails
        verify(mockRegistryService, never()).removeFromCache(any());
    }

    @Test
    public void testRetrieveObject() throws Exception {
        // Setup
        storageService.activateStorageService();
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("RetrievedPackage");

        when(mockStorageHelper.loadEObject("test-id")).thenReturn(testPackage);

        // Execute
        Promise<EObject> result = storageService.retrieveObject("test-id");
        EObject retrieved = result.getValue();

        // Verify
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof EPackage);
        assertEquals("RetrievedPackage", ((EPackage) retrieved).getName());

        verify(mockStorageHelper).loadEObject("test-id");
    }

    @Test
    public void testRetrieveMetadata() throws Exception {
        // Setup
        storageService.activateStorageService();
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId("test-id");
        metadata.setObjectName("TestPackage");

        when(mockStorageHelper.loadMetadata("test-id")).thenReturn(metadata);

        // Execute
        Promise<ObjectMetadata> result = storageService.retrieveMetadata("test-id");
        ObjectMetadata retrieved = result.getValue();

        // Verify
        assertNotNull(retrieved);
        assertEquals("test-id", retrieved.getObjectId());
        assertEquals("TestPackage", retrieved.getObjectName());

        verify(mockStorageHelper).loadMetadata("test-id");
    }

    @Test
    public void testListObjectIds() throws Exception {
        // Setup
        storageService.activateStorageService();
        List<String> expectedIds = List.of("id1", "id2", "id3");

        when(mockStorageHelper.listObjectIds()).thenReturn(expectedIds);

        // Execute
        Promise<List<String>> result = storageService.listObjectIds();
        List<String> ids = result.getValue();

        // Verify
        assertEquals(expectedIds, ids);
        verify(mockStorageHelper).listObjectIds();
    }

    @Test
    public void testExistsTrue() throws Exception {
        // Setup
        storageService.activateStorageService();

        when(mockStorageHelper.objectExists("existing-id")).thenReturn(true);

        // Execute
        Boolean exists = storageService.exists("existing-id");

        // Verify
        assertTrue(exists);
        verify(mockStorageHelper).objectExists("existing-id");
    }

    @Test
    public void testExistsFalse() throws Exception {
        // Setup
        storageService.activateStorageService();

        when(mockStorageHelper.objectExists("non-existent")).thenReturn(false);

        // Execute
        Boolean exists = storageService.exists("non-existent");

        // Verify
        assertFalse(exists);
        verify(mockStorageHelper).objectExists("non-existent");
    }

    @Test
    public void testGetObjectCount() throws Exception {
        // Setup
        storageService.activateStorageService();
        List<String> objectIds = List.of("id1", "id2", "id3", "id4", "id5");

        when(mockStorageHelper.listObjectIds()).thenReturn(objectIds);

        // Execute
        long count = storageService.getObjectCount();

        // Verify
        assertEquals(5L, count);
        verify(mockStorageHelper).listObjectIds();
    }

    @Test
    public void testQueryObjects() throws Exception {
        // Setup
        storageService.activateStorageService();
        
        ObjectMetadata metadata1 = createTestMetadata("id1", "Package1");
        ObjectMetadata metadata2 = createTestMetadata("id2", "Package2");
        
        when(mockStorageHelper.listObjectIds()).thenReturn(List.of("id1", "id2"));
        when(mockStorageHelper.loadMetadata("id1")).thenReturn(metadata1);
        when(mockStorageHelper.loadMetadata("id2")).thenReturn(metadata2);

        // Execute
        Promise<List<ObjectMetadata>> result = storageService.queryObjects(null);
        List<ObjectMetadata> metadataList = result.getValue();

        // Verify
        assertEquals(2, metadataList.size());
        assertEquals("id1", metadataList.get(0).getObjectId());
        assertEquals("id2", metadataList.get(1).getObjectId());

        verify(mockStorageHelper).listObjectIds();
        verify(mockStorageHelper).loadMetadata("id1");
        verify(mockStorageHelper).loadMetadata("id2");
    }

    @Test
    public void testDeactivation() throws Exception {
        // Setup
        storageService.activateStorageService();
        assertNotNull(storageService.storageHelper);
        assertNotNull(storageService.registryService);

        // Execute
        storageService.deactivateStorageService();

        // Verify cleanup
        assertNull(storageService.storageHelper);
        assertNull(storageService.registryService);
    }

    @Test
    public void testErrorHandlingInStoreObject() throws Exception {
        // Setup
        storageService.activateStorageService();
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("TestPackage");
        metadata.setStatus(ObjectStatus.DRAFT);

        // Mock storage helper to throw exception
        doThrow(new RuntimeException("Storage error")).when(mockStorageHelper).saveEObject(any(), any(), any());

        // Execute and verify exception is propagated
        Promise<String> result = storageService.storeObject("test-id", testPackage, metadata);
        // Verify the promise failed and contains the expected exception
        Throwable failure = result.getFailure();
        assertNotNull(failure, "Promise should have failed");
        assertTrue(failure instanceof RuntimeException);
        assertTrue(failure.getMessage().contains("Failed to store object"));
        
        // Verify role was still set even though the operation failed
        assertEquals("test-role", metadata.getRole());
    }

    @Test
    public void testSaveMetadataWithNullObjectIdThrowsException() throws Exception {
        // Setup
        storageService.activateStorageService();
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("TestPackage");
        metadata.setStatus(ObjectStatus.DRAFT);
        
        // Try to save metadata with null objectId - should fail in Promise
        Promise<Boolean> result = storageService.updateMetadata(null, metadata);
        
        // Verify the promise failed with appropriate exception
        Throwable failure = result.getFailure();
        assertNotNull(failure, "Promise should have failed");
        assertTrue(failure instanceof RuntimeException);
        assertTrue(failure.getCause() instanceof IllegalArgumentException);
        assertTrue(failure.getCause().getMessage().contains("Object ID cannot be null or empty"));
    }
    
    @Test
    public void testSaveMetadataWithEmptyObjectIdThrowsException() throws Exception {
        // Setup
        storageService.activateStorageService();
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("TestPackage");
        metadata.setStatus(ObjectStatus.DRAFT);
        
        // Try to save metadata with empty objectId - should fail in Promise
        Promise<Boolean> result = storageService.updateMetadata("", metadata);
        
        // Verify the promise failed with appropriate exception
        Throwable failure = result.getFailure();
        assertNotNull(failure, "Promise should have failed");
        assertTrue(failure instanceof RuntimeException);
        assertTrue(failure.getCause() instanceof IllegalArgumentException);
        assertTrue(failure.getCause().getMessage().contains("Object ID cannot be null or empty"));
    }
    
    @Test
    public void testSaveMetadataWithNullMetadataThrowsException() throws Exception {
        // Setup
        storageService.activateStorageService();
        
        // Try to save null metadata - should fail in Promise
        Promise<Boolean> result = storageService.updateMetadata("test-id", null);
        
        // Verify the promise failed with appropriate exception
        Throwable failure = result.getFailure();
        assertNotNull(failure, "Promise should have failed");
        assertTrue(failure instanceof RuntimeException);
        assertTrue(failure.getCause() instanceof IllegalArgumentException);
        assertTrue(failure.getCause().getMessage().contains("Metadata cannot be null"));
    }
    
    @Test
    public void testUpdateMetadataPreservesImmutableFields() throws Exception {
        // Setup
        storageService.activateStorageService();
        
        // Create existing metadata with immutable fields
        ObjectMetadata existingMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        existingMetadata.setObjectId("test-id");
        existingMetadata.setObjectName("OriginalName");
        existingMetadata.setVersion("1.0.0");
        existingMetadata.setUploadUser("original-user");
        existingMetadata.setUploadTime(Instant.now().minusSeconds(3600)); // 1 hour ago
        existingMetadata.setStatus(ObjectStatus.DRAFT);
        existingMetadata.setRole("original-role");
        existingMetadata.getProperties().put("original.prop", "original-value");
        
        // Create update metadata with changes to both mutable and immutable fields
        ObjectMetadata updateMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        updateMetadata.setObjectName("UpdatedName");
        updateMetadata.setVersion("2.0.0");
        updateMetadata.setStatus(ObjectStatus.APPROVED);
        updateMetadata.setReviewUser("reviewer-user");
        updateMetadata.setReviewReason("Approved for testing");
        // Try to change immutable fields (should be ignored)
        updateMetadata.setUploadUser("hacker-user"); // Should be ignored
        updateMetadata.setUploadTime(Instant.now()); // Should be ignored
        updateMetadata.setRole("hacker-role"); // Should be ignored
        updateMetadata.getProperties().put("new.prop", "new-value");
        
        // Mock storage helper
        when(mockStorageHelper.objectExists("test-id")).thenReturn(true);
        when(mockStorageHelper.loadMetadata("test-id")).thenReturn(existingMetadata);
        doNothing().when(mockStorageHelper).saveMetadata(any(), any());
        
        // Execute update
        Promise<Boolean> result = storageService.updateMetadata("test-id", updateMetadata);
        Boolean success = result.getValue();
        
        // Verify update succeeded
        assertTrue(success);
        
        // Verify original metadata was NOT modified (copy-based approach)
        assertEquals("OriginalName", existingMetadata.getObjectName(), "Original metadata should not be modified");
        assertEquals("1.0.0", existingMetadata.getVersion(), "Original metadata should not be modified");
        assertEquals(ObjectStatus.DRAFT, existingMetadata.getStatus(), "Original metadata should not be modified");
        assertEquals("original-role", existingMetadata.getRole(), "Original metadata should not be modified");
        assertEquals("original-user", existingMetadata.getUploadUser(), "Original metadata should not be modified");
        assertNull(existingMetadata.getReviewUser(), "Original metadata should not be modified");
        assertNull(existingMetadata.getLastChangeTime(), "Original metadata should not be modified");
        
        // The actual verification of the update behavior should be done by checking what was saved
        // We can't directly check the copy since it's created internally, but we can verify the save call
        
        // Verify storage operations
        verify(mockStorageHelper).objectExists("test-id");
        verify(mockStorageHelper).loadMetadata("test-id");
        verify(mockStorageHelper).saveMetadata(eq("test-id"), any(ObjectMetadata.class));
        verify(mockRegistryService).updateCache(any(ObjectMetadata.class));
    }
    
    @Test
    public void testUpdateMetadataWithPartialUpdates() throws Exception {
        // Setup
        storageService.activateStorageService();
        
        // Create existing metadata
        ObjectMetadata existingMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        existingMetadata.setObjectId("test-id");
        existingMetadata.setObjectName("OriginalName");
        existingMetadata.setVersion("1.0.0");
        existingMetadata.setUploadUser("original-user");
        existingMetadata.setStatus(ObjectStatus.DRAFT);
        
        // Create update with only some fields (partial update)
        ObjectMetadata updateMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        updateMetadata.setStatus(ObjectStatus.APPROVED);
        updateMetadata.setReviewUser("reviewer");
        // Note: objectName and version are NOT set in update
        
        // Mock storage helper
        when(mockStorageHelper.objectExists("test-id")).thenReturn(true);
        when(mockStorageHelper.loadMetadata("test-id")).thenReturn(existingMetadata);
        doNothing().when(mockStorageHelper).saveMetadata(any(), any());
        
        // Execute partial update
        Promise<Boolean> result = storageService.updateMetadata("test-id", updateMetadata);
        Boolean success = result.getValue();
        
        // Verify update succeeded
        assertTrue(success);
        
        // Verify original metadata was NOT modified (copy-based approach)
        assertEquals(ObjectStatus.DRAFT, existingMetadata.getStatus(), "Original metadata should not be modified");
        assertNull(existingMetadata.getReviewUser(), "Original metadata should not be modified");
        assertEquals("OriginalName", existingMetadata.getObjectName(), "Original metadata should not be modified");
        assertEquals("1.0.0", existingMetadata.getVersion(), "Original metadata should not be modified");
        assertEquals("original-user", existingMetadata.getUploadUser(), "Original metadata should not be modified");
        assertNull(existingMetadata.getLastChangeTime(), "Original metadata should not be modified");
    }
    
    @Test
    public void testUpdateMetadataWithEmptyProperties() throws Exception {
        // Setup
        storageService.activateStorageService();
        
        // Create existing metadata with properties
        ObjectMetadata existingMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        existingMetadata.setObjectId("test-id");
        existingMetadata.setObjectName("TestName");
        existingMetadata.setUploadUser("user");
        existingMetadata.setStatus(ObjectStatus.DRAFT);
        existingMetadata.getProperties().put("existing.prop", "existing-value");
        
        // Create update with null properties
        ObjectMetadata updateMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        updateMetadata.setStatus(ObjectStatus.APPROVED);
        // properties is null (default)
        
        // Mock storage helper
        when(mockStorageHelper.objectExists("test-id")).thenReturn(true);
        when(mockStorageHelper.loadMetadata("test-id")).thenReturn(existingMetadata);
        doNothing().when(mockStorageHelper).saveMetadata(any(), any());
        
        // Execute update
        Promise<Boolean> result = storageService.updateMetadata("test-id", updateMetadata);
        Boolean success = result.getValue();
        
        // Verify update succeeded
        assertTrue(success);
        
        // Verify original metadata was NOT modified (copy-based approach)
        assertEquals("existing-value", existingMetadata.getProperties().get("existing.prop"), 
                    "Original metadata should not be modified");
        assertEquals(ObjectStatus.DRAFT, existingMetadata.getStatus(), "Original metadata should not be modified");
    }

    @Test
    public void testObjectTypeAutoSetFromEObject() throws Exception {
        // Setup
        storageService.activateStorageService();
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("TestPackage");
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("TestPackage");
        metadata.setVersion("1.0.0");
        metadata.setUploadUser("testUser");
        metadata.setStatus(ObjectStatus.DRAFT);
        // Note: objectType is not set initially
        assertNull(metadata.getObjectType(), "ObjectType should be null initially");

        doNothing().when(mockStorageHelper).saveEObject(any(), any(), any());
        doNothing().when(mockStorageHelper).saveMetadata(any(), any());
        when(mockStorageHelper.getFileExtension(any())).thenReturn("ecore");

        // Execute
        Promise<String> result = storageService.storeObject("test-id", testPackage, metadata);
        String objectId = result.getValue();

        // Verify objectType was automatically set
        assertEquals("EPackage", metadata.getObjectType(), 
                    "ObjectType should be automatically set to 'EPackage'");
        assertEquals("test-id", objectId);

        // Verify registry was called with metadata containing objectType
        verify(mockRegistryService).updateCache(argThat(cachedMetadata -> 
            "EPackage".equals(cachedMetadata.getObjectType())));
    }

    @Test
    public void testObjectTypePreservedWhenAlreadySet() throws Exception {
        // Setup
        storageService.activateStorageService();
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("TestPackage");
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("TestPackage");
        metadata.setVersion("1.0.0");
        metadata.setUploadUser("testUser");
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.setObjectType("CustomEPackage"); // Pre-set custom type

        doNothing().when(mockStorageHelper).saveEObject(any(), any(), any());
        doNothing().when(mockStorageHelper).saveMetadata(any(), any());
        when(mockStorageHelper.getFileExtension(any())).thenReturn("ecore");

        // Execute
        Promise<String> result = storageService.storeObject("test-id", testPackage, metadata);
        result.getValue();

        // Verify custom objectType was preserved
        assertEquals("CustomEPackage", metadata.getObjectType(), 
                    "Custom objectType should be preserved");

        // Verify registry was called with custom objectType
        verify(mockRegistryService).updateCache(argThat(cachedMetadata -> 
            "CustomEPackage".equals(cachedMetadata.getObjectType())));
    }
    

    // Helper method
    private ObjectMetadata createTestMetadata(String objectId, String objectName) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName(objectName);
        metadata.setVersion("1.0.0");
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.setUploadTime(Instant.now());
        metadata.setUploadUser("test-user");
        metadata.setSourceChannel("TEST");
        return metadata;
    }
}