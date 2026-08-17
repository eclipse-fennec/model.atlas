# Issue sketch — the debug endpoints, and what actually guards them

*Draft for a GitHub issue. Written 2026-08-07 out of quality-review finding F61. The owner decided
on 2026-08-07 to **keep** the endpoints for now, so this is the security case for revisiting them —
and the larger question the case runs into.*

---

## Summary

Three leftover debug endpoints ship on production paths. Two of them are harmless echoes; one
discloses implementation internals. None of them is authenticated — but then, **neither is
anything else in the API**, and that is the finding that decides how much the first part matters.

## The endpoints

| Endpoint | Where | Answers |
|---|---|---|
| `GET /{scopeName}/schema/hello` | `SchemaPackagesResource.java:115-121` | `"Hello <scopeName>"` |
| `GET /openapi.{json\|yaml}/test` | `OpenApiResource.java:140-152` | `<Application class canonical name>#<identity hash>` |
| `GET …/jpa/hello` | `JpaDataResource.java:57-63` | `"Hello JpaDataResource"` |

The `schema/hello` endpoint does not check anything — it echoes the path segment back, so it
answers identically for a scope that exists and one that does not. The JPA one takes no
parameters at all.

**Keep the JPA one regardless of what happens to the others.** `JpaDataResourceFilter`'s
regression test for F51 (`testHello_isNotRejectedByTheFilter`) uses it precisely because it is the
one endpoint in that bundle carrying neither of the filter's two path parameters. If it is ever
removed, that test needs another parameterless endpoint first.

## What `/openapi.{type}/test` actually discloses

```java
String ctxId = app.getClass().getCanonicalName().concat("#")
        .concat(String.valueOf(System.identityHashCode(app)));
return Response.ok(ctxId).build();
```

Two things, and it is worth being precise about them:

1. **The JAX-RS `Application` implementation's canonical class name.** This fingerprints the
   framework and its version family to an unauthenticated caller — useful for picking a known CVE
   to try, not exploitable by itself.
2. **`System.identityHashCode(app)`.** This is *not* a memory address on a modern JVM — it is a
   generated identity hash cached in the object header. So it does not leak heap layout. It is
   stable for the object's lifetime, which makes it a reliable **restart / redeploy detector** and
   a way to tell two application instances behind a load balancer apart.

`@Operation(hidden = true)` keeps it out of the published OpenAPI document but not off the wire.
Being undocumented is not a control.

Realistically: low severity on its own, and **zero benefit in production** — which is the argument
for deleting it rather than for rating it.

## The finding this runs into

There is no authentication anywhere in the runtime. Verified 2026-08-07:

- `runtime.config.docker/configs/runtime.json` configures `org.apache.felix.http~atlasHtpp` on
  `0.0.0.0:8080` with a context path and nothing else.
- No authentication, authorization, JWT, OAuth or security bundle appears in
  `modelatlas.runtime_base.bndrun` or in any variant derived from it.
- No resource or filter in the API performs an authorization check.

So every endpoint is reachable unauthenticated by anyone who can reach the port — including the
write side: `POST`/`PUT` a schema, `DELETE` a package, transition an object between stages, run a
transformation. Against that, an endpoint that echoes a scope name is not the exposure worth
discussing.

This is presumably deliberate — the deployment is expected to sit behind a gateway that
terminates authentication — but nothing in the repository says so, no docker-compose file shows
such a gateway, and the README does not warn about it. **That** is the security issue; F61 is a
detail inside it.

Two sub-questions, and they are separable:

1. Is Atlas intended to be deployed only behind an authenticating proxy? If yes, say so in the
   README and the docker-compose files, and the debug endpoints drop to cosmetic.
2. If Atlas may be exposed directly, then the write endpoints need a control long before the
   debug endpoints do, and the debug endpoints should simply go.

## Options for the endpoints

### A. Delete `schema/hello` and `openapi/test`, keep the JPA one

- **for:** removes the disclosure and two untested code paths; no configuration to get wrong.
- **against:** the `openapi/test` endpoint may be someone's smoke test for
  "is the JAX-RS application wired up?" — worth asking before deleting.
- **effort:** minutes.

### B. Keep them, gated by configuration (default off)

A component property such as `debug.endpoints.enabled=false`, checked in the method or by a filter
that answers 404 when disabled.

- **for:** the smoke test survives for development; production images stop answering.
- **against:** more machinery than the endpoints are worth, and a default-off flag that someone
  flips in a config file is exactly how these things end up on in production anyway.
- **effort:** small.

### C. Keep them, drop the internals from the response

Replace the `ctxId` body with a fixed string (`"ok"`) and keep both endpoints as liveness probes.

- **for:** keeps whatever operational habit exists, removes the disclosure; one-line change.
- **against:** the health checks at `/atlas/system/health` already answer this question properly,
  with liveness and readiness tags. Two mechanisms for one job.
- **effort:** minutes.

### D. Keep as is, document them

- **for:** zero work.
- **against:** documents a fingerprinting endpoint as intended behaviour.

## Recommendation

**C for `openapi/test`, A for `schema/hello`** — the disclosure is the only part with any teeth, and
it disappears by changing one string; the echo endpoint has no purpose the health checks do not
already serve. **B** only if someone is actually using these in development.

But the ordering matters more than the choice: **question 1 above (is there meant to be a gateway?)
should be answered first**, in the README and the compose files. If the answer is "Atlas may be
exposed directly", the write endpoints are the issue and these three are noise.

## Questions for the decision

1. Is anyone using `GET /openapi.{type}/test` as a smoke test? (Decides A vs C.)
2. Is Atlas meant to run only behind an authenticating gateway? If so, where should that be
   written down — README, docker-compose comments, or both?
3. Should the absence of authentication be its own tracked issue? It is not a quality-review
   finding today, and the review's scope (SOLID + Eclipse guidelines) would not have caught it.

## Related

- **F61** — the review finding. Owner decision 2026-08-07: keep the endpoints for now.
- **F51** — its regression test depends on the JPA `hello` endpoint.
- The health checks (`/atlas/system/health`, with `liveness` and `readiness` tags) are the
  supported way to ask whether the runtime is up, and already exist.
