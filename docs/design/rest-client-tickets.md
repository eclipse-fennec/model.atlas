# Model Atlas Read-Only Client — Planning Tickets

Stories ready to copy into the issue tracker (GitHub Issues / GitLab Issues compatible). One story per ticket, flat — no epics. Each ticket carries its own *Description*, *Acceptance Criteria* (as task-list checkboxes), *Depends on*, *Estimate (person-days)*, and *Labels*.

Companion design document: [`rest-client.md`](./rest-client.md). All section references below point there.

## Overview

| ID | Title | Phase | Est. (PD) | Depends on |
|---|---|---|---:|---|
| P1-1 | Audit existing server-side ETag/conditional support | 1 | 1 | — |
| P1-2 | Strong ETag + Last-Modified on per-object content GETs | 1 | 2 | P1-1 |
| P1-3 | Conditional GET handling (304) | 1 | 1 | P1-2 |
| P1-4 | `Vary: Accept` on cacheable responses | 1 | 0.5 | P1-2 |
| P1-5 | ETag on metadata responses | 1 | 1 | P1-2 |
| P1-6 | `If-Match` on workflow writes (412) | 1 | 1 | P1-2 |
| P1-7 | Scope-level HEAD endpoint with change hints | 1 | 2 | P1-2 |
| P1-8 | Server-side tests for ETag / 304 / 412 / scope HEAD | 1 | 2 | P1-2…P1-7 |
| P2-1 | `rest.client.api` bundle with public interfaces and config types | 2 | 1 | — |
| P2-2 | `rest.client.impl` skeleton with Jakarta RS Client + plain-Java provider | 2 | 1 | P2-1 |
| P2-3 | REST mapping for EPackage list + content GET | 2 | 2 | P2-2 |
| P2-4 | EPackage XMI deserialization | 2 | 1 | P2-3 |
| P2-5 | In-memory cache with LRU + TTL | 2 | 1.5 | P2-2 |
| P2-6 | Client-side conditional GET (`If-None-Match` / ETag storage) | 2 | 1 | P2-3, P2-5, P1-2 |
| P2-7 | Scope-level drift watcher | 2 | 2 | P2-6, P1-7 |
| P2-8 | `AtlasDelegatingPackageRegistry` + `newResourceSet()` | 2 | 2 | P2-3 |
| P2-9 | Allow/deny lists for nsURIs | 2 | 0.5 | P2-3 |
| P2-10 | Bearer token + mTLS auth providers | 2 | 2 | P2-2 |
| P2-11 | Integration tests against a live Atlas | 2 | 3 | P2-3…P2-10 |
| P3-1 | `rest.client.osgi` bundle with DS components + ConfigAdmin | 3 | 1.5 | P2-* |
| P3-2 | OSGi-side `JakartaRsClientProvider` (Whiteboard `ClientBuilder`) | 3 | 1 | P3-1 |
| P3-3 | EPackage publisher (`EPackageConfigurator` + `EPackage` + `EFactory`) with `atlas.*` properties | 3 | 2 | P3-1, P2-4 |
| P3-4 | EAGER mode (startup pre-fetch) | 3 | 1 | P3-3 |
| P3-5 | LAZY mode with synchronously blocking trigger + timeout | 3 | 3 | P3-3 |
| P3-6 | HYBRID mode (`eager.nsuri.allow.list`) | 3 | 0.5 | P3-4, P3-5 |
| P3-7 | Local-first behaviour with `ServiceListener` | 3 | 2 | P3-3 |
| P3-8 | `force.remote=true` (ranking + startup version check) | 3 | 2 | P3-7 |
| P3-9 | Atomic substitution on drift refresh (per-nsURI lock) | 3 | 2 | P3-3, P2-7 |
| P3-10 | `ResourceSetConfigurator` integration | 3 | 1 | P3-3, P2-8 |
| P3-11 | `EPackage.Registry.INSTANCE` mirroring (opt-in) | 3 | 0.5 | P3-3 |
| P3-12 | OSGi integration tests for modes + force.remote + drift | 3 | 3 | P3-4…P3-11 |
| P4-1 | `scope.api` bundle with `ScopedEObjectsRegistry<T>` | 4 | 1 | — |
| P4-2 | Refactor `workflow.api`: `WritableScopeService<T> extends ScopedEObjectsRegistry<T>` | 4 | 1 | P4-1 |
| P4-3 | Refactor `ScopeServiceImpl` to publish both service shapes | 4 | 2 | P4-2 |
| P4-4 | `ScopedEObjectsRegistryCollector` | 4 | 1 | P4-3 |
| P4-5 | Audit `ScopeService<?>` consumers for read-only usage | 4 | 1 | P4-1 |
| P4-6 | Migrate validation service to `ScopedEObjectsRegistry<?>` | 4 | 1 | P4-3, P4-5 |
| P4-7 | Deprecate `ScopeService<T>` as typedef | 4 | 0.5 | P4-3 |
| P4-8 | Tests covering both service publications | 4 | 1 | P4-3 |
| P5-1 | `ScopedEObjectsRegistryImpl` (REST mapping for EObjects) | 5 | 2 | P4-1, P2-* |
| P5-2 | Wire EObject drift into scope-level watcher | 5 | 1 | P5-1, P2-7 |
| P5-3 | Wire `ModelAtlasClient.registry(scope, registry)` | 5 | 0.5 | P5-1 |
| P5-4 | Publish `ScopedEObjectsRegistry<EObject>` OSGi service per (scope, registry) | 5 | 2 | P5-1, P3-1 |
| P5-5 | Acceptance: validation runs unchanged against in-process & remote | 5 | 2 | P5-4, P4-6 |
| P5-6 | Object identity + cross-reference tests via Atlas-aware ResourceSet | 5 | 2 | P5-4, P3-10 |

