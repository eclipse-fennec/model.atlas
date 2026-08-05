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
package org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage;
import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Project</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.ProjectImpl#getGitHttpUrl <em>Git Http Url</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.ProjectImpl#getPathWithNamespace <em>Path With Namespace</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ProjectImpl extends MinimalEObjectImpl.Container implements Project {
	/**
	 * The default value of the '{@link #getGitHttpUrl() <em>Git Http Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGitHttpUrl()
	 * @generated
	 * @ordered
	 */
	protected static final String GIT_HTTP_URL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getGitHttpUrl() <em>Git Http Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGitHttpUrl()
	 * @generated
	 * @ordered
	 */
	protected String gitHttpUrl = GIT_HTTP_URL_EDEFAULT;

	/**
	 * The default value of the '{@link #getPathWithNamespace() <em>Path With Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPathWithNamespace()
	 * @generated
	 * @ordered
	 */
	protected static final String PATH_WITH_NAMESPACE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPathWithNamespace() <em>Path With Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPathWithNamespace()
	 * @generated
	 * @ordered
	 */
	protected String pathWithNamespace = PATH_WITH_NAMESPACE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ProjectImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GitlabWebhookPackage.Literals.PROJECT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getGitHttpUrl() {
		return gitHttpUrl;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setGitHttpUrl(String newGitHttpUrl) {
		String oldGitHttpUrl = gitHttpUrl;
		gitHttpUrl = newGitHttpUrl;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GitlabWebhookPackage.PROJECT__GIT_HTTP_URL, oldGitHttpUrl, gitHttpUrl));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getPathWithNamespace() {
		return pathWithNamespace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPathWithNamespace(String newPathWithNamespace) {
		String oldPathWithNamespace = pathWithNamespace;
		pathWithNamespace = newPathWithNamespace;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GitlabWebhookPackage.PROJECT__PATH_WITH_NAMESPACE, oldPathWithNamespace, pathWithNamespace));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GitlabWebhookPackage.PROJECT__GIT_HTTP_URL:
				return getGitHttpUrl();
			case GitlabWebhookPackage.PROJECT__PATH_WITH_NAMESPACE:
				return getPathWithNamespace();
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
			case GitlabWebhookPackage.PROJECT__GIT_HTTP_URL:
				setGitHttpUrl((String)newValue);
				return;
			case GitlabWebhookPackage.PROJECT__PATH_WITH_NAMESPACE:
				setPathWithNamespace((String)newValue);
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
			case GitlabWebhookPackage.PROJECT__GIT_HTTP_URL:
				setGitHttpUrl(GIT_HTTP_URL_EDEFAULT);
				return;
			case GitlabWebhookPackage.PROJECT__PATH_WITH_NAMESPACE:
				setPathWithNamespace(PATH_WITH_NAMESPACE_EDEFAULT);
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
			case GitlabWebhookPackage.PROJECT__GIT_HTTP_URL:
				return GIT_HTTP_URL_EDEFAULT == null ? gitHttpUrl != null : !GIT_HTTP_URL_EDEFAULT.equals(gitHttpUrl);
			case GitlabWebhookPackage.PROJECT__PATH_WITH_NAMESPACE:
				return PATH_WITH_NAMESPACE_EDEFAULT == null ? pathWithNamespace != null : !PATH_WITH_NAMESPACE_EDEFAULT.equals(pathWithNamespace);
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
		result.append(" (gitHttpUrl: ");
		result.append(gitHttpUrl);
		result.append(", pathWithNamespace: ");
		result.append(pathWithNamespace);
		result.append(')');
		return result.toString();
	}

} //ProjectImpl
