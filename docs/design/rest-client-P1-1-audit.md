# P1-1 — Audit of existing server-side ETag / conditional-request support

**Ticket:** P1-1 (Phase 1, `audit`/`etag`).
**Companion docs:** [`rest-client.md`](./rest-client.md) §"Phase 1 / Verification checklist", [`rest-client-tickets.md`](./rest-client-tickets.md).
**Scope of audit:** `org.eclipse.fennec.model.atlas.rest.application` (resources) and `org.eclipse.fennec.model.atlas.rest.tests`.
**Date:** 2026-06-03.

## TL;DR

The server is **further along than the design doc assumed**. Per-object strong `ETag`, `If-None-Match` → `304`, and `If-Match` → `412` are already implemented and tested on both `SchemaPackagesResource` and `ObjectRegistryResource`. What is missing is: `Last-Modified` + `If-Modified-Since`, `Vary: Accept`, a *metadata-specific* ETag (today metadata reuses the content hash), the whole **scope-level `HEAD /{scope}`** endpoint with `Atlas-Changed-*` diff hints, and centralisation of the header logic into a `ContainerResponseFilter`.

This reshuffles the Phase-1 follow-up tickets considerably — see [Impact on follow-up tickets](#impact-on-follow-up-tickets-p1-2p1-8).

## Where the existing implementation lives

The logic is **duplicated as private helpers** in each resource (not centralised in a filter):

| Helper | `SchemaPackagesResource.java` | `ObjectRegistryResource.java` |
|---|---|---|
| `addETagHeader(rb, md)` — `rb.tag(new EntityTag(md.getContentHash()))` (strong tag) | `705–711` | `599–603` |
| `checkIfMatch(md)` — `If-Match` → `412` | `718–733` | `610–625` |
| `evaluateConditionalGet(rb, md)` — `If-None-Match` → `304` | `739–747` | `631–639` |

Content GET wiring: `SchemaPackagesResource#getPackageContent` `368–400`; `ObjectRegistryResource#getObjectContent` `351–382`.
Metadata GET wiring (single-object branch): `SchemaPackagesResource#listPackagesInStage` `204–233` (ETag at `231–232`); `ObjectRegistryResource#listObjectsInRegistry` `175–208` (ETag at `206–207`).
`ScopesResource.java` has only two `@GET`s (`listScopes`, `getScopeByName`) — **no `@HEAD`**.

## Verification checklist — classification

Items are the verification checklist in `rest-client.md` (§"Verification checklist (against existing implementation)").

| # | Checklist item | Status | Evidence / gap |
|---|---|---|---|
| 1 | `SchemaPackagesResource` content GETs emit strong `ETag` | ✅ **implemented** | `getPackageContent` → `addETagHeader` (`SchemaPackagesResource:394–398`, `705–711`). Strong tag (`new EntityTag(hash)`, weak=false). Tested: `testGetPackageContent_ReturnsETag`. |
| 2 | `ObjectRegistryResource` content GETs emit strong `ETag` | ✅ **implemented** (test gap) | `getObjectContent` → `addETagHeader` (`ObjectRegistryResource:377–381`, `599–603`). No test asserts the ETag *on the content GET directly* (only on the metadata GET, via the 304 test). |
| 3 | Metadata GETs emit `ETag` (covering metadata-only changes) | ⚠️ **partial** | An ETag *is* emitted on the single-object metadata GET, but it is the **content hash** (`addETagHeader` is the only tag builder). The design requires a distinct validator = hash over `(contentHash, version, status, lastChangeTime)` so that a stage transition with no byte change still bumps the metadata ETag. Today it would **not** — content and metadata share one ETag. |
| 4 | All cacheable responses set `Vary: Accept` | ❌ **missing** | No `Vary` header anywhere in `rest.application`. Content is negotiated via `Accept` (`getResolvedMediaType()`), so this is a real correctness gap, not just hygiene. |
| 5 | `If-None-Match` / `If-Modified-Since` → `304` (no body) | ⚠️ **partial** | `If-None-Match` fully done & tested (`evaluateConditionalGet`; hit→304, miss→200). **`If-Modified-Since` not handled at all**, and there is **no `Last-Modified` header** emitted (per-object requirement #2), so IMS cannot work yet. `ObjectMetadata.getLastChangeTime()` exists, so the source data is available. |
| 6 | `If-Match` on workflow writes → `412` on mismatch | ⚠️ **partial** | Implemented & tested on **update (PUT/POST) and DELETE** in both resources (`checkIfMatch`). **Not applied to create or transition**: `createPackage`/`createObject` (POST/PUT) and `transitionPackage`/`transitionObject` (POST) never call `checkIfMatch`. P1-6 names "POST/PUT/transition" endpoints, so transition is a gap; create is arguably also in scope. |
| 7 | `HEAD /{scope}` returns aggregate ETag, `If-None-Match` → `304` | ❌ **missing** | No `@HEAD` method exists on `ScopesResource` (or anywhere). No aggregate-hash computation over `(nsUri, contentHash)` + `(registry, objectId, contentHash)`. |
| 8 | `HEAD /{scope}` populates `Atlas-Changed-NsUris` / `Atlas-Changed-Objects` | ❌ **missing** | Depends on #7; nothing exists. No `Atlas-Changed-*` headers referenced anywhere. |
| 9 | Tests in `rest.tests` cover all of the above | ⚠️ **partial** | Solid coverage of ETag/304/412 (see below). **Untested:** `Vary: Accept`, `Last-Modified`, `If-Modified-Since`, distinct metadata-vs-content ETag, content-GET ETag on `ObjectRegistryResource`, and the entire scope `HEAD`. |

### Per-object requirement #2 (`Last-Modified`) and #7 (`Cache-Control`)

The design's per-object requirement list also calls for **`Last-Modified` from `ObjectMetadata.lastChangeTime`** (folded into checklist #5 above — **missing**) and **`Cache-Control: private, must-revalidate` once auth is added** (not in the checklist; **N/A until auth lands**, track with the auth work, not Phase 1).

## Existing test coverage (what's already green)

`org.eclipse.fennec.model.atlas.rest.tests`, sections marked `// ========== ETag / Idempotency Tests ==========`:

**`SchemaPackagesResourceTest.java`** (`1058–1242`): `…_WithNsUri_ReturnsETag`, `getPackageContent_ReturnsETag`, `createPackage_ReturnsETag`, `IfNoneMatchHit_Returns304`, `IfNoneMatchMiss_Returns200`, `updatePackageContent_IfMatchSuccess`, `…_IfMatchFail_Returns412`, `…_NoIfMatch_StillWorks`, `deletePackage_IfMatchFail_Returns412`.

**`ObjectRegistryResourceTest.java`** (`781–915`): `getObjectMetadata_IfNoneMatchHit_Returns304`, `…_IfNoneMatchMiss_Returns200`, `updateObjectContent_IfMatchSuccess`, `…_IfMatchFail_Returns412`, `…_NoIfMatch_StillWorks`, `deleteObject_IfMatchFail_Returns412`.

## Impact on follow-up tickets (P1-2…P1-8)

| Ticket | Recommended disposition |
|---|---|
| **P1-2** Strong ETag + `Last-Modified` on content GETs | **Reduced.** Strong ETag already on both content GETs. Remaining: add `Last-Modified`, and **refactor the duplicated helpers into the `ContainerResponseFilter`** the ticket calls for (currently inline & duplicated). |
| **P1-3** Conditional GET (304) | **Mostly done — `If-None-Match` only.** Remaining: `If-Modified-Since` (blocked on `Last-Modified` from P1-2). Consider closing the `If-None-Match` portion as already-met. |
| **P1-4** `Vary: Accept` | **Open as written.** Nothing exists. |
| **P1-5** ETag on metadata responses | **Re-scope.** An ETag exists but is the content hash. Real work = compute the *distinct* metadata validator over `(contentHash, version, status, lastChangeTime)` and verify a content-less stage transition changes it. |
| **P1-6** `If-Match` on writes (412) | **Reduced.** Update + delete done & tested. Remaining: extend to **transition** (and decide on create), per the ticket's "POST/PUT/transition" wording. |
| **P1-7** Scope-level `HEAD` | **Open as written, full size.** Nothing exists; biggest remaining Phase-1 item. |
| **P1-8** Server-side tests | **Reduced but real.** Keep/relabel existing ETag/304/412 tests; add: `Vary: Accept`, `Last-Modified`/IMS, distinct metadata ETag under stage transition, `ObjectRegistry` content-GET ETag, and the full scope-`HEAD` round-trip (304 + `Atlas-Changed-*`). |

## Notes for implementers

- **Centralisation (P1-2):** resources already stash `ObjectMetadata` (they hold `contentMetadata`/`existingMetadata` locally); to feed a `ContainerResponseFilter` cleanly, attach the metadata to a request property (`ContainerRequestContext#setProperty`) as the design doc anticipates, then read it in the filter. `ObjectRegistryResource` already injects `ContainerRequestContext` (`:89–90`); `SchemaPackagesResource` would need it added.
- **Strong vs weak tags:** `new EntityTag(String)` is a strong tag — matches the design ("strong `ETag`"). Keep it.
- **ETag quoting:** `evaluateConditionalGet` strips quotes from the incoming `If-None-Match` before comparing to the raw hash (`:742`). That works for a single value but won't handle a multi-value `If-None-Match` list or `*`. Worth hardening in P1-3.
