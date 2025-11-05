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
package org.eclipse.fennec.model.atlas.policy.opendata;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;

import org.eclipse.fennec.model.atlas.governance.GovernancePackage;

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
 * @see org.eclipse.fennec.model.atlas.policy.opendata.OpenDataFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = OpenDataPackage.eNS_URI, genModel = "/../policies.genmodel", genModelSourceLocations = {"model/policies/policies.genmodel","org.eclipse.fennec.model.atlas.governance/model/policies/policies.genmodel"}, ecore="/../opendata.ecore", ecoreSourceLocations="/model/policies/opendata.ecore")
public interface OpenDataPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "opendata";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/fennec/model/atlas/policy/open_data/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "opendata";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	OpenDataPackage eINSTANCE = org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPolicyCheckImpl <em>Policy Check</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPolicyCheckImpl
	 * @see org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPackageImpl#getOpenDataPolicyCheck()
	 * @generated
	 */
	int OPEN_DATA_POLICY_CHECK = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPEN_DATA_POLICY_CHECK__STATUS = GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS;

	/**
	 * The feature id for the '<em><b>Check Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPEN_DATA_POLICY_CHECK__CHECK_TIMESTAMP = GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPEN_DATA_POLICY_CHECK__SUMMARY = GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY;

	/**
	 * The feature id for the '<em><b>Findings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPEN_DATA_POLICY_CHECK__FINDINGS = GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS;

	/**
	 * The feature id for the '<em><b>Pii Anonymization Verified</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPEN_DATA_POLICY_CHECK__PII_ANONYMIZATION_VERIFIED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>License Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPEN_DATA_POLICY_CHECK__LICENSE_TYPE = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Is Machine Readable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPEN_DATA_POLICY_CHECK__IS_MACHINE_READABLE = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Has Public Endpoint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPEN_DATA_POLICY_CHECK__HAS_PUBLIC_ENDPOINT = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPEN_DATA_POLICY_CHECK_FEATURE_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPEN_DATA_POLICY_CHECK_OPERATION_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.policy.opendata.OpenDataLicense <em>License</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.policy.opendata.OpenDataLicense
	 * @see org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPackageImpl#getOpenDataLicense()
	 * @generated
	 */
	int OPEN_DATA_LICENSE = 1;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPolicyCheck <em>Policy Check</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Policy Check</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPolicyCheck
	 * @generated
	 */
	EClass getOpenDataPolicyCheck();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPolicyCheck#isPiiAnonymizationVerified <em>Pii Anonymization Verified</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pii Anonymization Verified</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPolicyCheck#isPiiAnonymizationVerified()
	 * @see #getOpenDataPolicyCheck()
	 * @generated
	 */
	EAttribute getOpenDataPolicyCheck_PiiAnonymizationVerified();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPolicyCheck#getLicenseType <em>License Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>License Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPolicyCheck#getLicenseType()
	 * @see #getOpenDataPolicyCheck()
	 * @generated
	 */
	EAttribute getOpenDataPolicyCheck_LicenseType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPolicyCheck#isIsMachineReadable <em>Is Machine Readable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Is Machine Readable</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPolicyCheck#isIsMachineReadable()
	 * @see #getOpenDataPolicyCheck()
	 * @generated
	 */
	EAttribute getOpenDataPolicyCheck_IsMachineReadable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPolicyCheck#isHasPublicEndpoint <em>Has Public Endpoint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Has Public Endpoint</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPolicyCheck#isHasPublicEndpoint()
	 * @see #getOpenDataPolicyCheck()
	 * @generated
	 */
	EAttribute getOpenDataPolicyCheck_HasPublicEndpoint();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.policy.opendata.OpenDataLicense <em>License</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>License</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.opendata.OpenDataLicense
	 * @generated
	 */
	EEnum getOpenDataLicense();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	OpenDataFactory getOpenDataFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPolicyCheckImpl <em>Policy Check</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPolicyCheckImpl
		 * @see org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPackageImpl#getOpenDataPolicyCheck()
		 * @generated
		 */
		EClass OPEN_DATA_POLICY_CHECK = eINSTANCE.getOpenDataPolicyCheck();

		/**
		 * The meta object literal for the '<em><b>Pii Anonymization Verified</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OPEN_DATA_POLICY_CHECK__PII_ANONYMIZATION_VERIFIED = eINSTANCE.getOpenDataPolicyCheck_PiiAnonymizationVerified();

		/**
		 * The meta object literal for the '<em><b>License Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OPEN_DATA_POLICY_CHECK__LICENSE_TYPE = eINSTANCE.getOpenDataPolicyCheck_LicenseType();

		/**
		 * The meta object literal for the '<em><b>Is Machine Readable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OPEN_DATA_POLICY_CHECK__IS_MACHINE_READABLE = eINSTANCE.getOpenDataPolicyCheck_IsMachineReadable();

		/**
		 * The meta object literal for the '<em><b>Has Public Endpoint</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OPEN_DATA_POLICY_CHECK__HAS_PUBLIC_ENDPOINT = eINSTANCE.getOpenDataPolicyCheck_HasPublicEndpoint();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.policy.opendata.OpenDataLicense <em>License</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.policy.opendata.OpenDataLicense
		 * @see org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPackageImpl#getOpenDataLicense()
		 * @generated
		 */
		EEnum OPEN_DATA_LICENSE = eINSTANCE.getOpenDataLicense();

	}

} //OpenDataPackage
