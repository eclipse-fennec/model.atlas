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
package org.eclipse.fennec.model.atlas.governance.compliance;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.governance.ApprovalStatus;
import org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult;
import org.eclipse.fennec.model.atlas.governance.ComplianceStatus;
import org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation;
import org.eclipse.fennec.model.atlas.governance.GovernanceFactory;
import org.eclipse.fennec.model.atlas.governance.PolicyType;
import org.eclipse.fennec.model.atlas.governance.compliance.utils.GovernanceDocumentationFormatter;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.policy.dataqs.DataQSFactory;
import org.eclipse.fennec.model.atlas.policy.euaiact.EUAIFactory;
import org.eclipse.fennec.model.atlas.policy.gdpr.GDPRFactory;
import org.eclipse.fennec.model.atlas.policy.iso27001.ISO27Factory;
import org.eclipse.fennec.model.atlas.policy.kritis.KritisFactory;
import org.eclipse.fennec.model.atlas.policy.opendata.OpenDataFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.eclipse.fennec.model.atlas.governance.AttributeDefinition;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Demonstration-focused implementation of GovernanceComplianceService that generates
 * realistic governance documentation based on actual EObjects from draft storage.
 * 
 * <p>This service creates detailed, mockup-style documentation similar to the German 
 * "MOCKUP - Technische Modelldokumentation AirQualitySensor_v1.md" but with real data:</p>
 * <ul>
 * <li>Real timestamps and workflow instance IDs</li>
 * <li>Actual EObject structure and attribute information</li>
 * <li>Realistic policy compliance results based on object analysis</li>
 * <li>Proper data classification and ownership information</li>
 * </ul>
 * 
 * <p>Designed for demonstration at conferences and customer presentations to show
 * the full governance workflow with realistic, generated documentation.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@Component(
	scope = ServiceScope.PROTOTYPE,
    property = {
        "service.description=Demo Governance Compliance Service with realistic documentation generation",
        "service.vendor=Data In Motion",
        "service.ranking:Integer=200",
        "demo.mode=true"
    }
)
public class DemoGovernanceComplianceService implements GovernanceComplianceService {

    private static final Logger logger = Logger.getLogger(DemoGovernanceComplianceService.class.getName());

    private final PromiseFactory promiseFactory = new PromiseFactory(null);
    private final GovernanceFactory governanceFactory = GovernanceFactory.eINSTANCE;

    @Reference(target = "(storage.role=draft)")
    private EObjectStorageService<EObject> draftStorage;
    
    @Reference
    private GovernanceDocumentationService documentationService;

    // Available policy types for comprehensive compliance checking
    private static final List<PolicyType> COMPREHENSIVE_POLICIES = Arrays.asList(
        PolicyType.EU_AI_ACT,
        PolicyType.GDPR,
        PolicyType.ISO_27001,
        PolicyType.DATA_QUALITY,
        PolicyType.KRITIS,
        PolicyType.OPEN_DATA
    );
    
    @Activate
    void activate() {
        logger.info("Activated DemoGovernanceComplianceService");
    }

    @Override
    public Promise<GovernanceDocumentation> runComplianceChecks(String objectId, String reviewUser, String complianceReason) {
        return promiseFactory.submit(() -> {
            logger.info("Running comprehensive demo compliance checks for object: " + objectId);
            
            try {
                // Retrieve actual object and metadata from draft storage
                EObject eObject = draftStorage.retrieveObject(objectId).getValue();
                ObjectMetadata metadata = draftStorage.retrieveMetadata(objectId).getValue();
                
                // Generate realistic governance documentation
                GovernanceDocumentation documentation = createRealisticGovernanceDocumentation(objectId, eObject, metadata, reviewUser, complianceReason);
                
                // Add comprehensive policy compliance results
                for (PolicyType policyType : COMPREHENSIVE_POLICIES) {
                    ComplianceCheckResult result = createRealisticComplianceResult(objectId, eObject, metadata, policyType, reviewUser);
                    documentation.getComplianceChecks().add(result);
                }
                
                // Store the generated documentation persistently
                String docId = documentationService.storeDocumentation(objectId, documentation, reviewUser, 
                    complianceReason != null ? complianceReason : "Comprehensive compliance check").getValue();
                
                logger.info("Generated and stored comprehensive governance documentation for: " + objectId + " (doc ID: " + docId + ")");
                return documentation;
                
            } catch (Exception e) {
                logger.warning("Failed to retrieve object from draft storage: " + objectId + ". Generating fallback documentation.");
                GovernanceDocumentation fallbackDoc = createFallbackDocumentation(objectId, reviewUser, complianceReason);
                
                // Store the fallback documentation persistently
                try {
                    String docId = documentationService.storeDocumentation(objectId, fallbackDoc, reviewUser, 
                        "Fallback compliance check - " + (complianceReason != null ? complianceReason : "object not accessible")).getValue();
                    logger.info("Stored fallback governance documentation for: " + objectId + " (doc ID: " + docId + ")");
                } catch (Exception storeException) {
                    logger.warning("Failed to store fallback governance documentation for: " + objectId + " - " + storeException.getMessage());
                }
                
                return fallbackDoc;
            }
        });
    }

