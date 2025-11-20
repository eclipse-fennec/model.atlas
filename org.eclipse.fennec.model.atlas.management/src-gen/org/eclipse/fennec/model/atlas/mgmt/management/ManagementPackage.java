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
package org.eclipse.fennec.model.atlas.mgmt.management;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;

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
 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel modelName='Management' complianceLevel='17.0' copyrightText='Copyright (c) 2012 - 2025 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n     Mark Hoffmann - initial API and implementation' resource='XMI' oSGiCompatible='true' prefix='Management' basePackage='org.eclipse.fennec.model.atlas.mgmt'"
 * @generated
 */
@ProviderType
@EPackage(uri = ManagementPackage.eNS_URI, genModel = "/model/management.genmodel", genModelSourceLocations = {"model/management.genmodel","org.eclipse.fennec.model.atlas.management/model/management.genmodel"}, ecore="/model/management.ecore", ecoreSourceLocations="/model/management.ecore")
public interface ManagementPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "management";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/management/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "mgmt";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ManagementPackage eINSTANCE = org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectMetadataImpl <em>Object Metadata</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectMetadataImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getObjectMetadata()
	 * @generated
	 */
	int OBJECT_METADATA = 0;

	/**
	 * The feature id for the '<em><b>Upload User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__UPLOAD_USER = 0;

	/**
	 * The feature id for the '<em><b>Upload Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__UPLOAD_TIME = 1;

	/**
	 * The feature id for the '<em><b>Source Channel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__SOURCE_CHANNEL = 2;

	/**
	 * The feature id for the '<em><b>Content Hash</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__CONTENT_HASH = 3;

	/**
	 * The feature id for the '<em><b>Object Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__OBJECT_TYPE = 4;

	/**
	 * The feature id for the '<em><b>Review User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__REVIEW_USER = 5;

	/**
	 * The feature id for the '<em><b>Review Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__REVIEW_TIME = 6;

	/**
	 * The feature id for the '<em><b>Review Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__REVIEW_REASON = 7;

	/**
	 * The feature id for the '<em><b>Generation Trigger Fingerprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__GENERATION_TRIGGER_FINGERPRINT = 8;

	/**
	 * The feature id for the '<em><b>Compliance Check Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__COMPLIANCE_CHECK_TIME = 9;

	/**
	 * The feature id for the '<em><b>Compliance Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__COMPLIANCE_STATUS = 10;

	/**
	 * The feature id for the '<em><b>Governance Documentation Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__GOVERNANCE_DOCUMENTATION_ID = 11;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__PROPERTIES = 12;

	/**
	 * The feature id for the '<em><b>Last Change User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__LAST_CHANGE_USER = 13;

	/**
	 * The feature id for the '<em><b>Last Change Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__LAST_CHANGE_TIME = 14;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__STATUS = 15;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__VERSION = 16;

	/**
	 * The feature id for the '<em><b>Object Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__OBJECT_REF = 17;

	/**
	 * The feature id for the '<em><b>Object Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__OBJECT_ID = 18;

	/**
	 * The feature id for the '<em><b>Object Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__OBJECT_NAME = 19;

	/**
	 * The feature id for the '<em><b>Role</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__ROLE = 20;

	/**
	 * The feature id for the '<em><b>Last Change Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__LAST_CHANGE_REASON = 21;

	/**
	 * The feature id for the '<em><b>Storage Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA__STORAGE_ID = 22;

	/**
	 * The number of structural features of the '<em>Object Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA_FEATURE_COUNT = 23;

	/**
	 * The number of operations of the '<em>Object Metadata</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.StringToObjectMapEntryImpl <em>String To Object Map Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.StringToObjectMapEntryImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getStringToObjectMapEntry()
	 * @generated
	 */
	int STRING_TO_OBJECT_MAP_ENTRY = 1;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_OBJECT_MAP_ENTRY__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_OBJECT_MAP_ENTRY__VALUE = 1;

	/**
	 * The number of structural features of the '<em>String To Object Map Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_OBJECT_MAP_ENTRY_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>String To Object Map Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STRING_TO_OBJECT_MAP_ENTRY_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectQueryImpl <em>Object Query</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectQueryImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getObjectQuery()
	 * @generated
	 */
	int OBJECT_QUERY = 2;

	/**
	 * The feature id for the '<em><b>Upload User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_QUERY__UPLOAD_USER = 0;

	/**
	 * The feature id for the '<em><b>Source Channel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_QUERY__SOURCE_CHANNEL = 1;

	/**
	 * The feature id for the '<em><b>Object Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_QUERY__OBJECT_TYPE = 2;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_QUERY__STATUS = 3;

	/**
	 * The number of structural features of the '<em>Object Query</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_QUERY_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Object Query</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_QUERY_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.GenerationRequestImpl <em>Generation Request</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.GenerationRequestImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getGenerationRequest()
	 * @generated
	 */
	int GENERATION_REQUEST = 3;

	/**
	 * The feature id for the '<em><b>Request Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST__REQUEST_ID = 0;

	/**
	 * The feature id for the '<em><b>Json Sample</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST__JSON_SAMPLE = 1;

	/**
	 * The feature id for the '<em><b>Json Fingerprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST__JSON_FINGERPRINT = 2;

	/**
	 * The feature id for the '<em><b>Source Channel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST__SOURCE_CHANNEL = 3;

	/**
	 * The feature id for the '<em><b>Requesting User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST__REQUESTING_USER = 4;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST__STATUS = 5;

	/**
	 * The feature id for the '<em><b>Request Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST__REQUEST_TIME = 6;

	/**
	 * The feature id for the '<em><b>Start Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST__START_TIME = 7;

	/**
	 * The feature id for the '<em><b>Completion Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST__COMPLETION_TIME = 8;

	/**
	 * The feature id for the '<em><b>Error Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST__ERROR_MESSAGE = 9;

	/**
	 * The feature id for the '<em><b>Result Package Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST__RESULT_PACKAGE_ID = 10;

	/**
	 * The number of structural features of the '<em>Generation Request</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST_FEATURE_COUNT = 11;

	/**
	 * The number of operations of the '<em>Generation Request</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GENERATION_REQUEST_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectMetadataContainerImpl <em>Object Metadata Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectMetadataContainerImpl
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getObjectMetadataContainer()
	 * @generated
	 */
	int OBJECT_METADATA_CONTAINER = 4;

	/**
	 * The feature id for the '<em><b>Container Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA_CONTAINER__CONTAINER_ID = 0;

	/**
	 * The feature id for the '<em><b>Metadata</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA_CONTAINER__METADATA = 1;

	/**
	 * The number of structural features of the '<em>Object Metadata Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA_CONTAINER_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Object Metadata Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_METADATA_CONTAINER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus <em>Object Status</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getObjectStatus()
	 * @generated
	 */
	int OBJECT_STATUS = 5;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.PackageStatus <em>Package Status</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.PackageStatus
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getPackageStatus()
	 * @generated
	 */
	int PACKAGE_STATUS = 6;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType <em>Storage Backend Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getStorageBackendType()
	 * @generated
	 */
	int STORAGE_BACKEND_TYPE = 7;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationStatus <em>Generation Status</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationStatus
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getGenerationStatus()
	 * @generated
	 */
	int GENERATION_STATUS = 8;

	/**
	 * The meta object id for the '<em>Instant</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.time.Instant
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getInstant()
	 * @generated
	 */
	int INSTANT = 9;

	/**
	 * The meta object id for the '<em>Promise</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.osgi.util.promise.Promise
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getPromise()
	 * @generated
	 */
	int PROMISE = 10;

	/**
	 * The meta object id for the '<em>List</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.List
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getList()
	 * @generated
	 */
	int LIST = 11;

	/**
	 * The meta object id for the '<em>Void</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.lang.Void
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getVoid()
	 * @generated
	 */
	int VOID = 12;

	/**
	 * The meta object id for the '<em>Optional</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.Optional
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getOptional()
	 * @generated
	 */
	int OPTIONAL = 13;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata <em>Object Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Object Metadata</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata
	 * @generated
	 */
	EClass getObjectMetadata();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getUploadUser <em>Upload User</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Upload User</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getUploadUser()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_UploadUser();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getUploadTime <em>Upload Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Upload Time</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getUploadTime()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_UploadTime();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getSourceChannel <em>Source Channel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Source Channel</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getSourceChannel()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_SourceChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getContentHash <em>Content Hash</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Content Hash</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getContentHash()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_ContentHash();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectType <em>Object Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Object Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectType()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_ObjectType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewUser <em>Review User</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Review User</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewUser()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_ReviewUser();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewTime <em>Review Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Review Time</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewTime()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_ReviewTime();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewReason <em>Review Reason</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Review Reason</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewReason()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_ReviewReason();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getGenerationTriggerFingerprint <em>Generation Trigger Fingerprint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Generation Trigger Fingerprint</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getGenerationTriggerFingerprint()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_GenerationTriggerFingerprint();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getComplianceCheckTime <em>Compliance Check Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Compliance Check Time</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getComplianceCheckTime()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_ComplianceCheckTime();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getComplianceStatus <em>Compliance Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Compliance Status</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getComplianceStatus()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_ComplianceStatus();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getGovernanceDocumentationId <em>Governance Documentation Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Governance Documentation Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getGovernanceDocumentationId()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_GovernanceDocumentationId();

	/**
	 * Returns the meta object for the map '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getProperties <em>Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Properties</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getProperties()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EReference getObjectMetadata_Properties();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeUser <em>Last Change User</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last Change User</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeUser()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_LastChangeUser();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeTime <em>Last Change Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last Change Time</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeTime()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_LastChangeTime();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getStatus()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_Status();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getVersion()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_Version();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectRef <em>Object Ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Object Ref</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectRef()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EReference getObjectMetadata_ObjectRef();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectId <em>Object Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Object Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectId()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_ObjectId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectName <em>Object Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Object Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectName()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_ObjectName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getRole <em>Role</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Role</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getRole()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_Role();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeReason <em>Last Change Reason</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Last Change Reason</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeReason()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_LastChangeReason();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getStorageId <em>Storage Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Storage Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getStorageId()
	 * @see #getObjectMetadata()
	 * @generated
	 */
	EAttribute getObjectMetadata_StorageId();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>String To Object Map Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>String To Object Map Entry</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.EString" keyRequired="true"
	 *        valueDataType="org.eclipse.emf.ecore.EJavaObject" valueRequired="true"
	 * @generated
	 */
	EClass getStringToObjectMapEntry();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToObjectMapEntry()
	 * @generated
	 */
	EAttribute getStringToObjectMapEntry_Key();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getStringToObjectMapEntry()
	 * @generated
	 */
	EAttribute getStringToObjectMapEntry_Value();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery <em>Object Query</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Object Query</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery
	 * @generated
	 */
	EClass getObjectQuery();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getUploadUser <em>Upload User</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Upload User</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getUploadUser()
	 * @see #getObjectQuery()
	 * @generated
	 */
	EAttribute getObjectQuery_UploadUser();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getSourceChannel <em>Source Channel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Source Channel</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getSourceChannel()
	 * @see #getObjectQuery()
	 * @generated
	 */
	EAttribute getObjectQuery_SourceChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getObjectType <em>Object Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Object Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getObjectType()
	 * @see #getObjectQuery()
	 * @generated
	 */
	EAttribute getObjectQuery_ObjectType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getStatus()
	 * @see #getObjectQuery()
	 * @generated
	 */
	EAttribute getObjectQuery_Status();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest <em>Generation Request</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Generation Request</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest
	 * @generated
	 */
	EClass getGenerationRequest();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getRequestId <em>Request Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Request Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getRequestId()
	 * @see #getGenerationRequest()
	 * @generated
	 */
	EAttribute getGenerationRequest_RequestId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getJsonSample <em>Json Sample</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Json Sample</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getJsonSample()
	 * @see #getGenerationRequest()
	 * @generated
	 */
	EAttribute getGenerationRequest_JsonSample();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getJsonFingerprint <em>Json Fingerprint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Json Fingerprint</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getJsonFingerprint()
	 * @see #getGenerationRequest()
	 * @generated
	 */
	EAttribute getGenerationRequest_JsonFingerprint();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getSourceChannel <em>Source Channel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Source Channel</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getSourceChannel()
	 * @see #getGenerationRequest()
	 * @generated
	 */
	EAttribute getGenerationRequest_SourceChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getRequestingUser <em>Requesting User</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Requesting User</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getRequestingUser()
	 * @see #getGenerationRequest()
	 * @generated
	 */
	EAttribute getGenerationRequest_RequestingUser();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getStatus <em>Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Status</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getStatus()
	 * @see #getGenerationRequest()
	 * @generated
	 */
	EAttribute getGenerationRequest_Status();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getRequestTime <em>Request Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Request Time</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getRequestTime()
	 * @see #getGenerationRequest()
	 * @generated
	 */
	EAttribute getGenerationRequest_RequestTime();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getStartTime <em>Start Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Start Time</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getStartTime()
	 * @see #getGenerationRequest()
	 * @generated
	 */
	EAttribute getGenerationRequest_StartTime();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getCompletionTime <em>Completion Time</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Completion Time</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getCompletionTime()
	 * @see #getGenerationRequest()
	 * @generated
	 */
	EAttribute getGenerationRequest_CompletionTime();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getErrorMessage <em>Error Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Error Message</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getErrorMessage()
	 * @see #getGenerationRequest()
	 * @generated
	 */
	EAttribute getGenerationRequest_ErrorMessage();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getResultPackageId <em>Result Package Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Result Package Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest#getResultPackageId()
	 * @see #getGenerationRequest()
	 * @generated
	 */
	EAttribute getGenerationRequest_ResultPackageId();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer <em>Object Metadata Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Object Metadata Container</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer
	 * @generated
	 */
	EClass getObjectMetadataContainer();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer#getContainerId <em>Container Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Container Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer#getContainerId()
	 * @see #getObjectMetadataContainer()
	 * @generated
	 */
	EAttribute getObjectMetadataContainer_ContainerId();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer#getMetadata <em>Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Metadata</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer#getMetadata()
	 * @see #getObjectMetadataContainer()
	 * @generated
	 */
	EReference getObjectMetadataContainer_Metadata();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus <em>Object Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Object Status</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus
	 * @generated
	 */
	EEnum getObjectStatus();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.mgmt.management.PackageStatus <em>Package Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Package Status</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.PackageStatus
	 * @generated
	 */
	EEnum getPackageStatus();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType <em>Storage Backend Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Storage Backend Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType
	 * @generated
	 */
	EEnum getStorageBackendType();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationStatus <em>Generation Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Generation Status</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationStatus
	 * @generated
	 */
	EEnum getGenerationStatus();

	/**
	 * Returns the meta object for data type '{@link java.time.Instant <em>Instant</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Java 8 Instant for precise timestamps
     * <!-- end-model-doc -->
	 * @return the meta object for data type '<em>Instant</em>'.
	 * @see java.time.Instant
	 * @model instanceClass="java.time.Instant"
	 *        annotation="java.time.Instant"
	 * @generated
	 */
	EDataType getInstant();

	/**
	 * Returns the meta object for data type '{@link org.osgi.util.promise.Promise <em>Promise</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Promise</em>'.
	 * @see org.osgi.util.promise.Promise
	 * @model instanceClass="org.osgi.util.promise.Promise" serializeable="false" typeParameters="T"
	 * @generated
	 */
	EDataType getPromise();

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
	 * Returns the meta object for data type '{@link java.lang.Void <em>Void</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Void</em>'.
	 * @see java.lang.Void
	 * @model instanceClass="java.lang.Void" serializeable="false"
	 * @generated
	 */
	EDataType getVoid();

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
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ManagementFactory getManagementFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectMetadataImpl <em>Object Metadata</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectMetadataImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getObjectMetadata()
		 * @generated
		 */
		EClass OBJECT_METADATA = eINSTANCE.getObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>Upload User</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__UPLOAD_USER = eINSTANCE.getObjectMetadata_UploadUser();

		/**
		 * The meta object literal for the '<em><b>Upload Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__UPLOAD_TIME = eINSTANCE.getObjectMetadata_UploadTime();

		/**
		 * The meta object literal for the '<em><b>Source Channel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__SOURCE_CHANNEL = eINSTANCE.getObjectMetadata_SourceChannel();

		/**
		 * The meta object literal for the '<em><b>Content Hash</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__CONTENT_HASH = eINSTANCE.getObjectMetadata_ContentHash();

		/**
		 * The meta object literal for the '<em><b>Object Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__OBJECT_TYPE = eINSTANCE.getObjectMetadata_ObjectType();

		/**
		 * The meta object literal for the '<em><b>Review User</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__REVIEW_USER = eINSTANCE.getObjectMetadata_ReviewUser();

		/**
		 * The meta object literal for the '<em><b>Review Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__REVIEW_TIME = eINSTANCE.getObjectMetadata_ReviewTime();

		/**
		 * The meta object literal for the '<em><b>Review Reason</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__REVIEW_REASON = eINSTANCE.getObjectMetadata_ReviewReason();

		/**
		 * The meta object literal for the '<em><b>Generation Trigger Fingerprint</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__GENERATION_TRIGGER_FINGERPRINT = eINSTANCE.getObjectMetadata_GenerationTriggerFingerprint();

		/**
		 * The meta object literal for the '<em><b>Compliance Check Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__COMPLIANCE_CHECK_TIME = eINSTANCE.getObjectMetadata_ComplianceCheckTime();

		/**
		 * The meta object literal for the '<em><b>Compliance Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__COMPLIANCE_STATUS = eINSTANCE.getObjectMetadata_ComplianceStatus();

		/**
		 * The meta object literal for the '<em><b>Governance Documentation Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__GOVERNANCE_DOCUMENTATION_ID = eINSTANCE.getObjectMetadata_GovernanceDocumentationId();

		/**
		 * The meta object literal for the '<em><b>Properties</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OBJECT_METADATA__PROPERTIES = eINSTANCE.getObjectMetadata_Properties();

		/**
		 * The meta object literal for the '<em><b>Last Change User</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__LAST_CHANGE_USER = eINSTANCE.getObjectMetadata_LastChangeUser();

		/**
		 * The meta object literal for the '<em><b>Last Change Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__LAST_CHANGE_TIME = eINSTANCE.getObjectMetadata_LastChangeTime();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__STATUS = eINSTANCE.getObjectMetadata_Status();

		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__VERSION = eINSTANCE.getObjectMetadata_Version();

		/**
		 * The meta object literal for the '<em><b>Object Ref</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OBJECT_METADATA__OBJECT_REF = eINSTANCE.getObjectMetadata_ObjectRef();

		/**
		 * The meta object literal for the '<em><b>Object Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__OBJECT_ID = eINSTANCE.getObjectMetadata_ObjectId();

		/**
		 * The meta object literal for the '<em><b>Object Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__OBJECT_NAME = eINSTANCE.getObjectMetadata_ObjectName();

		/**
		 * The meta object literal for the '<em><b>Role</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__ROLE = eINSTANCE.getObjectMetadata_Role();

		/**
		 * The meta object literal for the '<em><b>Last Change Reason</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__LAST_CHANGE_REASON = eINSTANCE.getObjectMetadata_LastChangeReason();

		/**
		 * The meta object literal for the '<em><b>Storage Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA__STORAGE_ID = eINSTANCE.getObjectMetadata_StorageId();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.StringToObjectMapEntryImpl <em>String To Object Map Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.StringToObjectMapEntryImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getStringToObjectMapEntry()
		 * @generated
		 */
		EClass STRING_TO_OBJECT_MAP_ENTRY = eINSTANCE.getStringToObjectMapEntry();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING_TO_OBJECT_MAP_ENTRY__KEY = eINSTANCE.getStringToObjectMapEntry_Key();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STRING_TO_OBJECT_MAP_ENTRY__VALUE = eINSTANCE.getStringToObjectMapEntry_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectQueryImpl <em>Object Query</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectQueryImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getObjectQuery()
		 * @generated
		 */
		EClass OBJECT_QUERY = eINSTANCE.getObjectQuery();

		/**
		 * The meta object literal for the '<em><b>Upload User</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_QUERY__UPLOAD_USER = eINSTANCE.getObjectQuery_UploadUser();

		/**
		 * The meta object literal for the '<em><b>Source Channel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_QUERY__SOURCE_CHANNEL = eINSTANCE.getObjectQuery_SourceChannel();

		/**
		 * The meta object literal for the '<em><b>Object Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_QUERY__OBJECT_TYPE = eINSTANCE.getObjectQuery_ObjectType();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_QUERY__STATUS = eINSTANCE.getObjectQuery_Status();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.GenerationRequestImpl <em>Generation Request</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.GenerationRequestImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getGenerationRequest()
		 * @generated
		 */
		EClass GENERATION_REQUEST = eINSTANCE.getGenerationRequest();

		/**
		 * The meta object literal for the '<em><b>Request Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERATION_REQUEST__REQUEST_ID = eINSTANCE.getGenerationRequest_RequestId();

		/**
		 * The meta object literal for the '<em><b>Json Sample</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERATION_REQUEST__JSON_SAMPLE = eINSTANCE.getGenerationRequest_JsonSample();

		/**
		 * The meta object literal for the '<em><b>Json Fingerprint</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERATION_REQUEST__JSON_FINGERPRINT = eINSTANCE.getGenerationRequest_JsonFingerprint();

		/**
		 * The meta object literal for the '<em><b>Source Channel</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERATION_REQUEST__SOURCE_CHANNEL = eINSTANCE.getGenerationRequest_SourceChannel();

		/**
		 * The meta object literal for the '<em><b>Requesting User</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERATION_REQUEST__REQUESTING_USER = eINSTANCE.getGenerationRequest_RequestingUser();

		/**
		 * The meta object literal for the '<em><b>Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERATION_REQUEST__STATUS = eINSTANCE.getGenerationRequest_Status();

		/**
		 * The meta object literal for the '<em><b>Request Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERATION_REQUEST__REQUEST_TIME = eINSTANCE.getGenerationRequest_RequestTime();

		/**
		 * The meta object literal for the '<em><b>Start Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERATION_REQUEST__START_TIME = eINSTANCE.getGenerationRequest_StartTime();

		/**
		 * The meta object literal for the '<em><b>Completion Time</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERATION_REQUEST__COMPLETION_TIME = eINSTANCE.getGenerationRequest_CompletionTime();

		/**
		 * The meta object literal for the '<em><b>Error Message</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERATION_REQUEST__ERROR_MESSAGE = eINSTANCE.getGenerationRequest_ErrorMessage();

		/**
		 * The meta object literal for the '<em><b>Result Package Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GENERATION_REQUEST__RESULT_PACKAGE_ID = eINSTANCE.getGenerationRequest_ResultPackageId();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectMetadataContainerImpl <em>Object Metadata Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectMetadataContainerImpl
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getObjectMetadataContainer()
		 * @generated
		 */
		EClass OBJECT_METADATA_CONTAINER = eINSTANCE.getObjectMetadataContainer();

		/**
		 * The meta object literal for the '<em><b>Container Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_METADATA_CONTAINER__CONTAINER_ID = eINSTANCE.getObjectMetadataContainer_ContainerId();

		/**
		 * The meta object literal for the '<em><b>Metadata</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OBJECT_METADATA_CONTAINER__METADATA = eINSTANCE.getObjectMetadataContainer_Metadata();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus <em>Object Status</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getObjectStatus()
		 * @generated
		 */
		EEnum OBJECT_STATUS = eINSTANCE.getObjectStatus();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.PackageStatus <em>Package Status</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.PackageStatus
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getPackageStatus()
		 * @generated
		 */
		EEnum PACKAGE_STATUS = eINSTANCE.getPackageStatus();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType <em>Storage Backend Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getStorageBackendType()
		 * @generated
		 */
		EEnum STORAGE_BACKEND_TYPE = eINSTANCE.getStorageBackendType();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.management.GenerationStatus <em>Generation Status</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.GenerationStatus
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getGenerationStatus()
		 * @generated
		 */
		EEnum GENERATION_STATUS = eINSTANCE.getGenerationStatus();

		/**
		 * The meta object literal for the '<em>Instant</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.time.Instant
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getInstant()
		 * @generated
		 */
		EDataType INSTANT = eINSTANCE.getInstant();

		/**
		 * The meta object literal for the '<em>Promise</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.osgi.util.promise.Promise
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getPromise()
		 * @generated
		 */
		EDataType PROMISE = eINSTANCE.getPromise();

		/**
		 * The meta object literal for the '<em>List</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.List
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getList()
		 * @generated
		 */
		EDataType LIST = eINSTANCE.getList();

		/**
		 * The meta object literal for the '<em>Void</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.lang.Void
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getVoid()
		 * @generated
		 */
		EDataType VOID = eINSTANCE.getVoid();

		/**
		 * The meta object literal for the '<em>Optional</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.Optional
		 * @see org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementPackageImpl#getOptional()
		 * @generated
		 */
		EDataType OPTIONAL = eINSTANCE.getOptional();

	}

} //ManagementPackage
