/**
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 */
package org.eclipse.fennec.model.atlas.validation.model.cocl.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLFactory;
import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Severity;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class COCLPackageImpl extends EPackageImpl implements COCLPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oclConstraintEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oclConstraintSetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum severityEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum oclRoleEEnum = null;

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
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private COCLPackageImpl() {
		super(eNS_URI, COCLFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link COCLPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static COCLPackage init() {
		if (isInited) return (COCLPackage)EPackage.Registry.INSTANCE.getEPackage(COCLPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredCOCLPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		COCLPackageImpl theCOCLPackage = registeredCOCLPackage instanceof COCLPackageImpl ? (COCLPackageImpl)registeredCOCLPackage : new COCLPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theCOCLPackage.createPackageContents();

		// Initialize created meta-data
		theCOCLPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theCOCLPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(COCLPackage.eNS_URI, theCOCLPackage);
		return theCOCLPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOclConstraint() {
		return oclConstraintEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Name() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Description() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Expression() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Severity() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Role() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_ContextClass() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_FeatureName() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Active() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Overrides() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_TargetURIs() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOclConstraintSet() {
		return oclConstraintSetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraintSet_Name() {
		return (EAttribute)oclConstraintSetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraintSet_Version() {
		return (EAttribute)oclConstraintSetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraintSet_Description() {
		return (EAttribute)oclConstraintSetEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getOclConstraintSet_Constraints() {
		return (EReference)oclConstraintSetEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraintSet_TargetModelNsURIs() {
		return (EAttribute)oclConstraintSetEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getSeverity() {
		return severityEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getOclRole() {
		return oclRoleEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public COCLFactory getCOCLFactory() {
		return (COCLFactory)getEFactoryInstance();
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
		oclConstraintEClass = createEClass(OCL_CONSTRAINT);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__NAME);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__DESCRIPTION);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__EXPRESSION);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__SEVERITY);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__ROLE);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__CONTEXT_CLASS);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__FEATURE_NAME);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__ACTIVE);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__OVERRIDES);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__TARGET_UR_IS);

		oclConstraintSetEClass = createEClass(OCL_CONSTRAINT_SET);
		createEAttribute(oclConstraintSetEClass, OCL_CONSTRAINT_SET__NAME);
		createEAttribute(oclConstraintSetEClass, OCL_CONSTRAINT_SET__VERSION);
		createEAttribute(oclConstraintSetEClass, OCL_CONSTRAINT_SET__DESCRIPTION);
		createEReference(oclConstraintSetEClass, OCL_CONSTRAINT_SET__CONSTRAINTS);
		createEAttribute(oclConstraintSetEClass, OCL_CONSTRAINT_SET__TARGET_MODEL_NS_UR_IS);

		// Create enums
		severityEEnum = createEEnum(SEVERITY);
		oclRoleEEnum = createEEnum(OCL_ROLE);
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
		initEClass(oclConstraintEClass, OclConstraint.class, "OclConstraint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getOclConstraint_Name(), ecorePackage.getEString(), "name", null, 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Description(), ecorePackage.getEString(), "description", null, 0, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Expression(), ecorePackage.getEString(), "expression", null, 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Severity(), this.getSeverity(), "severity", "ERROR", 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Role(), this.getOclRole(), "role", "VALIDATION", 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_ContextClass(), ecorePackage.getEString(), "contextClass", null, 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_FeatureName(), ecorePackage.getEString(), "featureName", null, 0, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Active(), ecorePackage.getEBoolean(), "active", "true", 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Overrides(), ecorePackage.getEBoolean(), "overrides", "false", 0, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_TargetURIs(), ecorePackage.getEString(), "targetURIs", null, 0, -1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oclConstraintSetEClass, OclConstraintSet.class, "OclConstraintSet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getOclConstraintSet_Name(), ecorePackage.getEString(), "name", null, 1, 1, OclConstraintSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraintSet_Version(), ecorePackage.getEString(), "version", "1.0", 0, 1, OclConstraintSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraintSet_Description(), ecorePackage.getEString(), "description", null, 0, 1, OclConstraintSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getOclConstraintSet_Constraints(), this.getOclConstraint(), null, "constraints", null, 0, -1, OclConstraintSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getOclConstraintSet_Constraints().getEKeys().add(this.getOclConstraint_Name());
		initEAttribute(getOclConstraintSet_TargetModelNsURIs(), ecorePackage.getEString(), "targetModelNsURIs", null, 0, -1, OclConstraintSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(severityEEnum, Severity.class, "Severity");
		addEEnumLiteral(severityEEnum, Severity.TRACE);
		addEEnumLiteral(severityEEnum, Severity.INFO);
		addEEnumLiteral(severityEEnum, Severity.WARN);
		addEEnumLiteral(severityEEnum, Severity.ERROR);
		addEEnumLiteral(severityEEnum, Severity.FATAL);

		initEEnum(oclRoleEEnum, OclRole.class, "OclRole");
		addEEnumLiteral(oclRoleEEnum, OclRole.VALIDATION);
		addEEnumLiteral(oclRoleEEnum, OclRole.DERIVED);
		addEEnumLiteral(oclRoleEEnum, OclRole.REFERENCE_FILTER);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
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
			   "documentation", "Custom OCL (C-OCL) Metamodell zur Definition von projektspezifischen OCL-Constraints, die unabhaengig vom Ecore-Modell verwaltet werden koennen.",
			   "oSGiCompatible", "true",
			   "basePackage", "org.eclipse.fennec.model.atlas.validation.model",
			   "complianceLevel", "21.0",
			   "copyrightText", "Copyright (c) 2026 Contributors to the Eclipse Foundation.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n  Data In Motion Consulting - initial implementation",
			   "resource", "XMI",
			   "fileExtensions", "cocl"
		   });
		addAnnotation
		  (severityEEnum,
		   source,
		   new String[] {
			   "documentation", "Schweregrad einer OCL-Constraint-Verletzung. Bestimmt die Darstellung in der Log-Tabelle und das Verhalten bei Validierungsfehlern."
		   });
		addAnnotation
		  (severityEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Rein diagnostisch, nicht standardmaessig sichtbar. Fuer Entwickler und Debugging-Zwecke."
		   });
		addAnnotation
		  (severityEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "Hinweis ohne Handlungsbedarf. Informiert den Nutzer ueber einen Zustand."
		   });
		addAnnotation
		  (severityEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Potenzielles Problem, das geprueft werden sollte. Blockiert keine Aktionen."
		   });
		addAnnotation
		  (severityEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Fachlich ungueltiger Zustand. Die Instanz entspricht nicht den definierten Regeln."
		   });
		addAnnotation
		  (severityEEnum.getELiterals().get(4),
		   source,
		   new String[] {
			   "documentation", "Kritischer Zustand. Weitere Verarbeitung sollte abgebrochen werden."
		   });
		addAnnotation
		  (oclRoleEEnum,
		   source,
		   new String[] {
			   "documentation", "Definiert die Rolle/Verwendung eines OCL-Ausdrucks im System."
		   });
		addAnnotation
		  (oclRoleEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "OCL wird zur Validierung von Instanzen verwendet."
		   });
		addAnnotation
		  (oclRoleEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "OCL wird zur Berechnung von Derived Values verwendet."
		   });
		addAnnotation
		  (oclRoleEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "OCL wird zur Filterung von Referenzzielen verwendet."
		   });
		addAnnotation
		  (oclConstraintEClass,
		   source,
		   new String[] {
			   "documentation", "Zentrale Klasse zur Definition eines OCL-Constraints. Enthaelt den OCL-Ausdruck, Metadaten und optionale Einschraenkungen auf Zielobjekte."
		   });
		addAnnotation
		  (getOclConstraint_Name(),
		   source,
		   new String[] {
			   "documentation", "Eindeutiger, sprechender Name des Constraints. Wird in der Log-Tabelle und in Fehlermeldungen angezeigt."
		   });
		addAnnotation
		  (getOclConstraint_Description(),
		   source,
		   new String[] {
			   "documentation", "Fachliche Beschreibung des Constraints. Erklaert den Zweck und die Auswirkungen bei Verletzung."
		   });
		addAnnotation
		  (getOclConstraint_Expression(),
		   source,
		   new String[] {
			   "documentation", "Der OCL-Ausdruck selbst. Muss syntaktisch korrekt sein und zum Kontexttyp passen."
		   });
		addAnnotation
		  (getOclConstraint_Severity(),
		   source,
		   new String[] {
			   "documentation", "Schweregrad bei Constraint-Verletzung. Bestimmt Darstellung und Verhalten."
		   });
		addAnnotation
		  (getOclConstraint_Role(),
		   source,
		   new String[] {
			   "documentation", "Definiert die Verwendung des OCL-Ausdrucks (Validierung, Derived, Filter)."
		   });
		addAnnotation
		  (getOclConstraint_ContextClass(),
		   source,
		   new String[] {
			   "documentation", "Vollqualifizierter Name der EClass, auf die sich dieser Constraint bezieht. Format: EClass URI"
		   });
		addAnnotation
		  (getOclConstraint_FeatureName(),
		   source,
		   new String[] {
			   "documentation", "Optional: Name des Features (Attribut/Referenz), auf das sich der Constraint bezieht. Relevant fuer DERIVED und REFERENCE_FILTER Rollen."
		   });
		addAnnotation
		  (getOclConstraint_Active(),
		   source,
		   new String[] {
			   "documentation", "Gibt an, ob der Constraint aktiv ist. Inaktive Constraints werden bei der Validierung uebersprungen."
		   });
		addAnnotation
		  (getOclConstraint_Overrides(),
		   source,
		   new String[] {
			   "documentation", "Wenn true, ueberschreibt dieser Constraint einen gleichnamigen Constraint aus einer Quelle mit niedrigerer Prioritaet (z.B. Ecore)."
		   });
		addAnnotation
		  (getOclConstraint_TargetURIs(),
		   source,
		   new String[] {
			   "documentation", "Optionale Liste von URIs konkreter EObjects, auf die sich der Constraint beschraenkt. Wenn leer, gilt der Constraint fuer alle Instanzen der contextClass."
		   });
		addAnnotation
		  (oclConstraintSetEClass,
		   source,
		   new String[] {
			   "documentation", "Container fuer eine Sammlung von OCL-Constraints. Entspricht einer *.c-ocl Datei."
		   });
		addAnnotation
		  (getOclConstraintSet_Name(),
		   source,
		   new String[] {
			   "documentation", "Name des Constraint-Sets. Wird zur Identifikation und im Log verwendet."
		   });
		addAnnotation
		  (getOclConstraintSet_Version(),
		   source,
		   new String[] {
			   "documentation", "Versionsnummer des Constraint-Sets fuer Kompatibilitaetspruefungen."
		   });
		addAnnotation
		  (getOclConstraintSet_Description(),
		   source,
		   new String[] {
			   "documentation", "Optionale Beschreibung des Constraint-Sets."
		   });
		addAnnotation
		  (getOclConstraintSet_Constraints(),
		   source,
		   new String[] {
			   "documentation", "Liste der enthaltenen OCL-Constraints."
		   });
		addAnnotation
		  (getOclConstraintSet_TargetModelNsURIs(),
		   source,
		   new String[] {
			   "documentation", "Liste von Namespace URIs der Zielmodelle, fuer die dieses Constraint-Set gilt."
		   });
	}

} //COCLPackageImpl
