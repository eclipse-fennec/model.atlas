# JPA Integration — Overview

This document describes the JPA pipeline that turns a folder of `.ecore` /
`.eorm` / `.csv` files on disk into a queryable REST endpoint backed by
EclipseLink and H2.

The two components that live in **this** bundle are `DataFolderWatcher` and
`EormFileWatcher`. Everything else (EMF model loading, H2 DataSource, CSV
import, persistence unit) is delegated to existing components from
`org.eclipse.fennec.model.atlas`, `org.eclipse.daanse.*` and
`org.eclipse.fennec.persistence.*`. `DataFolderWatcher` wires them together
via OSGi factory configurations.

## Bundles involved

| Bundle | Role |
|--------|------|
| `org.eclipse.fennec.data.atlas.jpa.watcher` | `DataFolderWatcher` (pipeline entry point), `EormFileWatcher` (registers `EntityMappings` services) |
| `org.eclipse.fennec.model.atlas` | `EMFFileWatcher` — loads `.ecore` files and registers `EPackage` services |
| `org.eclipse.fennec.persistence.orm` | EORM EMF model: `EntityMappings`, `Entity`, `Id`, `Basic`, `Column`, … |
| `org.eclipse.fennec.persistence.eclipselink` | `EntityMappingPersistenceUnitConfigurator` (`fennec.jpa.EMPersistenceUnit`) — builds the EclipseLink `EntityManagerFactory` from a registered `EntityMappings` + DataSource |
| `org.eclipse.daanse.jdbc.datasource.h2` | H2 `DataSource` factory |
| `org.eclipse.daanse.jdbc.db.importer.csv` | `CsvDataImporter` (`fennec.jpa.CsvDataLoader`) — watches a folder for `.csv` files and imports them |
| `org.eclipse.daanse.io.fs.watcher.watchservice` | Filesystem watcher whiteboard delivering `handleBasePath` / `handleInitialPaths` / `handlePathEvent` to listeners |
| `org.eclipse.fennec.data.atlas.jpa.rest` | `JpaDataResource` — REST endpoints for querying the loaded data |

## Data folder layout

```
<root>/
├── mapping/
│   ├── model.ecore           Required. EMF metamodel defining the EClasses.
│   └── *.eorm                Required. XMI document of EntityMappings (one or more).
└── data/
    ├── <table>.csv           Optional. CSV files in the root → default schema.
    └── <schema>/
        └── <table>.csv       Optional. CSV files in subfolders → named schema.
```

`DataFolderWatcher` points `EMFFileWatcher` and `EormFileWatcher` at
`<root>/mapping/`, and the daanse `CsvDataImporter` at `<root>/data/`.

CSV format (consumed by `CsvDataImporter`):

| Row | Content | Example |
|-----|---------|---------|
| 1 | Column names (must match the DB column names declared in the `.eorm`) | `id,first_name,salary` |
| 2 | JDBC type names — used for `PreparedStatement` binding | `BIGINT,VARCHAR(255),DECIMAL(10,2)` |
| 3+ | Data rows | `1,Ada,95000.00` |

Schema is derived from the CSV file's parent folder relative to the watched
`data/` directory: files directly in `data/` are in the default schema; files
in a subfolder use the subfolder name as the schema (e.g. `finance/invoices.csv`
→ `finance.invoices`).

## The `.eorm` file

An `.eorm` is the XMI serialisation of a `org.eclipse.fennec.persistence.eorm.EntityMappings`
(generated EMF model in `org.eclipse.fennec.persistence.orm`). Minimum structure:

```xml
<eorm:EntityMappings xmi:version="2.0"
    xmlns:xmi="http://www.omg.org/XMI"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
    xmlns:eorm="https://eclipse.org/fennec/persistence/eorm/1.0.0"
    package="<nsUri of model.ecore>"
    name="<unit-name>">

  <entity access="FIELD" name="<EClassName>">
    <accessibleObject xsi:type="eorm:EClassObject" name="<qualified-name>">
      <eclass href="<nsUri>#//<EClassName>"/>
    </accessibleObject>
    <table name="<table>" schema="<schema?>"/>
    <attributes>
      <id name="<attr>"> <feature .../> <column .../> </id>
      <basic name="<attr>" fetch="EAGER" optional="..."> <feature .../> <column .../> </basic>
      <!-- … -->
    </attributes>
    <class xsi:type="ecore:EClass" href="<nsUri>#//<EClassName>"/>
  </entity>
  <!-- more entities -->
</eorm:EntityMappings>
```

For the full attribute/column/relationship vocabulary, see the upstream model
generated under `org.eclipse.fennec.persistence.orm/src-gen/.../eorm/` — this
project doesn't add anything on top of it.

