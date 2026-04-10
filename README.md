# 🎲 Minecraft Bingo Classique

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)
![PaperMC](https://img.shields.io/badge/API-PaperMC-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)

Un plugin performant, complet et *Vanilla-friendly* reproduisant le célèbre mode de jeu **Bingo**. Pas de mods requis du côté des joueurs : l'entièreté de l'interface et des défis (Advancements) sont générés par le serveur !

---

## ✨ Fonctionnalités Principales

*   **🏆 Succès Dynamiques (Datapack Auto-Généré) :** La grille de Bingo utilise la fameuse touche `[L]` (Advancements) ! Les objectifs se dessinent au centre de l'écran — items trouvés affichent une ★ étoile.
*   **📐 Grille Personnalisable :** Jouez sur le classique 5x5, le rapide 3x3, ou augmentez la difficulté jusqu'au 7x7 !
*   **🎯 ~150 Objectifs + ~40 Achievements :** Items (armures, potions, blocs du Nether/End...) et vrais achievements Minecraft (tuer un mob, brewer une potion...).
*   **⚔️ Système de Difficulté :** 4 niveaux — Facile, Normal, Difficile, Extrême. Chaque objectif est classé par difficulté.
*   **🎮 3 Modes de Jeu :** Items seuls, Achievements seuls, ou Mixte.
*   **🧭 GUI Admin (Compas) :** Interface visuelle complète pour configurer la partie sans taper de commandes — taille, difficulté, mode, durée, équipes, génération, lancement, reset.
*   **👥 Team System Poussé :** Limiteur de joueurs par équipe, sélection visuelle via bannières, assignation de force, répartition aléatoire.
*   **💬 Chat Privé d'Équipe :** Les messages normaux ne sont lus que par vos co-équipiers. Utilisez `!message` pour le chat global. Les `/msg` sont bloqués en jeu.
*   **⏸️ Pause Réelle :** `/bingo pause` freeze tous les joueurs (mouvement, minage, dégâts bloqués). `/bingo resume` pour reprendre.
*   **⚡ Scoring & Blackout :**
    *   **+1 Point** par case.
    *   **+3 Points** de bonus par Ligne/Colonne complète !
    *   **Mode Spectateur automatique** lorsqu'une équipe fait le Blackout (100%).
*   **📈 Scoreboard Dynamique :** Timer, progression de l'équipe, classement en temps réel. 100% configurable dans `config.yml`.
*   **🎆 Effets Sonores & Visuels :** Son de level-up, particules et feux d'artifice lors de la complétion d'objectifs.
*   **📊 Base de Données :** Statistiques SQL (MySQL/SQLite) des temps records pour chaque objectif.
*   **🚀 Ultra-Performance :** Commande `/pregen` gérant l'Asynchronous Chunk Loading de l'API Paper.

---

## 🛠️ Installation

1.  Déposez `Bingo.jar` dans votre dossier `plugins/`.
2.  Assurez-vous de tourner sous **PaperMC 1.21.1** (ou fork compatible) et en **Java 21**.
3.  Redémarrez le serveur. Le dossier `plugins/Bingo/` se créera avec un `config.yml`.
4.  Configurez vos paramètres (Database SQL, Scoreboard) si désiré.

---

## 🎮 Commandes en Jeu

### 👤 Pour les Joueurs
*   `/bingo help` ou `/b` : Affiche l'aide
*   `/bingo grid` ou `/bg` : Ouvre la grille de progression de votre équipe
*   `/team menu` : Ouvre un GUI cliquable pour rejoindre son équipe
*   `/tj <couleur>` : Raccourci pour rejoindre une équipe
*   `/team leave` : Quitter son équipe

### 👑 Pour les Administrateurs (`bingo.admin`)

**Partie :**
*   `/bingo start` : Lance le compte à rebours et démarre la partie
*   `/bingo pause` : Met la partie en pause (freeze les joueurs). Refaire = reprendre
*   `/bingo resume` : Reprend la partie après une pause
*   `/bingo reset` : Réinitialise complètement la partie

**Configuration :**
*   `/bingo generate` : Génère une nouvelle grille avec les paramètres actuels
*   `/bingo size <3|5|7>` (alias `/bs`) : Taille de la grille
*   `/bingo difficulty <easy|normal|hard|extreme>` : Niveau de difficulté
*   `/bingo mode <items|achievements|mixed>` : Mode de jeu
*   `/bingo time <minutes>` : Durée de la partie
*   🧭 **Compas dans la hotbar** : Ouvre le GUI de configuration (alternative aux commandes)

**Équipes :**
*   `/team random <n>` : Répartition aléatoire des joueurs
*   `/team lock` : Verrouiller/Déverrouiller les équipes
*   `/team setsize <max>` : Capacité maximale par équipe
*   `/team set <joueur> <couleur>` : Forcer un joueur dans une équipe

**Utilitaires :**
*   `/pregen <rayon>` : Pré-génération asynchrone des chunks

---

## ⚙️ GUI Admin (Compas)

En phase d'attente, les admins reçoivent automatiquement un **compas** dans leur hotbar. Clic droit pour ouvrir :

```
[ Taille ] [ ] [ Difficulté ] [ ] [ Mode ] [ ] [ ⏱ Durée ] [ ] [ Lock ]
[ ] [ ] [ ] [ Random ] [ ] [ GÉNÉRER ] [ ] [ START ] [ ] [ ]
[ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ RESET ] [ ]
```

---

## 📝 Crédits

Plugins by **HEL** 🌌
