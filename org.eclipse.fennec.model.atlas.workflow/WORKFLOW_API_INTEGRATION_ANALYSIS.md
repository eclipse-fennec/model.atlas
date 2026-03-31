# Workflow Service Integration with Model Atlas API

## Current State Analysis

### Existing EObjectWorkflowService

The `EObjectWorkflowService` provides a generic workflow system for managing EObjects through lifecycle states:

#### Architecture
```
┌─────────────────────────────────────┐
│  EObjectWorkflowService<T>          │
│  - Generic EObject workflow         │
└────────────┬────────────────────────┘
             │
             ├─> Draft Storage (role=draft)
             ├─> Release Storage (role=release)
             └─> EObjectRegistryService
```

#### Key Features
1. **Storage Backends**: Separate configurable storage for different roles
   - Draft storage: `(storage.role=draft)`
   - Release storage: `(storage.role=release)`

2. **Workflow Operations**:
   - `uploadDraft(object, metadata)` → Promise\<String\>
   - `getDraft(objectId)` → ObjectMetadata
   - `getDraftContent(objectId)` → T
   - `updateDraft(objectId, object)` → Promise\<Void\>
   - `deleteDraft(objectId)` → Promise\<Boolean\>
   - `approveObject(objectId, user, reason)` → ObjectMetadata
   - `rejectObject(objectId, user, reason)` → ObjectMetadata
   - `releaseObject(objectId, notes, checkCompliance)` → ObjectMetadata
   - `listDraftObjects()` → List\<ObjectMetadata\>
   - `listApprovedObjects()` → List\<ObjectMetadata\>
   - `listRejectedObjects()` → List\<ObjectMetadata\>
   - `listReleasedObjects()` → List\<ObjectMetadata\>

3. **Object Identification**: Uses `objectId` (String)

4. **Status Tracking**: Uses `ObjectStatus` enum
   - DRAFT
   - APPROVED
   - REJECTED
   - DEPLOYED/RELEASED

5. **Async Operations**: Uses OSGi Promises

---

## Model Atlas API Specification

### New Concepts

1. **Scopes**: Hierarchical tenant/partition system
   - Each scope has a parent (default: "atlas")
   - Scopes provide isolation and visibility boundaries
   - Example: `my-tenant` → `global-corporate` → `atlas`

2. **Stages**: Lifecycle states within a scope
   - Draft → Review → Released (configurable)
   - Stage transitions have validation rules
   - Different permissions per stage (e.g., Released is read-only)

3. **nsUri as Primary Key**: EPackage namespace URI
   - All lookups/operations use `nsUri` instead of arbitrary ID
   - Uniqueness enforced across scope hierarchy

4. **Hierarchical Visibility**:
   - Scopes can see their own packages (all stages)
   - Scopes can see parent scope's Released packages
   - Read-only flag when returning parent packages

5. **Content Negotiation**: Multiple formats
   - application/ecore+xml
   - application/schema+json
   - application/json
   - application/xml

---

## Integration Strategy

### Option 1: Extend Existing Workflow Service (Recommended)

**Add scope awareness to EObjectWorkflowService**

#### Changes Required:

1. **Scope Integration**
   ```java
   @Component(configurationPid = "EObjectWorkflowService")
   public class EObjectWorkflowServiceImpl<T> {

       @Reference
       private ScopeServiceCollector scopeCollector;

       private String scopeName; // from config

       // Operations now scope-aware
       public Promise<String> uploadDraft(String nsUri, T object, ObjectMetadata metadata) {
           // Validate nsUri uniqueness within scope hierarchy
           // Store with scope prefix: {scopeName}:{stage}:{nsUri}
       }
   }
   ```

2. **Stage-based Storage Selection**
   ```java
   // Instead of hard-coded draft/release roles
   @Reference(target = "(storage.scope={scopeName})")
   private Map<String, EObjectStorageService<T>> storageByStage;

   // Dynamic lookup based on stage
   private EObjectStorageService<T> getStorageForStage(String stage) {
       return storageByStage.get(stage);
   }
   ```

