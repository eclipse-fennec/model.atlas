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
package org.eclipse.fennec.model.atlas.rest.client.impl.spi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.JakartaRsClientProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;

/**
 * Default, plain-Java {@link JakartaRsClientProvider}. Builds a Jakarta RS
 * {@link Client} from {@code ClientBuilder.newBuilder()}, applies the configured
 * connect/read timeouts, and wires authentication (P2-10).
 * <p>
 * This is the <em>only</em> place in the code base that calls
 * {@link ClientBuilder#newBuilder()} — the SPI seam the rest of the client is
 * built on. The OSGi front-end (Phase 3) supplies its own provider that resolves
 * the Whiteboard {@code ClientBuilder} instead, but reuses the same auth wiring
 * by extending this class.
 * <p>
 * Authentication per {@code auth.type}:
 * <ul>
 * <li>{@code bearer} — registers a request filter adding {@code Authorization:
 * Bearer <token>}, the token read from the env var named by
 * {@code auth.token.env};</li>
 * <li>{@code mtls} — loads the configured key/trust stores and installs them on
 * the {@link ClientBuilder};</li>
 * <li>{@code none} — nothing.</li>
 * </ul>
 */
public class DefaultJakartaRsClientProvider implements JakartaRsClientProvider {

	private static final Logger logger = Logger.getLogger(DefaultJakartaRsClientProvider.class.getName());

	@Override
	public Client newClient(ClientConfiguration configuration) {
		Objects.requireNonNull(configuration, "configuration");
		ClientBuilder builder = newClientBuilder();
		builder.connectTimeout(configuration.getConnectTimeoutMs(), TimeUnit.MILLISECONDS);
		builder.readTimeout(configuration.getReadTimeoutMs(), TimeUnit.MILLISECONDS);
		applyAuth(builder, configuration);
		return builder.build();
	}

	/** Apply the configured authentication to the builder. */
	protected void applyAuth(ClientBuilder builder, ClientConfiguration configuration) {
		switch (configuration.getAuthType()) {
		case BEARER -> applyBearer(builder, configuration);
		case MTLS -> applyMutualTls(builder, configuration);
		case NONE -> {
			// no authentication
		}
		}
	}

	private void applyBearer(ClientBuilder builder, ClientConfiguration configuration) {
		String token = resolveToken(configuration.getAuthTokenEnv());
		if (token == null || token.isBlank()) {
			logger.log(Level.WARNING, () -> "auth.type=bearer but no token resolved from env var '"
					+ configuration.getAuthTokenEnv() + "'; requests will be unauthenticated");
			return;
		}
		builder.register(new BearerTokenFilter(token));
	}

	private void applyMutualTls(ClientBuilder builder, ClientConfiguration configuration) {
		KeyStore keyStore = loadStore(configuration.getKeystorePath(), configuration.getKeystorePassword(),
				configuration.getKeystoreType());
		if (keyStore != null) {
			char[] password = configuration.getKeystorePassword() == null ? new char[0]
					: configuration.getKeystorePassword().toCharArray();
			builder.keyStore(keyStore, password);
		}
		KeyStore trustStore = loadStore(configuration.getTruststorePath(), configuration.getTruststorePassword(),
				configuration.getTruststoreType());
		if (trustStore != null) {
			builder.trustStore(trustStore);
		}
	}

	/**
	 * Resolve the bearer token from the named environment variable. Seam for
	 * testing.
	 *
	 * @param envName the env var name (may be {@code null})
	 * @return the token, or {@code null} if unset
	 */
	protected String resolveToken(String envName) {
		return envName == null ? null : System.getenv(envName);
	}

	/**
	 * Load a key/trust store from {@code path}. Seam for testing. Returns
	 * {@code null} when no path is configured.
	 *
	 * @param path     the store file path (may be {@code null}/blank)
	 * @param password the store password (may be {@code null})
	 * @param type     the store type (e.g. {@code PKCS12})
	 * @return the loaded {@link KeyStore}, or {@code null} when no path is set
	 */
	protected KeyStore loadStore(String path, String password, String type) {
		if (path == null || path.isBlank()) {
			return null;
		}
		try (InputStream in = Files.newInputStream(Path.of(path))) {
			KeyStore store = KeyStore.getInstance(type != null ? type : ClientConfiguration.DEFAULT_STORE_TYPE);
			store.load(in, password != null ? password.toCharArray() : null);
			return store;
		} catch (IOException | GeneralSecurityException e) {
			throw new ModelAtlasClientException("Failed to load key/trust store '" + path + "'", e);
		}
	}

	/**
	 * The single {@link ClientBuilder#newBuilder()} call site, isolated as a seam
	 * so tests can substitute a builder without a Jakarta RS runtime.
	 *
	 * @return a fresh Jakarta RS client builder
	 */
	protected ClientBuilder newClientBuilder() {
		return ClientBuilder.newBuilder();
	}

	/** Adds {@code Authorization: Bearer <token>} to every outgoing request. */
	private static final class BearerTokenFilter implements ClientRequestFilter {

		private final String token;

		BearerTokenFilter(String token) {
			this.token = token;
		}

		@Override
		public void filter(ClientRequestContext requestContext) {
			requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		}
	}
}
