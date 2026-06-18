# P5-4 — Publish `ReadOnlyScopeService<EObject>` OSGi service per scope

**Status:** DONE & build-green (`:org.eclipse.fennec.model.atlas.rest.client.osgi:test`).

## What shipped

`rest.client.osgi` now publishes one remote `ReadOnlyScopeService<EObject>` OSGi service per
Atlas **scope** — the EObject-side counterpart of the EPackage trio (`RemoteEPackagePublisher`,
P3-3). A consumer's `@Reference(target="(atlas.scope=jena)") ReadOnlyScopeService<EObject>`
resolves against this client exactly as it does against the in-process server-side
`ScopeServiceImpl` publication (the Phase-4/5 symmetry payoff).

### New class — `RemoteScopeServicePublisher`

Mirrors `RemoteEPackagePublisher` in structure (per-key registration map, `NsUriLocks` reused as
the per-scope keyed lock, atomic publish/republish/unpublish, `unpublishAll`):

- `publish(scope, service)` — idempotent per scope; registers under the raw
  `ReadOnlyScopeService.class` (type erasure — a `ReadOnlyScopeService<EObject>` `@Reference`
  binds it, same as the server).
- `republish(scope, service)` — atomic swap under the per-scope lock (P3-9 idea, scope-scoped):
  register new → flip tracked registration → revoke old.
- `unpublish(scope)` / `unpublishAll()` — clean revocation (quiet on `IllegalStateException` at
  framework shutdown).
- `isPublished` / `publishedScopes` accessors.

### Property contract stamped on each publication

| Property | Value | Why |
|---|---|---|
| `atlas.scope` | scope name | the collector key (`ReadOnlyScopeCollector`) and what consumers filter on |
| `atlas.remote` | `true` | distinguishes a remote publication from an in-process one of the same scope |
| `atlas.base.uri` | client base URI | provenance; matches the P3-10 `ResourceSetConfigurator` publication |

`atlas.view` is **not** stamped — it is advisory only (no consumer filters on it; reads always
target the final stage, resolved server-side), and the in-process `ScopeServiceImpl` does not
stamp it on the scope service either (`ScopeServiceConfig` has no `atlas_view`). This keeps the
published property *set* shape-identical with the server.

### Wiring in `AtlasClientComponent`

- New `RemoteScopeServicePublisher scopePublisher` field, built from the bundle `BundleContext`
  and the configured base URI.
- `publishScopeServices(configuration)` runs inside the existing strict-aware `try` block (after
  the EAGER prefetch / force-remote check). Scope set = `scope.allow.list` when configured (no
  server call — the per-scope façade fetches lazily), otherwise `client.listScopeNames()`
  (`GET /scopes`). Publication is **independent of the EPackage resolution mode** (EAGER/HYBRID/LAZY).
- In `mode.strict` an unreachable server makes `listScopeNames()` throw → the existing `tearDown()`
  path runs and activation fails cleanly (same contract as the EAGER prefetch).
- `tearDown()` calls `scopePublisher.unpublishAll()` before the EPackage `publisher.unpublishAll()`.

### Drift note (intentional non-wiring)

The published service is a **stable façade over a cache**; EObject *content* drift is absorbed
**in place** by that cache (the scope-level drift watcher refreshes it — P5-2), so content changes
need **no** re-registration (swapping would throw away the warm cache). `republish` exists and is
unit-tested for swapping a scope's backing service (e.g. a scope-set reconfiguration), satisfying
the "replaced atomically, per-scope lock" criterion as a capability — it is deliberately not wired
to content drift.

## Tests

`RemoteScopeServicePublisherTest` (7 cases, mocked `BundleContext`, captured property `Dictionary`):
the `atlas.*` contract incl. `atlas.view` absent; per-scope idempotency; atomic republish revokes
old not new; republish-as-fresh-publish; unpublish; `unpublishAll`; blank-scope rejection. Full
osgi-bundle suite green.

## Remaining in Phase 5

- **P5-5** — acceptance: validation runs unchanged in-process vs remote (depends P5-4, P4-6).
- **P5-6** — object identity + cross-reference tests via the Atlas-aware ResourceSet.
- **P5-7** — retire `view` from the EPackage path (server stage-free `/{s}/schema` + `/schema/content`;
  client off `getView()`).
