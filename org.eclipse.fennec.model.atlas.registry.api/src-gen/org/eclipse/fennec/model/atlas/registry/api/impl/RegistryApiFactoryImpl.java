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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.model.atlas.registry.api.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class RegistryApiFactoryImpl extends EFactoryImpl implements RegistryApiFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static RegistryApiFactory init() {
		try {
			RegistryApiFactory theRegistryApiFactory = (RegistryApiFactory)EPackage.Registry.INSTANCE.getEFactory(RegistryApiPackage.eNS_URI);
			if (theRegistryApiFactory != null) {
				return theRegistryApiFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new RegistryApiFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RegistryApiFactoryImpl() {
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
			case RegistryApiPackage.OBJECT_ADDRESS: return (EObject)createObjectAddress();
			case RegistryApiPackage.PACKAGE_ADDRESS: return (EObject)createPackageAddress();
			case RegistryApiPackage.OBJECT_WRITE: return (EObject)createObjectWrite();
			case RegistryApiPackage.PACKAGE_WRITE: return (EObject)createPackageWrite();
			case RegistryApiPackage.PACKAGE_SEARCH: return (EObject)createPackageSearch();
			case RegistryApiPackage.PACKAGE_SEARCH_RESULT: return (EObject)createPackageSearchResult();
			case RegistryApiPackage.WRITE_RESULT: return (EObject)createWriteResult();
			case RegistryApiPackage.BATCH_ITEM_RESULT: return (EObject)createBatchItemResult();
			case RegistryApiPackage.BATCH_RESULT: return (EObject)createBatchResult();
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
			case RegistryApiPackage.CONFLICT_POLICY:
				return createConflictPolicyFromString(eDataType, initialValue);
			case RegistryApiPackage.WRITE_OUTCOME:
				return createWriteOutcomeFromString(eDataType, initialValue);
			case RegistryApiPackage.REFUSAL_REASON:
				return createRefusalReasonFromString(eDataType, initialValue);
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
			case RegistryApiPackage.CONFLICT_POLICY:
				return convertConflictPolicyToString(eDataType, instanceValue);
			case RegistryApiPackage.WRITE_OUTCOME:
				return convertWriteOutcomeToString(eDataType, instanceValue);
			case RegistryApiPackage.REFUSAL_REASON:
				return convertRefusalReasonToString(eDataType, instanceValue);
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
	public ObjectAddress createObjectAddress() {
		ObjectAddressImpl objectAddress = new ObjectAddressImpl();
		return objectAddress;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PackageAddress createPackageAddress() {
		PackageAddressImpl packageAddress = new PackageAddressImpl();
		return packageAddress;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObjectWrite createObjectWrite() {
		ObjectWriteImpl objectWrite = new ObjectWriteImpl();
		return objectWrite;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PackageWrite createPackageWrite() {
		PackageWriteImpl packageWrite = new PackageWriteImpl();
		return packageWrite;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PackageSearch createPackageSearch() {
		PackageSearchImpl packageSearch = new PackageSearchImpl();
		return packageSearch;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PackageSearchResult createPackageSearchResult() {
		PackageSearchResultImpl packageSearchResult = new PackageSearchResultImpl();
		return packageSearchResult;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public WriteResult createWriteResult() {
		WriteResultImpl writeResult = new WriteResultImpl();
		return writeResult;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BatchItemResult createBatchItemResult() {
		BatchItemResultImpl batchItemResult = new BatchItemResultImpl();
		return batchItemResult;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BatchResult createBatchResult() {
		BatchResultImpl batchResult = new BatchResultImpl();
		return batchResult;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ConflictPolicy createConflictPolicyFromString(EDataType eDataType, String initialValue) {
		ConflictPolicy result = ConflictPolicy.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertConflictPolicyToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WriteOutcome createWriteOutcomeFromString(EDataType eDataType, String initialValue) {
		WriteOutcome result = WriteOutcome.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertWriteOutcomeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RefusalReason createRefusalReasonFromString(EDataType eDataType, String initialValue) {
		RefusalReason result = RefusalReason.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertRefusalReasonToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RegistryApiPackage getRegistryApiPackage() {
		return (RegistryApiPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static RegistryApiPackage getPackage() {
		return RegistryApiPackage.eINSTANCE;
	}

} //RegistryApiFactoryImpl
