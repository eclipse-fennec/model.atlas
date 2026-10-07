# GDPR review findings on the reviewed model

A GDPR review produces a `ComplianceReport` in the atlas. Nothing on the **reviewed model** says so: open
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

One node per reviewed element, and one child per claim — several findings on one feature are
several signals for one verdict, so they fold into a single claim. A finding may **opt out** of that
by declaring its own node code, as an id of the form `<code>:<what it is about>` with a code under
`gdpr.`: a derived report's findings on one flow come from different rules and each states something
of its own, so folding them would run two statements into one paragraph and, where they share a
category and a relevance, mint one id for both. A review's findings declare nothing and behave
exactly as they always have.

The reviewed object need not be a metamodel. A compiled transformation gets a report too - derived
rather than written, from the reviews of the metamodels it was compiled against - and a second
instance of this same action puts those findings onto the compiled unit. See
[a GDPR report for a transformation](gdpr-transformation-reports.md).

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
on the order the analyser listed them in. Its message names how the combination combines — the
category ref whose taxonomy is `combination-kinds`, e.g. `LINKAGE`.

**Every node names the report it came from**, in `Diagnostic.source`: the object id of the review in
the report registry. The model that wrote the review, when it ran, its origin and the findings
themselves are all in that object, so naming it keeps them one fetch away instead of copying one of
them into the cell and losing the rest. `source` is not an identity — the id is derived from
producer, code and target — so a later review restating the same claim keeps the diagnostic, its
`createdTime` and its history, and only moves `source` onto itself.

**A leaf per claim**, coded `gdpr.finding.<CATEGORIES>.<RELEVANCE>`, carrying the review's own
words: the rationale, then the recommendation, the citations and the confidence. A finding names its
categories as refs into the `data-categories` taxonomy of the context the review ran against; one
category gives `gdpr.finding.SPECIAL_CATEGORY.HIGH`, several are joined **sorted** with `+` so the
code does not depend on the order a reviewer listed them in, and a finding that names none at a
relevance above `NONE` is written as `gdpr.finding.HIGH` rather than dropped. A ref from any other
taxonomy is not part of the code.

**Severities.** `MEDIUM` and `HIGH` are `WARNING`, `LOW` is `INFO`, and **no finding is ever an
`ERROR`**. The report's own disclaimer says it flags features needing human review and must not
assert compliance or non-compliance; `ERROR` means the check did not run, which would misstate what
it is. Nothing is lost by collapsing `MEDIUM` and `HIGH` onto one severity — the relevance is part
of the leaf's code, and therefore of its identity. `ERROR` is reserved for the two nodes that say
the check did *not* run: `gdpr.unreviewed-source` and `gdpr.no-review`.

**A finding that asserts nothing is not written**: `relevanceLevel` `NONE`, or every one of its
data categories in the benign set. That set is a component property of the two stage actions,
defaulted to the GDPR context's `NOT_PERSONAL_DATA` and `ANONYMOUS`; a deployment reviewing against
another context names its own ids there rather than needing code. A review does not record
cleanliness as a finding anyway — it records it by holding an evaluation with no findings — so this
is mostly a guard against a contradictory one.

### Silence is never the answer

A review that found nothing still writes, because an object nobody has reviewed and an object
reviewed and cleared must not look the same. Four states, and each one is readable off the object:

| On the object | What it means |
|---|---|
| no `gdpr.review` producer at all | nobody has reviewed this revision |
| one `gdpr.review` root, `INFO`, no children | reviewed, nothing of concern; the message names how many elements were examined |
| one `gdpr.review` root with children | reviewed, and this is what it found |
| one `gdpr.no-review` root, `ERROR` | there *was* a review and it was withdrawn, so nothing checks this revision now |

The fifth is the degenerate one: a report that asserts something but names no element any of it
could be written onto yields a `WARNING` root saying so, rather than reading as clean. It means the
report is malformed; the dropped findings are logged.

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
nothing of atlas topology. Run one instance per target registry, and tell each one which kind of
subject it answers for with `subject.type`.

**The write is guarded.** The fingerprint has to be present at that address before anything is
written, and a miss is logged and dropped rather than searched for elsewhere:

```
No object with fingerprint 'fp1:b55ae564…' is in stage 'draft' of registry 'schema' in scope 'dimcity',
so the GDPR review's findings were not written. They land when the reviewed object and its review
are in the same stage.
```

That is the line to look for when findings do not appear: it almost always means the report and the
model are in different stages. Configuration naming the wrong registry would otherwise not write
nothing — it would write one stage's verdict onto another stage's object, which looks like a result.

