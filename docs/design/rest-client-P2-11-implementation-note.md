# P2-11 — Implementation note (integration tests vs a live Atlas)

**Ticket:** P2-11 "Integration tests against a live Atlas server" (Phase 2, final ticket).
**Depends on:** P2-1 … P2-10. **Date:** 2026-06-08.

## Scope

End-to-end tests for the plain-Java `ModelAtlasClient` against a real Atlas server started with
Testcontainers from the `eclipsefennec/model.atlas:jena-snapshot` image. Plain JUnit + Testcontainers
(NOT an OSGi test): the plain-Java `ModelAtlasClient.builder()` resolves its factory via `ServiceLoader`,
which is fragile inside an OSGi framework, so a flat classpath is the truest test of the client.

Bundle: `org.eclipse.fennec.model.atlas.rest.client.tests` — `test/…/JenaAtlasClientIT.java`. The test
skips automatically when Docker is unavailable (`assumeTrue(DockerClientFactory…isDockerAvailable())`) so a
build without a Docker daemon stays green.

## Bringing the `jena` scope up in the container

The bare image only registers the root `atlas` scope. The `jena` scope is a ConfigAdmin factory config
(`ScopeService~jena`) that the compose stack supplies by mounting `./configs`. The IT replicates exactly
what `docker-compose-jena.yml` does:

```java
new GenericContainer<>(IMAGE).withExposedPorts(8080)
    .withEnv("STORAGE_ROOT", "/opt/modelatlas/runtime/data")
    .withEnv("ATLAS_HTTP_PORT", "8080")
    .withFileSystemBind(resolveConfigsDir(), "/opt/modelatlas/runtime/load", BindMode.READ_ONLY)
    .waitingFor(Wait.forHttp("/atlas/rest/scopes").forStatusCode(200)
        .forResponsePredicate(body -> body.contains("jena")))   // wait until jena binds, not just the REST layer
```

`resolveConfigsDir()` walks up from `user.dir` to find `docker/dockercompose/configs`, so the bind works
regardless of the directory the test task runs from.

## Two issues this test surfaced

### 1. Stage-name mismatch (client config)

The `atlas` (root) scope's schema registry uses a final stage named **`released`**; the `jena` scope's
schema registry (in `configs/jena.json`) names its final stage **`release`**. The client's default
`view` is `released`, so `listNsUris("jena")` 400'd with *"Stage released is not a valid stage for the
registry schema"*. Fixed in the test config via the existing configurable view (P2-3):
`ClientConfiguration.builder()…view("release").defaultScope("jena")`.

### 2. Content did not inherit from parent scopes (server fix)

`listNsUris` was changed to use the hierarchy-walking discovery alias (see below), so it surfaces
packages inherited from the parent `atlas` scope. But the content GET could not then fetch them: in
`ScopeServiceImpl`, `getMetadataFromStageForRegistry` fell back to the parent's final stage, while
`getContentFromStageForRegistry` did **not** (no parent fallback at all). Net effect: packages were
visible in the listing but unfetchable — the client could see what it couldn't get.

Root-cause fix (server, `org.eclipse.fennec.model.atlas.workflow`):

- Added `getContentFromFinalStage(scope, objectId)` to the `RegistryService` EMF model (sibling of
  `getMetadataFromFinalStage`); impl resolves the configured final stage and delegates to
  `getContentFromStage`.
- Added `getContentFromFinalStageForRegistry(registry, objectId)` to `ScopeService`.
- Gave `ScopeServiceImpl.getContentFromStageForRegistry` the same parent-final-stage fallback block the
  metadata path already had (atlas + SCHEMA → `atlasSchemaRegistryService.getContentFromFinalStage`,
  else parent → `getRegistryService(registry).getContentFromFinalStage`).

Because each scope resolves against its **own** final stage, the `release` vs `released` naming
difference is handled server-side. `rest.application` needed no change — `getPackageContent` already
delegates to `getContentFromStageForRegistry`.

New server tests (`workflow.tests`): `ScopeServiceIntegrationTest.shouldFallbackToParentScopeForContent`
+ `shouldFallbackToParentScopeForFinalStageContent`, and
`RegistryServiceIntegrationTest.shouldRetrieveContentFromFinalStage`. (The path was previously untested —
the tests stayed green before the change because nothing exercised content inheritance.)

