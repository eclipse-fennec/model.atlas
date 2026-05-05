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

---

## Data Retrieval Endpoints

These endpoints are served by `JpaDataResource` under the `/jpa/data` base path.
They query data from a registered JPA persistence unit by EMF class name.

### Prerequisites

Data retrieval requires:

1. A `.jpamapping` file to be loaded by the `DataFolderWatcher`, which registers a `JpaMappingConfig`
   OSGi service and sets up the H2 database with the declared table structure.
2. The `JpaModelSetup` component to have created the corresponding EclipseLink persistence unit,
   registered as an `EntityManagerFactory` OSGi service with property `osgi.unit.name` equal to
   the mapping name.

---

### `GET /jpa/data/{eClassName}`

Retrieves all objects of a given EMF class name from the JPA persistence unit.

**Path parameter**

| Parameter | Type | Description |
|-----------|------|-------------|
| `eClassName` | String | Simple name of the EMF class (e.g. `Employee`) |

**Query parameters**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `ePackageUri` | String | — | URI of the EPackage that declares the class. Optional when only one `JpaMappingConfig` maps the class; required when multiple configs match (see 409 below). |
| `limit` | int | `100` | Maximum number of objects to return |

**Response**

| Status | Body | Description |
|--------|------|-------------|
| 200 OK | `gecko.emf.utilities.Response` (XML or JSON) | Objects retrieved successfully |
| 204 No Content | — | No objects found for the given class |
| 404 Not Found | error message (text/plain) | No `JpaMappingConfig` or persistence unit found for the class |
| 409 Conflict | error message (text/plain) | Multiple `JpaMappingConfig` services map the class; provide `ePackageUri` to disambiguate |
| 500 Internal Server Error | error message (text/plain) | Unexpected error during query execution |

**Example — retrieve all employees**

```
GET /rest/jpa/data/Employee?ePackageUri=http://example.org/jpa/demo/1.0
Accept: application/xml
```

**Example — retrieve invoices from a schema-qualified table (without ePackageUri)**

```
GET /rest/jpa/data/Invoice
Accept: application/json
```

**Example — retrieve at most 5 products**

```
GET /rest/jpa/data/Product?ePackageUri=http://example.org/jpa/demo/1.0&limit=5
Accept: application/xml
```

---

### `GET /jpa/data/{eClassName}/{id}`

Retrieves a single object of a given EMF class name by its primary key.

**Path parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `eClassName` | String | Simple name of the EMF class (e.g. `Employee`) |
| `id` | String | Primary key value. Automatically parsed to the correct Java type based on the `columnType` declared for the primary key column in the `JpaMappingConfig` (see type mapping below). |

**Query parameters**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `ePackageUri` | String | — | URI of the EPackage. Optional when unambiguous, required when multiple configs map the class. |

**Primary key type mapping**

The `id` path segment is a string that is cast to the appropriate Java type using the `columnType`
declared for the primary key column in the `JpaMappingConfig`:

| Column type | Java type |
|-------------|-----------|
| `INTEGER`, `INT`, `SMALLINT` | `Integer` |
| `BIGINT` | `Long` |
| `DECIMAL`, `NUMERIC`, `REAL`, `FLOAT`, `DOUBLE` | `Double` |
| `BOOLEAN` | `Boolean` |
| anything else (e.g. `VARCHAR`) | `String` |

**Response**

| Status | Body | Description |
|--------|------|-------------|
| 200 OK | `gecko.emf.utilities.Response` (XML or JSON) | Object retrieved successfully |
| 204 No Content | — | No object found for the given id |
| 400 Bad Request | error message (text/plain) | The id value cannot be parsed to the primary key column type |
| 404 Not Found | error message (text/plain) | No `JpaMappingConfig` or persistence unit found for the class |
| 409 Conflict | error message (text/plain) | Multiple configs match; provide `ePackageUri` |
| 500 Internal Server Error | error message (text/plain) | No primary key mapping found, or unexpected query error |

**Example — retrieve employee with id 1**

```
GET /rest/jpa/data/Employee/1?ePackageUri=http://example.org/jpa/demo/1.0
Accept: application/xml
```

**Example — retrieve an invoice from a schema-qualified table**

```
GET /rest/jpa/data/Invoice/3?ePackageUri=http://example.org/jpa/demo/1.0
Accept: application/json
```

**Example — invalid id type returns 400**

```
GET /rest/jpa/data/Employee/notanumber?ePackageUri=http://example.org/jpa/demo/1.0
# Response: 400 Bad Request
# Body: Cannot parse id 'notanumber' for column type BIGINT: For input string: "notanumber"
```

---

## Constraints and current limitations

- Only the **H2** SQL dialect and the `org.h2.Driver` driver class are currently supported for
  connection testing via `POST /jpa/test`.
- Data retrieval via `GET /jpa/data/...` supports any JDBC-compatible database that is backed by
  a registered `EntityManagerFactory` OSGi service, but the current `DataFolderWatcher` pipeline
  only creates H2 in-memory or file-based databases from CSV data.
- The `ePackageUri` query parameter is required whenever more than one loaded `.jpamapping` file
  declares a table mapping for the requested class name.
