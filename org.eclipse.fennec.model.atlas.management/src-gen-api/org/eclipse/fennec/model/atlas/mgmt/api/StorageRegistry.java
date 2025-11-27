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
package org.eclipse.fennec.model.atlas.mgmt.api;

import java.util.Map;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Storage Registry</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Registry service for role-based storage service discovery and governance documentation lifecycle management. Provides centralized access to all storage services by role (draft, approved, release, documentation, archived) and handles bi-directional governance documentation references across storage tiers.
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.api.ManagementApiPackage#getStorageRegistry()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface StorageRegistry {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get storage service by role name. Returns the storage service configured for the specified role (e.g. 'draft', 'approved', 'release', 'documentation'). Returns null if no storage service is registered for the given role.
	 * <!-- end-model-doc -->
	 * @model roleRequired="true"
	 * @generated
	 */
	EObjectStorageService<EObject> getStorageByRole(String role);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get all registered storage services. Returns a list of all storage services across all roles, useful for bulk operations and system health checks.
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	EList<EObjectStorageService<EObject>> getAllStorages();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get set of all available storage roles. Returns the role names of all registered storage services (e.g. ['draft', 'approved', 'release', 'documentation']).
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	EList<String> getAvailableRoles();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Update governance documentation ID for all active objects with the specified object name in a specific storage role. This maintains bi-directional references between ObjectMetadata and GovernanceDocumentation for objects in the specified role only. Active objects (DRAFT, APPROVED, DEPLOYED) are updated with the new documentation ID, while archived objects preserve their historical references for audit trail.
	 * <!-- end-model-doc -->
	 * @model required="true" roleRequired="true" objectNameRequired="true" newDocumentationIdRequired="true"
	 * @generated
	 */
	int updateGovernanceDocumentationId(String role, String objectName, String newDocumentationId, String reason);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Search metadata across all storage roles using a query. Aggregates results from all registered storage services, useful for cross-role object discovery and lifecycle analysis.
	 * <!-- end-model-doc -->
	 * @model queryRequired="true"
	 * @generated
	 */
	EList<ObjectMetadata> searchMetadataAcrossRoles(ObjectQuery query);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get comprehensive storage statistics across all roles. Returns statistics including object counts per role, storage backend types, and overall system health metrics.
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	Map<String, Object> getStorageStatistics();

} // StorageRegistry
