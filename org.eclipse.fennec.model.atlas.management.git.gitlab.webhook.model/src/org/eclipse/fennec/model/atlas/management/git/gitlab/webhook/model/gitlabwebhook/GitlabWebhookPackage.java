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


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage;

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
 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookFactory
 * @model kind="package"
 *        annotation="Version value='1.0'"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel complianceLevel='17.0' oSGiCompatible='true' basePackage='org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model' resource='XMI' copyrightText='Copyright (c) 2012 - 2026 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n    Data In Motion - initial API and implementation'"
 * @generated
 */
@ProviderType
@EPackage(uri = GitlabWebhookPackage.eNS_URI, genModel = "/model/gitlab-webhook.genmodel", genModelSourceLocations = {"model/gitlab-webhook.genmodel","org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model/model/gitlab-webhook.genmodel"}, ecore = "/model/gitlab-webhook.ecore", ecoreSourceLocations = "/model/gitlab-webhook.ecore")
public interface GitlabWebhookPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "gitlabwebhook";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/management/gitlab/webhook/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "gitlabwebhook";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	GitlabWebhookPackage eINSTANCE = org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabWebhookPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabPayloadImpl <em>Gitlab Payload</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabPayloadImpl
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabWebhookPackageImpl#getGitlabPayload()
	 * @generated
	 */
	int GITLAB_PAYLOAD = 0;

	/**
	 * The feature id for the '<em><b>Ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD__REF = GitWebhookPackage.WEBHOOK_PAYLOAD__REF;

	/**
	 * The feature id for the '<em><b>Before</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD__BEFORE = GitWebhookPackage.WEBHOOK_PAYLOAD__BEFORE;

	/**
	 * The feature id for the '<em><b>After</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD__AFTER = GitWebhookPackage.WEBHOOK_PAYLOAD__AFTER;

	/**
	 * The feature id for the '<em><b>Project</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD__PROJECT = GitWebhookPackage.WEBHOOK_PAYLOAD_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Commits</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD__COMMITS = GitWebhookPackage.WEBHOOK_PAYLOAD_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Gitlab Payload</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD_FEATURE_COUNT = GitWebhookPackage.WEBHOOK_PAYLOAD_FEATURE_COUNT + 2;

	/**
	 * The operation id for the '<em>Is Created</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD___IS_CREATED = GitWebhookPackage.WEBHOOK_PAYLOAD___IS_CREATED;

	/**
	 * The operation id for the '<em>Is Deleted</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD___IS_DELETED = GitWebhookPackage.WEBHOOK_PAYLOAD___IS_DELETED;

	/**
	 * The operation id for the '<em>Is Forced</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD___IS_FORCED = GitWebhookPackage.WEBHOOK_PAYLOAD___IS_FORCED;

	/**
	 * The operation id for the '<em>Get Provider</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD___GET_PROVIDER = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Clone Url</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD___GET_CLONE_URL = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Repository Full Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD___GET_REPOSITORY_FULL_NAME = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 2;

	/**
	 * The operation id for the '<em>Get Added Paths</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD___GET_ADDED_PATHS = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Modified Paths</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD___GET_MODIFIED_PATHS = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 4;

	/**
	 * The operation id for the '<em>Get Removed Paths</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD___GET_REMOVED_PATHS = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 5;

	/**
	 * The number of operations of the '<em>Gitlab Payload</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITLAB_PAYLOAD_OPERATION_COUNT = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 6;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.ProjectImpl <em>Project</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.ProjectImpl
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabWebhookPackageImpl#getProject()
	 * @generated
	 */
	int PROJECT = 1;

	/**
	 * The feature id for the '<em><b>Git Http Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROJECT__GIT_HTTP_URL = 0;

	/**
	 * The feature id for the '<em><b>Path With Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROJECT__PATH_WITH_NAMESPACE = 1;

	/**
	 * The number of structural features of the '<em>Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROJECT_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Project</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROJECT_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload <em>Gitlab Payload</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Gitlab Payload</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload
	 * @generated
	 */
	EClass getGitlabPayload();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getProject <em>Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Project</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getProject()
	 * @see #getGitlabPayload()
	 * @generated
	 */
	EReference getGitlabPayload_Project();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getCommits <em>Commits</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Commits</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getCommits()
	 * @see #getGitlabPayload()
	 * @generated
	 */
	EReference getGitlabPayload_Commits();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getProvider() <em>Get Provider</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Provider</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getProvider()
	 * @generated
	 */
	EOperation getGitlabPayload__GetProvider();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getCloneUrl() <em>Get Clone Url</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Clone Url</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getCloneUrl()
	 * @generated
	 */
	EOperation getGitlabPayload__GetCloneUrl();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getRepositoryFullName() <em>Get Repository Full Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Repository Full Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getRepositoryFullName()
	 * @generated
	 */
	EOperation getGitlabPayload__GetRepositoryFullName();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getAddedPaths() <em>Get Added Paths</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Added Paths</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getAddedPaths()
	 * @generated
	 */
	EOperation getGitlabPayload__GetAddedPaths();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getModifiedPaths() <em>Get Modified Paths</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Modified Paths</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getModifiedPaths()
	 * @generated
	 */
	EOperation getGitlabPayload__GetModifiedPaths();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getRemovedPaths() <em>Get Removed Paths</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Removed Paths</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload#getRemovedPaths()
	 * @generated
	 */
	EOperation getGitlabPayload__GetRemovedPaths();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project <em>Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Project</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project
	 * @generated
	 */
	EClass getProject();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project#getGitHttpUrl <em>Git Http Url</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Git Http Url</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project#getGitHttpUrl()
	 * @see #getProject()
	 * @generated
	 */
	EAttribute getProject_GitHttpUrl();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project#getPathWithNamespace <em>Path With Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Path With Namespace</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project#getPathWithNamespace()
	 * @see #getProject()
	 * @generated
	 */
	EAttribute getProject_PathWithNamespace();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	GitlabWebhookFactory getGitlabWebhookFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabPayloadImpl <em>Gitlab Payload</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabPayloadImpl
		 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabWebhookPackageImpl#getGitlabPayload()
		 * @generated
		 */
		EClass GITLAB_PAYLOAD = eINSTANCE.getGitlabPayload();

		/**
		 * The meta object literal for the '<em><b>Project</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GITLAB_PAYLOAD__PROJECT = eINSTANCE.getGitlabPayload_Project();

		/**
		 * The meta object literal for the '<em><b>Commits</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GITLAB_PAYLOAD__COMMITS = eINSTANCE.getGitlabPayload_Commits();

		/**
		 * The meta object literal for the '<em><b>Get Provider</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITLAB_PAYLOAD___GET_PROVIDER = eINSTANCE.getGitlabPayload__GetProvider();

		/**
		 * The meta object literal for the '<em><b>Get Clone Url</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITLAB_PAYLOAD___GET_CLONE_URL = eINSTANCE.getGitlabPayload__GetCloneUrl();

		/**
		 * The meta object literal for the '<em><b>Get Repository Full Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITLAB_PAYLOAD___GET_REPOSITORY_FULL_NAME = eINSTANCE.getGitlabPayload__GetRepositoryFullName();

		/**
		 * The meta object literal for the '<em><b>Get Added Paths</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITLAB_PAYLOAD___GET_ADDED_PATHS = eINSTANCE.getGitlabPayload__GetAddedPaths();

		/**
		 * The meta object literal for the '<em><b>Get Modified Paths</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITLAB_PAYLOAD___GET_MODIFIED_PATHS = eINSTANCE.getGitlabPayload__GetModifiedPaths();

		/**
		 * The meta object literal for the '<em><b>Get Removed Paths</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITLAB_PAYLOAD___GET_REMOVED_PATHS = eINSTANCE.getGitlabPayload__GetRemovedPaths();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.ProjectImpl <em>Project</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.ProjectImpl
		 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabWebhookPackageImpl#getProject()
		 * @generated
		 */
		EClass PROJECT = eINSTANCE.getProject();

		/**
		 * The meta object literal for the '<em><b>Git Http Url</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROJECT__GIT_HTTP_URL = eINSTANCE.getProject_GitHttpUrl();

		/**
		 * The meta object literal for the '<em><b>Path With Namespace</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PROJECT__PATH_WITH_NAMESPACE = eINSTANCE.getProject_PathWithNamespace();

	}

} //GitlabWebhookPackage
