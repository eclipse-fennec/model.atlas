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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.wf.workflowapi;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage;

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
 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = WorkflowApiPackage.eNS_URI, fingerprint = "fp1:6075e2f1f13614ca70e92715c08d59cf66dc233c2c0a5eee8fd27728715c1883", genModel = "/model/workflow-api.genmodel", genModelSourceLocations = {"model/workflow-api.genmodel","org.eclipse.fennec.model.atlas.workflow/model/workflow-api.genmodel"}, ecore = "/model/workflow-api.ecore", ecoreSourceLocations = "/model/workflow-api.ecore")
public interface WorkflowApiPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "workflowapi";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/workflow/api/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "workflowapi";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	WorkflowApiPackage eINSTANCE = org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService <em>EObject Workflow Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getEObjectWorkflowService()
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
	int EOBJECT_WORKFLOW_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Upload To Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPLOAD_TO_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_OBJECTMETADATA = 0;

	/**
	 * The operation id for the '<em>Get From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = 1;

	/**
	 * The operation id for the '<em>Get From Final Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING = 2;

	/**
	 * The operation id for the '<em>Get Content From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___GET_CONTENT_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = 3;

	/**
	 * The operation id for the '<em>Update In Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___UPDATE_IN_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_STRING_STRING = 4;

	/**
	 * The operation id for the '<em>Delete From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___DELETE_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = 5;

	/**
	 * The operation id for the '<em>List In Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE_FOR_REGISTRY__STRING_STRING = 6;

	/**
	 * The operation id for the '<em>List In Stage For Registry By Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE_FOR_REGISTRY_BY_NAME__STRING_STRING_STRING = 7;

	/**
	 * The operation id for the '<em>List In Final Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___LIST_IN_FINAL_STAGE_FOR_REGISTRY__STRING = 8;

	/**
	 * The operation id for the '<em>Transition To Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___TRANSITION_TO_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING = 9;

	/**
	 * The operation id for the '<em>Is Transition Allowed</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING = 10;

	/**
	 * The number of operations of the '<em>EObject Workflow Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EOBJECT_WORKFLOW_SERVICE_OPERATION_COUNT = 11;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService <em>Registry Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getRegistryService()
	 * @generated
	 */
	int REGISTRY_SERVICE = 1;

	/**
	 * The number of structural features of the '<em>Registry Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE_FEATURE_COUNT = 0;

	/**
	 * The operation id for the '<em>Update Properties</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___UPDATE_PROPERTIES__STRING_STRING_STRING_MAP = 0;

	/**
	 * The operation id for the '<em>Upload To Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___UPLOAD_TO_STAGE__STRING_STRING_EOBJECT_OBJECTMETADATA = 1;

	/**
	 * The operation id for the '<em>Get Metadata From Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___GET_METADATA_FROM_STAGE__STRING_STRING_STRING = 2;

	/**
	 * The operation id for the '<em>Get Metadata From Final Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___GET_METADATA_FROM_FINAL_STAGE__STRING_STRING = 3;

	/**
	 * The operation id for the '<em>Get Content From Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___GET_CONTENT_FROM_STAGE__STRING_STRING_STRING = 4;

	/**
	 * The operation id for the '<em>Get Content From Final Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___GET_CONTENT_FROM_FINAL_STAGE__STRING_STRING = 5;

	/**
	 * The operation id for the '<em>Update In Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___UPDATE_IN_STAGE__STRING_STRING_EOBJECT_STRING_STRING = 6;

	/**
	 * The operation id for the '<em>Delete From Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___DELETE_FROM_STAGE__STRING_STRING_STRING = 7;

	/**
	 * The operation id for the '<em>List In Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___LIST_IN_STAGE__STRING_STRING = 8;

	/**
	 * The operation id for the '<em>List In Stage By Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___LIST_IN_STAGE_BY_NAME__STRING_STRING_STRING = 9;

	/**
	 * The operation id for the '<em>List In Final Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___LIST_IN_FINAL_STAGE__STRING = 10;

	/**
	 * The operation id for the '<em>List All</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___LIST_ALL__STRING = 11;

	/**
	 * The operation id for the '<em>Transition To Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___TRANSITION_TO_STAGE__STRING_STRING_STRING_STRING = 12;

	/**
	 * The operation id for the '<em>Get Registry Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___GET_REGISTRY_NAME = 13;

	/**
	 * The operation id for the '<em>Is Valid Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___IS_VALID_STAGE__STRING = 14;

	/**
	 * The operation id for the '<em>Is Writable Stage</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___IS_WRITABLE_STAGE__STRING = 15;

	/**
	 * The operation id for the '<em>Is Final Stage Writable</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___IS_FINAL_STAGE_WRITABLE = 16;

	/**
	 * The operation id for the '<em>Is Transition Allowed</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING = 17;

	/**
	 * The operation id for the '<em>Get Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___GET_REGISTRY = 18;

	/**
	 * The operation id for the '<em>Is EClass Compatible With Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___IS_ECLASS_COMPATIBLE_WITH_REGISTRY__ECLASS = 19;

	/**
	 * The operation id for the '<em>Get Root EClass</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___GET_ROOT_ECLASS = 20;

	/**
	 * The operation id for the '<em>Activate</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___ACTIVATE__STRING = 21;

	/**
	 * The operation id for the '<em>Deactivate</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE___DEACTIVATE__STRING = 22;

	/**
	 * The number of operations of the '<em>Registry Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_SERVICE_OPERATION_COUNT = 23;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService <em>Writable Scope Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getWritableScopeService()
	 * @generated
	 */
	int WRITABLE_SCOPE_SERVICE = 6;

	/**
	 * The number of structural features of the '<em>Writable Scope Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE_FEATURE_COUNT = ScopeApiPackage.READABLE_SCOPE_SERVICE_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Scope Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___GET_SCOPE_NAME = ScopeApiPackage.READABLE_SCOPE_SERVICE___GET_SCOPE_NAME;

	/**
	 * The operation id for the '<em>Is Inheriting From Parent Scope</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___IS_INHERITING_FROM_PARENT_SCOPE = ScopeApiPackage.READABLE_SCOPE_SERVICE___IS_INHERITING_FROM_PARENT_SCOPE;

	/**
	 * The operation id for the '<em>Get</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___GET__STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE___GET__STRING_STRING;

	/**
	 * The operation id for the '<em>List Object Ids</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___LIST_OBJECT_IDS__STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE___LIST_OBJECT_IDS__STRING;

	/**
	 * The operation id for the '<em>List All</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___LIST_ALL__STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE___LIST_ALL__STRING;

	/**
	 * The operation id for the '<em>Stream</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___STREAM__STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE___STREAM__STRING;

	/**
	 * The operation id for the '<em>Get Scope Info</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___GET_SCOPE_INFO = ScopeApiPackage.READABLE_SCOPE_SERVICE___GET_SCOPE_INFO;

	/**
	 * The operation id for the '<em>Registry View</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING;

	/**
	 * The operation id for the '<em>Registry View</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING_STRING;

	/**
	 * The operation id for the '<em>Update Properties In Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___UPDATE_PROPERTIES_IN_STAGE_FOR_REGISTRY__STRING_STRING_STRING_MAP = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Upload To Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___UPLOAD_TO_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_OBJECTMETADATA = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Metadata From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___GET_METADATA_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 2;

	/**
	 * The operation id for the '<em>Get Metadata From Final Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___GET_METADATA_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Metadata By Property From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___GET_METADATA_BY_PROPERTY_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 4;

	/**
	 * The operation id for the '<em>Get Metadata By Property From Final Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___GET_METADATA_BY_PROPERTY_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 5;

	/**
	 * The operation id for the '<em>Get Content From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___GET_CONTENT_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 6;

	/**
	 * The operation id for the '<em>Update In Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___UPDATE_IN_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 7;

	/**
	 * The operation id for the '<em>Delete From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___DELETE_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 8;

	/**
	 * The operation id for the '<em>List In Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY__STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 9;

	/**
	 * The operation id for the '<em>List In Stage For Registry By Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY_BY_NAME__STRING_STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 10;

	/**
	 * The operation id for the '<em>List In Final Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___LIST_IN_FINAL_STAGE_FOR_REGISTRY__STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 11;

	/**
	 * The operation id for the '<em>List All For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___LIST_ALL_FOR_REGISTRY__STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 12;

	/**
	 * The operation id for the '<em>Transition To Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___TRANSITION_TO_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 13;

	/**
	 * The operation id for the '<em>Is Valid Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___IS_VALID_REGISTRY__STRING = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 14;

	/**
	 * The operation id for the '<em>Get All Registries</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___GET_ALL_REGISTRIES = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 15;

	/**
	 * The operation id for the '<em>Get Scope</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE___GET_SCOPE = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 16;

	/**
	 * The number of operations of the '<em>Writable Scope Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WRITABLE_SCOPE_SERVICE_OPERATION_COUNT = ScopeApiPackage.READABLE_SCOPE_SERVICE_OPERATION_COUNT + 17;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService <em>Scope Service</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getScopeService()
	 * @deprecated See {@link ScopeService model documentation} for details.
	 * @generated
	 */
	@Deprecated
	int SCOPE_SERVICE = 2;

	/**
	 * The number of structural features of the '<em>Scope Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated See {@link ScopeService model documentation} for details.
	 * @generated
	 * @ordered
	 */
	@Deprecated
	int SCOPE_SERVICE_FEATURE_COUNT = WRITABLE_SCOPE_SERVICE_FEATURE_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Scope Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___GET_SCOPE_NAME = WRITABLE_SCOPE_SERVICE___GET_SCOPE_NAME;

	/**
	 * The operation id for the '<em>Is Inheriting From Parent Scope</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___IS_INHERITING_FROM_PARENT_SCOPE = WRITABLE_SCOPE_SERVICE___IS_INHERITING_FROM_PARENT_SCOPE;

	/**
	 * The operation id for the '<em>Get</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___GET__STRING_STRING = WRITABLE_SCOPE_SERVICE___GET__STRING_STRING;

	/**
	 * The operation id for the '<em>List Object Ids</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___LIST_OBJECT_IDS__STRING = WRITABLE_SCOPE_SERVICE___LIST_OBJECT_IDS__STRING;

	/**
	 * The operation id for the '<em>List All</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___LIST_ALL__STRING = WRITABLE_SCOPE_SERVICE___LIST_ALL__STRING;

	/**
	 * The operation id for the '<em>Stream</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___STREAM__STRING = WRITABLE_SCOPE_SERVICE___STREAM__STRING;

	/**
	 * The operation id for the '<em>Get Scope Info</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___GET_SCOPE_INFO = WRITABLE_SCOPE_SERVICE___GET_SCOPE_INFO;

	/**
	 * The operation id for the '<em>Registry View</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___REGISTRY_VIEW__STRING = WRITABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING;

	/**
	 * The operation id for the '<em>Registry View</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___REGISTRY_VIEW__STRING_STRING = WRITABLE_SCOPE_SERVICE___REGISTRY_VIEW__STRING_STRING;

	/**
	 * The operation id for the '<em>Update Properties In Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___UPDATE_PROPERTIES_IN_STAGE_FOR_REGISTRY__STRING_STRING_STRING_MAP = WRITABLE_SCOPE_SERVICE___UPDATE_PROPERTIES_IN_STAGE_FOR_REGISTRY__STRING_STRING_STRING_MAP;

	/**
	 * The operation id for the '<em>Upload To Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___UPLOAD_TO_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_OBJECTMETADATA = WRITABLE_SCOPE_SERVICE___UPLOAD_TO_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_OBJECTMETADATA;

	/**
	 * The operation id for the '<em>Get Metadata From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___GET_METADATA_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = WRITABLE_SCOPE_SERVICE___GET_METADATA_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING;

	/**
	 * The operation id for the '<em>Get Metadata From Final Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___GET_METADATA_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING = WRITABLE_SCOPE_SERVICE___GET_METADATA_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING;

	/**
	 * The operation id for the '<em>Get Metadata By Property From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___GET_METADATA_BY_PROPERTY_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING = WRITABLE_SCOPE_SERVICE___GET_METADATA_BY_PROPERTY_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING;

	/**
	 * The operation id for the '<em>Get Metadata By Property From Final Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___GET_METADATA_BY_PROPERTY_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING_STRING = WRITABLE_SCOPE_SERVICE___GET_METADATA_BY_PROPERTY_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING_STRING;

	/**
	 * The operation id for the '<em>Get Content From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___GET_CONTENT_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = WRITABLE_SCOPE_SERVICE___GET_CONTENT_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING;

	/**
	 * The operation id for the '<em>Update In Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___UPDATE_IN_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_STRING_STRING = WRITABLE_SCOPE_SERVICE___UPDATE_IN_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_STRING_STRING;

	/**
	 * The operation id for the '<em>Delete From Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___DELETE_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = WRITABLE_SCOPE_SERVICE___DELETE_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING;

	/**
	 * The operation id for the '<em>List In Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY__STRING_STRING = WRITABLE_SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY__STRING_STRING;

	/**
	 * The operation id for the '<em>List In Stage For Registry By Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY_BY_NAME__STRING_STRING_STRING = WRITABLE_SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY_BY_NAME__STRING_STRING_STRING;

	/**
	 * The operation id for the '<em>List In Final Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___LIST_IN_FINAL_STAGE_FOR_REGISTRY__STRING = WRITABLE_SCOPE_SERVICE___LIST_IN_FINAL_STAGE_FOR_REGISTRY__STRING;

	/**
	 * The operation id for the '<em>List All For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___LIST_ALL_FOR_REGISTRY__STRING = WRITABLE_SCOPE_SERVICE___LIST_ALL_FOR_REGISTRY__STRING;

	/**
	 * The operation id for the '<em>Transition To Stage For Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___TRANSITION_TO_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING = WRITABLE_SCOPE_SERVICE___TRANSITION_TO_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING;

	/**
	 * The operation id for the '<em>Is Valid Registry</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___IS_VALID_REGISTRY__STRING = WRITABLE_SCOPE_SERVICE___IS_VALID_REGISTRY__STRING;

	/**
	 * The operation id for the '<em>Get All Registries</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___GET_ALL_REGISTRIES = WRITABLE_SCOPE_SERVICE___GET_ALL_REGISTRIES;

	/**
	 * The operation id for the '<em>Get Scope</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_SERVICE___GET_SCOPE = WRITABLE_SCOPE_SERVICE___GET_SCOPE;

	/**
	 * The number of operations of the '<em>Scope Service</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated See {@link ScopeService model documentation} for details.
	 * @generated
	 * @ordered
	 */
	@Deprecated
	int SCOPE_SERVICE_OPERATION_COUNT = WRITABLE_SCOPE_SERVICE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.RegistryImpl <em>Registry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.RegistryImpl
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getRegistry()
	 * @generated
	 */
	int REGISTRY = 3;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY__NAME = ScopeApiPackage.REGISTRY_INFO__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY__DESCRIPTION = ScopeApiPackage.REGISTRY_INFO__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY__TYPE = ScopeApiPackage.REGISTRY_INFO__TYPE;

	/**
	 * The feature id for the '<em><b>Stages</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY__STAGES = ScopeApiPackage.REGISTRY_INFO__STAGES;

	/**
	 * The feature id for the '<em><b>Allowed Transitions</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY__ALLOWED_TRANSITIONS = ScopeApiPackage.REGISTRY_INFO_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Registry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_FEATURE_COUNT = ScopeApiPackage.REGISTRY_INFO_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Registry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REGISTRY_OPERATION_COUNT = ScopeApiPackage.REGISTRY_INFO_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.ScopeImpl <em>Scope</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.ScopeImpl
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getScope()
	 * @generated
	 */
	int SCOPE = 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE__NAME = ScopeApiPackage.SCOPE_INFO__NAME;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE__DESCRIPTION = ScopeApiPackage.SCOPE_INFO__DESCRIPTION;

	/**
	 * The feature id for the '<em><b>Parent Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE__PARENT_SCOPE = ScopeApiPackage.SCOPE_INFO__PARENT_SCOPE;

	/**
	 * The feature id for the '<em><b>Registries</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE__REGISTRIES = ScopeApiPackage.SCOPE_INFO__REGISTRIES;

	/**
	 * The number of structural features of the '<em>Scope</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_FEATURE_COUNT = ScopeApiPackage.SCOPE_INFO_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Scope</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SCOPE_OPERATION_COUNT = ScopeApiPackage.SCOPE_INFO_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.StageTransitionImpl <em>Stage Transition</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.StageTransitionImpl
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getStageTransition()
	 * @generated
	 */
	int STAGE_TRANSITION = 5;

	/**
	 * The feature id for the '<em><b>From Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_TRANSITION__FROM_STAGE = 0;

	/**
	 * The feature id for the '<em><b>To Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_TRANSITION__TO_STAGE = 1;

	/**
	 * The number of structural features of the '<em>Stage Transition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_TRANSITION_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Stage Transition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int STAGE_TRANSITION_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService <em>EObject Workflow Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>EObject Workflow Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService
	 * @generated
	 */
	EClass getEObjectWorkflowService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#uploadToStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata) <em>Upload To Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Upload To Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#uploadToStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__UploadToStageForRegistry__String_String_EObject_ObjectMetadata();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String) <em>Get From Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get From Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__GetFromStageForRegistry__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromFinalStageForRegistry(java.lang.String, java.lang.String) <em>Get From Final Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get From Final Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getFromFinalStageForRegistry(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__GetFromFinalStageForRegistry__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getContentFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String) <em>Get Content From Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Content From Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#getContentFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__GetContentFromStageForRegistry__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#updateInStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String) <em>Update In Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update In Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#updateInStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__UpdateInStageForRegistry__String_String_EObject_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#deleteFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String) <em>Delete From Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete From Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#deleteFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__DeleteFromStageForRegistry__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageForRegistry(java.lang.String, java.lang.String) <em>List In Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageForRegistry(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListInStageForRegistry__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageForRegistryByName(java.lang.String, java.lang.String, java.lang.String) <em>List In Stage For Registry By Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Stage For Registry By Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageForRegistryByName(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListInStageForRegistryByName__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInFinalStageForRegistry(java.lang.String) <em>List In Final Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Final Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInFinalStageForRegistry(java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__ListInFinalStageForRegistry__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#transitionToStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.lang.String) <em>Transition To Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Transition To Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#transitionToStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__TransitionToStageForRegistry__String_String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#isTransitionAllowed(java.lang.String, java.lang.String) <em>Is Transition Allowed</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Transition Allowed</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#isTransitionAllowed(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getEObjectWorkflowService__IsTransitionAllowed__String_String();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService <em>Registry Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Registry Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService
	 * @generated
	 */
	EClass getRegistryService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#updateProperties(java.lang.String, java.lang.String, java.lang.String, java.util.Map) <em>Update Properties</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Properties</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#updateProperties(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
	 * @generated
	 */
	EOperation getRegistryService__UpdateProperties__String_String_String_Map();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#uploadToStage(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata) <em>Upload To Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Upload To Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#uploadToStage(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 * @generated
	 */
	EOperation getRegistryService__UploadToStage__String_String_EObject_ObjectMetadata();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getMetadataFromStage(java.lang.String, java.lang.String, java.lang.String) <em>Get Metadata From Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Metadata From Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getMetadataFromStage(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__GetMetadataFromStage__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getMetadataFromFinalStage(java.lang.String, java.lang.String) <em>Get Metadata From Final Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Metadata From Final Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getMetadataFromFinalStage(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__GetMetadataFromFinalStage__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getContentFromStage(java.lang.String, java.lang.String, java.lang.String) <em>Get Content From Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Content From Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getContentFromStage(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__GetContentFromStage__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getContentFromFinalStage(java.lang.String, java.lang.String) <em>Get Content From Final Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Content From Final Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getContentFromFinalStage(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__GetContentFromFinalStage__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#updateInStage(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String) <em>Update In Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update In Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#updateInStage(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__UpdateInStage__String_String_EObject_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#deleteFromStage(java.lang.String, java.lang.String, java.lang.String) <em>Delete From Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete From Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#deleteFromStage(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__DeleteFromStage__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInStage(java.lang.String, java.lang.String) <em>List In Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInStage(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__ListInStage__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInStageByName(java.lang.String, java.lang.String, java.lang.String) <em>List In Stage By Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Stage By Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInStageByName(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__ListInStageByName__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInFinalStage(java.lang.String) <em>List In Final Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Final Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInFinalStage(java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__ListInFinalStage__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listAll(java.lang.String) <em>List All</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List All</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listAll(java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__ListAll__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#transitionToStage(java.lang.String, java.lang.String, java.lang.String, java.lang.String) <em>Transition To Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Transition To Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#transitionToStage(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__TransitionToStage__String_String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRegistryName() <em>Get Registry Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Registry Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRegistryName()
	 * @generated
	 */
	EOperation getRegistryService__GetRegistryName();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isValidStage(java.lang.String) <em>Is Valid Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Valid Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isValidStage(java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__IsValidStage__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isWritableStage(java.lang.String) <em>Is Writable Stage</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Writable Stage</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isWritableStage(java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__IsWritableStage__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isFinalStageWritable() <em>Is Final Stage Writable</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Final Stage Writable</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isFinalStageWritable()
	 * @generated
	 */
	EOperation getRegistryService__IsFinalStageWritable();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isTransitionAllowed(java.lang.String, java.lang.String) <em>Is Transition Allowed</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Transition Allowed</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isTransitionAllowed(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__IsTransitionAllowed__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRegistry() <em>Get Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRegistry()
	 * @generated
	 */
	EOperation getRegistryService__GetRegistry();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isEClassCompatibleWithRegistry(org.eclipse.emf.ecore.EClass) <em>Is EClass Compatible With Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is EClass Compatible With Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isEClassCompatibleWithRegistry(org.eclipse.emf.ecore.EClass)
	 * @generated
	 */
	EOperation getRegistryService__IsEClassCompatibleWithRegistry__EClass();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRootEClass() <em>Get Root EClass</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Root EClass</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRootEClass()
	 * @generated
	 */
	EOperation getRegistryService__GetRootEClass();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#activate(java.lang.String) <em>Activate</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Activate</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#activate(java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__Activate__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#deactivate(java.lang.String) <em>Deactivate</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Deactivate</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#deactivate(java.lang.String)
	 * @generated
	 */
	EOperation getRegistryService__Deactivate__String();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService <em>Scope Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Scope Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService
	 * @deprecated See {@link ScopeService model documentation} for details.
	 * @generated
	 */
	@Deprecated
	EClass getScopeService();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry <em>Registry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Registry</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.Registry
	 * @generated
	 */
	EClass getRegistry();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry#getAllowedTransitions <em>Allowed Transitions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Allowed Transitions</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.Registry#getAllowedTransitions()
	 * @see #getRegistry()
	 * @generated
	 */
	EReference getRegistry_AllowedTransitions();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Scope <em>Scope</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Scope</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.Scope
	 * @generated
	 */
	EClass getScope();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition <em>Stage Transition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Stage Transition</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition
	 * @generated
	 */
	EClass getStageTransition();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition#getFromStage <em>From Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>From Stage</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition#getFromStage()
	 * @see #getStageTransition()
	 * @generated
	 */
	EAttribute getStageTransition_FromStage();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition#getToStage <em>To Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>To Stage</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition#getToStage()
	 * @see #getStageTransition()
	 * @generated
	 */
	EAttribute getStageTransition_ToStage();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService <em>Writable Scope Service</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Writable Scope Service</em>'.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService
	 * @generated
	 */
	EClass getWritableScopeService();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#updatePropertiesInStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.util.Map) <em>Update Properties In Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update Properties In Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#updatePropertiesInStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
	 * @generated
	 */
	EOperation getWritableScopeService__UpdatePropertiesInStageForRegistry__String_String_String_Map();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#uploadToStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata) <em>Upload To Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Upload To Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#uploadToStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
	 * @generated
	 */
	EOperation getWritableScopeService__UploadToStageForRegistry__String_String_EObject_ObjectMetadata();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getMetadataFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String) <em>Get Metadata From Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Metadata From Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getMetadataFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__GetMetadataFromStageForRegistry__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getMetadataFromFinalStageForRegistry(java.lang.String, java.lang.String) <em>Get Metadata From Final Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Metadata From Final Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getMetadataFromFinalStageForRegistry(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__GetMetadataFromFinalStageForRegistry__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getMetadataByPropertyFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.lang.String) <em>Get Metadata By Property From Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Metadata By Property From Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getMetadataByPropertyFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__GetMetadataByPropertyFromStageForRegistry__String_String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getMetadataByPropertyFromFinalStageForRegistry(java.lang.String, java.lang.String, java.lang.String) <em>Get Metadata By Property From Final Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Metadata By Property From Final Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getMetadataByPropertyFromFinalStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__GetMetadataByPropertyFromFinalStageForRegistry__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getContentFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String) <em>Get Content From Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Content From Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getContentFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__GetContentFromStageForRegistry__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#updateInStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String) <em>Update In Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Update In Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#updateInStageForRegistry(java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__UpdateInStageForRegistry__String_String_EObject_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#deleteFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String) <em>Delete From Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Delete From Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#deleteFromStageForRegistry(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__DeleteFromStageForRegistry__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#listInStageForRegistry(java.lang.String, java.lang.String) <em>List In Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#listInStageForRegistry(java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__ListInStageForRegistry__String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#listInStageForRegistryByName(java.lang.String, java.lang.String, java.lang.String) <em>List In Stage For Registry By Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Stage For Registry By Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#listInStageForRegistryByName(java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__ListInStageForRegistryByName__String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#listInFinalStageForRegistry(java.lang.String) <em>List In Final Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List In Final Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#listInFinalStageForRegistry(java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__ListInFinalStageForRegistry__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#listAllForRegistry(java.lang.String) <em>List All For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>List All For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#listAllForRegistry(java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__ListAllForRegistry__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#transitionToStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.lang.String) <em>Transition To Stage For Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Transition To Stage For Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#transitionToStageForRegistry(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__TransitionToStageForRegistry__String_String_String_String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#isValidRegistry(java.lang.String) <em>Is Valid Registry</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Valid Registry</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#isValidRegistry(java.lang.String)
	 * @generated
	 */
	EOperation getWritableScopeService__IsValidRegistry__String();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getAllRegistries() <em>Get All Registries</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get All Registries</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getAllRegistries()
	 * @generated
	 */
	EOperation getWritableScopeService__GetAllRegistries();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getScope() <em>Get Scope</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Scope</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService#getScope()
	 * @generated
	 */
	EOperation getWritableScopeService__GetScope();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	WorkflowApiFactory getWorkflowApiFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService <em>EObject Workflow Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getEObjectWorkflowService()
		 * @generated
		 */
		EClass EOBJECT_WORKFLOW_SERVICE = eINSTANCE.getEObjectWorkflowService();

		/**
		 * The meta object literal for the '<em><b>Upload To Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___UPLOAD_TO_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_OBJECTMETADATA = eINSTANCE.getEObjectWorkflowService__UploadToStageForRegistry__String_String_EObject_ObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>Get From Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___GET_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__GetFromStageForRegistry__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Get From Final Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___GET_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING = eINSTANCE.getEObjectWorkflowService__GetFromFinalStageForRegistry__String_String();

		/**
		 * The meta object literal for the '<em><b>Get Content From Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___GET_CONTENT_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__GetContentFromStageForRegistry__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Update In Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___UPDATE_IN_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_STRING_STRING = eINSTANCE.getEObjectWorkflowService__UpdateInStageForRegistry__String_String_EObject_String_String();

		/**
		 * The meta object literal for the '<em><b>Delete From Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___DELETE_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__DeleteFromStageForRegistry__String_String_String();

		/**
		 * The meta object literal for the '<em><b>List In Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE_FOR_REGISTRY__STRING_STRING = eINSTANCE.getEObjectWorkflowService__ListInStageForRegistry__String_String();

		/**
		 * The meta object literal for the '<em><b>List In Stage For Registry By Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_IN_STAGE_FOR_REGISTRY_BY_NAME__STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__ListInStageForRegistryByName__String_String_String();

		/**
		 * The meta object literal for the '<em><b>List In Final Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___LIST_IN_FINAL_STAGE_FOR_REGISTRY__STRING = eINSTANCE.getEObjectWorkflowService__ListInFinalStageForRegistry__String();

		/**
		 * The meta object literal for the '<em><b>Transition To Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___TRANSITION_TO_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING = eINSTANCE.getEObjectWorkflowService__TransitionToStageForRegistry__String_String_String_String();

		/**
		 * The meta object literal for the '<em><b>Is Transition Allowed</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation EOBJECT_WORKFLOW_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING = eINSTANCE.getEObjectWorkflowService__IsTransitionAllowed__String_String();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService <em>Registry Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getRegistryService()
		 * @generated
		 */
		EClass REGISTRY_SERVICE = eINSTANCE.getRegistryService();

		/**
		 * The meta object literal for the '<em><b>Update Properties</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___UPDATE_PROPERTIES__STRING_STRING_STRING_MAP = eINSTANCE.getRegistryService__UpdateProperties__String_String_String_Map();

		/**
		 * The meta object literal for the '<em><b>Upload To Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___UPLOAD_TO_STAGE__STRING_STRING_EOBJECT_OBJECTMETADATA = eINSTANCE.getRegistryService__UploadToStage__String_String_EObject_ObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>Get Metadata From Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___GET_METADATA_FROM_STAGE__STRING_STRING_STRING = eINSTANCE.getRegistryService__GetMetadataFromStage__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Get Metadata From Final Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___GET_METADATA_FROM_FINAL_STAGE__STRING_STRING = eINSTANCE.getRegistryService__GetMetadataFromFinalStage__String_String();

		/**
		 * The meta object literal for the '<em><b>Get Content From Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___GET_CONTENT_FROM_STAGE__STRING_STRING_STRING = eINSTANCE.getRegistryService__GetContentFromStage__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Get Content From Final Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___GET_CONTENT_FROM_FINAL_STAGE__STRING_STRING = eINSTANCE.getRegistryService__GetContentFromFinalStage__String_String();

		/**
		 * The meta object literal for the '<em><b>Update In Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___UPDATE_IN_STAGE__STRING_STRING_EOBJECT_STRING_STRING = eINSTANCE.getRegistryService__UpdateInStage__String_String_EObject_String_String();

		/**
		 * The meta object literal for the '<em><b>Delete From Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___DELETE_FROM_STAGE__STRING_STRING_STRING = eINSTANCE.getRegistryService__DeleteFromStage__String_String_String();

		/**
		 * The meta object literal for the '<em><b>List In Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___LIST_IN_STAGE__STRING_STRING = eINSTANCE.getRegistryService__ListInStage__String_String();

		/**
		 * The meta object literal for the '<em><b>List In Stage By Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___LIST_IN_STAGE_BY_NAME__STRING_STRING_STRING = eINSTANCE.getRegistryService__ListInStageByName__String_String_String();

		/**
		 * The meta object literal for the '<em><b>List In Final Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___LIST_IN_FINAL_STAGE__STRING = eINSTANCE.getRegistryService__ListInFinalStage__String();

		/**
		 * The meta object literal for the '<em><b>List All</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___LIST_ALL__STRING = eINSTANCE.getRegistryService__ListAll__String();

		/**
		 * The meta object literal for the '<em><b>Transition To Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___TRANSITION_TO_STAGE__STRING_STRING_STRING_STRING = eINSTANCE.getRegistryService__TransitionToStage__String_String_String_String();

		/**
		 * The meta object literal for the '<em><b>Get Registry Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___GET_REGISTRY_NAME = eINSTANCE.getRegistryService__GetRegistryName();

		/**
		 * The meta object literal for the '<em><b>Is Valid Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___IS_VALID_STAGE__STRING = eINSTANCE.getRegistryService__IsValidStage__String();

		/**
		 * The meta object literal for the '<em><b>Is Writable Stage</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___IS_WRITABLE_STAGE__STRING = eINSTANCE.getRegistryService__IsWritableStage__String();

		/**
		 * The meta object literal for the '<em><b>Is Final Stage Writable</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___IS_FINAL_STAGE_WRITABLE = eINSTANCE.getRegistryService__IsFinalStageWritable();

		/**
		 * The meta object literal for the '<em><b>Is Transition Allowed</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___IS_TRANSITION_ALLOWED__STRING_STRING = eINSTANCE.getRegistryService__IsTransitionAllowed__String_String();

		/**
		 * The meta object literal for the '<em><b>Get Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___GET_REGISTRY = eINSTANCE.getRegistryService__GetRegistry();

		/**
		 * The meta object literal for the '<em><b>Is EClass Compatible With Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___IS_ECLASS_COMPATIBLE_WITH_REGISTRY__ECLASS = eINSTANCE.getRegistryService__IsEClassCompatibleWithRegistry__EClass();

		/**
		 * The meta object literal for the '<em><b>Get Root EClass</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___GET_ROOT_ECLASS = eINSTANCE.getRegistryService__GetRootEClass();

		/**
		 * The meta object literal for the '<em><b>Activate</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___ACTIVATE__STRING = eINSTANCE.getRegistryService__Activate__String();

		/**
		 * The meta object literal for the '<em><b>Deactivate</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation REGISTRY_SERVICE___DEACTIVATE__STRING = eINSTANCE.getRegistryService__Deactivate__String();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService <em>Scope Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getScopeService()
		 * @deprecated See {@link ScopeService model documentation} for details.
		 * @generated
		 */
		@Deprecated
		EClass SCOPE_SERVICE = eINSTANCE.getScopeService();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.RegistryImpl <em>Registry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.RegistryImpl
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getRegistry()
		 * @generated
		 */
		EClass REGISTRY = eINSTANCE.getRegistry();

		/**
		 * The meta object literal for the '<em><b>Allowed Transitions</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference REGISTRY__ALLOWED_TRANSITIONS = eINSTANCE.getRegistry_AllowedTransitions();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.ScopeImpl <em>Scope</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.ScopeImpl
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getScope()
		 * @generated
		 */
		EClass SCOPE = eINSTANCE.getScope();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.StageTransitionImpl <em>Stage Transition</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.StageTransitionImpl
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getStageTransition()
		 * @generated
		 */
		EClass STAGE_TRANSITION = eINSTANCE.getStageTransition();

		/**
		 * The meta object literal for the '<em><b>From Stage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_TRANSITION__FROM_STAGE = eINSTANCE.getStageTransition_FromStage();

		/**
		 * The meta object literal for the '<em><b>To Stage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute STAGE_TRANSITION__TO_STAGE = eINSTANCE.getStageTransition_ToStage();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService <em>Writable Scope Service</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.impl.WorkflowApiPackageImpl#getWritableScopeService()
		 * @generated
		 */
		EClass WRITABLE_SCOPE_SERVICE = eINSTANCE.getWritableScopeService();

		/**
		 * The meta object literal for the '<em><b>Update Properties In Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___UPDATE_PROPERTIES_IN_STAGE_FOR_REGISTRY__STRING_STRING_STRING_MAP = eINSTANCE.getWritableScopeService__UpdatePropertiesInStageForRegistry__String_String_String_Map();

		/**
		 * The meta object literal for the '<em><b>Upload To Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___UPLOAD_TO_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_OBJECTMETADATA = eINSTANCE.getWritableScopeService__UploadToStageForRegistry__String_String_EObject_ObjectMetadata();

		/**
		 * The meta object literal for the '<em><b>Get Metadata From Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___GET_METADATA_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = eINSTANCE.getWritableScopeService__GetMetadataFromStageForRegistry__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Get Metadata From Final Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___GET_METADATA_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING = eINSTANCE.getWritableScopeService__GetMetadataFromFinalStageForRegistry__String_String();

		/**
		 * The meta object literal for the '<em><b>Get Metadata By Property From Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___GET_METADATA_BY_PROPERTY_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING = eINSTANCE.getWritableScopeService__GetMetadataByPropertyFromStageForRegistry__String_String_String_String();

		/**
		 * The meta object literal for the '<em><b>Get Metadata By Property From Final Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___GET_METADATA_BY_PROPERTY_FROM_FINAL_STAGE_FOR_REGISTRY__STRING_STRING_STRING = eINSTANCE.getWritableScopeService__GetMetadataByPropertyFromFinalStageForRegistry__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Get Content From Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___GET_CONTENT_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = eINSTANCE.getWritableScopeService__GetContentFromStageForRegistry__String_String_String();

		/**
		 * The meta object literal for the '<em><b>Update In Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___UPDATE_IN_STAGE_FOR_REGISTRY__STRING_STRING_EOBJECT_STRING_STRING = eINSTANCE.getWritableScopeService__UpdateInStageForRegistry__String_String_EObject_String_String();

		/**
		 * The meta object literal for the '<em><b>Delete From Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___DELETE_FROM_STAGE_FOR_REGISTRY__STRING_STRING_STRING = eINSTANCE.getWritableScopeService__DeleteFromStageForRegistry__String_String_String();

		/**
		 * The meta object literal for the '<em><b>List In Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY__STRING_STRING = eINSTANCE.getWritableScopeService__ListInStageForRegistry__String_String();

		/**
		 * The meta object literal for the '<em><b>List In Stage For Registry By Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___LIST_IN_STAGE_FOR_REGISTRY_BY_NAME__STRING_STRING_STRING = eINSTANCE.getWritableScopeService__ListInStageForRegistryByName__String_String_String();

		/**
		 * The meta object literal for the '<em><b>List In Final Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___LIST_IN_FINAL_STAGE_FOR_REGISTRY__STRING = eINSTANCE.getWritableScopeService__ListInFinalStageForRegistry__String();

		/**
		 * The meta object literal for the '<em><b>List All For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___LIST_ALL_FOR_REGISTRY__STRING = eINSTANCE.getWritableScopeService__ListAllForRegistry__String();

		/**
		 * The meta object literal for the '<em><b>Transition To Stage For Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___TRANSITION_TO_STAGE_FOR_REGISTRY__STRING_STRING_STRING_STRING = eINSTANCE.getWritableScopeService__TransitionToStageForRegistry__String_String_String_String();

		/**
		 * The meta object literal for the '<em><b>Is Valid Registry</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___IS_VALID_REGISTRY__STRING = eINSTANCE.getWritableScopeService__IsValidRegistry__String();

		/**
		 * The meta object literal for the '<em><b>Get All Registries</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___GET_ALL_REGISTRIES = eINSTANCE.getWritableScopeService__GetAllRegistries();

		/**
		 * The meta object literal for the '<em><b>Get Scope</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WRITABLE_SCOPE_SERVICE___GET_SCOPE = eINSTANCE.getWritableScopeService__GetScope();

	}

} //WorkflowApiPackage
