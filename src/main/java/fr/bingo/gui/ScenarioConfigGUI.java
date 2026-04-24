package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.scenario.Scenario;
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
        this.inventory = Bukkit.createInventory(this, 54, "§6§lScénarios");
        populate();
    }

    public void populate() {
        inventory.clear();

        List<Scenario> scenarios = BingoPlugin.getInstance().getScenarioManager().getScenarios();
        
        int slot = 0;
        for (Scenario scenario : scenarios) {
            if (slot >= 45) break; 
            
            ItemStack item = new ItemStack(scenario.getIcon());
            ItemMeta meta = item.getItemMeta();
            
            if (scenario.isEnabled()) {
                meta.setDisplayName("§a§l" + scenario.getName());
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                meta.setDisplayName("§c§l" + scenario.getName());
            }
            
            List<String> lore = new ArrayList<>();
            lore.add("§7" + scenario.getDescription());
            lore.add("");
            if (scenario.isEnabled()) {
                lore.add("§a▸ Activé");
            } else {
                lore.add("§c▸ Désactivé");
            }
            lore.add("");
            lore.add("§e► Clic gauche pour basculer");
            if (scenario.hasConfigMenu()) {
                lore.add("§b► Clic droit pour configurer");
            }
            
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
            
            inventory.setItem(slot, item);
            slot++;
        }
        
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cRetour");
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);
    }

    public void handleClick(Player player, int slot, boolean isRightClick) {
        if (slot == 49) {
            player.openInventory(new AdminConfigGUI().getInventory());
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            return;
        }

        List<Scenario> scenarios = BingoPlugin.getInstance().getScenarioManager().getScenarios();
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
