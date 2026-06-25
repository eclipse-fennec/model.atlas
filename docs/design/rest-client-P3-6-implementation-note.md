# P3-6 — Implementation note (HYBRID mode)

**Ticket:** P3-6 "HYBRID mode (`eager.nsuri.allow.list`)" (Phase 3).
**Depends on:** P3-4 (EAGER pre-fetch), P3-5 (LAZY registry). **Date:** 2026-06-09.

## Scope

HYBRID = EAGER + LAZY: pre-fetch every nsURI listed in `eager.nsuri.allow.list` at activation, and treat
everything else as LAZY (resolved on demand through the `LazyResolvingPackageRegistry`). Small ticket — it
reuses both prior mechanisms.

## `EagerPrefetch.prefetchListedNsUris()`

A second entry point on the existing `EagerPrefetch` (alongside `run()` for EAGER):

- Iterates `config.getEagerNsUriAllowList()` and, for each nsURI, calls
  `RemoteEPackageProvider.resolve(nsUri)` — the P3-5 metadata-first lookup — so each package is published
  with its **authoritative** owning scope/stage/version, exactly like the LAZY path. (EAGER's `run()` stamps
  the prefetch scope + `view` because it deliberately mirrors a chosen scope; HYBRID lists individual
  nsURIs, so reading each one's true origin is the right thing and costs nothing extra.)
- An nsURI not visible from any allowed scope — or filtered out by the nsURI allow/deny gate, which
  `resolve()` applies internally — yields `Optional.empty()` and is logged at `WARNING` and skipped.
- Reachability is handled by the same `failOrSkip` helper as EAGER: a `TransportException` is rethrown when
  `mode.strict=true` (activation fails) and logged + swallowed (returning the partial count) when
  `mode.strict=false`.
- Discovery endpoints (`listNsUris` / `listScopeNames`) are never consulted — HYBRID publishes only what is
  explicitly listed.

The "everything else is LAZY" half needs no new code: the component already builds the
`LazyResolvingPackageRegistry` in every mode (P3-5), so unlisted nsURIs resolve on demand once that registry
is installed into ResourceSets (P3-10).

## Wiring (`AtlasClientComponent`)

`@Activate` now switches on the mode (inside the existing strict-failure cleanup wrapper):

```java
switch (configuration.getMode()) {
    case EAGER  -> prefetch.run();
    case HYBRID -> prefetch.prefetchListedNsUris();
    case LAZY   -> { /* nothing up front */ }
}
```

The strict-failure path (a `mode.strict=true` `TransportException` propagating out) still tears down the
client + publisher before rethrowing, so HYBRID gets the same no-leak activation-failure behaviour as EAGER.

## Tests

`EagerPrefetchTest` gained a HYBRID section (mocked `resolve()`): publishes only the listed nsURIs, each
stamped with the **exact** origin `resolve()` reported (e.g. `urn:a`→`atlas`/`released`/`1.0`,
`urn:b`→`jena`/`release`/`2.1`) and never touching discovery; skips an nsURI not visible from any allowed
scope; strict mode rethrows on an unreachable server; non-strict mode swallows it and returns the partial
count. Build green (osgi).

## Status

P3-6 complete and build-green. Next: P3-7 (local-first suppression via `ServiceListener`).
