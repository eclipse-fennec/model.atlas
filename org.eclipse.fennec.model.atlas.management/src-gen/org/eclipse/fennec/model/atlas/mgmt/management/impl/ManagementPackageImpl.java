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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.management.impl;

import java.time.Instant;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest;
import org.eclipse.fennec.model.atlas.mgmt.management.GenerationStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadataContainer;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.PackageStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ManagementPackageImpl extends EPackageImpl implements ManagementPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectMetadataEClass = null;

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
	private EClass diagnosticChangeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass stringToObjectMapEntryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectQueryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass generationRequestEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass objectMetadataContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum objectStatusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum packageStatusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum storageBackendTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum generationStatusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum diagnosticSeverityEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum diagnosticStatusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType instantEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType promiseEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType listEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType voidEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType optionalEDataType = null;

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
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ManagementPackageImpl() {
		super(eNS_URI, ManagementFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ManagementPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ManagementPackage init() {
		if (isInited) return (ManagementPackage)EPackage.Registry.INSTANCE.getEPackage(ManagementPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredManagementPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ManagementPackageImpl theManagementPackage = registeredManagementPackage instanceof ManagementPackageImpl ? (ManagementPackageImpl)registeredManagementPackage : new ManagementPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theManagementPackage.createPackageContents();

		// Initialize created meta-data
		theManagementPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theManagementPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ManagementPackage.eNS_URI, theManagementPackage);
		return theManagementPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getObjectMetadata() {
		return objectMetadataEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_UploadUser() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_UploadTime() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_SourceChannel() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ContentHash() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ObjectType() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ReviewUser() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ReviewTime() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ReviewReason() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_GenerationTriggerFingerprint() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ComplianceCheckTime() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ComplianceStatus() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_GovernanceDocumentationId() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getObjectMetadata_Properties() {
		return (EReference)objectMetadataEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_LastChangeUser() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_LastChangeTime() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_Status() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_Version() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getObjectMetadata_ObjectRef() {
		return (EReference)objectMetadataEClass.getEStructuralFeatures().get(17);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ObjectId() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(18);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_ObjectName() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(19);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_Stage() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(20);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_LastChangeReason() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(21);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_Scope() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(22);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_IsReadOnly() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(23);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_Registry() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(24);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadata_Fingerprint() {
		return (EAttribute)objectMetadataEClass.getEStructuralFeatures().get(25);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getObjectMetadata_Diagnostics() {
		return (EReference)objectMetadataEClass.getEStructuralFeatures().get(26);
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
	public EAttribute getDiagnostic_Id() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Producer() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Source() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Code() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Severity() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Message() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Category() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Target() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Status() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_CreatedTime() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_LastChangeTime() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Version() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDiagnostic_Children() {
		return (EReference)diagnosticEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDiagnostic_History() {
		return (EReference)diagnosticEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDiagnosticChange() {
		return diagnosticChangeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnosticChange_ChangeTime() {
		return (EAttribute)diagnosticChangeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnosticChange_ChangedBy() {
		return (EAttribute)diagnosticChangeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnosticChange_OldSeverity() {
		return (EAttribute)diagnosticChangeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnosticChange_NewSeverity() {
		return (EAttribute)diagnosticChangeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnosticChange_OldStatus() {
		return (EAttribute)diagnosticChangeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnosticChange_NewStatus() {
		return (EAttribute)diagnosticChangeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnosticChange_Reason() {
		return (EAttribute)diagnosticChangeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getStringToObjectMapEntry() {
		return stringToObjectMapEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStringToObjectMapEntry_Key() {
		return (EAttribute)stringToObjectMapEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getStringToObjectMapEntry_Value() {
		return (EAttribute)stringToObjectMapEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getObjectQuery() {
		return objectQueryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_UploadUser() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_SourceChannel() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_ObjectType() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_Status() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_Stage() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_Scope() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_Name() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectQuery_Registry() {
		return (EAttribute)objectQueryEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGenerationRequest() {
		return generationRequestEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_RequestId() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_JsonSample() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_JsonFingerprint() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_SourceChannel() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_RequestingUser() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_Status() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_RequestTime() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_StartTime() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_CompletionTime() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_ErrorMessage() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGenerationRequest_ResultPackageId() {
		return (EAttribute)generationRequestEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getObjectMetadataContainer() {
		return objectMetadataContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getObjectMetadataContainer_ContainerId() {
		return (EAttribute)objectMetadataContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getObjectMetadataContainer_Metadata() {
		return (EReference)objectMetadataContainerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getObjectStatus() {
		return objectStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getPackageStatus() {
		return packageStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getStorageBackendType() {
		return storageBackendTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getGenerationStatus() {
		return generationStatusEEnum;
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
	public EEnum getDiagnosticStatus() {
		return diagnosticStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getInstant() {
		return instantEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getPromise() {
		return promiseEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getList() {
		return listEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getVoid() {
		return voidEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getOptional() {
		return optionalEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ManagementFactory getManagementFactory() {
		return (ManagementFactory)getEFactoryInstance();
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
		objectMetadataEClass = createEClass(OBJECT_METADATA);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__UPLOAD_USER);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__UPLOAD_TIME);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__SOURCE_CHANNEL);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__CONTENT_HASH);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__OBJECT_TYPE);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__REVIEW_USER);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__REVIEW_TIME);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__REVIEW_REASON);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__GENERATION_TRIGGER_FINGERPRINT);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__COMPLIANCE_CHECK_TIME);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__COMPLIANCE_STATUS);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__GOVERNANCE_DOCUMENTATION_ID);
		createEReference(objectMetadataEClass, OBJECT_METADATA__PROPERTIES);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__LAST_CHANGE_USER);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__LAST_CHANGE_TIME);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__STATUS);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__VERSION);
		createEReference(objectMetadataEClass, OBJECT_METADATA__OBJECT_REF);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__OBJECT_ID);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__OBJECT_NAME);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__STAGE);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__LAST_CHANGE_REASON);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__SCOPE);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__IS_READ_ONLY);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__REGISTRY);
		createEAttribute(objectMetadataEClass, OBJECT_METADATA__FINGERPRINT);
		createEReference(objectMetadataEClass, OBJECT_METADATA__DIAGNOSTICS);

		diagnosticEClass = createEClass(DIAGNOSTIC);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__ID);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__PRODUCER);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__SOURCE);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__CODE);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__SEVERITY);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__MESSAGE);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__CATEGORY);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__TARGET);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__STATUS);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__CREATED_TIME);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__LAST_CHANGE_TIME);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__VERSION);
		createEReference(diagnosticEClass, DIAGNOSTIC__CHILDREN);
		createEReference(diagnosticEClass, DIAGNOSTIC__HISTORY);

		diagnosticChangeEClass = createEClass(DIAGNOSTIC_CHANGE);
		createEAttribute(diagnosticChangeEClass, DIAGNOSTIC_CHANGE__CHANGE_TIME);
		createEAttribute(diagnosticChangeEClass, DIAGNOSTIC_CHANGE__CHANGED_BY);
		createEAttribute(diagnosticChangeEClass, DIAGNOSTIC_CHANGE__OLD_SEVERITY);
		createEAttribute(diagnosticChangeEClass, DIAGNOSTIC_CHANGE__NEW_SEVERITY);
		createEAttribute(diagnosticChangeEClass, DIAGNOSTIC_CHANGE__OLD_STATUS);
		createEAttribute(diagnosticChangeEClass, DIAGNOSTIC_CHANGE__NEW_STATUS);
		createEAttribute(diagnosticChangeEClass, DIAGNOSTIC_CHANGE__REASON);

		stringToObjectMapEntryEClass = createEClass(STRING_TO_OBJECT_MAP_ENTRY);
		createEAttribute(stringToObjectMapEntryEClass, STRING_TO_OBJECT_MAP_ENTRY__KEY);
		createEAttribute(stringToObjectMapEntryEClass, STRING_TO_OBJECT_MAP_ENTRY__VALUE);

		objectQueryEClass = createEClass(OBJECT_QUERY);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__UPLOAD_USER);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__SOURCE_CHANNEL);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__OBJECT_TYPE);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__STATUS);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__STAGE);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__SCOPE);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__NAME);
		createEAttribute(objectQueryEClass, OBJECT_QUERY__REGISTRY);

		generationRequestEClass = createEClass(GENERATION_REQUEST);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__REQUEST_ID);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__JSON_SAMPLE);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__JSON_FINGERPRINT);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__SOURCE_CHANNEL);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__REQUESTING_USER);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__STATUS);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__REQUEST_TIME);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__START_TIME);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__COMPLETION_TIME);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__ERROR_MESSAGE);
		createEAttribute(generationRequestEClass, GENERATION_REQUEST__RESULT_PACKAGE_ID);

		objectMetadataContainerEClass = createEClass(OBJECT_METADATA_CONTAINER);
		createEAttribute(objectMetadataContainerEClass, OBJECT_METADATA_CONTAINER__CONTAINER_ID);
		createEReference(objectMetadataContainerEClass, OBJECT_METADATA_CONTAINER__METADATA);

		// Create enums
		objectStatusEEnum = createEEnum(OBJECT_STATUS);
		packageStatusEEnum = createEEnum(PACKAGE_STATUS);
		storageBackendTypeEEnum = createEEnum(STORAGE_BACKEND_TYPE);
		generationStatusEEnum = createEEnum(GENERATION_STATUS);
		diagnosticSeverityEEnum = createEEnum(DIAGNOSTIC_SEVERITY);
		diagnosticStatusEEnum = createEEnum(DIAGNOSTIC_STATUS);

		// Create data types
		instantEDataType = createEDataType(INSTANT);
		promiseEDataType = createEDataType(PROMISE);
		listEDataType = createEDataType(LIST);
		voidEDataType = createEDataType(VOID);
		optionalEDataType = createEDataType(OPTIONAL);
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
		addETypeParameter(promiseEDataType, "T");
		addETypeParameter(listEDataType, "T");
		addETypeParameter(optionalEDataType, "T");

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(objectMetadataEClass, ObjectMetadata.class, "ObjectMetadata", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getObjectMetadata_UploadUser(), ecorePackage.getEString(), "uploadUser", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_UploadTime(), this.getInstant(), "uploadTime", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_SourceChannel(), ecorePackage.getEString(), "sourceChannel", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ContentHash(), ecorePackage.getEString(), "contentHash", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ObjectType(), ecorePackage.getEString(), "objectType", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ReviewUser(), ecorePackage.getEString(), "reviewUser", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ReviewTime(), this.getInstant(), "reviewTime", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ReviewReason(), ecorePackage.getEString(), "reviewReason", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_GenerationTriggerFingerprint(), ecorePackage.getEString(), "generationTriggerFingerprint", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ComplianceCheckTime(), this.getInstant(), "complianceCheckTime", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ComplianceStatus(), ecorePackage.getEString(), "complianceStatus", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_GovernanceDocumentationId(), ecorePackage.getEString(), "governanceDocumentationId", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getObjectMetadata_Properties(), this.getStringToObjectMapEntry(), null, "properties", null, 0, -1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_LastChangeUser(), ecorePackage.getEString(), "lastChangeUser", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_LastChangeTime(), this.getInstant(), "lastChangeTime", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_Status(), this.getObjectStatus(), "status", "DRAFT", 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_Version(), ecorePackage.getEString(), "version", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getObjectMetadata_ObjectRef(), ecorePackage.getEObject(), null, "objectRef", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ObjectId(), ecorePackage.getEString(), "objectId", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_ObjectName(), ecorePackage.getEString(), "objectName", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_Stage(), ecorePackage.getEString(), "stage", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_LastChangeReason(), ecorePackage.getEString(), "lastChangeReason", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_Scope(), ecorePackage.getEString(), "scope", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_IsReadOnly(), ecorePackage.getEBoolean(), "isReadOnly", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_Registry(), ecorePackage.getEString(), "registry", null, 1, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectMetadata_Fingerprint(), ecorePackage.getEString(), "fingerprint", null, 0, 1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getObjectMetadata_Diagnostics(), this.getDiagnostic(), null, "diagnostics", null, 0, -1, ObjectMetadata.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getObjectMetadata_Diagnostics().getEKeys().add(this.getDiagnostic_Id());

		initEClass(diagnosticEClass, Diagnostic.class, "Diagnostic", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDiagnostic_Id(), ecorePackage.getEString(), "id", null, 1, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Producer(), ecorePackage.getEString(), "producer", null, 1, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Source(), ecorePackage.getEString(), "source", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Code(), ecorePackage.getEString(), "code", null, 1, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Severity(), this.getDiagnosticSeverity(), "severity", null, 1, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Message(), ecorePackage.getEString(), "message", null, 1, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Category(), ecorePackage.getEString(), "category", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Target(), ecorePackage.getEString(), "target", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Status(), this.getDiagnosticStatus(), "status", "OPEN", 1, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_CreatedTime(), this.getInstant(), "createdTime", null, 1, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_LastChangeTime(), this.getInstant(), "lastChangeTime", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Version(), ecorePackage.getELong(), "version", "0", 1, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDiagnostic_Children(), this.getDiagnostic(), null, "children", null, 0, -1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getDiagnostic_Children().getEKeys().add(this.getDiagnostic_Id());
		initEReference(getDiagnostic_History(), this.getDiagnosticChange(), null, "history", null, 0, -1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(diagnosticChangeEClass, DiagnosticChange.class, "DiagnosticChange", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDiagnosticChange_ChangeTime(), this.getInstant(), "changeTime", null, 1, 1, DiagnosticChange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnosticChange_ChangedBy(), ecorePackage.getEString(), "changedBy", null, 1, 1, DiagnosticChange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnosticChange_OldSeverity(), this.getDiagnosticSeverity(), "oldSeverity", null, 0, 1, DiagnosticChange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnosticChange_NewSeverity(), this.getDiagnosticSeverity(), "newSeverity", null, 0, 1, DiagnosticChange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnosticChange_OldStatus(), this.getDiagnosticStatus(), "oldStatus", null, 0, 1, DiagnosticChange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnosticChange_NewStatus(), this.getDiagnosticStatus(), "newStatus", null, 0, 1, DiagnosticChange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnosticChange_Reason(), ecorePackage.getEString(), "reason", null, 0, 1, DiagnosticChange.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(stringToObjectMapEntryEClass, Map.Entry.class, "StringToObjectMapEntry", !IS_ABSTRACT, !IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getStringToObjectMapEntry_Key(), ecorePackage.getEString(), "key", null, 1, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getStringToObjectMapEntry_Value(), ecorePackage.getEJavaObject(), "value", null, 1, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(objectQueryEClass, ObjectQuery.class, "ObjectQuery", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getObjectQuery_UploadUser(), ecorePackage.getEString(), "uploadUser", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectQuery_SourceChannel(), ecorePackage.getEString(), "sourceChannel", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectQuery_ObjectType(), ecorePackage.getEString(), "objectType", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectQuery_Status(), this.getObjectStatus(), "status", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectQuery_Stage(), ecorePackage.getEString(), "stage", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectQuery_Scope(), ecorePackage.getEString(), "scope", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectQuery_Name(), ecorePackage.getEString(), "name", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getObjectQuery_Registry(), ecorePackage.getEString(), "registry", null, 0, 1, ObjectQuery.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(generationRequestEClass, GenerationRequest.class, "GenerationRequest", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGenerationRequest_RequestId(), ecorePackage.getEString(), "requestId", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_JsonSample(), ecorePackage.getEString(), "jsonSample", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_JsonFingerprint(), ecorePackage.getEString(), "jsonFingerprint", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_SourceChannel(), ecorePackage.getEString(), "sourceChannel", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_RequestingUser(), ecorePackage.getEString(), "requestingUser", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_Status(), this.getGenerationStatus(), "status", "REQUESTED", 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_RequestTime(), this.getInstant(), "requestTime", null, 1, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_StartTime(), this.getInstant(), "startTime", null, 0, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_CompletionTime(), this.getInstant(), "completionTime", null, 0, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_ErrorMessage(), ecorePackage.getEString(), "errorMessage", null, 0, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGenerationRequest_ResultPackageId(), ecorePackage.getEString(), "resultPackageId", null, 0, 1, GenerationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(objectMetadataContainerEClass, ObjectMetadataContainer.class, "ObjectMetadataContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getObjectMetadataContainer_ContainerId(), ecorePackage.getEString(), "containerId", null, 1, 1, ObjectMetadataContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getObjectMetadataContainer_Metadata(), this.getObjectMetadata(), null, "metadata", null, 0, -1, ObjectMetadataContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getObjectMetadataContainer_Metadata().getEKeys().add(this.getObjectMetadata_ObjectId());

		// Initialize enums and add enum literals
		initEEnum(objectStatusEEnum, ObjectStatus.class, "ObjectStatus");
		addEEnumLiteral(objectStatusEEnum, ObjectStatus.DRAFT);
		addEEnumLiteral(objectStatusEEnum, ObjectStatus.APPROVED);
		addEEnumLiteral(objectStatusEEnum, ObjectStatus.REJECTED);
		addEEnumLiteral(objectStatusEEnum, ObjectStatus.DEPLOYED);
		addEEnumLiteral(objectStatusEEnum, ObjectStatus.ARCHIVED);

		initEEnum(packageStatusEEnum, PackageStatus.class, "PackageStatus");
		addEEnumLiteral(packageStatusEEnum, PackageStatus.DRAFT);
		addEEnumLiteral(packageStatusEEnum, PackageStatus.APPROVED);
		addEEnumLiteral(packageStatusEEnum, PackageStatus.DEPLOYED);
		addEEnumLiteral(packageStatusEEnum, PackageStatus.ARCHIVED);

		initEEnum(storageBackendTypeEEnum, StorageBackendType.class, "StorageBackendType");
		addEEnumLiteral(storageBackendTypeEEnum, StorageBackendType.FILE);
		addEEnumLiteral(storageBackendTypeEEnum, StorageBackendType.MINIO);
		addEEnumLiteral(storageBackendTypeEEnum, StorageBackendType.GIT);
		addEEnumLiteral(storageBackendTypeEEnum, StorageBackendType.APICURIO);

		initEEnum(generationStatusEEnum, GenerationStatus.class, "GenerationStatus");
		addEEnumLiteral(generationStatusEEnum, GenerationStatus.REQUESTED);
		addEEnumLiteral(generationStatusEEnum, GenerationStatus.IN_PROGRESS);
		addEEnumLiteral(generationStatusEEnum, GenerationStatus.COMPLETED);
		addEEnumLiteral(generationStatusEEnum, GenerationStatus.FAILED);
		addEEnumLiteral(generationStatusEEnum, GenerationStatus.CANCELLED);

		initEEnum(diagnosticSeverityEEnum, DiagnosticSeverity.class, "DiagnosticSeverity");
		addEEnumLiteral(diagnosticSeverityEEnum, DiagnosticSeverity.INFO);
		addEEnumLiteral(diagnosticSeverityEEnum, DiagnosticSeverity.WARNING);
		addEEnumLiteral(diagnosticSeverityEEnum, DiagnosticSeverity.ERROR);

		initEEnum(diagnosticStatusEEnum, DiagnosticStatus.class, "DiagnosticStatus");
		addEEnumLiteral(diagnosticStatusEEnum, DiagnosticStatus.OPEN);
		addEEnumLiteral(diagnosticStatusEEnum, DiagnosticStatus.ACKNOWLEDGED);
		addEEnumLiteral(diagnosticStatusEEnum, DiagnosticStatus.RESOLVED);

		// Initialize data types
		initEDataType(instantEDataType, Instant.class, "Instant", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(promiseEDataType, Promise.class, "Promise", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(listEDataType, List.class, "List", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(voidEDataType, Void.class, "Void", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(optionalEDataType, Optional.class, "Optional", !IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
		// java.time.Instant
		createJavaAnnotations();
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
			   "modelName", "Management",
			   "complianceLevel", "17.0",
			   "copyrightText", "Copyright (c) 2012 - 2025 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n     Data In Motion - initial API and implementation",
			   "resource", "XMI",
			   "oSGiCompatible", "true",
			   "prefix", "Management",
			   "basePackage", "org.eclipse.fennec.model.atlas.mgmt"
		   });
		addAnnotation
		  (objectMetadataEClass,
		   source,
		   new String[] {
			   "documentation", "Generic metadata associated with uploaded EObject"
		   });
		addAnnotation
		  (getObjectMetadata_UploadUser(),
		   source,
		   new String[] {
			   "documentation", "User who uploaded the object"
		   });
		addAnnotation
		  (getObjectMetadata_UploadTime(),
		   source,
		   new String[] {
			   "documentation", "Timestamp when object was uploaded"
		   });
		addAnnotation
		  (getObjectMetadata_SourceChannel(),
		   source,
		   new String[] {
			   "documentation", "Source channel (e.g., AI_GENERATOR, MANUAL_UPLOAD, SENSINACT)"
		   });
		addAnnotation
		  (getObjectMetadata_ContentHash(),
		   source,
		   new String[] {
			   "documentation", "SHA-256 hash of XMI content"
		   });
		addAnnotation
		  (getObjectMetadata_ObjectType(),
		   source,
		   new String[] {
			   "documentation", "Type of the EObject (e.g., EPackage, Route, SensorModel)"
		   });
		addAnnotation
		  (getObjectMetadata_ReviewUser(),
		   source,
		   new String[] {
			   "documentation", "User who reviewed the object (for approve/reject)"
		   });
		addAnnotation
		  (getObjectMetadata_ReviewTime(),
		   source,
		   new String[] {
			   "documentation", "Timestamp when object was reviewed"
		   });
		addAnnotation
		  (getObjectMetadata_ReviewReason(),
		   source,
		   new String[] {
			   "documentation", "Reason for approval or rejection"
		   });
		addAnnotation
		  (getObjectMetadata_GenerationTriggerFingerprint(),
		   source,
		   new String[] {
			   "documentation", "This is the fingerprint of the json which triggered the generation of this Object"
		   });
		addAnnotation
		  (getObjectMetadata_ComplianceCheckTime(),
		   source,
		   new String[] {
			   "documentation", "Timestamp when compliance checks were last performed"
		   });
		addAnnotation
		  (getObjectMetadata_ComplianceStatus(),
		   source,
		   new String[] {
			   "documentation", "Overall compliance check result (uses governance.ComplianceStatus values as strings)"
		   });
		addAnnotation
		  (getObjectMetadata_GovernanceDocumentationId(),
		   source,
		   new String[] {
			   "documentation", "Reference to the governance documentation containing detailed compliance results"
		   });
		addAnnotation
		  (getObjectMetadata_Properties(),
		   source,
		   new String[] {
			   "documentation", "Additional metadata properties"
		   });
		addAnnotation
		  (getObjectMetadata_LastChangeUser(),
		   source,
		   new String[] {
			   "documentation", "User who last modified the object (only set when object is updated after initial upload)"
		   });
		addAnnotation
		  (getObjectMetadata_LastChangeTime(),
		   source,
		   new String[] {
			   "documentation", "Timestamp when object was last modified (only set when object is updated after initial upload)"
		   });
		addAnnotation
		  (getObjectMetadata_Status(),
		   source,
		   new String[] {
			   "documentation", "Object lifecycle status"
		   });
		addAnnotation
		  (getObjectMetadata_Version(),
		   source,
		   new String[] {
			   "documentation", "Object version for optimistic locking"
		   });
		addAnnotation
		  (getObjectMetadata_ObjectRef(),
		   source,
		   new String[] {
			   "documentation", "Reference to the actual managed EObject"
		   });
		addAnnotation
		  (getObjectMetadata_ObjectId(),
		   source,
		   new String[] {
			   "documentation", "Unique metadata identifier (distinct from storage objectId key) for cross-referencing and registry lookups"
		   });
		addAnnotation
		  (getObjectMetadata_ObjectName(),
		   source,
		   new String[] {
			   "documentation", "Human-readable object name"
		   });
		addAnnotation
		  (getObjectMetadata_Stage(),
		   source,
		   new String[] {
			   "documentation", "The stage to which this metadata belongs (e.g. draft, approved, released, etc.)"
		   });
		addAnnotation
		  (getObjectMetadata_LastChangeReason(),
		   source,
		   new String[] {
			   "documentation", "Reason for approval or rejection"
		   });
		addAnnotation
		  (getObjectMetadata_Scope(),
		   source,
		   new String[] {
			   "documentation", "The scope to which this metadata belongs (it\'s the tenant that own the object)"
		   });
		addAnnotation
		  (getObjectMetadata_IsReadOnly(),
		   source,
		   new String[] {
			   "documentation", "This is to be set when an EObject is retrieved from a parent scope in its final stage and then cannot be modified."
		   });
		addAnnotation
		  (getObjectMetadata_Registry(),
		   source,
		   new String[] {
			   "documentation", "The registry to which this metadata belongs (e.g. schema, configuration, script, etc.)"
		   });
		addAnnotation
		  (getObjectMetadata_Fingerprint(),
		   source,
		   new String[] {
			   "documentation", "Content-derived model fingerprint (`emf.fingerprint` format, scheme-prefixed \ne.g. `fp1:<digest>`). Set for EPackage objects; computed server-side, never \ntaken from the client.\nDistinct from contentHash, which hashes the stored XMI bytes."
		   });
		addAnnotation
		  (getObjectMetadata_Diagnostics(),
		   source,
		   new String[] {
			   "documentation", "Findings about this object, produced by stage actions, stage gates and other\nmodules (issue #290). Diagnostics are metadata, not content: writing them changes\nneither contentHash nor version, fires no stage action, and is allowed in stages\nthat are otherwise read-only, because a refused transition records its veto on\nthe object in its source stage.\n\nEach entry is a root of a diagnostic tree owned by one producer. A producer\nreplaces only its own roots and never touches those of another producer. Keyed\nby id, which is stable across re-validation (see Diagnostic)."
		   });
		addAnnotation
		  (diagnosticEClass,
		   source,
		   new String[] {
			   "documentation", "One finding about an object, modelled on org.eclipse.emf.common.util.Diagnostic:\na severity, a message, a source and code that identify the kind of finding, and\nchildren that refine it. Unlike the EMF type it is persisted with the object\'s\nmetadata and lives on: it has a stable identity, a status that is decided\nseparately from its severity, a history of the changes made to it, and a version\nfor optimistic locking (issue #291).\n\nStable id rule: the id is deterministic, derived from producer, code and target\n(for a child: from the parent\'s id, code and target). The same finding about the\nsame element therefore gets the same id on every validation run, so references\nfrom outside - a GDPR report item, a UI bookmark - survive re-validation without a\nmatching step, and a producer\'s replace operation is a plain replace by id. Two\nfindings of the same kind about the same element are one finding; a producer\nthat needs to tell them apart puts the distinction into the target or the code.\nThe API that mints ids is part of the diagnostic service (#293); the rule is\nfixed here so every producer arrives at the same id."
		   });
		addAnnotation
		  (getDiagnostic_Id(),
		   source,
		   new String[] {
			   "documentation", "Unique and stable identifier of this diagnostic, derived from producer, code and\ntarget (see the class documentation). Unique within the object\'s metadata; external\nartifacts address a diagnostic by (scope, registry, stage, objectId, id)."
		   });
		addAnnotation
		  (getDiagnostic_Producer(),
		   source,
		   new String[] {
			   "documentation", "The action, gate or module that created this diagnostic, for example\n`QvtTransitionGate` or `gdpr.review`. Ownership: a producer\'s replace operation\nreplaces exactly the roots carrying its own producer value. Set on roots; children\ninherit their parent\'s producer and carry the same value."
		   });
		addAnnotation
		  (getDiagnostic_Source(),
		   source,
		   new String[] {
			   "documentation", "Identifies the origin of the finding within the producer, in the sense of\norg.eclipse.emf.common.util.Diagnostic#getSource(): typically the validator, rule\nset or compiler that raised it, for example `org.eclipse.fennec.m2x.qvto`. Optional;\na producer with a single source may leave it unset."
		   });
		addAnnotation
		  (getDiagnostic_Code(),
		   source,
		   new String[] {
			   "documentation", "Producer-defined identifier of the kind of finding, stable across releases of the\nproducer, for example `unresolved-import` or `personal-data-without-purpose`. Part\nof the id; a client may switch on it, the message is for people."
		   });
		addAnnotation
		  (getDiagnostic_Severity(),
		   source,
		   new String[] {
			   "documentation", "How serious the finding is. Independent of status: a resolved error is still\nrecognisable as a former error. A module may raise or lower the severity later\n(#293); every such change is recorded in the history."
		   });
		addAnnotation
		  (getDiagnostic_Message(),
		   source,
		   new String[] {
			   "documentation", "Human-readable description of the finding, written for whoever looks at the\nobject. It travels to clients unchanged, so it carries no internal detail such as\nstorage paths."
		   });
		addAnnotation
		  (getDiagnostic_Category(),
		   source,
		   new String[] {
			   "documentation", "Coarse classification a UI can filter and group by, across producers, for\nexample `compile`, `reference`, `compliance`. Optional. Not part of the id, so a\nproducer may recategorise without minting new ids."
		   });
		addAnnotation
		  (getDiagnostic_Target(),
		   source,
		   new String[] {
			   "documentation", "The affected element inside the object, as a URI or URI fragment (for example\n`//Person/birthDate` for an EStructuralFeature, or `line:12:col:4` for a text\nposition a producer defines). Optional; unset means the finding is about the object\nas a whole. Part of the id, so the same finding about two elements yields two\ndiagnostics."
		   });
		addAnnotation
		  (getDiagnostic_Status(),
		   source,
		   new String[] {
			   "documentation", "Where the finding stands in its life: OPEN when produced, ACKNOWLEDGED when\nsomeone has seen it and accepts it for now, RESOLVED when it no longer applies\nor was dealt with. Kept separate from severity on purpose. A status set by a\nperson is not silently reverted by an automatic re-validation (#293); the history\ntells who set it."
		   });
		addAnnotation
		  (getDiagnostic_CreatedTime(),
		   source,
		   new String[] {
			   "documentation", "When the diagnostic was first produced. A re-validation that yields the same id\nkeeps this value; the history records that it was seen again."
		   });
		addAnnotation
		  (getDiagnostic_LastChangeTime(),
		   source,
		   new String[] {
			   "documentation", "When severity, status or message last changed; equals the time of the newest\nhistory entry. Unset while the diagnostic is untouched since creation."
		   });
		addAnnotation
		  (getDiagnostic_Version(),
		   source,
		   new String[] {
			   "documentation", "Optimistic-locking counter, incremented on every change to this diagnostic\n(#293). A change that names an older version is refused as a conflict. Starts at 0."
		   });
		addAnnotation
		  (getDiagnostic_Children(),
		   source,
		   new String[] {
			   "documentation", "Findings that refine this one, as in an EMF diagnostic tree: a compile failure\nwith one child per error, a compliance finding with one child per affected\nfeature. Children share the parent\'s producer; their ids derive from the parent\'s\nid."
		   });
		addAnnotation
		  (getDiagnostic_History(),
		   source,
		   new String[] {
			   "documentation", "Every change made to this diagnostic after it was produced, oldest first: who,\nwhen, what changed and why. Appended by the diagnostic service (#293), never\nedited."
		   });
		addAnnotation
		  (diagnosticChangeEClass,
		   source,
		   new String[] {
			   "documentation", "One entry in a Diagnostic\'s history: a change of severity or status, or a\nre-validation that confirmed the finding, with who made it, when and why. Old and\nnew values are recorded per field so a reader can follow the diagnostic\'s life\nwithout diffing; an unchanged field carries the same value on both sides."
		   });
		addAnnotation
		  (getDiagnosticChange_ChangeTime(),
		   source,
		   new String[] {
			   "documentation", "When the change was made"
		   });
		addAnnotation
		  (getDiagnosticChange_ChangedBy(),
		   source,
		   new String[] {
			   "documentation", "Who made the change: a user name for a manual decision (acknowledge, resolve),\nthe producer or module name for an automatic one (re-validation, escalation).\nWhether a change was manual matters, because a manual status is protected\nagainst automatic reverts (#293)."
		   });
		addAnnotation
		  (getDiagnosticChange_OldSeverity(),
		   source,
		   new String[] {
			   "documentation", "Severity before the change"
		   });
		addAnnotation
		  (getDiagnosticChange_NewSeverity(),
		   source,
		   new String[] {
			   "documentation", "Severity after the change"
		   });
		addAnnotation
		  (getDiagnosticChange_OldStatus(),
		   source,
		   new String[] {
			   "documentation", "Status before the change"
		   });
		addAnnotation
		  (getDiagnosticChange_NewStatus(),
		   source,
		   new String[] {
			   "documentation", "Status after the change"
		   });
		addAnnotation
		  (getDiagnosticChange_Reason(),
		   source,
		   new String[] {
			   "documentation", "Why the change was made, in the words of whoever made it: the GDPR officer\'s\nnote on a resolution, or the producer\'s explanation of an escalation. Optional."
		   });
		addAnnotation
		  (diagnosticSeverityEEnum,
		   source,
		   new String[] {
			   "documentation", "How serious a finding is, in the order of org.eclipse.emf.common.util.Diagnostic.\nThere is no OK literal: a diagnostic exists because something was found; an object\nwithout findings has no diagnostics."
		   });
		addAnnotation
		  (diagnosticSeverityEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Worth knowing, nothing to do"
		   });
		addAnnotation
		  (diagnosticSeverityEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "Something to look at; the object remains usable"
		   });
		addAnnotation
		  (diagnosticSeverityEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "The object does not hold up as it stands; a gate may refuse an operation on it"
		   });
		addAnnotation
		  (diagnosticStatusEEnum,
		   source,
		   new String[] {
			   "documentation", "Where a finding stands in its life. Deliberately separate from severity: a\nresolved warning is still a former warning."
		   });
		addAnnotation
		  (diagnosticStatusEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Produced and not yet dealt with; the default"
		   });
		addAnnotation
		  (diagnosticStatusEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "Seen and accepted for now by a person; still applies"
		   });
		addAnnotation
		  (diagnosticStatusEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "No longer applies, or was dealt with; kept for its history"
		   });
		addAnnotation
		  (stringToObjectMapEntryEClass,
		   source,
		   new String[] {
			   "documentation", "String Key - Object value map entry"
		   });
		addAnnotation
		  (getStringToObjectMapEntry_Key(),
		   source,
		   new String[] {
			   "documentation", "Property key"
		   });
		addAnnotation
		  (getStringToObjectMapEntry_Value(),
		   source,
		   new String[] {
			   "documentation", "Property value"
		   });
		addAnnotation
		  (objectQueryEClass,
		   source,
		   new String[] {
			   "documentation", "Generic query criteria for searching objects"
		   });
		addAnnotation
		  (getObjectQuery_UploadUser(),
		   source,
		   new String[] {
			   "documentation", "Filter by upload user"
		   });
		addAnnotation
		  (getObjectQuery_SourceChannel(),
		   source,
		   new String[] {
			   "documentation", "Filter by source channel"
		   });
		addAnnotation
		  (getObjectQuery_ObjectType(),
		   source,
		   new String[] {
			   "documentation", "Filter by object type"
		   });
		addAnnotation
		  (getObjectQuery_Status(),
		   source,
		   new String[] {
			   "documentation", "Filter by object status"
		   });
		addAnnotation
		  (getObjectQuery_Stage(),
		   source,
		   new String[] {
			   "documentation", "Filter by object role"
		   });
		addAnnotation
		  (getObjectQuery_Scope(),
		   source,
		   new String[] {
			   "documentation", "Filter by object scope"
		   });
		addAnnotation
		  (getObjectQuery_Name(),
		   source,
		   new String[] {
			   "documentation", "Filter by object name"
		   });
		addAnnotation
		  (getObjectQuery_Registry(),
		   source,
		   new String[] {
			   "documentation", "Filter by object scope"
		   });
		addAnnotation
		  (generationRequestEClass,
		   source,
		   new String[] {
			   "documentation", "Request for AI-based EPackage generation"
		   });
		addAnnotation
		  (getGenerationRequest_RequestId(),
		   source,
		   new String[] {
			   "documentation", "Unique request identifier"
		   });
		addAnnotation
		  (getGenerationRequest_JsonSample(),
		   source,
		   new String[] {
			   "documentation", "JSON sample used for generation"
		   });
		addAnnotation
		  (getGenerationRequest_JsonFingerprint(),
		   source,
		   new String[] {
			   "documentation", "Structural fingerprint of JSON sample"
		   });
		addAnnotation
		  (getGenerationRequest_SourceChannel(),
		   source,
		   new String[] {
			   "documentation", "Source channel that triggered generation"
		   });
		addAnnotation
		  (getGenerationRequest_RequestingUser(),
		   source,
		   new String[] {
			   "documentation", "User who requested generation"
		   });
		addAnnotation
		  (getGenerationRequest_Status(),
		   source,
		   new String[] {
			   "documentation", "Current generation status"
		   });
		addAnnotation
		  (getGenerationRequest_RequestTime(),
		   source,
		   new String[] {
			   "documentation", "When generation was requested"
		   });
		addAnnotation
		  (getGenerationRequest_StartTime(),
		   source,
		   new String[] {
			   "documentation", "When generation started"
		   });
		addAnnotation
		  (getGenerationRequest_CompletionTime(),
		   source,
		   new String[] {
			   "documentation", "When generation completed"
		   });
		addAnnotation
		  (getGenerationRequest_ErrorMessage(),
		   source,
		   new String[] {
			   "documentation", "Error message if generation failed"
		   });
		addAnnotation
		  (getGenerationRequest_ResultPackageId(),
		   source,
		   new String[] {
			   "documentation", "Package ID of generated EPackage"
		   });
		addAnnotation
		  (objectStatusEEnum,
		   source,
		   new String[] {
			   "documentation", "Generic object lifecycle status"
		   });
		addAnnotation
		  (objectStatusEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Object is in draft state"
		   });
		addAnnotation
		  (objectStatusEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "Object has been approved for release"
		   });
		addAnnotation
		  (objectStatusEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Object has been rejected during review"
		   });
		addAnnotation
		  (objectStatusEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Object has been deployed to production"
		   });
		addAnnotation
		  (objectStatusEEnum.getELiterals().get(4),
		   source,
		   new String[] {
			   "documentation", "Object has been archived"
		   });
		addAnnotation
		  (packageStatusEEnum,
		   source,
		   new String[] {
			   "documentation", "Package lifecycle status"
		   });
		addAnnotation
		  (packageStatusEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Package is in draft state"
		   });
		addAnnotation
		  (packageStatusEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "Package has been approved"
		   });
		addAnnotation
		  (packageStatusEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Package has been deployed"
		   });
		addAnnotation
		  (packageStatusEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Package has been archived"
		   });
		addAnnotation
		  (storageBackendTypeEEnum,
		   source,
		   new String[] {
			   "documentation", "Storage backend types"
		   });
		addAnnotation
		  (storageBackendTypeEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "File object storage backend"
		   });
		addAnnotation
		  (storageBackendTypeEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "MinIO object storage backend"
		   });
		addAnnotation
		  (storageBackendTypeEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Git-based storage backend"
		   });
		addAnnotation
		  (storageBackendTypeEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Apicurio-based storage backend"
		   });
		addAnnotation
		  (generationStatusEEnum,
		   source,
		   new String[] {
			   "documentation", "Status of EPackage generation request"
		   });
		addAnnotation
		  (generationStatusEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Generation has been requested"
		   });
		addAnnotation
		  (generationStatusEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "AI is actively generating the model"
		   });
		addAnnotation
		  (generationStatusEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Generation completed successfully"
		   });
		addAnnotation
		  (generationStatusEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Generation failed with error"
		   });
		addAnnotation
		  (generationStatusEEnum.getELiterals().get(4),
		   source,
		   new String[] {
			   "documentation", "Generation was cancelled"
		   });
		addAnnotation
		  (instantEDataType,
		   source,
		   new String[] {
			   "documentation", "Java 8 Instant for precise timestamps"
		   });
		addAnnotation
		  (objectMetadataContainerEClass,
		   source,
		   new String[] {
			   "documentation", "Container for managing collections of ObjectMetadata with efficient keyed access. Note: With Lucene indexing, this container may not be necessary as the index serves as the registry."
		   });
		addAnnotation
		  (getObjectMetadataContainer_ContainerId(),
		   source,
		   new String[] {
			   "documentation", "Unique container identifier"
		   });
		addAnnotation
		  (getObjectMetadataContainer_Metadata(),
		   source,
		   new String[] {
			   "documentation", "Collection of metadata objects with automatic keying by objectId for efficient lookups"
		   });
	}

	/**
	 * Initializes the annotations for <b>java.time.Instant</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createJavaAnnotations() {
		String source = "java.time.Instant";
		addAnnotation
		  (instantEDataType,
		   source,
		   new String[] {
		   });
	}

} //ManagementPackageImpl
