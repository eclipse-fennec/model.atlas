# A GDPR report for a transformation, derived rather than reviewed

A metamodel is reviewed. A transformation is **derived**: the atlas walks the compiled unit, works
out which source field reaches which target field and how, and applies a fixed table of rules to
what the metamodels' own reviews already say about those fields. No agent, no provider key, no
network, and no compliance context read: the categories and the citations are the reviews' own.

The case it exists for:

> The target model has a `comment` field that is not GDPR-relevant on its own. The transformation
> maps `fullName`, `postcode` and `diagnosis` into it. **That** is the finding.

Nobody reviewing either model alone can see it. The clinic review says those three fields are
personal data, two of them quasi-identifying and one of them health data; the contacts review says
`comment` is an ordinary string. Only the transformation knows the three meet, and only there is it
visible that a field-level classification has been folded into something that cannot be classified,
minimised or erased per field.

It is a sibling of [the review findings on the reviewed model](gdpr-review-diagnostics.md) and
[the review history document](gdpr-review-history.md): three stage actions of one family, each with
one output, none a part of another.

## What happens, and when

A compiled unit entering a stage of the transformation registry triggers the analysis. The unit's
manifest pins every metamodel it was compiled against to an exact revision by `fp1:` fingerprint,
and that value is byte-identical to the one the schema registry computed — so finding the review of
the exact model revision the unit was built against is a lookup, not a heuristic.

**The metamodels need not be in the transformation's stage.** Package visibility runs along a
ladder — each stage of a schema registry sees the next one, and the last sees the parent scope — so
a transformation in `draft` routinely compiles against a metamodel that lives in `approved`. A
review is filed in the stage of the model it reviewed, so looking only where the transformation is
would report such a metamodel as unreviewed: not merely incomplete but **false**, because the review
of exactly those bytes exists one stage up.

```
SourceUnit uploaded ──▶ QvtStageActionService ──▶ CompiledUnit stored
                                                    │  metadata.fingerprint = m2x1:…
                                                    ▼
                                        QvtGdprFlowStageAction
                                          │  manifest.packageEntry → fp1: per metamodel
                                          │  each fp1: → that model's review, or no report at all
                                          │  walk the AST, apply the rules, inherit the evidence
                                          ▼
                                        ComplianceReport(TransformationSubject) ──▶ gdpr registry
                                          │
           ┌──────────────────────────────┼──────────────────────────────┐
           ▼                              ▼                              ▼
GDPRMetadataDiagnosticsStageAction  GDPRReportHistoryStageAction  QvtFlowFindingsStageAction
~transformations                    the transformation's          producer
producer gdpr.review                document, one row per         gdpr.transformation/<name>
the whole report onto the           compiled revision             half of it onto the SOURCE
COMPILED UNIT                                                     and TARGET METAMODELS
```

The first two needed no change: a transformation report stored in the gdpr registry is picked up by
machinery that was already there.

**The ordering is the rule, not an implementation detail.** The analyser writes a report; every
diagnostic anywhere is derived from a stored report. Nothing writes a finding that is not recorded
in an artefact somebody can read — which also means a report written by hand, or restored from a
backup, produces the same diagnostics as one the analyser just made.

## What the report says

The subject is the compiled unit, by its `m2x1:` fingerprint, and it lists every metamodel the
analysis rested on, split by whether the transformation reads it or writes it:

```xml
<subject xsi:type="TransformationSubject" qualifiedName="clinic2contacts" language="qvto"
         subjectFingerprint="m2x1:175c17b6…">
  <sourcePackages nsURI="http://example.org/clinic/1.0.0"
                  subjectFingerprint="fp1:5b87b0c6…" reportId="gdpr-fp1-5b87b0c6-…"/>
  <targetPackages nsURI="http://example.org/contacts/1.0.0"
                  subjectFingerprint="fp1:0bbb2a33…" reportId="gdpr-fp1-0bbb2a33-…"/>
</subject>
<contexts contextId="gdpr" contextVersion="20160504"/>
```

