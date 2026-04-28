# Design: Model Atlas Read-Only Client (Plain Java + OSGi)

## Problem Statement

Today, every consumer of the Fennec Model Atlas — for example a validation service, a code generator, or a data pipeline — needs the EPackages and the registered EObjects (e.g. the `cocl` and `datagen` registries in the `jena` scope) to be physically present in the JVM that runs them. They must be either shipped as bundles next to the consumer, or run inside the same OSGi runtime as the Atlas itself.

This couples remote consumers tightly to the Atlas deployment. A validation service that depends on a scope's EObject registry cannot easily be embedded in a third-party application that simply has network access to an Atlas instance.

A second, related problem is **interface asymmetry**. The server today exposes a single `ScopeService<T>` that mixes read operations (used by validation, generation, lookup) with write operations (upload, transition, delete — workflow operations). A consumer that only wants to read is forced to depend on the workflow contract, which both bloats its dependency surface and makes it harder to ship a runtime that lacks workflow capabilities — even though the same code paths run.

This design tackles both problems together: a read-only client, *and* a server-side interface split so that client and server expose the **same** read-only Java contract.

## Goals

1. Provide a Java client library that exposes the Atlas via a **local OSGi service surface that is contract-identical to the server side** for the read paths — so that a consumer (e.g. validation) cannot tell whether the underlying registry is in-process or remote.
2. **Split the existing `ScopeService<T>` into a read-only base interface and a writable workflow extension.** The read-only base is what both server and client publish; the writable extension is workflow-only and lives on the server (and, in the future, in a separate workflow client).
3. Make the client usable in **plain Java** (no OSGi runtime) through a programmatic API.
4. Resolve EPackages and EObjects **local-first**: only fall back to the remote Atlas when nothing local matches.
5. **Cache** remote EPackages and EObjects locally and re-publish them as OSGi services so existing consumers work unchanged.
6. **Detect drift**: provide a way to discover that a newer/different version exists on the server, optionally with background refresh.
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
The client periodically checks `contentHash` for each cached entry. On change, it refreshes the cache and **re-publishes** the OSGi service so injected consumers receive an updated reference (or are notified via service events).

## Architecture Overview

```
                      +---------------------------------------+
                      | Model Atlas Server                    |
                      |  REST: SchemaPackagesResource,        |
                      |        ObjectRegistryResource,        |
                      |        ScopesResource                 |
                      |  Java: WritableScopeService<T>        |
                      |        extends ScopedEObjectsRegistry |
                      +-------------------+-------------------+
                                          | HTTPS / Jakarta RS
                                          v
+-----------------------------------------+--------------------------+
|  scope.api  (NEW, server-side bundle)   |                          |
|    ScopedEObjectsRegistry<T>            |  shared by server and    |
|    EObjectInfo (small DTO if needed)    |  client implementations  |
|    OSGi service-property constants      |                          |
+-----------------------------------------+--------------------------+
                                          ^
                                          | implements
                                          |
+-----------------------------------------+--------------------------+
|  rest.client.api  (interfaces, no OSGi)                            |
|    ModelAtlasClient        — top-level entry, plain Java           |
|    RemoteEPackageProvider  — getEPackage(nsUri), ensureAvailable() |
|    DriftListener           — onPackageChanged, onObjectChanged     |
|    ResourceSetIntegration  — Atlas-aware ResourceSet helpers       |
|  (re-exports ScopedEObjectsRegistry<T> from scope.api)             |
+-----------------------------------------+--------------------------+
                                          ^
                                          | implements
                                          |
+-----------------------------------------+--------------------------+
|  rest.client.impl  (Jakarta RS, EMF)                               |
|    HttpModelAtlasClient                                            |
|    EPackageCache (in-memory + optional disk)                       |
|    EObjectCache  (per scope/registry/objectId)                     |
|    ScopedEObjectsRegistryImpl  (per scope+registry)                |
|    ObjectMetadata used INTERNALLY for drift detection only         |
+-----------------------------------------+--------------------------+
                                          ^
                                          | DS @Component wraps
                                          |
+-----------------------------------------+--------------------------+
|  rest.client.osgi  (DS components, ConfigAdmin)                    |
|    ClientConfiguration   (mode, scopes, force.remote, ...)         |
|    RemoteEPackageRegistrar                                         |
|        - registers EPackage / EFactory / EPackageConfigurator      |
|          with EMFNamespaces.* + atlas.* origin properties          |
|    ScopedEObjectsRegistryPublisher                                 |
|        - registers ScopedEObjectsRegistry<EObject> per scope+reg   |
|    EagerLoader / LazyResolver                                      |
|    ResourceSetConfigurator (Atlas fallback for unknown nsURIs)     |
+--------------------------------------------------------------------+
```

