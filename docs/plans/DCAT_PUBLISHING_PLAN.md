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
mapping needs: `registerCatalog/Dataset/Distribution`, the `linkDatasetToCatalog` /
`linkSubCatalog` pairs and their `unlink*` counterparts, `etagOf`, `delete(collection, id, DeleteMode)`, `ready()`. Writes are
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
of SHACL. So every Catalog and Dataset needs `title` + `description` +
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
accessURL   {base}/{scope}/schema/stages/{stage}/content?nsUri={enc}
downloadURL {base}/{scope}/schema/stages/{stage}/content?nsUri={enc}&mediaType=application/xmi
```

(`{base}` carries the public path prefix — §6 — because an APISIX may rewrite the container's
own `/atlas/rest`.)

So each Distribution carries a real `dcat:downloadURL`. **The mapping's §4 is updated
accordingly (2026-08-26): the "negotiation-only, empty downloadURL" decision is reversed**, its
premise having been the absence of a selector that turns out to exist.

The **APISIX** makes that the correct choice and not merely the nicer one. Negotiation-only
distributions share one URL and differ only by the `Accept` header, which puts the correctness
of the whole scheme in the hands of a cache we do not own: a gateway response cache keyed
without `Vary: Accept` will happily serve the XMI it cached to the next harvester asking for
JSON. `?mediaType=` is in a cache key by default, so a `downloadURL` per representation removes
the failure mode instead of documenting it.

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
| **`org.eclipse.fennec.model.atlas.dcat`** | the publisher. Exports one package, `…dcat.api` (the two SPIs of §7 and the `PublicationTarget` record); everything else `Private-Package`. Buildpath: `dcat.atlas.client.api` + `.client.osgi` + `dcatap.de.model` (Local repo, `version=latest`), `…model.atlas.scope.api`, `…model.atlas.workflow` (for `ObjectMetadata` and the scope services — §7), `…mediatypes.api`, `org.eclipse.fennec.emf.osgi` (for `EMFNamespaces` and `FingerprintHelper`), DS/metatype/cm annotations |
| **`org.eclipse.fennec.model.atlas.dcat.tests`** | plain-Java tests for the mapper, the id scheme and the policy; OSGi ITs against a portal container. `-maven-release: local` |
| `…runtime.config.local.dcat` (or additions to `…local.jena`) | `DcatAtlasClient~portal` + `DcatPublisher~portal` configurations, plus one `DcatScopeCatalog~{scope}` per scope that needs an adopted or configured Catalog (§7a) |

A separate `…dcat.api` *bundle* is deliberately not proposed: one exported package inside the
implementation bundle keeps the api/internal split the Eclipse guidelines ask for without a
fourth artifact, and splitting it later is mechanical.

### Components

| component | responsibility |
|---|---|
| `DcatPublisher` (factory PID `DcatPublisher`, `ConfigurationPolicy.REQUIRE`) | owns one portal: `@Reference(target = "(dcat.portal=…)")` on `AsyncDcatAtlasClient`, the work queue, the ETag/fingerprint state, the reconcile entry points |
| `ScopeCatalogTracker` | `@Reference(MULTIPLE, DYNAMIC)` on the scope service, target `(atlas.scope=*)` → enqueue `publishCatalog(scope)` / `retireCatalog(scope)`, and on a *new* scope also the ancestor-dataset fan-in of §4 |
| `ScopeHierarchy` | the scope tree from `ScopeInfo.parentScope`: ancestors of a scope, descendants of a scope, maintained as scope services come and go. The one place the fan-out is computed |
| `PackageServiceTracker` | `@Reference(MULTIPLE, DYNAMIC)` on `EPackage`, target `(&(dynamic.registration=true)(dcat=true)(emf.model.scope=*)(atlas.stage=*)(emf.nsURI=*))` → enqueue `publishPackage(target)` / `retirePackage(target)`. **The `(dcat=true)` term does the selecting** (§7), so a bind *is* a publish decision; reads scope/stage/nsURI/version/fingerprint straight off the service properties and never touches the portal on the DS thread |
| `DcatMapper` | atlas facts + defaults → `Catalog` / `Dataset` / `Distribution` EObjects |
| `DcatIds` | the id scheme of §5 |
| `CatalogResolver` | resolves a scope to (catalog id, ownership) across the three cases of §7a; the one place that knows whether a Catalog may be written |
| `StagePublicationPolicy` | the default `DcatPublicationPolicy` (§7): the scope gate plus the stage gate (final stages only, unless configured wider). The `dcat` flag itself is already handled by the tracker's filter |
| `ConfiguredMetadataSource` | the default `DcatMetadataSource` (§6), driven by configuration |
| `DcatPublisherHealthCheck` | `HealthCheck` tagged `atlas`: portal readiness, queue depth, last error per target |
| `DcatCommands` (optional) | gogo `dcat:status`, `dcat:reconcile [scope]` |

Everything the publisher does runs through `AsyncDcatAtlasClient.submit`, one task per target,
so a DS bind, a REST upload and a framework shutdown never wait on the portal.

---

## 4. The publishing sequence

The client's §6.4a is binding: a `PUT` replaces, so containment (`dcat:distribution`) and
membership (`dcat:dataset`) is dropped by a re-register and must be re-asserted — and with inheritance carried by links, "re-asserted" now means *into every Catalog the Dataset belongs to*, not just its own. Every step is idempotent, so the whole sequence is the normal path, not a repair.

**Per scope** (catalog tracker) — the branch is §7a's:

```
resolve Catalog(scope) → (catalogId, owned?)
if owned:   register Catalog(catalogId)     ← configured attributes, else ScopeInfo.name/description
if !owned:  catalog(catalogId) must be present, else refuse this scope and report it
for each ancestor a of scope:               ← the fan-in for a newly appearing scope
    for each published Dataset of a: link Dataset → Catalog(scope)
```

An **adopted** Catalog is never `PUT` — that would drop its `dcat:dataset` links, other
publishers' included. Datasets still link *into* it, which is additive and safe.

**No `dcat:catalog` link is ever written** (**O14**, decided 2026-08-27): not on an adopted
Catalog, not on one we own, in neither direction. Datasets link into Catalogs, and nothing else is
linked anywhere.

**Per published package-in-a-stage** (package tracker):

```
register Dataset(scope, stage, nsURI)      ← scope = the scope that DEFINES it
for each publishable media type:
    register Distribution(datasetId, mediaTypeId)
