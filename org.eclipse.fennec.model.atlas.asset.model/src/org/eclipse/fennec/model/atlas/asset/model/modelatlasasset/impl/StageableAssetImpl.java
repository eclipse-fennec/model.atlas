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
package org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl;

import inventory.impl.AssetImpl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Stageable Asset</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.StageableAssetImpl#getScope <em>Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.StageableAssetImpl#getRegistry <em>Registry</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.StageableAssetImpl#getStage <em>Stage</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.impl.StageableAssetImpl#getAssetType <em>Asset Type</em>}</li>
 * </ul>
 *
 * @generated
 */
public class StageableAssetImpl extends AssetImpl implements StageableAsset {
	/**
	 * The default value of the '{@link #getScope() <em>Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScope()
	 * @generated
	 * @ordered
	 */
	protected static final String SCOPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getScope() <em>Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScope()
	 * @generated
	 * @ordered
	 */
	protected String scope = SCOPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getRegistry() <em>Registry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRegistry()
	 * @generated
	 * @ordered
	 */
	protected static final String REGISTRY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRegistry() <em>Registry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRegistry()
	 * @generated
	 * @ordered
	 */
	protected String registry = REGISTRY_EDEFAULT;

	/**
	 * The default value of the '{@link #getStage() <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStage()
	 * @generated
	 * @ordered
	 */
	protected static final String STAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getStage() <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStage()
	 * @generated
	 * @ordered
	 */
	protected String stage = STAGE_EDEFAULT;

	/**
	 * The default value of the '{@link #getAssetType() <em>Asset Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAssetType()
	 * @generated
	 * @ordered
	 */
	protected static final String ASSET_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAssetType() <em>Asset Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAssetType()
	 * @generated
	 * @ordered
	 */
	protected String assetType = ASSET_TYPE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected StageableAssetImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelAtlasAssetPackage.Literals.STAGEABLE_ASSET;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getScope() {
		return scope;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setScope(String newScope) {
		String oldScope = scope;
		scope = newScope;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelAtlasAssetPackage.STAGEABLE_ASSET__SCOPE, oldScope, scope));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRegistry() {
		return registry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRegistry(String newRegistry) {
		String oldRegistry = registry;
		registry = newRegistry;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelAtlasAssetPackage.STAGEABLE_ASSET__REGISTRY, oldRegistry, registry));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getStage() {
		return stage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStage(String newStage) {
		String oldStage = stage;
		stage = newStage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelAtlasAssetPackage.STAGEABLE_ASSET__STAGE, oldStage, stage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getAssetType() {
		return assetType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAssetType(String newAssetType) {
		String oldAssetType = assetType;
		assetType = newAssetType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelAtlasAssetPackage.STAGEABLE_ASSET__ASSET_TYPE, oldAssetType, assetType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__SCOPE:
				return getScope();
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__REGISTRY:
				return getRegistry();
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__STAGE:
				return getStage();
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__ASSET_TYPE:
				return getAssetType();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__SCOPE:
				setScope((String)newValue);
				return;
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__REGISTRY:
				setRegistry((String)newValue);
				return;
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__STAGE:
				setStage((String)newValue);
				return;
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__ASSET_TYPE:
				setAssetType((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__SCOPE:
				setScope(SCOPE_EDEFAULT);
				return;
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__REGISTRY:
				setRegistry(REGISTRY_EDEFAULT);
				return;
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__STAGE:
				setStage(STAGE_EDEFAULT);
				return;
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__ASSET_TYPE:
				setAssetType(ASSET_TYPE_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__SCOPE:
				return SCOPE_EDEFAULT == null ? scope != null : !SCOPE_EDEFAULT.equals(scope);
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__REGISTRY:
				return REGISTRY_EDEFAULT == null ? registry != null : !REGISTRY_EDEFAULT.equals(registry);
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__STAGE:
				return STAGE_EDEFAULT == null ? stage != null : !STAGE_EDEFAULT.equals(stage);
			case ModelAtlasAssetPackage.STAGEABLE_ASSET__ASSET_TYPE:
				return ASSET_TYPE_EDEFAULT == null ? assetType != null : !ASSET_TYPE_EDEFAULT.equals(assetType);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (scope: ");
		result.append(scope);
		result.append(", registry: ");
		result.append(registry);
		result.append(", stage: ");
		result.append(stage);
		result.append(", assetType: ");
		result.append(assetType);
		result.append(')');
		return result.toString();
	}

} //StageableAssetImpl