### How client and server become symmetric

- The new `scope.api` bundle defines `ScopedEObjectsRegistry<T extends EObject>`.
- The server's existing `ScopeServiceImpl` is refactored to also publish `ScopedEObjectsRegistry<EObject>` services (one per scope+registry). The workflow operations move into a `WritableScopeService<T>` interface that **extends** `ScopedEObjectsRegistry<T>` and lives in `workflow.api`.
- The client's impl publishes the same `ScopedEObjectsRegistry<EObject>` services — but never the writable extension. There is *no* client-side type for writes; that is by design.
- A consumer that today injects `@Reference ScopeService<?>` and only uses read methods migrates to `@Reference ScopedEObjectsRegistry<?>`. After the migration, that consumer works identically against an in-process Atlas and against a remote one.

## Server-Side Interface Split (Prerequisite)

This is **server-side work** that must land before, or alongside, the client. Without it the client cannot fulfil Goal 1 (contract-identical surface).

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

    // staging/listing details that workflow tooling cares about
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

Drift detection in the client uses `ObjectMetadata.contentHash` over the wire, but only inside `rest.client.impl`. The api bundle never exports it.

### `ScopeServiceCollector`

The server-side `ScopeServiceCollector` today keys on `scope.name`. After the split it remains untouched (it serves the *workflow* facets), but a parallel **`ScopedEObjectsRegistryCollector`** is added (or generalised) that iterates services keyed by `(atlas.scope, atlas.registry)` — used by validation and any other read-only consumer. The two collectors are independent: shipping the workflow API is no longer a prerequisite for shipping a consumer that only reads.

### REST resource split (deferred but planned)

For full deployment-time security, the REST surface should also separate read-only from workflow:

- `ObjectRegistryReadOnlyResource` — only `GET` endpoints.
- `ObjectRegistryWorkflowResource` — `POST`/`PUT`/`DELETE`/transition.

Mounting only the read-only resource on a public Atlas instance, while a private one offers the workflow resource, is then a deployment choice. This is **not in v1 of this design** — the client only consumes GETs and works against the existing combined resource — but listed as a follow-up that pairs naturally with this work.

### Migration of existing consumers

- `org.eclipse.fennec.model.atlas.validation`: change `@Reference` from `ScopeService<?>` to `ScopedEObjectsRegistry<?>`. No other behavioural change.
- `org.eclipse.fennec.model.atlas.workflow.ScopeServiceImpl`: now publishes both `ScopedEObjectsRegistry<EObject>` (per scope+registry) and `WritableScopeService<EObject>` (per scope). Both as separate OSGi service registrations.
- Rest endpoints continue to work via the existing `ScopeService<T>` consumption path (now an alias for `WritableScopeService<T>`).

## Bundle / Module Structure

| Bundle | Side | Purpose | Key deps |
|---|---|---|---|
| `org.eclipse.fennec.model.atlas.scope.api` | **server (new)** | Read-only `ScopedEObjectsRegistry<T>` contract + property constants. Lightweight, EMF only. | EMF core |
| `org.eclipse.fennec.model.atlas.workflow` (api + impl) | server (refactored) | `WritableScopeService<T>` (extends `ScopedEObjectsRegistry<T>`); existing impl now publishes both. | scope.api, EMF, OSGi promises |
| `org.eclipse.fennec.model.atlas.rest.client.api` | client | `ModelAtlasClient`, `RemoteEPackageProvider`, `DriftListener`, ResourceSet helpers. Re-uses scope.api. No OSGi DS. | scope.api, EMF |
| `org.eclipse.fennec.model.atlas.rest.client.impl` | client | Jakarta RS Client, caching, drift detection, `ScopedEObjectsRegistry<T>` impl. Plain-Java capable. | api, jakarta.ws.rs, EMF, emf.common, rest.model (internal use of `ObjectMetadata`) |
| `org.eclipse.fennec.model.atlas.rest.client.osgi` | client | DS components, registers OSGi services, ResourceSet integration via emf.osgi. | impl, emf.osgi.api, OSGi DS, ConfigAdmin |
| `org.eclipse.fennec.model.atlas.rest.client.tests` | client | Integration tests. | testcontainers, JUnit 5, AssertJ |
| `org.eclipse.fennec.model.atlas.rest.client.workflow.*` | **future** | Separate workflow client, separate design doc. Not part of v1. | (not specified here) |

