# P2-10 — Implementation note (bearer token + mTLS auth providers)

**Ticket:** P2-10 "Bearer token + mTLS auth providers" (Phase 2).
**Depends on:** P2-2. **Date:** 2026-06-05.

## Scope

Pluggable authentication on the Jakarta RS client, applied through the `JakartaRsClientProvider` seam so
it works identically in plain Java and (Phase 3) the OSGi Whiteboard. Driven by `auth.type`:
`none` / `bearer` / `mtls`.

## Config additions (api `ClientConfiguration`)

mTLS needs key/trust material, which the config didn't carry. Added six properties, each documented as
**only used when `auth.type = mtls`**:

| Property | Default |
|---|---|
| `auth.keystore.path` / `.password` | — |
| `auth.keystore.type` | `PKCS12` (`DEFAULT_STORE_TYPE`) |
| `auth.truststore.path` / `.password` | — |
| `auth.truststore.type` | `PKCS12` |

Folded into `equals`/`hashCode` and `builder(from)`; **left out of `toString`** so passwords aren't
logged. Bearer reuses the existing `auth.token.env`.

## `DefaultJakartaRsClientProvider`

`newClient` now calls `applyAuth(builder, config)` after the timeouts:

- **bearer** → resolve the token from the env var named by `auth.token.env` and register a
  `ClientRequestFilter` that adds `Authorization: Bearer <token>` to every request. A missing/blank
  token logs a warning and registers nothing (requests go unauthenticated rather than failing the build).
- **mtls** → load the key store and trust store and install them via
  `ClientBuilder.keyStore(ks, password)` / `trustStore(ts)`.
- **none** → nothing.

Two `protected` seams keep it unit-testable without env vars or real store files:
`resolveToken(envName)` (defaults to `System.getenv`) and `loadStore(path, password, type)` (defaults to
`Files.newInputStream` + `KeyStore.load`; returns `null` for a blank path; load failure →
`ModelAtlasClientException`). No new buildpath deps (JDK `KeyStore` + the existing `jakarta.ws.rs` API).

The OSGi provider (Phase 3) extends this class, so it inherits the auth wiring and only overrides
`newClientBuilder()`.

## Acceptance-criteria coverage (`DefaultJakartaRsClientProviderTest`)

| Criterion | Test |
|---|---|
| Bearer adds `Authorization: Bearer <token>` | `bearerAuth_registersFilterAddingAuthorizationHeader` (captures the registered `ClientRequestFilter`, invokes it on a mock context, asserts the header) |
| Bearer with no token → unauthenticated, no failure | `bearerAuth_noToken_registersNothing` |
| mTLS installs key/trust stores on the builder | `mtls_installsKeyAndTrustStores` (overrides `loadStore`; verifies `keyStore(ks, "kpw")` + `trustStore(ts)`) |
| `none` installs nothing | `noneAuth_installsNothing` |

Tests mock `ClientBuilder` (no JAX-RS runtime) and use the `resolveToken`/`loadStore` seams.

## Build status

As of 2026-06-05 `:rest.client.api:build` and `:rest.client.impl:build` are green (61 unit tests).
Last remaining Phase-2 ticket: P2-11 (integration tests against a live Atlas, via Testcontainers).
