# EObjectWorkflowService

## Overview

The **EObjectWorkflowService** is a configurable workflow management service for EMF EObjects that provides stage-based lifecycle management with hierarchical scope support. It enables organizations to manage model artifacts through customizable workflow stages (e.g., draft, review, approved, release) with multi-tenant isolation and inheritance capabilities.

## Key Features

- **Configurable Workflow Stages**: Define custom stages that match your organization's workflow (e.g., draft → review → approved → release)
- **Hierarchical Scopes**: Parent-child scope relationships with automatic inheritance from parent scopes
- **Multi-tenant Support**: Complete isolation between scopes with optional sharing via hierarchy
- **Stage Transitions**: Move objects between workflow stages with configurable policies
- **Storage Backend Integration**: Pluggable storage backends via OSGi service references
- **Registry Integration**: Lucene-based search and indexing for efficient object retrieval
- **Transactional Operations**: Promise-based async operations with rollback support

## Architecture

### Core Concepts

#### **Scopes**
Scopes provide multi-tenant isolation and can be organized hierarchically:
- Each workflow instance operates within a specific **scope** (e.g., "tenant-a", "project-x")
- Scopes can have a **parent scope** for inheritance
- Child scopes can access objects from parent scope's **final stage** (read-only)
- Root scopes have no parent (use empty string `""` for `parent.scope`)

Example hierarchy:
```
atlas (root)
  ├── organization-1
  │     ├── project-a
  │     └── project-b
  └── organization-2
        └── project-c
```

#### **Stages**
Stages represent the lifecycle states of objects in the workflow:
- Fully customizable stage names (e.g., "draft", "review", "approved", "release")
- Each stage maps to a storage backend identified by **scope + role**
- One stage is designated as the **final stage** (typically "release")
- Objects in the final stage are visible to child scopes

#### **Final Stage**
The final stage is a special stage that:
- Represents the "published" or "released" state
- Is visible to child scopes in hierarchical lookups
- Defaults to "release" but can be customized
- Must be one of the configured stages

### Component Integration

```
┌─────────────────────────────────────────────────┐
│        EObjectWorkflowServiceImpl               │
│  (Scope: tenant-a, Stages: [draft, release])   │
└────────────┬──────────────┬─────────────────────┘
             │              │
             │              │
   ┌─────────▼──────┐  ┌───▼──────────────────────┐
   │   EObject      │  │  EObjectStorageService   │
   │   Registry     │  │  (scope=tenant-a,        │
   │   Service      │  │   role=draft|release)    │
   └────────────────┘  └──────────────────────────┘
             │
             │
   ┌─────────▼──────────────────────┐
   │  Parent WorkflowService        │
   │  (Optional, for hierarchy)     │
   └────────────────────────────────┘
```

## Configuration

The service is configured via OSGi Configuration Admin using the `WorkflowServiceConfig` interface.

### Configuration Properties

#### Basic Configuration

| Property | Type | Required | Default | Description |
|----------|------|----------|---------|-------------|
| `workflow_id` | String | Yes | "default" | Unique identifier for this workflow instance |
| `scope` | String | Yes | - | Unique identifier for the scope this workflow handles |
| `description` | String | No | - | Human-readable description of the scope |

#### Scope Hierarchy

| Property | Type | Required | Default | Description |
|----------|------|----------|---------|-------------|
| `parent_scope` | String | No | "atlas" | Parent scope identifier (use `""` for root scope) |
| `parentWorkflowService_target` | String | No | "(scope=atlas)" | OSGi filter to bind parent workflow service |

#### Workflow Stages

| Property | Type | Required | Default | Description |
|----------|------|----------|---------|-------------|
| `stages` | String[] | No | ["draft", "approved", "release"] | Array of stage names supported by this workflow |
| `writable_stages` | String[] | No | ["draft", "approved"] | Array of writable stages supported by this workflow |
| `final_stage` | String | No | "release" | The stage considered as "final" (must be in `stages` array) |
| `delete_after_transition` | Boolean | No | false | Whether to delete objects from source stage after transition |

#### Transactional Settings