Plain-Java users depend on `rest.client.api` + `rest.client.impl`. OSGi users add `rest.client.osgi`.

## Core API (`rest.client.api`)

All types use Java imports, never FQNs in source.

```java
public interface ModelAtlasClient extends AutoCloseable {

    /** Discover scope names the server exposes. */
    List<String> listScopeNames();

    /** Names of registries available in a given scope. */
    List<String> listRegistries(String scopeName);

    /** Read-only registry view for one (scope, registry). */
    ScopedEObjectsRegistry<EObject> registry(String scopeName, String registryName);

    /** Direct EPackage access. */
    RemoteEPackageProvider ePackages();

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
    void onObjectChanged(String scope, String registry, String objectId);
    void onObjectRemoved(String scope, String registry, String objectId);
}

public final class DriftReport {
    List<String> changedNsUris;
    List<String> removedNsUris;
    // (scope, registry, objectId) tuples
    List<EObjectKey> changedObjects;
    List<EObjectKey> removedObjects;
}
```

`ScopedEObjectsRegistry<T>` itself comes from the new `scope.api` bundle (see *Server-Side Interface Split* above). The client imports it and provides an implementation.

Errors are signalled via a small typed hierarchy (`ModelAtlasClientException`, `NotFoundException`, `TransportException`).

## REST Mapping

Read-only only:

| Client method | HTTP | Path |
|---|---|---|
| `listScopeNames()` | GET | `/scopes` |
| `listRegistries(s)` | GET | `/scopes/{s}` (read `Scope.registries`) |
| `ePackages.listNsUris(s)` | GET | `/{s}/schema` |
| `ePackages.getEPackage(nsUri)` (cache miss) | GET | `/{s}/schema/stages/released/content?nsUri=` |
| `registry(s,r).listObjectIds()` | GET | `/{s}/registries/{r}` |
| `registry(s,r).get(objectId)` | GET | `/{s}/registries/{r}/stages/released/content?objectId=` |
| internal: `head` for drift | GET | `/{s}/schema/stages/released?nsUri=` (parses metadata) |

Writes (`POST`/`PUT`/`DELETE`/`actions/transition`) are **not consumed by this client** and are out of scope. They are the workflow client's responsibility.

The client uses the *released* stage as the default view. Other stages are workflow concerns; if a future read-only consumer needs draft-stage access, we can add a configurable `view` per (scope, registry) — but only as long as no write semantics leak in.

The `ePackages.getEPackage(nsUri)` call needs a `(scope, stage)` pair to resolve from. Default policy: iterate the configured scope allow-list, look at the released stage of each, first hit wins. Configurable via `default.scope` and `scope.allow.list`.

## Caching & Drift Detection

**Cache key.** EPackages: `nsUri`. EObjects: `(scope, registry, objectId)`.

**Cache value.** The deserialized `EPackage` / `EObject`, plus the last seen `contentHash` / `lastChangeTime` (kept in `rest.client.impl` only — never exposed via `rest.client.api`).

**Eviction.** `CacheSpec` parameters: max entries (LRU), TTL. Disk caching is optional and stores raw XMI bytes addressed by `contentHash`.

**Drift detection.** Two paths:

1. **Eager** — on `getEPackage` / `get`, the client may issue a metadata-only HEAD-style request first and compare hashes. Disabled by default (extra round-trip). The two-call pattern goes away once the server emits ETags (see recommendation below).
2. **Lazy / background** — a single scheduled task (`drift.check.interval.ms`) walks all live cache keys, lists per scope/registry, diffs hashes against cached metadata, and fires `DriftListener` events. Default interval: 5 min. `0` disables the watcher.

