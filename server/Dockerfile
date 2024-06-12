FROM amazoncorretto:11
COPY target/*.jar /server.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/server.jar"]