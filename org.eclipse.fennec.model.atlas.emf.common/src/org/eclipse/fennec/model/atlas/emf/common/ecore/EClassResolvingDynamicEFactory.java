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
package org.eclipse.fennec.model.atlas.emf.common.ecore;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EFactoryImpl;

/**
 * A Factory that creates DynamicEObjects, that will resolve their EClass if it was unloaded. 
 * This can happen, when we reload the EPackage it was created from in the background.
 * 
 * @author Juergen Albert
 * @since 20 Feb 2025
 */
public class EClassResolvingDynamicEFactory extends EFactoryImpl {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.emf.ecore.impl.EFactoryImpl#basicCreate(org.eclipse.emf.ecore.
	 * EClass)
	 */
	@Override
	protected EObject basicCreate(EClass eClass) {
		return eClass.getInstanceClassName() == "java.util.Map$Entry"
				? new EClassResolvingDynamicEObject.BasicEMapEntry<String, String>(eClass)
				: new EClassResolvingDynamicEObject(eClass);
	}

}
