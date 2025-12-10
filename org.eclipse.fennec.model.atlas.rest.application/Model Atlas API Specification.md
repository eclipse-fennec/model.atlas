# Model Atlas API Specification

## 1. Core Concepts

### Model Atlas

A central registry for data schemas (EPackages). It acts  as a canonical source of truth in a distributed system, storing all  models as Ecore and allowing them to be retrieved in various formats  (XSD, JSON Schema, etc.).

### Scope

A "tenant" or logical partition within the registry. Scopes are hierarchical.

- `atlas`: A special, read-only  "system" scope that contains common base models (like Ecore itself). It  is the default parent for any scope that does not specify one.
- **Custom Scopes:** (e.g., `my-tenant`, `global-corporate`). These are the primary partitions for schemas.

### Stage

A lifecycle state for a `SchemaPackage` within a scope. Examples: `Draft`, `Review`, `Released`.

- Stages have a defined order (e.g., `Draft` -> `Review` -> `Released`).
- Actions (like `PUT`, `DELETE`) can be restricted based on stage (e.g., no `PUT` to a `Released` package).

### `nsUri`

The `nsUri` (Namespace URI) of an EPackage is the primary business key. **All package lookups are based on `nsUri`, not a system ID.**

### Scope Configuration (Backend)

In the Model Atlas backend (which is based on OSGi), new `Scopes` are provisioned as OSGi Services utilizing the OSGi Configurator Specification.

The configuration for a scope service must define its `name` and may optionally define its `parentScope`. If no `parentScope` is specified, it will default to the `atlas` scope. This hierarchy is then exposed through the REST API, particularly in the hierarchical search logic.

### Hierarchical Visibility and Uniqueness

The hierarchy of scopes (defined by `parentScope`) is a fundamental concept that governs visibility and uniqueness.

1. **Uniqueness (Write-Time):** A `nsUri` must be unique within its "visibility chain". When creating a new package (e.g., in `my-tenant/Draft`), the system will check if that `nsUri` already exists in *any* stage of `my-tenant` OR in the *Released* stage of any parent scope (`global-corporate`, `atlas`, etc.). If a conflict is found, the creation (`POST`) will be rejected with `409 Conflict`.
2. **Visibility (Read-Time):** Any query for a package by its `nsUri` (e.g., `GET ...?nsUri=...`) will also search this chain. The system will first look in the specified scope and stage (e.g., `my-tenant/Draft`). If not found, it will proceed to check the `Released` stage of the parent scope (`global-corporate/Released`), and so on up to `atlas/Released`.

This ensures that released packages from parent scopes are "known" and resolvable from all child scopes. When a parent package is  returned from a child scope query, it will be marked as read-only (`isReadOnly: true`) and its `sourceScope` property will indicate its origin.

## Resource Models (JSON)

### Scope

```
{
  "name": "my-tenant",
  "parentScope": "atlas",
  "description": "Primary scope for MyTenant application.",
  "links": {
    "self": "/scopes/my-tenant",
    "schemas": "/my-tenant/schema"
  }
}
```

​    

### SchemaPackage

```
{
  "scope": "my-tenant",
  "name": "Billing",
  "nsUri": "http://example.com/schemas/billing/v1",
  "stage": "Draft",
  "version": "1.0.0",
  "description": "Billing schema, v1.",
  "createdAt": "2023-10-27T10:00:00Z",
  "updatedAt": "2023-10-27T10:00:00Z",
  "isReadOnly": false,
  "sourceScope": "my-tenant",
  "links": {
    "self": "/my-tenant/schema/stages/Draft?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1",
    "content": "/my-tenant/schema/stages/Draft/content?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1",
    "transition": "/my-tenant/schema/stages/Draft/actions/transition"
  }
}
```

​    

- `isReadOnly` / `sourceScope`: Used when a package is resolved from a parent scope.

## API Endpoints

### 1. Scopes

Resource for discovering available scopes.

#### `GET /scopes`

- **Action:** List all configured scopes.
- **Response (200 OK):** `application/json` (A list of `Scope` objects).

#### `GET /scopes/{scopeName}`

- **Action:** Get metadata for a specific scope.
- **Response (200 OK):** `application/json` (A single `Scope` object).

### 2. SchemaPackages

Resource for managing schemas within a scope.

#### `GET /{scopeName}/schema`

- **Action:** List all packages in the *final/released* stage for this scope. This endpoint respects the "Hierarchical  Visibility" rule, including packages from parent scopes' released  stages.
- **Response (200 OK):** `application/json` (A list of `SchemaPackage` metadata objects)

