/**
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
/*
 */
package org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Project</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project#getGitHttpUrl <em>Git Http Url</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project#getPathWithNamespace <em>Path With Namespace</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage#getProject()
 * @model
 * @generated
 */
@ProviderType
public interface Project extends EObject {
	/**
	 * Returns the value of the '<em><b>Git Http Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Git Http Url</em>' attribute.
	 * @see #setGitHttpUrl(String)
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage#getProject_GitHttpUrl()
	 * @model extendedMetaData="name='git_http_url'"
	 * @generated
	 */
	String getGitHttpUrl();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project#getGitHttpUrl <em>Git Http Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Git Http Url</em>' attribute.
	 * @see #getGitHttpUrl()
	 * @generated
	 */
	void setGitHttpUrl(String value);

	/**
	 * Returns the value of the '<em><b>Path With Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Path With Namespace</em>' attribute.
	 * @see #setPathWithNamespace(String)
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage#getProject_PathWithNamespace()
	 * @model extendedMetaData="name='path_with_namespace'"
	 * @generated
	 */
	String getPathWithNamespace();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project#getPathWithNamespace <em>Path With Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Path With Namespace</em>' attribute.
	 * @see #getPathWithNamespace()
	 * @generated
	 */
	void setPathWithNamespace(String value);

} // Project
