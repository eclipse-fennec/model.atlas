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
package org.eclipse.fennec.model.atlas.mgmt.registry;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BasicRegistryHelper.
 * 
 * <p>Tests the in-memory registry functionality including initialization,
 * indexing operations, search methods, and lifecycle management.</p>
 * 
 * <h3>Test Coverage</h3>
 * <ul>
 * <li><strong>Initialization</strong> - Tests registry startup and readiness</li>
 * <li><strong>Index Operations</strong> - updateIndex, removeFromIndex</li>
 * <li><strong>Search Methods</strong> - All find* methods from AbstractRegistryHelper</li>
 * <li><strong>Statistics</strong> - Registry statistics generation</li>
 * <li><strong>Lifecycle</strong> - Proper cleanup and resource management</li>
 * <li><strong>Error Handling</strong> - Null input validation and error conditions</li>
 * </ul>
 */
class BasicRegistryHelperTest {

    private BasicRegistryHelper registryHelper;
    private ManagementFactory factory;

    @BeforeEach
    void setUp() throws IOException {
        registryHelper = new BasicRegistryHelper();
        factory = ManagementFactory.eINSTANCE;
        registryHelper.initialize();
    }

    @Test
    void testInitialization() throws Exception {
        try (BasicRegistryHelper newHelper = new BasicRegistryHelper()) {
            // Should be able to initialize without errors
            assertDoesNotThrow(() -> newHelper.initialize());
            
            // Should be marked as initialized
            assertEquals("basic-memory", newHelper.getRegistryType());
        }
    }

