# Issue sketch — the datagen endpoint hardcodes a scope, a registry and no stage

*Draft for a GitHub issue. Written 2026-08-07 out of quality-review finding F54. The finding is
"scope `jena` and registry `DataGen` are hardcoded"; the fix is not a rename, because the third
coordinate — the stage — is the same open question the OCL constraint sets have
(see [OCL_CONSTRAINT_STAGE_ISSUE.md](OCL_CONSTRAINT_STAGE_ISSUE.md)).*

---

## Summary

`GET /datagen/{objectId}` reads a `DataGenConfig` from a scope literally named `jena`, in a
registry literally named `DataGen`, from that registry's **final** stage, and then generates
instances of EClasses resolved against a `ResourceSet` that no part of the request selected.

Three of those four coordinates are decided by something other than the caller. The endpoint is
therefore dead for every deployment whose scope is not called `jena` — "jena" is just a scope
name, nothing about it is special.

## What happens today

`DataGenResource` (datagen.rest):

```java
private static final String JENA_SCOPE_NAME = "jena";
private static final String DATA_GEN_REGISTRY_NAME = "DataGen";

@Reference
private ResourceSet resourceSet;              // untargeted

@GET @Path("/{objectId}")
public Response generateByObjectId(@PathParam("objectId") String objectId) {
    ReadableScopeService<?> scopeService = getScopeService();               // -> scope "jena"
    Optional<?> content = scopeService.get(DATA_GEN_REGISTRY_NAME, objectId);  // -> final stage
    ...
    List<EPackage> targetPackages = resolvePackages(config);               // -> resourceSet's registry
}

private ReadableScopeService<?> getScopeService() {
    return scopeCollector.getScopeServiceByScopeName(JENA_SCOPE_NAME);
}
```

`POST /datagen` takes the config in the body, so it has no scope/registry/stage problem — but it
shares `resolvePackages`, and therefore the fourth coordinate.

Four separate things are being decided here:

| Coordinate | Decided by | Consequence |
|---|---|---|
| scope | the constant `"jena"` | endpoint is dead in any other deployment |
| registry | the constant `"DataGen"` | breaks if a deployment names it differently |
| stage | `ReadableScopeService.get()`, which is by contract the **final**-stage read | a config in `draft` cannot be run |
| target EPackages | whichever `ResourceSet` service SCR bound | not the caller's, possibly not even the caller's scope |

That last row is worth its own note — and it is **already on the books as info F112**
("untargeted static `@Reference ResourceSet` binds whichever service ranks highest; siblings use
`@Context ResourceSet`"), filed 2026-07-24. It is repeated here because the stage decision below
determines how it gets fixed, not because it is new. `SchemaRegistryChainConfigurator` creates one
`ResourceSetFactory` configuration **per (scope, stage) pair**
(`SchemaRegistryChainConfigurator.java:231-240`, properties `scope.name` / `stage.name`), so the
runtime holds many `ResourceSet` services. `DataGenResource`'s `@Reference ResourceSet` has no
target, so which one it binds — and hence which package registry `resolvePackages` searches — is
decided by service ranking and bind order, not by the request. Every other resource in the API
takes `@Context ResourceSet`, which `ScopedResourceSetProvider` resolves from the request's
`scopeName` **and** `stageName` path parameters, falling back to the default `ResourceSet` when
either is missing (`ScopedResourceSetProvider.java:99-105,127-134`).

## Why it matters

The registry is configured with a real workflow. From
`runtime.config.local.jena/configs/workflow.json:85-112` (identical in `docker/dockercompose/configs/jena.json`):

```json
"RegistryService~datagen": {
    "registry.name": "DataGen",
    "registry.description": "The DataGen Registry to store DataGenConfig objects",
    "workflow.transitions": [ "draft:release" ],
    "delete.after.transition": true,
    "stages": [ { "name": "draft",   "writable": true, "final": false },
                { "name": "release", "writable": true, "final": true  } ],
    "root.eclass.uri": "http://www.gme.org/datagen/1.0#//DataGenConfig"
}
```

Note there is **no `registry.type`**, so this registry's type is `OTHER` — the type-based lookup
the fix wants does not yet have a type to look for.

Note also `delete.after.transition: true`: a config exists in exactly one stage at a time. So
"read the final stage" is not a conservative default that merely misses newer drafts — a config
sitting in `draft` is not readable through this endpoint **at all**, and one that has been
released is no longer in `draft`. The draft stage of the DataGen registry is, as things stand,
write-only. That is the same shape as the COCL registry's problem in the OCL sketch.

## The three questions, in increasing difficulty

### 1. Scope — straightforward

Take it from the path, as every other resource does
(`/{scopeName}/schema`, `/{scopeName}/registries/{registryName}`, `/{scopeName}/stages/{stageName}/convert`),
and resolve it through the collector that is already injected. An unknown scope must answer 404
rather than today's NPE-into-500 (`getScopeServiceByScopeName` returns `null` and the result is
dereferenced immediately).

