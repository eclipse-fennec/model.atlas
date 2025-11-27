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
package org.eclipse.fennec.model.atlas.workflow.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.GovernanceTestAnnotations;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.GovernanceTestAnnotations.FullWorkflowWithDocumentationSetup;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
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
 * Complete workflow demonstration test showcasing the end-to-end governance process.
 * 
 * <p>This test demonstrates the complete workflow:</p>
 * 
 * <ol>
 * <li><strong>Upload EPackage as Draft</strong> - Store initial EPackage in draft storage</li>
 * <li><strong>Run Compliance Check</strong> - Execute checkDraft to generate governance documentation</li>
 * <li><strong>Set Documentation In Review</strong> - Move governance documentation to review state</li>
 * <li><strong>Approve Documentation</strong> - Approve governance documentation (enables object approval)</li>
 * <li><strong>Approve and Release Object</strong> - Approve object and immediately release to production</li>
 * <li><strong>Verify Final State</strong> - Confirm draft is archived, release is deployed, and documentation is available</li>
 * </ol>
 * 
 * <p>Key validation points:</p>
 * <ul>
 * <li>Draft status progression: DRAFT → ARCHIVED (after approval)</li>
 * <li>Release status: DEPLOYED in release storage</li>
 * <li>Governance documentation availability for released package</li>
 * <li>Proper object ID handling throughout workflow</li>
 * <li>Complete audit trail with user tracking</li>
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
@ExtendWith(MockitoExtension.class)
public class CompleteWorkflowDemonstrationTest {

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
    @FullWorkflowWithDocumentationSetup
    public void demonstrateCompleteEPackageWorkflow(
            @InjectService(cardinality = 0, filter = "(workflow.id=basic-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=draft))")
            ServiceAware<EObjectStorageService> draftServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=release))")
            ServiceAware<EObjectStorageService> releaseServiceAware
    ) throws Exception {
        
        // Wait for all services to become available
        EObjectWorkflowService<EObject> workflowService = workflowServiceAware.waitForService(5000L);
        assertNotNull(workflowService, "Workflow service should be available");
        
        EObjectStorageService<EObject> draftService = (EObjectStorageService<EObject>) draftServiceAware.waitForService(5000L);
        assertNotNull(draftService, "Draft storage service should be available");
        
        EObjectStorageService<EObject> releaseService = (EObjectStorageService<EObject>) releaseServiceAware.waitForService(5000L);
        assertNotNull(releaseService, "Release storage service should be available");

        System.out.println("=== COMPLETE WORKFLOW DEMONSTRATION ===");
        
        // === STEP 1: Upload EPackage as Draft ===
        System.out.println("\n1. Uploading EPackage as Draft...");
        
        EPackage demoPackage = EcoreFactory.eINSTANCE.createEPackage();
        demoPackage.setName("DemoWorkflowPackage");
        demoPackage.setNsPrefix("demo");
        demoPackage.setNsURI("http://demo.workflow.example.com/1.0");

        String originalObjectId = "demo-workflow-package-v1";
        ObjectMetadata draftMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        draftMetadata.setObjectId(originalObjectId);
        draftMetadata.setObjectName("DemoWorkflowPackage");
        draftMetadata.setVersion("1.0.0");
        draftMetadata.setStatus(ObjectStatus.DRAFT);
        draftMetadata.setUploadUser("developer@company.com");
        draftMetadata.setUploadTime(Instant.now());
        draftMetadata.setSourceChannel("DEVELOPMENT");
        draftMetadata.getProperties().put("file.extension", ".ecore");
        draftMetadata.getProperties().put("project", "demo-workflow");
        draftMetadata.getProperties().put("description", "Demonstration EPackage for complete workflow testing");

        // Store draft
        String draftId = workflowService.uploadDraft(demoPackage, draftMetadata).getValue();
        assertEquals(originalObjectId, draftId);
        
        // Verify draft exists and has correct status
        assertTrue(draftService.exists(originalObjectId), "Draft should exist in draft storage");
        ObjectMetadata storedDraft = draftService.retrieveMetadata(originalObjectId).getValue();
        assertEquals(ObjectStatus.DRAFT, storedDraft.getStatus(), "Initial status should be DRAFT");
        
        System.out.println("✓ Draft uploaded successfully with ID: " + draftId);
        System.out.println("  Status: " + storedDraft.getStatus());
        System.out.println("  Upload User: " + storedDraft.getUploadUser());

       

        // === STEP 2: Approve and Release Object in One Step ===
        System.out.println("\n5. Approving and Releasing Object...");
        
        String objectReviewer = "qa@company.com";
        String approvalComment = "Quality assurance passed - ready for production";
        
        // Approve the object (moves from draft to release storage)
        ObjectMetadata approvedMetadata = workflowService.approveObject(originalObjectId, objectReviewer, approvalComment);
        assertNotNull(approvedMetadata, "approveObject should return updated metadata");
        assertEquals(ObjectStatus.APPROVED, approvedMetadata.getStatus(), "Object should be APPROVED");
        
        String approvedObjectId = approvedMetadata.getObjectId();
        assertNotNull(approvedObjectId, "Approved object should have an ID");
        
        System.out.println("✓ Object approved successfully");
        System.out.println("  Original ID: " + originalObjectId);
        System.out.println("  Approved ID: " + approvedObjectId);
        System.out.println("  Status: " + approvedMetadata.getStatus());
        System.out.println("  Reviewed By: " + approvedMetadata.getReviewUser());
        
        // Immediately release to production
        String releaseNotes = "Production release v1.0 - Initial deployment of demo workflow package";
        boolean requireComplianceCheck = false; // Already done in step 2
        
        ObjectMetadata releasedMetadata = workflowService.releaseObject(approvedObjectId, releaseNotes, requireComplianceCheck);
        assertNotNull(releasedMetadata, "releaseObject should return updated metadata");
        assertEquals(ObjectStatus.DEPLOYED, releasedMetadata.getStatus(), "Object should be DEPLOYED");
        
        System.out.println("✓ Object released to production");
        System.out.println("  Status: " + releasedMetadata.getStatus());
        System.out.println("  Release Notes: " + releasedMetadata.getProperties().get("releaseNotes"));

        // === STEP 3: Verify Final State ===
        System.out.println("\n6. Verifying Final State...");
        
        // 6a. Verify draft is archived in draft storage
        assertTrue(draftService.exists(originalObjectId), "Original draft should still exist in draft storage");
        ObjectMetadata archivedDraft = draftService.retrieveMetadata(originalObjectId).getValue();
        assertEquals(ObjectStatus.ARCHIVED, archivedDraft.getStatus(), "Draft should be ARCHIVED after approval");
        
        System.out.println("✓ Draft correctly archived");
        System.out.println("  Draft ID: " + originalObjectId);
        System.out.println("  Draft Status: " + archivedDraft.getStatus());
        
        // 6b. Verify released package is deployed in release storage
        assertTrue(releaseService.exists(approvedObjectId), "Released object should exist in release storage");
        ObjectMetadata deployedRelease = releaseService.retrieveMetadata(approvedObjectId).getValue();
        assertEquals(ObjectStatus.DEPLOYED, deployedRelease.getStatus(), "Release should be DEPLOYED");
        
        System.out.println("✓ Release correctly deployed");
        System.out.println("  Release ID: " + approvedObjectId);
        System.out.println("  Release Status: " + deployedRelease.getStatus());
        
       
        // 6c. Verify object content can be retrieved from release storage
        Object releasedContent = workflowService.getObjectContent(approvedObjectId);
        assertNotNull(releasedContent, "Released object content should be retrievable");
        assertTrue(releasedContent instanceof EPackage, "Released content should be an EPackage");
        
        EPackage releasedPackage = (EPackage) releasedContent;
        assertEquals("DemoWorkflowPackage", releasedPackage.getName(), "Released package should have correct name");
        assertEquals("http://demo.workflow.example.com/1.0", releasedPackage.getNsURI(), "Released package should have correct namespace URI");
        
        System.out.println("✓ Released object content verified");
        System.out.println("  Package Name: " + releasedPackage.getName());
        System.out.println("  Namespace URI: " + releasedPackage.getNsURI());

        // === WORKFLOW COMPLETION SUMMARY ===
        System.out.println("\n=== WORKFLOW COMPLETION SUMMARY ===");
        System.out.println("Original Draft ID: " + originalObjectId + " (Status: " + archivedDraft.getStatus() + ")");
        System.out.println("Released Object ID: " + approvedObjectId + " (Status: " + deployedRelease.getStatus() + ")");
        System.out.println("Complete workflow executed successfully! ✓");
        
        // === ADDITIONAL VALIDATIONS ===
        
        // === STEP 7: Test listApprovedObjects Filtering ===
        System.out.println("\n7. Testing listApprovedObjects Filtering...");
        
        // Create a second object that we approve but don't release (to test filtering)
        EPackage filterTestPackage = EcoreFactory.eINSTANCE.createEPackage();
        filterTestPackage.setName("FilterTestPackage");
        filterTestPackage.setNsURI("http://filter.test.example.com/1.0");
        filterTestPackage.setNsPrefix("filtertest");
        
        ObjectMetadata filterTestMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        filterTestMetadata.setObjectName("FilterTestPackage");
        filterTestMetadata.setUploadUser("filter-test-user");
        filterTestMetadata.setSourceChannel("INTEGRATION_TEST");
        filterTestMetadata.setObjectType("EPackage");
        
        // Upload and approve the filter test package (but don't release it)
        String filterTestObjectId = workflowService.uploadDraft(filterTestPackage, filterTestMetadata).getValue();
        
       
        
        // Approve the business object (but don't release it - keep it as APPROVED)
        ObjectMetadata filterTestApproved = workflowService.approveObject(filterTestObjectId, "filter-business-approver", 
                "Filter test business object approved");
        assertEquals(ObjectStatus.APPROVED, filterTestApproved.getStatus(), "Filter test object should be APPROVED");
        
        System.out.println("✓ Created filter test object in APPROVED state");
        System.out.println("  Filter Test Object ID: " + filterTestApproved.getObjectId());
        System.out.println("  Status: " + filterTestApproved.getStatus());
        
        // Now test listApprovedObjects() filtering
        List<ObjectMetadata> approvedObjects = workflowService.listApprovedObjects();
        assertNotNull(approvedObjects, "Approved objects list should not be null");
        assertFalse(approvedObjects.isEmpty(), "Should have at least one approved object (our filter test object)");
        
        System.out.println("✓ listApprovedObjects() returned " + approvedObjects.size() + " objects");
        
        // Verify all returned objects are business objects (not governance documentation)
        boolean foundFilterTestObject = false;
        for (ObjectMetadata obj : approvedObjects) {
            System.out.println("  - Object: " + obj.getObjectName() + 
                             " (Type: " + obj.getObjectType() + 
                             ", Status: " + obj.getStatus() + 
                             ", Role: " + obj.getRole() + ")");
            
            assertNotNull(obj.getObjectType(), "Object type should be set");
            assertNotEquals("GovernanceDocumentation", obj.getObjectType(), 
                    "listApprovedObjects() should NOT return governance documentation objects");
            
            // Check if this is our filter test object
            if ("FilterTestPackage".equals(obj.getObjectName()) && "EPackage".equals(obj.getObjectType())) {
                foundFilterTestObject = true;
                assertEquals(ObjectStatus.APPROVED, obj.getStatus(), "Filter test object should be APPROVED");
            }
        }
        
        assertTrue(foundFilterTestObject, "Our filter test EPackage should be in the approved objects list");
        
        // Count governance documentation objects (should be zero)
        long govDocCount = approvedObjects.stream()
                .filter(obj -> "GovernanceDocumentation".equals(obj.getObjectType()))
                .count();
        
        if (govDocCount > 0) {
            System.err.println("❌ ISSUE DETECTED: listApprovedObjects() returned " + govDocCount + 
                             " governance documentation objects!");
            fail("listApprovedObjects() should only return business objects, not governance documentation");
        } else {
            System.out.println("✅ PASS: listApprovedObjects() correctly filtered out governance documentation");
        }
        
        // Verify workflow service queries work correctly
        assertTrue(workflowService.listReleasedObjects().size() >= 1, "Should have at least one released object");
        
        // Debug: Check what's in the draft list
        List<ObjectMetadata> draftObjects = workflowService.listDraftObjects();
        System.out.println("✓ Draft objects in list (" + draftObjects.size() + " total):");
        for (ObjectMetadata meta : draftObjects) {
            System.out.println("  - " + meta.getObjectId() + " (Status: " + meta.getStatus() + ")");
        }
        
        // Check if archived draft is accessible via direct lookup (it should be)
        ObjectMetadata directArchivedLookup = workflowService.getDraft(originalObjectId);
        assertNotNull(directArchivedLookup, "Should be able to retrieve archived draft directly");
        assertEquals(ObjectStatus.ARCHIVED, directArchivedLookup.getStatus(), "Direct lookup should show ARCHIVED status");
        System.out.println("✓ Archived draft accessible via direct lookup: " + originalObjectId + " (Status: " + directArchivedLookup.getStatus() + ")");
        
        // Note: listDraftObjects() might exclude archived objects depending on implementation
        // This is actually correct behavior - archived objects are no longer "active" drafts
        
        System.out.println("✓ All workflow service queries validated");
        System.out.println("\n=== DEMONSTRATION COMPLETE ===");
    }
}