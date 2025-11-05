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
package org.gecko.mac.policy.gdpr;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;

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
 * @see org.gecko.mac.policy.gdpr.GDPRFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = GDPRPackage.eNS_URI, genModel = "/../policies.genmodel", genModelSourceLocations = {"model/policies/policies.genmodel","org.gecko.mac.governance/model/policies/policies.genmodel"}, ecore="/../gdpr.ecore", ecoreSourceLocations="/model/policies/gdpr.ecore")
public interface GDPRPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "gdpr";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://datainmotion.com/mac/policy/gdpr/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "gdpr";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	GDPRPackage eINSTANCE = org.gecko.mac.policy.gdpr.impl.GDPRPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.gecko.mac.policy.gdpr.impl.GDPRPolicyCheckImpl <em>Policy Check</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.gecko.mac.policy.gdpr.impl.GDPRPolicyCheckImpl
	 * @see org.gecko.mac.policy.gdpr.impl.GDPRPackageImpl#getGDPRPolicyCheck()
	 * @generated
	 */
	int GDPR_POLICY_CHECK = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GDPR_POLICY_CHECK__STATUS = GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS;

	/**
	 * The feature id for the '<em><b>Check Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GDPR_POLICY_CHECK__CHECK_TIMESTAMP = GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GDPR_POLICY_CHECK__SUMMARY = GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY;

	/**
	 * The feature id for the '<em><b>Findings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GDPR_POLICY_CHECK__FINDINGS = GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS;

	/**
	 * The feature id for the '<em><b>Contains Personal Data</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GDPR_POLICY_CHECK__CONTAINS_PERSONAL_DATA = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Legal Basis For Processing</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GDPR_POLICY_CHECK__LEGAL_BASIS_FOR_PROCESSING = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Data Minimization Applied</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GDPR_POLICY_CHECK__DATA_MINIMIZATION_APPLIED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Purpose Limitation Met</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GDPR_POLICY_CHECK__PURPOSE_LIMITATION_MET = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GDPR_POLICY_CHECK_FEATURE_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GDPR_POLICY_CHECK_OPERATION_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.gecko.mac.policy.gdpr.LegalBasis <em>Legal Basis</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.gecko.mac.policy.gdpr.LegalBasis
	 * @see org.gecko.mac.policy.gdpr.impl.GDPRPackageImpl#getLegalBasis()
	 * @generated
	 */
	int LEGAL_BASIS = 1;


	/**
	 * Returns the meta object for class '{@link org.gecko.mac.policy.gdpr.GDPRPolicyCheck <em>Policy Check</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Policy Check</em>'.
	 * @see org.gecko.mac.policy.gdpr.GDPRPolicyCheck
	 * @generated
	 */
	EClass getGDPRPolicyCheck();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.gdpr.GDPRPolicyCheck#isContainsPersonalData <em>Contains Personal Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Contains Personal Data</em>'.
	 * @see org.gecko.mac.policy.gdpr.GDPRPolicyCheck#isContainsPersonalData()
	 * @see #getGDPRPolicyCheck()
	 * @generated
	 */
	EAttribute getGDPRPolicyCheck_ContainsPersonalData();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.gdpr.GDPRPolicyCheck#getLegalBasisForProcessing <em>Legal Basis For Processing</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Legal Basis For Processing</em>'.
	 * @see org.gecko.mac.policy.gdpr.GDPRPolicyCheck#getLegalBasisForProcessing()
	 * @see #getGDPRPolicyCheck()
	 * @generated
	 */
	EAttribute getGDPRPolicyCheck_LegalBasisForProcessing();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.gdpr.GDPRPolicyCheck#isDataMinimizationApplied <em>Data Minimization Applied</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data Minimization Applied</em>'.
	 * @see org.gecko.mac.policy.gdpr.GDPRPolicyCheck#isDataMinimizationApplied()
	 * @see #getGDPRPolicyCheck()
	 * @generated
	 */
	EAttribute getGDPRPolicyCheck_DataMinimizationApplied();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.gdpr.GDPRPolicyCheck#isPurposeLimitationMet <em>Purpose Limitation Met</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Purpose Limitation Met</em>'.
	 * @see org.gecko.mac.policy.gdpr.GDPRPolicyCheck#isPurposeLimitationMet()
	 * @see #getGDPRPolicyCheck()
	 * @generated
	 */
	EAttribute getGDPRPolicyCheck_PurposeLimitationMet();

	/**
	 * Returns the meta object for enum '{@link org.gecko.mac.policy.gdpr.LegalBasis <em>Legal Basis</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Legal Basis</em>'.
	 * @see org.gecko.mac.policy.gdpr.LegalBasis
	 * @generated
	 */
	EEnum getLegalBasis();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	GDPRFactory getGDPRFactory();

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
		 * The meta object literal for the '{@link org.gecko.mac.policy.gdpr.impl.GDPRPolicyCheckImpl <em>Policy Check</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.gecko.mac.policy.gdpr.impl.GDPRPolicyCheckImpl
		 * @see org.gecko.mac.policy.gdpr.impl.GDPRPackageImpl#getGDPRPolicyCheck()
		 * @generated
		 */
		EClass GDPR_POLICY_CHECK = eINSTANCE.getGDPRPolicyCheck();

		/**
		 * The meta object literal for the '<em><b>Contains Personal Data</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GDPR_POLICY_CHECK__CONTAINS_PERSONAL_DATA = eINSTANCE.getGDPRPolicyCheck_ContainsPersonalData();

		/**
		 * The meta object literal for the '<em><b>Legal Basis For Processing</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GDPR_POLICY_CHECK__LEGAL_BASIS_FOR_PROCESSING = eINSTANCE.getGDPRPolicyCheck_LegalBasisForProcessing();

		/**
		 * The meta object literal for the '<em><b>Data Minimization Applied</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GDPR_POLICY_CHECK__DATA_MINIMIZATION_APPLIED = eINSTANCE.getGDPRPolicyCheck_DataMinimizationApplied();

		/**
		 * The meta object literal for the '<em><b>Purpose Limitation Met</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GDPR_POLICY_CHECK__PURPOSE_LIMITATION_MET = eINSTANCE.getGDPRPolicyCheck_PurposeLimitationMet();

		/**
		 * The meta object literal for the '{@link org.gecko.mac.policy.gdpr.LegalBasis <em>Legal Basis</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.gecko.mac.policy.gdpr.LegalBasis
		 * @see org.gecko.mac.policy.gdpr.impl.GDPRPackageImpl#getLegalBasis()
		 * @generated
		 */
		EEnum LEGAL_BASIS = eINSTANCE.getLegalBasis();

	}

} //GDPRPackage
