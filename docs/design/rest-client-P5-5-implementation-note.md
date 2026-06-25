# P5-5 — Acceptance: validation runs unchanged in-process vs remote

**Status:** DONE & green (`:org.eclipse.fennec.model.atlas.validation.client.tests:testOSGi`,
against the locally-built `eclipsefennec/model.atlas:jena-snapshot`).

This is the proof of **Goal 1** (contract-identical surface): the same `ValidationServiceImpl`,
resolving its scope through the `ReadOnlyScopeCollector`, runs unchanged whether the bound
`ReadOnlyScopeService<EObject>` is the in-process server `ScopeServiceImpl` or the **remote** P5-4
publication of `rest.client.osgi`. Only OSGi configuration differs.

## The two configurations

- **In-process** — already covered by `validation.rest.tests` (`ObjectValidationResourceTest`):
  validation + the in-process `ScopeService`, uploading an `OclConstraintSet` via the in-process
  `ScopeService` and validating a DataGen `Company`.
- **Remote (new)** — `org.eclipse.fennec.model.atlas.validation.client.tests` (`RemoteValidationIT`),
  a testOSGi bundle. Runtime has validation + COCL + collector + OCL engine + DataGen model +
  `rest.client.osgi` + Jakarta RS client + EMF/ConfigAdmin, but **no server bundles** — the Atlas
  runs in a jena container (Testcontainers).

`ValidationServiceImpl` is byte-identical between the two; only the wiring differs (in-process
`ScopeService` vs `rest.client.osgi` ConfigAdmin factory pointed at the container). That is the
acceptance.

## RemoteValidationIT flow

1. Start the jena image (Testcontainers), mounting `docker/dockercompose/configs` (with the `cocl`
   registry); skip via `assumeTrue` if Docker/image absent (mirrors `AtlasClientOsgiIT`).
2. Drive the `rest.client.osgi` ConfigAdmin factory (`base.uri`=container, `mode=LAZY`,
   `scope.allow.list=jena`) → `RemoteScopeServicePublisher` (P5-4) publishes
   `ReadOnlyScopeService(atlas.scope=jena, atlas.remote=true)`; the collector binds it. The test
   waits on that service to confirm the binding.
3. Build an `OclConstraintSet` in-test (`COCLFactory`; constraint `self.name <> null` on `Company`,
   role `VALIDATION`), serialize to XMI, **POST** it to
   `jena/registries/cocl/stages/release/{id}` (the writable final stage).
4. `@InjectService ValidationService` → `validateWithOcl(company, coclId, "jena", rs)`. The constraint
   set is fetched from the **remote** scope (`getScopeInfo()` to find the COCL registry, then
   `get("cocl", id)` over the P5-0 stage-free content endpoint, decoded via the Atlas-aware
   ResourceSet). Asserts: a named `Company` passes; a nameless one fails the same constraint; an
   unknown `coclId` throws (proving the lookup really hits the remote scope).

## Gotchas hit (recorded for future tests)

- **Server image must carry the Phase-4/5 bundles.** The first run 404'd on `/atlas/rest/scopes` for
  the whole startup timeout. Root cause: a broken `bnd` on the codec-jsonschema bundle
  (`ParseError … Premature end of file`) left it unresolved, cascading so `scope.api` /
  `readonlyscope.collector` weren't wired → `workflow`, `rest.application`, `validation`,
  `healthcheck` all failed → no REST layer. Fixed in the image; the prerequisites for the remote
  half are: `cocl` registry in `jena.json` (+ resolved merge conflict), and a jena image rebuilt
  from this branch with `scope.api` + `readonlyscope.collector` resolving.
- **Upload via POST, not PUT.** `ObjectRegistryResource.createObject` is annotated `@POST @PUT`;
  Jersey registers only the first HTTP-method designator (`@POST`), so a PUT 405s. The test POSTs.

## Remaining in Phase 5

- **P5-7** — retire `view` from the EPackage path (server stage-free `/{s}/schema` + `/schema/content`;
  client off `getView()`). Last hardcoded stage name in a read path.
