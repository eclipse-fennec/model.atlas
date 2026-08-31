# GitHub CI

The repository runs two GitHub Actions workflows: a build pipeline that
also produces and publishes Docker images, and a license-header check.

All workflow definitions live in [`.github/workflows`](../.github/workflows).

## Branch model

`snapshot` is the active development line — all PRs target it, and every push
publishes the `:apicurio-snapshot` / `:file-snapshot` container images.
`main` always holds the latest released version, available as the
`:apicurio-latest` / `:file-latest` images on
[Docker Hub](https://hub.docker.com/r/eclipsefennec/model.atlas/tags) and
[GHCR](https://github.com/eclipse-fennec/model.atlas/pkgs/container/model.atlas).
This project ships container images instead of Maven Central artifacts.

| Branch     | Purpose                                            | Docker tag suffix                                                                 |
|------------|----------------------------------------------------|-----------------------------------------------------------------------------------|
| `snapshot` | Active development. PRs target this branch.       | `apicurio-snapshot`, `file-snapshot`, plus the bundle version (`apicurio-x.y.z`)  |
| `main`     | Latest release — images here match `:apicurio-latest` / `:file-latest`. | `apicurio-latest`, `file-latest`, plus the bundle version (`apicurio-x.y.z`)      |
| any other  | Topic branches and PRs run build only, no deploy. | —                                                                                 |

## Workflow overview

```
┌─────────────────────────┐
│   PR / feature branch   │
└────────────┬────────────┘
             │  push / pull_request
             ▼
    ┌────────────────────┐
    │     build.yml      │
    │     (Gradle Build) │
    └────────┬───────────┘
             │ on push to snapshot / main:
             ▼
    ┌────────────────────┐
    │  Container Deploy  │  →  Docker Hub  +  ghcr.io
    │  (apicurio + file) │     multi-arch: amd64 + arm64
    └────────────────────┘
```

## `build.yml` — CI Build + Container Deploy

* **File:** [`.github/workflows/build.yml`](../.github/workflows/build.yml)
* **Triggers:** `push` and `pull_request` on every branch.
* **Runner:** `ubuntu-latest`, Java 21 (Temurin).

Two jobs:

### `build`

* Validates the Gradle wrapper, sets up JDK 21 with Gradle cache, ensures
  Docker is available on the runner.
* Runs:
  * `./gradlew clean build --info --stacktrace`
  * `./gradlew org.eclipse.fennec.model.atlas.runtime:resolve.modelatlas.runtime_base --info`
  * `./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_apicurio --info`
  * `./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_file --info`
* Uploads three artifact bundles:
  * `jar-files` — the executable JARs and the REST application bundle
  * `Problem-report` — Gradle problems-report HTML, only on failure
  * `Test-reports` — JUnit XML for all test modules

### `container_deploy`

Runs only when **both** of the following are true:
* `github.event_name == 'push'`
* Ref is `refs/heads/main` *or* `refs/heads/snapshot`

So PRs never reach this job and untrusted forks cannot trigger image
publishes.

Steps:
1. Re-checkout, set up JDK 21.
2. Download the `jar-files` artifact from the upstream `build` job.
3. Install the BND CLI from Maven Central
   (`biz.aQute.bnd-7.2.1.jar`) and read `Bundle-Version` from
   `org.eclipse.fennec.model.atlas.rest.application.jar`.
4. Stage the runtime jars under `docker/modelatlas_apicurio/content/` and
   `docker/modelatlas_file/content/`.
5. Set up Docker Buildx and log in to Docker Hub + GHCR.
6. Build and push two multi-arch images (`linux/amd64,linux/arm64/v8`) per
   storage variant, tagged according to the branch:
   * `snapshot` → `:apicurio-snapshot` / `:file-snapshot` plus the
     version-specific tag
   * `main` → `:apicurio-latest` / `:file-latest` plus the
     version-specific tag

* **Secrets used by the deploy job:**
  * `DOCKER_USERNAME`, `DOCKER_API_TOKEN` — Docker Hub credentials
  * `GITHUB_TOKEN` (auto-provided) — used to push to `ghcr.io/eclipse-fennec/model.atlas`

## `license.yml` — License header check

* **File:** [`.github/workflows/license.yml`](../.github/workflows/license.yml)
* **Triggers:** `workflow_dispatch` **only**. The workflow does *not* run
  automatically on push or pull request.
* **Recommendation:** add `push:` and `pull_request:` triggers (limited to
  `main` / `snapshot`) to surface header regressions during review, the way
  the other Fennec repos do it. Until that is done, run the check locally:
  ```bash
  docker run --rm -v $(pwd):/github/workspace \
    ghcr.io/apache/skywalking-eyes/license-eye header check
  ```
* **Purpose:** Verify every source file carries the Eclipse Public License
  2.0 header. Uses [apache/skywalking-eyes](https://github.com/apache/skywalking-eyes)
  driven by [`.licenserc.yaml`](../.licenserc.yaml).

## Published artifacts

Model Atlas publishes multi-arch container images
(`linux/amd64,linux/arm64/v8`) to **two registries** with mirrored tags.

| Channel      | Registry & repository                                                                                  | Image tags                                                                                                                | Pushed by                                                       |
|--------------|--------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| Release      | [Docker Hub `eclipsefennec/model.atlas`](https://hub.docker.com/r/eclipsefennec/model.atlas/tags)      | `apicurio-latest`, `file-latest`, `apicurio-<version>`, `file-<version>`                                                  | `build.yml` *Container Deploy* job on `main`                    |
| Release      | [GHCR `ghcr.io/eclipse-fennec/model.atlas`](https://github.com/eclipse-fennec/model.atlas/pkgs/container/model.atlas) | same tags as Docker Hub                                                                                                   | `build.yml` *Container Deploy* job on `main`                    |
| Snapshot     | Docker Hub `eclipsefennec/model.atlas`                                                                 | `apicurio-snapshot`, `file-snapshot`, `apicurio-<version>`, `file-<version>`                                              | `build.yml` *Container Deploy* job on `snapshot`                |
| Snapshot     | GHCR `ghcr.io/eclipse-fennec/model.atlas`                                                              | same tags as Docker Hub                                                                                                   | `build.yml` *Container Deploy* job on `snapshot`                |

Pull examples:

```bash
# Latest release (file variant)
docker pull eclipsefennec/model.atlas:file-latest

# Pinned release version (apicurio variant)
docker pull eclipsefennec/model.atlas:apicurio-0.0.1

# Latest snapshot (file variant)
docker pull eclipsefennec/model.atlas:file-snapshot

# From GHCR instead of Docker Hub
docker pull ghcr.io/eclipse-fennec/model.atlas:apicurio-latest
```

The version-specific tags (`apicurio-<bundle-version>` /
`file-<bundle-version>`) are derived at build time from `Bundle-Version`
of `org.eclipse.fennec.model.atlas.rest.application.jar`, so they are
stable references to a specific commit's output.

No Maven artifacts are published from this repository.

## Secrets

| Secret name           | Purpose                                              |
|-----------------------|------------------------------------------------------|
| `DOCKER_USERNAME`     | Docker Hub user                                       |
| `DOCKER_API_TOKEN`    | Docker Hub access token                               |
| `GITHUB_TOKEN`        | Auto-provided; used to push to `ghcr.io/eclipse-fennec` |

No GPG signing secrets are required — this repository does not publish
Maven artifacts; only container images.

## Reproducing CI locally

* Full PR build:
  ```bash
  ./gradlew clean build --info
  ```
* Build the apicurio image locally:
  ```bash
  ./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_apicurio
  ./gradlew docker:modelatlas_apicurio:prepareDocker
  docker build -t eclipsefennec/model.atlas:apicurio-snapshot docker/modelatlas_apicurio/
  ```
* License headers:
  ```bash
  docker run --rm -v $(pwd):/github/workspace \
    ghcr.io/apache/skywalking-eyes/license-eye header check
  ```
