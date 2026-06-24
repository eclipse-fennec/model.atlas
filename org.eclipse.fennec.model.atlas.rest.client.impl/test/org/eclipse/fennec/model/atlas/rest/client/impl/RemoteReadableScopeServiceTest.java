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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;
import org.eclipse.fennec.model.atlas.rest.client.api.NotFoundException;
import org.eclipse.fennec.model.atlas.rest.client.api.TransportException;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * P5-1 — REST mapping for the per-scope {@code ReadableScopeService<EObject>}. The
 * {@link WebTarget} fluent chain is mocked (each {@code path}/{@code queryParam}
 * returns the same target) so the tests assert URL shape, status handling, caching
 * and conditional GET without a server. EObject content is plain EMF XMI, so
 * {@code get} decodes it with a stock {@code XMIResource} — the tests feed real XMI
 * bytes and let the service load them (no codec, no stub).
 */
class RemoteReadableScopeServiceTest {

	private static final URI BASE = URI.create("https://atlas.example.org/atlas");
	private static final String SCOPE = "jena";
	private static final String REGISTRY = "cocl";

	private final WebTarget target = mock(WebTarget.class);
	private final Invocation.Builder request = mock(Invocation.Builder.class);

	RemoteReadableScopeServiceTest() {
		when(target.path(anyString())).thenReturn(target);
		when(target.queryParam(anyString(), any())).thenReturn(target);
		when(target.getUri()).thenReturn(BASE);
		when(target.request(anyString())).thenReturn(request);
		when(request.header(anyString(), any())).thenReturn(request);
	}

	/** Bare {@link ResourceSet}s: the service installs the XMI factory, and Ecore is globally registered. */
	private static final Supplier<ResourceSet> RESOURCE_SETS = ResourceSetImpl::new;

	private RemoteReadableScopeService service() {
		RemoteReadableScopeService service = new RemoteReadableScopeService(target, config(), SCOPE, RESOURCE_SETS);
		// REGISTRY is a COCL registry, so the SCHEMA-registry guard passes without a
		// scope-info preflight; the guard's own behavior is exercised separately below.
		service.primeRegistryTypes(Map.of(REGISTRY, RegistryType.COCL));
		return service;
	}

