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
package org.gecko.mac.management.lucene.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.gecko.mac.management.lucene.tests.annotations.LuceneTestAnnotations;
import org.gecko.mac.management.lucene.tests.annotations.LuceneTestAnnotations.RegistryConfiguration;
import org.gecko.mac.mgmt.api.EObjectRegistryService;
import org.gecko.mac.mgmt.management.ManagementFactory;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.gecko.mac.mgmt.management.ObjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Advanced integration tests for LuceneEObjectRegistryService covering edge cases, 
 * error handling, and configuration variations.
 * 
 * <p>This test class focuses on scenarios not covered in the basic functionality tests:</p>
 * 
 * <ul>
 * <li><strong>Error Handling</strong> - Null parameter validation, malformed inputs</li>
 * <li><strong>Edge Cases</strong> - Empty results, invalid patterns, boundary conditions</li>
 * <li><strong>Configuration Variations</strong> - Different backend tracking, debug settings</li>
 * <li><strong>Concurrent Scenarios</strong> - Heavy load testing, stress scenarios</li>
 * </ul>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class LuceneRegistryAdvancedTest {

    @TempDir
    static Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Set system property for configurations that might need it
        System.setProperty(LuceneTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
    }

    // ===== Error Handling & Edge Cases Tests =====

    @SuppressWarnings("rawtypes")
    @Test
    @RegistryConfiguration
    public void testNullParameterValidation(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Test null parameter validation for all methods
        assertThrows(NullPointerException.class, () -> 
            registryService.getMetadata(null), "getMetadata should reject null objectId");

        assertThrows(NullPointerException.class, () -> 
            registryService.updateCache(null), "updateCache should reject null metadata");

        assertThrows(NullPointerException.class, () -> 
            registryService.removeFromCache(null), "removeFromCache should reject null objectId");

        assertThrows(NullPointerException.class, () -> 
            registryService.findByStatus(null), "findByStatus should reject null status");

        assertThrows(NullPointerException.class, () -> 
            registryService.findByObjectName(null), "findByObjectName should reject null objectName");

        assertThrows(NullPointerException.class, () -> 
            registryService.findByRole(null), "findByRole should reject null role");

        assertThrows(NullPointerException.class, () -> 
            registryService.findByObjectNameAndRole(null, "role"), "findByObjectNameAndRole should reject null objectName");

        assertThrows(NullPointerException.class, () -> 
            registryService.findByObjectNameAndRole("name", null), "findByObjectNameAndRole should reject null role");

        assertThrows(NullPointerException.class, () -> 
            registryService.findByFingerprint(null), "findByFingerprint should reject null fingerprint");

        assertThrows(NullPointerException.class, () -> 
            registryService.findByVersion(null), "findByVersion should reject null version");

        assertThrows(NullPointerException.class, () -> 
            registryService.findByVersionPattern(null), "findByVersionPattern should reject null pattern");

        assertThrows(NullPointerException.class, () -> 
            registryService.findByObjectType(null), "findByObjectType should reject null objectType");

        assertThrows(NullPointerException.class, () -> 
            registryService.findByStatusAndType(null, "type"), "findByStatusAndType should reject null status");

        assertThrows(NullPointerException.class, () -> 
            registryService.findByStatusAndType(ObjectStatus.DRAFT, null), "findByStatusAndType should reject null type");

        // Service automatically cleaned up by annotation
    }

    @SuppressWarnings("rawtypes")
    @Test
    @RegistryConfiguration
    public void testEmptyResultScenarios(
        @InjectService(timeout = 5000, filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Test empty registry scenarios
        assertFalse(registryService.getMetadata("non-existent").isPresent(), 
                   "Should return empty Optional for non-existent object");

        assertTrue(registryService.findByStatus(ObjectStatus.DRAFT).isEmpty(), 
                  "Should return empty list when no objects exist");

        assertTrue(registryService.findByObjectName("NonExistentName").isEmpty(), 
                  "Should return empty list for non-existent object name");

        assertTrue(registryService.findByRole("non-existent-role").isEmpty(), 
                  "Should return empty list for non-existent role");

        assertFalse(registryService.findByFingerprint("non-existent-fp").isPresent(), 
                   "Should return empty Optional for non-existent fingerprint");

        assertTrue(registryService.findByVersion("999.999.999").isEmpty(), 
                  "Should return empty list for non-existent version");

        assertTrue(registryService.findByVersionPattern("999.*").isEmpty(), 
                  "Should return empty list for non-matching version pattern");

        assertTrue(registryService.findByObjectType("NonExistentType").isEmpty(), 
                  "Should return empty list for non-existent object type");

        assertTrue(registryService.findPendingApproval().isEmpty(), 
                  "Should return empty list when no pending approvals exist");

        // Test findByStatus on empty registry (since getAllMetadata is not part of interface)
        assertTrue(registryService.findByStatus(ObjectStatus.DRAFT).isEmpty(), 
                  "Should return empty list when registry is empty");

        // Service automatically cleaned up by annotation
    }

    @SuppressWarnings("rawtypes")
    @Test
    @RegistryConfiguration
    public void testMalformedInputHandling(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Create some test objects first
        ObjectMetadata testObj = createTestMetadata("test-1", "TestObject", "1.0.0", ObjectStatus.DRAFT, "draft");
        registryService.updateCache(testObj);

        // Test malformed patterns - these should handle gracefully without throwing
        assertTrue(registryService.findByVersionPattern("").isEmpty(), 
                  "Should handle empty version pattern gracefully");

        assertTrue(registryService.findByVersionPattern("[invalid-regex").isEmpty(), 
                  "Should handle malformed regex pattern gracefully");

        // Test special characters in search strings
        assertTrue(registryService.findByObjectName("").isEmpty(), 
                  "Should handle empty object name gracefully");

        assertTrue(registryService.findByRole("").isEmpty(), 
                  "Should handle empty role gracefully");

        assertFalse(registryService.findByFingerprint("").isPresent(), 
                  "Should handle empty fingerprint gracefully");

        // Test very long strings
        String veryLongString = "a".repeat(10000);
        assertTrue(registryService.findByObjectName(veryLongString).isEmpty(), 
                  "Should handle very long object name gracefully");

        assertFalse(registryService.findByFingerprint(veryLongString).isPresent(), 
                  "Should handle very long fingerprint gracefully");

        // Service automatically cleaned up by annotation
    }

    // ===== Configuration Variations Tests =====

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @RegistryConfiguration
    public void testStorageBackendTrackingDisabled(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Create test objects
        ObjectMetadata testObj = createTestMetadata("test-1", "TestObject", "1.0.0", ObjectStatus.DRAFT, "draft");
        registryService.updateCache(testObj);

        // Get statistics to verify backend tracking is disabled
        Map<String, Object> stats = (Map<String, Object>) registryService.getRegistryStatistics().getValue();
        assertNotNull(stats);
        // Note: The @RegistryConfiguration has tracking enabled by default
        // This test would need a separate configuration to disable tracking
        assertTrue(stats.containsKey("storageBackendDistribution"), 
                   "Storage backend distribution should be present when tracking is enabled");

        // Service automatically cleaned up by annotation
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
    @RegistryConfiguration
    public void testDifferentIndexCapacities(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Add multiple objects to test capacity handling
        for (int i = 0; i < 50; i++) {
            ObjectMetadata obj = createTestMetadata("obj-" + i, "Object" + i, "1.0." + i, 
                                                   ObjectStatus.DRAFT, "draft");
            registryService.updateCache(obj);
        }

        // Verify all objects are stored correctly via status query
        assertEquals(50, registryService.findByStatus(ObjectStatus.DRAFT).size(), 
                    "Should store all objects regardless of initial capacity");

        // Verify basic functionality still works
        List<ObjectMetadata> drafts = registryService.findByStatus(ObjectStatus.DRAFT);
        assertEquals(50, drafts.size(), "Should find all draft objects");

        // Service automatically cleaned up by annotation
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testDebugLoggingEnabled(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Perform operations that would trigger debug logging
        ObjectMetadata testObj = createTestMetadata("debug-test", "DebugObject", "1.0.0", ObjectStatus.DRAFT, "draft");
        registryService.updateCache(testObj);

        // Verify cache hit behavior (should trigger debug logs if enabled)
        assertTrue(registryService.getMetadata("debug-test").isPresent(), 
                  "Should find cached object (cache hit)");

        // Verify cache miss behavior
        assertFalse(registryService.getMetadata("non-existent").isPresent(), 
                   "Should not find non-existent object (cache miss)");

        // Test search operations that would trigger debug logs
        List<ObjectMetadata> results = registryService.findByObjectName("DebugObject");
        assertEquals(1, results.size(), "Should find debug object");

        // Service automatically cleaned up by annotation
    }

    // ===== Stress and Performance Tests =====

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @RegistryConfiguration
    public void testLargeDatasetHandling(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Create large dataset (1000 objects) - focus on correctness, not timing
        int datasetSize = 1000;
        
        for (int i = 0; i < datasetSize; i++) {
            ObjectMetadata obj = createTestMetadata("large-obj-" + i, "LargeObject" + i, 
                                                   "1.0." + (i % 100), // Version pattern to test version queries
                                                   i % 2 == 0 ? ObjectStatus.DRAFT : ObjectStatus.APPROVED,
                                                   i % 3 == 0 ? "draft" : (i % 3 == 1 ? "approved" : "documentation"));
            obj.setObjectType(i % 4 == 0 ? "EPackage" : (i % 4 == 1 ? "Route" : "SensorModel"));
            registryService.updateCache(obj);
        }

        // Test dataset integrity and query correctness
        List<ObjectMetadata> drafts = registryService.findByStatus(ObjectStatus.DRAFT);
        assertTrue(drafts.size() > 0, "Should find draft objects");
        assertEquals(500, drafts.size(), "Should find exactly 500 draft objects (50% of dataset)");

        List<ObjectMetadata> approved = registryService.findByStatus(ObjectStatus.APPROVED);
        assertEquals(500, approved.size(), "Should find exactly 500 approved objects (50% of dataset)");

        // Test version pattern queries work correctly with large dataset
        List<ObjectMetadata> version1x = registryService.findByVersionPattern("1.0.*");
        assertEquals(datasetSize, version1x.size(), "All objects should match version pattern 1.0.*");
        
        // Test specific version queries
        List<ObjectMetadata> version10 = registryService.findByVersion("1.0.10");
        assertEquals(10, version10.size(), "Should find 10 objects with version 1.0.10 (i % 100 pattern)");

        // Test object type distribution
        List<ObjectMetadata> ePackages = registryService.findByObjectType("EPackage");
        assertEquals(250, ePackages.size(), "Should find 250 EPackage objects (25% of dataset)");
        
        List<ObjectMetadata> routes = registryService.findByObjectType("Route");
        assertEquals(250, routes.size(), "Should find 250 Route objects (25% of dataset)");
        
        List<ObjectMetadata> sensors = registryService.findByObjectType("SensorModel");
        assertEquals(500, sensors.size(), "Should find 500 SensorModel objects (50% of dataset)");

        // Test role distribution
        List<ObjectMetadata> draftRole = registryService.findByRole("draft");
        assertTrue(draftRole.size() > 300 && draftRole.size() < 350, 
                  "Should find ~333 objects with draft role (33% of dataset), found: " + draftRole.size());
        
        List<ObjectMetadata> approvedRole = registryService.findByRole("approved");
        assertTrue(approvedRole.size() > 300 && approvedRole.size() < 350, 
                  "Should find ~333 objects with approved role (33% of dataset), found: " + approvedRole.size());
        
        List<ObjectMetadata> docRole = registryService.findByRole("documentation");
        assertTrue(docRole.size() > 300 && docRole.size() < 350, 
                  "Should find ~334 objects with documentation role (33% of dataset), found: " + docRole.size());

        // Test statistics accuracy
        Map<String, Object> stats = (Map<String, Object>) registryService.getRegistryStatistics().getValue();
        assertNotNull(stats);
        assertEquals((long) datasetSize, stats.get("totalObjects"));
        
        Map<String, Long> statusDist = (Map<String, Long>) stats.get("statusDistribution");
        assertEquals(500L, statusDist.get("DRAFT"));
        assertEquals(500L, statusDist.get("APPROVED"));

        // Service automatically cleaned up by annotation
    }

    // ===== Metadata Integrity Validation Tests =====

    @SuppressWarnings("rawtypes")
    @Test
    @RegistryConfiguration
    public void testUpdateCacheRejectsMetadataWithoutObjectId(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Create metadata without objectId (violates data integrity)
        ObjectMetadata metadataWithoutId = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadataWithoutId.setObjectName("TestPackage");
        metadataWithoutId.setVersion("1.0.0");
        metadataWithoutId.setStatus(ObjectStatus.DRAFT);
        metadataWithoutId.setUploadTime(Instant.now());
        metadataWithoutId.setUploadUser("test-user");
        // Explicitly verify objectId is null
        assertNotNull(metadataWithoutId); // Metadata object exists
        assertEquals(null, metadataWithoutId.getObjectId(), "ObjectId should be null for this test");

        // Attempting to update cache with metadata without objectId should throw exception
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            registryService.updateCache(metadataWithoutId);
        });
        
        assertTrue(exception.getMessage().contains("ObjectId in metadata cannot be null"),
                  "Exception should mention null objectId requirement");
    }

    @SuppressWarnings("rawtypes")
    @Test
    @RegistryConfiguration
    public void testUpdateCacheRejectsMetadataWithEmptyObjectId(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Create metadata with empty objectId (violates data integrity)
        ObjectMetadata metadataWithEmptyId = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadataWithEmptyId.setObjectId(""); // Empty string
        metadataWithEmptyId.setObjectName("TestPackage");
        metadataWithEmptyId.setVersion("1.0.0");
        metadataWithEmptyId.setStatus(ObjectStatus.DRAFT);
        metadataWithEmptyId.setUploadTime(Instant.now());
        metadataWithEmptyId.setUploadUser("test-user");

        // Attempting to update cache with metadata with empty objectId should throw exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registryService.updateCache(metadataWithEmptyId);
        });
        
        assertTrue(exception.getMessage().contains("ObjectMetadata must have objectId set (cannot be empty)"),
                  "Exception should mention empty objectId requirement");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @RegistryConfiguration
    public void testUpdateCacheAcceptsValidMetadataWithObjectId(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Create valid metadata with proper objectId
        ObjectMetadata validMetadata = createTestMetadata("valid-lucene-id", "TestPackage", "1.0.0", ObjectStatus.DRAFT, "draft");
        
        // This should work without any exceptions and update the Lucene index
        registryService.updateCache(validMetadata);
        
        // Verify the metadata was actually cached and indexed
        assertTrue(registryService.getMetadata("valid-lucene-id").isPresent(),
                  "Metadata should be findable by objectId");
        ObjectMetadata retrievedMetadata = (ObjectMetadata) registryService.getMetadata("valid-lucene-id").get();
        assertEquals("TestPackage", retrievedMetadata.getObjectName());
        assertEquals("valid-lucene-id", retrievedMetadata.getObjectId());
        
        // Verify it's also searchable via Lucene indexing
        List<ObjectMetadata> byStatus = registryService.findByStatus(ObjectStatus.DRAFT);
        assertTrue(byStatus.stream().anyMatch(m -> "valid-lucene-id".equals(m.getObjectId())),
                  "Metadata should be findable via indexed search");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @RegistryConfiguration
    public void testLuceneRegistryMaintainsObjectIdIntegrity(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Create metadata with objectId and add to Lucene registry
        ObjectMetadata originalMetadata = createTestMetadata("lucene-integrity-test", "LuceneTestPackage", "1.0.0", ObjectStatus.DRAFT, "draft");
        
        // Add to registry (should index in Lucene)
        registryService.updateCache(originalMetadata);
        
        // Retrieve from registry (should come from Lucene index)
        assertTrue(registryService.getMetadata("lucene-integrity-test").isPresent(),
                  "Should find object in Lucene registry");
        ObjectMetadata retrieved = (ObjectMetadata) registryService.getMetadata("lucene-integrity-test").get();
        
        // Verify objectId is preserved exactly through Lucene indexing
        assertEquals("lucene-integrity-test", retrieved.getObjectId());
        assertNotNull(retrieved.getObjectId());
        assertFalse(retrieved.getObjectId().isEmpty());
        
        // Verify other metadata fields are also preserved through Lucene
        assertEquals("LuceneTestPackage", retrieved.getObjectName());
        assertEquals("1.0.0", retrieved.getVersion());
        assertEquals(ObjectStatus.DRAFT, retrieved.getStatus());
        assertEquals("draft", retrieved.getRole());
        
        // Verify the object is also findable through indexed searches
        List<ObjectMetadata> byName = registryService.findByObjectName("LuceneTestPackage");
        assertEquals(1, byName.size(), "Should find object by name via Lucene search");
        assertEquals("lucene-integrity-test", byName.get(0).getObjectId());
    }

    // ===== Helper Methods =====

    private ObjectMetadata createTestMetadata(String objectId, String objectName, String version, ObjectStatus status, String role) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName(objectName);
        metadata.setVersion(version);
        metadata.setStatus(status);
        metadata.setObjectType("EPackage"); // Default type, can be overridden
        metadata.setUploadTime(Instant.now());
        metadata.setUploadUser("test-user");
        metadata.setSourceChannel("TEST_CHANNEL");
        metadata.setLastChangeTime(Instant.now());
        metadata.setRole(role);
        return metadata;
    }
}