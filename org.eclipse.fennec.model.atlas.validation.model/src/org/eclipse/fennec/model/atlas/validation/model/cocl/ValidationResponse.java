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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Validation Response</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Validation response object, that i returned for all ValidationRequests
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse#getResults <em>Results</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse#getRole <em>Role</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse#getDiagnostics <em>Diagnostics</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getValidationResponse()
 * @model
 * @generated
 */
@ProviderType
public interface ValidationResponse extends EObject {
	/**
	 * Returns the value of the '<em><b>Results</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResult}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The resulting objects
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Results</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getValidationResponse_Results()
	 * @model containment="true"
	 * @generated
	 */
	EList<ValidationResult> getResults();

	/**
	 * Returns the value of the '<em><b>Role</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Role</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole
	 * @see #setRole(OclRole)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getValidationResponse_Role()
	 * @model
	 * @generated
	 */
	OclRole getRole();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse#getRole <em>Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Role</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole
	 * @see #getRole()
	 * @generated
	 */
	void setRole(OclRole value);

	/**
	 * Returns the value of the '<em><b>Diagnostics</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List of diagnostics to communicate
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Diagnostics</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getValidationResponse_Diagnostics()
	 * @model containment="true"
	 * @generated
	 */
	EList<Diagnostic> getDiagnostics();

} // ValidationResponse
