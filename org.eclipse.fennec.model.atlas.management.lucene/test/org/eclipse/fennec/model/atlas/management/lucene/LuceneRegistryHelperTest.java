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
package org.eclipse.fennec.model.atlas.management.lucene;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.management.lucene.registry.LuceneRegistryHelper;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for LuceneRegistryHelper.
 * 
 * <p>Tests the Lucene indexing functionality including document creation,
 * index updates, search operations, and index maintenance.</p>
 */
class LuceneRegistryHelperTest {

    @TempDir
    Path tempDir;

    private LuceneRegistryHelper helper;
    private ResourceSet resourceSet;
    private ManagementFactory factory;

    @BeforeEach
    void setUp() throws IOException {
        // Initialize EMF
        resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        resourceSet.getPackageRegistry().put(ManagementPackage.eNS_URI, ManagementPackage.eINSTANCE);
        
        factory = ManagementFactory.eINSTANCE;
        
        // Create helper with temp directory
        helper = new LuceneRegistryHelper(tempDir);
        helper.initialize();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (helper != null) {
            helper.close();
        }
    }

    @Test
    void testIndexCreation() {
        // Verify index directory is created
        Path indexPath = tempDir.resolve(".lucene-index");
        assertTrue(Files.exists(indexPath), "Index directory should be created");
        assertTrue(Files.isDirectory(indexPath), "Index path should be a directory");
    }

    @Test
    void testSaveAndIndexMetadata() throws Exception {
        // Create test metadata
        ObjectMetadata metadata = createTestMetadata("testUser", "AI_GENERATOR", "EPackage");
        
        // Update index (registry helper only handles indexing, not file storage)
        helper.updateIndex("test-obj-1", metadata);
        
        // Search for the indexed object
        List<String> results = helper.searchObjectIds("uploadUser:testUser", 10);
        assertEquals(1, results.size(), "Should find one object");
        assertEquals("test-obj-1", results.get(0), "Should find correct object ID");
    }

    @Test
    void testSearchByDifferentFields() throws Exception {
        // Create multiple test objects
        ObjectMetadata metadata1 = createTestMetadata("alice", "AI_GENERATOR", "EPackage");
        ObjectMetadata metadata2 = createTestMetadata("bob", "MANUAL_UPLOAD", "Route");
        ObjectMetadata metadata3 = createTestMetadata("charlie", "AI_GENERATOR", "SensorModel");
        
        helper.updateIndex("obj-1", metadata1);
        helper.updateIndex("obj-2", metadata2);
        helper.updateIndex("obj-3", metadata3);
        
        // Test search by upload user
        List<String> userResults = helper.searchObjectIds("uploadUser:alice", 10);
        assertEquals(1, userResults.size());
        assertEquals("obj-1", userResults.get(0));
        
        // Test search by source channel
        List<String> channelResults = helper.searchObjectIds("sourceChannel:AI_GENERATOR", 10);
        assertEquals(2, channelResults.size());
        assertTrue(channelResults.contains("obj-1"));
        assertTrue(channelResults.contains("obj-3"));
        
        // Test search by object type
        List<String> typeResults = helper.searchObjectIds("objectType:Route", 10);
        assertEquals(1, typeResults.size());
        assertEquals("obj-2", typeResults.get(0));
        
        // Test combined search
        List<String> combinedResults = helper.searchObjectIds("sourceChannel:AI_GENERATOR AND objectType:EPackage", 10);
        assertEquals(1, combinedResults.size());
        assertEquals("obj-1", combinedResults.get(0));
    }

    @Test
    void testSearchByTimeRange() throws Exception {
        Instant now = Instant.now();
        Instant past = now.minus(1, ChronoUnit.HOURS);
        Instant future = now.plus(1, ChronoUnit.HOURS);
        
        // Create metadata with specific timestamps
        ObjectMetadata oldMetadata = createTestMetadata("user1", "AI_GENERATOR", "EPackage");
        oldMetadata.setUploadTime(past);
        
        ObjectMetadata newMetadata = createTestMetadata("user2", "AI_GENERATOR", "EPackage");
        newMetadata.setUploadTime(now);
        
        helper.updateIndex("old-obj", oldMetadata);
        helper.updateIndex("new-obj", newMetadata);
        
        // Search for objects uploaded in the last 30 minutes
        Instant thirtyMinutesAgo = now.minus(30, ChronoUnit.MINUTES);
        
        List<String> recentResults = helper.searchByUploadTimeRange(thirtyMinutesAgo, future, 10);
        assertEquals(1, recentResults.size());
        assertEquals("new-obj", recentResults.get(0));
    }

