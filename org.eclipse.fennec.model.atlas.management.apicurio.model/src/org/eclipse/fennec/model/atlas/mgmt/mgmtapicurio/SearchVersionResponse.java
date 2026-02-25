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
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Search Version Response</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchVersionResponse#getVersions <em>Versions</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchVersionResponse#getCount <em>Count</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchVersionResponse()
 * @model
 * @generated
 */
@ProviderType
public interface SearchVersionResponse extends EObject {
	/**
	 * Returns the value of the '<em><b>Versions</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Versions</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchVersionResponse_Versions()
	 * @model containment="true"
	 * @generated
	 */
	EList<SearchedVersion> getVersions();

	/**
	 * Returns the value of the '<em><b>Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The total number of artifacts that matched the query that produced the result set (may be more than the number of artifacts in the result set).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Count</em>' attribute.
	 * @see #setCount(int)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchVersionResponse_Count()
	 * @model required="true"
	 * @generated
	 */
	int getCount();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchVersionResponse#getCount <em>Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Count</em>' attribute.
	 * @see #getCount()
	 * @generated
	 */
	void setCount(int value);

} // SearchVersionResponse
