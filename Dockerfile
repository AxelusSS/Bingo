# Étape 1 : Compilation (Maven)
FROM maven:3.8.4-openjdk-17-slim AS build
COPY src /app/src
COPY pom.xml /app
RUN mvn -f /app/pom.xml clean package

# Étape 2 : Runtime (Le serveur de jeu)
FROM itzg/minecraft-server:latest

# On récupère le JAR du Bingo
COPY --from=build /app/target/*.jar /data/plugins/Bingo.jar

# Configuration pour un serveur éphémère
ENV EULA=TRUE
ENV TYPE=PAPER
ENV VERSION=1.20.4
ENV MEMORY=2G

# On désactive le redémarrage automatique dans le container
ENV RESTART_ON_CRASH=false

EXPOSE 25565
