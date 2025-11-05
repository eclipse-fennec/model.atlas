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
package org.gecko.mac.mgmt.management;

import java.time.Instant;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Generation Request</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Request for AI-based EPackage generation
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.mgmt.management.GenerationRequest#getRequestId <em>Request Id</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.GenerationRequest#getJsonSample <em>Json Sample</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.GenerationRequest#getJsonFingerprint <em>Json Fingerprint</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.GenerationRequest#getSourceChannel <em>Source Channel</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.GenerationRequest#getRequestingUser <em>Requesting User</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.GenerationRequest#getStatus <em>Status</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.GenerationRequest#getRequestTime <em>Request Time</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.GenerationRequest#getStartTime <em>Start Time</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.GenerationRequest#getCompletionTime <em>Completion Time</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.GenerationRequest#getErrorMessage <em>Error Message</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.GenerationRequest#getResultPackageId <em>Result Package Id</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest()
 * @model
 * @generated
 */
@ProviderType
public interface GenerationRequest extends EObject {
	/**
	 * Returns the value of the '<em><b>Request Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unique request identifier
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Request Id</em>' attribute.
	 * @see #setRequestId(String)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest_RequestId()
	 * @model required="true"
	 * @generated
	 */
	String getRequestId();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.GenerationRequest#getRequestId <em>Request Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Request Id</em>' attribute.
	 * @see #getRequestId()
	 * @generated
	 */
	void setRequestId(String value);

	/**
	 * Returns the value of the '<em><b>Json Sample</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * JSON sample used for generation
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Json Sample</em>' attribute.
	 * @see #setJsonSample(String)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest_JsonSample()
	 * @model required="true"
	 * @generated
	 */
	String getJsonSample();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.GenerationRequest#getJsonSample <em>Json Sample</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Json Sample</em>' attribute.
	 * @see #getJsonSample()
	 * @generated
	 */
	void setJsonSample(String value);

	/**
	 * Returns the value of the '<em><b>Json Fingerprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Structural fingerprint of JSON sample
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Json Fingerprint</em>' attribute.
	 * @see #setJsonFingerprint(String)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest_JsonFingerprint()
	 * @model required="true"
	 * @generated
	 */
	String getJsonFingerprint();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.GenerationRequest#getJsonFingerprint <em>Json Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Json Fingerprint</em>' attribute.
	 * @see #getJsonFingerprint()
	 * @generated
	 */
	void setJsonFingerprint(String value);

	/**
	 * Returns the value of the '<em><b>Source Channel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Source channel that triggered generation
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source Channel</em>' attribute.
	 * @see #setSourceChannel(String)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest_SourceChannel()
	 * @model required="true"
	 * @generated
	 */
	String getSourceChannel();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.GenerationRequest#getSourceChannel <em>Source Channel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Channel</em>' attribute.
	 * @see #getSourceChannel()
	 * @generated
	 */
	void setSourceChannel(String value);

	/**
	 * Returns the value of the '<em><b>Requesting User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * User who requested generation
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Requesting User</em>' attribute.
	 * @see #setRequestingUser(String)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest_RequestingUser()
	 * @model required="true"
	 * @generated
	 */
	String getRequestingUser();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.GenerationRequest#getRequestingUser <em>Requesting User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Requesting User</em>' attribute.
	 * @see #getRequestingUser()
	 * @generated
	 */
	void setRequestingUser(String value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * The default value is <code>"REQUESTED"</code>.
	 * The literals are from the enumeration {@link org.gecko.mac.mgmt.management.GenerationStatus}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Current generation status
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see org.gecko.mac.mgmt.management.GenerationStatus
	 * @see #setStatus(GenerationStatus)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest_Status()
	 * @model default="REQUESTED" required="true"
	 * @generated
	 */
	GenerationStatus getStatus();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.GenerationRequest#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see org.gecko.mac.mgmt.management.GenerationStatus
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(GenerationStatus value);

	/**
	 * Returns the value of the '<em><b>Request Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When generation was requested
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Request Time</em>' attribute.
	 * @see #setRequestTime(Instant)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest_RequestTime()
	 * @model dataType="org.gecko.mac.mgmt.management.Instant" required="true"
	 * @generated
	 */
	Instant getRequestTime();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.GenerationRequest#getRequestTime <em>Request Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Request Time</em>' attribute.
	 * @see #getRequestTime()
	 * @generated
	 */
	void setRequestTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Start Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When generation started
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Start Time</em>' attribute.
	 * @see #setStartTime(Instant)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest_StartTime()
	 * @model dataType="org.gecko.mac.mgmt.management.Instant"
	 * @generated
	 */
	Instant getStartTime();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.GenerationRequest#getStartTime <em>Start Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Start Time</em>' attribute.
	 * @see #getStartTime()
	 * @generated
	 */
	void setStartTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Completion Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When generation completed
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Completion Time</em>' attribute.
	 * @see #setCompletionTime(Instant)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest_CompletionTime()
	 * @model dataType="org.gecko.mac.mgmt.management.Instant"
	 * @generated
	 */
	Instant getCompletionTime();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.GenerationRequest#getCompletionTime <em>Completion Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Completion Time</em>' attribute.
	 * @see #getCompletionTime()
	 * @generated
	 */
	void setCompletionTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Error Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Error message if generation failed
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Error Message</em>' attribute.
	 * @see #setErrorMessage(String)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest_ErrorMessage()
	 * @model
	 * @generated
	 */
	String getErrorMessage();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.GenerationRequest#getErrorMessage <em>Error Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Error Message</em>' attribute.
	 * @see #getErrorMessage()
	 * @generated
	 */
	void setErrorMessage(String value);

	/**
	 * Returns the value of the '<em><b>Result Package Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Package ID of generated EPackage
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Result Package Id</em>' attribute.
	 * @see #setResultPackageId(String)
	 * @see org.gecko.mac.mgmt.management.ManagementPackage#getGenerationRequest_ResultPackageId()
	 * @model
	 * @generated
	 */
	String getResultPackageId();

	/**
	 * Sets the value of the '{@link org.gecko.mac.mgmt.management.GenerationRequest#getResultPackageId <em>Result Package Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Result Package Id</em>' attribute.
	 * @see #getResultPackageId()
	 * @generated
	 */
	void setResultPackageId(String value);

} // GenerationRequest
