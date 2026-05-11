# JPA Integration — Overview

This document covers the full JPA integration across all bundles: how a data folder
on disk becomes a queryable REST endpoint backed by EclipseLink and H2.

Bundles involved:

| Bundle | Role |
|--------|------|
| `org.eclipse.fennec.data.atlas.jpa.watcher` | File watchers, internal CSV loader, persistence unit configurator |
| `org.eclipse.fennec.data.atlas.jpa.datasource` | H2 DataSource creation from `JpaMappingConfig` |
| `org.eclipse.fennec.data.atlas.mapping.model` | EMF metamodel: `JpaMappingConfig`, `TableMapping`, `ColumnMapping`, `JoinMapping` |
| `org.eclipse.fennec.data.atlas.jpa.rest` | REST endpoints: data query and connection test |
| `org.eclipse.fennec.model.atlas` | `EMFFileWatcher` — loads `.ecore` files and registers `EPackage` services |

---

## Pipeline overview

```
<root>/
├── mapping/
│   ├── model.ecore          ← EMF metamodel (user-authored)
│   └── mapping.jpamapping   ← JpaMappingConfig (user-authored)
└── data/
    ├── employees.csv        ← root data folder → default DB schema
    ├── products.csv
    └── finance/
        └── invoices.csv     ← subfolder name → DB schema "finance"
```

```
DataFolderWatcher  (jpa.watcher)
  │
  ├── creates ──► EMFFileWatcher           watches mapping/
  │               └── registers EPackage (OSGi service)
  │
  ├── creates ──► JpaMappingFileWatcher    watches mapping/
  │               └── registers JpaMappingConfig (OSGi service)
  │                             │
  │                             ▼
  │               DataSourceConfigHandler  (jpa.datasource, always active)
  │               └── creates daanse H2 DataSource config
  │                   └── H2 DataSource (OSGi service, unitName=<name>)
  │
  ├── creates ──► JpaCsvDataImporter       watches data/
  │               └── waits for DataSource + EntityManagerFactory
  │                   then: DELETE + INSERT rows into existing tables
  │
  └── creates ──► JpaPersistenceUnitConfigurator
                  └── waits for JpaMappingConfig + EPackage
                      └── TableMappingConverter → EntityMappings (OSGi service)
                          └── creates fennec.jpa.EMPersistenceUnit config
                              └── EclipseLink EntityManagerFactory (OSGi service)
                                  └── tables created/extended in H2
                                        │
                                        ▼
                              GET /jpa/data/{eClassName}   (jpa.rest)
                              GET /jpa/data/{eClassName}/{id}
```

---

## Data folder layout

```
<root>/
├── mapping/
│   ├── model.ecore           Required. EMF metamodel defining the EClasses.
│   └── *.jpamapping          Required. JpaMappingConfig XMI document.
└── data/
    ├── <table>.csv           Optional. CSV files in the root → default schema.
    └── <schema>/
        └── <table>.csv       Optional. CSV files in subfolders → named schema.
```

`DataFolderWatcher` points `EMFFileWatcher` and `JpaMappingFileWatcher` at
`<root>/mapping/`, and `JpaCsvDataImporter` at `<root>/data/`.

---

## The `.jpamapping` file

The `.jpamapping` file is an XMI document whose root element is a
`JpaMappingConfig`. Minimum structure:

```xml
<jpamapping:JpaMappingConfig
    xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
    name="<unit-name>"
    targetModelNsUri="<nsUri of model.ecore>">

  <dataSource driverClass="org.h2.Driver"
              jdbcUrl="jdbc:h2:mem:<dbname>;DB_CLOSE_DELAY=-1"
              username="sa"
              passwordRef="DB_PASSWORD"
              poolSize="5"
              dialect="H2"/>

  <!-- one TableMapping per EClass -->
  <tableMappings
      className="<nsUri>#//<EClassName>"
      tableName="<table>"
      schema="<schema>">
    <columnMappings featureName="id"        columnName="id"         columnType="BIGINT"       nullable="false" primaryKey="true"/>
    <columnMappings featureName="firstName" columnName="first_name" columnType="VARCHAR(255)"  nullable="false" primaryKey="false"/>
    <!-- ... -->
  </tableMappings>
</jpamapping:JpaMappingConfig>
```

### `JpaMappingConfig` attributes

