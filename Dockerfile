FROM amazoncorretto:21
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8099
CMD ["java", "-jar", "app.jar", "--server.port=${CATCONNECT_TOMCAT_PORT:-8099}"]