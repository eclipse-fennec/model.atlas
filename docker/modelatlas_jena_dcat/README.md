# Model Atlas — Jena + DCAT Variant

The jena image plus **DCAT publishing**: every schema package uploaded with `?dcat=true` to a final
stage of the `jena` scope is published to a [DCAT.Atlas](https://github.com/eclipse-fennec/dcat.atlas)
portal as a `dcat:Dataset` with one `dcat:Distribution` per servable media type, linked into the
`dcat:Catalog` of its scope and of every scope that inherits from it.

Nothing publishes on its own: a scope must be listed in the publisher's `scopes`, its stage must be
permitted by `publish.stages` (final stages only, by default), **and** the package's own metadata must
carry the flag. All three have to agree, and the first two belong to this deployment rather than to
whoever uploads a model.

## Prerequisites

- **A reachable DCAT.Atlas portal.** `DCAT_PORTAL_BASE_URI` must point at its `/rest/` base from
  *inside* the container. The image is not published to a registry; build it in the `dcat.atlas`
  repository.
- **A public base URI for this atlas.** `ATLAS_PUBLIC_BASE_URI` is the entire public prefix, up to and
  including whatever stands in for this container's own `/atlas/rest` — the address a harvester can
  actually fetch from, through the APISIX in front. Every Distribution's `downloadURL` and
  `accessURL` is built from it, so those URLs are directly downloadable exactly to the extent that
  this value is right. A loopback address is refused unless `DCAT_ALLOW_LOCAL_BASE_URI=true`: a
  `localhost` URL in a public catalogue is worse than no catalogue entry, but it is the correct value
  for a demo stack whose URLs should be curlable from the host.
- **A publisher identity.** `DCAT_PUBLISHER_NAME` is required by the portal's own model:
  `publisher` is a `lowerBound=1` containment on `dcat:DcatResource`, so a Catalog or Dataset without
  a named Agent never reaches its store.

## Usage

```bash
docker run -d -p 8080:8080 \
  -e DCAT_PORTAL_BASE_URI="http://dcat-portal:8080/rest/" \
  -e ATLAS_PUBLIC_BASE_URI="https://opendata.example.de/model-atlas" \
  -e DCAT_PUBLISHER_NAME="Stadt Jena" \
  -e DCAT_PUBLISHER_ABOUT="https://www.jena.de" \
  -v $(pwd)/configs:/opt/modelatlas/runtime/load:ro \
  eclipsefennec/model.atlas:jena-dcat-snapshot
```

`docker/dockercompose/docker-compose-jena-dcat.yml` runs this alongside a portal.

## Environment

| variable | default | meaning |
|---|---|---|
| `DCAT_PORTAL_BASE_URI` | *(required)* | the portal's REST base, as reachable from this container |
| `ATLAS_PUBLIC_BASE_URI` | *(required)* | the public prefix a harvester fetches models from; loopback refused |
| `DCAT_PUBLISHER_NAME` | *(required)* | `dct:publisher`, the Agent's name |
| `DCAT_PUBLISHER_ABOUT` | — | the publisher's IRI |
| `DCAT_LICENSE_URI` | `http://dcat-ap.de/def/licenses/dl-by-de/2.0` | `dct:license`; required on every Distribution by the portal's model |
| `DCAT_LANGUAGE` | `de` | the language tag stamped on generated literals |
| `DCAT_UNPUBLISH_MODE` | `UNLINK` | what retirement does: `NONE`, `UNLINK`, `DELETE`, `CASCADE` |
| `DCAT_ALLOW_LOCAL_BASE_URI` | `false` | permits a loopback `ATLAS_PUBLIC_BASE_URI`. For a demo whose advertised URLs must be curlable from the host; never for a real deployment |

Everything else — the scope list, the media types, the retry budget — is in the image's
`runtime.config.docker.jena.dcat` bundle, and the scope/workflow configuration is mounted as
`jena.json` exactly as in the plain jena image.

## Operating it

- **Health.** `GET /atlas/system/health?tags=atlas` includes the publisher: the scopes it publishes,
  how many Datasets it tracks, any scope it has refused, and any publication that is being retried or
  has been given up on. It is deliberately **not** part of `readiness` — a portal being down says
  nothing about whether this atlas can serve its models.
- **Turning publication on or off for one model**, without re-uploading it:

  ```bash
  curl -X PATCH "https://…/atlas/rest/jena/schema/stages/release/metadata?nsUri=<enc>&dcat=false"
  ```

  Clearing the flag retires the Dataset; setting it publishes the model.
- **A transient portal failure is retried** with a doubling backoff and reported by the health check
  if the attempts run out. A payload the portal refuses as invalid is never retried — it would be
  refused identically every time — and is reported the same way.
