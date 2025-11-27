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
package org.eclipse.fennec.model.atlas.rest.uml;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.uml2.uml.UMLPackage;
import org.gecko.emf.osgi.configurator.EPackageConfigurator;
import org.gecko.emf.osgi.constants.EMFNamespaces;

/**
 * 
 * @author ilenia
 * @since Nov 13, 2025
 */
public class UMLEPackageConfigurator implements EPackageConfigurator {
	
	private UMLPackage ePackage;
	
	protected UMLEPackageConfigurator(UMLPackage ePackage){
		this.ePackage = ePackage;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.osgi.configurator.EPackageConfigurator#configureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
	 */
	@Override
	public void configureEPackage(Registry registry) {
		registry.put(UMLPackage.eNS_URI, ePackage);

	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.osgi.configurator.EPackageConfigurator#unconfigureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
	 */
	@Override
	public void unconfigureEPackage(Registry registry) {
		registry.remove(UMLPackage.eNS_URI);

	}
	
	public Map<String, Object> getServiceProperties() {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(EMFNamespaces.EMF_MODEL_NAME, UMLPackage.eNAME);
		properties.put(EMFNamespaces.EMF_MODEL_NSURI, UMLPackage.eNS_URI);
		properties.put(EMFNamespaces.EMF_MODEL_REGISTRATION, EMFNamespaces.MODEL_REGISTRATION_PROVIDED);
		properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, "uml");
		properties.put(EMFNamespaces.EMF_MODEL_VERSION, "1.0");
		return properties;
	}

}
