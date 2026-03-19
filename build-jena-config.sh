#!/bin/bash
set -euo pipefail

./gradlew :org.eclipse.fennec.model.atlas.runtime.config.local.jena:build

cp org.eclipse.fennec.model.atlas.runtime.config.local.jena/generated/org.eclipse.fennec.model.atlas.runtime.config.local.jena.jar \
   docker/dockercompose/
