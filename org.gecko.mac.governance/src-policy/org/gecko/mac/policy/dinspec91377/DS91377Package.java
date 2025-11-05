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
package org.gecko.mac.policy.dinspec91377;


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
 * @see org.gecko.mac.policy.dinspec91377.DS91377Factory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = DS91377Package.eNS_URI, genModel = "/../policies.genmodel", genModelSourceLocations = {"model/policies/policies.genmodel","org.gecko.mac.governance/model/policies/policies.genmodel"}, ecore="/../din-spec91377.ecore", ecoreSourceLocations="/model/policies/din-spec91377.ecore")
public interface DS91377Package extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "dinspec91377";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://datainmotion.com/mac/policy/din_spec_91377/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "dinspec91377";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DS91377Package eINSTANCE = org.gecko.mac.policy.dinspec91377.impl.DS91377PackageImpl.init();

	/**
	 * The meta object id for the '{@link org.gecko.mac.policy.dinspec91377.impl.DINSPEC91377PolicyCheckImpl <em>DINSPEC91377 Policy Check</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.gecko.mac.policy.dinspec91377.impl.DINSPEC91377PolicyCheckImpl
	 * @see org.gecko.mac.policy.dinspec91377.impl.DS91377PackageImpl#getDINSPEC91377PolicyCheck()
	 * @generated
	 */
	int DINSPEC91377_POLICY_CHECK = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK__STATUS = GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS;

	/**
	 * The feature id for the '<em><b>Check Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK__CHECK_TIMESTAMP = GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK__SUMMARY = GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY;

	/**
	 * The feature id for the '<em><b>Findings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK__FINDINGS = GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS;

	/**
	 * The feature id for the '<em><b>Architecture Layers Implemented</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK__ARCHITECTURE_LAYERS_IMPLEMENTED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Model Centric Data Flow Applied</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK__MODEL_CENTRIC_DATA_FLOW_APPLIED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Interface Interoperability Ensured</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK__INTERFACE_INTEROPERABILITY_ENSURED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Integration Api Standards Met</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK__INTEGRATION_API_STANDARDS_MET = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Management Services Provided</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK__MANAGEMENT_SERVICES_PROVIDED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Information Security Concept Applied</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK__INFORMATION_SECURITY_CONCEPT_APPLIED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>DINSPEC91377 Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK_FEATURE_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 6;

	/**
	 * The number of operations of the '<em>DINSPEC91377 Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DINSPEC91377_POLICY_CHECK_OPERATION_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck <em>DINSPEC91377 Policy Check</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>DINSPEC91377 Policy Check</em>'.
	 * @see org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck
	 * @generated
	 */
	EClass getDINSPEC91377PolicyCheck();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isArchitectureLayersImplemented <em>Architecture Layers Implemented</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Architecture Layers Implemented</em>'.
	 * @see org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isArchitectureLayersImplemented()
	 * @see #getDINSPEC91377PolicyCheck()
	 * @generated
	 */
	EAttribute getDINSPEC91377PolicyCheck_ArchitectureLayersImplemented();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isModelCentricDataFlowApplied <em>Model Centric Data Flow Applied</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Model Centric Data Flow Applied</em>'.
	 * @see org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isModelCentricDataFlowApplied()
	 * @see #getDINSPEC91377PolicyCheck()
	 * @generated
	 */
	EAttribute getDINSPEC91377PolicyCheck_ModelCentricDataFlowApplied();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isInterfaceInteroperabilityEnsured <em>Interface Interoperability Ensured</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Interface Interoperability Ensured</em>'.
	 * @see org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isInterfaceInteroperabilityEnsured()
	 * @see #getDINSPEC91377PolicyCheck()
	 * @generated
	 */
	EAttribute getDINSPEC91377PolicyCheck_InterfaceInteroperabilityEnsured();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isIntegrationApiStandardsMet <em>Integration Api Standards Met</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Integration Api Standards Met</em>'.
	 * @see org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isIntegrationApiStandardsMet()
	 * @see #getDINSPEC91377PolicyCheck()
	 * @generated
	 */
	EAttribute getDINSPEC91377PolicyCheck_IntegrationApiStandardsMet();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isManagementServicesProvided <em>Management Services Provided</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Management Services Provided</em>'.
	 * @see org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isManagementServicesProvided()
	 * @see #getDINSPEC91377PolicyCheck()
	 * @generated
	 */
	EAttribute getDINSPEC91377PolicyCheck_ManagementServicesProvided();

	/**
	 * Returns the meta object for the attribute '{@link org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isInformationSecurityConceptApplied <em>Information Security Concept Applied</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Information Security Concept Applied</em>'.
	 * @see org.gecko.mac.policy.dinspec91377.DINSPEC91377PolicyCheck#isInformationSecurityConceptApplied()
	 * @see #getDINSPEC91377PolicyCheck()
	 * @generated
	 */
	EAttribute getDINSPEC91377PolicyCheck_InformationSecurityConceptApplied();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	DS91377Factory getDS91377Factory();

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
		 * The meta object literal for the '{@link org.gecko.mac.policy.dinspec91377.impl.DINSPEC91377PolicyCheckImpl <em>DINSPEC91377 Policy Check</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.gecko.mac.policy.dinspec91377.impl.DINSPEC91377PolicyCheckImpl
		 * @see org.gecko.mac.policy.dinspec91377.impl.DS91377PackageImpl#getDINSPEC91377PolicyCheck()
		 * @generated
		 */
		EClass DINSPEC91377_POLICY_CHECK = eINSTANCE.getDINSPEC91377PolicyCheck();

		/**
		 * The meta object literal for the '<em><b>Architecture Layers Implemented</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DINSPEC91377_POLICY_CHECK__ARCHITECTURE_LAYERS_IMPLEMENTED = eINSTANCE.getDINSPEC91377PolicyCheck_ArchitectureLayersImplemented();

		/**
		 * The meta object literal for the '<em><b>Model Centric Data Flow Applied</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DINSPEC91377_POLICY_CHECK__MODEL_CENTRIC_DATA_FLOW_APPLIED = eINSTANCE.getDINSPEC91377PolicyCheck_ModelCentricDataFlowApplied();

		/**
		 * The meta object literal for the '<em><b>Interface Interoperability Ensured</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DINSPEC91377_POLICY_CHECK__INTERFACE_INTEROPERABILITY_ENSURED = eINSTANCE.getDINSPEC91377PolicyCheck_InterfaceInteroperabilityEnsured();

		/**
		 * The meta object literal for the '<em><b>Integration Api Standards Met</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DINSPEC91377_POLICY_CHECK__INTEGRATION_API_STANDARDS_MET = eINSTANCE.getDINSPEC91377PolicyCheck_IntegrationApiStandardsMet();

		/**
		 * The meta object literal for the '<em><b>Management Services Provided</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DINSPEC91377_POLICY_CHECK__MANAGEMENT_SERVICES_PROVIDED = eINSTANCE.getDINSPEC91377PolicyCheck_ManagementServicesProvided();

		/**
		 * The meta object literal for the '<em><b>Information Security Concept Applied</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DINSPEC91377_POLICY_CHECK__INFORMATION_SECURITY_CONCEPT_APPLIED = eINSTANCE.getDINSPEC91377PolicyCheck_InformationSecurityConceptApplied();

	}

} //DS91377Package
