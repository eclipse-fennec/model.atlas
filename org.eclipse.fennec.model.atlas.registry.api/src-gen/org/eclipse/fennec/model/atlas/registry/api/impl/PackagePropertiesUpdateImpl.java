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

import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.EStringToStringMapEntryImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.registry.api.PackageAddress;
import org.eclipse.fennec.model.atlas.registry.api.PackagePropertiesUpdate;
import org.eclipse.fennec.model.atlas.registry.api.Precondition;
import org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Package Properties Update</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackagePropertiesUpdateImpl#getAddress <em>Address</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackagePropertiesUpdateImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackagePropertiesUpdateImpl#getPrecondition <em>Precondition</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PackagePropertiesUpdateImpl extends MinimalEObjectImpl.Container implements PackagePropertiesUpdate {
	/**
	 * The cached value of the '{@link #getAddress() <em>Address</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAddress()
	 * @generated
	 * @ordered
	 */
	protected PackageAddress address;

	/**
	 * The cached value of the '{@link #getProperties() <em>Properties</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProperties()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, String> properties;

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
	protected PackagePropertiesUpdateImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegistryApiPackage.Literals.PACKAGE_PROPERTIES_UPDATE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PackageAddress getAddress() {
		return address;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAddress(PackageAddress newAddress, NotificationChain msgs) {
		PackageAddress oldAddress = address;
		address = newAddress;
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAddress(PackageAddress newAddress) {
		if (newAddress != address) {
			NotificationChain msgs = null;
			if (address != null)
				msgs = ((InternalEObject)address).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__ADDRESS, null, msgs);
			if (newAddress != null)
				msgs = ((InternalEObject)newAddress).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__ADDRESS, null, msgs);
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
	public EMap<String, String> getProperties() {
		if (properties == null) {
			properties = new EcoreEMap<String,String>(EcorePackage.Literals.ESTRING_TO_STRING_MAP_ENTRY, EStringToStringMapEntryImpl.class, this, RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PROPERTIES);
		}
		return properties;
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
				msgs = ((InternalEObject)precondition).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PRECONDITION, null, msgs);
			if (newPrecondition != null)
				msgs = ((InternalEObject)newPrecondition).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PRECONDITION, null, msgs);
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
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__ADDRESS:
				return basicSetAddress(null, msgs);
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PROPERTIES:
				return ((InternalEList<?>)getProperties()).basicRemove(otherEnd, msgs);
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PRECONDITION:
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
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__ADDRESS:
				return getAddress();
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PROPERTIES:
				if (coreType) return getProperties();
				else return getProperties().map();
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PRECONDITION:
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
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__ADDRESS:
				setAddress((PackageAddress)newValue);
				return;
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PROPERTIES:
				((EStructuralFeature.Setting)getProperties()).set(newValue);
				return;
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PRECONDITION:
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
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__ADDRESS:
				setAddress((PackageAddress)null);
				return;
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PROPERTIES:
				getProperties().clear();
				return;
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PRECONDITION:
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
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__ADDRESS:
				return address != null;
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PROPERTIES:
				return properties != null && !properties.isEmpty();
			case RegistryApiPackage.PACKAGE_PROPERTIES_UPDATE__PRECONDITION:
				return precondition != null;
		}
		return super.eIsSet(featureID);
	}

} //PackagePropertiesUpdateImpl
