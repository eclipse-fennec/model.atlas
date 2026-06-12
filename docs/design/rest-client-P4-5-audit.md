# P4-5 — `ScopeService<?>` consumer audit

Audit deliverable for ticket **P4-5** (see `rest-client-tickets.md`). Branch
`issue#133_atlas_client_phase4`. Classifies every `ScopeService<?>` consumer as
read-only / writable / mixed and records, for each, whether it can migrate to
`ReadOnlyScopeService` (the P4-6 question). Builds on
`rest-client-P4-1/2/3-implementation-note.md` and the P4-4 `ReadOnlyScopeCollector`.

## Method surfaces (the classification basis)

**`ReadOnlyScopeService<T>` — the migration target (6 methods):** `getScopeName`,
`isInheritingFromParentScope`, `get(registry, objectId)` (final-stage),
`listObjectIds(registry)`, `listAll(registry)`, `stream(registry)`.

**`ScopeService<T>` (14 methods)** — note **none share a name** with the 6 above:
- *Write:* `uploadToStageForRegistry`, `updateInStageForRegistry`, `deleteFromStageForRegistry`, `transitionToStageForRegistry`
- *Read (metadata / stage-explicit — on `WritableScopeService`, NOT on `ReadOnlyScopeService`):* `getMetadataFromStageForRegistry`, `getMetadataFromFinalStageForRegistry`, `getContentFromStageForRegistry`, `listInStageForRegistry`, `listInStageForRegistryByName`, `listInFinalStageForRegistry`, `listAllForRegistry`
- *Util:* `isValidRegistry`, `getAllRegistries`, `getScope`

## Injection topology

Only **3** direct `@Reference ScopeService` sites exist; everything else goes through a
collector, so migrating a consumer mostly means **re-pointing it at the new
`ReadOnlyScopeCollector`** (keyed on `atlas.scope`), not editing an `@Reference`:

- `ScopeServiceCollector` (whiteboard, `MULTIPLE`) — the fan-out hub for all REST / validation / datagen consumers.
- `ScopesHealthCheck` — direct `@Reference List<ScopeService<?>>`.
- `SchemaRegistryChainConfigurator` — direct whiteboard `bindScopeService`.

## Production consumers

| Consumer | Methods used | Classification | Migratable to `ReadOnlyScopeService`? |
|---|---|---|---|
| `ObjectRegistryResource` | upload/update/delete/transition + getMetadata*/getContent/listIn*/listAll | **Mixed (write)** | No — CRUD endpoint; full workflow surface is intentional. Stays on `WritableScopeService`. |
| `SchemaPackagesResource` | upload/update/delete/transition + getMetadata*/getContent/listIn*/listAll + getScope | **Mixed (write)** | No — same. Stays writable. |
| `ValidationServiceImpl` | `getContentFromStageForRegistry` (final stage), `getScope` | **Read-only** | **MIGRATED (P4-6).** See below. |
| `DataGenResource` | `getContentFromStageForRegistry`, `listInStageForRegistryByName` | **Read-only** | **MIGRATED (post-P4-6).** Originally "not as-is" (used a by-name read absent from the surface); resolved by switching to id-based lookup (`get`) — see "DataGen migration" below. |
| `ScopesHealthCheck` | `getScope` | **Read-only (util)** | No — `getScope()` returns the workflow `Scope` type, absent from `ReadOnlyScopeService`. |
| `ModelAtlasRequestFilter` | `isValidRegistry` | **Read-only (util)** | No — `isValidRegistry` not on the read surface. |
| `SchemaRegistryChainConfigurator` | `getScope` ×4 | **Read-only (util)** | No — and it's workflow-bundle infrastructure, so keeping workflow types is fine. |
| `OpenApiResource` | *(none — `collector.getAllScopes()`)* | Collector-only | N/A — depends on `ScopeServiceCollector`, not `ScopeService`. |
| `ScopesResource` | *(none — `collector.getAllScopes()` / `getScopeByName()`)* | Collector-only | N/A — same. |

## Key finding for P4-6

**No read-only consumer can migrate as-is.** The `ReadOnlyScopeService` surface shares
**zero method names** with what consumers actually call (`getContentFromStageForRegistry`,
`getScope`, `isValidRegistry`, `listInStageForRegistryByName`). The read contract is
currently too thin to absorb even the validation consumer without re-expression and/or an
addition.

