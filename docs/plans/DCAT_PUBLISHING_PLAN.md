# Publishing model.atlas to a DCAT.Atlas portal

**Status:** proposal, 2026-08-24; all open questions decided 2026-08-25 (§11).
Nothing implemented in this repository.
**Companion documents (in `dcat.atlas`):**
[`docs/plans/model-atlas-dcat-mapping.md`](../../../dcat.atlas/docs/plans/model-atlas-dcat-mapping.md)
— what an EPackage *is* in DCAT, decided;
[`docs/plans/client-library-implementation-plan.md`](../../../dcat.atlas/docs/plans/client-library-implementation-plan.md)
— the client this plan consumes (P1 and P3 done, on branch `dcat_client`).

The mapping is settled. This plan answers the three questions that are not: **what does the
publisher hook into**, **how is publication made selective**, and **what happens when
something stops being published**.

---

## 1. What exists, measured

### 1.1 On the DCAT side — the client, as shipped

`DcatAtlasClient` (`org.eclipse.fennec.dcat.atlas.client.api`) is complete for everything the
mapping needs: `registerCatalog/Dataset/DatasetSeries/DataService/Distribution`, all six
`link*`/`unlink*` pairs, `etagOf`, `delete(collection, id, DeleteMode)`, `ready()`. Writes are
idempotent `PUT`s keyed by a caller-chosen id, and every `registerX` has a three-argument
conditional form returning `Registration<T>` (stored entity + new ETag, `applied()` false
when a precondition refused it).

`client.osgi` publishes one `DcatAtlasClientComponent` per portal as both `DcatAtlasClient`
and `AsyncDcatAtlasClient` (`<T> Promise<T> submit(Function<DcatAtlasClient,T>)`), factory PID
`org.eclipse.fennec.dcat.atlas.client`, service property `dcat.portal`, one daemon thread per
portal, optional readiness gate (`check.ready`, `require.ready`).

Four jars are already in `cnf/local` and indexed (`client.api`, `client.impl`, `client.osgi`,
`dcatap.de.model`, all `1.0.0.20260824…-SNAPSHOT`). They need SCR and a Jakarta RS whiteboard
`ClientBuilder` — model.atlas has both — and no SPI-Fly and no Jena. `dcatap.de.model` imports
`org.eclipse.fennec.emf.osgi.configurator`, i.e. the fennec (not gecko) EMF stack, so it fits
this workspace's runtime as-is.

**The write floor, from the client's P1/P3 measurements.** `publisher` is `lowerBound=1`
*containment* on `dcat:DcatResource` (`dcatap.ecore:654`) and `description` is the OCL
invariant `HasDescription`; `StoreConfig.validateOnWrite()` defaults to **on**, independently
of SHACL. So every Catalog/Dataset/DatasetSeries/DataService needs `title` + `description` +
a contained `foaf:Agent` that has its own `name`. Because publisher is containment, **each
entity carries its own copy of the Agent** — nothing is shared. A `Distribution` instead
requires `accessURL` and `license` (`license` is `lowerBound=1` containment there,
`dcatap.ecore:292`). `foaf:Agent` and `terms:LicenseDocument` both extend `rdf:IdentifiedResource`,
so both take an `about` IRI (constraint `TypeIsIri` applies to their `type` attribute).

### 1.2 On the model.atlas side — the hooks that exist

