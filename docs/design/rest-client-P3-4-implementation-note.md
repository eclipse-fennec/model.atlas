# P3-4 — Implementation note (EAGER mode, startup pre-fetch)

**Ticket:** P3-4 "EAGER mode (startup pre-fetch)" (Phase 3).
**Depends on:** P3-3 (publish mechanism), P2-3/P2-4 (REST mapping + XMI deserialization). **Date:** 2026-06-09.

## Scope

Give the P3-3 publish mechanism its first trigger: when `mode=EAGER`, pre-fetch the configured scopes
at component activation and publish each EPackage immediately, so the local framework
`EPackage.Registry` mirrors the Atlas the moment the client is up. LAZY (P3-5) and HYBRID (P3-6) reuse the
same publisher with different triggers.

## `EagerPrefetch`

New package-private class in the osgi bundle. Plain Java — it depends only on the Phase-2 client API and a
publish seam, so it is unit-testable without an OSGi framework or a live server.

- **Publish seam.** A nested `@FunctionalInterface Publication { boolean publish(EPackage, scope, stage, version); }`
  whose signature matches `RemoteEPackagePublisher.publish`. The component passes `publisher::publish`; tests
  pass a recorder. Keeps `EagerPrefetch` free of any `BundleContext` dependency.
- **`run()`** returns the number of packages newly published.
- **Scope resolution** (first non-empty wins):
  1. `eager.scopes`;
  2. else `scope.allow.list` (the ticket's "`eager.scopes=[]` + EAGER = all configured" reading);
  3. else every scope the server advertises (`client.listScopeNames()`).
- **Per scope:** `provider.listNsUris(scope)` → for each nsURI `provider.ensureAvailable(nsUri)` →
  `publication.publish(pkg, scope, view, null)`. `scope` is the one we listed from (the origin marker);
  `stage` is the configured `view`; `version` is `null` so the publisher stamps its `1.0` default.

## Reachability / strictness (the four acceptance criteria)

- All EPackages of the configured scopes are published after activation. ✔
- `eager.scopes=[]` + EAGER pre-fetches all configured scopes (allow-list, then all advertised). ✔
- **`mode.strict=true` + unreachable server → activation fails.** A `TransportException` thrown while
  listing or fetching is logged at `SEVERE` and **rethrown** from `run()`; it propagates out of `@Activate`
  so the SCR component fails to activate with a clear log line. ✔
- **`mode.strict=false` (default) + unreachable server → activation succeeds.** The `TransportException` is
  logged at `WARNING` and the pre-fetch stops gracefully, returning the count published so far. Because a
  config update tears the component down and re-activates it (no `@Modified`), the pre-fetch is naturally
  retried then. ✔

A `NotFoundException` (server reachable, but that scope / nsURI simply is not there) is logged and skipped
*per item* regardless of strictness — it never aborts the run. A `TransportException` aborts the remaining
scopes (the whole server is presumed down).

## Wiring (`AtlasClientComponent`)

`@Activate` builds the client + publisher (unchanged), then, when `configuration.getMode() == EAGER`, runs
`new EagerPrefetch(client, publisher::publish, configuration).run()`. The call is **synchronous** on the SCR
activation thread — required so `mode.strict=true` can fail activation, and the simplest reading of "on
activation". The whole block is wrapped so that if a strict failure throws, the component first
`publisher.unpublishAll()` + `client.close()` (releasing the Jakarta RS client and its drift scheduler)
before rethrowing — `@Deactivate` is **not** called for a component that never finished activating, so
without this the client would leak.

LAZY (`mode=LAZY`, the default) and HYBRID are no-ops here; they are wired by P3-5 / P3-6.

## Deviation — `eager.stages` is not an independent knob

The ticket/design speak of pre-fetching `eager.scopes × eager.stages`. The Phase-2 read client was
deliberately collapsed to a **single configured `view` stage** (see the P2-11 note): `listNsUris(scope)`
hits the final-stage inheriting alias `GET /{scope}/schema` and ignores any stage, and content is fetched
from `view`. There is therefore no client API to list/fetch an arbitrary stage, so `eager.stages` cannot be
honored as a separate multi-valued dimension. **Decision (user, 2026-06-09): pre-fetch each scope's
configured `view` and stamp `atlas.stage = view`; treat `eager.stages` as not reachable via the read-only
client.** Honoring it literally would require extending the Phase-2 client/provider with per-stage
listing + content fetch — out of scope for P3-4. (`eager.stages` remains on `ClientConfiguration` for the
shared config surface; it is simply not consulted by the EAGER trigger.)

## Tests

`EagerPrefetchTest` (plain JUnit Jupiter + Mockito, no OSGi): mocks `ModelAtlasClient` /
`RemoteEPackageProvider` and records publish calls. Covers: publishes each package of `eager.scopes`
(scope/stage/version stamping; `listScopeNames` not consulted); fallback to `scope.allow.list`; fallback to
all advertised scopes; unavailable content (`Optional.empty`) skipped; `NotFoundException` scope skipped
while other scopes still process; strict mode rethrows on `TransportException`; non-strict mode swallows it
and returns the partial count. Added `-testpath` (`junit-jupiter-api`, the api bundle) to the osgi
`bnd.bnd` (first tests in this bundle); Mockito comes from the root `build.gradle` `testImplementation`.

## Buildpath / build

No buildpath change — `EPackage`/`EcoreFactory` resolve via `-library: enableEMF`; the client API is already
on the buildpath. osgi bundle builds green and the unit tests pass. End-to-end activation behaviour
(configurator picked up → `EPackage.Registry` populated) is asserted by the P3-12 OSGi ITs.

## Status

P3-4 complete and build-green. Next: P3-5 (LAZY on-demand resolution with synchronous wait + timeout).
