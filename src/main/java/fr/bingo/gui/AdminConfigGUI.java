package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.game.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * GUI de configuration admin. S'ouvre via le compas dans la hotbar.
 */
public class AdminConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public AdminConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 27, "§6§l⚙ Configuration Bingo");
        populate();
    }

    private void populate() {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        inventory.clear();

        // Slot 0 : Taille de la grille
        int size = game.getGrid().getSize();
        ItemStack sizeItem = createItem(Material.MAP, "§e§lTaille : §b" + size + "x" + size,
                List.of("§7Clic pour changer", "§7Options : 3, 5, 7"));
        inventory.setItem(0, sizeItem);

        // Slot 2 : Difficulté
        Difficulty diff = game.getDifficulty();
        Material diffMat = switch (diff) {
            case EASY -> Material.LIME_DYE;
            case MEDIUM -> Material.YELLOW_DYE;
            case HARD -> Material.RED_DYE;
            case EXTREME -> Material.WITHER_SKELETON_SKULL;
        };
        ItemStack diffItem = createItem(diffMat, "§e§lDifficulté : " + diff.getColor() + diff.getDisplayName(),
                List.of("§7Clic pour changer",
                        (diff == Difficulty.EASY ? "§a▸ " : "§7  ") + "Facile",
                        (diff == Difficulty.MEDIUM ? "§e▸ " : "§7  ") + "Normal",
                        (diff == Difficulty.HARD ? "§c▸ " : "§7  ") + "Difficile",
                        (diff == Difficulty.EXTREME ? "§4▸ " : "§7  ") + "Extrême"));
        inventory.setItem(2, diffItem);

        // Slot 4 : Mode
        BingoMode mode = game.getMode();
        Material modeMat = switch (mode) {
            case ITEMS -> Material.CHEST;
            case ACHIEVEMENTS -> Material.DRAGON_EGG;
            case MIXED -> Material.ENDER_CHEST;
        };
        ItemStack modeItem = createItem(modeMat, "§e§lMode : " + mode.getColor() + mode.getDisplayName(),
                List.of("§7Clic pour changer",
                        (mode == BingoMode.ITEMS ? "§b▸ " : "§7  ") + "Items",
                        (mode == BingoMode.ACHIEVEMENTS ? "§d▸ " : "§7  ") + "Achievements",
                        (mode == BingoMode.MIXED ? "§6▸ " : "§7  ") + "Mixte"));
        inventory.setItem(4, modeItem);

        // Slot 6 : Random Teams
        ItemStack randomItem = createItem(Material.PLAYER_HEAD, "§e§lÉquipes Aléatoires",
                List.of("§7Répartir les joueurs aléatoirement", "§7Clic pour exécuter"));
        inventory.setItem(6, randomItem);

        // Slot 8 : Lock/Unlock Teams
        boolean locked = BingoPlugin.getInstance().getTeamManager().isTeamsLocked();
        ItemStack lockItem = createItem(locked ? Material.BARRIER : Material.OAK_DOOR,
                locked ? "§c§lÉquipes Verrouillées" : "§a§lÉquipes Ouvertes",
                List.of("§7Clic pour " + (locked ? "déverrouiller" : "verrouiller")));
        inventory.setItem(8, lockItem);

        // Slot 12 : GÉNÉRER
        ItemStack genItem = createItem(Material.NETHER_STAR, "§a§l✦ GÉNÉRER LA GRILLE",
                List.of("§7Génère une nouvelle grille", "§7avec les paramètres actuels",
                        "", "§7Taille : §b" + size + "x" + size,
                        "§7Difficulté : " + diff.getColor() + diff.getDisplayName(),
                        "§7Mode : " + mode.getColor() + mode.getDisplayName()));
        inventory.setItem(12, genItem);

        // Slot 14 : START
        ItemStack startItem = createItem(Material.LIME_CONCRETE, "§a§l▶ LANCER LA PARTIE",
                List.of("§7Démarre le décompte et lance le Bingo !"));
        inventory.setItem(14, startItem);

        // Slot 22 : RESET
        ItemStack resetItem = createItem(Material.TNT, "§c§l↻ RESET",
                List.of("§7Réinitialise la partie", "§7Remet tout à zéro"));
        inventory.setItem(22, resetItem);
    }

    /**
     * Gère les clics dans le GUI admin.
     */
    public void handleClick(Player player, int slot) {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();

        switch (slot) {
            case 0 -> { // Taille
                int current = game.getGrid().getSize();
                int next = current == 3 ? 5 : current == 5 ? 7 : 3;
                game.getGrid().setSize(next);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 2 -> { // Difficulté
                Difficulty[] vals = Difficulty.values();
                int next = (game.getDifficulty().ordinal() + 1) % vals.length;
                game.setDifficulty(vals[next]);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 4 -> { // Mode
                game.setMode(game.getMode().next());
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 6 -> { // Random Teams
                Bukkit.dispatchCommand(player, "team random 4");
                player.closeInventory();
            }
            case 8 -> { // Lock/Unlock
                boolean locked = BingoPlugin.getInstance().getTeamManager().isTeamsLocked();
                BingoPlugin.getInstance().getTeamManager().setTeamsLocked(!locked);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 12 -> { // Générer
                player.closeInventory();
                game.getGrid().generateRandomGrid(game.getMode(), game.getDifficulty());
                new DatapackManager().generateAdvancementsDatapack(game.getGrid());
                player.sendMessage("§a§lGrille générée ! §7(" + game.getGrid().getSize() + "x" + game.getGrid().getSize() +
                        ", " + game.getDifficulty().getDisplayName() + ", " + game.getMode().getDisplayName() + ")");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
            }
            case 14 -> { // Start
                player.closeInventory();
                if (game.getGrid().getObjectives().isEmpty()) {
                    player.sendMessage("§c§lERREUR : §cGénère d'abord une grille !");
                    return;
                }
                game.startParty();
            }
            case 22 -> { // Reset
                player.closeInventory();
                game.resetGame();
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1f);
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
