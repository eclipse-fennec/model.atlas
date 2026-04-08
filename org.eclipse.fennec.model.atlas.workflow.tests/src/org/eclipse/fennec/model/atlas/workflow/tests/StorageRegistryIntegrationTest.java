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
package org.eclipse.fennec.model.atlas.workflow.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations.StorageRegistrySetup;
import org.eclipse.fennec.model.atlas.workflow.tests.support.LuceneAwareTempDirExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration test for StorageRegistry with real storage services.
 *
 * <p>
 * This test verifies that the StorageRegistry correctly discovers and manages
 * storage services in a real OSGi environment with file-based storage backends.
 * </p>
 */
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
class StorageRegistryIntegrationTest {

    private ManagementFactory managementFactory;

    @BeforeEach
    void setUp() {
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
    void testStorageServiceDiscovery(@InjectService(timeout = 5000l) StorageRegistry storageRegistry) {
        // Verify that the registry is properly injected
        assertNotNull(storageRegistry, "StorageRegistry should be injected");
        // Verify that all expected storage services are registered
        EList<String> availableTypes = storageRegistry.getAvailableTypes();

        // We expect at least the file storage type configured in StorageRegistrySetup
        assertTrue(availableTypes.size() >= 1, "Should have at least 1 storage type");
        assertTrue(availableTypes.contains("file"), "Should have file storage type");
    }

    @Test
    @StorageRegistrySetup
    void testGetAllStorages(@InjectService StorageRegistry storageRegistry) {
        // Verify that the registry is properly injected
        assertNotNull(storageRegistry, "StorageRegistry should be injected");
        // Test getting all registered storage services
        EList<EObjectStorageService<EObject>> allStorages = storageRegistry.getAllStorages();

        assertTrue(allStorages.size() == 1, "Should have 1 storage service");
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
        assertTrue(statistics.containsKey("typeCount"));
        assertTrue(statistics.containsKey("typeStatistics"));
        assertTrue(statistics.containsKey("availableTypes"));

        // Verify that we have the expected number of types
        Integer typeCount = (Integer) statistics.get("typeCount");
        assertTrue(typeCount == 1, "Should have 1 type in statistics");
    }

    @Test
    @StorageRegistrySetup
    void testCrossStorageSearch(@InjectService StorageRegistry storageRegistry) throws Exception {
        // Verify that the registry is properly injected
        assertNotNull(storageRegistry, "StorageRegistry should be injected");
        // Store a test object in file storage to verify cross-storage search
        EObjectStorageService<EObject> fileStorage = storageRegistry.getStorageByType("file");
        assertNotNull(fileStorage);

        // Create test metadata
        ObjectMetadata testMetadata = createTestMetadata("test-object-1", ObjectStatus.DRAFT, "TestPackage");

        // Store a simple EObject (we'll use ObjectMetadata itself as the EObject for
        // simplicity)
        // Using default scope, registry, and stage for testing
        fileStorage.storeObject("default", "test-registry", "draft", "test-object-1", testMetadata, testMetadata)
                .getValue();
        String objectId = testMetadata.getObjectId();
        assertNotNull(objectId);

        // Now test cross-storage search
        ObjectQuery query = managementFactory.createObjectQuery();
        query.setStatus(ObjectStatus.DRAFT);

        EList<ObjectMetadata> searchResults = storageRegistry.searchMetadataAcrossTypes(query);

        // Should find at least our test object
        assertTrue(searchResults.size() >= 1, "Should find at least the test object");

        // Verify our test object is in the results
        boolean foundTestObject = searchResults.stream()
                .anyMatch(metadata -> "test-object-1".equals(metadata.getObjectId()));
        assertTrue(foundTestObject, "Should find our test object in cross-storage search");

        // Clean up
        fileStorage.deleteObject("default", "test-registry", "draft", objectId);
    }

    @Test
    @StorageRegistrySetup
    void testStorageBackendTypes(@InjectService StorageRegistry storageRegistry) {
        // Verify that the registry is properly injected
        assertNotNull(storageRegistry, "StorageRegistry should be injected");
        // Verify that storage services have proper backend types configured
        EObjectStorageService<EObject> fileStorage = storageRegistry.getStorageByType("file");

        assertNotNull(fileStorage);
        assertNotNull(fileStorage.getBackendType());
        assertNotNull(fileStorage.getStorageType());

        // Should be FILE backend for this test setup
        assertEquals("FILE", fileStorage.getBackendType().toString());
        assertEquals("file", fileStorage.getStorageType());
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