**Drift response.** The cache entry is invalidated; on next access it is refetched. In OSGi mode, the registrar reacts by **re-registering** the affected service so DS consumers see the change.

## OSGi Resolution: `emf.osgi` Integration, Modes, Conflict Handling

### Target framework

In OSGi mode the client targets the existing **`emf.osgi`** library (`/opt/git/emf.osgi/`) as its only contract surface. The client registers, per remote EPackage:

- one `EPackageConfigurator` service (per `org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator`) that the framework's `DefaultEPackageRegistryComponent` picks up dynamically and calls back to populate the local `EPackage.Registry`;
- one `EPackage` service exposing the package itself;
- one `EFactory` service.

All three with **identical service properties**.

### Service properties (canonical names)

Constants from `org.eclipse.fennec.emf.osgi.constants.EMFNamespaces`. **Always use the constants in code**:

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

### Resolution modes

Configured via `mode` in `ClientConfiguration`:

**EAGER** — On startup, the client lists EPackages in configured scopes & stages, downloads them, and registers them as OSGi services immediately. The local framework "looks like" the Atlas at activation time. Drift detection keeps it fresh.

**LAZY** *(default)* — Nothing is fetched at startup. Demand triggers a fetch:

  1. A consumer doing a service lookup with `target="(emf.nsURI=<X>)"` finds nothing — the client sees the unsatisfied requirement (via a `ServiceListener` on `LDAPFilter` requirements, or via `RemoteEPackageProvider.ensureAvailable(nsUri)`).
  2. A consumer that holds a reference to an Atlas-aware `ResourceSet` triggers resolution through the wrapped `EPackage.Registry` — see *Resolving Unknown Models in a ResourceSet*.

**HYBRID** — EAGER-load `eager.nsuri.allow.list`; everything else is LAZY.

### Local-first behaviour

Whichever mode: before publishing a remote EPackage, the client checks whether an `EPackage` *or* an `EPackageConfigurator` service for the same nsURI is already registered. If yes, the remote one is **suppressed**. The client subscribes to local service events; if a local one disappears later, suppressed remote candidates can be (re)published.

This keeps the contract obvious: shipping a model bundle locally always overrides whatever the Atlas would say.

### Conflict resolution: `force.remote`

| Setting | Behaviour |
|---|---|
| `force.remote=false` *(default)* | Local-first: remote is suppressed when local exists. |
| `force.remote=true` | Remote is published even when local exists, with high `service.ranking`. |

**Honest caveat for `force.remote=true`:** `emf.osgi`'s `DefaultEPackageRegistryComponent` does not consult `service.ranking` when populating `EPackage.Registry`. It iterates bound `EPackageConfigurator`s in bind order and lets `registry.put(...)` win last. Consequence:

- Direct service-lookup consumers that respect `service.ranking` → remote reliably wins.
- `EPackage.Registry.getEPackage(nsURI)` consumers → bind-order-dependent.

For deterministic registry-level override we recommend a small enhancement to `emf.osgi`: a ranking-aware aggregator (call out in *Recommendation: emf.osgi ranking-aware aggregator* below). In the meantime `force.remote=true` is best-effort and documented as such; the typical safe setup is "no local bundle for the same nsURI + `force.remote=true`".

### `EPackage.Registry.INSTANCE`

For plain-Java consumers and legacy code reaching the EMF singleton, the OSGi registrar additionally puts fetched EPackages into `EPackage.Registry.INSTANCE` when `register.in.global.registry=true` (default `false`).

### Recommendation: `emf.osgi` ranking-aware aggregator (follow-up)

> In `DefaultEPackageRegistryComponent`, when multiple `EPackageConfigurator` services target the same nsURI, apply them in descending `service.ranking` order so the highest-ranked configurator wins the registry slot.

Additive (no behavioural change when no ranking is set), small (one sort + one dedup pass), and benefits any future "two configurators for the same nsURI" scenario, not only the remote-override case.

## Resolving Unknown Models in a ResourceSet

