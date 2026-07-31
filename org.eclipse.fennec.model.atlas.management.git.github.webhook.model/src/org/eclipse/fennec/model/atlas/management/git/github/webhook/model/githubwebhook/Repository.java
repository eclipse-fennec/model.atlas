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
package org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Repository</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository#getCloneUrl <em>Clone Url</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository#getFullName <em>Full Name</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage#getRepository()
 * @model
 * @generated
 */
@ProviderType
public interface Repository extends EObject {
	/**
	 * Returns the value of the '<em><b>Clone Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Clone Url</em>' attribute.
	 * @see #setCloneUrl(String)
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage#getRepository_CloneUrl()
	 * @model extendedMetaData="name='clone_url'"
	 * @generated
	 */
	String getCloneUrl();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository#getCloneUrl <em>Clone Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Clone Url</em>' attribute.
	 * @see #getCloneUrl()
	 * @generated
	 */
	void setCloneUrl(String value);

	/**
	 * Returns the value of the '<em><b>Full Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Full Name</em>' attribute.
	 * @see #setFullName(String)
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage#getRepository_FullName()
	 * @model extendedMetaData="name='full_name'"
	 * @generated
	 */
	String getFullName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository#getFullName <em>Full Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Full Name</em>' attribute.
	 * @see #getFullName()
	 * @generated
	 */
	void setFullName(String value);

} // Repository
