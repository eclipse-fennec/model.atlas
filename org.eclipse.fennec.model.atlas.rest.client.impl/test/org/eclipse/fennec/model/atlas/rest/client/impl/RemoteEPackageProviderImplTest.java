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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;
import org.eclipse.fennec.model.atlas.rest.client.api.NotFoundException;
import org.eclipse.fennec.model.atlas.rest.client.api.PackageDescriptor;
import org.eclipse.fennec.model.atlas.rest.client.api.TransportException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * P2-3 — REST mapping for the EPackage endpoints. The {@link WebTarget} fluent
 * chain is mocked (each {@code path}/{@code queryParam} returns the same target)
 * so the tests assert the URL shape and status handling without a server.
 */
class RemoteEPackageProviderImplTest {

	private static final URI BASE = URI.create("https://atlas.example.org/atlas");

	private final WebTarget target = mock(WebTarget.class);
	private final Invocation.Builder request = mock(Invocation.Builder.class);

	RemoteEPackageProviderImplTest() {
		when(target.path(anyString())).thenReturn(target);
		when(target.queryParam(anyString(), any())).thenReturn(target);
		when(target.getUri()).thenReturn(BASE);
		when(target.request(anyString())).thenReturn(request);
		when(request.header(anyString(), any())).thenReturn(request);
	}

	private RemoteEPackageProviderImpl provider(ClientConfiguration config) {
		return new RemoteEPackageProviderImpl(target, config, fakeDeserializer(), () -> List.of("fallbackScope"));
	}

	private static ClientConfiguration config(String... allowList) {
		return ClientConfiguration.builder().baseUri(BASE).scopeAllowList(List.of(allowList)).build();
	}

	/** Returns a fresh EPackage whose nsURI is the one requested. */
	private static EPackageDeserializer fakeDeserializer() {
		return (content, nsUri, mediaType) -> {
			EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
			pkg.setNsURI(nsUri);
			return pkg;
		};
	}

	private static Response status(int code, Response.Status statusInfo) {
		Response r = mock(Response.class);
		when(r.getStatus()).thenReturn(code);
		when(r.getStatusInfo()).thenReturn(statusInfo);
		return r;
	}

	private static Response jsonOk(String body) {
		Response r = status(200, Response.Status.OK);
		when(r.readEntity(String.class)).thenReturn(body);
		return r;
	}

	private static Response contentOk(byte[] body) {
		return contentOk(body, null);
	}

	private static Response contentOk(byte[] body, String etag) {
		Response r = status(200, Response.Status.OK);
		when(r.hasEntity()).thenReturn(true);
		when(r.readEntity(byte[].class)).thenReturn(body);
		// new MediaType(...) avoids RuntimeDelegate (no JAX-RS runtime on the test classpath).
		when(r.getMediaType()).thenReturn(new MediaType("application", "xmi"));
		if (etag != null) {
			when(r.getHeaderString("ETag")).thenReturn(etag);
		}
		return r;
	}

	private static Response noContent() {
		Response r = status(204, Response.Status.NO_CONTENT);
		when(r.hasEntity()).thenReturn(false);
		return r;
	}

	private static Response notModified() {
		return status(304, Response.Status.NOT_MODIFIED);
	}

