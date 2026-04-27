package fr.bingo.scenario;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PvP18Scenario extends Scenario {

    public PvP18Scenario() {
        super("PvP 1.8", Material.IRON_SWORD, "Supprime le délai d'attaque pour un PvP type 1.8", false);
    }

    @Override
    public void onGameStart() {
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            apply(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (isEnabled()) {
            apply(event.getPlayer());
        }
    }

    private void apply(Player p) {
        var attr = p.getAttribute(Attribute.ATTACK_SPEED);
        if (attr != null) {
            attr.setBaseValue(100.0);
        }
    }

    @Override
    public void onDisable() {
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            var attr = p.getAttribute(Attribute.ATTACK_SPEED);
            if (attr != null) {
                attr.setBaseValue(4.0); // Valeur par défaut
            }
        }
    }
}
