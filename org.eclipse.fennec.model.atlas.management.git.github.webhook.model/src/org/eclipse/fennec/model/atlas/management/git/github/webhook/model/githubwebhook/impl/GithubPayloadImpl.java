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
package org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl;

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

import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload;
import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage;
import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitProvider;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.WebhookPayloadImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Github Payload</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubPayloadImpl#getRepository <em>Repository</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubPayloadImpl#getCommits <em>Commits</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubPayloadImpl#getHeadCommit <em>Head Commit</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GithubPayloadImpl extends WebhookPayloadImpl implements GithubPayload {
	/**
	 * The cached value of the '{@link #getRepository() <em>Repository</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRepository()
	 * @generated
	 * @ordered
	 */
	protected Repository repository;

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
	 * The cached value of the '{@link #getHeadCommit() <em>Head Commit</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHeadCommit()
	 * @generated
	 * @ordered
	 */
	protected Commit headCommit;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GithubPayloadImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GithubWebhookPackage.Literals.GITHUB_PAYLOAD;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Repository getRepository() {
		return repository;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRepository(Repository newRepository, NotificationChain msgs) {
		Repository oldRepository = repository;
		repository = newRepository;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GithubWebhookPackage.GITHUB_PAYLOAD__REPOSITORY, oldRepository, newRepository);
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
	public void setRepository(Repository newRepository) {
		if (newRepository != repository) {
			NotificationChain msgs = null;
			if (repository != null)
				msgs = ((InternalEObject)repository).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GithubWebhookPackage.GITHUB_PAYLOAD__REPOSITORY, null, msgs);
			if (newRepository != null)
				msgs = ((InternalEObject)newRepository).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GithubWebhookPackage.GITHUB_PAYLOAD__REPOSITORY, null, msgs);
			msgs = basicSetRepository(newRepository, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GithubWebhookPackage.GITHUB_PAYLOAD__REPOSITORY, newRepository, newRepository));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Commit> getCommits() {
		if (commits == null) {
			commits = new EObjectContainmentEList<Commit>(Commit.class, this, GithubWebhookPackage.GITHUB_PAYLOAD__COMMITS);
		}
		return commits;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Commit getHeadCommit() {
		return headCommit;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetHeadCommit(Commit newHeadCommit, NotificationChain msgs) {
		Commit oldHeadCommit = headCommit;
		headCommit = newHeadCommit;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GithubWebhookPackage.GITHUB_PAYLOAD__HEAD_COMMIT, oldHeadCommit, newHeadCommit);
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
	public void setHeadCommit(Commit newHeadCommit) {
		if (newHeadCommit != headCommit) {
			NotificationChain msgs = null;
			if (headCommit != null)
				msgs = ((InternalEObject)headCommit).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GithubWebhookPackage.GITHUB_PAYLOAD__HEAD_COMMIT, null, msgs);
			if (newHeadCommit != null)
				msgs = ((InternalEObject)newHeadCommit).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GithubWebhookPackage.GITHUB_PAYLOAD__HEAD_COMMIT, null, msgs);
			msgs = basicSetHeadCommit(newHeadCommit, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GithubWebhookPackage.GITHUB_PAYLOAD__HEAD_COMMIT, newHeadCommit, newHeadCommit));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GitProvider getProvider() {
		return GitProvider.GITHUB;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCloneUrl() {
		return getRepository() == null ? null : getRepository().getCloneUrl();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRepositoryFullName() {
		return getRepository() == null ? null : getRepository().getFullName();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getAddedPaths() {
		java.util.LinkedHashSet<String> paths = new java.util.LinkedHashSet<>();
		for(Commit c : getCommits()) {
			paths.addAll(c.getAdded());
		}
		if(getHeadCommit() != null) {
			paths.addAll(getHeadCommit().getAdded());
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
		for(Commit c : getCommits()) {
			paths.addAll(c.getModified());
		}
		if(getHeadCommit() != null) {
			paths.addAll(getHeadCommit().getModified());
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
		for(Commit c : getCommits()) {
			paths.addAll(c.getRemoved());
		}
		if(getHeadCommit() != null) {
			paths.addAll(getHeadCommit().getRemoved());
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
			case GithubWebhookPackage.GITHUB_PAYLOAD__REPOSITORY:
				return basicSetRepository(null, msgs);
			case GithubWebhookPackage.GITHUB_PAYLOAD__COMMITS:
				return ((InternalEList<?>)getCommits()).basicRemove(otherEnd, msgs);
			case GithubWebhookPackage.GITHUB_PAYLOAD__HEAD_COMMIT:
				return basicSetHeadCommit(null, msgs);
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
			case GithubWebhookPackage.GITHUB_PAYLOAD__REPOSITORY:
				return getRepository();
			case GithubWebhookPackage.GITHUB_PAYLOAD__COMMITS:
				return getCommits();
			case GithubWebhookPackage.GITHUB_PAYLOAD__HEAD_COMMIT:
				return getHeadCommit();
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
			case GithubWebhookPackage.GITHUB_PAYLOAD__REPOSITORY:
				setRepository((Repository)newValue);
				return;
			case GithubWebhookPackage.GITHUB_PAYLOAD__COMMITS:
				getCommits().clear();
				getCommits().addAll((Collection<? extends Commit>)newValue);
				return;
			case GithubWebhookPackage.GITHUB_PAYLOAD__HEAD_COMMIT:
				setHeadCommit((Commit)newValue);
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
			case GithubWebhookPackage.GITHUB_PAYLOAD__REPOSITORY:
				setRepository((Repository)null);
				return;
			case GithubWebhookPackage.GITHUB_PAYLOAD__COMMITS:
				getCommits().clear();
				return;
			case GithubWebhookPackage.GITHUB_PAYLOAD__HEAD_COMMIT:
				setHeadCommit((Commit)null);
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
			case GithubWebhookPackage.GITHUB_PAYLOAD__REPOSITORY:
				return repository != null;
			case GithubWebhookPackage.GITHUB_PAYLOAD__COMMITS:
				return commits != null && !commits.isEmpty();
			case GithubWebhookPackage.GITHUB_PAYLOAD__HEAD_COMMIT:
				return headCommit != null;
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
			case GithubWebhookPackage.GITHUB_PAYLOAD___GET_PROVIDER:
				return getProvider();
			case GithubWebhookPackage.GITHUB_PAYLOAD___GET_CLONE_URL:
				return getCloneUrl();
			case GithubWebhookPackage.GITHUB_PAYLOAD___GET_REPOSITORY_FULL_NAME:
				return getRepositoryFullName();
			case GithubWebhookPackage.GITHUB_PAYLOAD___GET_ADDED_PATHS:
				return getAddedPaths();
			case GithubWebhookPackage.GITHUB_PAYLOAD___GET_MODIFIED_PATHS:
				return getModifiedPaths();
			case GithubWebhookPackage.GITHUB_PAYLOAD___GET_REMOVED_PATHS:
				return getRemovedPaths();
		}
		return super.eInvoke(operationID, arguments);
	}

} //GithubPayloadImpl
