FROM amazoncorretto:11
COPY target/*.jar /gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/gateway.jar"]