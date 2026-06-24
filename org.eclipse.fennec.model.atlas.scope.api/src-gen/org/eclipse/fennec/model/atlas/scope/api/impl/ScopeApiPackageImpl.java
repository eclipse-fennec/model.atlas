/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.scope.api.impl;

import java.util.List;
import java.util.Optional;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypeParameter;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeApiFactory;
import org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ScopeApiPackageImpl extends EPackageImpl implements ScopeApiPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass readableScopeServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass scopeInfoEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass registryInfoEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stageInfoEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass readableRegistryViewEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum registryTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType optionalEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType listEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType streamEDataType = null;

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
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ScopeApiPackageImpl() {
		super(eNS_URI, ScopeApiFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ScopeApiPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ScopeApiPackage init() {
		if (isInited) return (ScopeApiPackage)EPackage.Registry.INSTANCE.getEPackage(ScopeApiPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredScopeApiPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ScopeApiPackageImpl theScopeApiPackage = registeredScopeApiPackage instanceof ScopeApiPackageImpl ? (ScopeApiPackageImpl)registeredScopeApiPackage : new ScopeApiPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theScopeApiPackage.createPackageContents();

		// Initialize created meta-data
		theScopeApiPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theScopeApiPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ScopeApiPackage.eNS_URI, theScopeApiPackage);
		return theScopeApiPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReadableScopeService() {
		return readableScopeServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableScopeService__GetScopeName() {
		return readableScopeServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableScopeService__IsInheritingFromParentScope() {
		return readableScopeServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableScopeService__Get__String_String() {
		return readableScopeServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableScopeService__ListObjectIds__String() {
		return readableScopeServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableScopeService__ListAll__String() {
		return readableScopeServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableScopeService__Stream__String() {
		return readableScopeServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableScopeService__GetScopeInfo() {
		return readableScopeServiceEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableScopeService__RegistryView__String() {
		return readableScopeServiceEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableScopeService__RegistryView__String_String() {
		return readableScopeServiceEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getScopeInfo() {
		return scopeInfoEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getScopeInfo_Name() {
		return (EAttribute)scopeInfoEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getScopeInfo_Description() {
		return (EAttribute)scopeInfoEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getScopeInfo_ParentScope() {
		return (EAttribute)scopeInfoEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getScopeInfo_Registries() {
		return (EReference)scopeInfoEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRegistryInfo() {
		return registryInfoEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRegistryInfo_Name() {
		return (EAttribute)registryInfoEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRegistryInfo_Description() {
		return (EAttribute)registryInfoEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRegistryInfo_Type() {
		return (EAttribute)registryInfoEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRegistryInfo_Stages() {
		return (EReference)registryInfoEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getStageInfo() {
		return stageInfoEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStageInfo_Name() {
		return (EAttribute)stageInfoEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStageInfo_Description() {
		return (EAttribute)stageInfoEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStageInfo_Readable() {
		return (EAttribute)stageInfoEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStageInfo_Writable() {
		return (EAttribute)stageInfoEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStageInfo_Final() {
		return (EAttribute)stageInfoEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReadableRegistryView() {
		return readableRegistryViewEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableRegistryView__GetScopeName() {
		return readableRegistryViewEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableRegistryView__GetRegistryName() {
		return readableRegistryViewEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableRegistryView__GetStageName() {
		return readableRegistryViewEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableRegistryView__Get__String() {
		return readableRegistryViewEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableRegistryView__ListObjectIds() {
		return readableRegistryViewEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableRegistryView__ListAll() {
		return readableRegistryViewEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getReadableRegistryView__Stream() {
		return readableRegistryViewEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getRegistryType() {
		return registryTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getOptional() {
		return optionalEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getList() {
		return listEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getStream() {
		return streamEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ScopeApiFactory getScopeApiFactory() {
		return (ScopeApiFactory)getEFactoryInstance();
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
		readableScopeServiceEClass = createEClass(READABLE_SCOPE_SERVICE);
		createEOperation(readableScopeServiceEClass, READABLE_SCOPE_SERVICE___GET_SCOPE_NAME);
		createEOperation(readableScopeServiceEClass, READABLE_SCOPE_SERVICE___IS_INHERITING_FROM_PARENT_SCOPE);
		createEOperation(readableScopeServiceEClass, READABLE_SCOPE_SERVICE___GET__STRING_STRING);
		createEOperation(readableScopeServiceEClass, READABLE_SCOPE_SERVICE___LIST_OBJECT_IDS__STRING);
		createEOperation(readableScopeServiceEClass, READABLE_SCOPE_SERVICE___LIST_ALL__STRING);
		createEOperation(readableScopeServiceEClass, READABLE_SCOPE_SERVICE___STREAM__STRING);
		createEOperation(readableScopeServiceEClass, READABLE_SCOPE_SERVICE___GET_SCOPE_INFO);
		createEOperation(readableScopeServiceEClass, READABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING);
		createEOperation(readableScopeServiceEClass, READABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING_STRING);

		scopeInfoEClass = createEClass(SCOPE_INFO);
		createEAttribute(scopeInfoEClass, SCOPE_INFO__NAME);
		createEAttribute(scopeInfoEClass, SCOPE_INFO__DESCRIPTION);
		createEAttribute(scopeInfoEClass, SCOPE_INFO__PARENT_SCOPE);
		createEReference(scopeInfoEClass, SCOPE_INFO__REGISTRIES);

		registryInfoEClass = createEClass(REGISTRY_INFO);
		createEAttribute(registryInfoEClass, REGISTRY_INFO__NAME);
		createEAttribute(registryInfoEClass, REGISTRY_INFO__DESCRIPTION);
		createEAttribute(registryInfoEClass, REGISTRY_INFO__TYPE);
		createEReference(registryInfoEClass, REGISTRY_INFO__STAGES);

		stageInfoEClass = createEClass(STAGE_INFO);
		createEAttribute(stageInfoEClass, STAGE_INFO__NAME);
		createEAttribute(stageInfoEClass, STAGE_INFO__DESCRIPTION);
		createEAttribute(stageInfoEClass, STAGE_INFO__READABLE);
		createEAttribute(stageInfoEClass, STAGE_INFO__WRITABLE);
		createEAttribute(stageInfoEClass, STAGE_INFO__FINAL);

		readableRegistryViewEClass = createEClass(READABLE_REGISTRY_VIEW);
		createEOperation(readableRegistryViewEClass, READABLE_REGISTRY_VIEW___GET_SCOPE_NAME);
		createEOperation(readableRegistryViewEClass, READABLE_REGISTRY_VIEW___GET_REGISTRY_NAME);
		createEOperation(readableRegistryViewEClass, READABLE_REGISTRY_VIEW___GET_STAGE_NAME);
		createEOperation(readableRegistryViewEClass, READABLE_REGISTRY_VIEW___GET__STRING);
		createEOperation(readableRegistryViewEClass, READABLE_REGISTRY_VIEW___LIST_OBJECT_IDS);
		createEOperation(readableRegistryViewEClass, READABLE_REGISTRY_VIEW___LIST_ALL);
		createEOperation(readableRegistryViewEClass, READABLE_REGISTRY_VIEW___STREAM);

		// Create enums
		registryTypeEEnum = createEEnum(REGISTRY_TYPE);

		// Create data types
		optionalEDataType = createEDataType(OPTIONAL);
		listEDataType = createEDataType(LIST);
		streamEDataType = createEDataType(STREAM);
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

		// Create type parameters
		ETypeParameter readableScopeServiceEClass_T = addETypeParameter(readableScopeServiceEClass, "T");
		ETypeParameter readableRegistryViewEClass_T = addETypeParameter(readableRegistryViewEClass, "T");
		addETypeParameter(optionalEDataType, "T");
		addETypeParameter(listEDataType, "T");
		addETypeParameter(streamEDataType, "T");

		// Set bounds for type parameters
		EGenericType g1 = createEGenericType(ecorePackage.getEObject());
		readableScopeServiceEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(ecorePackage.getEObject());
		readableRegistryViewEClass_T.getEBounds().add(g1);

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(readableScopeServiceEClass, ReadableScopeService.class, "ReadableScopeService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEOperation(getReadableScopeService__GetScopeName(), ecorePackage.getEString(), "getScopeName", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getReadableScopeService__IsInheritingFromParentScope(), ecorePackage.getEBoolean(), "isInheritingFromParentScope", 0, 1, IS_UNIQUE, IS_ORDERED);

		EOperation op = initEOperation(getReadableScopeService__Get__String_String(), null, "get", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getOptional());
		EGenericType g2 = createEGenericType(readableScopeServiceEClass_T);
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getReadableScopeService__ListObjectIds__String(), null, "listObjectIds", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getList());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getReadableScopeService__ListAll__String(), null, "listAll", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getList());
		g2 = createEGenericType(readableScopeServiceEClass_T);
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getReadableScopeService__Stream__String(), null, "stream", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getStream());
		g2 = createEGenericType(readableScopeServiceEClass_T);
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEOperation(getReadableScopeService__GetScopeInfo(), this.getScopeInfo(), "getScopeInfo", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getReadableScopeService__RegistryView__String(), null, "registryView", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getReadableRegistryView());
		g2 = createEGenericType(readableScopeServiceEClass_T);
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getReadableScopeService__RegistryView__String_String(), null, "registryView", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getReadableRegistryView());
		g2 = createEGenericType(readableScopeServiceEClass_T);
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(scopeInfoEClass, ScopeInfo.class, "ScopeInfo", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getScopeInfo_Name(), ecorePackage.getEString(), "name", null, 0, 1, ScopeInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getScopeInfo_Description(), ecorePackage.getEString(), "description", null, 0, 1, ScopeInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getScopeInfo_ParentScope(), ecorePackage.getEString(), "parentScope", null, 0, 1, ScopeInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getScopeInfo_Registries(), this.getRegistryInfo(), null, "registries", null, 0, -1, ScopeInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(registryInfoEClass, RegistryInfo.class, "RegistryInfo", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRegistryInfo_Name(), ecorePackage.getEString(), "name", null, 0, 1, RegistryInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRegistryInfo_Description(), ecorePackage.getEString(), "description", null, 0, 1, RegistryInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRegistryInfo_Type(), this.getRegistryType(), "type", null, 0, 1, RegistryInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRegistryInfo_Stages(), this.getStageInfo(), null, "stages", null, 0, -1, RegistryInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(stageInfoEClass, StageInfo.class, "StageInfo", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStageInfo_Name(), ecorePackage.getEString(), "name", null, 0, 1, StageInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStageInfo_Description(), ecorePackage.getEString(), "description", null, 0, 1, StageInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStageInfo_Readable(), ecorePackage.getEBoolean(), "readable", "true", 0, 1, StageInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStageInfo_Writable(), ecorePackage.getEBoolean(), "writable", "false", 0, 1, StageInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStageInfo_Final(), ecorePackage.getEBoolean(), "final", "false", 0, 1, StageInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(readableRegistryViewEClass, ReadableRegistryView.class, "ReadableRegistryView", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEOperation(getReadableRegistryView__GetScopeName(), ecorePackage.getEString(), "getScopeName", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getReadableRegistryView__GetRegistryName(), ecorePackage.getEString(), "getRegistryName", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getReadableRegistryView__GetStageName(), ecorePackage.getEString(), "getStageName", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getReadableRegistryView__Get__String(), null, "get", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getOptional());
		g2 = createEGenericType(readableScopeServiceEClass_T);
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getReadableRegistryView__ListObjectIds(), null, "listObjectIds", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getList());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getReadableRegistryView__ListAll(), null, "listAll", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getList());
		g2 = createEGenericType(readableScopeServiceEClass_T);
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getReadableRegistryView__Stream(), null, "stream", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getStream());
		g2 = createEGenericType(readableScopeServiceEClass_T);
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		// Initialize enums and add enum literals
		initEEnum(registryTypeEEnum, RegistryType.class, "RegistryType");
		addEEnumLiteral(registryTypeEEnum, RegistryType.OTHER);
		addEEnumLiteral(registryTypeEEnum, RegistryType.SCHEMA);
		addEEnumLiteral(registryTypeEEnum, RegistryType.COCL);

		// Initialize data types
		initEDataType(optionalEDataType, Optional.class, "Optional", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(listEDataType, List.class, "List", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(streamEDataType, Stream.class, "Stream", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //ScopeApiPackageImpl
