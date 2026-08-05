# Quality review — model.atlas — 2026-08-05

Mode: quick, follow-up of [quality-review-2026-07-24.md](quality-review-2026-07-24.md) · Scope: whole repo (status re-verification of all 122 previous findings) + fresh review of the git-backend bundles that were not on the reviewed branch last time · Rule sets: SOLID/OSGi + Eclipse Foundation (see skill references)
Branch reviewed: `code_quality_review` (after merging `snapshot`, incl. git storage backend, fingerprint support, CI reusable-workflow migration).

## Summary

| Severity | Carried over (still open) | Partially resolved | New (this review) | Open total |
|---|---|---|---|---|
| **blocker** | 7 (F3–F9) | – | 1 (F123) | **8** |
| **major** | 30 | 2 (F11, F32) | 3 (F124–F126) | **33 (+2 partial)** |
| **minor** | 33 | 5 (F71, F91, F96, F98, F108) | 6 (F127–F131, F133) | **39 (+5 partial)** |
| **info** | 8 | 1 (F122) | 1 (F132) | **9 (+1 partial)** |
| **Total** | **78** | **8** | **11** | **89 (+8 partial)** |

Of the 122 findings from 2026-07-24, **36 are resolved** (or no longer applicable), **8 partially resolved**, **78 still open**. Every fix from the 2026-07-24 fixing sessions survived the subsequent merges — nothing regressed. The resolved set is concentrated in the areas that were actively fixed (storage-layer lifecycle/correctness F15–F22, license headers F1/F2/F10, unbind-by-key F22, typos F56); the open set is dominated by the **DS-component-as-exported-API blockers (F3–F7)**, the **sibling contract-drift cluster** in REST handlers and storage backends, and the **release-readiness gaps** (baselining off, 150 `restricted` Dash entries — up from 126, README/publishing story mismatch). The freshly reviewed git-backend bundles are in good shape overall (webhook security fundamentals are done right: constant-time HMAC comparison, fail-closed defaults, secrets never logged) but contribute one new blocker (webhook.rest exports its component package) and two DS-lifecycle majors that repeat known bug classes, plus one deployment-security major (`requireSignature: false` shipped in the docker config).

Since this is the second consecutive review, **every carried-over finding is now open across 2 reviews** — the fix-rate signal: what got dedicated fixing sessions got fixed and stayed fixed; nothing else moved.

## Previous findings status

Resolved 36 · Partial 8 · Still open 78 (details per finding; fresh evidence for open findings is in the carried-over tables below).

