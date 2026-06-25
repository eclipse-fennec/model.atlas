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
package org.eclipse.fennec.model.atlas.scope.api;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Registry Info</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getStages <em>Stages</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getRegistryInfo()
 * @model
 * @generated
 */
@ProviderType
public interface RegistryInfo {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getRegistryInfo_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getName <em>Name</em>}' attribute.
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
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getRegistryInfo_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.scope.api.RegistryType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.scope.api.RegistryType
	 * @see #setType(RegistryType)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getRegistryInfo_Type()
	 * @model
	 * @generated
	 */
	RegistryType getType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.scope.api.RegistryType
	 * @see #getType()
	 * @generated
	 */
	void setType(RegistryType value);

	/**
	 * Returns the value of the '<em><b>Stages</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.scope.api.StageInfo}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stages</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getRegistryInfo_Stages()
	 * @model containment="true"
	 * @generated
	 */
	List<StageInfo> getStages();

} // RegistryInfo
