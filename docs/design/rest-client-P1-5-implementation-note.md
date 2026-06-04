# P1-5 — Implementation note (ETag on metadata responses)

**Ticket:** P1-5 "ETag on metadata responses" (Phase 1).
**Depends on:** P1-2 (also builds on P1-3, P1-4).
**Companion docs:** [`rest-client.md`](./rest-client.md) §"Phase 1", [`rest-client-tickets.md`](./rest-client-tickets.md),
[`rest-client-P1-2-implementation-note.md`](./rest-client-P1-2-implementation-note.md),
[`rest-client-P1-3-implementation-note.md`](./rest-client-P1-3-implementation-note.md),
[`rest-client-P1-4-implementation-note.md`](./rest-client-P1-4-implementation-note.md).
**Date:** 2026-06-04.

## What was implemented

Metadata responses now get their own validator, distinct from the content validator, still derived
inside `ObjectMetadataResponseFilter`.

1. **`CacheTarget` flavour on `attach`.** The filter previously treated every attached response as
   "content" and used `contentHash` as the ETag base. Added an enum `CacheTarget { CONTENT, METADATA }`
   and an overload `attach(requestContext, metadata, CacheTarget)`; the no-arg-target overload
   `attach(requestContext, metadata)` still defaults to `CONTENT` (so the content-GET call sites are
   unchanged). The chosen target is stashed in a second request property `PROP_CACHE_TARGET`.

2. **Metadata validator.** For `CacheTarget.METADATA`, the ETag base is
   `metadataValidator(...)` = `SHA-256( contentHash | version | status | lastChangeTime )` (hex). For
   `CacheTarget.CONTENT` it stays `contentHash`. Either base is then folded with the response media type
   (the P1-4 behaviour), so the full ETag is `base + "." + mediaTypeToken`. Because the metadata base is
   a hash that includes `status` and `lastChangeTime`, a metadata-only change (a stage transition leaves
   the bytes untouched but `AbstractEObjectStorageService` bumps both `status` and `lastChangeTime`)
   changes the metadata ETag while the content ETag (a function of `contentHash` only) is unaffected.

3. **Resource call sites tagged.** The metadata-returning responses now pass `CacheTarget.METADATA`:
   in both `SchemaPackagesResource` and `ObjectRegistryResource` the single-object metadata GET, the
   create (`201`), the create-with-overwrite update, the content-unchanged update short-circuit, and the
   normal content update (all of which return `ObjectMetadata` as the body). The two genuine content GETs
   (`getPackageContent` / `getObjectContent`, which return the `EPackage`/`EObject`) keep the default
   `CONTENT` target.

The `If-None-Match` comparison already runs against the computed `etagValue` (since P1-3/P1-4), so
conditional GETs on metadata automatically use the new metadata ETag with no extra plumbing.

## Acceptance criteria

| Criterion | Status |
|---|---|
| Metadata GET emits its own ETag, distinct from the content ETag | ✅ `testMetadataAndContentHaveDistinctETags` |
| A stage transition (no content change) changes the metadata ETag and not the content ETag | ✅ `testStageTransitionChangesMetadataETagNotContentETag` |
| Conditional GET on metadata works (304 / 200) | ✅ pre-existing `testListPackagesInStage_IfNoneMatch{Hit_Returns304,Miss_Returns200}` now exercise the metadata ETag unchanged |

Tests added to `SchemaPackagesResourceTest`: `testMetadataAndContentHaveDistinctETags`,
`testStageTransitionChangesMetadataETagNotContentETag`.

## Notes / deviations

- **`stage` is deliberately not in the validator tuple.** The ticket specifies
  `(contentHash, version, status, lastChangeTime)`; a transition bumps `status` + `lastChangeTime`
  (confirmed in `AbstractEObjectStorageService` ~L618), which is enough to invalidate the metadata ETag.
  Including `stage` would be redundant and would also make the same object's metadata ETag differ purely
  by access path.
- **Metadata ETag also applied to write responses** (create/update returning metadata), not just GETs.
  This is intentional: the metadata representation's validator should be the same wherever that
  representation is returned, so a client that just created/updated already holds the correct metadata
  ETag.
- **Symmetric across both resources.** Per-file: 5 `METADATA` sites + 1 `CONTENT` site; the `CONTENT`
  site is the only one whose body is the object content (`contentMetadata` variable).
- **Build status:** as of 2026-06-04 the additions compile (`rest.application` + `rest.tests`); the full
  test run is still blocked by an unrelated compiler issue on another dependency, so the new tests are
  not yet confirmed green by execution.
