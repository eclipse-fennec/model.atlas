# Apicurio Registry Storage

This module provides an Apicurio Registry-based storage implementation for EMF objects, using the [Apicurio Registry REST API v3](https://www.apicur.io/registry/docs/apicurio-registry/3.0.x/assets-attachments/registry-rest-api.htm).

## Overview

The `EObjectApicurioStorageService` stores EMF objects as artifacts in Apicurio Registry. Each object is stored with its metadata as separate artifacts, supporting versioning through Apicurio's native version management.

### Key Components

- **EObjectApicurioStorageService** - OSGi component providing the storage service
- **ApicurioStorageHelper** - Handles REST API communication with Apicurio Registry

## Configuration

Configure via OSGi ConfigAdmin with PID `ApicurioObjectStorage`:

| Property | Default | Description |
|----------|---------|-------------|
| `base_url` | `http://localhost:8080/apis/registry/v3/` | Base URL of Apicurio Registry REST API |
| `artifact_group_id` | `default` | Group ID under which artifacts are stored |
| `storage_role` | `draft` | Storage role; when set to "draft", versions are marked as drafts |

### Apicurio Environment Configuration

To enable artifact deletion, set this environment variable in Apicurio:
```
APICURIO_REST_DELETION_ARTIFACT_ENABLED=true
```

## Method Behaviors

### Loading Objects

#### `loadEObject(objectId)`

1. Searches for the artifact by `objectId` using `/search/artifacts`
2. If found, queries `/search/versions` sorted by version descending to get the **latest version**
3. Retrieves the `objectEClassURI` and `contentType` from version labels
4. Fetches the content from `/groups/{groupId}/artifacts/{artifactId}/versions/{version}/content`
5. Deserializes the content using the appropriate EClass

**Returns:** The deserialized EObject, or `null` if not found or version has no `objectEClassURI` label.

#### `loadMetadata(objectId)`

1. Builds metadata artifact ID: `{objectId}.metadata.xmi`
2. Queries `/search/versions` for the latest version of the metadata artifact
3. Retrieves content from the version endpoint
4. Deserializes as `ObjectMetadata`

**Returns:** The `ObjectMetadata` object, or `null` if not found.

### Saving Objects

#### `saveEObject(objectId, object, metadata)`

1. Serializes the EObject to a byte stream using the content type from metadata
2. Creates a `CreateArtifact` request with:
   - `artifactId`: the `objectId`
   - `artifactType`: derived from content type (JSON, XML, or XSD)
   - `name`: from `metadata.getObjectName()`
   - Labels containing metadata fields and `objectEClassURI`
3. POSTs to `/groups/{groupId}/artifacts?ifExists=CREATE_VERSION`
   - Creates new artifact if it doesn't exist
   - Creates new version if artifact already exists

**Note:** When using `ifExists=CREATE_VERSION`, ensure the version number in metadata is updated between saves, otherwise the request will fail with a 409.

#### `saveMetadata(objectId, metadata)`

1. Validates metadata (objectId consistency)
2. Serializes metadata as XMI
3. Creates artifact with ID `{objectId}.metadata.xmi`
4. POSTs with same `ifExists=CREATE_VERSION` behavior

### Artifact Labels

Both objects and metadata are stored with labels for retrieval:

- `objectEClassURI` - EMF EClass URI for deserialization
- `contentType` - MIME type of the content
- `objectType` - Type identifier
- `uploadUser`, `uploadTime` - Upload tracking
- `lastChangeUser`, `lastChangeTime` - Modification tracking
- `reviewUser`, `reviewTime`, `reviewReason` - Review workflow
- `contentHash` - Content integrity hash
- `generationTriggerFingerprint` - Generation tracking

### Deleting Objects

#### `deleteObject(objectId)`

Deletes both:
- The object artifact: `/groups/{groupId}/artifacts/{objectId}`
- The metadata artifact: `/groups/{groupId}/artifacts/{objectId}.metadata.xmi`

**Returns:** `true` if both deletions succeed, `false` otherwise.

### Listing Objects

#### `listObjectIds()`

1. Queries `/groups/{groupId}/artifacts` to list all artifacts
2. Filters out metadata artifacts (ending with `.metadata.xmi`)
3. Returns list of object IDs

**Note:** The search API has a `limit` parameter (default = 20). For large registries, you may need to handle pagination or configure a larger limit to retrieve all artifacts.

### Checking Existence

#### `objectExists(objectId)`

Checks if the metadata artifact exists by querying:
`/groups/{groupId}/artifacts/{objectId}.metadata.xmi`

**Returns:** `true` if metadata exists, `false` otherwise.

## Apicurio API Endpoints Used

| Operation | Endpoint | Method |
|-----------|----------|--------|
| Create/Update artifact | `/groups/{groupId}/artifacts?ifExists=CREATE_VERSION` | POST |
| Get artifact metadata | `/groups/{groupId}/artifacts/{artifactId}` | GET |
| Get version content | `/groups/{groupId}/artifacts/{artifactId}/versions/{version}/content` | GET |
| Delete artifact | `/groups/{groupId}/artifacts/{artifactId}` | DELETE |
| Search artifacts | `/search/artifacts?groupId={groupId}&artifactId={artifactId}` | GET |
| Search versions | `/search/versions?orderBy=version&order=desc&artifactId={artifactId}` | GET |

## Content Type Mapping

| Content Type | Apicurio Artifact Type |
|--------------|------------------------|
| `application/json`, `application/schema+json` | JSON |
| `application/xml`, `application/xmi` | XML |
| `application/xsd` | XSD |

## Draft Mode

When `storage_role` is set to `"draft"`, all created versions are marked with `isDraft=true`. This integrates with Apicurio's draft/publish workflow.

## Error Handling

- Returns `null` for load operations when artifacts are not found
- Logs errors at SEVERE level when:
  - Version search returns no results
  - `objectEClassURI` label is missing (required for deserialization)
  - Delete operations fail
- Throws `IllegalArgumentException` for unsupported content types

## Design Decisions

### Versioning Strategy

We use `ifExists=CREATE_VERSION` when creating artifacts. This means:
- First save creates a new artifact
- Subsequent saves create new versions of the same artifact
- Version history is preserved in Apicurio

### Content Retrieval

Since getting an artifact by ID only returns metadata (not content), we:
1. Search for versions of the artifact
2. Sort by version descending to get the latest
3. Fetch content from the version-specific endpoint

### Multiple Content Formats

An object can be stored in different formats (JSON, XMI, etc.) by specifying the content type in metadata properties. The metadata itself is always stored as XMI with artifact ID `{objectId}.metadata.xmi`.
