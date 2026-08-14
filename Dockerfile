FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

USER 1000

ENTRYPOINT ["java", "-jar", "app.jar"]