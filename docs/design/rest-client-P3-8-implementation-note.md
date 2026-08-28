# P3-8 — Implementation note (`force.remote=true`: ranking + startup version check)

**Ticket:** P3-8 "`force.remote=true` (ranking + startup version check)" (Phase 3).
**Depends on:** P3-7. **Date:** 2026-06-09.

## Scope

Two behaviours when `force.remote=true`:

1. remote services are published with a high `service.ranking`, even when a local service for the same nsURI
   exists;
2. on startup, every locally registered EPackage is checked against the Atlas and superseded by the remote
   when the Atlas copy is newer.

P3-7 already made the gate publish-regardless under `force.remote` (no suppression); P3-8 adds the ranking
and the proactive startup preemption.

## Part 1 — high `service.ranking`

`RemoteEPackagePublisher` gained a `serviceRanking` constructor argument. When non-zero it stamps
`Constants.SERVICE_RANKING` on the shared property set, so all three services (configurator, EPackage,
EFactory) carry it. The component passes `force.remote ? 1000 : 0` (`FORCE_REMOTE_SERVICE_RANKING`, above the
local default of 0). Because **every** publish path runs through this one publisher, ranking applies to
EAGER, HYBRID, LAZY and the startup check uniformly. `force.remote=false` → ranking 0 → property omitted →
behaviour unchanged.

## Part 2 — startup version check (`ForceRemoteStartupCheck`)

OSGi-free, unit-tested. Driven by a supplier of local models, a resolver and a `PackagePublication` (the
gate). `run()`:

- enumerates local models via `LocalServiceWatcher.localModels(ctx)` — distinct non-`atlas.remote`
  `EPackage`/`EPackageConfigurator` nsURIs with their `emf.version`, de-duped by nsURI;
- `resolve()`s each against the Atlas (the P3-5 metadata-first lookup — authoritative version);
- if `isRemoteNewer(localVersion, remoteVersion)`, publishes the remote through the gate (which, under
  `force.remote`, publishes with the high ranking), superseding the local.
- Best-effort: a `TransportException`/server error resolving one nsURI is logged and skipped (the local
  stays); it never fails activation.

It runs in **every mode** when `force.remote=true` (after the mode's own prefetch), so it covers local
packages that EAGER/HYBRID didn't already preempt.

**Version preference rule** (`isRemoteNewer`): no remote version → keep local; no local version → prefer
remote; equal → keep local; both parse as OSGi `Version` → remote wins iff strictly greater; unparseable but
different → prefer the (differing) remote. (Content-hash-based detection — the design's parenthetical — is a
possible refinement; `ResolvedEPackage` does not carry the hash today, so the rule is version-based.)

## Honest caveat (AC #4)

Documented in code (`RemoteEPackagePublisher` and `ForceRemoteStartupCheck` javadoc) and already in the
design doc (§`force.remote=true` + Open Questions #5): `emf.osgi`'s `DefaultEPackageRegistryComponent`
populates `EPackage.Registry` in **bind order**, not by `service.ranking`. So a forced remote reliably wins
for consumers doing a **direct service lookup**, but registry-level (`EPackage.Registry.getEPackage`)
precedence stays bind-order-dependent until the ranking-aware aggregator (tracked as a follow-up against
`emf.osgi`) lands. The safe setup remains "no local bundle for the same nsURI + `force.remote=true`".

## Tests

- `ForceRemoteStartupCheckTest`: the version rule (all branches); supersedes locals the Atlas is newer than
  and leaves equal/older ones; ignores locals the Atlas doesn't have; a transport failure on one nsURI is
  skipped while a reachable newer one is still superseded.
- `RemoteEPackagePublisherTest`: `service.ranking` is stamped (1000) when constructed forced, and absent by
  default — captured from the `Dictionary` passed to `registerService` (mocked `BundleContext`).

Build green (osgi). The framework enumeration (`localModels`) and end-to-end preemption are exercised by the
P3-12 OSGi ITs.

## Status

P3-8 complete and build-green. Next: P3-9 (atomic per-nsURI substitution on drift refresh).
