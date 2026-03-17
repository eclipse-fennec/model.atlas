/*
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic;


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
 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticFactory
 * @model kind="package"
 *        annotation="Version value='1.0'"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel complianceLevel='17.0' oSGiCompatible='true' basePackage='org.eclipse.fennec.model.atlas.diagnostic.model' resource='XMI' copyrightText='Copyright (c) 2012 - 2026 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n    Data In Motion - initial API and implementation'"
 * @generated
 */
@ProviderType
@EPackage(uri = DiagnosticPackage.eNS_URI, genModel = "/model/diagnostic.genmodel", genModelSourceLocations = {"model/diagnostic.genmodel","org.eclipse.fennec.model.atlas.diagnostic.model/model/diagnostic.genmodel"}, ecore = "/model/diagnostic.ecore", ecoreSourceLocations = "/model/diagnostic.ecore")
public interface DiagnosticPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "diagnostic";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/diagnostic/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "diagnostic";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DiagnosticPackage eINSTANCE = org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.impl.DiagnosticPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.impl.DiagnosticImpl <em>Diagnostic</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.impl.DiagnosticImpl
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.impl.DiagnosticPackageImpl#getDiagnostic()
	 * @generated
	 */
	int DIAGNOSTIC = 0;

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
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticType <em>Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticType
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.impl.DiagnosticPackageImpl#getDiagnosticType()
	 * @generated
	 */
	int DIAGNOSTIC_TYPE = 1;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic <em>Diagnostic</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Diagnostic</em>'.
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic
	 * @generated
	 */
	EClass getDiagnostic();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getMessage <em>Message</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Message</em>'.
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getMessage()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EAttribute getDiagnostic_Message();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getSource <em>Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Source</em>'.
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getSource()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EAttribute getDiagnostic_Source();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getType()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EAttribute getDiagnostic_Type();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Children</em>'.
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getChildren()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EReference getDiagnostic_Children();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getExceptionMsg <em>Exception Msg</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Exception Msg</em>'.
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getExceptionMsg()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EAttribute getDiagnostic_ExceptionMsg();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getData <em>Data</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Data</em>'.
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.Diagnostic#getData()
	 * @see #getDiagnostic()
	 * @generated
	 */
	EAttribute getDiagnostic_Data();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Type</em>'.
	 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticType
	 * @generated
	 */
	EEnum getDiagnosticType();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	DiagnosticFactory getDiagnosticFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.impl.DiagnosticImpl <em>Diagnostic</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.impl.DiagnosticImpl
		 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.impl.DiagnosticPackageImpl#getDiagnostic()
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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticType <em>Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticType
		 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.impl.DiagnosticPackageImpl#getDiagnosticType()
		 * @generated
		 */
		EEnum DIAGNOSTIC_TYPE = eINSTANCE.getDiagnosticType();

	}

} //DiagnosticPackage
