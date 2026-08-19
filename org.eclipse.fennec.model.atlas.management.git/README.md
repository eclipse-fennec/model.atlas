# Git Storage Backend (`management.git`)

A **read-only, remote-backed** storage backend for Fennec Model Atlas. Models and their
instances live in a **git repository** that people edit externally (GitHub/GitLab — pushes,
PRs, merges). Model Atlas never writes to git; it mirrors the repository, serves reads, and
stays in sync via **webhooks** and an always-on **reconcile poll**.

It plugs into the existing storage/registry machinery as an `EObjectStorageService`
(`storage.backend=git`), so scopes, registries, stages, `ReadableScopeService`, and dynamic
EPackage registration all work exactly as they do for the file/Apicurio backends — the git
specifics are confined to this bundle.

> For the design rationale, rejected alternatives, and decision history, see `PLAN.md`.
> This README describes **what was built and how to use it**.

---

## Core ideas

| Concept | How git maps to it |
|---|---|
| **Stage** | a **branch** of the repo. One `GitService` per branch. |
| **Scope** / **registry** | *not* in the repo — supplied by configuration (git carries neither). |
| **objectId** | `scope/stage/repoPath` (e.g. `jena/main/models/person.ecore`). The extension is part of the id. |
| **Content** | streamed straight from the local mirror at a commit via a `git://` URI handler — **no working-tree checkout**. |
| **Metadata** | **derived** from git facts, never authored: `stage`=branch, `version`=commit SHA, `objectType`=root EClass URI, `registry`=type-map lookup, `contentHash`=SHA-256 of the raw blob bytes (per file — stable across commits that don't touch the file, unlike `version`; feeds the content ETag and the scope aggregate manifest). |
| **Which files** | any file whose extension has a registered EMF factory (`.ecore`, `.xmi`, …); everything else (`README.md`, `.gitignore`) is ignored. |
| **Writes** | rejected — every write path throws `UnsupportedOperationException`. |

### Why `objectId` is stage-qualified

The shared registry cache is keyed by `objectId` alone. Git legitimately has the **same repo
path on several branches at once** (e.g. `person.ecore` on `main` and `release`), so a bare
repo path would collide across stages. Qualifying the id with `scope/stage/` keeps each
branch's copy distinct.

---

## Components (this bundle)

- **`EObjectGitStorageService`** — the `EObjectStorageService` (`storage.backend=git`). Binds
  one `GitService` per branch, builds the helper, subscribes to webhooks, runs the reconcile
  poll, and drives the EPackage re-derivation tracker.
- **`GitStorageHelper`** — the read engine (`AbstractStorageHelper`): derives metadata,
  resolves reads against the right per-stage `ResourceSet`, and reconciles a branch after a
  push. Write methods throw.
- **`GitURIHandler`** — EMF URI handler for `git://{commitId}/{path}`; reads blobs via
  `GitService.readFile` (no checkout). `createOutputStream` throws (read-only).
- **`GitEMFHelper`** — builds/parses `git://` URIs.
This bundle exports no public types of its own; the impl package is `Private-Package`. The
"model unavailable" signal is the shared `ModelUnavailableException` in
`org.eclipse.fennec.model.atlas.mgmt.storage` (thrown centrally by `AbstractStorageHelper` for
every backend — see *Removal*).

### Dependencies from Fennec EMF Util

The git plumbing this bundle builds on is **not in this repository** — it lives in
[Fennec EMF Util](https://github.com/eclipse-fennec/fennec-emf.util) and is consumed as a
released/snapshot Maven dependency:

| Bundle | Provides |
|---|---|
| `org.eclipse.fennec.jgit` | `GitService` + `GitServiceImpl`, the `GitConfig` factory (one repo + branch per instance) |
| `org.eclipse.fennec.git.webhook.model` | the neutral `WebhookPayload` and `WebhookTopics` |
| `org.eclipse.fennec.git.github.webhook.model` | `GithubPayload` (GitHub wire format) |
| `org.eclipse.fennec.git.gitlab.webhook.model` | `GitlabPayload` (GitLab wire format) |
| `org.eclipse.fennec.git.webhook.rest` | the `POST /github/webhook` / `POST /gitlab/webhook` ingest endpoints |

The webhook model and REST bundles used to live here under
`org.eclipse.fennec.model.atlas.management.git.*.webhook.*`; they were moved to Fennec EMF Util
so other projects can reuse them.

> **Breaking config change (2026-08-18):** the webhook ConfigAdmin pids moved with the bundles,
> from `org.eclipse.fennec.model.atlas.management.git.webhook.{github,gitlab}` to
> **`org.eclipse.fennec.git.webhook.{github,gitlab}`**. A configuration still using the old pid
> is simply never matched, and since the endpoints are gated on their pid the result is a silent
> `404` on `POST /github/webhook` rather than an error. Update any deployment configuration —
> the shipped `runtime.config.docker.git` already uses the new names.

---

## Reads

A read re-parses from the mirror every time (stateless catalog — no in-memory staleness):

- **Schemas** (`.ecore`) parse against Ecore alone, so the shared *management* `ResourceSet`
  suffices.
- **Instances** need their (dynamic) EPackage, which lives in the **per-(scope,stage)
  `ResourceSet`** produced by the registry chain. The helper leases that ResourceSet from
  `ResourceSetCollector`, adds the `git://` handler, parses, resolves all cross-references,
  and releases the lease — returning a **self-contained** object (cross-ecore references and
  instance `eClass()` are fully resolved before the leased ResourceSet is released).

Per-stage isolation is also a correctness requirement: the same `nsURI` can carry different
content on different branches, so an instance resolves against **its own branch's** packages.

---

## Staying in sync

External pushes reach the backend two ways, and they are **idempotent** with each other (a
tip-SHA compare no-ops when nothing changed):

1. **Webhooks** — the REST ingest verifies the provider signature (GitHub HMAC
   `X-Hub-Signature-256` / GitLab `X-Gitlab-Token`), normalizes GitHub/GitLab payloads to a
   neutral `WebhookPayload`, and delivers it on the OSGi TypedEvent bus. The storage service
   is a `TypedEventHandler` subscribed to its branches' topics; an inbound push triggers
   `reconcile(branch)`. Each provider's endpoint is **opt-in per configuration** — it only
   exists while its ConfigAdmin pid is present (see “4. Webhook endpoints” below), so a
   deployment can expose GitHub only, GitLab only, both, or none.
2. **Reconcile poll** — an always-on background poll (`poll.interval.seconds`, default 60;
   ≤0 disables) calls `reconcileAll()`, the guaranteed backstop for missed/undelivered
   webhooks and for deployments that block inbound HTTP.

**`reconcile(stage)`**: fetch the branch; if the tip is unchanged, no-op. Otherwise evict the
stage's derived metadata, re-derive the new tree, and — because a push may have added,
changed, or removed a schema — request a registry resync (below). Whole-branch re-derivation
covers add/modify/remove uniformly.

---

## Dynamic EPackage registration (schemas become live EMF)

The repo is **self-contained**: it ships `.ecore` models alongside their instances. Those
EPackages are registered as OSGi services by the **existing** workflow machinery
(`EPackageStageActionService` → `DynamicEPackageRegistrationService`, consumed by the
per-stage registry chain) — the same path the file/Apicurio backends use. Git adds two things:

- **Cold start** is free: the git helper primes schemas into the shared cache in its
  constructor (before the `RegistryService` binds), so the startup replay registers them.
- **Runtime pushes** can't drive dispatch directly (the dispatcher references the storage
  service — a direct call would be an activation cycle). Instead the backend **publishes a
  `RegistryResync` event** after a reconcile that moved a branch tip; a small workflow-side
  handler (`RegistryResyncHandler`) replays registration for the scope (ENTER for
  added/changed schemas) and drives EXIT for removed ones. This is event-decoupled, so there
  is no cycle.

### Stage-aware registration

Registration is keyed by **`scope + stage + nsURI`** (not `nsURI` alone), so the same schema
present on several branches registers **once per stage**, each tagged with its own
`emf.model.scope` / `atlas.stage` service properties. This is what makes a git schema visible
via the REST API and to Atlas clients on every branch it lives on.

---

## Removal semantics (schema deleted on a branch)

When a push removes an `.ecore` from a branch:

- Its **EPackage is unregistered** (EXIT) for that `(scope,stage)`; copies on other branches
  survive.
- A read of an **instance whose model is now gone** fails with a clean
  **`ModelUnavailableException`** (the parse's `PackageNotFoundException` is translated) rather
  than an opaque error — so the REST layer can surface a meaningful "model unavailable" state.
- The orphaned instance is **evicted from the index** (it simply isn't re-derived) and the
  drop is logged.

> **Not yet handled:** branch deletion and force-push/history-rewrite (deferred).

---

## Referential integrity across a reload

Because reads re-parse against the package registry *as of read time*, a schema change is
picked up automatically:

- After a model is **reloaded** (changed content, same `nsURI`, new commit), a fresh read of
  an instance re-resolves its `eClass()` to the **new** model.
- A **cross-ecore reference** (an `.ecore` whose feature type lives in another `.ecore`)
  re-links to the reloaded referenced package on the next read.
- An already-retrieved object is a **frozen snapshot** — it keeps its old package and does not
  auto-refresh (callers re-read to see changes).

---

## Configuration

The backend is wired entirely through OSGi configuration. A minimal git-backed scope needs:

**1. One `GitService` per branch** (from `org.eclipse.fennec.jgit`, factory PID `GitConfig`):

```
GitConfig~main    → { repo: "git@github.com:acme/models.git", branch: "main",    id: "acme" }
GitConfig~release → { repo: "git@github.com:acme/models.git", branch: "release", id: "acme" }
```

**2. The git storage service** (factory PID `GitObjectStorage`):

| Key | Meaning |
|---|---|
| `scope` | the Model Atlas scope this repo is exposed under |
| `type.registry.map` | `String[]` of `eClassUri:registryName` — routes objects to registries (use the `EObject` URI as an instance catch-all) |
| `gitservice.target` | filter selecting this repo's `GitService`s, e.g. `(id=acme)` |
| `poll.interval.seconds` | reconcile-poll interval (default 60; ≤0 = webhook-only) |
| `storage.type` | storage type label (default `git`) |
| `registry.target` | the shared registry cache, e.g. `(registry=main)` |

**3. The registry chain** (`RegistryService`, `EPackageStageActionService`, `ScopeService`)
wired to git — set `storageService.target=(storage.type=git)` and list the branches as the
stages / trigger stages.

**4. Webhook endpoints** (if using webhooks) via per-provider ConfigAdmin pids:
`org.eclipse.fennec.git.webhook.github` (`githubSecret`, `requireSignature`) and
`org.eclipse.fennec.git.webhook.gitlab` (`gitlabToken`, `requireSignature`). Both the endpoints
and these pids belong to `org.eclipse.fennec.git.webhook.rest` in Fennec EMF Util. A provider's
`POST /github/webhook` / `POST /gitlab/webhook` endpoint **only exists while its pid is
configured** — no configuration, no endpoint (404). Configure only the provider(s) you use.
With a configuration present but an empty secret, requests are rejected with 401 unless
`requireSignature` is explicitly set to `false` (fail-closed by default).

### Complete example

A full schema-serving git scope, as OSGi Configurator JSON (adapted from the working
`GitRegistryChainIT`). The wiring link is `storage.type=git`: the storage service *publishes*
it, and the registry + stage-action service *target* it. Repo `acme/models`, branch `main`
promoted to `release`.

```jsonc
{
  // One GitService per branch (branch = stage); same repo, one shared id for the target filter.
  "GitConfig~acme-main":    { "repo": "git@github.com:acme/models.git", "branch": "main",    "id": "acme" },
  "GitConfig~acme-release": { "repo": "git@github.com:acme/models.git", "branch": "release", "id": "acme" },

  // The git storage service. Publishes `storage.type=git`; binds the GitServices by id;
  // routes EPackages to the "schema" registry (add `…#//EObject:<reg>` to route instances too).
  "GitObjectStorage~acme": {
    "scope": "acme",
    "type.registry.map": [ "http://www.eclipse.org/emf/2002/Ecore#//EPackage:schema" ],
    "gitservice.target": "(id=acme)",
    "poll.interval.seconds": 60,
    "storage.type": "git",
    "registry.target": "(registry=main)"
  },

  // Schema-registration handler, pointed at the git storage; branches are the trigger stages.
  "EPackageStageActionService~acme": {
    "storageService.target": "(storage.type=git)",
    "trigger.stages": [ "main", "release" ],
    "replay.on.startup": true
  },

  // The "schema" registry, wired to the git storage. Stages = branches; `release` is final.
  "RegistryService~acme-schema": {
    "registry.name": "schema",
    "registry.type": "SCHEMA",
    "root.eclass.uri": "http://www.eclipse.org/emf/2002/Ecore#//EPackage",
    "schema.uri": "http://www.eclipse.org/emf/2002/Ecore",
    "storageService.target": "(storage.type=git)",
    "stageActionService.target": "(component.name=EPackageStageActionService)",
    "registryService.target": "(registry=main)",
    "resourceSet.target": "(emf.name=ecore)",
    "stage.storage.mappings": [ "main:git", "release:git" ],
    "workflow.transitions": [ "main:release" ],
    "stages": [
      "{ \"name\": \"main\",    \"writable\": false, \"final\": false }",
      "{ \"name\": \"release\", \"writable\": false, \"final\": true  }"
    ]
  },

  // The scope, bound to the registry; its activation builds the per-stage registries and
  // replays cold-start registration.
  "ScopeService~acme": {
    "atlas.scope": "acme",
    "scope.name": "acme",
    "registryService.target": "(registry.name=schema)",
    "registryService.cardinality.minimum": 1
  },

  // Optional: webhook endpoints, one pid per provider. An endpoint only exists while its
  // pid is configured; omit a block to leave that provider's endpoint absent (404).
  "org.eclipse.fennec.git.webhook.github": {
    "githubSecret": "<hmac-secret>",
    "requireSignature": true
  },
  "org.eclipse.fennec.git.webhook.gitlab": {
    "gitlabToken": "<gitlab-token>",
    "requireSignature": true
  }
}
```

To also serve **instances** (not just schemas), add a second `RegistryService` (e.g.
`registry.type=OBJECT`, its own `root.eclass.uri`) and route to it from `type.registry.map` —
using the `EObject` URI `http://www.eclipse.org/emf/2002/Ecore#//EObject:<registry>` as the
per-instance catch-all.

---

## Testing

- **Unit tests** (in-bundle `test/`): `GitStorageHelperTest`, `GitEMFHelperTest`,
  `EObjectGitStorageServiceTest` — plain JUnit 5 + Mockito over a mocked `GitService`.
- **OSGi integration tests** (`…management.git.tests`): serve a **real repository over the
  `git://` protocol from a throw-away container** (`GitTestRepository`: Alpine + the
  `git-daemon` package), driving the **production `org.eclipse.fennec.jgit.GitServiceImpl`** (one
  `GitConfig` factory configuration per branch, no `privateKey` → anonymous fetch).
  `EObjectGitStorageServiceIT` covers activation, per-branch reads,
  derived metadata, write-rejection, and webhook/poll resync; `GitRegistryChainIT` stands up
  the full registry chain and covers cold-start per-branch registration, webhook/poll ENTER,
  per-stage EXIT on removal, instance resolution, model-unavailable, and referential
  integrity across a reload (instance `eClass()` and cross-ecore references).
- **Manual end-to-end test (2026-07-22)**: full runtime
  (`modelatlas.runtime_local_git.bndrun`) against a real GitHub repository over anonymous
  `https://`, three branches (= stages) `draft`/`approved`/`release`, the same nsURI with
  *different* content on `draft` and `approved`, instances on both. Verified: per-stage
  schema registration and instance reads (each stage resolves against its own branch's
  schema), reconcile-poll pickup of pushes, schema removal on one branch → clean `409`
  model-unavailable for that stage's instances while the other stage keeps working. This
  test also uncovered the cross-stage JSON limitation below (since resolved).
- **Manual end-to-end re-test (2026-08-04, after fingerprint support)**: same live setup —
  the same nsURI with different content on `draft` and `release` (release variant adds an
  `email` attribute), instances on both, then schema removal on `release` only. Verified:
  the `draft` instance keeps deserializing through REST **in XML and `application/json`**
  after the removal — the cross-stage JSON breakage below no longer reproduces.

> **jgit transport (fixed upstream, 2026-07):** the `GitService` implementation was originally
> `org.gecko.jgit`; it now ships as `org.eclipse.fennec.jgit` from Fennec EMF Util.
> `GitServiceImpl` used to be SSH-only and
> hard-wired to legacy JCraft JSch (RSA/PEM keys only, no anonymous `git://`/`https://`). It now
> uses jgit's **Apache MINA sshd** backend (`org.eclipse.jgit.ssh.apache`) and only installs the
> SSH session factory **when a `privateKey` is configured** — so anonymous `git://`/`https://`
> remotes work without a key, and SSH keys may be **ed25519 / OpenSSH-format** (a `knownHosts`
> config option was added as well). The dependency closure changed accordingly: `ssh.apache` +
> `sshd-osgi`/`sshd-sftp` + BouncyCastle instead of `ssh.jsch` + servicemix-jsch. The former
> test-only `TestGitService` stand-in has been removed — the ITs bind the real impl.

---

## Known limitations / deferred

- ~~**Cross-stage JSON serialization after a schema removal (found in the manual e2e test,
  2026-07-22 — XML unaffected).**~~
  **RESOLVED — verified in the live e2e re-test 2026-08-04 (fingerprint support).** With the
  same nsURI registered for several stages (= git branches), removing the schema on ONE
  branch used to break REST **JSON** reads of the OTHER branches' objects with HTTP 500
  (`Error serializing outgoing object`), persistently, while XML kept working. Root cause
  was never in this bundle: the old fennec-codec `MetadataServiceImpl`
  (`org.eclipse.fennec.model.metadata`) tracked whiteboard-bound `EPackage` services keyed
  by **nsURI alone** (first-wins register, unconditional remove), so one stage's unbind
  dropped the metadata entry the surviving stages' JSON codec still needed — the
  "version, not URI" / fingerprint-join problem analyzed in
  [model.atlas#156](https://github.com/eclipse-fennec/model.atlas/issues/156). The
  MetadataService has since moved to fennec emf.osgi
  (`org.eclipse.fennec.emf.osgi.metadata`) and is **fingerprint-keyed with refcounted
  shared metadata trees**: identical content across stages dedupes onto one tree that is
  only withdrawn when the *last* holder unbinds, and diverging content under one nsURI
  yields separate fingerprints/trees, so one stage's removal cannot evict another stage's
  metadata from either direction (see `docs/plans/MODEL_FINGERPRINT_PLAN.md`,
  "MetadataService interplay"). JSON content reads now survive schema removal on a sibling
  stage; the XML-only workaround is obsolete.
- ~~**EPackages cannot be resolved by nsURI through the schema REST endpoints.**~~
  **RESOLVED 2026-08-03 (fingerprint plan phase F8).** The `/{scope}/schema/...` endpoints no
  longer recompute an encoded objectId from the nsURI; they resolve nsURI → metadata via the
  `properties["nsUri"]` scan over the stage listing
  (`WritableScopeService.getMetadataByPropertyFromStageForRegistry`), which this backend's
  derived metadata has carried since 2026-07-23. All nsUri-parameterized requests
  (single-package metadata `?nsUri=`, content `/content?nsUri=`, delete, transition) now work
  for git-backed packages; the backend keeps its `scope/stage/repoPath` objectIds unchanged
  (ids are opaque to all consumers since F8). Covered by
  `GitRegistryChainIT.nsUriPropertyLookup_findsGitBackedPackages`.
- Branch deletion and force-push/history-rewrite are not yet reflected.
- Runtime/deployment wiring (PLAN.md G9): the `runtime.config.docker.git` bundle, the docker +
  local bndruns, and the `docker/modelatlas_git` image files now exist. The **local** variant
  (`modelatlas.runtime_local_git.bndrun` + `secrets.git.bndrun`) runs via Eclipse; **building the
  docker image** currently needs Gradle on **Java 21** (Gradle 8.14 can't compile the docker
  build scripts on Java 25 — a limitation shared by all docker modules).
