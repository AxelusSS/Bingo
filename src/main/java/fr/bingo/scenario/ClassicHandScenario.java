package fr.bingo.scenario;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ClassicHandScenario extends Scenario {

    public ClassicHandScenario() {
        super("ClassicHand", Material.SHIELD, "Désactive la deuxième main (offhand)", false);
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getRawSlot() == 45) { // Slot offhand
            event.setCancelled(true);
        }
    }
}
