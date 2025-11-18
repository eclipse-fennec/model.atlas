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
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowDraftProvider;

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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass workflowDraftProviderEClass = null;

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
	public EOperation getEObjectWorkflowService__ApproveObject__String_String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__RejectObject__String_String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__ReleaseObject__String_String_boolean() {
		return eObjectWorkflowServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__ListApprovedObjects() {
		return eObjectWorkflowServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__ListRejectedObjects() {
		return eObjectWorkflowServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__ListReleasedObjects() {
		return eObjectWorkflowServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__GetObject__String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__GetObjectContent__String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__UpdateObject__String_EObject() {
		return eObjectWorkflowServiceEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__DeleteObject__String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getWorkflowDraftProvider() {
		return workflowDraftProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowDraftProvider__UploadDraft__EObject_ObjectMetadata() {
		return workflowDraftProviderEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowDraftProvider__ListDraftObjects() {
		return workflowDraftProviderEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowDraftProvider__GetDraft__String() {
		return workflowDraftProviderEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowDraftProvider__GetDraftContent__String() {
		return workflowDraftProviderEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowDraftProvider__UpdateDraft__String_EObject() {
		return workflowDraftProviderEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowDraftProvider__DeleteDraft__String() {
		return workflowDraftProviderEClass.getEOperations().get(5);
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
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___APPROVE_OBJECT__STRING_STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___REJECT_OBJECT__STRING_STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___RELEASE_OBJECT__STRING_STRING_BOOLEAN);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___LIST_APPROVED_OBJECTS);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___LIST_REJECTED_OBJECTS);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___LIST_RELEASED_OBJECTS);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___GET_OBJECT__STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___GET_OBJECT_CONTENT__STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___UPDATE_OBJECT__STRING_EOBJECT);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___DELETE_OBJECT__STRING);

		workflowDraftProviderEClass = createEClass(WORKFLOW_DRAFT_PROVIDER);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___UPLOAD_DRAFT__EOBJECT_OBJECTMETADATA);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___LIST_DRAFT_OBJECTS);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___GET_DRAFT__STRING);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___GET_DRAFT_CONTENT__STRING);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___UPDATE_DRAFT__STRING_EOBJECT);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___DELETE_DRAFT__STRING);
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
		ETypeParameter workflowDraftProviderEClass_T = addETypeParameter(workflowDraftProviderEClass, "T");

		// Set bounds for type parameters
		EGenericType g1 = createEGenericType(ecorePackage.getEObject());
		eObjectWorkflowServiceEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(ecorePackage.getEObject());
		workflowDraftProviderEClass_T.getEBounds().add(g1);

		// Add supertypes to classes
		g1 = createEGenericType(this.getWorkflowDraftProvider());
		EGenericType g2 = createEGenericType(eObjectWorkflowServiceEClass_T);
		g1.getETypeArguments().add(g2);
		eObjectWorkflowServiceEClass.getEGenericSuperTypes().add(g1);

		// Initialize classes, features, and operations; add parameters
		initEClass(eObjectWorkflowServiceEClass, EObjectWorkflowService.class, "EObjectWorkflowService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		EOperation op = initEOperation(getEObjectWorkflowService__ApproveObject__String_String_String(), theManagementPackage.getObjectMetadata(), "approveObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "approvalReason", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectWorkflowService__RejectObject__String_String_String(), theManagementPackage.getObjectMetadata(), "rejectObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "rejectionReason", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectWorkflowService__ReleaseObject__String_String_boolean(), theManagementPackage.getObjectMetadata(), "releaseObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "releaseNotes", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "requireComplianceCheck", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectWorkflowService__ListApprovedObjects(), null, "listApprovedObjects", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__ListRejectedObjects(), null, "listRejectedObjects", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__ListReleasedObjects(), null, "listReleasedObjects", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__GetObject__String(), theManagementPackage.getObjectMetadata(), "getObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectWorkflowService__GetObjectContent__String(), ecorePackage.getEObject(), "getObjectContent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectWorkflowService__UpdateObject__String_EObject(), null, "updateObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		addEParameter(op, g1, "updatedObject", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getVoid());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__DeleteObject__String(), null, "deleteObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(workflowDraftProviderEClass, WorkflowDraftProvider.class, "WorkflowDraftProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getWorkflowDraftProvider__UploadDraft__EObject_ObjectMetadata(), null, "uploadDraft", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(workflowDraftProviderEClass_T);
		addEParameter(op, g1, "object", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectMetadata(), "metadata", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getWorkflowDraftProvider__ListDraftObjects(), null, "listDraftObjects", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getWorkflowDraftProvider__GetDraft__String(), theManagementPackage.getObjectMetadata(), "getDraft", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getWorkflowDraftProvider__GetDraftContent__String(), null, "getDraftContent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(workflowDraftProviderEClass_T);
		initEOperation(op, g1);

		op = initEOperation(getWorkflowDraftProvider__UpdateDraft__String_EObject(), null, "updateDraft", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(workflowDraftProviderEClass_T);
		addEParameter(op, g1, "updatedObject", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getVoid());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getWorkflowDraftProvider__DeleteDraft__String(), null, "deleteDraft", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		// Create resource
		createResource(eNS_URI);
	}

} //WorkflowApiPackageImpl
