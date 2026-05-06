# org.eclipse.fennec.data.atlas.jpa.watcher

This bundle contains the file-watcher components that bootstrap the full
CSV-to-REST pipeline. Dropping a folder with a `.jpamapping` file into the
watched path is enough to have the data accessible as EMF instances over REST.

## Pipeline overview

```
data folder on disk
  ├── model.ecore          ← EMF metamodel (user-authored)
  ├── mapping.jpamapping   ← JpaMappingConfig (user-authored)
  ├── employees.csv        ← root folder → default DB schema
  ├── products.csv
  ├── finance/
  │   ├── invoices.csv     ← subfolder name → DB schema name
  │   └── payments.csv
  └── hr/
      └── contracts.csv
          │
          ▼
  DataFolderWatcher        (this bundle)
  ┌────────┬──────────────────┬──────────────────┬──────────────────┐
  │        │                  │                  │                  │
  ▼        ▼                  ▼                  ▼                  ▼
EMF-    JpaMapping-      CsvData-           JpaModel-
File-   FileWatcher      Importer           Setup
Watcher (this bundle)    (daanse)           (this bundle)
  │        │                  │                  │
  │  JpaMappingConfig         │ INSERT rows       │
  │  (OSGi service)           │ into H2           │
  │        │                  │                  │
  │        ▼                  │                  │
  │  DataSourceConfig-        │            EntityMappings
  │  Handler                  │            (OSGi service)
  │  (jpa.datasource)         │                  │
  │        │                  │                  │
  ▼        ▼                  ▼                  ▼
EPackage  H2 DataSource  H2 tables +       EMPersistenceUnit
(OSGi)   (OSGi service)  data rows         config (EclipseLink)
                                                  │
                                                  ▼
                                          GET /jpa/data/{eclass}
                                          (jpa.rest bundle)
```

## Bundle components

### DataFolderWatcher

PID: `DataFolderWatcher`  
Configuration policy: **REQUIRE**

The entry point of the pipeline. It is itself configured by a
`FileSystemWatcher` pointing at a root directory. When activated, it scans the
folder for a `.jpamapping` file, reads the `name` attribute from its XML root
element, and calls `setupPipeline`. If no `.jpamapping` is present yet the
pipeline is started lazily when the file appears (via `handlePathEvent`).

`setupPipeline` uses `ConfigurationAdmin` to create four factory configurations:

| Factory PID | Key properties set |
|-------------|-------------------|
| `EMFFileWatcher` | `io.fs.watcher.path` |
| `JpaMappingFileWatcher` | `io.fs.watcher.path`, `unitName` |
| `org.eclipse.daanse.jdbc.db.importer.csv.CsvDataImporter` | `io.fs.watcher.path`, `dataSource.target` |
| `JpaModelSetup` | `unitName`, `jpaMappingConfig.target` |

All four configurations share the same random `matcherKey` used as the factory
instance name, and are deleted together on `deactivate`.

**Required configuration property:**

| Property | Description |
|----------|-------------|
| `io.fs.watcher.path` | Absolute path of the data folder to watch |

---

### JpaMappingFileWatcher

PID: `JpaMappingFileWatcher`  
Configuration policy: **REQUIRE**

Watches a folder (recursively) for `*.jpamapping` files. Each file is loaded
as an EMF resource and its root `JpaMappingConfig` object is registered as an
OSGi service with the following properties:

| Service property | Value source |
|-----------------|--------------|
| `jpamapping.name` | `JpaMappingConfig.name` |
| `jpamapping.targetNsUri` | `JpaMappingConfig.targetModelNsUri` |
| `jpamapping.folder` | Parent directory of the file |
| `unitName` | From the component configuration |

File create/modify/delete events are debounced (1 s timer) to handle rapid
filesystem events. On modification the old service is unregistered and a new
one is registered with updated properties.

**Required configuration properties:**

| Property | Description |
|----------|-------------|
| `io.fs.watcher.path` | Folder to watch |
| `unitName` | Persistence unit name (echoed onto every registered service) |

---

### JpaModelSetup

PID: `JpaModelSetup`  
Configuration policy: **REQUIRE**

Bridges the watcher pipeline to the JPA persistence layer. It is activated by
DS once the `JpaMappingConfig` service for the configured `unitName` is
present (bound via `jpaMappingConfig.target`).

On activation it opens a `ServiceTracker` for the `EPackage` whose
`emf.model.nsuri` matches `JpaMappingConfig.targetModelNsUri`. When the
`EPackage` arrives:

1. `TableMappingConverter` converts the `JpaMappingConfig` table and column
   mappings into a `fennec.persistence.eorm.EntityMappings` object.
2. The `EntityMappings` is registered as an OSGi service (property
   `fennec.jpa.orm.mapping.name = <unitName>`).
3. A `fennec.jpa.EMPersistenceUnit` factory configuration is created, wiring
   together the DataSource and the EntityMappings so that EclipseLink can start
   serving JPA queries.

