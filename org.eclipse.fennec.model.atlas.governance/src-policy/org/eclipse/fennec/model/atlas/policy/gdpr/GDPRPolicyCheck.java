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
package org.eclipse.fennec.model.atlas.policy.gdpr;

import org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Policy Check</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A specialized compliance check that represents the verification against relevant articles of the General Data Protection Regulation (GDPR).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPolicyCheck#isContainsPersonalData <em>Contains Personal Data</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPolicyCheck#getLegalBasisForProcessing <em>Legal Basis For Processing</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPolicyCheck#isDataMinimizationApplied <em>Data Minimization Applied</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPolicyCheck#isPurposeLimitationMet <em>Purpose Limitation Met</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPackage#getGDPRPolicyCheck()
 * @model
 * @generated
 */
@ProviderType
public interface GDPRPolicyCheck extends ComplianceCheckResult {
	/**
	 * Returns the value of the '<em><b>Contains Personal Data</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Does the data model contain attributes that are considered Personal Identifiable Information (PII) under GDPR?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Contains Personal Data</em>' attribute.
	 * @see #setContainsPersonalData(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPackage#getGDPRPolicyCheck_ContainsPersonalData()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isContainsPersonalData();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPolicyCheck#isContainsPersonalData <em>Contains Personal Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contains Personal Data</em>' attribute.
	 * @see #isContainsPersonalData()
	 * @generated
	 */
	void setContainsPersonalData(boolean value);

	/**
	 * Returns the value of the '<em><b>Legal Basis For Processing</b></em>' attribute.
	 * The default value is <code>"NOT_APPLICABLE"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.policy.gdpr.LegalBasis}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Art. 6 GDPR): What is the legal basis for processing the personal data? This is only applicable if personal data is present.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Legal Basis For Processing</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.policy.gdpr.LegalBasis
	 * @see #setLegalBasisForProcessing(LegalBasis)
	 * @see org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPackage#getGDPRPolicyCheck_LegalBasisForProcessing()
	 * @model default="NOT_APPLICABLE" required="true"
	 * @generated
	 */
	LegalBasis getLegalBasisForProcessing();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPolicyCheck#getLegalBasisForProcessing <em>Legal Basis For Processing</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Legal Basis For Processing</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.policy.gdpr.LegalBasis
	 * @see #getLegalBasisForProcessing()
	 * @generated
	 */
	void setLegalBasisForProcessing(LegalBasis value);

	/**
	 * Returns the value of the '<em><b>Data Minimization Applied</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Art. 5(1)(c) GDPR): Is the collected data limited to what is necessary in relation to the purposes for which it is processed?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Data Minimization Applied</em>' attribute.
	 * @see #setDataMinimizationApplied(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPackage#getGDPRPolicyCheck_DataMinimizationApplied()
	 * @model default="true" required="true"
	 * @generated
	 */
	boolean isDataMinimizationApplied();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPolicyCheck#isDataMinimizationApplied <em>Data Minimization Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data Minimization Applied</em>' attribute.
	 * @see #isDataMinimizationApplied()
	 * @generated
	 */
	void setDataMinimizationApplied(boolean value);

	/**
	 * Returns the value of the '<em><b>Purpose Limitation Met</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Art. 5(1)(b) GDPR): Is the data collected for specified, explicit and legitimate purposes and not further processed in a manner that is incompatible with those purposes?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Purpose Limitation Met</em>' attribute.
	 * @see #setPurposeLimitationMet(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPackage#getGDPRPolicyCheck_PurposeLimitationMet()
	 * @model default="true" required="true"
	 * @generated
	 */
	boolean isPurposeLimitationMet();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPolicyCheck#isPurposeLimitationMet <em>Purpose Limitation Met</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Purpose Limitation Met</em>' attribute.
	 * @see #isPurposeLimitationMet()
	 * @generated
	 */
	void setPurposeLimitationMet(boolean value);

} // GDPRPolicyCheck
