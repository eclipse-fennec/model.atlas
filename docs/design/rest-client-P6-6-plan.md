# P6-6 — `AtlasEPackageRegistry` (scope + optional stage) — execution plan

**Ticket:** P6-6 (depends on P6-1 ✓, P2-8). OSGi-side, the largest P6 ticket.

Give an OSGi/Sensinact consumer a **configurable EPackage registry + `ResourceSetFactory` pair,
keyed by Atlas scope (+ optional stage), chained via `parentRegistry.target`** — the *same*
emf.osgi mechanism the server's `SchemaRegistryChainConfigurator` already uses, but fed from the
**remote** Atlas instead of locally-published EPackages.

> **DECISION: Hybrid approach chosen.** Stock emf.osgi configurable registry (Option A) for
> aggregation, chaining, and server fidelity, with a new `AtlasScopedFetchOnMissRegistry` OSGi
> service as the parent (Option B mechanism) for stage-aware fetch-on-miss. See "Chosen approach:
> Hybrid" below. Everything in "Common work" is needed either way.

## Grounding facts (from the current code)

**Server mechanism — `workflow/…/impl/SchemaRegistryChainConfigurator.java`** (mirror this):
- Per scope, per stage: one ConfigAdmin pair via factory PIDs `EMFNamespaces.EPACKAGE_REGISTRY_CONFIG_NAME`
  (`"EPackageRegistry"`) and `RESOURCE_SET_FACTORY_CONFIG_NAME` (`"ResourceSetFactory"`).
- `EPackageRegistry` config: `rsf.name = <scope>_<stage>`;
  `ePackageConfigurator.target = (&(emf.model.scope=<scope>)(atlas.stage=<stage>))`;
  `parentRegistry.target = <parent filter>`.
- `ResourceSetFactory` config: `ePackageRegistry.target = (rsf.name=<scope>_<stage>)`, `scope.name`, `stage.name`.
- Chain: `stage[i] → (rsf.name=<scope>_<stage[i+1]>)`; final stage → parent scope's final-stage
  `rsf.name`, or the default `(default.resourceset.epackage.registry=true)` when parent is absent/`atlas`.
- Property keys (constants): `EMFNamespaces.EPACKAGE_TARGET="ePackageConfigurator.target"`,
  `EPACKAGE_REGISTRY_TARGET="ePackageRegistry.target"`, `PROP_RESOURCE_SET_FACTORY_NAME="rsf.name"`,
  `EMF_MODEL_SCOPE="emf.model.scope"`; `WorkflowConstants.ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY="atlas.stage"`;
  default-registry filter `(default.resourceset.epackage.registry=true)`.

**Client EPackage path today:**
- `rest.client.osgi/…/RemoteEPackageConfigurator` stamps each published trio
  (EPackageConfigurator + EPackage + EFactory) with `atlas.remote=true`, `atlas.scope=<scope>`,
  `atlas.stage=<stage>` **only when known**, `atlas.base.uri`, and `emf.model.scope="resourceset"`
  (the EMF default scope — feeds the P3-10/11 global registry; **leave this as-is**).
- `RemoteEPackagePublisher.publish(ePackage, scope, stage, version)` does the atomic trio publish.
- `RemoteEPackageProvider(Impl)` reads **stage-free**: `GET /{scope}/schema`,
  `GET /{scope}/schema/content?nsUri=…`. **No stage-explicit fetch is wired client-side yet**
  (server has `GET /{scope}/schema/stages/{stage}`).
- `AtlasDelegatingPackageRegistry` = primary(local) → own-fetched → remote fetch-on-miss; already a
  parent + fetch-on-miss shape. `LazyResolvingPackageRegistry` serves general lazy resolution.
- `AtlasClientComponent` (factory PID `org.eclipse.fennec.model.atlas.rest.client`) config carries
  `base_uri`, `mode` (EAGER/LAZY/HYBRID), `eager_scopes`, `eager_stages` (default `{"released"}`,
  **currently not used in any REST call**), `scope_allow_list`, `default_scope`.

