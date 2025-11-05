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
package org.eclipse.fennec.model.atlas.policy.opendata.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.atlas.governance.impl.ComplianceCheckResultImpl;

import org.eclipse.fennec.model.atlas.policy.opendata.OpenDataLicense;
import org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPackage;
import org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPolicyCheck;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Policy Check</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPolicyCheckImpl#isPiiAnonymizationVerified <em>Pii Anonymization Verified</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPolicyCheckImpl#getLicenseType <em>License Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPolicyCheckImpl#isIsMachineReadable <em>Is Machine Readable</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPolicyCheckImpl#isHasPublicEndpoint <em>Has Public Endpoint</em>}</li>
 * </ul>
 *
 * @generated
 */
public class OpenDataPolicyCheckImpl extends ComplianceCheckResultImpl implements OpenDataPolicyCheck {
	/**
	 * The default value of the '{@link #isPiiAnonymizationVerified() <em>Pii Anonymization Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isPiiAnonymizationVerified()
	 * @generated
	 * @ordered
	 */
	protected static final boolean PII_ANONYMIZATION_VERIFIED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isPiiAnonymizationVerified() <em>Pii Anonymization Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isPiiAnonymizationVerified()
	 * @generated
	 * @ordered
	 */
	protected boolean piiAnonymizationVerified = PII_ANONYMIZATION_VERIFIED_EDEFAULT;

	/**
	 * The default value of the '{@link #getLicenseType() <em>License Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLicenseType()
	 * @generated
	 * @ordered
	 */
	protected static final OpenDataLicense LICENSE_TYPE_EDEFAULT = OpenDataLicense.NOT_APPLICABLE;

	/**
	 * The cached value of the '{@link #getLicenseType() <em>License Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLicenseType()
	 * @generated
	 * @ordered
	 */
	protected OpenDataLicense licenseType = LICENSE_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #isIsMachineReadable() <em>Is Machine Readable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsMachineReadable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IS_MACHINE_READABLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIsMachineReadable() <em>Is Machine Readable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsMachineReadable()
	 * @generated
	 * @ordered
	 */
	protected boolean isMachineReadable = IS_MACHINE_READABLE_EDEFAULT;

	/**
	 * The default value of the '{@link #isHasPublicEndpoint() <em>Has Public Endpoint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHasPublicEndpoint()
	 * @generated
	 * @ordered
	 */
	protected static final boolean HAS_PUBLIC_ENDPOINT_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isHasPublicEndpoint() <em>Has Public Endpoint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHasPublicEndpoint()
	 * @generated
	 * @ordered
	 */
	protected boolean hasPublicEndpoint = HAS_PUBLIC_ENDPOINT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OpenDataPolicyCheckImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OpenDataPackage.Literals.OPEN_DATA_POLICY_CHECK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isPiiAnonymizationVerified() {
		return piiAnonymizationVerified;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPiiAnonymizationVerified(boolean newPiiAnonymizationVerified) {
		boolean oldPiiAnonymizationVerified = piiAnonymizationVerified;
		piiAnonymizationVerified = newPiiAnonymizationVerified;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OpenDataPackage.OPEN_DATA_POLICY_CHECK__PII_ANONYMIZATION_VERIFIED, oldPiiAnonymizationVerified, piiAnonymizationVerified));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OpenDataLicense getLicenseType() {
		return licenseType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLicenseType(OpenDataLicense newLicenseType) {
		OpenDataLicense oldLicenseType = licenseType;
		licenseType = newLicenseType == null ? LICENSE_TYPE_EDEFAULT : newLicenseType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OpenDataPackage.OPEN_DATA_POLICY_CHECK__LICENSE_TYPE, oldLicenseType, licenseType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIsMachineReadable() {
		return isMachineReadable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIsMachineReadable(boolean newIsMachineReadable) {
		boolean oldIsMachineReadable = isMachineReadable;
		isMachineReadable = newIsMachineReadable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OpenDataPackage.OPEN_DATA_POLICY_CHECK__IS_MACHINE_READABLE, oldIsMachineReadable, isMachineReadable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isHasPublicEndpoint() {
		return hasPublicEndpoint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHasPublicEndpoint(boolean newHasPublicEndpoint) {
		boolean oldHasPublicEndpoint = hasPublicEndpoint;
		hasPublicEndpoint = newHasPublicEndpoint;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OpenDataPackage.OPEN_DATA_POLICY_CHECK__HAS_PUBLIC_ENDPOINT, oldHasPublicEndpoint, hasPublicEndpoint));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__PII_ANONYMIZATION_VERIFIED:
				return isPiiAnonymizationVerified();
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__LICENSE_TYPE:
				return getLicenseType();
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__IS_MACHINE_READABLE:
				return isIsMachineReadable();
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__HAS_PUBLIC_ENDPOINT:
				return isHasPublicEndpoint();
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
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__PII_ANONYMIZATION_VERIFIED:
				setPiiAnonymizationVerified((Boolean)newValue);
				return;
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__LICENSE_TYPE:
				setLicenseType((OpenDataLicense)newValue);
				return;
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__IS_MACHINE_READABLE:
				setIsMachineReadable((Boolean)newValue);
				return;
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__HAS_PUBLIC_ENDPOINT:
				setHasPublicEndpoint((Boolean)newValue);
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
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__PII_ANONYMIZATION_VERIFIED:
				setPiiAnonymizationVerified(PII_ANONYMIZATION_VERIFIED_EDEFAULT);
				return;
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__LICENSE_TYPE:
				setLicenseType(LICENSE_TYPE_EDEFAULT);
				return;
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__IS_MACHINE_READABLE:
				setIsMachineReadable(IS_MACHINE_READABLE_EDEFAULT);
				return;
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__HAS_PUBLIC_ENDPOINT:
				setHasPublicEndpoint(HAS_PUBLIC_ENDPOINT_EDEFAULT);
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
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__PII_ANONYMIZATION_VERIFIED:
				return piiAnonymizationVerified != PII_ANONYMIZATION_VERIFIED_EDEFAULT;
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__LICENSE_TYPE:
				return licenseType != LICENSE_TYPE_EDEFAULT;
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__IS_MACHINE_READABLE:
				return isMachineReadable != IS_MACHINE_READABLE_EDEFAULT;
			case OpenDataPackage.OPEN_DATA_POLICY_CHECK__HAS_PUBLIC_ENDPOINT:
				return hasPublicEndpoint != HAS_PUBLIC_ENDPOINT_EDEFAULT;
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
		result.append(" (piiAnonymizationVerified: ");
		result.append(piiAnonymizationVerified);
		result.append(", licenseType: ");
		result.append(licenseType);
		result.append(", isMachineReadable: ");
		result.append(isMachineReadable);
		result.append(", hasPublicEndpoint: ");
		result.append(hasPublicEndpoint);
		result.append(')');
		return result.toString();
	}

} //OpenDataPolicyCheckImpl