    @Test
    void testSearchCustomProperties() throws Exception {
        // Create metadata with custom properties
        ObjectMetadata metadata = createTestMetadata("propUser", "AI_GENERATOR", "EPackage");
        metadata.getProperties().put("version", "1.0");
        metadata.getProperties().put("namespace", "sensors");
        metadata.getProperties().put("author", "system");
        
        helper.updateIndex("prop-obj", metadata);
        
        // Search by property value
        List<String> versionResults = helper.searchObjectIds("properties:version\\:1.0", 10);
        assertEquals(1, versionResults.size());
        assertEquals("prop-obj", versionResults.get(0));
        
        // Search by property namespace
        List<String> namespaceResults = helper.searchObjectIds("properties:namespace\\:sensors", 10);
        assertEquals(1, namespaceResults.size());
        assertEquals("prop-obj", namespaceResults.get(0));
    }

    @Test
    void testDeleteAndRemoveFromIndex() throws Exception {
        // Create and save metadata
        ObjectMetadata metadata = createTestMetadata("deleteUser", "AI_GENERATOR", "EPackage");
        helper.updateIndex("delete-obj", metadata);
        
        // Verify it's indexed
        List<String> beforeDelete = helper.searchObjectIds("uploadUser:deleteUser", 10);
        assertEquals(1, beforeDelete.size());
        
        // Remove from index (registry helpers don't handle file deletion)
        helper.removeFromIndex("delete-obj");
        
        // Verify it's removed from index
        List<String> afterDelete = helper.searchObjectIds("uploadUser:deleteUser", 10);
        assertEquals(0, afterDelete.size(), "Object should not be found after deletion");
    }

    @Test
    void testIndexRebuild() throws Exception {
        // Create some metadata files manually (simulate existing files)
        ObjectMetadata metadata1 = createTestMetadata("rebuild1", "AI_GENERATOR", "EPackage");
        ObjectMetadata metadata2 = createTestMetadata("rebuild2", "MANUAL_UPLOAD", "Route");
        
        // Add metadata directly to index to simulate existing data
        helper.updateIndex("rebuild-obj-1", metadata1);
        helper.updateIndex("rebuild-obj-2", metadata2);
        
        // Close and recreate helper to trigger rebuild
        helper.close();
        helper = new LuceneRegistryHelper(tempDir);
        helper.initialize();
        
        // Verify both objects are indexed after rebuild
        List<String> results = helper.searchObjectIds("uploadUser:rebuild1 OR uploadUser:rebuild2", 10);
        assertEquals(2, results.size());
        assertTrue(results.contains("rebuild-obj-1"));
        assertTrue(results.contains("rebuild-obj-2"));
    }

    @Test
    void testSearchWithEmptyResults() throws Exception {
        // Search for non-existent user
        List<String> results = helper.searchObjectIds("uploadUser:nonexistent", 10);
        assertTrue(results.isEmpty(), "Should return empty results for non-existent user");
        
        // Search with invalid field
        List<String> invalidResults = helper.searchObjectIds("invalidField:value", 10);
        assertTrue(invalidResults.isEmpty(), "Should return empty results for invalid field");
    }

    @Test
    void testSearchWithMalformedQuery() throws Exception {
        // Test with malformed query - should not throw exception
        assertDoesNotThrow(() -> {
            helper.searchObjectIds("uploadUser:[invalid query", 10);
            // Should return empty or handle gracefully
        });
    }