	private static ClientConfiguration config() {
		return ClientConfiguration.builder().baseUri(BASE).build();
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

	private static Response contentOk(byte[] body, String etag) {
		Response r = status(200, Response.Status.OK);
		when(r.hasEntity()).thenReturn(true);
		when(r.readEntity(byte[].class)).thenReturn(body);
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

	/** Serialize an EObject to plain EMF XMI — the exact wire the server emits for {@code application/xmi}. */
	private static byte[] xmi(EObject obj) throws IOException {
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		Resource resource = rs.createResource(org.eclipse.emf.common.util.URI.createURI("mem://obj.xmi"));
		resource.getContents().add(obj);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, null);
		return out.toByteArray();
	}

	private static EClass thing() {
		EClass thing = EcoreFactory.eINSTANCE.createEClass();
		thing.setName("Thing");
		return thing;
	}

	// ---- getScopeName -----------------------------------------------------

	@Test
	void getScopeName_returnsConfiguredScope() {
		assertEquals(SCOPE, service().getScopeName());
	}

	// ---- listObjectIds ----------------------------------------------------

	@Test
	void listObjectIds_getsRegistryPath_andParsesIds() {
		Response response = jsonOk("{\"metadata\":[{\"objectId\":\"a\"},{\"objectId\":\"b\"}]}");
		when(request.get()).thenReturn(response);

		List<String> ids = service().listObjectIds(REGISTRY);

		assertEquals(List.of("a", "b"), ids);
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		// Final-stage listing: /{scope}/registries/{registry}
		assertEquals(List.of(SCOPE, "registries", REGISTRY), paths.getAllValues());
	}

	@Test
	void listObjectIds_noContent_returnsEmpty() {
		Response response = noContent();
		when(request.get()).thenReturn(response);
		assertTrue(service().listObjectIds(REGISTRY).isEmpty());
	}

	@Test
	void listObjectIds_errorStatus_throws() {
		Response response = status(500, Response.Status.INTERNAL_SERVER_ERROR);
		when(request.get()).thenReturn(response);
		assertThrows(ModelAtlasClientException.class, () -> service().listObjectIds(REGISTRY));
	}

	@Test
	void listObjectIds_404_throwsNotFound() {
		Response response = status(404, Response.Status.NOT_FOUND);
		when(request.get()).thenReturn(response);
		assertThrows(NotFoundException.class, () -> service().listObjectIds(REGISTRY));
	}

	// ---- get --------------------------------------------------------------

	@Test
	void get_success_loadsXmiFromFinalStagePath() throws IOException {
		Response response = contentOk(xmi(thing()), "\"v1\"");
		when(request.get()).thenReturn(response);

		EObject result = service().get(REGISTRY, "id1").orElseThrow();

		assertTrue(result instanceof EClass, "stock XMI load should reconstruct the EObject");
		assertEquals("Thing", ((EClass) result).getName());
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals(List.of(SCOPE, "registries", REGISTRY, "content"), paths.getAllValues());
		verify(target).queryParam(eq("objectId"), eq("id1"));
	}

	@Test
	void get_noContent_returnsEmpty() {
		Response response = noContent();
		when(request.get()).thenReturn(response);
		assertTrue(service().get(REGISTRY, "missing").isEmpty());
	}

	@Test
	void get_cacheHit_returnsSameInstance_withoutSecondCall() throws IOException {
		Response response = contentOk(xmi(thing()), "\"v1\"");
		when(request.get()).thenReturn(response);
		RemoteReadableScopeService service = service();

		EObject first = service.get(REGISTRY, "id1").orElseThrow();
		EObject second = service.get(REGISTRY, "id1").orElseThrow(); // served from cache

		assertSame(first, second, "a cache hit must return the same instance");
		verify(request, times(1)).get();
	}

	@Test
	void get_distinctObjectIds_returnDistinctInstances() throws IOException {
		// P5-6 identity policy: identity is per (registry, objectId) within one client
		// lifetime — a cache hit on the SAME id returns the same instance (see
		// get_cacheHit_returnsSameInstance_withoutSecondCall), but DIFFERENT ids never alias.
		Response first = contentOk(xmi(thing()), "\"a\"");
		Response second = contentOk(xmi(thing()), "\"b\"");
		when(request.get()).thenReturn(first, second);
		RemoteReadableScopeService service = service();

		EObject a = service.get(REGISTRY, "id1").orElseThrow();
		EObject b = service.get(REGISTRY, "id2").orElseThrow();

		assertNotSame(a, b, "distinct objectIds must map to distinct cache entries");
		verify(request, times(2)).get();
	}

	@Test
	void get_postTtlRevalidation_304_keepsSameInstance() throws IOException {
		Response first = contentOk(xmi(thing()), "\"v1\"");
		Response revalidated = notModified();
		when(request.get()).thenReturn(first, revalidated);

		AtomicLong now = new AtomicLong(0);
		ClientCache<RemoteReadableScopeService.ObjectKey, EObject> cache = new ClientCache<>(10, 1_000, now::get);
		RemoteReadableScopeService service = new RemoteReadableScopeService(target, config(), SCOPE, RESOURCE_SETS,
				cache);
		service.primeRegistryTypes(Map.of(REGISTRY, RegistryType.COCL));

		EObject initial = service.get(REGISTRY, "id1").orElseThrow(); // t=0: 200, expires at 1000

		now.set(1_000); // past TTL
		EObject afterRevalidation = service.get(REGISTRY, "id1").orElseThrow(); // conditional -> 304 -> keep

		verify(request).header("If-None-Match", "\"v1\"");
		assertSame(initial, afterRevalidation, "304 must return the same cached instance");
		verify(request, times(2)).get(); // initial + one revalidation only
	}

	// ---- getScopeInfo -----------------------------------------------------

	@Test
	void getScopeInfo_mapsNameParentAndRegistries() {
		String json = "{\"name\":\"jena\",\"description\":\"d\",\"parentScope\":\"atlas\","
				+ "\"registries\":[{\"name\":\"cocl\",\"description\":\"c\",\"type\":\"COCL\"},"
				+ "{\"name\":\"schema\",\"type\":\"SCHEMA\"}]}";
		Response response = jsonOk(json);
		when(request.get()).thenReturn(response);

		ScopeInfo info = service().getScopeInfo();

		assertEquals("jena", info.getName());
		assertEquals("atlas", info.getParentScope());
		assertEquals(2, info.getRegistries().size());
		assertEquals("cocl", info.getRegistries().get(0).getName());
		assertEquals(RegistryType.COCL, info.getRegistries().get(0).getType());
		assertEquals(RegistryType.SCHEMA, info.getRegistries().get(1).getType());
	}

	@Test
	void getScopeInfo_parsesRegistryStagesAndFlags() {
		// 'draft' carries explicit flags; 'release' is final; 'bare' omits every flag and
		// must fall back to the model defaults (readable=true, writable=final=false),
		// since EMF JSON drops default-valued attributes from the wire.
		String json = "{\"name\":\"jena\",\"registries\":[{\"name\":\"cocl\",\"type\":\"COCL\",\"stages\":["
				+ "{\"name\":\"draft\",\"description\":\"work in progress\",\"readable\":true,\"writable\":true,\"final\":false},"
				+ "{\"name\":\"release\",\"final\":true},"
				+ "{\"name\":\"bare\"}]}]}";
		Response response = jsonOk(json);
		when(request.get()).thenReturn(response);

		ScopeInfo info = service().getScopeInfo();

		List<StageInfo> stages = info.getRegistries().get(0).getStages();
		assertEquals(3, stages.size());

		StageInfo draft = stages.get(0);
		assertEquals("draft", draft.getName());
		assertEquals("work in progress", draft.getDescription());
		assertTrue(draft.isReadable());
		assertTrue(draft.isWritable());
		assertFalse(draft.isFinal());

		StageInfo release = stages.get(1);
		assertEquals("release", release.getName());
		assertTrue(release.isFinal());
		assertFalse(release.isWritable());

		StageInfo bare = stages.get(2);
		assertEquals("bare", bare.getName());
		assertTrue(bare.isReadable(), "readable must default to true when omitted");
		assertFalse(bare.isWritable(), "writable must default to false when omitted");
		assertFalse(bare.isFinal(), "final must default to false when omitted");
	}

	@Test
	void getScopeInfo_registryWithoutStages_yieldsEmptyStageList() {
		Response response = jsonOk("{\"name\":\"jena\",\"registries\":[{\"name\":\"cocl\",\"type\":\"COCL\"}]}");
		when(request.get()).thenReturn(response);

		ScopeInfo info = service().getScopeInfo();

		assertTrue(info.getRegistries().get(0).getStages().isEmpty());
	}

	@Test
	void isInheritingFromParentScope_trueWhenParentPresent() {
		Response response = jsonOk("{\"name\":\"jena\",\"parentScope\":\"atlas\"}");
		when(request.get()).thenReturn(response);
		assertTrue(service().isInheritingFromParentScope());
	}

	@Test
	void isInheritingFromParentScope_falseWhenNoParent() {
		Response response = jsonOk("{\"name\":\"atlas\"}");
		when(request.get()).thenReturn(response);
		assertFalse(service().isInheritingFromParentScope());
	}

	// ---- SCHEMA-registry guard --------------------------------------------

	/** A scope descriptor whose {@code schema} registry is SCHEMA-typed and {@code cocl} is not. */
	private static final String SCOPE_WITH_SCHEMA_REGISTRY = "{\"name\":\"jena\",\"parentScope\":\"atlas\","
			+ "\"registries\":[{\"name\":\"cocl\",\"type\":\"COCL\"},{\"name\":\"schema\",\"type\":\"SCHEMA\"}]}";

	/** An <em>unprimed</em> service, so the guard fetches the scope descriptor to learn registry types. */
	private RemoteReadableScopeService unprimedService() {
		return new RemoteReadableScopeService(target, config(), SCOPE, RESOURCE_SETS);
	}

	@Test
	void get_onSchemaRegistry_throws_andPointsToEPackageApi() {
		Response descriptor = jsonOk(SCOPE_WITH_SCHEMA_REGISTRY);
		when(request.get()).thenReturn(descriptor);

		ModelAtlasClientException ex = assertThrows(ModelAtlasClientException.class,
				() -> unprimedService().get("schema", "some.ns.uri"));
		assertTrue(ex.getMessage().contains("SCHEMA"), "message should name the registry type");
		assertTrue(ex.getMessage().contains("ePackages()"), "message should point to the EPackage API");
		// The guard rejects before any content fetch: the only GET was the scope-info preflight.
		verify(request, times(1)).get();
	}

	@Test
	void listObjectIds_onSchemaRegistry_throws() {
		Response descriptor = jsonOk(SCOPE_WITH_SCHEMA_REGISTRY);
		when(request.get()).thenReturn(descriptor);
		assertThrows(ModelAtlasClientException.class, () -> unprimedService().listObjectIds("schema"));
	}

	@Test
	void guard_isMemoized_scopeInfoFetchedOnceAcrossReads() {
		Response descriptor = jsonOk(SCOPE_WITH_SCHEMA_REGISTRY);
		when(request.get()).thenReturn(descriptor);
		RemoteReadableScopeService service = unprimedService();

		assertThrows(ModelAtlasClientException.class, () -> service.get("schema", "a"));
		assertThrows(ModelAtlasClientException.class, () -> service.listObjectIds("schema"));

		// Two guarded reads, but the registry-type map is fetched only once.
		verify(request, times(1)).get();
	}

	@Test
	void get_onNonSchemaRegistry_passesGuard() throws IOException {
		// Unprimed: the guard fetches the descriptor (1st GET), finds cocl is COCL-typed,
		// and lets the read through to the content fetch (2nd GET).
		Response descriptor = jsonOk(SCOPE_WITH_SCHEMA_REGISTRY);
		Response content = contentOk(xmi(thing()), "\"v1\"");
		when(request.get()).thenReturn(descriptor, content);
		RemoteReadableScopeService service = unprimedService();

		EObject result = service.get("cocl", "id1").orElseThrow();

		assertTrue(result instanceof EClass);
	}

	// ---- registryView (P6-4) ----------------------------------------------

	@Test
	void registryView_finalStage_getUsesStageFreeUrl() throws IOException {
		Response response = contentOk(xmi(thing()), "\"v1\"");
		when(request.get()).thenReturn(response);

		EObject result = service().registryView(REGISTRY).get("id1").orElseThrow();

		assertTrue(result instanceof EClass);
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals(List.of(SCOPE, "registries", REGISTRY, "content"), paths.getAllValues());
		verify(target).queryParam(eq("objectId"), eq("id1"));
	}

	@Test
	void registryView_explicitStage_getUsesStageExplicitUrl() throws IOException {
		Response response = contentOk(xmi(thing()), "\"v1\"");
		when(request.get()).thenReturn(response);

		EObject result = service().registryView(REGISTRY, "snapshot").get("id1").orElseThrow();

		assertTrue(result instanceof EClass);
		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals(List.of(SCOPE, "registries", REGISTRY, "stages", "snapshot", "content"), paths.getAllValues());
		verify(target).queryParam(eq("objectId"), eq("id1"));
	}

	@Test
	void registryView_explicitStage_listObjectIdsUsesStageExplicitUrl() {
		Response response = jsonOk("{\"metadata\":[{\"objectId\":\"a\"}]}");
		when(request.get()).thenReturn(response);

		assertEquals(List.of("a"), service().registryView(REGISTRY, "snapshot").listObjectIds());

		ArgumentCaptor<String> paths = ArgumentCaptor.forClass(String.class);
		verify(target, org.mockito.Mockito.atLeastOnce()).path(paths.capture());
		assertEquals(List.of(SCOPE, "registries", REGISTRY, "stages", "snapshot"), paths.getAllValues());
	}

	@Test
	void registryView_finalAndStaged_doNotAliasInCache() throws IOException {
		// Same objectId, two stages: the stage component of ObjectKey keeps them in distinct
		// entries — two server fetches, two distinct instances (no collision).
		Response finalResp = contentOk(xmi(thing()), "\"f\"");
		Response stagedResp = contentOk(xmi(thing()), "\"s\"");
		when(request.get()).thenReturn(finalResp, stagedResp);
		RemoteReadableScopeService service = service();

		EObject viaFinal = service.registryView(REGISTRY).get("id1").orElseThrow();
		EObject viaStage = service.registryView(REGISTRY, "snapshot").get("id1").orElseThrow();

		assertNotSame(viaFinal, viaStage, "final and staged reads of the same id must not alias");
		verify(request, times(2)).get();
	}

	@Test
	void registryView_sameView_repeatGetIsCacheHit() throws IOException {
		Response response = contentOk(xmi(thing()), "\"v1\"");
		when(request.get()).thenReturn(response);
		RemoteReadableScopeService service = service();
		ReadableRegistryView<EObject> view = service.registryView(REGISTRY, "snapshot");

		EObject first = view.get("id1").orElseThrow();
		EObject second = view.get("id1").orElseThrow();

		assertSame(first, second);
		verify(request, times(1)).get();
	}

	@Test
	void registryView_exposesScopeRegistryAndStage() {
		ReadableRegistryView<EObject> finalView = service().registryView(REGISTRY);
		assertEquals(SCOPE, finalView.getScopeName());
		assertEquals(REGISTRY, finalView.getRegistryName());
		assertNull(finalView.getStageName(), "final-stage view has no stage name");

		ReadableRegistryView<EObject> staged = service().registryView(REGISTRY, "snapshot");
		assertEquals("snapshot", staged.getStageName());
	}

	@Test
	void registryView_onSchemaRegistry_throws() {
		// The SCHEMA-registry guard applies at every stage: read through the view, same rejection.
		Response descriptor = jsonOk(SCOPE_WITH_SCHEMA_REGISTRY);
		when(request.get()).thenReturn(descriptor);

		assertThrows(ModelAtlasClientException.class,
				() -> unprimedService().registryView("schema", "snapshot").get("some.ns.uri"));
	}

	// ---- transport --------------------------------------------------------

	@Test
	void transportFault_mapsToTransportException() {
		when(request.get()).thenThrow(new ProcessingException("connection refused"));
		assertThrows(TransportException.class, () -> service().listObjectIds(REGISTRY));
	}
}
