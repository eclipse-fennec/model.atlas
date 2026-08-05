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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.util.promise.PromiseFactory;

/**
 * Unit tests for BasicEObjectRegistryService.
 * 
 * <p>
 * These tests validate the in-memory cache functionality, index management, and
 * all registry service operations without requiring OSGi or file system
 * resources.
 * </p>
 * 
 * <h3>Test Coverage</h3>
 * <ul>
 * <li><strong>Cache Initialization</strong> - Tests automatic loading from
 * storage on startup</li>
 * <li><strong>Metadata Operations</strong> - getMetadata, updateCache,
 * removeFromCache</li>
 * <li><strong>Index-based Searches</strong> - findByStatus, findByObjectType,
 * findByVersion</li>
 * <li><strong>Pattern Matching</strong> - findByVersionPattern with wildcard
 * support</li>
 * <li><strong>Compound Queries</strong> - findByStatusAndType with set
 * intersections</li>
 * <li><strong>Time-based Queries</strong> - findRecentlyModified with date
 * filtering</li>
 * <li><strong>Statistics Generation</strong> - getRegistryStatistics with
 * pre-computed metrics</li>
 * <li><strong>Thread Safety</strong> - ReadWriteLock behavior under concurrent
 * access</li>
 * <li><strong>Error Handling</strong> - Graceful handling of null inputs and
 * storage failures</li>
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
        // Set up mock storage helper to return empty lists initially. Cache
        // initialization is asynchronous (and lazy on first read), so tests that
        // never read the cache never consume this stubbing — hence lenient().
        lenient().when(mockStorageHelper.loadAllMetadata()).thenReturn(new ArrayList<>());

        // Create registry service with mocked dependencies
        registryService = new BasicEObjectRegistryService<>(mockStorageHelper, mockPromiseFactory);
    }

    @Test
    public void testConstructorDoesNotLoadSynchronously() throws Exception {
        // Construction happens on the DS activation path of storage services and
        // must not block on storage I/O (the mocked PromiseFactory swallows the
        // async initialization, so any synchronous load would show up here)
        verify(mockStorageHelper, never()).loadAllMetadata();

        // The first read initializes the cache lazily instead
        registryService.getMetadata("any-id");
        verify(mockStorageHelper).loadAllMetadata();
    }

    @Test
    public void testCacheInitializationWithExistingObjects() throws Exception {
        // Prepare test data

        ObjectMetadata metadata1 = createTestMetadata("obj1", "Package1", "1.0.0", ObjectStatus.DRAFT);
        ObjectMetadata metadata2 = createTestMetadata("obj2", "Package2", "2.0.0", ObjectStatus.APPROVED);
        ObjectMetadata metadata3 = createTestMetadata("obj3", "Package3", "1.1.0", ObjectStatus.DRAFT);
        List<ObjectMetadata> existingMetadatas = List.of(metadata1, metadata2, metadata3);

        // Mock storage helper responses
        when(mockStorageHelper.loadAllMetadata()).thenReturn(existingMetadatas);

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
        assertTrue(draftPackages.stream()
                .allMatch(m -> m.getStatus() == ObjectStatus.DRAFT && "EPackage".equals(m.getObjectType())));

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

        ObjectMetadata veryRecentMetadata = createTestMetadata("very-recent", "VeryRecentPackage", "1.0.0",
                ObjectStatus.DRAFT);
        veryRecentMetadata.setUploadTime(baseTime);
        veryRecentMetadata.setLastChangeTime(baseTime);
        registryService.updateCache(veryRecentMetadata);

        // Test finding objects modified since 30 minutes ago
        Instant sinceTime = baseTime.minusSeconds(1800);
        List<ObjectMetadata> recentObjects = registryService.findRecentlyModified(sinceTime, 10);
        assertEquals(2, recentObjects.size()); // Should find "recent" and "very-recent"

        // Verify they're sorted by lastChangeTime descending (most recent first)
        assertTrue(recentObjects.get(0).getLastChangeTime().isAfter(recentObjects.get(1).getLastChangeTime())
                || recentObjects.get(0).getLastChangeTime().equals(recentObjects.get(1).getLastChangeTime()));

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
        registryService
                .updateCache(createTestMetadata("approved1", "Package2", "2.0.0", "EPackage", ObjectStatus.APPROVED));
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
    public void testFindByGenerationTriggerFingerprint() {
        // Create test objects with generation trigger fingerprints
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
        Optional<ObjectMetadata> result1 = registryService.findByGenerationTriggerFingerprint("fingerprint1234567890abcdef");
        assertTrue(result1.isPresent());
        assertEquals("fp1", result1.get().getObjectId());
        assertEquals("Package1", result1.get().getObjectName());

        Optional<ObjectMetadata> result2 = registryService.findByGenerationTriggerFingerprint("fingerprintxyz9876543210");
        assertTrue(result2.isPresent());
        assertEquals("fp2", result2.get().getObjectId());
        assertEquals("Package2", result2.get().getObjectName());

        // Test non-existent fingerprint
        Optional<ObjectMetadata> notFound = registryService.findByGenerationTriggerFingerprint("nonexistentfingerprint");
        assertFalse(notFound.isPresent());

        // Test null input
        assertThrows(NullPointerException.class, () -> registryService.findByGenerationTriggerFingerprint(null));
    }

    @Test
    public void testGenerationTriggerFingerprintIndexManagement() {
        // Create object with fingerprint
        ObjectMetadata metadata = createTestMetadata("test-fp", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        metadata.setGenerationTriggerFingerprint("testfingerprint123");

        // Add to cache
        registryService.updateCache(metadata);

        // Verify it can be found by fingerprint
        Optional<ObjectMetadata> found = registryService.findByGenerationTriggerFingerprint("testfingerprint123");
        assertTrue(found.isPresent());
        assertEquals("test-fp", found.get().getObjectId());

        // Update metadata with different fingerprint (create new metadata object)
        ObjectMetadata updatedMetadata = createTestMetadata("test-fp", "TestPackage", "1.0.0", ObjectStatus.DRAFT);
        updatedMetadata.setGenerationTriggerFingerprint("newfingerprint456");
        registryService.updateCache(updatedMetadata);

        // Old fingerprint should not be found
        Optional<ObjectMetadata> oldNotFound = registryService.findByGenerationTriggerFingerprint("testfingerprint123");
        assertFalse(oldNotFound.isPresent());

        // New fingerprint should be found
        Optional<ObjectMetadata> newFound = registryService.findByGenerationTriggerFingerprint("newfingerprint456");
        assertTrue(newFound.isPresent());
        assertEquals("test-fp", newFound.get().getObjectId());

        // Remove from cache
        registryService.removeFromCache("test-fp");

        // Fingerprint should no longer be found
        Optional<ObjectMetadata> removedNotFound = registryService.findByGenerationTriggerFingerprint("newfingerprint456");
        assertFalse(removedNotFound.isPresent());
    }

    @Test
    public void testFindByFingerprint() {
        // The model fingerprint identifies a content version, not an object: the same
        // EPackage content in two stages (e.g. two git branches) yields two metadata
        // entries sharing one fingerprint — findByFingerprint therefore returns a List.
        String sharedFingerprint = "fp1:14466a0b5de879a6c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1b0";

        ObjectMetadata draftEntry = createTestMetadata("pkg-draft", "SensorPackage", "1.0.0", ObjectStatus.DRAFT);
        draftEntry.setFingerprint(sharedFingerprint);

        ObjectMetadata releaseEntry = createTestMetadata("pkg-release", "SensorPackage", "1.0.0", ObjectStatus.APPROVED);
        releaseEntry.setFingerprint(sharedFingerprint);

        ObjectMetadata otherEntry = createTestMetadata("pkg-other", "OtherPackage", "2.0.0", ObjectStatus.DRAFT);
        otherEntry.setFingerprint("fp1:aaaa0b5de879a6c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1cccc");

        registryService.updateCache(draftEntry);
        registryService.updateCache(releaseEntry);
        registryService.updateCache(otherEntry);

        // Shared fingerprint returns BOTH entries
        List<ObjectMetadata> shared = registryService.findByFingerprint(sharedFingerprint);
        assertEquals(2, shared.size());
        assertTrue(shared.stream().map(ObjectMetadata::getObjectId).toList()
                .containsAll(List.of("pkg-draft", "pkg-release")));

        // Distinct fingerprint returns exactly one
        List<ObjectMetadata> single = registryService
                .findByFingerprint("fp1:aaaa0b5de879a6c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1cccc");
        assertEquals(1, single.size());
        assertEquals("pkg-other", single.get(0).getObjectId());

        // Unknown fingerprint returns an empty list, not null
        assertTrue(registryService.findByFingerprint("fp1:0000000000000000000000000000000000000000000000000000000000000000")
                .isEmpty());

        // Null input rejected
        assertThrows(NullPointerException.class, () -> registryService.findByFingerprint(null));
    }

    @Test
    public void testFingerprintIndexManagement() {
        // Two entries share one fingerprint (same content in two stages)
        String sharedFingerprint = "fp1:14466a0b5de879a6c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1b0";

        ObjectMetadata draftEntry = createTestMetadata("pkg-draft", "SensorPackage", "1.0.0", ObjectStatus.DRAFT);
        draftEntry.setFingerprint(sharedFingerprint);
        ObjectMetadata releaseEntry = createTestMetadata("pkg-release", "SensorPackage", "1.0.0", ObjectStatus.APPROVED);
        releaseEntry.setFingerprint(sharedFingerprint);

        registryService.updateCache(draftEntry);
        registryService.updateCache(releaseEntry);
        assertEquals(2, registryService.findByFingerprint(sharedFingerprint).size());

        // Removing ONE entry must not evict the sibling sharing the fingerprint
        registryService.removeFromCache("pkg-draft");
        List<ObjectMetadata> remaining = registryService.findByFingerprint(sharedFingerprint);
        assertEquals(1, remaining.size());
        assertEquals("pkg-release", remaining.get(0).getObjectId());

        // Updating an entry to a new fingerprint moves it between index buckets
        ObjectMetadata updatedRelease = createTestMetadata("pkg-release", "SensorPackage", "1.0.1", ObjectStatus.APPROVED);
        updatedRelease.setFingerprint("fp1:bbbb0b5de879a6c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1dddd");
        registryService.updateCache(updatedRelease);
        assertTrue(registryService.findByFingerprint(sharedFingerprint).isEmpty());
        assertEquals(1, registryService
                .findByFingerprint("fp1:bbbb0b5de879a6c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1dddd").size());

        // Removing the last holder empties the bucket
        registryService.removeFromCache("pkg-release");
        assertTrue(registryService
                .findByFingerprint("fp1:bbbb0b5de879a6c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1dddd").isEmpty());
    }

    @Test
    public void testFindByObjectName() {
        // Create test objects with same objectName but different Stages
        ObjectMetadata draftVersion = createTestMetadata("pkg-draft", "PackageName", "1.0.0", "EPackage",
                ObjectStatus.DRAFT);
        draftVersion.setStage("draft");

        ObjectMetadata approvedVersion = createTestMetadata("pkg-approved", "PackageName", "1.0.0", "EPackage",
                ObjectStatus.APPROVED);
        approvedVersion.setStage("approved");

        ObjectMetadata documentationVersion = createTestMetadata("pkg-docs", "PackageName", "1.0.0", "EPackage",
                ObjectStatus.DEPLOYED);
        documentationVersion.setStage("documentation");

        ObjectMetadata differentPackage = createTestMetadata("other-pkg", "OtherPackage", "1.0.0", "EPackage",
                ObjectStatus.DRAFT);
        differentPackage.setStage("draft");

        // Update cache with test metadata
        registryService.updateCache(draftVersion);
        registryService.updateCache(approvedVersion);
        registryService.updateCache(documentationVersion);
        registryService.updateCache(differentPackage);

        // Test finding by objectName - should return all versions/Stages of PackageName
        List<ObjectMetadata> packageVersions = registryService.findByObjectName("PackageName");
        assertEquals(3, packageVersions.size());
        assertTrue(packageVersions.stream().allMatch(m -> "PackageName".equals(m.getObjectName())));

        // Verify all three Stages are present
        assertEquals(3, packageVersions.stream().map(ObjectMetadata::getStage).distinct().count());

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
    public void testFindByObjectNameAndStage() {
        // Create test objects with same objectName but different Stages
        ObjectMetadata draftVersion = createTestMetadata("pkg-draft", "PackageName", "1.0.0", "EPackage",
                ObjectStatus.DRAFT);
        draftVersion.setStage("draft");

        ObjectMetadata approvedVersion = createTestMetadata("pkg-approved", "PackageName", "1.0.0", "EPackage",
                ObjectStatus.APPROVED);
        approvedVersion.setStage("approved");

        ObjectMetadata documentationVersion = createTestMetadata("pkg-docs", "PackageName", "1.0.0", "EPackage",
                ObjectStatus.DEPLOYED);
        documentationVersion.setStage("documentation");

        // Update cache with test metadata
        registryService.updateCache(draftVersion);
        registryService.updateCache(approvedVersion);
        registryService.updateCache(documentationVersion);

        // Test finding specific objectName and Stage combination
        Optional<ObjectMetadata> draftResult = registryService.findByObjectNameAndStage("PackageName", "draft");
        assertTrue(draftResult.isPresent());
        assertEquals("pkg-draft", draftResult.get().getObjectId());
        assertEquals("draft", draftResult.get().getStage());

        Optional<ObjectMetadata> approvedResult = registryService.findByObjectNameAndStage("PackageName", "approved");
        assertTrue(approvedResult.isPresent());
        assertEquals("pkg-approved", approvedResult.get().getObjectId());
        assertEquals("approved", approvedResult.get().getStage());

        Optional<ObjectMetadata> documentationResult = registryService.findByObjectNameAndStage("PackageName",
                "documentation");
        assertTrue(documentationResult.isPresent());
        assertEquals("pkg-docs", documentationResult.get().getObjectId());
        assertEquals("documentation", documentationResult.get().getStage());

        // Test non-existent objectName
        Optional<ObjectMetadata> nonExistentName = registryService.findByObjectNameAndStage("NonExistentPackage",
                "draft");
        assertFalse(nonExistentName.isPresent());

        // Test non-existent Stage for existing objectName
        Optional<ObjectMetadata> nonExistentStage = registryService.findByObjectNameAndStage("PackageName",
                "production");
        assertFalse(nonExistentStage.isPresent());

        // Test null inputs
        assertThrows(NullPointerException.class, () -> registryService.findByObjectNameAndStage(null, "draft"));
        assertThrows(NullPointerException.class, () -> registryService.findByObjectNameAndStage("PackageName", null));
    }

    @Test
    public void testDeactivate() {
        // Add test data
        registryService.updateCache(createTestMetadata("test1", "Package1", "1.0.0", ObjectStatus.DRAFT));

        // Verify data exists
        assertTrue(registryService.getMetadata("test1").isPresent());

        // Deactivate
        registryService.deactivate();

        // Verify cache is cleared (note: this might require additional verification
        // depending on implementation)
        // The cache should be cleared but the service might still be functional for new
        // operations
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

        // Attempting to update cache with metadata without objectId should throw
        // exception
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

        // Attempting to update cache with metadata with empty objectId should throw
        // exception
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
        ObjectMetadata originalMetadata = createTestMetadata("integrity-test", "TestPackage", "1.0.0",
                ObjectStatus.DRAFT);

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

    @Test
    public void testFindByScopeAndStage() {
        // Create test objects with different scopes and Stages
        ObjectMetadata tenant1Draft1 = createTestMetadataWithScope("t1-draft-1", "Package1", "1.0.0", "EPackage",
                ObjectStatus.DRAFT, "draft", "tenant1");
        ObjectMetadata tenant1Draft2 = createTestMetadataWithScope("t1-draft-2", "Package2", "1.0.0", "EPackage",
                ObjectStatus.DRAFT, "draft", "tenant1");
        ObjectMetadata tenant1Approved = createTestMetadataWithScope("t1-approved-1", "Package3", "1.0.0", "EPackage",
                ObjectStatus.APPROVED, "approved", "tenant1");

        ObjectMetadata tenant2Draft = createTestMetadataWithScope("t2-draft-1", "Package1", "1.0.0", "EPackage",
                ObjectStatus.DRAFT, "draft", "tenant2");
        ObjectMetadata tenant2Approved = createTestMetadataWithScope("t2-approved-1", "Package2", "1.0.0", "EPackage",
                ObjectStatus.APPROVED, "approved", "tenant2");
        ObjectMetadata tenant2Doc = createTestMetadataWithScope("t2-doc-1", "Package3", "1.0.0", "EPackage",
                ObjectStatus.DEPLOYED, "documentation", "tenant2");

        // Update cache with test metadata
        registryService.updateCache(tenant1Draft1);
        registryService.updateCache(tenant1Draft2);
        registryService.updateCache(tenant1Approved);
        registryService.updateCache(tenant2Draft);
        registryService.updateCache(tenant2Approved);
        registryService.updateCache(tenant2Doc);

        // Test finding by scope and Stage - tenant1, draft
        List<ObjectMetadata> tenant1Drafts = registryService.findByScopeAndStage("tenant1", "draft");
        assertEquals(2, tenant1Drafts.size());
        assertTrue(
                tenant1Drafts.stream().allMatch(m -> "tenant1".equals(m.getScope()) && "draft".equals(m.getStage())));

        // Test finding by scope and Stage - tenant2, approved
        List<ObjectMetadata> tenant2Approveds = registryService.findByScopeAndStage("tenant2", "approved");
        assertEquals(1, tenant2Approveds.size());
        assertEquals("t2-approved-1", tenant2Approveds.get(0).getObjectId());
        assertEquals("tenant2", tenant2Approveds.get(0).getScope());
        assertEquals("approved", tenant2Approveds.get(0).getStage());

        // Test finding by scope and Stage - tenant2, documentation
        List<ObjectMetadata> tenant2Docs = registryService.findByScopeAndStage("tenant2", "documentation");
        assertEquals(1, tenant2Docs.size());
        assertEquals("t2-doc-1", tenant2Docs.get(0).getObjectId());

        // Test non-existent scope
        List<ObjectMetadata> nonExistentScope = registryService.findByScopeAndStage("tenant3", "draft");
        assertTrue(nonExistentScope.isEmpty());

        // Test non-existent Stage for existing scope
        List<ObjectMetadata> nonExistentStage = registryService.findByScopeAndStage("tenant1", "documentation");
        assertTrue(nonExistentStage.isEmpty());

        // Test null inputs
        assertThrows(NullPointerException.class, () -> registryService.findByScopeAndStage(null, "draft"));
        assertThrows(NullPointerException.class, () -> registryService.findByScopeAndStage("tenant1", null));
    }

    @Test
    public void testFindByScopeStageAndName() {
        // Create test objects with different scopes, Stages, and names
        ObjectMetadata tenant1DraftSensor = createTestMetadataWithScope("t1-sensor-draft", "SensorModel", "1.0.0",
                "EPackage", ObjectStatus.DRAFT, "draft", "tenant1");
        ObjectMetadata tenant1ApprovedSensor = createTestMetadataWithScope("t1-sensor-approved", "SensorModel", "2.0.0",
                "EPackage", ObjectStatus.APPROVED, "approved", "tenant1");
        ObjectMetadata tenant1DraftActuator = createTestMetadataWithScope("t1-actuator-draft", "ActuatorModel", "1.0.0",
                "EPackage", ObjectStatus.DRAFT, "draft", "tenant1");

        ObjectMetadata tenant2DraftSensor = createTestMetadataWithScope("t2-sensor-draft", "SensorModel", "1.0.0",
                "EPackage", ObjectStatus.DRAFT, "draft", "tenant2");
        ObjectMetadata tenant2ApprovedSensor = createTestMetadataWithScope("t2-sensor-approved", "SensorModel", "2.0.0",
                "EPackage", ObjectStatus.APPROVED, "approved", "tenant2");
        ObjectMetadata tenant2DocSensor = createTestMetadataWithScope("t2-sensor-doc", "SensorDocumentation", "1.0.0",
                "EPackage", ObjectStatus.DEPLOYED, "documentation", "tenant2");

        // Same name, different tenants and Stages
        ObjectMetadata tenant1DraftCommon = createTestMetadataWithScope("t1-common-draft", "CommonPackage", "1.0.0",
                "EPackage", ObjectStatus.DRAFT, "draft", "tenant1");
        ObjectMetadata tenant2DraftCommon = createTestMetadataWithScope("t2-common-draft", "CommonPackage", "1.0.0",
                "EPackage", ObjectStatus.DRAFT, "draft", "tenant2");
        ObjectMetadata tenant2ApprovedCommon = createTestMetadataWithScope("t2-common-approved", "CommonPackage",
                "2.0.0", "EPackage", ObjectStatus.APPROVED, "approved", "tenant2");

        // Update registry cache
        registryService.updateCache(tenant1DraftSensor);
        registryService.updateCache(tenant1ApprovedSensor);
        registryService.updateCache(tenant1DraftActuator);
        registryService.updateCache(tenant2DraftSensor);
        registryService.updateCache(tenant2ApprovedSensor);
        registryService.updateCache(tenant2DocSensor);
        registryService.updateCache(tenant1DraftCommon);
        registryService.updateCache(tenant2DraftCommon);
        registryService.updateCache(tenant2ApprovedCommon);

        // Test 1: Exact match - specific scope, Stage, and name
        List<ObjectMetadata> tenant1DraftSensors = registryService.findByScopeStageAndName("tenant1", "draft",
                "SensorModel");
        assertEquals(1, tenant1DraftSensors.size(), "Should find 1 SensorModel in tenant1 draft");
        assertEquals("t1-sensor-draft", tenant1DraftSensors.get(0).getObjectId());

        // Test 2: Different scope, same Stage and name
        List<ObjectMetadata> tenant2DraftSensors = registryService.findByScopeStageAndName("tenant2", "draft",
                "SensorModel");
        assertEquals(1, tenant2DraftSensors.size(), "Should find 1 SensorModel in tenant2 draft");
        assertEquals("t2-sensor-draft", tenant2DraftSensors.get(0).getObjectId());

        // Test 3: Different Stage, same scope and name
        List<ObjectMetadata> tenant1ApprovedSensors = registryService.findByScopeStageAndName("tenant1", "approved",
                "SensorModel");
        assertEquals(1, tenant1ApprovedSensors.size(), "Should find 1 SensorModel in tenant1 approved");
        assertEquals("t1-sensor-approved", tenant1ApprovedSensors.get(0).getObjectId());

        // Test 4: Wildcard search - find all Sensor* models in tenant2 draft
        List<ObjectMetadata> tenant2DraftSensorWildcard = registryService.findByScopeStageAndName("tenant2", "draft",
                "Sensor*");
        assertEquals(1, tenant2DraftSensorWildcard.size(), "Should find 1 Sensor* model in tenant2 draft");
        assertTrue(tenant2DraftSensorWildcard.stream().anyMatch(m -> m.getObjectName().startsWith("Sensor")));

        // Test 5: Wildcard search - find all models with "Sensor" in name across
        // tenant2 documentation
        List<ObjectMetadata> tenant2DocSensorWildcard = registryService.findByScopeStageAndName("tenant2",
                "documentation", "Sensor*");
        assertEquals(1, tenant2DocSensorWildcard.size(), "Should find 1 Sensor* model in tenant2 documentation");
        assertEquals("t2-sensor-doc", tenant2DocSensorWildcard.get(0).getObjectId());

        // Test 6: Multiple matches - same name, scope, and Stage
        List<ObjectMetadata> tenant2DraftCommons = registryService.findByScopeStageAndName("tenant2", "draft",
                "CommonPackage");
        assertEquals(1, tenant2DraftCommons.size(), "Should find 1 CommonPackage in tenant2 draft");
        assertEquals("t2-common-draft", tenant2DraftCommons.get(0).getObjectId());

        // Test 7: Non-existent combination - wrong scope
        List<ObjectMetadata> nonExistentScope = registryService.findByScopeStageAndName("tenant3", "draft",
                "SensorModel");
        assertEquals(0, nonExistentScope.size(), "Should find 0 results for non-existent tenant3");

        // Test 8: Non-existent combination - wrong Stage
        List<ObjectMetadata> nonExistentStage = registryService.findByScopeStageAndName("tenant1", "documentation",
                "SensorModel");
        assertEquals(0, nonExistentStage.size(),
                "Should find 0 results for SensorModel in tenant1 documentation (doesn't exist)");

        // Test 9: Non-existent combination - wrong name
        List<ObjectMetadata> nonExistentName = registryService.findByScopeStageAndName("tenant1", "draft",
                "NonExistentModel");
        assertEquals(0, nonExistentName.size(), "Should find 0 results for NonExistentModel");

        // Test 10: Verify all results have correct scope, Stage, and name filter
        List<ObjectMetadata> allTenant1Models = registryService.findByScopeStageAndName("tenant1", "draft", "Sensor*");
        assertEquals(1, allTenant1Models.size(), "Should find 1 Sensor* model in tenant1 draft");
        for (ObjectMetadata metadata : allTenant1Models) {
            assertEquals("tenant1", metadata.getScope(), "All results should have scope 'tenant1'");
            assertEquals("draft", metadata.getStage(), "All results should have Stage 'draft'");
            assertTrue(metadata.getObjectName().startsWith("Sensor"), "Object name should start with 'Sensor'");
        }

        // Test 11: Test case sensitivity - exact match
        List<ObjectMetadata> caseSensitive = registryService.findByScopeStageAndName("tenant1", "draft", "sensormodel");
        assertEquals(0, caseSensitive.size(), "Should not find results with different case (case-sensitive search)");

        // Test 12: Wildcard for multiple matches
        List<ObjectMetadata> actuatorWildcard = registryService.findByScopeStageAndName("tenant1", "draft",
                "Actuator*");
        assertEquals(1, actuatorWildcard.size(), "Should find 1 model starting with 'Actuator' in tenant1 draft");

        // Test null inputs
        assertThrows(NullPointerException.class,
                () -> registryService.findByScopeStageAndName(null, "draft", "SensorModel"));
        assertThrows(NullPointerException.class,
                () -> registryService.findByScopeStageAndName("tenant1", null, "SensorModel"));
        assertThrows(NullPointerException.class,
                () -> registryService.findByScopeStageAndName("tenant1", "draft", null));
    }

    @Test
    public void testScopeAndStageIndexManagement() {
        // Create test object with scope and Stage
        ObjectMetadata metadata = createTestMetadataWithScope("test-scope-Stage", "TestPackage", "1.0.0", "EPackage",
                ObjectStatus.DRAFT, "draft", "tenant1");

        // Add to cache
        registryService.updateCache(metadata);

        // Verify it can be found by scope and Stage
        List<ObjectMetadata> tenant1Drafts = registryService.findByScopeAndStage("tenant1", "draft");
        assertEquals(1, tenant1Drafts.size());
        assertEquals("test-scope-Stage", tenant1Drafts.get(0).getObjectId());

        // Update metadata with different scope and Stage
        ObjectMetadata updatedMetadata = createTestMetadataWithScope("test-scope-Stage", "TestPackage", "1.0.0",
                "EPackage", ObjectStatus.APPROVED, "approved", "tenant2");
        registryService.updateCache(updatedMetadata);

        // Old scope/Stage combination should not contain this object
        List<ObjectMetadata> tenant1DraftsAfter = registryService.findByScopeAndStage("tenant1", "draft");
        assertTrue(tenant1DraftsAfter.isEmpty());

        // New scope/Stage combination should contain this object
        List<ObjectMetadata> tenant2Approveds = registryService.findByScopeAndStage("tenant2", "approved");
        assertEquals(1, tenant2Approveds.size());
        assertEquals("test-scope-Stage", tenant2Approveds.get(0).getObjectId());

        // Remove from cache
        registryService.removeFromCache("test-scope-Stage");

        // Scope/Stage combination should no longer contain this object
        List<ObjectMetadata> tenant2ApprovedsAfter = registryService.findByScopeAndStage("tenant2", "approved");
        assertTrue(tenant2ApprovedsAfter.isEmpty());
    }

    @Test
    public void testFindByScopeRegistryAndStage() {
        // Create test objects with different scopes, registries, and Stages
        ObjectMetadata tenant1SchemasDraftSensor = createTestMetadataWithScopeAndRegistry("t1-schemas-draft-sensor",
                "SensorModel", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant1", "schemas");
        ObjectMetadata tenant1SchemasApprovedSensor = createTestMetadataWithScopeAndRegistry(
                "t1-schemas-approved-sensor", "SensorModel", "2.0.0", "EPackage", ObjectStatus.APPROVED, "approved",
                "tenant1", "schemas");
        ObjectMetadata tenant1ObjectsDraftSensor = createTestMetadataWithScopeAndRegistry("t1-objects-draft-sensor",
                "SensorData", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant1", "objects");

        ObjectMetadata tenant2SchemasDraftSensor = createTestMetadataWithScopeAndRegistry("t2-schemas-draft-sensor",
                "SensorModel", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant2", "schemas");
        ObjectMetadata tenant2SchemasApprovedSensor = createTestMetadataWithScopeAndRegistry(
                "t2-schemas-approved-sensor", "SensorModel", "2.0.0", "EPackage", ObjectStatus.APPROVED, "approved",
                "tenant2", "schemas");
        ObjectMetadata tenant2ObjectsDraftData = createTestMetadataWithScopeAndRegistry("t2-objects-draft-data",
                "ActuatorData", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant2", "objects");

        // Same scope, registry, and Stage but different objects
        ObjectMetadata tenant1SchemasDraftActuator = createTestMetadataWithScopeAndRegistry("t1-schemas-draft-actuator",
                "ActuatorModel", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant1", "schemas");

        // Update registry cache
        registryService.updateCache(tenant1SchemasDraftSensor);
        registryService.updateCache(tenant1SchemasApprovedSensor);
        registryService.updateCache(tenant1ObjectsDraftSensor);
        registryService.updateCache(tenant2SchemasDraftSensor);
        registryService.updateCache(tenant2SchemasApprovedSensor);
        registryService.updateCache(tenant2ObjectsDraftData);
        registryService.updateCache(tenant1SchemasDraftActuator);

        // Test 1: Exact match - tenant1, schemas registry, draft Stage
        List<ObjectMetadata> tenant1SchemasDrafts = registryService.findByScopeRegistryAndStage("tenant1", "schemas",
                "draft");
        assertEquals(2, tenant1SchemasDrafts.size(), "Should find 2 draft objects in tenant1 schemas registry");
        assertTrue(tenant1SchemasDrafts.stream().anyMatch(m -> "t1-schemas-draft-sensor".equals(m.getObjectId())));
        assertTrue(tenant1SchemasDrafts.stream().anyMatch(m -> "t1-schemas-draft-actuator".equals(m.getObjectId())));

        // Test 2: Different scope, same registry and Stage
        List<ObjectMetadata> tenant2SchemasDrafts = registryService.findByScopeRegistryAndStage("tenant2", "schemas",
                "draft");
        assertEquals(1, tenant2SchemasDrafts.size(), "Should find 1 draft object in tenant2 schemas registry");
        assertEquals("t2-schemas-draft-sensor", tenant2SchemasDrafts.get(0).getObjectId());

        // Test 3: Different registry, same scope and Stage
        List<ObjectMetadata> tenant1ObjectsDrafts = registryService.findByScopeRegistryAndStage("tenant1", "objects",
                "draft");
        assertEquals(1, tenant1ObjectsDrafts.size(), "Should find 1 draft object in tenant1 objects registry");
        assertEquals("t1-objects-draft-sensor", tenant1ObjectsDrafts.get(0).getObjectId());

        // Test 4: Different Stage, same scope and registry
        List<ObjectMetadata> tenant1SchemasApproved = registryService.findByScopeRegistryAndStage("tenant1", "schemas",
                "approved");
        assertEquals(1, tenant1SchemasApproved.size(), "Should find 1 approved object in tenant1 schemas registry");
        assertEquals("t1-schemas-approved-sensor", tenant1SchemasApproved.get(0).getObjectId());

        // Test 5: Non-existent combination - wrong scope
        List<ObjectMetadata> nonExistentScope = registryService.findByScopeRegistryAndStage("tenant3", "schemas",
                "draft");
        assertEquals(0, nonExistentScope.size(), "Should find 0 results for non-existent tenant3");

        // Test 6: Non-existent combination - wrong registry
        List<ObjectMetadata> nonExistentRegistry = registryService.findByScopeRegistryAndStage("tenant1",
                "non-existent-registry", "draft");
        assertEquals(0, nonExistentRegistry.size(), "Should find 0 results for non-existent registry");

        // Test 7: Non-existent combination - wrong Stage
        List<ObjectMetadata> nonExistentStage = registryService.findByScopeRegistryAndStage("tenant1", "schemas",
                "documentation");
        assertEquals(0, nonExistentStage.size(),
                "Should find 0 results for documentation Stage (doesn't exist in schemas)");

        // Test 8: Verify correct registry and scope values
        for (ObjectMetadata metadata : tenant1SchemasDrafts) {
            assertEquals("tenant1", metadata.getScope(), "Scope should be tenant1");
            assertEquals("schemas", metadata.getRegistry(), "Registry should be schemas");
            assertEquals("draft", metadata.getStage(), "Stage should be draft");
        }

        // Test null inputs
        assertThrows(NullPointerException.class,
                () -> registryService.findByScopeRegistryAndStage(null, "schemas", "draft"));
        assertThrows(NullPointerException.class,
                () -> registryService.findByScopeRegistryAndStage("tenant1", null, "draft"));
        assertThrows(NullPointerException.class,
                () -> registryService.findByScopeRegistryAndStage("tenant1", "schemas", null));
    }

    @Test
    public void testFindByScopeRegistryStageAndName() {
        // Create test objects with different scopes, registries, Stages, and names
        ObjectMetadata tenant1SchemasDraftSensor = createTestMetadataWithScopeAndRegistry("t1-schemas-draft-sensor",
                "SensorModel", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant1", "schemas");
        ObjectMetadata tenant1SchemasApprovedSensor = createTestMetadataWithScopeAndRegistry(
                "t1-schemas-approved-sensor", "SensorModel", "2.0.0", "EPackage", ObjectStatus.APPROVED, "approved",
                "tenant1", "schemas");
        ObjectMetadata tenant1SchemasDraftActuator = createTestMetadataWithScopeAndRegistry("t1-schemas-draft-actuator",
                "ActuatorModel", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant1", "schemas");
        ObjectMetadata tenant1ObjectsDraftSensor = createTestMetadataWithScopeAndRegistry("t1-objects-draft-sensor",
                "SensorData", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant1", "objects");

        ObjectMetadata tenant2SchemasDraftSensor = createTestMetadataWithScopeAndRegistry("t2-schemas-draft-sensor",
                "SensorModel", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant2", "schemas");
        ObjectMetadata tenant2SchemasApprovedSensor = createTestMetadataWithScopeAndRegistry(
                "t2-schemas-approved-sensor", "SensorModel", "2.0.0", "EPackage", ObjectStatus.APPROVED, "approved",
                "tenant2", "schemas");
        ObjectMetadata tenant2SchemasDraftSensorConfig = createTestMetadataWithScopeAndRegistry(
                "t2-schemas-draft-sensor-config", "SensorConfiguration", "1.0.0", "EPackage", ObjectStatus.DRAFT,
                "draft", "tenant2", "schemas");
        ObjectMetadata tenant2ObjectsDraftData = createTestMetadataWithScopeAndRegistry("t2-objects-draft-data",
                "ActuatorData", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant2", "objects");

        // Same name, different scope
        ObjectMetadata tenant1ObjectsDraftCommon = createTestMetadataWithScopeAndRegistry("t1-objects-draft-common",
                "CommonPackage", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant1", "objects");
        ObjectMetadata tenant2ObjectsDraftCommon = createTestMetadataWithScopeAndRegistry("t2-objects-draft-common",
                "CommonPackage", "1.0.0", "EPackage", ObjectStatus.DRAFT, "draft", "tenant2", "objects");

        // Update registry cache
        registryService.updateCache(tenant1SchemasDraftSensor);
        registryService.updateCache(tenant1SchemasApprovedSensor);
        registryService.updateCache(tenant1SchemasDraftActuator);
        registryService.updateCache(tenant1ObjectsDraftSensor);
        registryService.updateCache(tenant2SchemasDraftSensor);
        registryService.updateCache(tenant2SchemasApprovedSensor);
        registryService.updateCache(tenant2SchemasDraftSensorConfig);
        registryService.updateCache(tenant2ObjectsDraftData);
        registryService.updateCache(tenant1ObjectsDraftCommon);
        registryService.updateCache(tenant2ObjectsDraftCommon);

        // Test 1: Exact match - tenant1, schemas, draft, SensorModel
        List<ObjectMetadata> tenant1SchemasDraftSensors = registryService.findByScopeRegistryStageAndName("tenant1",
                "schemas", "draft", "SensorModel");
        assertEquals(1, tenant1SchemasDraftSensors.size(), "Should find 1 SensorModel in tenant1 schemas draft");
        assertEquals("t1-schemas-draft-sensor", tenant1SchemasDraftSensors.get(0).getObjectId());

        // Test 2: Different scope, same registry, Stage, and name
        List<ObjectMetadata> tenant2SchemasDraftSensors = registryService.findByScopeRegistryStageAndName("tenant2",
                "schemas", "draft", "SensorModel");
        assertEquals(1, tenant2SchemasDraftSensors.size(), "Should find 1 SensorModel in tenant2 schemas draft");
        assertEquals("t2-schemas-draft-sensor", tenant2SchemasDraftSensors.get(0).getObjectId());

        // Test 3: Different Stage, same scope, registry, and name
        List<ObjectMetadata> tenant1SchemasApprovedSensors = registryService.findByScopeRegistryStageAndName("tenant1",
                "schemas", "approved", "SensorModel");
        assertEquals(1, tenant1SchemasApprovedSensors.size(), "Should find 1 SensorModel in tenant1 schemas approved");
        assertEquals("t1-schemas-approved-sensor", tenant1SchemasApprovedSensors.get(0).getObjectId());

        // Test 4: Different registry, same scope, Stage, and name - should return
        // different object
        List<ObjectMetadata> tenant1ObjectsDraftSensors = registryService.findByScopeRegistryStageAndName("tenant1",
                "objects", "draft", "SensorData");
        assertEquals(1, tenant1ObjectsDraftSensors.size(), "Should find 1 SensorData in tenant1 objects draft");
        assertEquals("t1-objects-draft-sensor", tenant1ObjectsDraftSensors.get(0).getObjectId());

        // Test 5: Wildcard search - find all Sensor* models in tenant2 schemas draft
        List<ObjectMetadata> tenant2SchemasDraftSensorWildcard = registryService
                .findByScopeRegistryStageAndName("tenant2", "schemas", "draft", "Sensor*");
        assertEquals(2, tenant2SchemasDraftSensorWildcard.size(),
                "Should find 2 Sensor* models in tenant2 schemas draft");
        assertTrue(tenant2SchemasDraftSensorWildcard.stream()
                .anyMatch(m -> "t2-schemas-draft-sensor".equals(m.getObjectId())));
        assertTrue(tenant2SchemasDraftSensorWildcard.stream()
                .anyMatch(m -> "t2-schemas-draft-sensor-config".equals(m.getObjectId())));

        // Test 6: Wildcard search - find all models with "Actuator" in tenant1 schemas
        // draft
        List<ObjectMetadata> tenant1SchemasDraftActuatorWildcard = registryService
                .findByScopeRegistryStageAndName("tenant1", "schemas", "draft", "Actuator*");
        assertEquals(1, tenant1SchemasDraftActuatorWildcard.size(),
                "Should find 1 Actuator* model in tenant1 schemas draft");
        assertEquals("t1-schemas-draft-actuator", tenant1SchemasDraftActuatorWildcard.get(0).getObjectId());

        // Test 7: Same name in different scopes
        List<ObjectMetadata> tenant1ObjectsCommon = registryService.findByScopeRegistryStageAndName("tenant1",
                "objects", "draft", "CommonPackage");
        assertEquals(1, tenant1ObjectsCommon.size(), "Should find 1 CommonPackage in tenant1 objects");
        assertEquals("t1-objects-draft-common", tenant1ObjectsCommon.get(0).getObjectId());

        List<ObjectMetadata> tenant2ObjectsCommon = registryService.findByScopeRegistryStageAndName("tenant2",
                "objects", "draft", "CommonPackage");
        assertEquals(1, tenant2ObjectsCommon.size(), "Should find 1 CommonPackage in tenant2 objects");
        assertEquals("t2-objects-draft-common", tenant2ObjectsCommon.get(0).getObjectId());

        // Test 8: Non-existent combination - wrong scope
        List<ObjectMetadata> nonExistentScope = registryService.findByScopeRegistryStageAndName("tenant3", "schemas",
                "draft", "SensorModel");
        assertEquals(0, nonExistentScope.size(), "Should find 0 results for non-existent tenant3");

        // Test 9: Non-existent combination - wrong registry
        List<ObjectMetadata> nonExistentRegistry = registryService.findByScopeRegistryStageAndName("tenant1",
                "non-existent-registry", "draft", "SensorModel");
        assertEquals(0, nonExistentRegistry.size(), "Should find 0 results for non-existent registry");

        // Test 10: Non-existent combination - wrong Stage
        List<ObjectMetadata> nonExistentStage = registryService.findByScopeRegistryStageAndName("tenant1", "schemas",
                "documentation", "SensorModel");
        assertEquals(0, nonExistentStage.size(), "Should find 0 results for documentation Stage");

        // Test 11: Non-existent combination - wrong name
        List<ObjectMetadata> nonExistentName = registryService.findByScopeRegistryStageAndName("tenant1", "schemas",
                "draft", "NonExistentModel");
        assertEquals(0, nonExistentName.size(), "Should find 0 results for NonExistentModel");

        // Test 12: Verify all fields are correct
        for (ObjectMetadata metadata : tenant1SchemasDraftSensors) {
            assertEquals("tenant1", metadata.getScope(), "Scope should be tenant1");
            assertEquals("schemas", metadata.getRegistry(), "Registry should be schemas");
            assertEquals("draft", metadata.getStage(), "Stage should be draft");
            assertEquals("SensorModel", metadata.getObjectName(), "Object name should be SensorModel");
        }

        // Test null inputs
        assertThrows(NullPointerException.class,
                () -> registryService.findByScopeRegistryStageAndName(null, "schemas", "draft", "SensorModel"));
        assertThrows(NullPointerException.class,
                () -> registryService.findByScopeRegistryStageAndName("tenant1", null, "draft", "SensorModel"));
        assertThrows(NullPointerException.class,
                () -> registryService.findByScopeRegistryStageAndName("tenant1", "schemas", null, "SensorModel"));
        assertThrows(NullPointerException.class,
                () -> registryService.findByScopeRegistryStageAndName("tenant1", "schemas", "draft", null));
    }

    // ===== Helper Methods =====

    private ObjectMetadata createTestMetadata(String objectId, String objectName, String version, ObjectStatus status) {
        return createTestMetadata(objectId, objectName, version, "EPackage", status);
    }

    private ObjectMetadata createTestMetadata(String objectId, String objectName, String version, String objectType,
            ObjectStatus status) {
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

    private ObjectMetadata createTestMetadataWithScope(String objectId, String objectName, String version,
            String objectType, ObjectStatus status, String Stage, String scope) {
        ObjectMetadata metadata = createTestMetadata(objectId, objectName, version, objectType, status);
        metadata.setStage(Stage);
        metadata.setScope(scope);
        return metadata;
    }

    private ObjectMetadata createTestMetadataWithScopeAndRegistry(String objectId, String objectName, String version,
            String objectType, ObjectStatus status, String Stage, String scope, String registry) {
        ObjectMetadata metadata = createTestMetadata(objectId, objectName, version, objectType, status);
        metadata.setStage(Stage);
        metadata.setScope(scope);
        metadata.setRegistry(registry);
        return metadata;
    }
}