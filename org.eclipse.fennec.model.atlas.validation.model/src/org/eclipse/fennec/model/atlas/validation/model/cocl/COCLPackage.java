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


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Custom OCL (C-OCL) Metamodell zur Definition von projektspezifischen OCL-Constraints, die unabhaengig vom Ecore-Modell verwaltet werden koennen.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLFactory
 * @model kind="package"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel oSGiCompatible='true' basePackage='org.eclipse.fennec.model.atlas.validation.model' complianceLevel='21.0' copyrightText='Copyright (c) 2026 Contributors to the Eclipse Foundation.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n  Data In Motion Consulting - initial implementation' resource='XMI' fileExtensions='cocl'"
 * @generated
 */
@ProviderType
@EPackage(uri = COCLPackage.eNS_URI, fingerprint = "fp1:3349b64043aa15b20d6922e8f2c981d35c98c1e7ef66794a59bbf2d5a81082f3", genModel = "/model/cocl.genmodel", genModelSourceLocations = {"model/cocl.genmodel","org.eclipse.fennec.model.atlas.validation.model/model/cocl.genmodel"}, ecore = "/model/cocl.ecore", ecoreSourceLocations = "/model/cocl.ecore")
public interface COCLPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "cocl";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://www.gme.org/cocl/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "cocl";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	COCLPackage eINSTANCE = org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl <em>Ocl Constraint</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOclConstraint()
	 * @generated
	 */
	int OCL_CONSTRAINT = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__NAME = 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__DESCRIPTION = 1;

	/**
	 * The feature id for the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__EXPRESSION = 2;

	/**
	 * The feature id for the '<em><b>Severity</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__SEVERITY = 3;

	/**
	 * The feature id for the '<em><b>Role</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__ROLE = 4;

	/**
	 * The feature id for the '<em><b>Context Class</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__CONTEXT_CLASS = 5;

	/**
	 * The feature id for the '<em><b>Feature Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__FEATURE_NAME = 6;

	/**
	 * The feature id for the '<em><b>Active</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__ACTIVE = 7;

	/**
	 * The feature id for the '<em><b>Overrides</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__OVERRIDES = 8;

	/**
	 * The feature id for the '<em><b>Target UR Is</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__TARGET_UR_IS = 9;

	/**
	 * The feature id for the '<em><b>Operation Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__OPERATION_NAME = 10;

	/**
	 * The feature id for the '<em><b>Operation Parameter Names</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__OPERATION_PARAMETER_NAMES = 11;

	/**
	 * The feature id for the '<em><b>Operation Return Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT__OPERATION_RETURN_TYPE = 12;

	/**
	 * The number of structural features of the '<em>Ocl Constraint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT_FEATURE_COUNT = 13;

	/**
	 * The number of operations of the '<em>Ocl Constraint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintSetImpl <em>Ocl Constraint Set</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintSetImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOclConstraintSet()
	 * @generated
	 */
	int OCL_CONSTRAINT_SET = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT_SET__NAME = 0;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT_SET__VERSION = 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT_SET__DESCRIPTION = 2;

	/**
	 * The feature id for the '<em><b>Constraints</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT_SET__CONSTRAINTS = 3;

	/**
	 * The feature id for the '<em><b>Target Model Ns UR Is</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT_SET__TARGET_MODEL_NS_UR_IS = 4;

	/**
	 * The number of structural features of the '<em>Ocl Constraint Set</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT_SET_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Ocl Constraint Set</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT_SET_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationRequestImpl <em>Validation Request</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationRequestImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getValidationRequest()
	 * @generated
	 */
	int VALIDATION_REQUEST = 2;

	/**
	 * The feature id for the '<em><b>Role</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_REQUEST__ROLE = 0;

	/**
	 * The feature id for the '<em><b>Validation Objects</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_REQUEST__VALIDATION_OBJECTS = 1;

	/**
	 * The feature id for the '<em><b>Cocl Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_REQUEST__COCL_ID = 2;

	/**
	 * The number of structural features of the '<em>Validation Request</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_REQUEST_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Validation Request</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_REQUEST_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationResponseImpl <em>Validation Response</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationResponseImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getValidationResponse()
	 * @generated
	 */
	int VALIDATION_RESPONSE = 3;

	/**
	 * The feature id for the '<em><b>Results</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESPONSE__RESULTS = 0;

	/**
	 * The feature id for the '<em><b>Role</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESPONSE__ROLE = 1;

	/**
	 * The feature id for the '<em><b>Diagnostics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESPONSE__DIAGNOSTICS = 2;

	/**
	 * The number of structural features of the '<em>Validation Response</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESPONSE_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Validation Response</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESPONSE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationResultImpl <em>Validation Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationResultImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getValidationResult()
	 * @generated
	 */
	int VALIDATION_RESULT = 4;

	/**
	 * The feature id for the '<em><b>Diagnostics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT__DIAGNOSTICS = 0;

	/**
	 * The number of structural features of the '<em>Validation Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Validation Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int VALIDATION_RESULT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.SimpleValidationResultImpl <em>Simple Validation Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.SimpleValidationResultImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getSimpleValidationResult()
	 * @generated
	 */
	int SIMPLE_VALIDATION_RESULT = 5;

	/**
	 * The feature id for the '<em><b>Diagnostics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_VALIDATION_RESULT__DIAGNOSTICS = VALIDATION_RESULT__DIAGNOSTICS;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_VALIDATION_RESULT__VALUE = VALIDATION_RESULT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Value Java Class Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_VALIDATION_RESULT__VALUE_JAVA_CLASS_NAME = VALIDATION_RESULT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Simple Validation Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_VALIDATION_RESULT_FEATURE_COUNT = VALIDATION_RESULT_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Simple Validation Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SIMPLE_VALIDATION_RESULT_OPERATION_COUNT = VALIDATION_RESULT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.EObjectValidationResultImpl <em>EObject Validation Result</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.EObjectValidationResultImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getEObjectValidationResult()
	 * @generated
	 */
	int EOBJECT_VALIDATION_RESULT = 6;

	/**
	 * The feature id for the '<em><b>Diagnostics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_VALIDATION_RESULT__DIAGNOSTICS = VALIDATION_RESULT__DIAGNOSTICS;

	/**
	 * The feature id for the '<em><b>Values</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_VALIDATION_RESULT__VALUES = VALIDATION_RESULT_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>EObject Validation Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_VALIDATION_RESULT_FEATURE_COUNT = VALIDATION_RESULT_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>EObject Validation Result</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_VALIDATION_RESULT_OPERATION_COUNT = VALIDATION_RESULT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.DerivedValidationRequestImpl <em>Derived Validation Request</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.DerivedValidationRequestImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getDerivedValidationRequest()
	 * @generated
	 */
	int DERIVED_VALIDATION_REQUEST = 7;

	/**
	 * The feature id for the '<em><b>Role</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DERIVED_VALIDATION_REQUEST__ROLE = VALIDATION_REQUEST__ROLE;

	/**
	 * The feature id for the '<em><b>Validation Objects</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DERIVED_VALIDATION_REQUEST__VALIDATION_OBJECTS = VALIDATION_REQUEST__VALIDATION_OBJECTS;

	/**
	 * The feature id for the '<em><b>Cocl Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DERIVED_VALIDATION_REQUEST__COCL_ID = VALIDATION_REQUEST__COCL_ID;

	/**
	 * The feature id for the '<em><b>Derived Feature</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DERIVED_VALIDATION_REQUEST__DERIVED_FEATURE = VALIDATION_REQUEST_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Derived Validation Request</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DERIVED_VALIDATION_REQUEST_FEATURE_COUNT = VALIDATION_REQUEST_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Derived Validation Request</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DERIVED_VALIDATION_REQUEST_OPERATION_COUNT = VALIDATION_REQUEST_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationValidationRequestImpl <em>Operation Validation Request</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationValidationRequestImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOperationValidationRequest()
	 * @generated
	 */
	int OPERATION_VALIDATION_REQUEST = 8;

	/**
	 * The feature id for the '<em><b>Role</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_VALIDATION_REQUEST__ROLE = VALIDATION_REQUEST__ROLE;

	/**
	 * The feature id for the '<em><b>Validation Objects</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_VALIDATION_REQUEST__VALIDATION_OBJECTS = VALIDATION_REQUEST__VALIDATION_OBJECTS;

	/**
	 * The feature id for the '<em><b>Cocl Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_VALIDATION_REQUEST__COCL_ID = VALIDATION_REQUEST__COCL_ID;

	/**
	 * The feature id for the '<em><b>Operation</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_VALIDATION_REQUEST__OPERATION = VALIDATION_REQUEST_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_VALIDATION_REQUEST__PARAMETERS = VALIDATION_REQUEST_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Operation Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_VALIDATION_REQUEST__OPERATION_NAME = VALIDATION_REQUEST_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Operation Validation Request</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_VALIDATION_REQUEST_FEATURE_COUNT = VALIDATION_REQUEST_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Operation Validation Request</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_VALIDATION_REQUEST_OPERATION_COUNT = VALIDATION_REQUEST_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationRequestParameterImpl <em>Operation Request Parameter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationRequestParameterImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOperationRequestParameter()
	 * @generated
	 */
	int OPERATION_REQUEST_PARAMETER = 9;

	/**
	 * The feature id for the '<em><b>Parameter Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_REQUEST_PARAMETER__PARAMETER_NAME = 0;

	/**
	 * The feature id for the '<em><b>Parameter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_REQUEST_PARAMETER__PARAMETER = 1;

	/**
	 * The feature id for the '<em><b>Is Null</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_REQUEST_PARAMETER__IS_NULL = 2;

	/**
	 * The feature id for the '<em><b>Java Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_REQUEST_PARAMETER__JAVA_VALUE = 3;

	/**
	 * The feature id for the '<em><b>EValue</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_REQUEST_PARAMETER__EVALUE = 4;

	/**
	 * The number of structural features of the '<em>Operation Request Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_REQUEST_PARAMETER_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Operation Request Parameter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OPERATION_REQUEST_PARAMETER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.DiagnosticImpl <em>Diagnostic</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.DiagnosticImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getDiagnostic()
	 * @generated
	 */
	int DIAGNOSTIC = 10;

	/**
	 * The feature id for the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC__MESSAGE = 0;

	/**
	 * The feature id for the '<em><b>Source</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC__SOURCE = 1;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC__TYPE = 2;

	/**
	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC__CHILDREN = 3;

	/**
	 * The feature id for the '<em><b>Exception Msg</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC__EXCEPTION_MSG = 4;

	/**
	 * The feature id for the '<em><b>Data</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC__DATA = 5;

	/**
	 * The number of structural features of the '<em>Diagnostic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Diagnostic</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGNOSTIC_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.BatchValidationRequestImpl <em>Batch Validation Request</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.BatchValidationRequestImpl
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getBatchValidationRequest()
	 * @generated
	 */
	int BATCH_VALIDATION_REQUEST = 11;

	/**
	 * The feature id for the '<em><b>Role</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_VALIDATION_REQUEST__ROLE = VALIDATION_REQUEST__ROLE;

	/**
	 * The feature id for the '<em><b>Validation Objects</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_VALIDATION_REQUEST__VALIDATION_OBJECTS = VALIDATION_REQUEST__VALIDATION_OBJECTS;

	/**
	 * The feature id for the '<em><b>Cocl Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_VALIDATION_REQUEST__COCL_ID = VALIDATION_REQUEST__COCL_ID;

	/**
	 * The feature id for the '<em><b>Filter Constraint</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_VALIDATION_REQUEST__FILTER_CONSTRAINT = VALIDATION_REQUEST_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Batch Validation Request</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_VALIDATION_REQUEST_FEATURE_COUNT = VALIDATION_REQUEST_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Batch Validation Request</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BATCH_VALIDATION_REQUEST_OPERATION_COUNT = VALIDATION_REQUEST_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Severity <em>Severity</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Severity
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getSeverity()
	 * @generated
	 */
	int SEVERITY = 12;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole <em>Ocl Role</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOclRole()
	 * @generated
	 */
	int OCL_ROLE = 13;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationReturnType <em>Operation Return Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationReturnType
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOperationReturnType()
	 * @generated
	 */
	int OPERATION_RETURN_TYPE = 14;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint <em>Ocl Constraint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Ocl Constraint</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint
	 * @generated
	 */
	EClass getOclConstraint();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getName()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getDescription()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_Description();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getExpression <em>Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Expression</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getExpression()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_Expression();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getSeverity <em>Severity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Severity</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getSeverity()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_Severity();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getRole <em>Role</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Role</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getRole()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_Role();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getContextClass <em>Context Class</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Context Class</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getContextClass()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_ContextClass();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getFeatureName <em>Feature Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Feature Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getFeatureName()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_FeatureName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#isActive <em>Active</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Active</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#isActive()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_Active();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#isOverrides <em>Overrides</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Overrides</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#isOverrides()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_Overrides();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getTargetURIs <em>Target UR Is</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Target UR Is</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getTargetURIs()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_TargetURIs();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getOperationName <em>Operation Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Operation Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getOperationName()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_OperationName();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getOperationParameterNames <em>Operation Parameter Names</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Operation Parameter Names</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getOperationParameterNames()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_OperationParameterNames();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getOperationReturnType <em>Operation Return Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Operation Return Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getOperationReturnType()
	 * @see #getOclConstraint()
	 * @generated
	 */
	EAttribute getOclConstraint_OperationReturnType();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet <em>Ocl Constraint Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Ocl Constraint Set</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet
	 * @generated
	 */
	EClass getOclConstraintSet();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getName()
	 * @see #getOclConstraintSet()
	 * @generated
	 */
	EAttribute getOclConstraintSet_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getVersion()
	 * @see #getOclConstraintSet()
	 * @generated
	 */
	EAttribute getOclConstraintSet_Version();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getDescription()
	 * @see #getOclConstraintSet()
	 * @generated
	 */
	EAttribute getOclConstraintSet_Description();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getConstraints <em>Constraints</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Constraints</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getConstraints()
	 * @see #getOclConstraintSet()
	 * @generated
	 */
	EReference getOclConstraintSet_Constraints();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getTargetModelNsURIs <em>Target Model Ns UR Is</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Target Model Ns UR Is</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getTargetModelNsURIs()
	 * @see #getOclConstraintSet()
	 * @generated
	 */
	EAttribute getOclConstraintSet_TargetModelNsURIs();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest <em>Validation Request</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Validation Request</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest
	 * @generated
	 */
	EClass getValidationRequest();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest#getRole <em>Role</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Role</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest#getRole()
	 * @see #getValidationRequest()
	 * @generated
	 */
	EAttribute getValidationRequest_Role();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest#getValidationObjects <em>Validation Objects</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Validation Objects</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest#getValidationObjects()
	 * @see #getValidationRequest()
	 * @generated
	 */
	EReference getValidationRequest_ValidationObjects();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest#getCoclId <em>Cocl Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Cocl Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationRequest#getCoclId()
	 * @see #getValidationRequest()
	 * @generated
	 */
	EAttribute getValidationRequest_CoclId();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse <em>Validation Response</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Validation Response</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse
	 * @generated
	 */
	EClass getValidationResponse();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse#getResults <em>Results</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Results</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse#getResults()
	 * @see #getValidationResponse()
	 * @generated
	 */
	EReference getValidationResponse_Results();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse#getRole <em>Role</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Role</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse#getRole()
	 * @see #getValidationResponse()
	 * @generated
	 */
	EAttribute getValidationResponse_Role();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse#getDiagnostics <em>Diagnostics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Diagnostics</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResponse#getDiagnostics()
	 * @see #getValidationResponse()
	 * @generated
	 */
	EReference getValidationResponse_Diagnostics();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResult <em>Validation Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Validation Result</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResult
	 * @generated
	 */
	EClass getValidationResult();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResult#getDiagnostics <em>Diagnostics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Diagnostics</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.ValidationResult#getDiagnostics()
	 * @see #getValidationResult()
	 * @generated
	 */
	EReference getValidationResult_Diagnostics();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.SimpleValidationResult <em>Simple Validation Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Simple Validation Result</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.SimpleValidationResult
	 * @generated
	 */
	EClass getSimpleValidationResult();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.SimpleValidationResult#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.SimpleValidationResult#getValue()
	 * @see #getSimpleValidationResult()
	 * @generated
	 */
	EAttribute getSimpleValidationResult_Value();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.SimpleValidationResult#getValueJavaClassName <em>Value Java Class Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value Java Class Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.SimpleValidationResult#getValueJavaClassName()
	 * @see #getSimpleValidationResult()
	 * @generated
	 */
	EAttribute getSimpleValidationResult_ValueJavaClassName();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.EObjectValidationResult <em>EObject Validation Result</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EObject Validation Result</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.EObjectValidationResult
	 * @generated
	 */
	EClass getEObjectValidationResult();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.EObjectValidationResult#getValues <em>Values</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Values</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.EObjectValidationResult#getValues()
	 * @see #getEObjectValidationResult()
	 * @generated
	 */
	EReference getEObjectValidationResult_Values();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest <em>Derived Validation Request</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Derived Validation Request</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest
	 * @generated
	 */
	EClass getDerivedValidationRequest();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest#getDerivedFeature <em>Derived Feature</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Derived Feature</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest#getDerivedFeature()
	 * @see #getDerivedValidationRequest()
	 * @generated
	 */
	EReference getDerivedValidationRequest_DerivedFeature();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest <em>Operation Validation Request</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Operation Validation Request</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest
	 * @generated
	 */
	EClass getOperationValidationRequest();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest#getOperation <em>Operation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Operation</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest#getOperation()
	 * @see #getOperationValidationRequest()
	 * @generated
	 */
	EReference getOperationValidationRequest_Operation();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameters</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest#getParameters()
	 * @see #getOperationValidationRequest()
	 * @generated
	 */
	EReference getOperationValidationRequest_Parameters();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest#getOperationName <em>Operation Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Operation Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationValidationRequest#getOperationName()
	 * @see #getOperationValidationRequest()
	 * @generated
	 */
	EAttribute getOperationValidationRequest_OperationName();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter <em>Operation Request Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Operation Request Parameter</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter
	 * @generated
	 */
	EClass getOperationRequestParameter();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getParameterName <em>Parameter Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parameter Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getParameterName()
	 * @see #getOperationRequestParameter()
	 * @generated
	 */
	EAttribute getOperationRequestParameter_ParameterName();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getParameter <em>Parameter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Parameter</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getParameter()
	 * @see #getOperationRequestParameter()
	 * @generated
	 */
	EReference getOperationRequestParameter_Parameter();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#isIsNull <em>Is Null</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Is Null</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#isIsNull()
	 * @see #getOperationRequestParameter()
	 * @generated
	 */
	EAttribute getOperationRequestParameter_IsNull();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getJavaValue <em>Java Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Java Value</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getJavaValue()
	 * @see #getOperationRequestParameter()
	 * @generated
	 */
	EAttribute getOperationRequestParameter_JavaValue();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getEValue <em>EValue</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>EValue</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getEValue()
	 * @see #getOperationRequestParameter()
	 * @generated
	 */
	EReference getOperationRequestParameter_EValue();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic <em>Diagnostic</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Diagnostic</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic
	 * @generated
	 */
	EClass getDiagnostic();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getMessage <em>Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getMessage()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EAttribute getDiagnostic_Message();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getSource <em>Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Source</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getSource()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EAttribute getDiagnostic_Source();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getType()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EAttribute getDiagnostic_Type();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Children</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getChildren()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EReference getDiagnostic_Children();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getExceptionMsg <em>Exception Msg</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Exception Msg</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getExceptionMsg()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EAttribute getDiagnostic_ExceptionMsg();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getData <em>Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Data</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getData()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EAttribute getDiagnostic_Data();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest <em>Batch Validation Request</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Batch Validation Request</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest
	 * @generated
	 */
	EClass getBatchValidationRequest();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest#getFilterConstraint <em>Filter Constraint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Filter Constraint</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.BatchValidationRequest#getFilterConstraint()
	 * @see #getBatchValidationRequest()
	 * @generated
	 */
	EReference getBatchValidationRequest_FilterConstraint();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Severity <em>Severity</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Severity</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Severity
	 * @generated
	 */
	EEnum getSeverity();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole <em>Ocl Role</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Ocl Role</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole
	 * @generated
	 */
	EEnum getOclRole();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationReturnType <em>Operation Return Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Operation Return Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationReturnType
	 * @generated
	 */
	EEnum getOperationReturnType();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	COCLFactory getCOCLFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl <em>Ocl Constraint</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOclConstraint()
		 * @generated
		 */
		EClass OCL_CONSTRAINT = eINSTANCE.getOclConstraint();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__NAME = eINSTANCE.getOclConstraint_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__DESCRIPTION = eINSTANCE.getOclConstraint_Description();

		/**
		 * The meta object literal for the '<em><b>Expression</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__EXPRESSION = eINSTANCE.getOclConstraint_Expression();

		/**
		 * The meta object literal for the '<em><b>Severity</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__SEVERITY = eINSTANCE.getOclConstraint_Severity();

		/**
		 * The meta object literal for the '<em><b>Role</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__ROLE = eINSTANCE.getOclConstraint_Role();

		/**
		 * The meta object literal for the '<em><b>Context Class</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__CONTEXT_CLASS = eINSTANCE.getOclConstraint_ContextClass();

		/**
		 * The meta object literal for the '<em><b>Feature Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__FEATURE_NAME = eINSTANCE.getOclConstraint_FeatureName();

		/**
		 * The meta object literal for the '<em><b>Active</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__ACTIVE = eINSTANCE.getOclConstraint_Active();

		/**
		 * The meta object literal for the '<em><b>Overrides</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__OVERRIDES = eINSTANCE.getOclConstraint_Overrides();

		/**
		 * The meta object literal for the '<em><b>Target UR Is</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__TARGET_UR_IS = eINSTANCE.getOclConstraint_TargetURIs();

		/**
		 * The meta object literal for the '<em><b>Operation Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__OPERATION_NAME = eINSTANCE.getOclConstraint_OperationName();

		/**
		 * The meta object literal for the '<em><b>Operation Parameter Names</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__OPERATION_PARAMETER_NAMES = eINSTANCE.getOclConstraint_OperationParameterNames();

		/**
		 * The meta object literal for the '<em><b>Operation Return Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT__OPERATION_RETURN_TYPE = eINSTANCE.getOclConstraint_OperationReturnType();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintSetImpl <em>Ocl Constraint Set</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintSetImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOclConstraintSet()
		 * @generated
		 */
		EClass OCL_CONSTRAINT_SET = eINSTANCE.getOclConstraintSet();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT_SET__NAME = eINSTANCE.getOclConstraintSet_Name();

		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT_SET__VERSION = eINSTANCE.getOclConstraintSet_Version();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT_SET__DESCRIPTION = eINSTANCE.getOclConstraintSet_Description();

		/**
		 * The meta object literal for the '<em><b>Constraints</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OCL_CONSTRAINT_SET__CONSTRAINTS = eINSTANCE.getOclConstraintSet_Constraints();

		/**
		 * The meta object literal for the '<em><b>Target Model Ns UR Is</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OCL_CONSTRAINT_SET__TARGET_MODEL_NS_UR_IS = eINSTANCE.getOclConstraintSet_TargetModelNsURIs();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationRequestImpl <em>Validation Request</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationRequestImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getValidationRequest()
		 * @generated
		 */
		EClass VALIDATION_REQUEST = eINSTANCE.getValidationRequest();

		/**
		 * The meta object literal for the '<em><b>Role</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VALIDATION_REQUEST__ROLE = eINSTANCE.getValidationRequest_Role();

		/**
		 * The meta object literal for the '<em><b>Validation Objects</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VALIDATION_REQUEST__VALIDATION_OBJECTS = eINSTANCE.getValidationRequest_ValidationObjects();

		/**
		 * The meta object literal for the '<em><b>Cocl Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VALIDATION_REQUEST__COCL_ID = eINSTANCE.getValidationRequest_CoclId();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationResponseImpl <em>Validation Response</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationResponseImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getValidationResponse()
		 * @generated
		 */
		EClass VALIDATION_RESPONSE = eINSTANCE.getValidationResponse();

		/**
		 * The meta object literal for the '<em><b>Results</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VALIDATION_RESPONSE__RESULTS = eINSTANCE.getValidationResponse_Results();

		/**
		 * The meta object literal for the '<em><b>Role</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute VALIDATION_RESPONSE__ROLE = eINSTANCE.getValidationResponse_Role();

		/**
		 * The meta object literal for the '<em><b>Diagnostics</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VALIDATION_RESPONSE__DIAGNOSTICS = eINSTANCE.getValidationResponse_Diagnostics();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationResultImpl <em>Validation Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.ValidationResultImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getValidationResult()
		 * @generated
		 */
		EClass VALIDATION_RESULT = eINSTANCE.getValidationResult();

		/**
		 * The meta object literal for the '<em><b>Diagnostics</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference VALIDATION_RESULT__DIAGNOSTICS = eINSTANCE.getValidationResult_Diagnostics();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.SimpleValidationResultImpl <em>Simple Validation Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.SimpleValidationResultImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getSimpleValidationResult()
		 * @generated
		 */
		EClass SIMPLE_VALIDATION_RESULT = eINSTANCE.getSimpleValidationResult();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SIMPLE_VALIDATION_RESULT__VALUE = eINSTANCE.getSimpleValidationResult_Value();

		/**
		 * The meta object literal for the '<em><b>Value Java Class Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SIMPLE_VALIDATION_RESULT__VALUE_JAVA_CLASS_NAME = eINSTANCE.getSimpleValidationResult_ValueJavaClassName();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.EObjectValidationResultImpl <em>EObject Validation Result</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.EObjectValidationResultImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getEObjectValidationResult()
		 * @generated
		 */
		EClass EOBJECT_VALIDATION_RESULT = eINSTANCE.getEObjectValidationResult();

		/**
		 * The meta object literal for the '<em><b>Values</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EOBJECT_VALIDATION_RESULT__VALUES = eINSTANCE.getEObjectValidationResult_Values();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.DerivedValidationRequestImpl <em>Derived Validation Request</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.DerivedValidationRequestImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getDerivedValidationRequest()
		 * @generated
		 */
		EClass DERIVED_VALIDATION_REQUEST = eINSTANCE.getDerivedValidationRequest();

		/**
		 * The meta object literal for the '<em><b>Derived Feature</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DERIVED_VALIDATION_REQUEST__DERIVED_FEATURE = eINSTANCE.getDerivedValidationRequest_DerivedFeature();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationValidationRequestImpl <em>Operation Validation Request</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationValidationRequestImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOperationValidationRequest()
		 * @generated
		 */
		EClass OPERATION_VALIDATION_REQUEST = eINSTANCE.getOperationValidationRequest();

		/**
		 * The meta object literal for the '<em><b>Operation</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OPERATION_VALIDATION_REQUEST__OPERATION = eINSTANCE.getOperationValidationRequest_Operation();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OPERATION_VALIDATION_REQUEST__PARAMETERS = eINSTANCE.getOperationValidationRequest_Parameters();

		/**
		 * The meta object literal for the '<em><b>Operation Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OPERATION_VALIDATION_REQUEST__OPERATION_NAME = eINSTANCE.getOperationValidationRequest_OperationName();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationRequestParameterImpl <em>Operation Request Parameter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OperationRequestParameterImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOperationRequestParameter()
		 * @generated
		 */
		EClass OPERATION_REQUEST_PARAMETER = eINSTANCE.getOperationRequestParameter();

		/**
		 * The meta object literal for the '<em><b>Parameter Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OPERATION_REQUEST_PARAMETER__PARAMETER_NAME = eINSTANCE.getOperationRequestParameter_ParameterName();

		/**
		 * The meta object literal for the '<em><b>Parameter</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OPERATION_REQUEST_PARAMETER__PARAMETER = eINSTANCE.getOperationRequestParameter_Parameter();

		/**
		 * The meta object literal for the '<em><b>Is Null</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OPERATION_REQUEST_PARAMETER__IS_NULL = eINSTANCE.getOperationRequestParameter_IsNull();

		/**
		 * The meta object literal for the '<em><b>Java Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute OPERATION_REQUEST_PARAMETER__JAVA_VALUE = eINSTANCE.getOperationRequestParameter_JavaValue();

		/**
		 * The meta object literal for the '<em><b>EValue</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference OPERATION_REQUEST_PARAMETER__EVALUE = eINSTANCE.getOperationRequestParameter_EValue();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.DiagnosticImpl <em>Diagnostic</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.DiagnosticImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getDiagnostic()
		 * @generated
		 */
		EClass DIAGNOSTIC = eINSTANCE.getDiagnostic();

		/**
		 * The meta object literal for the '<em><b>Message</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DIAGNOSTIC__MESSAGE = eINSTANCE.getDiagnostic_Message();

		/**
		 * The meta object literal for the '<em><b>Source</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DIAGNOSTIC__SOURCE = eINSTANCE.getDiagnostic_Source();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DIAGNOSTIC__TYPE = eINSTANCE.getDiagnostic_Type();

		/**
		 * The meta object literal for the '<em><b>Children</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DIAGNOSTIC__CHILDREN = eINSTANCE.getDiagnostic_Children();

		/**
		 * The meta object literal for the '<em><b>Exception Msg</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DIAGNOSTIC__EXCEPTION_MSG = eINSTANCE.getDiagnostic_ExceptionMsg();

		/**
		 * The meta object literal for the '<em><b>Data</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DIAGNOSTIC__DATA = eINSTANCE.getDiagnostic_Data();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.BatchValidationRequestImpl <em>Batch Validation Request</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.BatchValidationRequestImpl
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getBatchValidationRequest()
		 * @generated
		 */
		EClass BATCH_VALIDATION_REQUEST = eINSTANCE.getBatchValidationRequest();

		/**
		 * The meta object literal for the '<em><b>Filter Constraint</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BATCH_VALIDATION_REQUEST__FILTER_CONSTRAINT = eINSTANCE.getBatchValidationRequest_FilterConstraint();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Severity <em>Severity</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Severity
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getSeverity()
		 * @generated
		 */
		EEnum SEVERITY = eINSTANCE.getSeverity();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole <em>Ocl Role</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOclRole()
		 * @generated
		 */
		EEnum OCL_ROLE = eINSTANCE.getOclRole();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationReturnType <em>Operation Return Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OperationReturnType
		 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOperationReturnType()
		 * @generated
		 */
		EEnum OPERATION_RETURN_TYPE = eINSTANCE.getOperationReturnType();

	}

} //COCLPackage