| # | Hook | What it gives | Cost |
|---|---|---|---|
| 1 | **`StageActionService` whiteboard** (`workflow`, exported) | `ActionContext(scope, registry, objectId, objectType, stage, sourceStage, targetStage, exitReason, triggerUser, triggerTime, notes, replay, metadata)` on ENTER/UPDATE/EXIT, plus startup and shutdown replays, plus `ExitReason.DELETED` vs `TRANSITIONED` | Every `RegistryService` config must widen its `stageActionService.target` (today `(component.name=EPackageStageActionService)`) — a change in **every** deployment's `workflow.json`. And `RegistryServiceImpl.dispatch` **joins** the returned promises, so a slow action delays the REST call that triggered it |
| 2 | **`EPackage` service tracking** — `@Reference(MULTIPLE, DYNAMIC)` filtered on the properties `DynamicEPackageConfigurator.getServiceProperties()` guarantees: `emf.nsURI`, `emf.name`, `emf.version`, `emf.fileExtension`, `emf.model.scope`, `atlas.stage`, `emf.fingerprint`, `dynamic.registration=true` | scope + stage + nsURI + fingerprint + version, for every registration and unregistration, delivered on our own thread pool | No `ExitReason`; shutdown looks like mass unregistration |
| 3 | `RegistryResync.TOPIC` untyped event | out-of-band (git push) reconcile signal, `scope` / `stage` / `removedObjectIds` | Only the git backend publishes it |
| 4 | `ReadableScopeService` (`scope.api`) — `getScopeInfo()` → `ScopeInfo(name, description, parentScope, registries)` → `RegistryInfo(name, description, type, stages)` → `StageInfo(name, description, readable, writable, final)`, plus `registryView(registry, stage)`, `listObjectIds`, `stream` | the read-side truth for enumeration; scope services carry `atlas.scope` (and legacy `scope.name`) | — |
| 5 | `SupportedMediatype.getSupportedMediaTypes()` (`mediatypes.api`) | the runtime list of servable media types | recomputed whenever the bound `ResourceSet` changes |

**Hook 2 sits downstream of all the others.** Every path that changes what the atlas serves —
`uploadToStage`, `updateInStage`, `deleteFromStage`, `transitionToStage`, the per-scope startup
ENTER replay, the shutdown EXIT replay, and the git `RegistryResync` handler — ends in
`EPackageStageActionService` calling `DynamicEPackageRegistrationService.registerEPackage` /
`unregisterEPackage`. That is exactly one place, and it is observable without touching a single
existing configuration.

**And a correction to the mapping document.** `ModelAtlasRequestFilter.resolveMediaType`
(`rest.filter`, lines 236-274) honours a **`?mediaType=` query parameter, which wins over
`Accept`** and answers 415 for an unsupported value. So the format selector the mapping's §4
assumed did not exist *does* exist:

```
accessURL   {base}/atlas/rest/{scope}/schema/stages/{stage}/content?nsUri={enc}
downloadURL {base}/atlas/rest/{scope}/schema/stages/{stage}/content?nsUri={enc}&mediaType=application/xmi
```

Each Distribution can therefore carry a real `dcat:downloadURL`, and §4's "negotiation-only,
empty downloadURL" decision can be revisited in our favour before anything is built.

---

## 2. Decision: track EPackage services, reconcile from the scope API

**Trigger = hook 2. Enumeration = hook 4.** The tracker says *something changed here*; the
scope API answers *what should the portal hold for this scope*. Two consequences worth stating:

- **No deployment config changes.** No `stageActionService.target` edits, so an existing
  runtime gains DCAT publishing by adding two bundles and one configuration.
- **DCAT mirrors what the atlas actually serves.** A package that is stored but not registered
  is not resolvable through the API either, so a Dataset for it would advertise a URL that
  answers 204.

That second point is also the limitation to write down: EPackage services exist only for the
stages in `EPackageStageActionService.trigger.stages` (default `{"release"}`; the jena configs
list `draft, approved, release`). A deployment that triggers only `release` will publish only
release datasets, whatever the DCAT policy says. That is not a compromise but the point (**O3, decided**):
the published set is exactly the set the atlas will serve, so no `accessURL` we advertise can
answer 204. No `storage-visible` publication mode is built.

