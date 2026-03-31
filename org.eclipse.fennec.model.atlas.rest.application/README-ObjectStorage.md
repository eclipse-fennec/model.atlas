# ObjectRegistryResource REST API

## Overview

The **ObjectRegistryResource** provides a RESTful HTTP API for managing storage objects within the Model Atlas system. It enables storing, retrieving, and managing EMF-based objects across different registries with schema validation and stage-based lifecycle management.

## Key Features

- **Registry-Based Storage**: Organize objects into logical registries (e.g., `configurations`, `data-models`)
- **Schema Validation**: Objects must conform to schemas known to the Model Atlas
- **Scope-Based Management**: Multi-tenant isolation via configurable scopes
- **Stage-Based Lifecycle**: Manage objects through workflow stages (draft, review, release, etc.)
- **Hierarchical Visibility**: Child scopes can access objects from parent scopes' final stages
- **Content Negotiation**: Support for multiple formats (JSON, XML, XMI, UML)
- **Stage Transitions**: Move objects between workflow stages with validation

## Architecture

### Component Dependencies

```
┌───────────────────────────────────┐
│     ObjectRegistryResource        │
│     (JAX-RS REST Endpoint)        │
└─────────────┬─────────────────────┘
              │
      ┌───────┴───────┐
      │               │
┌─────▼──────┐ ┌──────▼──────────────┐
│ ScopeService│ │ RegistryService     │
│ Collector   │ │ Collector           │
└─────┬──────┘ └──────┬──────────────┘
      │               │
┌─────▼──────┐ ┌──────▼──────────────┐
│ScopeService│ │RegistryService      │
│  (OSGi)    │ │   (OSGi)            │
└────────────┘ └─────────────────────┘
```

### Integration Points

#### **ScopeServiceCollector**
The `ScopeServiceCollector` dynamically tracks all registered `ScopeService` instances and provides access to scope-specific workflow operations.

**Key Methods:**
- `getScopeServiceByScopeName(String scopeName)`: Retrieves the scope service for a specific scope

#### **RegistryServiceCollector**
The `RegistryServiceCollector` dynamically tracks all registered `RegistryService` instances and provides registry-specific operations including schema validation.

**Key Methods:**
- `getRegistryServiceByRegistryName(String registryName)`: Retrieves the registry service by name

#### **RegistryService**
Provides schema validation and registry-specific operations.

**Used Operations:**
- `isEClassCompatibleWithRegistry(EClass eClass)`: Validates that an object type is compatible with the registry
- `getRootEClass()`: Returns the expected root EClass for the registry

#### **ScopeService**
Provides the underlying workflow operations for object management within a scope.

**Used Operations:**
- `listInFinalStageForRegistry(String registryName)`: List objects in the final/released stage (with hierarchy)
- `listInStageForRegistry(String registryName, String stage)`: List objects in a specific stage
- `listInStageForRegistryByName(String registryName, String stage, String name)`: List objects filtered by name (supports wildcards)
- `getMetadataFromStageForRegistry(String registryName, String stage, String objectId)`: Retrieve object metadata
- `getContentFromStageForRegistry(String registryName, String stage, String objectId)`: Retrieve actual object content
- `uploadToStageForRegistry(String registryName, String stage, EObject object, ObjectMetadata metadata)`: Create new object
- `updateInStageForRegistry(String registryName, String stage, EObject object, String objectId, String version)`: Update existing object
- `deleteFromStageForRegistry(String registryName, String stage, String objectId)`: Delete object
- `transitionToStageForRegistry(String registryName, String objectId, String fromStage, String toStage)`: Move between stages

## Resource Path Structure

All endpoints are rooted at: `/{scopeName}/registries/{registryName}`

Where:
- `{scopeName}` - The scope identifier (e.g., "my-tenant", "global-corporate")
- `{registryName}` - The registry identifier (e.g., "configurations", "data-models")

## API Endpoints

### 1. List Objects in Final Stage

```http
GET /{scopeName}/registries/{registryName}
Accept: application/json
```

**Purpose**: List all objects in the final/released stage for this scope and registry, including objects from parent scopes' final stages.

**Behavior**:
- Calls `scopeService.listInFinalStageForRegistry(registryName)` which implements hierarchical lookup
- Returns objects from local scope AND all parent scopes (recursively)
- Parent objects are marked as read-only

