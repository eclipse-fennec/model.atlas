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
package org.eclipse.fennec.model.atlas.validation.model.cocl;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Derived Validation Request</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Validation request for derived field validation
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest#getDerivedFeature <em>Derived Feature</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getDerivedValidationRequest()
 * @model
 * @generated
 */
@ProviderType
public interface DerivedValidationRequest extends ValidationRequest {
	/**
	 * Returns the value of the '<em><b>Derived Feature</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EStructuralFeature}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * EstructuralFeature for the derived field to be validated
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Derived Feature</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getDerivedValidationRequest_DerivedFeature()
	 * @model required="true"
	 * @generated
	 */
	EList<EStructuralFeature> getDerivedFeature();

} // DerivedValidationRequest