The only real decision is whether the `POST` moves under `/{scopeName}/...` too. It does not use
the scope today — but it does resolve EPackages, and that is exactly what a scope should decide,
so putting it under the scope is only honest if `resolvePackages` starts using the scope
(see question 3 and the `ResourceSet` note above). Otherwise the segment is decorative, which is
the sin F52 was originally filed for.

### 2. Registry — a type, an explicit name, or both

#### R-A. Add `RegistryType.DATA_GEN` and take the first registry of that type

Mirrors `ValidationServiceImpl.resolveConstraintSet` (`ValidationServiceImpl.java:380-392`) exactly:

```java
RegistryInfo datagenRegistry = scopeService.getScopeInfo().getRegistries().stream()
        .filter(r -> RegistryType.DATA_GEN == r.getType())
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("No DATA_GEN registry in scope: " + scope.getName()));
```

- **for:** no name in code and none in the URL; consistent with how COCL and SCHEMA are found; the
  deployment declares intent once, in `registry.type`.
- **against:** "first of type" is arbitrary if a scope ever has two; it is a **model change** —
  a literal in the `RegistryType` enum in `scope-api.ecore` — which is the model owner's call, and
  every existing config needs `"registry.type": "DATA_GEN"` adding or the endpoint finds nothing.
- **compatibility:** additive and safe in both directions. `RegistryServiceImpl:548` does
  `RegistryType.get(config.registry_type())`, and the generated EMF setter maps an unrecognised
  value to the default `OTHER`, so an older server reading a `DATA_GEN` config degrades to
  today's behaviour rather than failing. The REST client does the same
  (`RemoteReadableScopeService.java:410-415` keeps the default when `get` returns `null`).
- **effort:** small, once the model change lands.

#### R-B. Name the registry in the request

`/{scopeName}/registries/{registryName}/datagen/{objectId}`, or a `?registry=` parameter.

- **for:** no model change, works for any number of datagen registries, and it is the shape
  `ObjectRegistryResource` already uses.
- **against:** every caller has to know the deployment's registry name; the endpoint stops being
  self-describing.

#### R-C. A configurable default (component property)

`datagen.registry.name`, defaulting to `DataGen`, read from the component configuration.

- **for:** trivial, no model change.
- **against:** one name per **runtime**, not per scope — which fails the moment two scopes in the
  same runtime name their datagen registry differently. This is the weakest option; listed for
  completeness.

#### R-D. R-A with R-B as an override

Resolve by type by default; let an explicit `?registry=` pin one.

- **for:** zero-config for the normal case, escape hatch for the odd one, and it makes the
  "two registries of the same type" objection to R-A moot.
- **against:** slightly more surface than R-A alone.

#### R-E. Delete the GET endpoint

A client can already fetch the config from
`/{scopeName}/registries/{r}/stages/{s}/{objectId}` and POST it back to `/datagen`. The
convenience endpoint is what forces every one of these decisions.

- **for:** removes the whole problem; the POST is scope/registry/stage-free by construction.
- **against:** two round-trips, and the config travels through the client for no reason. Worth
  stating explicitly only because "remove the feature" is a legitimate answer to a dead endpoint.

### 3. Stage — the same question as the OCL constraint sets, plus one

For OCL, the stage decides one thing: which copy of the constraint set runs. Here it decides
**two**, and they need not be the same:

- **(a) which copy of the `DataGenConfig`** is read, and
- **(b) which stage's EPackages** the generated instances conform to — i.e. whether you can
  generate test data for a schema that is still in `draft`.

(b) is the reason this endpoint exists at all: generating data against a schema you are still
working on is more useful than generating it against a released one.

#### T-A. Final stage only, documented

Keep `get(registry, objectId)`; say in the OpenAPI description that datagen configs are released
artifacts.

- **for:** no change, consistent with OCL today.
- **against:** with `delete.after.transition`, the draft stage stays write-only, and (b) is
  answered "release only" — which rules out the main use case.

#### T-B. Stage in the path

`/{scopeName}/stages/{stageName}/datagen/{objectId}`, matching `ObjectRegistryResource` and
`ModelConverterResource`.

- **for:** answers (a) and (b) with one segment, and (b) comes for free: with both `scopeName`
  and `stageName` in the path, `ScopedResourceSetProvider` hands the resource the right
  per-(scope, stage) `ResourceSet`, so switching `@Reference ResourceSet` to `@Context ResourceSet`
  makes `resolvePackages` scope- and stage-correct with no further plumbing. Needs
  `registryView(registry, stage).get(objectId)` for the config read — implemented server-side
  since F44.
- **against:** the caller must know which stage their config is in; with
  `delete.after.transition` that is a 404 whenever they guess wrong.

#### T-C. Optional stage: two paths, one resource

