package fr.hel.game;

import java.util.List;
import java.util.UUID;
import fr.hel.HelPlugin;
import fr.hel.team.TeamManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class HelGame {

    private GameState state;
    private Location waitingPlatformLocation;
    private final HelGrid grid;
    private long startTime;
    private long pausedElapsed; // temps \u00E9coul\u00E9 au moment de la pause
    private Difficulty difficulty = Difficulty.HARD;
    private HelMode mode = HelMode.ITEMS;
    private int gameDurationMinutes = 120; // dur\u00E9e par d\u00E9faut 2h

    // \u2500\u2500 PVP \u2500\u2500
    private boolean pvpEnabled = false;
    private boolean pvpDisabled = false; // true = PVP compl\u00E8tement d\u00E9sactiv\u00E9 toute la partie
    private int pvpTimerMinutes = 20;
    private BukkitTask pvpTask;
    private BukkitTask pvpWarningTask;

    private EndMode endMode = EndMode.ALL_TEAMS;
    
    private JumpManager jumpManager = new JumpManager();
    private ArmorStand lobbyHologram;
    
    public JumpManager getJumpManager() { return jumpManager; }

    private final java.util.Set<String> disabledPoolItems = new java.util.HashSet<>();
    private final java.util.Set<UUID> startingPlayers = new java.util.HashSet<>();
    private BukkitTask gameTimerTask;
    private String activePresetName;

    private boolean borderEnabled = true;

    // \u2500\u2500 World Config \u2500\u2500
    private BiomeSize biomeSize = BiomeSize.MEDIUM;
    private long worldSeed = -1; // -1 = random
    private final java.util.Set<String> disabledBiomes = new java.util.HashSet<>();
    private GenerationType generationType = GenerationType.V_1_21_11;
    private int miniNetherDensity = 10;

    public HelGame() {
        this.state = GameState.WAITING;
        this.grid = new HelGrid();
        loadSettings();
    }

    public void loadSettings() {
        org.bukkit.configuration.file.FileConfiguration config = HelPlugin.getInstance().getConfig();
        if (config.contains("generation.type")) {
            this.generationType = GenerationType.valueOf(config.getString("generation.type"));
        }
        if (config.contains("generation.seed")) {
            this.worldSeed = config.getLong("generation.seed");
        }
        if (config.contains("generation.disabled_biomes")) {
            this.disabledBiomes.clear();
            this.disabledBiomes.addAll(config.getStringList("generation.disabled_biomes"));
        }
        if (config.contains("generation.mini_nether_density")) {
            this.miniNetherDensity = config.getInt("generation.mini_nether_density");
        }
    }

    public void saveSettings() {
        org.bukkit.configuration.file.FileConfiguration config = HelPlugin.getInstance().getConfig();
        config.set("generation.type", generationType.name());
        config.set("generation.seed", worldSeed);
        config.set("generation.disabled_biomes", new java.util.ArrayList<>(disabledBiomes));
        config.set("generation.mini_nether_density", miniNetherDensity);
        HelPlugin.getInstance().saveConfig();
    }

    // \u2500\u2500 Getters/Setters \u2500\u2500

    public HelGrid getGrid() { return grid; }
    public GameState getState() { return state; }
    public void setState(GameState state) {
        if (this.state == GameState.PLAYING && (state == GameState.PAUSED || state == GameState.FINISHED)) {
            this.pausedElapsed = getElapsedSeconds();
        }
        this.state = state;
    }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty d) { this.difficulty = d; }
    public HelMode getMode() { return mode; }
    public void setMode(HelMode m) { this.mode = m; }
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

    public String getActivePresetName() { return activePresetName; }
    public void setActivePresetName(String name) { this.activePresetName = name; }

    public boolean isBorderEnabled() { return borderEnabled; }
    public void setBorderEnabled(boolean borderEnabled) { this.borderEnabled = borderEnabled; }

    public long getElapsedSeconds() {
        if (state == GameState.WAITING) return 0;
        if (state == GameState.PAUSED || state == GameState.FINISHED) return pausedElapsed;
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    // \u2500\u2500 Plateforme d'attente \u2500\u2500

    public void setupWaitingPlatform() {
        if (Bukkit.getWorlds().isEmpty()) return;
        World world = Bukkit.getWorlds().get(0);
        int y = 250;
        this.waitingPlatformLocation = new Location(world, 0.5, y + 1, 0.5);
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                world.getBlockAt(x, y, z).setType(Material.GLASS);
                if (x == -16 || x == 16 || z == -16 || z == 16) {
                    for (int wallY = 1; wallY <= 4; wallY++) {
                        world.getBlockAt(x, y + wallY, z).setType(Material.BARRIER);
                    }
                }
            }
        }
        
        if (lobbyHologram != null) lobbyHologram.remove();
        lobbyHologram = (ArmorStand) world.spawnEntity(waitingPlatformLocation.clone().add(0, 1.5, 0), EntityType.ARMOR_STAND);
        lobbyHologram.setVisible(false);
        lobbyHologram.setMarker(true);
        lobbyHologram.setGravity(false);
        lobbyHologram.setCustomNameVisible(true);
        lobbyHologram.setCustomName("§e§lPour accéder au jump, faites /parkour");


        // Jour \u00E9ternel \u00E0 midi + pas de pluie pendant le hub
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(6000); // Midi, soleil au z\u00E9nith
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

    // \u2500\u2500 D\u00E9marrage \u2500\u2500

    public void startParty() {
        if (this.state == GameState.PLAYING) return;

        Bukkit.broadcastMessage("\u00A76\u00A7l\u25BA\u25BA La partie commence dans 5 secondes ! \u25C4\u25C4");

        for (int i = 5; i >= 1; i--) {
            final int count = i;
            Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
                String color = count <= 2 ? "\u00A7c\u00A7l" : count <= 3 ? "\u00A7e\u00A7l" : "\u00A7a\u00A7l";
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(color + count, "\u00A77Pr\u00E9parez-vous...", 0, 25, 5);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                }
            }, (5 - count) * 20L);
        }

        Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), this::finalizeStart, 5 * 20L);
    }
    
    private void finalizeStart() {
        this.state = GameState.PLAYING;
        this.startTime = System.currentTimeMillis();
        this.pvpEnabled = false;

        TeamManager tm = HelPlugin.getInstance().getTeamManager();

        // \u2500\u2500 FFA : auto-assigner chaque joueur \u00E0 sa propre \u00E9quipe \u2500\u2500
        if (tm.isSoloMode()) {
            setupFfaTeams(tm);
        }

        tm.setTeamsLocked(true);

        // Enregistrer les joueurs pr\u00E9sents au lancement
        startingPlayers.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            startingPlayers.add(p.getUniqueId());
        }

        World world = Bukkit.getWorlds().get(0);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setTime(2000); // 8h du matin
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setStorm(false);
        world.setThundering(false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

        // M\u00E9t\u00E9o fix\u00E9e pour toutes les dimensions
        for (World w : Bukkit.getWorlds()) {
            w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            w.setStorm(false);
            w.setThundering(false);
        }

        // D\u00E9sactiver la Locator Bar
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule locator_bar false");

        // Lancement des sc\u00E9narios
        HelPlugin.getInstance().getScenarioManager().onGameStart();

        // Conditionner la logique Hel
        boolean isHelMode = HelPlugin.getInstance().getScenarioManager()
                .isScenarioEnabled(fr.hel.scenario.HelScenario.class);

        if (isHelMode) {
            revokeAllHelAdvancements();
        }

        StarterInventoryManager starterInv = HelPlugin.getInstance().getStarterInventoryManager();
        
        // \u2500\u2500 D\u00E9part UHC (Plateformes) vs Hel (Centre) \u2500\u2500
        if (!isHelMode) {
            setupUhcPlatforms(tm, starterInv);
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("\u00A7a\u00A7lGO !", "\u00A7eBonne chance !", 0, 30, 10);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.5f);
                p.setGameMode(GameMode.SURVIVAL);
                p.getInventory().clear();
                starterInv.giveToPlayer(p);
                p.setInvulnerable(true);
                fr.hel.listeners.HelListener.discoverAllRecipes(p);
            }
            destroyWaitingPlatform();
            
            Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.setInvulnerable(false);
                }
            }, 1200L); // 1 minute d'invincibilit\u00E9
        }

        // Scanner d'inventaire (seulement si Hel actif)
        if (isHelMode) {
            startInventoryScanner();
        }

        // Scanner d'inventaire (seulement si Hel actif)
        if (isHelMode) {
            startInventoryScanner();
        }

        // \u2500\u2500 PVP Timer \u2500\u2500
        schedulePvpTimer();

        // \u2500\u2500 Game Duration Timer \u2500\u2500
        scheduleGameDurationTimer();

        // \u2500\u2500 Bordure \u2500\u2500
        if (borderEnabled) {
            HelPlugin.getInstance().getBorderManager().startBorder();
        } else {
            // S'assurer que la bordure est grande si d\u00E9sactiv\u00E9e
            World bw = Bukkit.getWorlds().get(0);
            bw.getWorldBorder().setSize(10000);
        }

        Bukkit.broadcastMessage("\u00A76\u00A7l\u25BA\u25BA LA PARTIE D\u00C9MARRE ! \u25C4\u25C4 \u00A7r\u00A7eQue le meilleur gagne !");

        // Annoncer le mode
        if (tm.isSoloMode()) {
            Bukkit.broadcastMessage("\u00A7d\u00A7l\u1F3AE Mode : \u00A7f\u00A7lFFA \u00A77\u2014 Chacun pour soi !");
        } else {
            Bukkit.broadcastMessage("\u00A7b\u00A7l\u1F3AE Mode : \u00A7f\u00A7l\u00C9QUIPES \u00A77\u2014 " + tm.getActiveTeamCount() + " \u00E9quipes");
        }

        // Annoncer le mode PVP
        if (pvpDisabled) {
            Bukkit.broadcastMessage("\u00A77\u00A7l\u2694 PVP : \u00A7c\u00A7lD\u00C9SACTIV\u00C9 \u00A77pour toute la partie");
        } else if (pvpTimerMinutes == 0) {
            pvpEnabled = true;
            Bukkit.broadcastMessage("\u00A7c\u00A7l\u2694 PVP ACTIV\u00C9 \u00A77d\u00E8s le d\u00E9but !");
        } else {
            Bukkit.broadcastMessage("\u00A77\u00A7l\u2694 PVP : \u00A7eActivation dans \u00A7b\u00A7l" + pvpTimerMinutes + " minutes");
        }
    }

    private void schedulePvpTimer() {
        // Annuler les t\u00E2ches pr\u00E9c\u00E9dentes si existantes
        if (pvpTask != null) pvpTask.cancel();
        if (pvpWarningTask != null) pvpWarningTask.cancel();

        if (pvpDisabled || pvpTimerMinutes == 0) return;

        final int totalSeconds = pvpTimerMinutes * 60;

        // T\u00E2che 6 : Timer PVP avec messages simples dans le chat aux paliers cl\u00E9s
        pvpTask = new org.bukkit.scheduler.BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (state != GameState.PLAYING) {
                    this.cancel();
                    return;
                }

                int remaining = totalSeconds - elapsed;

                // Annonces aux paliers cl\u00E9s (messages simples dans le chat)
                if (remaining == 20 * 60) {
                    Bukkit.broadcastMessage("\u00A77\u2694 PVP dans \u00A7f20 minutes");
                } else if (remaining == 10 * 60) {
                    Bukkit.broadcastMessage("\u00A77\u2694 PVP dans \u00A7f10 minutes");
                } else if (remaining == 5 * 60) {
                    Bukkit.broadcastMessage("\u00A77\u2694 PVP dans \u00A7f5 minutes");
                } else if (remaining == 60) {
                    Bukkit.broadcastMessage("\u00A7e\u2694 PVP dans \u00A7f1 minute");
                } else if (remaining == 10) {
                    Bukkit.broadcastMessage("\u00A7c\u2694 PVP dans \u00A7f10 secondes");
                } else if (remaining == 5) {
                    Bukkit.broadcastMessage("\u00A7c\u2694 PVP dans \u00A7f5s");
                } else if (remaining == 4) {
                    Bukkit.broadcastMessage("\u00A7c\u2694 PVP dans \u00A7f4s");
                } else if (remaining == 3) {
                    Bukkit.broadcastMessage("\u00A7c\u2694 PVP dans \u00A7f3s");
                } else if (remaining == 2) {
                    Bukkit.broadcastMessage("\u00A7c\u2694 PVP dans \u00A7f2s");
                } else if (remaining == 1) {
                    Bukkit.broadcastMessage("\u00A7c\u2694 PVP dans \u00A7f1s");
                } else if (remaining <= 0) {
                    pvpEnabled = true;
                    Bukkit.broadcastMessage("\u00A7c\u2694 Le PVP est d\u00E9sormais activ\u00E9 !");
                    for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
                    }
                    this.cancel();
                    return;
                }

                elapsed++;
            }
        }.runTaskTimer(HelPlugin.getInstance(), 0L, 20L);
    }

    private void scheduleGameDurationTimer() {
        if (gameTimerTask != null) gameTimerTask.cancel();
        
        // 0 = dur\u00E9e illimit\u00E9e (pas de timer)
        if (gameDurationMinutes <= 0) return;
        
        gameTimerTask = Bukkit.getScheduler().runTaskTimer(HelPlugin.getInstance(), () -> {
            if (state != GameState.PLAYING) return;
            
            long elapsedMin = getElapsedSeconds() / 60;
            if (elapsedMin >= gameDurationMinutes) {
                forceGameEnd("Temps \u00E9coul\u00E9 !");
            }
        }, 20 * 20L, 20 * 20L); // V\u00E9rifier toutes les 20 secondes au lieu de toutes les minutes
    }

    public void forceGameEnd(String reason) {
        if (state == GameState.FINISHED) return;
        
        this.state = GameState.FINISHED;
        
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage("\u00A7c\u00A7l\u231B FIN DE LA PARTIE : " + reason.toUpperCase());
        Bukkit.broadcastMessage("");
        
        // Terminer proprement via le listener
        HelPlugin.getInstance().getHelListener().triggerGameEnd();
    }

    public java.util.Set<UUID> getStartingPlayers() {
        return startingPlayers;
    }

    /**
     * En mode FFA : chaque joueur non-spectateur est assign\u00E9 \u00E0 sa propre \u00E9quipe.
     * 1 joueur = 1 \u00E9quipe unique \u2192 progression individuelle.
     */
    private void setupFfaTeams(fr.hel.team.TeamManager tm) {
        // Vider toutes les \u00E9quipes
        for (fr.hel.team.HelTeam team : tm.getTeams()) {
            team.getPlayers().clear();
        }
        tm.getSpectatorTeam().getPlayers().clear();

        // Collecter les joueurs en ligne
        java.util.List<Player> players = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());
        int teamIndex = 0;

        for (Player p : players) {
            if (teamIndex < tm.getTeams().size()) {
                fr.hel.team.HelTeam soloTeam = tm.getTeams().get(teamIndex);
                soloTeam.addPlayer(p);
                p.sendMessage(soloTeam.getChatColor() + "\u00A7l[FFA] \u00A7rVous jouez en solo !");
                teamIndex++;
            } else {
                // Plus de slots \u2192 spectateur
                tm.getSpectatorTeam().addPlayer(p);
                p.sendMessage("\u00A77Trop de joueurs pour le FFA ! Vous \u00EAtes spectateur.");
            }
        }

        // Adapter le nombre d'\u00E9quipes actives au nombre de joueurs
        tm.setActiveTeamCount(Math.max(2, teamIndex));

        Bukkit.getLogger().info("[Hel] FFA : " + teamIndex + " joueurs assign\u00E9s \u00E0 " + teamIndex + " \u00E9quipes individuelles.");
    }

    /**
     * Scanner d'inventaire p\u00E9riodique (toutes les 2 sec).
     * D\u00E9tecte les items obtenus via fourneau, \u00E9change, coffre, etc.
     * IMPORTANT : appelle checkTeamCompletion pour d\u00E9clencher la fin de partie.
     */
    private void startInventoryScanner() {
        Bukkit.getScheduler().runTaskTimer(HelPlugin.getInstance(), () -> {
            if (state != GameState.PLAYING) return;
            if (grid == null || grid.getObjectives().isEmpty()) return;

            fr.hel.team.TeamManager tm = HelPlugin.getInstance().getTeamManager();
            java.util.Set<fr.hel.team.HelTeam> teamsToCheck = new java.util.HashSet<>();
            java.util.List<HelObjective> objectives = grid.getObjectives();

            for (Player p : Bukkit.getOnlinePlayers()) {
                fr.hel.team.HelTeam team = tm.getPlayerTeam(p);
                if (team == null || team.getName().equals("Spectateur")) continue;

                for (org.bukkit.inventory.ItemStack item : p.getInventory().getContents()) {
                    if (item == null || item.getType() == Material.AIR) continue;

                    String objId = getIdentifier(item);
                    if (objId == null) continue;

                    for (int i = 0; i < objectives.size(); i++) {
                        HelObjective obj = objectives.get(i);
                        if (!obj.isAchievement() && obj.getId().equalsIgnoreCase(objId)) {
                            if (!team.hasUnlocked(objId)) {
                                team.unlockObjective(objId, grid.getSize());

                                // Grant advancement per-team
                                for (java.util.UUID uuid : team.getPlayers()) {
                                    Player tp = Bukkit.getPlayer(uuid);
                                    if (tp != null) {
                                        grantHelAdvancement(tp, i);
                                        tp.playSound(tp.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.4f, 1.2f);
                                        

                                    }
                                }

                                String displayName = getDisplayName(objId);
                                // FFA = pseudo blanc, Team = couleur d'\u00E9quipe
                                if (tm.isSoloMode()) {
                                    Bukkit.broadcastMessage("\u00A78[\u00A76Hel\u00A78] \u00A7f" + p.getName() + " \u00A7aa trouv\u00E9 \u00A7e" + displayName + " \u00A7a!");
                                } else {
                                    Bukkit.broadcastMessage("\u00A78[\u00A76Hel\u00A78] " + team.getChatColor() + "L'\u00E9quipe " + team.getName() + " \u00A7aa trouv\u00E9 \u00A7e" + displayName + " \u00A7a!");
                                }

                                int seconds = (int) getElapsedSeconds();
                                HelPlugin.getInstance().getDatabaseManager().recordStat(p.getName(), seconds, objId);
                                teamsToCheck.add(team);
                            }
                            break;
                        }
                    }
                }
            }

            // V\u00E9rifier la compl\u00E9tion pour chaque \u00E9quipe qui a trouv\u00E9 un nouvel item
            for (fr.hel.team.HelTeam team : teamsToCheck) {
                checkScannerTeamCompletion(team);
            }
        }, 40L, 40L);
    }

    /**
     * V\u00E9rifie si une \u00E9quipe a termin\u00E9 tous les objectifs (appel\u00E9 par le scanner).
     */
    private void checkScannerTeamCompletion(fr.hel.team.HelTeam team) {
        int totalObjectives = grid.getObjectives().size();
        int teamFound = team.getUnlockedObjectives().size();

        if (teamFound >= totalObjectives && !team.isFinished()) {
            team.setFinished(true);
            TeamManager tm = HelPlugin.getInstance().getTeamManager();

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
                    ? "\u00A7f\u00A7l\u2605 " + playerName + " a termin\u00E9 le Hel ! \u2605"
                    : team.getChatColor() + "\u00A7l\u2605 L'\u00E9quipe " + team.getName() + " a termin\u00E9 le Hel ! \u2605";

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("\u00A78\u00A7m                                                \u00A7r");
            Bukkit.broadcastMessage("  " + teamLabel);
            Bukkit.broadcastMessage("  \u00A77Temps : \u00A7e\u00A7l" + timeStr);
            Bukkit.broadcastMessage("\u00A78\u00A7m                                                \u00A7r");
            Bukkit.broadcastMessage("");

            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.setGameMode(GameMode.SPECTATOR);
                    p.getInventory().clear();
                    p.sendTitle(tm.isSoloMode() ? "\u00A7f\u00A7lBINGO !" : team.getChatColor() + "\u00A7lBINGO !", "\u00A77Temps : \u00A7e" + timeStr, 10, 60, 20);
                }
            }
        }
    }

    /**
     * Synchronise les advancements d'une \u00E9quipe pour un joueur qui vient de la rejoindre.
     */
    public void syncTeamAdvancements(Player player, fr.hel.team.HelTeam team) {
        if (state != GameState.PLAYING) return;
        List<HelObjective> objectives = grid.getObjectives();
        for (int i = 0; i < objectives.size(); i++) {
            if (team.hasUnlocked(objectives.get(i).getId())) {
                grantHelAdvancement(player, i);
            }
        }
    }

    /**
     * R\u00E9voque tous les advancements bingoclassique pour tous les joueurs.
     * Appel\u00E9 au start pour un tracking propre per-team.
     */
    public void revokeAllHelAdvancements() {
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
     * Accorde un advancement bingo \u00E0 un joueur sp\u00E9cifique (per-team tracking).
     */
    public void grantHelAdvancement(Player player, int index) {
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

    /**
     * Logique de d\u00E9part UHC : Plateformes aux extr\u00E9mit\u00E9s de la bordure.
     */
    private void setupUhcPlatforms(TeamManager tm, StarterInventoryManager starterInv) {
        List<fr.hel.team.HelTeam> activeTeams = tm.getActiveTeams();
        if (activeTeams.isEmpty()) return;

        World world = Bukkit.getWorlds().get(0);
        int borderSize = HelPlugin.getInstance().getBorderManager().getInitialSize();
        double radius = (borderSize / 2.0) - 20; // Un peu de marge par rapport \u00E0 la bordure
        int platformY = 200;

        List<Location> platformLocs = new java.util.ArrayList<>();
        int numTeams = activeTeams.size();

        for (int i = 0; i < numTeams; i++) {
            double angle = (2 * Math.PI / numTeams) * i;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            
            // Cr\u00E9er la plateforme 3x3 en verre
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    world.getBlockAt((int)x, platformY, (int)z).getRelative(dx, 0, dz).setType(Material.GLASS);
                }
            }
            platformLocs.add(new Location(world, x + 0.5, platformY + 1, z + 0.5));
        }

        // M\u00E9langer les plateformes pour l'al\u00E9atoire
        java.util.Collections.shuffle(platformLocs);

        // TP des \u00E9quipes
        for (int i = 0; i < activeTeams.size(); i++) {
            fr.hel.team.HelTeam team = activeTeams.get(i);
            Location loc = platformLocs.get(i);
            
            // Pregen chunk
            loc.getChunk().load(true);
            
            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.teleportAsync(loc);
                    p.setGameMode(GameMode.SURVIVAL);
                    p.getInventory().clear();
                    starterInv.giveToPlayer(p);
                    p.setInvulnerable(true);
                    fr.hel.listeners.HelListener.discoverAllRecipes(p);
                    p.sendTitle("\u00A7e60 secondes", "\u00A77Pr\u00E9paration & Invinciilit\u00E9", 0, 25, 5);
                }
            }
        }

        destroyWaitingPlatform();

        // 1 minute de compte \u00E0 rebours avant suppression plateformes + invincibilit\u00E9
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = 60;
            @Override
            public void run() {
                if (count <= 0) {
                    for (Location loc : platformLocs) {
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                loc.getBlock().getRelative(dx, -1, dz).setType(Material.AIR);
                            }
                        }
                    }
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.setInvulnerable(false);
                        p.sendTitle("\u00A7a\u00A7lGO !", "\u00A7eBonne chance !", 0, 30, 10);
                        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.5f);
                    }
                    this.cancel();
                    return;
                }
                
                if (count <= 5 || count == 30 || count == 15) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        String color = count <= 5 ? "\u00A7c" : "\u00A7e";
                        p.sendTitle(color + count, "\u00A77Lancement...", 0, 21, 0);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    }
                }
                count--;
            }
        }.runTaskTimer(HelPlugin.getInstance(), 20L, 20L);
    }

    private void destroyWaitingPlatform() {
        if (lobbyHologram != null) lobbyHologram.remove();
        jumpManager.cleanupAll();
        
        World world = waitingPlatformLocation.getWorld();
        int y = 250;
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                for (int h = 0; h <= 4; h++) {
                    world.getBlockAt(x, y + h, z).setType(Material.AIR);
                }
            }
        }
    }

    // \u2500\u2500 Pause / Resume \u2500\u2500

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
            p.sendTitle("\u00A7c\u00A7lPAUSE", "\u00A77La partie est en pause", 0, 60, 10);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 0.5f);
        }
        Bukkit.broadcastMessage("\u00A7c\u00A7l\u25BA\u25BA PARTIE EN PAUSE \u25C4\u25C4");
    }

    public void resumeParty() {
        if (this.state != GameState.PAUSED) return;
        // Recalculer le startTime pour conserver le temps \u00E9coul\u00E9
        this.startTime = System.currentTimeMillis() - (pausedElapsed * 1000);
        this.state = GameState.PLAYING;

        // Unfreeze tous les joueurs
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.SLOWNESS);
            p.removePotionEffect(PotionEffectType.JUMP_BOOST);
            p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
            p.setInvulnerable(false);
            p.sendTitle("\u00A7a\u00A7lREPRISE !", "\u00A7eC'est reparti !", 0, 30, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
        Bukkit.broadcastMessage("\u00A7a\u00A7l\u25BA\u25BA REPRISE DE LA PARTIE ! \u25C4\u25C4");
    }

    // \u2500\u2500 Reset \u2500\u2500

    public void resetGame() {
        this.state = GameState.WAITING;
        this.pvpEnabled = false;

        // Annuler les t\u00E2ches PVP
        if (pvpTask != null) { pvpTask.cancel(); pvpTask = null; }
        if (pvpWarningTask != null) { pvpWarningTask.cancel(); pvpWarningTask = null; }

        HelPlugin.getInstance().getTeamManager().setTeamsLocked(false);

        // Reset toutes les \u00E9quipes
        for (fr.hel.team.HelTeam team : HelPlugin.getInstance().getTeamManager().getTeams()) {
            team.getUnlockedObjectives().clear();
            team.setFinished(false);
            team.setScore(0);
        }

        setupWaitingPlatform();

        for (Player p : Bukkit.getOnlinePlayers()) {
            // Retirer les effets de pause si pr\u00E9sents
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
            HelPlugin.getInstance().getTeamManager().giveTeamBanners(p);

            // Donner le compas admin
            if (p.hasPermission("bingo.admin")) {
                giveAdminCompass(p);
            }
        }

        World world = Bukkit.getWorlds().get(0);
        // Garder jour \u00E9ternel + pas de pluie pendant le hub
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(6000);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setStorm(false);
        world.setThundering(false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, true);

        Bukkit.broadcastMessage("\u00A76\u00A7l\u25BA\u25BA La partie a \u00E9t\u00E9 r\u00E9initialis\u00E9e ! \u25C4\u25C4");
    }

    // \u2500\u2500 Admin Compass \u2500\u2500

    public static void giveAdminCompass(Player player) {
        org.bukkit.inventory.ItemStack compass = new org.bukkit.inventory.ItemStack(Material.NETHER_STAR);
        org.bukkit.inventory.meta.ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName("\u00A76\u00A7l\u2699 Configuration HEL");
        meta.setLore(java.util.List.of("\u00A77Clic droit pour configurer la partie"));
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(HelPlugin.getInstance(), "admin_compass");
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        compass.setItemMeta(meta);
        player.getInventory().setItem(8, compass); // Slot 9 (dernier)
    }

    /**
     * Pr\u00E9pare la r\u00E9initialisation compl\u00E8te du monde (Seed + Suppression dossiers).
     * N\u00E9cessite le script run_bingo.bat pour la partie suppression physique.
     * @param admin le joueur admin qui a d\u00E9clench\u00E9 l'action, ou null si automatique.
     */
    public void prepareWorldReset(Player admin) {
        // 1. G\u00E9n\u00E9rer une nouvelle Seed
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
                    props.store(out, "Modified by Hel Plugin for World Reset");
                }
            }
        } catch (java.io.IOException e) {
            String errMsg = "\u00A7c[Erreur] Impossible de modifier server.properties : " + e.getMessage();
            if (admin != null) {
                admin.sendMessage(errMsg);
            } else {
                Bukkit.getLogger().warning(errMsg);
            }
            return;
        }

        // 3. Cr\u00E9er le flag reset_map.txt
        try {
            new java.io.File("reset_map.txt").createNewFile();
        } catch (java.io.IOException e) {
            String errMsg = "\u00A7c[Erreur] Impossible de cr\u00E9er le flag reset_map.txt.";
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
                    Bukkit.broadcastMessage("\u00A7c\u00A7lRED\u00C9MARRAGE DU SERVEUR !");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.kickPlayer("\u00A7c\u00A7lR\u00E9initialisation du monde...\n\n\u00A77Le serveur revient dans quelques instants sur une nouvelle map !");
                    }
                    Bukkit.shutdown();
                    this.cancel();
                    return;
                }

                if (count <= 5 || count == 10) {
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage("\u00A7c\u00A7l  \u26A0 R\u00C9INITIALISATION DU MONDE DANS \u00A7e" + count + " \u00A7c\u00A7lSECONDES ! \u26A0");
                    Bukkit.broadcastMessage("\u00A77  (Dossiers world, world_nether et world_the_end seront supprim\u00E9s)");
                    Bukkit.broadcastMessage("");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                        p.sendTitle("\u00A7c\u00A7l\u26A0 RESET WORLD", "\u00A7eDans " + count + " secondes...", 0, 25, 5);
                    }
                }
                count--;
            }
        }.runTaskTimer(HelPlugin.getInstance(), 0L, 20L);
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

    public enum BiomeSize {
        SMALL(2), MEDIUM(4), LARGE(6);
        private final int value;
        BiomeSize(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    // Getters / Setters World Config
    public BiomeSize getBiomeSize() { return biomeSize; }
    public void setBiomeSize(BiomeSize biomeSize) { 
        this.biomeSize = biomeSize; 
        saveSettings();
    }
    public long getWorldSeed() { return worldSeed; }
    public void setWorldSeed(long seed) { 
        this.worldSeed = seed; 
        saveSettings();
    }
    public java.util.Set<String> getDisabledBiomes() { return disabledBiomes; }

    public GenerationType getGenerationType() { return generationType; }
    public void setGenerationType(GenerationType type) { 
        this.generationType = type; 
        saveSettings();
    }

    public int getMiniNetherDensity() { return miniNetherDensity; }
    public void setMiniNetherDensity(int density) { 
        this.miniNetherDensity = density; 
        saveSettings();
    }

    public enum GenerationType {
        V_1_8_STANDARD("1.8 Standard"),
        V_1_8_UHC_RUN("1.8 UHC RUN"),
        V_1_21_11("1.21.11");

        private final String displayName;
        GenerationType(String name) { this.displayName = name; }
        public String getDisplayName() { return displayName; }
    }
}
