# Design: Model Atlas Read-Only Client (Plain Java + OSGi)

## Problem Statement

Today, every consumer of the Fennec Model Atlas — for example a validation service, a code generator, or a data pipeline — needs the EPackages and the registered EObjects (e.g. the `cocl` and `datagen` registries in the `jena` scope) to be physically present in the JVM that runs them. They must be either shipped as bundles next to the consumer, or run inside the same OSGi runtime as the Atlas itself.

This couples remote consumers tightly to the Atlas deployment. A validation service that depends on a scope's EObject registry cannot easily be embedded in a third-party application that simply has network access to an Atlas instance.

A second, related problem is **interface asymmetry**. The server today exposes a single `ScopeService<T>` that mixes read operations (used by validation, generation, lookup) with write operations (upload, transition, delete — workflow operations). A consumer that only wants to read is forced to depend on the workflow contract, which both bloats its dependency surface and makes it harder to ship a runtime that lacks workflow capabilities — even though the same code paths run.

This design tackles both problems together: a read-only client, *and* a server-side interface split so that client and server expose the **same** read-only Java contract.

## Goals

1. Provide a Java client library that exposes the Atlas via a **local OSGi service surface that is contract-identical to the server side** for the read paths — so that a consumer (e.g. validation) cannot tell whether the underlying registry is in-process or remote.
2. **Split the existing `ScopeService<T>` into a read-only base interface and a writable workflow extension.** The read-only base is what both server and client publish; the writable extension is workflow-only and lives on the server (and, in the future, in a separate workflow client).
3. Make the client usable in **plain Java** (no OSGi runtime) through a programmatic API, sharing the same core implementation with the OSGi variant.
4. Resolve EPackages and EObjects **local-first**: only fall back to the remote Atlas when nothing local matches.
5. **Cache** remote EPackages and EObjects locally and re-publish them as OSGi services so existing consumers work unchanged.
6. **Detect drift** efficiently via HTTP conditional requests; provide a way to discover that a newer/different version exists on the server, with optional background refresh.
7. Keep the wire protocol simple — Jakarta RS Client only.
8. Keep the public API **free of `ObjectMetadata`**. `ObjectMetadata` is a wire/storage type optimised for client↔server transport and for drift bookkeeping; it should not surface in interfaces consumers program against.

## Non-Goals

- **Writes, transitions, deletes, and any workflow operation** are explicitly out of scope for this client. They are the responsibility of a separate **workflow client** (separate bundle family `rest.client.workflow.*`, separate design document, deployed only where workflow capabilities are explicitly wanted). The split is a **security boundary**: ship the read-only client where you only need to read.
- A graphical client / IDE integration.
- Authentication / authorization design beyond exposing pluggable hooks (Bearer token, mTLS) on the Jakarta RS `Client`.
- Replacing the existing in-process `ScopeServiceImpl` for read use cases — it stays. The client is a *parallel* implementation for remote use, exposing the same read-only interface.
- Server-side push notifications / SSE. Drift detection is poll-based in v1.

## Use Cases

**UC-1 — Embedded validation service.**
A standalone Spring Boot / Quarkus app uses `org.eclipse.fennec.model.atlas.validation` to validate domain objects against EPackages from the `jena` scope and against EObjects registered there (e.g. `cocl`, `datagen`). It runs nowhere near the Atlas. With the client, the validation bundle finds its EPackages and the read-only `ScopedEObjectsRegistry` services as ordinary OSGi services, transparently fetched from the remote Atlas on first use. The validation bundle does **not** depend on the workflow API.

**UC-2 — Plain Java tool.**
A CI pipeline tool wants to load the latest released EPackages from a scope and validate generated artefacts against them. Uses the client API directly without an OSGi runtime.

**UC-3 — Federated registry.**
A client OSGi runtime has its own local EPackages (e.g. test-only models). When a consumer asks for nsURI X, local registrations win; if X is unknown locally, the client falls back to the remote Atlas and registers the result.

**UC-4 — Drift watch.**
The client periodically issues a single conditional HEAD per configured scope. On change, it identifies the affected nsURIs from the response, refreshes those cache entries, and **re-publishes** the corresponding OSGi services so injected consumers receive updated references.

## Implementation Phases — Overview

The work is sequenced in five phases. Each phase has a clear deliverable and is independently shippable.

| Phase | Deliverable | Side |
|---|---|---|
| **1** | Server-side ETag and conditional GET / HEAD support (incl. scope-level HEAD) | Server |
| **2** | Common client library (plain Java) — Jakarta RS Client, caching, drift, delegating `EPackage.Registry` | Client |
| **3** | OSGi delegate registry — `EPackageConfigurator` publishing with `atlas.*` properties, atomic substitution, lazy-mode resolution | Client |
| **4** | Server-side interface split — `ScopedEObjectsRegistry<T>` / `WritableScopeService<T>` | Server |
| **5** | EObject-registry client — `ScopedEObjectsRegistry<EObject>` per `(scope, registry)` (UC-1) | Client |

Phases 1–3 cover EPackage handling end to end. Phases 4–5 cover EObject registries and depend on (4) being merged before (5) can ship.

---

## Phase 1 — Server-Side ETag & Conditional Requests

The Atlas REST layer must support strong validators and conditional requests so that the client can detect drift cheaply and avoid round-trips on unchanged content. There may already be a partial implementation on the server; this phase **specifies the requirements and provides a verification checklist** so the existing code can be audited and gaps closed.

### Why this is foundational

- **No new computation.** `ObjectMetadata.contentHash` is already a SHA-256 of the serialized content, computed on every store. It *is* a strong validator; it just needs to surface as a header.
- **Removes the two-call drift pattern.** Without ETags the client must do `HEAD metadata` → compare hash → `GET content`. With ETags, a single conditional `GET` replaces both.
- **Optimistic concurrency on writes** (relevant for the future workflow client). `If-Match` makes the existing `version`/`contentHash` conflict checks a standard HTTP idiom.
- **Plays nicely with intermediaries.** Reverse proxies, browsers, generic HTTP libraries all understand `ETag`/`If-None-Match`/`Last-Modified`.
- **Purely additive, fully backward compatible.**

