# ScopesResource REST API

## Overview

The **ScopesResource** provides a RESTful HTTP API for discovering and retrieving scope configuration metadata in the Model Atlas system. Scopes are the fundamental organizational units that provide multi-tenant isolation and hierarchical visibility for schema management.

## Key Features

- **Scope Discovery**: List all configured scopes in the system
- **Scope Metadata Retrieval**: Get detailed configuration for specific scopes
- **Hierarchical Information**: View parent-child relationships between scopes
- **Stage Configuration**: Discover available workflow stages and their properties
- **Lightweight API**: Read-only operations for browsing scope topology

## Architecture

### Component Dependencies

```
┌────────────────────────────┐
│    ScopesResource          │
│  (JAX-RS REST Endpoint)    │
└─────────────┬──────────────┘
              │
              │
        ┌─────▼─────┐
        │  Scope    │
        │ Collector │
        └─────┬─────┘
              │
              │
   ┌──────────▼────────────────┐
   │  EObjectWorkflowService   │
   │  (OSGi Dynamic Services)  │
   └───────────────────────────┘
```

### Integration Points

#### **ScopeCollector**
The `ScopeCollector` dynamically tracks all registered `EObjectWorkflowService` instances and constructs `Scope` objects from their OSGi configuration properties.

**Key Methods:**
- `getScopes()`: Returns list of all configured scopes
- `getScopeByName(String name)`: Retrieves specific scope metadata

#### **Scope Model**
Each scope contains the following metadata:

| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Unique identifier for the scope (e.g., "my-tenant", "global") |
| `parentScope` | String | Name of parent scope (empty for root scopes) |
| `description` | String | Human-readable description of the scope's purpose |
| `stages` | List<String> | All workflow stages configured for this scope |
| `finalStage` | String | The final/released stage (used for hierarchical lookups) |
| `writableStages` | List<String> | Stages that allow modifications (create/update/delete) |
| `links` | Map<String, String> | Optional links to related resources (e.g., documentation) |

## Resource Path Structure

All endpoints are rooted at: `/scopes`

## API Endpoints

### 1. List All Scopes

```http
GET /scopes
Accept: application/json
```

**Purpose**: Retrieve a list of all configured scopes in the Model Atlas system.

**Response**:
- **200 OK**: Returns `ScopeContainer` with list of `Scope` objects
- **500 Internal Server Error**: Server error

**Example Request**:
```bash
curl -X GET https://api.example.com/scopes \
  -H "Accept: application/json"
```

**Example Response**:
```json
{
  "scopes": [
    {
      "name": "atlas",
      "parentScope": "",
      "description": "System-level scope containing read-only base schemas",
      "stages": ["release"],
      "finalStage": "release",
      "writableStages": [],
      "links": {}
    },
    {
      "name": "global-corporate",
      "parentScope": "atlas",
      "description": "Corporate-wide shared schemas",
      "stages": ["draft", "approved", "release"],
      "finalStage": "release",
      "writableStages": ["draft", "approved"],
      "links": {
        "documentation": "https://wiki.example.com/global-corporate"
      }
    },
    {
      "name": "my-tenant",
      "parentScope": "global-corporate",
      "description": "Tenant-specific schema workspace",
      "stages": ["draft", "review", "approved", "release"],
      "finalStage": "release",
      "writableStages": ["draft", "review", "approved"],
      "links": {}
    }
  ]
}
```

---

### 2. Get Scope Metadata

```http
GET /scopes/{scopeName}
Accept: application/json
```

**Purpose**: Retrieve detailed metadata for a specific scope.

**Path Parameters**:
- `scopeName` (required): The name of the scope (e.g., "my-tenant")

**Response**:
- **200 OK**: Returns `Scope` object with metadata
- **204 No Content**: Scope not found (no scope with that name exists)
- **500 Internal Server Error**: Server error

**Example Request**:
```bash
curl -X GET https://api.example.com/scopes/my-tenant \
  -H "Accept: application/json"
```

**Example Response**:
```json
{
  "name": "my-tenant",
  "parentScope": "global-corporate",
  "description": "Tenant-specific schema workspace for customer ABC",
  "stages": ["draft", "review", "approved", "release"],
  "finalStage": "release",
  "writableStages": ["draft", "review", "approved"],
  "links": {
    "schemas": "/my-tenant/schema",
    "documentation": "https://docs.example.com/my-tenant"
  }
}
```

---

## Understanding Scope Hierarchy

### Hierarchical Structure

