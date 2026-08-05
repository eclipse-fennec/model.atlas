/*
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Commit</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.CommitImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.CommitImpl#getAdded <em>Added</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.CommitImpl#getModified <em>Modified</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.CommitImpl#getRemoved <em>Removed</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CommitImpl extends MinimalEObjectImpl.Container implements Commit {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAdded() <em>Added</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAdded()
	 * @generated
	 * @ordered
	 */
	protected EList<String> added;

	/**
	 * The cached value of the '{@link #getModified() <em>Modified</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getModified()
	 * @generated
	 * @ordered
	 */
	protected EList<String> modified;

	/**
	 * The cached value of the '{@link #getRemoved() <em>Removed</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRemoved()
	 * @generated
	 * @ordered
	 */
	protected EList<String> removed;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CommitImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GitWebhookPackage.Literals.COMMIT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GitWebhookPackage.COMMIT__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getAdded() {
		if (added == null) {
			added = new EDataTypeUniqueEList<String>(String.class, this, GitWebhookPackage.COMMIT__ADDED);
		}
		return added;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getModified() {
		if (modified == null) {
			modified = new EDataTypeUniqueEList<String>(String.class, this, GitWebhookPackage.COMMIT__MODIFIED);
		}
		return modified;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getRemoved() {
		if (removed == null) {
			removed = new EDataTypeUniqueEList<String>(String.class, this, GitWebhookPackage.COMMIT__REMOVED);
		}
		return removed;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GitWebhookPackage.COMMIT__ID:
				return getId();
			case GitWebhookPackage.COMMIT__ADDED:
				return getAdded();
			case GitWebhookPackage.COMMIT__MODIFIED:
				return getModified();
			case GitWebhookPackage.COMMIT__REMOVED:
				return getRemoved();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case GitWebhookPackage.COMMIT__ID:
				setId((String)newValue);
				return;
			case GitWebhookPackage.COMMIT__ADDED:
				getAdded().clear();
				getAdded().addAll((Collection<? extends String>)newValue);
				return;
			case GitWebhookPackage.COMMIT__MODIFIED:
				getModified().clear();
				getModified().addAll((Collection<? extends String>)newValue);
				return;
			case GitWebhookPackage.COMMIT__REMOVED:
				getRemoved().clear();
				getRemoved().addAll((Collection<? extends String>)newValue);
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
			case GitWebhookPackage.COMMIT__ID:
				setId(ID_EDEFAULT);
				return;
			case GitWebhookPackage.COMMIT__ADDED:
				getAdded().clear();
				return;
			case GitWebhookPackage.COMMIT__MODIFIED:
				getModified().clear();
				return;
			case GitWebhookPackage.COMMIT__REMOVED:
				getRemoved().clear();
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
			case GitWebhookPackage.COMMIT__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case GitWebhookPackage.COMMIT__ADDED:
				return added != null && !added.isEmpty();
			case GitWebhookPackage.COMMIT__MODIFIED:
				return modified != null && !modified.isEmpty();
			case GitWebhookPackage.COMMIT__REMOVED:
				return removed != null && !removed.isEmpty();
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
		result.append(" (id: ");
		result.append(id);
		result.append(", added: ");
		result.append(added);
		result.append(", modified: ");
		result.append(modified);
		result.append(", removed: ");
		result.append(removed);
		result.append(')');
		return result.toString();
	}

} //CommitImpl
