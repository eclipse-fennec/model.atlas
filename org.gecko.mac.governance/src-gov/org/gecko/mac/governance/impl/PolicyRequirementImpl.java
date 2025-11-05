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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EcoreUtil;

import org.gecko.mac.governance.GovernancePackage;
import org.gecko.mac.governance.PolicyRequirement;
import org.gecko.mac.governance.RequirementGroup;
import org.gecko.mac.governance.RequirementModality;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Policy Requirement</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyRequirementImpl#getRequirementId <em>Requirement Id</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyRequirementImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyRequirementImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyRequirementImpl#getDocumentationLink <em>Documentation Link</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyRequirementImpl#getModality <em>Modality</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.PolicyRequirementImpl#getGroup <em>Group</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PolicyRequirementImpl extends MinimalEObjectImpl.Container implements PolicyRequirement {
	/**
	 * The default value of the '{@link #getRequirementId() <em>Requirement Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRequirementId()
	 * @generated
	 * @ordered
	 */
	protected static final String REQUIREMENT_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRequirementId() <em>Requirement Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRequirementId()
	 * @generated
	 * @ordered
	 */
	protected String requirementId = REQUIREMENT_ID_EDEFAULT;

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
	 * The default value of the '{@link #getDocumentationLink() <em>Documentation Link</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDocumentationLink()
	 * @generated
	 * @ordered
	 */
	protected static final String DOCUMENTATION_LINK_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDocumentationLink() <em>Documentation Link</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDocumentationLink()
	 * @generated
	 * @ordered
	 */
	protected String documentationLink = DOCUMENTATION_LINK_EDEFAULT;

	/**
	 * The default value of the '{@link #getModality() <em>Modality</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModality()
	 * @generated
	 * @ordered
	 */
	protected static final RequirementModality MODALITY_EDEFAULT = RequirementModality.MUST;

	/**
	 * The cached value of the '{@link #getModality() <em>Modality</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModality()
	 * @generated
	 * @ordered
	 */
	protected RequirementModality modality = MODALITY_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PolicyRequirementImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.POLICY_REQUIREMENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRequirementId() {
		return requirementId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRequirementId(String newRequirementId) {
		String oldRequirementId = requirementId;
		requirementId = newRequirementId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY_REQUIREMENT__REQUIREMENT_ID, oldRequirementId, requirementId));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY_REQUIREMENT__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY_REQUIREMENT__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDocumentationLink() {
		return documentationLink;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDocumentationLink(String newDocumentationLink) {
		String oldDocumentationLink = documentationLink;
		documentationLink = newDocumentationLink;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY_REQUIREMENT__DOCUMENTATION_LINK, oldDocumentationLink, documentationLink));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RequirementModality getModality() {
		return modality;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setModality(RequirementModality newModality) {
		RequirementModality oldModality = modality;
		modality = newModality == null ? MODALITY_EDEFAULT : newModality;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY_REQUIREMENT__MODALITY, oldModality, modality));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RequirementGroup getGroup() {
		if (eContainerFeatureID() != GovernancePackage.POLICY_REQUIREMENT__GROUP) return null;
		return (RequirementGroup)eInternalContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetGroup(RequirementGroup newGroup, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newGroup, GovernancePackage.POLICY_REQUIREMENT__GROUP, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setGroup(RequirementGroup newGroup) {
		if (newGroup != eInternalContainer() || (eContainerFeatureID() != GovernancePackage.POLICY_REQUIREMENT__GROUP && newGroup != null)) {
			if (EcoreUtil.isAncestor(this, newGroup))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newGroup != null)
				msgs = ((InternalEObject)newGroup).eInverseAdd(this, GovernancePackage.REQUIREMENT_GROUP__REQUIREMENTS, RequirementGroup.class, msgs);
			msgs = basicSetGroup(newGroup, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.POLICY_REQUIREMENT__GROUP, newGroup, newGroup));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case GovernancePackage.POLICY_REQUIREMENT__GROUP:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetGroup((RequirementGroup)otherEnd, msgs);
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
			case GovernancePackage.POLICY_REQUIREMENT__GROUP:
				return basicSetGroup(null, msgs);
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
			case GovernancePackage.POLICY_REQUIREMENT__GROUP:
				return eInternalContainer().eInverseRemove(this, GovernancePackage.REQUIREMENT_GROUP__REQUIREMENTS, RequirementGroup.class, msgs);
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
			case GovernancePackage.POLICY_REQUIREMENT__REQUIREMENT_ID:
				return getRequirementId();
			case GovernancePackage.POLICY_REQUIREMENT__NAME:
				return getName();
			case GovernancePackage.POLICY_REQUIREMENT__DESCRIPTION:
				return getDescription();
			case GovernancePackage.POLICY_REQUIREMENT__DOCUMENTATION_LINK:
				return getDocumentationLink();
			case GovernancePackage.POLICY_REQUIREMENT__MODALITY:
				return getModality();
			case GovernancePackage.POLICY_REQUIREMENT__GROUP:
				return getGroup();
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
			case GovernancePackage.POLICY_REQUIREMENT__REQUIREMENT_ID:
				setRequirementId((String)newValue);
				return;
			case GovernancePackage.POLICY_REQUIREMENT__NAME:
				setName((String)newValue);
				return;
			case GovernancePackage.POLICY_REQUIREMENT__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case GovernancePackage.POLICY_REQUIREMENT__DOCUMENTATION_LINK:
				setDocumentationLink((String)newValue);
				return;
			case GovernancePackage.POLICY_REQUIREMENT__MODALITY:
				setModality((RequirementModality)newValue);
				return;
			case GovernancePackage.POLICY_REQUIREMENT__GROUP:
				setGroup((RequirementGroup)newValue);
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
			case GovernancePackage.POLICY_REQUIREMENT__REQUIREMENT_ID:
				setRequirementId(REQUIREMENT_ID_EDEFAULT);
				return;
			case GovernancePackage.POLICY_REQUIREMENT__NAME:
				setName(NAME_EDEFAULT);
				return;
			case GovernancePackage.POLICY_REQUIREMENT__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case GovernancePackage.POLICY_REQUIREMENT__DOCUMENTATION_LINK:
				setDocumentationLink(DOCUMENTATION_LINK_EDEFAULT);
				return;
			case GovernancePackage.POLICY_REQUIREMENT__MODALITY:
				setModality(MODALITY_EDEFAULT);
				return;
			case GovernancePackage.POLICY_REQUIREMENT__GROUP:
				setGroup((RequirementGroup)null);
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
			case GovernancePackage.POLICY_REQUIREMENT__REQUIREMENT_ID:
				return REQUIREMENT_ID_EDEFAULT == null ? requirementId != null : !REQUIREMENT_ID_EDEFAULT.equals(requirementId);
			case GovernancePackage.POLICY_REQUIREMENT__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case GovernancePackage.POLICY_REQUIREMENT__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case GovernancePackage.POLICY_REQUIREMENT__DOCUMENTATION_LINK:
				return DOCUMENTATION_LINK_EDEFAULT == null ? documentationLink != null : !DOCUMENTATION_LINK_EDEFAULT.equals(documentationLink);
			case GovernancePackage.POLICY_REQUIREMENT__MODALITY:
				return modality != MODALITY_EDEFAULT;
			case GovernancePackage.POLICY_REQUIREMENT__GROUP:
				return getGroup() != null;
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
		result.append(" (requirementId: ");
		result.append(requirementId);
		result.append(", name: ");
		result.append(name);
		result.append(", description: ");
		result.append(description);
		result.append(", documentationLink: ");
		result.append(documentationLink);
		result.append(", modality: ");
		result.append(modality);
		result.append(')');
		return result.toString();
	}

} //PolicyRequirementImpl
