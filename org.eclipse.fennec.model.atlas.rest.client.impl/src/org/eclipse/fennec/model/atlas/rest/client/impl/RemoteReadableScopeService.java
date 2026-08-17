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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeApiFactory;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;

import tools.jackson.databind.JsonNode;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Remote, read-only EObject view of a single Atlas scope (P5-1) — the client
 * mirror of the server-side {@code scope.api} {@link ReadableScopeService}, so a
 * consumer's {@code @Reference(target="(atlas.scope=…)") ReadableScopeService<EObject>}
 * resolves identically against the in-process server and this client.
 * <p>
 * Per scope; the registry is a method parameter. Reads target the scope's final
 * stage (resolved server-side — never a hardcoded stage name), reading through to
 * parent scopes' final stages when inheriting. Mapping:
 * <ul>
 * <li>{@code get(registry, objectId)} → {@code GET /{s}/registries/{r}/content?objectId=…}
 * (P5-0, stage-free final stage), fronted by the cache (P2-5) and conditional GET
 * (P2-6): the stored ETag is sent as {@code If-None-Match}; a {@code 304} keeps the
 * cached instance (and refreshes its TTL) without re-decoding;</li>
 * <li>{@code listObjectIds(registry)} → {@code GET /{s}/registries/{r}} (final-stage
 * {@code ObjectMetadata} listing, inheritance-aware);</li>
 * <li>{@code listAll}/{@code stream} → built on the two above;</li>
 * <li>{@code getScopeInfo()} → {@code GET /scopes/{s}}, mapped to a {@code scope.api}
 * {@link ScopeInfo} (the server's {@code Scope} <em>is</em> a {@code ScopeInfo}).</li>
 * </ul>
 * <p>
 * EObject content is requested and served as {@code application/xmi} (plain EMF XMI,
 * no codec): {@link #get} loads the body with a stock EMF {@code XMIResource} into a
 * ResourceSet from {@link #resourceSetFactory}, which is expected to be the
 * Atlas-aware ResourceSet ({@link ModelAtlasClientImpl#newResourceSet()}) so the
 * object's metamodel — and any cross-referenced remote objects — resolve, locally or
 * via the remote Atlas. The decode is identical in the plain-Java and OSGi clients —
 * no codec dependency, no asymmetry.
 */
class RemoteReadableScopeService implements ReadableScopeService<EObject> {

	/** The {@code registries} path segment under a scope. */
	static final String REGISTRIES = "registries";
	/** The {@code content} path segment (stage-free final-stage content, P5-0). */
	static final String CONTENT = "content";
	/** The {@code stages} path segment; present only for an explicit-stage read (P6-4). */
	static final String STAGES = "stages";
	/** EObject content is requested as plain EMF XMI; the body decode is stock EMF (no codec). */
	static final String EOBJECT_MEDIA_TYPE = "application/xmi";

	private final WebTarget baseTarget;
	private final String scopeName;
	private final Supplier<ResourceSet> resourceSetFactory;
	private final ClientCache<ObjectKey, EObject> cache;
	/**
	 * Lazily-cached {@code registry name → RegistryType} map for this scope, used by the
	 * SCHEMA-registry guard. A registry's type is structural and rarely drifts, so it is
	 * fetched once per service instance (via {@link #getScopeInfo()}) and memoized.
	 */
	private volatile Map<String, RegistryType> registryTypes;

	RemoteReadableScopeService(WebTarget baseTarget, ClientConfiguration configuration, String scopeName,
			Supplier<ResourceSet> resourceSetFactory) {
		this(baseTarget, configuration, scopeName, resourceSetFactory,
				new ClientCache<>(configuration.getCacheMaxEntries(), configuration.getCacheTtlMs()));
	}

	RemoteReadableScopeService(WebTarget baseTarget, ClientConfiguration configuration, String scopeName,
			Supplier<ResourceSet> resourceSetFactory, ClientCache<ObjectKey, EObject> cache) {
		this.baseTarget = Objects.requireNonNull(baseTarget, "baseTarget");
		Objects.requireNonNull(configuration, "configuration");
		this.scopeName = Objects.requireNonNull(scopeName, "scopeName");
		this.resourceSetFactory = Objects.requireNonNull(resourceSetFactory, "resourceSetFactory");
		this.cache = Objects.requireNonNull(cache, "cache");
	}

	/** The {@code (scope, registry, stage, objectId)} keys currently held in the cache — used by the drift watcher (P5-2). */
	java.util.Set<ObjectKey> cachedObjects() {
		return cache.keys();
	}

	/**
	 * Test seam: pre-seed the memoized {@code registry name → type} map so guarded reads
	 * skip the {@link #getScopeInfo()} preflight. Production code never calls this — it
	 * populates the map lazily on first guarded read.
	 */
	void primeRegistryTypes(Map<String, RegistryType> types) {
		this.registryTypes = Map.copyOf(types);
	}

	/** Outcome of revalidating one cached object view during drift (P6-5). */
	enum DriftOutcome {
		/** The view's content changed and was re-cached. */
		CHANGED,
		/** The view still resolves to the same content (conditional GET hit {@code 304}). */
		UNCHANGED,
		/** The view is gone on the server; the entry was dropped. */
		REMOVED
	}

	/**
	 * Revalidate one cached view {@code (registry, stage, objectId)} ({@code stage == null} = final)
	 * via conditional GET (P2-6), reporting whether its content actually changed. Used only by the
	 * drift watcher: because inheritance means a view's requested stage need not be its content's
	 * origin stage, the watcher revalidates every held view of a changed object rather than matching
	 * on stage (P6-5).
	 */
	DriftOutcome refresh(String registry, String stage, String objectId) {
		Objects.requireNonNull(registry, "registry");
		Objects.requireNonNull(objectId, "objectId");
		ObjectKey key = new ObjectKey(scopeName, registry, stage, objectId);
		Optional<EObject> before = cache.lookup(key).map(ClientCache.Entry::value);
		Optional<EObject> after = revalidateOrFetch(key);
		if (after.isEmpty()) {
			return DriftOutcome.REMOVED;
		}
		// revalidateOrFetch re-puts the SAME instance on 304 and a newly-decoded one on 200, so
		// identity distinguishes unchanged from changed. An entry evicted mid-cycle (absent before)
		// counts as changed — it was freshly fetched.
		return before.isPresent() && before.get() == after.get() ? DriftOutcome.UNCHANGED : DriftOutcome.CHANGED;
	}

	@Override
	public String getScopeName() {
		return scopeName;
	}

	@Override
	public boolean isInheritingFromParentScope() {
		String parent = getScopeInfo().getParentScope();
		return parent != null && !parent.isBlank();
	}

	@Override
	public Optional<EObject> get(String registry, String objectId) {
		return get(registry, null, objectId);
	}

	/**
	 * Resolve one object from {@code registry} at {@code stage} ({@code null} = final stage,
	 * stage-free URL), fronted by the shared cache and conditional GET. The final-stage path and
	 * the per-registry stage views ({@link RemoteReadableRegistryView}) both route through here so
	 * they share one cache, keyed by stage.
	 */
	Optional<EObject> get(String registry, String stage, String objectId) {
		Objects.requireNonNull(registry, "registry");
		Objects.requireNonNull(objectId, "objectId");
		assertNotSchemaRegistry(registry);
		ObjectKey key = new ObjectKey(scopeName, registry, stage, objectId);
		Optional<EObject> cached = cache.get(key);
		if (cached.isPresent()) {
			// Identity policy: a cache hit returns the same instance within one client
			// lifetime (== is meaningful only within a fetch session). See P5-6.
			return cached;
		}
		return revalidateOrFetch(key);
	}

	@Override
	public List<String> listObjectIds(String registry) {
		return listObjectIds(registry, null);
	}

	/** List object ids in {@code registry} at {@code stage} ({@code null} = final stage). */
	List<String> listObjectIds(String registry, String stage) {
		Objects.requireNonNull(registry, "registry");
		assertNotSchemaRegistry(registry);
		WebTarget listTarget = registryTarget(registry, stage);
		Response response = RestSupport.get(listTarget, MediaType.APPLICATION_JSON);
		try {
			if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
				return List.of();
			}
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, "listObjectIds(" + scopeName + ", " + registry + ")");
			}
			return parseObjectIds(response.readEntity(String.class), registry);
		} finally {
			response.close();
		}
	}

	@Override
	public List<EObject> listAll(String registry) {
		List<EObject> all = new ArrayList<>();
		for (String objectId : listObjectIds(registry)) {
			get(registry, objectId).ifPresent(all::add);
		}
		return List.copyOf(all);
	}

	@Override
	public Stream<EObject> stream(String registry) {
		return listAll(registry).stream();
	}

	@Override
	public ScopeInfo getScopeInfo() {
		WebTarget target = baseTarget.path("scopes").path(scopeName);
		Response response = RestSupport.get(target, MediaType.APPLICATION_JSON);
		try {
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, "getScopeInfo(" + scopeName + ")");
			}
			return parseScopeInfo(response.readEntity(String.class));
		} finally {
			response.close();
		}
	}

	/**
	 * Re-contact the server for one object, sending {@code If-None-Match} with the
	 * stored ETag if one is cached (P2-6):
	 * <ul>
	 * <li>{@code 304} → keep the cached instance, refresh its TTL, no decode;</li>
	 * <li>{@code 200} → decode the body, replace the cache entry with the new value/ETag;</li>
	 * <li>{@code 204}/{@code 404}/other → drop any stale entry and report absent.</li>
	 * </ul>
	 */
	private Optional<EObject> revalidateOrFetch(ObjectKey key) {
		Optional<ClientCache.Entry<EObject>> existing = cache.lookup(key);
		String ifNoneMatch = existing.map(ClientCache.Entry::etag).orElse(null);
		WebTarget target = registryTarget(key.registry(), key.stage()).path(CONTENT).queryParam("objectId",
				key.objectId());
		Response response = RestSupport.get(target, EOBJECT_MEDIA_TYPE, ifNoneMatch);
		try {
			if (RestSupport.isNotModified(response)) {
				ClientCache.Entry<EObject> entry = existing.orElseThrow();
				cache.put(key, entry.value(), entry.etag(), entry.lastModified());
				return Optional.of(entry.value());
			}
			if (!RestSupport.isSuccess(response) || !response.hasEntity()) {
				cache.invalidate(key);
				return Optional.empty();
			}
			byte[] body = response.readEntity(byte[].class);
			EObject content = loadEObject(body, key);
			cache.put(key, content, response.getHeaderString(HttpHeaders.ETAG),
					response.getHeaderString(HttpHeaders.LAST_MODIFIED));
			return Optional.of(content);
		} finally {
			response.close();
		}
	}

	/**
	 * Load a plain EMF XMI body into an {@link EObject} with a stock
	 * {@code XMIResource} — no codec. The body is loaded into a ResourceSet from
	 * {@link #resourceSetFactory} (the Atlas-aware set), so the object's metamodel and
	 * cross-references resolve locally or via the remote Atlas.
	 */
	private EObject loadEObject(byte[] body, ObjectKey key) {
		ResourceSet resourceSet = resourceSetFactory.get();
		// Be robust if a bare ResourceSet is supplied: ensure an XMI factory is present.
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.putIfAbsent(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		// Absolute URI to avoid "resolve against non-hierarchical or relative base".
		Resource resource = resourceSet.createResource(URI.createURI("atlas-client://eobject.xmi"));
		try {
			resource.load(new ByteArrayInputStream(body), loadOptions());
		} catch (IOException e) {
			throw new ModelAtlasClientException("Failed to read EObject XMI for " + key.objectId(), e);
		}
		if (!resource.getErrors().isEmpty()) {
			throw new ModelAtlasClientException(
					"EObject XMI for " + key.objectId() + " had load errors: " + resource.getErrors());
		}
		if (resource.getContents().isEmpty()) {
			throw new ModelAtlasClientException("EObject XMI for " + key.objectId() + " contained no content");
		}
		return resource.getContents().get(0);
	}

	/** Robust XMI load options, matching the server's EMF serialization. */
	private static Map<Object, Object> loadOptions() {
		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
		options.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
		options.put(XMLResource.OPTION_LAX_FEATURE_PROCESSING, Boolean.TRUE);
		options.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
		return options;
	}

	/**
	 * {@code /{scopeName}/registries/{registry}} for the final stage ({@code stage == null}), or
	 * {@code …/registries/{registry}/stages/{stage}} for an explicit stage (P6-4).
	 */
	private WebTarget registryTarget(String registry, String stage) {
		WebTarget target = baseTarget.path(scopeName).path(REGISTRIES).path(registry);
		return stage == null ? target : target.path(STAGES).path(stage);
	}

	/**
	 * Reject reading a SCHEMA registry through this object API: a SCHEMA registry holds
	 * EPackages, which an EObject read <em>could</em> fetch through the generic
	 * {@code registries/{r}/content} path, but the caller should use the dedicated EPackage
	 * API instead so the package is cached and resolved as a metamodel rather than handled as
	 * an opaque EObject. The registry type is read from the scope descriptor
	 * ({@link #getScopeInfo()}), memoized per instance. Unknown registries (absent from the
	 * descriptor) pass through, preserving the existing 404/empty behavior.
	 *
	 * @throws ModelAtlasClientException if {@code registry} is a {@link RegistryType#SCHEMA} registry
	 */
	private void assertNotSchemaRegistry(String registry) {
		if (registryTypeOf(registry) == RegistryType.SCHEMA) {
			throw new ModelAtlasClientException("Registry '" + registry + "' in scope '" + scopeName
					+ "' is a SCHEMA registry; fetch its EPackages via ModelAtlasClient.ePackages() "
					+ "(RemoteEPackageProvider), not the object API (ReadableScopeService).");
		}
	}

	/**
	 * The {@link RegistryType} of {@code registry} in this scope, or {@code null} if the
	 * registry is not listed in the scope descriptor. Lazily fetches and memoizes the
	 * {@code registry name → type} map (double-checked) on first use.
	 */
	private RegistryType registryTypeOf(String registry) {
		Map<String, RegistryType> types = registryTypes;
		if (types == null) {
			synchronized (this) {
				types = registryTypes;
				if (types == null) {
					types = loadRegistryTypes();
					registryTypes = types;
				}
			}
		}
		return types.get(registry);
	}

	/** Read the {@code registry name → type} map from {@link #getScopeInfo()}. */
	private Map<String, RegistryType> loadRegistryTypes() {
		Map<String, RegistryType> types = new HashMap<>();
		for (RegistryInfo ri : getScopeInfo().getRegistries()) {
			if (ri.getName() != null) {
				types.put(ri.getName(), ri.getType());
			}
		}
		return types;
	}

	/** Extract {@code objectId}s from an {@code ObjectMetadataContainer} JSON body. */
	private List<String> parseObjectIds(String json, String registry) {
		JsonNode metadata = RestSupport.parse(json, "listObjectIds(" + scopeName + ", " + registry + ")")
				.path("metadata");
		List<String> ids = new ArrayList<>();
		for (JsonNode entry : metadata) {
			JsonNode objectId = entry.get("objectId");
			if (objectId != null && !objectId.isNull()) {
				ids.add(objectId.asString());
			}
		}
		return List.copyOf(ids);
	}

	/**
	 * Map a server {@code Scope} JSON body onto a {@code scope.api} {@link ScopeInfo}.
	 * The server's {@code Scope} <em>is</em> a {@code ScopeInfo} (and each {@code Registry}
	 * a {@code RegistryInfo}), so the wire fields line up one-to-one.
	 */
	private ScopeInfo parseScopeInfo(String json) {
		JsonNode node = RestSupport.parse(json, "getScopeInfo(" + scopeName + ")");
		ScopeInfo info = ScopeApiFactory.eINSTANCE.createScopeInfo();
		info.setName(text(node, "name"));
		info.setDescription(text(node, "description"));
		info.setParentScope(text(node, "parentScope"));
		for (JsonNode r : node.path("registries")) {
			RegistryInfo ri = ScopeApiFactory.eINSTANCE.createRegistryInfo();
			ri.setName(text(r, "name"));
			ri.setDescription(text(r, "description"));
			String type = text(r, "type");
			if (type != null) {
				RegistryType resolved = RegistryType.get(type);
				if (resolved != null) {
					ri.setType(resolved);
				}
			}
			for (JsonNode s : r.path("stages")) {
				StageInfo si = ScopeApiFactory.eINSTANCE.createStageInfo();
				si.setName(text(s, "name"));
				si.setDescription(text(s, "description"));
				// EMF JSON omits default-valued attributes: readable defaults true,
				// writable/final default false (see StageInfo in scope-api.ecore).
				si.setReadable(bool(s, "readable", true));
				si.setWritable(bool(s, "writable", false));
				si.setFinal(bool(s, "final", false));
				ri.getStages().add(si);
			}
			info.getRegistries().add(ri);
		}
		return info;
	}

	private static String text(JsonNode node, String field) {
		JsonNode value = node.get(field);
		return value == null || value.isNull() ? null : value.asString();
	}

	/** Read a boolean field, falling back to {@code defaultValue} when absent/null (EMF JSON omits defaults). */
	private static boolean bool(JsonNode node, String field, boolean defaultValue) {
		JsonNode value = node.get(field);
		return value == null || value.isNull() ? defaultValue : value.asBoolean(defaultValue);
	}

	/**
	 * Cache key for one object: {@code (scope, registry, stage, objectId)}. A {@code null}
	 * {@code stage} means the registry's final stage (stage-free URL); a non-null stage is an
	 * explicit-stage read (P6-4). The stage component keeps a final fetch and a draft fetch of the
	 * same id in distinct entries.
	 */
	record ObjectKey(String scope, String registry, String stage, String objectId) {
	}

	/**
	 * A view bound to {@code registry}'s <em>final</em> stage (stage-free URLs); equivalent to the
	 * stage-free {@link #get}/{@link #listObjectIds} but in the registry-bound vocabulary.
	 * {@link ReadableRegistryView#getStageName()} is {@code null} for this view.
	 */
	@Override
	public ReadableRegistryView<EObject> registryView(String registry) {
		Objects.requireNonNull(registry, "registry");
		return new RemoteReadableRegistryView(this, registry, null);
	}

	/**
	 * A view bound to {@code registry}'s {@code stage} (stage-explicit URLs). Reads are
	 * inheritance-aware server-side (read-through to the parent hierarchy's final stage); a registry
	 * that has no such stage yields the server's {@code 400/404}. {@code stage} must be non-null — a
	 * caller wanting the final stage uses {@link #registryView(String)}.
	 */
	@Override
	public ReadableRegistryView<EObject> registryView(String registry, String stage) {
		Objects.requireNonNull(registry, "registry");
		Objects.requireNonNull(stage, "stage");
		return new RemoteReadableRegistryView(this, registry, stage);
	}

	/**
	 * Remote {@link ReadableRegistryView} bound to one {@code (registry, stage)} of the owning
	 * {@link RemoteReadableScopeService} ({@code stage == null} = final). It owns no state: every
	 * read delegates to the service's stage-aware core, so it shares the service's one cache (and,
	 * from P6-5, its drift revalidation) rather than a private one.
	 */
	static final class RemoteReadableRegistryView implements ReadableRegistryView<EObject> {

		private final RemoteReadableScopeService service;
		private final String registry;
		private final String stage;

		RemoteReadableRegistryView(RemoteReadableScopeService service, String registry, String stage) {
			this.service = service;
			this.registry = registry;
			this.stage = stage;
		}

		@Override
		public Optional<EObject> get(String objectId) {
			return service.get(registry, stage, objectId);
		}

		@Override
		public List<String> listObjectIds() {
			return service.listObjectIds(registry, stage);
		}

		@Override
		public List<EObject> listAll() {
			List<EObject> all = new ArrayList<>();
			for (String objectId : listObjectIds()) {
				get(objectId).ifPresent(all::add);
			}
			return List.copyOf(all);
		}

		@Override
		public Stream<EObject> stream() {
			return listAll().stream();
		}

		@Override
		public String getScopeName() {
			return service.getScopeName();
		}

		@Override
		public String getRegistryName() {
			return registry;
		}

		@Override
		public String getStageName() {
			return stage;
		}
	}
}
