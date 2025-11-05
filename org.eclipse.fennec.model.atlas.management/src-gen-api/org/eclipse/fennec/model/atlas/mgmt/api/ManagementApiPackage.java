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
package org.eclipse.fennec.model.atlas.mgmt.api;


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
 * @see org.eclipse.fennec.model.atlas.mgmt.api.ManagementApiFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = ManagementApiPackage.eNS_URI, genModel = "/model/management-api.genmodel", genModelSourceLocations = {"model/management-api.genmodel","org.eclipse.fennec.model.atlas.management/model/management-api.genmodel"}, ecore="/model/management-api.ecore", ecoreSourceLocations="/model/management-api.ecore")
public interface ManagementApiPackage extends org.eclipse.emf.ecore.EPackage {
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
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/management/api/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "mgmtapi";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ManagementApiPackage eINSTANCE = org.eclipse.fennec.model.atlas.mgmt.api.impl.ManagementApiPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService <em>EObject Generation Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.impl.ManagementApiPackageImpl#getEObjectGenerationService()
	 * @generated
	 */
	int EOBJECT_GENERATION_SERVICE = 0;

	/**
	 * The number of structural features of the '<em>EObject Generation Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_GENERATION_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Request Generation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_GENERATION_SERVICE___REQUEST_GENERATION__STRING_STRING_STRING_STRING = 0;

	/**
	 * The operation id for the '<em>Request Generation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_GENERATION_SERVICE___REQUEST_GENERATION__STRING_STRING_STRING_STRING_EOBJECT = 1;

	/**
	 * The operation id for the '<em>Get Generation Status</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_GENERATION_SERVICE___GET_GENERATION_STATUS__STRING = 2;

	/**
	 * The operation id for the '<em>Cancel Generation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_GENERATION_SERVICE___CANCEL_GENERATION__STRING = 3;

	/**
	 * The operation id for the '<em>List Active Generations</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_GENERATION_SERVICE___LIST_ACTIVE_GENERATIONS = 4;

	/**
	 * The number of operations of the '<em>EObject Generation Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_GENERATION_SERVICE_OPERATION_COUNT = 5;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService <em>EObject Discovery Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.impl.ManagementApiPackageImpl#getEObjectDiscoveryService()
	 * @generated
	 */
	int EOBJECT_DISCOVERY_SERVICE = 1;

	/**
	 * The number of structural features of the '<em>EObject Discovery Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_DISCOVERY_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Find Object By Json Pattern</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_DISCOVERY_SERVICE___FIND_OBJECT_BY_JSON_PATTERN__STRING_STRING_STRING = 0;

	/**
	 * The operation id for the '<em>Search Similar Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_DISCOVERY_SERVICE___SEARCH_SIMILAR_OBJECTS__STRING_STRING_DOUBLE = 1;

	/**
	 * The operation id for the '<em>Create Json Fingerprint</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_DISCOVERY_SERVICE___CREATE_JSON_FINGERPRINT__STRING = 2;

	/**
	 * The operation id for the '<em>Is Generation In Progress</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_DISCOVERY_SERVICE___IS_GENERATION_IN_PROGRESS__STRING = 3;

	/**
	 * The operation id for the '<em>Cache Generation Request</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_DISCOVERY_SERVICE___CACHE_GENERATION_REQUEST__STRING_STRING = 4;

	/**
	 * The operation id for the '<em>Remove Generation Request From Cache</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_DISCOVERY_SERVICE___REMOVE_GENERATION_REQUEST_FROM_CACHE__STRING = 5;

	/**
	 * The operation id for the '<em>Cache Object Metadata</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_DISCOVERY_SERVICE___CACHE_OBJECT_METADATA__OBJECTMETADATA = 6;

	/**
	 * The operation id for the '<em>Remove Object Registration From Cache</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_DISCOVERY_SERVICE___REMOVE_OBJECT_REGISTRATION_FROM_CACHE__STRING = 7;

	/**
	 * The operation id for the '<em>Get Generation Request Id For Fingerprint</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_DISCOVERY_SERVICE___GET_GENERATION_REQUEST_ID_FOR_FINGERPRINT__STRING = 8;

	/**
	 * The number of operations of the '<em>EObject Discovery Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_DISCOVERY_SERVICE_OPERATION_COUNT = 9;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService <em>EObject Storage Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.impl.ManagementApiPackageImpl#getEObjectStorageService()
	 * @generated
	 */
	int EOBJECT_STORAGE_SERVICE = 2;

