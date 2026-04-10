package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.game.BingoObjective;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BingoGridGUI implements InventoryHolder {

    private final Inventory inventory;

    public BingoGridGUI(Player player) {
        // UI Size: 54 (6 rows). A 5x5 grid fits perfectly in the middle rows.
        this.inventory = Bukkit.createInventory(this, 54, "§5Bingo Classique");
        setupInventory(player);
    }

    private void setupInventory(Player player) {
        List<BingoObjective> objectives = BingoPlugin.getInstance().getBingoGame().getGrid().getObjectives();
        
        // Verre teinté pour fond (esthétique)
        ItemStack background = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        if (bgMeta != null) {
            bgMeta.setDisplayName(" ");
            background.setItemMeta(bgMeta);
        }

        // Remplir tout le fond
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, background);
        }

        // Si la grille n'est pas générée, on affiche un message
        if (objectives.isEmpty()) {
            return;
        }

        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();
        BingoTeam myTeam = teamManager.getPlayerTeam(player);

        // Slots pour une grille 5x5 au centre de l'inventaire 54 :
        // Rows: 1, 2, 3, 4, 5 (indices 9 to 53 minus edges)
        // Les slots centraux:
        int[] bingoSlots = {
            11, 12, 13, 14, 15,
            20, 21, 22, 23, 24,
            29, 30, 31, 32, 33,
            38, 39, 40, 41, 42,
            47, 48, 49, 50, 51
        };

        for (int i = 0; i < 25; i++) {
            if (i >= objectives.size()) break;
            
            BingoObjective obj = objectives.get(i);
            ItemStack item = new ItemStack(obj.getDisplayMaterial());
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                if (obj.isAchievement()) {
                    meta.setDisplayName("§bSuccès: " + obj.getId());
                } else {
                    // Translation key normally, simplifions :
                    meta.setDisplayName("§bObtenir un(e) " + obj.getId().replace("_", " "));
                }

                // Plus tard : On ajoute dans le lore qui a trouvé l'item
                List<String> lore = new ArrayList<>();
                lore.add("§7Trouvé par :");
                // Fake validation pour l'instant
                // lore.add("- §cEquipe Rouge");

                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(bingoSlots[i], item);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
