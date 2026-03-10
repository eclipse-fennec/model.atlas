# Fennec Model Atlas REST Application

## Overview

The **Fennec Model Atlas REST Application** provides a comprehensive RESTful HTTP API for managing EMF (Eclipse Modeling Framework) schemas in a multi-tenant, scope-based environment with workflow stage management.

This application enables dynamic schema lifecycle management with:
- **Multi-tenant isolation** via configurable scopes
- **Stage-based workflows** (draft, review, release, etc.)
- **Hierarchical visibility** for shared schemas across organizational boundaries
- **Content negotiation** supporting multiple formats (JSON, XML, Ecore, JSON Schema)
- **OpenAPI 3.0 documentation** for automatic API client generation

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│              Model Atlas REST Application                           │
│           (Jakarta RS / JAX-RS Whiteboard)                          │
└──────┬─────────────────────────┬────────────────────────┬───────────┘
       │                         │                        │
  ┌────▼────────────┐    ┌───────▼────────────┐   ┌──────▼──────────────┐
  │ ScopesResource  │    │ SchemaPackages     │   │ ObjectRegistry      │
  │                 │    │ Resource           │   │ Resource            │
  └────┬────────────┘    └───────┬────────────┘   └──────┬──────────────┘
       │                         │                       │
       │         ┌───────────────┴───────────────────────┘
       │         │
  ┌────▼─────────▼────────────┐
  │  ScopeServiceCollector    │
  └────────────┬──────────────┘
               │
  ┌────────────▼──────────────┐
  │       ScopeService        │
  └────────────┬──────────────┘
               │
     ┌─────────┼─────────┐
     │         │         │
┌────▼────┐ ┌──▼───┐ ┌───▼───┐
│Registry │ │Stage │ │Storage│
│ Service │ │Service│ │Service│
└─────────┘ └──────┘ └───────┘
```

## API Resources

The REST application exposes the following resource endpoints:

### 1. Scopes API
**Base Path**: `/scopes`

Provides scope discovery and configuration metadata retrieval.

**Documentation**: [README-Scopes.md](README-Scopes.md)

**Key Operations**:
- List all configured scopes
- Get specific scope metadata
- Discover hierarchical relationships
- View stage configurations

**Use Cases**:
- Multi-tenant workspace discovery
- Understanding organizational hierarchy
- Validating workflow stages
- Building scope selectors in UIs

---

### 2. Schema Packages API
**Base Path**: `/{scopeName}/schema`

Provides full CRUD operations for EMF EPackages (schemas) with stage-based lifecycle management.

**Documentation**: [README-SchemaPackages.md](README-SchemaPackages.md)

**Key Operations**:
- Create schemas in specific stages
- Update and delete schemas
- Retrieve schemas in multiple formats
- Transition schemas between stages
- List schemas with filtering (by nsUri or name)

**Use Cases**:
- Schema development and publishing
- Multi-stage approval workflows
- Cross-scope schema sharing
- Schema versioning and lifecycle management

---

### 3. Object Storage API
**Base Path**: `/{scopeName}/registries/{registryName}`

Provides full CRUD operations for storage objects with schema validation and stage-based lifecycle management.

**Documentation**: [README-ObjectStorage.md](README-ObjectStorage.md)

**Key Operations**:
- Create objects in specific stages with schema validation
- Update and delete objects
- Retrieve objects in multiple formats
- Transition objects between stages
- List objects with filtering (by objectId or name)

**Use Cases**:
- Configuration management
- Data model storage
- Multi-stage approval workflows
- Cross-scope object sharing
- Schema-validated object storage

---

## API Specification

The complete Model Atlas API specification provides detailed information about:
- API design principles and conventions
- Hierarchical visibility rules
- Multi-tenancy and isolation
- Stage-based lifecycle workflows
- URL encoding and content negotiation
- Error handling and HTTP status codes

**Documentation**: [Model Atlas API Specification.md](Model%20Atlas%20API%20Specification.md)

---

## Quick Start

### Accessing the API

The REST API is available at the configured base URL (default: `http://localhost:8185/rest`)

```bash
# Discover available scopes
curl http://localhost:8185/rest/scopes

# List schemas in a scope's draft stage
curl http://localhost:8185/rest/my-tenant/schema/stages/draft

# Get OpenAPI specification
curl http://localhost:8185/rest/openapi.json
```

### OpenAPI Documentation

Interactive API documentation is available via OpenAPI/Swagger:

- **OpenAPI Specification (JSON)**: `/openapi.json`
- **OpenAPI Specification (YAML)**: `/openapi.yaml`
- **Swagger UI** (if configured): `/swagger-ui/`

### Content Types

The API supports multiple content types for schema serialization:

**Request/Response Formats**:
- `application/json` - EMF JSON format
- `application/xml` - Generic XML format
- `application/ecore+xml` - Ecore-specific XML
- `application/schema+json` - JSON Schema format (response only)
- `application/xmi` - XMI format
- `application/uml` - UML format

Specify format using HTTP headers:
```bash
# Request JSON format
curl -H "Accept: application/json" ...

# Send Ecore XML
curl -H "Content-Type: application/ecore+xml" ...
```

