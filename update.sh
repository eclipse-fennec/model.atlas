#!/bin/bash
#
./gradlew clean build -x testOSGi -x test --info

./gradlew org.eclipse.fennec.model.atlas.runtime:resolve.modelatlas.runtime_base
./gradlew org.eclipse.fennec.model.atlas.runtime:export.modelatlas.runtime_docker_jena

./gradlew docker:modelatlas_jena:prepareDocker
docker build -t eclipsefennec/model.atlas:jena-snapshot docker/modelatlas_jena/
docker compose -f docker/dockercompose/docker-compose-jena.yml down
docker compose -f docker/dockercompose/docker-compose-jena.yml up -d --force-recreate

