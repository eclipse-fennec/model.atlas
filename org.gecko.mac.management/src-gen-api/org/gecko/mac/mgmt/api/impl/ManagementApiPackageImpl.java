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
package org.gecko.mac.mgmt.api.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.ETypeParameter;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.gecko.mac.mgmt.api.EObjectDiscoveryService;
import org.gecko.mac.mgmt.api.EObjectGenerationService;
import org.gecko.mac.mgmt.api.EObjectRegistryService;
import org.gecko.mac.mgmt.api.EObjectStorageService;
import org.gecko.mac.mgmt.api.ManagementApiFactory;
import org.gecko.mac.mgmt.api.ManagementApiPackage;
import org.gecko.mac.mgmt.api.StorageRegistry;

import org.gecko.mac.mgmt.management.ManagementPackage;

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
	private EClass eObjectGenerationServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eObjectDiscoveryServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eObjectStorageServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eObjectRegistryServiceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass storageRegistryEClass = null;

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
	 * @see org.gecko.mac.mgmt.api.ManagementApiPackage#eNS_URI
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
	public EClass getEObjectGenerationService() {
		return eObjectGenerationServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectGenerationService__RequestGeneration__String_String_String_String() {
		return eObjectGenerationServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectGenerationService__RequestGeneration__String_String_String_String_EObject() {
		return eObjectGenerationServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectGenerationService__GetGenerationStatus__String() {
		return eObjectGenerationServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectGenerationService__CancelGeneration__String() {
		return eObjectGenerationServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectGenerationService__ListActiveGenerations() {
		return eObjectGenerationServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEObjectDiscoveryService() {
		return eObjectDiscoveryServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectDiscoveryService__FindObjectByJsonPattern__String_String_String() {
		return eObjectDiscoveryServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectDiscoveryService__SearchSimilarObjects__String_String_double() {
		return eObjectDiscoveryServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectDiscoveryService__CreateJsonFingerprint__String() {
		return eObjectDiscoveryServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectDiscoveryService__IsGenerationInProgress__String() {
		return eObjectDiscoveryServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectDiscoveryService__CacheGenerationRequest__String_String() {
		return eObjectDiscoveryServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectDiscoveryService__RemoveGenerationRequestFromCache__String() {
		return eObjectDiscoveryServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectDiscoveryService__CacheObjectMetadata__ObjectMetadata() {
		return eObjectDiscoveryServiceEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectDiscoveryService__RemoveObjectRegistrationFromCache__String() {
		return eObjectDiscoveryServiceEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectDiscoveryService__GetGenerationRequestIdForFingerprint__String() {
		return eObjectDiscoveryServiceEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEObjectStorageService() {
		return eObjectStorageServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectStorageService__StoreObject__String_EObject_ObjectMetadata() {
		return eObjectStorageServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectStorageService__RetrieveObject__String() {
		return eObjectStorageServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectStorageService__RetrieveMetadata__String() {
		return eObjectStorageServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectStorageService__DeleteObject__String() {
		return eObjectStorageServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectStorageService__ListObjectIds() {
		return eObjectStorageServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectStorageService__QueryObjects__ObjectQuery() {
		return eObjectStorageServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectStorageService__UpdateMetadata__String_ObjectMetadata() {
		return eObjectStorageServiceEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectStorageService__UpdateStatus__String_ObjectStatus_String() {
		return eObjectStorageServiceEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectStorageService__Exists__String() {
		return eObjectStorageServiceEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectStorageService__GetObjectCount() {
		return eObjectStorageServiceEClass.getEOperations().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectStorageService__GetBackendType() {
		return eObjectStorageServiceEClass.getEOperations().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEObjectRegistryService() {
		return eObjectRegistryServiceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__GetMetadata__String() {
		return eObjectRegistryServiceEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__FindByObjectName__String() {
		return eObjectRegistryServiceEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__FindByObjectNameAndRole__String_String() {
		return eObjectRegistryServiceEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__FindByRole__String() {
		return eObjectRegistryServiceEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__FindByStatus__ObjectStatus() {
		return eObjectRegistryServiceEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__FindPendingApproval() {
		return eObjectRegistryServiceEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__FindByVersion__String() {
		return eObjectRegistryServiceEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__FindByVersionPattern__String() {
		return eObjectRegistryServiceEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__FindByFingerprint__String() {
		return eObjectRegistryServiceEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__FindByObjectType__String() {
		return eObjectRegistryServiceEClass.getEOperations().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__FindByStatusAndType__ObjectStatus_String() {
		return eObjectRegistryServiceEClass.getEOperations().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__FindRecentlyModified__Instant_int() {
		return eObjectRegistryServiceEClass.getEOperations().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__UpdateCache__ObjectMetadata() {
		return eObjectRegistryServiceEClass.getEOperations().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__RemoveFromCache__String() {
		return eObjectRegistryServiceEClass.getEOperations().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getEObjectRegistryService__GetRegistryStatistics() {
		return eObjectRegistryServiceEClass.getEOperations().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getStorageRegistry() {
		return storageRegistryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getStorageRegistry__GetStorageByRole__String() {
		return storageRegistryEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getStorageRegistry__GetAllStorages() {
		return storageRegistryEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getStorageRegistry__GetAvailableRoles() {
		return storageRegistryEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getStorageRegistry__UpdateGovernanceDocumentationId__String_String_String_String() {
		return storageRegistryEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getStorageRegistry__SearchMetadataAcrossRoles__ObjectQuery() {
		return storageRegistryEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getStorageRegistry__GetStorageStatistics() {
		return storageRegistryEClass.getEOperations().get(5);
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
		eObjectGenerationServiceEClass = createEClass(EOBJECT_GENERATION_SERVICE);
		createEOperation(eObjectGenerationServiceEClass, EOBJECT_GENERATION_SERVICE___REQUEST_GENERATION__STRING_STRING_STRING_STRING);
		createEOperation(eObjectGenerationServiceEClass, EOBJECT_GENERATION_SERVICE___REQUEST_GENERATION__STRING_STRING_STRING_STRING_EOBJECT);
		createEOperation(eObjectGenerationServiceEClass, EOBJECT_GENERATION_SERVICE___GET_GENERATION_STATUS__STRING);
		createEOperation(eObjectGenerationServiceEClass, EOBJECT_GENERATION_SERVICE___CANCEL_GENERATION__STRING);
		createEOperation(eObjectGenerationServiceEClass, EOBJECT_GENERATION_SERVICE___LIST_ACTIVE_GENERATIONS);

		eObjectDiscoveryServiceEClass = createEClass(EOBJECT_DISCOVERY_SERVICE);
		createEOperation(eObjectDiscoveryServiceEClass, EOBJECT_DISCOVERY_SERVICE___FIND_OBJECT_BY_JSON_PATTERN__STRING_STRING_STRING);
		createEOperation(eObjectDiscoveryServiceEClass, EOBJECT_DISCOVERY_SERVICE___SEARCH_SIMILAR_OBJECTS__STRING_STRING_DOUBLE);
		createEOperation(eObjectDiscoveryServiceEClass, EOBJECT_DISCOVERY_SERVICE___CREATE_JSON_FINGERPRINT__STRING);
		createEOperation(eObjectDiscoveryServiceEClass, EOBJECT_DISCOVERY_SERVICE___IS_GENERATION_IN_PROGRESS__STRING);
		createEOperation(eObjectDiscoveryServiceEClass, EOBJECT_DISCOVERY_SERVICE___CACHE_GENERATION_REQUEST__STRING_STRING);
		createEOperation(eObjectDiscoveryServiceEClass, EOBJECT_DISCOVERY_SERVICE___REMOVE_GENERATION_REQUEST_FROM_CACHE__STRING);
		createEOperation(eObjectDiscoveryServiceEClass, EOBJECT_DISCOVERY_SERVICE___CACHE_OBJECT_METADATA__OBJECTMETADATA);
		createEOperation(eObjectDiscoveryServiceEClass, EOBJECT_DISCOVERY_SERVICE___REMOVE_OBJECT_REGISTRATION_FROM_CACHE__STRING);
		createEOperation(eObjectDiscoveryServiceEClass, EOBJECT_DISCOVERY_SERVICE___GET_GENERATION_REQUEST_ID_FOR_FINGERPRINT__STRING);

		eObjectStorageServiceEClass = createEClass(EOBJECT_STORAGE_SERVICE);
		createEOperation(eObjectStorageServiceEClass, EOBJECT_STORAGE_SERVICE___STORE_OBJECT__STRING_EOBJECT_OBJECTMETADATA);
		createEOperation(eObjectStorageServiceEClass, EOBJECT_STORAGE_SERVICE___RETRIEVE_OBJECT__STRING);
		createEOperation(eObjectStorageServiceEClass, EOBJECT_STORAGE_SERVICE___RETRIEVE_METADATA__STRING);
		createEOperation(eObjectStorageServiceEClass, EOBJECT_STORAGE_SERVICE___DELETE_OBJECT__STRING);
		createEOperation(eObjectStorageServiceEClass, EOBJECT_STORAGE_SERVICE___LIST_OBJECT_IDS);
		createEOperation(eObjectStorageServiceEClass, EOBJECT_STORAGE_SERVICE___QUERY_OBJECTS__OBJECTQUERY);
		createEOperation(eObjectStorageServiceEClass, EOBJECT_STORAGE_SERVICE___UPDATE_METADATA__STRING_OBJECTMETADATA);
		createEOperation(eObjectStorageServiceEClass, EOBJECT_STORAGE_SERVICE___UPDATE_STATUS__STRING_OBJECTSTATUS_STRING);
		createEOperation(eObjectStorageServiceEClass, EOBJECT_STORAGE_SERVICE___EXISTS__STRING);
		createEOperation(eObjectStorageServiceEClass, EOBJECT_STORAGE_SERVICE___GET_OBJECT_COUNT);
		createEOperation(eObjectStorageServiceEClass, EOBJECT_STORAGE_SERVICE___GET_BACKEND_TYPE);

		eObjectRegistryServiceEClass = createEClass(EOBJECT_REGISTRY_SERVICE);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___GET_METADATA__STRING);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___FIND_BY_OBJECT_NAME__STRING);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___FIND_BY_OBJECT_NAME_AND_ROLE__STRING_STRING);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___FIND_BY_ROLE__STRING);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___FIND_BY_STATUS__OBJECTSTATUS);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___FIND_PENDING_APPROVAL);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___FIND_BY_VERSION__STRING);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___FIND_BY_VERSION_PATTERN__STRING);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___FIND_BY_FINGERPRINT__STRING);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___FIND_BY_OBJECT_TYPE__STRING);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___FIND_BY_STATUS_AND_TYPE__OBJECTSTATUS_STRING);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___FIND_RECENTLY_MODIFIED__INSTANT_INT);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___UPDATE_CACHE__OBJECTMETADATA);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___REMOVE_FROM_CACHE__STRING);
		createEOperation(eObjectRegistryServiceEClass, EOBJECT_REGISTRY_SERVICE___GET_REGISTRY_STATISTICS);

		storageRegistryEClass = createEClass(STORAGE_REGISTRY);
		createEOperation(storageRegistryEClass, STORAGE_REGISTRY___GET_STORAGE_BY_ROLE__STRING);
		createEOperation(storageRegistryEClass, STORAGE_REGISTRY___GET_ALL_STORAGES);
		createEOperation(storageRegistryEClass, STORAGE_REGISTRY___GET_AVAILABLE_ROLES);
		createEOperation(storageRegistryEClass, STORAGE_REGISTRY___UPDATE_GOVERNANCE_DOCUMENTATION_ID__STRING_STRING_STRING_STRING);
		createEOperation(storageRegistryEClass, STORAGE_REGISTRY___SEARCH_METADATA_ACROSS_ROLES__OBJECTQUERY);
		createEOperation(storageRegistryEClass, STORAGE_REGISTRY___GET_STORAGE_STATISTICS);
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
		ETypeParameter eObjectGenerationServiceEClass_T = addETypeParameter(eObjectGenerationServiceEClass, "T");
		ETypeParameter eObjectDiscoveryServiceEClass_T = addETypeParameter(eObjectDiscoveryServiceEClass, "T");
		ETypeParameter eObjectStorageServiceEClass_T = addETypeParameter(eObjectStorageServiceEClass, "T");
		ETypeParameter eObjectRegistryServiceEClass_T = addETypeParameter(eObjectRegistryServiceEClass, "T");

		// Set bounds for type parameters
		EGenericType g1 = createEGenericType(ecorePackage.getEObject());
		eObjectGenerationServiceEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(ecorePackage.getEObject());
		eObjectDiscoveryServiceEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(ecorePackage.getEObject());
		eObjectStorageServiceEClass_T.getEBounds().add(g1);
		g1 = createEGenericType(ecorePackage.getEObject());
		eObjectRegistryServiceEClass_T.getEBounds().add(g1);

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(eObjectGenerationServiceEClass, EObjectGenerationService.class, "EObjectGenerationService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		EOperation op = initEOperation(getEObjectGenerationService__RequestGeneration__String_String_String_String(), null, "requestGeneration", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "jsonSample", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "sourceChannel", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "requestingUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "targetType", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		EGenericType g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectGenerationService__RequestGeneration__String_String_String_String_EObject(), null, "requestGeneration", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "jsonSample", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "sourceChannel", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "requestingUser", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "targetType", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectGenerationServiceEClass_T);
		addEParameter(op, g1, "parentEObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectGenerationService__GetGenerationStatus__String(), null, "getGenerationStatus", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "requestId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getGenerationRequest());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectGenerationService__CancelGeneration__String(), null, "cancelGeneration", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "requestId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectGenerationService__ListActiveGenerations(), null, "listActiveGenerations", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getList());
		g1.getETypeArguments().add(g2);
		EGenericType g3 = createEGenericType(theManagementPackage.getGenerationRequest());
		g2.getETypeArguments().add(g3);
		initEOperation(op, g1);

		initEClass(eObjectDiscoveryServiceEClass, EObjectDiscoveryService.class, "EObjectDiscoveryService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getEObjectDiscoveryService__FindObjectByJsonPattern__String_String_String(), null, "findObjectByJsonPattern", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "jsonSample", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "sourceChannel", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "targetType", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectDiscoveryService__SearchSimilarObjects__String_String_double(), null, "searchSimilarObjects", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "jsonSample", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "targetType", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEDouble(), "similarityThreshold", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getList());
		g1.getETypeArguments().add(g2);
		g3 = createEGenericType(theManagementPackage.getObjectMetadata());
		g2.getETypeArguments().add(g3);
		initEOperation(op, g1);

		op = initEOperation(getEObjectDiscoveryService__CreateJsonFingerprint__String(), null, "createJsonFingerprint", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "jsonSample", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectDiscoveryService__IsGenerationInProgress__String(), null, "isGenerationInProgress", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "jsonFingerprint", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectDiscoveryService__CacheGenerationRequest__String_String(), null, "cacheGenerationRequest", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "jsonFingerprint", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "requestId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getVoid());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectDiscoveryService__RemoveGenerationRequestFromCache__String(), null, "removeGenerationRequestFromCache", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "jsonFingerprint", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getVoid());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectDiscoveryService__CacheObjectMetadata__ObjectMetadata(), null, "cacheObjectMetadata", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectMetadata(), "objectRegistration", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getVoid());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectDiscoveryService__RemoveObjectRegistrationFromCache__String(), null, "removeObjectRegistrationFromCache", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getVoid());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectDiscoveryService__GetGenerationRequestIdForFingerprint__String(), null, "getGenerationRequestIdForFingerprint", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "jsonFingerprint", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEClass(eObjectStorageServiceEClass, EObjectStorageService.class, "EObjectStorageService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getEObjectStorageService__StoreObject__String_EObject_ObjectMetadata(), null, "storeObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(eObjectStorageServiceEClass_T);
		addEParameter(op, g1, "object", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectMetadata(), "metadata", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEString());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectStorageService__RetrieveObject__String(), null, "retrieveObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(eObjectStorageServiceEClass_T);
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectStorageService__RetrieveMetadata__String(), null, "retrieveMetadata", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectStorageService__DeleteObject__String(), null, "deleteObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectStorageService__ListObjectIds(), null, "listObjectIds", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getList());
		g1.getETypeArguments().add(g2);
		g3 = createEGenericType(ecorePackage.getEString());
		g2.getETypeArguments().add(g3);
		initEOperation(op, g1);

		op = initEOperation(getEObjectStorageService__QueryObjects__ObjectQuery(), null, "queryObjects", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectQuery(), "query", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(theManagementPackage.getList());
		g1.getETypeArguments().add(g2);
		g3 = createEGenericType(theManagementPackage.getObjectMetadata());
		g2.getETypeArguments().add(g3);
		initEOperation(op, g1);

		op = initEOperation(getEObjectStorageService__UpdateMetadata__String_ObjectMetadata(), null, "updateMetadata", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectMetadata(), "metadata", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectStorageService__UpdateStatus__String_ObjectStatus_String(), null, "updateStatus", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectStatus(), "newStatus", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "changeUser", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEBooleanObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectStorageService__Exists__String(), ecorePackage.getEBooleanObject(), "exists", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getEObjectStorageService__GetObjectCount(), ecorePackage.getELong(), "getObjectCount", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getEObjectStorageService__GetBackendType(), theManagementPackage.getStorageBackendType(), "getBackendType", 1, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(eObjectRegistryServiceEClass, EObjectRegistryService.class, "EObjectRegistryService", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getEObjectRegistryService__GetMetadata__String(), null, "getMetadata", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getOptional());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__FindByObjectName__String(), null, "findByObjectName", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectName", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__FindByObjectNameAndRole__String_String(), null, "findByObjectNameAndRole", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectName", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "role", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getOptional());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__FindByRole__String(), null, "findByRole", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "role", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__FindByStatus__ObjectStatus(), null, "findByStatus", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectStatus(), "status", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__FindPendingApproval(), null, "findPendingApproval", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__FindByVersion__String(), null, "findByVersion", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "version", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__FindByVersionPattern__String(), null, "findByVersionPattern", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "versionPattern", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__FindByFingerprint__String(), null, "findByFingerprint", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "fingerprint", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getOptional());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__FindByObjectType__String(), null, "findByObjectType", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectType", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__FindByStatusAndType__ObjectStatus_String(), null, "findByStatusAndType", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectStatus(), "status", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectType", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__FindRecentlyModified__Instant_int(), null, "findRecentlyModified", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getInstant(), "sinceTime", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEInt(), "maxResults", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getList());
		g2 = createEGenericType(theManagementPackage.getObjectMetadata());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getEObjectRegistryService__UpdateCache__ObjectMetadata(), null, "updateCache", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectMetadata(), "metadata", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectRegistryService__RemoveFromCache__String(), null, "removeFromCache", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getEObjectRegistryService__GetRegistryStatistics(), null, "getRegistryStatistics", 0, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(theManagementPackage.getPromise());
		g2 = createEGenericType(ecorePackage.getEMap());
		g1.getETypeArguments().add(g2);
		g3 = createEGenericType(ecorePackage.getEString());
		g2.getETypeArguments().add(g3);
		g3 = createEGenericType(ecorePackage.getEJavaObject());
		g2.getETypeArguments().add(g3);
		initEOperation(op, g1);

		initEClass(storageRegistryEClass, StorageRegistry.class, "StorageRegistry", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getStorageRegistry__GetStorageByRole__String(), null, "getStorageByRole", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "role", 1, 1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getEObjectStorageService());
		g2 = createEGenericType(ecorePackage.getEObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		op = initEOperation(getStorageRegistry__GetAllStorages(), null, "getAllStorages", 0, -1, IS_UNIQUE, IS_ORDERED);
		g1 = createEGenericType(this.getEObjectStorageService());
		g2 = createEGenericType(ecorePackage.getEObject());
		g1.getETypeArguments().add(g2);
		initEOperation(op, g1);

		initEOperation(getStorageRegistry__GetAvailableRoles(), ecorePackage.getEString(), "getAvailableRoles", 0, -1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getStorageRegistry__UpdateGovernanceDocumentationId__String_String_String_String(), ecorePackage.getEInt(), "updateGovernanceDocumentationId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "role", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectName", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "newDocumentationId", 1, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "reason", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getStorageRegistry__SearchMetadataAcrossRoles__ObjectQuery(), theManagementPackage.getObjectMetadata(), "searchMetadataAcrossRoles", 0, -1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectQuery(), "query", 1, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getStorageRegistry__GetStorageStatistics(), null, "getStorageStatistics", 0, 1, IS_UNIQUE, IS_ORDERED);
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
