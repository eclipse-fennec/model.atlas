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
 * Raised when the Atlas cannot be reached or does not answer in time —
 * connection refused, DNS failure, connect/read timeout, or any other
 * transport-level fault below the HTTP status layer.
 */
public class TransportException extends ModelAtlasClientException {

	private static final long serialVersionUID = 1L;

	public TransportException(String message) {
		super(message);
	}

	public TransportException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransportException(Throwable cause) {
		super(cause);
	}
}
