# P6-3 — `ReadableRegistryView` + `registryView(registry[, stage])` factory (execution plan)

**Ticket:** P6-3 (depends on P6-2 ✓). Gates P6-4 (client impl over stage-explicit URLs).

Adds the **registry-bound read vocabulary** to `scope.api` as contracts only — no behaviour yet.
Like P6-1/P6-2 the heart is an ecore/genmodel edit + regen (you do the regen); the only
hand-written fallout is throwing stubs in the two implementers.

## Decisions baked in (confirmed)

- **One type.** Only `ReadableRegistryView<T extends EObject>`. There is **no** separate
  `StagedRegistryView` interface — "staged" is merely how a view was obtained
  (`registryView(r, stage)` vs `registryView(r)`); `getStage()` distinguishes them at runtime.
- **`getStage()` returns `null` for a final-stage view** (the `registryView(r)` case). Final stays
  implicit and name-free (D1); no resolve round-trip just to name it. An explicit-stage view
  returns the stage it was bound to.
- **Contracts only.** Implementers get a throwing stub; real impls land in P6-4 (client) and the
  server ticket. Keeps the P6-3/P6-4 boundary clean.

## Model facts this builds on

`ReadableScopeService` is an abstract interface EClass with type param `T` (bounds `EObject`) and
operations whose generic returns reference the local `#//Optional`, `#//List`, `#//Stream`
`EDataType`s (instanceClassName `java.util.*`). `ReadableRegistryView` mirrors that exactly and
**reuses those same three `EDataType`s** — no new datatypes.

---

## Step 1 — `scope-api.ecore` (then you regenerate)

### 1a. New EClass `ReadableRegistryView` (abstract interface, type param `T` bounds `EObject`)

Operations (all mirror `ReadableScopeService`, minus the `registry` parameter since the view is
already bound to one registry):

| operation | return | params |
|---|---|---|
| `get` | `Optional<T>` (`#//Optional` arg → `#//ReadableRegistryView/T`) | `objectId : EString` (lower 1) |
| `listObjectIds` | `List<EString>` | — |
| `listAll` | `List<T>` | — |
| `stream` | `Stream<T>` | — |
| `getScopeName` | `EString` | — |
| `getRegistryName` | `EString` | — |
| `getStage` | `EString` | — |

Doc the contract on the EClass and on `getStage` specifically: *"the bound stage, or `null` for a
final-stage view (no stage was requested)."* Mirror the existing `get`/`listAll`/`stream`
gen-model doc style (`@param`/`@return`, "never `null`" where apt).

### 1b. Two factory operations on `ReadableScopeService`

Both return `ReadableRegistryView<T>` (`eGenericType eClassifier="#//ReadableRegistryView"`,
`eTypeArguments eTypeParameter="#//ReadableScopeService/T"`) — reusing the service's own `T`:

| operation | params | doc |
|---|---|---|
| `registryView` | `registry : EString` (lower 1) | final-stage view of `registry` |
| `registryView` | `registry : EString` (lower 1), `stage : EString` (lower 1) | explicit-stage view; inheritance reads through to the parent hierarchy's final stage server-side |

> EMF allows the two same-named operations (overloads) — they differ in parameter list. Confirm the
> generator emits both; if it balks on the overload, fall back to distinct names
> (`registryView` / `registryViewAtStage`) — but try the overload first, it matches the API shape.

No new `EDataType`s; no other EClass changes.

## Step 2 — `scope-api.genmodel`

Add a `genClasses` entry for `ReadableRegistryView` (with `genTypeParameters` for `T` and
`genOperations`/`genParameters` for each operation), and two `genOperations` under the existing
`ReadableScopeService` `genClass` for the `registryView` overloads — mirroring the existing
`get`/`listObjectIds`/… entries.

## Step 3 — Regenerate `scope.api` `src-gen` only

New: `ReadableRegistryView.java`. Changed: `ReadableScopeService.java` (gains `registryView`),
`ScopeApiPackage(.Impl)`, `ScopeApiSwitch`, `ScopeApiAdapterFactory`. **No workflow regen** — the
new ops are inherited by `WritableScopeService` (no `workflow-api.ecore` change), and EMF does not
re-declare inherited operations in the sub-interface. Commit the regen output.

---

## Step 4 — Stub the two implementers (hand-written)

Adding `registryView(...)` to `ReadableScopeService` makes both concrete implementers
non-compiling until they implement it. Add throwing stubs (real impls in P6-4 / server ticket):

- `…workflow/src/…/impl/ScopeServiceImpl.java` (`implements … ReadableScopeService<T>`)
- `…rest.client.impl/src/…/RemoteReadableScopeService.java` (`implements ReadableScopeService<EObject>`)

```java
@Override
public ReadableRegistryView<T> registryView(String registry) {
    throw new UnsupportedOperationException("registryView not yet implemented (P6-4)");
}

@Override
public ReadableRegistryView<T> registryView(String registry, String stage) {
    throw new UnsupportedOperationException("registryView not yet implemented (P6-4)");
}
```

(Client uses `<EObject>` for the type arg; add the `ReadableRegistryView` import.)

> `AtlasScopeService` only *references* `ReadableScopeService` (field/`@Reference`), it does not
> implement it — no stub there. `WritableScopeService` is an interface; it inherits the ops and
> needs no edit.

## Step 5 — Tests

Light, since there's no behaviour yet:

- `scope.api` smoke: load `ScopeApiPackage.eINSTANCE`, assert `ReadableRegistryView` EClass present
  and `ReadableScopeService` has both `registryView` operations (or just that the package
  initialises — forced already by any factory use).
- Optionally assert the stubs throw `UnsupportedOperationException`, to document the P6-3 boundary
  (will be replaced in P6-4).

Real `get`/`list`/`stream`/stage-URL behaviour is **P6-4** (client) + the server ticket.

## Step 6 — Verify

```bash
cd /opt/git/model.atlas
./gradlew :org.eclipse.fennec.model.atlas.scope.api:build \
          :org.eclipse.fennec.model.atlas.workflow:compileJava \
          :org.eclipse.fennec.model.atlas.rest.client.impl:test
```

**Done when:** `scope.api` exposes `ReadableRegistryView<T>` (get/listObjectIds/listAll/stream +
getScopeName/getRegistryName/getStage) and `ReadableScopeService.registryView(registry[, stage])`;
`scope.api` regenerates; both implementers compile (throwing stubs); build + client tests green.
Unblocks P6-4.