### Per-object requirements

1. **Strong `ETag` on single-object GETs.**
   - `GET /{scope}/schema/stages/{stage}/content?nsUri=...` → `ETag: "<contentHash>"`.
   - `GET /{scope}/registries/{reg}/stages/{stage}/content?objectId=...` → `ETag: "<contentHash>"`.
2. **`Last-Modified` from `ObjectMetadata.lastChangeTime`.**
3. **Conditional GET handling.** `If-None-Match` / `If-Modified-Since` → `304 Not Modified` when matching, no body.
4. **`ETag` on metadata responses.** Treat the metadata document as its own resource; ETag = strong hash over `(contentHash, version, status, lastChangeTime)` so metadata-only changes (e.g. stage transitions) invalidate metadata caches.
5. **`If-Match` on writes** (workflow side, not consumed by this client). On mismatch return `412 Precondition Failed`. On success return the new ETag.
6. **`Vary: Accept`** on every cacheable response. Atlas content-negotiates XMI / JSON / etc.; the ETag must vary with `Accept`.
7. **`Cache-Control: private, must-revalidate`** once auth is added.

### Scope-level HEAD endpoint

In addition to per-object ETags, the server exposes a HEAD per scope base URL that summarises the change state of the entire scope. This is what the drift watcher uses (one call per scope, not N per cached entry).

```
HEAD /{scope}
ETag: "<scopeHash>"            ; aggregate strong validator over the scope
Last-Modified: <timestamp>     ; max(lastChangeTime) within the scope
Atlas-Changed-NsUris: <csv>    ; optional response header — nsURIs whose contentHash
                               ; differs since the request's If-None-Match
Atlas-Changed-Objects: <csv>   ; optional — (registry/objectId) tuples
```

- Returns `304 Not Modified` when the client's `If-None-Match` matches the current `<scopeHash>`.
- On `200 OK`, the response headers carry diff hints so the client can refresh exactly the affected entries instead of refetching the entire scope.
- The aggregate ETag is computed over `(nsUri, contentHash)` pairs of all packages plus `(registry, objectId, contentHash)` of all registered EObjects in the scope, in canonical order. Cheap to compute, cheap to compare, stable under reordering.

### Verification checklist (against existing implementation)

When auditing what is already on the server:

- [ ] `SchemaPackagesResource` content GETs emit strong `ETag`.
- [ ] `ObjectRegistryResource` content GETs emit strong `ETag`.
- [ ] Metadata GETs emit `ETag` (covering metadata-only changes).
- [ ] All cacheable responses set `Vary: Accept`.
- [ ] `If-None-Match` / `If-Modified-Since` produce `304` (no body).
- [ ] `If-Match` on workflow writes returns `412` on mismatch.
- [ ] `HEAD /{scope}` exists, returns aggregate ETag, supports `If-None-Match` → `304`.
- [ ] `HEAD /{scope}` populates `Atlas-Changed-NsUris` / `Atlas-Changed-Objects` when called with a stale `If-None-Match`.
- [ ] Tests in `rest.tests` cover all of the above (assert headers, exercise 304/412).

### Where to change things

- `org.eclipse.fennec.model.atlas.rest.application/.../resource/SchemaPackagesResource.java`
- `org.eclipse.fennec.model.atlas.rest.application/.../resource/ObjectRegistryResource.java`
- `org.eclipse.fennec.model.atlas.rest.application/.../resource/ScopesResource.java` — add scope-level HEAD
- A new `jakarta.ws.rs.container.ContainerResponseFilter` (`@Provider`) under `rest.application` to centralise `ETag` / `Last-Modified` / `Vary: Accept` header handling. Resources already attach `ObjectMetadata` to a request property for content negotiation, so the filter can read it without re-fetching.
- A `ContainerRequestFilter` (or in-resource shortcut) for `If-None-Match` → `Response.notModified(EntityTag)`.

### Effort & risk

Small (1–2 days if hashing already exists). Risks:

- **ETag stability under serializer drift.** If the XMI/JSON serializer ever changes formatting, the hash changes even though the EObject is logically identical. That is correct HTTP-cache behaviour — bytes did change — but worth documenting.
- **Multi-format content negotiation.** Without `Vary: Accept`, a JSON-requesting client could be served a cached XMI representation. `Vary` is non-negotiable.

---

## Phase 2 — Common Client Library (Plain Java)

Phase 2 builds the **shared core** that both the plain-Java entry point and the OSGi variant (Phase 3) sit on. The architectural rule: there is **one** Jakarta RS Client implementation, **one** cache, **one** drift mechanism. The OSGi bundle in Phase 3 is a thin DS wrapper that reuses everything written here.

### Architectural goal — one core, two front-ends

The Jakarta RS Client API is the same in plain Java and inside an OSGi JAX-RS Whiteboard runtime — but the *acquisition* of the `Client` instance differs:

- **Plain Java:** built directly via `ClientBuilder.newBuilder()`.
- **OSGi (Whiteboard):** obtained through the Whiteboard's `ClientBuilder` factory (so connection providers, filters, and the runtime's HTTP client are honoured).

The client library encapsulates this difference behind a `JakartaRsClientProvider` SPI. The plain-Java module ships a default provider that uses `ClientBuilder.newBuilder()`; the OSGi module ships one that resolves the Whiteboard `ClientBuilder` via DS. **Everything above this seam — REST mapping, deserialization, caching, drift, configuration handling — is identical between the two variants.**

### Bundles introduced in this phase

