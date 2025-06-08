
FROM gradle:jdk21-alpine AS build


WORKDIR /app


COPY build.gradle.kts .
COPY gradlew .
COPY gradle gradle
COPY src src
COPY ssl-certs ssl-certs

RUN chmod +x gradlew
RUN ./gradlew clean build


FROM eclipse-temurin:21-jre-jammy AS runtime


WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/my-app.jar
COPY --from=build /app/ssl-certs/keystore.p12 /app/keystore.p12

EXPOSE 8080
#dummy

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/app/my-app.jar"]