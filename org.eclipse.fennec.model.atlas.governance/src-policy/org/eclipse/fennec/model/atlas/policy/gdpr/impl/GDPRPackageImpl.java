/**
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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.policy.gdpr.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.governance.GovernancePackage;

import org.eclipse.fennec.model.atlas.policy.dataqs.DataQSPackage;

import org.eclipse.fennec.model.atlas.policy.dataqs.impl.DataQSPackageImpl;

import org.eclipse.fennec.model.atlas.policy.dinspec91377.DS91377Package;

import org.eclipse.fennec.model.atlas.policy.dinspec91377.impl.DS91377PackageImpl;

import org.eclipse.fennec.model.atlas.policy.euaiact.EUAIPackage;

import org.eclipse.fennec.model.atlas.policy.euaiact.impl.EUAIPackageImpl;

import org.eclipse.fennec.model.atlas.policy.gdpr.GDPRFactory;
import org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPackage;
import org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPolicyCheck;
import org.eclipse.fennec.model.atlas.policy.gdpr.LegalBasis;

import org.eclipse.fennec.model.atlas.policy.iso27001.ISO27Package;

import org.eclipse.fennec.model.atlas.policy.iso27001.impl.ISO27PackageImpl;

import org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001Package;

import org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PackageImpl;

import org.eclipse.fennec.model.atlas.policy.kritis.KritisPackage;

import org.eclipse.fennec.model.atlas.policy.kritis.impl.KritisPackageImpl;

import org.eclipse.fennec.model.atlas.policy.opendata.OpenDataPackage;

import org.eclipse.fennec.model.atlas.policy.opendata.impl.OpenDataPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class GDPRPackageImpl extends EPackageImpl implements GDPRPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass gdprPolicyCheckEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum legalBasisEEnum = null;

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
	 * @see org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private GDPRPackageImpl() {
		super(eNS_URI, GDPRFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link GDPRPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static GDPRPackage init() {
		if (isInited) return (GDPRPackage)EPackage.Registry.INSTANCE.getEPackage(GDPRPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredGDPRPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		GDPRPackageImpl theGDPRPackage = registeredGDPRPackage instanceof GDPRPackageImpl ? (GDPRPackageImpl)registeredGDPRPackage : new GDPRPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		GovernancePackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(DataQSPackage.eNS_URI);
		DataQSPackageImpl theDataQSPackage = (DataQSPackageImpl)(registeredPackage instanceof DataQSPackageImpl ? registeredPackage : DataQSPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(EUAIPackage.eNS_URI);
		EUAIPackageImpl theEUAIPackage = (EUAIPackageImpl)(registeredPackage instanceof EUAIPackageImpl ? registeredPackage : EUAIPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(ISO27Package.eNS_URI);
		ISO27PackageImpl theISO27Package = (ISO27PackageImpl)(registeredPackage instanceof ISO27PackageImpl ? registeredPackage : ISO27Package.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(KritisPackage.eNS_URI);
		KritisPackageImpl theKritisPackage = (KritisPackageImpl)(registeredPackage instanceof KritisPackageImpl ? registeredPackage : KritisPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(OpenDataPackage.eNS_URI);
		OpenDataPackageImpl theOpenDataPackage = (OpenDataPackageImpl)(registeredPackage instanceof OpenDataPackageImpl ? registeredPackage : OpenDataPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(ISO9001Package.eNS_URI);
		ISO9001PackageImpl theISO9001Package = (ISO9001PackageImpl)(registeredPackage instanceof ISO9001PackageImpl ? registeredPackage : ISO9001Package.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(DS91377Package.eNS_URI);
		DS91377PackageImpl theDS91377Package = (DS91377PackageImpl)(registeredPackage instanceof DS91377PackageImpl ? registeredPackage : DS91377Package.eINSTANCE);

		// Create package meta-data objects
		theGDPRPackage.createPackageContents();
		theDataQSPackage.createPackageContents();
		theEUAIPackage.createPackageContents();
		theISO27Package.createPackageContents();
		theKritisPackage.createPackageContents();
		theOpenDataPackage.createPackageContents();
		theISO9001Package.createPackageContents();
		theDS91377Package.createPackageContents();

		// Initialize created meta-data
		theGDPRPackage.initializePackageContents();
		theDataQSPackage.initializePackageContents();
		theEUAIPackage.initializePackageContents();
		theISO27Package.initializePackageContents();
		theKritisPackage.initializePackageContents();
		theOpenDataPackage.initializePackageContents();
		theISO9001Package.initializePackageContents();
		theDS91377Package.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theGDPRPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(GDPRPackage.eNS_URI, theGDPRPackage);
		return theGDPRPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGDPRPolicyCheck() {
		return gdprPolicyCheckEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGDPRPolicyCheck_ContainsPersonalData() {
		return (EAttribute)gdprPolicyCheckEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGDPRPolicyCheck_LegalBasisForProcessing() {
		return (EAttribute)gdprPolicyCheckEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGDPRPolicyCheck_DataMinimizationApplied() {
		return (EAttribute)gdprPolicyCheckEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGDPRPolicyCheck_PurposeLimitationMet() {
		return (EAttribute)gdprPolicyCheckEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getLegalBasis() {
		return legalBasisEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GDPRFactory getGDPRFactory() {
		return (GDPRFactory)getEFactoryInstance();
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
		gdprPolicyCheckEClass = createEClass(GDPR_POLICY_CHECK);
		createEAttribute(gdprPolicyCheckEClass, GDPR_POLICY_CHECK__CONTAINS_PERSONAL_DATA);
		createEAttribute(gdprPolicyCheckEClass, GDPR_POLICY_CHECK__LEGAL_BASIS_FOR_PROCESSING);
		createEAttribute(gdprPolicyCheckEClass, GDPR_POLICY_CHECK__DATA_MINIMIZATION_APPLIED);
		createEAttribute(gdprPolicyCheckEClass, GDPR_POLICY_CHECK__PURPOSE_LIMITATION_MET);

		// Create enums
		legalBasisEEnum = createEEnum(LEGAL_BASIS);
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

		// Obtain other dependent packages
		GovernancePackage theGovernancePackage = (GovernancePackage)EPackage.Registry.INSTANCE.getEPackage(GovernancePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		gdprPolicyCheckEClass.getESuperTypes().add(theGovernancePackage.getComplianceCheckResult());

		// Initialize classes, features, and operations; add parameters
		initEClass(gdprPolicyCheckEClass, GDPRPolicyCheck.class, "GDPRPolicyCheck", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGDPRPolicyCheck_ContainsPersonalData(), ecorePackage.getEBoolean(), "containsPersonalData", "false", 1, 1, GDPRPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGDPRPolicyCheck_LegalBasisForProcessing(), this.getLegalBasis(), "legalBasisForProcessing", "NOT_APPLICABLE", 1, 1, GDPRPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGDPRPolicyCheck_DataMinimizationApplied(), ecorePackage.getEBoolean(), "dataMinimizationApplied", "true", 1, 1, GDPRPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGDPRPolicyCheck_PurposeLimitationMet(), ecorePackage.getEBoolean(), "purposeLimitationMet", "true", 1, 1, GDPRPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(legalBasisEEnum, LegalBasis.class, "LegalBasis");
		addEEnumLiteral(legalBasisEEnum, LegalBasis.NOT_APPLICABLE);
		addEEnumLiteral(legalBasisEEnum, LegalBasis.CONSENT);
		addEEnumLiteral(legalBasisEEnum, LegalBasis.CONTRACT);
		addEEnumLiteral(legalBasisEEnum, LegalBasis.LEGAL_OBLIGATION);
		addEEnumLiteral(legalBasisEEnum, LegalBasis.VITAL_INTEREST);
		addEEnumLiteral(legalBasisEEnum, LegalBasis.PUBLIC_TASK);
		addEEnumLiteral(legalBasisEEnum, LegalBasis.LEGITIMATE_INTEREST);

		// Create resource
		createResource(eNS_URI);
	}

} //GDPRPackageImpl
