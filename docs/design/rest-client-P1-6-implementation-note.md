# P1-6 — Implementation note (`If-Match` on workflow writes → 412)

**Ticket:** P1-6 "`If-Match` on workflow writes (412)" (Phase 1).
**Depends on:** P1-2 (and reconciles with P1-4/P1-5, which changed the emitted ETags).
**Companion docs:** [`rest-client.md`](./rest-client.md) §"Phase 1", [`rest-client-tickets.md`](./rest-client-tickets.md),
[`rest-client-P1-4-implementation-note.md`](./rest-client-P1-4-implementation-note.md),
[`rest-client-P1-5-implementation-note.md`](./rest-client-P1-5-implementation-note.md).
**Date:** 2026-06-04.

## Background — why this wasn't purely additive

`checkIfMatch` already existed and was already called *before the mutation* on **update-content** and
**delete** in both resources. But it compared the raw `If-Match` value against the raw `contentHash`,
while P1-4/P1-5 changed what we *emit*: content ETags are now `contentHash.<mediaToken>` (folded) and
metadata ETags are `SHA-256(contentHash|version|status|lastChangeTime).<mediaToken>`. A client echoing
back the ETag it received would therefore never equal the raw `contentHash` → spurious `412`. So P1-6
had to reconcile the precondition path with the emit path, not just add call sites.

## Key decision — the precondition is representation-independent

`If-Match` is an optimistic-concurrency check on the **resource version**, so it must work regardless of
which representation the client fetched. The media-type fold is a *caching* concern (GET), not a
*concurrency* concern (write). Accordingly:

- The precondition compares against the **unfolded base validator** (`contentHash` for content writes,
  the metadata hash for metadata writes).
- A client may send back **any** form it holds: the bare base, or a representation-folded ETag
  (`base + "." + token`). `ifMatchSatisfied` accepts a tag that `equals(base)` **or**
  `startsWith(base + ".")`. The prefix test is robust even when a media token itself contains `.`
  (e.g. `vnd.foo+json` → `vnd.foo_json`), because the base is a fixed hash prefix.

## What was implemented

1. **Shared derivation in `ObjectMetadataResponseFilter`** so the emit path and the precondition path
   can't drift:
   - `etagValue(...)` now delegates its base to a new public `baseValidator(metadata, CacheTarget)`.
   - New public `ifMatchSatisfied(headerValue, base)` implements the precondition match (`*`, lists,
     `W/`, quotes, `equals` / `startsWith(base + ".")`). `null` header or `null` base → satisfied.

2. **`checkIfMatch` reworked in both resources** to `checkIfMatch(metadata, CacheTarget)`:
   delegates to `baseValidator` + `ifMatchSatisfied`. `null` `If-Match` or `null` base → proceed (412
   only on a real mismatch).

3. **Per-endpoint validator target:**

   | Endpoint | Writes content? | `If-Match` validates against |
   |---|---|---|
   | update content (PUT/POST `.../content`) | yes | `CONTENT` |
   | create — overwrite/override branch | yes | `CONTENT` |
   | transition | no | `METADATA` |
   | delete | no | `CONTENT` (kept as-is — existing behaviour) |
   | create — true create | n/a (nothing exists) | skipped |

   The transition and create-overwrite call sites are **new** (they didn't enforce `If-Match` before);
   update-content and delete were already wired and now pass the explicit `CONTENT` target.

4. **Transition success now carries the new ETag.** The transition responses (both the normal success
   and the idempotent "already in target stage" retry) now `attach(..., METADATA)`, so a successful
   transition emits an ETag — satisfying the ticket's "success → response carries the new ETag".

## Acceptance criteria

| Criterion | Status |
|---|---|
| `If-Match` parsed on writable endpoints (both resources) | ✅ update, delete, transition, create-overwrite |
| Matching `If-Match` → proceeds; response carries new ETag | ✅ `testTransition*_IfMatchSuccess`; update success now carries ETag |
| Mismatching `If-Match` → 412, no side effects | ✅ `testTransition*_IfMatchFail_Returns412` asserts 412 **and** object still in source stage |
| Missing `If-Match` → behaves as today | ✅ pre-existing `…_NoIfMatch_StillWorks` tests |

## Tests

- **Fixed** `testUpdatePackageContent_IfMatchSuccess` / `testUpdateObjectContent_IfMatchSuccess`: they
  sourced the `If-Match` value from a **metadata** GET, which only matched a content write by coincidence
  pre-P1-5 (when content and metadata shared the raw-`contentHash` ETag). They now source it from a
  **content** GET — the correct resource view for a content write.
- **Added** to both `SchemaPackagesResourceTest` and `ObjectRegistryResourceTest`:
  `testTransition*_IfMatchSuccess` (metadata-ETag `If-Match` → 200 + new ETag) and
  `testTransition*_IfMatchFail_Returns412` (stale `If-Match` → 412, object unmoved).

## Notes / deviations

- **Delete stays on `CONTENT`** per explicit decision — no behaviour change there, and the existing
  delete 412 fail-tests (which send a junk value) pass under either validator.
- **Build status:** as of 2026-06-05 the full build and Phase-1 test suite (`rest.application` +
  `rest.tests`) run green.