3. **nsUri-based Operations**
   ```java
   // Replace objectId with nsUri + scope context
   public ObjectMetadata getDraft(String nsUri) {
       String objectId = buildObjectId(scopeName, "Draft", nsUri);
       return draftStorage.retrieveMetadata(objectId).getValue();
   }

   private String buildObjectId(String scope, String stage, String nsUri) {
       return scope + ":" + stage + ":" + nsUri;
   }
   ```

4. **Hierarchical Visibility**
   ```java
   public ObjectMetadata findPackageInHierarchy(String nsUri, String stage) {
       // 1. Check current scope
       ObjectMetadata local = findInScope(scopeName, stage, nsUri);
       if (local != null) return local;

       // 2. Check parent scopes (Released only)
       Scope scope = scopeCollector.getScopeByName(scopeName);
       while (scope.getParentScope() != null) {
           ObjectMetadata parent = findInScope(scope.getParentScope(), "Released", nsUri);
           if (parent != null) {
               parent.setReadOnly(true);
               metadata.setSourceScope(scope.getParentScope());
               return parent;
           }
           scope = scopeCollector.getScopeByName(scope.getParentScope());
       }

       return null;
   }
   ```

5. **Stage Transition Logic**
   ```java
   public ObjectMetadata transitionPackage(String nsUri, String fromStage, String toStage) {
       // Validate transition is allowed
       validateTransition(fromStage, toStage);

       // Get from source stage storage
       String sourceId = buildObjectId(scopeName, fromStage, nsUri);
       T object = getStorageForStage(fromStage).retrieveObject(sourceId).getValue();
       ObjectMetadata metadata = getStorageForStage(fromStage).retrieveMetadata(sourceId).getValue();

       // Store in target stage storage
       String targetId = buildObjectId(scopeName, toStage, nsUri);
       getStorageForStage(toStage).storeObject(targetId, object, metadata).getValue();

       // Remove from source (if configured)
       if (config.deleteAfterTransition()) {
           getStorageForStage(fromStage).deleteObject(sourceId).getValue();
       }

       return metadata;
   }
   ```

#### New Configuration
```java
@ObjectClassDefinition
public @interface WorkflowServiceConfig {
    String scope_name();
    String[] stages() default {"Draft", "Review", "Released"};
    String[] allowed_transitions() default {"Draft->Review", "Review->Released", "Review->Draft"};
    boolean delete_after_transition() default false;
}
```

---

### Option 2: New SchemaPackageService (Clean Slate)

**Create a dedicated service for the Model Atlas API**

```java
@Component(configurationPid = "SchemaPackageService")
public class SchemaPackageServiceImpl {

    @Reference
    private ScopeServiceCollector scopeCollector;

    @Reference
    private Map<String, EObjectStorageService<EPackage>> storageServices;

    public SchemaPackage createPackage(String scopeName, String stageName,
                                       String nsUri, EPackage ePackage) {
        // New implementation from scratch
    }

    public List<SchemaPackage> listPackages(String scopeName, String stageName) {
        // With hierarchical visibility
    }

    public SchemaPackage transitionPackage(String scopeName, String fromStage,
                                           String toStage, String nsUri) {
        // Stage-based transitions
    }
}
```

**Pros**: Clean API aligned with spec, no legacy constraints
**Cons**: Code duplication, two parallel systems

---

## Recommended Approach: Hybrid

1. **Keep EObjectWorkflowService** for generic EObject workflows
2. **Extend for EPackage-specific workflows** with scope/stage awareness
3. **Create EPackageWorkflowService** that wraps/extends EObjectWorkflowService:

