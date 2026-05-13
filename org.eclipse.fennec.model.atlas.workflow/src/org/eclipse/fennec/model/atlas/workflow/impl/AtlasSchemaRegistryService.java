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

import java.time.Instant;
import java.util.Base64;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryType;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Stage;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.promise.Promise;

/**
 * 
 * @author ilenia
 * @since Mar 27, 2026
 */
@Component(name = "AtlasSchemaRegistryService", immediate = true,
property = {
		"registry.name="+WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
		"registry.description=Atlas Schema Registry, where all the system schemas are managed.",
		"registry.type=SCHEMA"
		})
public class AtlasSchemaRegistryService implements RegistryService<EPackage> {
	
	private final Registry registryObject;

    private EObjectRegistryService<EObject> registry;	
	private EPackage.Registry staticPackageRegistry;

	private EPackageLuceneIndex ePackageIndex;

	@Activate
	public AtlasSchemaRegistryService(@Reference(cardinality = ReferenceCardinality.MANDATORY) EObjectRegistryService<EObject> registry, 
			@Reference(cardinality = ReferenceCardinality.MANDATORY) EPackageLuceneIndex ePackageIndex) {
		this.registry = registry;
		this.ePackageIndex = ePackageIndex;
		this.registryObject = createRegistryObject();
	}
	
	@Reference(target = "(component.name=StaticEPackageRegistry)", policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public void bindStaticEPackageRegistry(EPackage.Registry staticPackageRegistry) {
		this.staticPackageRegistry = staticPackageRegistry;
		staticPackageRegistry.values().stream().filter(v -> v instanceof EPackage).map(v -> (EPackage) v).forEach(ePackage -> {
			ObjectMetadata metadata = createMetadata(ePackage);
			registry.updateCache(metadata);	
			ePackageIndex.index(metadata, ePackage);
		});
	}
	
	public void unbindStaticEPackageRegistry(EPackage.Registry staticPackageRegistry) {
		staticPackageRegistry.values().stream().filter(v -> v instanceof EPackage).map(v -> (EPackage) v).forEach(ePackage -> {
			String objectId = new String(Base64.getUrlEncoder().encode(ePackage.getNsURI().getBytes()));
			registry.removeFromCache(objectId);
			ePackageIndex.remove(objectId);
		});
		this.staticPackageRegistry = null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#uploadToStage(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 */
	@Override
	public Promise<ObjectMetadata> uploadToStage(String scope, String stage, EPackage object, ObjectMetadata metadata) {
		throw new UnsupportedOperationException("Upload Operation now allowed for Atlas Schema Registry");
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getMetadataFromStage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getMetadataFromStage(String scope, String stage, String objectId) {
		validateStage(stage);
		return getMetadataFromFinalStage(scope, objectId);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getMetadataFromFinalStage(java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata getMetadataFromFinalStage(String scope, String objectId) {
		return registry.getMetadata(objectId).orElse(null);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getContentFromStage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public EPackage getContentFromStage(String scope, String stage, String objectId) {
		validateStage(stage);
		if(staticPackageRegistry != null) {
			byte[] decodedBytes = Base64.getUrlDecoder().decode(objectId);
			String originalNsUri = new String(decodedBytes);
			return staticPackageRegistry.getEPackage(originalNsUri);
		}
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#updateInStage(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<ObjectMetadata> updateInStage(String scope, String stage, EPackage updatedObject, String objectId,
			String version) {
		throw new UnsupportedOperationException("Update Operation now allowed for Atlas Schema Registry");

	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#deleteFromStage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Promise<Boolean> deleteFromStage(String scope, String stage, String objectId) {
		throw new UnsupportedOperationException("Delete Operation now allowed for Atlas Schema Registry");
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInStage(java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStage(String scope, String stage) {
		validateStage(stage);
		return listInFinalStage(scope);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInStageByName(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInStageByName(String scope, String stage, String name) {
		validateStage(stage);
		return registry.findByScopeRegistryStageAndName(scope, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, name);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInFinalStage(java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listInFinalStage(String scope) {
		return registry.findByScopeRegistryAndStage(scope, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#transitionToStage(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectMetadata transitionToStage(String scope, String objectId, String fromStage, String toStage) {
		throw new UnsupportedOperationException("Transition Operation now allowed for Atlas Schema Registry");
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRegistryName()
	 */
	@Override
	public String getRegistryName() {
		return WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isValidStage(java.lang.String)
	 */
	@Override
	public boolean isValidStage(String stageName) {
		return WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME.equals(stageName);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isWritableStage(java.lang.String)
	 */
	@Override
	public boolean isWritableStage(String stageName) {
		return false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isFinalStageWritable()
	 */
	@Override
	public boolean isFinalStageWritable() {
		return false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isTransitionAllowed(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isTransitionAllowed(String fromStage, String toStage) {
		return false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRegistry()
	 */
	@Override
	public Registry getRegistry() {
		return registryObject;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isEClassCompatibleWithRegistry(org.eclipse.emf.ecore.EClass)
	 */
	@Override
	public boolean isEClassCompatibleWithRegistry(EClass eClass) {
		return EcoreUtil.getURI(eClass).equals(EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE))
                || eClass.getEAllSuperTypes().contains(EcorePackage.Literals.EPACKAGE);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRootEClass()
	 */
	@Override
	public EClass getRootEClass() {
		return EcorePackage.Literals.EPACKAGE;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#activate(java.lang.String)
	 */
	@Override
	public Void activate(String scope) {
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#deactivate(java.lang.String)
	 */
	@Override
	public Void deactivate(String scope) {
		return null;
	}
	
	private Registry createRegistryObject() {
        Registry registry = WorkflowApiFactory.eINSTANCE.createRegistry();
        registry.setName(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME);
        registry.setDescription("Atlas Schema Registry, where all the system schemas are managed.");
        registry.setType(RegistryType.SCHEMA);
        
        Stage stage = WorkflowApiFactory.eINSTANCE.createStage();
        stage.setName(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME);
        stage.setWritable(false);
        stage.setFinal(true);
        
        registry.getStages().add(stage);
        return registry;
    }

	private ObjectMetadata createMetadata(EPackage ePackage) {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(new String(Base64.getUrlEncoder().encode(ePackage.getNsURI().getBytes())));
		metadata.setObjectName(ePackage.getName());
		metadata.setIsReadOnly(true);
		metadata.setObjectType(EcoreUtil.getURI(ePackage.eClass()).toString());
		metadata.setScope(WorkflowConstants.ATLAS_SCOPE_NAME);
		metadata.setRegistry(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME);
		metadata.setStage(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME);
		metadata.setUploadTime(Instant.now());
		metadata.setUploadUser("system");
		return metadata;
	}
	
	private void validateStage(String stageName) {
        if (!isValidStage(stageName)) {
            throw new IllegalArgumentException(String.format("Stage %s is not a valid stage for the registry %s",
                    stageName, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME));
        }
        return;
    }

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listAll(java.lang.String)
	 */
	@Override
	public List<ObjectMetadata> listAll(String scope) {
		return listInFinalStage(scope);
	}

}
