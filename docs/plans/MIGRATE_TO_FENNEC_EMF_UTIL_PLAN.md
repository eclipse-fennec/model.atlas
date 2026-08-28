# Migrate geckoEMFUtil → fennecUtil

**Goal:** consume the migrated Eclipse Fennec emf.util artifacts (repo
`eclipse-fennec/emf.util`, checked out at `/opt/git/fennec-emf.util`) via the
`fennecUtil` bnd library and drop the `geckoEMFUtil` library from
`cnf/build.bnd`.

**State as of Mon 2026-08-03 (uncommitted, branch `true_snapshot`):
`geckoEMFUtil` is fully removed.** Full `./gradlew clean build` green.

## Key coordinates / facts

- fennecUtil library artifact: `org.eclipse.fennec.util:org.eclipse.fennec.util.workspace.library:1.0.0-SNAPSHOT`
  from Central Portal snapshots (`https://central.sonatype.com/repository/maven-snapshots/`).
  **The emf.util repo's maven groupId is `org.eclipse.fennec.util`, NOT
  `org.eclipse.fennec.emf.util`** (set in emf.util's `gradle.properties: maven_group_id`).
- common.models library artifact (provides `-library: fennecEMFModels`):
  `org.eclipse.fennec.models:org.eclipse.fennec.common.models.library:0.0.1-SNAPSHOT`.
  **The published version is `0.0.1-SNAPSHOT`** (repo `base-version: 0.0.1`;
  the docs only say `<version>` — the "1.1.0" that floats around is the
  `modelVersion` attribute of maven-metadata.xml, not the artifact version).
  Source: `/opt/git/common.models`. Its library template registers the
  "Fennec Common Models Dependencies" MavenBndRepository with an index embedded
  in the jar.
- `org.gecko.emf.util.model` (utilities Request/Response) is superseded by
  **`org.eclipse.fennec.model`** from common.models — package moved
  `org.gecko.emf.utilities` → `org.eclipse.fennec.model.utilities`.
- Where geckoEMFUtil came from (for the record): `-library: geckoEMFUtil` was
  provided by `org.geckoprojects.emf.utils:org.gecko.emf.util.jakartars.bnd.library.workspace:2.4.0-SNAPSHOT`
  (was `cnf/ext/libraries.maven`), whose jar embedded its own
  `geckoEMFUtil.maven` index + repo plugin — its deps were never in
  `cnf/central.mvn`. The `org.gecko.emf.util.documentation.generators.*` jars
  and `org.gecko.emf.rest.jakartars` are vendored in **`cnf/local/`**
  (LocalIndexedRepo), independent of any maven index.

## Done (in working tree, 2026-08-03)

1. `cnf/build.bnd`: `-library` — **`geckoEMFUtil` removed**, replaced by
   `fennecUtil`; **`fennecEMFModels` added**.
2. `cnf/ext/libraries.maven`: removed
   `org.gecko.emf.util.jakartars.bnd.library.workspace:2.4.0-SNAPSHOT`
   (the geckoEMFUtil provider) and
   `org.gecko.emf.util.qvt.bnd.library.workspace.felix:2.1.0-SNAPSHOT`
   (dead leftover — never expanded, `fennecQVT` took over); added the
   fennecUtil and common.models library coordinates.
3. `cnf/build.bnd` (bottom): **`-plugin.fennecUtil` override workaround kept** —
   the published fennecUtil library's generated index (`fennecUtil.maven` =
   `${mavendeps}` of the whole emf.util workspace) lists the sensinact
   `0.0.2-SNAPSHOT` deps, but the library's repo plugin only serves Maven
   Central / Central Portal; sensinact snapshots live on
   `https://repo.eclipse.org/content/repositories/sensinact-snapshots/`.
   Without the override every workspace task fails with
   `No metadata for revision org.eclipse.sensinact.gateway.core:*:0.0.2-SNAPSHOT`.
4. **`org.gecko.emf.util.model` replaced by `org.eclipse.fennec.model`**:
   - `data.atlas.jpa.rest/bnd.bnd` buildpath swapped; `JpaDataResource.java`
     imports/usages moved to `org.eclipse.fennec.model.utilities`.
   - `launch.bndrun` + `jpa.rest.tests/test.bndrun` re-resolved; runbundles now
     pin `org.eclipse.fennec.model;version='[0.0.1,0.0.2)'`.
5. Dead gecko buildpath entries deleted (source never imported them; runtime
   already runs on `org.eclipse.fennec.codec.rest`):
   - `rest.jsonschema/bnd.bnd`, `rest.xsdschema/bnd.bnd`: `org.gecko.emf.rest.jakartars`
   - `data.atlas.jpa.rest/bnd.bnd`: `org.gecko.emf.rest.jakartars`, `org.gecko.emf.json`
   - `data.atlas.jpa.rest.tests/bnd.bnd`: `org.gecko.emf.rest.jakartars`
   - `rest.application/bnd.bnd`: `org.gecko.emf.util.annotations`,
     `org.gecko.emf.util.documentation.generators.apis`
   - `rest.tests/bnd.bnd`: `org.gecko.emf.util.common`
