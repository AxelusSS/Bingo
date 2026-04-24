package fr.bingo.scenario;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import java.util.HashMap;
import java.util.Map;

public class TeamInventoryScenario extends Scenario {

    private final Map<BingoTeam, Inventory> teamInventories = new HashMap<>();

    public TeamInventoryScenario() {
        super("TeamInventory", Material.CHEST, "Commande /ti pour accéder à un inventaire d'équipe partagé", false);
    }

    @Override
    public void onGameStart() {
        teamInventories.clear();
        for (BingoTeam team : BingoPlugin.getInstance().getTeamManager().getTeams()) {
            teamInventories.put(team, Bukkit.createInventory(null, 27, "§8Inventaire d'Équipe - " + team.getName()));
        }
        // Pour les équipes générées en FFA
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/ti") || event.getMessage().toLowerCase().startsWith("/ti ")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            
            if (BingoPlugin.getInstance().getBingoGame().getState() != fr.bingo.game.GameState.PLAYING) {
                player.sendMessage("§cLa partie n'a pas encore commencé.");
                return;
            }
            
            BingoTeam team = BingoPlugin.getInstance().getTeamManager().getPlayerTeam(player);
            if (team == null || team.getName().equals("Spectateur")) {
                player.sendMessage("§cVous n'êtes pas dans une équipe valide pour cela.");
                return;
            }
            
            Inventory inv = teamInventories.get(team);
            if (inv == null) {
                inv = Bukkit.createInventory(null, 27, "§8Inventaire d'Équipe");
                teamInventories.put(team, inv);
            }
            
            player.openInventory(inv);
        }
    }
}