---

## Key Concepts

### Scopes

**Scopes** are logical tenants or organizational units that provide:
- Multi-tenant isolation
- Hierarchical visibility (parent-child relationships)
- Independent workflow configurations
- Separate storage backends

Example hierarchy:
```
atlas (system schemas)
  ├─ global-corporate (company-wide)
  │    ├─ department-a
  │    └─ department-b
  └─ partner-external
```

**Read more**: [README-Scopes.md](README-Scopes.md)

---

### Workflow Stages

**Stages** represent lifecycle states within a scope:
- **Draft**: Work in progress
- **Review**: Under review/approval
- **Approved**: Approved for release
- **Release**: Published and available to child scopes

Stages are fully configurable per scope:
```json
{
  "stages": ["draft", "review", "approved", "release"],
  "finalStage": "release",
  "writableStages": ["draft", "review", "approved"]
}
```

**Read more**: [README-SchemaPackages.md](README-SchemaPackages.md)

---

### Hierarchical Visibility

Child scopes can access schemas from parent scopes' final stages:

**Write-Time** (Creating schemas):
- nsUri must be unique across visibility chain
- Checks local scope (all stages) + parent final stages

**Read-Time** (Retrieving schemas):
- Searches local scope first
- Falls back to parent final stages
- Marks parent schemas as read-only

**Read more**: [Model Atlas API Specification.md](Model%20Atlas%20API%20Specification.md)

---

## Common Workflows

### 1. Creating and Publishing a Schema

```bash
# 1. Create schema in draft stage
curl -X POST "http://localhost:8185/rest/my-tenant/schema/stages/draft?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1&name=Billing&version=1.0.0" \
  -H "Content-Type: application/json" \
  -d @billing-schema.json

# 2. Update if needed
curl -X PUT "http://localhost:8185/rest/my-tenant/schema/stages/draft/content?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1" \
  -H "Content-Type: application/json" \
  -d @billing-schema-updated.json

# 3. Transition to review
curl -X POST "http://localhost:8185/rest/my-tenant/schema/stages/draft/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{"objectId": "http://example.com/schemas/billing/v1", "targetStage": "review"}'

# 4. Transition to approved
curl -X POST "http://localhost:8185/rest/my-tenant/schema/stages/review/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{"objectId": "http://example.com/schemas/billing/v1", "targetStage": "approved"}'

# 5. Transition to release (final stage)
curl -X POST "http://localhost:8185/rest/my-tenant/schema/stages/approved/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{"objectId": "http://example.com/schemas/billing/v1", "targetStage": "release"}'

# 6. Verify it's published
curl "http://localhost:8185/rest/my-tenant/schema"
```

---

### 2. Discovering Scopes and Schemas

```bash
# List all scopes
curl http://localhost:8185/rest/scopes

# Get scope configuration
curl http://localhost:8185/rest/scopes/my-tenant

# List all released schemas
curl http://localhost:8185/rest/my-tenant/schema

# Filter by name (wildcard)
curl "http://localhost:8185/rest/my-tenant/schema/stages/draft?name=Billing*"

# Get specific schema
curl "http://localhost:8185/rest/my-tenant/schema/stages/draft?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1"
```

---

### 3. Retrieving Schema Content in Different Formats

```bash
# Get as JSON
curl -H "Accept: application/json" \
  "http://localhost:8185/rest/my-tenant/schema/stages/draft/content?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1"

# Get as Ecore XML
curl -H "Accept: application/ecore+xml" \
  "http://localhost:8185/rest/my-tenant/schema/stages/draft/content?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1"

# Get as JSON Schema
curl -H "Accept: application/schema+json" \
  "http://localhost:8185/rest/my-tenant/schema/stages/draft/content?nsUri=http%3A%2F%2Fexample.com%2Fschemas%2Fbilling%2Fv1"
```

---

## Configuration

### OSGi Configuration

The REST application requires the following OSGi services to be configured:

#### Workflow Services (Scopes)
```properties
# File: ScopeService~my-tenant.cfg
scope=my-tenant
description=Tenant workspace
parent.scope=global
stages=["draft", "review", "release"]
writable.stages=["draft", "review"]
final.stage=release
```

#### Storage Services
```properties
# File: FileObjectStorage~my-tenant-draft.cfg
workspace.folder=/data/my-tenant/draft
storage.scope=my-tenant
storage.role=draft
```

#### Registry Service
```properties
# File: LuceneEObjectRegistryService.cfg
registry.workspace.folder=/data/registry
registry.type=shared
```

