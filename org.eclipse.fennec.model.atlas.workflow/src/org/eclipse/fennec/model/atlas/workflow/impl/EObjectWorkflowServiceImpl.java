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
package org.eclipse.fennec.model.atlas.workflow.impl;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.annotations.RequireEObjectRegistry;
import org.eclipse.fennec.model.atlas.mgmt.annotations.RequireEObjectStorage;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.collector.EObjectStorageServiceCollector;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService;
import org.eclipse.fennec.model.atlas.workflow.PostReleaseActionService;
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
 * transactional copy mechanisms
 * 
 * Features:
 * - Configurable draft and approved storage backends
 * - Transactional copy operations with rollback capability
 * - Object locking during transactional operations
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
public class EObjectWorkflowServiceImpl<T extends EObject> implements WorkflowStageProvider<T>, WorkflowTransitionService<T>{

	private static final Logger logger = Logger.getLogger(EObjectWorkflowServiceImpl.class.getName());

	private final PromiseFactory promiseFactory = new PromiseFactory(null);
	private final Map<String, ReentrantLock> objectLocks = new ConcurrentHashMap<>();
	private final ManagementFactory managementFactory = ManagementFactory.eINSTANCE;

	private WorkflowServiceConfig config;

	@Reference
	EObjectStorageServiceCollector storageServiceCollector;

	//    @Reference(target = "(storage.role=draft)")
	//    private EObjectStorageService<T> draftStorage;
	//    
	//    @Reference(target = "(storage.role=release)")
	//    private EObjectStorageService<T> approvedStorage;

	@Reference
	private EObjectRegistryService<T> registryService;


	@Reference
	private PostReleaseActionService postReleaseActionService;

	@Activate
	void activate(WorkflowServiceConfig config) {
		this.config = requireNonNull(config, "Configuration cannot be null");

		// Validate that all required services are properly injected
		requireNonNull(draftStorage, "Draft storage service must be available");
		requireNonNull(approvedStorage, "Approved storage service must be available");
		requireNonNull(registryService, "Registry service must be available");
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


	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#uploadToStage(java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Promise<String> uploadToStage(String stage, T object, ObjectMetadata metadata) {
		return promiseFactory.submit(() -> {
			requireNonNull(object, "Object cannot be null");
			requireNonNull(metadata, "Metadata cannot be null");

			// Ensure draft status
			metadata.setStatus(ObjectStatus.DRAFT);
			metadata.setLastChangeTime(Instant.now());

			requireNonNull(metadata.getObjectName());

			EObjectStorageService<T> storageService = getStorageByStage(stage);
			if(storageService == null) {
				logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Object %s cannot be saved", stage, metadata.getObjectId()));
				return null;
			}
			String objectId = getPromiseValue(storageService.storeObject(metadata.getObjectId(), object, metadata));

			return objectId;
		});
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromStage(java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getFromStage(String stage, String objectId) {
		requireNonNull(objectId, "Object ID cannot be null");
		EObjectStorageService<T> storageService = getStorageByStage(stage);
		if(storageService != null) return getPromiseValue(storageService.retrieveMetadata(objectId));
		return null;
	}

	@SuppressWarnings("unchecked")
	private EObjectStorageService<T> getStorageByStage(String stage) {
		EObjectStorageService<T> storageService = (EObjectStorageService<T>) storageServiceCollector.getStorageByRole(stage);
		if(storageService == null) {
			logger.severe(String.format("Cannot retrieve EObjectStorageService for %s", stage));
			return null;
		}
		return storageService;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getContentFromStage(java.lang.String, java.lang.String)
	 */
	@Override
	public T getContentFromStage(String stage, String objectId) {
		requireNonNull(objectId, "Object ID cannot be null");
		EObjectStorageService<T> storageService = getStorageByStage(stage);
		if(storageService != null) return getPromiseValue(storageService.retrieveObject(objectId));
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#updateInStage(java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String)
	 */
	@Override
	public Promise<Void> updateInStage(String stage, T updatedObject, String objectId) {
		return promiseFactory.submit(() -> {
			requireNonNull(objectId, "Object ID cannot be null");
			requireNonNull(updatedObject, "Updated object cannot be null");
			
			EObjectStorageService<T> storageService = getStorageByStage(stage);
			if(storageService == null) {
				logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Object %s cannot be updated", stage, objectId));
				return null;
			}

			// Get current metadata
			ObjectMetadata metadata = getPromiseValue(storageService.retrieveMetadata(objectId));

			// Ensure it's still a draft
			if (metadata.getStatus() != ObjectStatus.DRAFT && metadata.getStatus() != ObjectStatus.REJECTED) {
				throw new IllegalStateException("Can only update objects in DRAFT or REJECTED status");
			}

			metadata.setLastChangeTime(Instant.now());

			// Update the object in draft storage
			getPromiseValue(storageService.storeObject(objectId, updatedObject, metadata));
			return null;
		});
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#deleteFromStage(java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<Boolean> deleteFromStage(String stage, String objectId) {
		return promiseFactory.submit(() -> {
			requireNonNull(objectId, "Object ID cannot be null");
			
			EObjectStorageService<T> storageService = getStorageByStage(stage);
			if(storageService == null) {
				logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Object %s cannot be deleted", stage, objectId));
				return false;
			}

			// Verify it exists and is in draft status
			ObjectMetadata metadata = getPromiseValue(storageService.retrieveMetadata(objectId));
			if (metadata.getStatus() != ObjectStatus.DRAFT && metadata.getStatus() != ObjectStatus.REJECTED) {
				throw new IllegalStateException("Can only delete objects in DRAFT or REJECTED status");
			}

			// Delete from draft storage
			boolean deleted = getPromiseValue(storageService.deleteObject(objectId));

			// Remove from registry
			if (deleted) {
				registryService.removeFromCache(objectId);
			}

			return deleted;
		});
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStage(java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStage(String stage) {
		try {
			return requireNonNullElse(registryService.findByStatus(ObjectStatus.DRAFT), List.of());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error listing draft objects via registry, falling back to storage query", e);
			try {
				EObjectStorageService<T> storageService = getStorageByStage(stage);
				if(storageService == null) {
					logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Cannot list objects.", stage));
					return List.of();;
				}
				return requireNonNullElse(getPromiseValue(storageService.queryObjects(createStatusQuery(ObjectStatus.DRAFT))), List.of());
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Error listing draft objects, returning empty list", ex);
				return List.of();
			}
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#transitionToStage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata transitionToStage(String objectId, String fromStage, String toStage) {
		// TODO Auto-generated method stub
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService#isTransitionAllowed(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isTransitionAllowed(String fromStage, String toStage) {
		// TODO Auto-generated method stub
		return false;
	}
}