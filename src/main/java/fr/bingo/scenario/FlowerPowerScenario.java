package fr.bingo.scenario;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Random;

public class FlowerPowerScenario extends Scenario {

    private final Material[] flowers = {
        Material.DANDELION, Material.POPPY, Material.BLUE_ORCHID, Material.ALLIUM,
        Material.AZURE_BLUET, Material.RED_TULIP, Material.ORANGE_TULIP,
        Material.WHITE_TULIP, Material.PINK_TULIP, Material.OXEYE_DAISY,
        Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY, Material.SUNFLOWER,
        Material.LILAC, Material.ROSE_BUSH, Material.PEONY
    };
    
    // Quelques loots random
    private final Material[] drops = {
        Material.DIAMOND, Material.GOLD_INGOT, Material.IRON_INGOT, Material.EMERALD,
        Material.APPLE, Material.GOLDEN_APPLE, Material.BONE, Material.STRING,
        Material.FEATHER, Material.LEATHER, Material.COAL, Material.LAPIS_LAZULI
    };
    
    private final Random random = new Random();

    public FlowerPowerScenario() {
        super("FlowerPower", Material.POPPY, "Les fleurs drop des items random", false);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Material type = event.getBlock().getType();
        for (Material flower : flowers) {
            if (type == flower) {
                event.setDropItems(false);
                Material randomDrop = drops[random.nextInt(drops.length)];
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(randomDrop));
                break;
            }
        }
    }
}