    @Test
    void testUpdateExistingObject() throws Exception {
        // Create initial metadata
        ObjectMetadata initialMetadata = createTestMetadata("updateUser", "AI_GENERATOR", "EPackage");
        helper.updateIndex("update-obj", initialMetadata);
        
        // Verify initial indexing
        List<String> initialResults = helper.searchObjectIds("uploadUser:updateUser", 10);
        assertEquals(1, initialResults.size());
        
        // Update metadata
        ObjectMetadata updatedMetadata = createTestMetadata("updatedUser", "MANUAL_UPLOAD", "Route");
        helper.updateIndex("update-obj", updatedMetadata);
        
        // Verify old data is not found
        List<String> oldResults = helper.searchObjectIds("uploadUser:updateUser", 10);
        assertEquals(0, oldResults.size(), "Old data should not be found");
        
        // Verify new data is found
        List<String> newResults = helper.searchObjectIds("uploadUser:updatedUser", 10);
        assertEquals(1, newResults.size());
        assertEquals("update-obj", newResults.get(0));
    }

    @Test
    void testListObjectIds() throws Exception {
        // Create multiple objects
        helper.updateIndex("list-obj-1", createTestMetadata("user1", "AI_GENERATOR", "EPackage"));
        helper.updateIndex("list-obj-2", createTestMetadata("user2", "MANUAL_UPLOAD", "Route"));
        helper.updateIndex("list-obj-3", createTestMetadata("user3", "AI_GENERATOR", "SensorModel"));
        
        // List all object IDs
        List<String> allIds = helper.getAllObjectIds();
        assertEquals(3, allIds.size());
        assertTrue(allIds.contains("list-obj-1"));
        assertTrue(allIds.contains("list-obj-2"));
        assertTrue(allIds.contains("list-obj-3"));
    }

    @Test
    void testSearchWithSpecialCharacters() throws Exception {
        // Create metadata with special characters
        ObjectMetadata metadata = createTestMetadata("user@domain.com", "AI_GENERATOR", "EPackage");
        helper.updateIndex("special-obj", metadata);
        
        // Search should handle special characters properly
        // Note: @ character doesn't need escaping in Lucene
        List<String> results = helper.searchObjectIds("uploadUser:user@domain.com", 10);
        assertEquals(1, results.size());
        assertEquals("special-obj", results.get(0));
    }

    @Test
    void testSearchByNewFields() throws Exception {
        Instant now = Instant.now();
        Instant complianceTime = now.minus(30, ChronoUnit.MINUTES);
        Instant lastChangeTime = now.minus(15, ChronoUnit.MINUTES);
        
        // Create metadata with new fields
        ObjectMetadata metadata = createTestMetadata("newFieldUser", "AI_GENERATOR", "EPackage");
        metadata.setReviewUser("reviewer");
        metadata.setReviewTime(now.minus(20, ChronoUnit.MINUTES));
        metadata.setReviewReason("Quality check passed");
        metadata.setGenerationTriggerFingerprint("fp-abc123def456");
        metadata.setComplianceCheckTime(complianceTime);
        metadata.setComplianceStatus("COMPLIANT");
        metadata.setGovernanceDocumentationId("gov-doc-789");
        metadata.setLastChangeUser("admin");
        metadata.setLastChangeTime(lastChangeTime);
        
        helper.updateIndex("new-fields-obj", metadata);
        
        // Test search by generation trigger fingerprint
        List<String> fingerprintResults = helper.searchObjectIds("generationTriggerFingerprint:fp-abc123def456", 10);
        assertEquals(1, fingerprintResults.size());
        assertEquals("new-fields-obj", fingerprintResults.get(0));
        
        // Test search by compliance status
        List<String> complianceResults = helper.searchObjectIds("complianceStatus:COMPLIANT", 10);
        assertEquals(1, complianceResults.size());
        assertEquals("new-fields-obj", complianceResults.get(0));
        
        // Test search by governance documentation ID
        List<String> govResults = helper.searchObjectIds("governanceDocumentationId:gov-doc-789", 10);
        assertEquals(1, govResults.size());
        assertEquals("new-fields-obj", govResults.get(0));
        
        // Test search by last change user
        List<String> lastChangeResults = helper.searchObjectIds("lastChangeUser:admin", 10);
        assertEquals(1, lastChangeResults.size());
        assertEquals("new-fields-obj", lastChangeResults.get(0));
    }

