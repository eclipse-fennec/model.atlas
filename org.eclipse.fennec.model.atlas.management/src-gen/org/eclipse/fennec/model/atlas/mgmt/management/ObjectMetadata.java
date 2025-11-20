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
package org.eclipse.fennec.model.atlas.mgmt.management;

import java.time.Instant;

import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Object Metadata</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Generic metadata associated with uploaded EObject
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getUploadUser <em>Upload User</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getUploadTime <em>Upload Time</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getSourceChannel <em>Source Channel</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getContentHash <em>Content Hash</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectType <em>Object Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewUser <em>Review User</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewTime <em>Review Time</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewReason <em>Review Reason</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getGenerationTriggerFingerprint <em>Generation Trigger Fingerprint</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getComplianceCheckTime <em>Compliance Check Time</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getComplianceStatus <em>Compliance Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getGovernanceDocumentationId <em>Governance Documentation Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeUser <em>Last Change User</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeTime <em>Last Change Time</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getStatus <em>Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectRef <em>Object Ref</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectId <em>Object Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectName <em>Object Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getRole <em>Role</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeReason <em>Last Change Reason</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getStorageId <em>Storage Id</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata()
 * @model
 * @generated
 */
@ProviderType
public interface ObjectMetadata extends EObject {
	/**
	 * Returns the value of the '<em><b>Upload User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * User who uploaded the object
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Upload User</em>' attribute.
	 * @see #setUploadUser(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_UploadUser()
	 * @model required="true"
	 * @generated
	 */
	String getUploadUser();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getUploadUser <em>Upload User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Upload User</em>' attribute.
	 * @see #getUploadUser()
	 * @generated
	 */
	void setUploadUser(String value);

	/**
	 * Returns the value of the '<em><b>Upload Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Timestamp when object was uploaded
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Upload Time</em>' attribute.
	 * @see #setUploadTime(Instant)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_UploadTime()
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Instant" required="true"
	 * @generated
	 */
	Instant getUploadTime();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getUploadTime <em>Upload Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Upload Time</em>' attribute.
	 * @see #getUploadTime()
	 * @generated
	 */
	void setUploadTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Source Channel</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Source channel (e.g., AI_GENERATOR, MANUAL_UPLOAD, SENSINACT)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source Channel</em>' attribute.
	 * @see #setSourceChannel(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_SourceChannel()
	 * @model required="true"
	 * @generated
	 */
	String getSourceChannel();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getSourceChannel <em>Source Channel</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Channel</em>' attribute.
	 * @see #getSourceChannel()
	 * @generated
	 */
	void setSourceChannel(String value);

	/**
	 * Returns the value of the '<em><b>Content Hash</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * SHA-256 hash of XMI content
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Content Hash</em>' attribute.
	 * @see #setContentHash(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_ContentHash()
	 * @model required="true"
	 * @generated
	 */
	String getContentHash();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getContentHash <em>Content Hash</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Content Hash</em>' attribute.
	 * @see #getContentHash()
	 * @generated
	 */
	void setContentHash(String value);

	/**
	 * Returns the value of the '<em><b>Object Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Type of the EObject (e.g., EPackage, Route, SensorModel)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Object Type</em>' attribute.
	 * @see #setObjectType(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_ObjectType()
	 * @model required="true"
	 * @generated
	 */
	String getObjectType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectType <em>Object Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Object Type</em>' attribute.
	 * @see #getObjectType()
	 * @generated
	 */
	void setObjectType(String value);

	/**
	 * Returns the value of the '<em><b>Review User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * User who reviewed the object (for approve/reject)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Review User</em>' attribute.
	 * @see #setReviewUser(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_ReviewUser()
	 * @model
	 * @generated
	 */
	String getReviewUser();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewUser <em>Review User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Review User</em>' attribute.
	 * @see #getReviewUser()
	 * @generated
	 */
	void setReviewUser(String value);

	/**
	 * Returns the value of the '<em><b>Review Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Timestamp when object was reviewed
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Review Time</em>' attribute.
	 * @see #setReviewTime(Instant)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_ReviewTime()
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Instant"
	 * @generated
	 */
	Instant getReviewTime();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewTime <em>Review Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Review Time</em>' attribute.
	 * @see #getReviewTime()
	 * @generated
	 */
	void setReviewTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Review Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reason for approval or rejection
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Review Reason</em>' attribute.
	 * @see #setReviewReason(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_ReviewReason()
	 * @model
	 * @generated
	 */
	String getReviewReason();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getReviewReason <em>Review Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Review Reason</em>' attribute.
	 * @see #getReviewReason()
	 * @generated
	 */
	void setReviewReason(String value);

	/**
	 * Returns the value of the '<em><b>Generation Trigger Fingerprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * This is the fingerprint of the json which triggered the generation of this Object
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Generation Trigger Fingerprint</em>' attribute.
	 * @see #setGenerationTriggerFingerprint(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_GenerationTriggerFingerprint()
	 * @model
	 * @generated
	 */
	String getGenerationTriggerFingerprint();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getGenerationTriggerFingerprint <em>Generation Trigger Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Generation Trigger Fingerprint</em>' attribute.
	 * @see #getGenerationTriggerFingerprint()
	 * @generated
	 */
	void setGenerationTriggerFingerprint(String value);

