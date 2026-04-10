package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.game.BingoGrid;
import fr.bingo.game.BingoObjective;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
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

/**
 * GUI coffre affichant la grille de Bingo de manière interactive.
 * Supporte les grilles de 3x3 à 7x7 dynamiquement.
 * Pour 7x7, on affiche 7 colonnes sur 6 rangées max (42 items) et la 7ème rangée en bas.
 */
public class BingoGridGUI implements InventoryHolder {

    private final Inventory inventory;

    public BingoGridGUI(Player player) {
        BingoGrid grid = BingoPlugin.getInstance().getBingoGame().getGrid();
        int gridSize = grid.getSize();

        // Calcul de la taille de l'inventaire
        int rows = Math.min(gridSize + 1, 6); // +1 pour le padding, max 6 rangées
        int invSize = rows * 9;

        this.inventory = Bukkit.createInventory(this, invSize, "§b§lBingo Classique §8— " + gridSize + "x" + gridSize);
        setupInventory(player, grid);
    }

    private void setupInventory(Player player, BingoGrid grid) {
        int gridSize = grid.getSize();
        List<BingoObjective> objectives = grid.getObjectives();

        // Fond : verre bleu clair
        ItemStack background = createBackground(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, background);
        }

        if (objectives.isEmpty()) {
            ItemStack noGrid = new ItemStack(Material.BARRIER);
            ItemMeta m = noGrid.getItemMeta();
            if (m != null) {
                m.setDisplayName("§c§lAucune grille générée");
                List<String> lore = new ArrayList<>();
                lore.add("§7Utilisez §e/bingo generate §7pour créer la grille.");
                m.setLore(lore);
                noGrid.setItemMeta(m);
            }
            inventory.setItem(22, noGrid);
            return;
        }

        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();
        BingoTeam myTeam = teamManager.getPlayerTeam(player);

        // Calculer les slots centrés pour la grille
        int[] slots = calculateGridSlots(gridSize);

        for (int i = 0; i < objectives.size() && i < slots.length; i++) {
            BingoObjective obj = objectives.get(i);
            int slot = slots[i];
            if (slot < 0 || slot >= inventory.getSize()) continue;

            ItemStack item = new ItemStack(obj.getDisplayMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = obj.getId().replace("_", " ");
                displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);

                // Vérifier si une équipe a trouvé cet item
                List<String> lore = new ArrayList<>();
                boolean foundByMyTeam = false;
                boolean foundByAny = false;

                for (BingoTeam team : teamManager.getTeams()) {
                    if (team.hasUnlocked(obj.getId())) {
                        foundByAny = true;
                        lore.add(team.getChatColor() + "  ✔ Équipe " + team.getName());
                        if (team == myTeam) foundByMyTeam = true;
                    }
                }

                if (foundByMyTeam) {
                    meta.setDisplayName("§a§l✔ " + displayName);
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.add(0, "§a§lTROUVÉ !");
                    lore.add(1, "");
                } else if (foundByAny) {
                    meta.setDisplayName("§e" + displayName);
                    lore.add(0, "§eTrouvé par d'autres équipes");
                    lore.add(1, "");
                } else {
                    meta.setDisplayName("§f" + displayName);
                    lore.add("§8Aucune équipe n'a trouvé cet item");
                }

                meta.setLore(lore);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot, item);
        }
    }

    /**
     * Calcule les slots d'inventaire pour centrer une grille NxN dans un coffre 9xR.
     */
    private int[] calculateGridSlots(int gridSize) {
        // Offset horizontal pour centrer : (9 - gridSize) / 2
        int colOffset = (9 - gridSize) / 2;

        int totalItems = gridSize * gridSize;
        int[] slots = new int[totalItems];

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int index = row * gridSize + col;
                int inventoryRow = row; // Commence à la rangée 0

                // Si la grille est trop grande (row >= 6), on déborde
                if (inventoryRow >= 6) {
                    slots[index] = -1; // Pas affichable
                } else {
                    slots[index] = inventoryRow * 9 + colOffset + col;
                }
            }
        }
        return slots;
    }

    private ItemStack createBackground(Material material) {
        ItemStack bg = new ItemStack(material);
        ItemMeta meta = bg.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            bg.setItemMeta(meta);
        }
        return bg;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
