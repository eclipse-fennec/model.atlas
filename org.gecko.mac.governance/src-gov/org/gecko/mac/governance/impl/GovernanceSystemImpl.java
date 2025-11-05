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

import java.util.stream.Collectors;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.gecko.mac.governance.FeatureHolder;
import org.gecko.mac.governance.FeatureRequirementMapping;
import org.gecko.mac.governance.GovernanceNamespace;
import org.gecko.mac.governance.GovernancePackage;
import org.gecko.mac.governance.GovernanceSystem;
import org.gecko.mac.governance.NamespaceHolder;
import org.gecko.mac.governance.Policy;
import org.gecko.mac.governance.PolicyFeature;
import org.gecko.mac.governance.PolicyHolder;
import org.gecko.mac.governance.SystemComponentHolder;
import org.gecko.mac.governance.SystemHolder;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>System</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getPolicyDefinitions <em>Policy Definitions</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getMappings <em>Mappings</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getFeatures <em>Features</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getNamespaces <em>Namespaces</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getSystemId <em>System Id</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getSubSystems <em>Sub Systems</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getReferencedSystems <em>Referenced Systems</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getComponentHolders <em>Component Holders</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getPolicyHolders <em>Policy Holders</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getHolders <em>Holders</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceSystemImpl#getPolicies <em>Policies</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GovernanceSystemImpl extends SystemComponentHolderImpl implements GovernanceSystem {
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
	 * The cached value of the '{@link #getFeatures() <em>Features</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatures()
	 * @generated
	 * @ordered
	 */
	protected EList<PolicyFeature> features;

	/**
	 * The cached value of the '{@link #getNamespaces() <em>Namespaces</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespaces()
	 * @generated
	 * @ordered
	 */
	protected EList<GovernanceNamespace> namespaces;

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
	 * The default value of the '{@link #getSystemId() <em>System Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSystemId()
	 * @generated
	 * @ordered
	 */
	protected static final String SYSTEM_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSystemId() <em>System Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSystemId()
	 * @generated
	 * @ordered
	 */
	protected String systemId = SYSTEM_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getSubSystems() <em>Sub Systems</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSubSystems()
	 * @generated
	 * @ordered
	 */
	protected EList<GovernanceSystem> subSystems;

	/**
	 * The cached value of the '{@link #getReferencedSystems() <em>Referenced Systems</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReferencedSystems()
	 * @generated
	 * @ordered
	 */
	protected EList<GovernanceSystem> referencedSystems;

	/**
	 * The cached value of the '{@link #getHolders() <em>Holders</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHolders()
	 * @generated
	 * @ordered
	 */
	protected EList<SystemHolder> holders;

	/**
	 * The cached value of the '{@link #getPolicies() <em>Policies</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPolicies()
	 * @generated
	 * @ordered
	 */
	protected EList<Policy> policies;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GovernanceSystemImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.GOVERNANCE_SYSTEM;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Policy> getPolicyDefinitions() {
		if (policyDefinitions == null) {
			policyDefinitions = new EObjectContainmentEList<Policy>(Policy.class, this, GovernancePackage.GOVERNANCE_SYSTEM__POLICY_DEFINITIONS);
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
			mappings = new EObjectContainmentEList<FeatureRequirementMapping>(FeatureRequirementMapping.class, this, GovernancePackage.GOVERNANCE_SYSTEM__MAPPINGS);
		}
		return mappings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<PolicyFeature> getFeatures() {
		if (features == null) {
			features = new EObjectContainmentWithInverseEList<PolicyFeature>(PolicyFeature.class, this, GovernancePackage.GOVERNANCE_SYSTEM__FEATURES, GovernancePackage.POLICY_FEATURE__PARENT);
		}
		return features;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<GovernanceNamespace> getNamespaces() {
		if (namespaces == null) {
			namespaces = new EObjectContainmentEList<GovernanceNamespace>(GovernanceNamespace.class, this, GovernancePackage.GOVERNANCE_SYSTEM__NAMESPACES);
		}
		return namespaces;
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.GOVERNANCE_SYSTEM__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.GOVERNANCE_SYSTEM__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSystemId() {
		return systemId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSystemId(String newSystemId) {
		String oldSystemId = systemId;
		systemId = newSystemId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.GOVERNANCE_SYSTEM__SYSTEM_ID, oldSystemId, systemId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<GovernanceSystem> getSubSystems() {
		if (subSystems == null) {
			subSystems = new EObjectContainmentEList<GovernanceSystem>(GovernanceSystem.class, this, GovernancePackage.GOVERNANCE_SYSTEM__SUB_SYSTEMS);
		}
		return subSystems;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<GovernanceSystem> getReferencedSystems() {
		if (referencedSystems == null) {
			referencedSystems = new EObjectResolvingEList<GovernanceSystem>(GovernanceSystem.class, this, GovernancePackage.GOVERNANCE_SYSTEM__REFERENCED_SYSTEMS);
		}
		return referencedSystems;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<SystemComponentHolder> getComponentHolders() {
		return getHolders().stream().filter(SystemComponentHolder.class::isInstance).map(SystemComponentHolder.class::cast).collect(Collectors.toCollection(BasicEList::new));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<PolicyHolder> getPolicyHolders() {
		return getHolders().stream().filter(PolicyHolder.class::isInstance).map(PolicyHolder.class::cast).collect(Collectors.toCollection(BasicEList::new));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<SystemHolder> getHolders() {
		if (holders == null) {
			holders = new EObjectWithInverseResolvingEList<SystemHolder>(SystemHolder.class, this, GovernancePackage.GOVERNANCE_SYSTEM__HOLDERS, GovernancePackage.SYSTEM_HOLDER__SYSTEM);
		}
		return holders;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Policy> getPolicies() {
		if (policies == null) {
			policies = new EObjectResolvingEList<Policy>(Policy.class, this, GovernancePackage.GOVERNANCE_SYSTEM__POLICIES);
		}
		return policies;
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
			case GovernancePackage.GOVERNANCE_SYSTEM__FEATURES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getFeatures()).basicAdd(otherEnd, msgs);
			case GovernancePackage.GOVERNANCE_SYSTEM__HOLDERS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getHolders()).basicAdd(otherEnd, msgs);
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
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICY_DEFINITIONS:
				return ((InternalEList<?>)getPolicyDefinitions()).basicRemove(otherEnd, msgs);
			case GovernancePackage.GOVERNANCE_SYSTEM__MAPPINGS:
				return ((InternalEList<?>)getMappings()).basicRemove(otherEnd, msgs);
			case GovernancePackage.GOVERNANCE_SYSTEM__FEATURES:
				return ((InternalEList<?>)getFeatures()).basicRemove(otherEnd, msgs);
			case GovernancePackage.GOVERNANCE_SYSTEM__NAMESPACES:
				return ((InternalEList<?>)getNamespaces()).basicRemove(otherEnd, msgs);
			case GovernancePackage.GOVERNANCE_SYSTEM__SUB_SYSTEMS:
				return ((InternalEList<?>)getSubSystems()).basicRemove(otherEnd, msgs);
			case GovernancePackage.GOVERNANCE_SYSTEM__HOLDERS:
				return ((InternalEList<?>)getHolders()).basicRemove(otherEnd, msgs);
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
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICY_DEFINITIONS:
				return getPolicyDefinitions();
			case GovernancePackage.GOVERNANCE_SYSTEM__MAPPINGS:
				return getMappings();
			case GovernancePackage.GOVERNANCE_SYSTEM__FEATURES:
				return getFeatures();
			case GovernancePackage.GOVERNANCE_SYSTEM__NAMESPACES:
				return getNamespaces();
			case GovernancePackage.GOVERNANCE_SYSTEM__NAME:
				return getName();
			case GovernancePackage.GOVERNANCE_SYSTEM__DESCRIPTION:
				return getDescription();
			case GovernancePackage.GOVERNANCE_SYSTEM__SYSTEM_ID:
				return getSystemId();
			case GovernancePackage.GOVERNANCE_SYSTEM__SUB_SYSTEMS:
				return getSubSystems();
			case GovernancePackage.GOVERNANCE_SYSTEM__REFERENCED_SYSTEMS:
				return getReferencedSystems();
			case GovernancePackage.GOVERNANCE_SYSTEM__COMPONENT_HOLDERS:
				return getComponentHolders();
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICY_HOLDERS:
				return getPolicyHolders();
			case GovernancePackage.GOVERNANCE_SYSTEM__HOLDERS:
				return getHolders();
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICIES:
				return getPolicies();
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
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICY_DEFINITIONS:
				getPolicyDefinitions().clear();
				getPolicyDefinitions().addAll((Collection<? extends Policy>)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__MAPPINGS:
				getMappings().clear();
				getMappings().addAll((Collection<? extends FeatureRequirementMapping>)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__FEATURES:
				getFeatures().clear();
				getFeatures().addAll((Collection<? extends PolicyFeature>)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__NAMESPACES:
				getNamespaces().clear();
				getNamespaces().addAll((Collection<? extends GovernanceNamespace>)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__NAME:
				setName((String)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__SYSTEM_ID:
				setSystemId((String)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__SUB_SYSTEMS:
				getSubSystems().clear();
				getSubSystems().addAll((Collection<? extends GovernanceSystem>)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__REFERENCED_SYSTEMS:
				getReferencedSystems().clear();
				getReferencedSystems().addAll((Collection<? extends GovernanceSystem>)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__COMPONENT_HOLDERS:
				getComponentHolders().clear();
				getComponentHolders().addAll((Collection<? extends SystemComponentHolder>)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICY_HOLDERS:
				getPolicyHolders().clear();
				getPolicyHolders().addAll((Collection<? extends PolicyHolder>)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__HOLDERS:
				getHolders().clear();
				getHolders().addAll((Collection<? extends SystemHolder>)newValue);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICIES:
				getPolicies().clear();
				getPolicies().addAll((Collection<? extends Policy>)newValue);
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
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICY_DEFINITIONS:
				getPolicyDefinitions().clear();
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__MAPPINGS:
				getMappings().clear();
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__FEATURES:
				getFeatures().clear();
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__NAMESPACES:
				getNamespaces().clear();
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__NAME:
				setName(NAME_EDEFAULT);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__SYSTEM_ID:
				setSystemId(SYSTEM_ID_EDEFAULT);
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__SUB_SYSTEMS:
				getSubSystems().clear();
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__REFERENCED_SYSTEMS:
				getReferencedSystems().clear();
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__COMPONENT_HOLDERS:
				getComponentHolders().clear();
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICY_HOLDERS:
				getPolicyHolders().clear();
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__HOLDERS:
				getHolders().clear();
				return;
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICIES:
				getPolicies().clear();
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
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICY_DEFINITIONS:
				return policyDefinitions != null && !policyDefinitions.isEmpty();
			case GovernancePackage.GOVERNANCE_SYSTEM__MAPPINGS:
				return mappings != null && !mappings.isEmpty();
			case GovernancePackage.GOVERNANCE_SYSTEM__FEATURES:
				return features != null && !features.isEmpty();
			case GovernancePackage.GOVERNANCE_SYSTEM__NAMESPACES:
				return namespaces != null && !namespaces.isEmpty();
			case GovernancePackage.GOVERNANCE_SYSTEM__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case GovernancePackage.GOVERNANCE_SYSTEM__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case GovernancePackage.GOVERNANCE_SYSTEM__SYSTEM_ID:
				return SYSTEM_ID_EDEFAULT == null ? systemId != null : !SYSTEM_ID_EDEFAULT.equals(systemId);
			case GovernancePackage.GOVERNANCE_SYSTEM__SUB_SYSTEMS:
				return subSystems != null && !subSystems.isEmpty();
			case GovernancePackage.GOVERNANCE_SYSTEM__REFERENCED_SYSTEMS:
				return referencedSystems != null && !referencedSystems.isEmpty();
			case GovernancePackage.GOVERNANCE_SYSTEM__COMPONENT_HOLDERS:
				return !getComponentHolders().isEmpty();
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICY_HOLDERS:
				return !getPolicyHolders().isEmpty();
			case GovernancePackage.GOVERNANCE_SYSTEM__HOLDERS:
				return holders != null && !holders.isEmpty();
			case GovernancePackage.GOVERNANCE_SYSTEM__POLICIES:
				return policies != null && !policies.isEmpty();
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
		if (baseClass == PolicyHolder.class) {
			switch (derivedFeatureID) {
				case GovernancePackage.GOVERNANCE_SYSTEM__POLICY_DEFINITIONS: return GovernancePackage.POLICY_HOLDER__POLICY_DEFINITIONS;
				case GovernancePackage.GOVERNANCE_SYSTEM__MAPPINGS: return GovernancePackage.POLICY_HOLDER__MAPPINGS;
				default: return -1;
			}
		}
		if (baseClass == FeatureHolder.class) {
			switch (derivedFeatureID) {
				case GovernancePackage.GOVERNANCE_SYSTEM__FEATURES: return GovernancePackage.FEATURE_HOLDER__FEATURES;
				default: return -1;
			}
		}
		if (baseClass == NamespaceHolder.class) {
			switch (derivedFeatureID) {
				case GovernancePackage.GOVERNANCE_SYSTEM__NAMESPACES: return GovernancePackage.NAMESPACE_HOLDER__NAMESPACES;
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
		if (baseClass == PolicyHolder.class) {
			switch (baseFeatureID) {
				case GovernancePackage.POLICY_HOLDER__POLICY_DEFINITIONS: return GovernancePackage.GOVERNANCE_SYSTEM__POLICY_DEFINITIONS;
				case GovernancePackage.POLICY_HOLDER__MAPPINGS: return GovernancePackage.GOVERNANCE_SYSTEM__MAPPINGS;
				default: return -1;
			}
		}
		if (baseClass == FeatureHolder.class) {
			switch (baseFeatureID) {
				case GovernancePackage.FEATURE_HOLDER__FEATURES: return GovernancePackage.GOVERNANCE_SYSTEM__FEATURES;
				default: return -1;
			}
		}
		if (baseClass == NamespaceHolder.class) {
			switch (baseFeatureID) {
				case GovernancePackage.NAMESPACE_HOLDER__NAMESPACES: return GovernancePackage.GOVERNANCE_SYSTEM__NAMESPACES;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
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
		result.append(", systemId: ");
		result.append(systemId);
		result.append(')');
		return result.toString();
	}

} //GovernanceSystemImpl
