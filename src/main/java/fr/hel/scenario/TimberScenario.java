package fr.hel.scenario;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TimberScenario extends Scenario {

    public TimberScenario() {
        super("Timber", Material.IRON_AXE, "Les arbres se cassent en un coup de hache", false);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item == null || !item.getType().name().endsWith("_AXE")) return;
        
        Block block = event.getBlock();
        if (!isLog(block.getType())) return;
        
        breakTree(block);
    }
    
    private boolean isLog(Material mat) {
        return mat.name().endsWith("_LOG") || mat.name().endsWith("_WOOD");
    }
    
    private void breakTree(Block startBlock) {
        Set<Block> logs = new HashSet<>();
        Queue<Block> toCheck = new LinkedList<>();
        
        toCheck.add(startBlock);
        
        int count = 0;
        int maxLogs = 200; // S\u00E9curit\u00E9 pour \u00E9viter de crash sur de trop gros arbres
        
        while (!toCheck.isEmpty() && count < maxLogs) {
            Block b = toCheck.poll();
            if (logs.add(b)) {
                count++;
                
                if (!b.equals(startBlock)) {
                    b.breakNaturally();
                }
                
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            Block neighbor = b.getRelative(x, y, z);
                            if (isLog(neighbor.getType()) && !logs.contains(neighbor)) {
                                toCheck.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
    }
}