link Dataset → Catalog(scope)
for each descendant d of scope:            ← inheritance, by link and not by copy
    link Dataset → Catalog(d)
```

**Change detection.** Keep, per published Dataset, the `emf.fingerprint` last published and the
ETag last returned. A tracker event whose fingerprint equals the stored one is a no-op — which
matters because the ENTER replay at startup re-registers every package on every boot. At
activation, re-seed validators with one `etagOf` per known resource (client §6.4c); if
`foreign.writes.expected` is false, use the two-argument unconditional form and skip the
seeding entirely.

### As built (D6, 2026-08-27)

- **One funnel.** Every portal interaction goes through `submit(key, what, work)`, so classification,
  retry and reporting exist in one place instead of at each call site. Retrying is safe because every
  write is an idempotent `PUT` under a caller-chosen id and every link assertion is additive — a
  retry is the same operation again, not a second one.
- **`portal.ready() == false` is now a failure, not a `return`.** It used to log and drop the
  publish, which meant the most ordinary transient condition there is — a portal still starting —
  left the catalogue behind until something unrelated triggered another write. It throws
  `PortalNotReadyException`, which classifies as retryable alongside a 503.
- **Bounded in both directions:** the delay doubles from `retry.initial.delay.seconds` to
  `retry.max.delay.seconds`, so a portal down for an hour is polled a handful of times; and
  `retry.max.attempts` runs out, so something that only looks transient does not retry forever.
  Giving up is *recorded*, which is the difference between a stale catalogue somebody can see and one
  nobody knows about.
- **The health check is the publisher itself** — one per portal configuration, tagged `atlas` and
  deliberately **not** `readiness`: a portal being down says nothing about whether this atlas can
  serve its models, and a catalogue briefly behind is no reason to pull the atlas out of a load
  balancer. It reports what was previously log-only: refused scopes (D1a), permanently rejected
  payloads, and retries that have run out.
- **`immediate = true` is load-bearing** on the publisher now. Providing a service makes a component
  delayed by default, and a delayed publisher would not activate until somebody fetched its health
  check — so nothing would publish until something asked how publishing was going. Caught by the
  ITs, which went 16 red.

**Errors, by class.** `DcatModelConstraintException` / `DcatShaclException` are permanent for
that entity: log once at WARNING with the report, mark the target unpublishable, do not retry.
`RetryableException` (503, portal's git push failed — the commit *is* durable) and
`TransportException` go on a bounded backoff queue. `Registration.applied() == false` is a
foreign edit: log at WARNING, carry on to the next resource, never unwind.

### The media-type allowlist, as built (2026-08-27)

`distribution.media.types` is the operator's allowlist and the only thing that decides which
Distributions a Dataset carries; it is intersected with what the runtime reports it can serve, so
the catalogue can never advertise a format the server would answer 415 for. Two things had to change
for the configuration to actually hold:

- **Resolved per use, not cached at activation.** The runtime's list grows as codecs register
  content types, so a publisher that activated before they were up advertised fewer formats than
  configured for as long as it ran, and the allowlist looked ignored.
- **The runtime's list now announces itself.** `SupportedMediatypesImpl` refreshed its list inside
  `bindResourceSet` without the service ever rebinding, so there was no event for a consumer to
  react to. It now **registers its own service** and refreshes the service properties (
  `SupportedMediatype.MEDIATYPES_PROPERTY`) whenever the list changes — a component cannot modify
  properties DS registered on its behalf; only the holder of the `ServiceRegistration` can. The
  publisher takes the reference by *method* injection, because a field reference gets no
  `updated` callback ("Annotated field — There is no updated method name", cmpn 112), and
  re-publishes its tracked packages when the resolved set has actually changed. A codec coming up
  after a Dataset was published therefore adds its Distribution with no upload, no configuration
  touch and no restart.
- Because the registration is manual, the `osgi.service` capability is no longer derivable from the
  DS metadata, so `SupportedMediatypesImpl` carries an **`@Capability(namespace = "osgi.service", …)`**
  annotation — the same way `AtlasClientComponent` declares the services it registers by hand.
  Without it the production runtime, which resolves with `-resolve.effective: active`, has no
  provider for its consumers' mandatory references.
- **A narrowed allowlist has to reach already-published Datasets.** Their content has not changed,
  so the fingerprint check skips the write and nothing else would ever revisit them. On the first
  publish after a configuration change the published Distributions' media types are compared with
  what the allowlist now resolves to, and a mismatch forces a full rewrite. An **empty** resolution
  never counts as a mismatch: empty is what startup looks like while codecs come up, and stripping
  every Distribution then would leave nothing to put them back.

### As built (D2a, 2026-08-27)

`ScopeHierarchy` is an immutable snapshot built per operation from the bound `ScopeInfo`s, not
maintained state: it is a walk over a handful of entries, and a fan-out computed against a tree
shifting underneath it would link into some descendants and not others with nothing left to notice.
`ScopeInfo.parentScope` is the whole input — `isInheritingFromParentScope()` is defined as
"has a parent", so there is no second flag to consult.

**The fan-out is two dual questions, and both are needed.**

| asked by | question | answer |
|---|---|---|
| `writeDataset` / `retire` | which Catalogs list this Dataset? | `catalogsListing(definingScope)` = own + **descendants** |
| `writeCatalog` / `relinkDatasets` | which Datasets does this Catalog list? | own scope's + every **ancestor's** |

The second is why the fan-in for a scope created later needs no separate code path: `relinkDatasets`
already runs after every Catalog write, so a new scope's first Catalog write pulls in every
ancestor's Datasets by itself.

- **A bind can complete a chain.** A leaf bound before its middle scope can only see the parent name
  it was told, so it never linked its grandparent's Datasets. `relinkDescendantCatalogs` re-links
  everything below a newly bound scope — nothing else would revisit the leaf, because no package
  changed.
- **A retirement captures its Catalog set when it is scheduled**, and unions it with a fresh
  computation when it runs. The ordinary reason for a retirement — a scope's configuration deleted —
  takes the scope services with it, so a set computed only at run time would be empty and the
  memberships would dangle. The union covers the reverse too: a descendant that appeared inside the
  window and linked the Dataset in through its own fan-in. The unlink tolerates a `NotFound` per
  Catalog, so a stale id abandons neither the rest of the fan-out nor the delete after it.
- **Sub-catalog links are asserted in both directions**, because a `dcat:catalog` link lives on the
  *parent*: writing a Catalog drops the links to its children (re-asserted after every write), while
  its own membership in its parent survives that write but must be asserted when the child appears.
  Superseded by **O14**: no `dcat:catalog` link is written for any Catalog, so nothing has to be
  re-asserted after a Catalog write except its `dcat:dataset` memberships.
- `descendants()` never returns the scope itself, even where a configuration has written a cycle;
  both walks truncate rather than loop. Caught by `ScopeHierarchyTest`, not by review.

---

## 5. Identity

Ids appear in portal URLs, so: URL-safe, stable across restarts, derived from nothing that
moves.

| entity | id | notes |
|---|---|---|
| `Catalog` | configured `catalog.id`, default `{scope}` | scope names are already URL path segments. An **adopted** Catalog's id is whatever the portal already calls it, so this is the one id we do not get to choose (§7a) |
| `Dataset` | `{scope}--{stage}--{b64url(nsURI)}` | scope *and* stage: the same nsURI legitimately lives in several scopes and stages |
| `Distribution` | `{mediaTypeSlug}` (e.g. `application-xmi`) under its dataset | the admin path is `…/datasets/{datasetId}/distributions/{id}`, so the id namespace is per-dataset |

**`{scope}` in a Dataset id is the scope that *defines* the package, not one it is visible in.**
With inheritance carried by links (mapping §3), one Dataset appears in several Catalogs and keeps
the single id of its defining scope. That is what makes the scheme work at all: an id per
appearance would mean a duplicate Dataset per descendant scope, which is precisely what the
linking design exists to avoid.

**Not the atlas `objectId`.** `SchemaPackagesResource` mints a **random UUID** per package at
upload (`SchemaPackagesResource.java:364`), stable across stage transitions; the nsURI lives in
`ObjectMetadata.properties["nsUri"]`. It is deliberately *not* derived from the nsURI, and it is
not a URL component anywhere in the REST layer — package endpoints address content by
`?nsUri=`. Three reasons the DCAT ids stay derived from the nsURI instead:

- **the event path does not carry it.** Hook 2 hands us `emf.nsURI`, `emf.model.scope`,
  `atlas.stage`, `emf.version`, `emf.fingerprint` — no `objectId`. Keying on it would mean a
  metadata lookup per registration event, purely to name a resource we can already name.
  *Weaker since 2026-08-26:* §7 now does that lookup anyway to read the `dcat` flag, so the
  `objectId` is in hand by the time an id is minted. The two reasons below are the ones that
  still decide it.
- **it is not stable across a delete/re-upload.** Deleting a package and uploading it again
  produces a *new* UUID for the same model, so the portal would grow a second Dataset IRI for
  something consumers have already bookmarked. `b64url(nsURI)` survives it.
- **it is per-object, not per-(scope, stage).** A Dataset is one nsURI in one stage of one
  scope, so an objectId-based id needs the same `{scope}--{stage}--` prefix regardless.

So the encoding is now purely our own convention — the "the schema REST layer already does it
this way" argument in O1 is gone, and O1 was decided on its own merits (§11).

**No `DatasetSeries` (O2, revised 2026-08-26).** An EPackage is a Dataset under a Catalog with
its Distributions, and nothing above it. `dcat:inSeries` is not written, `registerDatasetSeries`
is not called, and the series id is not minted. The mapping doc records what that trades away;
here the only consequence is that the sequence in §4 loses two lines and the entity that was
hardest to write correctly is not written at all.

**Dataset ids must survive a shared Catalog.** With adoption in play, our ids can land next to
ids we did not mint, so the `{scope}--{stage}--` prefix stops being merely descriptive and
starts doing work. It is not a guarantee — a foreign portal could hold anything — so a scope
whose Catalog is adopted may want `id.prefix` on its `DcatScopeCatalog` configuration. Left out
until a real portal asks for it; adding it later renames resources, so it is worth deciding
before the first production publish rather than after.

---

## 6. Metadata

Layer 1 only — configuration, per the mapping's decision; the per-model EAnnotation override
is the second implementation of `DcatMetadataSource` and is out of scope here.

| DCAT | source |
|---|---|
| `dct:title` | dataset: `EPackage.getName()` plus the stage (`"Person model (release)"`). Catalog: `DcatScopeCatalog.catalog.title`, else `ScopeInfo.getName()` (§7a) |
| `dct:description` | `EcoreUtil.getDocumentation(ePackage)`, else a configured template. Catalog: `ScopeInfo.getDescription()` |
| `dct:publisher` | configured `publisher.name` (+ optional `publisher.about` IRI, `publisher.mbox`, `publisher.type`); a **fresh contained `foaf:Agent` per entity** |
| `dct:license` | configured `license.uri` → a contained `LicenseDocument` with `about` = that IRI. Required on every Distribution |
| `dcat:theme` | configured `theme` list (DCAT-AP.de data-theme IRIs) |
| `dcat:keyword` | configured `keywords`, plus `scope:{scope}`, `stage:{stage}` |
| `dct:identifier` | the nsURI |
| `dcat:version` (Dataset) | `emf.version` service property |
| `dct:modified` / `dct:issued` | `ObjectMetadata` timestamps where available, else first-publish time |
| `dcat:mediaType`, `dct:format` | the media type string |
| `spdx:checksum` (Distribution) | `emf.fingerprint` — `fp1:<sha256 hex>` → `algorithm` = the SPDX sha256 IRI, `checksumValue` = the decoded bytes |
| `dcat:accessURL` / `dcat:downloadURL` | §1.2, with `{base}` from configuration (below) |

**Media types.** `SupportedMediatype.getSupportedMediaTypes()` lists everything the runtime can
serve, which is more than belongs in a catalogue (it includes whatever content types the bound
`ResourceSet` happens to carry). Configure an allowlist — `distribution.media.types`, default
`application/xmi, application/json, application/schema+json, application/schema+xml` — and
intersect it with the runtime list, so a portal never advertises a format the server would 415.

### `{base}` is configuration, and an APISIX sits in front

model.atlas has no notion of its own public URL — there is no `PUBLIC_BASE_URL` equivalent
anywhere in the repo — and a `UriInfo` is not available outside a request. The publisher has no
request at all: it acts on a DS bind, so there is nothing to derive a host from and
`X-Forwarded-*` never reaches it. `atlas.public.base.uri` is therefore a **required** property
of `DcatPublisher`, and the publisher refuses to activate without it rather than publish
`localhost` into a portal.

**Decided 2026-08-26: there is a gateway, so `{base}` includes the path prefix.** An APISIX
terminates the public URL, and a gateway does not merely swap scheme and host — `proxy-rewrite`
routinely changes the *path* too, so the container's `/atlas/rest` prefix is not necessarily
what the world sees. Hard-coding it and appending it to a host-only base would produce URLs that
404 for every harvester.

So the configured value is the **whole public prefix, up to and including whatever stands in for
`/atlas/rest`**, and the publisher appends only the resource-relative part:

```
atlas.public.base.uri = https://opendata.example.de/model-atlas     # behind APISIX
atlas.public.base.uri = http://localhost:8080/atlas/rest            # no gateway
```

```
Distribution.accessURL           {base}/{scope}/schema/stages/{stage}/content?nsUri={enc}
Distribution.downloadURL         {base}/{scope}/schema/stages/{stage}/content?nsUri={enc}&mediaType={mt}
```

With the `DataService` dropped, the Distribution URLs are the *only* place `{base}` appears — so
a misconfigured base no longer breaks an endpoint description as well, it simply makes every
distribution unfetchable. Still fatal, still worth refusing activation over.

One knob, and it survives any rewrite the gateway performs. Validation at activation: absolute,
`http`/`https`, no query or fragment, trailing slash normalised away, and `localhost` /
`127.0.0.1` refused unless `allow.local.base.uri` is set — a dev convenience that must not be
the default, because a `localhost` URL in a public catalogue is worse than no catalogue entry.

**Do not gate activation on fetching it.** The obvious next step — probe `{base}` at startup and
refuse if it does not answer — fails in exactly the normal deployment: the gateway is frequently
not reachable *from inside* the network it fronts, and split-horizon DNS makes the public name
resolve to something else or to nothing. Report the configured base and the last probe result in
`DcatPublisherHealthCheck`; never make publishing depend on the atlas being able to reach its own
public address.

**Two things about the gateway that are not ours to configure but are ours to check:**

- **the routes must be anonymous.** If APISIX requires a key or a consumer on
  `…/schema/stages/{stage}/content`, every published `accessURL` answers `401` to a harvester and
  the catalogue is decorative. An open-data portal advertises URLs anyone can fetch; that is a
  gateway-side decision and it needs making explicitly, not discovering later.
- **`?mediaType=` must survive the gateway.** APISIX passes the query string through by
  default, and it is part of a cache key by default; a route that strips or rewrites query
  parameters would break every `downloadURL` silently, serving whatever the server prefers.

**Changing the base later is cheap, and that is by design.** No id is derived from it — ids are
`{scope}`, `{scope}--{stage}--b64url(nsURI)}` and the media-type slug — so a new domain or a
rewritten prefix is a `dcat:reconcile` that rewrites `accessURL` and `downloadURL` in place. Every resource keeps its identity and no consumer's bookmark breaks. This is the payoff
for O1 having kept the ids independent of anything that moves.

---

## 7. Selective publication — from the metadata model

**Decided 2026-08-26: the per-package verdict comes from `ObjectMetadata`.** An upload carries
`?dcat=true`, `SchemaPackagesResource` stores it in `ObjectMetadata.properties`, and the
publisher reads it back. No nsURI allowlist, no glob rule language, no EAnnotation.

### The atlas-side change

The `@POST`/`@PUT` on `/{scopeName}/schema/stages/{stageName}` already takes `overwrite` as a
`@QueryParam` and already writes one property:

```java
metadata.getProperties().put("nsUri", validatedNsUri);   // SchemaPackagesResource.java:372
```

so the flag is the same two lines again:

```java
@Parameter(description = "Publish this package to the configured DCAT portal")
@QueryParam("dcat") boolean dcat
...
metadata.getProperties().put(DCAT_PUBLISH, dcat);
```

Three things to get right beyond that:

- **the content-update path** (`@PUT`/`@POST` on `…/stages/{stageName}/content`) and the
  **transition** action have to carry the property forward, or a content edit silently
  unpublishes the model. Same class of bug as the debounce in §8, in a different place.
- **the key belongs in a shared constant.** The property name is now API between two bundles;
  `WorkflowConstants` is where the atlas keeps that kind of thing.
- **the value's type.** `properties` is `String → EJavaObject`, so `true` the boolean and
  `"true"` the string are both storable and only one of them is what the publisher tests.
  Store the boolean, read defensively.

### The publisher side: the flag rides on the service properties

**Decided 2026-08-26: `DynamicEPackageRegistrationService` copies `dcat` onto the registered
`EPackage` service**, beside `emf.nsURI`, `emf.model.scope`, `atlas.stage`, `emf.version`,
`emf.fingerprint` and `dynamic.registration`. `PackageServiceTracker`'s target filter then does
the selecting:

```
(&(dynamic.registration=true)(dcat=true)(emf.model.scope=*)(atlas.stage=*)(emf.nsURI=*))
```

No metadata lookup, no cache, no `ObjectMetadata` dependency in the publisher at all. The
metadata stays the system of record; the service property is its projection into the registry,
which is where the publisher was already listening.

**And unpublication comes free with it.** DS re-evaluates a target filter when a service's
properties change, so flipping the flag to `false` makes the `EPackage` service stop matching
and the tracker **unbinds**. Publish and retire are the same bind/unbind path the tracker
already implements — no second notification channel, no polling, and the §8 debounce covers
the update case exactly as it already does.

Two things this makes load-bearing:

- **the copy has to happen on every registration path.** `DynamicEPackageRegistrationService`
  is the single funnel (§1.2, hook 2), which is what makes this cheap — but the property has to
  be read from `ObjectMetadata` at that point, so registration now consults metadata where the
  publisher used to.
- **a flag change must reach the service properties**, or nothing fires. For the new endpoint
  below that means `ServiceRegistration.setProperties` on the live registration, or an
  unregister/re-register through the funnel. Whichever it is, it is the endpoint's job, not the
  publisher's.

### Stages: final only, by default

**Decided 2026-08-26.** A `dcat=true` upload to `draft` records the intent; it does not put the
model in a portal. Publication additionally requires the stage to be permitted, and by default
that means the registry's **final** stages only — `StageInfo` already carries the `final` flag
(§1.2, hook 4) that decides it, so nothing new has to be modelled.

```
publish.stages = FINAL          # FINAL (default) | ALL | an explicit list, e.g. ["release","qa"]
```

This **narrows O3** rather than replacing it: publication is still limited to what the atlas
actually serves — a package must be *registered* to be a candidate — and the stage gate then
narrows that further. The property that makes the catalogue honest is unchanged: every published
`accessURL` resolves, because a published Dataset's stage is one the atlas serves from.

Worth noting what this makes possible that the flag alone could not: a model can be marked for
publication early in its life and reach the portal by being *promoted*, with no second action by
anybody. A `transitionToStage` into a final stage re-registers the EPackage service, the filter
matches, the tracker binds, the Dataset appears. That is the workflow driving the catalogue,
which is the right way round.

### The SPI stays

```java
public record PublicationTarget(String scope, String stage, String nsUri,
                                String version, String fingerprint) {}

