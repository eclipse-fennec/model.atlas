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
package org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookFactory
 * @model kind="package"
 *        annotation="Version value='1.0'"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel complianceLevel='17.0' oSGiCompatible='true' basePackage='org.eclipse.fennec.model.atlas.management.git.webhook.model' resource='XMI' copyrightText='Copyright (c) 2012 - 2026 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n    Data In Motion - initial API and implementation'"
 * @generated
 */
@ProviderType
@EPackage(uri = GitWebhookPackage.eNS_URI, fingerprint = "fp1:be92b1ed5284e862d865afe8db61e4048bf2f1ec3a023d4dcc7023a1d767de5f", genModel = "/model/git-webhook.genmodel", genModelSourceLocations = {"model/git-webhook.genmodel","org.eclipse.fennec.model.atlas.management.git.webhook.model/model/git-webhook.genmodel"}, ecore = "/model/git-webhook.ecore", ecoreSourceLocations = "/model/git-webhook.ecore")
public interface GitWebhookPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "gitwebhook";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/management/git/webhook/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "gitwebhook";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	GitWebhookPackage eINSTANCE = org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.GitWebhookPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.WebhookPayloadImpl <em>Webhook Payload</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.WebhookPayloadImpl
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.GitWebhookPackageImpl#getWebhookPayload()
	 * @generated
	 */
	int WEBHOOK_PAYLOAD = 0;

	/**
	 * The feature id for the '<em><b>Ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD__REF = 0;

	/**
	 * The feature id for the '<em><b>Before</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD__BEFORE = 1;

	/**
	 * The feature id for the '<em><b>After</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD__AFTER = 2;

	/**
	 * The number of structural features of the '<em>Webhook Payload</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD_FEATURE_COUNT = 3;

	/**
	 * The operation id for the '<em>Get Provider</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD___GET_PROVIDER = 0;

	/**
	 * The operation id for the '<em>Get Clone Url</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD___GET_CLONE_URL = 1;

	/**
	 * The operation id for the '<em>Get Repository Full Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD___GET_REPOSITORY_FULL_NAME = 2;

	/**
	 * The operation id for the '<em>Get Added Paths</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD___GET_ADDED_PATHS = 3;

	/**
	 * The operation id for the '<em>Get Modified Paths</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD___GET_MODIFIED_PATHS = 4;

	/**
	 * The operation id for the '<em>Get Removed Paths</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD___GET_REMOVED_PATHS = 5;

	/**
	 * The operation id for the '<em>Is Created</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD___IS_CREATED = 6;

	/**
	 * The operation id for the '<em>Is Deleted</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD___IS_DELETED = 7;

	/**
	 * The operation id for the '<em>Is Forced</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD___IS_FORCED = 8;

	/**
	 * The number of operations of the '<em>Webhook Payload</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WEBHOOK_PAYLOAD_OPERATION_COUNT = 9;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.CommitImpl <em>Commit</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.CommitImpl
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.GitWebhookPackageImpl#getCommit()
	 * @generated
	 */
	int COMMIT = 1;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMIT__ID = 0;

	/**
	 * The feature id for the '<em><b>Added</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMIT__ADDED = 1;

	/**
	 * The feature id for the '<em><b>Modified</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMIT__MODIFIED = 2;

	/**
	 * The feature id for the '<em><b>Removed</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMIT__REMOVED = 3;

	/**
	 * The number of structural features of the '<em>Commit</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMIT_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Commit</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMMIT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitProvider <em>Git Provider</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitProvider
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.GitWebhookPackageImpl#getGitProvider()
	 * @generated
	 */
	int GIT_PROVIDER = 2;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload <em>Webhook Payload</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Webhook Payload</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload
	 * @generated
	 */
	EClass getWebhookPayload();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getRef <em>Ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ref</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getRef()
	 * @see #getWebhookPayload()
	 * @generated
	 */
	EAttribute getWebhookPayload_Ref();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getBefore <em>Before</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Before</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getBefore()
	 * @see #getWebhookPayload()
	 * @generated
	 */
	EAttribute getWebhookPayload_Before();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getAfter <em>After</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>After</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getAfter()
	 * @see #getWebhookPayload()
	 * @generated
	 */
	EAttribute getWebhookPayload_After();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getProvider() <em>Get Provider</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Provider</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getProvider()
	 * @generated
	 */
	EOperation getWebhookPayload__GetProvider();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getCloneUrl() <em>Get Clone Url</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Clone Url</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getCloneUrl()
	 * @generated
	 */
	EOperation getWebhookPayload__GetCloneUrl();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getRepositoryFullName() <em>Get Repository Full Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Repository Full Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getRepositoryFullName()
	 * @generated
	 */
	EOperation getWebhookPayload__GetRepositoryFullName();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getAddedPaths() <em>Get Added Paths</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Added Paths</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getAddedPaths()
	 * @generated
	 */
	EOperation getWebhookPayload__GetAddedPaths();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getModifiedPaths() <em>Get Modified Paths</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Modified Paths</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getModifiedPaths()
	 * @generated
	 */
	EOperation getWebhookPayload__GetModifiedPaths();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getRemovedPaths() <em>Get Removed Paths</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Removed Paths</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#getRemovedPaths()
	 * @generated
	 */
	EOperation getWebhookPayload__GetRemovedPaths();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#isCreated() <em>Is Created</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Created</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#isCreated()
	 * @generated
	 */
	EOperation getWebhookPayload__IsCreated();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#isDeleted() <em>Is Deleted</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Deleted</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#isDeleted()
	 * @generated
	 */
	EOperation getWebhookPayload__IsDeleted();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#isForced() <em>Is Forced</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Is Forced</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload#isForced()
	 * @generated
	 */
	EOperation getWebhookPayload__IsForced();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit <em>Commit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Commit</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit
	 * @generated
	 */
	EClass getCommit();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getId()
	 * @see #getCommit()
	 * @generated
	 */
	EAttribute getCommit_Id();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getAdded <em>Added</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Added</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getAdded()
	 * @see #getCommit()
	 * @generated
	 */
	EAttribute getCommit_Added();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getModified <em>Modified</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Modified</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getModified()
	 * @see #getCommit()
	 * @generated
	 */
	EAttribute getCommit_Modified();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getRemoved <em>Removed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Removed</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit#getRemoved()
	 * @see #getCommit()
	 * @generated
	 */
	EAttribute getCommit_Removed();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitProvider <em>Git Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Git Provider</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitProvider
	 * @generated
	 */
	EEnum getGitProvider();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	GitWebhookFactory getGitWebhookFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.WebhookPayloadImpl <em>Webhook Payload</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.WebhookPayloadImpl
		 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.GitWebhookPackageImpl#getWebhookPayload()
		 * @generated
		 */
		EClass WEBHOOK_PAYLOAD = eINSTANCE.getWebhookPayload();

		/**
		 * The meta object literal for the '<em><b>Ref</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WEBHOOK_PAYLOAD__REF = eINSTANCE.getWebhookPayload_Ref();

		/**
		 * The meta object literal for the '<em><b>Before</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WEBHOOK_PAYLOAD__BEFORE = eINSTANCE.getWebhookPayload_Before();

		/**
		 * The meta object literal for the '<em><b>After</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WEBHOOK_PAYLOAD__AFTER = eINSTANCE.getWebhookPayload_After();

		/**
		 * The meta object literal for the '<em><b>Get Provider</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WEBHOOK_PAYLOAD___GET_PROVIDER = eINSTANCE.getWebhookPayload__GetProvider();

		/**
		 * The meta object literal for the '<em><b>Get Clone Url</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WEBHOOK_PAYLOAD___GET_CLONE_URL = eINSTANCE.getWebhookPayload__GetCloneUrl();

		/**
		 * The meta object literal for the '<em><b>Get Repository Full Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WEBHOOK_PAYLOAD___GET_REPOSITORY_FULL_NAME = eINSTANCE.getWebhookPayload__GetRepositoryFullName();

		/**
		 * The meta object literal for the '<em><b>Get Added Paths</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WEBHOOK_PAYLOAD___GET_ADDED_PATHS = eINSTANCE.getWebhookPayload__GetAddedPaths();

		/**
		 * The meta object literal for the '<em><b>Get Modified Paths</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WEBHOOK_PAYLOAD___GET_MODIFIED_PATHS = eINSTANCE.getWebhookPayload__GetModifiedPaths();

		/**
		 * The meta object literal for the '<em><b>Get Removed Paths</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WEBHOOK_PAYLOAD___GET_REMOVED_PATHS = eINSTANCE.getWebhookPayload__GetRemovedPaths();

		/**
		 * The meta object literal for the '<em><b>Is Created</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WEBHOOK_PAYLOAD___IS_CREATED = eINSTANCE.getWebhookPayload__IsCreated();

		/**
		 * The meta object literal for the '<em><b>Is Deleted</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WEBHOOK_PAYLOAD___IS_DELETED = eINSTANCE.getWebhookPayload__IsDeleted();

		/**
		 * The meta object literal for the '<em><b>Is Forced</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation WEBHOOK_PAYLOAD___IS_FORCED = eINSTANCE.getWebhookPayload__IsForced();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.CommitImpl <em>Commit</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.CommitImpl
		 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.GitWebhookPackageImpl#getCommit()
		 * @generated
		 */
		EClass COMMIT = eINSTANCE.getCommit();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMIT__ID = eINSTANCE.getCommit_Id();

		/**
		 * The meta object literal for the '<em><b>Added</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMIT__ADDED = eINSTANCE.getCommit_Added();

		/**
		 * The meta object literal for the '<em><b>Modified</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMIT__MODIFIED = eINSTANCE.getCommit_Modified();

		/**
		 * The meta object literal for the '<em><b>Removed</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMMIT__REMOVED = eINSTANCE.getCommit_Removed();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitProvider <em>Git Provider</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitProvider
		 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl.GitWebhookPackageImpl#getGitProvider()
		 * @generated
		 */
		EEnum GIT_PROVIDER = eINSTANCE.getGitProvider();

	}

} //GitWebhookPackage
