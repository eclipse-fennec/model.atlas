# Model Atlas REST Client — plain-Java implementation

Plain-Java (non-OSGi) implementation of the read-only Model Atlas client API
(`org.eclipse.fennec.model.atlas.rest.client.api`). It gives you a
`ModelAtlasClient` that talks to a remote Atlas over its REST API and turns the
schemas it serves into live EMF `EPackage`s — with an in-memory cache,
conditional (ETag) requests and drift detection on top.

Everything above the HTTP seam (REST mapping, caching, drift) is identical to the
OSGi front-end; the two differ only in how the Jakarta RS `Client` is built. If
you run inside an OSGi framework and want fetched packages published as OSGi
services / wired into `emf.osgi` `ResourceSet`s, use the OSGi front-end instead
(`org.eclipse.fennec.model.atlas.rest.client.osgi`, see its README). This bundle
is for plain Java: a CLI, a test fixture, a Spring/standalone app, etc.

## What you need on the classpath

| Artifact | Why |
|---|---|
| `…rest.client.api` | the interfaces you program against |
| `…rest.client.impl` | this bundle (the implementation, discovered via `ServiceLoader`) |
| A Jakarta RS **client** runtime (e.g. Jersey: `jersey-client` + `jersey-common` + `jersey-hk2`) | provides `jakarta.ws.rs.client.ClientBuilder.newBuilder()` |
| Jackson 3 (`tools.jackson:jackson-core`, `tools.jackson:jackson-databind`) + `com.fasterxml.jackson.core:jackson-annotations` (still 2.x-namespaced, required by databind) | JSON parsing of listings/metadata |
| EMF (`org.eclipse.emf.common`, `…ecore`, `…ecore.xmi`) | the `EPackage`s themselves and XMI decode |

The implementation registers a `ModelAtlasClientFactory` `ServiceLoader` provider
(`META-INF/services/…ModelAtlasClientFactory`), so `ModelAtlasClient.builder()`
finds it automatically — you never reference an impl class directly.

## Quick start

```java
import java.net.URI;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClient;

try (ModelAtlasClient client = ModelAtlasClient.builder()
        .baseUri(URI.create("http://localhost:8080/atlas/rest"))
        .build()) {

    // Discover scopes
    client.listScopeNames().forEach(System.out::println);

    // Fetch a package on demand (cache-fronted)
    client.ePackages()
          .getEPackage("http://example.org/my/model/1.0")
          .ifPresent(pkg -> System.out.println(pkg.getName()));
}
```

`ModelAtlasClient` holds an open Jakarta RS client and a background drift watcher,
so it is `AutoCloseable` — always close it (try-with-resources above).

### Loading a resource that references remote packages

`newResourceSet()` returns a `ResourceSet` whose package registry falls back to the
Atlas on a miss, so an XMI/JSON resource that references an unknown nsURI resolves
transparently:

```java
try (ModelAtlasClient client = ModelAtlasClient.builder()
        .baseUri(URI.create("http://localhost:8080/atlas/rest"))
        .defaultScope("jena")
        .build()) {

    ResourceSet rs = client.newResourceSet();
    Resource res = rs.getResource(URI.createFileURI("instance.xmi"), true);
    // EClasses from packages held only by the Atlas are resolved on access.
}
```

## The client surface

| Method | What it does |
|---|---|
| `listScopeNames()` | `GET /scopes` — the scope names the server exposes |
| `ePackages()` | the cache-fronted `RemoteEPackageProvider` for **schemas** (see below) |
| `readOnlyScope(scope)` | the per-scope `ReadableScopeService<EObject>` for **ordinary EObjects** (see below) |
| `listRegistries(scope)` | the registry names a scope exposes (`getScopeInfo().getRegistries()`) |
| `newResourceSet()` | a `ResourceSet` that falls back to the Atlas on an unknown nsURI |
| `checkForDrift()` | re-validate all cached entries now; returns a `DriftReport` |
| `addDriftListener(listener)` | subscribe to change/removal events; returns an unsubscribe handle |
| `close()` | release the HTTP client and the drift watcher |

`RemoteEPackageProvider` (from `ePackages()`):

