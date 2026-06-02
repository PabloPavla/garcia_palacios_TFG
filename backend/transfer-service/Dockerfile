FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY transfer-service/pom.xml transfer-service/pom.xml
RUN mvn dependency:go-offline -pl transfer-service -am -q
COPY transfer-service/src transfer-service/src
RUN mvn clean package -pl transfer-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=build /app/transfer-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
