#!/bin/bash

# Script d'initialisation du serveur de jeu HEL
echo "[HEL-INIT] Configuration du serveur pour le mode : ${GAME_TYPE:-BINGO}"

# Configuration du MOTD selon le jeu
case "$GAME_TYPE" in
    "BINGO")
        export MOTD="§6§lHEL NETWORK §8- §eBINGO"
        ;;
    "UHC")
        export MOTD="§6§lHEL NETWORK §8- §cUHC"
        ;;
    "UHC_RUN")
        export MOTD="§6§lHEL NETWORK §8- §eUHC RUN"
        ;;
    "JUMP")
        export MOTD="§6§lHEL NETWORK §8- §aJUMP"
        ;;
    *)
        export MOTD="§6§lHEL NETWORK §8- §7Serveur de Jeu"
        ;;
esac

# On s'assure que le serveur est bien en 1.21.1
export VERSION="1.21.1"

# Ici on pourrait ajouter de la logique pour copier des maps 
# ou des fichiers de config spécifiques selon le GAME_TYPE
# ex: cp /presets/${GAME_TYPE}.yml /data/plugins/Bingo/config.yml

echo "[HEL-INIT] Lancement de Minecraft..."
exec /start
