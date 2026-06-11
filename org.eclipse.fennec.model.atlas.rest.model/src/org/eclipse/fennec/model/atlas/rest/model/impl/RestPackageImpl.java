/**
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 */
package org.eclipse.fennec.model.atlas.rest.model.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;

import org.eclipse.fennec.model.atlas.rest.model.EPackageInfo;
import org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse;
import org.eclipse.fennec.model.atlas.rest.model.ErrorResponse;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.eclipse.fennec.model.atlas.rest.model.RestPackage;
import org.eclipse.fennec.model.atlas.rest.model.ScopeListResponse;
import org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest;

import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class RestPackageImpl extends EPackageImpl implements RestPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass ePackageInfoEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass ePackageListResponseEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass errorResponseEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stageTransitionRequestEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass scopeListResponseEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass containerEClass = null;

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
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private RestPackageImpl() {
		super(eNS_URI, RestFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link RestPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static RestPackage init() {
		if (isInited) return (RestPackage)EPackage.Registry.INSTANCE.getEPackage(RestPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredRestPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		RestPackageImpl theRestPackage = registeredRestPackage instanceof RestPackageImpl ? (RestPackageImpl)registeredRestPackage : new RestPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		ManagementPackage.eINSTANCE.eClass();
		WorkflowApiPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theRestPackage.createPackageContents();

		// Initialize created meta-data
		theRestPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theRestPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(RestPackage.eNS_URI, theRestPackage);
		return theRestPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEPackageInfo() {
		return ePackageInfoEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEPackageInfo_Name() {
		return (EAttribute)ePackageInfoEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEPackageInfo_NsURI() {
		return (EAttribute)ePackageInfoEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEPackageInfo_NsPrefix() {
		return (EAttribute)ePackageInfoEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEPackageInfo_ClassifierCount() {
		return (EAttribute)ePackageInfoEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEPackageInfo_SubPackageCount() {
		return (EAttribute)ePackageInfoEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEPackageListResponse() {
		return ePackageListResponseEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEPackageListResponse_Packages() {
		return (EReference)ePackageListResponseEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEPackageListResponse_Total() {
		return (EAttribute)ePackageListResponseEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEPackageListResponse_Skip() {
		return (EAttribute)ePackageListResponseEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEPackageListResponse_Limit() {
		return (EAttribute)ePackageListResponseEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getErrorResponse() {
		return errorResponseEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getErrorResponse_Message() {
		return (EAttribute)errorResponseEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getErrorResponse_Code() {
		return (EAttribute)errorResponseEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getErrorResponse_Timestamp() {
		return (EAttribute)errorResponseEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getStageTransitionRequest() {
		return stageTransitionRequestEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStageTransitionRequest_ObjectId() {
		return (EAttribute)stageTransitionRequestEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStageTransitionRequest_TargetStage() {
		return (EAttribute)stageTransitionRequestEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getScopeListResponse() {
		return scopeListResponseEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getScopeListResponse_Scopes() {
		return (EReference)scopeListResponseEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getContainer() {
		return containerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getContainer_Elements() {
		return (EReference)containerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RestFactory getRestFactory() {
		return (RestFactory)getEFactoryInstance();
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
		ePackageInfoEClass = createEClass(EPACKAGE_INFO);
		createEAttribute(ePackageInfoEClass, EPACKAGE_INFO__NAME);
		createEAttribute(ePackageInfoEClass, EPACKAGE_INFO__NS_URI);
		createEAttribute(ePackageInfoEClass, EPACKAGE_INFO__NS_PREFIX);
		createEAttribute(ePackageInfoEClass, EPACKAGE_INFO__CLASSIFIER_COUNT);
		createEAttribute(ePackageInfoEClass, EPACKAGE_INFO__SUB_PACKAGE_COUNT);

		ePackageListResponseEClass = createEClass(EPACKAGE_LIST_RESPONSE);
		createEReference(ePackageListResponseEClass, EPACKAGE_LIST_RESPONSE__PACKAGES);
		createEAttribute(ePackageListResponseEClass, EPACKAGE_LIST_RESPONSE__TOTAL);
		createEAttribute(ePackageListResponseEClass, EPACKAGE_LIST_RESPONSE__SKIP);
		createEAttribute(ePackageListResponseEClass, EPACKAGE_LIST_RESPONSE__LIMIT);

		errorResponseEClass = createEClass(ERROR_RESPONSE);
		createEAttribute(errorResponseEClass, ERROR_RESPONSE__MESSAGE);
		createEAttribute(errorResponseEClass, ERROR_RESPONSE__CODE);
		createEAttribute(errorResponseEClass, ERROR_RESPONSE__TIMESTAMP);

		stageTransitionRequestEClass = createEClass(STAGE_TRANSITION_REQUEST);
		createEAttribute(stageTransitionRequestEClass, STAGE_TRANSITION_REQUEST__OBJECT_ID);
		createEAttribute(stageTransitionRequestEClass, STAGE_TRANSITION_REQUEST__TARGET_STAGE);

		scopeListResponseEClass = createEClass(SCOPE_LIST_RESPONSE);
		createEReference(scopeListResponseEClass, SCOPE_LIST_RESPONSE__SCOPES);

		containerEClass = createEClass(CONTAINER);
		createEReference(containerEClass, CONTAINER__ELEMENTS);
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
		WorkflowApiPackage theWorkflowApiPackage = (WorkflowApiPackage)EPackage.Registry.INSTANCE.getEPackage(WorkflowApiPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(ePackageInfoEClass, EPackageInfo.class, "EPackageInfo", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEPackageInfo_Name(), ecorePackage.getEString(), "name", null, 0, 1, EPackageInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEPackageInfo_NsURI(), ecorePackage.getEString(), "nsURI", null, 0, 1, EPackageInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEPackageInfo_NsPrefix(), ecorePackage.getEString(), "nsPrefix", null, 0, 1, EPackageInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEPackageInfo_ClassifierCount(), ecorePackage.getEInt(), "classifierCount", null, 0, 1, EPackageInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEPackageInfo_SubPackageCount(), ecorePackage.getEInt(), "subPackageCount", null, 0, 1, EPackageInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(ePackageListResponseEClass, EPackageListResponse.class, "EPackageListResponse", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getEPackageListResponse_Packages(), this.getEPackageInfo(), null, "packages", null, 0, -1, EPackageListResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEPackageListResponse_Total(), ecorePackage.getEInt(), "total", null, 0, 1, EPackageListResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEPackageListResponse_Skip(), ecorePackage.getEInt(), "skip", "0", 0, 1, EPackageListResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEPackageListResponse_Limit(), ecorePackage.getEInt(), "limit", "100", 0, 1, EPackageListResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(errorResponseEClass, ErrorResponse.class, "ErrorResponse", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getErrorResponse_Message(), ecorePackage.getEString(), "message", null, 0, 1, ErrorResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getErrorResponse_Code(), ecorePackage.getEString(), "code", null, 0, 1, ErrorResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getErrorResponse_Timestamp(), ecorePackage.getEDate(), "timestamp", null, 0, 1, ErrorResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(stageTransitionRequestEClass, StageTransitionRequest.class, "StageTransitionRequest", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStageTransitionRequest_ObjectId(), ecorePackage.getEString(), "objectId", null, 0, 1, StageTransitionRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStageTransitionRequest_TargetStage(), ecorePackage.getEString(), "targetStage", null, 0, 1, StageTransitionRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(scopeListResponseEClass, ScopeListResponse.class, "ScopeListResponse", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getScopeListResponse_Scopes(), theWorkflowApiPackage.getScope(), null, "scopes", null, 0, -1, ScopeListResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(containerEClass, org.eclipse.fennec.model.atlas.rest.model.Container.class, "Container", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getContainer_Elements(), ecorePackage.getEObject(), null, "elements", null, 0, -1, org.eclipse.fennec.model.atlas.rest.model.Container.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/GenModel</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createGenModelAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/GenModel";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "copyrightText", "Copyright (c) 2026 Contributors to the Eclipse Foundation.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n  Data In Motion Consulting - initial implementation",
			   "oSGiCompatible", "true",
			   "complianceLevel", "21.0",
			   "basePackage", "org.eclipse.fennec.model.atlas.rest",
			   "resource", "XMI",
			   "fileExtensions", "rest"
		   });
	}

} //RestPackageImpl
