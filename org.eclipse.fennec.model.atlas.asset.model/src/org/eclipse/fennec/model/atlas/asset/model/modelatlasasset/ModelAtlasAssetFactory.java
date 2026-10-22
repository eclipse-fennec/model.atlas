/*
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
package org.eclipse.fennec.model.atlas.asset.model.modelatlasasset;

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage
 * @generated
 */
@ProviderType
public interface ModelAtlasAssetFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelAtlasAssetFactory eINSTANCE = org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.ModelAtlasAssetFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Stageable Asset</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Stageable Asset</em>'.
	 * @generated
	 */
	StageableAsset createStageableAsset();

	/**
	 * Returns a new object of class '<em>Fingerprintable Asset</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Fingerprintable Asset</em>'.
	 * @generated
	 */
	FingerprintableAsset createFingerprintableAsset();

	/**
	 * Returns a new object of class '<em>Schema Asset</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Schema Asset</em>'.
	 * @generated
	 */
	SchemaAsset createSchemaAsset();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ModelAtlasAssetPackage getModelAtlasAssetPackage();

} //ModelAtlasAssetFactory
