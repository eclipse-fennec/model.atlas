# P3-3 — Implementation note (EPackage publisher with `atlas.*` properties)

**Ticket:** P3-3 "EPackage publisher with `atlas.*` properties" (Phase 3).
**Depends on:** P3-1, P2-4. **Date:** 2026-06-08.

## Scope

Publish a remotely fetched EPackage to the OSGi service registry as the trio `emf.osgi` expects — an
`EPackageConfigurator`, the `EPackage`, and its `EFactory` — all carrying the **identical** property set,
so `emf.osgi`'s `DefaultEPackageRegistryComponent` binds the configurator and populates
`EPackage.Registry`. This is the publish/unpublish **mechanism** only; *when* it fires (EAGER/LAZY/HYBRID,
local-first, drift) is P3-4 … P3-9.

Pattern mirrors the in-repo server-side `DynamicEPackageRegistrationService` /
`DynamicEPackageConfigurator` (workflow bundle), which already does exactly this registration.

## `RemoteEPackageConfigurator`

`implements EPackageConfigurator`: `configureEPackage`/`unconfigureEPackage` put/remove the package by
nsURI. `getServiceProperties()` builds the property set — canonical `emf.*` via `EMFNamespaces` constants
only (no string literals), plus the `atlas.*` origin properties via `AtlasProperties`:

| Property | Constant | Value |
|---|---|---|
| `emf.name` | `EMF_NAME` | EPackage name |
| `emf.nsURI` | `EMF_MODEL_NSURI` | nsURI |
| `emf.version` | `EMF_MODEL_VERSION` | version (default `1.0` if unresolved) |
| `emf.fileExtension` | `EMF_MODEL_FILE_EXT` | `ecore` |
| `emf.registration` | `EMF_MODEL_REGISTRATION` | `MODEL_REGISTRATION_DYNAMIC` |
| `emf.model.scope` | `EMF_MODEL_SCOPE` | `EMF_MODEL_SCOPE_RESOURCE_SET` (`"resourceset"`) |
| `atlas.remote` | `ATLAS_REMOTE` | `Boolean.TRUE` |
| `atlas.scope` | `ATLAS_SCOPE` | the Atlas scope |
| `atlas.stage` | `ATLAS_STAGE` | the stage fetched from |
| `atlas.base.uri` | `ATLAS_BASE_URI` | the client's base URI |

**Decision (user, revised 2026-06-09 during P3-5):** `emf.model.scope` is set to
`EMFNamespaces.EMF_MODEL_SCOPE_RESOURCE_SET` (`"resourceset"`) — the EMF-OSGi scope concept, as the design
draft originally specified — **not** the Atlas scope. Rationale uncovered while reading the
`emf.osgi.component` sources for P3-5: `DefaultEPackageRegistryComponent` (and the default
`ResourceSetFactory`, via `default.resourceset.epackage.registry=true`) bind their `EPackageConfigurator`
reference with a hardcoded target `(emf.model.scope=resourceset)`. A configurator stamped with the Atlas
scope (e.g. `jena`) is therefore **never** bound into the global framework `EPackage.Registry`, which would
make P3-5's "wait until visible in `EPackage.Registry`" time out forever. The server's own
`DynamicEPackageConfigurator` *does* stamp the Atlas scope, but only because it pairs each scope with a
dedicated scoped registry / `ResourceSetFactory` (`ConfigurationEPackageRegistryComponent` factory configs,
whose `ePackageConfigurator.target` can be overridden per scope); the read-only client has no such scoped
registries and wants the global one. The real Atlas scope still travels explicitly as `atlas.scope`.
(`EMF_MODEL_SCOPE_RESOURCE_SET` *does* exist in the resolved `EMFNamespaces` — the earlier note was wrong on
that point.)

`scope`/`stage`/`baseUri` are required non-null (they become `Hashtable` values, which reject null);
`version` defaults to `1.0` when null/blank.

## `RemoteEPackagePublisher`

Owns the `BundleContext` and the client's base URI; tracks registrations per nsURI in a
`ConcurrentHashMap`.

- `publish(ePackage, scope, stage, version)` — atomic & idempotent per nsURI via `computeIfAbsent`;
  blank nsURI or already-published → `false`. Registers all three services with one shared property set
  (`registerService(EPackageConfigurator.class, …)`, then `EPackage` and `EFactory` each registered under
  `[impl-class, interface]` object classes, as the server does). A missing `EFactory` is logged and the
  other two still register.
- `unpublish(nsUri)` / `unpublishAll()` — revoke in reverse order (factory → package → configurator, so
  the registry-populating configurator goes last), swallowing `IllegalStateException` from
  already-revoked services.
- `isPublished` / `publishedNsUris` — for the mode and drift components.

## Wiring (AtlasClientComponent)

`@Activate` now also takes `BundleContext`; the component constructs a `RemoteEPackagePublisher` (base URI
from the config) and calls `unpublishAll()` on `@Deactivate`. A `publisher()` accessor exposes it to the
later mode/drift components. Nothing triggers a publish yet.

## Buildpath / build

No buildpath change needed — `emf.osgi` (`EPackageConfigurator`, `EMFNamespaces`), `emf.ecore`
(`EPackage`/`EFactory`) and `org.osgi.framework` (`BundleContext`/`ServiceRegistration`) all resolve via
`-library: enableEMF`. osgi bundle builds green.

## Status

Mechanism complete and compiling; observable end-to-end behaviour (configurator picked up →
`EPackage.Registry` populated; trio properties identical) is asserted by the P3-12 OSGi ITs once a trigger
exists. Next: P3-4 (EAGER pre-fetch on activation) wires `publisher.publish(...)` to the startup scan.
