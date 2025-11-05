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
package org.eclipse.fennec.model.atlas.governance.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.governance.tests.annotations.GovernanceTestAnnotations.StorageRegistrySetup;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration test for StorageRegistry with real storage services.
 * 
 * <p>This test verifies that the StorageRegistry correctly discovers and manages
 * storage services in a real OSGi environment with file-based storage backends.</p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
class StorageRegistryIntegrationTest {

    @TempDir
    static Path tempDir;

    private ManagementFactory managementFactory;

    @BeforeEach
    void setUp() {
        // Set temp directory system property for OSGi test configurations
        System.setProperty("tempDir", tempDir.toString());
        
        managementFactory = ManagementFactory.eINSTANCE;
        
    }

    @Test
    @StorageRegistrySetup
    void testStorageRegistryServiceAvailability(@InjectService StorageRegistry storageRegistry) {
        // Verify that StorageRegistry service is available
        assertNotNull(storageRegistry);
    }

    @Test
    @StorageRegistrySetup
    void testStorageServiceDiscovery(@InjectService StorageRegistry storageRegistry) {
    	// Verify that the registry is properly injected
        assertNotNull(storageRegistry, "StorageRegistry should be injected");    	
        // Verify that all expected storage services are registered
        EList<String> availableRoles = storageRegistry.getAvailableRoles();
        
        // We expect at least the roles configured in StorageRegistrySetup
        assertTrue(availableRoles.size() >= 3, "Should have at least 3 storage roles");
        assertTrue(availableRoles.contains("draft"), "Should have draft storage");
        assertTrue(availableRoles.contains("release"), "Should have release storage");
        assertTrue(availableRoles.contains("documentation"), "Should have documentation storage");
    }

    @Test
    @StorageRegistrySetup
    void testGetStorageByRole(@InjectService StorageRegistry storageRegistry) {
    	// Verify that the registry is properly injected
        assertNotNull(storageRegistry, "StorageRegistry should be injected");
        // Test retrieving storage services by role
        EObjectStorageService<EObject> draftStorage = storageRegistry.getStorageByRole("draft");
        EObjectStorageService<EObject> releaseStorage = storageRegistry.getStorageByRole("release");
        EObjectStorageService<EObject> documentationStorage = storageRegistry.getStorageByRole("documentation");

        assertNotNull(draftStorage, "Draft storage should be available");
        assertNotNull(releaseStorage, "Release storage should be available");
        assertNotNull(documentationStorage, "Documentation storage should be available");
    }

    @Test
    @StorageRegistrySetup
    void testGetAllStorages(@InjectService StorageRegistry storageRegistry) {
    	// Verify that the registry is properly injected
        assertNotNull(storageRegistry, "StorageRegistry should be injected");
        // Test getting all registered storage services
        EList<EObjectStorageService<EObject>> allStorages = storageRegistry.getAllStorages();
        
        assertTrue(allStorages.size() >= 3, "Should have at least 3 storage services");
    }

    @Test
    @StorageRegistrySetup
    void testStorageStatistics(@InjectService StorageRegistry storageRegistry) {
    	// Verify that the registry is properly injected
        assertNotNull(storageRegistry, "StorageRegistry should be injected");
        // Test getting storage statistics
        Map<String, Object> statistics = storageRegistry.getStorageStatistics();
        
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("totalObjectCount"));
        assertTrue(statistics.containsKey("roleCount"));
        assertTrue(statistics.containsKey("roleStatistics"));
        assertTrue(statistics.containsKey("availableRoles"));
        
