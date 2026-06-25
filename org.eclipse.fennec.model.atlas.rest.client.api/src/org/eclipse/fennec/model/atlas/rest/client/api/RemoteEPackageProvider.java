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
 * {@code default.scope}. Reads are stage-free: the server resolves each scope's final
 * stage and walks inheritance, so no stage name is embedded in any read URL (P5-7).
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
	 * List the nsURIs available in the given scope's final stage (inheritance-aware).
	 *
	 * @param scopeName the scope to enumerate
	 * @return the nsURIs (possibly empty)
	 */
	List<String> listNsUris(String scopeName);

	/**
	 * List the packages in the given scope's final stage with the origin metadata the
	 * listing already carries — the metadata-rich form of {@link #listNsUris(String)}.
	 * <p>
	 * A single listing call yields, per package, its nsURI plus owning scope / stage /
	 * version (see {@link PackageDescriptor}), so a caller (e.g. the OSGi front-end's EAGER
	 * prefetch) can publish each package with its <em>real</em> stage/version without the
	 * per-package metadata round-trip {@link #resolve(String)} would add — the stage/version
	 * are already in the listing the enumeration fetched.
	 * <p>
	 * The default implementation derives descriptors from {@link #listNsUris(String)} with
	 * unset metadata, for providers that do not surface it; the remote client overrides it to
	 * parse the full listing.
	 *
	 * @param scopeName the scope to enumerate
	 * @return the package descriptors (possibly empty)
	 */
	default List<PackageDescriptor> listPackages(String scopeName) {
		return listNsUris(scopeName).stream().map(nsUri -> new PackageDescriptor(nsUri, null, null, null)).toList();
	}

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
	 * Resolve an nsURI to its package <em>and</em> its authoritative origin.
	 * <p>
	 * Unlike {@link #getEPackage(String)}, which walks the configured scopes
	 * probing for content and so cannot report where the package actually lives,
	 * this reads the server's metadata first ({@code GET
	 * /{scope}/schema?nsUri=…}, the stage-free final-stage listing, which respects scope
	 * inheritance) to learn the owning scope, registry, stage and version, then fetches the
	 * content from that exact location. The entry scope queried is gated by
	 * {@code scope.allow.list} / {@code default.scope} just like {@link #getEPackage(String)};
	 * the resolved owning scope may be a parent of it. Intended for the OSGi
	 * front-end's lazy publication, where the {@code atlas.*} origin properties must
	 * be accurate rather than approximated.
	 *
	 * @param nsUri the package namespace URI
	 * @return the resolved package with its origin, or empty if not visible from any
	 *         allowed scope
	 */
	Optional<ResolvedEPackage> resolve(String nsUri);

	/**
	 * Force a re-fetch of one nsURI from the server, bypassing the cache, and
	 * replace the cached entry with the result.
	 *
	 * @param nsUri the package namespace URI
	 * @return the freshly fetched package, or empty if it is no longer available
	 */
	Optional<EPackage> refresh(String nsUri);

	/**
	 * Fetch a package from a specific scope at a specific stage
	 * ({@code GET /{scopeName}/schema/stages/{stage}/content?nsUri=…}, P6-6).
	 * <p>
	 * Unlike {@link #getEPackage(String)}, this call is scope- and stage-pinned: the
	 * server does not walk inheritance or resolve to the final stage, so the returned
	 * package is exactly the one published at {@code stage} in {@code scopeName}.
	 * Used by {@code AtlasScopedFetchOnMissRegistry} when a stage-specific registry
	 * misses its prefetched set.
	 * <p>
	 * The default implementation falls back to the stage-free {@link #getEPackage(String)};
	 * the remote client overrides it with the stage-explicit endpoint.
	 *
	 * @param nsUri     the package namespace URI
	 * @param scopeName the scope to query
	 * @param stage     the stage name (must not be null)
	 * @return the package at that scope+stage, or empty if absent
	 */
	default Optional<EPackage> getEPackageAtStage(String nsUri, String scopeName, String stage) {
		return getEPackage(nsUri);
	}

	/**
	 * List the packages available in a specific scope at a specific stage
	 * ({@code GET /{scopeName}/schema/stages/{stage}}, P6-6).
	 * <p>
	 * The default implementation falls back to the stage-free {@link #listPackages(String)};
	 * the remote client overrides it with the stage-explicit endpoint.
	 *
	 * @param scopeName the scope to enumerate
	 * @param stage     the stage name (must not be null)
	 * @return the package descriptors (possibly empty)
	 */
	default List<PackageDescriptor> listPackagesAtStage(String scopeName, String stage) {
		return listPackages(scopeName);
	}
}
