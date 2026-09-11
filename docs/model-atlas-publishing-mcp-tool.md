# Publishing to the model atlas

`org.eclipse.fennec.model.atlas.mcp.tools` adds two MCP tools:

| tool | what the agent hands over | where it lands |
|---|---|---|
| **`post_to_model_atlas`** | the namespace URI of a registered `EPackage` — a **schema** | a scope's schema stage |
| **`post_object_to_model_atlas`** | one serialized object — an **instance** | a stage of one of the scope's object registries |

Their companion `org.eclipse.fennec.model.atlas.mcp.config` is a resource-only
bundle carrying the Configurator JSON they need — one publisher configuration
each, and the tool provider that publishes them to an MCP server.

Both bundles live in the **model.atlas** project, while the MCP servers that
consume them live in
[emf.osgi-mcp](https://github.com/eclipse-fennec/emf.osgi-mcp). That split is
the point: model.atlas owns its own write path.

`post_to_model_atlas` exists so that a metamodel an agent inferred in this
session can *leave the runtime* without its XMI ever passing through the LLM.
The agent names a namespace URI; the bundle serializes the package and posts it
server-side, and the agent gets back a receipt.

`post_object_to_model_atlas` is its counterpart for data, and it is
deliberately **not** symmetric. A schema is something the runtime already
holds, so the agent can name it; an instance is not something the runtime holds
until someone states it, so here the agent does hand over the document. What
does not change is everything else: it names *what* to store and under which
id, and the destination stays the deployment's.

## What the agent controls, and what it does not

For `post_to_model_atlas`:

| | Who decides |
|---|---|
| which registered package is published | the agent (`nsURI`) |
| whether that namespace *may* be published | `publish.nsuri.allowlist`, deny-all by default, from `ModelAtlasPublisher` |
| destination scope and stage | configuration from `ModelAtlasPublisher` |
| whether an existing entry is replaced | configuration (`overwrite`) from `ModelAtlasPublisher` |
| the serialized document | this bundle — never the agent |

For `post_object_to_model_atlas`:

| | Who decides |
|---|---|
| the object itself | the agent (`content`), and its id (`objectId`), name and version |
| which **types** may be stored | the destination registry, which accepts only its own root EClasses — enforced server-side |
| destination scope, registry and stage | configuration from `ModelAtlasObjectPublisher` |
| whether an object already stored under that id is replaced | configuration (`overwrite`) from `ModelAtlasObjectPublisher` |
| the format the body is sent as | configuration (`content.type`), which the tool names in its own description |
| how large a body is accepted | configuration (`max.body.bytes`) |

Neither tool has a `scope`, `stage` or `overwrite` parameter, and the object
tool has no `registry` parameter either. A 409 means the name is taken and the
answer is a *different* name, not a retry with a flag flipped.

**There is no namespace allow-list on the object side**, and that is not an
oversight. `publish.nsuri.allowlist` exists because the package tool can see
every package the runtime registered and must not be able to publish all of
them. An object has no such ambient supply — the agent writes it — and what its
*type* may be is already decided, and enforced, by the configured registry:
`ObjectRegistryResource` refuses an EClass the registry does not accept, and
refuses outright anything the atlas marks as its own derived content. A second
type check here would have to parse a body whose format the deployment chooses,
and would still not be the authority.

**Deploying the bundle is the authorization decision.** A runtime that does not
install it cannot publish, which is why the write path is a bundle-private
service rather than a write method on the widely consumed, read-only
`ModelAtlasClient`.

## The endpoints

The create-package call is
`POST {base.uri}/{scope}/schema/stages/{stage}?nsUri=…&name=…&overwrite=…`,
with the `.ecore` document as an `application/xmi` body. That is
`SchemaPackagesResource`, which is `@Path("/{scopeName}/schema")` with the
create method at `@Path("/stages/{stageName}")`; the server deserializes an
`EPackage` from the body and cross-checks it against `nsUri`.

The create-object call is
`POST {base.uri}/{scope}/registries/{registry}/stages/{stage}/{objectId}?name=…&version=…&override=…`,
with the object as an `application/json` body by default. That is
`ObjectRegistryResource`, which is
`@Path("/{scopeName}/registries/{registryName}")` with the create method at
`@Path("/stages/{stageName}/{objectId}")`. Note the server spells the replace
flag **`override`** while the configuration property is `overwrite`, as it is
for packages; the flag is always sent, so the deployment's answer is explicit
rather than the endpoint's default.

`packages.path` and `registries.path` exist only so the `schema` and
`registries` segments stay configurable if those resources ever move.

### The object id is one path segment

`objectId` is the only path segment that does not come from configuration. A
value carrying a `/` is refused before anything is sent — it would either
address a different endpoint or, escaped, be stored under an id nobody can read
back. Everything else is percent-encoded for a *path*, so a `%20` and not a
`+` stands for a space.

### The body has to say what type it is

The resource method takes an `EObject`, so there is no Java entity type for the
codec to resolve a root EClass from: the document itself has to name its type.
For the Fennec codec's JSON that is the `_type` key on the root object, holding
`<nsURI>#//<EClass>`. The tool says so in its input schema, and the message it
returns for a 400 says it again.

The type's **schema must already be published in that scope** — which is what
makes `post_to_model_atlas` the natural predecessor of this tool for a model
authored in the same session.

So a call looks like this, with `content` a *string* holding the document:

```json
{
    "objectId": "device-1",
    "name": "EM310-UDL in room 3.14",
    "version": "1.0.0",
    "content": "{\"_type\":\"https://eclipse.org/fennec/inference/em310udl#//EM310UDLUplink\",\"devEui\":\"24e124…\",\"distance\":1420}"
}
```

and the receipt that comes back names where it went:

```json
{
    "outcome": "created",
    "objectId": "device-1",
    "objectName": "EM310-UDL in room 3.14",
    "version": "1.0.0",
    "scope": "jena",
    "registry": "default",
    "stage": "draft",
    "contentType": "application/json",
    "byteSize": 118
}
```

## How the request leaves the runtime

`UriHandlerAtlasTransport` is the only class here that talks HTTP, and it does
so through the `URIConverter` of a `ResourceSet` obtained from the runtime's
own `ResourceSetFactory` — where Fennec's RESTful URI handler
(`RestfulURIHandlerImpl`) serves `http` and `https`.

That is a deliberate choice over `jakarta.ws.rs.client.ClientBuilder`:

- the handler is already in every Fennec runtime, so publishing costs **no
  additional bundle and no JAX-RS stack** to deploy;
- `ClientBuilder` resolves its provider through the thread context class
  loader, which can fail because a provider bundle happened to activate lazily
  after this component did — a URI handler on the ResourceSet cannot;
- going through the runtime's factory rather than a bare `ResourceSetImpl`
  means the deployment's own URI handlers — a proxy handler, a test double —
  apply to publishing too.

`UriHandlerAtlasTransport` serves both tools; each publisher owns one, built
from its own `base.uri`, timeout and token variable.

Only the `URIConverter` of that ResourceSet is used, and a body is always
written as bytes the caller already produced — the `.ecore` as `EcoreXmi`
serialized it, an object exactly as the agent sent it — never as a live
`EPackage` or `EObject` handed to a codec. What leaves the runtime is what was
handed to the transport.

An unreachable endpoint comes back as `status == 0` rather than as an
exception: the caller shapes every outcome into a receipt, and a connection
failure is one of them.

### Outbound HTTP has to be allow-listed

`RestfulURIHandlerImpl` **blocks all `http(s)` resolution by default**. A
deployment that does not allow-list the atlas host gets

```
Blocked outbound http(s) resolution of URI '…' (host '…')
```

and the tool reports "the model atlas could not be reached". Allow the host in
the handler's own singleton PID:

```json
"org.eclipse.fennec.emf.osgi.urihandler.http": {
    "allowedHosts": [ "model-atlas.internal" ]
}
```

The PID is `org.eclipse.fennec.emf.osgi.urihandler.http`
(`RestUriHandlerProvider.PID`), it is deployment-wide, and it belongs to
whichever config bundle owns the runtime's EMF wiring — not to this one, which
must not fight another bundle over a singleton PID.

## Cross-package supertypes

`EcoreXmi` copies the package **and every package it references** in one
`EcoreUtil.Copier` pass, then puts each copy in a resource keyed by its own
namespace URI. A supertype from another package therefore leaves as
`<nsURI>#//<Name>`, which the atlas can resolve; without it the reference is
either dangling (a package from the session registry has no resource at all) or
a local file path. Foreign packages are **referenced, never inlined** — one
package goes over the wire.

This duplicates `emf.tools`' `Exports.toEcore` on purpose:
`org.eclipse.fennec.mcp.emf.tools.core` is a private package, and widening the
EMF tool bundle's contract so a second bundle can serialize is the wrong trade.

## Where the package comes from

`ModelAtlasPublisher` resolves the named namespace through `MetadataService`,
not through the EMF tool bundle's session registry, which is private to that
bundle. That is not a workaround: the metadata layer sees an OSGi-registered
package and a package the EMF tools registered in this session alike, so a
model the agent just inferred is publishable by the same path as one that was
always there — and `register_package` is the precondition for publishing
either way.

## Where the object comes from

Nowhere in the runtime: the agent sends it. That is the one real asymmetry
between the two tools, and it follows from what an instance is. A schema the
agent authored in this session is in the metadata layer by the time it may be
published, because `register_package` put it there; an instance is not in the
runtime at all unless something states it, and this bundle is not in the
business of holding session state.

The EMF tool bundle *does* hold instances — `create_dataset`,
`create_from_json` and the rest keep them in a session dataset — but
`org.eclipse.fennec.mcp.emf.tools.core` exports nothing, so its
`DatasetRegistry` is not reachable from here. Shipping a dataset instead of a
document would mean widening that bundle's contract, which is the same trade
that was declined for `Exports.toEcore` (see *Cross-package supertypes*
above). If that export is ever made, a second content source on
`ObjectPublisher` is the natural place for it — the transport, the policy and
the receipt above it do not change.

## Errors an agent can act on

No upstream response body ever reaches the agent — it goes to the server log,
where an operator can read it. What comes back instead says whether the agent
can do anything about it. For `post_to_model_atlas`:

| upstream | what the agent is told |
|---|---|
| 201 / 200 | receipt: `created` / `updated`, plus nsURI, name, scope, stage, classifier count, byte size |
| 409 | the namespace is taken in that stage — publish under a free one |
| 403 | the existing entry is read-only |
| 400 | one `GET` on the stage path separates *"this runtime is configured for a stage the atlas does not have"* from *"the atlas rejected the package as invalid"* |
| 401 / 407 | credentials rejected; no tool parameter fixes it |
| 415 | the configured content type is a deployment mismatch |
| unreachable | nothing was published, and **stop retrying** |

`post_object_to_model_atlas` answers the same way, with the destination named
differently and two checks that happen before anything leaves the runtime:

| outcome | what the agent is told |
|---|---|
| 201 / 200 | receipt: `created` / `updated`, plus objectId, name, version, scope, registry, stage, content type, byte size |
| id not one path segment | refused locally, with what a usable id looks like — nothing is sent |
| body over `max.body.bytes` | refused locally: split the document, do not retry — nothing is sent |
| 409 | the id is taken in that stage of that registry — store it under a free one |
| 403 | the object is read-only, **or** its type is content the atlas produces itself and will not accept from a client |
| 400 | one `GET` on the stage path separates *"this runtime is configured for a scope, registry or stage the atlas does not have"* from *"the atlas rejected the object"* — and the second message names both causes it can have: a type the registry does not accept, or a document that does not say which type it is |
| 401 / 407 | credentials rejected; no tool parameter fixes it |
| 415 | the configured content type is a deployment mismatch |
| unreachable | nothing was published, and **stop retrying** |

The stage probe reads only a **4xx** as a destination that is not there: the
list-in-stage endpoint answers `204` for a stage that exists and holds nothing,
and an empty stage is exactly the state a first object is written into.

## Configuration

Two factory PIDs, `ModelAtlasPublisher` and `ModelAtlasObjectPublisher` (tilde
notation), one per tool. They are separate because publishing schemas and
writing instances are separate decisions — a runtime may want one and not the
other — and because an unconfigured publisher has to make its tool **absent**
rather than present-and-failing. Both components are
`configuration-policy="require"` and refuse a blank mandatory property at
activation, so "not configured" and "no such tool" are the same state.

### The package publisher

Factory PID `ModelAtlasPublisher`. The connection half mirrors
the read client's `AtlasClientConfig` property names, so one deployment
configures both the same way; it is re-declared because
`org.eclipse.fennec.model.atlas.rest.client.osgi` exports no packages.

| property | default | |
|---|---|---|
| `base.uri` | *required* | base URI of the atlas REST API, e.g. `http://host:8080/atlas/rest` |
| `scope` | *required* | the scope every publication goes to |
| `stage` | `draft` | keep this a draft stage |
| `packages.path` | `schema` | segments between scope and `stages` |
| `content.type` | `application/xmi` | what the package body is sent as |
| `overwrite` | `false` | whether an existing entry may be replaced |
| `timeout.ms` | `30000` | connect and read timeout per request |
| `auth.token.env` | *empty* | **name of the environment variable** holding the bearer token — never the token itself. Read per request, so rotating it needs no reconfiguration; empty means an unauthenticated atlas |
| `publish.nsuri.allowlist` | *empty* | namespaces, or `*`-terminated prefixes, that may be published |

`org.eclipse.fennec.model.atlas.mcp.config` ships one instance,
`ModelAtlasPublisher~publisher`, driven entirely by the environment
(`configs/publisher.json`):

```json
"ModelAtlasPublisher~publisher": {
    "base.uri": "$[env:MODEL_ATLAS_BASE_URI;default=$[prop:MODEL_ATLAS_BASE_URI;default=]]",
    "scope": "$[env:MODEL_ATLAS_PUBLISHING_SCOPE;default=$[prop:MODEL_ATLAS_PUBLISHING_SCOPE;default=]]",
    "stage": "$[env:MODEL_ATLAS_PUBLISHING_STAGE;default=$[prop:MODEL_ATLAS_PUBLISHING_STAGE;default=draft]]",
    "overwrite": "$[env:MODEL_ATLAS_OVERWRITE;default=$[prop:MODEL_ATLAS_OVERWRITE;default=false]]",
    "publish.nsuri.allowlist": "$[env:MCP_ATLAS_PUBLISH_ALLOWLIST;type=String[];delimiter=|;default=$[prop:MCP_ATLAS_PUBLISH_ALLOWLIST;default=]]"
}
```

so a deployment sets `MODEL_ATLAS_BASE_URI`,
`MODEL_ATLAS_PUBLISHING_SCOPE`, `MODEL_ATLAS_PUBLISHING_STAGE`,
`MODEL_ATLAS_OVERWRITE` and a `|`-separated `MCP_ATLAS_PUBLISH_ALLOWLIST`. The
separator is a `|` rather than a `,` because a comma is legal in a URI and a
`|` is not. The remaining properties are not in the JSON and take their
defaults. The placeholders are resolved by the Configuration Admin
interpolation plugin, so the runtime needs

```
-runproperties: felix.cm.config.plugins=org.apache.felix.configadmin.plugin.interpolation
```

With none of those variables set the component does **not** activate: an unset
environment variable interpolates to `""`, and `base.uri` and `scope` are
checked for exactly that at activation — a blank `scope` would otherwise build
a request path with an empty segment and fail later, as an upstream status no
operator can trace back to the configuration. `post_to_model_atlas` is then
absent, which is the honest state. Set them but leave the allow-list empty and
the component does activate, publishes nothing, and logs a warning saying so.

`publish.nsuri.allowlist` is the control that stops the tool publishing
packages it merely happens to see. Rules are prefix-anchored on the **whole**
URI (a trailing `*`) or exact — never a substring match, so a rule for
`https://eclipse.org/fennec/inference/` cannot admit
`https://evil.example/…/inference/x`.

Keep `stage` a **draft** stage. Promotion to a released stage is a human
decision made in model.atlas, not something an MCP tool should reach.

### The object publisher

Factory PID `ModelAtlasObjectPublisher`. The connection half is the same set of
properties, read the same way.

| property | default | |
|---|---|---|
| `base.uri` | *required* | base URI of the atlas REST API |
| `scope` | *required* | the scope every object goes to |
| `registry` | *required* | the object registry within that scope, e.g. `default`. Naming it is at the same time naming what this tool may write, since the registry decides which root EClasses it accepts |
| `stage` | `draft` | keep this a draft stage |
| `registries.path` | `registries` | segment between scope and registry name |
| `content.type` | `application/json` | what the object body is sent as, and therefore the format the agent is told to produce |
| `overwrite` | `false` | whether an object already stored under the same id may be replaced |
| `max.body.bytes` | `1048576` | largest body accepted from the agent |
| `timeout.ms` | `30000` | connect and read timeout per request |
| `auth.token.env` | *empty* | as above — the **name** of the variable, never the token |

`configs/publisher.json` ships one instance,
`ModelAtlasObjectPublisher~publisher`:

```json
"ModelAtlasObjectPublisher~publisher": {
    "base.uri": "$[env:MODEL_ATLAS_BASE_URI;default=$[prop:MODEL_ATLAS_BASE_URI;default=]]",
    "scope": "$[env:MODEL_ATLAS_OBJECT_SCOPE;default=$[env:MODEL_ATLAS_PUBLISHING_SCOPE;default=$[prop:MODEL_ATLAS_PUBLISHING_SCOPE;default=]]]",
    "registry": "$[env:MODEL_ATLAS_OBJECT_REGISTRY;default=$[prop:MODEL_ATLAS_OBJECT_REGISTRY;default=]]",
    "stage": "$[env:MODEL_ATLAS_OBJECT_STAGE;default=$[prop:MODEL_ATLAS_OBJECT_STAGE;default=draft]]",
    "content.type": "$[env:MODEL_ATLAS_OBJECT_CONTENT_TYPE;default=$[prop:MODEL_ATLAS_OBJECT_CONTENT_TYPE;default=application/json]]",
    "overwrite": "$[env:MODEL_ATLAS_OBJECT_OVERWRITE;default=$[prop:MODEL_ATLAS_OBJECT_OVERWRITE;default=false]]",
    "max.body.bytes": "$[env:MODEL_ATLAS_OBJECT_MAX_BODY_BYTES;default=$[prop:MODEL_ATLAS_OBJECT_MAX_BODY_BYTES;default=1048576]]"
}
```

`base.uri` is the same variable the package publisher and the read client use —
a deployment reads from, and writes to, one atlas. `scope` falls back to
`MODEL_ATLAS_PUBLISHING_SCOPE`, so writing schemas and objects into one scope
takes one variable, while `MODEL_ATLAS_OBJECT_SCOPE` separates them when they
differ.

**`MODEL_ATLAS_OBJECT_REGISTRY` is the switch that turns the tool on.** It has
no default; unset, activation fails and `post_object_to_model_atlas` is simply
not there. A runtime that only publishes schemas therefore needs no change at
all — which is why the tool provider's `tools.cardinality.minimum` stays at 1.

## Wiring it into a runtime

`org.eclipse.fennec.model.atlas.mcp.config` also carries the tools' own
provider, `MCPToolProvider~modelAtlas` (`name=model_atlas_tool_provider`)
(`configs/tools.json`):

```json
"MCPToolProvider~modelAtlas": {
    "name": "model_atlas_tool_provider",
    "tools.target": "(|(tool.name=post_to_model_atlas)(tool.name=post_object_to_model_atlas))",
    "tools.cardinality.minimum:int": 1
}
```

The minimum is **1, not 2**, on purpose. The two tools are independently
configured, so a deployment that publishes schemas and not objects — or the
reverse — has exactly one of them. Raising the minimum to 2 would take that
runtime's provider down, and with it, at `/mcp/emf`, a working publishing
endpoint.

Two servers in `emf.osgi-mcp` consume it, and they treat its absence
differently on purpose:

- **`/mcp/emf`** (`org.eclipse.fennec.mcp.emf.runtime.config`) aggregates three
  providers but requires only **2**:

  ```json
  "toolProviders.target": "(|(name=emf_model_tool_provider)(name=emf_metadata_tool_provider)(name=model_atlas_tool_provider))",
  "toolProviders.cardinality.minimum:int": 2
  ```

  That minimum is the whole reason this provider is separate. When the bundle
  is absent its provider never activates, so an optional provider counted in
  the minimum would keep the entire endpoint down. With one combined provider
  the same fact could only be expressed as an off-by-one in a 38-tool
  `tools.cardinality.minimum` — true, but invisible. `server.instructions`
  there is deliberately left unchanged: it would otherwise advertise a tool to
  every runtime that does not install this bundle.

- **`/mcp/inference`** (`org.eclipse.fennec.mcp.inference.config`) names
  `post_to_model_atlas` directly in `MCPToolProvider~inference`, whose
  `tools.cardinality.minimum` counts it. That endpoint therefore does *not*
  come up without this bundle — which is correct, since the inference feature
  exists in order to publish. `org.eclipse.fennec.mcp.inference.runtime` is the
  resolution anchor and requires both of this project's bundles by identity.

  It does **not** name `post_object_to_model_atlas`, and adding it there is a
  change in `emf.osgi-mcp`, not here. Whoever makes it has to add the tool to
  `tools.target`, raise `tools.cardinality.minimum` accordingly, and accept
  that the endpoint then also requires `MODEL_ATLAS_OBJECT_REGISTRY` to be set
  — the inference flow infers a metamodel, so counting an instance-writing tool
  in its minimum is a real decision, not a formality.

A runtime does **not** have to contribute a `ModelAtlasPublisher` or
`ModelAtlasObjectPublisher` configuration —
`org.eclipse.fennec.model.atlas.mcp.config` already carries both. What it still
has to provide:

1. **The environment variables those configurations read.** `base.uri`, the
   scope, the stage, the allow-list and — for objects — the registry all come
   from the environment (see *Configuration* above), so a runtime that sets
   none of them gets neither tool.
2. **A host allow-list for the RESTful URI handler**, or nothing can be
   reached — see *Outbound HTTP has to be allow-listed* above.
3. **`MetadataService` and `ResourceSetFactory`** from `emf.osgi`, both present
   in any Fennec EMF runtime. No JAX-RS client implementation is needed.
