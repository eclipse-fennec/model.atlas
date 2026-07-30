/*
 */
package org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook;

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage
 * @generated
 */
@ProviderType
public interface GitlabWebhookFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	GitlabWebhookFactory eINSTANCE = org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.impl.GitlabWebhookFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Gitlab Payload</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Gitlab Payload</em>'.
	 * @generated
	 */
	GitlabPayload createGitlabPayload();

	/**
	 * Returns a new object of class '<em>Project</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Project</em>'.
	 * @generated
	 */
	Project createProject();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	GitlabWebhookPackage getGitlabWebhookPackage();

} //GitlabWebhookFactory
