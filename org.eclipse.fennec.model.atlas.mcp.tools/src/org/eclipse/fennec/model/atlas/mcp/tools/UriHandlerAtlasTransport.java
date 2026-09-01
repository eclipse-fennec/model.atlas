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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.fennec.emf.osgi.constants.EMFUriHandlerConstants;

/**
 * The only class in this bundle that talks HTTP.
 * <p>
 * It goes through the {@link URIConverter} of the runtime's own
 * {@code ResourceSet}, where Fennec's RESTful URI handler serves {@code http} and
 * {@code https}. That is a deliberate choice over a JAX-RS client: the handler is
 * already in every Fennec runtime, so publishing costs no additional bundle, and
 * — unlike {@code jakarta.ws.rs.client.ClientBuilder}, which resolves its provider
 * through the thread context class loader — it cannot fail because a provider
 * bundle happened to activate lazily after this component did.
 * <p>
 * The {@code .ecore} document is written as bytes the caller already produced,
 * not as a live {@code EPackage} handed to a codec: what leaves the runtime is
 * then exactly what {@link EcoreXmi} serialized.
 * <p>
 * An unreachable endpoint is reported as {@code status == 0} rather than thrown:
 * the caller shapes every outcome into a receipt, and a connection failure is one
 * of them.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
class UriHandlerAtlasTransport implements AtlasTransport {

	private static final Logger LOGGER = Logger.getLogger(UriHandlerAtlasTransport.class.getName());

	/**
	 * The key Fennec's RESTful URI handler writes the status under. It is private
	 * there and absent from {@code EMFUriHandlerConstants}, so it is repeated here;
	 * exporting it upstream would remove this duplication.
	 */
	private static final String RESPONSE_CODE = "HTTPResponseCode";

	/**
	 * Recovers the status from a failed read. On write the handler publishes the
	 * code into the response map before it throws, so this is only needed for
	 * {@code createInputStream}, which does not.
	 */
	private static final Pattern STATUS_IN_MESSAGE = Pattern.compile("failed with HTTP response code (\\d{3})");

	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String HEADER_ACCEPT = "Accept";
	private static final String HEADER_AUTHORIZATION = "Authorization";

	private final URIConverter uriConverter;
	private final String baseUri;
	private final String authTokenEnv;
	private final int timeoutMs;

	/**
	 * @param uriConverter the converter whose URI handlers serve http/https
	 * @param baseUri      the model.atlas REST base URI
	 * @param authTokenEnv the environment variable holding the bearer token, may be blank for no authentication
	 * @param timeoutMs    the connect/read timeout applied to every request
	 */
	UriHandlerAtlasTransport(URIConverter uriConverter, String baseUri, String authTokenEnv, int timeoutMs) {
		this.uriConverter = uriConverter;
		this.baseUri = baseUri;
		this.authTokenEnv = authTokenEnv;
		this.timeoutMs = timeoutMs;
	}

	@Override
	public AtlasTransport.Result post(String path, Map<String, String> query, String contentType, String body) {
		URI uri = uri(path, query);
		Map<Object, Object> response = new HashMap<>();
		Map<Object, Object> options = options(response);
		options.put(EMFUriHandlerConstants.OPTION_HTTP_METHOD, "POST");
		options.put(EMFUriHandlerConstants.OPTION_HTTP_HEADERS, headers(HEADER_CONTENT_TYPE, contentType));
		try (OutputStream out = uriConverter.createOutputStream(uri, options)) {
			out.write(body.getBytes(StandardCharsets.UTF_8));
		} catch (IOException | RuntimeException e) {
			return failure("POST", uri, e, response);
		}
		return new AtlasTransport.Result(status(response, 0), "");
	}

	@Override
	public AtlasTransport.Result get(String path) {
		URI uri = uri(path, Map.of());
		Map<Object, Object> response = new HashMap<>();
		Map<Object, Object> options = options(response);
		options.put(EMFUriHandlerConstants.OPTION_HTTP_HEADERS, headers(HEADER_ACCEPT, "application/json"));
		try (InputStream in = uriConverter.createInputStream(uri, options)) {
			String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			return new AtlasTransport.Result(status(response, 200), body);
		} catch (IOException | RuntimeException e) {
			return failure("GET", uri, e, response);
		}
	}

	/**
	 * Reports a failed request without letting the deployment's address or the
	 * upstream body escape into the returned result's shaping path — both go to the
	 * log, where an operator can read them.
	 */
	private static AtlasTransport.Result failure(String method, URI uri, Exception e, Map<Object, Object> response) {
		LOGGER.log(Level.WARNING, e, () -> String.format("%s to model.atlas failed: %s", method, uri));
		int status = status(response, statusFromMessage(e));
		return new AtlasTransport.Result(status, e.getMessage() == null ? "" : e.getMessage());
	}

	/** @return the status the handler published, or {@code fallback} when it published none */
	private static int status(Map<Object, Object> response, int fallback) {
		Object code = response.get(RESPONSE_CODE);
		return code instanceof Integer status ? status.intValue() : fallback;
	}

	/**
	 * @return the status the handler named in its message, or {@code 0} — which the
	 *         caller reads as "the atlas was never reached", the right answer for a
	 *         connection failure, which is the case that produces no status at all
	 */
	private static int statusFromMessage(Exception e) {
		String message = e.getMessage();
		if (message == null) {
			return 0;
		}
		Matcher matcher = STATUS_IN_MESSAGE.matcher(message);
		return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
	}

	private Map<Object, Object> options(Map<Object, Object> response) {
		Map<Object, Object> options = new LinkedHashMap<>();
		options.put(URIConverter.OPTION_RESPONSE, response);
		options.put(URIConverter.OPTION_TIMEOUT, Integer.valueOf(timeoutMs));
		return options;
	}

	/**
	 * The token is read per request rather than held in a field, so rotating it in
	 * the environment does not need the component reconfigured — and so this object
	 * never carries a credential.
	 */
	private Map<String, String> headers(String contentHeader, String contentValue) {
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put(contentHeader, contentValue);
		if (authTokenEnv != null && !authTokenEnv.isBlank()) {
			String token = System.getenv(authTokenEnv);
			if (token == null || token.isBlank()) {
				LOGGER.log(Level.WARNING, () -> String.format(
						"Environment variable '%s' holds no bearer token; the request goes out unauthenticated",
						authTokenEnv));
			} else {
				headers.put(HEADER_AUTHORIZATION, "Bearer " + token);
			}
		}
		return headers;
	}

	/**
	 * Builds the request URI with its query escaped. The result is parsed with
	 * escapes preserved, so a namespace URI carrying {@code :}, {@code /} or
	 * {@code #} survives the round trip into the query string.
	 */
	private URI uri(String path, Map<String, String> query) {
		StringBuilder uri = new StringBuilder(baseUri);
		if (!baseUri.endsWith("/")) {
			uri.append('/');
		}
		uri.append(path);
		if (!query.isEmpty()) {
			StringJoiner parameters = new StringJoiner("&");
			query.forEach((key, value) -> parameters.add(encode(key) + "=" + encode(value)));
			uri.append('?').append(parameters);
		}
		return URI.createURI(uri.toString(), true);
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
