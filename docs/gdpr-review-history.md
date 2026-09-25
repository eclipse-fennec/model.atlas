# The GDPR review history document

A GDPR review produces a `GdprReport` in the atlas. That report is a machine artefact: correct,
citable, and unreadable as a compliance document. It also says nothing about *how* the assessment of
a model got to where it is — which verdict an agent gave, which one a human corrected, and what
changed between them.

The **GDPR review history** is a second, derived object that answers exactly that. One document per
reviewed **subject**, **stage** and **language**, holding every review of it, the assessment each one
recorded, and a field-level diff between them.

The subject is what stays the same while the content moves: the **nsURI** of a package, the qualified
name of a compiled transformation unit. Keyed by fingerprint instead, a document would hold a single
revision and its change sheet - the point of the whole thing - would have nothing to compare against.
Each revision keeps the fingerprint it was about, so a model edited without its nsURI moving reads as
successive revisions with a changing fingerprint.

It is per **stage** because a review describes the stage it was carried out against: the same model at
`draft` and at `approved` is two judgements, and merging them would diff one against the other. And per
**language**, because a review is carried out in one language from start to seal and quotes that
language's consolidation of the regulation; merged, the diff would report every rationale as rewritten
on each switch. It is deliberately flat, so the tabular codec renders it as a
spreadsheet without any rendering code: an auditor opens it in LibreOffice or Excel and reads one
line per thing in the model.

## What is in it

`GdprReportHistory` has three containment lists, and each becomes a sheet:

| sheet | one row per | the columns that matter |
|---|---|---|
| `GdprReportHistory` | the document | `subjectIdentifier`, `subjectName`, `reportLanguage`, `subjectLanguage` (for a transformation), `rebuiltAt`, `revisionCount` |
| `ReportRevision` | review run | `revisionNumber`, `reportId`, `generatedAt`, `generatedBy`, `origin`, `findingCount`, `changeCount` |
| `EvaluationRow` | evaluated classifier or feature - or, for a transformation review, one flow - **per revision** | `classifierId`, `featureId`, `typeName`, `category`, `relevanceLevel`, `confidence`, `rationale`, `recommendation`, `citations`, `changeKind` |
| `ChangeRow` | field that changed | `revisionNumber`, `changedAt`, `changedBy`, `classifierId`, `featureId`, `field`, `changeKind`, `oldValue`, `newValue` |

The change sheet is the point of the whole document. A human raising a category reads like this:

```
rev  changedBy   classifierId  featureId          field       kind      oldValue                      newValue
2    a-human     Patient       Patient.diagnosis  category    MODIFIED  PERSONAL_DATA                 SPECIAL_CATEGORY
2    a-human     Patient       Patient.diagnosis  confidence  MODIFIED  REQUIRES_PURPOSE_CONFIRMATION HIGH
2    a-human     Patient       Patient.postcode   evidence    REMOVED   Rec.26
2    a-human     Patient       Patient.email                  ADDED                                   ONLINE_IDENTIFIER
```

Two cosmetics are structural to `SQL_TABLES` and cannot be switched off: every sheet carries a
surrogate `_id` column, and each child sheet a `<parent>_id` foreign key. Sheet names are EClass
names, so the tabs read `EvaluationRow` and `ChangeRow`.

## How it is produced

`GDPRReportHistoryStageAction` (bundle `org.eclipse.fennec.model.atlas.gdpr.history`) is a stage
action on the registry that holds the reviews. Whenever a `GdprReport` enters, changes or leaves a
watched stage, it rebuilds the document of that report's subject.

- **It rebuilds, it does not append.** Every rebuild reads every review of the subject and produces
  the whole document. A replayed event therefore cannot duplicate a revision, and a report written
  while the runtime was down is picked up by the next rebuild — a compliance document that is
  quietly wrong is worse than one that is missing.
- **One document per subject, stage and language.** The id is
  `gdpr-history-<identifier>-<digest>-<language>`: the identifier flattened to one path segment with
  everything non-alphanumeric replaced by a dash, then eight hex characters of the SHA-256 of the
  *raw* identifier, then the corpus language.

  The digest is not decoration. Flattening is not injective - `http://x.org/a/b` and
  `http://x.org/a-b` both give `http---x-org-a-b` - and the readable part is truncated, so a long
  nsURI would collide with another that differs only past the cut. Without the digest one subject's
  history would silently overwrite another's. It also keeps the id computable, so the action finds
  its own document without searching for it.

  **The stage is deliberately not in the id.** A document is stage-specific, but which stage it is in
  is *where it lives*, not part of what it is called - an id naming a stage would start lying the
  moment the document moved. The same id therefore names one document per stage, which is what an
  objectId already means everywhere else in the atlas.
