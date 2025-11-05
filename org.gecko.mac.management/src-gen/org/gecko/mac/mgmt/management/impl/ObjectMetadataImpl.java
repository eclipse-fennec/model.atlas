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
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

import org.gecko.mac.mgmt.management.ManagementPackage;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.gecko.mac.mgmt.management.ObjectStatus;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Object Metadata</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getUploadUser <em>Upload User</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getUploadTime <em>Upload Time</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getSourceChannel <em>Source Channel</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getContentHash <em>Content Hash</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getObjectType <em>Object Type</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getReviewUser <em>Review User</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getReviewTime <em>Review Time</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getReviewReason <em>Review Reason</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getGenerationTriggerFingerprint <em>Generation Trigger Fingerprint</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getComplianceCheckTime <em>Compliance Check Time</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getComplianceStatus <em>Compliance Status</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getGovernanceDocumentationId <em>Governance Documentation Id</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getLastChangeUser <em>Last Change User</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getLastChangeTime <em>Last Change Time</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getVersion <em>Version</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getObjectRef <em>Object Ref</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getObjectId <em>Object Id</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getObjectName <em>Object Name</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getRole <em>Role</em>}</li>
 *   <li>{@link org.gecko.mac.mgmt.management.impl.ObjectMetadataImpl#getLastChangeReason <em>Last Change Reason</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ObjectMetadataImpl extends MinimalEObjectImpl.Container implements ObjectMetadata {
	/**
	 * The default value of the '{@link #getUploadUser() <em>Upload User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUploadUser()
	 * @generated
	 * @ordered
	 */
	protected static final String UPLOAD_USER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUploadUser() <em>Upload User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUploadUser()
	 * @generated
	 * @ordered
	 */
	protected String uploadUser = UPLOAD_USER_EDEFAULT;

	/**
	 * The default value of the '{@link #getUploadTime() <em>Upload Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUploadTime()
	 * @generated
	 * @ordered
	 */
	protected static final Instant UPLOAD_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUploadTime() <em>Upload Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUploadTime()
	 * @generated
	 * @ordered
	 */
	protected Instant uploadTime = UPLOAD_TIME_EDEFAULT;

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
	 * The default value of the '{@link #getContentHash() <em>Content Hash</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContentHash()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTENT_HASH_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContentHash() <em>Content Hash</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContentHash()
	 * @generated
	 * @ordered
	 */
	protected String contentHash = CONTENT_HASH_EDEFAULT;

	/**
	 * The default value of the '{@link #getObjectType() <em>Object Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObjectType()
	 * @generated
	 * @ordered
	 */
	protected static final String OBJECT_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObjectType() <em>Object Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObjectType()
	 * @generated
	 * @ordered
	 */
	protected String objectType = OBJECT_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getReviewUser() <em>Review User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReviewUser()
	 * @generated
	 * @ordered
	 */
	protected static final String REVIEW_USER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReviewUser() <em>Review User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReviewUser()
	 * @generated
	 * @ordered
	 */
	protected String reviewUser = REVIEW_USER_EDEFAULT;

	/**
	 * The default value of the '{@link #getReviewTime() <em>Review Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReviewTime()
	 * @generated
	 * @ordered
	 */
	protected static final Instant REVIEW_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReviewTime() <em>Review Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReviewTime()
	 * @generated
	 * @ordered
	 */
	protected Instant reviewTime = REVIEW_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getReviewReason() <em>Review Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReviewReason()
	 * @generated
	 * @ordered
	 */
	protected static final String REVIEW_REASON_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReviewReason() <em>Review Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReviewReason()
	 * @generated
	 * @ordered
	 */
	protected String reviewReason = REVIEW_REASON_EDEFAULT;

	/**
	 * The default value of the '{@link #getGenerationTriggerFingerprint() <em>Generation Trigger Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGenerationTriggerFingerprint()
	 * @generated
	 * @ordered
	 */
	protected static final String GENERATION_TRIGGER_FINGERPRINT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getGenerationTriggerFingerprint() <em>Generation Trigger Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGenerationTriggerFingerprint()
	 * @generated
	 * @ordered
	 */
	protected String generationTriggerFingerprint = GENERATION_TRIGGER_FINGERPRINT_EDEFAULT;

	/**
	 * The default value of the '{@link #getComplianceCheckTime() <em>Compliance Check Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComplianceCheckTime()
	 * @generated
	 * @ordered
	 */
	protected static final Instant COMPLIANCE_CHECK_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getComplianceCheckTime() <em>Compliance Check Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComplianceCheckTime()
	 * @generated
	 * @ordered
	 */
	protected Instant complianceCheckTime = COMPLIANCE_CHECK_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getComplianceStatus() <em>Compliance Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComplianceStatus()
	 * @generated
	 * @ordered
	 */
	protected static final String COMPLIANCE_STATUS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getComplianceStatus() <em>Compliance Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComplianceStatus()
	 * @generated
	 * @ordered
	 */
	protected String complianceStatus = COMPLIANCE_STATUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getGovernanceDocumentationId() <em>Governance Documentation Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGovernanceDocumentationId()
	 * @generated
	 * @ordered
	 */
	protected static final String GOVERNANCE_DOCUMENTATION_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getGovernanceDocumentationId() <em>Governance Documentation Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGovernanceDocumentationId()
	 * @generated
	 * @ordered
	 */
	protected String governanceDocumentationId = GOVERNANCE_DOCUMENTATION_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getProperties() <em>Properties</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProperties()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, Object> properties;

	/**
	 * The default value of the '{@link #getLastChangeUser() <em>Last Change User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastChangeUser()
	 * @generated
	 * @ordered
	 */
	protected static final String LAST_CHANGE_USER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLastChangeUser() <em>Last Change User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastChangeUser()
	 * @generated
	 * @ordered
	 */
	protected String lastChangeUser = LAST_CHANGE_USER_EDEFAULT;

	/**
	 * The default value of the '{@link #getLastChangeTime() <em>Last Change Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastChangeTime()
	 * @generated
	 * @ordered
	 */
	protected static final Instant LAST_CHANGE_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLastChangeTime() <em>Last Change Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastChangeTime()
	 * @generated
	 * @ordered
	 */
	protected Instant lastChangeTime = LAST_CHANGE_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected static final ObjectStatus STATUS_EDEFAULT = ObjectStatus.DRAFT;

	/**
	 * The cached value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected ObjectStatus status = STATUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected static final String VERSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected String version = VERSION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getObjectRef() <em>Object Ref</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObjectRef()
	 * @generated
	 * @ordered
	 */
	protected EObject objectRef;

	/**
	 * The default value of the '{@link #getObjectId() <em>Object Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObjectId()
	 * @generated
	 * @ordered
	 */
	protected static final String OBJECT_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObjectId() <em>Object Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObjectId()
	 * @generated
	 * @ordered
	 */
	protected String objectId = OBJECT_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getObjectName() <em>Object Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObjectName()
	 * @generated
	 * @ordered
	 */
	protected static final String OBJECT_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObjectName() <em>Object Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObjectName()
	 * @generated
	 * @ordered
	 */
	protected String objectName = OBJECT_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getRole() <em>Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRole()
	 * @generated
	 * @ordered
	 */
	protected static final String ROLE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRole() <em>Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRole()
	 * @generated
	 * @ordered
	 */
	protected String role = ROLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getLastChangeReason() <em>Last Change Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastChangeReason()
	 * @generated
	 * @ordered
	 */
	protected static final String LAST_CHANGE_REASON_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLastChangeReason() <em>Last Change Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastChangeReason()
	 * @generated
	 * @ordered
	 */
	protected String lastChangeReason = LAST_CHANGE_REASON_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ObjectMetadataImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ManagementPackage.Literals.OBJECT_METADATA;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUploadUser() {
		return uploadUser;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUploadUser(String newUploadUser) {
		String oldUploadUser = uploadUser;
		uploadUser = newUploadUser;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__UPLOAD_USER, oldUploadUser, uploadUser));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getUploadTime() {
		return uploadTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUploadTime(Instant newUploadTime) {
		Instant oldUploadTime = uploadTime;
		uploadTime = newUploadTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__UPLOAD_TIME, oldUploadTime, uploadTime));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__SOURCE_CHANNEL, oldSourceChannel, sourceChannel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getContentHash() {
		return contentHash;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContentHash(String newContentHash) {
		String oldContentHash = contentHash;
		contentHash = newContentHash;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__CONTENT_HASH, oldContentHash, contentHash));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObjectType() {
		return objectType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObjectType(String newObjectType) {
		String oldObjectType = objectType;
		objectType = newObjectType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__OBJECT_TYPE, oldObjectType, objectType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getReviewUser() {
		return reviewUser;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setReviewUser(String newReviewUser) {
		String oldReviewUser = reviewUser;
		reviewUser = newReviewUser;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__REVIEW_USER, oldReviewUser, reviewUser));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getReviewTime() {
		return reviewTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setReviewTime(Instant newReviewTime) {
		Instant oldReviewTime = reviewTime;
		reviewTime = newReviewTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__REVIEW_TIME, oldReviewTime, reviewTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getReviewReason() {
		return reviewReason;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setReviewReason(String newReviewReason) {
		String oldReviewReason = reviewReason;
		reviewReason = newReviewReason;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__REVIEW_REASON, oldReviewReason, reviewReason));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getGenerationTriggerFingerprint() {
		return generationTriggerFingerprint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setGenerationTriggerFingerprint(String newGenerationTriggerFingerprint) {
		String oldGenerationTriggerFingerprint = generationTriggerFingerprint;
		generationTriggerFingerprint = newGenerationTriggerFingerprint;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__GENERATION_TRIGGER_FINGERPRINT, oldGenerationTriggerFingerprint, generationTriggerFingerprint));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getComplianceCheckTime() {
		return complianceCheckTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setComplianceCheckTime(Instant newComplianceCheckTime) {
		Instant oldComplianceCheckTime = complianceCheckTime;
		complianceCheckTime = newComplianceCheckTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__COMPLIANCE_CHECK_TIME, oldComplianceCheckTime, complianceCheckTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getComplianceStatus() {
		return complianceStatus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setComplianceStatus(String newComplianceStatus) {
		String oldComplianceStatus = complianceStatus;
		complianceStatus = newComplianceStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__COMPLIANCE_STATUS, oldComplianceStatus, complianceStatus));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getGovernanceDocumentationId() {
		return governanceDocumentationId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setGovernanceDocumentationId(String newGovernanceDocumentationId) {
		String oldGovernanceDocumentationId = governanceDocumentationId;
		governanceDocumentationId = newGovernanceDocumentationId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__GOVERNANCE_DOCUMENTATION_ID, oldGovernanceDocumentationId, governanceDocumentationId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EMap<String, Object> getProperties() {
		if (properties == null) {
			properties = new EcoreEMap<String,Object>(ManagementPackage.Literals.STRING_TO_OBJECT_MAP_ENTRY, StringToObjectMapEntryImpl.class, this, ManagementPackage.OBJECT_METADATA__PROPERTIES);
		}
		return properties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLastChangeUser() {
		return lastChangeUser;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLastChangeUser(String newLastChangeUser) {
		String oldLastChangeUser = lastChangeUser;
		lastChangeUser = newLastChangeUser;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__LAST_CHANGE_USER, oldLastChangeUser, lastChangeUser));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getLastChangeTime() {
		return lastChangeTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLastChangeTime(Instant newLastChangeTime) {
		Instant oldLastChangeTime = lastChangeTime;
		lastChangeTime = newLastChangeTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__LAST_CHANGE_TIME, oldLastChangeTime, lastChangeTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObjectStatus getStatus() {
		return status;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStatus(ObjectStatus newStatus) {
		ObjectStatus oldStatus = status;
		status = newStatus == null ? STATUS_EDEFAULT : newStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__STATUS, oldStatus, status));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getVersion() {
		return version;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setVersion(String newVersion) {
		String oldVersion = version;
		version = newVersion;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__VERSION, oldVersion, version));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject getObjectRef() {
		if (objectRef != null && objectRef.eIsProxy()) {
			InternalEObject oldObjectRef = (InternalEObject)objectRef;
			objectRef = eResolveProxy(oldObjectRef);
			if (objectRef != oldObjectRef) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ManagementPackage.OBJECT_METADATA__OBJECT_REF, oldObjectRef, objectRef));
			}
		}
		return objectRef;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EObject basicGetObjectRef() {
		return objectRef;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObjectRef(EObject newObjectRef) {
		EObject oldObjectRef = objectRef;
		objectRef = newObjectRef;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__OBJECT_REF, oldObjectRef, objectRef));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObjectId() {
		return objectId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObjectId(String newObjectId) {
		String oldObjectId = objectId;
		objectId = newObjectId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__OBJECT_ID, oldObjectId, objectId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObjectName() {
		return objectName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObjectName(String newObjectName) {
		String oldObjectName = objectName;
		objectName = newObjectName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__OBJECT_NAME, oldObjectName, objectName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRole() {
		return role;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRole(String newRole) {
		String oldRole = role;
		role = newRole;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__ROLE, oldRole, role));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLastChangeReason() {
		return lastChangeReason;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLastChangeReason(String newLastChangeReason) {
		String oldLastChangeReason = lastChangeReason;
		lastChangeReason = newLastChangeReason;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_METADATA__LAST_CHANGE_REASON, oldLastChangeReason, lastChangeReason));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ManagementPackage.OBJECT_METADATA__PROPERTIES:
				return ((InternalEList<?>)getProperties()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ManagementPackage.OBJECT_METADATA__UPLOAD_USER:
				return getUploadUser();
			case ManagementPackage.OBJECT_METADATA__UPLOAD_TIME:
				return getUploadTime();
			case ManagementPackage.OBJECT_METADATA__SOURCE_CHANNEL:
				return getSourceChannel();
			case ManagementPackage.OBJECT_METADATA__CONTENT_HASH:
				return getContentHash();
			case ManagementPackage.OBJECT_METADATA__OBJECT_TYPE:
				return getObjectType();
			case ManagementPackage.OBJECT_METADATA__REVIEW_USER:
				return getReviewUser();
			case ManagementPackage.OBJECT_METADATA__REVIEW_TIME:
				return getReviewTime();
			case ManagementPackage.OBJECT_METADATA__REVIEW_REASON:
				return getReviewReason();
			case ManagementPackage.OBJECT_METADATA__GENERATION_TRIGGER_FINGERPRINT:
				return getGenerationTriggerFingerprint();
			case ManagementPackage.OBJECT_METADATA__COMPLIANCE_CHECK_TIME:
				return getComplianceCheckTime();
			case ManagementPackage.OBJECT_METADATA__COMPLIANCE_STATUS:
				return getComplianceStatus();
			case ManagementPackage.OBJECT_METADATA__GOVERNANCE_DOCUMENTATION_ID:
				return getGovernanceDocumentationId();
			case ManagementPackage.OBJECT_METADATA__PROPERTIES:
				if (coreType) return getProperties();
				else return getProperties().map();
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_USER:
				return getLastChangeUser();
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_TIME:
				return getLastChangeTime();
			case ManagementPackage.OBJECT_METADATA__STATUS:
				return getStatus();
			case ManagementPackage.OBJECT_METADATA__VERSION:
				return getVersion();
			case ManagementPackage.OBJECT_METADATA__OBJECT_REF:
				if (resolve) return getObjectRef();
				return basicGetObjectRef();
			case ManagementPackage.OBJECT_METADATA__OBJECT_ID:
				return getObjectId();
			case ManagementPackage.OBJECT_METADATA__OBJECT_NAME:
				return getObjectName();
			case ManagementPackage.OBJECT_METADATA__ROLE:
				return getRole();
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_REASON:
				return getLastChangeReason();
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
			case ManagementPackage.OBJECT_METADATA__UPLOAD_USER:
				setUploadUser((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__UPLOAD_TIME:
				setUploadTime((Instant)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__SOURCE_CHANNEL:
				setSourceChannel((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__CONTENT_HASH:
				setContentHash((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__OBJECT_TYPE:
				setObjectType((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__REVIEW_USER:
				setReviewUser((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__REVIEW_TIME:
				setReviewTime((Instant)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__REVIEW_REASON:
				setReviewReason((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__GENERATION_TRIGGER_FINGERPRINT:
				setGenerationTriggerFingerprint((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__COMPLIANCE_CHECK_TIME:
				setComplianceCheckTime((Instant)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__COMPLIANCE_STATUS:
				setComplianceStatus((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__GOVERNANCE_DOCUMENTATION_ID:
				setGovernanceDocumentationId((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__PROPERTIES:
				((EStructuralFeature.Setting)getProperties()).set(newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_USER:
				setLastChangeUser((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_TIME:
				setLastChangeTime((Instant)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__STATUS:
				setStatus((ObjectStatus)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__VERSION:
				setVersion((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__OBJECT_REF:
				setObjectRef((EObject)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__OBJECT_ID:
				setObjectId((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__OBJECT_NAME:
				setObjectName((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__ROLE:
				setRole((String)newValue);
				return;
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_REASON:
				setLastChangeReason((String)newValue);
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
			case ManagementPackage.OBJECT_METADATA__UPLOAD_USER:
				setUploadUser(UPLOAD_USER_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__UPLOAD_TIME:
				setUploadTime(UPLOAD_TIME_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__SOURCE_CHANNEL:
				setSourceChannel(SOURCE_CHANNEL_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__CONTENT_HASH:
				setContentHash(CONTENT_HASH_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__OBJECT_TYPE:
				setObjectType(OBJECT_TYPE_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__REVIEW_USER:
				setReviewUser(REVIEW_USER_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__REVIEW_TIME:
				setReviewTime(REVIEW_TIME_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__REVIEW_REASON:
				setReviewReason(REVIEW_REASON_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__GENERATION_TRIGGER_FINGERPRINT:
				setGenerationTriggerFingerprint(GENERATION_TRIGGER_FINGERPRINT_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__COMPLIANCE_CHECK_TIME:
				setComplianceCheckTime(COMPLIANCE_CHECK_TIME_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__COMPLIANCE_STATUS:
				setComplianceStatus(COMPLIANCE_STATUS_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__GOVERNANCE_DOCUMENTATION_ID:
				setGovernanceDocumentationId(GOVERNANCE_DOCUMENTATION_ID_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__PROPERTIES:
				getProperties().clear();
				return;
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_USER:
				setLastChangeUser(LAST_CHANGE_USER_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_TIME:
				setLastChangeTime(LAST_CHANGE_TIME_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__STATUS:
				setStatus(STATUS_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__VERSION:
				setVersion(VERSION_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__OBJECT_REF:
				setObjectRef((EObject)null);
				return;
			case ManagementPackage.OBJECT_METADATA__OBJECT_ID:
				setObjectId(OBJECT_ID_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__OBJECT_NAME:
				setObjectName(OBJECT_NAME_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__ROLE:
				setRole(ROLE_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_REASON:
				setLastChangeReason(LAST_CHANGE_REASON_EDEFAULT);
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
			case ManagementPackage.OBJECT_METADATA__UPLOAD_USER:
				return UPLOAD_USER_EDEFAULT == null ? uploadUser != null : !UPLOAD_USER_EDEFAULT.equals(uploadUser);
			case ManagementPackage.OBJECT_METADATA__UPLOAD_TIME:
				return UPLOAD_TIME_EDEFAULT == null ? uploadTime != null : !UPLOAD_TIME_EDEFAULT.equals(uploadTime);
			case ManagementPackage.OBJECT_METADATA__SOURCE_CHANNEL:
				return SOURCE_CHANNEL_EDEFAULT == null ? sourceChannel != null : !SOURCE_CHANNEL_EDEFAULT.equals(sourceChannel);
			case ManagementPackage.OBJECT_METADATA__CONTENT_HASH:
				return CONTENT_HASH_EDEFAULT == null ? contentHash != null : !CONTENT_HASH_EDEFAULT.equals(contentHash);
			case ManagementPackage.OBJECT_METADATA__OBJECT_TYPE:
				return OBJECT_TYPE_EDEFAULT == null ? objectType != null : !OBJECT_TYPE_EDEFAULT.equals(objectType);
			case ManagementPackage.OBJECT_METADATA__REVIEW_USER:
				return REVIEW_USER_EDEFAULT == null ? reviewUser != null : !REVIEW_USER_EDEFAULT.equals(reviewUser);
			case ManagementPackage.OBJECT_METADATA__REVIEW_TIME:
				return REVIEW_TIME_EDEFAULT == null ? reviewTime != null : !REVIEW_TIME_EDEFAULT.equals(reviewTime);
			case ManagementPackage.OBJECT_METADATA__REVIEW_REASON:
				return REVIEW_REASON_EDEFAULT == null ? reviewReason != null : !REVIEW_REASON_EDEFAULT.equals(reviewReason);
			case ManagementPackage.OBJECT_METADATA__GENERATION_TRIGGER_FINGERPRINT:
				return GENERATION_TRIGGER_FINGERPRINT_EDEFAULT == null ? generationTriggerFingerprint != null : !GENERATION_TRIGGER_FINGERPRINT_EDEFAULT.equals(generationTriggerFingerprint);
			case ManagementPackage.OBJECT_METADATA__COMPLIANCE_CHECK_TIME:
				return COMPLIANCE_CHECK_TIME_EDEFAULT == null ? complianceCheckTime != null : !COMPLIANCE_CHECK_TIME_EDEFAULT.equals(complianceCheckTime);
			case ManagementPackage.OBJECT_METADATA__COMPLIANCE_STATUS:
				return COMPLIANCE_STATUS_EDEFAULT == null ? complianceStatus != null : !COMPLIANCE_STATUS_EDEFAULT.equals(complianceStatus);
			case ManagementPackage.OBJECT_METADATA__GOVERNANCE_DOCUMENTATION_ID:
				return GOVERNANCE_DOCUMENTATION_ID_EDEFAULT == null ? governanceDocumentationId != null : !GOVERNANCE_DOCUMENTATION_ID_EDEFAULT.equals(governanceDocumentationId);
			case ManagementPackage.OBJECT_METADATA__PROPERTIES:
				return properties != null && !properties.isEmpty();
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_USER:
				return LAST_CHANGE_USER_EDEFAULT == null ? lastChangeUser != null : !LAST_CHANGE_USER_EDEFAULT.equals(lastChangeUser);
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_TIME:
				return LAST_CHANGE_TIME_EDEFAULT == null ? lastChangeTime != null : !LAST_CHANGE_TIME_EDEFAULT.equals(lastChangeTime);
			case ManagementPackage.OBJECT_METADATA__STATUS:
				return status != STATUS_EDEFAULT;
			case ManagementPackage.OBJECT_METADATA__VERSION:
				return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
			case ManagementPackage.OBJECT_METADATA__OBJECT_REF:
				return objectRef != null;
			case ManagementPackage.OBJECT_METADATA__OBJECT_ID:
				return OBJECT_ID_EDEFAULT == null ? objectId != null : !OBJECT_ID_EDEFAULT.equals(objectId);
			case ManagementPackage.OBJECT_METADATA__OBJECT_NAME:
				return OBJECT_NAME_EDEFAULT == null ? objectName != null : !OBJECT_NAME_EDEFAULT.equals(objectName);
			case ManagementPackage.OBJECT_METADATA__ROLE:
				return ROLE_EDEFAULT == null ? role != null : !ROLE_EDEFAULT.equals(role);
			case ManagementPackage.OBJECT_METADATA__LAST_CHANGE_REASON:
				return LAST_CHANGE_REASON_EDEFAULT == null ? lastChangeReason != null : !LAST_CHANGE_REASON_EDEFAULT.equals(lastChangeReason);
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
		result.append(" (uploadUser: ");
		result.append(uploadUser);
		result.append(", uploadTime: ");
		result.append(uploadTime);
		result.append(", sourceChannel: ");
		result.append(sourceChannel);
		result.append(", contentHash: ");
		result.append(contentHash);
		result.append(", objectType: ");
		result.append(objectType);
		result.append(", reviewUser: ");
		result.append(reviewUser);
		result.append(", reviewTime: ");
		result.append(reviewTime);
		result.append(", reviewReason: ");
		result.append(reviewReason);
		result.append(", generationTriggerFingerprint: ");
		result.append(generationTriggerFingerprint);
		result.append(", complianceCheckTime: ");
		result.append(complianceCheckTime);
		result.append(", complianceStatus: ");
		result.append(complianceStatus);
		result.append(", governanceDocumentationId: ");
		result.append(governanceDocumentationId);
		result.append(", lastChangeUser: ");
		result.append(lastChangeUser);
		result.append(", lastChangeTime: ");
		result.append(lastChangeTime);
		result.append(", status: ");
		result.append(status);
		result.append(", version: ");
		result.append(version);
		result.append(", objectId: ");
		result.append(objectId);
		result.append(", objectName: ");
		result.append(objectName);
		result.append(", role: ");
		result.append(role);
		result.append(", lastChangeReason: ");
		result.append(lastChangeReason);
		result.append(')');
		return result.toString();
	}

} //ObjectMetadataImpl
