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
package org.eclipse.fennec.model.atlas.mgmt.management.impl;

import java.time.Instant;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.model.atlas.mgmt.management.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ManagementFactoryImpl extends EFactoryImpl implements ManagementFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ManagementFactory init() {
		try {
			ManagementFactory theManagementFactory = (ManagementFactory)EPackage.Registry.INSTANCE.getEFactory(ManagementPackage.eNS_URI);
			if (theManagementFactory != null) {
				return theManagementFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ManagementFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ManagementFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case ManagementPackage.OBJECT_METADATA: return createObjectMetadata();
			case ManagementPackage.STRING_TO_OBJECT_MAP_ENTRY: return (EObject)createStringToObjectMapEntry();
			case ManagementPackage.OBJECT_QUERY: return createObjectQuery();
			case ManagementPackage.GENERATION_REQUEST: return createGenerationRequest();
			case ManagementPackage.OBJECT_METADATA_CONTAINER: return createObjectMetadataContainer();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case ManagementPackage.OBJECT_STATUS:
				return createObjectStatusFromString(eDataType, initialValue);
			case ManagementPackage.PACKAGE_STATUS:
				return createPackageStatusFromString(eDataType, initialValue);
			case ManagementPackage.STORAGE_BACKEND_TYPE:
				return createStorageBackendTypeFromString(eDataType, initialValue);
			case ManagementPackage.GENERATION_STATUS:
				return createGenerationStatusFromString(eDataType, initialValue);
			case ManagementPackage.INSTANT:
				return createInstantFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case ManagementPackage.OBJECT_STATUS:
				return convertObjectStatusToString(eDataType, instanceValue);
			case ManagementPackage.PACKAGE_STATUS:
				return convertPackageStatusToString(eDataType, instanceValue);
			case ManagementPackage.STORAGE_BACKEND_TYPE:
				return convertStorageBackendTypeToString(eDataType, instanceValue);
			case ManagementPackage.GENERATION_STATUS:
				return convertGenerationStatusToString(eDataType, instanceValue);
			case ManagementPackage.INSTANT:
				return convertInstantToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObjectMetadata createObjectMetadata() {
		ObjectMetadataImpl objectMetadata = new ObjectMetadataImpl();
		return objectMetadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map.Entry<String, Object> createStringToObjectMapEntry() {
		StringToObjectMapEntryImpl stringToObjectMapEntry = new StringToObjectMapEntryImpl();
		return stringToObjectMapEntry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObjectQuery createObjectQuery() {
		ObjectQueryImpl objectQuery = new ObjectQueryImpl();
		return objectQuery;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GenerationRequest createGenerationRequest() {
		GenerationRequestImpl generationRequest = new GenerationRequestImpl();
		return generationRequest;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObjectMetadataContainer createObjectMetadataContainer() {
		ObjectMetadataContainerImpl objectMetadataContainer = new ObjectMetadataContainerImpl();
		return objectMetadataContainer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ObjectStatus createObjectStatusFromString(EDataType eDataType, String initialValue) {
		ObjectStatus result = ObjectStatus.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertObjectStatusToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PackageStatus createPackageStatusFromString(EDataType eDataType, String initialValue) {
		PackageStatus result = PackageStatus.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPackageStatusToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public StorageBackendType createStorageBackendTypeFromString(EDataType eDataType, String initialValue) {
		StorageBackendType result = StorageBackendType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertStorageBackendTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GenerationStatus createGenerationStatusFromString(EDataType eDataType, String initialValue) {
		GenerationStatus result = GenerationStatus.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertGenerationStatusToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Instant createInstantFromString(EDataType eDataType, String initialValue) {
		return (Instant)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertInstantToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ManagementPackage getManagementPackage() {
		return (ManagementPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ManagementPackage getPackage() {
		return ManagementPackage.eINSTANCE;
	}

} //ManagementFactoryImpl
