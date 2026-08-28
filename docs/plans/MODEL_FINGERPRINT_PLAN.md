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

### Phase F1 — dependency bump (prerequisite) — **DONE 2026-08-03**

- [x] `cnf/ext/libraries.maven:2` → `…emf.osgi.bnd.library.workspace:1.1.0-SNAPSHOT`.
- [x] Full clean build green.
- [x] Bndruns re-resolved; `runtime_base` runbundles carry
      `emf.osgi.component`/`metadata` at `[1.1.0,1.1.1)`; blacklist guards
      untouched.
- [ ] When emf.osgi releases 1.1.0: bump the library coordinate to the release.

### Phase F2 — `fingerprint` attribute on ObjectMetadata — **core DONE 2026-08-03**

- [x] `management.ecore` / `ObjectMetadata`: optional EString `fingerprint`
      with the agreed documentation (line ~154).
- [x] Regenerated (`ObjectMetadata.getFingerprint()` in src-gen).
- [x] Lucene: `FIELD_FINGERPRINT` in `LuceneRegistryHelper` (constant :130,
      `addFieldIfNotNull` :587 → non-analyzed `StringField`, rebuild list :660)
      + `MetadataQueryBuilder.fingerprint(...)` (:288).
- [x] **Retrieval by fingerprint — DONE 2026-08-03**: eOperation
      `findByFingerprint` renamed to `findByGenerationTriggerFingerprint`
      (its javadoc always described the AI-generation duplicate check; the
      #156 audit had confirmed no production callers) and a new
      `findByFingerprint(String): List<ObjectMetadata>` added, with the
      List-return rationale in the eOperation's GenModel documentation
      annotation (content version ≠ object; same content on several git
      branches shares one fingerprint). Implemented in
      `BasicEObjectRegistryService` (Set-valued `objectIdsByFingerprint`
      index) and `LuceneEObjectRegistryService` (cache stream filter).
      **Bug found & fixed during review**: `removeFromIndexes` removed from
      the *new* fingerprint map using the *generationTrigger* key — and a
      blanket `remove()` on the Set-valued map would have evicted all stages
      sharing a fingerprint (the shared-fate hazard again). Now: GTF removal
      targets its own map; fingerprint removal removes only the objectId from
      the bucket and drops empty buckets; `deactivate()` clears both maps.
- [x] **Tests — DONE 2026-08-03** (all green: `management:test`,
      `management.lucene:test`, `management.lucene.tests:testOSGi`):
      - `BasicEObjectRegistryServiceTest`: GTF tests renamed; new
        `testFindByFingerprint` (List semantics, shared fingerprint across two
        entries, empty-list on unknown, NPE on null) and
        `testFingerprintIndexManagement` (removing one holder keeps the
        sibling; fingerprint update moves index buckets; last removal empties).
      - `LuceneRegistryInterfaceMethodsTest` (OSGi IT): GTF test renamed; new
        `testFindByFingerprint` with realistic `fp1:<hex>` values, shared
        fingerprint across stages, removal-safety assertion.
      - `LuceneRegistryAdvancedTest` (OSGi IT): four call sites adapted to the
        List API, GTF variants added alongside.
      - `MetadataQueryBuilderTest`: `fingerprint()` query form with `fp1:` value,
        fluent chaining, null/empty short-circuit.
      - `LuceneRegistryHelperTest`: `testSearchByFingerprint` — **the colon
        concern is resolved**: `parseQuery`/`buildExactMatchQuery` routes
        exact-match fields through a manual `TermQuery` (splits field:value on
        the FIRST colon, strips quotes), so `fp1:<hex>` round-trips in both
        quoted and unquoted form — now proven by test, including no
        cross-match between `fingerprint` and `generationTriggerFingerprint`
        and survival of index rebuild.
      Still open for later phases: multi-match via git branches in
      `management.git.tests` (F4/F5 territory, needs the git producer).

### Phase F3 — fingerprint at registration: service property + version-aware key — **DONE 2026-08-03**

- [x] `DynamicEPackageRegistrationService`: `@Reference FingerprintService`
      (Ilenia); fingerprint computed once in `registerEPackage(...)` before the
      key, passed into the `DynamicEPackageConfigurator` constructor; drift
      detector logs WARNING when `metadata.getFingerprint()` disagrees with the
      computed value (compute-never-trust).
- [x] `DynamicEPackageConfigurator`: new `fingerprint` ctor param + getter;
      `getServiceProperties()` adds `EMFNamespaces.EMF_MODEL_FINGERPRINT`
      (omitted when null, so `(emf.fingerprint=*)` keeps its meaning); property
      javadoc corrected to the real property names; `equals`/`hashCode` widened
      from nsURI-only to nsURI+scope+stage+fingerprint (the #156 audit's stale
      identity note).
- [x] **`RegistrationKey(scope, stage, nsURI, fingerprint)`** (Ilenia started,
      completed):
      - identical content at the same location → idempotent no-op;
      - same location, different fingerprint → **replace** (stale services
        unregistered under the lock; no REMOVE config event since the model
        re-registers immediately);
      - `unregisterEPackage(scope, stage, nsURI)` overload kept — resolves the
        location's current key via `findKeyForLocation` (fingerprint `null` =
        wildcard, since stage EXIT events don't know it); the 4-arg variant
        matches exactly;
      - `isRegistered(scope, stage, nsURI)` matches the location in whatever
        version it holds.
      **Why scope/stage stays in the key — and why the fingerprint must NEVER
      be the sole lifecycle identity**: the fingerprint names *content*, and
      identical content legitimately exists in several stages at once (git:
      unchanged file on N branches). Two stages sharing a fingerprint are
      therefore **two independent registrations** (two keys differing in
      stage, two services, both carrying the same `emf.fingerprint` property);
      `unregister(scope, stage, nsURI)` removes only that location's services
      and cannot touch the other stage's — no shared-fate deletion.
      Consumers filtering `(atlas.stage=…)` / `(emf.model.scope=…)` also
      require one service per workflow location regardless.
      The fingerprint's role in the key is strictly *within* one location:
      same key incl. fingerprint → idempotent no-op; same location, different
      fingerprint → replace.
      *Rejected 2026-08-03 (Ilenia)*: keying purely by (nsURI, fingerprint)
      with the registration as shared state — naive form has exactly the
      shared-fingerprint hazard (stage A's EXIT would unregister the service
      stage B still needs); the refcounted variant that avoids it buys nothing
      but property churn. Off the table.
- [x] `EPackageStageActionService`: no fingerprint bookkeeping needed — the
      registration service now owns the no-op/replace decision. UPDATE only
      explicitly unregisters when the object's **nsURI changed** between
      versions; for an unchanged nsURI the update flows into `registerEPackage`
      (no-op on identical content, atomic replace on changed content) — the
      services no longer flap through an unregistered window on every UPDATE.
- [x] Verified green (2026-08-03): `workflow:test` (7 unit tests incl. new
      replace / property-emission / fingerprint-exact-unregister tests against
      a deterministic fake FingerprintService), `workflow.tests:testOSGi`
      **98/98** (real DS wiring — emf.osgi.component resolves at 1.1.0 in the
      regenerated `generated/test.bndrun`; the checked-in `test.bndrun` pins
      are inert, `resolve.test` writes to `outputBndrun`),
      `management.git.tests:testOSGi` **14/14** including new
      `GitRegistryChainIT` assertions: both branch registrations carry
      `emf.fingerprint` with the `fp1:` scheme and **diverging branch content
      yields diverging fingerprints** — the #158 end-to-end proof. Workspace
      `assemble` green.

### Phase F4 — metadata fingerprint producers — **DONE 2026-08-03**

Implemented with a better shape than planned: instead of three REST/git
producers, **one producer at the storage layer**, exactly where `contentHash`
already has its single producer.

- [x] `AbstractEObjectStorageService.storeObject(...)`: right after the
      contentHash block, `metadata.setFingerprint(...)` is **always
      overwritten** — computed via `FingerprintHelper` for `instanceof
      EPackage`, **cleared to null for non-EPackage objects** (a
      client-supplied value cannot survive the upload path; compute-never-trust
      is structural, not a per-endpoint audit). This single site covers:
      - `SchemaPackagesResource` fresh-create (`uploadToStageForRegistry` →
        `storeObject`) **and** overwrite (`updateInStageForRegistry` →
        `RegistryServiceImpl.updateInStage` → `updateObject` → `storeObject`) —
        the REST resource needed **no change**, and the response metadata is
        the same instance mutated by `storeObject`, so REST responses carry it;
      - `ObjectRegistryResource` generic uploads (same path, the instanceof
        check does the discrimination);
      - Apicurio (its `updateObject` is delete-then-`storeObject`).
      `mergeMetadataUpdates` (metadata-only updates, content untouched)
      deliberately does NOT copy fingerprint — content unchanged ⇒ fingerprint
      unchanged, and another never-adopt guard for free.
- [x] **Git backend**: `GitStorageHelper.deriveOne` sets the fingerprint next
      to the existing `nsUri` property stamp (`root instanceof EPackage`),
      wrapped so a failed computation never fails the derivation (same
      contract as contentHash). Static `FingerprintHelper` used instead of a
      DS reference — it is the guide's sanctioned accessor (exported
      `fingerprint.util` package, resolves against full and minimal impl
      bundle) and mirrors how `computeContentHash` is a static in the same
      layer. Consequence to note: the storage layer uses the default
      fingerprint singleton rather than a hypothetically substituted
      `FingerprintService` OSGi service — identical implementation today; the
      F3 drift detector would flag any future divergence.
