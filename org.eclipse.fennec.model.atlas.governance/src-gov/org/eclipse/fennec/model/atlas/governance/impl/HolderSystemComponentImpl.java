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
package org.eclipse.fennec.model.atlas.governance.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.governance.FeatureRequirementMapping;
import org.eclipse.fennec.model.atlas.governance.GovernancePackage;
import org.eclipse.fennec.model.atlas.governance.HolderSystemComponent;
import org.eclipse.fennec.model.atlas.governance.Policy;
import org.eclipse.fennec.model.atlas.governance.PolicyHolder;
import org.eclipse.fennec.model.atlas.governance.SystemComponent;
import org.eclipse.fennec.model.atlas.governance.SystemComponentHolder;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Holder System Component</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.HolderSystemComponentImpl#getSystemComponents <em>System Components</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.HolderSystemComponentImpl#getPolicyDefinitions <em>Policy Definitions</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.HolderSystemComponentImpl#getMappings <em>Mappings</em>}</li>
 * </ul>
 *
 * @generated
 */
public class HolderSystemComponentImpl extends SystemComponentImpl implements HolderSystemComponent {
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
	protected HolderSystemComponentImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.HOLDER_SYSTEM_COMPONENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<SystemComponent> getSystemComponents() {
		if (systemComponents == null) {
			systemComponents = new EObjectContainmentEList<SystemComponent>(SystemComponent.class, this, GovernancePackage.HOLDER_SYSTEM_COMPONENT__SYSTEM_COMPONENTS);
		}
		return systemComponents;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Policy> getPolicyDefinitions() {
		if (policyDefinitions == null) {
			policyDefinitions = new EObjectContainmentEList<Policy>(Policy.class, this, GovernancePackage.HOLDER_SYSTEM_COMPONENT__POLICY_DEFINITIONS);
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
			mappings = new EObjectContainmentEList<FeatureRequirementMapping>(FeatureRequirementMapping.class, this, GovernancePackage.HOLDER_SYSTEM_COMPONENT__MAPPINGS);
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
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__SYSTEM_COMPONENTS:
				return ((InternalEList<?>)getSystemComponents()).basicRemove(otherEnd, msgs);
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__POLICY_DEFINITIONS:
				return ((InternalEList<?>)getPolicyDefinitions()).basicRemove(otherEnd, msgs);
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__MAPPINGS:
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
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__SYSTEM_COMPONENTS:
				return getSystemComponents();
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__POLICY_DEFINITIONS:
				return getPolicyDefinitions();
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__MAPPINGS:
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
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__SYSTEM_COMPONENTS:
				getSystemComponents().clear();
				getSystemComponents().addAll((Collection<? extends SystemComponent>)newValue);
				return;
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__POLICY_DEFINITIONS:
				getPolicyDefinitions().clear();
				getPolicyDefinitions().addAll((Collection<? extends Policy>)newValue);
				return;
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__MAPPINGS:
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
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__SYSTEM_COMPONENTS:
				getSystemComponents().clear();
				return;
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__POLICY_DEFINITIONS:
				getPolicyDefinitions().clear();
				return;
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__MAPPINGS:
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
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__SYSTEM_COMPONENTS:
				return systemComponents != null && !systemComponents.isEmpty();
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__POLICY_DEFINITIONS:
				return policyDefinitions != null && !policyDefinitions.isEmpty();
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT__MAPPINGS:
				return mappings != null && !mappings.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == SystemComponentHolder.class) {
			switch (derivedFeatureID) {
				case GovernancePackage.HOLDER_SYSTEM_COMPONENT__SYSTEM_COMPONENTS: return GovernancePackage.SYSTEM_COMPONENT_HOLDER__SYSTEM_COMPONENTS;
				default: return -1;
			}
		}
		if (baseClass == PolicyHolder.class) {
			switch (derivedFeatureID) {
				case GovernancePackage.HOLDER_SYSTEM_COMPONENT__POLICY_DEFINITIONS: return GovernancePackage.POLICY_HOLDER__POLICY_DEFINITIONS;
				case GovernancePackage.HOLDER_SYSTEM_COMPONENT__MAPPINGS: return GovernancePackage.POLICY_HOLDER__MAPPINGS;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == SystemComponentHolder.class) {
			switch (baseFeatureID) {
				case GovernancePackage.SYSTEM_COMPONENT_HOLDER__SYSTEM_COMPONENTS: return GovernancePackage.HOLDER_SYSTEM_COMPONENT__SYSTEM_COMPONENTS;
				default: return -1;
			}
		}
		if (baseClass == PolicyHolder.class) {
			switch (baseFeatureID) {
				case GovernancePackage.POLICY_HOLDER__POLICY_DEFINITIONS: return GovernancePackage.HOLDER_SYSTEM_COMPONENT__POLICY_DEFINITIONS;
				case GovernancePackage.POLICY_HOLDER__MAPPINGS: return GovernancePackage.HOLDER_SYSTEM_COMPONENT__MAPPINGS;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

} //HolderSystemComponentImpl
