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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.util.promise.Promise;

/**
 * 
 * @author ilenia
 * @since Jan 13, 2026
 */
@Component(name = "ScopeService", configurationPid = "ScopeService", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = ScopeServiceConfig.class)
public class ScopeServiceImpl<T extends EObject> implements ScopeService<T>, WritableScopeService<T>, ReadableScopeService<T> {

	private Map<String, RegistryService<T>> registryServiceMap = new ConcurrentHashMap<>();
	private ScopeServiceConfig config;

	private volatile Scope scopeObject;

	@Reference(target = "(registry.name="+ WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME +")")
	RegistryService<EPackage> atlasSchemaRegistryService;


	@Activate
	public ScopeServiceImpl(ScopeServiceConfig config) {
		this.config = config;
	}

	@Reference(name = "registryService", policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
	public void bindRegistryService(RegistryService<T> registryService, Map<String, Object> properties) {
		registryServiceMap.put(registryService.getRegistryName(), registryService);
		scopeObject = createScopeObject();
		registryService.activate(config.scope_name());
	}

	public void unbindRegistryService(RegistryService<T> registryService, Map<String, Object> properties) {
		registryServiceMap.remove(registryService.getRegistryName());
		scopeObject = createScopeObject();
		registryService.deactivate(config.scope_name());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
	 * uploadToStageForRegistry(java.lang.String, java.lang.String,
	 * org.eclipse.emf.ecore.EObject,
	 * org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	public Promise<ObjectMetadata> uploadToStageForRegistry(String registry, String stage, T object,
			ObjectMetadata metadata) {
		validateRegistry(registry);
		return getRegistryService(registry).uploadToStage(config.scope_name(), stage, object, metadata);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
	 * getMetadataFromStageForRegistry(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public ObjectMetadata getMetadataFromStageForRegistry(String registry, String stage, String objectId) {
		validateRegistry(registry);
		ObjectMetadata scopedMetadata = getRegistryService(registry).getMetadataFromStage(config.scope_name(), stage,
				objectId);

		if(scopedMetadata == null) {
			return metadataFromParent(registry, objectId);
		}
		return scopedMetadata;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
	 * getMetadataFromFinalStageForRegistry(java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getMetadataFromFinalStageForRegistry(String registry, String objectId) {
		validateRegistry(registry);
		ObjectMetadata scopedMetadata = getRegistryService(registry).getMetadataFromFinalStage(config.scope_name(),
				objectId);

		if(scopedMetadata == null) {
			return metadataFromParent(registry, objectId);
		}
		return scopedMetadata;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#
	 * getMetadataByPropertyFromStageForRegistry(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> getMetadataByPropertyFromStageForRegistry(String registry, String stage, String key,
			String value) {
		validateRegistry(registry);
		validateProperty(key, value);
		List<ObjectMetadata> scopedMetadata = filterByProperty(
				getRegistryService(registry).listInStage(config.scope_name(), stage), key, value);
		if(scopedMetadata.isEmpty()) {
			return getMetadataByPropertyFromParentForRegistry(registry, key, value);
		}
		return scopedMetadata;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#
	 * getMetadataByPropertyFromFinalStageForRegistry(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> getMetadataByPropertyFromFinalStageForRegistry(String registry, String key,
			String value) {
		validateRegistry(registry);
		validateProperty(key, value);
		List<ObjectMetadata> scopedMetadata = filterByProperty(
				getRegistryService(registry).listInFinalStage(config.scope_name()), key, value);
		if(scopedMetadata.isEmpty()) {
			return getMetadataByPropertyFromParentForRegistry(registry, key, value);
		}
		return scopedMetadata;
	}

	private List<ObjectMetadata> getMetadataByPropertyFromParentForRegistry(String registry, String key, String value) {
		return filterByProperty(listFromParent(registry), key, value);
	}

	private static List<ObjectMetadata> filterByProperty(List<ObjectMetadata> metadata, String key, String value) {
		return metadata.stream().filter(m -> value.equals(m.getProperties().get(key))).toList();
	}

	private static void validateProperty(String key, String value) {
		if (key == null || key.isBlank()) {
			throw new IllegalArgumentException("Property key cannot be null or blank!");
		}
		if (value == null) {
			throw new IllegalArgumentException("Property value cannot be null!");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
	 * getContentFromStageForRegistry(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public T getContentFromStageForRegistry(String registry, String stage, String objectId) {
		validateRegistry(registry);
		T content = getRegistryService(registry).getContentFromStage(config.scope_name(), stage, objectId);
		if(content == null) {
			return getContentFromParentForRegistry(registry, objectId);
		}
		return content;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
	 * updateInStageForRegistry(java.lang.String, java.lang.String,
	 * org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<ObjectMetadata> updateInStageForRegistry(String registry, String stage, T updatedObject,
			String objectId, String version) {
		validateRegistry(registry);
		return getRegistryService(registry).updateInStage(config.scope_name(), stage, updatedObject, objectId, version);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
	 * updatePropertiesInStageForRegistry(java.lang.String, java.lang.String,
	 * java.lang.String, java.util.Map)
	 */
	@Override
	public Promise<ObjectMetadata> updatePropertiesInStageForRegistry(String registry, String stage, String objectId,
			Map<String, Object> properties) {
		validateRegistry(registry);
		return getRegistryService(registry).updateProperties(config.scope_name(), stage, objectId, properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
	 * deleteFromStageForRegistry(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Promise<Boolean> deleteFromStageForRegistry(String registry, String stage, String objectId) {
		validateRegistry(registry);
		return getRegistryService(registry).deleteFromStage(config.scope_name(), stage, objectId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
	 * listInStageForRegistry(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStageForRegistry(String registry, String stage) {
		validateRegistry(registry);
		return getRegistryService(registry).listInStage(config.scope_name(), stage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
	 * listInStageForRegistryByName(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStageForRegistryByName(String registry, String stage, String name) {
		validateRegistry(registry);
		return getRegistryService(registry).listInStageByName(config.scope_name(), stage, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
	 * listInFinalStageForRegistry(java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInFinalStageForRegistry(String registry) {
		validateRegistry(registry);
		List<ObjectMetadata> scopedMetadata = getRegistryService(registry).listInFinalStage(config.scope_name());
		scopedMetadata.addAll(listFromParent(registry));
		return scopedMetadata;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#listAllForRegistry(java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listAllForRegistry(String registry) {
		validateRegistry(registry);
		// Every stage of this scope, but only what the parent has released: inheritance
		// exposes a parent's final stage, never its work in progress.
		List<ObjectMetadata> scopedMetadata = getRegistryService(registry).listAll(config.scope_name());
		scopedMetadata.addAll(listFromParent(registry));
		return scopedMetadata;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
	 * transitionToStageForRegistry(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata transitionToStageForRegistry(String registry, String objectId, String fromStage,
			String toStage) {
		validateRegistry(registry);
		return getRegistryService(registry).transitionToStage(config.scope_name(), objectId, fromStage, toStage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#isValidRegistry(
	 * java.lang.String)
	 */
	@Override
	public boolean isValidRegistry(String registryName) {
		return registryServiceMap.containsKey(registryName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getAllRegistries()
	 */
	@Override
	public List<String> getAllRegistries() {
		return registryServiceMap.keySet().stream().toList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getScope()
	 */
	@Override
	public Scope getScope() {
		return scopeObject;
	}

	private Scope createScopeObject() {
		Scope scope = WorkflowApiFactory.eINSTANCE.createScope();
		scope.setName(config.scope_name());
		scope.setDescription(config.scope_description());
		scope.setParentScope(config.scope_parent());
		registryServiceMap.forEach((regName, reg) -> scope.getRegistries().add(reg.getRegistry()));
		return scope;
	}

	private RegistryService<T> getRegistryService(String registryName) {
		return registryServiceMap.getOrDefault(registryName, null);
	}

	private void validateRegistry(String registryName) {
		if (registryName == null) {
			throw new IllegalArgumentException(String.format("Registry name cannot be null!"));
		}
		if (!isValidRegistry(registryName)) {
			throw new IllegalArgumentException(String.format("Registry %s is not a valid registry for the scope %s",
					registryName, config.scope_name()));
		}
		return;
	}

	private T getContentFromFinalStageForRegistry(String registry, String objectId) {
		validateRegistry(registry);
		T content = getRegistryService(registry).getContentFromFinalStage(config.scope_name(), objectId);
		if(content == null) {
			return getContentFromParentForRegistry(registry, objectId);
		}
		return content;
	}
	
	/**
	 * The registry service to consult for this scope's parent, or empty when the parent
	 * holds nothing this registry can inherit.
	 *
	 * <p>
	 * One rule, in one place — it used to be spelled out at six call sites, and they had
	 * begun to drift apart:
	 * </p>
	 * <ul>
	 * <li>parent is the atlas scope and this is a SCHEMA registry → the atlas schema
	 * registry, the one registry every scope inherits from;</li>
	 * <li>parent is the atlas scope and this is any other registry → nothing to inherit,
	 * because the atlas scope holds only that schema registry;</li>
	 * <li>parent is a normal scope → its registry of the same name;</li>
	 * <li>no parent configured cannot happen: the default is the atlas scope.</li>
	 * </ul>
	 *
	 * <p>
	 * Whatever the parent is, only its <em>final</em> stage is inherited: a child sees
	 * what its parent has released, never what the parent is still working on.
	 * </p>
	 *
	 * @param registry the registry being read
	 * @return the parent's registry service, or empty if the parent must not be consulted
	 */
	private Optional<RegistryService<?>> parentRegistry(String registry) {
		if (WorkflowConstants.ATLAS_SCOPE_NAME.equals(config.scope_parent())) {
			return RegistryType.SCHEMA == getRegistryService(registry).getRegistry().getType()
					? Optional.of(atlasSchemaRegistryService)
					: Optional.empty();
		}
		return Optional.of(getRegistryService(registry));
	}

	/**
	 * Marks metadata as inherited, i.e. read-only in this scope: it belongs to the parent,
	 * and only the parent can change it. The atlas schema registry already creates its
	 * metadata read-only, so saying it again there costs nothing and keeps one rule.
	 */
	private static ObjectMetadata inherited(ObjectMetadata metadata) {
		if (metadata != null) {
			metadata.setIsReadOnly(true);
		}
		return metadata;
	}

	private static List<ObjectMetadata> inherited(List<ObjectMetadata> metadata) {
		metadata.forEach(ScopeServiceImpl::inherited);
		return metadata;
	}

	/** The parent's final-stage metadata for {@code objectId}, or {@code null}. */
	private ObjectMetadata metadataFromParent(String registry, String objectId) {
		return parentRegistry(registry)
				.map(parent -> inherited(parent.getMetadataFromFinalStage(config.scope_parent(), objectId)))
				.orElse(null);
	}

	/** Everything the parent has in its final stage, possibly empty. */
	private List<ObjectMetadata> listFromParent(String registry) {
		return parentRegistry(registry)
				.map(parent -> inherited(parent.listInFinalStage(config.scope_parent())))
				.orElseGet(List::of);
	}

	@SuppressWarnings("unchecked")
	private T getContentFromParentForRegistry(String registry, String objectId) {
		return parentRegistry(registry)
				.map(parent -> (T) parent.getContentFromFinalStage(config.scope_parent(), objectId))
				.orElse(null);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#getScopeName()
	 */
	@Override
	public String getScopeName() {
		return config.scope_name();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#isInheritingFromParentScope()
	 */
	@Override
	public boolean isInheritingFromParentScope() {
		return config.scope_parent() != null && !config.scope_parent().isBlank();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#get(java.lang.String, java.lang.String)
	 */
	@Override
	public Optional<T> get(String registry, String objectId) {
		return Optional.ofNullable(getContentFromFinalStageForRegistry(registry, objectId));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#listObjectIds(java.lang.String)
	 */
	@Override
	public List<String> listObjectIds(String registry) {
		return listInFinalStageForRegistry(registry).stream().map(metadata -> metadata.getObjectId()).toList();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#listAll(java.lang.String)
	 */
	@Override
	public List<T> listAll(String registry) {
		return listInFinalStageForRegistry(registry).stream().map(m -> getContentFromFinalStageForRegistry(registry, m.getObjectId())).toList();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#stream(java.lang.String)
	 */
	@Override
	public Stream<T> stream(String registry) {
		return listAll(registry).stream();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#getScopeInfo()
	 */
	@Override
	public ScopeInfo getScopeInfo() {
		return scopeObject;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#registryView(java.lang.String)
	 */
	@Override
	public ReadableRegistryView<T> registryView(String registry) {
		Objects.requireNonNull(registry, "registry");
		validateRegistry(registry);
		return new ScopeRegistryView<>(config.scope_name(), registry, null,
				() -> listObjectIds(registry),
				objectId -> getContentFromFinalStageForRegistry(registry, objectId));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#registryView(java.lang.String, java.lang.String)
	 */
	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The two reads a stage-explicit view offers inherit differently, because the
	 * underlying scope operations do: {@code get} falls back to the parent hierarchy's
	 * final stage ({@link #getContentFromStageForRegistry}), while the listing is this
	 * scope's stage only ({@link #listInStageForRegistry}). The remote client's view
	 * has the same shape, since it calls the endpoints backed by these operations.
	 * </p>
	 */
	@Override
	public ReadableRegistryView<T> registryView(String registry, String stage) {
		Objects.requireNonNull(registry, "registry");
		Objects.requireNonNull(stage, "stage — use registryView(registry) for the final-stage view");
		validateRegistry(registry);
		return new ScopeRegistryView<>(config.scope_name(), registry, stage,
				() -> listInStageForRegistry(registry, stage).stream().map(ObjectMetadata::getObjectId).toList(),
				objectId -> getContentFromStageForRegistry(registry, stage, objectId));
	}


}
