# ─────────────────────────────────────────────
# Etapa 1: Compilación con Maven
# ─────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar POM padre (necesario para el módulo hijo)
COPY pom.xml ./pom.xml

# Copiar POM del módulo eureka-server
COPY eureka-server/pom.xml eureka-server/pom.xml

# Descargar dependencias (aprovecha la caché de Docker)
RUN mvn dependency:go-offline -pl eureka-server -am -q

# Copiar fuentes del módulo y compilar
COPY eureka-server/src eureka-server/src
RUN mvn clean package -pl eureka-server -am -DskipTests

# ─────────────────────────────────────────────
# Etapa 2: Imagen de ejecución (Alpine – ligera)
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/eureka-server/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
