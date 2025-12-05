FROM eclipse-temurin:21.0.9_10-jre-alpine

ENV APP_HOME=/opt/modelatlas \
    RUNTIME_DIR=/opt/modelatlas/runtime \
    DATA_DIR=/opt/modelatlas/runtime/data \
    LOG_DIR=/opt/modelatlas/runtime/log \
    TMPDIR=/tmp/modelatlas \
    RUNTIME_JAR=/opt/modelatlas/modelatlas.runtime_docker.jar

RUN set -eux; \
    addgroup -g 7743 -S modelatlas; \
    adduser -u 7743 -h /home/modelatlas -s /sbin/nologin -S modelatlas -G modelatlas; \
    mkdir -p "${APP_HOME}"

COPY content/modelatlas.runtime_docker.jar "${RUNTIME_JAR}"
COPY content/runtime/ "${RUNTIME_DIR}/"

RUN set -eux; \
    mkdir -p "${LOG_DIR}" "${TMPDIR}"; \
    chown -R modelatlas:modelatlas "${DATA_DIR}"; \
    chmod -R u+rwX,go-rwx "${DATA_DIR}"; \
    chown modelatlas:modelatlas "${LOG_DIR}"; \
    chmod 0700 "${LOG_DIR}"; \
    chown modelatlas:modelatlas "${TMPDIR}"; \
    chown -R modelatlas:modelatlas "${LOG_DIR}"; \
    chmod -R u+rwX,go-rwx "${LOG_DIR}"; \
    chmod 0700 "${TMPDIR}"

EXPOSE 8080
WORKDIR ${APP_HOME}
USER modelatlas

CMD ["java", "-Dgosh.args=--nointeractive", "-Ddata=${DATA_DIR}", "-Datlas.log.dir=/opt/modelatlas/runtime/log", "-Dlogback.configurationFile=file:/opt/modelatlas/runtime/etc/logback.xml", "-Djava.io.tmpdir=/tmp/modelatlas", "-jar", "/opt/modelatlas/modelatlas.runtime_docker.jar"]
