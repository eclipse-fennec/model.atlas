# P6 — Stage-aware Client (plan, for review)

**Status:** DRAFT — for Jürgen's review. Nothing implemented yet.

Companion design document: [`rest-client.md`](./rest-client.md). Ticket table at the end of
this note, in the style of [`rest-client-tickets.md`](./rest-client-tickets.md).

## Source ticket

> In its current Version, the Model Atlas Client only supports connecting against the Final
> Stage, marked via the `ReadableScopeService`.
>
> There will be situations though, where a Client must be configured against e.g. a testing
> Stage, like Snapshot in Software Development. Therefore we need the following:
>
> 1. The `ReadableScopeService` becomes a `ReadableScopeService`.
> 2. Stages can be Readable and Writable. The default is, that only the first Stage is
>    writable. There may be situations, where the next one in the cascade might be writable as
>    well.
> 3. For OSGi and OSGi environment like e.g. Sensinact, we will need an `AtlasEPackageRegistry`
>    that can be configured against a Scope and optionally a Stage. This Registry must support a
>    Parent Registry, similar to our Configurable Registry in FennecEMF.

## Model facts this plan is grounded on

- **Stage is a `Registry` containment, not a `Scope` one.** `Registry.getStages()` /
  `getAllowedTransitions()` — each registry has its *own* stage list and its own final stage.
  Two registries in the same scope can have different stages (e.g. `cocl` has `snapshot`, a
  sibling registry does not). This is the central constraint.
- **Read/write is a per-stage flag the server already tracks.** `Stage.isWritable()`,
  `RegistryService.isWritableStage(name)`, `RegistryService.isFinalStageWritable()`. Ticket
  point 2 describes existing data, not something to invent.
- **Stage-explicit read endpoints already exist server-side** (only the client stopped using
  them at P5-7):
  - EObjects: `GET /{s}/registries/{r}/stages/{stage}` (listing),
    `…/stages/{stage}/content?objectId=`, `…/stages/{stage}/{objectId}`.
  - Schemas: `GET /{s}/schema/stages/{stage}` (listing, `nsUri` filter).
- **The client-facing info model does *not* expose stages today.** `scope.api` `ScopeInfo` /
  `RegistryInfo` carry name/description/parent/type only — no stages, no read/write flags.
- **The schema registry has a fixed well-known name** (`/{s}/schema`, `REGISTRY_NAME` constant
  in `SchemaPackagesResource`), so for EPackages "scope + stage" already pins the registry.

## Design decisions

### D1 — Final stage stays implicit and name-free; non-final stage is always explicit

A consumer that wants the published models keeps doing exactly what it does today, with **no
knowledge of any stage name**: the (renamed) `ReadableScopeService` reads each registry's final
stage, resolved server-side. A consumer that wants a non-final stage must say so **explicitly**,
naming the registry and the stage, through a dedicated `StagedRegistryView`.

The two paths never blur. "Fallback to final" means only *"you didn't ask for a stage"* — it is
**never** *"you asked for `snapshot`, that registry doesn't have it, so here's final."*

