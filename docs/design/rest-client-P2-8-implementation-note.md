# P2-8 — Implementation note (`AtlasDelegatingPackageRegistry` + `newResourceSet()`)

**Ticket:** P2-8 "`AtlasDelegatingPackageRegistry` + `newResourceSet()`" (Phase 2).
**Depends on:** P2-3. **Date:** 2026-06-05.

## Scope

An `EPackage.Registry` that, on a local miss, falls back to fetching the package from the Atlas, caches
the hit for direct subsequent look-ups, and evicts on drift. `ModelAtlasClient.newResourceSet()` returns
a `ResourceSetImpl` with this registry pre-installed — the one-liner for plain-Java consumers loading
XMI/JSON that references packages they don't ship locally.

## `AtlasDelegatingPackageRegistry`

A standalone `public` class (so the Phase-3 OSGi `ResourceSetConfigurator` can reuse it) that
`extends ConcurrentHashMap<String,Object> implements EPackage.Registry, DriftListener`:

- **`getEPackage(nsURI)`** resolves in order: (1) the **primary** registry (framework / `EPackage.Registry.INSTANCE`)
  — local precedence; (2) this registry's own already-fetched entries (direct hit); (3)
  `RemoteEPackageProvider.ensureAvailable(nsURI)`, caching a hit here. Returns `null` when nothing has it.
- **`getEFactory(nsURI)`** falls back to the fetched package's `getEFactoryInstance()`.
- **Drift**: as a `DriftListener`, `onPackageChanged`/`onPackageRemoved` simply **evict** the entry, so the
  next look-up re-fetches (the underlying `RemoteEPackageProvider` cache already holds the fresh copy from
  the drift refresh).
- Backed by a `ConcurrentHashMap` because loads (read) and drift eviction (write) run on different threads.

`null` nsURI is handled by delegating to the primary only (no remote fetch / no `ConcurrentHashMap`
null-key issue).

## `newResourceSet()`

```
ResourceSetImpl rs = new ResourceSetImpl();
rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl()); // default XMI
rs.setPackageRegistry(new AtlasDelegatingPackageRegistry(EPackage.Registry.INSTANCE, ePackagesImpl()));
addDriftListener(registry); // evict on drift
return rs;
```

A default XMI resource factory is registered so consumers can load instances with no extra setup
("nothing else is required", per the design). The registry is registered as a drift listener for the
client's lifetime.

**Lifecycle note:** each `newResourceSet()` registers one drift listener that lives until the client is
closed (the watcher holds it). Typical use creates few Atlas-aware ResourceSets, so this is fine;
revisit if a consumer churns many.

## Acceptance-criteria coverage

| Criterion | Test (`AtlasDelegatingPackageRegistryTest`, unless noted) |
|---|---|
| XMI referencing an unknown nsURI loads via the delegating registry | `loadsXmiInstanceReferencingUnknownNsUri` — serializes an `Item` instance of a dynamic package, loads it through a ResourceSet wired like `newResourceSet()` with a fake provider; the nsURI resolves through the Atlas fallback and the instance + its `label` come back |
| Drift evicts; subsequent look-ups re-fetch | `driftEvicts_soNextLookupRefetches` (`ensureAvailable` call count goes 1 → 1 (direct hit) → 2 after `onPackageChanged` → 3 after `onPackageRemoved`) |
| Primary registry takes precedence over the Atlas fallback | `primaryRegistryTakesPrecedence` (remote never consulted) |
| (fallback + caching, factory, unknown) | `fallsBackToRemote_andCachesForDirectHits`, `getEFactory_fallsBackToFetchedPackage`, `unknownNsUri_returnsNull` |
| `newResourceSet()` installs the registry | `ModelAtlasClientRestMappingTest.newResourceSet_installsAtlasDelegatingRegistry` |

Tests use a `FakeProvider` (in-memory `RemoteEPackageProvider`, counts `ensureAvailable`) and EMF XMI
fixtures — no server.

## Cleanup

`newResourceSet` was the last "not yet implemented" stub on `ModelAtlasClientImpl`, so the obsolete
`pendingOperationsFailFastUntilLaterTickets` test and the unused `notYetImplemented` helper were removed.

## Build status

As of 2026-06-05 `:rest.client.api:build` and `:rest.client.impl:build` are green (53 unit tests).
Remaining in Phase 2: P2-9 (nsURI allow/deny lists), P2-10 (bearer/mTLS auth providers), P2-11 (live
integration tests).
