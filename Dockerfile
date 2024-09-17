# Official Java 17 Runtime as basic image
FROM openjdk:17-jdk
WORKDIR /app
COPY target/echoai-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
