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

import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.gecko.emf.osgi.constants.EMFNamespaces;

/**
 * 
 * @author ilenia
 * @since Nov 13, 2025
 */
public class EnhanchedUMLResourceFactoryImpl extends UMLResourceFactoryImpl {
	
	public EnhanchedUMLResourceFactoryImpl() {
		super();
	}
	
	/**
	 * A method providing the Properties the services around this ResourceFactory should be registered with.
	 * @generated
	 */
	public Map<String, Object> getServiceProperties() {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(EMFNamespaces.EMF_CONFIGURATOR_NAME, UMLPackage.eNAME);
		properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, "uml");
		properties.put(EMFNamespaces.EMF_MODEL_VERSION, "1.0");
		return properties;
	}

}
