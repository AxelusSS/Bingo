package fr.hel.scenario;

import fr.hel.HelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SwitchInventoryScenario extends Scenario {

    private BukkitTask task;
    private final Random random = new Random();

    public SwitchInventoryScenario() {
        super("Switch Inventory", Material.BUNDLE, "\\u00C9change d'inventaire al\u00E9atoire entre 2 joueurs toutes les 10 min", false);
    }

    @Override
    public void onGameStart() {
        startTask();
    }

    @Override
    public void onEnable() {
        if (HelPlugin.getInstance().getHelGame().getState() == fr.hel.game.GameState.PLAYING) {
            startTask();
        }
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void startTask() {
        if (task != null) task.cancel();
        
        task = Bukkit.getScheduler().runTaskTimer(HelPlugin.getInstance(), () -> {
            List<Player> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                    players.add(p);
                }
            }
            
            if (players.size() >= 2) {
                Player p1 = players.remove(random.nextInt(players.size()));
                Player p2 = players.remove(random.nextInt(players.size()));
                
                // Swap inventory
                ItemStack[] inv1 = p1.getInventory().getContents();
                ItemStack[] armor1 = p1.getInventory().getArmorContents();
                ItemStack[] extra1 = p1.getInventory().getExtraContents();
                
                p1.getInventory().setContents(p2.getInventory().getContents());
                p1.getInventory().setArmorContents(p2.getInventory().getArmorContents());
                p1.getInventory().setExtraContents(p2.getInventory().getExtraContents());
                
                p2.getInventory().setContents(inv1);
                p2.getInventory().setArmorContents(armor1);
                p2.getInventory().setExtraContents(extra1);
                
                // Swap levels
                int level1 = p1.getLevel();
                float exp1 = p1.getExp();
                
                p1.setLevel(p2.getLevel());
                p1.setExp(p2.getExp());
                
                p2.setLevel(level1);
                p2.setExp(exp1);
                
                Bukkit.broadcastMessage("\u00A7d\u00A7lSWITCH INVENTORY ! \u00A7f" + p1.getName() + " \u00A77et \u00A7f" + p2.getName() + " \u00A77ont \u00E9chang\u00E9 leurs inventaires !");
            }
        }, 12000L, 12000L); // 10 minutes
    }
}
