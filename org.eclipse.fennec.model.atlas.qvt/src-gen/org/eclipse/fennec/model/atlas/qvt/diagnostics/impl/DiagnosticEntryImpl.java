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
package org.eclipse.fennec.model.atlas.qvt.diagnostics.impl;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Diagnostic Entry</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.DiagnosticEntryImpl#getLine <em>Line</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.DiagnosticEntryImpl#getColumn <em>Column</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.DiagnosticEntryImpl#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.DiagnosticEntryImpl#getMessage <em>Message</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DiagnosticEntryImpl extends MinimalEObjectImpl.Container implements DiagnosticEntry {
	/**
	 * The default value of the '{@link #getLine() <em>Line</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLine()
	 * @generated
	 * @ordered
	 */
	protected static final int LINE_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getLine() <em>Line</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLine()
	 * @generated
	 * @ordered
	 */
	protected int line = LINE_EDEFAULT;

	/**
	 * The default value of the '{@link #getColumn() <em>Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumn()
	 * @generated
	 * @ordered
	 */
	protected static final int COLUMN_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getColumn() <em>Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getColumn()
	 * @generated
	 * @ordered
	 */
	protected int column = COLUMN_EDEFAULT;

	/**
	 * The default value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeverity()
	 * @generated
	 * @ordered
	 */
	protected static final DiagnosticSeverity SEVERITY_EDEFAULT = DiagnosticSeverity.ERROR;

	/**
	 * The cached value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeverity()
	 * @generated
	 * @ordered
	 */
	protected DiagnosticSeverity severity = SEVERITY_EDEFAULT;

	/**
	 * The default value of the '{@link #getMessage() <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMessage()
	 * @generated
	 * @ordered
	 */
	protected static final String MESSAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMessage() <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMessage()
	 * @generated
	 * @ordered
	 */
	protected String message = MESSAGE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DiagnosticEntryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return QvtDiagnosticsPackage.Literals.DIAGNOSTIC_ENTRY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getLine() {
		return line;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLine(int newLine) {
		line = newLine;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getColumn() {
		return column;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setColumn(int newColumn) {
		column = newColumn;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DiagnosticSeverity getSeverity() {
		return severity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSeverity(DiagnosticSeverity newSeverity) {
		severity = newSeverity == null ? SEVERITY_EDEFAULT : newSeverity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMessage(String newMessage) {
		message = newMessage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__LINE:
				return getLine();
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__COLUMN:
				return getColumn();
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__SEVERITY:
				return getSeverity();
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__MESSAGE:
				return getMessage();
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
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__LINE:
				setLine((Integer)newValue);
				return;
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__COLUMN:
				setColumn((Integer)newValue);
				return;
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__SEVERITY:
				setSeverity((DiagnosticSeverity)newValue);
				return;
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__MESSAGE:
				setMessage((String)newValue);
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
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__LINE:
				setLine(LINE_EDEFAULT);
				return;
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__COLUMN:
				setColumn(COLUMN_EDEFAULT);
				return;
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__SEVERITY:
				setSeverity(SEVERITY_EDEFAULT);
				return;
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__MESSAGE:
				setMessage(MESSAGE_EDEFAULT);
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
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__LINE:
				return line != LINE_EDEFAULT;
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__COLUMN:
				return column != COLUMN_EDEFAULT;
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__SEVERITY:
				return severity != SEVERITY_EDEFAULT;
			case QvtDiagnosticsPackage.DIAGNOSTIC_ENTRY__MESSAGE:
				return MESSAGE_EDEFAULT == null ? message != null : !MESSAGE_EDEFAULT.equals(message);
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
		result.append(" (line: ");
		result.append(line);
		result.append(", column: ");
		result.append(column);
		result.append(", severity: ");
		result.append(severity);
		result.append(", message: ");
		result.append(message);
		result.append(')');
		return result.toString();
	}

} //DiagnosticEntryImpl