**Authoring gotchas** (learned the hard way during integration):

- **Every `<id>` needs a sibling `<basic>` for the same attribute.** EclipseLink
  treats the `<id>` as a read-only marker and looks for a separate non-read-only
  mapping for the same column. Without it, EMF creation fails with
  `EclipseLink-46: There should be one non-read-only mapping defined for the
  primary key field …`. Same `<feature>` href and same `<column>` definition in
  both elements.
- **Every `href="<nsUri>#//<EClassName>/<attr>"` must resolve.** If you reference
  an attribute that doesn't exist in the `.ecore`, `EcoreUtil.resolveAll` silently
  leaves the proxy. EclipseLink then hits an NPE on `getEAttributeType()` deep in
  `AttributeConfigurator`. Worth a startup-time validator pass.
- **EReferences referenced from the `.eorm` must exist in the `.ecore`.** If you
  declare `<oneToMany name="contracts">` against `Employee/contracts`, the ecore
  must have an `EReference contracts`. Same proxy-resolution failure mode as above.

## Pipeline overview

```
DataFolderWatcher  (this bundle)
  │   For every sub-config below, in addition to the properties listed,
  │   DataFolderWatcher also sets:
  │     jpa.root.folder = <rootFolder>     (the last segment of the watched path)
  │   This is the value REST clients pass as {rootFolderName} in the URL.
  │
  ├── creates ──► daanse H2 DataSource           file.context.matcher=<matcherKey>
  │                                              jpa.root.folder=<rootFolder>
  │
  ├── creates ──► EPackageRegistry               emf.resourceSetFactoryName=<matcherKey>
  │               + ResourceSetFactory           jpa.root.folder=<rootFolder>
  │                                              (consumed by EMFFileWatcher and
  │                                               by JpaDataResourceFilter)
  │
  ├── creates ──► EMFFileWatcher                 watches mapping/ (pattern: .*\.ecore)
  │               └── registers EPackage         emf.nsURI=<targetModelNsUri>
  │                                              jpa.root.folder=<rootFolder>
  │
  ├── creates ──► EormFileWatcher  (this bundle) watches mapping/ (pattern: .*\.eorm)
  │               └── once the matching EPackage is in the registry:
  │                   EcoreUtil.resolveAll → registers EntityMappings
  │                                          eorm.name=<name>
  │                                          eorm.targetNsUri=<package>
  │                                          file.context.matcher=<matcherKey>
  │                                          fennec.jpa.orm.mapping.name=<rootFolder>
  │                                          jpa.root.folder=<rootFolder>
  │
  ├── creates ──► daanse CsvDataImporter         watches data/ (kinds: CREATE|MODIFY|DELETE)
  │               targets the H2 DataSource by jpa.root.folder
  │               and the EntityManagerFactory by osgi.unit.name
  │
  └── creates ──► fennec.jpa.EMPersistenceUnit   persistenceUnitName=<rootFolder>
                                                  targets DataSource by jpa.root.folder
                                                  and EntityMappings by fennec.jpa.orm.mapping.name
                  └── EclipseLink configures dynamic types from the EntityMappings
                      and registers EntityManagerFactory  osgi.unit.name=<matcherKey>
                                                          │
                                                          ▼
                              GET /jpa/{rootFolderName}/data/{eClassName}        (jpa.rest)
                              GET /jpa/{rootFolderName}/data/{eClassName}/{id}
```

All sub-configurations share a single random `matcherKey` (a UUID) — that's
how the pipeline wires its own components together via OSGi targeted references
without colliding with any other pipeline running in the same framework. The
`jpa.root.folder` property (the last segment of the watched root path) is set
on every service so that `JpaDataResourceFilter` can look up the right
`ResourceSet` / `EntityMappings` / `EntityManagerFactory` by the path parameter
of the incoming HTTP request. On `DataFolderWatcher.deactivate()`, all
sub-configurations are deleted, which tears the entire pipeline down.

## Components owned by this bundle

### DataFolderWatcher

PID: `DataFolderWatcher`
Configuration policy: **REQUIRE**
`@FileSystemWatcherListenerProperties(recursive = false)`

Entry point of the pipeline. Configured by a `FileSystemWatcher` pointing at a
root directory that contains `mapping/` and `data/` subfolders.

On activation (`handleBasePath`) it scans `<root>/mapping/` for an `.eorm` file:

- If at least one is present, calls `setupPipeline()`.
- Otherwise logs INFO and waits for an `.eorm` file to appear; on
  `handlePathEvent(*, ENTRY_CREATE | ENTRY_MODIFY)` for an `.eorm`, the
  pipeline is started lazily.

