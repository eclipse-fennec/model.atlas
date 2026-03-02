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

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.rest.model.EPackageInfo;
import org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse;
import org.eclipse.fennec.model.atlas.rest.model.RestPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>EPackage List Response</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageListResponseImpl#getPackages <em>Packages</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageListResponseImpl#getTotal <em>Total</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageListResponseImpl#getSkip <em>Skip</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageListResponseImpl#getLimit <em>Limit</em>}</li>
 * </ul>
 *
 * @generated
 */
public class EPackageListResponseImpl extends MinimalEObjectImpl.Container implements EPackageListResponse {
	/**
	 * The cached value of the '{@link #getPackages() <em>Packages</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPackages()
	 * @generated
	 * @ordered
	 */
	protected EList<EPackageInfo> packages;

	/**
	 * The default value of the '{@link #getTotal() <em>Total</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTotal()
	 * @generated
	 * @ordered
	 */
	protected static final int TOTAL_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getTotal() <em>Total</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTotal()
	 * @generated
	 * @ordered
	 */
	protected int total = TOTAL_EDEFAULT;

	/**
	 * The default value of the '{@link #getSkip() <em>Skip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSkip()
	 * @generated
	 * @ordered
	 */
	protected static final int SKIP_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getSkip() <em>Skip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSkip()
	 * @generated
	 * @ordered
	 */
	protected int skip = SKIP_EDEFAULT;

	/**
	 * The default value of the '{@link #getLimit() <em>Limit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLimit()
	 * @generated
	 * @ordered
	 */
	protected static final int LIMIT_EDEFAULT = 100;

	/**
	 * The cached value of the '{@link #getLimit() <em>Limit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLimit()
	 * @generated
	 * @ordered
	 */
	protected int limit = LIMIT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EPackageListResponseImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RestPackage.Literals.EPACKAGE_LIST_RESPONSE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EPackageInfo> getPackages() {
		if (packages == null) {
			packages = new EObjectContainmentEList<EPackageInfo>(EPackageInfo.class, this, RestPackage.EPACKAGE_LIST_RESPONSE__PACKAGES);
		}
		return packages;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getTotal() {
		return total;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTotal(int newTotal) {
		int oldTotal = total;
		total = newTotal;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.EPACKAGE_LIST_RESPONSE__TOTAL, oldTotal, total));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getSkip() {
		return skip;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSkip(int newSkip) {
		int oldSkip = skip;
		skip = newSkip;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.EPACKAGE_LIST_RESPONSE__SKIP, oldSkip, skip));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getLimit() {
		return limit;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLimit(int newLimit) {
		int oldLimit = limit;
		limit = newLimit;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.EPACKAGE_LIST_RESPONSE__LIMIT, oldLimit, limit));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RestPackage.EPACKAGE_LIST_RESPONSE__PACKAGES:
				return ((InternalEList<?>)getPackages()).basicRemove(otherEnd, msgs);
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
			case RestPackage.EPACKAGE_LIST_RESPONSE__PACKAGES:
				return getPackages();
			case RestPackage.EPACKAGE_LIST_RESPONSE__TOTAL:
				return getTotal();
			case RestPackage.EPACKAGE_LIST_RESPONSE__SKIP:
				return getSkip();
			case RestPackage.EPACKAGE_LIST_RESPONSE__LIMIT:
				return getLimit();
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
			case RestPackage.EPACKAGE_LIST_RESPONSE__PACKAGES:
				getPackages().clear();
				getPackages().addAll((Collection<? extends EPackageInfo>)newValue);
				return;
			case RestPackage.EPACKAGE_LIST_RESPONSE__TOTAL:
				setTotal((Integer)newValue);
				return;
			case RestPackage.EPACKAGE_LIST_RESPONSE__SKIP:
				setSkip((Integer)newValue);
				return;
			case RestPackage.EPACKAGE_LIST_RESPONSE__LIMIT:
				setLimit((Integer)newValue);
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
			case RestPackage.EPACKAGE_LIST_RESPONSE__PACKAGES:
				getPackages().clear();
				return;
			case RestPackage.EPACKAGE_LIST_RESPONSE__TOTAL:
				setTotal(TOTAL_EDEFAULT);
				return;
			case RestPackage.EPACKAGE_LIST_RESPONSE__SKIP:
				setSkip(SKIP_EDEFAULT);
				return;
			case RestPackage.EPACKAGE_LIST_RESPONSE__LIMIT:
				setLimit(LIMIT_EDEFAULT);
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
			case RestPackage.EPACKAGE_LIST_RESPONSE__PACKAGES:
				return packages != null && !packages.isEmpty();
			case RestPackage.EPACKAGE_LIST_RESPONSE__TOTAL:
				return total != TOTAL_EDEFAULT;
			case RestPackage.EPACKAGE_LIST_RESPONSE__SKIP:
				return skip != SKIP_EDEFAULT;
			case RestPackage.EPACKAGE_LIST_RESPONSE__LIMIT:
				return limit != LIMIT_EDEFAULT;
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
		result.append(" (total: ");
		result.append(total);
		result.append(", skip: ");
		result.append(skip);
		result.append(", limit: ");
		result.append(limit);
		result.append(')');
		return result.toString();
	}

} //EPackageListResponseImpl
