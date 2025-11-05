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
package org.gecko.mac.policy.iso9001.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.gecko.mac.governance.GovernancePackage;

import org.gecko.mac.policy.dataqs.DataQSPackage;

import org.gecko.mac.policy.dataqs.impl.DataQSPackageImpl;

import org.gecko.mac.policy.dinspec91377.DS91377Package;

import org.gecko.mac.policy.dinspec91377.impl.DS91377PackageImpl;

import org.gecko.mac.policy.euaiact.EUAIPackage;

import org.gecko.mac.policy.euaiact.impl.EUAIPackageImpl;

import org.gecko.mac.policy.gdpr.GDPRPackage;

import org.gecko.mac.policy.gdpr.impl.GDPRPackageImpl;

import org.gecko.mac.policy.iso27001.ISO27Package;

import org.gecko.mac.policy.iso27001.impl.ISO27PackageImpl;

import org.gecko.mac.policy.iso9001.ISO9001Factory;
import org.gecko.mac.policy.iso9001.ISO9001Package;
import org.gecko.mac.policy.iso9001.ISO9001PolicyCheck;

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
public class ISO9001PackageImpl extends EPackageImpl implements ISO9001Package {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iso9001PolicyCheckEClass = null;

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
	 * @see org.gecko.mac.policy.iso9001.ISO9001Package#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ISO9001PackageImpl() {
		super(eNS_URI, ISO9001Factory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ISO9001Package#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ISO9001Package init() {
		if (isInited) return (ISO9001Package)EPackage.Registry.INSTANCE.getEPackage(ISO9001Package.eNS_URI);

		// Obtain or create and register package
		Object registeredISO9001Package = EPackage.Registry.INSTANCE.get(eNS_URI);
		ISO9001PackageImpl theISO9001Package = registeredISO9001Package instanceof ISO9001PackageImpl ? (ISO9001PackageImpl)registeredISO9001Package : new ISO9001PackageImpl();

		isInited = true;

		// Initialize simple dependencies
		GovernancePackage.eINSTANCE.eClass();

		// Obtain or create and register interdependencies
		Object registeredPackage = EPackage.Registry.INSTANCE.getEPackage(DataQSPackage.eNS_URI);
		DataQSPackageImpl theDataQSPackage = (DataQSPackageImpl)(registeredPackage instanceof DataQSPackageImpl ? registeredPackage : DataQSPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(EUAIPackage.eNS_URI);
		EUAIPackageImpl theEUAIPackage = (EUAIPackageImpl)(registeredPackage instanceof EUAIPackageImpl ? registeredPackage : EUAIPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(GDPRPackage.eNS_URI);
		GDPRPackageImpl theGDPRPackage = (GDPRPackageImpl)(registeredPackage instanceof GDPRPackageImpl ? registeredPackage : GDPRPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(ISO27Package.eNS_URI);
		ISO27PackageImpl theISO27Package = (ISO27PackageImpl)(registeredPackage instanceof ISO27PackageImpl ? registeredPackage : ISO27Package.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(KritisPackage.eNS_URI);
		KritisPackageImpl theKritisPackage = (KritisPackageImpl)(registeredPackage instanceof KritisPackageImpl ? registeredPackage : KritisPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(OpenDataPackage.eNS_URI);
		OpenDataPackageImpl theOpenDataPackage = (OpenDataPackageImpl)(registeredPackage instanceof OpenDataPackageImpl ? registeredPackage : OpenDataPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(DS91377Package.eNS_URI);
		DS91377PackageImpl theDS91377Package = (DS91377PackageImpl)(registeredPackage instanceof DS91377PackageImpl ? registeredPackage : DS91377Package.eINSTANCE);

		// Create package meta-data objects
		theISO9001Package.createPackageContents();
		theDataQSPackage.createPackageContents();
		theEUAIPackage.createPackageContents();
		theGDPRPackage.createPackageContents();
		theISO27Package.createPackageContents();
		theKritisPackage.createPackageContents();
		theOpenDataPackage.createPackageContents();
		theDS91377Package.createPackageContents();

		// Initialize created meta-data
		theISO9001Package.initializePackageContents();
		theDataQSPackage.initializePackageContents();
		theEUAIPackage.initializePackageContents();
		theGDPRPackage.initializePackageContents();
		theISO27Package.initializePackageContents();
		theKritisPackage.initializePackageContents();
		theOpenDataPackage.initializePackageContents();
		theDS91377Package.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theISO9001Package.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ISO9001Package.eNS_URI, theISO9001Package);
		return theISO9001Package;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getISO9001PolicyCheck() {
		return iso9001PolicyCheckEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISO9001PolicyCheck_ProcessApproachApplied() {
		return (EAttribute)iso9001PolicyCheckEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISO9001PolicyCheck_DocumentedInformationMaintained() {
		return (EAttribute)iso9001PolicyCheckEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISO9001PolicyCheck_CustomerFocusDemonstrated() {
		return (EAttribute)iso9001PolicyCheckEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISO9001PolicyCheck_NonconformityProcessExists() {
		return (EAttribute)iso9001PolicyCheckEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISO9001PolicyCheck_ResourceAndCompetenceVerified() {
		return (EAttribute)iso9001PolicyCheckEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ISO9001Factory getISO9001Factory() {
		return (ISO9001Factory)getEFactoryInstance();
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
		iso9001PolicyCheckEClass = createEClass(ISO9001_POLICY_CHECK);
		createEAttribute(iso9001PolicyCheckEClass, ISO9001_POLICY_CHECK__PROCESS_APPROACH_APPLIED);
		createEAttribute(iso9001PolicyCheckEClass, ISO9001_POLICY_CHECK__DOCUMENTED_INFORMATION_MAINTAINED);
		createEAttribute(iso9001PolicyCheckEClass, ISO9001_POLICY_CHECK__CUSTOMER_FOCUS_DEMONSTRATED);
		createEAttribute(iso9001PolicyCheckEClass, ISO9001_POLICY_CHECK__NONCONFORMITY_PROCESS_EXISTS);
		createEAttribute(iso9001PolicyCheckEClass, ISO9001_POLICY_CHECK__RESOURCE_AND_COMPETENCE_VERIFIED);
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
		iso9001PolicyCheckEClass.getESuperTypes().add(theGovernancePackage.getComplianceCheckResult());

		// Initialize classes, features, and operations; add parameters
		initEClass(iso9001PolicyCheckEClass, ISO9001PolicyCheck.class, "ISO9001PolicyCheck", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getISO9001PolicyCheck_ProcessApproachApplied(), ecorePackage.getEBoolean(), "processApproachApplied", "false", 1, 1, ISO9001PolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISO9001PolicyCheck_DocumentedInformationMaintained(), ecorePackage.getEBoolean(), "documentedInformationMaintained", "false", 1, 1, ISO9001PolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISO9001PolicyCheck_CustomerFocusDemonstrated(), ecorePackage.getEBoolean(), "customerFocusDemonstrated", "false", 1, 1, ISO9001PolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISO9001PolicyCheck_NonconformityProcessExists(), ecorePackage.getEBoolean(), "nonconformityProcessExists", "false", 1, 1, ISO9001PolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISO9001PolicyCheck_ResourceAndCompetenceVerified(), ecorePackage.getEBoolean(), "resourceAndCompetenceVerified", "false", 1, 1, ISO9001PolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //ISO9001PackageImpl
