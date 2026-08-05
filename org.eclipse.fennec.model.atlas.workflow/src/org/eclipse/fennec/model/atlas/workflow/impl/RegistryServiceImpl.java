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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeApiFactory;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.eclipse.fennec.model.atlas.workflow.ActionContext;
import org.eclipse.fennec.model.atlas.workflow.StageActionService;
import org.eclipse.fennec.model.atlas.workflow.StageActionService.ActionEvent;
import org.eclipse.fennec.model.atlas.workflow.StageActionService.ExitReason;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
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

    private RegistryServiceConfig config;
    private final List<StageTransition> allowedTransitionsList;
    private final Map<String, EObjectStorageService<T>> storageMap;
    private final List<StageInfo> stages;
    private final Registry registryObject;
    private final ExecutorService promiseExecutor = Executors.newCachedThreadPool();
    private final PromiseFactory promiseFactory = new PromiseFactory(promiseExecutor);

    @Deactivate
    void deactivate() {
        promiseExecutor.shutdown();
    }
    private final EClass rootEClass;

	private List<StageActionService> stageActionService;

    @Activate
    public RegistryServiceImpl(@Reference(name = "storageService", target = ("(scope=no-inject)")) List<EObjectStorageService<T>> storageService,
            @Reference(name = "resourceSet") ResourceSet resourceSet,
            @Reference(name = "stageActionService", target = ("(scope=no-inject)")) List<StageActionService> stageActionService,
            RegistryServiceConfig config) {
        this.stageActionService = stageActionService;
        this.config = config;
        this.allowedTransitionsList = parseTransitionsIntoList(config.workflow_transitions());
        this.storageMap = parseStageStorageMappings(config.stage_storage_mappings(), storageService);
        this.stages = parseStages(config.stages());
        validateStages();
        this.registryObject = createRegistryObject();
        EObject eObject = resourceSet.getEObject(URI.createURI(config.root_eclass_uri()), false);
        if (eObject != null && eObject instanceof EClass eClass) {
            rootEClass = eClass;
        } else {
            throw new IllegalArgumentException(String.format(
                    "The provided root.eclass.uri %s does not match to any known EClass", config.root_eclass_uri()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#activate(java.
     * lang.String)
     */
    @Override
    public Void activate(String scope) {
        stageActionService.forEach(sas -> {
            if (!sas.requiresReplayOnStartup()) {
                return;
            }
            sas.getTriggerStages().forEach(stage -> listInStage(scope, stage).forEach(m -> dispatch(ActionEvent.ENTER,
                    newContext(scope, stage, m, null, null, null, "startup replay", true))));
        });
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#deactivate(java
     * .lang.String)
     */
    @Override
    public Void deactivate(String scope) {
        stageActionService.forEach(sas -> {
            if (!sas.requiresReplayOnShutdown()) {
                return;
            }
            sas.getTriggerStages().forEach(stage -> listInStage(scope, stage).forEach(m -> dispatch(ActionEvent.EXIT,
                    newContext(scope, stage, m, null, null, ExitReason.DELETED, "shutdown replay", true))));
        });
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#uploadToStage(
     * java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject,
     * org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
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

            EObjectStorageService<T> storageService = storageFor(stage);
            ObjectMetadata objectMetadata = WorkflowServiceHelper.getPromiseValue(storageService.storeObject(scope,
                    config.registry_name(), stage, metadata.getObjectId(), object, metadata));
            dispatch(ActionEvent.ENTER, newContext(scope, stage, objectMetadata, null, null, null, null, false));
            return objectMetadata;
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
     * getMetadataFromStage(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ObjectMetadata getMetadataFromStage(String scope, String stage, String objectId) {
        requireNonNull(objectId, "Object ID cannot be null");
        validateStage(stage);
        EObjectStorageService<T> storageService = storageFor(stage);
        ObjectMetadata metadata = WorkflowServiceHelper
                .getPromiseValue(storageService.retrieveMetadata(scope, config.registry_name(), stage, objectId));
        if (!isWritableStage(stage) && metadata != null)
            metadata.setIsReadOnly(true);
        return metadata;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
     * getMetadataFromFinalStage(java.lang.String, java.lang.String)
     */
    @Override
    public ObjectMetadata getMetadataFromFinalStage(String scope, String objectId) {
    	StageInfo finalStage = stages.stream().filter(s -> s.isFinal()).findFirst().get();
        return getMetadataFromStage(scope, finalStage.getName(), objectId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
     * getContentFromStage(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public T getContentFromStage(String scope, String stage, String objectId) {
        requireNonNull(objectId, "Object ID cannot be null");
        validateStage(stage);
        EObjectStorageService<T> storageService = storageFor(stage);
        return WorkflowServiceHelper
                .getPromiseValue(storageService.retrieveObject(scope, config.registry_name(), stage, objectId));
    }
    
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getContentFromFinalStage(java.lang.String, java.lang.String)
	 */
	@Override
	public T getContentFromFinalStage(String scope, String objectId) {
		StageInfo finalStage = stages.stream().filter(s -> s.isFinal()).findFirst().get();
		return getContentFromStage(scope, finalStage.getName(), objectId);
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#updateInStage(
     * java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject,
     * java.lang.String, java.lang.String)
     */
    @Override
    public Promise<ObjectMetadata> updateInStage(String scope, String stage, T updatedObject, String objectId,
            String version) {

        return promiseFactory.submit(() -> {
            requireNonNull(objectId, "Object ID cannot be null");
            requireNonNull(updatedObject, "Updated object cannot be null");
            validateUpdatableStage(stage);

            EObjectStorageService<T> storageService = storageFor(stage);

            // Get current metadata
            ObjectMetadata metadata = WorkflowServiceHelper
                    .getPromiseValue(storageService.retrieveMetadata(scope, config.registry_name(), stage, objectId));
            metadata.setLastChangeTime(Instant.now());
            metadata.setStage(stage);
            metadata.setScope(scope);
            metadata.setRegistry(config.registry_name());
            metadata.setVersion(version);

            // Update the object in storage (uses updateObject which handles Apicurio
            // delete-then-create)
            metadata = WorkflowServiceHelper
                    .getPromiseValue(storageService.updateObject(objectId, updatedObject, metadata));
            dispatch(ActionEvent.UPDATE, newContext(scope, stage, metadata, null, null, null, null, false));
            return metadata;
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#deleteFromStage
     * (java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Promise<Boolean> deleteFromStage(String scope, String stage, String objectId) {

        return promiseFactory.submit(() -> {
            requireNonNull(objectId, "Object ID cannot be null");
            validateWritableStage(stage);

            EObjectStorageService<T> storageService = storageFor(stage);

            // Verify it exists
            ObjectMetadata metadata = WorkflowServiceHelper
                    .getPromiseValue(storageService.retrieveMetadata(scope, config.registry_name(), stage, objectId));
            if (metadata == null) {
                throw new IllegalStateException(String.format(
                        "Cannot delete object %s for scope '%s', registry '%s' and stage '%s' because no metadata has been found for it",
                        objectId, scope, config.registry_name(), stage));
            }

            // Delete from draft storage
            boolean deleted = WorkflowServiceHelper
                    .getPromiseValue(storageService.deleteObject(scope, config.registry_name(), stage, objectId));

            // Remove from registry
            if (deleted) {
                registryService.removeFromCache(objectId);
                dispatch(ActionEvent.EXIT, newContext(scope, stage, metadata, null, null, ExitReason.DELETED, null, false));
            }

            return deleted;
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInStage(
     * java.lang.String, java.lang.String)
     */
    @Override
    public List<ObjectMetadata> listInStage(String scope, String stage) {
        validateStage(stage);
        if (stages.stream().filter(s -> stage.equals(s.getName()) && s.isFinal()).findFirst().orElse(null) != null)
            return listInFinalStage(scope);
        try {
            return requireNonNullElse(registryService.findByScopeRegistryAndStage(scope, config.registry_name(), stage),
                    List.of());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
            EObjectStorageService<T> storageService = storageFor(stage);
            return requireNonNullElse(
                    WorkflowServiceHelper.getPromiseValue(storageService.queryObjects(
                            WorkflowServiceHelper.createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__STAGE,
                                    stage, ManagementPackage.Literals.OBJECT_QUERY__SCOPE, scope,
                                    ManagementPackage.Literals.OBJECT_QUERY__REGISTRY, config.registry_name())))),
                    List.of());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
     * listInStageByName(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ObjectMetadata> listInStageByName(String scope, String stage, String name) {
        validateStage(stage);
        try {
            return requireNonNullElse(
                    registryService.findByScopeRegistryStageAndName(scope, config.registry_name(), stage, name),
                    List.of());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
            EObjectStorageService<T> storageService = storageFor(stage);
            return requireNonNullElse(WorkflowServiceHelper.getPromiseValue(storageService.queryObjects(
                    WorkflowServiceHelper.createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__STAGE, stage,
                            ManagementPackage.Literals.OBJECT_QUERY__SCOPE, scope,
                            ManagementPackage.Literals.OBJECT_QUERY__REGISTRY, config.registry_name(),
                            ManagementPackage.Literals.OBJECT_QUERY__NAME, name)))),
                    List.of());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
     * listInFinalStage(java.lang.String)
     */
    @Override
    public List<ObjectMetadata> listInFinalStage(String scope) {
        List<ObjectMetadata> metadata = new LinkedList<>();
        StageInfo finalStage = stages.stream().filter(s -> s.isFinal()).findFirst().get();
        try {
            List<ObjectMetadata> localMetadata = requireNonNullElse(
                    registryService.findByScopeRegistryAndStage(scope, config.registry_name(), finalStage.getName()),
                    List.of());
            metadata.addAll(localMetadata);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error listing objects via registry, falling back to storage query", e);
            EObjectStorageService<T> storageService = storageFor(finalStage.getName());
            List<ObjectMetadata> localMetadata = requireNonNullElse(
                    WorkflowServiceHelper.getPromiseValue(storageService.queryObjects(
                            WorkflowServiceHelper.createQuery(Map.of(ManagementPackage.Literals.OBJECT_QUERY__STAGE,
                                    finalStage.getName(), ManagementPackage.Literals.OBJECT_QUERY__SCOPE, scope,
                                    ManagementPackage.Literals.OBJECT_QUERY__REGISTRY, config.registry_name())))),
                    List.of());
            metadata.addAll(localMetadata);
        }
        return metadata;
    }
    
    /* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listAll(java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listAll(String scope) {
		List<ObjectMetadata> metadata = new LinkedList<>();
		stages.forEach(stage -> {
			metadata.addAll(listInStage(scope, stage.getName()));
		});
		return metadata;
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
     * transitionToStage(java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public ObjectMetadata transitionToStage(String scope, String objectId, String fromStage, String toStage) {
        validateTransition(fromStage, toStage);
        EObjectStorageService<T> sourceStorage = storageFor(fromStage);
        T object = WorkflowServiceHelper
                .getPromiseValue(sourceStorage.retrieveObject(scope, config.registry_name(), fromStage, objectId));
        ObjectMetadata metadata = WorkflowServiceHelper
                .getPromiseValue(sourceStorage.retrieveMetadata(scope, config.registry_name(), fromStage, objectId));

        if (object == null || metadata == null) {
            throw new IllegalArgumentException("Object not found in stage " + fromStage + ": " + objectId);
        }
        // Update metadata for new stage
        metadata.setLastChangeTime(Instant.now());
        metadata.setStage(toStage);

        // Store in target stage
        EObjectStorageService<T> targetStorage = storageFor(toStage);

        // Delete from source stage (if configured). If the registry is shared though,
        // this will cause to remove also the newly created metadata,
        // so we have to do it before storing the object in the target stage
        if (config.delete_after_transition()) {
            WorkflowServiceHelper
                    .getPromiseValue(sourceStorage.deleteObject(scope, config.registry_name(), fromStage, objectId));
            dispatch(ActionEvent.EXIT,
                    newContext(scope, fromStage, metadata, null, toStage, ExitReason.TRANSITIONED, null, false));
        }
        WorkflowServiceHelper.getPromiseValue(
                targetStorage.storeObject(scope, config.registry_name(), toStage, objectId, object, metadata));
        dispatch(ActionEvent.ENTER, newContext(scope, toStage, metadata, fromStage, null, null, null, false));
        return metadata;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRegistryName
     * ()
     */
    @Override
    public String getRegistryName() {
        return config.registry_name();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isValidStage(
     * java.lang.String)
     */
    @Override
    public boolean isValidStage(String stageName) {
        return stages.stream().filter(s -> stageName.equals(s.getName())).findAny().isPresent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isWritableStage
     * (java.lang.String)
     */
    @Override
    public boolean isWritableStage(String stageName) {
        return stages.stream().filter(s -> stageName.equals(s.getName()) && s.isWritable()).findAny().isPresent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
     * isFinalStageWritable()
     */
    @Override
    public boolean isFinalStageWritable() {
        return stages.stream().filter(s -> s.isFinal() && s.isWritable()).findAny().isPresent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
     * isTransitionAllowed(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isTransitionAllowed(String fromStage, String toStage) {
        return allowedTransitionsList.stream()
                .filter(t -> fromStage.equals(t.getFromStage()) && toStage.equals(t.getToStage())).findFirst()
                .isPresent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
     * isEClassCompatibleWithRegistry(org.eclipse.emf.ecore.EClass)
     */
    @Override
    public boolean isEClassCompatibleWithRegistry(EClass eClass) {
        return EcoreUtil.getURI(eClass).equals(EcoreUtil.getURI(rootEClass))
                || eClass.getEAllSuperTypes().contains(rootEClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRootEClass()
     */
    @Override
    public EClass getRootEClass() {
        return rootEClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRegistry()
     */
    @Override
    public Registry getRegistry() {
        return registryObject;
    }

    private Registry createRegistryObject() {
        Registry registry = WorkflowApiFactory.eINSTANCE.createRegistry();
        registry.setName(config.registry_name());
        registry.setDescription(config.registry_description());
        registry.setType(RegistryType.get(config.registry_type()));
        registry.getAllowedTransitions().addAll(allowedTransitionsList);
        registry.getStages().addAll(stages);
        return registry;
    }

    private List<StageTransition> parseTransitionsIntoList(String[] workflow_transitions) {
        List<StageTransition> transitionsList = new LinkedList<>();
        for (String transition : workflow_transitions) {
            String[] transitionSplit = transition.split(":");
            if (transitionSplit.length != 2) {
                throw new IllegalArgumentException(String.format(
                        "Transition property %s is not properly formatted. Expected format 'fromStage:toStage'",
                        transition));
            }
            StageTransition stageTransition = WorkflowApiFactory.eINSTANCE.createStageTransition();
            stageTransition.setFromStage(transitionSplit[0]);
            stageTransition.setToStage(transitionSplit[1]);
            transitionsList.add(stageTransition);
        }
        return transitionsList;
    }

    private Map<String, EObjectStorageService<T>> parseStageStorageMappings(String[] mappings,
            List<EObjectStorageService<T>> storageServices) {
        Map<String, EObjectStorageService<T>> map = new HashMap<>();

        // Build storageType -> service lookup
        Map<String, EObjectStorageService<T>> storageByType = storageServices.stream()
                .collect(Collectors.toMap(s -> s.getStorageType(), Function.identity()));

        // Parse stage:storageType mappings
        for (String mapping : mappings) {
            String[] parts = mapping.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException(String.format(
                        "Storage mapping property %s is not properly formatted. Expected format 'stage:storageType'",
                        mapping));
            }
            String stageName = parts[0].trim();
            String storageType = parts[1].trim();
            EObjectStorageService<T> storage = storageByType.get(storageType);
            if (storage != null) {
                map.put(stageName, storage);
            } else {
                LOGGER.log(Level.WARNING, String.format(
                        "No storage service of type '%s' is registered, so stage '%s' of registry '%s' has no storage and every access to it will fail. Registered types: %s",
                        storageType, stageName, config.registry_name(), storageByType.keySet()));
            }
        }
        return map;
    }

    /**
     * Returns the storage service configured for the given stage. Stages whose
     * configured storage type was not registered are absent from the map (see
     * {@link #parseStageStorageMappings(String[], List)}); reporting that with the
     * configuration context beats the bare NPE the callers would otherwise hit.
     */
    private EObjectStorageService<T> storageFor(String stage) {
        EObjectStorageService<T> storageService = storageMap.get(stage);
        if (storageService == null) {
            throw new IllegalStateException(String.format(
                    "No storage service is available for stage '%s' of registry '%s'. Configured stage mappings: %s",
                    stage, config.registry_name(), Arrays.toString(config.stage_storage_mappings())));
        }
        return storageService;
    }

    private List<StageInfo> parseStages(String[] stages) {
        List<StageInfo> stageServices = new ArrayList<>(stages.length);
        for (String stage : stages) {
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> map = mapper.readValue(stage, new TypeReference<Map<String, Object>>() {
            });

            StageInfo stageService = ScopeApiFactory.eINSTANCE.createStageInfo();
            stageService.setName((String) map.get("name"));
            stageService.setWritable((boolean) map.get("writable"));
            stageService.setFinal((boolean) map.get("final"));
            stageServices.add(stageService);
        }
        return stageServices;
    }

    private void validateStages() {
        if (stages.stream().filter(s -> s.isFinal()).count() != 1) {
            throw new IllegalArgumentException("Exactly 1 final stage must be provided!");
        }
    }

    private boolean isFinalStage(String stageName) {
        return stages.stream().filter(s -> stageName.equals(s.getName()) && s.isFinal()).findFirst().isPresent();
    }

    private void validateStage(String stageName) {
        if (stageName == null) {
            throw new IllegalArgumentException(String.format("Stage name cannot be null!"));
        }
        if (!isValidStage(stageName)) {
            throw new IllegalArgumentException(String.format("Stage %s is not a valid stage for the registry %s",
                    stageName, config.registry_name()));
        }
        return;
    }

    private void validateUpdatableStage(String stageName) {
        validateWritableStage(stageName);
        if (isFinalStage(stageName)) {
            throw new IllegalArgumentException(String.format(
                    "Stage %s is final for the registry %s. Objects in the final stage cannot be updated.", stageName,
                    config.registry_name()));
        }
    }

    private void validateWritableStage(String stageName) {
        validateStage(stageName);
        if (!isWritableStage(stageName)) {
            throw new IllegalArgumentException(String.format("Stage %s is not a writable stage for the registry %s",
                    stageName, config.registry_name()));
        }
        return;
    }

    private void validateTransition(String fromStage, String toStage) {
        validateWritableStage(fromStage);
        validateWritableStage(toStage);
        if (!isTransitionAllowed(fromStage, toStage)) {
            throw new IllegalArgumentException(
                    String.format("Transition from stage %s to stage %s is not allowed in registry %s", fromStage,
                            toStage, config.registry_name()));
        }
    }


    private ActionContext newContext(String scope, String stage, ObjectMetadata m, String sourceStage,
            String targetStage, ExitReason exitReason, String notes, boolean replay) {
        return new ActionContext(scope, config.registry_name(), m.getObjectId(), m.getObjectType(), stage,
                sourceStage, targetStage, exitReason, "system", Instant.now(), notes, replay, Map.of());
    }

    private void dispatch(ActionEvent event, ActionContext ctx) {
        stageActionService.forEach(sas -> {
            if (!sas.supportsObjectType(ctx.objectType())) {
                return;
            }
            Set<String> triggerStages = sas.getTriggerStages();
            if (!triggerStages.isEmpty() && !triggerStages.contains(ctx.stage())) {
                return;
            }
            Set<ActionEvent> triggerEvents = sas.getTriggerEvents();
            if (!triggerEvents.isEmpty() && !triggerEvents.contains(event)) {
                return;
            }
            Promise<Void> p = switch (event) {
            case ENTER -> sas.onEnter(ctx);
            case UPDATE -> sas.onUpdate(ctx);
            case EXIT -> sas.onExit(ctx);
            };
            p.onFailure(t -> LOGGER.log(Level.WARNING, "StageAction " + sas.getClass().getSimpleName()
                    + " failed for " + event + " on " + ctx.objectId(), t));
        });
    }



	

}
