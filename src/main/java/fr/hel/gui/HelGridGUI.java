package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.game.HelGrid;
import fr.hel.game.HelObjective;
import fr.hel.team.HelTeam;
import fr.hel.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI coffre affichant la grille de Hel de mani\u00E8re interactive.
 * Supporte les grilles de 3x3 \u00E0 7x7 dynamiquement.
 * Pour 7x7, on affiche 7 colonnes sur 6 rang\u00E9es max (42 items) et la 7\u00E8me rang\u00E9e en bas.
 */
public class HelGridGUI implements InventoryHolder {

    private final Inventory inventory;

    public HelGridGUI(Player player) {
        HelGrid grid = HelPlugin.getInstance().getHelGame().getGrid();
        int gridSize = grid.getSize();

        // Calcul de la taille de l'inventaire
        int rows = Math.min(gridSize + 1, 6); // +1 pour le padding, max 6 rang\u00E9es
        int invSize = rows * 9;

        this.inventory = Bukkit.createInventory(this, invSize, "\u00A7b\u00A7lHel Classique \u00A78\u2014 " + gridSize + "x" + gridSize);
        setupInventory(player, grid);
    }

    private void setupInventory(Player player, HelGrid grid) {
        int gridSize = grid.getSize();
        List<HelObjective> objectives = grid.getObjectives();

        // Fond : verre bleu clair
        ItemStack background = createBackground(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, background);
        }

        if (objectives.isEmpty()) {
            ItemStack noGrid = new ItemStack(Material.BARRIER);
            ItemMeta m = noGrid.getItemMeta();
            if (m != null) {
                m.setDisplayName("\u00A7c\u00A7lAucune grille g\u00E9n\u00E9r\u00E9e");
                List<String> lore = new ArrayList<>();
                lore.add("\u00A77Utilisez \u00A7e/bingo generate \u00A77pour cr\u00E9er la grille.");
                m.setLore(lore);
                noGrid.setItemMeta(m);
            }
            inventory.setItem(22, noGrid);
            return;
        }

        TeamManager teamManager = HelPlugin.getInstance().getTeamManager();
        HelTeam myTeam = teamManager.getPlayerTeam(player);

        // Calculer les slots centr\u00E9s pour la grille
        int[] slots = calculateGridSlots(gridSize);

        for (int i = 0; i < objectives.size() && i < slots.length; i++) {
            HelObjective obj = objectives.get(i);
            int slot = slots[i];
            if (slot < 0 || slot >= inventory.getSize()) continue;

            ItemStack item = new ItemStack(obj.getDisplayMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = obj.getId().replace("_", " ");
                displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
                
                // --- Gestion sp\u00E9ciale des POTIONS ---
                if (obj.getId().startsWith("POTION_")) {
                    applyPotionMeta(item, obj.getId());
                    meta = item.getItemMeta(); // Update meta reference to keep PotionMeta
                    displayName = getPotionDisplayName(obj.getId());
                }

                // V\u00E9rifier si une \u00E9quipe a trouv\u00E9 cet item
                List<String> lore = new ArrayList<>();
                boolean foundByMyTeam = false;
                boolean foundByAny = false;

                for (HelTeam team : teamManager.getTeams()) {
                    if (team.hasUnlocked(obj.getId())) {
                        foundByAny = true;
                        if (teamManager.isSoloMode() && !team.getPlayers().isEmpty()) {
                            // En FFA, on affiche le pseudo du joueur en blanc
                            org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(team.getPlayers().get(0));
                            lore.add("\u00A7f  \u2714 " + (op.getName() != null ? op.getName() : team.getName()));
                        } else {
                            lore.add(team.getChatColor() + "  \u2714 \u00C9quipe " + team.getName());
                        }
                        if (team == myTeam) foundByMyTeam = true;
                    }
                }

                if (foundByMyTeam) {
                    meta.setDisplayName("\u00A7a\u00A7l\u2714 " + displayName);
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.add(0, "\u00A7a\u00A7lTROUV\u00C9 !");
                    lore.add(1, "");
                } else if (foundByAny) {
                    meta.setDisplayName("\u00A7e" + displayName);
                    lore.add(0, "\u00A7eTrouv\u00E9 par d'autres \u00E9quipes");
                    lore.add(1, "");
                } else {
                    meta.setDisplayName("\u00A7f" + displayName);
                    lore.add("\u00A78Aucune \u00E9quipe n'a trouv\u00E9 cet item");
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
                int inventoryRow = row; // Commence \u00E0 la rang\u00E9e 0

                // Si la grille est trop grande (row >= 6), on d\u00E9borde
                if (inventoryRow >= 6) {
                    slots[index] = -1; // Pas affichable
                } else {
                    slots[index] = inventoryRow * 9 + colOffset + col;
                }
            }
        }
        return slots;
    }

