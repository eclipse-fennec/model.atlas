# Exporter Migration Investigation

Investigation of what it takes to move the CSV / ODS / R-lang / XLSX exporters
out of `geckoprojects-emf-utils` into the `fennec-codec` workspace, and what
`fennec-codec` would need to support those formats.

**TL;DR:** the exporters are write-only tabular-export code; `fennec-codec` is a
token-stream serialization framework. They don't share a shape, so the cleanest
migration treats the exporters as a sibling concern in the `fennec-codec`
workspace and surfaces them as `Resource.Factory` services — not as
`CodecFormatProvider`s.

---

## What lives in `geckoprojects-emf-utils` today

`org.gecko.emf.exporter` (base, ~2k LOC) defines:

- `EMFExporter` — `exportResourceTo` / `exportEObjectsTo`
- `AbstractEMFExporter` — the matrix-building engine: pseudo-IDs, per-EClass
  column discovery, reference denormalization, metadata sheets, mapping
  matrices
- Helper classes in `cells/`, `headers/`, `keys/`
- `EMFExportOptions` interface, `EMFExportException`
- `@ProvideEMFExporter` capability annotation, namespace `emf.exporter`

Four format modules, each = small impl + tiny `.api` bundle holding constants /
options / `@RequireEMF<X>Exporter`:

