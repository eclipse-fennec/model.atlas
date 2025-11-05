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

import java.util.List;

import org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult;
import org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation;
import org.eclipse.fennec.model.atlas.governance.PolicyType;

import org.osgi.annotation.versioning.ProviderType;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Governance Compliance Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Service for running governance compliance checks and managing compliance documentation. This service orchestrates various policy-specific compliance checks and generates comprehensive governance documentation.
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.ManagementApiPackage#getGovernanceComplianceService()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface GovernanceComplianceService {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Run comprehensive compliance checks on an object for all applicable policies
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation&gt;" objectIdRequired="true" reviewUserRequired="true"
	 * @generated
	 */
	Promise<GovernanceDocumentation> runComplianceChecks(String objectId, String reviewUser, String complianceReason);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Run specific compliance checks for given policy types
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation&gt;" objectIdRequired="true" policyTypesMany="true" reviewUserRequired="true"
	 * @generated
	 */
	Promise<GovernanceDocumentation> runSpecificComplianceChecks(String objectId, List<PolicyType> policyTypes, String reviewUser);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Run compliance checks specifically for system components
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation&gt;" systemIdRequired="true" componentIdRequired="true" policyTypesMany="true" reviewUserRequired="true"
	 * @generated
	 */
	Promise<GovernanceDocumentation> runSystemComponentCompliance(String systemId, String componentId, List<PolicyType> policyTypes, String reviewUser);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get the current compliance status and documentation for an object
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation&gt;" objectIdRequired="true"
	 * @generated
	 */
	Promise<GovernanceDocumentation> getComplianceStatus(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Validate compliance for a specific policy without storing results
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult&gt;" objectIdRequired="true" policyTypeRequired="true" reviewUserRequired="true"
	 * @generated
	 */
	Promise<ComplianceCheckResult> validatePolicyCompliance(String objectId, PolicyType policyType, String reviewUser);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get list of all available policy types for compliance checking
	 * <!-- end-model-doc -->
	 * @model kind="operation" dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.governance.PolicyType&gt;" many="false"
	 * @generated
	 */
	List<PolicyType> getAvailablePolicies();

} // GovernanceComplianceService
