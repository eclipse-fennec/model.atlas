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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Object Write</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A request to store an instance.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getAddress <em>Address</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getContent <em>Content</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getOnConflict <em>On Conflict</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getPrecondition <em>Precondition</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectWrite()
 * @model
 * @generated
 */
@ProviderType
public interface ObjectWrite {
	/**
	 * Returns the value of the '<em><b>Address</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * where to store it
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Address</em>' containment reference.
	 * @see #setAddress(ObjectAddress)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectWrite_Address()
	 * @model containment="true"
	 * @generated
	 */
	ObjectAddress getAddress();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getAddress <em>Address</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Address</em>' containment reference.
	 * @see #getAddress()
	 * @generated
	 */
	void setAddress(ObjectAddress value);

	/**
	 * Returns the value of the '<em><b>Content</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the object; its type must be a root type of the registry. Referenced, not contained: the write does not take it out of its resource
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Content</em>' reference.
	 * @see #setContent(EObject)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectWrite_Content()
	 * @model resolveProxies="false"
	 * @generated
	 */
	EObject getContent();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getContent <em>Content</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Content</em>' reference.
	 * @see #getContent()
	 * @generated
	 */
	void setContent(EObject value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * a human-readable name, may be null
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectWrite_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the version to record, may be null
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectWrite_Version()
	 * @model
	 * @generated
	 */
	String getVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(String value);

	/**
	 * Returns the value of the '<em><b>On Conflict</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * what a create does when the address is taken; ignored by updates. Defaults to FAIL
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>On Conflict</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy
	 * @see #setOnConflict(ConflictPolicy)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectWrite_OnConflict()
	 * @model
	 * @generated
	 */
	ConflictPolicy getOnConflict();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getOnConflict <em>On Conflict</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>On Conflict</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy
	 * @see #getOnConflict()
	 * @generated
	 */
	void setOnConflict(ConflictPolicy value);

	/**
	 * Returns the value of the '<em><b>Precondition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * checked against the existing object before it is replaced; may be null
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Precondition</em>' attribute.
	 * @see #setPrecondition(Precondition)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectWrite_Precondition()
	 * @model dataType="org.eclipse.fennec.model.atlas.registry.api.Precondition"
	 * @generated
	 */
	Precondition getPrecondition();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite#getPrecondition <em>Precondition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Precondition</em>' attribute.
	 * @see #getPrecondition()
	 * @generated
	 */
	void setPrecondition(Precondition value);

} // ObjectWrite
