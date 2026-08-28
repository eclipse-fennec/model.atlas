# P6-2 — Rename `ReadOnlyScopeService` → `ReadableScopeService` (execution plan)

**Ticket:** P6-2 (depends on nothing; independent of P6-1). Gates P6-3.

Mechanical-but-wide rename. The interface is an **EMF-generated EClass** in `scope.api`, so the
heart is an ecore/genmodel edit + regen (you do the regen). Everything else is hand-written code,
bnd service filters, the collector bundle rename, and living docs.

## Tokens

A single global token substitution gives the whole consistent rename:

| from | to |
|---|---|
| `ReadOnlyScope` (PascalCase) | `ReadableScope` |
| `readonlyscope` (lowercase) | `readablescope` |

These cascade correctly:
- `ReadOnlyScopeService` → `ReadableScopeService`
- `RemoteReadOnlyScopeService` → `RemoteReadableScopeService`
- `ReadOnlyScopeCollector` → `ReadableScopeCollector` (DS `@Component(name=…)` included)
- `ReadOnlyScopeServiceTest` / `…IntegrationTest` → `Readable…`
- collector package/bundle segment `readonlyscope` → `readablescope`

Verified: `ReadOnlyScope` only ever appears as `ReadOnlyScopeService` or `ReadOnlyScopeCollector`
— no unrelated identifier is caught.

## Hard exclusions (do NOT text-replace these)

- **Generated code** — regenerated from the models, so hand-edits get clobbered:
  - `org.eclipse.fennec.model.atlas.scope.api/src-gen/**`
  - `org.eclipse.fennec.model.atlas.workflow/src-wf-api/**`
- **Historical design notes** (point-in-time records, decided to leave): `docs/design/rest-client-P4-*.md`, `docs/design/rest-client-P5-*.md`
- **This plan doc** (`rest-client-P6-2-rename-plan.md` — it documents old→new examples)
- Build output: `**/bin/**`, `**/bin_test/**`, `**/generated/**`, `build/`, `.gradle/`, `.git/`

---

## Step 1 — EMF source models (then you regenerate)

Edit (these live under `model/`, not `src-gen/`, so the token sweep below covers them — listed
here so you know what the regen depends on):

- `org.eclipse.fennec.model.atlas.scope.api/model/scope-api.ecore`
  — EClass `name="ReadOnlyScopeService"` (line ~17) + 3× `#//ReadOnlyScopeService/T` type-param refs.
- `org.eclipse.fennec.model.atlas.scope.api/model/scope-api.genmodel`
  — ~14 `scope-api.ecore#//ReadOnlyScopeService…` refs (genClass + genOperations + genParameters).
- `org.eclipse.fennec.model.atlas.workflow/model/workflow-api.ecore`
  — line ~436, `WritableScopeService` `eGenericSuperTypes` → `scope-api.ecore#//ReadOnlyScopeService`.
- `workflow-api.genmodel` — **no edit** (no direct ref; resolves the supertype via `usedGenPackages`).

**Then regenerate, in this order** (scope.api first so the supertype type exists for workflow):
1. `scope.api` `src-gen` — overwrites: `ReadOnlyScopeService.java` → **`ReadableScopeService.java`**,
   `ScopeApiPackage.java`, `ScopeApiPackageImpl.java`, `ScopeApiSwitch.java`, `ScopeApiAdapterFactory.java`.
2. `workflow` `src-wf-api` — overwrites: `WritableScopeService.java`, `WorkflowApiPackageImpl.java`,
   `WorkflowApiAdapterFactory.java`, `WorkflowApiSwitch.java`.

Commit the regen output; do not hand-edit it.

---

## Step 2 — Hand-written code + bnd service filters

Covered by the token sweep (Step 5). Counts are current occurrences, for sanity-checking afterwards.

**File renames required** (content + filename both carry the token):

| current | → |
|---|---|
| `…rest.client.impl/src/…/RemoteReadOnlyScopeService.java` (7) | `RemoteReadableScopeService.java` |
| `…rest.client.impl/test/…/RemoteReadOnlyScopeServiceTest.java` (13) | `RemoteReadableScopeServiceTest.java` |
| `…workflow.tests/src/…/ReadOnlyScopeServiceIntegrationTest.java` (11) | `ReadableScopeServiceIntegrationTest.java` |

