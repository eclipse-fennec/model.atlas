/*
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
package org.eclipse.fennec.model.atlas.registry.api;

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage
 * @generated
 */
@ProviderType
public interface RegistryApiFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	RegistryApiFactory eINSTANCE = org.eclipse.fennec.model.atlas.registry.api.impl.RegistryApiFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Object Address</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Object Address</em>'.
	 * @generated
	 */
	ObjectAddress createObjectAddress();

	/**
	 * Returns a new object of class '<em>Package Address</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Package Address</em>'.
	 * @generated
	 */
	PackageAddress createPackageAddress();

	/**
	 * Returns a new object of class '<em>Precondition</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Precondition</em>'.
	 * @generated
	 */
	Precondition createPrecondition();

	/**
	 * Returns a new object of class '<em>Object Write</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Object Write</em>'.
	 * @generated
	 */
	ObjectWrite createObjectWrite();

	/**
	 * Returns a new object of class '<em>Object Batch</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Object Batch</em>'.
	 * @generated
	 */
	ObjectBatch createObjectBatch();

	/**
	 * Returns a new object of class '<em>Package Write</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Package Write</em>'.
	 * @generated
	 */
	PackageWrite createPackageWrite();

	/**
	 * Returns a new object of class '<em>Package Batch</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Package Batch</em>'.
	 * @generated
	 */
	PackageBatch createPackageBatch();

	/**
	 * Returns a new object of class '<em>Package Properties Update</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Package Properties Update</em>'.
	 * @generated
	 */
	PackagePropertiesUpdate createPackagePropertiesUpdate();

	/**
	 * Returns a new object of class '<em>Package Search</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Package Search</em>'.
	 * @generated
	 */
	PackageSearch createPackageSearch();

	/**
	 * Returns a new object of class '<em>Package Search Result</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Package Search Result</em>'.
	 * @generated
	 */
	PackageSearchResult createPackageSearchResult();

	/**
	 * Returns a new object of class '<em>Write Result</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Write Result</em>'.
	 * @generated
	 */
	WriteResult createWriteResult();

	/**
	 * Returns a new object of class '<em>Batch Item Result</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Batch Item Result</em>'.
	 * @generated
	 */
	BatchItemResult createBatchItemResult();

	/**
	 * Returns a new object of class '<em>Batch Result</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Batch Result</em>'.
	 * @generated
	 */
	BatchResult createBatchResult();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	RegistryApiPackage getRegistryApiPackage();

} //RegistryApiFactory
