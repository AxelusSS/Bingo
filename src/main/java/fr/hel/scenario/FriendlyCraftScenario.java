package fr.hel.scenario;

import fr.hel.HelPlugin;
import fr.hel.team.HelTeam;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FriendlyCraftScenario extends Scenario {

    public FriendlyCraftScenario() {
        super("FriendlyCraft", Material.CRAFTING_TABLE, "Clic droit sur un alli\u00E9 pour ouvrir la table (D\u00E9sactiv\u00E9 en FFA)", false);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (HelPlugin.getInstance().getTeamManager().isSoloMode()) return;
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.CRAFTING_TABLE) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("\u00A7cLes tables de craft au sol sont d\u00E9sactiv\u00E9es ! Faites un clic droit sur un alli\u00E9.");
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (HelPlugin.getInstance().getTeamManager().isSoloMode()) return;
        
        if (event.getRightClicked() instanceof Player clickedPlayer) {
            Player player = event.getPlayer();
            HelTeam team1 = HelPlugin.getInstance().getTeamManager().getPlayerTeam(player);
            HelTeam team2 = HelPlugin.getInstance().getTeamManager().getPlayerTeam(clickedPlayer);
            
            if (team1 != null && team1.equals(team2)) {
                player.openWorkbench(null, true);
            }
        }
    }
}
