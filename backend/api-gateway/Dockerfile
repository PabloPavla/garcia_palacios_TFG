# ─────────────────────────────────────────────
# Etapa 1: Compilación con Maven
# ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml

RUN mvn dependency:go-offline -pl api-gateway -am -q

COPY api-gateway/src api-gateway/src
RUN mvn clean package -pl api-gateway -am -DskipTests

# ─────────────────────────────────────────────
# Etapa 2: Imagen de ejecución
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
