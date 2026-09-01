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

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Source Diagnostics</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * The compile outcome of one uploaded transformation source in one (scope, registry, stage): its status, and — for an invalid source — the parse/link findings with positions. Stored beside the source under the id segment `<language>/diagnostics/<qualifiedName>` and replaced on every recompile (issue #239).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getQualifiedName <em>Qualified Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getSourceFingerprint <em>Source Fingerprint</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getCompileStatus <em>Compile Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getUnitFingerprint <em>Unit Fingerprint</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getEntries <em>Entries</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getSourceDiagnostics()
 * @model
 * @generated
 */
@ProviderType
public interface SourceDiagnostics extends EObject {
	/**
	 * Returns the value of the '<em><b>Qualified Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Qualified Name</em>' attribute.
	 * @see #setQualifiedName(String)
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getSourceDiagnostics_QualifiedName()
	 * @model required="true"
	 * @generated
	 */
	String getQualifiedName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getQualifiedName <em>Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Qualified Name</em>' attribute.
	 * @see #getQualifiedName()
	 * @generated
	 */
	void setQualifiedName(String value);

	/**
	 * Returns the value of the '<em><b>Source Fingerprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The m2x source fingerprint (m2x1:…) of the source text this outcome belongs to.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source Fingerprint</em>' attribute.
	 * @see #setSourceFingerprint(String)
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getSourceDiagnostics_SourceFingerprint()
	 * @model
	 * @generated
	 */
	String getSourceFingerprint();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getSourceFingerprint <em>Source Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Fingerprint</em>' attribute.
	 * @see #getSourceFingerprint()
	 * @generated
	 */
	void setSourceFingerprint(String value);

	/**
	 * Returns the value of the '<em><b>Compile Status</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Compile Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus
	 * @see #setCompileStatus(CompileStatus)
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getSourceDiagnostics_CompileStatus()
	 * @model
	 * @generated
	 */
	CompileStatus getCompileStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getCompileStatus <em>Compile Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Compile Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus
	 * @see #getCompileStatus()
	 * @generated
	 */
	void setCompileStatus(CompileStatus value);

	/**
	 * Returns the value of the '<em><b>Unit Fingerprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * For status OK: the unit fingerprint (m2x1:…) of the compiled unit produced from this source — the value a consumer pins.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Unit Fingerprint</em>' attribute.
	 * @see #setUnitFingerprint(String)
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getSourceDiagnostics_UnitFingerprint()
	 * @model
	 * @generated
	 */
	String getUnitFingerprint();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getUnitFingerprint <em>Unit Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unit Fingerprint</em>' attribute.
	 * @see #getUnitFingerprint()
	 * @generated
	 */
	void setUnitFingerprint(String value);

	/**
	 * Returns the value of the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Top-level summary (e.g. the parse exception message); the per-finding detail lives in the entries.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Message</em>' attribute.
	 * @see #setMessage(String)
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getSourceDiagnostics_Message()
	 * @model
	 * @generated
	 */
	String getMessage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getMessage <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message</em>' attribute.
	 * @see #getMessage()
	 * @generated
	 */
	void setMessage(String value);

	/**
	 * Returns the value of the '<em><b>Entries</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Entries</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#getSourceDiagnostics_Entries()
	 * @model containment="true"
	 * @generated
	 */
	List<DiagnosticEntry> getEntries();

} // SourceDiagnostics
