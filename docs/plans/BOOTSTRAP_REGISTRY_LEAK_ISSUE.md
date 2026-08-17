# Issue sketch — the initial-model bootstrap: not deployed, and unsafe if it were

*Draft for a GitHub issue. Written 2026-08-06 from quality-review finding F39.*

---

## Summary

Two things, and the first one decides what to do about the second.

1. **The bootstrap bundle is deployed nowhere.** `org.eclipse.fennec.model.atlas.bootstrap` does
   not appear in any runtime assembly — not in `runtime_base`, not in any docker or local variant.
   The only bndrun naming it is its own test bndrun. Yet **every docker image ships a
   configuration for it**, so the documented `INITIAL_MODELS_FOLDER` environment variable
   currently does nothing at all.
2. **If it were deployed, a failed bootstrap would poison the JVM-global EPackage registry** and
   could not recover without restarting the process (quality-review finding F39).

So the question to answer first is whether initial-model loading is a feature we want. If yes,
the bundle needs to enter the runtime *and* F39 needs fixing before it does. If no, the bundle and
its configuration should go, and F39 closes with it.

## 1. The deployment mismatch

Verified 2026-08-06 by searching every `*.bndrun` in the repo:

| | |
|---|---|
| `org.eclipse.fennec.model.atlas.bootstrap` in a runtime bndrun | **no** — only `bootstrap.tests/test.bndrun` |
| `org.eclipse.fennec.model.atlas.runtime.config.docker` in the docker runtimes | **yes** — `modelatlas.runtime_docker.bndrun:3`, inherited by the file, apicurio, git and jena variants |
| that config bundle configures `InitialModelLoader` | **yes** — `configs/runtime.json:12-14` |

```json
"InitialModelLoader": {
    "initial.models.folder": "$[env:INITIAL_MODELS_FOLDER;default=/initial-models]"
}
```

Consequences as things stand:

- Setting `INITIAL_MODELS_FOLDER`, or mounting `/initial-models` into a container, has no effect.
  Nothing reports this: ConfigAdmin happily stores a configuration no component consumes.
- The bundle is still built, tested and published to Maven, so it looks alive from the outside.

Either the bundle was meant to be in the docker runtime and never made it in, or the configuration
is a leftover. Both are cheap to fix; they just point in opposite directions.

## 2. F39 — the leak, if the bundle is deployed

`InitialModelLoader` seeds the JVM-global `EPackageRegistryImpl.INSTANCE` package by package
*while* it is still validating the batch (`prepareEPackages`, InitialModelLoader.java:262-280):

```java
failOnDuplicate(ePackage);                                          // validates THIS one
...
resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
EPackageRegistryImpl.INSTANCE.put(ePackage.getNsURI(), ePackage);   // global, JVM-wide
collectSubPackages(ePackage, all);                                  // validates subpackages — can throw
```

`failOnDuplicate` (:297) throws when an nsURI is blank or already registered. The call chain is
`constructor (@Activate) → loadInitial → prepareEPackages`, so a failure on the third file aborts
activation after the first two are already in the global registry.

1. **`@Deactivate` never runs** — a component whose constructor throws never becomes active, and
   the only cleanup of the global registry lives there (:126).
2. **Even if it ran it would not help** — the list it cleans, `registeredNsUris`, is filled later
   in `registerEPackageServices` (:293), which the failure never reaches. Nothing tracks what was
   seeded.
3. **The failure is self-perpetuating** — the validation that failed is "is this nsURI already
   registered?". Every retry now trips over the previous attempt's leftovers and fails on file #1,
   reporting the wrong culprit. Only a process restart clears it.

`EPackageRegistryImpl.INSTANCE` is `EPackage.Registry.INSTANCE`, shared by every bundle in the
framework, so a half-seeded nsURI can also shadow a package another bundle would have contributed.
(The `resourceSet` half is harmless: that reference is `PROTOTYPE_REQUIRED` and dies with the
component.)

**Current blast radius: none in production**, because of §1 — the component runs only in
`bootstrap.tests`. This is a defect waiting for the day the bundle is deployed, not a live
incident.

## Decisions needed

**D1 — Is initial-model loading a feature we want?**
*Deploy it* (add the bundle to the runtime bndruns; fix F39 first), or *drop it* (remove the
bundle and the `InitialModelLoader` block from `runtime.json`, and the `INITIAL_MODELS_FOLDER`
mention from the docs). Anything else leaves an env var in the documentation that does nothing.

**D2 — If we deploy: should one bad file abort the whole deployment?**
Today one duplicate or blank nsURI anywhere in the folder aborts everything, and *no* initial
model is deployed — including when the duplicate is against a package another bundle registered,
which is not the operator's mistake. The alternative is to skip that file with a warning and
deploy the rest. This is an operational contract choice: "one stray file bricks the models" versus
"partial deployment is normal".

**D3 — If we deploy: should the bootstrap write to the global registry at all?**
The seeding exists so `EcoreUtil.resolveAll` can resolve nsURI hrefs between the loaded files
(comment at :205-207) — but they are loaded into this component's own `ResourceSet`, whose package
registry is seeded on the line above. If that suffices, the global `put` only serves code that
looks models up through `EPackage.Registry.INSTANCE`, and removing it would change what those
consumers see. Worth noting the project has been bitten from the other side: `EMFFileWatcher` had
to *add* global-registry cleanup on deactivate, because without it pipelines silently skipped
registration. So the global registry is load-bearing somewhere — where exactly should be
established before writes to it are removed.

## Fix options for F39 (only if D1 = deploy)

### A. Validate the whole batch, then seed
Two passes: collect and validate every resource and subpackage first, seed only once all have
passed. Needs the validation to track the batch's own nsURIs, because with nothing seeded yet an
intra-batch duplicate would no longer be caught by the registry check — today's seed-as-you-go
catches that case by accident.
*For:* the common failure becomes atomic; a retry works and reports the real error.
*Against:* does not cover a failure after seeding (`registerEPackageServices`, QVT configuration,
`resolveAll`). *Effort:* small.

### B. Roll back on any activation failure
Wrap `loadInitial` so a throw removes the seeded nsURIs from both registries and unregisters any
services already registered, then rethrows.
*For:* covers every failure point. *Against:* compensation must be kept in step with activation.
*Effort:* small–medium.

### C. A + B — what I would implement if D2 stays "fatal".

### D. Stop seeding the global registry (see D3)
*For:* the leak cannot happen; the bootstrap stops mutating global JVM state.
*Against:* unknown blast radius until the audit in D3 is done. *Effort:* small change, potentially
large consequences.

### E. Tolerate duplicates instead of failing (see D2)
Log and skip a package whose nsURI is taken; deploy the rest. Removes the "one bad file bricks
startup" mode and most of the leak's practical impact. Combines with A/B/C.

**Recommendation:** answer D1 first. If we deploy: **C**, or **E + A** if D2 says duplicates should
be tolerated; **D** only after the D3 audit.

## Testable either way

`bootstrap.tests` (`InitialModelLoaderIntegrationTest`) already drives the loader against a folder
of models. The scenario: a folder with two models sharing an nsURI → activation fails → assert the
global registry no longer holds the first model's nsURI, and that a corrected folder then deploys
successfully. That second assertion is the one that fails today.

## Related

- **F39** in `docs/reviews/quality-review-2026-08-05.md`.
- The same bug class was fixed in `EMFFileWatcher` (global-registry cleanup on deactivate); worth
  cross-checking any other component that writes to `EPackageRegistryImpl.INSTANCE`.
