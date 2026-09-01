# Publishing to the model atlas

`org.eclipse.fennec.model.atlas.mcp.tools` adds one MCP tool,
**`post_to_model_atlas`**, that hands a registered `EPackage` to a
model.atlas stage. Its companion
`org.eclipse.fennec.model.atlas.mcp.config` is a resource-only bundle
carrying the two Configurator JSON files the tool needs — the publisher
configuration and the tool provider that publishes it to an MCP server.

Both bundles live in the **model.atlas** project, while the MCP servers that
consume them live in
[emf.osgi-mcp](https://github.com/eclipse-fennec/emf.osgi-mcp). That split is
the point: model.atlas owns its own write path.

The tool exists so that a metamodel an agent inferred in this session can
*leave the runtime* without its XMI ever passing through the LLM. The agent
names a namespace URI; the bundle serializes the package and posts it
server-side, and the agent gets back a receipt.

## What the agent controls, and what it does not

| | Who decides |
|---|---|
| which registered package is published | the agent (`nsURI`) |
| whether that namespace *may* be published | `publish.nsuri.allowlist`, deny-all by default, from `ModelAtlasPublisher` |
| destination scope and stage | configuration from `ModelAtlasPublisher` |
| whether an existing entry is replaced | configuration (`overwrite`) from `ModelAtlasPublisher` |
| the serialized document | this bundle — never the agent |

There is deliberately no `scope`, `stage` or `overwrite` parameter. A 409 means
the namespace is taken and the answer is a *different namespace*, not a retry
with a flag flipped.

**Deploying the bundle is the authorization decision.** A runtime that does not
install it cannot publish, which is why the write path is a bundle-private
service rather than a write method on the widely consumed, read-only
`ModelAtlasClient`.

## The endpoint

The create-package call is
`POST {base.uri}/{scope}/schema/stages/{stage}?nsUri=…&name=…&overwrite=…`,
with the `.ecore` document as an `application/xmi` body. That is
`SchemaPackagesResource`, which is `@Path("/{scopeName}/schema")` with the
create method at `@Path("/stages/{stageName}")`; the server deserializes an
`EPackage` from the body and cross-checks it against `nsUri`.

`packages.path` exists only so the `schema` segment stays configurable if that
resource ever moves.

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

Only the `URIConverter` of that ResourceSet is used. The `.ecore` document is
written as bytes `EcoreXmi` already produced, never as a live `EPackage` handed
to a codec, so what leaves the runtime is exactly what this bundle serialized.

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

## Errors an agent can act on

No upstream response body ever reaches the agent — it goes to the server log,
where an operator can read it. What comes back instead says whether the agent
can do anything about it:

| upstream | what the agent is told |
|---|---|
| 201 / 200 | receipt: `created` / `updated`, plus nsURI, name, scope, stage, classifier count, byte size |
| 409 | the namespace is taken in that stage — publish under a free one |
| 403 | the existing entry is read-only |
| 400 | one `GET` on the stage path separates *"this runtime is configured for a stage the atlas does not have"* from *"the atlas rejected the package as invalid"* |
| 401 / 407 | credentials rejected; no tool parameter fixes it |
| 415 | the configured content type is a deployment mismatch |
| unreachable | nothing was published, and **stop retrying** |

## Configuration

Factory PID `ModelAtlasPublisher` (tilde notation). The connection half mirrors
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
    "publish.nsuri.allowlist": "$[env:MCP_ATLAS_PUBLISH_ALLOWLIST;type=String[];delimiter=,;default=]"
}
```

so a deployment sets `MODEL_ATLAS_BASE_URI`,
`MODEL_ATLAS_PUBLISHING_SCOPE`, `MODEL_ATLAS_PUBLISHING_STAGE`,
`MODEL_ATLAS_OVERWRITE` and a comma-separated `MCP_ATLAS_PUBLISH_ALLOWLIST`.
The remaining properties are not in the JSON and take their defaults. The
placeholders are resolved by the Configuration Admin interpolation plugin, so
the runtime needs

```
-runproperties: felix.cm.config.plugins=org.apache.felix.configadmin.plugin.interpolation
```

With none of those variables set the component still activates — `base.uri`
and `scope` are mandatory but an empty string satisfies them. It then publishes
nothing, because the allow-list is empty; activation logs a warning saying so.

`publish.nsuri.allowlist` is the control that stops the tool publishing
packages it merely happens to see. Rules are prefix-anchored on the **whole**
URI (a trailing `*`) or exact — never a substring match, so a rule for
`https://eclipse.org/fennec/inference/` cannot admit
`https://evil.example/…/inference/x`.

Keep `stage` a **draft** stage. Promotion to a released stage is a human
decision made in model.atlas, not something an MCP tool should reach.

## Wiring it into a runtime

`org.eclipse.fennec.model.atlas.mcp.config` also carries the tool's own
provider, `MCPToolProvider~modelAtlas` (`name=model_atlas_tool_provider`),
holding the single tool (`configs/tools.json`):

```json
"MCPToolProvider~modelAtlas": {
    "name": "model_atlas_tool_provider",
    "tools.target": "(|(tool.name=post_to_model_atlas))",
    "tools.cardinality.minimum:int": 1
}
```

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

A runtime does **not** have to contribute a `ModelAtlasPublisher`
configuration — `org.eclipse.fennec.model.atlas.mcp.config` already carries it.
What it still has to provide:

1. **The environment variables that configuration reads.** `base.uri`, the
   scope, the stage and the allow-list all come from the environment (see
   *Configuration* above), so a runtime that sets none of them activates a
   publisher that refuses every package.
2. **A host allow-list for the RESTful URI handler**, or nothing can be
   reached — see *Outbound HTTP has to be allow-listed* above.
3. **`MetadataService` and `ResourceSetFactory`** from `emf.osgi`, both present
   in any Fennec EMF runtime. No JAX-RS client implementation is needed.
