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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.wf.workflowapi.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.ETypeParameter;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;

import org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Stage;
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass registryServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass scopeServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stageEClass = null;

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
	public EOperation getEObjectWorkflowService__UploadToStageForRegistry__String_String_EObject_ObjectMetadata() {
		return eObjectWorkflowServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__GetFromStageForRegistry__String_String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__GetFromFinalStageForRegistry__String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__GetContentFromStageForRegistry__String_String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__UpdateInStageForRegistry__String_String_EObject_String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__DeleteFromStageForRegistry__String_String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__ListInStageForRegistry__String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__ListInStageForRegistryByName__String_String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__ListInFinalStageForRegistry__String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__TransitionToStageForRegistry__String_String_String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectWorkflowService__IsTransitionAllowed__String_String() {
		return eObjectWorkflowServiceEClass.getEOperations().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRegistryService() {
		return registryServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__UploadToStage__String_String_EObject_ObjectMetadata() {
		return registryServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__GetMetadataFromStage__String_String_String() {
		return registryServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__GetMetadataFromFinalStage__String_String() {
		return registryServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__GetContentFromStage__String_String_String() {
		return registryServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__UpdateInStage__String_String_EObject_String_String() {
		return registryServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__DeleteFromStage__String_String_String() {
		return registryServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__ListInStage__String_String() {
		return registryServiceEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__ListInStageByName__String_String_String() {
		return registryServiceEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__ListInFinalStage__String() {
		return registryServiceEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__TransitionToStage__String_String_String_String() {
		return registryServiceEClass.getEOperations().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__GetRegistryName() {
		return registryServiceEClass.getEOperations().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__IsValidStage__String() {
		return registryServiceEClass.getEOperations().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__IsWritableStage__String() {
		return registryServiceEClass.getEOperations().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__IsFinalStageWritable() {
		return registryServiceEClass.getEOperations().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryService__IsTransitionAllowed__String_String() {
		return registryServiceEClass.getEOperations().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getScopeService() {
		return scopeServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__UploadToStageForRegistry__String_String_EObject_ObjectMetadata() {
		return scopeServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__GetMetadataFromStageForRegistry__String_String_String() {
		return scopeServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__GetMetadataFromFinalStageForRegistry__String_String() {
		return scopeServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__GetContentFromStageForRegistry__String_String_String() {
		return scopeServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__UpdateInStageForRegistry__String_String_EObject_String_String() {
		return scopeServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__DeleteFromStageForRegistry__String_String_String() {
		return scopeServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__ListInStageForRegistry__String_String() {
		return scopeServiceEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__ListInStageForRegistryByName__String_String_String() {
		return scopeServiceEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__ListInFinalStageForRegistry__String() {
		return scopeServiceEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__TransitionToStageForRegistry__String_String_String_String() {
		return scopeServiceEClass.getEOperations().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__IsValidRegistry__String() {
		return scopeServiceEClass.getEOperations().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getScopeService__GetAllRegistries() {
		return scopeServiceEClass.getEOperations().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getStage() {
		return stageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStage_Name() {
		return (EAttribute)stageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStage_Writable() {
		return (EAttribute)stageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStage_Final() {
		return (EAttribute)stageEClass.getEStructuralFeatures().get(2);
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
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___UPLOAD_TO_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_OBJECTMETADATA);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___GET_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___GET_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___GET_CONTENT_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___UPDATE_IN_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___DELETE_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE_FOR_REGISTRY__STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE_FOR_REGISTRY_BY_NAME__STRING_STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___LIST_IN_FINAL_STAGE_FOR_REGISTRY__STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___TRANSITION_TO_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING);
		createEOperation(eObjectWorkflowServiceEClass, EOBJECT_WORKFLOW_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING);

		registryServiceEClass = createEClass(REGISTRY_SERVICE);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___UPLOAD_TO_STAGE__STRING_STRING_EOBJECT_OBJECTMETADATA);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___GET_METADATA_FROM_STAGE__STRING_STRING_STRING);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___GET_METADATA_FROM_FINAL_STAGE__STRING_STRING);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___GET_CONTENT_FROM_STAGE__STRING_STRING_STRING);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___UPDATE_IN_STAGE__STRING_STRING_EOBJECT_STRING_STRING);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___DELETE_FROM_STAGE__STRING_STRING_STRING);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___LIST_IN_STAGE__STRING_STRING);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___LIST_IN_STAGE_BY_NAME__STRING_STRING_STRING);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___LIST_IN_FINAL_STAGE__STRING);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___TRANSITION_TO_STAGE__STRING_STRING_STRING_STRING);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___GET_REGISTRY_NAME);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___IS_VALID_STAGE__STRING);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___IS_WRITABLE_STAGE__STRING);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___IS_FINAL_STAGE_WRITABLE);
		createEOperation(registryServiceEClass, REGISTRY_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING);

		scopeServiceEClass = createEClass(SCOPE_SERVICE);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___UPLOAD_TO_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_OBJECTMETADATA);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___GET_METADATA_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___GET_METADATA_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___GET_CONTENT_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___UPDATE_IN_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_STRING_STRING);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___DELETE_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY__STRING_STRING);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY_BY_NAME__STRING_STRING_STRING);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___LIST_IN_FINAL_STAGE_FOR_REGISTRY__STRING);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___TRANSITION_TO_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___IS_VALID_REGISTRY__STRING);
		createEOperation(scopeServiceEClass, SCOPE_SERVICE___GET_ALL_REGISTRIES);

		stageEClass = createEClass(STAGE);
		createEAttribute(stageEClass, STAGE__NAME);
		createEAttribute(stageEClass, STAGE__WRITABLE);
		createEAttribute(stageEClass, STAGE__FINAL);
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
		ETypeParameter registryServiceEClass_T = addETypeParameter(registryServiceEClass, "T");
		ETypeParameter scopeServiceEClass_T = addETypeParameter(scopeServiceEClass, "T");

		// Set bounds for type parameters
		EGenericType g1 = createEGenericType(ecorePackage.getEObject());
		eObjectWorkflowServiceEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(ecorePackage.getEObject());
		registryServiceEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(ecorePackage.getEObject());
		scopeServiceEClass_T.getEBounds().add(g1);

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(eObjectWorkflowServiceEClass, EObjectWorkflowService.class, "EObjectWorkflowService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		EOperation op = initEOperation(getEObjectWorkflowService__UploadToStageForRegistry__String_String_EObject_ObjectMetadata(), null, "uploadToStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		addEParameter(op, g1, "object", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectMetadata(), "metadata", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		EGenericType g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__GetFromStageForRegistry__String_String_String(), theManagementPackage.getObjectMetadata(), "getFromStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectWorkflowService__GetFromFinalStageForRegistry__String_String(), theManagementPackage.getObjectMetadata(), "getFromFinalStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectWorkflowService__GetContentFromStageForRegistry__String_String_String(), null, "getContentFromStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__UpdateInStageForRegistry__String_String_EObject_String_String(), null, "updateInStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		addEParameter(op, g1, "updatedObject", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "version", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__DeleteFromStageForRegistry__String_String_String(), null, "deleteFromStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__ListInStageForRegistry__String_String(), null, "listInStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__ListInStageForRegistryByName__String_String_String(), null, "listInStageForRegistryByName", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "name", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__ListInFinalStageForRegistry__String(), null, "listInFinalStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectWorkflowService__TransitionToStageForRegistry__String_String_String_String(), theManagementPackage.getObjectMetadata(), "transitionToStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "fromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "toStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectWorkflowService__IsTransitionAllowed__String_String(), ecorePackage.getEBoolean(), "isTransitionAllowed", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "fromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "toStage", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(registryServiceEClass, RegistryService.class, "RegistryService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getRegistryService__UploadToStage__String_String_EObject_ObjectMetadata(), null, "uploadToStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		addEParameter(op, g1, "object", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectMetadata(), "metadata", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getRegistryService__GetMetadataFromStage__String_String_String(), theManagementPackage.getObjectMetadata(), "getMetadataFromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryService__GetMetadataFromFinalStage__String_String(), theManagementPackage.getObjectMetadata(), "getMetadataFromFinalStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryService__GetContentFromStage__String_String_String(), null, "getContentFromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		initEOperation(op, g1);

		op = initEOperation(getRegistryService__UpdateInStage__String_String_EObject_String_String(), null, "updateInStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		addEParameter(op, g1, "updatedObject", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "version", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getRegistryService__DeleteFromStage__String_String_String(), null, "deleteFromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getRegistryService__ListInStage__String_String(), null, "listInStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getRegistryService__ListInStageByName__String_String_String(), null, "listInStageByName", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "name", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getRegistryService__ListInFinalStage__String(), null, "listInFinalStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getRegistryService__TransitionToStage__String_String_String_String(), theManagementPackage.getObjectMetadata(), "transitionToStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "fromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "toStage", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getRegistryService__GetRegistryName(), ecorePackage.getEString(), "getRegistryName", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryService__IsValidStage__String(), ecorePackage.getEBoolean(), "isValidStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stageName", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryService__IsWritableStage__String(), ecorePackage.getEBoolean(), "isWritableStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stageName", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getRegistryService__IsFinalStageWritable(), ecorePackage.getEBoolean(), "isFinalStageWritable", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryService__IsTransitionAllowed__String_String(), ecorePackage.getEBoolean(), "isTransitionAllowed", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "fromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "toStage", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(scopeServiceEClass, ScopeService.class, "ScopeService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getScopeService__UploadToStageForRegistry__String_String_EObject_ObjectMetadata(), null, "uploadToStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		addEParameter(op, g1, "object", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectMetadata(), "metadata", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getScopeService__GetMetadataFromStageForRegistry__String_String_String(), theManagementPackage.getObjectMetadata(), "getMetadataFromStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getScopeService__GetMetadataFromFinalStageForRegistry__String_String(), theManagementPackage.getObjectMetadata(), "getMetadataFromFinalStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getScopeService__GetContentFromStageForRegistry__String_String_String(), null, "getContentFromStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		initEOperation(op, g1);

		op = initEOperation(getScopeService__UpdateInStageForRegistry__String_String_EObject_String_String(), null, "updateInStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectWorkflowServiceEClass_T);
		addEParameter(op, g1, "updatedObject", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "version", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getScopeService__DeleteFromStageForRegistry__String_String_String(), null, "deleteFromStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getScopeService__ListInStageForRegistry__String_String(), null, "listInStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getScopeService__ListInStageForRegistryByName__String_String_String(), null, "listInStageForRegistryByName", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "name", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getScopeService__ListInFinalStageForRegistry__String(), null, "listInFinalStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getScopeService__TransitionToStageForRegistry__String_String_String_String(), theManagementPackage.getObjectMetadata(), "transitionToStageForRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "fromStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "toStage", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getScopeService__IsValidRegistry__String(), ecorePackage.getEBoolean(), "isValidRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registryName", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getScopeService__GetAllRegistries(), null, "getAllRegistries", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(stageEClass, Stage.class, "Stage", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStage_Name(), ecorePackage.getEString(), "name", null, 0, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStage_Writable(), ecorePackage.getEBoolean(), "writable", null, 0, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStage_Final(), ecorePackage.getEBoolean(), "final", null, 0, 1, Stage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //WorkflowApiPackageImpl
