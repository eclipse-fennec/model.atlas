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
package org.eclipse.fennec.model.atlas.management.lucene.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.model.atlas.management.lucene.tests.annotations.LuceneTestAnnotations;
import org.eclipse.fennec.model.atlas.management.lucene.tests.annotations.LuceneTestAnnotations.RegistryConfiguration;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for missing LuceneEObjectRegistryService interface methods.
 * 
 * <p>This test class focuses on interface methods that weren't covered in the main
 * LuceneRegistryServiceTest, providing comprehensive coverage of:</p>
 * 
 * <ul>
 * <li><strong>Version Operations</strong> - findByVersion, findByVersionPattern</li>
 * <li><strong>Fingerprint Queries</strong> - findByFingerprint for AI generation tracking</li>
 * <li><strong>Approval Workflow</strong> - findPendingApproval delegation</li>
 * <li><strong>Time-based Queries</strong> - findRecentlyModified with sorting and limits</li>
 * <li><strong>Type and Status Combinations</strong> - complex query scenarios</li>
 * </ul>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class LuceneRegistryInterfaceMethodsTest {

    @TempDir
    static Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Set system property for @RegistryConfiguration annotation
        System.setProperty(LuceneTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testFindByVersion(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {
        
        // Create test objects with different versions
        ObjectMetadata v1_0_0 = createTestMetadata("pkg-1", "TestPackage", "1.0.0", ObjectStatus.APPROVED, "approved");
        ObjectMetadata v1_0_1 = createTestMetadata("pkg-2", "TestPackage", "1.0.1", ObjectStatus.APPROVED, "approved");
        ObjectMetadata v2_0_0 = createTestMetadata("pkg-3", "TestPackage", "2.0.0", ObjectStatus.DRAFT, "draft");
        ObjectMetadata v1_0_0_dup = createTestMetadata("pkg-4", "AnotherPackage", "1.0.0", ObjectStatus.APPROVED, "approved");

        // Update registry
        registryService.updateCache(v1_0_0);
        registryService.updateCache(v1_0_1);
        registryService.updateCache(v2_0_0);
        registryService.updateCache(v1_0_0_dup);

        // Test version queries
        List<ObjectMetadata> version_1_0_0 = registryService.findByVersion("1.0.0");
        assertEquals(2, version_1_0_0.size(), "Should find 2 objects with version 1.0.0");
        assertTrue(version_1_0_0.stream().allMatch(m -> "1.0.0".equals(m.getVersion())));

        List<ObjectMetadata> version_1_0_1 = registryService.findByVersion("1.0.1");
        assertEquals(1, version_1_0_1.size(), "Should find 1 object with version 1.0.1");
        assertEquals("pkg-2", version_1_0_1.get(0).getObjectId());

        List<ObjectMetadata> version_2_0_0 = registryService.findByVersion("2.0.0");
        assertEquals(1, version_2_0_0.size(), "Should find 1 object with version 2.0.0");
        assertEquals("pkg-3", version_2_0_0.get(0).getObjectId());

        // Test non-existent version
        List<ObjectMetadata> version_3_0_0 = registryService.findByVersion("3.0.0");
        assertTrue(version_3_0_0.isEmpty(), "Should find no objects with version 3.0.0");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testFindByVersionPattern(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {
        
        // Create test objects with various version patterns
        ObjectMetadata v1_0_0 = createTestMetadata("pkg-1", "TestPackage", "1.0.0", ObjectStatus.APPROVED, "approved");
        ObjectMetadata v1_0_1 = createTestMetadata("pkg-2", "TestPackage", "1.0.1", ObjectStatus.APPROVED, "approved");
        ObjectMetadata v1_1_0 = createTestMetadata("pkg-3", "TestPackage", "1.1.0", ObjectStatus.APPROVED, "approved");
        ObjectMetadata v2_0_0 = createTestMetadata("pkg-4", "TestPackage", "2.0.0", ObjectStatus.DRAFT, "draft");
        ObjectMetadata v2_1_5 = createTestMetadata("pkg-5", "TestPackage", "2.1.5", ObjectStatus.DRAFT, "draft");

        // Update registry
        registryService.updateCache(v1_0_0);
        registryService.updateCache(v1_0_1);
        registryService.updateCache(v1_1_0);
        registryService.updateCache(v2_0_0);
        registryService.updateCache(v2_1_5);

        // Test version pattern queries
        List<ObjectMetadata> version_1_x = registryService.findByVersionPattern("1.*");
        assertEquals(3, version_1_x.size(), "Should find 3 objects with version 1.*");
        assertTrue(version_1_x.stream().allMatch(m -> m.getVersion().startsWith("1.")));

        List<ObjectMetadata> version_1_0_x = registryService.findByVersionPattern("1.0.*");
        assertEquals(2, version_1_0_x.size(), "Should find 2 objects with version 1.0.*");
        assertTrue(version_1_0_x.stream().allMatch(m -> m.getVersion().startsWith("1.0.")));

        List<ObjectMetadata> version_2_x = registryService.findByVersionPattern("2.*");
        assertEquals(2, version_2_x.size(), "Should find 2 objects with version 2.*");
        assertTrue(version_2_x.stream().allMatch(m -> m.getVersion().startsWith("2.")));

        // Test exact match (no wildcard)
        List<ObjectMetadata> version_exact = registryService.findByVersionPattern("1.0.0");
        assertEquals(1, version_exact.size(), "Should find 1 object with exact version 1.0.0");
        assertEquals("pkg-1", version_exact.get(0).getObjectId());

        // Test non-matching pattern
        List<ObjectMetadata> version_3_x = registryService.findByVersionPattern("3.*");
        assertTrue(version_3_x.isEmpty(), "Should find no objects with version 3.*");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testFindByFingerprint(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {
        
        // Create test objects with generation trigger fingerprints (AI generation use case)
        ObjectMetadata aiGenerated1 = createTestMetadata("ai-pkg-1", "AIGeneratedPackage1", "1.0.0", ObjectStatus.DRAFT, "draft");
        aiGenerated1.setGenerationTriggerFingerprint("fp_abc123def456");
        aiGenerated1.setSourceChannel("AI_GENERATOR");

        ObjectMetadata aiGenerated2 = createTestMetadata("ai-pkg-2", "AIGeneratedPackage2", "1.0.0", ObjectStatus.DRAFT, "draft");
        aiGenerated2.setGenerationTriggerFingerprint("fp_xyz789abc123");
        aiGenerated2.setSourceChannel("AI_GENERATOR");

        ObjectMetadata manual = createTestMetadata("manual-pkg", "ManualPackage", "1.0.0", ObjectStatus.APPROVED, "approved");
        // No fingerprint for manually created packages

        // Update registry
        registryService.updateCache(aiGenerated1);
        registryService.updateCache(aiGenerated2);
        registryService.updateCache(manual);

        // Test fingerprint queries
        Optional<ObjectMetadata> found1 = registryService.findByFingerprint("fp_abc123def456");
        assertTrue(found1.isPresent(), "Should find AI generated package with fingerprint fp_abc123def456");
        assertEquals("ai-pkg-1", found1.get().getObjectId());
        assertEquals("AI_GENERATOR", found1.get().getSourceChannel());

        Optional<ObjectMetadata> found2 = registryService.findByFingerprint("fp_xyz789abc123");
        assertTrue(found2.isPresent(), "Should find AI generated package with fingerprint fp_xyz789abc123");
        assertEquals("ai-pkg-2", found2.get().getObjectId());

        // Test non-existent fingerprint
        Optional<ObjectMetadata> notFound = registryService.findByFingerprint("fp_nonexistent");
        assertFalse(notFound.isPresent(), "Should not find package with non-existent fingerprint");

        // Test empty string fingerprint 
        Optional<ObjectMetadata> emptyFingerprint = registryService.findByFingerprint("");
        assertFalse(emptyFingerprint.isPresent(), "Should not find package with empty fingerprint");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testFindPendingApproval(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {
        
        // Create test objects with different statuses
        ObjectMetadata draft1 = createTestMetadata("draft-1", "DraftPackage1", "1.0.0", ObjectStatus.DRAFT, "draft");
        ObjectMetadata draft2 = createTestMetadata("draft-2", "DraftPackage2", "1.0.0", ObjectStatus.DRAFT, "draft");
        ObjectMetadata approved = createTestMetadata("approved-1", "ApprovedPackage", "1.0.0", ObjectStatus.APPROVED, "approved");
        ObjectMetadata rejected = createTestMetadata("rejected-1", "RejectedPackage", "1.0.0", ObjectStatus.REJECTED, "rejected");

        // Update registry
        registryService.updateCache(draft1);
        registryService.updateCache(draft2);
        registryService.updateCache(approved);
        registryService.updateCache(rejected);

        // Test findPendingApproval (should delegate to findByStatus(DRAFT))
        List<ObjectMetadata> pendingApproval = registryService.findPendingApproval();
        assertEquals(2, pendingApproval.size(), "Should find 2 objects pending approval");
        assertTrue(pendingApproval.stream().allMatch(m -> ObjectStatus.DRAFT.equals(m.getStatus())));
        
        // Verify it's the same as calling findByStatus(DRAFT) directly
        List<ObjectMetadata> draftStatus = registryService.findByStatus(ObjectStatus.DRAFT);
        assertEquals(pendingApproval.size(), draftStatus.size(), "findPendingApproval should equal findByStatus(DRAFT)");
        assertTrue(pendingApproval.containsAll(draftStatus), "Results should be identical");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testFindRecentlyModified(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {
        
        Instant now = Instant.now();
        
        // Create test objects with different modification times
        ObjectMetadata recent1 = createTestMetadata("recent-1", "RecentPackage1", "1.0.0", ObjectStatus.DRAFT, "draft");
        recent1.setLastChangeTime(now.minus(1, ChronoUnit.HOURS));

        ObjectMetadata recent2 = createTestMetadata("recent-2", "RecentPackage2", "1.0.0", ObjectStatus.APPROVED, "approved");
        recent2.setLastChangeTime(now.minus(30, ChronoUnit.MINUTES));

        ObjectMetadata recent3 = createTestMetadata("recent-3", "RecentPackage3", "1.0.0", ObjectStatus.DRAFT, "draft");
        recent3.setLastChangeTime(now.minus(15, ChronoUnit.MINUTES));

        ObjectMetadata old = createTestMetadata("old-1", "OldPackage", "1.0.0", ObjectStatus.APPROVED, "approved");
        old.setLastChangeTime(now.minus(2, ChronoUnit.DAYS));

        ObjectMetadata noTime = createTestMetadata("no-time", "NoTimePackage", "1.0.0", ObjectStatus.DRAFT, "draft");
        noTime.setLastChangeTime(null); // Explicitly set to null to override the helper method

        // Update registry
        registryService.updateCache(recent1);
        registryService.updateCache(recent2);
        registryService.updateCache(recent3);
        registryService.updateCache(old);
        registryService.updateCache(noTime);

        // Test recently modified within last 2 hours
        Instant sinceTime = now.minus(2, ChronoUnit.HOURS);
        List<ObjectMetadata> recentlyModified = registryService.findRecentlyModified(sinceTime, 10);
        
        assertEquals(3, recentlyModified.size(), "Should find 3 recently modified objects");
        assertTrue(recentlyModified.stream().allMatch(m -> 
            m.getLastChangeTime() != null && m.getLastChangeTime().isAfter(sinceTime)));

        // Verify sorting (most recent first)
        assertEquals("recent-3", recentlyModified.get(0).getObjectId(), "Most recent should be first");
        assertEquals("recent-2", recentlyModified.get(1).getObjectId(), "Second most recent should be second");
        assertEquals("recent-1", recentlyModified.get(2).getObjectId(), "Third most recent should be third");

        // Test with limit
        List<ObjectMetadata> limitedResults = registryService.findRecentlyModified(sinceTime, 2);
        assertEquals(2, limitedResults.size(), "Should respect maxResults limit");
        assertEquals("recent-3", limitedResults.get(0).getObjectId(), "Most recent should still be first");
        assertEquals("recent-2", limitedResults.get(1).getObjectId(), "Second most recent should still be second");

        // Test with stricter time filter
        Instant stricterTime = now.minus(45, ChronoUnit.MINUTES);
        List<ObjectMetadata> stricterResults = registryService.findRecentlyModified(stricterTime, 10);
        assertEquals(2, stricterResults.size(), "Should find only 2 objects modified in last 45 minutes");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testFindByStatusAndType(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {
        
        // Create test objects with different types and statuses
        ObjectMetadata draftPackage = createTestMetadata("draft-pkg", "DraftPackage", "1.0.0", ObjectStatus.DRAFT, "draft");
        draftPackage.setObjectType("EPackage");

        ObjectMetadata draftRoute = createTestMetadata("draft-route", "DraftRoute", "1.0.0", ObjectStatus.DRAFT, "draft");
        draftRoute.setObjectType("Route");

        ObjectMetadata approvedPackage = createTestMetadata("approved-pkg", "ApprovedPackage", "1.0.0", ObjectStatus.APPROVED, "approved");
        approvedPackage.setObjectType("EPackage");

        ObjectMetadata approvedRoute = createTestMetadata("approved-route", "ApprovedRoute", "1.0.0", ObjectStatus.APPROVED, "approved");
        approvedRoute.setObjectType("Route");

        // Update registry
        registryService.updateCache(draftPackage);
        registryService.updateCache(draftRoute);
        registryService.updateCache(approvedPackage);
        registryService.updateCache(approvedRoute);

        // Test status and type combinations
        List<ObjectMetadata> draftPackages = registryService.findByStatusAndType(ObjectStatus.DRAFT, "EPackage");
        assertEquals(1, draftPackages.size(), "Should find 1 DRAFT EPackage");
        assertEquals("draft-pkg", draftPackages.get(0).getObjectId());

        List<ObjectMetadata> draftRoutes = registryService.findByStatusAndType(ObjectStatus.DRAFT, "Route");
        assertEquals(1, draftRoutes.size(), "Should find 1 DRAFT Route");
        assertEquals("draft-route", draftRoutes.get(0).getObjectId());

        List<ObjectMetadata> approvedPackages = registryService.findByStatusAndType(ObjectStatus.APPROVED, "EPackage");
        assertEquals(1, approvedPackages.size(), "Should find 1 APPROVED EPackage");
        assertEquals("approved-pkg", approvedPackages.get(0).getObjectId());

        List<ObjectMetadata> approvedRoutes = registryService.findByStatusAndType(ObjectStatus.APPROVED, "Route");
        assertEquals(1, approvedRoutes.size(), "Should find 1 APPROVED Route");
        assertEquals("approved-route", approvedRoutes.get(0).getObjectId());

        // Test non-existent combinations
        List<ObjectMetadata> rejectedPackages = registryService.findByStatusAndType(ObjectStatus.REJECTED, "EPackage");
        assertTrue(rejectedPackages.isEmpty(), "Should find no REJECTED EPackages");

        List<ObjectMetadata> draftSensors = registryService.findByStatusAndType(ObjectStatus.DRAFT, "SensorModel");
        assertTrue(draftSensors.isEmpty(), "Should find no DRAFT SensorModels");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testFindByObjectType(
        @InjectService(filter = "(registry.type=shared)")
        EObjectRegistryService registryService
    ) throws Exception {
        
        // Create test objects with different types
        ObjectMetadata package1 = createTestMetadata("pkg-1", "Package1", "1.0.0", ObjectStatus.DRAFT, "draft");
        package1.setObjectType("EPackage");

        ObjectMetadata package2 = createTestMetadata("pkg-2", "Package2", "1.0.0", ObjectStatus.APPROVED, "approved");
        package2.setObjectType("EPackage");

        ObjectMetadata route1 = createTestMetadata("route-1", "Route1", "1.0.0", ObjectStatus.DRAFT, "draft");
        route1.setObjectType("Route");

        ObjectMetadata sensor1 = createTestMetadata("sensor-1", "Sensor1", "1.0.0", ObjectStatus.APPROVED, "approved");
        sensor1.setObjectType("SensorModel");

        // Update registry
        registryService.updateCache(package1);
        registryService.updateCache(package2);
        registryService.updateCache(route1);
        registryService.updateCache(sensor1);

        // Test type queries
        List<ObjectMetadata> packages = registryService.findByObjectType("EPackage");
        assertEquals(2, packages.size(), "Should find 2 EPackage objects");
        assertTrue(packages.stream().allMatch(m -> "EPackage".equals(m.getObjectType())));

        List<ObjectMetadata> routes = registryService.findByObjectType("Route");
        assertEquals(1, routes.size(), "Should find 1 Route object");
        assertEquals("route-1", routes.get(0).getObjectId());

        List<ObjectMetadata> sensors = registryService.findByObjectType("SensorModel");
        assertEquals(1, sensors.size(), "Should find 1 SensorModel object");
        assertEquals("sensor-1", sensors.get(0).getObjectId());

        // Test non-existent type
        List<ObjectMetadata> nonExistent = registryService.findByObjectType("NonExistentType");
        assertTrue(nonExistent.isEmpty(), "Should find no objects of non-existent type");
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