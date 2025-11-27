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

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

import org.osgi.annotation.versioning.ProviderType;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>EObject Discovery Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Generic service for JSON pattern recognition and object discovery
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.api.ManagementApiPackage#getEObjectDiscoveryService()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface EObjectDiscoveryService<T extends EObject> {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Find existing EObject that matches JSON structure
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;" jsonSampleRequired="true" sourceChannelRequired="true" targetTypeRequired="true"
	 * @generated
	 */
	Promise<ObjectMetadata> findObjectByJsonPattern(String jsonSample, String sourceChannel, String targetType);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Search for objects with similar JSON patterns
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata&gt;&gt;" jsonSampleRequired="true" targetTypeRequired="true"
	 * @generated
	 */
	Promise<List<ObjectMetadata>> searchSimilarObjects(String jsonSample, String targetType, double similarityThreshold);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Create structural fingerprint from JSON sample
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EString&gt;" jsonSampleRequired="true"
	 * @generated
	 */
	Promise<String> createJsonFingerprint(String jsonSample);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check if generation is already in progress for this pattern
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EBooleanObject&gt;" jsonFingerprintRequired="true"
	 * @generated
	 */
	Promise<Boolean> isGenerationInProgress(String jsonFingerprint);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cache generation request to prevent duplicates
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.Void&gt;" jsonFingerprintRequired="true" requestIdRequired="true"
	 * @generated
	 */
	Promise<Void> cacheGenerationRequest(String jsonFingerprint, String requestId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Remove generation request from cache
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.Void&gt;" jsonFingerprintRequired="true"
	 * @generated
	 */
	Promise<Void> removeGenerationRequestFromCache(String jsonFingerprint);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cache ObjectMetadata
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.Void&gt;" objectRegistrationRequired="true"
	 * @generated
	 */
	Promise<Void> cacheObjectMetadata(ObjectMetadata objectRegistration);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Remove ObjectMetadata from cache
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.Void&gt;" objectIdRequired="true"
	 * @generated
	 */
	Promise<Void> removeObjectRegistrationFromCache(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Retrieves the id of the GenerationRequest for that fingerprint, so one can further asks for the status
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EString&gt;" jsonFingerprintRequired="true"
	 * @generated
	 */
	Promise<String> getGenerationRequestIdForFingerprint(String jsonFingerprint);

} // EObjectDiscoveryService
