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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult;
import org.eclipse.fennec.model.atlas.governance.ComplianceStatus;
import org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation;
import org.eclipse.fennec.model.atlas.governance.PolicyType;
import org.eclipse.fennec.model.atlas.governance.compliance.utils.GovernanceDocumentationFormatter;
import org.eclipse.fennec.model.atlas.governance.tests.annotations.GovernanceTestAnnotations;
import org.eclipse.fennec.model.atlas.governance.tests.annotations.GovernanceTestAnnotations.ComplianceServiceTestSetup;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService;
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
 * Integration tests for the complete compliance and documentation workflow.
 * 
 * <p>Tests the integration between:</p>
 * <ul>
 * <li>GovernanceComplianceService (DemoGovernanceComplianceService)</li>
 * <li>GovernanceDocumentationService</li>
 * <li>EObjectStorageService with role-based storage</li>
 * </ul>
 * 
 * <p>Validates end-to-end scenarios including compliance checking, documentation generation,
 * and storage with proper metadata handling.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class ComplianceDocumentationIntegrationTest {

    private static final String TEST_OBJECT_ID = "integration-test-epackage";
    private static final String TEST_USER = "integration-test-reviewer";
    
    private ManagementFactory managementFactory;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        managementFactory = ManagementFactory.eINSTANCE;
		assertNotNull(tempDir, "TempDir should not be null");
		
		// Set system property for template argument resolution
		System.setProperty(GovernanceTestAnnotations.PROP_TEMP_DIR, tempDir.toString());    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @ComplianceServiceTestSetup
    void testEndToEndComplianceDocumentationWorkflow(
    		@InjectService(filter = "(demo.mode=true)") GovernanceComplianceService complianceService, 
    		@InjectService GovernanceDocumentationService documentationService, 
    		@InjectService(filter = "(storage.role=draft)") EObjectStorageService draftStorage) throws Exception {
        // 1. Create and store a test EPackage in draft storage
        EPackage testPackage = createTestEPackage("AirQualitySensor");
        ObjectMetadata packageMetadata = createTestMetadata(TEST_OBJECT_ID, "AI_GENERATOR");
        
        String storedObjectId = (String) draftStorage.storeObject(TEST_OBJECT_ID, testPackage, packageMetadata).getValue();
        assertEquals(TEST_OBJECT_ID, storedObjectId, "Object should be stored with expected ID");
        
        // 2. Run comprehensive compliance checks
        GovernanceDocumentation documentation = complianceService.runComplianceChecks(
            TEST_OBJECT_ID, TEST_USER, "Integration test compliance verification").getValue();
        
        // 3. Verify comprehensive documentation was generated
        assertNotNull(documentation, "Documentation should be generated");
        assertNotNull(documentation.getModelName(), "Model name should be set");
        assertTrue(documentation.getModelName().contains("AirQualitySensor"), "Model name should contain package name");
        assertEquals("1.0", documentation.getVersion());
        assertNotNull(documentation.getGenerationTimestamp());
        assertNotNull(documentation.getGeneratedBy());
        assertEquals(TEST_USER, documentation.getGeneratedBy());
        
        // 4. Verify compliance checks were added
        assertFalse(documentation.getComplianceChecks().isEmpty(), "Should have compliance checks");
        assertEquals(6, documentation.getComplianceChecks().size(), "Should have comprehensive policy checks");
        
        // Verify all expected policies are covered
        assertTrue(documentation.getComplianceChecks().stream()
            .anyMatch(check -> check.getSummary().contains("GDPR") || 
                             check.getClass().getSimpleName().contains("GDPR")), 
            "Should contain GDPR compliance check");
            
        assertTrue(documentation.getComplianceChecks().stream()
            .anyMatch(check -> check.getSummary().contains("AI") || check.getSummary().contains("risk") ||
                             check.getClass().getSimpleName().contains("EUAI")), 
            "Should contain EU AI Act compliance check");
            
        assertTrue(documentation.getComplianceChecks().stream()
            .anyMatch(check -> check.getSummary().contains("ISO") || check.getSummary().contains("security") ||
                             check.getClass().getSimpleName().contains("ISO")), 
            "Should contain ISO 27001 compliance check");
            
        assertTrue(documentation.getComplianceChecks().stream()
            .anyMatch(check -> check.getSummary().contains("Critical infrastructure") || check.getSummary().contains("KRITIS") ||
                             check.getClass().getSimpleName().contains("KRITIS")), 
            "Should contain KRITIS compliance check");
            
        assertTrue(documentation.getComplianceChecks().stream()
            .anyMatch(check -> check.getSummary().contains("Schema validation") || check.getSummary().contains("attributes") ||
                             check.getClass().getSimpleName().contains("DataQuality")), 
            "Should contain Data Quality compliance check");
            
        assertTrue(documentation.getComplianceChecks().stream()
            .anyMatch(check -> check.getSummary().contains("Open Data") || check.getSummary().contains("License") ||
                             check.getClass().getSimpleName().contains("OpenData")), 
            "Should contain Open Data compliance check");
        
        // 5. Store documentation using documentation service
        String docId = documentationService.storeDocumentation(TEST_OBJECT_ID, documentation, TEST_USER, "End-to-end integration test documentation storage").getValue();
        assertNotNull(docId, "Documentation should be stored successfully");
        
        // 6. Verify documentation can be retrieved
        Optional<GovernanceDocumentation> retrievedDoc = documentationService.getLatestDocumentation(TEST_OBJECT_ID);
        assertTrue(retrievedDoc.isPresent(), "Latest documentation should be retrievable");
        
        GovernanceDocumentation latestDoc = retrievedDoc.get();
        assertEquals(documentation.getModelName(), latestDoc.getModelName());
        assertEquals(documentation.getComplianceChecks().size(), latestDoc.getComplianceChecks().size());
        
        // 7. Verify all compliance checks passed (demo mode)
        for (ComplianceCheckResult check : latestDoc.getComplianceChecks()) {
            assertEquals(ComplianceStatus.PASSED, check.getStatus(), 
                        "All compliance checks should pass in demo mode");
            assertNotNull(check.getCheckTimestamp(), "Check timestamp should be set");
            assertNotNull(check.getSummary(), "Check summary should be set");
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @ComplianceServiceTestSetup
    void testSpecificPolicyComplianceWorkflow(
    		@InjectService(filter = "(demo.mode=true)") GovernanceComplianceService complianceService, 
    		@InjectService GovernanceDocumentationService documentationService, 
    		@InjectService(filter = "(storage.role=draft)") EObjectStorageService draftStorage) throws Exception {
        // Create and store test object
        EPackage testPackage = createTestEPackage("PlayerManagement");
        ObjectMetadata packageMetadata = createTestMetadata(TEST_OBJECT_ID + "-specific", "MANUAL_ENTRY");
        
        draftStorage.storeObject(TEST_OBJECT_ID + "-specific", testPackage, packageMetadata).getValue();
        
        // Run specific policy compliance checks
        List<PolicyType> specificPolicies = Arrays.asList(PolicyType.GDPR, PolicyType.DATA_QUALITY);
        GovernanceDocumentation documentation = complianceService.runSpecificComplianceChecks(
            TEST_OBJECT_ID + "-specific", specificPolicies, TEST_USER).getValue();
        
        // Verify only requested policies were checked
        assertNotNull(documentation, "Documentation should be generated");
        assertEquals(2, documentation.getComplianceChecks().size(), "Should have exactly 2 compliance checks");
        
        // Verify specific policies are present
        boolean hasGdprCheck = documentation.getComplianceChecks().stream()
            .anyMatch(check -> check.getSummary().contains("GDPR") || 
                             check.getClass().getSimpleName().contains("GDPR"));
        boolean hasDataQualityCheck = documentation.getComplianceChecks().stream()
            .anyMatch(check -> check.getSummary().contains("Schema validation") || 
                             check.getSummary().contains("attributes") ||
                             check.getClass().getSimpleName().contains("DataQuality"));
        
        assertTrue(hasGdprCheck, "Should contain GDPR compliance check");
        assertTrue(hasDataQualityCheck, "Should contain data quality compliance check");
        
        // Store and verify documentation
        String docId = documentationService.storeDocumentation(TEST_OBJECT_ID + "-specific", documentation, TEST_USER, "Specific policy compliance test documentation").getValue();
        Optional<GovernanceDocumentation> stored = documentationService.getDocumentation(docId);
        assertTrue(stored.isPresent(), "Specific policy documentation should be stored and retrievable");
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @ComplianceServiceTestSetup
    void testSystemComponentComplianceWorkflow(
    		@InjectService(filter = "(demo.mode=true)") GovernanceComplianceService complianceService, 
    		@InjectService GovernanceDocumentationService documentationService, 
    		@InjectService(filter = "(storage.role=draft)") EObjectStorageService draftStorage) throws Exception {
        // Create test system component
        EPackage systemPackage = createTestEPackage("IoTSensorSystem");
        String systemId = "iot-sensor-system";
        String componentId = "air-quality-component";
        String fullObjectId = systemId + "." + componentId;
        
        ObjectMetadata componentMetadata = createTestMetadata(fullObjectId, "SYSTEM_GENERATOR");
        draftStorage.storeObject(fullObjectId, systemPackage, componentMetadata).getValue();
        
        // Run system component compliance
        List<PolicyType> systemPolicies = Arrays.asList(PolicyType.ISO_27001, PolicyType.KRITIS);
        GovernanceDocumentation documentation = complianceService.runSystemComponentCompliance(
            systemId, componentId, systemPolicies, TEST_USER).getValue();
        
        // Verify system-specific documentation
        assertNotNull(documentation, "System component documentation should be generated");
        assertTrue(documentation.getModelName().contains("System Component"), "Should be marked as system component");
        assertTrue(documentation.getModelName().contains(componentId), "Should contain component ID");
        assertTrue(documentation.getDescription().contains("system"), "Description should mention system context");
        
        assertEquals(2, documentation.getComplianceChecks().size(), "Should have system-specific compliance checks");
        
        // Verify enhanced compliance check summaries for system context
        for (ComplianceCheckResult check : documentation.getComplianceChecks()) {
            assertTrue(check.getSummary().contains("System component") || 
                      check.getSummary().contains("ISO") || 
                      check.getSummary().contains("KRITIS"), 
                      "System component checks should have enhanced summaries");
        }
        
        // Store and verify system documentation
        documentationService.storeDocumentation(fullObjectId, documentation, TEST_USER, "System component compliance documentation").getValue();
        assertTrue(documentationService.hasDocumentation(fullObjectId), "System component should have documentation");
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
    @ComplianceServiceTestSetup
    void testDocumentationFormatterIntegration(
    		@InjectService(filter = "(demo.mode=true)") GovernanceComplianceService complianceService, 
    		@InjectService GovernanceDocumentationService documentationService, 
    		@InjectService(filter = "(storage.role=draft)") EObjectStorageService draftStorage) throws Exception {
        // Create and store test object
        EPackage testPackage = createTestEPackage("FormattedOutputTest");
        ObjectMetadata packageMetadata = createTestMetadata(TEST_OBJECT_ID + "-formatted", "AI_GENERATOR");
        draftStorage.storeObject(TEST_OBJECT_ID + "-formatted", testPackage, packageMetadata).getValue();
        
        // Run compliance checks to get documentation
        GovernanceDocumentation documentation = complianceService.runComplianceChecks(
            TEST_OBJECT_ID + "-formatted", TEST_USER, "Formatted output test").getValue();
        
        // Use the GovernanceDocumentationFormatter directly
        String formattedDoc = GovernanceDocumentationFormatter.formatAsMarkdown(documentation, testPackage);
        
        // Verify formatted output contains expected sections
        assertNotNull(formattedDoc, "Formatted documentation should be generated");
        assertTrue(formattedDoc.contains("# Technische Modelldokumentation"), "Should have German header");
        assertTrue(formattedDoc.contains("1. Allgemeine Informationen"), "Should have general information section");
        assertTrue(formattedDoc.contains("2. Datenschema / Attribute"), "Should have data schema section");
        assertTrue(formattedDoc.contains("3. Geprüfte Richtlinien"), "Should have policy section");
        assertTrue(formattedDoc.contains("4. Technische Integrationsdetails"), "Should have technical details");
        assertTrue(formattedDoc.contains("FormattedOutputTest"), "Should contain object name");
        assertTrue(formattedDoc.contains("PASSED"), "Should show compliance results");
        
        System.out.println("Generated formatted documentation:");
        System.out.println(formattedDoc);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @ComplianceServiceTestSetup
    void testValidatePolicyComplianceIndividually(
    		@InjectService(filter = "(demo.mode=true)") GovernanceComplianceService complianceService, 
    		@InjectService GovernanceDocumentationService documentationService, 
    		@InjectService(filter = "(storage.role=draft)") EObjectStorageService draftStorage) throws Exception {
        // Create and store test object
        EPackage testPackage = createTestEPackage("IndividualPolicyTest");
        ObjectMetadata packageMetadata = createTestMetadata(TEST_OBJECT_ID + "-individual", "BATCH_PROCESSOR");
        draftStorage.storeObject(TEST_OBJECT_ID + "-individual", testPackage, packageMetadata).getValue();
        
        // Test individual policy validation
        ComplianceCheckResult gdprResult = complianceService.validatePolicyCompliance(
            TEST_OBJECT_ID + "-individual", PolicyType.GDPR, TEST_USER).getValue();
        
        assertNotNull(gdprResult, "GDPR compliance result should be generated");
        assertEquals(ComplianceStatus.PASSED, gdprResult.getStatus(), "GDPR check should pass in demo mode");
        assertNotNull(gdprResult.getCheckTimestamp(), "Check timestamp should be set");
        assertNotNull(gdprResult.getSummary(), "Check summary should be set");
        assertTrue(gdprResult.getSummary().contains("GDPR") || 
                  gdprResult.getClass().getSimpleName().contains("GDPR"), 
                  "Result should be GDPR-specific");
        
        // Test another policy
        ComplianceCheckResult euAiResult = complianceService.validatePolicyCompliance(
            TEST_OBJECT_ID + "-individual", PolicyType.EU_AI_ACT, TEST_USER).getValue();
        
        assertNotNull(euAiResult, "EU AI Act compliance result should be generated");
        assertEquals(ComplianceStatus.PASSED, euAiResult.getStatus(), "EU AI Act check should pass in demo mode");
        assertTrue(euAiResult.getSummary().contains("AI") || euAiResult.getSummary().contains("risk"), 
                  "Result should be EU AI Act-specific");
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    @ComplianceServiceTestSetup
    void testGetAvailablePolicies(
    		@InjectService(filter = "(demo.mode=true)") GovernanceComplianceService complianceService, 
    		@InjectService GovernanceDocumentationService documentationService, 
    		@InjectService(filter = "(storage.role=draft)") EObjectStorageService draftStorage) throws Exception {
        List<PolicyType> availablePolicies = complianceService.getAvailablePolicies();
        
        assertNotNull(availablePolicies, "Available policies should not be null");
        assertFalse(availablePolicies.isEmpty(), "Should have available policies");
        
        // Verify comprehensive policy support
        assertTrue(availablePolicies.contains(PolicyType.GDPR), "Should support GDPR");
        assertTrue(availablePolicies.contains(PolicyType.EU_AI_ACT), "Should support EU AI Act");
        assertTrue(availablePolicies.contains(PolicyType.ISO_27001), "Should support ISO 27001");
        assertTrue(availablePolicies.contains(PolicyType.DATA_QUALITY), "Should support Data Quality");
        assertTrue(availablePolicies.contains(PolicyType.KRITIS), "Should support KRITIS");
        assertTrue(availablePolicies.contains(PolicyType.OPEN_DATA), "Should support Open Data");
        
        assertEquals(6, availablePolicies.size(), "Should support exactly 6 policies");
    }
    
    // Helper methods
    
    private EPackage createTestEPackage(String name) {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName(name);
        ePackage.setNsURI("http://test.example.com/" + name.toLowerCase() + "/1.0");
        ePackage.setNsPrefix(name.toLowerCase());
        
        // Add a simple EClass for more realistic structure
        EClass sensorClass = EcoreFactory.eINSTANCE.createEClass();
        sensorClass.setName(name + "Data");
        ePackage.getEClassifiers().add(sensorClass);
        
        // Add some attributes to make it more realistic
        sensorClass.getEStructuralFeatures().add(EcoreFactory.eINSTANCE.createEAttribute());
        sensorClass.getEStructuralFeatures().get(0).setName("timestamp");
        sensorClass.getEStructuralFeatures().get(0).setEType(EcorePackage.Literals.EDATE);
        
        return ePackage;
    }
    
    private ObjectMetadata createTestMetadata(String objectId, String sourceChannel) {
        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setUploadUser("test-user");
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel(sourceChannel);
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.setObjectType("EPackage");
        metadata.setObjectName("Test " + objectId + " for integration testing");
        return metadata;
    }
}