**Totals (PD):** Phase 1 ≈ 10.5 · Phase 2 ≈ 17 · Phase 3 ≈ 19.5 · Phase 4 ≈ 8.5 · Phase 5 ≈ 9.5 — **≈ 65 PD overall.**

---

## Phase 1 — Server-Side ETag & Conditional Requests

### P1-1: Audit existing server-side ETag/conditional support

**Estimate:** 1 PD
**Depends on:** —
**Labels:** `phase-1`, `server`, `audit`, `etag`

**Description**

Audit the existing REST application code to identify what parts of the ETag / conditional-request specification (see *Phase 1* in the design doc) are already implemented. Produce a gap report against the verification checklist that drives the scope of the remaining Phase-1 tickets.

**Acceptance Criteria**

- [ ] Each item in the design-doc verification checklist is classified as `implemented` / `partial` / `missing`.
- [ ] For partial/missing items, the relevant resource class and method are identified.
- [ ] Gap report posted as a comment on this ticket and linked from each follow-up ticket (P1-2…P1-8).
- [ ] Where an item is fully implemented and tested, the corresponding follow-up ticket is closed as not-needed.

---

### P1-2: Strong ETag + Last-Modified on per-object content GETs

**Estimate:** 2 PD
**Depends on:** P1-1
**Labels:** `phase-1`, `server`, `etag`

**Description**

Emit a strong `ETag` (value = `ObjectMetadata.contentHash`) and `Last-Modified` (value = `ObjectMetadata.lastChangeTime`) on single-object content GETs:

- `GET /{scope}/schema/stages/{stage}/content?nsUri=...`
- `GET /{scope}/registries/{reg}/stages/{stage}/content?objectId=...`

Implement via a centralised `jakarta.ws.rs.container.ContainerResponseFilter` (`@Provider`) so other resources can opt in without duplicating header logic. Resources already attach `ObjectMetadata` to a request property for content negotiation; the filter reads it from there.

**Acceptance Criteria**

- [ ] `SchemaPackagesResource` content GET emits `ETag: "<contentHash>"`.
- [ ] `ObjectRegistryResource` content GET emits `ETag: "<contentHash>"`.
- [ ] Both emit `Last-Modified` from `ObjectMetadata.lastChangeTime`.
- [ ] Header-handling lives in a `ContainerResponseFilter` under `rest.application`, not in each resource.
- [ ] Existing tests still pass.

---

### P1-3: Conditional GET handling (304)

**Estimate:** 1 PD
**Depends on:** P1-2
**Labels:** `phase-1`, `server`, `etag`

**Description**

Honour `If-None-Match` and `If-Modified-Since` on the same content GETs as P1-2. On a match, return `304 Not Modified` with an empty body. Implementation either via a `ContainerRequestFilter` or as an in-resource shortcut using `Response.notModified(EntityTag)`.

**Acceptance Criteria**

- [ ] `If-None-Match` matching the current ETag → `304`, no body.
- [ ] `If-None-Match` with a stale ETag → `200` with body and current ETag.
- [ ] `If-Modified-Since` honoured against `lastChangeTime`.
- [ ] `304` responses still carry the current ETag.

---

### P1-4: `Vary: Accept` on cacheable responses

**Estimate:** 0.5 PD
**Depends on:** P1-2
**Labels:** `phase-1`, `server`, `etag`

**Description**

Set `Vary: Accept` on every cacheable response so intermediaries treat representations under different `Accept` types as distinct cache entries. Without this, a JSON-requesting client could be served a cached XMI representation. Implemented in the same `ContainerResponseFilter` as P1-2.

**Acceptance Criteria**

- [ ] `Vary: Accept` present on all content GET responses (including `304`).
- [ ] Same nsURI requested with `Accept: application/xml` and `Accept: application/json` produces distinct ETags.

---

### P1-5: ETag on metadata responses

**Estimate:** 1 PD
**Depends on:** P1-2
**Labels:** `phase-1`, `server`, `etag`

**Description**

