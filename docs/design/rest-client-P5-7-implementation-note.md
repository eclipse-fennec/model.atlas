# P5-7 — Retire `view` from the EPackage path (stage-free schema reads)

**Status:** DONE & build-green (server `rest.tests`, client `rest.client.impl` + `rest.client.osgi`).

After P5-7 **no read endpoint embeds a stage name** — client or server. The EPackage path now
mirrors the EObject path (P5-0): the server resolves each scope's final stage and walks scope
inheritance; the client never sends a stage.

## Server (`SchemaPackagesResource`)

The two stage-free schema endpoints already existed (added with P5-0): `GET /{s}/schema`
(final-stage listing via `listInFinalStageForRegistry`) and `GET /{s}/schema/content?nsUri=`
(final-stage content). P5-7 adds the one missing piece the client's `resolve()` needs:

- **`GET /{s}/schema?nsUri=`** — `listReleasedPackages` gained an optional `nsUri` query param.
  With it, returns the single final-stage `ObjectMetadata` (hierarchy-aware, via
  `getMetadataFromFinalStageForRegistry`), `ETag` attached through
  `ObjectMetadataResponseFilter` (`CacheTarget.METADATA`); `204` when absent. Without it, the
  existing listing. Mirrors the stage-explicit `listPackagesInStage` nsUri branch.

## Client

`RemoteEPackageProviderImpl` — all three reads are now stage-free (no `STAGES`/`view` segment):
- `listNsUris` → `GET /{s}/schema` (already stage-free before P5-7).
- content (`getEPackage`/`ensureAvailable`/`refresh`, and `resolve`'s content fetch) →
  `GET /{s}/schema/content?nsUri=` (collapsed the two `fetchContent` overloads into one).
- `resolve`'s metadata fetch → `GET /{s}/schema?nsUri=`.

`EagerPrefetch` no longer reads `config.getView()`; it publishes with `stage = null` (the exact
origin stage is unknown for a stage-free read; `atlas.stage` is advisory provenance).
`RemoteEPackageConfigurator` now treats `stage` as nullable and **omits** `atlas.stage` from the
service properties when unknown (rather than NPEing on the required-non-null + `Hashtable`).

## `view` knob — deprecated, not removed

Retained so existing ConfigAdmin configurations keep parsing, but no longer load-bearing:
`ClientConfiguration.getView()` / `DEFAULT_VIEW` / `Builder.view(..)` and
`AtlasClientConfig.view()` are `@Deprecated`; `AtlasClientComponent.toConfiguration` still wires it
(with `@SuppressWarnings("deprecation")`) but nothing reads it. `atlas.view` is not stamped on any
publication (already the case after P5-4).

## Behavior change (called out in the ticket)

The stage-free `/{s}/schema` listing **reads through to parent scopes' final stages**, whereas the
old `/stages/{view}` listing did not (single scope, single stage). This aligns EPackage listing
with EObject listing. `listNsUris` already used the stage-free listing before P5-7, so this was in
effect; the new `?nsUri=` metadata branch is likewise inheritance-aware (covered by
`testListReleasedPackages_WithNsUri_InheritsFromParent`).

## Tests

- Server (`SchemaPackagesResourceTest`): `testListReleasedPackages_WithNsUri_{ReturnsSingleMetadata,
  NotFound,InheritsFromParent}`.
- Client (`RemoteEPackageProviderImplTest`): content + resolve URL assertions updated to the
  stage-free shape; `getEPackage_ignoresConfiguredView_stageFreeContent` and
  `listNsUris_ignoresConfiguredView` prove a configured (deprecated) `view` no longer changes any URL.
- Client (`EagerPrefetchTest`): EAGER now stamps `stage == null`; dropped the now-pointless
  `.view(..)` from the config builders.
- `JenaAtlasClientIT`: dropped `view`/`JENA_VIEW` — reads are stage-free.

## Phase 5

P5-7 was the last Phase-5 ticket — **Phase 5 complete** (P5-0…P5-7).
