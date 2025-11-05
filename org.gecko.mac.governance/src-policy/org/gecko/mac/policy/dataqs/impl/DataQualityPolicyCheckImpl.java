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
package org.gecko.mac.policy.dataqs.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.gecko.mac.governance.impl.ComplianceCheckResultImpl;

import org.gecko.mac.policy.dataqs.DataQSPackage;
import org.gecko.mac.policy.dataqs.DataQualityPolicyCheck;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Data Quality Policy Check</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.policy.dataqs.impl.DataQualityPolicyCheckImpl#isSchemaConformanceVerified <em>Schema Conformance Verified</em>}</li>
 *   <li>{@link org.gecko.mac.policy.dataqs.impl.DataQualityPolicyCheckImpl#isValueRangeValidated <em>Value Range Validated</em>}</li>
 *   <li>{@link org.gecko.mac.policy.dataqs.impl.DataQualityPolicyCheckImpl#isCompletenessChecked <em>Completeness Checked</em>}</li>
 *   <li>{@link org.gecko.mac.policy.dataqs.impl.DataQualityPolicyCheckImpl#isFormatValidationPassed <em>Format Validation Passed</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DataQualityPolicyCheckImpl extends ComplianceCheckResultImpl implements DataQualityPolicyCheck {
	/**
	 * The default value of the '{@link #isSchemaConformanceVerified() <em>Schema Conformance Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSchemaConformanceVerified()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SCHEMA_CONFORMANCE_VERIFIED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isSchemaConformanceVerified() <em>Schema Conformance Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSchemaConformanceVerified()
	 * @generated
	 * @ordered
	 */
	protected boolean schemaConformanceVerified = SCHEMA_CONFORMANCE_VERIFIED_EDEFAULT;

	/**
	 * The default value of the '{@link #isValueRangeValidated() <em>Value Range Validated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isValueRangeValidated()
	 * @generated
	 * @ordered
	 */
	protected static final boolean VALUE_RANGE_VALIDATED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isValueRangeValidated() <em>Value Range Validated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isValueRangeValidated()
	 * @generated
	 * @ordered
	 */
	protected boolean valueRangeValidated = VALUE_RANGE_VALIDATED_EDEFAULT;

	/**
	 * The default value of the '{@link #isCompletenessChecked() <em>Completeness Checked</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCompletenessChecked()
	 * @generated
	 * @ordered
	 */
	protected static final boolean COMPLETENESS_CHECKED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isCompletenessChecked() <em>Completeness Checked</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCompletenessChecked()
	 * @generated
	 * @ordered
	 */
	protected boolean completenessChecked = COMPLETENESS_CHECKED_EDEFAULT;

	/**
	 * The default value of the '{@link #isFormatValidationPassed() <em>Format Validation Passed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isFormatValidationPassed()
	 * @generated
	 * @ordered
	 */
	protected static final boolean FORMAT_VALIDATION_PASSED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isFormatValidationPassed() <em>Format Validation Passed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isFormatValidationPassed()
	 * @generated
	 * @ordered
	 */
	protected boolean formatValidationPassed = FORMAT_VALIDATION_PASSED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DataQualityPolicyCheckImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DataQSPackage.Literals.DATA_QUALITY_POLICY_CHECK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSchemaConformanceVerified() {
		return schemaConformanceVerified;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSchemaConformanceVerified(boolean newSchemaConformanceVerified) {
		boolean oldSchemaConformanceVerified = schemaConformanceVerified;
		schemaConformanceVerified = newSchemaConformanceVerified;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataQSPackage.DATA_QUALITY_POLICY_CHECK__SCHEMA_CONFORMANCE_VERIFIED, oldSchemaConformanceVerified, schemaConformanceVerified));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isValueRangeValidated() {
		return valueRangeValidated;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setValueRangeValidated(boolean newValueRangeValidated) {
		boolean oldValueRangeValidated = valueRangeValidated;
		valueRangeValidated = newValueRangeValidated;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataQSPackage.DATA_QUALITY_POLICY_CHECK__VALUE_RANGE_VALIDATED, oldValueRangeValidated, valueRangeValidated));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isCompletenessChecked() {
		return completenessChecked;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCompletenessChecked(boolean newCompletenessChecked) {
		boolean oldCompletenessChecked = completenessChecked;
		completenessChecked = newCompletenessChecked;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataQSPackage.DATA_QUALITY_POLICY_CHECK__COMPLETENESS_CHECKED, oldCompletenessChecked, completenessChecked));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isFormatValidationPassed() {
		return formatValidationPassed;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFormatValidationPassed(boolean newFormatValidationPassed) {
		boolean oldFormatValidationPassed = formatValidationPassed;
		formatValidationPassed = newFormatValidationPassed;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DataQSPackage.DATA_QUALITY_POLICY_CHECK__FORMAT_VALIDATION_PASSED, oldFormatValidationPassed, formatValidationPassed));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__SCHEMA_CONFORMANCE_VERIFIED:
				return isSchemaConformanceVerified();
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__VALUE_RANGE_VALIDATED:
				return isValueRangeValidated();
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__COMPLETENESS_CHECKED:
				return isCompletenessChecked();
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__FORMAT_VALIDATION_PASSED:
				return isFormatValidationPassed();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__SCHEMA_CONFORMANCE_VERIFIED:
				setSchemaConformanceVerified((Boolean)newValue);
				return;
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__VALUE_RANGE_VALIDATED:
				setValueRangeValidated((Boolean)newValue);
				return;
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__COMPLETENESS_CHECKED:
				setCompletenessChecked((Boolean)newValue);
				return;
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__FORMAT_VALIDATION_PASSED:
				setFormatValidationPassed((Boolean)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__SCHEMA_CONFORMANCE_VERIFIED:
				setSchemaConformanceVerified(SCHEMA_CONFORMANCE_VERIFIED_EDEFAULT);
				return;
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__VALUE_RANGE_VALIDATED:
				setValueRangeValidated(VALUE_RANGE_VALIDATED_EDEFAULT);
				return;
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__COMPLETENESS_CHECKED:
				setCompletenessChecked(COMPLETENESS_CHECKED_EDEFAULT);
				return;
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__FORMAT_VALIDATION_PASSED:
				setFormatValidationPassed(FORMAT_VALIDATION_PASSED_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__SCHEMA_CONFORMANCE_VERIFIED:
				return schemaConformanceVerified != SCHEMA_CONFORMANCE_VERIFIED_EDEFAULT;
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__VALUE_RANGE_VALIDATED:
				return valueRangeValidated != VALUE_RANGE_VALIDATED_EDEFAULT;
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__COMPLETENESS_CHECKED:
				return completenessChecked != COMPLETENESS_CHECKED_EDEFAULT;
			case DataQSPackage.DATA_QUALITY_POLICY_CHECK__FORMAT_VALIDATION_PASSED:
				return formatValidationPassed != FORMAT_VALIDATION_PASSED_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (schemaConformanceVerified: ");
		result.append(schemaConformanceVerified);
		result.append(", valueRangeValidated: ");
		result.append(valueRangeValidated);
		result.append(", completenessChecked: ");
		result.append(completenessChecked);
		result.append(", formatValidationPassed: ");
		result.append(formatValidationPassed);
		result.append(')');
		return result.toString();
	}

} //DataQualityPolicyCheckImpl
