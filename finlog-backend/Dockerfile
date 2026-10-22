FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --system finlog && useradd --system --gid finlog --no-create-home --shell /usr/sbin/nologin finlog

COPY --from=build --chown=finlog:finlog /app/target/*.jar app.jar

USER finlog

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
