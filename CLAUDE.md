# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Fennec Model Atlas** is a dynamic EMF model management system built on **OSGi/Eclipse Modeling Framework (EMF)** with **bnd tools** for OSGi bundle management and **Gradle** as the build system. It provides a RESTful API for managing and transforming EMF models dynamically at runtime.

### Core Architecture

- **OSGi-based**: Uses bnd workspace with OSGi bundles for modular architecture
- **Dynamic EMF Models**: Runtime loading of .ecore models from filesystem with automatic registration
- **RESTful API**: Jakarta RS-based REST API with Swagger/OpenAPI documentation
- **Model Transformations**: QVT (Query/View/Transformation) support for model-to-model transformations
- **Multi-format Export**: Supports JSON, BSON, XLSX, ODS, R-lang, and more via Fennec Codec
- **File Watching**: Automatic model reloading when .ecore or .qvto files change

### Package Structure

All packages use the `org.eclipse.fennec.model.atlas.*` prefix:
- `org.eclipse.fennec.model.atlas` - Core model bundle with EMFFileWatcher
- `org.eclipse.fennec.model.atlas.runtime` - Runtime configuration and requirements
- `org.eclipse.fennec.model.atlas.rest.application` - REST API with Swagger UI
- `org.eclipse.fennec.model.atlas.model.documentation.provider` - Model documentation generation
- `org.eclipse.fennec.model.atlas.mediatypes.api/impl` - Supported media types tracking
- `org.eclipse.fennec.model.atlas.emf.common` - Dynamic EPackage and EObject utilities

## Development Commands

### Build & Test
```bash
# Full build and test
./gradlew build

# Clean build
./gradlew clean build

# Build specific module
./gradlew :org.eclipse.fennec.model.atlas:build

# Release (creates OSGi bundles)
./gradlew release
```

### Runtime Export
```bash
# Resolve runtime dependencies
./gradlew org.eclipse.fennec.model.atlas.runtime:resolve.modelatlas.runtime_base

# Export docker runtime
./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker

# Prepare docker images
./gradlew prepareDocker
```

### Development Workflow
```bash
# Assemble without tests
./gradlew assemble

# Run unit tests with JaCoCo coverage
./gradlew test

# Run OSGi integration tests
./gradlew testOSGi

# Clean cache (bnd workspace cache)
./gradlew cleanCache
```

### Code Quality
- **JaCoCo**: Coverage reports in `build/reports/jacoco/codeCoverageReport/`
- **SonarQube**: Configured for code quality analysis (SonarCloud integration)
- **JUnit 5**: Testing framework with OSGi integration

## Key Components

### EMFFileWatcher (org.eclipse.fennec.model.atlas:86-556)
Central component that monitors filesystem for EMF models and QVT transformations:
- Watches `workspace/` directory for .ecore, .qvto, and .jsonschema files
- Automatically registers EPackages as OSGi services
- Creates dynamic EFactory instances for runtime model instantiation
- Registers QVT transformations with ConfigurationAdmin
- Supports JSON Schema to EPackage conversion

### REST Application
Provides HTTP endpoints for model access:
- Swagger UI at `/swagger-api/`
- Model resources exposed via Jakarta RS
- Multiple content types supported (JSON, XML, BSON, etc.)
- OpenAPI 3.0 documentation generation

### Runtime Workspaces
The `workspace/` directory (in runtime modules) contains:
- `models/` - .ecore model definitions
- `trafos/` - QVT transformation files (.qvto)
- `pipelines/` - Pipeline configuration files

## Important Configuration Files

- `cnf/build.bnd` - OSGi workspace configuration with Fennec/Gecko library imports
- `settings.gradle` - Multi-module Gradle setup with bnd workspace plugin
- `build.gradle` - Root build with JaCoCo, SonarQube, and JUnit setup
- `Jenkinsfile` - CI/CD pipeline for main/snapshot branches
- `*.bndrun` - Runtime configurations (base, local, docker)

## Libraries & Dependencies

### Core Libraries (cnf/build.bnd)
- `fennec` - Fennec framework libraries
- `fennecTest` - Fennec testing support
- `geckoEMF` - Gecko EMF/OSGi integration
- `fennecQVT` - QVT transformation engine
- `fennecPersistence` - JPA/ORM persistence support
- `geckoEMFUtil` - EMF utilities for Jakarta RS
- `geckoMessaging` - Messaging integration
- `geckoEMFRepository` - EMF repository support
- `jakartaREST` - Jakarta RESTful Web Services

### Key External Dependencies
- Eclipse EMF (2.29+)
- Apache Felix OSGi framework (7.0.5)
- Jakarta RS API (3.1.0)
- Swagger Core (2.2.28)
- Jackson (2.19.2)
- Fennec Codec (1.0.0) for serialization

## Deployment & Release

- **Snapshot builds**: Deploy to `cnf/release/` for development
- **Release builds**: Version-controlled releases via Jenkins to Maven Central
- **Docker images**: Built for `modelatlas` runtime
- **Repository**: Group ID `org.eclipse.fennec.model.atlas`
- **Baselining**: Enabled for API compatibility checking
- **Java Version**: 21 (source and target)

## Dynamic Model Loading

Models in `workspace/models/` are automatically loaded and registered:
1. File watcher detects new/modified .ecore files
2. EPackage loaded with dynamic factory
3. Registered as OSGi service with namespace URI
4. Accessible via REST API
5. QVT transformations automatically configured from .qvto files

JSON Schema files (.jsonschema) are converted to EPackages at runtime.
