FROM maven:3.8-amazoncorretto-11 AS MAVEN_BUILD

COPY pom.xml /build/
COPY src /build/src/

WORKDIR /build/
RUN mvn clean install -Dmaven.test.skip=true

FROM amazoncorretto:11.0.17-alpine

WORKDIR /app

COPY --from=MAVEN_BUILD /build/target/dossier-0.0.1-SNAPSHOT.jar /app/

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "dossier-0.0.1-SNAPSHOT.jar"]