Every `reportId` here is set — that is the gate, not a coincidence; see
[Where nothing was reviewed, there is no report](#where-nothing-was-reviewed-there-is-no-report).
The `contexts` are carried over from those reviews, never chosen here.

Each flow becomes a `FlowEvaluation` — one source feature, one target feature, one mapping, and how
the value travels — and the findings hang off those:

| rule | fires when |
|---|---|
| `PROPAGATION` | a classified source feature reaches a target feature; the target inherits at least that category and its own review does not know |
| `AGGREGATION` | two or more classified features reach **one** target feature. Classification, pseudonymisation and erasure all operate per field |
| `STRUCTURE_LOSS` | classified data arrives as prose: a string field whose name says it holds free text, or any string field several values were concatenated into |
| `TARGET_DISAGREEMENT` | the target review classifies the receiving field **more weakly** than what arrives in it — any weaker category, not only `NOT_PERSONAL_DATA` |
| `PURPOSE_NOT_CARRIED` | the source review states a purpose for the field and the target field states none. A purpose is per processing, and the transformation is a new processing |
| `NOT_PROPAGATED` | classified features no mapping reads. Positive evidence for a data-minimisation argument, emitted as **one** finding per metamodel |
| `OPAQUE_FLOW` | the analyser could not follow the value, or could not resolve where it lands |

`AGGREGATION`, and the statements about a target field that several flows made true, are
`CombinationFinding`s over the flows rather than findings on any one of them — they are true of the
flows together and of none of them alone.

**A finding's id declares what kind of statement it is**, as `<code>:<what it is about>`. The report
model has no field for the rule — a `Finding` carries a category and a rationale, not the reason it
was raised — so the id is where a producer says it and a reader reads it. It decides two things: the
node a finding becomes on the compiled unit, and which end of a flow it is written onto. A review's
findings declare nothing (`F-001` and the like) and keep folding into one claim per element, which
is what folding was written for.

## What the metamodels learn

The report lands on the compiled unit, which is what it is a report of. But nobody maintaining the
contacts model will ever open a transformation's report, and the thing they most need to know is
only in there. So half of what the report says is written onto the **models** too, under the
producer `gdpr.transformation/<qualifiedName>`:

```
gdpr.transformation                                              WARNING   (no target)
  clinic2contacts: 4 findings on 4 elements
├── gdpr.flow.aggregation          //Contact/comment             WARNING
│     SPECIAL_CATEGORY (HIGH)
│   └── gdpr.finding.SPECIAL_CATEGORY.HIGH
│         3 classified source features - //Patient/fullName, //Patient/postcode,
│         //Patient/diagnosis - are combined into the single field //Contact/comment …
│         | Evidence: Art.4(1), Rec.26, Art.9(1)
└── gdpr.flow.target-disagreement  //Contact/reference           WARNING
      DIRECT_IDENTIFIER (HIGH)
    └── gdpr.finding.DIRECT_IDENTIFIER.HIGH
          The target review classifies //Contact/reference as PERSONAL_DATA, but …
```

**Which half goes where** is the rule's *reach*, and it is not symmetric — the two ends would do
different things about it:

| rule | source model | target model |
|---|---|---|
| `PROPAGATION` | — | at the receiving field |
| `AGGREGATION` | — | at the receiving field |
| `STRUCTURE_LOSS` | at the field being flattened | at the receiving field |
| `TARGET_DISAGREEMENT` | — | at the receiving field |
| `PURPOSE_NOT_CARRIED` | at the field whose purpose was stated | — |
| `NOT_PROPAGATED` | the model as a whole | — |
| `OPAQUE_FLOW` | at the field whose destination is unknown | — |
| `gdpr.unreviewed-source` | — | — (the compiled unit only, and only on a report somebody stored) |

`PROPAGATION` is deliberately not written on the source: a metamodel read by several
transformations would collect one per flow of each of them, and that buries the findings that ask
for a decision. `STRUCTURE_LOSS` is the one both ends get, because the target model cannot express
what it now holds while the source model's field is being flattened into prose somewhere its owner
has no say over.

A finding whose rule the reader does not recognise — a report written by hand, or restored from a
backup — goes to **both** ends. Not knowing which end a statement belongs to is no reason to write
it nowhere.

**A field is badged with its own classification, never the set's.** A combination's category is the
category of the set — the strongest thing in it — so projecting it onto one contributing field would
say `//Patient/fullName` is health data when the clinic review says it is a name. What travels is
the field's own classification; what arrives in `//Contact/comment` is the set's. So the same
finding reads `DIRECT_IDENTIFIER (HIGH)` on `fullName`, `QUASI_IDENTIFIER (MEDIUM)` on `postcode`
and `SPECIAL_CATEGORY (HIGH)` on both `diagnosis` and the receiving field.

### The producer names the transformation

`gdpr.transformation/clinic2contacts`, and never `gdpr.review`. A producer's roots are replaced as
a set, so writing under `gdpr.review` would erase the model's *own* review — the write succeeds,
the event fires, and the findings are simply gone. One producer for all transformations has the
same problem one level down: a metamodel is read by many transformations, and the second to be
analysed would replace the first one's findings on it.

Named per transformation, each owns its roots on every model it touches. Re-analysing, replacing or
withdrawing one never touches another's, with no read-modify-write anywhere.

The **revision** is deliberately not in the producer but in `Diagnostic.source`. Keyed by the unit
fingerprint, every recompile would strand the previous revision's findings on the model with
nothing owning them and nothing able to clear them.

A model will therefore carry `gdpr.review` from its own review and one
`gdpr.transformation/<name>` per transformation that touches it. That is the normal case and what
the one-root-per-producer shape was built for: a reader collapses each to one row and opens the one
they want.

### Which stage a review is taken from

**Derived, not configured.** The stages searched are the transformation's own stage and every stage
the schema registry declares after it — exactly the set the compiler could resolve its metamodels
from:

```
declared: [draft, approved, release]

draft     →  approved  →  release  →  parent scope
approved  →  release   →  parent scope
release   →  parent scope
```

Visibility runs one way, and that direction is the point: work in progress may rest on released
models, released work never rests on drafts. Reading the search path off the same declaration the
chain is built from is what keeps the two from drifting — a configured list would be a second
statement of the same fact, and a second statement can be wrong. Wider than the ladder would let a
draft review speak for a released transformation; narrower would report a metamodel as unreviewed
when its review is exactly where the compiler found the model.

The stages are searched nearest first and **the first one that has a review of a fingerprint settles
it** — a stage further up never overrides a nearer one, not even with a more recent report. Within
one stage the usual rule applies and the latest review wins, because one revision is reviewed more
than once as a matter of course. Each metamodel is answered separately, so a transformation may rest
on one model's review from its own stage and another's from `release`.

The same ladder decides **which models a transformation may annotate**: the findings are written
onto the model in the nearest stage that holds its fingerprint. A `draft` transformation may
therefore annotate a released metamodel it actually read, and a released transformation can never
annotate a draft — which is the same one-way rule, not a separate policy.

A stage that no schema registry declares — one a non-schema registry added on its own — is served
too, chained onto the tail of the schema chain, so its path is itself plus the last schema stage.
And the path is narrowed to the stages the registry being searched actually has: asking a registry
for a stage it does not declare is an error rather than an empty answer, so a report registry with
fewer stages than the schema registry finds less rather than failing.

Nothing about where a review is *stored* changes: a review belongs to the stage of the subject it
reviewed, and [the review findings on the reviewed model](gdpr-review-diagnostics.md) stay
stage-local. What is dropped is only the assumption that that stage is also the transformation's.

## When a review changes, the transformations resting on it are derived again

A transformation report is only as current as the reviews it was derived from. A review that is
**corrected** changes the analysis as much as one that arrives, and a review that is **withdrawn**
changes it more: a transformation resting on a retracted review is worse than one resting on none,
because it still looks complete. So `QvtGdprReanalysisStageAction` answers for every change to a
review — `ENTER`, `UPDATE` and `EXIT` on the report registry, which is arrival including by
transition, correction in place, and deletion.

**Which transformations are stale is answered by the reports, not by the units.** A transformation
report records what it rested on: each entry of `sourcePackages` and `targetPackages` carries the
nsURI *and* the `subjectFingerprint` of the revision whose review was carried over. So the changed
review's fingerprint is looked for in the stored transformation reports, and every hit names a
transformation that is now out of date. That beats scanning the compiled units' manifests three
times over — it stays in one registry, it is the stale set exactly, and the hit's own subject gives
the unit's `m2x1:` fingerprint, so finding the compiled unit is a lookup rather than a computed id.

**And the units the gate stopped are answered for by their manifests.** A transformation whose
metamodels are not all reviewed has no report at all, so the scan above cannot see it — and it is
exactly the case this most needs to catch, because the review about to arrive may be the one that
completes it. Those units are found the other way, by scanning the manifests of the compiled units
in the same stages for the changed fingerprint. It costs a read of each unit in the stage, which is
why it is the second pass and not the only one: a transformation that already has a report is
answered for by that report. Start-up replay of the analyser covers a unit stored while the runtime
was down; it does not cover a review arriving in a running one, which is what these two passes are
for.

Which stages are searched is the **inverse of the ladder**: a review landing in `approved` makes
stale the transformations in `draft` and in `approved`, and none in `release`.

A re-analysis **writes a new report** rather than replacing the old one, because its id covers its
inputs and a changed input is a new revision of the judgement. The stage then holds both, the latest
speaks for the transformation, and the history document has two revisions to compare.

**It does not loop**, although a re-analysis writes into the very registry that triggers it: the
action answers only for a review of a *package*, so a transformation report — its own output — is
ignored outright. That a transformation's `m2x1:` subject fingerprint could never match a
`packageEntry` would also stop it, but that is a property of the digests rather than a rule, and the
termination of a loop is not something to leave resting on one.

### A note on the ids inside a report

`Evaluation.id` is an EMF **ID attribute**, and `CombinationFinding.features` is the model's only
non-containment reference — so a combination is serialised as a space-separated list of those ids.
An id containing a `#` is read back as a `<resource>#<fragment>` cross-document href rather than as
an id, and the loader then tries to instantiate the declared type to proxy it: `Evaluation`, which
is abstract. A report with a combination would store happily and be **unreadable from then on**.

The ids the analyser mints therefore use `|` as their separator and never `#` or whitespace. A
producer writing reports by hand has the same constraint.

## It cites nothing on its own authority

Every category the report asserts was asserted by a metamodel review first, and every citation is
**carried over** from the review the finding came from. The `citationId`, the `quote` and the
`sourceRef` are kept byte-identical — they were checked when that review was written — and the
analyser adds exactly one clause to the citation's `relevance`, saying how the data travels here:

```
The reviewer's relevance for //Patient/diagnosis.  ← from the clinic review, unchanged
Carried over from the review of //Patient/diagnosis, which this transformation combines with
2 other classified features into //Contact/comment.        ← the one clause the analyser adds
```

The **contexts** travel the same way. A `ComplianceReport` names the compliance contexts it was
judged against, pinned to a version, and a derived report carries over the union of the distinct
`ContextRef`s of the reviews it rested on. It never names a context of its own choosing, and it
never resolves one: a transformation is judged against exactly what its metamodels were judged
against. Since a report is derived only when every metamodel has a review, there is always at least
one to carry — which is also what makes `contexts [1..*]` satisfiable by construction.

So the analyser never looks a provision up and cannot invent one — the standing failure mode of
anything that composes a citation instead of quoting it. The other side of it is that a finding
with nothing to carry cannot be raised at all: `Finding.evidence` is mandatory, and a compliance
record must not hold a claim nobody backed.

## Where nothing was reviewed, there is no report

A report is derived **only when every metamodel of the manifest has a review** of the revision the
unit was compiled against. If one has none, nothing is derived, any report an earlier analysis
stored is withdrawn, and the compiled unit is told why:

```
gdpr.review                                                          ERROR   (no target)
  No GDPR analysis of this transformation: 1 of the 3 metamodels it was compiled against have
  no review of the revision it was compiled against, so nothing classifies the data that travels
  through them. The analysis is impossible, not clean.
└── gdpr.unreviewed-source   http://example.org/crm/1.0.0            ERROR
      http://example.org/crm/1.0.0 has no GDPR review of the revision this transformation was
      compiled against (fp1:7b81f48a…).
```

**Why the whole report and not just the missing part.** A transformation is not reviewed, it is
derived: every category it asserts was asserted by a metamodel review first. A metamodel with no
review contributes no classification, so the flows through it are still fact while nothing says
what travels along them — and a report that is silent about them reads exactly like one that found
them clean. A partial analysis of a transformation reads as an analysis, and the finding it is
missing may be the one that mattered. The flows among the *reviewed* metamodels are given up with
it; that is the deliberate price.

**Three things follow.** The statement goes on the unit under the **same producer** a derived
report's findings reach it under, `gdpr.review` — so writing it is exactly what withdraws the
findings of the last analysis that did succeed, and a later successful analysis writes over it in
turn. A report an earlier analysis stored is **deleted**, because it would otherwise go on claiming
an analysis that no longer holds and the history document would go on building revisions from it.
And deleting it is what takes the transformation's findings off the metamodels, since the action
that put them there answers a report's deletion by clearing them.

`ERROR` is not an exception to the rule that a review never produces one. That rule exists because
`ERROR` means *the check did not run* rather than *the model is bad* — and here the check genuinely
did not run. A finding about the data is at most a `WARNING`; a statement that there is no finding
to make is an `ERROR`.

The error lands on the compiled unit and never on the unreviewed metamodel. "You have no GDPR
review" is a statement about a metamodel, but it is not this transformation's business to write it
there.

`gdpr.unreviewed-source` still appears as a node on a **stored** report that lists a package with
its `reportId` unset — a report uploaded by hand or by an agent can still say that, and the
statement should still reach the object. It is simply no longer the route by which a *derived*
analysis reports a gap.

## It is deterministic, and that is what makes the history worth keeping

Every rationale and every added relevance clause is a template authored once and filled with facts
read off the unit and the reviews. The same unit and the same reviews produce byte-identical text,
so a difference between two revisions of a transformation's report document is a real change and
not a rewording — something no language model can offer.

The report's id covers the unit's fingerprint, every review the analysis rested on, and the rule
catalogue version. So:

- re-running the analysis on unchanged inputs **rewrites the same document**, which is why the
  start-up replay adds no revision;
- a corrected metamodel review **writes another report**, so the correction does not silently
  overwrite the analysis that preceded it and the change sheet has two revisions to compare.

The catalogue version (`qvt-flow-analysis/1`) is in every report's `generatedBy`, and
`origin` is `STATIC_ANALYSIS`. The catalogue is the compliance artefact: it is reviewed once and
versioned, and a report can be traced to the exact table of rules that wrote it.

## What it deliberately does not do

- **It does not review.** It derives. Where no review exists it says so rather than guessing —
  and rather than deriving the part it could.
- **Its ceiling is its rule table.** It will never fire a rule nobody wrote down, which is why
  `OPAQUE_FLOW` and an unreviewed source have to be loud — they are the only honest substitute for
  a judgement it cannot make. An absence of findings is not a statement that there is nothing to
  find, and the report's disclaimer says so.
- **It follows one hop.** Each assignment is read on its own; a value that reaches a field through
  an intermediate property or a local variable comes out as `OPAQUE` or with an unresolved target,
  never silently dropped.
- **The target field is matched by name.** The compiler turns `comment := …` into an assignment to
  a satellite variable *named* after the target feature, with no link back to it, so the target is
  looked up by that name on the mapping's result type. It is the one weak joint in the analysis; a
  name the result type does not have makes the flow `OPAQUE` rather than absent.
- **Nothing prunes a producer whose transformation was deleted without the atlas seeing it.** A
  withdrawn report clears its own findings; a runtime that never saw the delete leaves them.

## Configuration

One instance, in the runtime that has both the QVT stack and a report registry — today the jena
image. From `docker/dockercompose/configs/jena.json`:

```jsonc
"QvtGdprFlowStageAction": {
    "report.registry": "gdpr",               // where the reviews are read from and the report is
                                             // written. No stage is ever configured: the report
                                             // goes into the unit's stage, and the metamodels are
                                             // looked for along the visibility ladder from there
    "trigger.stages": ["draft", "approved", "release"],
    "trigger.scopes": ["dimcity"],           // empty means every scope that binds the registry
    "scope.target": "(atlas.scope=dimcity)"
},
"RegistryService~transformations": {
    // The action is triggered by a compiled unit, so it binds to the TRANSFORMATION registry, not
    // to the gdpr one. That reference defaults to (scope=no-inject) and matches nothing, so this
    // filter is what makes the action fire at all - edit it, do not replace it.
    "stageActionService.target":
        "(|(component.name=QvtStageActionService)(component.name=QvtGdprFlowStageAction))"
},
"GDPRMetadataDiagnosticsStageAction~transformations": {
    // The other half: what puts the report's findings onto the compiled unit
    "target.registry": "transformations",
    "subject.type": "TransformationSubject"
},
"QvtFlowFindingsStageAction": {
    // And what puts half of them onto the metamodels. It is triggered by a REPORT, so it binds to
    // the gdpr registry; target.registry only says where the findings are then written
    "target.registry": "schema",
    "report.stages": ["draft", "approved", "release"],
    "trigger.scopes": ["dimcity"],
    "scope.target": "(atlas.scope=dimcity)"
},
"QvtGdprReanalysisStageAction": {
    // Re-derives a transformation when a review it rests on changes. Also triggered by a REPORT,
    // so it binds to the gdpr registry; unit.registry is where the stale units are looked up
    "unit.registry": "transformations",
    "report.stages": ["draft", "approved", "release"],
    "trigger.scopes": ["dimcity"],
    "scope.target": "(atlas.scope=dimcity)"
},
"RegistryService~gdpr": {
    "stageActionService.target":
        "(|(component.name=GDPRReportHistoryStageAction)(component.name=GDPRMetadataDiagnosticsStageAction)(component.name=QvtFlowFindingsStageAction)(component.name=QvtGdprReanalysisStageAction))"
}
```

The configuration is `REQUIRE`, so a runtime that has not asked for transformation reports does not
start writing them by having the bundle installed.

**Nothing loops**, although the report is written into a registry whose actions read reports. Two
independent reasons: this action answers only for a `CompiledUnit` object type, and a transformation
report's subject is a `TransformationSubject` whose `m2x1:` fingerprint never matches a
`packageEntry`. The first is the rule; the second is a property of the digests.

## Where it came from

The whole chain was run by hand against a live jena runtime on 2026-09-23 before any of this was
built, which is why it is a work package and not a question. The compiled unit that probe produced
is the fixture the analyser is tested against — the real thing the atlas stored that day, not a
fixture written to suit the code.
