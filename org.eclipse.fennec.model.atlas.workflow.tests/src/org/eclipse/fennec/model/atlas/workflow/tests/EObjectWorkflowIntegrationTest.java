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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
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
 * Integration tests for the complete EObject workflow lifecycle.
 * 
 * <p>This test class validates the end-to-end workflow operations:</p>
 * 
 * <ul>
 * <li><strong>Draft Management</strong> - Creating and storing draft objects</li>
 * <li><strong>Approval Process</strong> - Approving drafts with metadata updates</li>
 * <li><strong>Release Process</strong> - Releasing approved objects to deployment</li>
 * <li><strong>Rejection Handling</strong> - Rejecting drafts with audit trail</li>
 * <li><strong>Cross-Storage Integration</strong> - Interaction between draft, release, and documentation storage</li>
 * <li><strong>Registry Integration</strong> - Shared registry visibility across all storage services</li>
 * </ul>
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
public class EObjectWorkflowIntegrationTest {

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
    public void testCompleteWorkflowLifecycle(
            @InjectService(cardinality = 0, filter = "(workflow.id=basic-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=draft))")
            ServiceAware<EObjectStorageService> draftServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=release))")
            ServiceAware<EObjectStorageService> releaseServiceAware,
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
        
        EObjectRegistryService<EObject> registryService = (EObjectRegistryService<EObject>) registryServiceAware.waitForService(5000L);
        assertNotNull(registryService, "Registry service should be available");

        // === STEP 1: Create and store draft object ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("WorkflowTestPackage");
        testPackage.setNsPrefix("workflow");
        testPackage.setNsURI("http://workflow.test.example.com");

        String objectId = "workflow-lifecycle-test";
        ObjectMetadata draftMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        draftMetadata.setObjectId(objectId);
        draftMetadata.setObjectName("WorkflowTestPackage");
        draftMetadata.setVersion("1.0.0");
        draftMetadata.setStatus(ObjectStatus.DRAFT);
        draftMetadata.setUploadUser("developerUser");
        draftMetadata.setUploadTime(Instant.now());
        draftMetadata.setSourceChannel("DEVELOPMENT");
        draftMetadata.getProperties().put("file.extension", ".ecore");
        draftMetadata.getProperties().put("project", "workflow-test");

        // Store draft using workflow service (should delegate to draft storage)
        String draftId = workflowService.uploadDraft(testPackage, draftMetadata).getValue();
        assertEquals(objectId, draftId);

        // Verify draft is stored in draft service only
        assertTrue(draftService.exists(objectId), "Draft should exist in draft storage");
        assertFalse(releaseService.exists(objectId), "Draft should not exist in release storage");

        // Verify registry can see the draft
        Optional<ObjectMetadata> draftFromRegistry = registryService.getMetadata(objectId);
        assertTrue(draftFromRegistry.isPresent(), "Registry should see draft metadata");
        assertEquals(ObjectStatus.DRAFT, draftFromRegistry.get().getStatus());
        assertEquals("draft", draftFromRegistry.get().getRole());

        // === STEP 2: Approve the draft ===
        
        ObjectMetadata approvedMetadata = workflowService.approveObject(objectId, "qaUser", "Quality assurance approval");
        
        // Verify approval metadata updates
        assertNotNull(approvedMetadata, "Approval should return updated metadata");
        assertEquals(ObjectStatus.APPROVED, approvedMetadata.getStatus());
        assertEquals("qaUser", approvedMetadata.getReviewUser());
        assertEquals("Quality assurance approval", approvedMetadata.getReviewReason());
        assertNotNull(approvedMetadata.getReviewTime());
        assertEquals("qaUser", approvedMetadata.getLastChangeUser());
        assertNotNull(approvedMetadata.getLastChangeTime());

        // Get the new approved objectId from the returned metadata
        String approvedObjectId = approvedMetadata.getObjectId();
        assertNotNull(approvedObjectId, "Approved object should have a new objectId");
        
        // Verify object moved to release storage with new objectId
        assertTrue(releaseService.exists(approvedObjectId), "Approved object should exist in release storage with new ID");
        assertFalse(releaseService.exists(objectId), "Original draft ID should not exist in release storage");
        
        // Verify draft handling based on configuration (archived by default)
        assertTrue(draftService.exists(objectId), "Draft should still exist (archived by default)");
        ObjectMetadata archivedDraft = draftService.retrieveMetadata(objectId).getValue();
        assertEquals(ObjectStatus.ARCHIVED, archivedDraft.getStatus(), "Draft should be archived");

        // Verify release storage has correct metadata
        ObjectMetadata releaseMetadata = releaseService.retrieveMetadata(approvedObjectId).getValue();
        assertNotNull(releaseMetadata);
        assertEquals(ObjectStatus.APPROVED, releaseMetadata.getStatus());
        assertEquals("release", releaseMetadata.getRole());
        assertEquals("qaUser", releaseMetadata.getReviewUser());

        // Verify registry reflects the updated status and role for approved object
        Optional<ObjectMetadata> approvedFromRegistry = registryService.getMetadata(approvedObjectId);
        assertTrue(approvedFromRegistry.isPresent(), "Registry should see approved metadata");
        assertEquals(ObjectStatus.APPROVED, approvedFromRegistry.get().getStatus());
        assertEquals("release", approvedFromRegistry.get().getRole());

        // === STEP 3: Release the approved object ===
        
        ObjectMetadata releasedMetadata = workflowService.releaseObject(approvedObjectId, "Release notes: First production release", false);
        
        // Verify release metadata updates
        assertNotNull(releasedMetadata, "Release should return updated metadata");
        assertEquals(ObjectStatus.DEPLOYED, releasedMetadata.getStatus());
        assertNotNull(releasedMetadata.getLastChangeTime());
        
        // Verify release notes are stored in properties
        assertTrue(releasedMetadata.getProperties().containsKey("releaseNotes"), "Release notes should be stored");
        assertEquals("Release notes: First production release", releasedMetadata.getProperties().get("releaseNotes"));

        // Verify object is still in release storage with updated status
        assertTrue(releaseService.exists(approvedObjectId), "Released object should remain in release storage");
        ObjectMetadata deployedMetadata = releaseService.retrieveMetadata(approvedObjectId).getValue();
        assertEquals(ObjectStatus.DEPLOYED, deployedMetadata.getStatus());

        // Verify registry reflects the deployed status
        Optional<ObjectMetadata> deployedFromRegistry = registryService.getMetadata(approvedObjectId);
        assertTrue(deployedFromRegistry.isPresent(), "Registry should see deployed metadata");
        assertEquals(ObjectStatus.DEPLOYED, deployedFromRegistry.get().getStatus());

        // === STEP 4: Verify workflow query methods ===
        
        // Test approved objects listing (should be empty after release)
        List<ObjectMetadata> approvedObjects = workflowService.listApprovedObjects();
        assertTrue(approvedObjects.isEmpty(), "Should have no approved objects after release");

        // === STEP 5: Create a second object to test multiple object listing ===
        
        EPackage secondPackage = EcoreFactory.eINSTANCE.createEPackage();
        secondPackage.setName("SecondTestPackage");
        secondPackage.setNsPrefix("second");
        secondPackage.setNsURI("http://second.test.example.com");

        String secondObjectId = "workflow-lifecycle-test-2";
        ObjectMetadata secondMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        secondMetadata.setObjectId(secondObjectId);
        secondMetadata.setObjectName("SecondTestPackage");
        secondMetadata.setVersion("1.0.0");
        secondMetadata.setStatus(ObjectStatus.DRAFT);
        secondMetadata.setUploadUser("developerUser");
        secondMetadata.setUploadTime(Instant.now());
        secondMetadata.setSourceChannel("DEVELOPMENT");
        secondMetadata.getProperties().put("file.extension", ".ecore");
        secondMetadata.getProperties().put("project", "workflow-test-2");

        // Upload second draft
        String secondDraftId = workflowService.uploadDraft(secondPackage, secondMetadata).getValue();
        assertEquals(secondObjectId, secondDraftId);
        
        // Approve and release second object
        ObjectMetadata secondApprovedMetadata = workflowService.approveObject(secondObjectId, "qaUser", "Second object approval");
        String secondApprovedObjectId = secondApprovedMetadata.getObjectId();
        workflowService.releaseObject(secondApprovedObjectId, "Release notes: Second production release", false);
        
        // Test released objects listing - should now have multiple objects
        List<ObjectMetadata> releasedObjects = workflowService.listReleasedObjects();
        assertNotNull(releasedObjects, "Should find the released objects");
        assertTrue(releasedObjects.size() >= 2, "Should have at least two released objects");
        
        // Verify both objects are in the released list
        boolean foundFirstObject = releasedObjects.stream().anyMatch(obj -> approvedObjectId.equals(obj.getObjectId()));
        boolean foundSecondObject = releasedObjects.stream().anyMatch(obj -> secondApprovedObjectId.equals(obj.getObjectId()));
        assertTrue(foundFirstObject, "Should find first released object in the list");
        assertTrue(foundSecondObject, "Should find second released object in the list");
        
        // Verify all objects have DEPLOYED status
        releasedObjects.forEach(obj -> assertEquals(ObjectStatus.DEPLOYED, obj.getStatus(), "All released objects should have DEPLOYED status"));

        // Test object retrieval using approved objectId
        ObjectMetadata retrievedMetadata = workflowService.getObject(approvedObjectId);
        assertNotNull(retrievedMetadata, "Should retrieve object metadata");
        assertEquals(ObjectStatus.DEPLOYED, retrievedMetadata.getStatus());

        // Test object content retrieval using approved objectId
        Object retrievedContent = workflowService.getObjectContent(approvedObjectId);
        assertNotNull(retrievedContent, "Should retrieve object content");
        assertTrue(retrievedContent instanceof EPackage);
        assertEquals("WorkflowTestPackage", ((EPackage) retrievedContent).getName());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @FullWorkflowWithDocumentationSetup
    public void testDraftRejectionWorkflow(
    		@InjectService(cardinality = 0, filter = "(workflow.id=basic-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=draft))")
            ServiceAware<EObjectStorageService> draftServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=release))")
            ServiceAware<EObjectStorageService> releaseServiceAware,
            @InjectService(cardinality = 0, filter = "(registry.type=shared)")
            ServiceAware<EObjectRegistryService> registryServiceAware
    ) throws Exception {
        
        // Wait for services
        EObjectWorkflowService<EObject> workflowService = workflowServiceAware.waitForService(5000L);
        EObjectStorageService<EObject> draftService = (EObjectStorageService<EObject>) draftServiceAware.waitForService(5000L);
        EObjectStorageService<EObject> releaseService = (EObjectStorageService<EObject>) releaseServiceAware.waitForService(5000L);
        EObjectRegistryService<EObject> registryService = (EObjectRegistryService<EObject>) registryServiceAware.waitForService(5000L);

        // Create and store draft
        EPackage rejectionPackage = EcoreFactory.eINSTANCE.createEPackage();
        rejectionPackage.setName("RejectionTestPackage");
        rejectionPackage.setNsPrefix("rejection");
        rejectionPackage.setNsURI("http://rejection.test.example.com");

        String objectId = "rejection-test";
        ObjectMetadata rejectionMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        rejectionMetadata.setObjectId(objectId);
        rejectionMetadata.setObjectName("RejectionTestPackage");
        rejectionMetadata.setVersion("1.0.0");
        rejectionMetadata.setStatus(ObjectStatus.DRAFT);
        rejectionMetadata.setUploadUser("developerUser");
        rejectionMetadata.setUploadTime(Instant.now());
        rejectionMetadata.setSourceChannel("DEVELOPMENT");
        rejectionMetadata.getProperties().put("file.extension", ".ecore");

        workflowService.uploadDraft(rejectionPackage, rejectionMetadata).getValue();

        // Verify draft exists
        assertTrue(draftService.exists(objectId), "Draft should exist");
        
        // === TEST: Reject the draft ===
        
        ObjectMetadata rejectedMetadata = workflowService.rejectObject(objectId, "qaUser", "Missing required documentation");
        
        // Verify rejection metadata updates
        assertNotNull(rejectedMetadata, "Rejection should return updated metadata");
        assertEquals(ObjectStatus.REJECTED, rejectedMetadata.getStatus());
        assertEquals("qaUser", rejectedMetadata.getReviewUser());
        assertEquals("Missing required documentation", rejectedMetadata.getReviewReason());
        assertNotNull(rejectedMetadata.getReviewTime());
        assertEquals("qaUser", rejectedMetadata.getLastChangeUser());

        // Verify object remains in draft storage but with REJECTED status
        assertTrue(draftService.exists(objectId), "Rejected object should remain in draft storage");
        assertFalse(releaseService.exists(objectId), "Rejected object should not be in release storage");

        ObjectMetadata draftRejectedMetadata = draftService.retrieveMetadata(objectId).getValue();
        assertEquals(ObjectStatus.REJECTED, draftRejectedMetadata.getStatus());
        assertEquals("qaUser", draftRejectedMetadata.getReviewUser());

        // Verify registry reflects the rejected status
        Optional<ObjectMetadata> rejectedFromRegistry = registryService.getMetadata(objectId);
        assertTrue(rejectedFromRegistry.isPresent(), "Registry should see rejected metadata");
        assertEquals(ObjectStatus.REJECTED, rejectedFromRegistry.get().getStatus());
        assertEquals("draft", rejectedFromRegistry.get().getRole());

        // === Create a second rejected object to test multiple object listing ===
        
        EPackage secondRejectionPackage = EcoreFactory.eINSTANCE.createEPackage();
        secondRejectionPackage.setName("SecondRejectionTestPackage");
        secondRejectionPackage.setNsPrefix("rejection2");
        secondRejectionPackage.setNsURI("http://rejection2.test.example.com");

        String secondObjectId = "rejection-test-2";
        ObjectMetadata secondRejectionMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        secondRejectionMetadata.setObjectId(secondObjectId);
        secondRejectionMetadata.setObjectName("SecondRejectionTestPackage");
        secondRejectionMetadata.setVersion("1.0.0");
        secondRejectionMetadata.setStatus(ObjectStatus.DRAFT);
        secondRejectionMetadata.setUploadUser("developerUser");
        secondRejectionMetadata.setUploadTime(Instant.now());
        secondRejectionMetadata.setSourceChannel("DEVELOPMENT");
        secondRejectionMetadata.getProperties().put("file.extension", ".ecore");

        workflowService.uploadDraft(secondRejectionPackage, secondRejectionMetadata).getValue();
        
        // Reject the second object
        workflowService.rejectObject(secondObjectId, "qaUser", "Second object also has issues");
        
        // Test rejected objects listing - should now have multiple objects
        List<ObjectMetadata> rejectedObjects = workflowService.listRejectedObjects();
        assertNotNull(rejectedObjects, "Should find the rejected objects");
        assertTrue(rejectedObjects.size() >= 2, "Should have at least two rejected objects");
        
        // Verify both objects are in the rejected list
        boolean foundFirstRejected = rejectedObjects.stream().anyMatch(obj -> objectId.equals(obj.getObjectId()));
        boolean foundSecondRejected = rejectedObjects.stream().anyMatch(obj -> secondObjectId.equals(obj.getObjectId()));
        assertTrue(foundFirstRejected, "Should find first rejected object in the list");
        assertTrue(foundSecondRejected, "Should find second rejected object in the list");
        
        // Verify all objects have REJECTED status
        rejectedObjects.forEach(obj -> assertEquals(ObjectStatus.REJECTED, obj.getStatus(), "All rejected objects should have REJECTED status"));

        // === TEST: Cannot approve rejected object without status change ===
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            workflowService.approveObject(objectId, "qaUser", "Attempting to approve rejected object");
        });
        assertTrue(exception.getMessage().contains("not in DRAFT status"), 
                "Should throw exception when trying to approve rejected object");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @FullWorkflowWithDocumentationSetup
    public void testWorkflowErrorHandling(
            @InjectService(cardinality = 0, filter = "(workflow.id=basic-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware
    ) throws Exception {
        
        EObjectWorkflowService<EObject> workflowService = workflowServiceAware.waitForService(5000L);

        // === TEST: Approve non-existent object ===
        
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            workflowService.approveObject("non-existent-object", "qaUser", "Testing error handling");
        });
        assertTrue(exception1.getMessage().contains("not found in draft storage"), 
                "Should throw exception for non-existent object");

        // === TEST: Reject non-existent object ===
        
        IllegalStateException exception2 = assertThrows(IllegalStateException.class, () -> {
            workflowService.rejectObject("non-existent-object", "qaUser", "Testing error handling");
        });
        assertTrue(exception2.getMessage().contains("Error updating the metadata") ||
                  exception2.getMessage().contains("Object not found in draft storage"), 
                "Should throw exception for non-existent object rejection");

        // === TEST: Release non-approved object ===
        
        IllegalStateException exception3 = assertThrows(IllegalStateException.class, () -> {
            workflowService.releaseObject("non-existent-object", "Testing error handling", false);
        });
        assertTrue(exception3.getMessage().contains("Error updating the metadata") || 
                  exception3.getMessage().contains("not in APPROVED status") ||
                  exception3.getMessage().contains("Object not found in approved storage"), 
                "Should throw exception for non-approved object release");

        // === TEST: Null parameter validation ===
        
        assertThrows(NullPointerException.class, () -> {
            workflowService.approveObject(null, "qaUser", "reason");
        }, "Should validate null object ID");

        assertThrows(NullPointerException.class, () -> {
            workflowService.approveObject("objectId", null, "reason");
        }, "Should validate null review user");

        assertThrows(NullPointerException.class, () -> {
            workflowService.rejectObject("objectId", "qaUser", null);
        }, "Should validate null rejection reason");
    }

   

  
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @FullWorkflowWithDocumentationSetup
    public void testConcurrentWorkflowOperations(
            @InjectService(cardinality = 0, filter = "(workflow.id=basic-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=draft))")
            ServiceAware<EObjectStorageService> draftServiceAware
    ) throws Exception {
        
        EObjectWorkflowService<EObject> workflowService = workflowServiceAware.waitForService(5000L);
        EObjectStorageService<EObject> draftService = (EObjectStorageService<EObject>) draftServiceAware.waitForService(5000L);

        // Create and store draft for concurrent testing
        EPackage concurrentPackage = EcoreFactory.eINSTANCE.createEPackage();
        concurrentPackage.setName("ConcurrentTestPackage");
        concurrentPackage.setNsPrefix("concurrent");
        concurrentPackage.setNsURI("http://concurrent.test.example.com");

        String objectId = "concurrent-test";
        ObjectMetadata concurrentMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        concurrentMetadata.setObjectId(objectId);
        concurrentMetadata.setObjectName("ConcurrentTestPackage");
        concurrentMetadata.setVersion("1.0.0");
        concurrentMetadata.setStatus(ObjectStatus.DRAFT);
        concurrentMetadata.setUploadUser("developerUser");
        concurrentMetadata.setUploadTime(Instant.now());
        concurrentMetadata.setSourceChannel("DEVELOPMENT");
        concurrentMetadata.getProperties().put("file.extension", ".ecore");

        workflowService.uploadDraft(concurrentPackage, concurrentMetadata).getValue();

        // === TEST: Concurrent approval attempts (should be serialized by object locking) ===
        
        Thread approvalThread1 = new Thread(() -> {
            try {
                workflowService.approveObject(objectId, "qaUser1", "First approval attempt");
            } catch (Exception e) {
                // Expected - one thread should fail due to locking/status change
            }
        });

        Thread approvalThread2 = new Thread(() -> {
            try {
                // Small delay to ensure first thread starts first
                Thread.sleep(100);
                workflowService.approveObject(objectId, "qaUser2", "Second approval attempt");
            } catch (Exception e) {
                // Expected - one thread should fail due to locking/status change
            }
        });

        approvalThread1.start();
        approvalThread2.start();

        approvalThread1.join(5000);
        approvalThread2.join(5000);

        // Verify that object ended up in approved state (one thread succeeded)
        // The object should still exist in draft storage but be archived (archive_drafts_on_approval=true by default)
        assertTrue(draftService.exists(objectId), 
                "Object should still exist in draft storage (archived due to archive_drafts_on_approval=true)");
        
        // Verify the draft has been archived
        ObjectMetadata archivedDraft = draftService.retrieveMetadata(objectId).getValue();
        assertEquals(ObjectStatus.ARCHIVED, archivedDraft.getStatus(), 
                "Draft should be archived after approval due to archive_drafts_on_approval configuration");
    }
}