FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY commons /app/commons
RUN cd /app/commons && mvn clean install -DskipTests

COPY admin-service/pom.xml .
COPY admin-service/src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]