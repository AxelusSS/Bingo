# 🎯 Bingo & UHC — Plugin Minecraft

Plugin multijoueur pour serveurs **Paper 1.21+**. Mode de jeu principal basé sur un **UHC** avec un système de scénarios modulaires, dont le **Bingo** classique.

## ✨ Fonctionnalités

### 🎮 Modes de jeu
Le plugin est un **Core UHC** dans lequel le Bingo est un scénario activable parmi d'autres. Vous pouvez :
- Lancer un **UHC classique** (sans Bingo) avec PVP, bordure, scénarios
- Lancer un **Bingo** en activant le scénario Bingo dans le menu
- Lancer un **UHC Run** via le preset communautaire (CutClean, HasteyBoys, etc.)

### 👥 Système d'équipes
- **2 à 15 équipes** avec toutes les couleurs de bannières
- **Mode Solo / FFA** — Chacun pour soi
- **Sélection via GUI** — Bannière dans la hotbar → clic droit pour ouvrir le menu
- Équipes aléatoires via commande ou GUI admin
- **Mode Spectateur** accessible à tout moment

### ⚔ PVP Configurable
- **Désactivé** — Pas de PVP pendant la partie
- **Immédiat** — PVP dès le début
- **Timer** — PVP activé après X minutes (configurable)

### 🌐 Système de Bordure (UHC)
- **Taille initiale** configurable (1000 / 2000 / 3000 / 4000)
- **Taille finale** configurable (50 / 100 / 200 / 500)
- **Délai avant réduction** (30 / 45 / 60 / 90 min)
- **Durée de réduction** (15 / 30 / 45 / 60 min)
- Annonces dans le chat aux paliers (10min, 5min, 1min)

### 🏁 Modes de fin de partie
- **Toutes les équipes** — Attend que tout le monde ait fini
- **Dernière debout** — S'arrête quand il ne reste qu'une équipe
- **Premier à finir** — S'arrête dès qu'une équipe termine

### 🎒 Inventaire de Départ
- Configurable via le GUI admin (Slot "Inventaire de Départ")
- L'admin passe en créatif, fait son inventaire, puis tape `/finish`
- L'inventaire est sauvegardé et donné à chaque joueur au lancement

### 📜 Scénarios (22 scénarios)

| Scénario | Description |
|---|---|
| **Bingo** | Active le mode Bingo avec grille d'objectifs (clic-droit pour configurer) |
| **CutClean** | Minerais et nourriture cuits automatiquement |
| **Hastey Boys** | Outils craftés enchantés (Efficacité 3 + Solidité 1) |
| **Lite Gapple** | Pomme en or avec 4 lingots d'or en croix au lieu de 8 |
| **Triple Ores** | Les minerais droppent 3x plus |
| **Boost Loot** | Drops améliorés (cochon→cuir, mouton→biblio, gravier→silex, etc.) |
| **Mini Nether** | Génère un mini-nether dans l'overworld (voir ci-dessous) |
| **Cat Eyes** | Vision nocturne infinie |
| **No Food** | Faim désactivée |
| **Keep Inventory** | Conservation de l'inventaire à la mort |
| **Flower Power** | Les fleurs droppent des items aléatoires |
| **Friendly Craft** | Craft partagé entre coéquipiers |
| **Team Inventory** | Inventaire d'équipe partagé |
| **Timber** | Couper un arbre fait tomber tout le tronc |
| **Fast Smelting** | Cuisson instantanée dans les fours |
| **No Fall Damage** | Pas de dégâts de chute |
| **Fireless** | Pas de dégâts de feu |
| **Switch** | Échange de position entre joueurs |
| **Switch Inventory** | Échange d'inventaire périodique |
| **Shared Health** | Vie partagée en équipe |
| **Grave** | Tombe avec coffre à la mort |
| **Super Hero** | Pouvoirs de super-héros |

### 🔥 Mini Nether — Comment ça marche ?

Quand le scénario **Mini Nether** est activé :
1. Construisez un **cadre de portail du Nether classique** (obsidienne 4×5)
2. Utilisez un **briquet** (Flint & Steel) sur le cadre
3. Au lieu de s'allumer, le portail génère une **mini-salle souterraine** sous vos pieds !
4. La salle contient :
   - Des murs en **netherrack**
   - Du **soul sand** avec des **verrues du nether**
   - Un **spawner à Blaze** au centre
   - De la **glowstone** au plafond
   - De la **lave** décorative
   - Un accès par **échelle** depuis la surface

> ⚠️ Le vrai Nether n'est **pas** désactivé par défaut. Le Mini Nether offre simplement un raccourci pratique pour les ressources essentielles (blaze rods, nether wart).

### 💾 Presets Communautaires

| Preset | Description |
|---|---|
| FFA EASY 1H nopvp Item | Bingo solo facile |
| To2 Normal 1H30 nopvp | Bingo par 2, moyen |
| To4 Difficile 1H30 nopvp | Bingo par 4, difficile |
| To4 Extreme pvp | Bingo par 4 avec PVP |
| Roulette FFA 1H nopvp | Bingo roulette (1 objectif) |
| **UHC Run FFA** | UHC Run avec CutClean, HasteyBoys, LiteGapple, TripleOres, BoostLoot |

### 📊 Scoreboard dynamique
- Chrono en temps réel
- Progression personnelle + points
- **Top 5** des équipes (ou joueurs en FFA)

### 🎛 GUI Admin
Interface complète via le **compas** dans la hotbar :
- **Équipes** — Configuration des équipes
- **Bordure** — Taille initiale, finale, temps de réduction
- **Durée** — 1h, 1h30, 2h
- **PVP** — Timer, désactivé, immédiat
- **Scénarios** — 22 scénarios activables (clic-droit pour configurer ceux qui ont un sous-menu)
- **Presets** — Sauvegardes personnelles et communautaires
- **Inventaire de Départ** — Personnaliser l'inventaire initial
- **Mode de Fin** — Configurer la condition de victoire
- **Lancer / Reset / Réinitialiser la map**

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
| `/finish` | Sauvegarder l'inventaire de départ | `bingo.admin` |
| `/ff` | Voter pour abandonner | - |
| `/game` | Voir les infos de la partie | - |

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
*Plugin développé par HEL — V1.4.0*
