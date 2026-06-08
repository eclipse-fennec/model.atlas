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
package org.eclipse.fennec.model.atlas.wf.workflowapi;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

import org.osgi.annotation.versioning.ProviderType;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Scope Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Orchestrates all data operations for this scope by validating and delegating to RegistryService.
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getScopeService()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface ScopeService<T extends EObject> {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Upload an EObject to a certain stage (draft, review, etc) for a certain registry (schema, configuration, script, etc); returns promise with uploaded ObjectMetadata.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" objectRequired="true" metadataRequired="true"
	 * @generated
	 */
	Promise<ObjectMetadata> uploadToStageForRegistry(String registry, String stage, T object, ObjectMetadata metadata);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get object registration by ID from a certain stage and a certain registry. If nothing is found in that stage/registry, the parents final stage of the same registry are also inspected. 
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	ObjectMetadata getMetadataFromStageForRegistry(String registry, String stage, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get object registration by ID from the final stage of a workflow and the specified registry. If nothing is found in that stage, the parents final stage of the same registry are also inspected. 
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	ObjectMetadata getMetadataFromFinalStageForRegistry(String registry, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get the actual EObject content by ID for a certain stage and registry. If nothing is found, the parents final stage of the same registry are also inspected.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	T getContentFromStageForRegistry(String registry, String stage, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get the actual EObject content by ID for the final stage of a registry. If nothing is found, the parents final stage of the same registry are also inspected.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	T getContentFromFinalStageForRegistry(String registry, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Update an existing object in a certain stage and registry. Returned updated ObjectMetadata.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" updatedObjectRequired="true" objectIdRequired="true" versionRequired="true"
	 * @generated
	 */
	Promise<ObjectMetadata> updateInStageForRegistry(String registry, String stage, T updatedObject, String objectId, String version);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Delete an object from a certain registry and stage. Returns whether the deletion was successfull.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EBooleanObject&gt;" objectIdRequired="true"
	 * @generated
	 */
	Promise<Boolean> deleteFromStageForRegistry(String registry, String stage, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all objects in a certain stage and registry of the workflow
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listInStageForRegistry(String registry, String stage);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all objects with a matching name filter in a certain stage and registry of the workflow
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listInStageForRegistryByName(String registry, String stage, String name);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all objects in the final stage and for the specified registry of the workflow
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listInFinalStageForRegistry(String registry);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all objects in all stages and for the specified registry of the workflow
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listAllForRegistry(String registry);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Performs a transition of an EObject from one stage to another of a certain registry, if allowed.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	ObjectMetadata transitionToStageForRegistry(String registry, String objectId, String fromStage, String toStage);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean isValidRegistry(String registryName);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation" dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.emf.ecore.EString&gt;" many="false"
	 * @generated
	 */
	List<String> getAllRegistries();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	Scope getScope();

} // ScopeService
