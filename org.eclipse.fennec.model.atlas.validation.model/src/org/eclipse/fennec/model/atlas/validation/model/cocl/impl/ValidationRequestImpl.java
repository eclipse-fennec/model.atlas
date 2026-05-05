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

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Validation Request</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationRequestImpl#getRole <em>Role</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationRequestImpl#getValidationObjects <em>Validation Objects</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationRequestImpl#getCoclId <em>Cocl Id</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class ValidationRequestImpl extends MinimalEObjectImpl.Container implements ValidationRequest {
	/**
	 * The default value of the '{@link #getRole() <em>Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRole()
	 * @generated
	 * @ordered
	 */
	protected static final OclRole ROLE_EDEFAULT = OclRole.VALIDATION;

	/**
	 * The cached value of the '{@link #getRole() <em>Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRole()
	 * @generated
	 * @ordered
	 */
	protected OclRole role = ROLE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getValidationObjects() <em>Validation Objects</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValidationObjects()
	 * @generated
	 * @ordered
	 */
	protected EList<EObject> validationObjects;

	/**
	 * The default value of the '{@link #getCoclId() <em>Cocl Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCoclId()
	 * @generated
	 * @ordered
	 */
	protected static final String COCL_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCoclId() <em>Cocl Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCoclId()
	 * @generated
	 * @ordered
	 */
	protected String coclId = COCL_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ValidationRequestImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return COCLPackage.Literals.VALIDATION_REQUEST;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OclRole getRole() {
		return role;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRole(OclRole newRole) {
		OclRole oldRole = role;
		role = newRole == null ? ROLE_EDEFAULT : newRole;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.VALIDATION_REQUEST__ROLE, oldRole, role));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EObject> getValidationObjects() {
		if (validationObjects == null) {
			validationObjects = new EObjectContainmentEList<EObject>(EObject.class, this, COCLPackage.VALIDATION_REQUEST__VALIDATION_OBJECTS);
		}
		return validationObjects;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCoclId() {
		return coclId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCoclId(String newCoclId) {
		String oldCoclId = coclId;
		coclId = newCoclId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.VALIDATION_REQUEST__COCL_ID, oldCoclId, coclId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case COCLPackage.VALIDATION_REQUEST__VALIDATION_OBJECTS:
				return ((InternalEList<?>)getValidationObjects()).basicRemove(otherEnd, msgs);
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
			case COCLPackage.VALIDATION_REQUEST__ROLE:
				return getRole();
			case COCLPackage.VALIDATION_REQUEST__VALIDATION_OBJECTS:
				return getValidationObjects();
			case COCLPackage.VALIDATION_REQUEST__COCL_ID:
				return getCoclId();
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
			case COCLPackage.VALIDATION_REQUEST__ROLE:
				setRole((OclRole)newValue);
				return;
			case COCLPackage.VALIDATION_REQUEST__VALIDATION_OBJECTS:
				getValidationObjects().clear();
				getValidationObjects().addAll((Collection<? extends EObject>)newValue);
				return;
			case COCLPackage.VALIDATION_REQUEST__COCL_ID:
				setCoclId((String)newValue);
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
			case COCLPackage.VALIDATION_REQUEST__ROLE:
				setRole(ROLE_EDEFAULT);
				return;
			case COCLPackage.VALIDATION_REQUEST__VALIDATION_OBJECTS:
				getValidationObjects().clear();
				return;
			case COCLPackage.VALIDATION_REQUEST__COCL_ID:
				setCoclId(COCL_ID_EDEFAULT);
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
			case COCLPackage.VALIDATION_REQUEST__ROLE:
				return role != ROLE_EDEFAULT;
			case COCLPackage.VALIDATION_REQUEST__VALIDATION_OBJECTS:
				return validationObjects != null && !validationObjects.isEmpty();
			case COCLPackage.VALIDATION_REQUEST__COCL_ID:
				return COCL_ID_EDEFAULT == null ? coclId != null : !COCL_ID_EDEFAULT.equals(coclId);
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
		result.append(" (role: ");
		result.append(role);
		result.append(", coclId: ");
		result.append(coclId);
		result.append(')');
		return result.toString();
	}

} //ValidationRequestImpl
