# P3-5 — Implementation note (LAZY mode: blocking on-demand resolution + timeout)

**Ticket:** P3-5 "LAZY mode with synchronously blocking trigger + timeout" (Phase 3).
**Depends on:** P3-3 (publish mechanism). **Date:** 2026-06-09.

## Scope

In LAZY mode an `EPackage.Registry.getEPackage(unknownNsUri)` triggers a fetch from the Atlas, publishes the
EPackage as OSGi services, and **blocks** until the package is observable through the framework
`EPackage.Registry` (because `emf.osgi` binds the new `EPackageConfigurator` asynchronously), with a
configurable timeout (`lazy.resolve.timeout.ms`, default 5 s). Concurrent calls for the same nsURI
deduplicate to a single fetch+publish.

## Prerequisite fix — `emf.model.scope` (P3-3 correction)

Reading the `emf.osgi.component` sources (`~/.m2/.../org.eclipse.fennec.emf.osgi.component/<ver>-sources.jar`)
to design the wait revealed that `DefaultEPackageRegistryComponent` — which owns the global framework
`EPackage.Registry` (service property `default.resourceset.epackage.registry=true`, consumed by the default
`ResourceSetFactory`) — binds its `EPackageConfigurator` reference with a **hardcoded target**
`(emf.model.scope=resourceset)`. P3-3 had stamped `emf.model.scope` = the *Atlas* scope, so our configurator
would **never** be bound there and the wait would never succeed. Fixed `RemoteEPackageConfigurator` to stamp
`emf.model.scope = EMFNamespaces.EMF_MODEL_SCOPE_RESOURCE_SET` (`"resourceset"`); the real Atlas scope still
travels as `atlas.scope`. (User decision 2026-06-09; P3-3 note + table updated. The server's
`DynamicEPackageConfigurator` keeps the Atlas scope because it pairs each scope with a dedicated scoped
registry via `ConfigurationEPackageRegistryComponent`, whose `ePackageConfigurator.target` is overridable —
the client has no such scoped registries and wants the global one.)

## `LazyResolvingPackageRegistry`

New package-private `EPackage.Registry` (extends `ConcurrentHashMap<String,Object>` for the `Map` contract,
exactly like the Phase-2 `AtlasDelegatingPackageRegistry`). `getEPackage(nsURI)`:

1. **primary** (the framework registry, injected) — a local / already-published package wins, no network;
2. this registry's own entries (anything `put` directly);
3. **`resolveRemote`** — `remote.ensureAvailable(nsURI)` → `publication.publish(pkg, scope, stage, null)` →
   **`waitUntilVisible`**.

- **`waitUntilVisible`** polls `primary.getEPackage(nsURI)` (a cheap map lookup) every
  `DEFAULT_POLL_INTERVAL_MS` (25 ms) until it appears or `timeoutMs` elapses. On timeout it returns `null`
  and logs a `WARNING`; the package is already published, so a later call returns it once `emf.osgi`'s bind
  completes.
- **Deadlock-free.** The poll runs on the *caller's* thread, never on the SCR component-binding thread, so
  the bind proceeds independently and the wait is bounded — satisfies the AC's "does not deadlock with
  `emf.osgi`'s component-binding thread".
- **De-duplication.** A `ConcurrentHashMap<String, CompletableFuture<EPackage>> inFlight` holds one
  resolution per nsURI. The first caller (`putIfAbsent` winner) owns the fetch+publish+wait and completes the
  future; concurrent callers `get(timeoutMs)` on the same future. So the server is hit once and `publish`
  runs once for a burst of identical lookups.
- **Never throws out of `getEPackage`.** A miss (`Optional.empty`) → `null` (no warning, legitimate). A
  `ModelAtlasClientException`/`TransportException` during fetch → logged + `null`. The owner completes the
  future *normally* (with the result or `null`), so awaiters never see an `ExecutionException`. An
  `EPackage.Registry` that threw would break EMF proxy resolution.
- **Testability.** Constructor seams for the poll interval, a `LongSupplier` clock and a `Sleeper`.

## Authoritative origin — metadata-first `resolve()` (Phase-2 reopened)

The first cut stamped a *best-effort* `atlas.scope` on lazily published packages (`default.scope` → first
`scope.allow.list` entry → `"*"`), because the Phase-2 `getEPackage` walk discards which scope actually
served a package. **User decision (2026-06-09): don't guess — read it.** Phase 2 was reopened to add an
authoritative origin lookup, and LAZY now uses it:

