# P1-2 — Implementation note (deviations from the ticket)

**Ticket:** P1-2 "Strong ETag + Last-Modified on per-object content GETs" (Phase 1).
**Companion docs:** [`rest-client.md`](./rest-client.md) §"Phase 1", [`rest-client-tickets.md`](./rest-client-tickets.md), [`rest-client-P1-1-audit.md`](./rest-client-P1-1-audit.md).
**Date:** 2026-06-03.

## What was implemented

- New `org.eclipse.fennec.model.atlas.rest.application.filter.ObjectMetadataResponseFilter` — a
  centralised `ContainerResponseFilter` that stamps a strong `ETag` (= `ObjectMetadata.getContentHash()`)
  and `Last-Modified` (= `ObjectMetadata.getLastChangeTime()`) onto responses, each only when the header
  is not already present.
- Resources attach the backing metadata via the static seam
  `ObjectMetadataResponseFilter.attach(requestContext, metadata)`; the filter reads it from the
  request property `PROP_OBJECT_METADATA`.
- The duplicated private `addETagHeader(...)` helpers were removed from `SchemaPackagesResource` and
  `ObjectRegistryResource`; all six call sites in each now use `attach(...)`.
- Tests: `testGetPackageContent_ReturnsStrongETagAndLastModified` (SchemaPackagesResourceTest) and
  `testGetObjectContent_ReturnsStrongETagAndLastModified` (ObjectRegistryResourceTest) assert a strong
  ETag (`getEntityTag()` non-null, `!isWeak()`) **and** a parseable `Last-Modified` (`getLastModified()`).

## Deviation 1 — the ticket's "existing request property" premise was false

The P1-2 description states:

> Resources already attach `ObjectMetadata` to a request property for content negotiation; the filter
> reads it from there.

This does **not** hold in the current codebase. The only request properties set during content
negotiation are `ModelAtlasRestConstants.RESOLVED_MEDIA_TYPE` (`"resolvedMediaType"`) and
`RESOLVED_RESOURCE_SET_CSO` (`"resolvedResourceSetCso"`). **Neither carries an `ObjectMetadata`**, so
there was nothing for the filter to read.

**Adaptation:** introduced a dedicated property `ObjectMetadataResponseFilter.PROP_OBJECT_METADATA`
and explicit `attach(...)` calls in the resources. Functionally equivalent to the ticket's intent
("filter reads metadata from a request property"), but the resources had to be changed to *populate*
that property rather than relying on a pre-existing one.

## Deviation 2 — registered as a Whiteboard extension, not a bare `@Provider`

The description says "a centralised `ContainerResponseFilter` (`@Provider`)". A bare JAX-RS `@Provider`
annotation is **not** picked up by this project's OSGi JAX-RS Whiteboard runtime. The filter is therefore
registered with `@Component @JakartarsExtension @JakartarsName("ObjectMetadataResponseFilter")`, mirroring
the existing `ModelAtlasExceptionMapper`. It is a JAX-RS provider in effect; only the registration
mechanism differs from the literal wording.

## Known seam deferred to P1-3 (RESOLVED in P1-3)

At P1-2 time, the **304** path was not centralised: `evaluateConditionalGet(...)` built
`Response.notModified(new EntityTag(hash))` inline in each resource, so the ETag for a conditional hit was
constructed in the resource, not the filter.

**Resolved in P1-3.** `ObjectMetadataResponseFilter` now also evaluates `If-None-Match` /
`If-Modified-Since` and rewrites a matching `200` GET/HEAD response to `304 Not Modified` (no body,
validators retained). The inline `evaluateConditionalGet(...)` helpers and their `EntityTag` imports were
removed from both resources, so **all** validator headers — on both 200 and 304 — now flow through the
filter. See [`rest-client-P1-3-implementation-note.md`](./rest-client-P1-3-implementation-note.md).
