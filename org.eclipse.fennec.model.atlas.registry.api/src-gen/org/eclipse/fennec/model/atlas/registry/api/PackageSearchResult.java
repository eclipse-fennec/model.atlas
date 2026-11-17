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

import org.eclipse.emf.common.util.EList;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Package Search Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One page of a package search.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult#getHits <em>Hits</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult#getTotalHits <em>Total Hits</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearchResult()
 * @model
 * @generated
 */
@ProviderType
public interface PackageSearchResult {
	/**
	 * Returns the value of the '<em><b>Hits</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the metadata of the hits on this page that still resolve; a hit from an ancestor scope is marked read-only
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Hits</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearchResult_Hits()
	 * @model resolveProxies="false"
	 * @generated
	 */
	EList<ObjectMetadata> getHits();

	/**
	 * Returns the value of the '<em><b>Total Hits</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the number of hits across all pages, as the search index counts them
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Total Hits</em>' attribute.
	 * @see #setTotalHits(long)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearchResult_TotalHits()
	 * @model
	 * @generated
	 */
	long getTotalHits();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult#getTotalHits <em>Total Hits</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Total Hits</em>' attribute.
	 * @see #getTotalHits()
	 * @generated
	 */
	void setTotalHits(long value);

} // PackageSearchResult
