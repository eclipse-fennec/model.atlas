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

import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for AbstractRegistryHelper interface contract.
 * 
 * <p>These tests validate the abstract registry helper interface through
 * a concrete test implementation, ensuring all registry operations work
 * correctly and enforce proper input validation.</p>
 * 
 * <h3>Test Coverage</h3>
 * <ul>
 * <li><strong>Interface Contract</strong> - Tests all abstract methods are properly defined</li>
 * <li><strong>Initialization</strong> - Tests registry startup and readiness</li>
 * <li><strong>Index Operations</strong> - updateIndex, removeFromIndex with validation</li>
 * <li><strong>Search Methods</strong> - All find* methods with comprehensive scenarios</li>
 * <li><strong>Statistics</strong> - Registry statistics and type information</li>
 * <li><strong>Lifecycle</strong> - AutoCloseable contract and resource cleanup</li>
 * <li><strong>Error Handling</strong> - Input validation and IOException handling</li>
 * </ul>
 * 
 * <h3>Implementation Strategy</h3>
 * <p>Uses a concrete test implementation of AbstractRegistryHelper to validate
 * the interface contract without depending on specific registry implementations.</p>
 */
@ExtendWith(MockitoExtension.class)
class AbstractRegistryHelperTest {

    private TestRegistryHelper registryHelper;
    private ManagementFactory factory;

    @BeforeEach
    void setUp() throws IOException {
        registryHelper = new TestRegistryHelper();
        factory = ManagementFactory.eINSTANCE;
        registryHelper.initialize();
    }

    @Test
    void testInitializationContract() throws Exception {
        try (TestRegistryHelper newHelper = new TestRegistryHelper()) {
            // Should be able to initialize without errors
            assertDoesNotThrow(() -> newHelper.initialize());
            
            // Should be marked as initialized
            assertTrue(newHelper.isInitialized());
            assertEquals("test-registry", newHelper.getRegistryType());
        }
    }

