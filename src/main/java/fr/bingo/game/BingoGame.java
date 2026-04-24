package fr.bingo.game;

import java.util.List;
import java.util.UUID;
import fr.bingo.BingoPlugin;
import fr.bingo.team.TeamManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class BingoGame {

    private GameState state;
    private Location waitingPlatformLocation;
    private final BingoGrid grid;
    private long startTime;
    private long pausedElapsed; // temps écoulé au moment de la pause
    private Difficulty difficulty = Difficulty.HARD;
    private BingoMode mode = BingoMode.ITEMS;
    private int gameDurationMinutes = 120; // durée par défaut 2h

    // ── PVP ──
    private boolean pvpEnabled = false;
    private boolean pvpDisabled = false; // true = PVP complètement désactivé toute la partie
    private int pvpTimerMinutes = 20;
    private BukkitTask pvpTask;
    private BukkitTask pvpWarningTask;

    private EndMode endMode = EndMode.ALL_TEAMS;

    private final java.util.Set<String> disabledPoolItems = new java.util.HashSet<>();
    private final java.util.Set<UUID> startingPlayers = new java.util.HashSet<>();
    private BukkitTask gameTimerTask;

    public BingoGame() {
        this.state = GameState.WAITING;
        this.grid = new BingoGrid();
        Bukkit.getScheduler().runTask(BingoPlugin.getInstance(), this::setupWaitingPlatform);
    }

    // ── Getters/Setters ──

    public BingoGrid getGrid() { return grid; }
    public GameState getState() { return state; }
    public void setState(GameState state) {
        if (this.state == GameState.PLAYING && (state == GameState.PAUSED || state == GameState.FINISHED)) {
            this.pausedElapsed = getElapsedSeconds();
        }
        this.state = state;
    }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty d) { this.difficulty = d; }
    public BingoMode getMode() { return mode; }
    public void setMode(BingoMode m) { this.mode = m; }
    public long getStartTime() { return startTime; }
    public int getGameDurationMinutes() { return gameDurationMinutes; }
    public void setGameDurationMinutes(int minutes) { this.gameDurationMinutes = minutes; }

    // PVP
    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }
    public boolean isPvpDisabled() { return pvpDisabled; }
    public void setPvpDisabled(boolean pvpDisabled) { this.pvpDisabled = pvpDisabled; }
    public int getPvpTimerMinutes() { return pvpTimerMinutes; }
    public void setPvpTimerMinutes(int minutes) { this.pvpTimerMinutes = Math.max(0, Math.min(minutes, 120)); }

    // End Mode
    public EndMode getEndMode() { return endMode; }
    public void setEndMode(EndMode endMode) { this.endMode = endMode; }

    public java.util.Set<String> getDisabledPoolItems() { return disabledPoolItems; }

    public long getElapsedSeconds() {
        if (state == GameState.WAITING) return 0;
        if (state == GameState.PAUSED || state == GameState.FINISHED) return pausedElapsed;
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    // ── Plateforme d'attente ──

    private void setupWaitingPlatform() {
        World world = Bukkit.getWorlds().get(0);
        int y = 250;
        this.waitingPlatformLocation = new Location(world, 0.5, y + 1, 0.5);
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                world.getBlockAt(x, y, z).setType(Material.GLASS);
                if (x == -10 || x == 10 || z == -10 || z == 10) {
                    for (int wallY = 1; wallY <= 3; wallY++) {
                        world.getBlockAt(x, y + wallY, z).setType(Material.BARRIER);
                    }
                }
            }
        }

        // Jour éternel à midi + pas de pluie pendant le hub
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(6000); // Midi, soleil au zénith
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setStorm(false);
        world.setThundering(false);
    }

    public void teleportToWaitingArea(Player player) {
        if (state == GameState.WAITING) {
            player.teleport(waitingPlatformLocation);
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    // ── Démarrage ──

    public void startParty() {
        if (this.state == GameState.PLAYING) return;

        Bukkit.broadcastMessage("§6§l►► La partie commence dans 5 secondes ! ◄◄");

        for (int i = 5; i >= 1; i--) {
            final int count = i;
            Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                String color = count <= 2 ? "§c§l" : count <= 3 ? "§e§l" : "§a§l";
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(color + count, "§7Préparez-vous...", 0, 25, 5);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                }
            }, (5 - count) * 20L);
        }

        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), this::finalizeStart, 5 * 20L);
    }
    
    private void finalizeStart() {
        this.state = GameState.PLAYING;
        this.startTime = System.currentTimeMillis();
        this.pvpEnabled = false;

        TeamManager tm = BingoPlugin.getInstance().getTeamManager();

        // ── FFA : auto-assigner chaque joueur à sa propre équipe ──
        if (tm.isSoloMode()) {
            setupFfaTeams(tm);
        }

        tm.setTeamsLocked(true);

        // Enregistrer les joueurs présents au lancement
        startingPlayers.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            startingPlayers.add(p.getUniqueId());
        }

        World world = Bukkit.getWorlds().get(0);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setStorm(false);
        world.setThundering(false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

        // Météo fixée pour toutes les dimensions
        for (World w : Bukkit.getWorlds()) {
            w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            w.setStorm(false);
            w.setThundering(false);
        }

        // Désactiver la Locator Bar
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule locator_bar false");

        // Lancement des scénarios
        BingoPlugin.getInstance().getScenarioManager().onGameStart();

        // Conditionner la logique Bingo
        boolean isBingoMode = BingoPlugin.getInstance().getScenarioManager()
                .isScenarioEnabled(fr.bingo.scenario.BingoScenario.class);

        if (isBingoMode) {
            revokeAllBingoAdvancements();
        }

        StarterInventoryManager starterInv = BingoPlugin.getInstance().getStarterInventoryManager();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("§a§lGO !", "§eBonne chance !", 0, 30, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.5f);
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();

            // Donner l'inventaire de départ personnalisé
            starterInv.giveToPlayer(p);

            p.setInvulnerable(true);

            // Débloquer tous les crafts du livre de recettes
            fr.bingo.listeners.BingoListener.discoverAllRecipes(p);
        }

        destroyWaitingPlatform();

        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setInvulnerable(false);
            }
        }, 200L);

        // Scanner d'inventaire (seulement si Bingo actif)
        if (isBingoMode) {
            startInventoryScanner();
        }

        // ── PVP Timer ──
        schedulePvpTimer();

        // ── Game Duration Timer ──
        scheduleGameDurationTimer();

        // ── Bordure ──
        BingoPlugin.getInstance().getBorderManager().startBorder();

        Bukkit.broadcastMessage("§6§l►► LA PARTIE DÉMARRE ! ◄◄ §r§eQue le meilleur gagne !");

        // Annoncer le mode
        if (tm.isSoloMode()) {
            Bukkit.broadcastMessage("§d§l🎮 Mode : §f§lFFA §7— Chacun pour soi !");
        } else {
            Bukkit.broadcastMessage("§b§l🎮 Mode : §f§lÉQUIPES §7— " + tm.getActiveTeamCount() + " équipes");
        }

        // Annoncer le mode PVP
        if (pvpDisabled) {
            Bukkit.broadcastMessage("§7§l⚔ PVP : §c§lDÉSACTIVÉ §7pour toute la partie");
        } else if (pvpTimerMinutes == 0) {
            pvpEnabled = true;
            Bukkit.broadcastMessage("§c§l⚔ PVP ACTIVÉ §7dès le début !");
        } else {
            Bukkit.broadcastMessage("§7§l⚔ PVP : §eActivation dans §b§l" + pvpTimerMinutes + " minutes");
        }
    }

    private void schedulePvpTimer() {
        // Annuler les tâches précédentes si existantes
        if (pvpTask != null) pvpTask.cancel();
        if (pvpWarningTask != null) pvpWarningTask.cancel();

        if (pvpDisabled || pvpTimerMinutes == 0) return;

        final int totalSeconds = pvpTimerMinutes * 60;

        // Tâche 6 : Timer PVP avec messages simples dans le chat aux paliers clés
        pvpTask = new org.bukkit.scheduler.BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (state != GameState.PLAYING) {
                    this.cancel();
                    return;
                }

                int remaining = totalSeconds - elapsed;

                // Annonces aux paliers clés (messages simples dans le chat)
                if (remaining == 20 * 60) {
                    Bukkit.broadcastMessage("§7⚔ PVP dans §f20 minutes");
                } else if (remaining == 10 * 60) {
                    Bukkit.broadcastMessage("§7⚔ PVP dans §f10 minutes");
                } else if (remaining == 5 * 60) {
                    Bukkit.broadcastMessage("§7⚔ PVP dans §f5 minutes");
                } else if (remaining == 60) {
                    Bukkit.broadcastMessage("§e⚔ PVP dans §f1 minute");
                } else if (remaining == 10) {
                    Bukkit.broadcastMessage("§c⚔ PVP dans §f10 secondes");
                } else if (remaining == 5) {
                    Bukkit.broadcastMessage("§c⚔ PVP dans §f5s");
                } else if (remaining == 4) {
                    Bukkit.broadcastMessage("§c⚔ PVP dans §f4s");
                } else if (remaining == 3) {
                    Bukkit.broadcastMessage("§c⚔ PVP dans §f3s");
                } else if (remaining == 2) {
                    Bukkit.broadcastMessage("§c⚔ PVP dans §f2s");
                } else if (remaining == 1) {
                    Bukkit.broadcastMessage("§c⚔ PVP dans §f1s");
                } else if (remaining <= 0) {
                    pvpEnabled = true;
                    Bukkit.broadcastMessage("§c⚔ Le PVP est désormais activé !");
                    for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
                    }
                    this.cancel();
                    return;
                }

                elapsed++;
            }
        }.runTaskTimer(BingoPlugin.getInstance(), 0L, 20L);
    }

    private void scheduleGameDurationTimer() {
        if (gameTimerTask != null) gameTimerTask.cancel();
        
        gameTimerTask = Bukkit.getScheduler().runTaskTimer(BingoPlugin.getInstance(), () -> {
            if (state != GameState.PLAYING) return;
            
            long elapsedMin = getElapsedSeconds() / 60;
            if (elapsedMin >= gameDurationMinutes) {
                forceGameEnd("Temps écoulé !");
            }
        }, 20 * 60L, 20 * 60L); // Vérifier toutes les minutes
    }

    public void forceGameEnd(String reason) {
        if (state == GameState.FINISHED) return;
        
        this.state = GameState.FINISHED;
        
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("§c§l⌛ FIN DE LA PARTIE : " + reason.toUpperCase());
        Bukkit.broadcastMessage("");
        
        // Terminer proprement via le listener
        BingoPlugin.getInstance().getBingoListener().triggerGameEnd();
    }

    public java.util.Set<UUID> getStartingPlayers() {
        return startingPlayers;
    }

    /**
     * En mode FFA : chaque joueur non-spectateur est assigné à sa propre équipe.
     * 1 joueur = 1 équipe unique → progression individuelle.
     */
    private void setupFfaTeams(fr.bingo.team.TeamManager tm) {
        // Vider toutes les équipes
        for (fr.bingo.team.BingoTeam team : tm.getTeams()) {
            team.getPlayers().clear();
        }
        tm.getSpectatorTeam().getPlayers().clear();

        // Collecter les joueurs en ligne
        java.util.List<Player> players = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());
        int teamIndex = 0;

        for (Player p : players) {
            if (teamIndex < tm.getTeams().size()) {
                fr.bingo.team.BingoTeam soloTeam = tm.getTeams().get(teamIndex);
                soloTeam.addPlayer(p);
                p.sendMessage(soloTeam.getChatColor() + "§l[FFA] §rVous jouez en solo !");
                teamIndex++;
            } else {
                // Plus de slots → spectateur
                tm.getSpectatorTeam().addPlayer(p);
                p.sendMessage("§7Trop de joueurs pour le FFA ! Vous êtes spectateur.");
            }
        }

        // Adapter le nombre d'équipes actives au nombre de joueurs
        tm.setActiveTeamCount(Math.max(2, teamIndex));

        Bukkit.getLogger().info("[Bingo] FFA : " + teamIndex + " joueurs assignés à " + teamIndex + " équipes individuelles.");
    }

    /**
     * Scanner d'inventaire périodique (toutes les 2 sec).
     * Détecte les items obtenus via fourneau, échange, coffre, etc.
     * IMPORTANT : appelle checkTeamCompletion pour déclencher la fin de partie.
     */
    private void startInventoryScanner() {
        Bukkit.getScheduler().runTaskTimer(BingoPlugin.getInstance(), () -> {
            if (state != GameState.PLAYING) return;
            if (grid == null || grid.getObjectives().isEmpty()) return;

            fr.bingo.team.TeamManager tm = BingoPlugin.getInstance().getTeamManager();
            java.util.Set<fr.bingo.team.BingoTeam> teamsToCheck = new java.util.HashSet<>();
            java.util.List<BingoObjective> objectives = grid.getObjectives();

            for (Player p : Bukkit.getOnlinePlayers()) {
                fr.bingo.team.BingoTeam team = tm.getPlayerTeam(p);
                if (team == null || team.getName().equals("Spectateur")) continue;

                for (org.bukkit.inventory.ItemStack item : p.getInventory().getContents()) {
                    if (item == null || item.getType() == Material.AIR) continue;

                    String objId = getIdentifier(item);
                    if (objId == null) continue;

                    for (int i = 0; i < objectives.size(); i++) {
                        BingoObjective obj = objectives.get(i);
                        if (!obj.isAchievement() && obj.getId().equalsIgnoreCase(objId)) {
                            if (!team.hasUnlocked(objId)) {
                                team.unlockObjective(objId, grid.getSize());

                                // Grant advancement per-team
                                for (java.util.UUID uuid : team.getPlayers()) {
                                    Player tp = Bukkit.getPlayer(uuid);
                                    if (tp != null) {
                                        grantBingoAdvancement(tp, i);
                                        tp.playSound(tp.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
                                    }
                                }

                                String displayName = getDisplayName(objId);
                                // FFA = pseudo blanc, Team = couleur d'équipe
                                if (tm.isSoloMode()) {
                                    Bukkit.broadcastMessage("§8[§6Bingo§8] §f" + p.getName() + " §aa trouvé §e" + displayName + " §a!");
                                } else {
                                    Bukkit.broadcastMessage("§8[§6Bingo§8] " + team.getChatColor() + "L'équipe " + team.getName() + " §aa trouvé §e" + displayName + " §a!");
                                }

                                int seconds = (int) getElapsedSeconds();
                                BingoPlugin.getInstance().getDatabaseManager().recordStat(p.getName(), seconds, objId);
                                teamsToCheck.add(team);
                            }
                            break;
                        }
                    }
                }
            }

            // Vérifier la complétion pour chaque équipe qui a trouvé un nouvel item
            for (fr.bingo.team.BingoTeam team : teamsToCheck) {
                checkScannerTeamCompletion(team);
            }
        }, 40L, 40L);
    }

    /**
     * Vérifie si une équipe a terminé tous les objectifs (appelé par le scanner).
     */
    private void checkScannerTeamCompletion(fr.bingo.team.BingoTeam team) {
        int totalObjectives = grid.getObjectives().size();
        int teamFound = team.getUnlockedObjectives().size();

        if (teamFound >= totalObjectives && !team.isFinished()) {
            team.setFinished(true);
            TeamManager tm = BingoPlugin.getInstance().getTeamManager();

            long elapsed = getElapsedSeconds();
            int min = (int) (elapsed / 60);
            int sec = (int) (elapsed % 60);
            String timeStr = String.format("%02d:%02d", min, sec);

            String playerName = "???";
            if (!team.getPlayers().isEmpty()) {
                org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(team.getPlayers().get(0));
                if (op.getName() != null) playerName = op.getName();
            }

            String teamLabel = tm.isSoloMode()
                    ? "§f§l★ " + playerName + " a terminé le Bingo ! ★"
                    : team.getChatColor() + "§l★ L'équipe " + team.getName() + " a terminé le Bingo ! ★";

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§8§m                                                §r");
            Bukkit.broadcastMessage("  " + teamLabel);
            Bukkit.broadcastMessage("  §7Temps : §e§l" + timeStr);
            Bukkit.broadcastMessage("§8§m                                                §r");
            Bukkit.broadcastMessage("");

            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.setGameMode(GameMode.SPECTATOR);
                    p.getInventory().clear();
                    p.sendTitle(tm.isSoloMode() ? "§f§lBINGO !" : team.getChatColor() + "§lBINGO !", "§7Temps : §e" + timeStr, 10, 60, 20);
                }
            }
        }
    }

    /**
     * Synchronise les advancements d'une équipe pour un joueur qui vient de la rejoindre.
     */
    public void syncTeamAdvancements(Player player, fr.bingo.team.BingoTeam team) {
        if (state != GameState.PLAYING) return;
        List<BingoObjective> objectives = grid.getObjectives();
        for (int i = 0; i < objectives.size(); i++) {
            if (team.hasUnlocked(objectives.get(i).getId())) {
                grantBingoAdvancement(player, i);
            }
        }
    }

    /**
     * Révoque tous les advancements bingoclassique pour tous les joueurs.
     * Appelé au start pour un tracking propre per-team.
     */
    public void revokeAllBingoAdvancements() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            java.util.Iterator<org.bukkit.advancement.Advancement> it = Bukkit.advancementIterator();
            while (it.hasNext()) {
                org.bukkit.advancement.Advancement adv = it.next();
                if (adv.getKey().getNamespace().equals("bingoclassique")) {
                    org.bukkit.advancement.AdvancementProgress progress = p.getAdvancementProgress(adv);
                    for (String criteria : progress.getAwardedCriteria()) {
                        progress.revokeCriteria(criteria);
                    }
                }
            }
        }
    }

    /**
     * Accorde un advancement bingo à un joueur spécifique (per-team tracking).
     */
    public void grantBingoAdvancement(Player player, int index) {
        String advId = DatapackManager.getAdvancementIdFromIndex(index, grid.getSize());
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("bingoclassique", advId);
        org.bukkit.advancement.Advancement adv = Bukkit.getAdvancement(key);
        if (adv != null) {
            org.bukkit.advancement.AdvancementProgress progress = player.getAdvancementProgress(adv);
            for (String criteria : adv.getCriteria()) {
                progress.awardCriteria(criteria);
            }
        }
    }

    private void destroyWaitingPlatform() {
        World world = waitingPlatformLocation.getWorld();
        int y = 250;
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                world.getBlockAt(x, y, z).setType(Material.AIR);
                for (int wallY = 1; wallY <= 3; wallY++)
                    world.getBlockAt(x, y + wallY, z).setType(Material.AIR);
            }
        }
    }

    // ── Pause / Resume ──

    public void pauseParty() {
        if (this.state != GameState.PLAYING) return;
        this.pausedElapsed = getElapsedSeconds();
        this.state = GameState.PAUSED;

        // Freeze tous les joueurs
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 250, false, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, Integer.MAX_VALUE, 255, false, false, false));
            p.setInvulnerable(true);
            p.sendTitle("§c§lPAUSE", "§7La partie est en pause", 0, 60, 10);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 0.5f);
        }
        Bukkit.broadcastMessage("§c§l►► PARTIE EN PAUSE ◄◄");
    }

    public void resumeParty() {
        if (this.state != GameState.PAUSED) return;
        // Recalculer le startTime pour conserver le temps écoulé
        this.startTime = System.currentTimeMillis() - (pausedElapsed * 1000);
        this.state = GameState.PLAYING;

        // Unfreeze tous les joueurs
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.SLOWNESS);
            p.removePotionEffect(PotionEffectType.JUMP_BOOST);
            p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
            p.setInvulnerable(false);
            p.sendTitle("§a§lREPRISE !", "§eC'est reparti !", 0, 30, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
        Bukkit.broadcastMessage("§a§l►► REPRISE DE LA PARTIE ! ◄◄");
    }

    // ── Reset ──

    public void resetGame() {
        this.state = GameState.WAITING;
        this.pvpEnabled = false;

        // Annuler les tâches PVP
        if (pvpTask != null) { pvpTask.cancel(); pvpTask = null; }
        if (pvpWarningTask != null) { pvpWarningTask.cancel(); pvpWarningTask = null; }

        BingoPlugin.getInstance().getTeamManager().setTeamsLocked(false);

        // Reset toutes les équipes
        for (fr.bingo.team.BingoTeam team : BingoPlugin.getInstance().getTeamManager().getTeams()) {
            team.getUnlockedObjectives().clear();
            team.setFinished(false);
            team.setScore(0);
        }

        setupWaitingPlatform();

        for (Player p : Bukkit.getOnlinePlayers()) {
            // Retirer les effets de pause si présents
            p.removePotionEffect(PotionEffectType.SLOWNESS);
            p.removePotionEffect(PotionEffectType.JUMP_BOOST);
            p.removePotionEffect(PotionEffectType.MINING_FATIGUE);

            p.setGameMode(GameMode.ADVENTURE);
            p.getInventory().clear();
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(20f);
            p.setInvulnerable(false);
            p.setExp(0f);
            p.setLevel(0);
            p.setTotalExperience(0);
            teleportToWaitingArea(p);
            BingoPlugin.getInstance().getTeamManager().giveTeamBanners(p);

            // Donner le compas admin
            if (p.hasPermission("bingo.admin")) {
                giveAdminCompass(p);
            }
        }

        World world = Bukkit.getWorlds().get(0);
        // Garder jour éternel + pas de pluie pendant le hub
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(6000);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setStorm(false);
        world.setThundering(false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, true);

        Bukkit.broadcastMessage("§6§l►► La partie a été réinitialisée ! ◄◄");
    }

    // ── Admin Compass ──

    public static void giveAdminCompass(Player player) {
        org.bukkit.inventory.ItemStack compass = new org.bukkit.inventory.ItemStack(Material.COMPASS);
        org.bukkit.inventory.meta.ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName("§6§l⚙ Configuration Bingo");
        meta.setLore(java.util.List.of("§7Clic droit pour configurer la partie"));
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "admin_compass");
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        compass.setItemMeta(meta);
        player.getInventory().setItem(8, compass); // Slot 9 (dernier)
    }

    /**
     * Prépare la réinitialisation complète du monde (Seed + Suppression dossiers).
     * Nécessite le script run_bingo.bat pour la partie suppression physique.
     * @param admin le joueur admin qui a déclenché l'action, ou null si automatique.
     */
    public void prepareWorldReset(Player admin) {
        // 1. Générer une nouvelle Seed
        long newSeed = new java.util.Random().nextLong();

        // 2. Modifier server.properties
        try {
            java.io.File propFile = new java.io.File("server.properties");
            if (propFile.exists()) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream in = new java.io.FileInputStream(propFile)) {
                    props.load(in);
                }
                props.setProperty("level-seed", String.valueOf(newSeed));
                try (java.io.FileOutputStream out = new java.io.FileOutputStream(propFile)) {
                    props.store(out, "Modified by Bingo Plugin for World Reset");
                }
            }
        } catch (java.io.IOException e) {
            String errMsg = "§c[Erreur] Impossible de modifier server.properties : " + e.getMessage();
            if (admin != null) {
                admin.sendMessage(errMsg);
            } else {
                Bukkit.getLogger().warning(errMsg);
            }
            return;
        }

        // 3. Créer le flag reset_map.txt
        try {
            new java.io.File("reset_map.txt").createNewFile();
        } catch (java.io.IOException e) {
            String errMsg = "§c[Erreur] Impossible de créer le flag reset_map.txt.";
            if (admin != null) {
                admin.sendMessage(errMsg);
            } else {
                Bukkit.getLogger().warning(errMsg);
            }
            return;
        }

        // 4. Countdown et Shutdown
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 10;

            @Override
            public void run() {
                if (count <= 0) {
                    Bukkit.broadcastMessage("§c§lREDÉMARRAGE DU SERVEUR !");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.kickPlayer("§c§lRéinitialisation du monde...\n\n§7Le serveur revient dans quelques instants sur une nouvelle map !");
                    }
                    Bukkit.shutdown();
                    this.cancel();
                    return;
                }

                if (count <= 5 || count == 10) {
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage("§c§l  ⚠ RÉINITIALISATION DU MONDE DANS §e" + count + " §c§lSECONDES ! ⚠");
                    Bukkit.broadcastMessage("§7  (Dossiers world, world_nether et world_the_end seront supprimés)");
                    Bukkit.broadcastMessage("");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                        p.sendTitle("§c§l⚠ RESET WORLD", "§eDans " + count + " secondes...", 0, 25, 5);
                    }
                }
                count--;
            }
        }.runTaskTimer(BingoPlugin.getInstance(), 0L, 20L);
    }

    private String getIdentifier(org.bukkit.inventory.ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        if (item.getType() == Material.POTION) {
            if (item.hasItemMeta() && item.getItemMeta() instanceof org.bukkit.inventory.meta.PotionMeta meta) {
                org.bukkit.potion.PotionType type = meta.getBasePotionType();
                String name = type.name();
                
                if (name.contains("SWIFTNESS")) return getPotionId("SPEED", name);
                if (name.contains("STRENGTH")) return getPotionId("STRENGTH", name);
                if (name.contains("LEAPING")) return getPotionId("JUMP", name);
                if (name.contains("FIRE_RES")) return getPotionId("FIRE_RES", name);
                if (name.contains("WATER_BREATH")) return getPotionId("WATER_BREATH", name);
                if (name.contains("REGENERATION")) return getPotionId("REGEN", name);
                if (name.contains("INVISIBILITY")) return getPotionId("INVIS", name);
                if (name.contains("NIGHT_VISION")) return getPotionId("NIGHT_VIS", name);
            }
            return "POTION";
        }
        return item.getType().name();
    }

    private String getPotionId(String base, String typeName) {
        String suffix = "_1";
        if (typeName.startsWith("STRONG_")) suffix = "_2";
        else if (typeName.startsWith("LONG_")) suffix = "_EXT";
        return "POTION_" + base + suffix;
    }

    private String getDisplayName(String id) {
        if (id.startsWith("POTION_")) {
            String base = "Potion de ";
            if (id.contains("SPEED")) base += "Vitesse";
            else if (id.contains("STRENGTH")) base += "Force";
            else if (id.contains("JUMP")) base += "Saut";
            else if (id.contains("FIRE_RES")) base += "Resistance au Feu";
            else if (id.contains("WATER_BREATH")) base += "Respiration Aquatique";
            else if (id.contains("REGEN")) base += "Regeneration";
            else if (id.contains("INVIS")) base += "Invisibilite";
            else if (id.contains("NIGHT_VIS")) base += "Vision Nocturne";
            
            if (id.endsWith("_2")) base += " II";
            if (id.endsWith("_EXT")) base += " (Allongee)";
            return base;
        }
        return id.replace("_", " ").toLowerCase();
    }
}
