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
package org.eclipse.fennec.model.atlas.mgmt.management.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Object Query</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectQueryImpl#getUploadUser <em>Upload User</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectQueryImpl#getSourceChannel <em>Source Channel</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectQueryImpl#getObjectType <em>Object Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.ObjectQueryImpl#getStatus <em>Status</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ObjectQueryImpl extends MinimalEObjectImpl.Container implements ObjectQuery {
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ObjectQueryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ManagementPackage.Literals.OBJECT_QUERY;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_QUERY__UPLOAD_USER, oldUploadUser, uploadUser));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_QUERY__SOURCE_CHANNEL, oldSourceChannel, sourceChannel));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_QUERY__OBJECT_TYPE, oldObjectType, objectType));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.OBJECT_QUERY__STATUS, oldStatus, status));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ManagementPackage.OBJECT_QUERY__UPLOAD_USER:
				return getUploadUser();
			case ManagementPackage.OBJECT_QUERY__SOURCE_CHANNEL:
				return getSourceChannel();
			case ManagementPackage.OBJECT_QUERY__OBJECT_TYPE:
				return getObjectType();
			case ManagementPackage.OBJECT_QUERY__STATUS:
				return getStatus();
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
			case ManagementPackage.OBJECT_QUERY__UPLOAD_USER:
				setUploadUser((String)newValue);
				return;
			case ManagementPackage.OBJECT_QUERY__SOURCE_CHANNEL:
				setSourceChannel((String)newValue);
				return;
			case ManagementPackage.OBJECT_QUERY__OBJECT_TYPE:
				setObjectType((String)newValue);
				return;
			case ManagementPackage.OBJECT_QUERY__STATUS:
				setStatus((ObjectStatus)newValue);
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
			case ManagementPackage.OBJECT_QUERY__UPLOAD_USER:
				setUploadUser(UPLOAD_USER_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_QUERY__SOURCE_CHANNEL:
				setSourceChannel(SOURCE_CHANNEL_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_QUERY__OBJECT_TYPE:
				setObjectType(OBJECT_TYPE_EDEFAULT);
				return;
			case ManagementPackage.OBJECT_QUERY__STATUS:
				setStatus(STATUS_EDEFAULT);
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
			case ManagementPackage.OBJECT_QUERY__UPLOAD_USER:
				return UPLOAD_USER_EDEFAULT == null ? uploadUser != null : !UPLOAD_USER_EDEFAULT.equals(uploadUser);
			case ManagementPackage.OBJECT_QUERY__SOURCE_CHANNEL:
				return SOURCE_CHANNEL_EDEFAULT == null ? sourceChannel != null : !SOURCE_CHANNEL_EDEFAULT.equals(sourceChannel);
			case ManagementPackage.OBJECT_QUERY__OBJECT_TYPE:
				return OBJECT_TYPE_EDEFAULT == null ? objectType != null : !OBJECT_TYPE_EDEFAULT.equals(objectType);
			case ManagementPackage.OBJECT_QUERY__STATUS:
				return status != STATUS_EDEFAULT;
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
		result.append(", sourceChannel: ");
		result.append(sourceChannel);
		result.append(", objectType: ");
		result.append(objectType);
		result.append(", status: ");
		result.append(status);
		result.append(')');
		return result.toString();
	}

} //ObjectQueryImpl
