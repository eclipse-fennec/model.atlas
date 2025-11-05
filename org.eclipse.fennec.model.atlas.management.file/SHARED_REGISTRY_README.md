# Shared Lucene Registry Service

## Overview

The `SharedLuceneRegistryService` provides a centralized metadata index that can be used by multiple storage services (draft, approved, documentation) instead of each service maintaining its own registry.

## Benefits

### Resource Efficiency
- **Single Lucene Index**: One shared index instead of 3 separate indexes
- **Unified Search**: Query across all storage backends in one operation
- **Memory Optimization**: One IndexWriter and SearcherManager for entire system
- **Consistent Performance**: O(log n) queries across entire system

### Cross-Storage Capabilities
- **Unified Queries**: Search draft, approved, and documentation objects together
- **Storage Backend Tracking**: Know which storage owns each object
- **Cross-Storage Analytics**: Get statistics across all storage types
- **Workflow Visibility**: See object lifecycle across storage transitions

## Configuration

### OSGi Configuration Admin
```json
{
  "SharedLuceneRegistryService": {
    "registry_workspace_folder": "/opt/registry/shared-lucene",
    "enable_lucene_index": true,
    "storage_backend_tracking": true,
    "initial_index_capacity": 50000,
    "enable_debug_logging": false
  }
}
```

### Service Properties
- `service.ranking`: Can be set higher to prefer shared registry over storage-specific ones
- `registry.type=shared`: Identifies this as a shared registry
- `registry.backend=lucene`: Indicates Lucene-backed implementation

## Usage Examples

### Storage Service Integration
```java
@Component
public class MyWorkflowService {
    
    // Inject the shared registry instead of storage-specific ones
    @Reference(target = "(registry.type=shared)")
    private EObjectRegistryService<EObject> sharedRegistry;
    
    public void searchAcrossAllStorages() {
        // Find all draft objects across ALL storage backends
        List<ObjectMetadata> allDrafts = ((SharedLuceneRegistryService<?>) sharedRegistry)
            .findAllDrafts();
        
        // Find objects by storage backend
        List<ObjectMetadata> fileObjects = ((SharedLuceneRegistryService<?>) sharedRegistry)
            .findByStorageBackend("file");
        
        // Get distribution summary
        Map<String, Long> distribution = ((SharedLuceneRegistryService<?>) sharedRegistry)
            .getStorageDistribution();
    }
}
```

### Metadata Enhancement
```java
// Storage services should enhance metadata with backend information
StorageBackendIdentifier.enhanceWithStorageInfo(
    metadata, 
    "file",           // backend type
    "draft",          // storage role
    "instance-01"     // optional instance
);

// This enables cross-storage queries like:
sharedRegistry.findByStorageBackendAndRole("file", "draft");
```

### Advanced Queries
```java
// Cross-storage status queries
List<ObjectMetadata> allApproved = sharedRegistry.findByStatus(ObjectStatus.APPROVED);

// Backend-specific queries  
List<ObjectMetadata> documentationObjects = sharedRegistry.findAllDocumentation();

// Combined criteria
List<ObjectMetadata> fileDrafts = sharedRegistry.findByStorageBackendAndRole("file", "draft");
```

## Integration Path

### Phase 1: Coexistence (Current)
- SharedLuceneRegistryService deployed alongside existing registries
- Both approaches work in parallel
- No disruption to existing functionality

### Phase 2: Migration (Future)
- Storage services updated to inject shared registry
- Storage backend tracking added to metadata
- Gradual migration of services

### Phase 3: Consolidation (Future)
- Remove storage-specific registry creation
- All services use shared registry
- Simplified architecture with single index

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SharedLuceneRegistryService                      │
├─────────────────────────────────────────────────────────────────────┤
│  Single Lucene Index (/opt/registry/shared-lucene)                 │
│  ┌─────────────────┬─────────────────┬─────────────────────────────┐ │
│  │ Draft Objects   │ Approved Objects│ Documentation Objects       │ │
│  │ storage.role=   │ storage.role=   │ storage.role=documentation  │ │
│  │ draft           │ approved        │                             │ │
│  └─────────────────┴─────────────────┴─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
            │                    │                    │
            ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐
│Draft Storage    │  │Approved Storage │  │Documentation Storage        │
│Service          │  │Service          │  │Service                      │
└─────────────────┘  └─────────────────┘  └─────────────────────────────┘
```

## Performance Benefits

### Current (3 Separate Registries)
- 3x Lucene IndexWriters → High memory usage
- 3x SearcherManagers → Resource duplication  
- Fragmented searches → Multiple queries needed
- Inconsistent state → Potential sync issues

### With Shared Registry
- 1x Lucene IndexWriter → Reduced memory usage
- 1x SearcherManager → Efficient resource usage
- Unified searches → Single query for cross-storage
- Consistent state → Single source of truth

## Monitoring

### Registry Statistics
```java
Promise<Map<String, Object>> stats = sharedRegistry.getRegistryStatistics();

// Returns:
{
  "totalObjects": 15000,
  "statusDistribution": {"DRAFT": 5000, "APPROVED": 8000, "DEPLOYED": 2000},
  "storageBackendDistribution": {"file:draft": 5000, "file:approved": 7000, "minio:documentation": 3000},
  "luceneEnabled": true,
  "registryType": "shared"
}
```

### Debug Logging
Enable detailed logging for troubleshooting:
```json
{
  "enable_debug_logging": true
}
```

This creates a foundation for much more efficient metadata management across the entire Model Atlas Cloud system.