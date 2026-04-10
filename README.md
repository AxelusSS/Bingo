# 🎲 Minecraft Bingo (TheGuill84 Edition)

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)
![PaperMC](https://img.shields.io/badge/API-PaperMC-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)

Un plugin performant, complet et *Vanilla-friendly* reproduisant le célèbre mode de jeu **Bingo de TheGuill84**. Pas de mods requis du côté des joueurs : l'entièreté de l'interface et des défis (Advancements) sont générés par le serveur !

---

## ✨ Fonctionnalités Principales

*   **🏆 Succès Dynamiques (Datapack Auto-Généré) :** La grille de Bingo utilise la fameuse touche `[L]` (Advancements) ! Les objectifs se dessinent au centre de l'écran et se débloquent pour toute la team.
*   **📐 Grille Personnalisable :** Jouez sur le classique 5x5 ou augmentez la difficulté jusqu'au 7x7 voire 10x10 avec une simple commande !
*   **👥 Team System Poussé :** Limiteur de joueurs par équipe, interface de sélection visuelle via bannières (GUI), et assignation de force par l'Admin.
*   **💬 Chat de Stratégie Privé :** Dès le lancement du jeu, aucune faille possible. Les chuchotements (`/msg`) sont bloqués, et les messages normaux ne sont lus que par vos co-équipiers ! (Utilisez `!message` pour le chat global).
*   **⚡ Scoring & Blackout :** 
    *   **+1 Point** par case.
    *   **+3 Points** de bonus par Ligne/Colonne !
    *   **Mode Spectateur automatique** immédiat lorsqu'une équipe remplit le Blackout (100%).
*   **📈 Leaderboard Configurable :** Un tableau des scores dynamique 100% repensable dans `config.yml` avec support du "Tie-Breaker" (le score va au plus rapide à temps égal).
*   **📊 Base de Données Intégrée :** Statistiques SQL (MySQL/SQLite) silencieuses des temps records établis pour le déblocage des objectifs !
*   **🚀 Ultra-Performance :** Inclus la commande `/pregen` gérant *l'Asynchronous Chunk Loading* pur de l'API Paper.

---

## 🛠️ Installation

1.  Déposez `Bingo.jar` dans votre dossier `plugins/`.
2.  Assurez-vous de tourner sous **PaperMC 1.21.1** (ou fork compatible) et en **Java 21**.
3.  Redémarrez le serveur. Le dossier `plugins/Bingo/` se créera avec un `config.yml`.
4.  Configurez vos paramètres (Database SQL, Tableau des scores) si désiré, et tapez `/reload confirm` !

---

## 🎮 Commandes en Jeu

### 👤 Pour les Joueurs
*   `/bingo help` ou `/b` : Affiche l'aide
*   `/team menu` : Ouvre un GUI cliquable pour rejoindre son équipe (Rouge, Bleu, Jaune, Vert).
*   `/tj <couleur>` : Raccourci magique pour vite rejoindre la `<couleur>`.
*   `/team leave` : Quitter son équipe.

### 👑 Pour les Administrateurs (Nécessite `bingo.admin`)
*   **Logique de jeu :**
    *   `/party start` : Lance l'épreuve ! Téléportation coordonnée des joueurs sur la carte.
    *   `/party pause` : Gèle le temps du jeu en cas de dispute.
    *   `/pregen <rayon>` : Lance un énorme chargement de chunks fluide avant un stream.
*   **Gestion Grille :**
    *   `/bs <taille>` (Alias `/bingo size`) : Modifie l'envergure du bingo (ex: `/bs 5` ou `/bs 7`).
    *   `/bingo generate` : Frappe magique ! Crée le Datapack Minecraft natif à la volée correspondant aux objectifs piochés.
    *   `/bingo time <min>` : Modifie la minuterie de la manche.
*   **Gestion Équipes :**
    *   `/team random <nombre>` : Répartit les indécis aléatoirement.
    *   `/team lock` : Empêche toute transition inter-équipe.
    *   `/team setsize <max>` : Fixe la capacité maximale pour équilibrer.
    *   `/team set <joueur> <couleur>` : Kidnappe un joueur pour le transférer.

---

## 📝 À noter / Développeurs
Conçu avec amour sur la robustesse de Spigot. Toute réutilisation publique modifiée est tolérée mais _strictement interdite_ à la revente commerciale. 

> *Antigravity was here.* 🌌
