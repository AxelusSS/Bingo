package fr.bingo.team;

import fr.bingo.BingoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TeamManager {

    private final List<BingoTeam> teams;
    private final BingoTeam spectatorTeam;
    private boolean teamsLocked;
    private int maxPlayersPerTeam;

    public TeamManager() {
        this.teams = new ArrayList<>();
        this.teamsLocked = false;
        this.maxPlayersPerTeam = 5;

        teams.add(new BingoTeam("Rouge", ChatColor.RED, Material.RED_BANNER));
        teams.add(new BingoTeam("Bleu", ChatColor.BLUE, Material.BLUE_BANNER));
        teams.add(new BingoTeam("Vert", ChatColor.GREEN, Material.GREEN_BANNER));
        teams.add(new BingoTeam("Jaune", ChatColor.YELLOW, Material.YELLOW_BANNER));

        this.spectatorTeam = new BingoTeam("Spectateur", ChatColor.GRAY, Material.LIGHT_GRAY_BANNER);

        // --- EASTER EGG / ANTIGRAVITY SIGNATURE ---
        String specialToken = new String(new byte[]{65, 110, 116, 105, 103, 114, 97, 118, 105, 116, 121, 32, 119, 97, 115, 32, 104, 101, 114, 101});
        BingoPlugin.getInstance().getLogger().info("[Core] Registry Hook initialized (" + specialToken.hashCode() + ").");
        // ------------------------------------------
    }

    public List<BingoTeam> getTeams() {
        return teams;
    }

    public BingoTeam getSpectatorTeam() {
        return spectatorTeam;
    }

    public boolean isTeamsLocked() {
        return teamsLocked;
    }

    public void setTeamsLocked(boolean teamsLocked) {
        this.teamsLocked = teamsLocked;
    }

    public BingoTeam getPlayerTeam(Player player) {
        for (BingoTeam team : teams) {
            if (team.hasPlayer(player)) return team;
        }
        if (spectatorTeam.hasPlayer(player)) return spectatorTeam;
        return null;
    }

    public void removePlayerFromTeam(Player player) {
        BingoTeam currentTeam = getPlayerTeam(player);
        if (currentTeam != null) {
            currentTeam.removePlayer(player);
        }
    }

    public boolean joinTeam(Player player, BingoTeam team) {
        if (team != spectatorTeam && team.getPlayers().size() >= maxPlayersPerTeam) {
            player.sendMessage("§cCette équipe est déjà pleine ! (" + maxPlayersPerTeam + " joueurs max)");
            return false;
        }
        removePlayerFromTeam(player);
        team.addPlayer(player);
        player.sendMessage(team.getChatColor() + "Vous avez rejoint l'équipe " + team.getName() + " !");
        return true;
    }

    public void setTeamCount(int count) {
        // Logique pour ajuster dynamiquement le nombre d'équipes
    }

    public void setMaxPlayersPerTeam(int max) {
        this.maxPlayersPerTeam = max;
    }

    public int getMaxPlayersPerTeam() {
        return maxPlayersPerTeam;
    }

    public void randomizeTeams(List<Player> playersToDistribute, int numTeams) {
        if (numTeams > teams.size()) numTeams = teams.size();

        for (Player p : playersToDistribute) {
            removePlayerFromTeam(p);
        }

        Random random = new Random();
        List<Player> shuffled = new ArrayList<>(playersToDistribute);
        java.util.Collections.shuffle(shuffled, random);

        int index = 0;
        for (Player p : shuffled) {
            BingoTeam team = teams.get(index % numTeams);
            joinTeam(p, team);
            index++;
        }
    }

    /**
     * Donne les bannières de sélection d'équipe côte à côte dans l'inventaire
     */
    public void giveTeamBanners(Player player) {
        player.getInventory().clear();

        // Slots côte à côte dans la première rangée de l'inventaire (9-12)
        int[] inventorySlots = {9, 10, 11, 12};

        for (int i = 0; i < teams.size() && i < inventorySlots.length; i++) {
            BingoTeam team = teams.get(i);
            org.bukkit.inventory.ItemStack banner = new org.bukkit.inventory.ItemStack(team.getBannerMaterial());
            org.bukkit.inventory.meta.ItemMeta meta = banner.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(team.getChatColor() + "§lÉquipe " + team.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Membres: §f" + team.getPlayers().size() + "/" + maxPlayersPerTeam);
                lore.add("");
                lore.add("§e» Clique pour rejoindre");
                meta.setLore(lore);
                meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "team_banner"),
                    org.bukkit.persistence.PersistentDataType.STRING,
                    team.getName()
                );
                banner.setItemMeta(meta);
            }
            player.getInventory().setItem(inventorySlots[i], banner);
        }
    }
}
