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

import java.time.Instant;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;

import org.osgi.annotation.versioning.ProviderType;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>EObject Registry Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Registry service providing fast lookup and lifecycle management of object metadata. Implementation leverages Lucene indexing for O(log n) performance on queries by objectId, status, version, and other metadata fields. Serves as the central ObjectRegistration registry eliminating need for separate in-memory collections.
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.api.ManagementApiPackage#getEObjectRegistryService()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface EObjectRegistryService<T extends EObject> {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Retrieve object metadata by unique objectId. Returns Optional.empty() if not found. ObjectId is now globally unique across all storage roles.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Optional&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" required="true" objectIdRequired="true"
	 * @generated
	 */
	Optional<ObjectMetadata> getMetadata(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find all metadata instances for a logical object across all storage roles. Returns list of metadata for the same objectName in different roles (draft, approved, documentation).
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false" objectNameRequired="true"
	 * @generated
	 */
	List<ObjectMetadata> findByObjectName(String objectName);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find metadata for specific logical object and storage role. Returns Optional.empty() if not found for the specific objectName and role combination.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Optional&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" objectNameRequired="true" roleRequired="true"
	 * @generated
	 */
	Optional<ObjectMetadata> findByObjectNameAndRole(String objectName, String role);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find all objects with the specified storage role (draft, approved, documentation) across all logical objects. Useful for role-based workflows.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false" roleRequired="true"
	 * @generated
	 */
	List<ObjectMetadata> findByRole(String role);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find all objects with the specified lifecycle status (DRAFT, APPROVED, REJECTED, DEPLOYED, ARCHIVED). Uses Lucene exact-match indexing for efficient retrieval.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false" statusRequired="true"
	 * @generated
	 */
	List<ObjectMetadata> findByStatus(ObjectStatus status);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Convenience method to find all objects with status=DRAFT that are pending approval. Equivalent to findByStatus(ObjectStatus.DRAFT).
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false"
	 * @generated
	 */
	List<ObjectMetadata> findPendingApproval();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find objects by exact version match. For wildcard searches (e.g. '1.*'), use findByVersionPattern().
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false" versionRequired="true"
	 * @generated
	 */
	List<ObjectMetadata> findByVersion(String version);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find objects by version wildcard pattern (e.g. '1.*' for all 1.x versions, '2.1.*' for all 2.1.x versions). Uses Lucene analyzed field for pattern matching.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false" versionPatternRequired="true"
	 * @generated
	 */
	List<ObjectMetadata> findByVersionPattern(String versionPattern);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find objects by generation trigger fingerprint for duplicate detection. Essential for AI generation workflow to prevent creating duplicate models for the same JSON structure. Returns Optional.empty() if no object with the specified fingerprint exists.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Optional&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" fingerprintRequired="true"
	 * @generated
	 */
	Optional<ObjectMetadata> findByFingerprint(String fingerprint);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find all objects of the specified type (e.g. 'EPackage', 'Route', 'SensorModel'). Useful for type-specific workflows and bulk operations.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false" objectTypeRequired="true"
	 * @generated
	 */
	List<ObjectMetadata> findByObjectType(String objectType);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find objects matching both status and type criteria. Combines exact-match indexing for optimal performance on common query patterns.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false" statusRequired="true" objectTypeRequired="true"
	 * @generated
	 */
	List<ObjectMetadata> findByStatusAndType(ObjectStatus status, String objectType);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find objects modified within the specified time window. Returns objects sorted by lastChangeTime descending (most recent first).
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" many="false" sinceTimeDataType="org.eclipse.fennec.model.atlas.mgmt.management.Instant" sinceTimeRequired="true"
	 * @generated
	 */
	List<ObjectMetadata> findRecentlyModified(Instant sinceTime, int maxResults);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Update registry cache with new or modified metadata. Automatically updates Lucene index for immediate searchability. Called by EObjectStorageService on save operations. Uses metadata.getObjectId() as unique key and metadata.getRole() for role-based indexing.
	 * <!-- end-model-doc -->
	 * @model metadataRequired="true"
	 * @generated
	 */
	void updateCache(ObjectMetadata metadata);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Remove object from registry cache and Lucene index by unique objectId. Called by EObjectStorageService on delete operations to maintain consistency.
	 * <!-- end-model-doc -->
	 * @model objectIdRequired="true"
	 * @generated
	 */
	void removeFromCache(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get registry statistics including total object count, count by status, count by type, and index health metrics. Useful for monitoring and dashboard displays.
	 * <!-- end-model-doc -->
	 * @model kind="operation" dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EMap&lt;org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EJavaObject&gt;&gt;"
	 * @generated
	 */
	Promise<Map<String, Object>> getRegistryStatistics();

} // EObjectRegistryService
