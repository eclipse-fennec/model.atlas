# Git Storage Backend — Implementation Plan (v2: external-write / read-only)

> **This supersedes the v1 plan.** v1 designed a full **read-write local JGit
> backend** where model.atlas committed to branches itself. The direction has
> changed: **writing happens externally, directly on GitHub** (PRs, pushes,
> merges by humans/CI). model.atlas no longer writes to git. It becomes a
> **read-only, remote-backed consumer** that stays in sync via git hooks
> (webhooks) and serves reads — with object **metadata cached locally** so
> metadata-only queries never hit git.
>
> **Anchor: there is working reference code to lift from — the MDO git
> extensions in `/opt/git/jena-MDO`.** See §0. This plan is largely "port the
> reference mechanics into a model.atlas storage backend so scope/registry/stage
> and `ReadableScopeService` still work."

## 0. Prior art — reuse from `/opt/git/jena-MDO`

Three bundles there already implement most of this against `org.gecko.jgit`:

| Reference bundle | What it gives us | Reuse |
|---|---|---|
| `de.jena.mdo.github.webhook.model` | EMF model of the GitHub push `Payload` (Commit.added/modified/removed, repository.git_url, ref, head_commit) + `github.genmodel` | Lift as-is (rename to fennec namespace). **GitHub-specific** — GitLab needs its own model/mapping to a provider-neutral change event (D7). |
| `de.jena.mdo.github.webhook.rest` | `GithubWebhookResource`: EMF-JSON Jakarta-RS endpoint that parses the payload, derives a topic from `git_url`+`ref`, and `TypedEventBus.deliver`s it | Lift as the **GitHub adapter**; **swap emfjson → Fennec `codec.rest`** (§G2 detail); **add HMAC verification** (missing in ref, see §5). A parallel **GitLab adapter** (token-auth, GitLab payload) normalizes into the same neutral event (D7). |
| `de.jena.mdo.git.epackage.registry` | `GitBasedEMFRegistry` (factory component bound to a `GitService`, `TypedEventHandler<Payload>`), `GitURIHandler` (`git://{commitId}/{path}` → `GitService.readFile`, **no checkout**), `GitEMFHelper` | Lift the git-read mechanics; **re-home the "register objects" side onto the model.atlas storage/registry layer** (see §2 integration decision). |

Mechanics worth internalizing from the reference:
- **`org.gecko.jgit` `GitService` = one repo + one branch.** Configured via a
  `GitConfig` factory (`repo`, `branch`, `id`); consumers bind by target filter
  `(id=…)`. API used: `fetch()`, `getFiles()` → `TreeResult` (file list +
  `commitId`), `readFile(commitId, path)` → `InputStream`, `getRef()`,
  `getGitUrl()`. So **branch = stage** falls out naturally, and the local clone
  the `GitService` maintains **is** our "mirror" (resolves D4 by reuse).
- **File selection = "does an EMF factory exist for this extension?"** Others are
  ignored — this is exactly D2's "ignore unrecognized files."
- **Notification path is an in-JVM `TypedEventBus`**, keyed by a topic derived
  from repo URL + branch. Storage backends subscribe to their own topic. The
  reference `notify()` already coalesces **multi-file, multi-commit** add/modify/
  remove into one batch — matching §5's batch requirement.
- Reference **trusts the payload's commit arrays** for the change set (no
  SHA-diff) and has **no reconcile poll** and **no signature check**. We diverge
  on the last two; the payload-vs-diff choice is called out in §5.

