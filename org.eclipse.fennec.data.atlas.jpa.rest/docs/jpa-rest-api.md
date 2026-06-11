# JPA Mapping REST API

This document describes the REST endpoints exposed by the `org.eclipse.fennec.data.atlas.jpa.rest` bundle.
All endpoints are served under the `/jpa/{rootFolderName}/data` base path.

## Routing model

`JpaDataResource` is mounted at `@Path("/jpa/{rootFolderName}/data")`. The
`{rootFolderName}` path segment is the name of the data root folder that
`DataFolderWatcher` was configured to watch (the last segment of
`io.fs.watcher.path`). When the pipeline starts, `DataFolderWatcher` propagates
this value as the `jpa.root.folder` service property on every service it builds
— H2 `DataSource`, `EPackage` registry, `ResourceSet` factory, `EntityMappings`,
and (indirectly, via `osgi.unit.name`) the `EntityManagerFactory`.

`JpaDataResourceFilter` (a `ContainerRequestFilter` registered as a JAX-RS
extension) maintains three maps keyed by `jpa.root.folder`:

- `ResourceSet` services
- `EntityMappings` services
- `EntityManagerFactory` services (keyed by `osgi.unit.name`, which
  `DataFolderWatcher` sets to the same value as the matcher key — see
  README for details)

On each request, the filter looks up the three services by `{rootFolderName}`,
validates the `ePackageUri` query parameter (if provided) against the
`EntityMappings.getPackage()`, and confirms that the `{eClassName}` resolves to
an `EClassifier` in the registered `EPackage`. The resolved
`EntityManagerFactory` and `EntityMappings` are stashed on the
`ContainerRequestContext` and consumed by the resource methods.

Any failure in the filter short-circuits the request with **400 Bad Request**;
in particular there is no longer a 404/409 disambiguation step, because each
`rootFolderName` resolves to exactly one `EntityMappings` (and therefore one
EPackage).

## Endpoints

### `GET /jpa/{rootFolderName}/data/hello`

A simple liveness probe that verifies the REST resource is up and reachable.

**Path parameter**

| Parameter | Type | Description |
|-----------|------|-------------|
| `rootFolderName` | String | Name of the data root folder (must match a registered `jpa.root.folder` service property) |

**Response**

| Status | Body | Description |
|--------|------|-------------|
| 200 OK | `Hello JpaDataResource` (text/plain) | The resource is running |

---

## Data Retrieval Endpoints

These endpoints are served by `JpaDataResource` under the
`/jpa/{rootFolderName}/data` base path. They query data from the JPA
persistence unit associated with `{rootFolderName}`.

### Prerequisites

Data retrieval requires:

1. A `DataFolderWatcher` configured against the data root folder (its name is
   what the client passes as `{rootFolderName}`). The watcher creates the
   pipeline configurations (DataSource, EMF/EORM watchers, CSV importer,
   persistence unit) and tags every resulting service with
   `jpa.root.folder=<rootFolderName>`.
2. An `.eorm` file present under `<root>/mapping/` so that `EormFileWatcher`
   registers the matching `EntityMappings` OSGi service (also tagged with
   `jpa.root.folder=<rootFolderName>`).
3. The `fennec.jpa.EMPersistenceUnit` component to have built the corresponding
   EclipseLink persistence unit, registered as an `EntityManagerFactory` OSGi
   service with property `osgi.unit.name=<matcherKey>` (the same value the
   filter binds via its `EntityManagerFactory` reference).

If any of the three (ResourceSet, EntityMappings, EntityManagerFactory) cannot
be resolved for `{rootFolderName}`, the request is rejected with 400.

---

### `GET /jpa/{rootFolderName}/data/{eClassName}`

Retrieves all objects of a given EMF class name from the JPA persistence unit
bound to `{rootFolderName}`.

**Path parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `rootFolderName` | String | Name of the data root folder (must match a registered `jpa.root.folder` service property) |
| `eClassName` | String | Simple name of the EMF class (e.g. `Employee`) |

