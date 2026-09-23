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
package org.eclipse.fennec.model.atlas.mgmt.management;

import java.time.Instant;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Diagnostic Change</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One entry in a Diagnostic's history: a change of severity or status, or a
 * re-validation that confirmed the finding, with who made it, when and why. Old and
 * new values are recorded per field so a reader can follow the diagnostic's life
 * without diffing; an unchanged field carries the same value on both sides.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getChangeTime <em>Change Time</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getChangedBy <em>Changed By</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getOldSeverity <em>Old Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getNewSeverity <em>New Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getOldStatus <em>Old Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getNewStatus <em>New Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getReason <em>Reason</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnosticChange()
 * @model
 * @generated
 */
@ProviderType
public interface DiagnosticChange extends EObject {
	/**
	 * Returns the value of the '<em><b>Change Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When the change was made
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Change Time</em>' attribute.
	 * @see #setChangeTime(Instant)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnosticChange_ChangeTime()
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Instant" required="true"
	 * @generated
	 */
	Instant getChangeTime();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getChangeTime <em>Change Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Change Time</em>' attribute.
	 * @see #getChangeTime()
	 * @generated
	 */
	void setChangeTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Changed By</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Who made the change: a user name for a manual decision (acknowledge, resolve),
	 * the producer or module name for an automatic one (re-validation, escalation).
	 * Whether a change was manual matters, because a manual status is protected
	 * against automatic reverts (#293).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Changed By</em>' attribute.
	 * @see #setChangedBy(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnosticChange_ChangedBy()
	 * @model required="true"
	 * @generated
	 */
	String getChangedBy();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getChangedBy <em>Changed By</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Changed By</em>' attribute.
	 * @see #getChangedBy()
	 * @generated
	 */
	void setChangedBy(String value);

	/**
	 * Returns the value of the '<em><b>Old Severity</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Severity before the change
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Old Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity
	 * @see #setOldSeverity(DiagnosticSeverity)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnosticChange_OldSeverity()
	 * @model
	 * @generated
	 */
	DiagnosticSeverity getOldSeverity();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getOldSeverity <em>Old Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Old Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity
	 * @see #getOldSeverity()
	 * @generated
	 */
	void setOldSeverity(DiagnosticSeverity value);

	/**
	 * Returns the value of the '<em><b>New Severity</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Severity after the change
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>New Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity
	 * @see #setNewSeverity(DiagnosticSeverity)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnosticChange_NewSeverity()
	 * @model
	 * @generated
	 */
	DiagnosticSeverity getNewSeverity();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getNewSeverity <em>New Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>New Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity
	 * @see #getNewSeverity()
	 * @generated
	 */
	void setNewSeverity(DiagnosticSeverity value);

	/**
	 * Returns the value of the '<em><b>Old Status</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Status before the change
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Old Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus
	 * @see #setOldStatus(DiagnosticStatus)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnosticChange_OldStatus()
	 * @model
	 * @generated
	 */
	DiagnosticStatus getOldStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getOldStatus <em>Old Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Old Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus
	 * @see #getOldStatus()
	 * @generated
	 */
	void setOldStatus(DiagnosticStatus value);

	/**
	 * Returns the value of the '<em><b>New Status</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Status after the change
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>New Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus
	 * @see #setNewStatus(DiagnosticStatus)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnosticChange_NewStatus()
	 * @model
	 * @generated
	 */
	DiagnosticStatus getNewStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getNewStatus <em>New Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>New Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus
	 * @see #getNewStatus()
	 * @generated
	 */
	void setNewStatus(DiagnosticStatus value);

	/**
	 * Returns the value of the '<em><b>Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Why the change was made, in the words of whoever made it: the GDPR officer's
	 * note on a resolution, or the producer's explanation of an escalation. Optional.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Reason</em>' attribute.
	 * @see #setReason(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnosticChange_Reason()
	 * @model
	 * @generated
	 */
	String getReason();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange#getReason <em>Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reason</em>' attribute.
	 * @see #getReason()
	 * @generated
	 */
	void setReason(String value);

} // DiagnosticChange
