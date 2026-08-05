# Current Model Atlas Issues

## The shared registry cache/index assumes objectId is globally unique, while the schema REST path derives objectId from the nsURI alone — an identity that ignores stage (and version), which stage-parallel worlds break. 
LuceneEObjectRegistryService.metadataCache is a flat Map<objectId, ObjectMetadata> (:216/:243), and the Lucene index enforces the same invariant — every update does deleteDocuments(new Term("objectId", objectId)) before adding, so there is exactly one cache entry and one index doc per objectId. With the schema-REST objectId = Base64URL(nsURI), that means one slot per nsURI — **globally**: the shared cache (`registry=main`) is a single instance serving all scopes and stages, and the encoded nsURI carries neither. Two stages holding
  the same nsURI → last write wins, and since findByScopeRegistryAndStage only filters the cache by stage, the losing stage's package simply vanishes from listings and reads (the storage file is still on disk, but the
  exception-fallback to storageService.queryObjects only triggers on a throw, not on empty). The same collision applies across **sibling scopes** holding the same nsURI: createPackage's uniqueness check only walks the parent
  visibility chain (SchemaPackagesResource:319-320), so two unrelated scopes uploading the same nsURI silently share one cache slot too (their storage files are separate — the file layout is scope/registry/stage-qualified — only the shared cache/index collides).

  Refinement 1 — the code already knew. RegistryServiceImpl.transitionToStage has this comment at lines 426-428: "If the registry is shared though, this will cause to remove also the newly created metadata, so we have to do
  it before storing the object in the target stage." That's why delete-from-source deliberately runs before store-to-target — storing first and deleting after would evict the freshly promoted entry from the shared cache,
  because both stages' copies share one objectId. So the collision was encountered during transition ordering and worked around locally, rather than identified as a keying problem.

  Refinement 2 — it's reachable today even without git. Two ways: (a) delete_after_transition=false (it's a config option, line 429) leaves the object in both stages with one cache slot; (b) createPackage checks uniqueness
  only in the target stage's visibility chain (SchemaPackagesResource:319-320), so directly uploading the same nsURI to draft and to approved is allowed — and silently collides. The classic move-workflow made the collision
  rare, not impossible.

  Refinement 3 — git made it structural, hence D9. With branch=stage the same repo path exists on all branches simultaneously and permanently, so the collision stops being an edge case. That's exactly the D9 decision
  (2026-07-17): qualify the git objectId as scope/stage/repoPath, contained to the git backend — which fixed the cache collision for git, at the cost of the two objectId schemes now coexisting (encoded-nsURI vs path-based;
  documented in the management.git README "Known limitations"). Note the git scheme also fixes the scope dimension (the id starts with the scope), which the encoded-nsURI scheme lacks.

## There are some REST endpoints that currently return a 204 when querying for a git-stored EPackage, because the objectId for git is set differently from the one assumed by the REST api

Affected (return 204 despite the package existing):
  - listReleasedPackages with ?nsUri= — the single-package branch (SchemaPackagesResource:179-190)
  - listPackagesInStage with ?nsUri= — same pattern (:238-249)
  - getPackageContent (/stages/{stage}/content?nsUri=, :404)
  - getPackageContentFromFinalStage (/content?nsUri=, :452)
  - deletePackage (:580) and transitionPackage (:636) — these also 204 on the lookup, which is arguably the wrong error for git: they fail as "not found" before ever reaching the read-only rejection, masking the real
    situation ("exists but immutable")
  - Related wrinkle on the write side: createPackage with overwrite=true against a git-backed scope finds no existing metadata (encoded-nsURI lookup misses) → takes the create path → the storage layer's
    UnsupportedOperationException surfaces as a 500 rather than a clean "read-only backend" response.

  Not affected: the stage listings without ?nsUri (they filter by scope+registry+stage, no objectId computation), and the generic /{scope}/registries/schema/... endpoints, where the client supplies the objectId verbatim —
  those retrieve git packages fine.

  Two details worth keeping attached to this issue:

  1. ~~Clients can't bridge the gap themselves~~ **Partially mitigated (2026-07-23):** GitStorageHelper.deriveOne now stamps properties["nsUri"] on derived schema metadata (same property the schema upload path sets at :364),
    so a client CAN map nsURI → objectId from a stage listing and then query by objectId. deriveOne also now sets contentHash (SHA-256 of the raw blob bytes), filling the content-ETag / aggregate-manifest gap for git.
  2. The root cause is the same identity confusion as issue 1, seen from the other side: the REST schema API hardcodes one objectId-derivation rule as if it were a system-wide contract, but objectId semantics are actually 
    backend-defined. Issue 1 is what breaks when two things share an objectId; issue 2 is what breaks when the same logical thing has two different ones.

  ~~Remaining short-term mitigation: change the schema endpoints to resolve nsURI → metadata via a query on the properties["nsUri"] field (or the ePackageIndex) instead of recomputing the encoded id — that makes the endpoints
  scheme-agnostic. Long-term it folds into the #156 identity work.~~
  **RESOLVED (2026-08-03, fingerprint plan phase F8):** exactly this was implemented — the schema endpoints resolve nsURI → metadata via the properties["nsUri"] scan (scope-service level, so it works for every backend; the
  ePackageIndex was deliberately NOT used because it only sees REST writes), upload-path objectIds became opaque UUIDs, and `encodePackageNsURI` is gone. The nsUri-parameterized endpoints now work for git-backed packages
  (proven by `GitRegistryChainIT.nsUriPropertyLookup_findsGitBackedPackages`). NOTE the write-side wrinkle above is only half-fixed: overwrite=true now FINDS the git-backed metadata (no more silent create path), but the
  subsequent update still surfaces the read-only backend's UnsupportedOperationException as a 500 — git-derived metadata does not set isReadOnly, so the clean 403 path is not taken. Still open.

