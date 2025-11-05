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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EcoreUtil;

import org.eclipse.fennec.model.atlas.governance.ComponentDependency;
import org.eclipse.fennec.model.atlas.governance.GovernanceNamespace;
import org.eclipse.fennec.model.atlas.governance.GovernancePackage;
import org.eclipse.fennec.model.atlas.governance.SystemComponent;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Component Dependency</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.ComponentDependencyImpl#getComponent <em>Component</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.ComponentDependencyImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.ComponentDependencyImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.ComponentDependencyImpl#getReferencedComponent <em>Referenced Component</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.ComponentDependencyImpl#getGovernanceNamespace <em>Governance Namespace</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class ComponentDependencyImpl extends MinimalEObjectImpl.Container implements ComponentDependency {
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
	 * The cached value of the '{@link #getReferencedComponent() <em>Referenced Component</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReferencedComponent()
	 * @generated
	 * @ordered
	 */
	protected SystemComponent referencedComponent;

	/**
	 * The cached value of the '{@link #getGovernanceNamespace() <em>Governance Namespace</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGovernanceNamespace()
	 * @generated
	 * @ordered
	 */
	protected GovernanceNamespace governanceNamespace;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ComponentDependencyImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.COMPONENT_DEPENDENCY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SystemComponent getComponent() {
		if (eContainerFeatureID() != GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT) return null;
		return (SystemComponent)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetComponent(SystemComponent newComponent, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newComponent, GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setComponent(SystemComponent newComponent) {
		if (newComponent != eInternalContainer() || (eContainerFeatureID() != GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT && newComponent != null)) {
			if (EcoreUtil.isAncestor(this, newComponent))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newComponent != null)
				msgs = ((InternalEObject)newComponent).eInverseAdd(this, GovernancePackage.SYSTEM_COMPONENT__DEPENDENCIES, SystemComponent.class, msgs);
			msgs = basicSetComponent(newComponent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT, newComponent, newComponent));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.COMPONENT_DEPENDENCY__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.COMPONENT_DEPENDENCY__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SystemComponent getReferencedComponent() {
		if (referencedComponent != null && referencedComponent.eIsProxy()) {
			InternalEObject oldReferencedComponent = (InternalEObject)referencedComponent;
			referencedComponent = (SystemComponent)eResolveProxy(oldReferencedComponent);
			if (referencedComponent != oldReferencedComponent) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GovernancePackage.COMPONENT_DEPENDENCY__REFERENCED_COMPONENT, oldReferencedComponent, referencedComponent));
			}
		}
		return referencedComponent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SystemComponent basicGetReferencedComponent() {
		return referencedComponent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setReferencedComponent(SystemComponent newReferencedComponent) {
		SystemComponent oldReferencedComponent = referencedComponent;
		referencedComponent = newReferencedComponent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.COMPONENT_DEPENDENCY__REFERENCED_COMPONENT, oldReferencedComponent, referencedComponent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GovernanceNamespace getGovernanceNamespace() {
		if (governanceNamespace != null && governanceNamespace.eIsProxy()) {
			InternalEObject oldGovernanceNamespace = (InternalEObject)governanceNamespace;
			governanceNamespace = (GovernanceNamespace)eResolveProxy(oldGovernanceNamespace);
			if (governanceNamespace != oldGovernanceNamespace) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GovernancePackage.COMPONENT_DEPENDENCY__GOVERNANCE_NAMESPACE, oldGovernanceNamespace, governanceNamespace));
			}
		}
		return governanceNamespace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GovernanceNamespace basicGetGovernanceNamespace() {
		return governanceNamespace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setGovernanceNamespace(GovernanceNamespace newGovernanceNamespace) {
		GovernanceNamespace oldGovernanceNamespace = governanceNamespace;
		governanceNamespace = newGovernanceNamespace;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.COMPONENT_DEPENDENCY__GOVERNANCE_NAMESPACE, oldGovernanceNamespace, governanceNamespace));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetComponent((SystemComponent)otherEnd, msgs);
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
			case GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT:
				return basicSetComponent(null, msgs);
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
			case GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT:
				return eInternalContainer().eInverseRemove(this, GovernancePackage.SYSTEM_COMPONENT__DEPENDENCIES, SystemComponent.class, msgs);
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
			case GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT:
				return getComponent();
			case GovernancePackage.COMPONENT_DEPENDENCY__NAME:
				return getName();
			case GovernancePackage.COMPONENT_DEPENDENCY__DESCRIPTION:
				return getDescription();
			case GovernancePackage.COMPONENT_DEPENDENCY__REFERENCED_COMPONENT:
				if (resolve) return getReferencedComponent();
				return basicGetReferencedComponent();
			case GovernancePackage.COMPONENT_DEPENDENCY__GOVERNANCE_NAMESPACE:
				if (resolve) return getGovernanceNamespace();
				return basicGetGovernanceNamespace();
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
			case GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT:
				setComponent((SystemComponent)newValue);
				return;
			case GovernancePackage.COMPONENT_DEPENDENCY__NAME:
				setName((String)newValue);
				return;
			case GovernancePackage.COMPONENT_DEPENDENCY__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case GovernancePackage.COMPONENT_DEPENDENCY__REFERENCED_COMPONENT:
				setReferencedComponent((SystemComponent)newValue);
				return;
			case GovernancePackage.COMPONENT_DEPENDENCY__GOVERNANCE_NAMESPACE:
				setGovernanceNamespace((GovernanceNamespace)newValue);
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
			case GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT:
				setComponent((SystemComponent)null);
				return;
			case GovernancePackage.COMPONENT_DEPENDENCY__NAME:
				setName(NAME_EDEFAULT);
				return;
			case GovernancePackage.COMPONENT_DEPENDENCY__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case GovernancePackage.COMPONENT_DEPENDENCY__REFERENCED_COMPONENT:
				setReferencedComponent((SystemComponent)null);
				return;
			case GovernancePackage.COMPONENT_DEPENDENCY__GOVERNANCE_NAMESPACE:
				setGovernanceNamespace((GovernanceNamespace)null);
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
			case GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT:
				return getComponent() != null;
			case GovernancePackage.COMPONENT_DEPENDENCY__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case GovernancePackage.COMPONENT_DEPENDENCY__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case GovernancePackage.COMPONENT_DEPENDENCY__REFERENCED_COMPONENT:
				return referencedComponent != null;
			case GovernancePackage.COMPONENT_DEPENDENCY__GOVERNANCE_NAMESPACE:
				return governanceNamespace != null;
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

} //ComponentDependencyImpl