    @Test
    void testSearchByComplianceCheckTimeRange() throws Exception {
        Instant now = Instant.now();
        Instant past = now.minus(2, ChronoUnit.HOURS);
        Instant recent = now.minus(30, ChronoUnit.MINUTES);
        Instant future = now.plus(1, ChronoUnit.HOURS);
        
        // Create metadata with different compliance check times
        ObjectMetadata oldCompliance = createTestMetadata("user1", "AI_GENERATOR", "EPackage");
        oldCompliance.setComplianceCheckTime(past);
        oldCompliance.setComplianceStatus("NON_COMPLIANT");
        
        ObjectMetadata recentCompliance = createTestMetadata("user2", "AI_GENERATOR", "EPackage");
        recentCompliance.setComplianceCheckTime(recent);
        recentCompliance.setComplianceStatus("COMPLIANT");
        
        helper.updateIndex("old-compliance", oldCompliance);
        helper.updateIndex("recent-compliance", recentCompliance);
        
        // Search for objects with compliance check in last hour
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        List<String> recentResults = helper.searchByComplianceCheckTimeRange(oneHourAgo, future, 10);
        assertEquals(1, recentResults.size());
        assertEquals("recent-compliance", recentResults.get(0));
        
        // Search for all compliance checks
        List<String> allResults = helper.searchByComplianceCheckTimeRange(past.minus(1, ChronoUnit.HOURS), future, 10);
        assertEquals(2, allResults.size());
        assertTrue(allResults.contains("old-compliance"));
        assertTrue(allResults.contains("recent-compliance"));
    }

    @Test
    void testSearchByLastChangeTimeRange() throws Exception {
        Instant now = Instant.now();
        Instant past = now.minus(2, ChronoUnit.HOURS);
        Instant recent = now.minus(10, ChronoUnit.MINUTES);
        Instant future = now.plus(1, ChronoUnit.HOURS);
        
        // Create metadata with different last change times
        ObjectMetadata oldChange = createTestMetadata("user1", "AI_GENERATOR", "EPackage");
        oldChange.setLastChangeUser("editor1");
        oldChange.setLastChangeTime(past);
        
        ObjectMetadata recentChange = createTestMetadata("user2", "AI_GENERATOR", "EPackage");
        recentChange.setLastChangeUser("editor2");
        recentChange.setLastChangeTime(recent);
        
        helper.updateIndex("old-change", oldChange);
        helper.updateIndex("recent-change", recentChange);
        
        // Search for objects changed in last 30 minutes
        Instant thirtyMinutesAgo = now.minus(30, ChronoUnit.MINUTES);
        List<String> recentResults = helper.searchByLastChangeTimeRange(thirtyMinutesAgo, future, 10);
        assertEquals(1, recentResults.size());
        assertEquals("recent-change", recentResults.get(0));
    }

    @Test
    void testSearchNewFieldsWithCombinedCriteria() throws Exception {
        // Create objects with different combinations of new fields
        ObjectMetadata compliantObj = createTestMetadata("user1", "AI_GENERATOR", "EPackage");
        compliantObj.setComplianceStatus("COMPLIANT");
        compliantObj.setLastChangeUser("admin");
        compliantObj.setGenerationTriggerFingerprint("fp-sensors-123");
        
        ObjectMetadata nonCompliantObj = createTestMetadata("user2", "AI_GENERATOR", "EPackage");
        nonCompliantObj.setComplianceStatus("NON_COMPLIANT");
        nonCompliantObj.setLastChangeUser("reviewer");
        nonCompliantObj.setGenerationTriggerFingerprint("fp-routes-456");
        
        helper.updateIndex("compliant-obj", compliantObj);
        helper.updateIndex("non-compliant-obj", nonCompliantObj);
        
        // Search for compliant objects by admin
        List<String> adminCompliantResults = helper.searchObjectIds(
            "complianceStatus:COMPLIANT AND lastChangeUser:admin", 10);
        assertEquals(1, adminCompliantResults.size());
        assertEquals("compliant-obj", adminCompliantResults.get(0));
        
        // Search for any AI-generated objects with compliance status
        List<String> aiComplianceResults = helper.searchObjectIds(
            "sourceChannel:AI_GENERATOR AND (complianceStatus:COMPLIANT OR complianceStatus:NON_COMPLIANT)", 10);
        assertEquals(2, aiComplianceResults.size());
        assertTrue(aiComplianceResults.contains("compliant-obj"));
        assertTrue(aiComplianceResults.contains("non-compliant-obj"));
    }