**Why not hook 1.** It is the better *semantic* hook — `ExitReason` distinguishes "deleted"
from "transitioned away", and `requiresReplayOnStartup()` hands you a free reconciliation. It
loses on three practical counts: it needs a config edit in every `workflow.json` that exists
(and every future one — a deployment that forgets it silently publishes nothing), its promises
are joined into the triggering REST call, and `RegistryResyncHandler` re-implements the
dispatch separately so a new action service has to behave under both. For DCAT the intent
distinction buys nothing, and that is now **decided (O5)**: whether a package was deleted or
moved on, the stage URL stops serving it, and that is what the portal should say. Hook 1 is out
of scope permanently, not held in reserve — so "no deployment config changes" is a property of
the final design, not of a first phase.

`RegistryResync` (hook 3) needs no separate subscription: the handler it already has calls
`RegistryService.activate`, which replays ENTER, which re-registers the services we track.

---

## 3. Where the code goes

| bundle | contents |
|---|---|
| **`org.eclipse.fennec.model.atlas.dcat`** | the publisher. Exports one package, `…dcat.api` (the two SPIs of §7 and the `PublicationTarget` record); everything else `Private-Package`. Buildpath: `dcat.atlas.client.api` + `.client.osgi` + `dcatap.de.model` (Local repo, `version=latest`), `…model.atlas.scope.api`, `…mediatypes.api`, `org.eclipse.fennec.emf.osgi` (for `EMFNamespaces` and `FingerprintHelper`), DS/metatype/cm annotations |
| **`org.eclipse.fennec.model.atlas.dcat.tests`** | plain-Java tests for the mapper, the id scheme and the policy; OSGi ITs against a portal container. `-maven-release: local` |
| `…runtime.config.local.dcat` (or additions to `…local.jena`) | `DcatAtlasClient~portal` + `DcatPublisher~portal` configurations |

A separate `…dcat.api` *bundle* is deliberately not proposed: one exported package inside the
implementation bundle keeps the api/internal split the Eclipse guidelines ask for without a
fourth artifact, and splitting it later is mechanical.

### Components

| component | responsibility |
|---|---|
| `DcatPublisher` (factory PID `DcatPublisher`, `ConfigurationPolicy.REQUIRE`) | owns one portal: `@Reference(target = "(dcat.portal=…)")` on `AsyncDcatAtlasClient`, the work queue, the ETag/fingerprint state, the reconcile entry points |
| `ScopeCatalogTracker` | `@Reference(MULTIPLE, DYNAMIC)` on `ReadableScopeService`, target `(atlas.scope=*)` → enqueue `publishCatalog(scope)` / `retireCatalog(scope)` |
| `PackageServiceTracker` | `@Reference(MULTIPLE, DYNAMIC)` on `EPackage`, target `(&(dynamic.registration=true)(emf.model.scope=*)(atlas.stage=*)(emf.nsURI=*))` → enqueue `publishPackage(target)` / `retirePackage(target)`. Reads scope/stage/nsURI/version/fingerprint straight off the service properties; never touches the portal on the DS thread |
| `DcatMapper` | atlas facts + defaults → `Catalog` / `DatasetSeries` / `Dataset` / `Distribution` / `DataService` EObjects |
| `DcatIds` | the id scheme of §5 |
| `ConfiguredPublicationPolicy` | the default `DcatPublicationPolicy` (§7), driven by configuration |
| `ConfiguredMetadataSource` | the default `DcatMetadataSource` (§6), driven by configuration |
| `DcatPublisherHealthCheck` | `HealthCheck` tagged `atlas`: portal readiness, queue depth, last error per target |
| `DcatCommands` (optional) | gogo `dcat:status`, `dcat:reconcile [scope]` |

Everything the publisher does runs through `AsyncDcatAtlasClient.submit`, one task per target,
so a DS bind, a REST upload and a framework shutdown never wait on the portal.

---

## 4. The publishing sequence

The client's §6.4a is binding: a `PUT` replaces, so containment (`dcat:distribution`) and
membership (`inSeries`, `dataset`, `servesDataset`) are dropped by a re-register and must be
re-asserted. Every step is idempotent, so the whole sequence is the normal path, not a repair.

