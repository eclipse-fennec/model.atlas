# P3-7 — Implementation note (local-first suppression with ServiceListener)

**Ticket:** P3-7 "Local-first behaviour with `ServiceListener`" (Phase 3).
**Depends on:** P3-3 (publish mechanism). **Date:** 2026-06-09.

## Scope

Before publishing a remote EPackage, check whether a **local** `EPackage` *or* `EPackageConfigurator`
service already provides that nsURI; if so, suppress the remote publication. Track suppressed candidates and,
via a `ServiceListener`, (re)publish one when its local counterpart disappears — without flapping when a
local is briefly unregistered and re-registered. Gated by `force.remote` (P3-8 builds on this).

This replaces the *partial* local precedence that existed before: the LAZY registry's step-1
`primary.getEPackage` only gave local priority on a lazy **lookup**, and EAGER/HYBRID published
unconditionally. Now **all** publish paths funnel through one gate.

## `LocalFirstPublicationGate` (OSGi-free, unit-tested)

A `PackagePublication` that wraps the real `RemoteEPackagePublisher` and decides per nsURI. Seams (so it
tests without a framework): the real publish, an unpublish `Consumer<String>`, a `Predicate<String>
localPresent`, the `force.remote` flag, and a `Scheduler` (debounce).

- **`publish(...)`** — `force.remote` → publish regardless. Else if `localPresent` → **park** the candidate
  (record the package + origin) and return `false` (suppressed). Else publish and remember it in
  `publishedByUs`.
- **`onLocalAppeared(nsUri)`** — cancel any pending republish (this is half the anti-flap); if `force.remote`
  return; if we had published this nsURI, **withdraw** it (`unpublisher`) and re-park it.
- **`onLocalDisappeared(nsUri)`** — if a candidate is parked, schedule a **debounced** republish (cancelling
  any prior pending one). When it fires, `republishIfStillAbsent` re-checks `localPresent` and only publishes
  if the local is still gone.
- **No flapping** = debounce + cancel-on-reappear + re-check-at-fire. A brief unregister/re-register cancels
  the scheduled task (`onLocalAppeared`), so the remote never momentarily registers; and even a bare
  re-check at fire time keeps it suppressed if the local returned without an event.

A single lock guards the maps (`parked`, `publishedByUs`, `pendingRepublish`). The gate's own
publish/unpublish can't re-enter it because the watcher filters out `atlas.remote` services (below).

## `LocalServiceWatcher` (the OSGi glue)

A `ServiceListener` registered with filter
`(&(|(objectClass=…EPackage)(objectClass=…EPackageConfigurator))(!(atlas.remote=true)))` — local model
services only, never our own remote publications (so no self-triggering, no re-entrancy). It reads
`emf.nsURI` off the reference and maps events to the gate: `REGISTERED`/`MODIFIED` → `onLocalAppeared`,
`UNREGISTERING`/`MODIFIED_ENDMATCH` → `onLocalDisappeared`. `close()` removes the listener.

`hasLocalService(ctx, nsUri)` (the gate's `localPresent`) queries the registry **by interface** and matches
the `emf.nsURI` / `atlas.remote` properties in code — so a user-supplied nsURI never enters an LDAP filter
(no escaping concerns). The filter constant carries no user input.

## Wiring (`AtlasClientComponent`)

The component builds a daemon single-thread `ScheduledExecutorService` (`atlas-client-local-first`), wraps it
as the gate's `Scheduler` (500 ms debounce), constructs the gate over `publisher::publish` /
`publisher::unpublish` with `localPresent = hasLocalService(ctx, …)` and `force.remote` from config, and
registers the `LocalServiceWatcher`. **EAGER, HYBRID and the LAZY registry are now handed `gate` as their
`PackagePublication`** instead of `publisher::publish`, so every publish is gated. `@Deactivate` (and the
strict-activation-failure cleanup) closes the watcher and shuts the executor down alongside the existing
client/publisher teardown.

## Tests

`LocalFirstPublicationGateTest` (plain JUnit; a mutable `Set` as the local-presence oracle; a hand-driven
`Scheduler` so the debounce is deterministic): publishes when no local; suppresses (parks) when a local
exists; withdraws + re-parks when a local appears for a package we published; republishes after the debounce
when the local disappears; **does not flap** when the local briefly disappears and returns (cancelled task
never runs); stays suppressed if the local is back by fire time; `force.remote` publishes regardless and
ignores local events. Build green (osgi). The `LocalServiceWatcher` framework glue is exercised by the P3-12
OSGi ITs.

## Status

P3-7 complete and build-green. Next: P3-8 (`force.remote` ranking + startup version check) — the gate
already honours `force.remote` (always publish, ignore locals); P3-8 adds the high `service.ranking` and the
startup local-vs-remote version comparison.
