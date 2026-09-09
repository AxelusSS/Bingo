package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.team.HelTeam;
import fr.hel.team.TeamManager;
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
 * GUI de s\u00E9lection d'\u00E9quipe.
 * Affiche toutes les banni\u00E8res actives + spectateur (blanche).
 * Rempli de glass panes d\u00E9coratives.
 */
public class TeamSelectorGUI implements InventoryHolder {

    private final Inventory inventory;

    public TeamSelectorGUI() {
        TeamManager tm = HelPlugin.getInstance().getTeamManager();
        int teamCount = tm.getActiveTeamCount();
        // Taille dynamique : 27 si \u22644, 45 si \u22649, 54 si >9
        int invSize = teamCount <= 4 ? 27 : teamCount <= 9 ? 45 : 54;
        this.inventory = Bukkit.createInventory(this, invSize, "\u00A78\u2726 Choisis ton \u00E9quipe \u2726");
        populate();
    }

    private void populate() {
        TeamManager tm = HelPlugin.getInstance().getTeamManager();
        List<HelTeam> teams = tm.getActiveTeams();
        int teamSize = tm.getMaxPlayersPerTeam();
        int invSize = inventory.getSize();

        // Remplir tout avec des glass panes
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, "\u00A7r", List.of());
        for (int i = 0; i < invSize; i++) {
            inventory.setItem(i, glass);
        }

        // Placer les banni\u00E8res d'\u00E9quipe (seulement si pas en FFA)
        if (!tm.isSoloMode()) {
            int[] teamSlots = getTeamSlots(teams.size(), invSize);
            for (int i = 0; i < teams.size() && i < teamSlots.length; i++) {
                HelTeam team = teams.get(i);
                inventory.setItem(teamSlots[i], createTeamBanner(team, teamSize));
            }
        } else {
            // Mode FFA : Message au centre
            ItemStack info = createItem(Material.PAPER, "\u00A7b\u00A7lMode FFA Activ\u00E9", List.of(
                    "\u00A77Les \u00E9quipes sont d\u00E9sactiv\u00E9es.",
                    "\u00A77Chaque joueur joue pour soi-m\u00EAme.",
                    "",
                    "\u00A77Vous pouvez toujours rejoindre les spectateurs",
                    "\u00A77en bas si vous ne souhaitez pas participer."
            ));
            inventory.setItem(invSize / 2, info);
        }

        // Spectateur (banni\u00E8re blanche) \u2014 toujours en derni\u00E8re position
        int specSlot = invSize - 5; // Avant-derni\u00E8re rang\u00E9e milieu
        ItemStack specBanner = new ItemStack(Material.WHITE_BANNER);
        ItemMeta specMeta = specBanner.getItemMeta();
        specMeta.setDisplayName("\u00A7f\u00A7lSpectateur");
        List<String> specLore = new ArrayList<>();
        specLore.add("\u00A77Observer la partie");
        specLore.add("");
        specLore.add("\u00A77Spectateurs (" + tm.getSpectatorTeam().getPlayers().size() + "):");
        for (UUID uuid : tm.getSpectatorTeam().getPlayers()) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            specLore.add("\u00A77  " + (p.getName() != null ? p.getName() : "???"));
        }
        specLore.add("");
        specLore.add("\u00A7e\u25BA Clic pour devenir spectateur");
        specMeta.setLore(specLore);
        specMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        specBanner.setItemMeta(specMeta);
        inventory.setItem(specSlot, specBanner);
    }

    private ItemStack createTeamBanner(HelTeam team, int teamSize) {
        ItemStack banner = new ItemStack(team.getBannerMaterial());
        ItemMeta meta = banner.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(team.getChatColor() + "\u00A7l" + team.getName());

            List<String> lore = new ArrayList<>();
            lore.add("\u00A77Membres (" + team.getPlayers().size() + "/" + teamSize + "):");

            // Tirets avec noms des joueurs
            for (int s = 0; s < Math.max(teamSize, team.getPlayers().size()); s++) {
                if (s < team.getPlayers().size()) {
                    UUID uuid = team.getPlayers().get(s);
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    lore.add(team.getChatColor() + " " + (p.getName() != null ? p.getName() : "???"));
                } else if (s < teamSize) {
                    lore.add("\u00A77 -");
                }
            }

            lore.add("");
            if (team.getPlayers().size() >= teamSize) {
                lore.add("\u00A7c\u2718 \u00C9quipe pleine !");
            } else {
                lore.add("\u00A7e\u25BA Clique pour rejoindre !");
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            banner.setItemMeta(meta);
        }
        return banner;
    }

    /**
     * Calcule les positions des slots pour centrer les banni\u00E8res.
     * Adapte dynamiquement selon le nombre d'\u00E9quipes.
     */
    private int[] getTeamSlots(int count, int invSize) {
        if (invSize == 27) {
            // 3 rang\u00E9es
            return switch (count) {
                case 2 -> new int[]{11, 15};
                case 3 -> new int[]{10, 13, 16};
                case 4 -> new int[]{10, 12, 14, 16};
                default -> new int[]{10, 12, 14, 16};
            };
        } else if (invSize == 45) {
            // 5 rang\u00E9es
            return switch (count) {
                case 5 -> new int[]{10, 12, 14, 16, 22};
                case 6 -> new int[]{10, 12, 14, 19, 21, 23};
                case 7 -> new int[]{10, 12, 14, 16, 19, 21, 23};
                case 8 -> new int[]{10, 12, 14, 16, 19, 21, 23, 25};
                case 9 -> new int[]{10, 12, 14, 16, 19, 21, 23, 25, 22};
                default -> new int[]{10, 12, 14, 16, 19, 21, 23, 25};
            };
        } else {
            // 6 rang\u00E9es (54 slots) \u2014 pour 10-15 \u00E9quipes
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
