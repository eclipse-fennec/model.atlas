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
import org.gecko.mac.governance.GovernanceSystem;
import org.gecko.mac.governance.SystemComponent;
import org.gecko.mac.governance.SystemComponentType;
import org.gecko.mac.governance.TrustLevel;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Governance System Repository</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Repository interface for managing governance systems and system components. This interface abstracts the storage and retrieval of governance-related entities.
 * <!-- end-model-doc -->
 *
 *
 * @see org.gecko.mac.mgmt.governanceapi.ManagementApiPackage#getGovernanceSystemRepository()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface GovernanceSystemRepository {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get a governance system by ID
	 * <!-- end-model-doc -->
	 * @model systemIdRequired="true"
	 * @generated
	 */
	GovernanceSystem getGovernanceSystem(String systemId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Create a new system component
	 * <!-- end-model-doc -->
	 * @model systemIdRequired="true" componentIdRequired="true" componentTypeRequired="true" nameRequired="true"
	 * @generated
	 */
	SystemComponent createSystemComponent(String systemId, String componentId, SystemComponentType componentType, String name, String description);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get a system component by ID within a specific system
	 * <!-- end-model-doc -->
	 * @model systemIdRequired="true" componentIdRequired="true"
	 * @generated
	 */
	SystemComponent getSystemComponent(String systemId, String componentId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Update the trust level of a system component
	 * <!-- end-model-doc -->
	 * @model systemIdRequired="true" componentIdRequired="true" trustLevelRequired="true"
	 * @generated
	 */
	SystemComponent updateSystemComponentTrustLevel(String systemId, String componentId, TrustLevel trustLevel);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Apply policies to a system component
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Void" systemIdRequired="true" componentIdRequired="true" policyIdsMany="true"
	 * @generated
	 */
	Void applyPoliciesToSystemComponent(String systemId, String componentId, List<String> policyIds);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find system components by compliance status
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.List&lt;org.gecko.mac.governance.SystemComponent&gt;" many="false" systemIdRequired="true" complianceStatusRequired="true"
	 * @generated
	 */
	List<SystemComponent> findSystemComponentsByComplianceStatus(String systemId, ComplianceStatus complianceStatus);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get system components by type
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.List&lt;org.gecko.mac.governance.SystemComponent&gt;" many="false" systemIdRequired="true" componentTypeRequired="true"
	 * @generated
	 */
	List<SystemComponent> getSystemComponentsByType(String systemId, SystemComponentType componentType);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Link a model object to its source system component
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Void" objectIdRequired="true" systemIdRequired="true" componentIdRequired="true"
	 * @generated
	 */
	Void linkModelToSystemComponent(String objectId, String systemId, String componentId);

} // GovernanceSystemRepository
