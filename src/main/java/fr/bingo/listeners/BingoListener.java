package fr.bingo.listeners;

import fr.bingo.BingoPlugin;
import fr.bingo.game.*;
import fr.bingo.gui.AdminConfigGUI;
import fr.bingo.gui.PvpConfigGUI;
import fr.bingo.gui.TeamConfigGUI;
import fr.bingo.gui.TeamSelectorGUI;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.scheduler.BukkitRunnable;

public class BingoListener implements Listener {

    // ── Sauvegarde d'état joueur pour la reconnexion (Tâche 1) ──
    private static class PlayerState {
        final ItemStack[] inventory;
        final ItemStack[] armor;
        final ItemStack offhand;
        final Location location;
        final GameMode gameMode;
        final double health;
        final int foodLevel;
        final float saturation;
        final float exp;
        final int level;
        final int totalExp;

        PlayerState(Player p) {
            this.inventory = p.getInventory().getStorageContents().clone();
            this.armor = p.getInventory().getArmorContents().clone();
            this.offhand = p.getInventory().getItemInOffHand().clone();
            this.location = p.getLocation().clone();
            this.gameMode = p.getGameMode();
            this.health = p.getHealth();
            this.foodLevel = p.getFoodLevel();
            this.saturation = p.getSaturation();
            this.exp = p.getExp();
            this.level = p.getLevel();
            this.totalExp = p.getTotalExperience();
        }

        void restore(Player p) {
            p.teleport(location);
            p.setGameMode(gameMode);
            p.getInventory().setStorageContents(inventory);
            p.getInventory().setArmorContents(armor);
            p.getInventory().setItemInOffHand(offhand);
            p.setHealth(Math.min(health, p.getMaxHealth()));
            p.setFoodLevel(foodLevel);
            p.setSaturation(saturation);
            p.setExp(exp);
            p.setLevel(level);
            p.setTotalExperience(totalExp);
        }
    }

    private final Map<UUID, PlayerState> savedStates = new ConcurrentHashMap<>();

