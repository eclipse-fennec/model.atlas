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

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Write Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * The result of a RegistryWriter operation.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.WriteResult#getOutcome <em>Outcome</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.WriteResult#getMetadata <em>Metadata</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getWriteResult()
 * @model
 * @generated
 */
@ProviderType
public interface WriteResult {
	/**
	 * Returns the value of the '<em><b>Outcome</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.registry.api.WriteOutcome}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * what the operation did
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Outcome</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.WriteOutcome
	 * @see #setOutcome(WriteOutcome)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getWriteResult_Outcome()
	 * @model
	 * @generated
	 */
	WriteOutcome getOutcome();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.WriteResult#getOutcome <em>Outcome</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Outcome</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.WriteOutcome
	 * @see #getOutcome()
	 * @generated
	 */
	void setOutcome(WriteOutcome value);

	/**
	 * Returns the value of the '<em><b>Metadata</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the metadata of the object as it is now stored, or null for NOT_FOUND and DELETED
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Metadata</em>' containment reference.
	 * @see #setMetadata(ObjectMetadata)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getWriteResult_Metadata()
	 * @model containment="true"
	 * @generated
	 */
	ObjectMetadata getMetadata();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.WriteResult#getMetadata <em>Metadata</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Metadata</em>' containment reference.
	 * @see #getMetadata()
	 * @generated
	 */
	void setMetadata(ObjectMetadata value);

} // WriteResult
