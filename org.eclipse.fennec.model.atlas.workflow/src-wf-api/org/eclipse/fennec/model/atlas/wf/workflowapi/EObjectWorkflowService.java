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
 *      Mark Hoffmann - initial API and implementation
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
public interface EObjectWorkflowService<T extends EObject> extends WorkflowDraftProvider<T> {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Approve an object for release, returns updated registration
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true" reviewUserRequired="true"
	 * @generated
	 */
	ObjectMetadata approveObject(String objectId, String reviewUser, String approvalReason);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reject an object during review, returns updated registration
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true" reviewUserRequired="true" rejectionReasonRequired="true"
	 * @generated
	 */
	ObjectMetadata rejectObject(String objectId, String reviewUser, String rejectionReason);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Release an approved object to production storage after compliance checks, returns updated registration
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true" requireComplianceCheckRequired="true"
	 * @generated
	 */
	ObjectMetadata releaseObject(String objectId, String releaseNotes, boolean requireComplianceCheck);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all approved objects ready for release
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listApprovedObjects();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all rejected objects
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listRejectedObjects();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all released/production objects
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> listReleasedObjects();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get object registration by ID
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	ObjectMetadata getObject(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get the actual EObject content by ID
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	Object getObjectContent(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Update an existing object
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.Void&gt;" objectIdRequired="true" updatedObjectRequired="true"
	 * @generated
	 */
	Promise<Void> updateObject(String objectId, T updatedObject);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Delete an object
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EBooleanObject&gt;" objectIdRequired="true"
	 * @generated
	 */
	Promise<Boolean> deleteObject(String objectId);

} // EObjectWorkflowService
