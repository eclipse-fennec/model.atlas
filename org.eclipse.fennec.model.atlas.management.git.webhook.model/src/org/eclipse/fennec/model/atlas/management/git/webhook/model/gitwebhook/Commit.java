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
 * A representation of the model object '<em><b>Commit</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getAdded <em>Added</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getModified <em>Modified</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getRemoved <em>Removed</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage#getCommit()
 * @model
 * @generated
 */
@ProviderType
public interface Commit extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage#getCommit_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Added</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Added</em>' attribute list.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage#getCommit_Added()
	 * @model
	 * @generated
	 */
	EList<String> getAdded();

	/**
	 * Returns the value of the '<em><b>Modified</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Modified</em>' attribute list.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage#getCommit_Modified()
	 * @model
	 * @generated
	 */
	EList<String> getModified();

	/**
	 * Returns the value of the '<em><b>Removed</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Removed</em>' attribute list.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage#getCommit_Removed()
	 * @model
	 * @generated
	 */
	EList<String> getRemoved();

} // Commit
