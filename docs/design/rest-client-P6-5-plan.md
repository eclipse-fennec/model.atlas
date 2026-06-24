# P6-5 — Stage-aware drift (execution plan)

**Ticket:** P6-5 (depends on P6-4 ✓, P2-7 drift ✓). **Client-only** — no server change, no wire-format
change.

## The decision and why

Drift must now revalidate the per-stage views P6-4 added, not just the final view. The natural
instinct — qualify the drift token with the changed stage and match it to the cached entry — is
**unsafe under inheritance**: an explicit-stage read (`…/stages/draft/content`) that misses is
served by the parent's *final* stage, so the stage a view was **requested** at is not the stage its
content **came from**. Neither the token's stage nor the cache key's stage reliably connects a
reported change to the entry it affects.

**Resolution: don't match on stage at all — match the object, revalidate every view of it.** The
`Atlas-Changed-Objects` header already identifies a changed object by `registry/objectId` (its
existing 2-part form). For each reported object the client refreshes **every held cache entry for
that `(scope, registry, objectId)`**, each via its own stage URL (which re-resolves inheritance
server-side). The per-entry conditional GET (304 vs 200) decides what actually changed:

- the `draft`-bound entry re-hits `…/stages/draft/content` with its ETag → server re-resolves to
  the parent's final → `200` if that changed, `304` if not;
- the final entry re-hits the stage-free URL → `200`/`304`;
- a view whose content is unchanged → `304`, no event.

This **never misses** (covers final + every staged view, inherited or not) and **never false-fires**
(the 304/200 gate per entry). Cost: a few extra conditional GETs (cheap 304s) when one object backs
several held views — accepted, as agreed. A precise-but-heavier variant (server returns the resolved
origin `(scope, stage)` per `/content` read; client keys/matches on resolved origin) is deferred;
not needed for correctness.

Because the join key is just `registry/objectId`, **the 2-part token is sufficient** — no server or
wire change. P6-5 is entirely in `RemoteReadableScopeService` + `DriftWatcher`.

## Model facts this builds on

- `ClientCache.keys()` returns a fresh `LinkedHashSet` snapshot → safe to iterate while `refresh`
  mutates the cache. `ClientCache.lookup(key)` exposes the entry value.
- `revalidateOrFetch(ObjectKey)` already returns the **same** instance on `304` (re-puts
  `entry.value()`) and a **newly decoded** instance on `200` → identity comparison distinguishes
  "unchanged" from "changed".
- `ObjectKey` carries stage (P6-4); `cachedObjects()` returns keys across all stages.

---

## Step 1 — A change-distinguishing drift refresh in `RemoteReadableScopeService`

Today's P6-4 seam is `Optional<EObject> refresh(String registry, String objectId)` (final only).
Replace it with a **stage-aware, 3-state** refresh so the watcher can fire precisely:

```java
/** Outcome of revalidating one cached object view during drift. */
enum DriftOutcome { CHANGED, UNCHANGED, REMOVED }

/**
 * Revalidate one cached view (registry, stage[, null=final], objectId) via conditional GET,
 * reporting whether its content actually changed. Used only by the drift watcher.
 */
DriftOutcome refresh(String registry, String stage, String objectId) {
    ObjectKey key = new ObjectKey(scopeName, registry, stage, objectId);
    Optional<EObject> before = cache.lookup(key).map(ClientCache.Entry::value);
    Optional<EObject> after = revalidateOrFetch(key);
    if (after.isEmpty()) {
        return DriftOutcome.REMOVED;
    }
    // 304 re-puts the same instance; 200 decodes a new one. Absent-before (evicted mid-cycle)
    // counts as changed (freshly fetched).
    return before.isPresent() && before.get() == after.get() ? DriftOutcome.UNCHANGED : DriftOutcome.CHANGED;
}
```

`revalidateOrFetch` and `get` are unchanged. The old 2-arg `refresh` is removed (only the watcher
used it). Drop the now-stale "Drift is final-stage only in P6-4" comment on the method.

