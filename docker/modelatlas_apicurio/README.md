# Model Atlas - Apicurio Variant

This Docker image runs Model Atlas with [Apicurio Registry](https://www.apicur.io/registry/) as the storage backend.

## Usage

This variant requires a running Apicurio Registry instance. Use the provided Docker Compose file for a complete setup:

```bash
docker compose -f docker/dockercompose/docker-compose-apicurio.yml up -d
```

Or run standalone with an existing Apicurio Registry:

```bash
docker run -d -p 8080:8080 \
  -e APICURIO_HOST=your-registry-host \
  -e APICURIO_PORT=8080 \
  eclipsefennec/model.atlas:apicurio-snapshot
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `APICURIO_HOST` | `localhost` | Hostname of the Apicurio Registry |
| `APICURIO_PORT` | `8081` | Port of the Apicurio Registry |
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
