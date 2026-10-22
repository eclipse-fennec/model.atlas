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

import inventory.Asset;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Stageable Asset</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * An Asset that has the concpets of stage, registry and scope typical of the model.atlas
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getScope <em>Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getRegistry <em>Registry</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getStage <em>Stage</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getAssetType <em>Asset Type</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage#getStageableAsset()
 * @model
 * @generated
 */
@ProviderType
public interface StageableAsset extends Asset {
	/**
	 * Returns the value of the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Scope</em>' attribute.
	 * @see #setScope(String)
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage#getStageableAsset_Scope()
	 * @model
	 * @generated
	 */
	String getScope();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getScope <em>Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scope</em>' attribute.
	 * @see #getScope()
	 * @generated
	 */
	void setScope(String value);

	/**
	 * Returns the value of the '<em><b>Registry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Registry</em>' attribute.
	 * @see #setRegistry(String)
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage#getStageableAsset_Registry()
	 * @model
	 * @generated
	 */
	String getRegistry();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getRegistry <em>Registry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Registry</em>' attribute.
	 * @see #getRegistry()
	 * @generated
	 */
	void setRegistry(String value);

	/**
	 * Returns the value of the '<em><b>Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stage</em>' attribute.
	 * @see #setStage(String)
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage#getStageableAsset_Stage()
	 * @model
	 * @generated
	 */
	String getStage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getStage <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stage</em>' attribute.
	 * @see #getStage()
	 * @generated
	 */
	void setStage(String value);

	/**
	 * Returns the value of the '<em><b>Asset Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object type of the asset, as it is saved in the corresponding ObjectMetadata#objectType
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Asset Type</em>' attribute.
	 * @see #setAssetType(String)
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage#getStageableAsset_AssetType()
	 * @model
	 * @generated
	 */
	String getAssetType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset#getAssetType <em>Asset Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Asset Type</em>' attribute.
	 * @see #getAssetType()
	 * @generated
	 */
	void setAssetType(String value);

} // StageableAsset
