package fr.hel.scenario;

import fr.hel.HelPlugin;
import fr.hel.team.HelTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import java.util.HashMap;
import java.util.Map;

public class TeamInventoryScenario extends Scenario {

    private final Map<HelTeam, Inventory> teamInventories = new HashMap<>();

    public TeamInventoryScenario() {
        super("TeamInventory", Material.CHEST, "Commande /ti pour acc\u00E9der \u00E0 un inventaire d'\u00E9quipe partag\u00E9", false);
    }

    @Override
    public void onGameStart() {
        teamInventories.clear();
        for (HelTeam team : HelPlugin.getInstance().getTeamManager().getTeams()) {
            teamInventories.put(team, Bukkit.createInventory(null, 27, "\u00A78Inventaire d'\u00C9quipe - " + team.getName()));
        }
        // Pour les \u00E9quipes g\u00E9n\u00E9r\u00E9es en FFA
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/ti") || event.getMessage().toLowerCase().startsWith("/ti ")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            
            if (HelPlugin.getInstance().getHelGame().getState() != fr.hel.game.GameState.PLAYING) {
                player.sendMessage("\u00A7cLa partie n'a pas encore commenc\u00E9.");
                return;
            }
            
            HelTeam team = HelPlugin.getInstance().getTeamManager().getPlayerTeam(player);
            if (team == null || team.getName().equals("Spectateur")) {
                player.sendMessage("\u00A7cVous n'\u00EAtes pas dans une \u00E9quipe valide pour cela.");
                return;
            }
            
            Inventory inv = teamInventories.get(team);
            if (inv == null) {
                inv = Bukkit.createInventory(null, 27, "\u00A78Inventaire d'\u00C9quipe");
                teamInventories.put(team, inv);
            }
            
            player.openInventory(inv);
        }
    }
}
