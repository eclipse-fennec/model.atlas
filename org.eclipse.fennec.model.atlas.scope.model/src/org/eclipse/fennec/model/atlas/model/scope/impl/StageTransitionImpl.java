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
package org.eclipse.fennec.model.atlas.model.scope.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.model.scope.ScopePackage;
import org.eclipse.fennec.model.atlas.model.scope.StageTransition;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Stage Transition</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.impl.StageTransitionImpl#getObjectId <em>Object Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.impl.StageTransitionImpl#getTargetStage <em>Target Stage</em>}</li>
 * </ul>
 *
 * @generated
 */
public class StageTransitionImpl extends MinimalEObjectImpl.Container implements StageTransition {
	/**
	 * The default value of the '{@link #getObjectId() <em>Object Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObjectId()
	 * @generated
	 * @ordered
	 */
	protected static final String OBJECT_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObjectId() <em>Object Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObjectId()
	 * @generated
	 * @ordered
	 */
	protected String objectId = OBJECT_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getTargetStage() <em>Target Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetStage()
	 * @generated
	 * @ordered
	 */
	protected static final String TARGET_STAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTargetStage() <em>Target Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetStage()
	 * @generated
	 * @ordered
	 */
	protected String targetStage = TARGET_STAGE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected StageTransitionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ScopePackage.Literals.STAGE_TRANSITION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObjectId() {
		return objectId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObjectId(String newObjectId) {
		String oldObjectId = objectId;
		objectId = newObjectId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScopePackage.STAGE_TRANSITION__OBJECT_ID, oldObjectId, objectId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTargetStage() {
		return targetStage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTargetStage(String newTargetStage) {
		String oldTargetStage = targetStage;
		targetStage = newTargetStage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScopePackage.STAGE_TRANSITION__TARGET_STAGE, oldTargetStage, targetStage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ScopePackage.STAGE_TRANSITION__OBJECT_ID:
				return getObjectId();
			case ScopePackage.STAGE_TRANSITION__TARGET_STAGE:
				return getTargetStage();
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
			case ScopePackage.STAGE_TRANSITION__OBJECT_ID:
				setObjectId((String)newValue);
				return;
			case ScopePackage.STAGE_TRANSITION__TARGET_STAGE:
				setTargetStage((String)newValue);
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
			case ScopePackage.STAGE_TRANSITION__OBJECT_ID:
				setObjectId(OBJECT_ID_EDEFAULT);
				return;
			case ScopePackage.STAGE_TRANSITION__TARGET_STAGE:
				setTargetStage(TARGET_STAGE_EDEFAULT);
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
			case ScopePackage.STAGE_TRANSITION__OBJECT_ID:
				return OBJECT_ID_EDEFAULT == null ? objectId != null : !OBJECT_ID_EDEFAULT.equals(objectId);
			case ScopePackage.STAGE_TRANSITION__TARGET_STAGE:
				return TARGET_STAGE_EDEFAULT == null ? targetStage != null : !TARGET_STAGE_EDEFAULT.equals(targetStage);
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
		result.append(" (objectId: ");
		result.append(objectId);
		result.append(", targetStage: ");
		result.append(targetStage);
		result.append(')');
		return result.toString();
	}

} //StageTransitionImpl
