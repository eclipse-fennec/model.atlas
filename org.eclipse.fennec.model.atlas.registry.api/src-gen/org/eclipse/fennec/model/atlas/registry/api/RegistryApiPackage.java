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
package org.eclipse.fennec.model.atlas.registry.api;


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
 * <!-- begin-model-doc -->
 * Client access to registry content (issue #386): the RegistryWriter/RegistryReader services and their request and result types.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = RegistryApiPackage.eNS_URI, fingerprint = "fp1:0d484ef614f1692024bea98806251abf687d73affbed79d84026b2f1528fb729", genModel = "/model/registry-api.genmodel", genModelSourceLocations = {"model/registry-api.genmodel","org.eclipse.fennec.model.atlas.registry.api/model/registry-api.genmodel"}, ecore = "/model/registry-api.ecore", ecoreSourceLocations = "/model/registry-api.ecore")
public interface RegistryApiPackage extends org.eclipse.emf.ecore.EPackage {
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
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/registry/api/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "registryapi";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	RegistryApiPackage eINSTANCE = org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter <em>Registry Writer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getRegistryWriter()
	 * @generated
	 */
	int REGISTRY_WRITER = 0;

	/**
	 * The number of structural features of the '<em>Registry Writer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Create Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER___CREATE_OBJECT__OBJECTWRITE = 0;

	/**
	 * The operation id for the '<em>Update Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER___UPDATE_OBJECT__OBJECTWRITE = 1;

	/**
	 * The operation id for the '<em>Delete Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER___DELETE_OBJECT__OBJECTADDRESS_BOOLEAN_PRECONDITION = 2;

	/**
	 * The operation id for the '<em>Transition Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER___TRANSITION_OBJECT__OBJECTADDRESS_STRING_BOOLEAN_PRECONDITION = 3;

	/**
	 * The operation id for the '<em>Create Package</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER___CREATE_PACKAGE__PACKAGEWRITE = 4;

	/**
	 * The operation id for the '<em>Update Package</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER___UPDATE_PACKAGE__PACKAGEWRITE = 5;

	/**
	 * The operation id for the '<em>Update Package Properties</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER___UPDATE_PACKAGE_PROPERTIES__PACKAGEADDRESS_MAP_PRECONDITION = 6;

	/**
	 * The operation id for the '<em>Delete Package</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER___DELETE_PACKAGE__PACKAGEADDRESS_BOOLEAN_PRECONDITION = 7;

	/**
	 * The operation id for the '<em>Transition Package</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER___TRANSITION_PACKAGE__STRING_STRING_STRING_STRING_BOOLEAN_PRECONDITION = 8;

	/**
	 * The operation id for the '<em>Write Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER___WRITE_OBJECTS__LIST = 9;

	/**
	 * The operation id for the '<em>Write Packages</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER___WRITE_PACKAGES__LIST = 10;

	/**
	 * The number of operations of the '<em>Registry Writer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_WRITER_OPERATION_COUNT = 11;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader <em>Registry Reader</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getRegistryReader()
	 * @generated
	 */
	int REGISTRY_READER = 1;

	/**
	 * The number of structural features of the '<em>Registry Reader</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Schema Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___SCHEMA_REGISTRY__STRING = 0;

	/**
	 * The operation id for the '<em>List All</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___LIST_ALL__STRING_STRING = 1;

	/**
	 * The operation id for the '<em>List Final</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___LIST_FINAL__STRING_STRING = 2;

	/**
	 * The operation id for the '<em>List</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___LIST__STRING_STRING_STRING = 3;

	/**
	 * The operation id for the '<em>List By Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___LIST_BY_NAME__STRING_STRING_STRING_STRING = 4;

	/**
	 * The operation id for the '<em>Metadata</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___METADATA__OBJECTADDRESS = 5;

	/**
	 * The operation id for the '<em>Final Metadata</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___FINAL_METADATA__STRING_STRING_STRING = 6;

	/**
	 * The operation id for the '<em>Content</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___CONTENT__OBJECTADDRESS = 7;

	/**
	 * The operation id for the '<em>Final Content</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___FINAL_CONTENT__STRING_STRING_STRING = 8;

	/**
	 * The operation id for the '<em>Package Metadata</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___PACKAGE_METADATA__PACKAGEADDRESS = 9;

	/**
	 * The operation id for the '<em>Final Package Metadata</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___FINAL_PACKAGE_METADATA__STRING_STRING = 10;

	/**
	 * The operation id for the '<em>Search Packages</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER___SEARCH_PACKAGES__STRING_PACKAGESEARCH = 11;

	/**
	 * The number of operations of the '<em>Registry Reader</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_READER_OPERATION_COUNT = 12;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.ObjectAddressImpl <em>Object Address</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.ObjectAddressImpl
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getObjectAddress()
	 * @generated
	 */
	int OBJECT_ADDRESS = 2;

	/**
	 * The feature id for the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_ADDRESS__SCOPE = 0;

	/**
	 * The feature id for the '<em><b>Registry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_ADDRESS__REGISTRY = 1;

	/**
	 * The feature id for the '<em><b>Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_ADDRESS__STAGE = 2;

	/**
	 * The feature id for the '<em><b>Object Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_ADDRESS__OBJECT_ID = 3;

	/**
	 * The number of structural features of the '<em>Object Address</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_ADDRESS_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Object Address</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_ADDRESS_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageAddressImpl <em>Package Address</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.PackageAddressImpl
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getPackageAddress()
	 * @generated
	 */
	int PACKAGE_ADDRESS = 3;

	/**
	 * The feature id for the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ADDRESS__SCOPE = 0;

	/**
	 * The feature id for the '<em><b>Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ADDRESS__STAGE = 1;

	/**
	 * The feature id for the '<em><b>Ns Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ADDRESS__NS_URI = 2;

	/**
	 * The number of structural features of the '<em>Package Address</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ADDRESS_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Package Address</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ADDRESS_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.ObjectWriteImpl <em>Object Write</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.ObjectWriteImpl
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getObjectWrite()
	 * @generated
	 */
	int OBJECT_WRITE = 4;

	/**
	 * The feature id for the '<em><b>Address</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_WRITE__ADDRESS = 0;

	/**
	 * The feature id for the '<em><b>Content</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_WRITE__CONTENT = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_WRITE__NAME = 2;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_WRITE__VERSION = 3;

	/**
	 * The feature id for the '<em><b>On Conflict</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_WRITE__ON_CONFLICT = 4;

	/**
	 * The feature id for the '<em><b>Precondition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_WRITE__PRECONDITION = 5;

	/**
	 * The number of structural features of the '<em>Object Write</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_WRITE_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Object Write</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OBJECT_WRITE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl <em>Package Write</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getPackageWrite()
	 * @generated
	 */
	int PACKAGE_WRITE = 5;

	/**
	 * The feature id for the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE__SCOPE = 0;

	/**
	 * The feature id for the '<em><b>Registry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE__REGISTRY = 1;

	/**
	 * The feature id for the '<em><b>Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE__STAGE = 2;

	/**
	 * The feature id for the '<em><b>Content</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE__CONTENT = 3;

	/**
	 * The feature id for the '<em><b>Ns Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE__NS_URI = 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE__NAME = 5;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE__VERSION = 6;

	/**
	 * The feature id for the '<em><b>Dcat</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE__DCAT = 7;

	/**
	 * The feature id for the '<em><b>On Conflict</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE__ON_CONFLICT = 8;

	/**
	 * The feature id for the '<em><b>Precondition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE__PRECONDITION = 9;

	/**
	 * The feature id for the '<em><b>Lenient Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE__LENIENT_VERSION = 10;

	/**
	 * The number of structural features of the '<em>Package Write</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE_FEATURE_COUNT = 11;

	/**
	 * The number of operations of the '<em>Package Write</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_WRITE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl <em>Package Search</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getPackageSearch()
	 * @generated
	 */
	int PACKAGE_SEARCH = 6;

	/**
	 * The feature id for the '<em><b>Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH__STAGE = 0;

	/**
	 * The feature id for the '<em><b>Ns Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH__NS_URI = 1;

	/**
	 * The feature id for the '<em><b>Ns Uri Exact</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH__NS_URI_EXACT = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH__NAME = 3;

	/**
	 * The feature id for the '<em><b>Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH__PREFIX = 4;

	/**
	 * The feature id for the '<em><b>Classifier</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH__CLASSIFIER = 5;

	/**
	 * The feature id for the '<em><b>Feature Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH__FEATURE_NAME = 6;

	/**
	 * The feature id for the '<em><b>Feature Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH__FEATURE_TYPE = 7;

	/**
	 * The feature id for the '<em><b>Feature Name Type Pair</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH__FEATURE_NAME_TYPE_PAIR = 8;

	/**
	 * The feature id for the '<em><b>Limit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH__LIMIT = 9;

	/**
	 * The feature id for the '<em><b>Offset</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH__OFFSET = 10;

	/**
	 * The number of structural features of the '<em>Package Search</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH_FEATURE_COUNT = 11;

	/**
	 * The number of operations of the '<em>Package Search</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchResultImpl <em>Package Search Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchResultImpl
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getPackageSearchResult()
	 * @generated
	 */
	int PACKAGE_SEARCH_RESULT = 7;

	/**
	 * The feature id for the '<em><b>Hits</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH_RESULT__HITS = 0;

	/**
	 * The feature id for the '<em><b>Total Hits</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH_RESULT__TOTAL_HITS = 1;

	/**
	 * The number of structural features of the '<em>Package Search Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH_RESULT_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Package Search Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_SEARCH_RESULT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.WriteResultImpl <em>Write Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.WriteResultImpl
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getWriteResult()
	 * @generated
	 */
	int WRITE_RESULT = 8;

	/**
	 * The feature id for the '<em><b>Outcome</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITE_RESULT__OUTCOME = 0;

	/**
	 * The feature id for the '<em><b>Metadata</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITE_RESULT__METADATA = 1;

	/**
	 * The number of structural features of the '<em>Write Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITE_RESULT_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Write Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITE_RESULT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.BatchItemResultImpl <em>Batch Item Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.BatchItemResultImpl
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getBatchItemResult()
	 * @generated
	 */
	int BATCH_ITEM_RESULT = 9;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_ITEM_RESULT__ID = 0;

	/**
	 * The feature id for the '<em><b>Outcome</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_ITEM_RESULT__OUTCOME = 1;

	/**
	 * The feature id for the '<em><b>Metadata</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_ITEM_RESULT__METADATA = 2;

	/**
	 * The feature id for the '<em><b>Refusal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_ITEM_RESULT__REFUSAL = 3;

	/**
	 * The feature id for the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_ITEM_RESULT__MESSAGE = 4;

	/**
	 * The number of structural features of the '<em>Batch Item Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_ITEM_RESULT_FEATURE_COUNT = 5;

	/**
	 * The operation id for the '<em>Is Refused</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_ITEM_RESULT___IS_REFUSED = 0;

	/**
	 * The number of operations of the '<em>Batch Item Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_ITEM_RESULT_OPERATION_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.BatchResultImpl <em>Batch Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.BatchResultImpl
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getBatchResult()
	 * @generated
	 */
	int BATCH_RESULT = 10;

	/**
	 * The feature id for the '<em><b>Applied</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_RESULT__APPLIED = 0;

	/**
	 * The feature id for the '<em><b>Items</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_RESULT__ITEMS = 1;

	/**
	 * The number of structural features of the '<em>Batch Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_RESULT_FEATURE_COUNT = 2;

	/**
	 * The operation id for the '<em>First Refusal</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_RESULT___FIRST_REFUSAL = 0;

	/**
	 * The number of operations of the '<em>Batch Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_RESULT_OPERATION_COUNT = 1;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy <em>Conflict Policy</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getConflictPolicy()
	 * @generated
	 */
	int CONFLICT_POLICY = 11;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.WriteOutcome <em>Write Outcome</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.WriteOutcome
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getWriteOutcome()
	 * @generated
	 */
	int WRITE_OUTCOME = 12;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.registry.api.RefusalReason <em>Refusal Reason</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.RefusalReason
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getRefusalReason()
	 * @generated
	 */
	int REFUSAL_REASON = 13;

	/**
	 * The meta object id for the '<em>Precondition</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.registry.api.Precondition
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getPrecondition()
	 * @generated
	 */
	int PRECONDITION = 14;

	/**
	 * The meta object id for the '<em>List</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.List
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getList()
	 * @generated
	 */
	int LIST = 15;

	/**
	 * The meta object id for the '<em>Optional</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.Optional
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getOptional()
	 * @generated
	 */
	int OPTIONAL = 16;

	/**
	 * The meta object id for the '<em>Map</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.Map
	 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getMap()
	 * @generated
	 */
	int MAP = 17;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter <em>Registry Writer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Registry Writer</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter
	 * @generated
	 */
	EClass getRegistryWriter();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#createObject(org.eclipse.fennec.model.atlas.registry.api.ObjectWrite) <em>Create Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Create Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#createObject(org.eclipse.fennec.model.atlas.registry.api.ObjectWrite)
	 * @generated
	 */
	EOperation getRegistryWriter__CreateObject__ObjectWrite();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#updateObject(org.eclipse.fennec.model.atlas.registry.api.ObjectWrite) <em>Update Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#updateObject(org.eclipse.fennec.model.atlas.registry.api.ObjectWrite)
	 * @generated
	 */
	EOperation getRegistryWriter__UpdateObject__ObjectWrite();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#deleteObject(org.eclipse.fennec.model.atlas.registry.api.ObjectAddress, boolean, org.eclipse.fennec.model.atlas.registry.api.Precondition) <em>Delete Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#deleteObject(org.eclipse.fennec.model.atlas.registry.api.ObjectAddress, boolean, org.eclipse.fennec.model.atlas.registry.api.Precondition)
	 * @generated
	 */
	EOperation getRegistryWriter__DeleteObject__ObjectAddress_boolean_Precondition();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#transitionObject(org.eclipse.fennec.model.atlas.registry.api.ObjectAddress, java.lang.String, boolean, org.eclipse.fennec.model.atlas.registry.api.Precondition) <em>Transition Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Transition Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#transitionObject(org.eclipse.fennec.model.atlas.registry.api.ObjectAddress, java.lang.String, boolean, org.eclipse.fennec.model.atlas.registry.api.Precondition)
	 * @generated
	 */
	EOperation getRegistryWriter__TransitionObject__ObjectAddress_String_boolean_Precondition();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#createPackage(org.eclipse.fennec.model.atlas.registry.api.PackageWrite) <em>Create Package</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Create Package</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#createPackage(org.eclipse.fennec.model.atlas.registry.api.PackageWrite)
	 * @generated
	 */
	EOperation getRegistryWriter__CreatePackage__PackageWrite();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#updatePackage(org.eclipse.fennec.model.atlas.registry.api.PackageWrite) <em>Update Package</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Package</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#updatePackage(org.eclipse.fennec.model.atlas.registry.api.PackageWrite)
	 * @generated
	 */
	EOperation getRegistryWriter__UpdatePackage__PackageWrite();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#updatePackageProperties(org.eclipse.fennec.model.atlas.registry.api.PackageAddress, java.util.Map, org.eclipse.fennec.model.atlas.registry.api.Precondition) <em>Update Package Properties</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Package Properties</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#updatePackageProperties(org.eclipse.fennec.model.atlas.registry.api.PackageAddress, java.util.Map, org.eclipse.fennec.model.atlas.registry.api.Precondition)
	 * @generated
	 */
	EOperation getRegistryWriter__UpdatePackageProperties__PackageAddress_Map_Precondition();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#deletePackage(org.eclipse.fennec.model.atlas.registry.api.PackageAddress, boolean, org.eclipse.fennec.model.atlas.registry.api.Precondition) <em>Delete Package</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete Package</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#deletePackage(org.eclipse.fennec.model.atlas.registry.api.PackageAddress, boolean, org.eclipse.fennec.model.atlas.registry.api.Precondition)
	 * @generated
	 */
	EOperation getRegistryWriter__DeletePackage__PackageAddress_boolean_Precondition();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#transitionPackage(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, org.eclipse.fennec.model.atlas.registry.api.Precondition) <em>Transition Package</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Transition Package</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#transitionPackage(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, org.eclipse.fennec.model.atlas.registry.api.Precondition)
	 * @generated
	 */
	EOperation getRegistryWriter__TransitionPackage__String_String_String_String_boolean_Precondition();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#writeObjects(java.util.List) <em>Write Objects</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Write Objects</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#writeObjects(java.util.List)
	 * @generated
	 */
	EOperation getRegistryWriter__WriteObjects__List();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#writePackages(java.util.List) <em>Write Packages</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Write Packages</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter#writePackages(java.util.List)
	 * @generated
	 */
	EOperation getRegistryWriter__WritePackages__List();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader <em>Registry Reader</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Registry Reader</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader
	 * @generated
	 */
	EClass getRegistryReader();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#schemaRegistry(java.lang.String) <em>Schema Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Schema Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#schemaRegistry(java.lang.String)
	 * @generated
	 */
	EOperation getRegistryReader__SchemaRegistry__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#listAll(java.lang.String, java.lang.String) <em>List All</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List All</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#listAll(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryReader__ListAll__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#listFinal(java.lang.String, java.lang.String) <em>List Final</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Final</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#listFinal(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryReader__ListFinal__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#list(java.lang.String, java.lang.String, java.lang.String) <em>List</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#list(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryReader__List__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#listByName(java.lang.String, java.lang.String, java.lang.String, java.lang.String) <em>List By Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List By Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#listByName(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryReader__ListByName__String_String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#metadata(org.eclipse.fennec.model.atlas.registry.api.ObjectAddress) <em>Metadata</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Metadata</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#metadata(org.eclipse.fennec.model.atlas.registry.api.ObjectAddress)
	 * @generated
	 */
	EOperation getRegistryReader__Metadata__ObjectAddress();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#finalMetadata(java.lang.String, java.lang.String, java.lang.String) <em>Final Metadata</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Final Metadata</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#finalMetadata(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryReader__FinalMetadata__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#content(org.eclipse.fennec.model.atlas.registry.api.ObjectAddress) <em>Content</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Content</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#content(org.eclipse.fennec.model.atlas.registry.api.ObjectAddress)
	 * @generated
	 */
	EOperation getRegistryReader__Content__ObjectAddress();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#finalContent(java.lang.String, java.lang.String, java.lang.String) <em>Final Content</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Final Content</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#finalContent(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryReader__FinalContent__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#packageMetadata(org.eclipse.fennec.model.atlas.registry.api.PackageAddress) <em>Package Metadata</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Package Metadata</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#packageMetadata(org.eclipse.fennec.model.atlas.registry.api.PackageAddress)
	 * @generated
	 */
	EOperation getRegistryReader__PackageMetadata__PackageAddress();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#finalPackageMetadata(java.lang.String, java.lang.String) <em>Final Package Metadata</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Final Package Metadata</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#finalPackageMetadata(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryReader__FinalPackageMetadata__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader#searchPackages(java.lang.String, org.eclipse.fennec.model.atlas.registry.api.PackageSearch) <em>Search Packages</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Search Packages</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader#searchPackages(java.lang.String, org.eclipse.fennec.model.atlas.registry.api.PackageSearch)
	 * @generated
	 */
	EOperation getRegistryReader__SearchPackages__String_PackageSearch();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress <em>Object Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Object Address</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectAddress
	 * @generated
	 */
	EClass getObjectAddress();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getScope <em>Scope</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Scope</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getScope()
	 * @see #getObjectAddress()
	 * @generated
	 */
	EAttribute getObjectAddress_Scope();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getRegistry <em>Registry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Registry</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getRegistry()
	 * @see #getObjectAddress()
	 * @generated
	 */
	EAttribute getObjectAddress_Registry();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getStage <em>Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stage</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getStage()
	 * @see #getObjectAddress()
	 * @generated
	 */
	EAttribute getObjectAddress_Stage();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getObjectId <em>Object Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Object Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getObjectId()
	 * @see #getObjectAddress()
	 * @generated
	 */
	EAttribute getObjectAddress_ObjectId();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.registry.api.PackageAddress <em>Package Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Package Address</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageAddress
	 * @generated
	 */
	EClass getPackageAddress();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageAddress#getScope <em>Scope</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Scope</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageAddress#getScope()
	 * @see #getPackageAddress()
	 * @generated
	 */
	EAttribute getPackageAddress_Scope();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageAddress#getStage <em>Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stage</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageAddress#getStage()
	 * @see #getPackageAddress()
	 * @generated
	 */
	EAttribute getPackageAddress_Stage();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageAddress#getNsUri <em>Ns Uri</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ns Uri</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageAddress#getNsUri()
	 * @see #getPackageAddress()
	 * @generated
	 */
	EAttribute getPackageAddress_NsUri();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite <em>Object Write</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Object Write</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectWrite
	 * @generated
	 */
	EClass getObjectWrite();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getAddress <em>Address</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Address</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getAddress()
	 * @see #getObjectWrite()
	 * @generated
	 */
	EReference getObjectWrite_Address();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getContent <em>Content</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Content</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getContent()
	 * @see #getObjectWrite()
	 * @generated
	 */
	EReference getObjectWrite_Content();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getName()
	 * @see #getObjectWrite()
	 * @generated
	 */
	EAttribute getObjectWrite_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getVersion()
	 * @see #getObjectWrite()
	 * @generated
	 */
	EAttribute getObjectWrite_Version();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getOnConflict <em>On Conflict</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>On Conflict</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getOnConflict()
	 * @see #getObjectWrite()
	 * @generated
	 */
	EAttribute getObjectWrite_OnConflict();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getPrecondition <em>Precondition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Precondition</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getPrecondition()
	 * @see #getObjectWrite()
	 * @generated
	 */
	EAttribute getObjectWrite_Precondition();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite <em>Package Write</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Package Write</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite
	 * @generated
	 */
	EClass getPackageWrite();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getScope <em>Scope</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Scope</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getScope()
	 * @see #getPackageWrite()
	 * @generated
	 */
	EAttribute getPackageWrite_Scope();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getRegistry <em>Registry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Registry</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getRegistry()
	 * @see #getPackageWrite()
	 * @generated
	 */
	EAttribute getPackageWrite_Registry();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getStage <em>Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stage</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getStage()
	 * @see #getPackageWrite()
	 * @generated
	 */
	EAttribute getPackageWrite_Stage();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getContent <em>Content</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Content</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getContent()
	 * @see #getPackageWrite()
	 * @generated
	 */
	EReference getPackageWrite_Content();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getNsUri <em>Ns Uri</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ns Uri</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getNsUri()
	 * @see #getPackageWrite()
	 * @generated
	 */
	EAttribute getPackageWrite_NsUri();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getName()
	 * @see #getPackageWrite()
	 * @generated
	 */
	EAttribute getPackageWrite_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getVersion()
	 * @see #getPackageWrite()
	 * @generated
	 */
	EAttribute getPackageWrite_Version();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getDcat <em>Dcat</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dcat</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getDcat()
	 * @see #getPackageWrite()
	 * @generated
	 */
	EAttribute getPackageWrite_Dcat();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getOnConflict <em>On Conflict</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>On Conflict</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getOnConflict()
	 * @see #getPackageWrite()
	 * @generated
	 */
	EAttribute getPackageWrite_OnConflict();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getPrecondition <em>Precondition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Precondition</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getPrecondition()
	 * @see #getPackageWrite()
	 * @generated
	 */
	EAttribute getPackageWrite_Precondition();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#isLenientVersion <em>Lenient Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Lenient Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageWrite#isLenientVersion()
	 * @see #getPackageWrite()
	 * @generated
	 */
	EAttribute getPackageWrite_LenientVersion();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch <em>Package Search</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Package Search</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch
	 * @generated
	 */
	EClass getPackageSearch();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getStage <em>Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stage</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getStage()
	 * @see #getPackageSearch()
	 * @generated
	 */
	EAttribute getPackageSearch_Stage();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getNsUri <em>Ns Uri</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ns Uri</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getNsUri()
	 * @see #getPackageSearch()
	 * @generated
	 */
	EAttribute getPackageSearch_NsUri();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getNsUriExact <em>Ns Uri Exact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ns Uri Exact</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getNsUriExact()
	 * @see #getPackageSearch()
	 * @generated
	 */
	EAttribute getPackageSearch_NsUriExact();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getName()
	 * @see #getPackageSearch()
	 * @generated
	 */
	EAttribute getPackageSearch_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getPrefix <em>Prefix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Prefix</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getPrefix()
	 * @see #getPackageSearch()
	 * @generated
	 */
	EAttribute getPackageSearch_Prefix();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getClassifier <em>Classifier</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Classifier</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getClassifier()
	 * @see #getPackageSearch()
	 * @generated
	 */
	EAttribute getPackageSearch_Classifier();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureName <em>Feature Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Feature Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureName()
	 * @see #getPackageSearch()
	 * @generated
	 */
	EAttribute getPackageSearch_FeatureName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureType <em>Feature Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Feature Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureType()
	 * @see #getPackageSearch()
	 * @generated
	 */
	EAttribute getPackageSearch_FeatureType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureNameTypePair <em>Feature Name Type Pair</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Feature Name Type Pair</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureNameTypePair()
	 * @see #getPackageSearch()
	 * @generated
	 */
	EAttribute getPackageSearch_FeatureNameTypePair();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getLimit <em>Limit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Limit</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getLimit()
	 * @see #getPackageSearch()
	 * @generated
	 */
	EAttribute getPackageSearch_Limit();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getOffset <em>Offset</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Offset</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getOffset()
	 * @see #getPackageSearch()
	 * @generated
	 */
	EAttribute getPackageSearch_Offset();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult <em>Package Search Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Package Search Result</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult
	 * @generated
	 */
	EClass getPackageSearchResult();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult#getHits <em>Hits</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Hits</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult#getHits()
	 * @see #getPackageSearchResult()
	 * @generated
	 */
	EReference getPackageSearchResult_Hits();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult#getTotalHits <em>Total Hits</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Total Hits</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult#getTotalHits()
	 * @see #getPackageSearchResult()
	 * @generated
	 */
	EAttribute getPackageSearchResult_TotalHits();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.registry.api.WriteResult <em>Write Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Write Result</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.WriteResult
	 * @generated
	 */
	EClass getWriteResult();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.WriteResult#getOutcome <em>Outcome</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Outcome</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.WriteResult#getOutcome()
	 * @see #getWriteResult()
	 * @generated
	 */
	EAttribute getWriteResult_Outcome();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.model.atlas.registry.api.WriteResult#getMetadata <em>Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Metadata</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.WriteResult#getMetadata()
	 * @see #getWriteResult()
	 * @generated
	 */
	EReference getWriteResult_Metadata();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult <em>Batch Item Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Batch Item Result</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchItemResult
	 * @generated
	 */
	EClass getBatchItemResult();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getId()
	 * @see #getBatchItemResult()
	 * @generated
	 */
	EAttribute getBatchItemResult_Id();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getOutcome <em>Outcome</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Outcome</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getOutcome()
	 * @see #getBatchItemResult()
	 * @generated
	 */
	EAttribute getBatchItemResult_Outcome();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getMetadata <em>Metadata</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Metadata</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getMetadata()
	 * @see #getBatchItemResult()
	 * @generated
	 */
	EReference getBatchItemResult_Metadata();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getRefusal <em>Refusal</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Refusal</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getRefusal()
	 * @see #getBatchItemResult()
	 * @generated
	 */
	EAttribute getBatchItemResult_Refusal();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getMessage <em>Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getMessage()
	 * @see #getBatchItemResult()
	 * @generated
	 */
	EAttribute getBatchItemResult_Message();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#isRefused() <em>Is Refused</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Refused</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#isRefused()
	 * @generated
	 */
	EOperation getBatchItemResult__IsRefused();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.registry.api.BatchResult <em>Batch Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Batch Result</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchResult
	 * @generated
	 */
	EClass getBatchResult();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.registry.api.BatchResult#isApplied <em>Applied</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Applied</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchResult#isApplied()
	 * @see #getBatchResult()
	 * @generated
	 */
	EAttribute getBatchResult_Applied();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.registry.api.BatchResult#getItems <em>Items</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Items</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchResult#getItems()
	 * @see #getBatchResult()
	 * @generated
	 */
	EReference getBatchResult_Items();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchResult#firstRefusal() <em>First Refusal</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>First Refusal</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.registry.api.BatchResult#firstRefusal()
	 * @generated
	 */
	EOperation getBatchResult__FirstRefusal();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy <em>Conflict Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Conflict Policy</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy
	 * @generated
	 */
	EEnum getConflictPolicy();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.registry.api.WriteOutcome <em>Write Outcome</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Write Outcome</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.WriteOutcome
	 * @generated
	 */
	EEnum getWriteOutcome();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.registry.api.RefusalReason <em>Refusal Reason</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Refusal Reason</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RefusalReason
	 * @generated
	 */
	EEnum getRefusalReason();

	/**
	 * Returns the meta object for data type '{@link org.eclipse.fennec.model.atlas.registry.api.Precondition <em>Precondition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * An optimistic-concurrency check on the object a write is about to change; a Java functional interface (the REST layer expresses If-Match as one).
     * <!-- end-model-doc -->
	 * @return the meta object for data type '<em>Precondition</em>'.
	 * @see org.eclipse.fennec.model.atlas.registry.api.Precondition
	 * @model instanceClass="org.eclipse.fennec.model.atlas.registry.api.Precondition" serializeable="false"
	 * @generated
	 */
	EDataType getPrecondition();

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
	 * Returns the meta object for data type '{@link java.util.Map <em>Map</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Map</em>'.
	 * @see java.util.Map
	 * @model instanceClass="java.util.Map" serializeable="false" typeParameters="K V"
	 * @generated
	 */
	EDataType getMap();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	RegistryApiFactory getRegistryApiFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryWriter <em>Registry Writer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryWriter
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getRegistryWriter()
		 * @generated
		 */
		EClass REGISTRY_WRITER = eINSTANCE.getRegistryWriter();

		/**
		 * The meta object literal for the '<em><b>Create Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_WRITER___CREATE_OBJECT__OBJECTWRITE = eINSTANCE.getRegistryWriter__CreateObject__ObjectWrite();

		/**
		 * The meta object literal for the '<em><b>Update Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_WRITER___UPDATE_OBJECT__OBJECTWRITE = eINSTANCE.getRegistryWriter__UpdateObject__ObjectWrite();

		/**
		 * The meta object literal for the '<em><b>Delete Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_WRITER___DELETE_OBJECT__OBJECTADDRESS_BOOLEAN_PRECONDITION = eINSTANCE.getRegistryWriter__DeleteObject__ObjectAddress_boolean_Precondition();

		/**
		 * The meta object literal for the '<em><b>Transition Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_WRITER___TRANSITION_OBJECT__OBJECTADDRESS_STRING_BOOLEAN_PRECONDITION = eINSTANCE.getRegistryWriter__TransitionObject__ObjectAddress_String_boolean_Precondition();

		/**
		 * The meta object literal for the '<em><b>Create Package</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_WRITER___CREATE_PACKAGE__PACKAGEWRITE = eINSTANCE.getRegistryWriter__CreatePackage__PackageWrite();

		/**
		 * The meta object literal for the '<em><b>Update Package</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_WRITER___UPDATE_PACKAGE__PACKAGEWRITE = eINSTANCE.getRegistryWriter__UpdatePackage__PackageWrite();

		/**
		 * The meta object literal for the '<em><b>Update Package Properties</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_WRITER___UPDATE_PACKAGE_PROPERTIES__PACKAGEADDRESS_MAP_PRECONDITION = eINSTANCE.getRegistryWriter__UpdatePackageProperties__PackageAddress_Map_Precondition();

		/**
		 * The meta object literal for the '<em><b>Delete Package</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_WRITER___DELETE_PACKAGE__PACKAGEADDRESS_BOOLEAN_PRECONDITION = eINSTANCE.getRegistryWriter__DeletePackage__PackageAddress_boolean_Precondition();

		/**
		 * The meta object literal for the '<em><b>Transition Package</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_WRITER___TRANSITION_PACKAGE__STRING_STRING_STRING_STRING_BOOLEAN_PRECONDITION = eINSTANCE.getRegistryWriter__TransitionPackage__String_String_String_String_boolean_Precondition();

		/**
		 * The meta object literal for the '<em><b>Write Objects</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_WRITER___WRITE_OBJECTS__LIST = eINSTANCE.getRegistryWriter__WriteObjects__List();

		/**
		 * The meta object literal for the '<em><b>Write Packages</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_WRITER___WRITE_PACKAGES__LIST = eINSTANCE.getRegistryWriter__WritePackages__List();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.RegistryReader <em>Registry Reader</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryReader
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getRegistryReader()
		 * @generated
		 */
		EClass REGISTRY_READER = eINSTANCE.getRegistryReader();

		/**
		 * The meta object literal for the '<em><b>Schema Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___SCHEMA_REGISTRY__STRING = eINSTANCE.getRegistryReader__SchemaRegistry__String();

		/**
		 * The meta object literal for the '<em><b>List All</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___LIST_ALL__STRING_STRING = eINSTANCE.getRegistryReader__ListAll__String_String();

		/**
		 * The meta object literal for the '<em><b>List Final</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___LIST_FINAL__STRING_STRING = eINSTANCE.getRegistryReader__ListFinal__String_String();

		/**
		 * The meta object literal for the '<em><b>List</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___LIST__STRING_STRING_STRING = eINSTANCE.getRegistryReader__List__String_String_String();

		/**
		 * The meta object literal for the '<em><b>List By Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___LIST_BY_NAME__STRING_STRING_STRING_STRING = eINSTANCE.getRegistryReader__ListByName__String_String_String_String();

		/**
		 * The meta object literal for the '<em><b>Metadata</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___METADATA__OBJECTADDRESS = eINSTANCE.getRegistryReader__Metadata__ObjectAddress();

		/**
		 * The meta object literal for the '<em><b>Final Metadata</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___FINAL_METADATA__STRING_STRING_STRING = eINSTANCE.getRegistryReader__FinalMetadata__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Content</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___CONTENT__OBJECTADDRESS = eINSTANCE.getRegistryReader__Content__ObjectAddress();

		/**
		 * The meta object literal for the '<em><b>Final Content</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___FINAL_CONTENT__STRING_STRING_STRING = eINSTANCE.getRegistryReader__FinalContent__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Package Metadata</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___PACKAGE_METADATA__PACKAGEADDRESS = eINSTANCE.getRegistryReader__PackageMetadata__PackageAddress();

		/**
		 * The meta object literal for the '<em><b>Final Package Metadata</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___FINAL_PACKAGE_METADATA__STRING_STRING = eINSTANCE.getRegistryReader__FinalPackageMetadata__String_String();

		/**
		 * The meta object literal for the '<em><b>Search Packages</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_READER___SEARCH_PACKAGES__STRING_PACKAGESEARCH = eINSTANCE.getRegistryReader__SearchPackages__String_PackageSearch();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.ObjectAddressImpl <em>Object Address</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.ObjectAddressImpl
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getObjectAddress()
		 * @generated
		 */
		EClass OBJECT_ADDRESS = eINSTANCE.getObjectAddress();

		/**
		 * The meta object literal for the '<em><b>Scope</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_ADDRESS__SCOPE = eINSTANCE.getObjectAddress_Scope();

		/**
		 * The meta object literal for the '<em><b>Registry</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_ADDRESS__REGISTRY = eINSTANCE.getObjectAddress_Registry();

		/**
		 * The meta object literal for the '<em><b>Stage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_ADDRESS__STAGE = eINSTANCE.getObjectAddress_Stage();

		/**
		 * The meta object literal for the '<em><b>Object Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_ADDRESS__OBJECT_ID = eINSTANCE.getObjectAddress_ObjectId();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageAddressImpl <em>Package Address</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.PackageAddressImpl
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getPackageAddress()
		 * @generated
		 */
		EClass PACKAGE_ADDRESS = eINSTANCE.getPackageAddress();

		/**
		 * The meta object literal for the '<em><b>Scope</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_ADDRESS__SCOPE = eINSTANCE.getPackageAddress_Scope();

		/**
		 * The meta object literal for the '<em><b>Stage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_ADDRESS__STAGE = eINSTANCE.getPackageAddress_Stage();

		/**
		 * The meta object literal for the '<em><b>Ns Uri</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_ADDRESS__NS_URI = eINSTANCE.getPackageAddress_NsUri();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.ObjectWriteImpl <em>Object Write</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.ObjectWriteImpl
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getObjectWrite()
		 * @generated
		 */
		EClass OBJECT_WRITE = eINSTANCE.getObjectWrite();

		/**
		 * The meta object literal for the '<em><b>Address</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OBJECT_WRITE__ADDRESS = eINSTANCE.getObjectWrite_Address();

		/**
		 * The meta object literal for the '<em><b>Content</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OBJECT_WRITE__CONTENT = eINSTANCE.getObjectWrite_Content();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_WRITE__NAME = eINSTANCE.getObjectWrite_Name();

		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_WRITE__VERSION = eINSTANCE.getObjectWrite_Version();

		/**
		 * The meta object literal for the '<em><b>On Conflict</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_WRITE__ON_CONFLICT = eINSTANCE.getObjectWrite_OnConflict();

		/**
		 * The meta object literal for the '<em><b>Precondition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OBJECT_WRITE__PRECONDITION = eINSTANCE.getObjectWrite_Precondition();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl <em>Package Write</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getPackageWrite()
		 * @generated
		 */
		EClass PACKAGE_WRITE = eINSTANCE.getPackageWrite();

		/**
		 * The meta object literal for the '<em><b>Scope</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_WRITE__SCOPE = eINSTANCE.getPackageWrite_Scope();

		/**
		 * The meta object literal for the '<em><b>Registry</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_WRITE__REGISTRY = eINSTANCE.getPackageWrite_Registry();

		/**
		 * The meta object literal for the '<em><b>Stage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_WRITE__STAGE = eINSTANCE.getPackageWrite_Stage();

		/**
		 * The meta object literal for the '<em><b>Content</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PACKAGE_WRITE__CONTENT = eINSTANCE.getPackageWrite_Content();

		/**
		 * The meta object literal for the '<em><b>Ns Uri</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_WRITE__NS_URI = eINSTANCE.getPackageWrite_NsUri();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_WRITE__NAME = eINSTANCE.getPackageWrite_Name();

		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_WRITE__VERSION = eINSTANCE.getPackageWrite_Version();

		/**
		 * The meta object literal for the '<em><b>Dcat</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_WRITE__DCAT = eINSTANCE.getPackageWrite_Dcat();

		/**
		 * The meta object literal for the '<em><b>On Conflict</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_WRITE__ON_CONFLICT = eINSTANCE.getPackageWrite_OnConflict();

		/**
		 * The meta object literal for the '<em><b>Precondition</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_WRITE__PRECONDITION = eINSTANCE.getPackageWrite_Precondition();

		/**
		 * The meta object literal for the '<em><b>Lenient Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_WRITE__LENIENT_VERSION = eINSTANCE.getPackageWrite_LenientVersion();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl <em>Package Search</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getPackageSearch()
		 * @generated
		 */
		EClass PACKAGE_SEARCH = eINSTANCE.getPackageSearch();

		/**
		 * The meta object literal for the '<em><b>Stage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH__STAGE = eINSTANCE.getPackageSearch_Stage();

		/**
		 * The meta object literal for the '<em><b>Ns Uri</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH__NS_URI = eINSTANCE.getPackageSearch_NsUri();

		/**
		 * The meta object literal for the '<em><b>Ns Uri Exact</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH__NS_URI_EXACT = eINSTANCE.getPackageSearch_NsUriExact();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH__NAME = eINSTANCE.getPackageSearch_Name();

		/**
		 * The meta object literal for the '<em><b>Prefix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH__PREFIX = eINSTANCE.getPackageSearch_Prefix();

		/**
		 * The meta object literal for the '<em><b>Classifier</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH__CLASSIFIER = eINSTANCE.getPackageSearch_Classifier();

		/**
		 * The meta object literal for the '<em><b>Feature Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH__FEATURE_NAME = eINSTANCE.getPackageSearch_FeatureName();

		/**
		 * The meta object literal for the '<em><b>Feature Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH__FEATURE_TYPE = eINSTANCE.getPackageSearch_FeatureType();

		/**
		 * The meta object literal for the '<em><b>Feature Name Type Pair</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH__FEATURE_NAME_TYPE_PAIR = eINSTANCE.getPackageSearch_FeatureNameTypePair();

		/**
		 * The meta object literal for the '<em><b>Limit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH__LIMIT = eINSTANCE.getPackageSearch_Limit();

		/**
		 * The meta object literal for the '<em><b>Offset</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH__OFFSET = eINSTANCE.getPackageSearch_Offset();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchResultImpl <em>Package Search Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchResultImpl
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getPackageSearchResult()
		 * @generated
		 */
		EClass PACKAGE_SEARCH_RESULT = eINSTANCE.getPackageSearchResult();

		/**
		 * The meta object literal for the '<em><b>Hits</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PACKAGE_SEARCH_RESULT__HITS = eINSTANCE.getPackageSearchResult_Hits();

		/**
		 * The meta object literal for the '<em><b>Total Hits</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_SEARCH_RESULT__TOTAL_HITS = eINSTANCE.getPackageSearchResult_TotalHits();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.WriteResultImpl <em>Write Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.WriteResultImpl
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getWriteResult()
		 * @generated
		 */
		EClass WRITE_RESULT = eINSTANCE.getWriteResult();

		/**
		 * The meta object literal for the '<em><b>Outcome</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WRITE_RESULT__OUTCOME = eINSTANCE.getWriteResult_Outcome();

		/**
		 * The meta object literal for the '<em><b>Metadata</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WRITE_RESULT__METADATA = eINSTANCE.getWriteResult_Metadata();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.BatchItemResultImpl <em>Batch Item Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.BatchItemResultImpl
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getBatchItemResult()
		 * @generated
		 */
		EClass BATCH_ITEM_RESULT = eINSTANCE.getBatchItemResult();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BATCH_ITEM_RESULT__ID = eINSTANCE.getBatchItemResult_Id();

		/**
		 * The meta object literal for the '<em><b>Outcome</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BATCH_ITEM_RESULT__OUTCOME = eINSTANCE.getBatchItemResult_Outcome();

		/**
		 * The meta object literal for the '<em><b>Metadata</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BATCH_ITEM_RESULT__METADATA = eINSTANCE.getBatchItemResult_Metadata();

		/**
		 * The meta object literal for the '<em><b>Refusal</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BATCH_ITEM_RESULT__REFUSAL = eINSTANCE.getBatchItemResult_Refusal();

		/**
		 * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BATCH_ITEM_RESULT__MESSAGE = eINSTANCE.getBatchItemResult_Message();

		/**
		 * The meta object literal for the '<em><b>Is Refused</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation BATCH_ITEM_RESULT___IS_REFUSED = eINSTANCE.getBatchItemResult__IsRefused();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.impl.BatchResultImpl <em>Batch Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.BatchResultImpl
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getBatchResult()
		 * @generated
		 */
		EClass BATCH_RESULT = eINSTANCE.getBatchResult();

		/**
		 * The meta object literal for the '<em><b>Applied</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BATCH_RESULT__APPLIED = eINSTANCE.getBatchResult_Applied();

		/**
		 * The meta object literal for the '<em><b>Items</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BATCH_RESULT__ITEMS = eINSTANCE.getBatchResult_Items();

		/**
		 * The meta object literal for the '<em><b>First Refusal</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation BATCH_RESULT___FIRST_REFUSAL = eINSTANCE.getBatchResult__FirstRefusal();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy <em>Conflict Policy</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getConflictPolicy()
		 * @generated
		 */
		EEnum CONFLICT_POLICY = eINSTANCE.getConflictPolicy();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.WriteOutcome <em>Write Outcome</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.WriteOutcome
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getWriteOutcome()
		 * @generated
		 */
		EEnum WRITE_OUTCOME = eINSTANCE.getWriteOutcome();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.registry.api.RefusalReason <em>Refusal Reason</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.RefusalReason
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getRefusalReason()
		 * @generated
		 */
		EEnum REFUSAL_REASON = eINSTANCE.getRefusalReason();

		/**
		 * The meta object literal for the '<em>Precondition</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.registry.api.Precondition
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getPrecondition()
		 * @generated
		 */
		EDataType PRECONDITION = eINSTANCE.getPrecondition();

		/**
		 * The meta object literal for the '<em>List</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.List
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getList()
		 * @generated
		 */
		EDataType LIST = eINSTANCE.getList();

		/**
		 * The meta object literal for the '<em>Optional</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.Optional
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getOptional()
		 * @generated
		 */
		EDataType OPTIONAL = eINSTANCE.getOptional();

		/**
		 * The meta object literal for the '<em>Map</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.Map
		 * @see org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiPackageImpl#getMap()
		 * @generated
		 */
		EDataType MAP = eINSTANCE.getMap();

	}

} //RegistryApiPackage
