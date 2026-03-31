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
package org.eclipse.fennec.model.atlas.mgmt.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Unit tests for BasicStorageRegistry.
 * 
 * <p>
 * Tests the storage registry functionality including service registration,
 * role-based discovery, cross-storage operations, and statistics collection
 * using mocked storage services.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class BasicStorageRegistryTest {

    @Mock
    private EObjectStorageService<EObject> fileStorage;

    @Mock
    private EObjectStorageService<EObject> minioStorage;

    private BasicStorageRegistry registry;
    private ManagementFactory managementFactory;
    private PromiseFactory promiseFactory;

    @BeforeEach
    void setUp() {
        registry = new BasicStorageRegistry();
        managementFactory = ManagementFactory.eINSTANCE;
        promiseFactory = new PromiseFactory(Executors.newCachedThreadPool());

        // Inject the ManagementFactory (in real OSGi this would be done by DS)
        registry.managementFactory = managementFactory;
    }

    @Test
    void testAddStorageService() {
        // Given: Storage service with type property
        Map<String, Object> fileProperties = createTypeProperties("file");
        Map<String, Object> minioProperties = createTypeProperties("minio");

        // When: Adding storage services
        registry.addStorageService(fileStorage, fileProperties);
        registry.addStorageService(minioStorage, minioProperties);

        // Then: Services are registered by type
        assertEquals(fileStorage, registry.getStorageByType("file"));
        assertEquals(minioStorage, registry.getStorageByType("minio"));
    }

    @Test
    void testAddStorageServiceWithoutType() {
        // Given: Storage service without type property
        Map<String, Object> emptyProperties = new HashMap<>();

        // When: Adding storage service without type
        registry.addStorageService(fileStorage, emptyProperties);

        // Then: Service is not registered
        assertNull(registry.getStorageByType("file"));
        assertTrue(registry.getAvailableTypes().isEmpty());
    }

    @Test
    void testRemoveStorageService() {
        // Given: Registered storage service
        Map<String, Object> fileProperties = createTypeProperties("file");
        registry.addStorageService(fileStorage, fileProperties);
        assertEquals(fileStorage, registry.getStorageByType("file"));

        // When: Removing storage service
        registry.removeStorageService(fileStorage);

        // Then: Service is no longer registered
        assertNull(registry.getStorageByType("file"));
        assertTrue(registry.getAvailableTypes().isEmpty());
    }

    @Test
    void testGetStorageByType() {
        // Given: Multiple registered storage services
        registry.addStorageService(fileStorage, createTypeProperties("file"));
        registry.addStorageService(minioStorage, createTypeProperties("minio"));

        // When/Then: Retrieving by type
        assertEquals(fileStorage, registry.getStorageByType("file"));
        assertEquals(minioStorage, registry.getStorageByType("minio"));
        assertNull(registry.getStorageByType("apicurio"));
    }

    @Test
    void testGetAllStorages() {
        // Given: Multiple registered storage services
        registry.addStorageService(fileStorage, createTypeProperties("file"));
        registry.addStorageService(minioStorage, createTypeProperties("minio"));

        // When: Getting all storages
        EList<EObjectStorageService<EObject>> allStorages = registry.getAllStorages();

        // Then: All registered services are returned
        assertEquals(2, allStorages.size());
        assertTrue(allStorages.contains(fileStorage));
        assertTrue(allStorages.contains(minioStorage));
    }

    @Test
    void testGetAvailableTypes() {
        // Given: Multiple registered storage services
        registry.addStorageService(fileStorage, createTypeProperties("file"));
        registry.addStorageService(minioStorage, createTypeProperties("minio"));

        // When: Getting available types
        EList<String> types = registry.getAvailableTypes();

        // Then: All types are returned
        assertEquals(2, types.size());
        assertTrue(types.contains("file"));
        assertTrue(types.contains("minio"));
    }

    @Test
    void testSearchMetadataAcrossTypes() throws Exception {
        // Given: Registered storage services
        registry.addStorageService(fileStorage, createTypeProperties("file"));
        registry.addStorageService(minioStorage, createTypeProperties("minio"));

        // Mock search results
        ObjectMetadata draftResult = createTestMetadata("obj1", ObjectStatus.DRAFT, "TestPackage");
        ObjectMetadata approvedResult = createTestMetadata("obj2", ObjectStatus.APPROVED, "TestPackage");

        Promise<List<ObjectMetadata>> draftPromise = promiseFactory.resolved(Arrays.asList(draftResult));
        Promise<List<ObjectMetadata>> approvedPromise = promiseFactory.resolved(Arrays.asList(approvedResult));

        when(fileStorage.queryObjects(any(ObjectQuery.class))).thenReturn(draftPromise);
        when(minioStorage.queryObjects(any(ObjectQuery.class))).thenReturn(approvedPromise);

        // When: Searching across all types
        ObjectQuery query = managementFactory.createObjectQuery();
        query.setStatus(ObjectStatus.DRAFT); // Will match both due to EMF enum default behavior

        EList<ObjectMetadata> results = registry.searchMetadataAcrossTypes(query);

        // Then: Results from all storages are aggregated
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(m -> "obj1".equals(m.getObjectId())));
        assertTrue(results.stream().anyMatch(m -> "obj2".equals(m.getObjectId())));
    }

    @Test
    void testGetStorageStatistics() {
        // Given: Registered storage services with mock statistics
        registry.addStorageService(fileStorage, createTypeProperties("file"));
        registry.addStorageService(minioStorage, createTypeProperties("minio"));

        when(fileStorage.getObjectCount()).thenReturn(5L);
        when(fileStorage.getBackendType()).thenReturn(StorageBackendType.FILE);
        when(minioStorage.getObjectCount()).thenReturn(3L);
        when(minioStorage.getBackendType()).thenReturn(StorageBackendType.MINIO);

        // When: Getting storage statistics
        Map<String, Object> statistics = registry.getStorageStatistics();

        // Then: Statistics are aggregated correctly
        assertEquals(8, statistics.get("totalObjectCount"));
        assertEquals(2, statistics.get("typeCount"));

        @SuppressWarnings("unchecked")
        EList<String> availableTypes = (EList<String>) statistics.get("availableTypes");
        assertEquals(2, availableTypes.size());

        @SuppressWarnings("unchecked")
        Map<String, Object> typeStatistics = (Map<String, Object>) statistics.get("typeStatistics");
        assertNotNull(typeStatistics);

        @SuppressWarnings("unchecked")
        Map<String, Object> fileStats = (Map<String, Object>) typeStatistics.get("file");
        assertEquals(5L, fileStats.get("objectCount"));
        assertEquals("FILE", fileStats.get("backendType"));

        @SuppressWarnings("unchecked")
        Map<String, Object> minioStats = (Map<String, Object>) typeStatistics.get("minio");
        assertEquals(3L, minioStats.get("objectCount"));
        assertEquals("MINIO", minioStats.get("backendType"));
    }

    @Test
    void testStorageServiceReplacement() {
        // Given: Storage service registered for a type
        registry.addStorageService(fileStorage, createTypeProperties("file"));
        assertEquals(fileStorage, registry.getStorageByType("file"));

        // When: Another storage service is registered for the same type
        registry.addStorageService(minioStorage, createTypeProperties("file"));

        // Then: The new service replaces the old one
        assertEquals(minioStorage, registry.getStorageByType("file"));
        assertEquals(1, registry.getAvailableTypes().size());
    }

    private Map<String, Object> createTypeProperties(String type) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("storage.type", type);
        return properties;
    }

    private ObjectMetadata createTestMetadata(String objectId, ObjectStatus status, String objectType) {
        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setStatus(status);
        metadata.setObjectType(objectType);
        metadata.setUploadTime(Instant.now());
        metadata.setLastChangeTime(Instant.now());
        return metadata;
    }
}