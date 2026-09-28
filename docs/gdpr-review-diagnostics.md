# GDPR review findings on the reviewed model

A GDPR review produces a `GdprReport` in the atlas. Nothing on the **reviewed model** says so: open
the schema registry and the package that was found to hold special-category data looks exactly like
one that was found clean. You have to know a report exists, and where, before you can learn that
the model needs looking at.

This closes that gap. When a report lands, the findings it holds are written onto the metadata of
the model the review is about, as [diagnostics](user-guide.md#diagnostics) under the producer
`gdpr.review`. They travel in every listing and every metadata `GET` of that stage, so a client
sees what a review found before it uses the model.

It moves what the review already decided, and decides nothing itself. Every diagnostic is a
projection of a `Finding` that already exists, and the mapping is a table rather than a second
opinion.

It is a sibling of [the review history document](gdpr-review-history.md), not a part of it: both
run off the same event, each with its own output. A deployment may want either without the other,
and a failing metadata write never costs a document rebuild.

## What it writes

One root for the producer, one node per reviewed element, one leaf per claim:

```
gdpr.review                                           WARNING   (no target)
  GDPR review: 21 findings on 19 elements
├── gdpr.classifier      //Patient                    WARNING
│     PERSONAL_DATA (HIGH)
│   └── gdpr.finding.PERSONAL_DATA.HIGH
│         Die Klasse Patient repräsentiert laut Dokumentationsannotation eine natürliche Person …
├── gdpr.feature         //Patient/denomination       WARNING
│     SPECIAL_CATEGORY (HIGH)
│   └── gdpr.finding.SPECIAL_CATEGORY.HIGH
│         Das Attribut denomination verweist auf das Enum Denomination, dessen Literale … | Evidence: Art.9(1) | Confidence: HIGH
└── gdpr.combination     //Patient/birthDate+//Patient/houseNumber+//Patient/postalCode   WARNING
      QUASI_IDENTIFIER_SET: QUASI_IDENTIFIER (HIGH)
    └── gdpr.finding.QUASI_IDENTIFIER.HIGH
          Die Kombination von Geburtsdatum, Postleitzahl und Hausnummer bildet ein Quasi-Identifikator-Set …
```

**One root, because an object has several producers.** Gate refusals, schema dependencies and the
runtime's own `stage-action/<name>` records all land in the same list. A reader wants one collapsed
row per producer and the choice of which to open; a review spread over one root per element buries
the others among them. So everything this producer says hangs under a single node, which carries no
target — the review is about the object as a whole — the worst severity anywhere beneath it, and a
count of what there is.

**A node per element.** `gdpr.classifier` targets a classifier's `uriFragment` (`//Patient`),
`gdpr.feature` a feature's (`//Patient/denomination`), `gdpr.flow` a transformation's path
(`mapping:<nsURI>#<source>-><nsURI>#<target>`, since a flow has no fragment of its own), and
`gdpr.combination` the elements a `CombinationFinding` spans, sorted so the address does not depend
on the order the analyser listed them in. Its message names the `combinationKind`.

**A leaf per claim**, coded `gdpr.finding.<CATEGORY>.<RELEVANCE>`, carrying the review's own words:
the rationale, then the recommendation, the citations and the confidence.

**Severities.** `MEDIUM` and `HIGH` are `WARNING`, `LOW` is `INFO`, and a review **never** produces
an `ERROR`. The report's own disclaimer says it flags features needing human review and must not
assert compliance or non-compliance; `ERROR` means the check did not run, which would misstate what
it is. Nothing is lost by collapsing `MEDIUM` and `HIGH` onto one severity — the relevance is part
of the leaf's code, and therefore of its identity.

**A finding that asserts nothing is not written**: `relevanceLevel` `NONE`, or a category of
`NOT_PERSONAL_DATA` or `ANONYMOUS`. A review does not record cleanliness as a finding anyway — it
records it by holding an evaluation with no findings — so this is mostly a guard against a
contradictory one. The consequence is the useful part: **an object with no `gdpr.review` root is
one the review found nothing on.**

## Which object, and in which stage

The report names its subject by **fingerprint**, and that is how the model is found — never by
nsURI, never by objectId. A fingerprint is the identity of a *revision*: a review is of content, so
the same bytes reached by any route are the same subject, while an objectId is opaque and a
delete-and-re-upload of the same nsURI yields a new one.

**Exactly one address is ever written, and its stage is the report's own.** A review is
stage-specific: a model reviewed in `approved` gets a report about `approved`, and that verdict
does not describe the copy in `draft`. The gdpr registry's stages mirror the reviewed registry's, so
the target stage is simply the stage the report is in. Only the target **registry** is configured,
because a `PackageSubject` resolves into the schema registry and a `TransformationSubject` into the
transformations one, and the report deliberately does not say which — it is a shared model and knows
nothing of atlas topology. Run one instance per target registry.

**The write is guarded.** The fingerprint has to be present at that address before anything is
written, and a miss is logged and dropped rather than searched for elsewhere:

```
No object with fingerprint 'fp1:b55ae564…' is in stage 'draft' of registry 'schema' in scope 'jena',
so the GDPR review's findings were not written. They land when the reviewed object and its review
are in the same stage.
```

That is the line to look for when findings do not appear: it almost always means the report and the
model are in different stages. Configuration naming the wrong registry would otherwise not write
nothing — it would write one stage's verdict onto another stage's object, which looks like a result.

A read-only stage is **not** a reason to skip. Diagnostics may be written where content is frozen,
so a review of a released model is recorded on the released model.

Two instances cannot write onto each other's objects even if both are triggered by one report: the
fingerprint schemes are self-describing, so an `fp1:` value never matches anything in the
transformations registry and an `m2x1:` never matches anything in the schema registry.

## Two reviews of one revision

One revision is reviewed more than once as a matter of course — an agent's review and a person's
correction are exactly what the history document holds as two revisions. The object shows **the
latest** of them: on every event the action gathers every report in the stage naming that
fingerprint and writes the findings of the most recent, ordered by `generatedAt`, then the report
metadata's `lastChangeTime`/`uploadTime`, then the objectId as a tie-break.

It deliberately does not map "the report that fired". That would make the object show whichever
report was written about last — which on the startup replay is whichever the registry happens to
list last, not the newest — and it would make withdrawing any one review erase what all the others
said, because a producer's roots are replaced as a set.

## When a review changes or goes away

- **A re-review** rewrites the producer's tree. A claim that comes back under the same id keeps its
  life, including a status a person set; see below.
- **A withdrawn review** takes its findings with it, unless another review of the same revision
  still stands, in which case that one's findings are written instead. Only this producer's roots
  go; another producer's findings on the same object are untouched.
- **A promotion** needs nothing. A transition carries the same `ObjectMetadata` into the target
  stage, diagnostics included, so a promoted object keeps its findings without anything being
  rewritten.
- **A restart** replays every stored report, so a review that landed while the runtime was down is
  picked up. The diagnostics it produces are identical — the ids are derived — so the replay is not
  even a change.

## When a person is asked to look again

A leaf's id turns on the **element, the category and the relevance**. Everything above it in the
tree is fixed for this producer, so:

- the same claim reached by another route — a reworded rationale, one more `detectedBy`, a
  different `Finding.id` — is the **same** diagnostic. It keeps its id, and with it an
  `ACKNOWLEDGED` or `RESOLVED` a person set.
- a changed category, or a changed relevance, is a **different** claim: a new id, status `OPEN`,
  and the old one disappears with the rewrite. A judgement that moved is one a person has to look
  at again.

`Finding.id` deliberately plays no part: the analyser assigns it afresh on every run (`F-001` in one
review, `f1` in the next for the same claim), so keying on it would make every diagnostic look new
every time.

Several findings that share an element, a category and a relevance **fold into one leaf**, their
rationales, recommendations and citations merged and the least certain confidence kept. They differ
in how the review reached the claim, not in what it asserts, and there is one decision to make about
it. Two findings of *different* categories on one feature stay two leaves — as do `PERSONAL_DATA` at
`HIGH` and at `MEDIUM` on the same feature, which a live review does produce.

`Finding.diagnosticId` is left unset by this path. The id is derivable from values a reader of the
report already holds, and writing it back would mean editing a sealed report — whose own history
document would then record the edit as a new revision.

## Configuration

One PID, plus the target registry having to name the action. Shown from
`docker/dockercompose/configs/jena.json`, the config the jena image mounts. The same entries live in
`org.eclipse.fennec.model.atlas.runtime.config.local.jena/configs/workflow.json` for the local
runtime and in `org.eclipse.fennec.model.atlas.runtime.config.docker.file/configs/workflow.json`,
which is baked into the file image rather than mounted — change one and the others do not follow.
The file image takes its scope from `MODEL_ATLAS_SCOPE`, as
[the history document's configuration](gdpr-review-history.md#configuration) describes in full.

```jsonc
"GDPRMetadataDiagnosticsStageAction": {
    "target.registry": "schema",             // where the reviewed objects are; one instance per
                                             // registry. The STAGE is never configured - it is the
                                             // stage the report itself is in
    "report.stages": ["draft", "approved", "release"],  // the report stages this answers for
    "trigger.scopes": ["jena"],              // empty means every scope that binds the registry
    "scope.target": "(atlas.scope=jena)"
},
"RegistryService~gdpr": {
    // Without naming the action here the registry binds none and dispatches nothing: the action
    // activates, logs its configuration, and never fires. The reference defaults to
    // (scope=no-inject), so this is a filter over several actions - edit it, do not replace it.
    "stageActionService.target":
        "(|(component.name=GDPRReportHistoryStageAction)(component.name=GDPRMetadataDiagnosticsStageAction))"
}
```

`stage.action.chains` is deliberately left unset. The default ranking order with
`onFailure: continue` is what keeps the two report actions independent; do not put them in a chain
that stops on failure.

The configuration is `REQUIRE`, so a runtime that has not asked for review findings on its models
does not start writing them by having the bundle installed.

## Reading them

```bash
# everything this producer says about one model
curl -s "http://localhost:8080/atlas/rest/jena/registries/schema/stages/approved?objectId=<objectId>" \
  | jq '.diagnostics[] | select(.producer=="gdpr.review")'

# every reviewed object in a stage, one line per finding
curl -s "http://localhost:8080/atlas/rest/jena/registries/schema/stages/approved" \
  | jq -r '.metadata[] | select(.diagnostics) | . as $m | .diagnostics[]
           | select(.producer=="gdpr.review")
           | "\($m.properties.nsUri)\t\(.severity)\t\(.message)"'
```

`status` is absent from the JSON rather than showing `OPEN`: EMF omits a value equal to the
default. Absent means `OPEN`.

## What it deliberately does not do

- **It does not summarise.** There is no condensed copy of the findings in `ObjectMetadata`; the
  whole tree is stored, and a reader that only wants the badge reads the root. `diagnostics` is a
  containment reference, so a listing of a stage pays for every object's tree — if that ever
  matters, the fix is a projection on the listing endpoint, not a second, lossy model.
- **It does not decide anything the review did not.** No severity is inferred, no finding is
  merged across elements, no claim is invented from a clean evaluation.
- **It does not touch `fennec-gdpr`,** and it does not make the review depend on the atlas knowing
  about diagnostics. It does not change `gdpr-report.ecore` either: scope and stage stay out of a
  shared model.
- **It does not write to more than one address.** A fingerprint held in two stages is two objects,
  and one stage's review says nothing about the other's copy.
- **It does not yet work for transformations.** `ObjectMetadata.fingerprint` is filled for
  EPackages only, so an `m2x1:` subject resolves to nothing and the action logs a skip. Nothing
  here changes when that is fixed — it compares strings, and the schemes are self-describing.

## Deployment

Optional, and ships only in the runtimes that ask for it:

| bundle | why |
|---|---|
| `org.eclipse.fennec.model.atlas.gdpr.diagnostics` | the stage action and the mapper |
| `org.eclipse.fennec.gdpr.report.model` | the `GdprReport` EPackage |

It depends on nothing in `fennec-gdpr`, so a deployment where a human uploads a report by hand —
with no AI review half — needs only these two.
