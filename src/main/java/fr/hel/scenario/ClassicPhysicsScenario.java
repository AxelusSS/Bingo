package fr.hel.scenario;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class ClassicPhysicsScenario extends Scenario {

    public ClassicPhysicsScenario() {
        super("Classic Physique", Material.WATER_BUCKET, "D\u00E9sactive la nage (crawl) et autres physiques modernes", false);
    }

    @EventHandler
    public void onSwim(EntityToggleSwimEvent event) {
        if (event.isSwimming()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(org.bukkit.event.player.PlayerMoveEvent event) {
        if (event.getPlayer().isSwimming()) {
            event.getPlayer().setSwimming(false);
        }
    }
}
