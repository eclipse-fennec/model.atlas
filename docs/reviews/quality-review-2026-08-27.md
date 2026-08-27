# Quality review — model.atlas — 2026-08-27
Mode: changes (branch `dcat_connection` vs `merge-base origin/snapshot` = `6d3d25c`) · Scope: the 17 commits of the DCAT integration · Rule sets: SOLID/OSGi + Eclipse Foundation (see skill references)

## Summary

| severity | count | categories |
|---|---|---|
| blocker | 0 | — |
| major | 3 | configuration (1), api-contract (1), correctness (1) |
| minor | 11 | osgi-ds (4), correctness (3), http-semantics (2), build/IDE (2) |
| info | 7 | api-evolution, dead code, SRP, test coverage |

91 files changed, 55 of them `.java`, across `…atlas.dcat` (new), `…atlas.dcat.tests` (new),
`…atlas.workflow`, `…atlas.rest.application`, `…atlas.mediatypes.api`/`.impl`, three new
runtime-config bundles and a new docker module.

**The foundations are clean.** Every changed `.java` file carries the EPL-2.0 header with SPDX and a
Contributors section; the new bundle exports exactly one versioned API package (`…dcat.api`,
`@Version("1.0.0")`) containing an SPI interface and a record, with `…dcat.internal` private; all
three new test/config bundles carry `-maven-release: local` per the repo convention; `deactivate`
undoes `activate` in the publisher, both executors are closed, no static mutable state, no foreign
impl imports, no reflection, no `new`-ing of services. The heavy logic is extracted into nine
plain-Java collaborators that are unit-tested without a framework (73 unit tests, 24 OSGi ITs).

What the findings concentrate on is the seam between *configuration and reality*: values that are
never validated (F1), an exported SPI whose documented fallback does not happen (F2), and a
forced-rewrite marker that a transient failure can consume (F3). None of these was caught by the
test suite, and F1 was found only by running the docker image with a variable unset.

Findings marked **verified live** were reproduced against a running atlas and a real portal
container; the rest were verified by reading the exact code path.

## Findings

### F1 · major · configuration · runtime.config.docker.jena.dcat + …atlas.dcat
- **Where:** `org.eclipse.fennec.model.atlas.runtime.config.docker.jena.dcat/configs/dcat.json:32`
  (`publisher.name`), and the missing guard in
  `org.eclipse.fennec.model.atlas.dcat/src/…/internal/DcatPublisher.java:576-600` (`activate`)
- **What:** a required environment variable that is not set is published to the portal as its own
  literal placeholder text.
- **Verified live:** with `DCAT_PUBLISHER_NAME` unset, the portal stored
  `<name value="$[env:DCAT_PUBLISHER_NAME]" lang="de"/>` as the Catalog's `dct:publisher`, and
  `/atlas/system/health?tags=atlas` reported the publisher **OK** throughout.
- **Why it matters:** `dct:publisher` is the field that says who governs the data; a placeholder
  there is visible to every harvester, and nothing in the atlas notices. `atlas.public.base.uri`
  escapes this only by accident — `PublicBaseUri.validate` happens to reject `$[env:…]` as an
  invalid URI — which shows the guard belongs at activation, not per property.
- **Suggested fix:** in `activate`, refuse to activate when any configured string still contains
  `$[env:`, naming the property and the variable. It is the same shape as the existing base-URI
  refusal, and turns a silent public-catalogue defect into a startup failure an operator can read.

### F2 · major · api-contract · …atlas.dcat
- **Where:** `…/internal/DcatPublisher.java:598-599`, with `…/internal/DcatMapper.java:151-153`
- **What:** a registered `DcatMetadataSource` *replaces* the configured defaults instead of layering
  over them, contradicting the exported SPI's own javadoc — and it is snapshotted at activation, so
  the whiteboard's documented "highest ranking wins" does not apply after that.
