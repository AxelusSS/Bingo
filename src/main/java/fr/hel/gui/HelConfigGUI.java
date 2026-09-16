package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.game.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * GUI de configuration du sc\u00E9nario Hel.
 * Accessible via clic-droit sur le sc\u00E9nario Hel dans la liste des sc\u00E9narios.
 */
public class HelConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public HelConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 27, "\u00A76\u00A7l\u2699 Configuration Hel");
        populate();
    }

    private void populate() {
        HelGame game = HelPlugin.getInstance().getHelGame();
        inventory.clear();

        // Fond
        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName("\u00A7r");
        bg.setItemMeta(bgMeta);
        for (int i = 0; i < 27; i++) inventory.setItem(i, bg);

        // Slot 10 : Taille de la grille
        int size = game.getGrid().getSize();
        String sizeStr = size == 1 ? "ROULETTE" : size + "x" + size;
        inventory.setItem(10, createItem(Material.MAP, "\u00A7e\u00A7lTaille : \u00A7b" + sizeStr,
                List.of("\u00A77Clic pour changer",
                        "",
                        (size == 1 ? "\u00A7b\u25B8 " : "\u00A77  ") + "Roulette (1 seul objectif)",
                        (size == 3 ? "\u00A7b\u25B8 " : "\u00A77  ") + "3x3",
                        (size == 5 ? "\u00A7b\u25B8 " : "\u00A77  ") + "5x5",
                        (size == 7 ? "\u00A7b\u25B8 " : "\u00A77  ") + "7x7")));

        // Slot 12 : Difficult\u00E9
        Difficulty diff = game.getDifficulty();
        Material diffMat = switch (diff) {
            case EASY -> Material.LIME_DYE;
            case MEDIUM -> Material.YELLOW_DYE;
            case HARD -> Material.RED_DYE;
            case EXTREME -> Material.WITHER_SKELETON_SKULL;
        };
        inventory.setItem(12, createItem(diffMat, "\u00A7e\u00A7lDifficult\u00E9 : " + diff.getColor() + diff.getDisplayName(),
                List.of("\u00A77Clic pour changer",
                        "",
                        (diff == Difficulty.EASY ? "\u00A7a\u25B8 " : "\u00A77  ") + "Facile",
                        (diff == Difficulty.MEDIUM ? "\u00A7e\u25B8 " : "\u00A77  ") + "Normal",
                        (diff == Difficulty.HARD ? "\u00A7c\u25B8 " : "\u00A77  ") + "Difficile",
                        (diff == Difficulty.EXTREME ? "\u00A74\u25B8 " : "\u00A77  ") + "Extr\u00EAme")));

        // Slot 14 : Mode
        HelMode mode = game.getMode();
        Material modeMat = switch (mode) {
            case ITEMS -> Material.CHEST;
            case ACHIEVEMENTS -> Material.DRAGON_EGG;
            case MIXED -> Material.ENDER_CHEST;
        };
        inventory.setItem(14, createItem(modeMat, "\u00A7e\u00A7lMode : " + mode.getColor() + mode.getDisplayName(),
                List.of("\u00A77Clic pour changer",
                        "",
                        (mode == HelMode.ITEMS ? "\u00A7b\u25B8 " : "\u00A77  ") + "Items",
                        (mode == HelMode.ACHIEVEMENTS ? "\u00A7d\u25B8 " : "\u00A77  ") + "Achievements",
                        (mode == HelMode.MIXED ? "\u00A76\u25B8 " : "\u00A77  ") + "Mixte")));

        // Slot 16 : G\u00E9n\u00E9rer la grille
        inventory.setItem(16, createItem(Material.NETHER_STAR, "\u00A7a\u00A7l\u2726 G\u00C9N\u00C9RER LA GRILLE",
                List.of("\u00A77G\u00E9n\u00E8re une nouvelle grille",
                        "\u00A77avec les param\u00E8tres actuels",
                        "",
                        "\u00A7e\u25BA Clic pour g\u00E9n\u00E9rer")));

        // Slot 19 : Pool Config
        inventory.setItem(19, createItem(Material.ENCHANTED_BOOK, "\u00A7b\u00A7l\u1F3AF Table des Items",
                List.of("\u00A77Configurer la liste des items",
                        "\u00A77pouvant appara\u00EEtre dans le bingo.",
                        "",
                        "\u00A7e\u25BA Clic pour ouvrir")));

        // Slot 21 : Bordure
        boolean border = game.isBorderEnabled();
        inventory.setItem(21, createItem(Material.BARRIER,
                "\u00A7c\u00A7lBordure : " + (border ? "\u00A7aACTIV\u00C9E" : "\u00A7cD\u00C9SACTIV\u00C9E"),
                List.of("\u00A77Si d\u00E9sactiv\u00E9e, la bordure sera",
                        "\u00A77fix\u00E9e \u00E0 10000 blocs.",
                        "",
                        "\u00A7e\u25BA Clic pour basculer")));

        // Slot 22 : Retour
        inventory.setItem(22, createItem(Material.ARROW, "\u00A7c\u00A7l\u2190 Retour", List.of("\u00A77Retour aux sc\u00E9narios")));
    }

    public void handleClick(Player player, int slot, boolean isRightClick) {
        HelGame game = HelPlugin.getInstance().getHelGame();

        switch (slot) {
            case 10 -> { // Taille grille
                int current = game.getGrid().getSize();
                int next = current == 1 ? 3 : current == 3 ? 5 : current == 5 ? 7 : 1;
                game.getGrid().setSize(next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 12 -> { // Difficult\u00E9
                Difficulty[] vals = Difficulty.values();
                int next = (game.getDifficulty().ordinal() + 1) % vals.length;
                game.setDifficulty(vals[next]);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 13 -> { // Mode Items
                game.setMode(fr.hel.game.HelMode.ITEMS);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 15 -> { // Mode Achievements
                game.setMode(fr.hel.game.HelMode.ACHIEVEMENTS);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 16 -> { // G\u00E9n\u00E9rer
                player.closeInventory();
                game.getGrid().generateRandomGrid(game.getMode(), game.getDifficulty());
                new fr.hel.game.DatapackManager().generateAdvancementsDatapack(game.getGrid(), game.getMode());

                if (game.getGrid().getSize() == 1) {
                    // Roulette mode
                    fr.hel.gui.RouletteGUI.startRoulette(game.getGrid().getObjectives().get(0), () -> {
                        player.sendMessage("\u00A7a\u00A7lL'objectif a \u00E9t\u00E9 tir\u00E9 au sort !");
                    });
                } else {
                    String sName = game.getGrid().getSize() + "x" + game.getGrid().getSize();
                    player.sendMessage("\u00A7a\u00A7lGrille g\u00E9n\u00E9r\u00E9e ! \u00A77(" + sName +
                            ", " + game.getDifficulty().getDisplayName() + ", " + game.getMode().getDisplayName() + ")");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
                }
            }
            case 19 -> { // Pool Config
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new PoolConfigGUI(0).getInventory());
            }
            case 21 -> { // Bordure
                game.setBorderEnabled(!game.isBorderEnabled());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 22 -> { // Retour
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new ScenarioConfigGUI().getInventory());
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
