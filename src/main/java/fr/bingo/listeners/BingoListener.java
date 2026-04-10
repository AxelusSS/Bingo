package fr.bingo.listeners;

import fr.bingo.BingoPlugin;
import fr.bingo.game.GameState;
import fr.bingo.gui.TeamSelectorGUI;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;
import fr.bingo.game.BingoObjective;
import fr.bingo.game.BingoGrid;
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
            player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacy("§b§lChoisis ton équipe dans l'inventaire")
            );
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // ── GUI Team Selector (ancien /team menu) ──
        if (event.getInventory().getHolder() instanceof fr.bingo.gui.BingoGridGUI) {
            event.setCancelled(true);
            return;
        }
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

        // ── Clic sur bannière PDC dans l'inventaire du joueur ──
        if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.WAITING) return;
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        org.bukkit.persistence.PersistentDataContainer pdc = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "team_banner");
        if (!pdc.has(key, org.bukkit.persistence.PersistentDataType.STRING)) return;

        event.setCancelled(true); // Empêcher de déplacer la bannière

        String teamName = pdc.get(key, org.bukkit.persistence.PersistentDataType.STRING);
        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();

        if (teamManager.isTeamsLocked() && !player.hasPermission("bingo.admin")) {
            player.sendMessage("§cLes équipes sont verrouillées !");
            return;
        }

        for (BingoTeam team : teamManager.getTeams()) {
            if (team.getName().equalsIgnoreCase(teamName)) {
                teamManager.joinTeam(player, team);
                // Rafraîchir les bannières (mise à jour du nombre de membres)
                teamManager.giveTeamBanners(player);
                return;
            }
        }
    }

    // ── Empêcher les joueurs en WAITING de drop/déplacer les bannières ──
    @EventHandler
    public void onItemDrop(org.bukkit.event.player.PlayerDropItemEvent event) {
        if (BingoPlugin.getInstance().getBingoGame().getState() == GameState.WAITING) {
            event.setCancelled(true);
        }
    }

    // ── Pas de PvP tant que la partie n'est pas lancée ──
    @EventHandler
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.PLAYING) {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                event.setCancelled(true);
            }
        }
    }

    // ── Pas de faim / pas de mort de faim ──
    @EventHandler
    public void onFoodLevelChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        // Empêcher la perte de faim (maintenir toujours à 20)
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
            ((Player) event.getEntity()).setFoodLevel(20);
            ((Player) event.getEntity()).setSaturation(20f);
        }
    }

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

                    // Sync Datapack — utiliser l'ID positionnel (r0c0, r0c1, etc.)
                    String advId = fr.bingo.game.DatapackManager.getAdvancementIdFromIndex(i, grid.getSize());
                    org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("bingoclassique", advId);
                    org.bukkit.advancement.Advancement adv = org.bukkit.Bukkit.getAdvancement(key);
                    if (adv != null) {
                        for (java.util.UUID uuid : team.getPlayers()) {
                            org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(uuid);
                            if (p != null) {
                                p.getAdvancementProgress(adv).awardCriteria("impossible");
                            }
                        }
                    }

                    player.getServer().broadcastMessage("§8[§6Bingo§8] " + team.getChatColor() + "L'équipe " + team.getName() + " §aa trouvé §e" + obj.getId().replace("_", " ") + " §a!");

                    int seconds = (int) BingoPlugin.getInstance().getBingoGame().getElapsedSeconds();
                    BingoPlugin.getInstance().getDatabaseManager().recordStat(player.getName(), seconds, objectiveId);
                }
                break;
            }
        }
    }

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
        checkObjective(event.getPlayer(), key);
    }

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
                Player p = org.bukkit.Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(formattedMessage);
            }

            BingoTeam specTeam = teamManager.getSpectatorTeam();
            for (java.util.UUID uuid : specTeam.getPlayers()) {
                Player p = org.bukkit.Bukkit.getPlayer(uuid);
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
