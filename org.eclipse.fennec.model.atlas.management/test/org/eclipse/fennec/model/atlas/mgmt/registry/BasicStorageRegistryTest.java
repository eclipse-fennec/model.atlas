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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
 * <p>Tests the storage registry functionality including service registration,
 * role-based discovery, cross-storage operations, and statistics collection
 * using mocked storage services.</p>
 */
@ExtendWith(MockitoExtension.class)
class BasicStorageRegistryTest {

    @Mock
    private EObjectStorageService<EObject> draftStorage;
    
    @Mock
    private EObjectStorageService<EObject> approvedStorage;
    
    @Mock
    private EObjectStorageService<EObject> documentationStorage;
    
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
        // Given: Storage service with role property
        Map<String, Object> draftProperties = createRoleProperties("draft");
        Map<String, Object> approvedProperties = createRoleProperties("approved");
        
        // When: Adding storage services
        registry.addStorageService(draftStorage, draftProperties);
        registry.addStorageService(approvedStorage, approvedProperties);
        
        // Then: Services are registered by role
        assertEquals(draftStorage, registry.getStorageByRole("draft"));
        assertEquals(approvedStorage, registry.getStorageByRole("approved"));
    }
    
    @Test
    void testAddStorageServiceWithoutRole() {
        // Given: Storage service without role property
        Map<String, Object> emptyProperties = new HashMap<>();
        
        // When: Adding storage service without role
        registry.addStorageService(draftStorage, emptyProperties);
        
        // Then: Service is not registered
        assertNull(registry.getStorageByRole("draft"));
        assertTrue(registry.getAvailableRoles().isEmpty());
    }
    
    @Test
    void testRemoveStorageService() {
        // Given: Registered storage service
        Map<String, Object> draftProperties = createRoleProperties("draft");
        registry.addStorageService(draftStorage, draftProperties);
        assertEquals(draftStorage, registry.getStorageByRole("draft"));
        
        // When: Removing storage service
        registry.removeStorageService(draftStorage);
        
        // Then: Service is no longer registered
        assertNull(registry.getStorageByRole("draft"));
        assertTrue(registry.getAvailableRoles().isEmpty());
    }
    
    @Test
    void testGetStorageByRole() {
        // Given: Multiple registered storage services
        registry.addStorageService(draftStorage, createRoleProperties("draft"));
        registry.addStorageService(approvedStorage, createRoleProperties("approved"));
        
        // When/Then: Retrieving by role
        assertEquals(draftStorage, registry.getStorageByRole("draft"));
        assertEquals(approvedStorage, registry.getStorageByRole("approved"));
        assertNull(registry.getStorageByRole("nonexistent"));
    }
    
    @Test
    void testGetAllStorages() {
        // Given: Multiple registered storage services
        registry.addStorageService(draftStorage, createRoleProperties("draft"));
        registry.addStorageService(approvedStorage, createRoleProperties("approved"));
        
        // When: Getting all storages
        EList<EObjectStorageService<EObject>> allStorages = registry.getAllStorages();
        
        // Then: All registered services are returned
        assertEquals(2, allStorages.size());
        assertTrue(allStorages.contains(draftStorage));
        assertTrue(allStorages.contains(approvedStorage));
    }
    
    @Test
    void testGetAvailableRoles() {
        // Given: Multiple registered storage services
        registry.addStorageService(draftStorage, createRoleProperties("draft"));
        registry.addStorageService(approvedStorage, createRoleProperties("approved"));
        
        // When: Getting available roles
        EList<String> roles = registry.getAvailableRoles();
        
        // Then: All roles are returned
        assertEquals(2, roles.size());
        assertTrue(roles.contains("draft"));
        assertTrue(roles.contains("approved"));
    }
    
    @Test
    void testUpdateGovernanceDocumentationId() throws Exception {
        // Given: Registered storage services with objects
        registry.addStorageService(draftStorage, createRoleProperties("draft"));
        registry.addStorageService(approvedStorage, createRoleProperties("approved"));
        
        // Mock objects in draft storage
        ObjectMetadata draftMetadata1 = createTestMetadata("obj1", ObjectStatus.DRAFT, "TestPackage");
        ObjectMetadata draftMetadata2 = createTestMetadata("obj2", ObjectStatus.DRAFT, "TestPackage");
        
        // Setup mock responses for draft storage - updateGovernanceDocumentationId uses listObjectIds + retrieveMetadata
        Promise<List<String>> objectIdsPromise = promiseFactory.resolved(Arrays.asList("obj1", "obj2"));
        Promise<ObjectMetadata> metadata1Promise = promiseFactory.resolved(draftMetadata1);
        Promise<ObjectMetadata> metadata2Promise = promiseFactory.resolved(draftMetadata2);
        Promise<Boolean> updatePromise = promiseFactory.resolved(true);
        
        when(draftStorage.listObjectIds()).thenReturn(objectIdsPromise);
        when(draftStorage.retrieveMetadata("obj1")).thenReturn(metadata1Promise);
        when(draftStorage.retrieveMetadata("obj2")).thenReturn(metadata2Promise);
        when(draftStorage.updateMetadata(any(String.class), any(ObjectMetadata.class))).thenReturn(updatePromise);
        
        // When: Updating governance documentation ID for draft role
        int updatedCount = registry.updateGovernanceDocumentationId("draft", "TestPackage", "gov-doc-123", "Test update");
        
        // Then: Only objects in draft role are updated
        assertEquals(2, updatedCount);
        verify(draftStorage).updateMetadata(eq("obj1"), any(ObjectMetadata.class));
        verify(draftStorage).updateMetadata(eq("obj2"), any(ObjectMetadata.class));
        verify(approvedStorage, never()).updateMetadata(any(String.class), any(ObjectMetadata.class));
    }
    
    @Test
    void testSearchMetadataAcrossRoles() throws Exception {
        // Given: Registered storage services
        registry.addStorageService(draftStorage, createRoleProperties("draft"));
        registry.addStorageService(approvedStorage, createRoleProperties("approved"));
        
        // Mock search results
        ObjectMetadata draftResult = createTestMetadata("obj1", ObjectStatus.DRAFT, "TestPackage");
        ObjectMetadata approvedResult = createTestMetadata("obj2", ObjectStatus.APPROVED, "TestPackage");
        
        Promise<List<ObjectMetadata>> draftPromise = promiseFactory.resolved(Arrays.asList(draftResult));
        Promise<List<ObjectMetadata>> approvedPromise = promiseFactory.resolved(Arrays.asList(approvedResult));
        
        when(draftStorage.queryObjects(any(ObjectQuery.class))).thenReturn(draftPromise);
        when(approvedStorage.queryObjects(any(ObjectQuery.class))).thenReturn(approvedPromise);
        
        // When: Searching across all roles
        ObjectQuery query = managementFactory.createObjectQuery();
        query.setStatus(ObjectStatus.DRAFT); // Will match both due to EMF enum default behavior
        
        EList<ObjectMetadata> results = registry.searchMetadataAcrossRoles(query);
        
        // Then: Results from all storages are aggregated
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(m -> "obj1".equals(m.getObjectId())));
        assertTrue(results.stream().anyMatch(m -> "obj2".equals(m.getObjectId())));
    }
    
    @Test
    void testGetStorageStatistics() {
        // Given: Registered storage services with mock statistics
        registry.addStorageService(draftStorage, createRoleProperties("draft"));
        registry.addStorageService(approvedStorage, createRoleProperties("approved"));
        
        when(draftStorage.getObjectCount()).thenReturn(5L);
        when(draftStorage.getBackendType()).thenReturn(StorageBackendType.FILE);
        when(approvedStorage.getObjectCount()).thenReturn(3L);
        when(approvedStorage.getBackendType()).thenReturn(StorageBackendType.MINIO);
        
        // When: Getting storage statistics
        Map<String, Object> statistics = registry.getStorageStatistics();
        
        // Then: Statistics are aggregated correctly
        assertEquals(8, statistics.get("totalObjectCount"));
        assertEquals(2, statistics.get("roleCount"));
        
        @SuppressWarnings("unchecked")
        EList<String> availableRoles = (EList<String>) statistics.get("availableRoles");
        assertEquals(2, availableRoles.size());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> roleStatistics = (Map<String, Object>) statistics.get("roleStatistics");
        assertNotNull(roleStatistics);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> draftStats = (Map<String, Object>) roleStatistics.get("draft");
        assertEquals(5L, draftStats.get("objectCount"));
        assertEquals("FILE", draftStats.get("backendType"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> approvedStats = (Map<String, Object>) roleStatistics.get("approved");
        assertEquals(3L, approvedStats.get("objectCount"));
        assertEquals("MINIO", approvedStats.get("backendType"));
    }
    
    @Test
    void testStorageServiceReplacement() {
        // Given: Storage service registered for a role
        registry.addStorageService(draftStorage, createRoleProperties("draft"));
        assertEquals(draftStorage, registry.getStorageByRole("draft"));
        
        // When: Another storage service is registered for the same role
        registry.addStorageService(approvedStorage, createRoleProperties("draft"));
        
        // Then: The new service replaces the old one
        assertEquals(approvedStorage, registry.getStorageByRole("draft"));
        assertEquals(1, registry.getAvailableRoles().size());
    }
    
    private Map<String, Object> createRoleProperties(String role) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("storage.role", role);
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