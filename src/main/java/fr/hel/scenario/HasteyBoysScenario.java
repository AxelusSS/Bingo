package fr.hel.scenario;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HasteyBoysScenario extends Scenario {

    public HasteyBoysScenario() {
        super("Hastey Boys", Material.DIAMOND_PICKAXE, "Les outils craft\u00E9s sont directement enchant\u00E9s", false);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;
        
        String name = item.getType().name();
        if (name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE")) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.EFFICIENCY, 3, true);
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                item.setItemMeta(meta);
            }
        }
    }
}