## Fennec-codec as well as some model atlas internal services still look up and store EPackage only by nsURI, causing issues when we have multiple EPackages with the same nsURI 

This issue has the widest blast radius, because it's not one bug but a bug class: any component that whiteboard-tracks EPackage services (or mirrors a registry) keyed by nsURI alone. The audit (2026-07-23) found these:

  fennec-codec side:
  - MetadataServiceImpl.packagesByNsURI (:73) — first-wins register (:307-313: second same-nsURI registration silently returns the existing entry) + unconditional remove by nsURI on unbind (:367-376). This is the
    production-confirmed one. Note it fails in two ways: even before any removal, the second stage's objects are serialized with the first stage's metadata (silent wrong-version reuse); after any stage's unbind, the surviving
    stages get "Package … is not registered" → the JSON 500 observed in the 2026-07-22 manual e2e test (documented in the management.git README and model.atlas#156).
  - Its internal classesByEClass/featuresByEFeature maps (:74-75) are keyed by object identity, so only the first-wins stage's EClasses are ever indexed — stage-B EClass lookups return null even while the nsURI lookup
    "works". Internally inconsistent under multi-version.
  - MetadataServiceComponent.addEPackage(EPackage) (:70-84) binds the bare service object — no ServiceReference, no properties map — so it cannot even see atlas.stage/emf.model.scope today. Any fix starts with changing that
    signature.
  - Transitively: TypeDiscriminatorService (driven by MetadataService's onPackageRegistered/Unregistered callbacks, plus its own EPackage.Registry.INSTANCE.getEPackage(nsUri) fallback at :269).
  - CodecResource itself does the version-sensitive lookup getPackageMetadata(ePackage.getNsURI()) (:602) — notable because it has the actual EPackage instance in hand, so an instance-keyed lookup would disambiguate with
    zero property plumbing.

  model.atlas side (all pre-existing, not on the git runtime path, but same class):
  - emf.common DynamicEPackageConfigurator — put/remove on EPackage.Registry.INSTANCE by nsURI (:44/:47/:63/:64); reached by InitialModelLoader and EMFFileWatcher
  - EMFFileWatcher.ownedNsUris — flat nsURI map, rejects a second same-nsURI package outright (:274-281)
  - RemoteEPackageConfigurator (rest.client.osgi) — same flat put/remove (:74/:80); plus the opt-in mirrorToGlobal in RemoteEPackagePublisher (default off)

  One clarification: the atlas's own stage-aware registration path is not on this list — DynamicEPackageRegistrationService keys by (scope, stage, nsURI) since the stage-aware registration change (D8 change A, 2026-07-20), and the per-stage
  registries/ResourceSets are stage-filtered. That's what makes issue 3 crisply statable: the producer side is now multi-version-correct; a set of consumers still assume the old single-version-per-nsURI world. (Only cosmetic
  residue on the producer side: the workflow configurator's equals/hashCode are still nsURI-only, :144-158.)

  The fix surface is correspondingly uniform: each consumer needs either (a) instance-aware entries with refcounting (unbind removes that instance's entry, survivors keep serving — the red repro test
  MetadataServiceSameNsUriMultiInstanceTest in fennec-codec is the acceptance test), (b) keying by the properties that already exist on every workflow-registered service (emf.nsURI + atlas.stage + emf.model.scope) — note
  this first requires changing the bind signature, since MetadataServiceComponent.addEPackage currently receives the bare EPackage and no properties — or (c) the #156 endgame: keying by fennec.model.fingerprint once it is
  stamped, which also gives identical-content stages dedup for free.