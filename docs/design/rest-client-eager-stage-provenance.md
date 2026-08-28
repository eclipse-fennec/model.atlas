# EAGER prefetch — stage/version provenance (Option A), and Option C as a future improvement

## Problem

After P5-7 made the EPackage read path stage-free, EAGER prefetch (`EagerPrefetch.prefetchScope`)
had no stage to stamp on the packages it published: it listed nsURIs (`listNsUris`) and fetched
content (`ensureAvailable`), neither of which carries origin metadata, so it published with
`stage = null` / `version = null`. HYBRID was unaffected — it uses `resolve()`, which returns the
exact `scope/stage/version`.

## Option A (implemented)

**The listing EAGER already performs carries the metadata.** `GET /{s}/schema`
(`listInFinalStageForRegistry`) returns an `ObjectMetadataContainer` whose every entry is a full
`ObjectMetadata` with `objectId` + owning `scope` / `stage` / `version`. The client previously
parsed only `objectId` and discarded the rest.

Changes (client only — **no server change**, the listing already returns this):
- **`rest.client.api`** — new `PackageDescriptor(nsUri, scope, stage, version)` record; new
  `RemoteEPackageProvider.listPackages(scope)` (a `default` method deriving metadata-less
  descriptors from `listNsUris`, so existing providers keep compiling).
- **`rest.client.impl`** — `RemoteEPackageProviderImpl.listPackages` parses the full listing
  (`parseDescriptors`); `listNsUris` now delegates to it (`map(PackageDescriptor::nsUri)`), so there
  is one listing path.
- **`rest.client.osgi`** — `EagerPrefetch.prefetchScope` lists via `listPackages` and publishes each
  package with the descriptor's `scope`/`stage`/`version`. The scope is the **owning** scope from the
  listing (a parent for an inherited package), matching HYBRID's `resolve()`-based provenance, with a
  fallback to the queried scope if the server omits it.

Cost: **same number of HTTP calls as before** (1 listing + N content); the stage/version come for
free from the listing already in hand. `RemoteEPackageConfigurator` keeps tolerating a null stage
(omits `atlas.stage` when unknown), so a provider that doesn't surface listing metadata still works.

## Option C (future improvement — not implemented)

Make the **content** endpoint self-describing: have `GET /{s}/schema/content?nsUri=` (and the EObject
`/{s}/registries/{r}/content`) stamp the origin as response headers —
`Atlas-Scope` / `Atlas-Stage` / `Atlas-Version` / `Atlas-Registry` — alongside the existing
`ETag`/`Last-Modified` (extend `ObjectMetadataResponseFilter`). Then a single content GET yields the
package **and** its origin, which would:

- let `resolve()` collapse from **two** round-trips (metadata `GET /{s}/schema?nsUri=` + content) to
  **one**, benefiting HYBRID, the LAZY registry, and drift re-resolution — not just EAGER;
- make Option A's separate listing-metadata parse unnecessary (EAGER could read origin from the
  content response too).

Cost: a server change (emit the headers) + an image rebuild + the client reading headers. Deferred;
Option A already closes the EAGER `stage=null` gap with no server churn. Tracked here as the
follow-up if `resolve()`'s double round-trip becomes worth removing.
