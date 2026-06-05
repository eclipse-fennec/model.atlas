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
package org.eclipse.fennec.model.atlas.rest.client.api;

import java.net.URI;
import java.util.List;
import java.util.ServiceLoader;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Read-only client for a remote Model Atlas instance.
 * <p>
 * This is the public surface shared by both the plain-Java implementation
 * ({@code rest.client.impl}) and the OSGi front-end ({@code rest.client.osgi},
 * Phase 3). The implementation builds its Jakarta RS client through a
 * {@link JakartaRsClientProvider} and is driven by a {@link ClientConfiguration};
 * everything above the provider seam — REST mapping, caching, drift detection —
 * is identical between the two variants.
 * <p>
 * Instances hold a Jakarta RS client and must be {@link #close() closed} when no
 * longer needed.
 */
@ProviderType
public interface ModelAtlasClient extends AutoCloseable {

	/**
	 * Entry point for plain-Java consumers: a fresh {@link Builder}.
	 * <p>
	 * The concrete builder is discovered through {@link ServiceLoader} (the same
	 * mechanism {@code jakarta.ws.rs.client.ClientBuilder.newBuilder()} uses),
	 * which keeps this API bundle free of any implementation dependency. The
	 * implementation bundle ({@code rest.client.impl}) registers a
	 * {@link ModelAtlasClientFactory} provider.
	 * <p>
	 * In OSGi (Phase 3) clients are obtained as DS services configured from
	 * ConfigAdmin rather than through this builder.
	 *
	 * @return a new builder
	 * @throws IllegalStateException if no implementation is on the classpath
	 */
	static Builder builder() {
		return ServiceLoader.load(ModelAtlasClientFactory.class, ModelAtlasClient.class.getClassLoader())
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"No ModelAtlasClient implementation found on the classpath (expected a "
								+ "ServiceLoader provider for " + ModelAtlasClientFactory.class.getName()
								+ ", e.g. the rest.client.impl bundle)"))
				.builder();
	}

	/**
	 * Discover the scope names the server exposes ({@code GET /scopes}).
	 *
	 * @return the scope names (possibly empty)
	 */
	List<String> listScopeNames();

	/**
	 * Direct, cache-fronted access to remote EPackages.
	 *
	 * @return the EPackage provider
	 */
	RemoteEPackageProvider ePackages();

	/**
	 * Trigger a drift check across cached entries and report what changed.
	 *
	 * @return the drift report (never {@code null})
	 */
	DriftReport checkForDrift();

	/**
	 * Subscribe to drift events.
	 *
	 * @param listener the listener to register
	 * @return a handle whose {@code close()} unsubscribes the listener
	 */
	AutoCloseable addDriftListener(DriftListener listener);

	/**
	 * Build an Atlas-aware {@link ResourceSet} whose package registry falls back
	 * to this client on a miss — the one-liner integration point for plain-Java
	 * consumers loading XMI/JSON resources.
	 *
	 * @return a ready-to-use ResourceSet
	 */
	ResourceSet newResourceSet();

	/**
	 * Release the underlying Jakarta RS client and any background tasks.
	 */
	@Override
	void close();

	// Phase 5 (EObject registries, depends on the scope.api ScopedEObjectsRegistry
	// contract from Phase 4) will add:
	//   List<String> listRegistries(String scopeName);                              // GET /scopes/{s}
	//   ScopedEObjectsRegistry<EObject> registry(String scopeName, String registry);

	/**
	 * Fluent builder for a plain-Java {@link ModelAtlasClient}.
	 * <p>
	 * The full configuration surface lives on {@link ClientConfiguration.Builder};
	 * this builder adds the construction-time conveniences ({@code base.uri},
	 * timeouts) plus the {@link JakartaRsClientProvider} seam. Reach for
	 * {@link #configuration(ClientConfiguration)} to set the remaining properties.
	 */
	interface Builder {

		/**
		 * Use this complete configuration as the basis, replacing any values set
		 * so far. Subsequent convenience setters refine it.
		 *
		 * @param configuration the configuration to start from
		 * @return this builder
		 */
		Builder configuration(ClientConfiguration configuration);

		/**
		 * Required server base URI ({@code base.uri}).
		 *
		 * @param baseUri the base URI
		 * @return this builder
		 */
		Builder baseUri(URI baseUri);

		/**
		 * Connect timeout in milliseconds ({@code connect.timeout.ms}).
		 *
		 * @param connectTimeoutMs the connect timeout
		 * @return this builder
		 */
		Builder connectTimeoutMs(int connectTimeoutMs);

		/**
		 * Read timeout in milliseconds ({@code read.timeout.ms}).
		 *
		 * @param readTimeoutMs the read timeout
		 * @return this builder
		 */
		Builder readTimeoutMs(int readTimeoutMs);

		/**
		 * Override the Jakarta RS client seam. Defaults to the implementation's
		 * plain-Java provider ({@code ClientBuilder.newBuilder()}); the OSGi
		 * front-end (Phase 3) supplies a Whiteboard-backed provider here.
		 *
		 * @param clientProvider the provider to use
		 * @return this builder
		 */
		Builder clientProvider(JakartaRsClientProvider clientProvider);

		/**
		 * Build the client. Opens the underlying Jakarta RS client immediately.
		 *
		 * @return a ready-to-use, must-be-closed client
		 * @throws IllegalStateException if {@code base.uri} was never set
		 */
		ModelAtlasClient build();
	}
}
