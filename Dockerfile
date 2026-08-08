FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/text-matcher-*.jar app.jar

COPY testingFiles ./testingFiles

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]