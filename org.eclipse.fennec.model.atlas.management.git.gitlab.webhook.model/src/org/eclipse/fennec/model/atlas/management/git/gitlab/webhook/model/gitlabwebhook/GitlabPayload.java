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
package org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook;

import org.eclipse.emf.common.util.EList;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitProvider;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Gitlab Payload</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Concrete GitLab push webhook payload. Only the fields the git backend reads are modelled; the codec skips all other JSON properties (strictOnUnknown=false). Implements the neutral WebhookPayload operations from GitLab-specific fields. GitLab sends no head_commit and no created/deleted/forced flags; created/deleted are derived from the before/after SHAs.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getProject <em>Project</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getCommits <em>Commits</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage#getGitlabPayload()
 * @model
 * @generated
 */
@ProviderType
public interface GitlabPayload extends WebhookPayload {
	/**
	 * Returns the value of the '<em><b>Project</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Project</em>' containment reference.
	 * @see #setProject(Project)
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage#getGitlabPayload_Project()
	 * @model containment="true"
	 * @generated
	 */
	Project getProject();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getProject <em>Project</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Project</em>' containment reference.
	 * @see #getProject()
	 * @generated
	 */
	void setProject(Project value);

	/**
	 * Returns the value of the '<em><b>Commits</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Commits</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage#getGitlabPayload_Commits()
	 * @model containment="true"
	 * @generated
	 */
	EList<Commit> getCommits();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='return GitProvider.GITLAB;'"
	 * @generated
	 */
	GitProvider getProvider();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='return getProject() == null ? null : getProject().getGitHttpUrl();'"
	 * @generated
	 */
	String getCloneUrl();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='return getProject() == null ? null : getProject().getPathWithNamespace(); '"
	 * @generated
	 */
	String getRepositoryFullName();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='java.util.LinkedHashSet&lt;String&gt; paths = new java.util.LinkedHashSet&lt;&gt;();\nfor (Commit c : getCommits()) {\n\tpaths.addAll(c.getAdded());\n }\nreturn new org.eclipse.emf.common.util.BasicEList&lt;&gt;(paths);'"
	 * @generated
	 */
	EList<String> getAddedPaths();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='java.util.LinkedHashSet&lt;String&gt; paths = new java.util.LinkedHashSet&lt;&gt;();\nfor (Commit c : getCommits()) {\n\tpaths.addAll(c.getModified());\n }\nreturn new org.eclipse.emf.common.util.BasicEList&lt;&gt;(paths);'"
	 * @generated
	 */
	EList<String> getModifiedPaths();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='java.util.LinkedHashSet&lt;String&gt; paths = new java.util.LinkedHashSet&lt;&gt;();\nfor (Commit c : getCommits()) {\n\tpaths.addAll(c.getRemoved());\n }\nreturn new org.eclipse.emf.common.util.BasicEList&lt;&gt;(paths);'"
	 * @generated
	 */
	EList<String> getRemovedPaths();

} // GitlabPayload
