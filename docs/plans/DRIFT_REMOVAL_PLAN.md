# Drift-Removal: Test Coverage & Bug-Fix Plan

**Status 2026-07-30 — BOTH BUGS FIXED, ALL TESTS GREEN.** Bug B (client) and Bug A
(server) are fixed test-first; the full `rest.client.osgi.tests` OSGi IT suite (7/7,
including the drift-removal IT with unweakened assertions) passes against a rebuilt
`jena-snapshot` image. Details in the *Resolution* section at the end. Remaining:
live re-test with the sensinact runtime (needs a redeployed Atlas + short
`drift.check.interval.ms`; see Resolution).

**Original status 2026-07-29** — scenario under work: *an EPackage is removed from the
Model Atlas, the client's drift check sees it, and from then on content of that package
must no longer deserialize on the client.* Motivated by a live failure: the
sensinact.mapping.atlas runtime (`/opt/git/fennec-emf.util`) kept deserializing weather
reports **more than 5 minutes** after the weather package was deleted via REST.

## What is done

### T1 — Unit test (GREEN)
`org.eclipse.fennec.model.atlas.rest.client.osgi/test/.../DriftRemovalDeserializationTest.java`

Wires `DriftSubstitution` + `LazyResolvingPackageRegistry` with a simulated publisher
(publish/unpublish mirror into the framework registry + current-publication map, like
`RemoteEPackagePublisher` + emf.osgi). Real XMI deserialization before/after.

- `removalDetectedByDriftCheckMakesContentOfThatPackageUndeserializable` — remove →
  unpublish → re-fetch legitimately misses → `IOException` caused by
  `PackageNotFoundException` with the right nsURI.
- `changedThenGoneAlsoMakesContentUndeserializable` — drift says *changed*, re-resolve
  finds it gone → same outcome.

Conclusion: **the designed chain is sound**; the live problem is in detection/serving,
not in the substitution mechanics.

### T2 — Live OSGi IT (WRITTEN, RED — blocked by Bug A)
`AtlasClientOsgiIT.driftDetectedRemoval_revokesThePackage_andBlocksDeserialization`
(rest.client.osgi.tests, runs against the local `eclipsefennec/model.atlas:jena-snapshot`
image via Testcontainers, like the other tests in that class).

Flow: POST own minimal schema (`http://atlas.example/test/driftremoval/1.0`, EClass
`Thing{name:EString}`) into jena `release` (writable) → configure client HYBRID pinned to
that nsURI, `drift.check.interval.ms=250` → deserialize an instance through a framework
ResourceSet (**fails today, Bug A**) → wait past the watcher's ETag baseline → REST
DELETE → assert published service revoked, nsURI unresolvable, load fails with
`PackageNotFoundException`.

The *before*-load failure is annotated in the test; the test goes green when Bug A is
fixed — do **not** water down the assertion to get it green.

## Bugs found (tasks #3 / #4 in the session task list)

### Bug A — serve-time `file:` URI leak in served schema content (SERVER, this repo)
Served content (`GET /{scope}/schema/stages/{stage}/content` and stage-free) can carry

```
eType="ecore:EDataType file:/opt/modelatlas/runtime/data/jena/schema/release/ecore.ecore#//EString"
```

instead of `http://www.eclipse.org/emf/2002/Ecore#//EString`. Any client then fails to
deserialize instances of that package ("Value 'x' is not legal", NPE
`EDataType.getEPackage()` null — the proxy is unresolvable everywhere).

Evidence gathered:
- Stored `.xmi` on disk is **canonical**; **no `ecore.ecore` file exists on disk** → an
  in-memory Resource with a storage URI holds (a copy of?) the Ecore package inside the
  server's storage ResourceSet; `AbstractStorageHelper.java:192` attaches stored objects
  to storage-URI resources (`buildObjectPath = objectId + extension`, so the URI reads as
  objectId `"ecore"` + `.ecore`).
- Reproduces **2/2** when the OSGi client's activation traffic precedes the fetch; **never
  reproduced via curl** despite replaying: upload→GET at t=0/5/10/20/30 s, GET+HEAD
  `/scopes/{scope}`, `/jena/registries/schema`, inherited-content GET via jena paths,
  stage-free GET, repeated GETs, delete/HEAD cycles.
- In the poisoned container, `GET /jena/schema` also listed atlas-builtin packages
  (management/api etc.); a manually started identical container lists only own uploads.
  Once, `lazyMode_frameworkResourceSetResolvesUnknownNsUriViaAtlas` failed too
  (management/api resolve → null), suggesting the leak poisons other packages' content.

