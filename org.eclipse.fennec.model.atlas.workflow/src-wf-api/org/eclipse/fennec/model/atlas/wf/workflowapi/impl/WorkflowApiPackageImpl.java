/**
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
package org.eclipse.fennec.model.atlas.wf.workflowapi.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.ETypeParameter;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;

import org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class WorkflowApiPackageImpl extends EPackageImpl implements WorkflowApiPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eObjectWorkflowServiceEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private WorkflowApiPackageImpl() {
		super(eNS_URI, WorkflowApiFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link WorkflowApiPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static WorkflowApiPackage init() {
		if (isInited) return (WorkflowApiPackage)EPackage.Registry.INSTANCE.getEPackage(WorkflowApiPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredWorkflowApiPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		WorkflowApiPackageImpl theWorkflowApiPackage = registeredWorkflowApiPackage instanceof WorkflowApiPackageImpl ? (WorkflowApiPackageImpl)registeredWorkflowApiPackage : new WorkflowApiPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		ManagementPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theWorkflowApiPackage.createPackageContents();

		// Initialize created meta-data
		theWorkflowApiPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theWorkflowApiPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(WorkflowApiPackage.eNS_URI, theWorkflowApiPackage);
		return theWorkflowApiPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEObjectWorkflowService() {
		return eObjectWorkflowServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__UploadToStage__String_EObject_ObjectMetadata() {
		return eObjectWorkflowServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__GetFromStage__String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__GetFromFinalStage__String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__GetContentFromStage__String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__UpdateInStage__String_EObject_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__DeleteFromStage__String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__ListInStage__String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__ListInFinalStage() {
		return eObjectWorkflowServiceEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__TransitionToStage__String_String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__IsTransitionAllowed__String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public WorkflowApiFactory getWorkflowApiFactory() {
		return (WorkflowApiFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		eObjectWorkflowServiceEClass = createEClass(EOBJECT_WORKFLOW_SERVICE);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___UPLOAD_TO_STAGE__STRING_EOBJECT_OBJECTMETADATA);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___GET_FROM_STAGE__STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___GET_FROM_FINAL_STAGE__STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___GET_CONTENT_FROM_STAGE__STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___UPDATE_IN_STAGE__STRING_EOBJECT_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___DELETE_FROM_STAGE__STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE__STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___LIST_IN_FINAL_STAGE);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___TRANSITION_TO_STAGE__STRING_STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		ManagementPackage theManagementPackage = (ManagementPackage)EPackage.Registry.INSTANCE.getEPackage(ManagementPackage.eNS_URI);

		// Create type parameters
		ETypeParameter eObjectWorkflowServiceEClass_T = addETypeParameter(eObjectWorkflowServiceEClass, "T");

		// Set bounds for type parameters
		EGenericType g1 = createEGenericType(ecorePackage.getEObject());
		eObjectWorkflowServiceEClass_T.getEBounds().add(g1);

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(eObjectWorkflowServiceEClass, EObjectWorkflowService.class, "EObjectWorkflowService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		EOperation op = initEOperation(getEObjectWorkflowService__UploadToStage__String_EObject_ObjectMetadata(), null, "uploadToStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		addEParameter(op, g1, "object", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectMetadata(), "metadata", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		EGenericType g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__GetFromStage__String_String(), theManagementPackage.getObjectMetadata(), "getFromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectWorkflowService__GetFromFinalStage__String(), theManagementPackage.getObjectMetadata(), "getFromFinalStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectWorkflowService__GetContentFromStage__String_String(), null, "getContentFromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__UpdateInStage__String_EObject_String(), null, "updateInStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		addEParameter(op, g1, "updatedObject", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getVoid());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__DeleteFromStage__String_String(), null, "deleteFromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__ListInStage__String(), null, "listInStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__ListInFinalStage(), null, "listInFinalStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__TransitionToStage__String_String_String(), theManagementPackage.getObjectMetadata(), "transitionToStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "fromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "toStage", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectWorkflowService__IsTransitionAllowed__String_String(), ecorePackage.getEBoolean(), "isTransitionAllowed", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "fromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "toStage", 0, 1, IS_UNIQUE, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //WorkflowApiPackageImpl
