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
 * A representation of the model object '<em><b>Scope Info</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getParentScope <em>Parent Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getRegistries <em>Registries</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getScopeInfo()
 * @model
 * @generated
 */
@ProviderType
public interface ScopeInfo {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getScopeInfo_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getName <em>Name</em>}' attribute.
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
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getScopeInfo_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Parent Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent Scope</em>' attribute.
	 * @see #setParentScope(String)
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getScopeInfo_ParentScope()
	 * @model
	 * @generated
	 */
	String getParentScope();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.scope.api.ScopeInfo#getParentScope <em>Parent Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent Scope</em>' attribute.
	 * @see #getParentScope()
	 * @generated
	 */
	void setParentScope(String value);

	/**
	 * Returns the value of the '<em><b>Registries</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.scope.api.RegistryInfo}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Registries</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getScopeInfo_Registries()
	 * @model containment="true"
	 * @generated
	 */
	List<RegistryInfo> getRegistries();

} // ScopeInfo
