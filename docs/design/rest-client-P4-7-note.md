# P4-7 — deprecate `ScopeService` as the `WritableScopeService` alias (as built)

Implementation note for ticket **P4-7** (see `rest-client-tickets.md`). Branch
`issue#133_atlas_client_phase4`. Continues `rest-client-P4-1…P4-6` notes.

## Goal

Collapse `ScopeService<T>` into a thin, deprecated alias of `WritableScopeService<T>` so the
type hierarchy is `ScopeService → WritableScopeService → ReadOnlyScopeService`, and existing
`ScopeService<?>` consumers keep compiling while being steered toward the split interfaces.

## What landed

### `ScopeService` becomes an empty deprecated alias

`workflow-api.ecore`: `ScopeService` lost all its (duplicated) operations and gained
`eGenericSuperTypes → WritableScopeService<T>`. The generic supertype's type argument is
**explicitly bound** — `<eTypeArguments eTypeParameter="#//ScopeService/T"/>` — avoiding the
empty-`<eTypeArguments/>` → `extends WritableScopeService<?>` wildcard compile error that bit
P4-2. The generated interface is now just:

```java
@Deprecated
@ProviderType
public interface ScopeService<T extends EObject> extends WritableScopeService<T> {}
```

Deprecation is done **properly**, not as prose:
- `@deprecated Use {@link WritableScopeService} instead.` *tag* in the model documentation →
  javadoc-level deprecation (regen-safe, sourced from the ecore).
- `@Deprecated` *annotation* on the interface (so the compiler warns consumers). EMF does not
  emit `@Deprecated` from a documentation detail, so this is a `@generated NOT`-style hand-add
  on a one-line interface (low regen risk).

### `AtlasScopeService` now implements the full read surface

Making `ScopeService extends WritableScopeService extends ReadOnlyScopeService` **forced the
deferred decision** (flagged since P4-1/P4-2): `AtlasScopeService implements ScopeService<EPackage>`
must now implement every `ReadOnlyScopeService` method. Root-atlas answers them against its
schema registry (`atlasSchemaRegistryService`):

- `getScopeName()` → `WorkflowConstants.ATLAS_SCOPE_NAME`.
- `isInheritingFromParentScope()` → `false` (atlas is the root; no parent).
- `getScopeInfo()` → `scopeObject` (the atlas `Scope`, which `extends ScopeInfo`).
- `get(registry, objectId)` → `validateRegistry(registry)` then
  `Optional.ofNullable(atlasSchemaRegistryService.getContentFromFinalStage(ATLAS_SCOPE_NAME, objectId))`.
- `listObjectIds(registry)` → final-stage metadata mapped to `objectId`.
- `listAll(registry)` → final-stage metadata resolved via `getContentFromFinalStage`.
- `stream(registry)` → `listAll(registry).stream()`.

## Review fixes folded in

- **`get()` contract bug** — initially used `Optional.of(...)`, which NPEs when the object is
  absent, violating `ReadOnlyScopeService.get`'s "empty if not visible" contract (and diverging
  from `ScopeServiceImpl.get`, which uses `ofNullable`). It would also have bypassed validation's
  restored `IllegalArgumentException` (the `.orElse(null)` path). Fixed to `Optional.ofNullable`.
- **Final-stage consistency** — `get()` originally read stage-explicit
  (`getContentFromStage(…, ATLAS_SCHEMA_REGISTRY_STAGE_NAME, …)`) while `listAll()` used
  `getContentFromFinalStage`. Aligned `get()` onto `getContentFromFinalStage` to match the
  final-stage read contract and the sibling methods.
- **Deprecation strength** — the first cut had only model-doc prose ("Deprecated. Use …"),
  which emits neither a `@Deprecated` annotation nor a `@deprecated` javadoc tag, so no consumer
  would be warned. Replaced with the real tag + annotation (above).

## Status

`workflow` compiles green. `ScopeServiceImpl` is unchanged — its explicit
`implements ScopeService, WritableScopeService, ReadOnlyScopeService` remains valid (and
harmlessly redundant now that the three are a chain).

## Acceptance (P4-7)

- [x] `ScopeService<T>` is an empty interface `extends WritableScopeService<T>`, no own operations.
- [x] Marked `@Deprecated` (annotation) + `@deprecated` javadoc tag pointing at `WritableScopeService`.
- [x] Generic supertype type-argument explicitly bound (no wildcard compile error).
- [x] `AtlasScopeService` implements the `ReadOnlyScopeService` read surface (the deferred decision, now resolved).
- [x] `get()` honours the empty-on-missing contract (`Optional.ofNullable`) and reads the final stage.
- [x] Full `workflow` build green; existing `ScopeService<?>` consumers still compile (now with deprecation warnings).

## Next

P4-8 — tests covering both publications: `ReadOnlyScopeService` + `WritableScopeService` lookups
resolve to the same `ScopeServiceImpl`; the new `getScopeInfo()`/`RegistryInfo`/`get()` read path
(incl. `AtlasScopeService`'s read methods and the empty-on-missing behaviour); and a
`scope.name`↔`atlas.scope` config-parity test (see P4-4).
