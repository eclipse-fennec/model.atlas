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
package org.eclipse.fennec.model.atlas.mgmt.governanceapi;


import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;

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
 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.ManagementApiFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = ManagementApiPackage.eNS_URI, genModel = "/model/governance-api.genmodel", genModelSourceLocations = {"model/governance-api.genmodel","org.eclipse.fennec.model.atlas.governance/model/governance-api.genmodel"}, ecore="/model/governance-api.ecore", ecoreSourceLocations="/model/governance-api.ecore")
public interface ManagementApiPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "governanceapi";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/governance/api/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "govapi";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ManagementApiPackage eINSTANCE = org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider <em>Workflow Draft Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getWorkflowDraftProvider()
	 * @generated
	 */
	int WORKFLOW_DRAFT_PROVIDER = 5;

	/**
	 * The number of structural features of the '<em>Workflow Draft Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Upload Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___UPLOAD_DRAFT__EOBJECT_OBJECTMETADATA = 0;

	/**
	 * The operation id for the '<em>List Draft Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___LIST_DRAFT_OBJECTS = 1;

	/**
	 * The operation id for the '<em>Get Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___GET_DRAFT__STRING = 2;

	/**
	 * The operation id for the '<em>Get Draft Content</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___GET_DRAFT_CONTENT__STRING = 3;

	/**
	 * The operation id for the '<em>Update Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___UPDATE_DRAFT__STRING_EOBJECT = 4;

	/**
	 * The operation id for the '<em>Delete Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___DELETE_DRAFT__STRING = 5;

	/**
	 * The operation id for the '<em>Check Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER___CHECK_DRAFT__STRING_STRING_STRING = 6;

	/**
	 * The number of operations of the '<em>Workflow Draft Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT = 7;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService <em>EObject Workflow Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getEObjectWorkflowService()
	 * @generated
	 */
	int EOBJECT_WORKFLOW_SERVICE = 0;

	/**
	 * The number of structural features of the '<em>EObject Workflow Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE_FEATURE_COUNT = WORKFLOW_DRAFT_PROVIDER_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Upload Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPLOAD_DRAFT__EOBJECT_OBJECTMETADATA = WORKFLOW_DRAFT_PROVIDER___UPLOAD_DRAFT__EOBJECT_OBJECTMETADATA;

	/**
	 * The operation id for the '<em>List Draft Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_DRAFT_OBJECTS = WORKFLOW_DRAFT_PROVIDER___LIST_DRAFT_OBJECTS;

	/**
	 * The operation id for the '<em>Get Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_DRAFT__STRING = WORKFLOW_DRAFT_PROVIDER___GET_DRAFT__STRING;

	/**
	 * The operation id for the '<em>Get Draft Content</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_DRAFT_CONTENT__STRING = WORKFLOW_DRAFT_PROVIDER___GET_DRAFT_CONTENT__STRING;

	/**
	 * The operation id for the '<em>Update Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPDATE_DRAFT__STRING_EOBJECT = WORKFLOW_DRAFT_PROVIDER___UPDATE_DRAFT__STRING_EOBJECT;

	/**
	 * The operation id for the '<em>Delete Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___DELETE_DRAFT__STRING = WORKFLOW_DRAFT_PROVIDER___DELETE_DRAFT__STRING;

	/**
	 * The operation id for the '<em>Check Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___CHECK_DRAFT__STRING_STRING_STRING = WORKFLOW_DRAFT_PROVIDER___CHECK_DRAFT__STRING_STRING_STRING;

	/**
	 * The operation id for the '<em>Check Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___CHECK_OBJECT__STRING_STRING_STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Run Compliance Checks</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___RUN_COMPLIANCE_CHECKS__STRING_LIST_STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Compliance Status</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_COMPLIANCE_STATUS__STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 2;

	/**
	 * The operation id for the '<em>Set Governance Documentation Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___SET_GOVERNANCE_DOCUMENTATION_DRAFT__STRING_STRING_STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 3;

	/**
	 * The operation id for the '<em>Set Governance Documentation In Review</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___SET_GOVERNANCE_DOCUMENTATION_IN_REVIEW__STRING_STRING_STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 4;

	/**
	 * The operation id for the '<em>Set Governance Documentation Approved</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___SET_GOVERNANCE_DOCUMENTATION_APPROVED__STRING_STRING_STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 5;

	/**
	 * The operation id for the '<em>Set Governance Documentation Rejected</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___SET_GOVERNANCE_DOCUMENTATION_REJECTED__STRING_STRING_STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 6;

	/**
	 * The operation id for the '<em>Approve Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___APPROVE_OBJECT__STRING_STRING_STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 7;

	/**
	 * The operation id for the '<em>Reject Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___REJECT_OBJECT__STRING_STRING_STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 8;

	/**
	 * The operation id for the '<em>Release Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___RELEASE_OBJECT__STRING_STRING_BOOLEAN = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 9;

	/**
	 * The operation id for the '<em>List Approved Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_APPROVED_OBJECTS = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 10;

	/**
	 * The operation id for the '<em>List Rejected Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_REJECTED_OBJECTS = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 11;

	/**
	 * The operation id for the '<em>List Released Objects</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_RELEASED_OBJECTS = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 12;

	/**
	 * The operation id for the '<em>Get Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_OBJECT__STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 13;

	/**
	 * The operation id for the '<em>Get Object Content</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_OBJECT_CONTENT__STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 14;

	/**
	 * The operation id for the '<em>Update Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPDATE_OBJECT__STRING_EOBJECT = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 15;

	/**
	 * The operation id for the '<em>Delete Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___DELETE_OBJECT__STRING = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 16;

	/**
	 * The number of operations of the '<em>EObject Workflow Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE_OPERATION_COUNT = WORKFLOW_DRAFT_PROVIDER_OPERATION_COUNT + 17;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService <em>System Component Governance Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getSystemComponentGovernanceService()
	 * @generated
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE = 1;

	/**
	 * The number of structural features of the '<em>System Component Governance Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Register System Component</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE___REGISTER_SYSTEM_COMPONENT__STRING_SYSTEMCOMPONENTTYPE_STRING_STRING = 0;

	/**
	 * The operation id for the '<em>Update System Component Trust Level</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE___UPDATE_SYSTEM_COMPONENT_TRUST_LEVEL__STRING_TRUSTLEVEL = 1;

	/**
	 * The operation id for the '<em>Run System Component Compliance</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE___RUN_SYSTEM_COMPONENT_COMPLIANCE__STRING_LIST_STRING = 2;

	/**
	 * The operation id for the '<em>Apply Policies To System Component</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE___APPLY_POLICIES_TO_SYSTEM_COMPONENT__STRING_LIST = 3;

	/**
	 * The operation id for the '<em>Get System Component</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE___GET_SYSTEM_COMPONENT__STRING = 4;

	/**
	 * The operation id for the '<em>Find System Components By Governance Status</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE___FIND_SYSTEM_COMPONENTS_BY_GOVERNANCE_STATUS__COMPLIANCESTATUS = 5;

	/**
	 * The operation id for the '<em>Link Model To System Component</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE___LINK_MODEL_TO_SYSTEM_COMPONENT__STRING_STRING = 6;

	/**
	 * The operation id for the '<em>Get System Components By Type</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE___GET_SYSTEM_COMPONENTS_BY_TYPE__SYSTEMCOMPONENTTYPE = 7;

	/**
	 * The operation id for the '<em>Get System Id</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE___GET_SYSTEM_ID = 8;

	/**
	 * The number of operations of the '<em>System Component Governance Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_SERVICE_OPERATION_COUNT = 9;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService <em>Governance Compliance Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getGovernanceComplianceService()
	 * @generated
	 */
	int GOVERNANCE_COMPLIANCE_SERVICE = 2;

	/**
	 * The number of structural features of the '<em>Governance Compliance Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_COMPLIANCE_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Run Compliance Checks</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_COMPLIANCE_SERVICE___RUN_COMPLIANCE_CHECKS__STRING_STRING_STRING = 0;

	/**
	 * The operation id for the '<em>Run Specific Compliance Checks</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_COMPLIANCE_SERVICE___RUN_SPECIFIC_COMPLIANCE_CHECKS__STRING_LIST_STRING = 1;

	/**
	 * The operation id for the '<em>Run System Component Compliance</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_COMPLIANCE_SERVICE___RUN_SYSTEM_COMPONENT_COMPLIANCE__STRING_STRING_LIST_STRING = 2;

	/**
	 * The operation id for the '<em>Get Compliance Status</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_COMPLIANCE_SERVICE___GET_COMPLIANCE_STATUS__STRING = 3;

	/**
	 * The operation id for the '<em>Validate Policy Compliance</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_COMPLIANCE_SERVICE___VALIDATE_POLICY_COMPLIANCE__STRING_POLICYTYPE_STRING = 4;

	/**
	 * The operation id for the '<em>Get Available Policies</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_COMPLIANCE_SERVICE___GET_AVAILABLE_POLICIES = 5;

	/**
	 * The number of operations of the '<em>Governance Compliance Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_COMPLIANCE_SERVICE_OPERATION_COUNT = 6;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository <em>Governance System Repository</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getGovernanceSystemRepository()
	 * @generated
	 */
	int GOVERNANCE_SYSTEM_REPOSITORY = 3;

	/**
	 * The number of structural features of the '<em>Governance System Repository</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_SYSTEM_REPOSITORY_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Get Governance System</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_SYSTEM_REPOSITORY___GET_GOVERNANCE_SYSTEM__STRING = 0;

	/**
	 * The operation id for the '<em>Create System Component</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_SYSTEM_REPOSITORY___CREATE_SYSTEM_COMPONENT__STRING_STRING_SYSTEMCOMPONENTTYPE_STRING_STRING = 1;

	/**
	 * The operation id for the '<em>Get System Component</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_SYSTEM_REPOSITORY___GET_SYSTEM_COMPONENT__STRING_STRING = 2;

	/**
	 * The operation id for the '<em>Update System Component Trust Level</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_SYSTEM_REPOSITORY___UPDATE_SYSTEM_COMPONENT_TRUST_LEVEL__STRING_STRING_TRUSTLEVEL = 3;

	/**
	 * The operation id for the '<em>Apply Policies To System Component</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_SYSTEM_REPOSITORY___APPLY_POLICIES_TO_SYSTEM_COMPONENT__STRING_STRING_LIST = 4;

	/**
	 * The operation id for the '<em>Find System Components By Compliance Status</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_SYSTEM_REPOSITORY___FIND_SYSTEM_COMPONENTS_BY_COMPLIANCE_STATUS__STRING_COMPLIANCESTATUS = 5;

	/**
	 * The operation id for the '<em>Get System Components By Type</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_SYSTEM_REPOSITORY___GET_SYSTEM_COMPONENTS_BY_TYPE__STRING_SYSTEMCOMPONENTTYPE = 6;

	/**
	 * The operation id for the '<em>Link Model To System Component</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_SYSTEM_REPOSITORY___LINK_MODEL_TO_SYSTEM_COMPONENT__STRING_STRING_STRING = 7;

	/**
	 * The number of operations of the '<em>Governance System Repository</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_SYSTEM_REPOSITORY_OPERATION_COUNT = 8;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceConfig <em>System Component Governance Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceConfig
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getSystemComponentGovernanceConfig()
	 * @generated
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_CONFIG = 4;

	/**
	 * The number of structural features of the '<em>System Component Governance Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_CONFIG_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>System Id</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_CONFIG___SYSTEM_ID = 0;

	/**
	 * The operation id for the '<em>Description</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_CONFIG___DESCRIPTION = 1;

	/**
	 * The number of operations of the '<em>System Component Governance Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SYSTEM_COMPONENT_GOVERNANCE_CONFIG_OPERATION_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider <em>Workflow Compliance Provider</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getWorkflowComplianceProvider()
	 * @generated
	 */
	int WORKFLOW_COMPLIANCE_PROVIDER = 6;

	/**
	 * The number of structural features of the '<em>Workflow Compliance Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_COMPLIANCE_PROVIDER_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Check Object</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_COMPLIANCE_PROVIDER___CHECK_OBJECT__STRING_STRING_STRING = 0;

	/**
	 * The operation id for the '<em>Run Compliance Checks</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_COMPLIANCE_PROVIDER___RUN_COMPLIANCE_CHECKS__STRING_LIST_STRING = 1;

	/**
	 * The operation id for the '<em>Get Compliance Status</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_COMPLIANCE_PROVIDER___GET_COMPLIANCE_STATUS__STRING = 2;

	/**
	 * The operation id for the '<em>Set Governance Documentation Draft</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_DRAFT__STRING_STRING_STRING = 3;

	/**
	 * The operation id for the '<em>Set Governance Documentation In Review</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_IN_REVIEW__STRING_STRING_STRING = 4;

	/**
	 * The operation id for the '<em>Set Governance Documentation Approved</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_APPROVED__STRING_STRING_STRING = 5;

	/**
	 * The operation id for the '<em>Set Governance Documentation Rejected</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_REJECTED__STRING_STRING_STRING = 6;

	/**
	 * The number of operations of the '<em>Workflow Compliance Provider</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WORKFLOW_COMPLIANCE_PROVIDER_OPERATION_COUNT = 7;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService <em>Governance Documentation Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getGovernanceDocumentationService()
	 * @generated
	 */
	int GOVERNANCE_DOCUMENTATION_SERVICE = 7;

	/**
	 * The number of structural features of the '<em>Governance Documentation Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_DOCUMENTATION_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Store Documentation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_DOCUMENTATION_SERVICE___STORE_DOCUMENTATION__STRING_GOVERNANCEDOCUMENTATION_STRING_STRING = 0;

	/**
	 * The operation id for the '<em>Get Latest Documentation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_DOCUMENTATION_SERVICE___GET_LATEST_DOCUMENTATION__STRING = 1;

	/**
	 * The operation id for the '<em>Get Documentation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_DOCUMENTATION_SERVICE___GET_DOCUMENTATION__STRING = 2;

	/**
	 * The operation id for the '<em>Get Documentation History</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_DOCUMENTATION_SERVICE___GET_DOCUMENTATION_HISTORY__STRING = 3;

	/**
	 * The operation id for the '<em>Has Documentation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_DOCUMENTATION_SERVICE___HAS_DOCUMENTATION__STRING = 4;

	/**
	 * The operation id for the '<em>Delete All Documentation</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_DOCUMENTATION_SERVICE___DELETE_ALL_DOCUMENTATION__STRING = 5;

	/**
	 * The operation id for the '<em>Get Documentation Statistics</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_DOCUMENTATION_SERVICE___GET_DOCUMENTATION_STATISTICS = 6;

	/**
	 * The number of operations of the '<em>Governance Documentation Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GOVERNANCE_DOCUMENTATION_SERVICE_OPERATION_COUNT = 7;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService <em>EObject Workflow Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EObject Workflow Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService
	 * @generated
	 */
	EClass getEObjectWorkflowService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#approveObject(java.lang.String, java.lang.String, java.lang.String) <em>Approve Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Approve Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#approveObject(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ApproveObject__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#rejectObject(java.lang.String, java.lang.String, java.lang.String) <em>Reject Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Reject Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#rejectObject(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__RejectObject__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#releaseObject(java.lang.String, java.lang.String, boolean) <em>Release Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Release Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#releaseObject(java.lang.String, java.lang.String, boolean)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ReleaseObject__String_String_boolean();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#listApprovedObjects() <em>List Approved Objects</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Approved Objects</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#listApprovedObjects()
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListApprovedObjects();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#listRejectedObjects() <em>List Rejected Objects</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Rejected Objects</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#listRejectedObjects()
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListRejectedObjects();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#listReleasedObjects() <em>List Released Objects</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Released Objects</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#listReleasedObjects()
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListReleasedObjects();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#getObject(java.lang.String) <em>Get Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#getObject(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__GetObject__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#getObjectContent(java.lang.String) <em>Get Object Content</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Object Content</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#getObjectContent(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__GetObjectContent__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#updateObject(java.lang.String, org.eclipse.emf.ecore.EObject) <em>Update Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#updateObject(java.lang.String, org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__UpdateObject__String_EObject();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#deleteObject(java.lang.String) <em>Delete Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService#deleteObject(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__DeleteObject__String();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService <em>System Component Governance Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>System Component Governance Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService
	 * @generated
	 */
	EClass getSystemComponentGovernanceService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#registerSystemComponent(java.lang.String, org.eclipse.fennec.model.atlas.governance.SystemComponentType, java.lang.String, java.lang.String) <em>Register System Component</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Register System Component</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#registerSystemComponent(java.lang.String, org.eclipse.fennec.model.atlas.governance.SystemComponentType, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getSystemComponentGovernanceService__RegisterSystemComponent__String_SystemComponentType_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#updateSystemComponentTrustLevel(java.lang.String, org.eclipse.fennec.model.atlas.governance.TrustLevel) <em>Update System Component Trust Level</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update System Component Trust Level</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#updateSystemComponentTrustLevel(java.lang.String, org.eclipse.fennec.model.atlas.governance.TrustLevel)
	 * @generated
	 */
	EOperation getSystemComponentGovernanceService__UpdateSystemComponentTrustLevel__String_TrustLevel();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#runSystemComponentCompliance(java.lang.String, java.util.List, java.lang.String) <em>Run System Component Compliance</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Run System Component Compliance</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#runSystemComponentCompliance(java.lang.String, java.util.List, java.lang.String)
	 * @generated
	 */
	EOperation getSystemComponentGovernanceService__RunSystemComponentCompliance__String_List_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#applyPoliciesToSystemComponent(java.lang.String, java.util.List) <em>Apply Policies To System Component</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Apply Policies To System Component</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#applyPoliciesToSystemComponent(java.lang.String, java.util.List)
	 * @generated
	 */
	EOperation getSystemComponentGovernanceService__ApplyPoliciesToSystemComponent__String_List();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#getSystemComponent(java.lang.String) <em>Get System Component</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get System Component</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#getSystemComponent(java.lang.String)
	 * @generated
	 */
	EOperation getSystemComponentGovernanceService__GetSystemComponent__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#findSystemComponentsByGovernanceStatus(org.eclipse.fennec.model.atlas.governance.ComplianceStatus) <em>Find System Components By Governance Status</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find System Components By Governance Status</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#findSystemComponentsByGovernanceStatus(org.eclipse.fennec.model.atlas.governance.ComplianceStatus)
	 * @generated
	 */
	EOperation getSystemComponentGovernanceService__FindSystemComponentsByGovernanceStatus__ComplianceStatus();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#linkModelToSystemComponent(java.lang.String, java.lang.String) <em>Link Model To System Component</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Link Model To System Component</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#linkModelToSystemComponent(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getSystemComponentGovernanceService__LinkModelToSystemComponent__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#getSystemComponentsByType(org.eclipse.fennec.model.atlas.governance.SystemComponentType) <em>Get System Components By Type</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get System Components By Type</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#getSystemComponentsByType(org.eclipse.fennec.model.atlas.governance.SystemComponentType)
	 * @generated
	 */
	EOperation getSystemComponentGovernanceService__GetSystemComponentsByType__SystemComponentType();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#getSystemId() <em>Get System Id</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get System Id</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService#getSystemId()
	 * @generated
	 */
	EOperation getSystemComponentGovernanceService__GetSystemId();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService <em>Governance Compliance Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Governance Compliance Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService
	 * @generated
	 */
	EClass getGovernanceComplianceService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#runComplianceChecks(java.lang.String, java.lang.String, java.lang.String) <em>Run Compliance Checks</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Run Compliance Checks</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#runComplianceChecks(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceComplianceService__RunComplianceChecks__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#runSpecificComplianceChecks(java.lang.String, java.util.List, java.lang.String) <em>Run Specific Compliance Checks</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Run Specific Compliance Checks</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#runSpecificComplianceChecks(java.lang.String, java.util.List, java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceComplianceService__RunSpecificComplianceChecks__String_List_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#runSystemComponentCompliance(java.lang.String, java.lang.String, java.util.List, java.lang.String) <em>Run System Component Compliance</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Run System Component Compliance</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#runSystemComponentCompliance(java.lang.String, java.lang.String, java.util.List, java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceComplianceService__RunSystemComponentCompliance__String_String_List_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#getComplianceStatus(java.lang.String) <em>Get Compliance Status</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Compliance Status</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#getComplianceStatus(java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceComplianceService__GetComplianceStatus__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#validatePolicyCompliance(java.lang.String, org.eclipse.fennec.model.atlas.governance.PolicyType, java.lang.String) <em>Validate Policy Compliance</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Validate Policy Compliance</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#validatePolicyCompliance(java.lang.String, org.eclipse.fennec.model.atlas.governance.PolicyType, java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceComplianceService__ValidatePolicyCompliance__String_PolicyType_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#getAvailablePolicies() <em>Get Available Policies</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Available Policies</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService#getAvailablePolicies()
	 * @generated
	 */
	EOperation getGovernanceComplianceService__GetAvailablePolicies();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository <em>Governance System Repository</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Governance System Repository</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository
	 * @generated
	 */
	EClass getGovernanceSystemRepository();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#getGovernanceSystem(java.lang.String) <em>Get Governance System</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Governance System</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#getGovernanceSystem(java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceSystemRepository__GetGovernanceSystem__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#createSystemComponent(java.lang.String, java.lang.String, org.eclipse.fennec.model.atlas.governance.SystemComponentType, java.lang.String, java.lang.String) <em>Create System Component</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Create System Component</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#createSystemComponent(java.lang.String, java.lang.String, org.eclipse.fennec.model.atlas.governance.SystemComponentType, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceSystemRepository__CreateSystemComponent__String_String_SystemComponentType_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#getSystemComponent(java.lang.String, java.lang.String) <em>Get System Component</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get System Component</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#getSystemComponent(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceSystemRepository__GetSystemComponent__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#updateSystemComponentTrustLevel(java.lang.String, java.lang.String, org.eclipse.fennec.model.atlas.governance.TrustLevel) <em>Update System Component Trust Level</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update System Component Trust Level</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#updateSystemComponentTrustLevel(java.lang.String, java.lang.String, org.eclipse.fennec.model.atlas.governance.TrustLevel)
	 * @generated
	 */
	EOperation getGovernanceSystemRepository__UpdateSystemComponentTrustLevel__String_String_TrustLevel();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#applyPoliciesToSystemComponent(java.lang.String, java.lang.String, java.util.List) <em>Apply Policies To System Component</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Apply Policies To System Component</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#applyPoliciesToSystemComponent(java.lang.String, java.lang.String, java.util.List)
	 * @generated
	 */
	EOperation getGovernanceSystemRepository__ApplyPoliciesToSystemComponent__String_String_List();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#findSystemComponentsByComplianceStatus(java.lang.String, org.eclipse.fennec.model.atlas.governance.ComplianceStatus) <em>Find System Components By Compliance Status</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Find System Components By Compliance Status</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#findSystemComponentsByComplianceStatus(java.lang.String, org.eclipse.fennec.model.atlas.governance.ComplianceStatus)
	 * @generated
	 */
	EOperation getGovernanceSystemRepository__FindSystemComponentsByComplianceStatus__String_ComplianceStatus();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#getSystemComponentsByType(java.lang.String, org.eclipse.fennec.model.atlas.governance.SystemComponentType) <em>Get System Components By Type</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get System Components By Type</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#getSystemComponentsByType(java.lang.String, org.eclipse.fennec.model.atlas.governance.SystemComponentType)
	 * @generated
	 */
	EOperation getGovernanceSystemRepository__GetSystemComponentsByType__String_SystemComponentType();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#linkModelToSystemComponent(java.lang.String, java.lang.String, java.lang.String) <em>Link Model To System Component</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Link Model To System Component</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository#linkModelToSystemComponent(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceSystemRepository__LinkModelToSystemComponent__String_String_String();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceConfig <em>System Component Governance Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>System Component Governance Config</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceConfig
	 * @generated
	 */
	EClass getSystemComponentGovernanceConfig();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceConfig#systemId() <em>System Id</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>System Id</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceConfig#systemId()
	 * @generated
	 */
	EOperation getSystemComponentGovernanceConfig__SystemId();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceConfig#description() <em>Description</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Description</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceConfig#description()
	 * @generated
	 */
	EOperation getSystemComponentGovernanceConfig__Description();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider <em>Workflow Draft Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Workflow Draft Provider</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider
	 * @generated
	 */
	EClass getWorkflowDraftProvider();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#uploadDraft(org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata) <em>Upload Draft</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Upload Draft</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#uploadDraft(org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__UploadDraft__EObject_ObjectMetadata();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#listDraftObjects() <em>List Draft Objects</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List Draft Objects</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#listDraftObjects()
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__ListDraftObjects();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#getDraft(java.lang.String) <em>Get Draft</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Draft</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#getDraft(java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__GetDraft__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#getDraftContent(java.lang.String) <em>Get Draft Content</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Draft Content</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#getDraftContent(java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__GetDraftContent__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#updateDraft(java.lang.String, org.eclipse.emf.ecore.EObject) <em>Update Draft</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Draft</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#updateDraft(java.lang.String, org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__UpdateDraft__String_EObject();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#deleteDraft(java.lang.String) <em>Delete Draft</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete Draft</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#deleteDraft(java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__DeleteDraft__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#checkDraft(java.lang.String, java.lang.String, java.lang.String) <em>Check Draft</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Check Draft</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider#checkDraft(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowDraftProvider__CheckDraft__String_String_String();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider <em>Workflow Compliance Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Workflow Compliance Provider</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider
	 * @generated
	 */
	EClass getWorkflowComplianceProvider();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#checkObject(java.lang.String, java.lang.String, java.lang.String) <em>Check Object</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Check Object</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#checkObject(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowComplianceProvider__CheckObject__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#runComplianceChecks(java.lang.String, java.util.List, java.lang.String) <em>Run Compliance Checks</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Run Compliance Checks</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#runComplianceChecks(java.lang.String, java.util.List, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowComplianceProvider__RunComplianceChecks__String_List_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#getComplianceStatus(java.lang.String) <em>Get Compliance Status</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Compliance Status</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#getComplianceStatus(java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowComplianceProvider__GetComplianceStatus__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#setGovernanceDocumentationDraft(java.lang.String, java.lang.String, java.lang.String) <em>Set Governance Documentation Draft</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Set Governance Documentation Draft</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#setGovernanceDocumentationDraft(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowComplianceProvider__SetGovernanceDocumentationDraft__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#setGovernanceDocumentationInReview(java.lang.String, java.lang.String, java.lang.String) <em>Set Governance Documentation In Review</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Set Governance Documentation In Review</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#setGovernanceDocumentationInReview(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowComplianceProvider__SetGovernanceDocumentationInReview__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#setGovernanceDocumentationApproved(java.lang.String, java.lang.String, java.lang.String) <em>Set Governance Documentation Approved</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Set Governance Documentation Approved</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#setGovernanceDocumentationApproved(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowComplianceProvider__SetGovernanceDocumentationApproved__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#setGovernanceDocumentationRejected(java.lang.String, java.lang.String, java.lang.String) <em>Set Governance Documentation Rejected</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Set Governance Documentation Rejected</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider#setGovernanceDocumentationRejected(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWorkflowComplianceProvider__SetGovernanceDocumentationRejected__String_String_String();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService <em>Governance Documentation Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Governance Documentation Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService
	 * @generated
	 */
	EClass getGovernanceDocumentationService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#storeDocumentation(java.lang.String, org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation, java.lang.String, java.lang.String) <em>Store Documentation</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Store Documentation</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#storeDocumentation(java.lang.String, org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceDocumentationService__StoreDocumentation__String_GovernanceDocumentation_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#getLatestDocumentation(java.lang.String) <em>Get Latest Documentation</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Latest Documentation</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#getLatestDocumentation(java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceDocumentationService__GetLatestDocumentation__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#getDocumentation(java.lang.String) <em>Get Documentation</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Documentation</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#getDocumentation(java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceDocumentationService__GetDocumentation__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#getDocumentationHistory(java.lang.String) <em>Get Documentation History</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Documentation History</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#getDocumentationHistory(java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceDocumentationService__GetDocumentationHistory__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#hasDocumentation(java.lang.String) <em>Has Documentation</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Has Documentation</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#hasDocumentation(java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceDocumentationService__HasDocumentation__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#deleteAllDocumentation(java.lang.String) <em>Delete All Documentation</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete All Documentation</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#deleteAllDocumentation(java.lang.String)
	 * @generated
	 */
	EOperation getGovernanceDocumentationService__DeleteAllDocumentation__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#getDocumentationStatistics() <em>Get Documentation Statistics</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Documentation Statistics</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService#getDocumentationStatistics()
	 * @generated
	 */
	EOperation getGovernanceDocumentationService__GetDocumentationStatistics();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ManagementApiFactory getManagementApiFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService <em>EObject Workflow Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.EObjectWorkflowService
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getEObjectWorkflowService()
		 * @generated
		 */
		EClass EOBJECT_WORKFLOW_SERVICE = eINSTANCE.getEObjectWorkflowService();

		/**
		 * The meta object literal for the '<em><b>Approve Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___APPROVE_OBJECT__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__ApproveObject__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Reject Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___REJECT_OBJECT__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__RejectObject__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Release Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___RELEASE_OBJECT__STRING_STRING_BOOLEAN = eINSTANCE.getEObjectWorkflowService__ReleaseObject__String_String_boolean();

		/**
		 * The meta object literal for the '<em><b>List Approved Objects</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_APPROVED_OBJECTS = eINSTANCE.getEObjectWorkflowService__ListApprovedObjects();

		/**
		 * The meta object literal for the '<em><b>List Rejected Objects</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_REJECTED_OBJECTS = eINSTANCE.getEObjectWorkflowService__ListRejectedObjects();

		/**
		 * The meta object literal for the '<em><b>List Released Objects</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_RELEASED_OBJECTS = eINSTANCE.getEObjectWorkflowService__ListReleasedObjects();

		/**
		 * The meta object literal for the '<em><b>Get Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___GET_OBJECT__STRING = eINSTANCE.getEObjectWorkflowService__GetObject__String();

		/**
		 * The meta object literal for the '<em><b>Get Object Content</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___GET_OBJECT_CONTENT__STRING = eINSTANCE.getEObjectWorkflowService__GetObjectContent__String();

		/**
		 * The meta object literal for the '<em><b>Update Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___UPDATE_OBJECT__STRING_EOBJECT = eINSTANCE.getEObjectWorkflowService__UpdateObject__String_EObject();

		/**
		 * The meta object literal for the '<em><b>Delete Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___DELETE_OBJECT__STRING = eINSTANCE.getEObjectWorkflowService__DeleteObject__String();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService <em>System Component Governance Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceService
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getSystemComponentGovernanceService()
		 * @generated
		 */
		EClass SYSTEM_COMPONENT_GOVERNANCE_SERVICE = eINSTANCE.getSystemComponentGovernanceService();

		/**
		 * The meta object literal for the '<em><b>Register System Component</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_COMPONENT_GOVERNANCE_SERVICE___REGISTER_SYSTEM_COMPONENT__STRING_SYSTEMCOMPONENTTYPE_STRING_STRING = eINSTANCE.getSystemComponentGovernanceService__RegisterSystemComponent__String_SystemComponentType_String_String();

		/**
		 * The meta object literal for the '<em><b>Update System Component Trust Level</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_COMPONENT_GOVERNANCE_SERVICE___UPDATE_SYSTEM_COMPONENT_TRUST_LEVEL__STRING_TRUSTLEVEL = eINSTANCE.getSystemComponentGovernanceService__UpdateSystemComponentTrustLevel__String_TrustLevel();

		/**
		 * The meta object literal for the '<em><b>Run System Component Compliance</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_COMPONENT_GOVERNANCE_SERVICE___RUN_SYSTEM_COMPONENT_COMPLIANCE__STRING_LIST_STRING = eINSTANCE.getSystemComponentGovernanceService__RunSystemComponentCompliance__String_List_String();

		/**
		 * The meta object literal for the '<em><b>Apply Policies To System Component</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_COMPONENT_GOVERNANCE_SERVICE___APPLY_POLICIES_TO_SYSTEM_COMPONENT__STRING_LIST = eINSTANCE.getSystemComponentGovernanceService__ApplyPoliciesToSystemComponent__String_List();

		/**
		 * The meta object literal for the '<em><b>Get System Component</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_COMPONENT_GOVERNANCE_SERVICE___GET_SYSTEM_COMPONENT__STRING = eINSTANCE.getSystemComponentGovernanceService__GetSystemComponent__String();

		/**
		 * The meta object literal for the '<em><b>Find System Components By Governance Status</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_COMPONENT_GOVERNANCE_SERVICE___FIND_SYSTEM_COMPONENTS_BY_GOVERNANCE_STATUS__COMPLIANCESTATUS = eINSTANCE.getSystemComponentGovernanceService__FindSystemComponentsByGovernanceStatus__ComplianceStatus();

		/**
		 * The meta object literal for the '<em><b>Link Model To System Component</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_COMPONENT_GOVERNANCE_SERVICE___LINK_MODEL_TO_SYSTEM_COMPONENT__STRING_STRING = eINSTANCE.getSystemComponentGovernanceService__LinkModelToSystemComponent__String_String();

		/**
		 * The meta object literal for the '<em><b>Get System Components By Type</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_COMPONENT_GOVERNANCE_SERVICE___GET_SYSTEM_COMPONENTS_BY_TYPE__SYSTEMCOMPONENTTYPE = eINSTANCE.getSystemComponentGovernanceService__GetSystemComponentsByType__SystemComponentType();

		/**
		 * The meta object literal for the '<em><b>Get System Id</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_COMPONENT_GOVERNANCE_SERVICE___GET_SYSTEM_ID = eINSTANCE.getSystemComponentGovernanceService__GetSystemId();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService <em>Governance Compliance Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceComplianceService
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getGovernanceComplianceService()
		 * @generated
		 */
		EClass GOVERNANCE_COMPLIANCE_SERVICE = eINSTANCE.getGovernanceComplianceService();

		/**
		 * The meta object literal for the '<em><b>Run Compliance Checks</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_COMPLIANCE_SERVICE___RUN_COMPLIANCE_CHECKS__STRING_STRING_STRING = eINSTANCE.getGovernanceComplianceService__RunComplianceChecks__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Run Specific Compliance Checks</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_COMPLIANCE_SERVICE___RUN_SPECIFIC_COMPLIANCE_CHECKS__STRING_LIST_STRING = eINSTANCE.getGovernanceComplianceService__RunSpecificComplianceChecks__String_List_String();

		/**
		 * The meta object literal for the '<em><b>Run System Component Compliance</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_COMPLIANCE_SERVICE___RUN_SYSTEM_COMPONENT_COMPLIANCE__STRING_STRING_LIST_STRING = eINSTANCE.getGovernanceComplianceService__RunSystemComponentCompliance__String_String_List_String();

		/**
		 * The meta object literal for the '<em><b>Get Compliance Status</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_COMPLIANCE_SERVICE___GET_COMPLIANCE_STATUS__STRING = eINSTANCE.getGovernanceComplianceService__GetComplianceStatus__String();

		/**
		 * The meta object literal for the '<em><b>Validate Policy Compliance</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_COMPLIANCE_SERVICE___VALIDATE_POLICY_COMPLIANCE__STRING_POLICYTYPE_STRING = eINSTANCE.getGovernanceComplianceService__ValidatePolicyCompliance__String_PolicyType_String();

		/**
		 * The meta object literal for the '<em><b>Get Available Policies</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_COMPLIANCE_SERVICE___GET_AVAILABLE_POLICIES = eINSTANCE.getGovernanceComplianceService__GetAvailablePolicies();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository <em>Governance System Repository</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceSystemRepository
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getGovernanceSystemRepository()
		 * @generated
		 */
		EClass GOVERNANCE_SYSTEM_REPOSITORY = eINSTANCE.getGovernanceSystemRepository();

		/**
		 * The meta object literal for the '<em><b>Get Governance System</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_SYSTEM_REPOSITORY___GET_GOVERNANCE_SYSTEM__STRING = eINSTANCE.getGovernanceSystemRepository__GetGovernanceSystem__String();

		/**
		 * The meta object literal for the '<em><b>Create System Component</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_SYSTEM_REPOSITORY___CREATE_SYSTEM_COMPONENT__STRING_STRING_SYSTEMCOMPONENTTYPE_STRING_STRING = eINSTANCE.getGovernanceSystemRepository__CreateSystemComponent__String_String_SystemComponentType_String_String();

		/**
		 * The meta object literal for the '<em><b>Get System Component</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_SYSTEM_REPOSITORY___GET_SYSTEM_COMPONENT__STRING_STRING = eINSTANCE.getGovernanceSystemRepository__GetSystemComponent__String_String();

		/**
		 * The meta object literal for the '<em><b>Update System Component Trust Level</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_SYSTEM_REPOSITORY___UPDATE_SYSTEM_COMPONENT_TRUST_LEVEL__STRING_STRING_TRUSTLEVEL = eINSTANCE.getGovernanceSystemRepository__UpdateSystemComponentTrustLevel__String_String_TrustLevel();

		/**
		 * The meta object literal for the '<em><b>Apply Policies To System Component</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_SYSTEM_REPOSITORY___APPLY_POLICIES_TO_SYSTEM_COMPONENT__STRING_STRING_LIST = eINSTANCE.getGovernanceSystemRepository__ApplyPoliciesToSystemComponent__String_String_List();

		/**
		 * The meta object literal for the '<em><b>Find System Components By Compliance Status</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_SYSTEM_REPOSITORY___FIND_SYSTEM_COMPONENTS_BY_COMPLIANCE_STATUS__STRING_COMPLIANCESTATUS = eINSTANCE.getGovernanceSystemRepository__FindSystemComponentsByComplianceStatus__String_ComplianceStatus();

		/**
		 * The meta object literal for the '<em><b>Get System Components By Type</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_SYSTEM_REPOSITORY___GET_SYSTEM_COMPONENTS_BY_TYPE__STRING_SYSTEMCOMPONENTTYPE = eINSTANCE.getGovernanceSystemRepository__GetSystemComponentsByType__String_SystemComponentType();

		/**
		 * The meta object literal for the '<em><b>Link Model To System Component</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_SYSTEM_REPOSITORY___LINK_MODEL_TO_SYSTEM_COMPONENT__STRING_STRING_STRING = eINSTANCE.getGovernanceSystemRepository__LinkModelToSystemComponent__String_String_String();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceConfig <em>System Component Governance Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.SystemComponentGovernanceConfig
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getSystemComponentGovernanceConfig()
		 * @generated
		 */
		EClass SYSTEM_COMPONENT_GOVERNANCE_CONFIG = eINSTANCE.getSystemComponentGovernanceConfig();

		/**
		 * The meta object literal for the '<em><b>System Id</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_COMPONENT_GOVERNANCE_CONFIG___SYSTEM_ID = eINSTANCE.getSystemComponentGovernanceConfig__SystemId();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation SYSTEM_COMPONENT_GOVERNANCE_CONFIG___DESCRIPTION = eINSTANCE.getSystemComponentGovernanceConfig__Description();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider <em>Workflow Draft Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowDraftProvider
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getWorkflowDraftProvider()
		 * @generated
		 */
		EClass WORKFLOW_DRAFT_PROVIDER = eINSTANCE.getWorkflowDraftProvider();

		/**
		 * The meta object literal for the '<em><b>Upload Draft</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___UPLOAD_DRAFT__EOBJECT_OBJECTMETADATA = eINSTANCE.getWorkflowDraftProvider__UploadDraft__EObject_ObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>List Draft Objects</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___LIST_DRAFT_OBJECTS = eINSTANCE.getWorkflowDraftProvider__ListDraftObjects();

		/**
		 * The meta object literal for the '<em><b>Get Draft</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___GET_DRAFT__STRING = eINSTANCE.getWorkflowDraftProvider__GetDraft__String();

		/**
		 * The meta object literal for the '<em><b>Get Draft Content</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___GET_DRAFT_CONTENT__STRING = eINSTANCE.getWorkflowDraftProvider__GetDraftContent__String();

		/**
		 * The meta object literal for the '<em><b>Update Draft</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___UPDATE_DRAFT__STRING_EOBJECT = eINSTANCE.getWorkflowDraftProvider__UpdateDraft__String_EObject();

		/**
		 * The meta object literal for the '<em><b>Delete Draft</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___DELETE_DRAFT__STRING = eINSTANCE.getWorkflowDraftProvider__DeleteDraft__String();

		/**
		 * The meta object literal for the '<em><b>Check Draft</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_DRAFT_PROVIDER___CHECK_DRAFT__STRING_STRING_STRING = eINSTANCE.getWorkflowDraftProvider__CheckDraft__String_String_String();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider <em>Workflow Compliance Provider</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.WorkflowComplianceProvider
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getWorkflowComplianceProvider()
		 * @generated
		 */
		EClass WORKFLOW_COMPLIANCE_PROVIDER = eINSTANCE.getWorkflowComplianceProvider();

		/**
		 * The meta object literal for the '<em><b>Check Object</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_COMPLIANCE_PROVIDER___CHECK_OBJECT__STRING_STRING_STRING = eINSTANCE.getWorkflowComplianceProvider__CheckObject__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Run Compliance Checks</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_COMPLIANCE_PROVIDER___RUN_COMPLIANCE_CHECKS__STRING_LIST_STRING = eINSTANCE.getWorkflowComplianceProvider__RunComplianceChecks__String_List_String();

		/**
		 * The meta object literal for the '<em><b>Get Compliance Status</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_COMPLIANCE_PROVIDER___GET_COMPLIANCE_STATUS__STRING = eINSTANCE.getWorkflowComplianceProvider__GetComplianceStatus__String();

		/**
		 * The meta object literal for the '<em><b>Set Governance Documentation Draft</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_DRAFT__STRING_STRING_STRING = eINSTANCE.getWorkflowComplianceProvider__SetGovernanceDocumentationDraft__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Set Governance Documentation In Review</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_IN_REVIEW__STRING_STRING_STRING = eINSTANCE.getWorkflowComplianceProvider__SetGovernanceDocumentationInReview__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Set Governance Documentation Approved</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_APPROVED__STRING_STRING_STRING = eINSTANCE.getWorkflowComplianceProvider__SetGovernanceDocumentationApproved__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Set Governance Documentation Rejected</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WORKFLOW_COMPLIANCE_PROVIDER___SET_GOVERNANCE_DOCUMENTATION_REJECTED__STRING_STRING_STRING = eINSTANCE.getWorkflowComplianceProvider__SetGovernanceDocumentationRejected__String_String_String();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService <em>Governance Documentation Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.GovernanceDocumentationService
		 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.impl.ManagementApiPackageImpl#getGovernanceDocumentationService()
		 * @generated
		 */
		EClass GOVERNANCE_DOCUMENTATION_SERVICE = eINSTANCE.getGovernanceDocumentationService();

		/**
		 * The meta object literal for the '<em><b>Store Documentation</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_DOCUMENTATION_SERVICE___STORE_DOCUMENTATION__STRING_GOVERNANCEDOCUMENTATION_STRING_STRING = eINSTANCE.getGovernanceDocumentationService__StoreDocumentation__String_GovernanceDocumentation_String_String();

		/**
		 * The meta object literal for the '<em><b>Get Latest Documentation</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_DOCUMENTATION_SERVICE___GET_LATEST_DOCUMENTATION__STRING = eINSTANCE.getGovernanceDocumentationService__GetLatestDocumentation__String();

		/**
		 * The meta object literal for the '<em><b>Get Documentation</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_DOCUMENTATION_SERVICE___GET_DOCUMENTATION__STRING = eINSTANCE.getGovernanceDocumentationService__GetDocumentation__String();

		/**
		 * The meta object literal for the '<em><b>Get Documentation History</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_DOCUMENTATION_SERVICE___GET_DOCUMENTATION_HISTORY__STRING = eINSTANCE.getGovernanceDocumentationService__GetDocumentationHistory__String();

		/**
		 * The meta object literal for the '<em><b>Has Documentation</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_DOCUMENTATION_SERVICE___HAS_DOCUMENTATION__STRING = eINSTANCE.getGovernanceDocumentationService__HasDocumentation__String();

		/**
		 * The meta object literal for the '<em><b>Delete All Documentation</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_DOCUMENTATION_SERVICE___DELETE_ALL_DOCUMENTATION__STRING = eINSTANCE.getGovernanceDocumentationService__DeleteAllDocumentation__String();

		/**
		 * The meta object literal for the '<em><b>Get Documentation Statistics</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GOVERNANCE_DOCUMENTATION_SERVICE___GET_DOCUMENTATION_STATISTICS = eINSTANCE.getGovernanceDocumentationService__GetDocumentationStatistics();

	}

} //ManagementApiPackage