    @Override
    public Promise<GovernanceDocumentation> runSpecificComplianceChecks(String objectId, List<PolicyType> policyTypes, String reviewUser) {
        return promiseFactory.submit(() -> {
            logger.info("Running specific demo compliance checks for object: " + objectId + " with policies: " + policyTypes);
            
            try {
                EObject eObject = draftStorage.retrieveObject(objectId).getValue();
                ObjectMetadata metadata = draftStorage.retrieveMetadata(objectId).getValue();
                
                GovernanceDocumentation documentation = createRealisticGovernanceDocumentation(objectId, eObject, metadata, reviewUser, "Specific policy compliance check");
                
                // Add compliance results for requested policies only
                for (PolicyType policyType : policyTypes) {
                    if (COMPREHENSIVE_POLICIES.contains(policyType)) {
                        ComplianceCheckResult result = createRealisticComplianceResult(objectId, eObject, metadata, policyType, reviewUser);
                        documentation.getComplianceChecks().add(result);
                    }
                }
                
                return documentation;
                
            } catch (Exception e) {
                logger.warning("Failed to retrieve object from draft storage for specific checks: " + objectId);
                return createFallbackDocumentation(objectId, reviewUser, "Specific policy compliance check");
            }
        });
    }

    @Override
    public Promise<GovernanceDocumentation> runSystemComponentCompliance(String systemId, String componentId, List<PolicyType> policyTypes, String reviewUser) {
        return promiseFactory.submit(() -> {
            String objectId = systemId + "." + componentId;
            logger.info("Running system component demo compliance for: " + objectId);
            
            try {
                EObject eObject = draftStorage.retrieveObject(objectId).getValue();
                ObjectMetadata metadata = draftStorage.retrieveMetadata(objectId).getValue();
                
                GovernanceDocumentation documentation = createRealisticGovernanceDocumentation(objectId, eObject, metadata, reviewUser, "System component compliance check");
                
                // Enhance with system-specific information
                documentation.setModelName("System Component: " + componentId + " (System: " + systemId + ")");
                documentation.setDescription("Governance compliance documentation for system component " + componentId + " within system " + systemId + ". " + documentation.getDescription());
                
                for (PolicyType policyType : policyTypes) {
                    if (COMPREHENSIVE_POLICIES.contains(policyType)) {
                        ComplianceCheckResult result = createRealisticComplianceResult(objectId, eObject, metadata, policyType, reviewUser);
                        result.setSummary("System component compliance: " + result.getSummary());
                        documentation.getComplianceChecks().add(result);
                    }
                }
                
                return documentation;
                
            } catch (Exception e) {
                logger.warning("Failed to retrieve system component from draft storage: " + objectId);
                return createFallbackDocumentation(objectId, reviewUser, "System component compliance check");
            }
        });
    }

    @Override
    public Promise<GovernanceDocumentation> getComplianceStatus(String objectId) {
        return promiseFactory.submit(() -> {
            logger.info("Retrieving demo compliance status for object: " + objectId);
            
            // First, check if governance documentation already exists in storage
            try {
                Optional<GovernanceDocumentation> existingDoc = documentationService.getLatestDocumentation(objectId);
                if (existingDoc.isPresent()) {
                    logger.info("Found existing governance documentation for object: " + objectId + " with status: " + existingDoc.get().getStatus());
                    return existingDoc.get();
                }
            } catch (Exception e) {
                logger.info("No existing governance documentation found for object: " + objectId + ", will generate new documentation");
            }
            
            // Do not auto-generate documentation - return null when none exists
            // This enables proper governance gate behavior
            logger.info("No existing governance documentation found for object: " + objectId);
            return null;
        });
    }

