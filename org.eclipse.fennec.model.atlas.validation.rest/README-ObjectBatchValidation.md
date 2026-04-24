# Object Batch Validation Resource

The Object Batch Validation Resource exposes endpoints for validating and filtering collections of EMF model objects (EObjects) against a C-OCL constraint set. All endpoints are rooted at `/{scopeName}/{stageName}/validate/batch` and accept and produce `application/xmi` or `application/json`.

The C-OCL constraint set is resolved at request time using the `coclId` supplied in the request body. The service finds the registry of type `COCL` in the given scope and fetches the object from its final stage. The constraint set must be pre-loaded into that registry before any batch request is made.

The examples below use scope `jena` and stage `release`, and the `dge` example model (namespace URI `https://dg.de/1.0`) which defines `Company`, `Person`, and `Address`.

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/{scopeName}/{stageName}/validate/batch` | Validate a collection of EObjects against their model constraints and a C-OCL constraint set |
| `POST` | `/{scopeName}/{stageName}/validate/batch/filter` | Filter a collection of EObjects using the REFERENCE_FILTER constraints in a C-OCL constraint set |

### Optional query parameter

Both endpoints accept an optional `mediaType` query parameter that overrides the `Accept` header to control the response content type:

```
POST /jena/release/validate/batch?mediaType=application/json
```

---

## POST /{scopeName}/{stageName}/validate/batch

Validates each EObject in the collection against:
1. The EMF model's own constraints (via `Diagnostician`)
2. Every active `VALIDATION` constraint in the referenced `OclConstraintSet`

An optional inline `filterConstraint` of role `REFERENCE_FILTER` can be supplied in the request body. When present, each object is evaluated against it first; objects that do not pass are skipped (recorded as filtered-out in the diagnostic, but not validated further).

**Request body:** `BatchValidationRequest` serialized as XMI

| Field | Description |
|-------|-------------|
| `validationObjects` | One or more EObjects to validate (containment, multi-valued) |
| `coclId` | Identifier of the `OclConstraintSet` in `jena/cocl/release` |
| `filterConstraint` | Optional inline `OclConstraint` with `role="REFERENCE_FILTER"` |

**Response:** `ValidationResponse` with `role: VALIDATION` and one `Diagnostic` entry per object in `diagnostics`. Each diagnostic wraps child diagnostics produced by the filter step, constraint evaluation, and the EMF `Diagnostician`.

---

### Example — validate two companies (no filter)

Suppose the `cocl` registry at stage `release` contains an `OclConstraintSet` with id `company-rules` that declares a VALIDATION constraint requiring `not self.name.oclIsUndefined()`.

```xml
<?xml version="1.0" encoding="ASCII"?>
<cocl:BatchValidationRequest xmi:version="2.0"
  xmlns:xmi="http://www.omg.org/XMI"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:cocl="http://www.gme.org/cocl/1.0"
  xmlns:dge="https://dg.de/1.0"
  coclId="company-rules">
  <validationObjects xsi:type="dge:Company" name="Acme">
    <address street="Main St" city="Springfield" zipCode="12345" country="US"/>
  </validationObjects>
  <validationObjects xsi:type="dge:Company"/>
</cocl:BatchValidationRequest>
```

```
POST /jena/release/validate/batch
Content-Type: application/xmi
Accept: application/json
```

**Response 200**

The first company (`Acme`) passes the name constraint. The second company has a `null` name and fails.

```json
{
  "role": "VALIDATION",
  "results": [],
  "diagnostics": [
    {
      "type": "INFO",
      "data": ["org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl (name: Acme)"],
      "children": [
        {
          "type": "INFO",
          "source": "org.eclipse.emf.ecore",
          "message": ""
        }
      ]
    },
    {
      "type": "ERROR",
      "data": ["org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl (name: null)"],
      "children": [
        {
          "type": "ERROR",
          "source": "https://dg.de/1.0#//Company",
          "message": "Constraint CompanyNameNotNull failed for EObject"
        },
        {
          "type": "INFO",
          "source": "org.eclipse.emf.ecore",
          "message": ""
        }
      ]
    }
  ]
}
```

The `type` on each top-level diagnostic reflects the worst severity among its children.

---

### Example — validate with an inline filter constraint

Here we use an inline `filterConstraint` to skip companies that have no address before applying the VALIDATION constraints. Only objects that pass the filter are validated.

```xml
<?xml version="1.0" encoding="ASCII"?>
<cocl:BatchValidationRequest xmi:version="2.0"
  xmlns:xmi="http://www.omg.org/XMI"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:cocl="http://www.gme.org/cocl/1.0"
  xmlns:dge="https://dg.de/1.0"
  coclId="company-rules">
  <validationObjects xsi:type="dge:Company" name="Acme">
    <address street="Main St" city="Springfield" zipCode="12345" country="US"/>
  </validationObjects>
  <validationObjects xsi:type="dge:Company" name="NoAddress"/>
  <filterConstraint
    name="HasAddress"
    contextClass="https://dg.de/1.0#//Company"
    expression="not self.address.oclIsUndefined()"
    role="REFERENCE_FILTER"
    active="true"/>
