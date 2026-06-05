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

import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;
import org.eclipse.fennec.model.atlas.rest.client.api.NotFoundException;
import org.eclipse.fennec.model.atlas.rest.client.api.TransportException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;

/**
 * Small internal helpers shared by the REST mapping: executing a GET while
 * translating transport faults, mapping non-success statuses onto the typed
 * exception hierarchy, and parsing JSON response bodies.
 */
final class RestSupport {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private RestSupport() {
		// statics only
	}

	/**
	 * GET {@code target} accepting {@code acceptMediaType}, mapping a transport
	 * fault ({@link ProcessingException} — connect/read timeout, connection
	 * refused, …) to {@link TransportException}.
	 */
	static Response get(WebTarget target, String acceptMediaType) {
		return get(target, acceptMediaType, null);
	}

	/**
	 * GET {@code target} accepting {@code acceptMediaType}, sending a conditional
	 * {@code If-None-Match} when {@code ifNoneMatch} is non-null. Maps a transport
	 * fault to {@link TransportException}.
	 */
	static Response get(WebTarget target, String acceptMediaType, String ifNoneMatch) {
		try {
			Invocation.Builder request = target.request(acceptMediaType);
			if (ifNoneMatch != null) {
				request = request.header(HttpHeaders.IF_NONE_MATCH, ifNoneMatch);
			}
			return request.get();
		} catch (ProcessingException e) {
			throw new TransportException("GET " + target.getUri() + " failed", e);
		}
	}

	/**
	 * HEAD {@code target}, sending a conditional {@code If-None-Match} when
	 * {@code ifNoneMatch} is non-null. Maps a transport fault to
	 * {@link TransportException}.
	 */
	static Response head(WebTarget target, String ifNoneMatch) {
		try {
			Invocation.Builder request = target.request();
			if (ifNoneMatch != null) {
				request = request.header(HttpHeaders.IF_NONE_MATCH, ifNoneMatch);
			}
			return request.head();
		} catch (ProcessingException e) {
			throw new TransportException("HEAD " + target.getUri() + " failed", e);
		}
	}

	/** {@code true} for a {@code 304 Not Modified} response. */
	static boolean isNotModified(Response response) {
		return response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode();
	}

	/** {@code true} for a 2xx status. */
	static boolean isSuccess(Response response) {
		return response.getStatusInfo().getFamily() == Family.SUCCESSFUL;
	}

	/**
	 * Build the typed exception for an unexpected, non-success response:
	 * {@code 404} → {@link NotFoundException}, anything else →
	 * {@link ModelAtlasClientException}. Reads the body (best effort) into the
	 * message.
	 */
	static ModelAtlasClientException statusError(Response response, String what) {
		int status = response.getStatus();
		String body = safeBody(response);
		String detail = what + " — unexpected status " + status + (body.isEmpty() ? "" : ": " + body);
		if (status == Response.Status.NOT_FOUND.getStatusCode()) {
			return new NotFoundException(detail);
		}
		return new ModelAtlasClientException(detail);
	}

	/** Parse a JSON body into a tree, wrapping parse failures. */
	static JsonNode parse(String json, String what) {
		try {
			return MAPPER.readTree(json);
		} catch (Exception e) {
			throw new ModelAtlasClientException(what + " — could not parse response body", e);
		}
	}

	private static String safeBody(Response response) {
		try {
			if (response.hasEntity()) {
				return response.readEntity(String.class);
			}
		} catch (RuntimeException e) {
			// best effort only
		}
		return "";
	}
}
