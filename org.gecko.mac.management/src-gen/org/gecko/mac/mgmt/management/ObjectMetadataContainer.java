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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Object Metadata Container</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Container for managing collections of ObjectMetadata with efficient keyed access. Note: With Lucene indexing, this container may not be necessary as the index serves as the registry.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.mgmt.management.ObjectMetadataContainer#getContainerId <em>Container Id</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.ObjectMetadataContainer#getMetadata <em>Metadata</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.mgmt.management.ManagementPackage#getObjectMetadataContainer()
 * @model
 * @generated
 */
@ProviderType
public interface ObjectMetadataContainer extends EObject {
	/**
	 * Returns the value of the '<em><b>Container Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unique container identifier
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Container Id</em>' attribute.
	 * @see #setContainerId(String)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getObjectMetadataContainer_ContainerId()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getContainerId();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.ObjectMetadataContainer#getContainerId <em>Container Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Container Id</em>' attribute.
	 * @see #getContainerId()
	 * @generated
	 */
	void setContainerId(String value);

	/**
	 * Returns the value of the '<em><b>Metadata</b></em>' containment reference list.
	 * The list contents are of type {@link org.gecko.mac.mgmt.management.ObjectMetadata}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Collection of metadata objects with automatic keying by objectId for efficient lookups
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Metadata</em>' containment reference list.
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getObjectMetadataContainer_Metadata()
	 * @model containment="true" keys="objectId"
	 * @generated
	 */
	EList<ObjectMetadata> getMetadata();

} // ObjectMetadataContainer
