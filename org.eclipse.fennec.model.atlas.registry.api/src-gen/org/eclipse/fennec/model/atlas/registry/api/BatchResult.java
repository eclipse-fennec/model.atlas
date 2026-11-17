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

import java.util.Optional;

import org.eclipse.emf.common.util.EList;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Batch Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * The result of a batch write: all of its items were applied, or none.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.BatchResult#isApplied <em>Applied</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.BatchResult#getItems <em>Items</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getBatchResult()
 * @model
 * @generated
 */
@ProviderType
public interface BatchResult {
	/**
	 * Returns the value of the '<em><b>Applied</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * whether the batch was applied; false means at least one item was refused and nothing of the batch remains written
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Applied</em>' attribute.
	 * @see #setApplied(boolean)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getBatchResult_Applied()
	 * @model
	 * @generated
	 */
	boolean isApplied();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchResult#isApplied <em>Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Applied</em>' attribute.
	 * @see #isApplied()
	 * @generated
	 */
	void setApplied(boolean value);

	/**
	 * Returns the value of the '<em><b>Items</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * one result per item, in the order of the batch
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Items</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getBatchResult_Items()
	 * @model containment="true"
	 * @generated
	 */
	EList<BatchItemResult> getItems();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return the first refused item, if the batch was not applied
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.registry.api.Optional&lt;org.eclipse.fennec.model.atlas.registry.api.BatchItemResult&gt;"
	 * @generated
	 */
	Optional<BatchItemResult> firstRefusal();

} // BatchResult
