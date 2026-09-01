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
package org.eclipse.fennec.model.atlas.qvt.diagnostics;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Diagnostic Entry</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getLine <em>Line</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getColumn <em>Column</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getMessage <em>Message</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getDiagnosticEntry()
 * @model
 * @generated
 */
@ProviderType
public interface DiagnosticEntry extends EObject {
	/**
	 * Returns the value of the '<em><b>Line</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * 1-based line; 0 means position unknown (m2x #110).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Line</em>' attribute.
	 * @see #setLine(int)
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getDiagnosticEntry_Line()
	 * @model
	 * @generated
	 */
	int getLine();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getLine <em>Line</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Line</em>' attribute.
	 * @see #getLine()
	 * @generated
	 */
	void setLine(int value);

	/**
	 * Returns the value of the '<em><b>Column</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * 1-based column; 0 means position unknown.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Column</em>' attribute.
	 * @see #setColumn(int)
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getDiagnosticEntry_Column()
	 * @model
	 * @generated
	 */
	int getColumn();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getColumn <em>Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Column</em>' attribute.
	 * @see #getColumn()
	 * @generated
	 */
	void setColumn(int value);

	/**
	 * Returns the value of the '<em><b>Severity</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity
	 * @see #setSeverity(DiagnosticSeverity)
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getDiagnosticEntry_Severity()
	 * @model
	 * @generated
	 */
	DiagnosticSeverity getSeverity();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getSeverity <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity
	 * @see #getSeverity()
	 * @generated
	 */
	void setSeverity(DiagnosticSeverity value);

	/**
	 * Returns the value of the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Message</em>' attribute.
	 * @see #setMessage(String)
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getDiagnosticEntry_Message()
	 * @model
	 * @generated
	 */
	String getMessage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getMessage <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message</em>' attribute.
	 * @see #getMessage()
	 * @generated
	 */
	void setMessage(String value);

} // DiagnosticEntry
