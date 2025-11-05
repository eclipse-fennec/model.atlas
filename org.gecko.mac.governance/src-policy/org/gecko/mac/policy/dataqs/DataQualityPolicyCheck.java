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
package org.gecko.mac.policy.dataqs;

import org.gecko.mac.governance.ComplianceCheckResult;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Data Quality Policy Check</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A specialized compliance check that represents the verification against predefined data quality metrics.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.policy.dataqs.DataQualityPolicyCheck#isSchemaConformanceVerified <em>Schema Conformance Verified</em>}</li>
 *   <li>{@link org.gecko.mac.policy.dataqs.DataQualityPolicyCheck#isValueRangeValidated <em>Value Range Validated</em>}</li>
 *   <li>{@link org.gecko.mac.policy.dataqs.DataQualityPolicyCheck#isCompletenessChecked <em>Completeness Checked</em>}</li>
 *   <li>{@link org.gecko.mac.policy.dataqs.DataQualityPolicyCheck#isFormatValidationPassed <em>Format Validation Passed</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.policy.dataqs.DataQSPackage#getDataQualityPolicyCheck()
 * @model
 * @generated
 */
@ProviderType
public interface DataQualityPolicyCheck extends ComplianceCheckResult {
	/**
	 * Returns the value of the '<em><b>Schema Conformance Verified</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Does the data instance conform to the defined schema (correct attribute names, data types)?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Schema Conformance Verified</em>' attribute.
	 * @see #setSchemaConformanceVerified(boolean)
	 * @see org.gecko.mac.policy.dataqs.DataQSPackage#getDataQualityPolicyCheck_SchemaConformanceVerified()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isSchemaConformanceVerified();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.dataqs.DataQualityPolicyCheck#isSchemaConformanceVerified <em>Schema Conformance Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Schema Conformance Verified</em>' attribute.
	 * @see #isSchemaConformanceVerified()
	 * @generated
	 */
	void setSchemaConformanceVerified(boolean value);

	/**
	 * Returns the value of the '<em><b>Value Range Validated</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Are all numeric values within their predefined, plausible ranges?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Value Range Validated</em>' attribute.
	 * @see #setValueRangeValidated(boolean)
	 * @see org.gecko.mac.policy.dataqs.DataQSPackage#getDataQualityPolicyCheck_ValueRangeValidated()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isValueRangeValidated();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.dataqs.DataQualityPolicyCheck#isValueRangeValidated <em>Value Range Validated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value Range Validated</em>' attribute.
	 * @see #isValueRangeValidated()
	 * @generated
	 */
	void setValueRangeValidated(boolean value);

	/**
	 * Returns the value of the '<em><b>Completeness Checked</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Are all mandatory fields populated? (No null values where not allowed).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Completeness Checked</em>' attribute.
	 * @see #setCompletenessChecked(boolean)
	 * @see org.gecko.mac.policy.dataqs.DataQSPackage#getDataQualityPolicyCheck_CompletenessChecked()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isCompletenessChecked();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.dataqs.DataQualityPolicyCheck#isCompletenessChecked <em>Completeness Checked</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Completeness Checked</em>' attribute.
	 * @see #isCompletenessChecked()
	 * @generated
	 */
	void setCompletenessChecked(boolean value);

	/**
	 * Returns the value of the '<em><b>Format Validation Passed</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Do values with specific formats (e.g., timestamps, identifiers) adhere to the required syntax?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Format Validation Passed</em>' attribute.
	 * @see #setFormatValidationPassed(boolean)
	 * @see org.gecko.mac.policy.dataqs.DataQSPackage#getDataQualityPolicyCheck_FormatValidationPassed()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isFormatValidationPassed();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.dataqs.DataQualityPolicyCheck#isFormatValidationPassed <em>Format Validation Passed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Format Validation Passed</em>' attribute.
	 * @see #isFormatValidationPassed()
	 * @generated
	 */
	void setFormatValidationPassed(boolean value);

} // DataQualityPolicyCheck
