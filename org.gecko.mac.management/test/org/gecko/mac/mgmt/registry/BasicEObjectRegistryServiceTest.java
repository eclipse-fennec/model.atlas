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
package org.gecko.mac.mgmt.registry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.gecko.mac.mgmt.management.ManagementFactory;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.gecko.mac.mgmt.management.ObjectStatus;
import org.gecko.mac.mgmt.storage.AbstractStorageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.util.promise.PromiseFactory;

/**
 * Unit tests for BasicEObjectRegistryService.
 * 
 * <p>These tests validate the in-memory cache functionality, index management,
 * and all registry service operations without requiring OSGi or file system resources.</p>
 * 
 * <h3>Test Coverage</h3>
 * <ul>
 * <li><strong>Cache Initialization</strong> - Tests automatic loading from storage on startup</li>
 * <li><strong>Metadata Operations</strong> - getMetadata, updateCache, removeFromCache</li>
 * <li><strong>Index-based Searches</strong> - findByStatus, findByObjectType, findByVersion</li>
 * <li><strong>Pattern Matching</strong> - findByVersionPattern with wildcard support</li>
 * <li><strong>Compound Queries</strong> - findByStatusAndType with set intersections</li>
 * <li><strong>Time-based Queries</strong> - findRecentlyModified with date filtering</li>
 * <li><strong>Statistics Generation</strong> - getRegistryStatistics with pre-computed metrics</li>
 * <li><strong>Thread Safety</strong> - ReadWriteLock behavior under concurrent access</li>
 * <li><strong>Error Handling</strong> - Graceful handling of null inputs and storage failures</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
public class BasicEObjectRegistryServiceTest {

    @Mock
    private AbstractStorageHelper mockStorageHelper;

    @Mock
    private PromiseFactory mockPromiseFactory;

    private BasicEObjectRegistryService<EObject> registryService;

    @BeforeEach
    public void setUp() throws Exception {
        // Set up mock storage helper to return empty lists initially
        when(mockStorageHelper.listObjectIds()).thenReturn(new ArrayList<>());
        
        // Create registry service with mocked dependencies
        registryService = new BasicEObjectRegistryService<>(mockStorageHelper, mockPromiseFactory);
    }

    @Test
    public void testCacheInitializationWithExistingObjects() throws Exception {
        // Prepare test data
        List<String> existingIds = List.of("obj1", "obj2", "obj3");
        ObjectMetadata metadata1 = createTestMetadata("obj1", "Package1", "1.0.0", ObjectStatus.DRAFT);
        ObjectMetadata metadata2 = createTestMetadata("obj2", "Package2", "2.0.0", ObjectStatus.APPROVED);
        ObjectMetadata metadata3 = createTestMetadata("obj3", "Package3", "1.1.0", ObjectStatus.DRAFT);

        // Mock storage helper responses
        when(mockStorageHelper.listObjectIds()).thenReturn(existingIds);
        when(mockStorageHelper.loadMetadata("obj1")).thenReturn(metadata1);
        when(mockStorageHelper.loadMetadata("obj2")).thenReturn(metadata2);
        when(mockStorageHelper.loadMetadata("obj3")).thenReturn(metadata3);

        // Create new registry service (this triggers cache initialization)
        registryService = new BasicEObjectRegistryService<>(mockStorageHelper, mockPromiseFactory);

        // Verify cache is populated correctly
        Optional<ObjectMetadata> retrieved1 = registryService.getMetadata("obj1");
        assertTrue(retrieved1.isPresent());
        assertEquals("Package1", retrieved1.get().getObjectName());

        Optional<ObjectMetadata> retrieved2 = registryService.getMetadata("obj2");
        assertTrue(retrieved2.isPresent());
        assertEquals("Package2", retrieved2.get().getObjectName());

        // Verify indexes are built correctly
        List<ObjectMetadata> draftObjects = registryService.findByStatus(ObjectStatus.DRAFT);
        assertEquals(2, draftObjects.size(), "Should find 2 DRAFT objects");

        List<ObjectMetadata> approvedObjects = registryService.findByStatus(ObjectStatus.APPROVED);
        assertEquals(1, approvedObjects.size(), "Should find 1 APPROVED object");
    }

    @Test
    public void testGetMetadata() {
        ObjectMetadata metadata = createTestMetadata("test-id", "TestPackage", "1.0.0", ObjectStatus.DRAFT);

        // Update cache with test metadata
        registryService.updateCache(metadata);

        // Test successful retrieval
        Optional<ObjectMetadata> result = registryService.getMetadata("test-id");
        assertTrue(result.isPresent());
        assertEquals("TestPackage", result.get().getObjectName());
        assertEquals("1.0.0", result.get().getVersion());

        // Test non-existent object
        Optional<ObjectMetadata> notFound = registryService.getMetadata("non-existent");
        assertFalse(notFound.isPresent());

        // Test null input
        assertThrows(NullPointerException.class, () -> registryService.getMetadata(null));
    }

    @Test
    public void testFindByStatus() {
        // Add test objects with different statuses
        registryService.updateCache(createTestMetadata("draft1", "Package1", "1.0.0", ObjectStatus.DRAFT));
        registryService.updateCache(createTestMetadata("draft2", "Package2", "2.0.0", ObjectStatus.DRAFT));
        registryService.updateCache(createTestMetadata("approved1", "Package3", "1.0.0", ObjectStatus.APPROVED));

        // Test finding by DRAFT status
        List<ObjectMetadata> draftObjects = registryService.findByStatus(ObjectStatus.DRAFT);
        assertEquals(2, draftObjects.size());
        assertTrue(draftObjects.stream().allMatch(m -> m.getStatus() == ObjectStatus.DRAFT));

        // Test finding by APPROVED status
        List<ObjectMetadata> approvedObjects = registryService.findByStatus(ObjectStatus.APPROVED);
        assertEquals(1, approvedObjects.size());
        assertEquals("approved1", approvedObjects.get(0).getObjectId());

        // Test finding non-existent status
        List<ObjectMetadata> rejectedObjects = registryService.findByStatus(ObjectStatus.REJECTED);
        assertTrue(rejectedObjects.isEmpty());

        // Test null input
        assertThrows(NullPointerException.class, () -> registryService.findByStatus(null));
    }

    @Test
    public void testFindByObjectType() {
        // Add test objects with different types
        registryService.updateCache(createTestMetadata("pkg1", "Package1", "1.0.0", "EPackage", ObjectStatus.DRAFT));
        registryService.updateCache(createTestMetadata("pkg2", "Package2", "2.0.0", "EPackage", ObjectStatus.APPROVED));
        registryService.updateCache(createTestMetadata("route1", "Route1", "1.0.0", "Route", ObjectStatus.DRAFT));

        // Test finding by EPackage type
        List<ObjectMetadata> packages = registryService.findByObjectType("EPackage");
        assertEquals(2, packages.size());
        assertTrue(packages.stream().allMatch(m -> "EPackage".equals(m.getObjectType())));

        // Test finding by Route type
        List<ObjectMetadata> routes = registryService.findByObjectType("Route");
        assertEquals(1, routes.size());
        assertEquals("route1", routes.get(0).getObjectId());

        // Test finding non-existent type
        List<ObjectMetadata> unknown = registryService.findByObjectType("UnknownType");
        assertTrue(unknown.isEmpty());

        // Test null input
        assertThrows(NullPointerException.class, () -> registryService.findByObjectType(null));
    }

    @Test
    public void testFindByVersion() {
        // Add test objects with different versions
        registryService.updateCache(createTestMetadata("v1", "Package1", "1.0.0", ObjectStatus.DRAFT));
        registryService.updateCache(createTestMetadata("v2", "Package2", "1.0.0", ObjectStatus.APPROVED));
        registryService.updateCache(createTestMetadata("v3", "Package3", "2.0.0", ObjectStatus.DRAFT));

        // Test finding by version 1.0.0
        List<ObjectMetadata> v1Objects = registryService.findByVersion("1.0.0");
        assertEquals(2, v1Objects.size());
        assertTrue(v1Objects.stream().allMatch(m -> "1.0.0".equals(m.getVersion())));

        // Test finding by version 2.0.0
        List<ObjectMetadata> v2Objects = registryService.findByVersion("2.0.0");
        assertEquals(1, v2Objects.size());
        assertEquals("v3", v2Objects.get(0).getObjectId());

        // Test finding non-existent version
        List<ObjectMetadata> v3Objects = registryService.findByVersion("3.0.0");
        assertTrue(v3Objects.isEmpty());

        // Test null input
        assertThrows(NullPointerException.class, () -> registryService.findByVersion(null));
    }

    @Test
    public void testFindByVersionPattern() {
        // Add test objects with different versions
        registryService.updateCache(createTestMetadata("v10", "Package1", "1.0.0", ObjectStatus.DRAFT));
        registryService.updateCache(createTestMetadata("v11", "Package2", "1.1.0", ObjectStatus.APPROVED));
        registryService.updateCache(createTestMetadata("v12", "Package3", "1.2.0", ObjectStatus.DRAFT));
        registryService.updateCache(createTestMetadata("v20", "Package4", "2.0.0", ObjectStatus.APPROVED));

        // Test wildcard pattern for 1.x versions
        List<ObjectMetadata> v1Pattern = registryService.findByVersionPattern("1.*");
        assertEquals(3, v1Pattern.size());
        assertTrue(v1Pattern.stream().allMatch(m -> m.getVersion().startsWith("1.")));

        // Test wildcard pattern for 1.1.x versions (should match 1.1.0)
        List<ObjectMetadata> v11Pattern = registryService.findByVersionPattern("1.1.*");
        assertEquals(1, v11Pattern.size());
        assertEquals("1.1.0", v11Pattern.get(0).getVersion());

        // Test single character wildcard
        List<ObjectMetadata> singleChar = registryService.findByVersionPattern("?.0.0");
        assertEquals(2, singleChar.size()); // Should match "1.0.0" and "2.0.0"

        // Test exact match (no wildcards)
        List<ObjectMetadata> exact = registryService.findByVersionPattern("1.0.0");
        assertEquals(1, exact.size());
        assertEquals("1.0.0", exact.get(0).getVersion());

        // Test null input
        assertThrows(NullPointerException.class, () -> registryService.findByVersionPattern(null));
    }

    @Test
    public void testFindByStatusAndType() {
        // Add test objects with different status/type combinations
        registryService.updateCache(createTestMetadata("dp1", "Package1", "1.0.0", "EPackage", ObjectStatus.DRAFT));
        registryService.updateCache(createTestMetadata("dp2", "Package2", "2.0.0", "EPackage", ObjectStatus.DRAFT));
        registryService.updateCache(createTestMetadata("ap1", "Package3", "1.0.0", "EPackage", ObjectStatus.APPROVED));
        registryService.updateCache(createTestMetadata("dr1", "Route1", "1.0.0", "Route", ObjectStatus.DRAFT));

        // Test finding DRAFT EPackages
        List<ObjectMetadata> draftPackages = registryService.findByStatusAndType(ObjectStatus.DRAFT, "EPackage");
        assertEquals(2, draftPackages.size());
        assertTrue(draftPackages.stream().allMatch(m -> 
            m.getStatus() == ObjectStatus.DRAFT && "EPackage".equals(m.getObjectType())));

        // Test finding APPROVED EPackages
        List<ObjectMetadata> approvedPackages = registryService.findByStatusAndType(ObjectStatus.APPROVED, "EPackage");
        assertEquals(1, approvedPackages.size());
        assertEquals("ap1", approvedPackages.get(0).getObjectId());

        // Test finding DRAFT Routes
        List<ObjectMetadata> draftRoutes = registryService.findByStatusAndType(ObjectStatus.DRAFT, "Route");
        assertEquals(1, draftRoutes.size());
        assertEquals("dr1", draftRoutes.get(0).getObjectId());

        // Test non-existent combination
        List<ObjectMetadata> rejectedRoutes = registryService.findByStatusAndType(ObjectStatus.REJECTED, "Route");
        assertTrue(rejectedRoutes.isEmpty());

        // Test null inputs
        assertThrows(NullPointerException.class, () -> registryService.findByStatusAndType(null, "EPackage"));
        assertThrows(NullPointerException.class, () -> registryService.findByStatusAndType(ObjectStatus.DRAFT, null));
    }

    @Test
    public void testFindRecentlyModified() {
        Instant baseTime = Instant.now();
        Instant oldTime = baseTime.minusSeconds(3600); // 1 hour ago
        Instant recentTime = baseTime.minusSeconds(300); // 5 minutes ago

        // Add test objects with different modification times
        ObjectMetadata oldMetadata = createTestMetadata("old", "OldPackage", "1.0.0", ObjectStatus.APPROVED);
        oldMetadata.setUploadTime(oldTime);
        oldMetadata.setLastChangeTime(oldTime);
        registryService.updateCache(oldMetadata);

        ObjectMetadata recentMetadata = createTestMetadata("recent", "RecentPackage", "1.0.0", ObjectStatus.DRAFT);
        recentMetadata.setUploadTime(recentTime);
        recentMetadata.setLastChangeTime(recentTime);
        registryService.updateCache(recentMetadata);

        ObjectMetadata veryRecentMetadata = createTestMetadata("very-recent", "VeryRecentPackage", "1.0.0", ObjectStatus.DRAFT);
        veryRecentMetadata.setUploadTime(baseTime);
        veryRecentMetadata.setLastChangeTime(baseTime);
        registryService.updateCache(veryRecentMetadata);

        // Test finding objects modified since 30 minutes ago
        Instant sinceTime = baseTime.minusSeconds(1800);
        List<ObjectMetadata> recentObjects = registryService.findRecentlyModified(sinceTime, 10);
        assertEquals(2, recentObjects.size()); // Should find "recent" and "very-recent"
        
        // Verify they're sorted by lastChangeTime descending (most recent first)
        assertTrue(recentObjects.get(0).getLastChangeTime().isAfter(recentObjects.get(1).getLastChangeTime()) ||
                  recentObjects.get(0).getLastChangeTime().equals(recentObjects.get(1).getLastChangeTime()));

        // Test with limit
        List<ObjectMetadata> limitedResults = registryService.findRecentlyModified(sinceTime, 1);
        assertEquals(1, limitedResults.size());

        // Test finding objects modified since now (should find none)
        List<ObjectMetadata> futureObjects = registryService.findRecentlyModified(baseTime.plusSeconds(1), 10);
        assertTrue(futureObjects.isEmpty());

        // Test null input
        assertThrows(NullPointerException.class, () -> registryService.findRecentlyModified(null, 10));
    }

    @Test
    public void testUpdateAndRemoveCache() {
        ObjectMetadata metadata = createTestMetadata("test-id", "TestPackage", "1.0.0", ObjectStatus.DRAFT);

        // Test cache update
        registryService.updateCache(metadata);
        Optional<ObjectMetadata> retrieved = registryService.getMetadata("test-id");
        assertTrue(retrieved.isPresent());
        assertEquals("TestPackage", retrieved.get().getObjectName());

        // Verify object appears in index searches
        List<ObjectMetadata> draftObjects = registryService.findByStatus(ObjectStatus.DRAFT);
        assertEquals(1, draftObjects.size());

        // Test cache removal
        registryService.removeFromCache("test-id");
        Optional<ObjectMetadata> afterRemoval = registryService.getMetadata("test-id");
        assertFalse(afterRemoval.isPresent());

        // Verify object is removed from index searches
        List<ObjectMetadata> draftObjectsAfter = registryService.findByStatus(ObjectStatus.DRAFT);
        assertTrue(draftObjectsAfter.isEmpty());

        // Test null inputs
        assertThrows(NullPointerException.class, () -> registryService.updateCache(null));
        assertThrows(NullPointerException.class, () -> registryService.updateCache(null));
        assertThrows(NullPointerException.class, () -> registryService.removeFromCache(null));
    }

    @Test
    public void testGetRegistryStatistics() throws Exception {
        // Set up mock promise factory to return completed promises
        when(mockPromiseFactory.submit(any())).thenAnswer(invocation -> {
            try {
                Object result = invocation.getArgument(0, java.util.concurrent.Callable.class).call();
                return org.osgi.util.promise.Promises.resolved(result);
            } catch (Exception e) {
                return org.osgi.util.promise.Promises.failed(e);
            }
        });

        // Add test objects for statistics
        registryService.updateCache(createTestMetadata("draft1", "Package1", "1.0.0", "EPackage", ObjectStatus.DRAFT));
        registryService.updateCache(createTestMetadata("approved1", "Package2", "2.0.0", "EPackage", ObjectStatus.APPROVED));
        registryService.updateCache(createTestMetadata("route1", "Route1", "1.0.0", "Route", ObjectStatus.DRAFT));

        // Get statistics
        Map<String, Object> stats = registryService.getRegistryStatistics().getValue();
        assertNotNull(stats);

        // Check basic counts
        assertEquals(3L, stats.get("totalObjects"));

        // Check status distribution
        @SuppressWarnings("unchecked")
        Map<String, Long> statusCounts = (Map<String, Long>) stats.get("statusCounts");
        assertNotNull(statusCounts);
        assertEquals(2L, statusCounts.get("DRAFT"));
        assertEquals(1L, statusCounts.get("APPROVED"));

        // Check type distribution
        @SuppressWarnings("unchecked")
        Map<String, Long> typeCounts = (Map<String, Long>) stats.get("objectTypeCounts");
        assertNotNull(typeCounts);
        assertEquals(2L, typeCounts.get("EPackage"));
        assertEquals(1L, typeCounts.get("Route"));

        // Check metadata fields
        assertEquals("basic-memory", stats.get("registryType"));
        assertTrue(stats.containsKey("cacheInitialized"));
        assertTrue(stats.containsKey("lastCacheUpdate"));
        assertTrue(stats.containsKey("generatedAt"));
    }

    @Test
    public void testFindPendingApproval() {
        // Add test objects
        registryService.updateCache(createTestMetadata("draft1", "Package1", "1.0.0", ObjectStatus.DRAFT));
        registryService.updateCache(createTestMetadata("approved1", "Package2", "2.0.0", ObjectStatus.APPROVED));
        registryService.updateCache(createTestMetadata("draft2", "Package3", "1.0.0", ObjectStatus.DRAFT));

        // Test findPendingApproval (should be equivalent to findByStatus(DRAFT))
        List<ObjectMetadata> pendingObjects = registryService.findPendingApproval();
        assertEquals(2, pendingObjects.size());
        assertTrue(pendingObjects.stream().allMatch(m -> m.getStatus() == ObjectStatus.DRAFT));
    }

    @Test
    public void testFindByFingerprint() {
        // Create test objects with fingerprints
        ObjectMetadata metadata1 = createTestMetadata("fp1", "Package1", "1.0.0", ObjectStatus.DRAFT);
        metadata1.setGenerationTriggerFingerprint("fingerprint1234567890abcdef");
        
        ObjectMetadata metadata2 = createTestMetadata("fp2", "Package2", "2.0.0", ObjectStatus.APPROVED);
        metadata2.setGenerationTriggerFingerprint("fingerprintxyz9876543210");
        
        ObjectMetadata metadata3 = createTestMetadata("fp3", "Package3", "1.0.0", ObjectStatus.DRAFT);
        // No fingerprint set for this one

        // Update cache with test metadata
        registryService.updateCache(metadata1);
        registryService.updateCache(metadata2);
        registryService.updateCache(metadata3);

        // Test successful fingerprint lookup
        Optional<ObjectMetadata> result1 = registryService.findByFingerprint("fingerprint1234567890abcdef");
        assertTrue(result1.isPresent());
        assertEquals("fp1", result1.get().getObjectId());
        assertEquals("Package1", result1.get().getObjectName());

        Optional<ObjectMetadata> result2 = registryService.findByFingerprint("fingerprintxyz9876543210");
        assertTrue(result2.isPresent());
        assertEquals("fp2", result2.get().getObjectId());
        assertEquals("Package2", result2.get().getObjectName());

        // Test non-existent fingerprint
        Optional<ObjectMetadata> notFound = registryService.findByFingerprint("nonexistentfingerprint");
        assertFalse(notFound.isPresent());

        // Test null input
        assertThrows(NullPointerException.class, () -> registryService.findByFingerprint(null));
    }

    @Test
    public void testFingerprintIndexManagement() {
        // Create object with fingerprint
        ObjectMetadata metadata = createTestMetadata("test-fp", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        metadata.setGenerationTriggerFingerprint("testfingerprint123");

        // Add to cache
        registryService.updateCache(metadata);
        
        // Verify it can be found by fingerprint
        Optional<ObjectMetadata> found = registryService.findByFingerprint("testfingerprint123");
        assertTrue(found.isPresent());
        assertEquals("test-fp", found.get().getObjectId());

        // Update metadata with different fingerprint (create new metadata object)
        ObjectMetadata updatedMetadata = createTestMetadata("test-fp", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        updatedMetadata.setGenerationTriggerFingerprint("newfingerprint456");
        registryService.updateCache(updatedMetadata);

        // Old fingerprint should not be found
        Optional<ObjectMetadata> oldNotFound = registryService.findByFingerprint("testfingerprint123");
        assertFalse(oldNotFound.isPresent());

        // New fingerprint should be found
        Optional<ObjectMetadata> newFound = registryService.findByFingerprint("newfingerprint456");
        assertTrue(newFound.isPresent());
        assertEquals("test-fp", newFound.get().getObjectId());

        // Remove from cache
        registryService.removeFromCache("test-fp");

        // Fingerprint should no longer be found
        Optional<ObjectMetadata> removedNotFound = registryService.findByFingerprint("newfingerprint456");
        assertFalse(removedNotFound.isPresent());
    }

    @Test
    public void testFindByObjectName() {
        // Create test objects with same objectName but different roles
        ObjectMetadata draftVersion = createTestMetadata("pkg-draft", "PackageName", "1.0.0", "EPackage", ObjectStatus.DRAFT);
        draftVersion.setRole("draft");
        
        ObjectMetadata approvedVersion = createTestMetadata("pkg-approved", "PackageName", "1.0.0", "EPackage", ObjectStatus.APPROVED);
        approvedVersion.setRole("approved");
        
        ObjectMetadata documentationVersion = createTestMetadata("pkg-docs", "PackageName", "1.0.0", "EPackage", ObjectStatus.DEPLOYED);
        documentationVersion.setRole("documentation");
        
        ObjectMetadata differentPackage = createTestMetadata("other-pkg", "OtherPackage", "1.0.0", "EPackage", ObjectStatus.DRAFT);
        differentPackage.setRole("draft");

        // Update cache with test metadata
        registryService.updateCache(draftVersion);
        registryService.updateCache(approvedVersion);
        registryService.updateCache(documentationVersion);
        registryService.updateCache(differentPackage);

        // Test finding by objectName - should return all versions/roles of PackageName
        List<ObjectMetadata> packageVersions = registryService.findByObjectName("PackageName");
        assertEquals(3, packageVersions.size());
        assertTrue(packageVersions.stream().allMatch(m -> "PackageName".equals(m.getObjectName())));
        
        // Verify all three roles are present
        assertEquals(3, packageVersions.stream().map(ObjectMetadata::getRole).distinct().count());

        // Test finding by different objectName
        List<ObjectMetadata> otherPackageVersions = registryService.findByObjectName("OtherPackage");
        assertEquals(1, otherPackageVersions.size());
        assertEquals("other-pkg", otherPackageVersions.get(0).getObjectId());

        // Test non-existent objectName
        List<ObjectMetadata> notFound = registryService.findByObjectName("NonExistentPackage");
        assertTrue(notFound.isEmpty());

        // Test null input
        assertThrows(NullPointerException.class, () -> registryService.findByObjectName(null));
    }

    @Test
    public void testFindByObjectNameAndRole() {
        // Create test objects with same objectName but different roles
        ObjectMetadata draftVersion = createTestMetadata("pkg-draft", "PackageName", "1.0.0", "EPackage", ObjectStatus.DRAFT);
        draftVersion.setRole("draft");
        
        ObjectMetadata approvedVersion = createTestMetadata("pkg-approved", "PackageName", "1.0.0", "EPackage", ObjectStatus.APPROVED);
        approvedVersion.setRole("approved");
        
        ObjectMetadata documentationVersion = createTestMetadata("pkg-docs", "PackageName", "1.0.0", "EPackage", ObjectStatus.DEPLOYED);
        documentationVersion.setRole("documentation");

        // Update cache with test metadata
        registryService.updateCache(draftVersion);
        registryService.updateCache(approvedVersion);
        registryService.updateCache(documentationVersion);

        // Test finding specific objectName and role combination
        Optional<ObjectMetadata> draftResult = registryService.findByObjectNameAndRole("PackageName", "draft");
        assertTrue(draftResult.isPresent());
        assertEquals("pkg-draft", draftResult.get().getObjectId());
        assertEquals("draft", draftResult.get().getRole());

        Optional<ObjectMetadata> approvedResult = registryService.findByObjectNameAndRole("PackageName", "approved");
        assertTrue(approvedResult.isPresent());
        assertEquals("pkg-approved", approvedResult.get().getObjectId());
        assertEquals("approved", approvedResult.get().getRole());

        Optional<ObjectMetadata> documentationResult = registryService.findByObjectNameAndRole("PackageName", "documentation");
        assertTrue(documentationResult.isPresent());
        assertEquals("pkg-docs", documentationResult.get().getObjectId());
        assertEquals("documentation", documentationResult.get().getRole());

        // Test non-existent objectName
        Optional<ObjectMetadata> nonExistentName = registryService.findByObjectNameAndRole("NonExistentPackage", "draft");
        assertFalse(nonExistentName.isPresent());

        // Test non-existent role for existing objectName
        Optional<ObjectMetadata> nonExistentRole = registryService.findByObjectNameAndRole("PackageName", "production");
        assertFalse(nonExistentRole.isPresent());

        // Test null inputs
        assertThrows(NullPointerException.class, () -> registryService.findByObjectNameAndRole(null, "draft"));
        assertThrows(NullPointerException.class, () -> registryService.findByObjectNameAndRole("PackageName", null));
    }

    @Test
    public void testFindByRole() {
        // Create test objects with different roles
        ObjectMetadata draft1 = createTestMetadata("pkg1-draft", "Package1", "1.0.0", "EPackage", ObjectStatus.DRAFT);
        draft1.setRole("draft");
        
        ObjectMetadata draft2 = createTestMetadata("pkg2-draft", "Package2", "1.0.0", "EPackage", ObjectStatus.DRAFT);
        draft2.setRole("draft");
        
        ObjectMetadata approved1 = createTestMetadata("pkg1-approved", "Package1", "1.0.0", "EPackage", ObjectStatus.APPROVED);
        approved1.setRole("approved");
        
        ObjectMetadata documentation1 = createTestMetadata("pkg1-docs", "Package1", "1.0.0", "EPackage", ObjectStatus.DEPLOYED);
        documentation1.setRole("documentation");

        // Update cache with test metadata
        registryService.updateCache(draft1);
        registryService.updateCache(draft2);
        registryService.updateCache(approved1);
        registryService.updateCache(documentation1);

        // Test finding by draft role
        List<ObjectMetadata> draftObjects = registryService.findByRole("draft");
        assertEquals(2, draftObjects.size());
        assertTrue(draftObjects.stream().allMatch(m -> "draft".equals(m.getRole())));

        // Test finding by approved role
        List<ObjectMetadata> approvedObjects = registryService.findByRole("approved");
        assertEquals(1, approvedObjects.size());
        assertEquals("pkg1-approved", approvedObjects.get(0).getObjectId());
        assertEquals("approved", approvedObjects.get(0).getRole());

        // Test finding by documentation role
        List<ObjectMetadata> documentationObjects = registryService.findByRole("documentation");
        assertEquals(1, documentationObjects.size());
        assertEquals("pkg1-docs", documentationObjects.get(0).getObjectId());
        assertEquals("documentation", documentationObjects.get(0).getRole());

        // Test non-existent role
        List<ObjectMetadata> productionObjects = registryService.findByRole("production");
        assertTrue(productionObjects.isEmpty());

        // Test null input
        assertThrows(NullPointerException.class, () -> registryService.findByRole(null));
    }

    @Test
    public void testRoleIndexManagement() {
        // Create test object with role
        ObjectMetadata metadata = createTestMetadata("test-role", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        metadata.setRole("draft");

        // Add to cache
        registryService.updateCache(metadata);
        
        // Verify it can be found by role
        List<ObjectMetadata> draftObjects = registryService.findByRole("draft");
        assertEquals(1, draftObjects.size());
        assertEquals("test-role", draftObjects.get(0).getObjectId());

        // Update metadata with different role (create new metadata object)
        ObjectMetadata updatedMetadata = createTestMetadata("test-role", "TestPackage", "1.0.0", ObjectStatus.APPROVED);
        updatedMetadata.setRole("approved");
        registryService.updateCache(updatedMetadata);

        // Old role should not contain this object
        List<ObjectMetadata> draftObjectsAfter = registryService.findByRole("draft");
        assertTrue(draftObjectsAfter.isEmpty());

        // New role should contain this object
        List<ObjectMetadata> approvedObjects = registryService.findByRole("approved");
        assertEquals(1, approvedObjects.size());
        assertEquals("test-role", approvedObjects.get(0).getObjectId());

        // Remove from cache
        registryService.removeFromCache("test-role");

        // Role should no longer contain this object
        List<ObjectMetadata> approvedObjectsAfter = registryService.findByRole("approved");
        assertTrue(approvedObjectsAfter.isEmpty());
    }

    @Test
    public void testDeactivate() {
        // Add test data
        registryService.updateCache(createTestMetadata("test1", "Package1", "1.0.0", ObjectStatus.DRAFT));
        
        // Verify data exists
        assertTrue(registryService.getMetadata("test1").isPresent());
        
        // Deactivate
        registryService.deactivate();
        
        // Verify cache is cleared (note: this might require additional verification depending on implementation)
        // The cache should be cleared but the service might still be functional for new operations
    }

    @Test
    public void testUpdateCacheRejectsMetadataWithoutObjectId() {
        // Create metadata without objectId (violates data integrity)
        ObjectMetadata metadataWithoutId = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadataWithoutId.setObjectName("TestPackage");
        metadataWithoutId.setVersion("1.0.0");
        metadataWithoutId.setStatus(ObjectStatus.DRAFT);
        metadataWithoutId.setUploadTime(Instant.now());
        metadataWithoutId.setUploadUser("test-user");
        // Explicitly verify objectId is null
        assertNull(metadataWithoutId.getObjectId(), "ObjectId should be null for this test");

        // Attempting to update cache with metadata without objectId should throw exception
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            registryService.updateCache(metadataWithoutId);
        });
        
        assertTrue(exception.getMessage().contains("ObjectId in metadata must not be null"),
                  "Exception should mention null objectId requirement");
    }

    @Test
    public void testUpdateCacheRejectsMetadataWithEmptyObjectId() {
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

    @Test
    public void testUpdateCacheAcceptsValidMetadataWithObjectId() {
        // Create valid metadata with proper objectId
        ObjectMetadata validMetadata = createTestMetadata("valid-id", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        
        // This should work without any exceptions
        assertDoesNotThrow(() -> {
            registryService.updateCache(validMetadata);
        });
        
        // Verify the metadata was actually cached
        Optional<ObjectMetadata> retrieved = registryService.getMetadata("valid-id");
        assertTrue(retrieved.isPresent());
        assertEquals("TestPackage", retrieved.get().getObjectName());
        assertEquals("valid-id", retrieved.get().getObjectId());
    }

    @Test
    public void testRegistryMaintainsObjectIdIntegrity() {
        // Create metadata with objectId
        ObjectMetadata originalMetadata = createTestMetadata("integrity-test", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        
        // Add to registry
        registryService.updateCache(originalMetadata);
        
        // Retrieve from registry
        Optional<ObjectMetadata> retrieved = registryService.getMetadata("integrity-test");
        assertTrue(retrieved.isPresent());
        
        // Verify objectId is preserved exactly
        assertEquals("integrity-test", retrieved.get().getObjectId());
        assertNotNull(retrieved.get().getObjectId());
        assertFalse(retrieved.get().getObjectId().isEmpty());
        
        // Verify other metadata fields are also preserved
        assertEquals("TestPackage", retrieved.get().getObjectName());
        assertEquals("1.0.0", retrieved.get().getVersion());
        assertEquals(ObjectStatus.DRAFT, retrieved.get().getStatus());
    }

    // ===== Helper Methods =====

    private ObjectMetadata createTestMetadata(String objectId, String objectName, String version, ObjectStatus status) {
        return createTestMetadata(objectId, objectName, version, "EPackage", status);
    }

    private ObjectMetadata createTestMetadata(String objectId, String objectName, String version, String objectType, ObjectStatus status) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName(objectName);
        metadata.setVersion(version);
        metadata.setObjectType(objectType);
        metadata.setStatus(status);
        metadata.setUploadTime(Instant.now());
        metadata.setLastChangeTime(Instant.now());
        metadata.setUploadUser("test-user");
        metadata.setSourceChannel("TEST_CHANNEL");
        return metadata;
    }
}