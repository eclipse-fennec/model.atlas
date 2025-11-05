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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.gecko.mac.governance.GovernancePackage;
import org.gecko.mac.governance.Policy;
import org.gecko.mac.governance.PolicyType;
import org.gecko.mac.governance.RequirementGroup;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Policy</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyImpl#getPolicyId <em>Policy Id</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyImpl#getRequirementGroups <em>Requirement Groups</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PolicyImpl extends MinimalEObjectImpl.Container implements Policy {
	/**
	 * The default value of the '{@link #getPolicyId() <em>Policy Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPolicyId()
	 * @generated
	 * @ordered
	 */
	protected static final String POLICY_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPolicyId() <em>Policy Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPolicyId()
	 * @generated
	 * @ordered
	 */
	protected String policyId = POLICY_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final PolicyType TYPE_EDEFAULT = PolicyType.GDPR;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected PolicyType type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getRequirementGroups() <em>Requirement Groups</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRequirementGroups()
	 * @generated
	 * @ordered
	 */
	protected EList<RequirementGroup> requirementGroups;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PolicyImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.POLICY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getPolicyId() {
		return policyId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPolicyId(String newPolicyId) {
		String oldPolicyId = policyId;
		policyId = newPolicyId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY__POLICY_ID, oldPolicyId, policyId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PolicyType getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setType(PolicyType newType) {
		PolicyType oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<RequirementGroup> getRequirementGroups() {
		if (requirementGroups == null) {
			requirementGroups = new EObjectContainmentWithInverseEList<RequirementGroup>(RequirementGroup.class, this, GovernancePackage.POLICY__REQUIREMENT_GROUPS, GovernancePackage.REQUIREMENT_GROUP__POLICY);
		}
		return requirementGroups;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case GovernancePackage.POLICY__REQUIREMENT_GROUPS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getRequirementGroups()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case GovernancePackage.POLICY__REQUIREMENT_GROUPS:
				return ((InternalEList<?>)getRequirementGroups()).basicRemove(otherEnd, msgs);
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
			case GovernancePackage.POLICY__POLICY_ID:
				return getPolicyId();
			case GovernancePackage.POLICY__TYPE:
				return getType();
			case GovernancePackage.POLICY__NAME:
				return getName();
			case GovernancePackage.POLICY__DESCRIPTION:
				return getDescription();
			case GovernancePackage.POLICY__REQUIREMENT_GROUPS:
				return getRequirementGroups();
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
			case GovernancePackage.POLICY__POLICY_ID:
				setPolicyId((String)newValue);
				return;
			case GovernancePackage.POLICY__TYPE:
				setType((PolicyType)newValue);
				return;
			case GovernancePackage.POLICY__NAME:
				setName((String)newValue);
				return;
			case GovernancePackage.POLICY__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case GovernancePackage.POLICY__REQUIREMENT_GROUPS:
				getRequirementGroups().clear();
				getRequirementGroups().addAll((Collection<? extends RequirementGroup>)newValue);
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
			case GovernancePackage.POLICY__POLICY_ID:
				setPolicyId(POLICY_ID_EDEFAULT);
				return;
			case GovernancePackage.POLICY__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case GovernancePackage.POLICY__NAME:
				setName(NAME_EDEFAULT);
				return;
			case GovernancePackage.POLICY__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case GovernancePackage.POLICY__REQUIREMENT_GROUPS:
				getRequirementGroups().clear();
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
			case GovernancePackage.POLICY__POLICY_ID:
				return POLICY_ID_EDEFAULT == null ? policyId != null : !POLICY_ID_EDEFAULT.equals(policyId);
			case GovernancePackage.POLICY__TYPE:
				return type != TYPE_EDEFAULT;
			case GovernancePackage.POLICY__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case GovernancePackage.POLICY__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case GovernancePackage.POLICY__REQUIREMENT_GROUPS:
				return requirementGroups != null && !requirementGroups.isEmpty();
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
		result.append(" (policyId: ");
		result.append(policyId);
		result.append(", type: ");
		result.append(type);
		result.append(", name: ");
		result.append(name);
		result.append(", description: ");
		result.append(description);
		result.append(')');
		return result.toString();
	}

} //PolicyImpl
