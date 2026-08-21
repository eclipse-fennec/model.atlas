# Model Atlas - Initial Model Bootstrap

This bundle provides the **`InitialModelLoader`** — a one-shot loader that scans a
configured folder **once on startup** and registers the models it finds as OSGi
services, so they are immediately available through the Atlas scope (and every
scope that inherits from it).

It is intended for seeding a fresh Model Atlas instance with a baseline set of
models, typically via a file mount baked into — or attached to — a Docker image.
Unlike the runtime `EMFFileWatcher` (which continuously watches the `workspace/`
folder), this loader runs exactly once when its component is activated and does
**not** react to later changes on disk.

## What it does

On activation the loader walks the configured folder **recursively** and processes
files by extension:

| Extension | Handling |
|-----------|----------|
| `.ecore` | Loaded as an Ecore model; the root `EPackage` (and any sub-packages) are registered. |
| `.jsonschema` | Converted to an `EPackage` via the Fennec JSON-Schema codec (the schema's `definitions` are expanded into `EClassifier`s). |
| `.qvto` | Registered as a `QVTModelTransformator` factory configuration (it is **not** loaded as a model). |

All other file types are ignored.

Files below the reserved **`scopes/`** folder follow different rules — see
[Seeding scopes](#seeding-scopes) below. Everything else is registered globally
(the "atlas" content).

For every registered `EPackage` the loader publishes two OSGi services:

- the `EPackage` itself, and
- a `DynamicEPackageConfigurator`

with the standard EMF service properties (`emf.name`, `emf.model.nsURI`,
`emf.model.dynamic`). Any key/value pairs found in a `properties` `EAnnotation`
on the package are added as additional service properties.

Because the packages are registered in the static EMF `EPackage.Registry`, they
appear in the read-only **atlas-schema-registry** and are therefore visible to
all scopes that have `atlas` as an ancestor. See the
[user guide](../docs/user-guide.md#bootstrapping-initial-models) for how this fits
into the scope/registry model.

### Cross-references and sub-packages

- **Sub-packages** of a loaded `EPackage` are registered as separate `EPackage`
  services in their own right.
- **Cross-references between files** are resolved. The loader seeds the package
  registries and aligns each resource URI to the package `nsURI` *before*
  resolving, so several `.ecore`/`.jsonschema` files in the folder may reference
  each other by `nsURI`.

### QVT transformations

Each `.qvto` file becomes a factory configuration for the `QVTModelTransformator`
service with:

- `transformator.id` — the **name of the file's parent folder**
- `qvt.template.uri` — the absolute `file:` URI of the `.qvto` file

Group a transformation's files in a dedicated subfolder; the folder name becomes
the transformator id.

### Seeding scopes

Models placed below `scopes/<scopeName>/` are **not** registered globally.
Instead, as soon as the named scope's `WritableScopeService` appears, they are
uploaded into that scope's registry (default: the `schema` registry, `draft`
stage) — exactly as if they had been uploaded through the REST API. The upload is
persisted in the scope's storage backend and triggers the regular stage ENTER
action, which registers the `EPackage` services with the scope/stage properties.

- A namespace URI that is **already present** in the target stage is skipped, so
  the seeding is idempotent across restarts and never overwrites content the
  scope already has. Model updates go through the REST API (or git), not through
  the boot mount.
- If the scope service does not appear within `scope.wait.seconds`, the seeding
  is considered failed (see below).
- The built-in **`atlas`** scope cannot be targeted this way — its schema
  registry is read-only. Its content is the set of globally registered packages,
  i.e. the top-level files.
- `.qvto` files are not scope-bound and are rejected below `scopes/`.

### Atomicity and failure behaviour

The deployment is **atomic**: every file is loaded and validated before anything
is registered, and any failure rolls back everything that was already seeded or
registered — a retry never trips over a failed attempt's leftovers. One bad file
(unreadable, empty, no `EPackage` root, blank or duplicate `nsURI` — against the
registries or within the batch) aborts the **whole** deployment, including the
perfectly fine files next to it.

By default (`halt.on.error=true`) a failed deployment also **stops the OSGi
framework**, so a container startup fails visibly instead of running without its
models. Set `halt.on.error=false` to only fail the component activation and keep
the runtime going.

### Lifecycle

On deactivation the loader cleanly reverses its global registrations: it
unregisters the `EPackage` / `EPackageConfigurator` services, removes the
packages from the static registry, and deletes the QVT factory configurations.
Models seeded **into scopes** are persisted uploads, exactly like REST uploads —
they intentionally survive the loader (and container) lifecycle.

## Configuration

The component PID is **`InitialModelLoader`** with `ConfigurationPolicy.OPTIONAL`,
so it activates even without configuration (using the default folder).

| Property | Default | Description |
|----------|---------|-------------|
| `initial.models.folder` | `/initial-models` | Folder scanned once on startup. |
| `halt.on.error` | `true` | Stop the OSGi framework when the deployment fails (after rolling back). |
| `initial.models.registry` | `schema` | Registry within a scope that `scopes/<scopeName>/` models are uploaded into. |
| `initial.models.stage` | `draft` | Stage the scope models are uploaded into. Must be writable, and should be a trigger stage of the `EPackageStageActionService` for the packages to be registered. |
| `scope.wait.seconds` | `60` | How long to wait for a scope folder's scope service before failing. |

The loader does nothing (and logs an `INFO` message) when the folder is blank,
does not exist, is not a directory, or still contains an un-interpolated
configadmin template such as `$[env:...]`.

### Docker / environment variable

The Docker runtime ships a configuration that wires the folder to the
`INITIAL_MODELS_FOLDER` environment variable via the Felix configadmin
interpolation plugin:

```json
"InitialModelLoader": {
    "initial.models.folder": "$[env:INITIAL_MODELS_FOLDER;default=/initial-models]"
}
```

So in a container you simply set `INITIAL_MODELS_FOLDER` and mount your models at
that path:

```yaml
services:
  model-atlas:
    image: eclipsefennec/model.atlas:file-snapshot
    ports:
      - "8080:8080"
    environment:
      - INITIAL_MODELS_FOLDER=/initial-models
    volumes:
      # Mount a host folder with the models to deploy on first start.
      - ./initial-models:/initial-models:ro
```

## Example folder layout

```
/initial-models
├── billing.ecore              # registered globally (visible via the atlas scope)
├── sensors.jsonschema
├── billing-to-report/         # folder name becomes transformator.id
│   └── transform.qvto
└── scopes/
    └── jena/                  # uploaded into the 'jena' scope's schema registry
        ├── person.ecore
        └── address.ecore
```

## License

Eclipse Public License 2.0 (EPL-2.0)

Copyright (c) 2012 - 2026 Data In Motion and others.
