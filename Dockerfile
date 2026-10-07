# ONE Dockerfile in the ROOT (microservice-new/). Used by every service via build arg MODULE.
FROM maven:3.9-eclipse-temurin-26 AS build
ARG MODULE
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -pl ${MODULE} -am clean package -DskipTests \
    && cp ${MODULE}/target/*.jar /app/app.jar

FROM eclipse-temurin:26-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

