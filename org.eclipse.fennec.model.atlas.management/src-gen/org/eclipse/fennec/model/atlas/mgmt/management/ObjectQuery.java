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
package org.eclipse.fennec.model.atlas.mgmt.management;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Object Query</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Generic query criteria for searching objects
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getUploadUser <em>Upload User</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getSourceChannel <em>Source Channel</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getObjectType <em>Object Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getStatus <em>Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getRole <em>Role</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getScope <em>Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getRegistry <em>Registry</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectQuery()
 * @model
 * @generated
 */
@ProviderType
public interface ObjectQuery extends EObject {
	/**
	 * Returns the value of the '<em><b>Upload User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Filter by upload user
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Upload User</em>' attribute.
	 * @see #setUploadUser(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectQuery_UploadUser()
	 * @model
	 * @generated
	 */
	String getUploadUser();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getUploadUser <em>Upload User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Upload User</em>' attribute.
	 * @see #getUploadUser()
	 * @generated
	 */
	void setUploadUser(String value);

	/**
	 * Returns the value of the '<em><b>Source Channel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Filter by source channel
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source Channel</em>' attribute.
	 * @see #setSourceChannel(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectQuery_SourceChannel()
	 * @model
	 * @generated
	 */
	String getSourceChannel();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getSourceChannel <em>Source Channel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Channel</em>' attribute.
	 * @see #getSourceChannel()
	 * @generated
	 */
	void setSourceChannel(String value);

	/**
	 * Returns the value of the '<em><b>Object Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Filter by object type
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Object Type</em>' attribute.
	 * @see #setObjectType(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectQuery_ObjectType()
	 * @model
	 * @generated
	 */
	String getObjectType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getObjectType <em>Object Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Object Type</em>' attribute.
	 * @see #getObjectType()
	 * @generated
	 */
	void setObjectType(String value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Filter by object status
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus
	 * @see #setStatus(ObjectStatus)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectQuery_Status()
	 * @model
	 * @generated
	 */
	ObjectStatus getStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(ObjectStatus value);

	/**
	 * Returns the value of the '<em><b>Role</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Filter by object role
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Role</em>' attribute.
	 * @see #setRole(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectQuery_Role()
	 * @model
	 * @generated
	 */
	String getRole();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getRole <em>Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Role</em>' attribute.
	 * @see #getRole()
	 * @generated
	 */
	void setRole(String value);

	/**
	 * Returns the value of the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Filter by object scope
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Scope</em>' attribute.
	 * @see #setScope(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectQuery_Scope()
	 * @model
	 * @generated
	 */
	String getScope();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getScope <em>Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scope</em>' attribute.
	 * @see #getScope()
	 * @generated
	 */
	void setScope(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Filter by object name
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectQuery_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Registry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Filter by object scope
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Registry</em>' attribute.
	 * @see #setRegistry(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectQuery_Registry()
	 * @model
	 * @generated
	 */
	String getRegistry();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery#getRegistry <em>Registry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Registry</em>' attribute.
	 * @see #getRegistry()
	 * @generated
	 */
	void setRegistry(String value);

} // ObjectQuery