        // Verify that we have the expected number of roles
        Integer roleCount = (Integer) statistics.get("roleCount");
        assertTrue(roleCount >= 3, "Should have at least 3 roles in statistics");
    }

    @Test
    @StorageRegistrySetup
    void testCrossStorageSearch(@InjectService StorageRegistry storageRegistry) throws Exception {
    	// Verify that the registry is properly injected
        assertNotNull(storageRegistry, "StorageRegistry should be injected");
        // Store a test object in draft storage to verify cross-storage search
        EObjectStorageService<EObject> draftStorage = storageRegistry.getStorageByRole("draft");
        assertNotNull(draftStorage);

        // Create test metadata
        ObjectMetadata testMetadata = createTestMetadata("test-object-1", ObjectStatus.DRAFT, "TestPackage");
        
        // Store a simple EObject (we'll use ObjectMetadata itself as the EObject for simplicity)
        String objectId = draftStorage.storeObject("test-object-1", testMetadata, testMetadata).getValue();
        assertNotNull(objectId);

        // Now test cross-storage search
        ObjectQuery query = managementFactory.createObjectQuery();
        query.setStatus(ObjectStatus.DRAFT);
        
        EList<ObjectMetadata> searchResults = storageRegistry.searchMetadataAcrossRoles(query);
        
        // Should find at least our test object
        assertTrue(searchResults.size() >= 1, "Should find at least the test object");
        
        // Verify our test object is in the results
        boolean foundTestObject = searchResults.stream()
                .anyMatch(metadata -> "test-object-1".equals(metadata.getObjectId()));
        assertTrue(foundTestObject, "Should find our test object in cross-storage search");
        
        // Clean up
        draftStorage.deleteObject(objectId);
    }

    @Test
    @StorageRegistrySetup
    void testUpdateGovernanceDocumentationId(@InjectService StorageRegistry storageRegistry) throws Exception {
    	// Verify that the registry is properly injected
        assertNotNull(storageRegistry, "StorageRegistry should be injected");
        // Store test objects in multiple storage services
        EObjectStorageService<EObject> draftStorage = storageRegistry.getStorageByRole("draft");
        EObjectStorageService<EObject> releaseStorage = storageRegistry.getStorageByRole("release");
        
        assertNotNull(draftStorage);
        assertNotNull(releaseStorage);

        // Create test objects with the same object type but different statuses
        ObjectMetadata draftMetadata = createTestMetadata("draft-obj", ObjectStatus.DRAFT, "TestPackage");
        ObjectMetadata releaseMetadata = createTestMetadata("release-obj", ObjectStatus.APPROVED, "TestPackage");
        
        // Create simple test EObjects (using metadata as EObject for simplicity in this test)
        EObject testDraftObject = managementFactory.createObjectMetadata();
        EObject testReleaseObject = managementFactory.createObjectMetadata();
        
        // Store objects
        String draftId = draftStorage.storeObject("draft-obj", testDraftObject, draftMetadata).getValue();
        String releaseId = releaseStorage.storeObject("release-obj", testReleaseObject, releaseMetadata).getValue();
        
        // Update governance documentation ID for draft role only
        int draftUpdatedCount = storageRegistry.updateGovernanceDocumentationId(
            "draft", "TestPackage", "gov-doc-456", "Integration test update"
        );
        
        // Update governance documentation ID for release role only
        int releaseUpdatedCount = storageRegistry.updateGovernanceDocumentationId(
            "release", "TestPackage", "gov-doc-789", "Integration test update"
        );
        
        // Should have updated one object in each storage
        assertEquals(1, draftUpdatedCount, "Should have updated 1 object in draft storage");
        assertEquals(1, releaseUpdatedCount, "Should have updated 1 object in release storage");
        
        // Verify the governance documentation IDs were set correctly (different per role)
        ObjectMetadata updatedDraftMetadata = draftStorage.retrieveMetadata(draftId).getValue();
        ObjectMetadata updatedReleaseMetadata = releaseStorage.retrieveMetadata(releaseId).getValue();
        
        assertEquals("gov-doc-456", updatedDraftMetadata.getGovernanceDocumentationId());
        assertEquals("gov-doc-789", updatedReleaseMetadata.getGovernanceDocumentationId());
        
        // Clean up
        draftStorage.deleteObject(draftId);
        releaseStorage.deleteObject(releaseId);
    }

    @Test
    @StorageRegistrySetup
    void testStorageBackendTypes(@InjectService StorageRegistry storageRegistry) {
    	// Verify that the registry is properly injected
        assertNotNull(storageRegistry, "StorageRegistry should be injected");
        // Verify that storage services have proper backend types configured
        EObjectStorageService<EObject> draftStorage = storageRegistry.getStorageByRole("draft");
        EObjectStorageService<EObject> releaseStorage = storageRegistry.getStorageByRole("release");
        EObjectStorageService<EObject> documentationStorage = storageRegistry.getStorageByRole("documentation");

        assertNotNull(draftStorage.getBackendType());
        assertNotNull(releaseStorage.getBackendType());
        assertNotNull(documentationStorage.getBackendType());
        
        // All should be FILE backend for this test setup
        assertEquals("FILE", draftStorage.getBackendType().toString());
        assertEquals("FILE", releaseStorage.getBackendType().toString());
        assertEquals("FILE", documentationStorage.getBackendType().toString());
    }

    private ObjectMetadata createTestMetadata(String objectId, ObjectStatus status, String objectType) {
        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setStatus(status);
        metadata.setObjectType(objectType);
        metadata.setUploadTime(java.time.Instant.now());
        metadata.setLastChangeTime(java.time.Instant.now());
        metadata.setUploadUser("test-user");
        return metadata;
    }
}