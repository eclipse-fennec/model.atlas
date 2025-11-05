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
package org.gecko.mac.mgmt.management.impl;

import java.time.Instant;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.gecko.mac.mgmt.management.GenerationRequest;
import org.gecko.mac.mgmt.management.GenerationStatus;
import org.gecko.mac.mgmt.management.ManagementPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Generation Request</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.GenerationRequestImpl#getRequestId <em>Request Id</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.GenerationRequestImpl#getJsonSample <em>Json Sample</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.GenerationRequestImpl#getJsonFingerprint <em>Json Fingerprint</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.GenerationRequestImpl#getSourceChannel <em>Source Channel</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.GenerationRequestImpl#getRequestingUser <em>Requesting User</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.GenerationRequestImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.GenerationRequestImpl#getRequestTime <em>Request Time</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.GenerationRequestImpl#getStartTime <em>Start Time</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.GenerationRequestImpl#getCompletionTime <em>Completion Time</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.GenerationRequestImpl#getErrorMessage <em>Error Message</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.GenerationRequestImpl#getResultPackageId <em>Result Package Id</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GenerationRequestImpl extends MinimalEObjectImpl.Container implements GenerationRequest {
	/**
	 * The default value of the '{@link #getRequestId() <em>Request Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRequestId()
	 * @generated
	 * @ordered
	 */
	protected static final String REQUEST_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRequestId() <em>Request Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRequestId()
	 * @generated
	 * @ordered
	 */
	protected String requestId = REQUEST_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getJsonSample() <em>Json Sample</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJsonSample()
	 * @generated
	 * @ordered
	 */
	protected static final String JSON_SAMPLE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getJsonSample() <em>Json Sample</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJsonSample()
	 * @generated
	 * @ordered
	 */
	protected String jsonSample = JSON_SAMPLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getJsonFingerprint() <em>Json Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJsonFingerprint()
	 * @generated
	 * @ordered
	 */
	protected static final String JSON_FINGERPRINT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getJsonFingerprint() <em>Json Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJsonFingerprint()
	 * @generated
	 * @ordered
	 */
	protected String jsonFingerprint = JSON_FINGERPRINT_EDEFAULT;

	/**
	 * The default value of the '{@link #getSourceChannel() <em>Source Channel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceChannel()
	 * @generated
	 * @ordered
	 */
	protected static final String SOURCE_CHANNEL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSourceChannel() <em>Source Channel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSourceChannel()
	 * @generated
	 * @ordered
	 */
	protected String sourceChannel = SOURCE_CHANNEL_EDEFAULT;

	/**
	 * The default value of the '{@link #getRequestingUser() <em>Requesting User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRequestingUser()
	 * @generated
	 * @ordered
	 */
	protected static final String REQUESTING_USER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRequestingUser() <em>Requesting User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRequestingUser()
	 * @generated
	 * @ordered
	 */
	protected String requestingUser = REQUESTING_USER_EDEFAULT;

	/**
	 * The default value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected static final GenerationStatus STATUS_EDEFAULT = GenerationStatus.REQUESTED;

	/**
	 * The cached value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected GenerationStatus status = STATUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getRequestTime() <em>Request Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRequestTime()
	 * @generated
	 * @ordered
	 */
	protected static final Instant REQUEST_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRequestTime() <em>Request Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRequestTime()
	 * @generated
	 * @ordered
	 */
	protected Instant requestTime = REQUEST_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getStartTime() <em>Start Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStartTime()
	 * @generated
	 * @ordered
	 */
	protected static final Instant START_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getStartTime() <em>Start Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStartTime()
	 * @generated
	 * @ordered
	 */
	protected Instant startTime = START_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getCompletionTime() <em>Completion Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCompletionTime()
	 * @generated
	 * @ordered
	 */
	protected static final Instant COMPLETION_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCompletionTime() <em>Completion Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCompletionTime()
	 * @generated
	 * @ordered
	 */
	protected Instant completionTime = COMPLETION_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getErrorMessage() <em>Error Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getErrorMessage()
	 * @generated
	 * @ordered
	 */
	protected static final String ERROR_MESSAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getErrorMessage() <em>Error Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getErrorMessage()
	 * @generated
	 * @ordered
	 */
	protected String errorMessage = ERROR_MESSAGE_EDEFAULT;

	/**
	 * The default value of the '{@link #getResultPackageId() <em>Result Package Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResultPackageId()
	 * @generated
	 * @ordered
	 */
	protected static final String RESULT_PACKAGE_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getResultPackageId() <em>Result Package Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getResultPackageId()
	 * @generated
	 * @ordered
	 */
	protected String resultPackageId = RESULT_PACKAGE_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GenerationRequestImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ManagementPackage.Literals.GENERATION_REQUEST;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRequestId() {
		return requestId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRequestId(String newRequestId) {
		String oldRequestId = requestId;
		requestId = newRequestId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.GENERATION_REQUEST__REQUEST_ID, oldRequestId, requestId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getJsonSample() {
		return jsonSample;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setJsonSample(String newJsonSample) {
		String oldJsonSample = jsonSample;
		jsonSample = newJsonSample;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.GENERATION_REQUEST__JSON_SAMPLE, oldJsonSample, jsonSample));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getJsonFingerprint() {
		return jsonFingerprint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setJsonFingerprint(String newJsonFingerprint) {
		String oldJsonFingerprint = jsonFingerprint;
		jsonFingerprint = newJsonFingerprint;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.GENERATION_REQUEST__JSON_FINGERPRINT, oldJsonFingerprint, jsonFingerprint));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSourceChannel() {
		return sourceChannel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSourceChannel(String newSourceChannel) {
		String oldSourceChannel = sourceChannel;
		sourceChannel = newSourceChannel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.GENERATION_REQUEST__SOURCE_CHANNEL, oldSourceChannel, sourceChannel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRequestingUser() {
		return requestingUser;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRequestingUser(String newRequestingUser) {
		String oldRequestingUser = requestingUser;
		requestingUser = newRequestingUser;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.GENERATION_REQUEST__REQUESTING_USER, oldRequestingUser, requestingUser));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GenerationStatus getStatus() {
		return status;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStatus(GenerationStatus newStatus) {
		GenerationStatus oldStatus = status;
		status = newStatus == null ? STATUS_EDEFAULT : newStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.GENERATION_REQUEST__STATUS, oldStatus, status));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getRequestTime() {
		return requestTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRequestTime(Instant newRequestTime) {
		Instant oldRequestTime = requestTime;
		requestTime = newRequestTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.GENERATION_REQUEST__REQUEST_TIME, oldRequestTime, requestTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getStartTime() {
		return startTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStartTime(Instant newStartTime) {
		Instant oldStartTime = startTime;
		startTime = newStartTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.GENERATION_REQUEST__START_TIME, oldStartTime, startTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getCompletionTime() {
		return completionTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCompletionTime(Instant newCompletionTime) {
		Instant oldCompletionTime = completionTime;
		completionTime = newCompletionTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.GENERATION_REQUEST__COMPLETION_TIME, oldCompletionTime, completionTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setErrorMessage(String newErrorMessage) {
		String oldErrorMessage = errorMessage;
		errorMessage = newErrorMessage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.GENERATION_REQUEST__ERROR_MESSAGE, oldErrorMessage, errorMessage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getResultPackageId() {
		return resultPackageId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setResultPackageId(String newResultPackageId) {
		String oldResultPackageId = resultPackageId;
		resultPackageId = newResultPackageId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.GENERATION_REQUEST__RESULT_PACKAGE_ID, oldResultPackageId, resultPackageId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ManagementPackage.GENERATION_REQUEST__REQUEST_ID:
				return getRequestId();
			case ManagementPackage.GENERATION_REQUEST__JSON_SAMPLE:
				return getJsonSample();
			case ManagementPackage.GENERATION_REQUEST__JSON_FINGERPRINT:
				return getJsonFingerprint();
			case ManagementPackage.GENERATION_REQUEST__SOURCE_CHANNEL:
				return getSourceChannel();
			case ManagementPackage.GENERATION_REQUEST__REQUESTING_USER:
				return getRequestingUser();
			case ManagementPackage.GENERATION_REQUEST__STATUS:
				return getStatus();
			case ManagementPackage.GENERATION_REQUEST__REQUEST_TIME:
				return getRequestTime();
			case ManagementPackage.GENERATION_REQUEST__START_TIME:
				return getStartTime();
			case ManagementPackage.GENERATION_REQUEST__COMPLETION_TIME:
				return getCompletionTime();
			case ManagementPackage.GENERATION_REQUEST__ERROR_MESSAGE:
				return getErrorMessage();
			case ManagementPackage.GENERATION_REQUEST__RESULT_PACKAGE_ID:
				return getResultPackageId();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ManagementPackage.GENERATION_REQUEST__REQUEST_ID:
				setRequestId((String)newValue);
				return;
			case ManagementPackage.GENERATION_REQUEST__JSON_SAMPLE:
				setJsonSample((String)newValue);
				return;
			case ManagementPackage.GENERATION_REQUEST__JSON_FINGERPRINT:
				setJsonFingerprint((String)newValue);
				return;
			case ManagementPackage.GENERATION_REQUEST__SOURCE_CHANNEL:
				setSourceChannel((String)newValue);
				return;
			case ManagementPackage.GENERATION_REQUEST__REQUESTING_USER:
				setRequestingUser((String)newValue);
				return;
			case ManagementPackage.GENERATION_REQUEST__STATUS:
				setStatus((GenerationStatus)newValue);
				return;
			case ManagementPackage.GENERATION_REQUEST__REQUEST_TIME:
				setRequestTime((Instant)newValue);
				return;
			case ManagementPackage.GENERATION_REQUEST__START_TIME:
				setStartTime((Instant)newValue);
				return;
			case ManagementPackage.GENERATION_REQUEST__COMPLETION_TIME:
				setCompletionTime((Instant)newValue);
				return;
			case ManagementPackage.GENERATION_REQUEST__ERROR_MESSAGE:
				setErrorMessage((String)newValue);
				return;
			case ManagementPackage.GENERATION_REQUEST__RESULT_PACKAGE_ID:
				setResultPackageId((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ManagementPackage.GENERATION_REQUEST__REQUEST_ID:
				setRequestId(REQUEST_ID_EDEFAULT);
				return;
			case ManagementPackage.GENERATION_REQUEST__JSON_SAMPLE:
				setJsonSample(JSON_SAMPLE_EDEFAULT);
				return;
			case ManagementPackage.GENERATION_REQUEST__JSON_FINGERPRINT:
				setJsonFingerprint(JSON_FINGERPRINT_EDEFAULT);
				return;
			case ManagementPackage.GENERATION_REQUEST__SOURCE_CHANNEL:
				setSourceChannel(SOURCE_CHANNEL_EDEFAULT);
				return;
			case ManagementPackage.GENERATION_REQUEST__REQUESTING_USER:
				setRequestingUser(REQUESTING_USER_EDEFAULT);
				return;
			case ManagementPackage.GENERATION_REQUEST__STATUS:
				setStatus(STATUS_EDEFAULT);
				return;
			case ManagementPackage.GENERATION_REQUEST__REQUEST_TIME:
				setRequestTime(REQUEST_TIME_EDEFAULT);
				return;
			case ManagementPackage.GENERATION_REQUEST__START_TIME:
				setStartTime(START_TIME_EDEFAULT);
				return;
			case ManagementPackage.GENERATION_REQUEST__COMPLETION_TIME:
				setCompletionTime(COMPLETION_TIME_EDEFAULT);
				return;
			case ManagementPackage.GENERATION_REQUEST__ERROR_MESSAGE:
				setErrorMessage(ERROR_MESSAGE_EDEFAULT);
				return;
			case ManagementPackage.GENERATION_REQUEST__RESULT_PACKAGE_ID:
				setResultPackageId(RESULT_PACKAGE_ID_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ManagementPackage.GENERATION_REQUEST__REQUEST_ID:
				return REQUEST_ID_EDEFAULT == null ? requestId != null : !REQUEST_ID_EDEFAULT.equals(requestId);
			case ManagementPackage.GENERATION_REQUEST__JSON_SAMPLE:
				return JSON_SAMPLE_EDEFAULT == null ? jsonSample != null : !JSON_SAMPLE_EDEFAULT.equals(jsonSample);
			case ManagementPackage.GENERATION_REQUEST__JSON_FINGERPRINT:
				return JSON_FINGERPRINT_EDEFAULT == null ? jsonFingerprint != null : !JSON_FINGERPRINT_EDEFAULT.equals(jsonFingerprint);
			case ManagementPackage.GENERATION_REQUEST__SOURCE_CHANNEL:
				return SOURCE_CHANNEL_EDEFAULT == null ? sourceChannel != null : !SOURCE_CHANNEL_EDEFAULT.equals(sourceChannel);
			case ManagementPackage.GENERATION_REQUEST__REQUESTING_USER:
				return REQUESTING_USER_EDEFAULT == null ? requestingUser != null : !REQUESTING_USER_EDEFAULT.equals(requestingUser);
			case ManagementPackage.GENERATION_REQUEST__STATUS:
				return status != STATUS_EDEFAULT;
			case ManagementPackage.GENERATION_REQUEST__REQUEST_TIME:
				return REQUEST_TIME_EDEFAULT == null ? requestTime != null : !REQUEST_TIME_EDEFAULT.equals(requestTime);
			case ManagementPackage.GENERATION_REQUEST__START_TIME:
				return START_TIME_EDEFAULT == null ? startTime != null : !START_TIME_EDEFAULT.equals(startTime);
			case ManagementPackage.GENERATION_REQUEST__COMPLETION_TIME:
				return COMPLETION_TIME_EDEFAULT == null ? completionTime != null : !COMPLETION_TIME_EDEFAULT.equals(completionTime);
			case ManagementPackage.GENERATION_REQUEST__ERROR_MESSAGE:
				return ERROR_MESSAGE_EDEFAULT == null ? errorMessage != null : !ERROR_MESSAGE_EDEFAULT.equals(errorMessage);
			case ManagementPackage.GENERATION_REQUEST__RESULT_PACKAGE_ID:
				return RESULT_PACKAGE_ID_EDEFAULT == null ? resultPackageId != null : !RESULT_PACKAGE_ID_EDEFAULT.equals(resultPackageId);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (requestId: ");
		result.append(requestId);
		result.append(", jsonSample: ");
		result.append(jsonSample);
		result.append(", jsonFingerprint: ");
		result.append(jsonFingerprint);
		result.append(", sourceChannel: ");
		result.append(sourceChannel);
		result.append(", requestingUser: ");
		result.append(requestingUser);
		result.append(", status: ");
		result.append(status);
		result.append(", requestTime: ");
		result.append(requestTime);
		result.append(", startTime: ");
		result.append(startTime);
		result.append(", completionTime: ");
		result.append(completionTime);
		result.append(", errorMessage: ");
		result.append(errorMessage);
		result.append(", resultPackageId: ");
		result.append(resultPackageId);
		result.append(')');
		return result.toString();
	}

} //GenerationRequestImpl