**Read more**: [README-SchemaPackages.md - Configuration](README-SchemaPackages.md#configuration-requirements)

---

## Error Handling

### Global Exception Mapper

All unhandled exceptions are caught by the `ModelAtlasExceptionMapper`, which ensures that no internal implementation details, stack traces, or raw exception messages are leaked to API consumers (TR-03187 W-19 conformance).

Error responses use a structured JSON format:
```json
{
  "message": "An internal server error occurred",
  "code": "500",
  "timestamp": "2026-03-10T12:00:00.000+00:00"
}
```

- **Client errors (4xx)**: The original error message is preserved (e.g. "Scope [foo] not found.").
- **Server errors (5xx)**: A generic `"An internal server error occurred"` message is returned. The full exception is logged server-side at `SEVERE` level.

### Debug Stack Traces

For troubleshooting in non-production environments, stack traces can be included in error responses by setting the `MODELATLAS_DEBUG_STACKTRACE` environment variable:

```bash
# Include full stack traces in HTTP error responses
export MODELATLAS_DEBUG_STACKTRACE=true
```

**Docker Compose example:**
```yaml
services:
  modelatlas:
    image: modelatlas:latest
    environment:
      - MODELATLAS_DEBUG_STACKTRACE=true
    ports:
      - "8185:8185"
```

**Docker run example:**
```bash
docker run -e MODELATLAS_DEBUG_STACKTRACE=true -p 8185:8185 modelatlas:latest
```

> **Warning**: Never enable `MODELATLAS_DEBUG_STACKTRACE` in production. Stack traces can expose internal class names, library versions, and file paths.

### Common HTTP Status Codes

| Code | Meaning | Common Causes |
|------|---------|---------------|
| 200 OK | Success | Request completed successfully |
| 201 Created | Resource created | Schema created successfully |
| 204 No Content | Success, no content | Schema deleted, or no results found |
| 400 Bad Request | Invalid request | Invalid parameters, malformed data |
| 403 Forbidden | Operation not allowed | Stage is read-only, or schema from parent scope |
| 404 Not Found | Resource not found | Scope, stage, or schema doesn't exist |
| 409 Conflict | Resource already exists | nsUri already exists in visibility chain |
| 415 Unsupported Media Type | Invalid Content-Type | Unsupported format in request |
| 500 Internal Server Error | Server error | Unexpected errors |

**Read more**: [README-SchemaPackages.md - Error Handling](README-SchemaPackages.md#error-handling)

---

## Testing

Comprehensive tests are available in the test bundles:

### Integration Tests
- **SchemaPackagesResourceTest.java** - REST API integration tests
- **ScopeAwareWorkflowServiceTest.java** - Workflow service operations
- **LuceneRegistryServiceTest.java** - Lucene-backed registry tests
- **BasicEObjectRegistryServiceTest.java** - In-memory registry tests

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew :org.eclipse.fennec.model.atlas.rest.tests:test --tests "*.SchemaPackagesResourceTest"

# Run OSGi integration tests
./gradlew testOSGi
```

---

## Security Considerations

### Multi-Tenancy Isolation
- Each scope has complete isolation at the workflow service level
- Cross-scope access only via explicit parent-child relationships
- No direct access to other tenants' data

### Read-Only Enforcement
- Parent schemas cannot be modified from child scopes
- Read-only stages prevent accidental modifications
- Both enforced at API and workflow service levels

### Input Validation
- All nsUri parameters are validated and encoded
- Stage names validated against configured stages
- Scope names validated via ScopeServiceCollector

**Read more**: [README-SchemaPackages.md - Security](README-SchemaPackages.md#security-considerations)

---

## Technology Stack

- **JAX-RS / Jakarta RS 3.1** - REST API implementation
- **OSGi R8 / R9** - Modular runtime with dynamic services
- **Eclipse EMF 2.29+** - Modeling framework
- **Apache Lucene** - Full-text search and indexing
- **Swagger OpenAPI 3.0** - API documentation
- **Jackson 2.19** - JSON serialization
- **Fennec Codec** - Multi-format serialization (JSON, XML, XLSX, etc.)

---

## Building and Running

### Build from Source
```bash
# Full build
./gradlew build

# Build without tests
./gradlew assemble

# Release (creates OSGi bundles)
./gradlew release
```

### Runtime Export
```bash
# Resolve runtime dependencies
./gradlew org.eclipse.fennec.model.atlas.runtime:resolve.modelatlas.runtime_base

# Export runtime
./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker

# Prepare Docker images
./gradlew prepareDocker
```

**Read more**: [CLAUDE.md](../CLAUDE.md) - Complete build instructions

---

## Related Documentation

- **[README-Scopes.md](README-Scopes.md)** - Scopes API documentation
- **[README-SchemaPackages.md](README-SchemaPackages.md)** - Schema Packages API documentation
- **[README-ObjectStorage.md](README-ObjectStorage.md)** - Object Storage API documentation
- **[Model Atlas API Specification.md](Model%20Atlas%20API%20Specification.md)** - Complete API specification
- **[Model Atlas Object API Specification.md](Model%20Atlas%20Object%20API%20Specification.md)** - Object Storage API specification
- **[ScopeService README](../org.eclipse.fennec.model.atlas.workflow/README.md)** - Scope service details
- **[CLAUDE.md](../CLAUDE.md)** - Project overview and development guide

---

## Support and Contributing

### Issues and Bug Reports
Please report issues at: https://github.com/anthropics/fennec-model-atlas/issues

### Contributing
Contributions are welcome! Please follow the Eclipse contribution guidelines.

---

## License

Eclipse Public License 2.0 (EPL-2.0)

Copyright (c) 2012 - 2026 Data In Motion and others.

All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0/
