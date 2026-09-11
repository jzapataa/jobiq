FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml
RUN chmod +x mvnw

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre-ubi10-minimal AS runtime
WORKDIR /app

COPY --from=build /workspace/target/jobiq-backend-*.jar /app/jobiq.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/jobiq.jar"]
