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
package org.eclipse.fennec.model.atlas.mgmt.management.impl;

import java.time.Instant;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest;
import org.eclipse.fennec.model.atlas.mgmt.management.GenerationStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.PackageStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ManagementPackageImpl extends EPackageImpl implements ManagementPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectMetadataEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stringToObjectMapEntryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectQueryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass generationRequestEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectMetadataContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum objectStatusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum packageStatusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum storageBackendTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum generationStatusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType instantEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType promiseEDataType = null;

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
	private EDataType voidEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType optionalEDataType = null;

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
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ManagementPackageImpl() {
		super(eNS_URI, ManagementFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ManagementPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ManagementPackage init() {
		if (isInited) return (ManagementPackage)EPackage.Registry.INSTANCE.getEPackage(ManagementPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredManagementPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ManagementPackageImpl theManagementPackage = registeredManagementPackage instanceof ManagementPackageImpl ? (ManagementPackageImpl)registeredManagementPackage : new ManagementPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theManagementPackage.createPackageContents();

		// Initialize created meta-data
		theManagementPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theManagementPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ManagementPackage.eNS_URI, theManagementPackage);
		return theManagementPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getObjectMetadata() {
		return objectMetadataEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_UploadUser() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_UploadTime() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_SourceChannel() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ContentHash() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ObjectType() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ReviewUser() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ReviewTime() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ReviewReason() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_GenerationTriggerFingerprint() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ComplianceCheckTime() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ComplianceStatus() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_GovernanceDocumentationId() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getObjectMetadata_Properties() {
		return (EReference)objectMetadataEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_LastChangeUser() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_LastChangeTime() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_Status() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_Version() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getObjectMetadata_ObjectRef() {
		return (EReference)objectMetadataEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ObjectId() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ObjectName() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(19);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_Role() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(20);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_LastChangeReason() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(21);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_StorageId() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(22);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getStringToObjectMapEntry() {
		return stringToObjectMapEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStringToObjectMapEntry_Key() {
		return (EAttribute)stringToObjectMapEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStringToObjectMapEntry_Value() {
		return (EAttribute)stringToObjectMapEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getObjectQuery() {
		return objectQueryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_UploadUser() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_SourceChannel() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_ObjectType() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_Status() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGenerationRequest() {
		return generationRequestEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_RequestId() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_JsonSample() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_JsonFingerprint() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_SourceChannel() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_RequestingUser() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_Status() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_RequestTime() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_StartTime() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_CompletionTime() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_ErrorMessage() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_ResultPackageId() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getObjectMetadataContainer() {
		return objectMetadataContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadataContainer_ContainerId() {
		return (EAttribute)objectMetadataContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getObjectMetadataContainer_Metadata() {
		return (EReference)objectMetadataContainerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getObjectStatus() {
		return objectStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getPackageStatus() {
		return packageStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getStorageBackendType() {
		return storageBackendTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getGenerationStatus() {
		return generationStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getInstant() {
		return instantEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getPromise() {
		return promiseEDataType;
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
	public EDataType getVoid() {
		return voidEDataType;
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
	public ManagementFactory getManagementFactory() {
		return (ManagementFactory)getEFactoryInstance();
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
		objectMetadataEClass = createEClass(OBJECT_METADATA);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__UPLOAD_USER);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__UPLOAD_TIME);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__SOURCE_CHANNEL);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__CONTENT_HASH);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__OBJECT_TYPE);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__REVIEW_USER);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__REVIEW_TIME);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__REVIEW_REASON);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__GENERATION_TRIGGER_FINGERPRINT);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__COMPLIANCE_CHECK_TIME);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__COMPLIANCE_STATUS);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__GOVERNANCE_DOCUMENTATION_ID);
		createEReference(objectMetadataEClass, OBJECT_METADATA__PROPERTIES);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__LAST_CHANGE_USER);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__LAST_CHANGE_TIME);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__STATUS);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__VERSION);
		createEReference(objectMetadataEClass, OBJECT_METADATA__OBJECT_REF);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__OBJECT_ID);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__OBJECT_NAME);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__ROLE);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__LAST_CHANGE_REASON);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__STORAGE_ID);

		stringToObjectMapEntryEClass = createEClass(STRING_TO_OBJECT_MAP_ENTRY);
		createEAttribute(stringToObjectMapEntryEClass, STRING_TO_OBJECT_MAP_ENTRY__KEY);
		createEAttribute(stringToObjectMapEntryEClass, STRING_TO_OBJECT_MAP_ENTRY__VALUE);

		objectQueryEClass = createEClass(OBJECT_QUERY);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__UPLOAD_USER);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__SOURCE_CHANNEL);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__OBJECT_TYPE);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__STATUS);

		generationRequestEClass = createEClass(GENERATION_REQUEST);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__REQUEST_ID);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__JSON_SAMPLE);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__JSON_FINGERPRINT);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__SOURCE_CHANNEL);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__REQUESTING_USER);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__STATUS);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__REQUEST_TIME);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__START_TIME);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__COMPLETION_TIME);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__ERROR_MESSAGE);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__RESULT_PACKAGE_ID);

		objectMetadataContainerEClass = createEClass(OBJECT_METADATA_CONTAINER);
		createEAttribute(objectMetadataContainerEClass, OBJECT_METADATA_CONTAINER__CONTAINER_ID);
		createEReference(objectMetadataContainerEClass, OBJECT_METADATA_CONTAINER__METADATA);

		// Create enums
		objectStatusEEnum = createEEnum(OBJECT_STATUS);
		packageStatusEEnum = createEEnum(PACKAGE_STATUS);
		storageBackendTypeEEnum = createEEnum(STORAGE_BACKEND_TYPE);
		generationStatusEEnum = createEEnum(GENERATION_STATUS);

		// Create data types
		instantEDataType = createEDataType(INSTANT);
		promiseEDataType = createEDataType(PROMISE);
		listEDataType = createEDataType(LIST);
		voidEDataType = createEDataType(VOID);
		optionalEDataType = createEDataType(OPTIONAL);
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
		addETypeParameter(promiseEDataType, "T");
		addETypeParameter(listEDataType, "T");
		addETypeParameter(optionalEDataType, "T");

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(objectMetadataEClass, ObjectMetadata.class, "ObjectMetadata", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getObjectMetadata_UploadUser(), ecorePackage.getEString(), "uploadUser", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_UploadTime(), this.getInstant(), "uploadTime", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_SourceChannel(), ecorePackage.getEString(), "sourceChannel", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ContentHash(), ecorePackage.getEString(), "contentHash", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ObjectType(), ecorePackage.getEString(), "objectType", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ReviewUser(), ecorePackage.getEString(), "reviewUser", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ReviewTime(), this.getInstant(), "reviewTime", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ReviewReason(), ecorePackage.getEString(), "reviewReason", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_GenerationTriggerFingerprint(), ecorePackage.getEString(), "generationTriggerFingerprint", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ComplianceCheckTime(), this.getInstant(), "complianceCheckTime", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ComplianceStatus(), ecorePackage.getEString(), "complianceStatus", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_GovernanceDocumentationId(), ecorePackage.getEString(), "governanceDocumentationId", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getObjectMetadata_Properties(), this.getStringToObjectMapEntry(), null, "properties", null, 0, -1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_LastChangeUser(), ecorePackage.getEString(), "lastChangeUser", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_LastChangeTime(), this.getInstant(), "lastChangeTime", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_Status(), this.getObjectStatus(), "status", "DRAFT", 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_Version(), ecorePackage.getEString(), "version", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getObjectMetadata_ObjectRef(), ecorePackage.getEObject(), null, "objectRef", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ObjectId(), ecorePackage.getEString(), "objectId", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ObjectName(), ecorePackage.getEString(), "objectName", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_Role(), ecorePackage.getEString(), "role", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_LastChangeReason(), ecorePackage.getEString(), "lastChangeReason", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_StorageId(), ecorePackage.getEString(), "storageId", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(stringToObjectMapEntryEClass, Map.Entry.class, "StringToObjectMapEntry", !IS_ABSTRACT, !IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStringToObjectMapEntry_Key(), ecorePackage.getEString(), "key", null, 1, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStringToObjectMapEntry_Value(), ecorePackage.getEJavaObject(), "value", null, 1, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(objectQueryEClass, ObjectQuery.class, "ObjectQuery", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getObjectQuery_UploadUser(), ecorePackage.getEString(), "uploadUser", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectQuery_SourceChannel(), ecorePackage.getEString(), "sourceChannel", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectQuery_ObjectType(), ecorePackage.getEString(), "objectType", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectQuery_Status(), this.getObjectStatus(), "status", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(generationRequestEClass, GenerationRequest.class, "GenerationRequest", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGenerationRequest_RequestId(), ecorePackage.getEString(), "requestId", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_JsonSample(), ecorePackage.getEString(), "jsonSample", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_JsonFingerprint(), ecorePackage.getEString(), "jsonFingerprint", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_SourceChannel(), ecorePackage.getEString(), "sourceChannel", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_RequestingUser(), ecorePackage.getEString(), "requestingUser", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_Status(), this.getGenerationStatus(), "status", "REQUESTED", 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_RequestTime(), this.getInstant(), "requestTime", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_StartTime(), this.getInstant(), "startTime", null, 0, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_CompletionTime(), this.getInstant(), "completionTime", null, 0, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_ErrorMessage(), ecorePackage.getEString(), "errorMessage", null, 0, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_ResultPackageId(), ecorePackage.getEString(), "resultPackageId", null, 0, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(objectMetadataContainerEClass, ObjectMetadataContainer.class, "ObjectMetadataContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getObjectMetadataContainer_ContainerId(), ecorePackage.getEString(), "containerId", null, 1, 1, ObjectMetadataContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getObjectMetadataContainer_Metadata(), this.getObjectMetadata(), null, "metadata", null, 0, -1, ObjectMetadataContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getObjectMetadataContainer_Metadata().getEKeys().add(this.getObjectMetadata_ObjectId());

		// Initialize enums and add enum literals
		initEEnum(objectStatusEEnum, ObjectStatus.class, "ObjectStatus");
		addEEnumLiteral(objectStatusEEnum, ObjectStatus.DRAFT);
		addEEnumLiteral(objectStatusEEnum, ObjectStatus.APPROVED);
		addEEnumLiteral(objectStatusEEnum, ObjectStatus.REJECTED);
		addEEnumLiteral(objectStatusEEnum, ObjectStatus.DEPLOYED);
		addEEnumLiteral(objectStatusEEnum, ObjectStatus.ARCHIVED);

		initEEnum(packageStatusEEnum, PackageStatus.class, "PackageStatus");
		addEEnumLiteral(packageStatusEEnum, PackageStatus.DRAFT);
		addEEnumLiteral(packageStatusEEnum, PackageStatus.APPROVED);
		addEEnumLiteral(packageStatusEEnum, PackageStatus.DEPLOYED);
		addEEnumLiteral(packageStatusEEnum, PackageStatus.ARCHIVED);

		initEEnum(storageBackendTypeEEnum, StorageBackendType.class, "StorageBackendType");
		addEEnumLiteral(storageBackendTypeEEnum, StorageBackendType.FILE);
		addEEnumLiteral(storageBackendTypeEEnum, StorageBackendType.MINIO);
		addEEnumLiteral(storageBackendTypeEEnum, StorageBackendType.GIT);
		addEEnumLiteral(storageBackendTypeEEnum, StorageBackendType.APICURIO);

		initEEnum(generationStatusEEnum, GenerationStatus.class, "GenerationStatus");
		addEEnumLiteral(generationStatusEEnum, GenerationStatus.REQUESTED);
		addEEnumLiteral(generationStatusEEnum, GenerationStatus.IN_PROGRESS);
		addEEnumLiteral(generationStatusEEnum, GenerationStatus.COMPLETED);
		addEEnumLiteral(generationStatusEEnum, GenerationStatus.FAILED);
		addEEnumLiteral(generationStatusEEnum, GenerationStatus.CANCELLED);

		// Initialize data types
		initEDataType(instantEDataType, Instant.class, "Instant", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(promiseEDataType, Promise.class, "Promise", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(listEDataType, List.class, "List", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(voidEDataType, Void.class, "Void", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(optionalEDataType, Optional.class, "Optional", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
		// java.time.Instant
		createJavaAnnotations();
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
			   "modelName", "Management",
			   "complianceLevel", "17.0",
			   "copyrightText", "Copyright (c) 2012 - 2025 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n     Mark Hoffmann - initial API and implementation",
			   "resource", "XMI",
			   "oSGiCompatible", "true",
			   "prefix", "Management",
			   "basePackage", "org.eclipse.fennec.model.atlas.mgmt"
		   });
		addAnnotation
		  (objectMetadataEClass,
		   source,
		   new String[] {
			   "documentation", "Generic metadata associated with uploaded EObject"
		   });
		addAnnotation
		  (getObjectMetadata_UploadUser(),
		   source,
		   new String[] {
			   "documentation", "User who uploaded the object"
		   });
		addAnnotation
		  (getObjectMetadata_UploadTime(),
		   source,
		   new String[] {
			   "documentation", "Timestamp when object was uploaded"
		   });
		addAnnotation
		  (getObjectMetadata_SourceChannel(),
		   source,
		   new String[] {
			   "documentation", "Source channel (e.g., AI_GENERATOR, MANUAL_UPLOAD, SENSINACT)"
		   });
		addAnnotation
		  (getObjectMetadata_ContentHash(),
		   source,
		   new String[] {
			   "documentation", "SHA-256 hash of XMI content"
		   });
		addAnnotation
		  (getObjectMetadata_ObjectType(),
		   source,
		   new String[] {
			   "documentation", "Type of the EObject (e.g., EPackage, Route, SensorModel)"
		   });
		addAnnotation
		  (getObjectMetadata_ReviewUser(),
		   source,
		   new String[] {
			   "documentation", "User who reviewed the object (for approve/reject)"
		   });
		addAnnotation
		  (getObjectMetadata_ReviewTime(),
		   source,
		   new String[] {
			   "documentation", "Timestamp when object was reviewed"
		   });
		addAnnotation
		  (getObjectMetadata_ReviewReason(),
		   source,
		   new String[] {
			   "documentation", "Reason for approval or rejection"
		   });
		addAnnotation
		  (getObjectMetadata_GenerationTriggerFingerprint(),
		   source,
		   new String[] {
			   "documentation", "This is the fingerprint of the json which triggered the generation of this Object"
		   });
		addAnnotation
		  (getObjectMetadata_ComplianceCheckTime(),
		   source,
		   new String[] {
			   "documentation", "Timestamp when compliance checks were last performed"
		   });
		addAnnotation
		  (getObjectMetadata_ComplianceStatus(),
		   source,
		   new String[] {
			   "documentation", "Overall compliance check result (uses governance.ComplianceStatus values as strings)"
		   });
		addAnnotation
		  (getObjectMetadata_GovernanceDocumentationId(),
		   source,
		   new String[] {
			   "documentation", "Reference to the governance documentation containing detailed compliance results"
		   });
		addAnnotation
		  (getObjectMetadata_Properties(),
		   source,
		   new String[] {
			   "documentation", "Additional metadata properties"
		   });
		addAnnotation
		  (getObjectMetadata_LastChangeUser(),
		   source,
		   new String[] {
			   "documentation", "User who last modified the object (only set when object is updated after initial upload)"
		   });
		addAnnotation
		  (getObjectMetadata_LastChangeTime(),
		   source,
		   new String[] {
			   "documentation", "Timestamp when object was last modified (only set when object is updated after initial upload)"
		   });
		addAnnotation
		  (getObjectMetadata_Status(),
		   source,
		   new String[] {
			   "documentation", "Object lifecycle status"
		   });
		addAnnotation
		  (getObjectMetadata_Version(),
		   source,
		   new String[] {
			   "documentation", "Object version for optimistic locking"
		   });
		addAnnotation
		  (getObjectMetadata_ObjectRef(),
		   source,
		   new String[] {
			   "documentation", "Reference to the actual managed EObject"
		   });
		addAnnotation
		  (getObjectMetadata_ObjectId(),
		   source,
		   new String[] {
			   "documentation", "Unique metadata identifier (distinct from storage objectId key) for cross-referencing and registry lookups"
		   });
		addAnnotation
		  (getObjectMetadata_ObjectName(),
		   source,
		   new String[] {
			   "documentation", "Human-readable object name"
		   });
		addAnnotation
		  (getObjectMetadata_Role(),
		   source,
		   new String[] {
			   "documentation", "Storage role (draft, approved, documentation) - indicates which storage backend this metadata represents"
		   });
		addAnnotation
		  (getObjectMetadata_LastChangeReason(),
		   source,
		   new String[] {
			   "documentation", "Reason for approval or rejection"
		   });
		addAnnotation
		  (getObjectMetadata_StorageId(),
		   source,
		   new String[] {
			   "documentation", "This attribute is used when retrieving an object or checking for its existence. If this is not set, the objectId will be used. This is needed when the storage assigns a different id to the object when stored."
		   });
		addAnnotation
		  (stringToObjectMapEntryEClass,
		   source,
		   new String[] {
			   "documentation", "String Key - Object value map entry"
		   });
		addAnnotation
		  (getStringToObjectMapEntry_Key(),
		   source,
		   new String[] {
			   "documentation", "Property key"
		   });
		addAnnotation
		  (getStringToObjectMapEntry_Value(),
		   source,
		   new String[] {
			   "documentation", "Property value"
		   });
		addAnnotation
		  (objectQueryEClass,
		   source,
		   new String[] {
			   "documentation", "Generic query criteria for searching objects"
		   });
		addAnnotation
		  (getObjectQuery_UploadUser(),
		   source,
		   new String[] {
			   "documentation", "Filter by upload user"
		   });
		addAnnotation
		  (getObjectQuery_SourceChannel(),
		   source,
		   new String[] {
			   "documentation", "Filter by source channel"
		   });
		addAnnotation
		  (getObjectQuery_ObjectType(),
		   source,
		   new String[] {
			   "documentation", "Filter by object type"
		   });
		addAnnotation
		  (getObjectQuery_Status(),
		   source,
		   new String[] {
			   "documentation", "Filter by object status"
		   });
		addAnnotation
		  (generationRequestEClass,
		   source,
		   new String[] {
			   "documentation", "Request for AI-based EPackage generation"
		   });
		addAnnotation
		  (getGenerationRequest_RequestId(),
		   source,
		   new String[] {
			   "documentation", "Unique request identifier"
		   });
		addAnnotation
		  (getGenerationRequest_JsonSample(),
		   source,
		   new String[] {
			   "documentation", "JSON sample used for generation"
		   });
		addAnnotation
		  (getGenerationRequest_JsonFingerprint(),
		   source,
		   new String[] {
			   "documentation", "Structural fingerprint of JSON sample"
		   });
		addAnnotation
		  (getGenerationRequest_SourceChannel(),
		   source,
		   new String[] {
			   "documentation", "Source channel that triggered generation"
		   });
		addAnnotation
		  (getGenerationRequest_RequestingUser(),
		   source,
		   new String[] {
			   "documentation", "User who requested generation"
		   });
		addAnnotation
		  (getGenerationRequest_Status(),
		   source,
		   new String[] {
			   "documentation", "Current generation status"
		   });
		addAnnotation
		  (getGenerationRequest_RequestTime(),
		   source,
		   new String[] {
			   "documentation", "When generation was requested"
		   });
		addAnnotation
		  (getGenerationRequest_StartTime(),
		   source,
		   new String[] {
			   "documentation", "When generation started"
		   });
		addAnnotation
		  (getGenerationRequest_CompletionTime(),
		   source,
		   new String[] {
			   "documentation", "When generation completed"
		   });
		addAnnotation
		  (getGenerationRequest_ErrorMessage(),
		   source,
		   new String[] {
			   "documentation", "Error message if generation failed"
		   });
		addAnnotation
		  (getGenerationRequest_ResultPackageId(),
		   source,
		   new String[] {
			   "documentation", "Package ID of generated EPackage"
		   });
		addAnnotation
		  (objectStatusEEnum,
		   source,
		   new String[] {
			   "documentation", "Generic object lifecycle status"
		   });
		addAnnotation
		  (objectStatusEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Object is in draft state"
		   });
		addAnnotation
		  (objectStatusEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "Object has been approved for release"
		   });
		addAnnotation
		  (objectStatusEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Object has been rejected during review"
		   });
		addAnnotation
		  (objectStatusEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Object has been deployed to production"
		   });
		addAnnotation
		  (objectStatusEEnum.getELiterals().get(4),
		   source,
		   new String[] {
			   "documentation", "Object has been archived"
		   });
		addAnnotation
		  (packageStatusEEnum,
		   source,
		   new String[] {
			   "documentation", "Package lifecycle status"
		   });
		addAnnotation
		  (packageStatusEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Package is in draft state"
		   });
		addAnnotation
		  (packageStatusEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "Package has been approved"
		   });
		addAnnotation
		  (packageStatusEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Package has been deployed"
		   });
		addAnnotation
		  (packageStatusEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Package has been archived"
		   });
		addAnnotation
		  (storageBackendTypeEEnum,
		   source,
		   new String[] {
			   "documentation", "Storage backend types"
		   });
		addAnnotation
		  (storageBackendTypeEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "File object storage backend"
		   });
		addAnnotation
		  (storageBackendTypeEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "MinIO object storage backend"
		   });
		addAnnotation
		  (storageBackendTypeEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Git-based storage backend"
		   });
		addAnnotation
		  (storageBackendTypeEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Apicurio-based storage backend"
		   });
		addAnnotation
		  (generationStatusEEnum,
		   source,
		   new String[] {
			   "documentation", "Status of EPackage generation request"
		   });
		addAnnotation
		  (generationStatusEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Generation has been requested"
		   });
		addAnnotation
		  (generationStatusEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "AI is actively generating the model"
		   });
		addAnnotation
		  (generationStatusEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Generation completed successfully"
		   });
		addAnnotation
		  (generationStatusEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Generation failed with error"
		   });
		addAnnotation
		  (generationStatusEEnum.getELiterals().get(4),
		   source,
		   new String[] {
			   "documentation", "Generation was cancelled"
		   });
		addAnnotation
		  (instantEDataType,
		   source,
		   new String[] {
			   "documentation", "Java 8 Instant for precise timestamps"
		   });
		addAnnotation
		  (objectMetadataContainerEClass,
		   source,
		   new String[] {
			   "documentation", "Container for managing collections of ObjectMetadata with efficient keyed access. Note: With Lucene indexing, this container may not be necessary as the index serves as the registry."
		   });
		addAnnotation
		  (getObjectMetadataContainer_ContainerId(),
		   source,
		   new String[] {
			   "documentation", "Unique container identifier"
		   });
		addAnnotation
		  (getObjectMetadataContainer_Metadata(),
		   source,
		   new String[] {
			   "documentation", "Collection of metadata objects with automatic keying by objectId for efficient lookups"
		   });
	}

	/**
	 * Initializes the annotations for <b>java.time.Instant</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createJavaAnnotations() {
		String source = "java.time.Instant";
		addAnnotation
		  (instantEDataType,
		   source,
		   new String[] {
		   });
	}

} //ManagementPackageImpl
