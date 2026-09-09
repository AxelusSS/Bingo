package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.scenario.Scenario;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ScenarioConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public ScenarioConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 54, "\u00A76\u00A7lSc\u00E9narios");
        populate();
    }

    public void populate() {
        inventory.clear();

        List<Scenario> scenarios = HelPlugin.getInstance().getScenarioManager().getScenarios();
        
        int slot = 0;
        for (Scenario scenario : scenarios) {
            if (slot >= 45) break; 
            
            ItemStack item = new ItemStack(scenario.getIcon());
            ItemMeta meta = item.getItemMeta();
            
            if (scenario.isEnabled()) {
                meta.setDisplayName("\u00A7a\u00A7l" + scenario.getName());
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                meta.setDisplayName("\u00A7c\u00A7l" + scenario.getName());
            }
            
            List<String> lore = new ArrayList<>();
            lore.add("\u00A77" + scenario.getDescription());
            lore.add("");
            if (scenario.isEnabled()) {
                lore.add("\u00A7a\u25B8 Activ\u00E9");
            } else {
                lore.add("\u00A7c\u25B8 D\u00E9sactiv\u00E9");
            }
            lore.add("");
            lore.add("\u00A7e\u25BA Clic gauche pour basculer");
            if (scenario.hasConfigMenu()) {
                lore.add("\u00A7b\u25BA Clic droit pour configurer");
            }
            
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            
            inventory.setItem(slot, item);
            slot++;
        }
        
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("\u00A7cRetour");
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);
    }

    public void handleClick(Player player, int slot, boolean isRightClick) {
        if (slot == 49) {
            player.openInventory(new AdminConfigGUI().getInventory());
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            return;
        }

        List<Scenario> scenarios = HelPlugin.getInstance().getScenarioManager().getScenarios();
        if (slot < scenarios.size()) {
            Scenario scenario = scenarios.get(slot);
            if (isRightClick && scenario.hasConfigMenu()) {
                scenario.onRightClick(player);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            } else {
                scenario.toggle();
                player.playSound(player.getLocation(), scenario.isEnabled() ? org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING : org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1f);
                populate();
            }
        }
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
