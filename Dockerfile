FROM openjdk:11

ARG JAR_FILE

COPY ${JAR_FILE} /app/application.jar

WORKDIR /app
RUN chgrp -R 0 /app && chmod -R g+rwX /app

ENTRYPOINT ["java", "-jar", "application.jar"]