| Format | Impl LOC | Extra deps                                                                                                |
|--------|----------|-----------------------------------------------------------------------------------------------------------|
| csv    | 1717     | `de.siegmar.fastcsv`                                                                                      |
| ods    | 473      | `com.github.miachm.sods` (repackaged as `org.gecko.com.github.miachm.sods`, since upstream isn't OSGi), `commons-text` |
| r_lang | 816      | `commons-text`                                                                                            |
| xlsx   | 537      | `org.apache.servicemix.bundles.poi`, `commons-text`                                                       |

All four register as DS components, `scope = PROTOTYPE`, with
`component.name=EMF<Fmt>Exporter` and the `emf.exporter` Provide-Capability.
All depend on Guava (`Stopwatch`, `Table`, `HashBasedTable`) and SLF4J.

---

## What `fennec-codec` expects from a "format"

`CodecFormatProvider<S,T>` + `FormatDelegate<T>` (writer) +
`FormatReaderDelegate<S>` (reader): token-stream API
(`writeStartObject` / `writeName` / `writeString` / …) with optional native
`ObjectId` support.

A new format ships:

1. a `CodecFormatProvider` (CBOR just wraps `JacksonFormatProvider` with
   `CBORFactory`),
2. a DS
   `@Component(service = Resource.Factory.class, property = {EMF_MODEL_FILE_EXT=…, EMF_MODEL_CONTENT_TYPE=…})`
   (see `CborResourceFactoryComponent`) that creates a `CodecResource` bound to
   the provider.

`CodecResource.doSave` / `doLoad` already routes to
`formatProvider.createWriter()` / `createReader()` when one is set.

Spec doc `17-format-abstraction.md` lists CSV as a candidate — but specifically
the "query-string / key-value" variant, not the tabular multi-sheet export
these exporters produce.

---

## How `model.atlas` plugs media types in

`SupportedMediatypesImpl` reads
`ResourceSet.getResourceFactoryRegistry().getContentTypeToFactoryMap()` and
filters to `application/*` / `text/*`. `ModelConverterResource` uses that list
to validate the `Accept` header.

But the actual serialization goes through **JAX-RS `MessageBodyWriter`**
components (see `EcoreMessageBodyHandler`, `JsonSchemaMessageBodyReaderWriter`,
`XSDSchemaMessageBodyReaderWriter`, `UMLMessageBodyReaderWriter`) — one per
media-type cluster. There is currently **no** consumer of `EMFExporter`
anywhere in `model.atlas`.

---

## The shape mismatch

`FormatDelegate` is tree / token oriented (one `writeStartObject` per EObject,
recursive children, then writers per attribute). The exporters work in the
opposite direction:

- First walk **all** EObjects to discover the union of columns per EClass.
- Build matrices.
- Denormalize many-references into expansion columns.
- Optionally add metadata / mapping sheets.
- Then emit.

They produce **multi-document** outputs (zipped CSVs, multi-sheet ODS / XLSX,
multi-file R). XLSX / ODS are binary container formats that can't be streamed
through a JSON-token-shaped writer.

Forcing them through `FormatDelegate` means buffering the full token stream
into an intermediate tree per save — heavy and lossy (you'd lose information
the exporter currently extracts directly from EReference metadata).

---

## Three options

### Option A — Move them, expose as `Resource.Factory` only (recommended)

Create `org.eclipse.fennec.codec.exporter` + four format modules in the
`fennec-codec` workspace. Keep `EMFExporter` / `AbstractEMFExporter` mostly
as-is (just repackage). Add one DS `Resource.Factory` component per format
that wires `EMF_MODEL_FILE_EXT` / `EMF_MODEL_CONTENT_TYPE` and returns a thin
`Resource` whose `doSave` delegates to the existing `EMFExporter` and whose
`doLoad` throws `UnsupportedOperationException`. **No changes needed in
`fennec-codec.api`.**

### Option B — Adapt to `CodecFormatProvider`

Implement a `FormatDelegate` that buffers tokens into a tree, then runs the
exporter at `close()`. Uniform with CBOR / BSON, but a large refactor for
marginal gain — the reads side stays unsupported, and XLSX / ODS don't suit
token streams.

### Option C — Add a `TabularFormatProvider` SPI in `codec.api`

New parallel SPI to `FormatDelegate`, modelled directly on `EMFExporter`,
registered with the codec resource factory machinery. Cleanest long-term, but
a chunk of API design and not needed unless we want one unified registration
path.

---

## What's required end-to-end to make these work in `model.atlas` (any option)

1. **Bring deps into the `fennec-codec` workspace** (`cnf/central.mvn`, library
   bnd):
   - `de.siegmar.fastcsv`
   - `com.github.miachm.sods` (port the `org.gecko.com.github.miachm.sods`
     repackaging too)
   - `org.apache.servicemix.bundles.poi`
   - `commons-text`
   - `com.google.guava` (or refactor away)

2. **Pick stable IANA content types** (none of the existing factories declare
   any):
   - csv → `text/csv`
   - ods → `application/vnd.oasis.opendocument.spreadsheet`
   - xlsx → `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
   - r_lang → pick one (e.g. `text/x-r-source`)

   CSV's "ZIP" export mode collides with generic `application/zip` — needs a
   sub-type or a mode query param.

3. **Add `Resource.Factory` DS components** with `EMF_MODEL_FILE_EXT` /
   `EMF_MODEL_CONTENT_TYPE` properties — that's what `SupportedMediatypesImpl`
   discovers.

4. **Add JAX-RS `MessageBodyWriter` components** (one per format, or one
   shared dispatcher). Without these, `/convert` will accept the Accept header
   but JAX-RS won't know how to write the entity. This is the piece I'd expect
   to be missed.

5. **Wire bundles into runtime `bndrun`s**: `modelatlas.runtime_base.bndrun`
   after `mediatypes.impl` (start level > 1071). Same for
   `runtime_local.bndrun` and the docker variants.

6. **Tests**: the gecko `*.tests` bundles depend on
   `org.gecko.emf.osgi.example.model.basic` and a `trees` test package. Port
   those or replace with codec test fixtures (`org.eclipse.fennec.codec.tests`).

---

## Open questions worth deciding before starting

- Keep the `emf.exporter` capability namespace, or fold into a fennec namespace
  (`fennec.codec.format` or similar)? Affects `@ProvideEMFExporter` /
  `@RequireEMF<X>Exporter` annotations.
- Drop Guava in the migration, or accept it as a `fennec-codec` dep? It's only
  used for `Stopwatch` and `Table` — replaceable, but adds ~1k LOC of
  refactoring.
- Do we want **read** support for any of these (CSV in particular)? If yes,
  that's a separate design — possibly the right place for the codec's
  "query-string-like" CSV reader mentioned in spec §17.

---

## Recommendation

**Option A**, scoped as: write-only `Resource.Factory` + JAX-RS
`MessageBodyWriter` per format, dropped into `fennec-codec` as sibling modules
to `codec.cbor` / `codec.bson`. Smallest change that gets CSV / ODS / XLSX /
R-lang showing up in `/convert` and producing valid output, without distorting
the codec's token-stream design to fit tabular exports.

---

## Write-only media types and the `Accept` / `Content-Type` split

A natural follow-up: if we only implement `MessageBodyWriter` (not `Reader`)
for these formats, do we discover the right type for `Accept` and not for
`Content-Type`? And is there a risk that `/convert` accepts e.g.
`Content-Type: text/csv` even though we can't read CSV?

### JAX-RS handles the split for us

JAX-RS negotiates the two headers independently, before the resource method is
invoked:

- **`Content-Type` (request body) → `MessageBodyReader`.** No reader whose
  `@Consumes` matches → framework returns `415 Unsupported Media Type`
  automatically. The resource method never runs.
- **`Accept` (response body) → `MessageBodyWriter`.** No writer whose
  `@Produces` matches → `406 Not Acceptable`.

If we register **only** a `MessageBodyWriter` for `text/csv`:

| Client sends                                              | Outcome                                              |
|-----------------------------------------------------------|------------------------------------------------------|
| `Accept: text/csv`, `Content-Type: application/json`      | ✅ JSON reader parses the EPackage, CSV writer emits |
| `Content-Type: text/csv`, any `Accept`                    | ❌ JAX-RS returns 415 before the method runs         |

So **there is no risk of accepting a `Content-Type` we can't read** — JAX-RS
rejects it for us.

### The `SupportedMediatypesImpl` gotcha

The `SupportedMediatype` service in `model.atlas` isn't sourced from JAX-RS
providers. It reads
`ResourceSet.getResourceFactoryRegistry().getContentTypeToFactoryMap()` — i.e.
EMF `Resource.Factory` registrations:

```java
set.getResourceFactoryRegistry().getContentTypeToFactoryMap().keySet().stream()
    .filter(s -> s.startsWith("application/") || s.startsWith("text/"))
    .forEach(mediaTypes::add);
```

And `ModelConverterResource.checkContentType()` — despite its name — checks
the **`Accept`** header against that list:

```java
private void checkContentType() {
    List<MediaType> acceptableMediaTypes = headers.getAcceptableMediaTypes();
    for (MediaType acceptedMediaType : acceptableMediaTypes) {
        ...
    }
}
```

Two consequences:

1. If we only add a `MessageBodyWriter` and **don't** register a
   `Resource.Factory` for `text/csv`, then `text/csv` won't be in
   `SupportedMediatype`, and `checkContentType()` will refuse the request even
   though a writer exists. So we still need the `Resource.Factory` registration
   — its job here is to **advertise** the content type to `model.atlas`'s
   allow-list.

2. EMF's `Resource.Factory` model doesn't separate read / write. A
   `Resource.Factory` for `text/csv` whose `doLoad` throws
   `UnsupportedOperationException` will still appear in `SupportedMediatype` as
   if it were bidirectional. That's cosmetic for `/convert` (JAX-RS still
   enforces correctness via 415), but it's misleading for anything that
   consumes `SupportedMediatype` as a "list of supported types" — e.g. OpenAPI
   docs.

### Possible refinement: split readable vs. writable

If we want the allow-list to reflect reality, split the service:

```java
List<String> getReadableMediaTypes();
List<String> getWritableMediaTypes();
```

Have `ModelConverterResource` (and any other caller) check against the right
one. That also lets the OpenAPI annotations on `convertPackage` declare
`@Produces` correctly per format.

Source of truth for each list:

- **Writable**: types for which a `MessageBodyWriter` is registered (or, as a
  proxy, `Resource.Factory` services advertising the type).
- **Readable**: types for which a `MessageBodyReader` is registered.

Not required for correctness — JAX-RS already enforces it — but worth doing if
the supported-types list shows up in user-facing places.
