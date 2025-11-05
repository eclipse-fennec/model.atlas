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
package org.gecko.mac.policy.iso27001.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.gecko.mac.governance.impl.ComplianceCheckResultImpl;

import org.gecko.mac.policy.iso27001.ISO27001PolicyCheck;
import org.gecko.mac.policy.iso27001.ISO27Package;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>ISO27001 Policy Check</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.policy.iso27001.impl.ISO27001PolicyCheckImpl#isNetworkControlImplemented <em>Network Control Implemented</em>}</li>
 *   <li>{@link org.gecko.mac.policy.iso27001.impl.ISO27001PolicyCheckImpl#isAccessControlVerified <em>Access Control Verified</em>}</li>
 *   <li>{@link org.gecko.mac.policy.iso27001.impl.ISO27001PolicyCheckImpl#isLoggingAndMonitoringActive <em>Logging And Monitoring Active</em>}</li>
 *   <li>{@link org.gecko.mac.policy.iso27001.impl.ISO27001PolicyCheckImpl#isAssetManagementCompliant <em>Asset Management Compliant</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ISO27001PolicyCheckImpl extends ComplianceCheckResultImpl implements ISO27001PolicyCheck {
	/**
	 * The default value of the '{@link #isNetworkControlImplemented() <em>Network Control Implemented</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNetworkControlImplemented()
	 * @generated
	 * @ordered
	 */
	protected static final boolean NETWORK_CONTROL_IMPLEMENTED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isNetworkControlImplemented() <em>Network Control Implemented</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNetworkControlImplemented()
	 * @generated
	 * @ordered
	 */
	protected boolean networkControlImplemented = NETWORK_CONTROL_IMPLEMENTED_EDEFAULT;

	/**
	 * The default value of the '{@link #isAccessControlVerified() <em>Access Control Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAccessControlVerified()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ACCESS_CONTROL_VERIFIED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isAccessControlVerified() <em>Access Control Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAccessControlVerified()
	 * @generated
	 * @ordered
	 */
	protected boolean accessControlVerified = ACCESS_CONTROL_VERIFIED_EDEFAULT;

	/**
	 * The default value of the '{@link #isLoggingAndMonitoringActive() <em>Logging And Monitoring Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isLoggingAndMonitoringActive()
	 * @generated
	 * @ordered
	 */
	protected static final boolean LOGGING_AND_MONITORING_ACTIVE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isLoggingAndMonitoringActive() <em>Logging And Monitoring Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isLoggingAndMonitoringActive()
	 * @generated
	 * @ordered
	 */
	protected boolean loggingAndMonitoringActive = LOGGING_AND_MONITORING_ACTIVE_EDEFAULT;

	/**
	 * The default value of the '{@link #isAssetManagementCompliant() <em>Asset Management Compliant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAssetManagementCompliant()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ASSET_MANAGEMENT_COMPLIANT_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isAssetManagementCompliant() <em>Asset Management Compliant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAssetManagementCompliant()
	 * @generated
	 * @ordered
	 */
	protected boolean assetManagementCompliant = ASSET_MANAGEMENT_COMPLIANT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ISO27001PolicyCheckImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ISO27Package.Literals.ISO27001_POLICY_CHECK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isNetworkControlImplemented() {
		return networkControlImplemented;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNetworkControlImplemented(boolean newNetworkControlImplemented) {
		boolean oldNetworkControlImplemented = networkControlImplemented;
		networkControlImplemented = newNetworkControlImplemented;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ISO27Package.ISO27001_POLICY_CHECK__NETWORK_CONTROL_IMPLEMENTED, oldNetworkControlImplemented, networkControlImplemented));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAccessControlVerified() {
		return accessControlVerified;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAccessControlVerified(boolean newAccessControlVerified) {
		boolean oldAccessControlVerified = accessControlVerified;
		accessControlVerified = newAccessControlVerified;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ISO27Package.ISO27001_POLICY_CHECK__ACCESS_CONTROL_VERIFIED, oldAccessControlVerified, accessControlVerified));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isLoggingAndMonitoringActive() {
		return loggingAndMonitoringActive;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLoggingAndMonitoringActive(boolean newLoggingAndMonitoringActive) {
		boolean oldLoggingAndMonitoringActive = loggingAndMonitoringActive;
		loggingAndMonitoringActive = newLoggingAndMonitoringActive;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ISO27Package.ISO27001_POLICY_CHECK__LOGGING_AND_MONITORING_ACTIVE, oldLoggingAndMonitoringActive, loggingAndMonitoringActive));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAssetManagementCompliant() {
		return assetManagementCompliant;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAssetManagementCompliant(boolean newAssetManagementCompliant) {
		boolean oldAssetManagementCompliant = assetManagementCompliant;
		assetManagementCompliant = newAssetManagementCompliant;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ISO27Package.ISO27001_POLICY_CHECK__ASSET_MANAGEMENT_COMPLIANT, oldAssetManagementCompliant, assetManagementCompliant));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ISO27Package.ISO27001_POLICY_CHECK__NETWORK_CONTROL_IMPLEMENTED:
				return isNetworkControlImplemented();
			case ISO27Package.ISO27001_POLICY_CHECK__ACCESS_CONTROL_VERIFIED:
				return isAccessControlVerified();
			case ISO27Package.ISO27001_POLICY_CHECK__LOGGING_AND_MONITORING_ACTIVE:
				return isLoggingAndMonitoringActive();
			case ISO27Package.ISO27001_POLICY_CHECK__ASSET_MANAGEMENT_COMPLIANT:
				return isAssetManagementCompliant();
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
			case ISO27Package.ISO27001_POLICY_CHECK__NETWORK_CONTROL_IMPLEMENTED:
				setNetworkControlImplemented((Boolean)newValue);
				return;
			case ISO27Package.ISO27001_POLICY_CHECK__ACCESS_CONTROL_VERIFIED:
				setAccessControlVerified((Boolean)newValue);
				return;
			case ISO27Package.ISO27001_POLICY_CHECK__LOGGING_AND_MONITORING_ACTIVE:
				setLoggingAndMonitoringActive((Boolean)newValue);
				return;
			case ISO27Package.ISO27001_POLICY_CHECK__ASSET_MANAGEMENT_COMPLIANT:
				setAssetManagementCompliant((Boolean)newValue);
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
			case ISO27Package.ISO27001_POLICY_CHECK__NETWORK_CONTROL_IMPLEMENTED:
				setNetworkControlImplemented(NETWORK_CONTROL_IMPLEMENTED_EDEFAULT);
				return;
			case ISO27Package.ISO27001_POLICY_CHECK__ACCESS_CONTROL_VERIFIED:
				setAccessControlVerified(ACCESS_CONTROL_VERIFIED_EDEFAULT);
				return;
			case ISO27Package.ISO27001_POLICY_CHECK__LOGGING_AND_MONITORING_ACTIVE:
				setLoggingAndMonitoringActive(LOGGING_AND_MONITORING_ACTIVE_EDEFAULT);
				return;
			case ISO27Package.ISO27001_POLICY_CHECK__ASSET_MANAGEMENT_COMPLIANT:
				setAssetManagementCompliant(ASSET_MANAGEMENT_COMPLIANT_EDEFAULT);
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
			case ISO27Package.ISO27001_POLICY_CHECK__NETWORK_CONTROL_IMPLEMENTED:
				return networkControlImplemented != NETWORK_CONTROL_IMPLEMENTED_EDEFAULT;
			case ISO27Package.ISO27001_POLICY_CHECK__ACCESS_CONTROL_VERIFIED:
				return accessControlVerified != ACCESS_CONTROL_VERIFIED_EDEFAULT;
			case ISO27Package.ISO27001_POLICY_CHECK__LOGGING_AND_MONITORING_ACTIVE:
				return loggingAndMonitoringActive != LOGGING_AND_MONITORING_ACTIVE_EDEFAULT;
			case ISO27Package.ISO27001_POLICY_CHECK__ASSET_MANAGEMENT_COMPLIANT:
				return assetManagementCompliant != ASSET_MANAGEMENT_COMPLIANT_EDEFAULT;
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
		result.append(" (networkControlImplemented: ");
		result.append(networkControlImplemented);
		result.append(", accessControlVerified: ");
		result.append(accessControlVerified);
		result.append(", loggingAndMonitoringActive: ");
		result.append(loggingAndMonitoringActive);
		result.append(", assetManagementCompliant: ");
		result.append(assetManagementCompliant);
		result.append(')');
		return result.toString();
	}

} //ISO27001PolicyCheckImpl
