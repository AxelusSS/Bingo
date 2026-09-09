package fr.hel.gui;

import fr.hel.scenario.SuperHeroScenario;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class SuperHeroConfigGUI implements InventoryHolder {

    private final Inventory inventory;
    private final SuperHeroScenario scenario;

    public SuperHeroConfigGUI(SuperHeroScenario scenario) {
        this.scenario = scenario;
        this.inventory = Bukkit.createInventory(this, 27, "\u00A76\u00A7lConfiguration Super Hero");
        populate();
    }

    public void populate() {
        inventory.clear();
        
        inventory.setItem(10, createEffectItem(PotionEffectType.STRENGTH, "Force", Material.IRON_SWORD));
        inventory.setItem(11, createEffectItem(PotionEffectType.RESISTANCE, "R\u00E9sistance", Material.IRON_CHESTPLATE));
        inventory.setItem(12, createEffectItem(PotionEffectType.HEALTH_BOOST, "Double Vie", Material.GOLDEN_APPLE));
        inventory.setItem(13, createEffectItem(PotionEffectType.SPEED, "Vitesse", Material.SUGAR));
        inventory.setItem(14, createEffectItem(PotionEffectType.JUMP_BOOST, "Saut Am\u00E9lior\u00E9", Material.RABBIT_FOOT));
        
        ItemStack vampire = new ItemStack(Material.REDSTONE);
        ItemMeta meta = vampire.getItemMeta();
        meta.setDisplayName("\u00A7c\u00A7lVampire Mode");
        meta.setLore(List.of(
            "\u00A77Voler l'effet de la derni\u00E8re personne tu\u00E9e",
            "",
            scenario.isVampireMode() ? "\u00A7a\u25B8 Activ\u00E9" : "\u00A7c\u25B8 D\u00E9sactiv\u00E9",
            "",
            "\u00A7e\u25BA Clic pour basculer"
        ));
        vampire.setItemMeta(meta);
        inventory.setItem(16, vampire);
        
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("\u00A7cRetour");
        back.setItemMeta(backMeta);
        inventory.setItem(22, back);
    }

    private ItemStack createEffectItem(PotionEffectType type, String name, Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        boolean enabled = scenario.getAllowedEffects().getOrDefault(type, false);
        meta.setDisplayName((enabled ? "\u00A7a" : "\u00A7c") + name);
        meta.setLore(List.of(
            "\u00A77Possibilit\u00E9 de recevoir cet effet au start",
            "",
            enabled ? "\u00A7a\u25B8 Activ\u00E9" : "\u00A7c\u25B8 D\u00E9sactiv\u00E9",
            "",
            "\u00A7e\u25BA Clic pour basculer"
        ));
        item.setItemMeta(meta);
        return item;
    }

    public void handleClick(Player player, int slot) {
        switch (slot) {
            case 10 -> toggleEffect(PotionEffectType.STRENGTH);
            case 11 -> toggleEffect(PotionEffectType.RESISTANCE);
            case 12 -> toggleEffect(PotionEffectType.HEALTH_BOOST);
            case 13 -> toggleEffect(PotionEffectType.SPEED);
            case 14 -> toggleEffect(PotionEffectType.JUMP_BOOST);
            case 16 -> scenario.setVampireMode(!scenario.isVampireMode());
            case 22 -> player.openInventory(new ScenarioConfigGUI().getInventory());
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
        populate();
    }

    private void toggleEffect(PotionEffectType type) {
        scenario.getAllowedEffects().put(type, !scenario.getAllowedEffects().getOrDefault(type, false));
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
