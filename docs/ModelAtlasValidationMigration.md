# Model Atlas Validation — Migration & Refactoring Log

This document tracks the ongoing refactoring of the validation layer, recording
decisions made, changes already applied, and the next planned steps.

---

## Context

The validation endpoints (`ObjectValidationResource`, `ObjectBatchValidationResource`)
were originally part of `org.eclipse.fennec.model.atlas.rest.application`. The goal
of this refactoring is to:

1. Move them to their own bundle family (`validation.rest`, `validation.rest.tests`).
2. Extract a reusable `ValidationService` API decoupled from the REST layer.
3. Make validation scope/stage-aware, so the correct resource set and C-OCL
   constraint sets are resolved per request context.

---

## Step 1 — Extract validation REST bundle (done)

**Bundles created:**
- `org.eclipse.fennec.model.atlas.validation.rest` — JAX-RS resources
- `org.eclipse.fennec.model.atlas.validation.rest.tests` — OSGi integration tests

**Resources moved (rename + package change):**
- `ObjectValidationResource` — path `/validate`
- `ObjectBatchValidationResource` — path `/validate/batch`

**Tests moved:**
- `ObjectValidationResourceTest`
- `ObjectBatchValidationResourceTest`

### Fix: test OSGi runtime not starting

The tests were not coming up because `validation.rest.tests` was missing the OSGi
Configurator setup that `rest.tests` already had. Three things were added:

**`bnd.bnd`:**
```
-includeresource: \
    OSGI-INF/configurator/=configs/

Require-Capability: osgi.extender;filter:='(osgi.extender=osgi.configurator)'
```

**`configs/config.json`** (new file — identical to the one in `rest.tests`):
```json
{
  ":configurator:resource-version": 1,
  "org.apache.felix.http~testHttp": {
    "org.osgi.service.http.port": "8185",
    "org.osgi.service.http.host": "localhost",
    "org.apache.felix.http.context_path": "/",
    "org.apache.felix.http.name": "Model Atlas Test HTTP",
    "org.apache.felix.http.runtime.init.id": "testHttp"
  },
  "JakartarsServletWhiteboardRuntimeComponent~testRest": {
    "jersey.jakartars.whiteboard.name": "Model Atlas Test REST",
    "jersey.context.path": "rest",
    "name": "modelatlas-test",
    "osgi.http.whiteboard.target": "(id=testHttp)"
  }
}
```

**`test.bndrun`:**
- Added `org.apache.felix.configurator` and `org.apache.felix.cm.json` to
  `-runbundles` and `-runrequires`.
- Added `-runproperties` with `gosh.args=--nointeractive` and logging settings.

---

## Step 2 — RegistryType enum (done)

The `Registry` model class in `workflow-api.ecore` had a `schemaRegistry: EBoolean`
attribute used to distinguish schema registries from others. This was replaced with a
`type: RegistryType` enum to support the upcoming C-OCL registry convention.

### Ecore change

**Removed:**
```xml
<eStructuralFeatures xsi:type="ecore:EAttribute" name="schemaRegistry"
    eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EBoolean"/>
```

**Added:**
```xml
<eStructuralFeatures xsi:type="ecore:EAttribute" name="type"
    eType="#//RegistryType" defaultValueLiteral="OTHER"/>

<eClassifiers xsi:type="ecore:EEnum" name="RegistryType">
  <eLiterals name="OTHER"  value="0"/>  <!-- default, no migration needed -->
  <eLiterals name="SCHEMA" value="1"/>  <!-- holds EPackage definitions -->
  <eLiterals name="COCL"   value="2"/>  <!-- holds OclConstraintSet objects -->
</eClassifiers>
```

The model code was regenerated from the ecore.

### Code changes

| File | Change |
|---|---|
| `RegistryServiceConfig.java` | `boolean schema_registry() default false` → `String registry_type() default "OTHER"` |
| `RegistryServiceImpl.java` | `registry.setSchemaRegistry(config.schema_registry())` → `registry.setType(RegistryType.get(config.registry_type()))` |
| `AtlasSchemaRegistryService.java` | Service property `schema.registry:Boolean=true` → `registry.type=SCHEMA`; `setSchemaRegistry(true)` → `setType(RegistryType.SCHEMA)` |
| `ScopeServiceImpl.java` | 4× `isSchemaRegistry()` → `RegistryType.SCHEMA == getRegistry().getType()` |

### Config JSON changes

All `RegistryService~schema` blocks across all four runtime configs gained:
```json
"registry.type": "SCHEMA"
```

The `RegistryService~ocl` block in `runtime.config.local.jena` gained:
```json
"registry.type": "COCL"
```

The other runtime configs (local, docker-apicurio, docker-file) do not yet have a
COCL registry — this will be added when those environments need validation support.

