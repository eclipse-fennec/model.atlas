# SchemaPackagesResource REST API

## Overview

The **SchemaPackagesResource** provides a RESTful HTTP API for managing EMF EPackages (schemas) within the Model Atlas system. It implements the [Model Atlas API Specification](Model%20Atlas%20API%20Specification.md) and provides full CRUD operations with stage-based lifecycle management and hierarchical scope visibility.

## Key Features

- **Scope-Based Management**: Multi-tenant isolation via configurable scopes
- **Stage-Based Lifecycle**: Manage schemas through workflow stages (draft, review, release, etc.)
- **Hierarchical Visibility**: Child scopes can access schemas from parent scopes' final stages
- **Content Negotiation**: Support for multiple formats (JSON, XML, Ecore, JSON Schema)
- **Stage Transitions**: Move schemas between workflow stages with validation
- **Uniqueness Validation**: Enforce unique `nsUri` across visibility chains
- **Read-Only Protection**: Prevent modification of parent scope schemas and read-only stages

## Architecture

### Component Dependencies

```
┌────────────────────────────┐
│  SchemaPackagesResource    │
│  (JAX-RS REST Endpoint)    │
└─────────┬──────────┬───────┘
          │          │
          │          │
    ┌─────▼─────┐  ┌▼─────────────────────┐
    │  Scope    │  │  EObjectWorkflow     │
    │ Collector │  │  Service             │
    └───────────┘  └──────────────────────┘
          │                   │
          │                   │
    ┌─────▼───────────────────▼──────┐
    │  Workflow Service Registry     │
    │  (OSGi Dynamic Services)       │
    └────────────────────────────────┘
```

### Integration Points

#### **ScopeCollector**
The `ScopeCollector` dynamically tracks all registered `EObjectWorkflowService` instances and constructs `Scope` objects from their OSGi configuration properties.

**Key Methods:**
- `getWorkflowServiceByScope(String scopeName)`: Retrieves the workflow service for a specific scope
- `getScopeByName(String name)`: Retrieves scope metadata
- `getScopes()`: Lists all available scopes

#### **EObjectWorkflowService**
Provides the underlying workflow operations for schema management within a scope.

**Used Operations:**
- `listInFinalStage()`: List schemas in the final/released stage (with hierarchy)
- `listInStage(String stage)`: List schemas in a specific stage
- `getFromStage(String stage, String objectId)`: Retrieve schema metadata
- `getContentFromStage(String stage, String objectId)`: Retrieve actual EPackage content
- `uploadToStage(String stage, EPackage object, ObjectMetadata metadata)`: Create new schema
- `updateInStage(String stage, EPackage object, String objectId)`: Update existing schema
- `deleteFromStage(String stage, String objectId)`: Delete schema
- `transitionToStage(String objectId, String fromStage, String toStage)`: Move between stages
- `isTransitionAllowed(String fromStage, String toStage)`: Validate transitions

## API Specification Compliance

This implementation follows the **Model Atlas API Specification** which defines:

### Core Concepts (Spec Section 1)

1. **Scope**: Logical tenant/partition with hierarchical structure
   - Special `atlas` scope contains read-only system schemas
   - Custom scopes (e.g., `my-tenant`, `global-corporate`) for user schemas

2. **Stage**: Lifecycle state within a scope (e.g., Draft, Review, Released)
   - Stages have defined order and transition rules
   - Some stages are read-only (typically "Released")

3. **nsUri as Primary Key**: All lookups use the EPackage namespace URI
   - NOT system-generated IDs
   - Must be URL-encoded in query parameters

4. **Hierarchical Visibility** (Spec Section 1)
   - **Write-Time Uniqueness**: `nsUri` must be unique within visibility chain (local scope + all parent final stages)
   - **Read-Time Visibility**: Searches local scope first, then parent final stages recursively
   - Parent packages appear as read-only in child scopes

## Resource Path Structure

All endpoints are rooted at: `/{scopeName}/schema`

Where:
- `{scopeName}` - The scope identifier (e.g., "my-tenant", "global-corporate")

## API Endpoints

### 1. List Released Packages

**Spec Reference**: Section 2, `GET /{scopeName}/schema`

```http
GET /{scopeName}/schema
Accept: application/json
```

**Purpose**: List all packages in the final/released stage for this scope, including packages from parent scopes' released stages.

