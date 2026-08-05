# Refactoring Plan: Split EObjectWorkflowService into ScopeService, RegistryService, and StageService

## Overview

Refactor the current `EObjectWorkflowService` to separate concerns into three distinct service layers:

1. **StageService** - Represents a single workflow stage configuration (e.g., "draft", "approved", "release") with properties like name, writable, and final flag - pure configuration, no storage knowledge
2. **RegistryService** - Validates stages, defines workflow transitions, and handles storage operations with **per-stage storage routing** (scope-agnostic, can serve multiple tenants, routes each stage to its configured storage backend)
3. **ScopeService** - Orchestrates operations by validating and delegating to RegistryService (represents a tenant, no direct storage access)

### Key Innovation: Per-Stage Storage Configuration

The major architectural improvement in this refactoring is **per-stage storage configuration at the RegistryService level**:

- **Problem**: Different lifecycle stages have different storage requirements (drafts need flexibility, releases need immutability)
- **Solution**: Configure different storage backends for each stage within a registry
- **Example**: Schema registry can use:
  - `draft` → MongoDB (fast, flexible document storage for work-in-progress)
  - `approved` → MinIO (object storage for review artifacts)
  - `release` → Apicurio/Maven Central (immutable, versioned artifact repository)

**Configuration**:
```properties
registry.name=schema
stage.storage.mappings=draft:mongodb,approved:minio,release:apicurio
workflow.transitions=draft:approved,approved:release
```

**Benefits**:
- Optimal storage per lifecycle phase (cost, performance, durability)
- Seamless cross-storage transitions (RegistryService handles copying between backends)
- Different registries can have completely different stage→storage mappings
- StageService remains pure configuration and highly reusable

## Key Architecture Principles

**Service Composition via OSGi Target Filters**:
- Each **StageService** is a separate OSGi service instance (one per stage name)
  - Provides configuration: stage name, writable flag, final flag
  - Pure configuration - no storage knowledge
- Each **RegistryService** is **scope-agnostic** and injects a `List<StageService>` using a target filter from config
  - Example: Schema registry injects stages: `(|(stage.name=draft)(stage.name=approved)(stage.name=release))`
  - Provides validation: `isValidStage()`, `isWritableStage()`, `isTransitionAllowed()`
  - **Handles all CRUD operations with scope parameter**: upload, retrieve, update, delete, list, transition
  - **Manages per-stage storage backends**: Each stage within a registry can use different storage (draft→MongoDB, approved→MinIO, release→Maven)
  - Injects multiple storage services and routes operations to the appropriate backend based on stage
- Each **ScopeService** injects a `List<RegistryService>` by registry name using a target filter from config
  - Example: Atlas scope injects registries: `(|(registry.name=schema)(registry.name=configuration))`
  - **Orchestrates operations**: validates registry, delegates to RegistryService
  - **NO direct storage access**: all storage operations delegated to RegistryService
- This allows:
  - **Multi-tenancy**: Multiple scopes can share the same RegistryService instance
  - **Storage per stage**: Different stages can use different storage backends (draft→MongoDB, release→Maven)
  - **Clear orchestration chain**: Scope validates registry → Registry validates stage and routes to appropriate storage
  - Different registries to use different stage configurations
  - Stages to be reused across multiple registries
  - Runtime reconfiguration via ConfigAdmin

**Example Multi-Tenant Scenario with Per-Stage Storage Backends**:
- ONE RegistryService for "schema" (validates stages, routes to different storage per stage)
  - Configured with `stage.storage.mappings=draft:mongodb,approved:minio,release:apicurio`
  - Injects THREE storage services: MongoDB, MinIO, and Apicurio
  - Routes each stage to its configured storage:
    - `uploadToStage(..., "draft", ...)` → calls `mongoStorage.save("tenant1", "schema", "draft", ...)`
    - `uploadToStage(..., "approved", ...)` → calls `minioStorage.save("tenant1", "schema", "approved", ...)`
    - `uploadToStage(..., "release", ...)` → calls `apicurioStorage.save("tenant1", "schema", "release", ...)` → constructs groupId "tenant1-schema-release"
  - Benefits: Fast, flexible MongoDB for drafts; object storage for review; immutable Maven-style repository for releases
- ONE RegistryService for "configuration" (different stage→storage mapping)
  - Configured with `stage.storage.mappings=development:file,testing:minio,production:s3`
  - Injects THREE storage services: File, MinIO, and S3
  - Routes each stage to its configured storage
- TWO ScopeServices: "atlas" (parent) and "tenant1" (child) - both orchestrate operations
- Both scopes inject and use the SAME RegistryService instances
- The RegistryServices handle storage for both scopes, using the scope parameter to isolate tenant data
- Storage backend is determined per-stage within each registry, enabling optimal storage per lifecycle phase

## Current State Analysis

The current `EObjectWorkflowServiceImpl` has mixed responsibilities:

- **Scope concerns**: Parent scope lookup, hierarchical delegation, scope configuration
- **Registry concerns**: Registry validation, registry-specific storage operations, registry caching
- **Stage concerns**: Stage validation, transition validation, writable stage checks, final stage operations

All methods take both `stage` and `registry` parameters, creating tight coupling between these concepts.

## Proposed Architecture

### Layer 1: StageService (Lowest Level - Stage Configuration)

**Purpose**: Represents a single stage in a workflow with its configuration and validation logic.

**Responsibilities**:
- Stage name identification
- Writability status
- Stage type/role (e.g., final stage flag)

**Service Type**: OSGi Component with factory configuration (one instance per stage)

**Key Methods**:
```java
public interface StageService {
    String getStageName();
    boolean isWritable();
    boolean isFinalStage();
}
```

**Service Properties**:
- `stage.name` - The name of this stage (e.g., "draft", "approved", "release")
- `stage.writable` - Boolean indicating if this stage allows modifications
- `stage.final` - Boolean indicating if this is the final stage

**OSGi Registration**:
- Each stage is a separate OSGi service instance
- Registered with properties that allow filtering via target
- Example: A workflow with "draft", "approved", "release" creates 3 StageService instances

**Note**: RegistryService will inject a collection of StageService instances using target filters.

---

### Layer 2: RegistryService (Middle Level - Stage Validation & Storage Operations)

**Purpose**: Manages stage validation and storage operations for a specific registry type (scope-agnostic, can serve multiple scopes).

