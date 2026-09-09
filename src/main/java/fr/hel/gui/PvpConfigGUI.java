package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.game.HelGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Sous-menu de configuration PVP.
 * Utilise des blocs de b\u00E9ton color\u00E9s (fiables \u00E0 100%).
 */
public class PvpConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public PvpConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 27, "\u00A7c\u00A7l\u2694 Configuration PVP");
        populate();
    }

    private void populate() {
        HelGame game = HelPlugin.getInstance().getHelGame();
        inventory.clear();

        // Bordure d\u00E9corative
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "\u00A7r", List.of());
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        // Slot 4 : Toggle PVP On/Off
        boolean disabled = game.isPvpDisabled();
        int timer = game.getPvpTimerMinutes();

        String pvpStatus;
        Material pvpMat;
        if (disabled) {
            pvpStatus = "\u00A7c\u00A7lD\u00C9SACTIV\u00C9";
            pvpMat = Material.BARRIER;
        } else if (timer == 0) {
            pvpStatus = "\u00A7a\u00A7lIMM\u00C9DIAT";
            pvpMat = Material.DIAMOND_SWORD;
        } else {
            pvpStatus = "\u00A7e\u00A7lAVEC TIMER";
            pvpMat = Material.IRON_SWORD;
        }

        ItemStack toggleItem = createItemHidden(pvpMat, "\u00A7e\u00A7lPVP : " + pvpStatus,
                List.of("\u00A77Clic pour changer le mode",
                        "",
                        (disabled ? "\u00A7c\u25B8 " : "\u00A77  ") + "D\u00E9sactiv\u00E9",
                        (!disabled && timer == 0 ? "\u00A7a\u25B8 " : "\u00A77  ") + "Imm\u00E9diat (d\u00E8s le start)",
                        (!disabled && timer > 0 ? "\u00A7e\u25B8 " : "\u00A77  ") + "Avec Timer"));
        inventory.setItem(4, toggleItem);

        // Slot 11 : Bouton ROUGE (diminuer)
        ItemStack redBtn = createItem(Material.RED_CONCRETE, "\u00A7c\u00A7l\u2296 Diminuer le timer",
                List.of("\u00A7a\u2296 Clic gauche \u00A77\u2192 \u00A7c-1 min",
                        "\u00A7e\u2296 Clic droit \u00A77\u2192 \u00A7c-5 min"));
        inventory.setItem(11, redBtn);

        // Slot 13 : Timer actuel (horloge)
        String timerDisplay;
        if (disabled) {
            timerDisplay = "\u00A7c\u00A7lD\u00C9SACTIV\u00C9";
        } else {
            timerDisplay = "\u00A7b\u00A7l" + timer + " min";
        }
        ItemStack timerItem = createItem(Material.CLOCK, "\u00A7e\u00A7lTimer PVP : " + timerDisplay,
                List.of("\u00A77Temps avant activation du PVP",
                        "",
                        "\u00A77Si \u00A7b0 min\u00A77 \u2192 PVP d\u00E8s le d\u00E9but",
                        "\u00A77Ajustable avec les boutons"));
        timerItem.setAmount(Math.max(1, Math.min(timer > 0 ? timer : 1, 64)));
        inventory.setItem(13, timerItem);

        // Slot 15 : Bouton VERT (augmenter)
        ItemStack greenBtn = createItem(Material.LIME_CONCRETE, "\u00A7a\u00A7l\u2295 Augmenter le timer",
                List.of("\u00A7a\u2295 Clic gauche \u00A77\u2192 \u00A7a+1 min",
                        "\u00A7e\u2295 Clic droit \u00A77\u2192 \u00A7a+5 min"));
        inventory.setItem(15, greenBtn);

        // Slot 22 : Retour
        ItemStack backItem = createItem(Material.ARROW, "\u00A77\u00A7l\u2190 Retour",
                List.of("\u00A77Retourner au menu principal"));
        inventory.setItem(22, backItem);
    }

    /**
     * G\u00E8re les clics dans le sous-menu PVP.
     */
    public void handleClick(Player player, int slot, boolean isRightClick) {
        HelGame game = HelPlugin.getInstance().getHelGame();

        switch (slot) {
            case 4 -> { // Toggle mode PVP
                if (game.isPvpDisabled()) {
                    game.setPvpDisabled(false);
                    game.setPvpTimerMinutes(0);
                } else if (game.getPvpTimerMinutes() == 0) {
                    game.setPvpTimerMinutes(20);
                } else {
                    game.setPvpDisabled(true);
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 11 -> { // Diminuer timer
                if (!game.isPvpDisabled()) {
                    int delta = isRightClick ? 5 : 1;
                    game.setPvpTimerMinutes(Math.max(0, game.getPvpTimerMinutes() - delta));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
                    refresh(player);
                }
            }
            case 15 -> { // Augmenter timer
                if (!game.isPvpDisabled()) {
                    int delta = isRightClick ? 5 : 1;
                    game.setPvpTimerMinutes(Math.min(120, game.getPvpTimerMinutes() + delta));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                    refresh(player);
                }
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

    private ItemStack createItemHidden(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
