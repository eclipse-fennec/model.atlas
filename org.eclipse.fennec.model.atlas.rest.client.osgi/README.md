# Model Atlas REST Client — OSGi front-end

OSGi/Declarative-Services front-end for the Model Atlas client. It wraps the
plain-Java core (`…rest.client.impl`) in a **ConfigAdmin-driven factory
component** and bridges it to `emf.osgi`: schemas fetched from a remote Atlas are
published as the OSGi service trio `emf.osgi` expects
(`EPackageConfigurator` + `EPackage` + `EFactory`), so they land in the framework
`EPackage.Registry` and in every `ResourceSet` the framework hands out.

The net effect: code elsewhere in the framework resolves an unknown nsURI — by
`@Reference EPackage`, through `EPackage.Registry`, or by loading a resource that
references it — and the package is fetched from the Atlas and wired in
transparently.

If you are **not** in OSGi, use `…rest.client.impl` directly (see its README).

## What it does on activation

For each configuration you create under the factory PID
`org.eclipse.fennec.model.atlas.rest.client`, the component builds one independent
client and, depending on `mode`:

- **LAZY** (default) — publishes nothing up front; the first demand for an unknown
  nsURI fetches it, publishes it, and blocks (up to `lazy.resolve.timeout.ms`)
  until it is visible in the framework registry.
- **EAGER** — pre-fetches the configured scopes at activation and publishes them
  immediately; drift detection keeps them fresh.
- **HYBRID** — pre-fetches exactly the nsURIs in `eager.nsuri.allow.list`; the rest
  is LAZY.

Published packages are kept in step with the server: a drift change atomically
re-publishes the trio, a removal revokes it. **Local-first** is the default — a
remote package is suppressed while a local bundle provides the same nsURI (unless
`force.remote=true`), and re-published if that local one disappears.

Independently of the mode, the component also publishes one
`ReadOnlyScopeService<EObject>` OSGi service **per scope** (keyed `atlas.scope`), so a
consumer can read the scope's ordinary EObjects — `get` / `listObjectIds` /
`listAll` / `stream`, the registry as a parameter — through the same contract the
in-process server exposes. The scope set is `scope.allow.list` when configured,
otherwise the scopes the server advertises (`GET /scopes`); in `mode.strict` a failing
enumeration tears down activation. See "Consuming the published packages" below.

## Runtime requirements

Besides this bundle, `…rest.client.api`, `…rest.client.impl` and
`…scope.api` (the `ReadOnlyScopeService` contract the per-scope services are
published under), the framework needs:

- **`org.eclipse.fennec.emf.osgi.component`** — provides the framework
  `EPackage.Registry` (`default.resourceset.epackage.registry=true`) and the
  `ResourceSetFactory` the front-end binds to.
- **Apache Felix SCR** and **Felix ConfigAdmin** — DS + configuration.
- **Aries SPI-Fly** (`org.apache.aries.spifly.dynamic.bundle`) — mediates the
  impl's `ModelAtlasClientFactory` `ServiceLoader` provider into the service
  registry (the component `@Reference`s it).
- A **Jakarta RS `ClientBuilder`** service (the Whiteboard / Jersey client stack,
  e.g. `org.eclipse.osgitech.rest*` + `jersey-client`/`-common` + hk2 +
  `jakarta.ws.rs-api`). The `ClientBuilder` reference is **mandatory**: with no
  Whiteboard present the component does not activate (fail-fast).

See `org.eclipse.fennec.model.atlas.rest.client.osgi.tests/test.bndrun` for a
complete, resolved runbundle set.

## Configuring a client

The component is a **factory** (`configurationPolicy = REQUIRE`): one instance —
and one connected Atlas — per configuration. There is no `@Modified`, so updating
a configuration cleanly tears the instance down (unpublishing everything) and
re-activates it.

### Via the OSGi Configurator (JSON, like the server's `*.json`)

```json
{
  "org.eclipse.fennec.model.atlas.rest.client~jena": {
    "base.uri": "http://atlas-host:8080/atlas/rest",
    "mode": "LAZY",
    "default.scope": "jena"
  }
}
```

The instance name after `~` is yours; create several blocks to connect to several
Atlas instances.

### Programmatically (ConfigurationAdmin)

