FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
COPY services/api/target/api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