| id | status | note |
|---|---|---|
| F1 | ✅ resolved | headers on all files, `license-eye` green; re-checked after merges |
| F2 | ✅ resolved | EPL-1.0 → EPL-2.0 replaced |
| F3 | ✅ fixed 2026-08-05 | collectors split: interfaces keep the FQNs in the exported package, `*Impl` components moved to private `workflow.impl` |
| F4 | ✅ fixed 2026-08-05 | BasicStorageRegistry moved to private `mgmt.registry.impl` (+ its white-box test) |
| F5 | ✅ fixed 2026-08-05 | `mgmt.collector` un-exported (only consumer was dead commented-out code) |
| F6 | ✅ fixed 2026-08-05 | same split as F3: interface in place, `ReadableScopeCollectorImpl` in private impl package |
| F7 | still open — ⏸ deferred | bundle's repo fate escalated to management (fennecUtil migration); do not refactor meanwhile |
| F8 | ✅ fixed 2026-08-05 | management approved the drafted fix: `EnhancedUMLResourceFactoryImpl` deleted, `UMLConfigurationComponent` registers the API-typed `UMLResource.Factory.INSTANCE` |
| F9 | ✅ resolved 2026-08-05 | schema.registry.impl DELETED together with schema.registry.api — the api's only real importer was this dead impl; neither was in any bndrun (also closes F117, and F102 by removal) |
| F10 | ✅ resolved | license.yml on push/PR, pinned `skywalking-eyes@v0.8.0` |
| F11 | 🟡 partial | tooling + DEPENDENCIES in place; **150 `restricted` entries (up from 126)**, IP-Lab review outstanding |
| F12 | ✅ resolved | SECURITY.md present |
| F13 | ✅ resolved | CODE_OF_CONDUCT.md present |
| F14 | ➖ deferred by design | `fennec-baselining: false` (cnf/build.bnd:55) — the project is still a prototype published only as snapshots, so there is no released baseline to check against (owner decision 2026-08-05); revisit at the first real release |
| F15–F22 | ✅ resolved | all storage-layer fixes verified still present post-merge |
| F23 | still open | searchObjectIds still returns all ids |
| F24 | ✅ resolved | stdout payload dump gone |
| F25–F28 | still open | apicurio activation/robustness/LSP + Lucene injection cluster unchanged |
| F29 | ✅ resolved | `@Deactivate` on Lucene index close() |
| F30, F31 | still open | catch-all `e.getMessage()` (19 sites) / 204-vs-200 drift unchanged |
| F32 | 🟡 partial | UUID objectIds ended cross-stage delete-clobber; transition still never reindexes → stale stage in index |
| F33 | ✅ fixed 2026-08-05 | parameterized media types now accepted — shared `isMediaType` check in rest.common, all four EPackage handlers aligned |
| F34, F35 | still open | IOException drift, ResourceAttacherHelper unchanged |
| F36 | ✅ fixed 2026-08-05 | half the finding was wrong (EAGER already filtered on strict); the real fatal call was scope discovery. Guarded, with three ITs that fail pre-fix |
| F37–F39 | still open | doc-provider hashCode persistence + closed dispatch, bootstrap registry leak |
| F40 | ✅ fixed 2026-08-05 | ScopesHealthCheck reference now DYNAMIC/GREEDY + volatile; new `healthcheck.tests` bundle proves it (2 ITs fail pre-fix, pass post-fix) |
| F41 | ✅ resolved | workflow promise executor shut down |
| F42 | ✅ fixed 2026-08-05 | unmatched storageType now logs a WARNING naming stage/registry/available types, and all 10 lookups go through `storageFor(stage)`, which throws with the configuration context instead of NPE-ing |
| F43 | still open | **worse: parent-fallback copy-paste grew 5 → 6 sites** |
| F44 | still open | `registryView` still UOE (P6-4 not landed) |
| F45, F46 | still open | southbound hardcoding; EMap-entry factory gap |
| F47–F51 | still open | jpa.watcher/jpa.rest cluster unchanged |
| F52 | still open | stageName still ignored (planned refactor not landed) |
| F53, F54 | still open | customGenerators dead; jena/DataGen hardcoded |
| F55 | still open | example model still in runtime_base.bndrun:44,133 |
| F56 | ✅ resolved | typo sweep held |
| F57 | still open | SchemaPackagesResource now 1026 lines, same mixing + duplication |
| F58 | ➖ n/a | Base64 objectId encoding removed from REST — but the same default-charset pattern now lives in workflow (new F133) |
| F59 | still open | 204-with-entity ×6 |
| F60 | ✅ resolved | unknown scope now 400 via ModelAtlasRequestFilter:123-129 |
| F61–F63, F65–F67, F69, F70 | still open | debug endpoints, /schema substring, iterator().next(), Content-Disposition drift, javadoc, validation dup/srp — unchanged |
| F64 | ✅ resolved | copy-serialize in try/finally (jsonschema got the same treatment) |
| F68 | ✅ resolved | format strings fixed |
| F71 | 🟡 partial | class-level exception contract documented; per-method docs + null-return contract still missing |
| F72–F76 | still open | validation/datagen minors unchanged |
| F77 | ✅ resolved | stale codegen comment gone |
| F78–F89 | still open | doc-provider, bootstrap, healthcheck, mediatypes, emf.common, client minors unchanged |
| F90 | ✅ resolved | finders call ensureCacheInitialized() |
| F91 | ✅ fixed 2026-08-05 | commented registration blocks deleted earlier; the 319-line commented-out `StorageRegistryServiceImpl.java` is now deleted too (every line was `//`-commented; only doc/plan prose referenced it) |
| F92–F95 | ✅ resolved | verified still present |
| F96 | ✅ fixed 2026-08-05 | type-level javadoc added: derived/rebuildable index, hit coordinates, thread-safety |
| F97 | still open | collector/registry duplicate tracking |
| F98 | ✅ fixed 2026-08-05 | `loadMetadata` now guards the metadata-type resolution the same way `loadEObject` does (ModelUnavailableException instead of an NPE inside sendGETRequest / a CCE) |
| F99, F100 | ✅ resolved | interrupt restore; volatile fields |
| F101 | ✅ fixed 2026-08-05 | `PostReleaseActionService` DELETED (no impl, no caller; only user was dead test scaffolding `WorkflowTestBase`, deleted too, plus a dead `EPackagePostReleaseActionService` PID in docker/dockercompose/configs/jena.json, removed). The exported package `...atlas.workflow` deliberately stays at `@Version("1.0")`: prototype, snapshot-only publishing, so removals need no major bump (owner decision 2026-08-05). |
| F102 | ➖ n/a | closed by the schema.registry removal (F9) |
| F103–F106 | ✅ resolved | EMFFileWatcher/EormFileWatcher fixes held through the merges |
| F107 | ✅ fixed 2026-08-05 | every PID/property in `WatcherConstants` documented, plus a type-level contract note |
| F108 | 🟡 partial | 409 still never produced (ePackageUri now filter-validated) |
| F109 | ✅ fixed 2026-08-05 | `/home/ilenia/tests/jpa/` replaced by the repo's env→prop→default chain (`JPA_WATCH_PATH`, defaulting under `base.path`) |
| F110 | ✅ resolved | CONTRIBUTING points at SECURITY.md |
| F111 | ✅ fixed 2026-08-05 | README "Branches & releases" now states both outputs (Maven Central bundles under `org.eclipse.fennec.model.atlas` + the container images) and notes that test/config bundles stay local via `-maven-release: local` |
| F112–F118 | still open | infos unchanged; F118: fingerprint got an API home, contentHash didn't |
| F119, F120 | ✅ resolved | verified |
| F121 | still open | UML2 vendoring provenance still undocumented |
| F122 | 🟡 partial | `REFACTORING_PLAN.md` + `exporter-migration-investigation.md` moved to `docs/plans/` 2026-08-05; only `update.sh` remains at root (kept deliberately — it drives the jena snapshot-image rebuild) |

