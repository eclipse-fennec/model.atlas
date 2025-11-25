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
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider <em>Workflow Stage Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getWorkflowStageProvider()
	 * @generated
	 */
	int WORKFLOW_STAGE_PROVIDER = 1;

	/**
	 * The number of structural features of the '<em>Workflow Stage Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_STAGE_PROVIDER_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Upload To Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_STAGE_PROVIDER___UPLOAD_TO_STAGE__STRING_EOBJECT_OBJECTMETADATA = 0;

	/**
	 * The operation id for the '<em>Get From Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_STAGE_PROVIDER___GET_FROM_STAGE__STRING_STRING = 1;

	/**
	 * The operation id for the '<em>Get Content From Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_STAGE_PROVIDER___GET_CONTENT_FROM_STAGE__STRING_STRING = 2;

	/**
	 * The operation id for the '<em>Update In Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_STAGE_PROVIDER___UPDATE_IN_STAGE__STRING_EOBJECT_STRING = 3;

	/**
	 * The operation id for the '<em>Delete From Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_STAGE_PROVIDER___DELETE_FROM_STAGE__STRING_STRING = 4;

	/**
	 * The operation id for the '<em>List In Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_STAGE_PROVIDER___LIST_IN_STAGE__STRING = 5;

	/**
	 * The number of operations of the '<em>Workflow Stage Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_STAGE_PROVIDER_OPERATION_COUNT = 6;

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
	int EOBJECT_WORKFLOW_SERVICE_FEATURE_COUNT = WORKFLOW_STAGE_PROVIDER_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Upload To Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPLOAD_TO_STAGE__STRING_EOBJECT_OBJECTMETADATA = WORKFLOW_STAGE_PROVIDER___UPLOAD_TO_STAGE__STRING_EOBJECT_OBJECTMETADATA;

	/**
	 * The operation id for the '<em>Get From Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_FROM_STAGE__STRING_STRING = WORKFLOW_STAGE_PROVIDER___GET_FROM_STAGE__STRING_STRING;

	/**
	 * The operation id for the '<em>Get Content From Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_CONTENT_FROM_STAGE__STRING_STRING = WORKFLOW_STAGE_PROVIDER___GET_CONTENT_FROM_STAGE__STRING_STRING;

	/**
	 * The operation id for the '<em>Update In Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPDATE_IN_STAGE__STRING_EOBJECT_STRING = WORKFLOW_STAGE_PROVIDER___UPDATE_IN_STAGE__STRING_EOBJECT_STRING;

	/**
	 * The operation id for the '<em>Delete From Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___DELETE_FROM_STAGE__STRING_STRING = WORKFLOW_STAGE_PROVIDER___DELETE_FROM_STAGE__STRING_STRING;

	/**
	 * The operation id for the '<em>List In Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE__STRING = WORKFLOW_STAGE_PROVIDER___LIST_IN_STAGE__STRING;

	/**
	 * The operation id for the '<em>Transition To Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___TRANSITION_TO_STAGE__STRING_STRING_STRING = WORKFLOW_STAGE_PROVIDER_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Is Transition Allowed</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING = WORKFLOW_STAGE_PROVIDER_OPERATION_COUNT + 1;

	/**
	 * The number of operations of the '<em>EObject Workflow Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE_OPERATION_COUNT = WORKFLOW_STAGE_PROVIDER_OPERATION_COUNT + 2;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService <em>Workflow Transition Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getWorkflowTransitionService()
	 * @generated
	 */
	int WORKFLOW_TRANSITION_SERVICE = 2;

	/**
	 * The number of structural features of the '<em>Workflow Transition Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_TRANSITION_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Transition To Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_TRANSITION_SERVICE___TRANSITION_TO_STAGE__STRING_STRING_STRING = 0;

	/**
	 * The operation id for the '<em>Is Transition Allowed</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_TRANSITION_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING = 1;

	/**
	 * The number of operations of the '<em>Workflow Transition Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_TRANSITION_SERVICE_OPERATION_COUNT = 2;


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
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider <em>Workflow Stage Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Workflow Stage Provider</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider
	 * @generated
	 */
	EClass getWorkflowStageProvider();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#uploadToStage(java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata) <em>Upload To Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Upload To Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#uploadToStage(java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 * @generated
	 */
	EOperation getWorkflowStageProvider__UploadToStage__String_EObject_ObjectMetadata();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#getFromStage(java.lang.String, java.lang.String) <em>Get From Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get From Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#getFromStage(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowStageProvider__GetFromStage__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#getContentFromStage(java.lang.String, java.lang.String) <em>Get Content From Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Content From Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#getContentFromStage(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowStageProvider__GetContentFromStage__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#updateInStage(java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String) <em>Update In Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update In Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#updateInStage(java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowStageProvider__UpdateInStage__String_EObject_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#deleteFromStage(java.lang.String, java.lang.String) <em>Delete From Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete From Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#deleteFromStage(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowStageProvider__DeleteFromStage__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#listInStage(java.lang.String) <em>List In Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider#listInStage(java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowStageProvider__ListInStage__String();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService <em>Workflow Transition Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Workflow Transition Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService
	 * @generated
	 */
	EClass getWorkflowTransitionService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService#transitionToStage(java.lang.String, java.lang.String, java.lang.String) <em>Transition To Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Transition To Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService#transitionToStage(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowTransitionService__TransitionToStage__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService#isTransitionAllowed(java.lang.String, java.lang.String) <em>Is Transition Allowed</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Transition Allowed</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService#isTransitionAllowed(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowTransitionService__IsTransitionAllowed__String_String();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider <em>Workflow Stage Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowStageProvider
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getWorkflowStageProvider()
		 * @generated
		 */
		EClass WORKFLOW_STAGE_PROVIDER = eINSTANCE.getWorkflowStageProvider();

		/**
		 * The meta object literal for the '<em><b>Upload To Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_STAGE_PROVIDER___UPLOAD_TO_STAGE__STRING_EOBJECT_OBJECTMETADATA = eINSTANCE.getWorkflowStageProvider__UploadToStage__String_EObject_ObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>Get From Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_STAGE_PROVIDER___GET_FROM_STAGE__STRING_STRING = eINSTANCE.getWorkflowStageProvider__GetFromStage__String_String();

		/**
		 * The meta object literal for the '<em><b>Get Content From Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_STAGE_PROVIDER___GET_CONTENT_FROM_STAGE__STRING_STRING = eINSTANCE.getWorkflowStageProvider__GetContentFromStage__String_String();

		/**
		 * The meta object literal for the '<em><b>Update In Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_STAGE_PROVIDER___UPDATE_IN_STAGE__STRING_EOBJECT_STRING = eINSTANCE.getWorkflowStageProvider__UpdateInStage__String_EObject_String();

		/**
		 * The meta object literal for the '<em><b>Delete From Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_STAGE_PROVIDER___DELETE_FROM_STAGE__STRING_STRING = eINSTANCE.getWorkflowStageProvider__DeleteFromStage__String_String();

		/**
		 * The meta object literal for the '<em><b>List In Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_STAGE_PROVIDER___LIST_IN_STAGE__STRING = eINSTANCE.getWorkflowStageProvider__ListInStage__String();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService <em>Workflow Transition Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowTransitionService
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getWorkflowTransitionService()
		 * @generated
		 */
		EClass WORKFLOW_TRANSITION_SERVICE = eINSTANCE.getWorkflowTransitionService();

		/**
		 * The meta object literal for the '<em><b>Transition To Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_TRANSITION_SERVICE___TRANSITION_TO_STAGE__STRING_STRING_STRING = eINSTANCE.getWorkflowTransitionService__TransitionToStage__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Is Transition Allowed</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_TRANSITION_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING = eINSTANCE.getWorkflowTransitionService__IsTransitionAllowed__String_String();

	}

} //WorkflowApiPackage