For the **validation** target (`ValidationServiceImpl.resolveConstraintSet`, lines 379-394):

1. `getContentFromStageForRegistry(coclRegistry, finalStage, oclId)` → maps cleanly to
   **`ReadOnlyScopeService.get(coclRegistry, oclId)`** — validation already reads the
   *final* stage, and `get()` resolves the final stage internally, so the manual
   `finalStage` computation disappears. ✅ migratable.
2. `getScope()` is used **only to discover the COCL registry by `RegistryType`**. That is
   the real blocker — `ReadOnlyScopeService` has no registry enumeration. P4-6 must resolve
   it one of three ways:
   - (a) add a registry-discovery method to the read contract,
   - (b) supply the COCL registry name to validation via config / constant, or
   - (c) keep validation on `WritableScopeService` solely for discovery (defeats the migration).

So **P4-6's actual scope = "give validation a workflow-free way to find the COCL registry
name,"** after which the content read is a trivial `get()`.

## DataGen migration (post-P4-6)

`DataGenResource` was the other genuine read-only consumer. The audit had flagged it "not
as-is" because, beyond a final-stage content read, it used `listInStageForRegistryByName` — a
**by-name** lookup returning `ObjectMetadata`, with no equivalent on the read surface (no name
filter, no metadata). Two ways out: extend the contract with a by-name read, or change the
endpoint to look up by id. **We chose id-based lookup** to keep `scope.api` minimal (and avoid
forcing the Phase-5 remote mirror to implement a name query).

Migrated like validation: `@Reference ReadOnlyScopeCollector` + `ReadOnlyScopeService<?>`, and
the `GET /datagen/{objectId}` endpoint now resolves the config via
`scopeService.get(DATA_GEN_REGISTRY_NAME, objectId)`. `bnd.bnd` dropped **both** `workflow` and
`management` (the latter was only present for `ObjectMetadata`), adding `scope.api` +
`readonlyscope.collector`. So `datagen.rest` is now workflow-free, same as validation.

### ⚠️ Behavior changes (intentional, the accepted trade-off)

The endpoint is public, so these are observable and belong in the changelog:

1. **Path value is now an object id, not a config name.** URL shape is unchanged
   (`/datagen/{…}`), but a caller that passed a config *name* must now pass the object *id*.
   (The path param / method were renamed `configName` → `objectId` / `generateByConfigName` →
   `generateByObjectId`; the by-name list — including the "first match" fallback — is gone.)
2. **The `?version=` query param was removed.** Version selection needs `ObjectMetadata`, which
   the read contract deliberately omits; `get()` returns the resolved final-stage content.
3. **Reads the registry's final stage** (via `get()`) instead of the hard-coded `"release"`
   stage. Equivalent **iff** `release` is the `DataGen` registry's final stage — otherwise it now
   follows whatever the final stage is. *(Not independently verified against the DataGen registry
   config; confirm before relying on equivalence.)*

`getContentFromStageForRegistry(reg, "release", id)` → `get(reg, id)` is the same clean mapping
as validation (final-stage content). Only the by-name/version metadata path was dropped.

## Test consumers (relevant to P4-8, not P4-6)

- **Exercise the write surface** (keep on `ScopeService` / `Writable`):
  `ScopeServiceIntegrationTest`, `AtlasScopeServiceTest`, `TestHelper`.
- **Read-only exercises:** `AtlasScopeServiceIntegrationTest`, `ModelAtlasExceptionMapperTest`.
- **Type-reference-only** (annotations / fixtures, no method calls): the various
  `TestAnnotations`, `AbstractRestTest`, `TestScopeServiceCollector`, and the resource tests.

## Acceptance (P4-5)

- [x] Every `ScopeService<?>` consumer listed with read-only / writable / mixed classification.
- [x] Mixed-usage consumers (`ObjectRegistryResource`, `SchemaPackagesResource`) documented as
      intentional workflow dependencies (CRUD endpoints).
- [x] P4-6 blocker identified: validation needs a workflow-free COCL-registry-discovery path;
      its content read already maps to `ReadOnlyScopeService.get`.
