package fr.bingo.scenario;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class NoFoodScenario extends Scenario {

    public NoFoodScenario() {
        super("NoFood", Material.COOKED_BEEF, "Saturation infinie pendant toute la partie", false);
    }

    @Override
    public void onGameStart() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setFoodLevel(20);
            p.setSaturation(20f);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20f);
        }
    }
}
