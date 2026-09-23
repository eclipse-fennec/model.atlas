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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.management.impl;

import java.time.Instant;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Diagnostic Change</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticChangeImpl#getChangeTime <em>Change Time</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticChangeImpl#getChangedBy <em>Changed By</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticChangeImpl#getOldSeverity <em>Old Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticChangeImpl#getNewSeverity <em>New Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticChangeImpl#getOldStatus <em>Old Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticChangeImpl#getNewStatus <em>New Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticChangeImpl#getReason <em>Reason</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DiagnosticChangeImpl extends MinimalEObjectImpl.Container implements DiagnosticChange {
	/**
	 * The default value of the '{@link #getChangeTime() <em>Change Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangeTime()
	 * @generated
	 * @ordered
	 */
	protected static final Instant CHANGE_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getChangeTime() <em>Change Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangeTime()
	 * @generated
	 * @ordered
	 */
	protected Instant changeTime = CHANGE_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getChangedBy() <em>Changed By</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangedBy()
	 * @generated
	 * @ordered
	 */
	protected static final String CHANGED_BY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getChangedBy() <em>Changed By</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChangedBy()
	 * @generated
	 * @ordered
	 */
	protected String changedBy = CHANGED_BY_EDEFAULT;

	/**
	 * The default value of the '{@link #getOldSeverity() <em>Old Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOldSeverity()
	 * @generated
	 * @ordered
	 */
	protected static final DiagnosticSeverity OLD_SEVERITY_EDEFAULT = DiagnosticSeverity.INFO;

	/**
	 * The cached value of the '{@link #getOldSeverity() <em>Old Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOldSeverity()
	 * @generated
	 * @ordered
	 */
	protected DiagnosticSeverity oldSeverity = OLD_SEVERITY_EDEFAULT;

	/**
	 * The default value of the '{@link #getNewSeverity() <em>New Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNewSeverity()
	 * @generated
	 * @ordered
	 */
	protected static final DiagnosticSeverity NEW_SEVERITY_EDEFAULT = DiagnosticSeverity.INFO;

	/**
	 * The cached value of the '{@link #getNewSeverity() <em>New Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNewSeverity()
	 * @generated
	 * @ordered
	 */
	protected DiagnosticSeverity newSeverity = NEW_SEVERITY_EDEFAULT;

	/**
	 * The default value of the '{@link #getOldStatus() <em>Old Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOldStatus()
	 * @generated
	 * @ordered
	 */
	protected static final DiagnosticStatus OLD_STATUS_EDEFAULT = DiagnosticStatus.OPEN;

	/**
	 * The cached value of the '{@link #getOldStatus() <em>Old Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOldStatus()
	 * @generated
	 * @ordered
	 */
	protected DiagnosticStatus oldStatus = OLD_STATUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getNewStatus() <em>New Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNewStatus()
	 * @generated
	 * @ordered
	 */
	protected static final DiagnosticStatus NEW_STATUS_EDEFAULT = DiagnosticStatus.OPEN;

	/**
	 * The cached value of the '{@link #getNewStatus() <em>New Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNewStatus()
	 * @generated
	 * @ordered
	 */
	protected DiagnosticStatus newStatus = NEW_STATUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getReason() <em>Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReason()
	 * @generated
	 * @ordered
	 */
	protected static final String REASON_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReason() <em>Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReason()
	 * @generated
	 * @ordered
	 */
	protected String reason = REASON_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DiagnosticChangeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ManagementPackage.Literals.DIAGNOSTIC_CHANGE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getChangeTime() {
		return changeTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setChangeTime(Instant newChangeTime) {
		Instant oldChangeTime = changeTime;
		changeTime = newChangeTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC_CHANGE__CHANGE_TIME, oldChangeTime, changeTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getChangedBy() {
		return changedBy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setChangedBy(String newChangedBy) {
		String oldChangedBy = changedBy;
		changedBy = newChangedBy;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC_CHANGE__CHANGED_BY, oldChangedBy, changedBy));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DiagnosticSeverity getOldSeverity() {
		return oldSeverity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOldSeverity(DiagnosticSeverity newOldSeverity) {
		DiagnosticSeverity oldOldSeverity = oldSeverity;
		oldSeverity = newOldSeverity == null ? OLD_SEVERITY_EDEFAULT : newOldSeverity;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC_CHANGE__OLD_SEVERITY, oldOldSeverity, oldSeverity));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DiagnosticSeverity getNewSeverity() {
		return newSeverity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNewSeverity(DiagnosticSeverity newNewSeverity) {
		DiagnosticSeverity oldNewSeverity = newSeverity;
		newSeverity = newNewSeverity == null ? NEW_SEVERITY_EDEFAULT : newNewSeverity;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC_CHANGE__NEW_SEVERITY, oldNewSeverity, newSeverity));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DiagnosticStatus getOldStatus() {
		return oldStatus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOldStatus(DiagnosticStatus newOldStatus) {
		DiagnosticStatus oldOldStatus = oldStatus;
		oldStatus = newOldStatus == null ? OLD_STATUS_EDEFAULT : newOldStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC_CHANGE__OLD_STATUS, oldOldStatus, oldStatus));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DiagnosticStatus getNewStatus() {
		return newStatus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNewStatus(DiagnosticStatus newNewStatus) {
		DiagnosticStatus oldNewStatus = newStatus;
		newStatus = newNewStatus == null ? NEW_STATUS_EDEFAULT : newNewStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC_CHANGE__NEW_STATUS, oldNewStatus, newStatus));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getReason() {
		return reason;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setReason(String newReason) {
		String oldReason = reason;
		reason = newReason;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC_CHANGE__REASON, oldReason, reason));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ManagementPackage.DIAGNOSTIC_CHANGE__CHANGE_TIME:
				return getChangeTime();
			case ManagementPackage.DIAGNOSTIC_CHANGE__CHANGED_BY:
				return getChangedBy();
			case ManagementPackage.DIAGNOSTIC_CHANGE__OLD_SEVERITY:
				return getOldSeverity();
			case ManagementPackage.DIAGNOSTIC_CHANGE__NEW_SEVERITY:
				return getNewSeverity();
			case ManagementPackage.DIAGNOSTIC_CHANGE__OLD_STATUS:
				return getOldStatus();
			case ManagementPackage.DIAGNOSTIC_CHANGE__NEW_STATUS:
				return getNewStatus();
			case ManagementPackage.DIAGNOSTIC_CHANGE__REASON:
				return getReason();
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
			case ManagementPackage.DIAGNOSTIC_CHANGE__CHANGE_TIME:
				setChangeTime((Instant)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__CHANGED_BY:
				setChangedBy((String)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__OLD_SEVERITY:
				setOldSeverity((DiagnosticSeverity)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__NEW_SEVERITY:
				setNewSeverity((DiagnosticSeverity)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__OLD_STATUS:
				setOldStatus((DiagnosticStatus)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__NEW_STATUS:
				setNewStatus((DiagnosticStatus)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__REASON:
				setReason((String)newValue);
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
			case ManagementPackage.DIAGNOSTIC_CHANGE__CHANGE_TIME:
				setChangeTime(CHANGE_TIME_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__CHANGED_BY:
				setChangedBy(CHANGED_BY_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__OLD_SEVERITY:
				setOldSeverity(OLD_SEVERITY_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__NEW_SEVERITY:
				setNewSeverity(NEW_SEVERITY_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__OLD_STATUS:
				setOldStatus(OLD_STATUS_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__NEW_STATUS:
				setNewStatus(NEW_STATUS_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC_CHANGE__REASON:
				setReason(REASON_EDEFAULT);
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
			case ManagementPackage.DIAGNOSTIC_CHANGE__CHANGE_TIME:
				return CHANGE_TIME_EDEFAULT == null ? changeTime != null : !CHANGE_TIME_EDEFAULT.equals(changeTime);
			case ManagementPackage.DIAGNOSTIC_CHANGE__CHANGED_BY:
				return CHANGED_BY_EDEFAULT == null ? changedBy != null : !CHANGED_BY_EDEFAULT.equals(changedBy);
			case ManagementPackage.DIAGNOSTIC_CHANGE__OLD_SEVERITY:
				return oldSeverity != OLD_SEVERITY_EDEFAULT;
			case ManagementPackage.DIAGNOSTIC_CHANGE__NEW_SEVERITY:
				return newSeverity != NEW_SEVERITY_EDEFAULT;
			case ManagementPackage.DIAGNOSTIC_CHANGE__OLD_STATUS:
				return oldStatus != OLD_STATUS_EDEFAULT;
			case ManagementPackage.DIAGNOSTIC_CHANGE__NEW_STATUS:
				return newStatus != NEW_STATUS_EDEFAULT;
			case ManagementPackage.DIAGNOSTIC_CHANGE__REASON:
				return REASON_EDEFAULT == null ? reason != null : !REASON_EDEFAULT.equals(reason);
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
		result.append(" (changeTime: ");
		result.append(changeTime);
		result.append(", changedBy: ");
		result.append(changedBy);
		result.append(", oldSeverity: ");
		result.append(oldSeverity);
		result.append(", newSeverity: ");
		result.append(newSeverity);
		result.append(", oldStatus: ");
		result.append(oldStatus);
		result.append(", newStatus: ");
		result.append(newStatus);
		result.append(", reason: ");
		result.append(reason);
		result.append(')');
		return result.toString();
	}

} //DiagnosticChangeImpl
