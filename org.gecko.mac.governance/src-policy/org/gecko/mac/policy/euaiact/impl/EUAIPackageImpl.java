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
package org.gecko.mac.policy.euaiact.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.gecko.mac.governance.GovernancePackage;

import org.gecko.mac.policy.dataqs.DataQSPackage;

import org.gecko.mac.policy.dataqs.impl.DataQSPackageImpl;

import org.gecko.mac.policy.dinspec91377.DS91377Package;

import org.gecko.mac.policy.dinspec91377.impl.DS91377PackageImpl;

import org.gecko.mac.policy.euaiact.AIActRiskLevel;
import org.gecko.mac.policy.euaiact.EUAIActPolicyCheck;
import org.gecko.mac.policy.euaiact.EUAIFactory;
import org.gecko.mac.policy.euaiact.EUAIPackage;

import org.gecko.mac.policy.gdpr.GDPRPackage;

import org.gecko.mac.policy.gdpr.impl.GDPRPackageImpl;

import org.gecko.mac.policy.iso27001.ISO27Package;

import org.gecko.mac.policy.iso27001.impl.ISO27PackageImpl;

import org.gecko.mac.policy.iso9001.ISO9001Package;

import org.gecko.mac.policy.iso9001.impl.ISO9001PackageImpl;

import org.gecko.mac.policy.kritis.KritisPackage;

import org.gecko.mac.policy.kritis.impl.KritisPackageImpl;

import org.gecko.mac.policy.opendata.OpenDataPackage;

import org.gecko.mac.policy.opendata.impl.OpenDataPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class EUAIPackageImpl extends EPackageImpl implements EUAIPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass euaiActPolicyCheckEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum aiActRiskLevelEEnum = null;

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
	 * @see org.gecko.mac.policy.euaiact.EUAIPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private EUAIPackageImpl() {
		super(eNS_URI, EUAIFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link EUAIPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static EUAIPackage init() {
		if (isInited) return (EUAIPackage)EPackage.Registry.INSTANCE.getEPackage(EUAIPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredEUAIPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		EUAIPackageImpl theEUAIPackage = registeredEUAIPackage instanceof EUAIPackageImpl ? (EUAIPackageImpl)registeredEUAIPackage : new EUAIPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		GovernancePackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(DataQSPackage.eNS_URI);
		DataQSPackageImpl theDataQSPackage = (DataQSPackageImpl)(registeredPackage instanceof DataQSPackageImpl ? registeredPackage : DataQSPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(GDPRPackage.eNS_URI);
		GDPRPackageImpl theGDPRPackage = (GDPRPackageImpl)(registeredPackage instanceof GDPRPackageImpl ? registeredPackage : GDPRPackage.eINSTANCE);
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
		theEUAIPackage.createPackageContents();
		theDataQSPackage.createPackageContents();
		theGDPRPackage.createPackageContents();
		theISO27Package.createPackageContents();
		theKritisPackage.createPackageContents();
		theOpenDataPackage.createPackageContents();
		theISO9001Package.createPackageContents();
		theDS91377Package.createPackageContents();

		// Initialize created meta-data
		theEUAIPackage.initializePackageContents();
		theDataQSPackage.initializePackageContents();
		theGDPRPackage.initializePackageContents();
		theISO27Package.initializePackageContents();
		theKritisPackage.initializePackageContents();
		theOpenDataPackage.initializePackageContents();
		theISO9001Package.initializePackageContents();
		theDS91377Package.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theEUAIPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(EUAIPackage.eNS_URI, theEUAIPackage);
		return theEUAIPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEUAIActPolicyCheck() {
		return euaiActPolicyCheckEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEUAIActPolicyCheck_RiskLevel() {
		return (EAttribute)euaiActPolicyCheckEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEUAIActPolicyCheck_IsHighRiskAnnexIII() {
		return (EAttribute)euaiActPolicyCheckEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEUAIActPolicyCheck_ThreatensHealthSafetyOrRights() {
		return (EAttribute)euaiActPolicyCheckEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEUAIActPolicyCheck_IsUnacceptableRiskArticle5() {
		return (EAttribute)euaiActPolicyCheckEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getAIActRiskLevel() {
		return aiActRiskLevelEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EUAIFactory getEUAIFactory() {
		return (EUAIFactory)getEFactoryInstance();
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
		euaiActPolicyCheckEClass = createEClass(EUAI_ACT_POLICY_CHECK);
		createEAttribute(euaiActPolicyCheckEClass, EUAI_ACT_POLICY_CHECK__RISK_LEVEL);
		createEAttribute(euaiActPolicyCheckEClass, EUAI_ACT_POLICY_CHECK__IS_HIGH_RISK_ANNEX_III);
		createEAttribute(euaiActPolicyCheckEClass, EUAI_ACT_POLICY_CHECK__THREATENS_HEALTH_SAFETY_OR_RIGHTS);
		createEAttribute(euaiActPolicyCheckEClass, EUAI_ACT_POLICY_CHECK__IS_UNACCEPTABLE_RISK_ARTICLE5);

		// Create enums
		aiActRiskLevelEEnum = createEEnum(AI_ACT_RISK_LEVEL);
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
		euaiActPolicyCheckEClass.getESuperTypes().add(theGovernancePackage.getComplianceCheckResult());

		// Initialize classes, features, and operations; add parameters
		initEClass(euaiActPolicyCheckEClass, EUAIActPolicyCheck.class, "EUAIActPolicyCheck", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEUAIActPolicyCheck_RiskLevel(), this.getAIActRiskLevel(), "riskLevel", "UNKNOWN", 1, 1, EUAIActPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEUAIActPolicyCheck_IsHighRiskAnnexIII(), ecorePackage.getEBoolean(), "isHighRiskAnnexIII", "false", 1, 1, EUAIActPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEUAIActPolicyCheck_ThreatensHealthSafetyOrRights(), ecorePackage.getEBoolean(), "threatensHealthSafetyOrRights", "false", 1, 1, EUAIActPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEUAIActPolicyCheck_IsUnacceptableRiskArticle5(), ecorePackage.getEBoolean(), "isUnacceptableRiskArticle5", "false", 1, 1, EUAIActPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(aiActRiskLevelEEnum, AIActRiskLevel.class, "AIActRiskLevel");
		addEEnumLiteral(aiActRiskLevelEEnum, AIActRiskLevel.UNKNOWN);
		addEEnumLiteral(aiActRiskLevelEEnum, AIActRiskLevel.MINIMAL_OR_NO_RISK);
		addEEnumLiteral(aiActRiskLevelEEnum, AIActRiskLevel.LIMITED_RISK);
		addEEnumLiteral(aiActRiskLevelEEnum, AIActRiskLevel.HIGH_RISK);
		addEEnumLiteral(aiActRiskLevelEEnum, AIActRiskLevel.UNACCEPTABLE_RISK);

		// Create resource
		createResource(eNS_URI);
	}

} //EUAIPackageImpl
