# Hosting QVT-O Transformations

The Model Atlas hosts QVT-O transformations as first-class content: you upload
`.qvto` source text into a scope, the Atlas validates and compiles it against
that scope's models, and consumers (such as the Data Atlas) fetch the compiled
unit by **qualified name + fingerprint**. The compiled-unit machinery is
[Fennec M2X](https://github.com/eclipse-fennec/emf.m2x) — see its
*Compiled Units — User Guide* for the concepts (units, manifests,
fingerprints, dependency modes).

## The transformation registry

Transformations live in a registry of type `TRANSFORMATION` (default name
`transformations`), configured like any other workflow registry with stages
(e.g. `draft` → `approved` → `release`). Three document kinds share it:

| Kind | What it is | Who writes it |
|---|---|---|
| Source (`SourceUnit`) | The `.qvto` text you edit | you, via upload |
| Compiled unit (`CompiledUnit`) | The self-contained, executable form with its manifest | the Atlas, on every successful compile |
| Diagnostics (`SourceDiagnostics`) | The compile outcome of your source: status + positioned findings | the Atlas, on every compile |

Only sources are yours to write: units and diagnostics are **derived content**
— attempting to POST/PUT them over the generic endpoints is answered `403`,
because a compiled unit the Atlas did not produce would be a forgery.
Deleting a source removes its diagnostics with it; already-compiled units
stay, versioned, for consumers that pinned them.

## Uploading a source

`POST`/`PUT` the plain transformation text to the generic registry endpoint
with `Content-Type: text/x-qvto`; pick the qualified name as the object id:

```bash
curl -X POST "http://localhost:8080/atlas/myscope/registries/transformations/stages/draft/Announce" \
     -H "Content-Type: text/x-qvto" \
     --data-binary @Announce.qvto
```

The upload response returns after the compile finished. What happened is in
the diagnostics document:

```bash
curl "http://localhost:8080/atlas/myscope/registries/transformations/units/draft/Announce/diagnostics" \
     -H "Accept: application/json"
```

| `compileStatus` | Meaning |
|---|---|
| `OK` | The source defines a startable root transformation; a compiled unit exists in this stage. `unitFingerprint` is the value a consumer pins. |
| `INVALID` | The source did not compile. It **stays stored as your draft**; the `entries` carry each finding with `line`, `column`, `severity` and `message` — fix and upload again. |
| `LIBRARY` | The source is a valid library (no startable root). It is stored and resolved as a dependency of other compilations. |

Reading the source back with `Accept: text/x-qvto` answers the stored text
unchanged — the editor round trip.

## Libraries and dependencies

An `import` in a transformation resolves against the sources and units of the
same (scope, stage) view. Dependencies are bound in the m2x default **pin**
mode: the unit's manifest names each dependency with the exact fingerprint it
was compiled against.

Uploading a changed library **automatically recompiles every unit that
depends on it, transitively**. The recompiled units get new fingerprints;
previously pinned versions stay resolvable, so a consumer that pinned the old
fingerprint keeps working until it moves.

## Stages

Each stage compiles against **its own package view** (the EPackages of the
scope's schema registry in that stage), and the package fingerprints go into
the unit's manifest — a consumer's prepare step verifies them.

Units never transition. Transitioning a **source** to another stage
recompiles it there, against that stage's package view. If the target-stage
compile fails (typically: an imported library has not been transitioned yet),
the transition still succeeds and the failure is visible as the diagnostics
document in the target stage; when the missing library later arrives there,
its upload recompiles the dependents automatically. Rule of thumb:
**transition libraries first**.

Because unit fingerprints do not incorporate package fingerprints, the stage
is part of a unit's address: consumers always address
`(scope, stage, qualifiedName, fingerprint)`.

## Fetching a unit

```bash
# the newest compiled unit of that name in the stage view
curl ".../myscope/registries/transformations/units/release/Announce"

# exactly one version — what the Data Atlas configuration pins
curl ".../myscope/registries/transformations/units/release/Announce?fingerprint=m2x1:9f86..."

# the stored fingerprints, newest first
curl ".../myscope/registries/transformations/units/release/Announce/versions"
```

A pinned fingerprint the stage does not hold is answered `404` naming the
versions it does have — never a silent substitute. The unit document arrives
in any EMF wire format the Atlas serves (XMI, JSON, …) and is loaded on the
consumer side through the m2x `UnitMaterializer`/`UnitPreparer`, which
verifies the manifest and binds the metamodels; see the m2x compiled-units
guide, §6.

## Configuration

The registry and the compile action are plain ConfigAdmin configuration — see
`org.eclipse.fennec.model.atlas.runtime.config.local/configs/workflow.json`
for the reference shape: a `RegistryService~transformations` factory config
(`registry.type: TRANSFORMATION`, the three root EClasses, a ResourceSet
target that knows the `compiled` and `diagnostics` models) plus a
`QvtStageActionService` config naming the trigger stages.
