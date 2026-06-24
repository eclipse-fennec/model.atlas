# P6-4 — Client: stage-bound registry view over stage-explicit URLs (execution plan)

**Ticket:** P6-4 (depends on P6-3 ✓). Gates P6-5 (stage-aware drift).

Implements the `registryView(...)` factory the P6-3 stubs throw from, and adds **stage** to the
object cache key so a draft fetch and a final fetch of the same id never collide. This is a
**client-impl-only** ticket — no model/regen, no server change (the stage-explicit endpoints
already exist).

## Decisions baked in / to confirm

- **One impl class, both modes.** A single view class handles the final-stage and explicit-stage
  cases (matching the P6-3 "one type" decision); the bound stage (`null` = final) selects the URL
  shape. **Naming:** I'll call it **`RemoteReadableRegistryView`** (it implements
  `ReadableRegistryView<EObject>`), not the ticket-table's older `RemoteStagedRegistryView` — that
  name predates the one-type decision. Say if you'd rather keep `RemoteStagedRegistryView`.
- **`null` stage = final.** In the cache key and in `getStageName()`, `null` means "final stage,
  stage-free URL." Mirrors the P6-3 `getStageName()` contract.
- **Shared cache.** The view reuses the owning `RemoteReadableScopeService`'s cache (does not own
  its own), so final and staged entries coexist keyed by stage — and so P6-5 drift, which inspects
  that one cache, can later revalidate staged entries too.
- **Drift stays final-only in P6-4** (documented gap, closed by P6-5). See Step 4.

## Server endpoints (already exist)

| mode | content (get) | listing |
|---|---|---|
| final (stage = `null`) | `GET /{s}/registries/{r}/content?objectId=` | `GET /{s}/registries/{r}` |
| explicit stage | `GET /{s}/registries/{r}/stages/{stage}/content?objectId=` | `GET /{s}/registries/{r}/stages/{stage}` |

So the only URL delta is an optional `…/stages/{stage}` segment between the registry and
`content`/listing.

---

## Step 1 — Stage in the cache key (`RemoteReadableScopeService.ObjectKey`)

`record ObjectKey(String scope, String registry, String objectId)` →
`record ObjectKey(String scope, String registry, String stage, String objectId)`.

- `stage == null` ⇒ final stage. `record` `equals`/`hashCode` handle `null` fine, so a final entry
  `(jena, cocl, null, id1)` and a draft entry `(jena, cocl, draft, id1)` are distinct keys — the
  collision-avoidance the ticket calls for.
- Update the `cachedObjects()` Javadoc to `(scope, registry, stage, objectId)`.

## Step 2 — Parameterize the read core by stage

Today `get`/`listObjectIds`/`revalidateOrFetch`/`registryTarget` all hard-assume the stage-free
final URLs. Thread an optional `stage` through (`null` = final):

- `private WebTarget registryTarget(String registry, String stage)` — appends `.path(STAGES).path(stage)`
  when `stage != null`. Add `static final String STAGES = "stages";`. Keep a `registryTarget(registry)`
  = `registryTarget(registry, null)` convenience if handy.
- `private Optional<EObject> get(String registry, String stage, String objectId)` — the body of
  today's `get`, keyed on `new ObjectKey(scopeName, registry, stage, objectId)`.
- `private List<String> listObjectIds(String registry, String stage)` — today's body using
  `registryTarget(registry, stage)`.
- `revalidateOrFetch(ObjectKey key)` — build the target with `registryTarget(key.registry(), key.stage())`.
- `loadEObject` is stage-agnostic — unchanged.

**Public `ReadableScopeService` methods stay final-stage and delegate:**
`get(registry, objectId)` → `get(registry, null, objectId)`;
`listObjectIds(registry)` → `listObjectIds(registry, null)`;
`listAll`/`stream` unchanged (built on the above). Signatures and behaviour for existing callers are
unchanged.

`refresh(registry, objectId)` (drift seam) stays final-stage: `revalidateOrFetch(new ObjectKey(scopeName, registry, null, objectId))`.

The **SCHEMA-registry guard** (`assertNotSchemaRegistry`) is called from the stage-aware `get`/`listObjectIds`,
so it applies at every stage automatically.

## Step 3 — The view class + factory

`RemoteReadableRegistryView implements ReadableRegistryView<EObject>`, bound to one
`(registry, stage)` and delegating to the owning service's stage-aware core (so the cache is shared):

