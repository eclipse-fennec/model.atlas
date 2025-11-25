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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifact;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Create Artifact</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.CreateArtifactImpl#getFirstVersion <em>First Version</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CreateArtifactImpl extends ArtifactImpl implements CreateArtifact {
	/**
	 * The cached value of the '{@link #getFirstVersion() <em>First Version</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstVersion()
	 * @generated
	 * @ordered
	 */
	protected Version firstVersion;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CreateArtifactImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MgmtApicurioPackage.Literals.CREATE_ARTIFACT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Version getFirstVersion() {
		return firstVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFirstVersion(Version newFirstVersion, NotificationChain msgs) {
		Version oldFirstVersion = firstVersion;
		firstVersion = newFirstVersion;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MgmtApicurioPackage.CREATE_ARTIFACT__FIRST_VERSION, oldFirstVersion, newFirstVersion);
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
	public void setFirstVersion(Version newFirstVersion) {
		if (newFirstVersion != firstVersion) {
			NotificationChain msgs = null;
			if (firstVersion != null)
				msgs = ((InternalEObject)firstVersion).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MgmtApicurioPackage.CREATE_ARTIFACT__FIRST_VERSION, null, msgs);
			if (newFirstVersion != null)
				msgs = ((InternalEObject)newFirstVersion).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MgmtApicurioPackage.CREATE_ARTIFACT__FIRST_VERSION, null, msgs);
			msgs = basicSetFirstVersion(newFirstVersion, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MgmtApicurioPackage.CREATE_ARTIFACT__FIRST_VERSION, newFirstVersion, newFirstVersion));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MgmtApicurioPackage.CREATE_ARTIFACT__FIRST_VERSION:
				return basicSetFirstVersion(null, msgs);
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
			case MgmtApicurioPackage.CREATE_ARTIFACT__FIRST_VERSION:
				return getFirstVersion();
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
			case MgmtApicurioPackage.CREATE_ARTIFACT__FIRST_VERSION:
				setFirstVersion((Version)newValue);
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
			case MgmtApicurioPackage.CREATE_ARTIFACT__FIRST_VERSION:
				setFirstVersion((Version)null);
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
			case MgmtApicurioPackage.CREATE_ARTIFACT__FIRST_VERSION:
				return firstVersion != null;
		}
		return super.eIsSet(featureID);
	}

} //CreateArtifactImpl
