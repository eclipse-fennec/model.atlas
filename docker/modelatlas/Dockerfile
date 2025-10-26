FROM eclipse-temurin:21-jre-alpine

ENV USERNAME=modelatlas \
	HOME_DIR=/modelatlas \
	OPT_DIR=/opt/modelatlas \
	GECKO_VERSION=2.0.0

RUN mkdir -p ${OPT_DIR} && \
    mkdir -p ${OPT_DIR}/logs && \
    mkdir -p /tmp

COPY content/ ${OPT_DIR}/

RUN mkdir ${HOME_DIR}

RUN addgroup -g 7743 -S ${USERNAME} && \
    adduser -u 7743 -h ${HOME_DIR} -s /bin/false -S ${USERNAME} -G ${USERNAME}

RUN chown -R ${USERNAME} ${OPT_DIR} && \
    chmod -R u+rwx ${OPT_DIR} && \
    chown -R ${USERNAME} /tmp && \
    chmod -R u+rw /tmp

RUN cd ${OPT_DIR}

EXPOSE 8080 8081 8082 8083 8084 8085 8090
WORKDIR ${OPT_DIR}
USER ${USERNAME}

CMD ["java", "-Dgosh.args=--nointeractive", "-Dgecko.data.dir=/opt/modelatlas/runtime/data", "-Dgecko.conf.dir=/opt/modelatlas/runtime/etc", "-Dgecko.base.dir=/opt/modelatlas", "-Dgecko.log.dir=/opt/modelatlas/runtime/log", "-Dlogback.configurationFile=file:/opt/modelatlas/runtime/etc/logback.xml",  "-jar", "org.civitas.model.atlas.runtime.jar"]
