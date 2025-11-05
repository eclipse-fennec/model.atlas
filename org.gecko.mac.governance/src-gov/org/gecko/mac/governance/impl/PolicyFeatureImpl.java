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

import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

import org.gecko.mac.governance.FeatureHolder;
import org.gecko.mac.governance.GovernancePackage;
import org.gecko.mac.governance.PolicyFeature;
import org.gecko.mac.governance.SystemComponent;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Policy Feature</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyFeatureImpl#getFeatureId <em>Feature Id</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyFeatureImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyFeatureImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyFeatureImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyFeatureImpl#getComponents <em>Components</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PolicyFeatureImpl extends MinimalEObjectImpl.Container implements PolicyFeature {
	/**
	 * The default value of the '{@link #getFeatureId() <em>Feature Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureId()
	 * @generated
	 * @ordered
	 */
	protected static final String FEATURE_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFeatureId() <em>Feature Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureId()
	 * @generated
	 * @ordered
	 */
	protected String featureId = FEATURE_ID_EDEFAULT;

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
	 * The cached value of the '{@link #getComponents() <em>Components</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComponents()
	 * @generated
	 * @ordered
	 */
	protected EList<SystemComponent> components;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PolicyFeatureImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.POLICY_FEATURE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFeatureId() {
		return featureId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFeatureId(String newFeatureId) {
		String oldFeatureId = featureId;
		featureId = newFeatureId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY_FEATURE__FEATURE_ID, oldFeatureId, featureId));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY_FEATURE__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY_FEATURE__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureHolder getParent() {
		if (eContainerFeatureID() != GovernancePackage.POLICY_FEATURE__PARENT) return null;
		return (FeatureHolder)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetParent(FeatureHolder newParent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newParent, GovernancePackage.POLICY_FEATURE__PARENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setParent(FeatureHolder newParent) {
		if (newParent != eInternalContainer() || (eContainerFeatureID() != GovernancePackage.POLICY_FEATURE__PARENT && newParent != null)) {
			if (EcoreUtil.isAncestor(this, newParent))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newParent != null)
				msgs = ((InternalEObject)newParent).eInverseAdd(this, GovernancePackage.FEATURE_HOLDER__FEATURES, FeatureHolder.class, msgs);
			msgs = basicSetParent(newParent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY_FEATURE__PARENT, newParent, newParent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<SystemComponent> getComponents() {
		if (components == null) {
			components = new EObjectWithInverseResolvingEList.ManyInverse<SystemComponent>(SystemComponent.class, this, GovernancePackage.POLICY_FEATURE__COMPONENTS, GovernancePackage.SYSTEM_COMPONENT__SUPPORTED_FEATURES);
		}
		return components;
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
			case GovernancePackage.POLICY_FEATURE__PARENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetParent((FeatureHolder)otherEnd, msgs);
			case GovernancePackage.POLICY_FEATURE__COMPONENTS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getComponents()).basicAdd(otherEnd, msgs);
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
			case GovernancePackage.POLICY_FEATURE__PARENT:
				return basicSetParent(null, msgs);
			case GovernancePackage.POLICY_FEATURE__COMPONENTS:
				return ((InternalEList<?>)getComponents()).basicRemove(otherEnd, msgs);
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
			case GovernancePackage.POLICY_FEATURE__PARENT:
				return eInternalContainer().eInverseRemove(this, GovernancePackage.FEATURE_HOLDER__FEATURES, FeatureHolder.class, msgs);
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
			case GovernancePackage.POLICY_FEATURE__FEATURE_ID:
				return getFeatureId();
			case GovernancePackage.POLICY_FEATURE__NAME:
				return getName();
			case GovernancePackage.POLICY_FEATURE__DESCRIPTION:
				return getDescription();
			case GovernancePackage.POLICY_FEATURE__PARENT:
				return getParent();
			case GovernancePackage.POLICY_FEATURE__COMPONENTS:
				return getComponents();
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
			case GovernancePackage.POLICY_FEATURE__FEATURE_ID:
				setFeatureId((String)newValue);
				return;
			case GovernancePackage.POLICY_FEATURE__NAME:
				setName((String)newValue);
				return;
			case GovernancePackage.POLICY_FEATURE__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case GovernancePackage.POLICY_FEATURE__PARENT:
				setParent((FeatureHolder)newValue);
				return;
			case GovernancePackage.POLICY_FEATURE__COMPONENTS:
				getComponents().clear();
				getComponents().addAll((Collection<? extends SystemComponent>)newValue);
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
			case GovernancePackage.POLICY_FEATURE__FEATURE_ID:
				setFeatureId(FEATURE_ID_EDEFAULT);
				return;
			case GovernancePackage.POLICY_FEATURE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case GovernancePackage.POLICY_FEATURE__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case GovernancePackage.POLICY_FEATURE__PARENT:
				setParent((FeatureHolder)null);
				return;
			case GovernancePackage.POLICY_FEATURE__COMPONENTS:
				getComponents().clear();
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
			case GovernancePackage.POLICY_FEATURE__FEATURE_ID:
				return FEATURE_ID_EDEFAULT == null ? featureId != null : !FEATURE_ID_EDEFAULT.equals(featureId);
			case GovernancePackage.POLICY_FEATURE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case GovernancePackage.POLICY_FEATURE__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case GovernancePackage.POLICY_FEATURE__PARENT:
				return getParent() != null;
			case GovernancePackage.POLICY_FEATURE__COMPONENTS:
				return components != null && !components.isEmpty();
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
		result.append(" (featureId: ");
		result.append(featureId);
		result.append(", name: ");
		result.append(name);
		result.append(", description: ");
		result.append(description);
		result.append(')');
		return result.toString();
	}

} //PolicyFeatureImpl
