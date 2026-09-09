package fr.hel.scenario;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CatEyesScenario extends Scenario {

    public CatEyesScenario() {
        super("Cat eyes", Material.GOLDEN_CARROT, "Donne l'effet night vision infini pendant toute la partie", true);
    }

    @Override
    public void onGameStart() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            // Dur\u00E9e infinie, amplificateur 0, particules cach\u00E9es
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
        }
    }
}
