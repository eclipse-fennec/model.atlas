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
package org.eclipse.fennec.model.atlas.policy.iso9001;


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
 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001Factory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = ISO9001Package.eNS_URI, genModel = "/../policies.genmodel", genModelSourceLocations = {"model/policies/policies.genmodel","org.eclipse.fennec.model.atlas.governance/model/policies/policies.genmodel"}, ecore="/../din-iso9001.ecore", ecoreSourceLocations="/model/policies/din-iso9001.ecore")
public interface ISO9001Package extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "iso9001";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/fennec/model/atlas/policy/iso_9001/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "iso9001";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ISO9001Package eINSTANCE = org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PolicyCheckImpl <em>Policy Check</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PolicyCheckImpl
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PackageImpl#getISO9001PolicyCheck()
	 * @generated
	 */
	int ISO9001_POLICY_CHECK = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO9001_POLICY_CHECK__STATUS = GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS;

	/**
	 * The feature id for the '<em><b>Check Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO9001_POLICY_CHECK__CHECK_TIMESTAMP = GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO9001_POLICY_CHECK__SUMMARY = GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY;

	/**
	 * The feature id for the '<em><b>Findings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO9001_POLICY_CHECK__FINDINGS = GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS;

	/**
	 * The feature id for the '<em><b>Process Approach Applied</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO9001_POLICY_CHECK__PROCESS_APPROACH_APPLIED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Documented Information Maintained</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO9001_POLICY_CHECK__DOCUMENTED_INFORMATION_MAINTAINED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Customer Focus Demonstrated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO9001_POLICY_CHECK__CUSTOMER_FOCUS_DEMONSTRATED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Nonconformity Process Exists</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO9001_POLICY_CHECK__NONCONFORMITY_PROCESS_EXISTS = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Resource And Competence Verified</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO9001_POLICY_CHECK__RESOURCE_AND_COMPETENCE_VERIFIED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO9001_POLICY_CHECK_FEATURE_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 5;

	/**
	 * The number of operations of the '<em>Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ISO9001_POLICY_CHECK_OPERATION_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck <em>Policy Check</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Policy Check</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck
	 * @generated
	 */
	EClass getISO9001PolicyCheck();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isProcessApproachApplied <em>Process Approach Applied</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Process Approach Applied</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isProcessApproachApplied()
	 * @see #getISO9001PolicyCheck()
	 * @generated
	 */
	EAttribute getISO9001PolicyCheck_ProcessApproachApplied();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isDocumentedInformationMaintained <em>Documented Information Maintained</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Documented Information Maintained</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isDocumentedInformationMaintained()
	 * @see #getISO9001PolicyCheck()
	 * @generated
	 */
	EAttribute getISO9001PolicyCheck_DocumentedInformationMaintained();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isCustomerFocusDemonstrated <em>Customer Focus Demonstrated</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Customer Focus Demonstrated</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isCustomerFocusDemonstrated()
	 * @see #getISO9001PolicyCheck()
	 * @generated
	 */
	EAttribute getISO9001PolicyCheck_CustomerFocusDemonstrated();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isNonconformityProcessExists <em>Nonconformity Process Exists</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Nonconformity Process Exists</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isNonconformityProcessExists()
	 * @see #getISO9001PolicyCheck()
	 * @generated
	 */
	EAttribute getISO9001PolicyCheck_NonconformityProcessExists();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isResourceAndCompetenceVerified <em>Resource And Competence Verified</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Resource And Competence Verified</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isResourceAndCompetenceVerified()
	 * @see #getISO9001PolicyCheck()
	 * @generated
	 */
	EAttribute getISO9001PolicyCheck_ResourceAndCompetenceVerified();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ISO9001Factory getISO9001Factory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PolicyCheckImpl <em>Policy Check</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PolicyCheckImpl
		 * @see org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PackageImpl#getISO9001PolicyCheck()
		 * @generated
		 */
		EClass ISO9001_POLICY_CHECK = eINSTANCE.getISO9001PolicyCheck();

		/**
		 * The meta object literal for the '<em><b>Process Approach Applied</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISO9001_POLICY_CHECK__PROCESS_APPROACH_APPLIED = eINSTANCE.getISO9001PolicyCheck_ProcessApproachApplied();

		/**
		 * The meta object literal for the '<em><b>Documented Information Maintained</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISO9001_POLICY_CHECK__DOCUMENTED_INFORMATION_MAINTAINED = eINSTANCE.getISO9001PolicyCheck_DocumentedInformationMaintained();

		/**
		 * The meta object literal for the '<em><b>Customer Focus Demonstrated</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISO9001_POLICY_CHECK__CUSTOMER_FOCUS_DEMONSTRATED = eINSTANCE.getISO9001PolicyCheck_CustomerFocusDemonstrated();

		/**
		 * The meta object literal for the '<em><b>Nonconformity Process Exists</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISO9001_POLICY_CHECK__NONCONFORMITY_PROCESS_EXISTS = eINSTANCE.getISO9001PolicyCheck_NonconformityProcessExists();

		/**
		 * The meta object literal for the '<em><b>Resource And Competence Verified</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ISO9001_POLICY_CHECK__RESOURCE_AND_COMPETENCE_VERIFIED = eINSTANCE.getISO9001PolicyCheck_ResourceAndCompetenceVerified();

	}

} //ISO9001Package