Everything is torn down cleanly when the `EPackage` disappears or this
component is deactivated.

**Required configuration properties:**

| Property | Description |
|----------|-------------|
| `unitName` | Persistence unit name |
| `jpaMappingConfig.target` | OSGi filter selecting the correct `JpaMappingConfig` service (e.g. `(unitName=demo-mapping)`) |

---

## Full pipeline walkthrough

This is the sequence of events after a data folder with all required files is
placed under the watched root:

```
1. FileSystemWatcher notifies DataFolderWatcher.handleBasePath(folder)
2. DataFolderWatcher reads name="demo-mapping" from mapping.jpamapping
3. DataFolderWatcher creates four factory configs via ConfigurationAdmin

4. EMFFileWatcher activates → scans folder → loads model.ecore
   → registers EPackage(nsUri="http://example.org/jpa/demo/1.0") as OSGi service

5. JpaMappingFileWatcher activates → scans folder → loads mapping.jpamapping
   → registers JpaMappingConfig("demo-mapping") as OSGi service
     with unitName="demo-mapping"

6. DataSourceConfigHandler (jpa.datasource, always running) reacts to the new
   JpaMappingConfig service → reads DataSourceConfig → calls
   DataSourceConfigHelper.createH2Config → registers H2 DataSource
     with unitName="demo-mapping" in its service properties

7. CsvDataImporter activates → scans folder recursively:
   - employees.csv, products.csv → default schema
   - finance/invoices.csv, finance/payments.csv → schema "finance"
   - hr/contracts.csv → schema "hr"
   CSV row 1 = column names, row 2 = data types, row 3+ = data rows.
   Tables are created and rows are batch-inserted into the H2 database.

8. JpaModelSetup activates (JpaMappingConfig + jpaMappingConfig.target met)
   → opens ServiceTracker for EPackage(nsUri=targetModelNsUri)
   → EPackage from step 4 arrives
   → TableMappingConverter.toEntityMappings(ePackage, jpaMappingConfig)
   → registers EntityMappings as OSGi service
   → creates fennec.jpa.EMPersistenceUnit factory config, wiring:
       - dataSource with (unitName=demo-mapping)
       - mapping with (fennec.jpa.orm.mapping.name=demo-mapping)

9. EclipseLink (EMPersistenceUnit) starts → EntityManagerFactory available

10. REST client: GET /jpa/data/Employee
    → JpaDataResource looks up JpaMappingConfig + EntityManagerFactory
      by unitName / nsUri
    → executes JPQL query via EntityManager
    → returns EObjects serialized as XMI or JSON
```

## Data folder layout

```
<root>/
├── model.ecore               Required. Defines the EMF metamodel.
├── mapping.jpamapping        Required. JpaMappingConfig root document.
├── <table>.csv               Optional. One file per EClass in the default schema.
└── <schema>/
    └── <table>.csv           Optional. Files in subfolders map to named schemas.
```

The `.jpamapping` file must be an XMI document whose root element has at
minimum:

```xml
<jpamapping:JpaMappingConfig
    xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
    name="<unit-name>"
    targetModelNsUri="<nsUri of model.ecore>">
  <dataSource driverClass="org.h2.Driver"
              jdbcUrl="jdbc:h2:mem:<dbname>"
              username="sa"
              passwordRef="DB_PASSWORD"
              poolSize="5"
              dialect="H2"/>
  <!-- one TableMapping per EClass / CSV file -->
</jpamapping:JpaMappingConfig>
```

## CSV format

Each CSV file uses **two fixed header rows** followed by data rows:

| Row | Content | Example |
|-----|---------|---------|
| 1 | Column names | `id,firstName,lastName,salary` |
| 2 | Java/EMF data types | `Long,String,String,BigDecimal` |
| 3+ | Data | `1,Max,Mustermann,75000.00` |

The folder that contains a CSV file determines the database schema:
- Root folder → default schema (no `schema` attribute on `TableMapping`)
- `finance/` subfolder → schema `finance`

## Related bundles

| Bundle | Role |
|--------|------|
| `org.eclipse.fennec.data.atlas.jpa.datasource` | `DataSourceConfigHandler` reacts to `JpaMappingConfig` services and creates H2 `DataSource` configurations |
| `org.eclipse.daanse.jdbc.db.importer.csv` | `CsvDataImporter` reads CSV files and populates the H2 database |
| `org.eclipse.fennec.data.atlas.mapping.model` | EMF metamodel for `JpaMappingConfig`, `TableMapping`, `ColumnMapping`, `JoinMapping` |
| `org.eclipse.fennec.data.atlas.jpa.rest` | `JpaDataResource` (`GET /jpa/data/{eclass}`) and `JpaConnectionResource` (`POST /jpa/connection/test`) |
| `org.eclipse.fennec.model.atlas` | `EMFFileWatcher` that loads `.ecore` files and registers `EPackage` services |