**Key consequence:** the client's per-scope registry should set
`ePackageConfigurator.target` on the **`atlas.*`** properties the client already stamps —
`(&(atlas.remote=true)(atlas.scope=<scope>)[(atlas.stage=<stage>)])` — **not** `emf.model.scope`.
That reuses the server's exact registry/RSF/chain mechanism while leaving the global mirroring path
untouched. The server filters on `emf.model.scope` only because *its* local publications are tagged
that way; the client's are tagged `atlas.*`.

---

## Common work (needed under both A and B)

1. **`AtlasEPackageRegistryConfigurator` (new, `rest.client.osgi`).** Given the client's configured
   scope (+ optional stage) — sourced from `AtlasClientConfig` / a dedicated factory config — generate
   the ConfigAdmin `EPackageRegistry` + `ResourceSetFactory` pair, mirroring
   `SchemaRegistryChainConfigurator`'s property shape:
   - `rsf.name = <scope>` (final) or `<scope>_<stage>` (explicit).
   - `ePackageConfigurator.target = (&(atlas.remote=true)(atlas.scope=<scope>))`, adding
     `(atlas.stage=<stage>)` when a stage is configured.
   - `parentRegistry.target` = the caller-supplied parent target, else
     `(default.resourceset.epackage.registry=true)`. (Single pair for the simple
     "scope + one stage" config; the full stage→stage→parent-final chain is only needed if the
     client ever models multiple stages — keep the hook but the common case is one pair.)
   - `ResourceSetFactory`: `ePackageRegistry.target=(rsf.name=…)`, `scope.name`, `stage.name`.
2. **Stage-explicit EPackage fetch.** When a stage is configured, the publications must carry
   `atlas.stage` so the registry's target matches — which means fetching at that stage. Add a
   stage-aware path to `RemoteEPackageProvider(Impl)` hitting `GET /{scope}/schema/stages/{stage}`
   (listing + `?nsUri=` + `/content`), mirroring the EObject stage-explicit URLs from P6-4. Final
   (no stage) keeps the existing stage-free path; `atlas.stage` stays unset and the target filters on
   scope only.
3. **`atlas.stage` on publications (overlaps P6-7).** Ensure `RemoteEPackageConfigurator` stamps
   `atlas.stage` whenever the fetch stage is known (it already does when non-blank). For the
   stage-explicit fetch the stage is always known.
4. **Config surface.** Decide where scope/stage/parentRegistry.target come from — extend
   `AtlasClientConfig` or a new factory PID for "one AtlasEPackageRegistry per (scope[,stage])".
   `eager_stages` already exists but is unused; this ticket is where a configured stage finally
   drives a real fetch.

## The A/B choice (resolved → Hybrid; kept for rationale)

Both deliver the same externally-visible artifact (a configurable registry + RSF the consumer
targets). They differ in **how a package that hasn't been fetched yet gets into the registry.**

### Approach A — reuse the stock emf.osgi configurable registry, populate by prefetch