`setupPipeline()` creates the following factory configurations via
`ConfigurationAdmin`. Every one of them, in addition to the properties listed
below, also carries `jpa.root.folder=<rootFolder>` so that the REST filter can
look the resulting services up by the `{rootFolderName}` path segment.

| Factory PID | Watched path / target | Key properties set |
|-------------|----------------------|--------------------|
| `daanse.jdbc.datasource.h2.DataSource` | file H2 under `./generated/tmp/databases/<matcherKey>` | `identifier`, `file.context.matcher`, `database.to.upper=false`, `jpa.root.folder` |
| `EPackageRegistry` (`EMF`) | — | `emf.resourceSetFactoryName=<matcherKey>`, `ePackageConfigurator.target=(jpa.root.folder=<rootFolder>)`, `jpa.root.folder` |
| `ResourceSetFactory` (`EMF`) | — | `ePackageRegistry.target=(emf.resourceSetFactoryName=<matcherKey>)`, `file.context.matcher`, `jpa.root.folder` |
| `EMFFileWatcher` | `<root>/mapping` | `io.fs.watcher.path`, `io.fs.watcher.pattern=.*\.ecore`, `file.context.matcher`, `resourceSet.target=(file.context.matcher=<matcherKey>)`, `jpa.root.folder` |
| `JpaMappingFileWatcher` (`EormFileWatcher`) | `<root>/mapping` | `io.fs.watcher.path`, `io.fs.watcher.pattern=.*\.eorm`, `file_context_matcher`, `jpa_root_folder` |
| `fennec.jpa.CsvDataLoader` | `<root>/data` | `io.fs.watcher.path`, `io.fs.watcher.kinds=CREATE,DELETE,MODIFY`, `file.context.matcher`, `dataSource.target=(jpa.root.folder=<rootFolder>)`, `entityManagerFactory.target=(osgi.unit.name=<matcherKey>)`, `jpa.root.folder` |
| `fennec.jpa.EMPersistenceUnit` | — | `fennec.jpa.persistenceUnitName=<rootFolder>`, `fennec.jpa.dataSource.target=(jpa.root.folder=<rootFolder>)`, `fennec.jpa.mapping.target=(fennec.jpa.orm.mapping.name=<rootFolder>)`, `jpa.root.folder` |

Note the explicit `io.fs.watcher.pattern` overrides on the EMF and EORM
watchers: the same `mapping/` folder holds both `.ecore` and `.eorm` files, and
without a pattern the EMF watcher would also load `.eorm` files (whose resource
factory is registered for the `eorm` extension), ending up with two
`EntityMappings` services per file. Likewise the `io.fs.watcher.kinds` override
on the CSV importer config: the component defaults to `ENTRY_MODIFY` only, so
without the override `Files.delete` and initial-scan-created CSVs wouldn't
reach `handlePathEvent`.

**Required configuration property:**

| Property | Description |
|----------|-------------|
| `io.fs.watcher.path` | Absolute path of the data root folder (parent of `mapping/` and `data/`) |

### EormFileWatcher

PID: `JpaMappingFileWatcher` (`WatcherConstants.PID_ENTITY_MAPPINGS_FILE_WATCHER` — the PID literal is unchanged from the old jpamapping watcher for backward compatibility).
Configuration policy: **REQUIRE**
`@FileSystemWatcherListenerProperties(pattern = ".*.eorm", recursive = true)`

Watches a folder for `.eorm` files, loads each into an EMF resource, and
registers the resulting `EntityMappings` as an OSGi service.

Because an `EntityMappings` references the user model via proxy URIs
(`<eclass href="…#//Foo"/>`, `<feature href="…#//Foo/bar"/>`), proxy resolution
needs the target `EPackage` to be reachable. The watcher handles this in two
modes:

1. **No entities** (e.g. minimal test fixtures, or eorms that don't reference an
   EClass): `EcoreUtil.resolveAll(resource)` + register immediately.
2. **Has entities**: open a `ServiceTracker` filtered by
   `(&(objectClass=EPackage)(emf.nsURI=<package>))`. When the matching EPackage
   service appears, call `EcoreUtil.resolveAll(mappings)` and register. If the
   EPackage later goes away, the registered `EntityMappings` is unregistered.

After registration, the loading resource is detached from the (shared) eorm
`ResourceSet` (`resource.getContents().clear()` + `getResources().remove(...)`).
The `EntityMappings` stays alive in our `registrations` map and in the OSGi
registry, with its cross-references resolved against the global EPackage
registry. Detaching keeps the resource set clean across many load/reload cycles.

