# Model Atlas Schema API Specification

## 1. Core Concepts

### Model Atlas

A central registry for data schemas (EPackages). It acts as a canonical source of truth in a distributed system, storing all models as Ecore and allowing them to be retrieved in various formats (JSON, XML, XMI, etc.).

### Scope

A "tenant" or logical partition within the registry. Scopes are hierarchical and contain one or more registries.

- `atlas`: A special, read-only "system" scope that contains common base models (like Ecore itself). It is the default parent for any scope that does not specify one.
- **Custom Scopes:** (e.g., `my-tenant`, `global-corporate`). These are the primary partitions for schemas.

### Registry

A logical grouping within a scope that defines its own workflow stages and transitions. The `SchemaPackagesResource` uses a fixed registry named `"schema"` for all operations.

### Stage

A lifecycle state for a schema within a registry. Examples: `draft`, `review`, `approved`, `release`.

- Each stage has a `writable` flag indicating if modifications are allowed.
- Each stage has a `final` flag indicating if it's the released/published stage.
- Transitions between stages are explicitly configured via `allowedTransitions`.

### `nsUri` / `objectId`

The `nsUri` (Namespace URI) of an EPackage is the primary business key: **all package
lookups are based on the `nsUri`** (URL-encoded in query parameters), which every schema
source carries in the `nsUri` entry of the metadata `properties` map.

The `objectId` is a separate, **opaque** identifier assigned by the storage side (a random
UUID for packages uploaded through REST, a backend-defined value for git-backed or
system-registered packages). Never parse it, and never derive an `objectId` from an
`nsUri`. Endpoints that ask for an `objectId` (stage transitions) take the value from the
package's metadata.

**Identity is scoped to a stage.** An `objectId` — and, for a schema registry, an `nsUri` —
identifies exactly one object within a `(scope, registry, stage)` triple. The same
`objectId`/`nsUri` may exist in several stages of the same registry, and when it does it
denotes *the same logical object at two points of its lifecycle*: a transition copies the
object into the target stage unless the registry sets `delete.after.transition=true`, and a
new draft revision of a released object re-uses its id. Cross-stage uniqueness is therefore
deliberately not enforced; two genuinely different objects must use two different ids, and
two versions of a model must use two different namespace URIs.

### Scope Configuration (Backend)

Scopes and registries are configured via JSON configuration files (`workflow.json`). See [README-SchemaPackages.md](README-SchemaPackages.md) for configuration details.

### Hierarchical Visibility and Uniqueness

The hierarchy of scopes (defined by `parentScope`) governs visibility and uniqueness.

1. **Uniqueness (Write-Time):** When creating a new package, the system checks whether that `nsUri` already exists in the **target** scope/stage, falling back to the final stages of the ancestor scopes (inherited packages). If a conflict is found and `overwrite=false`, the creation is rejected with `409 Conflict`; with `overwrite=true` an inherited (read-only) package yields `403 Forbidden`. The other stages of the local scope are not part of the check.

2. **Visibility (Read-Time):** Any query for a package by its `nsUri` (or, where an endpoint takes one, its `objectId`) will search the hierarchy. The system first looks in the specified scope and stage. If not found, it proceeds to check the final stage of the parent scope, and so on up to `atlas`.

When a parent package is returned from a child scope query, it will be marked as read-only (`isReadOnly: true`).

## 2. Resource Models (JSON)

### Scope

```json
{
  "name": "my-tenant",
  "description": "Primary scope for MyTenant application.",
  "parentScope": "atlas",
  "registries": [
    {
      "name": "schema",
      "description": "Schema registry for EMF models",
      "stages": [
        { "name": "draft", "writable": true, "final": false },
        { "name": "approved", "writable": true, "final": false },
        { "name": "release", "writable": false, "final": true }
      ],
      "allowedTransitions": [
        { "fromStage": "draft", "toStage": "approved" },
        { "fromStage": "approved", "toStage": "release" }
      ]
    }
  ]
}
```

### ObjectMetadata (SchemaPackage metadata)

```json
{
  "objectId": "3f8a1c2e-9d4b-4f6a-8c1d-2e5b7a9c0d41",
  "objectName": "Billing",
  "scope": "my-tenant",
  "registry": "schema",
  "stage": "draft",
  "version": "1.0.0",
  "isReadOnly": false,
  "uploadTime": "2023-10-27T10:00:00Z",
  "lastChangeTime": "2023-10-27T10:00:00Z",
  "properties": { "nsUri": "http://example.com/schemas/billing/v1" }
}
```

