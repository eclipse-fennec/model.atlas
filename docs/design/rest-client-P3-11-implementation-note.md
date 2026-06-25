# P3-11 — Implementation note (`EPackage.Registry.INSTANCE` mirroring, opt-in)

**Ticket:** P3-11 "`EPackage.Registry.INSTANCE` mirroring (opt-in)" (Phase 3).
**Depends on:** P3-3. **Date:** 2026-06-09.

## Scope

When `register.in.global.registry=true`, additionally put each published EPackage into the EMF singleton
`EPackage.Registry.INSTANCE` (on top of the OSGi service registration), for legacy code that reaches the
singleton directly. Default `false` leaves the singleton untouched. Drift swaps must replace the singleton
entry consistently with the OSGi service.

## Implementation

The publisher is the single point through which publish / republish / unpublish flow, so the mirroring lives
there and is automatically consistent with the per-nsURI lock (P3-9):

- `RemoteEPackagePublisher` gained an injectable `EPackage.Registry globalRegistry` constructor argument
  (`null` = mirroring off). `publish` and `republish` call `mirrorToGlobal(nsUri, ePackage)`
  (`globalRegistry.put`) inside the locked block; `unpublish` and `unpublishAll` call `removeFromGlobal`
  (`globalRegistry.remove`). Because these run under the same per-nsURI lock as the service swap, a drift
  `republish` replaces the singleton entry in step with the OSGi service — AC #3.
- The component passes `register.in.global.registry ? EPackage.Registry.INSTANCE : null`. So with the flag
  off (default), `globalRegistry` is `null` and the singleton is never touched — AC #1.

**Injectable, not hard-wired to `INSTANCE`.** Threading the registry in (rather than referencing the
singleton directly inside the publisher) keeps the unit tests off the real global singleton — they pass a
fresh `EPackageRegistryImpl` and assert on it, with no cross-test pollution of `EPackage.Registry.INSTANCE`.

**Cleanup matters.** `unpublishAll` (called on `@Deactivate` and on a strict-activation failure) removes our
mirrored entries, so the client does not leak EPackages in the process-wide singleton across
activate/deactivate cycles (the same class of bug noted for `EMFFileWatcher`). Ownership caveat: with the
flag on, the client assumes ownership of its published nsURIs in the singleton — `remove` drops the entry
outright (there is no "restore the previous value"). This is fine in practice because local-first (P3-7)
means we only publish, and thus only mirror, nsURIs that no local already provides.

## Tests

`RemoteEPackagePublisherTest` (extended, with an injected `EPackageRegistryImpl` as the "global" registry):
publish mirrors the package in; `republish` replaces the mirrored entry (drift consistency); `unpublish`
removes it; `unpublishAll` clears all mirrored entries; and with no global registry supplied the singleton
stand-in is left untouched.

Build green (osgi).

## Status

P3-11 complete and build-green. Phase-3 feature work (P3-1 … P3-11) is done; only **P3-12 (OSGi integration
tests against a Testcontainers Atlas)** remains, which exercises all of the above end-to-end in a running
framework.
