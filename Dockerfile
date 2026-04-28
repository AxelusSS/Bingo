# Étape 1 : Compilation (Maven)
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# On télécharge les dépendances en avance pour le cache
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Runtime
FROM itzg/minecraft-server:latest
WORKDIR /data

# On récupère le plugin
COPY --from=build /app/target/*.jar /data/plugins/GamePlugin.jar

# Scripts de configuration
COPY init-game.sh /init-game.sh
RUN chmod +x /init-game.sh

# Variables par défaut
ENV EULA=TRUE \
    TYPE=PAPER \
    VERSION=1.21.1 \
    MEMORY=2G \
    ONLINE_MODE=FALSE \
    RESTART_ON_CRASH=false

# On utilise notre script pour configurer le jeu au boot
ENTRYPOINT ["/init-game.sh"]