**Registered service properties:**

| Property | Source |
|----------|--------|
| `eorm.name` | `EntityMappings.getName()` |
| `eorm.targetNsUri` | `EntityMappings.getPackage()` |
| `eorm.folder` | Parent directory of the file |
| `file.context.matcher` | Component config (set by `DataFolderWatcher`) |
| `fennec.jpa.orm.mapping.name` | Component config — currently the root-folder name; this is the property `EntityMappingPersistenceUnitConfigurator` filters on by default |
| `jpa.root.folder` | Component config (set by `DataFolderWatcher`) — the value REST clients use as `{rootFolderName}` in `/jpa/{rootFolderName}/data/...`. `JpaDataResourceFilter` looks up the `EntityMappings` by this property. |

**Event semantics:**

| Event | Behaviour |
|-------|-----------|
| `ENTRY_CREATE` | Load + register |
| `ENTRY_MODIFY` | Unload existing registration for the URI, then load + register again |
| `ENTRY_DELETE` | Unload registration |

**Required configuration properties:**

| Property | Description |
|----------|-------------|
| `file_context_matcher` | Opaque identifier (a UUID, when created by `DataFolderWatcher`) used to link this watcher's `EntityMappings` to the other components in the same pipeline |
| `jpa_root_folder` | Name of the data root folder. Mirrored onto the registered `EntityMappings` as the `jpa.root.folder` service property and used (via `fennec.jpa.orm.mapping.name=<jpa_root_folder>`) by the persistence-unit configurator to bind the right mapping. |

## External components

This bundle does **not** define the components below — they're documented here
because `DataFolderWatcher` configures them and they're load-bearing for the
end-to-end flow.

### `EMFFileWatcher` (`org.eclipse.fennec.model.atlas`)

Loads `.ecore` (and other EMF) files, registers each top-level `EPackage` as an
OSGi service, and stores it in `EPackageRegistryImpl.INSTANCE` so other
`ResourceSet`s can resolve proxies against it. We restrict it to `.ecore` here
via `io.fs.watcher.pattern=.*\.ecore`.

### `daanse.jdbc.datasource.h2.DataSource` (`org.eclipse.daanse.jdbc.datasource.h2`)

Standard daanse factory that registers a `javax.sql.DataSource` from an H2 URL
described by the supplied `identifier` and filesystem mode. Config properties
flow through to OSGi service properties, including our `file.context.matcher`.

### `fennec.jpa.CsvDataLoader` (`org.eclipse.daanse.jdbc.db.importer.csv`)

Watches a folder for `.csv` files. On `ENTRY_CREATE` / `ENTRY_MODIFY` it
**drops** the existing table (if any), **creates** the table from the CSV header
+ types row, and inserts all data rows. On `ENTRY_DELETE` it drops the table.
Schema is derived from the file's parent folder relative to the watched root.

> The CSV importer thus owns the table DDL, not just the row data. This
> overlaps with EclipseLink's own `addETypes(true, true, …)` DDL pass (see
> below); the two converge on a consistent state but log race-induced
> "table/schema already exists" errors on the loser. An upstream PR to
> `fennec-persistence-jpa` to expose `createMissingTables=false` on the
> persistence-unit OCD is the planned fix.

### `fennec.jpa.EMPersistenceUnit` (`org.eclipse.fennec.persistence.eclipselink`)

`EntityMappingPersistenceUnitConfigurator`. References:

- `fennec.jpa.dataSource` — H2 DataSource, targeted via
  `(jpa.root.folder=<rootFolder>)`.
- `fennec.jpa.mapping` — `EntityMappings`, targeted via
  `(fennec.jpa.orm.mapping.name=<rootFolder>)`.
- `fennec.jpa.converter` — `ConverterService` (singleton from `org.eclipse.fennec.persistence`).

Builds the EclipseLink `EntityManagerFactory` asynchronously on a single-threaded
executor (`AbstractPersistenceUnitConfigurator.doActivate` submits via a
`PromiseFactory`). On success the `EntityManagerFactory` is registered with
`osgi.unit.name=<persistenceUnitName>` — and `DataFolderWatcher` configures
`persistenceUnitName=<rootFolder>`, so the unit name matches the value REST
clients pass as `{rootFolderName}`. (`JpaDataResourceFilter` indexes
`EntityManagerFactory` services by `osgi.unit.name` exactly for this reason.)
On failure, the SEVERE log line under that logger is the single best
diagnostic.

## Full pipeline walkthrough

Sequence after a correctly structured root folder is placed under the watched
path:

Assume the watched root folder is `…/<rootFolder>/` — e.g. `…/demo/` so that
`<rootFolder> = "demo"`.

