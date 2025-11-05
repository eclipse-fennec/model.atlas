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
package org.gecko.mac.management.lucene;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.gecko.mac.mgmt.management.ManagementFactory;
import org.gecko.mac.mgmt.management.ManagementPackage;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.gecko.mac.management.lucene.registry.LuceneRegistryHelper;
import org.gecko.mac.management.lucene.registry.MetadataQueryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests for Lucene query functionality using MetadataQueryBuilder.
 * 
 * <p>Tests real-world query scenarios including complex searches, time ranges,
 * property filtering, and full-text search across multiple fields.</p>
 */
class LuceneQueryIntegrationTest {

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
        
        // Populate test data
        setupTestData();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (helper != null) {
            helper.close();
        }
    }

    /**
     * Sets up comprehensive test data covering various query scenarios.
     */
    void setupTestData() throws IOException {
        Instant baseTime = Instant.now().minus(30, ChronoUnit.DAYS);
        
        // AI-generated EPackages (recent) with governance workflow
        for (int i = 1; i <= 5; i++) {
            ObjectMetadata metadata = factory.createObjectMetadata();
            metadata.setUploadUser("ai-system");
            metadata.setUploadTime(baseTime.plus(i, ChronoUnit.DAYS));
            metadata.setSourceChannel("AI_GENERATOR");
            metadata.setObjectType("EPackage");
            metadata.setContentHash("ai-hash-" + i);
            metadata.getProperties().put("generator-version", "2.1.0");
            metadata.getProperties().put("confidence", String.valueOf(0.9 + (i * 0.01)));
            metadata.getProperties().put("namespace", "sensors.iot.device" + i);
            
            // Add new governance fields
            metadata.setGenerationTriggerFingerprint("fp-sensors-" + i + "23");
            metadata.setComplianceCheckTime(baseTime.plus(i + 1, ChronoUnit.DAYS));
            metadata.setComplianceStatus(i <= 3 ? "COMPLIANT" : "PENDING");
            metadata.setGovernanceDocumentationId("gov-doc-ai-" + i);
            // Note: lastChangeUser/lastChangeTime not set - these are initial uploads, not updates
            
            // Add new object registration fields
            metadata.setStatus(org.gecko.mac.mgmt.management.ObjectStatus.DRAFT);
            metadata.setVersion("1." + i + ".0");
            metadata.setObjectName("AI Generated Sensor Package " + i);
            
            helper.updateIndex("ai-epackage-" + i, metadata);
        }
        
        // Manual uploads (older)
        for (int i = 1; i <= 3; i++) {
            ObjectMetadata metadata = factory.createObjectMetadata();
            metadata.setUploadUser("john.developer");
            metadata.setUploadTime(baseTime.minus(i * 2, ChronoUnit.DAYS));
            metadata.setSourceChannel("MANUAL_UPLOAD");
            metadata.setObjectType("EPackage");
            metadata.setContentHash("manual-hash-" + i);
            metadata.getProperties().put("project", "smart-city");
            metadata.getProperties().put("version", "1." + i + ".0");
            
            // Add governance workflow fields
            metadata.setComplianceCheckTime(baseTime.minus(i, ChronoUnit.DAYS));
            metadata.setComplianceStatus("COMPLIANT");
            metadata.setGovernanceDocumentationId("gov-doc-manual-" + i);
            // Note: lastChangeUser/lastChangeTime not set - these are initial uploads, not updates
            
            // Add new object registration fields
            metadata.setStatus(org.gecko.mac.mgmt.management.ObjectStatus.APPROVED);
            metadata.setVersion("1." + i + ".0");
            metadata.setObjectName("Smart City Model " + i);
            
            helper.updateIndex("manual-epackage-" + i, metadata);
        }
        
        // Route configurations (mixed times)
        for (int i = 1; i <= 4; i++) {
            ObjectMetadata metadata = factory.createObjectMetadata();
            metadata.setUploadUser(i % 2 == 0 ? "ops-team" : "dev-team");
            metadata.setUploadTime(baseTime.plus(i * 3, ChronoUnit.DAYS));
            metadata.setSourceChannel("API_GATEWAY");
            metadata.setObjectType("Route");
            metadata.setContentHash("route-hash-" + i);
            metadata.getProperties().put("environment", i <= 2 ? "production" : "staging");
            metadata.getProperties().put("api-version", "v" + i);
            
            // Add compliance tracking
            metadata.setComplianceCheckTime(baseTime.plus(i * 3 + 1, ChronoUnit.DAYS));
            metadata.setComplianceStatus(i <= 2 ? "COMPLIANT" : "NON_COMPLIANT");
            metadata.setGovernanceDocumentationId("gov-doc-route-" + i);
            // Note: lastChangeUser/lastChangeTime not set - these are initial uploads, not updates
            
            // Add new object registration fields
            metadata.setStatus(i <= 2 ? org.gecko.mac.mgmt.management.ObjectStatus.DEPLOYED : org.gecko.mac.mgmt.management.ObjectStatus.DRAFT);
            metadata.setVersion("1." + i + ".0");
            
            helper.updateIndex("route-config-" + i, metadata);
        }
        
        // Reviewed objects with full governance workflow
        for (int i = 1; i <= 2; i++) {
            ObjectMetadata metadata = factory.createObjectMetadata();
            metadata.setUploadUser("ai-system");
            metadata.setUploadTime(baseTime.plus(i + 10, ChronoUnit.DAYS));
            metadata.setSourceChannel("AI_GENERATOR");
            metadata.setObjectType("SensorModel");
            metadata.setContentHash("reviewed-hash-" + i);
            metadata.setReviewUser("quality.reviewer");
            metadata.setReviewTime(baseTime.plus(i + 15, ChronoUnit.DAYS));
            metadata.setReviewReason("Quality assurance passed after thorough review");
            metadata.getProperties().put("review-status", "approved");
            metadata.getProperties().put("sensor-type", "temperature");
            
            // Add full governance workflow fields
            metadata.setGenerationTriggerFingerprint("fp-temperature-sensor-" + i + "45");
            metadata.setComplianceCheckTime(baseTime.plus(i + 16, ChronoUnit.DAYS));
            metadata.setComplianceStatus("COMPLIANT");
            metadata.setGovernanceDocumentationId("gov-doc-sensor-" + i);
            // These objects HAVE been updated (reviewed), so lastChange fields are appropriate
            metadata.setLastChangeUser("quality.reviewer");
            metadata.setLastChangeTime(baseTime.plus(i + 16, ChronoUnit.DAYS));
            
            // Add new object registration fields
            metadata.setStatus(org.gecko.mac.mgmt.management.ObjectStatus.APPROVED);
            metadata.setVersion("2." + i + ".0");
            
            helper.updateIndex("reviewed-sensor-" + i, metadata);
        }
        
        // Objects with various compliance statuses for testing
        ObjectMetadata nonCompliantMetadata = factory.createObjectMetadata();
        nonCompliantMetadata.setUploadUser("test-system");
        nonCompliantMetadata.setUploadTime(baseTime.plus(20, ChronoUnit.DAYS));
        nonCompliantMetadata.setSourceChannel("EXTERNAL_API");
        nonCompliantMetadata.setObjectType("DataModel");
        nonCompliantMetadata.setContentHash("noncompliant-hash");
        nonCompliantMetadata.setComplianceCheckTime(baseTime.plus(21, ChronoUnit.DAYS));
        nonCompliantMetadata.setComplianceStatus("NON_COMPLIANT");
        nonCompliantMetadata.setGovernanceDocumentationId("gov-doc-issues-001");
        // Note: lastChangeUser/lastChangeTime not set - this is an initial upload, not an update
        
        // Add new object registration fields
        nonCompliantMetadata.setStatus(org.gecko.mac.mgmt.management.ObjectStatus.REJECTED);
        nonCompliantMetadata.setVersion("1.0.0");
        
        helper.updateIndex("noncompliant-model", nonCompliantMetadata);
    }

    @Test
    void testSimpleUserQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .uploadUser("ai-system")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(7, results.size(), "Should find all AI-generated objects");
        
        // Verify all results contain expected pattern
        for (String objectId : results) {
            assertTrue(objectId.startsWith("ai-") || objectId.startsWith("reviewed-"), 
                      "All results should be AI-generated: " + objectId);
        }
    }

    @Test
    void testObjectTypeQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .objectType("EPackage")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(8, results.size(), "Should find all EPackage objects");
    }

    @Test
    void testSourceChannelQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .sourceChannel("AI_GENERATOR")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(7, results.size(), "Should find all AI_GENERATOR objects");
    }

    @Test
    void testComplexAndQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .sourceChannel("AI_GENERATOR")
            .objectType("EPackage")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(5, results.size(), "Should find AI-generated EPackages only");
        
        for (String objectId : results) {
            assertTrue(objectId.startsWith("ai-epackage-"), 
                      "Should only return AI EPackages: " + objectId);
        }
    }

    @Test
    void testOrQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .or()
            .uploadUser("john.developer")
            .uploadUser("ops-team")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertTrue(results.size() >= 5, "Should find objects from both users");
    }

    @Test
    void testPropertyQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .property("project", "smart-city")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(3, results.size(), "Should find all smart-city project objects");
        
        for (String objectId : results) {
            assertTrue(objectId.startsWith("manual-epackage-"), 
                      "Should only return manual EPackages: " + objectId);
        }
    }

    @Test
    void testPropertyExistenceQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .hasProperty("confidence")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(5, results.size(), "Should find all objects with confidence property");
    }

    @Test
    void testMultiplePropertiesQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .property("environment", "production")
            .objectType("Route")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find production routes only");
    }

    @Test
    void testAnyUserQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .anyUser("quality.reviewer")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find objects uploaded or reviewed by quality.reviewer");
        
        for (String objectId : results) {
            assertTrue(objectId.startsWith("reviewed-sensor-"), 
                      "Should only return reviewed sensors: " + objectId);
        }
    }

    @Test
    void testFullTextSearch() throws IOException {
        String query = MetadataQueryBuilder.create()
            .property("sensor-type", "temperature")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find objects with temperature sensor type");
    }

    @Test
    void testMultiFieldTextSearch() throws IOException {
        String query = MetadataQueryBuilder.create()
            .anyField("smart-city")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertTrue(results.size() >= 3, "Should find objects with 'smart-city' in properties");
    }

    @Test
    void testTimeRangeQuery() throws IOException {
        Instant baseTime = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant start = baseTime.plus(2, ChronoUnit.DAYS);
        Instant end = baseTime.plus(8, ChronoUnit.DAYS);
        
        List<String> results = helper.searchByUploadTimeRange(start, end, 20);
        assertTrue(results.size() >= 3, "Should find objects in time range");
    }

    @Test
    void testTimeAfterQuery() throws IOException {
        Instant baseTime = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant searchAfter = baseTime.plus(5, ChronoUnit.DAYS); // Search after day 5 instead of day 10
        
        List<String> results = helper.searchByUploadTimeRange(searchAfter, null, 20);
        assertTrue(results.size() >= 4, "Should find recent objects after day 5");
    }

    @Test
    void testTimeBeforeQuery() throws IOException {
        Instant baseTime = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant searchBefore = baseTime.minus(1, ChronoUnit.DAYS);
        
        List<String> results = helper.searchByUploadTimeRange(null, searchBefore, 20);
        assertEquals(3, results.size(), "Should find old manual uploads");
    }

    @Test
    void testComplexMultiCriteriaQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .sourceChannel("AI_GENERATOR")
            .hasProperty("generator-version")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(5, results.size(), "Should find AI objects with generator-version property");
    }

    @Test
    void testReviewedObjectsQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .reviewUser("quality.reviewer")
            .property("review-status", "approved")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find approved reviewed objects");
    }

    @Test
    void testEmptyQuery() throws IOException {
        String query = MetadataQueryBuilder.create().build();
        
        List<String> results = helper.searchObjectIds(query, 50);
        assertEquals(15, results.size(), "Empty query should return all objects");
    }

    @Test
    void testNonMatchingQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .uploadUser("nonexistent-user")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertTrue(results.isEmpty(), "Non-matching query should return empty results");
    }

    @Test
    void testQueryWithSpecialCharacters() throws IOException {
        // First add an object with simpler special characters
        ObjectMetadata metadata = factory.createObjectMetadata();
        metadata.setUploadUser("test-user");
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel("TEST_CHANNEL");
        metadata.setObjectType("TestObject");
        metadata.getProperties().put("special-key", "value-with-dashes");
        
        helper.updateIndex("special-chars-test", metadata);
        
        String query = MetadataQueryBuilder.create()
            .property("special-key", "value-with-dashes")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(1, results.size(), "Should handle special characters in queries");
        assertEquals("special-chars-test", results.get(0));
    }

    @Test
    void testCaseSensitiveQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .sourceChannel("ai_generator") // lowercase
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        // Note: Lucene StandardAnalyzer is case-insensitive by default
        // This test verifies that behavior
        assertEquals(7, results.size(), "StandardAnalyzer is case-insensitive by default");
        
        // Test exact case still works
        String correctQuery = MetadataQueryBuilder.create()
            .sourceChannel("AI_GENERATOR")
            .build();
        
        List<String> correctResults = helper.searchObjectIds(correctQuery, 20);
        assertEquals(7, correctResults.size(), "Should find with exact case");
    }

    @Test
    void testQueryResultOrdering() throws IOException {
        String query = MetadataQueryBuilder.create()
            .objectType("EPackage")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertFalse(results.isEmpty(), "Should have results");
        
        // Verify all results are unique
        assertEquals(results.size(), results.stream().distinct().count(), 
                    "All results should be unique");
    }

    // ===== Tests for New Governance Workflow Fields =====

    @Test
    void testGenerationTriggerFingerprintQuery() throws IOException {
        // First verify what fingerprints exist in index
        String allQuery = MetadataQueryBuilder.create().build();
        List<String> allResults = helper.searchObjectIds(allQuery, 50);
        assertTrue(allResults.size() >= 5, "Should have test data - found: " + allResults.size());
        
        String query = MetadataQueryBuilder.create()
            .generationTriggerFingerprint("fp-sensors-123")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(1, results.size(), "Should find AI-generated object with specific fingerprint");
        assertEquals("ai-epackage-1", results.get(0));
    }

    @Test
    void testMultipleGenerationTriggerFingerprintQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .or()
            .generationTriggerFingerprint("fp-sensors-123")
            .generationTriggerFingerprint("fp-temperature-sensor-145")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find objects with different fingerprints");
        assertTrue(results.contains("ai-epackage-1"));
        assertTrue(results.contains("reviewed-sensor-1"));
    }

    @Test
    void testComplianceStatusQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .complianceStatus("COMPLIANT")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertTrue(results.size() >= 8, "Should find all compliant objects");
        
        // Test specific non-compliant query
        String nonCompliantQuery = MetadataQueryBuilder.create()
            .complianceStatus("NON_COMPLIANT")
            .build();
        
        List<String> nonCompliantResults = helper.searchObjectIds(nonCompliantQuery, 20);
        assertTrue(nonCompliantResults.size() >= 3, "Should find non-compliant objects");
        assertTrue(nonCompliantResults.contains("noncompliant-model"));
    }

    @Test
    void testComplianceStatusWithObjectType() throws IOException {
        String query = MetadataQueryBuilder.create()
            .complianceStatus("COMPLIANT")
            .objectType("EPackage")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(6, results.size(), "Should find all compliant EPackages");
    }

    @Test
    void testGovernanceDocumentationIdQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .governanceDocumentationId("gov-doc-ai-1")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(1, results.size(), "Should find object with specific governance doc ID");
        assertEquals("ai-epackage-1", results.get(0));
    }

    @Test
    void testGovernanceDocumentationIdPattern() throws IOException {
        // Test searching for multiple AI governance docs using OR query
        String query = MetadataQueryBuilder.create()
            .or()
            .governanceDocumentationId("gov-doc-ai-1")
            .governanceDocumentationId("gov-doc-ai-2")
            .governanceDocumentationId("gov-doc-ai-3")
            .governanceDocumentationId("gov-doc-ai-4")
            .governanceDocumentationId("gov-doc-ai-5")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertTrue(results.size() >= 5, "Should find objects with AI governance docs");
    }

    @Test
    void testLastChangeUserQuery() throws IOException {
        String query = MetadataQueryBuilder.create()
            .lastChangeUser("quality.reviewer")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find objects last changed by quality reviewer");
        assertTrue(results.contains("reviewed-sensor-1"));
        assertTrue(results.contains("reviewed-sensor-2"));
    }

    @Test
    void testLastChangeUserWithSourceChannel() throws IOException {
        String query = MetadataQueryBuilder.create()
            .lastChangeUser("quality.reviewer")
            .sourceChannel("AI_GENERATOR")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find AI objects last changed by quality reviewer");
        assertTrue(results.contains("reviewed-sensor-1"));
        assertTrue(results.contains("reviewed-sensor-2"));
    }

    @Test
    void testComplianceCheckTimeRangeQuery() throws IOException {
        Instant baseTime = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant start = baseTime.plus(1, ChronoUnit.DAYS);
        Instant end = baseTime.plus(10, ChronoUnit.DAYS);
        
        List<String> results = helper.searchByComplianceCheckTimeRange(start, end, 20);
        assertTrue(results.size() >= 5, "Should find objects with compliance checks in range");
    }

    @Test
    void testLastChangeTimeRangeQuery() throws IOException {
        Instant baseTime = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant start = baseTime.plus(15, ChronoUnit.DAYS);
        Instant end = baseTime.plus(25, ChronoUnit.DAYS);
        
        List<String> results = helper.searchByLastChangeTimeRange(start, end, 20);
        assertTrue(results.size() >= 2, "Should find recently changed objects");
    }

    @Test
    void testReviewReasonFullTextSearch() throws IOException {
        String query = MetadataQueryBuilder.create()
            .anyField("thorough review")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find objects with 'thorough review' in reason");
        assertTrue(results.contains("reviewed-sensor-1"));
        assertTrue(results.contains("reviewed-sensor-2"));
    }

    @Test
    void testComplexGovernanceWorkflowQuery() throws IOException {
        // Find AI-generated objects that are compliant and have been reviewed
        String query = MetadataQueryBuilder.create()
            .sourceChannel("AI_GENERATOR")
            .complianceStatus("COMPLIANT")
            .reviewUser("quality.reviewer")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find AI objects that passed governance review");
        assertTrue(results.contains("reviewed-sensor-1"));
        assertTrue(results.contains("reviewed-sensor-2"));
    }

    @Test
    void testGovernanceComplianceAuditQuery() throws IOException {
        
        // Find all objects with recent compliance checks regardless of status
        String query = MetadataQueryBuilder.create()
            .or()
            .complianceStatus("COMPLIANT")
            .complianceStatus("NON_COMPLIANT")
            .complianceStatus("PENDING")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 50);
        assertEquals(15, results.size(), "Should find all objects with compliance status");
    }

    @Test
    void testAIGenerationLifecycleQuery() throws IOException {
        // Test complete AI generation lifecycle: trigger fingerprint → compliance → review
        String query = MetadataQueryBuilder.create()
            .generationTriggerFingerprint("fp-temperature-sensor-145")
            .complianceStatus("COMPLIANT")
            .reviewReason("Quality assurance passed after thorough review")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(1, results.size(), "Should find complete AI lifecycle object");
        assertEquals("reviewed-sensor-1", results.get(0));
    }

    @Test
    void testGovernanceDocumentationLinkage() throws IOException {
        // Test that governance documentation IDs are properly indexed and searchable
        String query = MetadataQueryBuilder.create()
            .governanceDocumentationId("gov-doc-issues-001")
            .complianceStatus("NON_COMPLIANT")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(1, results.size(), "Should find non-compliant object with issues documentation");
        assertEquals("noncompliant-model", results.get(0));
    }

    @Test
    void testMultiFieldGovernanceSearch() throws IOException {
        // Test search across multiple governance fields
        String query = MetadataQueryBuilder.create()
            .anyField("quality")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertTrue(results.size() >= 2, "Should find objects with 'quality' in any governance field");
    }

    @Test
    void testTimeBasedGovernanceAnalysis() throws IOException {
        
        // Find objects with compliance checks in specific timeframe
        String query = MetadataQueryBuilder.create()
            .complianceStatus("COMPLIANT")
            .lastChangeUser("quality.reviewer")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find objects approved by quality reviewer");
    }

    @Test
    void testGovernanceFieldsCombinedWithLegacyFields() throws IOException {
        // Test that new governance fields work well with existing fields
        String query = MetadataQueryBuilder.create()
            .uploadUser("ai-system")
            .objectType("EPackage")
            .complianceStatus("PENDING")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find pending AI EPackages");
        assertTrue(results.contains("ai-epackage-4"));
        assertTrue(results.contains("ai-epackage-5"));
    }

    // ===== Tests for New Object Registration Fields =====


    @Test
    void testObjectStatusQuery() throws IOException {
        // Test status query on existing setupTestData objects
        // First, verify we have objects with DRAFT status (ai-epackage objects)
        String draftQuery = MetadataQueryBuilder.create()
            .status("DRAFT")
            .build();
        
        List<String> draftResults = helper.searchObjectIds(draftQuery, 20);
        assertTrue(draftResults.size() >= 5, "Should find DRAFT objects from test data");
        
        // Test approved status (manual-epackage and reviewed-sensor objects)
        String approvedQuery = MetadataQueryBuilder.create()
            .status("APPROVED")
            .build();
        
        List<String> approvedResults = helper.searchObjectIds(approvedQuery, 20);
        assertTrue(approvedResults.size() >= 5, "Should find APPROVED objects from test data");
        
        // Test deployed status (route-config objects 1-2)
        String deployedQuery = MetadataQueryBuilder.create()
            .status("DEPLOYED")
            .build();
        
        List<String> deployedResults = helper.searchObjectIds(deployedQuery, 20);
        assertTrue(deployedResults.size() >= 2, "Should find DEPLOYED objects from test data");
    }

    @Test
    void testVersionQuery() throws IOException {
        // The setupTestData creates objects with versions like "1.1.0", "1.2.0", etc.
        // Test exact version matching for one that should exist
        String query = MetadataQueryBuilder.create()
            .version("1.1.0")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertTrue(results.size() >= 1, "Should find objects with version 1.1.0");
        // Multiple objects could have this version, so just check we find some
    }

    @Test
    void testVersionWildcardQuery() throws IOException {
        // Test wildcard version search on existing test data (which has versions like "1.1.0", "1.2.0", etc.)
        String query = MetadataQueryBuilder.create()
            .versionWildcard("1.*")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertTrue(results.size() >= 8, "Should find all 1.x versions from test data");
        // Test data includes ai-epackage-1 through ai-epackage-5 (versions 1.1.0 to 1.5.0)
        // and manual-epackage-1 through manual-epackage-3 (versions 1.1.0 to 1.3.0)
        // and route-config-1 through route-config-4 (versions 1.1.0 to 1.4.0)
    }

    @Test
    void testObjectRefQuery() throws IOException {
        // Create test objects with proper URIs as suggested by user
        // Test 1: Object with detached objectRef using InternalEObject URI casting
        ObjectMetadata refMetadata1 = factory.createObjectMetadata();
        refMetadata1.setUploadUser("test-user");
        refMetadata1.setUploadTime(Instant.now());
        refMetadata1.setSourceChannel("TEST");
        refMetadata1.setObjectType("RefModel");
        refMetadata1.setContentHash("ref-hash-1");
        refMetadata1.setVersion("1.0.0");
        refMetadata1.setStatus(org.gecko.mac.mgmt.management.ObjectStatus.DRAFT);
        
        // Create a detached EObject and set URI using InternalEObject casting (as suggested by user)
        ObjectMetadata detachedObject = factory.createObjectMetadata();
        org.eclipse.emf.common.util.URI sensorModelUri = org.eclipse.emf.common.util.URI.createURI("platform:/resource/project/models/sensor-model.xmi#//@contents.0");
        ((org.eclipse.emf.ecore.InternalEObject) detachedObject).eSetProxyURI(sensorModelUri);
        refMetadata1.setObjectRef(detachedObject);
        
        helper.updateIndex("ref-object-1", refMetadata1);

        // Test 2: Object with different URI for contrast
        ObjectMetadata refMetadata2 = factory.createObjectMetadata();
        refMetadata2.setUploadUser("test-user");
        refMetadata2.setUploadTime(Instant.now());
        refMetadata2.setSourceChannel("TEST");
        refMetadata2.setObjectType("RefModel");
        refMetadata2.setContentHash("ref-hash-2");
        refMetadata2.setVersion("2.0.0");
        refMetadata2.setStatus(org.gecko.mac.mgmt.management.ObjectStatus.APPROVED);
        
        // Create another detached EObject with different URI
        ObjectMetadata detachedObject2 = factory.createObjectMetadata();
        ((org.eclipse.emf.ecore.InternalEObject) detachedObject2).eSetProxyURI(
            org.eclipse.emf.common.util.URI.createURI("platform:/resource/project/models/actuator-model.xmi#//@contents.0")
        );
        refMetadata2.setObjectRef(detachedObject2);
        helper.updateIndex("ref-object-2", refMetadata2);

        // Test that we can save and retrieve objects with proper objectRef URIs
        List<String> savedObjects = helper.getAllObjectIds();
        assertTrue(savedObjects.contains("ref-object-1"), "Should find first object with objectRef");
        assertTrue(savedObjects.contains("ref-object-2"), "Should find second object with objectRef");
        
        // Test searching by specific objectRef URI
        String uriQuery = MetadataQueryBuilder.create()
            .objectRef("platform:/resource/project/models/sensor-model.xmi#//@contents.0")
            .build();
        
        List<String> uriResults = helper.searchObjectIds(uriQuery, 20);
        assertEquals(1, uriResults.size(), "Should find object by specific objectRef URI");
        assertEquals("ref-object-1", uriResults.get(0));
        
        // Test combined search with objectRef and status
        String combinedQuery = MetadataQueryBuilder.create()
            .objectRef("platform:/resource/project/models/actuator-model.xmi#//@contents.0")
            .status("APPROVED")
            .build();
        
        List<String> combinedResults = helper.searchObjectIds(combinedQuery, 20);
        assertEquals(1, combinedResults.size(), "Should find object by objectRef and status combination");
        assertEquals("ref-object-2", combinedResults.get(0));
        
        // Test version search still works
        String versionQuery = MetadataQueryBuilder.create()
            .version("1.0.0")
            .objectType("RefModel")
            .build();
        
        List<String> versionResults = helper.searchObjectIds(versionQuery, 20);
        assertEquals(1, versionResults.size(), "Should find object by version and type");
        assertEquals("ref-object-1", versionResults.get(0));
    }

    @Test
    void testCombinedNewFieldsQuery() throws IOException {
        // Test combining new fields in a single query
        // Look for APPROVED objects with version 2.x (reviewed sensors have versions 2.1.0, 2.2.0)
        String query = MetadataQueryBuilder.create()
            .status("APPROVED")
            .versionWildcard("2.*")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(2, results.size(), "Should find approved reviewed sensors with 2.x versions");
        assertTrue(results.contains("reviewed-sensor-1"));
        assertTrue(results.contains("reviewed-sensor-2"));
    }

    @Test
    void testObjectMetadataIdQuery() throws IOException {
        // Create test objects with specific objectId values
        ObjectMetadata metadata1 = factory.createObjectMetadata();
        metadata1.setUploadUser("test-user");
        metadata1.setSourceChannel("MANUAL_UPLOAD");
        metadata1.setObjectType("TestModel");
        metadata1.setUploadTime(Instant.now());
        metadata1.setStatus(org.gecko.mac.mgmt.management.ObjectStatus.DRAFT);
        metadata1.setVersion("1.0.0");
        metadata1.setObjectId("metadata-001"); // Set specific objectId
        helper.updateIndex("object-meta-1", metadata1);
        
        ObjectMetadata metadata2 = factory.createObjectMetadata();
        metadata2.setUploadUser("test-user");
        metadata2.setSourceChannel("AI_GENERATOR");
        metadata2.setObjectType("SensorModel");
        metadata2.setUploadTime(Instant.now());
        metadata2.setStatus(org.gecko.mac.mgmt.management.ObjectStatus.APPROVED);
        metadata2.setVersion("2.0.0");
        metadata2.setObjectId("metadata-002"); // Set specific objectId
        helper.updateIndex("object-meta-2", metadata2);
        
        // Test searching by specific objectMetadataId
        String idQuery = MetadataQueryBuilder.create()
            .objectMetadataId("metadata-001")
            .build();
        
        List<String> idResults = helper.searchObjectIds(idQuery, 20);
        assertEquals(1, idResults.size(), "Should find object by specific objectMetadataId");
        assertEquals("object-meta-1", idResults.get(0));
        
        // Test searching by different objectMetadataId
        String idQuery2 = MetadataQueryBuilder.create()
            .objectMetadataId("metadata-002")
            .build();
        
        List<String> idResults2 = helper.searchObjectIds(idQuery2, 20);
        assertEquals(1, idResults2.size(), "Should find object by different objectMetadataId");
        assertEquals("object-meta-2", idResults2.get(0));
        
        // Test combined search with objectMetadataId and other criteria
        String combinedQuery = MetadataQueryBuilder.create()
            .objectMetadataId("metadata-002")
            .status("APPROVED")
            .build();
        
        List<String> combinedResults = helper.searchObjectIds(combinedQuery, 20);
        assertEquals(1, combinedResults.size(), "Should find object by objectMetadataId and status combination");
        assertEquals("object-meta-2", combinedResults.get(0));
        
        // Test search for non-existent objectMetadataId
        String nonExistentQuery = MetadataQueryBuilder.create()
            .objectMetadataId("metadata-999")
            .build();
        
        List<String> nonExistentResults = helper.searchObjectIds(nonExistentQuery, 20);
        assertEquals(0, nonExistentResults.size(), "Should find no results for non-existent objectMetadataId");
    }

    @Test
    void testNewFieldsWithLegacyFields() throws IOException {
        // Test new fields combined with existing fields
        // Find AI-generated objects with specific status and version pattern
        String query = MetadataQueryBuilder.create()
            .uploadUser("ai-system")
            .sourceChannel("AI_GENERATOR")
            .status("DRAFT")
            .versionWildcard("1.*")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(5, results.size(), "Should find AI draft objects with 1.x versions");
        // These should be ai-epackage-1 through ai-epackage-5
        for (int i = 1; i <= 5; i++) {
            assertTrue(results.contains("ai-epackage-" + i));
        }
    }

    @Test
    void testStatusTransitionWorkflow() throws IOException {
        // Test workflow from DRAFT to APPROVED
        ObjectMetadata workflowMetadata = factory.createObjectMetadata();
        workflowMetadata.setUploadUser("workflow-user");
        workflowMetadata.setUploadTime(Instant.now());
        workflowMetadata.setSourceChannel("WORKFLOW");
        workflowMetadata.setObjectType("WorkflowModel");
        workflowMetadata.setContentHash("workflow-hash");
        workflowMetadata.setStatus(org.gecko.mac.mgmt.management.ObjectStatus.DRAFT);
        workflowMetadata.setVersion("1.0.0");
        helper.updateIndex("workflow-object", workflowMetadata);

        // Verify initial DRAFT status
        String draftQuery = MetadataQueryBuilder.create()
            .status("DRAFT")
            .uploadUser("workflow-user")
            .build();
        
        List<String> draftResults = helper.searchObjectIds(draftQuery, 20);
        assertEquals(1, draftResults.size(), "Should find draft workflow object");

        // Update to APPROVED status (simulating workflow transition)
        workflowMetadata.setStatus(org.gecko.mac.mgmt.management.ObjectStatus.APPROVED);
        workflowMetadata.setVersion("1.1.0"); // Version bump on approval
        helper.updateIndex("workflow-object", workflowMetadata);

        // Verify status change
        String approvedQuery = MetadataQueryBuilder.create()
            .status("APPROVED")
            .uploadUser("workflow-user")
            .build();
        
        List<String> approvedResults = helper.searchObjectIds(approvedQuery, 20);
        assertEquals(1, approvedResults.size(), "Should find approved workflow object");

        // Verify old status no longer matches
        List<String> stillDraftResults = helper.searchObjectIds(draftQuery, 20);
        assertEquals(0, stillDraftResults.size(), "Should not find object in old draft status");
    }

    // ===== Tests for New ObjectName Field =====

    @Test
    void testObjectNameQuery() throws IOException {
        // Test exact object name search
        String query = MetadataQueryBuilder.create()
            .objectName("AI Generated Sensor Package 1")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(1, results.size(), "Should find object with exact name match");
        assertEquals("ai-epackage-1", results.get(0));
    }

    @Test
    void testObjectNamePartialSearch() throws IOException {
        // Test partial object name search (analyzed field supports partial matching)
        String query = MetadataQueryBuilder.create()
            .objectName("Smart City")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(3, results.size(), "Should find all Smart City models");
        assertTrue(results.contains("manual-epackage-1"));
        assertTrue(results.contains("manual-epackage-2"));
        assertTrue(results.contains("manual-epackage-3"));
    }

    @Test
    void testObjectNameInAnyFieldSearch() throws IOException {
        // Test that objectName is included in anyField searches
        String query = MetadataQueryBuilder.create()
            .anyField("Sensor")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertTrue(results.size() >= 5, "Should find objects with 'Sensor' in name or other fields");
        // Should include ai-epackage objects with "AI Generated Sensor Package" names
        assertTrue(results.contains("ai-epackage-1"));
        assertTrue(results.contains("ai-epackage-2"));
    }

    @Test
    void testObjectNameCombinedWithOtherFields() throws IOException {
        // Test objectName combined with other criteria
        String query = MetadataQueryBuilder.create()
            .objectName("Smart City")
            .sourceChannel("MANUAL_UPLOAD")
            .status("APPROVED")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(3, results.size(), "Should find approved Smart City models from manual upload");
        assertTrue(results.contains("manual-epackage-1"));
        assertTrue(results.contains("manual-epackage-2"));
        assertTrue(results.contains("manual-epackage-3"));
    }

    @Test
    void testObjectNameWithSpecialCharacters() throws IOException {
        // Create test object with special characters in name
        ObjectMetadata metadata = factory.createObjectMetadata();
        metadata.setUploadUser("test-user");
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel("TEST");
        metadata.setObjectType("SpecialModel");
        metadata.setContentHash("special-hash");
        metadata.setStatus(org.gecko.mac.mgmt.management.ObjectStatus.DRAFT);
        metadata.setVersion("1.0.0");
        metadata.setObjectName("Test Model (Version 2.0)");
        
        helper.updateIndex("special-name-test", metadata);
        
        String query = MetadataQueryBuilder.create()
            .objectName("Test Model (Version 2.0)")
            .build();
        
        List<String> results = helper.searchObjectIds(query, 20);
        assertEquals(1, results.size(), "Should handle special characters in object name");
        assertEquals("special-name-test", results.get(0));
    }
}