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
package org.gecko.mac.governance.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.gecko.mac.governance.ApprovalStatus;
import org.gecko.mac.governance.GovernanceDocumentation;
import org.gecko.mac.governance.tests.annotations.GovernanceTestAnnotations;
import org.gecko.mac.governance.tests.annotations.GovernanceTestAnnotations.StateTransitionWorkflowSetup;
import org.gecko.mac.mgmt.api.EObjectStorageService;
import org.gecko.mac.mgmt.governanceapi.EObjectWorkflowService;
import org.gecko.mac.mgmt.governanceapi.GovernanceComplianceService;
import org.gecko.mac.mgmt.management.ManagementFactory;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for governance documentation state transition functionality.
 * 
 * <p>This test class validates the state transition methods in EObjectWorkflowService:</p>
 * 
 * <ul>
 * <li><strong>setGovernanceDocumentationDraft</strong> - Transitions documentation to DRAFT state</li>
 * <li><strong>setGovernanceDocumentationInReview</strong> - Transitions documentation to IN_REVIEW state</li>
 * <li><strong>setGovernanceDocumentationApproved</strong> - Transitions documentation to APPROVED state</li>
 * <li><strong>setGovernanceDocumentationRejected</strong> - Transitions documentation to REJECTED state</li>
 * </ul>
 * 
 * <p>Tests include both valid state transitions and invalid transitions that should be blocked
 * by the GovernanceDocumentationStateMachine validation logic.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(MockitoExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class GovernanceDocumentationStateTransitionIntegrationTest {

    @TempDir
    Path tempDir;

    @InjectBundleContext
    BundleContext bundleContext;
    
    @BeforeEach
    void setUp() {
        // Set system property for template argument resolution
        System.setProperty(GovernanceTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
        
		
    }
    
    @AfterEach
    void tearDown() {
    	
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @StateTransitionWorkflowSetup
    public void testSimpleStateTransitionWorkflow(
            @InjectService(cardinality = 0, filter = "(workflow.id=state-transition-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=draft))")
            ServiceAware<EObjectStorageService> draftServiceAware,
            @InjectService(cardinality = 0)
            ServiceAware<GovernanceComplianceService> complianceServiceAware
    ) throws Exception {
        
        // Wait for all services to become available
        EObjectWorkflowService<EObject> workflowService = workflowServiceAware.waitForService(5000L);
        assertNotNull(workflowService, "Workflow service should be available");
        
        EObjectStorageService<EObject> draftService = (EObjectStorageService<EObject>) draftServiceAware.waitForService(5000L);
        assertNotNull(draftService, "Draft storage service should be available");
        
        GovernanceComplianceService complianceService = complianceServiceAware.waitForService(5000L);
        assertNotNull(complianceService, "Compliance service should be available");

        // === STEP 1: Create and store test object ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("SimpleStateTestPackage");
        testPackage.setNsPrefix("simpletest");
        testPackage.setNsURI("http://simple.state.test.example.com");

        String objectId = "simple-state-test";
        ObjectMetadata draftMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        draftMetadata.setObjectId(objectId);
        draftMetadata.setObjectName("SimpleStateTestPackage");
        draftMetadata.setVersion("1.0.0");
        draftMetadata.setUploadUser("testUser");
        draftMetadata.setUploadTime(Instant.now());
        draftMetadata.setSourceChannel("DEVELOPMENT");
        draftMetadata.getProperties().put("file.extension", ".ecore");

        // Upload draft - should trigger automatic compliance check
        String draftId = workflowService.uploadDraft(testPackage, draftMetadata).getValue();
        assertEquals(objectId, draftId);

        // Wait for automatic compliance check to complete
        Thread.sleep(1000);

        // === STEP 2: Test individual state transitions ===
        
        // Test: Ensure we can always set to DRAFT (universal transition)
        GovernanceDocumentation draftDoc = workflowService.setGovernanceDocumentationDraft(
            objectId, "reviewer1", "Setting to DRAFT state");
        assertNotNull(draftDoc, "Draft documentation should be returned");
        assertEquals(ApprovalStatus.DRAFT, draftDoc.getStatus(), "Status should be DRAFT");

        // Test: DRAFT → IN_REVIEW transition
        GovernanceDocumentation inReviewDoc = workflowService.setGovernanceDocumentationInReview(
            objectId, "reviewer1", "Moving to IN_REVIEW state");
        assertNotNull(inReviewDoc, "In-review documentation should be returned");
        assertEquals(ApprovalStatus.IN_REVIEW, inReviewDoc.getStatus(), "Status should be IN_REVIEW");

        // Test: IN_REVIEW → APPROVED transition
        GovernanceDocumentation approvedDoc = workflowService.setGovernanceDocumentationApproved(
            objectId, "reviewer1", "Moving to APPROVED state");
        assertNotNull(approvedDoc, "Approved documentation should be returned");
        assertEquals(ApprovalStatus.APPROVED, approvedDoc.getStatus(), "Status should be APPROVED");

        // Test: APPROVED → DRAFT transition (reset capability)
        GovernanceDocumentation resetDoc = workflowService.setGovernanceDocumentationDraft(
            objectId, "reviewer1", "Resetting to DRAFT for rework");
        assertNotNull(resetDoc, "Reset documentation should be returned");
        assertEquals(ApprovalStatus.DRAFT, resetDoc.getStatus(), "Status should be DRAFT after reset");

        // Test: DRAFT → IN_REVIEW → REJECTED flow
        GovernanceDocumentation reviewDoc2 = workflowService.setGovernanceDocumentationInReview(
            objectId, "reviewer1", "Second review attempt");
        assertEquals(ApprovalStatus.IN_REVIEW, reviewDoc2.getStatus(), "Status should be IN_REVIEW");

        GovernanceDocumentation rejectedDoc = workflowService.setGovernanceDocumentationRejected(
            objectId, "reviewer1", "Rejecting for insufficient documentation");
        assertNotNull(rejectedDoc, "Rejected documentation should be returned");
        assertEquals(ApprovalStatus.REJECTED, rejectedDoc.getStatus(), "Status should be REJECTED");

        // Test: REJECTED → DRAFT transition (for rework)
        GovernanceDocumentation reworkDoc = workflowService.setGovernanceDocumentationDraft(
            objectId, "reviewer1", "Back to DRAFT for rework");
        assertEquals(ApprovalStatus.DRAFT, reworkDoc.getStatus(), "Status should be DRAFT after rework");

        // Test: REJECTED → IN_REVIEW transition (direct resubmission)
        // First go to IN_REVIEW, then REJECTED (valid path)
        GovernanceDocumentation reviewDoc3 = workflowService.setGovernanceDocumentationInReview(objectId, "reviewer1", "Moving to review for rejection test");
        assertEquals(ApprovalStatus.IN_REVIEW, reviewDoc3.getStatus(), "Status should be IN_REVIEW");
        
        GovernanceDocumentation rejectedDoc2 = workflowService.setGovernanceDocumentationRejected(objectId, "reviewer1", "Rejecting for resubmission test");
        assertEquals(ApprovalStatus.REJECTED, rejectedDoc2.getStatus(), "Status should be REJECTED");
        
        // Now test REJECTED → IN_REVIEW transition (resubmission)
        GovernanceDocumentation resubmitDoc = workflowService.setGovernanceDocumentationInReview(
            objectId, "reviewer1", "Resubmitting after addressing issues");
        assertEquals(ApprovalStatus.IN_REVIEW, resubmitDoc.getStatus(), "Status should be IN_REVIEW after resubmission");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @StateTransitionWorkflowSetup
    public void testInvalidStateTransitions(
            @InjectService(cardinality = 0, filter = "(workflow.id=state-transition-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware
    ) throws Exception {
        
        EObjectWorkflowService<EObject> workflowService = workflowServiceAware.waitForService(5000L);

        // Create test object
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("InvalidTransitionTestPackage");
        testPackage.setNsPrefix("invalidtest");
        testPackage.setNsURI("http://invalid.transition.test.example.com");

        String objectId = "invalid-transition-test";
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName("InvalidTransitionTestPackage");
        metadata.setVersion("1.0.0");
        metadata.setUploadUser("testUser");
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel("DEVELOPMENT");

        workflowService.uploadDraft(testPackage, metadata).getValue();
        Thread.sleep(500);

        // Ensure we start from a known state (DRAFT)
        workflowService.setGovernanceDocumentationDraft(objectId, "reviewer", "Setting to draft for invalid transition tests");

        // === TEST 1: Invalid transition DRAFT → APPROVED (must go through IN_REVIEW) ===
        
        IllegalStateException exception1 = assertThrows(IllegalStateException.class, () -> {
            workflowService.setGovernanceDocumentationApproved(objectId, "reviewer", "Attempting invalid direct approval");
        });
        assertTrue(exception1.getMessage().contains("Invalid governance documentation state transition"),
                "Should block DRAFT → APPROVED transition");

        // === TEST 2: Invalid transition DRAFT → REJECTED (must go through IN_REVIEW) ===
        
        IllegalStateException exception2 = assertThrows(IllegalStateException.class, () -> {
            workflowService.setGovernanceDocumentationRejected(objectId, "reviewer", "Attempting invalid direct rejection");
        });
        assertTrue(exception2.getMessage().contains("Invalid governance documentation state transition"),
                "Should block DRAFT → REJECTED transition");

        // === TEST 3: Set to IN_REVIEW, then test invalid transition IN_REVIEW → IN_REVIEW ===
        
        workflowService.setGovernanceDocumentationInReview(objectId, "reviewer", "Setting to in-review for self-transition test");
        
        IllegalStateException exception3 = assertThrows(IllegalStateException.class, () -> {
            workflowService.setGovernanceDocumentationInReview(objectId, "reviewer", "Attempting self-transition");
        });
        assertTrue(exception3.getMessage().contains("Invalid governance documentation state transition"),
                "Should block IN_REVIEW → IN_REVIEW transition");

        // === TEST 4: Test invalid transition APPROVED → IN_REVIEW (must go through DRAFT first) ===
        
        // First approve the documentation
        workflowService.setGovernanceDocumentationApproved(objectId, "reviewer", "Approving for invalid transition test");
        
        IllegalStateException exception4 = assertThrows(IllegalStateException.class, () -> {
            workflowService.setGovernanceDocumentationInReview(objectId, "reviewer", "Attempting invalid return to review");
        });
        assertTrue(exception4.getMessage().contains("Invalid governance documentation state transition"),
                "Should block APPROVED → IN_REVIEW transition");

        // === TEST 5: Test invalid transition APPROVED → REJECTED (must go through proper review flow) ===
        
        IllegalStateException exception5 = assertThrows(IllegalStateException.class, () -> {
            workflowService.setGovernanceDocumentationRejected(objectId, "reviewer", "Attempting invalid rejection from approved");
        });
        assertTrue(exception5.getMessage().contains("Invalid governance documentation state transition"),
                "Should block APPROVED → REJECTED transition");

        // === TEST 6: Test invalid self-transitions for APPROVED and REJECTED states ===
        
        IllegalStateException exception6 = assertThrows(IllegalStateException.class, () -> {
            workflowService.setGovernanceDocumentationApproved(objectId, "reviewer", "Attempting self-transition to approved");
        });
        assertTrue(exception6.getMessage().contains("Invalid governance documentation state transition"),
                "Should block APPROVED → APPROVED transition");

        // Set to rejected for rejected self-transition test
        workflowService.setGovernanceDocumentationDraft(objectId, "reviewer", "Back to draft");
        workflowService.setGovernanceDocumentationInReview(objectId, "reviewer", "To review");
        workflowService.setGovernanceDocumentationRejected(objectId, "reviewer", "To rejected for self-transition test");
        
        IllegalStateException exception7 = assertThrows(IllegalStateException.class, () -> {
            workflowService.setGovernanceDocumentationRejected(objectId, "reviewer", "Attempting self-transition to rejected");
        });
        assertTrue(exception7.getMessage().contains("Invalid governance documentation state transition"),
                "Should block REJECTED → REJECTED transition");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @StateTransitionWorkflowSetup
    public void testStateTransitionReasonParameter(
            @InjectService(cardinality = 0, filter = "(workflow.id=state-transition-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware
    ) throws Exception {
        
        EObjectWorkflowService<EObject> workflowService = workflowServiceAware.waitForService(5000L);

        // Create test object
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("ReasonParameterTestPackage");
        testPackage.setNsPrefix("reasontest");
        testPackage.setNsURI("http://reason.parameter.test.example.com");

        String objectId = "reason-parameter-test";
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName("ReasonParameterTestPackage");
        metadata.setVersion("1.0.0");
        metadata.setUploadUser("testUser");
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel("DEVELOPMENT");

        workflowService.uploadDraft(testPackage, metadata).getValue();
        Thread.sleep(500);

        // === TEST 1: Test with meaningful reason ===
        
        String meaningfulReason = "Initial governance documentation review for compliance verification";
        GovernanceDocumentation draftDoc = workflowService.setGovernanceDocumentationDraft(objectId, "reviewer1", meaningfulReason);
        assertNotNull(draftDoc, "Documentation should be returned with meaningful reason");
        assertEquals(ApprovalStatus.DRAFT, draftDoc.getStatus());

        // === TEST 2: Test with null reason (should be acceptable) ===
        
        GovernanceDocumentation inReviewDoc = workflowService.setGovernanceDocumentationInReview(objectId, "reviewer1", null);
        assertNotNull(inReviewDoc, "Documentation should be returned with null reason");
        assertEquals(ApprovalStatus.IN_REVIEW, inReviewDoc.getStatus());

        // === TEST 3: Test with empty reason (should be acceptable) ===
        
        GovernanceDocumentation approvedDoc = workflowService.setGovernanceDocumentationApproved(objectId, "reviewer1", "");
        assertNotNull(approvedDoc, "Documentation should be returned with empty reason");
        assertEquals(ApprovalStatus.APPROVED, approvedDoc.getStatus());

        // === TEST 4: Test with whitespace-only reason (should be acceptable) ===
        
        workflowService.setGovernanceDocumentationDraft(objectId, "reviewer1", "Reset for whitespace test");
        workflowService.setGovernanceDocumentationInReview(objectId, "reviewer1", "To review for rejection");
        
        GovernanceDocumentation rejectedDoc = workflowService.setGovernanceDocumentationRejected(objectId, "reviewer1", "   ");
        assertNotNull(rejectedDoc, "Documentation should be returned with whitespace-only reason");
        assertEquals(ApprovalStatus.REJECTED, rejectedDoc.getStatus());

        // Note: The reason parameter is stored in the documentation service's audit trail
        // through the versioning system, providing a clean audit trail without manual management
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @StateTransitionWorkflowSetup
    public void testNonExistentObjectStateTransition(
            @InjectService(cardinality = 0, filter = "(workflow.id=state-transition-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware
    ) throws Exception {
        
        EObjectWorkflowService<EObject> workflowService = workflowServiceAware.waitForService(5000L);

        String nonExistentObjectId = "non-existent-object-" + System.currentTimeMillis();

        // === TEST: State transitions should handle non-existent objects gracefully ===
        
        // The implementation should either create governance documentation for the object
        // or throw a meaningful exception - depending on the business logic
        // This test verifies that we don't get unexpected runtime errors
        
        try {
            GovernanceDocumentation result = workflowService.setGovernanceDocumentationDraft(
                nonExistentObjectId, "reviewer", "Testing non-existent object handling");
            
            // If successful, verify the result is valid
            assertNotNull(result, "Result should not be null if operation succeeds");
            assertEquals(ApprovalStatus.DRAFT, result.getStatus(), "Status should be DRAFT if operation succeeds");
            
        } catch (Exception e) {
            // If it throws an exception, it should be a meaningful one
            assertNotNull(e.getMessage(), "Exception should have a meaningful message");
            assertTrue(e.getMessage().contains("object") || e.getMessage().contains("found") || e.getMessage().contains("exist"),
                    "Exception message should indicate object-related issue");
        }
    }
}