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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.gecko.mac.governance.ApprovalStatus;
import org.gecko.mac.governance.GovernanceDocumentation;
import org.gecko.mac.governance.tests.annotations.GovernanceTestAnnotations;
import org.gecko.mac.governance.tests.annotations.GovernanceTestAnnotations.FullWorkflowWithDocumentationSetup;
import org.gecko.mac.mgmt.governanceapi.EObjectWorkflowService;
import org.gecko.mac.mgmt.governanceapi.GovernanceDocumentationService;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.gecko.mac.mgmt.management.ObjectStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration test for GovernanceInitializerService to verify correct setup
 * and initialization of governance documentation with direct storage services.
 * 
 * Tests the complete flow: initialization → storage verification → documentation linking
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class) 
@ExtendWith(ConfigurationExtension.class)
@ExtendWith(MockitoExtension.class)
public class GovernanceInitializerIntegrationTest {
 
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

    @Disabled("We need some models here to make it work")
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	@WithFactoryConfiguration(factoryPid = GovernanceTestAnnotations.PID_INITIALIZER, name = "initializer", location = "?", properties = {
            @Property(key = "draftStorage.target", value = "(storage.role=draft)"),
            @Property(key = "releaseStorage.target", value = "(storage.role=release)")
        })
    @FullWorkflowWithDocumentationSetup
    public void testAutoInitializationAndVerification(
            @InjectService(cardinality = 0, filter = "(workflow.id=basic-test-workflow)")
            ServiceAware<EObjectWorkflowService> workflowServiceAware,
            @InjectService(cardinality = 0)
            ServiceAware<GovernanceDocumentationService> documentationServiceAware
           
    ) throws Exception {
    	
    	// Wait for all services to be ready
    	EObjectWorkflowService workflowService = workflowServiceAware.waitForService(5000L);
    	assertNotNull(workflowService, "Workflow service should be available");
    	
    	GovernanceDocumentationService documentationService = documentationServiceAware.waitForService(5000L);
    	assertNotNull(documentationService, "Documentation service should be available");
    	
    	
    	
    	// Give the auto-initialization some time to complete
    	Thread.sleep(2000);
    	
    	// Verify draft objects (should be 5: Booking, Membership, Player, Rental, Transaction)
    	List<ObjectMetadata> draftObjects = workflowService.listDraftObjects();
    	assertNotNull(draftObjects, "Draft objects list should not be null");
    	assertEquals(5, draftObjects.size(), "Should have 5 draft objects");
    	
        // Verify approved objects (should be 2: Equipment, LoRaWAN)
        List<ObjectMetadata> approvedObjects = workflowService.listApprovedObjects();
        assertNotNull(approvedObjects, "Approved objects list should not be null");
        assertEquals(2, approvedObjects.size(), "Should have 2 approved objects");
        
        
        // Verify all approved objects have governance documentation
        for (ObjectMetadata metadata : approvedObjects) {
            assertEquals(ObjectStatus.APPROVED, metadata.getStatus(), "Approved object should have APPROVED status");
            assertNotNull(metadata.getGovernanceDocumentationId(), "Approved object should have governance documentation ID");
            
            // Verify governance documentation exists and is accessible
            Optional<GovernanceDocumentation> docOpt = documentationService.getDocumentation(metadata.getGovernanceDocumentationId());
            assertTrue(docOpt.isPresent(), "Governance documentation should exist for ID: " + metadata.getGovernanceDocumentationId());
            
            GovernanceDocumentation doc = docOpt.get();
            assertEquals(ApprovalStatus.APPROVED, doc.getStatus(), "Governance documentation should be APPROVED");
            assertNotNull(doc.getModelName(), "Documentation should have model name");
        }
        
        // Verify all draft objects have governance documentation
        for (ObjectMetadata metadata : draftObjects) {
            assertEquals(ObjectStatus.DRAFT, metadata.getStatus(), "Draft object should have DRAFT status");
            assertNotNull(metadata.getGovernanceDocumentationId(), "Draft object should have governance documentation ID");
            
            // Verify governance documentation exists and is accessible
            Optional<GovernanceDocumentation> docOpt = documentationService.getDocumentation(metadata.getGovernanceDocumentationId());
            assertTrue(docOpt.isPresent(), "Governance documentation should exist for ID: " + metadata.getGovernanceDocumentationId());
            
            GovernanceDocumentation doc = docOpt.get();
            assertEquals(ApprovalStatus.REJECTED, doc.getStatus(), "Governance documentation should be REJECTED");
            assertNotNull(doc.getModelName(), "Documentation should have model name");
        }
        
        // Verify specific packages by name
        assertTrue(approvedObjects.stream().anyMatch(obj -> obj.getObjectName().contains("EPackage: equipment")), 
                  "Should contain Equipment package");
        assertTrue(approvedObjects.stream().anyMatch(obj -> obj.getObjectName().contains("LoRaWAN") || obj.getObjectName().contains("EPackage: lorawan")), 
                  "Should contain LoRaWAN package");
        
        assertTrue(draftObjects.stream().anyMatch(obj -> obj.getObjectName().contains("EPackage: rental")), 
        		"Should contain Rental package");
        assertTrue(draftObjects.stream().anyMatch(obj -> obj.getObjectName().contains("EPackage: booking")), 
                  "Should contain Booking package");
        assertTrue(draftObjects.stream().anyMatch(obj -> obj.getObjectName().contains("EPackage: player")), 
        		"Should contain Player package");
        assertTrue(draftObjects.stream().anyMatch(obj -> obj.getObjectName().contains("EPackage: transaction")), 
        		"Should contain Transaction package");
        assertTrue(draftObjects.stream().anyMatch(obj -> obj.getObjectName().contains("EPackage: membership")), 
                  "Should contain Membership package");
    }

}