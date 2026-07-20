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
| **Metadata** | **derived** from git facts, never authored: `stage`=branch, `version`=commit SHA, `objectType`=root EClass URI, `registry`=type-map lookup. |
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
- **`api/ModelUnavailableException`** — the bundle's **only exported type** (package
  `…management.git.api`). Thrown when an instance's model is unavailable (see *Removal*).

The webhook payload models and REST ingest live in sibling bundles
(`…git.webhook.model`, `…git.github.webhook.model`, `…git.gitlab.webhook.model`,
`…git.webhook.rest`).

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
   `reconcile(branch)`.
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

**1. One `GitService` per branch** (from `org.gecko.jgit`, factory PID `GitConfig`):

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
stages / trigger stages. (See `GitRegistryChainIT` for a complete, working wiring.)

**4. Webhook secrets** (if using webhooks) via ConfigAdmin pid
`org.eclipse.fennec.model.atlas.management.git.webhook` (`githubSecret`, `gitlabToken`,
`requireSignature` — fail-closed by default).

---

## Testing

- **Unit tests** (in-bundle `test/`): `GitStorageHelperTest`, `GitEMFHelperTest`,
  `EObjectGitStorageServiceTest` — plain JUnit 5 + Mockito over a mocked `GitService`.
- **OSGi integration tests** (`…management.git.tests`): serve a **real repository over the
  `git://` protocol from a throw-away container** (`GitTestRepository`: Alpine + the
  `git-daemon` package). `EObjectGitStorageServiceIT` covers activation, per-branch reads,
  derived metadata, write-rejection, and webhook/poll resync; `GitRegistryChainIT` stands up
  the full registry chain and covers cold-start per-branch registration, webhook/poll ENTER,
  per-stage EXIT on removal, instance resolution, model-unavailable, and referential
  integrity across a reload (instance `eClass()` and cross-ecore references).

> **`org.gecko.jgit.GitServiceImpl` is SSH-only** — it unconditionally installs an SSH
> transport callback, so it works with `git@host:…` remotes but not `git://`/`https://`.
> The OSGi ITs therefore bind a test-only `TestGitService` (jgit fetch + read, no SSH
> callback) instead of the real impl. Whether to relax that upstream is an open question
> (see `PLAN.md` G8).

---

## Known limitations / deferred

- Branch deletion and force-push/history-rewrite are not yet reflected.
- `ModelUnavailableException` → clean HTTP mapping in the REST layer is a follow-up.
- Audit of by-`nsURI` resolution across REST/clients now that one `nsURI` can be registered
  per stage (the schema-registration sibling of the objectId audit).
- `org.gecko.jgit`'s SSH-only transport (above).
