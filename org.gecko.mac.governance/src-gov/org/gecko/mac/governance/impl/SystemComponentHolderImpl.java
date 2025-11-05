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
package org.gecko.mac.governance.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.gecko.mac.governance.GovernancePackage;
import org.gecko.mac.governance.SystemComponent;
import org.gecko.mac.governance.SystemComponentHolder;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>System Component Holder</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentHolderImpl#getSystemComponents <em>System Components</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SystemComponentHolderImpl extends SystemHolderImpl implements SystemComponentHolder {
	/**
	 * The cached value of the '{@link #getSystemComponents() <em>System Components</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSystemComponents()
	 * @generated
	 * @ordered
	 */
	protected EList<SystemComponent> systemComponents;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SystemComponentHolderImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.SYSTEM_COMPONENT_HOLDER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<SystemComponent> getSystemComponents() {
		if (systemComponents == null) {
			systemComponents = new EObjectContainmentEList<SystemComponent>(SystemComponent.class, this, GovernancePackage.SYSTEM_COMPONENT_HOLDER__SYSTEM_COMPONENTS);
		}
		return systemComponents;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case GovernancePackage.SYSTEM_COMPONENT_HOLDER__SYSTEM_COMPONENTS:
				return ((InternalEList<?>)getSystemComponents()).basicRemove(otherEnd, msgs);
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
			case GovernancePackage.SYSTEM_COMPONENT_HOLDER__SYSTEM_COMPONENTS:
				return getSystemComponents();
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
			case GovernancePackage.SYSTEM_COMPONENT_HOLDER__SYSTEM_COMPONENTS:
				getSystemComponents().clear();
				getSystemComponents().addAll((Collection<? extends SystemComponent>)newValue);
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
			case GovernancePackage.SYSTEM_COMPONENT_HOLDER__SYSTEM_COMPONENTS:
				getSystemComponents().clear();
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
			case GovernancePackage.SYSTEM_COMPONENT_HOLDER__SYSTEM_COMPONENTS:
				return systemComponents != null && !systemComponents.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //SystemComponentHolderImpl
