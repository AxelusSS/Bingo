package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.team.HelTeam;
import fr.hel.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sous-menu de configuration des \u00E9quipes.
 * B\u00E9ton rouge/vert pour les boutons (100% fiable).
 */
public class TeamConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public TeamConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 45, "\u00A7b\u00A7l\u1F465 Configuration \u00C9quipes");
        populate();
    }

    private void populate() {
        TeamManager tm = HelPlugin.getInstance().getTeamManager();
        inventory.clear();

        // Bordure d\u00E9corative
        ItemStack border = createItem(Material.BLUE_STAINED_GLASS_PANE, "\u00A7r", List.of());
        for (int i = 0; i < 9; i++) inventory.setItem(i, border);
        for (int i = 36; i < 45; i++) inventory.setItem(i, border);
        inventory.setItem(9, border);
        inventory.setItem(17, border);
        inventory.setItem(18, border);
        inventory.setItem(26, border);
        inventory.setItem(27, border);
        inventory.setItem(35, border);

        int teamCount = tm.getActiveTeamCount();
        int teamSize = tm.getMaxPlayersPerTeam();
        boolean isSolo = tm.isSoloMode();

        // \u2500\u2500 Slot 2 : Mode Solo / \u00C9quipe \u2500\u2500
        ItemStack soloItem;
        if (isSolo) {
            soloItem = createItemHidden(Material.IRON_SWORD, "\u00A7e\u00A7lMode : \u00A7a\u00A7lSOLO / FFA",
                    List.of("\u00A77Chacun pour soi !",
                            "\u00A77Chaque joueur est seul",
                            "",
                            "\u00A7e\u25BA Clic pour passer en mode \u00C9quipe"));
        } else {
            soloItem = createItemHidden(Material.SHIELD, "\u00A7e\u00A7lMode : \u00A7b\u00A7l\u00C9QUIPES",
                    List.of("\u00A77Jouez en \u00E9quipes !",
                            "\u00A77" + teamCount + " \u00E9quipes de " + teamSize + " joueurs max",
                            "",
                            "\u00A7e\u25BA Clic pour passer en mode Solo/FFA"));
        }
        inventory.setItem(2, soloItem);

        if (!isSolo) {
            // \u2500\u2500 Nombre d'\u00E9quipes \u2500\u2500
            // Slot 12 : Bouton rouge -
            inventory.setItem(12, createItem(Material.RED_CONCRETE, "\u00A7c\u00A7l\u2296 Moins d'\u00E9quipes",
                    List.of("\u00A7a\u2296 Clic gauche \u00A77\u2192 \u00A7c-1",
                            "\u00A7e\u2296 Clic droit \u00A77\u2192 \u00A7c-2",
                            "",
                            "\u00A77Min : \u00A7b2")));

            // Slot 13 : Nombre actuel
            ItemStack countItem = createItem(Material.WHITE_BANNER, "\u00A7e\u00A7l\u00C9quipes : \u00A7b\u00A7l" + teamCount,
                    List.of("\u00A77Nombre d'\u00E9quipes actives",
                            "",
                            "\u00A77\u00C9quipes :",
                            getTeamListPreview(tm)));
            countItem.setAmount(Math.min(teamCount, 64));
            inventory.setItem(13, countItem);

            // Slot 14 : Bouton vert +
            inventory.setItem(14, createItem(Material.LIME_CONCRETE, "\u00A7a\u00A7l\u2295 Plus d'\u00E9quipes",
                    List.of("\u00A7a\u2295 Clic gauche \u00A77\u2192 \u00A7a+1",
                            "\u00A7e\u2295 Clic droit \u00A77\u2192 \u00A7a+2",
                            "",
                            "\u00A77Max : \u00A7b15")));

            // \u2500\u2500 Taille des \u00E9quipes \u2500\u2500
            // Slot 21 : Bouton rouge -
            inventory.setItem(21, createItem(Material.RED_CONCRETE, "\u00A7c\u00A7l\u2296 Moins de joueurs",
                    List.of("\u00A7a\u2296 Clic gauche \u00A77\u2192 \u00A7c-1",
                            "\u00A7e\u2296 Clic droit \u00A77\u2192 \u00A7c-5",
                            "",
                            "\u00A77Min : \u00A7b1")));

            // Slot 22 : Taille actuelle
            ItemStack sizeItem = createItem(Material.PLAYER_HEAD, "\u00A7e\u00A7lJoueurs/\u00E9quipe : \u00A7b\u00A7l" + teamSize,
                    List.of("\u00A77Taille maximale par \u00E9quipe",
                            "",
                            "\u00A77Pas de limite haute !"));
            sizeItem.setAmount(Math.min(teamSize, 64));
            inventory.setItem(22, sizeItem);

            // Slot 23 : Bouton vert +
            inventory.setItem(23, createItem(Material.LIME_CONCRETE, "\u00A7a\u00A7l\u2295 Plus de joueurs",
                    List.of("\u00A7a\u2295 Clic gauche \u00A77\u2192 \u00A7a+1",
                            "\u00A7e\u2295 Clic droit \u00A77\u2192 \u00A7a+5",
                            "",
                            "\u00A77Pas de limite !")));
            // (Contenu existant de la configuration d'\u00E9quipe...)
        } else {
            // \u2500\u2500 Slot 13 : Info Solo \u2500\u2500
            ItemStack infoItem = createItem(Material.PAPER, "\u00A7b\u00A7lMode Solo / FFA Actif",
                    List.of("\u00A77Chaque joueur joue seul.",
                            "\u00A77Le s\u00E9lecteur de team en hotbar",
                            "\u00A77permet de passer en Spectateur.",
                            "",
                            "\u00A77Pas de configuration d'\u00E9quipes n\u00E9cessaire."));
            inventory.setItem(13, infoItem);
        }

        // \u2500\u2500 Slot 8 : Verrouiller les \u00E9quipes \u2500\u2500
        boolean locked = tm.isTeamsLocked();
        ItemStack lockItem = createItem(locked ? Material.BARRIER : Material.OAK_DOOR,
                locked ? "\u00A7c\u00A7l\u1F512 \u00C9quipes Verrouill\u00E9es" : "\u00A7a\u00A7l\u1F513 \u00C9quipes Ouvertes",
                List.of("\u00A77Emp\u00EAche les joueurs de changer",
                        "\u00A77d'\u00E9quipe via la banni\u00E8re.",
                        "",
                        "\u00A7e\u25BA Clic pour " + (locked ? "d\u00E9verrouiller" : "verrouiller")));
        inventory.setItem(8, lockItem);


        // \u2500\u2500 Slot 6 : Random Teams \u2500\u2500
        if (!isSolo) {
            inventory.setItem(6, createItem(Material.ENDER_EYE, "\u00A7d\u00A7l\u1F3B2 \u00C9quipes Al\u00E9atoires",
                    List.of("\u00A77R\u00E9partir les joueurs al\u00E9atoirement",
                            "\u00A77dans \u00A7b" + teamCount + " \u00A77\u00E9quipes",
                            "",
                            "\u00A7e\u25BA Clic pour ex\u00E9cuter")));
        }

        // \u2500\u2500 Aper\u00E7u des \u00E9quipes (rang\u00E9e du bas) \u2500\u2500
        if (!isSolo) {
            List<HelTeam> active = tm.getActiveTeams();
            int[] previewSlots = {28, 29, 30, 31, 32, 33, 34};
            int shownTeams = Math.min(active.size(), previewSlots.length);
            for (int i = 0; i < shownTeams; i++) {
                HelTeam team = active.get(i);
                List<String> lore = new ArrayList<>();
                lore.add("\u00A77" + team.getPlayers().size() + "/" + teamSize);
                for (UUID uuid : team.getPlayers()) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    lore.add(team.getChatColor() + " " + (p.getName() != null ? p.getName() : "???"));
                }
                ItemStack teamItem = createItem(team.getBannerMaterial(),
                        team.getChatColor() + team.getName(), lore);
                inventory.setItem(previewSlots[i], teamItem);
            }
        }

        // \u2500\u2500 Slot 40 : Retour \u2500\u2500
        inventory.setItem(40, createItem(Material.ARROW, "\u00A77\u00A7l\u2190 Retour",
                List.of("\u00A77Retourner au menu principal")));
    }

    private String getTeamListPreview(TeamManager tm) {
        StringBuilder sb = new StringBuilder();
        List<HelTeam> active = tm.getActiveTeams();
        for (int i = 0; i < active.size(); i++) {
            if (i > 0) sb.append("\u00A77, ");
            sb.append(active.get(i).getChatColor()).append(active.get(i).getName());
        }
        return sb.toString();
    }

    public void handleClick(Player player, int slot, boolean isRightClick) {
        TeamManager tm = HelPlugin.getInstance().getTeamManager();

        switch (slot) {
            case 2 -> { // Toggle Solo/\u00C9quipe
                tm.setSoloMode(!tm.isSoloMode());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                
                // Rafra\u00EEchir les banni\u00E8res de TOUS les joueurs
                for (Player online : Bukkit.getOnlinePlayers()) {
                    tm.giveTeamBanner(online);
                }
                
                refresh(player);
            }
            case 8 -> { // Lock/Unlock
                tm.setTeamsLocked(!tm.isTeamsLocked());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 6 -> { // Random Teams
                if (!tm.isSoloMode()) {
                    Bukkit.dispatchCommand(player, "team random " + tm.getActiveTeamCount());
                    player.closeInventory();
                }
            }
            case 12 -> { // Moins d'\u00E9quipes
                if (!tm.isSoloMode()) {
                    int delta = isRightClick ? 2 : 1;
                    tm.setActiveTeamCount(tm.getActiveTeamCount() - delta);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
                    refresh(player);
                }
            }
            case 14 -> { // Plus d'\u00E9quipes
                if (!tm.isSoloMode()) {
                    int delta = isRightClick ? 2 : 1;
                    tm.setActiveTeamCount(tm.getActiveTeamCount() + delta);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                    refresh(player);
                }
            }
            case 21 -> { // Moins de joueurs
                if (!tm.isSoloMode()) {
                    int delta = isRightClick ? 5 : 1;
                    tm.setMaxPlayersPerTeam(Math.max(1, tm.getMaxPlayersPerTeam() - delta));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
                    refresh(player);
                }
            }
            case 23 -> { // Plus de joueurs
                if (!tm.isSoloMode()) {
                    int delta = isRightClick ? 5 : 1;
                    tm.setMaxPlayersPerTeam(tm.getMaxPlayersPerTeam() + delta);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                    refresh(player);
                }
            }
            case 40 -> { // Retour
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
