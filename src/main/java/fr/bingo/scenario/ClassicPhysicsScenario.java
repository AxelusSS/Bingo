package fr.bingo.scenario;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class ClassicPhysicsScenario extends Scenario {

    public ClassicPhysicsScenario() {
        super("Classic Physique", Material.WATER_BUCKET, "Désactive la nage (crawl) et autres physiques modernes", false);
    }

    @EventHandler
    public void onSwim(EntityToggleSwimEvent event) {
        if (event.isSwimming()) {
            event.setCancelled(true);
        }
    }
    
    // Note: Crawling is often triggered by trapdoors/etc. 
    // In 1.21, we can't easily disable it entirely without complex packet manipulation,
    // but we can try to cancel swimming which is the main "new" physics.
}
