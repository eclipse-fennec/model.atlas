# Model Atlas - File Variant

This Docker image runs Model Atlas with file-based storage. No external dependencies are required.

## Usage

```bash
docker run -d -p 8080:8080 eclipsefennec/model.atlas:file-snapshot
```

Or use Docker Compose:

```bash
docker compose -f docker/dockercompose/docker-compose-file.yml up -d
```

Storage defaults to `/opt/modelatlas/runtime/data` in the container, so persisting data across
container restarts is just a volume on that path:

```bash
docker run -d -p 8080:8080 \
  -v modelatlas-data:/opt/modelatlas/runtime/data \
  eclipsefennec/model.atlas:file-snapshot
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `STORAGE_ROOT` | `/opt/modelatlas/runtime/data` | Root directory for file-based storage |
| `INITIAL_MODELS_FOLDER` | `/initial-models` | Folder scanned once on startup to seed initial models (`.ecore` / `.jsonschema` / `.qvto`). Mount your models at this path; remove the variable to disable. See the [Initial Model Bootstrap README](../../org.eclipse.fennec.model.atlas.bootstrap/README.md). |

## Storage Backend

A single file storage backend serves every stage of every registry:

```json
"FileObjectStorage~file": {
  "workspace.folder": "$[env:STORAGE_ROOT;default=$[prop:STORAGE_ROOT;default=/opt/modelatlas/runtime/data]]",
  "storage.type": "file",
  "registry.target": "(registry=main)"
}
```

It lives in `org.eclipse.fennec.model.atlas.runtime.config.docker.file/configs/storage.json`; the
scope and registries below are configured next to it in `configs/workflow.json`.

`STORAGE_ROOT` also feeds the Lucene metadata index and the EPackage index
(`runtime.config/configs/shared-registry-config.json`, `epackage-index.json`), but those still fall
back to `/tmp/mac` when the variable is unset. Set `STORAGE_ROOT=/opt/modelatlas/runtime/data`
explicitly if you want the indexes inside the mounted volume as well.

## Preconfigured Scope and Registries

Besides the built-in read-only **atlas** scope, the image ships one configured scope, `jena`
(`parent.scope: atlas`, so all system schemas stay visible), with five registries. Every registry
maps all of its stages to the `file` storage and sets `delete.after.transition: true`, so a
transition moves an object rather than copying it.

| Registry | `registry.type` | Root EClass | Stages | Transitions |
|----------|-----------------|-------------|--------|-------------|
| `schema` | `SCHEMA` | `http://www.eclipse.org/emf/2002/Ecore#//EPackage` | `draft`, `approved`, `release` (final) | `draft→approved`, `approved→release`, `release→draft`, `approved→draft` |
| `workspace` | *(default)* | `http://www.eclipse.org/emf/2002/Ecore#//EObject` | `draft`, `release` (final) | `draft→release` |
| `DataGen` | *(default)* | `http://www.gme.org/datagen/1.0#//DataGenConfig` | `draft`, `release` (final) | `draft→release` |
| `cocl` | `COCL` | `http://www.gme.org/cocl/1.0#//OclConstraintSet` | `draft`, `release` (final) | `draft→release` |
| `sensinactmapping` | `OTHER` | `https://fennec.eclipse.org/event.atlas/mapping/1.0#//ProviderMapping` | `draft`, `release` (final) | `draft→release` |

The `schema` registry additionally runs the `EPackageStageActionService` (trigger stages `draft`,
`approved`, `release`, reading from the `file` storage): EPackages are registered as OSGi services
when they enter or are updated in one of those stages and unregistered when they leave it, so a
schema is usable by the runtime in every stage of this variant.

The models backing the registries come from the runtime itself: `DataGen` from
`org.eclipse.fennec.model.atlas.datagen.model`, `cocl` from
`org.eclipse.fennec.model.atlas.validation.model`, and `sensinactmapping` from
`org.eclipse.fennec.event.atlas.mapping`, which this variant's bndrun
(`modelatlas.runtime_docker_file.bndrun`) adds on top of the base runtime.

To change the scope name, the registry set or the stage/transition layout, edit
`configs/workflow.json` in the `runtime.config.docker.file` bundle and rebuild the image.

## Exposed Ports

| Port | Description |
|------|-------------|
| 8080 | Model Atlas REST API |

## Health Check

```bash
curl http://localhost:8080/atlas/system/health.json
```

## License

Eclipse Public License 2.0 (EPL-2.0)

Copyright (c) 2025 Contributors to the Eclipse Foundation
