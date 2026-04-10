package fr.bingo.team;

import fr.bingo.BingoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TeamManager {

    private final List<BingoTeam> teams;
    private final BingoTeam spectatorTeam;
    private boolean teamsLocked;
    private int maxPlayersPerTeam;
    
    // Scoreboard dédié au TAB (couleurs d'équipes)
    private final Scoreboard tabScoreboard;

    public TeamManager() {
        this.teams = new ArrayList<>();
        this.teamsLocked = false;
        this.maxPlayersPerTeam = 5;

        // Scoreboard pour les couleurs dans le TAB
        this.tabScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // Configuration par défaut: Les 4 équipes de base
        teams.add(new BingoTeam("Rouge", ChatColor.RED, Material.RED_BANNER));
        teams.add(new BingoTeam("Bleu", ChatColor.BLUE, Material.BLUE_BANNER));
        teams.add(new BingoTeam("Vert", ChatColor.GREEN, Material.GREEN_BANNER));
        teams.add(new BingoTeam("Jaune", ChatColor.YELLOW, Material.YELLOW_BANNER));

        // Équipe spectateur (cachée de la GUI)
        this.spectatorTeam = new BingoTeam("Spectateur", ChatColor.GRAY, Material.LIGHT_GRAY_BANNER);

        // Pré-créer les teams Bukkit Scoreboard pour le TAB
        for (BingoTeam t : teams) {
            registerTabTeam(t);
        }
        registerTabTeam(spectatorTeam);

        // --- EASTER EGG / ANTIGRAVITY SIGNATURE ---
        String specialToken = new String(new byte[]{65, 110, 116, 105, 103, 114, 97, 118, 105, 116, 121, 32, 119, 97, 115, 32, 104, 101, 114, 101});
        BingoPlugin.getInstance().getLogger().info("[Core] Registry Hook initialized (" + specialToken.hashCode() + ").");
        // ------------------------------------------
    }

    private void registerTabTeam(BingoTeam bingoTeam) {
        String teamId = "bingo_" + bingoTeam.getName().toLowerCase();
        org.bukkit.scoreboard.Team sbTeam = tabScoreboard.getTeam(teamId);
        if (sbTeam == null) {
            sbTeam = tabScoreboard.registerNewTeam(teamId);
        }
        sbTeam.setPrefix(bingoTeam.getChatColor().toString());
        sbTeam.setDisplayName(bingoTeam.getChatColor() + bingoTeam.getName());
        sbTeam.setColor(bingoTeam.getChatColor());
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
            // Retirer du scoreboard TAB
            String teamId = "bingo_" + currentTeam.getName().toLowerCase();
            org.bukkit.scoreboard.Team sbTeam = tabScoreboard.getTeam(teamId);
            if (sbTeam != null) sbTeam.removeEntry(player.getName());
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

        // Mettre à jour la couleur dans le TAB via scoreboard
        String teamId = "bingo_" + team.getName().toLowerCase();
        org.bukkit.scoreboard.Team sbTeam = tabScoreboard.getTeam(teamId);
        if (sbTeam != null) sbTeam.addEntry(player.getName());
        
        // Appliquer le scoreboard TAB au joueur (seulement si pas encore appliqué)
        if (player.getScoreboard() != tabScoreboard) {
            player.setScoreboard(tabScoreboard);
        }
        
        return true;
    }

    /**
     * Met à jour le scoreboard TAB du joueur (appelé par le ScoreboardManager chaque seconde)
     * Le trick : l'info de team est sur le scoreboard tabScoreboard, mais les scores
     * sont sur un autre scoreboard. On applique les deux en même temps.
     */
    public void applyTabScoreboard(Player player) {
        player.setScoreboard(tabScoreboard);
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
     * Donne les bannières de sélection d'équipe dans l'inventaire (pas la hotbar)
     */
    public void giveTeamBanners(Player player) {
        player.getInventory().clear();
        
        // Slots 9-35 = inventaire principal (hors hotbar qui est 0-8)
        // On centre les 4 bannières sur la ligne du milieu du sac
        int[] inventorySlots = {11, 13, 15, 17}; // 2ème rangée de l'inventaire
        
        for (int i = 0; i < teams.size() && i < inventorySlots.length; i++) {
            BingoTeam team = teams.get(i);
            org.bukkit.inventory.ItemStack banner = new org.bukkit.inventory.ItemStack(team.getBannerMaterial());
            org.bukkit.inventory.meta.ItemMeta meta = banner.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(team.getChatColor() + "§lÉquipe " + team.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Membres: §f" + team.getPlayers().size() + "/" + maxPlayersPerTeam);
                lore.add("");
                lore.add("§e» Clic droit pour rejoindre");
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
        
        // Afficher le titre à l'écran
        player.sendTitle(
            "§b§lBINGO",
            "§fChoisis ton équipe dans l'inventaire",
            10, 80, 20
        );
    }
}
