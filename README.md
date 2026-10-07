# Fennec Model Atlas

A dynamic EMF model management system providing a RESTful API for managing and transforming EMF models at runtime.

For the full user documentation covering the REST API, core concepts, configuration, and workflows, see the **[User Guide](docs/user-guide.md)**.

Feature guides:

- [The GDPR review history document](docs/gdpr-review-history.md) — the derived, diffable review record and how to download it as a spreadsheet
- [Publishing to the model atlas](docs/model-atlas-publishing-mcp-tool.md) — the MCP tools and the publisher bundle behind them
- [QVT transformations](docs/qvt-transformations.md) — model-to-model transformations

## Docker

Model Atlas is available as a Docker image with file-based storage.

### Image

| Image Tag | Description |
|-----------|-------------|
| `eclipsefennec/model.atlas:file-latest` | Uses local file-based storage, no external dependencies required |

The image is also available on GHCR as `ghcr.io/eclipse-fennec/model.atlas`.

Snapshot builds from the `snapshot` branch are tagged as `file-snapshot`. Version-specific tags (e.g. `file-0.0.1`) are also published.

### Quick Start

```bash
docker run -d -p 8080:8080 eclipsefennec/model.atlas:file-latest
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `STORAGE_ROOT` | `/tmp/mac` | Root directory for file-based storage |

### Docker Compose Files

Pre-configured compose files are available in `docker/dockercompose/`:

| File | Description |
|------|-------------|
| `docker-compose-file.yml` | Model Atlas with file-based storage (standalone) |

### Building Locally

```bash
# Build the project
./gradlew build -x test -x testOSGi

# Export the runtime JAR
./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_file

# Prepare and build the Docker image
./gradlew docker:modelatlas_file:prepareDocker

docker build -t eclipsefennec/model.atlas:file-snapshot docker/modelatlas_file/
```

## Branches & releases

* `snapshot` is the active development branch. PRs land here first; every
  push builds and publishes the `:file-snapshot` container image to Docker
  Hub and GHCR.
* `main` always holds the latest released version. A release publishes both:
  the OSGi bundles go to Maven Central under the group id
  `org.eclipse.fennec.model.atlas` (`maven-central: true` in `cnf/build.bnd`),
  and the runtime image appears as `:file-latest`
  on [Docker Hub](https://hub.docker.com/r/eclipsefennec/model.atlas/tags) and
  [GHCR](https://github.com/eclipse-fennec/model.atlas/pkgs/container/model.atlas),
  alongside a version-pinned tag built from `Bundle-Version`. Test and
  runtime-config bundles set `-maven-release: local` and are never published.

See [docs/ci.md](docs/ci.md) for the full CI / publishing pipeline.

## Health Checks

Model Atlas provides health check endpoints using [Apache Felix Health Checks](https://felix.apache.org/documentation/subprojects/apache-felix-healthchecks.html) for monitoring system health and supporting Kubernetes liveness/readiness probes.

### Endpoints

| Endpoint | Description |
|----------|-------------|
| `/atlas/system/health` | Returns all health checks with the `atlas` tag |
| `/atlas/system/health.json` | Returns health status in JSON format |
| `/atlas/system/health.html` | Returns health status as HTML page |
| `/atlas/system/health?tags=liveness` | Returns only liveness checks |
| `/atlas/system/health?tags=readiness` | Returns only readiness checks |

### Available Health Checks

| Health Check | Tags | Description |
|--------------|------|-------------|
| Liveness | `atlas`, `liveness` | Confirms the OSGi framework is running |
| EMF Registry | `atlas`, `readiness` | Verifies EPackages are registered in the EMF registry |
| Media Types | `atlas`, `readiness` | Verifies media type codecs are available |
| Scopes And Registries | `atlas`, `readiness` | Lists every scope with its registries and stages |
| Storage Scopes | `atlas` | File storage only, and only where configured: warns when a scope has no data while some stored folder belongs to no scope — the signature of a renamed scope, whose data the file backend leaves under the old name |

### Kubernetes Integration

Configure your Kubernetes deployment to use the health endpoints:

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

### Response Format

The JSON response includes the overall result and individual health check results:

```json
{
  "overallResult": "OK",
  "results": [
    {
      "name": "EMF Registry",
      "status": "OK",
      "messages": ["EMF Registry contains 5 EPackages"]
    },
    {
      "name": "Media Types",
      "status": "OK",
      "messages": ["Supporting 8 media types"]
    }
  ]
}
```

### HTTP Status Codes

| Status | HTTP Code |
|--------|-----------|
| OK | 200 |
| WARN | 200 |
| CRITICAL | 503 |
| TEMPORARILY_UNAVAILABLE | 503 |
