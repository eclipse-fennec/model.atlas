# Quality review — model.atlas — 2026-07-24

Mode: quick · Scope: whole repo · Rule sets: SOLID/OSGi + Eclipse Foundation (see skill references)
Branch reviewed: `snapshot` (the `management.git*` bundles are not on this branch — see Skipped).

## Summary

| Severity | License/IP | API hygiene | OSGi-DS / lifecycle | SOLID | Correctness / contract | Release-readiness | Docs / naming | Cleanliness | **Total** |
|---|---|---|---|---|---|---|---|---|---|
| **blocker** | 2 | 5 | 1 | 1 | – | – | – | – | **9** |
| **major** | – | – | 16 | 5 | 19 | 6 | – | – | **46** |
| **minor** | – | 1 | 10 | 6 | 19 | – | 12 | 8 | **56** |
| **info** | – | 2 | 1 | 1 | 2 | 1 | 1 | 3 | **11** |
| **Total** | 2 | 8 | 28 | 13 | 40 | 7 | 13 | 11 | **122** |

Overall: the workspace's bundle layout, api/impl splits, bnd manifests and package versioning are largely in good shape — most impl bundles are fully private and exported packages carry versions. The dominant problems are (1) a cluster of **DS component classes living in exported packages and registered as their own service type** (the impl *is* the API in five bundles), (2) **DS lifecycle gaps** — executors, Lucene writers, global-registry entries and shared-ResourceSet resources not released in `deactivate()`, plus a repeated unbind-by-key bug that breaks service replacement, (3) **license-header non-compliance** on ~52 files with header CI enforcement effectively disabled, and (4) missing Eclipse release-readiness plumbing (Dash/DEPENDENCIES, SECURITY.md, CODE_OF_CONDUCT.md, baselining). One component (`SchemaRegistryServiceImpl`) can never activate at all.

Blockers and majors are reported in full format below; minors and infos in compact tables. Occurrence lists consolidate the same root cause across files.

## Findings — blockers

### F1 · blocker · license · datagen / datagen.rest / datagen.model / datagen.example.model / data.atlas.jpa.rest / data.atlas.jpa.watcher / tests.common
- **Where:** org.eclipse.fennec.model.atlas.datagen/src/org/eclipse/fennec/model/atlas/datagen/DataGenService.java:1 (representative; ~48 files total)
- **What:** Shipped `.java` sources carry no EPL-2.0 license header at all: all hand-written sources in `datagen` (4), `datagen.rest` (1), the generated-but-unstamped `datagen.model` (22) and `datagen.example.model` (17) model files (empty `/* */` stub), plus `JpaDataResource.java`, `EormFileWatcher.java` and `tests.common/CommonTestAnnotations.java`.
- **Why it matters:** Eclipse IP policy requires the EPL-2.0 header on every shipped source file; these bundles are in the runtime images. The generated model files show the generator's header template is empty for those two genmodels.
- **Suggested fix:** Run `docker run -v $(pwd):/github/workspace apache/skywalking-eyes header fix` (or add headers manually); set the copyright text in the two `.genmodel` files so regeneration stamps headers.

### F2 · blocker · license · validation / validation.rest
- **Where:** org.eclipse.fennec.model.atlas.validation/src/org/eclipse/fennec/model/atlas/validation/ValidationService.java:1 (+ ValidationServiceImpl.java, ObjectValidationResource.java, ObjectBatchValidationResource.java)
- **What:** Four files carry the outdated **EPL-1.0** header ("Eclipse Public License v1.0 … epl-v10.html") instead of EPL-2.0.
- **Why it matters:** Wrong license declaration on shipped code; the project's declared license is EPL-2.0 (`.licenserc.yaml`, NOTICE.md).
- **Suggested fix:** Replace with the canonical EPL-2.0 header incl. `SPDX-License-Identifier: EPL-2.0`.

### F3 · blocker · api-hygiene · workflow
- **Where:** org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/package-info.java:14
- **What:** The exported package `org.eclipse.fennec.model.atlas.workflow` (`@Export @Version("1.0")`, confirmed in the generated manifest) contains three DS component classes registered as their own service type (`ResourceSetCollector`, `ScopeServiceCollector`, `RegistryServiceCollector`, e.g. ResourceSetCollector.java:46).
- **Why it matters:** The implementation classes are the API — consumers compile against components, blocking substitution and freezing implementation details into the exported package's semantic version.
- **Suggested fix:** Extract small collector interfaces into the exported package, register the services under them, move the `@Component` classes to the private `.impl` package.

### F4 · blocker · api-hygiene · management
- **Where:** org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/registry/BasicStorageRegistry.java:87
- **What:** DS component `BasicStorageRegistry` lives in the exported package `mgmt.registry` (`@Export @Version("1.0.0")`) alongside the API base classes.
- **Why it matters:** Exported package mixes extendable API (`AbstractRegistryHelper`, `BasicRegistryHelper`, `BasicEObjectRegistryService`) with a component impl; impl changes force API version bumps.
- **Suggested fix:** Move the component to a private `mgmt.registry.impl` package; keep only the base classes exported.

### F5 · blocker · api-hygiene · management
- **Where:** org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/collector/EObjectStorageServiceCollector.java:58
- **What:** Exported package `mgmt.collector` consists of a DS component registered as its own service type (`service = EObjectStorageServiceCollector.class`).
- **Why it matters:** Same impl-is-API problem as F3; every consumer of the collector binds to the concrete class.
- **Suggested fix:** Extract an interface (in `mgmt.api` or the collector package), register under it, make the component private.

### F6 · blocker · api-hygiene · readable.scope.collector
- **Where:** org.eclipse.fennec.model.atlas.readable.scope.collector/src/org/eclipse/fennec/model/atlas/readable/scope/collector/ReadableScopeCollector.java:29
- **What:** The bundle's only exported package is the DS component itself (`service = ReadableScopeCollector.class`, package `@Export`ed).
- **Why it matters:** Impl-is-API; consumers (REST layer) compile against the component class.
- **Suggested fix:** Split into an exported interface (natural home: `scope.api`) plus a private component.

### F7 · blocker · api-hygiene · model.documentation.provider
- **Where:** org.eclipse.fennec.model.atlas.model.documentation.provider/src/org/eclipse/fennec/model/atlas/model/documentation/provider/ModelDocumentationProvider.java:50
- **What:** The DS component is registered as its own service type and lives in the bundle's exported package (`package-info.java` `@Export`).
- **Why it matters:** Impl-is-API, no api/impl split at all in this bundle.
- **Suggested fix:** Extract a `ModelDocumentationService` interface into an exported api package; move the component to a private package.

### F8 · blocker · solid-dip · rest.uml
- **Where:** org.eclipse.fennec.model.atlas.rest.uml/src/org/eclipse/fennec/model/atlas/rest/uml/EnhancedUMLResourceFactoryImpl.java:21
- **What:** Compile-time import of a foreign internal package: `import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl` — the class subclasses UML2's internal factory only to attach service properties.
- **Why it matters:** Internal packages carry no API contract (exported `x-internal:=true`); any upstream UML2 refactor breaks this bundle, and it violates the never-import-internals rule.
- **Suggested fix:** Register the API-typed `UMLResource.Factory.INSTANCE` as the `Resource.Factory` service from `UMLConfigurationComponent` with the properties map there; delete the subclass.