A typical EMF consumer doesn't go through `EPackage.Registry.INSTANCE` directly — it loads an XMI/JSON resource into a `ResourceSet`, EMF tries to resolve referenced EPackages via the `ResourceSet`'s `getPackageRegistry()`, and on miss the load fails. For an Atlas-backed deployment we want a clean fallback: if the package registry doesn't know an nsURI, ask the Atlas client.

### Why `URIConverter` / URI mappings don't fit

`URIConverter.URIMap` rewrites one URI to another at the URI level. It works well for "physical URI for logical URI" indirection — e.g. mapping `platform:/plugin/.../foo.ecore` to a filesystem URI. It does **not** add a behavioural fallback when an nsURI is missing from the package registry, because:

- nsURIs in a `ResourceSet`'s package registry are keys, not URIs being resolved by `URIConverter`. The package registry sits on a different code path.
- Even if we redirected the nsURI to a remote URL, EMF would expect the `Resource.Factory.Registry` to know how to read it. Pre-registering factories for every potential nsURI is unworkable.
- URI maps are static tables; we want a behavioural lookup.

### The right hook: a delegating `EPackage.Registry` on the `ResourceSet`

The clean integration point is `ResourceSet.getPackageRegistry()`. We provide a delegating `EPackage.Registry` that:

1. Forwards `getEPackage(nsURI)` to a primary registry (the framework one, or `EPackage.Registry.INSTANCE`).
2. On `null`, calls `RemoteEPackageProvider.ensureAvailable(nsURI)`. If the client returns a package, the registry caches it locally and returns it; subsequent lookups are direct hits.
3. Optionally subscribes to drift events to evict stale entries.

This is the same pattern `DelegatingEPackageRegistry` in `emf.osgi` already uses — we just plug an Atlas-aware delegate behind it.

### Plain-Java helper

`ModelAtlasClient.newResourceSet()` returns a `ResourceSetImpl` with the delegating registry pre-installed. That is the one-liner for plain-Java consumers; nothing else is required.

### OSGi: `ResourceSetConfigurator`

The `rest.client.osgi` bundle registers a `ResourceSetConfigurator` (per `org.eclipse.fennec.emf.osgi.configurator.ResourceSetConfigurator`) so that every `ResourceSet` produced by the framework's `ResourceSetFactory` is post-processed: its package registry is wrapped in the Atlas-aware delegating registry. Consumers calling `resourceSetFactory.createResourceSet()` automatically get fallback resolution — no API change for them.

A boolean configuration property `resource.set.fallback=true` (default `true`) can disable this wrapper for runtimes that explicitly do not want implicit network calls during resource loads.

## ScopedEObjectsRegistry Adapter

The client publishes one `ScopedEObjectsRegistry<EObject>` per `(scope, registry)` pair, with properties:

- `atlas.scope=<scopeName>`
- `atlas.registry=<registryName>`
- `atlas.view=released`
- `atlas.remote=true`

Consumers (validation, etc.) look up by service filter:

```java
@Reference(target = "(&(atlas.scope=jena)(atlas.registry=cocl))")
ScopedEObjectsRegistry<EObject> coclRegistry;
```

This is the **same lookup that works against the in-process server-side publication** after the interface split — that is the symmetry payoff.

EObjects fetched via `get(...)` are detached copies (no shared `Resource`) — mutating them must not be expected to flow back. The contract is read-only by interface.

## Plain Java Mode

```java
ModelAtlasClient client = ModelAtlasClient.builder()
    .baseUri(URI.create("https://atlas.example.org/atlas"))
    .auth(BearerToken.of(System.getenv("ATLAS_TOKEN")))
    .cache(CacheSpec.inMemory(500))
    .build();

EPackage pkg = client.ePackages()
    .getEPackage("https://eclipse.dev/fennec/jena/cocl/1.0")
    .orElseThrow();

ScopedEObjectsRegistry<EObject> coclRegistry =
    client.registry("jena", "cocl");
EObject domainObj = coclRegistry.get("CustomerType").orElseThrow();

// Atlas-aware ResourceSet for plain-Java users
ResourceSet rs = client.newResourceSet();
rs.getResource(URI.createURI("file:/some/instance.xmi"), true);
// any unknown nsURI in that resource resolves through the Atlas client
```

