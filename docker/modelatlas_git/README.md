# Model Atlas - Git Variant

This Docker image runs Model Atlas with the **read-only git storage backend**: models/instances
are served from a remote git repository (branch = stage) and kept in sync via a reconcile poll
and/or webhooks. Writing happens externally on the git host; Model Atlas only reads.

## Prerequisites

- **SSH access.** `org.gecko.jgit` is SSH-only, so `GIT_REPO` must be an scp-like SSH URL
  (`git@host:owner/repo.git`) and you must provide a private key with read access. `https://`
  and `git://` are **not** supported by the current git transport.
- The repository must contain the branches configured as stages (default: `draft`, `approved`,
  `release`), each holding the `.ecore` schemas (and `.xmi` instances) to serve.

## Usage

```bash
docker run -d -p 8080:8080 \
  -e GIT_REPO="git@github.com:acme/models.git" \
  -e GIT_PRIVATE_KEY="/run/secrets/git_deploy_key" \
  -e GIT_PRIVATE_KEY_PASSPHRASE="" \
  -v /path/to/deploy_key:/run/secrets/git_deploy_key:ro \
  eclipsefennec/model.atlas:git-snapshot
```

Webhooks (optional) are served on the same port under the REST endpoints
(`/…/github`, `/…/gitlab`); set `GIT_WEBHOOK_GITHUB_SECRET` / `GIT_WEBHOOK_GITLAB_TOKEN` and flip
`requireSignature` to `true` in the config for production.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `GIT_REPO` | `git@github.com:acme/models.git` | SSH URL of the repository to serve |
| `GIT_PRIVATE_KEY` | `/opt/modelatlas/runtime/secrets/git_deploy_key` | Path to the SSH private key |
| `GIT_PRIVATE_KEY_PASSPHRASE` | *(empty)* | Passphrase for the key, if any |
| `GIT_WEBHOOK_GITHUB_SECRET` | *(empty)* | HMAC secret for GitHub push webhooks |
| `GIT_WEBHOOK_GITLAB_TOKEN` | *(empty)* | Token for GitLab push webhooks |

The scope, registries and branch→stage mapping live in the `runtime.config.docker.git` bundle
(`configs/workflow.json`); adjust the branches/stages there to match your repository.