**Responsibilities**:
- Stage validation using injected StageService collection
- Stage transition validation (validates against configured workflow transitions)
- Stage metadata queries (final stage, writable stages, etc.)
- **Workflow definition**: Defines allowed transitions between stages for this registry
- **CRUD operations with scope parameter**: upload, retrieve, update, delete, list, transition
- **Per-stage storage routing**: Routes each stage's operations to its configured storage backend
- **Cross-storage transitions**: Handles copying/moving objects between different storage backends during transitions
- **Scope-agnostic**: Same RegistryService instance can serve multiple scopes/tenants

**Service Type**: OSGi Component with factory configuration (one instance per registry type, shared across scopes)

**Key Methods**:
```java
public interface RegistryService<T extends EObject> {
    // Stage Validation (uses injected StageService collection)
    boolean isValidStage(String stageName);
    boolean isWritableStage(String stageName);
    boolean isTransitionAllowed(String fromStage, String toStage);

    // Stage Metadata Queries
    StageService getStage(String stageName);
    StageService getFinalStage();
    List<StageService> getAllStages();
    List<String> getStageNames();

    // CRUD Operations (scope-aware via parameter)
    Promise<ObjectMetadata> uploadToStage(String scope, String stage, T object, ObjectMetadata metadata);
    ObjectMetadata getMetadataFromStage(String scope, String stage, String objectId);
    T getContentFromStage(String scope, String stage, String objectId);
    ObjectMetadata getFromFinalStage(String scope, String objectId);
    T getContentFromFinalStage(String scope, String objectId);
    Promise<ObjectMetadata> updateInStage(String scope, String stage, T updatedObject, String objectId, String version);
    Promise<Boolean> deleteFromStage(String scope, String stage, String objectId);
    List<ObjectMetadata> listInStage(String scope, String stage);
    List<ObjectMetadata> listInStageByName(String scope, String stage, String name);
    List<ObjectMetadata> listInFinalStage(String scope);
    ObjectMetadata transitionToStage(String scope, String objectId, String fromStage, String toStage);

    // Configuration
    String getRegistryName();
}
```

**Note**: RegistryService handles both validation AND storage operations. This allows each stage within a registry to use different storage backends.

**Service Properties**:
- `registry.name` - Registry identifier (schema, configuration, etc.)
- `service.ranking` - For prioritization if needed (optional)

**Configuration Properties**:
- `registry.name` - Registry identifier (must be unique)
- `stage.storage.mappings` - Comma-separated list of stage→storage mappings
  - Example: `draft:mongodb,approved:minio,release:apicurio`
  - Format: `stageName:storageType,stageName:storageType,...`
  - Enables different storage backends per stage (draft in MongoDB, release in Maven Central)
- `workflow.transitions` - Comma-separated list of allowed stage transitions (fromStage:toStage pairs)
  - Example: `draft:approved,approved:release` (defines the workflow path)
  - Allows non-linear workflows (e.g., `draft:approved,draft:release,approved:release` for skipping stages)
- `stageService.target` - OSGi target filter to select StageService instances
  - Example: `(|(stage.name=draft)(stage.name=approved)(stage.name=release))`
  - This filter is automatically applied to the `@Reference List<StageService>` parameter in the constructor

**Configuration Interface**:
```java
@ObjectClassDefinition(name = "Registry Service Configuration")
public @interface RegistryServiceConfig {
    @AttributeDefinition(name = "Registry Name")
    String registry_name();

    @AttributeDefinition(name = "Stage Storage Mappings",
        description = "Comma-separated stage→storage mappings (e.g., draft:mongodb,approved:minio,release:apicurio)")
    String[] stage_storage_mappings();

    @AttributeDefinition(name = "Workflow Transitions",
        description = "Comma-separated list of allowed transitions (fromStage:toStage)")
    String workflow_transitions();

    @AttributeDefinition(name = "Stage Service Target Filter")
    String stageService_target() default "(stage.name=*)";
}
```

**Example Configurations:**
- Schema registry: `stage.storage.mappings=draft:mongodb,approved:minio,release:apicurio`
- Configuration registry: `stage.storage.mappings=development:file,testing:minio,production:s3`
- Model registry: `stage.storage.mappings=draft:file,release:file` (all stages use same storage)

**Note**: No `registry.scope` property - the RegistryService is scope-agnostic and can serve multiple scopes!

**Dependencies** (all injected via constructor):
- `List<StageService>` (injected via @Reference with target filter from config) - for stage validation
- `List<EObjectStorageService<T>>` (injected via @Reference, MULTIPLE storage services) - for per-stage storage routing
  - RegistryService builds internal stage→storage map from configuration
  - Routes each stage's operations to its configured storage backend
- `EObjectRegistryService<T>` (injected) - for metadata management
- `PromiseFactory` (injected) - for async operations
- `PostReleaseActionService` (injected, optional) - for post-release hooks

**Key Design**:
- Each registry injects MULTIPLE storage service instances (based on stage.storage.mappings config)
- RegistryService builds a map: stageName → storageService
- Storage service accepts (scope, registry, stage) as parameters to construct identifiers dynamically
- Schema registry example:
  - draft stage → routes to MongoDB storage service
  - approved stage → routes to MinIO storage service
  - release stage → routes to Apicurio storage service
- Each storage service constructs groupIds/paths like "tenant1-schema-draft" dynamically

---

### Layer 3: ScopeService (Highest Level - Orchestration & Validation)

**Purpose**: Orchestrates all data operations for this scope by validating and delegating to RegistryService.

**Responsibilities**:
- **Orchestrates CRUD operations** for objects within this scope
- Registry validation (is this registry valid for my scope?)
- Delegates to RegistryService for stage validation AND storage operations
- Parent scope delegation for hierarchical lookups
- **No direct storage access** - delegates all storage to RegistryService

**Service Type**: OSGi Component with factory configuration (one instance per scope/tenant)

**Key Methods**:
```java
public interface ScopeService<T extends EObject> {
    // Upload/Create
    Promise<ObjectMetadata> uploadToStageForRegistry(String registry, String stage, T object, ObjectMetadata metadata);

    // Retrieve
    ObjectMetadata getMetadataFromStageForRegistry(String registry, String stage, String objectId);
    T getContentFromStageForRegistry(String registry, String stage, String objectId);
    ObjectMetadata getFromFinalStageForRegistry(String registry, String objectId);
    T getContentFromFinalStageForRegistry(String registry, String objectId);

    // Update
    Promise<ObjectMetadata> updateInStageForRegistry(String registry, String stage, T updatedObject, String objectId, String version);

    // Delete
    Promise<Boolean> deleteFromStageForRegistry(String registry, String stage, String objectId);

    // List
    List<ObjectMetadata> listInStageForRegistry(String registry, String stage);
    List<ObjectMetadata> listInStageForRegistryByName(String registry, String stage, String name);
    List<ObjectMetadata> listInFinalStageForRegistry(String registry);

    // Transitions
    ObjectMetadata transitionToStageForRegistry(String registry, String objectId, String fromStage, String toStage);

    // Registry Access & Validation
    RegistryService getRegistryService(String registryName);
    List<String> getAllRegistries();
    boolean isValidRegistry(String registryName);

    // Scope Info
    String getScopeName();
    String getParentScope();

    // Parent Delegation
    Optional<ScopeService> getParentScopeService();
    <T extends EObject> ObjectMetadata getFromParentFinalStage(String registry, String objectId);
}
```

