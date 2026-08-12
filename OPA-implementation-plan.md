# OPA authorization for the Atlas platform — gateway-side PDP, zero Atlas code

## Context

Model Atlas has **no server-side authorization whatsoever** — verified across the tree: no `SecurityContext`, no `Principal`, no `@RolesAllowed`, no JWT, no OIDC, no CORS, no server TLS. Every REST endpoint is anonymous. The platform is gaining **APISIX** (routing, TLS, OIDC termination) and **Keycloak**.

The proposal: put OPA between the gateway and Model Atlas as the sole PDP, and have OPA obtain the facts it needs from Keycloak and Model Atlas over their REST APIs — so **no Java changes to Model Atlas**, and one policy engine serving every `*atlas` platform service.

Exploration says this works for most of the surface, better than expected. Three findings decide the shape:

1. **The entire policy-relevant topology is already on one endpoint.** `ScopesResource.listScopes()` returns `Scope` objects whose `registries` are real `Registry` instances (`ScopeServiceImpl:390` adds `reg.getRegistry()`), and `RegistryServiceImpl:535 createRegistryObject()` populates `allowedTransitions` *and* `stages`. So `GET /atlas/rest/scopes` yields `parentScope` (the inheritance chain), registry name/type, the full transition matrix, and every `StageInfo.readable/writable/final`. That is everything I previously assumed only an in-process PEP could see.
2. **That topology is near-static.** There is no REST CRUD for scopes, registries or stages — they exist only as Config Admin factory PIDs (`ScopeService~<name>`, `RegistryService~<name>`). It changes on reconfiguration, not on traffic. So it should be **replicated into OPA's `data`, not fetched per request.**
3. **The APISIX `opa` plugin never forwards the request body** (verified in `apisix/plugins/opa/helper.lua` — `build_http_request` returns only scheme/method/host/port/path/headers/query). And Atlas puts the discriminating fields for its most privileged operation in the body: `transitionPackage` takes `StageTransitionRequest` and reads `getObjectId()` / `getTargetStage()` from it. This is the one real limit, and it has a cheap fix.

**One correction to the framing:** OPA does not sit *in* the data path. Traffic flows client → APISIX → Atlas; APISIX *calls* OPA as a decision service and OPA returns allow/deny. There is no useful mode in which OPA proxies HTTP (`opa-envoy` is an ext_authz plugin, also out of the data path). The deployment diagram should reflect that — it matters for latency budgeting and for what OPA can and cannot modify.

---

## Architecture

```
client ──TLS──> APISIX ──1─> Keycloak (JWKS, cached)
                  │
                  ├──2─> OPA  POST /v1/data/atlas/gateway/decision
                  │            ↑ data.atlas.topology  (replicated, no per-request I/O)
                  │            ↑ data.atlas.entitlements (policy bundle)
                  │
                  └──3─> Model Atlas   (no published port)

atlas-authz-sync ──HEAD/GET /atlas/rest/scopes (If-None-Match)──> Atlas
                 ──PUT /v1/data/atlas/topology────────────────────> OPA
```

**1 — APISIX `openid-connect`** (priority 2599): `bearer_only: true`, `use_jwks: true`, local RS256 verification. No `client_secret` needed in this mode and no session. Audience validated against `client_id`.

**2 — APISIX `opa`** (priority 2001, runs after OIDC): the only enforcement point. Fail-closed with **no configuration and no fail-open option** — OPA unreachable or timeout ⇒ 403, non-JSON ⇒ 503, missing `result` key ⇒ 503. An *undefined* OPA decision returns HTTP 200 `{}`, which the plugin correctly turns into 503.

**3 — Trust boundary.** `openid-connect` **strips client-supplied** `X-Userinfo` / `X-Access-Token` / `X-ID-Token` before doing anything (verified in plugin source), so they cannot be forged through the gateway. `send_headers_upstream: [X-Atlas-Subject, X-Atlas-Roles]` additionally *deletes* client-supplied values for listed names. Atlas publishes **no host port** — only APISIX can reach it, and with no in-Atlas PEP that network isolation *is* the boundary, so it must be enforced (network policy in k8s, no published port in compose) rather than assumed.

### Replicate, don't `http.send`