- [x] Buildpath additions: `management/bnd.bnd` and `management.git/bnd.bnd`
      gained `org.eclipse.fennec.emf.osgi.component.minimal` (for the exported
      `fingerprint.util` package).
- [x] Tests green (2026-08-03):
      - `GitStorageHelperTest.deriveMetadata_stampsAllReplayFields…` now also
        asserts a `fp1:`-prefixed fingerprint (management.git:test 19/19);
      - `EObjectFileStorageServiceTest`: store-and-retrieve asserts the
        persisted fingerprint; new `testFingerprintComputedServerSide_
        neverAdopted` proves client-supplied values are overwritten for
        EPackages, cleared for non-EPackages, and identical content
        reproduces the identical fingerprint (file.tests testOSGi 22/22);
      - `management.git.tests:testOSGi` 14/14, `rest.tests:testOSGi` 208
        passed / 4 skipped (pre-existing skips) — the full REST upload path
        over the changed storage layer.
- [ ] OpenAPI/Swagger: attribute appears automatically via the model — spot-
      check the generated schema once the runtime is next started (F6).

### Phase F5 — tests — **DONE 2026-08-03**

- [x] `workflow` unit (`DynamicEPackageRegistrationServiceStageAwareTest`,
      done in F3, 7/7): registered services carry `emf.fingerprint` (asserted
      via captured service properties against the test's deterministic fake
      FingerprintService — the real-value match is covered in the ITs below);
      same nsURI across stages stays independently registered (the #158
      scenario).
