package fr.hel.scenario;

import fr.hel.HelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;

public class EternalDayScenario extends Scenario {

    public EternalDayScenario() {
        super("Eternal Day", Material.SUNFLOWER, "Le soleil reste bloqu\u00E9 \u00E0 midi.", true);
    }

    @Override
    public void onGameStart() {
        World world = Bukkit.getWorlds().get(0);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(6000);
    }
}
