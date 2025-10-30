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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>EPackage List Response</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getPackages <em>Packages</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getTotal <em>Total</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getSkip <em>Skip</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getLimit <em>Limit</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getEPackageListResponse()
 * @model
 * @generated
 */
@ProviderType
public interface EPackageListResponse extends EObject {
	/**
	 * Returns the value of the '<em><b>Packages</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Packages</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getEPackageListResponse_Packages()
	 * @model containment="true"
	 * @generated
	 */
	EList<EPackageInfo> getPackages();

	/**
	 * Returns the value of the '<em><b>Total</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Total</em>' attribute.
	 * @see #setTotal(int)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getEPackageListResponse_Total()
	 * @model
	 * @generated
	 */
	int getTotal();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getTotal <em>Total</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Total</em>' attribute.
	 * @see #getTotal()
	 * @generated
	 */
	void setTotal(int value);

	/**
	 * Returns the value of the '<em><b>Skip</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Skip</em>' attribute.
	 * @see #setSkip(int)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getEPackageListResponse_Skip()
	 * @model default="0"
	 * @generated
	 */
	int getSkip();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getSkip <em>Skip</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Skip</em>' attribute.
	 * @see #getSkip()
	 * @generated
	 */
	void setSkip(int value);

	/**
	 * Returns the value of the '<em><b>Limit</b></em>' attribute.
	 * The default value is <code>"100"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Limit</em>' attribute.
	 * @see #setLimit(int)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getEPackageListResponse_Limit()
	 * @model default="100"
	 * @generated
	 */
	int getLimit();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse#getLimit <em>Limit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Limit</em>' attribute.
	 * @see #getLimit()
	 * @generated
	 */
	void setLimit(int value);

} // EPackageListResponse
