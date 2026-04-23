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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLFactory;
import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage;
import org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic;
import org.eclipse.fennec.model.atlas.validation.model.cocl.EObjectValidationResult;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Severity;
import org.eclipse.fennec.model.atlas.validation.model.cocl.SimpleValidationResult;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse;
import org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResult;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class COCLPackageImpl extends EPackageImpl implements COCLPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oclConstraintEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oclConstraintSetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass validationRequestEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass validationResponseEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass validationResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass simpleValidationResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eObjectValidationResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass derivedValidationRequestEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass operationValidationRequestEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass operationRequestParameterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass diagnosticEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum severityEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum oclRoleEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private COCLPackageImpl() {
		super(eNS_URI, COCLFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link COCLPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static COCLPackage init() {
		if (isInited) return (COCLPackage)EPackage.Registry.INSTANCE.getEPackage(COCLPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredCOCLPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		COCLPackageImpl theCOCLPackage = registeredCOCLPackage instanceof COCLPackageImpl ? (COCLPackageImpl)registeredCOCLPackage : new COCLPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		EcorePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theCOCLPackage.createPackageContents();

		// Initialize created meta-data
		theCOCLPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theCOCLPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(COCLPackage.eNS_URI, theCOCLPackage);
		return theCOCLPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOclConstraint() {
		return oclConstraintEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Name() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Description() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Expression() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Severity() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Role() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_ContextClass() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_FeatureName() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Active() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_Overrides() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraint_TargetURIs() {
		return (EAttribute)oclConstraintEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOclConstraintSet() {
		return oclConstraintSetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraintSet_Name() {
		return (EAttribute)oclConstraintSetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraintSet_Version() {
		return (EAttribute)oclConstraintSetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraintSet_Description() {
		return (EAttribute)oclConstraintSetEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getOclConstraintSet_Constraints() {
		return (EReference)oclConstraintSetEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOclConstraintSet_TargetModelNsURIs() {
		return (EAttribute)oclConstraintSetEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getValidationRequest() {
		return validationRequestEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getValidationRequest_Role() {
		return (EAttribute)validationRequestEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getValidationRequest_ValidationObjects() {
		return (EReference)validationRequestEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getValidationRequest_CoclId() {
		return (EAttribute)validationRequestEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getValidationResponse() {
		return validationResponseEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getValidationResponse_Results() {
		return (EReference)validationResponseEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getValidationResponse_Role() {
		return (EAttribute)validationResponseEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getValidationResponse_Diagnostics() {
		return (EReference)validationResponseEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getValidationResult() {
		return validationResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getValidationResult_Diagnostics() {
		return (EReference)validationResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSimpleValidationResult() {
		return simpleValidationResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSimpleValidationResult_Value() {
		return (EAttribute)simpleValidationResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEObjectValidationResult() {
		return eObjectValidationResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEObjectValidationResult_Values() {
		return (EReference)eObjectValidationResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDerivedValidationRequest() {
		return derivedValidationRequestEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDerivedValidationRequest_DerivedFeature() {
		return (EReference)derivedValidationRequestEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOperationValidationRequest() {
		return operationValidationRequestEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getOperationValidationRequest_Operation() {
		return (EReference)operationValidationRequestEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getOperationValidationRequest_Parameters() {
		return (EReference)operationValidationRequestEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOperationRequestParameter() {
		return operationRequestParameterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getOperationRequestParameter_Parameter() {
		return (EReference)operationRequestParameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOperationRequestParameter_IsNull() {
		return (EAttribute)operationRequestParameterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOperationRequestParameter_JavaValue() {
		return (EAttribute)operationRequestParameterEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getOperationRequestParameter_EValue() {
		return (EReference)operationRequestParameterEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDiagnostic() {
		return diagnosticEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Message() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Source() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Type() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getDiagnostic_Children() {
		return (EReference)diagnosticEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_ExceptionMsg() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDiagnostic_Data() {
		return (EAttribute)diagnosticEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getSeverity() {
		return severityEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getOclRole() {
		return oclRoleEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public COCLFactory getCOCLFactory() {
		return (COCLFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		oclConstraintEClass = createEClass(OCL_CONSTRAINT);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__NAME);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__DESCRIPTION);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__EXPRESSION);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__SEVERITY);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__ROLE);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__CONTEXT_CLASS);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__FEATURE_NAME);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__ACTIVE);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__OVERRIDES);
		createEAttribute(oclConstraintEClass, OCL_CONSTRAINT__TARGET_UR_IS);

		oclConstraintSetEClass = createEClass(OCL_CONSTRAINT_SET);
		createEAttribute(oclConstraintSetEClass, OCL_CONSTRAINT_SET__NAME);
		createEAttribute(oclConstraintSetEClass, OCL_CONSTRAINT_SET__VERSION);
		createEAttribute(oclConstraintSetEClass, OCL_CONSTRAINT_SET__DESCRIPTION);
		createEReference(oclConstraintSetEClass, OCL_CONSTRAINT_SET__CONSTRAINTS);
		createEAttribute(oclConstraintSetEClass, OCL_CONSTRAINT_SET__TARGET_MODEL_NS_UR_IS);

		validationRequestEClass = createEClass(VALIDATION_REQUEST);
		createEAttribute(validationRequestEClass, VALIDATION_REQUEST__ROLE);
		createEReference(validationRequestEClass, VALIDATION_REQUEST__VALIDATION_OBJECTS);
		createEAttribute(validationRequestEClass, VALIDATION_REQUEST__COCL_ID);

		validationResponseEClass = createEClass(VALIDATION_RESPONSE);
		createEReference(validationResponseEClass, VALIDATION_RESPONSE__RESULTS);
		createEAttribute(validationResponseEClass, VALIDATION_RESPONSE__ROLE);
		createEReference(validationResponseEClass, VALIDATION_RESPONSE__DIAGNOSTICS);

		validationResultEClass = createEClass(VALIDATION_RESULT);
		createEReference(validationResultEClass, VALIDATION_RESULT__DIAGNOSTICS);

		simpleValidationResultEClass = createEClass(SIMPLE_VALIDATION_RESULT);
		createEAttribute(simpleValidationResultEClass, SIMPLE_VALIDATION_RESULT__VALUE);

		eObjectValidationResultEClass = createEClass(EOBJECT_VALIDATION_RESULT);
		createEReference(eObjectValidationResultEClass, EOBJECT_VALIDATION_RESULT__VALUES);

		derivedValidationRequestEClass = createEClass(DERIVED_VALIDATION_REQUEST);
		createEReference(derivedValidationRequestEClass, DERIVED_VALIDATION_REQUEST__DERIVED_FEATURE);

		operationValidationRequestEClass = createEClass(OPERATION_VALIDATION_REQUEST);
		createEReference(operationValidationRequestEClass, OPERATION_VALIDATION_REQUEST__OPERATION);
		createEReference(operationValidationRequestEClass, OPERATION_VALIDATION_REQUEST__PARAMETERS);

		operationRequestParameterEClass = createEClass(OPERATION_REQUEST_PARAMETER);
		createEReference(operationRequestParameterEClass, OPERATION_REQUEST_PARAMETER__PARAMETER);
		createEAttribute(operationRequestParameterEClass, OPERATION_REQUEST_PARAMETER__IS_NULL);
		createEAttribute(operationRequestParameterEClass, OPERATION_REQUEST_PARAMETER__JAVA_VALUE);
		createEReference(operationRequestParameterEClass, OPERATION_REQUEST_PARAMETER__EVALUE);

		diagnosticEClass = createEClass(DIAGNOSTIC);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__MESSAGE);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__SOURCE);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__TYPE);
		createEReference(diagnosticEClass, DIAGNOSTIC__CHILDREN);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__EXCEPTION_MSG);
		createEAttribute(diagnosticEClass, DIAGNOSTIC__DATA);

		// Create enums
		severityEEnum = createEEnum(SEVERITY);
		oclRoleEEnum = createEEnum(OCL_ROLE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		EcorePackage theEcorePackage = (EcorePackage)EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		simpleValidationResultEClass.getESuperTypes().add(this.getValidationResult());
		eObjectValidationResultEClass.getESuperTypes().add(this.getValidationResult());
		derivedValidationRequestEClass.getESuperTypes().add(this.getValidationRequest());
		operationValidationRequestEClass.getESuperTypes().add(this.getValidationRequest());

		// Initialize classes, features, and operations; add parameters
		initEClass(oclConstraintEClass, OclConstraint.class, "OclConstraint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getOclConstraint_Name(), ecorePackage.getEString(), "name", null, 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Description(), ecorePackage.getEString(), "description", null, 0, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Expression(), ecorePackage.getEString(), "expression", null, 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Severity(), this.getSeverity(), "severity", "ERROR", 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Role(), this.getOclRole(), "role", "VALIDATION", 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_ContextClass(), ecorePackage.getEString(), "contextClass", null, 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_FeatureName(), ecorePackage.getEString(), "featureName", null, 0, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Active(), ecorePackage.getEBoolean(), "active", "true", 1, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_Overrides(), ecorePackage.getEBoolean(), "overrides", "false", 0, 1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraint_TargetURIs(), ecorePackage.getEString(), "targetURIs", null, 0, -1, OclConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oclConstraintSetEClass, OclConstraintSet.class, "OclConstraintSet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getOclConstraintSet_Name(), ecorePackage.getEString(), "name", null, 1, 1, OclConstraintSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraintSet_Version(), ecorePackage.getEString(), "version", "1.0", 0, 1, OclConstraintSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOclConstraintSet_Description(), ecorePackage.getEString(), "description", null, 0, 1, OclConstraintSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getOclConstraintSet_Constraints(), this.getOclConstraint(), null, "constraints", null, 0, -1, OclConstraintSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getOclConstraintSet_Constraints().getEKeys().add(this.getOclConstraint_Name());
		initEAttribute(getOclConstraintSet_TargetModelNsURIs(), ecorePackage.getEString(), "targetModelNsURIs", null, 0, -1, OclConstraintSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(validationRequestEClass, ValidationRequest.class, "ValidationRequest", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getValidationRequest_Role(), this.getOclRole(), "role", null, 0, 1, ValidationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getValidationRequest_ValidationObjects(), theEcorePackage.getEObject(), null, "validationObjects", null, 0, -1, ValidationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getValidationRequest_CoclId(), theEcorePackage.getEString(), "coclId", null, 0, 1, ValidationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(validationResponseEClass, ValidationResponse.class, "ValidationResponse", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getValidationResponse_Results(), this.getValidationResult(), null, "results", null, 0, -1, ValidationResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getValidationResponse_Role(), this.getOclRole(), "role", null, 0, 1, ValidationResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getValidationResponse_Diagnostics(), this.getDiagnostic(), null, "diagnostics", null, 0, -1, ValidationResponse.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(validationResultEClass, ValidationResult.class, "ValidationResult", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getValidationResult_Diagnostics(), this.getDiagnostic(), null, "diagnostics", null, 0, -1, ValidationResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(simpleValidationResultEClass, SimpleValidationResult.class, "SimpleValidationResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSimpleValidationResult_Value(), ecorePackage.getEJavaObject(), "value", null, 0, 1, SimpleValidationResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eObjectValidationResultEClass, EObjectValidationResult.class, "EObjectValidationResult", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getEObjectValidationResult_Values(), theEcorePackage.getEObject(), null, "values", null, 0, -1, EObjectValidationResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(derivedValidationRequestEClass, DerivedValidationRequest.class, "DerivedValidationRequest", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getDerivedValidationRequest_DerivedFeature(), theEcorePackage.getEStructuralFeature(), null, "derivedFeature", null, 1, -1, DerivedValidationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(operationValidationRequestEClass, OperationValidationRequest.class, "OperationValidationRequest", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getOperationValidationRequest_Operation(), theEcorePackage.getEOperation(), null, "operation", null, 1, 1, OperationValidationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getOperationValidationRequest_Parameters(), this.getOperationRequestParameter(), null, "parameters", null, 0, -1, OperationValidationRequest.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(operationRequestParameterEClass, OperationRequestParameter.class, "OperationRequestParameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getOperationRequestParameter_Parameter(), theEcorePackage.getEParameter(), null, "parameter", null, 1, 1, OperationRequestParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOperationRequestParameter_IsNull(), ecorePackage.getEBoolean(), "isNull", null, 0, 1, OperationRequestParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOperationRequestParameter_JavaValue(), ecorePackage.getEString(), "javaValue", null, 0, 1, OperationRequestParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getOperationRequestParameter_EValue(), ecorePackage.getEObject(), null, "eValue", null, 0, 1, OperationRequestParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(diagnosticEClass, Diagnostic.class, "Diagnostic", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDiagnostic_Message(), ecorePackage.getEString(), "message", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Source(), ecorePackage.getEString(), "source", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Type(), this.getSeverity(), "type", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getDiagnostic_Children(), this.getDiagnostic(), null, "children", null, 0, -1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_ExceptionMsg(), ecorePackage.getEString(), "exceptionMsg", null, 0, 1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDiagnostic_Data(), ecorePackage.getEString(), "data", null, 0, -1, Diagnostic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(severityEEnum, Severity.class, "Severity");
		addEEnumLiteral(severityEEnum, Severity.TRACE);
		addEEnumLiteral(severityEEnum, Severity.INFO);
		addEEnumLiteral(severityEEnum, Severity.WARN);
		addEEnumLiteral(severityEEnum, Severity.ERROR);
		addEEnumLiteral(severityEEnum, Severity.FATAL);

		initEEnum(oclRoleEEnum, OclRole.class, "OclRole");
		addEEnumLiteral(oclRoleEEnum, OclRole.VALIDATION);
		addEEnumLiteral(oclRoleEEnum, OclRole.DERIVED);
		addEEnumLiteral(oclRoleEEnum, OclRole.REFERENCE_FILTER);
		addEEnumLiteral(oclRoleEEnum, OclRole.OPERATION);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/GenModel</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createGenModelAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/GenModel";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "documentation", "Custom OCL (C-OCL) Metamodell zur Definition von projektspezifischen OCL-Constraints, die unabhaengig vom Ecore-Modell verwaltet werden koennen.",
			   "oSGiCompatible", "true",
			   "basePackage", "org.eclipse.fennec.model.atlas.validation.model",
			   "complianceLevel", "21.0",
			   "copyrightText", "Copyright (c) 2026 Contributors to the Eclipse Foundation.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n  Data In Motion Consulting - initial implementation",
			   "resource", "XMI",
			   "fileExtensions", "cocl"
		   });
		addAnnotation
		  (severityEEnum,
		   source,
		   new String[] {
			   "documentation", "Schweregrad einer OCL-Constraint-Verletzung. Bestimmt die Darstellung in der Log-Tabelle und das Verhalten bei Validierungsfehlern."
		   });
		addAnnotation
		  (severityEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Rein diagnostisch, nicht standardmaessig sichtbar. Fuer Entwickler und Debugging-Zwecke."
		   });
		addAnnotation
		  (severityEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "Hinweis ohne Handlungsbedarf. Informiert den Nutzer ueber einen Zustand."
		   });
		addAnnotation
		  (severityEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Potenzielles Problem, das geprueft werden sollte. Blockiert keine Aktionen."
		   });
		addAnnotation
		  (severityEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Fachlich ungueltiger Zustand. Die Instanz entspricht nicht den definierten Regeln."
		   });
		addAnnotation
		  (severityEEnum.getELiterals().get(4),
		   source,
		   new String[] {
			   "documentation", "Kritischer Zustand. Weitere Verarbeitung sollte abgebrochen werden."
		   });
		addAnnotation
		  (oclRoleEEnum,
		   source,
		   new String[] {
			   "documentation", "Definiert die Rolle/Verwendung eines OCL-Ausdrucks im System."
		   });
		addAnnotation
		  (oclRoleEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "OCL wird zur Validierung von Instanzen verwendet."
		   });
		addAnnotation
		  (oclRoleEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "OCL wird zur Berechnung von Derived Values verwendet."
		   });
		addAnnotation
		  (oclRoleEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "OCL wird zur Filterung von Referenzzielen verwendet."
		   });
		addAnnotation
		  (oclRoleEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "OCL to execute an operation."
		   });
		addAnnotation
		  (oclConstraintEClass,
		   source,
		   new String[] {
			   "documentation", "Zentrale Klasse zur Definition eines OCL-Constraints. Enthaelt den OCL-Ausdruck, Metadaten und optionale Einschraenkungen auf Zielobjekte."
		   });
		addAnnotation
		  (getOclConstraint_Name(),
		   source,
		   new String[] {
			   "documentation", "Eindeutiger, sprechender Name des Constraints. Wird in der Log-Tabelle und in Fehlermeldungen angezeigt."
		   });
		addAnnotation
		  (getOclConstraint_Description(),
		   source,
		   new String[] {
			   "documentation", "Fachliche Beschreibung des Constraints. Erklaert den Zweck und die Auswirkungen bei Verletzung."
		   });
		addAnnotation
		  (getOclConstraint_Expression(),
		   source,
		   new String[] {
			   "documentation", "Der OCL-Ausdruck selbst. Muss syntaktisch korrekt sein und zum Kontexttyp passen."
		   });
		addAnnotation
		  (getOclConstraint_Severity(),
		   source,
		   new String[] {
			   "documentation", "Schweregrad bei Constraint-Verletzung. Bestimmt Darstellung und Verhalten."
		   });
		addAnnotation
		  (getOclConstraint_Role(),
		   source,
		   new String[] {
			   "documentation", "Definiert die Verwendung des OCL-Ausdrucks (Validierung, Derived, Filter)."
		   });
		addAnnotation
		  (getOclConstraint_ContextClass(),
		   source,
		   new String[] {
			   "documentation", "Vollqualifizierter Name der EClass, auf die sich dieser Constraint bezieht. Format: EClass URI"
		   });
		addAnnotation
		  (getOclConstraint_FeatureName(),
		   source,
		   new String[] {
			   "documentation", "Optional: Name des Features (Attribut/Referenz), auf das sich der Constraint bezieht. Relevant fuer DERIVED und REFERENCE_FILTER Rollen."
		   });
		addAnnotation
		  (getOclConstraint_Active(),
		   source,
		   new String[] {
			   "documentation", "Gibt an, ob der Constraint aktiv ist. Inaktive Constraints werden bei der Validierung uebersprungen."
		   });
		addAnnotation
		  (getOclConstraint_Overrides(),
		   source,
		   new String[] {
			   "documentation", "Wenn true, ueberschreibt dieser Constraint einen gleichnamigen Constraint aus einer Quelle mit niedrigerer Prioritaet (z.B. Ecore)."
		   });
		addAnnotation
		  (getOclConstraint_TargetURIs(),
		   source,
		   new String[] {
			   "documentation", "Optionale Liste von URIs konkreter EObjects, auf die sich der Constraint beschraenkt. Wenn leer, gilt der Constraint fuer alle Instanzen der contextClass."
		   });
		addAnnotation
		  (oclConstraintSetEClass,
		   source,
		   new String[] {
			   "documentation", "Container fuer eine Sammlung von OCL-Constraints. Entspricht einer *.c-ocl Datei."
		   });
		addAnnotation
		  (getOclConstraintSet_Name(),
		   source,
		   new String[] {
			   "documentation", "Name des Constraint-Sets. Wird zur Identifikation und im Log verwendet."
		   });
		addAnnotation
		  (getOclConstraintSet_Version(),
		   source,
		   new String[] {
			   "documentation", "Versionsnummer des Constraint-Sets fuer Kompatibilitaetspruefungen."
		   });
		addAnnotation
		  (getOclConstraintSet_Description(),
		   source,
		   new String[] {
			   "documentation", "Optionale Beschreibung des Constraint-Sets."
		   });
		addAnnotation
		  (getOclConstraintSet_Constraints(),
		   source,
		   new String[] {
			   "documentation", "Liste der enthaltenen OCL-Constraints."
		   });
		addAnnotation
		  (getOclConstraintSet_TargetModelNsURIs(),
		   source,
		   new String[] {
			   "documentation", "Liste von Namespace URIs der Zielmodelle, fuer die dieses Constraint-Set gilt."
		   });
		addAnnotation
		  (validationRequestEClass,
		   source,
		   new String[] {
			   "documentation", "Abstract validation request object"
		   });
		addAnnotation
		  (getValidationRequest_ValidationObjects(),
		   source,
		   new String[] {
			   "documentation", "Objects to be validated. Muli-valued for batch request"
		   });
		addAnnotation
		  (getValidationRequest_CoclId(),
		   source,
		   new String[] {
			   "documentation", "The id of the ocol object, which is usually the coc constraint set name"
		   });
		addAnnotation
		  (validationResponseEClass,
		   source,
		   new String[] {
			   "documentation", "Validation response object, that i returned for all ValidationRequests"
		   });
		addAnnotation
		  (getValidationResponse_Results(),
		   source,
		   new String[] {
			   "documentation", "The resulting objects"
		   });
		addAnnotation
		  (getValidationResponse_Diagnostics(),
		   source,
		   new String[] {
			   "documentation", "List of diagnostics to communicate"
		   });
		addAnnotation
		  (validationResultEClass,
		   source,
		   new String[] {
			   "documentation", "Abstract validation result"
		   });
		addAnnotation
		  (getValidationResult_Diagnostics(),
		   source,
		   new String[] {
			   "documentation", "List of diagnostics to communicate"
		   });
		addAnnotation
		  (simpleValidationResultEClass,
		   source,
		   new String[] {
			   "documentation", "Validation result for simple java types"
		   });
		addAnnotation
		  (getSimpleValidationResult_Value(),
		   source,
		   new String[] {
			   "documentation", "The simple value to be returned"
		   });
		addAnnotation
		  (eObjectValidationResultEClass,
		   source,
		   new String[] {
			   "documentation", "Validation result for EMF EObjects"
		   });
		addAnnotation
		  (getEObjectValidationResult_Values(),
		   source,
		   new String[] {
			   "documentation", "The EObjects to be returned"
		   });
		addAnnotation
		  (derivedValidationRequestEClass,
		   source,
		   new String[] {
			   "documentation", "Validation request for derived field validation"
		   });
		addAnnotation
		  (getDerivedValidationRequest_DerivedFeature(),
		   source,
		   new String[] {
			   "documentation", "EstructuralFeature for the derived field to be validated"
		   });
		addAnnotation
		  (operationValidationRequestEClass,
		   source,
		   new String[] {
			   "documentation", "Validation request for operation validation"
		   });
		addAnnotation
		  (getOperationValidationRequest_Operation(),
		   source,
		   new String[] {
			   "documentation", "Operation to be executed"
		   });
		addAnnotation
		  (operationRequestParameterEClass,
		   source,
		   new String[] {
			   "documentation", "Operation request parameter object"
		   });
		addAnnotation
		  (getOperationRequestParameter_JavaValue(),
		   source,
		   new String[] {
			   "documentation", "We put the String representation here to not have issues via REST. Then we can construct the value back from the parameter type."
		   });
	}

} //COCLPackageImpl
