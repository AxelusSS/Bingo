package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
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
 * Sous-menu de configuration des équipes.
 * Béton rouge/vert pour les boutons (100% fiable).
 */
public class TeamConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public TeamConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 45, "§b§l👥 Configuration Équipes");
        populate();
    }

    private void populate() {
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        inventory.clear();

        // Bordure décorative
        ItemStack border = createItem(Material.BLUE_STAINED_GLASS_PANE, "§r", List.of());
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

        // ── Slot 2 : Mode Solo / Équipe ──
        ItemStack soloItem;
        if (isSolo) {
            soloItem = createItemHidden(Material.IRON_SWORD, "§e§lMode : §a§lSOLO / FFA",
                    List.of("§7Chacun pour soi !",
                            "§7Chaque joueur est seul",
                            "",
                            "§e► Clic pour passer en mode Équipe"));
        } else {
            soloItem = createItemHidden(Material.SHIELD, "§e§lMode : §b§lÉQUIPES",
                    List.of("§7Jouez en équipes !",
                            "§7" + teamCount + " équipes de " + teamSize + " joueurs max",
                            "",
                            "§e► Clic pour passer en mode Solo/FFA"));
        }
        inventory.setItem(2, soloItem);

        if (!isSolo) {
            // ── Nombre d'équipes ──
            // Slot 12 : Bouton rouge -
            inventory.setItem(12, createItem(Material.RED_CONCRETE, "§c§l⊖ Moins d'équipes",
                    List.of("§a⊖ Clic gauche §7→ §c-1",
                            "§e⊖ Clic droit §7→ §c-2",
                            "",
                            "§7Min : §b2")));

            // Slot 13 : Nombre actuel
            ItemStack countItem = createItem(Material.WHITE_BANNER, "§e§lÉquipes : §b§l" + teamCount,
                    List.of("§7Nombre d'équipes actives",
                            "",
                            "§7Équipes :",
                            getTeamListPreview(tm)));
            countItem.setAmount(Math.min(teamCount, 64));
            inventory.setItem(13, countItem);

            // Slot 14 : Bouton vert +
            inventory.setItem(14, createItem(Material.LIME_CONCRETE, "§a§l⊕ Plus d'équipes",
                    List.of("§a⊕ Clic gauche §7→ §a+1",
                            "§e⊕ Clic droit §7→ §a+2",
                            "",
                            "§7Max : §b15")));

            // ── Taille des équipes ──
            // Slot 21 : Bouton rouge -
            inventory.setItem(21, createItem(Material.RED_CONCRETE, "§c§l⊖ Moins de joueurs",
                    List.of("§a⊖ Clic gauche §7→ §c-1",
                            "§e⊖ Clic droit §7→ §c-5",
                            "",
                            "§7Min : §b1")));

            // Slot 22 : Taille actuelle
            ItemStack sizeItem = createItem(Material.PLAYER_HEAD, "§e§lJoueurs/équipe : §b§l" + teamSize,
                    List.of("§7Taille maximale par équipe",
                            "",
                            "§7Pas de limite haute !"));
            sizeItem.setAmount(Math.min(teamSize, 64));
            inventory.setItem(22, sizeItem);

            // Slot 23 : Bouton vert +
            inventory.setItem(23, createItem(Material.LIME_CONCRETE, "§a§l⊕ Plus de joueurs",
                    List.of("§a⊕ Clic gauche §7→ §a+1",
                            "§e⊕ Clic droit §7→ §a+5",
                            "",
                            "§7Pas de limite !")));
            // (Contenu existant de la configuration d'équipe...)
        } else {
            // ── Slot 13 : Info Solo ──
            ItemStack infoItem = createItem(Material.PAPER, "§b§lMode Solo / FFA Actif",
                    List.of("§7Chaque joueur joue seul.",
                            "§7Le sélecteur de team en hotbar",
                            "§7permet de passer en Spectateur.",
                            "",
                            "§7Pas de configuration d'équipes nécessaire."));
            inventory.setItem(13, infoItem);
        }

        // ── Slot 8 : Verrouiller les équipes ──
        boolean locked = tm.isTeamsLocked();
        ItemStack lockItem = createItem(locked ? Material.BARRIER : Material.OAK_DOOR,
                locked ? "§c§l🔒 Équipes Verrouillées" : "§a§l🔓 Équipes Ouvertes",
                List.of("§7Empêche les joueurs de changer",
                        "§7d'équipe via la bannière.",
                        "",
                        "§e► Clic pour " + (locked ? "déverrouiller" : "verrouiller")));
        inventory.setItem(8, lockItem);


        // ── Slot 6 : Random Teams ──
        if (!isSolo) {
            inventory.setItem(6, createItem(Material.ENDER_EYE, "§d§l🎲 Équipes Aléatoires",
                    List.of("§7Répartir les joueurs aléatoirement",
                            "§7dans §b" + teamCount + " §7équipes",
                            "",
                            "§e► Clic pour exécuter")));
        }

        // ── Aperçu des équipes (rangée du bas) ──
        if (!isSolo) {
            List<BingoTeam> active = tm.getActiveTeams();
            int[] previewSlots = {28, 29, 30, 31, 32, 33, 34};
            int shownTeams = Math.min(active.size(), previewSlots.length);
            for (int i = 0; i < shownTeams; i++) {
                BingoTeam team = active.get(i);
                List<String> lore = new ArrayList<>();
                lore.add("§7" + team.getPlayers().size() + "/" + teamSize);
                for (UUID uuid : team.getPlayers()) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    lore.add(team.getChatColor() + " " + (p.getName() != null ? p.getName() : "???"));
                }
                ItemStack teamItem = createItem(team.getBannerMaterial(),
                        team.getChatColor() + team.getName(), lore);
                inventory.setItem(previewSlots[i], teamItem);
            }
        }

        // ── Slot 40 : Retour ──
        inventory.setItem(40, createItem(Material.ARROW, "§7§l← Retour",
                List.of("§7Retourner au menu principal")));
    }

    private String getTeamListPreview(TeamManager tm) {
        StringBuilder sb = new StringBuilder();
        List<BingoTeam> active = tm.getActiveTeams();
        for (int i = 0; i < active.size(); i++) {
            if (i > 0) sb.append("§7, ");
            sb.append(active.get(i).getChatColor()).append(active.get(i).getName());
        }
        return sb.toString();
    }

    public void handleClick(Player player, int slot, boolean isRightClick) {
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();

        switch (slot) {
            case 2 -> { // Toggle Solo/Équipe
                tm.setSoloMode(!tm.isSoloMode());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                
                // Rafraîchir les bannières de TOUS les joueurs
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
            case 12 -> { // Moins d'équipes
                if (!tm.isSoloMode()) {
                    int delta = isRightClick ? 2 : 1;
                    tm.setActiveTeamCount(tm.getActiveTeamCount() - delta);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.8f);
                    refresh(player);
                }
            }
            case 14 -> { // Plus d'équipes
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
