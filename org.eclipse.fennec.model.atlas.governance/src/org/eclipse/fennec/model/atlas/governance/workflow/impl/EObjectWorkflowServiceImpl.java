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
package org.eclipse.fennec.model.atlas.governance.workflow.impl;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.governance.ApprovalStatus;
import org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation;
import org.eclipse.fennec.model.atlas.governance.PolicyType;
import org.eclipse.fennec.model.atlas.governance.workflow.PostReleaseActionService;
import org.eclipse.fennec.model.atlas.mgmt.annotations.RequireEObjectRegistry;
import org.eclipse.fennec.model.atlas.mgmt.annotations.RequireEObjectStorage;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Enhanced implementation of EObjectWorkflowService with configurable storage providers,
 * transactional copy mechanisms, and governance documentation management.
 * 
 * Features:
 * - Configurable draft and approved storage backends
 * - Transactional copy operations with rollback capability
 * - Object locking during transactional operations
 * - Governance documentation storage
 * - Configurable archiving vs deletion of drafts
 * 
 * @param <T> the EObject type being managed
 */
@Component(
	name = "EObjectWorkflowService",
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    configurationPid = "EObjectWorkflowService",
    service = EObjectWorkflowService.class,
    scope = ServiceScope.PROTOTYPE,
    property = {
        "service.description=Enhanced EObject Workflow Service with configurable storage providers",
        "service.vendor=Data In Motion"
    }
)
@RequireEObjectRegistry
@RequireEObjectStorage
@Designate(ocd = WorkflowServiceConfig.class, factory = true)
public class EObjectWorkflowServiceImpl<T extends EObject> 
    implements EObjectWorkflowService<T>, WorkflowDraftProvider<T>, WorkflowComplianceProvider {

    private static final Logger logger = Logger.getLogger(EObjectWorkflowServiceImpl.class.getName());
    
    private final PromiseFactory promiseFactory = new PromiseFactory(null);
    private final Map<String, ReentrantLock> objectLocks = new ConcurrentHashMap<>();
    private final ManagementFactory managementFactory = ManagementFactory.eINSTANCE;
    
    private WorkflowServiceConfig config;
    
    @Reference(target = "(storage.role=draft)")
    private EObjectStorageService<T> draftStorage;
    
    @Reference(target = "(storage.role=release)")
    private EObjectStorageService<T> approvedStorage;
    
    @Reference
    private EObjectRegistryService<T> registryService;
    
    @Reference
    private GovernanceComplianceService complianceService;
    
    @Reference
    private GovernanceDocumentationService documentationService;
    
    @Reference
    private PostReleaseActionService postReleaseActionService;
    
    @Activate
    void activate(WorkflowServiceConfig config) {
        this.config = requireNonNull(config, "Configuration cannot be null");
        
        // Validate that all required services are properly injected
        requireNonNull(draftStorage, "Draft storage service must be available");
        requireNonNull(approvedStorage, "Approved storage service must be available");
        requireNonNull(registryService, "Registry service must be available");
        requireNonNull(complianceService, "Compliance service must be available");
        requireNonNull(documentationService, "Documentation service must be available");
        requireNonNull(postReleaseActionService, "Post-release action service must be available");
        
        logger.info("Activated EObjectWorkflowService: " + config.workflow_id());
    }
    
    @Deactivate
    void deactivate() {
        // Release all locks
        objectLocks.clear();
        logger.info("Deactivated EObjectWorkflowService: " + config.workflow_id());
    }

    @Override
    public ObjectMetadata approveObject(String objectId, String reviewUser, String approvalReason) {
            requireNonNull(objectId, "Object ID cannot be null");
            requireNonNull(reviewUser, "Review user cannot be null");
            
            ReentrantLock lock = acquireObjectLock(objectId);
            try {
                logger.info("Starting approval process for object: " + objectId);
                
                // 1. Validate object exists in draft storage
                if (!draftStorage.exists(objectId)) {
                    throw new IllegalArgumentException("Object not found in draft storage: " + objectId);
                }
                
                // 2. Retrieve object and metadata from draft storage
                T object = getPromiseValue(draftStorage.retrieveObject(objectId));
                ObjectMetadata metadata = getPromiseValue(draftStorage.retrieveMetadata(objectId));
                
                if (metadata.getStatus() != ObjectStatus.DRAFT) {
                    throw new IllegalStateException("Object is not in DRAFT status: " + metadata.getStatus());
                }
                
                // NEW: Check for governance documentation if required
                if (config.require_approved_governance_documentation()) {
                    logger.info("Checking governance documentation status for object: " + objectId);
                    
                    GovernanceDocumentation govDoc = null;
                    try {
                        govDoc = getPromiseValue(complianceService.getComplianceStatus(objectId));
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Failed to retrieve compliance status for object: " + objectId, e);
                    }
                    
                    if (govDoc == null) {
                        throw new IllegalStateException(
                            "No governance documentation found for object: " + objectId + 
                            ". Compliance check must be run before approval."
                        );
                    }
                    
                    if (govDoc.getStatus() != ApprovalStatus.APPROVED) {
                        throw new IllegalStateException(
                            "Governance documentation is not approved (status: " + govDoc.getStatus() + 
                            "). Object cannot be approved until governance documentation is approved."
                        );
                    }
                    
                    logger.info("Governance documentation validated successfully for object: " + objectId + 
                               " (documentation status: " + govDoc.getStatus() + ")");
                }
                
                // 3. Update metadata for approval
                metadata.setStatus(ObjectStatus.APPROVED);
                metadata.setReviewUser(reviewUser);
                metadata.setReviewTime(Instant.now());
                metadata.setReviewReason(approvalReason);
                metadata.setLastChangeUser(reviewUser);
                metadata.setLastChangeTime(Instant.now());
                
                // 4. Copy to approved storage with new unique objectId (transactional)
                try {
                    // Create new metadata for approved storage with new unique objectId
                    ObjectMetadata approvedMetadata = managementFactory.createObjectMetadata();
                    approvedMetadata.setObjectName(metadata.getObjectName());
                    approvedMetadata.setVersion(metadata.getVersion());
                    approvedMetadata.setStatus(ObjectStatus.APPROVED);
                    approvedMetadata.setUploadUser(metadata.getUploadUser());
                    approvedMetadata.setUploadTime(metadata.getUploadTime());
                    approvedMetadata.setSourceChannel(metadata.getSourceChannel());
                    approvedMetadata.setReviewUser(reviewUser);
                    approvedMetadata.setReviewTime(Instant.now());
                    approvedMetadata.setReviewReason(approvalReason);
                    approvedMetadata.setLastChangeUser(reviewUser);
                    approvedMetadata.setLastChangeTime(Instant.now());
                    // Copy properties
                    approvedMetadata.getProperties().putAll(metadata.getProperties());
                    
                    // Add reference to original draft object ID for governance documentation lookup
                    approvedMetadata.getProperties().put("original.draft.objectId", objectId);
                    
                    // Copy governance documentation ID from draft to approved object
                    if (metadata.getGovernanceDocumentationId() != null) {
                        approvedMetadata.setGovernanceDocumentationId(metadata.getGovernanceDocumentationId());
                        logger.info("Copied governance documentation ID to approved object: " + metadata.getGovernanceDocumentationId());
                    }
                    
                    // Store in approved storage with new unique objectId (auto-generated UUID)
                    String approvedObjectId = getPromiseValue(approvedStorage.storeObject(null, object, approvedMetadata));
                    // The storage service automatically sets the objectId in metadata, but let's ensure it's set
                    approvedMetadata.setObjectId(approvedObjectId);
                    logger.info("Successfully copied object to approved storage with new ID: " + approvedObjectId);
                    
                    // 5. Update registry
                    registryService.updateCache(approvedMetadata);
                    
                    // 6. Handle draft - archive or delete based on configuration
                    if (config.archive_drafts_on_approval()) {
                        metadata.setStatus(ObjectStatus.ARCHIVED);
                        getPromiseValue(draftStorage.updateMetadata(objectId, metadata));
                        logger.info("Archived draft object: " + objectId);
                    } else {
                        getPromiseValue(draftStorage.deleteObject(objectId));
                        logger.info("Deleted draft object: " + objectId);
                    }
                    
                    return approvedMetadata;
                    
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to copy object to approved storage, rolling back", e);
                    if (config.enable_auto_rollback()) {
                        performRollback(objectId, metadata);
                    }
                    throw e;
                }
                
            } finally {
                releaseObjectLock(objectId, lock);
            }
    }

    @Override
    public ObjectMetadata rejectObject(String objectId, String reviewUser, String rejectionReason) {
        requireNonNull(objectId, "Object ID cannot be null");
        requireNonNull(reviewUser, "Review user cannot be null");
        requireNonNull(rejectionReason, "Rejection reason cannot be null");
        
        ReentrantLock lock = acquireObjectLock(objectId);
        try {
            logger.info("Starting rejection process for object: " + objectId);
            
            // Retrieve and update metadata
            ObjectMetadata metadata = getPromiseValue(draftStorage.retrieveMetadata(objectId));
            
            if (metadata == null) {
                throw new IllegalStateException("Object not found in draft storage: " + objectId);
            }
            
            if (metadata.getStatus() != ObjectStatus.DRAFT) {
                throw new IllegalStateException("Object is not in DRAFT status: " + metadata.getStatus());
            }
            
            metadata.setStatus(ObjectStatus.REJECTED);
            metadata.setReviewUser(reviewUser);
            metadata.setReviewTime(Instant.now());
            metadata.setReviewReason(rejectionReason);
            metadata.setLastChangeUser(reviewUser);
            metadata.setLastChangeTime(Instant.now());
            
            // Update storage and registry
            getPromiseValue(draftStorage.updateMetadata(objectId, metadata));
            registryService.updateCache(metadata);
            
            logger.info("Successfully rejected object: " + objectId);
            return metadata;
            
        } catch (Exception e) {
        	throw new IllegalStateException("Error updating the metadata", e);
		} finally {
            releaseObjectLock(objectId, lock);
        }
    }

    @Override
    public ObjectMetadata releaseObject(String objectId, String releaseNotes, boolean requireComplianceCheck) {
        requireNonNull(objectId, "Object ID cannot be null");
        
        ReentrantLock lock = acquireObjectLock(objectId);
        try {
            logger.info("Starting release process for object: " + objectId);
            
            // Retrieve from approved storage
            ObjectMetadata metadata = getPromiseValue(approvedStorage.retrieveMetadata(objectId));
            
            if (metadata == null) {
                throw new IllegalStateException("Object not found in approved storage: " + objectId);
            }
            
            if (metadata.getStatus() != ObjectStatus.APPROVED) {
                throw new IllegalStateException("Object is not in APPROVED status: " + metadata.getStatus());
            }
            
            // Optional: Store governance documentation reference if already available
            if (requireComplianceCheck) {
                logger.info("Linking existing governance documentation for object release: " + objectId);
                
                try {
                    // Get existing governance documentation instead of running new checks
                    GovernanceDocumentation existingDoc = getPromiseValue(complianceService.getComplianceStatus(objectId));
                    
                    if (existingDoc != null && existingDoc.getStatus() == ApprovalStatus.APPROVED) {
                        // Store or update governance documentation reference using the documentation service
                        String storedDocId = getPromiseValue(documentationService.storeDocumentation(objectId, existingDoc, metadata.getReviewUser(), "Referenced during object release"));
                        metadata.setGovernanceDocumentationId(storedDocId);
                        logger.info("Linked existing governance documentation: " + storedDocId + " for object: " + objectId);
                    } else {
                        logger.warning("No approved governance documentation found for release of object: " + objectId);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to link governance documentation for object release: " + objectId, e);
                }
            }
            
            // Update to DEPLOYED status
            metadata.setStatus(ObjectStatus.DEPLOYED);
            metadata.setLastChangeTime(Instant.now());
            if (releaseNotes != null) {
                // Add release notes to properties
                if (metadata.getProperties().stream().noneMatch(p -> "releaseNotes".equals(p.getKey()))) {
                    metadata.getProperties().put("releaseNotes", releaseNotes);
                }
            }
            
            getPromiseValue(approvedStorage.updateMetadata(objectId, metadata));
            registryService.updateCache(metadata);
            
            // Execute post-release actions asynchronously
            try {
                String objectType = metadata.getObjectType();
                String releaseUser = metadata.getReviewUser();
                
                // Execute post-release actions (e.g., EPackage registration)
                postReleaseActionService.executePostReleaseActions(objectId, objectType, releaseUser, releaseNotes)
                    .onSuccess(result -> logger.info("Post-release actions completed successfully for object: " + objectId))
                    .onFailure(error -> logger.log(Level.WARNING, "Post-release actions failed for object: " + objectId, error));
                
                logger.info("Post-release actions initiated for object: " + objectId + " (type: " + objectType + ")");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to initiate post-release actions for object: " + objectId, e);
                // Don't fail the release process if post-release actions fail
            }
            
            logger.info("Successfully released object: " + objectId);
            return metadata;
            
        } finally {
            releaseObjectLock(objectId, lock);
        }
    }

    @Override
    public List<ObjectMetadata> listApprovedObjects() {
        try {
            return requireNonNullElse(registryService.findByStatus(ObjectStatus.APPROVED), List.of());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error listing approved objects via registry, falling back to storage query", e);
            try {
                return requireNonNullElse(getPromiseValue(approvedStorage.queryObjects(createStatusQuery(ObjectStatus.APPROVED))), List.of());
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error listing approved objects, returning empty list", ex);
                return List.of();
            }
        }
    }

    @Override
    public List<ObjectMetadata> listRejectedObjects() {
        try {
            return requireNonNullElse(registryService.findByStatus(ObjectStatus.REJECTED), List.of());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error listing rejected objects via registry, falling back to storage query", e);
            try {
                return requireNonNullElse(getPromiseValue(draftStorage.queryObjects(createStatusQuery(ObjectStatus.REJECTED))), List.of());
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error listing rejected objects, returning empty list", ex);
                return List.of();
            }
        }
    }

    @Override
    public List<ObjectMetadata> listReleasedObjects() {
        try {
            return requireNonNullElse(registryService.findByStatus(ObjectStatus.DEPLOYED), List.of());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error listing released objects via registry, falling back to storage query", e);
            try {
                return requireNonNullElse(getPromiseValue(approvedStorage.queryObjects(createStatusQuery(ObjectStatus.DEPLOYED))), List.of());
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error listing released objects, returning empty list", ex);
                return List.of();
            }
        }
    }

    @Override
    public ObjectMetadata getObject(String objectId) {
        requireNonNull(objectId, "Object ID cannot be null");
        
        // Try approved storage first, then draft storage
        try {
            return getPromiseValue(approvedStorage.retrieveMetadata(objectId));
        } catch (Exception e) {
            return getPromiseValue(draftStorage.retrieveMetadata(objectId));
        }
    }

    @Override
    public Object getObjectContent(String objectId) {
        requireNonNull(objectId, "Object ID cannot be null");
        
        // Try approved storage first, then draft storage
        try {
            return getPromiseValue(approvedStorage.retrieveObject(objectId));
        } catch (Exception e) {
            try {
                return getPromiseValue(draftStorage.retrieveObject(objectId));
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Object not found in any storage: " + objectId, ex);
                return null;
            }
        }
    }

    @Override
    public Promise<Void> updateObject(String objectId, T updatedObject) {
        return promiseFactory.submit(() -> {
            requireNonNull(objectId, "Object ID cannot be null");
            requireNonNull(updatedObject, "Updated object cannot be null");
            
            ReentrantLock lock = acquireObjectLock(objectId);
            try {
                // Update in appropriate storage based on current status
                ObjectMetadata metadata = getObject(objectId);
                
                if (metadata.getStatus() == ObjectStatus.DRAFT || metadata.getStatus() == ObjectStatus.REJECTED) {
                    return draftStorage.storeObject(objectId, updatedObject, metadata).then(p -> null);
                } else {
                    return approvedStorage.storeObject(objectId, updatedObject, metadata).then(p -> null);
                }
                
            } finally {
                releaseObjectLock(objectId, lock);
            }
        }).then(p -> null);
    }

    @Override
    public Promise<Boolean> deleteObject(String objectId) {
        return promiseFactory.submit(() -> {
            requireNonNull(objectId, "Object ID cannot be null");
            
            ReentrantLock lock = acquireObjectLock(objectId);
            try {
                boolean deleted = false;
                
                // Try to delete from both storages
                try {
                    deleted = getPromiseValue(draftStorage.deleteObject(objectId)) || deleted;
                } catch (Exception e) {
                    // Ignore if not found in draft storage
                }
                
                try {
                    deleted = getPromiseValue(approvedStorage.deleteObject(objectId)) || deleted;
                } catch (Exception e) {
                    // Ignore if not found in approved storage
                }
                
                // Remove from registry
                if (deleted) {
                    registryService.removeFromCache(objectId);
                }
                
                return deleted;
                
            } finally {
                releaseObjectLock(objectId, lock);
            }
        });
    }

    // WorkflowDraftProvider implementation methods would go here...
    // WorkflowComplianceProvider implementation methods would go here...
    
    // Helper methods
    
    /**
     * Helper method to unwrap Promise results with proper exception handling
     */
    private <R> R getPromiseValue(Promise<R> promise) {
        try {
            return promise.getValue();
        } catch (InvocationTargetException | InterruptedException e) {
            throw new RuntimeException("Promise execution failed", e);
        }
    }
    
    private ReentrantLock acquireObjectLock(String objectId) {
        ReentrantLock lock = objectLocks.computeIfAbsent(objectId, k -> new ReentrantLock());
        try {
            if (!lock.tryLock(config.transaction_timeout_ms(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("Failed to acquire lock for object: " + objectId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while acquiring lock for object: " + objectId, e);
        }
        return lock;
    }
    
    private void releaseObjectLock(String objectId, ReentrantLock lock) {
        if (nonNull(lock) && lock.isHeldByCurrentThread()) {
            lock.unlock();
            // Clean up unused locks
            if (!lock.hasQueuedThreads()) {
                objectLocks.remove(objectId, lock);
            }
        }
    }
    
    private void performRollback(String objectId, ObjectMetadata originalMetadata) {
        try {
            logger.info("Performing rollback for object: " + objectId);
            
            // Restore original metadata in draft storage
            originalMetadata.setStatus(ObjectStatus.DRAFT);
            originalMetadata.setReviewUser(null);
            originalMetadata.setReviewTime(null);
            originalMetadata.setReviewReason(null);
            
            draftStorage.updateMetadata(objectId, originalMetadata);
            
            // Try to remove from approved storage if it was added
            try {
                approvedStorage.deleteObject(objectId);
            } catch (Exception e) {
                // Ignore if not found
            }
            
            logger.info("Rollback completed for object: " + objectId);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Rollback failed for object: " + objectId, e);
        }
    }
    
    
    private ObjectQuery createStatusQuery(ObjectStatus status) {
        ObjectQuery query = managementFactory.createObjectQuery();
        query.setStatus(status);
        return query;
    }

    // Placeholder implementations for missing methods from interfaces
    @Override
    public Promise<String> uploadDraft(T object, ObjectMetadata metadata) {
        return promiseFactory.submit(() -> {
            requireNonNull(object, "Object cannot be null");
            requireNonNull(metadata, "Metadata cannot be null");
            
            // Ensure draft status
            metadata.setStatus(ObjectStatus.DRAFT);
            metadata.setLastChangeTime(Instant.now());
            
            requireNonNull(metadata.getObjectName());
            
            // Store in draft storage and return the object ID
            String objectId = getPromiseValue(draftStorage.storeObject(metadata.getObjectId(), object, metadata));
            
            // NEW: Trigger automatic compliance check if configured
            if (config.enable_auto_compliance_check()) {
                logger.info("Triggering automatic compliance check for uploaded draft: " + objectId);
                try {
                    // Use our improved checkDraft method for consistency and proper governanceDocumentationId management
                    checkDraft(objectId, metadata.getUploadUser(), "Automatic compliance check after draft upload").getValue();
                    logger.info("Automatic compliance check initiated for draft: " + objectId);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to trigger automatic compliance check for draft: " + objectId, e);
                    // Don't fail the upload if compliance check fails to start
                }
            }
            
            return objectId;
        });
    }

    @Override
    public List<ObjectMetadata> listDraftObjects() {
        try {
            return requireNonNullElse(registryService.findByStatus(ObjectStatus.DRAFT), List.of());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error listing draft objects via registry, falling back to storage query", e);
            try {
                return requireNonNullElse(getPromiseValue(draftStorage.queryObjects(createStatusQuery(ObjectStatus.DRAFT))), List.of());
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error listing draft objects, returning empty list", ex);
                return List.of();
            }
        }
    }

    @Override
    public ObjectMetadata getDraft(String objectId) {
        requireNonNull(objectId, "Object ID cannot be null");
        return getPromiseValue(draftStorage.retrieveMetadata(objectId));
    }

    @Override
    public T getDraftContent(String objectId) {
        requireNonNull(objectId, "Object ID cannot be null");
        return getPromiseValue(draftStorage.retrieveObject(objectId));
    }

    @Override
    public Promise<Void> updateDraft(String objectId, T updatedObject) {
        return promiseFactory.submit(() -> {
            requireNonNull(objectId, "Object ID cannot be null");
            requireNonNull(updatedObject, "Updated object cannot be null");
            
            // Get current metadata
            ObjectMetadata metadata = getPromiseValue(draftStorage.retrieveMetadata(objectId));
            
            // Ensure it's still a draft
            if (metadata.getStatus() != ObjectStatus.DRAFT && metadata.getStatus() != ObjectStatus.REJECTED) {
                throw new IllegalStateException("Can only update objects in DRAFT or REJECTED status");
            }
            
            metadata.setLastChangeTime(Instant.now());
            
            // Update the object in draft storage
            getPromiseValue(draftStorage.storeObject(objectId, updatedObject, metadata));
            return null;
        });
    }

    @Override
    public Promise<Boolean> deleteDraft(String objectId) {
        return promiseFactory.submit(() -> {
            requireNonNull(objectId, "Object ID cannot be null");
            
            // Verify it exists and is in draft status
            ObjectMetadata metadata = getPromiseValue(draftStorage.retrieveMetadata(objectId));
            if (metadata.getStatus() != ObjectStatus.DRAFT && metadata.getStatus() != ObjectStatus.REJECTED) {
                throw new IllegalStateException("Can only delete objects in DRAFT or REJECTED status");
            }
            
            // Delete from draft storage
            boolean deleted = getPromiseValue(draftStorage.deleteObject(objectId));
            
            // Remove from registry
            if (deleted) {
                registryService.removeFromCache(objectId);
            }
            
            return deleted;
        });
    }

    @Override
    public Promise<GovernanceDocumentation> checkDraft(String objectId, String reviewUser, String complianceReason) {
        // Validate parameters immediately (not inside Promise task)
        requireNonNull(objectId, "Object ID cannot be null");
        requireNonNull(reviewUser, "Review user cannot be null");
        
        return promiseFactory.submit(() -> {
            // Validate that the object exists as a draft
            ObjectMetadata metadata = getPromiseValue(draftStorage.retrieveMetadata(objectId));
            if (metadata == null) {
                throw new IllegalArgumentException("Draft object not found: " + objectId);
            }
            
            // Run compliance checks via the compliance service
            String reason = complianceReason != null ? complianceReason : "Draft compliance checking";
            GovernanceDocumentation documentation = getPromiseValue(complianceService.runComplianceChecks(objectId, reviewUser, reason));
            
            // Store the governance documentation and get its ID
            String documentationId = getPromiseValue(documentationService.storeDocumentation(objectId, documentation, reviewUser, reason));
            
            // Update the draft metadata with the governance documentation ID
            metadata.setGovernanceDocumentationId(documentationId);
            getPromiseValue(draftStorage.updateMetadata(objectId, metadata));
            
            logger.info("Updated draft object " + objectId + " with governance documentation ID: " + documentationId);
            
            return documentation;
        });
    }

    @Override
    public Promise<GovernanceDocumentation> checkObject(String objectId, String reviewUser, String complianceReason) {
        // Validate parameters immediately (not inside Promise task)
        requireNonNull(objectId, "Object ID cannot be null");
        requireNonNull(reviewUser, "Review user cannot be null");
        
        return promiseFactory.submit(() -> {
            // Validate that the object exists in approved storage
            ObjectMetadata metadata = getPromiseValue(approvedStorage.retrieveMetadata(objectId));
            if (metadata == null) {
                throw new IllegalArgumentException("Object not found in approved storage: " + objectId);
            }
            
            // Get the original object ID for compliance checks (governance documentation is tied to original ID)
            String originalObjectId = getOriginalDraftObjectId(objectId);
            String complianceObjectId = originalObjectId != null ? originalObjectId : objectId;
            
            // Run compliance checks via the compliance service
            String reason = complianceReason != null ? complianceReason : "Object compliance checking";
            GovernanceDocumentation documentation = getPromiseValue(complianceService.runComplianceChecks(complianceObjectId, reviewUser, reason));
            
            // Store the updated governance documentation and get its ID
            String documentationId = getPromiseValue(documentationService.storeDocumentation(complianceObjectId, documentation, reviewUser, reason));
            
            // Update the approved/released object metadata with the new governance documentation ID
            metadata.setGovernanceDocumentationId(documentationId);
            getPromiseValue(approvedStorage.updateMetadata(objectId, metadata));
            
            logger.info("Updated approved/released object " + objectId + " with governance documentation ID: " + documentationId);
            
            return documentation;
        });
    }

    @Override
    public Promise<GovernanceDocumentation> runComplianceChecks(String objectId, List<PolicyType> policyTypes, String reviewUser) {
        return promiseFactory.submit(() -> {
            return getPromiseValue(complianceService.runSpecificComplianceChecks(objectId, policyTypes, reviewUser));
        });
    }

    @Override
    public GovernanceDocumentation getComplianceStatus(String objectId) {
        requireNonNull(objectId, "Object ID cannot be null");
        
        try {
            // 1. First try to get governance documentation ID from object metadata
            String governanceDocumentationId = getGovernanceDocumentationIdFromMetadata(objectId);
            
            if (governanceDocumentationId != null) {
                // Use the direct documentation ID for lookup
                Optional<GovernanceDocumentation> documentationOpt = documentationService.getDocumentation(governanceDocumentationId);
                if (documentationOpt.isPresent()) {
                    logger.info("Found governance documentation using stored ID: " + governanceDocumentationId);
                    return documentationOpt.get();
                } else {
                    logger.warning("Governance documentation ID " + governanceDocumentationId + " stored in metadata but documentation not found");
                }
            }
            
            // 2. Fallback: Try direct lookup with the provided object ID (legacy support)
            GovernanceDocumentation documentation = getPromiseValue(complianceService.getComplianceStatus(objectId));
            
            // 3. Fallback: If not found and this might be an approved/released object, check for original draft reference
            if (documentation == null) {
                String originalDraftId = getOriginalDraftObjectId(objectId);
                if (originalDraftId != null && !originalDraftId.equals(objectId)) {
                    logger.info("Governance documentation not found for " + objectId + ", checking original draft ID: " + originalDraftId);
                    documentation = getPromiseValue(complianceService.getComplianceStatus(originalDraftId));
                    if (documentation != null) {
                        logger.info("Found governance documentation using original draft ID: " + originalDraftId);
                    }
                }
            }
            
            return documentation;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to retrieve compliance status for object: " + objectId, e);
            return null;
        }
    }
    
    /**
     * Get the governance documentation ID from object metadata.
     * Checks both approved and draft storage for the object.
     * 
     * @param objectId the object ID
     * @return the governance documentation ID if found, null otherwise
     */
    private String getGovernanceDocumentationIdFromMetadata(String objectId) {
        try {
            // Check approved storage first (most likely for active objects)
            if (approvedStorage.exists(objectId)) {
                ObjectMetadata metadata = getPromiseValue(approvedStorage.retrieveMetadata(objectId));
                if (metadata != null && metadata.getGovernanceDocumentationId() != null) {
                    return metadata.getGovernanceDocumentationId();
                }
            }
            
            // Check draft storage
            if (draftStorage.exists(objectId)) {
                ObjectMetadata metadata = getPromiseValue(draftStorage.retrieveMetadata(objectId));
                if (metadata != null && metadata.getGovernanceDocumentationId() != null) {
                    return metadata.getGovernanceDocumentationId();
                }
            }
            
        } catch (Exception e) {
            logger.log(Level.FINE, "Could not retrieve governance documentation ID from metadata for: " + objectId, e);
        }
        
        return null;
    }
    
    /**
     * Get the original draft object ID for an approved/released object.
     * This method checks the object's metadata for a reference to the original draft.
     * 
     * @param objectId the approved/released object ID
     * @return the original draft object ID if found, or null if not found or if this is already a draft
     */
    private String getOriginalDraftObjectId(String objectId) {
        try {
            // Check in approved storage first
            if (approvedStorage.exists(objectId)) {
                ObjectMetadata metadata = getPromiseValue(approvedStorage.retrieveMetadata(objectId));
                if (metadata != null && metadata.getProperties().containsKey("original.draft.objectId")) {
                    Object originalDraftId = metadata.getProperties().get("original.draft.objectId");
                    return originalDraftId != null ? originalDraftId.toString() : null;
                }
            }
            
            // If not in approved storage, check draft storage (object might still be there)
            if (draftStorage.exists(objectId)) {
                // This is likely the original draft object, return the same ID
                return objectId;
            }
            
        } catch (Exception e) {
            logger.log(Level.FINE, "Could not determine original draft object ID for: " + objectId, e);
        }
        
        return null;
    }

    @Override
    public GovernanceDocumentation setGovernanceDocumentationDraft(String objectId, String reviewUser, String reason) {
        requireNonNull(objectId, "Object ID cannot be null");
        requireNonNull(reviewUser, "Review user cannot be null");
        
        logger.info("Setting governance documentation to DRAFT for object: " + objectId + ", user: " + reviewUser + ", reason: " + reason);
        
        ReentrantLock lock = acquireObjectLock(objectId);
        try {
            // Get existing governance documentation
            GovernanceDocumentation govDoc = getComplianceStatus(objectId);
            ApprovalStatus currentStatus = govDoc != null ? govDoc.getStatus() : null;
            
            // Validate state transition using state machine
            GovernanceDocumentationStateMachine.validateTransition(currentStatus, ApprovalStatus.DRAFT, objectId);
            
            if (govDoc == null) {
                // Create new governance documentation if none exists
                govDoc = getPromiseValue(complianceService.runComplianceChecks(objectId, reviewUser, 
                    reason != null ? reason : "Initial governance documentation creation"));
            }
            
            // Update status to DRAFT with proper metadata
            govDoc.setStatus(ApprovalStatus.DRAFT);
            govDoc.setGeneratedBy(reviewUser);
            govDoc.setGenerationTimestamp(new Date());
            
            // Store updated documentation with reason - versioning handles audit trail
            String storeReason = reason != null ? reason : "Status changed to DRAFT by " + reviewUser;
            String docId = getPromiseValue(documentationService.storeDocumentation(objectId, govDoc, reviewUser, storeReason));
            logger.info("Governance documentation set to DRAFT. Documentation ID: " + docId);
            
            return govDoc;
            
        } catch (IllegalStateException e) {
            logger.log(Level.WARNING, "Invalid state transition to DRAFT for object: " + objectId, e);
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to set governance documentation to DRAFT for object: " + objectId, e);
            throw new RuntimeException("Failed to set governance documentation to DRAFT", e);
        } finally {
            releaseObjectLock(objectId, lock);
        }
    }

    @Override
    public GovernanceDocumentation setGovernanceDocumentationInReview(String objectId, String reviewUser, String reason) {
        requireNonNull(objectId, "Object ID cannot be null");
        requireNonNull(reviewUser, "Review user cannot be null");
        
        logger.info("Setting governance documentation to IN_REVIEW for object: " + objectId + ", user: " + reviewUser + ", reason: " + reason);
        
        ReentrantLock lock = acquireObjectLock(objectId);
        try {
            // Get existing governance documentation
            GovernanceDocumentation govDoc = getComplianceStatus(objectId);
            
            if (govDoc == null) {
                throw new IllegalStateException("No governance documentation found for object: " + objectId + 
                    ". Cannot set to IN_REVIEW without existing documentation.");
            }
            
            // Validate state transition using state machine
            ApprovalStatus currentStatus = govDoc.getStatus();
            GovernanceDocumentationStateMachine.validateTransition(currentStatus, ApprovalStatus.IN_REVIEW, objectId);
            
            // Update status to IN_REVIEW with proper metadata
            govDoc.setStatus(ApprovalStatus.IN_REVIEW);
            govDoc.setGeneratedBy(reviewUser);
            govDoc.setGenerationTimestamp(new Date());
            
            // Store updated documentation with reason - versioning handles audit trail
            String storeReason = reason != null ? reason : "Status changed to IN_REVIEW by " + reviewUser;
            String docId = getPromiseValue(documentationService.storeDocumentation(objectId, govDoc, reviewUser, storeReason));
            logger.info("Governance documentation set to IN_REVIEW. Documentation ID: " + docId);
            
            return govDoc;
            
        } catch (IllegalStateException e) {
            logger.log(Level.WARNING, "Invalid state transition to IN_REVIEW for object: " + objectId, e);
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to set governance documentation to IN_REVIEW for object: " + objectId, e);
            throw new RuntimeException("Failed to set governance documentation to IN_REVIEW", e);
        } finally {
            releaseObjectLock(objectId, lock);
        }
    }

    @Override
    public GovernanceDocumentation setGovernanceDocumentationApproved(String objectId, String reviewUser, String reason) {
        requireNonNull(objectId, "Object ID cannot be null");
        requireNonNull(reviewUser, "Review user cannot be null");
        
        logger.info("Setting governance documentation to APPROVED for object: " + objectId + ", user: " + reviewUser + ", reason: " + reason);
        
        ReentrantLock lock = acquireObjectLock(objectId);
        try {
            // Get existing governance documentation
            GovernanceDocumentation govDoc = getComplianceStatus(objectId);
            
            if (govDoc == null) {
                throw new IllegalStateException("No governance documentation found for object: " + objectId + 
                    ". Cannot approve without existing documentation.");
            }
            
            // Validate state transition using state machine
            ApprovalStatus currentStatus = govDoc.getStatus();
            GovernanceDocumentationStateMachine.validateTransition(currentStatus, ApprovalStatus.APPROVED, objectId);
            
            // Update status to APPROVED with proper metadata
            govDoc.setStatus(ApprovalStatus.APPROVED);
            govDoc.setApprovedBy(reviewUser);
            govDoc.setGenerationTimestamp(new Date());
            
            // Store updated documentation with reason - versioning handles audit trail
            String storeReason = reason != null ? reason : "Status changed to APPROVED by " + reviewUser;
            String docId = getPromiseValue(documentationService.storeDocumentation(objectId, govDoc, reviewUser, storeReason));
            logger.info("Governance documentation APPROVED. Documentation ID: " + docId + ". Object approval now enabled.");
            
            return govDoc;
            
        } catch (IllegalStateException e) {
            logger.log(Level.WARNING, "Invalid state transition to APPROVED for object: " + objectId, e);
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to approve governance documentation for object: " + objectId, e);
            throw new RuntimeException("Failed to approve governance documentation", e);
        } finally {
            releaseObjectLock(objectId, lock);
        }
    }

    @Override
    public GovernanceDocumentation setGovernanceDocumentationRejected(String objectId, String reviewUser, String reason) {
        requireNonNull(objectId, "Object ID cannot be null");
        requireNonNull(reviewUser, "Review user cannot be null");
        
        logger.info("Setting governance documentation to REJECTED for object: " + objectId + ", user: " + reviewUser + ", reason: " + reason);
        
        ReentrantLock lock = acquireObjectLock(objectId);
        try {
            // Get existing governance documentation
            GovernanceDocumentation govDoc = getComplianceStatus(objectId);
            
            if (govDoc == null) {
                throw new IllegalStateException("No governance documentation found for object: " + objectId + 
                    ". Cannot reject without existing documentation.");
            }
            
            // Validate state transition using state machine
            ApprovalStatus currentStatus = govDoc.getStatus();
            GovernanceDocumentationStateMachine.validateTransition(currentStatus, ApprovalStatus.REJECTED, objectId);
            
            // Update status to REJECTED with proper metadata
            govDoc.setStatus(ApprovalStatus.REJECTED);
            govDoc.setGeneratedBy(reviewUser); // Use generatedBy instead of setRejectedBy
            govDoc.setGenerationTimestamp(new Date());
            
            // Store updated documentation with reason - versioning handles audit trail
            String storeReason = reason != null ? reason : "Status changed to REJECTED by " + reviewUser;
            String docId = getPromiseValue(documentationService.storeDocumentation(objectId, govDoc, reviewUser, storeReason));
            logger.info("Governance documentation REJECTED. Documentation ID: " + docId + ". Object approval blocked until resolved.");
            
            return govDoc;
            
        } catch (IllegalStateException e) {
            logger.log(Level.WARNING, "Invalid state transition to REJECTED for object: " + objectId, e);
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reject governance documentation for object: " + objectId, e);
            throw new RuntimeException("Failed to reject governance documentation", e);
        } finally {
            releaseObjectLock(objectId, lock);
        }
    }
}