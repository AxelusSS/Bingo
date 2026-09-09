package fr.hel.scenario;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.EntityType;

import java.util.Random;

/**
 * Boost Loot : Am\u00E9liore les drops de mobs et blocs pour un gameplay UHC Run rapide.
 * - Canne \u00E0 sucre x3
 * - Cochons \u2192 cuir
 * - Moutons \u2192 biblioth\u00E8que + laine
 * - Araign\u00E9es \u2192 corde x3
 * - Drop de plumes augment\u00E9
 * - Gravier \u2192 silex garanti
 */
public class BoostLootScenario extends Scenario {

    private final Random random = new Random();

    public BoostLootScenario() {
        super("Boost Loot", Material.SUGAR_CANE, "Boost les drops de mobs et blocs", false);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Material type = event.getBlock().getType();

        switch (type) {
            case SUGAR_CANE -> {
                // Triple la canne \u00E0 sucre
                event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation(), new ItemStack(Material.SUGAR_CANE, 2));
            }
            case GRAVEL -> {
                // Gravier \u2192 toujours du silex
                event.setDropItems(false);
                event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation(), new ItemStack(Material.FLINT));
            }
            default -> {}
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        EntityType entityType = event.getEntityType();

        switch (entityType) {
            case PIG -> {
                // Cochons \u2192 cuir
                event.getDrops().add(new ItemStack(Material.LEATHER, 1 + random.nextInt(2)));
            }
            case SHEEP -> {
                // Moutons \u2192 biblioth\u00E8que
                event.getDrops().add(new ItemStack(Material.BOOKSHELF, 1));
            }
            case SPIDER -> {
                // Araign\u00E9es \u2192 corde x3
                for (ItemStack drop : event.getDrops()) {
                    if (drop.getType() == Material.STRING) {
                        drop.setAmount(drop.getAmount() * 3);
                    }
                }
            }
            case CHICKEN -> {
                // Plumes augment\u00E9es
                for (ItemStack drop : event.getDrops()) {
                    if (drop.getType() == Material.FEATHER) {
                        drop.setAmount(drop.getAmount() + 2);
                    }
                }
            }
            case ZOMBIE -> {
                // Zombies \u2192 fer parfois
                if (random.nextInt(4) == 0) {
                    event.getDrops().add(new ItemStack(Material.IRON_INGOT));
                }
            }
            case SKELETON -> {
                // Squelettes \u2192 os + fl\u00E8ches augment\u00E9es
                for (ItemStack drop : event.getDrops()) {
                    if (drop.getType() == Material.ARROW) {
                        drop.setAmount(drop.getAmount() + 4);
                    }
                    if (drop.getType() == Material.BONE) {
                        drop.setAmount(drop.getAmount() + 1);
                    }
                }
            }
            case WITCH -> {
                // Sorci\u00E8res \u2192 glowstone dust
                event.getDrops().add(new ItemStack(Material.GLOWSTONE_DUST, 2 + random.nextInt(4)));
            }
            default -> {}
        }
    }
}
