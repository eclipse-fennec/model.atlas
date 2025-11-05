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

import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.gecko.mac.governance.ComponentDependency;
import org.gecko.mac.governance.GovernancePackage;
import org.gecko.mac.governance.Policy;
import org.gecko.mac.governance.PolicyFeature;
import org.gecko.mac.governance.SystemComponent;
import org.gecko.mac.governance.SystemComponentType;
import org.gecko.mac.governance.TrustLevel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>System Component</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getComponentId <em>Component Id</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getTrustLevel <em>Trust Level</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getAppliedPolicies <em>Applied Policies</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getType <em>Type</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getChildComponents <em>Child Components</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getParentComponents <em>Parent Components</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getSupervisorComponent <em>Supervisor Component</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getSupervisedComponents <em>Supervised Components</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getDependencies <em>Dependencies</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.SystemComponentImpl#getSupportedFeatures <em>Supported Features</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SystemComponentImpl extends FeatureHolderImpl implements SystemComponent {
	/**
	 * The default value of the '{@link #getComponentId() <em>Component Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComponentId()
	 * @generated
	 * @ordered
	 */
	protected static final String COMPONENT_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getComponentId() <em>Component Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComponentId()
	 * @generated
	 * @ordered
	 */
	protected String componentId = COMPONENT_ID_EDEFAULT;

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
	 * The default value of the '{@link #getTrustLevel() <em>Trust Level</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTrustLevel()
	 * @generated
	 * @ordered
	 */
	protected static final TrustLevel TRUST_LEVEL_EDEFAULT = TrustLevel.UNKNOWN;

	/**
	 * The cached value of the '{@link #getTrustLevel() <em>Trust Level</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTrustLevel()
	 * @generated
	 * @ordered
	 */
	protected TrustLevel trustLevel = TRUST_LEVEL_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAppliedPolicies() <em>Applied Policies</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAppliedPolicies()
	 * @generated
	 * @ordered
	 */
	protected EList<Policy> appliedPolicies;

	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final SystemComponentType TYPE_EDEFAULT = SystemComponentType.UNKNOWN;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected SystemComponentType type = TYPE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getChildComponents() <em>Child Components</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildComponents()
	 * @generated
	 * @ordered
	 */
	protected EList<SystemComponent> childComponents;

	/**
	 * The cached value of the '{@link #getParentComponents() <em>Parent Components</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParentComponents()
	 * @generated
	 * @ordered
	 */
	protected EList<SystemComponent> parentComponents;

	/**
	 * The cached value of the '{@link #getSupervisorComponent() <em>Supervisor Component</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSupervisorComponent()
	 * @generated
	 * @ordered
	 */
	protected SystemComponent supervisorComponent;

	/**
	 * The cached value of the '{@link #getSupervisedComponents() <em>Supervised Components</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSupervisedComponents()
	 * @generated
	 * @ordered
	 */
	protected EList<SystemComponent> supervisedComponents;

	/**
	 * The cached value of the '{@link #getDependencies() <em>Dependencies</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDependencies()
	 * @generated
	 * @ordered
	 */
	protected EList<ComponentDependency> dependencies;

	/**
	 * The cached value of the '{@link #getSupportedFeatures() <em>Supported Features</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSupportedFeatures()
	 * @generated
	 * @ordered
	 */
	protected EList<PolicyFeature> supportedFeatures;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SystemComponentImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.SYSTEM_COMPONENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getComponentId() {
		return componentId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setComponentId(String newComponentId) {
		String oldComponentId = componentId;
		componentId = newComponentId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.SYSTEM_COMPONENT__COMPONENT_ID, oldComponentId, componentId));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.SYSTEM_COMPONENT__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.SYSTEM_COMPONENT__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TrustLevel getTrustLevel() {
		return trustLevel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTrustLevel(TrustLevel newTrustLevel) {
		TrustLevel oldTrustLevel = trustLevel;
		trustLevel = newTrustLevel == null ? TRUST_LEVEL_EDEFAULT : newTrustLevel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.SYSTEM_COMPONENT__TRUST_LEVEL, oldTrustLevel, trustLevel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Policy> getAppliedPolicies() {
		if (appliedPolicies == null) {
			appliedPolicies = new EObjectResolvingEList<Policy>(Policy.class, this, GovernancePackage.SYSTEM_COMPONENT__APPLIED_POLICIES);
		}
		return appliedPolicies;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SystemComponentType getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setType(SystemComponentType newType) {
		SystemComponentType oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.SYSTEM_COMPONENT__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<SystemComponent> getChildComponents() {
		if (childComponents == null) {
			childComponents = new EObjectWithInverseResolvingEList.ManyInverse<SystemComponent>(SystemComponent.class, this, GovernancePackage.SYSTEM_COMPONENT__CHILD_COMPONENTS, GovernancePackage.SYSTEM_COMPONENT__PARENT_COMPONENTS);
		}
		return childComponents;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<SystemComponent> getParentComponents() {
		if (parentComponents == null) {
			parentComponents = new EObjectWithInverseResolvingEList.ManyInverse<SystemComponent>(SystemComponent.class, this, GovernancePackage.SYSTEM_COMPONENT__PARENT_COMPONENTS, GovernancePackage.SYSTEM_COMPONENT__CHILD_COMPONENTS);
		}
		return parentComponents;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SystemComponent getSupervisorComponent() {
		if (supervisorComponent != null && supervisorComponent.eIsProxy()) {
			InternalEObject oldSupervisorComponent = (InternalEObject)supervisorComponent;
			supervisorComponent = (SystemComponent)eResolveProxy(oldSupervisorComponent);
			if (supervisorComponent != oldSupervisorComponent) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GovernancePackage.SYSTEM_COMPONENT__SUPERVISOR_COMPONENT, oldSupervisorComponent, supervisorComponent));
			}
		}
		return supervisorComponent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SystemComponent basicGetSupervisorComponent() {
		return supervisorComponent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSupervisorComponent(SystemComponent newSupervisorComponent, NotificationChain msgs) {
		SystemComponent oldSupervisorComponent = supervisorComponent;
		supervisorComponent = newSupervisorComponent;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GovernancePackage.SYSTEM_COMPONENT__SUPERVISOR_COMPONENT, oldSupervisorComponent, newSupervisorComponent);
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
	public void setSupervisorComponent(SystemComponent newSupervisorComponent) {
		if (newSupervisorComponent != supervisorComponent) {
			NotificationChain msgs = null;
			if (supervisorComponent != null)
				msgs = ((InternalEObject)supervisorComponent).eInverseRemove(this, GovernancePackage.SYSTEM_COMPONENT__SUPERVISED_COMPONENTS, SystemComponent.class, msgs);
			if (newSupervisorComponent != null)
				msgs = ((InternalEObject)newSupervisorComponent).eInverseAdd(this, GovernancePackage.SYSTEM_COMPONENT__SUPERVISED_COMPONENTS, SystemComponent.class, msgs);
			msgs = basicSetSupervisorComponent(newSupervisorComponent, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.SYSTEM_COMPONENT__SUPERVISOR_COMPONENT, newSupervisorComponent, newSupervisorComponent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<SystemComponent> getSupervisedComponents() {
		if (supervisedComponents == null) {
			supervisedComponents = new EObjectWithInverseResolvingEList<SystemComponent>(SystemComponent.class, this, GovernancePackage.SYSTEM_COMPONENT__SUPERVISED_COMPONENTS, GovernancePackage.SYSTEM_COMPONENT__SUPERVISOR_COMPONENT);
		}
		return supervisedComponents;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ComponentDependency> getDependencies() {
		if (dependencies == null) {
			dependencies = new EObjectContainmentWithInverseEList<ComponentDependency>(ComponentDependency.class, this, GovernancePackage.SYSTEM_COMPONENT__DEPENDENCIES, GovernancePackage.COMPONENT_DEPENDENCY__COMPONENT);
		}
		return dependencies;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<PolicyFeature> getSupportedFeatures() {
		if (supportedFeatures == null) {
			supportedFeatures = new EObjectWithInverseResolvingEList.ManyInverse<PolicyFeature>(PolicyFeature.class, this, GovernancePackage.SYSTEM_COMPONENT__SUPPORTED_FEATURES, GovernancePackage.POLICY_FEATURE__COMPONENTS);
		}
		return supportedFeatures;
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
			case GovernancePackage.SYSTEM_COMPONENT__CHILD_COMPONENTS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getChildComponents()).basicAdd(otherEnd, msgs);
			case GovernancePackage.SYSTEM_COMPONENT__PARENT_COMPONENTS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getParentComponents()).basicAdd(otherEnd, msgs);
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISOR_COMPONENT:
				if (supervisorComponent != null)
					msgs = ((InternalEObject)supervisorComponent).eInverseRemove(this, GovernancePackage.SYSTEM_COMPONENT__SUPERVISED_COMPONENTS, SystemComponent.class, msgs);
				return basicSetSupervisorComponent((SystemComponent)otherEnd, msgs);
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISED_COMPONENTS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getSupervisedComponents()).basicAdd(otherEnd, msgs);
			case GovernancePackage.SYSTEM_COMPONENT__DEPENDENCIES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getDependencies()).basicAdd(otherEnd, msgs);
			case GovernancePackage.SYSTEM_COMPONENT__SUPPORTED_FEATURES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getSupportedFeatures()).basicAdd(otherEnd, msgs);
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
			case GovernancePackage.SYSTEM_COMPONENT__CHILD_COMPONENTS:
				return ((InternalEList<?>)getChildComponents()).basicRemove(otherEnd, msgs);
			case GovernancePackage.SYSTEM_COMPONENT__PARENT_COMPONENTS:
				return ((InternalEList<?>)getParentComponents()).basicRemove(otherEnd, msgs);
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISOR_COMPONENT:
				return basicSetSupervisorComponent(null, msgs);
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISED_COMPONENTS:
				return ((InternalEList<?>)getSupervisedComponents()).basicRemove(otherEnd, msgs);
			case GovernancePackage.SYSTEM_COMPONENT__DEPENDENCIES:
				return ((InternalEList<?>)getDependencies()).basicRemove(otherEnd, msgs);
			case GovernancePackage.SYSTEM_COMPONENT__SUPPORTED_FEATURES:
				return ((InternalEList<?>)getSupportedFeatures()).basicRemove(otherEnd, msgs);
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
			case GovernancePackage.SYSTEM_COMPONENT__COMPONENT_ID:
				return getComponentId();
			case GovernancePackage.SYSTEM_COMPONENT__NAME:
				return getName();
			case GovernancePackage.SYSTEM_COMPONENT__DESCRIPTION:
				return getDescription();
			case GovernancePackage.SYSTEM_COMPONENT__TRUST_LEVEL:
				return getTrustLevel();
			case GovernancePackage.SYSTEM_COMPONENT__APPLIED_POLICIES:
				return getAppliedPolicies();
			case GovernancePackage.SYSTEM_COMPONENT__TYPE:
				return getType();
			case GovernancePackage.SYSTEM_COMPONENT__CHILD_COMPONENTS:
				return getChildComponents();
			case GovernancePackage.SYSTEM_COMPONENT__PARENT_COMPONENTS:
				return getParentComponents();
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISOR_COMPONENT:
				if (resolve) return getSupervisorComponent();
				return basicGetSupervisorComponent();
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISED_COMPONENTS:
				return getSupervisedComponents();
			case GovernancePackage.SYSTEM_COMPONENT__DEPENDENCIES:
				return getDependencies();
			case GovernancePackage.SYSTEM_COMPONENT__SUPPORTED_FEATURES:
				return getSupportedFeatures();
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
			case GovernancePackage.SYSTEM_COMPONENT__COMPONENT_ID:
				setComponentId((String)newValue);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__NAME:
				setName((String)newValue);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__TRUST_LEVEL:
				setTrustLevel((TrustLevel)newValue);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__APPLIED_POLICIES:
				getAppliedPolicies().clear();
				getAppliedPolicies().addAll((Collection<? extends Policy>)newValue);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__TYPE:
				setType((SystemComponentType)newValue);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__CHILD_COMPONENTS:
				getChildComponents().clear();
				getChildComponents().addAll((Collection<? extends SystemComponent>)newValue);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__PARENT_COMPONENTS:
				getParentComponents().clear();
				getParentComponents().addAll((Collection<? extends SystemComponent>)newValue);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISOR_COMPONENT:
				setSupervisorComponent((SystemComponent)newValue);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISED_COMPONENTS:
				getSupervisedComponents().clear();
				getSupervisedComponents().addAll((Collection<? extends SystemComponent>)newValue);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__DEPENDENCIES:
				getDependencies().clear();
				getDependencies().addAll((Collection<? extends ComponentDependency>)newValue);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__SUPPORTED_FEATURES:
				getSupportedFeatures().clear();
				getSupportedFeatures().addAll((Collection<? extends PolicyFeature>)newValue);
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
			case GovernancePackage.SYSTEM_COMPONENT__COMPONENT_ID:
				setComponentId(COMPONENT_ID_EDEFAULT);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__NAME:
				setName(NAME_EDEFAULT);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__TRUST_LEVEL:
				setTrustLevel(TRUST_LEVEL_EDEFAULT);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__APPLIED_POLICIES:
				getAppliedPolicies().clear();
				return;
			case GovernancePackage.SYSTEM_COMPONENT__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__CHILD_COMPONENTS:
				getChildComponents().clear();
				return;
			case GovernancePackage.SYSTEM_COMPONENT__PARENT_COMPONENTS:
				getParentComponents().clear();
				return;
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISOR_COMPONENT:
				setSupervisorComponent((SystemComponent)null);
				return;
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISED_COMPONENTS:
				getSupervisedComponents().clear();
				return;
			case GovernancePackage.SYSTEM_COMPONENT__DEPENDENCIES:
				getDependencies().clear();
				return;
			case GovernancePackage.SYSTEM_COMPONENT__SUPPORTED_FEATURES:
				getSupportedFeatures().clear();
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
			case GovernancePackage.SYSTEM_COMPONENT__COMPONENT_ID:
				return COMPONENT_ID_EDEFAULT == null ? componentId != null : !COMPONENT_ID_EDEFAULT.equals(componentId);
			case GovernancePackage.SYSTEM_COMPONENT__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case GovernancePackage.SYSTEM_COMPONENT__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case GovernancePackage.SYSTEM_COMPONENT__TRUST_LEVEL:
				return trustLevel != TRUST_LEVEL_EDEFAULT;
			case GovernancePackage.SYSTEM_COMPONENT__APPLIED_POLICIES:
				return appliedPolicies != null && !appliedPolicies.isEmpty();
			case GovernancePackage.SYSTEM_COMPONENT__TYPE:
				return type != TYPE_EDEFAULT;
			case GovernancePackage.SYSTEM_COMPONENT__CHILD_COMPONENTS:
				return childComponents != null && !childComponents.isEmpty();
			case GovernancePackage.SYSTEM_COMPONENT__PARENT_COMPONENTS:
				return parentComponents != null && !parentComponents.isEmpty();
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISOR_COMPONENT:
				return supervisorComponent != null;
			case GovernancePackage.SYSTEM_COMPONENT__SUPERVISED_COMPONENTS:
				return supervisedComponents != null && !supervisedComponents.isEmpty();
			case GovernancePackage.SYSTEM_COMPONENT__DEPENDENCIES:
				return dependencies != null && !dependencies.isEmpty();
			case GovernancePackage.SYSTEM_COMPONENT__SUPPORTED_FEATURES:
				return supportedFeatures != null && !supportedFeatures.isEmpty();
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
		result.append(" (componentId: ");
		result.append(componentId);
		result.append(", name: ");
		result.append(name);
		result.append(", description: ");
		result.append(description);
		result.append(", trustLevel: ");
		result.append(trustLevel);
		result.append(", type: ");
		result.append(type);
		result.append(')');
		return result.toString();
	}

} //SystemComponentImpl