	/**
	 * The number of structural features of the '<em>EObject Storage Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Store Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE___STORE_OBJECT__STRING_EOBJECT_OBJECTMETADATA = 0;

	/**
	 * The operation id for the '<em>Retrieve Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE___RETRIEVE_OBJECT__STRING = 1;

	/**
	 * The operation id for the '<em>Retrieve Metadata</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE___RETRIEVE_METADATA__STRING = 2;

	/**
	 * The operation id for the '<em>Delete Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE___DELETE_OBJECT__STRING = 3;

	/**
	 * The operation id for the '<em>List Object Ids</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE___LIST_OBJECT_IDS = 4;

	/**
	 * The operation id for the '<em>Query Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE___QUERY_OBJECTS__OBJECTQUERY = 5;

	/**
	 * The operation id for the '<em>Update Metadata</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE___UPDATE_METADATA__STRING_OBJECTMETADATA = 6;

	/**
	 * The operation id for the '<em>Update Status</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE___UPDATE_STATUS__STRING_OBJECTSTATUS_STRING = 7;

	/**
	 * The operation id for the '<em>Exists</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE___EXISTS__STRING = 8;

	/**
	 * The operation id for the '<em>Get Object Count</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE___GET_OBJECT_COUNT = 9;

	/**
	 * The operation id for the '<em>Get Backend Type</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE___GET_BACKEND_TYPE = 10;

	/**
	 * The number of operations of the '<em>EObject Storage Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_STORAGE_SERVICE_OPERATION_COUNT = 11;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService <em>EObject Registry Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.impl.ManagementApiPackageImpl#getEObjectRegistryService()
	 * @generated
	 */
	int EOBJECT_REGISTRY_SERVICE = 3;

	/**
	 * The number of structural features of the '<em>EObject Registry Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Get Metadata</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___GET_METADATA__STRING = 0;

	/**
	 * The operation id for the '<em>Find By Object Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___FIND_BY_OBJECT_NAME__STRING = 1;

	/**
	 * The operation id for the '<em>Find By Object Name And Role</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___FIND_BY_OBJECT_NAME_AND_ROLE__STRING_STRING = 2;

	/**
	 * The operation id for the '<em>Find By Role</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___FIND_BY_ROLE__STRING = 3;

	/**
	 * The operation id for the '<em>Find By Status</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___FIND_BY_STATUS__OBJECTSTATUS = 4;

	/**
	 * The operation id for the '<em>Find Pending Approval</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___FIND_PENDING_APPROVAL = 5;

	/**
	 * The operation id for the '<em>Find By Version</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___FIND_BY_VERSION__STRING = 6;

	/**
	 * The operation id for the '<em>Find By Version Pattern</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___FIND_BY_VERSION_PATTERN__STRING = 7;

	/**
	 * The operation id for the '<em>Find By Fingerprint</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___FIND_BY_FINGERPRINT__STRING = 8;

	/**
	 * The operation id for the '<em>Find By Object Type</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___FIND_BY_OBJECT_TYPE__STRING = 9;

	/**
	 * The operation id for the '<em>Find By Status And Type</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___FIND_BY_STATUS_AND_TYPE__OBJECTSTATUS_STRING = 10;

	/**
	 * The operation id for the '<em>Find Recently Modified</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___FIND_RECENTLY_MODIFIED__INSTANT_INT = 11;

	/**
	 * The operation id for the '<em>Update Cache</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___UPDATE_CACHE__OBJECTMETADATA = 12;

	/**
	 * The operation id for the '<em>Remove From Cache</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___REMOVE_FROM_CACHE__STRING = 13;

	/**
	 * The operation id for the '<em>Get Registry Statistics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE___GET_REGISTRY_STATISTICS = 14;

	/**
	 * The number of operations of the '<em>EObject Registry Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_REGISTRY_SERVICE_OPERATION_COUNT = 15;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry <em>Storage Registry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.impl.ManagementApiPackageImpl#getStorageRegistry()
	 * @generated
	 */
	int STORAGE_REGISTRY = 4;