**Behavior**:
- Calls `workflowService.listInFinalStage()` which implements hierarchical lookup
- Returns schemas from local scope AND all parent scopes (recursively)
- Parent schemas are marked as read-only

**Response**:
- **200 OK**: Returns `ObjectMetadataContainer` with list of `ObjectMetadata`
- **404 Not Found**: Scope does not exist

**Example**:
```bash
curl -X GET https://api.example.com/my-tenant/schema \
  -H "Accept: application/json"
```

**Example Response**:
```json
{
  "metadata": [
    {
      "objectId": "http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1",
      "objectName": "Billing",
      "scope": "my-tenant",
      "role": "release",
      "version": "1.0.0",
      "isReadOnly": false,
      "uploadTime": "2023-10-27T10:00:00Z",
      "lastChangeTime": "2023-10-27T10:00:00Z"
    },
    {
      "objectId": "http%3A%2F%2Fexample.com%2Fschemas%2Fcommon%2Fv1",
      "objectName": "CommonTypes",
      "scope": "atlas",
      "role": "release",
      "version": "2.0.0",
      "isReadOnly": true,
      "uploadTime": "2023-01-15T08:00:00Z",
      "lastChangeTime": "2023-01-15T08:00:00Z"
    }
  ]
}
```

---

### 2. List Packages in Specific Stage

**Spec Reference**: Section 2, `GET /{scopeName}/schema/stages/{stageName}`

```http
GET /{scopeName}/schema/stages/{stageName}
Accept: application/json
```

**Purpose**: List all packages within a specific stage of a scope.

**Query Parameters**:
- `nsUri` (optional): Find single package by exact namespace URI (hierarchical lookup)
- `name` (optional): Filter by package name (supports wildcards, e.g., `*Billing*`)

**Behavior**:
- **Without `nsUri`**: Lists all packages in the specified stage (local scope only)
- **With `nsUri`**: Performs hierarchical lookup (local stage, then parent final stages)

**Response**:
- **200 OK**: Returns `ObjectMetadataContainer` with list of `ObjectMetadata`
- **404 Not Found**: Scope or stage does not exist

**Example**:
```bash
# List all packages in draft stage
curl -X GET https://api.example.com/my-tenant/schema/stages/draft \
  -H "Accept: application/json"

# Find specific package by nsUri (with hierarchical lookup)
curl -X GET "https://api.example.com/my-tenant/schema/stages/draft?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1" \
  -H "Accept: application/json"
```

---

### 3. Create Package

**Spec Reference**: Section 2, `POST /{scopeName}/schema/stages/{stageName}`

```http
POST /{scopeName}/schema/stages/{stageName}
Content-Type: application/json | application/xml | application/ecore+xml
```

**Purpose**: Create a new SchemaPackage in the specified stage.

**Query Parameters** (Required):
- `nsUri` (string, required): The namespace URI of the package
- `name` (string, optional): Human-readable name
- `version` (string, optional): Package version

**Request Body**: EPackage content in the specified format

**Behavior** (Per Spec):
1. Validates `nsUri` is provided
2. **Checks uniqueness across visibility chain**: Searches local scope (all stages) AND parent final stages
3. Returns `409 Conflict` if `nsUri` exists anywhere in visibility chain
4. Creates package with metadata
5. URL-encodes the `nsUri` for use as object ID

**Response**:
- **201 Created**: Package created successfully
  - `Location` header: `/{scopeName}/schema/stages/{stageName}?nsUri={encodedNsUri}`
  - Body: `ObjectMetadata` with created package info
- **400 Bad Request**: Invalid package data or missing `nsUri`
- **404 Not Found**: Scope or stage does not exist
- **409 Conflict**: Package with `nsUri` already exists in visibility chain
- **415 Unsupported Media Type**: Invalid Content-Type
- **500 Internal Server Error**: Server error

**Example**:
```bash
curl -X POST "https://api.example.com/my-tenant/schema/stages/draft?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1&name=Billing&version=1.0.0" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "billing",
    "nsURI": "http://example.com/schemas/billing/v1",
    "nsPrefix": "bill",
    "eClassifiers": [...]
  }'
```

