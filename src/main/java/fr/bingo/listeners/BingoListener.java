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
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

public class BingoListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();

        if (teamManager.getPlayerTeam(player) == null) {
            teamManager.joinTeam(player, teamManager.getSpectatorTeam());
        }

        if (BingoPlugin.getInstance().getBingoGame().getState() == GameState.WAITING) {
            BingoPlugin.getInstance().getBingoGame().teleportToWaitingArea(player);

            // Bannière de sélection d'équipe (slot 4, centre hotbar)
            teamManager.giveTeamBanner(player);

            // Compas admin
            if (player.hasPermission("bingo.admin")) {
                BingoGame.giveAdminCompass(player);
            }

            startActionBarReminder(player);
        }
    }

    private void startActionBarReminder(Player player) {
        Bukkit.getScheduler().runTaskTimer(BingoPlugin.getInstance(), () -> {
            if (!player.isOnline()) return;
            if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.WAITING) return;

            TeamManager tm = BingoPlugin.getInstance().getTeamManager();
            BingoTeam team = tm.getPlayerTeam(player);

            if (team == null || team.getName().equals("Spectateur")) {
                player.spigot().sendMessage(
                    net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacy("§b§l⚑ Clique sur la bannière pour choisir ton équipe ⚑")
                );
            }
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

        // Bannière de sélection d'équipe
        org.bukkit.NamespacedKey teamKey = new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "team_selector");
        if (pdc.has(teamKey, org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
            if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.WAITING) return;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            player.openInventory(new TeamSelectorGUI().getInventory());
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

                    new DatapackManager().refreshFoundItems(grid);

                    String displayName = obj.getId().replace("_", " ");
                    String prefix = obj.isAchievement() ? "§d[Achievement] " : "";

                    // En FFA : afficher le pseudo du joueur, sinon le nom d'équipe
                    if (teamManager.isSoloMode()) {
                        player.getServer().broadcastMessage("§8[§6Bingo§8] " + team.getChatColor() + player.getName() + " §aa trouvé " + prefix + "§e" + displayName + " §a!");
                    } else {
                        player.getServer().broadcastMessage("§8[§6Bingo§8] " + team.getChatColor() + "L'équipe " + team.getName() + " §aa trouvé " + prefix + "§e" + displayName + " §a!");
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

    private void checkEndCondition() {
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
        }
    }

    private void triggerGameEnd() {
        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§6§l✦✦✦ PARTIE TERMINÉE ! ✦✦✦");
            Bukkit.broadcastMessage("§7Merci d'avoir joué !");
            Bukkit.broadcastMessage("");

            displayRanking();

            BingoPlugin.getInstance().getBingoGame().setState(GameState.WAITING);
        }, 60L);
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
            checkObjective(player, event.getItem().getItemStack().getType().name());
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            ItemStack result = event.getCurrentItem();
            if (result != null && result.getType() != Material.AIR) {
                checkObjective(player, result.getType().name());
            }
        }
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
}
