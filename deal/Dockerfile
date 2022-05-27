FROM maven:3.8.1-openjdk-11 AS MAVEN_BUILD

COPY pom.xml /build/
COPY src /build/src/

WORKDIR /build/
RUN mvn clean install -Dmaven.test.skip=true

FROM openjdk:11

WORKDIR /app

COPY --from=MAVEN_BUILD /build/target/deal-0.0.1-SNAPSHOT.jar /app/

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "deal-0.0.1-SNAPSHOT.jar"]