    @Override
    public Promise<ComplianceCheckResult> validatePolicyCompliance(String objectId, PolicyType policyType, String reviewUser) {
        return promiseFactory.submit(() -> {
            logger.info("Validating demo policy compliance for object: " + objectId + ", policy: " + policyType);
            
            try {
                EObject eObject = draftStorage.retrieveObject(objectId).getValue();
                ObjectMetadata metadata = draftStorage.retrieveMetadata(objectId).getValue();
                
                return createRealisticComplianceResult(objectId, eObject, metadata, policyType, reviewUser);
                
            } catch (Exception e) {
                logger.warning("Failed to validate policy compliance for: " + objectId);
                return createFallbackComplianceResult(objectId, policyType, reviewUser);
            }
        });
    }

    @Override
    public List<PolicyType> getAvailablePolicies() {
        return COMPREHENSIVE_POLICIES;
    }

    // Helper methods for realistic documentation generation

    private GovernanceDocumentation createRealisticGovernanceDocumentation(String objectId, EObject eObject, ObjectMetadata metadata, String reviewUser, String reason) {
        GovernanceDocumentation documentation = governanceFactory.createGovernanceDocumentation();
        
        // Use real object information
        String displayName = getObjectDisplayName(eObject, objectId);
        documentation.setModelName(displayName);
        documentation.setDescription(createRealisticDescription(eObject, metadata, reason));
        documentation.setVersion("1.0");
        
        
        // Real timestamps
        Instant now = Instant.now();
        documentation.setGenerationTimestamp(Date.from(now));
        documentation.setGeneratedBy(reviewUser);
        // documentation.setApprovedBy(reviewUser); // Only set when actually approved
        documentation.setStatus(ApprovalStatus.DRAFT);
        
        // Real data classification and ownership
        documentation.setDataOwner(determineDataOwner(eObject, metadata));
        documentation.setDataClassification(determineDataClassification(eObject, metadata));
        
        // Add detailed attribute information from the actual EObject
        addAttributeDefinitions(documentation, eObject);
        
        return documentation;
    }

    private ComplianceCheckResult createRealisticComplianceResult(String objectId, EObject eObject, ObjectMetadata metadata, PolicyType policyType, String reviewUser) {
        ComplianceCheckResult result = createPolicySpecificResult(policyType);
        
        result.setStatus(ComplianceStatus.PASSED); // Demo mode - everything passes
        result.setCheckTimestamp(Date.from(Instant.now()));
        result.setSummary(createRealisticPolicySummary(eObject, metadata, policyType));
        
        return result;
    }

    private ComplianceCheckResult createPolicySpecificResult(PolicyType policyType) {
        switch (policyType) {
            case EU_AI_ACT:
                return EUAIFactory.eINSTANCE.createEUAIActPolicyCheck();
            case GDPR:
                return GDPRFactory.eINSTANCE.createGDPRPolicyCheck();
            case ISO_27001:
                return ISO27Factory.eINSTANCE.createISO27001PolicyCheck();
            case KRITIS:
                return KritisFactory.eINSTANCE.createKRITISPolicyCheck();
            case DATA_QUALITY:
                return DataQSFactory.eINSTANCE.createDataQualityPolicyCheck();
            case OPEN_DATA:
                return OpenDataFactory.eINSTANCE.createOpenDataPolicyCheck();
            default:
                return DataQSFactory.eINSTANCE.createDataQualityPolicyCheck();
        }
    }

    private String createRealisticPolicySummary(EObject eObject, ObjectMetadata metadata, PolicyType policyType) {
        String objectType = eObject.eClass().getName();
        
        switch (policyType) {
            case EU_AI_ACT:
                return "**Low Risk:** The AI-generated " + objectType + " does not fall under high-risk applications according to Annex III and poses no danger to health, safety, or fundamental rights.";
            case GDPR:
                return "No personal data according to Art. 4 GDPR is processed. The " + objectType + " handles exclusively technical metadata.";
            case ISO_27001:
                return "**Compliance by inheritance:** The involved assets fulfill the relevant security controls (A.13.1.1 - Network security through TLS & authentication).";
            case DATA_QUALITY:
                return "All attributes of the " + objectType + " conform to the predefined data types and value ranges. Schema validation successful.";
            case KRITIS:
                return "Critical infrastructure requirements fulfilled. Resilience and incident response readiness for " + objectType + " verified.";
            case OPEN_DATA:
                return "Open Data compliance verified. License requirements fulfilled, machine-readable, public endpoint available.";
            default:
                return "General compliance check for " + objectType + " successfully completed.";
        }
    }

