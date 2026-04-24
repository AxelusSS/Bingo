package fr.bingo.scenario;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FriendlyCraftScenario extends Scenario {

    public FriendlyCraftScenario() {
        super("FriendlyCraft", Material.CRAFTING_TABLE, "Clic droit sur un allié pour ouvrir la table (Désactivé en FFA)", false);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (BingoPlugin.getInstance().getTeamManager().isSoloMode()) return;
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.CRAFTING_TABLE) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cLes tables de craft au sol sont désactivées ! Faites un clic droit sur un allié.");
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (BingoPlugin.getInstance().getTeamManager().isSoloMode()) return;
        
        if (event.getRightClicked() instanceof Player clickedPlayer) {
            Player player = event.getPlayer();
            BingoTeam team1 = BingoPlugin.getInstance().getTeamManager().getPlayerTeam(player);
            BingoTeam team2 = BingoPlugin.getInstance().getTeamManager().getPlayerTeam(clickedPlayer);
            
            if (team1 != null && team1.equals(team2)) {
                player.openWorkbench(null, true);
            }
        }
    }
}
