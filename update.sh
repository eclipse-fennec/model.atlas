#!/bin/bash
# Copyright (c) 2012 - 2026 Data In Motion and others.
# All rights reserved.
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
./gradlew clean build -x testOSGi -x test --info

./gradlew org.eclipse.fennec.model.atlas.runtime:resolve.modelatlas.runtime_base
./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_jena

./gradlew docker:modelatlas_jena:prepareDocker
docker build -t eclipsefennec/model.atlas:jena-snapshot docker/modelatlas_jena/
docker compose -f docker/dockercompose/docker-compose-jena.yml down
docker compose -f docker/dockercompose/docker-compose-jena.yml up -d --force-recreate

