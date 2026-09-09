package fr.hel.scenario;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class KeepInventoryScenario extends Scenario {

    public KeepInventoryScenario() {
        super("KeepInventory", Material.ENDER_CHEST, "Garde l'inventaire \u00E0 la mort", true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
        event.setDroppedExp(0);
    }
}