| Bundle | Purpose | Key deps |
|---|---|---|
| `org.eclipse.fennec.model.atlas.rest.client.api` | Public interfaces, configuration types, `JakartaRsClientProvider` SPI. No OSGi DS, no Jakarta RS impl. | EMF core |
| `org.eclipse.fennec.model.atlas.rest.client.impl` | Jakarta RS Client logic, caching, drift detection, deserialization, delegating `EPackage.Registry`. Plain-Java capable. | api, jakarta.ws.rs, EMF, emf.common, rest.model (internal use of `ObjectMetadata`) |
| `org.eclipse.fennec.model.atlas.rest.client.tests` | Integration tests against a live Atlas. | testcontainers, JUnit 5, AssertJ |

Plain-Java users depend on `rest.client.api` + `rest.client.impl`. OSGi users add the Phase-3 bundle on top.

### Core API

All types use Java imports; no FQNs in source.

```java
public interface ModelAtlasClient extends AutoCloseable {

    /** Discover scope names the server exposes. */
    List<String> listScopeNames();

    /** Names of registries available in a given scope. (Phase 5) */
    List<String> listRegistries(String scopeName);

    /** Direct EPackage access. */
    RemoteEPackageProvider ePackages();

    /** Read-only EObject view for a scope; registry is a per-call parameter. (Phase 5) */
    ReadableScopeService<EObject> readOnlyScope(String scopeName);

    /** Trigger a drift check across cached entries. */
    DriftReport checkForDrift();

    /** Subscribe to drift events. */
    AutoCloseable addDriftListener(DriftListener listener);

    /** Build an Atlas-aware ResourceSet for plain-Java consumers. */
    ResourceSet newResourceSet();
}

public interface RemoteEPackageProvider {

    /** Local-first: returns from cache; on miss, fetches from server. */
    Optional<EPackage> getEPackage(String nsUri);

    /** List nsURIs available in the configured scope/stage. */
    List<String> listNsUris(String scopeName);

    /** Eagerly load and register an nsURI; useful for warm-up. */
    Optional<EPackage> ensureAvailable(String nsUri);

    /** Force-refetch one nsUri from the server, ignoring cache. */
    Optional<EPackage> refresh(String nsUri);
}

public interface DriftListener {
    void onPackageChanged(String nsUri, EPackage newPackage);
    void onPackageRemoved(String nsUri);
    void onObjectChanged(String scope, String registry, String objectId);   // Phase 5
    void onObjectRemoved(String scope, String registry, String objectId);   // Phase 5
}
```

Errors are signalled via a small typed hierarchy (`ModelAtlasClientException`, `NotFoundException`, `TransportException`).

### Plain-Java builder

```java
ModelAtlasClient client = ModelAtlasClient.builder()
    .baseUri(URI.create("https://atlas.example.org/atlas"))
    .auth(BearerToken.of(System.getenv("ATLAS_TOKEN")))
    .cache(CacheSpec.inMemory(500))
    .build();

EPackage pkg = client.ePackages()
    .getEPackage("https://eclipse.dev/fennec/jena/cocl/1.0")
    .orElseThrow();

ResourceSet rs = client.newResourceSet();
rs.getResource(URI.createURI("file:/some/instance.xmi"), true);
// any unknown nsURI in that resource resolves through the Atlas client
```

The plain-Java client never mutates `EPackage.Registry.INSTANCE` automatically; the caller decides via `register.in.global.registry`.

### REST mapping

| Client method | HTTP | Path |
|---|---|---|
| `listScopeNames()` | GET | `/scopes` |
| `listRegistries(s)` | GET | `/scopes/{s}` (Phase 5) |
| `ePackages.listNsUris(s)` | GET | `/{s}/schema` |
| `ePackages.getEPackage(nsUri)` (cache miss / conditional) | GET | `/{s}/schema/stages/released/content?nsUri=` (with `If-None-Match` if cached) |
| `readOnlyScope(s).listObjectIds(r)` (Phase 5) | GET | `/{s}/registries/{r}` (final-stage listing) |
| `readOnlyScope(s).get(r, objectId)` (Phase 5) | GET | `/{s}/registries/{r}/content?objectId=` (stage-free final-stage content, P5-0) |
| drift watcher (per scope) | HEAD | `/{s}` (with `If-None-Match`) |

The client uses the *released* stage as the default view. Other stages are workflow concerns; if a future read-only consumer needs draft-stage access, a configurable per-(scope, registry) `view` can be added.

`ePackages.getEPackage(nsUri)` resolves without explicit scope context by walking `scope.allow.list` in order; first hit wins. Configurable via `default.scope` and `scope.allow.list`.

### Caching

- **Cache key.** EPackages: `nsUri`. EObjects: `(scope, registry, objectId)`.
- **Cache value.** Deserialized `EPackage` / `EObject`, plus the last-seen `ETag` and `Last-Modified` (kept in `rest.client.impl` only — never exposed via `rest.client.api`).
- **Eviction.** `CacheSpec` parameters: max entries (LRU), TTL. Disk caching is optional; raw bytes stored addressed by ETag.

### ETag-based drift detection (client side)

Two paths, both built on Phase 1's server support:

1. **Per-request conditional GET.** On a cache hit that is past TTL or explicitly revalidated, the client issues `GET ... If-None-Match: "<etag>"`. `304` → cache entry stays valid; `200` → cache entry replaced.
2. **Background scope watcher.** A scheduled task (`drift.check.interval.ms`, default 5 min, `0` disables) walks each configured scope and issues a single `HEAD /{scope}` with the cached scope ETag. On `304` nothing happens; on `200` the response's `Atlas-Changed-NsUris` / `Atlas-Changed-Objects` headers tell the client *exactly* which entries to invalidate.

On invalidation, the affected cache entry is dropped and `DriftListener` events fire. In OSGi (Phase 3) the registrar additionally re-publishes the affected service.

### Delegating `EPackage.Registry` and Atlas-aware `ResourceSet`

A typical EMF consumer doesn't go through `EPackage.Registry.INSTANCE` directly — it loads an XMI/JSON resource into a `ResourceSet`, EMF tries to resolve referenced EPackages via the `ResourceSet`'s `getPackageRegistry()`, and on miss the load fails. The clean integration point is therefore `ResourceSet.getPackageRegistry()`.