**Inheritance is automatic and server-driven (resolved).** An explicit-stage read is still
inheritance-aware: when the requested `(scope, registry, stage)` does not hold the object, the
server reads through the parent hierarchy to the **parent scope's final stage** — the same
read-through the final-stage path uses — and the client relies on this rather than walking
parents itself. The "no silent demotion" rule is about the *same* scope (asking `snapshot` never
quietly yields that scope's own final); inheriting a parent's published (final) content is the
expected behaviour, not a demotion. A registry that has no such stage at all remains a hard
`400/404` ("stage not available for registry"), unchanged.

> **Difference vs. the ticket (confirmed acceptable).** The ticket says "a **Client** [is]
> configured against a … Stage," i.e. a client-wide stage. We are *not* doing that for EObjects, because
> stages live on registries and differ between them — a single client-wide EObject stage cannot
> be honoured uniformly and would force the silent-fallback behaviour we want to avoid. Instead,
> a non-final EObject read is an explicit per-`(registry, stage)` **view**. This is strictly
> more capable (it also expresses "snapshot in `cocl`, final everywhere else", which a
> client-wide stage cannot) and never lies about which stage served a read. **For EPackages the
> ticket's wording is honoured verbatim** — see D3.

### D2 — Stage-bound read contracts live in `scope.api`

`StagedRegistryView` (and the shared read shape it uses) go into `scope.api`, next to
`ReadableScopeService`, so the in-process server and the OSGi front-end can also *publish*
stage-bound views — symmetric with how `ReadableScopeService` is published today. Not
client-only.

### D3 — EPackages: `AtlasEPackageRegistry` configured by scope + optional stage

Because the schema registry name is fixed, "scope + optional stage" fully determines what to
read — this is the ticket's point 3 honoured as written, and it *is* a `(registry, stage)`
binding under the hood (registry = `schema`). No stage → that scope's schema **final** stage.

## Proposed API shape

### `scope.api`

- **Rename** `ReadableScopeService<T>` → `ReadableScopeService<T>`. `WritableScopeService`
  re-parents to it (`WritableScopeService extends ReadableScopeService`). Read methods are
  unchanged and stay **stage-free** (registry-as-parameter, each registry's final stage).
- **New** `ReadableRegistryView<T extends EObject>` — the read vocabulary bound to one registry:
  `Optional<T> get(String objectId)`, `List<String> listObjectIds()`, `List<T> listAll()`,
  `Stream<T> stream()`, plus `getScopeName()`, `getRegistryName()`, `getStage()` (the bound
  stage, or the resolved final-stage name / `null` when final). `StagedRegistryView` is simply a
  `ReadableRegistryView` whose stage was given explicitly.
- **Factory on the scope service:**
  `ReadableRegistryView<T> registryView(String registry)` (final stage) and
  `registryView(String registry, String stage)` (explicit). The scope is already bound by the
  service, so callers pass only registry (+ stage).
- **Stage metadata on `RegistryInfo`:** add `List<StageInfo> getStages()` where `StageInfo`
  carries `name`, `readable`, `writable`. Sourced from the workflow model
  (`Stage.isWritable()` etc.). This is what makes "which stages does this registry have, and
  which are writable" answerable on the client without guessing names.

### Client (`rest.client.api` / `rest.client.impl`)

- `RemoteReadableScopeService` → `RemoteReadableScopeService`; gains the `registryView(...)`
  factory returning a `RemoteStagedRegistryView` that targets the stage-explicit URLs.
- Cache key gains a **stage** component (`ObjectKey` becomes `(scope, registry, stage,
  objectId)`); the EPackage cache key gains stage. A draft fetch and a final fetch of the same
  id/nsURI must not collide.
- Drift revalidates each entry against **the stage it was fetched from** (drift keys carry
  stage; conditional GET hits the stage-explicit URL). A `snapshot`-bound view gets
  snapshot-drift; the final view gets final-drift.
- The SCHEMA-registry guard (rejecting object-API reads of a SCHEMA registry) is unaffected and
  applies at every stage.

### OSGi (`rest.client.osgi`)

- **`AtlasEPackageRegistry`** — built on the **same FennecEMF mechanism the server already uses**
  in `SchemaRegistryChainConfigurator`: per scope (+ optional stage), generate an emf.osgi
  configurable EPackage registry + `ResourceSetFactory` pair via the factory PIDs
  `EMFNamespaces.EPACKAGE_REGISTRY_CONFIG_NAME` / `RESOURCE_SET_FACTORY_CONFIG_NAME`, with the
  **`parentRegistry.target`** property providing the parent chain (Jürgen's "Parent Registry").
  This is the drop-in Sensinact expects — no bespoke registry interface to invent.
  - The chaining rule can mirror the server's: stage[i] → stage[i+1] → parent scope's final-stage
    pair → the default registry (`default.resourceset.epackage.registry=true`). For the simple
    "scope + one stage" client config, that's a single pair whose `parentRegistry.target` points
    at the caller-supplied parent (or the default registry).
  - **Atlas-specific delta vs. the server configurator:** the server *aggregates locally
    published* EPackage services (tagged `emf.model.scope` + stage); the client must *fetch the
    EPackage from the remote Atlas* at the configured scope/stage and feed it in, then keep it
    fresh via drift. Our existing `RemoteEPackageConfigurator` already publishes the trio with
    `atlas.*`/`emf.*` properties — the work is to (a) tag those publications so the configurable
    registry's target filter aggregates them, and (b) decide the miss path (below).
  - **One implementation choice to settle during P6-6** (engineering, not a question for Jürgen):
    either *(A)* reuse the stock FennecEMF configurable registry and have the Atlas client publish
    fetched packages into it — clean reuse, but the stock registry aggregates existing services
    and has no fetch-on-miss hook, so it fits EAGER/HYBRID better than pure LAZY; or *(B)* a
    registry that honours the same config surface (`parentRegistry.target`, scope/stage) **and**
    fetches from the Atlas on a miss (today's `AtlasDelegatingPackageRegistry` already has the
    parent + fetch-on-miss shape) — keeps LAZY. Likely a small hybrid; resolved when P6-6 starts.
- The per-scope `ReadableScopeService` publication (P5-4) and the published EPackage services
  gain an `atlas.stage` property so two front-ends (e.g. one final, one snapshot) for the same
  scope can be told apart by consumers via `(&(atlas.scope=…)(atlas.stage=…))`.

## Server impact (small)

The read endpoints already exist (above). The one addition needed: **`GET /scopes/{s}` must
carry each registry's stages and their read/write flags**, so the client `RegistryInfo` can
expose them (D-`RegistryInfo.getStages()`). Everything else is client/OSGi side.

## Cross-cutting notes & risks

- **Rename blast radius.** `ReadableScopeService` is referenced across `scope.api`, the server
  workflow impls, the client impl, the OSGi publisher (registers under
  `ReadableScopeService.class`), the validation service `@Reference`, tests, and both client
  READMEs. Mechanical, but spans server + client + consumers — not a client-only change.
- **Cache isolation by stage** is the highest-risk correctness item; without it stages alias.
- **Client writes are out of scope here**: this plan does the
  rename and the readable/writable *metadata*, but does not expose `WritableScopeService` over
  REST in the client. Writing remains a server/workflow capability.

## Decisions resolved since the first draft

- **EObject stage granularity (the D1 difference)** — **confirmed acceptable.** Non-final EObject
  reads are explicit per-`(registry, stage)` views; EPackages keep the client-wide scope+stage
  configuration as the ticket wrote it.
- **FennecEMF Configurable Registry** — resolved: reuse the emf.osgi configurable EPackage
  registry + `ResourceSetFactory` mechanism, chained via `parentRegistry.target`, exactly as the
  server-side `SchemaRegistryChainConfigurator` (workflow bundle) already does per scope/stage.
  See the OSGi section for the one implementation choice (A/B) left to settle when P6-6 starts.
- **Inheritance under an explicit stage** — read-through to the parent hierarchy's **final**
  stage, automatically via the server services (see D1). The client does not walk parents itself.
- **One schema registry per scope** — assumed, though not enforced server-side; EPackage
  "scope + stage" is therefore unambiguous in practice.
- **Client writes** — out of scope for P6 (the rename + readable/writable *metadata* only);
  writing stays a server/workflow capability.

## Open questions

None outstanding — all prior questions are resolved above. The plan is ready for Jürgen's
overall sign-off; the only deferred item is the A/B implementation choice for `AtlasEPackageRegistry`,
which is an engineering decision to make at the start of P6-6 (not a blocker for approval).

## Ticket breakdown (proposed)

| ID | Title | Est. (PD) | Depends on |
|---|---|---:|---|
| P6-1 | Expose stages + read/write flags on `GET /scopes/{s}`; add `RegistryInfo.getStages()` / `StageInfo` in `scope.api` | 2 | — |
| P6-2 | Rename `ReadableScopeService` → `ReadableScopeService` across server, client, OSGi, consumers, docs | 2 | — |
| P6-3 | `ReadableRegistryView` + `StagedRegistryView` in `scope.api`; `registryView(registry[, stage])` factory | 1.5 | P6-2 |
| P6-4 | Client impl: `RemoteStagedRegistryView` over stage-explicit URLs; stage in cache key | 3 | P6-3 |
| P6-5 | Stage-aware drift (per `scope/registry/stage`) | 2 | P6-4, P2-7 |
| P6-6 | `AtlasEPackageRegistry` (scope + optional stage; emf.osgi configurable registry + RSF + `parentRegistry.target`, mirroring `SchemaRegistryChainConfigurator`) | 4 | P6-1, P2-8 |
| P6-7 | `atlas.stage` on per-scope `ReadableScopeService` + EPackage publications; consumer-filter story | 1 | P6-2, P6-6 | ✅ DONE |
| P6-8 | Client write path against writable stages (future — out of scope for P6) | TBD | P6-1 |
| P6-9 | Tests (stage-explicit URL shape, cache isolation between stages, stage-aware drift, parent-chain) + README updates | 3 | P6-4…P6-7 |

## P6-1 detail — expose per-registry stages + read/write flags

**Core insight.** `GET /scopes/{s}` already serializes the whole `Scope` EObject
(`ScopesResource.getScopeByName` returns it wholesale), and the workflow `Registry` already owns
`stages` (each `Stage` carries `name` / `writable` / `final`). So the flags are very likely
**already on the wire** — the gap is that the client-facing `scope.api` model can't represent
them (`RegistryInfo` has no `stages`; `parseScopeInfo` ignores them). P6-1 is therefore mostly a
**shared-model + client-parse** change, not a server change.

**Forced model reconciliation.** `scope.api` `RegistryInfo` and workflow `Registry` share the
`Registry extends RegistryInfo` inheritance, so `stages` cannot live on both (duplicate-feature
clash). The fix mirrors the existing `Registry extends RegistryInfo` / `Scope extends ScopeInfo`
pattern: lift the stage attributes into a new `StageInfo` in `scope.api`, move the `stages`
containment up to `RegistryInfo`, and make `Stage extends StageInfo`.

**Decision baked in.** `StageInfo` carries `readable` (EBoolean, default `true`) alongside
`name` / `writable` / `final`, to match the ticket's "Readable and Writable" wording. Whether the
workflow ever sets `readable = false` (a real non-readable stage) is a later semantics question,
not a P6-1 blocker.

### Steps

0. **Verify the wire (do first).** Hit `GET /scopes/jena`; confirm the JSON contains
   `registries[].stages[]` with `name` / `writable` / `final`. Present → no server logic change
   (only the OpenAPI schema updates on regen). Absent → add a small serialization step (not
   expected).
1. **`scope-api.ecore` (+ genmodel).** Add `StageInfo` EClass (`name` EString, `readable`
   EBoolean default `true`, `writable` EBoolean, `final` EBoolean). Add `RegistryInfo.stages`
   (`StageInfo`, `[0..*]`, containment). Update the doc annotations on `ScopeInfo` /
   `getScopeInfo()` that say "without the workflow stage/transition detail" — stages (name +
   flags) are now included; **transitions still are not**.
2. **`workflow-api.ecore` (+ genmodel).** `Stage`: add `eSuperTypes` → scope-api `#//StageInfo`,
   remove its now-inherited `name` / `writable` / `final`. `Registry`: remove its own `stages`
   containment (now inherited), keep `allowedTransitions` (workflow-only, not client-exposed).
3. **Regenerate** scope.api (`src-gen`) and workflow (`src-wf-api`) from the genmodels.
4. **Fix compile fallout** (~19 sites in the workflow bundle). Most call
   `getName()` / `isFinal()` / `isWritable()` (now on `StageInfo`) and keep compiling; the edits
   are mainly declared types `List<Stage>` → `List<StageInfo>` (`SchemaRegistryChainConfigurator`,
   `RegistryServiceImpl`, `AtlasSchemaRegistryService`, `EPackageStageActionService`,
   `StorageRegistryServiceImpl`). `WorkflowApiFactory.createStage()` still returns `Stage` (a
   `StageInfo`), so building/adding stages is unchanged.
5. **Client parse.** `RemoteReadableScopeService.parseScopeInfo`: read each registry's `stages`
   array into `StageInfo` via `ScopeApiFactory` (mirroring the `type` parse) and
   `ri.getStages().add(...)`.
6. **Tests.** Extend `RemoteReadableScopeServiceTest.getScopeInfo_*` to assert parsed stages +
   flags; a scope.api model-load smoke test; optionally a server `rest.tests` assertion that the
   JSON carries `stages`.
7. **OpenAPI sanity check.** `@Schema(implementation = Scope.class)` on `ScopesResource` picks up
   the new structure on regen; eyeball the generated spec.

**Done when:** `scope.api` exposes `RegistryInfo.getStages()` → `List<StageInfo>`
(name/readable/writable/final); both models regenerate and the workflow bundle compiles; the
client populates stages from `GET /scopes/{s}`; tests green. Unblocks P6-3 (needs a registry's
stages); independent of P6-2.
