# model.atlas
Fennec Model Atlas

## Health Checks

Model Atlas provides health check endpoints using [Apache Felix Health Checks](https://felix.apache.org/documentation/subprojects/apache-felix-healthchecks.html) for monitoring system health and supporting Kubernetes liveness/readiness probes.

### Endpoints

| Endpoint | Description |
|----------|-------------|
| `/health` | Returns all health checks with the `atlas` tag |
| `/health.json` | Returns health status in JSON format |
| `/health.html` | Returns health status as HTML page |
| `/health?tags=liveness` | Returns only liveness checks |
| `/health?tags=readiness` | Returns only readiness checks |

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
    path: /atlas/health?tags=liveness
    port: 8086
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /atlas/health?tags=readiness
    port: 8086
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