**Example Response** (201 Created):
```json
{
  "objectId": "http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1",
  "objectName": "Billing",
  "scope": "my-tenant",
  "role": "draft",
  "version": "1.0.0",
  "isReadOnly": false,
  "uploadTime": "2023-10-27T10:00:00Z",
  "lastChangeTime": "2023-10-27T10:00:00Z"
}
```

---

### 4. Get Package Content

**Spec Reference**: Section 2, `GET /{scopeName}/schema/stages/{stageName}/content`

```http
GET /{scopeName}/schema/stages/{stageName}/content?nsUri={encodedNsUri}
Accept: application/json | application/xml | application/ecore+xml | application/schema+json
```

**Purpose**: Retrieve the actual content (EPackage) of a schema in the requested format.

**Query Parameters**:
- `nsUri` (required): The namespace URI of the package

**Behavior**:
- URL-decodes and encodes the `nsUri` parameter
- Performs hierarchical lookup via `getContentFromStage()`
- Returns the EPackage object (serialized based on Accept header)

**Response**:
- **200 OK**: Package content retrieved successfully
  - Body: EPackage in requested format
  - `Content-Type` header matches requested format
- **204 No Content**: Package metadata exists but no content
- **404 Not Found**: Package not found in visibility chain
- **406 Not Acceptable**: Requested format not supported
- **500 Internal Server Error**: Server error

**Example**:
```bash
# Get as JSON
curl -X GET "https://api.example.com/my-tenant/schema/stages/draft/content?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1" \
  -H "Accept: application/json"

# Get as Ecore XML
curl -X GET "https://api.example.com/my-tenant/schema/stages/draft/content?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1" \
  -H "Accept: application/ecore+xml"
```

---

### 5. Update Package Content

**Spec Reference**: Section 2, `PUT /{scopeName}/schema/stages/{stageName}/content`

```http
PUT /{scopeName}/schema/stages/{stageName}/content?nsUri={encodedNsUri}
Content-Type: application/json | application/xml | application/ecore+xml
```

**Purpose**: Replace the content of an existing SchemaPackage.

**Query Parameters**:
- `nsUri` (required): The namespace URI of the package

**Request Body**: New EPackage content

**Behavior**:
1. Checks if stage is writable (using `Scope.getWritableStages()`)
2. Retrieves existing metadata via `getFromStage()` (hierarchical lookup)
3. Checks if package is read-only (`isReadOnly` flag)
4. Updates package via `updateInStage()`

**Protection Rules**:
- Cannot update if stage is not in writable stages list
- Cannot update if package is from parent scope (read-only)
- Cannot update if package doesn't exist locally

**Response**:
- **200 OK**: Package updated successfully
  - Body: Updated `ObjectMetadata`
- **400 Bad Request**: Invalid package data
- **403 Forbidden**: Stage is read-only OR package is from parent scope
- **404 Not Found**: Scope, stage, or package not found
- **500 Internal Server Error**: Server error

**Example**:
```bash
curl -X PUT "https://api.example.com/my-tenant/schema/stages/draft/content?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "billing",
    "nsURI": "http://example.com/schemas/billing/v1",
    "nsPrefix": "bill",
    "eClassifiers": [...]
  }'
```

---

### 6. Delete Package

**Spec Reference**: Section 2, `DELETE /{scopeName}/schema/stages/{stageName}`

```http
DELETE /{scopeName}/schema/stages/{stageName}?nsUri={encodedNsUri}
```

**Purpose**: Delete a SchemaPackage from the specified stage.

**Query Parameters**:
- `nsUri` (required): The namespace URI of the package to delete

**Behavior**:
1. Checks if stage is writable
2. Retrieves package metadata to verify existence
3. Checks if package is read-only
4. Deletes via `deleteFromStage()`

**Protection Rules**:
- Cannot delete from read-only stages
- Cannot delete parent scope packages
- Cannot delete if package doesn't exist locally

**Response**:
- **204 No Content**: Package deleted successfully
- **403 Forbidden**: Stage is read-only OR package is from parent scope
- **404 Not Found**: Package not found
- **500 Internal Server Error**: Server error

**Example**:
```bash
curl -X DELETE "https://api.example.com/my-tenant/schema/stages/draft?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1"
```

---

### 7. Transition Package Between Stages

**Spec Reference**: Section 3, `POST /{scopeName}/schema/stages/{stageName}/actions/transition`

```http
POST /{scopeName}/schema/stages/{stageName}/actions/transition
Content-Type: application/json
```