- `objectId`: the package's opaque storage id — a random UUID for packages uploaded through
  REST, a backend-defined value for git-backed or system-registered ones. Unique within the
  package's `(scope, registry, stage)`; the same id reappears in the next stage after a
  transition, because it is the same package
- `properties.nsUri`: the namespace URI — the business key every schema source carries
- `isReadOnly`: `true` when the package is resolved from a parent scope

### ObjectMetadataContainer

```json
{
  "metadata": [
    { /* ObjectMetadata */ },
    { /* ObjectMetadata */ }
  ]
}
```

## 3. API Endpoints

### 3.1 Scopes

Resource for discovering available scopes.

#### `GET /scopes`

- **Action:** List all configured scopes.
- **Response:**
  - `200 OK`: `application/json` - `ScopeListResponse` containing list of `Scope` objects

#### `GET /scopes/{scopeName}`

- **Action:** Get metadata for a specific scope.
- **Response:**
  - `200 OK`: `application/json` - Single `Scope` object
  - `404 Not Found`: Scope does not exist

---

### 3.2 SchemaPackages

Resource for managing schemas within a scope. All endpoints use the fixed registry name `"schema"`.

**Base Path:** `/{scopeName}/schema`

#### `GET /{scopeName}/schema`

- **Action:** List all packages in the *final/released* stage for this scope. Respects hierarchical visibility, including packages from parent scopes' final stages.
- **Response:**
  - `200 OK`: `application/json` - `ObjectMetadataContainer` with list of `ObjectMetadata`
  - `204 No Content`: No schemas found
  - `400 Bad Request`: Scope not available
  - `500 Internal Server Error`: Unexpected error

#### `GET /{scopeName}/schema/stages/{stageName}`

- **Action:** List all packages within a *specific stage* of a scope.
- **Query Parameters:**
  - `nsUri` (string, optional): Find a single package by its exact namespace URI (URL-encoded). Respects hierarchical visibility. Returns single `ObjectMetadata` or `204 No Content`.
  - `name` (string, optional): Search by package name. Supports trailing wildcards (e.g., `name=Billing*`). **Note:** Leading wildcards are NOT supported. Returns list within local scope only (no hierarchy).
- **Response:**
  - `200 OK`: `application/json` - `ObjectMetadataContainer` or single `ObjectMetadata` (if `nsUri` specified)
  - `204 No Content`: No matching schemas found
  - `400 Bad Request`: Scope not available, stage not valid
  - `500 Internal Server Error`: Unexpected error

#### `POST /{scopeName}/schema/stages/{stageName}`

- **Action:** Create a new schema package or update an existing one in the specified stage.
- **Query Parameters:**
  - `nsUri` (string, optional): Namespace URI of the package. Defaults to the EPackage's own `nsURI`; if given, it must match it
  - `name` (string, optional): Human-readable name for the package
  - `version` (string, optional): Package version
  - `overwrite` (boolean, optional): If `true`, update the package already in this stage. If `false` (default), return conflict if one is there.
- **Request Body:** The EPackage content
- **Content-Type Header:** `application/json`, `application/xml`, or `application/ecore+xml`
- **Logic:**
  1. Check whether the `nsUri` is taken in the **target stage** (then, if free there, in the ancestor final stages)
  2. If taken and `overwrite=false`: Return `409 Conflict`
  3. If taken and `overwrite=true`: Update if not read-only
  4. If read-only (inherited from a parent scope, or a read-only stage): Return `403 Forbidden`
  5. If free: Create the package with a fresh `objectId`
  6. The other stages of the local scope are **not** consulted: identity is scoped to a stage
- **Response:**
  - `201 Created`: Package created successfully
    - `Location` header: `/{scopeName}/schema/stages/{stageName}?nsUri={encodedNsUri}`
    - Body: `ObjectMetadata`
  - `200 OK`: Package updated successfully (when `overwrite=true`)
  - `400 Bad Request`: Scope not available, stage not valid, or invalid package data
  - `403 Forbidden`: Package is read-only (from parent scope)
  - `409 Conflict`: The `nsUri` is already taken in this stage and `overwrite=false`
  - `500 Internal Server Error`: Unexpected error

#### `GET /{scopeName}/schema/stages/{stageName}/content`

