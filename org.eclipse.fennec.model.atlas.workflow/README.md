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

#### `updateDiagnostics(String scope, String stage, String objectId, String producer, List<Diagnostic> diagnostics): Promise<ObjectMetadata>`

Replaces the diagnostics one producer holds on an object, without touching the object's content (issue #292).

**Parameters:**
- `scope`, `stage`, `objectId` - The object; the stage may be final or non-writable
- `producer` - The action, gate or module that owns the findings, e.g. `QvtTransitionGate`
- `diagnostics` - The producer's new roots; an empty list clears them. Ids left unset are minted by the stable id rule (producer, code, target); children inherit the producer

**Returns:** Promise<ObjectMetadata> with the stored metadata, or `null` when the object is not in that stage

**Behavior:**
- Validates the stage exists — deliberately **not** that it is writable or non-final: a finding about a released object is recorded on the released object. The content bar of `updateInStage` is untouched
- Removes the producer's current roots and adds the given ones; roots of other producers stay
- Changes neither `contentHash` nor `version` nor `lastChangeTime`, and fires **no** stage action (an action reacting to diagnostics would loop with the action that wrote them)
- Pushes the result into the shared registry cache, so listings show it at once
- A root that names another producer is refused with `IllegalArgumentException`

**Example:**
```java
Diagnostic finding = ManagementFactory.eINSTANCE.createDiagnostic();
finding.setCode("unresolved-reference");
finding.setTarget("//Person/address");
finding.setSeverity(DiagnosticSeverity.WARNING);
finding.setMessage("http://example.org/b is not visible in release");
workflow.updateDiagnostics("jena", "release", objectId, "reference-check", List.of(finding)).getValue();
```

