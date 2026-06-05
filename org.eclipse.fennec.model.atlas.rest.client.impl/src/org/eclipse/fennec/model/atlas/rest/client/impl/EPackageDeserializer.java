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
package org.eclipse.fennec.model.atlas.rest.client.impl;

import java.io.InputStream;

import org.eclipse.emf.ecore.EPackage;

/**
 * Seam that turns the body of a package content GET into an {@link EPackage}.
 * <p>
 * The REST mapping (scope walking, status handling, error mapping — P2-3) hands
 * the response body here; {@link XmiEPackageDeserializer} is the default Ecore
 * XMI implementation (P2-4). Isolating it as a seam lets the REST mapping be
 * tested with a stub and lets the OSGi front-end (Phase 3) substitute a
 * registry-aware variant.
 */
@FunctionalInterface
public interface EPackageDeserializer {

	/**
	 * Deserialize a package payload.
	 *
	 * @param content   the response body stream (already buffered by the caller)
	 * @param nsUri     the namespace URI that was requested
	 * @param mediaType the response {@code Content-Type}, for format selection
	 * @return the deserialized package
	 */
	EPackage deserialize(InputStream content, String nsUri, String mediaType);
}