### Test changes

- `TestAnnotations.java` (workflow, rest, validation.rest test bundles):
  `@Property(key="schema.registry", value="true", scalar=Boolean)` →
  `@Property(key="registry.type", value="SCHEMA")`
- `AtlasSchemaRegistryServiceIntegrationTest.java`:
  OSGi filter `(schema.registry=true)` → `(registry.type=SCHEMA)`
- `AtlasScopeServiceIntegrationTest.java`, `AtlasSchemaRegistryServiceIntegrationTest.java`,
  `AtlasSchemaRegistryServiceTest.java`:
  `assertTrue(registry.isSchemaRegistry())` → `assertEquals(RegistryType.SCHEMA, registry.getType())`

---

## Steps 3 + 4 — ValidationService API + scope/stage-aware endpoints (done)

These two steps were implemented together because the service interface signature
depends directly on the scope/stage path design.

### What changed

**New files:**
- `org.eclipse.fennec.model.atlas.validation/src/.../ValidationService.java` — public interface
- `org.eclipse.fennec.model.atlas.validation/src/.../impl/ValidationServiceImpl.java` — PROTOTYPE DS component

**`ValidationService` interface:**

```java
// scope-agnostic
Diagnostic validate(EObject eObject);
ValidationResponse compute(OperationValidationRequest request);

// scope-aware (ResourceSet passed as parameter — see ResourceSet design note)
ValidationResponse validateWithOcl(EObject eObject, String oclId, String scopeName, ResourceSet resourceSet);
ValidationResponse derive(DerivedValidationRequest request, String oclId, String scopeName, ResourceSet resourceSet);
ValidationResponse validateBatch(BatchValidationRequest request, String scopeName, ResourceSet resourceSet);
ValidationResponse filterBatch(BatchValidationRequest request, String scopeName, ResourceSet resourceSet);
```

**`ValidationServiceImpl`:**
- `ServiceScope.PROTOTYPE` — holds a `PROTOTYPE_REQUIRED` `OclEngine`
- `@Reference ScopeServiceCollector scopeCollector`
- Resolves the COCL registry by finding the first `Registry` with `RegistryType.COCL`
  in the scope's registry list, then finds its final stage
- Throws `IllegalArgumentException` for bad input (→ HTTP 400)
- Throws `NoSuchElementException` for not-found (→ HTTP 404)
- `filterBatch` returns `null` when all objects are retained or no filter constraints
  exist; the REST layer maps `null` → HTTP 204

**ResourceSet design:**
The `ResourceSet` is passed as a method parameter rather than injected into the service.
This decouples the service from the ResourceSet resolution strategy: callers (REST
resources) currently pass the globally injected `ResourceSet`, but can in the future
pass a scope/stage-aware ResourceSet without any API change.

**Path restructuring:**

| Before | After |
|---|---|
| `POST /validate` | `POST /{scopeName}/{stageName}/validate` |
| `POST /validate/{oclId}` | `POST /{scopeName}/{stageName}/validate/{oclId}` |
| `POST /validate/derive` | `POST /{scopeName}/{stageName}/validate/derive` |
| `POST /validate/compute` | `POST /{scopeName}/{stageName}/validate/compute` |
| `POST /validate/batch` | `POST /{scopeName}/{stageName}/validate/batch` |
| `POST /validate/batch/filter` | `POST /{scopeName}/{stageName}/validate/batch/filter` |

**Two-axis design:**

1. **C-OCL axis** — `scopeName` is sufficient. The service finds the one registry
   with `RegistryType.COCL` in that scope and uses its final stage. No extra
   parameter needed.

2. **ResourceSet axis** — `stageName` is captured as a path param in the REST
   resources for future scope-aware ResourceSet resolution. Currently it is not
   forwarded to the service; the globally injected `ResourceSet` is used instead.

**`bnd.bnd` changes:**

| Bundle | Change |
|---|---|
| `validation` | Added `workflow`, `ocl.api`, `ocl.model` to buildpath; added `Private-Package` for `impl` |
| `validation.rest` | Removed `workflow`, `ocl.api`, `ocl.model`, `management` from buildpath |

**Test changes:**
- `CoclRegistryServiceSetup` in `validation.rest.tests/TestAnnotations.java` gained
  `registry.type=COCL` so the service can find the registry by type
- All test URL paths updated with `jena/release/` prefix

---

## Next steps (planned, not yet implemented)

### Step 5 — Scope/stage-aware ResourceSet

Wire `stageName` from the REST path into a ResourceSet resolved per `(scopeName, stageName)`,
replacing the current globally injected ResourceSet. This requires infrastructure for
per-scope EPackage visibility that does not yet exist. The `ValidationService` API is
already designed to accept this without changes.
