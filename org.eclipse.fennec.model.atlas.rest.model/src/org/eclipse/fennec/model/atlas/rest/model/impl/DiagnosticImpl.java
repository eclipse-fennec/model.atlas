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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.model.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.rest.model.Diagnostic;
import org.eclipse.fennec.model.atlas.rest.model.DiagnosticType;
import org.eclipse.fennec.model.atlas.rest.model.RestPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Diagnostic</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.DiagnosticImpl#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.DiagnosticImpl#getSource <em>Source</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.DiagnosticImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.DiagnosticImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.DiagnosticImpl#getExceptionMsg <em>Exception Msg</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.DiagnosticImpl#getData <em>Data</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DiagnosticImpl extends MinimalEObjectImpl.Container implements Diagnostic {
	/**
	 * The default value of the '{@link #getMessage() <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMessage()
	 * @generated
	 * @ordered
	 */
	protected static final String MESSAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMessage() <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMessage()
	 * @generated
	 * @ordered
	 */
	protected String message = MESSAGE_EDEFAULT;

	/**
	 * The default value of the '{@link #getSource() <em>Source</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSource()
	 * @generated
	 * @ordered
	 */
	protected static final String SOURCE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSource() <em>Source</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSource()
	 * @generated
	 * @ordered
	 */
	protected String source = SOURCE_EDEFAULT;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final DiagnosticType TYPE_EDEFAULT = DiagnosticType.OK;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected DiagnosticType type = TYPE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<Diagnostic> children;

	/**
	 * The default value of the '{@link #getExceptionMsg() <em>Exception Msg</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExceptionMsg()
	 * @generated
	 * @ordered
	 */
	protected static final String EXCEPTION_MSG_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getExceptionMsg() <em>Exception Msg</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExceptionMsg()
	 * @generated
	 * @ordered
	 */
	protected String exceptionMsg = EXCEPTION_MSG_EDEFAULT;

	/**
	 * The cached value of the '{@link #getData() <em>Data</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getData()
	 * @generated
	 * @ordered
	 */
	protected EList<String> data;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DiagnosticImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RestPackage.Literals.DIAGNOSTIC;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMessage(String newMessage) {
		String oldMessage = message;
		message = newMessage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.DIAGNOSTIC__MESSAGE, oldMessage, message));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSource() {
		return source;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSource(String newSource) {
		String oldSource = source;
		source = newSource;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.DIAGNOSTIC__SOURCE, oldSource, source));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DiagnosticType getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setType(DiagnosticType newType) {
		DiagnosticType oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.DIAGNOSTIC__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Diagnostic> getChildren() {
		if (children == null) {
			children = new EObjectContainmentEList<Diagnostic>(Diagnostic.class, this, RestPackage.DIAGNOSTIC__CHILDREN);
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getExceptionMsg() {
		return exceptionMsg;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExceptionMsg(String newExceptionMsg) {
		String oldExceptionMsg = exceptionMsg;
		exceptionMsg = newExceptionMsg;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.DIAGNOSTIC__EXCEPTION_MSG, oldExceptionMsg, exceptionMsg));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getData() {
		if (data == null) {
			data = new EDataTypeUniqueEList<String>(String.class, this, RestPackage.DIAGNOSTIC__DATA);
		}
		return data;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RestPackage.DIAGNOSTIC__CHILDREN:
				return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
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
			case RestPackage.DIAGNOSTIC__MESSAGE:
				return getMessage();
			case RestPackage.DIAGNOSTIC__SOURCE:
				return getSource();
			case RestPackage.DIAGNOSTIC__TYPE:
				return getType();
			case RestPackage.DIAGNOSTIC__CHILDREN:
				return getChildren();
			case RestPackage.DIAGNOSTIC__EXCEPTION_MSG:
				return getExceptionMsg();
			case RestPackage.DIAGNOSTIC__DATA:
				return getData();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case RestPackage.DIAGNOSTIC__MESSAGE:
				setMessage((String)newValue);
				return;
			case RestPackage.DIAGNOSTIC__SOURCE:
				setSource((String)newValue);
				return;
			case RestPackage.DIAGNOSTIC__TYPE:
				setType((DiagnosticType)newValue);
				return;
			case RestPackage.DIAGNOSTIC__CHILDREN:
				getChildren().clear();
				getChildren().addAll((Collection<? extends Diagnostic>)newValue);
				return;
			case RestPackage.DIAGNOSTIC__EXCEPTION_MSG:
				setExceptionMsg((String)newValue);
				return;
			case RestPackage.DIAGNOSTIC__DATA:
				getData().clear();
				getData().addAll((Collection<? extends String>)newValue);
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
			case RestPackage.DIAGNOSTIC__MESSAGE:
				setMessage(MESSAGE_EDEFAULT);
				return;
			case RestPackage.DIAGNOSTIC__SOURCE:
				setSource(SOURCE_EDEFAULT);
				return;
			case RestPackage.DIAGNOSTIC__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case RestPackage.DIAGNOSTIC__CHILDREN:
				getChildren().clear();
				return;
			case RestPackage.DIAGNOSTIC__EXCEPTION_MSG:
				setExceptionMsg(EXCEPTION_MSG_EDEFAULT);
				return;
			case RestPackage.DIAGNOSTIC__DATA:
				getData().clear();
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
			case RestPackage.DIAGNOSTIC__MESSAGE:
				return MESSAGE_EDEFAULT == null ? message != null : !MESSAGE_EDEFAULT.equals(message);
			case RestPackage.DIAGNOSTIC__SOURCE:
				return SOURCE_EDEFAULT == null ? source != null : !SOURCE_EDEFAULT.equals(source);
			case RestPackage.DIAGNOSTIC__TYPE:
				return type != TYPE_EDEFAULT;
			case RestPackage.DIAGNOSTIC__CHILDREN:
				return children != null && !children.isEmpty();
			case RestPackage.DIAGNOSTIC__EXCEPTION_MSG:
				return EXCEPTION_MSG_EDEFAULT == null ? exceptionMsg != null : !EXCEPTION_MSG_EDEFAULT.equals(exceptionMsg);
			case RestPackage.DIAGNOSTIC__DATA:
				return data != null && !data.isEmpty();
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
		result.append(" (message: ");
		result.append(message);
		result.append(", source: ");
		result.append(source);
		result.append(", type: ");
		result.append(type);
		result.append(", exceptionMsg: ");
		result.append(exceptionMsg);
		result.append(", data: ");
		result.append(data);
		result.append(')');
		return result.toString();
	}

} //DiagnosticImpl
