FROM maven:3.9.9-eclipse-temurin-23 AS build

WORKDIR /build

COPY . .

RUN chmod +x mvnw && ./mvnw -pl bot -am clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=5

FROM eclipse-temurin:23-jre

WORKDIR /opt/app

USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /build/bot/target/bot-*.jar app.jar

USER nobody

ENTRYPOINT ["java", "-jar", "app.jar"]