The client never mutates `EPackage.Registry.INSTANCE` automatically in plain-Java mode — the caller decides.

## Configuration (OSGi)

ConfigAdmin PID `org.eclipse.fennec.model.atlas.rest.client`:

| Property | Type | Default | Notes |
|---|---|---|---|
| `base.uri` | String | — | required |
| `connect.timeout.ms` | int | 5_000 | |
| `read.timeout.ms` | int | 30_000 | |
| `mode` | String | `LAZY` | `EAGER` / `LAZY` / `HYBRID` |
| `eager.scopes` | String[] | `[]` | EAGER/HYBRID: scopes to pre-fetch. Empty + EAGER = all. |
| `eager.stages` | String[] | `["released"]` | EAGER/HYBRID: stages to pre-fetch from each scope. |
| `eager.nsuri.allow.list` | String[] | `[]` | HYBRID: nsURIs to fetch eagerly. |
| `mode.strict` | boolean | `false` | If `true`, EAGER fails activation when the server is unreachable; default best-effort. |
| `force.remote` | boolean | `false` | If `true`, publish remote EPackage even when same nsURI exists locally. |
| `register.in.global.registry` | boolean | `false` | Mirror published EPackages into `EPackage.Registry.INSTANCE`. |
| `resource.set.fallback` | boolean | `true` | Wrap ResourceSets produced by the framework with Atlas-aware fallback. |
| `drift.check.interval.ms` | int | 300_000 | `0` disables |
| `scope.allow.list` | String[] | `[]` | empty = all scopes |
| `default.scope` | String | — | for anonymous EPackage lookup |
| `cache.max.entries` | int | 500 | |
| `cache.ttl.ms` | int | 0 | `0` = no TTL |
| `cache.disk.dir` | String | — | empty = in-memory only |
| `auth.type` | String | `none` | `none` / `bearer` / `mtls` |
| `auth.token.env` | String | — | env var holding the token |

The origin properties (`atlas.remote=true`, `atlas.scope`, `atlas.registry`, `atlas.view`, `atlas.base.uri`) are not configurable — they are always set on every published service.

Multiple PIDs (factory-configurable) allow connecting to several Atlas instances.

## Phased Roadmap

**Phase 0 — Server-side interface split (prerequisite).**
- New `scope.api` bundle with `ScopedEObjectsRegistry<T>`.
- Refactor `workflow.api` to add `WritableScopeService<T> extends ScopedEObjectsRegistry<T>`.
- `ScopeServiceImpl` publishes both kinds of services.
- Migrate `validation` and any other read-only consumer to `ScopedEObjectsRegistry<T>`.
- No client code yet; this is the platform pre-work.

**Phase 1 — Read-only EPackage client (MVP).**
- `rest.client.api` (re-exporting `scope.api`), `rest.client.impl` with Jakarta RS Client + in-memory cache + XMI deserialization.
- `rest.client.osgi` registers `EPackage` / `EFactory` / `EPackageConfigurator` with `EMFNamespaces` properties + `atlas.*` origin tags. EAGER and LAZY modes.
- ResourceSet integration via `ResourceSetConfigurator`.
- Manual drift check via `client.checkForDrift()`.
- Tests against a live Atlas (`runtime.config.local.jena`) plus a Jakarta RS stub.

**Phase 2 — Read-only `ScopedEObjectsRegistry`.**
- Client publishes `ScopedEObjectsRegistry<EObject>` per `(scope, registry)` with `atlas.*` properties.
- Background drift watcher with re-registration on change.
- Validation service tested running against both in-process and remote registries with no consumer-side change.

**Phase 3 — Optional features.**
- Persistent disk cache.
- Auth providers (Bearer, mTLS).
- ETag negotiation once the server adds it (see recommendation below).
- HYBRID mode polish.
- Server-side push (SSE) replacing the polling watcher.

**Out of scope for this client (separate workflow client).**
- Uploads, transitions, deletes, stage management.
- A separate `rest.client.workflow.*` bundle family with its own design document.
- It will consume the same `scope.api` for reads, plus `WritableScopeService<T>` for writes.

## Recommendation: Server-Side ETag & Conditional Request Support