- [x] Key semantics (done in F3): re-register identical content at the same
      location → no-op; changed content at the same (scope, stage, nsURI) →
      replace, count stays 1; fingerprint-exact unregister refuses a wrong
      fingerprint; unregister removes only that location's entry.
- [x] `workflow.tests` IT (`EPackageStageActionServiceIntegrationTest`):
      `testRegistrationCarriesWorkflowProperties` now also asserts the
      registered service carries `emf.fingerprint` (`fp1:` scheme) **and that
      it equals the fingerprint the storage producer persisted in metadata** —
      the F3+F4 end-to-end agreement (drift detector silent). EXIT-removes-
      services was already covered. 98/98 green.
- [x] `rest.tests` IT: new `SchemaPackagesResourceTest.
      testCreatePackage_ComputesFingerprint` — create → `fp1:`-prefixed
      fingerprint in the response metadata; overwrite with identical content →
      identical value (reproducible); overwrite with changed content →
      different value (identifying). 209 passed / 4 pre-existing skips.
      Client-supplied-fingerprint rejection is covered at the storage seam
      (see F4's `testFingerprintComputedServerSide_neverAdopted`) — the schema
      upload body is the EPackage itself, so there is no metadata payload to
      smuggle a fingerprint through on this endpoint.
- [x] `management.git.tests`: `GitRegistryChainIT` asserts the property with
      diverging-branches → diverging-fingerprints (F3);
      `GitStorageHelperTest` asserts the derived-metadata fingerprint (F4).
- [x] `ObjectRegistryResource` uploads: covered at the shared storage seam —
      `createObject` funnels into the same `storeObject` producer proven by
      `testFingerprintComputedServerSide_neverAdopted` (EPackage → computed,
      non-EPackage → cleared); no separate REST-level duplicate test added.
- [x] Lucene tests: done in F2 (`testSearchByFingerprint`, query-builder and
      rebuild coverage incl. the colon round-trip).

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

### Phase F7 — client-side exposure (`rest.client.*`) — **DONE 2026-08-03**

- [x] API: `PackageDescriptor` gained a `fingerprint` component and
      `ResolvedEPackage` a `fingerprint` field/getter — both with the old
      constructors kept as compatibility delegates (no churn for existing
      callers incl. the emf.util consumers of the published client jars; the
      value is documented as *advisory*: trustworthy consumers compute
      locally).
- [x] Impl: `PackageMetadata` record + `fetchMetadata`/`resolve()` and
      `parseDescriptors` parse the `fingerprint` field from metadata JSON.
- [x] OSGi front-end: `RemoteEPackageConfigurator` computes the fingerprint
      **locally** via `FingerprintHelper` from the parsed package and stamps
      `emf.fingerprint` on the whole published trio (omitted when
      uncomputable, preserving the `(emf.fingerprint=*)` convention); a
      server-reported value is only a cross-check — WARNING on mismatch (the
      lossy-transport drift signal), never adopted.
      The `PackagePublication` seam's SAM became the 5-arg
      `publish(…, serverFingerprint)` (4-arg default delegates with null) so
      the cross-check value survives the `LocalFirstPublicationGate`
      (Candidate record carries it through park/republish) and reaches the
      publisher from every trigger: EAGER prefetch (descriptor + resolved),
      LAZY resolution, drift substitution, force.remote startup check.
      `republish` got the same overload. Buildpath +=
      `emf.osgi.component.minimal`.
- [x] Tests green (2026-08-03): impl unit tests extended (listing → descriptor
      fingerprint; resolve → `ResolvedEPackage.getFingerprint()`); new
      `RemoteEPackageConfiguratorTest` (local value stamped, mismatching
      server value never adopted, matching value silent); 7 existing recorder
      fakes adapted to the new SAM; api/impl/osgi `test` + workspace
      `assemble` + `rest.client.osgi.tests:testClasses` all green.
- [x] `AtlasClientOsgiIT.eagerMode_publishesRemoteEPackageServices` now
      asserts `emf.fingerprint` (`fp1:` scheme) on published client services —
      **runs in F6** after the jena-snapshot image rebuild (IT needs the
      fingerprint-aware server image).
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

- [x] `createPackage`: `metadata.setObjectId(UUID.randomUUID().toString())`.
      `properties["nsUri"]` stays the nsURI carrier. The objectId is assigned
      once at upload and stays stable across stage transitions — that is the
      audit trail. *(2026-08-03: done; Location header also fixed to the real
      resource path with the percent-encoded raw nsURI.)*
- [x] **nsUri→metadata resolution** replaces every `encodePackageNsURI` lookup.
      *Design pivot (Ilenia, 2026-08-03): the nsUri-specific operations were a
      schema-registry leak into the generic scope API — generalized to
      `getMetadataByPropertyFromStageForRegistry(registry, stage, key, value)`
      + `getMetadataByPropertyFromFinalStageForRegistry(registry, key, value)`,
      both returning `List<ObjectMetadata>` (property values are not unique in
      general; the nsUri caller takes the single element guaranteed by the
      upload conflict check). Implemented in `ScopeServiceImpl` (parent
      fallback only on empty own-stage result, parent hits read-only, never
      merged) and `AtlasScopeService`; key constant
      `WorkflowConstants.NS_URI_METADATA_PROPERTY`.* Resolves via
      `properties["nsUri"]` over the stage listing — the authoritative route
      (the Lucene index misses git-backed packages).
      Uniqueness invariant stays: one nsURI per (scope, registry, stage),
      enforced by the upload conflict/overwrite check via the new lookup.
      **Gotcha fixed:** `AtlasSchemaRegistryService.createMetadata` never set
      `properties["nsUri"]` — static-registry packages would have been
      invisible to the property scan (its internal Base64 objectId scheme is
      kept; it is self-consistent with its content lookups).
- [x] **Bonus fix, note in README (note still TODO)**: the nsUri-parameterized
      endpoints now work for **git-backed** packages — proven by the new
      `GitRegistryChainIT.nsUriPropertyLookup_findsGitBackedPackages`.
- [x] `encodePackageNsURI` dead; null nsUri/objectId params now throw IAE →
      400 (not WebApplicationException — several endpoints have no WAE catch
      and would turn it into a 500).
- [x] **Client fix**: `parseDescriptors` now reads `properties["nsUri"]` from
      the listing (tolerant of both EMap wire shapes: object map and key/value
      entry array), keeps the Base64 decode as a guarded fallback for pre-F8
      servers, and skips undecodable entries instead of poisoning the listing.
- [x] Backends unchanged: git keeps `scope/stage/repoPath` (decided again
      2026-08-03: a random UUID would be re-minted on every reconcile of the
      derived metadata — churn; UUIDv5 over the location triple is the escape
      hatch if shape-uniformity is ever needed), `ObjectRegistryResource`
      keeps client-supplied ids.
- [x] Back-compat: legacy encoded objectIds in stored metadata are harmless;
      transition endpoint additionally resolves a legacy nsUri-shaped
      objectId via the property lookup.
- [x] **Regression found by the suite:** `ScopeAggregateService.manifestKey`
      keyed on objectId → delete+recreate with a fresh UUID broke the
      aggregate-ETag stability contract; the manifest now keys schema entries
      by `registry+stage+nsUri` (matching what the diff reports), objectId
      otherwise.
- [x] Tests (all green 2026-08-03): rest.tests 208/208 incl. 3 new (UUID id +
      Location; two stages → distinct ids; transition by real objectId keeps
      the id stable); client.impl 120 incl. 3 new `nsUriOf` shape tests; git
      ITs 15/15 incl. the git-backed lookup; workflow 98, file 22, lucene 29.

Ordering note: F8 is independent of the emf.osgi bump (F1) and can land before
or in parallel with F2–F7; it touches the same `createPackage` block as F4, so
doing F8 first avoids a double edit.

## MetadataService interplay (verified 2026-08-03)

Background: in the git integration, removing an EPackage from one branch broke
JSON deserialization for objects of *another* branch holding the same content —
the old fennec.codec `MetadataService` was nsURI-keyed with first-wins
registration and **unconditional** removal (#156 audit, production-confirmed).

The MetadataService has since moved to fennec emf.osgi
(`org.eclipse.fennec.emf.osgi.metadata`) and the concern "it only knows
fingerprints, so removing one branch's package kills the shared-fingerprint
tree" does **not** hold — verified in `MetadataServiceImpl` (class javadoc
documents exactly this design):

- `registerPackage` computes the fingerprint first; **identical content
  deduplicates onto one shared metadata tree** and a per-fingerprint refcount
  (`livenessByFingerprint`) is incremented.
- `unregisterPackage` **decrements**; the tree is withdrawn only when the
  *last* holder unbinds. Two branches with identical content → refcount 2;
  branch A removed → 2→1 → branch B keeps deserializing.
- Diverging content under one nsURI = different fingerprints = separate trees;
  removal of one version cannot affect the other from that direction either.
- Pull-path trees (`getPackageMetadata(EPackage)`) carry no refcount and are
  never evicted by an unbind — cached reads can't be yanked by another
  branch's removal at all.

Why no stage/scope knowledge is needed there: identical content implies an
identical (content-derived) metadata tree — keeping one shared tree alive
until the last holder disappears is correct for every holder. Stage/scope only
determine which EPackage *service* a consumer binds, and that is Atlas's job
via the `atlas.stage`/`emf.model.scope` service properties.

Composition with Phase F3: Atlas registers one EPackage service per
(scope, stage) location; `MetadataServiceComponent` binds each of them
(MULTIPLE/DYNAMIC), so the refcount per fingerprint equals "how many live
locations hold this content" — stage A's EXIT is exactly one unbind.

Contract Atlas must uphold: **never mutate an EPackage after registration** —
`unregisterPackage` recomputes the fingerprint from the instance at unbind
time, so a mutated package would decrement the wrong entry (the guide's
"do not mutate" rule is load-bearing here).

## Out of scope

- **Consumer-side fingerprint-keyed registries** (#158 task 2 beyond Atlas,
  `EMFModelInfo`, `TypeDiscriminatorService`): model.metadata#15 / emf.osgi —
  other repos.