Next step (needs a debugger, black-box probing exhausted): run the server from Eclipse
(or add temp logging), breakpoint/log **creation of Resources whose URI ends in
`.ecore` under the storage root**, and `EcorePackage` being added to any resource.
Suspects: `EPackageStageActionService.registerOrUpdate` →
`DynamicEPackageRegistrationService.registerEPackage` (detaches/re-URIs resources),
storage-ResourceSet proxy resolution during `ePackageIndex.index(...)`, registry-cache
(`FileStorageHelper.updateRegistryCache`).

Fix shape: never let a registered/served package's Ecore (or any cross-package) reference
resolve to a storage-URI resource; serve-time hrefs must deresolve to canonical nsURIs.

### Bug B — DriftWatcher provider-cache gate: stage-explicit packages are drift-blind (CLIENT)
`DriftWatcher.handleChangedNsUris` (rest.client.impl) skips every nsURI not in
`RemoteEPackageProviderImpl.cachedNsUris()`. The stage-explicit fetch path
`getEPackageAtStage` — used by `AtlasScopedFetchOnMissRegistry`, i.e. stage-aware views
and scope-service content parsing — **deliberately bypasses that cache**. Packages
obtained only that way therefore never receive `onPackageChanged/onPackageRemoved`:
never evicted, never unpublished, content keeps deserializing forever. Best candidate
for the live weather-package symptom.

Server side is NOT the problem — verified live with curl: DELETE flips the
`HEAD /scopes/{scope}` ETag and `Atlas-Changed-NsUris` lists the deleted nsURI.

Plan (failing test first, then fix):
1. Failing unit test in rest.client.impl: watcher + a listener/scoped-cache holding an
   nsURI that is **not** in the provider cache; server reports it changed+gone; expect
   the listener to be notified / the cache evicted — fails today.
2. Fix: gate on the **union of held nsURIs** — provider cache + publisher's published
   set + registered scoped-registry caches (e.g. extend `DriftListener` with a
   `heldNsUris()` default or register interest suppliers on the watcher) — or fire events
   for every changed nsURI and let listeners no-op; mind the cost of `refresh()` for
   nsURIs nobody holds (the gate exists to avoid refetching the world).
3. Re-check the classification path: for a non-provider-cached nsURI, `refresh()` would
   *add* it to the provider cache as a side effect — decide whether that's acceptable or
   probe existence without caching.

### Follow-ups / smaller findings
- `DriftWatcher.check()`: an exception in one scope's `checkScope` aborts the whole check
  and is logged at `FINE` only → recurring silent death of the schedule. Catch per scope
  + log at `WARNING`.
- sensinact local config (`org.eclipse.fennec.sensinact.mapping.atlas.local.config`) sets
  no `drift.check.interval.ms` → default 300 000 ms. For live testing add e.g. 10 000.
- fennec-codec (separate repo, Ilenia handles): `XMLURIHandler.resolve()` AIOOBE on
  relative `xsi:schemaLocation` with < 3 segments — issue text drafted 2026-07-29;
  workaround: strip `xsi:schemaLocation` from POSTed XMI.

## Repro recipe
```bash
docker run -d --rm --name atlas-repro -p 18086:8080 \
  -e STORAGE_ROOT=/opt/modelatlas/runtime/data -e ATLAS_HTTP_PORT=8080 \
  -v /opt/git/model.atlas/docker/dockercompose/configs:/opt/modelatlas/runtime/load:ro \
  eclipsefennec/model.atlas:jena-snapshot
# distroless: no shell — inspect via `docker cp atlas-repro:/opt/modelatlas/runtime/data ./dump`
./gradlew :org.eclipse.fennec.model.atlas.rest.client.osgi.tests:testOSGi   # IT (starts its own container)
./gradlew :org.eclipse.fennec.model.atlas.rest.client.osgi:test --tests "*DriftRemovalDeserializationTest"  # unit, green
```

## Order of work (proposed)
1. **Bug B** failing test + fix (client, explains the live symptom, independent of Bug A).
2. **Bug A** server-side debugging + fix (unblocks the IT's before-step).
3. Re-run T2 IT → should go fully green; then also re-test live with the sensinact runtime
   (short drift interval) to confirm the weather scenario.
4. DriftWatcher robustness follow-up (per-scope catch + WARNING).

## Resolution (2026-07-30)

### Bug B — fixed (client, test-first)
- `DriftListener.heldNsUris()` default method added (`rest.client.api`, package 1.0.0 → 1.1.0):
  a listener reports the nsURIs it holds outside the provider cache.
- `DriftWatcher.handleChangedNsUris` gates on the **union** of `provider.cachedNsUris()`
  and every listener's `heldNsUris()` (guarded per listener; a misbehaving listener can't
  kill the check). Note: the eventual `refresh()` is stage-free — a package existing only
  at a non-final stage classifies as *removed*, the listener evicts, and the next
  stage-explicit look-up re-fetches it (self-healing, documented in the javadoc).