This client design works without server changes, but a small, additive enhancement to the Atlas REST layer would make it noticeably better — and would benefit any other HTTP consumer (browsers, CDNs, generic HTTP caches, future SDKs in other languages). It is called out here as a recommendation so the work can be planned independently of the client itself.

### Why it pays off

- **No new computation.** `ObjectMetadata.contentHash` is already a SHA-256 of the serialized content, computed on every store. It *is* a strong validator; it just isn't exposed as a header.
- **Removes the two-call drift pattern.** Today the client must do `head` (metadata) → compare hash → `get content`. With ETags, a single conditional `GET` replaces both: 304 if unchanged, 200+body if changed.
- **Optimistic concurrency on writes** (relevant for the future workflow client). `If-Match` on writes makes the existing `version` / `contentHash` conflict checks a standard HTTP idiom (`412 Precondition Failed`).
- **Plays nicely with intermediaries.** Reverse proxies, browsers, generic HTTP libraries all understand `ETag` / `If-None-Match` / `Last-Modified` without bespoke code.
- **Purely additive, fully backward compatible.** Older clients ignore the new headers.

### What to add

1. **Strong `ETag` on single-object GETs.** For `GET /{scope}/schema/stages/{stage}/content?nsUri=...` and `GET /{scope}/registries/{reg}/stages/{stage}/content?objectId=...`. Set `ETag: "<contentHash>"`.
2. **`Last-Modified` from `ObjectMetadata.lastChangeTime`.**
3. **Conditional GET handling.** `If-None-Match` / `If-Modified-Since` → `304 Not Modified` when matching.
4. **`ETag` on metadata responses.** Treat the metadata document as its own resource: ETag = strong hash over `(contentHash, version, status, lastChangeTime)` so metadata-only changes (e.g. stage transitions) invalidate metadata caches.
5. **`If-Match` on writes** (workflow side). On mismatch return `412`. On success return new ETag.
6. **`Vary: Accept` everywhere.** Atlas content-negotiates XMI / JSON / etc.; the ETag must vary with Accept.
7. **`Cache-Control: private, must-revalidate`.** Once auth is added.
8. **List endpoints (optional).** Weak ETags; deferrable.

### Where to change things

- `org.eclipse.fennec.model.atlas.rest.application/.../resource/SchemaPackagesResource.java` — content GET, metadata GET, etc.
- `org.eclipse.fennec.model.atlas.rest.application/.../resource/ObjectRegistryResource.java` — same set of endpoints.
- A new `jakarta.ws.rs.container.ContainerResponseFilter` (`@Provider`) under `rest.application` to centralise `ETag` / `Last-Modified` / `Vary: Accept` header handling. The resources already attach `ObjectMetadata` to a request property for content negotiation, so the filter can read it without re-fetching.
- A `ContainerRequestFilter` (or in-resource shortcut) for `If-None-Match` → `Response.notModified(EntityTag)`.
- Tests in `rest.tests` — extend GET/PUT/POST tests to assert `ETag` and exercise `If-None-Match` (304) and `If-Match` (412).

### Effort estimate

Roughly **1–2 days**. Hash already exists, metadata already flows through the request context. No model or storage change.

### Risks / caveats

- **ETag stability under serializer drift.** `contentHash` is computed over serialized bytes. If the XMI/JSON serializer ever changes formatting, the hash changes even though the EObject is logically identical. That is correct HTTP-cache behaviour — bytes did change — but worth documenting.
- **Multi-format content negotiation.** Without `Vary: Accept`, a JSON-requesting client could be served an XMI cached representation. `Vary` is non-negotiable.
- **List-endpoint ETags** drift quickly; cache-hit rate is lower; deferrable.

### Recommendation

Schedule the per-object ETag + `If-Match`/`If-None-Match` work as a small standalone PR ahead of, or in parallel to, the client's Phase 1.

## Open Questions / Risks

