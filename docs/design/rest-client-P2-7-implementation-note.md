# P2-7 — Implementation note (scope-level drift watcher)

**Ticket:** P2-7 "Scope-level drift watcher" (Phase 2).
**Depends on:** P2-6, P1-7. **Date:** 2026-06-05.

## Scope

A scheduled task that, per configured scope, issues a conditional `HEAD` against the P1-7 scope
aggregate endpoint. On a stale baseline the response's `Atlas-Changed-NsUris` tells the client exactly
which cached entries to refresh; affected entries fire `DriftListener` events. Also wires
`ModelAtlasClient.checkForDrift()` (manual trigger) and `addDriftListener()`.

## Endpoint correction

The ticket/design text says `HEAD /{scope}`. The **actual** server endpoint (confirmed in
`ScopesResource`: class `@Path("/scopes")` + method `@Path("/{scopeName}")`) is **`HEAD /scopes/{scope}`**
— the `ScopeAggregateService` aggregate from P1-7. The watcher targets `/scopes/{scope}` accordingly.
That endpoint returns a strong aggregate `ETag`, `304` on a matching `If-None-Match`, and comma-joined
`Atlas-Changed-NsUris` / `Atlas-Changed-Objects` headers on a stale-but-known baseline.

## `DriftWatcher`

- Holds a per-scope aggregate-ETag map (separate from the per-package cache), a `CopyOnWriteArrayList`
  of listeners, and a daemon-threaded `ScheduledExecutorService` (created only when
  `drift.check.interval.ms > 0`; `0` disables the schedule).
- `RestSupport.head(target, ifNoneMatch)` added for the conditional HEAD.
- `check()` (synchronized; also the manual `checkForDrift` path) per scope:
  - `HEAD /scopes/{scope}` with the stored aggregate ETag as `If-None-Match`;
  - `304` or non-2xx → skip;
  - store the new ETag; if there was **no** previous ETag → first sight, establish baseline, emit
    nothing;
  - else split `Atlas-Changed-NsUris`; for each nsURI the client **currently holds**
    (`provider.cachedNsUris()`), `provider.refresh(nsUri)`:
    - present → `onPackageChanged(nsUri, pkg)`, add to report `changed`;
    - empty → `onPackageRemoved(nsUri)`, add to report `removed`;
  - nsURIs not held locally are ignored (a lazy read client doesn't care about packages it never
    fetched; newly-*added* packages also land here and are skipped).
- `refresh(nsUri)` reuses the P2-6 conditional path, so it updates the per-package cache and (for a
  removal) invalidates it. Listener exceptions are caught per-listener so one bad listener can't break
  the sweep or the schedule.
- Scheduled runs go through `safeCheck` (swallows/logs `RuntimeException` so a transient failure doesn't
  kill the schedule).

## Wiring in `ModelAtlasClientImpl`

- Builds a `DriftWatcher(baseTarget, this::scopesToWatch, this::ePackagesImpl, interval)` and `start()`s
  it in the constructor; `close()` shuts it down (then closes the JAX-RS client).
- `scopesToWatch()` = `scope.allow.list` if non-empty, else `listScopeNames()` (all scopes).
- The provider is resolved lazily by the watcher (`this::ePackagesImpl`), so construction with a
  not-yet-targetable client (unit tests) doesn't force provider creation; the scheduled check only runs
  after the interval.
- `checkForDrift()` → `driftWatcher.check()`; `addDriftListener()` → `driftWatcher.addListener()`
  (returns an `AutoCloseable` that unsubscribes).

EObject-level drift (`Atlas-Changed-Objects`, `onObjectChanged/Removed`) is Phase 5; only nsURIs are
handled now.

## Acceptance-criteria coverage (`DriftWatcherTest`, mocked HEAD transport + mocked provider)

| Criterion | Test |
|---|---|
| Per-scope conditional `HEAD` with cached scope ETag | `notModified_noEvents` (verifies `If-None-Match` carries the baseline ETag) |
| `304` → nothing happens | `notModified_noEvents` |
| `200` → invalidate exactly the changed-and-held entries, fire `onPackageChanged` | `change_refreshesCachedEntry_andFiresOnPackageChanged` (ns1 held → refreshed+fired; ns2 not held → ignored) |
| Removed package → `onPackageRemoved` | `removal_firesOnPackageRemoved` |
| First sight establishes a baseline silently | `firstCheckEstablishesBaseline_noEvents` |
| Listener unsubscribe | `removedListenerNoLongerNotified` |

The actual scheduled firing (timing) and the live end-to-end sweep are covered at P2-11; the schedule
itself is thin glue over `check()`.

## Build status

As of 2026-06-05 `:rest.client.api:build` and `:rest.client.impl:build` are green (47 unit tests).
Remaining in Phase 2: P2-8 (`AtlasDelegatingPackageRegistry` + `newResourceSet()`), P2-9 (nsURI
allow/deny lists), P2-10 (auth providers), P2-11 (live integration tests).