The single most important design decision. Rego *can* call out with `http.send` during evaluation; it should not here.

- **It puts Atlas in its own authorization critical path.** Atlas slow ⇒ OPA slow ⇒ every request to Atlas denied ⇒ cascade. With replication, Atlas being down does not break authz at all; the data just goes stale.
- **It is circular.** OPA would call Atlas to authorize a call to Atlas, so the introspection endpoint must itself be authz-exempt — an unauthenticated read of the full scope inventory.
- `http.send` is non-deterministic, breaks partial evaluation, and its cache invalidates on a timer only — so a stage-config change is honored no sooner than a replicated poll would honor it, at far higher cost.
- Errors evaluate to **undefined** by default, which silently becomes `false`; only `strict-builtin-errors` makes that a hard 500.

**Reuse `ScopeAggregateService`.** `HEAD /atlas/rest/scopes/{scopeName}` already returns an aggregate ETag + `Last-Modified`, and on a stale `If-None-Match` adds `Atlas-Changed-NsUris` / `Atlas-Changed-Objects` hints. Its javadoc says it exists so "a drift watcher can poll one scope instead of every cached entry" — exactly this use case. The sync polls conditionally and re-pulls only on change.

Keep `http.send` for exactly one thing: JWKS retrieval, with `force_cache: true`.

**`atlas-authz-sync`** — a small container, no Atlas code:
- poll `GET /atlas/rest/scopes` (30 s) and `HEAD /scopes/{scope}` with `If-None-Match`; re-pull on change
- optionally the Keycloak Admin API for group→scope entitlements (not needed if group membership rides in the token, which is the recommendation below)
- push with `PUT /v1/data/atlas/topology`

**Split policy from replicated data by path.** Policy and entitlements arrive as a signed OPA **bundle** with `roots: ["atlas/policy", "atlas/entitlements"]`; topology is pushed via the Data API to `atlas/topology`, deliberately **outside** the bundle roots — a bundle activation overwrites everything under its roots, so overlapping them would have the sync's data silently erased on every bundle poll. Data-API writes do not survive an OPA restart, so the sync must push on OPA startup too (watch `/health?bundles=true`, then push).

---

## What this decides, and what it cannot

**Decidable today, with zero Atlas code changes:**

| Decision | Source |
|---|---|
| Valid token, issuer, audience, expiry | APISIX OIDC |
| Caller entitled in the scope named by path segment 0 | token claims |
| Read via `parentScope` inheritance | `data.atlas.topology` |
| Write refused on a non-writable or final stage | `StageInfo.writable/final` |
| Transition *legality* (`from→to` in the matrix) | `Registry.allowedTransitions` |
| Registry type / stage validity | topology |
| Verb class vs persona (reader may not POST) | method + claims |
| Route exemptions (health, webhooks, openapi) | path |

**Not decidable — all three trace to the same cause: the `opa` plugin does not forward the body, and Atlas puts the discriminating field there.**

| Gap | Why | Fix |
|---|---|---|
| **Transition authority** — *which* stage you may promote **to** | `getTargetStage()` is in the body. OPA sees only the *source* stage from the path, so it can confirm a `draft→approved` transition exists but not that you are promoting to `release` — the single most privileged act. | **Accept `targetStage` + `objectId` as optional query params** on the two transition endpoints, body still authoritative and required to match. ~15 lines, additive, backward compatible. Then it lands in `input.request.query`. Recommended. |
| **Per-object rules** (four-eyes `reviewUser != uploadUser`, `isReadOnly`) | `objectId` is in the body; and `ObjectMetadata.uploadUser` is **never populated from a request** — hardcoded `"system"` at `AtlasSchemaRegistryService.java:308`. | Needs identity propagation into `ObjectMetadata` — a Java change in *any* topology. Deferred; not a cost of this design. |
| **Collection filtering** (`GET /scopes` trimmed to visible scopes) | A gateway PDP allows or denies a whole response; it cannot trim one. | Round 1: **conscious decision** that any authenticated caller may read the scope inventory — note this discloses tenant names, registry names and stage topology. Filtering later needs an in-Atlas obligation filter. `response-rewrite` / `serverless-post-function` body rewriting is possible but not recommended. |