### F9 · blocker · osgi-ds · schema.registry.impl
- **Where:** org.eclipse.fennec.model.atlas.schema.registry.impl/src/org/eclipse/fennec/model/atlas/schema/registry/impl/SchemaRegistryServiceImpl.java:45
- **What:** The `@Activate` constructor dereferences the **field-injected** `resourceSet` reference (`resourceSet.getEObject(...)`), but DS injects fields only after construction — the component throws NPE on every activation attempt and can never come up.
- **Why it matters:** The bundle's only component is dead on arrival (it is also wired into no bndrun — see F117); if anyone configures the `SchemaRegistryService` PID it fails silently at SCR level.
- **Suggested fix:** Take the `ResourceSet` as a constructor `@Reference` parameter (as `RegistryServiceImpl` in workflow does) — or retire the api+impl pair in favour of the workflow `RegistryService` (F117).

## Findings — majors

### Repo-level (release-readiness)

### F10 · major · release-readiness · repo
- **Where:** .github/workflows/license.yml:5
- **What:** The SkyWalking-Eyes license-header workflow triggers only on `workflow_dispatch` — header enforcement never runs on push/PR (which is how F1/F2 accumulated). It also uses unpinned `apache/skywalking-eyes@main` and `actions/checkout@v2`.
- **Why it matters:** Header compliance is unenforced; the guideline treats missing enforcement as major even when headers are fine — and here they are not.
- **Suggested fix:** Add `push`/`pull_request` triggers (copy from emf.osgi and adapt); pin the actions.

### F11 · major · release-readiness · repo
- **Where:** repo root (no `DEPENDENCIES`, no `tools/dash-licenses.*`, no `.github/workflows/dash-licenses.yml`)
- **What:** The Eclipse Dash IP-check tooling is entirely absent: no committed `DEPENDENCIES` file, no dash script, no workflow — although CONTRIBUTING.md (line 30, 131) instructs contributors to run Dash.
- **Why it matters:** No visibility on `restricted` third-party content before a release; a release-blocking IP gap. The vendored `org.eclipse.uml2.uml` source (F121) especially needs to be covered.
- **Suggested fix:** Copy `tools/dash-licenses.sh`/`.bat` and `dash-licenses.yml` from emf.osgi, generate `DEPENDENCIES` via `bnd repo deps`, review and commit it (PMI id `technology.fennec`).

### F12 · major · release-readiness · repo
- **Where:** repo root (missing `SECURITY.md`); CONTRIBUTING.md:46
- **What:** No `SECURITY.md`; CONTRIBUTING.md directs security reports to the `security@eclipse.org` mailing list instead.
- **Why it matters:** Handbook expects a per-repo `SECURITY.md` (GitHub advisories URL, supported-versions table); the mailing-list pointer is the pattern the fennec guide explicitly replaces.
- **Suggested fix:** Add `SECURITY.md` from emf.osgi (advisories URL for THIS repo, current versions table); point CONTRIBUTING.md at it.

### F13 · major · release-readiness · repo
- **Where:** repo root (missing `CODE_OF_CONDUCT.md`)
- **What:** No `CODE_OF_CONDUCT.md` at the repo root; the other five root documents exist and are adapted.
- **Why it matters:** Required root document (Eclipse Community CoC 2.0).
- **Suggested fix:** Copy from emf.osgi verbatim (it needs no adaptation).

### F14 · major · release-readiness · repo
- **Where:** cnf/build.bnd:54
- **What:** `fennec-baselining: false` although release `0.0.1` exists (git tag + published docker images with these bundles).
- **Why it matters:** Exported-package semantic versioning (§5 of the guidelines) is mechanically unenforced — nothing catches an API change without a version bump. Several exported packages sit at 1.0/1.0.0.
- **Suggested fix:** After the next release, publish the release OBR (`release-obr` orphan branch) and enable the baseline block against it.

### F55 · major · release-readiness · datagen.example.model / runtime
- **Where:** org.eclipse.fennec.model.atlas.runtime/modelatlas.runtime_base.bndrun:141 (runrequires at line 50)
- **What:** The **example** model bundle (Company/Person/Address demo EPackage + configurator components) is wired into the shared base bndrun, so it ships live in every runtime and Docker variant derived from `runtime_base`.
- **Why it matters:** Demo model content and its registered services are present in production images.
- **Suggested fix:** Remove it from `runtime_base` and include it only in a demo/local bndrun.

### Storage layer (management, management.file, management.apicurio, management.lucene)

### F15 · major · correctness · management
- **Where:** org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/storage/AbstractEObjectStorageService.java:702
- **What:** `delegateQueryToRegistry` calls `registryService.findByScopeAndStage(query.getStage(), query.getScope())` — scope and stage **swapped** against the API signature `findByScopeAndStage(String scope, String stage)` (EObjectRegistryService:210). Verified.
- **Why it matters:** Every backend inheriting this base silently returns wrong/empty results for scope+stage queries.
- **Suggested fix:** Swap the arguments.

### F16 · major · osgi-ds · management
- **Where:** org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/storage/AbstractEObjectStorageService.java:300
- **What:** `deactivateStorageService()` nulls fields but neither shuts down the cached thread pool created in `activateStorageService()` (line 197) nor calls `storageHelper.close()` (an `AutoCloseable`).
- **Why it matters:** Threads, Lucene handles and EMF resources leak on every component restart/config update.
- **Suggested fix:** Keep the `ExecutorService` in a field; call `shutdown()` and `storageHelper.close()` in deactivate.

### F17 · major · correctness · management
- **Where:** org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/storage/AbstractEObjectStorageService.java:625
- **What:** `updateStatus` saves metadata but never calls `registryService.updateCache(...)`, unlike `storeObject`/`updateMetadata`.
- **Why it matters:** Registry status indexes go stale after every status transition; status-filtered queries return outdated results.
- **Suggested fix:** Call `registryService.updateCache(EcoreUtil.copy(metadata))` after `saveMetadata`, mirroring `updateMetadata`.

### F18 · major · solid-lsp · management
- **Where:** org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/storage/AbstractEObjectStorageService.java:510
- **What:** Promise-returning `queryObjects` throws `IllegalArgumentException`/`IllegalStateException` synchronously instead of failing the returned Promise.
- **Why it matters:** Breaks the async error contract for all backends; callers composing promises never see the failure path they handle.
- **Suggested fix:** Return `promiseFactory.failed(...)` for both error paths.

### F19 · major · osgi-ds · management
- **Where:** org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/storage/AbstractStorageHelper.java:69
- **What:** The constructor puts a factory into the JVM-global `ConversionDelegate.Factory.Registry.INSTANCE` and `close()` never removes it.
- **Why it matters:** Global-registry leak across component restarts — the known EMFFileWatcher bug class.
- **Suggested fix:** Remember the key and `remove()` it in `close()` (or register once statically).