- **Why it matters:** `DcatMetadataSource`'s javadoc promises "Every method may return an empty
  `Optional` … in which case the publisher falls back to its configuration — so an implementation
  can answer for one field and stay silent about the rest." `ConfiguredMetadataSource` is used *only*
  when no whiteboard service exists, so the first third party who implements the SPI to override a
  title finds that `toDistribution`'s `licenseUri(...).orElseThrow(...)` fires even though
  `license.uri` is configured — and the failure is recorded as *permanent*, so nothing retries it.
  `themes`/`keywords` silently degrade to empty the same way. Only `publisherName` layers
  (`DcatMapper:212`). Nothing in the repo implements the SPI yet, which is why no test caught it.
- **Suggested fix:** compose rather than substitute — wrap the whiteboard source and
  `ConfiguredMetadataSource` in a fallback source that tries the former and falls through on empty,
  and read the volatile field per call (or rebuild the mapper on bind/unbind) so a rebind takes
  effect. The mediatypes reference in the same class already does the latter correctly.

### F3 · major · correctness · …atlas.dcat
- **Where:** `…/internal/DcatPublisher.java:949-955`
- **What:** the forced-rewrite marker is consumed before the write, so a transient failure during a
  marked rewrite downgrades the retry into the "already published at this fingerprint" shortcut.
- **Why it matters:** `submit` retries by re-running the same lambda. On the retry
  `pendingRewrites.remove(datasetId)` returns false, and the path falls through to the fingerprint
  check — where the retirement has already cleared the in-memory fingerprint while the portal still
  holds the resource at the *same* fingerprint. The publisher concludes "already published; no
  write", and the Dataset stays in no Catalog. A restart repeats the same reasoning, and the health
  check is green because the retry succeeded. This defeats precisely the case the set exists for
  (its javadoc at `:159-169`), and it needs only a portal blip while someone flips the flag.
- **Suggested fix:** consume the marker only on success — `if (pendingRewrites.contains(id)) { Dataset d = writeDataset(...); if (d != null) { pendingRewrites.remove(id); } return d; }`.

### F4 · minor · osgi-ds · …atlas.dcat
- **Where:** `…/internal/DcatPublisher.java:420-433` (`unbindScopeCatalog`)
- **What:** the Catalog settings are removed by scope name without checking the entry still belongs
  to the departing service.
- **Why it matters:** two `DcatScopeCatalog` configurations naming the same scope — an operator typo
  — overwrite each other on bind, and unbinding *either* wipes the survivor's settings. The scope
  then resolves as derived and therefore `OWNED`, and the next `publishCatalog` **PUTs a Catalog the
  operator declared adopted**, which is the one destructive outcome `CatalogResolver`'s javadoc says
  the design exists to prevent. The mediatypes reference in the same class has exactly this identity
  guard (`:204`).
- **Suggested fix:** remove only when the stored settings are the departing service's, and log a
  warning when a second configuration claims a scope that already has one.

### F5 · minor · correctness · …atlas.dcat
- **Where:** `…/internal/DcatPublisher.java:973-976`
- **What:** the fingerprint shortcut returns without asserting the Catalog membership, so the
  outcome depends on whether packages happen to be tracked before their scope's Catalog is written.
- **Not reproduced:** in the docker deployment the packages are tracked first, so a restart kept the
  membership (verified live: Catalog still listed the Dataset after a container restart, with no
  Dataset write). The hazard is real in principle — `writeCatalog` PUTs, dropping every membership,
  and `relinkDatasets` re-asserts only what is tracked at that instant — but it needs the reverse
  arrival order, and the existing IT `aCatalogRewriteKeepsItsDatasetMemberships` covers only the
  order that works.
- **Why it matters:** if it ever inverts (a slower registry replay, a scope service that binds
  earlier), the Catalog lists nothing while every Dataset still exists, and nothing re-asserts it:
  there is no reconcile job, and the health check stays green.