	private static String base64Url(String value) {
		return Base64.getUrlEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	// ---- listNsUris -------------------------------------------------------

	@Test
	void listNsUris_getsSchemaPath_andDecodesObjectIds() {
		String ns1 = "https://eclipse.dev/fennec/jena/cocl/1.0";
		String ns2 = "https://eclipse.dev/fennec/jena/datagen/1.0";
		String json = "{\"metadata\":[{\"objectId\":\"" + base64Url(ns1) + "\"},{\"objectId\":\"" + base64Url(ns2)
				+ "\"}]}";
		Response response = jsonOk(json);
		when(request.get()).thenReturn(response);

		List<String> result = provider(config()).listNsUris("jena");

		assertEquals(List.of(ns1, ns2), result);
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		// Discovery hits the released/final-stage alias `/{scope}/schema` (hierarchy-walking),
		// not the stage-explicit `/{scope}/schema/stages/{view}` listing.
		assertEquals(List.of("jena", "schema"), paths.getAllValues());
	}

	@Test
	void listPackages_parsesOriginMetadataFromTheListing() {
		// Option A: the listing carries each package's owning scope/stage/version, so EAGER can
		// publish accurate provenance without a per-package metadata round-trip.
		String ns = "https://eclipse.dev/fennec/jena/cocl/1.0";
		String json = "{\"metadata\":[{\"objectId\":\"" + base64Url(ns)
				+ "\",\"scope\":\"atlas\",\"stage\":\"released\",\"version\":\"1.2\","
				+ "\"fingerprint\":\"fp1:14466a0b5de879a6\"}]}";
		Response response = jsonOk(json);
		when(request.get()).thenReturn(response);

		List<PackageDescriptor> result = provider(config()).listPackages("jena");

		assertEquals(1, result.size());
		PackageDescriptor d = result.get(0);
		assertEquals(ns, d.nsUri());
		assertEquals("atlas", d.scope());
		assertEquals("released", d.stage());
		assertEquals("1.2", d.version());
		assertEquals("fp1:14466a0b5de879a6", d.fingerprint(),
				"the listing's fingerprint travels into the descriptor");
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals(List.of("jena", "schema"), paths.getAllValues());
	}

	@Test
	void listNsUris_prefersNsUriProperty_overObjectIdDecoding() {
		// F8 servers: objectId is an opaque UUID, the nsURI travels in the nsUri
		// metadata property — decoding the UUID would yield garbage, so the property wins.
		String ns = "https://eclipse.dev/fennec/jena/cocl/1.0";
		String json = "{\"metadata\":[{\"objectId\":\"550e8400-e29b-41d4-a716-446655440000\","
				+ "\"properties\":{\"nsUri\":\"" + ns + "\"}}]}";
		Response response = jsonOk(json);
		when(request.get()).thenReturn(response);

		assertEquals(List.of(ns), provider(config()).listNsUris("jena"));
	}

	@Test
	void listNsUris_readsNsUriProperty_fromEntryArrayShape() {
		// The EMap may also serialize as an array of key/value entries.
		String ns = "https://eclipse.dev/fennec/jena/cocl/1.0";
		String json = "{\"metadata\":[{\"objectId\":\"550e8400-e29b-41d4-a716-446655440000\","
				+ "\"properties\":[{\"key\":\"nsUri\",\"value\":\"" + ns + "\"}]}]}";
		Response response = jsonOk(json);
		when(request.get()).thenReturn(response);

		assertEquals(List.of(ns), provider(config()).listNsUris("jena"));
	}

	@Test
	void listNsUris_skipsEntriesWithoutResolvableNsUri() {
		// A pre-F8 git-backed id (scope/stage/repoPath) is not Base64-URL and has no
		// property — the entry is skipped instead of poisoning the whole listing.
		String ns = "https://eclipse.dev/fennec/jena/cocl/1.0";
		String json = "{\"metadata\":[{\"objectId\":\"jena/release/models/cocl.ecore\"},"
				+ "{\"objectId\":\"" + base64Url(ns) + "\"}]}";
		Response response = jsonOk(json);
		when(request.get()).thenReturn(response);

		assertEquals(List.of(ns), provider(config()).listNsUris("jena"));
	}

	@Test
	void listNsUris_noContent_returnsEmpty() {
		Response response = noContent();
		when(request.get()).thenReturn(response);
		assertTrue(provider(config()).listNsUris("jena").isEmpty());
	}

	@Test
	void listNsUris_errorStatus_throws() {
		Response response = status(500, Response.Status.INTERNAL_SERVER_ERROR);
		when(request.get()).thenReturn(response);
		assertThrows(ModelAtlasClientException.class, () -> provider(config()).listNsUris("jena"));
	}

	@Test
	void listNsUris_404_throwsNotFound() {
		Response response = status(404, Response.Status.NOT_FOUND);
		when(request.get()).thenReturn(response);
		assertThrows(NotFoundException.class, () -> provider(config()).listNsUris("ghost"));
	}

	@Test
	void transportFault_mapsToTransportException() {
		when(request.get()).thenThrow(new ProcessingException("connection refused"));
		assertThrows(TransportException.class, () -> provider(config()).listNsUris("jena"));
	}

	// ---- getEPackage ------------------------------------------------------

	@Test
	void getEPackage_hit_requestsContentAsXmi_andDeserializes() {
		Response response = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(response);

		Optional<EPackage> pkg = provider(config("jena")).getEPackage("urn:ns:cocl");

		assertTrue(pkg.isPresent());
		assertEquals("urn:ns:cocl", pkg.get().getNsURI());
		// stage-free content path (P5-7) + nsUri query param + XMI accept
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals(List.of("jena", "schema", "content"), paths.getAllValues());
		verify(target).queryParam("nsUri", "urn:ns:cocl");
		verify(target).request(RemoteEPackageProviderImpl.EPACKAGE_MEDIA_TYPE);
	}

	@Test
	void getEPackage_walksScopes_firstHitWins() {
		// scope "a" misses (204), scope "b" hits (200).
		Response miss = noContent();
		Response hit = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(miss, hit);

		Optional<EPackage> pkg = provider(config("a", "b")).getEPackage("urn:ns:x");

		assertTrue(pkg.isPresent());
		assertEquals("urn:ns:x", pkg.get().getNsURI());
	}

	@Test
	void getEPackage_allScopesMiss_returnsEmpty() {
		Response miss1 = noContent();
		Response miss2 = noContent();
		when(request.get()).thenReturn(miss1, miss2);
		assertFalse(provider(config("a", "b")).getEPackage("urn:ns:missing").isPresent());
	}

	@Test
	void ensureAvailable_delegatesToGetEPackage() {
		Response response = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(response);
		assertTrue(provider(config("jena")).ensureAvailable("urn:ns:y").isPresent());
	}

	@Test
	void getEPackage_emptyAllowList_usesScopeNamesSupplier() {
		Response response = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(response);

		// allow list empty + no default scope → falls back to the supplier ("fallbackScope")
		Optional<EPackage> pkg = provider(config()).getEPackage("urn:ns:z");

		assertTrue(pkg.isPresent());
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertSame("fallbackScope", paths.getAllValues().get(0));
	}

	@Test
	void getEPackage_defaultScope_usedWhenAllowListEmpty() {
		Response response = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(response);
		ClientConfiguration cfg = ClientConfiguration.builder().baseUri(BASE).defaultScope("dflt").build();

		assertTrue(provider(cfg).getEPackage("urn:ns:d").isPresent());
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals("dflt", paths.getAllValues().get(0));
		verify(target).queryParam(eq("nsUri"), eq("urn:ns:d"));
	}

	@Test
	void getEPackage_usesStageFreeContentUrl() {
		// P5-7: the content read is stage-free — no stage segment in the URL.
		Response response = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(response);
		ClientConfiguration cfg = ClientConfiguration.builder().baseUri(BASE).scopeAllowList(List.of("jena"))
				.build();

		assertTrue(provider(cfg).getEPackage("urn:ns:a").isPresent());
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals(List.of("jena", "schema", "content"), paths.getAllValues());
	}

	@Test
	void listNsUris_usesStageFreeListingUrl() {
		// Discovery is the hierarchy-walking final-stage alias `/{scope}/schema`; it takes
		// no stage path segment.
		Response response = jsonOk("{\"metadata\":[]}");
		when(request.get()).thenReturn(response);
		ClientConfiguration cfg = ClientConfiguration.builder().baseUri(BASE).build();

		provider(cfg).listNsUris("jena");

		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals(List.of("jena", "schema"), paths.getAllValues());
	}

	// ---- resolve (metadata-first) -----------------------------------------

	private static String metadataJson(String scope, String registry, String stage, String version) {
		return "{\"scope\":\"" + scope + "\",\"registry\":\"" + registry + "\",\"stage\":\"" + stage
				+ "\",\"version\":\"" + version + "\",\"fingerprint\":\"fp1:14466a0b5de879a6\"}";
	}

	@Test
	void resolve_labelsOriginFromMetadata_butFetchesContentViaEntryScope() {
		// Metadata query (json) reports the owning scope/registry/stage/version of an
		// inherited package (owner "atlas", queried via "jena").
		Response metadata = jsonOk(metadataJson("atlas", "schema", "released", "1.2"));
		Response content = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(metadata, content);

		var resolved = provider(config("jena")).resolve("urn:ns:inherited");

		assertTrue(resolved.isPresent());
		assertEquals("urn:ns:inherited", resolved.get().getNsUri());
		// The origin is LABELLED from the metadata — the owner, not the queried entry scope.
		assertEquals("atlas", resolved.get().getScope());
		assertEquals("schema", resolved.get().getRegistry());
		assertEquals("released", resolved.get().getStage());
		assertEquals("1.2", resolved.get().getVersion());
		assertEquals("fp1:14466a0b5de879a6", resolved.get().getFingerprint(),
				"the server-reported fingerprint travels into the ResolvedEPackage");
		assertEquals("urn:ns:inherited", resolved.get().getEPackage().getNsURI());

		// First the stage-free metadata URL (/{scope}/schema?nsUri=, no /content), then the
		// stage-free content from the ENTRY scope (/{scope}/schema/content) — NOT a direct
		// /{owningScope}/... request: a parent's schema registry is exposed under a different
		// name, so parent-owned content is only reachable through the queried child, which the
		// server resolves by inheritance (P5-7: no stage segment in either URL).
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals(List.of("jena", "schema", "jena", "schema", "content"), paths.getAllValues());
	}

	@Test
	void resolve_walksEntryScopes_untilMetadataVisible() {
		// entry "a" cannot see it (204); entry "b" can, owner is "b"; then content.
		Response miss = noContent();
		Response metadata = jsonOk(metadataJson("b", "schema", "released", "1.0"));
		Response content = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(miss, metadata, content);

		var resolved = provider(config("a", "b")).resolve("urn:ns:x");

		assertTrue(resolved.isPresent());
		assertEquals("b", resolved.get().getScope());
	}

	@Test
	void resolve_notVisibleFromAnyAllowedScope_returnsEmpty() {
		// Build the mocks first; constructing them inside thenReturn(...) nests stubbing.
		Response miss1 = noContent();
		Response miss2 = noContent();
		when(request.get()).thenReturn(miss1, miss2);
		assertFalse(provider(config("a", "b")).resolve("urn:ns:nope").isPresent());
	}

	@Test
	void resolve_deniedNsUri_returnsEmpty_withoutFetch() {
		ClientConfiguration cfg = ClientConfiguration.builder().baseUri(BASE).scopeAllowList(List.of("jena"))
				.nsUriDenyList(List.of("urn:ns:denied")).build();
		assertFalse(provider(cfg).resolve("urn:ns:denied").isPresent());
		verify(request, org.mockito.Mockito.never()).get();
	}

	// ---- caching (P2-5) ---------------------------------------------------

	@Test
	void getEPackage_cachesResult_secondCallSkipsHttp() {
		Response response = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(response);
		RemoteEPackageProviderImpl provider = provider(config("jena"));

		assertTrue(provider.getEPackage("urn:ns:cache").isPresent());
		assertTrue(provider.getEPackage("urn:ns:cache").isPresent());

		// One scope, one content GET; the second lookup is served from cache.
		verify(request, org.mockito.Mockito.times(1)).get();
	}

	@Test
	void refresh_invalidatesAndRefetches() {
		Response response = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(response);
		RemoteEPackageProviderImpl provider = provider(config("jena"));

		assertTrue(provider.getEPackage("urn:ns:r").isPresent()); // fetch + cache
		assertTrue(provider.refresh("urn:ns:r").isPresent()); // forced re-fetch

		verify(request, org.mockito.Mockito.times(2)).get();
	}

	@Test
	void getEPackage_missesAreNotCached() {
		Response miss = noContent();
		when(request.get()).thenReturn(miss);
		RemoteEPackageProviderImpl provider = provider(config("jena"));

		assertFalse(provider.getEPackage("urn:ns:none").isPresent());
		assertFalse(provider.getEPackage("urn:ns:none").isPresent());

		// A miss is not cached, so both calls hit the server.
		verify(request, org.mockito.Mockito.times(2)).get();
	}

	// ---- conditional GET / If-None-Match (P2-6) ---------------------------

	/** Counts how many times the body is parsed, so we can assert "no parse on 304". */
	private static final class CountingDeserializer implements EPackageDeserializer {
		final AtomicInteger calls = new AtomicInteger();

		@Override
		public EPackage deserialize(java.io.InputStream content, String nsUri, String mediaType) {
			calls.incrementAndGet();
			EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
			pkg.setNsURI(nsUri);
			return pkg;
		}
	}

	@Test
	void refresh_sendsIfNoneMatch_and304KeepsCachedValueWithoutParsing() {
		CountingDeserializer deserializer = new CountingDeserializer();
		Response first = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8), "\"v1\"");
		Response revalidated = notModified();
		when(request.get()).thenReturn(first, revalidated);
		RemoteEPackageProviderImpl provider = new RemoteEPackageProviderImpl(target, config("jena"), deserializer,
				() -> List.of());

