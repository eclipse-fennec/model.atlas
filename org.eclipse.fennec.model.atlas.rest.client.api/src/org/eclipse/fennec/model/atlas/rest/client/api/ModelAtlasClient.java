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

import java.util.List;

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
}