    private String getObjectDisplayName(EObject eObject, String fallbackId) {
        if (eObject instanceof EPackage) {
            EPackage ePackage = (EPackage) eObject;
            String name = ePackage.getName();
            return name != null ? name + "_v1" : fallbackId + "_v1";
        }
        return eObject.eClass().getName() + "_" + fallbackId.substring(Math.max(0, fallbackId.length() - 6)) + "_v1";
    }

    private String createRealisticDescription(EObject eObject, ObjectMetadata metadata, String reason) {
        String objectType = eObject.eClass().getName();
        String sourceChannel = metadata.getSourceChannel() != null ? metadata.getSourceChannel() : "Unknown";
        
        if (objectType.toLowerCase().contains("air") || objectType.toLowerCase().contains("sensor")) {
            return "Technical data model for capturing air quality data as part of the Smart City initiative. " +
                   "Generated by " + sourceChannel + " for monitoring environmental parameters. " + reason + ".";
        } else if (objectType.toLowerCase().contains("player") || objectType.toLowerCase().contains("golf")) {
            return "Data model for golf player management as part of the digital golf course initiative. " +
                   "Generated by " + sourceChannel + " for managing player data and reservations. " + reason + ".";
        } else {
            return "Technical data model for " + objectType + " generated by " + sourceChannel + ". " +
                   "Serves structured data collection and processing. " + reason + ".";
        }
    }

    private String determineDataOwner(EObject eObject, ObjectMetadata metadata) {
        String objectType = eObject.eClass().getName().toLowerCase();
        
        if (objectType.contains("air") || objectType.contains("environment") || objectType.contains("sensor")) {
            return "Facility Management / Environmental Department";
        } else if (objectType.contains("player") || objectType.contains("golf") || objectType.contains("booking")) {
            return "Golf Club Management / Club Administration";
        } else if (objectType.contains("transaction") || objectType.contains("payment")) {
            return "Financial Operations / Accounting";
        } else {
            return "Data Steward / IT Department";
        }
    }

    private String determineDataClassification(EObject eObject, ObjectMetadata metadata) {
        String objectType = eObject.eClass().getName().toLowerCase();
        
        if (objectType.contains("payment") || objectType.contains("transaction") || objectType.contains("member")) {
            return "Internal / Confidential";
        } else if (objectType.contains("air") || objectType.contains("environment") || objectType.contains("weather")) {
            return "Public / Non-sensitive";
        } else {
            return "Internal / Restricted";
        }
    }

    // Fallback methods for when draft storage is unavailable

    private GovernanceDocumentation createFallbackDocumentation(String objectId, String reviewUser, String reason) {
        GovernanceDocumentation documentation = governanceFactory.createGovernanceDocumentation();
        
        documentation.setModelName("Demo_" + objectId + "_v1");
        documentation.setDescription("Fallback governance documentation for demonstration purposes. " + reason + ".");
        documentation.setVersion("1.0");
        documentation.setGenerationTimestamp(Date.from(Instant.now()));
        documentation.setGeneratedBy(reviewUser);
        // documentation.setApprovedBy(reviewUser); // Only set when actually approved
        documentation.setStatus(ApprovalStatus.DRAFT);
        documentation.setDataOwner("Demo Data Steward");
        documentation.setDataClassification("Demo / Test Data");
        
        return documentation;
    }

    private ComplianceCheckResult createFallbackComplianceResult(String objectId, PolicyType policyType, String reviewUser) {
        ComplianceCheckResult result = createPolicySpecificResult(policyType);
        
        result.setStatus(ComplianceStatus.PASSED);
        result.setCheckTimestamp(Date.from(Instant.now()));
        result.setSummary("Demo compliance check for " + policyType + " - All requirements met (fallback mode)");
        
        return result;
    }

    /**
     * Extracts attribute definitions from the EObject and adds them to the documentation.
     * This provides realistic schema information similar to the German mockup.
     */
    private void addAttributeDefinitions(GovernanceDocumentation documentation, EObject eObject) {
        if (eObject instanceof EPackage) {
            EPackage ePackage = (EPackage) eObject;
            // Get first EClass as example
            if (!ePackage.getEClassifiers().isEmpty() && ePackage.getEClassifiers().get(0) instanceof EClass) {
                EClass eClass = (EClass) ePackage.getEClassifiers().get(0);
                addAttributesFromEClass(documentation, eClass);
            }
        } else {
            // For other EObjects, use their class attributes
            addAttributesFromEClass(documentation, eObject.eClass());
        }
    }

