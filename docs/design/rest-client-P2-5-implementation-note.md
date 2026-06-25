# P2-5 — Implementation note (in-memory cache: LRU + TTL)

**Ticket:** P2-5 "In-memory cache with LRU + TTL" (Phase 2).
**Depends on:** P2-2. **Date:** 2026-06-05.

## Scope

A bounded, TTL-aware in-memory cache internal to `rest.client.impl`, fronting the EPackage fetch path.
EObject caching (the `(scope, registry, objectId)` key) is Phase 5; the cache is generic so it carries
over unchanged.

## `ClientCache<K, V>`

Generic, thread-safe (all ops `synchronized`), backed by an access-ordered `LinkedHashMap`.

- **Entries** store the value plus the last-seen `ETag` and `Last-Modified` and an `expiresAtMillis`.
  These validators are kept here for the P2-6 conditional-GET revalidation and are **never** exposed
  through `rest.client.api` (`ClientCache` and `Entry` are package-private to `rest.client.impl`).
- **LRU** via `removeEldestEntry` beyond `cache.max.entries`; `<= 0` = unbounded.
- **TTL** via `cache.ttl.ms`; `0` = no expiry. The clock is an injectable `LongSupplier`
  (`System::currentTimeMillis` by default) so TTL is tested deterministically without sleeping.
- **API:** `get` (live value only — empty if absent or past TTL), `lookup` (raw entry regardless of
  expiry, for revalidation), `put`, `invalidate`, `keys` (snapshot for the drift watcher), `size`,
  `clear`.
- **Expired entries are retained, not evicted on `get`** — so P2-6 can revalidate with the stored ETag,
  and the watcher still sees the key via `keys()`. They age out via LRU or are replaced on re-fetch.

## Wiring into `RemoteEPackageProviderImpl`

- The provider builds a `ClientCache<String, EPackage>` (keyed by nsURI) from
  `cache.max.entries` / `cache.ttl.ms`. A second constructor injects a cache (clock control / tests).
- `getEPackage(nsUri)`: cache hit → return; miss → `fetchAndCache` (walk scopes, first hit wins,
  `cache.put(nsUri, pkg, etag, lastModified)`).
- `ensureAvailable` → `getEPackage` (local-first).
- `refresh(nsUri)`: `invalidate` then `fetchAndCache` — forced re-fetch replacing the entry.
- `fetchContent` now returns a `FetchedPackage(ePackage, etag, lastModified)`, reading
  `ETag` / `Last-Modified` response headers (via `HttpHeaders` constants).
- **Misses are not cached** (only `200` hits), so an absent package is re-tried on the next call.
- `listNsUris` is **not** cached — it is a listing, and the design caches EPackages by nsURI only.

Disk caching (`cache.disk.dir`) is intentionally deferred — the design marks it optional and the ticket
asks only for the in-memory cache.

## Acceptance-criteria coverage

| Criterion | Test |
|---|---|
| Respects `cache.max.entries` (LRU eviction) | `ClientCacheTest.evictsLeastRecentlyUsedBeyondMaxEntries`, `zeroOrNegativeMaxEntriesIsUnbounded` |
| Respects `cache.ttl.ms` (`0` = no TTL) | `respectsTtl`, `zeroTtlNeverExpires` (injected clock) |
| get/put/invalidate + list-all-keys for the watcher | `getPutAndInvalidate`, `keysListsAllEntries`, `expiredEntryIsNotReturnedButKeyRemainsForWatcher` |
| `ETag`/`Last-Modified` stored alongside, not on the API | `storesValidatorsButNotViaApi` (via `lookup`); `ClientCache`/`Entry` are impl-private |
| Cache fronts the fetch path | `RemoteEPackageProviderImplTest.getEPackage_cachesResult_secondCallSkipsHttp`, `refresh_invalidatesAndRefetches`, `getEPackage_missesAreNotCached` |

## Build status

As of 2026-06-05 `:rest.client.api:build` and `:rest.client.impl:build` are green (39 unit tests).
Remaining in Phase 2: P2-6 (client conditional GET — uses the stored ETag via `lookup`), P2-7
(Atlas-aware ResourceSet / delegating registry), P2-8…P2-11.