The library provides an `AtlasDelegatingPackageRegistry` that:

1. Forwards `getEPackage(nsURI)` to a primary registry (the framework one, or `EPackage.Registry.INSTANCE`).
2. On `null`, calls `RemoteEPackageProvider.ensureAvailable(nsURI)`. If the client returns a package, the registry caches it and returns it; subsequent lookups are direct hits.
3. Subscribes to drift events to evict stale entries.

This is the same pattern `DelegatingEPackageRegistry` in `emf.osgi` already uses — we just plug an Atlas-aware delegate behind it.

`ModelAtlasClient.newResourceSet()` returns a `ResourceSetImpl` with this delegating registry pre-installed. That is the one-liner for plain-Java consumers; nothing else is required.

#### Why `URIConverter` / URI mappings don't fit

`URIConverter.URIMap` rewrites one URI to another at the URI level. It works well for "physical URI for logical URI" indirection, but it does **not** add a behavioural fallback when an nsURI is missing from the package registry: nsURIs in a `ResourceSet`'s package registry are keys, not URIs being resolved by `URIConverter`. URI maps are static tables; we want a behavioural lookup.

### Configuration (programmatic + ConfigAdmin in Phase 3)

The configuration model is shared between plain-Java and OSGi. In plain Java it is set via the builder; in OSGi (Phase 3) the same fields come from ConfigAdmin.

| Property | Type | Default | Notes |
|---|---|---|---|
| `base.uri` | URI | — | required |
| `connect.timeout.ms` | int | 5_000 | |
| `read.timeout.ms` | int | 30_000 | |
| `mode` | enum | `LAZY` | `EAGER` / `LAZY` / `HYBRID` |
| `eager.scopes` | String[] | `[]` | EAGER/HYBRID: scopes to pre-fetch. Empty + EAGER = all configured. |
| `eager.stages` | String[] | `["released"]` | EAGER/HYBRID: stages to pre-fetch from each scope. |
| `eager.nsuri.allow.list` | String[] | `[]` | HYBRID: nsURIs to fetch eagerly. |
| `mode.strict` | boolean | `false` | If `true`, EAGER fails activation when the server is unreachable. |
| `nsuri.allow.list` | String[] | `[]` | If non-empty, only these nsURIs are publishable. |
| `nsuri.deny.list` | String[] | `[]` | nsURIs that are never publishable, even if returned by the server. |
| `force.remote` | boolean | `false` | If `true`, prefer remote EPackage over a same-nsURI local one. (Effects detailed in Phase 3.) |
| `register.in.global.registry` | boolean | `false` | Mirror published EPackages into `EPackage.Registry.INSTANCE`. |
| `drift.check.interval.ms` | int | 300_000 | `0` disables |
| `scope.allow.list` | String[] | `[]` | empty = all scopes |
| `default.scope` | String | — | for anonymous EPackage lookup |
| `cache.max.entries` | int | 500 | |
| `cache.ttl.ms` | int | 0 | `0` = no TTL |
| `cache.disk.dir` | String | — | empty = in-memory only |
| `auth.type` | enum | `none` | `none` / `bearer` / `mtls` |
| `auth.token.env` | String | — | env var holding the bearer token |

Allow/deny lists operate on exact nsURI matches; pattern matching is intentionally out of scope for v1.

### Authentication

Pluggable on the Jakarta RS `Client`:

- **Bearer token.** `Authorization: Bearer <token>`, token sourced from `auth.token.env`.
- **mTLS.** `KeyStore` / `TrustStore` configured on the underlying `ClientBuilder`.

Both providers are wired through `JakartaRsClientProvider` so they work identically in plain Java and the OSGi Whiteboard.

---

## Phase 3 — OSGi Delegate Registry for `emf.osgi`

Phase 3 wraps the Phase-2 core in OSGi DS components and bridges it to `emf.osgi`. The library reuses the exact same Jakarta RS Client, cache, and drift logic from Phase 2 — only the publishing surface and configuration source change.

### Bundle introduced in this phase

| Bundle | Purpose | Key deps |
|---|---|---|
| `org.eclipse.fennec.model.atlas.rest.client.osgi` | DS components: configuration, EPackage publishing, ResourceSet integration. Reuses Phase-2 core. | impl, emf.osgi.api, OSGi DS, ConfigAdmin |

### emf.osgi service-property contract

Per remote EPackage the client registers three OSGi services with **identical** service properties:

- `EPackageConfigurator` (per `org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator`) — picked up by `emf.osgi`'s `DefaultEPackageRegistryComponent` to populate the local `EPackage.Registry`;
- `EPackage` — the package itself;
- `EFactory` — its factory.

Constants from `org.eclipse.fennec.emf.osgi.constants.EMFNamespaces`. **Always use the constants in code:**

| Constant | Literal | Set to |
|---|---|---|
| `EMF_MODEL_NSURI` | `emf.nsURI` | The remote EPackage's nsURI |
| `EMF_NAME` | `emf.name` | The EPackage name |
| `EMF_MODEL_VERSION` | `emf.version` | Version (from `ObjectMetadata.version`, falling back to URI parsing) |
| `EMF_MODEL_FILE_EXT` | `emf.fileExtension` | `ecore` |
| `EMF_MODEL_SCOPE` | `emf.model.scope` | `EMF_MODEL_SCOPE_RESOURCE_SET` (this is the EMF-OSGi scope concept; **unrelated** to the Atlas scope) |
| `EMF_MODEL_REGISTRATION` | `emf.registration` | `MODEL_REGISTRATION_DYNAMIC` |

Plus origin properties (constants in `rest.client.api`):