- **Suggested fix:** in the skip branch, still run the link fan-out before returning. It is additive
  and idempotent — the property the rest of the design already relies on — and it removes the
  dependence on arrival order entirely.

### F6 · minor · osgi-ds · …atlas.dcat
- **Where:** `…/internal/DcatPublisher.java:851` (retire) vs `:318` (publish)
- **What:** publishing and retiring a scope's Catalog share the retry key `"catalog:<scope>"`, while
  the Dataset paths deliberately use distinct keys (`"dataset:"` vs `"retire:"`).
- **Why it matters:** `RetryQueue` keeps one attempt counter and one pending retry per key, so the
  two operations share a `retry.max.attempts` budget, a failing retirement replaces a pending
  publish retry, and the health check collapses them into one line describing only one of them.
- **Suggested fix:** prefix the retirement's submit key (`"retire-catalog:" + scope`), leaving the
  `RetirementQueue` key as it is.

### F7 · minor · correctness · …atlas.dcat
- **Where:** `…/internal/DcatPublisher.java:984-1001` (`writeDataset`)
- **What:** the Dataset is registered before the Distributions are built, so a missing `license.uri`
  leaves an orphan Dataset in the portal.
- **Why it matters:** `mapper.toDistribution` throws for a missing licence — deliberately, for a
  readable message — but `registerDataset` has already succeeded by then, and the link fan-out is
  after the loop. The portal keeps a Dataset with no Distribution in no Catalog, for a pure
  configuration error, until an operator fixes the config and the component reactivates.
- **Suggested fix:** resolve the licence (or build the Distribution list) before `registerDataset`,
  so a misconfiguration fails before anything is written.

### F8 · minor · osgi-ds · …atlas.dcat
- **Where:** `…/internal/DcatPublisher.java:740-743` (`retireEverything`)
- **What:** `retire.on.shutdown` failures are swallowed — `onResolve` counts the latch down for both
  outcomes, and only the *timeout* is logged.
- **Why it matters:** with an unreachable portal every promise fails immediately, the latch reaches
  zero, and the only trace is the earlier optimistic "unlinking N Dataset(s)". The operator who
  deliberately opted into shutdown retirement gets no signal that none of it happened.
- **Suggested fix:** add an `onFailure` that logs at WARNING with the dataset id.

### F9 · minor · osgi-ds · …atlas.mediatypes.impl
- **Where:** `…/mediatypes/impl/SupportedMediatypesImpl.java:88-93` (`publishProperties`)
- **What:** `setProperties` can throw `IllegalStateException` out of a DS reference callback when
  `deactivate` races an `updated` invocation.
- **Why it matters:** the volatile read can return a registration that `deactivate` unregisters
  before `setProperties` runs; the exception then escapes `bindResourceSet`, and SCR logs it as a
  component error. The sibling code added in this same branch
  (`RegisteredEPackage.updateProperty`) guards exactly this.
- **Suggested fix:** wrap the `setProperties` call in the same `catch (IllegalStateException)`.

### F10 · minor · correctness · …atlas.workflow
- **Where:** `…/workflow/registration/DynamicEPackageRegistrationService.java:163-188`
- **What:** `updateProperty` updates four registrations one at a time and swallows a failure at
  `FINE`, while `updateDcatFlag` still returns `true`.
- **Why it matters:** the publisher's tracker filters `(dcat=true)` on the *EPackage* service, but
  another consumer may filter the configurator or the Condition. A partial update leaves them
  disagreeing, and the only record is a log level production does not enable.
  `setProperties` can also throw `IllegalArgumentException`, which is not caught at all.
- **Suggested fix:** track per-registration success, log at WARNING, and return `false` when any
  registration was not updated. (The `registrationLock` does make the in-bundle race impossible, so
  the catch is defensive — but then the report should say so.)

### F11 · minor · observability · …atlas.workflow
- **Where:** `…/workflow/impl/RegistryServiceImpl.java:404-421` (`propagateDcatFlag`)
- **What:** propagation gives up silently in three places — no registration service bound, no
  `nsUri` metadata property, or `updateDcatFlag` returning `false`.