	/**
	 * The number of structural features of the '<em>Storage Registry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STORAGE_REGISTRY_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Get Storage By Role</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STORAGE_REGISTRY___GET_STORAGE_BY_ROLE__STRING = 0;

	/**
	 * The operation id for the '<em>Get All Storages</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STORAGE_REGISTRY___GET_ALL_STORAGES = 1;

	/**
	 * The operation id for the '<em>Get Available Roles</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STORAGE_REGISTRY___GET_AVAILABLE_ROLES = 2;

	/**
	 * The operation id for the '<em>Update Governance Documentation Id</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STORAGE_REGISTRY___UPDATE_GOVERNANCE_DOCUMENTATION_ID__STRING_STRING_STRING_STRING = 3;

	/**
	 * The operation id for the '<em>Search Metadata Across Roles</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STORAGE_REGISTRY___SEARCH_METADATA_ACROSS_ROLES__OBJECTQUERY = 4;

	/**
	 * The operation id for the '<em>Get Storage Statistics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STORAGE_REGISTRY___GET_STORAGE_STATISTICS = 5;

	/**
	 * The number of operations of the '<em>Storage Registry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STORAGE_REGISTRY_OPERATION_COUNT = 6;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService <em>EObject Generation Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EObject Generation Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService
	 * @generated
	 */
	EClass getEObjectGenerationService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService#requestGeneration(java.lang.String, java.lang.String, java.lang.String, java.lang.String) <em>Request Generation</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Request Generation</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService#requestGeneration(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectGenerationService__RequestGeneration__String_String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService#requestGeneration(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject) <em>Request Generation</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Request Generation</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService#requestGeneration(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	EOperation getEObjectGenerationService__RequestGeneration__String_String_String_String_EObject();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService#getGenerationStatus(java.lang.String) <em>Get Generation Status</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Generation Status</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService#getGenerationStatus(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectGenerationService__GetGenerationStatus__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService#cancelGeneration(java.lang.String) <em>Cancel Generation</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Cancel Generation</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService#cancelGeneration(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectGenerationService__CancelGeneration__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService#listActiveGenerations() <em>List Active Generations</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Active Generations</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService#listActiveGenerations()
	 * @generated
	 */
	EOperation getEObjectGenerationService__ListActiveGenerations();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService <em>EObject Discovery Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EObject Discovery Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService
	 * @generated
	 */
	EClass getEObjectDiscoveryService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#findObjectByJsonPattern(java.lang.String, java.lang.String, java.lang.String) <em>Find Object By Json Pattern</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find Object By Json Pattern</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#findObjectByJsonPattern(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectDiscoveryService__FindObjectByJsonPattern__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#searchSimilarObjects(java.lang.String, java.lang.String, double) <em>Search Similar Objects</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Search Similar Objects</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#searchSimilarObjects(java.lang.String, java.lang.String, double)
	 * @generated
	 */
	EOperation getEObjectDiscoveryService__SearchSimilarObjects__String_String_double();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#createJsonFingerprint(java.lang.String) <em>Create Json Fingerprint</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Create Json Fingerprint</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#createJsonFingerprint(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectDiscoveryService__CreateJsonFingerprint__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#isGenerationInProgress(java.lang.String) <em>Is Generation In Progress</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Generation In Progress</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#isGenerationInProgress(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectDiscoveryService__IsGenerationInProgress__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#cacheGenerationRequest(java.lang.String, java.lang.String) <em>Cache Generation Request</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Cache Generation Request</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#cacheGenerationRequest(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectDiscoveryService__CacheGenerationRequest__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#removeGenerationRequestFromCache(java.lang.String) <em>Remove Generation Request From Cache</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Remove Generation Request From Cache</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#removeGenerationRequestFromCache(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectDiscoveryService__RemoveGenerationRequestFromCache__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#cacheObjectMetadata(org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata) <em>Cache Object Metadata</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Cache Object Metadata</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#cacheObjectMetadata(org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 * @generated
	 */
	EOperation getEObjectDiscoveryService__CacheObjectMetadata__ObjectMetadata();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#removeObjectRegistrationFromCache(java.lang.String) <em>Remove Object Registration From Cache</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Remove Object Registration From Cache</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#removeObjectRegistrationFromCache(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectDiscoveryService__RemoveObjectRegistrationFromCache__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#getGenerationRequestIdForFingerprint(java.lang.String) <em>Get Generation Request Id For Fingerprint</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Generation Request Id For Fingerprint</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService#getGenerationRequestIdForFingerprint(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectDiscoveryService__GetGenerationRequestIdForFingerprint__String();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService <em>EObject Storage Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EObject Storage Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService
	 * @generated
	 */
	EClass getEObjectStorageService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#storeObject(java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata) <em>Store Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Store Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#storeObject(java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 * @generated
	 */
	EOperation getEObjectStorageService__StoreObject__String_EObject_ObjectMetadata();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#retrieveObject(java.lang.String) <em>Retrieve Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Retrieve Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#retrieveObject(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectStorageService__RetrieveObject__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#retrieveMetadata(java.lang.String) <em>Retrieve Metadata</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Retrieve Metadata</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#retrieveMetadata(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectStorageService__RetrieveMetadata__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#deleteObject(java.lang.String) <em>Delete Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#deleteObject(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectStorageService__DeleteObject__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#listObjectIds() <em>List Object Ids</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Object Ids</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#listObjectIds()
	 * @generated
	 */
	EOperation getEObjectStorageService__ListObjectIds();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#queryObjects(org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery) <em>Query Objects</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Query Objects</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#queryObjects(org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery)
	 * @generated
	 */
	EOperation getEObjectStorageService__QueryObjects__ObjectQuery();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#updateMetadata(java.lang.String, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata) <em>Update Metadata</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Metadata</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#updateMetadata(java.lang.String, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 * @generated
	 */
	EOperation getEObjectStorageService__UpdateMetadata__String_ObjectMetadata();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#updateStatus(java.lang.String, org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus, java.lang.String) <em>Update Status</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Status</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#updateStatus(java.lang.String, org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectStorageService__UpdateStatus__String_ObjectStatus_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#exists(java.lang.String) <em>Exists</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Exists</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#exists(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectStorageService__Exists__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#getObjectCount() <em>Get Object Count</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Object Count</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#getObjectCount()
	 * @generated
	 */
	EOperation getEObjectStorageService__GetObjectCount();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#getBackendType() <em>Get Backend Type</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Backend Type</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService#getBackendType()
	 * @generated
	 */
	EOperation getEObjectStorageService__GetBackendType();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService <em>EObject Registry Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EObject Registry Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService
	 * @generated
	 */
	EClass getEObjectRegistryService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#getMetadata(java.lang.String) <em>Get Metadata</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Metadata</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#getMetadata(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectRegistryService__GetMetadata__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByObjectName(java.lang.String) <em>Find By Object Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find By Object Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByObjectName(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectRegistryService__FindByObjectName__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByObjectNameAndRole(java.lang.String, java.lang.String) <em>Find By Object Name And Role</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find By Object Name And Role</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByObjectNameAndRole(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectRegistryService__FindByObjectNameAndRole__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByRole(java.lang.String) <em>Find By Role</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find By Role</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByRole(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectRegistryService__FindByRole__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByStatus(org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus) <em>Find By Status</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find By Status</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByStatus(org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus)
	 * @generated
	 */
	EOperation getEObjectRegistryService__FindByStatus__ObjectStatus();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findPendingApproval() <em>Find Pending Approval</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find Pending Approval</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findPendingApproval()
	 * @generated
	 */
	EOperation getEObjectRegistryService__FindPendingApproval();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByVersion(java.lang.String) <em>Find By Version</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find By Version</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByVersion(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectRegistryService__FindByVersion__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByVersionPattern(java.lang.String) <em>Find By Version Pattern</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find By Version Pattern</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByVersionPattern(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectRegistryService__FindByVersionPattern__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByFingerprint(java.lang.String) <em>Find By Fingerprint</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find By Fingerprint</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByFingerprint(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectRegistryService__FindByFingerprint__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByObjectType(java.lang.String) <em>Find By Object Type</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find By Object Type</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByObjectType(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectRegistryService__FindByObjectType__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByStatusAndType(org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus, java.lang.String) <em>Find By Status And Type</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find By Status And Type</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByStatusAndType(org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectRegistryService__FindByStatusAndType__ObjectStatus_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findRecentlyModified(java.time.Instant, int) <em>Find Recently Modified</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find Recently Modified</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findRecentlyModified(java.time.Instant, int)
	 * @generated
	 */
	EOperation getEObjectRegistryService__FindRecentlyModified__Instant_int();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#updateCache(org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata) <em>Update Cache</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Cache</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#updateCache(org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 * @generated
	 */
	EOperation getEObjectRegistryService__UpdateCache__ObjectMetadata();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#removeFromCache(java.lang.String) <em>Remove From Cache</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Remove From Cache</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#removeFromCache(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectRegistryService__RemoveFromCache__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#getRegistryStatistics() <em>Get Registry Statistics</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Registry Statistics</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#getRegistryStatistics()
	 * @generated
	 */
	EOperation getEObjectRegistryService__GetRegistryStatistics();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry <em>Storage Registry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Storage Registry</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry
	 * @generated
	 */
	EClass getStorageRegistry();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#getStorageByRole(java.lang.String) <em>Get Storage By Role</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Storage By Role</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#getStorageByRole(java.lang.String)
	 * @generated
	 */
	EOperation getStorageRegistry__GetStorageByRole__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#getAllStorages() <em>Get All Storages</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get All Storages</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#getAllStorages()
	 * @generated
	 */
	EOperation getStorageRegistry__GetAllStorages();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#getAvailableRoles() <em>Get Available Roles</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Available Roles</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#getAvailableRoles()
	 * @generated
	 */
	EOperation getStorageRegistry__GetAvailableRoles();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#updateGovernanceDocumentationId(java.lang.String, java.lang.String, java.lang.String, java.lang.String) <em>Update Governance Documentation Id</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Governance Documentation Id</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#updateGovernanceDocumentationId(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getStorageRegistry__UpdateGovernanceDocumentationId__String_String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#searchMetadataAcrossRoles(org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery) <em>Search Metadata Across Roles</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Search Metadata Across Roles</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#searchMetadataAcrossRoles(org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery)
	 * @generated
	 */
	EOperation getStorageRegistry__SearchMetadataAcrossRoles__ObjectQuery();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#getStorageStatistics() <em>Get Storage Statistics</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Storage Statistics</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#getStorageStatistics()
	 * @generated
	 */
	EOperation getStorageRegistry__GetStorageStatistics();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ManagementApiFactory getManagementApiFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService <em>EObject Generation Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectGenerationService
		 * @see org.eclipse.fennec.model.atlas.mgmt.api.impl.ManagementApiPackageImpl#getEObjectGenerationService()
		 * @generated
		 */
		EClass EOBJECT_GENERATION_SERVICE = eINSTANCE.getEObjectGenerationService();

		/**
		 * The meta object literal for the '<em><b>Request Generation</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_GENERATION_SERVICE___REQUEST_GENERATION__STRING_STRING_STRING_STRING = eINSTANCE.getEObjectGenerationService__RequestGeneration__String_String_String_String();

		/**
		 * The meta object literal for the '<em><b>Request Generation</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_GENERATION_SERVICE___REQUEST_GENERATION__STRING_STRING_STRING_STRING_EOBJECT = eINSTANCE.getEObjectGenerationService__RequestGeneration__String_String_String_String_EObject();

		/**
		 * The meta object literal for the '<em><b>Get Generation Status</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_GENERATION_SERVICE___GET_GENERATION_STATUS__STRING = eINSTANCE.getEObjectGenerationService__GetGenerationStatus__String();

		/**
		 * The meta object literal for the '<em><b>Cancel Generation</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_GENERATION_SERVICE___CANCEL_GENERATION__STRING = eINSTANCE.getEObjectGenerationService__CancelGeneration__String();

		/**
		 * The meta object literal for the '<em><b>List Active Generations</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_GENERATION_SERVICE___LIST_ACTIVE_GENERATIONS = eINSTANCE.getEObjectGenerationService__ListActiveGenerations();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService <em>EObject Discovery Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectDiscoveryService
		 * @see org.eclipse.fennec.model.atlas.mgmt.api.impl.ManagementApiPackageImpl#getEObjectDiscoveryService()
		 * @generated
		 */
		EClass EOBJECT_DISCOVERY_SERVICE = eINSTANCE.getEObjectDiscoveryService();

		/**
		 * The meta object literal for the '<em><b>Find Object By Json Pattern</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_DISCOVERY_SERVICE___FIND_OBJECT_BY_JSON_PATTERN__STRING_STRING_STRING = eINSTANCE.getEObjectDiscoveryService__FindObjectByJsonPattern__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Search Similar Objects</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_DISCOVERY_SERVICE___SEARCH_SIMILAR_OBJECTS__STRING_STRING_DOUBLE = eINSTANCE.getEObjectDiscoveryService__SearchSimilarObjects__String_String_double();

		/**
		 * The meta object literal for the '<em><b>Create Json Fingerprint</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_DISCOVERY_SERVICE___CREATE_JSON_FINGERPRINT__STRING = eINSTANCE.getEObjectDiscoveryService__CreateJsonFingerprint__String();

		/**
		 * The meta object literal for the '<em><b>Is Generation In Progress</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_DISCOVERY_SERVICE___IS_GENERATION_IN_PROGRESS__STRING = eINSTANCE.getEObjectDiscoveryService__IsGenerationInProgress__String();

		/**
		 * The meta object literal for the '<em><b>Cache Generation Request</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_DISCOVERY_SERVICE___CACHE_GENERATION_REQUEST__STRING_STRING = eINSTANCE.getEObjectDiscoveryService__CacheGenerationRequest__String_String();

		/**
		 * The meta object literal for the '<em><b>Remove Generation Request From Cache</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_DISCOVERY_SERVICE___REMOVE_GENERATION_REQUEST_FROM_CACHE__STRING = eINSTANCE.getEObjectDiscoveryService__RemoveGenerationRequestFromCache__String();

		/**
		 * The meta object literal for the '<em><b>Cache Object Metadata</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_DISCOVERY_SERVICE___CACHE_OBJECT_METADATA__OBJECTMETADATA = eINSTANCE.getEObjectDiscoveryService__CacheObjectMetadata__ObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>Remove Object Registration From Cache</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_DISCOVERY_SERVICE___REMOVE_OBJECT_REGISTRATION_FROM_CACHE__STRING = eINSTANCE.getEObjectDiscoveryService__RemoveObjectRegistrationFromCache__String();

		/**
		 * The meta object literal for the '<em><b>Get Generation Request Id For Fingerprint</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_DISCOVERY_SERVICE___GET_GENERATION_REQUEST_ID_FOR_FINGERPRINT__STRING = eINSTANCE.getEObjectDiscoveryService__GetGenerationRequestIdForFingerprint__String();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService <em>EObject Storage Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService
		 * @see org.eclipse.fennec.model.atlas.mgmt.api.impl.ManagementApiPackageImpl#getEObjectStorageService()
		 * @generated
		 */
		EClass EOBJECT_STORAGE_SERVICE = eINSTANCE.getEObjectStorageService();

		/**
		 * The meta object literal for the '<em><b>Store Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_STORAGE_SERVICE___STORE_OBJECT__STRING_EOBJECT_OBJECTMETADATA = eINSTANCE.getEObjectStorageService__StoreObject__String_EObject_ObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>Retrieve Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_STORAGE_SERVICE___RETRIEVE_OBJECT__STRING = eINSTANCE.getEObjectStorageService__RetrieveObject__String();

		/**
		 * The meta object literal for the '<em><b>Retrieve Metadata</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_STORAGE_SERVICE___RETRIEVE_METADATA__STRING = eINSTANCE.getEObjectStorageService__RetrieveMetadata__String();

		/**
		 * The meta object literal for the '<em><b>Delete Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_STORAGE_SERVICE___DELETE_OBJECT__STRING = eINSTANCE.getEObjectStorageService__DeleteObject__String();

		/**
		 * The meta object literal for the '<em><b>List Object Ids</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_STORAGE_SERVICE___LIST_OBJECT_IDS = eINSTANCE.getEObjectStorageService__ListObjectIds();

		/**
		 * The meta object literal for the '<em><b>Query Objects</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_STORAGE_SERVICE___QUERY_OBJECTS__OBJECTQUERY = eINSTANCE.getEObjectStorageService__QueryObjects__ObjectQuery();

		/**
		 * The meta object literal for the '<em><b>Update Metadata</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_STORAGE_SERVICE___UPDATE_METADATA__STRING_OBJECTMETADATA = eINSTANCE.getEObjectStorageService__UpdateMetadata__String_ObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>Update Status</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_STORAGE_SERVICE___UPDATE_STATUS__STRING_OBJECTSTATUS_STRING = eINSTANCE.getEObjectStorageService__UpdateStatus__String_ObjectStatus_String();

		/**
		 * The meta object literal for the '<em><b>Exists</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_STORAGE_SERVICE___EXISTS__STRING = eINSTANCE.getEObjectStorageService__Exists__String();

		/**
		 * The meta object literal for the '<em><b>Get Object Count</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_STORAGE_SERVICE___GET_OBJECT_COUNT = eINSTANCE.getEObjectStorageService__GetObjectCount();

		/**
		 * The meta object literal for the '<em><b>Get Backend Type</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_STORAGE_SERVICE___GET_BACKEND_TYPE = eINSTANCE.getEObjectStorageService__GetBackendType();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService <em>EObject Registry Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService
		 * @see org.eclipse.fennec.model.atlas.mgmt.api.impl.ManagementApiPackageImpl#getEObjectRegistryService()
		 * @generated
		 */
		EClass EOBJECT_REGISTRY_SERVICE = eINSTANCE.getEObjectRegistryService();

		/**
		 * The meta object literal for the '<em><b>Get Metadata</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___GET_METADATA__STRING = eINSTANCE.getEObjectRegistryService__GetMetadata__String();

		/**
		 * The meta object literal for the '<em><b>Find By Object Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___FIND_BY_OBJECT_NAME__STRING = eINSTANCE.getEObjectRegistryService__FindByObjectName__String();

		/**
		 * The meta object literal for the '<em><b>Find By Object Name And Role</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___FIND_BY_OBJECT_NAME_AND_ROLE__STRING_STRING = eINSTANCE.getEObjectRegistryService__FindByObjectNameAndRole__String_String();

		/**
		 * The meta object literal for the '<em><b>Find By Role</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___FIND_BY_ROLE__STRING = eINSTANCE.getEObjectRegistryService__FindByRole__String();

		/**
		 * The meta object literal for the '<em><b>Find By Status</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___FIND_BY_STATUS__OBJECTSTATUS = eINSTANCE.getEObjectRegistryService__FindByStatus__ObjectStatus();

		/**
		 * The meta object literal for the '<em><b>Find Pending Approval</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___FIND_PENDING_APPROVAL = eINSTANCE.getEObjectRegistryService__FindPendingApproval();

		/**
		 * The meta object literal for the '<em><b>Find By Version</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___FIND_BY_VERSION__STRING = eINSTANCE.getEObjectRegistryService__FindByVersion__String();

		/**
		 * The meta object literal for the '<em><b>Find By Version Pattern</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___FIND_BY_VERSION_PATTERN__STRING = eINSTANCE.getEObjectRegistryService__FindByVersionPattern__String();

		/**
		 * The meta object literal for the '<em><b>Find By Fingerprint</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___FIND_BY_FINGERPRINT__STRING = eINSTANCE.getEObjectRegistryService__FindByFingerprint__String();

		/**
		 * The meta object literal for the '<em><b>Find By Object Type</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___FIND_BY_OBJECT_TYPE__STRING = eINSTANCE.getEObjectRegistryService__FindByObjectType__String();

		/**
		 * The meta object literal for the '<em><b>Find By Status And Type</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___FIND_BY_STATUS_AND_TYPE__OBJECTSTATUS_STRING = eINSTANCE.getEObjectRegistryService__FindByStatusAndType__ObjectStatus_String();

		/**
		 * The meta object literal for the '<em><b>Find Recently Modified</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___FIND_RECENTLY_MODIFIED__INSTANT_INT = eINSTANCE.getEObjectRegistryService__FindRecentlyModified__Instant_int();

		/**
		 * The meta object literal for the '<em><b>Update Cache</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___UPDATE_CACHE__OBJECTMETADATA = eINSTANCE.getEObjectRegistryService__UpdateCache__ObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>Remove From Cache</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___REMOVE_FROM_CACHE__STRING = eINSTANCE.getEObjectRegistryService__RemoveFromCache__String();

		/**
		 * The meta object literal for the '<em><b>Get Registry Statistics</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_REGISTRY_SERVICE___GET_REGISTRY_STATISTICS = eINSTANCE.getEObjectRegistryService__GetRegistryStatistics();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry <em>Storage Registry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry
		 * @see org.eclipse.fennec.model.atlas.mgmt.api.impl.ManagementApiPackageImpl#getStorageRegistry()
		 * @generated
		 */
		EClass STORAGE_REGISTRY = eINSTANCE.getStorageRegistry();

		/**
		 * The meta object literal for the '<em><b>Get Storage By Role</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation STORAGE_REGISTRY___GET_STORAGE_BY_ROLE__STRING = eINSTANCE.getStorageRegistry__GetStorageByRole__String();

		/**
		 * The meta object literal for the '<em><b>Get All Storages</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation STORAGE_REGISTRY___GET_ALL_STORAGES = eINSTANCE.getStorageRegistry__GetAllStorages();

		/**
		 * The meta object literal for the '<em><b>Get Available Roles</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation STORAGE_REGISTRY___GET_AVAILABLE_ROLES = eINSTANCE.getStorageRegistry__GetAvailableRoles();

		/**
		 * The meta object literal for the '<em><b>Update Governance Documentation Id</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation STORAGE_REGISTRY___UPDATE_GOVERNANCE_DOCUMENTATION_ID__STRING_STRING_STRING_STRING = eINSTANCE.getStorageRegistry__UpdateGovernanceDocumentationId__String_String_String_String();

		/**
		 * The meta object literal for the '<em><b>Search Metadata Across Roles</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation STORAGE_REGISTRY___SEARCH_METADATA_ACROSS_ROLES__OBJECTQUERY = eINSTANCE.getStorageRegistry__SearchMetadataAcrossRoles__ObjectQuery();

		/**
		 * The meta object literal for the '<em><b>Get Storage Statistics</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation STORAGE_REGISTRY___GET_STORAGE_STATISTICS = eINSTANCE.getStorageRegistry__GetStorageStatistics();

	}

} //ManagementApiPackage