- **The document is written into the stage its reviews were carried out at**, not into a configured
  one. That stage may be the registry's final stage, which refuses updates - so the document's EClass
  is declared `derived.eclass.uri` on the document registry, which is what lets the atlas rewrite its
  own output there. Without that declaration the first document is accepted and every rebuild of it
  is refused.
- **Reviews are read from every configured stage.** `report.stages` has to name every stage the
  reports can be in, including `approved`: a report in a stage the action was not told about is
  invisible to it, and no document is built for it.
- **Revisions are matched on `ClassifierEvaluation.id` and `FeatureEvaluation.id`**, which the report
  model documents as stable across reruns — never on `Finding.id`, which is assigned per run and
  would report a change to a finding that did not change.
- **The write is in-process**, through the scope service, not through a REST call to the runtime's
  own endpoint. The startup replay runs while the HTTP connector is still being configured, so a
  loopback would lose precisely the rebuild that exists to close a gap.

## Downloading it

The document is **not stored as a spreadsheet**. It is an EObject like any other, and the ODS is a
representation the content endpoint serves on demand — which also gives CSV, XLSX and JSON for free.

```bash
curl -o gdpr-history.ods \
  'http://localhost:8080/atlas/rest/jena/registries/gdprdoc/stages/draft/content?objectId=gdpr-history-https---example-org-clinic-1-0-0-a1b2c3d4-en&mediaType=application/vnd.oasis.opendocument.spreadsheet'
```

No `Codec-Options` header is needed. The two options the spreadsheet depends on -
`codec.tabular.referenceMode=SQL_TABLES`, which turns each containment list into a sheet, and
`codec.serializeDefault=true`, without which every value equal to its default arrives as an empty
cell - are pinned on the content endpoint. Whether a compliance document shows its own default values
is not a caller's decision, and `codec.serializeDefault` could not come from the header anyway.

`Accept: application/vnd.oasis.opendocument.spreadsheet` works in place of `?mediaType=`. Drop both
and you get the JSON.

To find the documents a scope holds:

```bash
curl 'http://localhost:8080/atlas/rest/jena/registries/gdprdoc/stages/draft' -H 'Accept: application/json'
```

### The two save options are mandatory, not cosmetic

| option | value | without it |
|---|---|---|
| `codec.tabular.referenceMode` | `SQL_TABLES` | the containment lists are not emitted at all: **one sheet, none of the content** |
| `serializeDefault` | `true` | every value equal to its feature default is omitted, so `UNCHANGED` and `changeCount=0` render as empty cells |

**Mind the spelling.** In the `Codec-Options` header the tabular option is prefixed
(`codec.tabular.referenceMode`) and the core one is **not** (`serializeDefault`). The whitelist of
client-overridable keys is built from `ConfigProperty.getKey()`, which is bare, while the module
options carry their `codec.` prefix. A header sending `codec.serializeDefault=true` is not rejected —
non-whitelisted keys are silently ignored — it simply comes back with the default values missing.

## Configuration

Two PIDs, plus the registry the document lives in. Shown here from
`docker/dockercompose/configs/jena.json`, the config the jena image mounts. The same three
entries live in `org.eclipse.fennec.model.atlas.runtime.config.local.jena/configs/workflow.json`
for the local runtime and in
`org.eclipse.fennec.model.atlas.runtime.config.docker.file/configs/workflow.json`, which is baked
into the file image rather than mounted — change one and the others do not follow.

The file image does not hardcode the scope. The `jena` in `trigger.scopes` and `scope.target`
below, and the scope of the `ModelAtlasObjectPublisher~gdprStatus` publisher and of the
`GDPRAtlasRequestStatusStore`, are all
`$[env:GDPR_ATLAS_SCOPE;default=$[prop:GDPR_ATLAS_SCOPE;default=jena]]` there, so a deployment
with a differently named tenant scope sets one variable. It must match `GDPR_ATLAS_SCOPE` on the
fennec-gdpr MCP half, which reads the model under review from that scope and seals the report into
it. The scope itself still has to exist and bind `gdpr` and `gdprdoc`; the image only defines
`jena`.

