# P4-6 — migrate validation to `ReadOnlyScopeService` (as built)

Implementation note for ticket **P4-6** (see `rest-client-tickets.md`; audit in
`rest-client-P4-5-audit.md`). Branch `issue#133_atlas_client_phase4`. Continues the
`scope.api` work in `rest-client-P4-1/2/3-implementation-note.md` and the P4-4
`ReadOnlyScopeCollector`.

## Goal

Make `ValidationServiceImpl` — the canonical "pure consumer of published models" — depend
only on the read contract, with **no `workflow` bundle dependency**. The P4-5 audit found
the sole blocker was registry discovery: validation used `ScopeService.getScope()` (a
workflow type) only to find the COCL registry by `RegistryType`, then read its final-stage
content.

## Approach — separate read-only DTOs (not "rich extends slim" containment)

The design options weighed in P4-5 were (A) move `Scope`/`Registry`/`Stage` wholesale, or
(B) a model-level read/write split. We took a clean variant of B: define **independent
read-only descriptors** in `scope.api` rather than reuse the workflow model shape.

`scope-api.ecore` gained:

- **`ScopeInfo`** — `name`, `description`, `parentScope`, `registries: RegistryInfo` (containment).
- **`RegistryInfo`** — `name`, `description`, `type: RegistryType`. **No** stages, **no** transitions
  (those stay a workflow concern, honouring the P4-1 "no per-stage detail in the read contract" decision).
- **`RegistryType`** — the enum, **moved** out of `workflow-api` (deleted there) with literal parity
  preserved: `OTHER=0`, `SCHEMA=1`, `COCL=2`.
- **`ReadOnlyScopeService.getScopeInfo(): ScopeInfo`** — the new read-side scope descriptor accessor.

`workflow-api.ecore`:

- `Scope` now `extends ScopeInfo`; `Registry` now `extends RegistryInfo` (adding `stages` +
  `allowedTransitions`). `Stage`/`StageTransition` remain in `workflow-api`.
- `Registry.type` repoints at the `scope.api` `RegistryType`; the `usedGenPackages` link to
  `scope-api.genmodel` (already present from P4-2) was extended.
- Because `Scope extends ScopeInfo`, `ScopeServiceImpl.getScopeInfo()` is simply
  `return scopeObject` — no mapping, the workflow `Scope` *is* a `ScopeInfo`.

## The containment-covariance gotcha (predicted, hit, handled)

`ScopeInfo.registries` is typed `EList<RegistryInfo>`, and **EMF cannot narrow an inherited
containment reference's element type in a subclass**. So even on a workflow `Scope`,
`getRegistries()` yields `RegistryInfo` elements — code wanting `Registry.getStages()` must
downcast. Resolved with `instanceof Registry` pattern checks (`SchemaRegistryChainConfigurator`,
2 sites). This is safe **only because** `ScopeServiceImpl.createScopeObject()` and
`AtlasScopeService` populate the containment list with **rich `Registry` instances**, so the
downcast always succeeds at runtime. Read-only consumers (validation) see the `RegistryInfo`
view and never downcast.

## `ValidationServiceImpl` changes

- `@Reference ScopeServiceCollector` → `@Reference ReadOnlyScopeCollector`.
- `ScopeService<?>` → `ReadOnlyScopeService<?>` throughout.
- `resolveConstraintSet`: `getScopeInfo()` → filter `registries` by `RegistryType.COCL` → name →
  **`get(coclRegistryName, oclId)`**. The manual final-stage lookup is gone — `ReadOnlyScopeService.get()`
  resolves the final stage internally, so validation no longer needs `Stage` at all (confirming
  the slim `RegistryInfo` is sufficient).
- **`bnd.bnd`: dropped `org.eclipse.fennec.model.atlas.workflow`**, added
  `org.eclipse.fennec.model.atlas.scope.api` + `org.eclipse.fennec.model.atlas.readonlyscope.collector`.
  This is the concrete P4-6 deliverable: validation is workflow-free.

## Review fixes folded in

- **Error-handling regression** — an interim `if(optional.isEmpty()) return null;` made
  `resolveConstraintSet` return `null` for a missing OCL id; no caller null-checks it, so it
  would NPE downstream instead of the prior clear `IllegalArgumentException`. Fixed to
  `scopeService.get(...).orElse(null)` and let the existing `instanceof` check throw.
- Removed the now-unused `java.util.Optional` import.
- Removed a dead `parentSchemaRegistry == null` check (unreachable after `instanceof`+cast) in
  `SchemaRegistryChainConfigurator`.
- Corrected the `getScopeInfo()` javadoc (copy-pasted from `getScopeName`) in **both**
  `scope-api.ecore` and the generated `src-gen` source. NOTE: the bnd `-generate` task keys off
  the **genmodel**, so an ecore-doc-only edit is skipped UP-TO-DATE — hand-edit `src-gen` (or
  touch the genmodel) to regenerate.

## Status

`scope.api` + `workflow` + `validation` build green. `getScopeName()` is kept on the read
contract alongside `getScopeInfo()` (mildly redundant, still referenced, harmless).

## Acceptance (P4-6)

- [x] Validation depends on `ReadOnlyScopeService` / `ReadOnlyScopeCollector`, not `ScopeService`.
- [x] Validation `bnd.bnd` has no `workflow` dependency.
- [x] Final-stage content read via `ReadOnlyScopeService.get`; no `Stage`/workflow types in validation.
- [x] `RegistryType` moved to `scope.api` with literal parity; all importers repointed; full build green.

## Next

P4-7 (deprecate `ScopeService` as the alias `extends WritableScopeService<T>`),
P4-8 (tests for both publications, plus a `scope.name`↔`atlas.scope` config-parity test and
coverage of the new `ScopeInfo`/`RegistryInfo` read path).
