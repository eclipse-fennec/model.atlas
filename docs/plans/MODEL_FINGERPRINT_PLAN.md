# Model Fingerprint Support (issue #158, task 3 + metadata integration)

**Goal:** adopt the fennec emf.osgi model-fingerprint feature
(`docs/model-fingerprint-guide.md` in `eclipse-fennec/emf.osgi`) so every
EPackage the Atlas registers or stores carries a content-derived identity:

1. as the `emf.fingerprint` **service property** on every EPackage the
   `EPackageStageActionService` → `DynamicEPackageRegistrationService` chain
   publishes, and
2. as a new **`fingerprint` attribute on `ObjectMetadata`**, computed
   server-side whenever an EPackage is uploaded/updated via REST.

Context: issue #158 (multi-version EPackage publication). Task 1 of #158 is
**only partially addressed**: `DynamicEPackageRegistrationService` today keys
by scope+stage+nsURI — the issue's *fallback* option ("fingerprint or
nsURI+stage"), i.e. the workflow location stands in for the model version.
This plan upgrades the key to true fingerprint-based version identity (Phase
F3) and covers task 3 (emit the fingerprint property) plus the Atlas-side
metadata integration; the consumer-side fingerprint-keyed registries are
model.metadata#15 and out of scope here.

## Investigation findings (2026-08-03)

### emf.osgi (source `/opt/git/emf.osgi`, branch state)

- Current version: `base-version: 1.1.0` (`cnf/ext/version.bnd`). Maven releases
  stop at **1.0.2** (2026-07-16, **without** fingerprint support);
  **`1.1.0-SNAPSHOT` on Central Portal snapshots (published 2026-07-29) contains
  the fingerprint API** — verified by downloading
  `org.eclipse.fennec.emf.osgi.api-1.1.0-20260729.170249-1.jar`
  (contains `org/eclipse/fennec/emf/osgi/fingerprint/FingerprintService.class`).
  → we must consume the snapshot until 1.1.0 is released.
- API surface:
  - `FingerprintService` (api bundle, package `…osgi.fingerprint`):
    `fingerprint(EPackage, String... derivationInputs)`, `currentScheme()`,
    `supportedSchemes()`, `fingerprintInScheme(...)`.
  - `FingerprintHelper` (impl bundles, exported package `…osgi.fingerprint.util`)
    for non-OSGi/static contexts.
  - Property/attribute name constant:
    `EMFNamespaces.EMF_MODEL_FINGERPRINT = "emf.fingerprint"`
    (`org.eclipse.fennec.emf.osgi.constants.EMFNamespaces:80`).
  - `DefaultFingerprintService` is a DS component private to **both**
    `…emf.osgi.component` and `…emf.osgi.component.minimal`; both export
    `fingerprint` + `fingerprint.util`. Our runtimes already run these bundles →
    after the dep bump the service is available with no extra runrequires.
- Value format: `fp1:<sha256-hex>`; reproducible, representation-independent
  (ecore file vs generated code), **computed-never-trusted** (never adopt a
  client-supplied value), ~0.1–0.5 ms per package, cached per instance.
- NOTE: issue #158 names the property "fennec.model.fingerprint"; the framework
  standardized on **`emf.fingerprint`** (guide + `EMF_MODEL_FINGERPRINT`
  constant). Use the constant; mention on the issue.

### model.atlas current state

- fennecEMF library pin: `cnf/ext/libraries.maven:2` =
  `org.eclipse.fennec.emf:org.eclipse.fennec.emf.osgi.bnd.library.workspace:0.1.2`
  → its embedded index serves all emf.osgi artifacts at 0.1.2 (no fingerprint).
