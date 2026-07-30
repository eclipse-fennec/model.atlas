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
package org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.Commit;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitProvider;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookFactory;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class GitWebhookPackageImpl extends EPackageImpl implements GitWebhookPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass webhookPayloadEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass commitEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum gitProviderEEnum = null;

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
	 * @see org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.GitWebhookPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private GitWebhookPackageImpl() {
		super(eNS_URI, GitWebhookFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link GitWebhookPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static GitWebhookPackage init() {
		if (isInited) return (GitWebhookPackage)EPackage.Registry.INSTANCE.getEPackage(GitWebhookPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredGitWebhookPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		GitWebhookPackageImpl theGitWebhookPackage = registeredGitWebhookPackage instanceof GitWebhookPackageImpl ? (GitWebhookPackageImpl)registeredGitWebhookPackage : new GitWebhookPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theGitWebhookPackage.createPackageContents();

		// Initialize created meta-data
		theGitWebhookPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theGitWebhookPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(GitWebhookPackage.eNS_URI, theGitWebhookPackage);
		return theGitWebhookPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getWebhookPayload() {
		return webhookPayloadEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getWebhookPayload_Ref() {
		return (EAttribute)webhookPayloadEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getWebhookPayload_Before() {
		return (EAttribute)webhookPayloadEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getWebhookPayload_After() {
		return (EAttribute)webhookPayloadEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWebhookPayload__GetProvider() {
		return webhookPayloadEClass.getEOperations().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWebhookPayload__GetCloneUrl() {
		return webhookPayloadEClass.getEOperations().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWebhookPayload__GetRepositoryFullName() {
		return webhookPayloadEClass.getEOperations().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWebhookPayload__GetAddedPaths() {
		return webhookPayloadEClass.getEOperations().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWebhookPayload__GetModifiedPaths() {
		return webhookPayloadEClass.getEOperations().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWebhookPayload__GetRemovedPaths() {
		return webhookPayloadEClass.getEOperations().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWebhookPayload__IsCreated() {
		return webhookPayloadEClass.getEOperations().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWebhookPayload__IsDeleted() {
		return webhookPayloadEClass.getEOperations().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EOperation getWebhookPayload__IsForced() {
		return webhookPayloadEClass.getEOperations().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCommit() {
		return commitEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCommit_Id() {
		return (EAttribute)commitEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCommit_Added() {
		return (EAttribute)commitEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCommit_Modified() {
		return (EAttribute)commitEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCommit_Removed() {
		return (EAttribute)commitEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getGitProvider() {
		return gitProviderEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GitWebhookFactory getGitWebhookFactory() {
		return (GitWebhookFactory)getEFactoryInstance();
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
		webhookPayloadEClass = createEClass(WEBHOOK_PAYLOAD);
		createEAttribute(webhookPayloadEClass, WEBHOOK_PAYLOAD__REF);
		createEAttribute(webhookPayloadEClass, WEBHOOK_PAYLOAD__BEFORE);
		createEAttribute(webhookPayloadEClass, WEBHOOK_PAYLOAD__AFTER);
		createEOperation(webhookPayloadEClass, WEBHOOK_PAYLOAD___GET_PROVIDER);
		createEOperation(webhookPayloadEClass, WEBHOOK_PAYLOAD___GET_CLONE_URL);
		createEOperation(webhookPayloadEClass, WEBHOOK_PAYLOAD___GET_REPOSITORY_FULL_NAME);
		createEOperation(webhookPayloadEClass, WEBHOOK_PAYLOAD___GET_ADDED_PATHS);
		createEOperation(webhookPayloadEClass, WEBHOOK_PAYLOAD___GET_MODIFIED_PATHS);
		createEOperation(webhookPayloadEClass, WEBHOOK_PAYLOAD___GET_REMOVED_PATHS);
		createEOperation(webhookPayloadEClass, WEBHOOK_PAYLOAD___IS_CREATED);
		createEOperation(webhookPayloadEClass, WEBHOOK_PAYLOAD___IS_DELETED);
		createEOperation(webhookPayloadEClass, WEBHOOK_PAYLOAD___IS_FORCED);

		commitEClass = createEClass(COMMIT);
		createEAttribute(commitEClass, COMMIT__ID);
		createEAttribute(commitEClass, COMMIT__ADDED);
		createEAttribute(commitEClass, COMMIT__MODIFIED);
		createEAttribute(commitEClass, COMMIT__REMOVED);

		// Create enums
		gitProviderEEnum = createEEnum(GIT_PROVIDER);
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

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(webhookPayloadEClass, WebhookPayload.class, "WebhookPayload", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getWebhookPayload_Ref(), ecorePackage.getEString(), "ref", null, 0, 1, WebhookPayload.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getWebhookPayload_Before(), ecorePackage.getEString(), "before", null, 0, 1, WebhookPayload.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getWebhookPayload_After(), ecorePackage.getEString(), "after", null, 0, 1, WebhookPayload.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEOperation(getWebhookPayload__GetProvider(), this.getGitProvider(), "getProvider", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getWebhookPayload__GetCloneUrl(), ecorePackage.getEString(), "getCloneUrl", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getWebhookPayload__GetRepositoryFullName(), ecorePackage.getEString(), "getRepositoryFullName", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getWebhookPayload__GetAddedPaths(), ecorePackage.getEString(), "getAddedPaths", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getWebhookPayload__GetModifiedPaths(), ecorePackage.getEString(), "getModifiedPaths", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getWebhookPayload__GetRemovedPaths(), ecorePackage.getEString(), "getRemovedPaths", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getWebhookPayload__IsCreated(), ecorePackage.getEBoolean(), "isCreated", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getWebhookPayload__IsDeleted(), ecorePackage.getEBoolean(), "isDeleted", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEOperation(getWebhookPayload__IsForced(), ecorePackage.getEBoolean(), "isForced", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(commitEClass, Commit.class, "Commit", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getCommit_Id(), ecorePackage.getEString(), "id", null, 0, 1, Commit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCommit_Added(), ecorePackage.getEString(), "added", null, 0, -1, Commit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCommit_Modified(), ecorePackage.getEString(), "modified", null, 0, -1, Commit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getCommit_Removed(), ecorePackage.getEString(), "removed", null, 0, -1, Commit.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(gitProviderEEnum, GitProvider.class, "GitProvider");
		addEEnumLiteral(gitProviderEEnum, GitProvider.GITHUB);
		addEEnumLiteral(gitProviderEEnum, GitProvider.GITLAB);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// Version
		createVersionAnnotations();
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
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
			   "basePackage", "org.eclipse.fennec.model.atlas.management.git.webhook.model",
			   "resource", "XMI",
			   "copyrightText", "Copyright (c) 2012 - 2025 Data In Motion and others.\nAll rights reserved.\n\nThis program and the accompanying materials are made\navailable under the terms of the Eclipse Public License 2.0\nwhich is available at https://www.eclipse.org/legal/epl-2.0/\n\nSPDX-License-Identifier: EPL-2.0\n\nContributors:\n    Data In Motion - initial API and implementation"
		   });
		addAnnotation
		  (gitProviderEEnum,
		   source,
		   new String[] {
			   "documentation", "The git host a webhook payload originates from."
		   });
		addAnnotation
		  (webhookPayloadEClass,
		   source,
		   new String[] {
			   "documentation", "Provider-neutral base for an inbound push webhook. Fields whose JSON key is identical across GitHub and GitLab (ref, before, after) are modelled as attributes and populated directly by the codec. Everything that differs per provider (clone URL, repository name, the added/modified/removed change set, and the created/deleted/forced flags) is exposed as an EOperation that each concrete provider payload overrides via a GenModel \'body\' annotation. Downstream (GitSyncService) consumes only this abstract type."
		   });
		addAnnotation
		  (getWebhookPayload__GetProvider(),
		   source,
		   new String[] {
			   "documentation", "Which git host this payload came from."
		   });
		addAnnotation
		  (getWebhookPayload__GetCloneUrl(),
		   source,
		   new String[] {
			   "documentation", "HTTP(S) fetch/clone URL of the repository (GitHub repository.clone_url; GitLab project.git_http_url)."
		   });
		addAnnotation
		  (getWebhookPayload__GetRepositoryFullName(),
		   source,
		   new String[] {
			   "documentation", "Namespaced repository identifier (GitHub repository.full_name; GitLab project.path_with_namespace)."
		   });
		addAnnotation
		  (getWebhookPayload__GetAddedPaths(),
		   source,
		   new String[] {
			   "documentation", "Repository-relative paths added by this push, aggregated across the payload\'s commits."
		   });
		addAnnotation
		  (getWebhookPayload__GetModifiedPaths(),
		   source,
		   new String[] {
			   "documentation", "Repository-relative paths modified by this push, aggregated across the payload\'s commits."
		   });
		addAnnotation
		  (getWebhookPayload__GetRemovedPaths(),
		   source,
		   new String[] {
			   "documentation", "Repository-relative paths removed by this push, aggregated across the payload\'s commits."
		   });
		addAnnotation
		  (getWebhookPayload__IsCreated(),
		   source,
		   new String[] {
			   "documentation", "True when this push created the ref (GitHub \'created\' flag; GitLab: before is the all-zero SHA).",
			   "body", "return getBefore() != null && getBefore().matches(\"0+\");"
		   });
		addAnnotation
		  (getWebhookPayload__IsDeleted(),
		   source,
		   new String[] {
			   "documentation", "True when this push deleted the ref (GitHub \'deleted\' flag; GitLab: after is the all-zero SHA).",
			   "body", "return getAfter() != null && getAfter().matches(\"0+\");"
		   });
		addAnnotation
		  (getWebhookPayload__IsForced(),
		   source,
		   new String[] {
			   "documentation", "True when this push was a force-push / non-fast-forward update (GitHub \'forced\' flag; GitLab: derived).",
			   "body", "return false;"
		   });
		addAnnotation
		  (getWebhookPayload_Ref(),
		   source,
		   new String[] {
			   "documentation", "The full git ref of the push, e.g. refs/heads/main. Same JSON key on GitHub and GitLab."
		   });
		addAnnotation
		  (getWebhookPayload_Before(),
		   source,
		   new String[] {
			   "documentation", "SHA of the ref before the push (all-zero SHA when the branch was created). Same JSON key on GitHub and GitLab."
		   });
		addAnnotation
		  (getWebhookPayload_After(),
		   source,
		   new String[] {
			   "documentation", "SHA of the ref after the push (all-zero SHA when the branch was deleted). Same JSON key on GitHub and GitLab."
		   });
	}

} //GitWebhookPackageImpl
