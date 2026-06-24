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
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.util.promise.Promise;


/**
 * 
 * @author ilenia
 * @since Mar 27, 2026
 */
@Component(name = "AtlasScopeService", immediate = true,
property = {
		"atlas.scope="+WorkflowConstants.ATLAS_SCOPE_NAME, 
		"scope.name="+WorkflowConstants.ATLAS_SCOPE_NAME, 
		"scope.description=Atlas Scope. The parent of all other scopes."
})
public class AtlasScopeService implements ScopeService<EPackage> {

	private RegistryService<EPackage> atlasSchemaRegistryService;
	private Scope scopeObject;

	@Activate
	public AtlasScopeService(@Reference(target = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME+")", cardinality = ReferenceCardinality.MANDATORY) RegistryService<EPackage> atlasSchemaRegistryService) {
		this.atlasSchemaRegistryService = atlasSchemaRegistryService;
		scopeObject = createScopeObject();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#uploadToStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	public Promise<ObjectMetadata> uploadToStageForRegistry(String registry, String stage, EPackage object,
			ObjectMetadata metadata) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.uploadToStage(WorkflowConstants.ATLAS_SCOPE_NAME, stage, object, metadata);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getMetadataFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getMetadataFromStageForRegistry(String registry, String stage, String objectId) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.getMetadataFromStage(WorkflowConstants.ATLAS_SCOPE_NAME, stage, objectId);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getMetadataFromFinalStageForRegistry(java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getMetadataFromFinalStageForRegistry(String registry, String objectId) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.getMetadataFromFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME, objectId);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getContentFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public EPackage getContentFromStageForRegistry(String registry, String stage, String objectId) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.getContentFromStage(WorkflowConstants.ATLAS_SCOPE_NAME, stage, objectId);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#updateInStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<ObjectMetadata> updateInStageForRegistry(String registry, String stage, EPackage updatedObject,
			String objectId, String version) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.updateInStage(WorkflowConstants.ATLAS_SCOPE_NAME, stage, updatedObject, objectId, version);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#deleteFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<Boolean> deleteFromStageForRegistry(String registry, String stage, String objectId) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.deleteFromStage(WorkflowConstants.ATLAS_SCOPE_NAME, stage, objectId);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#listInStageForRegistry(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStageForRegistry(String registry, String stage) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.listInStage(WorkflowConstants.ATLAS_SCOPE_NAME, stage);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#listInStageForRegistryByName(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStageForRegistryByName(String registry, String stage, String name) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.listInStageByName(WorkflowConstants.ATLAS_SCOPE_NAME, stage, name);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#listInFinalStageForRegistry(java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInFinalStageForRegistry(String registry) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.listInFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#transitionToStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata transitionToStageForRegistry(String registry, String objectId, String fromStage,
			String toStage) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.transitionToStage(WorkflowConstants.ATLAS_SCOPE_NAME, objectId, fromStage, toStage);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#isValidRegistry(java.lang.String)
	 */
	@Override
	public boolean isValidRegistry(String registryName) {
		return WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME.equals(registryName);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getAllRegistries()
	 */
	@Override
	public List<String> getAllRegistries() {
		return List.of(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getScope()
	 */
	@Override
	public Scope getScope() {
		return scopeObject;
	}



	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#listAllForRegistry(java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listAllForRegistry(String registry) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.listAll(WorkflowConstants.ATLAS_SCOPE_NAME);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#getScopeName()
	 */
	@Override
	public String getScopeName() {
		return WorkflowConstants.ATLAS_SCOPE_NAME;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#isInheritingFromParentScope()
	 */
	@Override
	public boolean isInheritingFromParentScope() {
		return false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#get(java.lang.String, java.lang.String)
	 */
	@Override
	public Optional<EPackage> get(String registry, String objectId) {
		validateRegistry(registry);
		return Optional.ofNullable(atlasSchemaRegistryService.getContentFromFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME, objectId));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#listObjectIds(java.lang.String)
	 */
	@Override
	public List<String> listObjectIds(String registry) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.listInFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME).stream().map(m -> m.getObjectId()).toList();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#listAll(java.lang.String)
	 */
	@Override
	public List<EPackage> listAll(String registry) {
		validateRegistry(registry);
		return atlasSchemaRegistryService.listInFinalStage(WorkflowConstants.ATLAS_SCOPE_NAME).stream().map(m -> atlasSchemaRegistryService.getContentFromFinalStage(getScopeName(), m.getObjectId())).toList();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#stream(java.lang.String)
	 */
	@Override
	public Stream<EPackage> stream(String registry) {
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

	private Scope createScopeObject() {
		Scope scope = WorkflowApiFactory.eINSTANCE.createScope();
		scope.setName(WorkflowConstants.ATLAS_SCOPE_NAME);
		scope.setDescription("Atlas Scope. The parent of all other scopes.");
		scope.setParentScope(null);
		scope.getRegistries().add(atlasSchemaRegistryService.getRegistry());
		return scope;
	}

	private void validateRegistry(String registryName) {
		if (!isValidRegistry(registryName)) {
			throw new IllegalArgumentException(String.format("Registry %s is not a valid registry for the scope %s",
					registryName, WorkflowConstants.ATLAS_SCOPE_NAME));
		}
		return;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#registryView(java.lang.String)
	 */
	@Override
	public ReadableRegistryView<EPackage> registryView(String registry) {
		throw new UnsupportedOperationException("registryView not yet implemented (P6-4)");
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#registryView(java.lang.String, java.lang.String)
	 */
	@Override
	public ReadableRegistryView<EPackage> registryView(String registry, String stage) {
		throw new UnsupportedOperationException("registryView not yet implemented (P6-4)");
	}

}
