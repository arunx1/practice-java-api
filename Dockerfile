FROM maven:3.9.11-eclipse-temurin-24-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests && mvn clean

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/target/java-api-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]