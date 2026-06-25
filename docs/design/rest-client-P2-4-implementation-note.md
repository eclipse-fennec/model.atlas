# P2-4 — Implementation note (EPackage XMI deserialization)

**Ticket:** P2-4 "EPackage XMI deserialization" (Phase 2).
**Depends on:** P2-3. **Date:** 2026-06-05.

## Scope

Fill the `EPackageDeserializer` seam (P2-3) with a real implementation that turns the
`application/xmi` body the server returns for a package content GET into a fully resolved
`EPackage` whose `EFactory` works. No live server is needed — the wire format is plain EMF XMI, so
tests round-trip the exact bytes the server emits.

## Wire format (confirmed)

The server's `EcoreMessageBodyHandler` (bundle `rest.ecore.xmi`) serializes/deserializes an EPackage
with **stock EMF XMI** — no codec: `Resource.save`/`load` with `EcorePackage.eCONTENT_TYPE`. So the
client mirrors `readFrom` directly; a fixture serialized the same way is byte-faithful to the wire.

## `XmiEPackageDeserializer`

`deserialize(InputStream, nsUri, mediaType)`:
- loads the body into a `Resource` (absolute URI `atlas-client://epackage.ecore`) with the same robust
  options the server uses (`OPTION_DEFER_ATTACHMENT`, `OPTION_DEFER_IDREF_RESOLUTION`,
  `OPTION_LAX_FEATURE_PROCESSING`, `OPTION_RECORD_UNKNOWN_FEATURE`),
- maps load errors / empty content / non-EPackage roots to `ModelAtlasClientException`,
- returns `resource.getContents().get(0)` as the `EPackage` (dynamic `EFactory`, so
  `getEFactoryInstance().create(eClass)` works).

Wired as the default deserializer in `ModelAtlasClientImpl` (replacing the P2-3
`EPackageDeserializer.unsupported()` placeholder, now removed).

## Reference resolution without I/O — `PackageLoadingResourceSet`

A bare standalone `ResourceSet` can't resolve a fetched package's references in a plain-Java client.
Two cases, both fixed by a small `ResourceSetImpl` subclass that resolves at the `getEObject` layer
(diagnosed by dumping the serialized XMI + post-load proxy state):

1. **References into a registered package** (e.g. `EString`): in a plain-Java client `Ecore` has no
   backing resource, so the default set tries to demand-load
   `http://www.eclipse.org/emf/2002/Ecore` and fails. The set instead resolves the fragment against the
   registered `EPackage` (materializing a cached holder resource if the package — standalone `Ecore` —
   has none). In OSGi the packages already carry resources, so the holder is never used.
2. **Internal self-references serialized with the server's resource name.** EMF saves with a *relative*
   resource URI, so a same-document reference comes across as e.g. `sample.ecore#//Person` (verified in
   the dumped bytes). **The server does the same** (`createResource(createURI(name + ".ecore"))` is
   relative), so this is a real production case, not a fixture artifact. Loaded under a different URI it
   wouldn't resolve, so — while exactly one document is being loaded — such a reference's fragment is
   resolved against that one document.

`EcoreUtil.resolve`/`resolveAll` alone would **not** fix either: they route through
`resourceSet.getEObject`, which still can't locate `Ecore` or the server's `sample.ecore`. The fix has
to be at the resolution layer, which is what this set provides; lazy resolution via `getEType()` then
works with no explicit `resolveAll` needed.

Genuinely external references (to another Atlas package not yet fetched) stay unresolved here — that is
the Atlas-aware ResourceSet's job (P2-7).

## Tests (no live server)

`XmiEPackageDeserializerTest` round-trips the exact server bytes via `EcoreXmiFixtures` (a sample
dynamic package: one `EClass`, an `EString` attribute → cross-ref into `Ecore`, and a self `EReference`
→ intra-document ref), covering the ticket criteria:

| Criterion | Test |
|---|---|
| `getEPackage(nsUri)` returns a fully resolved `EPackage` | `deserializesToFullyResolvedEPackage`, `resolvesCrossReferencesIntoEcoreAndSelf` (EString resolves to Ecore; self ref resolves to Person) |
| `EFactory.create(EClass)` works | `eFactoryCreatesInstances` |
| works for representative EPackages | the fixture package (see note on "jena" below) |
| (robustness) error handling | `malformedXmi_throwsModelAtlasClientException` |
| fetched package isn't pre-registered locally | `deserializedPackageIsNotRegisteredGlobally` |
| end-to-end through the fetch path | `fetchedThroughProvider_returnsResolvedPackage` (real deserializer + mocked transport) |

**On the ticket's "jena" wording:** there is no special "jena EPackage" — `jena` is just a scope name
(a scope + schema registry); any EPackage can live there. So the criterion is satisfied with
representative ecore fixtures rather than a canonical jena model. The genuine live server↔client fetch
is **P2-11** (Testcontainers).

## Build status

As of 2026-06-05 `:rest.client.api:build` and `:rest.client.impl:build` are green (28 unit tests).
`org.eclipse.emf.ecore.xmi` added to the impl buildpath. Remaining in Phase 2: P2-5 (cache), P2-6
(drift), P2-7 (Atlas-aware ResourceSet — where external cross-package refs get fetched/resolved).