| Method | What it does |
|---|---|
| `getEPackage(nsUri)` | local-first: cached, else fetched + cached; empty if no allowed scope holds it |
| `ensureAvailable(nsUri)` | same as `getEPackage`, named for warm-up intent |
| `resolve(nsUri)` | metadata-first: also reports the **owning** scope/registry/stage/version (`ResolvedEPackage`), resolving through scope inheritance |
| `listNsUris(scope)` | the nsURIs available in a scope's final stage (inherited packages included) |
| `refresh(nsUri)` | force a re-fetch, bypassing the cache |

Reads are **stage-free**: the server resolves each scope's final stage and walks
inheritance, so no stage name is embedded in any read URL.

### Reading EObjects (not just schemas)

Besides schemas, a scope holds ordinary EObjects in its registries.
`readOnlyScope(scope)` returns a per-scope `ReadableScopeService<EObject>` — the same
contract the in-process server exposes, so a consumer can depend on it whether it
reads a local Atlas or a remote one. The registry is a method parameter:

| Method | What it does |
|---|---|
| `get(registry, objectId)` | resolve one object from a registry's final stage (cache-fronted, ETag-revalidated) |
| `listObjectIds(registry)` | the object ids visible in a registry's final stage |
| `listAll(registry)` / `stream(registry)` | resolve every object in a registry |
| `getScopeInfo()` | the scope descriptor: name, description, parent scope, and registries (with their stages — see below) |
| `isInheritingFromParentScope()` | whether reads read through to a parent scope's final stage |
| `registryView(registry)` | a `ReadableRegistryView` bound to one registry's final stage |
| `registryView(registry, stage)` | a `ReadableRegistryView` bound to a specific stage; inheritance still reads through to parent scopes' final stages server-side |

```java
ReadableScopeService<EObject> jena = client.readOnlyScope("jena");
for (String id : jena.listObjectIds("cocl")) {
    jena.get("cocl", id).ifPresent(obj -> process(obj));
}
```

`ReadableRegistryView` (returned by `registryView(...)`) binds a single `(scope, registry[, stage])` combination and provides the same read operations without repeating the registry parameter:

| Method | What it does |
|---|---|
| `get(objectId)` | resolve one object |
| `listObjectIds()` | the visible object ids |
| `listAll()` / `stream()` | resolve everything |
| `getScopeName()` / `getRegistryName()` | the bound scope and registry |
| `getStageName()` | the bound stage, or `null` for a final-stage view |

> **SCHEMA registries are off-limits to this API.** A registry typed `SCHEMA` holds
> EPackages; an EObject read against it would treat the package as an opaque EObject.
> The client refuses: `get` / `listObjectIds` / `listAll` / `stream` on a SCHEMA
> registry throw `ModelAtlasClientException` pointing you at `ePackages()`. Fetch
> schemas through the EPackage API, EObjects through this one. (The registry type is
> read once from the scope descriptor and memoized.)

## Reading from a specific stage

By default every read targets each registry's **final stage**, resolved server-side. Stage
names are user-defined (e.g. `snapshot`, `review`, `released`) and differ per registry;
never hardcode a stage name — discover it from the scope descriptor instead:

```java
ReadableScopeService<EObject> jena = client.readOnlyScope("jena");
List<StageInfo> stages = jena.getScopeInfo().getRegistries().stream()
        .filter(r -> "cocl".equals(r.getName()))
        .findFirst()
        .map(RegistryInfo::getStages)
        .orElse(List.of());

String snapshotStage = stages.stream()
        .filter(s -> !s.isFinal() && s.isReadable())
        .map(StageInfo::getName)
        .findFirst()
        .orElseThrow();
```

### Schemas (EPackages) at a specific stage

```java
RemoteEPackageProvider packages = client.ePackages();

// Final stage (default — stage-free):
Optional<EPackage> pkg = packages.getEPackage("http://example.org/model/1.0");

// Explicit stage:
Optional<EPackage> draft = packages.getEPackageAtStage(
        "http://example.org/model/1.0", "jena", "snapshot");

// List all packages available in a stage:
List<PackageDescriptor> listed = packages.listPackagesAtStage("jena", "snapshot");
```

`getEPackageAtStage` bypasses the cache and goes straight to the server — it is intended
for comparison / review workflows, not for hot-path resolution. The stage-free
`getEPackage` remains the right call for normal use (cache-fronted).

