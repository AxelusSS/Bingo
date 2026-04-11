package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
 * GUI de sélection d'équipe.
 * Affiche toutes les bannières actives + spectateur (blanche).
 * Rempli de glass panes décoratives.
 */
public class TeamSelectorGUI implements InventoryHolder {

    private final Inventory inventory;

    public TeamSelectorGUI() {
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        int teamCount = tm.getActiveTeamCount();
        // Taille dynamique : 27 si ≤4, 45 si ≤9, 54 si >9
        int invSize = teamCount <= 4 ? 27 : teamCount <= 9 ? 45 : 54;
        this.inventory = Bukkit.createInventory(this, invSize, "§8✦ Choisis ton équipe ✦");
        populate();
    }

    private void populate() {
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        List<BingoTeam> teams = tm.getActiveTeams();
        int teamSize = tm.getMaxPlayersPerTeam();
        int invSize = inventory.getSize();

        // Remplir tout avec des glass panes
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, "§r", List.of());
        for (int i = 0; i < invSize; i++) {
            inventory.setItem(i, glass);
        }

        // Placer les bannières d'équipe
        int[] teamSlots = getTeamSlots(teams.size(), invSize);

        for (int i = 0; i < teams.size() && i < teamSlots.length; i++) {
            BingoTeam team = teams.get(i);
            inventory.setItem(teamSlots[i], createTeamBanner(team, teamSize));
        }

        // Spectateur (bannière blanche) — toujours en dernière position
        int specSlot = invSize - 5; // Avant-dernière rangée milieu
        ItemStack specBanner = new ItemStack(Material.WHITE_BANNER);
        ItemMeta specMeta = specBanner.getItemMeta();
        specMeta.setDisplayName("§f§lSpectateur");
        List<String> specLore = new ArrayList<>();
        specLore.add("§7Observer la partie");
        specLore.add("");
        specLore.add("§7Spectateurs (" + tm.getSpectatorTeam().getPlayers().size() + "):");
        for (UUID uuid : tm.getSpectatorTeam().getPlayers()) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            specLore.add("§7  " + (p.getName() != null ? p.getName() : "???"));
        }
        specLore.add("");
        specLore.add("§e► Clic pour devenir spectateur");
        specMeta.setLore(specLore);
        specMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        specBanner.setItemMeta(specMeta);
        inventory.setItem(specSlot, specBanner);
    }

    private ItemStack createTeamBanner(BingoTeam team, int teamSize) {
        ItemStack banner = new ItemStack(team.getBannerMaterial());
        ItemMeta meta = banner.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(team.getChatColor() + "§l" + team.getName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Membres (" + team.getPlayers().size() + "/" + teamSize + "):");

            // Tirets avec noms des joueurs
            for (int s = 0; s < Math.max(teamSize, team.getPlayers().size()); s++) {
                if (s < team.getPlayers().size()) {
                    UUID uuid = team.getPlayers().get(s);
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    lore.add(team.getChatColor() + " " + (p.getName() != null ? p.getName() : "???"));
                } else if (s < teamSize) {
                    lore.add("§7 -");
                }
            }

            lore.add("");
            if (team.getPlayers().size() >= teamSize) {
                lore.add("§c✘ Équipe pleine !");
            } else {
                lore.add("§e► Clique pour rejoindre !");
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            banner.setItemMeta(meta);
        }
        return banner;
    }

    /**
     * Calcule les positions des slots pour centrer les bannières.
     * Adapte dynamiquement selon le nombre d'équipes.
     */
    private int[] getTeamSlots(int count, int invSize) {
        if (invSize == 27) {
            // 3 rangées
            return switch (count) {
                case 2 -> new int[]{11, 15};
                case 3 -> new int[]{10, 13, 16};
                case 4 -> new int[]{10, 12, 14, 16};
                default -> new int[]{10, 12, 14, 16};
            };
        } else if (invSize == 45) {
            // 5 rangées
            return switch (count) {
                case 5 -> new int[]{10, 12, 14, 16, 22};
                case 6 -> new int[]{10, 12, 14, 19, 21, 23};
                case 7 -> new int[]{10, 12, 14, 16, 19, 21, 23};
                case 8 -> new int[]{10, 12, 14, 16, 19, 21, 23, 25};
                case 9 -> new int[]{10, 12, 14, 16, 19, 21, 23, 25, 22};
                default -> new int[]{10, 12, 14, 16, 19, 21, 23, 25};
            };
        } else {
            // 6 rangées (54 slots) — pour 10-15 équipes
            return switch (count) {
                case 10 -> new int[]{10, 12, 14, 16, 19, 21, 23, 25, 28, 30};
                case 11 -> new int[]{10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32};
                case 12 -> new int[]{10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34};
                case 13 -> new int[]{10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34, 37};
                case 14 -> new int[]{10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34, 37, 39};
                case 15 -> new int[]{10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34, 37, 39, 41};
                default -> new int[]{10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34, 37, 39, 41};
            };
        }
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
    public Inventory getInventory() {
        return inventory;
    }
}