    @Test
    void testIndexRebuildWithNewFields() throws Exception {
        // Create metadata with new fields using parent helper
        ObjectMetadata metadata = createTestMetadata("rebuildUser", "AI_GENERATOR", "EPackage");
        metadata.setGenerationTriggerFingerprint("fp-rebuild-test");
        metadata.setComplianceStatus("PENDING");
        metadata.setLastChangeUser("system");
        metadata.setLastChangeTime(Instant.now());
        
        // Add metadata directly to index to simulate existing data
        helper.updateIndex("rebuild-new-fields", metadata);
        
        // Close and recreate helper to trigger rebuild
        helper.close();
        helper = new LuceneRegistryHelper(tempDir);
        helper.initialize();
        
        // Verify new fields are indexed after rebuild
        List<String> fingerprintResults = helper.searchObjectIds("generationTriggerFingerprint:fp-rebuild-test", 10);
        assertEquals(1, fingerprintResults.size());
        assertEquals("rebuild-new-fields", fingerprintResults.get(0));
        
        List<String> complianceResults = helper.searchObjectIds("complianceStatus:PENDING", 10);
        assertEquals(1, complianceResults.size());
        assertEquals("rebuild-new-fields", complianceResults.get(0));
        
        List<String> lastChangeResults = helper.searchObjectIds("lastChangeUser:system", 10);
        assertEquals(1, lastChangeResults.size());
        assertEquals("rebuild-new-fields", lastChangeResults.get(0));
    }

    @Test
    void testSearchByRole() throws Exception {
        // Create objects with different roles
        ObjectMetadata draftMetadata = createTestMetadata("user1", "AI_GENERATOR", "EPackage", "draft");
        ObjectMetadata approvedMetadata = createTestMetadata("user2", "MANUAL_UPLOAD", "Route", "approved");
        ObjectMetadata documentationMetadata = createTestMetadata("user3", "AI_GENERATOR", "SensorModel", "documentation");
        
        helper.updateIndex("draft-obj", draftMetadata);
        helper.updateIndex("approved-obj", approvedMetadata);
        helper.updateIndex("doc-obj", documentationMetadata);
        
        // Test search by role: draft
        List<String> draftResults = helper.searchObjectIds("role:draft", 10);
        assertEquals(1, draftResults.size());
        assertEquals("draft-obj", draftResults.get(0));
        
        // Test search by role: approved
        List<String> approvedResults = helper.searchObjectIds("role:approved", 10);
        assertEquals(1, approvedResults.size());
        assertEquals("approved-obj", approvedResults.get(0));
        
        // Test search by role: documentation
        List<String> docResults = helper.searchObjectIds("role:documentation", 10);
        assertEquals(1, docResults.size());
        assertEquals("doc-obj", docResults.get(0));
    }

    @Test
    void testSearchByObjectNameAndRole() throws Exception {
        // Create multiple objects with same name but different roles
        String objectName = "sensor-model-v1";
        ObjectMetadata draftMetadata = createTestMetadata("user1", "AI_GENERATOR", "SensorModel", "draft");
        draftMetadata.setObjectName(objectName);
        ObjectMetadata approvedMetadata = createTestMetadata("user2", "MANUAL_UPLOAD", "SensorModel", "approved");
        approvedMetadata.setObjectName(objectName);
        
        helper.updateIndex("sensor-draft", draftMetadata);
        helper.updateIndex("sensor-approved", approvedMetadata);
        
        // Test search by objectName and role
        List<String> draftResults = helper.searchObjectIds("(objectName:\"" + objectName + "\" AND role:draft)", 10);
        assertEquals(1, draftResults.size());
        assertEquals("sensor-draft", draftResults.get(0));
        
        List<String> approvedResults = helper.searchObjectIds("(objectName:\"" + objectName + "\" AND role:approved)", 10);
        assertEquals(1, approvedResults.size());
        assertEquals("sensor-approved", approvedResults.get(0));
        
        // Test search by objectName only (should find both)
        List<String> nameResults = helper.searchObjectIds("objectName:\"" + objectName + "\"", 10);
        assertEquals(2, nameResults.size());
        assertTrue(nameResults.contains("sensor-draft"));
        assertTrue(nameResults.contains("sensor-approved"));
    }

