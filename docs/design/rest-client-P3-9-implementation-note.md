# P3-9 — Implementation note (atomic substitution on drift refresh, per-nsURI lock)

**Ticket:** P3-9 "Atomic substitution on drift refresh (per-nsURI lock)" (Phase 3).
**Depends on:** P3-3 (publish mechanism), P2-7 (drift watcher). **Date:** 2026-06-09.

## Scope

Two things: (a) wire the Phase-2 drift mechanism to **re-publish** an affected nsURI in OSGi (which did not
exist yet), and (b) make that swap **atomic per nsURI** — a concurrent `getEPackage(nsURI)` sees the old or
the new package, never `null` or a half-state — with nsURI-scoped locks so unrelated substitutions run in
parallel.

## Where "atomic, never-null" is guaranteed (and where it can't be)

A frank constraint, consistent with P3-8's caveat: the **framework** `EPackage.Registry` cannot be made
swap-atomic by us. Two `EPackageConfigurator`s for one nsURI use put-on-bind / remove-on-unbind semantics,
and `emf.osgi`'s aggregator processes bind/unbind in its own order, so the framework registry's mid/post-swap
state is bind-order-dependent. The never-null/never-mixed guarantee is therefore provided at **our delegating
registry** (`LazyResolvingPackageRegistry`) — what ResourceSet consumers actually go through (P3-10) — by
serving the currently-published package from the publisher's tracking map.

## `NsUriLocks`

A registry of per-nsURI `ReentrantLock`s (`run(nsUri, Runnable)` / `lockFor(nsUri)`), created on demand and
kept. Distinct nsURIs never block each other; the same nsURI serialises. Held by the publisher.

## Publisher — atomic `republish` + `publishedEPackage`

`RemoteEPackagePublisher` now serialises `publish`/`republish`/`unpublish` under the per-nsURI lock, and the
`Registration` carries its `EPackage`.

- **`republish(ePackage, scope, stage, version)`** — under `lock(nsUri)`: register the new trio, **swap the
  tracking-map entry** (`published.put(nsUri, fresh)`), then revoke the old trio. The map swap is the atomic
  point: `publishedEPackage(nsUri)` returns the old package up to the `put` and the new one after — never
  `null` mid-swap. If nothing was published it behaves like `publish`.
- **`publishedEPackage(nsUri)`** — the package currently published for nsURI (atomic `ConcurrentHashMap`
  read), or `null`.

## Delegating registry — the never-null bridge

`LazyResolvingPackageRegistry.getEPackage` gains a step between "local/primary" and the cold resolve: it
consults `publishedLookup` (= `publisher::publishedEPackage`). So while `emf.osgi` is still (un)binding during
a swap — or while a fresh publication has not yet been bound into the framework registry — our registry
returns the old or new package atomically, never `null`, never a frankenpackage. (Local precedence is
unchanged: `primary` is still checked first.)

## `DriftSubstitution` (the drift listener) + wiring

A `DriftListener` subscribed via `client.addDriftListener`:

- `onPackageChanged(nsUri, …)` — only if we currently publish nsURI (`publisher.isPublished`): **re-resolve**
  its authoritative origin (P3-5 `resolve()`, so the swapped service carries the possibly-bumped version) and
  `publisher.republish(...)`; if the re-resolve comes back empty (changed-then-gone), `unpublish`. A transport
  failure is logged and the current publication is left in place.
- `onPackageRemoved(nsUri)` — `unpublish`.

The component registers it before the prefetch (events for not-yet-published nsURIs are ignored by the
`isPublished` gate) and closes the subscription first in `tearDown()` (shared by `@Deactivate` and the
strict-activation-failure path), before the watcher, the debounce executor, `unpublishAll` and `client.close`.

**Local-first interaction (documented corner):** the substitution only swaps nsURIs we *currently publish*.
A remote suppressed by a local (P3-7, parked in the gate) has no service to swap, so it is skipped; if its
local later disappears the gate re-publishes the parked candidate, which may be one drift generation stale
until the next drift/refresh. Acceptable: while suppressed, nobody is using our remote.

## Tests

- `NsUriLocksTest`: same nsURI → same lock, distinct → distinct; `run` holds then releases; distinct nsURIs
  don't block each other (two-thread test).
- `RemoteEPackagePublisherTest` (extended): `publishedEPackage` tracks publish→present, unpublish→null;
  `republish` swaps to the new package and **revokes the old configurator registration, not the new** (fresh
  registration mock per call); republish with nothing published acts as a fresh publish.
- `DriftSubstitutionTest`: change of a published package re-resolves + republishes; change of a non-published
  one is ignored; changed-then-gone unpublishes; a transport failure leaves the publication in place; removal
  unpublishes.

Build green (osgi). Stress/parallel and the framework-registry behaviour during a swap are covered by the
P3-12 OSGi ITs.

## Status

P3-9 complete and build-green. Next: P3-10 (`ResourceSetConfigurator` integration — install the delegating
registry into framework-produced ResourceSets; `resource.set.fallback`).
