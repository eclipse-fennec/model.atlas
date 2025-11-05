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
package org.eclipse.fennec.model.atlas.policy.iso27001.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.governance.GovernancePackage;

import org.eclipse.fennec.model.atlas.policy.dataqs.DataQSPackage;

import org.eclipse.fennec.model.atlas.policy.dataqs.impl.DataQSPackageImpl;

import org.eclipse.fennec.model.atlas.policy.dinspec91377.DS91377Package;

import org.eclipse.fennec.model.atlas.policy.dinspec91377.impl.DS91377PackageImpl;

import org.eclipse.fennec.model.atlas.policy.euaiact.EUAIPackage;

import org.eclipse.fennec.model.atlas.policy.euaiact.impl.EUAIPackageImpl;

import org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPackage;

import org.eclipse.fennec.model.atlas.policy.gdpr.impl.GDPRPackageImpl;

import org.eclipse.fennec.model.atlas.policy.iso27001.ISO27001PolicyCheck;
import org.eclipse.fennec.model.atlas.policy.iso27001.ISO27Factory;
import org.eclipse.fennec.model.atlas.policy.iso27001.ISO27Package;

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
public class ISO27PackageImpl extends EPackageImpl implements ISO27Package {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass iso27001PolicyCheckEClass = null;

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
	 * @see org.eclipse.fennec.model.atlas.policy.iso27001.ISO27Package#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ISO27PackageImpl() {
		super(eNS_URI, ISO27Factory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ISO27Package#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ISO27Package init() {
		if (isInited) return (ISO27Package)EPackage.Registry.INSTANCE.getEPackage(ISO27Package.eNS_URI);

		// Obtain or create and register package
		Object registeredISO27Package = EPackage.Registry.INSTANCE.get(eNS_URI);
		ISO27PackageImpl theISO27Package = registeredISO27Package instanceof ISO27PackageImpl ? (ISO27PackageImpl)registeredISO27Package : new ISO27PackageImpl();

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
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(KritisPackage.eNS_URI);
		KritisPackageImpl theKritisPackage = (KritisPackageImpl)(registeredPackage instanceof KritisPackageImpl ? registeredPackage : KritisPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(OpenDataPackage.eNS_URI);
		OpenDataPackageImpl theOpenDataPackage = (OpenDataPackageImpl)(registeredPackage instanceof OpenDataPackageImpl ? registeredPackage : OpenDataPackage.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(ISO9001Package.eNS_URI);
		ISO9001PackageImpl theISO9001Package = (ISO9001PackageImpl)(registeredPackage instanceof ISO9001PackageImpl ? registeredPackage : ISO9001Package.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(DS91377Package.eNS_URI);
		DS91377PackageImpl theDS91377Package = (DS91377PackageImpl)(registeredPackage instanceof DS91377PackageImpl ? registeredPackage : DS91377Package.eINSTANCE);

		// Create package meta-data objects
		theISO27Package.createPackageContents();
		theDataQSPackage.createPackageContents();
		theEUAIPackage.createPackageContents();
		theGDPRPackage.createPackageContents();
		theKritisPackage.createPackageContents();
		theOpenDataPackage.createPackageContents();
		theISO9001Package.createPackageContents();
		theDS91377Package.createPackageContents();

		// Initialize created meta-data
		theISO27Package.initializePackageContents();
		theDataQSPackage.initializePackageContents();
		theEUAIPackage.initializePackageContents();
		theGDPRPackage.initializePackageContents();
		theKritisPackage.initializePackageContents();
		theOpenDataPackage.initializePackageContents();
		theISO9001Package.initializePackageContents();
		theDS91377Package.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theISO27Package.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ISO27Package.eNS_URI, theISO27Package);
		return theISO27Package;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getISO27001PolicyCheck() {
		return iso27001PolicyCheckEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISO27001PolicyCheck_NetworkControlImplemented() {
		return (EAttribute)iso27001PolicyCheckEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISO27001PolicyCheck_AccessControlVerified() {
		return (EAttribute)iso27001PolicyCheckEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISO27001PolicyCheck_LoggingAndMonitoringActive() {
		return (EAttribute)iso27001PolicyCheckEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getISO27001PolicyCheck_AssetManagementCompliant() {
		return (EAttribute)iso27001PolicyCheckEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ISO27Factory getISO27Factory() {
		return (ISO27Factory)getEFactoryInstance();
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
		iso27001PolicyCheckEClass = createEClass(ISO27001_POLICY_CHECK);
		createEAttribute(iso27001PolicyCheckEClass, ISO27001_POLICY_CHECK__NETWORK_CONTROL_IMPLEMENTED);
		createEAttribute(iso27001PolicyCheckEClass, ISO27001_POLICY_CHECK__ACCESS_CONTROL_VERIFIED);
		createEAttribute(iso27001PolicyCheckEClass, ISO27001_POLICY_CHECK__LOGGING_AND_MONITORING_ACTIVE);
		createEAttribute(iso27001PolicyCheckEClass, ISO27001_POLICY_CHECK__ASSET_MANAGEMENT_COMPLIANT);
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
		iso27001PolicyCheckEClass.getESuperTypes().add(theGovernancePackage.getComplianceCheckResult());

		// Initialize classes, features, and operations; add parameters
		initEClass(iso27001PolicyCheckEClass, ISO27001PolicyCheck.class, "ISO27001PolicyCheck", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getISO27001PolicyCheck_NetworkControlImplemented(), ecorePackage.getEBoolean(), "networkControlImplemented", "false", 1, 1, ISO27001PolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISO27001PolicyCheck_AccessControlVerified(), ecorePackage.getEBoolean(), "accessControlVerified", "false", 1, 1, ISO27001PolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISO27001PolicyCheck_LoggingAndMonitoringActive(), ecorePackage.getEBoolean(), "loggingAndMonitoringActive", "false", 1, 1, ISO27001PolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getISO27001PolicyCheck_AssetManagementCompliant(), ecorePackage.getEBoolean(), "assetManagementCompliant", "false", 1, 1, ISO27001PolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);
	}

} //ISO27PackageImpl
