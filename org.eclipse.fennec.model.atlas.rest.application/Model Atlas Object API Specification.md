# Model Atlas Object Storage API Specification

This API defines resources for storing and managing objects within the Model Atlas system. Objects are stored in registries with schema validation and stage-based lifecycle management.

## 1. Core Concepts

### Registry

A "Registry" is a logical repository within a `Scope` for storing objects of a specific type. Each scope can have multiple registries.

- `schema`: The registry used by SchemaPackagesResource for EPackages.
- **Custom Registries:** (e.g., `configurations`, `data-models`). These are configured via `RegistryService` in `workflow.json`.

Each registry defines:
- Its own workflow stages (e.g., `draft`, `approved`, `release`)
- Allowed transitions between stages
- The expected root EClass for schema validation

### Storage Object

The actual EMF object being stored. Objects must conform to the registry's configured schema.

- **Identifier (`objectId`):** A unique string identifier for the object within a registry/scope.
- **Content:** The EMF object (EObject) serialized in various formats (JSON, XML, XMI).

### Schema Validation

When creating or updating objects, the `RegistryService` validates that the object's EClass is compatible with the registry's configured `root.eclass.uri`. If validation fails, the request is rejected with `400 Bad Request`.

### Stage

A lifecycle state for an object within a registry. Examples: `draft`, `approved`, `release`.

- Each stage has a `writable` flag indicating if modifications are allowed.
- Each stage has a `final` flag indicating if it's the released/published stage.
- Transitions between stages are explicitly configured via `allowedTransitions`.

### Hierarchical Visibility

Similar to the Schema API, objects follow hierarchical visibility rules:

1. **Read-Time:** When retrieving an object by `objectId`, the system searches the current scope/stage first, then the final stage of parent scopes.
2. **Read-Only:** Objects from parent scopes are marked as `isReadOnly: true` and cannot be modified or deleted from child scopes.

## 2. Resource Models (JSON)

### ObjectMetadata

```json
{
  "objectId": "production-db-settings",
  "objectName": "Production Database Settings",
  "scope": "my-tenant",
  "registry": "configurations",
  "stage": "draft",
  "version": "1.0.0",
  "isReadOnly": false,
  "uploadTime": "2023-10-27T10:00:00Z",
  "lastChangeTime": "2023-10-27T10:00:00Z"
}
```

- `objectId`: Unique identifier for the object
- `isReadOnly`: `true` when the object is resolved from a parent scope

### ObjectMetadataContainer

```json
{
  "metadata": [
    { /* ObjectMetadata */ },
    { /* ObjectMetadata */ }
  ]
}
```

### TransitionRequest

```json
{
  "objectId": "production-db-settings",
  "targetStage": "release"
}
```

## 3. API Endpoints

**Base Path:** `/{scopeName}/registries/{registryName}`

### 3.1 List Objects in Final Stage

#### `GET /{scopeName}/registries/{registryName}`

- **Action:** List all objects in the *final/released* stage for this scope and registry. Includes objects from parent scopes' final stages (marked as read-only).
- **Response:**
  - `200 OK`: `application/json` - `ObjectMetadataContainer` with list of `ObjectMetadata`
  - `204 No Content`: No objects found
  - `400 Bad Request`: Scope not available, registry not configured
  - `500 Internal Server Error`: Unexpected error

---

### 3.2 List Objects in Specific Stage

#### `GET /{scopeName}/registries/{registryName}/stages/{stageName}`

- **Action:** List objects within a specific stage.
- **Query Parameters:**
  - `objectId` (string, optional): Find a single object by exact ID. Respects hierarchical visibility. Returns single `ObjectMetadata` or `204 No Content`.
  - `name` (string, optional): Search by object name. Supports trailing wildcards (e.g., `name=Config*`). Returns list within local scope only (no hierarchy).
- **Response:**
  - `200 OK`: `application/json` - `ObjectMetadataContainer` or single `ObjectMetadata` (if `objectId` specified)
  - `204 No Content`: No matching objects found
  - `400 Bad Request`: Scope not available, registry not configured, stage not valid
  - `500 Internal Server Error`: Unexpected error

---

### 3.3 Create/Update Object

#### `POST /{scopeName}/registries/{registryName}/stages/{stageName}/{objectId}`
#### `PUT /{scopeName}/registries/{registryName}/stages/{stageName}/{objectId}`

- **Action:** Create a new object or update an existing one.
- **Path Parameters:**
  - `objectId` (required): Unique identifier for the object
- **Query Parameters:**
  - `name` (string, optional): Human-readable name for the object
  - `version` (string, optional): Object version
  - `override` (boolean, optional): If `true`, update existing object. If `false` (default), return conflict if exists.
