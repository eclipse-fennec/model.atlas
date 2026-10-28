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
package inventory.impl;

import inventory.AssetRelation;
import inventory.AssetRelationKind;
import inventory.InventoryPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Asset Relation</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link inventory.impl.AssetRelationImpl#getId <em>Id</em>}</li>
 *   <li>{@link inventory.impl.AssetRelationImpl#getKind <em>Kind</em>}</li>
 *   <li>{@link inventory.impl.AssetRelationImpl#getRationale <em>Rationale</em>}</li>
 *   <li>{@link inventory.impl.AssetRelationImpl#getSourceAssetId <em>Source Asset Id</em>}</li>
 *   <li>{@link inventory.impl.AssetRelationImpl#getTargetAssetId <em>Target Asset Id</em>}</li>
 * </ul>
 *
 * @generated
 */
public class AssetRelationImpl extends MinimalEObjectImpl.Container implements AssetRelation {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected static final AssetRelationKind KIND_EDEFAULT = AssetRelationKind.DEPENDS_ON;

	/**
	 * The cached value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected AssetRelationKind kind = KIND_EDEFAULT;

	/**
	 * The default value of the '{@link #getRationale() <em>Rationale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRationale()
	 * @generated
	 * @ordered
	 */
	protected static final String RATIONALE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRationale() <em>Rationale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRationale()
	 * @generated
	 * @ordered
	 */
	protected String rationale = RATIONALE_EDEFAULT;

	/**
	 * The default value of the '{@link #getSourceAssetId() <em>Source Asset Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceAssetId()
	 * @generated
	 * @ordered
	 */
	protected static final String SOURCE_ASSET_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSourceAssetId() <em>Source Asset Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceAssetId()
	 * @generated
	 * @ordered
	 */
	protected String sourceAssetId = SOURCE_ASSET_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getTargetAssetId() <em>Target Asset Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetAssetId()
	 * @generated
	 * @ordered
	 */
	protected static final String TARGET_ASSET_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTargetAssetId() <em>Target Asset Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetAssetId()
	 * @generated
	 * @ordered
	 */
	protected String targetAssetId = TARGET_ASSET_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AssetRelationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return InventoryPackage.Literals.ASSET_RELATION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InventoryPackage.ASSET_RELATION__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AssetRelationKind getKind() {
		return kind;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKind(AssetRelationKind newKind) {
		AssetRelationKind oldKind = kind;
		kind = newKind == null ? KIND_EDEFAULT : newKind;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InventoryPackage.ASSET_RELATION__KIND, oldKind, kind));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRationale() {
		return rationale;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRationale(String newRationale) {
		String oldRationale = rationale;
		rationale = newRationale;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InventoryPackage.ASSET_RELATION__RATIONALE, oldRationale, rationale));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSourceAssetId() {
		return sourceAssetId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSourceAssetId(String newSourceAssetId) {
		String oldSourceAssetId = sourceAssetId;
		sourceAssetId = newSourceAssetId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InventoryPackage.ASSET_RELATION__SOURCE_ASSET_ID, oldSourceAssetId, sourceAssetId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTargetAssetId() {
		return targetAssetId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTargetAssetId(String newTargetAssetId) {
		String oldTargetAssetId = targetAssetId;
		targetAssetId = newTargetAssetId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, InventoryPackage.ASSET_RELATION__TARGET_ASSET_ID, oldTargetAssetId, targetAssetId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case InventoryPackage.ASSET_RELATION__ID:
				return getId();
			case InventoryPackage.ASSET_RELATION__KIND:
				return getKind();
			case InventoryPackage.ASSET_RELATION__RATIONALE:
				return getRationale();
			case InventoryPackage.ASSET_RELATION__SOURCE_ASSET_ID:
				return getSourceAssetId();
			case InventoryPackage.ASSET_RELATION__TARGET_ASSET_ID:
				return getTargetAssetId();
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
			case InventoryPackage.ASSET_RELATION__ID:
				setId((String)newValue);
				return;
			case InventoryPackage.ASSET_RELATION__KIND:
				setKind((AssetRelationKind)newValue);
				return;
			case InventoryPackage.ASSET_RELATION__RATIONALE:
				setRationale((String)newValue);
				return;
			case InventoryPackage.ASSET_RELATION__SOURCE_ASSET_ID:
				setSourceAssetId((String)newValue);
				return;
			case InventoryPackage.ASSET_RELATION__TARGET_ASSET_ID:
				setTargetAssetId((String)newValue);
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
			case InventoryPackage.ASSET_RELATION__ID:
				setId(ID_EDEFAULT);
				return;
			case InventoryPackage.ASSET_RELATION__KIND:
				setKind(KIND_EDEFAULT);
				return;
			case InventoryPackage.ASSET_RELATION__RATIONALE:
				setRationale(RATIONALE_EDEFAULT);
				return;
			case InventoryPackage.ASSET_RELATION__SOURCE_ASSET_ID:
				setSourceAssetId(SOURCE_ASSET_ID_EDEFAULT);
				return;
			case InventoryPackage.ASSET_RELATION__TARGET_ASSET_ID:
				setTargetAssetId(TARGET_ASSET_ID_EDEFAULT);
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
			case InventoryPackage.ASSET_RELATION__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case InventoryPackage.ASSET_RELATION__KIND:
				return kind != KIND_EDEFAULT;
			case InventoryPackage.ASSET_RELATION__RATIONALE:
				return RATIONALE_EDEFAULT == null ? rationale != null : !RATIONALE_EDEFAULT.equals(rationale);
			case InventoryPackage.ASSET_RELATION__SOURCE_ASSET_ID:
				return SOURCE_ASSET_ID_EDEFAULT == null ? sourceAssetId != null : !SOURCE_ASSET_ID_EDEFAULT.equals(sourceAssetId);
			case InventoryPackage.ASSET_RELATION__TARGET_ASSET_ID:
				return TARGET_ASSET_ID_EDEFAULT == null ? targetAssetId != null : !TARGET_ASSET_ID_EDEFAULT.equals(targetAssetId);
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
		result.append(" (id: ");
		result.append(id);
		result.append(", kind: ");
		result.append(kind);
		result.append(", rationale: ");
		result.append(rationale);
		result.append(", sourceAssetId: ");
		result.append(sourceAssetId);
		result.append(", targetAssetId: ");
		result.append(targetAssetId);
		result.append(')');
		return result.toString();
	}

} //AssetRelationImpl
