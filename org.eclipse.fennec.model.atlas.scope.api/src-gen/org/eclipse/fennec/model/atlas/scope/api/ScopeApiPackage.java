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
@EPackage(uri = ScopeApiPackage.eNS_URI, fingerprint = "fp1:a21b8f3df410facb77a0cb85a7285a55b99af601d8d32017bd835bee7c10ff09", genModel = "/model/scope-api.genmodel", genModelSourceLocations = {"model/scope-api.genmodel","org.eclipse.fennec.model.atlas.scope.api/model/scope-api.genmodel"}, ecore = "/model/scope-api.ecore", ecoreSourceLocations = "/model/scope-api.ecore")
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
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService <em>Readable Scope Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getReadableScopeService()
	 * @generated
	 */
	int READABLE_SCOPE_SERVICE = 0;

	/**
	 * The number of structural features of the '<em>Readable Scope Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_SCOPE_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Get Scope Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_SCOPE_SERVICE___GET_SCOPE_NAME = 0;

	/**
	 * The operation id for the '<em>Is Inheriting From Parent Scope</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_SCOPE_SERVICE___IS_INHERITING_FROM_PARENT_SCOPE = 1;

	/**
	 * The operation id for the '<em>Get</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_SCOPE_SERVICE___GET__STRING_STRING = 2;

	/**
	 * The operation id for the '<em>List Object Ids</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_SCOPE_SERVICE___LIST_OBJECT_IDS__STRING = 3;

	/**
	 * The operation id for the '<em>List All</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_SCOPE_SERVICE___LIST_ALL__STRING = 4;

	/**
	 * The operation id for the '<em>Stream</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_SCOPE_SERVICE___STREAM__STRING = 5;

	/**
	 * The operation id for the '<em>Get Scope Info</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_SCOPE_SERVICE___GET_SCOPE_INFO = 6;

	/**
	 * The operation id for the '<em>Registry View</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING = 7;

	/**
	 * The operation id for the '<em>Registry View</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING_STRING = 8;

	/**
	 * The number of operations of the '<em>Readable Scope Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_SCOPE_SERVICE_OPERATION_COUNT = 9;

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
	 * The feature id for the '<em><b>Stages</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_INFO__STAGES = 3;

	/**
	 * The number of structural features of the '<em>Registry Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_INFO_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Registry Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_INFO_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.scope.api.impl.StageInfoImpl <em>Stage Info</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.StageInfoImpl
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getStageInfo()
	 * @generated
	 */
	int STAGE_INFO = 3;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_INFO__NAME = 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_INFO__DESCRIPTION = 1;

	/**
	 * The feature id for the '<em><b>Readable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_INFO__READABLE = 2;

	/**
	 * The feature id for the '<em><b>Writable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_INFO__WRITABLE = 3;

	/**
	 * The feature id for the '<em><b>Final</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_INFO__FINAL = 4;

	/**
	 * The number of structural features of the '<em>Stage Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_INFO_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Stage Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_INFO_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView <em>Readable Registry View</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getReadableRegistryView()
	 * @generated
	 */
	int READABLE_REGISTRY_VIEW = 4;

	/**
	 * The number of structural features of the '<em>Readable Registry View</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_REGISTRY_VIEW_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Get Scope Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_REGISTRY_VIEW___GET_SCOPE_NAME = 0;

	/**
	 * The operation id for the '<em>Get Registry Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_REGISTRY_VIEW___GET_REGISTRY_NAME = 1;

	/**
	 * The operation id for the '<em>Get Stage Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_REGISTRY_VIEW___GET_STAGE_NAME = 2;

	/**
	 * The operation id for the '<em>Get</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_REGISTRY_VIEW___GET__STRING = 3;

	/**
	 * The operation id for the '<em>List Object Ids</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_REGISTRY_VIEW___LIST_OBJECT_IDS = 4;

	/**
	 * The operation id for the '<em>List All</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_REGISTRY_VIEW___LIST_ALL = 5;

	/**
	 * The operation id for the '<em>Stream</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_REGISTRY_VIEW___STREAM = 6;

	/**
	 * The number of operations of the '<em>Readable Registry View</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int READABLE_REGISTRY_VIEW_OPERATION_COUNT = 7;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryType <em>Registry Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.scope.api.RegistryType
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getRegistryType()
	 * @generated
	 */
	int REGISTRY_TYPE = 5;

	/**
	 * The meta object id for the '<em>Optional</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.Optional
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getOptional()
	 * @generated
	 */
	int OPTIONAL = 6;

	/**
	 * The meta object id for the '<em>List</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.List
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getList()
	 * @generated
	 */
	int LIST = 7;

	/**
	 * The meta object id for the '<em>Stream</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.stream.Stream
	 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getStream()
	 * @generated
	 */
	int STREAM = 8;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService <em>Readable Scope Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Readable Scope Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService
	 * @generated
	 */
	EClass getReadableScopeService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#getScopeName() <em>Get Scope Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Scope Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#getScopeName()
	 * @generated
	 */
	EOperation getReadableScopeService__GetScopeName();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#isInheritingFromParentScope() <em>Is Inheriting From Parent Scope</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Inheriting From Parent Scope</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#isInheritingFromParentScope()
	 * @generated
	 */
	EOperation getReadableScopeService__IsInheritingFromParentScope();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#get(java.lang.String, java.lang.String) <em>Get</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#get(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getReadableScopeService__Get__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#listObjectIds(java.lang.String) <em>List Object Ids</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Object Ids</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#listObjectIds(java.lang.String)
	 * @generated
	 */
	EOperation getReadableScopeService__ListObjectIds__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#listAll(java.lang.String) <em>List All</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List All</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#listAll(java.lang.String)
	 * @generated
	 */
	EOperation getReadableScopeService__ListAll__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#stream(java.lang.String) <em>Stream</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Stream</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#stream(java.lang.String)
	 * @generated
	 */
	EOperation getReadableScopeService__Stream__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#getScopeInfo() <em>Get Scope Info</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Scope Info</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#getScopeInfo()
	 * @generated
	 */
	EOperation getReadableScopeService__GetScopeInfo();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#registryView(java.lang.String) <em>Registry View</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Registry View</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#registryView(java.lang.String)
	 * @generated
	 */
	EOperation getReadableScopeService__RegistryView__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#registryView(java.lang.String, java.lang.String) <em>Registry View</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Registry View</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService#registryView(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getReadableScopeService__RegistryView__String_String();

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
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getStages <em>Stages</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Stages</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getStages()
	 * @see #getRegistryInfo()
	 * @generated
	 */
	EReference getRegistryInfo_Stages();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.scope.api.StageInfo <em>Stage Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Stage Info</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.StageInfo
	 * @generated
	 */
	EClass getStageInfo();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.scope.api.StageInfo#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.StageInfo#getName()
	 * @see #getStageInfo()
	 * @generated
	 */
	EAttribute getStageInfo_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.scope.api.StageInfo#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.StageInfo#getDescription()
	 * @see #getStageInfo()
	 * @generated
	 */
	EAttribute getStageInfo_Description();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.scope.api.StageInfo#isReadable <em>Readable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Readable</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.StageInfo#isReadable()
	 * @see #getStageInfo()
	 * @generated
	 */
	EAttribute getStageInfo_Readable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.scope.api.StageInfo#isWritable <em>Writable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Writable</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.StageInfo#isWritable()
	 * @see #getStageInfo()
	 * @generated
	 */
	EAttribute getStageInfo_Writable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.scope.api.StageInfo#isFinal <em>Final</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Final</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.StageInfo#isFinal()
	 * @see #getStageInfo()
	 * @generated
	 */
	EAttribute getStageInfo_Final();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView <em>Readable Registry View</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Readable Registry View</em>'.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView
	 * @generated
	 */
	EClass getReadableRegistryView();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#getScopeName() <em>Get Scope Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Scope Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#getScopeName()
	 * @generated
	 */
	EOperation getReadableRegistryView__GetScopeName();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#getRegistryName() <em>Get Registry Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Registry Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#getRegistryName()
	 * @generated
	 */
	EOperation getReadableRegistryView__GetRegistryName();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#getStageName() <em>Get Stage Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Stage Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#getStageName()
	 * @generated
	 */
	EOperation getReadableRegistryView__GetStageName();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#get(java.lang.String) <em>Get</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#get(java.lang.String)
	 * @generated
	 */
	EOperation getReadableRegistryView__Get__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#listObjectIds() <em>List Object Ids</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Object Ids</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#listObjectIds()
	 * @generated
	 */
	EOperation getReadableRegistryView__ListObjectIds();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#listAll() <em>List All</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List All</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#listAll()
	 * @generated
	 */
	EOperation getReadableRegistryView__ListAll();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#stream() <em>Stream</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Stream</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView#stream()
	 * @generated
	 */
	EOperation getReadableRegistryView__Stream();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService <em>Readable Scope Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getReadableScopeService()
		 * @generated
		 */
		EClass READABLE_SCOPE_SERVICE = eINSTANCE.getReadableScopeService();

		/**
		 * The meta object literal for the '<em><b>Get Scope Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_SCOPE_SERVICE___GET_SCOPE_NAME = eINSTANCE.getReadableScopeService__GetScopeName();

		/**
		 * The meta object literal for the '<em><b>Is Inheriting From Parent Scope</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_SCOPE_SERVICE___IS_INHERITING_FROM_PARENT_SCOPE = eINSTANCE.getReadableScopeService__IsInheritingFromParentScope();

		/**
		 * The meta object literal for the '<em><b>Get</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_SCOPE_SERVICE___GET__STRING_STRING = eINSTANCE.getReadableScopeService__Get__String_String();

		/**
		 * The meta object literal for the '<em><b>List Object Ids</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_SCOPE_SERVICE___LIST_OBJECT_IDS__STRING = eINSTANCE.getReadableScopeService__ListObjectIds__String();

		/**
		 * The meta object literal for the '<em><b>List All</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_SCOPE_SERVICE___LIST_ALL__STRING = eINSTANCE.getReadableScopeService__ListAll__String();

		/**
		 * The meta object literal for the '<em><b>Stream</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_SCOPE_SERVICE___STREAM__STRING = eINSTANCE.getReadableScopeService__Stream__String();

		/**
		 * The meta object literal for the '<em><b>Get Scope Info</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_SCOPE_SERVICE___GET_SCOPE_INFO = eINSTANCE.getReadableScopeService__GetScopeInfo();

		/**
		 * The meta object literal for the '<em><b>Registry View</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING = eINSTANCE.getReadableScopeService__RegistryView__String();

		/**
		 * The meta object literal for the '<em><b>Registry View</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING_STRING = eINSTANCE.getReadableScopeService__RegistryView__String_String();

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
		 * The meta object literal for the '<em><b>Stages</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REGISTRY_INFO__STAGES = eINSTANCE.getRegistryInfo_Stages();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.scope.api.impl.StageInfoImpl <em>Stage Info</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.StageInfoImpl
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getStageInfo()
		 * @generated
		 */
		EClass STAGE_INFO = eINSTANCE.getStageInfo();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_INFO__NAME = eINSTANCE.getStageInfo_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_INFO__DESCRIPTION = eINSTANCE.getStageInfo_Description();

		/**
		 * The meta object literal for the '<em><b>Readable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_INFO__READABLE = eINSTANCE.getStageInfo_Readable();

		/**
		 * The meta object literal for the '<em><b>Writable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_INFO__WRITABLE = eINSTANCE.getStageInfo_Writable();

		/**
		 * The meta object literal for the '<em><b>Final</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_INFO__FINAL = eINSTANCE.getStageInfo_Final();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView <em>Readable Registry View</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView
		 * @see org.eclipse.fennec.model.atlas.scope.api.impl.ScopeApiPackageImpl#getReadableRegistryView()
		 * @generated
		 */
		EClass READABLE_REGISTRY_VIEW = eINSTANCE.getReadableRegistryView();

		/**
		 * The meta object literal for the '<em><b>Get Scope Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_REGISTRY_VIEW___GET_SCOPE_NAME = eINSTANCE.getReadableRegistryView__GetScopeName();

		/**
		 * The meta object literal for the '<em><b>Get Registry Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_REGISTRY_VIEW___GET_REGISTRY_NAME = eINSTANCE.getReadableRegistryView__GetRegistryName();

		/**
		 * The meta object literal for the '<em><b>Get Stage Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_REGISTRY_VIEW___GET_STAGE_NAME = eINSTANCE.getReadableRegistryView__GetStageName();

		/**
		 * The meta object literal for the '<em><b>Get</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_REGISTRY_VIEW___GET__STRING = eINSTANCE.getReadableRegistryView__Get__String();

		/**
		 * The meta object literal for the '<em><b>List Object Ids</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_REGISTRY_VIEW___LIST_OBJECT_IDS = eINSTANCE.getReadableRegistryView__ListObjectIds();

		/**
		 * The meta object literal for the '<em><b>List All</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_REGISTRY_VIEW___LIST_ALL = eINSTANCE.getReadableRegistryView__ListAll();

		/**
		 * The meta object literal for the '<em><b>Stream</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation READABLE_REGISTRY_VIEW___STREAM = eINSTANCE.getReadableRegistryView__Stream();

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