A read-only stage is **not** a reason to skip. Diagnostics may be written where content is frozen,
so a review of a released model is recorded on the released model.

**Two instances cannot write onto each other's objects**, although every report does reach both. An
instance answers only for the kind of subject its `subject.type` names — `PackageSubject` or
`TransformationSubject` — and stops at the report otherwise, before it looks in any registry. That
is the rule; the fingerprint schemes say the same thing a second time, since an `fp1:` value matches
nothing in the transformations registry and an `m2x1:` nothing in the schema one, but that is a
property of the digests rather than something anybody wrote down, and it is not what the separation
rests on.

It also keeps the miss above worth reading. Without the filter every instance logged that line for
every report of somebody else's kind, and the one case it exists for — the reviewed object and its
review sitting in different stages — was one line among many.

Leaving `subject.type` unset answers for every kind. That is right only where a single registry
holds everything reviewable; a value that names no subject of the report model fails the
configuration outright rather than leaving an instance that answers for nothing and says nothing
about it.

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
  still stands, in which case that one's findings are written instead. When none is left, the
  producer does not go quiet — it writes the `gdpr.no-review` root, because an object whose review
  was withdrawn has not been checked and must not look like one that was checked and cleared. Only
  this producer's roots are rewritten; another producer's findings on the same object are untouched.
  What happens when the runtime cannot tell which object the withdrawn report was about is below.
- **A promotion** needs nothing. A transition carries the same `ObjectMetadata` into the target
  stage, diagnostics included, so a promoted object keeps its findings without anything being
  rewritten.
- **A restart** replays every stored report, so a review that landed while the runtime was down is
  picked up. The diagnostics it produces are identical — the ids are derived — so the replay is not
  even a change. The replay is also what refills the runtime's memory of which subject each report
  reviewed, which is why a withdrawal after a restart usually behaves exactly like one before it.

### When the runtime does not remember the report

The link from a report to the object it was written onto is kept in memory, and a delete carries an
object id and nothing else — the id does not name a subject. So a report the runtime has no record
of is one it cannot clear by looking up. That happens when the report's ENTER never succeeded: a
replay that failed leaves no record, and the withdrawal then has nothing to work from.

The objects themselves are the durable record. Each one this producer wrote onto carries its roots
under `gdpr.review` and knows its own fingerprint, so when there is nothing in memory the stage is
read instead and **every object in it carrying this producer's findings is recomputed**. The same
holds for `gdpr.transformation/<name>`, where the producer additionally names the transformation, so
the models and the qualified name are both recovered from what stands on them.

Recomputing rather than clearing is what makes the sweep safe: each object is rewritten from the
reviews that are still readable, so one whose review stands is written what it already had, and one
whose last review has gone gets `gdpr.no-review`. It cannot invent a finding, and it repairs
whatever was missed while nothing was listening.

**It heals the stage the withdrawal happened in, and only that one.** A review describes the stage
it was carried out against, so the rewrite looks in the stage the deleted report was in — a stale
finding left in another stage waits until something is deleted from *that* stage. A review in
`draft` withdrawn while nothing was listening is not repaired by a later deletion in `approved`.

## When a person is asked to look again

A leaf's id turns on the **element, the categories and the relevance**. Everything above it in the
tree is fixed for this producer, so:

- the same claim reached by another route — a reworded rationale, one more `detectedBy`, a
  different `Finding.id` — is the **same** diagnostic. It keeps its id, and with it an
  `ACKNOWLEDGED` or `RESOLVED` a person set.
- a changed category set, or a changed relevance, is a **different** claim: a new id, status `OPEN`,
  and the old one disappears with the rewrite. A judgement that moved is one a person has to look
  at again.

`Finding.id` deliberately plays no part: the analyser assigns it afresh on every run (`F-001` in one
review, `f1` in the next for the same claim), so keying on it would make every diagnostic look new
every time.

Several findings that share an element, its categories and a relevance **fold into one leaf**, their
rationales, recommendations and citations merged and the least certain confidence kept. They differ
in how the review reached the claim, not in what it asserts, and there is one decision to make about
it. Two findings of *different* category sets on one feature stay two leaves — as do `PERSONAL_DATA` at
`HIGH` and at `MEDIUM` on the same feature, which a live review does produce.

`Finding.diagnosticId` is left unset by this path. The id is derivable from values a reader of the
report already holds, and writing it back would mean editing a sealed report — whose own history
document would then record the edit as a new revision.

## Configuration

