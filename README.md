# 🎯 Bingo Classique — Plugin Minecraft

Plugin de Bingo multijoueur pour serveurs **Paper 1.21+**. Les joueurs doivent collecter des items ou compléter des achievements Minecraft dans une grille Bingo, seuls ou en équipe.

## ✨ Fonctionnalités

### 🎮 Modes de jeu
- **Items** — Collecter des objets Minecraft
- **Achievements** — Compléter des achievements vanilla
- **Mixte** — Combinaison items + achievements

### 👥 Système d'équipes
- **2 à 15 équipes** avec toutes les couleurs de bannières
- **Mode Solo / FFA** — Chacun pour soi
- **Sélection via GUI** — Bannière blanche dans la hotbar → clic droit pour ouvrir le menu
- La bannière change de couleur selon l'équipe rejointe
- Bannière **indropable** et automatiquement retirée au début de la partie
- Équipes aléatoires via commande ou GUI admin
- **Mode Spectateur** accessible à tout moment (bannière blanche dans le GUI)

### ⚔ PVP Configurable
- **Désactivé** — Pas de PVP pendant la partie
- **Immédiat** — PVP dès le début
- **Timer** — PVP activé après X minutes (configurable par pas de 1 ou 5)
- Annonce dans le chat + title quand le PVP s'active

### 🏁 Modes de fin de partie
- **Toutes les équipes** — La partie continue jusqu'à ce que toutes les équipes aient fini ou que le timer expire
- **Dernière debout** — La partie s'arrête quand il ne reste qu'une équipe en jeu

### 📊 Scoreboard dynamique
- Chrono en temps réel
- Progression personnelle + points
- **Top 5** des équipes (ou joueurs en FFA)
- Affichage dynamique selon le mode (noms d'équipes ou pseudos)

### 🎛 GUI Admin
Interface complète via le **compas** dans la hotbar :
- Taille de grille (3×3, 5×5, 7×7)
- Difficulté (Facile, Normal, Difficile, Extrême)
- Mode (Items, Achievements, Mixte)
- Durée (1h, 1h30, 2h)
- Sous-menus : **Équipes** et **PVP**
- Verrouillage des équipes
- Génération / Lancement / Reset

### 🌤 Environnement
- **Jour éternel** à midi (soleil au zénith) pendant le hub ET la partie
- **Pas de pluie** — Météo désactivée en permanence
- **Livre de recettes débloqué** automatiquement dès le lancement de la partie
- Désactivation de la **Locator Bar** (barre de tracking 1.21) par défaut pour éviter l'anti-jeu

### ✨ Dernières Nouveautés (V1.1)
- **Scénarios façon Erisium :** 16 nouveaux scénarios (CatEyes, NoFood, SuperHero, Timber, CutClean, Switch, Grave, etc.) configurables via un grand GUI dynamique accessible par le compas admin !
- **Configuration de la Pool Bingo :** Un clic droit sur l'item "Générer la grille" (Nether Star) permet d'ouvrir un menu interactif et paginé pour désactiver individuellement les items/achievements que vous ne souhaitez pas voir apparaître.
- **Affichage des Potions :** Les potions à trouver s'affichent maintenant avec leur vraie couleur et leur effet dans l'inventaire Bingo (vision nocturne, force, etc.) pour une meilleure clarté visuelle.
- **Fix Reconnexion :** Les joueurs qui se déconnectent en pleine partie conservent leur inventaire, vie, faim, XP et position à la reconnexion sans être téléportés au spawn.
- **Fin de partie (500s) :** Un décompte discret dans l'ActionBar s'affiche à la fin, suivi d'un redémarrage automatique du serveur (nouveau monde généré).
- **Amélioration du mode FFA :** Lors de la complétion d'une ligne/colonne, c'est le pseudo du joueur qui s'affiche en blanc, et non plus "L'équipe".
- **Refonte des alertes PVP :** Les annonces agressives avec sons et "Titles" sont remplacées par des messages discrets dans le chat (20m, 10m, 5m, 1m, 10s... 1s).

## 📋 Commandes

| Commande | Description | Permission |
|---|---|---|
| `/team join <couleur>` | Rejoindre une équipe | - |
| `/team leave` | Quitter son équipe | - |
| `/team set <joueur> <couleur>` | Assigner un joueur | `bingo.admin` |
| `/team random <N>` | Équipes aléatoires | `bingo.admin` |
| `/team setsize <N>` | Taille max par équipe | `bingo.admin` |
| `/bg` | Voir la grille Bingo | - |
| `/c` | Récupérer le compas admin | `bingo.admin` |

## ⚙ Configuration

### config.yml
```yaml
database:
  type: "sqlite"  # ou mysql

game:
  default_game_time: 120  # minutes
  default_teams_count: 4
  announce_in_chat: true
  pregen_radius: 500

scoreboard:
  title: "§6§lBINGO CLASSIQUE"
```

## 🔧 Installation

1. Compiler avec `mvn clean package`
2. Copier le JAR dans le dossier `plugins/` du serveur Paper
3. Redémarrer le serveur
4. Configurer via le compas ou les commandes

## 📦 Dépendances
- **Paper 1.21+** (API)
- **SQLite** (inclus) ou **MySQL** (optionnel)
- **Maven** pour la compilation

## 🎨 Couleurs d'équipes disponibles
Rouge, Bleu, Vert, Jaune, Rose, Cyan, Orange, Violet, Lime, Ciel, Magenta, Marron, Noir, Gris, Argent

---
*Plugin développé par HEL*
