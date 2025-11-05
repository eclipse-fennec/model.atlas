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
package org.eclipse.fennec.model.atlas.governance.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.fennec.model.atlas.governance.ApprovalStatus;
import org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult;
import org.eclipse.fennec.model.atlas.governance.ComplianceStatus;
import org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation;
import org.eclipse.fennec.model.atlas.governance.GovernanceFactory;
import org.eclipse.fennec.model.atlas.governance.tests.annotations.GovernanceTestAnnotations;
import org.eclipse.fennec.model.atlas.governance.tests.annotations.GovernanceTestAnnotations.DocumentationServiceTestSetup;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.policy.gdpr.GDPRFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for GovernanceDocumentationService.
 * 
 * <p>Tests comprehensive documentation storage functionality including:</p>
 * <ul>
 * <li>Documentation storage and retrieval with versioning</li>
 * <li>Latest documentation access and history tracking</li>
 * <li>Documentation existence checks and statistics</li>
 * <li>Documentation deletion and cleanup</li>
 * <li>Concurrent operations and caching behavior</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class GovernanceDocumentationServiceIntegrationTest {

    private static final String TEST_OBJECT_ID = "test-epackage-001";
    private static final String TEST_USER = "test-reviewer";
    
    private GovernanceFactory governanceFactory;
    private GDPRFactory gdprFactory;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        governanceFactory = GovernanceFactory.eINSTANCE;
        gdprFactory = GDPRFactory.eINSTANCE;
		assertNotNull(tempDir, "TempDir should not be null");
		
		// Set system property for template argument resolution
		System.setProperty(GovernanceTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
    }
    
    @Test
    @DocumentationServiceTestSetup
    void testStoreAndRetrieveDocumentation(@InjectService GovernanceDocumentationService documentationService) throws Exception {
        // Create comprehensive governance documentation
        GovernanceDocumentation documentation = createTestDocumentation(TEST_OBJECT_ID);
        
        // Store documentation with reason
        String docId = documentationService.storeDocumentation(TEST_OBJECT_ID, documentation, TEST_USER, "Initial documentation storage test").getValue();
        
        // Verify storage result
        assertNotNull(docId, "Documentation ID should be returned");
        assertTrue(docId.contains(TEST_OBJECT_ID), "Documentation ID should contain object ID");
        assertTrue(docId.contains("governance"), "Documentation ID should contain governance marker");
        
        // Verify documentation can be retrieved by specific ID
        Optional<GovernanceDocumentation> retrievedDoc = documentationService.getDocumentation(docId);
        assertTrue(retrievedDoc.isPresent(), "Documentation should be retrievable by ID");
        
        GovernanceDocumentation doc = retrievedDoc.get();
        assertEquals(documentation.getModelName(), doc.getModelName());
        assertEquals(documentation.getVersion(), doc.getVersion());
        assertEquals(documentation.getDescription(), doc.getDescription());
        assertEquals(documentation.getStatus(), doc.getStatus());
        assertEquals(documentation.getComplianceChecks().size(), doc.getComplianceChecks().size());
    }
    
    @Test
    @DocumentationServiceTestSetup
    void testLatestDocumentationAccess(@InjectService GovernanceDocumentationService documentationService) throws Exception {
        // Initially no documentation should exist
        assertFalse(documentationService.hasDocumentation(TEST_OBJECT_ID), "Initially no documentation should exist");
        
        Optional<GovernanceDocumentation> initialDoc = documentationService.getLatestDocumentation(TEST_OBJECT_ID);
        assertFalse(initialDoc.isPresent(), "Initially no latest documentation should exist");
        
        // Store first version
        GovernanceDocumentation doc1 = createTestDocumentation(TEST_OBJECT_ID);
        doc1.setVersion("1.0");
        String docId1 = documentationService.storeDocumentation(TEST_OBJECT_ID, doc1, TEST_USER, "First version for versioning test").getValue();
        
        // Verify documentation exists
        assertTrue(documentationService.hasDocumentation(TEST_OBJECT_ID), "Documentation should exist after storage");
        
        // Retrieve latest documentation
        Optional<GovernanceDocumentation> latestDoc = documentationService.getLatestDocumentation(TEST_OBJECT_ID);
        assertTrue(latestDoc.isPresent(), "Latest documentation should be available");
        assertEquals("1.0", latestDoc.get().getVersion());
        
        // Store second version
        Thread.sleep(10); // Ensure different timestamp
        GovernanceDocumentation doc2 = createTestDocumentation(TEST_OBJECT_ID);
        doc2.setVersion("1.1");
        doc2.setDescription("Updated documentation with enhanced compliance checks");
        String docId2 = documentationService.storeDocumentation(TEST_OBJECT_ID, doc2, TEST_USER + "-v2", null).getValue(); // Test null reason
        
        // Verify latest documentation is updated
        Optional<GovernanceDocumentation> updatedLatest = documentationService.getLatestDocumentation(TEST_OBJECT_ID);
        assertTrue(updatedLatest.isPresent(), "Updated latest documentation should be available");
        assertEquals("1.1", updatedLatest.get().getVersion());
        assertEquals("Updated documentation with enhanced compliance checks", updatedLatest.get().getDescription());
        
        // Verify both versions can be retrieved individually
        Optional<GovernanceDocumentation> firstVersion = documentationService.getDocumentation(docId1);
        Optional<GovernanceDocumentation> secondVersion = documentationService.getDocumentation(docId2);
        
        assertTrue(firstVersion.isPresent(), "First version should still be retrievable");
        assertTrue(secondVersion.isPresent(), "Second version should be retrievable");
        assertEquals("1.0", firstVersion.get().getVersion());
        assertEquals("1.1", secondVersion.get().getVersion());
    }
    
    @Test
    @DocumentationServiceTestSetup
    void testDocumentationHistory(@InjectService GovernanceDocumentationService documentationService) throws Exception {
        String objectId = "history-test-object";
        
        // Store multiple versions
        for (int i = 1; i <= 3; i++) {
            GovernanceDocumentation doc = createTestDocumentation(objectId);
            doc.setVersion("1." + i);
            doc.setDescription("Version " + i + " documentation");
            
            documentationService.storeDocumentation(objectId, doc, TEST_USER + "-v" + i, "Version " + i + " history test").getValue();
            Thread.sleep(10); // Ensure different timestamps
        }
        
        // Get documentation history
        List<ObjectMetadata> history = documentationService.getDocumentationHistory(objectId);
        
        assertNotNull(history, "History should not be null");
        assertEquals(3, history.size(), "History should contain 3 versions");
        
        // Verify metadata properties
        for (ObjectMetadata metadata : history) {
            assertNotNull(metadata.getObjectId(), "Metadata should have object ID");
            assertTrue(metadata.getObjectId().contains(objectId), "Object ID should contain source object ID");
            assertTrue(metadata.getObjectId().contains("governance"), "Object ID should contain governance marker");
            assertEquals("GovernanceDocumentation", metadata.getObjectType());
            assertEquals("GOVERNANCE_COMPLIANCE", metadata.getSourceChannel());
            assertNotNull(metadata.getUploadTime(), "Upload time should be set");
        }
    }
    
    @Test
    @DocumentationServiceTestSetup
    void testDocumentationStatistics(@InjectService GovernanceDocumentationService documentationService) throws Exception {
        // Store documentation for multiple objects
        String[] objectIds = {"stats-test-1", "stats-test-2", "stats-test-3"};
        
        for (String objectId : objectIds) {
            GovernanceDocumentation doc = createTestDocumentation(objectId);
            documentationService.storeDocumentation(objectId, doc, TEST_USER, "Statistics test documentation").getValue();
        }
        
        // Store additional version for one object
        GovernanceDocumentation updatedDoc = createTestDocumentation(objectIds[0]);
        updatedDoc.setVersion("2.0");
        documentationService.storeDocumentation(objectIds[0], updatedDoc, TEST_USER, "Updated version for statistics").getValue();
        
        // Get statistics
        Map<String, Object> stats = documentationService.getDocumentationStatistics();
        
        assertNotNull(stats, "Statistics should not be null");
        
        // Verify basic statistics
        assertTrue(stats.containsKey("totalDocumentation"), "Should contain total documentation count");
        assertTrue(stats.containsKey("activeObjects"), "Should contain active objects count");
        assertTrue(stats.containsKey("historicalChecks"), "Should contain historical checks count");
        assertTrue(stats.containsKey("averageChecksPerObject"), "Should contain average checks per object");
        assertTrue(stats.containsKey("lastUpdated"), "Should contain last updated timestamp");
        
        // Verify numerical values
        long totalDocs = ((Number) stats.get("totalDocumentation")).longValue();
        long activeObjects = ((Number) stats.get("activeObjects")).longValue();
        long historicalChecks = ((Number) stats.get("historicalChecks")).longValue();
        
        assertTrue(totalDocs >= 6, "Total documentation should include latest + versioned docs");
        assertEquals(3, activeObjects, "Should have 3 active objects");
        assertTrue(historicalChecks >= 1, "Should have at least 1 historical check");
        
        // Verify compliance statistics if available
        if (stats.containsKey("totalComplianceChecks")) {
            long totalChecks = ((Number) stats.get("totalComplianceChecks")).longValue();
            assertTrue(totalChecks >= 3, "Should have compliance checks from test documentation");
        }
    }
    
    @Test
    @DocumentationServiceTestSetup
    void testDocumentationDeletion(@InjectService GovernanceDocumentationService documentationService) throws Exception {
        String objectId = "deletion-test-object";
        
        // Store multiple versions
        documentationService.storeDocumentation(objectId, createTestDocumentation(objectId), TEST_USER, "First version for deletion test").getValue();
        Thread.sleep(10);
        documentationService.storeDocumentation(objectId, createTestDocumentation(objectId), TEST_USER, "Second version for deletion test").getValue();
        
        // Verify documentation exists
        assertTrue(documentationService.hasDocumentation(objectId), "Documentation should exist before deletion");
        assertFalse(documentationService.getDocumentationHistory(objectId).isEmpty(), "History should not be empty");
        
        // Delete all documentation
        Boolean deleted = documentationService.deleteAllDocumentation(objectId).getValue();
        assertTrue(deleted, "Deletion should be successful");
        
        // Verify documentation is removed
        assertFalse(documentationService.hasDocumentation(objectId), "Documentation should not exist after deletion");
        Optional<GovernanceDocumentation> latestDoc = documentationService.getLatestDocumentation(objectId);
        assertFalse(latestDoc.isPresent(), "Latest documentation should not exist after deletion");
        
        List<ObjectMetadata> history = documentationService.getDocumentationHistory(objectId);
        assertTrue(history.isEmpty(), "History should be empty after deletion");
    }
    
    @Test
    @DocumentationServiceTestSetup
    void testConcurrentDocumentationOperations(@InjectService GovernanceDocumentationService documentationService) throws Exception {
        String objectId = "concurrent-test-object";
        int numThreads = 5;
        int operationsPerThread = 3;
        
        // Create multiple threads that store documentation simultaneously
        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        GovernanceDocumentation doc = createTestDocumentation(objectId + "-thread-" + threadIndex);
                        doc.setVersion(threadIndex + "." + j);
                        doc.setDescription("Thread " + threadIndex + " operation " + j);
                        
                        String docId = documentationService.storeDocumentation(
                            objectId + "-thread-" + threadIndex, doc, TEST_USER + "-thread-" + threadIndex, "Concurrent test thread " + threadIndex).getValue();
                        
                        assertNotNull(docId, "Documentation should be stored successfully");
                        
                        // Verify immediate retrieval
                        Optional<GovernanceDocumentation> retrieved = documentationService.getDocumentation(docId);
                        assertTrue(retrieved.isPresent(), "Stored documentation should be immediately retrievable");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Concurrent operation failed", e);
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(5000); // 5 second timeout
        }
        
        // Verify all objects have documentation
        for (int i = 0; i < numThreads; i++) {
            String testObjectId = objectId + "-thread-" + i;
            assertTrue(documentationService.hasDocumentation(testObjectId), 
                      "Thread " + i + " should have created documentation");
            
            List<ObjectMetadata> history = documentationService.getDocumentationHistory(testObjectId);
            assertEquals(operationsPerThread, history.size(), 
                        "Thread " + i + " should have " + operationsPerThread + " versions in history");
        }
    }
    
    @Test
    @DocumentationServiceTestSetup
    void testDocumentationWithComplexComplianceChecks(@InjectService GovernanceDocumentationService documentationService) throws Exception {
        String objectId = "complex-compliance-test";
        
        // Create documentation with multiple compliance checks
        GovernanceDocumentation documentation = createComplexTestDocumentation(objectId);
        
        String docId = documentationService.storeDocumentation(objectId, documentation, TEST_USER, "Complex documentation test with multiple compliance checks").getValue();
        
        // Retrieve and verify complex documentation
        Optional<GovernanceDocumentation> retrieved = documentationService.getDocumentation(docId);
        assertTrue(retrieved.isPresent(), "Complex documentation should be retrievable");
        
        GovernanceDocumentation doc = retrieved.get();
        assertEquals(3, doc.getComplianceChecks().size(), "Should have 3 compliance checks");
        
        // Verify compliance check details
        ComplianceCheckResult gdprCheck = doc.getComplianceChecks().stream()
            .filter(check -> check.getClass().getSimpleName().contains("GDPR"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(gdprCheck, "GDPR compliance check should be present");
        assertEquals(ComplianceStatus.PASSED, gdprCheck.getStatus());
        assertNotNull(gdprCheck.getCheckTimestamp());
        assertNotNull(gdprCheck.getSummary());
        assertTrue(gdprCheck.getSummary().contains("GDPR"));
    }
    
    @Test
    @DocumentationServiceTestSetup
    void testNonExistentDocumentationAccess(@InjectService GovernanceDocumentationService documentationService) {
        String nonExistentId = "non-existent-object-" + System.currentTimeMillis();
        
        // Test hasDocumentation
        assertFalse(documentationService.hasDocumentation(nonExistentId), 
                   "Non-existent object should not have documentation");
        
        // Test getLatestDocumentation
        Optional<GovernanceDocumentation> latest = documentationService.getLatestDocumentation(nonExistentId);
        assertFalse(latest.isPresent(), "Non-existent object should not have latest documentation");
        
        // Test getDocumentation with invalid ID
        Optional<GovernanceDocumentation> direct = documentationService.getDocumentation("invalid-doc-id");
        assertFalse(direct.isPresent(), "Invalid documentation ID should return empty");
        
        // Test getDocumentationHistory
        List<ObjectMetadata> history = documentationService.getDocumentationHistory(nonExistentId);
        assertTrue(history.isEmpty(), "Non-existent object should have empty history");
        
        // Test deleteAllDocumentation for non-existent object
        try {
            Boolean deleted = documentationService.deleteAllDocumentation(nonExistentId).getValue();
            assertFalse(deleted, "Deleting non-existent documentation should return false");
        } catch (Exception e) {
            // Expected for non-existent documentation
        }
    }
    
    @Test
    @DocumentationServiceTestSetup  
    void testStoreDocumentationWithReasonParameter(@InjectService GovernanceDocumentationService documentationService) throws Exception {
        String objectId = "reason-parameter-test";
        
        // Test 1: Store documentation with meaningful reason
        GovernanceDocumentation doc1 = createTestDocumentation(objectId);
        doc1.setVersion("1.0");
        String reason1 = "Initial documentation creation for testing reason parameter functionality";
        String docId1 = documentationService.storeDocumentation(objectId, doc1, TEST_USER, reason1).getValue();
        
        assertNotNull(docId1, "Documentation should be stored successfully with reason");
        assertTrue(docId1.contains(objectId), "Documentation ID should contain object ID");
        
        // Test 2: Store documentation with null reason (should be acceptable)
        GovernanceDocumentation doc2 = createTestDocumentation(objectId);
        doc2.setVersion("1.1");
        String docId2 = documentationService.storeDocumentation(objectId, doc2, TEST_USER + "-v2", null).getValue();
        
        assertNotNull(docId2, "Documentation should be stored successfully with null reason");
        assertTrue(docId2.contains(objectId), "Documentation ID should contain object ID");
        
        // Test 3: Store documentation with empty reason (should be acceptable)
        GovernanceDocumentation doc3 = createTestDocumentation(objectId);
        doc3.setVersion("1.2");
        String docId3 = documentationService.storeDocumentation(objectId, doc3, TEST_USER + "-v3", "").getValue();
        
        assertNotNull(docId3, "Documentation should be stored successfully with empty reason");
        assertTrue(docId3.contains(objectId), "Documentation ID should contain object ID");
        
        // Test 4: Store documentation with whitespace-only reason (should be acceptable)
        GovernanceDocumentation doc4 = createTestDocumentation(objectId);
        doc4.setVersion("1.3");
        String docId4 = documentationService.storeDocumentation(objectId, doc4, TEST_USER + "-v4", "   ").getValue();
        
        assertNotNull(docId4, "Documentation should be stored successfully with whitespace-only reason");
        assertTrue(docId4.contains(objectId), "Documentation ID should contain object ID");
        
        // Verify all versions exist in history
        List<ObjectMetadata> history = documentationService.getDocumentationHistory(objectId);
        assertEquals(4, history.size(), "Should have 4 versions in history");
        
        // Verify latest documentation is the most recent one
        Optional<GovernanceDocumentation> latest = documentationService.getLatestDocumentation(objectId);
        assertTrue(latest.isPresent(), "Latest documentation should be available");
        assertEquals("1.3", latest.get().getVersion(), "Latest should be version 1.3");
        
        // Note: The reason parameter is stored in ObjectMetadata.reviewReason for audit purposes
        // This provides a clean audit trail through the documentation service's versioning system
        // rather than manual audit trail management within the GovernanceDocumentation object itself
    }
    
    // Helper methods
    
    private GovernanceDocumentation createTestDocumentation(String objectId) {
        GovernanceDocumentation documentation = governanceFactory.createGovernanceDocumentation();
        
        documentation.setModelName(objectId + "_v1");
        documentation.setVersion("1.0");
        documentation.setStatus(ApprovalStatus.APPROVED);
        documentation.setGenerationTimestamp(Date.from(Instant.now()));
        documentation.setGeneratedBy("Test AI Assistant");
        documentation.setApprovedBy(TEST_USER);
        documentation.setDescription("Test governance documentation for " + objectId + 
                                   " generated during integration testing");
        documentation.setDataOwner("Test Data Steward");
        documentation.setDataClassification("Test / Non-sensitive");
        
        // Add a basic compliance check
        ComplianceCheckResult gdprCheck = gdprFactory.createGDPRPolicyCheck();
        gdprCheck.setStatus(ComplianceStatus.PASSED);
        gdprCheck.setCheckTimestamp(Date.from(Instant.now()));
        gdprCheck.setSummary("GDPR compliance verified - No personal data detected in test object");
        
        documentation.getComplianceChecks().add(gdprCheck);
        
        return documentation;
    }
    
    private GovernanceDocumentation createComplexTestDocumentation(String objectId) {
        GovernanceDocumentation documentation = createTestDocumentation(objectId);
        
        // Add additional compliance checks for comprehensive testing
        ComplianceCheckResult euAiCheck = gdprFactory.createGDPRPolicyCheck(); // Using GDPR factory for simplicity
        euAiCheck.setStatus(ComplianceStatus.PASSED);
        euAiCheck.setCheckTimestamp(Date.from(Instant.now()));
        euAiCheck.setSummary("EU AI Act compliance verified - Low risk classification");
        documentation.getComplianceChecks().add(euAiCheck);
        
        ComplianceCheckResult dataQualityCheck = gdprFactory.createGDPRPolicyCheck();
        dataQualityCheck.setStatus(ComplianceStatus.PASSED);
        dataQualityCheck.setCheckTimestamp(Date.from(Instant.now()));
        dataQualityCheck.setSummary("Data quality standards verified - Schema validation passed");
        documentation.getComplianceChecks().add(dataQualityCheck);
        
        return documentation;
    }
}