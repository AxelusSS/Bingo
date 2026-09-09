package fr.hel.scenario;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class GraveScenario extends Scenario {

    public GraveScenario() {
        super("Grave", Material.SKELETON_SKULL, "\u00C0 la mort, le stuff est plac\u00E9 dans un coffre prot\u00E9g\u00E9", false);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getDrops().isEmpty()) return;
        
        Location loc = event.getEntity().getLocation();
        Block block = loc.getBlock();
        
        // Protection lave / eau
        if (block.getType() == Material.LAVA || block.getType() == Material.WATER) {
            while (block.getType() == Material.LAVA || block.getType() == Material.WATER) {
                if (block.getY() > 310) break;
                block = block.getRelative(0, 1, 0);
            }
            Block under = block.getRelative(0, -1, 0);
            if (under.getType() == Material.LAVA || under.getType() == Material.WATER || under.getType() == Material.AIR) {
                under.setType(Material.GLASS);
            }
        }
        
        block.setType(Material.CHEST);
        if (block.getState() instanceof Chest chest) {
            for (ItemStack item : event.getDrops()) {
                if (item != null && item.getType() != Material.AIR) {
                    chest.getInventory().addItem(item);
                }
            }
            event.getDrops().clear();
        }
    }
}
