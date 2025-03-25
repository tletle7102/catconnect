FROM amazoncorretto:21
WORKDIR /app
COPY build/libs/catconnect-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar", "-Dspring.profiles.active=${CATCONNECT_SPRING_PROFILE_ACTIVE}", "--server.port=${CATCONNECT_TOMCAT_PORT}"]