**Orchestration Flow**:
1. ScopeService receives request (e.g., `uploadToStageForRegistry("schema", "draft", ...)`)
2. ScopeService validates: Is "schema" a valid registry for this scope?
3. ScopeService gets the RegistryService for "schema"
4. ScopeService delegates to RegistryService: `registryService.uploadToStage(this.scopeName, "draft", ...)`
5. RegistryService validates stage and performs storage operation

**Service Properties**:
- `scope.name` - Scope identifier
- `scope.parent` - Parent scope name (optional)

**Configuration Properties**:
- `scope.name` - Scope identifier (must be unique)
- `scope.parent` - Parent scope name (optional, for hierarchical lookups)
- `registryService.target` - OSGi target filter to select RegistryService instances by registry name
  - Example: `(|(registry.name=schema)(registry.name=configuration))`
  - Note: Filters by registry name, NOT by scope, since registries are scope-agnostic
  - This filter is automatically applied to the `@Reference List<RegistryService>` parameter in the constructor
- `parentScopeService.target` - OSGi target filter for parent scope (optional)
  - Example: `(scope.name=atlas)`
  - This filter is automatically applied to the optional `@Reference ScopeService` parameter in the constructor

**Configuration Interface**:
```java
@ObjectClassDefinition(name = "Scope Service Configuration")
public @interface ScopeServiceConfig {
    @AttributeDefinition(name = "Scope Name")
    String scope_name();

    @AttributeDefinition(name = "Parent Scope Name")
    String scope_parent() default "";

    @AttributeDefinition(name = "Registry Service Target Filter")
    String registryService_target() default "(registry.name=*)";

    @AttributeDefinition(name = "Parent Scope Service Target Filter")
    String parentScopeService_target() default "";
}
```

**Dependencies**:
- `List<RegistryService>` (injected via @Reference with MULTIPLE cardinality and target filter from config) - for validation and delegation
- Optional parent `ScopeService` (injected via @Reference with OPTIONAL cardinality and target filter from config) - for hierarchical lookups

**Note**: ScopeService does NOT inject storage services directly. All storage operations are delegated to RegistryService, which handles per-stage storage routing.

---

## Prerequisites: Storage API Refactoring

**IMPORTANT**: Before implementing the workflow service refactoring, the storage API must be refactored to support dynamic context parameters.

### Current Storage API Problem

Currently, `EObjectStorageServiceCollector.getStorage(scope, registry, stage)` returns pre-configured storage instances per (scope, registry, stage) tuple. This creates:
- Massive number of instances (N scopes × M registries × P stages)
- Storage type is tied to the tuple, not to registry type
- Cannot easily configure "schema uses Apicurio, configuration uses Minio"

### Required Storage API Changes

**Refactor storage interfaces to accept context parameters:**

```java
public interface EObjectStorageService<T extends EObject> {
    // Add scope, registry, stage as parameters to all methods
    Promise<ObjectMetadata> save(String scope, String registry, String stage, T object, ObjectMetadata metadata);
    T load(String scope, String registry, String stage, String objectId);
    Promise<Boolean> delete(String scope, String registry, String stage, String objectId);
    List<ObjectMetadata> list(String scope, String registry, String stage);
    // ... all other methods updated similarly
}
```

**Update implementations to construct identifiers dynamically:**

**Apicurio Storage Implementation:**
```java
public Promise<ObjectMetadata> save(String scope, String registry, String stage, T object, ObjectMetadata metadata) {
    // Dynamically construct groupId: "tenant1-schema-draft"
    String groupId = scope + "-" + registry + "-" + stage;
    // Use groupId for Apicurio API operations
    return apicurioClient.createArtifact(groupId, ...);
}
```

**File Storage Implementation:**
```java
public Promise<ObjectMetadata> save(String scope, String registry, String stage, T object, ObjectMetadata metadata) {
    // Dynamically construct path: "/data/tenant1/schema/draft/"
    Path storagePath = basePath.resolve(scope).resolve(registry).resolve(stage);
    // Use path for file operations
    Files.write(storagePath.resolve(objectId), ...);
}
```

**Benefits:**
- ONE storage service instance per storage type (not per scope+registry+stage)
- Storage backend is configured per registry type
- More flexible and scalable

**Storage Service Type Property:**

Each storage service implementation must expose a `storage.type` service property so that RegistryService can match storage services to stage mappings:

```java
@Component(
    service = EObjectStorageService.class,
    property = {
        "storage.type=mongodb"  // MongoDB storage service
    }
)
public class MongoDBStorageService implements EObjectStorageService<EObject> {
    // ...
}

@Component(
    service = EObjectStorageService.class,
    property = {
        "storage.type=apicurio"  // Apicurio storage service
    }
)
public class ApicurioStorageService implements EObjectStorageService<EObject> {
    // ...
}
```

Alternatively, add a method to the storage interface:
```java
public interface EObjectStorageService<T extends EObject> {
    String getStorageType();  // Returns "mongodb", "apicurio", "minio", etc.
    // ... other methods
}
```

**Files to Modify:**
1. `org.eclipse.fennec.model.atlas.management/src-gen-api/org/eclipse/fennec/model/atlas/mgmt/api/EObjectStorageService.java` - Add scope, registry, stage parameters; add getStorageType() method
2. `org.eclipse.fennec.model.atlas.management.file/src/org/eclipse/fennec/model/atlas/management/file/EObjectFileStorageService.java` - Update to construct paths dynamically; add storage.type property
3. `org.eclipse.fennec.model.atlas.management.apicurio/src/.../EObjectApicurioStorageService.java` - Update to construct groupIds dynamically; add storage.type property
4. All other storage implementations - Add storage.type property or implement getStorageType()

---

## Refactoring Steps

### Phase 1: Create StageService

