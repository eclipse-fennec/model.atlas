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
- **G2 — Lift webhook model + REST (provider-neutral, GitHub + GitLab — D7).**
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
- **G3 — `GitStorageHelper` (read-only).** `GitService`/`GitURIHandler` reads,
  no checkout; extension-filtered file selection (D2); `loadAllStoredMetadata`
  derived per D1. Write methods throw.
- **G4 — `EObjectGitStorageService`.** `@Component(storage.backend=git)` mirroring
  `EObjectFileStorageService`; binds a `GitService` per (repo, branch=stage);
  subscribes as `TypedEventHandler` on its topic; read-only wiring so git stages
  surface via `ReadableScopeService` only.
- **G5 — Sync-driven EPackage dispatch + register-before-parse ordering.**
  EPackage registration is inherited from `EPackageStageActionService`/
  `DynamicEPackageRegistrationService` — no new registrar. Wire `GitSyncService`
  to drive ENTER/UPDATE/EXIT dispatch for schemas a push changes (startup replay
  covers cold start), and ensure the repo's `.ecore` models are registered **and
  propagated** into the per-stage `ResourceSet` before `.xmi` instances are parsed.
- **G6 — `MetadataIndex` + startup cache.** Build-from-clone and incremental
  update; wire into the registry cache on startup (mirrors
  `FileStorageHelper.updateRegistryCache`, sourced from git). **Ordering
  constraint (verified):** the cache must be primed **before** the git
  `RegistryService` binds to its scope — `ScopeServiceImpl.bindRegistryService`
  calls `activate(scope)` synchronously on bind, and `activate`→`listInStage`
  reads the cache to drive startup replay/registration. Prime on the storage
  helper's own activation (as `FileStorageHelper` does) so it precedes the
  registry bind; otherwise cold-start replay sees an empty stage and registers
  nothing until the next `GitSyncService` dispatch.
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
