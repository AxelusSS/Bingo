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
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!isEnabled()) {
                    this.cancel();
                    return;
                }
                for (Player p : Bukkit.getOnlinePlayers()) {
                    // Durée de 10 secondes (200 ticks), amplificateur 0
                    p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0, false, false, false));
                }
            }
        }.runTaskTimer(fr.hel.HelPlugin.getInstance(), 0L, 100L); // Toutes les 5 secondes
    }
}
