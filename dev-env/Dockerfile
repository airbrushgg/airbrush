FROM amazoncorretto:22-alpine-jdk

WORKDIR /app
EXPOSE 25565
ENV SERVER_PORT=25565
COPY ./schematics ./schematics

ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005

ENTRYPOINT [ "java", "-jar", "airbrush.jar" ]