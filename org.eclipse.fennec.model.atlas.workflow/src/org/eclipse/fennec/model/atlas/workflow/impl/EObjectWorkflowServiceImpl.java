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
import org.eclipse.fennec.model.atlas.mgmt.annotations.RequireEObjectRegistry;
import org.eclipse.fennec.model.atlas.mgmt.annotations.RequireEObjectStorage;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.collector.EObjectStorageServiceCollector;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
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
		WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageAllowed(config,config.final_stage()), String.format("Final Stage %s should also be part of the stages config property", config.final_stage()));
		for(String writableStage : config.writable_stages()) WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageAllowed(config,writableStage), String.format("Writable Stage %s should also be part of the stages config property", writableStage));

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
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#uploadToStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	public Promise<ObjectMetadata> uploadToStageForRegistry(String stage, String registry, T object,
			ObjectMetadata metadata) {
		return promiseFactory.submit(() -> {
			requireNonNull(object, "Object cannot be null");
			requireNonNull(metadata, "Metadata cannot be null");
			validateInput(stage, registry);

			metadata.setLastChangeTime(Instant.now());
			metadata.setStage(stage);
			metadata.setRegistry(registry);
			metadata.setScope(config.scope());

			requireNonNull(metadata.getObjectName());

			EObjectStorageService<T> storageService = getStorageService(stage, registry);
			if(storageService == null) {
				logger.severe(String.format("Cannot retrieve EObjectStorageService for %s. Object %s cannot be saved", stage, metadata.getObjectId()));
				return null;
			}
			ObjectMetadata objectMetadata = WorkflowServiceHelper.getPromiseValue(storageService.storeObject(config.scope(), registry, stage, metadata.getObjectId(), object, metadata));
			return objectMetadata;
		});
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getFromStageForRegistry(String stage, String registry, String objectId) {
		requireNonNull(objectId, "Object ID cannot be null");
		validateInput(stage, registry);
		EObjectStorageService<T> storageService = getStorageService(stage, registry);
		if(storageService != null) {
			ObjectMetadata localMetadata = WorkflowServiceHelper.getPromiseValue(storageService.retrieveMetadata(config.scope(), registry, stage, objectId));
			if(localMetadata == null && parentWorkflowService != null) {
				logger.warning(String.format("Object %s not found for scope '%s', registry '%s' and stage '%s'. Looking in the parent scope '%s', registry '%s' and final stage", objectId, config.scope(), registry, stage, config.parent_scope(), registry));
				//				we might want to set a read-only flag on the parent metadata here
				ObjectMetadata parentMetadata = parentWorkflowService.getFromFinalStageForRegistry(registry, objectId);
				if(parentMetadata != null) parentMetadata.setIsReadOnly(true);
				return parentMetadata;
			} else if(localMetadata == null) {
				return localMetadata;
			}
			//			if stage is not writable we might want to set a read-only flag (?)
			if(!WorkflowServiceHelper.isStageWritable(config,stage)) localMetadata.setIsReadOnly(true);
			return localMetadata;
		}
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromFinalStageForRegistry(java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getFromFinalStageForRegistry(String registry, String objectId) {
		return getFromStageForRegistry(config.final_stage(), registry, objectId);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getContentFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public T getContentFromStageForRegistry(String stage, String registry, String objectId) {
		requireNonNull(objectId, "Object ID cannot be null");
		validateInput(stage, registry);
		EObjectStorageService<T> storageService = getStorageService(stage, registry);
		if(storageService != null) return WorkflowServiceHelper.getPromiseValue(storageService.retrieveObject(config.scope(), registry, stage, objectId));
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#updateInStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<ObjectMetadata> updateInStageForRegistry(String stage, String registry, T updatedObject,
			String objectId, String updatedVersion) {
		return promiseFactory.submit(() -> {
			requireNonNull(objectId, "Object ID cannot be null");
			requireNonNull(updatedObject, "Updated object cannot be null");
			validateInput(stage, registry);
			WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageWritable(config,stage), String.format("Stage %s is not writable for this WorkflowService", stage));

			EObjectStorageService<T> storageService = getStorageService(stage, registry);
			if(storageService == null) {
				logger.severe(String.format("Cannot retrieve EObjectStorageService for scope '%s', registry '%s' and stage '%s'. Object %s cannot be updated", config.scope(), registry, stage, objectId));
				return null;
			}

			// Get current metadata
			ObjectMetadata metadata = WorkflowServiceHelper.getPromiseValue(storageService.retrieveMetadata(config.scope(), registry, stage, objectId));		
			metadata.setLastChangeTime(Instant.now());
			metadata.setStage(stage);
			metadata.setRegistry(registry);
			metadata.setVersion(updatedVersion);

			// Update the object in draft storage
			metadata = WorkflowServiceHelper.getPromiseValue(storageService.storeObject(config.scope(), registry, stage, objectId, updatedObject, metadata));
			return metadata;
		});
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#deleteFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<Boolean> deleteFromStageForRegistry(String stage, String registry, String objectId) {
		return promiseFactory.submit(() -> {
			requireNonNull(objectId, "Object ID cannot be null");
			validateInput(stage, registry);

			EObjectStorageService<T> storageService = getStorageService(stage, registry);
			if(storageService == null) {
				logger.severe(String.format("Cannot retrieve EObjectStorageService for scope '%s', registry '%s' and stage '%s'. Object %s cannot be deleted", config.scope(), registry, stage, objectId));
				return false;
			}

			// Verify it exists
			ObjectMetadata metadata = WorkflowServiceHelper.getPromiseValue(storageService.retrieveMetadata(config.scope(), registry, stage, objectId));
			if(metadata == null) {
				throw new IllegalStateException(String.format("Cannot delete object %s for scope '%s', registry '%s' and stage '%s' because no metadata has been found for it", objectId, config.scope(), registry, stage));
			}

			// Delete from draft storage
			boolean deleted = WorkflowServiceHelper.getPromiseValue(storageService.deleteObject(config.scope(), registry, stage, objectId));

			// Remove from registry
			if (deleted) {
				registryService.removeFromCache(objectId);
			}

			return deleted;
		});
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageForRegistry(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStageForRegistry(String stage, String registry) {
		validateInput(stage, registry);
		if(stage.equals(config.final_stage())) return listInFinalStageForRegistry(registry);		
		try {
			return requireNonNullElse(registryService.findByScopeRegistryAndStage(config.scope(), registry, stage), List.of());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
			try {
				EObjectStorageService<T> storageService = getStorageService(stage, registry);
				if(storageService == null) {
					logger.severe(String.format("Cannot retrieve EObjectStorageService for scope '%s', registry '%s' and stage '%s'. Cannot list objects.", config.scope(), registry, stage));
					return List.of();
				}
				return requireNonNullElse(WorkflowServiceHelper.getPromiseValue(storageService.queryObjects(WorkflowServiceHelper.createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__STAGE, stage, ManagementPackage.Literals.OBJECT_QUERY__SCOPE, config.scope(), ManagementPackage.Literals.OBJECT_QUERY__REGISTRY, registry)))), List.of());
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Error listing objects, returning empty list", ex);
				return List.of();
			}
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageForRegistryByName(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStageForRegistryByName(String stage, String registry, String name) {
		validateInput(stage, registry);
		try {
			return requireNonNullElse(registryService.findByScopeRegistryStageAndName(config.scope(), registry, stage, name), List.of());
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
			try {
				EObjectStorageService<T> storageService = getStorageService(stage, registry);
				if(storageService == null) {
					logger.severe(String.format("Cannot retrieve EObjectStorageService for scope '%s', registry '%s' and stage '%s'. Cannot list objects.", config.scope(), registry, stage));
					return List.of();
				}
				return requireNonNullElse(WorkflowServiceHelper.getPromiseValue(storageService.queryObjects(WorkflowServiceHelper.createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__STAGE, stage, ManagementPackage.Literals.OBJECT_QUERY__SCOPE, config.scope(), ManagementPackage.Literals.OBJECT_QUERY__REGISTRY, registry, ManagementPackage.Literals.OBJECT_QUERY__NAME, name)))), List.of());
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Error listing objects, returning empty list", ex);
				return List.of();
			}
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInFinalStageForRegistry(java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInFinalStageForRegistry(String registry) {
		WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isRegistryAllowed(config,registry), String.format("Registry %s is not supported from WorkflowService", registry));
		List<ObjectMetadata> metadata = new LinkedList<>();
		try {			
			List<ObjectMetadata> localMetadata = requireNonNullElse(registryService.findByScopeRegistryAndStage(config.scope(), registry, config.final_stage()), List.of());
			metadata.addAll(localMetadata);			 
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
			try {
				EObjectStorageService<T> storageService = getStorageService(config.final_stage(), registry);
				if(storageService == null) {
					logger.severe(String.format("Cannot retrieve EObjectStorageService for scope '%s', registry '%s' and stage '%s'. Cannot list objects.", config.scope(), registry, config.final_stage()));		
					return Collections.emptyList();
				}
				List<ObjectMetadata> localMetadata =  requireNonNullElse(WorkflowServiceHelper.getPromiseValue(storageService.queryObjects(WorkflowServiceHelper.createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__STAGE, config.final_stage(), ManagementPackage.Literals.OBJECT_QUERY__SCOPE, config.scope(), ManagementPackage.Literals.OBJECT_QUERY__REGISTRY, registry)))), List.of());
				metadata.addAll(localMetadata);			
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Error listing objects, returning empty list", ex);
				return Collections.emptyList();
			}
		}
		if(parentWorkflowService != null) {
			metadata.addAll(parentWorkflowService.listInFinalStageForRegistry(registry));
		}		
		return metadata;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#transitionToStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata transitionToStageForRegistry(String objectId, String fromStage, String toStage,
			String registry) {
		// Validate transition is allowed
		if(!isTransitionAllowed(fromStage, toStage)) {
			throw new IllegalStateException(String.format("Transition is not allowed for object %s from stage %s to stage %s", objectId, fromStage, toStage));
		}

		// Get object from source stage
		EObjectStorageService<T> sourceStorage = getStorageService(fromStage, registry);
		if(sourceStorage == null) {
			logger.severe(String.format("Cannot retrieve EObjectStorageService for scope '%s', registry '%s' and stage '%s'. Cannot move object.", config.scope(), registry, fromStage));
			return null;
		}
		//        String sourceId = buildObjectId(fromStage, nsUri);

		T object =  WorkflowServiceHelper.getPromiseValue(sourceStorage.retrieveObject(config.scope(), registry, fromStage, objectId));
		ObjectMetadata metadata = WorkflowServiceHelper.getPromiseValue(sourceStorage.retrieveMetadata(config.scope(), registry, fromStage, objectId));

		if (object == null || metadata == null) {
			throw new IllegalArgumentException("Object not found in stage " + fromStage + ": " + objectId);
		}

		// Update metadata for new stage
		metadata.setLastChangeTime(Instant.now());
		metadata.setStage(toStage);

		// Store in target stage
		EObjectStorageService<T> targetStorage = getStorageService(toStage, registry);
		if(targetStorage == null) {
			logger.severe(String.format("Cannot retrieve EObjectStorageService for scope '%s', registry '%s' and stage '%s'. Cannot move object.", config.scope(), registry, toStage));
			return null;
		}

		// Delete from source stage (if configured). If the registry is shared though, this will cause to remove also the newly created metadata,
		// so we have to do it before storing the object in the target stage
		if (config.delete_after_transition()) {
			WorkflowServiceHelper.getPromiseValue(sourceStorage.deleteObject(config.scope(), registry, fromStage, objectId));
		}

		WorkflowServiceHelper.getPromiseValue(targetStorage.storeObject(config.scope(), registry, toStage, objectId, object, metadata));	

		return metadata;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService#isTransitionAllowed(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isTransitionAllowed(String fromStage, String toStage) {
		if(!WorkflowServiceHelper.isStageAllowed(config, fromStage) || !WorkflowServiceHelper.isStageAllowed(config, toStage) || !WorkflowServiceHelper.isStageWritable(config,fromStage) || !WorkflowServiceHelper.isStageWritable(config,toStage) || !WorkflowServiceHelper.areStagesSubsequent(config,fromStage, toStage)) return false;
		return true;
	}

	// Helper methods

	@SuppressWarnings("unchecked")
	private EObjectStorageService<T> getStorageService(String stage, String registry) {
		EObjectStorageService<T> storageService = (EObjectStorageService<T>) storageServiceCollector.getStorage(config.scope(), registry, stage);
		if(storageService == null) {
			logger.severe(String.format("Cannot retrieve EObjectStorageService for scope '%s', registry '%s' and stage '%s'", config.scope(), registry, stage));
			return null;
		}
		return storageService;
	}


	private void validateInput(String stage, String registry) {
		WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageAllowed(config,stage), String.format("Stage %s is not supported from WorkflowService", stage));
		WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isRegistryAllowed(config,registry), String.format("Registry %s is not supported from WorkflowService", registry));
	}




}