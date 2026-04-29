# JPA Mapping REST API

This document describes the REST endpoints exposed by the `org.eclipse.fennec.data.atlas.jpa.rest` bundle.
All endpoints are served under the `/jpa` base path.

## Endpoints

### `GET /jpa/hello`

A simple liveness probe that verifies the REST resource is up and reachable.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| 200 OK | `Hello` (text/plain) | The resource is running |

---

### `POST /jpa/test`

Tests whether a JPA mapping configuration can establish a connection to its declared data source.
The body is a `JpaMappingConfig` object serialised as XMI.

Implements requirement [de-jena/Projektplanung#22](https://github.com/de-jena/Projektplanung/issues/22).

**Request**

- Content-Type: `application/xml` or `application/xmi`
- Body: a `JpaMappingConfig` XMI document, e.g.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jpamapping:JpaMappingConfig xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
    name="my-mapping">
  <dataSource
      driverClass="org.h2.Driver"
      jdbcUrl="jdbc:h2:mem:mydb"
      username="sa"
      dialect="H2"/>
</jpamapping:JpaMappingConfig>
```

**Validation**

The endpoint performs the following checks before attempting a connection, returning `400 Bad Request` if any fails:

1. The `JpaMappingConfig` must contain a `DataSourceConfig` child element.
2. The declared SQL dialect must be `H2` (the only currently supported dialect).
3. The driver class must be `org.h2.Driver` (the only currently supported driver).
4. The JDBC URL must be present and non-blank.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| 200 OK | — | Connection established successfully |
| 400 Bad Request | error message (text/plain) | Validation failed or connection could not be established |
| 408 Request Timeout | error message (text/plain) | No connection result was returned within 5 seconds |
| 500 Internal Server Error | error message (text/plain) | The waiting thread was interrupted unexpectedly |

---

### `GET /jpa/test/{dataSourceName}`

Tests the connection to an already-registered OSGi `DataSource` service identified by name.
The name corresponds to the `name` attribute of the `JpaMappingConfig` whose H2 data source was
registered through the `DataSourceConfigHandler` (i.e. a `.jpamapping` file loaded by the
`JpaMappingFileWatcher`).

Implements requirement [de-jena/Projektplanung#111](https://github.com/de-jena/Projektplanung/issues/111).

**Path parameter**

| Parameter | Type | Description |
|-----------|------|-------------|
| `dataSourceName` | String | The name of the registered `DataSource` OSGi service to test |

**How the DataSource is resolved**

When a `.jpamapping` file is picked up by the `JpaMappingFileWatcher`, the `DataSourceConfigHandler`
registers an H2 `DataSource` OSGi service whose `name` property matches the `name` attribute of the
`JpaMappingConfig`. This endpoint looks up that service by name and attempts to obtain a connection.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| 200 OK | — | Connection established successfully |
| 400 Bad Request | error message (text/plain) | No `DataSource` found with that name, or connection failed |
| 408 Request Timeout | error message (text/plain) | No connection result was returned within 5 seconds |
| 500 Internal Server Error | error message (text/plain) | The waiting thread was interrupted unexpectedly |

## Constraints and current limitations

- Only the **H2** SQL dialect and the `org.h2.Driver` driver class are currently supported for
  connection testing via `POST /jpa/test`.
