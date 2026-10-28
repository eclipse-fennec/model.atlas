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
package inventory;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Asset Relation</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A typed relation from the owning asset to another asset.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link inventory.AssetRelation#getId <em>Id</em>}</li>
 *   <li>{@link inventory.AssetRelation#getKind <em>Kind</em>}</li>
 *   <li>{@link inventory.AssetRelation#getRationale <em>Rationale</em>}</li>
 *   <li>{@link inventory.AssetRelation#getSourceAssetId <em>Source Asset Id</em>}</li>
 *   <li>{@link inventory.AssetRelation#getTargetAssetId <em>Target Asset Id</em>}</li>
 * </ul>
 *
 * @see inventory.InventoryPackage#getAssetRelation()
 * @model
 * @generated
 */
@ProviderType
public interface AssetRelation extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Identifier, unique within the inventory. EMF ID: unique within the whole inventory resource, also across assets, measures and processing activities, so references are written by id and survive reordering.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see inventory.InventoryPackage#getAssetRelation_Id()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link inventory.AssetRelation#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The literals are from the enumeration {@link inventory.AssetRelationKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Kind of relation.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see inventory.AssetRelationKind
	 * @see #setKind(AssetRelationKind)
	 * @see inventory.InventoryPackage#getAssetRelation_Kind()
	 * @model required="true"
	 * @generated
	 */
	AssetRelationKind getKind();

	/**
	 * Sets the value of the '{@link inventory.AssetRelation#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see inventory.AssetRelationKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(AssetRelationKind value);

	/**
	 * Returns the value of the '<em><b>Rationale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Why the relation exists.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Rationale</em>' attribute.
	 * @see #setRationale(String)
	 * @see inventory.InventoryPackage#getAssetRelation_Rationale()
	 * @model
	 * @generated
	 */
	String getRationale();

	/**
	 * Sets the value of the '{@link inventory.AssetRelation#getRationale <em>Rationale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Rationale</em>' attribute.
	 * @see #getRationale()
	 * @generated
	 */
	void setRationale(String value);

	/**
	 * Returns the value of the '<em><b>Source Asset Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source Asset Id</em>' attribute.
	 * @see #setSourceAssetId(String)
	 * @see inventory.InventoryPackage#getAssetRelation_SourceAssetId()
	 * @model required="true"
	 * @generated
	 */
	String getSourceAssetId();

	/**
	 * Sets the value of the '{@link inventory.AssetRelation#getSourceAssetId <em>Source Asset Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Asset Id</em>' attribute.
	 * @see #getSourceAssetId()
	 * @generated
	 */
	void setSourceAssetId(String value);

	/**
	 * Returns the value of the '<em><b>Target Asset Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Target Asset Id</em>' attribute.
	 * @see #setTargetAssetId(String)
	 * @see inventory.InventoryPackage#getAssetRelation_TargetAssetId()
	 * @model required="true"
	 * @generated
	 */
	String getTargetAssetId();

	/**
	 * Sets the value of the '{@link inventory.AssetRelation#getTargetAssetId <em>Target Asset Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target Asset Id</em>' attribute.
	 * @see #getTargetAssetId()
	 * @generated
	 */
	void setTargetAssetId(String value);

} // AssetRelation
