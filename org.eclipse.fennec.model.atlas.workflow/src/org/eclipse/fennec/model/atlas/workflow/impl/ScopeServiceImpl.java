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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.util.promise.Promise;

/**
 * 
 * @author ilenia
 * @since Jan 13, 2026
 */
@Component(name = "ScopeService", configurationPid = "ScopeService", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = ScopeServiceConfig.class)
public class ScopeServiceImpl<T extends EObject> implements ScopeService<T> {

	private List<RegistryService<T>> registryService;
	private ScopeServiceConfig config;

	@Activate
	public ScopeServiceImpl(
			@Reference(name = "registryService") List<RegistryService<T>> registryService,
			ScopeServiceConfig config
			) {
		this.registryService = registryService;
		this.config = config;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#uploadToStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	public Promise<ObjectMetadata> uploadToStageForRegistry(String registry, String stage, T object,
			ObjectMetadata metadata) {
		validateRegistry(registry);
		return getRegistryService(registry).uploadToStage(config.scope_name(), stage, object, metadata);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getMetadataFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getMetadataFromStageForRegistry(String registry, String stage, String objectId) {
		validateRegistry(registry);
		ObjectMetadata scopedMetadata = getRegistryService(registry).getMetadataFromStage(config.scope_name(), stage, objectId);
		if(scopedMetadata == null && config.scope_parent() != null) {
			ObjectMetadata parentScopeMetadata = getRegistryService(registry).getMetadataFromFinalStage(config.scope_parent(), objectId);
			if(parentScopeMetadata != null) parentScopeMetadata.setIsReadOnly(true);
			return parentScopeMetadata;
		}
		return scopedMetadata;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getMetadataFromFinalStageForRegistry(java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getMetadataFromFinalStageForRegistry(String registry, String objectId) {
		validateRegistry(registry);
		ObjectMetadata scopedMetadata = getRegistryService(registry).getMetadataFromFinalStage(config.scope_name(), objectId);
		if(scopedMetadata == null && config.scope_parent() != null) {
			ObjectMetadata parentScopeMetadata = getRegistryService(registry).getMetadataFromFinalStage(config.scope_parent(), objectId);
			if(parentScopeMetadata != null) parentScopeMetadata.setIsReadOnly(true);
			return parentScopeMetadata;
		}
		return scopedMetadata;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getContentFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public T getContentFromStageForRegistry(String registry, String stage, String objectId) {
		validateRegistry(registry);
		return getRegistryService(registry).getContentFromStage(config.scope_name(), stage, objectId);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#updateInStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<ObjectMetadata> updateInStageForRegistry(String registry, String stage, T updatedObject,
			String objectId, String version) {
		validateRegistry(registry);
		return getRegistryService(registry).updateInStage(config.scope_name(), stage, updatedObject, objectId, version);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#deleteFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<Boolean> deleteFromStageForRegistry(String registry, String stage, String objectId) {
		validateRegistry(registry);
		return getRegistryService(registry).deleteFromStage(config.scope_name(), stage, objectId);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#listInStageForRegistry(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStageForRegistry(String registry, String stage) {
		validateRegistry(registry);
		return getRegistryService(registry).listInStage(config.scope_name(), stage);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#listInStageForRegistryByName(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStageForRegistryByName(String registry, String stage, String name) {
		validateRegistry(registry);
		return getRegistryService(registry).listInStageByName(config.scope_name(), stage, name);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#listInFinalStageForRegistry(java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInFinalStageForRegistry(String registry) {
		validateRegistry(registry);
		List<ObjectMetadata> scopedMetadata = getRegistryService(registry).listInFinalStage(config.scope_name());
		if(config.scope_parent() != null) {
			scopedMetadata.addAll(getRegistryService(registry).listInFinalStage(config.scope_parent()));
		}
		return scopedMetadata;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#transitionToStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata transitionToStageForRegistry(String registry, String objectId, String fromStage,
			String toStage) {
		validateRegistry(registry);
		return getRegistryService(registry).transitionToStage(config.scope_name(), objectId, fromStage, toStage);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#isValidRegistry(java.lang.String)
	 */
	@Override
	public boolean isValidRegistry(String registryName) {
		return registryService.stream().filter(r -> registryName.equals(r.getRegistryName())).findAny().isPresent();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getAllRegistries()
	 */
	@Override
	public List<String> getAllRegistries() {
		return registryService.stream().map(r -> r.getRegistryName()).toList();
	}
	
	private RegistryService<T> getRegistryService(String registryName) {
		return registryService.stream().filter(r -> registryName.equals(r.getRegistryName())).findAny().get();
	}
	
	private void validateRegistry(String registryName) {
		if(registryName == null) {
			throw new IllegalArgumentException(String.format("Registry name cannot be null!"));
		}
		if(!isValidRegistry(registryName)) {
			throw new IllegalArgumentException(String.format("Registry %s is not a valid registry for the scope %s", registryName, config.scope_name()));
		}
		return;
	}

}
