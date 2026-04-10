FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/qqauto-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8086
ENV APP_CONTEXT_PATH=/qqauto

EXPOSE 8086

ENTRYPOINT ["java","-jar","/app/app.jar"]
