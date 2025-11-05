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
import org.eclipse.emf.ecore.util.InternalEList;

import org.gecko.mac.governance.GovernanceNamespace;
import org.gecko.mac.governance.GovernancePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Namespace</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceNamespaceImpl#getNamespaceId <em>Namespace Id</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceNamespaceImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceNamespaceImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceNamespaceImpl#getVersion <em>Version</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceNamespaceImpl#getStandardReference <em>Standard Reference</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceNamespaceImpl#getParentNamespace <em>Parent Namespace</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.GovernanceNamespaceImpl#getChildNamespaces <em>Child Namespaces</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GovernanceNamespaceImpl extends MinimalEObjectImpl.Container implements GovernanceNamespace {
	/**
	 * The default value of the '{@link #getNamespaceId() <em>Namespace Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespaceId()
	 * @generated
	 * @ordered
	 */
	protected static final String NAMESPACE_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNamespaceId() <em>Namespace Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespaceId()
	 * @generated
	 * @ordered
	 */
	protected String namespaceId = NAMESPACE_ID_EDEFAULT;

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
	 * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected static final String VERSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected String version = VERSION_EDEFAULT;

	/**
	 * The default value of the '{@link #getStandardReference() <em>Standard Reference</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStandardReference()
	 * @generated
	 * @ordered
	 */
	protected static final String STANDARD_REFERENCE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getStandardReference() <em>Standard Reference</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStandardReference()
	 * @generated
	 * @ordered
	 */
	protected String standardReference = STANDARD_REFERENCE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParentNamespace() <em>Parent Namespace</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParentNamespace()
	 * @generated
	 * @ordered
	 */
	protected GovernanceNamespace parentNamespace;

	/**
	 * The cached value of the '{@link #getChildNamespaces() <em>Child Namespaces</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildNamespaces()
	 * @generated
	 * @ordered
	 */
	protected EList<GovernanceNamespace> childNamespaces;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GovernanceNamespaceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.GOVERNANCE_NAMESPACE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNamespaceId() {
		return namespaceId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNamespaceId(String newNamespaceId) {
		String oldNamespaceId = namespaceId;
		namespaceId = newNamespaceId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.GOVERNANCE_NAMESPACE__NAMESPACE_ID, oldNamespaceId, namespaceId));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.GOVERNANCE_NAMESPACE__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.GOVERNANCE_NAMESPACE__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getVersion() {
		return version;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setVersion(String newVersion) {
		String oldVersion = version;
		version = newVersion;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.GOVERNANCE_NAMESPACE__VERSION, oldVersion, version));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getStandardReference() {
		return standardReference;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStandardReference(String newStandardReference) {
		String oldStandardReference = standardReference;
		standardReference = newStandardReference;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.GOVERNANCE_NAMESPACE__STANDARD_REFERENCE, oldStandardReference, standardReference));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GovernanceNamespace getParentNamespace() {
		if (parentNamespace != null && parentNamespace.eIsProxy()) {
			InternalEObject oldParentNamespace = (InternalEObject)parentNamespace;
			parentNamespace = (GovernanceNamespace)eResolveProxy(oldParentNamespace);
			if (parentNamespace != oldParentNamespace) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GovernancePackage.GOVERNANCE_NAMESPACE__PARENT_NAMESPACE, oldParentNamespace, parentNamespace));
			}
		}
		return parentNamespace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GovernanceNamespace basicGetParentNamespace() {
		return parentNamespace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetParentNamespace(GovernanceNamespace newParentNamespace, NotificationChain msgs) {
		GovernanceNamespace oldParentNamespace = parentNamespace;
		parentNamespace = newParentNamespace;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GovernancePackage.GOVERNANCE_NAMESPACE__PARENT_NAMESPACE, oldParentNamespace, newParentNamespace);
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
	public void setParentNamespace(GovernanceNamespace newParentNamespace) {
		if (newParentNamespace != parentNamespace) {
			NotificationChain msgs = null;
			if (parentNamespace != null)
				msgs = ((InternalEObject)parentNamespace).eInverseRemove(this, GovernancePackage.GOVERNANCE_NAMESPACE__CHILD_NAMESPACES, GovernanceNamespace.class, msgs);
			if (newParentNamespace != null)
				msgs = ((InternalEObject)newParentNamespace).eInverseAdd(this, GovernancePackage.GOVERNANCE_NAMESPACE__CHILD_NAMESPACES, GovernanceNamespace.class, msgs);
			msgs = basicSetParentNamespace(newParentNamespace, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.GOVERNANCE_NAMESPACE__PARENT_NAMESPACE, newParentNamespace, newParentNamespace));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<GovernanceNamespace> getChildNamespaces() {
		if (childNamespaces == null) {
			childNamespaces = new EObjectWithInverseResolvingEList<GovernanceNamespace>(GovernanceNamespace.class, this, GovernancePackage.GOVERNANCE_NAMESPACE__CHILD_NAMESPACES, GovernancePackage.GOVERNANCE_NAMESPACE__PARENT_NAMESPACE);
		}
		return childNamespaces;
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
			case GovernancePackage.GOVERNANCE_NAMESPACE__PARENT_NAMESPACE:
				if (parentNamespace != null)
					msgs = ((InternalEObject)parentNamespace).eInverseRemove(this, GovernancePackage.GOVERNANCE_NAMESPACE__CHILD_NAMESPACES, GovernanceNamespace.class, msgs);
				return basicSetParentNamespace((GovernanceNamespace)otherEnd, msgs);
			case GovernancePackage.GOVERNANCE_NAMESPACE__CHILD_NAMESPACES:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getChildNamespaces()).basicAdd(otherEnd, msgs);
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
			case GovernancePackage.GOVERNANCE_NAMESPACE__PARENT_NAMESPACE:
				return basicSetParentNamespace(null, msgs);
			case GovernancePackage.GOVERNANCE_NAMESPACE__CHILD_NAMESPACES:
				return ((InternalEList<?>)getChildNamespaces()).basicRemove(otherEnd, msgs);
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
			case GovernancePackage.GOVERNANCE_NAMESPACE__NAMESPACE_ID:
				return getNamespaceId();
			case GovernancePackage.GOVERNANCE_NAMESPACE__NAME:
				return getName();
			case GovernancePackage.GOVERNANCE_NAMESPACE__DESCRIPTION:
				return getDescription();
			case GovernancePackage.GOVERNANCE_NAMESPACE__VERSION:
				return getVersion();
			case GovernancePackage.GOVERNANCE_NAMESPACE__STANDARD_REFERENCE:
				return getStandardReference();
			case GovernancePackage.GOVERNANCE_NAMESPACE__PARENT_NAMESPACE:
				if (resolve) return getParentNamespace();
				return basicGetParentNamespace();
			case GovernancePackage.GOVERNANCE_NAMESPACE__CHILD_NAMESPACES:
				return getChildNamespaces();
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
			case GovernancePackage.GOVERNANCE_NAMESPACE__NAMESPACE_ID:
				setNamespaceId((String)newValue);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__NAME:
				setName((String)newValue);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__VERSION:
				setVersion((String)newValue);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__STANDARD_REFERENCE:
				setStandardReference((String)newValue);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__PARENT_NAMESPACE:
				setParentNamespace((GovernanceNamespace)newValue);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__CHILD_NAMESPACES:
				getChildNamespaces().clear();
				getChildNamespaces().addAll((Collection<? extends GovernanceNamespace>)newValue);
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
			case GovernancePackage.GOVERNANCE_NAMESPACE__NAMESPACE_ID:
				setNamespaceId(NAMESPACE_ID_EDEFAULT);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__VERSION:
				setVersion(VERSION_EDEFAULT);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__STANDARD_REFERENCE:
				setStandardReference(STANDARD_REFERENCE_EDEFAULT);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__PARENT_NAMESPACE:
				setParentNamespace((GovernanceNamespace)null);
				return;
			case GovernancePackage.GOVERNANCE_NAMESPACE__CHILD_NAMESPACES:
				getChildNamespaces().clear();
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
			case GovernancePackage.GOVERNANCE_NAMESPACE__NAMESPACE_ID:
				return NAMESPACE_ID_EDEFAULT == null ? namespaceId != null : !NAMESPACE_ID_EDEFAULT.equals(namespaceId);
			case GovernancePackage.GOVERNANCE_NAMESPACE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case GovernancePackage.GOVERNANCE_NAMESPACE__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case GovernancePackage.GOVERNANCE_NAMESPACE__VERSION:
				return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
			case GovernancePackage.GOVERNANCE_NAMESPACE__STANDARD_REFERENCE:
				return STANDARD_REFERENCE_EDEFAULT == null ? standardReference != null : !STANDARD_REFERENCE_EDEFAULT.equals(standardReference);
			case GovernancePackage.GOVERNANCE_NAMESPACE__PARENT_NAMESPACE:
				return parentNamespace != null;
			case GovernancePackage.GOVERNANCE_NAMESPACE__CHILD_NAMESPACES:
				return childNamespaces != null && !childNamespaces.isEmpty();
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
		result.append(" (namespaceId: ");
		result.append(namespaceId);
		result.append(", name: ");
		result.append(name);
		result.append(", description: ");
		result.append(description);
		result.append(", version: ");
		result.append(version);
		result.append(", standardReference: ");
		result.append(standardReference);
		result.append(')');
		return result.toString();
	}

} //GovernanceNamespaceImpl
