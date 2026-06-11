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
package org.eclipse.fennec.data.atlas.jpa.rest.tests.helper;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * 
 * @author ilenia
 * @since Apr 28, 2026
 */
public class TestHelper {
	
	 /**
     * Registers the XMI resource factory if not already registered.
     *
     * @param resourceSet the ResourceSet to register the factory with
     */
    public static void ensureXMIFactory(ResourceSet resourceSet) {
        if (!resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey("xmi")) {
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi",
                    new XMIResourceFactoryImpl());
        }
    }

}
