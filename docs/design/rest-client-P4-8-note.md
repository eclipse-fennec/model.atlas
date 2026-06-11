# P4-8 — tests covering both service publications (as built)

Implementation note for ticket **P4-8**, the close-out of Phase 4. Branch
`issue#133_atlas_client_phase4`. Continues `rest-client-P4-1…P4-7` notes.

## Goal

Lock in the read/write split with tests: a `ScopeServiceImpl` must be reachable under **both**
the read (`ReadOnlyScopeService`) and write (`WritableScopeService`) shapes; the new read path
(`getScopeInfo()` → `RegistryInfo` → `get()`) must work against the final stage; and the
`ReadOnlyScopeCollector` (keyed on `atlas.scope`) must collect a live scope.

## Where the tests live

All in **`org.eclipse.fennec.model.atlas.workflow.tests`** — no new bundle. It already has the
OSGi-test harness, depends on `workflow` (where `ScopeServiceImpl`/`AtlasScopeService` live), and
carries `scope.api` on its buildpath. One buildpath/dependson addition was needed:
`org.eclipse.fennec.model.atlas.readonlyscope.collector` (for the collector-resolution test).

The collector's own bind/unbind unit logic is already covered by `ReadOnlyScopeCollectorTest`
in the `readonlyscope.collector` bundle (P4-4).

## What landed

### `ReadOnlyScopeServiceIntegrationTest` (new)

Uses `@ScopeServiceSetup` (a real configured scope `test-scope` with a `schema` registry; the
setup carries `atlas.scope`):

- **Dual-Shape Publication** — injects the same scope by filter `(scope.name=test-scope)` as
  `ScopeService`, `WritableScopeService`, and `ReadOnlyScopeService`; asserts all three resolve
  and are the **same instance** (DS registers one component under all three interfaces).
- **Read Path** — uploads an `EPackage` to the final (`release`) stage via the writable surface,
  then through the read contract: `getScopeInfo()` → filter `registries` by `RegistryType.SCHEMA`
  → `get(registry, objectId)` resolves the content; `listObjectIds` includes it. A second test
  asserts `get()` on a missing id returns `Optional.empty()` (not NPE).
- **Collector** — injects `ReadOnlyScopeCollector`, waits for the scope component, then asserts
  the collector bound it under its `atlas.scope` key (`getScopeServiceByScopeName` /
  `getAllScopeNames`). Whiteboard binding is asynchronous, so a short bounded poll
  (`awaitCollected`, 5 s) avoids a race.

### `AtlasScopeServiceIntegrationTest` (extended)

New `ReadOnly Surface Tests` nested class for root-atlas:

- `getScopeName()` == `atlas`, `isInheritingFromParentScope()` == `false`.
- `getScopeInfo()` describes the atlas scope and exposes its schema registry by `RegistryType.SCHEMA`.
- `get(schemaRegistry, "does-not-exist")` returns `Optional.empty()` — guards the
  `Optional.ofNullable` fix from P4-7 (was `Optional.of`, which NPE'd).
- `listObjectIds` is never null and is registry-validated (throws for an invalid registry).

## Config parity, done behaviorally (not as a static scan)

The P4-4 `scope.name`↔`atlas.scope` sync hazard is covered by the **collector-resolution
test**: the collector only sees a scope when `atlas.scope` is present on its publication
(`TestAnnotations.ScopeServiceSetup` carries it). That is a more meaningful guard than a static
glob over the `runtime.config.*` JSON files (which aren't on the test bundle's classpath anyway),
and the runtime bind-time `severe` log backstops a forgotten production config block. So the
brittle file-scan test was deliberately skipped.

## Result

`./gradlew :…workflow.tests:testOSGi` → **95 tests, 0 failures, 0 errors, 0 skipped**. The four
new `ReadOnlyScopeServiceIntegrationTest` cases and the four atlas `ReadOnly Surface Tests` all pass.

## Acceptance (P4-8)

- [x] `ScopeServiceImpl` resolves under `ReadOnlyScopeService` + `WritableScopeService` (same instance).
- [x] Read path (`getScopeInfo`/`RegistryInfo`/`get`) exercised against the final stage, incl. empty-on-missing.
- [x] `AtlasScopeService` read surface exercised (incl. the `Optional.ofNullable` empty-on-missing guard).
- [x] `ReadOnlyScopeCollector` collects a live scope by `atlas.scope` (config-propagation verified end-to-end).
- [x] Full `workflow.tests` OSGi suite green.

## Phase 4 status

**Complete.** P4-1 (scope.api) → P4-2 (Read/Writable split) → P4-3 (ScopeServiceImpl) →
P4-4 (ReadOnlyScopeCollector + atlas.scope) → P4-5 (consumer audit) → P4-6 (validation migrated,
workflow-free; ScopeInfo/RegistryInfo read model) → P4-7 (ScopeService deprecated alias) →
P4-8 (tests). Sets up the Phase-5 remote EObject-registry client, which mirrors `scope.api` and
publishes `ReadOnlyScopeService` with `atlas.scope`/`atlas.remote=true`.
