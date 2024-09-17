FROM amazoncorretto:22-alpine-jdk

WORKDIR /app
EXPOSE 25565
ENV SERVER_PORT=25565
COPY ./schematics ./schematics

ENTRYPOINT [ "java", "-jar", "airbrush.jar" ]