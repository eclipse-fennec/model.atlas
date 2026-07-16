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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Webhook Payload</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Provider-neutral base for an inbound push webhook. Fields whose JSON key is identical across GitHub and GitLab (ref, before, after) are modelled as attributes and populated directly by the codec. Everything that differs per provider (clone URL, repository name, the added/modified/removed change set, and the created/deleted/forced flags) is exposed as an EOperation that each concrete provider payload overrides via a GenModel 'body' annotation. Downstream (GitSyncService) consumes only this abstract type.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getRef <em>Ref</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getBefore <em>Before</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getAfter <em>After</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage#getWebhookPayload()
 * @model abstract="true"
 * @generated
 */
@ProviderType
public interface WebhookPayload extends EObject {
	/**
	 * Returns the value of the '<em><b>Ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The full git ref of the push, e.g. refs/heads/main. Same JSON key on GitHub and GitLab.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Ref</em>' attribute.
	 * @see #setRef(String)
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage#getWebhookPayload_Ref()
	 * @model
	 * @generated
	 */
	String getRef();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getRef <em>Ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ref</em>' attribute.
	 * @see #getRef()
	 * @generated
	 */
	void setRef(String value);

	/**
	 * Returns the value of the '<em><b>Before</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * SHA of the ref before the push (all-zero SHA when the branch was created). Same JSON key on GitHub and GitLab.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Before</em>' attribute.
	 * @see #setBefore(String)
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage#getWebhookPayload_Before()
	 * @model
	 * @generated
	 */
	String getBefore();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getBefore <em>Before</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Before</em>' attribute.
	 * @see #getBefore()
	 * @generated
	 */
	void setBefore(String value);

	/**
	 * Returns the value of the '<em><b>After</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * SHA of the ref after the push (all-zero SHA when the branch was deleted). Same JSON key on GitHub and GitLab.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>After</em>' attribute.
	 * @see #setAfter(String)
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage#getWebhookPayload_After()
	 * @model
	 * @generated
	 */
	String getAfter();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getAfter <em>After</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>After</em>' attribute.
	 * @see #getAfter()
	 * @generated
	 */
	void setAfter(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Which git host this payload came from.
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	GitProvider getProvider();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * HTTP(S) fetch/clone URL of the repository (GitHub repository.clone_url; GitLab project.git_http_url).
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getCloneUrl();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Namespaced repository identifier (GitHub repository.full_name; GitLab project.path_with_namespace).
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getRepositoryFullName();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Repository-relative paths added by this push, aggregated across the payload's commits.
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	EList<String> getAddedPaths();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Repository-relative paths modified by this push, aggregated across the payload's commits.
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	EList<String> getModifiedPaths();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Repository-relative paths removed by this push, aggregated across the payload's commits.
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	EList<String> getRemovedPaths();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * True when this push created the ref (GitHub 'created' flag; GitLab: before is the all-zero SHA).
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='return getBefore() != null &amp;&amp; getBefore().matches(\"0+\");'"
	 * @generated
	 */
	boolean isCreated();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * True when this push deleted the ref (GitHub 'deleted' flag; GitLab: after is the all-zero SHA).
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	boolean isDeleted();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * True when this push was a force-push / non-fast-forward update (GitHub 'forced' flag; GitLab: derived).
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 *        annotation="http://www.eclipse.org/emf/2002/GenModel body='return false;'"
	 * @generated
	 */
	boolean isForced();

} // WebhookPayload