		EPackage initial = provider.getEPackage("urn:ns:c").orElseThrow(); // 200 -> cache (etag v1)
		EPackage refreshed = provider.refresh("urn:ns:c").orElseThrow(); // conditional -> 304

		verify(request).header("If-None-Match", "\"v1\"");
		assertSame(initial, refreshed, "304 must return the same cached package instance");
		assertEquals(1, deserializer.calls.get(), "304 must not re-parse the body");
	}

	@Test
	void refresh_200_replacesEntryAndReparses() {
		CountingDeserializer deserializer = new CountingDeserializer();
		Response first = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8), "\"v1\"");
		Response changed = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8), "\"v2\"");
		when(request.get()).thenReturn(first, changed);
		RemoteEPackageProviderImpl provider = new RemoteEPackageProviderImpl(target, config("jena"), deserializer,
				() -> List.of());

		EPackage initial = provider.getEPackage("urn:ns:c").orElseThrow();
		EPackage refreshed = provider.refresh("urn:ns:c").orElseThrow();

		verify(request).header("If-None-Match", "\"v1\"");
		assertEquals(2, deserializer.calls.get(), "200 must re-parse the new body");
		org.junit.jupiter.api.Assertions.assertNotSame(initial, refreshed, "200 must replace the cached package");
	}

	@Test
	void getEPackage_postTtlRevalidation_304_keepsValueAndBumpsTtl() {
		CountingDeserializer deserializer = new CountingDeserializer();
		Response first = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8), "\"v1\"");
		Response revalidated = notModified();
		when(request.get()).thenReturn(first, revalidated);

		AtomicLong now = new AtomicLong(0);
		ClientCache<String, EPackage> cache = new ClientCache<>(10, 1_000, now::get);
		RemoteEPackageProviderImpl provider = new RemoteEPackageProviderImpl(target, config("jena"), deserializer,
				() -> List.of(), cache);

		provider.getEPackage("urn:ns:t").orElseThrow(); // t=0: 200, expires at 1000

		now.set(1_000); // past TTL
		provider.getEPackage("urn:ns:t").orElseThrow(); // revalidate -> 304 -> keep, new expiry 2000

		now.set(1_500); // within the bumped TTL
		provider.getEPackage("urn:ns:t").orElseThrow(); // served from cache, no server call

		verify(request, org.mockito.Mockito.times(2)).get(); // initial + one revalidation only
		assertEquals(1, deserializer.calls.get(), "only the initial 200 parses");
	}

	// ---- nsURI allow / deny lists (P2-9) ----------------------------------

	private RemoteEPackageProviderImpl provider(ClientConfiguration config, ClientCache<String, EPackage> cache) {
		return new RemoteEPackageProviderImpl(target, config, fakeDeserializer(), () -> List.of("fallbackScope"),
				cache);
	}

	@Test
	void getEPackage_deniedNsUri_returnsEmpty_evenIfCached() {
		// A cached entry must not leak past the deny list.
		ClientCache<String, EPackage> cache = new ClientCache<>(10, 0);
		EPackage cached = EcoreFactory.eINSTANCE.createEPackage();
		cached.setNsURI("urn:ns:denied");
		cache.put("urn:ns:denied", cached, null, null);

		ClientConfiguration cfg = ClientConfiguration.builder().baseUri(BASE).scopeAllowList(List.of("jena"))
				.nsUriDenyList(List.of("urn:ns:denied")).build();

		assertFalse(provider(cfg, cache).getEPackage("urn:ns:denied").isPresent());
		verify(request, org.mockito.Mockito.never()).get(); // no fetch either
	}

	@Test
	void getEPackage_notInAllowList_returnsEmpty() {
		ClientConfiguration cfg = ClientConfiguration.builder().baseUri(BASE).scopeAllowList(List.of("jena"))
				.nsUriAllowList(List.of("urn:ns:allowed")).build();

		assertFalse(provider(cfg).getEPackage("urn:ns:other").isPresent());
		verify(request, org.mockito.Mockito.never()).get();
	}

	@Test
	void getEPackage_inAllowList_isFetched() {
		Response response = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(response);
		ClientConfiguration cfg = ClientConfiguration.builder().baseUri(BASE).scopeAllowList(List.of("jena"))
				.nsUriAllowList(List.of("urn:ns:allowed")).build();

		assertTrue(provider(cfg).getEPackage("urn:ns:allowed").isPresent());
	}

	@Test
	void refresh_deniedNsUri_returnsEmpty_appliesToWatcherPath() {
		// The drift watcher re-fetches via refresh(), so the gate must cover it too.
		ClientConfiguration cfg = ClientConfiguration.builder().baseUri(BASE).scopeAllowList(List.of("jena"))
				.nsUriDenyList(List.of("urn:ns:denied")).build();

		assertFalse(provider(cfg).refresh("urn:ns:denied").isPresent());
		verify(request, org.mockito.Mockito.never()).get();
	}

	// ---- getEPackageAtStage (P6-6) ----------------------------------------

	@Test
	void getEPackageAtStage_hitsStageExplicitContentPath() {
		Response response = contentOk("<xmi/>".getBytes(StandardCharsets.UTF_8));
		when(request.get()).thenReturn(response);

		Optional<EPackage> pkg = provider(config()).getEPackageAtStage("urn:ns:gateway", "jena", "snapshot");

		assertTrue(pkg.isPresent());
		assertEquals("urn:ns:gateway", pkg.get().getNsURI());
		// URL must be /{scope}/schema/stages/{stage}/content?nsUri=…
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals(List.of("jena", "schema", "stages", "snapshot", "content"), paths.getAllValues());
		verify(target).queryParam("nsUri", "urn:ns:gateway");
	}

	@Test
	void getEPackageAtStage_noContent_returnsEmpty() {
		Response response = noContent();
		when(request.get()).thenReturn(response);
		assertFalse(provider(config()).getEPackageAtStage("urn:ns:missing", "jena", "snapshot").isPresent());
	}

	// ---- listPackagesAtStage (P6-6) ----------------------------------------

	@Test
	void listPackagesAtStage_hitsStageExplicitListingPath() {
		String ns = "https://eclipse.dev/fennec/jena/gateway/1.0";
		String json = "{\"metadata\":[{\"objectId\":\"" + base64Url(ns)
				+ "\",\"scope\":\"jena\",\"stage\":\"snapshot\",\"version\":\"1.0\"}]}";
		Response response = jsonOk(json);
		when(request.get()).thenReturn(response);

		var result = provider(config()).listPackagesAtStage("jena", "snapshot");

		assertEquals(1, result.size());
		assertEquals(ns, result.get(0).nsUri());
		assertEquals("snapshot", result.get(0).stage());
		// URL must be /{scope}/schema/stages/{stage}  (no /content, no nsUri param)
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals(List.of("jena", "schema", "stages", "snapshot"), paths.getAllValues());
	}

	@Test
	void listPackagesAtStage_noContent_returnsEmpty() {
		Response response = noContent();
		when(request.get()).thenReturn(response);
		assertTrue(provider(config()).listPackagesAtStage("jena", "snapshot").isEmpty());
	}
}
