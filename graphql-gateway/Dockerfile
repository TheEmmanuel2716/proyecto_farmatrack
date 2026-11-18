FROM docker.io/library/maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q clean package -DskipTests

FROM docker.io/library/eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --home-dir /app --shell /usr/sbin/nologin appuser
COPY --from=build /workspace/target/graphql-gateway-0.0.1-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
