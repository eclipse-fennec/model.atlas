/*
 * Copyright (c) 2012 - 2025 Data In Motion and others.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.policy.dataqs;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.fennec.model.atlas.governance.GovernancePackage;

import org.gecko.emf.osgi.annotation.provide.EPackage;

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
 * @see org.eclipse.fennec.model.atlas.policy.dataqs.DataQSFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = DataQSPackage.eNS_URI, genModel = "/../policies.genmodel", genModelSourceLocations = {"model/policies/policies.genmodel","org.eclipse.fennec.model.atlas.governance/model/policies/policies.genmodel"}, ecore="/../data-qs.ecore", ecoreSourceLocations="/model/policies/data-qs.ecore")
public interface DataQSPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "dataqs";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/fennec/model/atlas/policy/data_quality/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "dataqs";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DataQSPackage eINSTANCE = org.eclipse.fennec.model.atlas.policy.dataqs.impl.DataQSPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.policy.dataqs.impl.DataQualityPolicyCheckImpl <em>Data Quality Policy Check</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.policy.dataqs.impl.DataQualityPolicyCheckImpl
	 * @see org.eclipse.fennec.model.atlas.policy.dataqs.impl.DataQSPackageImpl#getDataQualityPolicyCheck()
	 * @generated
	 */
	int DATA_QUALITY_POLICY_CHECK = 0;

	/**
	 * The feature id for the '<em><b>Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_QUALITY_POLICY_CHECK__STATUS = GovernancePackage.COMPLIANCE_CHECK_RESULT__STATUS;

	/**
	 * The feature id for the '<em><b>Check Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_QUALITY_POLICY_CHECK__CHECK_TIMESTAMP = GovernancePackage.COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP;

	/**
	 * The feature id for the '<em><b>Summary</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_QUALITY_POLICY_CHECK__SUMMARY = GovernancePackage.COMPLIANCE_CHECK_RESULT__SUMMARY;

	/**
	 * The feature id for the '<em><b>Findings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_QUALITY_POLICY_CHECK__FINDINGS = GovernancePackage.COMPLIANCE_CHECK_RESULT__FINDINGS;

	/**
	 * The feature id for the '<em><b>Schema Conformance Verified</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_QUALITY_POLICY_CHECK__SCHEMA_CONFORMANCE_VERIFIED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Value Range Validated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_QUALITY_POLICY_CHECK__VALUE_RANGE_VALIDATED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Completeness Checked</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_QUALITY_POLICY_CHECK__COMPLETENESS_CHECKED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Format Validation Passed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_QUALITY_POLICY_CHECK__FORMAT_VALIDATION_PASSED = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Data Quality Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_QUALITY_POLICY_CHECK_FEATURE_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Data Quality Policy Check</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_QUALITY_POLICY_CHECK_OPERATION_COUNT = GovernancePackage.COMPLIANCE_CHECK_RESULT_OPERATION_COUNT + 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.policy.dataqs.DataQualityPolicyCheck <em>Data Quality Policy Check</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Data Quality Policy Check</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.dataqs.DataQualityPolicyCheck
	 * @generated
	 */
	EClass getDataQualityPolicyCheck();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.dataqs.DataQualityPolicyCheck#isSchemaConformanceVerified <em>Schema Conformance Verified</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Schema Conformance Verified</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.dataqs.DataQualityPolicyCheck#isSchemaConformanceVerified()
	 * @see #getDataQualityPolicyCheck()
	 * @generated
	 */
	EAttribute getDataQualityPolicyCheck_SchemaConformanceVerified();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.dataqs.DataQualityPolicyCheck#isValueRangeValidated <em>Value Range Validated</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value Range Validated</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.dataqs.DataQualityPolicyCheck#isValueRangeValidated()
	 * @see #getDataQualityPolicyCheck()
	 * @generated
	 */
	EAttribute getDataQualityPolicyCheck_ValueRangeValidated();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.dataqs.DataQualityPolicyCheck#isCompletenessChecked <em>Completeness Checked</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Completeness Checked</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.dataqs.DataQualityPolicyCheck#isCompletenessChecked()
	 * @see #getDataQualityPolicyCheck()
	 * @generated
	 */
	EAttribute getDataQualityPolicyCheck_CompletenessChecked();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.policy.dataqs.DataQualityPolicyCheck#isFormatValidationPassed <em>Format Validation Passed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Format Validation Passed</em>'.
	 * @see org.eclipse.fennec.model.atlas.policy.dataqs.DataQualityPolicyCheck#isFormatValidationPassed()
	 * @see #getDataQualityPolicyCheck()
	 * @generated
	 */
	EAttribute getDataQualityPolicyCheck_FormatValidationPassed();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	DataQSFactory getDataQSFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.policy.dataqs.impl.DataQualityPolicyCheckImpl <em>Data Quality Policy Check</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.policy.dataqs.impl.DataQualityPolicyCheckImpl
		 * @see org.eclipse.fennec.model.atlas.policy.dataqs.impl.DataQSPackageImpl#getDataQualityPolicyCheck()
		 * @generated
		 */
		EClass DATA_QUALITY_POLICY_CHECK = eINSTANCE.getDataQualityPolicyCheck();

		/**
		 * The meta object literal for the '<em><b>Schema Conformance Verified</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_QUALITY_POLICY_CHECK__SCHEMA_CONFORMANCE_VERIFIED = eINSTANCE.getDataQualityPolicyCheck_SchemaConformanceVerified();

		/**
		 * The meta object literal for the '<em><b>Value Range Validated</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_QUALITY_POLICY_CHECK__VALUE_RANGE_VALIDATED = eINSTANCE.getDataQualityPolicyCheck_ValueRangeValidated();

		/**
		 * The meta object literal for the '<em><b>Completeness Checked</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_QUALITY_POLICY_CHECK__COMPLETENESS_CHECKED = eINSTANCE.getDataQualityPolicyCheck_CompletenessChecked();

		/**
		 * The meta object literal for the '<em><b>Format Validation Passed</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_QUALITY_POLICY_CHECK__FORMAT_VALIDATION_PASSED = eINSTANCE.getDataQualityPolicyCheck_FormatValidationPassed();

	}

} //DataQSPackage
