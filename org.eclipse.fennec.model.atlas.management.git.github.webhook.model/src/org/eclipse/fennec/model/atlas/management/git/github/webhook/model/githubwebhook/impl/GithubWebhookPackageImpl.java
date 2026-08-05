/**
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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload;
import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookFactory;
import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage;
import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class GithubWebhookPackageImpl extends EPackageImpl implements GithubWebhookPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass githubPayloadEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass repositoryEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private GithubWebhookPackageImpl() {
		super(eNS_URI, GithubWebhookFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link GithubWebhookPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static GithubWebhookPackage init() {
		if (isInited) return (GithubWebhookPackage)EPackage.Registry.INSTANCE.getEPackage(GithubWebhookPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredGithubWebhookPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		GithubWebhookPackageImpl theGithubWebhookPackage = registeredGithubWebhookPackage instanceof GithubWebhookPackageImpl ? (GithubWebhookPackageImpl)registeredGithubWebhookPackage : new GithubWebhookPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		GitWebhookPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theGithubWebhookPackage.createPackageContents();

		// Initialize created meta-data
		theGithubWebhookPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theGithubWebhookPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(GithubWebhookPackage.eNS_URI, theGithubWebhookPackage);
		return theGithubWebhookPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGithubPayload() {
		return githubPayloadEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGithubPayload_Repository() {
		return (EReference)githubPayloadEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGithubPayload_Commits() {
		return (EReference)githubPayloadEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGithubPayload_HeadCommit() {
		return (EReference)githubPayloadEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGithubPayload__GetProvider() {
		return githubPayloadEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGithubPayload__GetCloneUrl() {
		return githubPayloadEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGithubPayload__GetRepositoryFullName() {
		return githubPayloadEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGithubPayload__GetAddedPaths() {
		return githubPayloadEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGithubPayload__GetModifiedPaths() {
		return githubPayloadEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGithubPayload__GetRemovedPaths() {
		return githubPayloadEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRepository() {
		return repositoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRepository_CloneUrl() {
		return (EAttribute)repositoryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRepository_FullName() {
		return (EAttribute)repositoryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GithubWebhookFactory getGithubWebhookFactory() {
		return (GithubWebhookFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		githubPayloadEClass = createEClass(GITHUB_PAYLOAD);
		createEReference(githubPayloadEClass, GITHUB_PAYLOAD__REPOSITORY);
		createEReference(githubPayloadEClass, GITHUB_PAYLOAD__COMMITS);
		createEReference(githubPayloadEClass, GITHUB_PAYLOAD__HEAD_COMMIT);
		createEOperation(githubPayloadEClass, GITHUB_PAYLOAD___GET_PROVIDER);
		createEOperation(githubPayloadEClass, GITHUB_PAYLOAD___GET_CLONE_URL);
		createEOperation(githubPayloadEClass, GITHUB_PAYLOAD___GET_REPOSITORY_FULL_NAME);
		createEOperation(githubPayloadEClass, GITHUB_PAYLOAD___GET_ADDED_PATHS);
		createEOperation(githubPayloadEClass, GITHUB_PAYLOAD___GET_MODIFIED_PATHS);
		createEOperation(githubPayloadEClass, GITHUB_PAYLOAD___GET_REMOVED_PATHS);

		repositoryEClass = createEClass(REPOSITORY);
		createEAttribute(repositoryEClass, REPOSITORY__CLONE_URL);
		createEAttribute(repositoryEClass, REPOSITORY__FULL_NAME);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		GitWebhookPackage theGitWebhookPackage = (GitWebhookPackage)EPackage.Registry.INSTANCE.getEPackage(GitWebhookPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		githubPayloadEClass.getESuperTypes().add(theGitWebhookPackage.getWebhookPayload());

		// Initialize classes, features, and operations; add parameters
		initEClass(githubPayloadEClass, GithubPayload.class, "GithubPayload", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGithubPayload_Repository(), this.getRepository(), null, "repository", null, 0, 1, GithubPayload.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGithubPayload_Commits(), theGitWebhookPackage.getCommit(), null, "commits", null, 0, -1, GithubPayload.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGithubPayload_HeadCommit(), theGitWebhookPackage.getCommit(), null, "headCommit", null, 0, 1, GithubPayload.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEOperation(getGithubPayload__GetProvider(), theGitWebhookPackage.getGitProvider(), "getProvider", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getGithubPayload__GetCloneUrl(), ecorePackage.getEString(), "getCloneUrl", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getGithubPayload__GetRepositoryFullName(), ecorePackage.getEString(), "getRepositoryFullName", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getGithubPayload__GetAddedPaths(), ecorePackage.getEString(), "getAddedPaths", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getGithubPayload__GetModifiedPaths(), ecorePackage.getEString(), "getModifiedPaths", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getGithubPayload__GetRemovedPaths(), ecorePackage.getEString(), "getRemovedPaths", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEClass(repositoryEClass, Repository.class, "Repository", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRepository_CloneUrl(), ecorePackage.getEString(), "cloneUrl", null, 0, 1, Repository.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRepository_FullName(), ecorePackage.getEString(), "fullName", null, 0, 1, Repository.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// Version
		createVersionAnnotations();
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
		// http:///org/eclipse/emf/ecore/util/ExtendedMetaData
		createExtendedMetaDataAnnotations();
	}

	/**
	 * Initializes the annotations for <b>Version</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createVersionAnnotations() {
		String source = "Version";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "value", "1.0"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/GenModel</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createGenModelAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/GenModel";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "complianceLevel", "17.0",
			   "oSGiCompatible", "true",
			   "basePackage", "org.eclipse.fennec.model.atlas.management.git.github.webhook.model",
			   "resource", "XMI",
			   "copyrightText", "Copyright (c) 2012 - 2026 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n    Data In Motion - initial API and implementation"
		   });
		addAnnotation
		  (githubPayloadEClass,
		   source,
		   new String[] {
			   "documentation", "Concrete GitHub push webhook payload. Only the fields the git backend reads are modelled; the codec skips all other JSON properties (strictOnUnknown=false). Implements the neutral WebhookPayload operations from GitHub-specific fields."
		   });
		addAnnotation
		  (getGithubPayload__GetProvider(),
		   source,
		   new String[] {
			   "body", "return GitProvider.GITHUB;"
		   });
		addAnnotation
		  (getGithubPayload__GetCloneUrl(),
		   source,
		   new String[] {
			   "body", "return getRepository() == null ? null : getRepository().getCloneUrl();"
		   });
		addAnnotation
		  (getGithubPayload__GetRepositoryFullName(),
		   source,
		   new String[] {
			   "body", "return getRepository() == null ? null : getRepository().getFullName();"
		   });
		addAnnotation
		  (getGithubPayload__GetAddedPaths(),
		   source,
		   new String[] {
			   "body", "java.util.LinkedHashSet<String> paths = new java.util.LinkedHashSet<>();\nfor(Commit c : getCommits()) {\n\tpaths.addAll(c.getAdded());\n}\nif(getHeadCommit() != null) {\n\tpaths.addAll(getHeadCommit().getAdded());\n}\nreturn new org.eclipse.emf.common.util.BasicEList<>(paths);"
		   });
		addAnnotation
		  (getGithubPayload__GetModifiedPaths(),
		   source,
		   new String[] {
			   "body", "java.util.LinkedHashSet<String> paths = new java.util.LinkedHashSet<>();\nfor(Commit c : getCommits()) {\n\tpaths.addAll(c.getModified());\n}\nif(getHeadCommit() != null) {\n\tpaths.addAll(getHeadCommit().getModified());\n}\nreturn new org.eclipse.emf.common.util.BasicEList<>(paths);"
		   });
		addAnnotation
		  (getGithubPayload__GetRemovedPaths(),
		   source,
		   new String[] {
			   "body", "java.util.LinkedHashSet<String> paths = new java.util.LinkedHashSet<>();\nfor(Commit c : getCommits()) {\n\tpaths.addAll(c.getRemoved());\n}\nif(getHeadCommit() != null) {\n\tpaths.addAll(getHeadCommit().getRemoved());\n}\nreturn new org.eclipse.emf.common.util.BasicEList<>(paths);"
		   });
	}

	/**
	 * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createExtendedMetaDataAnnotations() {
		String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";
		addAnnotation
		  (getGithubPayload_HeadCommit(),
		   source,
		   new String[] {
			   "name", "head_commit"
		   });
		addAnnotation
		  (getRepository_CloneUrl(),
		   source,
		   new String[] {
			   "name", "clone_url"
		   });
		addAnnotation
		  (getRepository_FullName(),
		   source,
		   new String[] {
			   "name", "full_name"
		   });
	}

} //GithubWebhookPackageImpl
