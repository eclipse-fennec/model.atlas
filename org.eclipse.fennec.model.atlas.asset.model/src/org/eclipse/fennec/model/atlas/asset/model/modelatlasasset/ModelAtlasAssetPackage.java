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


import inventory.InventoryPackage;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetFactory
 * @model kind="package"
 *        annotation="Version value='1.0'"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel complianceLevel='17.0' oSGiCompatible='true' basePackage='org.eclipse.fennec.model.atlas.asset.model' resource='XMI' copyrightText='Copyright (c) 2012 - 2025 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n     Data In Motion - initial API and implementation'"
 * @generated
 */
@ProviderType
@EPackage(uri = ModelAtlasAssetPackage.eNS_URI, fingerprint = "fp1:f7f8286adc48a55181e34582be0f2b19a6cdb552558d1e0e44ff4c420ce4821e", genModel = "/model/model-atlas-asset.genmodel", genModelSourceLocations = {"model/model-atlas-asset.genmodel","org.eclipse.fennec.model.atlas.asset.model/model/model-atlas-asset.genmodel"}, ecore = "/model/model-atlas-asset.ecore", ecoreSourceLocations = "/model/model-atlas-asset.ecore")
public interface ModelAtlasAssetPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "modelatlasasset";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/asset/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "modelatlasasset";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ModelAtlasAssetPackage eINSTANCE = org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.ModelAtlasAssetPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.StageableAssetImpl <em>Stageable Asset</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.StageableAssetImpl
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.ModelAtlasAssetPackageImpl#getStageableAsset()
	 * @generated
	 */
	int STAGEABLE_ASSET = 0;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__ID = InventoryPackage.ASSET__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__NAME = InventoryPackage.ASSET__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__DESCRIPTION = InventoryPackage.ASSET__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__OWNER = InventoryPackage.ASSET__OWNER;

	/**
	 * The feature id for the '<em><b>Relations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__RELATIONS = InventoryPackage.ASSET__RELATIONS;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__SOURCES = InventoryPackage.ASSET__SOURCES;

	/**
	 * The feature id for the '<em><b>Aspects</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__ASPECTS = InventoryPackage.ASSET__ASPECTS;

	/**
	 * The feature id for the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__SCOPE = InventoryPackage.ASSET_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Registry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__REGISTRY = InventoryPackage.ASSET_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__STAGE = InventoryPackage.ASSET_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Asset Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__ASSET_TYPE = InventoryPackage.ASSET_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Object Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET__OBJECT_ID = InventoryPackage.ASSET_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>Stageable Asset</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET_FEATURE_COUNT = InventoryPackage.ASSET_FEATURE_COUNT + 5;

	/**
	 * The number of operations of the '<em>Stageable Asset</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGEABLE_ASSET_OPERATION_COUNT = InventoryPackage.ASSET_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.FingerprintableAssetImpl <em>Fingerprintable Asset</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.FingerprintableAssetImpl
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.ModelAtlasAssetPackageImpl#getFingerprintableAsset()
	 * @generated
	 */
	int FINGERPRINTABLE_ASSET = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__ID = STAGEABLE_ASSET__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__NAME = STAGEABLE_ASSET__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__DESCRIPTION = STAGEABLE_ASSET__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__OWNER = STAGEABLE_ASSET__OWNER;

	/**
	 * The feature id for the '<em><b>Relations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__RELATIONS = STAGEABLE_ASSET__RELATIONS;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__SOURCES = STAGEABLE_ASSET__SOURCES;

	/**
	 * The feature id for the '<em><b>Aspects</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__ASPECTS = STAGEABLE_ASSET__ASPECTS;

	/**
	 * The feature id for the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__SCOPE = STAGEABLE_ASSET__SCOPE;

	/**
	 * The feature id for the '<em><b>Registry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__REGISTRY = STAGEABLE_ASSET__REGISTRY;

	/**
	 * The feature id for the '<em><b>Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__STAGE = STAGEABLE_ASSET__STAGE;

	/**
	 * The feature id for the '<em><b>Asset Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__ASSET_TYPE = STAGEABLE_ASSET__ASSET_TYPE;

	/**
	 * The feature id for the '<em><b>Object Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__OBJECT_ID = STAGEABLE_ASSET__OBJECT_ID;

	/**
	 * The feature id for the '<em><b>Fingerprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET__FINGERPRINT = STAGEABLE_ASSET_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Fingerprintable Asset</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET_FEATURE_COUNT = STAGEABLE_ASSET_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Fingerprintable Asset</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FINGERPRINTABLE_ASSET_OPERATION_COUNT = STAGEABLE_ASSET_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.SchemaAssetImpl <em>Schema Asset</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.SchemaAssetImpl
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.ModelAtlasAssetPackageImpl#getSchemaAsset()
	 * @generated
	 */
	int SCHEMA_ASSET = 2;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__ID = FINGERPRINTABLE_ASSET__ID;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__NAME = FINGERPRINTABLE_ASSET__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__DESCRIPTION = FINGERPRINTABLE_ASSET__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__OWNER = FINGERPRINTABLE_ASSET__OWNER;

	/**
	 * The feature id for the '<em><b>Relations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__RELATIONS = FINGERPRINTABLE_ASSET__RELATIONS;

	/**
	 * The feature id for the '<em><b>Sources</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__SOURCES = FINGERPRINTABLE_ASSET__SOURCES;

	/**
	 * The feature id for the '<em><b>Aspects</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__ASPECTS = FINGERPRINTABLE_ASSET__ASPECTS;

	/**
	 * The feature id for the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__SCOPE = FINGERPRINTABLE_ASSET__SCOPE;

	/**
	 * The feature id for the '<em><b>Registry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__REGISTRY = FINGERPRINTABLE_ASSET__REGISTRY;

	/**
	 * The feature id for the '<em><b>Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__STAGE = FINGERPRINTABLE_ASSET__STAGE;

	/**
	 * The feature id for the '<em><b>Asset Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__ASSET_TYPE = FINGERPRINTABLE_ASSET__ASSET_TYPE;

	/**
	 * The feature id for the '<em><b>Object Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__OBJECT_ID = FINGERPRINTABLE_ASSET__OBJECT_ID;

	/**
	 * The feature id for the '<em><b>Fingerprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__FINGERPRINT = FINGERPRINTABLE_ASSET__FINGERPRINT;

	/**
	 * The feature id for the '<em><b>Ns URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__NS_URI = FINGERPRINTABLE_ASSET_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Ns Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET__NS_PREFIX = FINGERPRINTABLE_ASSET_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Schema Asset</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET_FEATURE_COUNT = FINGERPRINTABLE_ASSET_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Schema Asset</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCHEMA_ASSET_OPERATION_COUNT = FINGERPRINTABLE_ASSET_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset <em>Stageable Asset</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Stageable Asset</em>'.
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset
	 * @generated
	 */
	EClass getStageableAsset();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getScope <em>Scope</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Scope</em>'.
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getScope()
	 * @see #getStageableAsset()
	 * @generated
	 */
	EAttribute getStageableAsset_Scope();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getRegistry <em>Registry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Registry</em>'.
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getRegistry()
	 * @see #getStageableAsset()
	 * @generated
	 */
	EAttribute getStageableAsset_Registry();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getStage <em>Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stage</em>'.
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getStage()
	 * @see #getStageableAsset()
	 * @generated
	 */
	EAttribute getStageableAsset_Stage();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getAssetType <em>Asset Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Asset Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getAssetType()
	 * @see #getStageableAsset()
	 * @generated
	 */
	EAttribute getStageableAsset_AssetType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getObjectId <em>Object Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Object Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getObjectId()
	 * @see #getStageableAsset()
	 * @generated
	 */
	EAttribute getStageableAsset_ObjectId();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.FingerprintableAsset <em>Fingerprintable Asset</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Fingerprintable Asset</em>'.
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.FingerprintableAsset
	 * @generated
	 */
	EClass getFingerprintableAsset();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.FingerprintableAsset#getFingerprint <em>Fingerprint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Fingerprint</em>'.
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.FingerprintableAsset#getFingerprint()
	 * @see #getFingerprintableAsset()
	 * @generated
	 */
	EAttribute getFingerprintableAsset_Fingerprint();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.SchemaAsset <em>Schema Asset</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Schema Asset</em>'.
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.SchemaAsset
	 * @generated
	 */
	EClass getSchemaAsset();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.SchemaAsset#getNsURI <em>Ns URI</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ns URI</em>'.
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.SchemaAsset#getNsURI()
	 * @see #getSchemaAsset()
	 * @generated
	 */
	EAttribute getSchemaAsset_NsURI();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.SchemaAsset#getNsPrefix <em>Ns Prefix</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ns Prefix</em>'.
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.SchemaAsset#getNsPrefix()
	 * @see #getSchemaAsset()
	 * @generated
	 */
	EAttribute getSchemaAsset_NsPrefix();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ModelAtlasAssetFactory getModelAtlasAssetFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.StageableAssetImpl <em>Stageable Asset</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.StageableAssetImpl
		 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.ModelAtlasAssetPackageImpl#getStageableAsset()
		 * @generated
		 */
		EClass STAGEABLE_ASSET = eINSTANCE.getStageableAsset();

		/**
		 * The meta object literal for the '<em><b>Scope</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGEABLE_ASSET__SCOPE = eINSTANCE.getStageableAsset_Scope();

		/**
		 * The meta object literal for the '<em><b>Registry</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGEABLE_ASSET__REGISTRY = eINSTANCE.getStageableAsset_Registry();

		/**
		 * The meta object literal for the '<em><b>Stage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGEABLE_ASSET__STAGE = eINSTANCE.getStageableAsset_Stage();

		/**
		 * The meta object literal for the '<em><b>Asset Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGEABLE_ASSET__ASSET_TYPE = eINSTANCE.getStageableAsset_AssetType();

		/**
		 * The meta object literal for the '<em><b>Object Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGEABLE_ASSET__OBJECT_ID = eINSTANCE.getStageableAsset_ObjectId();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.FingerprintableAssetImpl <em>Fingerprintable Asset</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.FingerprintableAssetImpl
		 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.ModelAtlasAssetPackageImpl#getFingerprintableAsset()
		 * @generated
		 */
		EClass FINGERPRINTABLE_ASSET = eINSTANCE.getFingerprintableAsset();

		/**
		 * The meta object literal for the '<em><b>Fingerprint</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FINGERPRINTABLE_ASSET__FINGERPRINT = eINSTANCE.getFingerprintableAsset_Fingerprint();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.SchemaAssetImpl <em>Schema Asset</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.SchemaAssetImpl
		 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.ModelAtlasAssetPackageImpl#getSchemaAsset()
		 * @generated
		 */
		EClass SCHEMA_ASSET = eINSTANCE.getSchemaAsset();

		/**
		 * The meta object literal for the '<em><b>Ns URI</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCHEMA_ASSET__NS_URI = eINSTANCE.getSchemaAsset_NsURI();

		/**
		 * The meta object literal for the '<em><b>Ns Prefix</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SCHEMA_ASSET__NS_PREFIX = eINSTANCE.getSchemaAsset_NsPrefix();

	}

} //ModelAtlasAssetPackage
