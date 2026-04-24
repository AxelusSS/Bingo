package fr.bingo.scenario;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class NoFallDamageScenario extends Scenario {

    public NoFallDamageScenario() {
        super("No Fall Damage", Material.FEATHER, "Désactive les dégâts de chute", false);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }
}
