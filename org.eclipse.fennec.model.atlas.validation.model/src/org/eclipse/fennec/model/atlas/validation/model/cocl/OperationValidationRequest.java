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

import org.eclipse.emf.ecore.EOperation;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Operation Validation Request</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Validation request for operation validation
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest#getOperation <em>Operation</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest#getParameters <em>Parameters</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest#getOperationName <em>Operation Name</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOperationValidationRequest()
 * @model
 * @generated
 */
@ProviderType
public interface OperationValidationRequest extends ValidationRequest {
	/**
	 * Returns the value of the '<em><b>Operation</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Operation to be executed. This has to be provided by the client when no C-OCL id is provided, but an EOperation defined in the model has to be invoked.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Operation</em>' reference.
	 * @see #setOperation(EOperation)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOperationValidationRequest_Operation()
	 * @model
	 * @generated
	 */
	EOperation getOperation();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest#getOperation <em>Operation</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Operation</em>' reference.
	 * @see #getOperation()
	 * @generated
	 */
	void setOperation(EOperation value);

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Operation . This has to be provided by the client when no C-OCL id is provided, but an EOperation defined in the model has to be invoked.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parameters</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOperationValidationRequest_Parameters()
	 * @model containment="true"
	 * @generated
	 */
	EList<OperationRequestParameter> getParameters();

	/**
	 * Returns the value of the '<em><b>Operation Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When requesting an OperationValidation using a C-OCL id, the client has to provide an operationName, which then will be used to filter the right OPERATION Constraint from the C-OCL ConstraintSet.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Operation Name</em>' attribute.
	 * @see #setOperationName(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOperationValidationRequest_OperationName()
	 * @model
	 * @generated
	 */
	String getOperationName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest#getOperationName <em>Operation Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Operation Name</em>' attribute.
	 * @see #getOperationName()
	 * @generated
	 */
	void setOperationName(String value);

} // OperationValidationRequest
