package fr.hel.scenario;

import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class CutCleanScenario extends Scenario {

    public CutCleanScenario() {
        super("CutClean", Material.IRON_PICKAXE, "Les minerais et la nourriture sont cuits automatiquement", false);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Material type = event.getBlock().getType();
        
        Material drop = null;
        int xp = 0;
        
        switch (type) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> { drop = Material.IRON_INGOT; xp = 2; }
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> { drop = Material.GOLD_INGOT; xp = 3; }
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> { drop = Material.COPPER_INGOT; xp = 2; }
            default -> {}
        }
        
        if (drop != null) {
            event.setDropItems(false);
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(drop));
            if (xp > 0) {
                event.getBlock().getWorld().spawn(event.getBlock().getLocation(), ExperienceOrb.class).setExperience(xp);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        for (ItemStack drop : event.getDrops()) {
            switch (drop.getType()) {
                case BEEF -> drop.setType(Material.COOKED_BEEF);
                case PORKCHOP -> drop.setType(Material.COOKED_PORKCHOP);
                case CHICKEN -> drop.setType(Material.COOKED_CHICKEN);
                case MUTTON -> drop.setType(Material.COOKED_MUTTON);
                case RABBIT -> drop.setType(Material.COOKED_RABBIT);
                case SALMON -> drop.setType(Material.COOKED_SALMON);
                case COD -> drop.setType(Material.COOKED_COD);
                case POTATO -> drop.setType(Material.BAKED_POTATO);
                default -> {}
            }
        }
    }
}
