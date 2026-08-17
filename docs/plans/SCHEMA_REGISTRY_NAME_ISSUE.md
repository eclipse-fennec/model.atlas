# Issue sketch — the schema registry has to be called "schema"

*Draft for a GitHub issue. Written 2026-08-07 out of quality-review finding F62. The finding is a
substring-match bug; behind it is a naming constraint nothing documents: a scope's schema registry
must be named literally `schema` or the whole `/{scopeName}/schema/**` API answers 400.*

---

## Summary

The REST layer identifies the schema registry by the **URL literal** `/schema`, then uses that
literal as the **registry name** when addressing the registry. The two are different things. A
deployment that names its schema registry anything else has a valid, correctly-typed scope whose
entire schema API is unreachable — and nothing in the configuration, the README or the OpenAPI
document says the name is load-bearing.

`RegistryType.SCHEMA` already exists for exactly this purpose, and parts of the codebase already
use it.

## What happens today

Four constants spell `"schema"`. They do **not** all mean the same thing:

| Where | Meaning | Correct? |
|---|---|---|
| `RemoteEPackageProviderImpl.java:69` | the URL path segment, documented as such | ✅ yes — it builds `/{scope}/schema/…` URLs |
| `ModelAtlasRequestFilter.java:83` | a **registry name**, derived from the URL segment | ❌ conflates the two |
| `SchemaPackagesResource.java:113` | the **registry name** every read and write addresses | ❌ conflates the two |
| `ScopeAggregateService.java:57` | a **registry name**, compared against `md.getRegistry()` | ❌ conflates the two |

### 1. The filter turns the URL literal into a registry name, then validates it

```java
private String resolveRegistryName(ContainerRequestContext requestContext,
        MultivaluedMap<String, String> pathParams) {
    String registryName = pathParams.getFirst("registryName");
    if (registryName != null) {
        return registryName;
    }
    String path = requestContext.getUriInfo().getPath();
    if (path.contains("/schema")) {              // <-- F62: substring, not segment
        return SCHEMA_REGISTRY_NAME;             // <-- "schema"
    }
    return null;
}
```

and then, in `filter(...)`:

```java
if (registryName != null && !scopeService.isValidRegistry(registryName)) {
    throw new WebApplicationException(Response.status(BAD_REQUEST)
            .entity(String.format("Registry [%s] is not available for scope [%s].", registryName, scopeName)).build());
}
```

So if the scope's schema registry is called `models`, every request to `/{scope}/schema/**` is
rejected with **400 `Registry [schema] is not available for scope [X]`** — before the resource is
even reached. The API is not degraded, it is off.

### 2. F62 proper: `contains` where a path segment is meant

`path.contains("/schema")` also matches paths where `schema` merely *starts* a segment. Any path
without a `registryName` parameter qualifies — for example
`/{scope}/stages/schemaDraft/convert` resolves the registry to `schema` and gets validated against
it, which is either a spurious 400 or a check against the wrong registry. This part is a plain bug
and needs no decision (see "separable" below).

### 3. The aggregate's identity semantics change with the name

`ScopeAggregateService` decides how an object is identified in the manifest by comparing the
registry name:

```java
String identity = SCHEMA_REGISTRY.equals(md.getRegistry()) && nsUri != null ? nsUri.toString()
        : md.getObjectId();
```

Rename the registry and schema packages are identified by objectId instead of nsURI in the
manifest and the aggregate ETag — quietly, with no error. This is the subtlest of the three,
because everything keeps working while meaning something different.

## The type is already there

`RegistryInfo.getType()` returns a `RegistryType` — `OTHER`, `SCHEMA`, `COCL` — and the config sets
it (`workflow.json:17`, `"registry.type": "SCHEMA"`). Two places already resolve by type rather
than name:

- `SchemaRegistryChainConfigurator.java:137,199` — `.filter(r -> RegistryType.SCHEMA.equals(r.getType()))`
- `ValidationServiceImpl.java:380-392` — the same shape for `RegistryType.COCL`

So the codebase resolves the schema registry by type in the wiring layer and by name in the REST
layer. The inconsistency is the finding.

