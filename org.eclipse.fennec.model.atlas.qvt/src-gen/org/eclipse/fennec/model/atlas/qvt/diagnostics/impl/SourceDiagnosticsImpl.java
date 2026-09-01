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

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.BasicInternalEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Source Diagnostics</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.SourceDiagnosticsImpl#getQualifiedName <em>Qualified Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.SourceDiagnosticsImpl#getSourceFingerprint <em>Source Fingerprint</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.SourceDiagnosticsImpl#getCompileStatus <em>Compile Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.SourceDiagnosticsImpl#getUnitFingerprint <em>Unit Fingerprint</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.SourceDiagnosticsImpl#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.SourceDiagnosticsImpl#getEntries <em>Entries</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SourceDiagnosticsImpl extends MinimalEObjectImpl.Container implements SourceDiagnostics {
	/**
	 * The default value of the '{@link #getQualifiedName() <em>Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getQualifiedName()
	 * @generated
	 * @ordered
	 */
	protected static final String QUALIFIED_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getQualifiedName() <em>Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getQualifiedName()
	 * @generated
	 * @ordered
	 */
	protected String qualifiedName = QUALIFIED_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getSourceFingerprint() <em>Source Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceFingerprint()
	 * @generated
	 * @ordered
	 */
	protected static final String SOURCE_FINGERPRINT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSourceFingerprint() <em>Source Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceFingerprint()
	 * @generated
	 * @ordered
	 */
	protected String sourceFingerprint = SOURCE_FINGERPRINT_EDEFAULT;

	/**
	 * The default value of the '{@link #getCompileStatus() <em>Compile Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCompileStatus()
	 * @generated
	 * @ordered
	 */
	protected static final CompileStatus COMPILE_STATUS_EDEFAULT = CompileStatus.OK;

	/**
	 * The cached value of the '{@link #getCompileStatus() <em>Compile Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCompileStatus()
	 * @generated
	 * @ordered
	 */
	protected CompileStatus compileStatus = COMPILE_STATUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getUnitFingerprint() <em>Unit Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitFingerprint()
	 * @generated
	 * @ordered
	 */
	protected static final String UNIT_FINGERPRINT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUnitFingerprint() <em>Unit Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitFingerprint()
	 * @generated
	 * @ordered
	 */
	protected String unitFingerprint = UNIT_FINGERPRINT_EDEFAULT;

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
	 * The cached value of the '{@link #getEntries() <em>Entries</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEntries()
	 * @generated
	 * @ordered
	 */
	protected EList<DiagnosticEntry> entries;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SourceDiagnosticsImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return QvtDiagnosticsPackage.Literals.SOURCE_DIAGNOSTICS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getQualifiedName() {
		return qualifiedName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setQualifiedName(String newQualifiedName) {
		qualifiedName = newQualifiedName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSourceFingerprint() {
		return sourceFingerprint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSourceFingerprint(String newSourceFingerprint) {
		sourceFingerprint = newSourceFingerprint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CompileStatus getCompileStatus() {
		return compileStatus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCompileStatus(CompileStatus newCompileStatus) {
		compileStatus = newCompileStatus == null ? COMPILE_STATUS_EDEFAULT : newCompileStatus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUnitFingerprint() {
		return unitFingerprint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnitFingerprint(String newUnitFingerprint) {
		unitFingerprint = newUnitFingerprint;
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
	public List<DiagnosticEntry> getEntries() {
		if (entries == null) {
			entries = new BasicInternalEList<DiagnosticEntry>(DiagnosticEntry.class);
		}
		return entries;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__ENTRIES:
				return ((InternalEList<?>)getEntries()).basicRemove(otherEnd, msgs);
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
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__QUALIFIED_NAME:
				return getQualifiedName();
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__SOURCE_FINGERPRINT:
				return getSourceFingerprint();
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__COMPILE_STATUS:
				return getCompileStatus();
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__UNIT_FINGERPRINT:
				return getUnitFingerprint();
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__MESSAGE:
				return getMessage();
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__ENTRIES:
				return getEntries();
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
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__QUALIFIED_NAME:
				setQualifiedName((String)newValue);
				return;
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__SOURCE_FINGERPRINT:
				setSourceFingerprint((String)newValue);
				return;
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__COMPILE_STATUS:
				setCompileStatus((CompileStatus)newValue);
				return;
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__UNIT_FINGERPRINT:
				setUnitFingerprint((String)newValue);
				return;
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__MESSAGE:
				setMessage((String)newValue);
				return;
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__ENTRIES:
				getEntries().clear();
				getEntries().addAll((Collection<? extends DiagnosticEntry>)newValue);
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
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__QUALIFIED_NAME:
				setQualifiedName(QUALIFIED_NAME_EDEFAULT);
				return;
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__SOURCE_FINGERPRINT:
				setSourceFingerprint(SOURCE_FINGERPRINT_EDEFAULT);
				return;
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__COMPILE_STATUS:
				setCompileStatus(COMPILE_STATUS_EDEFAULT);
				return;
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__UNIT_FINGERPRINT:
				setUnitFingerprint(UNIT_FINGERPRINT_EDEFAULT);
				return;
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__MESSAGE:
				setMessage(MESSAGE_EDEFAULT);
				return;
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__ENTRIES:
				getEntries().clear();
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
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__QUALIFIED_NAME:
				return QUALIFIED_NAME_EDEFAULT == null ? qualifiedName != null : !QUALIFIED_NAME_EDEFAULT.equals(qualifiedName);
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__SOURCE_FINGERPRINT:
				return SOURCE_FINGERPRINT_EDEFAULT == null ? sourceFingerprint != null : !SOURCE_FINGERPRINT_EDEFAULT.equals(sourceFingerprint);
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__COMPILE_STATUS:
				return compileStatus != COMPILE_STATUS_EDEFAULT;
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__UNIT_FINGERPRINT:
				return UNIT_FINGERPRINT_EDEFAULT == null ? unitFingerprint != null : !UNIT_FINGERPRINT_EDEFAULT.equals(unitFingerprint);
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__MESSAGE:
				return MESSAGE_EDEFAULT == null ? message != null : !MESSAGE_EDEFAULT.equals(message);
			case QvtDiagnosticsPackage.SOURCE_DIAGNOSTICS__ENTRIES:
				return entries != null && !entries.isEmpty();
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
		result.append(" (qualifiedName: ");
		result.append(qualifiedName);
		result.append(", sourceFingerprint: ");
		result.append(sourceFingerprint);
		result.append(", compileStatus: ");
		result.append(compileStatus);
		result.append(", unitFingerprint: ");
		result.append(unitFingerprint);
		result.append(", message: ");
		result.append(message);
		result.append(')');
		return result.toString();
	}

} //SourceDiagnosticsImpl