**Content-only** (Java):

- `scope.api/src/…/AtlasProperties.java` (2) — hand-written (`src/`, not `src-gen/`)
- `rest.client.api/…/ModelAtlasClient.java` (3)
- `rest.client.impl/src/…/DriftWatcher.java` (6), `ModelAtlasClientImpl.java` (4)
- `rest.client.impl/test/…/DriftWatcherTest.java` (5)
- `rest.client.osgi/src/…/AtlasClientComponent.java` (5), `RemoteScopeServicePublisher.java` (12)
- `rest.client.osgi/test/…/RemoteScopeServicePublisherTest.java` (8)
- `validation/src/…/ValidationServiceImpl.java` (10)
- `validation.client.tests/src/…/RemoteValidationIT.java` (6)
- `datagen.rest/src/…/DataGenResource.java` (3)
- `workflow/src/…/AtlasScopeService.java` (7), `ScopeServiceImpl.java` (9)

**bnd service-filter / package refs:**

- `rest.client.tests/bnd.bnd` (1)
- `validation.client.tests/bnd.bnd` (2)

---

## Step 3 — Full rename of the `readonlyscope.collector` bundle

Decision: rename the bundle + class, not just references.

**Directory / file renames (`git mv`):**

- Bundle dir: `org.eclipse.fennec.model.atlas.readonlyscope.collector` → `…readablescope.collector`
- Package segment dirs (src **and** test): `…/atlas/readonlyscope/collector` → `…/atlas/readablescope/collector`
- `ReadOnlyScopeCollector.java` → `ReadableScopeCollector.java`
- `ReadOnlyScopeCollectorTest.java` → `ReadableScopeCollectorTest.java`

**Content** (covered by the sweep): the two `.java` files (package decl, class name,
`@Component(name="ReadOnlyScopeCollector")`, `ReadOnlyScopeService` refs), `package-info.java`,
and `.project` `<name>` (line 3). `bnd.bnd` needs no edit — it has no explicit
`Bundle-SymbolicName`, so the name derives from the (now-renamed) directory.

> The bnd workspace auto-discovers bundle dirs; `settings.gradle` does **not** list it, so no
> `settings.gradle` edit. The DS component-name change is safe — no `.config`/`.bndrun`/`.cfg`
> references the component name, only the bundle symbolic name (below).

**Dependents of the symbolic name `…readonlyscope.collector`** — update each (sweep covers them):

- buildpath: `workflow.tests/bnd.bnd` (lines 28, 40), `validation/bnd.bnd` (line 9), `datagen.rest/bnd.bnd` (line 6)
- bndrun: `runtime/modelatlas.runtime_base.bndrun` (line 150), `workflow.tests/test.bndrun` (line 64),
  `validation.client.tests/test.bndrun` (lines 14 `bnd.identity`, 58)

**Java importers of the collector class** (sweep covers — they import the renamed package + class):
`workflow.tests` IntegrationTest, `validation/ValidationServiceImpl`,
`validation.client.tests/RemoteValidationIT`, `rest.client.osgi/RemoteScopeServicePublisher`,
`datagen.rest/DataGenResource`.

---

## Step 4 — Living docs (leave historical P4/P5 notes)

Token-replace: `docs/design/rest-client.md` (8), `docs/design/rest-client-tickets.md` (14),
`docs/design/rest-client-P6-stage-aware-client-plan.md` (10),
`datagen.rest/README.md` (1), `rest.client.impl/README.md` (3), `rest.client.osgi/README.md` (4).

---

## Step 5 — Suggested mechanics

Run the text sweep over a curated list, **then** the `git mv`s, **then** regenerate (Step 1).