```java
Configuration cfg = configAdmin.createFactoryConfiguration(
        "org.eclipse.fennec.model.atlas.rest.client", "?");
Hashtable<String, Object> props = new Hashtable<>();
props.put("base.uri", "http://atlas-host:8080/atlas/rest");
props.put("mode", "LAZY");
props.put("default.scope", "jena");
cfg.update(props);
```

## Configuration reference

Attribute names in the OCD use `_`; the ConfigAdmin/Configurator property uses `.`
(so `base_uri` → `base.uri`). `base.uri` is required; everything else has a default.

**Connection**

| Property | Default | Meaning |
|---|---|---|
| `base.uri` | — (required) | Atlas REST base, e.g. `http://host:8080/atlas/rest` |
| `connect.timeout.ms` | `5000` | TCP connect timeout |
| `read.timeout.ms` | `30000` | socket read timeout |

**Resolution mode**

| Property | Default | Meaning |
|---|---|---|
| `mode` | `LAZY` | `LAZY` / `EAGER` / `HYBRID` |
| `eager.scopes` (`String[]`) | _empty_ | EAGER/HYBRID scopes to pre-fetch; empty + EAGER = all configured scopes |
| `eager.stages` (`String[]`) | `["released"]` | stages pre-fetched per scope |
| `eager.nsuri.allow.list` (`String[]`) | _empty_ | nsURIs pre-fetched in HYBRID |
| `mode.strict` | `false` | if `true`, EAGER activation **fails** when the server is unreachable |
| `lazy.resolve.timeout.ms` | `5000` | how long a LAZY look-up blocks for a fetched package to become visible in `EPackage.Registry` |

**Scope selection**

| Property | Default | Meaning |
|---|---|---|
| `scope.allow.list` (`String[]`) | _empty_ = all | scopes looked in (in order); first that can see an nsURI wins |
| `default.scope` | _unset_ | scope used for anonymous look-ups when no allow-list is set |

**Publishing behaviour**

| Property | Default | Meaning |
|---|---|---|
| `resource.set.fallback` | `true` | wrap framework-produced `ResourceSet`s with the Atlas-aware registry |
| `force.remote` | `false` | prefer the remote EPackage over a same-nsURI local one (high `service.ranking` + startup version check) |
| `register.in.global.registry` | `false` | also mirror published EPackages into `EPackage.Registry.INSTANCE` (legacy consumers) |
| `nsuri.allow.list` / `nsuri.deny.list` (`String[]`) | _empty_ | restrict which nsURIs are publishable |
| `drift.check.interval.ms` | `300000` | background drift-watcher period; `0` disables it |

**Caching**

| Property | Default | Meaning |
|---|---|---|
| `cache.max.entries` | `500` | LRU bound; `<= 0` = unbounded |
| `cache.ttl.ms` | `0` | entry TTL; `0` = no expiry |

**Authentication**

| Property | Default | Meaning |
|---|---|---|
| `auth.type` | `NONE` | `NONE` / `BEARER` / `MTLS` |
| `auth.token.env` | _unset_ | env var holding the bearer token (`BEARER`) |
| `auth.keystore.path` / `.password` / `.type` | type `PKCS12` | client keystore (`MTLS`) |
| `auth.truststore.path` / `.password` / `.type` | type `PKCS12` | truststore (`MTLS`) |

## Examples

### LAZY with ResourceSet fallback (the common case)

Resolve on demand; framework `ResourceSet`s gain Atlas fallback automatically.

```json
{
  "org.eclipse.fennec.model.atlas.rest.client~jena": {
    "base.uri": "http://host:8080/atlas/rest",
    "mode": "LAZY",
    "default.scope": "jena"
  }
}
```

### EAGER — mirror whole scopes at start-up

```json
{
  "org.eclipse.fennec.model.atlas.rest.client~jena": {
    "base.uri": "http://host:8080/atlas/rest",
    "mode": "EAGER",
    "eager.scopes": ["jena"],
    "mode.strict": true
  }
}
```

`mode.strict=true` makes activation fail if the Atlas is unreachable (so a missing
backend is loud rather than silent).

### HYBRID — pin a few packages, lazy-resolve the rest

```json
{
  "org.eclipse.fennec.model.atlas.rest.client~jena": {
    "base.uri": "http://host:8080/atlas/rest",
    "mode": "HYBRID",
    "default.scope": "jena",
    "eager.nsuri.allow.list": [
      "http://eclipse.org/fennec/model/atlas/management/api/1.0.0"
    ]
  }
}
```