**Files to Create**:
1. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src-wf-api/org/eclipse/fennec/model/atlas/wf/workflowapi/StageService.java` (interface)
2. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/StageServiceImpl.java` (OSGi component implementation)
3. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/config/StageServiceConfig.java` (OSGi metatype config)

**Implementation Details**:
- OSGi component with factory configuration: `@Designate(ocd = StageServiceConfig.class, factory = true)`
- Each instance represents a single stage (e.g., "draft", "approved", "release")
- Service properties populated from configuration:
  - `stage.name` - from config
  - `stage.writable` - from config
  - `stage.final` - from config
- Simple getters for configuration values
- Immutable after activation
- Thread-safe (no mutable state)

**Configuration Interface**:
```java
@ObjectClassDefinition(name = "Stage Service Configuration")
public @interface StageServiceConfig {
    @AttributeDefinition(name = "Stage Name", description = "The name of this stage")
    String stage_name();

    @AttributeDefinition(name = "Writable", description = "Whether this stage allows modifications")
    boolean stage_writable() default true;

    @AttributeDefinition(name = "Final Stage", description = "Whether this is the final stage")
    boolean stage_final() default false;
}
```

**Dependencies**: None (pure configuration-based service)

---

### Phase 2: Create RegistryService

**Files to Create**:
1. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src-wf-api/org/eclipse/fennec/model/atlas/wf/workflowapi/RegistryService.java` (interface)
2. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/RegistryServiceImpl.java` (implementation)
3. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/config/RegistryServiceConfig.java` (config interface)

**Implementation Details**:
- Extract ALL `*ForRegistry` methods from `EObjectWorkflowServiceImpl` and remove the registry parameter
- Add scope parameter as first parameter to all CRUD methods
- Extract stage validation logic from `WorkflowServiceHelper` and `EObjectWorkflowServiceImpl`
- Inject all dependencies via constructor in `@Activate` method
- Build internal stage lookup map (stageName → StageService) in constructor
- Parse `workflow.transitions` configuration in constructor to build transition map (fromStage → Set<toStage>)
- **Parse `stage.storage.mappings` configuration to build stage→storage routing map**
- Implement stage validation methods using the injected StageService collection:
  - `isValidStage()` - check if stage name exists in the map
  - `isWritableStage()` - check if stage exists and is writable
  - `isTransitionAllowed()` - check if transition exists in the configured transition map
- Implement stage metadata queries (getFinalStage, getAllStages, etc.)
- **Implement all CRUD operations with scope parameter**: upload, retrieve, update, delete, list, transition
- **Route each operation to the appropriate storage service based on stage**
- Maintain the lock map for transactional operations (per scope+registry+objectId)
- Use service properties: `registry.name` (NO scope property - service is scope-agnostic!)
- OSGi component with `@Designate(ocd = RegistryServiceConfig.class, factory = true)` for factory configuration

**Key Design**: Multiple storage backends are injected based on `stage.storage.mappings` from config:
- Schema registry → `stage.storage.mappings=draft:mongodb,approved:minio,release:apicurio`
- Injects MongoDB, MinIO, and Apicurio storage services
- Routes draft operations to MongoDB, approved to MinIO, release to Apicurio
- Each storage service constructs identifiers (groupIds, paths) dynamically from (scope, registry, stage) parameters

**Constructor Injection with Dependencies**:
```java
@Activate
public RegistryServiceImpl(
    RegistryServiceConfig config,
    @Reference List<StageService> stageServices,
    @Reference List<EObjectStorageService<T>> storageServices,  // MULTIPLE storage services
    @Reference EObjectRegistryService<T> objectRegistryService,
    @Reference PromiseFactory promiseFactory,
    @Reference(cardinality = ReferenceCardinality.OPTIONAL) PostReleaseActionService postReleaseActionService
) {
    this.registryName = config.registry_name();
    this.objectRegistryService = objectRegistryService;
    this.promiseFactory = promiseFactory;
    this.postReleaseActionService = postReleaseActionService;

    // Build internal lookup map: stageName -> StageService
    this.stageMap = stageServices.stream()
        .collect(Collectors.toMap(StageService::getStageName, Function.identity()));

    // Parse workflow transitions: "draft:approved,approved:release" -> Map<String, Set<String>>
    this.transitionMap = parseTransitions(config.workflow_transitions());

    // Parse stage storage mappings: "draft:mongodb,approved:minio,release:apicurio" -> Map<String, EObjectStorageService>
    this.stageStorageMap = parseStageStorageMappings(config.stage_storage_mappings(), storageServices);
}

private Map<String, Set<String>> parseTransitions(String transitionsConfig) {
    Map<String, Set<String>> map = new HashMap<>();
    if (transitionsConfig != null && !transitionsConfig.isEmpty()) {
        for (String transition : transitionsConfig.split(",")) {
            String[] parts = transition.split(":");
            if (parts.length == 2) {
                map.computeIfAbsent(parts[0].trim(), k -> new HashSet<>()).add(parts[1].trim());
            }
        }
    }
    return map;
}

private Map<String, EObjectStorageService<T>> parseStageStorageMappings(
        String[] mappings,
        List<EObjectStorageService<T>> storageServices) {
    Map<String, EObjectStorageService<T>> map = new HashMap<>();

    // Build storageType -> service lookup
    Map<String, EObjectStorageService<T>> storageByType = storageServices.stream()
        .collect(Collectors.toMap(
            s -> s.getStorageType(),  // Assumes storage services expose their type
            Function.identity()
        ));

    // Parse stage:storageType mappings
    for (String mapping : mappings) {
        String[] parts = mapping.split(":");
        if (parts.length == 2) {
            String stageName = parts[0].trim();
            String storageType = parts[1].trim();
            EObjectStorageService<T> storage = storageByType.get(storageType);
            if (storage != null) {
                map.put(stageName, storage);
            }
        }
    }
    return map;
}
```

**Note**:
- The target filter `stageService.target` from config is automatically applied to the `@Reference List<StageService>` injection
- Multiple storage services are injected and mapped to stages based on `stage.storage.mappings`
- RegistryService routes each stage's operations to its configured storage backend
- No need for explicit bind/unbind methods!

**CRUD Implementation Example**:
```java
public Promise<ObjectMetadata> uploadToStage(String scope, String stage, T object, ObjectMetadata metadata) {
    // Validate stage
    if (!isValidStage(stage)) {
        throw new IllegalArgumentException("Invalid stage: " + stage);
    }
    if (!isWritableStage(stage)) {
        throw new IllegalArgumentException("Stage not writable: " + stage);
    }

    // Get the storage service for this stage
    EObjectStorageService<T> storageService = stageStorageMap.get(stage);
    if (storageService == null) {
        throw new IllegalStateException("No storage configured for stage: " + stage);
    }

    // Call storage service with scope, registry, stage parameters
    // Storage service constructs identifiers like "tenant1-schema-draft"
    return storageService.save(scope, this.registryName, stage, object, metadata);
}
```