    @Test
    void testRoleFieldExactMatch() throws Exception {
        // Create objects with roles that might have partial matches
        ObjectMetadata draftMetadata = createTestMetadata("user1", "AI_GENERATOR", "EPackage", "draft");
        ObjectMetadata draftDocumentationMetadata = createTestMetadata("user2", "MANUAL_UPLOAD", "Route", "draft-documentation");
        
        helper.updateIndex("draft-obj", draftMetadata);
        helper.updateIndex("draft-doc-obj", draftDocumentationMetadata);
        
        // Search for exact role match "draft" - should only find draft object, not draft-documentation
        List<String> exactDraftResults = helper.searchObjectIds("role:draft", 10);
        assertEquals(1, exactDraftResults.size());
        assertEquals("draft-obj", exactDraftResults.get(0));
        
        // Search for exact role match "draft-documentation"
        List<String> draftDocResults = helper.searchObjectIds("role:draft-documentation", 10);
        assertEquals(1, draftDocResults.size());
        assertEquals("draft-doc-obj", draftDocResults.get(0));
    }

    @Test
    void testRoleWithOtherFieldCombinations() throws Exception {
        // Create objects for combination testing
        ObjectMetadata epackageDraft = createTestMetadata("alice", "AI_GENERATOR", "EPackage", "draft");
        ObjectMetadata epackageApproved = createTestMetadata("alice", "AI_GENERATOR", "EPackage", "approved");
        ObjectMetadata routeDraft = createTestMetadata("alice", "MANUAL_UPLOAD", "Route", "draft");
        
        helper.updateIndex("epackage-draft", epackageDraft);
        helper.updateIndex("epackage-approved", epackageApproved);
        helper.updateIndex("route-draft", routeDraft);
        
        // Test role + objectType combination
        List<String> epackageDraftResults = helper.searchObjectIds("role:draft AND objectType:EPackage", 10);
        assertEquals(1, epackageDraftResults.size());
        assertEquals("epackage-draft", epackageDraftResults.get(0));
        
        // Test role + uploadUser combination
        List<String> aliceDraftResults = helper.searchObjectIds("role:draft AND uploadUser:alice", 10);
        assertEquals(2, aliceDraftResults.size());
        assertTrue(aliceDraftResults.contains("epackage-draft"));
        assertTrue(aliceDraftResults.contains("route-draft"));
        
        // Test role + sourceChannel combination
        List<String> aiDraftResults = helper.searchObjectIds("role:draft AND sourceChannel:AI_GENERATOR", 10);
        assertEquals(1, aiDraftResults.size());
        assertEquals("epackage-draft", aiDraftResults.get(0));
    }

    /**
     * Creates test metadata with the specified parameters.
     */
    private ObjectMetadata createTestMetadata(String user, String channel, String type) {
        return createTestMetadata(user, channel, type, "draft"); // Default role
    }

    private ObjectMetadata createTestMetadata(String user, String channel, String type, String role) {
        ObjectMetadata metadata = factory.createObjectMetadata();
        metadata.setObjectId("obj-" + System.nanoTime()); // Ensure unique object ID
        metadata.setUploadUser(user);
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel(channel);
        metadata.setObjectType(type);
        metadata.setContentHash("test-hash-" + System.nanoTime());
        metadata.setRole(role);
        metadata.setObjectName(type.toLowerCase() + "-" + role + "-" + System.nanoTime()); // Unique name per role
        return metadata;
    }
}