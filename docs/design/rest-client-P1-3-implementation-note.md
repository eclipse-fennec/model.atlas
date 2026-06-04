# P1-3 — Implementation note (Conditional GET / 304)

**Ticket:** P1-3 "Conditional GET handling (304)" (Phase 1).
**Depends on:** P1-2.
**Companion docs:** [`rest-client.md`](./rest-client.md) §"Phase 1", [`rest-client-tickets.md`](./rest-client-tickets.md),
[`rest-client-P1-2-implementation-note.md`](./rest-client-P1-2-implementation-note.md).
**Date:** 2026-06-03.

## What was implemented

Conditional-GET handling was **centralised into `ObjectMetadataResponseFilter`** (the P1-2 filter),
rather than left as the inline per-resource shortcut. The ticket allowed either; centralising here
also discharges the seam P1-2 deferred (the inline `evaluateConditionalGet` 304 construction).

- The filter now evaluates preconditions against the attached `ObjectMetadata` and, on a safe
  (`GET`/`HEAD`) request that produced a `200` and matches, rewrites the response to `304 Not Modified`
  with `setStatus(304)` + `setEntity(null)`. The `ETag` / `Last-Modified` validators are stamped *before*
  the check, so the `304` retains them.
- `If-None-Match`: handles `*`, single tag, and comma-separated lists; strips weak `W/` prefix and quotes;
  compares against `ObjectMetadata.getContentHash()`.
- `If-Modified-Since`: parses the RFC 1123 HTTP-date and compares at **second precision** against
  `ObjectMetadata.getLastChangeTime()` (`lastChangeTime <= If-Modified-Since` → not modified).
- RFC 7232 precedence: when `If-None-Match` is present, `If-Modified-Since` is ignored.
- The inline `evaluateConditionalGet(...)` helpers and their now-unused `EntityTag` imports were removed
  from `SchemaPackagesResource` and `ObjectRegistryResource`. `checkIfMatch(...)` (If-Match → 412) stays
  in the resources for P1-6.

## Acceptance criteria

| Criterion | Status |
|---|---|
| `If-None-Match` matching the current ETag → `304`, no body | ✅ filter rewrites to 304 + `setEntity(null)`; test asserts `!hasEntity()` |
| `If-None-Match` with a stale ETag → `200` with body and current ETag | ✅ test asserts 200 + ETag + body |
| `If-Modified-Since` honoured against `lastChangeTime` | ✅ tests for not-modified (304) and modified (200) |
| `304` responses still carry the current ETag | ✅ validators stamped before the 304 rewrite; test asserts equal ETag on 304 |

Tests added: `SchemaPackagesResourceTest` — `…_IfNoneMatchHit_Returns304WithETagNoBody`,
`…_IfNoneMatchStale_Returns200WithBodyAndETag`, `…_IfModifiedSince_NotModified_Returns304`,
`…_IfModifiedSince_Modified_Returns200`; `ObjectRegistryResourceTest` —
`…_IfNoneMatchHit_Returns304WithETagNoBody`, `…_IfModifiedSince_NotModified_Returns304`. The pre-existing
metadata-GET `If-None-Match` 304/200 tests now exercise the filter path unchanged.

## Notes / deviations

- **Centralised in a response filter, not a request filter.** A `ContainerRequestFilter` cannot
  short-circuit before the resource runs without independently fetching the metadata (the resource is what
  loads it). The response filter reuses the metadata the resource already attached. Consequence: the
  content is still fetched/serialized by the resource before the filter discards it for a `304` — identical
  to the previous inline behaviour, so no regression. A fetch-avoiding request-filter optimisation is
  possible but out of scope and would duplicate the storage lookup.
- **Scope.** Applies only to single-object content/metadata GETs that attach `ObjectMetadata`. List
  endpoints (no metadata attached) are untouched. The scope-level `HEAD` conditional handling is P1-7.
- **`Vary: Accept`** on cacheable responses (including 304) remains **P1-4** — not added here.
