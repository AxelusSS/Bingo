package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.game.BorderManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BorderConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public BorderConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 27, "\u00A76\u00A7l\u2699 Configuration Bordure");
        populate();
    }

    private void populate() {
        BorderManager bm = HelPlugin.getInstance().getBorderManager();
        inventory.clear();

        // Fond d\u00E9coratif
        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName("\u00A7r");
        bg.setItemMeta(bgMeta);
        for (int i = 0; i < 27; i++) inventory.setItem(i, bg);

        // Slot 10 : Taille initiale
        inventory.setItem(10, createItem(Material.GRASS_BLOCK, "\u00A7e\u00A7lTaille initiale : \u00A7b" + bm.getInitialSize(),
                List.of("\u00A77La taille de la bordure au d\u00E9but",
                        "",
                        (bm.getInitialSize() == 1000 ? "\u00A7b\u25B8 " : "\u00A77  ") + "1000",
                        (bm.getInitialSize() == 2000 ? "\u00A7b\u25B8 " : "\u00A77  ") + "2000",
                        (bm.getInitialSize() == 3000 ? "\u00A7b\u25B8 " : "\u00A77  ") + "3000",
                        (bm.getInitialSize() == 4000 ? "\u00A7b\u25B8 " : "\u00A77  ") + "4000",
                        "",
                        "\u00A7e\u25BA Clic pour changer")));

        // Slot 12 : Taille finale
        inventory.setItem(12, createItem(Material.COBBLESTONE, "\u00A7e\u00A7lTaille finale : \u00A7b" + bm.getFinalSize(),
                List.of("\u00A77La taille de la bordure \u00E0 la fin",
                        "",
                        (bm.getFinalSize() == 50 ? "\u00A7b\u25B8 " : "\u00A77  ") + "50",
                        (bm.getFinalSize() == 100 ? "\u00A7b\u25B8 " : "\u00A77  ") + "100",
                        (bm.getFinalSize() == 200 ? "\u00A7b\u25B8 " : "\u00A77  ") + "200",
                        (bm.getFinalSize() == 500 ? "\u00A7b\u25B8 " : "\u00A77  ") + "500",
                        "",
                        "\u00A7e\u25BA Clic pour changer")));

        // Slot 14 : Temps avant r\u00E9duction
        inventory.setItem(14, createItem(Material.CLOCK, "\u00A7e\u00A7lR\u00E9duction apr\u00E8s : \u00A7b" + bm.getTimeBeforeShrinkMinutes() + " min",
                List.of("\u00A77Temps avant que la bordure r\u00E9tr\u00E9cisse",
                        "",
                        (bm.getTimeBeforeShrinkMinutes() == 30 ? "\u00A7b\u25B8 " : "\u00A77  ") + "30 min",
                        (bm.getTimeBeforeShrinkMinutes() == 45 ? "\u00A7b\u25B8 " : "\u00A77  ") + "45 min",
                        (bm.getTimeBeforeShrinkMinutes() == 60 ? "\u00A7b\u25B8 " : "\u00A77  ") + "60 min",
                        (bm.getTimeBeforeShrinkMinutes() == 90 ? "\u00A7b\u25B8 " : "\u00A77  ") + "90 min",
                        "",
                        "\u00A7e\u25BA Clic pour changer")));

        // Slot 16 : Temps de r\u00E9duction
        inventory.setItem(16, createItem(Material.SOUL_TORCH, "\u00A7e\u00A7lDur\u00E9e r\u00E9duction : \u00A7b" + bm.getShrinkTimeMinutes() + " min",
                List.of("\u00A77Temps mis pour r\u00E9tr\u00E9cir",
                        "",
                        (bm.getShrinkTimeMinutes() == 15 ? "\u00A7b\u25B8 " : "\u00A77  ") + "15 min",
                        (bm.getShrinkTimeMinutes() == 30 ? "\u00A7b\u25B8 " : "\u00A77  ") + "30 min",
                        (bm.getShrinkTimeMinutes() == 45 ? "\u00A7b\u25B8 " : "\u00A77  ") + "45 min",
                        (bm.getShrinkTimeMinutes() == 60 ? "\u00A7b\u25B8 " : "\u00A77  ") + "60 min",
                        "",
                        "\u00A7e\u25BA Clic pour changer")));

        // Slot 22 : Retour
        inventory.setItem(22, createItem(Material.ARROW, "\u00A7c\u00A7l\u2190 Retour", List.of("\u00A77Retour au menu principal")));
    }

    public void handleClick(Player player, int slot, boolean isRightClick) {
        BorderManager bm = HelPlugin.getInstance().getBorderManager();

        switch (slot) {
            case 10 -> { // Taille initiale
                int cur = bm.getInitialSize();
                int next = cur == 1000 ? 2000 : cur == 2000 ? 3000 : cur == 3000 ? 4000 : 1000;
                bm.setInitialSize(next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 12 -> { // Taille finale
                int cur = bm.getFinalSize();
                int next = cur == 50 ? 100 : cur == 100 ? 200 : cur == 200 ? 500 : 50;
                bm.setFinalSize(next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 14 -> { // Temps avant r\u00E9duction
                int cur = bm.getTimeBeforeShrinkMinutes();
                int next = cur == 30 ? 45 : cur == 45 ? 60 : cur == 60 ? 90 : 30;
                bm.setTimeBeforeShrinkMinutes(next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 16 -> { // Temps de r\u00E9duction
                int cur = bm.getShrinkTimeMinutes();
                int next = cur == 15 ? 30 : cur == 30 ? 45 : cur == 45 ? 60 : 15;
                bm.setShrinkTimeMinutes(next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 22 -> { // Retour
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new AdminConfigGUI().getInventory());
            }
        }
    }

    private void refresh(Player player) {
        populate();
        player.openInventory(inventory);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
