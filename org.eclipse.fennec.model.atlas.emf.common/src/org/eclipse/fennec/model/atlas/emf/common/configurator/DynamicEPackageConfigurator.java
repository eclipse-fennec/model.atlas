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
package org.eclipse.fennec.model.atlas.emf.common.configurator;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.gecko.emf.osgi.configurator.EPackageConfigurator;

public class DynamicEPackageConfigurator implements EPackageConfigurator {
	
	private static Logger logger = Logger.getLogger(DynamicEPackageConfigurator.class.getName());
	private final EPackage ePackage;

	/**
	 * Creates a new instance.
	 */
	public DynamicEPackageConfigurator(EPackage ePackage) {
		this.ePackage = ePackage;
	}


	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.osgi.EPackageConfigurator#configureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
	 */
	@Override
	public void configureEPackage(org.eclipse.emf.ecore.EPackage.Registry registry) {
		if (ePackage != null) {
			registry.put(ePackage.getNsURI(), ePackage);
			//Todo: This can be removed with the fennec EMF by targeting the static registry and is only neccessary, so QVT  
			EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
		} else {
			logger.log(Level.SEVERE, ()->"Error registering a NULL package, that should never happen");
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.osgi.EPackageConfigurator#unconfigureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
	 */
	@Override
	public void unconfigureEPackage(org.eclipse.emf.ecore.EPackage.Registry registry) {
		if (ePackage != null) {
			registry.remove(ePackage.getNsURI());
			EPackage.Registry.INSTANCE.remove(ePackage.getNsURI());
		} else {
			logger.log(Level.SEVERE, ()->"Error un-registering a NULL package, that should never happen");
		}
	}

}
