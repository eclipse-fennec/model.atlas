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
package org.eclipse.fennec.model.atlas.registry.api.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;

import org.eclipse.fennec.model.atlas.registry.api.BatchItemResult;
import org.eclipse.fennec.model.atlas.registry.api.BatchResult;
import org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy;
import org.eclipse.fennec.model.atlas.registry.api.ObjectAddress;
import org.eclipse.fennec.model.atlas.registry.api.ObjectBatch;
import org.eclipse.fennec.model.atlas.registry.api.ObjectWrite;
import org.eclipse.fennec.model.atlas.registry.api.PackageAddress;
import org.eclipse.fennec.model.atlas.registry.api.PackageBatch;
import org.eclipse.fennec.model.atlas.registry.api.PackagePropertiesUpdate;
import org.eclipse.fennec.model.atlas.registry.api.PackageSearch;
import org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult;
import org.eclipse.fennec.model.atlas.registry.api.PackageWrite;
import org.eclipse.fennec.model.atlas.registry.api.Precondition;
import org.eclipse.fennec.model.atlas.registry.api.PreconditionTarget;
import org.eclipse.fennec.model.atlas.registry.api.RefusalReason;
import org.eclipse.fennec.model.atlas.registry.api.RegistryApiFactory;
import org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage;
import org.eclipse.fennec.model.atlas.registry.api.RegistryReader;
import org.eclipse.fennec.model.atlas.registry.api.RegistryWriter;
import org.eclipse.fennec.model.atlas.registry.api.WriteOutcome;
import org.eclipse.fennec.model.atlas.registry.api.WriteResult;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class RegistryApiPackageImpl extends EPackageImpl implements RegistryApiPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass registryWriterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass registryReaderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectAddressEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass packageAddressEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass preconditionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectWriteEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectBatchEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass packageWriteEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass packageBatchEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass packagePropertiesUpdateEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass packageSearchEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass packageSearchResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass writeResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass batchItemResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass batchResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum conflictPolicyEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum writeOutcomeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum refusalReasonEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum preconditionTargetEEnum = null;

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
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private RegistryApiPackageImpl() {
		super(eNS_URI, RegistryApiFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link RegistryApiPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static RegistryApiPackage init() {
		if (isInited) return (RegistryApiPackage)EPackage.Registry.INSTANCE.getEPackage(RegistryApiPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredRegistryApiPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		RegistryApiPackageImpl theRegistryApiPackage = registeredRegistryApiPackage instanceof RegistryApiPackageImpl ? (RegistryApiPackageImpl)registeredRegistryApiPackage : new RegistryApiPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		ManagementPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theRegistryApiPackage.createPackageContents();

		// Initialize created meta-data
		theRegistryApiPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theRegistryApiPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(RegistryApiPackage.eNS_URI, theRegistryApiPackage);
		return theRegistryApiPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRegistryWriter() {
		return registryWriterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryWriter__CreateObject__ObjectWrite() {
		return registryWriterEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryWriter__UpdateObject__ObjectWrite() {
		return registryWriterEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryWriter__DeleteObject__ObjectAddress_boolean_Precondition() {
		return registryWriterEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryWriter__TransitionObject__ObjectAddress_String_boolean_Precondition() {
		return registryWriterEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryWriter__CreatePackage__PackageWrite() {
		return registryWriterEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryWriter__UpdatePackage__PackageWrite() {
		return registryWriterEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryWriter__UpdatePackageProperties__PackagePropertiesUpdate() {
		return registryWriterEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryWriter__DeletePackage__PackageAddress_boolean_Precondition() {
		return registryWriterEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryWriter__TransitionPackage__String_String_String_String_boolean_Precondition() {
		return registryWriterEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryWriter__WriteObjects__ObjectBatch() {
		return registryWriterEClass.getEOperations().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryWriter__WritePackages__PackageBatch() {
		return registryWriterEClass.getEOperations().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRegistryReader() {
		return registryReaderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__SchemaRegistry__String() {
		return registryReaderEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__ListAll__String_String() {
		return registryReaderEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__ListFinal__String_String() {
		return registryReaderEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__List__String_String_String() {
		return registryReaderEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__ListByName__String_String_String_String() {
		return registryReaderEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__Metadata__ObjectAddress() {
		return registryReaderEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__FinalMetadata__String_String_String() {
		return registryReaderEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__Content__ObjectAddress() {
		return registryReaderEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__FinalContent__String_String_String() {
		return registryReaderEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__PackageMetadata__PackageAddress() {
		return registryReaderEClass.getEOperations().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__FinalPackageMetadata__String_String() {
		return registryReaderEClass.getEOperations().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getRegistryReader__SearchPackages__String_PackageSearch() {
		return registryReaderEClass.getEOperations().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getObjectAddress() {
		return objectAddressEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectAddress_Scope() {
		return (EAttribute)objectAddressEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectAddress_Registry() {
		return (EAttribute)objectAddressEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectAddress_Stage() {
		return (EAttribute)objectAddressEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectAddress_ObjectId() {
		return (EAttribute)objectAddressEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPackageAddress() {
		return packageAddressEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageAddress_Scope() {
		return (EAttribute)packageAddressEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageAddress_Stage() {
		return (EAttribute)packageAddressEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageAddress_NsUri() {
		return (EAttribute)packageAddressEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPrecondition() {
		return preconditionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPrecondition_Expected() {
		return (EAttribute)preconditionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPrecondition_Target() {
		return (EAttribute)preconditionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getPrecondition__IsSatisfiedBy__ObjectMetadata() {
		return preconditionEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getObjectWrite() {
		return objectWriteEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getObjectWrite_Address() {
		return (EReference)objectWriteEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getObjectWrite_Content() {
		return (EReference)objectWriteEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectWrite_Name() {
		return (EAttribute)objectWriteEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectWrite_Version() {
		return (EAttribute)objectWriteEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectWrite_OnConflict() {
		return (EAttribute)objectWriteEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getObjectWrite_Precondition() {
		return (EReference)objectWriteEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getObjectBatch() {
		return objectBatchEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getObjectBatch_Writes() {
		return (EReference)objectBatchEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPackageWrite() {
		return packageWriteEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageWrite_Scope() {
		return (EAttribute)packageWriteEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageWrite_Registry() {
		return (EAttribute)packageWriteEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageWrite_Stage() {
		return (EAttribute)packageWriteEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPackageWrite_Content() {
		return (EReference)packageWriteEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageWrite_NsUri() {
		return (EAttribute)packageWriteEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageWrite_Name() {
		return (EAttribute)packageWriteEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageWrite_Version() {
		return (EAttribute)packageWriteEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageWrite_Dcat() {
		return (EAttribute)packageWriteEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageWrite_OnConflict() {
		return (EAttribute)packageWriteEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPackageWrite_Precondition() {
		return (EReference)packageWriteEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageWrite_LenientVersion() {
		return (EAttribute)packageWriteEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPackageBatch() {
		return packageBatchEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPackageBatch_Writes() {
		return (EReference)packageBatchEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPackagePropertiesUpdate() {
		return packagePropertiesUpdateEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPackagePropertiesUpdate_Address() {
		return (EReference)packagePropertiesUpdateEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPackagePropertiesUpdate_Properties() {
		return (EReference)packagePropertiesUpdateEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPackagePropertiesUpdate_Precondition() {
		return (EReference)packagePropertiesUpdateEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPackageSearch() {
		return packageSearchEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearch_Stage() {
		return (EAttribute)packageSearchEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearch_NsUri() {
		return (EAttribute)packageSearchEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearch_NsUriExact() {
		return (EAttribute)packageSearchEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearch_Name() {
		return (EAttribute)packageSearchEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearch_Prefix() {
		return (EAttribute)packageSearchEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearch_Classifier() {
		return (EAttribute)packageSearchEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearch_FeatureName() {
		return (EAttribute)packageSearchEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearch_FeatureType() {
		return (EAttribute)packageSearchEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearch_FeatureNameTypePair() {
		return (EAttribute)packageSearchEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearch_Limit() {
		return (EAttribute)packageSearchEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearch_Offset() {
		return (EAttribute)packageSearchEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPackageSearchResult() {
		return packageSearchResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPackageSearchResult_Hits() {
		return (EReference)packageSearchResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageSearchResult_TotalHits() {
		return (EAttribute)packageSearchResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getWriteResult() {
		return writeResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getWriteResult_Outcome() {
		return (EAttribute)writeResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getWriteResult_Metadata() {
		return (EReference)writeResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBatchItemResult() {
		return batchItemResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBatchItemResult_Id() {
		return (EAttribute)batchItemResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBatchItemResult_Outcome() {
		return (EAttribute)batchItemResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBatchItemResult_Metadata() {
		return (EReference)batchItemResultEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBatchItemResult_Refusal() {
		return (EAttribute)batchItemResultEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBatchItemResult_Message() {
		return (EAttribute)batchItemResultEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getBatchItemResult__IsRefused() {
		return batchItemResultEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBatchResult() {
		return batchResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBatchResult_Applied() {
		return (EAttribute)batchResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBatchResult_Items() {
		return (EReference)batchResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getBatchResult__FirstRefusal() {
		return batchResultEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getConflictPolicy() {
		return conflictPolicyEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getWriteOutcome() {
		return writeOutcomeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getRefusalReason() {
		return refusalReasonEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getPreconditionTarget() {
		return preconditionTargetEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RegistryApiFactory getRegistryApiFactory() {
		return (RegistryApiFactory)getEFactoryInstance();
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
		registryWriterEClass = createEClass(REGISTRY_WRITER);
		createEOperation(registryWriterEClass, REGISTRY_WRITER___CREATE_OBJECT__OBJECTWRITE);
		createEOperation(registryWriterEClass, REGISTRY_WRITER___UPDATE_OBJECT__OBJECTWRITE);
		createEOperation(registryWriterEClass, REGISTRY_WRITER___DELETE_OBJECT__OBJECTADDRESS_BOOLEAN_PRECONDITION);
		createEOperation(registryWriterEClass, REGISTRY_WRITER___TRANSITION_OBJECT__OBJECTADDRESS_STRING_BOOLEAN_PRECONDITION);
		createEOperation(registryWriterEClass, REGISTRY_WRITER___CREATE_PACKAGE__PACKAGEWRITE);
		createEOperation(registryWriterEClass, REGISTRY_WRITER___UPDATE_PACKAGE__PACKAGEWRITE);
		createEOperation(registryWriterEClass, REGISTRY_WRITER___UPDATE_PACKAGE_PROPERTIES__PACKAGEPROPERTIESUPDATE);
		createEOperation(registryWriterEClass, REGISTRY_WRITER___DELETE_PACKAGE__PACKAGEADDRESS_BOOLEAN_PRECONDITION);
		createEOperation(registryWriterEClass, REGISTRY_WRITER___TRANSITION_PACKAGE__STRING_STRING_STRING_STRING_BOOLEAN_PRECONDITION);
		createEOperation(registryWriterEClass, REGISTRY_WRITER___WRITE_OBJECTS__OBJECTBATCH);
		createEOperation(registryWriterEClass, REGISTRY_WRITER___WRITE_PACKAGES__PACKAGEBATCH);

		registryReaderEClass = createEClass(REGISTRY_READER);
		createEOperation(registryReaderEClass, REGISTRY_READER___SCHEMA_REGISTRY__STRING);
		createEOperation(registryReaderEClass, REGISTRY_READER___LIST_ALL__STRING_STRING);
		createEOperation(registryReaderEClass, REGISTRY_READER___LIST_FINAL__STRING_STRING);
		createEOperation(registryReaderEClass, REGISTRY_READER___LIST__STRING_STRING_STRING);
		createEOperation(registryReaderEClass, REGISTRY_READER___LIST_BY_NAME__STRING_STRING_STRING_STRING);
		createEOperation(registryReaderEClass, REGISTRY_READER___METADATA__OBJECTADDRESS);
		createEOperation(registryReaderEClass, REGISTRY_READER___FINAL_METADATA__STRING_STRING_STRING);
		createEOperation(registryReaderEClass, REGISTRY_READER___CONTENT__OBJECTADDRESS);
		createEOperation(registryReaderEClass, REGISTRY_READER___FINAL_CONTENT__STRING_STRING_STRING);
		createEOperation(registryReaderEClass, REGISTRY_READER___PACKAGE_METADATA__PACKAGEADDRESS);
		createEOperation(registryReaderEClass, REGISTRY_READER___FINAL_PACKAGE_METADATA__STRING_STRING);
		createEOperation(registryReaderEClass, REGISTRY_READER___SEARCH_PACKAGES__STRING_PACKAGESEARCH);

		objectAddressEClass = createEClass(OBJECT_ADDRESS);
		createEAttribute(objectAddressEClass, OBJECT_ADDRESS__SCOPE);
		createEAttribute(objectAddressEClass, OBJECT_ADDRESS__REGISTRY);
		createEAttribute(objectAddressEClass, OBJECT_ADDRESS__STAGE);
		createEAttribute(objectAddressEClass, OBJECT_ADDRESS__OBJECT_ID);

		packageAddressEClass = createEClass(PACKAGE_ADDRESS);
		createEAttribute(packageAddressEClass, PACKAGE_ADDRESS__SCOPE);
		createEAttribute(packageAddressEClass, PACKAGE_ADDRESS__STAGE);
		createEAttribute(packageAddressEClass, PACKAGE_ADDRESS__NS_URI);

		preconditionEClass = createEClass(PRECONDITION);
		createEAttribute(preconditionEClass, PRECONDITION__EXPECTED);
		createEAttribute(preconditionEClass, PRECONDITION__TARGET);
		createEOperation(preconditionEClass, PRECONDITION___IS_SATISFIED_BY__OBJECTMETADATA);

		objectWriteEClass = createEClass(OBJECT_WRITE);
		createEReference(objectWriteEClass, OBJECT_WRITE__ADDRESS);
		createEReference(objectWriteEClass, OBJECT_WRITE__CONTENT);
		createEAttribute(objectWriteEClass, OBJECT_WRITE__NAME);
		createEAttribute(objectWriteEClass, OBJECT_WRITE__VERSION);
		createEAttribute(objectWriteEClass, OBJECT_WRITE__ON_CONFLICT);
		createEReference(objectWriteEClass, OBJECT_WRITE__PRECONDITION);

		objectBatchEClass = createEClass(OBJECT_BATCH);
		createEReference(objectBatchEClass, OBJECT_BATCH__WRITES);

		packageWriteEClass = createEClass(PACKAGE_WRITE);
		createEAttribute(packageWriteEClass, PACKAGE_WRITE__SCOPE);
		createEAttribute(packageWriteEClass, PACKAGE_WRITE__REGISTRY);
		createEAttribute(packageWriteEClass, PACKAGE_WRITE__STAGE);
		createEReference(packageWriteEClass, PACKAGE_WRITE__CONTENT);
		createEAttribute(packageWriteEClass, PACKAGE_WRITE__NS_URI);
		createEAttribute(packageWriteEClass, PACKAGE_WRITE__NAME);
		createEAttribute(packageWriteEClass, PACKAGE_WRITE__VERSION);
		createEAttribute(packageWriteEClass, PACKAGE_WRITE__DCAT);
		createEAttribute(packageWriteEClass, PACKAGE_WRITE__ON_CONFLICT);
		createEReference(packageWriteEClass, PACKAGE_WRITE__PRECONDITION);
		createEAttribute(packageWriteEClass, PACKAGE_WRITE__LENIENT_VERSION);

		packageBatchEClass = createEClass(PACKAGE_BATCH);
		createEReference(packageBatchEClass, PACKAGE_BATCH__WRITES);

		packagePropertiesUpdateEClass = createEClass(PACKAGE_PROPERTIES_UPDATE);
		createEReference(packagePropertiesUpdateEClass, PACKAGE_PROPERTIES_UPDATE__ADDRESS);
		createEReference(packagePropertiesUpdateEClass, PACKAGE_PROPERTIES_UPDATE__PROPERTIES);
		createEReference(packagePropertiesUpdateEClass, PACKAGE_PROPERTIES_UPDATE__PRECONDITION);

		packageSearchEClass = createEClass(PACKAGE_SEARCH);
		createEAttribute(packageSearchEClass, PACKAGE_SEARCH__STAGE);
		createEAttribute(packageSearchEClass, PACKAGE_SEARCH__NS_URI);
		createEAttribute(packageSearchEClass, PACKAGE_SEARCH__NS_URI_EXACT);
		createEAttribute(packageSearchEClass, PACKAGE_SEARCH__NAME);
		createEAttribute(packageSearchEClass, PACKAGE_SEARCH__PREFIX);
		createEAttribute(packageSearchEClass, PACKAGE_SEARCH__CLASSIFIER);
		createEAttribute(packageSearchEClass, PACKAGE_SEARCH__FEATURE_NAME);
		createEAttribute(packageSearchEClass, PACKAGE_SEARCH__FEATURE_TYPE);
		createEAttribute(packageSearchEClass, PACKAGE_SEARCH__FEATURE_NAME_TYPE_PAIR);
		createEAttribute(packageSearchEClass, PACKAGE_SEARCH__LIMIT);
		createEAttribute(packageSearchEClass, PACKAGE_SEARCH__OFFSET);

		packageSearchResultEClass = createEClass(PACKAGE_SEARCH_RESULT);
		createEReference(packageSearchResultEClass, PACKAGE_SEARCH_RESULT__HITS);
		createEAttribute(packageSearchResultEClass, PACKAGE_SEARCH_RESULT__TOTAL_HITS);

		writeResultEClass = createEClass(WRITE_RESULT);
		createEAttribute(writeResultEClass, WRITE_RESULT__OUTCOME);
		createEReference(writeResultEClass, WRITE_RESULT__METADATA);

		batchItemResultEClass = createEClass(BATCH_ITEM_RESULT);
		createEAttribute(batchItemResultEClass, BATCH_ITEM_RESULT__ID);
		createEAttribute(batchItemResultEClass, BATCH_ITEM_RESULT__OUTCOME);
		createEReference(batchItemResultEClass, BATCH_ITEM_RESULT__METADATA);
		createEAttribute(batchItemResultEClass, BATCH_ITEM_RESULT__REFUSAL);
		createEAttribute(batchItemResultEClass, BATCH_ITEM_RESULT__MESSAGE);
		createEOperation(batchItemResultEClass, BATCH_ITEM_RESULT___IS_REFUSED);

		batchResultEClass = createEClass(BATCH_RESULT);
		createEAttribute(batchResultEClass, BATCH_RESULT__APPLIED);
		createEReference(batchResultEClass, BATCH_RESULT__ITEMS);
		createEOperation(batchResultEClass, BATCH_RESULT___FIRST_REFUSAL);

		// Create enums
		conflictPolicyEEnum = createEEnum(CONFLICT_POLICY);
		writeOutcomeEEnum = createEEnum(WRITE_OUTCOME);
		refusalReasonEEnum = createEEnum(REFUSAL_REASON);
		preconditionTargetEEnum = createEEnum(PRECONDITION_TARGET);
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
		ManagementPackage theManagementPackage = (ManagementPackage)EPackage.Registry.INSTANCE.getEPackage(ManagementPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(registryWriterEClass, RegistryWriter.class, "RegistryWriter", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		EOperation op = initEOperation(getRegistryWriter__CreateObject__ObjectWrite(), this.getWriteResult(), "createObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getObjectWrite(), "write", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryWriter__UpdateObject__ObjectWrite(), this.getWriteResult(), "updateObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getObjectWrite(), "write", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryWriter__DeleteObject__ObjectAddress_boolean_Precondition(), this.getWriteResult(), "deleteObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getObjectAddress(), "address", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "force", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getPrecondition(), "precondition", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryWriter__TransitionObject__ObjectAddress_String_boolean_Precondition(), this.getWriteResult(), "transitionObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getObjectAddress(), "address", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "targetStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "overwrite", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getPrecondition(), "precondition", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryWriter__CreatePackage__PackageWrite(), this.getWriteResult(), "createPackage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getPackageWrite(), "write", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryWriter__UpdatePackage__PackageWrite(), this.getWriteResult(), "updatePackage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getPackageWrite(), "write", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryWriter__UpdatePackageProperties__PackagePropertiesUpdate(), this.getWriteResult(), "updatePackageProperties", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getPackagePropertiesUpdate(), "update", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryWriter__DeletePackage__PackageAddress_boolean_Precondition(), this.getWriteResult(), "deletePackage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getPackageAddress(), "address", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "force", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getPrecondition(), "precondition", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryWriter__TransitionPackage__String_String_String_String_boolean_Precondition(), this.getWriteResult(), "transitionPackage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "idOrNsUri", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "targetStage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEBoolean(), "overwrite", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getPrecondition(), "precondition", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryWriter__WriteObjects__ObjectBatch(), this.getBatchResult(), "writeObjects", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getObjectBatch(), "batch", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryWriter__WritePackages__PackageBatch(), this.getBatchResult(), "writePackages", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getPackageBatch(), "batch", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(registryReaderEClass, RegistryReader.class, "RegistryReader", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		op = initEOperation(getRegistryReader__SchemaRegistry__String(), ecorePackage.getEString(), "schemaRegistry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryReader__ListAll__String_String(), theManagementPackage.getObjectMetadata(), "listAll", 0, -1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryReader__ListFinal__String_String(), theManagementPackage.getObjectMetadata(), "listFinal", 0, -1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryReader__List__String_String_String(), theManagementPackage.getObjectMetadata(), "list", 0, -1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryReader__ListByName__String_String_String_String(), theManagementPackage.getObjectMetadata(), "listByName", 0, -1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "stage", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "namePattern", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryReader__Metadata__ObjectAddress(), theManagementPackage.getObjectMetadata(), "metadata", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getObjectAddress(), "address", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryReader__FinalMetadata__String_String_String(), theManagementPackage.getObjectMetadata(), "finalMetadata", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryReader__Content__ObjectAddress(), ecorePackage.getEObject(), "content", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getObjectAddress(), "address", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryReader__FinalContent__String_String_String(), ecorePackage.getEObject(), "finalContent", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "registry", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "objectId", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryReader__PackageMetadata__PackageAddress(), theManagementPackage.getObjectMetadata(), "packageMetadata", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getPackageAddress(), "address", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryReader__FinalPackageMetadata__String_String(), theManagementPackage.getObjectMetadata(), "finalPackageMetadata", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "nsUri", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = initEOperation(getRegistryReader__SearchPackages__String_PackageSearch(), this.getPackageSearchResult(), "searchPackages", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "scope", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getPackageSearch(), "search", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(objectAddressEClass, ObjectAddress.class, "ObjectAddress", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getObjectAddress_Scope(), ecorePackage.getEString(), "scope", null, 0, 1, ObjectAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectAddress_Registry(), ecorePackage.getEString(), "registry", null, 0, 1, ObjectAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectAddress_Stage(), ecorePackage.getEString(), "stage", null, 0, 1, ObjectAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectAddress_ObjectId(), ecorePackage.getEString(), "objectId", null, 0, 1, ObjectAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(packageAddressEClass, PackageAddress.class, "PackageAddress", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPackageAddress_Scope(), ecorePackage.getEString(), "scope", null, 0, 1, PackageAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageAddress_Stage(), ecorePackage.getEString(), "stage", null, 0, 1, PackageAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageAddress_NsUri(), ecorePackage.getEString(), "nsUri", null, 0, 1, PackageAddress.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(preconditionEClass, Precondition.class, "Precondition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPrecondition_Expected(), ecorePackage.getEString(), "expected", null, 0, -1, Precondition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPrecondition_Target(), this.getPreconditionTarget(), "target", null, 0, 1, Precondition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = initEOperation(getPrecondition__IsSatisfiedBy__ObjectMetadata(), ecorePackage.getEBoolean(), "isSatisfiedBy", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theManagementPackage.getObjectMetadata(), "current", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(objectWriteEClass, ObjectWrite.class, "ObjectWrite", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getObjectWrite_Address(), this.getObjectAddress(), null, "address", null, 0, 1, ObjectWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getObjectWrite_Content(), ecorePackage.getEObject(), null, "content", null, 0, 1, ObjectWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectWrite_Name(), ecorePackage.getEString(), "name", null, 0, 1, ObjectWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectWrite_Version(), ecorePackage.getEString(), "version", null, 0, 1, ObjectWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectWrite_OnConflict(), this.getConflictPolicy(), "onConflict", null, 0, 1, ObjectWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getObjectWrite_Precondition(), this.getPrecondition(), null, "precondition", null, 0, 1, ObjectWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(objectBatchEClass, ObjectBatch.class, "ObjectBatch", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getObjectBatch_Writes(), this.getObjectWrite(), null, "writes", null, 0, -1, ObjectBatch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(packageWriteEClass, PackageWrite.class, "PackageWrite", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPackageWrite_Scope(), ecorePackage.getEString(), "scope", null, 0, 1, PackageWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageWrite_Registry(), ecorePackage.getEString(), "registry", null, 0, 1, PackageWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageWrite_Stage(), ecorePackage.getEString(), "stage", null, 0, 1, PackageWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPackageWrite_Content(), ecorePackage.getEPackage(), null, "content", null, 0, 1, PackageWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageWrite_NsUri(), ecorePackage.getEString(), "nsUri", null, 0, 1, PackageWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageWrite_Name(), ecorePackage.getEString(), "name", null, 0, 1, PackageWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageWrite_Version(), ecorePackage.getEString(), "version", null, 0, 1, PackageWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageWrite_Dcat(), ecorePackage.getEBooleanObject(), "dcat", null, 0, 1, PackageWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageWrite_OnConflict(), this.getConflictPolicy(), "onConflict", null, 0, 1, PackageWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPackageWrite_Precondition(), this.getPrecondition(), null, "precondition", null, 0, 1, PackageWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageWrite_LenientVersion(), ecorePackage.getEBoolean(), "lenientVersion", null, 0, 1, PackageWrite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(packageBatchEClass, PackageBatch.class, "PackageBatch", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getPackageBatch_Writes(), this.getPackageWrite(), null, "writes", null, 0, -1, PackageBatch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(packagePropertiesUpdateEClass, PackagePropertiesUpdate.class, "PackagePropertiesUpdate", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getPackagePropertiesUpdate_Address(), this.getPackageAddress(), null, "address", null, 0, 1, PackagePropertiesUpdate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPackagePropertiesUpdate_Properties(), ecorePackage.getEStringToStringMapEntry(), null, "properties", null, 0, -1, PackagePropertiesUpdate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPackagePropertiesUpdate_Precondition(), this.getPrecondition(), null, "precondition", null, 0, 1, PackagePropertiesUpdate.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(packageSearchEClass, PackageSearch.class, "PackageSearch", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPackageSearch_Stage(), ecorePackage.getEString(), "stage", null, 0, 1, PackageSearch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageSearch_NsUri(), ecorePackage.getEString(), "nsUri", null, 0, 1, PackageSearch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageSearch_NsUriExact(), ecorePackage.getEString(), "nsUriExact", null, 0, 1, PackageSearch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageSearch_Name(), ecorePackage.getEString(), "name", null, 0, 1, PackageSearch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageSearch_Prefix(), ecorePackage.getEString(), "prefix", null, 0, 1, PackageSearch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageSearch_Classifier(), ecorePackage.getEString(), "classifier", null, 0, 1, PackageSearch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageSearch_FeatureName(), ecorePackage.getEString(), "featureName", null, 0, 1, PackageSearch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageSearch_FeatureType(), ecorePackage.getEString(), "featureType", null, 0, 1, PackageSearch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageSearch_FeatureNameTypePair(), ecorePackage.getEString(), "featureNameTypePair", null, 0, 1, PackageSearch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageSearch_Limit(), ecorePackage.getEInt(), "limit", null, 0, 1, PackageSearch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageSearch_Offset(), ecorePackage.getEInt(), "offset", null, 0, 1, PackageSearch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(packageSearchResultEClass, PackageSearchResult.class, "PackageSearchResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getPackageSearchResult_Hits(), theManagementPackage.getObjectMetadata(), null, "hits", null, 0, -1, PackageSearchResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageSearchResult_TotalHits(), ecorePackage.getELong(), "totalHits", null, 0, 1, PackageSearchResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(writeResultEClass, WriteResult.class, "WriteResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getWriteResult_Outcome(), this.getWriteOutcome(), "outcome", null, 0, 1, WriteResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getWriteResult_Metadata(), theManagementPackage.getObjectMetadata(), null, "metadata", null, 0, 1, WriteResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(batchItemResultEClass, BatchItemResult.class, "BatchItemResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getBatchItemResult_Id(), ecorePackage.getEString(), "id", null, 0, 1, BatchItemResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBatchItemResult_Outcome(), this.getWriteOutcome(), "outcome", null, 0, 1, BatchItemResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBatchItemResult_Metadata(), theManagementPackage.getObjectMetadata(), null, "metadata", null, 0, 1, BatchItemResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBatchItemResult_Refusal(), this.getRefusalReason(), "refusal", null, 0, 1, BatchItemResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getBatchItemResult_Message(), ecorePackage.getEString(), "message", null, 0, 1, BatchItemResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEOperation(getBatchItemResult__IsRefused(), ecorePackage.getEBoolean(), "isRefused", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(batchResultEClass, BatchResult.class, "BatchResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getBatchResult_Applied(), ecorePackage.getEBoolean(), "applied", null, 0, 1, BatchResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBatchResult_Items(), this.getBatchItemResult(), null, "items", null, 0, -1, BatchResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEOperation(getBatchResult__FirstRefusal(), this.getBatchItemResult(), "firstRefusal", 0, 1, IS_UNIQUE, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(conflictPolicyEEnum, ConflictPolicy.class, "ConflictPolicy");
		addEEnumLiteral(conflictPolicyEEnum, ConflictPolicy.FAIL);
		addEEnumLiteral(conflictPolicyEEnum, ConflictPolicy.OVERWRITE);
		addEEnumLiteral(conflictPolicyEEnum, ConflictPolicy.SKIP);

		initEEnum(writeOutcomeEEnum, WriteOutcome.class, "WriteOutcome");
		addEEnumLiteral(writeOutcomeEEnum, WriteOutcome.CREATED);
		addEEnumLiteral(writeOutcomeEEnum, WriteOutcome.UPDATED);
		addEEnumLiteral(writeOutcomeEEnum, WriteOutcome.UNCHANGED);
		addEEnumLiteral(writeOutcomeEEnum, WriteOutcome.SKIPPED);
		addEEnumLiteral(writeOutcomeEEnum, WriteOutcome.DELETED);
		addEEnumLiteral(writeOutcomeEEnum, WriteOutcome.TRANSITIONED);
		addEEnumLiteral(writeOutcomeEEnum, WriteOutcome.ALREADY_IN_TARGET);
		addEEnumLiteral(writeOutcomeEEnum, WriteOutcome.NOT_FOUND);

		initEEnum(refusalReasonEEnum, RefusalReason.class, "RefusalReason");
		addEEnumLiteral(refusalReasonEEnum, RefusalReason.INVALID);
		addEEnumLiteral(refusalReasonEEnum, RefusalReason.INCOMPATIBLE_TYPE);
		addEEnumLiteral(refusalReasonEEnum, RefusalReason.DERIVED_CONTENT);
		addEEnumLiteral(refusalReasonEEnum, RefusalReason.UNKNOWN_SCOPE);
		addEEnumLiteral(refusalReasonEEnum, RefusalReason.UNKNOWN_REGISTRY);
		addEEnumLiteral(refusalReasonEEnum, RefusalReason.NO_SCHEMA_REGISTRY);
		addEEnumLiteral(refusalReasonEEnum, RefusalReason.ALREADY_EXISTS);
		addEEnumLiteral(refusalReasonEEnum, RefusalReason.INHERITED);
		addEEnumLiteral(refusalReasonEEnum, RefusalReason.READ_ONLY);
		addEEnumLiteral(refusalReasonEEnum, RefusalReason.PRECONDITION_FAILED);
		addEEnumLiteral(refusalReasonEEnum, RefusalReason.INTERRUPTED);

		initEEnum(preconditionTargetEEnum, PreconditionTarget.class, "PreconditionTarget");
		addEEnumLiteral(preconditionTargetEEnum, PreconditionTarget.CONTENT);
		addEEnumLiteral(preconditionTargetEEnum, PreconditionTarget.METADATA);

		// Create resource
		createResource(eNS_URI);
	}

} //RegistryApiPackageImpl