**Once per portal, at activation** — `DataService`:

```
ready()  → register DataService(dataServiceId)
           endpointURL         {base}/atlas/rest
           endpointDescription {base}/atlas/rest/openapi.json
```

**Per scope** (catalog tracker):

```
register Catalog(scope)                    ← ScopeInfo.name/description
link DataService → Catalog
if ScopeInfo.parentScope is itself published: link sub-catalog(parent, scope)
```

**Per published package-in-a-stage** (package tracker):

```
register DatasetSeries(scope, nsURI)       if not already known
register Dataset(scope, stage, nsURI)
for each publishable media type:
    register Distribution(datasetId, mediaTypeId)
link Dataset → Catalog(scope)
link Dataset → DatasetSeries(scope, nsURI)
link Dataset → DataService                 (dcat:servesDataset)
for each Distribution: link accessService(datasetId, distId, dataServiceId)
```

**Change detection.** Keep, per published Dataset, the `emf.fingerprint` last published and the
ETag last returned. A tracker event whose fingerprint equals the stored one is a no-op — which
matters because the ENTER replay at startup re-registers every package on every boot. At
activation, re-seed validators with one `etagOf` per known resource (client §6.4c); if
`foreign.writes.expected` is false, use the two-argument unconditional form and skip the
seeding entirely.