One factory configuration per target registry, plus the gdpr registry having to name the action.
Shown from
`docker/dockercompose/configs/jena.json`, the config the jena image mounts. The same entries live in
`org.eclipse.fennec.model.atlas.runtime.config.local.jena/configs/workflow.json` for the local
runtime and in `org.eclipse.fennec.model.atlas.runtime.config.docker.file/configs/workflow.json`,
which is baked into the file image rather than mounted — change one and the others do not follow.
The file image takes its scope from `MODEL_ATLAS_SCOPE`, as
[the history document's configuration](gdpr-review-history.md#configuration) describes in full.

```jsonc
"GDPRMetadataDiagnosticsStageAction~schema": {
    "target.registry": "schema",             // where the reviewed objects are. The STAGE is never
                                             // configured - it is the stage the report is in
    "subject.type": "PackageSubject",        // and which reports those are. Every report reaches
                                             // every instance; this is what keeps them apart
    "report.stages": ["draft", "approved", "release"],  // the report stages this answers for
    "trigger.scopes": ["dimcity"],           // empty means every scope that binds the registry
    "scope.target": "(atlas.scope=dimcity)",
    "stage.action.name": "GDPRMetadataDiagnosticsStageAction~schema"
},
"GDPRMetadataDiagnosticsStageAction~transformations": {
    "target.registry": "transformations",    // the same action again, for reviews of compiled units
    "subject.type": "TransformationSubject",
    "report.stages": ["draft", "approved", "release"],
    "trigger.scopes": ["dimcity"],
    "scope.target": "(atlas.scope=dimcity)",
    "stage.action.name": "GDPRMetadataDiagnosticsStageAction~transformations"
},
"RegistryService~gdpr": {
    // Without naming the action here the registry binds none and dispatches nothing: the action
    // activates, logs its configuration, and never fires. The reference defaults to
    // (scope=no-inject), so this is a filter over several actions - edit it, do not replace it.
    "stageActionService.target":
        "(|(component.name=GDPRReportHistoryStageAction)(component.name=GDPRMetadataDiagnosticsStageAction))"
}
```

**Both instances bind to the gdpr registry, not to the registry they write into.** The trigger is a
report arriving, and reports live in the gdpr registry; `target.registry` only says where the
findings are then written. So the filter above needs no edit for a second instance — DS gives every
component configuration the same `component.name` — and the transformations registry keeps naming
only its own actions.

**One factory configuration per instance** (`PID~name`), never a plain `PID` entry alongside them.
DS reads a component's configuration PID either as a singleton or as a factory PID, and what a
runtime does when both exist for one PID is not something the specification settles
(§112.7). Each instance also names itself with `stage.action.name`, because the runtime records
what a stage action made of an event under the producer `stage-action/<name>` and two instances
sharing a name would overwrite each other's record on the report.

A runtime without a transformations registry — the local jena runtime, today — configures the
`~schema` instance only. An instance whose target registry does not exist has nothing to resolve
into.

`stage.action.chains` is deliberately left unset. The default ranking order with
`onFailure: continue` is what keeps the two report actions independent; do not put them in a chain
that stops on failure.

The configuration is `REQUIRE`, so a runtime that has not asked for review findings on its models
does not start writing them by having the bundle installed.

## Reading them

```bash
# everything this producer says about one model
curl -s "http://localhost:8080/atlas/rest/dimcity/registries/schema/stages/approved?objectId=<objectId>" \
  | jq '.diagnostics[] | select(.producer=="gdpr.review")'

# every reviewed object in a stage, one line per finding
curl -s "http://localhost:8080/atlas/rest/dimcity/registries/schema/stages/approved" \
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
  about diagnostics. It does not change `report.ecore` either: scope and stage stay out of a
  shared model.
- **It does not write to more than one address.** A fingerprint held in two stages is two objects,
  and one stage's review says nothing about the other's copy.
- **It does not itself review anything about a transformation.** A compiled unit now carries its
  `m2x1:` fingerprint in `ObjectMetadata.fingerprint`, so the `~transformations` instance resolves a
  `TransformationSubject` and writes its findings like any other — but something has to produce
  those reports first (issue #319). Until then the instance sees no report it answers for.

## Deployment

Optional, and ships only in the runtimes that ask for it:

| bundle | why |
|---|---|
| `org.eclipse.fennec.model.atlas.gdpr.diagnostics` | the stage action and the mapper |
| `org.eclipse.fennec.compliance.report.model` | the `ComplianceReport` EPackage |

It depends on nothing in `fennec-gdpr`, so a deployment where a human uploads a report by hand —
with no AI review half — needs only these two.
