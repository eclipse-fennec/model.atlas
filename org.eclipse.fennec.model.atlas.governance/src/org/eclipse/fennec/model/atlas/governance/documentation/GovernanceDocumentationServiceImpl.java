/*
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
package org.eclipse.fennec.model.atlas.governance.documentation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Implementation of GovernanceDocumentationService providing dedicated storage
 * for governance documentation independent of object lifecycle.
 * 
 * Features:
 * - Versioned documentation storage with history tracking
 * - Fast lookup by object ID and documentation ID
 * - Automatic metadata generation for documentation objects
 * - Comprehensive statistics and reporting
 * 
 * Documentation Storage Strategy:
 * 1. Documentation is stored independently from the managed objects
 * 2. Each object can have multiple documentation versions (compliance history)
 * 3. Documentation IDs follow pattern: {objectId}-governance-{timestamp}
 * 4. Latest documentation is also stored with simplified ID: {objectId}-governance-latest
 */
@Component(
	scope = ServiceScope.PROTOTYPE,
    property = {
        "service.description=Governance Documentation Service Implementation",
        "service.vendor=Data In Motion"
    }
)
public class GovernanceDocumentationServiceImpl implements GovernanceDocumentationService {

    private static final Logger logger = Logger.getLogger(GovernanceDocumentationServiceImpl.class.getName());
    
    private final PromiseFactory promiseFactory = new PromiseFactory(null);
    private final ManagementFactory managementFactory = ManagementFactory.eINSTANCE;
    private final Map<String, Long> objectDocumentationCache = new ConcurrentHashMap<>();
    
    @Reference(target = "(storage.role=documentation)")
    private EObjectStorageService<EObject> documentationStorage;
    
    @Activate
    void activate() {
        logger.info("Activated GovernanceDocumentationServiceImpl");
    }
    
    @Override
    public Promise<String> storeDocumentation(String objectId, GovernanceDocumentation documentation, String reviewUser, String reason) {
        return promiseFactory.submit(() -> {
            Objects.requireNonNull(objectId, "Object ID cannot be null");
            Objects.requireNonNull(documentation, "Documentation cannot be null");
            Objects.requireNonNull(reviewUser, "Review user cannot be null");
            // reason is optional, can be null
            
            // ===== CONTENT-BASED VERSIONING: Check if content actually changed =====
            
            Optional<GovernanceDocumentation> existingDoc = getLatestDocumentation(objectId);
            if (existingDoc.isPresent()) {
                GovernanceDocumentation existing = existingDoc.get();
                
                // Compare content using EMF comparison (ignoring audit trail fields)
                if (isContentIdentical(existing, documentation)) {
                    // Content unchanged - return existing latest ID, no new version needed
                    String latestDocId = objectId + "-governance-latest";
                    logger.info("Documentation content unchanged for " + objectId + " - returning existing ID: " + latestDocId);
                    return latestDocId;
                } else {
                    // Content changed - create new version with updated audit trail
                    logger.info("Documentation content changed for " + objectId + " - creating new version");
                    
                    // Update version number
                    String newVersion = incrementVersion(existing.getVersion());
                    documentation.setVersion(newVersion);
                    
                    // Update audit trail properties for change tracking
                    updateAuditTrail(documentation, reviewUser, reason);
                    
                    // Store new version
                    return storeNewVersionWithAuditTrail(objectId, documentation, reviewUser, reason);
                }
            } else {
                // No existing documentation - create initial version
                logger.info("No existing governance documentation found - creating initial version for object " + objectId);
                
                // Set initial version and audit trail
                documentation.setVersion("1.0");
                if (documentation.getGeneratedBy() == null) {
                    documentation.setGeneratedBy(reviewUser);
                }
                if (documentation.getGenerationTimestamp() == null) {
                    documentation.setGenerationTimestamp(java.util.Date.from(Instant.now()));
                }
                
                // Update audit trail for initial creation
                updateAuditTrail(documentation, reviewUser, reason);
                
                return storeNewVersionWithAuditTrail(objectId, documentation, reviewUser, reason);
            }
        });
    }
    
