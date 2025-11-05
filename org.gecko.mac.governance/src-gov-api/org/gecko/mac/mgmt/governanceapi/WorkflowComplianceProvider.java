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
package org.gecko.mac.mgmt.governanceapi;

import java.util.List;

import org.gecko.mac.governance.GovernanceDocumentation;
import org.gecko.mac.governance.PolicyType;

import org.osgi.annotation.versioning.ProviderType;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Workflow Compliance Provider</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Generic orchestrator for EObject workflow management
 * <!-- end-model-doc -->
 *
 *
 * @see org.gecko.mac.mgmt.governanceapi.ManagementApiPackage#getWorkflowComplianceProvider()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface WorkflowComplianceProvider {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Run comprehensive governance compliance checks on an object, returns documentation with check results
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.gecko.mac.governance.GovernanceDocumentation&gt;" objectIdRequired="true" reviewUserRequired="true"
	 * @generated
	 */
	Promise<GovernanceDocumentation> checkObject(String objectId, String reviewUser, String complianceReason);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Run specific compliance checks on an object for given policy types
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.gecko.mac.governance.GovernanceDocumentation&gt;" objectIdRequired="true" policyTypesMany="true" reviewUserRequired="true"
	 * @generated
	 */
	Promise<GovernanceDocumentation> runComplianceChecks(String objectId, List<PolicyType> policyTypes, String reviewUser);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get the current compliance status for an object
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	GovernanceDocumentation getComplianceStatus(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Set governance documentation status to DRAFT with audit trail. Valid transitions: Any state → DRAFT (allows resetting to draft for rework). This operation is typically used when creating new governance documentation or when returning documentation for rework after rejection.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true" reviewUserRequired="true"
	 * @generated
	 */
	GovernanceDocumentation setGovernanceDocumentationDraft(String objectId, String reviewUser, String reason);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Set governance documentation status to IN_REVIEW with audit trail. Valid transitions: DRAFT → IN_REVIEW, REJECTED → IN_REVIEW (allows resubmission after addressing rejection feedback). Cannot transition from APPROVED to IN_REVIEW without first returning to DRAFT.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true" reviewUserRequired="true"
	 * @generated
	 */
	GovernanceDocumentation setGovernanceDocumentationInReview(String objectId, String reviewUser, String reason);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Set governance documentation status to APPROVED with audit trail. Valid transitions: IN_REVIEW → APPROVED (normal approval path). This is the terminal state that enables object approval in the governance-gated workflow. Once approved, governance documentation enables the parent object to be approved for release.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true" reviewUserRequired="true"
	 * @generated
	 */
	GovernanceDocumentation setGovernanceDocumentationApproved(String objectId, String reviewUser, String reason);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Set governance documentation status to REJECTED with audit trail. Valid transitions: IN_REVIEW → REJECTED (when compliance review fails). From REJECTED state, documentation can be returned to DRAFT for rework or resubmitted to IN_REVIEW after addressing issues. Rejected governance documentation blocks object approval until resolved.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true" reviewUserRequired="true"
	 * @generated
	 */
	GovernanceDocumentation setGovernanceDocumentationRejected(String objectId, String reviewUser, String reason);

} // WorkflowComplianceProvider
