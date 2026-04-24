package fr.bingo.scenario;

import fr.bingo.BingoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SuperHeroScenario extends Scenario {

    private boolean vampireMode = false;
    private final Map<PotionEffectType, Boolean> allowedEffects = new HashMap<>();
    private final Map<UUID, PotionEffectType> playerEffects = new HashMap<>();

    public SuperHeroScenario() {
        super("Super Hero", Material.TOTEM_OF_UNDYING, "Effets aléatoires au lancement (Clic droit pour config)", false);
        
        allowedEffects.put(PotionEffectType.STRENGTH, true);
        allowedEffects.put(PotionEffectType.RESISTANCE, true);
        allowedEffects.put(PotionEffectType.HEALTH_BOOST, true);
        allowedEffects.put(PotionEffectType.SPEED, true);
        allowedEffects.put(PotionEffectType.JUMP_BOOST, true);
    }

    @Override
    public boolean hasConfigMenu() {
        return true;
    }

    @Override
    public void onRightClick(Player player) {
        player.openInventory(new fr.bingo.gui.SuperHeroConfigGUI(this).getInventory());
    }

    @Override
    public void onGameStart() {
        playerEffects.clear();
        List<PotionEffectType> pool = getActiveEffectsPool();
        if (pool.isEmpty()) return;

        Random rand = new Random();
        for (Player p : Bukkit.getOnlinePlayers()) {
            PotionEffectType type = pool.get(rand.nextInt(pool.size()));
            applyEffect(p, type);
            playerEffects.put(p.getUniqueId(), type);
            
            String name = type.getName();
            if (type == PotionEffectType.STRENGTH) name = "Force";
            else if (type == PotionEffectType.RESISTANCE) name = "Résistance";
            else if (type == PotionEffectType.HEALTH_BOOST) name = "Double Vie";
            else if (type == PotionEffectType.SPEED) name = "Vitesse";
            else if (type == PotionEffectType.JUMP_BOOST) name = "Saut Amélioré";
            
            p.sendMessage("§e[SuperHero] §aVous avez reçu le pouvoir : §e" + name + " !");
        }
    }

    private void applyEffect(Player p, PotionEffectType type) {
        p.removePotionEffect(PotionEffectType.STRENGTH);
        p.removePotionEffect(PotionEffectType.RESISTANCE);
        p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
        p.removePotionEffect(PotionEffectType.SPEED);
        p.removePotionEffect(PotionEffectType.JUMP_BOOST);
        
        if (type == PotionEffectType.HEALTH_BOOST) {
            p.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, 4, false, false, false)); // +10 hearts
        } else {
            p.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, 0, false, false, false));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!vampireMode) return;
        
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer != null && playerEffects.containsKey(victim.getUniqueId())) {
            PotionEffectType stolen = playerEffects.get(victim.getUniqueId());
            applyEffect(killer, stolen);
            playerEffects.put(killer.getUniqueId(), stolen);
            
            String name = stolen.getName();
            if (stolen == PotionEffectType.STRENGTH) name = "Force";
            else if (stolen == PotionEffectType.RESISTANCE) name = "Résistance";
            else if (stolen == PotionEffectType.HEALTH_BOOST) name = "Double Vie";
            else if (stolen == PotionEffectType.SPEED) name = "Vitesse";
            else if (stolen == PotionEffectType.JUMP_BOOST) name = "Saut Amélioré";
            
            killer.sendMessage("§c[Vampire] §aVous avez volé le pouvoir de §e" + victim.getName() + " §a: §e" + name);
        }
    }

    public boolean isVampireMode() { return vampireMode; }
    public void setVampireMode(boolean vampireMode) { this.vampireMode = vampireMode; }
    public Map<PotionEffectType, Boolean> getAllowedEffects() { return allowedEffects; }
    
    private List<PotionEffectType> getActiveEffectsPool() {
        List<PotionEffectType> pool = new ArrayList<>();
        for (Map.Entry<PotionEffectType, Boolean> entry : allowedEffects.entrySet()) {
            if (entry.getValue()) pool.add(entry.getKey());
        }
        return pool;
    }
}