**Purpose**: Move a package from the current stage to a target stage.

**Request Body**:
```json
{
  "objectId": "http://example.com/schemas/billing/v1",
  "targetStage": "review"
}
```

**Body Fields**:
- `objectId` (string, required): The `nsUri` of the package (NOT the encoded version)
- `targetStage` (string, required): The target stage name

**Behavior**:
1. Encodes the `objectId` (nsUri) for lookup
2. Verifies package exists in source stage
3. Checks if package is read-only (cannot transition parent packages)
4. Validates transition is allowed via `isTransitionAllowed()`
5. Performs transition via `transitionToStage()`

**Response**:
- **200 OK**: Package transitioned successfully
  - Body: Updated `ObjectMetadata` with new stage
- **400 Bad Request**: Invalid transition or missing parameters
- **404 Not Found**: Package not found in source stage
- **500 Internal Server Error**: Server error

**Example**:
```bash
curl -X POST "https://api.example.com/my-tenant/schema/stages/draft/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{
    "objectId": "http://example.com/schemas/billing/v1",
    "targetStage": "review"
  }'
```

**Example Response** (200 OK):
```json
{
  "objectId": "http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1",
  "objectName": "Billing",
  "scope": "my-tenant",
  "role": "review",
  "version": "1.0.0",
  "isReadOnly": false,
  "uploadTime": "2023-10-27T10:00:00Z",
  "lastChangeTime": "2023-10-27T11:30:00Z"
}
```

---

## Hierarchical Visibility in Detail

### How It Works

The Model Atlas API Specification defines strict rules for hierarchical visibility:

#### **Write-Time (Uniqueness Check)**

When creating a new package (`POST`):

```
Check for nsUri in:
  1. Current scope, all stages (draft, review, release)
  2. Parent scope, final stage only
  3. Grandparent scope, final stage only
  ... (recursively up to root)

If found anywhere → 409 Conflict
If not found → Create package
```

**Example**:
```
Hierarchy: atlas (root) → global-corporate → my-tenant

Creating in my-tenant/draft with nsUri="http://example.com/billing/v1"

Search locations:
  ✓ my-tenant/draft
  ✓ my-tenant/review
  ✓ my-tenant/release
  ✓ global-corporate/release (final stage)
  ✓ atlas/release (final stage)

If found in ANY location → 409 Conflict
```

#### **Read-Time (Retrieval)**

When retrieving a package by `nsUri` (`GET` with `nsUri` parameter):

```
Search for nsUri in:
  1. Current scope, specified stage
  2. If not found AND parent exists:
     → Search parent scope, final stage
  3. If not found AND grandparent exists:
     → Search grandparent scope, final stage
  ... (recursively up to root)

Return first match, mark as read-only if from parent
```

**Example**:
```
Hierarchy: atlas → global-corporate → my-tenant
Request: GET /my-tenant/schema/stages/draft?nsUri=http://example.com/common/v1

Search order:
  1. my-tenant/draft → Not found
  2. global-corporate/release → Found! (mark as read-only)

Response: Package from global-corporate, isReadOnly=true
```

### Read-Only Flag Behavior

Packages from parent scopes are automatically marked as read-only:

- **`isReadOnly: true`**: Package originates from a parent scope
- **`isReadOnly: false`**: Package exists in local scope

**Operations on Read-Only Packages**:
- ✅ GET (retrieve metadata and content)
- ✅ LIST (appears in lists)
- ❌ PUT (update) → 403 Forbidden
- ❌ DELETE → 403 Forbidden
- ❌ Transition → 400 Bad Request

### Writable Stages

Scopes can configure which stages are writable via the `writable_stages` configuration property:

```properties
stages=["draft", "review", "approved", "release"]
writable.stages=["draft", "review", "approved"]
final.stage=release
```

In this example:
- **Writable**: draft, review, approved
- **Read-Only**: release (final stage is typically read-only)

**API Enforcement**:
- `PUT` and `DELETE` operations check if stage is in `writable_stages`
- Returns `403 Forbidden` if stage is not writable

---

## URL Encoding of nsUri

### Why URL Encoding?

The `nsUri` typically contains characters that are not URL-safe (e.g., `:`, `/`, `?`). When used as a query parameter or object ID, it must be URL-encoded.

