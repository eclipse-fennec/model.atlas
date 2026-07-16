/*
 */
package org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook;


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
 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookFactory
 * @model kind="package"
 *        annotation="Version value='1.0'"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel complianceLevel='17.0' oSGiCompatible='true' basePackage='org.eclipse.fennec.model.atlas.management.git.github.webhook.model' resource='XMI' copyrightText='null'"
 * @generated
 */
@ProviderType
@EPackage(uri = GithubWebhookPackage.eNS_URI, genModel = "/model/github-webhook.genmodel", genModelSourceLocations = {"model/github-webhook.genmodel","org.eclipse.fennec.model.atlas.management.git.github.webhook.model/model/github-webhook.genmodel"}, ecore = "/model/github-webhook.ecore", ecoreSourceLocations = "/model/github-webhook.ecore")
public interface GithubWebhookPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "githubwebhook";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/model/atlas/management/github/webhook/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "githubwebhook";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	GithubWebhookPackage eINSTANCE = org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubWebhookPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubPayloadImpl <em>Github Payload</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubPayloadImpl
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubWebhookPackageImpl#getGithubPayload()
	 * @generated
	 */
	int GITHUB_PAYLOAD = 0;

	/**
	 * The feature id for the '<em><b>Ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD__REF = GitWebhookPackage.WEBHOOK_PAYLOAD__REF;

	/**
	 * The feature id for the '<em><b>Before</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD__BEFORE = GitWebhookPackage.WEBHOOK_PAYLOAD__BEFORE;

	/**
	 * The feature id for the '<em><b>After</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD__AFTER = GitWebhookPackage.WEBHOOK_PAYLOAD__AFTER;

	/**
	 * The feature id for the '<em><b>Repository</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD__REPOSITORY = GitWebhookPackage.WEBHOOK_PAYLOAD_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Commits</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD__COMMITS = GitWebhookPackage.WEBHOOK_PAYLOAD_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Head Commit</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD__HEAD_COMMIT = GitWebhookPackage.WEBHOOK_PAYLOAD_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Github Payload</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD_FEATURE_COUNT = GitWebhookPackage.WEBHOOK_PAYLOAD_FEATURE_COUNT + 3;

	/**
	 * The operation id for the '<em>Is Created</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD___IS_CREATED = GitWebhookPackage.WEBHOOK_PAYLOAD___IS_CREATED;

	/**
	 * The operation id for the '<em>Is Deleted</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD___IS_DELETED = GitWebhookPackage.WEBHOOK_PAYLOAD___IS_DELETED;

	/**
	 * The operation id for the '<em>Is Forced</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD___IS_FORCED = GitWebhookPackage.WEBHOOK_PAYLOAD___IS_FORCED;

	/**
	 * The operation id for the '<em>Get Provider</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD___GET_PROVIDER = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 0;

	/**
	 * The operation id for the '<em>Get Clone Url</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD___GET_CLONE_URL = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 1;

	/**
	 * The operation id for the '<em>Get Repository Full Name</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD___GET_REPOSITORY_FULL_NAME = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 2;

	/**
	 * The operation id for the '<em>Get Added Paths</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD___GET_ADDED_PATHS = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 3;

	/**
	 * The operation id for the '<em>Get Modified Paths</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD___GET_MODIFIED_PATHS = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 4;

	/**
	 * The operation id for the '<em>Get Removed Paths</em>' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD___GET_REMOVED_PATHS = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 5;

	/**
	 * The number of operations of the '<em>Github Payload</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GITHUB_PAYLOAD_OPERATION_COUNT = GitWebhookPackage.WEBHOOK_PAYLOAD_OPERATION_COUNT + 6;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.RepositoryImpl <em>Repository</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.RepositoryImpl
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubWebhookPackageImpl#getRepository()
	 * @generated
	 */
	int REPOSITORY = 1;

	/**
	 * The feature id for the '<em><b>Clone Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPOSITORY__CLONE_URL = 0;

	/**
	 * The feature id for the '<em><b>Full Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPOSITORY__FULL_NAME = 1;

	/**
	 * The number of structural features of the '<em>Repository</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPOSITORY_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Repository</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPOSITORY_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload <em>Github Payload</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Github Payload</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload
	 * @generated
	 */
	EClass getGithubPayload();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getRepository <em>Repository</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Repository</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getRepository()
	 * @see #getGithubPayload()
	 * @generated
	 */
	EReference getGithubPayload_Repository();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getCommits <em>Commits</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Commits</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getCommits()
	 * @see #getGithubPayload()
	 * @generated
	 */
	EReference getGithubPayload_Commits();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getHeadCommit <em>Head Commit</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Head Commit</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getHeadCommit()
	 * @see #getGithubPayload()
	 * @generated
	 */
	EReference getGithubPayload_HeadCommit();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getProvider() <em>Get Provider</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Provider</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getProvider()
	 * @generated
	 */
	EOperation getGithubPayload__GetProvider();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getCloneUrl() <em>Get Clone Url</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Clone Url</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getCloneUrl()
	 * @generated
	 */
	EOperation getGithubPayload__GetCloneUrl();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getRepositoryFullName() <em>Get Repository Full Name</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Repository Full Name</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getRepositoryFullName()
	 * @generated
	 */
	EOperation getGithubPayload__GetRepositoryFullName();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getAddedPaths() <em>Get Added Paths</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Added Paths</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getAddedPaths()
	 * @generated
	 */
	EOperation getGithubPayload__GetAddedPaths();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getModifiedPaths() <em>Get Modified Paths</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Modified Paths</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getModifiedPaths()
	 * @generated
	 */
	EOperation getGithubPayload__GetModifiedPaths();

	/**
	 * Returns the meta object for the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getRemovedPaths() <em>Get Removed Paths</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the '<em>Get Removed Paths</em>' operation.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload#getRemovedPaths()
	 * @generated
	 */
	EOperation getGithubPayload__GetRemovedPaths();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository <em>Repository</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Repository</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository
	 * @generated
	 */
	EClass getRepository();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository#getCloneUrl <em>Clone Url</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Clone Url</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository#getCloneUrl()
	 * @see #getRepository()
	 * @generated
	 */
	EAttribute getRepository_CloneUrl();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository#getFullName <em>Full Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Full Name</em>'.
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository#getFullName()
	 * @see #getRepository()
	 * @generated
	 */
	EAttribute getRepository_FullName();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	GithubWebhookFactory getGithubWebhookFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubPayloadImpl <em>Github Payload</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubPayloadImpl
		 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubWebhookPackageImpl#getGithubPayload()
		 * @generated
		 */
		EClass GITHUB_PAYLOAD = eINSTANCE.getGithubPayload();

		/**
		 * The meta object literal for the '<em><b>Repository</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GITHUB_PAYLOAD__REPOSITORY = eINSTANCE.getGithubPayload_Repository();

		/**
		 * The meta object literal for the '<em><b>Commits</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GITHUB_PAYLOAD__COMMITS = eINSTANCE.getGithubPayload_Commits();

		/**
		 * The meta object literal for the '<em><b>Head Commit</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GITHUB_PAYLOAD__HEAD_COMMIT = eINSTANCE.getGithubPayload_HeadCommit();

		/**
		 * The meta object literal for the '<em><b>Get Provider</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITHUB_PAYLOAD___GET_PROVIDER = eINSTANCE.getGithubPayload__GetProvider();

		/**
		 * The meta object literal for the '<em><b>Get Clone Url</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITHUB_PAYLOAD___GET_CLONE_URL = eINSTANCE.getGithubPayload__GetCloneUrl();

		/**
		 * The meta object literal for the '<em><b>Get Repository Full Name</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITHUB_PAYLOAD___GET_REPOSITORY_FULL_NAME = eINSTANCE.getGithubPayload__GetRepositoryFullName();

		/**
		 * The meta object literal for the '<em><b>Get Added Paths</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITHUB_PAYLOAD___GET_ADDED_PATHS = eINSTANCE.getGithubPayload__GetAddedPaths();

		/**
		 * The meta object literal for the '<em><b>Get Modified Paths</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITHUB_PAYLOAD___GET_MODIFIED_PATHS = eINSTANCE.getGithubPayload__GetModifiedPaths();

		/**
		 * The meta object literal for the '<em><b>Get Removed Paths</b></em>' operation.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EOperation GITHUB_PAYLOAD___GET_REMOVED_PATHS = eINSTANCE.getGithubPayload__GetRemovedPaths();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.RepositoryImpl <em>Repository</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.RepositoryImpl
		 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.GithubWebhookPackageImpl#getRepository()
		 * @generated
		 */
		EClass REPOSITORY = eINSTANCE.getRepository();

		/**
		 * The meta object literal for the '<em><b>Clone Url</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REPOSITORY__CLONE_URL = eINSTANCE.getRepository_CloneUrl();

		/**
		 * The meta object literal for the '<em><b>Full Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REPOSITORY__FULL_NAME = eINSTANCE.getRepository_FullName();

	}

} //GithubWebhookPackage
