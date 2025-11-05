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

import org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Act Policy Check</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A specialized compliance check that represents the risk assessment according to the EU AI Act.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#getRiskLevel <em>Risk Level</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isIsHighRiskAnnexIII <em>Is High Risk Annex III</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isThreatensHealthSafetyOrRights <em>Threatens Health Safety Or Rights</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isIsUnacceptableRiskArticle5 <em>Is Unacceptable Risk Article5</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIPackage#getEUAIActPolicyCheck()
 * @model
 * @generated
 */
@ProviderType
public interface EUAIActPolicyCheck extends ComplianceCheckResult {
	/**
	 * Returns the value of the '<em><b>Risk Level</b></em>' attribute.
	 * The default value is <code>"UNKNOWN"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.policy.euaiact.AIActRiskLevel}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The final, determined risk level of the AI system according to the EU AI Act classification.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Risk Level</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.AIActRiskLevel
	 * @see #setRiskLevel(AIActRiskLevel)
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIPackage#getEUAIActPolicyCheck_RiskLevel()
	 * @model default="UNKNOWN" required="true"
	 * @generated
	 */
	AIActRiskLevel getRiskLevel();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#getRiskLevel <em>Risk Level</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Risk Level</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.AIActRiskLevel
	 * @see #getRiskLevel()
	 * @generated
	 */
	void setRiskLevel(AIActRiskLevel value);

	/**
	 * Returns the value of the '<em><b>Is High Risk Annex III</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Does the system fall into one of the high-risk categories defined in Annex III of the AI Act?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Is High Risk Annex III</em>' attribute.
	 * @see #setIsHighRiskAnnexIII(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIPackage#getEUAIActPolicyCheck_IsHighRiskAnnexIII()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isIsHighRiskAnnexIII();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isIsHighRiskAnnexIII <em>Is High Risk Annex III</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is High Risk Annex III</em>' attribute.
	 * @see #isIsHighRiskAnnexIII()
	 * @generated
	 */
	void setIsHighRiskAnnexIII(boolean value);

	/**
	 * Returns the value of the '<em><b>Threatens Health Safety Or Rights</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Does the system pose a potential threat to health, safety, or fundamental rights?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Threatens Health Safety Or Rights</em>' attribute.
	 * @see #setThreatensHealthSafetyOrRights(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIPackage#getEUAIActPolicyCheck_ThreatensHealthSafetyOrRights()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isThreatensHealthSafetyOrRights();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isThreatensHealthSafetyOrRights <em>Threatens Health Safety Or Rights</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Threatens Health Safety Or Rights</em>' attribute.
	 * @see #isThreatensHealthSafetyOrRights()
	 * @generated
	 */
	void setThreatensHealthSafetyOrRights(boolean value);

	/**
	 * Returns the value of the '<em><b>Is Unacceptable Risk Article5</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Does the system employ any of the prohibited practices listed in Article 5 (e.g., social scoring)?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Is Unacceptable Risk Article5</em>' attribute.
	 * @see #setIsUnacceptableRiskArticle5(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIPackage#getEUAIActPolicyCheck_IsUnacceptableRiskArticle5()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isIsUnacceptableRiskArticle5();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.euaiact.EUAIActPolicyCheck#isIsUnacceptableRiskArticle5 <em>Is Unacceptable Risk Article5</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Unacceptable Risk Article5</em>' attribute.
	 * @see #isIsUnacceptableRiskArticle5()
	 * @generated
	 */
	void setIsUnacceptableRiskArticle5(boolean value);

} // EUAIActPolicyCheck
