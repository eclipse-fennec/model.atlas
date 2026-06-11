# P4-2 — read/write split: `ReadOnlyScopeService` + `WritableScopeService` (and the P4-3 plan)

Implementation note for **P4-2** (and the design pivot that reshaped P4-1). Branch
`issue#133_atlas_client_phase4`. See also `rest-client-P4-1-implementation-note.md`.

## Design pivot — per-scope, not per-(scope, registry)

The design doc modelled the read contract as a per-(scope, registry)
`ScopedEObjectsRegistry`. We rejected that: it would force a granularity shift (the
registry moves from a per-call argument to a per-instance identity) and a ~72-call-site
consumer refactor, and the doc was internally inconsistent (it also called
`WritableScopeService` "per scope" while having it extend the per-(scope,registry) type).

Decision: **the read contract is per-scope with the registry as a method parameter**,
mirroring today's `ScopeService`. This dissolves the granularity tension —
`WritableScopeService<T> extends ReadOnlyScopeService<T>` is trivial same-granularity
inheritance — keeps `ScopeServiceImpl` and the ~72 consumers on their existing
registry-param calls, and still delivers Phase 4's actual goal: **read consumers depend
on a read-only interface, free of the workflow/write surface and of `ObjectMetadata` /
`Promise` / `management`.** What per-(scope,registry) would have bought (a meaningful
generic `<T>` + `getRootEClass`, registry-focused OSGi injection) is negligible here —
`ScopeService` is always used as `<?>`.

## `ReadOnlyScopeService<T extends EObject>` (in `scope.api`)

Renamed from `ScopedEObjectsRegistry`. Per-scope, final-stage, `ObjectMetadata`-free:

- `String getScopeName()`
- `boolean isInheritingFromParentScope()`
- `Optional<T> get(String registry, String objectId)`
- `List<String> listObjectIds(String registry)`
- `List<T> listAll(String registry)`
- `Stream<T> stream(String registry)`

Dropped `getRegistryName()`/`getRootEClass()`. `Optional`/`List`/`Stream` are local
`java.util.*` EDataTypes in `scope-api.ecore` (not borrowed from `management.ecore`, to
keep `scope.api` — and the Phase-5 client mirror — free of a `management` dependency).
Generated with `oSGiCompatible="false"` into package `org.eclipse.fennec.model.atlas.scope.api`.

## `WritableScopeService<T>` (in `workflow-api`)

A copy of `ScopeService`'s operations (upload/update/delete/transition, the stage-explicit
and `ObjectMetadata`-returning reads, `isValidRegistry`/`getAllRegistries`/`getScope`) plus
`eGenericSuperTypes → ReadOnlyScopeService<T>`. `workflow-api.genmodel` gained the
`usedGenPackages` entry for `scope-api.genmodel` (+ genDataTypes + the ReadOnlyScopeService
genClass). `ScopeService` is **not yet touched** — it becomes the deprecated alias
(`extends WritableScopeService<T>`) in P4-7.

### EMF gotchas hit (all resolved except #3, which is benign)

1. **Wildcard supertype** — an empty `<eTypeArguments/>` on the generic supertype generated
   `extends ReadOnlyScopeService<?>` ("a supertype may not specify any wildcard"). Fix: bind
   it — `<eTypeArguments eTypeParameter="#//WritableScopeService/T"/>`.
2. **Type parameter not in scope** — the copied operations referenced
   `#//EObjectWorkflowService/T` (another class's `T`). Fix: repointed the 3 refs inside the
   `WritableScopeService` block to `#//WritableScopeService/T` (scoped edit, old classes left
   alone).
3. **Non-fatal genmodel warning** — "a generic type may only refer to a type parameter that
   is in scope" still shows for the OLD classes (`ScopeService`/`RegistryService`/
   `EObjectWorkflowService`), which all carry the same `#//EObjectWorkflowService/T` cross-ref.
   EMF generates type vars by name, so the Java compiles. We deliberately did not churn the
   old classes. **Generation + compile succeed despite this warning.**

## P4-3 — wire `ScopeServiceImpl` (IN PROGRESS, resume here)

Prerequisite: `RegistryService` has no final-stage *content* method (only `getContentFromStage`,
`getMetadataFromFinalStage`, `listInFinalStage`), so add the building block:

1. **Model `getContentFromFinalStage(scope, objectId): T` on `RegistryService`** (ecore EOperation,
   return bound to `#//RegistryService/T`, params `scope:EString` + `objectId:EString` lowerBound=1)
   → regenerate.
2. **Impl** (verbatim from commit `7bc8614`):
   - `RegistryServiceImpl`: `finalStage = stages.stream().filter(Stage::isFinal).findFirst().get(); return getContentFromStage(scope, finalStage.getName(), objectId);`
   - `AtlasSchemaRegistryService`: the staticPackageRegistry/base64 body.
3. **`ScopeServiceImpl`**: add `service = { ReadOnlyScopeService.class, WritableScopeService.class, ScopeService.class }`
   to `@Component`; `implements ScopeService<T>, WritableScopeService<T>`. Add a **private**
   `getContentFromFinalStageForRegistry(registry, objectId)` helper (hierarchical lookup,
   verbatim from `7bc8614`: this scope → `atlasSchemaRegistryService` when parent==atlas & SCHEMA
   → parent registry otherwise; `(T)` cast on the atlas branch). Implement the six read methods:
   `getScopeName`→`config.scope_name()`; `isInheritingFromParentScope`→ parent set & non-blank;
   `get`→`Optional.ofNullable(helper)`; `listObjectIds`→`listInFinalStageForRegistry(registry)`
   mapped to `objectId` (already inheritance-aware); `listAll`→ ids→`get` per id; `stream`→
   `listAll(...).stream()`. Keep the helper private (don't re-model it) to localise the change.
4. **`AtlasScopeService`** (root-atlas impl, `implements ScopeService<EPackage>`): DECISION DEFERRED —
   whether to also expose it as `ReadOnly`/`WritableScopeService`. Not needed for inheritance
   (children read through to atlas). Flagged, not silently skipped.

The exact code is in commit **`7bc8614`** ("added getContentFromFinalStage and hierarchical lookup
for content", reachable via `git show 7bc8614`) — it lives on the client branch; we replicate only
the `RegistryService` building block + impl bodies, keeping `getContentFromFinalStageForRegistry`
private here.

## Branch strategy

Phase 4 is based on jena/main, **not** the client-impl branch (to keep the client changes in their
own reviewable PR). We do **not** rebase onto the client feature branch (would pull unreviewed
changes + couple two unmerged branches). When the client branch lands in **main**, rebase Phase 4
onto main and reconcile the duplicate `getContentFromFinalStage` (trivial) — conflicts in
`workflow-api.ecore` + `ScopeServiceImpl` are inevitable either way.
