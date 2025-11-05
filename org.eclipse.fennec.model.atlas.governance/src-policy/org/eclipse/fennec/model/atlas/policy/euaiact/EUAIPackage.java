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
package org.eclipse.fennec.model.atlas.policy.euaiact;


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
 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = EUAIPackage.eNS_URI, genModel = "/../policies.genmodel", genModelSourceLocations = {"model/policies/policies.genmodel","org.eclipse.fennec.model.atlas.governance/model/policies/policies.genmodel"}, ecore="/../eu-ai-act.ecore", ecoreSourceLocations="/model/policies/eu-ai-act.ecore")
public interface EUAIPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "euaiact";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/fennec/model/atlas/policy/eu_ai_act/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "euaiact";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	EUAIPackage eINSTANCE = org.eclipse.fennec.model.atlas.policy.euaiact.impl.EUAIPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.policy.euaiact.impl.EUAIActPolicyCheckImpl <em>Act Policy Check</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.impl.EUAIActPolicyCheckImpl
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.impl.EUAIPackageImpl#getEUAIActPolicyCheck()
	 * @generated
	 */
	int EUAI_ACT_POLICY_CHECK = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EUAI_ACT_POLICY_CHECK__STATUS = GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS;

	/**
	 * The feature id for the '<em><b>Check Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EUAI_ACT_POLICY_CHECK__CHECK_TIMESTAMP = GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EUAI_ACT_POLICY_CHECK__SUMMARY = GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY;

	/**
	 * The feature id for the '<em><b>Findings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EUAI_ACT_POLICY_CHECK__FINDINGS = GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS;

	/**
	 * The feature id for the '<em><b>Risk Level</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EUAI_ACT_POLICY_CHECK__RISK_LEVEL = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Is High Risk Annex III</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EUAI_ACT_POLICY_CHECK__IS_HIGH_RISK_ANNEX_III = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Threatens Health Safety Or Rights</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EUAI_ACT_POLICY_CHECK__THREATENS_HEALTH_SAFETY_OR_RIGHTS = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Is Unacceptable Risk Article5</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EUAI_ACT_POLICY_CHECK__IS_UNACCEPTABLE_RISK_ARTICLE5 = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Act Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EUAI_ACT_POLICY_CHECK_FEATURE_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Act Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EUAI_ACT_POLICY_CHECK_OPERATION_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.policy.euaiact.AIActRiskLevel <em>AI Act Risk Level</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.AIActRiskLevel
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.impl.EUAIPackageImpl#getAIActRiskLevel()
	 * @generated
	 */
	int AI_ACT_RISK_LEVEL = 1;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck <em>Act Policy Check</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Act Policy Check</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck
	 * @generated
	 */
	EClass getEUAIActPolicyCheck();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#getRiskLevel <em>Risk Level</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Risk Level</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#getRiskLevel()
	 * @see #getEUAIActPolicyCheck()
	 * @generated
	 */
	EAttribute getEUAIActPolicyCheck_RiskLevel();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isIsHighRiskAnnexIII <em>Is High Risk Annex III</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Is High Risk Annex III</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isIsHighRiskAnnexIII()
	 * @see #getEUAIActPolicyCheck()
	 * @generated
	 */
	EAttribute getEUAIActPolicyCheck_IsHighRiskAnnexIII();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isThreatensHealthSafetyOrRights <em>Threatens Health Safety Or Rights</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Threatens Health Safety Or Rights</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isThreatensHealthSafetyOrRights()
	 * @see #getEUAIActPolicyCheck()
	 * @generated
	 */
	EAttribute getEUAIActPolicyCheck_ThreatensHealthSafetyOrRights();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isIsUnacceptableRiskArticle5 <em>Is Unacceptable Risk Article5</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Is Unacceptable Risk Article5</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isIsUnacceptableRiskArticle5()
	 * @see #getEUAIActPolicyCheck()
	 * @generated
	 */
	EAttribute getEUAIActPolicyCheck_IsUnacceptableRiskArticle5();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.policy.euaiact.AIActRiskLevel <em>AI Act Risk Level</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>AI Act Risk Level</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.AIActRiskLevel
	 * @generated
	 */
	EEnum getAIActRiskLevel();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	EUAIFactory getEUAIFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.policy.euaiact.impl.EUAIActPolicyCheckImpl <em>Act Policy Check</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.policy.euaiact.impl.EUAIActPolicyCheckImpl
		 * @see org.eclipse.fennec.model.atlas.policy.euaiact.impl.EUAIPackageImpl#getEUAIActPolicyCheck()
		 * @generated
		 */
		EClass EUAI_ACT_POLICY_CHECK = eINSTANCE.getEUAIActPolicyCheck();

		/**
		 * The meta object literal for the '<em><b>Risk Level</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EUAI_ACT_POLICY_CHECK__RISK_LEVEL = eINSTANCE.getEUAIActPolicyCheck_RiskLevel();

		/**
		 * The meta object literal for the '<em><b>Is High Risk Annex III</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EUAI_ACT_POLICY_CHECK__IS_HIGH_RISK_ANNEX_III = eINSTANCE.getEUAIActPolicyCheck_IsHighRiskAnnexIII();

		/**
		 * The meta object literal for the '<em><b>Threatens Health Safety Or Rights</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EUAI_ACT_POLICY_CHECK__THREATENS_HEALTH_SAFETY_OR_RIGHTS = eINSTANCE.getEUAIActPolicyCheck_ThreatensHealthSafetyOrRights();

		/**
		 * The meta object literal for the '<em><b>Is Unacceptable Risk Article5</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EUAI_ACT_POLICY_CHECK__IS_UNACCEPTABLE_RISK_ARTICLE5 = eINSTANCE.getEUAIActPolicyCheck_IsUnacceptableRiskArticle5();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.policy.euaiact.AIActRiskLevel <em>AI Act Risk Level</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.policy.euaiact.AIActRiskLevel
		 * @see org.eclipse.fennec.model.atlas.policy.euaiact.impl.EUAIPackageImpl#getAIActRiskLevel()
		 * @generated
		 */
		EEnum AI_ACT_RISK_LEVEL = eINSTANCE.getAIActRiskLevel();

	}

} //EUAIPackage
