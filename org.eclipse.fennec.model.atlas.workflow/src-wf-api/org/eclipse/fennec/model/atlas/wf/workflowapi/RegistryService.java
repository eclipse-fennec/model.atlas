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
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

import org.osgi.annotation.versioning.ProviderType;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Registry Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Manages stage validation and storage operations for a specific registry type (scope-agnostic, can serve multiple scopes).
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getRegistryService()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface RegistryService<T extends EObject> {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Merge entries into the properties map of an object's metadata for a certain stage and scope, without touching the object's content. Entries present in the argument are set; keys absent from the argument are left untouched. Returns the stored ObjectMetadata.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" objectIdRequired="true" propertiesMapType="org.eclipse.fennec.model.atlas.mgmt.management.StringToObjectMapEntry&lt;org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EJavaObject&gt;"
	 * @generated
	 */
	Promise<ObjectMetadata> updateProperties(String scope, String stage, String objectId, Map<String, Object> properties);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Upload an EObject to a certain stage (draft, review, etc) for a certain scope; returns promise with uploaded ObjectMetadata.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" objectRequired="true" metadataRequired="true"
	 * @generated
	 */
	Promise<ObjectMetadata> uploadToStage(String scope, String stage, T object, ObjectMetadata metadata);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Retrieve the ObjectMetadata for a certain objectId, for the provided scope and stage.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	ObjectMetadata getMetadataFromStage(String scope, String stage, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get ObjectMetadata for the provided objectId and the provided scope, for the final stage of the configured workflow.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	ObjectMetadata getMetadataFromFinalStage(String scope, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get the actual EObject content by ID for a certain stage and scope.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	T getContentFromStage(String scope, String stage, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get the actual EObject content by ID for the final stage of a certain scope.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	T getContentFromFinalStage(String scope, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Update an existing object for a certain stage and scope. Returned updated ObjectMetadata.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" updatedObjectRequired="true" objectIdRequired="true" versionRequired="true"
	 * @generated
	 */
	Promise<ObjectMetadata> updateInStage(String scope, String stage, T updatedObject, String objectId, String version);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Delete an object from a certain stage for a certain scope. Returns whether the deletion was successfull.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EBooleanObject&gt;" objectIdRequired="true"
	 * @generated
	 */
	Promise<Boolean> deleteFromStage(String scope, String stage, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all objects in a certain stage and scope of the workflow
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listInStage(String scope, String stage);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all objects with a matching name filter in a certain stage and scope of the workflow
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listInStageByName(String scope, String stage, String name);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all objects in the final stage and for the specified scope of the workflow
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listInFinalStage(String scope);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all objects in all the stages and for the specified scope of the workflow
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listAll(String scope);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Performs a transition of an EObject from one stage to another for a certain scope, if allowed.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	ObjectMetadata transitionToStage(String scope, String objectId, String fromStage, String toStage);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getRegistryName();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean isValidStage(String stageName);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean isWritableStage(String stageName);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	boolean isFinalStageWritable();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean isTransitionAllowed(String fromStage, String toStage);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	Registry getRegistry();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean isEClassCompatibleWithRegistry(EClass eClass);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	EClass getRootEClass();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Void"
	 * @generated
	 */
	Void activate(String scope);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Void"
	 * @generated
	 */
	Void deactivate(String scope);

} // RegistryService
