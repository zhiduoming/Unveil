# 构建：mvn package -pl jieqi-app -am -DskipTests
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY jieqi-core/pom.xml jieqi-core/pom.xml
COPY jieqi-server/pom.xml jieqi-server/pom.xml
COPY jieqi-client/pom.xml jieqi-client/pom.xml
COPY jieqi-ai/pom.xml jieqi-ai/pom.xml
COPY jieqi-app/pom.xml jieqi-app/pom.xml
COPY jieqi-core jieqi-core
COPY jieqi-server jieqi-server
COPY jieqi-client jieqi-client
COPY jieqi-ai jieqi-ai
COPY jieqi-app jieqi-app
RUN mvn -q package -pl jieqi-app -am -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/jieqi-app/target/unveil-jieqi.jar /app/unveil-jieqi.jar
EXPOSE 8887
ENV JIEQI_PORT=8887
ENTRYPOINT ["java", "-jar", "/app/unveil-jieqi.jar", "server-ws", "8887"]
