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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic;
import org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticFactory;
import org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticPackage;
import org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class DiagnosticPackageImpl extends EPackageImpl implements DiagnosticPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass diagnosticEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum diagnosticTypeEEnum = null;

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
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private DiagnosticPackageImpl() {
		super(eNS_URI, DiagnosticFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link DiagnosticPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static DiagnosticPackage init() {
		if (isInited) return (DiagnosticPackage)EPackage.Registry.INSTANCE.getEPackage(DiagnosticPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredDiagnosticPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		DiagnosticPackageImpl theDiagnosticPackage = registeredDiagnosticPackage instanceof DiagnosticPackageImpl ? (DiagnosticPackageImpl)registeredDiagnosticPackage : new DiagnosticPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theDiagnosticPackage.createPackageContents();

		// Initialize created meta-data
		theDiagnosticPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theDiagnosticPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(DiagnosticPackage.eNS_URI, theDiagnosticPackage);
		return theDiagnosticPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDiagnostic() {
		return diagnosticEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Message() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Source() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Type() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDiagnostic_Children() {
		return (EReference)diagnosticEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_ExceptionMsg() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Data() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getDiagnosticType() {
		return diagnosticTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DiagnosticFactory getDiagnosticFactory() {
		return (DiagnosticFactory)getEFactoryInstance();
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
		diagnosticEClass = createEClass(DIAGNOSTIC);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__MESSAGE);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__SOURCE);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__TYPE);
		createEReference(diagnosticEClass, DIAGNOSTIC__CHILDREN);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__EXCEPTION_MSG);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__DATA);

		// Create enums
		diagnosticTypeEEnum = createEEnum(DIAGNOSTIC_TYPE);
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
		initEClass(diagnosticEClass, Diagnostic.class, "Diagnostic", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDiagnostic_Message(), ecorePackage.getEString(), "message", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Source(), ecorePackage.getEString(), "source", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Type(), this.getDiagnosticType(), "type", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDiagnostic_Children(), this.getDiagnostic(), null, "children", null, 0, -1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_ExceptionMsg(), ecorePackage.getEString(), "exceptionMsg", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Data(), ecorePackage.getEString(), "data", null, 0, -1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(diagnosticTypeEEnum, DiagnosticType.class, "DiagnosticType");
		addEEnumLiteral(diagnosticTypeEEnum, DiagnosticType.OK);
		addEEnumLiteral(diagnosticTypeEEnum, DiagnosticType.INFO);
		addEEnumLiteral(diagnosticTypeEEnum, DiagnosticType.WARNING);
		addEEnumLiteral(diagnosticTypeEEnum, DiagnosticType.ERROR);
		addEEnumLiteral(diagnosticTypeEEnum, DiagnosticType.CANCEL);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// Version
		createVersionAnnotations();
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
	}

	/**
	 * Initializes the annotations for <b>Version</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createVersionAnnotations() {
		String source = "Version";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "value", "1.0"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/GenModel</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createGenModelAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/GenModel";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "complianceLevel", "17.0",
			   "oSGiCompatible", "true",
			   "basePackage", "org.eclipse.fennec.model.atlas.diagnostic.model",
			   "resource", "XMI",
			   "copyrightText", "Copyright (c) 2012 - 2026 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n    Data In Motion - initial API and implementation"
		   });
	}

} //DiagnosticPackageImpl