**Example**:
```
Original:  http://example.com/schemas/billing/v1
Encoded:   http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1
```

### Implementation Details

The `encodePackageNsURI()` helper method handles encoding:

```java
private String encodePackageNsURI(String nsUri) throws UnsupportedEncodingException {
    return URLEncoder.encode(nsUri, StandardCharsets.UTF_8.toString());
}
```

**Usage**:
- All internal object IDs use encoded `nsUri`
- API consumers provide raw `nsUri` in query parameters
- API automatically encodes/decodes as needed

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 OK | Success | GET, PUT, POST (transition) successful |
| 201 Created | Resource created | POST (create package) successful |
| 204 No Content | Success, no body | DELETE successful, or GET content with no data |
| 400 Bad Request | Invalid request | Invalid transition, missing required parameters |
| 403 Forbidden | Operation not allowed | Stage is read-only, package is from parent scope |
| 404 Not Found | Resource not found | Scope, stage, or package doesn't exist |
| 409 Conflict | Resource already exists | `nsUri` exists in visibility chain during creation |
| 415 Unsupported Media Type | Invalid Content-Type | Unsupported format in POST/PUT |
| 500 Internal Server Error | Server error | Unexpected errors, exceptions |

### Common Error Scenarios

#### **Creating Duplicate Package**

```bash
POST /my-tenant/schema/stages/draft?nsUri=http://existing.com/v1

→ 409 Conflict
```

**Reason**: Package with same `nsUri` exists in visibility chain

---

#### **Updating Parent Package**

```bash
PUT /my-tenant/schema/stages/draft/content?nsUri=http://parent.com/v1

→ 403 Forbidden
```

**Reason**: Package originates from parent scope (read-only)

---

#### **Deleting from Read-Only Stage**

```bash
DELETE /my-tenant/schema/stages/release?nsUri=http://example.com/v1

→ 403 Forbidden
```

**Reason**: "release" stage is not in writable stages

---

#### **Invalid Transition**

```bash
POST /my-tenant/schema/stages/draft/actions/transition
{
  "objectId": "http://example.com/v1",
  "targetStage": "production"  # stage doesn't exist
}

→ 400 Bad Request
```

**Reason**: Target stage not configured or transition not allowed

---

## Configuration Requirements

### Scope/Workflow Service Configuration

Each scope requires an `EObjectWorkflowService` instance configured via OSGi Config Admin:

```properties
# Scope identity
scope=my-tenant
description=Primary tenant workspace
parent.scope=global-corporate
parentWorkflowService.target=(scope=global-corporate)

# Workflow stages
stages=["draft", "review", "approved", "release"]
writable.stages=["draft", "review", "approved"]
final.stage=release

# Policies
delete.after.transition=true
```

**Key Properties for API**:
- `scope`: Scope identifier (used in URL paths)
- `stages`: All valid stage names
- `writable_stages`: Stages that allow PUT/DELETE
- `final.stage`: Stage used for hierarchical lookup
- `parent.scope`: Parent scope for hierarchy (empty string for root)

### Storage Backend Configuration

Each stage requires a storage backend:

```properties
# Draft storage
workspace.folder=/data/my-tenant/draft
storage.scope=my-tenant
storage.role=draft

# Release storage
workspace.folder=/data/my-tenant/release
storage.scope=my-tenant
storage.role=release
```

The `storage.role` must match a configured stage name.

---

## Content Negotiation

### Supported Media Types

**Request (POST/PUT)**:
- `application/json` - EMF JSON format
- `application/xml` - Generic XML format
- `application/ecore+xml` - Ecore-specific XML

**Response (GET content)**:
- `application/json` - EMF JSON format
- `application/xml` - Generic XML format
- `application/ecore+xml` - Ecore-specific XML
- `application/schema+json` - JSON Schema format

### Usage Examples

**Create with JSON**:
```bash
curl -X POST "..." \
  -H "Content-Type: application/json" \
  -d '{"name":"billing", "nsURI":"...", ...}'
```

**Retrieve as Ecore XML**:
```bash
curl -X GET "..." \
  -H "Accept: application/ecore+xml"
```

**Retrieve as JSON Schema**:
```bash
curl -X GET "..." \
  -H "Accept: application/schema+json"
```

---

## Complete Workflow Example

### Scenario: Creating and Publishing a Schema