#### `GET /{scopeName}/schema/stages/{stageName}`

- **Action:** List all packages within a *specific stage* of a scope.
- Query Parameters:
  - `nsUri` (string): Find a single  package by its exact namespace URI. This lookup respects the  "Hierarchical Visibility" rule, searching the current stage and then  parent's released stages. If present, this returns a single `SchemaPackage` metadata object (or 404).
  - `name` (string): Search by package name (supports wildcards, e.g., `name=*Billing*`).
- **Response (200 OK):** `application/json` (A list of `SchemaPackage` metadata objects, or a single object if `nsUri` is used).

#### `POST /{scopeName}/schema/stages/{stageName}`

- **Action:** Create a new `SchemaPackage` in the specified stage. The package content is sent as the request body.
- Query Parameters:
  - `nsUri` (string, required): The namespace URI of the package being created.
  - `name` (string, optional): A human-readable name for the package.
  - `version` (string, optional): The package version.
- **Request Body:** The raw schema file (e.g., `application/ecore+xml`, `application/xml`).
- **Content-Type Header:** Must specify the format of the schema being uploaded.
- Logic:
  1. Server checks for uniqueness based on the `nsUri` (see "Hierarchical Visibility and Uniqueness").
  2. If `nsUri` exists, return `409 Conflict`.
  3. If not, create the new package.
- Response (201 Created):
  - `Location` Header: `/{scopeName}/schema/stages/{stageName}?nsUri={encodedNsUri}`
  - Body: The `SchemaPackage` metadata (JSON).
- **Error Response (409 Conflict):** If `nsUri` already exists.

#### `GET /{scopeName}/schema/stages/{stageName}/content`

- **Action:** Get the *content* of a `SchemaPackage` in a specific format.
- Query Parameters:
  - `nsUri` (string, required): The namespace URI of the package to retrieve.
- **Accept Header:** The client specifies the desired format (e.g., `application/ecore+xml`, `application/schema+json`).
- Logic:
  1. Server finds the package by `nsUri` (respecting "Hierarchical Visibility").
  2. Server transforms the canonical Ecore model into the requested format.
- Response (200 OK):
  - Body: The raw schema file (e.g., `application/schema+json`).
  - `Content-Type` Header: Reflects the format being returned.
- **Error Response (406 Not Acceptable):** If the requested format is not supported.

#### `PUT /{scopeName}/schema/stages/{stageName}/content`

- **Action:** Update (i.e., replace) the content of an existing `SchemaPackage`.
- Query Parameters:
  - `nsUri` (string, required): The namespace URI of the package to update.
- **Request Body:** The *new* raw schema file.
- **Content-Type Header:** Must specify the format of the schema being uploaded.
- Logic:
  - Fails with `403 Forbidden` (or `405 Method Not Allowed`) if the stage is read-only (e.g., `Released`).
- **Response (200 OK):** Body: The updated `SchemaPackage` metadata (JSON).

#### `DELETE /{scopeName}/schema/stages/{stageName}`

- **Action:** Delete a `SchemaPackage`.
- Query Parameters:
  - `nsUri` (string, required): The namespace URI of the package to delete.
- Logic:
  - Fails with `403 Forbidden` if the stage is read-only.
- **Response (204 No Content):**

### 3. Lifecycle Actions

This endpoint manages the movement of a package between stages.

#### `POST /{scopeName}/schema/stages/{stageName}/actions/transition`

- **Action:** Move a package to a different stage. The `{stageName}` in the URL represents the *source* stage.

- **Query Parameters:**

  - (None)

- **Request Body:** `application/json`

  ```
  {
    "nsUri": "http://example.com/schemas/billing/v1",
    "targetStage": "Review"
  }
  ```

  ​    

- `nsUri` (string, required): The namespace URI of the package to transition.
- `targetStage` (string, required): The name of the stage to move the package to.

**Logic:**

1. Server identifies the package by `nsUri` from the request body.
2. Server verifies the package is currently in the `{stageName}` specified in the URL.
3. Server checks its internal rules to see if a transition from `{stageName}` (the source) to `targetStage` is allowed.
4. If the transition is not allowed, it returns a `400 Bad Request` error.
5. If allowed, the package's `stage` property is updated.

**Response (200 OK):** Body: The updated `SchemaPackage` metadata (JSON), now showing the new `stage`.

**Error Response (400 Bad Request):** If the transition is not valid.

```
{
  "error": "InvalidTransition",
  "message": "Transition from 'Draft' to 'Released' is not allowed. Must pass 'Review' stage."
}
```