@ConsumerType
public interface DcatPublicationPolicy {
    boolean publishScope(String scope);
    boolean publish(PublicationTarget target);
}

@ConsumerType
public interface DcatMetadataSource { /* title/description/publisher/license/theme/keywords */ }
```

Whiteboards, highest `service.ranking` wins. The default `DcatPublicationPolicy` is now
`MetadataPublicationPolicy`; a configured or annotation-driven one stays available as a second
implementation, at no cost today.

**The verdict is three-level**, and this is the part not to lose:

1. **Scopes are opt-in, from configuration.** A scope publishes only if a `DcatPublisher`
   configuration names it. `ObjectMetadata` cannot express "this deployment publishes to that
   portal" — that is a property of a deployment, not of a model — so the guarantee that nothing
   reaches a public portal because somebody uploaded a package with a query parameter has to
   come from the operator's side.
2. **Stages are gated, from configuration.** `publish.stages`, final-only by default.
3. **Within a published scope and a permitted stage, the package's own metadata decides.**
   `properties["dcat"]`, absent meaning false — enforced by the tracker's filter rather than by
   a policy call.

Levels 1 and 2 are the operator's, level 3 is the model's. Nothing publishes unless all three
agree.

The `exclude`/`include` glob language of the previous draft is dropped: the metadata flag does
per-package selection now, and a second rule language over the same decision is the kind of
thing that ends up disagreeing with itself.

```json
"DcatPublisher~jena": {
    "dcat.portal.target": "(dcat.portal=jena)",
    "atlas.public.base.uri": "$[env:ATLAS_PUBLIC_BASE_URI;default=http://localhost:8080]",
    "scopes": ["jena"],
    "publish.stages": "FINAL",
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

---

## 7b. A metadata endpoint, with an allowlist

**Decided 2026-08-26.** Re-uploading a package with `?dcat=true&overwrite=true` should not be
the only way to change its mind about publication, so a small endpoint edits the metadata
directly — restricted to fields that are labels, never fields that are identity.

```
PATCH /{scope}/schema/stages/{stage}/metadata?nsUri={enc}
If-Match: "<etag from the metadata GET>"
```

`PATCH`, not `PUT`: a whole-document `PUT` of an `ObjectMetadata` invites exactly the identity
overwrite this endpoint exists to forbid. And `If-Match` is already available — the
`ObjectMetadataResponseFilter` puts a strong `ETag` over metadata state on every metadata GET
(and `Last-Modified` from `getLastChangeTime()`), so conditional update costs nothing new.

**Editable:**

| field | why |
|---|---|
| `properties`, restricted to a **key allowlist** | `dcat` today; the per-model DCAT metadata of §6 later. `nsUri` is *not* in the allowlist — it is identity that happens to live in this map |
| `objectName` | the human-readable name, which is what a label is |
| `lastChangeReason` | the audit note explaining this very edit |
| `governanceDocumentationId` | a pointer, label-shaped |

**Refused, with the offending field named in a 400** — never silently ignored, because a
metadata editor that quietly drops half a request is how somebody comes to believe they changed
a publisher when they did not:

| group | fields |
|---|---|
| identity | `objectId`, `objectRef`, `scope`, `stage`, `registry`, `version`, `properties["nsUri"]` |
| content-derived | `contentHash`, `fingerprint`, `generationTriggerFingerprint` |
| provenance | `uploadUser`, `uploadTime`, `sourceChannel`, `objectType` |
| workflow-owned | `status`, `isReadOnly`, `review*`, `compliance*` — the review and stage machinery owns these, and a label editor is not a back door into it |
| server-maintained | `lastChangeUser`, `lastChangeTime` — set by the endpoint, not sent to it |

**It has to propagate.** Because the `dcat` flag now rides on the `EPackage` service properties
(§7), an edit that changes it must update the live service registration — otherwise the metadata
says one thing, the registry says another, and the publisher believes the registry. That is the
endpoint's responsibility and it is the one part of this that is not merely CRUD.

**Two things left open**, deliberately:

- **authorization.** This endpoint changes whether a model appears in a public catalogue, which
  makes it a more consequential write than its size suggests. Whatever the atlas does for the
  upload endpoints applies here, and if that is currently nothing, this is a good moment to say
  so out loud rather than to discover it.
- **`isReadOnly` packages.** An inherited package reports its parent's scope; editing its
  metadata through a child scope's URL should be a `409`, not a silent write to the parent's
  record. Needs confirming against what `isReadOnly` actually guards today.

---

## 7a. The Catalog for a scope: adopted, configured, or derived

A scope's Catalog may already exist in the portal, created by someone else. Three cases, one
factory configuration, `DcatScopeCatalog~{scope}`:

| case | configuration | ownership |
|---|---|---|
| **adopted** | `catalog.id` + `catalog.adopt = true` | not ours. Link Datasets in; never `PUT`, never `DELETE` |
| **configured** | `catalog.id` (optional) plus `catalog.title`, `catalog.description`, `catalog.publisher.*`, `catalog.license.uri`, `catalog.theme`, `catalog.homepage` | ours. Created and reconciled from these attributes |
| **derived** | no `DcatScopeCatalog` configuration for the scope | ours. `ScopeInfo.name` / `getDescription()` plus the `DcatPublisher` defaults of §6. The behaviour of every version of this plan so far |

The **root scope is not a special case**: `atlas` gets its Catalog by these same three rules. It
is only distinguished by position — every other scope descends from it, so its Datasets are
linked into every other Catalog (§4).

```json
"DcatScopeCatalog~jena": {
    "scope": "jena",
    "catalog.id": "stadt-jena-opendata",
    "catalog.adopt": true
}
```

```json
"DcatScopeCatalog~verkehr": {
    "scope": "verkehr",
    "catalog.title": "Verkehrsmodelle Jena",
    "catalog.description": "Datenmodelle des Verkehrsbetriebs",
    "catalog.publisher.name": "Stadt Jena",
    "catalog.license.uri": "http://dcat-ap.de/def/licenses/dl-by-de/2.0"
}
```

**Ownership comes from configuration, never from inspecting the portal.** "Did we create this
Catalog?" is not answerable by reading it — nothing in the DCAT graph records authorship — and
guessing wrong is destructive in one direction. `catalog.adopt` is the whole answer, and
`CatalogResolver` is the only component that consults it.

**Why an adopted Catalog is read-only to us.** `registerCatalog` is a `PUT`, a `PUT` replaces,
and `dcat:dataset` membership lives on the Catalog. Re-registering an adopted Catalog drops
every dataset link it holds, other publishers' included, and a `CASCADE` delete on it reaches
their Datasets. `linkDatasetToCatalog` / `unlinkDatasetFromCatalog` are additive, and are the
only two operations the resolver permits there.

**An adopted Catalog that is not there is a configuration error, not a licence to create one.**
The operator asserted that id exists. If `client.catalog(id)` comes back empty: refuse the
scope, log once, and let `DcatPublisherHealthCheck` report it. Minting a Catalog under an id in
somebody else's portal is the one failure mode with no clean recovery. A
`catalog.create.if.missing` switch can exist for operators who disagree; default false.

**Metadata precedence** for an owned Catalog: `DcatScopeCatalog` attributes → `ScopeInfo` →
`DcatPublisher` defaults. A configured title wins over the scope's name, and a scope that
configures nothing behaves exactly as the derived case — which is what makes all of this
backward compatible with §4 as written.

### As built (D1a, 2026-08-27)

- `DcatScopeCatalog` is a factory component registering itself as a service, tracked by every
  publisher. **Not per portal, deliberately:** a Catalog's ownership is a fact about the catalogue,
  not about which of this atlas's publishers is talking to it, and having to repeat it per portal is
  how one of them ends up writing a Catalog it does not own.
- **An unusable configuration still activates**, carrying `CatalogSettings.invalidReason()`.
  Refusing to activate would remove the service, and the *absence* of a service is how the derived
  case is expressed — so a broken `catalog.adopt` would silently become "write a Catalog under the
  scope name", the opposite of what it asked for. `CatalogResolver` turns it into a `REFUSED`
  resolution instead, and a refused scope publishes nothing at all: its Catalog is the precondition
  for its Datasets.
- **`DcatIds.catalogId` is no longer the answer**; every call site resolves through
  `resolveCatalog(scope)`, because a configured id replaces the scope name.
- **The refusal has to be re-checked on the client thread.** `publishPackage` gates on the DS
  thread, but whether an adopted Catalog exists is only answered inside a submitted task — and the
  client's executor is single-threaded, so the Catalog task queued first has finished by the time
  the Dataset task runs. Checking only up front published the first Dataset of a refused scope into
  no Catalog at all. Caught by `aMissingAdoptedCatalogRefusesTheScopeInsteadOfCreatingIt`.
- **No sub-catalog link at all** (O14). What began as "never on an adopted Catalog" is now the rule
  for every Catalog: `relinkSubCatalogs` is gone, and `ScopeHierarchy` serves the Dataset fan-out
  only.
- `unpublish.mode` is capped at `UNLINK` when **any** Catalog listing the Dataset is adopted, not
  only when the defining scope's is — the fan-out of D2a means a descendant's adopted Catalog is
  just as reachable by a `CASCADE`. Announced once at activation.
- **Still open, and no longer an ownership question:** retiring a *Catalog* when its scope goes away
  (§8, row 4). Ownership is now known, but the portal's delete semantics for a Catalog that still
  lists Datasets, and the ordering against those Datasets' own concurrent retirements, are not — and
  the operation is destructive enough to want an explicit decision rather than an inference. Under
  `UNLINK` the outcome is already correct without it.

---

## 8. Unpublication

Selective publication implies it, and the shapes differ:

| what happened | what the portal should say |
|---|---|
| a package is deleted, or transitions out of a stage that has `delete.after.transition` | the stage URL stops serving it → its Dataset should go |
| the `dcat` flag is cleared, or a promotion leaves a permitted stage | same — and it arrives as a tracker *unbind*, because the flag is a service property (§7) |
| the policy changes so a target is no longer publishable | same |
| a scope's configuration is deleted | its Catalog and everything only reachable through it — **unless the Catalog is adopted**, in which case only our Datasets are unlinked from it (§7a) |
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

Note from the client's P1: a cascade delete of a Dataset unlinks its catalog and service
links. With no `DatasetSeries` in the mapping (O2, revised) there is no second membership to
reason about and no orphaned series to reap — one of the concrete simplifications the
2026-08-26 decision buys.

**Unlinking means every Catalog.** A Dataset is linked into its own scope's Catalog and every
descendant's, so retiring it is a fan-out exactly mirroring §4's — computed from
`ScopeHierarchy`, not from reading the portal back. A `CASCADE` delete does it for us and
reports what it unlinked; the other modes have to do it themselves, and a missed descendant
leaves a Catalog advertising a Dataset that is gone.

**`unpublish.mode` is capped by ownership.** For a scope with an adopted Catalog, `DELETE` and
`CASCADE` are downgraded to `UNLINK` and the downgrade is logged once at activation, not per
event. Our Datasets are ours to delete; what a shared Catalog looks like afterwards is not
ours to decide.

### As built (D5, 2026-08-27)

- `unpublish.mode` (default `UNLINK`), `unpublish.delay.seconds` (default 30) and
  `retire.on.shutdown` (default `false`) are on `DcatPublisherConfig`. `UnpublishMode` has to be
  **`public`** even though its package is private: an enum attribute is read through the JDK proxy
  that realizes the annotation, and a package-private type fails there with an
  `IllegalAccessError` at activation rather than at compile time.
- The two guards collapse into one question — `retirementAllowed()` — which is `active &&
  framework != STOPPING`. The second half covers a shutdown, the first covers everything else that
  unbinds our references without saying anything about a model: a configuration update, a bundle
  refresh, our own deactivate. On the stopping path the unbind deliberately **keeps** its
  `trackedPackages` entry, because that map is what `retire.on.shutdown` works from.
- `RetirementQueue` owns the debounce (one pending task per key, cancel on re-register) and is unit
  tested without a portal; it abandons rather than drains on close.
- **A retirement drops the fingerprint and remembers the id.** Otherwise a re-publish of unchanged
  content takes the "already published" shortcut and never re-asserts the Catalog membership the
  retirement dropped — the same invariant as the Catalog-rewrite bug, from the other direction.
- **`DELETE` unlinks first, then deletes `SINGLE`.** Our own membership is ours to drop, and
  `SINGLE` then refuses only on a referrer we did not create, which is the check worth keeping.
- **`NONE` keeps the package tracked** rather than dropping it, because a Catalog `PUT` replaces:
  an untracked Dataset would lose the membership `NONE` promises to keep at the next Catalog write.
  The guarantee holds while the publisher is active; across a restart a Dataset whose package is
  gone from the runtime is not re-asserted, since nothing left knows it should be. Preserving
  memberships this publisher did not create is the same problem as writing to an adopted Catalog,
  and belongs with D1a.
- **Deferred, and on purpose:** the unlink does **not** fan out over descendant Catalogs (added in
  D2a), and a vanished scope's **Catalog resource is left in place**. Under `UNLINK` the outcome is
  already right — the scope's packages unbind with it, so each Dataset retires itself and the
  Catalog ends up listing nothing. D1a settled the ownership half of the Catalog-deletion question;
  what remains is the portal's delete semantics for a Catalog that still lists Datasets and the
  ordering against their own retirements. See §7a's as-built note.

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
| **D0** | The atlas-side `?dcat=true` query parameter and the shared constant, on all three write paths of §7 | upload with and without the flag; `GET …/stages/{stage}?nsUri=` shows `properties["dcat"]`, and a content update preserves it |
| **D1** | Bundle scaffolding, `DcatPublisher` config, client reference, readiness gate, one derived `Catalog` | a scope appears as a Catalog in a portal container |
| **D1a** ✅ | `CatalogResolver` and `DcatScopeCatalog`: all three cases of §7a | adopted catalog gets links and no `PUT`; configured catalog carries its own title; a missing adopted id fails the health check instead of creating one |
| **D2** | `DcatIds`, `DcatMapper`, `ConfiguredMetadataSource`; the full sequence of §4 for one target, driven by a gogo `dcat:reconcile` | `dcat:dataset` link present, distributions ≥ 1, each with its own `downloadURL`, read back |
| **D2a** ✅ | `ScopeHierarchy` + the link fan-out both ways | `atlas → jena → nawerker`: an `atlas` package's Dataset is one resource in three Catalogs; creating `nawerker` last still gets it |
| **D3** | `PackageServiceTracker` + the work queue + fingerprint/ETag state | an upload through the REST API lands in the portal without a manual step; a restart re-publishes nothing |
| **D4** | `MetadataPublicationPolicy`: the `WritableScopeService` lookup, the cache, the scope gate | a flagged package publishes, an unflagged one does not, a scope absent from `scopes` publishes nothing whatever its packages say |
| **D5** ✅ | Unpublication: modes, the STOPPING guard, the debounce | a restart retires nothing; a delete retires exactly one Dataset |
| **D6** ✅ | Error classification, backoff queue, health check | a 503 retries, a SHACL refusal does not |
| **D7** | OSGi ITs against a portal container; the runtime config bundle and bndrun variant | `testOSGi` green |

D1–D3 is the walking skeleton and the honest end of "does this work". D4–D6 is what makes it
operable.

---

## 11. Decisions

O1–O6 were resolved by the owner on **2026-08-25**. On **2026-08-26** O2 and O4 were reversed,
O3 was narrowed, and O7–O13 were added. Kept here as the record of *why*, since each one is a constraint on the
implementation rather than a preference.

| # | question | decision |
|---|---|---|
| **O1** | the id encoding | **`b64url(nsURI)`**, as proposed |
| **O2** | series per scope or global | ~~per scope~~ → **no `DatasetSeries` at all** (2026-08-26) |
| **O3** | which stages get published | what is registered, **narrowed to final stages** (`publish.stages`, 2026-08-26) |
| **O4** | inherited packages | ~~not published~~ → **published, by linking one Dataset into every descendant Catalog** (2026-08-26) |
| **O5** | do we need `ExitReason` | **no** |
| **O5b** | should the portal reflect availability | **not from here** — the DCAT side already has an issue for it |
| **O6** | one portal or several | **one for now**; the factory shape stays |
| **O7** | what says a package is publishable | **`ObjectMetadata.properties["dcat"]`**, set by `?dcat=true` at upload |
| **O8** | does configuration still gate anything | **yes — scopes stay opt-in**; metadata decides packages within them |
| **O9** | whose Catalog is it | **adopted / configured / derived** (§7a); ownership from configuration only |
| **O10** | where the public URL comes from | **`atlas.public.base.uri`, including the path prefix** — an APISIX fronts the atlas |
| **O11** | how many entity types | **three: Catalog, Dataset, Distribution** — no `DataService` |
| **O12** | can the flag be changed after upload | **yes**, a `PATCH` metadata endpoint with a field allowlist (§7b) |
| **O13** | how the publisher learns the flag | **an `EPackage` service property**, so the tracker's filter decides and unbind means retire |
| **O14** | are Catalogs nested with `dcat:catalog` | **no — never, for any Catalog** (2026-08-27) |

**O1 — `b64url(nsURI)`.** A portal id is permanent and a slug collision is not repairable after
publication, so opacity wins over readability. `dct:identifier` carries the readable nsURI and
`dct:title` the readable name, so nothing is actually lost except the look of the URL.

**O2 — no `DatasetSeries` (revised 2026-08-26).** An EPackage is a Dataset under a Catalog with
its Distributions, and that is the whole hierarchy. The original question — per scope or global —
is moot. What this costs is the link joining one nsURI's Datasets across stages; a consumer
wanting all of them searches `dct:identifier`. What it buys is that the entity the client's own
documentation uses as *the* example of an unwritable read-modify-write is never written, the
cascade has one membership to reason about instead of two, and §4 loses two steps. Re-adding it
is a widening: `registerDatasetSeries` and `linkDatasetToSeries` are already in the client and
no Dataset changes shape to gain an `inSeries` link.

**O7 — the flag comes from the metadata model.** Publishability is a fact about a package that
somebody asserts when they upload it, so it belongs with the package's other governance facts,
not in a deployment's configuration file and not in the `.ecore`. `properties` is already the
map the upload path writes to, and it is queryable and mutable through an API that exists. The
only cost is one metadata lookup per registration event (§7), and even that may be removable by
propagating the flag onto the EPackage's service properties — a separate ask on the atlas side.
Reading it through the workflow API rather than `ReadableScopeService` is not a cost: this bundle
lives inside model.atlas, and one component provides both faces.

**O8 — configuration still gates scopes.** This is the §7 safety property surviving O7 intact.
A flag in a package's metadata says "this model may be published"; it cannot say "this
deployment publishes to that portal", because it knows nothing about deployments or portals. If
the metadata were the only gate, uploading a package with a query parameter would be enough to
put a model into a public catalogue — which is precisely the property the two-level verdict
exists to prevent.

**O10 — `{base}` is configured, prefix and all.** There is an APISIX in front, so the public URL
is not a scheme-and-host substitution on the container's own address: the gateway can rewrite the
path, and the publisher has no request context to learn any of it from. Making the configured
value the entire public prefix — rather than a host that we then append `/atlas/rest` to — means
one property covers host, scheme, port and any rewrite, and no code has to know what the gateway
was told. The two consequences worth carrying forward are in §6: the routes have to be anonymous
or the published URLs are useless, and activation must not depend on the atlas being able to
fetch its own public address, because from inside the network it usually cannot.

**O9 — three Catalog cases, ownership from configuration.** The portal is shared: a scope's
Catalog may predate us and outlive us. Since nothing in a DCAT graph records who created a
resource, ownership cannot be discovered, only declared — so `catalog.adopt` declares it, and
everything destructive keys off that one flag. The asymmetry in §7a is deliberate: guessing
"ours" when it is not loses somebody else's dataset links to a `PUT`, while guessing "not ours"
when it is only leaves a Catalog slightly stale.

**O3 — publish what is registered, and only from final stages.** The reconciler still
enumerates through the *service registry* view of a scope, so the §2 limitation stands as
intended behaviour rather than a caveat, and the honesty property is unchanged: every published
`accessURL` resolves, because a published Dataset is one the atlas serves. **Narrowed
2026-08-26:** being registered is now necessary and not sufficient — `publish.stages` gates it,
final-only by default, so a `dcat=true` upload to `draft` records intent without publishing.
The good consequence is that promotion publishes: a `transitionToStage` into a final stage
re-registers the service, the filter matches, the Dataset appears, and nobody had to do a second
thing. No `storage-visible` mode is built.

**O4 — inherited packages are published, by linking (reversed 2026-08-26).** The old answer
avoided a real problem the wrong way: `publish.inherited=true` would have minted an additional
Dataset per inheriting scope, so one model became N resources with N ids and a harvester saw N
models. The fix is not to omit the model from the child catalogue but to stop duplicating it —
**one Dataset, registered under the scope that defines it, linked into every descendant
Catalog**. `atlas → jena → nawerker` gives `nawerker` its own Datasets plus `jena`'s plus
`atlas`'s, and an `atlas` package is one resource appearing in three catalogues. The old
surprise ("the child API serves it but the child catalogue does not list it") disappears rather
than needing a README paragraph; what replaces it is the link bookkeeping in the mapping's §3,
including the case that is easy to forget — **a scope created later must have every ancestor's
Datasets linked into it**.

**O11 — three entity types.** Dropping `dcat:DataService` removes `endpointURL`,
`endpointDescription`, `servesDataset` and `accessService` from the mapping. The API still serves
the content; it is simply not a catalogue resource. Two things follow. The catalogue now makes
only the narrower claim — *these models exist, here is where to fetch them* — rather than also
describing the atlas as a service, which is the claim it can actually keep. And `{base}` is now
used in exactly one place, the Distribution URLs, so O10's failure mode is smaller. The cost:
`dcat:accessService` (FR-10) loses its first consumer and stays unexercised, along with FR-11.

**O12 — a metadata endpoint with an allowlist.** The alternative was leaving
`?dcat=true&overwrite=true` as the only route, which means re-uploading content to change a
label. The allowlist is the whole design (§7b): editable fields are labels, everything carrying
identity, provenance, content hashes or workflow state is refused *by name* in a 400. The part
that is not CRUD is propagation — the flag lives on the `EPackage` service properties too (O13),
so an edit has to reach the live registration or the publisher never learns.

**O13 — the flag rides on the service properties.** Asked for as an optimisation, kept for a
better reason: it makes retire the same code path as publish. DS re-evaluates target filters on
a service-property change, so `(dcat=true)` in `PackageServiceTracker`'s filter means clearing
the flag *unbinds* the service and the existing unpublish path runs. No metadata lookup, no
cache, no second notification channel. What it costs is that
`DynamicEPackageRegistrationService` now reads `ObjectMetadata` at registration time — the
lookup moved rather than vanished, but it moved to the place that was already loading the
metadata anyway.

**O5 — no `ExitReason`.** This settles the hook choice for good: **hook 1 (`StageActionService`)
is not needed at all**, not now and not as a planned second phase. Deleted and
transitioned-away both mean the stage URL stops serving the package, and the portal says the
same thing either way. Hook 2 alone is the trigger, and no deployment's `workflow.json` is
touched — the "no config changes" property of §2 is now permanent, not provisional.

**O14 — no sub-catalogs anywhere (decided 2026-08-27).** D1a had arrived at "never on an adopted
Catalog", for ownership reasons. The owner extended it to every Catalog, and the reason is better
than the ownership one: **the hierarchy is already in the catalogue, carried by the Datasets.** O4
puts an inherited model into its descendants' Catalogs, so `jena`'s Catalog already shows what
`jena` serves from `atlas`. A `dcat:catalog` link would state the same fact a second way, in a form
a harvester may traverse — and one that walks both the sub-catalogue and its parent counts the same
Dataset twice. It also removes the last operation this publisher performed *on* a resource other
than the one it was writing, which is what made ownership a question at every Catalog write. The
mapping is now exactly: a Catalog per published scope, a Dataset per publishable package-in-a-stage,
Distributions contained in it, and `dcat:dataset` links from Datasets into Catalogs. Nothing else.

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

