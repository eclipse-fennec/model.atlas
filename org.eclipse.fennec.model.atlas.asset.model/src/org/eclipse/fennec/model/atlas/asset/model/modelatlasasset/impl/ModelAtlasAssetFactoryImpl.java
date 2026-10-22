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
package org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ModelAtlasAssetFactoryImpl extends EFactoryImpl implements ModelAtlasAssetFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ModelAtlasAssetFactory init() {
		try {
			ModelAtlasAssetFactory theModelAtlasAssetFactory = (ModelAtlasAssetFactory)EPackage.Registry.INSTANCE.getEFactory(ModelAtlasAssetPackage.eNS_URI);
			if (theModelAtlasAssetFactory != null) {
				return theModelAtlasAssetFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ModelAtlasAssetFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ModelAtlasAssetFactoryImpl() {
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
			case ModelAtlasAssetPackage.STAGEABLE_ASSET: return createStageableAsset();
			case ModelAtlasAssetPackage.FINGERPRINTABLE_ASSET: return createFingerprintableAsset();
			case ModelAtlasAssetPackage.SCHEMA_ASSET: return createSchemaAsset();
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
	public StageableAsset createStageableAsset() {
		StageableAssetImpl stageableAsset = new StageableAssetImpl();
		return stageableAsset;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FingerprintableAsset createFingerprintableAsset() {
		FingerprintableAssetImpl fingerprintableAsset = new FingerprintableAssetImpl();
		return fingerprintableAsset;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SchemaAsset createSchemaAsset() {
		SchemaAssetImpl schemaAsset = new SchemaAssetImpl();
		return schemaAsset;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ModelAtlasAssetPackage getModelAtlasAssetPackage() {
		return (ModelAtlasAssetPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ModelAtlasAssetPackage getPackage() {
		return ModelAtlasAssetPackage.eINSTANCE;
	}

} //ModelAtlasAssetFactoryImpl