```java
@Component
public class EPackageWorkflowService {

    @Reference
    private EObjectWorkflowService<EPackage> baseWorkflow;

    @Reference
    private ScopeServiceCollector scopeCollector;

    // Configuration per scope
    private String scopeName;
    private StageConfiguration stages;

    // API-aligned methods
    public SchemaPackage createPackage(String stageName, String nsUri,
                                       String name, String version,
                                       EPackage content) {
        // Validate nsUri uniqueness in hierarchy
        validateNsUriUniqueness(nsUri);

        // Create metadata
        ObjectMetadata metadata = createMetadata(nsUri, name, version);

        // Build scoped object ID
        String objectId = buildObjectId(scopeName, stageName, nsUri);

        // Delegate to base workflow
        return baseWorkflow.uploadDraft(objectId, content, metadata);
    }

    public List<SchemaPackage> listPackages(String stageName) {
        List<ObjectMetadata> local = baseWorkflow.listByStage(stageName);

        // Add parent scope packages if stage is "Released"
        if ("Released".equals(stageName)) {
            local.addAll(getParentScopePackages());
        }

        return toSchemaPackages(local);
    }

    public SchemaPackage transitionPackage(String fromStage, String toStage,
                                           String nsUri) {
        String objectId = buildObjectId(scopeName, fromStage, nsUri);

        // Map to workflow operations
        switch (toStage) {
            case "Review":
                return baseWorkflow.approveObject(objectId, "system", "Moved to review");
            case "Released":
                return baseWorkflow.releaseObject(objectId, "Released", false);
            default:
                // Custom transition logic
                return customTransition(objectId, fromStage, toStage);
        }
    }
}
```

---

## REST Resource Integration

Update `SchemaPackagesResource` to use the new service:

```java
@Path("/{scopeName}/schema")
public class SchemaPackagesResource {

    @Reference
    private Map<String, EPackageWorkflowService> workflowByScope;

    @POST
    @Path("/stages/{stageName}")
    public Response createPackage(
            @PathParam("scopeName") String scopeName,
            @PathParam("stageName") String stageName,
            @QueryParam("nsUri") String nsUri,
            @QueryParam("name") String name,
            @QueryParam("version") String version,
            EPackage ePackage) {

        EPackageWorkflowService workflow = workflowByScope.get(scopeName);
        if (workflow == null) {
            return Response.status(404).entity("Scope not found").build();
        }

        try {
            SchemaPackage pkg = workflow.createPackage(stageName, nsUri, name, version, ePackage);
            return Response.status(201)
                .header("Location", buildLocation(scopeName, stageName, nsUri))
                .entity(pkg)
                .build();
        } catch (DuplicateNsUriException e) {
            return Response.status(409).entity("nsUri already exists").build();
        }
    }
}
```

---

## Migration Path

### Phase 1: Core Extensions (Week 1-2)
- [ ] Add scope configuration to `WorkflowServiceConfig`
- [ ] Implement nsUri-based object ID generation
- [ ] Add hierarchical visibility to list operations
- [ ] Update storage service bindings for stage-based lookup

### Phase 2: Stage Transitions (Week 2-3)
- [ ] Implement `transitionPackage` method
- [ ] Add transition validation rules
- [ ] Configure stage-to-storage mappings
- [ ] Add stage metadata to ObjectMetadata

### Phase 3: REST API Integration (Week 3-4)
- [ ] Implement `SchemaPackagesResource` endpoints
- [ ] Add content negotiation for multiple formats
- [ ] Implement hierarchical package lookup
- [ ] Add uniqueness validation across scope hierarchy

### Phase 4: Testing & Documentation (Week 4-5)
- [ ] Integration tests for scope hierarchy
- [ ] Stage transition tests
- [ ] Performance tests for hierarchical queries
- [ ] API documentation and examples

---

## Open Questions

1. **Stage Configuration**: Should stages be configurable per scope or global?
2. **Storage Mapping**: One storage service per stage or dynamic routing?
3. **Transition Rules**: Hardcoded or configurable state machine?
4. **Backward Compatibility**: Support both objectId and nsUri simultaneously?
5. **Content Formats**: Should storage services handle format conversion or add a transformation layer?
6. **Cache Strategy**: How to cache hierarchical lookups efficiently?

---

## Benefits of Integration

✅ **Reuse existing infrastructure**: Storage, registry, metadata tracking
✅ **Leverage tested workflow logic**: Approval, rejection, release
✅ **Maintain async capabilities**: OSGi Promises for performance
✅ **Add scope isolation**: Multi-tenancy support
✅ **Flexible stage management**: Configurable lifecycle beyond draft/release
✅ **Standards alignment**: nsUri as primary key matches EPackage semantics
✅ **Hierarchical visibility**: Parent scope inheritance built-in

---

## Next Steps

1. Review this analysis with the team
2. Decide on Option 1 (extend) vs Option 2 (new service)
3. Define exact stage-to-status mapping
4. Design storage service configuration for multi-stage
5. Prototype nsUri-based hierarchical lookup
