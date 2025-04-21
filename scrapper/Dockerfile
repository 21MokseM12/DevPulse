ARG RUNTIME_IMAGE

FROM ${RUNTIME_IMAGE}

WORKDIR /opt/app
USER nobody
COPY target/scrapper-1.0.jar .

ENTRYPOINT ["java", "-jar", "scrapper-1.0.jar"]
