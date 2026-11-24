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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Package Batch</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Packages written as one unit by RegistryWriter#writePackages: all of them, or none.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageBatch#getWrites <em>Writes</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageBatch()
 * @model
 * @generated
 */
@ProviderType
public interface PackageBatch {
	/**
	 * Returns the value of the '<em><b>Writes</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the packages, in the order they are written
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Writes</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageBatch_Writes()
	 * @model containment="true"
	 * @generated
	 */
	EList<PackageWrite> getWrites();

} // PackageBatch