    // --- UTILS POTIONS ---
    
    private void applyPotionMeta(ItemStack item, String id) {
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return;
        
        PotionType type;
        
        if (id.contains("SPEED")) {
            if (id.endsWith("_2")) type = PotionType.STRONG_SWIFTNESS;
            else if (id.endsWith("_EXT")) type = PotionType.LONG_SWIFTNESS;
            else type = PotionType.SWIFTNESS;
        } else if (id.contains("STRENGTH")) {
            if (id.endsWith("_2")) type = PotionType.STRONG_STRENGTH;
            else if (id.endsWith("_EXT")) type = PotionType.LONG_STRENGTH;
            else type = PotionType.STRENGTH;
        } else if (id.contains("JUMP")) {
            if (id.endsWith("_2")) type = PotionType.STRONG_LEAPING;
            else if (id.endsWith("_EXT")) type = PotionType.LONG_LEAPING;
            else type = PotionType.LEAPING;
        } else if (id.contains("FIRE_RES")) {
            if (id.endsWith("_EXT")) type = PotionType.LONG_FIRE_RESISTANCE;
            else type = PotionType.FIRE_RESISTANCE;
        } else if (id.contains("WATER_BREATH")) {
            if (id.endsWith("_EXT")) type = PotionType.LONG_WATER_BREATHING;
            else type = PotionType.WATER_BREATHING;
        } else if (id.contains("REGEN")) {
            if (id.endsWith("_2")) type = PotionType.STRONG_REGENERATION;
            else if (id.endsWith("_EXT")) type = PotionType.LONG_REGENERATION;
            else type = PotionType.REGENERATION;
        } else if (id.contains("INVIS")) {
            if (id.endsWith("_EXT")) type = PotionType.LONG_INVISIBILITY;
            else type = PotionType.INVISIBILITY;
        } else if (id.contains("NIGHT_VIS")) {
            if (id.endsWith("_EXT")) type = PotionType.LONG_NIGHT_VISION;
            else type = PotionType.NIGHT_VISION;
        } else {
            type = PotionType.AWKWARD;
        }
        
        meta.setBasePotionType(type);
        item.setItemMeta(meta);
    }
    
    private String getPotionDisplayName(String id) {
        String base = "Potion de ";
        if (id.contains("SPEED")) base += "Vitesse";
        else if (id.contains("STRENGTH")) base += "Force";
        else if (id.contains("JUMP")) base += "Saut";
        else if (id.contains("FIRE_RES")) base += "Resistance au Feu";
        else if (id.contains("WATER_BREATH")) base += "Respiration Aquatique";
        else if (id.contains("REGEN")) base += "Regeneration";
        else if (id.contains("INVIS")) base += "Invisibilite";
        else if (id.contains("NIGHT_VIS")) base += "Vision Nocturne";
        
        if (id.endsWith("_2")) base += " II";
        if (id.endsWith("_EXT")) base += " (Allongee)";
        
        return "\u00A7d" + base;
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
