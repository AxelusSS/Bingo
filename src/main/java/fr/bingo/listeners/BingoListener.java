package fr.bingo.listeners;

import fr.bingo.BingoPlugin;
import fr.bingo.game.GameState;
import fr.bingo.gui.TeamSelectorGUI;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
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
            // Donner les bannières de sélection d'équipe (clear l'inv d'abord)
            teamManager.giveTeamBanners(player);
            player.sendMessage("§e§lBienvenue ! §r§eClic droit sur une bannière pour rejoindre une équipe !");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        if (event.getInventory().getHolder() instanceof fr.bingo.gui.BingoGridGUI) {
            event.setCancelled(true);
            return;
        }

        if (event.getInventory().getHolder() instanceof TeamSelectorGUI) {
            event.setCancelled(true); // Bloque le transfert d'item
            
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
                        // Re-open fresh UI to correctly show updated lore cache
                        player.openInventory(new TeamSelectorGUI().getInventory());
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Seulement en WAITING, seulement clic droit
        if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.WAITING) return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
            && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        
        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;
        
        // Vérifier si c'est une bannière de sélection via le tag PDC
        org.bukkit.persistence.PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
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
                // Rafraîchir les bannières pour mettre à jour les membres affichés
                teamManager.giveTeamBanners(player);
                return;
            }
        }
    }

    private void checkObjective(Player player, String objectiveId) {
        if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.PLAYING) return;
        
        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();
        BingoTeam team = teamManager.getPlayerTeam(player);
        if (team == null || team.getName().equals("Spectateur")) return;

        List<BingoObjective> grid = BingoPlugin.getInstance().getBingoGame().getGrid().getObjectives();
        for (BingoObjective obj : grid) {
            if (obj.getId().equalsIgnoreCase(objectiveId)) {
                if (!team.hasUnlocked(objectiveId)) {
                    team.unlockObjective(objectiveId, BingoPlugin.getInstance().getBingoGame().getGrid().getSize());
                    
                    // Sync Datapack Enhancement pour l'équipe (L'advancement devient jaune)
                    org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("bingoclassique", obj.getId().toLowerCase());
                    org.bukkit.advancement.Advancement adv = org.bukkit.Bukkit.getAdvancement(key);
                    if (adv != null) {
                        for (java.util.UUID uuid : team.getPlayers()) {
                            org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(uuid);
                            if (p != null) {
                                p.getAdvancementProgress(adv).awardCriteria("impossible");
                            }
                        }
                    }

                    // Annonce
                    player.getServer().broadcastMessage("§8[§6Bingo§8] " + team.getChatColor() + "L'équipe " + team.getName() + " §aa trouvé §e" + obj.getId().replace("_", " ") + " §a!");
                    
                    // DB Save
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
        // Enlève l'affichage de base si le gamerule correspond, ou ignore-les si ce ne sont pas des advancements Vanilla (ex: recettes)
        String key = event.getAdvancement().getKey().getKey();
        if (key.contains("recipes/")) return; // Ne pas compter le débloquage de recettes
        checkObjective(event.getPlayer(), key);
    }
    
    @EventHandler
    public void onPlayerChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();
        BingoTeam team = teamManager.getPlayerTeam(player);
        
        if (message.startsWith("!")) {
            // Chat global
            event.setFormat("§8[§7Global§8] " + (team != null ? team.getChatColor() : "§7") + "%1$s §8» §f%2$s");
            event.setMessage(message.substring(1));
        } else {
            // Chat d'équipe
            if (team == null || team.getName().equals("Spectateur")) {
                // S'ils n'ont pas d'équipe, on force le global
                event.setFormat("§8[§7Spectateur§8] §7%1$s §8» §f%2$s");
                return;
            }
            
            event.setCancelled(true);
            String teamPrefix = "§8[" + team.getChatColor() + "Team " + team.getName() + "§8] ";
            String formattedMessage = teamPrefix + team.getChatColor() + player.getName() + " §8» §f" + message;
            
            // Envoyer à l'équipe
            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = org.bukkit.Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(formattedMessage);
            }
            
            // Envoyer aux spectateurs (Admins)
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
