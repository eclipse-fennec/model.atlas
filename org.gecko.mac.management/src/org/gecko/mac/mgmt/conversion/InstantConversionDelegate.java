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

import java.time.Instant;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EDataType.Internal.ConversionDelegate;

/**
 * ConversionDelegate for java.time.Instant serialization/deserialization in EMF.
 * 
 * @author mark
 * @since 09.10.2025
 */
public class InstantConversionDelegate implements ConversionDelegate {

	private static final Logger LOGGER = Logger.getLogger(InstantConversionDelegate.class.getName());

	@Override
	public String convertToString(Object value) {
		if (value == null) {
			return null;
		}
		
		if (!(value instanceof Instant)) {
			LOGGER.log(Level.WARNING, "Expected Instant but got: " + value.getClass());
			return null;
		}
		
		try {
			return ((Instant) value).toString();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to convert Instant to string: " + value, e);
			return null;
		}
	}

	@Override
	public Object createFromString(String literal) {
		if (Objects.isNull(literal) || literal.trim().isEmpty()) {
			return null;
		}
		
		try {
			return Instant.parse(literal.trim());
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to parse Instant from string: " + literal, e);
			throw new IllegalArgumentException("Cannot parse Instant from: " + literal, e);
		}
	}

}
