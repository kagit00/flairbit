# Stage 1: Build the Spring Boot app
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom first for caching
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the JAR (skip tests for faster build)
RUN mvn clean package -DskipTests --no-transfer-progress

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install netcat for dependency checks
RUN apt-get update \
    && apt-get install -y netcat \
    && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# CONTAINER-AWARE JVM SETTINGS (SAFETY NET)
ENV JAVA_OPTS="\
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=70 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom"

# Wait for all dependencies to be ready, then start the app
ENTRYPOINT ["sh", "-c", "\
mkdir -p /logs; \
echo \"==== NEW RUN `date` ====\" >> /logs/flairbit.log; \
until nc -z kafka 9092; do echo 'Waiting for Kafka...' >> /logs/flairbit.log; sleep 2; done; \
until nc -z postgres_alt 5432; do echo 'Waiting for PostgreSQL...' >> /logs/flairbit.log; sleep 2; done; \
until nc -z redis_alt 6379; do echo 'Waiting for Redis...' >> /logs/flairbit.log; sleep 2; done; \
echo 'All dependencies are up! Starting app...' >> /logs/flairbit.log; \
java ${JAVA_OPTS} -jar app.jar >> /logs/flairbit.log 2>&1"]