| Property | Type | Required | Default | Description |
|----------|------|----------|---------|-------------|
| `transaction_timeout_ms` | Long | No | 30000 | Timeout in milliseconds for transactional operations |
| `enable_auto_rollback` | Boolean | No | true | Enable automatic rollback on transactional failures |
| `max_concurrent_approvals` | Integer | No | 10 | Maximum concurrent approval operations |

#### Logging

| Property | Type | Required | Default | Description |
|----------|------|----------|---------|-------------|
| `enable_detailed_logging` | Boolean | No | false | Enable detailed operation logging for debugging |

### Configuration Example

```properties
# Basic workflow configuration
workflow_id=tenant-a-workflow
scope=tenant-a
description=Workflow for Tenant A

# Hierarchy
parent_scope=atlas
parentWorkflowService.target=(scope=atlas)

# Stages
stages=["draft", "review", "approved", "release"]
writable.stages=["draft", "review", "approved"]
final.stage=release

# Policies
delete.after.transition=true
enable_auto_rollback=true
```

### Storage Backend Configuration

Each stage requires a corresponding storage backend configured with matching `scope` and `role`:

```properties
# Draft storage for tenant-a
workspace.folder=/var/data/tenant-a/draft
storage.scope=tenant-a
storage.role=draft

# Release storage for tenant-a
workspace.folder=/var/data/tenant-a/release
storage.scope=tenant-a
storage.role=release
```

## API Reference

### Upload Operations

#### `uploadToStage(String stage, T object, ObjectMetadata metadata): Promise<ObjectMetadata>`

Uploads an EObject to a specified workflow stage.

**Parameters:**
- `stage` - Target stage name (must be in configured `stages`)
- `object` - The EObject to upload
- `metadata` - Object metadata (name, user, etc.)

**Returns:** Promise containing the object metadata

**Behavior:**

- Updates `lastChangeTime` to current timestamp
- Stores object in storage backend for the specified stage
- Returns the metadata for future reference

**Example:**
```java
EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
pkg.setName("MyPackage");
pkg.setNsURI("http://example.com/mypackage");

ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
metadata.setObjectName("MyPackage");
metadata.setUploadUser("john.doe");

metadata = workflow.uploadToStage("draft", pkg, metadata).getValue();
```

### Retrieval Operations

#### `getFromStage(String stage, String objectId): ObjectMetadata`

Retrieves object metadata from a specific stage with hierarchical lookup.

**Parameters:**
- `stage` - Stage to search in
- `objectId` - Object identifier

**Returns:** ObjectMetadata or null if not found

**Behavior:**
1. Searches in local scope's specified stage
2. If not found AND parent workflow exists, searches parent's **final stage** (hierarchical lookup)
3. Returns metadata if found, null otherwise

**Example:**
```java
ObjectMetadata metadata = workflow.getFromStage("draft", objectId);
if (metadata != null) {
    System.out.println("Found: " + metadata.getObjectName());
}
```

#### `getFromFinalStage(String objectId): ObjectMetadata`

Retrieves object metadata from the configured final stage with hierarchical lookup.

**Parameters:**
- `objectId` - Object identifier

**Returns:** ObjectMetadata or null if not found

**Behavior:**
- Convenience method that delegates to `getFromStage(final_stage, objectId)`
- Supports hierarchical lookup through parent scopes
- Useful for retrieving "published" or "released" objects

**Example:**
```java
// Get from final stage (typically "release")
ObjectMetadata released = workflow.getFromFinalStage(objectId);
```

#### `getContentFromStage(String stage, String objectId): T`

Retrieves the actual EObject content from a specific stage.

**Parameters:**
- `stage` - Stage to retrieve from
- `objectId` - Object identifier

**Returns:** The EObject or null if not found

**Example:**
```java
EPackage pkg = (EPackage) workflow.getContentFromStage("release", objectId);
if (pkg != null) {
    System.out.println("Package URI: " + pkg.getNsURI());
}
```

### List Operations

#### `listInStage(String stage): List<ObjectMetadata>`

Lists all objects in a specific stage.

**Parameters:**
- `stage` - Stage to list

**Returns:** List of ObjectMetadata (may be empty)

**Behavior:**
- If stage is the final stage, delegates to `listInFinalStage()`
- Uses registry service for efficient lookup (falls back to storage query)
- Returns only objects in the local scope for non-final stages