## New findings

### F123 · blocker · api-hygiene · management.git.webhook.rest ✅ FIXED (2026-08-05)
- **Where:** org.eclipse.fennec.model.atlas.management.git.webhook.rest/src/org/eclipse/fennec/model/atlas/management/git/webhook/rest/package-info.java:26
- **What:** The bundle's only package — consisting entirely of DS components (two whiteboard resources, two signature filters) and their name-binding annotations — carries `@org.osgi.annotation.bundle.Export` with no `@Version` (falls back to 0.1.0); no external consumer exists in the workspace.
- **Why it matters:** Implementation classes become public API with semantic-versioning obligations, against the repo's own convention (rest.application keeps all resource/filter packages private).
- **Suggested fix:** Delete the `@Export` line; the white-box tests live in the same bundle and are unaffected.
- **Status:** ✅ Fixed 2026-08-05 — `@Export` removed; `-privatepackage` added to `bnd.bnd` (the annotation was the only content-inclusion mechanism), which also required fixing the file's dangling continuation and added the missing `Bundle-Version: 1.0.0.SNAPSHOT` + name/description (closes F130 too). Manifest verified: package now `Private-Package`, no exports.

### F124 · major · security · runtime.config.docker.git ✅ FIXED (2026-08-05)
- **Where:** org.eclipse.fennec.model.atlas.runtime.config.docker.git/configs/workflow.json:114 and :118
- **What:** The shipped docker configuration sets `"requireSignature": false` for both webhook PIDs (GitHub + GitLab) while the secrets default to empty, overriding the filters' fail-closed component default (`requireSignature() default true`).
- **Why it matters:** A stock docker-git deployment where the operator forgets to set `GIT_WEBHOOK_GITHUB_SECRET`/`GIT_WEBHOOK_GITLAB_TOKEN` silently accepts unauthenticated webhook POSTs (anyone reaching the endpoint can trigger fetch/reconcile/resync churn) instead of returning 401.
- **Suggested fix:** Remove both `"requireSignature": false` lines — the component default is the safe one; put the override only in the local-run secrets bndrun if needed for testing.
- **Status:** ✅ Fixed 2026-08-05 — both PIDs now use the file's standard env→prop→default chain with **default=true** (`GIT_WEBHOOK_REQUIRE_SIGNATURE`); `secrets.git.bndrun` sets `-DGIT_WEBHOOK_REQUIRE_SIGNATURE=false` for local manual runs only.

### F125 · major · osgi-ds · management.git ✅ FIXED (2026-08-05)
- **Where:** org.eclipse.fennec.model.atlas.management.git/src/org/eclipse/fennec/model/atlas/management/git/GitStorageHelper.java:155-156 (reached from EObjectGitStorageService.java:210 → createStorageHelper:433)
- **What:** The helper constructor synchronously runs `refresh()` (remote `fetch()` per branch) and `deriveAll()` (parses every routable file of every branch) inside DS `activate()` — which the STATIC/GREEDY `gitservice` reference re-triggers on every branch-set change.
- **Why it matters:** Network fetches and a full-repo parse block the SCR thread; a hung remote stalls component activation indefinitely (same class as F21/F25).
- **Suggested fix:** Create `rederiveExecutor` before `activateStorageService()` and run the initial `refresh()+deriveAll()` on it — reads already tolerate a not-yet-primed helper.
- **Status:** ✅ Fixed 2026-08-05 — constructor no longer primes; new `GitStorageHelper.prime()` runs on the re-derive worker submitted from `activate()` (executor now created first, shut down if activation throws). Priming completion publishes a scope-wide `RegistryResync` ENTER replay, because the workflow's cold-start replay can run against the still-empty store (found by the chain ITs). Unit tests + all 15 OSGi ITs green (`awaitStorage` in the IT now awaits priming, matching the documented read contract).

