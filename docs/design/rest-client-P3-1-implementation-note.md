# P3-1 — Implementation note (`rest.client.osgi` bundle + ConfigAdmin factory component)

**Ticket:** P3-1 "`rest.client.osgi` bundle with DS components + ConfigAdmin" (Phase 3).
**Depends on:** Phase 2 complete. **Date:** 2026-06-08.

## Scope

Stand up the OSGi front-end bundle and its ConfigAdmin entry point. The bundle is a thin DS wrapper
around the Phase-2 plain-Java core: per configuration it builds **one** `ModelAtlasClient` and owns its
lifecycle. EPackage publishing, resolution modes, drift substitution and ResourceSet integration are
layered on by P3-2 … P3-12 — none of that is in this ticket.

Bundle: `org.eclipse.fennec.model.atlas.rest.client.osgi` (the `Example`/`ExampleTest` scaffold stubs
were removed).

## Configuration — `AtlasClientConfig` (`@ObjectClassDefinition`)

A typed metatype OCD covering every Phase-2 `ClientConfiguration` property plus the two Phase-3-only
ones. Attribute method names map to the design's dotted property names by the metatype rule (`_` → `.`):
`base_uri()` → `base.uri`, `auth_keystore_path()` → `auth.keystore.path`,
`eager_nsuri_allow_list()` → `eager.nsuri.allow.list`, `lazy_resolve_timeout_ms()` →
`lazy.resolve.timeout.ms`, `resource_set_fallback()` → `resource.set.fallback`. Defaults mirror
`ClientConfiguration.Builder`. `mode` and `auth_type` are typed as the api enums (`ResolutionMode` /
`AuthType`); the two password attributes use `AttributeType.PASSWORD`. `base.uri` is the only required
attribute.

### api change

`ClientConfiguration` gained the two Phase-3 properties its javadoc had forecast:
`lazyResolveTimeoutMs` (default `5000`) and `resourceSetFallback` (default `true`), with builder setters,
accessors (`getLazyResolveTimeoutMs` / `isResourceSetFallback`), and inclusion in `equals`/`hashCode`/
`builder(from)`. They are documented as **OSGi-only — ignored by the plain-Java client** — kept on the
shared value type so there is a single configuration model (the design's "one core, two front-ends"
principle). Existing impl unit tests stay green (additive, defaulted fields).

## Component — `AtlasClientComponent`

```java
@Component(name = PID, configurationPid = PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = AtlasClientConfig.class, factory = true)
```

- **PID** `org.eclipse.fennec.model.atlas.rest.client`, **factory** → one component instance (one
  independent client) per configuration, so several Atlas instances connect in parallel.
- **`REQUIRE` + no `@Modified`** → a configuration update tears the instance down (`@Deactivate` closes
  the client) and re-activates cleanly. That is the "reentrant activation / clean restart" criterion.
- **Client construction:** `@Activate` constructor takes `@Reference ModelAtlasClientFactory` and the
  config, maps the config onto a `ClientConfiguration`, and calls
  `clientFactory.builder().configuration(cfg).build()`.

### Why `@Reference ModelAtlasClientFactory` (not `ModelAtlasClient.builder()`)

The impl bundle exports nothing (all Private-Package), so this bundle cannot reference impl classes, and
the static `ModelAtlasClient.builder()` resolves its factory via `ServiceLoader`, which is fragile inside
an OSGi framework. The impl's `DefaultModelAtlasClientFactory` is annotated
`@aQute.bnd.annotation.spi.ServiceProvider(ModelAtlasClientFactory.class)`, and **Aries SPI-Fly** is in
the workspace (`cnf/central.mvn`), so the factory is mediated into the service registry. The component
just `@Reference`s it. Compile-time buildpath is therefore api + DS/metatype annotations only (EMF comes
transitively via `-library: enableEMF`); impl + SPI-Fly are runtime-resolve concerns.

### Config mapping detail

ConfigAdmin has no null, so optional string attributes default to `""`; `toConfiguration` collapses blank
strings back to unset (via an `emptyToNull` helper) so the builder's own defaults apply (e.g. a blank
`default.scope` stays unset rather than becoming an empty-string scope). List attributes map through
`List.of(String[])`.

## Status

`api` + `osgi` bundles build green. Not yet exercised at runtime (no bndrun/IT in this ticket) — runtime
activation against a live framework comes with the OSGi integration tests in P3-12. Next: P3-2
(OSGi-side `JakartaRsClientProvider` from the Whiteboard `ClientBuilder`).