- **Why it matters:** the endpoint answers 200, storage says `dcat=true`, and nothing is ever
  published — the exact failure the method exists to prevent. The most likely trigger is ordinary:
  the operator patches a stage the package is not registered in.
- **Suggested fix:** log at INFO/WARNING on each exit, naming scope, stage and objectId.

### F12 · minor · architecture · …atlas.workflow
- **Where:** `…/workflow/impl/RegistryServiceImpl.java:143-145, 397-421`
- **What:** the generic registry service reaches directly into the concrete
  `DynamicEPackageRegistrationService`, bypassing the `StageActionService` whiteboard that already
  owns EPackage registrations.
- **Why it matters:** `EPackageStageActionService` is the component that owns the nsURI↔registration
  mapping and already holds that reference; after this change two components mutate the same
  registrations, and a second projected property means editing this method rather than adding a
  service. The code comment defends the location against the *REST endpoint*, which is right, but
  does not address the whiteboard one method away.
- **Suggested fix:** dispatch an `ActionEvent` and project in `EPackageStageActionService`. Noted as
  minor rather than major because the whiteboard route needs a new `ActionEvent` literal, i.e. an
  ecore change, which is the model owner's call — worth doing before a *second* property is
  projected, not necessarily now.

### F13 · minor · http-semantics · …atlas.rest.application
- **Where:** `…/exception/EndpointFailures.java:80-82`
- **What:** *any* `UnsupportedOperationException` in the cause chain becomes a 405 with its message
  forwarded to the client, and is no longer logged.
- **Why it matters:** it is one of the most commonly thrown JDK exceptions — mutating a `List.of`,
  an unimplemented EMF operation, a codec refusing a mode — and those are bugs, not refusals. The
  mapper only logs the `SERVER_ERROR` family, so they now vanish from the log as well. The intended
  sender is one known class of refusals from `AtlasSchemaRegistryService`.
- **Suggested fix:** have the refusing registry throw a dedicated subtype and match on that.

### F14 · minor · http-semantics · …atlas.rest.application
- **Where:** `…/exception/EndpointFailures.java:81`, response built in
  `…/exception/ModelAtlasExceptionMapper.java:106-109`
- **What:** the 405 carries no `Allow` header, which RFC 9110 §15.5.6 requires on every 405.
- **Why it matters:** a conformant client reads `Allow` to learn what it may do instead, and some
  intermediaries treat a 405 without it as a protocol error.
- **Suggested fix:** attach `Allow` when building the response — or use 403, since the method *is*
  allowed on the resource and it is the registry that forbids the operation.

### F15 · minor · build/IDE · runtime.config.docker.jena.dcat
- **Where:** `org.eclipse.fennec.model.atlas.runtime.config.docker.jena.dcat/` (no `.project`,
  no `.classpath`)
- **What:** the only new bundle on the branch without Eclipse project files; 69 other bundles have
  them, including its three siblings on this branch.
- **Why it matters:** it builds under Gradle but does not appear in a bndtools workspace, which is
  how the team works — so the file an operator most needs to edit is invisible in the IDE.
- **Suggested fix:** copy both files from `…runtime.config.local.jena.dcat` and change the `<name>`.

### F16 · minor · build/CI · repo
- **Where:** `.github/workflows/build.yml:31-35`
- **What:** CI exports only the apicurio and file runtimes, so neither jena bndrun — including the
  DCAT one — is ever resolved or exported in CI.
- **Why it matters:** this branch shipped a docker image whose jar contained **no DCAT bundles at
  all** (a plain `-include` overriding the resolved `-runbundles`) and every local build reported
  success. An export step in CI is the cheapest thing that would have failed.
- **Suggested fix:** add `export.modelatlas.runtime_docker_jena_dcat` (and `…_jena`) to the export
  step.

