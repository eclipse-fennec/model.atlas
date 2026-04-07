# Fennec Model Atlas - User Guide

Fennec Model Atlas is a dynamic EMF model management system that provides a RESTful API for managing schemas and data objects with multi-tenant isolation, stage-based workflows, and hierarchical visibility.

## Table of Contents

- [Getting Started](#getting-started)
  - [Running with Docker](#running-with-docker)
  - [Building from Source](#building-from-source)
- [Core Concepts](#core-concepts)
  - [Scopes](#scopes)
  - [Registries](#registries)
  - [Workflow Stages](#workflow-stages)
  - [Hierarchical Visibility](#hierarchical-visibility)
- [REST API](#rest-api)
  - [Base URL and Swagger UI](#base-url-and-swagger-ui)
  - [Scopes API](#scopes-api)
  - [Schema Packages API](#schema-packages-api)
  - [Object Storage API](#object-storage-api)
  - [Model Converter API](#model-converter-api)
  - [Object Validation API](#object-validation-api)
  - [Data Generation API](#data-generation-api)
  - [Content Negotiation](#content-negotiation)
  - [Error Handling](#error-handling)
- [Workflows](#workflows)
  - [Publishing a Schema](#publishing-a-schema)
  - [Storing Objects with Schema Validation](#storing-objects-with-schema-validation)
- [Health Checks](#health-checks)
- [Configuration](#configuration)
  - [Scope Configuration](#scope-configuration)
  - [Registry Configuration](#registry-configuration)
  - [Storage Backend Configuration](#storage-backend-configuration)
- [Further Reading](#further-reading)

---

## Getting Started

### Running with Docker

Model Atlas is available as Docker images in two variants:

| Variant | Image Tag | Description |
|---------|-----------|-------------|
| **File** | `eclipsefennec/model.atlas:file-latest` | Local file-based storage, no external dependencies |
| **Apicurio** | `eclipsefennec/model.atlas:apicurio-latest` | Uses [Apicurio Registry](https://www.apicur.io/registry/) for versioned artifact storage |

Both are also available on GHCR as `ghcr.io/eclipse-fennec/model.atlas`.

#### File variant (standalone)

```bash
docker run -d -p 8080:8080 eclipsefennec/model.atlas:file-latest
```

#### Apicurio variant (with Docker Compose)

The Apicurio variant requires a running Apicurio Registry. Use the provided compose file:

```bash
docker compose -f docker/dockercompose/docker-compose-apicurio.yml up -d
```

This starts the full stack:

| Service | URL |
|---------|-----|
| Model Atlas | http://localhost:8080 |
| Apicurio Registry API | http://localhost:8081 |
| Apicurio Registry UI | http://localhost:8888 |

#### Environment Variables

| Variable | Default | Variant | Description |
|----------|---------|---------|-------------|
| `STORAGE_ROOT` | `/tmp/mac` | File | Root directory for file-based storage |
| `APICURIO_HOST` | `localhost` | Apicurio | Hostname of the Apicurio Registry |
| `APICURIO_PORT` | `8081` | Apicurio | Port of the Apicurio Registry |

> For more details on Docker setup, see the variant-specific documentation:
> - [Docker Apicurio variant](../docker/modelatlas_apicurio/README.md)
> - [Docker File variant](../docker/modelatlas_file/README.md)

### Building from Source

```bash
# Build the project (skip tests for faster builds)
./gradlew build -x test -x testOSGi

# Export the runtime JARs for a variant
./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_file
# or
./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_apicurio

# Prepare and build Docker image
./gradlew docker:modelatlas_file:prepareDocker
docker build -t eclipsefennec/model.atlas:file-snapshot docker/modelatlas_file/
```

---

## Core Concepts

### Scopes

**Scopes** are logical tenants or organizational units. Each scope provides:

- **Isolation**: Schemas and objects in one scope are not visible to sibling scopes
- **Hierarchy**: Scopes can have parent-child relationships for sharing
- **Independent Workflows**: Each scope has its own stage configurations per registry

Example hierarchy:

```
atlas (system-level, read-only base schemas)
  +-- global-corporate (company-wide shared schemas)
  |     +-- department-a
  |     |     +-- team-alpha
  |     +-- department-b
  +-- partner-external
```

### Registries

A **Registry** is a named collection within a scope that stores a specific type of object. Each scope can have multiple registries with independent workflow configurations.

Common registries:
- `schema` - Stores EMF EPackage schema definitions (used by the Schema Packages API)
- `configurations` - Stores configuration objects
- Custom registries for domain-specific data

The registry name in the configuration directly determines the **URL path segment** under which it is accessible in the REST API. For example, a registry configured with `registry.name=products` is reachable at `/{scopeName}/registries/products/...`.

### Workflow Stages

Each registry defines its own **stages** that control the lifecycle of objects:

```
draft  -->  review  -->  approved  -->  release
 (writable)  (writable)   (writable)    (read-only, final)
```

Key properties of stages:
- **writable**: Whether objects can be created, updated, or deleted in this stage
- **final**: Whether this stage is the "released" stage (visible to child scopes)

Stages and allowed transitions are fully configurable per registry. Common patterns:

| Pattern | Stages | Use Case |
|---------|--------|----------|
| Simple | draft, release | Quick publish without review |
| Standard | draft, approved, release | Basic approval workflow |
| Enterprise | draft, review, approved, staging, production | Multi-gate release process |

### Hierarchical Visibility

Child scopes can see objects from parent scopes' **final stages**:

**Visibility rules:**
- A child scope sees its own objects in all stages, plus parent objects in the final stage
- Parent objects appear as **read-only** in child scopes (cannot be modified or deleted)
- Parents cannot see children's objects; siblings cannot see each other's objects

**Write-time uniqueness** (for schemas):
- When creating a schema, the `nsUri` must be unique across the entire visibility chain (local scope all stages + all ancestor final stages)

**Read-time fallback** (for lookups by ID):
- If an object is not found in the local scope/stage, the system searches parent final stages recursively

> For the full specification of visibility rules, see the [Model Atlas API Specification](../org.eclipse.fennec.model.atlas.rest.application/Model%20Atlas%20API%20Specification.md).

---

## REST API

### Base URL and Swagger UI

| Endpoint | Description |
|----------|-------------|
| `/rest/` | REST API base path |
| `/rest/openapi.json` | OpenAPI 3.0 specification (JSON) |
| `/rest/openapi.yaml` | OpenAPI 3.0 specification (YAML) |
| `/swagger-api/` | Interactive Swagger UI |

### Scopes API

**Base path**: `/scopes`

Discover available scopes and their configuration.

```bash
# List all scopes
curl http://localhost:8080/rest/scopes

# Get metadata for a specific scope (stages, registries, parent)
curl http://localhost:8080/rest/scopes/my-tenant
```

The response includes the scope's registries, each with their stage definitions and allowed transitions:

```json
{
  "name": "my-tenant",
  "description": "Tenant workspace",
  "parentScope": "global-corporate",
  "registries": [
    {
      "name": "schema",
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

> Full endpoint documentation: [README-Scopes.md](../org.eclipse.fennec.model.atlas.rest.application/README-Scopes.md)

### Schema Packages API

**Base path**: `/{scopeName}/schema`

Manage EMF EPackage schemas with full CRUD and stage transitions. The Schema API uses the fixed registry name `schema`.

```bash
# List all released schemas (includes parent scope schemas)
curl http://localhost:8080/rest/my-tenant/schema

# List all schemas across all stages (includes parent scope schemas)
curl http://localhost:8080/rest/my-tenant/schema/all

# List schemas in a specific stage
curl http://localhost:8080/rest/my-tenant/schema/stages/draft

# Find schema by nsUri (hierarchical lookup)
curl "http://localhost:8080/rest/my-tenant/schema/stages/draft?nsUri=http%3A%2F%2Fexample.com%2Fbilling%2Fv1"

# Filter by name with wildcard (scope-local only)
curl "http://localhost:8080/rest/my-tenant/schema/stages/draft?name=Billing*"

# Create a schema in draft
curl -X POST "http://localhost:8080/rest/my-tenant/schema/stages/draft?nsUri=http%3A%2F%2Fexample.com%2Fbilling%2Fv1&name=Billing&version=1.0.0" \
  -H "Content-Type: application/json" \
  -d @billing-schema.json

# Get schema content in a specific format
curl -H "Accept: application/ecore+xml" \
  "http://localhost:8080/rest/my-tenant/schema/stages/draft/content?nsUri=http%3A%2F%2Fexample.com%2Fbilling%2Fv1"

# Update schema content
curl -X PUT "http://localhost:8080/rest/my-tenant/schema/stages/draft/content?nsUri=http%3A%2F%2Fexample.com%2Fbilling%2Fv1" \
  -H "Content-Type: application/json" \
  -d @billing-schema-updated.json

# Delete a schema
curl -X DELETE "http://localhost:8080/rest/my-tenant/schema/stages/draft?nsUri=http%3A%2F%2Fexample.com%2Fbilling%2Fv1"

# Transition a schema to the next stage
curl -X POST "http://localhost:8080/rest/my-tenant/schema/stages/draft/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{"objectId": "http://example.com/billing/v1", "targetStage": "release"}'
```

> Full endpoint documentation: [README-SchemaPackages.md](../org.eclipse.fennec.model.atlas.rest.application/README-SchemaPackages.md)

### Object Storage API

**Base path**: `/{scopeName}/registries/{registryName}`

Manage arbitrary EObjects in named registries with schema validation.

```bash
# List all released objects in a registry
curl http://localhost:8080/rest/my-tenant/registries/configurations

# List all objects across all stages in a registry (includes parent scope objects)
curl http://localhost:8080/rest/my-tenant/registries/configurations/all

# List objects in a specific stage
curl http://localhost:8080/rest/my-tenant/registries/configurations/stages/draft

# Find by objectId (hierarchical lookup)
curl "http://localhost:8080/rest/my-tenant/registries/configurations/stages/draft?objectId=app-settings"

# Filter by name with wildcard
curl "http://localhost:8080/rest/my-tenant/registries/configurations/stages/draft?name=Production*"

# Create an object (objectId in path)
curl -X POST "http://localhost:8080/rest/my-tenant/registries/configurations/stages/draft/app-settings?name=App%20Settings&version=1.0.0" \
  -H "Content-Type: application/json" \
  -d '{"logLevel": "INFO", "maxConnections": 100}'

# Get object content
curl "http://localhost:8080/rest/my-tenant/registries/configurations/stages/draft/content?objectId=app-settings"

# Update object content
curl -X PUT "http://localhost:8080/rest/my-tenant/registries/configurations/stages/draft/content?objectId=app-settings" \
  -H "Content-Type: application/json" \
  -d '{"logLevel": "WARN", "maxConnections": 200}'

# Delete an object
curl -X DELETE "http://localhost:8080/rest/my-tenant/registries/configurations/stages/draft?objectId=app-settings"

# Transition to next stage
curl -X POST "http://localhost:8080/rest/my-tenant/registries/configurations/stages/draft/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{"objectId": "app-settings", "targetStage": "release"}'
```

Objects are validated against the registry's configured schema (`root.eclass.uri`). If the object's EClass is not compatible, the request is rejected with `400 Bad Request`.

> Full endpoint documentation: [README-ObjectStorage.md](../org.eclipse.fennec.model.atlas.rest.application/README-ObjectStorage.md)

### Model Converter API

Convert EMF models between different serialization formats.

> Full endpoint documentation: [README-ModelConverter.md](../org.eclipse.fennec.model.atlas.rest.application/README-ModelConverter.md)

### Object Validation API

**Base path**: `/validate`

Validate an EObject instance against its schema constraints, including any OCL constraints defined in the model. The endpoint runs EMF's `Diagnostician` on the submitted object and returns a structured diagnostic report.

```bash
# Validate a Person object (JSON request, JSON response)
curl -X POST http://localhost:8080/rest/validate \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "eClass": "https://dg.de/1.0#//Person",
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane.doe@example.com",
    "phone": "0301234567",
    "jobTitle": "Engineer"
  }'

# Validate using XMI format
curl -X POST http://localhost:8080/rest/validate \
  -H "Content-Type: application/xmi" \
  -H "Accept: application/xmi" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<dge:Person xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:dge="https://dg.de/1.0"
    firstName="Jane"
    lastName="Doe"
    phone="0301234567"/>'

# Override response format via query parameter
curl -X POST "http://localhost:8080/rest/validate?mediaType=application/json" \
  -H "Content-Type: application/xmi" \
  -d @person.xmi
```

**Response**

The endpoint always returns `200 OK` with a `Diagnostic` object describing the validation result. The diagnostic has a tree structure: a root entry with a `type` summarizing the overall result, and `children` containing one entry per constraint violation.

A valid object returns a diagnostic with type `OK` and no children:

```json
{
  "type": "OK",
  "message": "Diagnosis of Person",
  "children": []
}
```

An invalid object (e.g., a `phone` value that violates the `ValidPhoneNumber` OCL constraint `self.phone.matches('^\\d{10}$')`) returns child diagnostics with type `ERROR`:

```json
{
  "type": "ERROR",
  "message": "Diagnosis of Person",
  "children": [
    {
      "type": "ERROR",
      "message": "The 'ValidPhoneNumber' constraint is violated",
      "source": "org.eclipse.emf.ecore",
      "children": []
    }
  ]
}
```

**Diagnostic types:**

| Type | Meaning |
|------|---------|
| `OK` | Validation passed with no issues |
| `INFO` | Informational message |
| `WARNING` | Non-critical issue detected |
| `ERROR` | Constraint violation or structural error |
| `CANCEL` | Validation was cancelled |

**Error responses:**

| Code | Condition |
|------|-----------|
| 200 | Validation was performed (check diagnostic `type` for the result) |
| 415 | Unsupported `mediaType` query parameter value |
| 500 | Internal server error (e.g., request body could not be deserialized) |

### Data Generation API

**Base path**: `/datagen`

Generate fake test data for any registered EMF model. Send a `DataGenConfig` as XMI and receive a `DataGenResult` containing the generated EObject instances. The response format is controlled by the `Accept` header (`application/xmi` or `application/json`).

```bash
# Generate 5 Person instances with German locale (JSON response)
curl -X POST http://localhost:8086/atlas/rest/datagen \
  -H "Content-Type: application/xmi" \
  -H "Accept: application/json" \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<datagen:DataGenConfig xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:datagen="http://www.gme.org/datagen/1.0"
    name="person-gen"
    locale="de"
    seed="42">
  <classConfigs contextClass="Person" instanceCount="5">
    <attributeGens featureName="firstName" generatorKey="faker.person.firstName"/>
    <attributeGens featureName="lastName" generatorKey="faker.person.lastName"/>
  </classConfigs>
</datagen:DataGenConfig>'
```

The referenced EClasses (e.g. `Person`) must be registered in the runtime via loaded EPackages. If a class is not found, the endpoint returns `400 Bad Request` with the missing class names.

> Full endpoint documentation and more examples: [DataGen REST README](../org.eclipse.fennec.model.atlas.datagen.rest/README.md)
> Service and configuration model reference: [DataGen Service README](../org.eclipse.fennec.model.atlas.datagen/README.md)

### Content Negotiation

Use the `Content-Type` and `Accept` headers to control serialization formats.

**Supported formats:**

| Media Type | Description | Request | Response |
|------------|-------------|---------|----------|
| `application/json` | EMF JSON format | yes | yes |
| `application/xml` | Generic XML | yes | yes |
| `application/ecore+xml` | Ecore-specific XML | yes | yes |
| `application/schema+json` | JSON Schema | - | yes (schema API only) |
| `application/xmi` | XMI format | yes | yes |
| `application/uml` | UML format | yes | yes |

```bash
# Send Ecore XML, receive JSON
curl -X POST "..." \
  -H "Content-Type: application/ecore+xml" \
  -H "Accept: application/json" \
  -d @model.ecore

# Retrieve as JSON Schema
curl -H "Accept: application/schema+json" \
  "http://localhost:8080/rest/my-tenant/schema/stages/release/content?nsUri=..."
```

### Error Handling

All errors return a structured JSON response:

```json
{
  "message": "An internal server error occurred",
  "code": "500",
  "timestamp": "2026-03-10T12:00:00.000+00:00"
}
```

- **4xx errors**: The original error message is preserved (e.g., `"Scope [foo] not found."`)
- **5xx errors**: A generic message is returned; the full exception is logged server-side

For debugging in non-production environments, enable stack traces in error responses:

```bash
export MODELATLAS_DEBUG_STACKTRACE=true
```

**Common HTTP status codes:**

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Resource created |
| 204 | No content / not found |
| 400 | Invalid request (bad scope, stage, parameters) |
| 403 | Forbidden (read-only stage or parent object) |
| 409 | Conflict (duplicate nsUri or objectId) |
| 415 | Unsupported media type |
| 500 | Internal server error |

---

## Workflows

### Publishing a Schema

A typical schema lifecycle from creation to release:

```bash
# 1. Create schema in draft
curl -X POST "http://localhost:8080/rest/my-tenant/schema/stages/draft?nsUri=http%3A%2F%2Fexample.com%2Fbilling%2Fv1&name=Billing&version=1.0.0" \
  -H "Content-Type: application/json" \
  -d @billing.json

# 2. Transition: draft -> approved
curl -X POST "http://localhost:8080/rest/my-tenant/schema/stages/draft/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{"objectId": "http://example.com/billing/v1", "targetStage": "approved"}'

# 3. Transition: approved -> release
curl -X POST "http://localhost:8080/rest/my-tenant/schema/stages/approved/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{"objectId": "http://example.com/billing/v1", "targetStage": "release"}'

# 4. Schema is now visible to child scopes
curl http://localhost:8080/rest/child-tenant/schema
# -> includes billing schema with isReadOnly=true
```

### Storing Objects with Schema Validation

Objects stored via the Object Storage API are validated against the registry's schema:

```bash
# 1. Ensure the schema is published (see above)

# 2. Store an object conforming to the schema
curl -X POST "http://localhost:8080/rest/my-tenant/registries/configurations/stages/draft/my-config?name=My%20Config&version=1.0.0" \
  -H "Content-Type: application/json" \
  -d @my-config.json

# 3. Transition through stages
curl -X POST "http://localhost:8080/rest/my-tenant/registries/configurations/stages/draft/actions/transition" \
  -H "Content-Type: application/json" \
  -d '{"objectId": "my-config", "targetStage": "release"}'
```

---

## Health Checks

Model Atlas provides health check endpoints using [Apache Felix Health Checks](https://felix.apache.org/documentation/subprojects/apache-felix-healthchecks.html):

| Endpoint | Description |
|----------|-------------|
| `/atlas/system/health` | All health checks |
| `/atlas/system/health.json` | JSON format |
| `/atlas/system/health.html` | HTML page |
| `/atlas/system/health?tags=liveness` | Liveness checks only |
| `/atlas/system/health?tags=readiness` | Readiness checks only |

**Available checks:**

| Check | Tags | Verifies |
|-------|------|----------|
| Liveness | `atlas`, `liveness` | OSGi framework is running |
| EMF Registry | `atlas`, `readiness` | EPackages are registered |
| Media Types | `atlas`, `readiness` | Media type codecs are available |

**Kubernetes integration:**

```yaml
livenessProbe:
  httpGet:
    path: /atlas/system/health?tags=liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /atlas/system/health?tags=readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

---

## Configuration

Model Atlas is configured via OSGi Configuration Admin. Configuration files are JSON-based and located in the runtime config modules.

### Scope Configuration

Each scope is a factory configuration of `ScopeService`:

```json
{
  "ScopeService~my-tenant": {
    "scope.name": "my-tenant",
    "scope.description": "Primary tenant workspace",
    "parent.scope": "global-corporate",
    "registryService.target": "(|(registry.name=schema)(registry.name=configurations))",
    "registryService.cardinality.minimum:int": 1
  }
}
```

| Property | Required | Description |
|----------|----------|-------------|
| `scope.name` | yes | Unique scope identifier (used in URL paths) |
| `scope.description` | no | Human-readable description |
| `parent.scope` | no | Parent scope name (empty for root scopes) |
| `registryService.target` | yes | OSGi filter selecting which registries belong to this scope |

### Registry Configuration

Each registry is a factory configuration of `RegistryService`:

```json
{
  "RegistryService~schema": {
    "registry.name": "schema",
    "registry.description": "Schema registry for EPackage objects",
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
    "delete.after.transition": true,
    "storageService.target": "(storage.type=apicurio)",
    "schema.uri": "http://www.eclipse.org/emf/2002/Ecore",
    "root.eclass.uri": "http://www.eclipse.org/emf/2002/Ecore#//EPackage"
  }
}
```

| Property | Description |
|----------|-------------|
| `registry.name` | Registry identifier (becomes the URL path segment) |
| `stages` | Stage definitions with `writable` and `final` flags |
| `workflow.transitions` | Allowed transitions, format: `"fromStage:toStage"` |
| `stage.storage.mappings` | Maps stages to storage types, format: `"stage:storageType"` |
| `storageService.target` | OSGi filter to select the storage backend |
| `delete.after.transition` | Remove object from source stage after transition |
| `root.eclass.uri` | Expected root EClass URI for schema validation (Object Storage) |

### Storage Backend Configuration

Storage backends are configured independently and referenced by `storage.type`:

**File-based storage:**
```json
{
  "FileObjectStorage~file": {
    "workspace.folder": "/data/storage",
    "storage.type": "file",
    "registry.target": "(registry=main)"
  }
}
```

**Apicurio Registry storage:**
```json
{
  "ApicurioObjectStorage~apicurio": {
    "base.url": "http://localhost:8081/apis/registry/v3/",
    "storage.type": "apicurio",
    "registry.target": "(registry=main)"
  }
}
```

**Shared Lucene Registry** (metadata index across all storage backends):
```json
{
  "LuceneEObjectRegistryService~main": {
    "registry.workspace.folder": "/data/shared-registry",
    "storage.backend.tracking": true,
    "initial.index.capacity": 10000
  }
}
```

> For details on the Apicurio storage integration, see [Apicurio Management README](../org.eclipse.fennec.model.atlas.management.apicurio/README.md).

---

## Further Reading

### API Documentation (detailed endpoint reference)
- [REST Application Overview](../org.eclipse.fennec.model.atlas.rest.application/README.md) - Architecture and quick start
- [Scopes API](../org.eclipse.fennec.model.atlas.rest.application/README-Scopes.md) - Full ScopesResource documentation
- [Schema Packages API](../org.eclipse.fennec.model.atlas.rest.application/README-SchemaPackages.md) - Full SchemaPackagesResource documentation
- [Object Storage API](../org.eclipse.fennec.model.atlas.rest.application/README-ObjectStorage.md) - Full ObjectRegistryResource documentation
- [Model Converter API](../org.eclipse.fennec.model.atlas.rest.application/README-ModelConverter.md) - Format conversion endpoints
- [Object Validation API](#object-validation-api) - Validate EObjects against schema and OCL constraints

### Specifications
- [Model Atlas API Specification](../org.eclipse.fennec.model.atlas.rest.application/Model%20Atlas%20API%20Specification.md) - Core API design, visibility rules, conventions
- [Model Atlas Object API Specification](../org.eclipse.fennec.model.atlas.rest.application/Model%20Atlas%20Object%20API%20Specification.md) - Object Storage API design

### Data Generation
- [DataGen Service](../org.eclipse.fennec.model.atlas.datagen/README.md) - Fake data generation for EMF models using Datafaker
- [DataGen REST API](../org.eclipse.fennec.model.atlas.datagen.rest/README.md) - REST endpoint with XMI examples

### Internal Components
- [Workflow / ScopeService](../org.eclipse.fennec.model.atlas.workflow/README.md) - Workflow service internals and configuration
- [Apicurio Storage](../org.eclipse.fennec.model.atlas.management.apicurio/README.md) - Apicurio Registry integration details

### Docker
- [Docker Apicurio variant](../docker/modelatlas_apicurio/README.md)
- [Docker File variant](../docker/modelatlas_file/README.md)
- [Docker Compose Apicurio](../docker/dockercompose/docker-compose-apicurio.yml) - Full stack with Apicurio + UI
- [Docker Compose File](../docker/dockercompose/docker-compose-file.yml) - Standalone file storage

---

*Eclipse Public License 2.0 (EPL-2.0) - Copyright (c) 2012 - 2026 Data In Motion and others.*
