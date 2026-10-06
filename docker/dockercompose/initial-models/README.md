# Initial models

What `InitialModelLoader` seeds into a scope the first time the stack starts. The directory is
mounted into the container, so a file dropped here is seeded on the next start and nothing has to
be rebuilt. Seeding is idempotent: an object id already present in the target stage is skipped, so
restarting does not duplicate or overwrite anything.

The layout is the one the loader reads (issue #198):

```
scopes/<scope>/<registry>/<objectId>.xmi
```

Everything below this README is ignored by git. These are generated documents, not source, and
not all of them are ours to redistribute — see the licences below.

## `scopes/dimcity/compliance-context/`

The `ComplianceContext` catalogue: the requirements, taxonomies and corpora a review is made
against (issue #332). One file per context, each one self-contained — contexts refer to each other
by `ContextRef.contextId` only, never by `href`.

`ComplianceContext.id` is an EMF ID, so the object id is the `id` inside the file. Keep the file
name equal to it anyway; it is how a reader finds the file a stored object came from.

| File | Context | Licence |
|---|---|---|
| `gdpr.xmi` | DSGVO, DE + EN, 25 requirements; the `data-categories`, `combination-kinds` and `lawful-bases` taxonomies the GDPR review codes refer into | EUR-Lex, reuse permitted (Commission Decision 2011/833/EU) |
| `cra.xmi` | Cyber Resilience Act, DE + EN | EUR-Lex, reuse permitted |
| `ai-act.xmi` | EU AI Act, DE + EN | EUR-Lex, reuse permitted |
| `bsi-grundschutz-pp.xmi` | Grundschutz++ as an OSCAL catalogue, ~1000 requirements | **CC BY-SA 4.0** (BSI) — attribution and share-alike apply |
| `iso27001-annex-a.xmi` | Annex A as identifiers only; the standard's text is not reproduced | BSI mapping |

`gdpr.xmi` is the only one the review path needs today: every category id a `ComplianceReport`
names is a `Category.id` of its `data-categories` taxonomy. The other four are catalogue content
for readers and for contexts other than the GDPR.

Where to get them: they are produced by the compliance data generator
(`scripts/compliance/generate.sh`) from the corpora, alongside `crosswalks/` and `inventory/`,
which this mount does not seed. Ask for the generated bundle, or regenerate it, and copy
`contexts/*.xmi` to `scopes/dimcity/compliance-context/`.

## Pointing the loader here

The loader reads `initial.models.folder`, which `runtime.config.docker` fills from
`INITIAL_MODELS_FOLDER`. The compose file sets that env var and mounts this directory at the same
path. Do not configure the `InitialModelLoader` PID a second time in `configs/jena.json`: two
providers of one singleton PID are resolved by Configurator ranking, not by intent.

`halt.on.error` defaults to `true` and the loader is atomic — everything is parsed and validated
before anything is written — so one malformed file stops the framework rather than leaving the
scope half-seeded.