The configurator (Common work #1) creates the **stock** FennecEMF configurable `EPackageRegistry` +
`ResourceSetFactory`. The client **fetches the scope/stage's EPackages and publishes them as
services** (existing `RemoteEPackagePublisher`), tagged `atlas.scope`(+`atlas.stage`); the stock
registry aggregates everything matching its `ePackageConfigurator.target`.

- **How packages arrive:** EAGER/HYBRID prefetch of the configured scope(+stage) publishes the trio
  → the stock registry picks them up via its target filter. Drift keeps them fresh (P6-5 EObject
  drift is separate; EPackage drift via `Atlas-Changed-NsUris` already exists).
- **Pros:** byte-for-byte the server's mechanism; `parentRegistry.target` chaining for free; the
  cleanest Sensinact drop-in; no bespoke registry code.
- **Cons:** the stock registry has **no fetch-on-miss hook**, so a lookup for an nsURI never
  prefetched into *this* registry misses. Fine for Sensinact (a known scope is prefetched);
  general lazy resolution still goes through the existing `LazyResolvingPackageRegistry`/global path.
- **Shape:**
  ```
  EPackageRegistry  cfg: ePackageConfigurator.target=(&(atlas.remote=true)(atlas.scope=S)[(atlas.stage=ST)])
                         parentRegistry.target=<caller|default>   rsf.name=S[_ST]
  ResourceSetFactory cfg: ePackageRegistry.target=(rsf.name=S[_ST])
  prefetch: GET /S/schema[/stages/ST] -> publish trio(atlas.scope=S[,atlas.stage=ST]) -> aggregated
  ```

### Approach B — custom registry honouring the same config surface + fetch-on-miss

Extend `AtlasDelegatingPackageRegistry` (already parent + fetch-on-miss) to honour the same config
properties (`scope`, optional `stage`, `parentRegistry.target`) and register as the scope's
`EPackage.Registry`. A lookup miss fetches from Atlas (stage-explicit when configured) and caches.

- **How packages arrive:** on demand — true LAZY; first `get(nsURI)` miss triggers the fetch.
- **Pros:** keeps pure-LAZY into *this* registry; reuses the existing fetch-on-miss shape.
- **Cons:** forks the configurable-registry behaviour, so it's **not** the identical server
  mechanism; the parent chain is wired bespoke rather than via the stock `rsf.name` RSF chain; more
  client code to own and test.
- **Shape:**
  ```
  AtlasDelegatingPackageRegistry(scope=S, stage=ST, parent=<target>)
    get(nsURI): local -> own-cache -> FETCH /S/schema[/stages/ST] -> cache
    registered as EPackage.Registry (scope S)
  ```

### Chosen approach: Hybrid

**Decision:** use Option A's stock registry for structure + chaining, and add a new
`AtlasScopedFetchOnMissRegistry` DS component as the parent, providing stage-aware fetch-on-miss
(Option B's mechanism) without forking the stock registry shape.

**Why not A alone:** Option A's lazy fallback (miss → `parentRegistry.target` → default global
registry) fetches stage-free. If the package exists only at the configured stage (not yet at final),
the global fetch returns 404. If it exists at both, the consumer silently gets the wrong (final-stage)
version. HYBRID client mode does not help: it makes the *global* path lazy but the scope-specific
registry still has no stage-aware miss path.

**Why not B alone:** forks the configurable-registry mechanism; the parent chain is wired bespoke
inside the custom registry rather than via `rsf.name` / `parentRegistry.target`; more client code
to own and test.

#### Chain shape (3 ConfigAdmin configs)

```
[Stock EPackageRegistry — rsf.name=<scope>[_<stage>]]
  ePackageConfigurator.target = (&(atlas.remote=true)(atlas.scope=<S>)[(atlas.stage=<ST>)])
  parentRegistry.target       = (&(atlas.scope=<S>)[(atlas.stage=<ST>)](atlas.fetch.on.miss=true))
         ↓ prefetched packages aggregate here
         ↓ miss → delegate to parent

[AtlasScopedFetchOnMissRegistry — factory PID, registered as EPackage.Registry]
  service props: atlas.scope=<S>[, atlas.stage=<ST>], atlas.fetch.on.miss=true
  config: atlas.scope=<S>, atlas.stage=<ST> (optional), parentRegistry.target=<final-parent-filter>
  getEPackage(nsURI): own cache → GET /<S>/schema[/stages/<ST>]/content?nsUri=… → cache → return
         ↓ miss → delegate to its own parent

[Default / parent-scope final-stage registry]
  (default.resourceset.epackage.registry=true)  ← or  (rsf.name=<parentScope>_<finalStage>)

[Stock ResourceSetFactory — rsf.name=<scope>[_<stage>]]
  ePackageRegistry.target = (rsf.name=<S>[_<ST>])
  scope.name, stage.name
```

The **stock registry** aggregates everything prefetched and published as services (existing
EAGER/HYBRID path). A lookup miss falls to the **fetch-on-miss bridge** which fetches
stage-explicitly from Atlas and caches the result. A miss there falls to the original final
parent — the same target that Option A would have used directly.

#### New component: `AtlasScopedFetchOnMissRegistry`

New DS `@Component` (factory PID `AtlasScopedFetchOnMissRegistry`) in `rest.client.osgi`:

- **Config:** `atlas.scope` (required), `atlas.stage` (optional), `parentRegistry.target`
  (default `(default.resourceset.epackage.registry=true)`).
- **References:** a stage-aware `RemoteEPackageProvider` variant (uses
  `GET /<scope>/schema[/stages/<stage>]/content?nsUri=…`); a `parentRegistry`
  `EPackage.Registry` reference bound via `parentRegistry.target`.
- **Registers as:** `EPackage.Registry` OSGi service with properties `atlas.scope=<S>`,
  `atlas.stage=<ST>` (when configured), `atlas.fetch.on.miss=true`.
- **`getEPackage(nsURI)`:** check own `ConcurrentHashMap` cache → fetch stage-aware → cache hit
  → return; miss → delegate to injected parent registry.
- **Drift:** implement `DriftListener` (same as `AtlasDelegatingPackageRegistry`) — evict on
  `onPackageChanged` / `onPackageRemoved` so the next lookup re-fetches at the correct stage.
- **Reuse:** internally delegates to `AtlasDelegatingPackageRegistry` (or mirrors its cache +
  fetch logic), where `primary` is the injected parent registry from the `parentRegistry.target`
  reference.

#### Updated `AtlasEPackageRegistryConfigurator` (Common work #1)

Generates **3** ConfigAdmin factory configs per configured scope(+stage) instead of 2:

1. **Stock `EPackageRegistry`** — same as before but `parentRegistry.target` points to the
   fetch-on-miss bridge: `(&(atlas.scope=<S>)[(atlas.stage=<ST>)](atlas.fetch.on.miss=true))`.
2. **`AtlasScopedFetchOnMissRegistry`** — `atlas.scope`, `atlas.stage`, `parentRegistry.target`
   set to the original final parent (what the stock registry's parent was in pure Option A).
3. **Stock `ResourceSetFactory`** — unchanged.

## Tests

- **Configurator unit/IT:** asserts the generated `EPackageRegistry` + `ResourceSetFactory` configs
  carry the exact properties (`ePackageConfigurator.target` on `atlas.scope`(+`atlas.stage`),
  `parentRegistry.target`, `rsf.name`, `ePackageRegistry.target`) — mirror
  `SchemaRegistryChainConfiguratorIntegrationTest`'s assertions.
- **Stage-explicit fetch:** `RemoteEPackageProvider` hits `…/schema/stages/{stage}` when a stage is
  configured; stage-free otherwise (URL-shape test, like P6-4's view tests).
- **Aggregation IT (OSGi):** publish a fetched package tagged `atlas.scope`(+`atlas.stage`); assert
  the configurable registry resolves its nsURI and a `ResourceSetFactory` from the pair loads it.
  Parent-chain: an nsURI absent locally resolves through `parentRegistry.target`.
- **Hybrid fetch-on-miss:** simulate a package added to Atlas *after* prefetch (i.e. not in
  the stock registry's aggregated set); assert `AtlasScopedFetchOnMissRegistry.getEPackage(nsURI)`
  fires `GET /<scope>/schema/stages/<stage>/content?nsUri=…` (stage-explicit) and returns the
  package. Assert a stage-free lookup (no stage configured) fires `GET /<scope>/schema/content?nsUri=…`.
- **Wrong-stage guard:** assert that when `gateway` exists only in `snapshot` (not at final stage),
  the hybrid bridge resolves it correctly while the fallback-to-default path (pure Option A) would
  return null; documents the gap Option A alone leaves.
- **Drift eviction:** assert `onPackageChanged` evicts the bridge's cache so the next miss
  re-fetches the updated package at the correct stage.

## Verify

```bash
cd /opt/git/model.atlas
./gradlew :org.eclipse.fennec.model.atlas.rest.client.osgi:build
# plus the OSGi IT bundle once the aggregation IT lands (resolve + testOSGi)
```

**Done when:** an OSGi consumer can configure an `AtlasEPackageRegistry` by scope (+ optional stage),
get a configurable `EPackageRegistry` + `ResourceSetFactory` pair whose packages come from the remote
Atlas at that scope/stage, with a working `parentRegistry.target` chain, kept fresh by drift; the
chosen A/B (or hybrid) behaviour is implemented and tested. Unblocks P6-7 (`atlas.stage` publication
+ consumer-filter story).
