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

import java.util.List;

import org.eclipse.fennec.model.atlas.rest.client.api.Diagnostic;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;
import org.eclipse.fennec.model.atlas.rest.client.api.NotFoundException;
import org.eclipse.fennec.model.atlas.rest.client.api.OperationRefusedException;
import org.eclipse.fennec.model.atlas.rest.client.api.VersionMismatchException;
import org.eclipse.fennec.model.atlas.rest.client.api.TransportException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

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
	 * {@code 404} → {@link NotFoundException}, {@code 412} →
	 * {@link VersionMismatchException}, a {@code 409} whose body is an object's
	 * metadata with diagnostics → {@link OperationRefusedException} carrying them
	 * (issue #295), anything else → {@link ModelAtlasClientException}. Reads the
	 * body (best effort) into the message.
	 */
	static ModelAtlasClientException statusError(Response response, String what) {
		int status = response.getStatus();
		String body = safeBody(response);
		String detail = what + " — unexpected status " + status + (body.isEmpty() ? "" : ": " + body);
		if (status == Response.Status.NOT_FOUND.getStatusCode()) {
			return new NotFoundException(detail);
		}
		// A pinned read that found a different model version (#274): the location resolved, so
		// this is emphatically not a "not found" and must not be retried into the wrong content.
		if (status == Response.Status.PRECONDITION_FAILED.getStatusCode()) {
			return new VersionMismatchException(detail);
		}
		// A stage gate's veto (#295): the body is the object's metadata and its diagnostics say
		// what to change. A 409 without them - an occupied id - is the plain conflict it was.
		if (status == Response.Status.CONFLICT.getStatusCode()) {
			List<Diagnostic> diagnostics = refusalDiagnostics(body);
			if (!diagnostics.isEmpty()) {
				return new OperationRefusedException(what + " — refused by a stage gate: "
						+ diagnostics.stream().map(Diagnostic::message).filter(m -> m != null).reduce((a, b) -> a + "; " + b)
								.orElse(""), diagnostics);
			}
		}
		return new ModelAtlasClientException(detail);
	}

	/**
	 * The diagnostics of a 409 body that is an object's metadata; empty for any other body,
	 * including one that is no JSON at all.
	 */
	static List<Diagnostic> refusalDiagnostics(String body) {
		if (body == null || body.isBlank() || !body.trim().startsWith("{")) {
			return List.of();
		}
		try {
			JsonNode root = MAPPER.readTree(body);
			return root == null ? List.of() : RemoteEPackageProviderImpl.diagnosticsOf(root.get("diagnostics"));
		} catch (Exception e) {
			return List.of();
		}
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
