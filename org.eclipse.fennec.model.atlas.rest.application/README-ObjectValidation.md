# Object Validation Resource

The Object Validation Resource exposes endpoints for validating and computing derived properties of EMF model objects (EObjects). All endpoints are rooted at `/validate` and accept and produce `application/xmi` or `application/json`.

The examples below use the `dge` example model (namespace URI `https://dg.de/1.0`) which defines `Company`, `Person`, and `Address`.

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/validate` | Validate an EObject against its EMF model constraints |
| `POST` | `/validate/{oclId}` | Validate an EObject against its model and a C-OCL constraint set |
| `POST` | `/validate/derive` | Compute one or more derived structural features of an EObject |
| `POST` | `/validate/compute` | Invoke an EOperation on an EObject |

### Optional query parameter

All endpoints accept an optional `mediaType` query parameter that overrides the `Accept` header to control the response content type:

```
POST /validate?mediaType=application/xmi
```

---

## POST /validate

Validates an EObject against the constraints declared in its EMF model. Returns a `Diagnostic` describing the outcome.

**Request body:** XMI-serialized EObject (`Content-Type: application/xmi`)

**Example — validate a Person**

A `Person` in the `dge` model has a constraint `ValidPhoneNumber` requiring a 10-digit phone number. Sending a person with an invalid number:

```xml
<?xml version="1.0" encoding="ASCII"?>
<dge:Person xmi:version="2.0"
  xmlns:xmi="http://www.omg.org/XMI"
  xmlns:dge="https://dg.de/1.0"
  firstName="Jane" lastName="Doe"
  phone="123"/>
```

```
POST /validate
Content-Type: application/xmi
Accept: application/json
```

**Response 200 — validation found issues**

```json
{
  "type": "ERROR",
  "source": "org.eclipse.emf.ecore",
  "message": "The constraint 'ValidPhoneNumber' is violated on 'Jane Doe'",
  "children": [],
  "data": []
}
```

**Response 200 — object is valid**

When no constraints are violated the diagnostic carries severity `INFO` and an empty message from the EMF `Diagnostician`.

**Error responses**

| Code | Reason |
|------|--------|
| 415 | The `mediaType` query parameter names an unsupported type |
| 500 | Unexpected server error |

---

## POST /validate/{oclId}

Validates an EObject against both its EMF model constraints and an additional C-OCL constraint set stored in the `jena` scope registry.

**Path parameter:** `oclId` — the identifier of the `OclConstraintSet` to apply

**Request body:** XMI-serialized EObject

**Response:** `ValidationResponse` containing a list of `Diagnostic` entries and the role `VALIDATION`.

**Error responses**

| Code | Reason |
|------|--------|
| 400 | The `oclId` was not found, or the constraint set cannot handle the supplied EObject type |
| 404 | The `jena` scope service is not available |
| 415 | Unsupported media type |
| 500 | Unexpected server error |

---

## POST /validate/derive

Computes one or more structural features of an EObject using the EMF model's own implementation (e.g. derived attributes, references).

The request body is a `DerivedValidationRequest` containing:
- `validationObjects` — exactly one EObject to evaluate
- `derivedFeature` — one or more `EStructuralFeature` references (non-containment cross-references to the feature definitions in the model)

The response is a `ValidationResponse` with role `DERIVED`. Each requested feature produces one entry in `results`:
- If the feature type is an `EClass` → `EObjectValidationResult` with the values list populated (supports multi-valued references)
- If the feature type is an `EDataType` → `SimpleValidationResult` with the value serialized to its canonical string form via the EDataType factory

### Constructing the request

The `derivedFeature` list holds cross-references to `EStructuralFeature` instances defined in a registered EPackage. In XMI these serialize as `href` attributes pointing to the feature's URI within its package.

**Example — compute the `fullName` derived attribute on a Person**

The `dge` model defines `Person.fullName` as a derived `EString` feature (concatenation of `firstName` and `lastName`).

```xml
<?xml version="1.0" encoding="ASCII"?>
<cocl:DerivedValidationRequest xmi:version="2.0"
  xmlns:xmi="http://www.omg.org/XMI"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:cocl="http://www.gme.org/cocl/1.0"
  xmlns:dge="https://dg.de/1.0"
  xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore">
  <validationObjects xsi:type="dge:Person"
    firstName="Jane" lastName="Doe"/>
  <derivedFeature href="https://dg.de/1.0#//Person/fullName"/>
</cocl:DerivedValidationRequest>
```

```
POST /validate/derive
Content-Type: application/xmi
Accept: application/json
```

**Response 200**

```json
{
  "role": "DERIVED",
  "results": [
    {
      "eClass": "SimpleValidationResult",
      "value": "Jane Doe"
    }
  ],
  "diagnostics": [
    {
      "type": "INFO",
      "source": "fullName",
      "message": "Succesfully computed derived feature"
    }
  ]
}
```

**Example — compute a reference feature (`Company.address`)**

```xml
<?xml version="1.0" encoding="ASCII"?>
<cocl:DerivedValidationRequest xmi:version="2.0"
  xmlns:xmi="http://www.omg.org/XMI"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:cocl="http://www.gme.org/cocl/1.0"
  xmlns:dge="https://dg.de/1.0">
  <validationObjects xsi:type="dge:Company" name="Acme">
    <address street="Main St" city="Springfield" zipCode="12345" country="US"/>
  </validationObjects>
  <derivedFeature href="https://dg.de/1.0#//Company/address"/>
