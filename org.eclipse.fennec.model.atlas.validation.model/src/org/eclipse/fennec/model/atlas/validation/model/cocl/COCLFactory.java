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

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage
 * @generated
 */
@ProviderType
public interface COCLFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	COCLFactory eINSTANCE = org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Ocl Constraint</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Ocl Constraint</em>'.
	 * @generated
	 */
	OclConstraint createOclConstraint();

	/**
	 * Returns a new object of class '<em>Ocl Constraint Set</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Ocl Constraint Set</em>'.
	 * @generated
	 */
	OclConstraintSet createOclConstraintSet();

	/**
	 * Returns a new object of class '<em>Validation Response</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Validation Response</em>'.
	 * @generated
	 */
	ValidationResponse createValidationResponse();

	/**
	 * Returns a new object of class '<em>Simple Validation Result</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Simple Validation Result</em>'.
	 * @generated
	 */
	SimpleValidationResult createSimpleValidationResult();

	/**
	 * Returns a new object of class '<em>EObject Validation Result</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>EObject Validation Result</em>'.
	 * @generated
	 */
	EObjectValidationResult createEObjectValidationResult();

	/**
	 * Returns a new object of class '<em>Derived Validation Request</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Derived Validation Request</em>'.
	 * @generated
	 */
	DerivedValidationRequest createDerivedValidationRequest();

	/**
	 * Returns a new object of class '<em>Operation Validation Request</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Operation Validation Request</em>'.
	 * @generated
	 */
	OperationValidationRequest createOperationValidationRequest();

	/**
	 * Returns a new object of class '<em>Operation Request Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Operation Request Parameter</em>'.
	 * @generated
	 */
	OperationRequestParameter createOperationRequestParameter();

	/**
	 * Returns a new object of class '<em>Diagnostic</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Diagnostic</em>'.
	 * @generated
	 */
	Diagnostic createDiagnostic();

	/**
	 * Returns a new object of class '<em>Batch Validation Request</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Batch Validation Request</em>'.
	 * @generated
	 */
	BatchValidationRequest createBatchValidationRequest();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	COCLPackage getCOCLPackage();

} //COCLFactory