**Response**:
- **200 OK**: Returns `ObjectMetadataContainer` with list of `ObjectMetadata`
- **204 No Content**: No objects found in the final stage
- **400 Bad Request**: Scope not available, registry not available for scope
- **500 Internal Server Error**: Server error

**Example**:
```bash
curl -X GET https://api.example.com/my-tenant/registries/configurations \
  -H "Accept: application/json"
```

**Example Response**:
```json
{
  "metadata": [
    {
      "objectId": "production-db-settings",
      "objectName": "Production Database Settings",
      "scope": "my-tenant",
      "registry": "configurations",
      "stage": "release",
      "version": "1.0.0",
      "isReadOnly": false,
      "uploadTime": "2023-10-27T10:00:00Z",
      "lastChangeTime": "2023-10-27T10:00:00Z"
    },
    {
      "objectId": "shared-logging-config",
      "objectName": "Shared Logging Configuration",
      "scope": "atlas",
      "registry": "configurations",
      "stage": "release",
      "version": "2.0.0",
      "isReadOnly": true,
      "uploadTime": "2023-01-15T08:00:00Z",
      "lastChangeTime": "2023-01-15T08:00:00Z"
    }
  ]
}
```

---

### 2. List Objects in Specific Stage

```http
GET /{scopeName}/registries/{registryName}/stages/{stageName}
Accept: application/json
```

**Purpose**: List all objects within a specific stage of a scope and registry.

**Query Parameters**:
- `objectId` (optional): Find single object by exact ID (hierarchical lookup)
- `name` (optional): Filter by object name with wildcard support (scope-specific, no hierarchy)

**Behavior**:
- **Without parameters**: Lists all objects in the specified stage (local scope only)
- **With `objectId`**: Performs hierarchical lookup (local stage, then parent final stages)
- **With `name`**: Filters objects by name in the local scope only (no hierarchical lookup)
  - Supports trailing wildcards: `Config*` (matches "Config", "ConfigFile", "ConfigData")
  - Exact match: `ProductionConfig` (matches only "ProductionConfig")
  - **Note**: Leading wildcards (e.g., `*Config`) are NOT supported

**Response**:
- **200 OK**: Returns `ObjectMetadataContainer` with list of `ObjectMetadata`, or single `ObjectMetadata` if objectId specified
- **204 No Content**: No objects match the filter criteria
- **400 Bad Request**: Scope not available, registry not available, stage not valid
- **500 Internal Server Error**: Server error

**Example**:
```bash
# List all objects in draft stage
curl -X GET https://api.example.com/my-tenant/registries/configurations/stages/draft \
  -H "Accept: application/json"

# Find specific object by ID (with hierarchical lookup)
curl -X GET "https://api.example.com/my-tenant/registries/configurations/stages/draft?objectId=production-db-settings" \
  -H "Accept: application/json"

# Filter by name (scope-specific, no hierarchy)
curl -X GET "https://api.example.com/my-tenant/registries/configurations/stages/draft?name=Production*" \
  -H "Accept: application/json"
```

---

### 3. Create/Update Object

```http
POST /{scopeName}/registries/{registryName}/stages/{stageName}/{objectId}
PUT /{scopeName}/registries/{registryName}/stages/{stageName}/{objectId}
Content-Type: application/json | application/xml | application/xmi
```

**Purpose**: Create a new storage object or update an existing one in the specified stage.

**Path Parameters**:
- `scopeName` (required): The scope name
- `registryName` (required): The registry name
- `stageName` (required): The stage name
- `objectId` (required): The unique object identifier

**Query Parameters**:
- `name` (optional): Human-readable name for the object
- `version` (optional): Object version
- `override` (boolean, optional): If `true` and object exists, update it. If `false` and object exists, return 409 Conflict.