```java
final class RemoteReadableRegistryView implements ReadableRegistryView<EObject> {
    private final RemoteReadableScopeService service;
    private final String registry;
    private final String stage;            // null = final
    // ctor assigns; registry non-null

    public Optional<EObject> get(String objectId)  { return service.get(registry, stage, objectId); }
    public List<String>     listObjectIds()        { return service.listObjectIds(registry, stage); }
    public List<EObject>    listAll()              { /* loop get over listObjectIds, like the service */ }
    public Stream<EObject>  stream()               { return listAll().stream(); }
    public String getScopeName()    { return service.getScopeName(); }
    public String getRegistryName() { return registry; }
    public String getStageName()    { return stage; }   // null when final
}
```

(Inner/nested in `RemoteReadableScopeService`, or a package-private class taking the service — pick
whichever keeps the stage-aware core methods package-visible. Inner is simplest.)

**Replace the P6-3 stubs:**

```java
@Override public ReadableRegistryView<EObject> registryView(String registry) {
    return new RemoteReadableRegistryView(this, registry, null);          // final
}
@Override public ReadableRegistryView<EObject> registryView(String registry, String stage) {
    Objects.requireNonNull(stage, "stage");
    return new RemoteReadableRegistryView(this, registry, stage);         // explicit
}
```

(`registry` non-null in both; the explicit overload also requires `stage` non-null — a caller
wanting final uses the single-arg overload.)

## Step 4 — DriftWatcher: keep it compiling + final-stage drift working

`handleChangedObjects` reconstructs an `ObjectKey` from the `Atlas-Changed-Objects` header
(`registry/objectId` pairs) — that header is the **final-stage** drift signal. Update the one
constructor call to the 4-arg key with `null` stage:

```java
if (!held.contains(new RemoteReadableScopeService.ObjectKey(scope, registry, null, objectId))) {
    continue;
}
```

Effect: a final-stage drift notification matches and refreshes only final entries
(`stage == null`); staged entries are **not** revalidated by drift in P6-4. That's the intended
P6-4/P6-5 boundary — **`log`/comment it** so the gap is explicit, and note it in the README. P6-5
makes the header (and this reconstruction) stage-aware.

## Step 5 — Tests (`RemoteReadableScopeServiceTest`, + maybe a focused view test)

Replace the two `registryView_*_throwsUntilP6_4` boundary tests with real coverage:

1. **Final view URL shape** — `registryView("cocl").get("id1")` hits
   `…/cocl/content?objectId=id1` (no `stages` segment); `listObjectIds()` hits `…/cocl`.
2. **Explicit-stage URL shape** — `registryView("cocl","snapshot").get("id1")` hits
   `…/cocl/stages/snapshot/content?objectId=id1`; `listObjectIds()` hits `…/cocl/stages/snapshot`.
   Assert the captured `path(...)` segments include `stages` + `snapshot`.
3. **Cache isolation between stages (the key correctness test)** — fetch the same `objectId` from
   the final view and the snapshot view; assert **two** server GETs and **distinct** instances
   (no aliasing). Conversely, a repeat read on the same view is a cache hit (one GET).
4. **`getStageName()`** — `null` for the final view, `"snapshot"` for the explicit view; plus
   `getScopeName()`/`getRegistryName()`.
5. **SCHEMA guard via the view** — `registryView("schema").get(...)` throws the same
   `ModelAtlasClientException` (guard applies at every stage).

Reuse the existing mock harness (the fluent `WebTarget` returns itself; `ArgumentCaptor` on
`path(...)`). The `xmi(thing())` helper supplies bodies.

A small `DriftWatcherTest` check is optional: a final-stage drift notification still refreshes a
final entry after the key change (regression guard for Step 4).

## Step 6 — Verify

```bash
cd /opt/git/model.atlas
./gradlew :org.eclipse.fennec.model.atlas.rest.client.impl:test \
          :org.eclipse.fennec.model.atlas.rest.client.osgi:compileJava
```

(`rest.client.osgi` compile guards against any ripple from the `ObjectKey`/method-shape changes
through the OSGi front-end.)

**Done when:** `registryView(registry)` / `registryView(registry, stage)` return working views over
the correct (stage-free vs stage-explicit) URLs; `ObjectKey` carries stage so final/staged entries
don't collide; existing final-stage reads + drift behave exactly as before; tests green. Unblocks
P6-5 (stage-aware drift) and P6-7 (`atlas.stage` publication).
