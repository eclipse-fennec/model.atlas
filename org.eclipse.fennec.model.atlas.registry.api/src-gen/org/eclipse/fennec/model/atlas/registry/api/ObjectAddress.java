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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Object Address</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Where an instance lives: one object id in one stage of one registry of one scope.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getScope <em>Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getRegistry <em>Registry</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getStage <em>Stage</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getObjectId <em>Object Id</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectAddress()
 * @model
 * @generated
 */
@ProviderType
public interface ObjectAddress {
	/**
	 * Returns the value of the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the scope name
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Scope</em>' attribute.
	 * @see #setScope(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectAddress_Scope()
	 * @model
	 * @generated
	 */
	String getScope();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getScope <em>Scope</em>}' attribute.
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
	 * <!-- begin-model-doc -->
	 * the registry name
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Registry</em>' attribute.
	 * @see #setRegistry(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectAddress_Registry()
	 * @model
	 * @generated
	 */
	String getRegistry();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getRegistry <em>Registry</em>}' attribute.
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
	 * <!-- begin-model-doc -->
	 * the stage name
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Stage</em>' attribute.
	 * @see #setStage(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectAddress_Stage()
	 * @model
	 * @generated
	 */
	String getStage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getStage <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stage</em>' attribute.
	 * @see #getStage()
	 * @generated
	 */
	void setStage(String value);

	/**
	 * Returns the value of the '<em><b>Object Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the object id. A write may leave it null when the content's class has an ID attribute: the id is then taken from that attribute, and an id given as well must equal it. Content without an ID attribute needs an explicit id
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Object Id</em>' attribute.
	 * @see #setObjectId(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectAddress_ObjectId()
	 * @model
	 * @generated
	 */
	String getObjectId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectAddress#getObjectId <em>Object Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Object Id</em>' attribute.
	 * @see #getObjectId()
	 * @generated
	 */
	void setObjectId(String value);

} // ObjectAddress
