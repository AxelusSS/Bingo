package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.game.BorderManager;
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
        this.inventory = Bukkit.createInventory(this, 27, "§6§l⚙ Configuration Bordure");
        populate();
    }

    private void populate() {
        BorderManager bm = BingoPlugin.getInstance().getBorderManager();
        inventory.clear();

        // Fond décoratif
        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName("§r");
        bg.setItemMeta(bgMeta);
        for (int i = 0; i < 27; i++) inventory.setItem(i, bg);

        // Slot 10 : Taille initiale
        inventory.setItem(10, createItem(Material.GRASS_BLOCK, "§e§lTaille initiale : §b" + bm.getInitialSize(),
                List.of("§7La taille de la bordure au début",
                        "",
                        (bm.getInitialSize() == 1000 ? "§b▸ " : "§7  ") + "1000",
                        (bm.getInitialSize() == 2000 ? "§b▸ " : "§7  ") + "2000",
                        (bm.getInitialSize() == 3000 ? "§b▸ " : "§7  ") + "3000",
                        (bm.getInitialSize() == 4000 ? "§b▸ " : "§7  ") + "4000",
                        "",
                        "§e► Clic pour changer")));

        // Slot 12 : Taille finale
        inventory.setItem(12, createItem(Material.COBBLESTONE, "§e§lTaille finale : §b" + bm.getFinalSize(),
                List.of("§7La taille de la bordure à la fin",
                        "",
                        (bm.getFinalSize() == 50 ? "§b▸ " : "§7  ") + "50",
                        (bm.getFinalSize() == 100 ? "§b▸ " : "§7  ") + "100",
                        (bm.getFinalSize() == 200 ? "§b▸ " : "§7  ") + "200",
                        (bm.getFinalSize() == 500 ? "§b▸ " : "§7  ") + "500",
                        "",
                        "§e► Clic pour changer")));

        // Slot 14 : Temps avant réduction
        inventory.setItem(14, createItem(Material.CLOCK, "§e§lRéduction après : §b" + bm.getTimeBeforeShrinkMinutes() + " min",
                List.of("§7Temps avant que la bordure rétrécisse",
                        "",
                        (bm.getTimeBeforeShrinkMinutes() == 30 ? "§b▸ " : "§7  ") + "30 min",
                        (bm.getTimeBeforeShrinkMinutes() == 45 ? "§b▸ " : "§7  ") + "45 min",
                        (bm.getTimeBeforeShrinkMinutes() == 60 ? "§b▸ " : "§7  ") + "60 min",
                        (bm.getTimeBeforeShrinkMinutes() == 90 ? "§b▸ " : "§7  ") + "90 min",
                        "",
                        "§e► Clic pour changer")));

        // Slot 16 : Temps de réduction
        inventory.setItem(16, createItem(Material.SOUL_TORCH, "§e§lDurée réduction : §b" + bm.getShrinkTimeMinutes() + " min",
                List.of("§7Temps mis pour rétrécir",
                        "",
                        (bm.getShrinkTimeMinutes() == 15 ? "§b▸ " : "§7  ") + "15 min",
                        (bm.getShrinkTimeMinutes() == 30 ? "§b▸ " : "§7  ") + "30 min",
                        (bm.getShrinkTimeMinutes() == 45 ? "§b▸ " : "§7  ") + "45 min",
                        (bm.getShrinkTimeMinutes() == 60 ? "§b▸ " : "§7  ") + "60 min",
                        "",
                        "§e► Clic pour changer")));

        // Slot 22 : Retour
        inventory.setItem(22, createItem(Material.ARROW, "§c§l← Retour", List.of("§7Retour au menu principal")));
    }

    public void handleClick(Player player, int slot, boolean isRightClick) {
        BorderManager bm = BingoPlugin.getInstance().getBorderManager();

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
            case 14 -> { // Temps avant réduction
                int cur = bm.getTimeBeforeShrinkMinutes();
                int next = cur == 30 ? 45 : cur == 45 ? 60 : cur == 60 ? 90 : 30;
                bm.setTimeBeforeShrinkMinutes(next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 16 -> { // Temps de réduction
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
