package fr.bingo.scenario;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.EntityType;

import java.util.Random;

/**
 * Boost Loot : Améliore les drops de mobs et blocs pour un gameplay UHC Run rapide.
 * - Canne à sucre x3
 * - Cochons → cuir
 * - Moutons → bibliothèque + laine
 * - Araignées → corde x3
 * - Drop de plumes augmenté
 * - Gravier → silex garanti
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
                // Triple la canne à sucre
                event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation(), new ItemStack(Material.SUGAR_CANE, 2));
            }
            case GRAVEL -> {
                // Gravier → toujours du silex
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
                // Cochons → cuir
                event.getDrops().add(new ItemStack(Material.LEATHER, 1 + random.nextInt(2)));
            }
            case SHEEP -> {
                // Moutons → bibliothèque
                event.getDrops().add(new ItemStack(Material.BOOKSHELF, 1));
            }
            case SPIDER -> {
                // Araignées → corde x3
                for (ItemStack drop : event.getDrops()) {
                    if (drop.getType() == Material.STRING) {
                        drop.setAmount(drop.getAmount() * 3);
                    }
                }
            }
            case CHICKEN -> {
                // Plumes augmentées
                for (ItemStack drop : event.getDrops()) {
                    if (drop.getType() == Material.FEATHER) {
                        drop.setAmount(drop.getAmount() + 2);
                    }
                }
            }
            case ZOMBIE -> {
                // Zombies → fer parfois
                if (random.nextInt(4) == 0) {
                    event.getDrops().add(new ItemStack(Material.IRON_INGOT));
                }
            }
            case SKELETON -> {
                // Squelettes → os + flèches augmentées
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
                // Sorcières → glowstone dust
                event.getDrops().add(new ItemStack(Material.GLOWSTONE_DUST, 2 + random.nextInt(4)));
            }
            default -> {}
        }
    }
}
