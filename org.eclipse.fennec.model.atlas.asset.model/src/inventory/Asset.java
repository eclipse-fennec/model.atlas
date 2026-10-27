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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Asset</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A participant in a system, as well as the system itself. This is all we have to keep track of in an infrastructure, for whatever purpose: a host, a container, a network, a volume, an application, a data set. Generated from deployment and service specifications where possible, then curated.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link inventory.Asset#getId <em>Id</em>}</li>
 *   <li>{@link inventory.Asset#getName <em>Name</em>}</li>
 *   <li>{@link inventory.Asset#getDescription <em>Description</em>}</li>
 *   <li>{@link inventory.Asset#getOwner <em>Owner</em>}</li>
 *   <li>{@link inventory.Asset#getRelations <em>Relations</em>}</li>
 *   <li>{@link inventory.Asset#getSources <em>Sources</em>}</li>
 *   <li>{@link inventory.Asset#getAspects <em>Aspects</em>}</li>
 * </ul>
 *
 * @see inventory.InventoryPackage#getAsset()
 * @model
 * @generated
 */
@ProviderType
public interface Asset extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Identifier, unique across assets. EMF ID.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see inventory.InventoryPackage#getAsset_Id()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link inventory.Asset#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Display name.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see inventory.InventoryPackage#getAsset_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link inventory.Asset#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Description.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see inventory.InventoryPackage#getAsset_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link inventory.Asset#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Responsible role or person.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Owner</em>' attribute.
	 * @see #setOwner(String)
	 * @see inventory.InventoryPackage#getAsset_Owner()
	 * @model
	 * @generated
	 */
	String getOwner();

	/**
	 * Sets the value of the '{@link inventory.Asset#getOwner <em>Owner</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Owner</em>' attribute.
	 * @see #getOwner()
	 * @generated
	 */
	void setOwner(String value);

	/**
	 * Returns the value of the '<em><b>Relations</b></em>' containment reference list.
	 * The list contents are of type {@link inventory.AssetRelation}.
	 * It is bidirectional and its opposite is '{@link inventory.AssetRelation#getTarget <em>Target</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Relations to other assets.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Relations</em>' containment reference list.
	 * @see inventory.InventoryPackage#getAsset_Relations()
	 * @see inventory.AssetRelation#getTarget
	 * @model opposite="target" containment="true" keys="id"
	 * @generated
	 */
	EList<AssetRelation> getRelations();

	/**
	 * Returns the value of the '<em><b>Sources</b></em>' containment reference list.
	 * The list contents are of type {@link inventory.SourceRef}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Where the asset is defined.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Sources</em>' containment reference list.
	 * @see inventory.InventoryPackage#getAsset_Sources()
	 * @model containment="true"
	 * @generated
	 */
	EList<SourceRef> getSources();

	/**
	 * Returns the value of the '<em><b>Aspects</b></em>' reference list.
	 * The list contents are of type {@link inventory.Aspect}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Context-specific extensions.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Aspects</em>' reference list.
	 * @see inventory.InventoryPackage#getAsset_Aspects()
	 * @model resolveProxies="false" transient="true" volatile="true" derived="true"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel get='java.util.List&lt;Aspect&gt; aspects = new java.util.ArrayList&lt;&gt;();\nfor (AssetRelation relation : getRelations()) {\n      if (relation instanceof Aspect aspect) {\n          aspects.add(aspect);\n      }\n  }\n  return new org.eclipse.emf.common.util.BasicEList.UnmodifiableEList&lt;&gt;(aspects.size(), aspects.toArray());'"
	 * @generated
	 */
	EList<Aspect> getAspects();

} // Asset
