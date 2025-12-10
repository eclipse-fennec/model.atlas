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
	int EOBJECT_WORKFLOW_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Upload To Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPLOAD_TO_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_OBJECTMETADATA = 0;

	/**
	 * The operation id for the '<em>Get From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = 1;

	/**
	 * The operation id for the '<em>Get From Final Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING = 2;

	/**
	 * The operation id for the '<em>Get Content From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_CONTENT_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = 3;

	/**
	 * The operation id for the '<em>Update In Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPDATE_IN_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_STRING_STRING = 4;

	/**
	 * The operation id for the '<em>Delete From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___DELETE_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = 5;

	/**
	 * The operation id for the '<em>List In Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE_FOR_REGISTRY__STRING_STRING = 6;

	/**
	 * The operation id for the '<em>List In Stage For Registry By Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE_FOR_REGISTRY_BY_NAME__STRING_STRING_STRING = 7;

	/**
	 * The operation id for the '<em>List In Final Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_IN_FINAL_STAGE_FOR_REGISTRY__STRING = 8;

	/**
	 * The operation id for the '<em>Transition To Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___TRANSITION_TO_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING = 9;

	/**
	 * The operation id for the '<em>Is Transition Allowed</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING = 10;

	/**
	 * The number of operations of the '<em>EObject Workflow Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE_OPERATION_COUNT = 11;


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
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#uploadToStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata) <em>Upload To Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Upload To Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#uploadToStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__UploadToStageForRegistry__String_String_EObject_ObjectMetadata();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String) <em>Get From Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get From Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__GetFromStageForRegistry__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromFinalStageForRegistry(java.lang.String, java.lang.String) <em>Get From Final Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get From Final Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromFinalStageForRegistry(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__GetFromFinalStageForRegistry__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getContentFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String) <em>Get Content From Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Content From Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getContentFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__GetContentFromStageForRegistry__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#updateInStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String) <em>Update In Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update In Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#updateInStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__UpdateInStageForRegistry__String_String_EObject_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#deleteFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String) <em>Delete From Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete From Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#deleteFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__DeleteFromStageForRegistry__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageForRegistry(java.lang.String, java.lang.String) <em>List In Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageForRegistry(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListInStageForRegistry__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageForRegistryByName(java.lang.String, java.lang.String, java.lang.String) <em>List In Stage For Registry By Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Stage For Registry By Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageForRegistryByName(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListInStageForRegistryByName__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInFinalStageForRegistry(java.lang.String) <em>List In Final Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Final Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInFinalStageForRegistry(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListInFinalStageForRegistry__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#transitionToStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.lang.String) <em>Transition To Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Transition To Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#transitionToStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__TransitionToStageForRegistry__String_String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#isTransitionAllowed(java.lang.String, java.lang.String) <em>Is Transition Allowed</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Transition Allowed</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#isTransitionAllowed(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__IsTransitionAllowed__String_String();

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
		 * The meta object literal for the '<em><b>Upload To Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___UPLOAD_TO_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_OBJECTMETADATA = eINSTANCE.getEObjectWorkflowService__UploadToStageForRegistry__String_String_EObject_ObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>Get From Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___GET_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__GetFromStageForRegistry__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Get From Final Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___GET_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING = eINSTANCE.getEObjectWorkflowService__GetFromFinalStageForRegistry__String_String();

		/**
		 * The meta object literal for the '<em><b>Get Content From Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___GET_CONTENT_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__GetContentFromStageForRegistry__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Update In Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___UPDATE_IN_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_STRING_STRING = eINSTANCE.getEObjectWorkflowService__UpdateInStageForRegistry__String_String_EObject_String_String();

		/**
		 * The meta object literal for the '<em><b>Delete From Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___DELETE_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__DeleteFromStageForRegistry__String_String_String();

		/**
		 * The meta object literal for the '<em><b>List In Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE_FOR_REGISTRY__STRING_STRING = eINSTANCE.getEObjectWorkflowService__ListInStageForRegistry__String_String();

		/**
		 * The meta object literal for the '<em><b>List In Stage For Registry By Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE_FOR_REGISTRY_BY_NAME__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__ListInStageForRegistryByName__String_String_String();

		/**
		 * The meta object literal for the '<em><b>List In Final Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_IN_FINAL_STAGE_FOR_REGISTRY__STRING = eINSTANCE.getEObjectWorkflowService__ListInFinalStageForRegistry__String();

		/**
		 * The meta object literal for the '<em><b>Transition To Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___TRANSITION_TO_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__TransitionToStageForRegistry__String_String_String_String();

		/**
		 * The meta object literal for the '<em><b>Is Transition Allowed</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING = eINSTANCE.getEObjectWorkflowService__IsTransitionAllowed__String_String();

	}

} //WorkflowApiPackage
