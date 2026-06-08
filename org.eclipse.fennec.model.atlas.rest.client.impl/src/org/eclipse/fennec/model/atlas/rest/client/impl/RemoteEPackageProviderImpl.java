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
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.WebTarget;

/**
 * REST mapping for the read-only EPackage endpoints (P2-3), fronted by the
 * in-memory cache (P2-5).
 * <p>
 * Maps (against the configured {@code view} stage, default {@code released}):
 * <ul>
 * <li>{@code listNsUris(scope)} → {@code GET /{scope}/schema/stages/{view}}
 * (not cached — it is a listing)</li>
 * <li>{@code getEPackage(nsUri)} / {@code ensureAvailable(nsUri)} → cache hit
 * (within TTL), else revalidate/fetch via
 * {@code GET /{scope}/schema/stages/{view}/content?nsUri=…} walking the resolved
 * scopes in order (first hit wins), caching the result</li>
 * <li>{@code refresh(nsUri)} → always re-contact the server, revalidating with
 * the stored ETag</li>
 * </ul>
 * The body of a content hit is decoded by an {@link EPackageDeserializer} (P2-4)
 * and stored in the cache with its {@code ETag}/{@code Last-Modified} validators.
 * On revalidation (P2-6) the stored ETag is sent as {@code If-None-Match}; a
 * {@code 304} keeps the cached value (and refreshes its TTL) without re-parsing.
 */
class RemoteEPackageProviderImpl implements RemoteEPackageProvider {

	/** The schema-registry path segment under a scope. */
	static final String SCHEMA = "schema";
	/** The {@code stages} path segment. */
	static final String STAGES = "stages";
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
		Objects.requireNonNull(scopeName, "scopeName");
		// Discovery uses the released/final-stage alias `GET /{scope}/schema`
		// (SchemaPackagesResource.listReleasedPackages → listInFinalStageForRegistry),
		// which walks the scope hierarchy and so also surfaces packages inherited from
		// parent scopes' final stages — each scope resolved against its OWN final stage,
		// so a child's `release` and a parent's `released` both work. The stage-explicit
		// `…/stages/{view}` listing does NOT inherit (single scope, single stage); switch
		// back to it here if/when per-stage discovery is needed.
		WebTarget listTarget = baseTarget.path(scopeName).path(SCHEMA);
		Response response = RestSupport.get(listTarget, MediaType.APPLICATION_JSON);
		try {
			if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
				return List.of();
			}
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, "listNsUris(" + scopeName + ")");
			}
			return parseNsUris(response.readEntity(String.class), scopeName);
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
	public Optional<EPackage> refresh(String nsUri) {
		Objects.requireNonNull(nsUri, "nsUri");
		if (!isPublishable(nsUri)) {
			return Optional.empty();
		}
		// Forced: always re-contact the server, but still revalidate with the stored ETag.
		return revalidateOrFetch(nsUri);
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
		WebTarget target = baseTarget.path(scope).path(SCHEMA).path(STAGES).path(configuration.getView())
				.path("content").queryParam("nsUri", nsUri);
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

	/** Extract nsURIs from an {@code ObjectMetadataContainer} JSON body. */
	private List<String> parseNsUris(String json, String scopeName) {
		JsonNode metadata = RestSupport.parse(json, "listNsUris(" + scopeName + ")").path("metadata");
		List<String> nsUris = new ArrayList<>();
		for (JsonNode entry : metadata) {
			JsonNode objectId = entry.get("objectId");
			if (objectId != null && !objectId.isNull()) {
				nsUris.add(decodeNsUri(objectId.asText()));
			}
		}
		return List.copyOf(nsUris);
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
