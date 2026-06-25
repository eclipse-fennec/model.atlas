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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

import org.eclipse.fennec.model.atlas.rest.client.api.AuthType;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.impl.spi.DefaultJakartaRsClientProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * P2-2 — the default plain-Java provider applies the configured connect/read
 * timeouts to the {@link ClientBuilder}. The single
 * {@link ClientBuilder#newBuilder()} call site is overridden so the test needs
 * no Jakarta RS runtime.
 */
class DefaultJakartaRsClientProviderTest {

	@Test
	void appliesConnectAndReadTimeoutsThenBuilds() {
		ClientBuilder clientBuilder = mock(ClientBuilder.class);
		Client built = mock(Client.class);
		when(clientBuilder.build()).thenReturn(built);

		DefaultJakartaRsClientProvider provider = new DefaultJakartaRsClientProvider() {
			@Override
			protected ClientBuilder newClientBuilder() {
				return clientBuilder;
			}
		};

		ClientConfiguration config = ClientConfiguration.builder().baseUri(URI.create("https://atlas.example.org"))
				.connectTimeoutMs(7_000).readTimeoutMs(42_000).build();

		Client result = provider.newClient(config);

		assertEquals(built, result);
		verify(clientBuilder).connectTimeout(7_000L, TimeUnit.MILLISECONDS);
		verify(clientBuilder).readTimeout(42_000L, TimeUnit.MILLISECONDS);
		verify(clientBuilder).build();
	}

	private static final URI BASE = URI.create("https://atlas.example.org");

	private static ClientBuilder mockBuilder() {
		ClientBuilder b = mock(ClientBuilder.class);
		when(b.build()).thenReturn(mock(Client.class));
		return b;
	}

	// ---- bearer (P2-10) ---------------------------------------------------

	@Test
	void bearerAuth_registersFilterAddingAuthorizationHeader() throws Exception {
		ClientBuilder clientBuilder = mockBuilder();
		DefaultJakartaRsClientProvider provider = new DefaultJakartaRsClientProvider() {
			@Override
			protected ClientBuilder newClientBuilder() {
				return clientBuilder;
			}

			@Override
			protected String resolveToken(String envName) {
				return "secret-token";
			}
		};
		ClientConfiguration config = ClientConfiguration.builder().baseUri(BASE).authType(AuthType.BEARER)
				.authTokenEnv("ATLAS_TOKEN").build();

		provider.newClient(config);

		ArgumentCaptor<Object> registered = ArgumentCaptor.forClass(Object.class);
		verify(clientBuilder).register(registered.capture());
		Object filter = registered.getValue();
		assertInstanceOf(ClientRequestFilter.class, filter);

		// The filter stamps the Authorization header.
		ClientRequestContext ctx = mock(ClientRequestContext.class);
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		when(ctx.getHeaders()).thenReturn(headers);
		((ClientRequestFilter) filter).filter(ctx);
		assertEquals("Bearer secret-token", headers.getFirst("Authorization"));
	}

	@Test
	void bearerAuth_noToken_registersNothing() {
		ClientBuilder clientBuilder = mockBuilder();
		DefaultJakartaRsClientProvider provider = new DefaultJakartaRsClientProvider() {
			@Override
			protected ClientBuilder newClientBuilder() {
				return clientBuilder;
			}

			@Override
			protected String resolveToken(String envName) {
				return null; // env var unset
			}
		};
		ClientConfiguration config = ClientConfiguration.builder().baseUri(BASE).authType(AuthType.BEARER)
				.authTokenEnv("ATLAS_TOKEN").build();

		provider.newClient(config);

		verify(clientBuilder, never()).register(any());
	}

	// ---- mTLS (P2-10) -----------------------------------------------------

	@Test
	void mtls_installsKeyAndTrustStores() throws Exception {
		ClientBuilder clientBuilder = mockBuilder();
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		KeyStore trustStore = KeyStore.getInstance("PKCS12");
		DefaultJakartaRsClientProvider provider = new DefaultJakartaRsClientProvider() {
			@Override
			protected ClientBuilder newClientBuilder() {
				return clientBuilder;
			}

			@Override
			protected KeyStore loadStore(String path, String password, String type) {
				return path.contains("keystore") ? keyStore : trustStore;
			}
		};
		ClientConfiguration config = ClientConfiguration.builder().baseUri(BASE).authType(AuthType.MTLS)
				.keystorePath("/etc/atlas/keystore.p12").keystorePassword("kpw")
				.truststorePath("/etc/atlas/truststore.p12").truststorePassword("tpw").build();

		provider.newClient(config);

		verify(clientBuilder).keyStore(eq(keyStore), aryEq("kpw".toCharArray()));
		verify(clientBuilder).trustStore(trustStore);
	}

	@Test
	void noneAuth_installsNothing() {
		ClientBuilder clientBuilder = mockBuilder();
		DefaultJakartaRsClientProvider provider = new DefaultJakartaRsClientProvider() {
			@Override
			protected ClientBuilder newClientBuilder() {
				return clientBuilder;
			}
		};
		ClientConfiguration config = ClientConfiguration.builder().baseUri(BASE).build(); // AuthType.NONE

		provider.newClient(config);

		verify(clientBuilder, never()).register(any());
		verify(clientBuilder, never()).keyStore(any(KeyStore.class), any(char[].class));
		verify(clientBuilder, never()).trustStore(any(KeyStore.class));
	}
}
