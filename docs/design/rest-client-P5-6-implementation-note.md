# P5-6 — Object identity + cross-reference tests via the Atlas-aware ResourceSet

**Status:** DONE & build-green (`:org.eclipse.fennec.model.atlas.rest.client.impl:test`).

A test-only ticket: it proves three properties of the already-shipped machinery
(`RemoteReadOnlyScopeService` cache from P5-1, `AtlasDelegatingPackageRegistry` from P2-8) rather
than adding production code.

## 1. Identity policy (confirmed + documented)

Policy: a cache hit returns the **same instance** within one client lifetime; identity is keyed
per `(scope, registry, objectId)`; the read-only interface means callers must not mutate returned
objects. The design-doc *Open Question* is now marked **Resolved**.

Tests in `RemoteReadOnlyScopeServiceTest`:
- `get_cacheHit_returnsSameInstance_withoutSecondCall` (P5-1) — same id → same instance, no 2nd GET.
- `get_postTtlRevalidation_304_keepsSameInstance` (P5-1) — a `304` keeps the cached instance.
- `get_distinctObjectIds_returnDistinctInstances` (**new**) — different ids never alias.

## 2. Cross-package reference resolution via the Atlas-aware ResourceSet

`loadsInstanceWithCrossPackageReference_resolvingBothPackagesRemotely` in
`AtlasDelegatingPackageRegistryTest`:

- Two packages: `lib.Library` (containment `entries : lib.Entry` [abstract], `flagship : book.Book`)
  and `book.Book extends lib.Entry` (`shelf : lib.Library`). A `Library` contains a `Book`.
- Because `entries` is declared as the base `Entry` but holds a concrete `Book`, the serialized XMI
  carries `xsi:type="book:Book"` — so the loader resolves `NS_BOOK` **through the package registry**,
  i.e. the remote Atlas fallback (not via the in-memory metamodel graph).
- Asserts: the contained object's metamodel is the *other* package (`NS_BOOK`, fetched remotely),
  and cross-references resolve **both ways** — `book.shelf == library` and `library.flagship == book`.

### Key finding (why the first attempt failed)

If the contained element's declared feature type *is* the concrete type, EMF emits **no**
`xsi:type`, and the second package is reached through the first package's in-memory `eType` —
never looked up by nsURI. The registry is then consulted exactly **once** (root only), so an
eviction of the second package changes nothing. The fix is the abstract-base-typed containment
above, which forces an `xsi:type` and a genuine registry lookup. This mirrors how the server emits
polymorphic XMI for real models.

## 3. Jürgen's interdependent-package case (unload → re-resolution)

`interdependentPackages_reResolveAfterUnload_viaTheRootedRegistry`:

- First load roots both packages — `ensureAvailable` called once per nsURI (root `NS_LIB` + the
  `xsi:type` `NS_BOOK`) = 2 fetches.
- The dependent package is unloaded via drift (`registry.onPackageRemoved(NS_BOOK)`).
- A subsequent load **re-fetches** `NS_BOOK` and re-roots it (3rd fetch); `NS_LIB` stays cached
  (no extra fetch). Re-resolution succeeds because the delegating registry guarantees the package
  is re-rooted on demand.

## Files

- `org.eclipse.fennec.model.atlas.rest.client.impl/test/.../RemoteReadOnlyScopeServiceTest.java`
  (+1 test)
- `org.eclipse.fennec.model.atlas.rest.client.impl/test/.../AtlasDelegatingPackageRegistryTest.java`
  (+2 tests, shared fixtures `interdependentPackages` / `libraryWithBookXmi`)

## Remaining in Phase 5

- **P5-5** — acceptance: validation runs unchanged in-process vs remote (depends P5-4, P4-6).
- **P5-7** — retire `view` from the EPackage path (server stage-free `/{s}/schema` + `/schema/content`;
  client off `getView()`).
