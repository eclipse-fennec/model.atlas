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
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * This implementation of a {@link DynamicEObjectImpl}, can handle EClasses, that become proxies 
 * due to a reload of their EPackage.
 * 
 * @author Juergen Albert
 * @since 20 Feb 2025
 */
public class EClassResolvingDynamicEObject extends DynamicEObjectImpl {
	
	/**
	 * Creates a new instance.
	 * @param eClass
	 */
	public EClassResolvingDynamicEObject(EClass eClass) {
		super(eClass);
	}
	
	public EClassResolvingDynamicEObject() {
		super();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.emf.ecore.impl.DynamicEObjectImpl#eClass()
	 */
	@Override
	public EClass eClass() {
		if(eClass.eIsProxy()) {
			eClass = (EClass) EcoreUtil.resolve(eClass, this);
		}
		return eClass;
	}
}