The reference's `GitBasedEMFRegistry` registers EPackages/EObjects **directly as
OSGi services** (like model.atlas's own `EMFFileWatcher`), bypassing the
scope/registry/stage + `ReadableScopeService` machinery. Our job is to keep that
git-read core but surface the results through model.atlas's storage/registry
layer instead — see the integration decision in §2.

## 1. New model in one paragraph

A **git repository is configured in the storage service** (base repo URL); each
**stage is a branch** of it. **Scope and registry do *not* map to any folder
structure in the repo** — they are model.atlas overlays. A **registry selects
which objects from the repo it cares about, by type**: a `RegistryService<EPackage>`
surfaces the models (`.ecore`) found in the repo, a `RegistryService`<instance>
surfaces the instances — one repo can feed several registries. The repo is
**self-contained**: it carries both the models and their instances (e.g. a
`person.xmi` instance ships alongside the `person.ecore` that defines it, on top
of well-known base packages like Ecore), so the git backend must **register the
repo's own EPackages dynamically** so its instances can be parsed — a
responsibility the file/Apicurio backends never had. model.atlas keeps a local
clone (via `GitService`, fetch-only, never checked out) plus a **derived,
file-based metadata index**; a **push webhook** (routed over the TypedEvent bus)
tells it what changed. Git-backed stages are exposed **only as
`ReadableScopeService`** — writes are rejected.

## 2. Decisions

### D1. Where does `ObjectMetadata` come from? — DECIDED (a: derive from git facts)
model.atlas **derives** `ObjectMetadata` from git facts — no atlas-specific
metadata authored by contributors. `objectId` from the path (D2), `version` from
the commit SHA, author/timestamp from the commit, and any extra `properties`
from git and/or an EPackage `properties` EAnnotation (the reference already reads
a `"properties"` EAnnotation off EPackages into service properties). Optionally
enriched by an in-repo metadata file if one is present, but never required.

### D2. Path ↔ objectId ↔ content-type convention — DECIDED
The git backend does **not** name files (contributors do, on GitHub); it must
derive identity from the observed path. Rule:

1. **`objectId` = the file path relative to the repo/branch root, *including the
   extension*.** The content type is thus embedded in the id (boss's call). So
   `models/person.ecore` and `models/person.xmi` are two distinct objects — which
   also disambiguates a model from its like-named instance.
2. **Content type / format** is derived from the extension for *parsing* (via the
   EMF factory registry / media-type registry), but the extension **stays part of
   the objectId**.
3. **Nested directories are allowed**; the directory prefix is part of the
   objectId.
4. **Unrecognized files are ignored** — if no EMF factory is registered for the
   extension (a `README.md`, `.gitignore`, …), skip it. (Reference does exactly
   this via `getExtensionToFactoryMap().containsKey`.)

### D3. What does "configure the stage as read-only" mean? — DECIDED (internal, per-stage)
A **stage** carries a **read-only flag in its model.atlas configuration**. When
set, only read operations are supported for that stage; every write entry point
(`storeObject`/`deleteObject`/`transitionToStage`) throws an exception. Purely
internal to model.atlas — **no GitHub API calls, no branch-protection**.
Granularity is **per stage**, in the existing scope/registry/stage config
(no new config unit). For git, every configured stage is read-only.

> G4 detail: pick the exception type (`ReadOnlyStorageException` /
> `UnsupportedOperationException`) and make the REST/workflow layers surface it as
> a clean 4xx, not an opaque 500 (§5.8).

### D4. How is object *content* fetched? — DECIDED (a: local clone via `GitService`)
Reuse `org.gecko.jgit` `GitService`: it maintains a **local clone**, `fetch`es on
sync, and serves blobs at a commit via `readFile(commitId, path)` behind a
`git://` `URIHandler` — **no working-tree checkout**. Fast reads, offline-
tolerant, no GitHub API rate limits. The metadata index (D1) is a derived index
over this, so metadata-only queries skip git entirely.

### D5. How does model.atlas learn which repos/branches/objects to track? — DECIDED (config + registry type filter)
Fully config-driven through the existing chain, with the corrected mapping:
- The **git `StorageService` config carries the repo** (base URL); a `GitService`
  (`GitConfig` factory: `repo`, `branch`, `id`) is configured **per branch =
  stage**. `storage.type=git`, plus auth.
- A `RegistryService` binds to the git `StorageService` via the existing
  `RegistryServiceConfig.storageService_target` filter, exactly as file/Apicurio.
- **The registry's type parameter is the object filter**: it only surfaces repo
  objects of the kind it manages (EPackages vs instances). Scope/registry are
  **not** encoded in git paths.
Because only configured repos/branches exist as stages, model.atlas inherently
only tracks — and only accepts webhooks/polls for — those repo/branch pairs.

**D5 refinement (DECIDED 2026-07-17) — where derived `ObjectMetadata` gets scope/registry.**
Git carries neither, but startup replay needs exact `scope`+`registry`+`stage` and routes
by `objectType` (verified: shared registry cache is keyed by `objectId` only, but
`RegistryServiceImpl.activate`→`listInStage`→`findByScopeRegistryAndStage(scope,
registry_name, stage)` filters on all three, and `dispatch` gates on
`objectType`=EClass-URI; a metadata missing any is *silently* never replayed).
**Chosen: Option 1 — config on the git `StorageService`.** The git storage config declares
**one `scope`** plus a map **`eClassUri → registryName`**; the `GitStorageHelper` stamps
`scope`(config), `registry`(map lookup on the object's EClass URI), `stage`(=branch),
`objectId`(=repo path incl. extension), `objectType`(=`EcoreUtil.getURI(obj.eClass())`),
`version`(=commit SHA). Objects whose EClass URI is absent from the map are ignored
(aligns with D2 "ignore unrecognized"). Config entries parsed by splitting on the **last**
`:` (registry names have none; the URI itself contains colons). `objectType` requires
loading each resource (EPackage for `.ecore`; `contents.get(0).eClass()` for instances) —
the accepted G6 bootstrap cost.

**Catch-all refinement (DECIDED 2026-07-17): registry routing = exact eClass URI, then
the `EObject` URI as an *explicitly-configured* catch-all, else skip.** Rationale: EPackages
all share the fixed Ecore `EPackage` eClass URI, so schemas auto-match with no per-model
config; but instances have per-type URIs that can't be enumerated a priori, so an
`http://www.eclipse.org/emf/2002/Ecore#//EObject` entry (which the configurer declares —
**not** a hardcoded code default) catches every instance type. A specific instance type can
still be pinned to its own registry (its exact URI wins by specificity). If no `EObject`
entry is configured, unmapped objects are ignored. **Minimal genuine-object guard**
(documented): non-EMF files never reach parsing (extension filter, D2); after a successful
parse the only extra check is that the root's `eClass()` is resolved (not a proxy) — a
registered-extension file that parsed into an unresolved root is ignored, not routed.
(Stricter `AnyType` rejection deferred.) (Option 2 registry/scope-driven priming and Option 3
in-repo manifest rejected: 2 = more G4 plumbing + diverges from FileStorageHelper priming;
3 = repo must not encode overlays.)

### D6. Delivery guarantees — DECIDED (webhook + cron reconcile poll)
Webhooks are best-effort (missed on downtime/network/retry-exhaustion; the
reference has **no** backstop). **Decision: run both** — inbound webhooks for
low-latency updates **and** a **cron-based reconcile poll** (`GitService.fetch` +
last-synced-SHA compare) as the guaranteed backstop, always on (not just a
fallback for no-inbound deployments). The poll also happens to be
**provider-agnostic** (plain git fetch/diff), so it is the uniform path that works
identically for GitHub and GitLab (D7) and covers force-push/branch-delete
robustly (§5.4/§5.11). Interval is configurable per git `StorageService` (TBD
default). Webhooks must stay idempotent since a poll may race a delivery for the
same change.

### D-INT. Integration approach — DECIDED (a: storage-helper backend)
The reference registers EPackages/EObjects **directly as OSGi services**, outside
the scope/registry/stage model. We want the read mechanics but inside model.atlas's
model. **Decision: (a).** We lift the git-read + webhook code from the reference
but surface everything through model.atlas's storage/registry layer; EPackage
registration is reused, not rebuilt. Option (b) is recorded below only as the
rejected alternative.
- **(a) Storage-helper backend [CHOSEN].** Implement `GitStorageHelper`
  (extends `AbstractStorageHelper`, read-only) + `EObjectGitStorageService`
  (extends `AbstractEObjectStorageService`, `storage.backend=git`), using
  `GitService`/`GitURIHandler` for reads. Plugs straight into
  `ReadableScopeService`/registry-cache; write methods throw. Best fit for the
  confirmed model. **EPackage registration comes for free** — model.atlas already
  registers a stored schema's EPackage via `EPackageStageActionService` →
  `DynamicEPackageRegistrationService` (dispatched by `RegistryServiceImpl`, not by
  any storage backend), and `SchemaRegistryChainConfigurator` routes it into the
  right per-stage `EPackageRegistry`/`ResourceSetFactory`. So we do **not** build a
  bespoke registrar or port `GitBasedEMFRegistry`'s EPackage side. The git-specific
  work is narrow: `GitSyncService` must **drive the ENTER/UPDATE/EXIT dispatch** at
  push-time (git has no write ops, so runtime sync bypasses the normal trigger;
  startup replay already covers cold start), and must **order register→parse**
  within a batch (§5.12).
- **(b) Port `GitBasedEMFRegistry` wholesale [REJECTED].** Faster, proven, but it
  bypasses scope/registry/stage and doesn't produce
  `ObjectMetadata`/`ReadableScopeService` — so it wouldn't satisfy the model the
  boss confirmed.

### D7. Multi-provider: GitHub *and* GitLab — DECIDED (support both)
Both GitHub and GitLab must be supported. Impact is **confined to the webhook
layer**; the git-read core is untouched:
- **Unaffected** — `org.gecko.jgit` `GitService`/`GitURIHandler`, the storage
  helper, metadata derivation, and the **reconcile poll (D6)** are all plain-git
  and provider-neutral. The poll works identically for either host, which is why
  it's the guaranteed common path.
- **Affected — webhook ingest** differs per provider on two axes:
  1. **Payload shape.** The reference `Payload` EMF model is GitHub-specific.
     GitLab's push event has a different structure (`object_kind=push`,
     `project.git_http_url`, `ref`, `checkout_sha`, per-commit
     `added/modified/removed`). Need either a second EMF model or a small mapping
     to a **provider-neutral internal change event** that the topic/derivation and
     `GitSyncService` consume.
  2. **Signature/auth.** GitHub signs with an HMAC-SHA256 `X-Hub-Signature-256`
     header (shared secret). GitLab instead sends a **plain shared-secret token**
     in `X-Gitlab-Token` (no HMAC). So verification is per-provider, not one HMAC
     check.
- **Approach:** a provider-neutral webhook abstraction with two adapters
  (parse + verify per provider) that normalize into one internal event; everything
  downstream (topic routing, sync, dispatch) stays provider-agnostic. Config
  selects the provider (or auto-detects by header) per git `StorageService`.
  Because the poll is provider-neutral and mandatory (D6), GitLab support can even
  ship poll-first with the webhook adapter added incrementally.

### D9. Cache objectId-collision across branches — DECIDED (A: qualify objectId) & DONE
**Decision (boss, 2026-07-17): Option A — the "easy workaround".** The git objectId is
qualified as **`scope + "/" + stage + "/" + repoPath`** (e.g. `jena/draft/models/person.xmi`),
making it unique in the shared objectId-keyed cache across branches *and* scopes. `scope` and
`stage` are also request parameters, so reads strip the prefix back to the repo path
(`GitStorageHelper.repoPathOf`, lenient — tolerates a bare repo path too); `loadEObject` then
leases the per-`(scope,stage)` ResourceSet (correct dynamic EPackages) and reads
`git://{commit}/{repoPath}`. Implemented in `GitStorageHelper` (`qualifiedId`/`repoPathOf`,
`deriveOne` stamps qualified id, `derived` keyed by objectId, `findObjectPath` strips, added
`loadMetadata` [serves derived copy] + `objectExists` overrides). Build green, 18 unit tests.
Option B (composite shared-cache key) rejected as too broad for now. §9 note: objectIds contain
`/`, so the REST layer's slash-in-id handling (already needed for nested file paths) applies.

### D9-history. (original open write-up, retained)
The shared registry cache (`LuceneEObjectRegistryService`) is keyed by **`objectId` alone**
— in-memory `metadataCache.put(objectId, md)` and the Lucene index deletes/re-adds by
`Term("objectId", objectId)`, so there is exactly **one document per objectId**;
`findByScopeRegistryAndStage` only *filters* by scope+stage+registry. That is fine for
file/apicurio because their workflow model keeps an object in **one stage at a time**
(objectId globally unique). **Git violates it:** branch = stage, so the same repo path
(`models/person.ecore`) exists on multiple branches at once, and with `objectId` = repo path
(D2) the two collide → the last-primed stage wins → `listInStage` for the other stage returns
nothing. (Not caught by current tests — single branch.) **Options:** **(A)** stage-qualify the
git objectId (`<stage>/<path>`) — contained to the git backend, no shared-cache change, but
deviates from D2, leaks the stage into the id, and needs the §9 client objectId audit; **(B)**
make the shared cache key composite (`scope+registry+stage+objectId`) — the correct fix (keys by
the identity it is queried by, doesn't break file/apicurio), but a change to a core service used
by every backend → boss buy-in. Lean: **B**. **G7 reconcile is paused until this is decided**,
since the reconcile evicts/re-adds cache entries per stage and needs stage-correct keying.

### D8. Reload / referential-integrity semantics — OPEN (spike done; contract still to decide)
When a push changes/removes a `.ecore`, its EPackage is reloaded. What happens to
(a) other `.ecore`s that reference it and (b) stored instances of its EClasses?
The desired **contract** is still a decision; a characterization spike now pins
down what the *current* mechanism actually does, so we decide against facts.

**Spike (green): `ReloadReferentialIntegritySpikeTest`** in
`org.eclipse.fennec.model.atlas.workflow/test/.../reload/`. Findings, combined with
a code read of `AbstractStorageHelper.loadEObject` (fresh parse per read + resource
`cleanup()`, **no caching**):
- **The catalog hypothesis holds.** A fresh read re-parses against the package
  registry *as of read time*, so after B is unregistered+re-registered a new read of
  an instance resolves `eClass().getEPackage()` to the **new** B; the same holds for
  a cross-ecore A→B reference. model.atlas keeps no resolved graph, so there is **no
  in-memory staleness** on the read path.
- **Retained objects are frozen.** An already-read `EObject`/`EPackage` holds a hard
  Java reference to the *old* package and never auto-refreshes — so nothing may cache
  a resolved read across a reload (the read path already doesn't).
- **The sharp edge is the reload *window*, not staleness.** Reading an instance
  whose model is **not currently registered throws `PackageNotFoundException`** (EMF
  does not degrade to a proxy/empty result). And `DynamicEPackageRegistrationService`
  **no-ops a re-register of an already-registered nsURI**, so a reload is necessarily
  *unregister-then-register* — `EPackageStageActionService` even documents the "brief
  window where the EPackage is not registered." A read landing in that window (or of a
  genuinely removed model) hard-fails.

**So the decision narrows to how to handle the window + removals**, e.g.:
- **(1) Atomic per-commit snapshot [leaning].** Build the new consistent package set
  for a commit and swap it in; avoid an unregistered gap (register-new-before-drop-old
  where nsURI is unchanged, or version the swap). Cleanest fit with `git://commitId`.
- **(2) Register-before-parse ordering + retry/degrade** on the read path for the
  window (surface a clean 409/503-style "resyncing", not a 500).
- **(3) Removal policy:** define what a read of an instance whose model was removed
  returns (404 vs. quarantine), and whether such instances are evicted from the index.
This is a boss/team call like D6; the spike is the input, not the answer.

## 3. Components

| Component | Bundle | Role |
|---|---|---|
| Webhook payload model(s) | `management.git` (or `.webhook.model`) | GitHub `Payload` EMF model **lifted from `de.jena.mdo.github.webhook.model`**, plus a GitLab payload model/mapping; both normalize to a **provider-neutral change event** (D7). |
| `GitWebhookResource` (+ per-provider adapters) | `management.git` (or `.webhook.rest`) | Fennec `codec.rest` RS endpoint (**not** emfjson); **GitHub adapter** HMAC-verifies (`X-Hub-Signature-256`) — **lifted from `de.jena.mdo.github.webhook.rest`**, codec-swapped; **GitLab adapter** token-verifies (`X-Gitlab-Token`). Each parses its payload → neutral event → `TypedEventBus.deliver` on repo+branch topic (D7). |
| `ReconcilePoll` (cron) | `management.git` | Provider-agnostic backstop (D6): periodic `GitService.fetch` + last-synced-SHA compare → drives `GitSyncService`. Always on; the guaranteed path when webhooks are missed or inbound HTTP is blocked. |
| `GitStorageHelper` | `management.git` | Read-only `AbstractStorageHelper`: `git://` reads via `GitService`/`GitURIHandler` (no checkout), `loadAllStoredMetadata` derived per D1. Write methods throw. |
| `EObjectGitStorageService` | `management.git` | `AbstractEObjectStorageService`, `storage.backend=git`. Binds a `GitService` (repo+branch=stage); `TypedEventHandler` for the provider-neutral change event on its topic (fed by either webhook adapter or the reconcile poll). `storeObject`/`deleteObject`/`transitionToStage` → exception. |
| Sync-driven dispatch (in `GitSyncService`) | `management.git` | The repo's own `.ecore` EPackages are registered by the **existing** `EPackageStageActionService`/`DynamicEPackageRegistrationService` (no new registrar). Git's only job: `GitSyncService` drives the ENTER/UPDATE/EXIT dispatch on push-time changes and orders register→parse within a batch (§5.12). |
| `MetadataIndex` | `management.git` | File-based derived index of `ObjectMetadata`; rebuilt/updated on sync; serves metadata-only queries without touching git. |
| `GitSyncService` | `management.git` | Reconcile engine: on webhook (or poll) fetch → resolve change set → update mirror + `MetadataIndex` → push into the registry cache. |

## 4. Phases

- **G0 — Decisions.** D1–D5 decided; D-INT = (a) storage-helper; D6 = webhook +
  always-on cron reconcile poll; D7 = support GitHub *and* GitLab (provider-neutral
  webhook layer). **D8 (reload/referential-integrity contract) still open** — spike
  done (see G0.5), contract decision pending (boss/team call, like D6). Also confirm
  the derived-metadata mapping (D1) against a real self-contained repo.
- **G0.5 — Reload referential-integrity spike (D8 input) — DONE.**
  `ReloadReferentialIntegritySpikeTest` (pure-EMF, in `...workflow/test`) +
  code read of `AbstractStorageHelper.loadEObject`. Confirmed: read path re-parses
  per read against the current registry (catalog hypothesis holds, no in-memory
  staleness); retained objects freeze; and the real risk is the *reload window* —
  reads of an unregistered/removed model throw `PackageNotFoundException`, and
  re-register is necessarily unregister-then-register. Feeds the D8 contract choice
  and the G8 test matrix.
- **G1 — Bundle setup — DONE (build green).** `Example.java`/`ExampleTest.java`
  deleted; `management.git` `-buildpath` wired to the read core (mirrors
  `management.file`: `management` + osgi framework/promise/function/component/cm).
  RS whiteboard / typed-event / `codec.rest` are added in G2, `org.gecko.jgit` in
  G3 (comments in `bnd.bnd` flag both).
  **Correction to the original note:** JGit is **NOT** yet in model.atlas's build
  config (the note was copied from the reference). It must be added before G3 —
  the reference (`jena-MDO/cnf/ext/project.maven`) uses
  `org.geckoprojects.jgit:org.gecko.jgit:1.0.0-SNAPSHOT` (exports
  `org.gecko.jgit.api`) + `org.eclipse.jgit:org.eclipse.jgit:7.1.0.202411261347-r`.
  **[BLOCKER for G3 — needs these coordinates added to `cnf`.]**
- **G2 — Lift webhook model + REST (provider-neutral, GitHub + GitLab — D7) — DONE (build green).**
  Webhook **models** done (meta `webhook.model` + concrete `github.webhook.model`/
  `gitlab.webhook.model`, generated to Java; `isDeleted` body added in the meta).
  **REST adapters** done in `...management.git.webhook.rest`:
  `GithubWebhookResource` (`@Path("/github")`, `@RootElement(rootType=GithubWebhookPackage.eNS_URI+"#//GithubPayload")`)
  and `GitlabWebhookResource` (`@Path("/gitlab")`, `…#//GitlabPayload`), both parse via
  the Fennec codec and `eventBus.deliver(WebhookTopics.topicFor(payload), payload)` the
  **neutral `WebhookPayload`**. Security + push-gate live in a **name-bound
  `ContainerRequestFilter`** (`WebhookSignatureFilter` + `@VerifyWebhookSignature`
  NameBinding): it buffers the raw body (needed for HMAC), verifies GitHub
  `X-Hub-Signature-256` (HMAC-SHA256, constant-time) / GitLab `X-Gitlab-Token`, resets
  the stream so `@RootElement` can still parse, and `abortWith(200)` on non-push events
  (e.g. GitHub `ping`). Secrets via ConfigAdmin pid
  `org.eclipse.fennec.model.atlas.management.git.webhook`
  (`githubSecret`/`gitlabToken`/`requireSignature`, **fail-closed by default**).
  **Topic derivation** (`WebhookTopics`, exported) is the **shared publish/subscribe
  contract** — the G4 storage service must compute its subscribe topic via the same
  `topicFor(repoFullName, branch)` (branch from `refs/heads/…`); consider relocating it
  to a non-REST shared bundle when G4 lands so `management.git` need not depend on a
  JAX-RS bundle.
  **PLAN CORRECTION:** the Fennec `codec.rest` `@RootElement` has **no `rootClassUri`** —
  it exposes `rootType` (EClass URI) + `rootSchema` (EPackage nsURI). Adapters use
  `rootType`. (Original note below is stale on that attribute name.)
  **Unit tests DONE & green (23 tests, 0 fail):** `WebhookTopicsTest` (8),
  `WebhookSignatureFilterTest` (11 — HMAC valid/invalid/missing, fail-closed vs
  `requireSignature=false`, non-push→200, no-event→400, stream buffered+reset),
  `GithubWebhookResourceTest`/`GitlabWebhookResourceTest` (2 each — neutral payload
  delivered on derived topic, identical topic shape across providers, missing repo→400).
  Plain JUnit5 + Mockito, in-bundle `test/`. **Build note:** resource/filter build
  `jakarta.ws.rs Response` objects → need a JAX-RS `RuntimeDelegate` at test time, which
  `jakarta.ws.rs-api` lacks; added `org.glassfish.jersey.core:jersey-common:3.1.3` as a
  **test-only** dep scoped to this project in root `build.gradle` (not a bundle
  compile/runtime dep). Whiteboard/codec-MBR/ConfigAdmin/real-TypedEventBus wiring is
  **deferred to an OSGi IT (G8)** or whenever a live event consumer (G4) exists.
  <details><summary>original G2 spec (retained)</summary>
  Port the two webhook bundles into the fennec namespace as the **GitHub adapter**;
  add HMAC (`X-Hub-Signature-256`) verification. Define a **provider-neutral change
  event** and normalize the GitHub payload into it, then add a **GitLab adapter**
  (GitLab push payload + `X-Gitlab-Token` verification) that normalizes into the
  same event. Downstream topic routing/sync stays provider-agnostic. **Swap
  emfjson for the Fennec `codec.rest`** on the resource (model.atlas has already
  migrated off emfjson — `RequireRuntime` has the gecko `@RequireEMFJson`/
  `@RequireEMFMessageBodyReaderWriter` commented out). Concretely: drop the gecko
  class annotations `@RequireEMFJson` + `@RequireEMFMessageBodyReaderWriter` in
  favour of `org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter`;
  repoint the `@RootElement(rootClassUri = …#//Payload)` param import from
  `org.gecko.emf.rest.annotations.json.RootElement` to
  `org.eclipse.fennec.codec.rest.annotations.json.RootElement` (same `rootClassUri`
  attribute, so the annotation body is unchanged); `@Consumes(APPLICATION_JSON)`
  and the `Payload`/`Response` method shape stay as-is. bnd: add
  `org.eclipse.fennec.codec.rest;version=latest` to `-buildpath` (the `fennecCodec`
  library is already enabled in `cnf/build.bnd`) and ensure the runtime resolves
  `org.eclipse.fennec.codec.rest` (via the generated `fennec.codec.rest;messagebody`
  capability requirement or explicit `-runrequires`, as `runtime_base.bndrun` does).
  Codec source: `/opt/git/fennec-codec` (`org.eclipse.fennec.codec.rest`,
  provider `EObjectMessageBodyHandler`).
  </details>
- **G3 — `GitStorageHelper` (read-only) — DONE (build green, 10 unit tests).**
  jgit on buildpath (`org.gecko.jgit` + `org.eclipse.jgit`). Lifted `GitEMFHelper`
  (`git://{commitId}/{path}` parse + `createGitURI`) and `GitURIHandler` (adapted to
  route by a live `commitId→GitService` map, since one helper may serve several
  branches). `GitStorageHelper extends AbstractStorageHelper`: ctor takes
  `(resourceSet, Collection<GitService> [one per branch=stage], scope,
  Map<eClassUri,registryName>, registryService)`; registers the URI handler, `refresh()`
  (fetch + `getFiles()` per branch, cache `TreeResult`), then primes the registry cache.
  `createStorageURI`→`git://{commitId}/{path}`; `storageExists`/`findObjectPath` check the
  branch tree (objectId = repo path incl. extension, no probing); `listObjectIds` filters
  the derived cache by stage+registry; `loadAllStoredMetadata` parses each recognized file,
  derives objectType=EClass URI → registry (map), stamps scope/registry/stage/objectId/
  objectType/version(=commit); write methods (`persistResource`/`deleteObject`) throw
  `UnsupportedOperationException` (G4 §5.8 to refine to a typed 4xx). Tests use a Mockito
  `GitService` + real `ResourceSet` (also exercises `GitURIHandler` via `loadEObject`).
  **Deferred (correctly): (a)** supplying the `GitService`s + scope + type→registry map from
  config = **G4** (`EObjectGitStorageService`); **(b)** cold-start *instance* coverage —
  `objectType` needs a parse and an instance only parses once its EPackage is registered, so
  at construction instances are skipped-with-log; the register-then-reparse ordering is
  **G5**; **(c)** per-file author/time (`uploadUser`/`uploadTime` via `getLog`) — optional,
  not on the replay path, future refinement (`version`=commit is set).
- **G4 — `EObjectGitStorageService` — DONE (build green, 16 unit tests in-bundle).**
  `@Component(storage.backend=git)` extending `AbstractEObjectStorageService`, mirroring
  `EObjectFileStorageService`; `getBackendType()=StorageBackendType.GIT` (enum literal
  already existed). OCD `Config`: `repo`, `scope`, `type_registry_map` (`String[]` of
  `eClassUri:registryName`, parsed by `parseTypeToRegistry` splitting on the LAST `:`),
  `gitservice_target`, `storage_type`. Binds `GitService` per branch=stage via a
  `gitservice` reference (`MULTIPLE`/`STATIC`/`GREEDY`, target from `gitservice.target`
  config) so the component re-activates and rebuilds the helper when the branch set
  changes. `createStorageHelper()` → `new GitStorageHelper(resourceSet[(emf.name=management)],
  gitServices, scope, typeToRegistry, registry)`. Verified generated DS descriptor + the
  `mac.management … storage.backend=git` capability. Unit test covers `parseTypeToRegistry`
  (last-colon split, trim, skip malformed/blank, null); the DS wiring itself is an OSGi IT
  (G8). **Deferred to G7 (with the reconcile poll):** the `TypedEventHandler<WebhookPayload>`
  subscription + resync — that is where topic-consistency (`WebhookTopics.topicFor` on the
  subscribe side, incl. deriving repoFullName from the clone URL to match the webhook side)
  and the poll live together; **`WebhookTopics` relocation to a non-REST shared bundle is
  therefore deferred to G7** too (not needed until management.git actually consumes it,
  avoiding a premature edit to the generated meta-model bundle).
- **G5 — Cold-start instance derivation via an EPackage ServiceTracker (NO registry
  coupling).** **Architectural constraint (verified 2026-07-17):** the git storage
  service must NOT reference `RegistryServiceImpl` — `RegistryServiceImpl` already
  `@Reference`s `EObjectStorageService` (`storageService`, RegistryServiceImpl.java:88),
  so a storage→registry edge would be a DS activation **cycle**. It isn't needed:
  - **Schemas** are registered by the **existing** `RegistryServiceImpl.activate` replay
    (reads the cache the helper primed) — no storage→registry ref.
  - **Instances need no dispatch at all** — the only `StageActionService.supportsObjectType`
    is `EPackageStageActionService` = `EPACKAGE_TYPE` only. Instances just need to be in the
    **shared cache** (a leaf: `LuceneEObjectRegistryService` has no back-ref to storage), which
    the git backend already references. So `storage→cache` is safe/acyclic.
  The only real problem is **timing** (instances aren't parseable at construction because their
  EPackage isn't registered yet).
  **DONE (build green, 17 unit tests):** implemented via **per-(scope,stage) ResourceSet leasing +
  an EPackage ServiceTracker for timing** (Ilenia's ResourceSet insight — do NOT hand-insert
  EPackages). Details:
  - `GitStorageHelper` now takes a `ResourceSetCollector` (leaf singleton; verified no cycle) and,
    for every parse (derivation *and* `loadEObject`), leases the per-`(scope,stage)` ResourceSet
    (`getResourceSetObjects(scope, branch)`) — which already carries that stage's dynamic
    EPackages — adds the `git://` handler to that lease, parses, `ungetService`s. Falls back to
    the injected management ResourceSet when no per-stage RS is available yet (schemas need only
    Ecore, so they derive at cold start). Per-stage leasing is also required for correctness
    (same nsURI may differ per branch → no cross-stage package bleed).
  - `deriveAll()` is incremental: per branch it parses only files not yet in the `derived` map,
    so repeated passes are cheap; `rederive()` re-runs it. `derived` keyed by (stage + path).
  - `EObjectGitStorageService` opens a `ServiceTracker<EPackage>` (all EPackages) whose events
    schedule a **coalesced** `rederive()` on a single-thread daemon executor (`AtomicBoolean`
    guard → a burst of registrations collapses to ~one pass). A not-yet-propagated package just
    fails that pass and is retried on the next tracker event (absorbs the async-propagation race).
  - **Zero `RegistryServiceImpl` coupling** — instances only `updateCache` into the shared cache
    (leaf). (Earlier framing of "drive ENTER/UPDATE/EXIT via a RegistryService reference" was
    wrong — it would have created the cycle above.)
  - `management.git` bnd now also buildpaths `org.eclipse.fennec.model.atlas.workflow`
    (`ResourceSetCollector`, exported) + `org.osgi.util.tracker`. No cycle (workflow doesn't
    depend on management.git). **Deferred:** EPackage *removal*/EXIT handling (drift) → G7.
- **G6 — Startup cache priming ordering — DONE (verified; satisfied by G3/G5, no code change).**
  Traced the chain: `ScopeServiceImpl.bindRegistryService`→`registryService.activate(scope)`
  synchronously (ScopeServiceImpl.java:72) → `RegistryServiceImpl.activate` → per trigger-stage
  `listInStage` → `findByScopeRegistryAndStage(scope, registry_name, stage)` reads the **shared
  cache** (RegistryServiceImpl.java:314-320). The git helper primes **schemas** into that same
  shared cache in its **constructor** (`deriveAll()`→`updateCache`), which runs inside
  `EObjectGitStorageService.activate()` *before the storage service is registered* — mirroring
  `FileStorageHelper.updateRegistryCache`. DS ordering then guarantees priming precedes the
  RegistryService bind/`activate(scope)`, so schemas are in the cache before `listInStage` reads
  them → ENTER dispatch registers the EPackages. **Schemas parse at ctor on the management
  ResourceSet** (only need Ecore), so priming does not depend on per-stage RS availability.
  Instances are primed later (tracker `rederive`, after `activate(scope)`) — fine, they need no
  dispatch (G5), only cache presence for on-demand `findByScopeRegistryAndStage`.
  **Runtime-config prerequisites (G9, identical to the file backend):** the git storage config
  must (a) make the service discoverable by `RegistryServiceImpl`/`EPackageStageActionService`,
  which filter storage on `(scope=no-inject)`; and (b) point the storage's `registry` reference
  at the same shared cache the RegistryService reads (`storage.json` uses
  `registry.target=(registry=main)`). The DS-lifecycle ordering itself is only exercisable in an
  OSGi IT → covered at G8.
- **G7 — `GitSyncService` + cron reconcile poll (D6, DECIDED).** Last-SHA
  tracking, fetch, batch reconcile, force-push/branch-delete handling. The
  **cron-based reconcile poll is mandatory and always on** (not just a fallback):
  configurable interval per git `StorageService`, provider-agnostic (works for
  GitHub and GitLab alike), and idempotent with webhook deliveries so a poll/webhook
  race for the same change is safe.
- **G8 — Tests.** `management.git.tests` over a local temp bare repo: reads per
  stage/branch, metadata derivation, model+instance self-contained resolution,
  batch multi-file sync, missed-webhook-caught-by-poll, poll/webhook idempotency
  (same change via both), force-push/branch-delete, write-rejection, concurrent
  reads across branches. **Reload/referential-integrity (D8), lifting the G0.5
  matrix onto the real git read path:** instance `eClass()` + cross-ecore A→B
  resolution across a model reload; **reads during the reload window** (model
  momentarily unregistered → today throws `PackageNotFoundException`) per the D8
  contract chosen; and reads of a **removed** model. Opt-in ITs against real GitHub
  **and** GitLab (payload + signature/token differences, D7).
- **G9 — Runtime wiring (deferrable).** `runtime.config.docker.git` +
  `modelatlas.runtime_docker_git.bndrun` + `docker/modelatlas_git`; expose the
  webhook port; document credential/secret injection **per provider** (GitHub PAT/
  App + webhook HMAC secret; GitLab token + `X-Gitlab-Token` secret) and the cron
  poll interval. (Reference config lives in `de.jena.mdo.runtime.config/configs/git.json`.)

## 5. Concerns / risks (for the discussion)

1. **Integration approach (D-INT) — DECIDED (a, storage-helper).** We get
   `ReadableScopeService`/metadata and EPackage registration via the existing
   model.atlas machinery; the residual git-specific work is `GitSyncService`
   driving the dispatch + register-before-parse ordering (§5.12), not a new
   registry. Kept here only as a closed decision.
2. **Webhooks are best-effort (D6) — RESOLVED.** Decision: webhook **plus** an
   always-on cron reconcile poll (last-SHA compare). The poll is the guaranteed,
   provider-agnostic backstop and also covers deployments that block inbound HTTP.
   Kept here as a closed decision.
3. **Webhook security (per provider, D7)** — the reference does **no** signature
   check. GitHub: verify the HMAC-SHA256 `X-Hub-Signature-256` (shared secret).
   GitLab: verify the plain `X-Gitlab-Token` shared-secret header (no HMAC). Both:
   restrict to push events and be **idempotent** (retries and the reconcile poll
   can deliver the same change more than once).
4. **Force-push / history rewrite / branch delete** — must reflect removals and
   non-fast-forward updates in the cache. The reference's payload-array approach
   is fragile here (see §5.11).
5. **Content-vs-metadata skew** — metadata cached, content read from the clone;
   between syncs the clone can lag GitHub. The `get`/content path must degrade
   gracefully when metadata references a blob not yet fetched or rewritten away.
6. **Multi-instance / clustering** — each instance keeps its own clone and must
   receive events. A webhook reaches one instance only; the always-on cron poll
   (D6) already gives every instance its own convergence path, so clustering leans
   on the poll and treats webhooks as a per-instance latency optimization.
7. **Auth & secrets (per provider, D7)** — private repos need a fetch credential
   (GitHub PAT/App/deploy key; GitLab PAT/deploy token) **and** a webhook secret
   (GitHub HMAC secret; GitLab `X-Gitlab-Token`). Keep all tokens out of logs and
   out of metadata `properties`.
8. **Read-only semantics leak (D3)** — REST/workflow layers assume writable
   scopes in places. Confirm every write path fails cleanly and visibly, and the
   client/UI presents git stages as read-only rather than erroring opaquely.
9. **objectId = path-with-extension (D2)** — downstream code that assumed objectId
   had no extension (file backend strips it) must tolerate ids like
   `models/person.xmi`. Audit `RegistryService`/client id handling.
10. **Initial bootstrap cost** — first startup clones every configured repo and
    builds the index; large repos → slow, disk-heavy cold start. Consider lazy
    per-repo clone on first access.
11. **A notification affects many objects, not one** — a single push bundles
    multiple commits × multiple files (github.dev, `git push`, and PR merges —
    which is how transitions land). Only the classic single-file pencil editor is
    one-file. So each notification drives a **batch** metadata update (added/
    modified/removed). The reference reads the change set from the payload's
    commit arrays; that's simplest but GitHub caps it (≤20 commits, truncation on
    large pushes, ~25 MB) and it's meaningless on force-push. **Robust
    alternative:** treat the payload as a trigger and compute the change set from
    the clone diff (last-synced SHA → new tip) — one path for single/multi-file,
    multi-commit, truncated, and force-push. Decide payload-array (match ref) vs.
    SHA-diff (robust), and the consistency granularity the API promises between
    notification and batch completion (all-or-nothing vs. eventually-consistent).
12. **Self-contained repos → dynamic EPackage registration** — the repo ships its
    own `.ecore` models alongside their instances. **Registration itself is
    inherited, not new work**: the existing `EPackageStageActionService` →
    `DynamicEPackageRegistrationService` path (dispatched by `RegistryServiceImpl`,
    consumed by `SchemaRegistryChainConfigurator`'s per-stage registries) registers
    a stored schema's EPackage regardless of storage backend — file/Apicurio never
    had to write this either. What **is** git-specific are two residual concerns:
    (a) **sync-driven dispatch** — git rejects writes and its content changes at
    runtime via webhook/sync, which bypasses the normal upload/transition triggers;
    startup replay covers cold start, but `GitSyncService` must itself drive
    ENTER/UPDATE/EXIT for schemas a push adds/changes/removes, else a schema pushed
    after startup stays unregistered until restart; (b) **batch ordering /
    async-propagation** — registration publishes OSGi services and the per-stage
    `ResourceSet` reconfigures via ConfigAdmin/DS asynchronously, so within one push
    models must be registered **and propagated** before instances are parsed. The
    lifecycle question (what happens to instances when their model changes or is
    removed in the same push) is now tracked as **D8**, informed by the G0.5 spike:
    reads are stateless/re-resolving, but a read during the unregister→register
    window throws `PackageNotFoundException`, so ordering here is not just an
    optimization — it's correctness.

    > **Verified (code-traced):** the read-only path *does* run startup replay.
    > `ScopeServiceImpl` is a single component implementing
    > `ScopeService`/`WritableScopeService`/`ReadableScopeService` (no separate
    > read-only impl; `StorageRegistryServiceImpl` is commented-out dead code), and
    > its `bindRegistryService` calls `registryService.activate(scope)`
    > **unconditionally** for every bound registry
    > (`ScopeServiceImpl.java:68-72`). `RegistryServiceImpl.activate` then does
    > `listInStage → dispatch(ENTER)` for each trigger stage
    > (`RegistryServiceImpl.java:116-125`); read-only is only a per-stage flag on
    > write paths (`isWritableStage`, `:473`), never gating replay. **So cold-start
    > registration is free given:** (1) the git registry is a `RegistryServiceImpl`
    > config instance [D-INT(a) — done]; (2) the git stage(s) appear in the
    > `EPackageStageActionService` `trigger_stages`; and (3) **ordering** — since
    > `activate`→`listInStage` reads the registry *cache* synchronously at bind
    > time (`:314-319`), the git storage helper must prime that cache from the clone
    > **before** the `RegistryService` binds, or replay sees an empty stage (G6).

## 6. What carries over from v1 vs. what's dropped
- **Carried:** JGit; per-repo handle; **read** without checkout (now via
  `GitService`/`git://` URIHandler); branch-per-stage.
- **Dropped:** commit-without-checkout **writes**, orphan-branch creation,
  per-branch write locks, "transition = a commit we make," and the incorrect
  **(scope,registry)=repo path** mapping (scope/registry are overlays; registry is
  a type filter — §1).
- **New:** reuse of the jena-MDO reference (webhook model/REST, `GitService`,
  `GitURIHandler`); TypedEvent-routed notifications; **sync-driven dispatch** so the
  self-contained repo's EPackages get registered through the *existing*
  `EPackageStageActionService` path (registration itself is inherited, not new) plus
  register-before-parse ordering; derived file-based metadata index; reconcile poll +
  HMAC (both absent in the reference); read-only enforcement.
