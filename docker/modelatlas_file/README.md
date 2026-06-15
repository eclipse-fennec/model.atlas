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

To persist data across container restarts, mount a volume for the storage directory:

```bash
docker run -d -p 8080:8080 \
  -v modelatlas-data:/opt/modelatlas/runtime/data \
  eclipsefennec/model.atlas:file-snapshot
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `STORAGE_ROOT` | `/tmp/mac` | Root directory for file-based storage |
| `INITIAL_MODELS_FOLDER` | `/initial-models` | Folder scanned once on startup to seed initial models (`.ecore` / `.jsonschema` / `.qvto`). Mount your models at this path; remove the variable to disable. See the [Initial Model Bootstrap README](../../org.eclipse.fennec.model.atlas.bootstrap/README.md). |

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
