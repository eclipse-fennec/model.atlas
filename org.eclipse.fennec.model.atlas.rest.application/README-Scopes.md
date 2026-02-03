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
┌──────────────────────────────┐
│      ScopesResource          │
│  (JAX-RS REST Endpoint)      │
└─────────────┬────────────────┘
              │
              │
   ┌──────────▼───────────────┐
   │  ScopeServiceCollector   │
   └──────────┬───────────────┘
              │
              │
   ┌──────────▼───────────────┐
   │      ScopeService        │
   │  (OSGi Dynamic Services) │
   └──────────────────────────┘
```

### Integration Points

#### **ScopeServiceCollector**
The `ScopeServiceCollector` dynamically tracks all registered `ScopeService` instances and constructs `Scope` objects from their OSGi configuration properties.

**Key Methods:**
- `getAllScopes()`: Returns list of all configured scopes
- `getScopeByName(String name)`: Retrieves specific scope metadata
- `getScopeServiceByScopeName(String name)`: Retrieves the ScopeService for a specific scope

#### **Scope Model**
Each scope contains the following metadata:

| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Unique identifier for the scope (e.g., "my-tenant", "global") |
| `description` | String | Human-readable description of the scope's purpose |
| `parentScope` | String | Name of parent scope (null/empty for root scopes) |
| `registries` | List<Registry> | List of registries available in this scope |

#### **Registry Model**
Each registry within a scope contains:

| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Unique identifier for the registry (e.g., "schema", "configurations") |
| `description` | String | Human-readable description of the registry's purpose |
| `stages` | List<Stage> | Workflow stages configured for this registry |
| `allowedTransitions` | List<StageTransition> | Valid transitions between stages |

#### **Stage Model**
Each stage within a registry contains:

| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Stage identifier (e.g., "draft", "approved", "release") |
| `writable` | boolean | Whether objects can be created/modified in this stage |
| `final` | boolean | Whether this is the final/released stage (used for hierarchical lookups) |

#### **StageTransition Model**
Each allowed transition between stages:

| Field | Type | Description |
|-------|------|-------------|
| `fromStage` | String | Source stage name |
| `toStage` | String | Target stage name |

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
      "description": "System-level scope containing read-only base schemas",
      "parentScope": null,
      "registries": [
        {
          "name": "schema",
          "description": "Schema registry",
          "stages": [
            { "name": "release", "writable": false, "final": true }
          ],
          "allowedTransitions": []
        }
      ]
    },
    {
      "name": "global-corporate",
      "description": "Corporate-wide shared schemas",
      "parentScope": "atlas",
      "registries": [
        {
          "name": "schema",
          "description": "Schema registry",
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
    },
    {
      "name": "my-tenant",
      "description": "Tenant-specific schema workspace",
      "parentScope": "global-corporate",
      "registries": [
        {
          "name": "schema",
          "description": "Schema registry",
          "stages": [
            { "name": "draft", "writable": true, "final": false },
            { "name": "review", "writable": true, "final": false },
            { "name": "approved", "writable": true, "final": false },
            { "name": "release", "writable": false, "final": true }
          ],
          "allowedTransitions": [
            { "fromStage": "draft", "toStage": "review" },
            { "fromStage": "review", "toStage": "approved" },
            { "fromStage": "approved", "toStage": "release" }
          ]
        },
        {
          "name": "configurations",
          "description": "Configuration objects registry",
          "stages": [
            { "name": "draft", "writable": true, "final": false },
            { "name": "release", "writable": false, "final": true }
          ],
          "allowedTransitions": [
            { "fromStage": "draft", "toStage": "release" }
          ]
        }
      ]
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
- **404 Not Found**: Scope not found (no scope with that name exists)
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
  "description": "Tenant-specific schema workspace for customer ABC",
  "parentScope": "global-corporate",
  "registries": [
    {
      "name": "schema",
      "description": "Schema registry for EMF models",
      "stages": [
        { "name": "draft", "writable": true, "final": false },
        { "name": "review", "writable": true, "final": false },
        { "name": "approved", "writable": true, "final": false },
        { "name": "release", "writable": false, "final": true }
      ],
      "allowedTransitions": [
        { "fromStage": "draft", "toStage": "review" },
        { "fromStage": "review", "toStage": "approved" },
        { "fromStage": "approved", "toStage": "release" }
      ]
    },
    {
      "name": "configurations",
      "description": "Configuration objects registry",
      "stages": [
        { "name": "draft", "writable": true, "final": false },
        { "name": "release", "writable": false, "final": true }
      ],
      "allowedTransitions": [
        { "fromStage": "draft", "toStage": "release" }
      ]
    }
  ]
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

Each **registry** within a scope defines its own workflow stages that control the lifecycle of objects within that registry. Different registries can have different workflow configurations.

**Common Stage Patterns**:

#### Simple (2-stage)
```json
{
  "name": "configurations",
  "stages": [
    { "name": "draft", "writable": true, "final": false },
    { "name": "release", "writable": false, "final": true }
  ],
  "allowedTransitions": [
    { "fromStage": "draft", "toStage": "release" }
  ]
}
```
- **draft**: Work in progress (writable)
- **release**: Published and available to child scopes (read-only, final)

#### Standard (3-stage)
```json
{
  "name": "schema",
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
```
- **draft**: Initial development (writable)
- **approved**: Reviewed and approved (writable)
- **release**: Published (read-only, final)

#### Complex (4+ stages)
```json
{
  "name": "enterprise-models",
  "stages": [
    { "name": "draft", "writable": true, "final": false },
    { "name": "review", "writable": true, "final": false },
    { "name": "approved", "writable": true, "final": false },
    { "name": "staging", "writable": true, "final": false },
    { "name": "production", "writable": false, "final": true }
  ],
  "allowedTransitions": [
    { "fromStage": "draft", "toStage": "review" },
    { "fromStage": "review", "toStage": "approved" },
    { "fromStage": "approved", "toStage": "staging" },
    { "fromStage": "staging", "toStage": "production" }
  ]
}
```

### Stage Properties

Each **Stage** has the following properties:

**`name`**: Stage identifier
- Defines the stage name used in API paths
- Must be unique within the registry

**`writable`**: Whether objects can be modified in this stage
- `true`: Allows PUT/DELETE/POST operations
- `false`: Read-only stage (no modifications allowed)

**`final`**: Whether this is the final/released stage
- `true`: Used for hierarchical lookups (children see parent's final stage)
- `false`: Regular workflow stage
- Typically only one stage should be marked as final

### Allowed Transitions

Each **StageTransition** defines a valid transition path:

**`fromStage`**: Source stage name
**`toStage`**: Target stage name

Transitions are explicitly defined - objects can only move between stages if a matching transition exists.

---

## Scope Configuration

### OSGi Configuration

Scopes are defined via OSGi Config Admin factory configurations:

```properties
# File: ScopeService~my-tenant.cfg

# Scope Identity
scope=my-tenant
description=Tenant-specific schema workspace
parent.scope=global-corporate

# Policies
delete.after.transition=true
```

**Required Properties**:
- `scope`: Unique scope identifier

**Optional Properties**:
- `description`: Human-readable description
- `parent.scope`: Parent scope name (empty for root)
- `delete.after.transition`: Delete from source after transition

**Note**: Registry configurations (including stages and transitions) are defined separately in the workflow configuration files.

### Storage Backend Configuration

Storage backends are configured once per storage type, and registries map stages to storage types. Multiple stages can share the same storage backend.

**Storage Configuration** (storage.json):
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

**Registry Configuration** (workflow.json):
```json
{
  "RegistryService~schema": {
    "registry.name": "schema",
    "registry.description": "The schema registry to store EPackage objects",
    "stage.storage.mappings": [
      "draft:apicurio",
      "approved:apicurio",
      "release:apicurio"
    ],
    "workflow.transitions": [
      "draft:approved",
      "approved:release"
    ],
    "stages": [
      { "name": "draft", "writable": true, "final": false },
      { "name": "approved", "writable": true, "final": false },
      { "name": "release", "writable": false, "final": true }
    ],
    "storageService.target": "(storage.type=apicurio)"
  }
}
```

**Key Configuration Properties**:
- `storage.type`: Identifies the storage backend type
- `stage.storage.mappings`: Maps each stage to a storage type (format: `"stage:storageType"`)
- `storageService.target`: OSGi filter to select the storage service
- `workflow.transitions`: Defines allowed stage transitions (format: `"fromStage:toStage"`)

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
  "registries": [
    {
      "name": "schema",
      "stages": [
        { "name": "release", "writable": false, "final": true }  ← Final stage visible to children
      ],
      ...
    }
  ]
}

# Get parent metadata
curl -X GET https://api.example.com/scopes/global-corporate

# Response shows:
{
  "name": "global-corporate",
  "parentScope": "atlas",  ← Grandparent scope
  "registries": [...]  ← Each registry has its own final stage
}
```

**Use Case**: Building a hierarchical browser that shows which schemas are inherited from parent scopes.

---

### 3. Validating Stage Names

**Scenario**: A client wants to create or transition a schema and needs to know which stages are available.

```bash
# Get scope configuration
curl -X GET https://api.example.com/scopes/my-tenant

# Response shows available stages per registry:
{
  "registries": [
    {
      "name": "schema",
      "stages": [
        { "name": "draft", "writable": true, "final": false },
        { "name": "review", "writable": true, "final": false },
        { "name": "approved", "writable": true, "final": false },
        { "name": "release", "writable": false, "final": true }
      ],
      "allowedTransitions": [
        { "fromStage": "draft", "toStage": "review" },
        { "fromStage": "review", "toStage": "approved" },
        { "fromStage": "approved", "toStage": "release" }
      ]
    }
  ]
}

# Now client knows for the "schema" registry:
# - Can create in: draft, review, approved (writable: true)
# - Can transition: draft → review → approved → release
# - Cannot modify: release (writable: false)
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
| 404 Not Found | Not found | Scope with given name doesn't exist |
| 500 Internal Server Error | Server error | Unexpected errors, exceptions |

### Common Error Scenarios

#### **Scope Not Found**

```bash
GET /scopes/non-existent-scope

→ 404 Not Found
```

**Reason**: No `ScopeService` registered with `scope=non-existent-scope`

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

**Reason**: No `ScopeService` instances registered

**Solution**: Verify scope services are started and configured

---

## Integration Examples

### JavaScript/TypeScript Client

```typescript
interface StageTransition {
  fromStage: string;
  toStage: string;
}

interface Stage {
  name: string;
  writable: boolean;
  final: boolean;
}

interface Registry {
  name: string;
  description: string;
  stages: Stage[];
  allowedTransitions: StageTransition[];
}

interface Scope {
  name: string;
  description: string;
  parentScope: string | null;
  registries: Registry[];
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

  if (response.status === 404) {
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

// Get final stage for a specific registry
function getFinalStage(scope: Scope, registryName: string): Stage | undefined {
  const registry = scope.registries.find(r => r.name === registryName);
  return registry?.stages.find(s => s.final);
}

// Check if a transition is allowed
function isTransitionAllowed(registry: Registry, fromStage: string, toStage: string): boolean {
  return registry.allowedTransitions.some(
    t => t.fromStage === fromStage && t.toStage === toStage
  );
}
```

### Python Client

```python
import requests
from typing import List, Dict, Optional
from dataclasses import dataclass

@dataclass
class StageTransition:
    from_stage: str
    to_stage: str

    @classmethod
    def from_dict(cls, data: dict) -> 'StageTransition':
        return cls(data['fromStage'], data['toStage'])

@dataclass
class Stage:
    name: str
    writable: bool
    final: bool

    @classmethod
    def from_dict(cls, data: dict) -> 'Stage':
        return cls(data['name'], data['writable'], data['final'])

@dataclass
class Registry:
    name: str
    description: str
    stages: List[Stage]
    allowed_transitions: List[StageTransition]

    @classmethod
    def from_dict(cls, data: dict) -> 'Registry':
        return cls(
            name=data['name'],
            description=data.get('description', ''),
            stages=[Stage.from_dict(s) for s in data.get('stages', [])],
            allowed_transitions=[StageTransition.from_dict(t) for t in data.get('allowedTransitions', [])]
        )

    def get_final_stage(self) -> Optional[Stage]:
        return next((s for s in self.stages if s.final), None)

    def is_transition_allowed(self, from_stage: str, to_stage: str) -> bool:
        return any(t.from_stage == from_stage and t.to_stage == to_stage
                   for t in self.allowed_transitions)

class Scope:
    def __init__(self, data: dict):
        self.name = data['name']
        self.description = data.get('description', '')
        self.parent_scope = data.get('parentScope')
        self.registries = [Registry.from_dict(r) for r in data.get('registries', [])]

    def get_registry(self, name: str) -> Optional[Registry]:
        return next((r for r in self.registries if r.name == name), None)

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

        if response.status_code == 404:
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
    print(f"{scope.name}: {len(scope.registries)} registries")
    for registry in scope.registries:
        final = registry.get_final_stage()
        print(f"  - {registry.name}: {len(registry.stages)} stages, final={final.name if final else 'N/A'}")

# Get specific scope and registry info
my_scope = client.get_scope("my-tenant")
if my_scope:
    schema_registry = my_scope.get_registry("schema")
    if schema_registry:
        writable = [s.name for s in schema_registry.stages if s.writable]
        print(f"Writable stages: {', '.join(writable)}")

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

Before attempting schema operations, validate stage names against scope and registry configuration:

```typescript
async function canCreateInStage(
  scopeName: string,
  registryName: string,
  stageName: string
): Promise<boolean> {
  const scope = await getScope(scopeName);
  if (!scope) return false;

  const registry = scope.registries.find(r => r.name === registryName);
  if (!registry) return false;

  const stage = registry.stages.find(s => s.name === stageName);
  return stage?.writable ?? false;
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

The API returns 404 Not Found when a scope doesn't exist:

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

**Cause**: No `ScopeService` instances registered

**Solution**:
1. Check OSGi Config Admin for scope service configurations
2. Verify services are started: `scr:list | grep ScopeService`
3. Check for configuration errors in logs
4. Ensure `scope` property is set in each configuration

---

### Scope not found (404)

**Cause**: No scope service with matching `scope` property

**Solution**:
1. List all scopes to see available names
2. Check for typos in scope name (case-sensitive)
3. Verify scope service configuration exists
4. Check `ScopeServiceCollector` has bound the service

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
- [ScopeService README](../org.eclipse.fennec.model.atlas.workflow/README.md) - Scope service details
- [CLAUDE.md](../CLAUDE.md) - Project overview and build instructions

---

## License

Eclipse Public License 2.0 (EPL-2.0)

Copyright (c) 2012 - 2026 Data In Motion and others.
