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
package org.eclipse.fennec.model.atlas.rest.client.osgi;

import java.util.Objects;

import org.eclipse.fennec.model.atlas.rest.client.impl.spi.DefaultJakartaRsClientProvider;

import jakarta.ws.rs.client.ClientBuilder;

/**
 * P3-2 — the OSGi front-end's {@code JakartaRsClientProvider}. It reuses the
 * plain-Java provider's timeout and authentication wiring (P2-10) by extending
 * {@link DefaultJakartaRsClientProvider} and overriding only the builder-creation
 * seam: instead of {@code ClientBuilder.newBuilder()} it returns the
 * {@link ClientBuilder} resolved from the OSGi service registry (registered by the
 * Jakarta RS Whiteboard), so the runtime's HTTP client selection, registered
 * providers and framework-level configuration apply.
 * <p>
 * One instance wraps one Whiteboard {@code ClientBuilder} and is used to build a
 * single client (one per {@link AtlasClientComponent} configuration), so the
 * builder is configured and built exactly once.
 */
final class WhiteboardJakartaRsClientProvider extends DefaultJakartaRsClientProvider {

	private final ClientBuilder clientBuilder;

	WhiteboardJakartaRsClientProvider(ClientBuilder clientBuilder) {
		this.clientBuilder = Objects.requireNonNull(clientBuilder, "clientBuilder");
	}

	@Override
	protected ClientBuilder newClientBuilder() {
		return clientBuilder;
	}
}