**Errors, by class.** `DcatModelConstraintException` / `DcatShaclException` are permanent for
that entity: log once at WARNING with the report, mark the target unpublishable, do not retry.
`RetryableException` (503, portal's git push failed — the commit *is* durable) and
`TransportException` go on a bounded backoff queue. `Registration.applied() == false` is a
foreign edit: log at WARNING, carry on to the next resource, never unwind.

---

## 5. Identity

Ids appear in portal URLs, so: URL-safe, stable across restarts, derived from nothing that
moves.

| entity | id | notes |
|---|---|---|
| `DataService` | config `dataservice.id`, default `model-atlas` | must be distinct per atlas instance if two publish into one portal |
| `Catalog` | `{scope}` | scope names are already URL path segments |
| `DatasetSeries` | `{scope}--{b64url(nsURI)}` | Base64-URL, unpadded, computed by us — see below. `dct:identifier` carries the readable nsURI |
| `Dataset` | `{scope}--{stage}--{b64url(nsURI)}` | scope *and* stage: the same nsURI legitimately lives in several scopes and stages |
| `Distribution` | `{mediaTypeSlug}` (e.g. `application-xmi`) under its dataset | the admin path is `…/datasets/{datasetId}/distributions/{id}`, so the id namespace is per-dataset |

**Not the atlas `objectId`.** `SchemaPackagesResource` mints a **random UUID** per package at
upload (`SchemaPackagesResource.java:364`), stable across stage transitions; the nsURI lives in
`ObjectMetadata.properties["nsUri"]`. It is deliberately *not* derived from the nsURI, and it is
not a URL component anywhere in the REST layer — package endpoints address content by
`?nsUri=`. Three reasons the DCAT ids stay derived from the nsURI instead:

- **the event path does not carry it.** Hook 2 hands us `emf.nsURI`, `emf.model.scope`,
  `atlas.stage`, `emf.version`, `emf.fingerprint` — no `objectId`. Keying on it would mean a
  metadata lookup per registration event, purely to name a resource we can already name.
- **it is not stable across a delete/re-upload.** Deleting a package and uploading it again
  produces a *new* UUID for the same model, so the portal would grow a second Dataset IRI for
  something consumers have already bookmarked. `b64url(nsURI)` survives it.
- **it is per-object, not per-(scope, stage).** A Dataset is one nsURI in one stage of one
  scope, so an objectId-based id needs the same `{scope}--{stage}--` prefix regardless.

So the encoding is now purely our own convention — the "the schema REST layer already does it
this way" argument in O1 is gone, and O1 was decided on its own merits (§11).

**Series per scope, not global.** The mapping says "EPackage nsURI = DatasetSeries", which
read literally makes one series span every scope holding that nsURI. Per-scope keeps a scope's
catalogue self-contained, avoids a resource two catalogues both want to govern, and matches
model.atlas's own reality: the same nsURI in two scopes is two independently governed things.
Since `dcat:DatasetSeries` *is* a `dcat:Dataset` in DCAT 3, the series is also linked into its
scope's Catalog, so nothing is left unlinked. Global series stays available as a config switch
if a portal operator wants cross-scope grouping (O2).

---

## 6. Metadata

Layer 1 only — configuration, per the mapping's decision; the per-model EAnnotation override
is the second implementation of `DcatMetadataSource` and is out of scope here.

| DCAT | source |
|---|---|
| `dct:title` | series/dataset: `EPackage.getName()`; dataset adds the stage (`"Person model (release)"`). Catalog: `ScopeInfo.getName()` |
| `dct:description` | `EcoreUtil.getDocumentation(ePackage)`, else a configured template. Catalog: `ScopeInfo.getDescription()` |
| `dct:publisher` | configured `publisher.name` (+ optional `publisher.about` IRI, `publisher.mbox`, `publisher.type`); a **fresh contained `foaf:Agent` per entity** |
| `dct:license` | configured `license.uri` → a contained `LicenseDocument` with `about` = that IRI. Required on every Distribution |
| `dcat:theme` | configured `theme` list (DCAT-AP.de data-theme IRIs) |
| `dcat:keyword` | configured `keywords`, plus `scope:{scope}`, `stage:{stage}`, `registry:schema` |
| `dct:identifier` | the nsURI |
| `dcat:version` (Dataset) | `emf.version` service property |
| `dct:modified` / `dct:issued` | `ObjectMetadata` timestamps where available, else first-publish time |
| `dcat:mediaType`, `dct:format` | the media type string |
| `spdx:checksum` (Distribution) | `emf.fingerprint` — `fp1:<sha256 hex>` → `algorithm` = the SPDX sha256 IRI, `checksumValue` = the decoded bytes |
| `dcat:accessURL` / `dcat:downloadURL` | §1.2 |

**Media types.** `SupportedMediatype.getSupportedMediaTypes()` lists everything the runtime can
serve, which is more than belongs in a catalogue (it includes whatever content types the bound
`ResourceSet` happens to carry). Configure an allowlist — `distribution.media.types`, default
`application/xmi, application/json, application/schema+json, application/schema+xml` — and
intersect it with the runtime list, so a portal never advertises a format the server would 415.

**`{base}` has to be configured.** model.atlas has no notion of its own public URL — there is
no `PUBLIC_BASE_URL` equivalent anywhere in the repo — and a `UriInfo` is not available outside
a request. `atlas.public.base.uri` is therefore a required property of `DcatPublisher`, and the
publisher should refuse to activate without it rather than publish `localhost` URLs into a
portal.

---

## 7. Selective publication — the configurable part

Two SPIs in `…dcat.api`, both whiteboards, highest `service.ranking` wins:

```java
public record PublicationTarget(String scope, String stage, String registry,
                                String nsUri, String version, String fingerprint) {}

@ConsumerType
public interface DcatPublicationPolicy {
    boolean publishScope(String scope);
    boolean publish(PublicationTarget target);
}

@ConsumerType
public interface DcatMetadataSource { /* title/description/publisher/license/theme/keywords */ }
```

The default implementation is configuration-driven, on the `DcatPublisher` configuration:

```json
"DcatPublisher~jena": {
    "dcat.portal.target": "(dcat.portal=jena)",
    "atlas.public.base.uri": "$[env:ATLAS_PUBLIC_BASE_URI;default=http://localhost:8080]",
    "scopes": ["jena"],
    "exclude": ["jena/draft", "*#http://internal.example.org/*"],
    "include": [],
    "distribution.media.types": ["application/xmi", "application/json"],
    "publish.inherited": false,
    "unpublish.mode": "UNLINK",
    "retire.on.shutdown": false,
    "publisher.name": "Stadt Jena",
    "publisher.about": "https://www.jena.de",
    "license.uri": "http://dcat-ap.de/def/licenses/dl-by-de/2.0",
    "theme": ["http://publications.europa.eu/resource/authority/data-theme/TECH"]
}
```

**The rule shape** is one string, `scopePattern[/stagePattern][#nsUriPattern]`, with `*` as a
glob. `jena` is a whole scope, `jena/draft` a stage within it, `jena/release#http://x/*` a set
of packages in one scope+stage, `*#http://internal/*` those packages everywhere. That covers
each granularity the requirement names, in one syntax, with no separate list per dimension.

**The verdict**, deliberately two-level rather than one:

1. **Scopes are opt-in.** A scope publishes only if it is named in `scopes` (globs allowed).
   Nothing reaches a public portal because somebody added a configuration.
2. **Within a published scope, everything publishes unless excluded.** `exclude` then `include`
   as a re-inclusion of a narrower slice; on a tie the more specific pattern wins, and on equal
   specificity exclusion wins.

Because policy is an SPI, the mapping document's other candidate — an EAnnotation on the
EPackage that opts it in — is a second implementation later, and the two compose by ranking
(configuration above annotations, per the mapping's "both with configuration winning").

---

## 8. Unpublication

Selective publication implies it, and the shapes differ:

| what happened | what the portal should say |
|---|---|
| a package is deleted, or transitions out of a stage that has `delete.after.transition` | the stage URL stops serving it → its Dataset should go |
| the policy changes so a target is no longer publishable | same |
| a scope's configuration is deleted | its Catalog and everything only reachable through it |
| **the framework is shutting down** | **nothing, by default** — see below |

That last row is the one to argue about, because the opposite reading is perfectly reasonable:
if this atlas is down, every `accessURL` in the catalogue 503s, so why keep advertising them?

The answer is that **shutdown-driven retirement cannot deliver that guarantee, and pays a real
cost for the half of it it can deliver.** Three things, in order of weight:

1. **It is unreliable exactly when it matters.** `SIGKILL`, an OOM kill, a crashed container, a
   severed network, a dead host — all produce precisely the state we would be trying to prevent
   (atlas gone, portal still advertising) and none of them run a `@Deactivate`. A catalogue that
   tracks liveness only on *clean* shutdown does not track liveness; it just behaves differently
   on the one path where nothing was wrong. If unreachable-URL suppression is a genuine
   requirement, it needs a mechanism that survives the atlas dying: the portal probing
   `accessURL`, or a heartbeat/TTL the atlas refreshes while alive. That is a portal-side
   feature — and there is already a DCAT.Atlas issue open for an availability check on the
   Distribution URL, which is where it belongs (§11, O5b). We do not approximate it here.
2. **The cost falls on the common case.** Restarts are routine — redeploys, config changes,
   `docker compose up -d`. Retiring on shutdown turns each one into a full retire + full
   re-publish of every entity: portal write churn (on a git-backed portal, two commits per
   entity per restart), a window where the catalogue is empty for harvesters mid-poll, and in
   `DELETE`/`CASCADE` mode the destruction of anything the portal added on its side. Worse, if
   the restart fails, we have already emptied the catalogue and there is nothing left running
   to put it back.
3. **A DCAT entry is not an uptime claim.** It says *this model exists, here is who governs it
   and where it is served from*, and that stays true across a restart — the models are still in
   git/Apicurio, and the same catalogue comes back. Deletion should mean "this model is gone",
   which is what makes the other rows of the table meaningful; if shutdown also means it, the
   portal loses the ability to distinguish "retired" from "rebooting".

So: default `false`, and a knob for operators who disagree.

```
retire.on.shutdown = false   # true → retire the catalogue on clean @Deactivate
```

When `true`, force `unpublish.mode=UNLINK` for the shutdown path regardless of the configured
mode — a restart must never `DELETE` — and skip it when the framework is stopping for an
update rather than a stop, if we can tell. Everything else in this section applies unchanged.

**Either way, the guards below are still needed**, because they are not about shutdown:

- ignore unbind events once `bundleContext.getBundle(0).getState()` is `STOPPING` (gating the
  shutdown path on one flag instead of scattering the decision), and treat our own
  `@Deactivate` as "stop working" — the bundle being refreshed for an update is not a statement
  about the models;
- debounce retirement (`unpublish.delay.seconds`, default 30) so a re-register of the same
  target inside the window cancels it — which is what a content **update** looks like, since
  `DynamicEPackageRegistrationService` replaces a changed package by unregister-then-register.
  Without this, every model edit briefly unpublishes its own Dataset.

`unpublish.mode`, per the mapping's §5 (the client must be able to do either):

| mode | behaviour |
|---|---|
| `NONE` | leave it; the portal keeps advertising it (an operator's choice, and a defensible one for an archive) |
| `UNLINK` *(default)* | remove the membership links, keep the resource — it stops being discoverable through the catalogue without deleting anything |
| `DELETE` | `delete(…, SINGLE)`; fails if referrers remain |
| `CASCADE` | `delete(…, CASCADE)`; the returned unlinked-resource list goes to the log |

Note from the client's P1: a cascade delete of a dataset unlinks its catalog and service links
but *not* `inSeries` — that lives on the dataset and dies with it. And a series whose last
dataset has gone is left behind; reap it only in `CASCADE` mode.

---

## 9. Runtime and build plumbing

- Add to `modelatlas.runtime_base.bndrun` (or a new `…_local-jena-dcat.bndrun` variant first):
  `bnd.identity` for `…model.atlas.dcat`, `…dcat.atlas.client.api`, `…client.impl`,
  `…client.osgi`, `…dcatap.de.model`, and the configuration bundle.
- **Do not set `-resolve.effective: active`** in the test bndrun. The osgitech whiteboard
  registers `jakarta.ws.rs.client.ClientBuilder` programmatically without an `osgi.service`
  capability, so `DcatAtlasClientComponent`'s mandatory reference has nothing to resolve
  against; `rest.client.osgi.tests` in this repo already omits the line for the same reason.
- Re-run `resolve` with `--rerun`; an UP-TO-DATE resolve bakes stale `-runbundles`.
- New files need the EPL-2.0 header block (SkyWalking Eyes gates CI).
- Test and config bundles get `-maven-release: local`.
- **Reproducibility caveat, worth an issue.** The four jars are hand-placed from an unmerged
  `dcat_client` branch and `dcat.atlas` publishes nothing to Maven — the same arrangement as
  `org.gecko.jgit`. Switching dcat.atlas's snapshot deploy on (Central Portal snapshots) and
  replacing the local jars with coordinates should land before this is anything but a
  prototype.
- No EMF interaction to worry about: the client does its own XMI through a plain
  `ResourceSetImpl`, and `dcatap.de.model`'s packages simply join the default EPackage
  registry. The publisher registers no `ResourceSet`, so `SupportedMediatypesImpl`'s list is
  unaffected.

---

## 10. Phasing

| phase | content | done when |
|---|---|---|
| **D1** | Bundle scaffolding, `DcatPublisher` config, client reference, readiness gate, `DataService` + one `Catalog` from a `ReadableScopeService` bind | a scope appears as a Catalog in a portal container, with the API as its DataService |
| **D2** | `DcatIds`, `DcatMapper`, `ConfiguredMetadataSource`; the full sequence of §4 for one target, driven by a gogo `dcat:reconcile` | `inSeries=1`, distributions ≥ 1, `accessService=1` read back |
| **D3** | `PackageServiceTracker` + the work queue + fingerprint/ETag state | an upload through the REST API lands in the portal without a manual step; a restart re-publishes nothing |
| **D4** | `DcatPublicationPolicy` + the rule language of §7 | each of the four granularities has a test |
| **D5** | Unpublication: modes, the STOPPING guard, the debounce | a restart retires nothing; a delete retires exactly one Dataset |
| **D6** | Error classification, backoff queue, health check | a 503 retries, a SHACL refusal does not |
| **D7** | OSGi ITs against a portal container; the runtime config bundle and bndrun variant | `testOSGi` green |

D1–D3 is the walking skeleton and the honest end of "does this work". D4–D6 is what makes it
operable.

---

## 11. Decisions

All seven were resolved by the owner on **2026-08-25**. Kept here as the record of *why*, since
each one is a constraint on the implementation rather than a preference.

| # | question | decision |
|---|---|---|
| **O1** | the id encoding | **`b64url(nsURI)`**, as proposed |
| **O2** | series per scope or global | **per scope**, as proposed (global stays a config switch) |
| **O3** | which stages get published | **what is registered** |
| **O4** | inherited packages | **configurable**, `publish.inherited=false` by default |
| **O5** | do we need `ExitReason` | **no** |
| **O5b** | should the portal reflect availability | **not from here** — the DCAT side already has an issue for it |
| **O6** | one portal or several | **one for now**; the factory shape stays |

**O1 — `b64url(nsURI)`.** A portal id is permanent and a slug collision is not repairable after
publication, so opacity wins over readability. `dct:identifier` carries the readable nsURI and
`dct:title` the readable name, so nothing is actually lost except the look of the URL.

**O2 — series per scope.** A scope's catalogue stays self-contained and no resource has two
catalogues claiming to govern it. Global grouping remains available as configuration if a portal
operator ever asks for it.

**O3 — publish what is registered.** So the reconciler keeps enumerating through the *service
registry* view of a scope, and the §2 limitation stands as intended behaviour rather than a
caveat: a deployment whose `EPackageStageActionService.trigger.stages` lists only `release`
publishes only release datasets. This is the property that keeps the catalogue honest — every
published `accessURL` resolves, because it is exactly the set the atlas will serve. No
`storage-visible` mode is built.

**O4 — `publish.inherited=false`.** A child scope's catalogue will not relist a model it
inherits from its parent; the parent's Catalog is where that model lives. Flipping the flag to
`true` publishes an additional Dataset per inheriting scope, under that scope's id prefix, and
that is the operator's call. Needs a README paragraph, because "the child API serves it but the
child catalogue does not list it" is surprising until explained.

**O5 — no `ExitReason`.** This settles the hook choice for good: **hook 1 (`StageActionService`)
is not needed at all**, not now and not as a planned second phase. Deleted and
transitioned-away both mean the stage URL stops serving the package, and the portal says the
same thing either way. Hook 2 alone is the trigger, and no deployment's `workflow.json` is
touched — the "no config changes" property of §2 is now permanent, not provisional.

**O5b — availability belongs to the DCAT side.** There is an existing DCAT.Atlas issue for an
availability check on (at least) the Distribution URL; that is the right home for it, because a
portal-side probe survives the atlas being killed, which nothing running inside the atlas can.
So §8 stands as written: `retire.on.shutdown` stays `false` by default and the catalogue is a
statement about what exists, not about what is up right now. When the DCAT-side check lands, the
two compose without changes here — it probes the URLs we publish.

**O6 — one portal.** One `DcatPublisher` configuration, one `dcat.portal` client. Nothing is
built for multi-portal, but nothing assumes single either: `DcatPublisher` stays a factory
component with a `dcat.portal.target`, so a second portal is a second configuration with its own
policy, not a redesign. Zero extra cost today, so there is no reason to design it away.

