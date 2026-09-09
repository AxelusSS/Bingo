package fr.hel.scenario;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Triple Ores : Les minerais droppent 3x plus.
 */
public class TripleOresScenario extends Scenario {

    public TripleOresScenario() {
        super("Triple Ores", Material.GOLD_ORE, "Les minerais droppent 3x plus", false);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Material type = event.getBlock().getType();

        Material drop = null;
        switch (type) {
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> drop = Material.DIAMOND;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> drop = Material.EMERALD;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> drop = Material.LAPIS_LAZULI;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> drop = Material.REDSTONE;
            case COAL_ORE, DEEPSLATE_COAL_ORE -> drop = Material.COAL;
            case NETHER_GOLD_ORE -> drop = Material.GOLD_NUGGET;
            case NETHER_QUARTZ_ORE -> drop = Material.QUARTZ;
            // Iron/Gold/Copper g\u00E9r\u00E9 par CutClean si activ\u00E9, sinon raw
            case IRON_ORE, DEEPSLATE_IRON_ORE -> drop = Material.RAW_IRON;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> drop = Material.RAW_GOLD;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> drop = Material.RAW_COPPER;
            default -> {}
        }

        if (drop != null) {
            // Ajouter 2x la quantit\u00E9 suppl\u00E9mentaire (total = 3x)
            event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(), new ItemStack(drop, 2));
        }
    }
}