| Attribute | Description |
|-----------|-------------|
| `name` | Unique identifier for the persistence unit. Used as `unitName` on all OSGi services in the pipeline. |
| `targetModelNsUri` | Namespace URI of the EMF `EPackage` defined in `model.ecore`. |
| `dataSource` | Contained `DataSourceConfig` (see below). |
| `tableMappings` | List of `TableMapping` elements, one per EClass. |

### `DataSourceConfig` attributes

| Attribute | Description |
|-----------|-------------|
| `driverClass` | JDBC driver class (only `org.h2.Driver` currently supported). |
| `jdbcUrl` | JDBC connection URL (e.g. `jdbc:h2:mem:mydb;DB_CLOSE_DELAY=-1`). |
| `username` | Database user name. |
| `passwordRef` | Environment variable or secret key from which the password is resolved at runtime. |
| `poolSize` | Maximum connections in the H2 connection pool. |
| `dialect` | SQL dialect — only `H2` is currently supported. |

### `TableMapping` attributes

| Attribute | Description |
|-----------|-------------|
| `className` | Full EMF URI of the EClass: `<nsUri>#//<ClassName>` |
| `tableName` | Name of the DB table. |
| `schema` | DB schema (default `PUBLIC`). |
| `columnMappings` | List of `ColumnMapping` elements. |
| `joinMappings` | List of `JoinMapping` elements for EReference → foreign-key mappings. |

### `ColumnMapping` attributes

| Attribute | Description |
|-----------|-------------|
| `featureName` | Name of the EMF `EAttribute` in the EClass. |
| `columnName` | DB column name. |
| `columnType` | SQL type, e.g. `BIGINT`, `VARCHAR(255)`, `DECIMAL(10,2)`. |
| `nullable` | Whether the column accepts `NULL`. |
| `primaryKey` | Marks this column as the table's primary key (`@Id` in JPA). |

---

## Bundle components — `jpa.watcher`

### DataFolderWatcher

PID: `DataFolderWatcher`  
Configuration policy: **REQUIRE**

Entry point of the pipeline. Configured by a `FileSystemWatcher` pointing at a
root directory that contains `mapping/` and `data/` subfolders.

On activation it reads the `name` attribute from the `.jpamapping` file found in
`<root>/mapping/` and calls `setupPipeline`. If no `.jpamapping` is present yet,
the pipeline starts lazily when the file is created (`ENTRY_CREATE` event).

`setupPipeline` uses `ConfigurationAdmin` to create four factory configurations:

| Factory PID | Watched path | Key properties |
|-------------|-------------|----------------|
| `EMFFileWatcher` | `<root>/mapping` | `io.fs.watcher.path` |
| `JpaMappingFileWatcher` | `<root>/mapping` | `io.fs.watcher.path`, `unitName` |
| `fennec.jpa.CsvDataLoader` | `<root>/data` | `io.fs.watcher.path`, `dataSource.target`, `entityManagerFactory.target` |
| `JpaPersistenceUnitConfigurator` | — | `unitName`, `jpaMappingConfig.target` |

All four share the same random `matcherKey` as their factory instance name and
are deleted together on deactivate.

**Required configuration property:**

| Property | Description |
|----------|-------------|
| `io.fs.watcher.path` | Absolute path of the data root folder (parent of `mapping/` and `data/`) |

---

### JpaMappingFileWatcher

PID: `JpaMappingFileWatcher`  
Configuration policy: **REQUIRE**

Watches a folder (recursively) for `*.jpamapping` files using the daanse
filesystem-watcher whiteboard. Each file is loaded as an EMF resource and its
root `JpaMappingConfig` is registered as an OSGi service with:

| Service property | Value source |
|-----------------|--------------|
| `jpamapping.name` | `JpaMappingConfig.name` |
| `jpamapping.targetNsUri` | `JpaMappingConfig.targetModelNsUri` |
| `jpamapping.folder` | Parent directory of the file |
| `unitName` | From the component configuration |

On `ENTRY_MODIFY` the old service is unregistered and a new one is registered
with the updated config. `DataSourceConfigHandler` and
`JpaPersistenceUnitConfigurator` react to these service lifecycle events to
tear down and rebuild the DataSource and persistence unit.

---

### StandaloneJpaMappingFileWatcher

PID: `StandaloneJpaMappingFileWatcher`  
Configuration policy: **REQUIRE**

A workaround alternative to `JpaMappingFileWatcher`. Uses its own dedicated
`java.nio.file.WatchService` instance rather than the daanse whiteboard, to
avoid a bug in the daanse watcher where two listeners watching the same
directory share the same `WatchKey` map entry — causing `JpaCsvDataImporter`
(which activates last) to silently overwrite the entry and drop all
`.jpamapping` events.

