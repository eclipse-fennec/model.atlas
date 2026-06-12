/*
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
package org.eclipse.fennec.model.atlas.scope.api;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

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
 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = ScopeApiPackage.eNS_URI, genModel = "/model/scope-api.genmodel", genModelSourceLocations = {"model/scope-api.genmodel","org.eclipse.fennec.model.atlas.scope.api/model/scope-api.genmodel"}, ecore = "/model/scope-api.ecore", ecoreSourceLocations = "/model/scope-api.ecore")
public interface ScopeApiPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "api";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/scope/api/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "scopeapi";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ScopeApiPackage eINSTANCE = org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService <em>Read Only Scope Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getReadOnlyScopeService()
	 * @generated
	 */
	int READ_ONLY_SCOPE_SERVICE = 0;

	/**
	 * The number of structural features of the '<em>Read Only Scope Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READ_ONLY_SCOPE_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Get Scope Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READ_ONLY_SCOPE_SERVICE___GET_SCOPE_NAME = 0;

	/**
	 * The operation id for the '<em>Is Inheriting From Parent Scope</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READ_ONLY_SCOPE_SERVICE___IS_INHERITING_FROM_PARENT_SCOPE = 1;

	/**
	 * The operation id for the '<em>Get</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READ_ONLY_SCOPE_SERVICE___GET__STRING_STRING = 2;

	/**
	 * The operation id for the '<em>List Object Ids</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READ_ONLY_SCOPE_SERVICE___LIST_OBJECT_IDS__STRING = 3;

	/**
	 * The operation id for the '<em>List All</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READ_ONLY_SCOPE_SERVICE___LIST_ALL__STRING = 4;

	/**
	 * The operation id for the '<em>Stream</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READ_ONLY_SCOPE_SERVICE___STREAM__STRING = 5;

	/**
	 * The operation id for the '<em>Get Scope Info</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READ_ONLY_SCOPE_SERVICE___GET_SCOPE_INFO = 6;

	/**
	 * The number of operations of the '<em>Read Only Scope Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READ_ONLY_SCOPE_SERVICE_OPERATION_COUNT = 7;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.scope.api.impl.ScopeInfoImpl <em>Scope Info</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeInfoImpl
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getScopeInfo()
	 * @generated
	 */
	int SCOPE_INFO = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_INFO__NAME = 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_INFO__DESCRIPTION = 1;

	/**
	 * The feature id for the '<em><b>Parent Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_INFO__PARENT_SCOPE = 2;

	/**
	 * The feature id for the '<em><b>Registries</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_INFO__REGISTRIES = 3;

	/**
	 * The number of structural features of the '<em>Scope Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_INFO_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Scope Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_INFO_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.scope.api.impl.RegistryInfoImpl <em>Registry Info</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.RegistryInfoImpl
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getRegistryInfo()
	 * @generated
	 */
	int REGISTRY_INFO = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_INFO__NAME = 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_INFO__DESCRIPTION = 1;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_INFO__TYPE = 2;

	/**
	 * The number of structural features of the '<em>Registry Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_INFO_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Registry Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_INFO_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryType <em>Registry Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.scope.api.RegistryType
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getRegistryType()
	 * @generated
	 */
	int REGISTRY_TYPE = 3;

	/**
	 * The meta object id for the '<em>Optional</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.Optional
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getOptional()
	 * @generated
	 */
	int OPTIONAL = 4;

	/**
	 * The meta object id for the '<em>List</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.List
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getList()
	 * @generated
	 */
	int LIST = 5;

	/**
	 * The meta object id for the '<em>Stream</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.stream.Stream
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getStream()
	 * @generated
	 */
	int STREAM = 6;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService <em>Read Only Scope Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Read Only Scope Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService
	 * @generated
	 */
	EClass getReadOnlyScopeService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#getScopeName() <em>Get Scope Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Scope Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#getScopeName()
	 * @generated
	 */
	EOperation getReadOnlyScopeService__GetScopeName();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#isInheritingFromParentScope() <em>Is Inheriting From Parent Scope</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Inheriting From Parent Scope</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#isInheritingFromParentScope()
	 * @generated
	 */
	EOperation getReadOnlyScopeService__IsInheritingFromParentScope();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#get(java.lang.String, java.lang.String) <em>Get</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#get(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getReadOnlyScopeService__Get__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#listObjectIds(java.lang.String) <em>List Object Ids</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Object Ids</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#listObjectIds(java.lang.String)
	 * @generated
	 */
	EOperation getReadOnlyScopeService__ListObjectIds__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#listAll(java.lang.String) <em>List All</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List All</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#listAll(java.lang.String)
	 * @generated
	 */
	EOperation getReadOnlyScopeService__ListAll__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#stream(java.lang.String) <em>Stream</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Stream</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#stream(java.lang.String)
	 * @generated
	 */
	EOperation getReadOnlyScopeService__Stream__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#getScopeInfo() <em>Get Scope Info</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Scope Info</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService#getScopeInfo()
	 * @generated
	 */
	EOperation getReadOnlyScopeService__GetScopeInfo();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo <em>Scope Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Scope Info</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeInfo
	 * @generated
	 */
	EClass getScopeInfo();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getName()
	 * @see #getScopeInfo()
	 * @generated
	 */
	EAttribute getScopeInfo_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getDescription()
	 * @see #getScopeInfo()
	 * @generated
	 */
	EAttribute getScopeInfo_Description();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getParentScope <em>Parent Scope</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parent Scope</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getParentScope()
	 * @see #getScopeInfo()
	 * @generated
	 */
	EAttribute getScopeInfo_ParentScope();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getRegistries <em>Registries</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Registries</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getRegistries()
	 * @see #getScopeInfo()
	 * @generated
	 */
	EReference getScopeInfo_Registries();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo <em>Registry Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Registry Info</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.RegistryInfo
	 * @generated
	 */
	EClass getRegistryInfo();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getName()
	 * @see #getRegistryInfo()
	 * @generated
	 */
	EAttribute getRegistryInfo_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getDescription()
	 * @see #getRegistryInfo()
	 * @generated
	 */
	EAttribute getRegistryInfo_Description();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getType()
	 * @see #getRegistryInfo()
	 * @generated
	 */
	EAttribute getRegistryInfo_Type();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryType <em>Registry Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Registry Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.RegistryType
	 * @generated
	 */
	EEnum getRegistryType();

	/**
	 * Returns the meta object for data type '{@link java.util.Optional <em>Optional</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Optional</em>'.
	 * @see java.util.Optional
	 * @model instanceClass="java.util.Optional" serializeable="false" typeParameters="T"
	 * @generated
	 */
	EDataType getOptional();

	/**
	 * Returns the meta object for data type '{@link java.util.List <em>List</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>List</em>'.
	 * @see java.util.List
	 * @model instanceClass="java.util.List" serializeable="false" typeParameters="T"
	 * @generated
	 */
	EDataType getList();

	/**
	 * Returns the meta object for data type '{@link java.util.stream.Stream <em>Stream</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Stream</em>'.
	 * @see java.util.stream.Stream
	 * @model instanceClass="java.util.stream.Stream" serializeable="false" typeParameters="T"
	 * @generated
	 */
	EDataType getStream();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ScopeApiFactory getScopeApiFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService <em>Read Only Scope Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getReadOnlyScopeService()
		 * @generated
		 */
		EClass READ_ONLY_SCOPE_SERVICE = eINSTANCE.getReadOnlyScopeService();

		/**
		 * The meta object literal for the '<em><b>Get Scope Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READ_ONLY_SCOPE_SERVICE___GET_SCOPE_NAME = eINSTANCE.getReadOnlyScopeService__GetScopeName();

		/**
		 * The meta object literal for the '<em><b>Is Inheriting From Parent Scope</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READ_ONLY_SCOPE_SERVICE___IS_INHERITING_FROM_PARENT_SCOPE = eINSTANCE.getReadOnlyScopeService__IsInheritingFromParentScope();

		/**
		 * The meta object literal for the '<em><b>Get</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READ_ONLY_SCOPE_SERVICE___GET__STRING_STRING = eINSTANCE.getReadOnlyScopeService__Get__String_String();

		/**
		 * The meta object literal for the '<em><b>List Object Ids</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READ_ONLY_SCOPE_SERVICE___LIST_OBJECT_IDS__STRING = eINSTANCE.getReadOnlyScopeService__ListObjectIds__String();

		/**
		 * The meta object literal for the '<em><b>List All</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READ_ONLY_SCOPE_SERVICE___LIST_ALL__STRING = eINSTANCE.getReadOnlyScopeService__ListAll__String();

		/**
		 * The meta object literal for the '<em><b>Stream</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READ_ONLY_SCOPE_SERVICE___STREAM__STRING = eINSTANCE.getReadOnlyScopeService__Stream__String();

		/**
		 * The meta object literal for the '<em><b>Get Scope Info</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READ_ONLY_SCOPE_SERVICE___GET_SCOPE_INFO = eINSTANCE.getReadOnlyScopeService__GetScopeInfo();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.scope.api.impl.ScopeInfoImpl <em>Scope Info</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeInfoImpl
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getScopeInfo()
		 * @generated
		 */
		EClass SCOPE_INFO = eINSTANCE.getScopeInfo();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCOPE_INFO__NAME = eINSTANCE.getScopeInfo_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCOPE_INFO__DESCRIPTION = eINSTANCE.getScopeInfo_Description();

		/**
		 * The meta object literal for the '<em><b>Parent Scope</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCOPE_INFO__PARENT_SCOPE = eINSTANCE.getScopeInfo_ParentScope();

		/**
		 * The meta object literal for the '<em><b>Registries</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SCOPE_INFO__REGISTRIES = eINSTANCE.getScopeInfo_Registries();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.scope.api.impl.RegistryInfoImpl <em>Registry Info</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.RegistryInfoImpl
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getRegistryInfo()
		 * @generated
		 */
		EClass REGISTRY_INFO = eINSTANCE.getRegistryInfo();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGISTRY_INFO__NAME = eINSTANCE.getRegistryInfo_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGISTRY_INFO__DESCRIPTION = eINSTANCE.getRegistryInfo_Description();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REGISTRY_INFO__TYPE = eINSTANCE.getRegistryInfo_Type();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryType <em>Registry Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.scope.api.RegistryType
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getRegistryType()
		 * @generated
		 */
		EEnum REGISTRY_TYPE = eINSTANCE.getRegistryType();

		/**
		 * The meta object literal for the '<em>Optional</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.Optional
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getOptional()
		 * @generated
		 */
		EDataType OPTIONAL = eINSTANCE.getOptional();

		/**
		 * The meta object literal for the '<em>List</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.List
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getList()
		 * @generated
		 */
		EDataType LIST = eINSTANCE.getList();

		/**
		 * The meta object literal for the '<em>Stream</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.stream.Stream
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getStream()
		 * @generated
		 */
		EDataType STREAM = eINSTANCE.getStream();

	}

} //ScopeApiPackage
