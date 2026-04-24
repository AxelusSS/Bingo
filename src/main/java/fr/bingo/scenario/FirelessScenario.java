package fr.bingo.scenario;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class FirelessScenario extends Scenario {

    public FirelessScenario() {
        super("Fireless", Material.MAGMA_CREAM, "Désactive les dégâts liés au feu et à la lave", false);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (cause == EntityDamageEvent.DamageCause.FIRE 
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK 
                || cause == EntityDamageEvent.DamageCause.LAVA 
                || cause == EntityDamageEvent.DamageCause.HOT_FLOOR) {
                event.setCancelled(true);
            }
        }
    }
}