- Implementations: `AtlasScopedFetchOnMissRegistry` (own map — the original victim),
  `AtlasDelegatingPackageRegistry` (own entries can outlive provider-cache TTL/size
  eviction), `DriftSubstitution` (publisher's published set — a published service whose
  cache entry was evicted was also drift-blind). All with unit tests; new/changed tests in
  `DriftWatcherTest`, `AtlasScopedFetchOnMissRegistryTest`,
  `AtlasDelegatingPackageRegistryTest`, `DriftSubstitutionTest`.
- Robustness follow-up done too: per-scope try/catch in `DriftWatcher.check()` at
  `WARNING` (one broken scope no longer starves the rest), scheduled-check failure log
  raised FINE → `WARNING`.

### Bug A — root cause found & fixed (server)
Root cause (static analysis, then confirmed by test): **`EcoreMessageBodyHandler.writeTo`
re-parented the served EPackage instance** into the throw-away response resource
(`resource.getContents().add(eObject)`, resource URI = `<packageName>.ecore`). For a
storage-loaded package this detached it from its `file:` resource; for the parent-`atlas`
fallback (`AtlasSchemaRegistryService` → `staticPackageRegistry.getEPackage`) it moved a
**live registered singleton**. The jena listing inherits the atlas built-ins — including
the **Ecore package itself** (registered by emf.osgi's `EcorePackagesRegistrator`) — so a
client that prefetches every listed nsURI (exactly what the OSGi client's activation does;
curl replays never fetched Ecore's content → the "client-only" reproduction) steals
`EcorePackage.eINSTANCE` into a resource literally named **`ecore.ecore`**. Every href
later computed against Ecore elements then leaks that URI; one save/parse round-trip
against a storage base absolutizes it to
`file:/opt/modelatlas/.../release/ecore.ecore#//EString`. With `-ea`, EMF itself flags the
steal: `AssertionError: A frozen model should not be modified` (production runs without
`-ea`, so it corrupted silently).
- Fix: `writeTo` now serializes an **`EcoreUtil.copy`** of the served package and discards
  the response resource; the live instance is never touched. (In-place serialization of
  the instance's own resource was tried first and does NOT work: a registered singleton's
  lazily created resource is a plain `ResourceImpl` whose `save()` throws
  `UnsupportedOperationException` — found by the 5 builtin-serving ITs going red.)
- Same pattern applied to `JsonSchemaMessageBodyReaderWriter.writeTo` (it detached the
  served instance from its resource).
- Regression tests: `EcoreMessageBodyHandlerTest` (rest.ecore.xmi, new plain-JUnit) — no
  re-parenting for attached packages, no hijack of `EcorePackage.eINSTANCE`, and canonical
  `http://www.eclipse.org/emf/2002/Ecore#//EString` hrefs even after Ecore's own content
  was served.

### Verification
- `rest.client.osgi.tests` testOSGi: **7/7 green** against the rebuilt
  `eclipsefennec/model.atlas:jena-snapshot` (drift-removal IT passes with the original
  strict assertions; the 5 builtin-serving tests confirm the copy-based writer).
- Manual smoke test on the image: builtin content GETs return 200 with fully canonical
  eType hrefs; serving Ecore's own content no longer poisons subsequent serves.
- Gotcha for rebuilds: `export.modelatlas.runtime_docker_jena` can wrongly report
  UP-TO-DATE after a bundle rebuild — delete
  `runtime/generated/distributions/executable/modelatlas.runtime_docker_jena.jar` and
  re-export before `prepareDocker`/`docker build`.

### Live sensinact re-test (2026-07-30): WORKING
The remaining live symptom was a config placement issue: `drift.check.interval.ms` had
been added under the wrong PID. It must live in the **`AtlasClientComponent` factory
config** (the same block as `base.uri`/mode). With the property under the right PID the
drift watcher runs and the removal takes effect. Note for future debugging: the watcher
is silent on the happy path — the visible signal of a removal is the publisher's INFO
line `Unpublished remote EPackage <nsUri>`.

### Still open
- fennec-codec `XMLURIHandler.resolve()` AIOOBE issue (separate repo, Ilenia).