Registers and unregisters `JpaMappingConfig` services with the same properties
as `JpaMappingFileWatcher`. Uses a virtual thread for the watch loop.

Switch `DataFolderWatcher.JPA_MAPPING_FILE_WATCHER_PID` from
`"JpaMappingFileWatcher"` to `StandaloneJpaMappingFileWatcher.PID` to activate
this variant. Revert once the upstream daanse watcher bug is resolved.

---

### JpaCsvDataImporter

PID: `fennec.jpa.CsvDataLoader`  
Configuration policy: **REQUIRE**

Internal CSV data loader. Watches a folder for `*.csv` files and imports their
rows into the H2 database. This is a **data-only** importer: table structure is
owned by EclipseLink (via `JpaPersistenceUnitConfigurator`); this component only
issues `DELETE` + `INSERT` statements.

Two DS reference gates prevent activation before the infrastructure is ready:

- `dataSource` (overridden via `dataSource.target`) — waits for the H2
  `DataSource` registered by `DataSourceConfigHandler`.
- `entityManagerFactory` (overridden via `entityManagerFactory.target`) — waits
  for EclipseLink to finish creating tables and register the
  `EntityManagerFactory`.

On each `ENTRY_CREATE` or `ENTRY_MODIFY` event the component clears the
corresponding table (`DELETE FROM <table>`) and re-imports all rows. On
`ENTRY_DELETE` it clears the table. Referential integrity checks are suspended
around the operation to allow importing tables with foreign keys in any order.

**CSV format:**

| Row | Content | Example |
|-----|---------|---------|
| 1 | Column names (used as field names by the CSV reader) | `id,first_name,salary` |
| 2 | JDBC/SQL type specs — used for `PreparedStatement` binding | `BIGINT,VARCHAR(255),DECIMAL(10,2)` |
| 3+ | Data rows | `1,Ada,95000.00` |

Schema is derived from the CSV file's parent folder relative to the watched
base path: files directly in the base path use no schema qualifier; files in a
subfolder use the subfolder name as the schema (e.g. `finance/invoices.csv` →
`finance.invoices`).

---

### JpaPersistenceUnitConfigurator

PID: `JpaPersistenceUnitConfigurator`  
Configuration policy: **REQUIRE**

Bridges the watcher pipeline to the JPA persistence layer. DS activates this
component once the `JpaMappingConfig` service matching the configured
`jpaMappingConfig.target` filter is registered.

On activation it opens a `ServiceTracker` for the `EPackage` whose
`emf.model.nsuri` matches `JpaMappingConfig.targetModelNsUri`. When the
`EPackage` arrives:

1. `TableMappingConverter` converts the `JpaMappingConfig` table and column
   mappings into a `fennec.persistence.eorm.EntityMappings` object.
2. The `EntityMappings` is registered as an OSGi service (property
   `fennec.jpa.orm.mapping.name = <unitName>`).
3. A `fennec.jpa.EMPersistenceUnit` factory configuration is created, wiring:
   - DataSource: `(unitName=<unitName>)`
   - Mapping: `(fennec.jpa.orm.mapping.name=<unitName>)`

EclipseLink picks up the configuration, creates the `EntityManagerFactory`, and
creates any missing tables via `DynamicSchemaManager.createTables`.

When the `JpaMappingConfig` service is unregistered (e.g. on file modify), DS
deactivates this component, which tears down the `EntityMappings` service and
deletes the `fennec.jpa.EMPersistenceUnit` config. The component is then
re-activated with the new `JpaMappingConfig`.

**Required configuration properties:**

| Property | Description |
|----------|-------------|
| `unitName` | Persistence unit name |
| `jpaMappingConfig.target` | OSGi filter selecting the correct `JpaMappingConfig` (e.g. `(unitName=demo)`) |

---

## Bundle components — `jpa.datasource`

### DataSourceConfigHandler

PID: `DataSourceConfigHandler`  
Always active (immediate DS component).

Listens for `JpaMappingConfig` service registrations. For each config with
`dialect=H2`, reads the embedded `DataSourceConfig` and creates a factory
configuration for `daanse.jdbc.datasource.h2.DataSource` with:

