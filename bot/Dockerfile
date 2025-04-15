ARG RUNTIME_IMAGE

FROM ${RUNTIME_IMAGE}

WORKDIR /opt/app
USER nobody
COPY target/bot-1.0.jar .

ENTRYPOINT ["java", "-jar", "bot-1.0.jar"]