- **api:** `ResolvedEPackage` (`@ProviderType` final value type: `ePackage`, `nsUri`, `scope`, `registry`,
  `stage`, `version`) + `Optional<ResolvedEPackage> resolve(String nsUri)` on `RemoteEPackageProvider`.
- **impl (`RemoteEPackageProviderImpl.resolve`):** *metadata-first.* `GET
  /{entryScope}/schema/stages/{view}?nsUri=…` (`SchemaPackagesResource.listPackagesInStage` with `nsUri` →
  `getMetadataFromStageForRegistry`, which **respects scope inheritance**) returns one `ObjectMetadata`
  carrying the **true owning** scope/registry/stage/version — `scope=atlas` even when queried via `jena`.
  Content is then fetched from that exact scope/stage (the existing `fetchContent`, generalised to take a
  stage), reusing the cache + conditional-GET path. `getEPackage`/`ensureAvailable`/`refresh`/drift are
  untouched, so the plain-Java client and EAGER keep their behaviour.
- **Entry-scope gating (user decision):** the entry scope queried is gated by `scope.allow.list` /
  `default.scope`, first scope that can see the nsURI wins. The *owning* scope returned may be a parent not
  itself in the list — that is the P2-11 inheritance working as designed (querying `jena` can only ever
  surface `jena` + its ancestors, never a sibling), so we serve it and label `atlas.scope` with the true
  owner. We do **not** reject a package because its owner isn't explicitly listed (that would regress the
  parent-content inheritance).

`LazyResolvingPackageRegistry` now calls `remote.resolve(nsUri)` and publishes with the **exact**
`scope`/`stage`/`version` — the `lazyScope()` guess and the construction-time scope/stage are gone.

## Wiring (`AtlasClientComponent`)

`@Activate` gains `@Reference(target="(default.resourceset.epackage.registry=true)") EPackage.Registry
frameworkRegistry` (mandatory — the OSGi front-end's reason to exist is bridging to `emf.osgi`). It builds
the `LazyResolvingPackageRegistry` over that registry, `client.ePackages()`, `publisher::publish` and
`lazy.resolve.timeout.ms` (no scope/stage — the registry reads each package's origin per-fetch via
`resolve()`). Exposed via `lazyRegistry()` for P3-10 (which installs it as the package registry of
framework-produced `ResourceSet`s) and P3-6 (HYBRID). Built in every mode (harmless; serves on-demand misses
of packages not eagerly pre-fetched). The generated DS descriptor was checked: the registry reference is
bound as constructor parameter 2 with the target.

## What is *not* in P3-5

- **Plugging the registry into `ResourceSet`s** is P3-10 (`ResourceSetConfigurator` + `resource.set.fallback`).
- The design's alternative trigger — a `ServiceListener` on unsatisfied `(emf.nsURI=…)` service requirements —
  is not implemented; P3-5 is specifically the `getEPackage`-driven blocking trigger.

## Tests

- **impl `RemoteEPackageProviderImplTest`** (added): `resolve` reads metadata then content from the
  **owning** scope (asserts `scope=atlas` though queried via `jena`, and the URL order metadata→content);
  walks entry scopes until metadata is visible; not visible from any allowed scope → empty; denied nsURI →
  empty without any fetch. (Gotcha re-confirmed: build each mocked `Response` into a var before
  `thenReturn(...)` — constructing them inside the args nests Mockito stubbing → `UnfinishedStubbingException`.)
- **osgi `LazyResolvingPackageRegistryTest`** (plain JUnit + Mockito; real `EPackageRegistryImpl` framework
  registry; a publish seam that optionally mirrors the package in to simulate the `emf.osgi` bind):
  local-first (no `resolve`), resolve→publish→visible→returns and **asserts the authoritative
  scope/stage/version are stamped** (`atlas`/`released`/`1.2`), timeout→`null`, unknown→`null` (no publish),
  transport error→`null` (no throw), `getEFactory` via lazy fetch, **8-thread concurrent dedup** (`resolve` +
  `publish` each invoked once), null nsURI delegated to primary.

Build green across api + impl + osgi. The EAGER `Publication` seam was extracted to a shared top-level
`PackagePublication` interface used by both triggers.

## Status

P3-5 complete and build-green. Next: P3-6 (HYBRID = pre-fetch `eager.nsuri.allow.list`, rest LAZY) — small,
reuses `EagerPrefetch` + this registry.
