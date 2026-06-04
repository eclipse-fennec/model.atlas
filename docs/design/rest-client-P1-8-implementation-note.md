# P1-8 — Implementation note (server-side Phase-1 test suite)

**Ticket:** P1-8 "Server-side tests for ETag / 304 / 412 / scope HEAD" (Phase 1).
**Depends on:** P1-2…P1-7. **Date:** 2026-06-04.

## Approach

P1-2…P1-7 each shipped focused tests next to the resource they exercise (the agreed convention: tests
live with their endpoint). P1-8 is therefore a **gap audit + fill** against the ticket's acceptance
criteria, not a consolidation churn. Tests stay in `SchemaPackagesResourceTest`,
`ObjectRegistryResourceTest`, and `ScopesResourceTest`.

## Acceptance-criteria coverage matrix

| Criterion | Schema (`SchemaPackagesResourceTest`) | Object registry (`ObjectRegistryResourceTest`) | Scope (`ScopesResourceTest`) |
|---|---|---|---|
| ETag on content / metadata GETs | `testGetPackageContent_ReturnsStrongETagAndLastModified`, `…_ReturnsETag`, `testListPackagesInStage_WithNsUri_ReturnsETag`, `testMetadataAndContentHaveDistinctETags` | `testGetObjectContent_ReturnsStrongETagAndLastModified`, `testGetObjectMetadata_IfNoneMatchHit_Returns304` | — |
| `If-None-Match` round-trip (match→304, miss→200+ETag) | `…_IfNoneMatchHit_Returns304WithETagNoBody`, `…_IfNoneMatchStale_Returns200WithBodyAndETag`, list variants | `testGetObjectContent_IfNoneMatchHit_Returns304WithETagNoBody`, `testGetObjectMetadata_IfNoneMatch{Hit,Miss}` | aggregate: `…_IfNoneMatchMatch_Returns304`, `…_UnknownBaseline_Returns200WithoutDiff` |
| `If-Modified-Since` round-trip | `…_IfModifiedSince_NotModified_Returns304`, `…_Modified_Returns200` | `…_IfModifiedSince_NotModified_Returns304`, **`…_Modified_Returns200`** (new) | — |
| `Vary: Accept` + distinct ETag per `Accept` | `…_SetsVaryAccept`, `…_VaryAcceptPresentOn304`, `…_DistinctETagPerRepresentation` | **`…_SetsVaryAccept`, `…_VaryAcceptPresentOn304`, `…_DistinctETagPerRepresentation`** (new) | — |
| `If-Match` write → 412 / matching → 200/201 + new ETag | update success/fail, delete fail, transition success/fail, `testCreatePackage_ReturnsETag` (201+ETag), **`testCreatePackage_Overwrite_IfMatchSuccess` / `…_Fail_Returns412`** (new) | update success/fail, delete fail, transition success/fail, **`testCreateObject_Override_IfMatchSuccess`** (new) | — |
| `HEAD /{scope}` stale `If-None-Match` → 200 + exact `Atlas-Changed-*` | — | — | `…_StaleIfNoneMatch_Returns200WithExactDiff` (nsUris), **`…_StaleIfNoneMatch_ReportsChangedObjects`** (objects, new), `…_ReturnsDeterministicAggregateETag`, `…_ETagStableUnderReordering`, `…_UnknownScope_Returns404` |
| Tests run in CI | see below | | |

**Bold** = added in P1-8. Everything else already existed from P1-2…P1-7.

## Gaps filled in P1-8

- **Object-registry parity for `Vary`/representation ETags** — the response filter is resource-agnostic,
  but only the schema resource asserted it. Added `Vary: Accept` (200 and 304) and
  distinct-ETag-per-representation tests on `getObjectContent`.
- **Object-registry `If-Modified-Since` modified→200** — only the 304 case existed.
- **Create-with-overwrite `If-Match`** — P1-6 added this write path on both resources but it was
  untested. Added a matching→200+ETag test on both, plus a mismatch→412 on the schema resource.
- **`Atlas-Changed-Objects`** — the scope-HEAD diff only had the schema (`Atlas-Changed-NsUris`) branch
  covered; added a test that registers two `person` objects and asserts the non-schema branch reports
  exactly `person/<objectId>`.

## CI

CI (`.github/workflows/build.yml`) runs `./gradlew clean build`; the bnd `testOSGi` task runs as part of
the build lifecycle (its `generated/test-reports/testOSGi/` outputs exist), and the workflow already
uploads `**/test-reports/*/TEST-*.xml`. So the Phase-1 OSGi integration tests run in CI with no workflow
change — criterion met once the project compiles.

## Notes

- Build status: `rest.application` + `rest.tests` COMPILE; the full OSGi run remains blocked by the
  unrelated dependency compiler issue, so the suite is not yet confirmed green by execution.
- Phase 1 (P1-1…P1-8) is now complete pending that green run.
