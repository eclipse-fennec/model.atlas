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
package org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook;

import org.eclipse.emf.common.util.EList;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitProvider;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Github Payload</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Concrete GitHub push webhook payload. Only the fields the git backend reads are modelled; the codec skips all other JSON properties (strictOnUnknown=false). Implements the neutral WebhookPayload operations from GitHub-specific fields.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getRepository <em>Repository</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getCommits <em>Commits</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getHeadCommit <em>Head Commit</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage#getGithubPayload()
 * @model
 * @generated
 */
@ProviderType
public interface GithubPayload extends WebhookPayload {
	/**
	 * Returns the value of the '<em><b>Repository</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Repository</em>' containment reference.
	 * @see #setRepository(Repository)
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage#getGithubPayload_Repository()
	 * @model containment="true"
	 * @generated
	 */
	Repository getRepository();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getRepository <em>Repository</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Repository</em>' containment reference.
	 * @see #getRepository()
	 * @generated
	 */
	void setRepository(Repository value);

	/**
	 * Returns the value of the '<em><b>Commits</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Commits</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage#getGithubPayload_Commits()
	 * @model containment="true"
	 * @generated
	 */
	EList<Commit> getCommits();

	/**
	 * Returns the value of the '<em><b>Head Commit</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Head Commit</em>' containment reference.
	 * @see #setHeadCommit(Commit)
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage#getGithubPayload_HeadCommit()
	 * @model containment="true"
	 *        extendedMetaData="name='head_commit'"
	 * @generated
	 */
	Commit getHeadCommit();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getHeadCommit <em>Head Commit</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Head Commit</em>' containment reference.
	 * @see #getHeadCommit()
	 * @generated
	 */
	void setHeadCommit(Commit value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='return GitProvider.GITHUB;'"
	 * @generated
	 */
	GitProvider getProvider();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='return getRepository() == null ? null : getRepository().getCloneUrl();'"
	 * @generated
	 */
	String getCloneUrl();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='return getRepository() == null ? null : getRepository().getFullName();'"
	 * @generated
	 */
	String getRepositoryFullName();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='java.util.LinkedHashSet&lt;String&gt; paths = new java.util.LinkedHashSet&lt;&gt;();\nfor(Commit c : getCommits()) {\n\tpaths.addAll(c.getAdded());\n}\nif(getHeadCommit() != null) {\n\tpaths.addAll(getHeadCommit().getAdded());\n}\nreturn new org.eclipse.emf.common.util.BasicEList&lt;&gt;(paths);'"
	 * @generated
	 */
	EList<String> getAddedPaths();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='java.util.LinkedHashSet&lt;String&gt; paths = new java.util.LinkedHashSet&lt;&gt;();\nfor(Commit c : getCommits()) {\n\tpaths.addAll(c.getModified());\n}\nif(getHeadCommit() != null) {\n\tpaths.addAll(getHeadCommit().getModified());\n}\nreturn new org.eclipse.emf.common.util.BasicEList&lt;&gt;(paths);'"
	 * @generated
	 */
	EList<String> getModifiedPaths();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='java.util.LinkedHashSet&lt;String&gt; paths = new java.util.LinkedHashSet&lt;&gt;();\nfor(Commit c : getCommits()) {\n\tpaths.addAll(c.getRemoved());\n}\nif(getHeadCommit() != null) {\n\tpaths.addAll(getHeadCommit().getRemoved());\n}\nreturn new org.eclipse.emf.common.util.BasicEList&lt;&gt;(paths);'"
	 * @generated
	 */
	EList<String> getRemovedPaths();

} // GithubPayload