### F17 · info · api-evolution · …atlas.dcat, …atlas.workflow
- `…dcat.api` stayed at `@Version("1.0.0")` while `DcatPublicationPolicy` was **removed** from it,
  and the workflow bundle's exported packages gained members (`RegistryService.updateProperties`,
  `WorkflowConstants.DCAT_PUBLISH_METADATA_PROPERTY`) at `@Version("1.0")`. Both are breaches of
  §5/OSGi semantic versioning that the repo has deliberately accepted: prototype, snapshot-only
  publishing, baselining off (owner decision, recorded as F101 in the 2026-08-05 review). Listed for
  traceability, not as work.

### F18 · info · dead code · …atlas.dcat
- `DcatIds.catalogId` (`DcatIds.java:41-43`) now has no production caller — only `DcatIdsTest:64` —
  and duplicates the derivation that lives in `CatalogResolver:50`. Two homes for one id scheme that
  must stay in step. Delete it, or route the resolver through it.
- `AttributeDefinition` is imported but unused in `DcatPublisherConfig.java:16` and
  `ScopeCatalogConfig.java:16`.

### F19 · info · documentation · …atlas.workflow
- Three new members were inserted between an existing javadoc block and the member it documented, so
  the old text now reads as documentation of the new member and the original lost its javadoc:
  `DynamicEPackageRegistrationService.java:128-133` ("Container for all service registrations…" now
  sits above `FRAMEWORK_OWNED_PROPERTIES` — verified), the same file's 3-arg `unregisterEPackage`
  doc (now above `updateDcatFlag`), and `RegistryServiceImpl.java:127-145` (the stage-action-service
  rationale now above `ePackageRegistrations`).

### F20 · info · test coverage · …atlas.workflow
- `updateDcatFlag` and `RegisteredEPackage.updateProperty` are the subtlest new code in the change —
  read properties back off the `ServiceReference`, skip framework-owned keys, drop nulls, preserve
  `condition.id` — and are entirely untested. A regression dropping `emf.nsURI` or `condition.id`
  from an updated registration would break consumers silently. One test asserting on the
  `Dictionary` passed to `setProperties` would cover it.

### F21 · info · SRP · …atlas.dcat
- `DcatPublisher` is 1288 lines with 6 references, but the cohesion is largely real: nine
  collaborators are already extracted, and what remains shares the same maps and hierarchy snapshot,
  so splitting "tracking" from "publishing" would move coupling into a new interface rather than
  remove it. The one worthwhile split is `HealthCheck` — it is the component's only provided service,
  which is exactly why `immediate = true` had to become load-bearing (see the comment at `:84-87`).
  A separate health-check component reading a `report()`-shaped view would delete that subtlety.

## Skipped / not reviewed

- EMF-generated sources (`src-wf-api/`, `model/`, `WorkflowApiPackageImpl`): header check only, per
  the rule set. Headers present and correct.
- The repo-level release-readiness pass was **not** run in full: the change touches no root
  document, no `.licenserc.yaml`, no `tools/`, no `cnf/`. Only `.github/workflows/build.yml` was
  looked at, for F16. New `.java` files still got the header check (all clean).
- `docs/`, `.settings/`, `.classpath`/`.project` content, and the compose/Dockerfile text were read
  for F15/F16 but not otherwise reviewed for style.
- Deliberately not flagged, per the "do NOT flag" list and the code's own documented decisions:
  `AtlasSchemaRegistryService`'s `UnsupportedOperationException` refusals; the `emf.nsURI` spelling;
  the hand-written `@Capability(namespace="osgi.service")` without `effective:=active` (matches five
  other bundles); `SupportedMediatypesImpl`'s pre-`activate` first refresh (correct — the static
  mandatory reference binds before `activate`, which then registers with the finished list); the
  `@Reference` on a concrete class within one bundle's private packages; `immediate = true` on the
  publisher (documented, load-bearing).