Scopes can be organized in a parent-child hierarchy to enable:
1. **Shared schemas**: Parent scopes publish schemas that children can access
2. **Organizational structure**: Mirror your organization's tenant structure
3. **Visibility control**: Children see parent's final stage, but not vice versa

**Example Hierarchy**:
```
atlas (root)
  ├─ global-corporate
  │    ├─ department-a
  │    │    └─ team-alpha
  │    └─ department-b
  └─ partner-company
```

### Parent-Child Relationships

**Visibility Rules**:
- Child scopes can **read** schemas from parent's final stage
- Child scopes **cannot modify** parent schemas (read-only)
- Parent scopes **cannot see** child schemas
- Siblings **cannot see** each other's schemas

**Use Cases**:
- **Root "atlas" scope**: System-wide common types (Address, DateTime, etc.)
- **Corporate scope**: Company-wide business objects (Customer, Order, etc.)
- **Department scopes**: Department-specific schemas
- **Team scopes**: Team or project-specific schemas

---

## Workflow Stages

### Stage Configuration

Each scope defines its own workflow stages that control the lifecycle of schemas within that scope.

**Common Stage Patterns**:

#### Simple (2-stage)
```json
{
  "stages": ["draft", "release"],
  "finalStage": "release",
  "writableStages": ["draft"]
}
```
- **draft**: Work in progress
- **release**: Published and available to child scopes

#### Standard (3-stage)
```json
{
  "stages": ["draft", "approved", "release"],
  "finalStage": "release",
  "writableStages": ["draft", "approved"]
}
```
- **draft**: Initial development
- **approved**: Reviewed and approved
- **release**: Published (read-only)

#### Complex (4+ stages)
```json
{
  "stages": ["draft", "review", "approved", "staging", "production"],
  "finalStage": "production",
  "writableStages": ["draft", "review", "approved", "staging"]
}
```

### Stage Properties

**`stages`**: All available stages in order
- Defines the complete workflow
- Order matters for transition validation

