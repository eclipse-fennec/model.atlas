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
 * A representation of the model object '<em><b>Object Batch</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Instances written as one unit by RegistryWriter#writeObjects: all of them, or none.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.ObjectBatch#getWrites <em>Writes</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectBatch()
 * @model
 * @generated
 */
@ProviderType
public interface ObjectBatch {
	/**
	 * Returns the value of the '<em><b>Writes</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.registry.api.ObjectWrite}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the instances, in the order they are written
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Writes</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getObjectBatch_Writes()
	 * @model containment="true"
	 * @generated
	 */
	EList<ObjectWrite> getWrites();

} // ObjectBatch
