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
@EPackage(uri = COCLPackage.eNS_URI, genModel = "/model/cocl.genmodel", genModelSourceLocations = {"model/cocl.genmodel","org.eclipse.fennec.model.atlas.validation.model/model/cocl.genmodel"}, ecore = "/model/cocl.ecore", ecoreSourceLocations = "/model/cocl.ecore")
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
	 * The number of structural features of the '<em>Ocl Constraint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int OCL_CONSTRAINT_FEATURE_COUNT = 10;

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
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Severity <em>Severity</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Severity
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getSeverity()
	 * @generated
	 */
	int SEVERITY = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole <em>Ocl Role</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.impl.COCLPackageImpl#getOclRole()
	 * @generated
	 */
	int OCL_ROLE = 3;


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

	}

} //COCLPackage