**Cross-Storage Transition Implementation**:
```java
public ObjectMetadata transitionToStage(String scope, String objectId, String fromStage, String toStage) {
    // Validate transition
    if (!isTransitionAllowed(fromStage, toStage)) {
        throw new IllegalArgumentException("Transition not allowed: " + fromStage + " → " + toStage);
    }

    // Get storage services for both stages
    EObjectStorageService<T> fromStorage = stageStorageMap.get(fromStage);
    EObjectStorageService<T> toStorage = stageStorageMap.get(toStage);

    // Load object from source stage
    T object = fromStorage.load(scope, this.registryName, fromStage, objectId);
    ObjectMetadata metadata = fromStorage.loadMetadata(scope, this.registryName, fromStage, objectId);

    // Save to target stage (potentially different storage backend!)
    // Example: MongoDB draft → Apicurio release
    toStorage.save(scope, this.registryName, toStage, object, metadata).getValue();

    // Optionally delete from source stage if configured
    // fromStorage.delete(scope, this.registryName, fromStage, objectId);

    return metadata;
}
```

**Note**: The transition method handles cross-storage copying seamlessly. When transitioning from draft (MongoDB) to release (Apicurio), the object is loaded from MongoDB and saved to Apicurio automatically.

**Validation Methods Extracted from WorkflowServiceHelper**:
- `WorkflowServiceHelper.isStageAllowed()` → `RegistryService.isValidStage()`
- `WorkflowServiceHelper.isStageWritable()` → `RegistryService.isWritableStage()`
- `WorkflowServiceHelper.areStagesSubsequent()` → `RegistryService.isTransitionAllowed()` (now uses transition map instead of sequential index check)

**Transition Validation Implementation**:
```java
public boolean isTransitionAllowed(String fromStage, String toStage) {
    // Check both stages exist
    if (!isValidStage(fromStage) || !isValidStage(toStage)) {
        return false;
    }
    // Check if transition is explicitly allowed in configuration
    Set<String> allowedTargets = transitionMap.get(fromStage);
    return allowedTargets != null && allowedTargets.contains(toStage);
}
```

**Dependencies**:
- `List<StageService>` (mandatory, injected via @Reference)

**Benefits of Transition Rules in RegistryService**:
1. **Maximum Stage Reuse**: Same "draft", "approved", "release" stages can be used by all registries
2. **Flexible Workflows**: Each registry defines its own workflow independently
   - Schema registry: `draft → approved → release`
   - Configuration registry: `draft → release` (skip approved stage)
   - Model registry: `draft → approved → release` with additional `draft → release` (allows skipping)
3. **Non-Linear Workflows**: Support for branching and stage skipping via explicit transition rules
4. **Clear Ownership**: RegistryService owns its workflow definition, StageService stays simple
5. **Simpler Configuration**: Fewer StageService instances to configure

**Example: Why this approach is better than sequenceIndex**:

Consider two registries with different workflows:
- **Registry 1 (schema)**: draft → approved → released
- **Registry 2 (configuration)**: draft → release

**With sequenceIndex approach (problematic)**:
```properties
# Would need TWO different draft instances!
# draft-for-registry1.cfg
stage.name=draft
stage.sequence.index=0  # allows transition to index 1 (approved)

# draft-for-registry2.cfg
stage.name=draft
stage.sequence.index=0  # allows transition to index 1 (release, not approved!)
```
Problem: Can't reuse the same "draft" stage across registries.

**With workflow.transitions approach (recommended)**:
```properties
# ONE draft instance shared by both registries
# stage-draft.cfg
stage.name=draft
stage.writable=true

# Each registry defines its own transitions
# registry-schema.cfg
workflow.transitions=draft:approved,approved:released

# registry-configuration.cfg
workflow.transitions=draft:release
```
Solution: Same "draft" stage used by both registries, each with different allowed transitions!

---

### Phase 3: Create ScopeService

**Files to Create**:
1. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src-wf-api/org/eclipse/fennec/model/atlas/wf/workflowapi/ScopeService.java` (interface)
2. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/ScopeServiceImpl.java` (implementation)
3. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/config/ScopeServiceConfig.java` (config interface)

**Implementation Details**:
- Extract ALL `*ForRegistry` CRUD methods from `EObjectWorkflowServiceImpl`
- Keep the method signatures the same but implementation becomes pure delegation
- Inject all dependencies via constructor in `@Activate` method
- Build internal registry lookup map (registryName → RegistryService) in constructor
- **NO storage dependencies** - all storage handled by RegistryService
- Implement orchestration flow:
  1. Validate registry is valid for this scope: `isValidRegistry(registryName)`
  2. Get the RegistryService for this registry
  3. Delegate to RegistryService: `registryService.uploadToStage(this.scopeName, stage, ...)`
- Implement parent scope delegation logic for hierarchical lookups
- Service properties: `scope.name`, `scope.parent`
- OSGi component with `@Designate(ocd = ScopeServiceConfig.class, factory = true)` for factory configuration

**Constructor Injection with Target Filters**:
```java
@Activate
public ScopeServiceImpl(
    ScopeServiceConfig config,
    @Reference List<RegistryService> registryServices,
    @Reference(cardinality = ReferenceCardinality.OPTIONAL) ScopeService parentScopeService
) {
    this.scopeName = config.scope_name();
    this.parentScopeName = config.scope_parent();
    this.parentScopeService = parentScopeService;

    // Build internal lookup map: registryName -> RegistryService
    this.registryMap = registryServices.stream()
        .collect(Collectors.toMap(RegistryService::getRegistryName, Function.identity()));
}
```

**Note**: Target filters from config (`registryService.target`, `parentScopeService.target`) are automatically applied. No need for explicit bind/unbind methods! All storage operations are delegated to RegistryService.

**Implementation Example for Orchestration Flow**:
```java
public Promise<ObjectMetadata> uploadToStageForRegistry(String registry, String stage, T object, ObjectMetadata metadata) {
    // Step 1: Validate registry is valid for this scope
    if (!isValidRegistry(registry)) {
        throw new IllegalArgumentException("Invalid registry: " + registry);
    }

    // Step 2: Get RegistryService for this registry
    RegistryService<T> registryService = getRegistryService(registry);

    // Step 3: Delegate to RegistryService (which validates stage and performs storage)
    return registryService.uploadToStage(this.scopeName, stage, object, metadata);
}
```

**Note**: The RegistryService handles stage validation AND storage operations. This allows each registry to use its own storage backend (e.g., schema → Apicurio, configuration → Minio).

**Parent Scope Delegation**:
- Parent scope injected as optional parameter in constructor (shown above)
- When object not found in current scope's registry, delegate to parent's final stage
- Target filter `parentScopeService.target` from config automatically applied

**Dependencies** (all injected via constructor):
- `List<RegistryService>` - for validation and delegation (filtered by `registryService.target`)
- `ScopeService` (optional) - parent scope for hierarchical lookups (filtered by `parentScopeService.target`)

**Key Design**: ScopeService has NO storage dependencies. All storage operations are delegated to RegistryService, which allows each registry to use its own storage backend.

---

### Phase 4: Refactor EObjectWorkflowService

**Files to Modify**:
1. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src-wf-api/org/eclipse/fennec/model/atlas/wf/workflowapi/EObjectWorkflowService.java`
2. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/EObjectWorkflowServiceImpl.java`

**Implementation Strategy**:

**Option A: Keep as Facade (Recommended)**
- Keep the existing interface and method signatures unchanged
- Inject `ScopeService`, delegate all operations
- Maintains backward compatibility
- Minimal changes to calling code

```java
@Component(...)
public class EObjectWorkflowServiceImpl<T extends EObject> implements EObjectWorkflowService<T> {

