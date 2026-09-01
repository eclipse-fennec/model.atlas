/**
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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsFactory;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class QvtDiagnosticsPackageImpl extends EPackageImpl implements QvtDiagnosticsPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sourceDiagnosticsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass diagnosticEntryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum compileStatusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum diagnosticSeverityEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private QvtDiagnosticsPackageImpl() {
		super(eNS_URI, QvtDiagnosticsFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link QvtDiagnosticsPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static QvtDiagnosticsPackage init() {
		if (isInited) return (QvtDiagnosticsPackage)EPackage.Registry.INSTANCE.getEPackage(QvtDiagnosticsPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredQvtDiagnosticsPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		QvtDiagnosticsPackageImpl theQvtDiagnosticsPackage = registeredQvtDiagnosticsPackage instanceof QvtDiagnosticsPackageImpl ? (QvtDiagnosticsPackageImpl)registeredQvtDiagnosticsPackage : new QvtDiagnosticsPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theQvtDiagnosticsPackage.createPackageContents();

		// Initialize created meta-data
		theQvtDiagnosticsPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theQvtDiagnosticsPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(QvtDiagnosticsPackage.eNS_URI, theQvtDiagnosticsPackage);
		return theQvtDiagnosticsPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSourceDiagnostics() {
		return sourceDiagnosticsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSourceDiagnostics_QualifiedName() {
		return (EAttribute)sourceDiagnosticsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSourceDiagnostics_SourceFingerprint() {
		return (EAttribute)sourceDiagnosticsEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSourceDiagnostics_CompileStatus() {
		return (EAttribute)sourceDiagnosticsEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSourceDiagnostics_UnitFingerprint() {
		return (EAttribute)sourceDiagnosticsEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSourceDiagnostics_Message() {
		return (EAttribute)sourceDiagnosticsEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSourceDiagnostics_Entries() {
		return (EReference)sourceDiagnosticsEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDiagnosticEntry() {
		return diagnosticEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnosticEntry_Line() {
		return (EAttribute)diagnosticEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnosticEntry_Column() {
		return (EAttribute)diagnosticEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnosticEntry_Severity() {
		return (EAttribute)diagnosticEntryEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnosticEntry_Message() {
		return (EAttribute)diagnosticEntryEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getCompileStatus() {
		return compileStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getDiagnosticSeverity() {
		return diagnosticSeverityEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public QvtDiagnosticsFactory getQvtDiagnosticsFactory() {
		return (QvtDiagnosticsFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		sourceDiagnosticsEClass = createEClass(SOURCE_DIAGNOSTICS);
		createEAttribute(sourceDiagnosticsEClass, SOURCE_DIAGNOSTICS__QUALIFIED_NAME);
		createEAttribute(sourceDiagnosticsEClass, SOURCE_DIAGNOSTICS__SOURCE_FINGERPRINT);
		createEAttribute(sourceDiagnosticsEClass, SOURCE_DIAGNOSTICS__COMPILE_STATUS);
		createEAttribute(sourceDiagnosticsEClass, SOURCE_DIAGNOSTICS__UNIT_FINGERPRINT);
		createEAttribute(sourceDiagnosticsEClass, SOURCE_DIAGNOSTICS__MESSAGE);
		createEReference(sourceDiagnosticsEClass, SOURCE_DIAGNOSTICS__ENTRIES);

		diagnosticEntryEClass = createEClass(DIAGNOSTIC_ENTRY);
		createEAttribute(diagnosticEntryEClass, DIAGNOSTIC_ENTRY__LINE);
		createEAttribute(diagnosticEntryEClass, DIAGNOSTIC_ENTRY__COLUMN);
		createEAttribute(diagnosticEntryEClass, DIAGNOSTIC_ENTRY__SEVERITY);
		createEAttribute(diagnosticEntryEClass, DIAGNOSTIC_ENTRY__MESSAGE);

		// Create enums
		compileStatusEEnum = createEEnum(COMPILE_STATUS);
		diagnosticSeverityEEnum = createEEnum(DIAGNOSTIC_SEVERITY);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(sourceDiagnosticsEClass, SourceDiagnostics.class, "SourceDiagnostics", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSourceDiagnostics_QualifiedName(), ecorePackage.getEString(), "qualifiedName", null, 1, 1, SourceDiagnostics.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSourceDiagnostics_SourceFingerprint(), ecorePackage.getEString(), "sourceFingerprint", null, 0, 1, SourceDiagnostics.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSourceDiagnostics_CompileStatus(), this.getCompileStatus(), "compileStatus", null, 0, 1, SourceDiagnostics.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSourceDiagnostics_UnitFingerprint(), ecorePackage.getEString(), "unitFingerprint", null, 0, 1, SourceDiagnostics.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSourceDiagnostics_Message(), ecorePackage.getEString(), "message", null, 0, 1, SourceDiagnostics.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSourceDiagnostics_Entries(), this.getDiagnosticEntry(), null, "entries", null, 0, -1, SourceDiagnostics.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(diagnosticEntryEClass, DiagnosticEntry.class, "DiagnosticEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDiagnosticEntry_Line(), ecorePackage.getEInt(), "line", null, 0, 1, DiagnosticEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnosticEntry_Column(), ecorePackage.getEInt(), "column", null, 0, 1, DiagnosticEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnosticEntry_Severity(), this.getDiagnosticSeverity(), "severity", null, 0, 1, DiagnosticEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnosticEntry_Message(), ecorePackage.getEString(), "message", null, 0, 1, DiagnosticEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(compileStatusEEnum, CompileStatus.class, "CompileStatus");
		addEEnumLiteral(compileStatusEEnum, CompileStatus.OK);
		addEEnumLiteral(compileStatusEEnum, CompileStatus.INVALID);
		addEEnumLiteral(compileStatusEEnum, CompileStatus.LIBRARY);

		initEEnum(diagnosticSeverityEEnum, DiagnosticSeverity.class, "DiagnosticSeverity");
		addEEnumLiteral(diagnosticSeverityEEnum, DiagnosticSeverity.ERROR);
		addEEnumLiteral(diagnosticSeverityEEnum, DiagnosticSeverity.WARNING);
		addEEnumLiteral(diagnosticSeverityEEnum, DiagnosticSeverity.INFO);

		// Create resource
		createResource(eNS_URI);
	}

} //QvtDiagnosticsPackageImpl