- **Request Body:** The object content (must conform to registry's schema)
- **Content-Type Header:** `application/json`, `application/xml`, `application/xmi`
- **Logic:**
  1. Validate registry is configured and available
  2. Validate object's EClass is compatible with registry
  3. Check if object exists:
     - If exists and `override=false`: Return `409 Conflict`
     - If exists and `override=true` and not read-only: Update
     - If read-only: Return `403 Forbidden`
  4. If new: Create object
- **Response:**
  - `201 Created`: Object created successfully
    - `Location` header: `/{scopeName}/registries/{registryName}/stages/{stageName}?objectId={objectId}`
    - Body: `ObjectMetadata`
  - `200 OK`: Object updated successfully (when `override=true`)
  - `400 Bad Request`: Scope/registry/stage not available, schema validation failed
  - `403 Forbidden`: Object is read-only (from parent scope)
  - `409 Conflict`: Object exists and `override=false`
  - `500 Internal Server Error`: Unexpected error

---

### 3.4 Get Object Content

#### `GET /{scopeName}/registries/{registryName}/stages/{stageName}/content`

- **Action:** Get the content of an object.
- **Query Parameters:**
  - `objectId` (string, required): The object identifier
- **Accept Header:** Desired format (`application/json`, `application/xml`, `application/xmi`)
- **Logic:** Finds object respecting hierarchical visibility and returns content in requested format.
- **Response:**
  - `200 OK`: Object content in requested format
  - `204 No Content`: Object not found
  - `400 Bad Request`: Scope/registry/stage not available
  - `500 Internal Server Error`: Unexpected error

---

### 3.5 Update Object Content

#### `PUT /{scopeName}/registries/{registryName}/stages/{stageName}/content`
#### `POST /{scopeName}/registries/{registryName}/stages/{stageName}/content`

- **Action:** Update the content of an existing object.
- **Query Parameters:**
  - `objectId` (string, required): The object identifier
  - `version` (string, optional): New version string
- **Request Body:** The updated object content
- **Content-Type Header:** `application/json`, `application/xml`, `application/xmi`
- **Logic:**
  1. Validate object's EClass is compatible with registry
  2. Find existing object metadata
  3. Verify object is not read-only
  4. Update content
- **Response:**
  - `200 OK`: Object updated successfully - Body: `ObjectMetadata`
  - `204 No Content`: Object not found
  - `400 Bad Request`: Scope/registry/stage not available, schema validation failed
  - `403 Forbidden`: Object is read-only (from parent scope)
  - `500 Internal Server Error`: Unexpected error

---

### 3.6 Delete Object

#### `DELETE /{scopeName}/registries/{registryName}/stages/{stageName}`

- **Action:** Delete an object.
- **Query Parameters:**
  - `objectId` (string, required): The object identifier to delete
- **Logic:**
  1. Find object metadata
  2. Verify object is not read-only
  3. Delete object
- **Response:**
  - `200 OK`: Object deleted successfully
  - `204 No Content`: Object not found
  - `400 Bad Request`: Scope/registry/stage not available
  - `403 Forbidden`: Object is read-only (from parent scope)
  - `500 Internal Server Error`: Unexpected error

---

### 3.7 Transition Object Between Stages

#### `POST /{scopeName}/registries/{registryName}/stages/{stageName}/actions/transition`

- **Action:** Move an object from the current stage (`{stageName}`) to a target stage.
- **Request Body:** `application/json`

```json
{
  "objectId": "production-db-settings",
  "targetStage": "release"
}
```

- `objectId` (string, required): The object identifier
- `targetStage` (string, required): Target stage name

- **Logic:**
  1. Find object in source stage
  2. Verify object is not read-only
  3. Verify transition is allowed (check registry's `allowedTransitions`)
  4. Move object to target stage
- **Response:**
  - `200 OK`: Transition successful - Body: Updated `ObjectMetadata` with new stage
  - `204 No Content`: Object not found in source stage
  - `400 Bad Request`: Invalid transition, missing parameters, or scope/registry/stage not available
  - `403 Forbidden`: Object is read-only (from parent scope)
  - `500 Internal Server Error`: Unexpected error

---

## 4. Schema Validation

### How It Works

When creating or updating objects, the `ObjectRegistryResource` validates the object against the registry's schema:

1. **Registry Lookup:** The `RegistryServiceCollector` retrieves the `RegistryService` for the registry name.
2. **Type Validation:** The `isEClassCompatibleWithRegistry(EClass)` method checks if the object's EClass is compatible with the registry's `root.eclass.uri`.
3. **Rejection:** If validation fails, the request is rejected with `400 Bad Request`.

**Error Response Example:**
```json
{
  "error": "Object type http://example.com/models/WrongType not compatible with registry configurations (expects http://example.com/models/Configuration)"
}
```

---

## 5. HTTP Status Code Summary

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 OK | Success | GET, PUT, POST (update/transition) successful |
| 201 Created | Resource created | POST (create) successful |
| 204 No Content | Not found / Empty | Object not found, or list is empty |
| 400 Bad Request | Invalid request | Scope/registry/stage not available, invalid transition, schema validation failed |
| 403 Forbidden | Operation not allowed | Object is read-only (from parent scope) |
| 409 Conflict | Resource exists | Object exists and override flag is false |
| 500 Internal Server Error | Server error | Unexpected errors |

---

## 6. Configuration

### Registry Configuration

Registries are configured in `workflow.json`:

```json
{
  "RegistryService~configurations": {
    "registry.name": "configurations",
    "registry.description": "Configuration objects registry",
    "stage.storage.mappings": [
      "draft:file",
      "release:file"
    ],
    "workflow.transitions": [
      "draft:release"
    ],
    "stages": [
      { "name": "draft", "writable": true, "final": false },
      { "name": "release", "writable": false, "final": true }
    ],
    "storageService.target": "(storage.type=file)",
    "schema.uri": "http://example.com/models/config",
    "root.eclass.uri": "http://example.com/models/config#//Configuration"
  }
}
```

See [README-ObjectStorage.md](README-ObjectStorage.md) for complete configuration details.

---

## 7. Related Documentation

- [README-ObjectStorage.md](README-ObjectStorage.md) - Detailed Object Storage API documentation
- [README-Scopes.md](README-Scopes.md) - Scopes API documentation
- [Model Atlas API Specification.md](Model%20Atlas%20API%20Specification.md) - Schema API specification
