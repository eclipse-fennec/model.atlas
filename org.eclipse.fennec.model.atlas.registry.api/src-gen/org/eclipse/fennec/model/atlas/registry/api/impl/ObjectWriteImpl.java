/*
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.api.impl;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy;
import org.eclipse.fennec.model.atlas.registry.api.ObjectAddress;
import org.eclipse.fennec.model.atlas.registry.api.ObjectWrite;
import org.eclipse.fennec.model.atlas.registry.api.Precondition;
import org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Object Write</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.ObjectWriteImpl#getAddress <em>Address</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.ObjectWriteImpl#getContent <em>Content</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.ObjectWriteImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.ObjectWriteImpl#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.ObjectWriteImpl#getOnConflict <em>On Conflict</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.ObjectWriteImpl#getPrecondition <em>Precondition</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ObjectWriteImpl extends MinimalEObjectImpl.Container implements ObjectWrite {
	/**
	 * The cached value of the '{@link #getAddress() <em>Address</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAddress()
	 * @generated
	 * @ordered
	 */
	protected ObjectAddress address;

	/**
	 * The cached value of the '{@link #getContent() <em>Content</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContent()
	 * @generated
	 * @ordered
	 */
	protected EObject content;

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
	 * The default value of the '{@link #getOnConflict() <em>On Conflict</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOnConflict()
	 * @generated
	 * @ordered
	 */
	protected static final ConflictPolicy ON_CONFLICT_EDEFAULT = ConflictPolicy.FAIL;

	/**
	 * The cached value of the '{@link #getOnConflict() <em>On Conflict</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOnConflict()
	 * @generated
	 * @ordered
	 */
	protected ConflictPolicy onConflict = ON_CONFLICT_EDEFAULT;

	/**
	 * The cached value of the '{@link #getPrecondition() <em>Precondition</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrecondition()
	 * @generated
	 * @ordered
	 */
	protected Precondition precondition;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ObjectWriteImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegistryApiPackage.Literals.OBJECT_WRITE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObjectAddress getAddress() {
		return address;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAddress(ObjectAddress newAddress, NotificationChain msgs) {
		ObjectAddress oldAddress = address;
		address = newAddress;
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAddress(ObjectAddress newAddress) {
		if (newAddress != address) {
			NotificationChain msgs = null;
			if (address != null)
				msgs = ((InternalEObject)address).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegistryApiPackage.OBJECT_WRITE__ADDRESS, null, msgs);
			if (newAddress != null)
				msgs = ((InternalEObject)newAddress).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RegistryApiPackage.OBJECT_WRITE__ADDRESS, null, msgs);
			msgs = basicSetAddress(newAddress, msgs);
			if (msgs != null) msgs.dispatch();
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject getContent() {
		return content;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetContent(EObject newContent, NotificationChain msgs) {
		EObject oldContent = content;
		content = newContent;
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContent(EObject newContent) {
		if (newContent != content) {
			NotificationChain msgs = null;
			if (content != null)
				msgs = ((InternalEObject)content).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegistryApiPackage.OBJECT_WRITE__CONTENT, null, msgs);
			if (newContent != null)
				msgs = ((InternalEObject)newContent).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RegistryApiPackage.OBJECT_WRITE__CONTENT, null, msgs);
			msgs = basicSetContent(newContent, msgs);
			if (msgs != null) msgs.dispatch();
		}
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
		name = newName;
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
		version = newVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ConflictPolicy getOnConflict() {
		return onConflict;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOnConflict(ConflictPolicy newOnConflict) {
		onConflict = newOnConflict == null ? ON_CONFLICT_EDEFAULT : newOnConflict;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Precondition getPrecondition() {
		return precondition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetPrecondition(Precondition newPrecondition, NotificationChain msgs) {
		Precondition oldPrecondition = precondition;
		precondition = newPrecondition;
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPrecondition(Precondition newPrecondition) {
		if (newPrecondition != precondition) {
			NotificationChain msgs = null;
			if (precondition != null)
				msgs = ((InternalEObject)precondition).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegistryApiPackage.OBJECT_WRITE__PRECONDITION, null, msgs);
			if (newPrecondition != null)
				msgs = ((InternalEObject)newPrecondition).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RegistryApiPackage.OBJECT_WRITE__PRECONDITION, null, msgs);
			msgs = basicSetPrecondition(newPrecondition, msgs);
			if (msgs != null) msgs.dispatch();
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RegistryApiPackage.OBJECT_WRITE__ADDRESS:
				return basicSetAddress(null, msgs);
			case RegistryApiPackage.OBJECT_WRITE__CONTENT:
				return basicSetContent(null, msgs);
			case RegistryApiPackage.OBJECT_WRITE__PRECONDITION:
				return basicSetPrecondition(null, msgs);
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
			case RegistryApiPackage.OBJECT_WRITE__ADDRESS:
				return getAddress();
			case RegistryApiPackage.OBJECT_WRITE__CONTENT:
				return getContent();
			case RegistryApiPackage.OBJECT_WRITE__NAME:
				return getName();
			case RegistryApiPackage.OBJECT_WRITE__VERSION:
				return getVersion();
			case RegistryApiPackage.OBJECT_WRITE__ON_CONFLICT:
				return getOnConflict();
			case RegistryApiPackage.OBJECT_WRITE__PRECONDITION:
				return getPrecondition();
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
			case RegistryApiPackage.OBJECT_WRITE__ADDRESS:
				setAddress((ObjectAddress)newValue);
				return;
			case RegistryApiPackage.OBJECT_WRITE__CONTENT:
				setContent((EObject)newValue);
				return;
			case RegistryApiPackage.OBJECT_WRITE__NAME:
				setName((String)newValue);
				return;
			case RegistryApiPackage.OBJECT_WRITE__VERSION:
				setVersion((String)newValue);
				return;
			case RegistryApiPackage.OBJECT_WRITE__ON_CONFLICT:
				setOnConflict((ConflictPolicy)newValue);
				return;
			case RegistryApiPackage.OBJECT_WRITE__PRECONDITION:
				setPrecondition((Precondition)newValue);
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
			case RegistryApiPackage.OBJECT_WRITE__ADDRESS:
				setAddress((ObjectAddress)null);
				return;
			case RegistryApiPackage.OBJECT_WRITE__CONTENT:
				setContent((EObject)null);
				return;
			case RegistryApiPackage.OBJECT_WRITE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case RegistryApiPackage.OBJECT_WRITE__VERSION:
				setVersion(VERSION_EDEFAULT);
				return;
			case RegistryApiPackage.OBJECT_WRITE__ON_CONFLICT:
				setOnConflict(ON_CONFLICT_EDEFAULT);
				return;
			case RegistryApiPackage.OBJECT_WRITE__PRECONDITION:
				setPrecondition((Precondition)null);
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
			case RegistryApiPackage.OBJECT_WRITE__ADDRESS:
				return address != null;
			case RegistryApiPackage.OBJECT_WRITE__CONTENT:
				return content != null;
			case RegistryApiPackage.OBJECT_WRITE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case RegistryApiPackage.OBJECT_WRITE__VERSION:
				return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
			case RegistryApiPackage.OBJECT_WRITE__ON_CONFLICT:
				return onConflict != ON_CONFLICT_EDEFAULT;
			case RegistryApiPackage.OBJECT_WRITE__PRECONDITION:
				return precondition != null;
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
		result.append(", version: ");
		result.append(version);
		result.append(", onConflict: ");
		result.append(onConflict);
		result.append(')');
		return result.toString();
	}

} //ObjectWriteImpl
