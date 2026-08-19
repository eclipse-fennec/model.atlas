# Model Atlas - Git Variant

This Docker image runs Model Atlas with the **read-only git storage backend**: models/instances
are served from a remote git repository (branch = stage) and kept in sync via a reconcile poll
and/or webhooks. Writing happens externally on the git host; Model Atlas only reads.

## Prerequisites

- **Repository access.** `GIT_REPO` may be an scp-like SSH URL (`git@host:owner/repo.git`),
  `ssh://`, `https://` or `git://`. For SSH remotes provide a private key with read access
  (e.g. a read-only deploy key) — `org.eclipse.fennec.jgit` uses jgit’s Apache MINA sshd backend, so
  **ed25519 / OpenSSH-format keys are fine**. Anonymous `https://`/`git://` remotes need no
  key (the SSH stack is only engaged for SSH transports).
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
(`/…/github`, `/…/gitlab`). Each provider's endpoint only exists while its ConfigAdmin pid
(`org.eclipse.fennec.git.webhook.github` / `…git.webhook.gitlab`) is present — the shipped
`workflow.json` configures both; remove a block to drop that provider's endpoint entirely.
Set `GIT_WEBHOOK_GITHUB_SECRET` / `GIT_WEBHOOK_GITLAB_TOKEN` and flip `requireSignature` to
`true` in the config for production.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `GIT_REPO` | `git@github.com:acme/models.git` | URL of the repository to serve (SSH, `https://` or `git://`) |
| `GIT_PRIVATE_KEY` | `/opt/modelatlas/runtime/secrets/git_deploy_key` | Path to the SSH private key (only used for SSH remotes) |
| `GIT_PRIVATE_KEY_PASSPHRASE` | *(empty)* | Passphrase for the key, if any |
| `GIT_WEBHOOK_GITHUB_SECRET` | *(empty)* | HMAC secret for GitHub push webhooks |
| `GIT_WEBHOOK_GITLAB_TOKEN` | *(empty)* | Token for GitLab push webhooks |

The scope, registries and branch→stage mapping live in the `runtime.config.docker.git` bundle
(`configs/workflow.json`); adjust the branches/stages there to match your repository.
