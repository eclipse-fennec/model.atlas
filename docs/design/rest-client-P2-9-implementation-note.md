# P2-9 — Implementation note (nsURI allow/deny lists)

**Ticket:** P2-9 "Allow/deny lists for nsURIs" (Phase 2).
**Depends on:** P2-3. **Date:** 2026-06-05.

## Scope

Gate EPackage resolution by the configured `nsuri.allow.list` / `nsuri.deny.list` (exact matches, no
patterns): a denied nsURI is never returned (even from cache); a non-empty allow-list restricts to its
entries; both empty = everything allowed.

## Implementation

A single `isPublishable(nsUri)` gate in `RemoteEPackageProviderImpl`:

```
deny.contains(nsUri)        -> false   (deny wins, even over the allow-list)
allow.isEmpty()             -> true
else                        -> allow.contains(nsUri)
```

Applied at the **top** of `getEPackage(nsUri)` and `refresh(nsUri)`, **before** the cache is consulted —
so a denied nsURI returns `Optional.empty()` regardless of cache state and never triggers a fetch.
`ensureAvailable` delegates to `getEPackage`, and the drift watcher re-fetches via `refresh`, so the gate
covers the direct look-up and the watcher uniformly.

`listNsUris` is left unfiltered — it is server discovery, not a publish/resolve action; the ticket scopes
the gate to `getEPackage` / `ensureAvailable`. The config fields (`getNsUriAllowList()` /
`getNsUriDenyList()`, default empty) already existed from P2-1.

## Acceptance-criteria coverage (`RemoteEPackageProviderImplTest`)

| Criterion | Test |
|---|---|
| Denied nsURI → `Optional.empty()` regardless of cache state | `getEPackage_deniedNsUri_returnsEmpty_evenIfCached` (pre-populated injected cache; no fetch) |
| Allow-list filtering applied uniformly to direct look-up and the watcher | `getEPackage_notInAllowList_returnsEmpty`, `getEPackage_inAllowList_isFetched`, `refresh_deniedNsUri_returnsEmpty_appliesToWatcherPath` (refresh is the watcher's path) |
| Filtering testable in isolation | all four are pure unit tests with mocked transport (denied/not-allowed cases verify `request.get()` is never called) |

## Build status

As of 2026-06-05 `:rest.client.api:build` and `:rest.client.impl:build` are green (57 unit tests).
Remaining in Phase 2: P2-10 (bearer/mTLS auth providers), P2-11 (live integration tests).
