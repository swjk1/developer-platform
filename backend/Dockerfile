# Multi-stage: the build image carries Maven and the JDK, the runtime image
# carries neither. Keeps the deployed image small and its attack surface low.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Dependencies are their own layer, so a source-only change does not re-download
# the world on every build.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Never run as root.
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build --chown=app:app /build/target/*.jar app.jar

EXPOSE 8080

# MaxRAMPercentage rather than a fixed -Xmx: the container memory limit is the
# single source of truth, so the same image behaves correctly on a 512 MB
# instance and on the 4 GB VPS.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=50 -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

HEALTHCHECK --interval=30s --timeout=3s --start-period=45s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
