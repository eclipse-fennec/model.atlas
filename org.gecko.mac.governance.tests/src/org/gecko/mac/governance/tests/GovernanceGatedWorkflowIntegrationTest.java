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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.gecko.mac.governance.ApprovalStatus;
import org.gecko.mac.governance.GovernanceDocumentation;
import org.gecko.mac.governance.tests.annotations.GovernanceTestAnnotations;
import org.gecko.mac.governance.tests.annotations.GovernanceTestAnnotations.GovernanceGatedWorkflowSetup;
import org.gecko.mac.governance.tests.support.WorkflowTestBase;
import org.gecko.mac.mgmt.api.EObjectRegistryService;
import org.gecko.mac.mgmt.api.EObjectStorageService;
import org.gecko.mac.mgmt.governanceapi.EObjectWorkflowService;
import org.gecko.mac.mgmt.governanceapi.GovernanceComplianceService;
import org.gecko.mac.mgmt.governanceapi.GovernanceDocumentationService;
import org.gecko.mac.mgmt.management.ManagementFactory;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.gecko.mac.mgmt.management.ObjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for governance-gated workflow functionality.
 * 
 * <p>This test class validates the enhanced workflow operations with governance gates:</p>
 * 
 * <ul>
 * <li><strong>Governance Gate Enforcement</strong> - Blocking approval without approved governance documentation</li>
 * <li><strong>Automatic Compliance Checks</strong> - Triggering compliance checks after draft upload</li>
 * <li><strong>Governance Documentation Lifecycle</strong> - Complete governance document workflow integration</li>
 * <li><strong>Error Scenarios</strong> - Proper error handling for governance violations</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class GovernanceGatedWorkflowIntegrationTest extends WorkflowTestBase {

    @TempDir
    Path tempDir;
    
    @InjectBundleContext
    BundleContext context;

    @BeforeEach
    void setUp() {
        // Set system property for template argument resolution
        System.setProperty(GovernanceTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
    }
    
    @Override
    protected BundleContext getBundleContext() {
        return context;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @GovernanceGatedWorkflowSetup
    public void testGovernanceGatedWorkflowSuccess(
            @InjectService(cardinality = 0, filter = "(workflow.id=governance-gated-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=draft))")
            ServiceAware<EObjectStorageService> draftServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=release))")
            ServiceAware<EObjectStorageService> releaseServiceAware,
            @InjectService(cardinality = 0)
            ServiceAware<GovernanceComplianceService> complianceServiceAware,
            @InjectService(cardinality = 0)
            ServiceAware<GovernanceDocumentationService> docServiceAware,
            @InjectService(cardinality = 0, filter = "(registry.type=shared)")
            ServiceAware<EObjectRegistryService> registryServiceAware
    ) throws Exception {
        
        // Wait for all services to become available
        EObjectWorkflowService<EObject> workflowService = workflowServiceAware.waitForService(5000L);
        assertNotNull(workflowService, "Workflow service should be available");
        
        EObjectStorageService<EObject> draftService = (EObjectStorageService<EObject>) draftServiceAware.waitForService(5000L);
        assertNotNull(draftService, "Draft storage service should be available");
        
        EObjectStorageService<EObject> releaseService = (EObjectStorageService<EObject>) releaseServiceAware.waitForService(5000L);
        assertNotNull(releaseService, "Release storage service should be available");
        
        GovernanceComplianceService complianceService = complianceServiceAware.waitForService(5000L);
        assertNotNull(complianceService, "Compliance service should be available");
        
        GovernanceDocumentationService docService = docServiceAware.waitForService(5000L);
        assertNotNull(docService, "Documentation service should be available");
        
        EObjectRegistryService<EObject> registryService = (EObjectRegistryService<EObject>) registryServiceAware.waitForService(5000L);
        assertNotNull(registryService, "Registry service should be available");

        // === STEP 1: Create and store draft object ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("GovernanceGatedTestPackage");
        testPackage.setNsPrefix("govtest");
        testPackage.setNsURI("http://governance.test.example.com");

        String objectId = "governance-gated-test";
        ObjectMetadata draftMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        draftMetadata.setObjectId(objectId);
        draftMetadata.setObjectName("GovernanceGatedTestPackage");
        draftMetadata.setVersion("1.0.0");
        draftMetadata.setUploadUser("developerUser");
        draftMetadata.setUploadTime(Instant.now());
        draftMetadata.setSourceChannel("DEVELOPMENT");
        draftMetadata.getProperties().put("file.extension", ".ecore");
        draftMetadata.getProperties().put("project", "governance-gated-test");

        // Upload draft - should trigger automatic compliance check
        String draftId = workflowService.uploadDraft(testPackage, draftMetadata).getValue();
        assertEquals(objectId, draftId);

        // Verify draft is stored in draft service only
        assertTrue(draftService.exists(objectId), "Draft should exist in draft storage");
        assertFalse(releaseService.exists(objectId), "Draft should not exist in release storage");

        // === STEP 2: Wait for automatic compliance check to complete and set governance documentation to DRAFT status ===
        
        // Give the automatic compliance check some time to complete
        Thread.sleep(1000);
        
        // Verify that governance documentation was created by the automatic compliance check
        GovernanceDocumentation govDoc = complianceService.getComplianceStatus(objectId).getValue();
        assertNotNull(govDoc, "Governance documentation should exist after automatic compliance check");
        
        // Ensure governance documentation is in DRAFT status for testing the governance gate
        // (The automatic compliance check might create it in various states, so we explicitly set it to DRAFT)
        govDoc = workflowService.setGovernanceDocumentationDraft(objectId, "developerUser", "Ensuring documentation is in DRAFT status for governance gate testing");
        assertEquals(ApprovalStatus.DRAFT, govDoc.getStatus(), "Governance documentation should be in DRAFT status after explicit transition");

        // === STEP 3: Attempt approval without approved governance documentation (should fail) ===
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            workflowService.approveObject(objectId, "qaUser", "Attempting approval without approved governance");
        });
        assertTrue(exception.getMessage().contains("Governance documentation is not approved"), 
                "Should block approval when governance documentation is not approved");

        // === STEP 4: Simulate governance documentation approval using proper state transitions ===
        
        // First transition to IN_REVIEW status
        govDoc = workflowService.setGovernanceDocumentationInReview(objectId, "governanceReviewer", "Submitting governance documentation for review");
        assertEquals(ApprovalStatus.IN_REVIEW, govDoc.getStatus(), "Documentation should be in IN_REVIEW status");
        
        // Then approve the governance documentation (simulating external governance review process)
        govDoc = workflowService.setGovernanceDocumentationApproved(objectId, "governanceReviewer", "Governance documentation approved after thorough review");
        assertEquals(ApprovalStatus.APPROVED, govDoc.getStatus(), "Documentation should be in APPROVED status");
        assertNotNull(govDoc.getApprovedBy(), "Approved by should be set");

        // === STEP 5: Approve the object now that governance documentation is approved ===
        
        ObjectMetadata approvedMetadata = workflowService.approveObject(objectId, "qaUser", "Quality assurance approval with governance");
        
        // Verify approval metadata updates
        assertNotNull(approvedMetadata, "Approval should return updated metadata");
        assertEquals(ObjectStatus.APPROVED, approvedMetadata.getStatus());
        assertEquals("qaUser", approvedMetadata.getReviewUser());
        assertEquals("Quality assurance approval with governance", approvedMetadata.getReviewReason());
        assertNotNull(approvedMetadata.getReviewTime());

        // Get the new approved objectId from the returned metadata
        String approvedObjectId = approvedMetadata.getObjectId();
        assertNotNull(approvedObjectId, "Approved object should have a new objectId");
        
        // Verify object moved to release storage with new objectId
        assertTrue(releaseService.exists(approvedObjectId), "Approved object should exist in release storage with new ID");
        assertFalse(releaseService.exists(objectId), "Original draft ID should not exist in release storage");
        
        // Verify draft handling (archived by default)
        assertTrue(draftService.exists(objectId), "Draft should still exist (archived by default)");
        ObjectMetadata archivedDraft = draftService.retrieveMetadata(objectId).getValue();
        assertEquals(ObjectStatus.ARCHIVED, archivedDraft.getStatus(), "Draft should be archived");

        // === STEP 6: Release the approved object ===
        
        ObjectMetadata releasedMetadata = workflowService.releaseObject(approvedObjectId, "Production release with governance compliance", false);
        
        // Verify release metadata updates
        assertNotNull(releasedMetadata, "Release should return updated metadata");
        assertEquals(ObjectStatus.DEPLOYED, releasedMetadata.getStatus());
        
        // Verify release notes are stored
        assertTrue(releasedMetadata.getProperties().containsKey("releaseNotes"), "Release notes should be stored");
        assertEquals("Production release with governance compliance", releasedMetadata.getProperties().get("releaseNotes"));

        // === STEP 7: Verify final states ===
        
        // Verify workflow query methods
        List<ObjectMetadata> approvedObjects = workflowService.listApprovedObjects();
        System.out.println("Approved objects after release: " + approvedObjects.size());
        for (ObjectMetadata obj : approvedObjects) {
            System.out.println("  - Object ID: " + obj.getObjectId() + ", Status: " + obj.getStatus());
        }
        assertTrue(approvedObjects.isEmpty(), "Should have no approved objects after release");
        
        List<ObjectMetadata> releasedObjects = workflowService.listReleasedObjects();
        System.out.println("Released objects after release: " + releasedObjects.size());
        for (ObjectMetadata obj : releasedObjects) {
            System.out.println("  - Object ID: " + obj.getObjectId() + ", Status: " + obj.getStatus());
        }
        assertNotNull(releasedObjects, "Should find the released objects");
        assertTrue(releasedObjects.size() >= 1, "Should have at least one released object");
        
        boolean foundReleasedObject = releasedObjects.stream().anyMatch(obj -> approvedObjectId.equals(obj.getObjectId()));
        assertTrue(foundReleasedObject, "Should find released object in the list");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @GovernanceGatedWorkflowSetup
    public void testGovernanceGateBlocking(
            @InjectService(cardinality = 0, filter = "(workflow.id=governance-gated-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware,
            @InjectService(cardinality = 0)
            ServiceAware<GovernanceComplianceService> complianceServiceAware,
            @InjectService(cardinality = 0)
            ServiceAware<GovernanceDocumentationService> docServiceAware
    ) throws Exception {
        
        EObjectWorkflowService<EObject> workflowService = workflowServiceAware.waitForService(5000L);
        GovernanceComplianceService complianceService = complianceServiceAware.waitForService(5000L);
        GovernanceDocumentationService docService = docServiceAware.waitForService(5000L);

        // === Create draft without any governance documentation ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("BlockedTestPackage");
        testPackage.setNsPrefix("blocked");
        testPackage.setNsURI("http://blocked.test.example.com");

        String objectId = "governance-blocked-test";
        ObjectMetadata draftMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        draftMetadata.setObjectId(objectId);
        draftMetadata.setObjectName("BlockedTestPackage");
        draftMetadata.setVersion("1.0.0");
        draftMetadata.setStatus(ObjectStatus.DRAFT);
        draftMetadata.setUploadUser("developerUser");
        draftMetadata.setUploadTime(Instant.now());
        draftMetadata.setSourceChannel("DEVELOPMENT");

        // Upload draft
        String draftId = workflowService.uploadDraft(testPackage, draftMetadata).getValue();
        assertEquals(objectId, draftId);

        // === TEST 1: Approval should fail when no governance documentation exists ===
        
        // Wait a bit for automatic compliance check, then clear any governance documentation to test the "no documentation" scenario
        Thread.sleep(500);
        
        // Actually delete any governance documentation to test the blocking scenario
        try {
            docService.deleteAllDocumentation(objectId).getValue();
            System.out.println("Deleted governance documentation for object: " + objectId + " to test 'no documentation' scenario");
        } catch (Exception e) {
            // Expected if no documentation exists
            System.out.println("No governance documentation to delete for object: " + objectId);
        }

        IllegalStateException exception1 = assertThrows(IllegalStateException.class, () -> {
            workflowService.approveObject(objectId, "qaUser", "Attempting approval without any governance documentation");
        });
        assertTrue(exception1.getMessage().contains("No governance documentation found") ||
                  exception1.getMessage().contains("Governance documentation is not approved"), 
                "Should block approval when no governance documentation exists");

        // === TEST 2: Approval should fail when governance documentation is not approved ===
        
        // Create governance documentation but keep it in DRAFT status
        GovernanceDocumentation draftGovDoc = complianceService.runComplianceChecks(objectId, "reviewer", "Test compliance check").getValue();
        assertNotNull(draftGovDoc, "Compliance check should create governance documentation");
        assertEquals(ApprovalStatus.DRAFT, draftGovDoc.getStatus(), "Governance documentation should be in DRAFT status");

        IllegalStateException exception2 = assertThrows(IllegalStateException.class, () -> {
            workflowService.approveObject(objectId, "qaUser", "Attempting approval with draft governance documentation");
        });
        assertTrue(exception2.getMessage().contains("Governance documentation is not approved"), 
                "Should block approval when governance documentation is in DRAFT status");

        // === TEST 3: Test with IN_REVIEW status ===
        
        draftGovDoc.setStatus(ApprovalStatus.IN_REVIEW);
        
        IllegalStateException exception3 = assertThrows(IllegalStateException.class, () -> {
            workflowService.approveObject(objectId, "qaUser", "Attempting approval with in-review governance documentation");
        });
        assertTrue(exception3.getMessage().contains("Governance documentation is not approved"), 
                "Should block approval when governance documentation is in IN_REVIEW status");

        // === TEST 4: Test with REJECTED status ===
        
        draftGovDoc.setStatus(ApprovalStatus.REJECTED);
        
        IllegalStateException exception4 = assertThrows(IllegalStateException.class, () -> {
            workflowService.approveObject(objectId, "qaUser", "Attempting approval with rejected governance documentation");
        });
        assertTrue(exception4.getMessage().contains("Governance documentation is not approved"), 
                "Should block approval when governance documentation is in REJECTED status");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @GovernanceGatedWorkflowSetup
    public void testAutomaticComplianceCheckTrigger(
            @InjectService(cardinality = 0, filter = "(workflow.id=governance-gated-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware,
            @InjectService(cardinality = 0)
            ServiceAware<GovernanceComplianceService> complianceServiceAware
    ) throws Exception {
        
        EObjectWorkflowService<EObject> workflowService = workflowServiceAware.waitForService(5000L);
        GovernanceComplianceService complianceService = complianceServiceAware.waitForService(5000L);

        // === Create and upload draft ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("AutoComplianceTestPackage");
        testPackage.setNsPrefix("autocomp");
        testPackage.setNsURI("http://autocomp.test.example.com");

        String objectId = "auto-compliance-test";
        ObjectMetadata draftMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        draftMetadata.setObjectId(objectId);
        draftMetadata.setObjectName("AutoComplianceTestPackage");
        draftMetadata.setVersion("1.0.0");
        draftMetadata.setStatus(ObjectStatus.DRAFT);
        draftMetadata.setUploadUser("developerUser");
        draftMetadata.setUploadTime(Instant.now());
        draftMetadata.setSourceChannel("DEVELOPMENT");

        // Verify no governance documentation exists before upload
        try {
            GovernanceDocumentation preDraftDoc = complianceService.getComplianceStatus(objectId).getValue();
            assertTrue(preDraftDoc == null, "Should have no governance documentation before upload");
        } catch (Exception e) {
            // Expected if no documentation exists
        }

        // Upload draft - should automatically trigger compliance check
        String draftId = workflowService.uploadDraft(testPackage, draftMetadata).getValue();
        assertEquals(objectId, draftId);

        // === Verify automatic compliance check was triggered ===
        
        // Give the automatic compliance check some time to complete (it's asynchronous)
        Thread.sleep(2000);
        
        // Verify that governance documentation was automatically created
        GovernanceDocumentation govDoc = complianceService.getComplianceStatus(objectId).getValue();
        assertNotNull(govDoc, "Governance documentation should be automatically created after draft upload");
        assertNotNull(govDoc.getGenerationTimestamp(), "Governance documentation should have generation timestamp");
        assertEquals("developerUser", govDoc.getGeneratedBy(), "Governance documentation should be generated by the upload user");
        
        // The automatic compliance check should create documentation in DRAFT status initially
        // (it would need external approval to move to APPROVED status)
        assertTrue(govDoc.getStatus() == ApprovalStatus.DRAFT || 
                  govDoc.getStatus() == ApprovalStatus.IN_REVIEW ||
                  govDoc.getStatus() == ApprovalStatus.APPROVED,
                  "Governance documentation should be in a valid status after automatic compliance check");
    }
}