## Options

### A. Fix the substring match only

`contains("/schema")` → an exact path-segment comparison. Leaves the naming constraint in place.

- **for:** removes a real bug, no decision needed, no behaviour change for correct deployments.
- **against:** the "must be called schema" constraint remains, still undocumented.
- **effort:** minutes. **See "separable" — this can land independently of everything below.**

### B. Resolve the scope's SCHEMA-typed registry

Keep `/schema` as a URL literal; look the registry up by type, as validation does for COCL. Three
call sites: the filter, `SchemaPackagesResource`, `ScopeAggregateService`.

```java
RegistryInfo schema = scopeService.getScopeInfo().getRegistries().stream()
        .filter(r -> RegistryType.SCHEMA == r.getType())
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Scope '" + scopeName + "' has no schema registry"));
```

- **for:** the deployment names its registries freely; consistent with the wiring layer and with
  the COCL lookup; a scope with no schema registry gets an honest 404 instead of a confusing 400
  about a registry name the caller never mentioned.
- **against:** "first of type" is arbitrary if a scope has two SCHEMA registries — same objection
  as the `DATA_GEN` proposal in the [datagen sketch](DATAGEN_SCOPE_STAGE_ISSUE.md), and worth
  answering the same way for both. Every existing config must actually carry
  `"registry.type": "SCHEMA"` (the two `workflow.json` files do; the datagen registry does not,
  which is what that sketch is about).
- **effort:** small — three call sites, plus deciding what a 0-or-2 match answers.

### C. Make the name configurable

A `schema.registry.name` component property, default `schema`.

- **for:** no model or lookup change.
- **against:** one name per **runtime**, not per scope, so it fails as soon as two scopes disagree —
  and the type already encodes the intent better than a property would.

### D. Drop the special path

Serve packages through the generic `/{scopeName}/registries/{registryName}/**` endpoints and retire
`/{scopeName}/schema`.

- **for:** no conflation possible; one way to address any registry.
- **against:** the schema path exists because EPackages need package-specific operations (nsURI
  lookup, `/search`, version reconciliation) that the generic object endpoints do not have. This is
  a large API change for a naming problem.

### E. Make the constraint explicit instead of removing it

Document that the schema registry must be named `schema`, and validate it at startup: a scope with
a `SCHEMA`-typed registry under a different name fails to configure, loudly, instead of serving 400s.

- **for:** cheapest honest option; turns a silent trap into a startup error.
- **against:** enshrines a constraint the type system already makes unnecessary.

## Recommendation

**B, together with the `DATA_GEN` work in the [datagen sketch](DATAGEN_SCOPE_STAGE_ISSUE.md)** —
they are the same refactor ("resolve a registry by its type, not by its name") applied to two
registries, and the 0-or-2-matches question needs one answer for both, not two. **A** should land
first and separately whatever else is decided.

If B is rejected, then **E** — because the status quo is not "the name happens to be schema", it is
"the name is part of the API contract and nothing says so".

## Separable, needs no decision

The exact-segment fix (option A) is independent of the naming question and safe: it can only reduce
false matches. It is a candidate for the next minor sweep regardless of what the bosses decide.

## Questions for the decision

1. May a deployment name its schema registry something other than `schema`? (Picks B/C vs E.)
2. If a scope has **two** `SCHEMA`-typed registries, what should the schema API do — first match,
   or refuse to serve? (Same question as the datagen sketch's; answer once.)
3. Does the aggregate/manifest identity rule (nsURI vs objectId) belong to the registry's *type*
   rather than its name? It reads like it should — a `SCHEMA` registry holds EPackages, and those
   are identified by nsURI.

## Related

- **F62** — the review finding (the substring match).
- **F54 / [DATAGEN_SCOPE_STAGE_ISSUE.md](DATAGEN_SCOPE_STAGE_ISSUE.md)** — the same
  resolve-by-type question for the datagen registry, where the type literal does not exist yet.
- `SchemaRegistryChainConfigurator.java:137,199` and `ValidationServiceImpl.java:380-392` — the
  by-type lookups that already exist and that option B would mirror.
