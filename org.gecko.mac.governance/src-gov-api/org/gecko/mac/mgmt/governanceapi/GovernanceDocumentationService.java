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
import java.util.Map;
import java.util.Optional;

import org.gecko.mac.governance.GovernanceDocumentation;

import org.gecko.mac.mgmt.management.ObjectMetadata;

import org.osgi.annotation.versioning.ProviderType;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Governance Documentation Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Service for managing governance documentation storage and retrieval. Provides dedicated storage for governance documentation independent of object lifecycle with versioned storage, history tracking, and fast lookup capabilities.
 * <!-- end-model-doc -->
 *
 *
 * @see org.gecko.mac.mgmt.governanceapi.ManagementApiPackage#getGovernanceDocumentationService()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface GovernanceDocumentationService {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Store governance documentation for an object. Creates both versioned and latest documentation entries for comprehensive audit trail and fast access. The reason parameter is stored in the ObjectMetadata reviewReason field for audit purposes.
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EString&gt;" objectIdRequired="true" documentationRequired="true" reviewUserRequired="true"
	 * @generated
	 */
	Promise<String> storeDocumentation(String objectId, GovernanceDocumentation documentation, String reviewUser, String reason);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Retrieve the latest governance documentation for an object
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Optional&lt;org.gecko.mac.governance.GovernanceDocumentation&gt;" objectIdRequired="true"
	 * @generated
	 */
	Optional<GovernanceDocumentation> getLatestDocumentation(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Retrieve specific governance documentation by its ID
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Optional&lt;org.gecko.mac.governance.GovernanceDocumentation&gt;" documentationIdRequired="true"
	 * @generated
	 */
	Optional<GovernanceDocumentation> getDocumentation(String documentationId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get the governance documentation history for an object, returning all versioned documentation ordered by creation time
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.List&lt;org.gecko.mac.mgmt.management.ObjectMetadata&gt;" many="false" objectIdRequired="true"
	 * @generated
	 */
	List<ObjectMetadata> getDocumentationHistory(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check if governance documentation exists for an object
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	Boolean hasDocumentation(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Delete all governance documentation for an object. Removes both versioned and latest documentation. Use with caution as this destroys the compliance audit trail.
	 * <!-- end-model-doc -->
	 * @model dataType="org.gecko.mac.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EBooleanObject&gt;" objectIdRequired="true"
	 * @generated
	 */
	Promise<Boolean> deleteAllDocumentation(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get governance documentation statistics including total documentation count, active objects, and historical compliance check metrics
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	Map<String, Object> getDocumentationStatistics();

} // GovernanceDocumentationService
