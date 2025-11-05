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
package org.gecko.mac.mgmt.conversion;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EDataType.Internal.ConversionDelegate;
import org.eclipse.emf.ecore.EDataType.Internal.ConversionDelegate.Factory;

/**
 * Factory for creating InstantConversionDelegate instances.
 * 
 * @author mark
 * @since 09.10.2025
 */
public class InstantConversionDelegateFactory implements Factory {
	
	@Override
	public ConversionDelegate createConversionDelegate(EDataType eDataType) {
		// Check if this is the Instant data type
		if ("java.time.Instant".equals(eDataType.getInstanceClassName())) {
			return new InstantConversionDelegate();
		}
		return null;
	}

}
