package fr.bingo.scenario;

import fr.bingo.BingoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

public class FastSmeltingScenario extends Scenario {

    public FastSmeltingScenario() {
        super("Fast Smelting", Material.FURNACE, "Les fours cuisent 5 fois plus vite", false);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.FURNACE 
            || event.getInventory().getType() == InventoryType.BLAST_FURNACE 
            || event.getInventory().getType() == InventoryType.SMOKER) {
            
            if (event.getInventory().getLocation() != null) {
                updateFurnace(event.getInventory().getLocation().getBlock().getState());
            }
        }
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getState() instanceof Furnace) {
                updateFurnace(event.getClickedBlock().getState());
            }
        }
    }

    private void updateFurnace(org.bukkit.block.BlockState state) {
        if (state instanceof Furnace furnace) {
            Bukkit.getScheduler().runTask(BingoPlugin.getInstance(), () -> {
                furnace.setCookTimeTotal((short) 40); // 40 ticks = 2s, default is 200 ticks = 10s
                furnace.update();
            });
        }
    }
}
