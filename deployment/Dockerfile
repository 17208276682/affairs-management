FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY affairs-management-1.0.0-SNAPSHOT.jar app.jar
VOLUME /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