Also note `ObjectValidationResource` is `/{scopeName}/{stageName}/validate` — a *stage* where every other resource has a *registry*. Policy must extract only path segment 0 as the scope and never trust segment 1.

`/datagen` is unscoped and can generate from **any** registered EPackage — a genuine privilege-escalation path (a reader in one scope plus datagen ⇒ data shaped by any scope's schema). Interim: require realm role `atlas-datagen`; later move it under `/{scopeName}/datagen`.

---

## Identity model — proposed convention

**Keycloak groups carry membership; the policy bundle carries semantics.** Recommended over per-scope realm roles: onboarding a scope means creating one group tree and nothing else — persona→verb semantics live in policy and change without touching Keycloak.

```
/atlas/<scope>/reader        read the scope and, by inheritance, its ancestors
/atlas/<scope>/contributor   write work-class stages
/atlas/<scope>/approver      write review-class stages; transition into review class
/atlas/<scope>/publisher     transition into a final (publish) stage
/atlas/<scope>/curator       all of the above within the scope
/atlas/_all/reader           platform-wide read (sentinel; keeps tokens small)
```

- Emitted as claim **`atlas_groups`** by an explicit `oidc-group-membership-mapper` (`full.path: "true"`) on the `model-atlas` client's dedicated scope. **Trap:** Keycloak has no built-in `groups` scope, and the built-in mapper *named* `groups` emits realm roles. In a realm export all mapper `config` values are JSON **strings** (`"true"`, not `true`).
- Because entitlements ride in the token rather than in replicated data, **revocation latency is the token TTL, not the sync interval** — keep access-token lifetime short (5 min).
- **Stage classes, not stage names.** Stage names are per-registry configuration, so policy must never hardcode `draft`/`approved`/`release`. Classify from replicated `StageInfo`: `final: true` ⇒ **publish**; `writable: true, final: false` ⇒ **work**, unless the scope's entitlement entry declares it **review**. Default for an undeclared writable non-final stage is `work`, so enabling enforcement never breaks a working deployment; `strict_stages: true` per scope flips undeclared stages to `review`.
- Root `atlas` scope: read inherited by everyone via `parentScope`; write requires realm role `atlas-schema-curator`. Note `SchemaPackagesResource:108` hardcodes `REGISTRY_NAME = "schema"` while the root scope's registry is `atlas-schema-registry`, so `GET /atlas/schema` 400s today and the root scope is reachable only by inheritance. That is the posture we want — **do not "fix" it here**, but if it is ever fixed the curator gate must be live that day.
- `/jpa/{rootFolderName}` is a **second tenancy axis** — separate group axis `/atlas-jpa/<rootFolderName>/<persona>`.
- **Service accounts** (`client_credentials`): keyed by `azp` in `data.atlas.entitlements.service_accounts`; roles appear under `resource_access.<client>.roles`.
- Webhooks keep HMAC verification (`AbstractWebhookSignatureFilter`) and are OIDC-exempt at the gateway.

---

## OPA contract

**Query a sub-path, never a package root.** Verified empirically: querying a package root returns every helper rule including the fully decoded JWT, which then lands in APISIX and OPA decision logs. Point the plugin at `atlas/gateway/decision`.

Input is fixed by APISIX and **is wrapped in `{"input": ...}`** (the docs omit the wrapper). Header keys are **lower-cased**. `route`/`service`/`consumer` keys are **absent, not `{}`**, unless the matching `with_*` flag is set — write the Rego defensively.

Response is exactly four fields under `result` — `allow`, `reason`, `status_code`, `headers`. There is no `result.status`. On deny, `status_code` defaults to 403 and `reason` becomes the body; an object `reason` is JSON-encoded, so return RFC 7807-shaped problem JSON.

```rego
package atlas.gateway

decision["allow"] := allow
decision["status_code"] := status_code if not allow
decision["reason"] := problem if not allow
decision["headers"] := {"X-Atlas-Subject": claims.sub,
                        "X-Atlas-Roles": concat(",", sort(groups))} if {
	allow
}

default allow := false
allow if exempt_route
allow if scope_read_permitted
allow if stage_write_permitted
allow if transition_permitted
```

`default allow := false`, and every decision is a **total** object. Never write an inverted rule (`deny`/`violation`): undefined ⇒ `false` ⇒ "no violation" ⇒ allow, which fails *open*.

Rego **v1** syntax throughout — `if` on every rule body, `contains` for partial sets. **Do not write `import rego.v1`**; it is a no-op in OPA 1.x.

Packages: `atlas.claims` (claim extraction, JWKS verify), `atlas.topology` (helpers over replicated data: scope chain, stage lookup, transition matrix), `atlas.stageclass`, `atlas.gateway` (decision), `system.log` (`mask` — scrubs the bearer token from decision logs).

---

## Operations

`POST /v1/data/atlas/gateway/decision?strict-builtin-errors` — enable the flag so a failed `io.jwt.decode_verify` becomes a hard 500 rather than silently undefined.

```
opa run --server --addr=0.0.0.0:8181 --diagnostic-addr=0.0.0.0:8282 \
  --config-file=/config/opa.yaml --log-format=json \
  --set=decision_logs.console=true --ready-timeout=30 /policies
```

- `--addr` defaults to **localhost**:8181 — must be set explicitly in a container.
- `--diagnostic-addr` exposes `/health*` and `/metrics` on a second port while the policy API stays restricted.
- `--log-level=debug` logs request *and response* bodies (inputs and decisions). Never in production.
- Image `openpolicyagent/opa:1.19.0` — the **default tag is already distroless with no shell**, runs as `1000:1000`. `-rootless` is dead (removed in 0.59.0). Pin ≥ 1.19.0.
- Probes: `/health` and `/health?bundles=true`. `?bundles` is a **startup gate only** — a bundle server dying after first activation will not fail it; use `/v1/status` for ongoing freshness. Do **not** probe `/health/live` / `/health/ready` unless we ship `package system.health`, or they return 500 and the pod never becomes ready.
- Latency budget: eval p99 < 2 ms (pure evaluation, no I/O), gateway→OPA round trip p99 < 10 ms. The default `/metrics` histogram top bucket is 1 s, so add buckets or tail latency is invisible.
- HA: 3 replicas behind a service, keep-alive from APISIX (`keepalive: true`, `keepalive_pool`), `persist: true` for bundles. OPA's own docs recommend co-locating the PDP with the PEP; we are accepting a network hop in exchange for one central PDP serving every platform service — state that tradeoff explicitly in the design doc rather than eliding it.
- **No batch API in open-source OPA** (that is Styra Enterprise). One decision per request.

---

## Files

**Design doc** (matching the lowercase-hyphenated `docs/design/` convention)
- `docs/design/opa-authorization.md`
- `docs/design/opa-authorization-tickets.md`

**Policy bundle** — new top-level `authz/`, alongside `cnf/` and `docker/`
```
authz/README.md, authz/.manifest, authz/build.sh
authz/policy/atlas/{claims,topology,stageclass,gateway,mask}.rego
authz/policy/atlas/*_test.rego
authz/data/atlas/entitlements/{config,personas,stages,scopes,service_accounts,jwks}/data.json
authz/testdata/*.json
```
`data/atlas/entitlements/<leaf>/data.json` is deliberate — the only layout whose `data.*` paths are identical under `opa run <dir>` and under `opa build -b`. Topology fixtures for tests come from a captured `GET /scopes` response.

**Sync**
```
authz/sync/sync.sh          # curl + jq conditional poll; dev stack
authz/sync/README.md        # production shape: same logic, proper service or CI-built bundle
```

**Local stack**
```
docker/dockercompose/docker-compose-authz.yml
docker/dockercompose/authz/apisix/{config.yaml,apisix.yaml}
docker/dockercompose/authz/keycloak/realm-atlas.json
docker/dockercompose/authz/opa/opa.yaml
docker/dockercompose/authz/README.md
```
Ports — 8080/8081/8888/8086 are taken, so APISIX **9080** (+9091 Prometheus, 9090 control API), Keycloak **8180**, OPA **8181** (+8282 diagnostic). **model-atlas publishes no port** — that is both the boundary and the demonstration of it.

APISIX **standalone** mode (`deployment.role: data_plane`, `role_data_plane.config_provider: yaml`) — one container, no etcd, no admin key, routes git-diffable. Three traps: `#END` is still required at the end of `apisix.yaml`; **omit any `plugins:` section** (it *replaces* the defaults, and both plugins are default-enabled); mount the **directory**, not the single file (editors save via write-temp-then-rename, which breaks a single-file bind mount silently). `openid-connect.timeout` is in **seconds**, `opa.timeout` in **milliseconds** — not a typo.

Keycloak `quay.io/keycloak/keycloak:26.7.1`, `start-dev --import-realm`, realm at `/opt/keycloak/data/import/`. Use `KC_BOOTSTRAP_ADMIN_USERNAME`/`_PASSWORD` (`KEYCLOAK_ADMIN*` deprecated in 26.0). Realm import **never overwrites** (`IGNORE_EXISTING` is hardcoded) — to re-seed, drop the volume. Seed realm `atlas`, clients `apisix-gateway` + `model-atlas`, the `atlas_groups` mapper, the group tree, and users `alice-contributor` / `bob-approver` / `carol-publisher` / `dave-reader`.

**Repo edits**
- `.licenserc.yaml` — add `'**/*.rego'` to `paths-ignore` (`.yaml`/`.json`/`.md` are already there; `.rego` is not, and `docker/**` is not excluded wholesale).
- `.github/workflows/policy.yml` + a `policy` stage in `.gitlab-ci.yml`, on the `openpolicyagent/opa:1.19.0` image, independent of Gradle so a policy error fails in seconds and a Gradle failure never masks a policy regression.
- **The only Atlas Java change**, and it is optional-but-recommended: optional `targetStage` / `objectId` query params on `SchemaPackagesResource.transitionPackage` and the `ObjectRegistryResource` equivalent, validated to match the body. Without it, transition authority cannot be enforced at the gateway.
- Doc corrections (keeping docs consistent per the standing instruction): health check paths are **`/atlas/health/*`**, not the `/atlas/system/health` claimed in `CLAUDE.md:122-125`, `README.md:101-105,122,129` and the three `docker/modelatlas_*/README.md` — the servlet pattern is `/health/*` under context path `atlas/`. Verify or drop the `/swagger-api/` and `/swagger-ui/` claims in `rest.application/README*.md`. Update the "Security Considerations" section in `rest.application/README.md`, which currently presents isolation-by-configuration *as* the security model, and add a security section to `docs/user-guide.md`.

**Recommended immediately, separate from authz:** `runtime.config.local.jena/configs/gogomcp.json` publishes an unauthenticated MCP server on port 8088 exposing `execute_gogo` — arbitrary code execution in the live OSGi framework. It is inert only because no MCP bundle currently resolves, and `-resolve: never` on that bndrun makes that a convention rather than a mechanism. Delete the config and `-runblacklist` the MCP bundles in the docker bndruns.

---

## Verification

**Policy** — runs in CI and locally, no Gradle:
```bash
opa fmt --fail --list authz/
opa check --strict authz/policy authz/data
opa test authz/policy authz/data -v --coverage --threshold 90
opa eval -i authz/testdata/reader-put-denied.json -d authz/policy -d authz/data \
  --fail-defined 'data.atlas.gateway.allow'
opa build -b authz/policy -b authz/data -o bundle.tar.gz && opa inspect bundle.tar.gz
```
Test cases: route exemptions; missing token ⇒ deny; reader POST ⇒ deny with reason + 403; read of an ancestor scope via `parentScope`; write refused on `final`/non-writable stage; transition absent from the matrix ⇒ deny; transition into a final stage requires `publisher`; unknown scope ⇒ deny; empty/stale `data.atlas.topology` ⇒ deny; mask set contains the token path.

**Stack, end to end:**
```bash
docker compose -f docker/dockercompose/docker-compose-authz.yml up -d
TOKEN=$(curl -s -d grant_type=password -d client_id=model-atlas \
  -d username=alice-contributor -d password=alice \
  http://localhost:8180/realms/atlas/protocol/openid-connect/token | jq -r .access_token)

curl -s http://localhost:8181/v1/data/atlas/topology | jq '.result.scopes|keys'   # sync landed
curl -si http://localhost:9080/atlas/rest/scopes                    # 401 + WWW-Authenticate
curl -si -H "Authorization: Bearer $TOKEN" http://localhost:9080/atlas/rest/scopes         # 200
curl -si -X PUT -H "Authorization: Bearer $TOKEN" \
  http://localhost:9080/atlas/rest/acme/schema/stages/release       # 403, final stage
curl -si -X POST -H "Authorization: Bearer $TOKEN" \
  'http://localhost:9080/atlas/rest/acme/schema/stages/approved/actions/transition?targetStage=release&objectId=x'
                                                                     # 403, needs publisher
curl -si -H "X-Userinfo: $(echo '{"sub":"x","realm_access":{"roles":["atlas-admin"]}}'|base64 -w0)" \
  http://localhost:9080/atlas/rest/scopes                            # 401 — header stripped
curl -si http://localhost:9080/atlas/health/liveness                 # 200, exempt
docker compose ... stop opa   && curl -si -H "Authorization: Bearer $TOKEN" .../scopes   # 403/503 fail-closed
docker compose ... stop atlas && curl -s http://localhost:8181/v1/data/atlas/gateway/decision -d @in.json
                                                                     # still decides — no http.send
```
Assert Atlas is unreachable directly (no published port). **Verify empirically that the `opa` plugin actually sees `x-userinfo`**: APISIX's `ctx.headers` cache syncs only on the *first* header modification and `openid-connect` sets several in sequence, so the value may be stale. If it is, decode from `input.request.headers.authorization` with `io.jwt.decode_verify` instead — which the policy should do anyway as the verified path.

## Risks

| Risk | Resolution |
|---|---|
| **Network isolation is the whole boundary** — with no in-Atlas PEP, anything that can reach Atlas is fully authorized | Make it explicit and tested: no published port, k8s NetworkPolicy restricting ingress to the APISIX pods, and a stack test asserting direct access fails. Revisit if Atlas ever needs to be reachable by anything but the gateway. |
| **Replication staleness** — a scope reconfiguration is honored up to one poll interval late | 30 s conditional poll via the existing aggregate ETag; alert on sync lag; expose the synced revision in the decision for log correlation. Bounded and operator-controlled: entitlements come from token claims, so *revocation* latency is the token TTL (5 min), not the sync interval. |
| **Data-API topology is lost on OPA restart** | Sync watches `/health?bundles=true` and re-pushes on startup; policy denies on empty/stale `data.atlas.topology` rather than falling through. Keep `atlas/topology` outside the bundle `roots` or bundle activation erases it. |
| Transition authority unenforceable without the query-param change | Ship the ~15-line additive change. Until then, treat *any* transition as requiring `publisher` — coarse but fail-safe, and say so in the doc. |
| Scope inventory readable by any authenticated caller | A conscious round-1 decision; it discloses tenant names, registry names and stage topology. If unacceptable, `GET /scopes` must be gated to callers with at least one entitlement and full filtering deferred to an in-Atlas filter. |
| A bad `.rego` denies everything | Signed bundles; `opa test` + coverage floor as a merge gate; promote by re-tagging (revert ≈ 60 s, no redeploy); a canary replica queried in shadow mode. |
| JWKS in the bundle is a snapshot | CI rebuilds the JWKS data file on schedule and on realm change; keep current **and** previous key so rotation is not a cliff. |
| Decision logs will contain bearer tokens | `system.log.mask` ships in round 1, plus a test asserting the mask set contains the token path — a masking regression is otherwise silent. |
| Header scrubbing depends on APISIX plugin phase order | Pin the image tag; stack test asserting a client-supplied `X-Atlas-Subject` never reaches Atlas. |

**Open questions, with recommendations:** (a) does `parentScope` grant read, or must an ancestor grant be held? — recommend inheritance grants read (an operator setting `parentScope` has already made that call; requiring a second grant breaks every existing deployment), with a per-scope `require_ancestor_grant` opt-out; (b) `strict_stages` default — keep `work`, revisit after an audit window; (c) whether to run an audit/dry-run phase — recommend yes, but note the `opa` plugin has no audit mode, so it means a policy flag returning `allow: true` plus a decision-log annotation, and the log must then be actually reviewed before flipping.
