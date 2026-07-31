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
package org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload;
import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookFactory;
import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage;
import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class GitlabWebhookPackageImpl extends EPackageImpl implements GitlabWebhookPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass gitlabPayloadEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass projectEClass = null;

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
	 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private GitlabWebhookPackageImpl() {
		super(eNS_URI, GitlabWebhookFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link GitlabWebhookPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static GitlabWebhookPackage init() {
		if (isInited) return (GitlabWebhookPackage)EPackage.Registry.INSTANCE.getEPackage(GitlabWebhookPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredGitlabWebhookPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		GitlabWebhookPackageImpl theGitlabWebhookPackage = registeredGitlabWebhookPackage instanceof GitlabWebhookPackageImpl ? (GitlabWebhookPackageImpl)registeredGitlabWebhookPackage : new GitlabWebhookPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		GitWebhookPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theGitlabWebhookPackage.createPackageContents();

		// Initialize created meta-data
		theGitlabWebhookPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theGitlabWebhookPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(GitlabWebhookPackage.eNS_URI, theGitlabWebhookPackage);
		return theGitlabWebhookPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGitlabPayload() {
		return gitlabPayloadEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGitlabPayload_Project() {
		return (EReference)gitlabPayloadEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGitlabPayload_Commits() {
		return (EReference)gitlabPayloadEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGitlabPayload__GetProvider() {
		return gitlabPayloadEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGitlabPayload__GetCloneUrl() {
		return gitlabPayloadEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGitlabPayload__GetRepositoryFullName() {
		return gitlabPayloadEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGitlabPayload__GetAddedPaths() {
		return gitlabPayloadEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGitlabPayload__GetModifiedPaths() {
		return gitlabPayloadEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getGitlabPayload__GetRemovedPaths() {
		return gitlabPayloadEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getProject() {
		return projectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getProject_GitHttpUrl() {
		return (EAttribute)projectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getProject_PathWithNamespace() {
		return (EAttribute)projectEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GitlabWebhookFactory getGitlabWebhookFactory() {
		return (GitlabWebhookFactory)getEFactoryInstance();
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
		gitlabPayloadEClass = createEClass(GITLAB_PAYLOAD);
		createEReference(gitlabPayloadEClass, GITLAB_PAYLOAD__PROJECT);
		createEReference(gitlabPayloadEClass, GITLAB_PAYLOAD__COMMITS);
		createEOperation(gitlabPayloadEClass, GITLAB_PAYLOAD___GET_PROVIDER);
		createEOperation(gitlabPayloadEClass, GITLAB_PAYLOAD___GET_CLONE_URL);
		createEOperation(gitlabPayloadEClass, GITLAB_PAYLOAD___GET_REPOSITORY_FULL_NAME);
		createEOperation(gitlabPayloadEClass, GITLAB_PAYLOAD___GET_ADDED_PATHS);
		createEOperation(gitlabPayloadEClass, GITLAB_PAYLOAD___GET_MODIFIED_PATHS);
		createEOperation(gitlabPayloadEClass, GITLAB_PAYLOAD___GET_REMOVED_PATHS);

		projectEClass = createEClass(PROJECT);
		createEAttribute(projectEClass, PROJECT__GIT_HTTP_URL);
		createEAttribute(projectEClass, PROJECT__PATH_WITH_NAMESPACE);
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
		gitlabPayloadEClass.getESuperTypes().add(theGitWebhookPackage.getWebhookPayload());

		// Initialize classes, features, and operations; add parameters
		initEClass(gitlabPayloadEClass, GitlabPayload.class, "GitlabPayload", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGitlabPayload_Project(), this.getProject(), null, "project", null, 0, 1, GitlabPayload.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGitlabPayload_Commits(), theGitWebhookPackage.getCommit(), null, "commits", null, 0, -1, GitlabPayload.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEOperation(getGitlabPayload__GetProvider(), theGitWebhookPackage.getGitProvider(), "getProvider", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getGitlabPayload__GetCloneUrl(), ecorePackage.getEString(), "getCloneUrl", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getGitlabPayload__GetRepositoryFullName(), ecorePackage.getEString(), "getRepositoryFullName", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getGitlabPayload__GetAddedPaths(), ecorePackage.getEString(), "getAddedPaths", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getGitlabPayload__GetModifiedPaths(), ecorePackage.getEString(), "getModifiedPaths", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getGitlabPayload__GetRemovedPaths(), ecorePackage.getEString(), "getRemovedPaths", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEClass(projectEClass, Project.class, "Project", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getProject_GitHttpUrl(), ecorePackage.getEString(), "gitHttpUrl", null, 0, 1, Project.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getProject_PathWithNamespace(), ecorePackage.getEString(), "pathWithNamespace", null, 0, 1, Project.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

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
			   "basePackage", "org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model",
			   "resource", "XMI",
			   "copyrightText", "Copyright (c) 2012 - 2026 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n    Data In Motion - initial API and implementation"
		   });
		addAnnotation
		  (gitlabPayloadEClass,
		   source,
		   new String[] {
			   "documentation", "Concrete GitLab push webhook payload. Only the fields the git backend reads are modelled; the codec skips all other JSON properties (strictOnUnknown=false). Implements the neutral WebhookPayload operations from GitLab-specific fields. GitLab sends no head_commit and no created/deleted/forced flags; created/deleted are derived from the before/after SHAs."
		   });
		addAnnotation
		  (getGitlabPayload__GetProvider(),
		   source,
		   new String[] {
			   "body", "return GitProvider.GITLAB;"
		   });
		addAnnotation
		  (getGitlabPayload__GetCloneUrl(),
		   source,
		   new String[] {
			   "body", "return getProject() == null ? null : getProject().getGitHttpUrl();"
		   });
		addAnnotation
		  (getGitlabPayload__GetRepositoryFullName(),
		   source,
		   new String[] {
			   "body", "return getProject() == null ? null : getProject().getPathWithNamespace(); "
		   });
		addAnnotation
		  (getGitlabPayload__GetAddedPaths(),
		   source,
		   new String[] {
			   "body", "java.util.LinkedHashSet<String> paths = new java.util.LinkedHashSet<>();\nfor (Commit c : getCommits()) {\n\tpaths.addAll(c.getAdded());\n }\nreturn new org.eclipse.emf.common.util.BasicEList<>(paths);"
		   });
		addAnnotation
		  (getGitlabPayload__GetModifiedPaths(),
		   source,
		   new String[] {
			   "body", "java.util.LinkedHashSet<String> paths = new java.util.LinkedHashSet<>();\nfor (Commit c : getCommits()) {\n\tpaths.addAll(c.getModified());\n }\nreturn new org.eclipse.emf.common.util.BasicEList<>(paths);"
		   });
		addAnnotation
		  (getGitlabPayload__GetRemovedPaths(),
		   source,
		   new String[] {
			   "body", "java.util.LinkedHashSet<String> paths = new java.util.LinkedHashSet<>();\nfor (Commit c : getCommits()) {\n\tpaths.addAll(c.getRemoved());\n }\nreturn new org.eclipse.emf.common.util.BasicEList<>(paths);"
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
		  (getProject_GitHttpUrl(),
		   source,
		   new String[] {
			   "name", "git_http_url"
		   });
		addAnnotation
		  (getProject_PathWithNamespace(),
		   source,
		   new String[] {
			   "name", "path_with_namespace"
		   });
	}

} //GitlabWebhookPackageImpl