### F20 · major · osgi-ds · management
- **Where:** org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/storage/AbstractStorageHelper.java:445
- **What:** `close()` unloads and clears **all** resources of the injected `(emf.name=management)` ResourceSet — a shared OSGi service the helper does not own.
- **Why it matters:** One storage service's shutdown wipes the resources of every other component sharing that ResourceSet.
- **Suggested fix:** Track and remove only resources this helper created.

### F21 · major · osgi-ds · management
- **Where:** org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/registry/BasicEObjectRegistryService.java:198
- **What:** The constructor (on the DS activation path of storage services) synchronously loads all metadata from storage while the adjacent comment claims "Initialize cache asynchronously".
- **Why it matters:** Blocking I/O in activation; slow storage delays SCR and every dependent component.
- **Suggested fix:** Run `initializeCache` via Promise/executor (the lazy `ensureCacheInitialized` path already exists); fix the comment.

### F22 · major · osgi-ds · management / workflow / readable.scope.collector / data.atlas.jpa.rest
- **Where:** org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/registry/BasicStorageRegistry.java:114 (also workflow/ScopeServiceCollector.java:89, workflow/RegistryServiceCollector.java:62, readable.scope.collector/ReadableScopeCollector.java:62, data.atlas.jpa.rest/JpaDataResourceFilter.java:80 — three unbind methods)
- **What:** Dynamic MULTIPLE unbind methods remove map entries **by key only**, so DS's bind-new-then-unbind-old replacement order deletes the freshly bound replacement service. Six sites across four bundles.
- **Why it matters:** After any service replacement (config update, bundle refresh) lookups fail until another rebind; in the JPA filter all requests for the folder 400.
- **Suggested fix:** Use the two-arg idiom `map.remove(key, service)` everywhere (`ResourceSetCollector.java:104` and `EObjectStorageServiceCollector` already do it right).

### F23 · major · solid-lsp · management
- **Where:** org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/registry/BasicRegistryHelper.java:90
- **What:** `searchObjectIds` ignores the query and returns ALL object ids ("query parsing not implemented"), drifting from the documented contract that sibling `LuceneRegistryHelper` honors.
- **Why it matters:** Callers get every object regardless of filter — silently wrong results rather than an explicit unsupported-operation.
- **Suggested fix:** Implement minimal field:value filtering or throw documented `UnsupportedOperationException` — never silently return everything.

### F24 · major · debug-leftover · management.apicurio
- **Where:** org.eclipse.fennec.model.atlas.management.apicurio/src/org/eclipse/fennec/model/atlas/management/apicurio/ApicurioStorageHelper.java:477
- **What:** Every store double-serializes and dumps the full artifact payload to stdout (`apicurioResource.save(System.out, options)`) before the real save. Verified.
- **Why it matters:** Log flooding and payload leakage into container logs on every write; measurable overhead.
- **Suggested fix:** Delete the line.

### F25 · major · osgi-ds/lsp · management.apicurio
- **Where:** org.eclipse.fennec.model.atlas.management.apicurio/src/org/eclipse/fennec/model/atlas/management/apicurio/ApicurioStorageHelper.java:79
- **What:** Constructor does synchronous HTTP scans of the whole Apicurio registry during `@Activate` and swallows failure with `printStackTrace()` — the service activates with a silently empty registry, while sibling `FileStorageHelper` fails activation on the same error (LSP drift).
- **Why it matters:** A down registry at startup yields a healthy-looking but empty storage service; siblings behave differently for the same fault.
- **Suggested fix:** Log properly and either propagate (matching file) or defer the scan off the activation thread.

### F26 · major · robustness · management.apicurio
- **Where:** org.eclipse.fennec.model.atlas.management.apicurio/src/org/eclipse/fennec/model/atlas/management/apicurio/ApicurioStorageHelper.java:283
- **What:** `loadAllStoredMetadata` NPEs on a null groups response, throws `IllegalStateException` for any foreign groupId, and `createGroupId` (line 317) joins scope-registry-stage with `-` so any name containing a hyphen produces unparseable group ids.
- **Why it matters:** One foreign or hyphenated group breaks the whole metadata load.
- **Suggested fix:** Null-check the response, skip-and-warn on foreign ids, use a separator that cannot occur in names (or Apicurio labels).

### F27 · major · solid-lsp · management.apicurio
- **Where:** org.eclipse.fennec.model.atlas.management.apicurio/src/org/eclipse/fennec/model/atlas/management/apicurio/ApicurioStorageHelper.java:183
- **What:** `deleteObject` returns true only if object AND metadata deletes succeed (short-circuiting the metadata delete), while `FileStorageHelper` returns true if EITHER was deleted.
- **Why it matters:** Contract drift desyncs the registry-cache removal in `AbstractEObjectStorageService.deleteObject` between backends.
- **Suggested fix:** Attempt both deletes unconditionally; return true if either succeeded, matching file semantics.

### F28 · major · robustness/security · management.lucene
- **Where:** org.eclipse.fennec.model.atlas.management.lucene/src/org/eclipse/fennec/model/atlas/management/lucene/registry/LuceneRegistryHelper.java:691 (+ LuceneEObjectRegistryService.java:438)
- **What:** `parseQuery` falls back to `MatchAllDocsQuery` on `ParseException`, and callers concatenate unescaped scope/stage/name values into query strings.
- **Why it matters:** One special character in a name makes a scoped query return EVERY object across all scopes — a cross-tenant data exposure in multi-scope deployments.
- **Suggested fix:** Return a match-none query (or rethrow) on parse failure; build `BooleanQuery`/`TermQuery` from values (or `QueryParser.escape`) instead of string concatenation.

### F29 · major · osgi-ds · management.lucene
- **Where:** org.eclipse.fennec.model.atlas.management.lucene/src/org/eclipse/fennec/model/atlas/management/lucene/epackage/impl/EPackageLuceneIndexImpl.java:230
- **What:** The component opens IndexWriter/SearcherManager/Directory in `@Activate` but has no `@Deactivate` — `close()` exists and DS never calls it. Verified (only `@Activate` present).
- **Why it matters:** Leaks the Lucene `write.lock` and file handles on every reactivation; subsequent activations can fail to acquire the lock.
- **Suggested fix:** Annotate `close()` with `@Deactivate`.

### REST server layer

### F30 · major · exception-discipline · rest.application
- **Where:** org.eclipse.fennec.model.atlas.rest.application/src/org/eclipse/fennec/model/atlas/rest/application/resource/ObjectRegistryResource.java:129 (13 sites; same pattern in SchemaPackagesResource)
- **What:** Broad per-endpoint `catch (Exception e)` blocks return raw `e.getMessage()` to clients — bypassing `ModelAtlasExceptionMapper`'s no-internal-leak hardening — and swallow `InterruptedException` from `Promise.getValue()` without re-interrupt.
- **Why it matters:** Internal details reach clients; interrupted threads lose their status; the central sanitized 500 path is dead code for these endpoints.
- **Suggested fix:** Drop the catch-alls (keep only `IllegalArgumentException`→400); let the ExceptionMapper handle the rest.

