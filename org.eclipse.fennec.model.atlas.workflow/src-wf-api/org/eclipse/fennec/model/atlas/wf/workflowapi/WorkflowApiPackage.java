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


import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;

import org.gecko.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = WorkflowApiPackage.eNS_URI, genModel = "/model/workflow-api.genmodel", genModelSourceLocations = {"model/workflow-api.genmodel","org.eclipse.fennec.model.atlas.workflow/model/workflow-api.genmodel"}, ecore="/model/workflow-api.ecore", ecoreSourceLocations="/model/workflow-api.ecore")
public interface WorkflowApiPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "workflowapi";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/workflow/api/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "workflowapi";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	WorkflowApiPackage eINSTANCE = org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider <em>Workflow Draft Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getWorkflowDraftProvider()
	 * @generated
	 */
	int WORKFLOW_DRAFT_PROVIDER = 1;

	/**
	 * The number of structural features of the '<em>Workflow Draft Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Upload Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___UPLOAD_DRAFT__EOBJECT_OBJECTMETADATA = 0;

	/**
	 * The operation id for the '<em>List Draft Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___LIST_DRAFT_OBJECTS = 1;

	/**
	 * The operation id for the '<em>Get Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___GET_DRAFT__STRING = 2;

	/**
	 * The operation id for the '<em>Get Draft Content</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___GET_DRAFT_CONTENT__STRING = 3;

	/**
	 * The operation id for the '<em>Update Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___UPDATE_DRAFT__STRING_EOBJECT = 4;

	/**
	 * The operation id for the '<em>Delete Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___DELETE_DRAFT__STRING = 5;

	/**
	 * The number of operations of the '<em>Workflow Draft Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT = 6;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService <em>EObject Workflow Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getEObjectWorkflowService()
	 * @generated
	 */
	int EOBJECT_WORKFLOW_SERVICE = 0;

	/**
	 * The number of structural features of the '<em>EObject Workflow Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE_FEATURE_COUNT = WORKFLOW_DRAFT_PROVIDER_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Upload Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPLOAD_DRAFT__EOBJECT_OBJECTMETADATA = WORKFLOW_DRAFT_PROVIDER___UPLOAD_DRAFT__EOBJECT_OBJECTMETADATA;

	/**
	 * The operation id for the '<em>List Draft Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_DRAFT_OBJECTS = WORKFLOW_DRAFT_PROVIDER___LIST_DRAFT_OBJECTS;

	/**
	 * The operation id for the '<em>Get Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_DRAFT__STRING = WORKFLOW_DRAFT_PROVIDER___GET_DRAFT__STRING;

	/**
	 * The operation id for the '<em>Get Draft Content</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_DRAFT_CONTENT__STRING = WORKFLOW_DRAFT_PROVIDER___GET_DRAFT_CONTENT__STRING;

	/**
	 * The operation id for the '<em>Update Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPDATE_DRAFT__STRING_EOBJECT = WORKFLOW_DRAFT_PROVIDER___UPDATE_DRAFT__STRING_EOBJECT;

	/**
	 * The operation id for the '<em>Delete Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___DELETE_DRAFT__STRING = WORKFLOW_DRAFT_PROVIDER___DELETE_DRAFT__STRING;

	/**
	 * The operation id for the '<em>Approve Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___APPROVE_OBJECT__STRING_STRING_STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Reject Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___REJECT_OBJECT__STRING_STRING_STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 1;

	/**
	 * The operation id for the '<em>Release Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___RELEASE_OBJECT__STRING_STRING_BOOLEAN = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 2;

	/**
	 * The operation id for the '<em>List Approved Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_APPROVED_OBJECTS = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 3;

	/**
	 * The operation id for the '<em>List Rejected Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_REJECTED_OBJECTS = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 4;

	/**
	 * The operation id for the '<em>List Released Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_RELEASED_OBJECTS = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 5;

	/**
	 * The operation id for the '<em>Get Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_OBJECT__STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 6;

	/**
	 * The operation id for the '<em>Get Object Content</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_OBJECT_CONTENT__STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 7;

	/**
	 * The operation id for the '<em>Update Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPDATE_OBJECT__STRING_EOBJECT = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 8;

	/**
	 * The operation id for the '<em>Delete Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___DELETE_OBJECT__STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 9;

	/**
	 * The number of operations of the '<em>EObject Workflow Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE_OPERATION_COUNT = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 10;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService <em>EObject Workflow Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EObject Workflow Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService
	 * @generated
	 */
	EClass getEObjectWorkflowService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#approveObject(java.lang.String, java.lang.String, java.lang.String) <em>Approve Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Approve Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#approveObject(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ApproveObject__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#rejectObject(java.lang.String, java.lang.String, java.lang.String) <em>Reject Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Reject Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#rejectObject(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__RejectObject__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#releaseObject(java.lang.String, java.lang.String, boolean) <em>Release Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Release Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#releaseObject(java.lang.String, java.lang.String, boolean)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ReleaseObject__String_String_boolean();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listApprovedObjects() <em>List Approved Objects</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Approved Objects</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listApprovedObjects()
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListApprovedObjects();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listRejectedObjects() <em>List Rejected Objects</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Rejected Objects</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listRejectedObjects()
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListRejectedObjects();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listReleasedObjects() <em>List Released Objects</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Released Objects</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listReleasedObjects()
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListReleasedObjects();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getObject(java.lang.String) <em>Get Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getObject(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__GetObject__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getObjectContent(java.lang.String) <em>Get Object Content</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Object Content</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getObjectContent(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__GetObjectContent__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#updateObject(java.lang.String, org.eclipse.emf.ecore.EObject) <em>Update Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#updateObject(java.lang.String, org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__UpdateObject__String_EObject();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#deleteObject(java.lang.String) <em>Delete Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#deleteObject(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__DeleteObject__String();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider <em>Workflow Draft Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Workflow Draft Provider</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider
	 * @generated
	 */
	EClass getWorkflowDraftProvider();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#uploadDraft(org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata) <em>Upload Draft</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Upload Draft</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#uploadDraft(org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__UploadDraft__EObject_ObjectMetadata();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#listDraftObjects() <em>List Draft Objects</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Draft Objects</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#listDraftObjects()
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__ListDraftObjects();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#getDraft(java.lang.String) <em>Get Draft</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Draft</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#getDraft(java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__GetDraft__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#getDraftContent(java.lang.String) <em>Get Draft Content</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Draft Content</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#getDraftContent(java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__GetDraftContent__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#updateDraft(java.lang.String, org.eclipse.emf.ecore.EObject) <em>Update Draft</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Draft</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#updateDraft(java.lang.String, org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__UpdateDraft__String_EObject();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#deleteDraft(java.lang.String) <em>Delete Draft</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete Draft</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider#deleteDraft(java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__DeleteDraft__String();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	WorkflowApiFactory getWorkflowApiFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService <em>EObject Workflow Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getEObjectWorkflowService()
		 * @generated
		 */
		EClass EOBJECT_WORKFLOW_SERVICE = eINSTANCE.getEObjectWorkflowService();

		/**
		 * The meta object literal for the '<em><b>Approve Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___APPROVE_OBJECT__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__ApproveObject__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Reject Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___REJECT_OBJECT__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__RejectObject__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Release Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___RELEASE_OBJECT__STRING_STRING_BOOLEAN = eINSTANCE.getEObjectWorkflowService__ReleaseObject__String_String_boolean();

		/**
		 * The meta object literal for the '<em><b>List Approved Objects</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_APPROVED_OBJECTS = eINSTANCE.getEObjectWorkflowService__ListApprovedObjects();

		/**
		 * The meta object literal for the '<em><b>List Rejected Objects</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_REJECTED_OBJECTS = eINSTANCE.getEObjectWorkflowService__ListRejectedObjects();

		/**
		 * The meta object literal for the '<em><b>List Released Objects</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_RELEASED_OBJECTS = eINSTANCE.getEObjectWorkflowService__ListReleasedObjects();

		/**
		 * The meta object literal for the '<em><b>Get Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___GET_OBJECT__STRING = eINSTANCE.getEObjectWorkflowService__GetObject__String();

		/**
		 * The meta object literal for the '<em><b>Get Object Content</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___GET_OBJECT_CONTENT__STRING = eINSTANCE.getEObjectWorkflowService__GetObjectContent__String();

		/**
		 * The meta object literal for the '<em><b>Update Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___UPDATE_OBJECT__STRING_EOBJECT = eINSTANCE.getEObjectWorkflowService__UpdateObject__String_EObject();

		/**
		 * The meta object literal for the '<em><b>Delete Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___DELETE_OBJECT__STRING = eINSTANCE.getEObjectWorkflowService__DeleteObject__String();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider <em>Workflow Draft Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getWorkflowDraftProvider()
		 * @generated
		 */
		EClass WORKFLOW_DRAFT_PROVIDER = eINSTANCE.getWorkflowDraftProvider();

		/**
		 * The meta object literal for the '<em><b>Upload Draft</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___UPLOAD_DRAFT__EOBJECT_OBJECTMETADATA = eINSTANCE.getWorkflowDraftProvider__UploadDraft__EObject_ObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>List Draft Objects</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___LIST_DRAFT_OBJECTS = eINSTANCE.getWorkflowDraftProvider__ListDraftObjects();

		/**
		 * The meta object literal for the '<em><b>Get Draft</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___GET_DRAFT__STRING = eINSTANCE.getWorkflowDraftProvider__GetDraft__String();

		/**
		 * The meta object literal for the '<em><b>Get Draft Content</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___GET_DRAFT_CONTENT__STRING = eINSTANCE.getWorkflowDraftProvider__GetDraftContent__String();

		/**
		 * The meta object literal for the '<em><b>Update Draft</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___UPDATE_DRAFT__STRING_EOBJECT = eINSTANCE.getWorkflowDraftProvider__UpdateDraft__String_EObject();

		/**
		 * The meta object literal for the '<em><b>Delete Draft</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___DELETE_DRAFT__STRING = eINSTANCE.getWorkflowDraftProvider__DeleteDraft__String();

	}

} //WorkflowApiPackage
