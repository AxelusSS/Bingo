package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.game.*;
import fr.hel.team.TeamManager;
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
 * 45 slots (5 rang\u00E9es) \u2014 design harmonieux.
 */
public class AdminConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public AdminConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 45, "\u00A76\u00A7l\u2699 Configuration HEL");
        populate();
    }

    private void populate() {
        HelGame game = HelPlugin.getInstance().getHelGame();
        TeamManager tm = HelPlugin.getInstance().getTeamManager();
        inventory.clear();

        // \u2500\u2500 Bordure d\u00E9corative \u2500\u2500
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "\u00A7r", List.of());
        for (int i = 0; i < 9; i++) inventory.setItem(i, border);     // Rang\u00E9e 1 haut
        for (int i = 36; i < 45; i++) inventory.setItem(i, border);   // Rang\u00E9e 5 bas
        // C\u00F4t\u00E9s
        for (int row = 1; row <= 3; row++) {
            inventory.setItem(row * 9, border);
            inventory.setItem(row * 9 + 8, border);
        }

        // \u2500\u2500 Rang\u00E9e 2 (slots 10-16) : Param\u00E8tres principaux \u2500\u2500

        // Slot 10 : \u00C9quipes (sous-menu)
        String teamLabel;
        if (tm.isSoloMode()) {
            teamLabel = "\u00A7a\u00A7lSOLO / FFA";
        } else {
            teamLabel = "\u00A7b\u00A7l" + tm.getActiveTeamCount() + " \u00D7 " + tm.getMaxPlayersPerTeam();
        }
        ItemStack teamItem = createItem(Material.WHITE_BANNER, "\u00A7e\u00A7l\u1F465 \u00C9quipes : " + teamLabel,
                List.of("\u00A77Configurer les \u00E9quipes",
                        "",
                        tm.isSoloMode() ? "\u00A7a  Mode Solo / FFA" : "\u00A7b  " + tm.getActiveTeamCount() + " \u00E9quipes de " + tm.getMaxPlayersPerTeam() + " joueurs",
                        "",
                        "\u00A7e\u25BA Clic pour ouvrir le sous-menu"));
        inventory.setItem(10, teamItem);

        // Slot 12 : Bordure Config
        BorderManager bm = HelPlugin.getInstance().getBorderManager();
        ItemStack borderItem = createItem(Material.GLASS, "\u00A7e\u00A7l\u1F4CF Bordure",
                List.of("\u00A77Taille initiale : \u00A7b" + bm.getInitialSize() + "x" + bm.getInitialSize(),
                        "\u00A77Taille finale : \u00A7b" + bm.getFinalSize() + "x" + bm.getFinalSize(),
                        "\u00A77R\u00E9duction apr\u00E8s : \u00A7b" + bm.getTimeBeforeShrinkMinutes() + " min",
                        "\u00A77Temps de r\u00E9duction : \u00A7b" + bm.getShrinkTimeMinutes() + " min",
                        "",
                        "\u00A7e\u25BA Clic pour configurer"));
        inventory.setItem(12, borderItem);

        // Slot 14 : Dur\u00E9e
        int duration = game.getGameDurationMinutes();
        String durationStr = duration >= 60
                ? (duration / 60) + "h" + (duration % 60 > 0 ? String.format("%02d", duration % 60) : "")
                : duration + "min";
        ItemStack timeItem = createItem(Material.CLOCK, "\u00A7e\u00A7lDur\u00E9e : \u00A7b" + durationStr,
                List.of("\u00A77Temps avant la fin ou le meetup",
                        "",
                        (duration == 60 ? "\u00A7b\u25B8 " : "\u00A77  ") + "1h",
                        (duration == 90 ? "\u00A7b\u25B8 " : "\u00A77  ") + "1h30",
                        (duration == 120 ? "\u00A7b\u25B8 " : "\u00A77  ") + "2h"));
        inventory.setItem(14, timeItem);

        // Slot 16 : PVP Config (sous-menu)
        boolean pvpOff = game.isPvpDisabled();
        int pvpTimer = game.getPvpTimerMinutes();
        String pvpLabel;
        Material pvpMat;
        if (pvpOff) {
            pvpLabel = "\u00A7c\u00A7lD\u00C9SACTIV\u00C9";
            pvpMat = Material.BARRIER;
        } else if (pvpTimer == 0) {
            pvpLabel = "\u00A7a\u00A7lIMM\u00C9DIAT";
            pvpMat = Material.DIAMOND_SWORD;
        } else {
            pvpLabel = "\u00A7e\u00A7l" + pvpTimer + " min";
            pvpMat = Material.IRON_SWORD;
        }
        ItemStack pvpItem = createItemHidden(pvpMat, "\u00A7e\u00A7l\u2694 PVP : " + pvpLabel,
                List.of("\u00A77Configurer l'activation du PVP",
                        "",
                        pvpOff ? "\u00A7c  PVP d\u00E9sactiv\u00E9" : (pvpTimer == 0 ? "\u00A7a  PVP d\u00E8s le d\u00E9but" : "\u00A7e  PVP apr\u00E8s " + pvpTimer + " min"),
                        "",
                        "\u00A7e\u25BA Clic pour ouvrir le sous-menu"));
        inventory.setItem(16, pvpItem);

        // \u2500\u2500 Rang\u00E9e 3 (slots 19-25) : Sc\u00E9narios / Presets / Inv / Fin \u2500\u2500

        // Slot 19 : Sc\u00E9narios
        int activeScenarios = 0;
        for (fr.hel.scenario.Scenario s : HelPlugin.getInstance().getScenarioManager().getScenarios()) {
            if (s.isEnabled()) activeScenarios++;
        }
        ItemStack scenariosItem = createItemHidden(Material.COMMAND_BLOCK,
                "\u00A7e\u00A7l\u1F4DC Sc\u00E9narios",
                List.of("\u00A77G\u00E9rer les sc\u00E9narios actifs",
                        "",
                        "\u00A7b\u25B8 " + activeScenarios + " sc\u00E9nario(s) activ\u00E9(s)",
                        "",
                        "\u00A7e\u25BA Clic pour ouvrir le sous-menu"));
        inventory.setItem(19, scenariosItem);

        // Slot 21 : Presets
        ItemStack presetItem = createItemHidden(Material.BOOK, "\u00A7d\u00A7lSauvegardes & Presets",
                List.of("\u00A77G\u00E9rer les configurations",
                        "\u00A77du jeu.",
                        "",
                        "\u00A7e\u25BA Clic pour ouvrir"));
        inventory.setItem(21, presetItem);

        // Slot 23 : Inventaire de d\u00E9part
        ItemStack invItem = createItemHidden(Material.CHEST, "\u00A7a\u00A7l\u1F392 Inventaire de D\u00E9part",
                List.of("\u00A77Configurer l'inventaire donn\u00E9",
                        "\u00A77aux joueurs au lancement.",
                        "",
                        "\u00A7e\u25BA Clic pour \u00E9diter l'inventaire"));
        inventory.setItem(23, invItem);

        // Slot 25 : Mode de fin
        EndMode endMode = game.getEndMode();
        Material endMat = endMode == EndMode.ALL_TEAMS ? Material.HOPPER : endMode == EndMode.LAST_STANDING ? Material.GOLDEN_SWORD : Material.GOLD_INGOT;
        String endLabel = endMode == EndMode.ALL_TEAMS
                ? "\u00A7a\u00A7lToutes les \u00E9quipes"
                : endMode == EndMode.LAST_STANDING ? "\u00A7c\u00A7lDerni\u00E8re debout" : "\u00A7e\u00A7lPremier \u00E0 finir";
        ItemStack endItem = createItemHidden(endMat, "\u00A7e\u00A7l\u1F3C1 Fin : " + endLabel,
                List.of("\u00A77Mode de fin de partie",
                        "",
                        (endMode == EndMode.ALL_TEAMS ? "\u00A7a\u25B8 " : "\u00A77  ") + "Toutes les \u00E9quipes doivent finir",
                        (endMode == EndMode.LAST_STANDING ? "\u00A7c\u25B8 " : "\u00A77  ") + "Derni\u00E8re \u00E9quipe debout",
                        (endMode == EndMode.FIRST_TO_FINISH ? "\u00A7e\u25B8 " : "\u00A77  ") + "Premi\u00E8re \u00E9quipe \u00E0 finir",
                        "\u00A7e\u25BA Clic pour changer"));
        inventory.setItem(25, endItem);

        // \u2500\u2500 Rang\u00E9e 4 (slots 27-35) : Actions \u2500\u2500

        // Slot 31 : START
        ItemStack startItem = createItem(Material.LIME_CONCRETE, "\u00A7a\u00A7l\u25B6 LANCER LA PARTIE",
                List.of("\u00A77D\u00E9marre le d\u00E9compte et lance la partie !"));
        inventory.setItem(31, startItem);

        // Slot 33 : RESET
        ItemStack resetGameItem = createItem(Material.TNT, "\u00A7c\u00A7l\u21BB RESET",
                List.of("\u00A77R\u00E9initialise la partie", "\u00A77Remet tout \u00E0 z\u00E9ro"));
        inventory.setItem(33, resetGameItem);

        // Slot 38 : Configuration du monde (G\u00E9n\u00E9ration)
        ItemStack worldItem = createItemHidden(Material.GRASS_BLOCK, "\u00A7a\u00A7l\u1F30D G\u00E9n\u00E9ration de Map",
                List.of("\u00A77Configurer la g\u00E9n\u00E9ration du monde",
                        "\u00A77(Biomes, Seed, Presets)",
                        "",
                        "\u00A7e\u25BA Clic pour ouvrir"));
        inventory.setItem(38, worldItem);
    }

    /**
     * G\u00E8re les clics dans le GUI admin.
     * @param isRightClick true si le joueur a fait un clic droit
     */
    public void handleClick(Player player, int slot, boolean isRightClick) {
        HelGame game = HelPlugin.getInstance().getHelGame();
        TeamManager tm = HelPlugin.getInstance().getTeamManager();

        switch (slot) {
            case 10 -> { // \u00C9quipes \u2192 sous-menu
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new TeamConfigGUI().getInventory());
            }
            case 12 -> { // Bordure \u2192 sous-menu
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new BorderConfigGUI().getInventory());
            }
            case 14 -> { // Dur\u00E9e
                int current = game.getGameDurationMinutes();
                int next = current == 60 ? 90 : current == 90 ? 120 : 60;
                game.setGameDurationMinutes(next);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 16 -> { // PVP \u2192 sous-menu
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new PvpConfigGUI().getInventory());
            }
            case 19 -> { // Sc\u00E9narios
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new ScenarioConfigGUI().getInventory());
            }
            case 21 -> { // Presets
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new PresetConfigGUI(player).getInventory());
            }
            case 23 -> { // Inv d\u00E9part
                player.closeInventory();
                HelPlugin.getInstance().getStarterInventoryManager().enterEditMode(player);
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
            case 38 -> { // World Config
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new WorldConfigGUI().getInventory());
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
     * Cr\u00E9e un item en cachant les attributs (pas de "When in Main Hand: 6 Attack Damage")
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
