# 🤖 CLAUDE.md — État du projet Bingo

> Fichier de suivi du projet. Dernière mise à jour : 11/04/2026

---

## ✅ Fonctionnalités implémentées

### Core
- [x] Système de grille Bingo (3×3 / 5×5 / 7×7)
- [x] 3 modes de jeu : Items, Achievements, Mixte
- [x] 4 niveaux de difficulté (Facile → Extrême)
- [x] Durée configurable (1h / 1h30 / 2h)
- [x] Timer de partie avec chrono en temps réel
- [x] Détection automatique des items (pickup + craft)
- [x] Détection des achievements Minecraft vanilla
- [x] Système de points (item = 1pt, ligne/colonne = 3pts)
- [x] Datapack dynamique pour afficher la grille en tant qu'advancements in-game

### Équipes
- [x] 15 équipes disponibles (toutes les couleurs de bannières)
- [x] Mode Solo / FFA (chacun pour soi)
- [x] Mode Spectateur (bannière blanche, accessible à tous)
- [x] Sélection via GUI (bannière dans la hotbar → clic droit)
- [x] La bannière hotbar change de couleur selon l'équipe
- [x] Bannière indropable + retirée au start
- [x] Équipes aléatoires (`/team random`)
- [x] Verrouillage des équipes
- [x] GUI admin sous-menu équipes avec boutons béton rouge/vert

### PVP
- [x] 3 modes : Désactivé / Immédiat / Timer
- [x] Timer ajustable par ±1 (clic gauche) et ±5 (clic droit)
- [x] Annonce chat + title à l'activation
- [x] GUI admin sous-menu PVP avec boutons béton rouge/vert

### Interface Admin (GUI)
- [x] Compas dans la hotbar → clic droit → GUI 45 slots
- [x] Taille / Difficulté / Mode / Durée sur rangée 2
- [x] Équipes / PVP / Mode fin / Lock sur rangée 3
- [x] Générer / Start / Reset sur rangée 4
- [x] Commande `/c` pour récupérer le compas
- [x] Attributs d'épée cachés (`HIDE_ATTRIBUTES`)

### Scoreboard
- [x] Chrono temps réel
- [x] Progression personnelle (items trouvés / total)
- [x] Points du joueur/équipe
- [x] Top 5 classement dynamique
- [x] En FFA : pseudos des joueurs
- [x] En équipe : noms des équipes
- [x] Espace entre chrono et progression

### Fin de partie
- [x] Mode "Toutes les équipes finissent"
- [x] Mode "Dernière debout"
- [x] Cleanup spectateur (inventaire, XP, advancements)
- [x] Classement final avec médailles
- [x] Feux d'artifice à la fin

### Environnement
- [x] Jour éternel à midi (soleil au zénith)
- [x] Pas de pluie (hub + partie)
- [x] Plateforme d'attente en verre à Y=250

### Chat
- [x] Chat d'équipe par défaut
- [x] Chat global avec `!` prefix
- [x] Messages privés bloqués en partie
- [x] Spectateurs voient le chat d'équipe (spy)

### Base de données
- [x] SQLite (par défaut)
- [x] MySQL (optionnel)
- [x] Stats par joueur (objectif, temps)

---

## 🐛 Bugs connus / Pièges à éviter

### ⚠️ Têtes custom (SkullMeta + PlayerProfile)
Les têtes custom avec URL de texture (`textures.minecraft.net`) ne chargent **PAS de manière fiable** sur Paper 1.21.
**Solution adoptée** : utiliser des blocs de béton colorés (`RED_CONCRETE` / `LIME_CONCRETE`) au lieu de têtes custom pour les boutons +/-.

### ⚠️ Mode Achievements — Advancements vanilla
En mode **Achievements** ou **Mixte**, il ne faut **PAS** désactiver les advancements vanilla via le datapack (`disableVanillaAdvancements`), sinon les achievements ne peuvent jamais être complétés par les joueurs.
**Solution** : `disableVanillaAdvancements()` n'est appelé qu'en mode **Items** pur.

### ⚠️ Scoreboard — Lignes dupliquées
Bukkit n'accepte pas deux lignes identiques dans un scoreboard sidebar. Si deux lignes sont identiques (ex: deux espaces vides), il faut les rendre uniques avec des `§r` invisibles.

### ⚠️ Bannière dans l'inventaire
La bannière de sélection d'équipe est dans le slot 4 (centre hotbar). Il faut s'assurer de :
- Ne pas la donner si la partie est déjà en cours
- La retirer au `startParty()` (via `player.getInventory().clear()`)
- La remettre au `onPlayerJoin()` si état = WAITING

### ⚠️ Bannière blanche = Spectateur
La bannière blanche (`WHITE_BANNER`) est réservée au mode spectateur. Si on ajoute des équipes, ne **jamais** utiliser `WHITE_BANNER` comme couleur d'équipe.

### ⚠️ `setActiveTeamCount` — Bornes
Min = 2, Max = `teams.size()` (actuellement 15). Ne pas descendre en dessous de 2 sinon la logique de fin de partie casse.

### ⚠️ Easter egg dans TeamManager
Le constructeur de `TeamManager` contient un easter egg encodé en bytes. Ne pas supprimer, c'est intentionnel.

---

## 📝 À faire / Améliorations possibles

- [ ] Système de vote pour les paramètres de partie
- [ ] Historique des parties (base de données)
- [ ] Commande `/bingo stats` pour voir ses stats
- [ ] Spectateur peut choisir quel joueur suivre
- [ ] Support multi-monde (world per game)
- [ ] Config YAML pour les objectifs custom
- [ ] Son / animation quand une équipe complète une ligne
- [ ] Prévisualisation de la grille dans le GUI admin avant de lancer

---

## 📁 Architecture du projet

```
src/main/java/fr/bingo/
├── BingoPlugin.java          — Point d'entrée du plugin
├── commands/
│   ├── BingoCommand.java     — Commande /bg
│   ├── CompassCommand.java   — Commande /c
│   └── TeamCommand.java      — Commande /team
├── game/
│   ├── BingoGame.java        — Logique de jeu principale
│   ├── BingoGrid.java        — Grille d'objectifs
│   ├── BingoMode.java        — Enum modes (Items/Achievements/Mixed)
│   ├── BingoObjective.java   — Un objectif (item ou achievement)
│   ├── BingoObjectivePool.java — Pool de tous les objectifs
│   ├── DatapackManager.java  — Génération du datapack d'advancements
│   ├── Difficulty.java       — Enum difficultés
│   ├── EndMode.java          — Enum modes de fin
│   ├── GameState.java        — Enum états de jeu
│   └── ScoreboardManager.java — Scoreboard sidebar dynamique
├── gui/
│   ├── AdminConfigGUI.java   — GUI admin principal (45 slots)
│   ├── BingoGridGUI.java     — GUI de la grille (/bg)
│   ├── PvpConfigGUI.java     — Sous-menu config PVP
│   ├── TeamConfigGUI.java    — Sous-menu config équipes
│   └── TeamSelectorGUI.java  — GUI sélection d'équipe (joueurs)
├── listeners/
│   └── BingoListener.java    — Tous les events
├── team/
│   ├── BingoTeam.java        — Classe équipe
│   └── TeamManager.java      — Gestion des équipes
└── database/
    └── DatabaseManager.java  — Gestion SQLite/MySQL
```

---

*Généré automatiquement par l'assistant de développement*
