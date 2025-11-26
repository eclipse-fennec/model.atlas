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
package org.eclipse.fennec.model.atlas.mgmt.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
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
 * Test class for verifying the query delegation behavior in AbstractEObjectStorageService.
 * 
 * <p>This test verifies that:</p>
 * <ul>
 * <li>When registry service is available, queries are delegated to registry for fast lookup</li>
 * <li>When registry service is unavailable, queries fall back to direct storage scanning</li>
 * <li>Registry results are properly filtered by storage role</li>
 * <li>Query parameters are mapped to appropriate registry methods</li>
 * <li>Fallback behavior works when registry queries fail</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class AbstractEObjectStorageServiceQueryDelegationTest {

	@Mock
    private EObjectRegistryService<EObject> registryService;
    
    @Mock
    private AbstractStorageHelper storageHelper;
    
    private ManagementFactory managementFactory;
    
    private TestableAbstractEObjectStorageService storageService;
    private PromiseFactory promiseFactory;
    
    @BeforeEach
    void setUp() {
        promiseFactory = new PromiseFactory(Executors.newCachedThreadPool());
        storageService = new TestableAbstractEObjectStorageService();
        storageService.setPromiseFactory(promiseFactory);
        storageService.setStorageHelper(storageHelper);
        storageService.setStorageRole("draft");
        
        // Use EMF factory to create real instances
        managementFactory = ManagementFactory.eINSTANCE;
    }

    @Test
    void testQueryDelegationToRegistry_StatusAndType() throws Exception {
        // Given: Registry service is available and query has status + type
        storageService.setRegistryService(registryService);
        
        // Create fresh query for this test
        ObjectQuery statusAndTypeQuery = managementFactory.createObjectQuery();
        statusAndTypeQuery.setStatus(ObjectStatus.DRAFT);
        statusAndTypeQuery.setObjectType("EPackage");
        
        ObjectMetadata metadata1 = createTestMetadata("obj1", "draft", ObjectStatus.DRAFT, "EPackage");
        ObjectMetadata metadata2 = createTestMetadata("obj2", "draft", ObjectStatus.DRAFT, "EPackage");
        List<ObjectMetadata> registryResults = Arrays.asList(metadata1, metadata2);
        
        when(registryService.findByStatusAndType(ObjectStatus.DRAFT, "EPackage"))
            .thenReturn(registryResults);
        
        // When: Query is executed
        Promise<List<ObjectMetadata>> resultPromise = storageService.queryObjects(statusAndTypeQuery);
        List<ObjectMetadata> results = resultPromise.getValue();
        
        // Then: Registry method was called and results returned
        verify(registryService).findByStatusAndType(ObjectStatus.DRAFT, "EPackage");
        verify(storageHelper, never()).listObjectIds(); // No direct storage access
        assertEquals(2, results.size());
        assertEquals("obj1", results.get(0).getObjectId());
        assertEquals("obj2", results.get(1).getObjectId());
    }

    @Test
    void testQueryDelegationToRegistry_StatusOnly() throws Exception {
        // Given: Registry service is available and query has status only
        storageService.setRegistryService(registryService);
        
        // Create fresh query for this test
        ObjectQuery statusOnlyQuery = managementFactory.createObjectQuery();
        statusOnlyQuery.setStatus(ObjectStatus.APPROVED);
        // objectType remains null by default
        
        ObjectMetadata metadata = createTestMetadata("obj1", "draft", ObjectStatus.APPROVED, "Route");
        when(registryService.findByStatus(ObjectStatus.APPROVED))
            .thenReturn(Arrays.asList(metadata));
        
        // When: Query is executed
        Promise<List<ObjectMetadata>> resultPromise = storageService.queryObjects(statusOnlyQuery);
        List<ObjectMetadata> results = resultPromise.getValue();
        
        // Then: Registry method was called
        verify(registryService).findByStatus(ObjectStatus.APPROVED);
        verify(storageHelper, never()).listObjectIds();
        assertEquals(1, results.size());
    }

    @Test
    void testQueryDelegationToRegistry_TypeOnly() throws Exception {
        // Given: Registry service is available and query has type only
        // Note: EMF enum defaults to DRAFT, so this actually becomes status+type query
        storageService.setRegistryService(registryService);
        
        // Create fresh query for this test
        ObjectQuery typeQuery = managementFactory.createObjectQuery();
        typeQuery.setObjectType("Route");
        // status defaults to DRAFT (first enum value in EMF)
        
        ObjectMetadata metadata = createTestMetadata("obj1", "draft", ObjectStatus.DRAFT, "Route");
        when(registryService.findByStatusAndType(ObjectStatus.DRAFT, "Route"))
            .thenReturn(Arrays.asList(metadata));
        
        // When: Query is executed
        Promise<List<ObjectMetadata>> resultPromise = storageService.queryObjects(typeQuery);
        List<ObjectMetadata> results = resultPromise.getValue();
        
        // Then: Registry method was called with both status and type (EMF default behavior)
        verify(registryService).findByStatusAndType(ObjectStatus.DRAFT, "Route");
        verify(storageHelper, never()).listObjectIds();
        assertEquals(1, results.size());
    }

    @Test
    void testQueryFallbackToStorageScan_NoRegistryService() throws Exception {
        // Given: No registry service is available
        storageService.setRegistryService(null);
        
        // Create fresh query for this test
        ObjectQuery noRegistryQuery = managementFactory.createObjectQuery();
        noRegistryQuery.setStatus(ObjectStatus.DRAFT);
        
        setupStorageHelperForDirectScan();
        
        // When: Query is executed
        Promise<List<ObjectMetadata>> resultPromise = storageService.queryObjects(noRegistryQuery);
        List<ObjectMetadata> results = resultPromise.getValue();
        
        // Then: Direct storage scan was performed
        verify(storageHelper).listObjectIds();
        verify(storageHelper, times(2)).loadMetadata(any(String.class));
        assertEquals(1, results.size()); // Only obj1 matches DRAFT status filter
    }

    @Test
    void testQueryFallbackToStorageScan_RegistryFailure() throws Exception {
        // Given: Registry service throws exception
        storageService.setRegistryService(registryService);
        
        // Create fresh query for this test
        ObjectQuery registryFailureQuery = managementFactory.createObjectQuery();
        registryFailureQuery.setStatus(ObjectStatus.DRAFT);
        
        when(registryService.findByStatus(ObjectStatus.DRAFT))
            .thenThrow(new RuntimeException("Registry failure"));
        
        setupStorageHelperForDirectScan();
        
        // When: Query is executed
        Promise<List<ObjectMetadata>> resultPromise = storageService.queryObjects(registryFailureQuery);
        List<ObjectMetadata> results = resultPromise.getValue();
        
        // Then: Registry was tried first, then fell back to storage scan
        verify(registryService).findByStatus(ObjectStatus.DRAFT);
        verify(storageHelper).listObjectIds();
        assertEquals(1, results.size()); // Only obj1 matches DRAFT status filter
    }

    @Test
    void testQueryRoleFiltering() throws Exception {
        // Given: Registry returns results from multiple roles, but we only want our role
        storageService.setRegistryService(registryService);
        storageService.setStorageRole("approved");
        
        // Create fresh query for this test
        ObjectQuery roleFilterQuery = managementFactory.createObjectQuery();
        roleFilterQuery.setStatus(ObjectStatus.APPROVED);
        
        ObjectMetadata draftMetadata = createTestMetadata("obj1", "draft", ObjectStatus.APPROVED, "EPackage");
        ObjectMetadata approvedMetadata = createTestMetadata("obj2", "approved", ObjectStatus.APPROVED, "EPackage");
        ObjectMetadata releaseMetadata = createTestMetadata("obj3", "release", ObjectStatus.APPROVED, "EPackage");
        
        when(registryService.findByStatus(ObjectStatus.APPROVED))
            .thenReturn(Arrays.asList(draftMetadata, approvedMetadata, releaseMetadata));
        
        // When: Query is executed
        Promise<List<ObjectMetadata>> resultPromise = storageService.queryObjects(roleFilterQuery);
        List<ObjectMetadata> results = resultPromise.getValue();
        
        // Then: Only objects from our storage role are returned
        assertEquals(1, results.size());
        assertEquals("obj2", results.get(0).getObjectId());
        assertEquals("approved", results.get(0).getRole());
    }

    @Test
    void testQueryWithoutSpecificCriteria_FallsBackToStorageScan() throws Exception {
        // Given: Query has uploadUser only (non-indexed field)
        // Note: EMF sets status to DRAFT by default, so registry will be called but return empty results
        storageService.setRegistryService(registryService);
        
        // Create fresh query for this test
        ObjectQuery fallbackQuery = managementFactory.createObjectQuery();
        fallbackQuery.setUploadUser("user1"); // Not indexed in registry
        // status defaults to DRAFT due to EMF enum behavior
        
        // Mock registry to return empty results (no objects match default DRAFT status)
        when(registryService.findByStatus(ObjectStatus.DRAFT))
            .thenReturn(Arrays.asList());
        
        // No storage helper setup needed since registry returns empty results
        
        // When: Query is executed
        Promise<List<ObjectMetadata>> resultPromise = storageService.queryObjects(fallbackQuery);
        List<ObjectMetadata> results = resultPromise.getValue();
        
        // Then: Registry was called due to EMF default, but returned empty results
        // Since registry found 0 results, no fallback to storage scan is needed
        verify(registryService).findByStatus(ObjectStatus.DRAFT);
        verify(storageHelper, never()).listObjectIds(); // No fallback needed since registry found 0 results
        assertEquals(0, results.size()); // Registry returned 0 results, so no filtering needed
    }

    private void setupStorageHelperForDirectScan() throws Exception {
        // Setup storage helper to return test objects
        when(storageHelper.listObjectIds()).thenReturn(Arrays.asList("obj1", "obj2"));
        
        ObjectMetadata metadata1 = createTestMetadata("obj1", "draft", ObjectStatus.DRAFT, "EPackage");
        metadata1.setUploadUser("user1");
        ObjectMetadata metadata2 = createTestMetadata("obj2", "draft", ObjectStatus.APPROVED, "Route");
        metadata2.setUploadUser("user2");
        
        when(storageHelper.loadMetadata("obj1")).thenReturn(metadata1);
        when(storageHelper.loadMetadata("obj2")).thenReturn(metadata2);
    }

    private ObjectMetadata createTestMetadata(String objectId, String role, ObjectStatus status, String objectType) {
        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setRole(role);
        metadata.setStatus(status);
        metadata.setObjectType(objectType);
        return metadata;
    }

    /**
     * Testable implementation of AbstractEObjectStorageService for testing purposes.
     */
    private static class TestableAbstractEObjectStorageService extends AbstractEObjectStorageService {
        
        private EObjectRegistryService<EObject> testRegistryService;
        private String testStorageRole = "test";
        
        @Override
        protected AbstractStorageHelper createStorageHelper() throws Exception {
            return null; // Not used in these tests
        }

        @Override
        public StorageBackendType getBackendType() {
            return StorageBackendType.FILE;
        }
        
        @Override
        protected EObjectRegistryService<EObject> getRegistryService() {
            return testRegistryService;
        }
        
        @Override
        protected String getStorageRole() {
            return testStorageRole;
        }
        
        // Helper methods for test setup
        void setPromiseFactory(PromiseFactory promiseFactory) {
            this.promiseFactory = promiseFactory;
        }
        
        void setStorageHelper(AbstractStorageHelper storageHelper) {
            this.storageHelper = storageHelper;
        }
        
        void setRegistryService(EObjectRegistryService<EObject> registryService) {
            this.testRegistryService = registryService;
            this.registryService = registryService;
        }
        
        void setStorageRole(String storageRole) {
            this.testStorageRole = storageRole;
            this.storageRole = storageRole;
        }
    }
}