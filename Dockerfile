FROM amazoncorretto:21

# healthcheck용 curl 설치 (amazoncorretto = Amazon Linux 기반)
RUN yum install -y curl && yum clean all

WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8099

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=180s \
  CMD curl -fsS http://localhost:8099/actuator/health/liveness || exit 1

CMD ["java", "-jar", "app.jar", "--server.port=${CATCONNECT_TOMCAT_PORT:-8099}"]