**Query parameters**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `ePackageUri` | String | — | Optional. If provided, must equal the `EntityMappings.getPackage()` URI registered for `{rootFolderName}`; otherwise the request is rejected with 400. |
| `limit` | int | `100` | Maximum number of objects to return |

**Response**

| Status | Body | Description |
|--------|------|-------------|
| 200 OK | `gecko.emf.utilities.Response` (XML or JSON) | Objects retrieved successfully |
| 204 No Content | — | No objects found for the given class |
| 400 Bad Request | error message (text/plain) | No `ResourceSet` / `EntityManagerFactory` / `EntityMappings` registered for `{rootFolderName}`; or `ePackageUri` does not match the registered `EntityMappings`; or `{eClassName}` is not an `EClassifier` of the registered `EPackage` |
| 500 Internal Server Error | error message (text/plain) | Unexpected error during query execution |

**Example — retrieve all employees from the `demo` root folder**

```
GET /rest/jpa/demo/data/Employee?ePackageUri=http://example.org/jpa/demo/1.0
Accept: application/xml
```

**Example — retrieve invoices (without ePackageUri)**

```
GET /rest/jpa/demo/data/Invoice
Accept: application/json
```

**Example — retrieve at most 5 products**

```
GET /rest/jpa/demo/data/Product?ePackageUri=http://example.org/jpa/demo/1.0&limit=5
Accept: application/xml
```

---

### `GET /jpa/{rootFolderName}/data/{eClassName}/{id}`

Retrieves a single object of a given EMF class name by its primary key from the
JPA persistence unit bound to `{rootFolderName}`.

**Path parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `rootFolderName` | String | Name of the data root folder (must match a registered `jpa.root.folder` service property) |
| `eClassName` | String | Simple name of the EMF class (e.g. `Employee`) |
| `id` | String | Primary key value. Automatically parsed to the correct Java type based on the `columnDefinition` declared for the primary key `<column>` in the `.eorm` (see type mapping below). |

**Query parameters**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `ePackageUri` | String | — | Optional. If provided, must equal the `EntityMappings.getPackage()` URI registered for `{rootFolderName}`. |

**Primary key type mapping**

The `id` path segment is a string that is cast to the appropriate Java type
using the `columnDefinition` declared for the primary key column in the `.eorm`:

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
| 400 Bad Request | error message (text/plain) | The id cannot be parsed to the primary key column type; or any of the filter-level resolution failures (see endpoint above) |
| 500 Internal Server Error | error message (text/plain) | No primary key mapping found in the `EntityMappings` for `{eClassName}`, or unexpected query error |

**Example — retrieve employee with id 1**

```
GET /rest/jpa/demo/data/Employee/1?ePackageUri=http://example.org/jpa/demo/1.0
Accept: application/xml
```

**Example — retrieve an invoice**

```
GET /rest/jpa/demo/data/Invoice/3?ePackageUri=http://example.org/jpa/demo/1.0
Accept: application/json
```

**Example — invalid id type returns 400**

```
GET /rest/jpa/demo/data/Employee/notanumber?ePackageUri=http://example.org/jpa/demo/1.0
# Response: 400 Bad Request
# Body: Cannot parse id 'notanumber' for column type BIGINT: For input string: "notanumber"
```

---

## Constraints and current limitations

- Each `{rootFolderName}` resolves to **exactly one** `EntityMappings` (the one
  loaded from `<root>/mapping/*.eorm`), so there is no per-class
  disambiguation: the `ePackageUri` query parameter is purely a sanity check
  against the registered `EntityMappings.getPackage()`. To serve a different
  EPackage, configure a separate `DataFolderWatcher` against a different root
  folder.
- Data retrieval via `GET /jpa/{rootFolderName}/data/...` supports any
  JDBC-compatible database that is backed by a registered
  `EntityManagerFactory` OSGi service, but the current `DataFolderWatcher`
  pipeline only creates H2 in-memory or file-based databases from CSV data.