**`finalStage`**: The published/released stage
- Used for hierarchical lookups (children see parent's final stage)
- Typically the last stage in the workflow
- Usually read-only

**`writableStages`**: Stages that allow modifications
- Controls which stages accept PUT/DELETE/POST operations
- Final stages typically excluded for safety
- Can be customized per organizational policy

---

## Scope Configuration

### OSGi Configuration

Scopes are defined via OSGi Config Admin factory configurations:

```properties
# File: EObjectWorkflowService~my-tenant.cfg

# Scope Identity
scope=my-tenant
description=Tenant-specific schema workspace
parent.scope=global-corporate
parentWorkflowService.target=(scope=global-corporate)

# Workflow Stages
stages=["draft", "review", "approved", "release"]
writable.stages=["draft", "review", "approved"]
final.stage=release

# Policies
delete.after.transition=true
```

**Required Properties**:
- `scope`: Unique scope identifier
- `stages`: Array of stage names
- `final.stage`: Final/released stage name

**Optional Properties**:
- `description`: Human-readable description
- `parent.scope`: Parent scope name (empty for root)
- `parentWorkflowService.target`: OSGi filter for parent service
- `writable.stages`: Writable stages (defaults to all stages)
- `delete.after.transition`: Delete from source after transition

### Storage Backend Configuration

Each stage requires a corresponding storage backend:

```properties
# File: FileObjectStorage~my-tenant-draft.cfg
workspace.folder=/data/scopes/my-tenant/draft
storage.scope=my-tenant
storage.role=draft

# File: FileObjectStorage~my-tenant-release.cfg
workspace.folder=/data/scopes/my-tenant/release
storage.scope=my-tenant
storage.role=release
```

The `storage.role` must match a stage name in the scope's `stages` configuration.

---

## Use Cases

### 1. Discovering Available Scopes

**Scenario**: A client needs to know which scopes are available for schema management.

```bash
# List all scopes
curl -X GET https://api.example.com/scopes

# Returns:
# - atlas (root, system schemas)
# - global-corporate (company-wide)
# - my-tenant (specific tenant)
```

**Use Case**: Populating a dropdown/selector in a UI for scope selection.

---

### 2. Understanding Scope Hierarchy

**Scenario**: A client needs to understand which parent scopes provide shared schemas.

```bash
# Get my-tenant metadata
curl -X GET https://api.example.com/scopes/my-tenant

# Response shows:
{
  "name": "my-tenant",
  "parentScope": "global-corporate",  ← Parent scope
  ...
}

# Get parent metadata
curl -X GET https://api.example.com/scopes/global-corporate

# Response shows:
{
  "name": "global-corporate",
  "parentScope": "atlas",  ← Grandparent scope
  "finalStage": "release",  ← my-tenant can see schemas from this stage
  ...
}
```

**Use Case**: Building a hierarchical browser that shows which schemas are inherited from parent scopes.

---

### 3. Validating Stage Names

**Scenario**: A client wants to create or transition a schema and needs to know which stages are available.

```bash
# Get scope configuration
curl -X GET https://api.example.com/scopes/my-tenant

# Response shows available stages:
{
  "stages": ["draft", "review", "approved", "release"],
  "writableStages": ["draft", "review", "approved"]
}

# Now client knows:
# - Can create in: draft, review, approved
# - Can transition to: draft → review → approved → release
# - Cannot modify: release (not in writableStages)
```

**Use Case**: Client-side validation before attempting schema operations.

---

### 4. Building Multi-Tenant Applications

**Scenario**: SaaS application with multiple tenants, each needing isolated schema workspaces.

```bash
# List all tenant scopes
curl -X GET https://api.example.com/scopes

# Filter scopes by tenant:
# - tenant-a (parentScope: "corporate")
# - tenant-b (parentScope: "corporate")
# - tenant-c (parentScope: "corporate")

# Each tenant has isolation but can access corporate shared schemas
```

**Use Case**: Multi-tenant SaaS dashboard showing each tenant's workspace.

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 OK | Success | Scope(s) retrieved successfully |
| 204 No Content | Not found | Scope with given name doesn't exist |
| 500 Internal Server Error | Server error | Unexpected errors, exceptions |

### Common Error Scenarios

#### **Scope Not Found**

```bash
GET /scopes/non-existent-scope

→ 204 No Content
```

**Reason**: No `EObjectWorkflowService` registered with `scope=non-existent-scope`

**Solution**: Check OSGi Config Admin for scope configurations

---

#### **Empty Scopes List**

```bash
GET /scopes

→ 200 OK
{
  "scopes": []
}
```

**Reason**: No `EObjectWorkflowService` instances registered

**Solution**: Verify workflow services are started and configured

---

## Integration Examples

### JavaScript/TypeScript Client

```typescript
interface Scope {
  name: string;
  parentScope: string;
  description: string;
  stages: string[];
  finalStage: string;
  writableStages: string[];
  links: Record<string, string>;
}

interface ScopeContainer {
  scopes: Scope[];
}

// Fetch all scopes
async function getAllScopes(): Promise<Scope[]> {
  const response = await fetch('https://api.example.com/scopes', {
    headers: { 'Accept': 'application/json' }
  });
  const container: ScopeContainer = await response.json();
  return container.scopes;
}

// Get specific scope
async function getScope(scopeName: string): Promise<Scope | null> {
  const response = await fetch(`https://api.example.com/scopes/${scopeName}`, {
    headers: { 'Accept': 'application/json' }
  });

  if (response.status === 204) {
    return null; // Scope not found
  }

  return await response.json();
}

// Build hierarchy tree
async function buildScopeHierarchy(): Promise<Map<string, Scope[]>> {
  const scopes = await getAllScopes();
  const hierarchy = new Map<string, Scope[]>();

  for (const scope of scopes) {
    const parent = scope.parentScope || 'root';
    if (!hierarchy.has(parent)) {
      hierarchy.set(parent, []);
    }
    hierarchy.get(parent)!.push(scope);
  }

  return hierarchy;
}
```

### Python Client

```python
import requests
from typing import List, Dict, Optional

class Scope:
    def __init__(self, data: dict):
        self.name = data['name']
        self.parent_scope = data.get('parentScope', '')
        self.description = data.get('description', '')
        self.stages = data.get('stages', [])
        self.final_stage = data.get('finalStage', '')
        self.writable_stages = data.get('writableStages', [])
        self.links = data.get('links', {})

class ScopesClient:
    def __init__(self, base_url: str):
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({'Accept': 'application/json'})

    def list_scopes(self) -> List[Scope]:
        """List all configured scopes."""
        response = self.session.get(f"{self.base_url}/scopes")
        response.raise_for_status()
        data = response.json()
        return [Scope(s) for s in data.get('scopes', [])]

    def get_scope(self, scope_name: str) -> Optional[Scope]:
        """Get metadata for a specific scope."""
        response = self.session.get(f"{self.base_url}/scopes/{scope_name}")

        if response.status_code == 204:
            return None  # Scope not found

        response.raise_for_status()
        return Scope(response.json())

    def get_hierarchy(self, scope_name: str) -> List[Scope]:
        """Get full hierarchy chain for a scope (scope → parent → grandparent → ...)"""
        hierarchy = []
        current = self.get_scope(scope_name)

        while current:
            hierarchy.append(current)
            if not current.parent_scope:
                break
            current = self.get_scope(current.parent_scope)

        return hierarchy