6. **Documentation-generators feature unwired from runtime** (endpoints were
   already deleted 2026-03-10, `1afd7f9`; audit 2026-08-03 confirmed the
   provider bundle was in no bndrun and nothing consumed
   `EcoreToDocumentationService`):
   - `rest.application/bnd.bnd`: dropped the stale
     `model.documentation.provider` buildpath entry.
   - `runtime.config/configs/runtime.json`: removed the dead
     `ModelDocumentationProvider~docProvider` config block (plus the
     commented-out `DsComponentsCheck` block).
   - `modelatlas.runtime_base.bndrun`: removed all 6
     `documentation.generators.*` runrequires + 7 runbundles
     (startlevels 1096–1102), re-resolved.
   - **The `model.documentation.provider` bundle itself is KEPT** (still builds;
     its `documentation.generators.apis` dependency resolves from `cnf/local/`).
     Deleting it awaits a decision from the boss.
7. bndrun housekeeping: `runtime_docker_apicurio.bndrun` `-runrequires` renamed
   to `-runrequires.apicurio` (no longer clobbers the base list);
   `runtime_local.bndrun` now includes `runtime_docker_apicurio.bndrun`
   instead of base.
8. **`org.gecko.emf.osgi.*` → `org.eclipse.fennec.emf.osgi.*` swap is complete**
   in all bndruns (runbundles run `fennec.emf.osgi.component/component.minimal/metadata`).
   The remaining `org.gecko.emf.osgi.*` mentions in `runtime_base.bndrun`
   (~65–68), `data.atlas.jpa.rest/launch.bndrun` (~121) and
   `jpa.rest.tests/test.bndrun` (~147) are **intentional `-runblacklist`
   guards** that keep the gecko bundles out of the resolution — do NOT remove
   them, they are not stale requirements.

## Upstream fix needed in emf.util (removes the build.bnd override)

In `org.eclipse.fennec.util.workspace.library/resources/template/workspace.bnd`,
add the sensinact repos to the plugin (or stop leaking non-Central deps into
`${mavendeps}`):

```
-plugin.fennecUtil: \
	aQute.bnd.repository.maven.provider.MavenBndRepository;\
		releaseUrl	= "https://repo.maven.apache.org/maven2/,https://repo.eclipse.org/content/repositories/sensinact-releases/"; \
		snapshotUrl	= "https://central.sonatype.com/repository/maven-snapshots/,https://repo.eclipse.org/content/repositories/sensinact-snapshots/";\
		index		= "\${.}/fennecUtil.maven";\
		readOnly	= true;\
		name="Eclipse Fennec Util - ${Bundle-Version}"
```

(Also fix the stray leading space in the existing `releaseUrl` value.) After a
new emf.util snapshot is published: remove the override block from
`cnf/build.bnd` and refresh the expanded library cache
(`rm -rf cnf/cache/*/expanded/urn%resource%org.eclipse.fennec.util.workspace.library-*`
or `./gradlew cleanCache`) — the expanded template is cached by *version*, not
timestamp, so it does NOT refresh on its own.

## Remaining

1. **Boss decision: delete `model.documentation.provider` bundle?**
   - If yes: delete the bundle, then prune `cnf/local/` —
     the 7 `org.gecko.emf.util.documentation.generators.*` dirs + their
     `index.xml` entries.
   - If no (feature may come back): keep bundle; only the 6 non-`apis`
     generator dirs in `cnf/local` are prunable (runtime no longer pulls them),
     `apis` must stay for the buildpath.
   - Either way check whether the vendored `cnf/local/org.gecko.emf.rest.jakartars`
     dir is still referenced by anything before pruning it.
2. Upstream emf.util template fix (section above) → drop the
   `-plugin.fennecUtil` override from `cnf/build.bnd`.
3. After committing: rebuild the `jena-snapshot` docker image (runtime_base
   changed!) and rerun `rest.client.osgi.tests:testOSGi` — see Gotchas.

## Gotchas learned along the way

- **`rest.client.osgi.tests` ITs run against the locally-built Docker image
  `eclipsefennec/model.atlas:jena-snapshot`** (AtlasClientOsgiIT). After ANY
  server-side change, rebuild it (see `update.sh`: export
  `modelatlas.runtime_docker_jena`, `docker:modelatlas_jena:prepareDocker`,
  `docker build`). A stale image fails
  `lazyMode_frameworkResourceSetResolvesUnknownNsUriViaAtlas` with
  "should resolve through the Atlas-aware ResourceSet ==> expected: not <null>".
- The atlas client itself publishes to Central Portal snapshots on every
  `snapshot`-branch push as
  `org.eclipse.fennec.model.atlas:org.eclipse.fennec.model.atlas.rest.client.{api,impl,osgi}:0.1.0-SNAPSHOT`
  (+ `scope.api`) — emf.util consumes these instead of `cnf/local` jars.
