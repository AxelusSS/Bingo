package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.game.*;
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
 * GUI de configuration du scénario Bingo.
 * Accessible via clic-droit sur le scénario Bingo dans la liste des scénarios.
 */
public class BingoConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public BingoConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 27, "§6§l⚙ Configuration Bingo");
        populate();
    }

    private void populate() {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        inventory.clear();

        // Fond
        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName("§r");
        bg.setItemMeta(bgMeta);
        for (int i = 0; i < 27; i++) inventory.setItem(i, bg);

        // Slot 10 : Taille de la grille
        int size = game.getGrid().getSize();
        String sizeStr = size == 1 ? "ROULETTE" : size + "x" + size;
        inventory.setItem(10, createItem(Material.MAP, "§e§lTaille : §b" + sizeStr,
                List.of("§7Clic pour changer",
                        "",
                        (size == 1 ? "§b▸ " : "§7  ") + "Roulette (1 seul objectif)",
                        (size == 3 ? "§b▸ " : "§7  ") + "3x3",
                        (size == 5 ? "§b▸ " : "§7  ") + "5x5",
                        (size == 7 ? "§b▸ " : "§7  ") + "7x7")));

        // Slot 12 : Difficulté
        Difficulty diff = game.getDifficulty();
        Material diffMat = switch (diff) {
            case EASY -> Material.LIME_DYE;
            case MEDIUM -> Material.YELLOW_DYE;
            case HARD -> Material.RED_DYE;
            case EXTREME -> Material.WITHER_SKELETON_SKULL;
        };
        inventory.setItem(12, createItem(diffMat, "§e§lDifficulté : " + diff.getColor() + diff.getDisplayName(),
                List.of("§7Clic pour changer",
                        "",
                        (diff == Difficulty.EASY ? "§a▸ " : "§7  ") + "Facile",
                        (diff == Difficulty.MEDIUM ? "§e▸ " : "§7  ") + "Normal",
                        (diff == Difficulty.HARD ? "§c▸ " : "§7  ") + "Difficile",
                        (diff == Difficulty.EXTREME ? "§4▸ " : "§7  ") + "Extrême")));

        // Slot 14 : Mode
        BingoMode mode = game.getMode();
        Material modeMat = switch (mode) {
            case ITEMS -> Material.CHEST;
            case ACHIEVEMENTS -> Material.DRAGON_EGG;
            case MIXED -> Material.ENDER_CHEST;
        };
        inventory.setItem(14, createItem(modeMat, "§e§lMode : " + mode.getColor() + mode.getDisplayName(),
                List.of("§7Clic pour changer",
                        "",
                        (mode == BingoMode.ITEMS ? "§b▸ " : "§7  ") + "Items",
                        (mode == BingoMode.ACHIEVEMENTS ? "§d▸ " : "§7  ") + "Achievements",
                        (mode == BingoMode.MIXED ? "§6▸ " : "§7  ") + "Mixte")));

        // Slot 16 : Générer la grille
        inventory.setItem(16, createItem(Material.NETHER_STAR, "§a§l✦ GÉNÉRER LA GRILLE",
                List.of("§7Génère une nouvelle grille",
                        "§7avec les paramètres actuels",
                        "",
                        "§e► Clic gauche pour générer",
                        "§d► Clic droit pour configurer la pool")));

        // Slot 22 : Retour
        inventory.setItem(22, createItem(Material.ARROW, "§c§l← Retour", List.of("§7Retour aux scénarios")));
    }

    public void handleClick(Player player, int slot, boolean isRightClick) {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();

        switch (slot) {
            case 10 -> { // Taille grille
                int current = game.getGrid().getSize();
                int next = current == 1 ? 3 : current == 3 ? 5 : current == 5 ? 7 : 1;
                game.getGrid().setSize(next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 12 -> { // Difficulté
                Difficulty[] vals = Difficulty.values();
                int next = (game.getDifficulty().ordinal() + 1) % vals.length;
                game.setDifficulty(vals[next]);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 14 -> { // Mode
                game.setMode(game.getMode().next());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 16 -> { // Générer
                if (isRightClick) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                    player.openInventory(new PoolConfigGUI(0).getInventory());
                } else {
                    player.closeInventory();
                    game.getGrid().generateRandomGrid(game.getMode(), game.getDifficulty());
                    new DatapackManager().generateAdvancementsDatapack(game.getGrid(), game.getMode());

                    if (game.getGrid().getSize() == 1) {
                        // Roulette mode
                        RouletteGUI.startRoulette(game.getGrid().getObjectives().get(0), () -> {
                            player.sendMessage("§a§lL'objectif a été tiré au sort !");
                        });
                    } else {
                        String sName = game.getGrid().getSize() + "x" + game.getGrid().getSize();
                        player.sendMessage("§a§lGrille générée ! §7(" + sName +
                                ", " + game.getDifficulty().getDisplayName() + ", " + game.getMode().getDisplayName() + ")");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
                    }
                }
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