```
1. FileSystemWatcher notifies DataFolderWatcher.handleBasePath(<root>)
2. DataFolderWatcher confirms <root>/mapping/*.eorm exists, generates matcherKey (UUID),
   captures rootFolder = <root>.getFileName(), and creates the factory configurations
   via ConfigurationAdmin — every one of them tagged with jpa.root.folder=<rootFolder>

3. daanse H2 DataSource activates (config-only) → javax.sql.DataSource registered
   (file.context.matcher=<matcherKey>, jpa.root.folder=<rootFolder>)

4. EMFFileWatcher activates → scans <root>/mapping/ (pattern .*\.ecore)
   → loads model.ecore → registers EPackage(nsURI="http://example.org/jpa/demo/1.0",
                                             jpa.root.folder=<rootFolder>)
   → also stuffs it into EPackageRegistryImpl.INSTANCE

5. EormFileWatcher activates → scans <root>/mapping/ (pattern .*\.eorm)
   → loads mapping.eorm → since it has entities, opens a ServiceTracker for
     (emf.nsURI=http://example.org/jpa/demo/1.0)
   → tracker fires (the EPackage from step 4 is already there)
   → EcoreUtil.resolveAll(mappings) → register EntityMappings
     (eorm.name="<name>", file.context.matcher=<matcherKey>,
      fennec.jpa.orm.mapping.name=<rootFolder>,
      jpa.root.folder=<rootFolder>)
   → resource detached from the eorm ResourceSet

6. fennec.jpa.EMPersistenceUnit activates (DataSource + EntityMappings both bound
   via (jpa.root.folder=<rootFolder>) / (fennec.jpa.orm.mapping.name=<rootFolder>))
   → AbstractPersistenceUnitConfigurator submits configure() to its executor
   → EclipseLink builds dynamic types, currently also runs
     EDynamicHelper.addETypes(true, true, …) which CREATE SCHEMA / CREATE TABLE
   → EntityManagerFactory registered (osgi.unit.name=<rootFolder>)

7. daanse CsvDataImporter activates (DataSource + EntityManagerFactory both bound)
   → scans <root>/data/ recursively:
       employees.csv, products.csv → default schema
       finance/invoices.csv         → schema "finance"
       hr/contracts.csv             → schema "hr"
   → for each CSV: DROP TABLE → CREATE TABLE (from CSV header) → INSERT rows

8. REST client: GET /jpa/<rootFolder>/data/Employee
    → JpaDataResourceFilter looks up ResourceSet, EntityMappings, and
      EntityManagerFactory keyed by <rootFolder> (using jpa.root.folder for
      ResourceSet/EntityMappings, and osgi.unit.name for EntityManagerFactory)
    → validates that the EPackage in the ResourceSet declares an EClassifier
      named "Employee" (and that any ?ePackageUri= matches EntityMappings.getPackage())
    → stashes the resolved EntityManagerFactory and EntityMappings on the
      ContainerRequestContext
    → JpaDataResource executes JPQL: SELECT e FROM Employee e
    → returns EObjects as XML or JSON
```

## Known limitations and follow-ups

- **DDL race between EclipseLink and CsvDataImporter.** Both create schemas
  and tables for the entities defined in the `.eorm`. The loser logs a
  benign-but-noisy `object already exists` (H2's `CREATE SCHEMA IF NOT EXISTS`
  isn't atomic; H2 issue [#4188](https://github.com/h2database/h2database/issues/4188))
  or `table already exists`. The end state is consistent (CSV importer wins
  semantically because it drops + recreates). Planned fix: an upstream PR to
  `fennec-persistence-jpa` exposing `createMissingTables` / `generateFKConstraints`
  on the persistence-unit OCD; `DataFolderWatcher` will then set
  `createMissingTables=false`.

- **Initial-scan event race in daanse fs.watcher.** `FileWatcherRunable.registerPath`
  delivers `handleInitialPaths` to the listener **before** registering the
  `WatchKey` with the underlying `WatchService`. Any filesystem change in that
  window goes undetected. Two PRs already drafted upstream:
  (1) apply the pattern to `handleInitialPaths` (delivered), and
  (2) swap the order so the `WatchKey` is live before the initial scan.

- **`WorkspaceFileWatcher`-style discovery.** Today every `DataFolderWatcher`
  instance must be configured by hand (factory config with `io.fs.watcher.path`).
  A wrapper that scans a workspace root and registers a `DataFolderWatcher`
  per `<folder>/mapping/*.eorm` it finds would remove that boilerplate. Out of
  scope for this bundle as of writing.