    @Reference
    private ScopeService<T> scopeService;

    @Override
    public Promise<ObjectMetadata> uploadToStageForRegistry(String stage, String registry, T object, ObjectMetadata metadata) {
        // Direct delegation - ScopeService handles validation and storage
        return scopeService.uploadToStageForRegistry(registry, stage, object, metadata);
    }

    @Override
    public ObjectMetadata getFromStageForRegistry(String stage, String registry, String objectId) {
        // Direct delegation - ScopeService handles validation, retrieval, and parent delegation
        return scopeService.getMetadataFromStageForRegistry(registry, stage, objectId);
    }

    @Override
    public ObjectMetadata transitionToStageForRegistry(String objectId, String fromStage, String toStage, String registry) {
        // Direct delegation - ScopeService handles validation and transition
        return scopeService.transitionToStageForRegistry(registry, objectId, fromStage, toStage);
    }

    // ... delegate all other methods similarly (simple pass-through)
}
```

**Note**: The facade is now a simple pass-through to ScopeService. The method signatures match perfectly, just parameter order is adjusted (registry comes first in ScopeService).

**Option B: Deprecate and Replace**
- Mark `EObjectWorkflowService` as `@Deprecated`
- Direct clients to use `ScopeService` API instead
- Requires updating all calling code
- Cleaner architecture but more invasive

**Recommendation**: Use Option A (facade pattern) to maintain compatibility.

---

### Phase 5: Update Configuration

**Files to Modify**:
1. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/config/WorkflowServiceConfig.java`

**Changes**:
- Split configuration into three separate service configs:
  - `StageServiceConfig` - stage.name, stage.writable, stage.final (NO sequenceIndex!)
  - `RegistryServiceConfig` - registry.name, storage.type, workflow.transitions, stageService.target (NO scope property!)
  - `ScopeServiceConfig` - scope.name, scope.parent, registryService.target

**Configuration Strategy**:
- Create factory configurations for each service type
- Use OSGi Configuration Admin to create instances
- For a multi-tenant system with multiple registries:
  1. M `StageService` instances (one per unique stage name - can be shared across registries with different workflows!)
  2. N `RegistryService` instances (one per registry type, shared across scopes/tenants, each defining its own workflow.transitions)
  3. P `ScopeService` instances (one per scope/tenant, each injecting the registries it needs)
- **Key Advantage**: With workflow.transitions in RegistryService, you need fewer StageService instances
  - Example: Both "schema" and "configuration" registries can share the same "draft" and "release" stages
  - Without workflow.transitions, you'd need separate stage instances for each registry's workflow

**Example Configuration for 2 Scopes (atlas, tenant1) using same registry (schema)**:
- 3 StageService instances: draft, approved, release
- 1 RegistryService instance: schema (shared by both scopes)
- 2 ScopeService instances: atlas, tenant1 (both inject the same schema RegistryService)

**Example Configuration Files**:

**Stage Configurations for Schema Workflow**:

`modelatlas.runtime/workspace/config/stage-draft.cfg`:
```properties
# StageService for draft stage
stage.name=draft
stage.writable=true
stage.final=false
```

`modelatlas.runtime/workspace/config/stage-approved.cfg`:
```properties
# StageService for approved stage
stage.name=approved
stage.writable=true
stage.final=false
```

`modelatlas.runtime/workspace/config/stage-release.cfg`:
```properties
# StageService for release stage
stage.name=release
stage.writable=true
stage.final=true
```

**Registry Configuration** (`modelatlas.runtime/workspace/config/registry-schema.cfg`):
```properties
# RegistryService factory configuration for schema registry
# This registry is scope-agnostic and can serve multiple scopes/tenants
registry.name=schema
# Per-stage storage backends - enables draft in MongoDB, release in Maven Central!
stage.storage.mappings=draft:mongodb,approved:minio,release:apicurio
# Define the workflow transitions for this registry
workflow.transitions=draft:approved,approved:release
# Target filter to inject draft, approved, and release stages
stageService.target=(|(stage.name=draft)(stage.name=approved)(stage.name=release))
```

**Stage Configurations for Configuration Workflow** (different stages):

`modelatlas.runtime/workspace/config/stage-development.cfg`:
```properties
stage.name=development
stage.writable=true
stage.final=false
```

`modelatlas.runtime/workspace/config/stage-testing.cfg`:
```properties
stage.name=testing
stage.writable=true
stage.final=false
```

`modelatlas.runtime/workspace/config/stage-production.cfg`:
```properties
stage.name=production
stage.writable=false
stage.final=true
```

**Registry Configuration** (`modelatlas.runtime/workspace/config/registry-configuration.cfg`):
```properties
# RegistryService factory configuration for configuration registry
# This registry uses different stages than schema and is scope-agnostic
registry.name=configuration
# Per-stage storage backends - different from schema registry!
stage.storage.mappings=development:file,testing:minio,production:s3
# Define the workflow transitions for this registry (different from schema workflow!)
workflow.transitions=development:testing,testing:production
# Target filter to inject development, testing, and production stages
stageService.target=(|(stage.name=development)(stage.name=testing)(stage.name=production))
```

