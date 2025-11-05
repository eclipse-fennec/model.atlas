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
package org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.ETypeParameter;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.governance.GovernancePackage;

import org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.ManagementApiFactory;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.ManagementApiPackage;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceConfig;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider;
import org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider;

import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ManagementApiPackageImpl extends EPackageImpl implements ManagementApiPackage {
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
	private EClass systemComponentGovernanceServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass governanceComplianceServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass governanceSystemRepositoryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass systemComponentGovernanceConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass workflowDraftProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass workflowComplianceProviderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass governanceDocumentationServiceEClass = null;

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
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.ManagementApiPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ManagementApiPackageImpl() {
		super(eNS_URI, ManagementApiFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ManagementApiPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ManagementApiPackage init() {
		if (isInited) return (ManagementApiPackage)EPackage.Registry.INSTANCE.getEPackage(ManagementApiPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredManagementApiPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ManagementApiPackageImpl theManagementApiPackage = registeredManagementApiPackage instanceof ManagementApiPackageImpl ? (ManagementApiPackageImpl)registeredManagementApiPackage : new ManagementApiPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		GovernancePackage.eINSTANCE.eClass();
		ManagementPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theManagementApiPackage.createPackageContents();

		// Initialize created meta-data
		theManagementApiPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theManagementApiPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ManagementApiPackage.eNS_URI, theManagementApiPackage);
		return theManagementApiPackage;
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
	public EClass getSystemComponentGovernanceService() {
		return systemComponentGovernanceServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemComponentGovernanceService__RegisterSystemComponent__String_SystemComponentType_String_String() {
		return systemComponentGovernanceServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemComponentGovernanceService__UpdateSystemComponentTrustLevel__String_TrustLevel() {
		return systemComponentGovernanceServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemComponentGovernanceService__RunSystemComponentCompliance__String_List_String() {
		return systemComponentGovernanceServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemComponentGovernanceService__ApplyPoliciesToSystemComponent__String_List() {
		return systemComponentGovernanceServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemComponentGovernanceService__GetSystemComponent__String() {
		return systemComponentGovernanceServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemComponentGovernanceService__FindSystemComponentsByGovernanceStatus__ComplianceStatus() {
		return systemComponentGovernanceServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemComponentGovernanceService__LinkModelToSystemComponent__String_String() {
		return systemComponentGovernanceServiceEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemComponentGovernanceService__GetSystemComponentsByType__SystemComponentType() {
		return systemComponentGovernanceServiceEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemComponentGovernanceService__GetSystemId() {
		return systemComponentGovernanceServiceEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGovernanceComplianceService() {
		return governanceComplianceServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceComplianceService__RunComplianceChecks__String_String_String() {
		return governanceComplianceServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceComplianceService__RunSpecificComplianceChecks__String_List_String() {
		return governanceComplianceServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceComplianceService__RunSystemComponentCompliance__String_String_List_String() {
		return governanceComplianceServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceComplianceService__GetComplianceStatus__String() {
		return governanceComplianceServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceComplianceService__ValidatePolicyCompliance__String_PolicyType_String() {
		return governanceComplianceServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceComplianceService__GetAvailablePolicies() {
		return governanceComplianceServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGovernanceSystemRepository() {
		return governanceSystemRepositoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceSystemRepository__GetGovernanceSystem__String() {
		return governanceSystemRepositoryEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceSystemRepository__CreateSystemComponent__String_String_SystemComponentType_String_String() {
		return governanceSystemRepositoryEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceSystemRepository__GetSystemComponent__String_String() {
		return governanceSystemRepositoryEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceSystemRepository__UpdateSystemComponentTrustLevel__String_String_TrustLevel() {
		return governanceSystemRepositoryEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceSystemRepository__ApplyPoliciesToSystemComponent__String_String_List() {
		return governanceSystemRepositoryEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceSystemRepository__FindSystemComponentsByComplianceStatus__String_ComplianceStatus() {
		return governanceSystemRepositoryEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceSystemRepository__GetSystemComponentsByType__String_SystemComponentType() {
		return governanceSystemRepositoryEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceSystemRepository__LinkModelToSystemComponent__String_String_String() {
		return governanceSystemRepositoryEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSystemComponentGovernanceConfig() {
		return systemComponentGovernanceConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemComponentGovernanceConfig__SystemId() {
		return systemComponentGovernanceConfigEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getSystemComponentGovernanceConfig__Description() {
		return systemComponentGovernanceConfigEClass.getEOperations().get(1);
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
	public EOperation getWorkflowDraftProvider__CheckDraft__String_String_String() {
		return workflowDraftProviderEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getWorkflowComplianceProvider() {
		return workflowComplianceProviderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowComplianceProvider__CheckObject__String_String_String() {
		return workflowComplianceProviderEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowComplianceProvider__RunComplianceChecks__String_List_String() {
		return workflowComplianceProviderEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowComplianceProvider__GetComplianceStatus__String() {
		return workflowComplianceProviderEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowComplianceProvider__SetGovernanceDocumentationDraft__String_String_String() {
		return workflowComplianceProviderEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowComplianceProvider__SetGovernanceDocumentationInReview__String_String_String() {
		return workflowComplianceProviderEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowComplianceProvider__SetGovernanceDocumentationApproved__String_String_String() {
		return workflowComplianceProviderEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWorkflowComplianceProvider__SetGovernanceDocumentationRejected__String_String_String() {
		return workflowComplianceProviderEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGovernanceDocumentationService() {
		return governanceDocumentationServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceDocumentationService__StoreDocumentation__String_GovernanceDocumentation_String_String() {
		return governanceDocumentationServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceDocumentationService__GetLatestDocumentation__String() {
		return governanceDocumentationServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceDocumentationService__GetDocumentation__String() {
		return governanceDocumentationServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceDocumentationService__GetDocumentationHistory__String() {
		return governanceDocumentationServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceDocumentationService__HasDocumentation__String() {
		return governanceDocumentationServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceDocumentationService__DeleteAllDocumentation__String() {
		return governanceDocumentationServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGovernanceDocumentationService__GetDocumentationStatistics() {
		return governanceDocumentationServiceEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ManagementApiFactory getManagementApiFactory() {
		return (ManagementApiFactory)getEFactoryInstance();
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

		systemComponentGovernanceServiceEClass = createEClass(SYSTEM_COMPONENT_GOVERNANCE_SERVICE);
		createEOperation(systemComponentGovernanceServiceEClass, SYSTEM_COMPONENT_GOVERNANCE_SERVICE___REGISTER_SYSTEM_COMPONENT__STRING_SYSTEMCOMPONENTTYPE_STRING_STRING);
		createEOperation(systemComponentGovernanceServiceEClass, SYSTEM_COMPONENT_GOVERNANCE_SERVICE___UPDATE_SYSTEM_COMPONENT_TRUST_LEVEL__STRING_TRUSTLEVEL);
		createEOperation(systemComponentGovernanceServiceEClass, SYSTEM_COMPONENT_GOVERNANCE_SERVICE___RUN_SYSTEM_COMPONENT_COMPLIANCE__STRING_LIST_STRING);
		createEOperation(systemComponentGovernanceServiceEClass, SYSTEM_COMPONENT_GOVERNANCE_SERVICE___APPLY_POLICIES_TO_SYSTEM_COMPONENT__STRING_LIST);
		createEOperation(systemComponentGovernanceServiceEClass, SYSTEM_COMPONENT_GOVERNANCE_SERVICE___GET_SYSTEM_COMPONENT__STRING);
		createEOperation(systemComponentGovernanceServiceEClass, SYSTEM_COMPONENT_GOVERNANCE_SERVICE___FIND_SYSTEM_COMPONENTS_BY_GOVERNANCE_STATUS__COMPLIANCESTATUS);
		createEOperation(systemComponentGovernanceServiceEClass, SYSTEM_COMPONENT_GOVERNANCE_SERVICE___LINK_MODEL_TO_SYSTEM_COMPONENT__STRING_STRING);
		createEOperation(systemComponentGovernanceServiceEClass, SYSTEM_COMPONENT_GOVERNANCE_SERVICE___GET_SYSTEM_COMPONENTS_BY_TYPE__SYSTEMCOMPONENTTYPE);
		createEOperation(systemComponentGovernanceServiceEClass, SYSTEM_COMPONENT_GOVERNANCE_SERVICE___GET_SYSTEM_ID);

		governanceComplianceServiceEClass = createEClass(GOVERNANCE_COMPLIANCE_SERVICE);
		createEOperation(governanceComplianceServiceEClass, GOVERNANCE_COMPLIANCE_SERVICE___RUN_COMPLIANCE_CHECKS__STRING_STRING_STRING);
		createEOperation(governanceComplianceServiceEClass, GOVERNANCE_COMPLIANCE_SERVICE___RUN_SPECIFIC_COMPLIANCE_CHECKS__STRING_LIST_STRING);
		createEOperation(governanceComplianceServiceEClass, GOVERNANCE_COMPLIANCE_SERVICE___RUN_SYSTEM_COMPONENT_COMPLIANCE__STRING_STRING_LIST_STRING);
		createEOperation(governanceComplianceServiceEClass, GOVERNANCE_COMPLIANCE_SERVICE___GET_COMPLIANCE_STATUS__STRING);
		createEOperation(governanceComplianceServiceEClass, GOVERNANCE_COMPLIANCE_SERVICE___VALIDATE_POLICY_COMPLIANCE__STRING_POLICYTYPE_STRING);
		createEOperation(governanceComplianceServiceEClass, GOVERNANCE_COMPLIANCE_SERVICE___GET_AVAILABLE_POLICIES);

		governanceSystemRepositoryEClass = createEClass(GOVERNANCE_SYSTEM_REPOSITORY);
		createEOperation(governanceSystemRepositoryEClass, GOVERNANCE_SYSTEM_REPOSITORY___GET_GOVERNANCE_SYSTEM__STRING);
		createEOperation(governanceSystemRepositoryEClass, GOVERNANCE_SYSTEM_REPOSITORY___CREATE_SYSTEM_COMPONENT__STRING_STRING_SYSTEMCOMPONENTTYPE_STRING_STRING);
		createEOperation(governanceSystemRepositoryEClass, GOVERNANCE_SYSTEM_REPOSITORY___GET_SYSTEM_COMPONENT__STRING_STRING);
		createEOperation(governanceSystemRepositoryEClass, GOVERNANCE_SYSTEM_REPOSITORY___UPDATE_SYSTEM_COMPONENT_TRUST_LEVEL__STRING_STRING_TRUSTLEVEL);
		createEOperation(governanceSystemRepositoryEClass, GOVERNANCE_SYSTEM_REPOSITORY___APPLY_POLICIES_TO_SYSTEM_COMPONENT__STRING_STRING_LIST);
		createEOperation(governanceSystemRepositoryEClass, GOVERNANCE_SYSTEM_REPOSITORY___FIND_SYSTEM_COMPONENTS_BY_COMPLIANCE_STATUS__STRING_COMPLIANCESTATUS);
		createEOperation(governanceSystemRepositoryEClass, GOVERNANCE_SYSTEM_REPOSITORY___GET_SYSTEM_COMPONENTS_BY_TYPE__STRING_SYSTEMCOMPONENTTYPE);
		createEOperation(governanceSystemRepositoryEClass, GOVERNANCE_SYSTEM_REPOSITORY___LINK_MODEL_TO_SYSTEM_COMPONENT__STRING_STRING_STRING);

		systemComponentGovernanceConfigEClass = createEClass(SYSTEM_COMPONENT_GOVERNANCE_CONFIG);
		createEOperation(systemComponentGovernanceConfigEClass, SYSTEM_COMPONENT_GOVERNANCE_CONFIG___SYSTEM_ID);
		createEOperation(systemComponentGovernanceConfigEClass, SYSTEM_COMPONENT_GOVERNANCE_CONFIG___DESCRIPTION);

		workflowDraftProviderEClass = createEClass(WORKFLOW_DRAFT_PROVIDER);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___UPLOAD_DRAFT__EOBJECT_OBJECTMETADATA);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___LIST_DRAFT_OBJECTS);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___GET_DRAFT__STRING);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___GET_DRAFT_CONTENT__STRING);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___UPDATE_DRAFT__STRING_EOBJECT);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___DELETE_DRAFT__STRING);
		createEOperation(workflowDraftProviderEClass, WORKFLOW_DRAFT_PROVIDER___CHECK_DRAFT__STRING_STRING_STRING);

		workflowComplianceProviderEClass = createEClass(WORKFLOW_COMPLIANCE_PROVIDER);
		createEOperation(workflowComplianceProviderEClass, WORKFLOW_COMPLIANCE_PROVIDER___CHECK_OBJECT__STRING_STRING_STRING);
		createEOperation(workflowComplianceProviderEClass, WORKFLOW_COMPLIANCE_PROVIDER___RUN_COMPLIANCE_CHECKS__STRING_LIST_STRING);
		createEOperation(workflowComplianceProviderEClass, WORKFLOW_COMPLIANCE_PROVIDER___GET_COMPLIANCE_STATUS__STRING);
		createEOperation(workflowComplianceProviderEClass, WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_DRAFT__STRING_STRING_STRING);
		createEOperation(workflowComplianceProviderEClass, WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_IN_REVIEW__STRING_STRING_STRING);
		createEOperation(workflowComplianceProviderEClass, WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_APPROVED__STRING_STRING_STRING);
		createEOperation(workflowComplianceProviderEClass, WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_REJECTED__STRING_STRING_STRING);

		governanceDocumentationServiceEClass = createEClass(GOVERNANCE_DOCUMENTATION_SERVICE);
		createEOperation(governanceDocumentationServiceEClass, GOVERNANCE_DOCUMENTATION_SERVICE___STORE_DOCUMENTATION__STRING_GOVERNANCEDOCUMENTATION_STRING_STRING);
		createEOperation(governanceDocumentationServiceEClass, GOVERNANCE_DOCUMENTATION_SERVICE___GET_LATEST_DOCUMENTATION__STRING);
		createEOperation(governanceDocumentationServiceEClass, GOVERNANCE_DOCUMENTATION_SERVICE___GET_DOCUMENTATION__STRING);
		createEOperation(governanceDocumentationServiceEClass, GOVERNANCE_DOCUMENTATION_SERVICE___GET_DOCUMENTATION_HISTORY__STRING);
		createEOperation(governanceDocumentationServiceEClass, GOVERNANCE_DOCUMENTATION_SERVICE___HAS_DOCUMENTATION__STRING);
		createEOperation(governanceDocumentationServiceEClass, GOVERNANCE_DOCUMENTATION_SERVICE___DELETE_ALL_DOCUMENTATION__STRING);
		createEOperation(governanceDocumentationServiceEClass, GOVERNANCE_DOCUMENTATION_SERVICE___GET_DOCUMENTATION_STATISTICS);
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
		GovernancePackage theGovernancePackage = (GovernancePackage)EPackage.Registry.INSTANCE.getEPackage(GovernancePackage.eNS_URI);

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
		g1 = createEGenericType(this.getWorkflowComplianceProvider());
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

		initEClass(systemComponentGovernanceServiceEClass, SystemComponentGovernanceService.class, "SystemComponentGovernanceService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getSystemComponentGovernanceService__RegisterSystemComponent__String_SystemComponentType_String_String(), null, "registerSystemComponent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getSystemComponentType(), "componentType", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "name", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "description", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getSystemComponent());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getSystemComponentGovernanceService__UpdateSystemComponentTrustLevel__String_TrustLevel(), null, "updateSystemComponentTrustLevel", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getTrustLevel(), "trustLevel", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getSystemComponent());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getSystemComponentGovernanceService__RunSystemComponentCompliance__String_List_String(), null, "runSystemComponentCompliance", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getPolicyType(), "policyTypes", 0, -1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getGovernanceDocumentation());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getSystemComponentGovernanceService__ApplyPoliciesToSystemComponent__String_List(), null, "applyPoliciesToSystemComponent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "policyIds", 0, -1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getVoid());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getSystemComponentGovernanceService__GetSystemComponent__String(), null, "getSystemComponent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getSystemComponent());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getSystemComponentGovernanceService__FindSystemComponentsByGovernanceStatus__ComplianceStatus(), null, "findSystemComponentsByGovernanceStatus", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getComplianceStatus(), "complianceStatus", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getList());
		g1.getETypeArguments().add(g2);
		EGenericType g3 = createEGenericType(theGovernancePackage.getSystemComponent());
		g2.getETypeArguments().add(g3);
		initEOperation(op, g1);

		op = initEOperation(getSystemComponentGovernanceService__LinkModelToSystemComponent__String_String(), null, "linkModelToSystemComponent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getVoid());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getSystemComponentGovernanceService__GetSystemComponentsByType__SystemComponentType(), null, "getSystemComponentsByType", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getSystemComponentType(), "componentType", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getList());
		g1.getETypeArguments().add(g2);
		g3 = createEGenericType(theGovernancePackage.getSystemComponent());
		g2.getETypeArguments().add(g3);
		initEOperation(op, g1);

		initEOperation(getSystemComponentGovernanceService__GetSystemId(), ecorePackage.getEString(), "getSystemId", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(governanceComplianceServiceEClass, GovernanceComplianceService.class, "GovernanceComplianceService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getGovernanceComplianceService__RunComplianceChecks__String_String_String(), null, "runComplianceChecks", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "complianceReason", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getGovernanceDocumentation());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceComplianceService__RunSpecificComplianceChecks__String_List_String(), null, "runSpecificComplianceChecks", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getPolicyType(), "policyTypes", 0, -1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getGovernanceDocumentation());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceComplianceService__RunSystemComponentCompliance__String_String_List_String(), null, "runSystemComponentCompliance", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "systemId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getPolicyType(), "policyTypes", 0, -1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getGovernanceDocumentation());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceComplianceService__GetComplianceStatus__String(), null, "getComplianceStatus", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getGovernanceDocumentation());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceComplianceService__ValidatePolicyCompliance__String_PolicyType_String(), null, "validatePolicyCompliance", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getPolicyType(), "policyType", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getComplianceCheckResult());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceComplianceService__GetAvailablePolicies(), null, "getAvailablePolicies", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theGovernancePackage.getPolicyType());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(governanceSystemRepositoryEClass, GovernanceSystemRepository.class, "GovernanceSystemRepository", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getGovernanceSystemRepository__GetGovernanceSystem__String(), theGovernancePackage.getGovernanceSystem(), "getGovernanceSystem", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "systemId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getGovernanceSystemRepository__CreateSystemComponent__String_String_SystemComponentType_String_String(), theGovernancePackage.getSystemComponent(), "createSystemComponent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "systemId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getSystemComponentType(), "componentType", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "name", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "description", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getGovernanceSystemRepository__GetSystemComponent__String_String(), theGovernancePackage.getSystemComponent(), "getSystemComponent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "systemId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getGovernanceSystemRepository__UpdateSystemComponentTrustLevel__String_String_TrustLevel(), theGovernancePackage.getSystemComponent(), "updateSystemComponentTrustLevel", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "systemId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getTrustLevel(), "trustLevel", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getGovernanceSystemRepository__ApplyPoliciesToSystemComponent__String_String_List(), theManagementPackage.getVoid(), "applyPoliciesToSystemComponent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "systemId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "policyIds", 0, -1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getGovernanceSystemRepository__FindSystemComponentsByComplianceStatus__String_ComplianceStatus(), null, "findSystemComponentsByComplianceStatus", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "systemId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getComplianceStatus(), "complianceStatus", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theGovernancePackage.getSystemComponent());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceSystemRepository__GetSystemComponentsByType__String_SystemComponentType(), null, "getSystemComponentsByType", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "systemId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getSystemComponentType(), "componentType", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theGovernancePackage.getSystemComponent());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceSystemRepository__LinkModelToSystemComponent__String_String_String(), theManagementPackage.getVoid(), "linkModelToSystemComponent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "systemId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "componentId", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(systemComponentGovernanceConfigEClass, SystemComponentGovernanceConfig.class, "SystemComponentGovernanceConfig", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEOperation(getSystemComponentGovernanceConfig__SystemId(), ecorePackage.getEString(), "systemId", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getSystemComponentGovernanceConfig__Description(), ecorePackage.getEString(), "description", 0, 1, IS_UNIQUE, IS_ORDERED);

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

		op = initEOperation(getWorkflowDraftProvider__CheckDraft__String_String_String(), null, "checkDraft", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "complianceReason", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getGovernanceDocumentation());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(workflowComplianceProviderEClass, WorkflowComplianceProvider.class, "WorkflowComplianceProvider", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getWorkflowComplianceProvider__CheckObject__String_String_String(), null, "checkObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "complianceReason", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getGovernanceDocumentation());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getWorkflowComplianceProvider__RunComplianceChecks__String_List_String(), null, "runComplianceChecks", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getPolicyType(), "policyTypes", 0, -1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theGovernancePackage.getGovernanceDocumentation());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getWorkflowComplianceProvider__GetComplianceStatus__String(), theGovernancePackage.getGovernanceDocumentation(), "getComplianceStatus", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getWorkflowComplianceProvider__SetGovernanceDocumentationDraft__String_String_String(), theGovernancePackage.getGovernanceDocumentation(), "setGovernanceDocumentationDraft", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reason", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getWorkflowComplianceProvider__SetGovernanceDocumentationInReview__String_String_String(), theGovernancePackage.getGovernanceDocumentation(), "setGovernanceDocumentationInReview", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reason", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getWorkflowComplianceProvider__SetGovernanceDocumentationApproved__String_String_String(), theGovernancePackage.getGovernanceDocumentation(), "setGovernanceDocumentationApproved", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reason", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getWorkflowComplianceProvider__SetGovernanceDocumentationRejected__String_String_String(), theGovernancePackage.getGovernanceDocumentation(), "setGovernanceDocumentationRejected", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reason", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(governanceDocumentationServiceEClass, GovernanceDocumentationService.class, "GovernanceDocumentationService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getGovernanceDocumentationService__StoreDocumentation__String_GovernanceDocumentation_String_String(), null, "storeDocumentation", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theGovernancePackage.getGovernanceDocumentation(), "documentation", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reviewUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reason", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceDocumentationService__GetLatestDocumentation__String(), null, "getLatestDocumentation", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getOptional());
		g2 = createEGenericType(theGovernancePackage.getGovernanceDocumentation());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceDocumentationService__GetDocumentation__String(), null, "getDocumentation", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "documentationId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getOptional());
		g2 = createEGenericType(theGovernancePackage.getGovernanceDocumentation());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceDocumentationService__GetDocumentationHistory__String(), null, "getDocumentationHistory", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceDocumentationService__HasDocumentation__String(), ecorePackage.getEBooleanObject(), "hasDocumentation", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getGovernanceDocumentationService__DeleteAllDocumentation__String(), null, "deleteAllDocumentation", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getGovernanceDocumentationService__GetDocumentationStatistics(), null, "getDocumentationStatistics", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(ecorePackage.getEMap());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		g2 = createEGenericType(ecorePackage.getEJavaObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		// Create resource
		createResource(eNS_URI);
	}

} //ManagementApiPackageImpl
