/*
 */
package org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.resource.Resource;

import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;

import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;

import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Resource Factory</b> associated with the package.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.util.GithubWebhookResourceImpl
 * @generated
 */
public class GithubWebhookResourceFactoryImpl extends ResourceFactoryImpl {
	/**
	 * Creates an instance of the resource factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GithubWebhookResourceFactoryImpl() {
		super();
	}

	/**
	 * Creates an instance of the resource.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Resource createResource(URI uri) {
		Resource result = new GithubWebhookResourceImpl(uri);
		return result;
	}

	/**
	 * A method providing the Properties the services around this ResourceFactory should be registered with.
	 * @generated
	 */
	public Map<String, Object> getServiceProperties() {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(EMFNamespaces.EMF_CONFIGURATOR_NAME, GithubWebhookPackage.eNAME);
		properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, "githubwebhook");
		properties.put(EMFNamespaces.EMF_MODEL_VERSION, "1.0");
		return properties;
	}

} //GithubWebhookResourceFactoryImpl
