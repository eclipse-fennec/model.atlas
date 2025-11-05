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
import java.util.Date;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.gecko.mac.governance.ComplianceCheckResult;
import org.gecko.mac.governance.ComplianceStatus;
import org.gecko.mac.governance.Finding;
import org.gecko.mac.governance.GovernancePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Compliance Check Result</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.impl.ComplianceCheckResultImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.ComplianceCheckResultImpl#getCheckTimestamp <em>Check Timestamp</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.ComplianceCheckResultImpl#getSummary <em>Summary</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.ComplianceCheckResultImpl#getFindings <em>Findings</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class ComplianceCheckResultImpl extends MinimalEObjectImpl.Container implements ComplianceCheckResult {
	/**
	 * The default value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected static final ComplianceStatus STATUS_EDEFAULT = ComplianceStatus.NOT_CHECKED;

	/**
	 * The cached value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected ComplianceStatus status = STATUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getCheckTimestamp() <em>Check Timestamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCheckTimestamp()
	 * @generated
	 * @ordered
	 */
	protected static final Date CHECK_TIMESTAMP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCheckTimestamp() <em>Check Timestamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCheckTimestamp()
	 * @generated
	 * @ordered
	 */
	protected Date checkTimestamp = CHECK_TIMESTAMP_EDEFAULT;

	/**
	 * The default value of the '{@link #getSummary() <em>Summary</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSummary()
	 * @generated
	 * @ordered
	 */
	protected static final String SUMMARY_EDEFAULT = "A short summary of the check result.";

	/**
	 * The cached value of the '{@link #getSummary() <em>Summary</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSummary()
	 * @generated
	 * @ordered
	 */
	protected String summary = SUMMARY_EDEFAULT;

	/**
	 * The cached value of the '{@link #getFindings() <em>Findings</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFindings()
	 * @generated
	 * @ordered
	 */
	protected EList<Finding> findings;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ComplianceCheckResultImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.COMPLIANCE_CHECK_RESULT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ComplianceStatus getStatus() {
		return status;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStatus(ComplianceStatus newStatus) {
		ComplianceStatus oldStatus = status;
		status = newStatus == null ? STATUS_EDEFAULT : newStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS, oldStatus, status));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Date getCheckTimestamp() {
		return checkTimestamp;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCheckTimestamp(Date newCheckTimestamp) {
		Date oldCheckTimestamp = checkTimestamp;
		checkTimestamp = newCheckTimestamp;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP, oldCheckTimestamp, checkTimestamp));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSummary() {
		return summary;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSummary(String newSummary) {
		String oldSummary = summary;
		summary = newSummary;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY, oldSummary, summary));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Finding> getFindings() {
		if (findings == null) {
			findings = new EObjectContainmentEList<Finding>(Finding.class, this, GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS);
		}
		return findings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS:
				return ((InternalEList<?>)getFindings()).basicRemove(otherEnd, msgs);
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
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS:
				return getStatus();
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP:
				return getCheckTimestamp();
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY:
				return getSummary();
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS:
				return getFindings();
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
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS:
				setStatus((ComplianceStatus)newValue);
				return;
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP:
				setCheckTimestamp((Date)newValue);
				return;
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY:
				setSummary((String)newValue);
				return;
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS:
				getFindings().clear();
				getFindings().addAll((Collection<? extends Finding>)newValue);
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
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS:
				setStatus(STATUS_EDEFAULT);
				return;
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP:
				setCheckTimestamp(CHECK_TIMESTAMP_EDEFAULT);
				return;
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY:
				setSummary(SUMMARY_EDEFAULT);
				return;
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS:
				getFindings().clear();
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
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS:
				return status != STATUS_EDEFAULT;
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP:
				return CHECK_TIMESTAMP_EDEFAULT == null ? checkTimestamp != null : !CHECK_TIMESTAMP_EDEFAULT.equals(checkTimestamp);
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY:
				return SUMMARY_EDEFAULT == null ? summary != null : !SUMMARY_EDEFAULT.equals(summary);
			case GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS:
				return findings != null && !findings.isEmpty();
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
		result.append(" (status: ");
		result.append(status);
		result.append(", checkTimestamp: ");
		result.append(checkTimestamp);
		result.append(", summary: ");
		result.append(summary);
		result.append(')');
		return result.toString();
	}

} //ComplianceCheckResultImpl