- Registration chain (single choke point for ALL storage backends —
  file/git/apicurio — because the stage action loads from storage first):
  `EPackageStageActionService` (workflow) →
  `DynamicEPackageRegistrationService.registerEPackage(ePackage, metadata)` →
  `DynamicEPackageConfigurator.getServiceProperties()` builds the props
  (`emf.name`, `emf.nsURI`, `emf.fileExtension`, `emf.version`,
  `emf.model.scope`, `atlas.stage`, `dynamic.registration`) used for **all four**
  service registrations (EPackageConfigurator, EPackage, EFactory, Condition).
- `ObjectMetadata` (management.ecore) already has `contentHash` (SHA-256 of the
  stored XMI bytes — transport/storage identity) and
  `generationTriggerFingerprint` (hash of the datagen JSON trigger — unrelated).
  Neither is the semantic model identity; a new attribute is needed.
- REST: `SchemaPackagesResource` is the **only** place EPackages are
  created/updated via REST (`createPackage` fresh-create ~line 355 and the
  overwrite path ~line 340; `rest.jsonschema`/`rest.xsdschema`/`rest.ecore.xmi`
  have no @POST/@PUT). `ObjectRegistryResource` uploads generic EObjects into
  non-schema registries — not an EPackage path.
- Lucene: `LuceneRegistryHelper` indexes metadata attributes via explicit field
  constants (`FIELD_GENERATION_TRIGGER_FINGERPRINT` pattern, lines 137/579/657).
- `management.genmodel` has `copyrightText` set → regen will NOT wipe license
  headers (CI license gate safe).

## Steps

### Phase F1 — dependency bump (prerequisite)