**Request Body**: The object content (must conform to the registry's expected schema)

**Behavior**:
1. Validates that the registry is configured and available
2. Validates that the object's EClass is compatible with the registry
3. Checks if object already exists:
   - If exists and `override=false`: Returns `409 Conflict`
   - If exists and `override=true`: Updates the object (returns `200 OK`)
   - If read-only: Returns `403 Forbidden`
4. If new object: Creates and returns `201 Created`

**Response**:
- **201 Created**: Object created successfully
  - `Location` header: `/{scopeName}/registries/{registryName}/stages/{stageName}?objectId={objectId}`
  - Body: `ObjectMetadata` with created object info
- **200 OK**: Object updated successfully (when override=true)
- **400 Bad Request**: Invalid object data, schema validation failed, or registry/scope/stage not available
- **403 Forbidden**: Object is read-only (from parent scope)
- **409 Conflict**: Object with ID already exists and override flag is false
- **415 Unsupported Media Type**: Invalid Content-Type
- **500 Internal Server Error**: Server error

**Example**:
```bash
curl -X POST "https://api.example.com/my-tenant/registries/configurations/stages/draft/production-db-settings?name=Production%20DB%20Settings&version=1.0.0" \
  -H "Content-Type: application/json" \
  -d '{
    "host": "db.example.com",
    "port": 5432,
    "database": "production"
  }'
```

**Example Response** (201 Created):
```json
{
  "objectId": "production-db-settings",
  "objectName": "Production DB Settings",
  "scope": "my-tenant",
  "registry": "configurations",
  "stage": "draft",
  "version": "1.0.0",
  "isReadOnly": false,
  "uploadTime": "2023-10-27T10:00:00Z",
  "lastChangeTime": "2023-10-27T10:00:00Z"
}
```

---

### 4. Get Object Content

```http
GET /{scopeName}/registries/{registryName}/stages/{stageName}/content?objectId={objectId}
Accept: application/json | application/xml | application/xmi
```

**Purpose**: Retrieve the actual content of a storage object in the requested format.

**Query Parameters**:
- `objectId` (required): The unique object identifier

**Behavior**:
- Performs hierarchical lookup via `getContentFromStageForRegistry()`
- Returns the EObject (serialized based on Accept header)

**Response**:
- **200 OK**: Object content retrieved successfully
  - Body: Object content in requested format
  - `Content-Type` header matches requested format
- **204 No Content**: Object not found in visibility chain
- **400 Bad Request**: Scope, registry, or stage not available
- **406 Not Acceptable**: Requested format not supported
- **500 Internal Server Error**: Server error

**Example**:
```bash
# Get as JSON
curl -X GET "https://api.example.com/my-tenant/registries/configurations/stages/draft/content?objectId=production-db-settings" \
  -H "Accept: application/json"

# Get as XML
curl -X GET "https://api.example.com/my-tenant/registries/configurations/stages/draft/content?objectId=production-db-settings" \
  -H "Accept: application/xml"
```

---

### 5. Update Object Content

```http
PUT /{scopeName}/registries/{registryName}/stages/{stageName}/content?objectId={objectId}
POST /{scopeName}/registries/{registryName}/stages/{stageName}/content?objectId={objectId}
Content-Type: application/json | application/xml | application/xmi
```

**Purpose**: Replace the content of an existing storage object.

**Query Parameters**:
- `objectId` (required): The unique object identifier
- `version` (optional): Updated version string

**Request Body**: New object content

**Behavior**:
1. Validates that the registry is configured and available
2. Validates that the object's EClass is compatible with the registry
3. Retrieves existing metadata via `getMetadataFromStageForRegistry()` (hierarchical lookup)
4. Checks if object is read-only (`isReadOnly` flag)
5. Updates object via `updateInStageForRegistry()`

**Protection Rules**:
- Cannot update if object is from parent scope (read-only)
- Cannot update if object doesn't exist

**Response**:
- **200 OK**: Object updated successfully
  - Body: Updated `ObjectMetadata`
- **204 No Content**: Object not found
- **400 Bad Request**: Invalid object data or schema validation failed
- **403 Forbidden**: Object is from parent scope (read-only)
- **500 Internal Server Error**: Server error

**Example**:
```bash
curl -X PUT "https://api.example.com/my-tenant/registries/configurations/stages/draft/content?objectId=production-db-settings&version=1.1.0" \
  -H "Content-Type: application/json" \
  -d '{
    "host": "db-new.example.com",
    "port": 5432,
    "database": "production",
    "poolSize": 10
  }'
```

---

### 6. Delete Object

```http
DELETE /{scopeName}/registries/{registryName}/stages/{stageName}?objectId={objectId}
```

**Purpose**: Delete a storage object from the specified stage.

**Query Parameters**:
- `objectId` (required): The unique object identifier to delete

**Behavior**:
1. Retrieves object metadata to verify existence
2. Checks if object is read-only
3. Deletes via `deleteFromStageForRegistry()`

**Protection Rules**:
- Cannot delete parent scope objects
- Cannot delete if object doesn't exist locally

**Response**:
- **200 OK**: Object deleted successfully
- **204 No Content**: Object not found
- **403 Forbidden**: Object is from parent scope (read-only)
- **400 Bad Request**: Scope, registry, or stage not available
- **500 Internal Server Error**: Server error

**Example**:
```bash
curl -X DELETE "https://api.example.com/my-tenant/registries/configurations/stages/draft?objectId=production-db-settings"
```

---

### 7. Transition Object Between Stages

```http
POST /{scopeName}/registries/{registryName}/stages/{stageName}/actions/transition
Content-Type: application/json
```

**Purpose**: Move an object from the current stage to a target stage.

**Request Body**:
```json
{
  "objectId": "production-db-settings",
  "targetStage": "review"
}
```

**Body Fields**:
- `objectId` (string, required): The unique object identifier
- `targetStage` (string, required): The target stage name

**Behavior**:
1. Verifies object exists in source stage
2. Checks if object is read-only (cannot transition parent objects)
3. Performs transition via `transitionToStageForRegistry()`

**Response**:
- **200 OK**: Object transitioned successfully
  - Body: Updated `ObjectMetadata` with new stage
- **204 No Content**: Object not found in source stage
- **400 Bad Request**: Invalid transition or missing parameters
- **403 Forbidden**: Object is read-only (from parent scope)
- **500 Internal Server Error**: Server error

**Example**:
```bash
curl -X POST "https://api.example.com/my-tenant/registries/configurations/stages/draft/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{
    "objectId": "production-db-settings",
    "targetStage": "review"
  }'
```

**Example Response** (200 OK):
```json
{
  "objectId": "production-db-settings",
  "objectName": "Production DB Settings",
  "scope": "my-tenant",
  "registry": "configurations",
  "stage": "review",
  "version": "1.0.0",
  "isReadOnly": false,
  "uploadTime": "2023-10-27T10:00:00Z",
  "lastChangeTime": "2023-10-27T11:30:00Z"
}
```

---

## Schema Validation

### How It Works

When creating or updating objects, the `ObjectRegistryResource` validates that the object conforms to the registry's expected schema:

1. **Registry Lookup**: The `RegistryServiceCollector` retrieves the `RegistryService` for the specified registry name
2. **Type Validation**: The `isEClassCompatibleWithRegistry(EClass)` method checks if the object's EClass is compatible with the registry's expected root EClass
3. **Rejection**: If validation fails, the request is rejected with `400 Bad Request`

**Example Error Response**:
```json
{
  "error": "Object type http://example.com/models/WrongType not compatible with registry configurations (expects http://example.com/models/Configuration)"
}
```

### Registry Configuration

Registries are configured via OSGi services that implement `RegistryService`. See [Configuration Requirements](#configuration-requirements) for the full JSON configuration format.

The key property for schema validation is `root.eclass.uri`, which defines the expected root EClass for objects stored in the registry.

---

## Hierarchical Visibility

### How It Works

Similar to the Schema API, the Object Storage API supports hierarchical visibility:

#### **Read-Time (Retrieval)**

When retrieving an object by `objectId`:

```
Search for objectId in:
  1. Current scope, specified stage
  2. If not found AND parent exists:
     → Search parent scope, final stage
  3. If not found AND grandparent exists:
     → Search grandparent scope, final stage
  ... (recursively up to root)

Return first match, mark as read-only if from parent
```

### Read-Only Flag Behavior

Objects from parent scopes are automatically marked as read-only:

- **`isReadOnly: true`**: Object originates from a parent scope
- **`isReadOnly: false`**: Object exists in local scope

**Operations on Read-Only Objects**:
- GET (retrieve metadata and content)
- LIST (appears in lists)
- PUT/POST (update) → 403 Forbidden
- DELETE → 403 Forbidden
- Transition → 403 Forbidden

---

## Content Negotiation

### Supported Media Types

**Request (POST/PUT)**:
- `application/json` - EMF JSON format
- `application/xml` - Generic XML format
- `application/xmi` - XMI format
- `application/uml` - UML format

**Response (GET content)**:
- `application/json` - EMF JSON format
- `application/xml` - Generic XML format
- `application/xmi` - XMI format
- `application/uml` - UML format

### Usage Examples

**Create with JSON**:
```bash
curl -X POST "..." \
  -H "Content-Type: application/json" \
  -d '{"host":"db.example.com", "port": 5432}'
```

**Retrieve as XML**:
```bash
curl -X GET "..." \
  -H "Accept: application/xml"
```

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 OK | Success | GET, PUT, POST (transition/update) successful |
| 201 Created | Resource created | POST (create object) successful |
| 204 No Content | Not found | Object not found in the specified scope/registry/stage |
| 400 Bad Request | Invalid request | Invalid parameters, schema validation failed, scope/registry/stage not available |
| 403 Forbidden | Operation not allowed | Object is read-only (from parent scope) |
| 409 Conflict | Resource already exists | Object with ID already exists and override flag is false |
| 415 Unsupported Media Type | Invalid Content-Type | Unsupported format in POST/PUT |
| 500 Internal Server Error | Server error | Unexpected errors, exceptions |

### Common Error Scenarios

#### **Creating Duplicate Object**

```bash
POST /my-tenant/registries/configurations/stages/draft/existing-id

→ 409 Conflict
```

**Reason**: Object with same ID exists and override flag is false

**Solution**: Use `override=true` query parameter to update existing object

---

#### **Updating Parent Object**

```bash
PUT /my-tenant/registries/configurations/stages/draft/content?objectId=parent-object

→ 403 Forbidden
```

**Reason**: Object originates from parent scope (read-only)

**Solution**: Create a copy in the local scope with a different ID

---

#### **Schema Validation Failed**

```bash
POST /my-tenant/registries/configurations/stages/draft/my-object
Content-Type: application/json

→ 400 Bad Request
{
  "error": "Object type ... not compatible with registry configurations (expects ...)"
}
```

**Reason**: The object's EClass doesn't match the registry's expected type

**Solution**: Ensure the object conforms to the registry's schema

---

## Complete Workflow Example

### Scenario: Creating and Publishing a Configuration

```bash
# 1. Create configuration in draft stage
curl -X POST "https://api.example.com/my-tenant/registries/configurations/stages/draft/app-settings?name=Application%20Settings&version=1.0.0" \
  -H "Content-Type: application/json" \
  -d '{
    "debug": true,
    "logLevel": "INFO",
    "maxConnections": 100
  }'

# Response: 201 Created
# Location: /my-tenant/registries/configurations/stages/draft?objectId=app-settings

# 2. Update if needed
curl -X PUT "https://api.example.com/my-tenant/registries/configurations/stages/draft/content?objectId=app-settings&version=1.0.1" \
  -H "Content-Type: application/json" \
  -d '{
    "debug": false,
    "logLevel": "WARN",
    "maxConnections": 200
  }'

# Response: 200 OK

# 3. Transition to review stage
curl -X POST "https://api.example.com/my-tenant/registries/configurations/stages/draft/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{
    "objectId": "app-settings",
    "targetStage": "review"
  }'

# Response: 200 OK (now in review stage)

# 4. Transition to release stage (final)
curl -X POST "https://api.example.com/my-tenant/registries/configurations/stages/review/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{
    "objectId": "app-settings",
    "targetStage": "release"
  }'

# Response: 200 OK (now in release stage)

# 5. Verify it appears in released objects list
curl -X GET "https://api.example.com/my-tenant/registries/configurations" \
  -H "Accept: application/json"

# Response: 200 OK (includes app-settings)

# 6. Child scopes can now access it
curl -X GET "https://api.example.com/child-tenant/registries/configurations" \
  -H "Accept: application/json"

# Response: 200 OK (includes app-settings with isReadOnly=true)
```

---

## Configuration Requirements

### Scope Service Configuration

Scopes are configured in `workflow.json`. Each scope requires a `ScopeService` instance:

```json
{
  "ScopeService~my-tenant": {
    "scope.name": "my-tenant",
    "scope.description": "Primary tenant workspace",
    "parent.scope": "atlas",
    "registryService.target": "(|(registry.name=schema)(registry.name=configurations))",
    "registryService.cardinality.minimum:int": 1
  }
}
```

**Key Properties**:
- `scope.name`: Scope identifier (used in URL paths)
- `scope.description`: Human-readable description
- `parent.scope`: Parent scope for hierarchy (empty string for root)
- `registryService.target`: OSGi filter to select which registries are available in this scope

### Registry Service Configuration

Registries define the stages, workflow transitions, and schema validation. Each registry requires a `RegistryService` instance:

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
    "delete.after.transition": true,
    "storageService.target": "(storage.type=file)",
    "schema.uri": "http://example.com/models/config",
    "root.eclass.uri": "http://example.com/models/config#//Configuration"
  }
}
```

**Key Properties**:
- `registry.name`: Registry identifier (used in URL paths)
- `stages`: Stage definitions with `writable` and `final` flags
- `workflow.transitions`: Allowed transitions (format: `"fromStage:toStage"`)
- `stage.storage.mappings`: Maps stages to storage types (format: `"stage:storageType"`)
- `storageService.target`: OSGi filter to select the storage service
- `root.eclass.uri`: The expected root EClass for schema validation

### Storage Backend Configuration

Storage backends are configured once per type in `storage.json`. Multiple stages and registries can share the same storage:

```json
{
  "ApicurioObjectStorage~apicurio": {
    "base.url": "http://localhost:8081/apis/registry/v3/",
    "storage.type": "apicurio",
    "registry.target": "(registry=main)"
  },
  "FileObjectStorage~file": {
    "workspace.folder": "/data/storage",
    "storage.type": "file",
    "registry.target": "(registry=main)"
  }
}
```

**Key Properties**:
- `storage.type`: Identifies the storage backend (referenced in `stage.storage.mappings`)
- `workspace.folder` (FileObjectStorage): Root folder for file-based storage
- `base.url` (ApicurioObjectStorage): Apicurio Registry API URL

---

## Security Considerations

### Multi-Tenancy Isolation

- Each scope has complete isolation at the scope service level
- Cross-scope access only via explicit parent-child relationships
- No direct access to other tenants' data

### Read-Only Enforcement

- Parent objects cannot be modified from child scopes
- Both enforced at API and scope service levels

### Schema Validation

- All objects are validated against their registry's expected schema
- Prevents storing malformed or incompatible data

### Input Validation

- All objectId parameters are validated
- Stage names validated against configured stages
- Scope and registry names validated via collectors

---

## OpenAPI/Swagger Documentation

The resource is annotated with Swagger/OpenAPI v3 annotations for automatic API documentation:

- **Access Swagger UI**: Navigate to `/swagger-api/` endpoint
- **Tag**: "Storage Management"
- **All operations** include:
  - Summary and detailed descriptions
  - Parameter documentation
  - Response codes and schemas

---

## Troubleshooting

### "Scope not found" (400)

**Cause**: No `ScopeService` registered for the scope

**Solution**:
1. Verify scope configuration exists
2. Check OSGi Config Admin
3. Verify `ScopeServiceCollector` has bound the service

---

### "Unknown or unconfigured registry" (400)

**Cause**: No `RegistryService` registered for the registry name

**Solution**:
1. Verify registry configuration exists
2. Check OSGi Config Admin for registry service configurations
3. Verify `RegistryServiceCollector` has bound the service

---

### "Object type not compatible with registry" (400)

**Cause**: The object's EClass doesn't match the registry's expected root EClass

**Solution**:
1. Check the object's type/EClass
2. Verify the registry's expected root EClass
3. Ensure the object conforms to the correct schema

---

### "Object not found" (204)

**Cause**: Object doesn't exist in the specified scope/registry/stage or parent hierarchy

**Solution**:
1. Verify the objectId is correct
2. Check if the object exists in the specified stage
3. For hierarchical lookup, ensure parent scope has the object in its final stage

---

### "Object is read-only" (403)

**Cause**: Object originates from parent scope

**Solution**:
1. Objects from parent scopes cannot be modified in child scopes
2. Create a copy in the local scope if modifications are needed
3. Or modify the object directly in the parent scope

---

## Related Documentation

- [README-Scopes.md](README-Scopes.md) - Scope discovery and metadata
- [README-SchemaPackages.md](README-SchemaPackages.md) - Schema CRUD operations
- [Model Atlas API Specification](Model%20Atlas%20API%20Specification.md) - Complete API spec
- [Model Atlas Object API Specification](Model%20Atlas%20Object%20API%20Specification.md) - Storage API design spec
- [ScopeService README](../org.eclipse.fennec.model.atlas.workflow/README.md) - Scope service details
- [CLAUDE.md](../CLAUDE.md) - Project overview and build instructions

---

## License

Eclipse Public License 2.0 (EPL-2.0)

Copyright (c) 2012 - 2026 Data In Motion and others.
