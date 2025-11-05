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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
 * OSGi integration test for SharedLuceneRegistryService.
 * 
 * <p>This test validates that the SharedLuceneRegistryService properly provides
 * a centralized metadata registry that can be used by multiple storage services
 * instead of each service maintaining its own registry.</p>
 * 
 * <h3>Test Coverage</h3>
 * <ul>
 * <li><strong>Service Registration</strong> - Verifies shared registry service is properly registered</li>
 * <li><strong>Independent Configuration</strong> - Tests service with own workspace folder</li>
 * <li><strong>Basic Registry Functions</strong> - Validates standard EObjectRegistryService methods</li>
 * <li><strong>Cross-Storage Queries</strong> - Tests findByStorageBackend, findByStorageRole methods</li>
 * <li><strong>Storage Backend Tracking</strong> - Verifies storage backend identification works correctly</li>
 * <li><strong>Shared Registry Statistics</strong> - Tests statistics include storage distribution</li>
 * <li><strong>Concurrent Access</strong> - Validates thread-safe operations</li>
 * </ul>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class LuceneRegistryServiceTest {

    @TempDir
    static Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Set system property for @RegistryConfiguration annotation
        System.setProperty(LuceneTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
    }

    @SuppressWarnings("rawtypes")
    @Test
    @RegistryConfiguration
    public void testSharedRegistryServiceRegistration(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Service should be available due to @RegistryConfiguration annotation
        assertNotNull(registryService, "Shared registry service should be available");

        // Verify the registry service is a LuceneEObjectRegistryService
        assertEquals("org.gecko.mac.management.lucene.service.LuceneEObjectRegistryService", 
                    registryService.getClass().getName(),
                    "Registry should be LuceneEObjectRegistryService implementation");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testBasicRegistryFunctionality(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Create test objects with role information
        ObjectMetadata draftMetadata = createTestMetadata("test-package-1", "TestPackage", "1.0.0", ObjectStatus.DRAFT, "draft");
        enhanceWithStorageInfo(draftMetadata, "file", "draft", "instance-01");

        ObjectMetadata approvedMetadata = createTestMetadata("test-package-2", "TestPackage2", "2.0.0", ObjectStatus.APPROVED, "approved");
        enhanceWithStorageInfo(approvedMetadata, "minio", "approved", "instance-02");

        // Update registry cache (simulating storage service updates)
        registryService.updateCache(draftMetadata);
        registryService.updateCache(approvedMetadata);

        // Test registry lookup
        Optional<ObjectMetadata> retrieved = registryService.getMetadata("test-package-1");
        assertTrue(retrieved.isPresent(), "Metadata should be found in registry");
        assertEquals("TestPackage", retrieved.get().getObjectName());

        // Test status search
        List<ObjectMetadata> draftObjects = registryService.findByStatus(ObjectStatus.DRAFT);
        assertEquals(1, draftObjects.size(), "Should find one DRAFT object");
        assertEquals("test-package-1", draftObjects.get(0).getObjectId());

        // Test type search
        List<ObjectMetadata> packageObjects = registryService.findByObjectType("EPackage");
        assertEquals(2, packageObjects.size(), "Should find two EPackage objects");

        // Test status and type combination
        List<ObjectMetadata> draftPackages = registryService.findByStatusAndType(ObjectStatus.DRAFT, "EPackage");
        assertEquals(1, draftPackages.size(), "Should find one DRAFT EPackage");

        // Service is automatically cleaned up by test annotation
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testCrossStorageQueries(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {
        
        // Verify this is indeed the shared registry implementation for cross-storage testing
        assertTrue(registryService.getClass().getName().contains("LuceneEObjectRegistryService"),
                  "Should be LuceneEObjectRegistryService for cross-storage testing");

        // Create test objects from different storage backends with roles
        ObjectMetadata fileDraftMetadata = createTestMetadata("file-draft-1", "FileDraft", "1.0.0", ObjectStatus.DRAFT, "draft");
        enhanceWithStorageInfo(fileDraftMetadata, "file", "draft", null);

        ObjectMetadata fileApprovedMetadata = createTestMetadata("file-approved-1", "FileApproved", "1.0.0", ObjectStatus.APPROVED, "approved");
        enhanceWithStorageInfo(fileApprovedMetadata, "file", "approved", null);

        ObjectMetadata minioDocMetadata = createTestMetadata("minio-doc-1", "MinIODoc", "1.0.0", ObjectStatus.APPROVED, "documentation");
        enhanceWithStorageInfo(minioDocMetadata, "minio", "documentation", null);

        // Update registry cache
        registryService.updateCache(fileDraftMetadata);
        registryService.updateCache(fileApprovedMetadata);
        registryService.updateCache(minioDocMetadata);

        // Test cross-storage queries using standard interface methods
        // Find objects by role (replaces findAllDrafts, findAllApproved, findAllDocumentation)
        List<ObjectMetadata> draftObjects = registryService.findByRole("draft");
        assertEquals(1, draftObjects.size(), "Should find 1 draft object across all backends");

        List<ObjectMetadata> approvedObjects = registryService.findByStatus(ObjectStatus.APPROVED);
        assertEquals(2, approvedObjects.size(), "Should find 2 approved objects across all backends");

        List<ObjectMetadata> documentationObjects = registryService.findByRole("documentation");
        assertEquals(1, documentationObjects.size(), "Should find 1 documentation object");

        // Test specific object lookups
        Optional<ObjectMetadata> fileDraft = registryService.findByObjectNameAndRole("FileDraft", "draft");
        assertTrue(fileDraft.isPresent(), "Should find file draft object");
        assertEquals("file-draft-1", fileDraft.get().getObjectId());

        // Test object name queries
        List<ObjectMetadata> fileApprovedObjects = registryService.findByObjectName("FileApproved");
        assertEquals(1, fileApprovedObjects.size(), "Should find 1 FileApproved object");
        
        List<ObjectMetadata> minioDocObjects = registryService.findByObjectName("MinIODoc");
        assertEquals(1, minioDocObjects.size(), "Should find 1 MinIODoc object");

        // Service is automatically cleaned up by test annotation
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @RegistryConfiguration
    public void testSharedRegistryStatistics(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Store multiple test objects with different storage backends
        for (int i = 1; i <= 3; i++) {
            // Vary storage backends
            String backend = (i == 1) ? "file" : "minio";
            String role = (i == 1) ? "draft" : (i == 2) ? "approved" : "documentation";
            
            ObjectMetadata metadata = createTestMetadata("package-" + i, "Package" + i, "1." + i + ".0", 
                                                         i == 1 ? ObjectStatus.DRAFT : ObjectStatus.APPROVED, role);
            enhanceWithStorageInfo(metadata, backend, role, "instance-" + i);
            
            registryService.updateCache(metadata);
        }

        // Get statistics
        Map<String, Object> stats = (Map<String, Object>) registryService.getRegistryStatistics().getValue();
        assertNotNull(stats, "Statistics should not be null");

        // Check basic counts
        assertEquals(3L, stats.get("totalObjects"), "Should have 3 total objects");

        // Check shared registry specific fields
        assertEquals("shared", stats.get("registryType"), "Should be shared registry");
        assertTrue((Boolean) stats.get("storageBackendTracking"), "Storage backend tracking should be enabled");
        assertTrue(stats.containsKey("registryWorkspace"), "Should have registry workspace path");

        // Check status distribution
        Map<String, Long> statusCounts = (Map<String, Long>) stats.get("statusDistribution");
        assertNotNull(statusCounts, "Status counts should not be null");
        assertEquals(1L, statusCounts.get("DRAFT"), "Should have 1 DRAFT object");
        assertEquals(2L, statusCounts.get("APPROVED"), "Should have 2 APPROVED objects");

        // Check storage backend distribution
        Map<String, Long> backendCounts = (Map<String, Long>) stats.get("storageBackendDistribution");
        assertNotNull(backendCounts, "Backend counts should not be null");
        assertTrue(backendCounts.containsKey("file"), "Should track file backend");
        assertTrue(backendCounts.containsKey("minio"), "Should track minio backend");

        // Service is automatically cleaned up by test annotation
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testStorageBackendTracking(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Create metadata with role information
        ObjectMetadata metadata = createTestMetadata("test-package", "TestPackage", "1.0.0", ObjectStatus.DRAFT, "draft");
        
        // Enhance with storage backend information
        enhanceWithStorageInfo(metadata, "file", "draft", "test-instance");
        
        // Verify storage backend properties were added
        assertEquals("file", extractStorageBackend(metadata));
        assertEquals("draft", metadata.getRole());
        assertEquals("test-instance", extractStorageInstance(metadata));

        // Update registry
        registryService.updateCache(metadata);

        // Retrieve and verify storage backend information is preserved
        Optional<ObjectMetadata> retrieved = registryService.getMetadata("test-package");
        assertTrue(retrieved.isPresent());
        assertEquals("file", extractStorageBackend(retrieved.get()));
        assertEquals("draft", retrieved.get().getRole());

        // Service is automatically cleaned up by test annotation
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testConcurrentAccess(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Test concurrent updates from multiple threads (simulating multiple storage services)
        int numThreads = 5;
        int objectsPerThread = 10;
        Thread[] threads = new Thread[numThreads];

        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            threads[t] = new Thread(() -> {
                for (int i = 0; i < objectsPerThread; i++) {
                    String objectId = "thread-" + threadId + "-object-" + i;
                    ObjectMetadata metadata = createTestMetadata(objectId, "Object" + i, "1.0." + i, ObjectStatus.DRAFT, "draft");
                    enhanceWithStorageInfo(metadata, "file", "draft", "thread-" + threadId);
                    
                    registryService.updateCache(metadata);
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(5000);
        }

        // Verify all objects were stored correctly
        List<ObjectMetadata> allObjects = registryService.findByStatus(ObjectStatus.DRAFT);
        assertEquals(numThreads * objectsPerThread, allObjects.size(), 
                    "Should have stored all objects from concurrent threads");

        // Verify each thread's objects are present
        for (int t = 0; t < numThreads; t++) {
            for (int i = 0; i < objectsPerThread; i++) {
                String objectId = "thread-" + t + "-object-" + i;
                Optional<ObjectMetadata> metadata = registryService.getMetadata(objectId);
                assertTrue(metadata.isPresent(), "Object " + objectId + " should be present");
            }
        }

        // Service is automatically cleaned up by test annotation
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testRoleBasedQueries(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {

        // Create test objects with different roles
        ObjectMetadata draftMetadata = createTestMetadata("obj-1", "SensorModel", "1.0.0", ObjectStatus.DRAFT, "draft");
        ObjectMetadata approvedMetadata = createTestMetadata("obj-2", "SensorModel", "1.0.0", ObjectStatus.APPROVED, "approved");
        ObjectMetadata documentationMetadata = createTestMetadata("obj-3", "SensorModel", "1.0.0", ObjectStatus.APPROVED, "documentation");
        
        // Same objectName, different roles - should be separate objects
        ObjectMetadata packageDraft = createTestMetadata("pkg-draft", "CommonPackage", "2.0.0", ObjectStatus.DRAFT, "draft");
        ObjectMetadata packageApproved = createTestMetadata("pkg-approved", "CommonPackage", "2.0.0", ObjectStatus.APPROVED, "approved");

        // Update registry cache
        registryService.updateCache(draftMetadata);
        registryService.updateCache(approvedMetadata);
        registryService.updateCache(documentationMetadata);
        registryService.updateCache(packageDraft);
        registryService.updateCache(packageApproved);

        // Test role-based queries via standard interface
        List<ObjectMetadata> drafts = registryService.findByRole("draft");
        assertEquals(2, drafts.size(), "Should find 2 draft objects");
        assertTrue(drafts.stream().anyMatch(m -> "obj-1".equals(m.getObjectId())));
        assertTrue(drafts.stream().anyMatch(m -> "pkg-draft".equals(m.getObjectId())));

        List<ObjectMetadata> approved = registryService.findByRole("approved");
        assertEquals(2, approved.size(), "Should find 2 approved objects");
        assertTrue(approved.stream().anyMatch(m -> "obj-2".equals(m.getObjectId())));
        assertTrue(approved.stream().anyMatch(m -> "pkg-approved".equals(m.getObjectId())));

        List<ObjectMetadata> documentation = registryService.findByRole("documentation");
        assertEquals(1, documentation.size(), "Should find 1 documentation object");
        assertEquals("obj-3", documentation.get(0).getObjectId());

        // Test objectName-based queries
        List<ObjectMetadata> sensorModels = registryService.findByObjectName("SensorModel");
        assertEquals(3, sensorModels.size(), "Should find 3 SensorModel objects with different roles");

        List<ObjectMetadata> commonPackages = registryService.findByObjectName("CommonPackage");
        assertEquals(2, commonPackages.size(), "Should find 2 CommonPackage objects with different roles");

        // Test combined objectName and role queries
        Optional<ObjectMetadata> sensorDraft = registryService.findByObjectNameAndRole("SensorModel", "draft");
        assertTrue(sensorDraft.isPresent(), "Should find SensorModel draft");
        assertEquals("obj-1", sensorDraft.get().getObjectId());

        Optional<ObjectMetadata> sensorApproved = registryService.findByObjectNameAndRole("SensorModel", "approved");
        assertTrue(sensorApproved.isPresent(), "Should find SensorModel approved");
        assertEquals("obj-2", sensorApproved.get().getObjectId());

        Optional<ObjectMetadata> packageDoc = registryService.findByObjectNameAndRole("CommonPackage", "documentation");
        assertTrue(packageDoc.isEmpty(), "Should not find CommonPackage documentation (doesn't exist)");

        // Test role queries return correct role values
        for (ObjectMetadata metadata : drafts) {
            assertEquals("draft", metadata.getRole(), "All draft objects should have role 'draft'");
        }

        for (ObjectMetadata metadata : approved) {
            assertEquals("approved", metadata.getRole(), "All approved objects should have role 'approved'");
        }

        for (ObjectMetadata metadata : documentation) {
            assertEquals("documentation", metadata.getRole(), "All documentation objects should have role 'documentation'");
        }

        // Service is automatically cleaned up by test annotation
    }

    private ObjectMetadata createTestMetadata(String objectId, String objectName, String version, ObjectStatus status, String role) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName(objectName);
        metadata.setVersion(version);
        metadata.setStatus(status);
        metadata.setObjectType("EPackage");
        metadata.setUploadTime(Instant.now());
        metadata.setUploadUser("test-user");
        metadata.setSourceChannel("TEST_CHANNEL");
        metadata.setLastChangeTime(Instant.now());
        metadata.setRole(role);
        return metadata;
    }
    
    /**
     * Helper method to enhance metadata with storage backend information.
     * Replaces StorageBackendIdentifier.enhanceWithStorageInfo().
     */
    private void enhanceWithStorageInfo(ObjectMetadata metadata, String backend, String role, String instance) {
        // Set the role directly on the metadata
        metadata.setRole(role);
        
        // Store backend and instance in properties
        if (backend != null) {
            metadata.getProperties().put("storage.backend", backend);
        }
        if (instance != null) {
            metadata.getProperties().put("storage.instance", instance);
        }
    }
    
    /**
     * Helper method to extract storage backend from metadata properties.
     * Replaces StorageBackendIdentifier.extractStorageBackend().
     */
    private String extractStorageBackend(ObjectMetadata metadata) {
        if (metadata.getProperties() != null) {
            Object backend = metadata.getProperties().get("storage.backend");
            return backend != null ? backend.toString() : "unknown";
        }
        return "unknown";
    }
    
    /**
     * Helper method to extract storage instance from metadata properties.
     * Replaces StorageBackendIdentifier.extractStorageInstance().
     */
    private String extractStorageInstance(ObjectMetadata metadata) {
        if (metadata.getProperties() != null) {
            Object instance = metadata.getProperties().get("storage.instance");
            return instance != null ? instance.toString() : null;
        }
        return null;
    }
}