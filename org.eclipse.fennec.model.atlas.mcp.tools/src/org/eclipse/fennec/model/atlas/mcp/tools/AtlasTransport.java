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
package org.eclipse.fennec.model.atlas.mcp.tools;

import java.util.Map;

/**
 * The seam between publishing logic and HTTP.
 * <p>
 * Everything that decides <em>what</em> gets published and <em>what the agent is
 * told</em> — the namespace allow-list, validation, receipt shaping — sits above
 * this interface and is testable without a server. Below it sits the only code
 * whose exact shape depends on a running model.atlas.
 *
 * @author ilenia
 * @since Aug 26, 2026
 */
public interface AtlasTransport {

	/**
	 * A raw upstream response. Never handed to the agent as-is: an upstream body
	 * can carry the deployment's internals, and the receipt is shaped from it.
	 * <p>
	 * Named {@code Result} rather than {@code Response} so that an implementation
	 * can also name {@code jakarta.ws.rs.core.Response} without qualifying either.
	 *
	 * @param status the HTTP status code, or {@code 0} when the endpoint could not be reached
	 * @param body   the response body, possibly empty, never {@code null}
	 */
	record Result(int status, String body) {

		/** @return whether the endpoint answered at all */
		public boolean reached() {
			return status > 0;
		}
	}

	/**
	 * @param path        the path below the configured base URI, already URI-safe
	 * @param query       the query parameters
	 * @param contentType the request content type
	 * @param body        the request body
	 * @return the upstream response, or a {@code status == 0} response if unreachable
	 */
	Result post(String path, Map<String, String> query, String contentType, String body);

	/**
	 * @param path the path below the configured base URI
	 * @return the upstream response, or a {@code status == 0} response if unreachable
	 */
	Result get(String path);
}
