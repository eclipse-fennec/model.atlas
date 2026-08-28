# P3-2 — Implementation note (OSGi-side `JakartaRsClientProvider` from the Whiteboard `ClientBuilder`)

**Ticket:** P3-2 "OSGi-side `JakartaRsClientProvider` (Whiteboard `ClientBuilder`)" (Phase 3).
**Depends on:** P3-1. **Date:** 2026-06-08.

## Scope

Build the OSGi client's Jakarta RS `Client` from the `ClientBuilder` registered in the OSGi service
registry by the Jakarta RS Whiteboard, instead of `ClientBuilder.newBuilder()`, so the runtime's HTTP
client selection, registered providers and framework configuration apply — while reusing the P2-10
auth/timeout wiring unchanged.

## Exposing the shared provider base (impl)

The reuse path is inheritance (as the P2-10 note intended: *"the OSGi front-end supplies its own provider
that … reuses the same auth wiring by extending this class"*). `DefaultJakartaRsClientProvider` already
isolates the only `ClientBuilder.newBuilder()` call behind a `protected newClientBuilder()` seam and keeps
`newClient` (timeouts + `applyAuth`) above it.

That class lived in the un-exported `…rest.client.impl` package. Decision (user): **export it from impl**
rather than lift it into api. It only depends on api / jakarta / JDK types, so it was moved verbatim to a
dedicated package `org.eclipse.fennec.model.atlas.rest.client.impl.spi` and that single package is
`Export-Package`d; everything else in impl stays Private-Package. Updated the one internal reference
(`ModelAtlasClientBuilderImpl`) and the `DefaultJakartaRsClientProviderTest` import; impl unit tests stay
green (the test subclasses the provider and overrides the `protected` seams, which works across packages).

## `WhiteboardJakartaRsClientProvider` (osgi)

```java
final class WhiteboardJakartaRsClientProvider extends DefaultJakartaRsClientProvider {
    private final ClientBuilder clientBuilder;
    WhiteboardJakartaRsClientProvider(ClientBuilder clientBuilder) { ... }
    @Override protected ClientBuilder newClientBuilder() { return clientBuilder; }
}
```

Overrides only the seam; `newClient` (timeouts) and `applyAuth` (bearer/mTLS, P2-10) are inherited
unchanged — so "auth works unchanged through this provider" is satisfied by construction. One instance
wraps one Whiteboard `ClientBuilder` and builds a single client (one per component configuration).

## Wiring (AtlasClientComponent)

`@Activate` now also takes `@Reference ClientBuilder clientBuilder` (the Whiteboard service — confirmed in
repo by `AbstractRestTest`'s `@InjectService ClientBuilder` and `PiveauRestConnector`'s
`@Reference ClientBuilder`). The component builds the client with
`.clientProvider(new WhiteboardJakartaRsClientProvider(clientBuilder))`.

The `ClientBuilder` reference is **mandatory**: with no Whiteboard present the component does not activate.
That is the ticket's sanctioned *"or fails fast"* alternative to a configuration switch — an OSGi front-end
always wants the Whiteboard builder; consumers wanting the plain `ClientBuilder.newBuilder()` provider use
the plain-Java front-end (`rest.client.impl`) directly.

## Buildpath

osgi bundle gained `org.eclipse.fennec.model.atlas.rest.client.impl;version=snapshot` (to extend the now-
exported provider) and `jakarta.ws.rs-api;version=latest` (for `ClientBuilder`). impl remains a runtime
requirement too (the `ModelAtlasClientFactory` via SPI-Fly from P3-1).

## Status

impl + osgi build green; impl unit tests (incl. the relocated provider test) pass. Runtime activation
against a live Whiteboard is exercised by the P3-12 OSGi ITs. Next: P3-3 (EPackage publisher with the
`EMFNamespaces` + `atlas.*` service properties).