### F126 · major · osgi-ds · management.git ✅ FIXED (2026-08-05)
- **Where:** org.eclipse.fennec.model.atlas.management.git/src/org/eclipse/fennec/model/atlas/management/git/GitStorageHelper.java:624-630
- **What:** `closeStorageResources()` clears the local `derived` map but never calls `registryService.removeFromCache(...)` for the entries this helper pushed into the shared registry cache — unlike `evictStage` (line 307), which does exactly that on reconcile.
- **Why it matters:** Dropping a branch (or removing the config) leaves that stage's metadata permanently in the shared registry cache — listings keep advertising objects no storage service can load (the known EMFFileWatcher global-registry leak class; the component's own contract is to re-activate on every branch-set change).
- **Suggested fix:** In `closeStorageResources()`, iterate `derived.keySet()` and call `registryService.removeFromCache(id)` before clearing.
- **Status:** ✅ Fixed 2026-08-05 — `closeStorageResources()` now evicts every derived entry from the shared registry cache before clearing; regression test `close_evictsDerivedEntriesFromSharedRegistryCache` added. Unit tests + OSGi ITs green.

### New minors / info (compact)

| # | Sev | Bundle | Where | What → fix |
|---|---|---|---|---|
| F127 | minor | management.git | GitStorageHelper.java:376 | `loadMetadata` looks up `derived` with the raw id while sibling load paths accept qualified id OR bare repo path (documented D9 contract) — bare path: object loads, metadata null → normalize the id first |
| F128 | minor | management.git | ~~EObjectGitStorageService.java:219-237~~ | Unfiltered EPackage ServiceTracker calls `getService()` for every EPackage in the framework though the object is never used — eagerly instantiates + pins use counts → return null from `addingService` after `scheduleRederive()` ✅ **FIXED 2026-08-05** — `addingService` schedules the re-derive and returns null, so nothing is instantiated or pinned. Verified safe: `removedService` only ungot the service (no re-derive on removal), so leaving services untracked loses no behaviour. |
| F129 | minor | management.git | ~~EObjectGitStorageService.java:292,343,398,467~~ | Four catches log only `e.getMessage()` (can be null), discarding stack traces; sibling GitStorageHelper logs `(Level.WARNING, msg, e)` → align ✅ **FIXED 2026-08-05** — all four (reconcile poll, webhook reconcile, re-derive pass, resync publish) now use `LOGGER.log(Level.WARNING, msg, e)` like the helper and like this file's own line 239 |
| F130 | minor | management.git.webhook.rest | bnd.bnd:10 | File ends mid-continuation (trailing `,\`, no newline) — next appended instruction is silently swallowed into `-buildpath`; also no `Bundle-Version` (publishes 0.1.0.*-SNAPSHOT while its four sibling git bundles are 1.0.0) and no Bundle-Name/Description → terminate the entry, add version/name matching siblings ✅ FIXED 2026-08-05 (with F123) |
| F131 | minor | runtime.config.docker.git | ~~test/emf-attacher-handlers.json:1 (+ bin_test copy)~~ | Unrelated "civitas meter usecase" EMFAttacherHandler config copied from the docker.file template; not in the built jar but committed → delete test/ + bin_test/ ✅ **FIXED 2026-08-05** — both directories deleted; `.classpath` never declared `test` as a source folder and `bnd.bnd` only includes `configs/`, so the bundle is unaffected |
| F133 | minor | workflow | ~~impl/AtlasSchemaRegistryService.java:84,127,300,337~~ | The default-charset Base64 objectId pattern fixed as F58 in REST lives on here: `ePackage.getNsURI().getBytes()` and `new String(...)` without `StandardCharsets.UTF_8` on both encode and decode paths → pass UTF_8 explicitly ✅ **FIXED 2026-08-05** — the four sites now go through private `encodeObjectId(EPackage)` / `decodeNsUri(String)` helpers that fix UTF-8 on both directions (also removes the 2×2 duplication). Output is byte-identical for ASCII nsURIs, so existing ids are unaffected. Same one-line defect fixed in the client mirror at `rest.client.impl/RemoteEPackageProviderImpl.java:462`, whose own test helper already used UTF-8 explicitly. |
| F132 | info | runtime | ~~secrets.git.bndrun:10~~ | Committed local-run template defaults GIT_REPO to a personal repository; no actual secrets committed anywhere → neutral placeholder ✅ **FIXED 2026-08-05** — now `https://github.com/your-org/your-model-repo.git` |

## Carried-over findings — fresh evidence (open across 2 consecutive reviews)

All locations re-verified 2026-08-05; line numbers are current. Full problem statements are in the [2026-07-24 report](quality-review-2026-07-24.md).

### Blockers

| id | Bundle | Fresh location | State |
|---|---|---|---|
| F3 | workflow | ~~package-info.java:15; three collectors~~ | ✅ **FIXED 2026-08-05** — FQN-preserving split: `ResourceSetCollector`/`ScopeServiceCollector`/`RegistryServiceCollector` are now interfaces in the exported package; components live in private `workflow.impl` as `*Impl` with unchanged component names. No production consumer changes; workflow ITs 98/98, REST ITs 212/212 green. Follow-up: the two test doubles in `rest.filter.tests` (`TestResourceSetCollector`, `TestScopeServiceCollector`) subclassed the old component classes and had to switch to `implements` + the previously inherited methods — fixed 2026-08-05, that bundle's 6 ITs green. |
| F4 | management | ~~mgmt/registry/BasicStorageRegistry.java~~ | ✅ **FIXED 2026-08-05** — component (+ white-box test) moved to private `mgmt.registry.impl`; `mgmt.registry` keeps only the API base classes. |
| F5 | management | ~~mgmt/collector/package-info.java~~ | ✅ **FIXED 2026-08-05** — `@Export`/`@Version` removed from `mgmt.collector` package-info; package now Private-Package (manifest verified). |
| F6 | readable.scope.collector | ~~ReadableScopeCollector.java:29~~ | ✅ **FIXED 2026-08-05** — same split as F3; interface exported, `ReadableScopeCollectorImpl` private; unit test updated. |
| F7 | model.documentation.provider | ModelDocumentationProvider.java:50; package-info.java:2 | ⏸ DEFERRED — bundle's repo fate escalated to management (fennecUtil migration); untouched by agreement |
| F8 | rest.uml | ~~EnhancedUMLResourceFactoryImpl.java:21,28~~ | ✅ **FIXED 2026-08-05** — management approved the drafted fix. Subclass of the internal `org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl` deleted; `UMLConfigurationComponent.registerResourceFactoryService` now registers `UMLResource.Factory.INSTANCE` (API package `org.eclipse.uml2.uml.resource`) under `Resource.Factory` with the same three `emf.*` properties (`FILE_EXTENSION` constant instead of the literal `"uml"`). Manifest verified: bundle no longer imports any `org.eclipse.uml2.*.internal.*` package. rest.tests testOSGi 208/208 green, incl. the 7 UML reader/writer tests that round-trip through the registered `.uml` factory. |
| F9 | schema.registry.impl | ~~SchemaRegistryServiceImpl.java~~ | ✅ **RESOLVED 2026-08-05** — bundle DELETED together with `schema.registry.api` (api's only real importer was the dead impl; neither in any bndrun; stale `-buildpath` entries in rest.application/rest.tests removed). Closes F117 and F102 by removal. |

### Majors

| id | Bundle | Fresh location | State |
|---|---|---|---|
| F14 | repo | cnf/build.bnd:55 | `fennec-baselining: false` — ➖ **deferred by design 2026-08-05**: prototype with snapshot-only releases, no baseline exists yet |
| F23 | management | BasicRegistryHelper.java:90-94 | searchObjectIds ignores query, returns all ids |
| F25 | management.apicurio | ApicurioStorageHelper.java:80-84 | sync HTTP scan in @Activate + printStackTrace-swallow; FileStorageHelper:51-55 fails instead — drift |
| F26 | management.apicurio | ApicurioStorageHelper.java:298-306,334-336 | null-groups NPE; foreign/hyphenated groupId breaks whole load |
| F27 | management.apicurio | ApicurioStorageHelper.java:199-204 vs FileStorageHelper.java:157-183 | delete-result semantics drift (AND vs OR) |
| F28 | management.lucene | LuceneRegistryHelper.java:691-695,318,324; LuceneEObjectRegistryService.java:439-440,683-685,728-729,765-767 | MatchAllDocs fallback + unescaped query concatenation — cross-scope exposure |
| F30 | rest.application | ObjectRegistryResource.java:130,165,234,350,399,453,554,610,680; SchemaPackagesResource.java:150,201,270,378,429,477,567,617,680,949 | 19 catch-all sites return raw `e.getMessage()`; InterruptedException swallowed at 8 Promise.getValue() sites, zero re-interrupts in bundle |
| F31 | rest.application | ObjectRegistryResource.java:571,605 vs SchemaPackagesResource.java:576,610 | 204/200 delete drift vs own @ApiResponse |
| F32 🟡 | rest.application + management.lucene | SchemaPackagesResource.java:609,673-677; EPackageLuceneIndexImpl.java:157,179 | remaining half: transition never reindexes → stale `stage` field, search hits silently dropped at :764 |
| F33 | rest.jsonschema/xsdschema/uml | ~~JsonSchemaMessageBodyReaderWriter.java:66; XSDSchemaMessageBodyReaderWriter.java:68; UMLMessageBodyReaderWriter.java:58~~ | ✅ **FIXED 2026-08-05** — the three `"type/subtype".equals(mediaType.toString())` checks now call a shared `AbstractEPackageMessageBodyHandler.isMediaType(actual, expected)` (rest.common, package bumped 1.0→1.1) that compares type/subtype case-insensitively and ignores parameters — the same semantics the `rest.ecore.xmi` sibling already had, so the four handlers no longer drift. Tests added for `;charset=UTF-8` on the UML writer+reader and the JSON-schema writer; rest.tests 215 tests, 0 failures. |
| F34 | rest.ecore.xmi | EcoreMessageBodyHandler.java:129,133 | IOException→500 while three siblings return null |
| F35 | rest.common | ResourceAttacherHelper.java:31-39 | printStackTrace swallow + `resource.save(null)` writes into server cwd; called 4× from validation.rest |
| F36 | rest.client.osgi | ~~AtlasClientComponent.java:294 (call), :251-258 (catch)~~ | network I/O in @Activate in all modes; catch(RuntimeException) rethrows unconditionally — `mode.strict=false` not honored. ✅ **FIXED 2026-08-05, with regression tests that prove it.** Re-diagnosis: the EAGER path *did* honour strict (`EagerPrefetch#failOrSkip`), so the blanket claim was too broad — but `publishScopeServices` called `client.listScopeNames()` unguarded, so an unreachable Atlas aborted activation **in every mode including the LAZY default**, contradicting this method's own javadoc. Now a `TransportException` there propagates only under `mode.strict`, otherwise it is logged and no scope façade is published; the catch-all keeps handling strict failures and genuine defects. New `AtlasClientOfflineActivationIT` (no server needed — an unreachable base URI *is* the fixture, so it runs in every build, unlike the Docker-gated `AtlasClientOsgiIT`) asserts SCR component state, not service presence: the component registers its `ResourceSetConfigurator` *before* the network call and tears it down on failure, so a service-appearance check passes either way (my first attempt did exactly that and was misleading). Three cases: LAZY+non-strict must reach ACTIVE (**failed pre-fix: `expected: <8> but was: <16>`**, i.e. FAILED_ACTIVATION), strict must reach FAILED_ACTIVATION, and a configured `scope.allow.list` must reach ACTIVE — the last one passing pre-fix is what pinpointed the discovery call. 10/10 ITs green post-fix. |
| F37 | model.documentation.provider | ModelDocumentationProvider.java:81,221 | identity-hashCode persisted for change detection; NPE on empty hash file |
| F38 | model.documentation.provider | ModelDocumentationProvider.java:56-60,125-136,146-157 | closed format dispatch (component.name targets + switch) |
| F39 | bootstrap | InitialModelLoader.java:119,267,271,294 | global-registry seed-before-validate leak; exception escapes @Activate ctor |
| F40 | healthcheck | ~~ScopesHealthCheck.java:40-41~~ | ✅ **FIXED 2026-08-05, with a regression test that proves it** — reference is now `policy = DYNAMIC, policyOption = GREEDY` on a `volatile` field. New bundle `org.eclipse.fennec.model.atlas.healthcheck.tests` (the healthcheck bundle had no test home) holds `ScopesHealthCheckIT`: it resolves the health check service first (forcing activation), then publishes a ScopeService, and asserts the scope is reported; a second test asserts a withdrawn ScopeService stops being reported. **Both tests fail on the pre-fix code** (`expected: <true> but was: <false>`) and pass after — that is the empirical proof the DS defaults never bound the late service. |
| F42 | workflow | ~~RegistryServiceImpl.java:588-591,175-176~~ | ✅ **FIXED 2026-08-05** — `parseStageStorageMappings` logs a WARNING (stage, registry, requested type, registered types) instead of dropping the mapping silently, and the 10 `storageMap.get(...)` call sites now go through `storageFor(stage)`, which throws `IllegalStateException` naming the stage, registry and configured mappings. No call site null-checked the old result, so this only replaces a bare NPE with a diagnosable failure. workflow ITs 98/98 green. |
| F43 | workflow | ScopeServiceImpl.java:115,144,201,306,328,421 | parent-fallback copy-paste **grew 5 → 6 sites** — the drift the finding predicted is happening |
| F44 | workflow | ScopeServiceImpl.java:498,507; AtlasScopeService.java:326,335 | registryView still UOE (P6-4 pending) |
| F45 | workflow | DynamicEPackageRegistrationService.java:556-558,585 | southbound topic + `<model>Sensor` EClass hardcoded |
| F46 | emf.common | EClassResolvingDynamicEFactory.java:40 | `EClassResolvingDynamicEObject.BasicEMapEntry` resolves to the INHERITED DynamicEObjectImpl.BasicEMapEntry (no override exists) — proxy-resolving eClass() missing for map entries |
| F47 | data.atlas.jpa.watcher | EormFileWatcher.java:178,264-282 | register() from load() mutates `registrations` without `lock` |
| F48 | data.atlas.jpa.watcher | EormFileWatcher.java:105-120 | deactivate never detaches Resources parked in awaitEPackageAndRegister (:162,:183) |
| F49 | data.atlas.jpa.watcher | DataFolderWatcher.java:134-194 | mid-sequence config failure orphans earlier configs; fresh matcherKey per retry (:135) |
| F50 | data.atlas.jpa.watcher | DataFolderWatcher.java:61,106,121-131,43-58 | recursive=false vs mapping/ subfolder — deferred pipeline start can't trigger; javadoc still says .jpamapping |
| F51 | data.atlas.jpa.rest | JpaDataResourceFilter.java:60-64,130-152 | whiteboard-global filter NPEs on paths without its params |
| F52 | validation.rest | ObjectValidationResource.java:63,73-74; ObjectBatchValidationResource.java:60,70-71 | stageName injected, never read (decided refactor not landed) |
| F53 | datagen | DataGenServiceImpl.java:173-185 | customGenerators extension point still dead |
| F54 | datagen.rest | DataGenResource.java:59-60,185-187 | scope "jena" + registry "DataGen" hardcoded |
| F55 | runtime | modelatlas.runtime_base.bndrun:44,133 | demo example model ships in every runtime image |

### Minors (fresh anchors only)

| id | Fresh location | id | Fresh location |
|---|---|---|---|
| F57 | SchemaPackagesResource.java (1026 lines; dup helpers :788,:804) | F79 | ModelDocumentationProvider.java:45-49 |
| F59 | ObjectRegistryResource.java:203,385,439,516,587,658 | F80 | InitialModelLoader.java:111-119,163,205-216 |
| F61 | SchemaPackagesResource.java:110-115; OpenApiResource.java:140-151 | F81 | MediaTypesHealthCheck.java:34-35,41 |
| F62 | ModelAtlasRequestFilter.java:165 | F82 | SupportedMediatypesImpl.java:39-41 |
| F63 | UMLMessageBodyReaderWriter.java:92; XSDSchemaMessageBodyReaderWriter.java:107 | F83 | SupportedMediatypesImpl.java:34-42,52-53 |
| F65 ✅ | ~~uml / jsonschema never set Content-Disposition~~ fixed 2026-08-05 — both writers now set it (`<name>.uml` / `<name>.schema.json`), asserted in rest.tests | F84 | DynamicEPackageConfigurator.java:22 |
| F66 | ResourceAttacherHelper.java:24-29 | F85 | AtlasEPackageRegistryConfigurator.java:92-93,123-130,136-144 |
| F67 | ObjectValidationResource.java:215-233 = ObjectBatchValidationResource.java:156-174 (+6 catch-ladder copies) | F86 | RemoteEPackagePublisher.java:116-137,225-231 |
| F69 | ValidationServiceImpl.java:170-245 | F87 | AtlasScopedFetchOnMissRegistry.java:93-101 vs :70-91 |
| F70 | ValidationServiceImpl.java:394-402,404-413 | F88 | ModelAtlasClientImpl.java:142-149 (drift mechanism still live on this branch) |
| F71 🟡 | ValidationService.java:24-46 (methods still undocumented) | F89 | ClientConfiguration.java:383-522 (~29 setters) |
| F72 ✅ | ~~ValidationHelper.java:27~~ fixed 2026-08-05 — documented, `final` + private ctor | F91 ✅ | ~~StorageRegistryServiceImpl.java:1-319~~ deleted 2026-08-05 |
| F73 ✅ | ~~DataGenService.java:34~~ fixed 2026-08-05 — `generate` javadoc now says the keys are contextClass URIs | F96 ✅ | ~~EPackageLuceneIndex.java:21-25~~ documented 2026-08-05 |
| F74 | ExpressionIndex.java:143-145 | F97 | EObjectStorageServiceCollector.java:64,124-136 vs BasicStorageRegistry.java:92-105 |
| F75 | DataGenResource.java:97-100 | F98 ✅ | ~~ApicurioStorageHelper.java:153-155~~ guarded 2026-08-05 |
| F76 | DataGenResource.java:137-143 | F101 ✅ | ~~PostReleaseActionService.java:46~~ deleted 2026-08-05 |
| F78 | ModelDocumentationConstants.java:21 | F102 | SchemaRegistryService.java:20-28 |
| — | | F107 ✅ | ~~WatcherConstants.java:18-23~~ documented 2026-08-05 |
| — | | F108 🟡 | JpaDataResource.java:71,75,109,114 (409 never produced) |
| — | | F109 | configs/watcher.json:5 (`/home/ilenia/tests/jpa/`) |
| — | | F111 ✅ | ~~README.md:85 vs cnf/build.bnd:57 + release.yml~~ fixed 2026-08-05 |

### Infos still open

F112 (DataGenResource.java:65-66 untargeted ResourceSet), F113 (rest.client.impl.spi export — deferred to next major by design), F114 (DynamicEPackageConfigurator.java:64 unconditional global-registry remove — client side is clean/opt-in), F115 (ModelAtlasExceptionMapper.java:112-114 env var per request), F116 (EcoreMessageBodyHandler.java:103,137,153,184 INFO logging + governance leftovers), F117 (schema.registry.impl in no bndrun), F118 (ObjectRegistryResource.java:535 / SchemaPackagesResource.java:537 — fingerprint got an API, contentHash didn't), F121 (UML2 vendoring provenance), F122 🟡 (only `update.sh` left at root — the two working docs moved to `docs/plans/` 2026-08-05).

## Systemic issues

1. **DS component classes exported as API** (F3–F7 + new F123): now six bundles. Unchanged across two reviews for the original five; the new git webhook bundle repeated the pattern — worth fixing as one sweep with the same refactor (interface in exported package or simply un-export; component private).
2. **`deactivate()` does not undo `activate()`**: the 2026-07-24 fixes held (storage base, Lucene, workflow executor, EMFFileWatcher), but F39/F48 remain and the new git backend re-introduced the class twice (F125 activation blocking, F126 shared-cache leak). The bug class keeps re-entering with new code — consider a PR-review checklist item.
3. **Sibling contract drift** (F25, F27, F31, F33, F34, F63, F65 + new F127): untouched cluster; the four REST format handlers and the storage backends still answer identical situations differently.
4. **Recurring-finding signal**: the parent-fallback duplication (F43) grew from 5 to 6 copy-paste sites since flagged — exactly the drift the finding predicted. Extracting the helper before more call sites appear is getting cheaper than the alternative.
5. **Release-readiness**: header enforcement is done and CI shape is now exemplary (pinned reusable workflows, secrets isolated from verify); what blocks a release is F11 (150 restricted Dash entries needing IP-Lab review — count is growing as deps are added) and F55 (demo model in production images); F14 (baselining) is deferred by design while the project stays prototype/snapshot-only; F111 (publishing story contradiction) was fixed 2026-08-05.

## Skipped / not reviewed

- **Fresh-review depth**: full reads for management.git, management.git.webhook.rest, runtime.config.docker.git + git bndruns; webhook model bundles and management.git.tests checked for headers/exports only (EMF-generated / test code per rules).
- **Carried-over re-verification** re-located and re-read every previous finding's code, but did not re-review unchanged bundles for brand-new findings (quick mode); the datagen/validation/jpa bundles in particular only got status checks.
- Uncommitted working-tree edits (two bndrun tweaks, .claude/settings.json, .gitignore) not in scope.
- `emf.py`-style sibling repos, generated EMF code (structural), `*.tests` bundles (structure) — per rule set.