### F31 · major · lsp · rest.application
- **Where:** org.eclipse.fennec.model.atlas.rest.application/src/org/eclipse/fennec/model/atlas/rest/application/resource/ObjectRegistryResource.java:589
- **What:** `deleteObject` returns 204 on success although its own `@ApiResponse` documents 200 and uses 204 for "not found"; sibling `SchemaPackagesResource.deletePackage` returns 200 for the same operation.
- **Why it matters:** Client cannot distinguish success from not-found; sibling endpoints disagree.
- **Suggested fix:** Return 200 like `deletePackage` (or fix the OpenAPI doc and use 404 for not-found).

### F32 · major · correctness · rest.application / management.lucene
- **Where:** org.eclipse.fennec.model.atlas.rest.application/src/org/eclipse/fennec/model/atlas/rest/application/resource/SchemaPackagesResource.java:608
- **What:** Search-index maintenance is objectId-keyed only: deleting a package from one stage removes its index doc for every scope/stage (`EPackageLuceneIndexImpl` deletes by `Term(FIELD_OBJECT_ID,…)`), and `transitionPackage` never reindexes, leaving stale stage fields.
- **Why it matters:** Stage-filtered search returns wrong results after deletes and transitions.
- **Suggested fix:** Key/remove index docs by (scope, stage, objectId); reindex after successful transition.

### F33 · major · lsp · rest.jsonschema / rest.xsdschema / rest.uml
- **Where:** org.eclipse.fennec.model.atlas.rest.jsonschema/src/org/eclipse/fennec/model/atlas/rest/jsonschema/JsonSchemaMessageBodyReaderWriter.java:65 (also xsdschema/XSDSchemaMessageBodyReaderWriter.java:68, uml/UMLMessageBodyReaderWriter.java:58)
- **What:** `isWriteable`/`isReadable` compare `mediaType.toString()` for exact equality, silently rejecting parameterized media types (`…;charset=UTF-8`) that `@Consumes` accepts — sibling ecore.xmi correctly compares type/subtype.
- **Why it matters:** Clients sending a charset parameter get no reader/writer (406/500) on three of four format endpoints; sibling drift.
- **Suggested fix:** Compare `getType()`/`getSubtype()` (or `MediaType.isCompatible`) in all three.

### F34 · major · lsp · rest.ecore.xmi
- **Where:** org.eclipse.fennec.model.atlas.rest.ecore.xmi/src/org/eclipse/fennec/model/atlas/rest/ecore/xmi/EcoreMessageBodyHandler.java:131
- **What:** Empty/unparseable payload throws `IOException` (→500) here while the jsonschema (92), uml (87) and xsdschema (99) siblings return null for the same condition.
- **Why it matters:** Same error class, four different behaviors across the sibling format handlers.
- **Suggested fix:** Agree one contract (prefer 400 via `BadRequestException`) and apply it in all four handlers.

### F35 · major · exception-discipline · rest.common
- **Where:** org.eclipse.fennec.model.atlas.rest.common/src/org/eclipse/fennec/model/atlas/rest/common/ResourceAttacherHelper.java:37
- **What:** Exported API helper (used by validation.rest) swallows `IOException` with `printStackTrace`, and `resource.save(null)` on a relative `<uuid>.xmi` URI can write files into the server working directory.
- **Why it matters:** Silent failures plus unintended filesystem writes from a shared API helper.
- **Suggested fix:** Rethrow unchecked (or drop the save if only attachment is intended); log via a real logger.

### Client

### F36 · major · osgi-ds · rest.client.osgi
- **Where:** org.eclipse.fennec.model.atlas.rest.client.osgi/src/org/eclipse/fennec/model/atlas/rest/client/osgi/AtlasClientComponent.java:283
- **What:** `publishScopeServices` calls `client.listScopeNames()` (network I/O) inside `@Activate` in every mode when `scope.allow.list` is empty — including LAZY ("nothing fetched up front") — and a `TransportException` fails activation even when `mode.strict=false`, contradicting its own javadoc and `EagerPrefetch.failOrSkip`'s non-strict contract.
- **Why it matters:** A briefly unreachable Atlas at startup takes the whole client component down in the mode that promises tolerance.
- **Suggested fix:** In non-strict mode catch `TransportException`, log, skip scope-service publication (retry on config update); rethrow only in strict mode.

### emf.common / healthcheck / bootstrap / model.documentation.provider

### F37 · major · correctness · model.documentation.provider
- **Where:** org.eclipse.fennec.model.atlas.model.documentation.provider/src/org/eclipse/fennec/model/atlas/model/documentation/provider/ModelDocumentationProvider.java:81
- **What:** Change detection persists `EPackage.hashCode()` (identity hash) to disk and compares across restarts — random per JVM run — and NPEs on an empty hash file (`readLine()` null).
- **Why it matters:** Every restart regenerates all documentation ("changed" always true); empty file crashes the component.
- **Suggested fix:** Fingerprint serialized content (e.g. SHA-256 of the XMI bytes); null-guard the read.

### F38 · major · solid-ocp · model.documentation.provider
- **Where:** org.eclipse.fennec.model.atlas.model.documentation.provider/src/org/eclipse/fennec/model/atlas/model/documentation/provider/ModelDocumentationProvider.java:56
- **What:** Format dispatch is closed: two `@Reference`s target concrete components by `component.name` and a switch (lines 125–135) routes between them — every new documentation format edits this class.
- **Why it matters:** Bypasses the whiteboard pattern the workspace uses everywhere else for format extensibility.
- **Suggested fix:** MULTIPLE dynamic reference to `EcoreToDocumentationService` selected by a format service property.

### F39 · major · osgi-ds · bootstrap
- **Where:** org.eclipse.fennec.model.atlas.bootstrap/src/org/eclipse/fennec/model/atlas/bootstrap/InitialModelLoader.java:271
- **What:** `prepareEPackages` seeds the global `EPackageRegistryImpl.INSTANCE` per resource before `failOnDuplicate` (line 306/313) can still throw for a later resource/subpackage; the exception escapes the `@Activate` constructor so `@Deactivate` never runs and the already-seeded nsURIs leak permanently. Verified.
- **Why it matters:** The known global-registry leak bug class: a failed bootstrap poisons the global registry for the JVM lifetime.
- **Suggested fix:** Validate all packages first, or catch in `loadInitial` and remove already-seeded nsURIs from both registries before rethrowing.

### F40 · major · osgi-ds · healthcheck
- **Where:** org.eclipse.fennec.model.atlas.healthcheck/src/org/eclipse/fennec/model/atlas/healthcheck/ScopesHealthCheck.java:40
- **What:** The MULTIPLE field reference defaults to static/reluctant, so `ScopeService`s registered after activation are never bound — readiness keeps reporting the stale set, or "No ScopeServices found" forever if it activated with zero.
- **Why it matters:** Kubernetes readiness can be permanently wrong in the common late-configuration case.
- **Suggested fix:** `policy = DYNAMIC` with a `volatile List`, or `ServiceScope.PROTOTYPE` like `EMFRegistryHealthCheck`.

