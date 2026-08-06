# Issue sketch — which stage do OCL constraint sets come from?

*Draft for a GitHub issue. Written 2026-08-06 out of quality-review finding F52; the finding's
original diagnosis ("stageName is ignored") was wrong, this is what is actually behind it.*

---

## Summary

The validation endpoints take a stage in their path — `POST /{scopeName}/{stageName}/validate/...` —
and that stage decides which `ResourceSet` the request body is deserialized and type-resolved
with. It does **not** decide where the OCL constraint set comes from: constraint sets are always
read from the COCL registry's **final** stage.

So a caller can ask for `draft` and have their draft-typed object checked against `release`
constraints, without anything in the response saying so.

## What happens today

`ValidationServiceImpl.resolveConstraintSet` (ValidationServiceImpl.java:380-392) resolves the
scope's COCL registry and then reads the constraint set through the final-stage accessor:

```java
private OclConstraintSet resolveConstraintSet(String oclId, ReadableScopeService<?> scopeService) {
    ScopeInfo scope = scopeService.getScopeInfo();
    RegistryInfo coclRegistry = scope.getRegistries().stream()
            .filter(r -> RegistryType.COCL == r.getType())
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No COCL registry found in scope: " + scope.getName()));

    Object oclObject = scopeService.get(coclRegistry.getName(), oclId).orElse(null);   // <-- final stage
    ...
}
```

`ReadableScopeService.get(registry, objectId)` is by contract the registry's *final-stage* read;
until 2026-08-06 the interface had no working stage-explicit read at all — `registryView(registry, stage)`
threw `UnsupportedOperationException` server-side (F44, now fixed), which is why the lookup was written
this way.

Five of the six validation endpoints go through it:

| Endpoint | Constraint set from |
|---|---|
| `POST /{scope}/{stage}/validate` | — (plain EMF `Diagnostician`, no OCL) |
| `POST /{scope}/{stage}/validate/{oclId}` | path `oclId` |
| `POST /{scope}/{stage}/validate/derive` | `?oclId=` query parameter |
| `POST /{scope}/{stage}/validate/compute` | `request.coclId` |
| `POST /{scope}/{stage}/validate/batch` | `request.coclId` |
| `POST /{scope}/{stage}/validate/batch/filter` | `request.coclId` |

## Why it matters

The COCL registry is configured with a full workflow, not a single stage. From
`runtime.config.local.jena/configs/workflow.json` (and the identical docker config):

```json
"RegistryService~ocl": {
  "registry.type": "COCL",
  "registry.name": "cocl",
  "stages": [ { "name": "draft",   "writable": true, "final": false },
              { "name": "release", "writable": true, "final": true  } ],
  "workflow.transitions": [ "draft:release" ],
  ...
}
```

Two consequences:

1. **The COCL `draft` stage is write-only.** A constraint set can be uploaded to `draft` and
   transitioned to `release`, but there is no way to *run* it while it is in `draft` — so a
   constraint cannot be tried out before it is released.
2. **The stage in the URL means two different things** in one request: the payload's schema
   context (honoured) and, a caller may reasonably assume, the constraint set's stage (ignored).
   Nothing in the response reveals which constraints actually ran.

## Options

### A. Document the current behaviour, change nothing

Say in the OpenAPI description and the service javadoc that `{stageName}` selects the model
context for the payload and that constraint sets always resolve from the COCL registry's final
stage.

```
POST /jena/draft/validate/my-rules      → 200, ran the *release* copy of "my-rules" (documented)
```

- **for:** zero risk, no behaviour change, no client impact.
- **against:** the draft stage of the COCL registry stays useless; the surprise is written down
  rather than removed.
- **effort:** hours.

### B. Reject any stage that is not the COCL registry's final stage

Pass `stageName` into the `ValidationService` methods and fail fast when it is not final.

```
POST /jena/release/validate/my-rules    → 200
POST /jena/draft/validate/my-rules      → 400 "Stage 'draft' is not the final stage of registry 'cocl'"
```

- **for:** the contract becomes unambiguous; no silent mismatch.
- **against:** it also forbids validating **draft-typed data**, because the same path segment
  selects the payload's `ResourceSet` — "check it before promoting it" is arguably the main use
  case for validation. It cements the write-only draft stage. Note it must be applied only to the
  five OCL endpoints, or the plain `/validate` (Diagnostician-only) breaks too.
