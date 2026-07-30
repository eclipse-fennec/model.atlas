/*
 */
package org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.resource.Resource;

import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;

import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;

import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage;

/**
 * <!-- begin-user-doc -->
 * The <b>Resource Factory</b> associated with the package.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.util.GitlabWebhookResourceImpl
 * @generated
 */
public class GitlabWebhookResourceFactoryImpl extends ResourceFactoryImpl {
	/**
	 * Creates an instance of the resource factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GitlabWebhookResourceFactoryImpl() {
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
		Resource result = new GitlabWebhookResourceImpl(uri);
		return result;
	}

	/**
	 * A method providing the Properties the services around this ResourceFactory should be registered with.
	 * @generated
	 */
	public Map<String, Object> getServiceProperties() {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(EMFNamespaces.EMF_CONFIGURATOR_NAME, GitlabWebhookPackage.eNAME);
		properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, "gitlabwebhook");
		properties.put(EMFNamespaces.EMF_MODEL_VERSION, "1.0");
		return properties;
	}

} //GitlabWebhookResourceFactoryImpl