| Property | Type | Example | Purpose |
|---|---|---|---|
| `atlas.remote` | boolean | `true` | Marks the EPackage as fetched from a remote Atlas. Filters: `(atlas.remote=true)` / `(!(atlas.remote=true))`. |
| `atlas.scope` | String | `jena` | Atlas scope this came from. Distinct from `emf.model.scope` to avoid the naming clash. |
| `atlas.stage` | String | `released` | Stage on the server when fetched. |
| `atlas.base.uri` | String | `https://atlas.example.org/atlas` | Server URI; useful when several Atlas instances are connected. |

The origin properties are not configurable — they are always set on every published service.

### Resolution modes

Configured via `mode` in `ClientConfiguration`:

**EAGER** — On startup, the client lists EPackages in configured scopes & stages, downloads them, and registers them as OSGi services immediately. The local framework "looks like" the Atlas at activation time. Drift detection keeps it fresh.

**LAZY** *(default)* — Nothing is fetched at startup. Demand triggers a fetch:

1. A consumer doing a service lookup with `target="(emf.nsURI=<X>)"` finds nothing — the client sees the unsatisfied requirement (via a `ServiceListener` on `LDAPFilter` requirements, or via `RemoteEPackageProvider.ensureAvailable(nsUri)`).
2. A consumer holding an Atlas-aware `ResourceSet` triggers resolution through the wrapped `EPackage.Registry`.

**HYBRID** — EAGER-load `eager.nsuri.allow.list`; everything else is LAZY.

### Lazy-mode resolution: synchronous wait with timeout

A subtle race exists in LAZY mode: `EPackage.Registry.getEPackage(nsUri)` triggers the Atlas fetch, but the package only becomes resolvable through `EPackage.Registry` *after* `emf.osgi`'s `DefaultEPackageRegistryComponent` has bound the newly registered `EPackageConfigurator` and replayed it into the registry. That binding is **asynchronous**.

To shield consumers from this, the LAZY trigger inside the delegating registry **blocks synchronously** until the package is observable via the framework registry, with a configurable timeout.

```java
@Override
public EPackage getEPackage(String nsURI) {
    EPackage local = primary.getEPackage(nsURI);
    if (local != null) return local;

    // 1. Fetch from Atlas (returns the deserialized EPackage immediately).
    Optional<EPackage> fetched = remote.ensureAvailable(nsURI);
    if (fetched.isEmpty()) return null;

    // 2. Register OSGi services (EPackageConfigurator + EPackage + EFactory).
    publisher.publish(fetched.get(), originProperties(nsURI));

    // 3. Wait until DefaultEPackageRegistryComponent has propagated the
    //    configurator into EPackage.Registry. Times out after lazy.resolve.timeout.ms.
    return waitUntilVisibleInRegistry(nsURI, lazyResolveTimeoutMs);
}
```

If the timeout expires the call returns `null` and a warning is logged; the next call will see the package once propagation completes. Default timeout: 5 s. Configurable via `lazy.resolve.timeout.ms`.

### Local-first behaviour

Whichever mode: before publishing a remote EPackage, the client checks whether an `EPackage` *or* `EPackageConfigurator` for the same nsURI is already registered. If yes, the remote one is **suppressed**. The client subscribes to local service events; if a local one disappears later, suppressed remote candidates can be (re)published.

Shipping a model bundle locally always overrides what the Atlas would say — unless `force.remote=true`.

### `force.remote=true`

| Setting | Behaviour |
|---|---|
| `force.remote=false` *(default)* | Local-first: remote is suppressed when local exists. |
| `force.remote=true` | Remote is published even when local exists, with high `service.ranking`. Additionally, on startup, every locally registered EPackage is checked against the Atlas; if the Atlas has a newer version, the local registration is preempted by the remote. |

**Honest caveat for `force.remote=true`:** `emf.osgi`'s `DefaultEPackageRegistryComponent` does not consult `service.ranking` when populating `EPackage.Registry`. It iterates bound `EPackageConfigurator`s in bind order and lets `registry.put(...)` win last. Consequence:

- Direct service-lookup consumers that respect `service.ranking` → remote reliably wins.
- `EPackage.Registry.getEPackage(nsURI)` consumers → bind-order-dependent.

For deterministic registry-level override, the *Recommendation: emf.osgi ranking-aware aggregator* below applies. In the meantime `force.remote=true` is best-effort and documented as such; the typical safe setup is "no local bundle for the same nsURI + `force.remote=true`".

### Atomic substitution on drift refresh

When the drift watcher detects that an nsURI on the Atlas has changed and the local OSGi service must be replaced, the substitution must be **atomic per nsURI**: no `ResourceSet` may observe a stale package after the new one has been announced, and no `ResourceSet` already loading should mix versions.

The publisher holds a per-nsURI lock used in two places:

1. **Service swap.** Acquire lock → unregister old `EPackageConfigurator` / `EPackage` / `EFactory` → register new ones with same properties → release lock. While the lock is held, lookups via the delegating registry block (briefly).
2. **Cache update.** Cache entry is replaced under the same lock so a `getEPackage` racing the swap either sees the old set or the new set, never a half-state.

Locks are nsURI-scoped; substitutions of unrelated packages run in parallel.

