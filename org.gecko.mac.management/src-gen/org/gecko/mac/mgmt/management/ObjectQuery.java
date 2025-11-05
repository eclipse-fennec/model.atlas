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
 *      Mark Hoffmann - initial API and implementation
 */
package org.gecko.mac.mgmt.management;

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
 *   <li>{@link org.gecko.mac.mgmt.management.ObjectQuery#getUploadUser <em>Upload User</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.ObjectQuery#getSourceChannel <em>Source Channel</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.ObjectQuery#getObjectType <em>Object Type</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.ObjectQuery#getStatus <em>Status</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.mgmt.management.ManagementPackage#getObjectQuery()
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
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getObjectQuery_UploadUser()
	 * @model
	 * @generated
	 */
	String getUploadUser();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.ObjectQuery#getUploadUser <em>Upload User</em>}' attribute.
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
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getObjectQuery_SourceChannel()
	 * @model
	 * @generated
	 */
	String getSourceChannel();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.ObjectQuery#getSourceChannel <em>Source Channel</em>}' attribute.
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
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getObjectQuery_ObjectType()
	 * @model
	 * @generated
	 */
	String getObjectType();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.ObjectQuery#getObjectType <em>Object Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Object Type</em>' attribute.
	 * @see #getObjectType()
	 * @generated
	 */
	void setObjectType(String value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * The literals are from the enumeration {@link org.gecko.mac.mgmt.management.ObjectStatus}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Filter by object status
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see org.gecko.mac.mgmt.management.ObjectStatus
	 * @see #setStatus(ObjectStatus)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getObjectQuery_Status()
	 * @model
	 * @generated
	 */
	ObjectStatus getStatus();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.ObjectQuery#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see org.gecko.mac.mgmt.management.ObjectStatus
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(ObjectStatus value);

} // ObjectQuery
