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
 * Base type for every error raised by the Model Atlas client.
 * <p>
 * Unchecked so that the read-oriented client API (which signals "absent"
 * through {@link java.util.Optional}) stays free of checked-exception noise;
 * exceptional conditions — a transport failure, an unexpected server response,
 * a hard {@code 404} on a direct fetch — surface as subclasses of this type.
 *
 * @see NotFoundException
 * @see TransportException
 */
public class ModelAtlasClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ModelAtlasClientException(String message) {
		super(message);
	}

	public ModelAtlasClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModelAtlasClientException(Throwable cause) {
		super(cause);
	}
}