    @Test
    void testIndexOperationsContract() throws IOException {
        ObjectMetadata metadata = createTestMetadata("test-obj", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        
        // Test updateIndex
        assertDoesNotThrow(() -> registryHelper.updateIndex("test-obj", metadata));
        assertTrue(registryHelper.exists("test-obj"));
        
        // Test removeFromIndex
        assertDoesNotThrow(() -> registryHelper.removeFromIndex("test-obj"));
        assertFalse(registryHelper.exists("test-obj"));
    }

    @Test
    void testSearchOperationsContract() throws IOException {
        // Add test data
        ObjectMetadata draft = createTestMetadata("draft-obj", "Package1", "1.0.0", ObjectStatus.DRAFT);
        draft.setRole("draft");
        ObjectMetadata approved = createTestMetadata("approved-obj", "Package2", "2.0.0", ObjectStatus.APPROVED);
        approved.setRole("approved");
        
        registryHelper.updateIndex("draft-obj", draft);
        registryHelper.updateIndex("approved-obj", approved);
        
        // Test search operations return expected types
        List<String> byStatus = registryHelper.findByStatus(ObjectStatus.DRAFT);
        assertNotNull(byStatus);
        assertTrue(byStatus.contains("draft-obj"));
        
        List<String> byObjectName = registryHelper.findByObjectName("Package1");
        assertNotNull(byObjectName);
        assertTrue(byObjectName.contains("draft-obj"));
        
        List<String> byRole = registryHelper.findByRole("draft");
        assertNotNull(byRole);
        assertTrue(byRole.contains("draft-obj"));
        
        Optional<String> byNameAndRole = registryHelper.findByObjectNameAndRole("Package1", "draft");
        assertTrue(byNameAndRole.isPresent());
        assertEquals("draft-obj", byNameAndRole.get());
        
        List<String> searchResults = registryHelper.searchObjectIds("test query", 10);
        assertNotNull(searchResults);
        
        List<String> allIds = registryHelper.getAllObjectIds();
        assertNotNull(allIds);
        assertEquals(2, allIds.size());
    }

    @Test
    void testStatisticsAndInfoContract() throws IOException {
        // Add test data
        registryHelper.updateIndex("test1", createTestMetadata("test1", "Package1", "1.0.0", ObjectStatus.DRAFT));
        registryHelper.updateIndex("test2", createTestMetadata("test2", "Package2", "2.0.0", ObjectStatus.APPROVED));
        
        // Test count operation
        long count = registryHelper.getObjectCount();
        assertEquals(2, count);
        
        // Test statistics operation
        Object stats = registryHelper.getRegistryStatistics();
        assertNotNull(stats);
        
        // Test registry type
        String registryType = registryHelper.getRegistryType();
        assertNotNull(registryType);
        assertEquals("test-registry", registryType);
    }

    @Test
    void testRebuildIndexContract() throws IOException {
        // Add test data
        registryHelper.updateIndex("test1", createTestMetadata("test1", "Package1", "1.0.0", ObjectStatus.DRAFT));
        
        // Rebuild should not throw exception
        assertDoesNotThrow(() -> registryHelper.rebuildIndex());
        
        // Data should still be accessible after rebuild
        assertTrue(registryHelper.exists("test1"));
    }

    @Test
    void testAutoCloseableContract() throws Exception {
        // Add test data
        registryHelper.updateIndex("test1", createTestMetadata("test1", "Package1", "1.0.0", ObjectStatus.DRAFT));
        assertTrue(registryHelper.exists("test1"));
        
        // Close should not throw exception
        assertDoesNotThrow(() -> registryHelper.close());
        
        // Verify close behavior (implementation-specific)
        assertTrue(registryHelper.isClosed());
    }

    @Test
    void testInputValidationContract() {
        ObjectMetadata metadata = createTestMetadata("test-obj", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        
        // Test null validation for updateIndex
        assertThrows(NullPointerException.class, () -> 
            registryHelper.updateIndex(null, metadata));
        assertThrows(NullPointerException.class, () -> 
            registryHelper.updateIndex("test-obj", null));
        
        // Test null validation for removeFromIndex
        assertThrows(NullPointerException.class, () -> 
            registryHelper.removeFromIndex(null));
        
        // Test null validation for search operations
        assertThrows(NullPointerException.class, () -> 
            registryHelper.findByStatus(null));
        assertThrows(NullPointerException.class, () -> 
            registryHelper.findByObjectName(null));
        assertThrows(NullPointerException.class, () -> 
            registryHelper.findByRole(null));
        assertThrows(NullPointerException.class, () -> 
            registryHelper.findByObjectNameAndRole(null, "role"));
        assertThrows(NullPointerException.class, () -> 
            registryHelper.findByObjectNameAndRole("name", null));
        
        // Test null validation for exists
        assertThrows(NullPointerException.class, () -> 
            registryHelper.exists(null));
    }

    @Test
    void testIOExceptionHandling() throws Exception {
        try (TestRegistryHelper faultyHelper = new TestRegistryHelper(true)) { // Enable IOExceptions
            // Initialize should handle IOException
            assertThrows(IOException.class, () -> faultyHelper.initialize());
            
            ObjectMetadata metadata = createTestMetadata("test-obj", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
            
            // Operations should propagate IOException
            assertThrows(IOException.class, () -> faultyHelper.updateIndex("test-obj", metadata));
            assertThrows(IOException.class, () -> faultyHelper.removeFromIndex("test-obj"));
            assertThrows(IOException.class, () -> faultyHelper.findByStatus(ObjectStatus.DRAFT));
            assertThrows(IOException.class, () -> faultyHelper.getAllObjectIds());
            assertThrows(IOException.class, () -> faultyHelper.getObjectCount());
            assertThrows(IOException.class, () -> faultyHelper.exists("test-obj"));
            assertThrows(IOException.class, () -> faultyHelper.rebuildIndex());
            assertThrows(IOException.class, () -> faultyHelper.getRegistryStatistics());
        }
    }

    @Test
    void testEmptyRegistryBehavior() throws IOException {
        // Test operations on empty registry
        assertEquals(0, registryHelper.getObjectCount());
        assertFalse(registryHelper.exists("non-existent"));
        
        List<String> emptyStatus = registryHelper.findByStatus(ObjectStatus.DRAFT);
        assertTrue(emptyStatus.isEmpty());
        
        List<String> emptyName = registryHelper.findByObjectName("NonExistent");
        assertTrue(emptyName.isEmpty());
        
        List<String> emptyRole = registryHelper.findByRole("non-existent");
        assertTrue(emptyRole.isEmpty());
        
        Optional<String> emptyNameRole = registryHelper.findByObjectNameAndRole("NonExistent", "non-existent");
        assertFalse(emptyNameRole.isPresent());
        
        List<String> emptySearch = registryHelper.searchObjectIds("query", 10);
        assertTrue(emptySearch.isEmpty());
        
        List<String> emptyAll = registryHelper.getAllObjectIds();
        assertTrue(emptyAll.isEmpty());
    }

    @Test
    void testSearchResultLimits() throws IOException {
        // Add multiple objects
        for (int i = 0; i < 5; i++) {
            ObjectMetadata metadata = createTestMetadata("obj" + i, "Package" + i, "1.0.0", ObjectStatus.DRAFT);
            registryHelper.updateIndex("obj" + i, metadata);
        }
        
        // Test search with limits
        List<String> limitedResults = registryHelper.searchObjectIds("test", 3);
        assertTrue(limitedResults.size() <= 3);
        
        List<String> unlimitedResults = registryHelper.searchObjectIds("test", 0);
        assertEquals(5, unlimitedResults.size());
    }

    @Test
    void testAbstractMethodsAreDefined() {
        // Verify all abstract methods are properly declared
        assertDoesNotThrow(() -> {
            AbstractRegistryHelper.class.getDeclaredMethod("initialize");
            AbstractRegistryHelper.class.getDeclaredMethod("updateIndex", String.class, ObjectMetadata.class);
            AbstractRegistryHelper.class.getDeclaredMethod("removeFromIndex", String.class);
            AbstractRegistryHelper.class.getDeclaredMethod("searchObjectIds", String.class, int.class);
            AbstractRegistryHelper.class.getDeclaredMethod("findByStatus", ObjectStatus.class);
            AbstractRegistryHelper.class.getDeclaredMethod("findByObjectName", String.class);
            AbstractRegistryHelper.class.getDeclaredMethod("findByRole", String.class);
            AbstractRegistryHelper.class.getDeclaredMethod("findByObjectNameAndRole", String.class, String.class);
            AbstractRegistryHelper.class.getDeclaredMethod("getAllObjectIds");
            AbstractRegistryHelper.class.getDeclaredMethod("getObjectCount");
            AbstractRegistryHelper.class.getDeclaredMethod("exists", String.class);
            AbstractRegistryHelper.class.getDeclaredMethod("rebuildIndex");
            AbstractRegistryHelper.class.getDeclaredMethod("getRegistryStatistics");
            AbstractRegistryHelper.class.getDeclaredMethod("getRegistryType");
        });
    }

    private ObjectMetadata createTestMetadata(String objectId, String objectName, String version, ObjectStatus status) {
        ObjectMetadata metadata = factory.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName(objectName);
        metadata.setVersion(version);
        metadata.setStatus(status);
        metadata.setUploadTime(Instant.now());
        metadata.setObjectType("EPackage");
        metadata.setUploadUser("test-user");
        metadata.setSourceChannel("TEST_CHANNEL");
        return metadata;
    }

    /**
     * Concrete test implementation of AbstractRegistryHelper for testing the interface contract.
     */
    private static class TestRegistryHelper extends AbstractRegistryHelper {
        
        private final java.util.Map<String, ObjectMetadata> data = new java.util.concurrent.ConcurrentHashMap<>();
        private boolean initialized = false;
        private boolean closed = false;
        private final boolean throwIOException;
        
        public TestRegistryHelper() {
            this(false);
        }
        
        public TestRegistryHelper(boolean throwIOException) {
            this.throwIOException = throwIOException;
        }
        
        @Override
        public void initialize() throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            initialized = true;
        }
        
        @Override
        public void updateIndex(String objectId, ObjectMetadata metadata) throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            java.util.Objects.requireNonNull(objectId, "Object ID cannot be null");
            java.util.Objects.requireNonNull(metadata, "Metadata cannot be null");
            data.put(objectId, metadata);
        }
        
        @Override
        public void removeFromIndex(String objectId) throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            java.util.Objects.requireNonNull(objectId, "Object ID cannot be null");
            data.remove(objectId);
        }
        
        @Override
        public List<String> searchObjectIds(String query, int maxResults) throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            List<String> allIds = getAllObjectIds();
            return allIds.stream()
                .limit(maxResults > 0 ? maxResults : Integer.MAX_VALUE)
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public List<String> findByStatus(ObjectStatus status) throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            java.util.Objects.requireNonNull(status, "Status cannot be null");
            return data.entrySet().stream()
                .filter(entry -> status.equals(entry.getValue().getStatus()))
                .map(java.util.Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public List<String> findByObjectName(String objectName) throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            java.util.Objects.requireNonNull(objectName, "Object name cannot be null");
            return data.entrySet().stream()
                .filter(entry -> objectName.equals(entry.getValue().getObjectName()))
                .map(java.util.Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public List<String> findByRole(String role) throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            java.util.Objects.requireNonNull(role, "Role cannot be null");
            return data.entrySet().stream()
                .filter(entry -> role.equals(entry.getValue().getRole()))
                .map(java.util.Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public Optional<String> findByObjectNameAndRole(String objectName, String role) throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            java.util.Objects.requireNonNull(objectName, "Object name cannot be null");
            java.util.Objects.requireNonNull(role, "Role cannot be null");
            return data.entrySet().stream()
                .filter(entry -> objectName.equals(entry.getValue().getObjectName()) && 
                               role.equals(entry.getValue().getRole()))
                .map(java.util.Map.Entry::getKey)
                .findFirst();
        }
        
        @Override
        public List<String> getAllObjectIds() throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            return List.copyOf(data.keySet());
        }
        
        @Override
        public long getObjectCount() throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            return data.size();
        }
        
        @Override
        public boolean exists(String objectId) throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            java.util.Objects.requireNonNull(objectId, "Object ID cannot be null");
            return data.containsKey(objectId);
        }
        
        @Override
        public void rebuildIndex() throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            // Test implementation - no-op
        }
        
        @Override
        public Object getRegistryStatistics() throws IOException {
            if (throwIOException) {
                throw new IOException("Test IOException");
            }
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("registryType", getRegistryType());
            stats.put("totalObjects", (long) data.size());
            stats.put("initialized", initialized);
            return stats;
        }
        
        @Override
        public String getRegistryType() {
            return "test-registry";
        }
        
        @Override
        public void close() throws Exception {
            super.close();
            data.clear();
            closed = true;
        }
        
        public boolean isInitialized() {
            return initialized;
        }
        
        public boolean isClosed() {
            return closed;
        }
    }
}