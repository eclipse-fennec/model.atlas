# P4-1 — `scope.api` bundle with `ScopedEObjectsRegistry<T>` (+ `atlas.*` constants)

> **SUPERSEDED in part — see `rest-client-P4-2-implementation-note.md`.** The read
> contract was renamed `ScopedEObjectsRegistry` → **`ReadOnlyScopeService<T>`** and made
> **per-scope with the registry as a method parameter** (not per-(scope,registry)).
> `getRegistryName()`/`getRootEClass()` were dropped; the operations now take a `registry`
> argument. The bundle/EDataTypes/`AtlasProperties`/`oSGiCompatible=false` decisions below
> still hold.

Implementation note for ticket **P4-1** (see `rest-client-tickets.md`; design in
`rest-client.md` → "Phase 4 — Server-Side Interface Split"). Phase 4 is server-side
work on branch `issue#133_atlas_client_phase4`.

## What landed

New bundle **`org.eclipse.fennec.model.atlas.scope.api`** — the read-only contract a
pure consumer of published models depends on, EMF-core only.

- **`ScopedEObjectsRegistry<T extends EObject>`** (`@ProviderType`) — read-only view of
  a single `(scope, registry)` at that registry's **final** stage:
  `getScopeName()`, `getRegistryName()`, `getRootEClass()`, `get(objectId)`,
  `listObjectIds()`, `listAll()`, `stream()`, `isInheritingFromParentScope()`.
  - **No stage parameter** — a reader sees one view (the final/released stage).
    Per-stage access is a workflow concern and stays on the writable contract (P4-2).
    The exposed stage is advertised as the `atlas.view` service property, not chosen
    per call.
  - **No `ObjectMetadata`** — content hashes/versions/storage props are wire/storage
    concerns and stay on the workflow contract.
  - Reads/listings are **read-through** to parent scopes' final stages when inheriting.
- **`AtlasProperties`** — the `atlas.*` service-property vocabulary:
  `ATLAS_SCOPE`, `ATLAS_REGISTRY`, `ATLAS_VIEW`, `ATLAS_REMOTE` (registry identity) and
  `ATLAS_STAGE`, `ATLAS_BASE_URI` (remote-publication provenance).
- `bnd.bnd`: `-library: enableEMF`. Package exported at `1.0.0` via `package-info`.
  Removed the template `Example.java`.

## Decisions

- **Package name `…scope.api` (not `…scope`).** The design doc wrote
  `org.eclipse.fennec.model.atlas.scope`; house convention (e.g. `schema.registry.api`)
  uses the `.api` suffix, so the bundle uses `org.eclipse.fennec.model.atlas.scope.api`.
  Phase 5's client mirror must match this. *(Update the doc's literal package to the
  `.api` form for consistency.)*
- **`scope.api` is the canonical home of the `atlas.*` constants.** The values mirror
  the remote client's `org.eclipse.fennec.model.atlas.rest.client.api.AtlasProperties`
  (same constant names, same string values) — see the follow-up below.

## Follow-up — consolidate the `atlas.*` constants (post-Phase-4)

The remote client (`org.eclipse.fennec.model.atlas.rest.client.api`, on the Phase-3
branch) already ships its own `AtlasProperties` with the same constant names/values.
Right now the two are **duplicated on purpose** — the client branch and this Phase-4
branch are separate, and we did not want a cross-branch dependency mid-phase.

**After Phase 4 is merged**, remove the duplication so the vocabulary can't drift:

1. Add a buildpath/dependency from `rest.client.api` onto
   `org.eclipse.fennec.model.atlas.scope.api`.
2. Delete the client's own `AtlasProperties` and re-point its consumers
   (`rest.client.osgi` `RemoteEPackageConfigurator`, `AtlasClientComponent`, the
   `RemoteEPackagePublisher`/configurator tests, the OSGi IT) at
   `org.eclipse.fennec.model.atlas.scope.api.AtlasProperties`.
   *Alternatively*, if the layering (client api depending on scope api) is unwanted,
   keep both classes but add a test asserting the string values are identical.
3. Until then: **the two files must be kept in sync by hand** — never change a value,
   only add. The class javadoc in both records this.

## Acceptance (P4-1)

- [x] Bundle builds and exports `ScopedEObjectsRegistry<T>` with the design-doc methods.
- [x] `atlas.*` service-property constants present in the bundle.
- [x] Depends only on EMF core (no workflow/storage/REST deps).