- **effort:** small (signature change + check).
- **breaking:** yes, for anyone validating against a non-final stage today.

### C. Resolve the constraint set from the stage that was asked for

Make the stage mean the same thing throughout the request.

```java
private OclConstraintSet resolveConstraintSet(String oclId, ReadableScopeService<?> scopeService, String stage) {
    RegistryInfo cocl = /* as today */;
    Object oclObject = scopeService.registryView(cocl.getName(), stage).get(oclId).orElse(null);
    ...
}
```

```
POST /jena/draft/validate/my-rules      → 200, ran the *draft* copy of "my-rules"
POST /jena/release/validate/my-rules    → 200, ran the *release* copy
POST /jena/draft/validate/only-released → 404 "No OclConstraintSet 'only-released' in stage 'draft' of registry 'cocl'"
```

- **for:** one meaning for the stage; consistent with every other endpoint in the API, which all
  resolve objects by `(scope, registry, stage, objectId)`; makes the COCL draft stage usable, so
  constraints can be tested before release.
- **against:** behaviour change — a caller who asks for `draft` today gets release constraints and
  would afterwards get a 404 unless the set exists in draft.
- **depends on:** `registryView(registry, stage)` being implemented server-side (F44) — **done 2026-08-06**,
  so this is now a small change.
- **effort:** small.
- **breaking:** yes, for callers relying on today's fallback.

### D. Resolve from the requested stage, fall back to the final stage

As C, but when the constraint set is not in the requested stage, read the final-stage copy instead
— the same read-through idiom the scope hierarchy already uses.

```
POST /jena/draft/validate/my-rules      → 200, draft copy if present, else the release copy
```

- **for:** no breakage; draft constraints become usable when they exist.
- **against:** a silent fallback — the caller can believe they are testing draft rules while
  release rules run. If chosen, the resolved stage should be reported back (response field or a
  header) so the caller can tell which rules ran.
- **effort:** small (C plus a fallback).
- **breaking:** no.

### E. Separate the two coordinates

Keep the path stage for the payload, add an optional parameter for the constraint stage,
defaulting to the COCL registry's final stage (today's behaviour).

```
POST /jena/draft/validate/my-rules                  → 200, release constraints (default, unchanged)
POST /jena/draft/validate/my-rules?constraintStage=draft → 200, draft constraints
```

- **for:** completely backward compatible, explicit, and the two independent concerns stop
  sharing one segment.
- **against:** more API surface; a caller who does not know about the parameter still gets the
  cross-stage behaviour, just now documented as a default.
- **effort:** small–medium (parameter through 5 endpoints + service signatures).
- **breaking:** no.

## Recommendation

**C**, once F44 (`registryView(registry, stage)`) is in — it removes the ambiguity instead of
documenting it, unlocks the configured-but-unusable draft stage, and matches how the rest of the
API resolves objects. **D** or **E** if backward compatibility for existing callers of
`/{scope}/<non-final>/validate/{oclId}` outweighs that; **B** only if the product decision is that
constraint sets are released artifacts exclusively, in which case the check should read "the COCL
registry has no stage *X*" rather than "*X* is not final", so `draft` starts working the moment a
constraint set is put there.

## Questions for the decision

1. Should a constraint set be runnable while it is in `draft`, or are constraints released
   artifacts only? (This is the question that picks the option.)
2. Is anyone today calling the validation endpoints with a non-final stage and relying on getting
   the final-stage constraints? That decides whether a breaking change is acceptable.
3. If the constraint stage can differ from the payload stage (D or E), should the response state
   which stage the constraints came from?

## Related

- **F44** — `registryView(registry, stage)` was unimplemented server-side; C and D need it. **Implemented
  2026-08-06**, so neither option is blocked any more.
- **F52** — the review finding this came out of. Its original diagnosis (the stage segment is
  ignored and validation runs against a default) is wrong: the stage *is* consumed by
  `ScopedResourceSetProvider` for the `ResourceSet`. The report row has been corrected.
- Leftovers from F52, independent of this decision: the unread `@PathParam("stageName")` field in
  both validation resources (check the generated OpenAPI before deleting it — the path parameter
  may be derived from it), and a comment at `resolveConstraintSet` recording whichever rule wins.