    @Test
    void testUpdateIndex() throws IOException {
        // Create test metadata
        ObjectMetadata metadata = createTestMetadata("test-obj", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        
        // Update index
        registryHelper.updateIndex("test-obj", metadata);
        
        // Verify index was updated
        assertEquals(1, registryHelper.getObjectCount());
        assertTrue(registryHelper.exists("test-obj"));
    }

    @Test
    void testUpdateIndexValidation() {
        ObjectMetadata metadata = createTestMetadata("test-obj", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        
        // Test null object ID validation
        assertThrows(NullPointerException.class, () -> 
            registryHelper.updateIndex(null, metadata));
        
        // Test null metadata validation
        assertThrows(NullPointerException.class, () -> 
            registryHelper.updateIndex("test-obj", null));
    }

    @Test
    void testRemoveFromIndex() throws IOException {
        // Add object first
        ObjectMetadata metadata = createTestMetadata("test-obj", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        registryHelper.updateIndex("test-obj", metadata);
        
        // Verify it exists
        assertTrue(registryHelper.exists("test-obj"));
        
        // Remove from index
        registryHelper.removeFromIndex("test-obj");
        
        // Verify it's removed
        assertFalse(registryHelper.exists("test-obj"));
        assertEquals(0, registryHelper.getObjectCount());
    }

    @Test
    void testRemoveFromIndexValidation() {
        // Test null object ID validation
        assertThrows(NullPointerException.class, () -> 
            registryHelper.removeFromIndex(null));
    }

    @Test
    void testFindByStatus() throws IOException {
        // Create test objects with different statuses
        ObjectMetadata draft1 = createTestMetadata("draft1", "Package1", "1.0.0", ObjectStatus.DRAFT);
        ObjectMetadata draft2 = createTestMetadata("draft2", "Package2", "1.0.0", ObjectStatus.DRAFT);
        ObjectMetadata approved = createTestMetadata("approved1", "Package3", "1.0.0", ObjectStatus.APPROVED);
        
        // Add to index
        registryHelper.updateIndex("draft1", draft1);
        registryHelper.updateIndex("draft2", draft2);
        registryHelper.updateIndex("approved1", approved);
        
        // Test finding drafts
        List<String> drafts = registryHelper.findByStatus(ObjectStatus.DRAFT);
        assertEquals(2, drafts.size());
        assertTrue(drafts.contains("draft1"));
        assertTrue(drafts.contains("draft2"));
        
        // Test finding approved
        List<String> approvedList = registryHelper.findByStatus(ObjectStatus.APPROVED);
        assertEquals(1, approvedList.size());
        assertTrue(approvedList.contains("approved1"));
        
        // Test finding non-existent status
        List<String> archived = registryHelper.findByStatus(ObjectStatus.ARCHIVED);
        assertEquals(0, archived.size());
    }

    @Test
    void testFindByStatusValidation() {
        // Test null status validation
        assertThrows(NullPointerException.class, () -> 
            registryHelper.findByStatus(null));
    }

    @Test
    void testFindByObjectName() throws IOException {
        // Create test objects with same and different names
        ObjectMetadata obj1 = createTestMetadata("obj1", "PackageA", "1.0.0", ObjectStatus.DRAFT);
        ObjectMetadata obj2 = createTestMetadata("obj2", "PackageA", "2.0.0", ObjectStatus.APPROVED);
        ObjectMetadata obj3 = createTestMetadata("obj3", "PackageB", "1.0.0", ObjectStatus.DRAFT);
        
        // Add to index
        registryHelper.updateIndex("obj1", obj1);
        registryHelper.updateIndex("obj2", obj2);
        registryHelper.updateIndex("obj3", obj3);
        
        // Test finding by name
        List<String> packageAObjects = registryHelper.findByObjectName("PackageA");
        assertEquals(2, packageAObjects.size());
        assertTrue(packageAObjects.contains("obj1"));
        assertTrue(packageAObjects.contains("obj2"));
        
        List<String> packageBObjects = registryHelper.findByObjectName("PackageB");
        assertEquals(1, packageBObjects.size());
        assertTrue(packageBObjects.contains("obj3"));
        
        // Test non-existent name
        List<String> nonExistent = registryHelper.findByObjectName("NonExistent");
        assertEquals(0, nonExistent.size());
    }

    @Test
    void testFindByObjectNameValidation() {
        // Test null object name validation
        assertThrows(NullPointerException.class, () -> 
            registryHelper.findByObjectName(null));
    }

    @Test
    void testFindByRole() throws IOException {
        // Create test objects with different roles
        ObjectMetadata draft = createTestMetadata("draft-obj", "Package1", "1.0.0", ObjectStatus.DRAFT);
        draft.setRole("draft");
        ObjectMetadata approved = createTestMetadata("approved-obj", "Package2", "1.0.0", ObjectStatus.APPROVED);
        approved.setRole("approved");
        ObjectMetadata documentation = createTestMetadata("doc-obj", "Package3", "1.0.0", ObjectStatus.DEPLOYED);
        documentation.setRole("documentation");
        
        // Add to index
        registryHelper.updateIndex("draft-obj", draft);
        registryHelper.updateIndex("approved-obj", approved);
        registryHelper.updateIndex("doc-obj", documentation);
        
        // Test finding by role
        List<String> draftObjects = registryHelper.findByRole("draft");
        assertEquals(1, draftObjects.size());
        assertTrue(draftObjects.contains("draft-obj"));
        
        List<String> approvedObjects = registryHelper.findByRole("approved");
        assertEquals(1, approvedObjects.size());
        assertTrue(approvedObjects.contains("approved-obj"));
        
        // Test non-existent role
        List<String> productionObjects = registryHelper.findByRole("production");
        assertEquals(0, productionObjects.size());
    }

    @Test
    void testFindByRoleValidation() {
        // Test null role validation
        assertThrows(NullPointerException.class, () -> 
            registryHelper.findByRole(null));
    }

    @Test
    void testFindByObjectNameAndRole() throws IOException {
        // Create test objects with same name but different roles
        ObjectMetadata draft = createTestMetadata("draft-obj", "PackageName", "1.0.0", ObjectStatus.DRAFT);
        draft.setRole("draft");
        ObjectMetadata approved = createTestMetadata("approved-obj", "PackageName", "2.0.0", ObjectStatus.APPROVED);
        approved.setRole("approved");
        
        // Add to index
        registryHelper.updateIndex("draft-obj", draft);
        registryHelper.updateIndex("approved-obj", approved);
        
        // Test finding specific name+role combination
        Optional<String> draftResult = registryHelper.findByObjectNameAndRole("PackageName", "draft");
        assertTrue(draftResult.isPresent());
        assertEquals("draft-obj", draftResult.get());
        
        Optional<String> approvedResult = registryHelper.findByObjectNameAndRole("PackageName", "approved");
        assertTrue(approvedResult.isPresent());
        assertEquals("approved-obj", approvedResult.get());
        
        // Test non-existent combinations
        Optional<String> nonExistentName = registryHelper.findByObjectNameAndRole("NonExistent", "draft");
        assertFalse(nonExistentName.isPresent());
        
        Optional<String> nonExistentRole = registryHelper.findByObjectNameAndRole("PackageName", "production");
        assertFalse(nonExistentRole.isPresent());
    }

    @Test
    void testFindByObjectNameAndRoleValidation() {
        // Test null validation
        assertThrows(NullPointerException.class, () -> 
            registryHelper.findByObjectNameAndRole(null, "draft"));
        assertThrows(NullPointerException.class, () -> 
            registryHelper.findByObjectNameAndRole("PackageName", null));
    }

    @Test
    void testGetAllObjectIds() throws IOException {
        // Add multiple objects
        ObjectMetadata obj1 = createTestMetadata("obj1", "Package1", "1.0.0", ObjectStatus.DRAFT);
        ObjectMetadata obj2 = createTestMetadata("obj2", "Package2", "1.0.0", ObjectStatus.APPROVED);
        ObjectMetadata obj3 = createTestMetadata("obj3", "Package3", "1.0.0", ObjectStatus.DEPLOYED);
        
        registryHelper.updateIndex("obj1", obj1);
        registryHelper.updateIndex("obj2", obj2);
        registryHelper.updateIndex("obj3", obj3);
        
        // Get all IDs
        List<String> allIds = registryHelper.getAllObjectIds();
        assertEquals(3, allIds.size());
        assertTrue(allIds.contains("obj1"));
        assertTrue(allIds.contains("obj2"));
        assertTrue(allIds.contains("obj3"));
    }

    @Test
    void testGetObjectCount() throws IOException {
        // Start with empty registry
        assertEquals(0, registryHelper.getObjectCount());
        
        // Add objects one by one
        ObjectMetadata obj1 = createTestMetadata("obj1", "Package1", "1.0.0", ObjectStatus.DRAFT);
        registryHelper.updateIndex("obj1", obj1);
        assertEquals(1, registryHelper.getObjectCount());
        
        ObjectMetadata obj2 = createTestMetadata("obj2", "Package2", "1.0.0", ObjectStatus.APPROVED);
        registryHelper.updateIndex("obj2", obj2);
        assertEquals(2, registryHelper.getObjectCount());
        
        // Remove one
        registryHelper.removeFromIndex("obj1");
        assertEquals(1, registryHelper.getObjectCount());
    }

    @Test
    void testExists() throws IOException {
        // Test non-existent object
        assertFalse(registryHelper.exists("non-existent"));
        
        // Add object
        ObjectMetadata obj = createTestMetadata("test-obj", "Package1", "1.0.0", ObjectStatus.DRAFT);
        registryHelper.updateIndex("test-obj", obj);
        
        // Test existing object
        assertTrue(registryHelper.exists("test-obj"));
        
        // Remove and test again
        registryHelper.removeFromIndex("test-obj");
        assertFalse(registryHelper.exists("test-obj"));
    }

    @Test
    void testExistsValidation() {
        // Test null object ID validation
        assertThrows(NullPointerException.class, () -> 
            registryHelper.exists(null));
    }

    @Test
    void testRebuildIndex() throws IOException {
        // Add some objects
        ObjectMetadata obj1 = createTestMetadata("obj1", "Package1", "1.0.0", ObjectStatus.DRAFT);
        ObjectMetadata obj2 = createTestMetadata("obj2", "Package2", "1.0.0", ObjectStatus.APPROVED);
        registryHelper.updateIndex("obj1", obj1);
        registryHelper.updateIndex("obj2", obj2);
        
        assertEquals(2, registryHelper.getObjectCount());
        
        // Rebuild index (for in-memory this is a no-op, but shouldn't fail)
        assertDoesNotThrow(() -> registryHelper.rebuildIndex());
        
        // Data should still be there (BasicRegistryHelper keeps existing data)
        assertEquals(2, registryHelper.getObjectCount());
    }

    @Test
    void testGetRegistryStatistics() throws IOException {
        // Add test data
        ObjectMetadata draft = createTestMetadata("draft-obj", "Package1", "1.0.0", ObjectStatus.DRAFT);
        draft.setRole("draft");
        ObjectMetadata approved = createTestMetadata("approved-obj", "Package2", "1.0.0", ObjectStatus.APPROVED);
        approved.setRole("approved");
        
        registryHelper.updateIndex("draft-obj", draft);
        registryHelper.updateIndex("approved-obj", approved);
        
        // Get statistics
        Object stats = registryHelper.getRegistryStatistics();
        assertNotNull(stats);
        
        // Should be a Map with expected structure
        assertTrue(stats instanceof java.util.Map);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> statsMap = (java.util.Map<String, Object>) stats;
        
        assertEquals("basic-memory", statsMap.get("registryType"));
        assertEquals(2L, statsMap.get("totalObjects"));
        assertTrue(statsMap.get("initialized") instanceof Boolean);
        assertTrue(statsMap.containsKey("statusDistribution"));
        assertTrue(statsMap.containsKey("roleDistribution"));
    }

    @Test
    void testGetRegistryType() {
        assertEquals("basic-memory", registryHelper.getRegistryType());
    }

    @Test
    void testSearchObjectIds() throws IOException {
        // Add test objects
        ObjectMetadata obj1 = createTestMetadata("obj1", "Package1", "1.0.0", ObjectStatus.DRAFT);
        ObjectMetadata obj2 = createTestMetadata("obj2", "Package2", "1.0.0", ObjectStatus.APPROVED);
        
        registryHelper.updateIndex("obj1", obj1);
        registryHelper.updateIndex("obj2", obj2);
        
        // Basic implementation returns all IDs (query parsing not implemented)
        List<String> results = registryHelper.searchObjectIds("any query", 10);
        assertEquals(2, results.size());
        
        // Test max results limit
        List<String> limitedResults = registryHelper.searchObjectIds("any query", 1);
        assertEquals(1, limitedResults.size());
    }

    @Test
    void testGetAllMetadata() throws IOException {
        // Add test objects
        ObjectMetadata obj1 = createTestMetadata("obj1", "Package1", "1.0.0", ObjectStatus.DRAFT);
        ObjectMetadata obj2 = createTestMetadata("obj2", "Package2", "1.0.0", ObjectStatus.APPROVED);
        
        registryHelper.updateIndex("obj1", obj1);
        registryHelper.updateIndex("obj2", obj2);
        
        // Get all metadata
        Set<ObjectMetadata> allMetadata = registryHelper.getAllMetadata();
        assertEquals(2, allMetadata.size());
        
        // Verify metadata is included
        boolean foundObj1 = allMetadata.stream().anyMatch(m -> "obj1".equals(m.getObjectId()));
        boolean foundObj2 = allMetadata.stream().anyMatch(m -> "obj2".equals(m.getObjectId()));
        assertTrue(foundObj1);
        assertTrue(foundObj2);
    }

    @Test
    void testLifecycleManagement() throws Exception {
        // Add some data
        ObjectMetadata obj = createTestMetadata("test-obj", "Package1", "1.0.0", ObjectStatus.DRAFT);
        registryHelper.updateIndex("test-obj", obj);
        assertEquals(1, registryHelper.getObjectCount());
        
        // Close helper
        registryHelper.close();
        
        // After close, should be cleared
        assertEquals(0, registryHelper.getObjectCount());
        assertFalse(registryHelper.exists("test-obj"));
    }

    private ObjectMetadata createTestMetadata(String objectId, String objectName, String version, ObjectStatus status) {
        ObjectMetadata metadata = factory.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName(objectName);
        metadata.setVersion(version);
        metadata.setStatus(status);
        metadata.setUploadTime(Instant.now());
        metadata.setObjectType("EPackage");
        return metadata;
    }
}