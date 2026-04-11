package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.game.*;
import fr.bingo.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * GUI de configuration admin. S'ouvre via le compas dans la hotbar.
 * 45 slots (5 rangées) — design harmonieux.
 */
public class AdminConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public AdminConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 45, "§6§l⚙ Configuration Bingo");
        populate();
    }

    private void populate() {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        inventory.clear();

        // ── Bordure décorative ──
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "§r", List.of());
        for (int i = 0; i < 9; i++) inventory.setItem(i, border);     // Rangée 1 haut
        for (int i = 36; i < 45; i++) inventory.setItem(i, border);   // Rangée 5 bas
        // Côtés
        for (int row = 1; row <= 3; row++) {
            inventory.setItem(row * 9, border);
            inventory.setItem(row * 9 + 8, border);
        }

        // ── Rangée 2 (slots 9-17) : Paramètres de jeu ──

        // Slot 10 : Taille de la grille
        int size = game.getGrid().getSize();
        ItemStack sizeItem = createItem(Material.MAP, "§e§lTaille : §b" + size + "x" + size,
                List.of("§7Clic pour changer",
                        "",
                        (size == 3 ? "§b▸ " : "§7  ") + "3x3",
                        (size == 5 ? "§b▸ " : "§7  ") + "5x5",
                        (size == 7 ? "§b▸ " : "§7  ") + "7x7"));
        inventory.setItem(10, sizeItem);

        // Slot 12 : Difficulté
        Difficulty diff = game.getDifficulty();
        Material diffMat = switch (diff) {
            case EASY -> Material.LIME_DYE;
            case MEDIUM -> Material.YELLOW_DYE;
            case HARD -> Material.RED_DYE;
            case EXTREME -> Material.WITHER_SKELETON_SKULL;
        };
        ItemStack diffItem = createItem(diffMat, "§e§lDifficulté : " + diff.getColor() + diff.getDisplayName(),
                List.of("§7Clic pour changer",
                        "",
                        (diff == Difficulty.EASY ? "§a▸ " : "§7  ") + "Facile",
                        (diff == Difficulty.MEDIUM ? "§e▸ " : "§7  ") + "Normal",
                        (diff == Difficulty.HARD ? "§c▸ " : "§7  ") + "Difficile",
                        (diff == Difficulty.EXTREME ? "§4▸ " : "§7  ") + "Extrême"));
        inventory.setItem(12, diffItem);

        // Slot 14 : Mode
        BingoMode mode = game.getMode();
        Material modeMat = switch (mode) {
            case ITEMS -> Material.CHEST;
            case ACHIEVEMENTS -> Material.DRAGON_EGG;
            case MIXED -> Material.ENDER_CHEST;
        };
        ItemStack modeItem = createItem(modeMat, "§e§lMode : " + mode.getColor() + mode.getDisplayName(),
                List.of("§7Clic pour changer",
                        "",
                        (mode == BingoMode.ITEMS ? "§b▸ " : "§7  ") + "Items",
                        (mode == BingoMode.ACHIEVEMENTS ? "§d▸ " : "§7  ") + "Achievements",
                        (mode == BingoMode.MIXED ? "§6▸ " : "§7  ") + "Mixte"));
        inventory.setItem(14, modeItem);

        // Slot 16 : Durée
        int duration = game.getGameDurationMinutes();
        String durationStr = duration >= 60
                ? (duration / 60) + "h" + (duration % 60 > 0 ? String.format("%02d", duration % 60) : "")
                : duration + "min";
        ItemStack timeItem = createItem(Material.CLOCK, "§e§lDurée : §b" + durationStr,
                List.of("§7Clic pour changer",
                        "",
                        (duration == 60 ? "§b▸ " : "§7  ") + "1h",
                        (duration == 90 ? "§b▸ " : "§7  ") + "1h30",
                        (duration == 120 ? "§b▸ " : "§7  ") + "2h"));
        inventory.setItem(16, timeItem);

        // ── Rangée 3 (slots 18-26) : Équipes / PVP / Fin ──

        // Slot 19 : Équipes (sous-menu)
        String teamLabel;
        if (tm.isSoloMode()) {
            teamLabel = "§a§lSOLO / FFA";
        } else {
            teamLabel = "§b§l" + tm.getActiveTeamCount() + " × " + tm.getMaxPlayersPerTeam();
        }
        ItemStack teamItem = createItem(Material.WHITE_BANNER, "§e§l👥 Équipes : " + teamLabel,
                List.of("§7Configurer les équipes",
                        "",
                        tm.isSoloMode() ? "§a  Mode Solo / FFA" : "§b  " + tm.getActiveTeamCount() + " équipes de " + tm.getMaxPlayersPerTeam() + " joueurs",
                        "",
                        "§e► Clic pour ouvrir le sous-menu"));
        inventory.setItem(19, teamItem);

        // Slot 21 : PVP Config (sous-menu)
        boolean pvpOff = game.isPvpDisabled();
        int pvpTimer = game.getPvpTimerMinutes();
        String pvpLabel;
        Material pvpMat;
        if (pvpOff) {
            pvpLabel = "§c§lDÉSACTIVÉ";
            pvpMat = Material.BARRIER;
        } else if (pvpTimer == 0) {
            pvpLabel = "§a§lIMMÉDIAT";
            pvpMat = Material.DIAMOND_SWORD;
        } else {
            pvpLabel = "§e§l" + pvpTimer + " min";
            pvpMat = Material.IRON_SWORD;
        }
        ItemStack pvpItem = createItemHidden(pvpMat, "§e§l⚔ PVP : " + pvpLabel,
                List.of("§7Configurer le PVP",
                        "",
                        pvpOff ? "§c  PVP désactivé" : (pvpTimer == 0 ? "§a  PVP dès le début" : "§e  PVP après " + pvpTimer + " min"),
                        "",
                        "§e► Clic pour ouvrir le sous-menu"));
        inventory.setItem(21, pvpItem);

        // Slot 23 : Mode de fin
        EndMode endMode = game.getEndMode();
        Material endMat = endMode == EndMode.ALL_TEAMS ? Material.HOPPER : Material.GOLDEN_SWORD;
        String endLabel = endMode == EndMode.ALL_TEAMS
                ? "§a§lToutes les équipes"
                : "§c§lDernière debout";
        ItemStack endItem = createItemHidden(endMat, "§e§l🏁 Fin : " + endLabel,
                List.of("§7Mode de fin de partie",
                        "",
                        (endMode == EndMode.ALL_TEAMS ? "§a▸ " : "§7  ") + "Toutes les équipes doivent finir",
                        (endMode == EndMode.LAST_STANDING ? "§c▸ " : "§7  ") + "Dernière équipe debout",
                        "",
                        "§e► Clic pour changer"));
        inventory.setItem(23, endItem);

        // Slot 25 : Lock/Unlock Teams
        boolean locked = tm.isTeamsLocked();
        ItemStack lockItem = createItem(locked ? Material.BARRIER : Material.OAK_DOOR,
                locked ? "§c§l🔒 Équipes Verrouillées" : "§a§l🔓 Équipes Ouvertes",
                List.of("§7Clic pour " + (locked ? "déverrouiller" : "verrouiller")));
        inventory.setItem(25, lockItem);

        // ── Rangée 4 (slots 27-35) : Actions ──

        // Slot 29 : GÉNÉRER
        ItemStack genItem = createItem(Material.NETHER_STAR, "§a§l✦ GÉNÉRER LA GRILLE",
                List.of("§7Génère une nouvelle grille",
                        "§7avec les paramètres actuels",
                        "",
                        "§7Taille : §b" + size + "x" + size,
                        "§7Difficulté : " + diff.getColor() + diff.getDisplayName(),
                        "§7Mode : " + mode.getColor() + mode.getDisplayName(),
                        "§7Durée : §b" + durationStr));
        inventory.setItem(29, genItem);

        // Slot 31 : START
        ItemStack startItem = createItem(Material.LIME_CONCRETE, "§a§l▶ LANCER LA PARTIE",
                List.of("§7Démarre le décompte et lance le Bingo !"));
        inventory.setItem(31, startItem);

        // Slot 33 : RESET
        ItemStack resetItem = createItem(Material.TNT, "§c§l↻ RESET",
                List.of("§7Réinitialise la partie", "§7Remet tout à zéro"));
        inventory.setItem(33, resetItem);
    }

    /**
     * Gère les clics dans le GUI admin.
     * @param isRightClick true si le joueur a fait un clic droit
     */
    public void handleClick(Player player, int slot, boolean isRightClick) {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();

        switch (slot) {
            case 10 -> { // Taille grille
                int current = game.getGrid().getSize();
                int next = current == 3 ? 5 : current == 5 ? 7 : 3;
                game.getGrid().setSize(next);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 12 -> { // Difficulté
                Difficulty[] vals = Difficulty.values();
                int next = (game.getDifficulty().ordinal() + 1) % vals.length;
                game.setDifficulty(vals[next]);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 14 -> { // Mode
                game.setMode(game.getMode().next());
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 16 -> { // Durée
                int current = game.getGameDurationMinutes();
                int next = current == 60 ? 90 : current == 90 ? 120 : 60;
                game.setGameDurationMinutes(next);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 19 -> { // Équipes → sous-menu
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new TeamConfigGUI().getInventory());
            }
            case 21 -> { // PVP → sous-menu
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new PvpConfigGUI().getInventory());
            }
            case 23 -> { // Mode de fin
                game.setEndMode(game.getEndMode().next());
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 25 -> { // Lock/Unlock
                boolean locked = tm.isTeamsLocked();
                tm.setTeamsLocked(!locked);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 29 -> { // Générer
                player.closeInventory();
                game.getGrid().generateRandomGrid(game.getMode(), game.getDifficulty());
                new DatapackManager().generateAdvancementsDatapack(game.getGrid(), game.getMode());
                player.sendMessage("§a§lGrille générée ! §7(" + game.getGrid().getSize() + "x" + game.getGrid().getSize() +
                        ", " + game.getDifficulty().getDisplayName() + ", " + game.getMode().getDisplayName() + ")");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
            }
            case 31 -> { // Start
                player.closeInventory();
                if (game.getGrid().getObjectives().isEmpty()) {
                    player.sendMessage("§c§lERREUR : §cGénère d'abord une grille !");
                    return;
                }
                game.startParty();
            }
            case 33 -> { // Reset
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

    /**
     * Crée un item en cachant les attributs (pas de "When in Main Hand: 6 Attack Damage")
     */
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