### EObjects at a specific stage

Use `registryView(registry, stage)` to bind a `ReadableRegistryView` to an explicit stage.
Reads through this view use the stage-explicit server endpoints; inheritance still reads
through to parent scopes' **final** stages server-side (the "no silent demotion" rule: asking
for `snapshot` in this scope never quietly yields a parent scope's snapshot — it yields the
parent's final).

```java
ReadableScopeService<EObject> jena = client.readOnlyScope("jena");

// Final stage (stage-free — the default):
ReadableRegistryView<EObject> finalView = jena.registryView("cocl");
finalView.listObjectIds().forEach(id ->
    finalView.get(id).ifPresent(obj -> process(obj)));

// Snapshot stage (explicit):
ReadableRegistryView<EObject> snapshotView = jena.registryView("cocl", "snapshot");
snapshotView.listObjectIds().forEach(id ->
    snapshotView.get(id).ifPresent(obj -> reviewDraft(obj)));

System.out.println(snapshotView.getStageName()); // "snapshot"
System.out.println(finalView.getStageName());     // null  (= final, server-resolved)
```

The two views are independent: a `snapshot` read and a final read for the same id can
return different content and do not share a cache slot.

## Configuration reference (honored by the plain-Java client)

Set these via `ModelAtlasClient.builder()…` or by passing a fully-built
`ClientConfiguration` (`builder().configuration(cfg)`). `base.uri` is required;
everything else has a default.

| Property (builder method) | Default | Meaning |
|---|---|---|
| `base.uri` (`baseUri`) | — (required) | Atlas REST base, e.g. `http://host:8080/atlas/rest` |
| `connect.timeout.ms` (`connectTimeoutMs`) | `5000` | TCP connect timeout |
| `read.timeout.ms` (`readTimeoutMs`) | `30000` | socket read timeout |
| `default.scope` (`defaultScope`) | _unset_ | scope used for anonymous look-ups when no allow-list is set |
| `scope.allow.list` (`scopeAllowList`) | _empty_ = all scopes | scopes probed, in order, for an anonymous look-up; first hit wins |
| `nsuri.allow.list` (`nsUriAllowList`) | _empty_ = all | if non-empty, only these nsURIs are ever returned |
| `nsuri.deny.list` (`nsUriDenyList`) | _empty_ | nsURIs never returned, even if the server has them |
| `cache.max.entries` (`cacheMaxEntries`) | `500` | LRU bound; `<= 0` = unbounded |
| `cache.ttl.ms` (`cacheTtlMs`) | `0` | entry TTL; `0` = no expiry (revalidated by ETag) |
| `drift.check.interval.ms` (`driftCheckIntervalMs`) | `300000` | background drift-watcher period; `0` disables it |
| `auth.type` (`authType`) | `NONE` | `NONE` / `BEARER` / `MTLS` |
| `auth.token.env` (`authTokenEnv`) | _unset_ | env var holding the bearer token (`BEARER`) |
| `auth.keystore.path` / `.password` / `.type` | type `PKCS12` | client keystore for mTLS |
| `auth.truststore.path` / `.password` / `.type` | type `PKCS12` | truststore for mTLS |

> **Not consumed here.** `mode`, `eager.scopes`, `eager.stages`,
> `eager.nsuri.allow.list`, `mode.strict`, `force.remote`,
> `register.in.global.registry`, `lazy.resolve.timeout.ms` and
> `resource.set.fallback` describe *publication into the OSGi service registry* and
> are honored only by the OSGi front-end. The plain-Java client always resolves
> on demand. `cache.disk.dir` is reserved — the cache is currently in-memory only.

## Examples

### Pin to one scope

```java
ModelAtlasClient.builder()
    .baseUri(URI.create("http://host:8080/atlas/rest"))
    .scopeAllowList(List.of("jena"))   // only look in jena (+ its parents, by inheritance)
    .build();
```

There is no stage to pin: reads resolve each scope's final stage server-side.

### Restrict which packages are usable

```java
ModelAtlasClient.builder()
    .baseUri(base)
    .nsUriAllowList(List.of("http://example.org/a/1.0", "http://example.org/b/1.0"))
    .nsUriDenyList(List.of("http://example.org/internal/1.0"))
    .build();
```