**Example:**
```java
List<ObjectMetadata> drafts = workflow.listInStage("draft");
drafts.forEach(m -> System.out.println(m.getObjectName()));
```

#### `listInFinalStage(): List<ObjectMetadata>`

Lists all objects in the final stage, including parent scope objects.

**Returns:** List of ObjectMetadata from local and parent final stages

**Behavior:**
1. Retrieves objects from local scope's final stage
2. If parent workflow exists, retrieves objects from parent's final stage (recursively)
3. Combines results from all levels in the hierarchy

**Example:**
```java
// Lists objects from this scope AND all parent scopes
List<ObjectMetadata> allReleased = workflow.listInFinalStage();
System.out.println("Total released objects: " + allReleased.size());
```

### Update Operations

#### `updateInStage(String stage, T updatedObject, String objectId): Promise<ObjectMetadata>`

Updates an existing object in a stage.

**Parameters:**
- `stage` - Stage containing the object
- `updatedObject` - The updated EObject
- `objectId` - Object identifier

**Returns:** Promise<ObjectMetadata> with the updated metadata

**Behavior:**
- Verifies object exists and is in DRAFT or REJECTED status
- Updates `lastChangeTime` timestamp
- Stores updated object in same stage
- Throws exception if object is not in updatable status

**Example:**
```java
EPackage pkg = (EPackage) workflow.getContentFromStage("draft", objectId);
pkg.setName("UpdatedName");
workflow.updateInStage("draft", pkg, objectId).getValue();
```

### Delete Operations

#### `deleteFromStage(String stage, String objectId): Promise<Boolean>`

Deletes an object from a stage.

**Parameters:**
- `stage` - Stage containing the object
- `objectId` - Object identifier

**Returns:** Promise<Boolean> (true if deleted)

**Behavior:**
- Verifies object is in DRAFT or REJECTED status
- Deletes from storage backend
- Removes from registry cache
- Throws exception if object is not in deletable status

**Example:**
```java
Boolean deleted = workflow.deleteFromStage("draft", objectId).getValue();
if (deleted) {
    System.out.println("Object deleted successfully");
}
```

### Transition Operations

#### `transitionToStage(String objectId, String fromStage, String toStage): ObjectMetadata`

Transitions an object from one stage to another.

**Parameters:**
- `objectId` - Object identifier
- `fromStage` - Source stage
- `toStage` - Target stage

**Returns:** Updated ObjectMetadata after transition

**Behavior:**
1. Validates transition is allowed (`isTransitionAllowed`)
2. Retrieves object and metadata from source stage
3. Updates metadata timestamps
4. Stores object in target stage
5. Optionally deletes from source stage (if `delete_after_transition` is true)

**Example:**
```java
// Move from draft to review
ObjectMetadata reviewed = workflow.transitionToStage(objectId, "draft", "review");

// Move from review to release
ObjectMetadata released = workflow.transitionToStage(objectId, "review", "release");
```

#### `isTransitionAllowed(String fromStage, String toStage): boolean`

Checks if a transition between stages is allowed.

**Parameters:**
- `fromStage` - Source stage
- `toStage` - Target stage

**Returns:** true if transition is allowed

**Current Behavior:**
- Validates both stages exist in configuration
- Always returns true (can be extended for custom transition rules)

**Example:**
```java
if (workflow.isTransitionAllowed("draft", "release")) {
    workflow.transitionToStage(objectId, "draft", "release");
}
```

## Usage Examples

### Example 1: Simple Workflow (Single Scope)

```java
// Configuration
// scope=my-project
// stages=[draft, release]
// final.stage=release

// Upload to draft
EPackage pkg = createPackage();
ObjectMetadata metadata = createMetadata();
workflow.uploadToStage("draft", pkg, metadata).getValue();
String id = metadata.getObjectId();

// Transition to release
workflow.transitionToStage(id, "draft", "release");

// Retrieve from final stage
ObjectMetadata released = workflow.getFromFinalStage(id);
```

### Example 2: Hierarchical Workflow

