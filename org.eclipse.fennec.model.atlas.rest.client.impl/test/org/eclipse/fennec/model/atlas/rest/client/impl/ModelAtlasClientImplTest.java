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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.JakartaRsClientProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClient;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.client.Client;

/**
 * P2-2 — construction / lifecycle of the plain-Java {@link ModelAtlasClient}.
 * <p>
 * The builder is driven through {@link DefaultModelAtlasClientFactory} directly
 * rather than the static {@link ModelAtlasClient#builder()} so the tests do not
 * depend on the {@code ServiceLoader} descriptor being on the plain unit-test
 * classpath; both paths exercise the same builder implementation. A fake
 * {@link JakartaRsClientProvider} keeps the tests free of a Jakarta RS runtime.
 */
class ModelAtlasClientImplTest {

	private static final URI BASE = URI.create("https://atlas.example.org/atlas");

	private static ModelAtlasClient.Builder builder() {
		return new DefaultModelAtlasClientFactory().builder();
	}

	/** Captures the configuration it is handed and returns a supplied client. */
	private static final class CapturingProvider implements JakartaRsClientProvider {
		private final Client client;
		private final AtomicReference<ClientConfiguration> seen = new AtomicReference<>();

		CapturingProvider(Client client) {
			this.client = client;
		}

		@Override
		public Client newClient(ClientConfiguration configuration) {
			seen.set(configuration);
			return client;
		}
	}

	@Test
	void builderBuildsAnInstanceFromConfiguration() {
		Client client = mock(Client.class);
		CapturingProvider provider = new CapturingProvider(client);

		ClientConfiguration config = ClientConfiguration.builder().baseUri(BASE).connectTimeoutMs(1_234)
				.readTimeoutMs(5_678).build();

		ModelAtlasClient atlas = builder().configuration(config).clientProvider(provider).build();

		assertInstanceOf(ModelAtlasClientImpl.class, atlas);
		// The underlying client was obtained through the SPI seam, from our config
		// (the builder copies the configuration, so it is equal, not identical).
		assertEquals(config, provider.seen.get());
	}

	@Test
	void builderConvenienceSettersFeedTheConfiguration() {
		Client client = mock(Client.class);
		CapturingProvider provider = new CapturingProvider(client);

		builder().baseUri(BASE).connectTimeoutMs(111).readTimeoutMs(222).clientProvider(provider).build();

		ClientConfiguration seen = provider.seen.get();
		assertEquals(BASE, seen.getBaseUri());
		assertEquals(111, seen.getConnectTimeoutMs());
		assertEquals(222, seen.getReadTimeoutMs());
	}

	@Test
	void builderRequiresBaseUri() {
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> builder().clientProvider(new CapturingProvider(mock(Client.class))).build());
		assertTrue(ex.getMessage().contains("base.uri"), ex.getMessage());
	}

	@Test
	void closeReleasesTheUnderlyingClient() {
		Client client = mock(Client.class);
		ModelAtlasClient atlas = builder().baseUri(BASE).clientProvider(new CapturingProvider(client)).build();

		atlas.close();

		verify(client).close();
	}

}