    @Override
    public Optional<GovernanceDocumentation> getLatestDocumentation(String objectId) {
        Objects.requireNonNull(objectId, "Object ID cannot be null");
        
        String latestDocId = objectId + "-governance-latest";
        
        try {
            GovernanceDocumentation doc = (GovernanceDocumentation) documentationStorage.retrieveObject(latestDocId).getValue();
            return Optional.of(doc);
        } catch (Exception e) {
            logger.info("No governance documentation found for object: " + objectId);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<GovernanceDocumentation> getDocumentation(String documentationId) {
        Objects.requireNonNull(documentationId, "Documentation ID cannot be null");
        
        try {
            GovernanceDocumentation doc = (GovernanceDocumentation) documentationStorage.retrieveObject(documentationId).getValue();
            return Optional.of(doc);
        } catch (Exception e) {
            logger.info("Governance documentation not found: " + documentationId);
            return Optional.empty();
        }
    }
    
    @Override
    public List<ObjectMetadata> getDocumentationHistory(String objectId) {
        Objects.requireNonNull(objectId, "Object ID cannot be null");
        
        try {
            // Query for all documentation related to this object
            // The query looks for objects with names starting with objectId + "-governance-"
            // but excluding the "-latest" version
            List<String> docIds = documentationStorage.listObjectIds().getValue().stream()
                .filter(id -> id.startsWith(objectId + "-governance-") && !id.endsWith("-latest"))
                .sorted() // Sort by timestamp in ID
                .toList();
            
            return docIds.stream()
                .map(id -> {
                    try {
                        return documentationStorage.retrieveMetadata(id).getValue();
                    } catch (Exception e) {
                        logger.warning("Failed to retrieve metadata for documentation: " + id);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
                
        } catch (Exception e) {
            logger.warning("Failed to retrieve documentation history for object: " + objectId);
            return List.of();
        }
    }
    
    @Override
    public Boolean hasDocumentation(String objectId) {
        Objects.requireNonNull(objectId, "Object ID cannot be null");
        
        // First check cache for fast response
        if (objectDocumentationCache.containsKey(objectId)) {
            return true;
        }
        
        // Check storage
        String latestDocId = objectId + "-governance-latest";
        boolean exists = documentationStorage.exists(latestDocId);
        
        // Update cache if documentation exists
        if (exists) {
            objectDocumentationCache.put(objectId, System.currentTimeMillis());
        }
        
        return exists;
    }
    
    @Override
    public Promise<Boolean> deleteAllDocumentation(String objectId) {
        return promiseFactory.submit(() -> {
            Objects.requireNonNull(objectId, "Object ID cannot be null");
            
            boolean deleted = false;
            
            try {
                // Delete latest documentation
                String latestDocId = objectId + "-governance-latest";
                try {
                    deleted = documentationStorage.deleteObject(latestDocId).getValue() || deleted;
                } catch (Exception e) {
                    // Ignore if not found
                }
                
                // Delete all versioned documentation
                List<String> docIds = documentationStorage.listObjectIds().getValue().stream()
                    .filter(id -> id.startsWith(objectId + "-governance-") && !id.endsWith("-latest"))
                    .toList();
                
                for (String docId : docIds) {
                    try {
                        deleted = documentationStorage.deleteObject(docId).getValue() || deleted;
                    } catch (Exception e) {
                        logger.warning("Failed to delete governance documentation: " + docId);
                    }
                }
                
                // Remove from cache
                if (deleted) {
                    objectDocumentationCache.remove(objectId);
                    logger.info("Deleted all governance documentation for object: " + objectId);
                }
                
                return deleted;
                
            } catch (Exception e) {
                logger.severe("Failed to delete documentation for object " + objectId + ": " + e.getMessage());
                throw new RuntimeException("Failed to delete governance documentation", e);
            }
        });
    }
    
    @Override
    public Map<String, Object> getDocumentationStatistics() {
        try {
            Map<String, Object> stats = new ConcurrentHashMap<>();
            
            List<String> allDocIds = documentationStorage.listObjectIds().getValue();
            
            // Count total documentation objects
            long totalDocs = allDocIds.size();
            
            // Count latest documentation (objects with active governance)
            long activeDocs = allDocIds.stream()
                .filter(id -> id.endsWith("-governance-latest"))
                .count();
            
            // Count versioned documentation (historical compliance checks)
            long historicalDocs = allDocIds.stream()
                .filter(id -> id.contains("-governance-") && !id.endsWith("-latest"))
                .count();
            
            // Calculate compliance metrics
            long totalComplianceChecks = 0;
            long passedComplianceChecks = 0;
            
            for (String docId : allDocIds) {
                if (docId.endsWith("-governance-latest")) {
                    try {
                        ObjectMetadata metadata = documentationStorage.retrieveMetadata(docId).getValue();
                        
                        // Extract compliance statistics from metadata properties
                        Optional<Entry<String, Object>> totalChecksProperty = metadata.getProperties().stream()
                            .filter(prop -> "totalComplianceChecks".equals(prop.getKey()))
                            .findFirst();
                        
                        Optional<Entry<String, Object>> passedChecksProperty = metadata.getProperties().stream()
                            .filter(prop -> "passedComplianceChecks".equals(prop.getKey()))
                            .findFirst();
                        
                        if (totalChecksProperty.isPresent() && totalChecksProperty.get().getValue() instanceof Number) {
                            totalComplianceChecks += ((Number) totalChecksProperty.get().getValue()).longValue();
                        }
                        
                        if (passedChecksProperty.isPresent() && passedChecksProperty.get().getValue() instanceof Number) {
                            passedComplianceChecks += ((Number) passedChecksProperty.get().getValue()).longValue();
                        }
                        
                    } catch (Exception e) {
                        // Skip if metadata cannot be retrieved
                    }
                }
            }
            
            // Build statistics map
            stats.put("totalDocumentation", totalDocs);
            stats.put("activeObjects", activeDocs);
            stats.put("historicalChecks", historicalDocs);
            stats.put("averageChecksPerObject", activeDocs > 0 ? (double) historicalDocs / activeDocs : 0.0);
            stats.put("totalComplianceChecks", totalComplianceChecks);
            stats.put("passedComplianceChecks", passedComplianceChecks);
            stats.put("overallComplianceRate", totalComplianceChecks > 0 ? (double) passedComplianceChecks / totalComplianceChecks : 0.0);
            stats.put("cacheSize", objectDocumentationCache.size());
            stats.put("lastUpdated", Instant.now());
            
            return stats;
            
        } catch (Exception e) {
            logger.warning("Failed to generate documentation statistics: " + e.getMessage());
            Map<String, Object> errorStats = new ConcurrentHashMap<>();
            errorStats.put("error", "Failed to generate statistics");
            errorStats.put("message", e.getMessage());
            return errorStats;
        }
    }
    
    // Helper methods
    
    /**
     * Compares documentation content excluding audit trail fields.
     * 
     * @param existing the existing documentation
     * @param newDoc the new documentation to compare
     * @return true if content is identical (excluding audit fields)
     */
    private boolean isContentIdentical(GovernanceDocumentation existing, GovernanceDocumentation newDoc) {
        // Create copies to compare content without audit trail fields
        GovernanceDocumentation existingCopy = EcoreUtil.copy(existing);
        GovernanceDocumentation newCopy = EcoreUtil.copy(newDoc);
        
        // Clear audit fields before comparison to focus on actual content
        clearAuditFields(existingCopy);
        clearAuditFields(newCopy);
        
        boolean identical = EcoreUtil.equals(existingCopy, newCopy);
        
        if (logger.isLoggable(java.util.logging.Level.FINE)) {
            logger.fine("Content comparison result: " + (identical ? "IDENTICAL" : "DIFFERENT"));
        }
        
        return identical;
    }
    
    /**
     * Clears audit trail fields for content comparison.
     * 
     * @param doc the documentation to clear audit fields from
     */
    private void clearAuditFields(GovernanceDocumentation doc) {
        // Clear version and timing fields
        doc.setVersion(null);
        doc.setGenerationTimestamp(null);
        doc.setGeneratedBy(null);
        doc.setApprovedBy(null);
        
        // Note: We keep status as it's part of the content/state, not just audit trail
        // The status represents the current state of the documentation content
    }
    
    /**
     * Increments the version number in a semantic way.
     * 
     * @param currentVersion the current version (e.g., "1.0", "2.3")
     * @return the incremented version (e.g., "1.1", "2.4")
     */
    private String incrementVersion(String currentVersion) {
        if (currentVersion == null || currentVersion.isEmpty()) {
            return "1.0";
        }
        
        try {
            // Handle semantic versioning (major.minor format)
            String[] parts = currentVersion.split("\\.");
            if (parts.length >= 2) {
                int major = Integer.parseInt(parts[0]);
                int minor = Integer.parseInt(parts[1]);
                return major + "." + (minor + 1);
            } else if (parts.length == 1) {
                // Single number version
                int version = Integer.parseInt(parts[0]);
                return (version + 1) + ".0";
            }
        } catch (NumberFormatException e) {
            logger.warning("Could not parse version number: " + currentVersion + ", using incremental fallback");
        }
        
        // Fallback: append incremental suffix
        return currentVersion + ".1";
    }
    
    /**
     * Updates audit trail properties for documentation changes.
     * 
     * @param documentation the documentation to update
     * @param reviewUser the user making the change
     * @param reason the reason for the change (optional)
     */
    private void updateAuditTrail(GovernanceDocumentation documentation, String reviewUser, String reason) {
        // Set change tracking fields - these are separate from generation fields
        // Note: GovernanceDocumentation model may need these fields added if not present
        
        // For now, we use the review fields as change tracking
        // In a complete implementation, we'd have dedicated lastChangeUser, lastChangeTime, lastChangeReason fields
        
        if (reason != null && !reason.trim().isEmpty()) {
            // Store reason in description or create a change log entry
            String existingDesc = documentation.getDescription();
            if (existingDesc == null || existingDesc.isEmpty()) {
                documentation.setDescription("Change reason: " + reason);
            } else {
                // Append change reason to existing description
                documentation.setDescription(existingDesc + " | Change: " + reason);
            }
        }
    }
    
    /**
     * Stores a new version of documentation with proper audit trail.
     * 
     * @param objectId the object ID
     * @param documentation the documentation to store
     * @param reviewUser the user storing the documentation
     * @param reason the reason for storing (optional)
     * @return the versioned documentation ID
     */
    private String storeNewVersionWithAuditTrail(String objectId, GovernanceDocumentation documentation, 
                                                String reviewUser, String reason) {
        long timestamp = System.currentTimeMillis();
        String versionedDocId = objectId + "-governance-" + timestamp;
        String latestDocId = objectId + "-governance-latest";
        
        // Create metadata for the documentation with reason
        ObjectMetadata metadata = createDocumentationMetadata(objectId, documentation, reviewUser, versionedDocId, reason);
        
        try {
            // Store versioned documentation
            documentationStorage.storeObject(versionedDocId, documentation, metadata).getValue();
            logger.info("Stored versioned governance documentation: " + versionedDocId + 
                       " (version: " + documentation.getVersion() + ")" +
                       (reason != null ? " Reason: " + reason : ""));
            
            // Store/update latest documentation
            ObjectMetadata latestMetadata = createDocumentationMetadata(objectId, documentation, reviewUser, latestDocId, reason);
            documentationStorage.storeObject(latestDocId, documentation, latestMetadata).getValue();
            logger.info("Updated latest governance documentation: " + latestDocId);
            
            // Update cache
            objectDocumentationCache.put(objectId, timestamp);
            
            return versionedDocId;
            
        } catch (Exception e) {
            logger.severe("Failed to store governance documentation for object " + objectId + ": " + e.getMessage());
            throw new RuntimeException("Failed to store governance documentation", e);
        }
    }
    
    private ObjectMetadata createDocumentationMetadata(String objectId, GovernanceDocumentation documentation, 
                                                      String reviewUser, String docId, String reason) {
        ObjectMetadata metadata = managementFactory.createObjectMetadata();
        
        metadata.setObjectId(docId);
        metadata.setUploadUser(reviewUser);
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel("GOVERNANCE_COMPLIANCE");
        metadata.setObjectType("GovernanceDocumentation");
        metadata.setStatus(ObjectStatus.APPROVED); // Documentation is always approved when stored
        metadata.setObjectName("Governance Documentation for " + objectId);
        
        // Set reason in reviewReason field for audit trail
        if (reason != null && !reason.trim().isEmpty()) {
            metadata.setReviewReason(reason);
        }
        
        // Add custom properties
        addProperty(metadata, "sourceObjectId", objectId);
        addProperty(metadata, "documentationVersion", documentation.getVersion());
        addProperty(metadata, "generatedBy", documentation.getGeneratedBy());
        addProperty(metadata, "approvalStatus", documentation.getStatus() != null ? documentation.getStatus().toString() : "UNKNOWN");
        
        if (documentation.getGenerationTimestamp() != null) {
            addProperty(metadata, "generationTimestamp", documentation.getGenerationTimestamp().toString());
        }
        
        // Add compliance summary
        if (documentation.getComplianceChecks() != null && !documentation.getComplianceChecks().isEmpty()) {
            long passedChecks = documentation.getComplianceChecks().stream()
                .filter(check -> "PASSED".equals(check.getStatus().toString()))
                .count();
            addProperty(metadata, "totalComplianceChecks", documentation.getComplianceChecks().size());
            addProperty(metadata, "passedComplianceChecks", passedChecks);
            addProperty(metadata, "complianceRate", String.format("%.2f", (double) passedChecks / documentation.getComplianceChecks().size()));
        }
        
        return metadata;
    }
    
    private void addProperty(ObjectMetadata metadata, String key, Object value) {
        metadata.getProperties().put(key, value);
    }
}