```jsonc
"GDPRReportHistoryStageAction": {
    "reports.registry": "gdpr",              // where the GdprReport objects are
    "report.stages": ["draft", "approved", "release"],  // every stage holding reviews; each triggers
    "trigger.scopes": ["jena"],              // empty means every scope (see below)
    "scope.target": "(atlas.scope=jena)",
    "document.registry": "gdprdoc"           // where the derived document goes; the stage is the
                                             // stage of the reviews, not a configured one
},
"RegistryService~gdprdoc": {
    "registry.name": "gdprdoc",
    "registry.type": "OTHER",
    "root.eclass.uri": ["https://org.eclipse/fennec/gdpr-report-history/1.0.0#//GdprReportHistory"],
    // Derived: the atlas builds these itself, so the service API may rewrite one even in a final
    // stage. A document lives in the stage its reviews were carried out at, which may be that one.
    "derived.eclass.uri": ["https://org.eclipse/fennec/gdpr-report-history/1.0.0#//GdprReportHistory"],
    "schemaPackage.target": "(emf.nsURI=https://org.eclipse/fennec/gdpr-report-history/1.0.0)",
    "stages": [
        { "name": "draft",   "writable": true, "final": false },
        { "name": "release", "writable": true, "final": true }
    ],
    ...
}
```

and, on the registry holding the reviews:

```jsonc
"RegistryService~gdpr": {
    "stageActionService.target": "(component.name=GDPRReportHistoryStageAction)",
    ...
}
```

Four constraints are easy to get wrong, and three of them fail silently:

- **The reports registry must name the action.** A registry's `stageActionService` reference
  defaults to `(scope=no-inject)`, which matches nothing. Leave the target out and the action
  activates, logs that it is ready, and never fires.
- **Do not add `stageActionService.cardinality.minimum` to it.** The action needs the scope service,
  the scope needs the registry, and pinning the reference closes that ring: none of the three
  activates, and the only symptom is a scope service that never appears. The registry comes up
  without the action and binds it when it appears; because the action replays on startup, that bind
  picks up anything stored in the meantime.
- **The document registry needs the stages of the registry it describes**, and its document EClass
  declared `derived.eclass.uri`. A document is written into the stage its reviews were carried out
  at; a stage the registry does not have fails the write, and a final stage refuses *updates*, so
  without the derived declaration the first document is accepted and every rebuild of it is
  rejected.
- **`trigger.scopes` is the action's own filter.** The workflow dispatches on object type, stage and
  event only, and a registry instance is shared by every scope that binds it, so without this filter
  the action answers for all of them. Empty means every scope, which is what a single-scope
  deployment wants; name them as soon as a second scope exists.

## What it deliberately does not do

- **It does not reconcile.** The document records what changed; merging a human correction with a
  fresh agent review is out of scope.
- **It does not keep the spreadsheet.** Nothing is written to disk; the ODS is a representation. A
  mail attachment or a DCAT distribution would be a separate exporter.
- **It does not guess about a deleted report.** A promotion is an exit, and there the report is still
  readable in the stage it moved to, so the rebuild finds it. A report that is really gone cannot be
  traced back to a subject from its id alone; that case is logged, and the document catches up when
  the subject is next reviewed.
- **A second finding on a feature widens a cell rather than adding a row.** Rows are keyed by
  classifier or feature, not by finding — keying by finding would turn a category raised from
  `PERSONAL_DATA` to `SPECIAL_CATEGORY` into a removal plus an addition, which is the thing the
  change sheet exists to avoid.

## Deployment

The feature is optional and ships only in the runtimes that ask for it. In the jena runtime it needs:

| bundle | why |
|---|---|
| `org.eclipse.fennec.model.atlas.gdpr.history` | the stage action and the builder |
| `org.eclipse.fennec.gdpr.report.model` | the `GdprReport` EPackage |
| `org.eclipse.fennec.gdpr.report.history.model` | the `GdprReportHistory` EPackage |
| `org.eclipse.fennec.codec.ods` | the spreadsheet; pulls `codec.tabular`, `codec.tabular.model` and SODS |

The bundle depends on nothing in `fennec-gdpr`, so a deployment where a human uploads a report by
hand — with no AI review half — needs only these four.
