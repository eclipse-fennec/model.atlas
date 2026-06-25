# P2-2 — Implementation note (`rest.client.impl` skeleton)

**Ticket:** P2-2 "`rest.client.impl` skeleton with Jakarta RS Client + plain-Java provider" (Phase 2).
**Depends on:** P2-1. **Date:** 2026-06-05.

## Scope

Stand up the implementation bundle: a builder-based `ModelAtlasClient` that constructs a Jakarta RS
`Client` through the default plain-Java `JakartaRsClientProvider`, wiring base URI + connect/read
timeouts + the SPI seam + lifecycle. The actual read operations (REST mapping, caching, drift) are
later Phase-2 tickets; here they fail fast with a pointer to the owning ticket.

## How `ModelAtlasClient.builder()` stays API-impl-decoupled (ServiceLoader)

The ticket requires `ModelAtlasClient.builder()` — a static on the API interface — yet the api bundle
must not depend on the impl. Resolved exactly the way `jakarta.ws.rs.client.ClientBuilder.newBuilder()`
resolves its own implementation: **`ServiceLoader`**.

- **api additions (P2-1 surface, additive):**
  - `ModelAtlasClient.builder()` → `ServiceLoader.load(ModelAtlasClientFactory.class, …).findFirst()…builder()`;
    throws `IllegalStateException` with a clear message if no impl is on the classpath.
  - `ModelAtlasClient.Builder` nested interface: `configuration(ClientConfiguration)`, `baseUri(URI)`,
    `connectTimeoutMs(int)`, `readTimeoutMs(int)`, `clientProvider(JakartaRsClientProvider)`, `build()`.
    The full config surface stays on `ClientConfiguration.Builder`; this builder adds only the
    construction-time conveniences + the provider seam.
  - `ModelAtlasClientFactory` bootstrap SPI (`@ConsumerType`): `ModelAtlasClient.Builder builder()`.
  - No new imports beyond `java.net.URI` / `java.util.ServiceLoader` → the api bundle stays
    OSGi-runtime-free (verified: still no `org.osgi.*` runtime imports).

- **impl classes (pkg `…rest.client.impl`, Private-Package):**
  - `DefaultJakartaRsClientProvider` — the **only** `ClientBuilder.newBuilder()` call site, isolated
    behind a `protected ClientBuilder newClientBuilder()` seam; `newClient(cfg)` applies
    `connectTimeout` / `readTimeout` (ms) and `build()`s. (Auth = P2-10.)
  - `ModelAtlasClientImpl` — holds the `ClientConfiguration` + `Client` + base `WebTarget`
    (`client.target(base.uri)`, ready for P2-3 REST mapping). `close()` → `client.close()`. The read
    methods throw `UnsupportedOperationException("Not yet implemented — P2-3/P2-6/P2-7")`.
  - `ModelAtlasClientBuilderImpl` (package-private) — accumulates on a `ClientConfiguration.Builder`;
    `build()` resolves the provider (caller-supplied or a fresh `DefaultJakartaRsClientProvider`),
    obtains the `Client` through it, and hands it to `ModelAtlasClientImpl`.
  - `DefaultModelAtlasClientFactory` — `@aQute.bnd.annotation.spi.ServiceProvider(ModelAtlasClientFactory.class)`,
    so bnd emits `META-INF/services/…ModelAtlasClientFactory` (verified in the jar) plus the
    `osgi.serviceloader` capability. The OSGi extender requirement this adds lands on the **impl**
    bundle, not the api.

`bnd.bnd` buildpath: `rest.client.api` (snapshot), `jakarta.ws.rs-api`, `org.eclipse.emf.common`,
`org.eclipse.emf.ecore`, `biz.aQute.bnd.annotation`. `-testpath: junit-jupiter-api`.

## Acceptance-criteria coverage

| Criterion | How met / test |
|---|---|
| `ModelAtlasClient.builder()` builds an instance from a `ClientConfiguration` | `ModelAtlasClientBuilderImpl.build()`; `ModelAtlasClientImplTest.builderBuildsAnInstanceFromConfiguration` + `…ConvenienceSettersFeedTheConfiguration` (drive the builder via `DefaultModelAtlasClientFactory` directly — see note below). |
| `JakartaRsClientProvider` is the only construction↔Jakarta RS seam; no `ClientBuilder.newBuilder()` elsewhere | Only `DefaultJakartaRsClientProvider.newClientBuilder()` calls it; the impl obtains its `Client` solely via the provider. The capturing-provider tests confirm the client is sourced through the SPI. |
| Plain-Java provider applies connect/read timeouts | `DefaultJakartaRsClientProviderTest.appliesConnectAndReadTimeoutsThenBuilds` — mocks `ClientBuilder`, verifies `connectTimeout(7000, MS)` / `readTimeout(42000, MS)` / `build()`. |
| `close()` releases the underlying `Client` | `ModelAtlasClientImplTest.closeReleasesTheUnderlyingClient` — `verify(client).close()`. |

## Notes / decisions

- **Tests drive the factory directly**, not the static `ModelAtlasClient.builder()`. The
  `ServiceLoader` descriptor is bnd-generated at jar-assembly time and is not guaranteed on the plain
  `test` task classpath; routing through `new DefaultModelAtlasClientFactory().builder()` exercises the
  identical builder code deterministically. The static `builder()` glue (the `ServiceLoader` lookup) is
  verified by the presence of the generated `META-INF/services` descriptor and is exercised end-to-end
  once the bundle is assembled.
- **Test style** follows the repo convention (`org.junit.jupiter.api.Assertions.*` + Mockito; e.g.
  `AbstractRegistryHelperTest`), not AssertJ. Fakes (`CapturingProvider`, mocked `Client` /
  `ClientBuilder`) keep the unit tests free of any Jakarta RS runtime.
- **Impl package is Private-Package** for now. Phase 3 (OSGi front-end) "reuses the exact same
  Jakarta RS client / cache / drift logic"; when that bundle lands, whatever it needs from
  `rest.client.impl` gets exported then — deferred to avoid premature impl exports.
- **Deferred fail-fast** read methods reference their tickets: `listScopeNames`/`ePackages` → P2-3,
  `checkForDrift`/`addDriftListener` → P2-6, `newResourceSet` → P2-7.

## Build status

As of 2026-06-05 `:rest.client.api:build` and `:rest.client.impl:build` (incl. the 6 unit tests) are
green. Template `Example`/`ExampleTest` stubs (which carried a stray DS `@Component`) were removed.
