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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.governance.GovernancePackage;
import org.eclipse.fennec.model.atlas.governance.Policy;
import org.eclipse.fennec.model.atlas.governance.PolicyRequirement;
import org.eclipse.fennec.model.atlas.governance.RequirementGroup;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Requirement Group</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.RequirementGroupImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.RequirementGroupImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.RequirementGroupImpl#getRequirements <em>Requirements</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.RequirementGroupImpl#getSubGroups <em>Sub Groups</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.RequirementGroupImpl#getGroup <em>Group</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.RequirementGroupImpl#getPolicy <em>Policy</em>}</li>
 * </ul>
 *
 * @generated
 */
public class RequirementGroupImpl extends MinimalEObjectImpl.Container implements RequirementGroup {
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
	 * The cached value of the '{@link #getRequirements() <em>Requirements</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRequirements()
	 * @generated
	 * @ordered
	 */
	protected EList<PolicyRequirement> requirements;

	/**
	 * The cached value of the '{@link #getSubGroups() <em>Sub Groups</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSubGroups()
	 * @generated
	 * @ordered
	 */
	protected EList<RequirementGroup> subGroups;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RequirementGroupImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.REQUIREMENT_GROUP;
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.REQUIREMENT_GROUP__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.REQUIREMENT_GROUP__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<PolicyRequirement> getRequirements() {
		if (requirements == null) {
			requirements = new EObjectContainmentWithInverseEList<PolicyRequirement>(PolicyRequirement.class, this, GovernancePackage.REQUIREMENT_GROUP__REQUIREMENTS, GovernancePackage.POLICY_REQUIREMENT__GROUP);
		}
		return requirements;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<RequirementGroup> getSubGroups() {
		if (subGroups == null) {
			subGroups = new EObjectContainmentWithInverseEList<RequirementGroup>(RequirementGroup.class, this, GovernancePackage.REQUIREMENT_GROUP__SUB_GROUPS, GovernancePackage.REQUIREMENT_GROUP__GROUP);
		}
		return subGroups;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RequirementGroup getGroup() {
		if (eContainerFeatureID() != GovernancePackage.REQUIREMENT_GROUP__GROUP) return null;
		return (RequirementGroup)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetGroup(RequirementGroup newGroup, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newGroup, GovernancePackage.REQUIREMENT_GROUP__GROUP, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setGroup(RequirementGroup newGroup) {
		if (newGroup != eInternalContainer() || (eContainerFeatureID() != GovernancePackage.REQUIREMENT_GROUP__GROUP && newGroup != null)) {
			if (EcoreUtil.isAncestor(this, newGroup))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newGroup != null)
				msgs = ((InternalEObject)newGroup).eInverseAdd(this, GovernancePackage.REQUIREMENT_GROUP__SUB_GROUPS, RequirementGroup.class, msgs);
			msgs = basicSetGroup(newGroup, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.REQUIREMENT_GROUP__GROUP, newGroup, newGroup));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Policy getPolicy() {
		if (eContainerFeatureID() != GovernancePackage.REQUIREMENT_GROUP__POLICY) return null;
		return (Policy)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetPolicy(Policy newPolicy, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newPolicy, GovernancePackage.REQUIREMENT_GROUP__POLICY, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPolicy(Policy newPolicy) {
		if (newPolicy != eInternalContainer() || (eContainerFeatureID() != GovernancePackage.REQUIREMENT_GROUP__POLICY && newPolicy != null)) {
			if (EcoreUtil.isAncestor(this, newPolicy))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newPolicy != null)
				msgs = ((InternalEObject)newPolicy).eInverseAdd(this, GovernancePackage.POLICY__REQUIREMENT_GROUPS, Policy.class, msgs);
			msgs = basicSetPolicy(newPolicy, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.REQUIREMENT_GROUP__POLICY, newPolicy, newPolicy));
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
			case GovernancePackage.REQUIREMENT_GROUP__REQUIREMENTS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getRequirements()).basicAdd(otherEnd, msgs);
			case GovernancePackage.REQUIREMENT_GROUP__SUB_GROUPS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getSubGroups()).basicAdd(otherEnd, msgs);
			case GovernancePackage.REQUIREMENT_GROUP__GROUP:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetGroup((RequirementGroup)otherEnd, msgs);
			case GovernancePackage.REQUIREMENT_GROUP__POLICY:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetPolicy((Policy)otherEnd, msgs);
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
			case GovernancePackage.REQUIREMENT_GROUP__REQUIREMENTS:
				return ((InternalEList<?>)getRequirements()).basicRemove(otherEnd, msgs);
			case GovernancePackage.REQUIREMENT_GROUP__SUB_GROUPS:
				return ((InternalEList<?>)getSubGroups()).basicRemove(otherEnd, msgs);
			case GovernancePackage.REQUIREMENT_GROUP__GROUP:
				return basicSetGroup(null, msgs);
			case GovernancePackage.REQUIREMENT_GROUP__POLICY:
				return basicSetPolicy(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID()) {
			case GovernancePackage.REQUIREMENT_GROUP__GROUP:
				return eInternalContainer().eInverseRemove(this, GovernancePackage.REQUIREMENT_GROUP__SUB_GROUPS, RequirementGroup.class, msgs);
			case GovernancePackage.REQUIREMENT_GROUP__POLICY:
				return eInternalContainer().eInverseRemove(this, GovernancePackage.POLICY__REQUIREMENT_GROUPS, Policy.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GovernancePackage.REQUIREMENT_GROUP__NAME:
				return getName();
			case GovernancePackage.REQUIREMENT_GROUP__DESCRIPTION:
				return getDescription();
			case GovernancePackage.REQUIREMENT_GROUP__REQUIREMENTS:
				return getRequirements();
			case GovernancePackage.REQUIREMENT_GROUP__SUB_GROUPS:
				return getSubGroups();
			case GovernancePackage.REQUIREMENT_GROUP__GROUP:
				return getGroup();
			case GovernancePackage.REQUIREMENT_GROUP__POLICY:
				return getPolicy();
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
			case GovernancePackage.REQUIREMENT_GROUP__NAME:
				setName((String)newValue);
				return;
			case GovernancePackage.REQUIREMENT_GROUP__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case GovernancePackage.REQUIREMENT_GROUP__REQUIREMENTS:
				getRequirements().clear();
				getRequirements().addAll((Collection<? extends PolicyRequirement>)newValue);
				return;
			case GovernancePackage.REQUIREMENT_GROUP__SUB_GROUPS:
				getSubGroups().clear();
				getSubGroups().addAll((Collection<? extends RequirementGroup>)newValue);
				return;
			case GovernancePackage.REQUIREMENT_GROUP__GROUP:
				setGroup((RequirementGroup)newValue);
				return;
			case GovernancePackage.REQUIREMENT_GROUP__POLICY:
				setPolicy((Policy)newValue);
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
			case GovernancePackage.REQUIREMENT_GROUP__NAME:
				setName(NAME_EDEFAULT);
				return;
			case GovernancePackage.REQUIREMENT_GROUP__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case GovernancePackage.REQUIREMENT_GROUP__REQUIREMENTS:
				getRequirements().clear();
				return;
			case GovernancePackage.REQUIREMENT_GROUP__SUB_GROUPS:
				getSubGroups().clear();
				return;
			case GovernancePackage.REQUIREMENT_GROUP__GROUP:
				setGroup((RequirementGroup)null);
				return;
			case GovernancePackage.REQUIREMENT_GROUP__POLICY:
				setPolicy((Policy)null);
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
			case GovernancePackage.REQUIREMENT_GROUP__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case GovernancePackage.REQUIREMENT_GROUP__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case GovernancePackage.REQUIREMENT_GROUP__REQUIREMENTS:
				return requirements != null && !requirements.isEmpty();
			case GovernancePackage.REQUIREMENT_GROUP__SUB_GROUPS:
				return subGroups != null && !subGroups.isEmpty();
			case GovernancePackage.REQUIREMENT_GROUP__GROUP:
				return getGroup() != null;
			case GovernancePackage.REQUIREMENT_GROUP__POLICY:
				return getPolicy() != null;
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
		result.append(" (name: ");
		result.append(name);
		result.append(", description: ");
		result.append(description);
		result.append(')');
		return result.toString();
	}

} //RequirementGroupImpl
