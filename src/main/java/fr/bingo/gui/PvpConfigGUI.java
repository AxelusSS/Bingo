package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.game.BingoGame;
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
 * Utilise des blocs de béton colorés (fiables à 100%).
 */
public class PvpConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public PvpConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 27, "§c§l⚔ Configuration PVP");
        populate();
    }

    private void populate() {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        inventory.clear();

        // Bordure décorative
        ItemStack border = createItem(Material.GRAY_STAINED_GLASS_PANE, "§r", List.of());
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        // Slot 4 : Toggle PVP On/Off
        boolean disabled = game.isPvpDisabled();
        int timer = game.getPvpTimerMinutes();

        String pvpStatus;
        Material pvpMat;
        if (disabled) {
            pvpStatus = "§c§lDÉSACTIVÉ";
            pvpMat = Material.BARRIER;
        } else if (timer == 0) {
            pvpStatus = "§a§lIMMÉDIAT";
            pvpMat = Material.DIAMOND_SWORD;
        } else {
            pvpStatus = "§e§lAVEC TIMER";
            pvpMat = Material.IRON_SWORD;
        }

        ItemStack toggleItem = createItemHidden(pvpMat, "§e§lPVP : " + pvpStatus,
                List.of("§7Clic pour changer le mode",
                        "",
                        (disabled ? "§c▸ " : "§7  ") + "Désactivé",
                        (!disabled && timer == 0 ? "§a▸ " : "§7  ") + "Immédiat (dès le start)",
                        (!disabled && timer > 0 ? "§e▸ " : "§7  ") + "Avec Timer"));
        inventory.setItem(4, toggleItem);

        // Slot 11 : Bouton ROUGE (diminuer)
        ItemStack redBtn = createItem(Material.RED_CONCRETE, "§c§l⊖ Diminuer le timer",
                List.of("§a⊖ Clic gauche §7→ §c-1 min",
                        "§e⊖ Clic droit §7→ §c-5 min"));
        inventory.setItem(11, redBtn);

        // Slot 13 : Timer actuel (horloge)
        String timerDisplay;
        if (disabled) {
            timerDisplay = "§c§lDÉSACTIVÉ";
        } else {
            timerDisplay = "§b§l" + timer + " min";
        }
        ItemStack timerItem = createItem(Material.CLOCK, "§e§lTimer PVP : " + timerDisplay,
                List.of("§7Temps avant activation du PVP",
                        "",
                        "§7Si §b0 min§7 → PVP dès le début",
                        "§7Ajustable avec les boutons"));
        timerItem.setAmount(Math.max(1, Math.min(timer > 0 ? timer : 1, 64)));
        inventory.setItem(13, timerItem);

        // Slot 15 : Bouton VERT (augmenter)
        ItemStack greenBtn = createItem(Material.LIME_CONCRETE, "§a§l⊕ Augmenter le timer",
                List.of("§a⊕ Clic gauche §7→ §a+1 min",
                        "§e⊕ Clic droit §7→ §a+5 min"));
        inventory.setItem(15, greenBtn);

        // Slot 22 : Retour
        ItemStack backItem = createItem(Material.ARROW, "§7§l← Retour",
                List.of("§7Retourner au menu principal"));
        inventory.setItem(22, backItem);
    }

    /**
     * Gère les clics dans le sous-menu PVP.
     */
    public void handleClick(Player player, int slot, boolean isRightClick) {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();

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
