/**
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
package org.eclipse.fennec.model.atlas.validation.model.cocl.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.fennec.model.atlas.validation.model.cocl.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage
 * @generated
 */
public class COCLSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static COCLPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public COCLSwitch() {
		if (modelPackage == null) {
			modelPackage = COCLPackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case COCLPackage.OCL_CONSTRAINT: {
				OclConstraint oclConstraint = (OclConstraint)theEObject;
				T result = caseOclConstraint(oclConstraint);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case COCLPackage.OCL_CONSTRAINT_SET: {
				OclConstraintSet oclConstraintSet = (OclConstraintSet)theEObject;
				T result = caseOclConstraintSet(oclConstraintSet);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case COCLPackage.VALIDATION_REQUEST: {
				ValidationRequest validationRequest = (ValidationRequest)theEObject;
				T result = caseValidationRequest(validationRequest);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case COCLPackage.VALIDATION_RESPONSE: {
				ValidationResponse validationResponse = (ValidationResponse)theEObject;
				T result = caseValidationResponse(validationResponse);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case COCLPackage.VALIDATION_RESULT: {
				ValidationResult validationResult = (ValidationResult)theEObject;
				T result = caseValidationResult(validationResult);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case COCLPackage.SIMPLE_VALIDATION_RESULT: {
				SimpleValidationResult simpleValidationResult = (SimpleValidationResult)theEObject;
				T result = caseSimpleValidationResult(simpleValidationResult);
				if (result == null) result = caseValidationResult(simpleValidationResult);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case COCLPackage.EOBJECT_VALIDATION_RESULT: {
				EObjectValidationResult eObjectValidationResult = (EObjectValidationResult)theEObject;
				T result = caseEObjectValidationResult(eObjectValidationResult);
				if (result == null) result = caseValidationResult(eObjectValidationResult);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case COCLPackage.DERIVED_VALIDATION_REQUEST: {
				DerivedValidationRequest derivedValidationRequest = (DerivedValidationRequest)theEObject;
				T result = caseDerivedValidationRequest(derivedValidationRequest);
				if (result == null) result = caseValidationRequest(derivedValidationRequest);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case COCLPackage.OPERATION_VALIDATION_REQUEST: {
				OperationValidationRequest operationValidationRequest = (OperationValidationRequest)theEObject;
				T result = caseOperationValidationRequest(operationValidationRequest);
				if (result == null) result = caseValidationRequest(operationValidationRequest);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case COCLPackage.OPERATION_REQUEST_PARAMETER: {
				OperationRequestParameter operationRequestParameter = (OperationRequestParameter)theEObject;
				T result = caseOperationRequestParameter(operationRequestParameter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case COCLPackage.DIAGNOSTIC: {
				Diagnostic diagnostic = (Diagnostic)theEObject;
				T result = caseDiagnostic(diagnostic);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Ocl Constraint</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Ocl Constraint</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOclConstraint(OclConstraint object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Ocl Constraint Set</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Ocl Constraint Set</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOclConstraintSet(OclConstraintSet object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Validation Request</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Validation Request</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseValidationRequest(ValidationRequest object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Validation Response</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Validation Response</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseValidationResponse(ValidationResponse object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Validation Result</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Validation Result</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseValidationResult(ValidationResult object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Simple Validation Result</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Simple Validation Result</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSimpleValidationResult(SimpleValidationResult object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject Validation Result</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject Validation Result</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseEObjectValidationResult(EObjectValidationResult object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Derived Validation Request</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Derived Validation Request</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDerivedValidationRequest(DerivedValidationRequest object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Operation Validation Request</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Operation Validation Request</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOperationValidationRequest(OperationValidationRequest object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Operation Request Parameter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Operation Request Parameter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOperationRequestParameter(OperationRequestParameter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Diagnostic</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Diagnostic</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseDiagnostic(Diagnostic object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //COCLSwitch
