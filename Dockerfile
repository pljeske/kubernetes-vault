#FROM openjdk:11
FROM openjdk:11-jre-slim

ARG JAR_FILE

RUN useradd vaultuser && usermod -aG 0 vaultuser

COPY --chown=vaultuser ${JAR_FILE} /app/application.jar

COPY --chown=vaultuser keys/ /app/keys/

RUN chgrp -R 0 /app && chmod -R g+rwX /app

WORKDIR /app
USER vaultuser

ENTRYPOINT ["java", "-jar", "application.jar"]