### workflow

### F41 · major · osgi-ds · workflow
- **Where:** org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/RegistryServiceImpl.java:82
- **What:** Each factory instance creates `new PromiseFactory(Executors.newCachedThreadPool())` and the class has no `@Deactivate` — the executor is never shut down.
- **Why it matters:** Thread leak on every config update/reactivation of every registry.
- **Suggested fix:** Keep the ExecutorService in a field; add `@Deactivate` calling `shutdown()`.

### F42 · major · osgi-ds · workflow
- **Where:** org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/RegistryServiceImpl.java:581
- **What:** A stage→storageType mapping whose storage type matches no bound service is silently skipped at activation (`if (storage != null)`), deferring the misconfiguration to an NPE on first use (`uploadToStage`:167).
- **Why it matters:** Config errors surface as runtime NPEs far from the cause instead of failing fast.
- **Suggested fix:** Throw `IllegalArgumentException` from the constructor when a configured storage type has no matching service.

### F43 · major · solid-srp · workflow
- **Where:** org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/ScopeServiceImpl.java:115 (also 144, 238, 260, 353)
- **What:** The atlas/SCHEMA parent-fallback special-casing is copy-pasted five times.
- **Why it matters:** Any change to inheritance rules must be edited in five places; they will drift.
- **Suggested fix:** Extract one private `resolveFromParent(...)` helper (or strategy) used by all five sites.

### F44 · major · solid-lsp · workflow
- **Where:** org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/ScopeServiceImpl.java:430 (also :438, AtlasScopeService.java:289, :297)
- **What:** Both production impls of `ReadableScopeService.registryView` throw `UnsupportedOperationException` ("not yet implemented (P6-4)") although the API documents no optionality.
- **Why it matters:** Consumers of the read-only contract get a runtime surprise; unlike the documented read-only-backend idiom, this is undocumented.
- **Suggested fix:** Implement (planned P6-4) or interim-document the operation as unsupported on the API.

### F45 · major · solid-srp · workflow
- **Where:** org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/registration/DynamicEPackageRegistrationService.java:435 (REMOVE variant :464)
- **What:** The generic EPackage registration service hardcodes one downstream consumer's config topic (`configuration/ADD/SouthboundMappingService`) and assumes an EClass named `<modelName>Sensor`.
- **Why it matters:** Application-specific southbound-gateway knowledge baked into the workflow bundle; unrelated deployments emit bogus events.
- **Suggested fix:** Move the SouthboundMapping event emission into a separate `StageActionService`/event listener owned by the consuming application.

