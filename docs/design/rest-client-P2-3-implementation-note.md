# P2-3 — Implementation note (REST mapping: EPackage list + content GET)

**Ticket:** P2-3 "REST mapping for EPackage list + content GET" (Phase 2).
**Depends on:** P2-2. **Date:** 2026-06-05.

## Scope

Wire the read-only EPackage REST calls: `listScopeNames`, `listNsUris(scope)`,
`getEPackage(nsUri)` / `ensureAvailable(nsUri)` (scope-walking + first-hit-wins), plus error mapping to
the typed exception hierarchy. The package-body → `EPackage` decode is **P2-4** and sits behind a seam;
caching is **P2-5**, so `refresh` currently behaves like a fresh fetch.

## REST mapping

| Client call | HTTP | Notes |
|---|---|---|
| `listScopeNames()` | `GET /scopes` | parse `{"scopes":[{"name":…}]}` |
| `listNsUris(scope)` | `GET /{scope}/schema/stages/{view}` | parse `{"metadata":[{"objectId":…}]}`; `204` → empty |
| `getEPackage(nsUri)` / `ensureAvailable(nsUri)` | `GET /{scope}/schema/stages/{view}/content?nsUri=…` | walk resolved scopes; `200` → deserialize & return; else → next scope |

- **JSON parsing.** Responses are read as `String` and parsed with a shared Jackson `ObjectMapper`
  tree (no JAX-RS JSON provider needs registering on the plain client). `jackson-core` +
  `jackson-databind` + `jackson-annotations` added to the impl buildpath.
- **nsURI recovery.** ~~For schema packages the server's `objectId` is the Base64-URL encoding of the
  nsURI (`SchemaPackagesResource.encodePackageNsURI`), so `listNsUris` Base64-URL-decodes each
  `objectId`.~~ *Superseded 2026-08-03 (server F8): objectIds are opaque UUIDs; the client now reads
  the nsURI from the listing entry's `properties["nsUri"]` and keeps the Base64 decode only as a
  guarded fallback for pre-F8 servers (undecodable entries are skipped).*
- **Content media type.** EPackage content is requested as `application/xmi` (the format the server's
  codec round-trips; matches the existing `SchemaPackagesResourceTest` POSTs). The body is buffered to
  `byte[]` and handed to the `EPackageDeserializer` seam.
- **Scope resolution** (`getEPackage`, anonymous): `scope.allow.list` in order → else single
  `default.scope` → else every scope from `listScopeNames()`. First content `200` wins.

## Configurable stage `view` (not hard-coded "released")

Initially the stage was a `RELEASED_STAGE = "released"` constant. Changed to a configurable
**`view`** (review feedback): `eager.stages` already presumes stage configurability, an `ATLAS_VIEW`
origin property already exists, and "released" is only a server-side naming convention — not safe to
bake in.

- New `ClientConfiguration.view` (String, default `ClientConfiguration.DEFAULT_VIEW = "released"`) +
  `Builder.view(...)`, folded into `equals`/`hashCode`/`toString`/`builder(from)`.
- Both `listNsUris` and the content GET use `…/stages/{view}`. Default behaviour is unchanged
  (`released`); a consumer can now read `draft`/`approved`/any server stage.
- The OSGi front-end (Phase 3) will stamp `atlas.view=<view>` on published services — the config field
  and the `AtlasProperties.ATLAS_VIEW` constant line up.

**Deviation from the design's REST table.** The table lists `listNsUris(s)` → `GET /{s}/schema` (the
released-only alias, which also folds in parent-scope packages). To honour the configurable `view` we
use the stage-explicit `GET /{s}/schema/stages/{view}` for all stages, including `released`. Same
response shape (`ObjectMetadataContainer`), and `204` semantics. Consequence: `listNsUris(scope)` lists
that scope's own packages for the view; cross-scope availability is still handled by `getEPackage`
walking `scope.allow.list`. If parent-scope inheritance in the listing is ever required, it can be added
as a flag later.

## Error mapping

- Transport faults (`ProcessingException` — connect/read timeout, connection refused) →
  `TransportException` (in `RestSupport.get`).
- Non-success status on `listScopeNames`/`listNsUris`: `404` → `NotFoundException`, otherwise →
  `ModelAtlasClientException` (`RestSupport.statusError`, best-effort body in the message).
- `getEPackage` returns `Optional.empty()` on a miss (`204`/`404`/non-200 per scope) — it never throws
  `NotFoundException`, since absence is normal for an Optional-returning lookup; it moves to the next
  scope.

## New / changed types (impl, Private-Package)

- `RemoteEPackageProviderImpl implements RemoteEPackageProvider` — the REST mapping above.
- `EPackageDeserializer` (functional seam) + `EPackageDeserializer.unsupported()` placeholder (P2-4
  supplies the real XMI reader). `ModelAtlasClientImpl` defaults to `unsupported()`; a package-private
  constructor injects a real/fake one.
- `RestSupport` — `get` (transport mapping), `isSuccess`, `statusError`, `parse` (shared `ObjectMapper`).
- `ModelAtlasClientImpl` — `listScopeNames()` implemented; `ePackages()` lazily builds & caches a
  `RemoteEPackageProviderImpl` (wired with `this::listScopeNames` as the scope-name fallback). `close`
  unchanged; `checkForDrift`/`addDriftListener` (P2-6) and `newResourceSet` (P2-7) still fail fast.

## Acceptance-criteria coverage

| Criterion | Test |
|---|---|
| `listScopeNames()` calls `GET /scopes` | `ModelAtlasClientRestMappingTest.listScopeNames_getsScopes_andParsesNames` |
| `listNsUris(scope)` calls `GET /{scope}/schema[/stages/{view}]` | `RemoteEPackageProviderImplTest.listNsUris_getsSchemaPath_andDecodesObjectIds`, `…_usesConfiguredView` |
| `getEPackage` walks `scope.allow.list`, first hit wins | `…getEPackage_walksScopes_firstHitWins`, `…_allScopesMiss_returnsEmpty`, `…_emptyAllowList_usesScopeNamesSupplier`, `…_defaultScope_usedWhenAllowListEmpty` |
| `ensureAvailable(nsUri)` exists (warm-up / registry-delegate path) | `…ensureAvailable_delegatesToGetEPackage` |
| Errors → typed hierarchy (`404`→NotFound, transport→Transport) | `…listNsUris_404_throwsNotFound`, `…_errorStatus_throws`, `…transportFault_mapsToTransportException`, `ModelAtlasClientRestMappingTest.listScopeNames_errorStatus_throws` |

Tests mock the `WebTarget` fluent chain (each `path`/`queryParam` returns the same target) and assert
the captured path segments + status handling — no live server. Note: `MediaType.valueOf(...)` /
`MediaType.toString()` route through `RuntimeDelegate` (absent without a JAX-RS runtime), so production
builds the content-type string from `getType()/getSubtype()` and tests use the `MediaType(type,subtype)`
constructor.

## Build status

As of 2026-06-05 `:rest.client.api:build` and `:rest.client.impl:build` are green (22 unit tests).
Pending in Phase 2: P2-4 (XMI deserialization — fills the `EPackageDeserializer` seam), P2-5 (cache),
P2-6 (drift), P2-7 (Atlas-aware ResourceSet).