**ScopeService Configuration** (`modelatlas.runtime/workspace/config/scope-atlas.cfg`):
```properties
# ScopeService configuration for atlas scope
scope.name=atlas
scope.parent=
# Target filter to inject registries by name (not by scope!)
# This scope uses both schema and configuration registries
registryService.target=(|(registry.name=schema)(registry.name=configuration))
```

**Another Scope Using Same Registry** (`modelatlas.runtime/workspace/config/scope-tenant1.cfg`):
```properties
# ScopeService configuration for tenant1 scope
# This tenant also uses the schema registry (shared instance)
scope.name=tenant1
scope.parent=atlas
# Same registry, different scope/tenant
registryService.target=(registry.name=schema)
# Could also use: parentScopeService.target=(scope.name=atlas)
```

**Key Point**: Both "atlas" and "tenant1" scopes inject the SAME RegistryService instance for "schema". The RegistryService doesn't know or care which scope is calling it - it just processes the scope parameter passed to its methods.

---

### Phase 6: Update ScopeCollector

**Files to Modify**:
1. `/opt/git/model.atlas/org.eclipse.fennec.model.atlas.scope/src/org/eclipse/fennec/model/atlas/scope/ScopeCollector.java`

**Changes**:
- Update to collect `ScopeService` instances instead of (or in addition to) `EObjectWorkflowService`
- Extract scope metadata from `ScopeService.getScopeName()`, `getParentScope()`, etc.
- Build Scope EMF objects from ScopeService properties

---

### Phase 7: Testing Strategy

**Unit Tests to Create**:
1. `StageServiceImplTest.java` - Test stage configuration and getters (OSGi component tests)
2. `RegistryServiceImplTest.java` - Test stage validation logic with mock StageService list, test stage queries
3. `ScopeServiceImplTest.java` - Test CRUD operations, registry/stage validation flow, parent delegation, storage integration

**Integration Tests to Update**:
1. Update existing workflow integration tests to verify behavior unchanged
2. Add tests for new service interactions
3. Test OSGi service registration and binding/unbinding

**Test Scenarios**:
- Stage validation (valid/invalid stages, writable checks)
- Stage transitions (allowed/disallowed per workflow.transitions, boundary cases)
- Workflow configurations (linear workflows, non-linear workflows with branching, stage skipping)
- Registry operations (upload, retrieve, update, delete, list)
- Scope hierarchy (parent delegation, final stage lookup)
- Service lifecycle (activation, configuration, deactivation)
- Concurrent operations (locks, thread safety)

---

## Critical Files Summary

### Files to Create (New)
1. `org.eclipse.fennec.model.atlas.workflow/src-wf-api/org/eclipse/fennec/model/atlas/wf/workflowapi/StageService.java` (interface)
2. `org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/StageServiceImpl.java` (OSGi component)
3. `org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/config/StageServiceConfig.java` (OSGi metatype config)
4. `org.eclipse.fennec.model.atlas.workflow/src-wf-api/org/eclipse/fennec/model/atlas/wf/workflowapi/RegistryService.java` (interface)
5. `org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/RegistryServiceImpl.java` (OSGi component)
6. `org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/config/RegistryServiceConfig.java` (OSGi metatype config)
7. `org.eclipse.fennec.model.atlas.workflow/src-wf-api/org/eclipse/fennec/model/atlas/wf/workflowapi/ScopeService.java` (interface)
8. `org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/ScopeServiceImpl.java` (OSGi component)
9. `org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/config/ScopeServiceConfig.java` (OSGi metatype config)

### Files to Modify (Existing)
1. `org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/impl/EObjectWorkflowServiceImpl.java` - Refactor to facade pattern
2. `org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/config/WorkflowServiceConfig.java` - Split configuration
3. `org.eclipse.fennec.model.atlas.scope/src/org/eclipse/fennec/model/atlas/scope/ScopeCollector.java` - Update to use ScopeService
4. Configuration files in `modelatlas.runtime/workspace/config/` - Update to new configuration structure

### Files to Keep (Reference)
1. `org.eclipse.fennec.model.atlas.workflow/src/org/eclipse/fennec/model/atlas/workflow/util/WorkflowServiceHelper.java` - May extract stage logic from here
2. `org.eclipse.fennec.model.atlas.management/src-gen-api/org/eclipse/fennec/model/atlas/mgmt/api/EObjectRegistryService.java` - Existing dependency
3. `org.eclipse.fennec.model.atlas.management/src/org/eclipse/fennec/model/atlas/mgmt/collector/EObjectStorageServiceCollector.java` - Existing dependency

---

## Verification Plan

### Build Verification
```bash
./gradlew clean build
./gradlew :org.eclipse.fennec.model.atlas.workflow:build
./gradlew test
```

### Runtime Verification
1. Start the runtime with updated configurations
2. Verify StageService instances are created for each stage (draft, approved, release, etc.)
3. Verify RegistryService instances are created per (scope, registry) with correct StageService bindings
4. Verify ScopeService instances are created per scope with correct RegistryService bindings
5. Verify EObjectWorkflowService still works as facade
6. Test workflow operations: upload → transition → retrieve
7. Test parent scope delegation
8. Monitor OSGi service registrations in console using `services` and `inspect` commands
9. Verify target filters are working correctly (check service references)

### Integration Testing
1. Upload an EPackage to draft stage for schema registry
2. Transition to approved stage
3. Transition to release stage
4. Verify parent scope can access child scope's released objects
5. Test with multiple scopes and registries
6. Verify service lifecycle (stop/start bundles)

---

## Benefits of This Refactoring

1. **Clear Separation of Concerns**:
   - **StageService**: Pure configuration (stage metadata)
   - **RegistryService**: Stage validation + storage operations
   - **ScopeService**: Orchestration only (no storage knowledge)

2. **Per-Stage Storage Configuration**:
   - Each stage within a registry can use a different storage backend
   - Schema registry example:
     - draft stage → MongoDB (fast, flexible for work-in-progress)
     - approved stage → MinIO (object storage for review)
     - release stage → Apicurio/Maven (immutable, versioned artifacts)
   - Configuration registry example:
     - development → File storage (local, fast iterations)
     - testing → MinIO (shared testing environment)
     - production → S3 (highly available, durable)
   - Enables optimal storage per lifecycle phase (transient vs. permanent, cost vs. performance)

3. **True Multi-Tenancy**:
   - RegistryService is scope-agnostic, serving multiple scopes/tenants
   - Tenant isolation via scope parameter passed to RegistryService methods
   - Single RegistryService instance handles storage for all tenants using that registry type