# Usage
client = ScopesClient("https://api.example.com")

# List all scopes
scopes = client.list_scopes()
for scope in scopes:
    print(f"{scope.name}: {len(scope.stages)} stages")

# Get specific scope
my_scope = client.get_scope("my-tenant")
if my_scope:
    print(f"Writable stages: {', '.join(my_scope.writable_stages)}")

# Get full hierarchy
hierarchy = client.get_hierarchy("my-tenant")
print("Hierarchy:", " → ".join(s.name for s in reversed(hierarchy)))
```

---

## Best Practices

### 1. **Cache Scope Metadata**

Scope configuration rarely changes at runtime. Cache the results to reduce API calls:

```typescript
class ScopeCache {
  private cache = new Map<string, Scope>();
  private ttl = 5 * 60 * 1000; // 5 minutes

  async getScope(name: string): Promise<Scope | null> {
    if (this.cache.has(name)) {
      return this.cache.get(name)!;
    }

    const scope = await fetchScope(name);
    if (scope) {
      this.cache.set(name, scope);
      setTimeout(() => this.cache.delete(name), this.ttl);
    }
    return scope;
  }
}
```

### 2. **Validate Stage Names Client-Side**

Before attempting schema operations, validate stage names against scope configuration:

```typescript
async function canCreateInStage(scopeName: string, stageName: string): Promise<boolean> {
  const scope = await getScope(scopeName);
  return scope?.writableStages.includes(stageName) ?? false;
}
```

### 3. **Build Hierarchical UI**

Use parent-child relationships to build intuitive navigation:

```typescript
function buildScopeTree(scopes: Scope[]): TreeNode[] {
  const roots = scopes.filter(s => !s.parentScope);

  function buildChildren(parent: Scope): TreeNode {
    const children = scopes
      .filter(s => s.parentScope === parent.name)
      .map(buildChildren);

    return { scope: parent, children };
  }

  return roots.map(buildChildren);
}
```

### 4. **Handle Scope Not Found Gracefully**

The API returns 204 No Content (not 404) when a scope doesn't exist:

```typescript
const scope = await getScope("unknown");
if (!scope) {
  // Show user-friendly message or redirect to scope selector
  console.log("Scope not found. Please select from available scopes.");
}
```

---

## OpenAPI/Swagger Documentation

The resource is annotated with Swagger/OpenAPI v3 annotations for automatic API documentation:

- **Access Swagger UI**: Navigate to `/swagger-api/` endpoint
- **Tag**: "Scope Management"
- **All operations** include:
  - Summary and detailed descriptions
  - Parameter documentation
  - Response codes and schemas

---

## Troubleshooting

### No scopes returned

**Cause**: No `EObjectWorkflowService` instances registered

**Solution**:
1. Check OSGi Config Admin for workflow service configurations
2. Verify services are started: `scr:list | grep EObjectWorkflowService`
3. Check for configuration errors in logs
4. Ensure `scope` property is set in each configuration

---

### Scope not found (204)

**Cause**: No workflow service with matching `scope` property

**Solution**:
1. List all scopes to see available names
2. Check for typos in scope name (case-sensitive)
3. Verify workflow service configuration exists
4. Check `ScopeCollector` has bound the service

---

### Parent scope not visible

**Cause**: Parent scope may not be configured or not started

**Solution**:
1. Verify parent scope exists: `GET /scopes/{parentScopeName}`
2. Check `parentWorkflowService.target` filter matches parent's service properties
3. Verify parent service is active in OSGi

---

## Related Documentation

- [SchemaPackagesResource Documentation](README-SchemaPackages.md) - Schema CRUD operations
- [Model Atlas API Specification](Model%20Atlas%20API%20Specification.md) - Complete API spec
- [EObjectWorkflowService README](../org.eclipse.fennec.model.atlas.workflow/README.md) - Workflow service details
- [CLAUDE.md](../CLAUDE.md) - Project overview and build instructions

---

## License

Eclipse Public License 2.0 (EPL-2.0)

Copyright (c) 2012 - 2025 Data In Motion and others.
