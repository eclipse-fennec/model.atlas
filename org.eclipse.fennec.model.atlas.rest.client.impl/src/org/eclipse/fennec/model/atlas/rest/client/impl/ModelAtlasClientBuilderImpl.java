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

import java.net.URI;
import java.util.Objects;

import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.JakartaRsClientProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClient;
import org.eclipse.fennec.model.atlas.rest.client.impl.spi.DefaultJakartaRsClientProvider;

import jakarta.ws.rs.client.Client;

/**
 * Default {@link ModelAtlasClient.Builder}. Accumulates settings on a
 * {@link ClientConfiguration.Builder} and, on {@link #build()}, obtains the
 * Jakarta RS {@link Client} through the configured
 * {@link JakartaRsClientProvider} (the {@link DefaultJakartaRsClientProvider} by
 * default) and hands it to a {@link ModelAtlasClientImpl}.
 */
final class ModelAtlasClientBuilderImpl implements ModelAtlasClient.Builder {

	private ClientConfiguration.Builder configuration = ClientConfiguration.builder();
	private JakartaRsClientProvider clientProvider;

	@Override
	public ModelAtlasClient.Builder configuration(ClientConfiguration configuration) {
		this.configuration = ClientConfiguration.builder(Objects.requireNonNull(configuration, "configuration"));
		return this;
	}

	@Override
	public ModelAtlasClient.Builder baseUri(URI baseUri) {
		configuration.baseUri(baseUri);
		return this;
	}

	@Override
	public ModelAtlasClient.Builder connectTimeoutMs(int connectTimeoutMs) {
		configuration.connectTimeoutMs(connectTimeoutMs);
		return this;
	}

	@Override
	public ModelAtlasClient.Builder readTimeoutMs(int readTimeoutMs) {
		configuration.readTimeoutMs(readTimeoutMs);
		return this;
	}

	@Override
	public ModelAtlasClient.Builder clientProvider(JakartaRsClientProvider clientProvider) {
		this.clientProvider = clientProvider;
		return this;
	}

	@Override
	public ModelAtlasClient build() {
		ClientConfiguration config = configuration.build();
		JakartaRsClientProvider provider = clientProvider != null ? clientProvider
				: new DefaultJakartaRsClientProvider();
		Client client = provider.newClient(config);
		return new ModelAtlasClientImpl(config, client);
	}
}