> Open question: when two packages drift together and reference each other (Jürgen's Test-Case below), do we serialize their swaps or coordinate one combined swap? Tracked under *Open Questions*.

### `ResourceSetConfigurator` integration

The `rest.client.osgi` bundle registers a `ResourceSetConfigurator` (per `org.eclipse.fennec.emf.osgi.configurator.ResourceSetConfigurator`) so every `ResourceSet` produced by the framework's `ResourceSetFactory` is post-processed: its package registry is wrapped in `AtlasDelegatingPackageRegistry`. Consumers calling `resourceSetFactory.createResourceSet()` automatically get fallback resolution — no API change.

A boolean configuration property `resource.set.fallback=true` (default `true`) can disable the wrapper for runtimes that explicitly do not want implicit network calls during resource loads.

### `EPackage.Registry.INSTANCE` mirroring

For plain-Java consumers and legacy code reaching the EMF singleton, the OSGi registrar additionally puts fetched EPackages into `EPackage.Registry.INSTANCE` when `register.in.global.registry=true` (default `false`).

### Configuration (ConfigAdmin)

ConfigAdmin PID `org.eclipse.fennec.model.atlas.rest.client`. All Phase-2 properties apply, plus:

| Property | Type | Default | Notes |
|---|---|---|---|
| `lazy.resolve.timeout.ms` | int | 5_000 | Synchronous wait in LAZY mode. |
| `resource.set.fallback` | boolean | `true` | Wrap framework-produced ResourceSets. |

Multiple PIDs (factory-configurable) allow connecting to several Atlas instances.

### Recommendation: `emf.osgi` ranking-aware aggregator (follow-up)

> In `DefaultEPackageRegistryComponent`, when multiple `EPackageConfigurator` services target the same nsURI, apply them in descending `service.ranking` order so the highest-ranked configurator wins the registry slot.

Additive (no behavioural change when no ranking is set), small (one sort + one dedup pass), and benefits any future "two configurators for the same nsURI" scenario, not only the remote-override case. Tracked as a separate PR against `emf.osgi`.

---

## Phase 4 — Server-Side Interface Split (Pre-Work for Phase 5)

This is **server-side work** that must land before Phase 5 starts. Without it the client cannot fulfil Goal 1 (contract-identical surface) for EObject registries.

### What changes

**A new bundle `org.eclipse.fennec.model.atlas.scope.api`** (parallel to `schema.registry.api`, `mediatypes.api`) that contains the read-only contract, depending only on EMF core:

```java
package org.eclipse.fennec.model.atlas.scope;

public interface ScopedEObjectsRegistry<T extends EObject> {

    String  getScopeName();        // e.g. "jena"
    String  getRegistryName();     // e.g. "cocl"
    EClass  getRootEClass();       // type constraint of this registry

    Optional<T> get(String objectId);
    List<String> listObjectIds();
    List<T>      listAll();        // resolved EObjects, default view
    Stream<T>    stream();         // for large registries

    /** True if listing reflects parent scopes (read-through). */
    boolean isInheritingFromParentScope();
}
```

OSGi service properties (constants in the same bundle):

| Property | Value example | Purpose |
|---|---|---|
| `atlas.scope` | `jena` | Scope this registry belongs to |
| `atlas.registry` | `cocl` | Registry name |
| `atlas.view` | `released` | Which stage view is exposed (default `released`) |
| `atlas.remote` | `true`/absent | Whether the publication came from a remote client |

**The existing `workflow.api`** is refactored:

```java
package org.eclipse.fennec.model.atlas.wf.workflowapi;

public interface WritableScopeService<T extends EObject>
        extends ScopedEObjectsRegistry<T> {

    Promise<ObjectMetadata> upload(String stage, T obj, ObjectMetadata md);
    Promise<ObjectMetadata> updateInStage(String stage, T obj, String objectId, String version);
    Promise<Boolean>        deleteFromStage(String stage, String objectId);
    ObjectMetadata          transitionToStage(String objectId, String fromStage, String toStage);

    ObjectMetadata          getMetadataFromStage(String stage, String objectId);
    List<ObjectMetadata>    listInStage(String stage);
    List<ObjectMetadata>    listAllStages();
}
```

The existing `ScopeService<T>` stays as a typedef for the next minor release, deprecated, redirecting consumers to `WritableScopeService<T>` (writes) or `ScopedEObjectsRegistry<T>` (reads) depending on what they actually use.

### Why no `ObjectMetadata` on the read-only contract

`ObjectMetadata` carries `contentHash`, `version`, `lastChangeTime`, `properties` (storage indexing keys), stage transition history, etc. — none of which a typical reader needs. They are wire-format and storage concerns. Leaking them through the read-only interface means:

- consumers couple to a wire/storage type they should not depend on;
- a remote client must materialise it identically to the server even when the data is not really meaningful client-side (e.g. storage properties);
- migration of the storage format becomes a public-API concern.

Drift detection in the client uses `ObjectMetadata.contentHash` (and ETags, see Phase 1) over the wire, but only inside `rest.client.impl`. The api bundle never exports it.

### `ScopeServiceCollector`

The server-side `ScopeServiceCollector` today keys on `scope.name`. After the split it remains untouched (it serves the *workflow* facets), but a parallel **`ScopedEObjectsRegistryCollector`** is added (or generalised) that iterates services keyed by `(atlas.scope, atlas.registry)` — used by validation and any other read-only consumer. The two collectors are independent: shipping the workflow API is no longer a prerequisite for shipping a consumer that only reads.

### REST resource split (deferred but planned)

For full deployment-time security, the REST surface should also separate read-only from workflow:

- `ObjectRegistryReadOnlyResource` — only `GET` endpoints.
- `ObjectRegistryWorkflowResource` — `POST`/`PUT`/`DELETE`/transition.

Mounting only the read-only resource on a public Atlas instance, while a private one offers the workflow resource, is then a deployment choice. Not part of v1.

### Migration of existing consumers

- `org.eclipse.fennec.model.atlas.validation`: change `@Reference` from `ScopeService<?>` to `ScopedEObjectsRegistry<?>`. No other behavioural change.
- `org.eclipse.fennec.model.atlas.workflow.ScopeServiceImpl`: now publishes both `ScopedEObjectsRegistry<EObject>` (per scope+registry) and `WritableScopeService<EObject>` (per scope). Both as separate OSGi service registrations.
- REST endpoints continue to work via the existing `ScopeService<T>` consumption path (now an alias for `WritableScopeService<T>`).

---

## Phase 5 — EObject-Registry Client

Phase 5 extends the client (both plain-Java and OSGi variants) with the `scope.api`
`ReadableScopeService<EObject>` per **scope** (registry is a method parameter). It depends on
Phase 4 having shipped `scope.api`.

> **Reframed (2026-06-12).** Phase 4 shipped a per-scope `ReadableScopeService<T>` rather than
> the per-`(scope, registry)` `ScopedEObjectsRegistry<T>` this section originally described.
> Phase 5 mirrors that contract exactly. A new server endpoint (P5-0) is required because the
> only single-object content endpoint requires a stage name in the path, and final-stage names
> are user-defined.

### What gets added

- **Server (P5-0)** — a stage-free final-stage content endpoint
  `GET /{s}/registries/{r}/content?objectId=`, mirroring the existing stage-free final-stage
  *listing* at `GET /{s}/registries/{r}`. Delegates to `ReadableScopeService.get(registry, objectId)`
  with ETag from `getMetadataFromFinalStageForRegistry(...)`.

- `rest.client.impl` gains `RemoteReadableScopeService implements ReadableScopeService<EObject>`
  (one per scope) that:
  - lists object IDs via `GET /{s}/registries/{r}` (final-stage listing),
  - fetches single objects via `GET /{s}/registries/{r}/content?objectId=` (P5-0), using the same
    conditional-GET / cache machinery as EPackages (cache key `(scope, registry, objectId)`),
  - exposes `getScopeInfo()` via `GET /scopes/{s}`,
  - participates in the scope-level drift watcher (Phase 1 HEAD response surfaces changed
    `(registry, objectId)` pairs).

- `rest.client.api`'s `ModelAtlasClient.readOnlyScope(scope)` and `listRegistries(scope)` become
  functional.

- `rest.client.osgi` publishes one `ReadableScopeService<EObject>` OSGi service per **scope**,
  with the property contract from Phase 4 (`atlas.view` discussed below):

  ```
  atlas.scope=<scopeName>     # the collector key
  atlas.remote=true
  # + whatever else the in-process ScopeServiceImpl stamps, mirrored for shape-identical
  #   publications. atlas.view is advisory only (no consumer filters on it; reads always
  #   target the final stage, resolved server-side) — mirrored if the server stamps it,
  #   otherwise omitted. See P5-7 for retiring the stage name from the EPackage path too.
  ```

  Consumers (e.g. validation, UC-1) look up by service filter:

  ```java
  @Reference(target = "(atlas.scope=jena)")
  ReadableScopeService<EObject> jenaScope;
  // ...then jenaScope.get("cocl", objectId)
  ```

  This is the **same lookup that works against the in-process server-side publication** after
  Phase 4 — that is the symmetry payoff.

### Object identity and cross-references

EObjects fetched via `get(...)` are detached copies (no shared `Resource`). The contract is read-only by interface; mutations are not expected to flow back. Cross-references between fetched EObjects resolve through the Atlas-aware `ResourceSet` (Phase 2/3), so an object referencing another via its URI lazily triggers a fetch of the referenced one.

> **Resolved (P5-6):** cache hits return the **same instance** within one client lifetime
> (the recommendation). `==` is meaningful only within one fetch session; the interface is
> read-only so callers must not mutate returned objects. Identity is keyed per
> `(scope, registry, objectId)` — distinct ids never alias. A `304` revalidation keeps the same
> instance. Verified by `RemoteReadableScopeServiceTest` (`get_cacheHit_returnsSameInstance_*`,
> `get_distinctObjectIds_returnDistinctInstances`, `get_postTtlRevalidation_304_keepsSameInstance`).

### Validation service migration as acceptance test

Validation service tested running against both in-process and remote registries with no consumer-side code change. That is the proof that Goal 1 is met end-to-end.

---

## Bundle / Module Structure (across all phases)

| Bundle | Phase | Side | Purpose | Key deps |
|---|---|---|---|---|
| `org.eclipse.fennec.model.atlas.rest.application` (modifications) | 1 | server | ETag, conditional GET, scope HEAD | jakarta.ws.rs |
| `org.eclipse.fennec.model.atlas.rest.tests` (extensions) | 1 | server | ETag/304/412 tests; scope-HEAD tests | jakarta.ws.rs.client |
| `org.eclipse.fennec.model.atlas.rest.client.api` | 2 | client | Public client interfaces, configuration types, `JakartaRsClientProvider` SPI | EMF |
| `org.eclipse.fennec.model.atlas.rest.client.impl` | 2 / 5 | client | Jakarta RS Client logic, caching, drift, delegating registry; EObject registry impl in 5 | api, jakarta.ws.rs, EMF, emf.common, rest.model |
| `org.eclipse.fennec.model.atlas.rest.client.tests` | 2+ | client | Integration tests | testcontainers, JUnit 5 |
| `org.eclipse.fennec.model.atlas.rest.client.osgi` | 3 / 5 | client | DS components: publishing, ResourceSet integration, EObject registry publication in 5 | impl, emf.osgi.api, OSGi DS, ConfigAdmin |
| `org.eclipse.fennec.model.atlas.scope.api` | 4 | server | `ScopedEObjectsRegistry<T>` contract | EMF core |
| `org.eclipse.fennec.model.atlas.workflow` (refactor) | 4 | server | `WritableScopeService<T> extends ScopedEObjectsRegistry<T>`; impl publishes both | scope.api, EMF |
| `org.eclipse.fennec.model.atlas.rest.client.workflow.*` | future | client | Separate workflow client, separate design doc. | — |

---

## Open Questions / Risks

1. **Default scope for anonymous EPackage lookups.** Most existing consumers ask `EPackage.Registry.INSTANCE.getEPackage(nsUri)` without scope context. Hard-require a `default.scope`, or walk all configured scopes? Recommendation: configurable allow-list, deterministic order.
2. **EObject identity across fetches.** Cache hits return the same instance vs. always a fresh copy. Recommendation: same instance, document read-only semantics.
3. **Cross-references between EObjects.** Lazy proxy resolution via the Atlas-aware ResourceSet — exactly why the ResourceSet integration exists.
4. **Drift in `EPackage.Registry.INSTANCE`.** Replacing an EPackage in the global mutable map at runtime can disturb consumers that captured references. Documented and opt-in (`register.in.global.registry=true`).
5. **`force.remote=true` registry-level determinism.** Today's `emf.osgi` aggregator is bind-order, last-write-wins; we can guarantee remote precedence for direct service-lookup consumers via `service.ranking`, but not for `EPackage.Registry.getEPackage(nsURI)` consumers. Decision: ship Phase 3 best-effort, track the ranking-aware aggregator as a follow-up against `emf.osgi`.
6. **EAGER + unreachable server.** Strict-fail vs. best-effort. Default best-effort, `mode.strict=true` opt-in.
7. **Coordinated drift swap of mutually-referencing EPackages.** When two packages reference each other and both drift together, do we serialize their swaps (risk: transient mismatched state) or coordinate one combined critical section? Likely the second; needs spec in Phase 3 implementation.
8. **Validation service migration.** Verify `validation` only depends on read methods of `ScopeService<?>`. If it depends on workflow-only methods, those need to stay in the writable interface and `validation` cannot be made workflow-independent. Action: audit before Phase 4.
9. **`atlas.scope` vs `scope.name`.** The existing server-side `ScopeServiceCollector` keys on `scope.name`. The new read-only registries key on `atlas.scope` + `atlas.registry`. Two different services, two different key conventions — clean. Worth a sanity check that no consumer mixes them up.
10. **Workflow client coupling.** When the workflow client is designed, it must extend, not duplicate, the read-only client's capabilities — sharing the same `scope.api` types and the same caching where it makes sense.

---

## Test Cases

### Phase 1 — Server

- ETag emitted on content GET; `If-None-Match` matching → `304`, mismatching → `200` with new ETag.
- `Vary: Accept` present; the same nsURI with `Accept: application/xml` and `Accept: application/json` produce different ETags.
- Scope-level HEAD: stale `If-None-Match` returns `200` with `Atlas-Changed-NsUris` header listing exactly the changed nsURIs.
- `If-Match` on workflow write: matching → success with new ETag; mismatching → `412`.

### Phase 2 — Plain Java

- `getEPackage(nsUri)` cache miss → GET, hit → no GET. After TTL, conditional GET with `If-None-Match` → `304` keeps cache, `200` replaces.
- `newResourceSet().getResource(...)` resolves an unknown nsURI through the Atlas client.
- Allow-list / deny-list filtering: a denied nsURI is never returned.
- Drift listener fires `onPackageChanged` after the watcher detects a scope change.

### Phase 3 — OSGi

- LAZY mode: `EPackage.Registry.getEPackage(unknownNsUri)` blocks until the package is observable, returns it within the timeout; on timeout returns `null` and logs.
- Local-first: a locally registered EPackage with the same nsURI suppresses the remote registration.
- `force.remote=true`: on startup, locally registered EPackages get superseded by Atlas-side newer versions (verified through service-listener observation of unregister/register events).
- Atomic substitution: under load, a `ResourceSet` lookup during a drift swap never observes a missing-then-new sequence; either the old or the new package is returned.

### Phase 5 — EObject Registry

- `@Reference(target=…)` lookup against the OSGi service publication returns the same registry shape as the in-process server-side publication.
- Validation service: same code passes against in-process and remote backends.

### Jürgen's Test Cases

These are EMF-level edge cases that stress the delegating registry / ResourceSet integration — they originated in `fennec.emf` work but are directly relevant to Phase 3 correctness.

- **Two interdependent EPackages where one is unloaded → proxy.** When the unloaded package is referenced again, EMF re-resolves it through `EPackage.Registry`. Today this can fail because a registry entry exists with a `Resource` (for the nsURI) but no `ResourceSet`. The Atlas-aware delegating registry must ensure a `ResourceSet` is attached, or fall back to a fresh fetch that brings a properly-rooted package.
- **Mutating EPackages in the registry must be detectable.** Recommendation for `fennec.emf`: add an EMF adapter that throws when somebody re-parents an `EPackage` / `EClass` / `EStructuralFeature` (i.e. moves it into a different container). The adapter belongs in `fennec.emf`, not in this client; it is included here as a recommendation because it directly protects every consumer of remote-fetched packages.

---

## References

### Server (existing)
- `org.eclipse.fennec.model.atlas.rest.application/.../resource/SchemaPackagesResource.java` — EPackage REST endpoints
- `org.eclipse.fennec.model.atlas.rest.application/.../resource/ObjectRegistryResource.java` — EObject REST endpoints
- `org.eclipse.fennec.model.atlas.rest.application/.../resource/ScopesResource.java` — scope discovery
- `org.eclipse.fennec.model.atlas.workflow/src-wf-api/.../ScopeService.java` — current combined scope contract (to be split in Phase 4)
- `org.eclipse.fennec.model.atlas.workflow/src-wf-api/.../RegistryService.java` — registry contract
- `org.eclipse.fennec.model.atlas.workflow/src-wf-api/.../EObjectWorkflowService.java` — workflow contract
- `org.eclipse.fennec.model.atlas.workflow/src/.../registration/DynamicEPackageConfigurator.java` — service-property contract
- `org.eclipse.fennec.model.atlas.workflow/src/.../registration/DynamicEPackageRegistrationService.java` — multi-service registration pattern
- `org.eclipse.fennec.model.atlas.rest.tests/...` — Jakarta RS Client usage examples

### emf.osgi
- `/opt/git/emf.osgi/.../EMFNamespaces.java` — canonical OSGi service-property constants
- `/opt/git/emf.osgi/.../EPackageConfigurator.java` — the configurator interface the client implements
- `/opt/git/emf.osgi/.../components/DefaultEPackageRegistryComponent.java` — the framework aggregator (relevant for `force.remote` and the ranking-aware enhancement recommendation)
- `/opt/git/emf.osgi/.../configurator/ResourceSetConfigurator.java` — the configurator the client uses to wrap ResourceSets with Atlas fallback
- `/opt/git/emf.osgi/.../helper/DelegatingEPackageRegistry.java` — pattern for a delegating package registry