## Step 2 — `DriftWatcher.handleChangedObjects`: revalidate every held view of the object

Replace the single-`contains`/single-`refresh` block with an iterate-and-aggregate over the held
keys (the parsing of `registry`/`objectId` from each `registry/objectId` token is unchanged):

```java
Set<RemoteReadableScopeService.ObjectKey> held = service.cachedObjects(); // snapshot
for (String raw : header.split(",")) {
    // … parse registry + objectId exactly as today (first '/') …
    boolean anyChanged = false, anyHeld = false, allRemoved = true;
    for (RemoteReadableScopeService.ObjectKey k : held) {
        if (!scope.equals(k.scope()) || !registry.equals(k.registry()) || !objectId.equals(k.objectId())) {
            continue; // a different object/scope
        }
        anyHeld = true;
        // Inheritance means a view's requested stage need not be its content's origin stage, so we
        // can't narrow by stage: revalidate EVERY held view of this object, each at its own stage.
        switch (service.refresh(k.registry(), k.stage(), k.objectId())) {
            case CHANGED   -> { anyChanged = true; allRemoved = false; }
            case UNCHANGED -> allRemoved = false;
            case REMOVED   -> { /* this view gone */ }
        }
    }
    if (!anyHeld) {
        continue;                                  // we hold no view of this object
    }
    if (anyChanged) {
        fireObjectChanged(scope, registry, objectId);
    } else if (allRemoved) {
        fireObjectRemoved(scope, registry, objectId);
    }                                              // else: only 304s → no event
}
```

**Firing semantics (per object, deduped across its views):** changed if any view's content changed;
removed only if *every* held view is now gone; otherwise (all unchanged) silent. The listener
callbacks stay `(scope, registry, objectId)` — no stage in the event. (A stage-qualified listener
payload would be a `DriftListener` API change; out of scope here — note for a future ticket.)

## Step 3 — Tests (`DriftWatcherTest`)

The existing object-drift tests mock `service.refresh(registry, objectId) → Optional` and seed
`cachedObjects()` with a single final key; update them to the new `DriftOutcome` refresh and add
stage coverage:

- **Staged view refreshed on drift** — seed `cachedObjects()` with `(jena, cocl, "snapshot", id1)`;
  header `cocl/id1`; `refresh("cocl","snapshot","id1") → CHANGED` ⇒ one `onObjectChanged`. (Pre-P6-5
  this entry was ignored — the regression the ticket targets.)
- **Multiple views, one changed** — hold both `(…,null,id1)` and `(…,"snapshot",id1)`; final
  `UNCHANGED`, snapshot `CHANGED` ⇒ exactly one `onObjectChanged`, no double-fire; verify *both*
  `refresh` calls happened (each view revalidated).
- **All unchanged ⇒ no event** — every matching view returns `UNCHANGED` ⇒ no fire (the
  cross-stage-change-no-false-fire guarantee).
- **All views gone ⇒ removed** — every matching view returns `REMOVED` ⇒ one `onObjectRemoved`;
  mixed (one `REMOVED`, one `UNCHANGED`) ⇒ no removal (object still present somewhere).
- **Unheld object ignored** — header names an object with no held view ⇒ no `refresh`, no event.

(Optionally add a `RemoteReadableScopeServiceTest` case that `refresh(...)` returns `UNCHANGED` on a
304 and `CHANGED` on a 200, exercising the identity distinction directly.)

## Step 4 — Verify

```bash
cd /opt/git/model.atlas
./gradlew :org.eclipse.fennec.model.atlas.rest.client.impl:test \
          :org.eclipse.fennec.model.atlas.rest.client.osgi:compileJava
```

**Done when:** a reported change to an object revalidates *every* held view of it (final + each
staged), firing `onObjectChanged` once when any view's content changed, `onObjectRemoved` once when
all are gone, and nothing when only unaffected sibling stages changed; existing final-stage drift is
unchanged; tests green. No server or wire-format change. Closes the P6-4 "staged entries not
drift-covered" gap.
