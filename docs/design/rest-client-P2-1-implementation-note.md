# P2-1 — Implementation note (`rest.client.api` bundle)

**Ticket:** P2-1 "`rest.client.api` bundle with public interfaces and config types" (Phase 2).
**Depends on:** — (first Phase-2 ticket). **Date:** 2026-06-05.

## Scope

Create the public API surface that both client front-ends sit on: the plain-Java implementation
(`rest.client.impl`, P2-2+) and the OSGi front-end (`rest.client.osgi`, Phase 3). Pure contract —
interfaces, value types, the provider SPI, constants, exceptions. No client logic, no caching, no
REST mapping (those are P2-2+).

## Dependency decision (resolves a ticket inconsistency)

The ticket lists both "Exports the `JakartaRsClientProvider` SPI" and "Bundle has only EMF core as a
runtime dependency". Those conflict: the SPI's whole purpose is to hand back a
`jakarta.ws.rs.client.Client`, so the api package must reference `jakarta.ws.rs.client`.

Resolution (confirmed with the architect): **"only EMF core" was shorthand for "no OSGi runtime
dependencies"** — the bundle is the shared contract for the plain-Java *and* OSGi impls, so it must not
drag in DS / framework / ConfigAdmin. `jakarta.ws.rs` (the **spec API**, not an implementation) is fine.

The generated manifest confirms the intent holds:

```
Export-Package: org.eclipse.fennec.model.atlas.rest.client.api;version="1.0.0"
Import-Package: jakarta.ws.rs.client, java.*, org.eclipse.emf.ecore, org.eclipse.emf.ecore.resource
Require-Capability: osgi.ee (JavaSE 21)
```

No `org.osgi.*` runtime imports. The `@ProviderType` / `@ConsumerType` / `@Export` / `@Version`
annotations are build-time-only (bnd manifest generation) and leave no runtime trace.

`bnd.bnd` buildpath: `org.osgi.annotation.versioning`, `org.osgi.annotation.bundle` (build-time
annotations), `org.eclipse.emf.common`, `org.eclipse.emf.ecore`, `jakarta.ws.rs-api`.

## Exported surface (all in package `…rest.client.api`, exported at 1.0.0)

| Type | Kind | Notes |
|---|---|---|
| `ModelAtlasClient` | `@ProviderType` interface, `AutoCloseable` | `listScopeNames()`, `ePackages()`, `checkForDrift()`, `addDriftListener()`, `newResourceSet()`, `close()`. |
| `RemoteEPackageProvider` | `@ProviderType` interface | `getEPackage`, `listNsUris`, `ensureAvailable`, `refresh` — all `Optional`/`List`-returning. |
| `DriftListener` | `@ConsumerType` interface | `onPackageChanged(nsUri, EPackage)`, `onPackageRemoved(nsUri)`. |
| `DriftReport` | `@ProviderType` final value type | `getChangedNsUris()`, `getRemovedNsUris()`, `hasChanges()`; `of(Set,Set)` / `empty()` factories, dedup + unmodifiable. |
| `ClientConfiguration` | final immutable value type + `Builder` | All Phase-2 config properties with defaults; `base.uri` required (else `IllegalStateException`); `equals`/`hashCode`/`toString`; `builder()` / `builder(from)`. |
| `ResolutionMode` | enum | `EAGER` / `LAZY` (default) / `HYBRID`. |
| `AuthType` | enum | `NONE` (default) / `BEARER` / `MTLS`. |
| `JakartaRsClientProvider` | `@ConsumerType` SPI | `Client newClient(ClientConfiguration)` — the single seam to Jakarta RS. |
| `AtlasProperties` | constants holder | `ATLAS_REMOTE/SCOPE/STAGE/BASE_URI` (Phase 3) + `ATLAS_REGISTRY/VIEW` (Phase 4/5). |
| `ModelAtlasClientException` | unchecked base | + `NotFoundException` (hard 404), `TransportException` (connect/timeout). Unchecked so the `Optional`-based read API stays free of checked-exception noise. |

`ClientConfiguration` field ↔ property mapping (defaults) follows the Phase-2 table in
`rest-client.md` §Configuration: `base.uri`(req), `connect.timeout.ms`(5000), `read.timeout.ms`(30000),
`mode`(LAZY), `eager.scopes`([]), `eager.stages`(["released"]), `eager.nsuri.allow.list`([]),
`mode.strict`(false), `nsuri.allow.list`([]), `nsuri.deny.list`([]), `force.remote`(false),
`register.in.global.registry`(false), `drift.check.interval.ms`(300000), `scope.allow.list`([]),
`default.scope`(null), `cache.max.entries`(500), `cache.ttl.ms`(0), `cache.disk.dir`(null),
`auth.type`(NONE), `auth.token.env`(null).

## Deferred to later phases (documented in-code as comments)

- **Phase 5 / EObject registries** (depend on the `scope.api` `ScopedEObjectsRegistry` contract from
  Phase 4): `ModelAtlasClient.listRegistries(scope)` + `registry(scope, registry)`; the
  `DriftListener.onObjectChanged/onObjectRemoved` callbacks. Omitted now because the return type does
  not exist yet — including them would not compile.
- **P2-2 / plain-Java builder**: `ModelAtlasClient.builder()` (the `.baseUri().auth().cache().build()`
  fluent entry point) lives in the impl bundle — it needs a concrete client. `ClientConfiguration` does
  ship its own builder, which that entry point will populate.
- **Phase-3 config**: `lazy.resolve.timeout.ms`, `resource.set.fallback` are added to
  `ClientConfiguration` when the OSGi front-end lands.
- **Convenience config sugar** (`BearerToken.of(...)`, `CacheSpec.inMemory(...)` from the design's
  builder example) are P2-2 builder helpers, not part of the canonical flat config.

## Build status

As of 2026-06-05 `:org.eclipse.fennec.model.atlas.rest.client.api:build` is green (compile + jar).
No tests yet — the bundle is pure declarations; behaviour arrives with `rest.client.impl` (P2-2).
The template `Example`/`ExampleTest` stubs (which carried a DS `@Component`) were removed.