```java
// Parent workflow: scope=organization
// Child workflow: scope=project, parent_scope=organization

// Parent uploads a shared library
parentWorkflow
    .uploadToStage("release", sharedLib, metadata)
    .getValue();
String sharedLibId = metadata.getObjectId();

// Child can access parent's released objects
ObjectMetadata found = childWorkflow.getFromFinalStage(sharedLibId);
// found != null - child found parent's object!

// List all released objects (including parent's)
List<ObjectMetadata> all = childWorkflow.listInFinalStage();
// Contains both child's and parent's released objects
```

### Example 3: Custom Workflow Stages

```java
// Configuration
// stages=[draft, peer-review, qa-review, approved, production]
// final.stage=production

// Upload
workflow.uploadToStage("draft", object, metadata).getValue();
String id = metadata.getObjectId();

// Progress through stages
workflow.transitionToStage(id, "draft", "peer-review");
workflow.transitionToStage(id, "peer-review", "qa-review");
workflow.transitionToStage(id, "qa-review", "approved");
workflow.transitionToStage(id, "approved", "production");

// Now visible to child scopes
ObjectMetadata prod = workflow.getFromFinalStage(id);
```

### Example 4: Query and Update Pattern

```java
// List all drafts
List<ObjectMetadata> drafts = workflow.listInStage("draft");

// Update each draft
for (ObjectMetadata meta : drafts) {
    T object = workflow.getContentFromStage("draft", meta.getObjectId());

    // Modify object
    modifyObject(object);

    // Update in workflow
    workflow.updateInStage("draft", object, meta.getObjectId()).getValue();
}
```

## Hierarchical Scope Behavior

### Lookup Rules

When searching for an object:

1. **Local Lookup**: Always searches local scope first in the specified stage
2. **Parent Lookup**: If not found and parent exists, searches parent's **final stage only**
3. **Recursive**: Parent lookup is recursive (grandparent, great-grandparent, etc.)
4. **Read-Only**: Objects from parent scopes are read-only in child scopes

### Visibility Matrix

| Operation | Local Objects | Parent Final Stage | Parent Other Stages |
|-----------|---------------|-------------------|-------------------|
| `getFromStage(stage)` | ✓ | ✓ (if not found locally) | ✗ |
| `getFromFinalStage()` | ✓ | ✓ | ✗ |
| `listInStage(stage)` | ✓ | ✗ | ✗ |
| `listInFinalStage()` | ✓ | ✓ | ✗ |
| `updateInStage()` | ✓ | ✗ (read-only) | ✗ |
| `deleteFromStage()` | ✓ | ✗ (read-only) | ✗ |

### Example Scenario

```
Scope Hierarchy:
  global-libs (final.stage=release)
    └── Contains: commons-v1, utils-v2

  tenant-a (final.stage=release, parent=global-libs)
    └── Contains: app-module-v1

Query from tenant-a:
  getFromFinalStage("commons-v1") → Found (from parent)
  getFromFinalStage("app-module-v1") → Found (from local)
  listInFinalStage() → [app-module-v1, commons-v1, utils-v2]
```

## Stage Action Services

The workflow dispatches lifecycle callbacks to **`StageActionService`** implementations whenever objects enter, are updated in, or leave a stage. This is the extension point for reacting to workflow changes (registering services, sending notifications, invalidating caches, etc.) without modifying the core registry.

### Lifecycle Events

| Event | When it fires |
|-------|---------------|
| `ENTER` | After an object is written into a stage for the first time (initial upload or transition from another stage). |
| `UPDATE` | After an object's content is updated in place while remaining in the same stage. |
| `EXIT`  | After an object leaves a stage, either by deletion (`ExitReason.DELETED`) or by transitioning elsewhere (`ExitReason.TRANSITIONED`, with `targetStage` populated). By the time `EXIT` fires, the object is no longer retrievable from storage. |

All events are delivered post-commit: the storage mutation has already been applied. Callbacks return an OSGi `Promise<Void>` and must be idempotent, because the workflow may replay `ENTER` events at startup to reconcile runtime state that does not survive a restart.

### Implementation Contract

A `StageActionService` declares what it cares about via:

- `supportsObjectType(String)` — for example `"EPackage"`.
- `getTriggerStages()` — stage names it subscribes to (empty set means "all stages").
- `getTriggerEvents()` — subset of `ENTER` / `UPDATE` / `EXIT`.
- `requiresReplayOnStartup()` / `requiresReplayOnShutdown()` — ask the workflow to replay `ENTER` / synthetic `EXIT` events for objects currently in a trigger stage.

The registry filters dispatches by these declarations before calling `onEnter` / `onUpdate` / `onExit`, each receiving an `ActionContext` record with scope, registry, objectId, objectType, stage, `sourceStage` / `targetStage` (for transitions), `exitReason`, and a `replay` flag.

### Bundled Implementation: `EPackageStageActionService`

Ships as the default action for EMF schemas. When an `EPackage` object enters or is updated in a configured trigger stage it registers the following OSGi services backed by that EPackage:

- `EPackageConfigurator`
- `EPackage` (also as its concrete class)
- `EFactory`
- `Condition` (with `osgi.condition.id = <nsURI>`)

so the EPackage becomes immediately available to the EMF runtime and to any consumer filtering by `emf.model.scope` / `atlas.stage` / `emf.name`. On `EXIT` the same services are unregistered.

Configuration PID: `EPackageStageActionService` (factory or singleton). Typical properties:

| Property | Default | Description |
|----------|---------|-------------|
| `trigger.stages` | `["release"]` | Stages whose `ENTER` / `UPDATE` / `EXIT` events trigger (un)registration. In the default runtime this is set to *all configured stages* (e.g. `["draft", "approved", "release"]`) so schemas are available for use — and for validation — from the moment they are uploaded, not only once they reach `release`. |
| `replay.on.startup` | `true` | Replay `ENTER` for every EPackage currently in a trigger stage when the service starts. |
| `replay.on.shutdown` | `true` | Replay `EXIT` for every EPackage currently in a trigger stage when the service stops. |
| `storageService.target` | – | OSGi target filter for the `EObjectStorageService<EPackage>` used to fetch EPackage content during `ENTER` / `UPDATE`. |

An `UPDATE` always tears down the previous OSGi registrations before re-registering, so service consumers see the new EPackage content (even when the `nsURI` is unchanged).

## Integration Points

### Required Services

1. **EObjectRegistryService**: For object indexing and search
2. **EObjectStorageService**: For persistent storage (one per stage)
3. **StageActionService** (optional, multiple): Lifecycle hooks fired on `ENTER` / `UPDATE` / `EXIT` for configured stages. See [Stage Action Services](#stage-action-services).
4. **Parent WorkflowService** (optional): For hierarchical scopes

### OSGi Service Properties

When registered, the service includes:
- `scope` - The scope identifier
- Standard OSGi service properties

Filter for specific workflow:
```java
String filter = "(scope=my-scope)";
EObjectWorkflowService<?> workflow = getService(filter);
```

## Error Handling

### Common Exceptions

- **IllegalArgumentException**: Invalid stage, missing object, or null parameters
- **IllegalStateException**: Invalid object status for operation, invalid final stage configuration
- **RuntimeException**: Promise execution failures, storage backend errors

## Best Practices

1. **Stage Design**: Keep stage count reasonable (3-5 stages typical)
2. **Final Stage**: Choose a stage name that represents "published" state
3. **Scope Naming**: Use hierarchical naming (e.g., "org.project.subproject")
4. **Storage Backends**: Ensure storage backends are configured for all stages
5. **Error Handling**: Always handle Promise failures with proper error handling
6. **Object IDs**: Generate unique IDs (UUIDs recommended)
7. **Metadata**: Always populate scope, role, and timestamps in metadata

## Performance Considerations

- Registry lookups are faster than storage queries
- `listInFinalStage()` with deep hierarchies may query multiple scopes
- Use appropriate timeouts for Promise operations
- Enable detailed logging only for debugging (performance impact)

## Testing

See `ScopeAwareWorkflowServiceTest` for comprehensive test coverage including:
- Single scope operations
- Hierarchical scope lookups
- Stage transitions
- Final stage operations
- Custom stage configurations
- Three-level hierarchy scenarios

## License

Eclipse Public License 2.0 (EPL-2.0)