4. **Better Encapsulation**:
   - ScopeService orchestrates but doesn't touch storage
   - RegistryService owns storage operations for its registry type
   - Clean delegation chain: Scope → Registry → Storage

5. **Testability**:
   - Each layer can be unit tested independently
   - ScopeService tests don't need storage mocks (just mock RegistryService)
   - RegistryService tests can mock storage collector

6. **Flexibility**:
   - Can add/remove stages dynamically via configuration without code changes
   - Independent Workflow Configuration: Each registry defines its own transition rules
   - Support for non-linear workflows (branching, stage skipping) via explicit transition rules
   - Maximum stage reuse across registries with different workflows

7. **Maintainability**:
   - Clearer code structure, easier to understand and modify
   - Validation logic centralized in RegistryService
   - Storage operations centralized in ScopeService

8. **Backward Compatibility**:
   - Facade pattern maintains existing API
   - Simple pass-through delegation from EObjectWorkflowService to ScopeService

9. **OSGi Best Practices**:
   - Follows collector pattern, dynamic references, target filters, proper service properties

10. **Runtime Reconfiguration**:
    - Stages, registries, and scopes can be added/removed at runtime via ConfigAdmin

11. **Service Reuse**:
    - StageService instances can be shared across multiple registries
    - RegistryService instances can be shared across multiple scopes

12. **Resource Efficiency**:
    - Sharing services across tenants reduces memory footprint and improves performance

13. **Practical Per-Stage Storage Use Cases**:
    - **Draft stage** → MongoDB: Fast iterations, flexible schema, easy rollback
    - **Approved/Testing stage** → MinIO/S3: Object storage for staging/review artifacts
    - **Release stage** → Maven Central/Apicurio: Immutable versioned artifacts, public distribution
    - **Development stage** → File system: Local development, no network latency
    - **Production stage** → Cloud storage (S3, Azure Blob): High availability, durability, compliance
    - Cost optimization: Use cheap storage for transient stages, premium for permanent stages
    - Lifecycle policies: Auto-expire drafts after 30 days, keep releases forever
    - Performance optimization: Fast local storage for iteration, distributed storage for sharing

---

## Migration Path

1. **Phase 0 (PREREQUISITE)**: Refactor Storage API
   - Update `EObjectStorageService` interface to accept (scope, registry, stage) parameters
   - Update all storage implementations (Apicurio, Minio, File) to construct identifiers dynamically
   - Update storage service registrations to use `storage.type` property
   - **CRITICAL**: This must be completed before workflow refactoring

2. **Phase 1-3**: Implement new workflow services (non-breaking, additive)
   - StageService, RegistryService, ScopeService

3. **Phase 4**: Refactor EObjectWorkflowService to use new services (transparent to clients)

4. **Phase 5-6**: Update configurations and collectors

5. **Phase 7**: Test thoroughly

6. **Future**: Optionally deprecate old API and migrate clients to use ScopeService directly

This approach allows for incremental development and testing without breaking existing functionality.

---

## Final Architecture Summary

### Service Responsibilities

1. **StageService** (Configuration)
   - Pure configuration service
   - Represents one stage (e.g., "draft")
   - Properties: name, writable, final flag
   - No logic, just getters
   - Can be reused across registries with different workflows

2. **RegistryService** (Validation + Storage Routing + Workflow Definition)
   - Represents one registry type (e.g., "schema")
   - Defines workflow transitions via `workflow.transitions` configuration
   - Validates stages and transitions against configured workflow
   - Handles CRUD operations with scope parameter
   - Injects MULTIPLE storage services based on `stage.storage.mappings` config
   - Routes each stage's operations to its configured storage backend
   - Handles cross-storage transitions (e.g., copying from MongoDB draft to Maven release)
   - Scope-agnostic: serves multiple tenants

3. **ScopeService** (Orchestration)
   - Represents one tenant/scope (e.g., "atlas", "tenant1")
   - Validates registry membership
   - Delegates to RegistryService for all operations
   - No storage dependencies
   - Handles parent scope delegation

### Data Flow Example

**User Request**: Upload schema to draft stage, then transition to release for tenant1

```
# Upload to draft stage
EObjectWorkflowService.uploadToStageForRegistry("draft", "schema", schema, metadata)
  ↓
ScopeService["tenant1"].uploadToStageForRegistry("schema", "draft", schema, metadata)
  ↓ validates "schema" is valid for "tenant1" scope
  ↓ gets RegistryService["schema"]
  ↓
RegistryService["schema"].uploadToStage("tenant1", "draft", schema, metadata)
  ↓ validates "draft" is a valid stage (exists in stage map)
  ↓ validates "draft" is writable (checks StageService.isWritable())
  ↓ gets storage service for "draft" stage from stageStorageMap
  ↓ routes to MongoDB storage service
  ↓
MongoDBStorageService.save("tenant1", "schema", "draft", schema, metadata)
  ↓ constructs collection/document identifier: "tenant1-schema-draft"
  ↓ stores in MongoDB (fast, flexible document storage for work-in-progress)

# Later: Transition from draft to release
EObjectWorkflowService.transitionToStageForRegistry(objectId, "draft", "release", "schema")
  ↓
RegistryService["schema"].transitionToStage("tenant1", objectId, "draft", "release")
  ↓ validates transition is allowed (checks workflow.transitions: draft→approved→release)
  ↓ loads object from draft stage (MongoDB)
  ↓ gets storage service for "release" stage from stageStorageMap
  ↓ routes to Apicurio storage service
  ↓
ApicurioStorageService.save("tenant1", "schema", "release", schema, metadata)
  ↓ constructs groupId: "tenant1-schema-release"
  ↓ stores in Apicurio/Maven Central (immutable, versioned artifact repository)
```

### Key Benefits

1. **Per-stage storage routing**: Draft → MongoDB, Release → Maven Central (optimal storage per lifecycle)
2. **True multi-tenancy**: One RegistryService serves all tenants, routes to appropriate storage per stage
3. **Clear separation**: StageService (config), RegistryService (validation + storage routing + workflow), ScopeService (orchestration)
4. **Flexible workflows**: Each registry defines its own transitions via `workflow.transitions` configuration
5. **Maximum stage reuse**: Same StageService instances shared across registries with different workflows
6. **Cost optimization**: Use cheaper storage (MongoDB, File) for transient stages, premium storage (S3, Maven) for permanent stages
7. **Flexible configuration**: Via OSGi target filters
8. **Backward compatible**: EObjectWorkflowService remains as facade
