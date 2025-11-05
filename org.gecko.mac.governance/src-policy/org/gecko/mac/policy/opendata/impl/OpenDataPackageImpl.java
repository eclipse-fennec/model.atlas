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
package org.gecko.mac.policy.opendata.impl;

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

import org.gecko.mac.policy.euaiact.EUAIPackage;

import org.gecko.mac.policy.euaiact.impl.EUAIPackageImpl;

import org.gecko.mac.policy.gdpr.GDPRPackage;

import org.gecko.mac.policy.gdpr.impl.GDPRPackageImpl;

import org.gecko.mac.policy.iso27001.ISO27Package;

import org.gecko.mac.policy.iso27001.impl.ISO27PackageImpl;

import org.gecko.mac.policy.iso9001.ISO9001Package;

import org.gecko.mac.policy.iso9001.impl.ISO9001PackageImpl;

import org.gecko.mac.policy.kritis.KritisPackage;

import org.gecko.mac.policy.kritis.impl.KritisPackageImpl;

import org.gecko.mac.policy.opendata.OpenDataFactory;
import org.gecko.mac.policy.opendata.OpenDataLicense;
import org.gecko.mac.policy.opendata.OpenDataPackage;
import org.gecko.mac.policy.opendata.OpenDataPolicyCheck;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class OpenDataPackageImpl extends EPackageImpl implements OpenDataPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass openDataPolicyCheckEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum openDataLicenseEEnum = null;

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
	 * @see org.gecko.mac.policy.opendata.OpenDataPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private OpenDataPackageImpl() {
		super(eNS_URI, OpenDataFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link OpenDataPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static OpenDataPackage init() {
		if (isInited) return (OpenDataPackage)EPackage.Registry.INSTANCE.getEPackage(OpenDataPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredOpenDataPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		OpenDataPackageImpl theOpenDataPackage = registeredOpenDataPackage instanceof OpenDataPackageImpl ? (OpenDataPackageImpl)registeredOpenDataPackage : new OpenDataPackageImpl();

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
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(ISO9001Package.eNS_URI);
		ISO9001PackageImpl theISO9001Package = (ISO9001PackageImpl)(registeredPackage instanceof ISO9001PackageImpl ? registeredPackage : ISO9001Package.eINSTANCE);
		registeredPackage = EPackage.Registry.INSTANCE.getEPackage(DS91377Package.eNS_URI);
		DS91377PackageImpl theDS91377Package = (DS91377PackageImpl)(registeredPackage instanceof DS91377PackageImpl ? registeredPackage : DS91377Package.eINSTANCE);

		// Create package meta-data objects
		theOpenDataPackage.createPackageContents();
		theDataQSPackage.createPackageContents();
		theEUAIPackage.createPackageContents();
		theGDPRPackage.createPackageContents();
		theISO27Package.createPackageContents();
		theKritisPackage.createPackageContents();
		theISO9001Package.createPackageContents();
		theDS91377Package.createPackageContents();

		// Initialize created meta-data
		theOpenDataPackage.initializePackageContents();
		theDataQSPackage.initializePackageContents();
		theEUAIPackage.initializePackageContents();
		theGDPRPackage.initializePackageContents();
		theISO27Package.initializePackageContents();
		theKritisPackage.initializePackageContents();
		theISO9001Package.initializePackageContents();
		theDS91377Package.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theOpenDataPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(OpenDataPackage.eNS_URI, theOpenDataPackage);
		return theOpenDataPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOpenDataPolicyCheck() {
		return openDataPolicyCheckEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOpenDataPolicyCheck_PiiAnonymizationVerified() {
		return (EAttribute)openDataPolicyCheckEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOpenDataPolicyCheck_LicenseType() {
		return (EAttribute)openDataPolicyCheckEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOpenDataPolicyCheck_IsMachineReadable() {
		return (EAttribute)openDataPolicyCheckEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOpenDataPolicyCheck_HasPublicEndpoint() {
		return (EAttribute)openDataPolicyCheckEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getOpenDataLicense() {
		return openDataLicenseEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OpenDataFactory getOpenDataFactory() {
		return (OpenDataFactory)getEFactoryInstance();
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
		openDataPolicyCheckEClass = createEClass(OPEN_DATA_POLICY_CHECK);
		createEAttribute(openDataPolicyCheckEClass, OPEN_DATA_POLICY_CHECK__PII_ANONYMIZATION_VERIFIED);
		createEAttribute(openDataPolicyCheckEClass, OPEN_DATA_POLICY_CHECK__LICENSE_TYPE);
		createEAttribute(openDataPolicyCheckEClass, OPEN_DATA_POLICY_CHECK__IS_MACHINE_READABLE);
		createEAttribute(openDataPolicyCheckEClass, OPEN_DATA_POLICY_CHECK__HAS_PUBLIC_ENDPOINT);

		// Create enums
		openDataLicenseEEnum = createEEnum(OPEN_DATA_LICENSE);
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
		openDataPolicyCheckEClass.getESuperTypes().add(theGovernancePackage.getComplianceCheckResult());

		// Initialize classes, features, and operations; add parameters
		initEClass(openDataPolicyCheckEClass, OpenDataPolicyCheck.class, "OpenDataPolicyCheck", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getOpenDataPolicyCheck_PiiAnonymizationVerified(), ecorePackage.getEBoolean(), "piiAnonymizationVerified", "false", 1, 1, OpenDataPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOpenDataPolicyCheck_LicenseType(), this.getOpenDataLicense(), "licenseType", "NOT_APPLICABLE", 1, 1, OpenDataPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOpenDataPolicyCheck_IsMachineReadable(), ecorePackage.getEBoolean(), "isMachineReadable", "false", 1, 1, OpenDataPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOpenDataPolicyCheck_HasPublicEndpoint(), ecorePackage.getEBoolean(), "hasPublicEndpoint", "false", 1, 1, OpenDataPolicyCheck.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(openDataLicenseEEnum, OpenDataLicense.class, "OpenDataLicense");
		addEEnumLiteral(openDataLicenseEEnum, OpenDataLicense.NOT_APPLICABLE);
		addEEnumLiteral(openDataLicenseEEnum, OpenDataLicense.PROPRIETARY);
		addEEnumLiteral(openDataLicenseEEnum, OpenDataLicense.CC0);
		addEEnumLiteral(openDataLicenseEEnum, OpenDataLicense.CC_BY);
		addEEnumLiteral(openDataLicenseEEnum, OpenDataLicense.ODB_L);
		addEEnumLiteral(openDataLicenseEEnum, OpenDataLicense.PDDL);

		// Create resource
		createResource(eNS_URI);
	}

} //OpenDataPackageImpl
