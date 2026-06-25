# P2-6 — Implementation note (client-side conditional GET / If-None-Match)

**Ticket:** P2-6 "Client-side conditional GET (`If-None-Match` / ETag storage)" (Phase 2).
**Depends on:** P2-3, P2-5, P1-2. **Date:** 2026-06-05.

## Scope

When re-contacting the server for a cached EPackage — a forced `refresh` or a post-TTL revalidation
from `getEPackage` — send `If-None-Match` with the stored ETag. `304` keeps the cached value and
refreshes its TTL (no parsing); `200` replaces the entry with the new payload and ETag. Builds on the
P1-2 server's strong ETag.

## Conditional request plumbing

- `RestSupport.get(target, accept, ifNoneMatch)` — overload that adds the `If-None-Match` header
  (via `HttpHeaders.IF_NONE_MATCH`) when non-null; the 2-arg form delegates with `null`.
- `RestSupport.isNotModified(response)` — `304` check (`304` is a 3xx, so it is deliberately handled
  before the `isSuccess` 2xx check).

## `RemoteEPackageProviderImpl` — unified revalidate/fetch

The P2-5 `fetchAndCache` is replaced by `revalidateOrFetch(nsUri)`, used by both entry points:

- `getEPackage(nsUri)`: cache **live** hit (within TTL) → return without any server call; otherwise
  `revalidateOrFetch`.
- `refresh(nsUri)`: always `revalidateOrFetch` — it no longer pre-invalidates, so the stored ETag is
  available to send.

`revalidateOrFetch`:
1. `cache.lookup(nsUri)` (raw entry, **even if expired**) → its ETag becomes the `If-None-Match` value
   (null when nothing is cached → a plain fetch).
2. Walk the resolved scopes; per scope `fetchContent(scope, nsUri, ifNoneMatch)` returns:
   - **`304`** → keep the cached value, `cache.put(...)` it again to **refresh the TTL**, return it.
     The body is never read or parsed.
   - **`200`** → deserialize, `cache.put(...)` the new payload + ETag, return it.
   - **miss** (`204`/`404`/other) → next scope.
3. All scopes miss → `cache.invalidate(nsUri)` (the package is gone) → empty.

`fetchContent` now returns a `ContentResult` (`notModified` flag or a `FetchedPackage`); the `304` branch
short-circuits before reading the entity, so `EPackageDeserializer` is not invoked.

Same-scope consistency: first-hit-wins ordering is unchanged between fetch and revalidation, so the
scope that originally served a package is hit first again — it is the one that can answer `304`.

## Acceptance-criteria coverage

| Criterion | Test (`RemoteEPackageProviderImplTest`) |
|---|---|
| Refresh of a cached EPackage sends `If-None-Match` | `refresh_sendsIfNoneMatch_and304KeepsCachedValueWithoutParsing` (verifies `header("If-None-Match","\"v1\"")`) |
| `304` → entry retained, TTL refreshed, no parsing | same test (same instance returned, `deserialize` called once) + `getEPackage_postTtlRevalidation_304_keepsValueAndBumpsTtl` (bumped TTL → later read served from cache; only initial parse) |
| `200` → entry replaced, new ETag stored | `refresh_200_replacesEntryAndReparses` (re-parses, different instance) |
| Works against the Phase-1 server | strong ETag emitted by P1-2 (server side, green); end-to-end live check is P2-11 |

Tests use a `CountingDeserializer` to assert "no parse on 304", and an injected-clock `ClientCache` to
drive post-TTL revalidation deterministically.

## Build status

As of 2026-06-05 `:rest.client.api:build` and `:rest.client.impl:build` are green (42 unit tests).
Remaining in Phase 2: P2-7 (scope-level drift watcher — `HEAD /{scope}` + `DriftListener` events),
P2-8…P2-11.