## Client change — discovery uses the hierarchy-walking alias

`RemoteEPackageProviderImpl.listNsUris` switched from the stage-explicit listing
`GET /{scope}/schema/stages/{view}` (single scope, single stage — no inheritance) to the released/
final-stage alias **`GET /{scope}/schema`** (`SchemaPackagesResource.listReleasedPackages` →
`listInFinalStageForRegistry`), which walks the scope hierarchy and surfaces parent-scope packages, each
resolved against that scope's own final stage. Response shape is the same `ObjectMetadataContainer`
(204 → empty), so `parseNsUris` is unchanged. Content fetch (`getEPackage`) is unchanged — still
`…/stages/{view}/content?nsUri=`, now backed by the server's content inheritance.

Updated impl unit tests: `listNsUris_getsSchemaPath_andDecodesObjectIds` expects `[jena, schema]`;
`listNsUris_usesConfiguredView` → `listNsUris_ignoresConfiguredView` (discovery no longer takes a view
segment).

## Tests (`JenaAtlasClientIT`) — all green

- `listScopeNames_includesJena` — `GET /scopes` lists the `jena` scope.
- `getEPackage_roundTrips_andSecondCallIsCached` — fetch the first released nsURI, assert nsURI +
  `EFactory`, and that the second look-up returns the **same** instance (cache hit).
- `newResourceSet_resolvesUnknownNsUriThroughAtlas` — a fresh client's ResourceSet resolves the nsURI
  through the Atlas-aware delegating registry (P2-8).
- `denyList_blocksAnOtherwiseAvailablePackage` — an nsURI on `nsUriDenyList` (P2-9) does not resolve even
  though the server has it.

Data-dependent tests use `assumeFalse(listNsUris("jena").isEmpty())` so they skip rather than fail if the
scope is empty. The released packages exercised come from the parent `atlas` scope, inherited via the
server fix above.

## CI / skipping when the image isn't built

The `jena-snapshot` image is built **locally** and not published, so it is absent on CI runners that
have Docker but never build it. The first guard (`assumeTrue(isDockerAvailable())`) only covers the
*no-Docker* case; GitHub Actions runners **do** have Docker, so without a second guard Testcontainers
would try to **pull** the missing image and the IT would **error** (it would not skip).

A second guard checks the local image store and skips (does not pull) when the image is absent:

```java
assumeTrue(atlasImageAvailableLocally(),
    "Image " + IMAGE + " not present locally — skipping (build it to run this IT)");

private static boolean atlasImageAvailableLocally() {
    try {
        DockerClientFactory.instance().client().inspectImageCmd(IMAGE).exec();
        return true;
    } catch (RuntimeException notPresent) {   // docker-java NotFoundException extends RuntimeException
        return false;
    }
}
```

Net behaviour:

- **Locally** (image built) → both assumes pass, all four IT tests run.
- **CI / no image** → the IT **skips**, the build stays green. Real coverage in CI comes from the 61
  `rest.client.impl` unit tests, which always run.

This is why the CI `build` job (`./gradlew clean build`, which runs the client `test` task with Docker
present) does not fail even though it never builds the jena image — CI builds only the `apicurio` and
`file` variants, and only the `container_deploy` job (on `main`/`snapshot` push) builds/pushes images.

### Optional follow-up — run the live IT in CI (not done)

To actually exercise the IT on CI, build the image before the client `test` task: build the non-IT
parts first, then `export.modelatlas.runtime_docker_jena` → `docker:modelatlas_jena:prepareDocker` →
`docker build -t eclipsefennec/model.atlas:jena-snapshot docker/modelatlas_jena/`, then run the client
IT. Requires reordering the `build` job (the image depends on the build that currently also runs the IT)
and adding the jena export to CI, which today exports only `apicurio` + `file`. Left as a follow-up.

## Deferred (need server-side writes / an auth-enabled server)

Drift `onPackageChanged` after a server mutation, and end-to-end bearer auth — both require driving
server state the read-only client can't set up itself.
