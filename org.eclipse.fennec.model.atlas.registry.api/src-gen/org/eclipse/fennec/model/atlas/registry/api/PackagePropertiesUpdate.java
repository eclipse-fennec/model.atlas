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

import org.eclipse.emf.common.util.EMap;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Package Properties Update</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A request to change metadata properties of a stored package without uploading it again.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackagePropertiesUpdate#getAddress <em>Address</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackagePropertiesUpdate#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackagePropertiesUpdate#getPrecondition <em>Precondition</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackagePropertiesUpdate()
 * @model
 * @generated
 */
@ProviderType
public interface PackagePropertiesUpdate {
	/**
	 * Returns the value of the '<em><b>Address</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the package
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Address</em>' containment reference.
	 * @see #setAddress(PackageAddress)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackagePropertiesUpdate_Address()
	 * @model containment="true"
	 * @generated
	 */
	PackageAddress getAddress();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackagePropertiesUpdate#getAddress <em>Address</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Address</em>' containment reference.
	 * @see #getAddress()
	 * @generated
	 */
	void setAddress(PackageAddress value);

	/**
	 * Returns the value of the '<em><b>Properties</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link java.lang.String},
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the properties to set
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Properties</em>' map.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackagePropertiesUpdate_Properties()
	 * @model mapType="org.eclipse.emf.ecore.EStringToStringMapEntry&lt;org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EString&gt;"
	 * @generated
	 */
	EMap<String, String> getProperties();

	/**
	 * Returns the value of the '<em><b>Precondition</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * checked against the package before it is changed; may be null
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Precondition</em>' containment reference.
	 * @see #setPrecondition(Precondition)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackagePropertiesUpdate_Precondition()
	 * @model containment="true"
	 * @generated
	 */
	Precondition getPrecondition();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackagePropertiesUpdate#getPrecondition <em>Precondition</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Precondition</em>' containment reference.
	 * @see #getPrecondition()
	 * @generated
	 */
	void setPrecondition(Precondition value);

} // PackagePropertiesUpdate