```bash
cd /opt/git/model.atlas

# 1) Curated file list: everything carrying a token, minus generated code,
#    build output, historical notes, and this plan doc.
mapfile -t FILES < <(grep -rlE 'ReadOnlyScope|readonlyscope' . \
  --exclude-dir=.git --exclude-dir=bin --exclude-dir=bin_test \
  --exclude-dir=generated --exclude-dir=build --exclude-dir=.gradle \
  | grep -v '/src-gen/' \
  | grep -v '/src-wf-api/' \
  | grep -vE 'docs/design/rest-client-P[45]-' \
  | grep -v 'rest-client-P6-2-rename-plan.md')

printf '%s\n' "${FILES[@]}"          # eyeball the list before editing
sed -i -e 's/ReadOnlyScope/ReadableScope/g' -e 's/readonlyscope/readablescope/g' "${FILES[@]}"

# 2) File / directory renames
git mv org.eclipse.fennec.model.atlas.rest.client.impl/src/org/eclipse/fennec/model/atlas/rest/client/impl/RemoteReadOnlyScopeService.java \
       org.eclipse.fennec.model.atlas.rest.client.impl/src/org/eclipse/fennec/model/atlas/rest/client/impl/RemoteReadableScopeService.java
git mv org.eclipse.fennec.model.atlas.rest.client.impl/test/org/eclipse/fennec/model/atlas/rest/client/impl/RemoteReadOnlyScopeServiceTest.java \
       org.eclipse.fennec.model.atlas.rest.client.impl/test/org/eclipse/fennec/model/atlas/rest/client/impl/RemoteReadableScopeServiceTest.java
git mv org.eclipse.fennec.model.atlas.workflow.tests/src/org/eclipse/fennec/model/atlas/workflow/tests/ReadOnlyScopeServiceIntegrationTest.java \
       org.eclipse.fennec.model.atlas.workflow.tests/src/org/eclipse/fennec/model/atlas/workflow/tests/ReadableScopeServiceIntegrationTest.java

# Collector: class files -> package segment dirs (src + test) -> bundle dir
C=org.eclipse.fennec.model.atlas.readonlyscope.collector
git mv $C/src/org/eclipse/fennec/model/atlas/readonlyscope/collector/ReadOnlyScopeCollector.java \
       $C/src/org/eclipse/fennec/model/atlas/readonlyscope/collector/ReadableScopeCollector.java
git mv $C/test/org/eclipse/fennec/model/atlas/readonlyscope/collector/ReadOnlyScopeCollectorTest.java \
       $C/test/org/eclipse/fennec/model/atlas/readonlyscope/collector/ReadableScopeCollectorTest.java
git mv $C/src/org/eclipse/fennec/model/atlas/readonlyscope \
       $C/src/org/eclipse/fennec/model/atlas/readablescope
git mv $C/test/org/eclipse/fennec/model/atlas/readonlyscope \
       $C/test/org/eclipse/fennec/model/atlas/readablescope
git mv $C org.eclipse.fennec.model.atlas.readablescope.collector

# 3) Regenerate scope.api src-gen, then workflow src-wf-api (Step 1), and commit the output.
```

> The sweep edits `scope-api.ecore`/`.genmodel` and `workflow-api.ecore` (Step 1 inputs) because
> they live under `model/` and aren't excluded — so after the sweep they already name
> `ReadableScopeService`, ready for regen.

---

## Step 6 — Verify

```bash
cd /opt/git/model.atlas
# Should print ONLY: src-gen/**, src-wf-api/** (pre-regen), historical P4/P5 docs, and this plan.
grep -rEln 'ReadOnlyScope|readonlyscope' . \
  --exclude-dir=.git --exclude-dir=bin --exclude-dir=bin_test --exclude-dir=build --exclude-dir=.gradle
```

After regen, re-run — the `src-gen`/`src-wf-api` hits should be gone too, leaving only the
historical docs and this plan. Then:

```bash
./gradlew :org.eclipse.fennec.model.atlas.scope.api:compileJava \
          :org.eclipse.fennec.model.atlas.workflow:compileJava \
          :org.eclipse.fennec.model.atlas.readablescope.collector:compileJava \
          :org.eclipse.fennec.model.atlas.rest.client.impl:test \
          :org.eclipse.fennec.model.atlas.validation:compileJava
```

**Done when:** the interface is `ReadableScopeService` across models + generated code + all
hand-written refs; `WritableScopeService extends ReadableScopeService`; the collector bundle is
`…readablescope.collector` with no dangling references; living docs updated; build + client tests
green. Unblocks P6-3.