### F46 · major · correctness/lsp · emf.common
- **Where:** org.eclipse.fennec.model.atlas.emf.common/src/org/eclipse/fennec/model/atlas/emf/common/ecore/EClassResolvingDynamicEFactory.java:40
- **What:** Map-entry EClasses get the inherited `DynamicEObjectImpl.BasicEMapEntry` (the referenced `EClassResolvingDynamicEObject.BasicEMapEntry` doesn't exist as an override), which lacks the proxy-resolving `eClass()` override.
- **Why it matters:** The reload-resilience this factory exists for silently does not apply to EMap entries.
- **Suggested fix:** Add a nested `BasicEMapEntry` subclass overriding `eClass()` with the same `EcoreUtil.resolve` logic.

### data.atlas (JPA pipeline)

### F47 · major · osgi-ds · data.atlas.jpa.watcher
- **Where:** org.eclipse.fennec.data.atlas.jpa.watcher/src/org/eclipse/fennec/data/atlas/jpa/watcher/EormFileWatcher.java:165
- **What:** `register()` mutates the plain `HashMap` field `registrations` without holding `lock` when called from `load()` (watcher thread), while ServiceTracker callbacks and `deactivate()` mutate the same map under `lock`.
- **Why it matters:** Unsynchronized concurrent mutation of component state; lost updates/corruption under concurrent file events.
- **Suggested fix:** Take `lock` around the register-immediately path (or make the map concurrent and the operations atomic).

### F48 · major · osgi-ds · data.atlas.jpa.watcher
- **Where:** org.eclipse.fennec.data.atlas.jpa.watcher/src/org/eclipse/fennec/data/atlas/jpa/watcher/EormFileWatcher.java:93
- **What:** `deactivate()` closes trackers but never detaches Resources parked in `awaitEPackageAndRegister` (EPackage never arrived), leaking loaded Resources into the shared `(emf.name=eorm)` ResourceSet across restarts.
- **Why it matters:** The known shared-registry leak bug class, on a shared singleton ResourceSet.
- **Suggested fix:** Remember the pending Resource per URI and `detach()` each in `deactivate()`/`unload()`.

### F49 · major · osgi-ds · data.atlas.jpa.watcher
- **Where:** org.eclipse.fennec.data.atlas.jpa.watcher/src/org/eclipse/fennec/data/atlas/jpa/watcher/DataFolderWatcher.java:191
- **What:** `setupPipeline()` creates ConfigAdmin configurations sequentially and the catch only logs — a mid-sequence failure leaves earlier configs alive with `emfWatcherConfig == null`; the next event re-runs setup with a fresh `matcherKey`, orphaning the previously created configurations (never deleted by deactivate).
- **Why it matters:** Orphaned DataSource/registry/resourceSetFactory configs accumulate and keep live components bound to dead pipelines.
- **Suggested fix:** On failure delete configs created so far (reuse the deactivate cleanup); use a stable matcherKey per folder.

### F50 · major · correctness · data.atlas.jpa.watcher
- **Where:** org.eclipse.fennec.data.atlas.jpa.watcher/src/org/eclipse/fennec/data/atlas/jpa/watcher/DataFolderWatcher.java:120
- **What:** The documented deferred pipeline start can never trigger: the component watches the unit folder with `recursive = false` (line 61) but the `.eorm` file it waits for lives in the `mapping/` subfolder (line 106), so `handlePathEvent` never receives the `.eorm` create event. Verified.
- **Why it matters:** "Pipeline will start when a mapping is added" only works at activation time — this is very likely the root cause of the known never-delivered-event mystery in the JPA importer tests. The class javadoc also still says ".jpamapping".
- **Suggested fix:** Set `recursive = true` (or watch `basePath/mapping`); update the javadoc.

### F51 · major · osgi-ds · data.atlas.jpa.rest
- **Where:** org.eclipse.fennec.data.atlas.jpa.rest/src/org/eclipse/fennec/data/atlas/jpa/rest/JpaDataResourceFilter.java:130
- **What:** The filter is whiteboard-global (no application-select/name binding) but assumes `rootFolderName`/`eClassName` path params exist: absent params mean `containsKey(null)` → NPE → 500, and even the bundle's own `/hello` endpoint is rejected 400 at the line-173 EClassifier check.
- **Why it matters:** The filter breaks unrelated endpoints of any application it happens to match.
- **Suggested fix:** No-op when the params are absent, or scope the filter via `@NameBinding`/`osgi.jakartars.application.select`.

### validation / datagen

### F52 · major · api-contract · validation.rest
- **Where:** org.eclipse.fennec.model.atlas.validation.rest/src/org/eclipse/fennec/model/atlas/validation/rest/ObjectValidationResource.java:71 (same in ObjectBatchValidationResource.java:68)
- **What:** The URL template `/{scopeName}/{stageName}/validate` promises stage-aware validation but the injected `stageName` is never read — the stage segment is silently ignored in both resources. Verified.
- **Why it matters:** Clients validating against a specific stage actually validate against whatever the service defaults to — silently wrong results. (Matches the already-decided, not-yet-implemented scope/stage refactor.)
- **Suggested fix:** Pass `stageName` through the ValidationService/scope resolution, or drop the segment until the refactor lands.

### F53 · major · solid-ocp · datagen
- **Where:** org.eclipse.fennec.model.atlas.datagen/src/org/eclipse/fennec/model/atlas/datagen/impl/DataGenServiceImpl.java:160
- **What:** The model's designed extension point `DataGenConfig.getCustomGenerators()` is never read — `resolveExpression` consults only the hardcoded static `KEY_TO_EXPRESSION` map, Lucene fuzzy match and naming convention.
- **Why it matters:** Users cannot add generators without editing `GeneratorKeyMapper`; the model advertises an extension mechanism that does nothing.
- **Suggested fix:** Resolve against `config.getCustomGenerators()` first (or remove the feature from the model).

### F54 · major · solid-dip/config · datagen.rest
- **Where:** org.eclipse.fennec.model.atlas.datagen.rest/src/org/eclipse/fennec/model/atlas/datagen/rest/DataGenResource.java:46
- **What:** The `/datagen/{objectId}` endpoint hardcodes scope `"jena"` and registry `"DataGen"` — it works only for a scope literally named jena, although "jena" is just a scope name, and every other resource takes `{scopeName}` as a path param. Verified.
- **Why it matters:** The endpoint is dead for every other tenant/deployment; violates the multi-tenant scope design.
- **Suggested fix:** Move under `/{scopeName}/…` and resolve the registry via `RegistryType` lookup or config, mirroring the validation resources.

## Findings — minors (compact)

| # | Bundle | Where | Category | What → fix |
|---|---|---|---|---|
| F56 | several | org.eclipse.fennec.data.atlas.epackage.watcher/src/.../EMFFileWatcher.java:197 (anchor) | naming | Misspelled identifiers and user-facing strings across ≥5 bundles: `scheduleDelaied`/`DelaiedTimerTask`, `folderToResrouceSetFactoryMap` (JpaDataResourceFilter:67), "EPckage" (:176), "Obejct" (ObjectRegistryResource:201), "Regsitries" (ScopesHealthCheck:60), "Upload Operation **now** allowed" (AtlasSchemaRegistryService:97,141,151,189), "Satge" (RegistryServiceImpl:617), "meaningflu" (ScopeServiceConfig:27), "tatrget" (RegistryServiceConfig:45) → rename/correct |
| F57 | rest.application | resource/SchemaPackagesResource.java:89 | srp/dry | 985-line resource mixes HTTP with version parsing (871–945), objectId encoding, index maintenance; duplicates `checkIfMatch`/`getResolvedMediaType` with ObjectRegistryResource:681–707 → extract shared helpers into rest.common |
| F58 | rest.application | resource/SchemaPackagesResource.java:828 | correctness | `nsUri.getBytes()` uses platform default charset for Base64URL objectIds → `StandardCharsets.UTF_8` |
| F59 | rest.application | resource/ObjectRegistryResource.java:201 | http | Six 204 responses built with entity bodies that HTTP silently drops → bare 204 or 404-with-body |
| F60 | rest.application | resource/ObjectRegistryResource.java:698 | lsp | Unknown scope → null → NPE → 500, while SchemaPackagesResource:818 throws 400 for the same case → same 400 WAE |
| F61 | rest.application / datagen.rest / data.atlas.jpa.rest | resource/SchemaPackagesResource.java:109 (also OpenApiResource.java:143, JpaDataResource.java:43) | api-hygiene | Leftover `hello`/`test` debug endpoints ship on production paths (OpenApi one leaks class name + identityHashCode) → delete |
| F62 | rest.filter | ModelAtlasRequestFilter.java:165 | correctness | `path.contains("/schema")` substring heuristic also matches segments merely starting with "schema" → match exact path segment |
| F63 | rest.uml / rest.xsdschema | UMLMessageBodyReaderWriter.java:92 (also XSDSchemaMessageBodyReaderWriter.java:107) | robustness | Unguarded `values.iterator().next()` after conversion → NoSuchElementException → 500 on empty result → guard with 400/null |
| F64 | rest.ecore.xmi | EcoreMessageBodyHandler.java:166 | lsp | writeTo leaves serialized EPackage attached to the request-scoped resource; all three siblings `getContents().clear()` after save → add the clear |
| F65 | rest.uml | UMLMessageBodyReaderWriter.java:67 | lsp | Content-Disposition drift: ecore.xmi/xsdschema set attachment filename, uml computes but never sets, jsonschema none → unify |
| F66 | rest.common | ResourceAttacherHelper.java:29 | javadoc | Exported API class has only @author/@since → document contract/side effects/thread-safety |
| F67 | validation.rest | ObjectBatchValidationResource.java:154 | dry | `checkContentType()` + 4-branch exception mapping duplicated verbatim across/within both resources → shared helper or ExceptionMapper |
| F68 | validation | impl/ValidationServiceImpl.java:398 | correctness | Format string has one `%s`, two args — parse-error message silently dropped (also :408) → add second `%s` |
| F69 | validation | impl/ValidationServiceImpl.java:168 | srp | ~76-line `compute()` fuses OCL evaluation and reflective `eInvoke` marshalling behind one if/else → split into two methods |
| F70 | validation | impl/ValidationServiceImpl.java:392 | dry | Two `evaluateConstraint` overloads copy-pasted except `OclContext.of(...)` arg → delegate |
| F71 | validation | ValidationService.java:33 | javadoc | Exported API: no per-method docs; `filterBatch` null return (→ HTTP 204) is an undocumented contract → add @param/@return/@throws |
| F72 | validation | ValidationHelper.java:14 | javadoc/api | Exported instantiable static-utility class, no javadoc → document, `final` + private ctor |
| F73 | datagen | DataGenService.java:21 | javadoc | `generate` javadoc says keys are "EClass name" but they are `nsURI#//ClassName` contextClass URIs → correct doc |
| F74 | datagen | impl/ExpressionIndex.java:130 | error-handling | `findExpression` swallows IOException, returns null (indistinguishable from no-match) → log or rethrow |
| F75 | datagen.rest | DataGenResource.java:86 | api-contract | Unknown objectId → 204 instead of 404 (siblings use 404) → return 404 |
| F76 | datagen.rest | DataGenResource.java:130 | correctness | Checks for `#` but blindly skips 3 chars assuming `#//` → verify separator before slicing |
| F77 | datagen.example.model | bnd.bnd:5 | config-hygiene | Comment claims codegen disabled to protect manual impls, but `-generate:` is active and no `@generated NOT` markers exist → delete stale comment or disable generate |
| F78 | model.documentation.provider | ModelDocumentationConstants.java:21 | naming/api | Constant-interface antipattern in exported API → final class + private ctor |
| F79 | model.documentation.provider | ModelDocumentationProvider.java:45 | javadoc | Exported type: empty class javadoc, no docs on ~10 public methods (incl. null-on-IOException) → add |
| F80 | bootstrap | InitialModelLoader.java:119 | osgi-ds | `@Activate` ctor synchronously walks + loads all models, blocking SCR (bounded boot mount, but unbounded on large volumes) → go async via Promise |
| F81 | healthcheck | MediaTypesHealthCheck.java:34 | osgi-ds | Mandatory static reference makes the readiness check vanish (component deactivates) instead of reporting critical; null-guard at :41 is dead code → OPTIONAL + PROTOTYPE like EMFRegistryHealthCheck |
| F82 | mediatypes.impl | SupportedMediatypesImpl.java:39 | solid-ocp | Three media types hardcoded on top of the dynamic scan → register upstream content-types or config property |
| F83 | mediatypes.impl | SupportedMediatypesImpl.java:34 | concurrency | Updater synchronizes on the list, reader copies without the lock — the sync protects nothing → build new immutable list, assign to volatile field |
| F84 | emf.common | configurator/DynamicEPackageConfigurator.java:22 | javadoc | Exported API class: no class javadoc explaining the global-INSTANCE mirroring side effect → document |
| F85 | rest.client.osgi | AtlasEPackageRegistryConfigurator.java:123 | robustness | ConfigAdmin `scope`/`stage` values concatenated unescaped into LDAP target filters (also :92) → escape per RFC 1960 |
| F86 | rest.client.osgi | RemoteEPackagePublisher.java:209 | osgi-ds | No closed state: LAZY resolution racing `deactivate()` can re-register the configurator/EPackage/EFactory trio after `unpublishAll()` → volatile closed flag checked under the per-nsURI lock |
| F87 | rest.client.osgi | AtlasScopedFetchOnMissRegistry.java:94 | solid-lsp | `getEFactory` inverts `getEPackage`'s resolution order (parent first), can pair the parent's factory with a different EPackage instance → derive from own `getEPackage` first |
| F88 | rest.client.impl | ModelAtlasClientImpl.java:147 | resource-lifecycle | Every `newResourceSet()` registers a drift listener and drops the unsubscribe handle — unbounded accumulation until `close()` → weak refs or closeable handle |
| F89 | rest.client.api | ClientConfiguration.java:383 | javadoc | ~28 public Builder setters (383–521) have no javadoc, unlike the rest of the API bundle → add one-liners referencing the dotted property names |
| F90 | management | registry/BasicEObjectRegistryService.java:742 | consistency | Six newer finders skip `ensureCacheInitialized()` unlike all older finders — silently empty after a failed initial load → add the call |
| F91 | workflow / data.atlas.jpa.watcher / management | impl/StorageRegistryServiceImpl.java:1 (anchor) | dead-code | Fully commented-out code shipped: 319-line StorageRegistryServiceImpl (only "impl" of exported EObjectWorkflowService), five //-prefixed files in jpa.watcher (DataFolderWatcherOld, JpaCsvDataImporter, JpaModelSetup, JpaPersistenceUnitConfigurator, StandaloneJpaMappingFileWatcher), 40-line initializeCache block (BasicEObjectRegistryService:557), ModelInitializer/airquality blocks (DynamicEPackageRegistrationService:91, 286) → delete; git history preserves them |
| F92 | management | storage/AbstractEObjectStorageService.java:808 | dead-code | Properties-merge branch self-contradictory (null-check then deref; both branches identical) → collapse to one `putAll` |
| F93 | management.file | FileStorageHelper.java:54 | exception-hygiene | IllegalStateException wraps activation IOException without the cause → chain `e` |
| F94 | management.file | EObjectFileStorageService.java:141 | shadowing | Private `storageType` hides the inherited protected field also assigned by the base activate → delete the private field |
| F95 | management.lucene | service/LuceneEObjectRegistryService.java:159 | concurrency | `totalUpdates`/`totalRemovals` plain longs incremented from concurrent threads → AtomicLong |
| F96 | management.lucene | epackage/EPackageLuceneIndex.java:25 | javadoc | Exported API interface: empty javadoc → document contract/lifecycle/thread-safety |
| F97 | management | collector/EObjectStorageServiceCollector.java:64 | solid-srp | Collector duplicates BasicStorageRegistry's job (both track EObjectStorageService by storage.type) → keep one, deprecate the other |
| F98 | management.apicurio | ApicurioStorageHelper.java:108 | robustness | Unchecked `(EClass)` cast of `resourceSet.getEObject(...)` — unresolvable objectType label NPEs later (:328) → guard for null |
| F99 | workflow | impl/WorkflowServiceHelper.java:45 | osgi-ds | InterruptedException wrapped without restoring interrupt status → `Thread.currentThread().interrupt()` first |
| F100 | workflow | impl/ScopeServiceImpl.java:57 | concurrency | Non-volatile fields written by dynamic bind/unbind, read from other threads (`scopeObject`; also `staticPackageRegistry` in AtlasSchemaRegistryService:60) → volatile |
| F101 | workflow | PostReleaseActionService.java:46 | api-hygiene | Exported legacy interface with no production impl/caller coexists undeprecated with replacement StageActionService → @Deprecated or remove pre-release |
| F102 | schema.registry.api | SchemaRegistryService.java:20 | javadoc | Exported API interface + all 4 methods undocumented → add contract docs |
| F103 | data.atlas.epackage.watcher | EMFFileWatcher.java:154 | osgi-ds | `deactivate()` cancels the timer but an already-running DelaiedTimerTask can still `registerService` on the dead context → closed flag under the lock |
| F104 | data.atlas.epackage.watcher | EMFFileWatcher.java:183 | osgi-ds | ENTRY_DELETE doesn't remove the path from `pendingUris` — CREATE+DELETE inside one debounce window still loads the deleted file → remove in the DELETE branch |
| F105 | data.atlas.jpa.watcher | EormFileWatcher.java:50 | robustness | Watcher pattern `.*.eorm` (unescaped dot) + `endsWith("eorm")` without the dot let names like `fooXeorm` pass → `.*\.eorm` and reuse `isJpaMappingFile` |
| F106 | data.atlas.jpa.watcher | EormFileWatcher.java:96 | cleanliness | Debug `System.out.println` in production components (96, 98, 141, 271, 281, 295; DataFolderWatcher:237 prints "Deleted config" before deleting) → remove or Logger |
| F107 | data.atlas.jpa.watcher | api/WatcherConstants.java:23 | javadoc | Exported API constants interface: empty javadoc → document each PID/property contract |
| F108 | data.atlas.jpa.rest | JpaDataResource.java:43 | api-contract | `ePackageUri` declared but unused; documented 409 "Multiple EntityMappings match" is never produced → align @ApiResponse with behavior |
| F109 | data.atlas.jpa.config.local | configs/watcher.json:5 | configuration | Hardcoded user-specific absolute path `/home/ilenia/tests/jpa/` in a checked-in config → env→prop→default placeholder like the model.atlas config bundles |
| F110 | repo | CONTRIBUTING.md:46 | release-readiness | Security reports pointed at the mailing list instead of SECURITY.md (see F12) → update once SECURITY.md exists |
| F111 | repo | README.md:84 vs cnf/build.bnd:56 | docs | README says the project ships container images "instead of Maven Central artifacts", but build.bnd sets `maven-central: true` and build.yml wires Sonatype/GPG release secrets on main — docs and build config disagree → pick one story and align |

## Findings — infos (compact)

| # | Bundle | Where | What |
|---|---|---|---|
| F112 | datagen.rest | DataGenResource.java:53 | Untargeted static `@Reference ResourceSet` binds whichever service ranks highest; siblings use `@Context ResourceSet` — add a target filter or use @Context |
| F113 | rest.client.impl | spi/package-info.java:2 | Exported SPI lives under `.impl.spi` (documented/deliberate); consider promoting to `rest.client.spi` on the next major |
| F114 | emf.common / rest.client.osgi | configurator/DynamicEPackageConfigurator.java:47 (also RemoteEPackagePublisher.java:218) | Known legacy `EPackage.Registry.INSTANCE` mirroring — symmetric and/or opt-in by design; in DynamicEPackageConfigurator prefer conditional `remove` (`INSTANCE.get(nsURI) == ePackage`) to avoid evicting another owner's entry |
| F115 | rest.application | exception/ModelAtlasExceptionMapper.java:113 | Debug-stacktrace switch read from a raw env var per request (deliberate debug aid); could be a component property with env default |
| F116 | rest.ecore.xmi | EcoreMessageBodyHandler.java:102 | Per-request INFO logging (102, 136, 152, 168) and leftover "governance application" naming/`temp://governance/` URI from copy-paste origin — downgrade to FINE, rename |
| F117 | schema.registry.impl | bnd.bnd:1 | Bundle referenced by no bndrun/runtime config and duplicates workflow RegistryService's root-EClass validation; together with F9 it is effectively dead — wire it in or retire the api+impl pair |
| F118 | rest.application | resource/SchemaPackagesResource.java:537 | REST layer computes content hashes via storage-layer static `AbstractEObjectStorageService.computeContentHash` (also ObjectRegistryResource:519) — issue #156 territory; expose on the ScopeService/metadata API |
| F119 | workflow | bnd.bnd:27 | Misspelled buildpath attribute `versio=latest` silently ignored by bnd → `version=latest` |
| F120 | rest.uml | EnhancedUMLResourceFactoryImpl.java:38 | Hand-written method carries a misleading `@generated` tag (tooling will skip it) → remove tag |
| F121 | org.eclipse.uml2.uml | bnd.bnd:16 | Vendored repackaging of Eclipse UML2 5.7.0 (768 upstream EPL files) under the upstream BSN; exports are versioned and `internal.*` marked `x-internal` per upstream convention — document upstream version/commit provenance and cover via Dash (F11) |
| F122 | repo | REFACTORING_PLAN.md, WORKFLOW_COLLECTOR_PLAN.md, exporter-migration-investigation.md, update.sh (root) | Working documents/scripts at the repo root — move under docs/ or remove before release |

## Systemic issues

1. **DS component classes exported as API** (F3–F7): five bundles export packages whose contents are `@Component` classes registered as their own service type (`workflow` ×3 collectors, `management` ×2 packages, `readable.scope.collector`, `model.documentation.provider`). One refactor pattern fixes all: interface in the exported package, component in a private one.
2. **Unbind-by-key breaks service replacement** (F22): six sites in four bundles remove map entries by key in dynamic unbind methods; the codebase itself contains the correct two-arg idiom (`ResourceSetCollector`, `EObjectStorageServiceCollector`) to copy.
3. **`deactivate()` does not undo `activate()`** (F16, F19, F20, F29, F39, F41, F48, F103): unshutdown executors, unclosed Lucene writers, unremoved global-registry entries, undetached shared-ResourceSet resources. Worth a one-time sweep with a checklist per component.
4. **Sibling-implementation contract drift** (F25, F27, F31, F33, F34, F60, F63, F65): the four REST format handlers and the file/apicurio storage pair each answer the same situations differently. Define the shared contract once (rest.common helper / storage API docs) and align.
5. **Debug leftovers in production code** (F24, F61, F106): stdout payload dumps, hello/test endpoints, System.out prints.
6. **License headers** (F1, F2, F10): ~52 files missing or wrong-license headers while the enforcement workflow never runs — fix headers and turn the workflow on in the same PR.
7. **Typos in identifiers and user-facing text** (F56): frequent enough across bundles to warrant a spell-check pass; several are user-facing ("now allowed" inverts the meaning).

## Skipped / not reviewed

- **`management.git*` bundles** — not on this branch (`snapshot`); the directories contain only untracked `bin`/`generated` leftovers, no `bnd.bnd`/src. Same for `org.eclipse.fennec.model.atlas.runtime.config.docker.git` (untracked skeleton), `org.eclipse.xsd`, `org.test.model`, `org.eclipse.fennec.data.atlas.mapping.datasource`, `org.eclipse.fennec.model.atlas.datagen.rest.tests` (no `bnd.bnd`).
- **`*.tests` bundles** — excluded from SOLID/structure checks per the rule set; they were included in the mechanical license-header scan (only `tests.common` flagged).
- **EMF-generated code** — structural/naming findings suppressed (`rest.model`, `management.apicurio.model`, `datagen.model`, `datagen.example.model`, workflow/scope.api `src-gen`); header findings still applied (F1).
- **Read depth** — full reads: management, management.file, management.apicurio, management.lucene, all eight rest.* server bundles except rest.model, rest.client.api, rest.client.osgi, workflow, scope.api, schema.registry.api/impl, readable.scope.collector, emf.common, healthcheck, mediatypes.api/impl, model.documentation.provider, bootstrap, validation, validation.rest, datagen, datagen.rest, data.atlas.epackage.watcher, data.atlas.jpa.rest, data.atlas.jpa.watcher. Skimmed: rest.model, management.apicurio.model, datagen.model, datagen.example.model, validation.model, rest.client.impl (bnd + core classes full, periphery spot-checked), data.atlas.jpa.config.local, runtime + runtime.config* (bnd/bndrun level), org.eclipse.uml2.uml (provenance/exports only).
- Uncommitted working-tree edits (CLAUDE.md, two bndruns) were not part of the review scope.
