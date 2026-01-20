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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.impl;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.atlas.mgmt.annotations.RequireEObjectRegistry;
import org.eclipse.fennec.model.atlas.mgmt.annotations.RequireEObjectStorage;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.collector.EObjectStorageServiceCollector;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
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
		requireTrue(isStageAllowed(config.final_stage()), String.format("Final Stage %s should also be part of the stages config property", config.final_stage()));
		for(String writableStage : config.writable_stages()) requireTrue(isStageAllowed(writableStage), String.format("Writable Stage %s should also be part of the stages config property", writableStage));
		
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
	public Promise<ObjectMetadata> uploadToStage(String stage, T object, ObjectMetadata metadata) {
		return promiseFactory.submit(() -> {
			requireNonNull(object, "Object cannot be null");
			requireNonNull(metadata, "Metadata cannot be null");
			requireTrue(isStageAllowed(stage), String.format("Stage %s is not supported from WorkflowService", stage));

			metadata.setLastChangeTime(Instant.now());
			metadata.setRole(stage);
			metadata.setScope(config.scope());

			requireNonNull(metadata.getObjectName());

			EObjectStorageService<T> storageService = getStorageByStage(stage);
			if(storageService == null) {
				logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Object %s cannot be saved", stage, metadata.getObjectId()));
				return null;
			}
			ObjectMetadata objectMetadata = getPromiseValue(storageService.storeObject(metadata.getObjectId(), object, metadata));
			return objectMetadata;
		});
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromStage(java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getFromStage(String stage, String objectId) {
		requireNonNull(objectId, "Object ID cannot be null");
		requireTrue(isStageAllowed(stage), String.format("Stage %s is not supported from WorkflowService", stage));
		EObjectStorageService<T> storageService = getStorageByStage(stage);
		if(storageService != null) {
			ObjectMetadata localMetadata = getPromiseValue(storageService.retrieveMetadata(objectId));
			if(localMetadata == null && parentWorkflowService != null) {
				logger.warning(String.format("Object %s not found for scope %s. Looking in the parent scope %s final stage", objectId, config.scope(), config.parent_scope()));
//				we might want to set a read-only flag on the parent metadata here
				ObjectMetadata parentMetadata = parentWorkflowService.getFromFinalStage(objectId);
				if(parentMetadata != null) parentMetadata.setIsReadOnly(true);
				return parentMetadata;
			} else if(localMetadata == null) {
				return localMetadata;
			}
//			if stage is not writable we might want to set a read-only flag (?)
			if(!isStageWritable(stage)) localMetadata.setIsReadOnly(true);
			return localMetadata;
		}
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromFinalStage(java.lang.String)
	 */
	@Override
	public ObjectMetadata getFromFinalStage(String objectId) {
		return getFromStage(config.final_stage(), objectId);
	}


	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getContentFromStage(java.lang.String, java.lang.String)
	 */
	@Override
	public T getContentFromStage(String stage, String objectId) {
		requireNonNull(objectId, "Object ID cannot be null");
		requireTrue(isStageAllowed(stage), String.format("Stage %s is not supported from WorkflowService", stage));
		EObjectStorageService<T> storageService = getStorageByStage(stage);
		if(storageService != null) return getPromiseValue(storageService.retrieveObject(objectId));
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#updateInStage(java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<ObjectMetadata> updateInStage(String stage, T updatedObject, String objectId, String updatedVersion) {
		return promiseFactory.submit(() -> {
			requireNonNull(objectId, "Object ID cannot be null");
			requireNonNull(updatedObject, "Updated object cannot be null");
			requireTrue(isStageAllowed(stage), String.format("Stage %s is not supported from WorkflowService", stage));
			requireTrue(isStageWritable(stage), String.format("Stage %s is not writable for this WorkflowService", stage));

			EObjectStorageService<T> storageService = getStorageByStage(stage);
			if(storageService == null) {
				logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Object %s cannot be updated", stage, objectId));
				return null;
			}

			// Get current metadata
			ObjectMetadata metadata = getPromiseValue(storageService.retrieveMetadata(objectId));
			metadata.setLastChangeTime(Instant.now());
			metadata.setRole(stage);
			metadata.setVersion(updatedVersion);

			// Update the object in storage using updateObject (handles delete+create for Apicurio)
			metadata = getPromiseValue(storageService.updateObject(objectId, updatedObject, metadata));
			return metadata;
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
			requireTrue(isStageAllowed(stage), String.format("Stage %s is not supported from WorkflowService", stage));

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
		requireTrue(isStageAllowed(stage), String.format("Stage %s is not supported from WorkflowService", stage));
		if(stage.equals(config.final_stage())) return listInFinalStage();		
		try {
			return requireNonNullElse(registryService.findByScopeAndRole(config.scope(), stage), List.of());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
			try {
				EObjectStorageService<T> storageService = getStorageByStage(stage);
				if(storageService == null) {
					logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Cannot list objects.", stage));
					return List.of();
				}
				return requireNonNullElse(getPromiseValue(storageService.queryObjects(createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__ROLE, stage, ManagementPackage.Literals.OBJECT_QUERY__SCOPE, config.scope())))), List.of());
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Error listing objects, returning empty list", ex);
				return List.of();
			}
		}
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageByName(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStageByName(String stage, String name) {
		requireTrue(isStageAllowed(stage), String.format("Stage %s is not supported from WorkflowService", stage));
		try {
			return requireNonNullElse(registryService.findByScopeRoleAndName(config.scope(), stage, name), List.of());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
			try {
				EObjectStorageService<T> storageService = getStorageByStage(stage);
				if(storageService == null) {
					logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Cannot list objects.", stage));
					return List.of();
				}
				return requireNonNullElse(getPromiseValue(storageService.queryObjects(createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__ROLE, stage, ManagementPackage.Literals.OBJECT_QUERY__SCOPE, config.scope(), ManagementPackage.Literals.OBJECT_QUERY__NAME, name)))), List.of());
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Error listing objects, returning empty list", ex);
				return List.of();
			}
		}
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInFinalStage()
	 */
	@Override
	public List<ObjectMetadata> listInFinalStage() {
		List<ObjectMetadata> metadata = new LinkedList<>();
		try {			
			 List<ObjectMetadata> localMetadata = requireNonNullElse(registryService.findByScopeAndRole(config.scope(), config.final_stage()), List.of());
			 metadata.addAll(localMetadata);			 
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
			try {
				EObjectStorageService<T> storageService = getStorageByStage(config.final_stage());
				if(storageService == null) {
					logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Cannot list objects.", config.final_stage()));		
					return Collections.emptyList();
				}
				List<ObjectMetadata> localMetadata =  requireNonNullElse(getPromiseValue(storageService.queryObjects(createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__ROLE, config.final_stage(), ManagementPackage.Literals.OBJECT_QUERY__SCOPE, config.scope())))), List.of());
				metadata.addAll(localMetadata);			
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Error listing objects, returning empty list", ex);
				return Collections.emptyList();
			}
		}
		if(parentWorkflowService != null) {
			metadata.addAll(parentWorkflowService.listInFinalStage());
		}		
		return metadata;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#transitionToStage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata transitionToStage(String objectId, String fromStage, String toStage) {
		// Validate transition is allowed
		if(!isTransitionAllowed(fromStage, toStage)) {
			throw new IllegalStateException(String.format("Transition is not allowed for object %s from stage %s to stage %s", objectId, fromStage, toStage));
		}

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
		metadata.setLastChangeTime(Instant.now());
		metadata.setRole(toStage);

		// Store in target stage
		EObjectStorageService<T> targetStorage = getStorageByStage(toStage);
		if(targetStorage == null) {
			logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Cannot move object.", toStage));
			return null;
		}
		
		// Delete from source stage (if configured). If the registry is shared though, this will cause to remove also the newly created metadata,
		// so we have to do it before storing the object in the target stage
		if (config.delete_after_transition()) {
			getPromiseValue(sourceStorage.deleteObject(objectId));
		}
		
		getPromiseValue(targetStorage.storeObject(objectId, object, metadata));	

		return metadata;

	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService#isTransitionAllowed(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isTransitionAllowed(String fromStage, String toStage) {
		if(!isStageAllowed(fromStage) || !isStageAllowed(toStage) || !isStageWritable(fromStage) || !isStageWritable(toStage) || !areStagesSubsequent(fromStage, toStage)) return false;
		return true;
	}

	// Helper methods
	
	private static void requireTrue(boolean value, String message) {
		if(value) return;
		throw new IllegalStateException(message);
	}
	
	private boolean areStagesSubsequent(String fromStage, String toStage) {
		int fromIndex = List.of(config.stages()).indexOf(fromStage);
		int toIndex = List.of(config.stages()).indexOf(toStage);
		return (toIndex - fromIndex) == 1;
	}
	
	private boolean isStageAllowed(String stage) {
		return List.of(config.stages()).contains(stage);
	}
	
	private boolean isStageWritable(String stage) {
		return List.of(config.writable_stages()).contains(stage);
	}

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




	private ObjectQuery createQuery(Map<EStructuralFeature, Object> queryValueMap) {
		ObjectQuery query = managementFactory.createObjectQuery();
		queryValueMap.forEach((k,v) -> {
			query.eSet(k, v);
		});
		return query;
	}

	

	

	
}