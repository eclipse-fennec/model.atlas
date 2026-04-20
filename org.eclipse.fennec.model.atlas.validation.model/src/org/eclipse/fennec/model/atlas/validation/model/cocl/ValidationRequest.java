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
 * A representation of the model object '<em><b>Validation Request</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Abstract validation request object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest#getRole <em>Role</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest#getValidationObjects <em>Validation Objects</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest#getCoclId <em>Cocl Id</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getValidationRequest()
 * @model abstract="true"
 * @generated
 */
@ProviderType
public interface ValidationRequest extends EObject {
	/**
	 * Returns the value of the '<em><b>Role</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Role</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole
	 * @see #setRole(OclRole)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getValidationRequest_Role()
	 * @model
	 * @generated
	 */
	OclRole getRole();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest#getRole <em>Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Role</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole
	 * @see #getRole()
	 * @generated
	 */
	void setRole(OclRole value);

	/**
	 * Returns the value of the '<em><b>Validation Objects</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EObject}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Objects to be validated. Muli-valued for batch request
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Validation Objects</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getValidationRequest_ValidationObjects()
	 * @model containment="true"
	 * @generated
	 */
	EList<EObject> getValidationObjects();

	/**
	 * Returns the value of the '<em><b>Cocl Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The id of the ocol file
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Cocl Id</em>' attribute.
	 * @see #setCoclId(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getValidationRequest_CoclId()
	 * @model
	 * @generated
	 */
	String getCoclId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest#getCoclId <em>Cocl Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Cocl Id</em>' attribute.
	 * @see #getCoclId()
	 * @generated
	 */
	void setCoclId(String value);

} // ValidationRequest