| Config property | Source |
|-----------------|--------|
| `identifier` | JDBC URL stripped of the `jdbc:h2:` prefix |
| `username` | `DataSourceConfig.username` |
| `.password` | `DataSourceConfig.passwordRef` (resolved from environment) |
| `unitName` | `unitName` service property of the `JpaMappingConfig` |

The resulting H2 `DataSource` OSGi service carries `unitName=<name>`, which is
the filter used by `JpaCsvDataImporter` and `JpaPersistenceUnitConfigurator` to
locate the right DataSource.

When the `JpaMappingConfig` service disappears the corresponding DataSource
configuration is deleted and the DataSource service goes away.

> **Note:** Only H2 dialect is currently supported.

---

## Bundle components — `jpa.rest`

### JpaDataResource

Path prefix: `/jpa/data`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/jpa/data/{eClassName}` | Returns all instances of the given EClass. Supports `?limit=<n>` (default 100) and `?ePackageUri=<uri>` to disambiguate when the same class name appears in multiple mappings. |
| `GET` | `/jpa/data/{eClassName}/{id}` | Returns a single instance by its primary key. The `id` is coerced to the primary key's column type. |

Resolution logic: the resource looks up a `JpaMappingConfig` service that has a
`TableMapping` for `eClassName`, derives the `unitName`, then fetches the
matching `EntityManagerFactory` service (filter `osgi.unit.name=<unitName>`) and
executes a JPQL query through it. Results are returned as EMF objects serialised
as XML or JSON.

HTTP status codes:

| Code | Meaning |
|------|---------|
| 200 | Success |
| 204 | No objects found |
| 400 | `id` cannot be parsed to the primary key type |
| 404 | No `JpaMappingConfig` or persistence unit found |
| 409 | Multiple `JpaMappingConfig` services match; provide `?ePackageUri` |
| 500 | Internal error |

---

### JpaConnectionResource

Path prefix: `/jpa`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/jpa/test` | Tests the DataSource connection described in a `JpaMappingConfig` XMI body. Times out after 5 s. |
| `GET` | `/jpa/test/{dataSourceName}` | Tests a DataSource already registered in the OSGi registry by name. |
| `GET` | `/jpa/hello` | Liveness check — returns `"Hello"`. |

---

## Full pipeline walkthrough

Sequence after a correctly structured root folder is placed under the watched
path:

```
1. FileSystemWatcher notifies DataFolderWatcher.handleBasePath(<root>)
2. DataFolderWatcher reads name="demo" from <root>/mapping/mapping.jpamapping
3. DataFolderWatcher creates four factory configurations via ConfigurationAdmin

4. EMFFileWatcher activates → scans <root>/mapping/ → loads model.ecore
   → registers EPackage(nsUri="http://example.org/demo/1.0") as OSGi service

5. JpaMappingFileWatcher activates → scans <root>/mapping/ → loads mapping.jpamapping
   → registers JpaMappingConfig("demo") as OSGi service (unitName="demo")

6. DataSourceConfigHandler (always running) reacts to the new JpaMappingConfig
   → reads DataSourceConfig → creates daanse.jdbc.datasource.h2.DataSource config
   → H2 DataSource registered as OSGi service (unitName="demo")

7. JpaPersistenceUnitConfigurator activates (jpaMappingConfig.target=(unitName=demo) met)
   → opens ServiceTracker for EPackage(nsUri=targetModelNsUri)
   → EPackage from step 4 arrives
   → TableMappingConverter.toEntityMappings(ePackage, jpaMappingConfig)
   → registers EntityMappings as OSGi service (fennec.jpa.orm.mapping.name=demo)
   → creates fennec.jpa.EMPersistenceUnit config, wiring:
       dataSource.target=(unitName=demo)
       mapping.target=(fennec.jpa.orm.mapping.name=demo)

8. EclipseLink (EMPersistenceUnit) starts
   → creates EntityManagerFactory (osgi.unit.name=demo)
   → creates any missing tables in H2 (DynamicSchemaManager.createTables)

9. JpaCsvDataImporter activates (DataSource + EntityManagerFactory both present)
   → scans <root>/data/ recursively:
       employees.csv, products.csv → default schema
       finance/invoices.csv → schema "finance"
   → for each CSV file: DELETE FROM <table>, then INSERT all data rows

10. REST client: GET /jpa/data/Employee
    → JpaDataResource resolves JpaMappingConfig + EntityManagerFactory by unitName
    → executes JPQL: SELECT e FROM Employee e
    → returns EObjects as XML or JSON
```
