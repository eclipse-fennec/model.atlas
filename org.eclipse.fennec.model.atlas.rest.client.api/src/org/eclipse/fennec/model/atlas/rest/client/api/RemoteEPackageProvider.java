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
import java.util.Optional;

import org.eclipse.emf.ecore.EPackage;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Read-only access to EPackages held by the Atlas, fronted by the client cache.
 * <p>
 * Anonymous lookups ({@link #getEPackage(String)}, {@link #ensureAvailable(String)},
 * {@link #refresh(String)}) resolve scope context by walking the configured
 * {@code scope.allow.list} in order — first hit wins — defaulting to
 * {@code default.scope}. The {@code released} stage is the default view.
 *
 * @see ModelAtlasClient#ePackages()
 */
@ProviderType
public interface RemoteEPackageProvider {

	/**
	 * Local-first lookup: returns the cached package, or on a miss fetches it
	 * from the server (and caches it). Empty when no scope holds the nsURI.
	 *
	 * @param nsUri the package namespace URI
	 * @return the package, or empty if unknown to the Atlas
	 */
	Optional<EPackage> getEPackage(String nsUri);

	/**
	 * List the nsURIs available in the given scope's {@code released} stage.
	 *
	 * @param scopeName the scope to enumerate
	 * @return the nsURIs (possibly empty)
	 */
	List<String> listNsUris(String scopeName);

	/**
	 * Eagerly load and cache an nsURI — useful for warm-up and for the OSGi
	 * registry-delegate path. Behaves like {@link #getEPackage(String)} but is
	 * named for its intent.
	 *
	 * @param nsUri the package namespace URI
	 * @return the package, or empty if unknown to the Atlas
	 */
	Optional<EPackage> ensureAvailable(String nsUri);

	/**
	 * Force a re-fetch of one nsURI from the server, bypassing the cache, and
	 * replace the cached entry with the result.
	 *
	 * @param nsUri the package namespace URI
	 * @return the freshly fetched package, or empty if it is no longer available
	 */
	Optional<EPackage> refresh(String nsUri);
}
