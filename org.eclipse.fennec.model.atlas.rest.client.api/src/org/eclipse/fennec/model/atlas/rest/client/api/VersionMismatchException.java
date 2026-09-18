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
package org.eclipse.fennec.model.atlas.rest.client.api;

/**
 * A pinned read found a different model version than the one asked for (#274).
 * <p>
 * Raised when a fetch carried a fingerprint precondition and the Atlas answered {@code 412}: the
 * location resolved, but what sits there is not the version the caller named. Distinct from
 * {@link NotFoundException} on purpose — the package exists, and treating the two alike would let
 * a caller retry its way into the wrong content.
 *
 * @author Data In Motion
 */
public class VersionMismatchException extends ModelAtlasClientException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message what was expected and what was found
	 */
	public VersionMismatchException(String message) {
		super(message);
	}
}
