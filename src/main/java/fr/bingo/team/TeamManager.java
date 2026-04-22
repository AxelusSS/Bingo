package fr.bingo.team;

import fr.bingo.BingoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class TeamManager {

    private final List<BingoTeam> teams;
    private final BingoTeam spectatorTeam;
    private boolean teamsLocked;
    private int maxPlayersPerTeam;
    private int activeTeamCount;
    private boolean soloMode;

    public TeamManager() {
        this.teams = new ArrayList<>();
        this.teamsLocked = false;
        this.maxPlayersPerTeam = 4;
        this.activeTeamCount = 4;
        this.soloMode = false;

        // 15 équipes disponibles (toutes les couleurs de bannières sauf blanche)
        teams.add(new BingoTeam("Rouge", ChatColor.RED, Material.RED_BANNER));
        teams.add(new BingoTeam("Bleu", ChatColor.BLUE, Material.BLUE_BANNER));
        teams.add(new BingoTeam("Vert", ChatColor.GREEN, Material.GREEN_BANNER));
        teams.add(new BingoTeam("Jaune", ChatColor.YELLOW, Material.YELLOW_BANNER));
        teams.add(new BingoTeam("Rose", ChatColor.LIGHT_PURPLE, Material.PINK_BANNER));
        teams.add(new BingoTeam("Cyan", ChatColor.AQUA, Material.CYAN_BANNER));
        teams.add(new BingoTeam("Orange", ChatColor.GOLD, Material.ORANGE_BANNER));
        teams.add(new BingoTeam("Violet", ChatColor.DARK_PURPLE, Material.PURPLE_BANNER));
        teams.add(new BingoTeam("Lime", ChatColor.GREEN, Material.LIME_BANNER));
        teams.add(new BingoTeam("Ciel", ChatColor.AQUA, Material.LIGHT_BLUE_BANNER));
        teams.add(new BingoTeam("Magenta", ChatColor.LIGHT_PURPLE, Material.MAGENTA_BANNER));
        teams.add(new BingoTeam("Marron", ChatColor.DARK_RED, Material.BROWN_BANNER));
        teams.add(new BingoTeam("Noir", ChatColor.DARK_GRAY, Material.BLACK_BANNER));
        teams.add(new BingoTeam("Gris", ChatColor.GRAY, Material.GRAY_BANNER));
        teams.add(new BingoTeam("Argent", ChatColor.GRAY, Material.LIGHT_GRAY_BANNER));

        // Spectateur = bannière blanche (n'importe qui peut rejoindre)
        this.spectatorTeam = new BingoTeam("Spectateur", ChatColor.WHITE, Material.WHITE_BANNER);

        // --- EASTER EGG / ANTIGRAVITY SIGNATURE ---
        String specialToken = new String(new byte[]{65, 110, 116, 105, 103, 114, 97, 118, 105, 116, 121, 32, 119, 97, 115, 32, 104, 101, 114, 101});
        BingoPlugin.getInstance().getLogger().info("[Core] Registry Hook initialized (" + specialToken.hashCode() + ").");
        // ------------------------------------------
    }

    public List<BingoTeam> getTeams() {
        return teams;
    }

    /**
     * Retourne uniquement les équipes actives (les N premières selon activeTeamCount)
     */
    public List<BingoTeam> getActiveTeams() {
        return teams.subList(0, Math.min(activeTeamCount, teams.size()));
    }

    public int getActiveTeamCount() {
        return activeTeamCount;
    }

    public void setActiveTeamCount(int count) {
        // Min 2, max = nombre total d'équipes (8)
        this.activeTeamCount = Math.max(2, Math.min(count, teams.size()));
    }

    // ── Solo / FFA ──

    public boolean isSoloMode() {
        return soloMode;
    }

    public void setSoloMode(boolean solo) {
        this.soloMode = solo;
        if (solo) {
            // Passer tout le monde en mode joueur par défaut
            for (Player player : Bukkit.getOnlinePlayers()) {
                removePlayerFromTeam(player);
                if (BingoPlugin.getInstance().getBingoGame().getState() == fr.bingo.game.GameState.WAITING) {
                    giveTeamBanner(player);
                }
            }
        }
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
        return getPlayerTeam(player.getUniqueId());
    }

    public BingoTeam getPlayerTeam(java.util.UUID uuid) {
        for (BingoTeam team : teams) {
            if (team.getPlayers().contains(uuid)) return team;
        }
        if (spectatorTeam.getPlayers().contains(uuid)) return spectatorTeam;
        return null;
    }

    public BingoTeam getPlayerTeamByName(String name) {
        for (BingoTeam team : teams) {
            for (java.util.UUID uuid : team.getPlayers()) {
                org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                if (name.equalsIgnoreCase(op.getName())) {
                    return team;
                }
            }
        }
        for (java.util.UUID uuid : spectatorTeam.getPlayers()) {
            org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            if (name.equalsIgnoreCase(op.getName())) {
                return spectatorTeam;
            }
        }
        return null;
    }

    public void removePlayerFromTeam(Player player) {
        BingoTeam currentTeam = getPlayerTeam(player);
        if (currentTeam != null) {
            currentTeam.removePlayer(player);
        }
    }

    public boolean joinTeam(Player player, BingoTeam team) {
        if (soloMode && team != spectatorTeam) {
            player.sendMessage("§cLe mode FFA est activé : les équipes sont désactivées !");
            return false;
        }
        if (teamsLocked && team != spectatorTeam) {
            player.sendMessage("§cLes équipes sont verrouillées !");
            return false;
        }
        if (!soloMode && team != spectatorTeam && !getActiveTeams().contains(team)) {
            player.sendMessage("§cCette équipe n'est pas active !");
            return false;
        }
        if (!soloMode && team != spectatorTeam && team.getPlayers().size() >= maxPlayersPerTeam) {
            player.sendMessage("§cCette équipe est déjà pleine ! (" + maxPlayersPerTeam + " joueurs max)");
            return false;
        }
        removePlayerFromTeam(player);
        team.addPlayer(player);
        player.sendMessage(team.getChatColor() + "Vous avez rejoint l'équipe " + team.getName() + " !");

        // Synchroniser les advancements déjà trouvés par l'équipe
        BingoPlugin.getInstance().getBingoGame().syncTeamAdvancements(player, team);


        // Mettre à jour la bannière dans la hotbar
        if (BingoPlugin.getInstance().getBingoGame().getState() == fr.bingo.game.GameState.WAITING) {
            giveTeamBanner(player);
        }
        return true;
    }

    /**
     * Force un joueur dans une équipe (bypass le lock) — utilisé par /team set uniquement
     */
    public boolean forceJoinTeam(Player player, BingoTeam team) {
        removePlayerFromTeam(player);
        team.addPlayer(player);
        player.sendMessage(team.getChatColor() + "Vous avez été assigné à l'équipe " + team.getName() + " !");
        if (BingoPlugin.getInstance().getBingoGame().getState() == fr.bingo.game.GameState.WAITING) {
            giveTeamBanner(player);
        }
        return true;
    }

    public void setTeamCount(int count) {
        setActiveTeamCount(count);
    }

    public void setMaxPlayersPerTeam(int max) {
        this.maxPlayersPerTeam = Math.max(1, max);
    }

    public int getMaxPlayersPerTeam() {
        return maxPlayersPerTeam;
    }

    public void randomizeTeams(List<Player> playersToDistribute, int numTeams) {
        if (numTeams > activeTeamCount) numTeams = activeTeamCount;

        for (Player p : playersToDistribute) {
            removePlayerFromTeam(p);
        }

        Random random = new Random();
        List<Player> shuffled = new ArrayList<>(playersToDistribute);
        java.util.Collections.shuffle(shuffled, random);

        int index = 0;
        for (Player p : shuffled) {
            BingoTeam team = teams.get(index % numTeams);
            forceJoinTeam(p, team);
            index++;
        }
    }

    /**
     * Donne UNE SEULE bannière dans la hotbar (slot 4 = centre).
     * Bannière blanche si pas d'équipe, couleur de l'équipe sinon.
     * Indropable, ouvre le GUI de sélection au clic droit.
     */
    public void giveTeamBanner(Player player) {
        BingoTeam team = getPlayerTeam(player);
        ItemStack item;
        String name;
        List<String> lore;

        if (soloMode) {
            // Mode FFA : Toggle Joueur (Vert) / Spectateur (Rouge)
            boolean isSpectator = (team != null && team.getName().equals("Spectateur"));
            
            if (isSpectator) {
                item = new ItemStack(Material.RED_BANNER);
                name = "§6§lMode Spectateur";
                lore = List.of("§7Vous observez la partie.", "", "§e► Clic pour PARTICIPER");
            } else {
                item = new ItemStack(Material.LIME_BANNER);
                name = "§a§lMode Joueur";
                lore = List.of("§7Vous participez à la partie.", "", "§e► Clic pour passer SPECTATEUR");
            }
        } else {
            // Mode Équipe : Sélection classique
            Material bannerMat;
            if (team == null || team.getName().equals("Spectateur")) {
                bannerMat = Material.WHITE_BANNER;
                name = "§f§lChoisir une équipe";
            } else {
                bannerMat = team.getBannerMaterial();
                name = team.getChatColor() + "§lÉquipe " + team.getName();
            }
            item = new ItemStack(bannerMat);
            lore = List.of("§7Clic droit pour choisir/changer d'équipe");
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            // Marquer comme item de sélection
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "team_selector");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }

        player.getInventory().setItem(4, item);
    }



    /**
     * Ancienne méthode — redirige vers la nouvelle
     */
    public void giveTeamBanners(Player player) {
        giveTeamBanner(player);
    }
}
