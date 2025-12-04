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
 * A representation of the model object '<em><b>EObject Workflow Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Generic orchestrator for EObject workflow management
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getEObjectWorkflowService()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface EObjectWorkflowService<T extends EObject> {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Upload an EObject to a certain stage (draft, review, etc); returns promise with uploaded ObjectMetadata.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" objectRequired="true" metadataRequired="true"
	 * @generated
	 */
	Promise<ObjectMetadata> uploadToStage(String stage, T object, ObjectMetadata metadata);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get object registration by ID from a certain stage. If nothing is found in that stage, the parents release stages are also inspected. 
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	ObjectMetadata getFromStage(String stage, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get object registration by ID from the final stage of a workflow. If nothing is found in that stage, the parents final stages are also inspected. 
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	ObjectMetadata getFromFinalStage(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get the actual EObject content by ID for a certain stage. If nothing is found, the parents release stages are also inspected.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	T getContentFromStage(String stage, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Update an existing object. Returned updated ObjectMetadata.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" updatedObjectRequired="true" objectIdRequired="true" versionRequired="true"
	 * @generated
	 */
	Promise<ObjectMetadata> updateInStage(String stage, T updatedObject, String objectId, String version);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Delete an object
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EBooleanObject&gt;" objectIdRequired="true"
	 * @generated
	 */
	Promise<Boolean> deleteFromStage(String stage, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all objects in a certain stage of the workflow
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listInStage(String stage);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all objects in the final stage of the workflow
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listInFinalStage();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Performs a transition of an EObject from one stage to another, if allowed.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	ObjectMetadata transitionToStage(String objectId, String fromStage, String toStage);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Checks whether a transition from one stage to another is allowed or not.
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	boolean isTransitionAllowed(String fromStage, String toStage);

} // EObjectWorkflowService
