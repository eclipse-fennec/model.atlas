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

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import org.eclipse.fennec.model.atlas.workflow.PostReleaseActionService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
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
public class EObjectWorkflowServiceImpl<T extends EObject> implements EObjectWorkflowService<T>{

	private static final Logger logger = Logger.getLogger(EObjectWorkflowServiceImpl.class.getName());

	private final PromiseFactory promiseFactory = new PromiseFactory(null);
	private final Map<String, ReentrantLock> objectLocks = new ConcurrentHashMap<>();
	private final ManagementFactory managementFactory = ManagementFactory.eINSTANCE;

	private WorkflowServiceConfig config;

	@Reference
	EObjectStorageServiceCollector storageServiceCollector;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	EObjectWorkflowService<T> parentWorkflowService;

	@Reference
	private EObjectRegistryService<T> registryService;


	@Reference
	private PostReleaseActionService postReleaseActionService;

	@Activate
	void activate(WorkflowServiceConfig config) {
		this.config = requireNonNull(config, "Configuration cannot be null");

		// Validate that all required services are properly injected

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






	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#uploadToStage(java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
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
		if(storageService != null) {
			ObjectMetadata localMetadata = getPromiseValue(storageService.retrieveMetadata(objectId));
			if(localMetadata == null && parentWorkflowService != null) {
				logger.warning(String.format("Object %s not found for scope %s. Looking in the parent scope %s release stage", objectId, config.scope(), config.parent_scope()));
				return parentWorkflowService.getFromStage("release", objectId);
			}			
			return getPromiseValue(storageService.retrieveMetadata(objectId));
		}
		return null;
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
					return List.of();
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
		// Validate transition is allowed
		isTransitionAllowed(fromStage, toStage);

		// Get object from source stage
		EObjectStorageService<T> sourceStorage = getStorageByStage(fromStage);
		if(sourceStorage == null) {
			logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Cannot move object.", fromStage));
			return null;
		}
		//        String sourceId = buildObjectId(fromStage, nsUri);

		T object =  getPromiseValue(sourceStorage.retrieveObject(objectId));
		ObjectMetadata metadata = getPromiseValue(sourceStorage.retrieveMetadata(objectId));

		if (object == null || metadata == null) {
			throw new IllegalArgumentException("Object not found in stage " + fromStage + ": " + objectId);
		}

		// Update metadata for new stage
		//        metadata.setStage(toStage);
		metadata.setLastChangeTime(Instant.now());

		// Store in target stage
		EObjectStorageService<T> targetStorage = getStorageByStage(toStage);
		if(targetStorage == null) {
			logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Cannot move object.", toStage));
			return null;
		}
		getPromiseValue(targetStorage.storeObject(objectId, object, metadata));

		// Delete from source stage (if configured)
		if (config.delete_after_transition()) {
			getPromiseValue(sourceStorage.deleteObject(objectId));
		}

		return metadata;

	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService#isTransitionAllowed(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isTransitionAllowed(String fromStage, String toStage) {
		// TODO Auto-generated method stub
		return true;
	}

	// Helper methods

	@SuppressWarnings("unchecked")
	private EObjectStorageService<T> getStorageByStage(String stage) {
		EObjectStorageService<T> storageService = (EObjectStorageService<T>) storageServiceCollector.getStorage(config.scope(), stage);
		if(storageService == null) {
			logger.severe(String.format("Cannot retrieve EObjectStorageService for %s", stage));
			return null;
		}
		return storageService;
	}

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




	private ObjectQuery createStatusQuery(ObjectStatus status) {
		ObjectQuery query = managementFactory.createObjectQuery();
		query.setStatus(status);
		return query;
	}
}