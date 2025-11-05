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
package org.eclipse.fennec.model.atlas.policy.iso27001;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;

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
 * @see org.eclipse.fennec.model.atlas.policy.iso27001.ISO27Factory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = ISO27Package.eNS_URI, genModel = "/../policies.genmodel", genModelSourceLocations = {"model/policies/policies.genmodel","org.eclipse.fennec.model.atlas.governance/model/policies/policies.genmodel"}, ecore="/../iso27001.ecore", ecoreSourceLocations="/model/policies/iso27001.ecore")
public interface ISO27Package extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "iso27001";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/fennec/model/atlas/policy/iso_27001/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "iso27001";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ISO27Package eINSTANCE = org.eclipse.fennec.model.atlas.policy.iso27001.impl.ISO27PackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.policy.iso27001.impl.ISO27001PolicyCheckImpl <em>ISO27001 Policy Check</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.policy.iso27001.impl.ISO27001PolicyCheckImpl
	 * @see org.eclipse.fennec.model.atlas.policy.iso27001.impl.ISO27PackageImpl#getISO27001PolicyCheck()
	 * @generated
	 */
	int ISO27001_POLICY_CHECK = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO27001_POLICY_CHECK__STATUS = GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS;

	/**
	 * The feature id for the '<em><b>Check Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO27001_POLICY_CHECK__CHECK_TIMESTAMP = GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO27001_POLICY_CHECK__SUMMARY = GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY;

	/**
	 * The feature id for the '<em><b>Findings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO27001_POLICY_CHECK__FINDINGS = GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS;

	/**
	 * The feature id for the '<em><b>Network Control Implemented</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO27001_POLICY_CHECK__NETWORK_CONTROL_IMPLEMENTED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Access Control Verified</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO27001_POLICY_CHECK__ACCESS_CONTROL_VERIFIED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Logging And Monitoring Active</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO27001_POLICY_CHECK__LOGGING_AND_MONITORING_ACTIVE = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Asset Management Compliant</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO27001_POLICY_CHECK__ASSET_MANAGEMENT_COMPLIANT = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>ISO27001 Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO27001_POLICY_CHECK_FEATURE_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>ISO27001 Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO27001_POLICY_CHECK_OPERATION_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.policy.iso27001.ISO27001PolicyCheck <em>ISO27001 Policy Check</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>ISO27001 Policy Check</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.iso27001.ISO27001PolicyCheck
	 * @generated
	 */
	EClass getISO27001PolicyCheck();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.iso27001.ISO27001PolicyCheck#isNetworkControlImplemented <em>Network Control Implemented</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Network Control Implemented</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.iso27001.ISO27001PolicyCheck#isNetworkControlImplemented()
	 * @see #getISO27001PolicyCheck()
	 * @generated
	 */
	EAttribute getISO27001PolicyCheck_NetworkControlImplemented();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.iso27001.ISO27001PolicyCheck#isAccessControlVerified <em>Access Control Verified</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Access Control Verified</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.iso27001.ISO27001PolicyCheck#isAccessControlVerified()
	 * @see #getISO27001PolicyCheck()
	 * @generated
	 */
	EAttribute getISO27001PolicyCheck_AccessControlVerified();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.iso27001.ISO27001PolicyCheck#isLoggingAndMonitoringActive <em>Logging And Monitoring Active</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Logging And Monitoring Active</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.iso27001.ISO27001PolicyCheck#isLoggingAndMonitoringActive()
	 * @see #getISO27001PolicyCheck()
	 * @generated
	 */
	EAttribute getISO27001PolicyCheck_LoggingAndMonitoringActive();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.iso27001.ISO27001PolicyCheck#isAssetManagementCompliant <em>Asset Management Compliant</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Asset Management Compliant</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.iso27001.ISO27001PolicyCheck#isAssetManagementCompliant()
	 * @see #getISO27001PolicyCheck()
	 * @generated
	 */
	EAttribute getISO27001PolicyCheck_AssetManagementCompliant();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ISO27Factory getISO27Factory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.policy.iso27001.impl.ISO27001PolicyCheckImpl <em>ISO27001 Policy Check</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.policy.iso27001.impl.ISO27001PolicyCheckImpl
		 * @see org.eclipse.fennec.model.atlas.policy.iso27001.impl.ISO27PackageImpl#getISO27001PolicyCheck()
		 * @generated
		 */
		EClass ISO27001_POLICY_CHECK = eINSTANCE.getISO27001PolicyCheck();

		/**
		 * The meta object literal for the '<em><b>Network Control Implemented</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISO27001_POLICY_CHECK__NETWORK_CONTROL_IMPLEMENTED = eINSTANCE.getISO27001PolicyCheck_NetworkControlImplemented();

		/**
		 * The meta object literal for the '<em><b>Access Control Verified</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISO27001_POLICY_CHECK__ACCESS_CONTROL_VERIFIED = eINSTANCE.getISO27001PolicyCheck_AccessControlVerified();

		/**
		 * The meta object literal for the '<em><b>Logging And Monitoring Active</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISO27001_POLICY_CHECK__LOGGING_AND_MONITORING_ACTIVE = eINSTANCE.getISO27001PolicyCheck_LoggingAndMonitoringActive();

		/**
		 * The meta object literal for the '<em><b>Asset Management Compliant</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISO27001_POLICY_CHECK__ASSET_MANAGEMENT_COMPLIANT = eINSTANCE.getISO27001PolicyCheck_AssetManagementCompliant();

	}

} //ISO27Package
