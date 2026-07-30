/**
 */
package org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class GithubWebhookFactoryImpl extends EFactoryImpl implements GithubWebhookFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static GithubWebhookFactory init() {
		try {
			GithubWebhookFactory theGithubWebhookFactory = (GithubWebhookFactory)EPackage.Registry.INSTANCE.getEFactory(GithubWebhookPackage.eNS_URI);
			if (theGithubWebhookFactory != null) {
				return theGithubWebhookFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new GithubWebhookFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GithubWebhookFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case GithubWebhookPackage.GITHUB_PAYLOAD: return createGithubPayload();
			case GithubWebhookPackage.REPOSITORY: return createRepository();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GithubPayload createGithubPayload() {
		GithubPayloadImpl githubPayload = new GithubPayloadImpl();
		return githubPayload;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Repository createRepository() {
		RepositoryImpl repository = new RepositoryImpl();
		return repository;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GithubWebhookPackage getGithubWebhookPackage() {
		return (GithubWebhookPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static GithubWebhookPackage getPackage() {
		return GithubWebhookPackage.eINSTANCE;
	}

} //GithubWebhookFactoryImpl
