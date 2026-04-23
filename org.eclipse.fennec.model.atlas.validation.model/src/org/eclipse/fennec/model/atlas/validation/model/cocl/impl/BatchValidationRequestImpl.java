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
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Batch Validation Request</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.BatchValidationRequestImpl#getFilterConstraint <em>Filter Constraint</em>}</li>
 * </ul>
 *
 * @generated
 */
public class BatchValidationRequestImpl extends ValidationRequestImpl implements BatchValidationRequest {
	/**
	 * The cached value of the '{@link #getFilterConstraint() <em>Filter Constraint</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFilterConstraint()
	 * @generated
	 * @ordered
	 */
	protected OclConstraint filterConstraint;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BatchValidationRequestImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return COCLPackage.Literals.BATCH_VALIDATION_REQUEST;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OclConstraint getFilterConstraint() {
		return filterConstraint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFilterConstraint(OclConstraint newFilterConstraint, NotificationChain msgs) {
		OclConstraint oldFilterConstraint = filterConstraint;
		filterConstraint = newFilterConstraint;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, COCLPackage.BATCH_VALIDATION_REQUEST__FILTER_CONSTRAINT, oldFilterConstraint, newFilterConstraint);
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
	public void setFilterConstraint(OclConstraint newFilterConstraint) {
		if (newFilterConstraint != filterConstraint) {
			NotificationChain msgs = null;
			if (filterConstraint != null)
				msgs = ((InternalEObject)filterConstraint).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - COCLPackage.BATCH_VALIDATION_REQUEST__FILTER_CONSTRAINT, null, msgs);
			if (newFilterConstraint != null)
				msgs = ((InternalEObject)newFilterConstraint).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - COCLPackage.BATCH_VALIDATION_REQUEST__FILTER_CONSTRAINT, null, msgs);
			msgs = basicSetFilterConstraint(newFilterConstraint, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.BATCH_VALIDATION_REQUEST__FILTER_CONSTRAINT, newFilterConstraint, newFilterConstraint));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case COCLPackage.BATCH_VALIDATION_REQUEST__FILTER_CONSTRAINT:
				return basicSetFilterConstraint(null, msgs);
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
			case COCLPackage.BATCH_VALIDATION_REQUEST__FILTER_CONSTRAINT:
				return getFilterConstraint();
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
			case COCLPackage.BATCH_VALIDATION_REQUEST__FILTER_CONSTRAINT:
				setFilterConstraint((OclConstraint)newValue);
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
			case COCLPackage.BATCH_VALIDATION_REQUEST__FILTER_CONSTRAINT:
				setFilterConstraint((OclConstraint)null);
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
			case COCLPackage.BATCH_VALIDATION_REQUEST__FILTER_CONSTRAINT:
				return filterConstraint != null;
		}
		return super.eIsSet(featureID);
	}

} //BatchValidationRequestImpl