	/**
	 * Returns the value of the '<em><b>Compliance Check Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Timestamp when compliance checks were last performed
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Compliance Check Time</em>' attribute.
	 * @see #setComplianceCheckTime(Instant)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_ComplianceCheckTime()
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Instant"
	 * @generated
	 */
	Instant getComplianceCheckTime();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getComplianceCheckTime <em>Compliance Check Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Compliance Check Time</em>' attribute.
	 * @see #getComplianceCheckTime()
	 * @generated
	 */
	void setComplianceCheckTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Compliance Status</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Overall compliance check result (uses governance.ComplianceStatus values as strings)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Compliance Status</em>' attribute.
	 * @see #setComplianceStatus(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_ComplianceStatus()
	 * @model
	 * @generated
	 */
	String getComplianceStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getComplianceStatus <em>Compliance Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Compliance Status</em>' attribute.
	 * @see #getComplianceStatus()
	 * @generated
	 */
	void setComplianceStatus(String value);

	/**
	 * Returns the value of the '<em><b>Governance Documentation Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference to the governance documentation containing detailed compliance results
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Governance Documentation Id</em>' attribute.
	 * @see #setGovernanceDocumentationId(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_GovernanceDocumentationId()
	 * @model
	 * @generated
	 */
	String getGovernanceDocumentationId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getGovernanceDocumentationId <em>Governance Documentation Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Governance Documentation Id</em>' attribute.
	 * @see #getGovernanceDocumentationId()
	 * @generated
	 */
	void setGovernanceDocumentationId(String value);

	/**
	 * Returns the value of the '<em><b>Properties</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link java.lang.Object},
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Additional metadata properties
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Properties</em>' map.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_Properties()
	 * @model mapType="org.eclipse.fennec.model.atlas.mgmt.management.StringToObjectMapEntry&lt;org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EJavaObject&gt;"
	 * @generated
	 */
	EMap<String, Object> getProperties();

	/**
	 * Returns the value of the '<em><b>Last Change User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * User who last modified the object (only set when object is updated after initial upload)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Last Change User</em>' attribute.
	 * @see #setLastChangeUser(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_LastChangeUser()
	 * @model
	 * @generated
	 */
	String getLastChangeUser();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeUser <em>Last Change User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last Change User</em>' attribute.
	 * @see #getLastChangeUser()
	 * @generated
	 */
	void setLastChangeUser(String value);

	/**
	 * Returns the value of the '<em><b>Last Change Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Timestamp when object was last modified (only set when object is updated after initial upload)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Last Change Time</em>' attribute.
	 * @see #setLastChangeTime(Instant)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_LastChangeTime()
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Instant"
	 * @generated
	 */
	Instant getLastChangeTime();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeTime <em>Last Change Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last Change Time</em>' attribute.
	 * @see #getLastChangeTime()
	 * @generated
	 */
	void setLastChangeTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * The default value is <code>"DRAFT"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Object lifecycle status
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus
	 * @see #setStatus(ObjectStatus)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_Status()
	 * @model default="DRAFT" required="true"
	 * @generated
	 */
	ObjectStatus getStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(ObjectStatus value);

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Object version for optimistic locking
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_Version()
	 * @model
	 * @generated
	 */
	String getVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(String value);

	/**
	 * Returns the value of the '<em><b>Object Ref</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference to the actual managed EObject
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Object Ref</em>' reference.
	 * @see #setObjectRef(EObject)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_ObjectRef()
	 * @model
	 * @generated
	 */
	EObject getObjectRef();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectRef <em>Object Ref</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Object Ref</em>' reference.
	 * @see #getObjectRef()
	 * @generated
	 */
	void setObjectRef(EObject value);

	/**
	 * Returns the value of the '<em><b>Object Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unique metadata identifier (distinct from storage objectId key) for cross-referencing and registry lookups
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Object Id</em>' attribute.
	 * @see #setObjectId(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_ObjectId()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getObjectId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectId <em>Object Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Object Id</em>' attribute.
	 * @see #getObjectId()
	 * @generated
	 */
	void setObjectId(String value);

	/**
	 * Returns the value of the '<em><b>Object Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Human-readable object name
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Object Name</em>' attribute.
	 * @see #setObjectName(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_ObjectName()
	 * @model
	 * @generated
	 */
	String getObjectName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getObjectName <em>Object Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Object Name</em>' attribute.
	 * @see #getObjectName()
	 * @generated
	 */
	void setObjectName(String value);

	/**
	 * Returns the value of the '<em><b>Role</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Storage role (draft, approved, documentation) - indicates which storage backend this metadata represents
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Role</em>' attribute.
	 * @see #setRole(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_Role()
	 * @model required="true"
	 * @generated
	 */
	String getRole();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getRole <em>Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Role</em>' attribute.
	 * @see #getRole()
	 * @generated
	 */
	void setRole(String value);

	/**
	 * Returns the value of the '<em><b>Last Change Reason</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reason for approval or rejection
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Last Change Reason</em>' attribute.
	 * @see #setLastChangeReason(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_LastChangeReason()
	 * @model
	 * @generated
	 */
	String getLastChangeReason();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getLastChangeReason <em>Last Change Reason</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last Change Reason</em>' attribute.
	 * @see #getLastChangeReason()
	 * @generated
	 */
	void setLastChangeReason(String value);

	/**
	 * Returns the value of the '<em><b>Storage Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * This attribute is used when retrieving an object or checking for its existence. If this is not set, the objectId will be used. This is needed when the storage assigns a different id to the object when stored.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Storage Id</em>' attribute.
	 * @see #setStorageId(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getObjectMetadata_StorageId()
	 * @model
	 * @generated
	 */
	String getStorageId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata#getStorageId <em>Storage Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Storage Id</em>' attribute.
	 * @see #getStorageId()
	 * @generated
	 */
	void setStorageId(String value);

} // ObjectMetadata
