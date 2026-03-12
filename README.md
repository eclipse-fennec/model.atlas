# Fennec Model Atlas

A dynamic EMF model management system providing a RESTful API for managing and transforming EMF models at runtime.

## Docker

Model Atlas is available as a Docker image in two variants, each tailored to a different storage backend.

### Image Variants

| Variant | Image Tag | Description |
|---------|-----------|-------------|
| **Apicurio** | `eclipsefennec/model.atlas:apicurio-latest` | Uses [Apicurio Registry](https://www.apicur.io/registry/) for model storage with a full workflow (draft, approved, release stages) |
| **File** | `eclipsefennec/model.atlas:file-latest` | Uses local file-based storage, no external dependencies required |

Both variants are also available on GHCR as `ghcr.io/eclipse-fennec/model.atlas`.

Snapshot builds from the `snapshot` branch are tagged as `apicurio-snapshot` and `file-snapshot`. Version-specific tags (e.g. `apicurio-0.0.1`) are also published.

### Quick Start (File variant)

```bash
docker run -d -p 8080:8080 eclipsefennec/model.atlas:file-latest
```

### Quick Start (Apicurio variant)

The Apicurio variant requires a running Apicurio Registry. Use the provided Docker Compose file:

```bash
docker compose -f docker/dockercompose/docker-compose-apicurio.yml up -d
```

This starts Model Atlas together with Apicurio Registry and its UI:

| Service | URL |
|---------|-----|
| Model Atlas | http://localhost:8080 |
| Apicurio Registry API | http://localhost:8081 |
| Apicurio Registry UI | http://localhost:8888 |

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `APICURIO_HOST` | `localhost` | Hostname of the Apicurio Registry (Apicurio variant only) |
| `APICURIO_PORT` | `8081` | Port of the Apicurio Registry (Apicurio variant only) |
| `STORAGE_ROOT` | `/tmp/mac` | Root directory for file-based storage (File variant only) |

### Docker Compose Files

Pre-configured compose files are available in `docker/dockercompose/`:

| File | Description |
|------|-------------|
| `docker-compose-apicurio.yml` | Model Atlas with Apicurio Registry and UI |
| `docker-compose-file.yml` | Model Atlas with file-based storage (standalone) |

### Building Locally

```bash
# Build the project
./gradlew build -x test -x testOSGi

# Export the runtime JARs
./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_apicurio
./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_file

# Prepare and build Docker images
./gradlew docker:modelatlas_apicurio:prepareDocker
./gradlew docker:modelatlas_file:prepareDocker

docker build -t eclipsefennec/model.atlas:apicurio-snapshot docker/modelatlas_apicurio/
docker build -t eclipsefennec/model.atlas:file-snapshot docker/modelatlas_file/
```

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
