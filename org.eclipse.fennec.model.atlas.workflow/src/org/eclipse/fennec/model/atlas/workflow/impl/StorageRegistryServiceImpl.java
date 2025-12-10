///**
// * Copyright (c) 2012 - 2025 Data In Motion and others.
// * All rights reserved. 
// * 
// * This program and the accompanying materials are made
// * available under the terms of the Eclipse Public License 2.0
// * which is available at https://www.eclipse.org/legal/epl-2.0/
// *
// * SPDX-License-Identifier: EPL-2.0
// * 
// * Contributors:
// *     Data In Motion - initial API and implementation
// */
//package org.eclipse.fennec.model.atlas.workflow.impl;
//
//import static java.util.Objects.requireNonNull;
//import static java.util.Objects.requireNonNullElse;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import org.eclipse.emf.ecore.EObject;
//import org.eclipse.fennec.model.atlas.mgmt.annotations.RequireEObjectRegistry;
//import org.eclipse.fennec.model.atlas.mgmt.annotations.RequireEObjectStorage;
//import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
//import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
//import org.eclipse.fennec.model.atlas.mgmt.collector.EObjectStorageServiceCollector;
//import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
//import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
//import org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService;
//import org.osgi.service.component.annotations.Activate;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.ConfigurationPolicy;
//import org.osgi.service.component.annotations.Reference;
//import org.osgi.service.component.annotations.ReferenceCardinality;
//import org.osgi.service.component.annotations.ServiceScope;
//import org.osgi.service.metatype.annotations.Designate;
//import org.osgi.util.promise.Promise;
//import org.osgi.util.promise.PromiseFactory;
//
///**
// * 
// * @author ilenia
// * @since Dec 9, 2025
// */
//@Component(
//		name = "StorageRegistryService",
//		configurationPolicy = ConfigurationPolicy.REQUIRE,
//		configurationPid = "StorageRegistryService",
//		service = EObjectWorkflowService.class,
//		scope = ServiceScope.PROTOTYPE,
//		property = {
//				"service.description=Enhanced EObject Storage Service with configurable storage providers",
//				"service.vendor=Data In Motion",
//				"type=objectRegistry"
//		}
//		)
//@RequireEObjectRegistry
//@RequireEObjectStorage
//@Designate(ocd = WorkflowServiceConfig.class, factory = true)
//public class StorageRegistryServiceImpl<T extends EObject> implements EObjectWorkflowService<T> {
//
//	private static final Logger LOGGER = Logger.getLogger(StorageRegistryServiceImpl.class.getName());
//	
//	private final PromiseFactory promiseFactory = new PromiseFactory(null);
//
//	private WorkflowServiceConfig config;
//	
//	@Reference
//	EObjectStorageServiceCollector storageServiceCollector;
//
//	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
//	EObjectWorkflowService<EObject> parentWorkflowService;
//
//	@Reference
//	private EObjectRegistryService<T> registryService;
//	
//	@Activate
//	void activate(WorkflowServiceConfig config) {
//		this.config = requireNonNull(config, "Configuration cannot be null");
//		for(String writableStage : config.writable_stages()) WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageAllowed(config,writableStage), String.format("Writable Stage %s should also be part of the stages config property", writableStage));
//		requireNonNull(registryService, "Registry service must be available");
//		LOGGER.info("Activated StorageRegistryService: " + config.workflow_id());
//	}
//	
//	/* 
//	 * (non-Javadoc)
//	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#uploadToStage(java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
//	 */
//	@Override
//	public Promise<ObjectMetadata> uploadToStage(String registryName, T object, ObjectMetadata metadata) {
//		return promiseFactory.submit(() -> {
//			requireNonNull(object, "Object cannot be null");
//			requireNonNull(metadata, "Metadata cannot be null");
//			WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageAllowed(config,registryName), String.format("Registry %s is not supported from StorageRegistryService", registryName));
//
//			metadata.setLastChangeTime(Instant.now());
//			metadata.setRole(registryName);
//			metadata.setScope(config.scope());
//
//			requireNonNull(metadata.getObjectName());
//
//			EObjectStorageService<T> storageService = getStorageByStage(registryName);
//			if(storageService == null) {
//				LOGGER.severe(String.format("Cannot retrieve EObjectStorageService for %s. Object %s cannot be saved", registryName, metadata.getObjectId()));
//				return null;
//			}
//			ObjectMetadata objectMetadata = WorkflowServiceHelper.getPromiseValue(storageService.storeObject(metadata.getObjectId(), object, metadata));
//			return objectMetadata;
//		});
//	}
//
//	/* 
//	 * (non-Javadoc)
//	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromStage(java.lang.String, java.lang.String)
//	 */
//	@Override
//	public ObjectMetadata getFromStage(String registryName, String objectId) {
//		requireNonNull(objectId, "Object ID cannot be null");
//		WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageAllowed(config,registryName), String.format("Registry %s is not supported from StorageRegistryService", registryName));
//		EObjectStorageService<T> storageService = getStorageByStage(registryName);
//		if(storageService != null) {
//			ObjectMetadata localMetadata = WorkflowServiceHelper.getPromiseValue(storageService.retrieveMetadata(objectId));
//			if(localMetadata == null && parentWorkflowService != null) {
//				LOGGER.warning(String.format("Object %s not found for scope %s. Looking in the parent scope %s registry", objectId, config.scope(), config.parent_scope()));
////				we might want to set a read-only flag on the parent metadata here
//				ObjectMetadata parentMetadata = parentWorkflowService.getFromStage(registryName, objectId);
//				if(parentMetadata != null) parentMetadata.setIsReadOnly(true);
//				return parentMetadata;
//			} else if(localMetadata == null) {
//				return localMetadata;
//			}
////			if stage is not writable we might want to set a read-only flag (?)
//			if(!WorkflowServiceHelper.isStageWritable(config,registryName)) localMetadata.setIsReadOnly(true);
//			return localMetadata;
//		}
//		return null;
//	}
//
//	/* 
//	 * (non-Javadoc)
//	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromFinalStage(java.lang.String)
//	 */
//	@Override
//	public ObjectMetadata getFromFinalStage(String objectId) {
//		throw new UnsupportedOperationException("Not implemented for this service. It should not be needed.");
//	}
//
//	/* 
//	 * (non-Javadoc)
//	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getContentFromStage(java.lang.String, java.lang.String)
//	 */
//	@Override
//	public T getContentFromStage(String registryName, String objectId) {
//		requireNonNull(objectId, "Object ID cannot be null");
//		WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageAllowed(config,registryName), String.format("Registry %s is not supported from StorageRegistryService", registryName));
//		EObjectStorageService<T> storageService = getStorageByStage(registryName);
//		if(storageService != null) return WorkflowServiceHelper.getPromiseValue(storageService.retrieveObject(objectId));
//		return null;
//	}
//
//	/* 
//	 * (non-Javadoc)
//	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#updateInStage(java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
//	 */
//	@Override
//	public Promise<ObjectMetadata> updateInStage(String registryName, T updatedObject, String objectId, String updatedVersion) {
//		return promiseFactory.submit(() -> {
//			requireNonNull(objectId, "Object ID cannot be null");
//			requireNonNull(updatedObject, "Updated object cannot be null");
//			WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageAllowed(config,registryName), String.format("Registry %s is not supported from StorageRegistryService", registryName));
//			WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageWritable(config,registryName), String.format("Registry %s is not writable for this StorageRegistryService", registryName));
//			
//			EObjectStorageService<T> storageService = getStorageByStage(registryName);
//			if(storageService == null) {
//				LOGGER.severe(String.format("Cannot retrieve EObjectStorageService for %s. Object %s cannot be updated", registryName, objectId));
//				return null;
//			}
//
//			// Get current metadata
//			ObjectMetadata metadata = WorkflowServiceHelper.getPromiseValue(storageService.retrieveMetadata(objectId));		
//			metadata.setLastChangeTime(Instant.now());
//			metadata.setRole(registryName);
//			metadata.setVersion(updatedVersion);
//			
//			// Update the object in draft storage
//			metadata = WorkflowServiceHelper.getPromiseValue(storageService.storeObject(objectId, updatedObject, metadata));
//			return metadata;
//		});
//	}
//
//	/* 
//	 * (non-Javadoc)
//	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#deleteFromStage(java.lang.String, java.lang.String)
//	 */
//	@Override
//	public Promise<Boolean> deleteFromStage(String registryName, String objectId) {
//		return promiseFactory.submit(() -> {
//			requireNonNull(objectId, "Object ID cannot be null");
//			WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageAllowed(config,registryName), String.format("Registry %s is not supported from StorageRegistryService", registryName));
//
//			EObjectStorageService<T> storageService = getStorageByStage(registryName);
//			if(storageService == null) {
//				LOGGER.severe(String.format("Cannot retrieve EObjectStorageService for %s. Object %s cannot be deleted", registryName, objectId));
//				return false;
//			}
//
//			// Verify it exists
//			ObjectMetadata metadata = WorkflowServiceHelper.getPromiseValue(storageService.retrieveMetadata(objectId));
//			if(metadata == null) {
//				throw new IllegalStateException(String.format("Cannot delete object %s because no metadata has been found for it", objectId));
//			}
//
//			// Delete from draft storage
//			boolean deleted = WorkflowServiceHelper.getPromiseValue(storageService.deleteObject(objectId));
//
//			// Remove from registry
//			if (deleted) {
//				registryService.removeFromCache(objectId);
//			}
//
//			return deleted;
//		});
//	}
//
//	/* 
//	 * (non-Javadoc)
//	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStage(java.lang.String)
//	 */
//	@Override
//	public List<ObjectMetadata> listInStage(String registryName) {
//		WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageAllowed(config,registryName), String.format("Registry %s is not supported from StorageRegistryService", registryName));
//		try {
//			return requireNonNullElse(registryService.findByScopeAndRole(config.scope(), registryName), List.of());
//		} catch (Exception e) {
//			LOGGER.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
//			try {
//				EObjectStorageService<T> storageService = getStorageByStage(registryName);
//				if(storageService == null) {
//					LOGGER.severe(String.format("Cannot retrieve EObjectStorageService for %s. Cannot list objects.", registryName));
//					return List.of();
//				}
//				return requireNonNullElse(WorkflowServiceHelper.getPromiseValue(storageService.queryObjects(WorkflowServiceHelper.createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__ROLE, registryName, ManagementPackage.Literals.OBJECT_QUERY__SCOPE, config.scope())))), List.of());
//			} catch (Exception ex) {
//				LOGGER.log(Level.WARNING, "Error listing objects, returning empty list", ex);
//				return List.of();
//			}
//		}
//	}
//
//	/* 
//	 * (non-Javadoc)
//	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageByName(java.lang.String, java.lang.String)
//	 */
//	@Override
//	public List<ObjectMetadata> listInStageByName(String registryName, String name) {
//		WorkflowServiceHelper.requireTrue(WorkflowServiceHelper.isStageAllowed(config,registryName), String.format("Registry %s is not supported from StorageRegistryService", registryName));
//		try {
//			return requireNonNullElse(registryService.findByScopeRoleAndName(config.scope(), registryName, name), List.of());
//		} catch (Exception e) {
//			LOGGER.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
//			try {
//				EObjectStorageService<T> storageService = getStorageByStage(registryName);
//				if(storageService == null) {
//					LOGGER.severe(String.format("Cannot retrieve EObjectStorageService for %s. Cannot list objects.", registryName));
//					return List.of();
//				}
//				return requireNonNullElse(WorkflowServiceHelper.getPromiseValue(storageService.queryObjects(WorkflowServiceHelper.createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__ROLE, registryName, ManagementPackage.Literals.OBJECT_QUERY__SCOPE, config.scope(), ManagementPackage.Literals.OBJECT_QUERY__NAME, name)))), List.of());
//			} catch (Exception ex) {
//				LOGGER.log(Level.WARNING, "Error listing objects, returning empty list", ex);
//				return List.of();
//			}
//		}
//	}
//
//	/* 
//	 * (non-Javadoc)
//	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInFinalStage()
//	 */
//	@Override
//	public List<ObjectMetadata> listInFinalStage() {
//		throw new UnsupportedOperationException("Not implemented for this service. It should not be needed.");
//	}
//
//	/* 
//	 * (non-Javadoc)
//	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#transitionToStage(java.lang.String, java.lang.String, java.lang.String)
//	 */
//	@Override
//	public ObjectMetadata transitionToStage(String objectId, String fromStage, String toStage) {
//		throw new UnsupportedOperationException("Not implemented for this service. It should not be needed.");
//	}
//
//	/* 
//	 * (non-Javadoc)
//	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#isTransitionAllowed(java.lang.String, java.lang.String)
//	 */
//	@Override
//	public boolean isTransitionAllowed(String fromStage, String toStage) {
//		throw new UnsupportedOperationException("Not implemented for this service. It should not be needed.");
//	}
//
//
//	
//	@SuppressWarnings("unchecked")
//	private EObjectStorageService<T> getStorageByStage(String stage) {
//		EObjectStorageService<T> storageService = (EObjectStorageService<T>) storageServiceCollector.getStorage(config.scope(), stage);
//		if(storageService == null) {
//			LOGGER.severe(String.format("Cannot retrieve EObjectStorageService for %s", stage));
//			return null;
//		}
//		return storageService;
//	}
//	
//
//}