```bash
# 1. Create package in draft stage
curl -X POST "https://api.example.com/my-tenant/schema/stages/draft?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1&name=Billing&version=1.0.0" \
  -H "Content-Type: application/json" \
  -d @billing-schema.json

# Response: 201 Created
# Location: /my-tenant/schema/stages/draft?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1

# 2. Review and update if needed
curl -X PUT "https://api.example.com/my-tenant/schema/stages/draft/content?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1" \
  -H "Content-Type: application/json" \
  -d @billing-schema-updated.json

# Response: 200 OK

# 3. Transition to review stage
curl -X POST "https://api.example.com/my-tenant/schema/stages/draft/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{
    "objectId": "http://example.com/schemas/billing/v1",
    "targetStage": "review"
  }'

# Response: 200 OK (now in review stage)

# 4. Transition to approved stage
curl -X POST "https://api.example.com/my-tenant/schema/stages/review/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{
    "objectId": "http://example.com/schemas/billing/v1",
    "targetStage": "approved"
  }'

# Response: 200 OK

# 5. Transition to release stage (final)
curl -X POST "https://api.example.com/my-tenant/schema/stages/approved/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{
    "objectId": "http://example.com/schemas/billing/v1",
    "targetStage": "release"
  }'

# Response: 200 OK (now in release stage)

# 6. Verify it appears in released packages list
curl -X GET "https://api.example.com/my-tenant/schema" \
  -H "Accept: application/json"

# Response: 200 OK (includes billing schema)

# 7. Child scopes can now access it
curl -X GET "https://api.example.com/child-tenant/schema" \
  -H "Accept: application/json"

# Response: 200 OK (includes billing schema with isReadOnly=true)
```

---

## Security Considerations

### Multi-Tenancy Isolation

- Each scope has complete isolation at the workflow service level
- Cross-scope access only via explicit parent-child relationships
- No direct access to other tenants' data

### Read-Only Enforcement

- Parent packages cannot be modified from child scopes
- Read-only stages prevent accidental modifications
- Both enforced at API and workflow service levels

### Input Validation

- All `nsUri` parameters are validated and encoded
- Stage names validated against configured stages
- Scope names validated via `ScopeCollector`

---

## OpenAPI/Swagger Documentation

The resource is annotated with Swagger/OpenAPI v3 annotations for automatic API documentation:

- **Access Swagger UI**: Navigate to `/swagger-api/` endpoint
- **Tag**: "Schema Management"
- **All operations** include:
  - Summary and detailed descriptions
  - Parameter documentation
  - Response codes and schemas
  - Example requests/responses

---

## Testing

Comprehensive integration tests can be found in:
- `ScopeAwareWorkflowServiceTest.java` - Tests workflow service operations
- Tests cover:
  - Single scope operations
  - Hierarchical scope lookups
  - Stage transitions
  - Final stage operations
  - Custom stage configurations

---

## Troubleshooting

### "Scope not found" (404)

**Cause**: No `EObjectWorkflowService` registered for the scope

**Solution**:
1. Verify scope configuration exists
2. Check OSGi Config Admin
3. Verify `ScopeCollector` has bound the service

```bash
# Check logs for:
Cannot store EObjectWorkflowService with scope property not set or empty
```

---

### "Package not found" but it exists in parent

**Cause**: Package only in parent's non-final stage

**Solution**: Hierarchical lookup only checks parent **final stages**. Transition parent package to final stage first.

---

### "Forbidden" when updating

**Cause**: Stage is read-only OR package is from parent scope

**Solution**:
1. Check if stage is in `writable_stages` configuration
2. Verify package is not from parent scope (`isReadOnly: false`)
3. Transition to writable stage if needed

---

### "Conflict" when creating

**Cause**: `nsUri` exists in visibility chain

**Solution**:
1. Check all stages in current scope
2. Check parent scope final stages
3. Use different `nsUri` or update existing package

---

## Related Documentation

- [Model Atlas API Specification](Model%20Atlas%20API%20Specification.md) - Complete API spec
- [EObjectWorkflowService README](../org.eclipse.fennec.model.atlas.workflow/README.md) - Workflow service details
- [CLAUDE.md](../CLAUDE.md) - Project overview and build instructions

---

## License

Eclipse Public License 2.0 (EPL-2.0)

Copyright (c) 2012 - 2025 Data In Motion and others.