A root that comes back with the **same id** as one the producer held before is the same finding seen again (issue #293): it keeps its `createdTime`, `status`, `history` and `version`, and takes only the producer's fresh view (severity, message, target, children). A changed severity is recorded in the history in the producer's name. Only a replacement at a *higher* version than its predecessor, an informed change such as the `DiagnosticService` makes, is taken as it is. So a status a person set survives every re-validation.

#### `DiagnosticService` — deciding about one finding

The `DiagnosticService` (component `DiagnosticService`, whiteboard service in this bundle) changes single diagnostics by id: `add`, `update`, `acknowledge`, `resolve`, `escalate`, `remove`. Every call names `changedBy` and the `expectedVersion` it decided about; a stale version is refused with `DiagnosticVersionConflictException` and writes nothing, an unknown object or id with `DiagnosticNotFoundException`. Applied changes append a history entry, bump the version and are written through `updateDiagnostics`, so they take the same path and fire the same event as a producer's write. Operations on one object are serialised per address within the runtime.

```java
DiagnosticAddress at = new DiagnosticAddress("jena", "schema", "release", objectId);
Diagnostic resolved = diagnosticService.resolve(at, findingId, 0, "gdpr.officer", "pseudonymised downstream").getValue();
```

#### `DiagnosticsChanged` events

Every `updateDiagnostics` that changes what a reader would see delivers one `DiagnosticsChanged` event on the OSGi **Typed Event Bus** (topic `DiagnosticsChanged.TOPIC`), for the (scope, registry, stage, objectId) it happened on, with a `DiagnosticDelta` per diagnostic that looks different (`ADDED`, `CHANGED`, `REMOVED`) carrying the `DiagnosticState` before and after. A no-op write delivers nothing, so modules reacting to each other converge instead of looping. Subscribe as `TypedEventHandler<DiagnosticsChanged>` with `event.topics=org/eclipse/fennec/model/atlas/diagnostics/DIAGNOSTICS_CHANGED`. The bus is an optional reference of the registry: a runtime without it still writes diagnostics.

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
3. Refuses if the target stage holds a *different* object under the id (`StageOccupiedException`, unless `overwrite`)
4. Asks every configured [stage gate](#stage-gates); a refusal aborts with `StageGateRefusedException` before anything is written
5. Updates metadata timestamps
6. Optionally deletes from source stage (if `delete_after_transition` is true) and fires `EXIT`
7. Stores object in target stage and fires `ENTER`

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

### Order, chains and results (issue #296)

For one event the registry runs its actions **one after the other**, each joined before the next starts, and the operation's promise resolves only after the last. The order is:

- **by default** `service.ranking`, highest first, ties broken by `service.id` (registration order), and a failing action does not stop the others - it is logged and recorded, see below;
- **with a chain** as the registry configures it in `stage.action.chains`, one JSON object per entry:

  ```json
  {"stage": "draft", "objectType": "http://…#//SourceUnit", "actions": ["QvtCompile", "QvtValidate"], "onFailure": "stop"}
  ```

  | Key | Meaning |
  |-----|---------|
  | `stage`, `objectType` | Optional. Narrow the chain to the events it is for; the **first** entry that matches an event applies, so put the specific ones first. |
  | `actions` | The actions that have a place in the order, by name: the `stage.action.name` service property, else the DS `component.name`, else the simple class name. They run first, in this order; every other bound action follows in ranking order. A name nobody is bound under is simply not there - a registry that must not run without an action says so with `stageActionService.cardinality.minimum`. |
  | `onFailure` | `continue` (default): the actions after a failing one still run. `stop`: they do not run for this event. |

The chain is configuration, not a new kind of service: the actions know nothing of each other, and the same action may sit in different places in different registries. The startup and shutdown replays run per action in ranking order; a replay reconciles one action's own state, so chains and their failure rule do not apply there.

**Results are recorded on the object.** After each action the workflow writes a diagnostic under the producer `stage-action/<name>` on the object in the event's stage: `stage-action.failed` (`ERROR`, the failure's message, `target` the event) when the action's promise failed, `stage-action.skipped` (`WARNING`) for every action a stopping chain did not run, and nothing when it succeeded - which clears the record the same action left on an earlier event. The metadata the operation returns carries the records as well. Nothing is recorded after a delete, the object is gone, nor after the `EXIT` of a transition whose source copy is deleted with it. An action that wants to say more than "failed" writes its own diagnostics under its own producer, through `RegistryService.updateDiagnostics`; the two never collide.

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

### Bundled Implementation: `SchemaDependencyStageAction`

Keeps the *unresolved* state of a schema's dependents current (issue #250). It listens on the schema registry next to the `EPackageStageActionService` (`stageActionService.target=(|(component.name=EPackageStageActionService)(component.name=SchemaDependencyStageAction))`, no configuration of its own):

- `EXIT` of a package from a stage - a delete, forced or not, or a transition that removes the source copy - writes a `schema.dependency-missing` diagnostic (severity `ERROR`, category `dependencies`, target the package's nsURI) under the producer `SchemaDependencies` to every object that referenced the package in the view that stage served: other schemas in the stage, instances, compiled transformation units. A forced delete the `SchemaDeleteGate` let through has recorded the same already; the write is idempotent.
- `ENTER` of a package into a stage removes that diagnostic again from the dependents in the views the stage now serves - a dependency arriving later, or again, heals them. This runs on the startup replay too, so a restart catches up; shutdown replays are ignored.

Which objects count, and which stages see a package served from a given stage, is the [dependents query](#the-dependents-query-schemadependents) below.

## Stage Gates

A `StageActionService` reacts to a mutation that has already happened and cannot stop it. A **`StageGate`** (same `action.api` bundle, issue #248) is the other half of that contract: the registry asks every gate **before** a transition or a delete commits, and a refusal aborts the operation before any store is touched. The caller gets the gates' reasons as a `StageGateRefusedException`, which the REST layer answers with `409 Conflict` whose body is the refused object's metadata from its source stage, diagnostics included (issue #295, `GateRefusals` in the endpoints, `StageGateRefusedExceptionMapper` as the fallback); post-commit action failures stay non-fatal as before.

### Contract

- `supportsObjectType(String)` — which object types the gate wants to be asked about.
- `beforeTransition(GateContext)` — returns a `Promise<GateVerdict>`. The `GateContext` carries the `trigger` (`TRANSITION`), scope, registry, objectId, objectType, `sourceStage`, `targetStage` and the object's fingerprint; the object is still readable in its source stage, the target has not been written.
- `beforeDelete(GateContext)` (issue #294) — the same for a delete: `trigger` is `DELETE`, `targetStage` is `null`, nothing has been removed yet. A `default` that passes, so a gate written against 1.1 keeps its behaviour.
- `producer()` — the name the gate's diagnostics are recorded under; defaults to the class name. Override it when the class may move.

A verdict is `GateVerdict.pass()`, `GateVerdict.pass(diagnostics)`, `GateVerdict.refuse(reason)` or `GateVerdict.refuse(reason, diagnostics)`. A **`GateDiagnostic`** is the EMF-free shape of a finding: severity, a gate-defined `code`, message, optional `category` and `target` (the element inside the object), children, and optionally the `Dependent` (registry, stage, objectId) it is about. A refusal always has at least one diagnostic; `refuse(reason)` makes one, coded `refused`, from the reason.

What the registry does with a round of verdicts:

- **All** gates that support the type are asked, not only up to the first refusal, so the caller sees everything in the way at once.
- **Refused**: every consulted gate's findings about the object are written to its source-stage copy under the gate's `producer()` — a refusing gate's veto, and a passing gate's (possibly empty) findings, which clears a veto that gate recorded on an earlier attempt. Then a `StageGateRefusedException` is raised carrying `trigger`, scope, registry, stage, objectId and the refusing gates' diagnostics. Nothing else changes.
- **Passed transition**: each gate's findings replace what it recorded on the metadata before, so warnings travel with the object into the target stage and stale vetoes are gone; when the source copy stays (`delete_after_transition` off) it is brought up to date too.
- **Consequences**: findings about a `Dependent` are written to that object before the operation commits — through this registry, or through the `RegistryServiceCollector` for another registry. An unreachable dependent stops the operation like an undecided gate; a dependent that no longer exists is only logged.
- **Forced delete** (`deleteFromStage(scope, stage, objectId, true)`, REST `?force=true`): a refusal no longer stops the delete. It is logged, the object goes, and the consequences are recorded on the dependents. The veto about the object itself is not written anywhere — the object is gone.

A gate whose promise **fails** does not let the operation through: the registry treats an undecided gate as a fault of the operation (`IllegalStateException`, a `500` over REST). A check that silently passes when it breaks is no check.

### Wiring

Gates are wired like stage actions, per registry, through the `stageGate` reference of the `RegistryService` configuration:

| Property | Description |
|----------|-------------|
| `stageGate.target` | OSGi target filter selecting the gates for this registry, e.g. `(component.name=QvtTransitionGate)`. Without it a registry has no gates and transitions behave as before. |
| `stageGate.cardinality.minimum` | Set to `1` when the registry must not run without its gate; the registry then waits for the gate to appear. |

### Bundled Implementation: `QvtTransitionGate`

Ships in the `qvt` bundle. For a QVT-O source it compiles the source against the **target** stage's view (its unit store for imports, its chain ResourceSet for model types) and refuses the transition when the compile fails, typically because an imported library has not been promoted yet. The reason lists the compiler's findings and names the remedy; the verdict carries them as one `qvto.does-not-compile` diagnostic (category `compile`, target the qualified name) with one `qvto.compiler-finding` child per compiler message at `line:column`, recorded under the producer `QvtTransitionGate`. The runtime configurations wire it into the `transformations` registry.

### Bundled Implementation: `SchemaDeleteGate`

Ships in this bundle (issue #250). For a package in a **schema registry** it refuses the delete while objects in the view that stage serves still depend on it: the verdict carries one `schema.has-dependents` finding (category `dependencies`, target the nsURI) with a `schema.dependent` child per dependent (target `registry/stage/objectId`), and one `schema.dependency-missing` consequence per dependent. A refusal records the finding on the package and answers `409 Conflict` with the package's metadata; `force` deletes anyway and the consequences land on the dependents, which is the same state the `SchemaDependencyStageAction` maintains from then on. Transitions pass. The runtime configurations wire it into the `schema` registry with `stageGate.target=(component.name=SchemaDeleteGate)`.

### The dependents query: `SchemaDependents`

`org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependents` answers *what breaks if this package leaves this stage*: `dependentsOf(scope, stage, nsURI)` lists every `SchemaDependent` (kind, registry, stage, objectId, objectType) that references the package as served from that stage. References are keyed by nsURI, and the *view* a stage serves follows the chain the registry chain configurator wires:

- the stage itself, for all three edge types;
- every earlier stage of the schema registry's chain up to the first one that holds a copy of the package itself (`draft` resolves through `approved` through `release`);
- for the final stage, the stages only other registries have (they are wired to the final schema stage).

Other schemas count in the stage itself only, because a registered package resolves its cross-package references against the packages registered for its own stage. Child scopes are not walked.

Three edge types are known: another schema whose `eSuperTypes` or `eType`s point into the package (read off the registered `EPackage` services), an instance whose object type is a class of the package (read off the registries' listings), and a compiled transformation unit whose manifest lists the package (contributed by the `qvt` bundle). A bundle that knows a further kind of reference registers a `SchemaDependentsContributor` service; the query asks it per registry and stage of the view.

## Integration Points

### Required Services

1. **EObjectRegistryService**: For object indexing and search
2. **EObjectStorageService**: For persistent storage (one per stage)
3. **StageActionService** (optional, multiple): Lifecycle hooks fired on `ENTER` / `UPDATE` / `EXIT` for configured stages. See [Stage Action Services](#stage-action-services).
4. **StageGate** (optional, multiple): Veto points asked before a transition commits. See [Stage Gates](#stage-gates).
5. **Parent WorkflowService** (optional): For hierarchical scopes

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
