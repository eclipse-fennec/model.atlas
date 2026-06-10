# P3-10 — Implementation note (`ResourceSetConfigurator` integration)

**Ticket:** P3-10 "`ResourceSetConfigurator` integration" (Phase 3).
**Depends on:** P3-3 (publish), P2-8 (delegating registry — realised here as the P3-5 OSGi
`LazyResolvingPackageRegistry`). **Date:** 2026-06-09.

## Scope

Register a `ResourceSetConfigurator` so every `ResourceSet` the framework's `ResourceSetFactory` builds has
its package registry replaced by the Atlas-aware delegating registry. A resource load that references an
unknown nsURI then resolves it from the Atlas. Toggled by `resource.set.fallback` (default `true`). This is
where P3-5's lazy registry and P3-9's atomic reads actually reach EMF resource loads.

## How the framework picks it up

`emf.osgi`'s `DefaultResourceSetFactoryComponent` has a `DYNAMIC`/`MULTIPLE` `ResourceSetConfigurator`
reference with **no target filter**, and calls `configureResourceSet(rs)` on every configurator for each
`ResourceSet` it creates. So simply registering our configurator as an OSGi service is enough to have it
applied to all framework-produced `ResourceSet`s; unregistering it stops *new* ones from being wrapped.

## `AtlasResourceSetConfigurator`

`configureResourceSet(rs)` sets the `ResourceSet`'s package registry to the shared
`LazyResolvingPackageRegistry` (idempotent — skips if already that registry). The interface has **no
unconfigure hook**.

**Why install the shared registry rather than per-RS wrapping.** P3-5 built one `LazyResolvingPackageRegistry`
whose `primary` is the framework's default `EPackage.Registry` (the `default.resourceset.epackage.registry=true`
service) — which is exactly the registry a default-factory `ResourceSet` already carries. So installing it
preserves local-first (its `primary` is checked before any Atlas call) and adds the Atlas fallback on a miss,
for the default factory — the "framework factory" the ticket targets. Sharing one instance also shares the
in-flight de-dup and the publish path. (A `ResourceSet` built by some *non-default* factory with a different
registry would have that registry replaced rather than wrapped; faithful per-RS wrapping for that niche is a
possible follow-up. Documented, not implemented.)

## Wiring (`AtlasClientComponent`)

When `configuration.isResourceSetFallback()` (default `true`), the component registers
`new AtlasResourceSetConfigurator(lazyRegistry)` as a `ResourceSetConfigurator` service and keeps the
`ServiceRegistration`. `resource.set.fallback=false` → it is not registered → no wrapping (the sanctioned way
to forbid implicit network calls during resource loads). `tearDown()` (shared by `@Deactivate` and the
strict-activation-failure path) unregisters it **first**, so no further `ResourceSet`s are wrapped before the
client is closed. The now-unused `lazyRegistry()` accessor was removed (the configurator holds the registry).

**Lifecycle corner (documented):** because the interface has no unconfigure, `ResourceSet`s already created
keep the delegating registry after the configurator is unregistered / the client closes. That is benign —
local resolution via `primary` is unaffected, and the Atlas fallback on a closed client simply throws
internally and is caught, so `getEPackage` returns `null` for a genuine miss (same as "not found").

## Tests

`AtlasResourceSetConfiguratorTest` (real `ResourceSetImpl` / `EPackageRegistryImpl`): installs the registry
(and it differs from the original); is idempotent; and — end-to-end — a `ResourceSet` configured with a real
`LazyResolvingPackageRegistry` (mocked remote that `resolve()`s the nsURI, a publication that mirrors it into
the framework registry to simulate the `emf.osgi` bind) resolves an **unknown nsURI** through
`resourceSet.getPackageRegistry().getEPackage(...)` — the AC's "XMI load with an unknown nsURI succeeds via
Atlas fallback" at unit level. Full XMI loading is covered by P2-8's test and the P3-12 OSGi ITs.

Build green (osgi).

## Status

P3-10 complete and build-green. Next: P3-11 (`EPackage.Registry.INSTANCE` mirroring, opt-in via
`register.in.global.registry`).
