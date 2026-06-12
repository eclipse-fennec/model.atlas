# P4-3 — wire `ScopeServiceImpl` to the read/write split

Implementation note for **P4-3** (as built). Branch `issue#133_atlas_client_phase4`.
See also `rest-client-P4-1-implementation-note.md` and `rest-client-P4-2-implementation-note.md`
(the latter carries the P4-3 *plan*; this note records what actually landed).

## Goal

Make `ScopeServiceImpl` implement the new read contract so read consumers can depend on
`ReadOnlyScopeService` (P4-6) without pulling in the workflow/write surface, `ObjectMetadata`,
or `Promise`. No consumer churn — the registry stays a method parameter (the per-scope pivot
from P4-2).

## What landed

### 1. New building block — `RegistryService.getContentFromFinalStage(scope, objectId)`

`RegistryService` previously had no final-stage **content** method (only `getContentFromStage`
(stage-explicit), `getMetadataFromFinalStage`, and `listInFinalStage`). Added it as a modeled
EOperation in `workflow-api.ecore` (return bound to `#//RegistryService/T`, params
`scope:EString` + `objectId:EString` `lowerBound=1`) and regenerated — `RegistryService.java`,
`WorkflowApiPackage`, `WorkflowApiPackageImpl`, and the switch/adapter factory.

Impl bodies lifted verbatim from commit `7bc8614`:

- **`RegistryServiceImpl`** — resolve the final stage and delegate:
  ```java
  Stage finalStage = stages.stream().filter(s -> s.isFinal()).findFirst().get();
  return getContentFromStage(scope, finalStage.getName(), objectId);
  ```
- **`AtlasSchemaRegistryService`** — base64-URL-decode the objectId to the original nsURI and
  look it up in the static package registry (null-guarded):
  ```java
  if (staticPackageRegistry != null) {
      byte[] decodedBytes = Base64.getUrlDecoder().decode(objectId);
      String originalNsUri = new String(decodedBytes);
      return staticPackageRegistry.getEPackage(originalNsUri);
  }
  return null;
  ```

### 2. `ScopeServiceImpl implements ScopeService<T>, WritableScopeService<T>, ReadOnlyScopeService<T>`

The six read methods delegate to existing scope plumbing:

- `getScopeName()` → `config.scope_name()`
- `isInheritingFromParentScope()` → `config.scope_parent() != null && !config.scope_parent().isBlank()`
- `get(registry, objectId)` → `Optional.ofNullable(getContentFromFinalStageForRegistry(...))`
- `listObjectIds(registry)` → `listInFinalStageForRegistry(registry)` mapped to `ObjectMetadata::getObjectId` (already inheritance-aware)
- `listAll(registry)` → those ids resolved via `getContentFromFinalStageForRegistry` per id
- `stream(registry)` → `listAll(registry).stream()`

**Config-based, not `scopeObject`-based** (deviation from the verbatim `7bc8614`, decided in
review): `getScopeName`/`isInheritingFromParentScope` read from `config`, which is set in the
constructor. The `registryService` reference is `cardinality = MULTIPLE` (0..n), so the
component can be active with **zero** registries bound — at which point `scopeObject` (created
lazily in `bindRegistryService`) is still `null`. Reading config avoids that NPE window. The
`!isBlank()` guard also protects against a blank-string parent slipping past a bare `!= null`.

### 3. Hierarchical content lookup — DRY-extracted private helper

`7bc8614` copy-pasted the parent-lookup block into both `getContentFromStageForRegistry` and
`getContentFromFinalStageForRegistry`. Here it's factored into one **private** method:

```java
private T getContentFromParentForRegistry(String registry, String objectId) {
    T parentContent = null;
    if (ATLAS_SCOPE_NAME.equals(config.scope_parent())
            && RegistryType.SCHEMA == getRegistryService(registry).getRegistry().getType()) {
        parentContent = (T) atlasSchemaRegistryService.getContentFromFinalStage(config.scope_parent(), objectId);
    } else if (!ATLAS_SCOPE_NAME.equals(config.scope_parent())) {
        parentContent = getRegistryService(registry).getContentFromFinalStage(config.scope_parent(), objectId);
    }
    return parentContent;
}
```

Inheritance rules (unchanged from the metadata methods): parent==atlas & registry is SCHEMA →
read through to `atlasSchemaRegistryService`; parent!=atlas → read the same-named registry in
the parent scope; parent==atlas & non-SCHEMA → no parent lookup. The `(T)` cast lives on the
atlas branch (`EPackage` → `T`). Both `getContentFromStageForRegistry` (now: fall back to the
parent's final stage when the requested stage is null) and the private
`getContentFromFinalStageForRegistry` reuse it.

`getContentFromFinalStageForRegistry` is kept **private** — it is the engine for the public
`ReadOnlyScopeService.get(...)` but is not re-modeled on any interface, localising the change.

## Decisions / deviations from the P4-2 plan

1. **Config over `scopeObject`** for the two zero-arg read methods — NPE-safety (see above).
2. **DRY parent-lookup helper** instead of `7bc8614`'s duplicated blocks — same behavior, one
   copy.
3. **`getContentFromFinalStageForRegistry` stays private** (plan already called for this) — not
   re-modeled on the interface.
4. **No explicit `service = {...}` on `@Component`.** The P4-2 plan suggested listing the three
   interfaces. Skipped because, absent a `service` attribute, DS registers the component under
   all directly-implemented interfaces — and the class implements exactly
   `ScopeService`, `WritableScopeService`, `ReadOnlyScopeService`. Registration is identical.
   (Could be made explicit later for robustness against future `implements` changes / clarity
   for the P4-6 `ReadOnlyScopeService` lookups, but not required.)
5. **Behavior change to `getContentFromStageForRegistry`** — it now falls back to the parent's
   final stage on a null result, matching the metadata methods. Intended; the previous
   no-fallback behavior was wrong. (Also what `7bc8614` did.)
6. **`AtlasScopeService` left as `implements ScopeService<EPackage>` only** — deferred decision
   from the plan. Children read through to atlas already, so it needs neither contract until
   P4-7 turns `ScopeService` into a `WritableScopeService` subtype (deprecated alias).

## Acceptance (P4-3)

- [x] `RegistryService.getContentFromFinalStage` modeled + regenerated; impl in
      `RegistryServiceImpl` + `AtlasSchemaRegistryService`.
- [x] `ScopeServiceImpl` implements all three contracts; six read methods wired.
- [x] Hierarchical parent fallback for content (this scope → atlas schema / parent registry).
- [x] `scope.api;version=snapshot` on the workflow buildpath (`bnd.bnd`).
- [x] `:scope.api:compileJava` + `:workflow:compileJava` build green.

## Next

P4-4 (per-scope collector, keyed by the propagated `scope.name` ConfigAdmin property),
P4-5 (audit ~72 `ScopeService<?>` consumers), P4-6 (migrate validation → `ReadOnlyScopeService<?>`),
P4-7 (`ScopeService` becomes the deprecated alias `extends WritableScopeService<T>`),
P4-8 (tests for both shapes).
