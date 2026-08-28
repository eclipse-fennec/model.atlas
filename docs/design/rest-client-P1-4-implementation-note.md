# P1-4 — Implementation note (`Vary: Accept` + per-representation ETag)

**Ticket:** P1-4 "`Vary: Accept` on cacheable responses" (Phase 1).
**Depends on:** P1-2.
**Companion docs:** [`rest-client.md`](./rest-client.md) §"Phase 1", [`rest-client-tickets.md`](./rest-client-tickets.md),
[`rest-client-P1-2-implementation-note.md`](./rest-client-P1-2-implementation-note.md),
[`rest-client-P1-3-implementation-note.md`](./rest-client-P1-3-implementation-note.md).
**Date:** 2026-06-04.

## What was implemented

Both changes live in `ObjectMetadataResponseFilter` (the P1-2/P1-3 filter), keeping all caching semantics
in one place.

1. **`Vary: Accept`** is added to every cacheable (metadata-attached) response via `addVaryAccept(...)`.
   It is set *before* the 304 rewrite, so a `304 Not Modified` carries it too. The helper is idempotent —
   if the response already varies on `Accept` (case-insensitive substring check over existing `Vary`
   values) it does nothing, so it never produces a duplicate `Vary: Accept`.

2. **Per-representation ETag.** The emitted ETag is no longer the bare `contentHash`; it is
   `contentEtagValue(metadata, responseContext.getMediaType())` = `contentHash + "." + token(mediaType)`,
   where `token` is the `type/subtype` with parameters dropped and non-`[A-Za-z0-9_.-]` chars replaced by
   `_` (e.g. `application/json` → `application_json`). So the same object served as XMI and as JSON gets
   distinct strong ETags, and a shared cache keyed on ETag cannot return the wrong representation. Falls
   back to the bare `contentHash` when the response media type is unknown, and to no ETag when there is no
   content hash.

3. **Conditional GET stays consistent.** `If-None-Match` is now compared against the same
   media-type-folded `etagValue`, not the raw `contentHash`. Because a client always echoes back the ETag
   it received (which already carried the representation token), conditional GETs continue to hit/miss
   correctly. `If-Modified-Since` is unchanged.

## Acceptance criteria

| Criterion | Status |
|---|---|
| `Vary: Accept` present on all content GET responses (including `304`) | ✅ set before the 304 rewrite; tests assert it on `200` and on `304` |
| Same nsURI under `Accept: application/xml` vs `application/json` produces distinct ETags | ✅ ETag folds the response media type; test compares `mediaType=application/xml` vs `application/xmi` ETags |

Tests added to `SchemaPackagesResourceTest`:
- `testGetPackageContent_SetsVaryAccept` — `Vary` present and contains `Accept` on a content `200`.
- `testGetPackageContent_VaryAcceptPresentOn304` — `Vary: Accept` retained on the `304`.
- `testGetPackageContent_DistinctETagPerRepresentation` — XML vs XMI of the same nsURI → different ETags.

## Notes / deviations

- **Representation discriminator = response `Content-Type`,** read from `ContainerResponseContext.getMediaType()`.
  The content GET sets `Content-Type` to the resolved media type (incl. the `mediaType` query-param
  override), so this is the actual negotiated representation. The pre-existing
  `ModelAtlasRestConstants.RESOLVED_MEDIA_TYPE` request property would also work, but the response media
  type is what the client/cache actually sees and needs no extra request-property plumbing.
  
- **Applied to metadata responses too.** Metadata GETs are always JSON, so folding the media type is a
  no-op for them today; `Vary: Accept` on them is harmless. P1-5 will give metadata its own ETag (a hash
  over `contentHash, version, status, lastChangeTime`) distinct from content.
  
  
