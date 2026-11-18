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
 * A representation of the model object '<em><b>Precondition</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * An optimistic-concurrency check: the write proceeds only if the object it is about to change is still in a state the caller has seen. The writer compares it with the object's metadata as it has just read it, within the same call. Front ends map their own conditional requests onto it, e.g. the REST layer its If-Match header.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.Precondition#getExpected <em>Expected</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.Precondition#getTarget <em>Target</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPrecondition()
 * @model
 * @generated
 */
@ProviderType
public interface Precondition {
	/**
	 * Returns the value of the '<em><b>Expected</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the validators of the states the caller has seen, as Validators#validator computes them; empty holds for every object
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Expected</em>' attribute list.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPrecondition_Expected()
	 * @model
	 * @generated
	 */
	EList<String> getExpected();

	/**
	 * Returns the value of the '<em><b>Target</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.registry.api.PreconditionTarget}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * which validator of the current object is compared; CONTENT unless set
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Target</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PreconditionTarget
	 * @see #setTarget(PreconditionTarget)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPrecondition_Target()
	 * @model
	 * @generated
	 */
	PreconditionTarget getTarget();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.Precondition#getTarget <em>Target</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.PreconditionTarget
	 * @see #getTarget()
	 * @generated
	 */
	void setTarget(PreconditionTarget value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return whether the write may proceed: expected is empty, the object has no validator yet, or its validator is one of the expected ones
	 * @param current the current metadata of the object the write addresses
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	boolean isSatisfiedBy(ObjectMetadata current);

} // Precondition