- **Action:** Get the *content* of a schema package.
- **Query Parameters:**
  - `nsUri` (string, required): URL-encoded namespace URI of the package
- **Accept Header:** Desired format (`application/json`, `application/xml`, `application/ecore+xml`, `application/schema+json`)
- **Logic:** Finds package respecting hierarchical visibility and returns content in requested format.
- **Response:**
  - `200 OK`: Package content in requested format
  - `204 No Content`: Package not found
  - `400 Bad Request`: Scope not available, stage not valid
  - `500 Internal Server Error`: Unexpected error

#### `PUT /{scopeName}/schema/stages/{stageName}/content`

- **Action:** Update the content of an existing schema package.
- **Query Parameters:**
  - `nsUri` (string, required): URL-encoded namespace URI of the package
  - `version` (string, optional): New version string
- **Request Body:** The updated EPackage content
- **Content-Type Header:** `application/json`, `application/xml`, or `application/ecore+xml`
- **Logic:**
  1. Find existing package metadata
  2. Verify package is not read-only
  3. Update content
- **Response:**
  - `200 OK`: Package updated successfully - Body: `ObjectMetadata`
  - `204 No Content`: Package not found
  - `400 Bad Request`: Scope not available, stage not valid
  - `403 Forbidden`: Package is read-only (from parent scope)
  - `500 Internal Server Error`: Unexpected error

#### `DELETE /{scopeName}/schema/stages/{stageName}`

- **Action:** Delete a schema package.
- **Query Parameters:**
  - `nsUri` (string, required): URL-encoded namespace URI of the package to delete
- **Logic:**
  1. Find package metadata
  2. Verify package is not read-only
  3. Delete package
- **Response:**
  - `200 OK`: Package deleted successfully
  - `204 No Content`: Package not found
  - `400 Bad Request`: Scope not available, stage not valid
  - `403 Forbidden`: Package is read-only (from parent scope)
  - `500 Internal Server Error`: Unexpected error

---

### 3.3 Lifecycle Actions

Endpoint for moving packages between stages.

#### `POST /{scopeName}/schema/stages/{stageName}/actions/transition`

- **Action:** Move a package from the current stage (`{stageName}`) to a target stage.
- **Request Body:** `application/json`

```json
{
  "objectId": "3f8a1c2e-9d4b-4f6a-8c1d-2e5b7a9c0d41",
  "targetStage": "approved"
}
```

- `objectId` (string, required): The package's `objectId` as returned in its
  `ObjectMetadata`. For backward compatibility a raw `nsUri` is also accepted and resolved
  via the `nsUri` metadata property
- `targetStage` (string, required): Target stage name

- **Logic:**
  1. Find package in source stage
  2. Verify package is not read-only
  3. Verify transition is allowed (check `allowedTransitions`)
  4. Write the package into the target stage under its own `objectId`, and remove it from
     the source stage only if the registry sets `delete.after.transition=true`. The target
     stage is **not** checked for a conflicting occupant of that id
- **Response:**
  - `200 OK`: Transition successful - Body: Updated `ObjectMetadata` with new stage
  - `204 No Content`: Package not found in source stage
  - `400 Bad Request`: Invalid transition, missing parameters, or scope/stage not available
  - `403 Forbidden`: Package is read-only (from parent scope)
  - `500 Internal Server Error`: Unexpected error

**Error Response Example (400 Bad Request):**
```json
{
  "error": "Transition from 'draft' to 'release' is not allowed."
}
```

---

## 4. HTTP Status Code Summary

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 OK | Success | GET, PUT, POST (update/transition) successful |
| 201 Created | Resource created | POST (create) successful |
| 204 No Content | Not found / Empty | Resource not found, or list is empty |
| 400 Bad Request | Invalid request | Scope not available, invalid stage, invalid transition |
| 403 Forbidden | Operation not allowed | Resource is read-only (from parent scope) |
| 404 Not Found | Scope not found | Only for `/scopes/{scopeName}` endpoint |
| 409 Conflict | Resource exists | Package exists and override flag is false |
| 500 Internal Server Error | Server error | Unexpected errors |

---

## 5. Related Documentation

- [README-Scopes.md](README-Scopes.md) - Scopes API documentation
- [README-SchemaPackages.md](README-SchemaPackages.md) - Detailed Schema Packages API documentation
- [Model Atlas Object API Specification.md](Model%20Atlas%20Object%20API%20Specification.md) - Object Storage API specification
