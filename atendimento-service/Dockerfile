FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY commons ./commons
COPY admin-service ./admin-service
COPY agendamento-service ./agendamento-service
COPY atendimento-service ./atendimento-service

RUN mvn -pl atendimento-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/atendimento-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]