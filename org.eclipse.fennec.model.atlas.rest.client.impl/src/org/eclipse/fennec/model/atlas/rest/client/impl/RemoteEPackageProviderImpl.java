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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.PackageDescriptor;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.WebTarget;

/**
 * REST mapping for the read-only EPackage endpoints (P2-3), fronted by the
 * in-memory cache (P2-5).
 * <p>
 * Maps to the <b>stage-free final-stage</b> schema endpoints (P5-7 — no stage name is
 * embedded in any read URL; the server resolves the final stage and walks scope
 * inheritance):
 * <ul>
 * <li>{@code listNsUris(scope)} → {@code GET /{scope}/schema}
 * (not cached — it is a listing)</li>
 * <li>{@code getEPackage(nsUri)} / {@code ensureAvailable(nsUri)} → cache hit
 * (within TTL), else revalidate/fetch via
 * {@code GET /{scope}/schema/content?nsUri=…} walking the resolved
 * scopes in order (first hit wins), caching the result</li>
 * <li>{@code refresh(nsUri)} → always re-contact the server, revalidating with
 * the stored ETag</li>
 * <li>{@code resolve(nsUri)} → metadata-first: {@code GET
 * /{scope}/schema?nsUri=…} for the authoritative origin (owning
 * scope/registry/stage/version, resolved through inheritance), then content fetched
 * through that same entry scope's stage-free content endpoint (which walks the hierarchy
 * server-side, so a parent-owned package is served via the queried child) — the
 * metadata only labels the origin, so the caller need not guess where it lives</li>
 * </ul>
 * The body of a content hit is decoded by an {@link EPackageDeserializer} (P2-4)
 * and stored in the cache with its {@code ETag}/{@code Last-Modified} validators.
 * On revalidation (P2-6) the stored ETag is sent as {@code If-None-Match}; a
 * {@code 304} keeps the cached value (and refreshes its TTL) without re-parsing.
 */
class RemoteEPackageProviderImpl implements RemoteEPackageProvider {

	/** The schema-registry path segment under a scope. */
	static final String SCHEMA = "schema";
	/** EPackage content is requested as XMI; the EMF/XMI decode is P2-4. */
	static final String EPACKAGE_MEDIA_TYPE = "application/xmi";

	private final WebTarget baseTarget;
	private final ClientConfiguration configuration;
	private final EPackageDeserializer deserializer;
	private final Supplier<List<String>> scopeNamesSupplier;
	private final ClientCache<String, EPackage> cache;

	RemoteEPackageProviderImpl(WebTarget baseTarget, ClientConfiguration configuration,
			EPackageDeserializer deserializer, Supplier<List<String>> scopeNamesSupplier) {
		this(baseTarget, configuration, deserializer, scopeNamesSupplier,
				new ClientCache<>(configuration.getCacheMaxEntries(), configuration.getCacheTtlMs()));
	}

	RemoteEPackageProviderImpl(WebTarget baseTarget, ClientConfiguration configuration,
			EPackageDeserializer deserializer, Supplier<List<String>> scopeNamesSupplier,
			ClientCache<String, EPackage> cache) {
		this.baseTarget = Objects.requireNonNull(baseTarget, "baseTarget");
		this.configuration = Objects.requireNonNull(configuration, "configuration");
		this.deserializer = Objects.requireNonNull(deserializer, "deserializer");
		this.scopeNamesSupplier = Objects.requireNonNull(scopeNamesSupplier, "scopeNamesSupplier");
		this.cache = Objects.requireNonNull(cache, "cache");
	}

	/** The nsURIs currently held in the cache — used by the drift watcher (P2-7). */
	java.util.Set<String> cachedNsUris() {
		return cache.keys();
	}

	@Override
	public List<String> listNsUris(String scopeName) {
		return listPackages(scopeName).stream().map(PackageDescriptor::nsUri).toList();
	}