Treat metadata responses as their own resources for caching. ETag = strong hash over `(contentHash, version, status, lastChangeTime)` so metadata-only changes (e.g. stage transitions that don't change bytes) invalidate metadata caches without invalidating content caches.

**Acceptance Criteria**

- [ ] Metadata GET emits its own ETag, distinct from the content ETag.
- [ ] A stage transition (no content change) changes the metadata ETag and not the content ETag.
- [ ] Conditional GET on metadata works (304 / 200).

---

### P1-6: `If-Match` on workflow writes (412)

**Estimate:** 1 PD
**Depends on:** P1-2
**Labels:** `phase-1`, `server`, `etag`, `workflow`

**Description**

On workflow `POST` / `PUT` / transition endpoints, accept `If-Match` and use it as an optimistic-concurrency check against the current `contentHash`. Mismatch → `412 Precondition Failed`. Success → response carries the new ETag.

This is workflow-side (not consumed by the read-only client), but specified in Phase 1 because it lives in the same filter.

**Acceptance Criteria**

- [ ] `If-Match` header parsed on writable endpoints in `ObjectRegistryResource` (and any other writable resource).
- [ ] Matching `If-Match` → request proceeds; response carries new ETag.
- [ ] Mismatching `If-Match` → `412`, no side effects.
- [ ] Missing `If-Match` continues to behave as today (no precondition).

---

### P1-7: Scope-level HEAD endpoint with change hints

**Estimate:** 2 PD
**Depends on:** P1-2
**Labels:** `phase-1`, `server`, `etag`, `drift`

**Description**

Implement `HEAD /{scope}` that returns an aggregate strong validator for the entire scope and, on a stale `If-None-Match`, lists exactly which entries changed:

- `ETag`: aggregate hash over `(nsUri, contentHash)` of all packages plus `(registry, objectId, contentHash)` of all registered EObjects in the scope, in canonical order.
- `Last-Modified`: max(`lastChangeTime`) within the scope.
- `Atlas-Changed-NsUris`: CSV of changed nsURIs since the request's `If-None-Match` (only set when responding 200).
- `Atlas-Changed-Objects`: CSV of changed `(registry/objectId)` tuples since the request's `If-None-Match` (only set when responding 200).
- `If-None-Match` matching the current aggregate → `304` with no diff headers.

The drift watcher in Phases 2/3 calls this once per scope instead of N times per cached entry.

**Acceptance Criteria**

- [ ] `HEAD /{scope}` returns a deterministic aggregate `ETag`.
- [ ] Aggregate ETag is stable under reordering of underlying entries.
- [ ] `If-None-Match` matching → `304`.
- [ ] `If-None-Match` with a stale value → `200` with `Atlas-Changed-NsUris` and/or `Atlas-Changed-Objects` containing exactly the diff.
- [ ] Documented in OpenAPI / Swagger (or equivalent).

---

### P1-8: Server-side tests for ETag / 304 / 412 / scope HEAD

**Estimate:** 2 PD
**Depends on:** P1-2, P1-3, P1-4, P1-5, P1-6, P1-7
**Labels:** `phase-1`, `server`, `tests`

**Description**

Extend `rest.tests` (or add a new test class) to cover all Phase-1 behaviours. The test suite is the contract the client (Phases 2/3) relies on.

**Acceptance Criteria**

- [ ] ETag emitted on content / metadata GETs.
- [ ] `If-None-Match` round-trip: matching → 304, mismatching → 200 + new ETag.
- [ ] `If-Modified-Since` round-trip equivalent.
- [ ] `Vary: Accept` asserted; same nsURI under different `Accept` produces distinct ETags.
- [ ] `If-Match` mismatching write → 412; matching → 200/201 with new ETag.
- [ ] `HEAD /{scope}` round-trip: stale `If-None-Match` → 200 with `Atlas-Changed-*` headers covering exactly the diff.
- [ ] Tests run in CI.

---

## Phase 2 — Common Client Library (Plain Java)

### P2-1: `rest.client.api` bundle with public interfaces and config types

**Estimate:** 1 PD
**Depends on:** —
**Labels:** `phase-2`, `client`, `api`

**Description**

Create the API bundle. Pure-Java, no DS, no Jakarta RS implementation. Defines the public surface that both the plain-Java front-end and the OSGi front-end (Phase 3) implement.

**Acceptance Criteria**

- [ ] Bundle `org.eclipse.fennec.model.atlas.rest.client.api` builds.
- [ ] Exports `ModelAtlasClient`, `RemoteEPackageProvider`, `DriftListener`, `DriftReport`.
- [ ] Exports `ClientConfiguration` value type (matching the property table in *Phase 2 / Configuration* in the design doc).
- [ ] Exports `JakartaRsClientProvider` SPI.
- [ ] Exports `atlas.*` origin-property constants.
- [ ] Exports the typed exception hierarchy (`ModelAtlasClientException`, `NotFoundException`, `TransportException`).
- [ ] Bundle has only EMF core as a runtime dependency.

---

### P2-2: `rest.client.impl` skeleton with Jakarta RS Client + plain-Java provider

**Estimate:** 1 PD
**Depends on:** P2-1
**Labels:** `phase-2`, `client`, `impl`

**Description**

Stand up the implementation bundle with a builder-based `ModelAtlasClient` that constructs a Jakarta RS `Client` via the default plain-Java `JakartaRsClientProvider` (`ClientBuilder.newBuilder()`). Wire timeouts, base URI, and the SPI seam.

**Acceptance Criteria**

- [ ] `ModelAtlasClient.builder()` builds an instance from a `ClientConfiguration`.
- [ ] `JakartaRsClientProvider` is the only seam between client construction and Jakarta RS — no direct `ClientBuilder.newBuilder()` calls outside the plain-Java provider.
- [ ] Plain-Java provider applies connect/read timeouts.
- [ ] `close()` releases the underlying `Client`.

---

### P2-3: REST mapping for EPackage list + content GET

**Estimate:** 2 PD
**Depends on:** P2-2
**Labels:** `phase-2`, `client`, `rest`

**Description**

Implement the read-only EPackage REST endpoints: `listScopeNames`, `listNsUris(scope)`, `getEPackage(nsUri)` (resolves through `scope.allow.list` / `default.scope`), and `ensureAvailable(nsUri)`.

**Acceptance Criteria**

- [ ] `listScopeNames()` calls `GET /scopes`.
- [ ] `listNsUris(scope)` calls `GET /{scope}/schema`.
- [ ] `getEPackage(nsUri)` walks `scope.allow.list` in order; first hit wins.
- [ ] `ensureAvailable(nsUri)` exists for warm-up and for the registry-delegate path (P2-8).
- [ ] Errors map to the typed exception hierarchy (`NotFoundException` on 404, `TransportException` on connect/timeout).

---

### P2-4: EPackage XMI deserialization

**Estimate:** 1 PD
**Depends on:** P2-3
**Labels:** `phase-2`, `client`, `emf`

**Description**

Deserialize the EPackage payload returned by the content GET into an `EPackage` instance using EMF's XMI infrastructure. Reuse `emf.common` helpers where they exist.

**Acceptance Criteria**

- [ ] `getEPackage(nsUri)` returns a fully resolved `EPackage`.
- [ ] EFactory for the package is set correctly so that `EFactory.create(EClass)` works.
- [ ] Deserialization works for the EPackages used in `runtime.config.local.jena` end-to-end.

---

### P2-5: In-memory cache with LRU + TTL

**Estimate:** 1.5 PD
**Depends on:** P2-2
**Labels:** `phase-2`, `client`, `cache`

**Description**

Implement an in-memory cache keyed by `nsUri` (EPackages) and `(scope, registry, objectId)` (EObjects, used by Phase 5). Bounded by `cache.max.entries` (LRU) and `cache.ttl.ms`. Cache values store the deserialized object plus the last-seen `ETag` and `Last-Modified` (kept internal to `rest.client.impl`).

**Acceptance Criteria**

- [ ] Cache respects `cache.max.entries` (LRU eviction beyond the limit).
- [ ] Cache respects `cache.ttl.ms` (`0` = no TTL).
- [ ] Cache exposes get/put/invalidate by key, and a "list all keys" for the watcher.
- [ ] `ETag` and `Last-Modified` stored alongside but not exposed via `rest.client.api`.

---

### P2-6: Client-side conditional GET (`If-None-Match` / ETag storage)

**Estimate:** 1 PD
**Depends on:** P2-3, P2-5, P1-2
**Labels:** `phase-2`, `client`, `etag`, `cache`

**Description**

When refetching a cached entry (forced refresh or post-TTL revalidation), send `If-None-Match` with the stored ETag. On `304` keep the cache entry and bump its TTL; on `200` replace the cache entry with the new payload and ETag.

**Acceptance Criteria**

- [ ] Refresh of a cached EPackage sends `If-None-Match`.
- [ ] `304` response → cache entry retained, TTL refreshed, no parsing happens.
- [ ] `200` response → cache entry replaced, new ETag stored.
- [ ] Works against the Phase-1 server.

---

### P2-7: Scope-level drift watcher

**Estimate:** 2 PD
**Depends on:** P2-6, P1-7
**Labels:** `phase-2`, `client`, `drift`

**Description**

A scheduled task (`drift.check.interval.ms`) that, for each configured scope, issues `HEAD /{scope}` with the cached scope ETag. On `304` nothing happens. On `200`, the response's `Atlas-Changed-NsUris` and `Atlas-Changed-Objects` headers tell the client exactly which cache entries to invalidate. After invalidation, registered `DriftListener`s receive `onPackageChanged` / `onPackageRemoved` events. `0` disables the watcher.

**Acceptance Criteria**

- [ ] One HEAD per configured scope per tick.
- [ ] Stored scope ETag updated on `200`.
- [ ] Only the entries listed in `Atlas-Changed-*` headers are invalidated.
- [ ] `DriftListener` events fire for each changed/removed entry.
- [ ] `drift.check.interval.ms=0` disables the watcher cleanly.

---

### P2-8: `AtlasDelegatingPackageRegistry` + `newResourceSet()`

**Estimate:** 2 PD
**Depends on:** P2-3
**Labels:** `phase-2`, `client`, `emf`

**Description**

Implement an `EPackage.Registry` that forwards `getEPackage(nsURI)` to a primary registry (the framework one, or `EPackage.Registry.INSTANCE`) and on `null` calls `RemoteEPackageProvider.ensureAvailable(nsURI)`. Cache hits within the delegating registry are direct. Subscribes to drift events to evict stale entries.

`ModelAtlasClient.newResourceSet()` returns a `ResourceSetImpl` with the delegating registry pre-installed.

**Acceptance Criteria**

- [ ] An XMI resource referencing an unknown nsURI loads successfully through `newResourceSet()`.
- [ ] On drift event for an nsURI, the delegating-registry entry is evicted; subsequent lookups refetch.
- [ ] Same-nsURI in the primary registry takes precedence over the Atlas fallback.

---

### P2-9: Allow/deny lists for nsURIs

**Estimate:** 0.5 PD
**Depends on:** P2-3
**Labels:** `phase-2`, `client`, `config`

**Description**

Apply `nsuri.allow.list` and `nsuri.deny.list` (exact matches, no patterns) when `getEPackage` / `ensureAvailable` is asked for a package. Denied → never returned. Allow-list non-empty → only listed nsURIs returned. Both empty → all allowed.

**Acceptance Criteria**

- [ ] Denied nsURI returns `Optional.empty()` regardless of cache state.
- [ ] Allow-list filtering applied uniformly to direct lookup and to the watcher.
- [ ] Filtering is testable in isolation (unit test).

---

### P2-10: Bearer token + mTLS auth providers

**Estimate:** 2 PD
**Depends on:** P2-2
**Labels:** `phase-2`, `client`, `auth`

**Description**

Implement two pluggable auth providers wired through `JakartaRsClientProvider`:

- **Bearer**: `Authorization: Bearer <token>`, sourced from `auth.token.env`.
- **mTLS**: `KeyStore` / `TrustStore` configured on the underlying `ClientBuilder`.

Both must work in plain-Java; the OSGi variant in Phase 3 reuses them unchanged.

**Acceptance Criteria**

- [ ] `auth.type=bearer` adds the header on every request.
- [ ] `auth.type=mtls` configures keystore/truststore on the `ClientBuilder`.
- [ ] `auth.type=none` (default) sends no auth.
- [ ] Token is read once at provider construction; documented behaviour for env var changes.

---

### P2-11: Integration tests against a live Atlas

**Estimate:** 3 PD
**Depends on:** P2-3 — P2-10
**Labels:** `phase-2`, `client`, `tests`

**Description**

Bundle `rest.client.tests` exercises the plain-Java client end-to-end against a real Atlas instance (Testcontainers) and a Jakarta RS stub for negative paths.

**Acceptance Criteria**

- [ ] `getEPackage` round-trip against `runtime.config.local.jena` (or equivalent).
- [ ] Cache miss → GET; cache hit → no GET; post-TTL → conditional GET (`304` keeps cache, `200` replaces).
- [ ] `newResourceSet()` resolves an unknown nsURI through the Atlas client.
- [ ] Allow/deny list filtering verified.
- [ ] Drift watcher fires `onPackageChanged` after server-side mutation.
- [ ] Bearer auth verified end-to-end.

---

## Phase 3 — OSGi Delegate Registry

### P3-1: `rest.client.osgi` bundle with DS components + ConfigAdmin

**Estimate:** 1.5 PD
**Depends on:** Phase 2 complete
**Labels:** `phase-3`, `client`, `osgi`

**Description**

Stand up the OSGi bundle. Define the ConfigAdmin PID `org.eclipse.fennec.model.atlas.rest.client` mapping to `ClientConfiguration`. Factory PID support so multiple Atlas instances can be configured in parallel.

**Acceptance Criteria**

- [ ] Bundle activates on a configuration arriving via ConfigAdmin.
- [ ] Multiple PIDs produce multiple independent client instances.
- [ ] `lazy.resolve.timeout.ms` and `resource.set.fallback` exposed in addition to the Phase-2 properties.
- [ ] Activation is reentrant (config update → clean restart of the components).

---

### P3-2: OSGi-side `JakartaRsClientProvider` (Whiteboard `ClientBuilder`)

**Estimate:** 1 PD
**Depends on:** P3-1
**Labels:** `phase-3`, `client`, `osgi`

**Description**

Provide a DS-resolved `JakartaRsClientProvider` that obtains the Whiteboard `ClientBuilder` factory rather than calling `ClientBuilder.newBuilder()` directly. This honours framework-level filters, connection providers, and the runtime's HTTP client selection.

**Acceptance Criteria**

- [ ] Provider obtains the `ClientBuilder` from the OSGi service registry.
- [ ] Auth providers (P2-10) work unchanged through this provider.
- [ ] Falls back to the plain-Java provider only with an explicit configuration switch (or fails fast — TBD in implementation).

---

### P3-3: EPackage publisher with `atlas.*` properties

**Estimate:** 2 PD
**Depends on:** P3-1, P2-4
**Labels:** `phase-3`, `client`, `osgi`, `emf-osgi`

**Description**

Per fetched EPackage, register three OSGi services with **identical** properties: `EPackageConfigurator` (per `org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator`), `EPackage`, and `EFactory`. Use the `EMFNamespaces` constants for the canonical EMF properties and the `atlas.*` constants from `rest.client.api` for origin properties.

**Acceptance Criteria**

- [ ] Every fetched EPackage results in three services with matching properties.
- [ ] EMF properties set via `EMFNamespaces` constants only (no string literals in code).
- [ ] `atlas.remote=true`, `atlas.scope`, `atlas.stage`, `atlas.base.uri` set on every publication.
- [ ] `emf.osgi`'s `DefaultEPackageRegistryComponent` picks the configurator up and populates `EPackage.Registry`.

---

### P3-4: EAGER mode (startup pre-fetch)

**Estimate:** 1 PD
**Depends on:** P3-3
**Labels:** `phase-3`, `client`, `osgi`

**Description**

On activation, list nsURIs in `eager.scopes` × `eager.stages` and fetch + publish each. With `mode.strict=false` (default), unreachable server is logged but does not fail activation; with `mode.strict=true`, activation fails.

**Acceptance Criteria**

- [ ] All EPackages from configured scopes & stages are published as OSGi services after activation.
- [ ] `eager.scopes=[]` + `mode=EAGER` pre-fetches all configured scopes (interpreted as "all in `scope.allow.list`").
- [ ] `mode.strict=true` + unreachable server → activation fails with a clear log line.
- [ ] `mode.strict=false` + unreachable server → activation succeeds, retried on next config update or on demand.

---

### P3-5: LAZY mode with synchronously blocking trigger + timeout

**Estimate:** 3 PD
**Depends on:** P3-3
**Labels:** `phase-3`, `client`, `osgi`, `lazy`

**Description**

In LAZY mode, an `EPackage.Registry.getEPackage(unknownNsUri)` triggers fetch + publish. Because `emf.osgi`'s aggregator binds `EPackageConfigurator`s asynchronously, the trigger must **block synchronously** until the package is observable in the framework `EPackage.Registry`, with a configurable timeout (`lazy.resolve.timeout.ms`, default 5 s).

The blocking happens inside the delegating `EPackage.Registry`'s `getEPackage` implementation. On timeout, the call returns `null` and a warning is logged.

**Acceptance Criteria**

- [ ] `getEPackage(unknownNsUri)` blocks until the fetched package is observable via the framework registry, then returns it.
- [ ] On timeout, returns `null` and logs a warning at `WARN` level.
- [ ] Concurrent calls for the same nsURI deduplicate (one fetch + publish, all callers wait on the same condition).
- [ ] Implementation does not deadlock with `emf.osgi`'s component-binding thread.

---

### P3-6: HYBRID mode (`eager.nsuri.allow.list`)

**Estimate:** 0.5 PD
**Depends on:** P3-4, P3-5
**Labels:** `phase-3`, `client`, `osgi`

**Description**

Combine EAGER + LAZY: pre-fetch every nsURI in `eager.nsuri.allow.list` at activation, treat everything else as LAZY.

**Acceptance Criteria**

- [ ] Listed nsURIs are published at activation; unlisted ones only on demand.
- [ ] All other EAGER and LAZY behaviours apply unchanged.

---

### P3-7: Local-first behaviour with `ServiceListener`

**Estimate:** 2 PD
**Depends on:** P3-3
**Labels:** `phase-3`, `client`, `osgi`

**Description**

Before publishing a remote EPackage, check whether an `EPackage` *or* `EPackageConfigurator` for the same nsURI is already registered. If yes, suppress the remote publication. Subscribe to local service events; when a local one is unregistered, publish the suppressed remote candidate.

**Acceptance Criteria**

- [ ] Local registration with same nsURI → remote not published.
- [ ] Local registration disappears → remote candidate published (if `force.remote=false`).
- [ ] No flapping when both are present and one is briefly unregistered/registered.

---

### P3-8: `force.remote=true` (ranking + startup version check)

**Estimate:** 2 PD
**Depends on:** P3-7
**Labels:** `phase-3`, `client`, `osgi`

**Description**

When `force.remote=true`:

1. Remote services are published with high `service.ranking`, even when local services for the same nsURI exist.
2. On startup, every locally registered EPackage is checked against the Atlas; if the Atlas has a newer version (different content hash, with version preference rule), the remote registration preempts the local one.

Document the honest caveat: `emf.osgi`'s aggregator is bind-order-dependent; ranking-aware aggregation is the follow-up recommendation tracked in P3 closing notes.

**Acceptance Criteria**

- [ ] `force.remote=true` publishes remote services with a higher `service.ranking` than the local default.
- [ ] On startup, locally registered EPackages whose Atlas-side counterpart is newer are superseded by the remote registration.
- [ ] Behaviour with `force.remote=false` (default) unchanged.
- [ ] Caveat about aggregator bind-order documented in code and in the design doc.

---

### P3-9: Atomic substitution on drift refresh (per-nsURI lock)

**Estimate:** 2 PD
**Depends on:** P3-3, P2-7
**Labels:** `phase-3`, `client`, `osgi`, `drift`

**Description**

On drift event, replace the OSGi services for the affected nsURI atomically: per-nsURI lock around (a) cache update and (b) service unregistration + re-registration. Lookups via the delegating `EPackage.Registry` block briefly during the swap; `ResourceSet`s never see a half-state where the package is missing or where two versions coexist.

**Acceptance Criteria**

- [ ] During a swap, a concurrent `getEPackage(nsURI)` returns either the old or the new package, never `null` or a mixture.
- [ ] Locks are nsURI-scoped — substitutions of unrelated packages run in parallel.
- [ ] No deadlocks under load (stress test).
- [ ] Cache and services are always in sync after a swap.

---

### P3-10: `ResourceSetConfigurator` integration

**Estimate:** 1 PD
**Depends on:** P3-3, P2-8
**Labels:** `phase-3`, `client`, `osgi`, `emf-osgi`

**Description**

Register a `ResourceSetConfigurator` (per `org.eclipse.fennec.emf.osgi.configurator.ResourceSetConfigurator`) so every `ResourceSet` produced by the framework's `ResourceSetFactory` has its package registry wrapped by `AtlasDelegatingPackageRegistry`. Disable-able via `resource.set.fallback=false` (default `true`).

**Acceptance Criteria**

- [ ] `ResourceSet`s from the framework factory carry the delegating registry by default.
- [ ] `resource.set.fallback=false` disables wrapping cleanly.
- [ ] An XMI load with an unknown nsURI succeeds via Atlas fallback.

---

### P3-11: `EPackage.Registry.INSTANCE` mirroring (opt-in)

**Estimate:** 0.5 PD
**Depends on:** P3-3
**Labels:** `phase-3`, `client`, `osgi`

**Description**

When `register.in.global.registry=true`, additionally put each fetched EPackage into `EPackage.Registry.INSTANCE` (in addition to the OSGi service registration). For legacy code reaching the EMF singleton.

**Acceptance Criteria**

- [ ] Default (`false`) leaves `EPackage.Registry.INSTANCE` untouched.
- [ ] `true` mirrors every published EPackage into the singleton.
- [ ] Drift swaps replace the singleton entry consistently with the OSGi service.

---

### P3-12: OSGi integration tests for modes + force.remote + drift

**Estimate:** 3 PD
**Depends on:** P3-4 — P3-11
**Labels:** `phase-3`, `client`, `osgi`, `tests`

**Description**

Cover the OSGi-specific behaviours end-to-end against a Testcontainers-hosted Atlas.

**Acceptance Criteria**

- [ ] EAGER mode: configured scopes/stages produce expected service registrations on activation.
- [ ] LAZY mode: `getEPackage(unknownNsUri)` blocks then succeeds; on timeout returns `null`.
- [ ] HYBRID mode: listed nsURIs eager; others lazy.
- [ ] Local-first: pre-existing local registration suppresses remote.
- [ ] `force.remote=true`: remote supersedes local on startup if Atlas has a newer version.
- [ ] Drift swap is atomic (concurrent lookup returns old or new, never null/half-state).
- [ ] `ResourceSetConfigurator` wrapping is observable via service inspection.

---

## Phase 4 — Server-Side Interface Split

### P4-1: `scope.api` bundle with `ScopedEObjectsRegistry<T>`

**Estimate:** 1 PD
**Depends on:** —
**Labels:** `phase-4`, `server`, `api`

**Description**

New bundle `org.eclipse.fennec.model.atlas.scope.api` containing the read-only contract `ScopedEObjectsRegistry<T extends EObject>` plus the `atlas.*` service-property constants. Depends only on EMF core.

**Acceptance Criteria**

- [ ] Bundle builds and exports `ScopedEObjectsRegistry<T>` with the methods specified in the design doc (Phase 4 / What changes).
- [ ] Constants for `atlas.scope`, `atlas.registry`, `atlas.view`, `atlas.remote` exported.
- [ ] No transitive dependency on workflow types.

---

### P4-2: Refactor `workflow.api`: `WritableScopeService<T> extends ScopedEObjectsRegistry<T>`

**Estimate:** 1 PD
**Depends on:** P4-1
**Labels:** `phase-4`, `server`, `api`

**Description**

Introduce `WritableScopeService<T>` extending `ScopedEObjectsRegistry<T>` and carrying the workflow methods (upload, transition, delete, stage management). The existing `ScopeService<T>` becomes a deprecated alias (P4-7).

**Acceptance Criteria**

- [ ] `WritableScopeService<T>` interface present in `workflow.api` and extends `ScopedEObjectsRegistry<T>`.
- [ ] Workflow-specific methods moved from `ScopeService<T>` to `WritableScopeService<T>`.
- [ ] No method signatures changed (only relocated).

---

### P4-3: Refactor `ScopeServiceImpl` to publish both service shapes

**Estimate:** 2 PD
**Depends on:** P4-2
**Labels:** `phase-4`, `server`, `impl`

**Description**

The existing implementation registers itself as both `ScopedEObjectsRegistry<EObject>` (one publication per `(scope, registry)`) **and** `WritableScopeService<EObject>` (per scope). Two independent OSGi service registrations.

**Acceptance Criteria**

- [ ] Per `(scope, registry)`, exactly one `ScopedEObjectsRegistry<EObject>` is published with `atlas.scope`/`atlas.registry`/`atlas.view`/`atlas.remote` properties (no `atlas.remote=true` here — it's the in-process publication).
- [ ] Per scope, exactly one `WritableScopeService<EObject>` is published.
- [ ] Both shapes resolve to the same underlying state.
- [ ] Existing REST endpoints continue to work.

---

### P4-4: `ScopedEObjectsRegistryCollector`

**Estimate:** 1 PD
**Depends on:** P4-3
**Labels:** `phase-4`, `server`, `impl`

**Description**

Add (or generalise) a collector that iterates `ScopedEObjectsRegistry<EObject>` services keyed by `(atlas.scope, atlas.registry)`. The existing `ScopeServiceCollector` (keyed on `scope.name`) remains untouched; the two collectors are independent.

**Acceptance Criteria**

- [ ] New collector available for use by read-only consumers.
- [ ] Iteration covers all `(scope, registry)` services without duplication.
- [ ] No regression in `ScopeServiceCollector` consumers.

---

### P4-5: Audit `ScopeService<?>` consumers for read-only usage

**Estimate:** 1 PD
**Depends on:** P4-1
**Labels:** `phase-4`, `server`, `audit`

**Description**

Identify every `@Reference ScopeService<?>` in the codebase. For each, classify whether it uses only read methods (candidate for migration to `ScopedEObjectsRegistry<?>`) or workflow methods (must stay on `WritableScopeService<?>`).

**Acceptance Criteria**

- [ ] List of consumers with usage classification (read-only / writable / mixed) posted on the ticket.
- [ ] For mixed-usage consumers, document whether the workflow dependency is intentional.

---

### P4-6: Migrate validation service to `ScopedEObjectsRegistry<?>`

**Estimate:** 1 PD
**Depends on:** P4-3, P4-5
**Labels:** `phase-4`, `server`, `migration`

**Description**

Change `org.eclipse.fennec.model.atlas.validation`'s `@Reference` from `ScopeService<?>` to `ScopedEObjectsRegistry<?>`. No behavioural change. After this, `validation` no longer depends on the workflow API.

**Acceptance Criteria**

- [ ] Validation bundle compiles against `scope.api` instead of `workflow.api`.
- [ ] All validation tests pass unchanged.
- [ ] No workflow-API dependency in `validation`'s OSGi `Import-Package` / `Require-Bundle`.

---

### P4-7: Deprecate `ScopeService<T>` as typedef

**Estimate:** 0.5 PD
**Depends on:** P4-3
**Labels:** `phase-4`, `server`, `api`

**Description**

`ScopeService<T>` becomes a deprecated alias for `WritableScopeService<T>` (next minor release), with Javadoc redirecting consumers to either `WritableScopeService<T>` (writes) or `ScopedEObjectsRegistry<T>` (reads).

**Acceptance Criteria**

- [ ] `ScopeService<T>` annotated `@Deprecated` with a clear Javadoc redirect.
- [ ] No build warnings turn into errors yet (deprecation, not removal).
- [ ] Removal scheduled for a follow-up major release.

---

### P4-8: Tests covering both service publications

**Estimate:** 1 PD
**Depends on:** P4-3
**Labels:** `phase-4`, `server`, `tests`

**Description**

`workflow.tests` (or a new test class) verifies that both `ScopedEObjectsRegistry<EObject>` and `WritableScopeService<EObject>` are published correctly and resolve to the same backing state.

**Acceptance Criteria**

- [ ] Test asserts a `ScopedEObjectsRegistry<EObject>` publication per `(scope, registry)`.
- [ ] Test asserts a `WritableScopeService<EObject>` publication per scope.
- [ ] Test asserts that a write via `WritableScopeService` is observable through `ScopedEObjectsRegistry`.

---

## Phase 5 — EObject-Registry Client

### P5-1: `ScopedEObjectsRegistryImpl` (REST mapping for EObjects)

**Estimate:** 2 PD
**Depends on:** P4-1, Phase 2 complete
**Labels:** `phase-5`, `client`, `impl`

**Description**

Implement `ScopedEObjectsRegistry<EObject>` in `rest.client.impl` for one `(scope, registry)` pair. Maps to:

- `listObjectIds()` → `GET /{s}/registries/{r}`
- `get(objectId)` → `GET /{s}/registries/{r}/stages/released/content?objectId=` (with conditional GET via P2-6)
- `listAll()` / `stream()` → built on the above

**Acceptance Criteria**

- [ ] All `ScopedEObjectsRegistry` methods implemented.
- [ ] Cache reuses the Phase-2 cache infrastructure (key: `(scope, registry, objectId)`).
- [ ] Conditional GET works for EObject content.
- [ ] EObjects returned are detached (no shared `Resource`).

---

### P5-2: Wire EObject drift into scope-level watcher

**Estimate:** 1 PD
**Depends on:** P5-1, P2-7
**Labels:** `phase-5`, `client`, `drift`

**Description**

The Phase-2 watcher already calls `HEAD /{scope}` and parses `Atlas-Changed-Objects`. Extend handling so the listed `(registry, objectId)` tuples invalidate the cache and fire `DriftListener.onObjectChanged` / `onObjectRemoved`.

**Acceptance Criteria**

- [ ] `Atlas-Changed-Objects` parsed and dispatched.
- [ ] Cache entries for affected objects evicted.
- [ ] `onObjectChanged` / `onObjectRemoved` fire as appropriate.

---

### P5-3: Wire `ModelAtlasClient.registry(scope, registry)`

**Estimate:** 0.5 PD
**Depends on:** P5-1
**Labels:** `phase-5`, `client`, `api`

**Description**

Make the `registry(scope, registry)` and `listRegistries(scope)` methods on `ModelAtlasClient` functional. `listRegistries` reads `Scope.registries` from `GET /scopes/{s}`.

**Acceptance Criteria**

- [ ] `listRegistries(scope)` returns the server-known registries.
- [ ] `registry(scope, registry)` returns a working `ScopedEObjectsRegistry<EObject>` instance.
- [ ] Repeated calls for the same `(scope, registry)` return the same instance (or at least one backed by the same cache).

---

### P5-4: Publish `ScopedEObjectsRegistry<EObject>` OSGi service per (scope, registry)

**Estimate:** 2 PD
**Depends on:** P5-1, P3-1
**Labels:** `phase-5`, `client`, `osgi`

**Description**

In `rest.client.osgi`, register one `ScopedEObjectsRegistry<EObject>` OSGi service per `(scope, registry)` with properties `atlas.scope`, `atlas.registry`, `atlas.stage=released`, `atlas.remote=true`. Service shape matches the in-process server-side publication from P4-3.

**Acceptance Criteria**

- [ ] `@Reference(target = "(&(atlas.scope=jena)(atlas.registry=cocl))") ScopedEObjectsRegistry<EObject>` resolves against the client publication.
- [ ] On drift, the publication is replaced atomically (same per-scope lock idea as P3-9, scope-and-registry-scoped).
- [ ] Service unregistered cleanly on bundle deactivation.

---

### P5-5: Acceptance — validation runs unchanged against in-process & remote

**Estimate:** 2 PD
**Depends on:** P5-4, P4-6
**Labels:** `phase-5`, `client`, `acceptance`, `tests`

**Description**

The validation service runs unchanged against (a) the in-process Atlas server and (b) a remote Atlas backend exposed by the client. This is the proof of Goal 1 (contract-identical surface). One test, two configurations.

**Acceptance Criteria**

- [ ] Same validation test scenario passes against in-process and remote backends.
- [ ] No code change in the validation bundle between the two runs — only OSGi configuration differs.
- [ ] CI runs both configurations.

---

### P5-6: Object identity + cross-reference tests via Atlas-aware ResourceSet

**Estimate:** 2 PD
**Depends on:** P5-4, P3-10
**Labels:** `phase-5`, `client`, `tests`

**Description**

Edge cases for object identity and cross-references:

- `get(id)` cache hit returns the same instance (per the *Open Question* recommendation in the design doc — confirm and document).
- A fetched EObject referencing another by URI lazily resolves through the Atlas-aware `ResourceSet`.
- Jürgen's case: two interdependent EPackages, one unloaded → proxy → re-resolution succeeds because the delegating registry guarantees the package is rooted in a `ResourceSet`.

**Acceptance Criteria**

- [ ] Identity policy verified by test (same instance on cache hit within one client lifetime).
- [ ] Cross-reference resolution via Atlas-aware `ResourceSet` works for a referenced remote EObject.
- [ ] Jürgen's interdependent-package scenario resolves correctly post-unload.
