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
package org.gecko.mac.policy.kritis;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;

import org.gecko.emf.osgi.annotation.provide.EPackage;

import org.gecko.mac.governance.GovernancePackage;

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
 * @see org.gecko.mac.policy.kritis.KritisFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = KritisPackage.eNS_URI, genModel = "/../policies.genmodel", genModelSourceLocations = {"model/policies/policies.genmodel","org.gecko.mac.governance/model/policies/policies.genmodel"}, ecore="/../kritis.ecore", ecoreSourceLocations="/model/policies/kritis.ecore")
public interface KritisPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "kritis";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://datainmotion.com/mac/policy/kritis/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "kritis";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	KritisPackage eINSTANCE = org.gecko.mac.policy.kritis.impl.KritisPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.gecko.mac.policy.kritis.impl.KRITISPolicyCheckImpl <em>KRITIS Policy Check</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.gecko.mac.policy.kritis.impl.KRITISPolicyCheckImpl
	 * @see org.gecko.mac.policy.kritis.impl.KritisPackageImpl#getKRITISPolicyCheck()
	 * @generated
	 */
	int KRITIS_POLICY_CHECK = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KRITIS_POLICY_CHECK__STATUS = GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS;

	/**
	 * The feature id for the '<em><b>Check Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KRITIS_POLICY_CHECK__CHECK_TIMESTAMP = GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KRITIS_POLICY_CHECK__SUMMARY = GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY;

	/**
	 * The feature id for the '<em><b>Findings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KRITIS_POLICY_CHECK__FINDINGS = GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS;

	/**
	 * The feature id for the '<em><b>Resilience Verified</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KRITIS_POLICY_CHECK__RESILIENCE_VERIFIED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Incident Response Ready</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KRITIS_POLICY_CHECK__INCIDENT_RESPONSE_READY = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Supply Chain Secure</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KRITIS_POLICY_CHECK__SUPPLY_CHAIN_SECURE = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>System Hardening Applied</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KRITIS_POLICY_CHECK__SYSTEM_HARDENING_APPLIED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>KRITIS Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KRITIS_POLICY_CHECK_FEATURE_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>KRITIS Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int KRITIS_POLICY_CHECK_OPERATION_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck <em>KRITIS Policy Check</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>KRITIS Policy Check</em>'.
	 * @see org.gecko.mac.policy.kritis.KRITISPolicyCheck
	 * @generated
	 */
	EClass getKRITISPolicyCheck();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isResilienceVerified <em>Resilience Verified</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Resilience Verified</em>'.
	 * @see org.gecko.mac.policy.kritis.KRITISPolicyCheck#isResilienceVerified()
	 * @see #getKRITISPolicyCheck()
	 * @generated
	 */
	EAttribute getKRITISPolicyCheck_ResilienceVerified();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isIncidentResponseReady <em>Incident Response Ready</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Incident Response Ready</em>'.
	 * @see org.gecko.mac.policy.kritis.KRITISPolicyCheck#isIncidentResponseReady()
	 * @see #getKRITISPolicyCheck()
	 * @generated
	 */
	EAttribute getKRITISPolicyCheck_IncidentResponseReady();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isSupplyChainSecure <em>Supply Chain Secure</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Supply Chain Secure</em>'.
	 * @see org.gecko.mac.policy.kritis.KRITISPolicyCheck#isSupplyChainSecure()
	 * @see #getKRITISPolicyCheck()
	 * @generated
	 */
	EAttribute getKRITISPolicyCheck_SupplyChainSecure();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isSystemHardeningApplied <em>System Hardening Applied</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>System Hardening Applied</em>'.
	 * @see org.gecko.mac.policy.kritis.KRITISPolicyCheck#isSystemHardeningApplied()
	 * @see #getKRITISPolicyCheck()
	 * @generated
	 */
	EAttribute getKRITISPolicyCheck_SystemHardeningApplied();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	KritisFactory getKritisFactory();

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
		 * The meta object literal for the '{@link org.gecko.mac.policy.kritis.impl.KRITISPolicyCheckImpl <em>KRITIS Policy Check</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.gecko.mac.policy.kritis.impl.KRITISPolicyCheckImpl
		 * @see org.gecko.mac.policy.kritis.impl.KritisPackageImpl#getKRITISPolicyCheck()
		 * @generated
		 */
		EClass KRITIS_POLICY_CHECK = eINSTANCE.getKRITISPolicyCheck();

		/**
		 * The meta object literal for the '<em><b>Resilience Verified</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute KRITIS_POLICY_CHECK__RESILIENCE_VERIFIED = eINSTANCE.getKRITISPolicyCheck_ResilienceVerified();

		/**
		 * The meta object literal for the '<em><b>Incident Response Ready</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute KRITIS_POLICY_CHECK__INCIDENT_RESPONSE_READY = eINSTANCE.getKRITISPolicyCheck_IncidentResponseReady();

		/**
		 * The meta object literal for the '<em><b>Supply Chain Secure</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute KRITIS_POLICY_CHECK__SUPPLY_CHAIN_SECURE = eINSTANCE.getKRITISPolicyCheck_SupplyChainSecure();

		/**
		 * The meta object literal for the '<em><b>System Hardening Applied</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute KRITIS_POLICY_CHECK__SYSTEM_HARDENING_APPLIED = eINSTANCE.getKRITISPolicyCheck_SystemHardeningApplied();

	}

} //KritisPackage
