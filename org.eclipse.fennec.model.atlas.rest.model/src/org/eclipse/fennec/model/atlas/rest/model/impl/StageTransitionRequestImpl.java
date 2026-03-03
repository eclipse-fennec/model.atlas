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

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.rest.model.RestPackage;
import org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Stage Transition Request</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.StageTransitionRequestImpl#getObjectId <em>Object Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.StageTransitionRequestImpl#getTargetStage <em>Target Stage</em>}</li>
 * </ul>
 *
 * @generated
 */
public class StageTransitionRequestImpl extends MinimalEObjectImpl.Container implements StageTransitionRequest {
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
	protected StageTransitionRequestImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RestPackage.Literals.STAGE_TRANSITION_REQUEST;
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
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.STAGE_TRANSITION_REQUEST__OBJECT_ID, oldObjectId, objectId));
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
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.STAGE_TRANSITION_REQUEST__TARGET_STAGE, oldTargetStage, targetStage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RestPackage.STAGE_TRANSITION_REQUEST__OBJECT_ID:
				return getObjectId();
			case RestPackage.STAGE_TRANSITION_REQUEST__TARGET_STAGE:
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
			case RestPackage.STAGE_TRANSITION_REQUEST__OBJECT_ID:
				setObjectId((String)newValue);
				return;
			case RestPackage.STAGE_TRANSITION_REQUEST__TARGET_STAGE:
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
			case RestPackage.STAGE_TRANSITION_REQUEST__OBJECT_ID:
				setObjectId(OBJECT_ID_EDEFAULT);
				return;
			case RestPackage.STAGE_TRANSITION_REQUEST__TARGET_STAGE:
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
			case RestPackage.STAGE_TRANSITION_REQUEST__OBJECT_ID:
				return OBJECT_ID_EDEFAULT == null ? objectId != null : !OBJECT_ID_EDEFAULT.equals(objectId);
			case RestPackage.STAGE_TRANSITION_REQUEST__TARGET_STAGE:
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

} //StageTransitionRequestImpl