	@Override
	public List<PackageDescriptor> listPackages(String scopeName) {
		Objects.requireNonNull(scopeName, "scopeName");
		// Discovery uses the stage-free final-stage alias `GET /{scope}/schema`
		// (SchemaPackagesResource.listReleasedPackages → listInFinalStageForRegistry),
		// which walks the scope hierarchy and so also surfaces packages inherited from
		// parent scopes' final stages — each scope resolved against its OWN final stage,
		// so a child's `release` and a parent's `released` both work. Each entry's
		// ObjectMetadata carries the owning scope/stage/version, so a single listing call
		// yields full provenance (no per-package metadata round-trip — see listPackages doc).
		WebTarget listTarget = baseTarget.path(scopeName).path(SCHEMA);
		Response response = RestSupport.get(listTarget, MediaType.APPLICATION_JSON);
		try {
			if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
				return List.of();
			}
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, "listPackages(" + scopeName + ")");
			}
			return parseDescriptors(response.readEntity(String.class), scopeName);
		} finally {
			response.close();
		}
	}

	@Override
	public Optional<EPackage> getEPackage(String nsUri) {
		Objects.requireNonNull(nsUri, "nsUri");
		// Allow/deny gate (P2-9): a denied / not-allowed nsURI is never returned,
		// regardless of cache state — checked before the cache.
		if (!isPublishable(nsUri)) {
			return Optional.empty();
		}
		Optional<EPackage> cached = cache.get(nsUri);
		if (cached.isPresent()) {
			return cached;
		}
		// Absent or past TTL: revalidate against the server (conditional GET if we
		// still hold the expired entry's ETag), else a full fetch.
		return revalidateOrFetch(nsUri);
	}

	@Override
	public Optional<EPackage> ensureAvailable(String nsUri) {
		// Local-first, same as getEPackage; named for the warm-up / registry-delegate intent.
		return getEPackage(nsUri);
	}

	@Override
	public Optional<ResolvedEPackage> resolve(String nsUri) {
		Objects.requireNonNull(nsUri, "nsUri");
		if (!isPublishable(nsUri)) {
			return Optional.empty();
		}
		// Metadata-first: ask an entry scope for the authoritative origin (the server
		// resolves it through scope inheritance, so the returned scope is the OWNING
		// scope — possibly a parent of the queried one). Entry scopes are gated by
		// scope.allow.list / default.scope, so nothing outside an allowed scope's
		// visibility can be reached. First entry scope that can see it wins.
		for (String entryScope : resolveScopes()) {
			Optional<PackageMetadata> metadata = fetchMetadata(entryScope, nsUri);
			if (metadata.isEmpty()) {
				continue;
			}
			PackageMetadata md = metadata.get();
			if (md.scope() == null || md.stage() == null) {
				continue; // malformed metadata — cannot describe the origin
			}
			// Fetch the content through the ENTRY scope's stage-free final-stage endpoint, not the
			// owning scope/stage the metadata reports. A package inherited from a parent (e.g. the
			// root `atlas` scope) is not reachable by a direct `/{owningScope}/schema/...`
			// request — the parent's schema registry is exposed under a different name and
			// only a child scope walks the hierarchy. The entry scope's content endpoint
			// resolves inheritance server-side (this is the same path getEPackage uses), so
			// it serves the parent-owned content; the metadata is used only to LABEL the
			// authoritative origin (owning scope/registry/stage/version) on the result.
			Optional<EPackage> content = fetchResolvedContent(entryScope, nsUri);
			if (content.isEmpty()) {
				continue;
			}
			return Optional.of(new ResolvedEPackage(content.get(), nsUri, md.scope(), md.registry(), md.stage(),
					md.version()));
		}
		return Optional.empty();
	}

	@Override
	public Optional<EPackage> refresh(String nsUri) {
		Objects.requireNonNull(nsUri, "nsUri");
		if (!isPublishable(nsUri)) {
			return Optional.empty();
		}
		// Forced: always re-contact the server, but still revalidate with the stored ETag.
		return revalidateOrFetch(nsUri);
	}

	@Override
	public Optional<EPackage> getEPackageAtStage(String nsUri, String scopeName, String stage) {
		Objects.requireNonNull(nsUri, "nsUri");
		Objects.requireNonNull(scopeName, "scopeName");
		Objects.requireNonNull(stage, "stage");
		// Stage-explicit content: GET /{scope}/schema/stages/{stage}/content?nsUri=…
		// No caching here — the caller (AtlasScopedFetchOnMissRegistry) owns its own cache.
		WebTarget target = baseTarget.path(scopeName).path(SCHEMA).path("stages").path(stage).path("content")
				.queryParam("nsUri", nsUri);
		Optional<ContentResult> result = fetchContent(target, nsUri, null);
		if (result.isEmpty() || result.get().notModified()) {
			return Optional.empty();
		}
		return Optional.of(result.get().fetched().ePackage());
	}

	@Override
	public List<PackageDescriptor> listPackagesAtStage(String scopeName, String stage) {
		Objects.requireNonNull(scopeName, "scopeName");
		Objects.requireNonNull(stage, "stage");
		// Stage-explicit listing: GET /{scope}/schema/stages/{stage}
		WebTarget listTarget = baseTarget.path(scopeName).path(SCHEMA).path("stages").path(stage);
		Response response = RestSupport.get(listTarget, MediaType.APPLICATION_JSON);
		try {
			if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
				return List.of();
			}
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, "listPackagesAtStage(" + scopeName + ", " + stage + ")");
			}
			return parseDescriptors(response.readEntity(String.class), scopeName);
		} finally {
			response.close();
		}
	}

	/**
	 * The nsURI allow/deny gate (P2-9, exact matches): a denied nsURI is never
	 * publishable; with a non-empty allow-list only listed nsURIs are; both empty
	 * = everything allowed. Applied uniformly to direct look-up and to the drift
	 * watcher (which re-fetches via {@link #refresh(String)}).
	 */
	private boolean isPublishable(String nsUri) {
		if (configuration.getNsUriDenyList().contains(nsUri)) {
			return false;
		}
		List<String> allowList = configuration.getNsUriAllowList();
		return allowList.isEmpty() || allowList.contains(nsUri);
	}

	/**
	 * Re-contact the server for {@code nsUri}, sending {@code If-None-Match} with the
	 * stored ETag if one is cached (P2-6). Walk the resolved scopes, first hit wins:
	 * <ul>
	 * <li>{@code 304} → keep the cached value, refresh its TTL, no parsing;</li>
	 * <li>{@code 200} → replace the cache entry with the new payload and ETag;</li>
	 * <li>all scopes miss → drop any stale entry and report absent.</li>
	 * </ul>
	 */
	private Optional<EPackage> revalidateOrFetch(String nsUri) {
		Optional<ClientCache.Entry<EPackage>> existing = cache.lookup(nsUri);
		String ifNoneMatch = existing.map(ClientCache.Entry::etag).orElse(null);
		for (String scope : resolveScopes()) {
			Optional<ContentResult> result = fetchContent(scope, nsUri, ifNoneMatch);
			if (result.isEmpty()) {
				continue;
			}
			ContentResult content = result.get();
			if (content.notModified()) {
				ClientCache.Entry<EPackage> entry = existing.orElseThrow();
				// Keep the cached value; re-put to refresh its TTL.
				cache.put(nsUri, entry.value(), entry.etag(), entry.lastModified());
				return Optional.of(entry.value());
			}
			FetchedPackage fetched = content.fetched();
			cache.put(nsUri, fetched.ePackage(), fetched.etag(), fetched.lastModified());
			return Optional.of(fetched.ePackage());
		}
		// No scope holds it any more: drop a now-stale entry.
		cache.invalidate(nsUri);
		return Optional.empty();
	}

	/**
	 * Scope resolution for anonymous lookups: the configured {@code scope.allow.list}
	 * in order, else the single {@code default.scope}, else every scope the server
	 * exposes.
	 */
	private List<String> resolveScopes() {
		List<String> allowList = configuration.getScopeAllowList();
		if (!allowList.isEmpty()) {
			return allowList;
		}
		if (configuration.getDefaultScope() != null) {
			return List.of(configuration.getDefaultScope());
		}
		return scopeNamesSupplier.get();
	}

	/**
	 * Fetch one package's content from a single scope, conditionally on
	 * {@code ifNoneMatch} when non-null. Returns:
	 * <ul>
	 * <li>{@code notModified} — server answered {@code 304} (no body read, no parse);</li>
	 * <li>a {@code fetched} package — {@code 200} (deserialized, with its validators);</li>
	 * <li>empty — a miss ({@code 204}/{@code 404}/other), so the caller tries the next scope.</li>
	 * </ul>
	 */
	private Optional<ContentResult> fetchContent(String scope, String nsUri, String ifNoneMatch) {
		// Stage-free final-stage content (P5-7): GET /{scope}/schema/content?nsUri=… — the server
		// resolves the final stage and walks scope inheritance, so no stage name is embedded here.
		WebTarget target = baseTarget.path(scope).path(SCHEMA).path("content").queryParam("nsUri", nsUri);
		return fetchContent(target, nsUri, ifNoneMatch);
	}

	/**
	 * Fetch one package's content from a pre-built target, conditionally on {@code ifNoneMatch}.
	 * Shared by stage-free and stage-explicit paths.
	 */
	private Optional<ContentResult> fetchContent(WebTarget target, String nsUri, String ifNoneMatch) {
		Response response = RestSupport.get(target, EPACKAGE_MEDIA_TYPE, ifNoneMatch);
		try {
			if (RestSupport.isNotModified(response)) {
				return Optional.of(ContentResult.ofNotModified());
			}
			if (!RestSupport.isSuccess(response) || !response.hasEntity()) {
				return Optional.empty();
			}
			byte[] body = response.readEntity(byte[].class);
			MediaType contentType = response.getMediaType();
			// Build the type string from the parts, not toString(): the latter routes
			// through RuntimeDelegate, which need not be present in a plain-Java client.
			String mediaType = contentType != null ? contentType.getType() + "/" + contentType.getSubtype()
					: EPACKAGE_MEDIA_TYPE;
			EPackage ePackage = deserializer.deserialize(new ByteArrayInputStream(body), nsUri, mediaType);
			FetchedPackage fetched = new FetchedPackage(ePackage, response.getHeaderString(HttpHeaders.ETAG),
					response.getHeaderString(HttpHeaders.LAST_MODIFIED));
			return Optional.of(ContentResult.of(fetched));
		} finally {
			response.close();
		}
	}

	/**
	 * Fetch the authoritative metadata for {@code nsUri} as seen from {@code entryScope}
	 * ({@code GET /{scope}/schema?nsUri=…}, the stage-free final-stage listing, which
	 * respects scope inheritance — P5-7). Empty when the package is not visible from this
	 * entry scope ({@code 204}/{@code 404}/any non-success), so the caller tries the next scope.
	 */
	private Optional<PackageMetadata> fetchMetadata(String entryScope, String nsUri) {
		WebTarget target = baseTarget.path(entryScope).path(SCHEMA).queryParam("nsUri", nsUri);
		Response response = RestSupport.get(target, MediaType.APPLICATION_JSON);
		try {
			// 204 (not visible from this scope) or any non-success → a miss; the caller
			// tries the next entry scope rather than failing the whole resolution.
			if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()
					|| !RestSupport.isSuccess(response)) {
				return Optional.empty();
			}
			JsonNode node = RestSupport.parse(response.readEntity(String.class), "resolve(" + nsUri + ")");
			return Optional.of(new PackageMetadata(text(node, "scope"), text(node, "registry"), text(node, "stage"),
					text(node, "version")));
		} finally {
			response.close();
		}
	}

	/**
	 * Content fetch for {@link #resolve} from the given (entry) scope's stage-free
	 * final-stage endpoint — which resolves scope inheritance server-side — reusing the
	 * cache and its conditional-GET path (P2-5/P2-6).
	 */
	private Optional<EPackage> fetchResolvedContent(String scope, String nsUri) {
		Optional<ClientCache.Entry<EPackage>> existing = cache.lookup(nsUri);
		String ifNoneMatch = existing.map(ClientCache.Entry::etag).orElse(null);
		WebTarget target = baseTarget.path(scope).path(SCHEMA).path("content").queryParam("nsUri", nsUri);
		Optional<ContentResult> result = fetchContent(target, nsUri, ifNoneMatch);
		if (result.isEmpty()) {
			return Optional.empty();
		}
		ContentResult content = result.get();
		if (content.notModified()) {
			ClientCache.Entry<EPackage> entry = existing.orElseThrow();
			cache.put(nsUri, entry.value(), entry.etag(), entry.lastModified());
			return Optional.of(entry.value());
		}
		FetchedPackage fetched = content.fetched();
		cache.put(nsUri, fetched.ePackage(), fetched.etag(), fetched.lastModified());
		return Optional.of(fetched.ePackage());
	}

	private static String text(JsonNode node, String field) {
		JsonNode value = node.get(field);
		return value == null || value.isNull() ? null : value.asText();
	}

	/** The origin fields of a package's {@code ObjectMetadata} needed to locate and stamp it. */
	private record PackageMetadata(String scope, String registry, String stage, String version) {
	}

	/** A freshly fetched package together with its HTTP validators. */
	private record FetchedPackage(EPackage ePackage, String etag, String lastModified) {
	}

	/** Outcome of a conditional content GET: either {@code 304} or a fetched package. */
	private record ContentResult(boolean notModified, FetchedPackage fetched) {
		static ContentResult ofNotModified() {
			return new ContentResult(true, null);
		}

		static ContentResult of(FetchedPackage fetched) {
			return new ContentResult(false, fetched);
		}
	}

	/**
	 * Extract per-package descriptors from an {@code ObjectMetadataContainer} JSON body —
	 * each entry's {@code objectId} (decoded to the nsURI) plus the owning
	 * {@code scope}/{@code stage}/{@code version} the listing already carries.
	 */
	private List<PackageDescriptor> parseDescriptors(String json, String scopeName) {
		JsonNode metadata = RestSupport.parse(json, "listPackages(" + scopeName + ")").path("metadata");
		List<PackageDescriptor> descriptors = new ArrayList<>();
		for (JsonNode entry : metadata) {
			JsonNode objectId = entry.get("objectId");
			if (objectId != null && !objectId.isNull()) {
				descriptors.add(new PackageDescriptor(decodeNsUri(objectId.asText()), text(entry, "scope"),
						text(entry, "stage"), text(entry, "version")));
			}
		}
		return List.copyOf(descriptors);
	}

	/**
	 * For schema packages the server's {@code objectId} is the Base64-URL encoding
	 * of the nsURI (see {@code SchemaPackagesResource.encodePackageNsURI}); decode
	 * it back to the real nsURI.
	 */
	private static String decodeNsUri(String objectId) {
		return new String(Base64.getUrlDecoder().decode(objectId));
	}
}
