/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.impl;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Stage;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * 
 * @author ilenia
 * @since Jan 13, 2026
 */
@Component(name = "RegistryService", configurationPid = "RegistryService", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = RegistryServiceConfig.class)
public class RegistryServiceImpl<T extends EObject> implements RegistryService<T> {

	@Reference
	private EObjectRegistryService<T> registryService;

	private static final Logger LOGGER = Logger.getLogger(RegistryServiceImpl.class.getName());

	//	private List<StageService> stageService;
	private RegistryServiceConfig config;
	private final Map<String, Set<String>> transitionsMap;
	private final Map<String, EObjectStorageService<T>> storageMap;
	private final List<Stage> stages;
	private final PromiseFactory promiseFactory = new PromiseFactory(null);


	@Activate
	public RegistryServiceImpl(
			@Reference(name = "storageService") List<EObjectStorageService<T>> storageService,
			RegistryServiceConfig config) {		
		this.config = config;
		this.transitionsMap = parseTransitionsMap(config.workflow_transitions());
		this.storageMap = parseStageStorageMappings(config.stage_storage_mappings(), storageService);
		this.stages = parseStages(config.stages());
		validateStages();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#uploadToStage(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	public Promise<ObjectMetadata> uploadToStage(String scope, String stage, T object, ObjectMetadata metadata) {

		return promiseFactory.submit(() -> {
			requireNonNull(object, "Object cannot be null");
			requireNonNull(metadata, "Metadata cannot be null");
			validateStage(stage);

			metadata.setLastChangeTime(Instant.now());
			metadata.setStage(stage);
			metadata.setRegistry(config.registry_name());
			metadata.setScope(scope);

			requireNonNull(metadata.getObjectName());

			EObjectStorageService<T> storageService = storageMap.get(stage);
			ObjectMetadata objectMetadata = WorkflowServiceHelper.getPromiseValue(storageService.storeObject(scope, config.registry_name(), stage, metadata.getObjectId(), object, metadata));
			return objectMetadata;
		});
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getMetadataFromStage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getMetadataFromStage(String scope, String stage, String objectId) {
		requireNonNull(objectId, "Object ID cannot be null");
		validateStage(stage);
		EObjectStorageService<T> storageService = storageMap.get(stage);
		ObjectMetadata metadata = WorkflowServiceHelper.getPromiseValue(storageService.retrieveMetadata(scope, config.registry_name(), stage, objectId));
		if(!isWritableStage(stage)) metadata.setIsReadOnly(true);
		return metadata;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getMetadataFromFinalStage(java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getMetadataFromFinalStage(String scope, String objectId) {
		Stage finalStage = stages.stream().filter(s -> s.isFinal()).findFirst().get();
		return getMetadataFromStage(scope, finalStage.getName(), objectId);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getContentFromStage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public T getContentFromStage(String scope, String stage, String objectId) {
		requireNonNull(objectId, "Object ID cannot be null");
		validateStage(stage);
		EObjectStorageService<T> storageService = storageMap.get(stage);
		return WorkflowServiceHelper.getPromiseValue(storageService.retrieveObject(scope, config.registry_name(), stage, objectId));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#updateInStage(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<ObjectMetadata> updateInStage(String scope, String stage, T updatedObject, String objectId,
			String version) {

		return promiseFactory.submit(() -> {
			requireNonNull(objectId, "Object ID cannot be null");
			requireNonNull(updatedObject, "Updated object cannot be null");
			validateWritableStage(stage);

			EObjectStorageService<T> storageService = storageMap.get(stage);


			// Get current metadata
			ObjectMetadata metadata = WorkflowServiceHelper.getPromiseValue(storageService.retrieveMetadata(scope, config.registry_name(), stage, objectId));		
			metadata.setLastChangeTime(Instant.now());
			metadata.setStage(stage);
			metadata.setScope(scope);
			metadata.setRegistry(config.registry_name());
			metadata.setVersion(version);

			// Update the object in draft storage
			metadata = WorkflowServiceHelper.getPromiseValue(storageService.storeObject(scope, config.registry_name(), stage, objectId, updatedObject, metadata));
			return metadata;
		});
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#deleteFromStage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<Boolean> deleteFromStage(String scope, String stage, String objectId) {


		return promiseFactory.submit(() -> {
			requireNonNull(objectId, "Object ID cannot be null");
			validateWritableStage(stage);

			EObjectStorageService<T> storageService = storageMap.get(stage);

			// Verify it exists
			ObjectMetadata metadata = WorkflowServiceHelper.getPromiseValue(storageService.retrieveMetadata(scope, config.registry_name(), stage, objectId));
			if(metadata == null) {
				throw new IllegalStateException(String.format("Cannot delete object %s for scope '%s', registry '%s' and stage '%s' because no metadata has been found for it", objectId, scope, config.registry_name(), stage));
			}

			// Delete from draft storage
			boolean deleted = WorkflowServiceHelper.getPromiseValue(storageService.deleteObject(scope, config.registry_name(), stage, objectId));

			// Remove from registry
			if (deleted) {
				registryService.removeFromCache(objectId);
			}

			return deleted;
		});
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInStage(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStage(String scope, String stage) {
		validateStage(stage);
		if(stages.stream().filter(s -> stage.equals(s.getName()) && s.isFinal()).findFirst().orElse(null) != null) return listInFinalStage(scope);		
		try {
			return requireNonNullElse(registryService.findByScopeRegistryAndStage(scope, config.registry_name(), stage), List.of());
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
			EObjectStorageService<T> storageService = storageMap.get(stage);
			return requireNonNullElse(WorkflowServiceHelper.getPromiseValue(storageService.queryObjects(WorkflowServiceHelper.createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__STAGE, stage, ManagementPackage.Literals.OBJECT_QUERY__SCOPE, scope, ManagementPackage.Literals.OBJECT_QUERY__REGISTRY, config.registry_name())))), List.of());
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInStageByName(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStageByName(String scope, String stage, String name) {
		validateStage(stage);
		try {
			return requireNonNullElse(registryService.findByScopeRegistryStageAndName(scope, config.registry_name(), stage, name), List.of());
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
			EObjectStorageService<T> storageService = storageMap.get(stage);				
			return requireNonNullElse(WorkflowServiceHelper.getPromiseValue(storageService.queryObjects(WorkflowServiceHelper.createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__STAGE, stage, ManagementPackage.Literals.OBJECT_QUERY__SCOPE, scope, ManagementPackage.Literals.OBJECT_QUERY__REGISTRY, config.registry_name(), ManagementPackage.Literals.OBJECT_QUERY__NAME, name)))), List.of());
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInFinalStage(java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInFinalStage(String scope) {
		List<ObjectMetadata> metadata = new LinkedList<>();
		Stage finalStage = stages.stream().filter(s -> s.isFinal()).findFirst().get();
		try {			
			List<ObjectMetadata> localMetadata = requireNonNullElse(registryService.findByScopeRegistryAndStage(scope, config.registry_name(), finalStage.getName()), List.of());
			metadata.addAll(localMetadata);			 
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
			EObjectStorageService<T> storageService = storageMap.get(finalStage.getName());
			List<ObjectMetadata> localMetadata =  requireNonNullElse(WorkflowServiceHelper.getPromiseValue(storageService.queryObjects(WorkflowServiceHelper.createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__STAGE, finalStage.getName(), ManagementPackage.Literals.OBJECT_QUERY__SCOPE, scope, ManagementPackage.Literals.OBJECT_QUERY__REGISTRY, config.registry_name())))), List.of());
			metadata.addAll(localMetadata);			
		}
		return metadata;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#transitionToStage(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata transitionToStage(String scope, String objectId, String fromStage, String toStage) {
		validateTransition(fromStage, toStage);
		EObjectStorageService<T> sourceStorage = storageMap.get(fromStage);
		T object =  WorkflowServiceHelper.getPromiseValue(sourceStorage.retrieveObject(scope, config.registry_name(), fromStage, objectId));
		ObjectMetadata metadata = WorkflowServiceHelper.getPromiseValue(sourceStorage.retrieveMetadata(scope, config.registry_name(), fromStage, objectId));

		if (object == null || metadata == null) {
			throw new IllegalArgumentException("Object not found in stage " + fromStage + ": " + objectId);
		}
		// Update metadata for new stage
		metadata.setLastChangeTime(Instant.now());
		metadata.setStage(toStage);

		// Store in target stage
		EObjectStorageService<T> targetStorage = storageMap.get(toStage);

		// Delete from source stage (if configured). If the registry is shared though, this will cause to remove also the newly created metadata,
		// so we have to do it before storing the object in the target stage
		if (config.delete_after_transition()) {
			WorkflowServiceHelper.getPromiseValue(sourceStorage.deleteObject(scope, config.registry_name(), fromStage, objectId));
		}
		WorkflowServiceHelper.getPromiseValue(targetStorage.storeObject(scope, config.registry_name(), toStage, objectId, object, metadata));	
		return metadata;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRegistryName()
	 */
	@Override
	public String getRegistryName() {
		return config.registry_name();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isValidStage(java.lang.String)
	 */
	@Override
	public boolean isValidStage(String stageName) {
		return stages.stream().filter(s -> stageName.equals(s.getName())).findAny().isPresent();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isWritableStage(java.lang.String)
	 */
	@Override
	public boolean isWritableStage(String stageName) {
		return stages.stream().filter(s -> stageName.equals(s.getName()) && s.isWritable()).findAny().isPresent();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isFinalStageWritable()
	 */
	@Override
	public boolean isFinalStageWritable() {
		return stages.stream().filter(s -> s.isFinal() && s.isWritable()).findAny().isPresent();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isTransitionAllowed(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isTransitionAllowed(String fromStage, String toStage) {
		if(!transitionsMap.containsKey(fromStage)) return false;
		return transitionsMap.get(fromStage).contains(toStage);
	}

	private Map<String, Set<String>> parseTransitionsMap(String[] workflow_transitions) {
		Map<String, Set<String>> transitionsMap = new HashMap<>();
		for(String transition : workflow_transitions) {
			String[] transitionSplit = transition.split(":");
			if(transitionSplit.length != 2) {
				throw new IllegalArgumentException(String.format("Transition property %s is not properly formatted. Expected format 'fromStage:toStage'", transition));
			}
			if(!transitionsMap.containsKey(transitionSplit[0])) {
				transitionsMap.put(transitionSplit[0], new HashSet<String>());
			}
			transitionsMap.get(transitionSplit[0]).add(transitionSplit[1]);
		}
		return transitionsMap;
	}

	private Map<String, EObjectStorageService<T>> parseStageStorageMappings(
			String[] mappings,
			List<EObjectStorageService<T>> storageServices) {
		Map<String, EObjectStorageService<T>> map = new HashMap<>();

		//		 Build storageType -> service lookup
		Map<String, EObjectStorageService<T>> storageByType = storageServices.stream()
				.collect(Collectors.toMap(
						s -> s.getStorageType(),  
						Function.identity()
						));

		// Parse stage:storageType mappings
		for (String mapping : mappings) {
			String[] parts = mapping.split(":");
			if(parts.length != 2) {
				throw new IllegalArgumentException(String.format("Storage mapping property %s is not properly formatted. Expected format 'stage:storageType'", mapping));
			}
			String stageName = parts[0].trim();
			String storageType = parts[1].trim();
			EObjectStorageService<T> storage = storageByType.get(storageType);
			if (storage != null) {
				map.put(stageName, storage);
			}
		}
		return map;
	}

	private List<Stage> parseStages(String[] stages) {		
		List<Stage> stageServices = new ArrayList<>(stages.length);
		for(String stage : stages) {
			ObjectMapper mapper = new ObjectMapper();

			Map<String, Object> map = mapper.readValue(stage, new TypeReference<Map<String, Object>>(){});

			Stage stageService = WorkflowApiFactory.eINSTANCE.createStage();
			stageService.setName((String) map.get("name"));
			stageService.setWritable((boolean) map.get("writable"));
			stageService.setFinal((boolean) map.get("final"));
			stageServices.add(stageService);
		}
		return stageServices;
	}

	private void validateStages() {
		if(stages.stream().filter(s -> s.isFinal()).count() != 1) {
			throw new IllegalArgumentException("Exactly 1 final stage must be provided!");
		}
	}


	private void validateStage(String stageName) {
		if(stageName == null) {
			throw new IllegalArgumentException(String.format("Satge name cannot be null!"));
		}
		if(!isValidStage(stageName)) {
			throw new IllegalArgumentException(String.format("Stage %s is not a valid stage for the registry %s", stageName, config.registry_name()));
		}
		return;
	}

	private void validateWritableStage(String stageName) {
		validateStage(stageName);
		if(!isWritableStage(stageName)) {
			throw new IllegalArgumentException(String.format("Stage %s is not a writable stage for the registry %s", stageName, config.registry_name()));
		}
		return;
	}

	private void validateTransition(String fromStage, String toStage) {
		validateWritableStage(fromStage);
		validateWritableStage(toStage);
		if(!transitionsMap.get(fromStage).contains(toStage)) {
			throw new IllegalArgumentException(String.format("Transition from stage %s to stage %s is not allowed in registry %s", fromStage, toStage, config.registry_name()));
		}
	}
}