</cocl:BatchValidationRequest>
```

**Response 200**

`Acme` passes the filter and is validated normally. `NoAddress` is filtered out and recorded with an `INFO` diagnostic; it is not evaluated against VALIDATION constraints.

```json
{
  "role": "VALIDATION",
  "results": [],
  "diagnostics": [
    {
      "type": "INFO",
      "data": ["org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl (name: Acme)"],
      "children": [
        {
          "type": "INFO",
          "source": "org.eclipse.emf.ecore",
          "message": ""
        }
      ]
    },
    {
      "type": "INFO",
      "data": ["org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl (name: NoAddress)"],
      "children": [
        {
          "type": "INFO",
          "source": "HasAddress",
          "message": "Object was filtered out by filter constraint"
        }
      ]
    }
  ]
}
```

**Error responses**

| Code | Reason |
|------|--------|
| 400 | No objects provided, `coclId` missing, `coclId` not found in registry, no active VALIDATION constraints in the set, or `filterConstraint` has a role other than `REFERENCE_FILTER` |
| 404 | The `jena` scope service is not available |
| 415 | Unsupported media type |
| 500 | Unexpected server error |

---

## POST /{scopeName}/{stageName}/validate/batch/filter

Evaluates every active `REFERENCE_FILTER` constraint in the referenced `OclConstraintSet` against each object. Objects that fail any filter constraint are excluded from the result; objects that pass all constraints are retained.

**Request body:** `BatchValidationRequest` serialized as XMI (same structure as `/{scopeName}/{stageName}/validate/batch`, `filterConstraint` field is ignored)

**Response:** `ValidationResponse` with `role: REFERENCE_FILTER` and one `EObjectValidationResult` entry per object in `results`. A retained object has a non-empty `values` list; a filtered-out object has an empty `values` list. Each result also carries a `diagnostics` list explaining the outcome for that object.

**204 No Content** is returned (with no body) in two cases:
- The constraint set contains no active `REFERENCE_FILTER` constraints — nothing to filter
- All objects pass every filter constraint and the collection is unchanged

---

### Example — filter companies by address

`OclConstraintSet` `company-filter` contains one active `REFERENCE_FILTER` constraint: `not self.address.isOclUndefined()`.

```xml
<?xml version="1.0" encoding="ASCII"?>
<cocl:BatchValidationRequest xmi:version="2.0"
  xmlns:xmi="http://www.omg.org/XMI"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:cocl="http://www.gme.org/cocl/1.0"
  xmlns:dge="https://dg.de/1.0"
  coclId="company-filter">
  <validationObjects xsi:type="dge:Company" name="Acme">
    <address street="Main St" city="Springfield" zipCode="12345" country="US"/>
  </validationObjects>
  <validationObjects xsi:type="dge:Company" name="Empty Corp"/>
</cocl:BatchValidationRequest>
```

```
POST /jena/release/validate/batch/filter
Content-Type: application/xmi
Accept: application/json
```

**Response 200 — some objects filtered**

`Acme` passes (has an address, `values` is populated). `Empty Corp` fails (no address, `values` is empty).

```json
{
  "role": "REFERENCE_FILTER",
  "results": [
    {
      "eClass": "EObjectValidationResult",
      "values": [
        {
          "eClass": "dge:Company",
          "name": "Acme",
          "address": { "street": "Main St", "city": "Springfield", "zipCode": "12345", "country": "US" }
        }
      ],
      "diagnostics": [
        {
          "type": "INFO",
          "data": ["org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl (name: Acme)"],
          "children": []
        }
      ]
    },
    {
      "eClass": "EObjectValidationResult",
      "values": [],
      "diagnostics": [
        {
          "type": "INFO",
          "data": ["org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl (name: Empty Corp)"],
          "children": [
            {
              "type": "INFO",
              "source": "HasAddress",
              "message": "Object was filtered out by filter constraint"
            }
          ]
        }
      ]
    }
  ],
  "diagnostics": []
}
```

**Response 204 — all objects retained**

When every object passes all filter constraints the collection is unchanged and the server returns `204 No Content` with no body.

**Response 204 — no REFERENCE_FILTER constraints**

When the referenced `OclConstraintSet` contains no active `REFERENCE_FILTER` constraints there is nothing to filter; the server returns `204 No Content` with no body.

**Error responses**

| Code | Reason |
|------|--------|
| 400 | No objects provided, `coclId` missing, `coclId` not found, or the constraint set is not applicable to the provided object types |
| 404 | The `jena` scope service is not available |
| 415 | Unsupported media type |
| 500 | Unexpected server error |

---

## C-OCL Constraint Set format

Both endpoints resolve the `OclConstraintSet` identified by `coclId` at request time. The service finds the one registry of type `COCL` in the given scope and fetches the object from its final stage. The constraint set must be uploaded before any batch request is made.

The relevant constraint roles are:

| Role | Used by |
|------|---------|
| `VALIDATION` | `/{scopeName}/{stageName}/validate/batch` — applied to each object (or each filtered object) |
| `REFERENCE_FILTER` | `/{scopeName}/{stageName}/validate/batch/filter` — determines which objects to retain; also used as the inline `filterConstraint` role in `/{scopeName}/{stageName}/validate/batch` |

Only constraints with `active="true"` are evaluated.
