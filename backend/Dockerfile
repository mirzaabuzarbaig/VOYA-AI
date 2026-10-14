# ═══════════════════════════════════════════════════════════════
# Stage 1: Build executable JAR using Maven & Eclipse Temurin JDK 21
# ═══════════════════════════════════════════════════════════════
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache dependency layer
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build production package
COPY src ./src
RUN mvn clean package -DskipTests -B

# ═══════════════════════════════════════════════════════════════
# Stage 2: Minimal production runtime using Eclipse Temurin JRE 21
# ═══════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-privileged user for container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled executable JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

# Default port and JVM flags optimized for Render's 512 MB free tier:
# - SerialGC minimizes GC memory metadata overhead
# - Thread stack size (512k) prevents native memory exhaustion
# - Maximum heap (320m) leaves safe headroom for Metaspace and OS
ENV PORT=8080 \
    JAVA_OPTS="-XX:+UseSerialGC -Xss512k -Xmx320m"

EXPOSE 8080

# Starts application listening on Render's $PORT (or 8080 locally)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
