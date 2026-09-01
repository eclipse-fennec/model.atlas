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


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = QvtDiagnosticsPackage.eNS_URI, fingerprint = "fp1:acbc80afc4c7d3da9c2273192c26166b6eacd09ce8baf3ea790f021607e7f03a", genModel = "/model/qvt-diagnostics.genmodel", genModelSourceLocations = {"model/qvt-diagnostics.genmodel","org.eclipse.fennec.model.atlas.qvt/model/qvt-diagnostics.genmodel"}, ecore = "/model/qvt-diagnostics.ecore", ecoreSourceLocations = "/model/qvt-diagnostics.ecore")
public interface QvtDiagnosticsPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "diagnostics";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/qvt/diagnostics/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "qvtdiag";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	QvtDiagnosticsPackage eINSTANCE = org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.QvtDiagnosticsPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.SourceDiagnosticsImpl <em>Source Diagnostics</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.SourceDiagnosticsImpl
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.QvtDiagnosticsPackageImpl#getSourceDiagnostics()
	 * @generated
	 */
	int SOURCE_DIAGNOSTICS = 0;

	/**
	 * The feature id for the '<em><b>Qualified Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_DIAGNOSTICS__QUALIFIED_NAME = 0;

	/**
	 * The feature id for the '<em><b>Source Fingerprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_DIAGNOSTICS__SOURCE_FINGERPRINT = 1;

	/**
	 * The feature id for the '<em><b>Compile Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_DIAGNOSTICS__COMPILE_STATUS = 2;

	/**
	 * The feature id for the '<em><b>Unit Fingerprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_DIAGNOSTICS__UNIT_FINGERPRINT = 3;

	/**
	 * The feature id for the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_DIAGNOSTICS__MESSAGE = 4;

	/**
	 * The feature id for the '<em><b>Entries</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_DIAGNOSTICS__ENTRIES = 5;

	/**
	 * The number of structural features of the '<em>Source Diagnostics</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_DIAGNOSTICS_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Source Diagnostics</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE_DIAGNOSTICS_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.DiagnosticEntryImpl <em>Diagnostic Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.DiagnosticEntryImpl
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.QvtDiagnosticsPackageImpl#getDiagnosticEntry()
	 * @generated
	 */
	int DIAGNOSTIC_ENTRY = 1;

	/**
	 * The feature id for the '<em><b>Line</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC_ENTRY__LINE = 0;

	/**
	 * The feature id for the '<em><b>Column</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC_ENTRY__COLUMN = 1;

	/**
	 * The feature id for the '<em><b>Severity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC_ENTRY__SEVERITY = 2;

	/**
	 * The feature id for the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC_ENTRY__MESSAGE = 3;

	/**
	 * The number of structural features of the '<em>Diagnostic Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC_ENTRY_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Diagnostic Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC_ENTRY_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus <em>Compile Status</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.QvtDiagnosticsPackageImpl#getCompileStatus()
	 * @generated
	 */
	int COMPILE_STATUS = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity <em>Diagnostic Severity</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.QvtDiagnosticsPackageImpl#getDiagnosticSeverity()
	 * @generated
	 */
	int DIAGNOSTIC_SEVERITY = 3;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics <em>Source Diagnostics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Source Diagnostics</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics
	 * @generated
	 */
	EClass getSourceDiagnostics();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getQualifiedName <em>Qualified Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Qualified Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getQualifiedName()
	 * @see #getSourceDiagnostics()
	 * @generated
	 */
	EAttribute getSourceDiagnostics_QualifiedName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getSourceFingerprint <em>Source Fingerprint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Source Fingerprint</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getSourceFingerprint()
	 * @see #getSourceDiagnostics()
	 * @generated
	 */
	EAttribute getSourceDiagnostics_SourceFingerprint();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getCompileStatus <em>Compile Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Compile Status</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getCompileStatus()
	 * @see #getSourceDiagnostics()
	 * @generated
	 */
	EAttribute getSourceDiagnostics_CompileStatus();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getUnitFingerprint <em>Unit Fingerprint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unit Fingerprint</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getUnitFingerprint()
	 * @see #getSourceDiagnostics()
	 * @generated
	 */
	EAttribute getSourceDiagnostics_UnitFingerprint();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getMessage <em>Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getMessage()
	 * @see #getSourceDiagnostics()
	 * @generated
	 */
	EAttribute getSourceDiagnostics_Message();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getEntries <em>Entries</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Entries</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics#getEntries()
	 * @see #getSourceDiagnostics()
	 * @generated
	 */
	EReference getSourceDiagnostics_Entries();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry <em>Diagnostic Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Diagnostic Entry</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry
	 * @generated
	 */
	EClass getDiagnosticEntry();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getLine <em>Line</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Line</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getLine()
	 * @see #getDiagnosticEntry()
	 * @generated
	 */
	EAttribute getDiagnosticEntry_Line();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getColumn <em>Column</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Column</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getColumn()
	 * @see #getDiagnosticEntry()
	 * @generated
	 */
	EAttribute getDiagnosticEntry_Column();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getSeverity <em>Severity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Severity</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getSeverity()
	 * @see #getDiagnosticEntry()
	 * @generated
	 */
	EAttribute getDiagnosticEntry_Severity();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getMessage <em>Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry#getMessage()
	 * @see #getDiagnosticEntry()
	 * @generated
	 */
	EAttribute getDiagnosticEntry_Message();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus <em>Compile Status</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Compile Status</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus
	 * @generated
	 */
	EEnum getCompileStatus();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity <em>Diagnostic Severity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Diagnostic Severity</em>'.
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity
	 * @generated
	 */
	EEnum getDiagnosticSeverity();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	QvtDiagnosticsFactory getQvtDiagnosticsFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.SourceDiagnosticsImpl <em>Source Diagnostics</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.SourceDiagnosticsImpl
		 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.QvtDiagnosticsPackageImpl#getSourceDiagnostics()
		 * @generated
		 */
		EClass SOURCE_DIAGNOSTICS = eINSTANCE.getSourceDiagnostics();

		/**
		 * The meta object literal for the '<em><b>Qualified Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE_DIAGNOSTICS__QUALIFIED_NAME = eINSTANCE.getSourceDiagnostics_QualifiedName();

		/**
		 * The meta object literal for the '<em><b>Source Fingerprint</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE_DIAGNOSTICS__SOURCE_FINGERPRINT = eINSTANCE.getSourceDiagnostics_SourceFingerprint();

		/**
		 * The meta object literal for the '<em><b>Compile Status</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE_DIAGNOSTICS__COMPILE_STATUS = eINSTANCE.getSourceDiagnostics_CompileStatus();

		/**
		 * The meta object literal for the '<em><b>Unit Fingerprint</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE_DIAGNOSTICS__UNIT_FINGERPRINT = eINSTANCE.getSourceDiagnostics_UnitFingerprint();

		/**
		 * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE_DIAGNOSTICS__MESSAGE = eINSTANCE.getSourceDiagnostics_Message();

		/**
		 * The meta object literal for the '<em><b>Entries</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference SOURCE_DIAGNOSTICS__ENTRIES = eINSTANCE.getSourceDiagnostics_Entries();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.DiagnosticEntryImpl <em>Diagnostic Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.DiagnosticEntryImpl
		 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.QvtDiagnosticsPackageImpl#getDiagnosticEntry()
		 * @generated
		 */
		EClass DIAGNOSTIC_ENTRY = eINSTANCE.getDiagnosticEntry();

		/**
		 * The meta object literal for the '<em><b>Line</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DIAGNOSTIC_ENTRY__LINE = eINSTANCE.getDiagnosticEntry_Line();

		/**
		 * The meta object literal for the '<em><b>Column</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DIAGNOSTIC_ENTRY__COLUMN = eINSTANCE.getDiagnosticEntry_Column();

		/**
		 * The meta object literal for the '<em><b>Severity</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DIAGNOSTIC_ENTRY__SEVERITY = eINSTANCE.getDiagnosticEntry_Severity();

		/**
		 * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DIAGNOSTIC_ENTRY__MESSAGE = eINSTANCE.getDiagnosticEntry_Message();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus <em>Compile Status</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus
		 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.QvtDiagnosticsPackageImpl#getCompileStatus()
		 * @generated
		 */
		EEnum COMPILE_STATUS = eINSTANCE.getCompileStatus();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity <em>Diagnostic Severity</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity
		 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.impl.QvtDiagnosticsPackageImpl#getDiagnosticSeverity()
		 * @generated
		 */
		EEnum DIAGNOSTIC_SEVERITY = eINSTANCE.getDiagnosticSeverity();

	}

} //QvtDiagnosticsPackage
