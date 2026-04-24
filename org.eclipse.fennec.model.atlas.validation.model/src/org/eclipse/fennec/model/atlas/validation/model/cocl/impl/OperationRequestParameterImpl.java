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
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Operation Request Parameter</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationRequestParameterImpl#getParameter <em>Parameter</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationRequestParameterImpl#isIsNull <em>Is Null</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationRequestParameterImpl#getJavaValue <em>Java Value</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationRequestParameterImpl#getEValue <em>EValue</em>}</li>
 * </ul>
 *
 * @generated
 */
public class OperationRequestParameterImpl extends MinimalEObjectImpl.Container implements OperationRequestParameter {
	/**
	 * The cached value of the '{@link #getParameter() <em>Parameter</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParameter()
	 * @generated
	 * @ordered
	 */
	protected EParameter parameter;

	/**
	 * The default value of the '{@link #isIsNull() <em>Is Null</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsNull()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IS_NULL_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIsNull() <em>Is Null</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsNull()
	 * @generated
	 * @ordered
	 */
	protected boolean isNull = IS_NULL_EDEFAULT;

	/**
	 * The default value of the '{@link #getJavaValue() <em>Java Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJavaValue()
	 * @generated
	 * @ordered
	 */
	protected static final String JAVA_VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getJavaValue() <em>Java Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJavaValue()
	 * @generated
	 * @ordered
	 */
	protected String javaValue = JAVA_VALUE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getEValue() <em>EValue</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEValue()
	 * @generated
	 * @ordered
	 */
	protected EObject eValue;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OperationRequestParameterImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return COCLPackage.Literals.OPERATION_REQUEST_PARAMETER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EParameter getParameter() {
		if (parameter != null && parameter.eIsProxy()) {
			InternalEObject oldParameter = (InternalEObject)parameter;
			parameter = (EParameter)eResolveProxy(oldParameter);
			if (parameter != oldParameter) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, COCLPackage.OPERATION_REQUEST_PARAMETER__PARAMETER, oldParameter, parameter));
			}
		}
		return parameter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EParameter basicGetParameter() {
		return parameter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setParameter(EParameter newParameter) {
		EParameter oldParameter = parameter;
		parameter = newParameter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OPERATION_REQUEST_PARAMETER__PARAMETER, oldParameter, parameter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIsNull() {
		return isNull;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIsNull(boolean newIsNull) {
		boolean oldIsNull = isNull;
		isNull = newIsNull;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OPERATION_REQUEST_PARAMETER__IS_NULL, oldIsNull, isNull));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getJavaValue() {
		return javaValue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setJavaValue(String newJavaValue) {
		String oldJavaValue = javaValue;
		javaValue = newJavaValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OPERATION_REQUEST_PARAMETER__JAVA_VALUE, oldJavaValue, javaValue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject getEValue() {
		return eValue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetEValue(EObject newEValue, NotificationChain msgs) {
		EObject oldEValue = eValue;
		eValue = newEValue;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, COCLPackage.OPERATION_REQUEST_PARAMETER__EVALUE, oldEValue, newEValue);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEValue(EObject newEValue) {
		if (newEValue != eValue) {
			NotificationChain msgs = null;
			if (eValue != null)
				msgs = ((InternalEObject)eValue).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - COCLPackage.OPERATION_REQUEST_PARAMETER__EVALUE, null, msgs);
			if (newEValue != null)
				msgs = ((InternalEObject)newEValue).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - COCLPackage.OPERATION_REQUEST_PARAMETER__EVALUE, null, msgs);
			msgs = basicSetEValue(newEValue, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OPERATION_REQUEST_PARAMETER__EVALUE, newEValue, newEValue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case COCLPackage.OPERATION_REQUEST_PARAMETER__EVALUE:
				return basicSetEValue(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case COCLPackage.OPERATION_REQUEST_PARAMETER__PARAMETER:
				if (resolve) return getParameter();
				return basicGetParameter();
			case COCLPackage.OPERATION_REQUEST_PARAMETER__IS_NULL:
				return isIsNull();
			case COCLPackage.OPERATION_REQUEST_PARAMETER__JAVA_VALUE:
				return getJavaValue();
			case COCLPackage.OPERATION_REQUEST_PARAMETER__EVALUE:
				return getEValue();
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
			case COCLPackage.OPERATION_REQUEST_PARAMETER__PARAMETER:
				setParameter((EParameter)newValue);
				return;
			case COCLPackage.OPERATION_REQUEST_PARAMETER__IS_NULL:
				setIsNull((Boolean)newValue);
				return;
			case COCLPackage.OPERATION_REQUEST_PARAMETER__JAVA_VALUE:
				setJavaValue((String)newValue);
				return;
			case COCLPackage.OPERATION_REQUEST_PARAMETER__EVALUE:
				setEValue((EObject)newValue);
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
			case COCLPackage.OPERATION_REQUEST_PARAMETER__PARAMETER:
				setParameter((EParameter)null);
				return;
			case COCLPackage.OPERATION_REQUEST_PARAMETER__IS_NULL:
				setIsNull(IS_NULL_EDEFAULT);
				return;
			case COCLPackage.OPERATION_REQUEST_PARAMETER__JAVA_VALUE:
				setJavaValue(JAVA_VALUE_EDEFAULT);
				return;
			case COCLPackage.OPERATION_REQUEST_PARAMETER__EVALUE:
				setEValue((EObject)null);
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
			case COCLPackage.OPERATION_REQUEST_PARAMETER__PARAMETER:
				return parameter != null;
			case COCLPackage.OPERATION_REQUEST_PARAMETER__IS_NULL:
				return isNull != IS_NULL_EDEFAULT;
			case COCLPackage.OPERATION_REQUEST_PARAMETER__JAVA_VALUE:
				return JAVA_VALUE_EDEFAULT == null ? javaValue != null : !JAVA_VALUE_EDEFAULT.equals(javaValue);
			case COCLPackage.OPERATION_REQUEST_PARAMETER__EVALUE:
				return eValue != null;
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
		result.append(" (isNull: ");
		result.append(isNull);
		result.append(", javaValue: ");
		result.append(javaValue);
		result.append(')');
		return result.toString();
	}

} //OperationRequestParameterImpl
