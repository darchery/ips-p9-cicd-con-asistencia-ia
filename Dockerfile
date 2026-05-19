FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built jar (the workflow will build the project and place the jar in target/)
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
