package fr.bingo.listeners;

import fr.bingo.BingoPlugin;
import fr.bingo.game.*;
import fr.bingo.gui.AdminConfigGUI;
import fr.bingo.gui.TeamSelectorGUI;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.*;
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
            teamManager.giveTeamBanners(player);

            // Donner le compas admin
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
                    net.md_5.bungee.api.chat.TextComponent.fromLegacy("§b§l⚑ Choisis ton équipe dans l'inventaire ⚑")
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
            gui.handleClick(player, event.getRawSlot());
            return;
        }

        // GUI Team Selector
        if (event.getInventory().getHolder() instanceof TeamSelectorGUI) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();
            if (teamManager.isTeamsLocked() && !player.hasPermission("bingo.admin")) {
                player.sendMessage("§cLes équipes sont verrouillées !");
                player.closeInventory();
                return;
            }
            Material clickedMat = event.getCurrentItem().getType();
            for (BingoTeam team : teamManager.getTeams()) {
                if (team.getBannerMaterial() == clickedMat) {
                    if (teamManager.joinTeam(player, team)) {
                        player.openInventory(new TeamSelectorGUI().getInventory());
                    }
                    return;
                }
            }
        }

        // Clic bannière PDC
        if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.WAITING) return;
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        org.bukkit.persistence.PersistentDataContainer pdc = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "team_banner");
        if (!pdc.has(key, org.bukkit.persistence.PersistentDataType.STRING)) return;

        event.setCancelled(true);
        String teamName = pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();

        if (teamManager.isTeamsLocked() && !player.hasPermission("bingo.admin")) {
            player.sendMessage("§cLes équipes sont verrouillées !");
            return;
        }

        for (BingoTeam team : teamManager.getTeams()) {
            if (team.getName().equalsIgnoreCase(teamName)) {
                teamManager.joinTeam(player, team);
                teamManager.giveTeamBanners(player);
                return;
            }
        }
    }

    // ── Clic droit compas admin ──

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() == null || !event.getItem().hasItemMeta()) return;
        if (!event.getAction().name().contains("RIGHT")) return;

        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "admin_compass");
        if (event.getItem().getItemMeta().getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
            if (!player.hasPermission("bingo.admin")) {
                player.sendMessage("§cPermission refusée.");
                return;
            }
            player.openInventory(new AdminConfigGUI().getInventory());
        }
    }

    // ── Protection gameplay ──

    @EventHandler
    public void onItemDrop(org.bukkit.event.player.PlayerDropItemEvent event) {
        if (BingoPlugin.getInstance().getBingoGame().getState() == GameState.WAITING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.PLAYING) {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                event.setCancelled(true);
            }
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

                    // Refresh la grille : item trouvé → étoile ★
                    new DatapackManager().refreshFoundItems(grid);

                    String displayName = obj.getId().replace("_", " ");
                    String prefix = obj.isAchievement() ? "§d[Achievement] " : "";
                    player.getServer().broadcastMessage("§8[§6Bingo§8] " + team.getChatColor() + "L'équipe " + team.getName() + " §aa trouvé " + prefix + "§e" + displayName + " §a!");

                    // Sons & effets
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
        // Son pour toute l'équipe
        for (java.util.UUID uuid : team.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
                // Particules autour du joueur qui a trouvé
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

            long elapsed = BingoPlugin.getInstance().getBingoGame().getElapsedSeconds();
            int min = (int) (elapsed / 60);
            int sec = (int) (elapsed % 60);
            String timeStr = String.format("%02d:%02d", min, sec);

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§8§m                                                §r");
            Bukkit.broadcastMessage("  " + team.getChatColor() + "§l★ L'équipe " + team.getName() + " a terminé le Bingo ! ★");
            Bukkit.broadcastMessage("  §7Temps : §e§l" + timeStr);
            Bukkit.broadcastMessage("§8§m                                                §r");
            Bukkit.broadcastMessage("");

            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.setGameMode(GameMode.SPECTATOR);
                    p.sendTitle(team.getChatColor() + "§lBINGO !", "§7Temps : §e" + timeStr, 10, 60, 20);
                    // Feu d'artifice !
                    launchFirework(p.getLocation());
                }
            }

            checkAllTeamsFinished();
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

    private void checkAllTeamsFinished() {
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        boolean allDone = true;

        for (BingoTeam t : tm.getTeams()) {
            if (t.getName().equals("Spectateur")) continue;
            if (t.getPlayers().isEmpty()) continue;
            if (!t.isFinished()) { allDone = false; break; }
        }

        if (allDone) {
            Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage("§6§l✦✦✦ TOUTES LES ÉQUIPES ONT TERMINÉ ! ✦✦✦");
                Bukkit.broadcastMessage("§7La partie est terminée. Merci d'avoir joué !");
                Bukkit.broadcastMessage("");
                BingoPlugin.getInstance().getBingoGame().setState(GameState.WAITING);
            }, 60L);
        }
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
        // Ignorer les recettes et les advancements bingo auto-générés
        if (key.contains("recipes/")) return;
        if (event.getAdvancement().getKey().getNamespace().equals("bingoclassique")) return;
        // Vérifier si c'est un objectif achievement du bingo
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
            String teamPrefix = "§8[" + team.getChatColor() + "Team " + team.getName() + "§8] ";
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
