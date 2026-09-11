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
	 * <b>Answer at that stage, or do not answer.</b> Several versions of one nsURI can be
	 * live at once, one per stage, so returning the stage-free (final-stage) package here
	 * would be a wrong answer presented as a right one — the caller cannot tell the two
	 * apart. An implementation that cannot resolve per stage must return
	 * {@link Optional#empty()} if the absence is a fact, or throw
	 * {@link UnsupportedOperationException} if it simply does not model stages. Callers
	 * treat empty as "not at this stage" and may fall back deliberately.
	 *
	 * @param nsUri     the package namespace URI
	 * @param scopeName the scope to query
	 * @param stage     the stage name (must not be null)
	 * @return the package at that scope+stage, or empty if absent
	 */
	Optional<EPackage> getEPackageAtStage(String nsUri, String scopeName, String stage);

	/**
	 * As {@link #getEPackageAtStage(String, String, String)}, but also reporting where the package
	 * came from: the scope, stage, version and model fingerprint the Atlas states for it (#273).
	 * <p>
	 * The stage-explicit content endpoint is the only path that reaches a package at a non-final
	 * stage, so without this a client can fetch one of several live versions of an nsURI and have
	 * no way to say which. The fingerprint is the server's statement about the content; a client
	 * that computes fingerprints itself keeps its own value authoritative and uses this one as a
	 * cross-check, never as the key.
	 * <p>
	 * Where the server reports no origin — an Atlas older than #273 — the returned scope and stage
	 * are the ones that were asked for, and version and fingerprint are {@code null}. The registry
	 * is {@code null}: this endpoint serves the scope's schema registry by definition and does not
	 * name it.
	 *
	 * @param nsUri     the package namespace URI
	 * @param scopeName the scope to query
	 * @param stage     the stage name (must not be null)
	 * @return the resolved package with its origin, or empty if absent at that stage
	 */
	default Optional<ResolvedEPackage> resolveAtStage(String nsUri, String scopeName, String stage) {
		return resolveAtStage(nsUri, scopeName, stage, null);
	}

	/**
	 * As {@link #resolveAtStage(String, String, String)}, but pinned: {@code fingerprint} asserts
	 * <em>which model version</em> the caller expects at that location (#274).
	 * <p>
	 * Addressing is unchanged — the package is still found by {@code (scope, stage, nsUri)}. The
	 * fingerprint is a precondition on what is found there, which matters because several versions
	 * of one nsURI can be live at once and a transition can move them between the moment a client
	 * lists a version and the moment it fetches one. A pinned read therefore either returns the
	 * version named or fails; it never returns a different one.
	 *
	 * @param nsUri       the package namespace URI
	 * @param scopeName   the scope to query
	 * @param stage       the stage name (must not be null)
	 * @param fingerprint the expected model fingerprint, or {@code null} for no precondition
	 * @return the resolved package with its origin, or empty if absent at that stage
	 * @throws VersionMismatchException if a different model version sits at that location
	 */
	Optional<ResolvedEPackage> resolveAtStage(String nsUri, String scopeName, String stage, String fingerprint);

	/**
	 * List the packages available in a specific scope at a specific stage
	 * ({@code GET /{scopeName}/schema/stages/{stage}}, P6-6).
	 * <p>
	 * <b>Enumerate that stage, or do not answer.</b> As for
	 * {@link #getEPackageAtStage(String, String, String)}, falling back to the stage-free
	 * listing would report the final stage's contents as the stage's own. An implementation
	 * that does not model stages must return an empty list or throw
	 * {@link UnsupportedOperationException}.
	 *
	 * @param scopeName the scope to enumerate
	 * @param stage     the stage name (must not be null)
	 * @return the package descriptors (possibly empty)
	 */
	List<PackageDescriptor> listPackagesAtStage(String scopeName, String stage);
}
