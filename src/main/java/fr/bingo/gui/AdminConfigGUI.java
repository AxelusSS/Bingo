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
import org.bukkit.Sound;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * GUI de configuration admin. S'ouvre via le compas dans la hotbar.
 * 45 slots (5 rangées) — design harmonieux.
 */
public class AdminConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public AdminConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 45, "§6§l⚙ Configuration UHC");
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

        // ── Rangée 2 (slots 10-16) : Paramètres principaux ──

        // Slot 10 : Équipes (sous-menu)
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
        inventory.setItem(10, teamItem);

        // Slot 12 : Bordure Config
        BorderManager bm = BingoPlugin.getInstance().getBorderManager();
        ItemStack borderItem = createItem(Material.GLASS, "§e§l📏 Bordure",
                List.of("§7Taille initiale : §b" + bm.getInitialSize() + "x" + bm.getInitialSize(),
                        "§7Taille finale : §b" + bm.getFinalSize() + "x" + bm.getFinalSize(),
                        "§7Réduction après : §b" + bm.getTimeBeforeShrinkMinutes() + " min",
                        "§7Temps de réduction : §b" + bm.getShrinkTimeMinutes() + " min",
                        "",
                        "§e► Clic pour configurer"));
        inventory.setItem(12, borderItem);

        // Slot 14 : Durée
        int duration = game.getGameDurationMinutes();
        String durationStr = duration >= 60
                ? (duration / 60) + "h" + (duration % 60 > 0 ? String.format("%02d", duration % 60) : "")
                : duration + "min";
        ItemStack timeItem = createItem(Material.CLOCK, "§e§lDurée : §b" + durationStr,
                List.of("§7Temps avant la fin ou le meetup",
                        "",
                        (duration == 60 ? "§b▸ " : "§7  ") + "1h",
                        (duration == 90 ? "§b▸ " : "§7  ") + "1h30",
                        (duration == 120 ? "§b▸ " : "§7  ") + "2h"));
        inventory.setItem(14, timeItem);

        // Slot 16 : PVP Config (sous-menu)
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
                List.of("§7Configurer l'activation du PVP",
                        "",
                        pvpOff ? "§c  PVP désactivé" : (pvpTimer == 0 ? "§a  PVP dès le début" : "§e  PVP après " + pvpTimer + " min"),
                        "",
                        "§e► Clic pour ouvrir le sous-menu"));
        inventory.setItem(16, pvpItem);

        // ── Rangée 3 (slots 19-25) : Scénarios / Presets / Inv / Fin ──

        // Slot 19 : Scénarios
        int activeScenarios = 0;
        for (fr.bingo.scenario.Scenario s : BingoPlugin.getInstance().getScenarioManager().getScenarios()) {
            if (s.isEnabled()) activeScenarios++;
        }
        ItemStack scenariosItem = createItemHidden(Material.COMMAND_BLOCK,
                "§e§l📜 Scénarios",
                List.of("§7Gérer les scénarios actifs",
                        "",
                        "§b▸ " + activeScenarios + " scénario(s) activé(s)",
                        "",
                        "§e► Clic pour ouvrir le sous-menu"));
        inventory.setItem(19, scenariosItem);

        // Slot 21 : Presets
        ItemStack presetItem = createItemHidden(Material.BOOK, "§d§lSauvegardes & Presets",
                List.of("§7Gérer les configurations",
                        "§7du jeu.",
                        "",
                        "§e► Clic pour ouvrir"));
        inventory.setItem(21, presetItem);

        // Slot 23 : Inventaire de départ
        ItemStack invItem = createItemHidden(Material.CHEST, "§a§l🎒 Inventaire de Départ",
                List.of("§7Configurer l'inventaire donné",
                        "§7aux joueurs au lancement.",
                        "",
                        "§e► Clic pour éditer l'inventaire"));
        inventory.setItem(23, invItem);

        // Slot 25 : Mode de fin
        EndMode endMode = game.getEndMode();
        Material endMat = endMode == EndMode.ALL_TEAMS ? Material.HOPPER : endMode == EndMode.LAST_STANDING ? Material.GOLDEN_SWORD : Material.GOLD_INGOT;
        String endLabel = endMode == EndMode.ALL_TEAMS
                ? "§a§lToutes les équipes"
                : endMode == EndMode.LAST_STANDING ? "§c§lDernière debout" : "§e§lPremier à finir";
        ItemStack endItem = createItemHidden(endMat, "§e§l🏁 Fin : " + endLabel,
                List.of("§7Mode de fin de partie",
                        "",
                        (endMode == EndMode.ALL_TEAMS ? "§a▸ " : "§7  ") + "Toutes les équipes doivent finir",
                        (endMode == EndMode.LAST_STANDING ? "§c▸ " : "§7  ") + "Dernière équipe debout",
                        (endMode == EndMode.FIRST_TO_FINISH ? "§e▸ " : "§7  ") + "Première équipe à finir",
                        "",
                        "§e► Clic pour changer"));
        inventory.setItem(25, endItem);

        // ── Rangée 4 (slots 27-35) : Actions ──

        // Slot 31 : START
        ItemStack startItem = createItem(Material.LIME_CONCRETE, "§a§l▶ LANCER LA PARTIE",
                List.of("§7Démarre le décompte et lance la partie !"));
        inventory.setItem(31, startItem);

        // Slot 33 : RESET
        ItemStack resetGameItem = createItem(Material.TNT, "§c§l↻ RESET",
                List.of("§7Réinitialise la partie", "§7Remet tout à zéro"));
        inventory.setItem(33, resetGameItem);

        // Slot 40 : Réinitialisation du monde
        ItemStack resetItem = createItemHidden(Material.TNT, "§c§l💥 RÉINITIALISER LA MAP 💥",
                List.of("§7Supprime la carte actuelle au redémarrage",
                        "§7et génère un nouveau monde aléatoire.",
                        "",
                        "§c⚠ ACTION IRRÉVERSIBLE ⚠",
                        "",
                        "§e► Clic pour lancer la procédure"));
        inventory.setItem(40, resetItem);
    }

    /**
     * Gère les clics dans le GUI admin.
     * @param isRightClick true si le joueur a fait un clic droit
     */
    public void handleClick(Player player, int slot, boolean isRightClick) {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();

        switch (slot) {
            case 10 -> { // Équipes → sous-menu
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new TeamConfigGUI().getInventory());
            }
            case 12 -> { // Bordure → sous-menu
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new BorderConfigGUI().getInventory());
            }
            case 14 -> { // Durée
                int current = game.getGameDurationMinutes();
                int next = current == 60 ? 90 : current == 90 ? 120 : 60;
                game.setGameDurationMinutes(next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 16 -> { // PVP → sous-menu
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new PvpConfigGUI().getInventory());
            }
            case 19 -> { // Scénarios
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new ScenarioConfigGUI().getInventory());
            }
            case 21 -> { // Presets
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new PresetConfigGUI(player).getInventory());
            }
            case 23 -> { // Inv départ
                player.closeInventory();
                BingoPlugin.getInstance().getStarterInventoryManager().enterEditMode(player);
            }
            case 25 -> { // Mode de fin
                game.setEndMode(game.getEndMode().next());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 31 -> { // Start
                player.closeInventory();
                game.startParty();
            }
            case 33 -> { // Reset
                player.closeInventory();
                game.resetGame();
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1f);
            }
            case 40 -> { // World reset
                game.prepareWorldReset(player);
                player.closeInventory();
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
