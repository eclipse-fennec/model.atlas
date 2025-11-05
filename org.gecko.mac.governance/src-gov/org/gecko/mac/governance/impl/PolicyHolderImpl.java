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

import org.gecko.mac.governance.FeatureRequirementMapping;
import org.gecko.mac.governance.GovernancePackage;
import org.gecko.mac.governance.Policy;
import org.gecko.mac.governance.PolicyHolder;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Policy Holder</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyHolderImpl#getPolicyDefinitions <em>Policy Definitions</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyHolderImpl#getMappings <em>Mappings</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PolicyHolderImpl extends SystemHolderImpl implements PolicyHolder {
	/**
	 * The cached value of the '{@link #getPolicyDefinitions() <em>Policy Definitions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPolicyDefinitions()
	 * @generated
	 * @ordered
	 */
	protected EList<Policy> policyDefinitions;

	/**
	 * The cached value of the '{@link #getMappings() <em>Mappings</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMappings()
	 * @generated
	 * @ordered
	 */
	protected EList<FeatureRequirementMapping> mappings;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PolicyHolderImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.POLICY_HOLDER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Policy> getPolicyDefinitions() {
		if (policyDefinitions == null) {
			policyDefinitions = new EObjectContainmentEList<Policy>(Policy.class, this, GovernancePackage.POLICY_HOLDER__POLICY_DEFINITIONS);
		}
		return policyDefinitions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<FeatureRequirementMapping> getMappings() {
		if (mappings == null) {
			mappings = new EObjectContainmentEList<FeatureRequirementMapping>(FeatureRequirementMapping.class, this, GovernancePackage.POLICY_HOLDER__MAPPINGS);
		}
		return mappings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case GovernancePackage.POLICY_HOLDER__POLICY_DEFINITIONS:
				return ((InternalEList<?>)getPolicyDefinitions()).basicRemove(otherEnd, msgs);
			case GovernancePackage.POLICY_HOLDER__MAPPINGS:
				return ((InternalEList<?>)getMappings()).basicRemove(otherEnd, msgs);
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
			case GovernancePackage.POLICY_HOLDER__POLICY_DEFINITIONS:
				return getPolicyDefinitions();
			case GovernancePackage.POLICY_HOLDER__MAPPINGS:
				return getMappings();
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
			case GovernancePackage.POLICY_HOLDER__POLICY_DEFINITIONS:
				getPolicyDefinitions().clear();
				getPolicyDefinitions().addAll((Collection<? extends Policy>)newValue);
				return;
			case GovernancePackage.POLICY_HOLDER__MAPPINGS:
				getMappings().clear();
				getMappings().addAll((Collection<? extends FeatureRequirementMapping>)newValue);
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
			case GovernancePackage.POLICY_HOLDER__POLICY_DEFINITIONS:
				getPolicyDefinitions().clear();
				return;
			case GovernancePackage.POLICY_HOLDER__MAPPINGS:
				getMappings().clear();
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
			case GovernancePackage.POLICY_HOLDER__POLICY_DEFINITIONS:
				return policyDefinitions != null && !policyDefinitions.isEmpty();
			case GovernancePackage.POLICY_HOLDER__MAPPINGS:
				return mappings != null && !mappings.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //PolicyHolderImpl
