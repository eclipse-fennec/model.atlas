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
package org.eclipse.fennec.model.atlas.policy.kritis.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.atlas.governance.impl.ComplianceCheckResultImpl;

import org.eclipse.fennec.model.atlas.policy.kritis.KRITISPolicyCheck;
import org.eclipse.fennec.model.atlas.policy.kritis.KritisPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>KRITIS Policy Check</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.kritis.impl.KRITISPolicyCheckImpl#isResilienceVerified <em>Resilience Verified</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.kritis.impl.KRITISPolicyCheckImpl#isIncidentResponseReady <em>Incident Response Ready</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.kritis.impl.KRITISPolicyCheckImpl#isSupplyChainSecure <em>Supply Chain Secure</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.kritis.impl.KRITISPolicyCheckImpl#isSystemHardeningApplied <em>System Hardening Applied</em>}</li>
 * </ul>
 *
 * @generated
 */
public class KRITISPolicyCheckImpl extends ComplianceCheckResultImpl implements KRITISPolicyCheck {
	/**
	 * The default value of the '{@link #isResilienceVerified() <em>Resilience Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isResilienceVerified()
	 * @generated
	 * @ordered
	 */
	protected static final boolean RESILIENCE_VERIFIED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isResilienceVerified() <em>Resilience Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isResilienceVerified()
	 * @generated
	 * @ordered
	 */
	protected boolean resilienceVerified = RESILIENCE_VERIFIED_EDEFAULT;

	/**
	 * The default value of the '{@link #isIncidentResponseReady() <em>Incident Response Ready</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIncidentResponseReady()
	 * @generated
	 * @ordered
	 */
	protected static final boolean INCIDENT_RESPONSE_READY_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIncidentResponseReady() <em>Incident Response Ready</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIncidentResponseReady()
	 * @generated
	 * @ordered
	 */
	protected boolean incidentResponseReady = INCIDENT_RESPONSE_READY_EDEFAULT;

	/**
	 * The default value of the '{@link #isSupplyChainSecure() <em>Supply Chain Secure</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSupplyChainSecure()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SUPPLY_CHAIN_SECURE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isSupplyChainSecure() <em>Supply Chain Secure</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSupplyChainSecure()
	 * @generated
	 * @ordered
	 */
	protected boolean supplyChainSecure = SUPPLY_CHAIN_SECURE_EDEFAULT;

