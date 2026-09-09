package fr.hel.scenario;

import fr.hel.HelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

public class FastSmeltingScenario extends Scenario {

    public FastSmeltingScenario() {
        super("Fast Smelting", Material.FURNACE, "Les fours cuisent 5 fois plus vite", false);
    }

    @EventHandler
    public void onFurnaceBurn(org.bukkit.event.inventory.FurnaceBurnEvent event) {
        if (event.getBlock().getState() instanceof Furnace furnace) {
            furnace.setCookSpeedMultiplier(5.0);
            furnace.update();
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.FURNACE 
            || event.getInventory().getType() == InventoryType.BLAST_FURNACE 
            || event.getInventory().getType() == InventoryType.SMOKER) {
            
            if (event.getInventory().getLocation() != null && event.getInventory().getLocation().getBlock().getState() instanceof Furnace furnace) {
                furnace.setCookSpeedMultiplier(5.0);
                furnace.update();
            }
        }
    }
}
