/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 */
package org.eclipse.fennec.model.atlas.rest.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Batch Response</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * The answer to a batch write: all items were applied, or none.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.BatchResponse#isApplied <em>Applied</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.BatchResponse#getItems <em>Items</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getBatchResponse()
 * @model
 * @generated
 */
@ProviderType
public interface BatchResponse extends EObject {
	/**
	 * Returns the value of the '<em><b>Applied</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Applied</em>' attribute.
	 * @see #setApplied(boolean)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getBatchResponse_Applied()
	 * @model
	 * @generated
	 */
	boolean isApplied();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.BatchResponse#isApplied <em>Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Applied</em>' attribute.
	 * @see #isApplied()
	 * @generated
	 */
	void setApplied(boolean value);

	/**
	 * Returns the value of the '<em><b>Items</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.rest.model.BatchItem}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Items</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getBatchResponse_Items()
	 * @model containment="true"
	 * @generated
	 */
	EList<BatchItem> getItems();

} // BatchResponse
