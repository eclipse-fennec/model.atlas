/*
 * ******************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 * ******************************************************************
 */
package org.eclipse.fennec.model.atlas.publisher;

/**
 * Exception carrying a sanitized, caller-facing error message.
 * <p>
 * A message of this exception is written for whoever asked for the publication —
 * an agent talking to an MCP tool, or the service that called a publisher
 * directly — and is safe to hand on verbatim: it never carries the model.atlas
 * base URI, an upstream response body, or anything else about the deployment.
 * Every other exception out of a publisher is a fault, and says nothing.
 *
 * @author ilenia
 * @since Aug 26, 2026
 */
public class PublishException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PublishException(String message) {
		super(message);
	}
}