</cocl:DerivedValidationRequest>
```

**Response 200**

```json
{
  "role": "DERIVED",
  "results": [
    {
      "eClass": "EObjectValidationResult",
      "values": [
        {
          "eClass": "dge:Address",
          "street": "Main St",
          "city": "Springfield",
          "zipCode": "12345",
          "country": "US"
        }
      ]
    }
  ],
  "diagnostics": [
    {
      "type": "INFO",
      "source": "address",
      "message": "Succesfully computed derived feature"
    }
  ]
}
```

**Error responses**

| Code | Reason |
|------|--------|
| 400 | No validation object provided, more than one provided, no derived features in request, or a requested feature does not belong to the object's EClass |
| 415 | Unsupported media type |
| 500 | Unexpected server error |

---

## POST /validate/compute

Invokes an `EOperation` on an EObject and returns the result.

The request body is an `OperationValidationRequest` containing:
- `validationObjects` — exactly one EObject on which to invoke the operation
- `operation` — the `EOperation` to invoke, serialized as a **contained** element whose name must match an operation declared on the object's EClass; the signature (return type and parameter types) is validated before invocation
- `parameters` — zero or more `OperationRequestParameter` entries supplying argument values; each parameter carries either a string value (`javaValue`), an EObject value (`eValue`), or a null flag (`isNull`). The `javaValue` is converted to the correct Java type on the server using the corresponding `EParameter`'s `EDataType` factory (`createFromString`)

The operation name is matched against the operations of the object's EClass. The signature (return type and each parameter type) must match exactly.

The response is a `ValidationResponse` with role `OPERATION` and one entry in `results`:
- If the return type is an `EClass` → `EObjectValidationResult`
- If the return type is an `EDataType` → `SimpleValidationResult` with the value as a string produced by `EDataType.convertToString`. The client can reconstruct the typed value using `EDataType.createFromString` with knowledge of the operation's declared return type.

### Constructing the request

The `operation` element is containment: it is serialized inline. Its `eType` and parameter `eType` fields are non-containment cross-references to `EClassifier` instances in registered EPackages, serialized as `href` attributes.

**Example — invoke `getTotalEmployees()` on a Company**

`getTotalEmployees` takes no parameters and returns `EInt`.

```xml
<?xml version="1.0" encoding="ASCII"?>
<cocl:OperationValidationRequest xmi:version="2.0"
  xmlns:xmi="http://www.omg.org/XMI"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:cocl="http://www.gme.org/cocl/1.0"
  xmlns:dge="https://dg.de/1.0"
  xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore">
  <validationObjects xsi:type="dge:Company" name="Acme"/>
  <operation name="getTotalEmployees">
    <eType xsi:type="ecore:EDataType" href="http://www.eclipse.org/emf/2002/Ecore#//EInt"/>
  </operation>
</cocl:OperationValidationRequest>
```

```
POST /validate/compute
Content-Type: application/xmi
Accept: application/json
```

**Response 200**

```json
{
  "role": "OPERATION",
  "results": [
    {
      "eClass": "SimpleValidationResult",
      "value": "0"
    }
  ],
  "diagnostics": []
}
```

The value `"0"` is the string form of the integer `0` (the company has no employees). The client reconstructs it with `EcorePackage.eINSTANCE.getEFactoryInstance().createFromString(EcorePackage.Literals.EINT, "0")`.

**Example — invoke `findEmployeesByNamePrefix(namePrefix)` on a Company**

`findEmployeesByNamePrefix` takes one `EString` parameter and returns a list of `Person`.

```xml
<?xml version="1.0" encoding="ASCII"?>
<cocl:OperationValidationRequest xmi:version="2.0"
  xmlns:xmi="http://www.omg.org/XMI"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:cocl="http://www.gme.org/cocl/1.0"
  xmlns:dge="https://dg.de/1.0"
  xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore">
  <validationObjects xsi:type="dge:Company" name="Acme">
    <employees firstName="Jane" lastName="Doe"/>
    <employees firstName="John" lastName="Doe"/>
    <employees firstName="Alice" lastName="Smith"/>
  </validationObjects>
  <operation name="findEmployeesByNamePrefix">
    <eType xsi:type="ecore:EClass" href="https://dg.de/1.0#//Person"/>
    <eParameters name="namePrefix">
      <eType xsi:type="ecore:EDataType" href="http://www.eclipse.org/emf/2002/Ecore#//EString"/>
    </eParameters>
  </operation>
  <parameters javaValue="D"/>
</cocl:OperationValidationRequest>
```

**Response 200**

```json
{
  "role": "OPERATION",
  "results": [
    {
      "eClass": "EObjectValidationResult",
      "values": [
        { "eClass": "dge:Person", "firstName": "Jane", "lastName": "Doe" },
        { "eClass": "dge:Person", "firstName": "John", "lastName": "Doe" }
      ]
    }
  ],
  "diagnostics": []
}
```

**Error responses**

| Code | Reason |
|------|--------|
| 400 | No validation object, more than one object, no operation provided, operation name not found in the object's EClass, return type mismatch, parameter count mismatch, parameter type mismatch, or argument count does not match parameter count |
| 415 | Unsupported media type |
| 500 | Unexpected server error |