### Bearer-token authentication

```java
// Token is read from the named environment variable at request time.
ModelAtlasClient.builder()
    .baseUri(base)
    .authType(AuthType.BEARER)
    .authTokenEnv("ATLAS_TOKEN")
    .build();
```

### Mutual TLS

```java
ModelAtlasClient.builder()
    .baseUri(URI.create("https://host:8443/atlas/rest"))
    .authType(AuthType.MTLS)
    .keystorePath("/etc/atlas/client.p12").keystorePassword(secret).keystoreType("PKCS12")
    .truststorePath("/etc/atlas/truststore.p12").truststorePassword(secret)
    .build();
```

### Tune the cache

```java
ModelAtlasClient.builder()
    .baseUri(base)
    .cacheMaxEntries(2000)
    .cacheTtlMs(60_000)   // re-validate entries older than 60s (still cheap via ETag/304)
    .build();
```

### React to drift

```java
try (ModelAtlasClient client = ModelAtlasClient.builder().baseUri(base).build()) {
    AutoCloseable handle = client.addDriftListener(new DriftListener() {
        public void onPackageAdded(String nsUri, EPackage p)   { /* newly available */ }
        public void onPackageChanged(String nsUri, EPackage p) { /* reload */ }
        public void onPackageRemoved(String nsUri)             { /* invalidate */ }
    });
    // … the background watcher (drift.check.interval.ms) fires events; or call:
    DriftReport report = client.checkForDrift();
    handle.close(); // unsubscribe
}
```

`onPackageChanged` / `onPackageRemoved` concern nsURIs the client already holds.
`onPackageAdded` reports one it held nothing under — a package published and
promoted into a scope's final stage after the client started — so a running client
sees it without a restart. Discovery is on for **EAGER** and **HYBRID** and off for
**LAZY**, which fetches on demand anyway. An nsURI the server names but cannot yet
resolve stage-free (a draft-only publish) is skipped silently: it is not an
addition, and never a removal.

### Read from a non-final stage (snapshot review)

```java
try (ModelAtlasClient client = ModelAtlasClient.builder()
        .baseUri(URI.create("http://localhost:8080/atlas/rest"))
        .build()) {

    ReadableScopeService<EObject> jena = client.readOnlyScope("jena");

    // Discover what stages exist for the "cocl" registry.
    String snapshotStage = jena.getScopeInfo().getRegistries().stream()
            .filter(r -> "cocl".equals(r.getName()))
            .flatMap(r -> r.getStages().stream())
            .filter(s -> !s.isFinal() && s.isReadable())
            .map(StageInfo::getName)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("no readable non-final stage"));

    // Bind a view to that stage.
    ReadableRegistryView<EObject> draft = jena.registryView("cocl", snapshotStage);
    draft.listAll().forEach(obj -> System.out.println(obj));

    // Fetch a schema at the same stage.
    client.ePackages()
          .getEPackageAtStage("http://example.org/model/1.0", "jena", snapshotStage)
          .ifPresent(pkg -> System.out.println("draft pkg: " + pkg.getName()));
}
```

### Reuse one configuration as a template

```java
ClientConfiguration base = ClientConfiguration.builder()
    .baseUri(URI.create("http://host:8080/atlas/rest"))
    .authType(AuthType.BEARER).authTokenEnv("ATLAS_TOKEN")
    .build();

ModelAtlasClient client = ModelAtlasClient.builder()
    .configuration(ClientConfiguration.builder(base).defaultScope("jena").build())
    .build();
```

## Errors

Transport failures surface as `TransportException`, server/protocol errors as
`ModelAtlasClientException` (`NotFoundException` for a 404). A look-up that is
simply not present returns `Optional.empty()` rather than throwing.

## See also

- `org.eclipse.fennec.model.atlas.rest.client.osgi` — the OSGi/DS front-end (ConfigAdmin-driven, publishes EPackages as services).
- `docs/design/rest-client.md` — full design (Phase 2 covers the EPackage core; Phase 5 the per-scope EObject API); per-ticket notes `docs/design/rest-client-P2-*.md` and `docs/design/rest-client-P5-*.md`.
