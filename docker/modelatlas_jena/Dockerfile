ARG APP_HOME=/opt/modelatlas
ARG RUNTIME_DIR=${APP_HOME}/runtime
ARG DATA_DIR=${RUNTIME_DIR}/data
ARG LOG_DIR=${RUNTIME_DIR}/log
ARG LOAD_DIR=${RUNTIME_DIR}/load
ARG TMPDIR=/tmp/modelatlas
ARG RUNTIME_JAR=${APP_HOME}/modelatlas.runtime_docker.jar

FROM eclipse-temurin:21-jre-alpine AS builder

ARG APP_HOME
ARG RUNTIME_DIR
ARG DATA_DIR
ARG LOG_DIR
ARG LOAD_DIR
ARG TMPDIR
ARG RUNTIME_JAR

RUN set -eux; \
    mkdir -p "${APP_HOME}" "${DATA_DIR}" "${LOG_DIR}" "${LOAD_DIR}" "${TMPDIR}"; \
    chown -R 65532:65532 "${APP_HOME}" "${TMPDIR}"; \
    chmod -R u+rwX,go-rwx "${DATA_DIR}" "${LOG_DIR}" "${LOAD_DIR}" "${TMPDIR}"

COPY content/modelatlas.runtime_docker.jar "${RUNTIME_JAR}"
COPY content/runtime/ "${RUNTIME_DIR}/"

RUN chown -R 65532:65532 "${APP_HOME}"

FROM gcr.io/distroless/java21-debian12:nonroot

ARG APP_HOME
ARG RUNTIME_DIR
ARG DATA_DIR
ARG LOG_DIR
ARG LOAD_DIR
ARG TMPDIR
ARG RUNTIME_JAR

ENV APP_HOME=${APP_HOME} \
    RUNTIME_DIR=${RUNTIME_DIR} \
    DATA_DIR=${DATA_DIR} \
    LOG_DIR=${LOG_DIR} \
    LOAD_DIR=${LOAD_DIR} \
    TMPDIR=${TMPDIR} \
    RUNTIME_JAR=${RUNTIME_JAR}

COPY --from=builder ${APP_HOME} ${APP_HOME}
COPY --from=builder --chown=65532:65532 ${TMPDIR} ${TMPDIR}

EXPOSE 8080
WORKDIR ${APP_HOME}

ENTRYPOINT ["java", \
    "-Dgosh.args=--nointeractive", \
    "-Ddata=/opt/modelatlas/runtime/data", \
    "-Datlas.log.dir=/opt/modelatlas/runtime/log", \
    "-Dlogback.configurationFile=file:/opt/modelatlas/runtime/etc/logback.xml", \
    "-Djava.io.tmpdir=/tmp/modelatlas", \
    "-jar", "/opt/modelatlas/modelatlas.runtime_docker.jar"]
