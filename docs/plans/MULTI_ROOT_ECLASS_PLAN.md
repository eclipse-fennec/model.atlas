# Plan: Multi-EClass support for RegistryService (`root.eclass.uri`)

**Status:** proposal — pending discussion
**Date:** 2026-07-29
**Estimated effort:** ~half a day including tests

## Motivation

A `RegistryService` is currently configured with a single `root.eclass.uri`, so one
registry can only store objects of one EClass (or its subclasses). There are use cases
where a single registry should accept objects of two or more unrelated EClasses.

## Current state (analysis)

The config value is resolved once in the `RegistryServiceImpl` constructor into a single
`EClass rootEClass` field (`RegistryServiceImpl.java:99-106`). After that it is used in
exactly two places:

1. `isEClassCompatibleWithRegistry(EClass)` (`RegistryServiceImpl.java:508-511`) — the
   actual gate: exact match, or the root is among `eClass.getEAllSuperTypes()`.
2. `getRootEClass()` (`RegistryServiceImpl.java:520`) — inside this repo only consumed by
   `ObjectRegistryResource` to build the *error message* when the compatibility check
   fails (`ObjectRegistryResource.java:285` and `:491`), plus tests.

**Nothing else depends on the root EClass:**

- Storage services (file / apicurio / git) never see it.
- `ObjectMetadata` and the stage-action dispatch key on the per-object `objectType`.
- REST deserialization derives the EClass from the parsed payload itself; the
  compatibility check runs *after* parsing.
- `RegistryInfo` / the registries REST endpoint / the rest-client never expose or use it.
- The `schema.uri` config property is defined but not read by the impl at all.

`SchemaRegistryServiceImpl` (`schema.registry.impl`) has the same single-root pattern,
but no consumers outside its own bundle were found — it can follow later or stay as-is.

## Zero-code alternative (works today)

The compatibility check already accepts **subclasses**. If the EClasses share a
meaningful common supertype, configure that supertype as the root — the `workspace`
registry already does this with `Ecore#//EObject` (accepts everything). Multiple roots
only add value when the EClasses have **no useful common ancestor** and the registry
should still reject everything else.

## Proposed change

### 1. Config — `RegistryServiceConfig`

Change `String root_eclass_uri()` → `String[] root_eclass_uri()`
(`RegistryServiceConfig.java:57`), update the metatype description.

> **Backward compatible:** DS/ConfigAdmin coerces a single `String` value to a
> one-element array, so all existing configs (`workflow.json` files, test `@Property`
> annotations) keep working unchanged.

### 2. Implementation — `RegistryServiceImpl`

- Resolve to `List<EClass> rootEClasses`; fail activation if **any** URI does not
  resolve to a known EClass (same behavior as today for a single bad URI).
- `isEClassCompatibleWithRegistry` becomes: matches any root, or has any root among its
  supertypes.

### 3. API — `workflow-api.ecore` (the only mildly invasive part)

`RegistryService` is EMF-generated; the `getRootEClass` operation is defined at
`workflow-api.ecore:279`.

- Add a `getRootEClasses()` operation (eType `EClass`, upper bound -1) and regenerate.
- Keep `getRootEClass()` returning the first/primary root for backward compatibility
  (document it), or drop it — **decision needed**.
- `AtlasSchemaRegistryService` implements the new operation trivially:
  `List.of(EcorePackage.Literals.EPACKAGE)`.

### 4. REST — `ObjectRegistryResource`

Update the two error-message strings (`:285`, `:491`) to list all accepted root EClasses
instead of the single one.

### 5. Tests & docs

- Extend `AtlasSchemaRegistryServiceTest` / `AtlasSchemaRegistryServiceIntegrationTest`
  assertions for the new operation.
- New unit test: registry configured with two unrelated roots accepts instances of both
  (and their subclasses), rejects a third EClass.
- Note in config docs / example `workflow.json` showing the array form.

## Out of scope

- `SchemaRegistryServiceImpl` (`schema.registry.impl`) — same pattern, no known
  consumers; follow-up if ever needed.
- Client-side changes — the rest-client never uses the registry root EClass.
- Storage backends — unaffected.

## Open decisions

| # | Question | Options |
|---|----------|---------|
| 1 | Keep `getRootEClass()` after adding `getRootEClasses()`? | keep (deprecated, returns first root) / remove |
| 2 | Is the zero-code supertype approach good enough for the concrete use case? | if yes, no change needed |