1. **Default scope for anonymous EPackage lookups.** Most existing consumers ask `EPackage.Registry.INSTANCE.getEPackage(nsUri)` without scope context. Hard-require a `default.scope`, or walk all configured scopes? Recommendation: configurable allow-list, deterministic order.
2. **EObject identity across fetches.** Consumers that compare with `==` will break. Decision needed: cache hits return the same instance (faster, may surprise on mutation), or always return a fresh copy (safe, costs more CPU/memory). Recommendation: same instance, document that the contract is read-only and `==` is meaningful only within one fetch session.
3. **Cross-references between EObjects.** A fetched EObject may reference another by URI. If the referenced one is also remote, do we follow lazily (proxy resolution via the Atlas-aware ResourceSet) or only on explicit fetch? Recommendation: lazy proxy resolution via the ResourceSet — that is exactly why we built the ResourceSet integration.
4. **Drift in `EPackage.Registry.INSTANCE`.** Replacing an EPackage in the global mutable map at runtime can disturb consumers that captured references. Documented and opt-in (`register.in.global.registry=true`).
5. **`force.remote=true` registry-level determinism.** Today's `emf.osgi` aggregator is bind-order, last-write-wins; we can guarantee remote precedence for direct service-lookup consumers via `service.ranking`, but not for `EPackage.Registry.getEPackage(nsURI)` consumers. Decision: ship v1 best-effort, track the ranking-aware aggregator as a follow-up.
6. **EAGER + unreachable server.** Strict-fail vs. best-effort. Default best-effort, `mode.strict=true` opt-in.
7. **REST resource split for security.** The server-side REST resource also mixes read and write endpoints. A future deployment may want to mount only the read-only resource publicly. Plan the split (`ObjectRegistryReadOnlyResource` vs. `ObjectRegistryWorkflowResource`) when the workflow client design is written.
8. **Validation service migration.** Verify `validation` only depends on read methods of `ScopeService<?>`. If it depends on workflow-only methods, those need to stay in the writable interface and `validation` cannot be made workflow-independent. Action: audit before Phase 0.
9. **`atlas.scope` vs `scope.name`.** The existing server-side `ScopeServiceCollector` keys on `scope.name`. The new read-only registries key on `atlas.scope` + `atlas.registry`. Two different services, two different key conventions — clean. Worth a sanity check that no consumer mixes them up.
10. **Workflow client coupling.** When the workflow client is designed, it must be careful to extend, not duplicate, the read-only client's capabilities — sharing the same `scope.api` types and the same caching where it makes sense. That is a future-doc concern, but worth flagging now so the workflow client is not designed in isolation.

## References

### Server (existing)
- `org.eclipse.fennec.model.atlas.rest.application/.../resource/SchemaPackagesResource.java` — EPackage REST endpoints
- `org.eclipse.fennec.model.atlas.rest.application/.../resource/ObjectRegistryResource.java` — EObject REST endpoints
- `org.eclipse.fennec.model.atlas.rest.application/.../resource/ScopesResource.java` — scope discovery
- `org.eclipse.fennec.model.atlas.workflow/src-wf-api/.../ScopeService.java` — current combined scope contract (to be split)
- `org.eclipse.fennec.model.atlas.workflow/src-wf-api/.../RegistryService.java` — registry contract
- `org.eclipse.fennec.model.atlas.workflow/src-wf-api/.../EObjectWorkflowService.java` — workflow contract
- `org.eclipse.fennec.model.atlas.workflow/src/.../registration/DynamicEPackageConfigurator.java` — service-property contract
- `org.eclipse.fennec.model.atlas.workflow/src/.../registration/DynamicEPackageRegistrationService.java` — multi-service registration pattern
- `org.eclipse.fennec.model.atlas.rest.tests/...` — Jakarta RS Client usage examples

### emf.osgi
- `/opt/git/emf.osgi/.../EMFNamespaces.java` — canonical OSGi service-property constants
- `/opt/git/emf.osgi/.../EPackageConfigurator.java` — the configurator interface the client implements
- `/opt/git/emf.osgi/.../components/DefaultEPackageRegistryComponent.java` — the framework aggregator (relevant for the `force.remote` discussion and the proposed ranking-aware enhancement)
- `/opt/git/emf.osgi/.../configurator/ResourceSetConfigurator.java` — the configurator the client uses to wrap ResourceSets with Atlas fallback
- `/opt/git/emf.osgi/.../helper/DelegatingEPackageRegistry.java` — pattern for a delegating package registry