- [ ] `cnf/ext/libraries.maven:2` → `…emf.osgi.bnd.library.workspace:1.1.0-SNAPSHOT`.
- [ ] `./gradlew cleanCache` (expanded library templates are cached by version,
      but going release→snapshot deserves a clean slate) + full build.
      Watch for compile breaks: 0.1.2→1.1.0 includes the Gecko→Fennec codegen
      class renames and a new codegen (generated `*ConfigurationComponent`
      sources will change — commit regen separately if noisy; check every
      regenerated model bundle's genmodel has `copyrightText` first).
- [ ] Re-resolve ALL bndruns (ranges move from `[0.1.2,0.1.3)` to 1.1.0):
      `runtime_base` + docker file/apicurio/git + local + local-jena,
      `data.atlas.jpa.rest/launch.bndrun`, every `*.tests/test.bndrun`,
      `rest.client.osgi.tests`. (`resolve.*` not parallel-safe:
      `gradle-parallel false`.)
- [ ] Sanity: `emf.osgi.component(.minimal)` 1.1.0 in the resolved runbundles;
      the `org.gecko.emf.osgi.*` `-runblacklist` guards stay as-is.
- [ ] When emf.osgi releases 1.1.0: bump the library coordinate to the release.

### Phase F2 — `fingerprint` attribute on ObjectMetadata

- [ ] `management.ecore` / `ObjectMetadata`: add optional (`lowerBound=0`)
      EString `fingerprint`. Documentation: "Content-derived model fingerprint
      (`emf.fingerprint` format, scheme-prefixed e.g. `fp1:<digest>`). Set for
      EPackage objects; computed server-side, never taken from the client.
      Distinct from contentHash, which hashes the stored XMI bytes."
- [ ] Regenerate from `management.genmodel` (copyrightText verified present).
- [ ] Lucene: add `FIELD_FINGERPRINT` to `LuceneRegistryHelper` (mirror the
      `generationTriggerFingerprint` pattern: constant + `addFieldIfNotNull` +
      the queryable-fields list at ~657) and `MetadataQueryBuilder` so a
      fingerprint → metadata lookup works (the #156/#158 join-key use case).

### Phase F3 — fingerprint at registration: service property + version-aware key

- [ ] `DynamicEPackageRegistrationService`: `@Reference FingerprintService`;
      in `registerEPackage(...)` compute
      `String fp = fingerprintService.fingerprint(ePackage)` once, **before**
      building the key; pass it into the `DynamicEPackageConfigurator`
      constructor.
- [ ] `DynamicEPackageConfigurator.getServiceProperties()`: add
      `EMFNamespaces.EMF_MODEL_FINGERPRINT → fp` (all four registrations pick it
      up automatically since they share the property map).
- [ ] **Rework `RegistrationKey` to carry version identity (issue #158 task 1
      proper)** — recommended shape:
      `RegistrationKey(scope, stage, nsURI, fingerprint)` with semantics:
      - register: same full key already present → idempotent no-op (identical
        content re-registered, e.g. startup replay);
      - same (scope, stage, nsURI) present under a **different** fingerprint →
        **replace**: unregister the stale entry, register the new one (today
        this case silently returns `false` and keeps the stale services — the
        actual bug behind #158 task 1);
      - unregister(scope, stage, nsURI): removes whatever fingerprint that
        location currently holds (stage events don't know the fingerprint).
      Keep a secondary lookup (or iterate — the map is small) for the
      (scope, stage, nsURI) prefix.
      **Why scope/stage stays in the key**: consumers filter on
      `(atlas.stage=…)` / `(emf.model.scope=…)`, so one service registration
      per workflow location is required regardless; the fingerprint *augments*
      the key with content identity rather than replacing the coordinates.
      *Alternative (flag for boss)*: key purely by (nsURI, fingerprint) and
      refcount holder stages — dedupes identical content across stages but
      needs multi-valued scope/stage service properties and live property
      updates on stage exit; more churn for consumers, only worth it if
      duplicate registrations of identical content are an actual problem.
- [ ] Adjust `EPackageStageActionService` bookkeeping: track the registered
      fingerprint per objectId (alongside `registeredNsURIs`/
      `registeredVersions`) so UPDATE can short-circuit when content is
      unchanged and EXIT stays correct.
- [ ] Guide compliance: the value is computed here, not read from metadata —
      even when `metadata.getFingerprint()` is present (compute-never-trust; the
      two should agree, log at WARNING if they don't — cheap drift detector
      between stored content and registered instance).
- [ ] `workflow/bnd.bnd`: buildpath already has the emf.osgi api via
      `enableEMF`; verify `FingerprintService` package import resolves.

### Phase F4 — metadata fingerprint producers (REST + git)

- [ ] `SchemaPackagesResource`: `@Reference FingerprintService`; set
      `metadata.setFingerprint(fingerprintService.fingerprint(ePackage))` in
      **both** the fresh-create path (~line 355 block) and the overwrite path
      (before `updateInStageForRegistry`, ~line 340).
- [ ] `ObjectRegistryResource`: on content upload/update, if the uploaded
      EObject is an `instanceof EPackage`, compute and set the fingerprint too
      (packages arriving outside a schema registry still get their identity).
- [ ] **Git backend** (`management.git`): `GitStorageHelper.deriveOne(...)`
      already parses the resource and matches `root instanceof EPackage pkg`
      (~line 548) — set `md.setFingerprint(...)` right there. Inject
      `FingerprintService` in `EObjectGitStorageService` (DS) and pass it into
      the helper constructor, consistent with F3/F4 (the static
      `FingerprintHelper` stays the fallback for non-DS contexts). Cost: once
      per derived entry per reconcile, cached in the `derived` map.
      Note: entries derived while their dynamic EPackage is still unresolved
      are retried later (existing `deriveOne` behaviour) — fingerprint follows
      the same retry, and per the guide an unresolved-proxy hash would differ,
      so never fingerprint a partially resolved package.
- [ ] Apicurio/file backends need no producer change: metadata is persisted
      with whatever the REST layer set.
- [ ] Never adopt a fingerprint arriving in a client payload anywhere metadata
      is deserialized from the request (audit: batch/validation endpoints that
      accept metadata containers).
- [ ] OpenAPI/Swagger: attribute appears automatically via the model; verify the
      generated schema mentions it.

### Phase F5 — tests

- [ ] `workflow` unit (`DynamicEPackageRegistrationServiceStageAwareTest`):
      registered EPackage service carries `emf.fingerprint`; value matches
      `FingerprintHelper.fingerprint(pkg)`; two stages of diverging content
      under one nsURI carry different values (the #158 scenario).
- [ ] Key semantics (new): re-register identical content at the same location →
      no-op (same services stay up, no churn); register **changed** content at
      the same (scope, stage, nsURI) → old services unregistered, new ones up
      with the new fingerprint (replace, not silent skip); unregister removes
      only that location's entry.
- [ ] `workflow.tests` IT (`EPackageStageActionServiceIntegrationTest`):
      end-to-end ENTER → service property present; EXIT → gone.
- [ ] `rest.tests` IT: upload EPackage → response metadata has `fp1:`-prefixed
      fingerprint; re-upload identical content (overwrite) → same value;
      upload changed content → different value; client-supplied fingerprint in
      the payload is ignored.
- [ ] `management.git.tests` (`GitRegistryChainIT` /
      `EObjectGitStorageServiceIT`): derived metadata for a schema entry
      carries the fingerprint; registered service carries the property; value
      matches a locally computed one for the same content.
- [ ] `rest.tests`: EPackage uploaded through `ObjectRegistryResource` (non-
      schema registry) also gets a fingerprint; non-EPackage uploads stay null.
- [ ] Lucene tests: fingerprint field indexed + queryable.

### Phase F6 — runtime & follow-ups

- [ ] Re-export docker variants, rebuild `jena-snapshot`, rerun
      `rest.client.osgi.tests:testOSGi` (see migration-plan gotcha).
- [ ] Comment on #158: task 1 completed here (fingerprint-aware key; the
      earlier scope+stage+nsURI keying was the interim fallback), task 3 done,
      property name is `emf.fingerprint` per emf.osgi convention.

## Decisions (Ilenia, 2026-08-03)

1. **Git**: yes — compute the fingerprint for git-derived metadata too
   (→ Phase F4, `deriveOne`).
2. **`ObjectRegistryResource`**: yes — add the `instanceof EPackage` check
   (→ Phase F4).
3. **Client-side**: yes — expose the fingerprint in the client
   (→ Phase F7 below).
4. **`contentHash` vs `fingerprint`**: keep **both** — bytes-identity of the
   stored XMI vs semantic model identity; non-EPackage objects only ever have
   contentHash.
5. **objectId becomes a UUID** (Ilenia, 2026-08-03): today
   `SchemaPackagesResource` sets `objectId = Base64URL(nsURI)`, so two packages
   sharing an nsURI share an objectId — but objectId is the *lifecycle-audit*
   identity (upload, stage transitions, removal) and must be unique per object.
   → Phase F8. This also completes the #156 fix direction: objectId =
   **address/lifecycle id**, fingerprint = **content identity**, nsURI = a
   *property* — never a key.

### Phase F7 — client-side exposure (`rest.client.*`)

- [ ] Parse `fingerprint` from metadata responses into the client's metadata
      representation (api + impl + tests).
- [ ] OSGi front-end: the EPackage publisher trio registers client-side
      EPackage services — add `emf.fingerprint` there as well. Per
      compute-never-trust, **compute it locally** from the parsed package
      (emf.osgi is on the client's classpath anyway) and use the
      server-provided metadata value only as a cross-check; log at WARNING on
      mismatch (detects lossy transport/parsing — exactly the drift the
      fingerprint exists to catch).
- [ ] Candidate consumer win (follow-up, optional): use the fingerprint for
      client cache validation instead of/alongside ETags.

### Phase F8 — objectId decoupling: UUID instead of Base64URL(nsURI)

Investigated 2026-08-03; builds on the #156 audit (2026-07-23).

**Current state:** `SchemaPackagesResource.createPackage` sets
`objectId = encodePackageNsURI(nsUri)` (:317/:356) and **every**
nsUri-parameterized endpoint re-derives it for the lookup (listings :182/:245,
content :412/:459, delete :587/:608, transition :643). The raw nsURI is already
stored in `properties["nsUri"]` on both the upload path (:364) and git's
`deriveOne` (stamped 2026-07-23). Known latent bug from the audit: same nsURI
in two stages (file backend) = same objectId → `LuceneEObjectRegistryService.
metadataCache` (objectId-alone key) and Lucene `Term("objectId")`
delete-before-add collide; only the one-stage-at-a-time invariant hides it —
exactly the invariant multi-version publication removes.

- [ ] `createPackage`: `metadata.setObjectId(UUID.randomUUID().toString())`.
      `properties["nsUri"]` stays the nsURI carrier. The objectId is assigned
      once at upload and stays stable across stage transitions — that is the
      audit trail.
- [ ] **nsUri→metadata resolution** replaces every `encodePackageNsURI` lookup:
      add a scope-service-level lookup (e.g.
      `getMetadataByNsUriFromStageForRegistry(registry, stage, nsUri)` + a
      hierarchy-aware final-stage variant mirroring
      `getMetadataFromFinalStageForRegistry`) that resolves via
      `properties["nsUri"]` over the stage listing (metadata is already cached
      by the storage layer; `EPackageLuceneIndex.nsUriExact` exists but is only
      populated on REST writes — git-backed packages aren't in it, so the
      property scan is the authoritative route).
      Uniqueness invariant stays: one nsURI per (scope, registry, stage),
      enforced by the upload conflict/overwrite check via the new lookup.
- [ ] **Bonus fix, note in README**: the nsUri-parameterized endpoints start
      working for **git-backed** packages (today they recompute Base64URL and
      can never match git's `scope/stage/repoPath` ids — the documented "known
      limitation" from 2026-07-23 disappears).
- [ ] `encodePackageNsURI` dies; keep the null-param → 400 validation the #156
      audit asked for (the NPE sites :411/:459/:587/:643 get fixed by the same
      refactor).
- [ ] **Client fix (breaking dependency on the encoding!)**:
      `RemoteEPackageProviderImpl.decodeNsUri` (:434-439) Base64-decodes
      objectId to get the nsURI in `parseDescriptors` — switch to reading
      `properties["nsUri"]` from the listing JSON (present for upload- and
      git-backed entries alike); optionally keep the decode as a fallback for
      old servers during transition.
- [ ] Backends unchanged: git keeps `scope/stage/repoPath` (read-only, derived,
      no lifecycle transitions), generic `ObjectRegistryResource` keeps
      client-supplied ids, `AbstractEObjectStorageService` already falls back
      to `UUID.randomUUID()` for empty ids.
- [ ] Back-compat: existing stored metadata keeps its legacy encoded objectIds —
      harmless, since after this phase nothing derives meaning from the id
      shape and lookups go via the nsUri property. Legacy same-id-across-stages
      objects keep the metadataCache collision until re-uploaded; new uploads
      are collision-free.
- [ ] Tests: upload → objectId is a UUID and Location header uses it; same
      nsURI uploaded to two stages → two distinct objectIds (audit trails don't
      merge); nsUri-parameterized GET/DELETE/content/transition resolve
      correctly; nsUri lookup finds git-backed packages (the bonus fix);
      transition keeps the objectId stable.

Ordering note: F8 is independent of the emf.osgi bump (F1) and can land before
or in parallel with F2–F7; it touches the same `createPackage` block as F4, so
doing F8 first avoids a double edit.

## Out of scope

- **Consumer-side fingerprint-keyed registries** (#158 task 2 beyond Atlas,
  `EMFModelInfo`, `TypeDiscriminatorService`): model.metadata#15 / emf.osgi —
  other repos.
