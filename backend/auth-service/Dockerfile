# ─────────────────────────────────────────────
# Etapa 1: Compilación
# ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./pom.xml
COPY auth-service/pom.xml auth-service/pom.xml

RUN mvn dependency:go-offline -pl auth-service -am -q

COPY auth-service/src auth-service/src
RUN mvn clean package -pl auth-service -am -DskipTests

# ─────────────────────────────────────────────
# Etapa 2: Imagen de ejecución
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/auth-service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