`/{scopeName}/datagen/{objectId}` = final stage (today's semantics),
`/{scopeName}/stages/{stageName}/datagen/{objectId}` = explicit. `ObjectRegistryResource` already
carries exactly this pair for `/content`.

- **for:** backward compatible, explicit when it matters.
- **against:** two paths to document and test; the short form keeps the (b) problem.

#### T-D. Derive the stage from where the config was found

Do not put the stage in the URL. Look the config up across the registry's stages, and generate
against the stage it was found in: *the data is generated against the schema of the stage the
config lives in*.

- **for:** self-consistent, no extra parameter, and it answers the user-facing question directly —
  a draft config generates draft-schema data, a released one generates released data. With
  `delete.after.transition` there is exactly one candidate, so the lookup is unambiguous **in the
  current configuration**.
- **against:** that unambiguity is a property of the config, not of the model — a registry without
  `delete.after.transition` has the same object in several stages and the rule needs a tie-break
  (first? final? most recent?). The scan is also N reads instead of one.
- **note:** the POST has no location to derive from, so it still needs an explicit stage or the
  default.

#### T-E. Two independent coordinates

The config's stage and the schema's stage are genuinely separable: read the config from the final
stage (or from `?configStage=`) and generate against `?schemaStage=`.

- **for:** the most expressive; a released, reviewed generator config can be run against a draft
  schema, which is a plausible workflow.
- **against:** the most surface, and two stage parameters in one request is a lot to explain.

#### T-F. Put the target stage in the model

`DataGenConfig` already carries `targetModelNsURIs`. It could carry the scope and stage those
nsURIs should resolve against, making generation self-describing regardless of the URL.

- **for:** the config becomes portable — the same document generates the same data wherever it is
  posted from; answers (b) for the POST too, which no URL-based option does.
- **against:** a model change; and a stored config can then contradict the URL it was fetched
  through, so one of the two has to be declared authoritative.

## Recommendation

**Scope: path parameter** — settled, no real alternative.

**Registry: R-A, with R-D as the fallback** if a scope may hold more than one datagen registry.
The type-based lookup is what the codebase already does twice, and it keeps deployment knowledge
in the deployment. It needs the `DATA_GEN` literal in `scope-api.ecore` and
`"registry.type": "DATA_GEN"` added to the two `workflow.json` files.

**Stage: T-B**, i.e. `/{scopeName}/stages/{stageName}/datagen/{objectId}`, with `@Context ResourceSet`
replacing the untargeted `@Reference` — because it is the only option that fixes the target-EPackage
coordinate as a side effect rather than as extra work, and it makes this endpoint resolve objects
the way `(scope, registry, stage, objectId)` is resolved everywhere else in the API. **T-D** is the
more elegant answer to the user-facing question and should be preferred if the "which stage is my
config in?" burden turns out to matter in practice — but it should not be adopted while its
unambiguity rests on `delete.after.transition` being set.

Whichever stage option wins here, it should also settle the OCL question the same way, or the two
endpoints will explain the same URL segment differently.

## Questions for the decision

1. Should a `DataGenConfig` be runnable while it is in `draft`? (Same question as for OCL
   constraint sets — and the answer should be the same for both.)
2. Should generating against a **draft schema** be possible? If yes, the stage cannot be dropped
   from the request (T-A is out) and the `ResourceSet` binding has to change.
3. Is one datagen registry per scope guaranteed, or should the registry stay nameable per request?
4. Does the `POST /datagen` endpoint move under `/{scopeName}/stages/{stageName}/` as well? It is
   the only way its EPackage resolution stops depending on which `ResourceSet` happened to bind —
   but it means a body-only endpoint carrying two path segments it uses only indirectly.
5. Is anyone calling `GET /datagen/{objectId}` today? (It only ever worked in a scope named
   `jena`, so the blast radius of changing the path is probably zero.)

## Related

- **F54** — the review finding this came out of.
- **F52 / [OCL_CONSTRAINT_STAGE_ISSUE.md](OCL_CONSTRAINT_STAGE_ISSUE.md)** — the same stage
  question for OCL constraint sets. Its option C ("resolve from the stage that was asked for")
  is this sketch's T-B.
- **F44** — `registryView(registry, stage)` was unimplemented server-side; T-B, T-C and T-E need
  it. Implemented 2026-08-06.
- **#156** — the flat-nsURI audit. `resolvePackages` looking an nsURI up in a single
  `ResourceSet`'s package registry is precisely the flat-nsURI consumer pattern that audit
  described; whichever option is chosen should make this lookup scope-aware.
- **F112** (info) — the untargeted `@Reference ResourceSet`. Fixed as a side effect by the
  recommended stage option; fixable on its own with a target filter if the decision goes elsewhere.
- **F75** (minor) — a missing object answers **204 with no body** where the rest of the API answers
  404. **F76** (minor) — `resolvePackages` checks for `#` and then skips three characters assuming
  `#//`. Both live in the code any of these options rewrite, so they are worth folding into
  whichever change lands.
- Leftover not yet filed: an unknown scope NPEs into a 500 (should be 404), and both endpoints
  catch bare `Exception` and return the exception message as the entity — the pattern F30/F31
  removed elsewhere.
