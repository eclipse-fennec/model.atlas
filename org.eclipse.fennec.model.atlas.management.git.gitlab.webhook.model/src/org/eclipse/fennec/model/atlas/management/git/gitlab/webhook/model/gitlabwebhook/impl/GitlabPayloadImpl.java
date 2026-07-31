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

import java.lang.reflect.InvocationTargetException;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload;
import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage;
import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitProvider;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.WebhookPayloadImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Gitlab Payload</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabPayloadImpl#getProject <em>Project</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabPayloadImpl#getCommits <em>Commits</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GitlabPayloadImpl extends WebhookPayloadImpl implements GitlabPayload {
	/**
	 * The cached value of the '{@link #getProject() <em>Project</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProject()
	 * @generated
	 * @ordered
	 */
	protected Project project;

	/**
	 * The cached value of the '{@link #getCommits() <em>Commits</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCommits()
	 * @generated
	 * @ordered
	 */
	protected EList<Commit> commits;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GitlabPayloadImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GitlabWebhookPackage.Literals.GITLAB_PAYLOAD;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Project getProject() {
		return project;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetProject(Project newProject, NotificationChain msgs) {
		Project oldProject = project;
		project = newProject;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GitlabWebhookPackage.GITLAB_PAYLOAD__PROJECT, oldProject, newProject);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setProject(Project newProject) {
		if (newProject != project) {
			NotificationChain msgs = null;
			if (project != null)
				msgs = ((InternalEObject)project).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GitlabWebhookPackage.GITLAB_PAYLOAD__PROJECT, null, msgs);
			if (newProject != null)
				msgs = ((InternalEObject)newProject).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GitlabWebhookPackage.GITLAB_PAYLOAD__PROJECT, null, msgs);
			msgs = basicSetProject(newProject, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GitlabWebhookPackage.GITLAB_PAYLOAD__PROJECT, newProject, newProject));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Commit> getCommits() {
		if (commits == null) {
			commits = new EObjectContainmentEList<Commit>(Commit.class, this, GitlabWebhookPackage.GITLAB_PAYLOAD__COMMITS);
		}
		return commits;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GitProvider getProvider() {
		return GitProvider.GITLAB;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCloneUrl() {
		return getProject() == null ? null : getProject().getGitHttpUrl();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRepositoryFullName() {
		return getProject() == null ? null : getProject().getPathWithNamespace(); 
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getAddedPaths() {
		java.util.LinkedHashSet<String> paths = new java.util.LinkedHashSet<>();
		for (Commit c : getCommits()) {
			paths.addAll(c.getAdded());
		 }
		return new org.eclipse.emf.common.util.BasicEList<>(paths);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getModifiedPaths() {
		java.util.LinkedHashSet<String> paths = new java.util.LinkedHashSet<>();
		for (Commit c : getCommits()) {
			paths.addAll(c.getModified());
		 }
		return new org.eclipse.emf.common.util.BasicEList<>(paths);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getRemovedPaths() {
		java.util.LinkedHashSet<String> paths = new java.util.LinkedHashSet<>();
		for (Commit c : getCommits()) {
			paths.addAll(c.getRemoved());
		 }
		return new org.eclipse.emf.common.util.BasicEList<>(paths);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case GitlabWebhookPackage.GITLAB_PAYLOAD__PROJECT:
				return basicSetProject(null, msgs);
			case GitlabWebhookPackage.GITLAB_PAYLOAD__COMMITS:
				return ((InternalEList<?>)getCommits()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GitlabWebhookPackage.GITLAB_PAYLOAD__PROJECT:
				return getProject();
			case GitlabWebhookPackage.GITLAB_PAYLOAD__COMMITS:
				return getCommits();
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
			case GitlabWebhookPackage.GITLAB_PAYLOAD__PROJECT:
				setProject((Project)newValue);
				return;
			case GitlabWebhookPackage.GITLAB_PAYLOAD__COMMITS:
				getCommits().clear();
				getCommits().addAll((Collection<? extends Commit>)newValue);
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
			case GitlabWebhookPackage.GITLAB_PAYLOAD__PROJECT:
				setProject((Project)null);
				return;
			case GitlabWebhookPackage.GITLAB_PAYLOAD__COMMITS:
				getCommits().clear();
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
			case GitlabWebhookPackage.GITLAB_PAYLOAD__PROJECT:
				return project != null;
			case GitlabWebhookPackage.GITLAB_PAYLOAD__COMMITS:
				return commits != null && !commits.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case GitlabWebhookPackage.GITLAB_PAYLOAD___GET_PROVIDER:
				return getProvider();
			case GitlabWebhookPackage.GITLAB_PAYLOAD___GET_CLONE_URL:
				return getCloneUrl();
			case GitlabWebhookPackage.GITLAB_PAYLOAD___GET_REPOSITORY_FULL_NAME:
				return getRepositoryFullName();
			case GitlabWebhookPackage.GITLAB_PAYLOAD___GET_ADDED_PATHS:
				return getAddedPaths();
			case GitlabWebhookPackage.GITLAB_PAYLOAD___GET_MODIFIED_PATHS:
				return getModifiedPaths();
			case GitlabWebhookPackage.GITLAB_PAYLOAD___GET_REMOVED_PATHS:
				return getRemovedPaths();
		}
		return super.eInvoke(operationID, arguments);
	}

} //GitlabPayloadImpl