### Prefer the remote over a local bundle

```json
{
  "org.eclipse.fennec.model.atlas.rest.client~jena": {
    "base.uri": "http://host:8080/atlas/rest",
    "mode": "EAGER",
    "eager.scopes": ["jena"],
    "force.remote": true
  }
}
```

> Caveat: `emf.osgi`'s default registry aggregates by **bind order**, not
> `service.ranking`, so `force.remote` wins reliably for *direct* service look-ups;
> registry-level precedence over a same-nsURI local bundle is bind-order dependent.
> See the P3-8 note in the design doc.

### Bearer authentication

```json
{
  "org.eclipse.fennec.model.atlas.rest.client~secure": {
    "base.uri": "https://host:8443/atlas/rest",
    "mode": "LAZY",
    "default.scope": "jena",
    "auth.type": "BEARER",
    "auth.token.env": "ATLAS_TOKEN"
  }
}
```

### Mutual TLS

```json
{
  "org.eclipse.fennec.model.atlas.rest.client~mtls": {
    "base.uri": "https://host:8443/atlas/rest",
    "mode": "LAZY",
    "default.scope": "jena",
    "auth.type": "MTLS",
    "auth.keystore.path": "/etc/atlas/client.p12",
    "auth.keystore.password": "changeit",
    "auth.truststore.path": "/etc/atlas/truststore.p12",
    "auth.truststore.password": "changeit"
  }
}
```

### Several Atlas instances at once

```json
{
  "org.eclipse.fennec.model.atlas.rest.client~prod": {
    "base.uri": "https://atlas-prod:8443/atlas/rest", "default.scope": "jena"
  },
  "org.eclipse.fennec.model.atlas.rest.client~staging": {
    "base.uri": "http://atlas-staging:8080/atlas/rest", "default.scope": "jena"
  }
}
```

### Mirror into `EPackage.Registry.INSTANCE` for legacy code

```json
{
  "org.eclipse.fennec.model.atlas.rest.client~jena": {
    "base.uri": "http://host:8080/atlas/rest",
    "mode": "EAGER", "eager.scopes": ["jena"],
    "register.in.global.registry": true
  }
}
```

## Consuming the published services

### Schemas (EPackages)

Once a package is published you can reach it the usual `emf.osgi` ways:

```java
// Through any framework ResourceSet (Atlas fallback installed by this front-end):
ResourceSet rs = resourceSetFactory.createResourceSet();
EPackage pkg = rs.getPackageRegistry().getEPackage("http://example.org/model/1.0");

// Or directly as a service (Atlas-published ones carry atlas.remote=true):
@Reference(target = "(emf.nsURI=http://example.org/model/1.0)")
EPackage pkg;
```

Atlas-published EPackage services carry `atlas.remote=true`, `atlas.base.uri`,
`atlas.scope`, and the standard `emf.*` properties (`emf.nsURI`, `emf.model.version`,
…). `atlas.stage` is also stamped as advisory provenance — the package's real owning
stage, resolved server-side — and is omitted only when that stage is unknown. (The
former `atlas.view` is no longer stamped, P5-7.)

### EObjects (per-scope `ReadOnlyScopeService`)

For ordinary EObjects, bind the per-scope service the front-end publishes (one per
scope, keyed `atlas.scope`):

```java
@Reference(target = "(atlas.scope=jena)")
ReadOnlyScopeService<EObject> jena;
// ...
jena.get("cocl", objectId).ifPresent(obj -> process(obj));
```

These services carry `atlas.scope`, `atlas.remote=true` and `atlas.base.uri`. The
same contract is what an in-process server publishes, so a consumer's `@Reference`
binds either source identically. **Note:** reading a `SCHEMA`-typed registry through
this service throws — use the EPackage path above for schemas.

## See also

- `org.eclipse.fennec.model.atlas.rest.client.impl` — the plain-Java client (non-OSGi).
- `docs/design/rest-client.md` — full design; "Phase 3 — OSGi Delegate Registry for `emf.osgi`" (EPackage publication) and Phase 5 (per-scope EObject services); per-ticket notes `docs/design/rest-client-P3-*.md` and `docs/design/rest-client-P5-*.md`.
