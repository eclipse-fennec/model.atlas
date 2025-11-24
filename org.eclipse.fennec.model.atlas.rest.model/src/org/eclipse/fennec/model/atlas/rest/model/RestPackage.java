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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.model;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
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
 * @see org.eclipse.fennec.model.atlas.rest.model.RestFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = RestPackage.eNS_URI, genModel = "/model/rest-response.genmodel", genModelSourceLocations = {"model/rest-response.genmodel","org.eclipse.fennec.model.atlas.rest.model/model/rest-response.genmodel"}, ecore="/model/rest-response.ecore", ecoreSourceLocations="/model/rest-response.ecore")
public interface RestPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "model";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/rest/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "rest";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	RestPackage eINSTANCE = org.eclipse.fennec.model.atlas.rest.model.impl.RestPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageInfoImpl <em>EPackage Info</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.rest.model.impl.EPackageInfoImpl
	 * @see org.eclipse.fennec.model.atlas.rest.model.impl.RestPackageImpl#getEPackageInfo()
	 * @generated
	 */
	int EPACKAGE_INFO = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_INFO__NAME = 0;

	/**
	 * The feature id for the '<em><b>Ns URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_INFO__NS_URI = 1;

	/**
	 * The feature id for the '<em><b>Ns Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_INFO__NS_PREFIX = 2;

	/**
	 * The feature id for the '<em><b>Classifier Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_INFO__CLASSIFIER_COUNT = 3;

	/**
	 * The feature id for the '<em><b>Sub Package Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_INFO__SUB_PACKAGE_COUNT = 4;

	/**
	 * The number of structural features of the '<em>EPackage Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_INFO_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>EPackage Info</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_INFO_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageListResponseImpl <em>EPackage List Response</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.rest.model.impl.EPackageListResponseImpl
	 * @see org.eclipse.fennec.model.atlas.rest.model.impl.RestPackageImpl#getEPackageListResponse()
	 * @generated
	 */
	int EPACKAGE_LIST_RESPONSE = 1;

	/**
	 * The feature id for the '<em><b>Packages</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_LIST_RESPONSE__PACKAGES = 0;

	/**
	 * The feature id for the '<em><b>Total</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_LIST_RESPONSE__TOTAL = 1;

	/**
	 * The feature id for the '<em><b>Skip</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_LIST_RESPONSE__SKIP = 2;

	/**
	 * The feature id for the '<em><b>Limit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_LIST_RESPONSE__LIMIT = 3;

	/**
	 * The number of structural features of the '<em>EPackage List Response</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_LIST_RESPONSE_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>EPackage List Response</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EPACKAGE_LIST_RESPONSE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.rest.model.impl.ErrorResponseImpl <em>Error Response</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.rest.model.impl.ErrorResponseImpl
	 * @see org.eclipse.fennec.model.atlas.rest.model.impl.RestPackageImpl#getErrorResponse()
	 * @generated
	 */
	int ERROR_RESPONSE = 2;

	/**
	 * The feature id for the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ERROR_RESPONSE__MESSAGE = 0;

	/**
	 * The feature id for the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ERROR_RESPONSE__CODE = 1;

	/**
	 * The feature id for the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ERROR_RESPONSE__TIMESTAMP = 2;

	/**
	 * The number of structural features of the '<em>Error Response</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ERROR_RESPONSE_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Error Response</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ERROR_RESPONSE_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo <em>EPackage Info</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EPackage Info</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.EPackageInfo
	 * @generated
	 */
	EClass getEPackageInfo();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getName()
	 * @see #getEPackageInfo()
	 * @generated
	 */
	EAttribute getEPackageInfo_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getNsURI <em>Ns URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ns URI</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getNsURI()
	 * @see #getEPackageInfo()
	 * @generated
	 */
	EAttribute getEPackageInfo_NsURI();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getNsPrefix <em>Ns Prefix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ns Prefix</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getNsPrefix()
	 * @see #getEPackageInfo()
	 * @generated
	 */
	EAttribute getEPackageInfo_NsPrefix();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getClassifierCount <em>Classifier Count</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Classifier Count</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getClassifierCount()
	 * @see #getEPackageInfo()
	 * @generated
	 */
	EAttribute getEPackageInfo_ClassifierCount();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getSubPackageCount <em>Sub Package Count</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sub Package Count</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getSubPackageCount()
	 * @see #getEPackageInfo()
	 * @generated
	 */
	EAttribute getEPackageInfo_SubPackageCount();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse <em>EPackage List Response</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EPackage List Response</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse
	 * @generated
	 */
	EClass getEPackageListResponse();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getPackages <em>Packages</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Packages</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getPackages()
	 * @see #getEPackageListResponse()
	 * @generated
	 */
	EReference getEPackageListResponse_Packages();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getTotal <em>Total</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Total</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getTotal()
	 * @see #getEPackageListResponse()
	 * @generated
	 */
	EAttribute getEPackageListResponse_Total();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getSkip <em>Skip</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Skip</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getSkip()
	 * @see #getEPackageListResponse()
	 * @generated
	 */
	EAttribute getEPackageListResponse_Skip();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getLimit <em>Limit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Limit</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getLimit()
	 * @see #getEPackageListResponse()
	 * @generated
	 */
	EAttribute getEPackageListResponse_Limit();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.rest.model.ErrorResponse <em>Error Response</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Error Response</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.ErrorResponse
	 * @generated
	 */
	EClass getErrorResponse();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.rest.model.ErrorResponse#getMessage <em>Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.ErrorResponse#getMessage()
	 * @see #getErrorResponse()
	 * @generated
	 */
	EAttribute getErrorResponse_Message();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.rest.model.ErrorResponse#getCode <em>Code</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Code</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.ErrorResponse#getCode()
	 * @see #getErrorResponse()
	 * @generated
	 */
	EAttribute getErrorResponse_Code();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.rest.model.ErrorResponse#getTimestamp <em>Timestamp</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Timestamp</em>'.
	 * @see org.eclipse.fennec.model.atlas.rest.model.ErrorResponse#getTimestamp()
	 * @see #getErrorResponse()
	 * @generated
	 */
	EAttribute getErrorResponse_Timestamp();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	RestFactory getRestFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageInfoImpl <em>EPackage Info</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.rest.model.impl.EPackageInfoImpl
		 * @see org.eclipse.fennec.model.atlas.rest.model.impl.RestPackageImpl#getEPackageInfo()
		 * @generated
		 */
		EClass EPACKAGE_INFO = eINSTANCE.getEPackageInfo();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EPACKAGE_INFO__NAME = eINSTANCE.getEPackageInfo_Name();

		/**
		 * The meta object literal for the '<em><b>Ns URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EPACKAGE_INFO__NS_URI = eINSTANCE.getEPackageInfo_NsURI();

		/**
		 * The meta object literal for the '<em><b>Ns Prefix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EPACKAGE_INFO__NS_PREFIX = eINSTANCE.getEPackageInfo_NsPrefix();

		/**
		 * The meta object literal for the '<em><b>Classifier Count</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EPACKAGE_INFO__CLASSIFIER_COUNT = eINSTANCE.getEPackageInfo_ClassifierCount();

		/**
		 * The meta object literal for the '<em><b>Sub Package Count</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EPACKAGE_INFO__SUB_PACKAGE_COUNT = eINSTANCE.getEPackageInfo_SubPackageCount();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageListResponseImpl <em>EPackage List Response</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.rest.model.impl.EPackageListResponseImpl
		 * @see org.eclipse.fennec.model.atlas.rest.model.impl.RestPackageImpl#getEPackageListResponse()
		 * @generated
		 */
		EClass EPACKAGE_LIST_RESPONSE = eINSTANCE.getEPackageListResponse();

		/**
		 * The meta object literal for the '<em><b>Packages</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EPACKAGE_LIST_RESPONSE__PACKAGES = eINSTANCE.getEPackageListResponse_Packages();

		/**
		 * The meta object literal for the '<em><b>Total</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EPACKAGE_LIST_RESPONSE__TOTAL = eINSTANCE.getEPackageListResponse_Total();

		/**
		 * The meta object literal for the '<em><b>Skip</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EPACKAGE_LIST_RESPONSE__SKIP = eINSTANCE.getEPackageListResponse_Skip();

		/**
		 * The meta object literal for the '<em><b>Limit</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EPACKAGE_LIST_RESPONSE__LIMIT = eINSTANCE.getEPackageListResponse_Limit();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.rest.model.impl.ErrorResponseImpl <em>Error Response</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.rest.model.impl.ErrorResponseImpl
		 * @see org.eclipse.fennec.model.atlas.rest.model.impl.RestPackageImpl#getErrorResponse()
		 * @generated
		 */
		EClass ERROR_RESPONSE = eINSTANCE.getErrorResponse();

		/**
		 * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ERROR_RESPONSE__MESSAGE = eINSTANCE.getErrorResponse_Message();

		/**
		 * The meta object literal for the '<em><b>Code</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ERROR_RESPONSE__CODE = eINSTANCE.getErrorResponse_Code();

		/**
		 * The meta object literal for the '<em><b>Timestamp</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ERROR_RESPONSE__TIMESTAMP = eINSTANCE.getErrorResponse_Timestamp();

	}

} //RestPackage
