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

import org.gecko.mac.governance.ComplianceStatus;
import org.gecko.mac.governance.GovernanceDocumentation;
import org.gecko.mac.governance.PolicyType;
import org.gecko.mac.governance.SystemComponent;
import org.gecko.mac.governance.SystemComponentType;
import org.gecko.mac.governance.TrustLevel;

import org.osgi.annotation.versioning.ProviderType;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>System Component Governance Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Service for managing governance of system components within a specific GovernanceSystem. Each service instance is scoped to a single system for isolation and security.
 * <!-- end-model-doc -->
 *
 *
 * @see org.gecko.mac.mgmt.governanceapi.ManagementApiPackage#getSystemComponentGovernanceService()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface SystemComponentGovernanceService {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Register a new system component in the governance framework
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.gecko.mac.governance.SystemComponent&gt;" componentIdRequired="true" componentTypeRequired="true" nameRequired="true"
	 * @generated
	 */
	Promise<SystemComponent> registerSystemComponent(String componentId, SystemComponentType componentType, String name, String description);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Update the trust level of a system component
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.gecko.mac.governance.SystemComponent&gt;" componentIdRequired="true" trustLevelRequired="true"
	 * @generated
	 */
	Promise<SystemComponent> updateSystemComponentTrustLevel(String componentId, TrustLevel trustLevel);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Run compliance checks on a system component for specified policies
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.gecko.mac.governance.GovernanceDocumentation&gt;" componentIdRequired="true" policyTypesMany="true" reviewUserRequired="true"
	 * @generated
	 */
	Promise<GovernanceDocumentation> runSystemComponentCompliance(String componentId, List<PolicyType> policyTypes, String reviewUser);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Apply policies to a system component
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.gecko.mac.mgmt.management.Void&gt;" componentIdRequired="true" policyIdsMany="true"
	 * @generated
	 */
	Promise<Void> applyPoliciesToSystemComponent(String componentId, List<String> policyIds);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get system component by ID
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.gecko.mac.governance.SystemComponent&gt;" componentIdRequired="true"
	 * @generated
	 */
	Promise<SystemComponent> getSystemComponent(String componentId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find system components by governance compliance status
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.gecko.mac.mgmt.management.List&lt;org.gecko.mac.governance.SystemComponent&gt;&gt;" complianceStatusRequired="true"
	 * @generated
	 */
	Promise<List<SystemComponent>> findSystemComponentsByGovernanceStatus(ComplianceStatus complianceStatus);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Link an EObject (like generated EPackage) to its source system component
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.gecko.mac.mgmt.management.Void&gt;" objectIdRequired="true" componentIdRequired="true"
	 * @generated
	 */
	Promise<Void> linkModelToSystemComponent(String objectId, String componentId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get all system components of a specific type
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.gecko.mac.mgmt.management.List&lt;org.gecko.mac.governance.SystemComponent&gt;&gt;" componentTypeRequired="true"
	 * @generated
	 */
	Promise<List<SystemComponent>> getSystemComponentsByType(SystemComponentType componentType);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get the system ID this service instance is managing
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getSystemId();

} // SystemComponentGovernanceService
