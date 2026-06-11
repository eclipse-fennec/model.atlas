# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Fennec Model Atlas** is a dynamic EMF model management system built on **OSGi/Eclipse Modeling Framework (EMF)** with **bnd tools** for OSGi bundle management and **Gradle** as the build system. It provides a RESTful API for managing and transforming EMF models dynamically at runtime.

### Core Architecture

- **OSGi-based**: Uses bnd workspace with OSGi Declarative Services (DS) annotations for component wiring
- **Dynamic EMF Models**: Runtime loading of .ecore models from filesystem with automatic EPackage registration as OSGi services
- **Scope/Workflow Management**: Multi-tenant scope system with configurable stage-based workflows (draft, review, approved, release)
- **Pluggable Storage**: Storage backends are interchangeable (File-based, Apicurio Registry) via OSGi services
- **RESTful API**: Jakarta RS-based REST API with Swagger/OpenAPI documentation
- **Model Transformations**: QVT (Query/View/Transformation) support for model-to-model transformations
- **Multi-format Export**: Supports JSON, BSON, XLSX, ODS, R-lang, and more via Fennec Codec

### Architectural Layers

```
REST API (Jakarta RS)  -->  Workflow/Scope Service  -->  Storage Backends
     |                           |                        |-- File-based
     |                           |                        |-- Apicurio Registry
     v                           v                        |-- Lucene (search/index)
  OpenAPI/Swagger         Schema Registry
                                 |
                          EMF Core (EPackage loading, dynamic factories)
```

- **REST Layer** (`rest.application`, `rest.model`, `rest.ecore.xmi`, `rest.jsonschema`, `rest.xsdschema`, `rest.uml`): HTTP endpoints for model access, format conversion, and schema management
- **Workflow Layer** (`workflow`): `ScopeServiceImpl` manages scopes with parent-child hierarchies; `EObjectWorkflowService` handles stage transitions
- **Management Layer** (`management`, `management.file`, `management.apicurio`, `management.lucene`): Pluggable storage and search via `AbstractEObjectStorageService`
- **EMF Core** (`org.eclipse.fennec.model.atlas`): `EMFFileWatcher` monitors `workspace/` for .ecore/.qvto/.jsonschema files, registers EPackages as OSGi services
- **EMF Common** (`emf.common`): `DynamicEPackageConfigurator`, `PrototypeEObjectServiceFactory`, format conversion utilities

## Development Commands

### Build & Test
```bash
./gradlew build                    # Full build and test
./gradlew clean build              # Clean build
./gradlew assemble                 # Build without tests
./gradlew :org.eclipse.fennec.model.atlas:build  # Build specific module

# Tests
./gradlew test                     # Unit tests with JaCoCo coverage
./gradlew testOSGi                 # OSGi integration tests
./gradlew :org.eclipse.fennec.model.atlas.rest.tests:test  # Single module tests
./gradlew build -x test -x testOSGi  # Build skipping all tests

# Coverage
./gradlew codeCoverageReport       # Aggregate JaCoCo report -> build/reports/jacoco/codeCoverageReport/

# Release
./gradlew release                  # Create OSGi bundles
./gradlew cleanCache               # Clear bnd workspace cache
```

### Runtime Export & Docker
```bash
# Resolve runtime dependencies
./gradlew org.eclipse.fennec.model.atlas.runtime:resolve.modelatlas.runtime_base

# Export runtime JARs for docker variants
./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_apicurio
./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_file

# Prepare and build Docker images
./gradlew docker:modelatlas_apicurio:prepareDocker
./gradlew docker:modelatlas_file:prepareDocker
docker build -t eclipsefennec/model.atlas:apicurio-snapshot docker/modelatlas_apicurio/
docker build -t eclipsefennec/model.atlas:file-snapshot docker/modelatlas_file/

# Run with Docker Compose
docker compose -f docker/dockercompose/docker-compose-file.yml up -d       # File storage (standalone)
docker compose -f docker/dockercompose/docker-compose-apicurio.yml up -d   # Apicurio + UI stack
```

**Docker image variants**: Apicurio (uses Apicurio Registry for versioned artifact storage) and File (local filesystem, no external deps). Both use distroless Java 21 base images, port 8080.

## Bundle/Module Structure

All bundles use the `org.eclipse.fennec.model.atlas` prefix. Key groupings:

| Group | Bundles | Purpose |
|-------|---------|---------|
| **Core** | `.` (root bundle) | EMFFileWatcher, EPackageService |
| **REST** | `.rest.application`, `.rest.model`, `.rest.ecore.xmi`, `.rest.jsonschema`, `.rest.xsdschema`, `.rest.uml`, `.rest.tests` | HTTP API, format-specific endpoints |
| **Workflow** | `.workflow`, `.workflow.tests` | Scope management, stage-based workflows |
| **Storage** | `.management`, `.management.file`, `.management.apicurio`, `.management.apicurio.model`, `.management.lucene` + test bundles | Pluggable storage backends |
| **Schema** | `.schema.registry.api`, `.schema.registry.impl` | Schema registry service |
| **Media** | `.mediatypes.api`, `.mediatypes.impl` | Media type codec tracking |
| **EMF Utils** | `.emf.common` | Dynamic EPackage config, format converters |
| **Runtime** | `.runtime`, `.runtime.config`, `.runtime.config.local`, `.runtime.config.docker`, `.runtime.config.docker.apicurio`, `.runtime.config.docker.file` | bndrun configurations per environment |
| **Health** | `.healthcheck` | Felix Health Checks (liveness/readiness) |
| **Docs** | `.model.documentation.provider` | Model documentation generation |

## Key Configuration Files

- `cnf/build.bnd` - OSGi workspace config: library definitions (fennec, gecko, EMF), Maven Central repos, Java 21
- `cnf/central.mvn` - Maven dependency coordinates
- `settings.gradle` - bnd workspace plugin (v7.2.1), includes docker modules
- `build.gradle` - JaCoCo, SonarQube, JUnit 5.13.4 / Mockito 4.11.0 / AssertJ 3.27.4
- `*.bndrun` files in runtime modules - OSGi runtime assembly with start levels

## Runtime Workspaces

The `workspace/` directory (in runtime config modules) contains:
- `models/` - .ecore model definitions (auto-loaded by EMFFileWatcher)
- `trafos/` - QVT transformation files (.qvto)
- `pipelines/` - Pipeline configuration files

JSON Schema files (.jsonschema) are converted to EPackages at runtime.

## Health Check Endpoints

- `/atlas/system/health` - All health checks (tag: `atlas`)
- `/atlas/system/health.json` - JSON format
- `/atlas/system/health?tags=liveness` - Kubernetes liveness
- `/atlas/system/health?tags=readiness` - Kubernetes readiness (EMF Registry + Media Types)

## CI/CD

- **GitHub Actions** (`.github/workflows/build.yml`): JDK 21, build + export all variants + artifact upload
- **GitLab CI** (`.gitlab-ci.yml`): License check, build, docker build stages; uses Testcontainers with DinD
- **Jenkins** (`Jenkinsfile`): Main/snapshot branch builds, docker push to internal registry
- **License checks** (`.github/workflows/license.yml`): SkyWalking Eyes header verification

## Java Version

Java 21 (source and target).
