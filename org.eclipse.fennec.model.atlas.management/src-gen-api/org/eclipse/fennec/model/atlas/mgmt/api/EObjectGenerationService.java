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

import org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest;

import org.osgi.annotation.versioning.ProviderType;

import org.osgi.util.promise.Promise;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>EObject Generation Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Service for AI-based EObject generation from JSON samples. Supports asynchronous generation with progress tracking, duplicate detection via fingerprinting, and automatic model registry integration. Used by IoT Broker for unknown sensor types and by manual model creation workflows.
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.api.ManagementApiPackage#getEObjectGenerationService()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface EObjectGenerationService<T extends EObject> {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Request EObject generation from JSON sample. Returns Promise&lt;String&gt; with unique request ID for status tracking. Example: service.requestGeneration('{"temperature": 23.5}', 'SENSINACT', 'iot-system', 'SensorModel').then(requestId -&gt; pollForCompletion(requestId))
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EString&gt;" jsonSampleRequired="true" sourceChannelRequired="true" requestingUserRequired="true" targetTypeRequired="true"
	 * @generated
	 */
	Promise<String> requestGeneration(String jsonSample, String sourceChannel, String requestingUser, String targetType);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Request EObject generation with parent context. Use when generating child objects that reference an existing parent EObject. The parentEObject provides type context for better AI generation results.
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EString&gt;" jsonSampleRequired="true" sourceChannelRequired="true" requestingUserRequired="true" targetTypeRequired="true"
	 * @generated
	 */
	Promise<String> requestGeneration(String jsonSample, String sourceChannel, String requestingUser, String targetType, T parentEObject);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Get current status of generation request
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest&gt;" requestIdRequired="true"
	 * @generated
	 */
	Promise<GenerationRequest> getGenerationStatus(String requestId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cancel ongoing generation request
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.emf.ecore.EBooleanObject&gt;" requestIdRequired="true"
	 * @generated
	 */
	Promise<Boolean> cancelGeneration(String requestId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * List all active generation requests
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Promise&lt;org.eclipse.fennec.model.atlas.mgmt.management.List&lt;org.eclipse.fennec.model.atlas.mgmt.management.GenerationRequest&gt;&gt;"
	 * @generated
	 */
	Promise<List<GenerationRequest>> listActiveGenerations();

} // EObjectGenerationService