    public BingoListener() {
        startActionBarFFTask();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Custom Join Message
        event.setJoinMessage("§8[§a+§8] §f" + player.getName());
        
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();

        BingoPlugin.getInstance().getLogger().info("[Bingo] Connexion de " + player.getName() + " - Etat: " + game.getState());

        // Si la partie est lancée, on ne touche pas au joueur s'il est déjà en jeu
        if (game.getState() == GameState.PLAYING) {
            BingoTeam team = teamManager.getPlayerTeam(player);
            if (team == null) {
                team = teamManager.getPlayerTeamByName(player.getName());
                if (team != null) BingoPlugin.getInstance().getLogger().info("[Bingo] Joueur reconnu par pseudo : " + player.getName());
            }

            if (team == null || team.getName().equals("Spectateur")) {
                // Nouveau joueur ou spectateur rejoignant en cours de route
                if (team == null) teamManager.joinTeam(player, teamManager.getSpectatorTeam());
                player.setGameMode(GameMode.SPECTATOR);
                BingoPlugin.getInstance().getLogger().info("[Bingo] Nouveau joueur/Spec forcé en spectateur : " + player.getName());
            } else {
                // Restaurer l'état sauvegardé (Tâche 1 — fix reconnexion)
                PlayerState state = savedStates.remove(player.getUniqueId());
                if (state != null) {
                    // Délai de 1 tick pour laisser le serveur finir le login
                    final BingoTeam finalTeam = team;
                    Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                        state.restore(player);
                        game.syncTeamAdvancements(player, finalTeam);
                        BingoPlugin.getInstance().getLogger().info("[Bingo] État restauré pour " + player.getName() + " (Equipe: " + finalTeam.getName() + ")");
                    }, 1L);
                } else {
                    // Pas d'état sauvegardé, mais le joueur est dans une équipe
                    // On force le gamemode survival et on sync les advancements
                    final BingoTeam finalTeam = team;
                    Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                        player.setGameMode(GameMode.SURVIVAL);
                        game.syncTeamAdvancements(player, finalTeam);
                    }, 1L);
                }
                BingoPlugin.getInstance().getLogger().info("[Bingo] Reconnexion autorisée pour " + player.getName() + " (Equipe: " + team.getName() + ")");
            }

            // Débloquer tous les crafts pour le joueur qui rejoint (Tâche 4)
            Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                discoverAllRecipes(player);
            }, 2L);

            return; // On arrête là pour les parties en cours - SECURITE ABSOLUE
        }

        // --- Logique du HUB (WAITING) ---
        if (teamManager.getPlayerTeam(player) == null) {
            teamManager.joinTeam(player, teamManager.getSpectatorTeam());
        }

        if (game.getState() == GameState.WAITING) {
            game.teleportToWaitingArea(player);

            // Bannière de sélection d'équipe (slot 4, centre hotbar)
            teamManager.giveTeamBanner(player);

            // Compas admin
            if (player.hasPermission("bingo.admin")) {
                BingoGame.giveAdminCompass(player);
            }

            startActionBarReminder(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Custom Quit Message
        event.setQuitMessage("§8[§c-§8] §f" + player.getName());
        
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();

        // Sauvegarder l'état du joueur s'il est en partie (Tâche 1)
        if (game.getState() == GameState.PLAYING) {
            BingoTeam team = teamManager.getPlayerTeam(player);
            if (team != null && !team.getName().equals("Spectateur")) {
                savedStates.put(player.getUniqueId(), new PlayerState(player));
                BingoPlugin.getInstance().getLogger().info("[Bingo] État sauvegardé pour " + player.getName() + " (Equipe: " + team.getName() + ")");
            }
        }
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        
        String mode = tm.isSoloMode() ? "FFA" : "To" + tm.getMaxPlayersPerTeam();
        String diff = game.getDifficulty().getDisplayName();
        String size = game.getGrid().getSize() + "x" + game.getGrid().getSize();
        String type = game.getMode().getDisplayName();

        if (game.getState() == GameState.WAITING) {
            event.setMotd("§6§lBINGO §7» §f" + mode + " §8| §7" + type + " §8| §7" + diff + " §8| §7" + size + "\n§e§l➡ §aEn attente de joueurs...");
        } else if (game.getState() == GameState.PLAYING) {
            long elapsed = game.getElapsedSeconds();
            String timer = String.format("%02d:%02d", elapsed / 60, elapsed % 60);
            event.setMotd("§6§lBINGO §7» §cEn cours §8| §e" + timer + " §8| §b" + mode + " §8| §b" + type + "\n§e§l➡ §f" + diff + " §8| §f" + size);
        } else {
            event.setMotd("§6§lBINGO §7» §8Partie terminée");
        }
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        if (game.getState() == GameState.PLAYING) {
            UUID uuid = event.getUniqueId();
            String name = event.getName();
            TeamManager tm = BingoPlugin.getInstance().getTeamManager();
            
            // On autorise si: présent au lancement OU dans une équipe (y compris spectateur)
            boolean authorized = game.getStartingPlayers().contains(uuid);
            if (!authorized) {
                fr.bingo.team.BingoTeam team = tm.getPlayerTeam(uuid);
                if (team == null) team = tm.getPlayerTeamByName(name);
                
                if (team != null) {
                    authorized = true;
                }
            }
            
            // OPs toujours autorisés
            if (!authorized && !org.bukkit.Bukkit.getOfflinePlayer(uuid).isOp()) {
                long elapsed = game.getElapsedSeconds();
                String timer = String.format("%02d:%02d", elapsed / 60, elapsed % 60);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, 
                    "§c§lBINGO\n\n§cUne partie est déjà lancée !\n§7Temps écoulé : §e" + timer + "\n\n§7Seuls les participants peuvent rejoindre.");
            }
        }
    }

    private void startActionBarFFTask() {
        Bukkit.getScheduler().runTaskTimer(BingoPlugin.getInstance(), () -> {
            BingoGame game = BingoPlugin.getInstance().getBingoGame();
            if (game.getState() != GameState.PLAYING) return;

            TeamManager tm = BingoPlugin.getInstance().getTeamManager();
            if (tm.isSoloMode()) return;

            for (BingoTeam team : tm.getActiveTeams()) {
                int votes = team.getForfeitVoteCount();
                if (votes > 0) {
                    int total = team.getPlayers().size();
                    String message = "§c§lVote FF : §e" + votes + "§7/§e" + total;
                    for (UUID uuid : team.getPlayers()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                        }
                    }
                }
            }
        }, 0L, 20L);
    }

    private void startActionBarReminder(Player player) {
        Bukkit.getScheduler().runTaskTimer(BingoPlugin.getInstance(), () -> {
            if (!player.isOnline()) return;
            if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.WAITING) return;

            TeamManager tm = BingoPlugin.getInstance().getTeamManager();
            BingoTeam team = tm.getPlayerTeam(player);
            boolean isSpectator = (team != null && team.getName().equals("Spectateur"));

            String message;
            if (tm.isSoloMode()) {
                if (isSpectator) {
                    message = "§e§l⚑ Clique sur l'item pour PARTICIPER ⚑";
                } else {
                    message = "§a§l⚑ Vous participez au Bingo Solo ⚑";
                }
            } else {
                if (team == null || isSpectator) {
                    message = "§b§l⚑ Clique sur la bannière pour choisir ton équipe ⚑";
                } else {
                    message = "§a§l⚑ Vous êtes prêt pour la partie ! ⚑";
                }
            }

            player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacy(message)
            );
        }, 0L, 30L);
    }

    // ── Clic inventaire ──

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // GUI Grille Bingo
        if (event.getInventory().getHolder() instanceof fr.bingo.gui.BingoGridGUI) {
            event.setCancelled(true);
            return;
        }

        // GUI Admin Config
        if (event.getInventory().getHolder() instanceof AdminConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // GUI Scenario Config
        if (event.getInventory().getHolder() instanceof fr.bingo.gui.ScenarioConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // GUI Super Hero Config
        if (event.getInventory().getHolder() instanceof fr.bingo.gui.SuperHeroConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot());
            return;
        }

        // GUI Pool Config
        if (event.getInventory().getHolder() instanceof fr.bingo.gui.PoolConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot());
            return;
        }

        // GUI Preset Config
        if (event.getInventory().getHolder() instanceof fr.bingo.gui.PresetConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // GUI PVP Config
        if (event.getInventory().getHolder() instanceof PvpConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // GUI Team Config
        if (event.getInventory().getHolder() instanceof TeamConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // Détection d'items pour le Bingo (si dans l'inventaire du joueur)
        if (BingoPlugin.getInstance().getBingoGame().getState() == GameState.PLAYING) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR) {
                checkObjective(player, getIdentifier(clicked));
            }
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                checkObjective(player, getIdentifier(cursor));
            }
        }

        // GUI Team Selector — clic sur une bannière d'équipe
        if (event.getInventory().getHolder() instanceof TeamSelectorGUI) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            // Ignorer les glass panes
            if (!event.getCurrentItem().getType().name().endsWith("_BANNER")) return;

            TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();
            Material clickedMat = event.getCurrentItem().getType();

            // Bannière blanche = spectateur (toujours accessible)
            if (clickedMat == Material.WHITE_BANNER) {
                teamManager.joinTeam(player, teamManager.getSpectatorTeam());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new TeamSelectorGUI().getInventory());
                return;
            }

            // Les autres bannières = équipes
            if (teamManager.isTeamsLocked() && !player.hasPermission("bingo.admin")) {
                player.sendMessage("§cLes équipes sont verrouillées !");
                player.closeInventory();
                return;
            }

            for (BingoTeam team : teamManager.getActiveTeams()) {
                if (team.getBannerMaterial() == clickedMat) {
                    if (teamManager.joinTeam(player, team)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                        player.openInventory(new TeamSelectorGUI().getInventory());
                    }
                    return;
                }
            }
        }

        // Empêcher de déplacer la bannière de sélection en WAITING
        if (BingoPlugin.getInstance().getBingoGame().getState() == GameState.WAITING) {
            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "team_selector");
                if (event.getCurrentItem().getItemMeta().getPersistentDataContainer()
                        .has(key, org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // ── Clic droit : bannière d'équipe OU compas admin ──

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() == null || !event.getItem().hasItemMeta()) return;
        if (!event.getAction().name().contains("RIGHT")) return;

        org.bukkit.persistence.PersistentDataContainer pdc = event.getItem().getItemMeta().getPersistentDataContainer();

        // Compas admin
        org.bukkit.NamespacedKey compassKey = new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "admin_compass");
        if (pdc.has(compassKey, org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
            if (!player.hasPermission("bingo.admin")) {
                player.sendMessage("§cPermission refusée.");
                return;
            }
            player.openInventory(new AdminConfigGUI().getInventory());
            return;
        }

        // Item de sélection d'équipe (Bannière ou Tête)
        org.bukkit.NamespacedKey teamKey = new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "team_selector");
        if (pdc.has(teamKey, org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
            if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.WAITING) return;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

            TeamManager tm = BingoPlugin.getInstance().getTeamManager();
            if (tm.isSoloMode()) {
                // Toggle FFA : Joueur <-> Spectateur
                BingoTeam current = tm.getPlayerTeam(player);
                if (current != null && current.getName().equals("Spectateur")) {
                    tm.removePlayerFromTeam(player);
                    player.sendMessage("§a§lBingo §7» §fVous participez désormais à la partie !");
                } else {
                    tm.joinTeam(player, tm.getSpectatorTeam());
                    player.sendMessage("§a§lBingo §7» §7Vous êtes désormais spectateur.");
                }
                // Mettre à jour l'item visuellement
                tm.giveTeamBanner(player);
            } else {
                // Mode Équipe : Ouvrir le menu classique
                player.openInventory(new TeamSelectorGUI().getInventory());
            }
        }
    }

    // ── Protection gameplay ──

    @EventHandler
    public void onItemDrop(org.bukkit.event.player.PlayerDropItemEvent event) {
        if (BingoPlugin.getInstance().getBingoGame().getState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        // Empêcher de drop la bannière de sélection
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (dropped.hasItemMeta()) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "team_selector");
            if (dropped.getItemMeta().getPersistentDataContainer()
                    .has(key, org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        BingoGame game = BingoPlugin.getInstance().getBingoGame();

        if (game.getState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (game.isPvpDisabled() || !game.isPvpEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (BingoPlugin.getInstance().getBingoGame().getState() == GameState.WAITING) {
            if (event.getEntity() instanceof Player p) {
                event.setCancelled(true);
                p.setFoodLevel(20);
                p.setSaturation(20f);
            }
        }
    }

    // ── Détection d'objectifs ──

    private void checkObjective(Player player, String objectiveId) {
        if (objectiveId == null) return;
        if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.PLAYING) return;

        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();
        BingoTeam team = teamManager.getPlayerTeam(player);
        if (team == null || team.getName().equals("Spectateur")) return;

        BingoGrid grid = BingoPlugin.getInstance().getBingoGame().getGrid();
        List<BingoObjective> objectives = grid.getObjectives();
        for (int i = 0; i < objectives.size(); i++) {
            BingoObjective obj = objectives.get(i);
            if (obj.getId().equalsIgnoreCase(objectiveId)) {
                if (!team.hasUnlocked(objectiveId)) {
                    team.unlockObjective(objectiveId, grid.getSize());

                    // Accorder l'advancement UNIQUEMENT aux joueurs de cette équipe
                    for (java.util.UUID uuid : team.getPlayers()) {
                        Player tp = Bukkit.getPlayer(uuid);
                        if (tp != null) {
                            BingoPlugin.getInstance().getBingoGame().grantBingoAdvancement(tp, i);
                        }
                    }

                    String displayName = obj.getId().replace("_", " ");
                    String prefix = obj.isAchievement() ? "§d[Achievement] " : "";

                    // En FFA : pseudo blanc, pas de couleur d'équipe
                    if (teamManager.isSoloMode()) {
                        Bukkit.broadcastMessage("§8[§6Bingo§8] §f" + player.getName() + " §aa trouvé " + prefix + "§e" + displayName + " §a!");
                    } else {
                        Bukkit.broadcastMessage("§8[§6Bingo§8] " + team.getChatColor() + "L'équipe " + team.getName() + " §aa trouvé " + prefix + "§e" + displayName + " §a!");
                    }

                    playFoundEffects(player, team);

                    int seconds = (int) BingoPlugin.getInstance().getBingoGame().getElapsedSeconds();
                    BingoPlugin.getInstance().getDatabaseManager().recordStat(player.getName(), seconds, objectiveId);

                    checkTeamCompletion(team, grid);
                }
                break;
            }
        }
    }

    private void playFoundEffects(Player player, BingoTeam team) {
        for (java.util.UUID uuid : team.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
                p.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5);
            }
        }
    }

    // ── Fin de partie ──

    private void checkTeamCompletion(BingoTeam team, BingoGrid grid) {
        int totalObjectives = grid.getObjectives().size();
        int teamFound = team.getUnlockedObjectives().size();

        if (teamFound >= totalObjectives && !team.isFinished()) {
            team.setFinished(true);
            TeamManager tm = BingoPlugin.getInstance().getTeamManager();

            long elapsed = BingoPlugin.getInstance().getBingoGame().getElapsedSeconds();
            int min = (int) (elapsed / 60);
            int sec = (int) (elapsed % 60);
            String timeStr = String.format("%02d:%02d", min, sec);

            String teamLabel = tm.isSoloMode()
                    ? team.getChatColor() + "§l★ " + getTeamPlayerName(team) + " a terminé le Bingo ! ★"
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
                    cleanupAndSpectate(p);
                    p.sendTitle(team.getChatColor() + "§lBINGO !", "§7Temps : §e" + timeStr, 10, 60, 20);
                    launchFirework(p.getLocation());
                }
            }

            checkEndCondition();
        }
    }

    private String getTeamPlayerName(BingoTeam team) {
        if (team.getPlayers().isEmpty()) return "???";
        org.bukkit.OfflinePlayer p = Bukkit.getOfflinePlayer(team.getPlayers().get(0));
        return p.getName() != null ? p.getName() : "???";
    }

    private void cleanupAndSpectate(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();
        player.setExp(0f);
        player.setLevel(0);
        player.setTotalExperience(0);

        Iterator<Advancement> advIterator = Bukkit.advancementIterator();
        while (advIterator.hasNext()) {
            Advancement adv = advIterator.next();
            AdvancementProgress progress = player.getAdvancementProgress(adv);
            for (String criteria : progress.getAwardedCriteria()) {
                progress.revokeCriteria(criteria);
            }
        }
    }

    private void launchFirework(Location loc) {
        org.bukkit.entity.Firework fw = loc.getWorld().spawn(loc, org.bukkit.entity.Firework.class);
        org.bukkit.inventory.meta.FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(org.bukkit.FireworkEffect.builder()
                .withColor(Color.YELLOW, Color.ORANGE)
                .withFade(Color.RED)
                .with(org.bukkit.FireworkEffect.Type.STAR)
                .flicker(true)
                .trail(true)
                .build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    public void checkEndCondition() {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        EndMode endMode = game.getEndMode();

        List<BingoTeam> activeTeams = tm.getActiveTeams();
        int totalPlaying = 0;
        int notFinished = 0;

        for (BingoTeam t : activeTeams) {
            if (t.getPlayers().isEmpty()) continue;
            totalPlaying++;
            if (!t.isFinished()) notFinished++;
        }

        if (endMode == EndMode.ALL_TEAMS) {
            if (notFinished == 0 && totalPlaying > 0) {
                triggerGameEnd();
            }
        } else if (endMode == EndMode.LAST_STANDING) {
            if (totalPlaying >= 2 && notFinished <= 1) {
                if (notFinished == 1) {
                    for (BingoTeam t : activeTeams) {
                        if (!t.getPlayers().isEmpty() && !t.isFinished()) {
                            String label = tm.isSoloMode() ? getTeamPlayerName(t) : "L'équipe " + t.getName();
                            Bukkit.broadcastMessage("");
                            Bukkit.broadcastMessage("§8§m                                                §r");
                            Bukkit.broadcastMessage("  §6§l⏰ La partie est terminée !");
                            Bukkit.broadcastMessage("  " + t.getChatColor() + label + " §7est la dernière en jeu.");
                            Bukkit.broadcastMessage("§8§m                                                §r");
                            Bukkit.broadcastMessage("");
                            for (java.util.UUID uuid : t.getPlayers()) {
                                Player p = Bukkit.getPlayer(uuid);
                                if (p != null) cleanupAndSpectate(p);
                            }
                            break;
                        }
                    }
                }
                triggerGameEnd();
            } else if (notFinished == 0 && totalPlaying > 0) {
                triggerGameEnd();
            }
        } else if (endMode == EndMode.FIRST_TO_FINISH) {
            if (notFinished < totalPlaying && totalPlaying > 0) {
                triggerGameEnd();
            }
        }
    }

    public void triggerGameEnd() {
        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§6§l✦✦✦ PARTIE TERMINÉE ! ✦✦✦");
            Bukkit.broadcastMessage("§7Merci d'avoir joué !");
            Bukkit.broadcastMessage("");

            displayRanking();

            BingoPlugin.getInstance().getBingoGame().setState(GameState.FINISHED);

            // Tâche 2 : Décompte de 300 secondes dans l'ActionBar avant changement de map
            startEndCountdown();
        }, 60L);
    }

    /**
     * Décompte de 300 secondes (5 minutes) affiché discrètement dans l'ActionBar.
     * À la fin, déclenche le changement de map automatique.
     */
    private void startEndCountdown() {
        new BukkitRunnable() {
            int remaining = 300;

            @Override
            public void run() {
                if (remaining <= 0) {
                    this.cancel();
                    Bukkit.broadcastMessage("§6§l► Changement de carte en cours...");
                    BingoPlugin.getInstance().getBingoGame().prepareWorldReset(null);
                    return;
                }

                int min = remaining / 60;
                int sec = remaining % 60;
                String timeStr = min > 0
                        ? "§b" + min + "m" + String.format("%02d", sec) + "s"
                        : "§b" + sec + "s";
                String actionBarMsg = "§7Changement de carte dans " + timeStr;

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacy(actionBarMsg));
                }

                remaining--;
            }
        }.runTaskTimer(BingoPlugin.getInstance(), 0L, 20L);
    }

    private void displayRanking() {
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        List<BingoTeam> ranking = new java.util.ArrayList<>(tm.getActiveTeams());
        ranking.removeIf(t -> t.getPlayers().isEmpty());
        ranking.sort((a, b) -> {
            if (a.isFinished() && !b.isFinished()) return -1;
            if (!a.isFinished() && b.isFinished()) return 1;
            if (a.isFinished() && b.isFinished()) return Long.compare(a.getFinishedTime(), b.getFinishedTime());
            return Integer.compare(b.getScore(), a.getScore());
        });

        Bukkit.broadcastMessage("§8§m                                                §r");
        Bukkit.broadcastMessage("  §6§l✦ CLASSEMENT FINAL ✦");
        String[] medals = {"§e§l🥇 ", "§f§l🥈 ", "§6§l🥉 ", "§7   ", "§7   "};
        for (int i = 0; i < ranking.size(); i++) {
            BingoTeam t = ranking.get(i);
            String medal = i < medals.length ? medals[i] : "§7   ";
            String name = tm.isSoloMode() ? getTeamPlayerName(t) : t.getName();
            String status = t.isFinished() ? "§a✔ Terminé" : "§7" + t.getScore() + " pts";
            Bukkit.broadcastMessage("  " + medal + t.getChatColor() + name + " §8- " + status);
        }
        Bukkit.broadcastMessage("§8§m                                                §r");
    }

    // ── Events de détection ──

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            checkObjective(player, getIdentifier(event.getItem().getItemStack()));
        }
    }
 
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            ItemStack result = event.getCurrentItem();
            if (result != null && result.getType() != Material.AIR) {
                checkObjective(player, getIdentifier(result));
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            ItemStack dragged = event.getOldCursor();
            if (dragged != null && dragged.getType() != Material.AIR) {
                checkObjective(player, getIdentifier(dragged));
            }
        }
    }

    private String getIdentifier(ItemStack item) {
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

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        String key = event.getAdvancement().getKey().getKey();
        if (key.contains("recipes/")) return;
        if (event.getAdvancement().getKey().getNamespace().equals("bingoclassique")) return;
        checkObjective(event.getPlayer(), key);
    }

    // ── Chat team ──

    @EventHandler
    public void onPlayerChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // --- Vérification Preset Save ---
        fr.bingo.preset.PresetManager manager = new fr.bingo.preset.PresetManager();
        fr.bingo.preset.PresetData pending = manager.getPendingSave(player.getUniqueId());
        if (pending != null) {
            event.setCancelled(true);
            
            if (message.equalsIgnoreCase("annuler") || message.equalsIgnoreCase("cancel")) {
                manager.removePendingSave(player.getUniqueId());
                player.sendMessage("§c[Bingo] §fCréation de la sauvegarde annulée.");
                return;
            }
            
            manager.removePendingSave(player.getUniqueId());
            String json = manager.toJson(pending);
            BingoPlugin.getInstance().getDatabaseManager().savePreset(message, player.getUniqueId().toString(), json);
            
            player.sendMessage("§a[Bingo] §fSauvegarde '§e" + message + "§f' créée avec succès !");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
            
            Bukkit.getScheduler().runTask(BingoPlugin.getInstance(), () -> {
                player.openInventory(new fr.bingo.gui.PresetConfigGUI(player).getInventory());
            });
            return;
        }
        // --------------------------------

        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();
        BingoTeam team = teamManager.getPlayerTeam(player);

        if (message.startsWith("!")) {
            event.setFormat("§8[§7Global§8] " + (team != null ? team.getChatColor() : "§7") + "%1$s §8» §f%2$s");
            event.setMessage(message.substring(1));
        } else {
            if (team == null || team.getName().equals("Spectateur")) {
                event.setFormat("§8[§7Spectateur§8] §7%1$s §8» §f%2$s");
                return;
            }

            event.setCancelled(true);
            String teamPrefix;
            if (teamManager.isSoloMode()) {
                teamPrefix = "§8[" + team.getChatColor() + "Chat§8] ";
            } else {
                teamPrefix = "§8[" + team.getChatColor() + "Team " + team.getName() + "§8] ";
            }
            String formattedMessage = teamPrefix + team.getChatColor() + player.getName() + " §8» §f" + message;

            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(formattedMessage);
            }

            BingoTeam specTeam = teamManager.getSpectatorTeam();
            for (java.util.UUID uuid : specTeam.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage("§8[§cSpy§8] " + formattedMessage);
            }
        }
    }

    @EventHandler
    public void onCommandPreprocess(org.bukkit.event.player.PlayerCommandPreprocessEvent event) {
        if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.PLAYING) return;
        String msg = event.getMessage().toLowerCase();
        if (msg.startsWith("/msg ") || msg.startsWith("/tell ") || msg.startsWith("/w ") || msg.startsWith("/r ") || msg.startsWith("/whisper ")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cLes messages privés sont désactivés pendant le Bingo !");
        }
    }

    private String getPotionId(String base, String typeName) {
        String suffix = "_1";
        if (typeName.startsWith("STRONG_")) suffix = "_2";
        else if (typeName.startsWith("LONG_")) suffix = "_EXT";
        return "POTION_" + base + suffix;
    }

    /**
     * Tâche 4 : Découvre toutes les recettes du serveur pour un joueur.
     * Permet d'avoir le livre de recettes entièrement débloqué.
     */
    public static void discoverAllRecipes(Player player) {
        java.util.List<org.bukkit.NamespacedKey> keys = new java.util.ArrayList<>();
        Iterator<org.bukkit.inventory.Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            org.bukkit.inventory.Recipe recipe = it.next();
            if (recipe instanceof org.bukkit.Keyed keyed) {
                keys.add(keyed.getKey());
            }
        }
        if (!keys.isEmpty()) {
            player.discoverRecipes(keys);
        }
    }
}
