/*
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.api.impl;

import java.lang.reflect.InvocationTargetException;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.BasicInternalEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.registry.api.BatchItemResult;
import org.eclipse.fennec.model.atlas.registry.api.BatchResult;
import org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Batch Result</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.BatchResultImpl#isApplied <em>Applied</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.BatchResultImpl#getItems <em>Items</em>}</li>
 * </ul>
 *
 * @generated
 */
public class BatchResultImpl extends MinimalEObjectImpl.Container implements BatchResult {
	/**
	 * The default value of the '{@link #isApplied() <em>Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isApplied()
	 * @generated
	 * @ordered
	 */
	protected static final boolean APPLIED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isApplied() <em>Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isApplied()
	 * @generated
	 * @ordered
	 */
	protected boolean applied = APPLIED_EDEFAULT;

	/**
	 * The cached value of the '{@link #getItems() <em>Items</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getItems()
	 * @generated
	 * @ordered
	 */
	protected EList<BatchItemResult> items;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BatchResultImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegistryApiPackage.Literals.BATCH_RESULT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isApplied() {
		return applied;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setApplied(boolean newApplied) {
		applied = newApplied;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<BatchItemResult> getItems() {
		if (items == null) {
			items = new BasicInternalEList<BatchItemResult>(BatchItemResult.class);
		}
		return items;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BatchItemResult firstRefusal() {
		return getItems().stream().filter(BatchItemResult::isRefused).findFirst().orElse(null);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RegistryApiPackage.BATCH_RESULT__ITEMS:
				return ((InternalEList<?>)getItems()).basicRemove(otherEnd, msgs);
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
			case RegistryApiPackage.BATCH_RESULT__APPLIED:
				return isApplied();
			case RegistryApiPackage.BATCH_RESULT__ITEMS:
				return getItems();
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
			case RegistryApiPackage.BATCH_RESULT__APPLIED:
				setApplied((Boolean)newValue);
				return;
			case RegistryApiPackage.BATCH_RESULT__ITEMS:
				getItems().clear();
				getItems().addAll((Collection<? extends BatchItemResult>)newValue);
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
			case RegistryApiPackage.BATCH_RESULT__APPLIED:
				setApplied(APPLIED_EDEFAULT);
				return;
			case RegistryApiPackage.BATCH_RESULT__ITEMS:
				getItems().clear();
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
			case RegistryApiPackage.BATCH_RESULT__APPLIED:
				return applied != APPLIED_EDEFAULT;
			case RegistryApiPackage.BATCH_RESULT__ITEMS:
				return items != null && !items.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case RegistryApiPackage.BATCH_RESULT___FIRST_REFUSAL:
				return firstRefusal();
		}
		return super.eInvoke(operationID, arguments);
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
		result.append(" (applied: ");
		result.append(applied);
		result.append(')');
		return result.toString();
	}

} //BatchResultImpl
