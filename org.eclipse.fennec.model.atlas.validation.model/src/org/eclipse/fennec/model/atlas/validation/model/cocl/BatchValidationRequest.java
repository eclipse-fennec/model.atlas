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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Batch Validation Request</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest#getFilterConstraint <em>Filter Constraint</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getBatchValidationRequest()
 * @model
 * @generated
 */
@ProviderType
public interface BatchValidationRequest extends ValidationRequest {
	/**
	 * Returns the value of the '<em><b>Filter Constraint</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * An optional OclConstraint representing a filter constraint. It has to be of role type REFERENCE_FILTER
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Filter Constraint</em>' containment reference.
	 * @see #setFilterConstraint(OclConstraint)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getBatchValidationRequest_FilterConstraint()
	 * @model containment="true"
	 * @generated
	 */
	OclConstraint getFilterConstraint();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest#getFilterConstraint <em>Filter Constraint</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Filter Constraint</em>' containment reference.
	 * @see #getFilterConstraint()
	 * @generated
	 */
	void setFilterConstraint(OclConstraint value);

} // BatchValidationRequest
