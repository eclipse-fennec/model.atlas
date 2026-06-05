# P1-7 — Implementation note (scope-level HEAD with change hints)

**Ticket:** P1-7 "Scope-level HEAD endpoint with change hints" (Phase 1).
**Plan:** [`rest-client-P1-7-plan.md`](./rest-client-P1-7-plan.md) (decisions: strategy **A**, mount at
`/scopes/{scopeName}`). **Date:** 2026-06-04.

## What was implemented

### `ScopeAggregateService` (new `@Component`, singleton)

`org.eclipse.fennec.model.atlas.rest.application.aggregate.ScopeAggregateService`, referencing
`ScopeServiceCollector`. Responsibilities:

- **Manifest** — for a scope, iterate `getAllRegistries()` × `listAllForRegistry(r)` (all registries,
  all stages, incl. inherited parent-final entries) into `Map<key, ManifestEntry>`, where
  `key = registry␟stage␟objectId` (ASCII unit separator ``) and `ManifestEntry` carries
  `(registry, objectId, nsUri, contentHash)`. nsURI comes from `getProperties().get("nsUri")`.
- **Aggregate ETag** — `static aggregateEtag(manifest)`: each entry rendered as `key␟contentHash`,
  **sorted**, joined with record separator ``, SHA-256 → hex. Sorting makes it deterministic and
  reorder-stable. Pure/package-private for unit testing.
- **Last-Modified** — `max` over non-null `getLastChangeTime()`; absent if none.
- **Snapshot cache** — per scope, a bounded access-ordered `LinkedHashMap` LRU (`MAX_SNAPSHOTS_PER_SCOPE
  = 16`, synchronized; outer `ConcurrentHashMap<scope, …>`). Every `computeAggregate` stores
  `etag → manifest` so a later request citing that etag can be diffed.
- **`diffSince(scope, ifNoneMatch, current)`** — looks up the baseline manifest by the normalized
  If-None-Match tag; if absent → `baselineKnown=false` (no exact diff). Otherwise reports added ∪
  removed ∪ content-changed, mapping schema-registry entries to nsURIs (`Atlas-Changed-NsUris`) and the
  rest to `registry/objectId` (`Atlas-Changed-Objects`), both deduped + sorted.
- **`matchesIfNoneMatch`** — `*`, comma lists, `W/`, quotes.

### `HEAD /scopes/{scopeName}` on `ScopesResource`

`scopeAggregate(...)`: `computeAggregate` → `404` if scope unknown; else build a `200` with
`ETag` + `Vary: Accept` (+ `Last-Modified` when present). If `If-None-Match` matches → `304` (no diff
headers). Otherwise `diffSince`; when the baseline is known, attach the non-empty `Atlas-Changed-*`
headers. HEAD → no body. Swagger `@Operation`/`@ApiResponse(200,304,404)` added. This path does **not**
attach an `ObjectMetadata`, so `ObjectMetadataResponseFilter` does not touch it — the aggregate
validators are set directly on the builder.

## Acceptance criteria

| Criterion | Status |
|---|---|
| Deterministic aggregate ETag | ✅ `testScopeHead_ReturnsDeterministicAggregateETag` |
| Stable under reordering of entries | ✅ `testScopeHead_ETagStableUnderReordering` (delete + recreate in opposite order → same ETag) |
| `If-None-Match` matching → `304` | ✅ `testScopeHead_IfNoneMatchMatch_Returns304` (asserts no diff headers) |
| Stale `If-None-Match` → `200` + exact diff | ✅ `testScopeHead_StaleIfNoneMatch_Returns200WithExactDiff` (adds one pkg; asserts `Atlas-Changed-NsUris` == exactly that nsURI) |
| Documented in OpenAPI/Swagger | ✅ annotations on the HEAD method |

Plus `testScopeHead_UnknownBaseline_Returns200WithoutDiff` (LRU miss → `200`, no diff headers) and
`testScopeHead_UnknownScope_Returns404`. Tests live in `ScopesResourceTest` (the endpoint's own resource;
it already targets `/scopes` and waits for `ScopesResource`). Added there: a `scopesTarget(scope)` =
`/scopes/{scope}` overload and a small `createPackage`/`deletePackage` pair that posts to the scope's
schema draft stage to populate the scope (`SchemaPackagesResource` is co-registered in the same bundle).

## Notes / deviations

- **Mount path** `/scopes/{scopeName}` (co-located with the existing scope GET), not the ticket's literal
  `/{scope}` (a bare top-level template would collide with `/scopes` and the `/{scope}/...` content
  resources).
- **Identity keys include stage** internally (so a package present in several stages with different
  content doesn't collapse); for packages `objectId` ≡ encoded nsURI, so the diff still reports nsURIs.
- **Exact diff only for cached baselines.** Unknown/evicted baseline → `200` with no diff headers
  ("re-sync fully"). This matches the polling drift-watcher's access pattern (it always cites its most
  recent etag). LRU cap is a constant for now (trivially promotable to a `@Component` config property).
- **Build status:** as of 2026-06-05 the full build and Phase-1 test suite (`rest.application` +
  `rest.tests`) run green.
