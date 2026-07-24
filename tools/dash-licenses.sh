#!/usr/bin/env bash
#
# Copyright (c) 2026 Contributors to the Eclipse Foundation.
#
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#     Data In Motion - initial API and implementation
#
# Generate the Eclipse Dash "DEPENDENCIES" file for this bnd workspace.
#
# It uses the new `bnd repo deps` subcommand (bnd 7.4.0-SNAPSHOT or newer) to
# export the Maven GAVs of every artifact referenced by the workspace's Maven
# repositories, then feeds that list to the Eclipse Dash License Tool
# (dash-licenses) to produce the DEPENDENCIES summary used by IP Dash.
#
# Local usage (e.g. from Git Bash on Windows or any shell on Linux/macOS):
#   tools/dash-licenses.sh                 # regenerate DEPENDENCIES, then commit it
#   tools/dash-licenses.sh --review        # additionally open IP review issues
#
# Exit code is the number of dependencies that are "restricted" (i.e. not yet
# vetted / need IP review). 0 means everything is approved. CI relies on this.

set -euo pipefail

# ---- defaults -------------------------------------------------------------
BND_VERSION="${BND_VERSION:-7.4.0-SNAPSHOT}"
BND_SNAPSHOTS="${BND_SNAPSHOTS:-https://bndtools.jfrog.io/bndtools/libs-snapshot-local}"
DASH_VERSION="${DASH_VERSION:-1.1.0}"
DASH_RELEASES="${DASH_RELEASES:-https://repo.eclipse.org/content/repositories/dash-licenses}"

REVIEW=false
TOKEN="${DASH_IPLAB_TOKEN:-}"
PROJECT="${DASH_PROJECT_ID:-}"
SUMMARY=""

usage() {
  cat <<'EOF'
Generate the Eclipse Dash "DEPENDENCIES" file for this bnd workspace.

Usage:
  tools/dash-licenses.sh [options]

Options:
  --review              Open IP review issues in the Eclipse GitLab IP Lab.
                        Requires --token and --project (or the matching env vars).
  --token <token>       GitLab API token        (env: DASH_IPLAB_TOKEN)
  --project <id>        Eclipse project id, e.g. technology.fennec
                                                (env: DASH_PROJECT_ID)
  --summary <file>      DEPENDENCIES output file (default: <repo-root>/DEPENDENCIES)
  --bnd-version <v>     bnd snapshot version     (default: 7.4.0-SNAPSHOT)
  --dash-version <v>    dash-licenses version    (default: 1.1.0)
  -h, --help            Show this help.
EOF
}

# ---- arg parsing ----------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --review)       REVIEW=true; shift ;;
    --token)        TOKEN="$2"; shift 2 ;;
    --project)      PROJECT="$2"; shift 2 ;;
    --summary)      SUMMARY="$2"; shift 2 ;;
    --bnd-version)  BND_VERSION="$2"; shift 2 ;;
    --dash-version) DASH_VERSION="$2"; shift 2 ;;
    -h|--help)      usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage; exit 2 ;;
  esac
done

# ---- locate workspace -----------------------------------------------------
ROOT="$(git rev-parse --show-toplevel)"
CACHE="$ROOT/cnf/cache/dash-licenses"   # under the gitignored cnf/cache
mkdir -p "$CACHE"
SUMMARY="${SUMMARY:-$ROOT/DEPENDENCIES}"
DEPS="$CACHE/deps.txt"

# ---- resolve & download the bnd CLI snapshot ------------------------------
if [[ "$BND_VERSION" == *-SNAPSHOT ]]; then
  echo ">> Resolving latest $BND_VERSION snapshot of the bnd CLI ..."
  META="$(curl -fsSL "$BND_SNAPSHOTS/biz/aQute/bnd/biz.aQute.bnd/$BND_VERSION/maven-metadata.xml")"
  TS="$(printf '%s' "$META" | sed -n 's:.*<timestamp>\(.*\)</timestamp>.*:\1:p' | head -1)"
  BN="$(printf '%s' "$META" | sed -n 's:.*<buildNumber>\(.*\)</buildNumber>.*:\1:p' | head -1)"
  BND_JAR_VERSION="${BND_VERSION%-SNAPSHOT}-${TS}-${BN}"
  BND_URL="$BND_SNAPSHOTS/biz/aQute/bnd/biz.aQute.bnd/$BND_VERSION/biz.aQute.bnd-${BND_JAR_VERSION}.jar"
else
  BND_JAR_VERSION="$BND_VERSION"
  BND_URL="https://repo.maven.apache.org/maven2/biz/aQute/bnd/biz.aQute.bnd/$BND_VERSION/biz.aQute.bnd-${BND_VERSION}.jar"
fi
BND_JAR="$CACHE/biz.aQute.bnd-${BND_JAR_VERSION}.jar"
if [[ ! -f "$BND_JAR" ]]; then
  echo ">> Downloading bnd CLI ${BND_JAR_VERSION} ..."
  curl -fsSL -o "$BND_JAR" "$BND_URL"
fi

# ---- download the dash-licenses tool --------------------------------------
DASH_JAR="$CACHE/org.eclipse.dash.licenses-${DASH_VERSION}.jar"
if [[ ! -f "$DASH_JAR" ]]; then
  echo ">> Downloading dash-licenses ${DASH_VERSION} ..."
  curl -fsSL -o "$DASH_JAR" \
    "$DASH_RELEASES/org/eclipse/dash/org.eclipse.dash.licenses/$DASH_VERSION/org.eclipse.dash.licenses-${DASH_VERSION}.jar"
fi

# ---- 1) export the dependency list ----------------------------------------
echo ">> Exporting dependency GAVs with 'bnd repo deps' ..."
( cd "$ROOT" && java -jar "$BND_JAR" repo deps -o "$DEPS" )
echo ">> $(wc -l < "$DEPS" | tr -d ' ') dependencies written to $DEPS"

# ---- 2) run dash-licenses -------------------------------------------------
DASH_ARGS=("$DEPS" -summary "$SUMMARY")
if [[ "$REVIEW" == true ]]; then
  if [[ -z "$TOKEN" || -z "$PROJECT" ]]; then
    echo "ERROR: --review needs a GitLab token (--token / DASH_IPLAB_TOKEN) and an Eclipse project id (--project / DASH_PROJECT_ID)." >&2
    exit 2
  fi
  echo ">> Running dash-licenses in review mode for project '$PROJECT' ..."
  DASH_ARGS+=(-review -token "$TOKEN" -project "$PROJECT")
else
  echo ">> Running dash-licenses ..."
fi

set +e
java -jar "$DASH_JAR" "${DASH_ARGS[@]}"
RC=$?
set -e

echo ">> DEPENDENCIES written to $SUMMARY"
if [[ $RC -ne 0 ]]; then
  echo ">> $RC dependency/dependencies are restricted (need IP review). See output above."
fi
exit $RC