	/**
	 * The default value of the '{@link #isSystemHardeningApplied() <em>System Hardening Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSystemHardeningApplied()
	 * @generated
	 * @ordered
	 */
	protected static final boolean SYSTEM_HARDENING_APPLIED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isSystemHardeningApplied() <em>System Hardening Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSystemHardeningApplied()
	 * @generated
	 * @ordered
	 */
	protected boolean systemHardeningApplied = SYSTEM_HARDENING_APPLIED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected KRITISPolicyCheckImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return KritisPackage.Literals.KRITIS_POLICY_CHECK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isResilienceVerified() {
		return resilienceVerified;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setResilienceVerified(boolean newResilienceVerified) {
		boolean oldResilienceVerified = resilienceVerified;
		resilienceVerified = newResilienceVerified;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, KritisPackage.KRITIS_POLICY_CHECK__RESILIENCE_VERIFIED, oldResilienceVerified, resilienceVerified));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIncidentResponseReady() {
		return incidentResponseReady;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIncidentResponseReady(boolean newIncidentResponseReady) {
		boolean oldIncidentResponseReady = incidentResponseReady;
		incidentResponseReady = newIncidentResponseReady;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, KritisPackage.KRITIS_POLICY_CHECK__INCIDENT_RESPONSE_READY, oldIncidentResponseReady, incidentResponseReady));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSupplyChainSecure() {
		return supplyChainSecure;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSupplyChainSecure(boolean newSupplyChainSecure) {
		boolean oldSupplyChainSecure = supplyChainSecure;
		supplyChainSecure = newSupplyChainSecure;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, KritisPackage.KRITIS_POLICY_CHECK__SUPPLY_CHAIN_SECURE, oldSupplyChainSecure, supplyChainSecure));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSystemHardeningApplied() {
		return systemHardeningApplied;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSystemHardeningApplied(boolean newSystemHardeningApplied) {
		boolean oldSystemHardeningApplied = systemHardeningApplied;
		systemHardeningApplied = newSystemHardeningApplied;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, KritisPackage.KRITIS_POLICY_CHECK__SYSTEM_HARDENING_APPLIED, oldSystemHardeningApplied, systemHardeningApplied));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case KritisPackage.KRITIS_POLICY_CHECK__RESILIENCE_VERIFIED:
				return isResilienceVerified();
			case KritisPackage.KRITIS_POLICY_CHECK__INCIDENT_RESPONSE_READY:
				return isIncidentResponseReady();
			case KritisPackage.KRITIS_POLICY_CHECK__SUPPLY_CHAIN_SECURE:
				return isSupplyChainSecure();
			case KritisPackage.KRITIS_POLICY_CHECK__SYSTEM_HARDENING_APPLIED:
				return isSystemHardeningApplied();
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
			case KritisPackage.KRITIS_POLICY_CHECK__RESILIENCE_VERIFIED:
				setResilienceVerified((Boolean)newValue);
				return;
			case KritisPackage.KRITIS_POLICY_CHECK__INCIDENT_RESPONSE_READY:
				setIncidentResponseReady((Boolean)newValue);
				return;
			case KritisPackage.KRITIS_POLICY_CHECK__SUPPLY_CHAIN_SECURE:
				setSupplyChainSecure((Boolean)newValue);
				return;
			case KritisPackage.KRITIS_POLICY_CHECK__SYSTEM_HARDENING_APPLIED:
				setSystemHardeningApplied((Boolean)newValue);
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
			case KritisPackage.KRITIS_POLICY_CHECK__RESILIENCE_VERIFIED:
				setResilienceVerified(RESILIENCE_VERIFIED_EDEFAULT);
				return;
			case KritisPackage.KRITIS_POLICY_CHECK__INCIDENT_RESPONSE_READY:
				setIncidentResponseReady(INCIDENT_RESPONSE_READY_EDEFAULT);
				return;
			case KritisPackage.KRITIS_POLICY_CHECK__SUPPLY_CHAIN_SECURE:
				setSupplyChainSecure(SUPPLY_CHAIN_SECURE_EDEFAULT);
				return;
			case KritisPackage.KRITIS_POLICY_CHECK__SYSTEM_HARDENING_APPLIED:
				setSystemHardeningApplied(SYSTEM_HARDENING_APPLIED_EDEFAULT);
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
			case KritisPackage.KRITIS_POLICY_CHECK__RESILIENCE_VERIFIED:
				return resilienceVerified != RESILIENCE_VERIFIED_EDEFAULT;
			case KritisPackage.KRITIS_POLICY_CHECK__INCIDENT_RESPONSE_READY:
				return incidentResponseReady != INCIDENT_RESPONSE_READY_EDEFAULT;
			case KritisPackage.KRITIS_POLICY_CHECK__SUPPLY_CHAIN_SECURE:
				return supplyChainSecure != SUPPLY_CHAIN_SECURE_EDEFAULT;
			case KritisPackage.KRITIS_POLICY_CHECK__SYSTEM_HARDENING_APPLIED:
				return systemHardeningApplied != SYSTEM_HARDENING_APPLIED_EDEFAULT;
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
		result.append(" (resilienceVerified: ");
		result.append(resilienceVerified);
		result.append(", incidentResponseReady: ");
		result.append(incidentResponseReady);
		result.append(", supplyChainSecure: ");
		result.append(supplyChainSecure);
		result.append(", systemHardeningApplied: ");
		result.append(systemHardeningApplied);
		result.append(')');
		return result.toString();
	}

} //KRITISPolicyCheckImpl
