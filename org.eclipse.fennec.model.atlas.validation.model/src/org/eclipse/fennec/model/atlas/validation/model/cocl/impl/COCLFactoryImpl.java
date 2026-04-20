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
package org.eclipse.fennec.model.atlas.validation.model.cocl.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.model.atlas.validation.model.cocl.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class COCLFactoryImpl extends EFactoryImpl implements COCLFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static COCLFactory init() {
		try {
			COCLFactory theCOCLFactory = (COCLFactory)EPackage.Registry.INSTANCE.getEFactory(COCLPackage.eNS_URI);
			if (theCOCLFactory != null) {
				return theCOCLFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new COCLFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public COCLFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case COCLPackage.OCL_CONSTRAINT: return createOclConstraint();
			case COCLPackage.OCL_CONSTRAINT_SET: return createOclConstraintSet();
			case COCLPackage.VALIDATION_RESPONSE: return createValidationResponse();
			case COCLPackage.SIMPLE_VALIDATION_RESULT: return createSimpleValidationResult();
			case COCLPackage.EOBJECT_VALIDATION_RESULT: return createEObjectValidationResult();
			case COCLPackage.DERIVED_VALIDATION_REQUEST: return createDerivedValidationRequest();
			case COCLPackage.OPERATION_VALIDATION_REQUEST: return createOperationValidationRequest();
			case COCLPackage.OPERATION_REQUEST_PARAMETER: return createOperationRequestParameter();
			case COCLPackage.DIAGNOSTIC: return createDiagnostic();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case COCLPackage.SEVERITY:
				return createSeverityFromString(eDataType, initialValue);
			case COCLPackage.OCL_ROLE:
				return createOclRoleFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case COCLPackage.SEVERITY:
				return convertSeverityToString(eDataType, instanceValue);
			case COCLPackage.OCL_ROLE:
				return convertOclRoleToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OclConstraint createOclConstraint() {
		OclConstraintImpl oclConstraint = new OclConstraintImpl();
		return oclConstraint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OclConstraintSet createOclConstraintSet() {
		OclConstraintSetImpl oclConstraintSet = new OclConstraintSetImpl();
		return oclConstraintSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ValidationResponse createValidationResponse() {
		ValidationResponseImpl validationResponse = new ValidationResponseImpl();
		return validationResponse;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SimpleValidationResult createSimpleValidationResult() {
		SimpleValidationResultImpl simpleValidationResult = new SimpleValidationResultImpl();
		return simpleValidationResult;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObjectValidationResult createEObjectValidationResult() {
		EObjectValidationResultImpl eObjectValidationResult = new EObjectValidationResultImpl();
		return eObjectValidationResult;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DerivedValidationRequest createDerivedValidationRequest() {
		DerivedValidationRequestImpl derivedValidationRequest = new DerivedValidationRequestImpl();
		return derivedValidationRequest;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OperationValidationRequest createOperationValidationRequest() {
		OperationValidationRequestImpl operationValidationRequest = new OperationValidationRequestImpl();
		return operationValidationRequest;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OperationRequestParameter createOperationRequestParameter() {
		OperationRequestParameterImpl operationRequestParameter = new OperationRequestParameterImpl();
		return operationRequestParameter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Diagnostic createDiagnostic() {
		DiagnosticImpl diagnostic = new DiagnosticImpl();
		return diagnostic;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Severity createSeverityFromString(EDataType eDataType, String initialValue) {
		Severity result = Severity.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertSeverityToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OclRole createOclRoleFromString(EDataType eDataType, String initialValue) {
		OclRole result = OclRole.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertOclRoleToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public COCLPackage getCOCLPackage() {
		return (COCLPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static COCLPackage getPackage() {
		return COCLPackage.eINSTANCE;
	}

} //COCLFactoryImpl
