/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 */
package org.eclipse.fennec.model.atlas.validation.model.cocl.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage;
import org.eclipse.fennec.model.atlas.validation.model.cocl.SimpleValidationResult;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Simple Validation Result</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.SimpleValidationResultImpl#getValue <em>Value</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.SimpleValidationResultImpl#getValueJavaClassName <em>Value Java Class Name</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SimpleValidationResultImpl extends ValidationResultImpl implements SimpleValidationResult {
	/**
	 * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected static final String VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected String value = VALUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getValueJavaClassName() <em>Value Java Class Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueJavaClassName()
	 * @generated
	 * @ordered
	 */
	protected static final String VALUE_JAVA_CLASS_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValueJavaClassName() <em>Value Java Class Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValueJavaClassName()
	 * @generated
	 * @ordered
	 */
	protected String valueJavaClassName = VALUE_JAVA_CLASS_NAME_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SimpleValidationResultImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return COCLPackage.Literals.SIMPLE_VALIDATION_RESULT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setValue(String newValue) {
		String oldValue = value;
		value = newValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.SIMPLE_VALIDATION_RESULT__VALUE, oldValue, value));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getValueJavaClassName() {
		return valueJavaClassName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setValueJavaClassName(String newValueJavaClassName) {
		String oldValueJavaClassName = valueJavaClassName;
		valueJavaClassName = newValueJavaClassName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.SIMPLE_VALIDATION_RESULT__VALUE_JAVA_CLASS_NAME, oldValueJavaClassName, valueJavaClassName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case COCLPackage.SIMPLE_VALIDATION_RESULT__VALUE:
				return getValue();
			case COCLPackage.SIMPLE_VALIDATION_RESULT__VALUE_JAVA_CLASS_NAME:
				return getValueJavaClassName();
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
			case COCLPackage.SIMPLE_VALIDATION_RESULT__VALUE:
				setValue((String)newValue);
				return;
			case COCLPackage.SIMPLE_VALIDATION_RESULT__VALUE_JAVA_CLASS_NAME:
				setValueJavaClassName((String)newValue);
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
			case COCLPackage.SIMPLE_VALIDATION_RESULT__VALUE:
				setValue(VALUE_EDEFAULT);
				return;
			case COCLPackage.SIMPLE_VALIDATION_RESULT__VALUE_JAVA_CLASS_NAME:
				setValueJavaClassName(VALUE_JAVA_CLASS_NAME_EDEFAULT);
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
			case COCLPackage.SIMPLE_VALIDATION_RESULT__VALUE:
				return VALUE_EDEFAULT == null ? value != null : !VALUE_EDEFAULT.equals(value);
			case COCLPackage.SIMPLE_VALIDATION_RESULT__VALUE_JAVA_CLASS_NAME:
				return VALUE_JAVA_CLASS_NAME_EDEFAULT == null ? valueJavaClassName != null : !VALUE_JAVA_CLASS_NAME_EDEFAULT.equals(valueJavaClassName);
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
		result.append(" (value: ");
		result.append(value);
		result.append(", valueJavaClassName: ");
		result.append(valueJavaClassName);
		result.append(')');
		return result.toString();
	}

} //SimpleValidationResultImpl