    private void addAttributesFromEClass(GovernanceDocumentation documentation, EClass eClass) {
        for (EAttribute attr : eClass.getEAllAttributes()) {
            AttributeDefinition attrDef = governanceFactory.createAttributeDefinition();
            attrDef.setName(attr.getName());
            attrDef.setDataType(getSimpleDataType(attr.getEType()));
            attrDef.setDescription(getAttributeDescription(attr.getName(), attrDef.getDataType()));
            
            documentation.getAttributes().add(attrDef);
        }
        
        // Add some common IoT attributes if none exist
        if (documentation.getAttributes().isEmpty()) {
            addDefaultIoTAttributes(documentation);
        }
    }

    private void addDefaultIoTAttributes(GovernanceDocumentation documentation) {
        // Add timestamp
        AttributeDefinition timestamp = governanceFactory.createAttributeDefinition();
        timestamp.setName("timestamp");
        timestamp.setDataType("DateTime");
        timestamp.setDescription("Measurement timestamp (UTC)");
        documentation.getAttributes().add(timestamp);
        
        // Add temperature (common for sensors)
        AttributeDefinition temperature = governanceFactory.createAttributeDefinition();
        temperature.setName("temperature");
        temperature.setDataType("Double");
        temperature.setDescription("Measured ambient temperature in Celsius");
        documentation.getAttributes().add(temperature);
        
        // Add status
        AttributeDefinition status = governanceFactory.createAttributeDefinition();
        status.setName("status");
        status.setDataType("String");
        status.setDescription("Sensor status information");
        documentation.getAttributes().add(status);
    }

    private String getSimpleDataType(EClassifier classifier) {
        String typeName = classifier.getName();
        if (typeName.contains("String")) return "String";
        if (typeName.contains("Int")) return "Integer";
        if (typeName.contains("Double") || typeName.contains("Float")) return "Double";
        if (typeName.contains("Date") || typeName.contains("Time")) return "DateTime";
        if (typeName.contains("Boolean")) return "Boolean";
        return typeName;
    }

    private String getAttributeDescription(String attributeName, String dataType) {
        String name = attributeName.toLowerCase();
        
        if (name.contains("timestamp") || name.contains("time")) return "Measurement timestamp (UTC)";
        if (name.contains("temp")) return "Measured ambient temperature in Celsius";
        if (name.contains("pressure")) return "Measured atmospheric pressure in hectopascal";
        if (name.contains("co2")) return "Measured CO2 concentration in parts per million";
        if (name.contains("humidity")) return "Relative humidity in percent";
        if (name.contains("id")) return "Unique identifier";
        if (name.contains("name")) return "Element designation";
        if (name.contains("status")) return "Status information";
        if (name.contains("value")) return "Measured value";
        if (name.contains("location") || name.contains("position")) return "Geographic position";
        if (name.contains("battery")) return "Battery charge in percent";
        if (name.contains("signal")) return "Connection signal strength";
        
        return "Attribute of type " + dataType;
    }

    /**
     * Generates human-readable formatted documentation similar to the German mockup.
     * This can be used for display purposes or export to markdown/text files.
     */
    public String generateFormattedDocumentation(String objectId, String reviewUser, String reason) {
        try {
            EObject eObject = draftStorage.retrieveObject(objectId).getValue();
            ObjectMetadata metadata = draftStorage.retrieveMetadata(objectId).getValue();
            GovernanceDocumentation documentation = createRealisticGovernanceDocumentation(objectId, eObject, metadata, reviewUser, reason);
            
            // Add comprehensive policy compliance results
            for (PolicyType policyType : COMPREHENSIVE_POLICIES) {
                ComplianceCheckResult result = createRealisticComplianceResult(objectId, eObject, metadata, policyType, reviewUser);
                documentation.getComplianceChecks().add(result);
            }
            
            return GovernanceDocumentationFormatter.formatAsMarkdown(documentation, eObject);
            
        } catch (Exception e) {
            logger.warning("Failed to generate formatted documentation for: " + objectId + ". Error: " + e.getMessage());
            GovernanceDocumentation fallback = createFallbackDocumentation(objectId, reviewUser, reason);
            return GovernanceDocumentationFormatter.formatAsMarkdown(fallback, null);
        }
    }
}