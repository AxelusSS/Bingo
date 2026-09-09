package fr.hel.team;

import fr.hel.HelPlugin;
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

    private final List<HelTeam> teams;
    private final HelTeam spectatorTeam;
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

        // 15 \u00E9quipes disponibles (toutes les couleurs de banni\u00E8res sauf blanche)
        teams.add(new HelTeam("Rouge", ChatColor.RED, Material.RED_BANNER));
        teams.add(new HelTeam("Bleu", ChatColor.BLUE, Material.BLUE_BANNER));
        teams.add(new HelTeam("Vert", ChatColor.GREEN, Material.GREEN_BANNER));
        teams.add(new HelTeam("Jaune", ChatColor.YELLOW, Material.YELLOW_BANNER));
        teams.add(new HelTeam("Rose", ChatColor.LIGHT_PURPLE, Material.PINK_BANNER));
        teams.add(new HelTeam("Cyan", ChatColor.AQUA, Material.CYAN_BANNER));
        teams.add(new HelTeam("Orange", ChatColor.GOLD, Material.ORANGE_BANNER));
        teams.add(new HelTeam("Violet", ChatColor.DARK_PURPLE, Material.PURPLE_BANNER));
        teams.add(new HelTeam("Lime", ChatColor.GREEN, Material.LIME_BANNER));
        teams.add(new HelTeam("Ciel", ChatColor.AQUA, Material.LIGHT_BLUE_BANNER));
        teams.add(new HelTeam("Magenta", ChatColor.LIGHT_PURPLE, Material.MAGENTA_BANNER));
        teams.add(new HelTeam("Marron", ChatColor.DARK_RED, Material.BROWN_BANNER));
        teams.add(new HelTeam("Noir", ChatColor.DARK_GRAY, Material.BLACK_BANNER));
        teams.add(new HelTeam("Gris", ChatColor.GRAY, Material.GRAY_BANNER));
        teams.add(new HelTeam("Argent", ChatColor.GRAY, Material.LIGHT_GRAY_BANNER));

        // Spectateur = banni\u00E8re blanche (n'importe qui peut rejoindre)
        this.spectatorTeam = new HelTeam("Spectateur", ChatColor.WHITE, Material.WHITE_BANNER);

        // --- EASTER EGG / ANTIGRAVITY SIGNATURE ---
        String specialToken = new String(new byte[]{65, 110, 116, 105, 103, 114, 97, 118, 105, 116, 121, 32, 119, 97, 115, 32, 104, 101, 114, 101});
        HelPlugin.getInstance().getLogger().info("[Core] Registry Hook initialized (" + specialToken.hashCode() + ").");
        // ------------------------------------------
    }

    public List<HelTeam> getTeams() {
        return teams;
    }

    /**
     * Retourne uniquement les \u00E9quipes actives (les N premi\u00E8res selon activeTeamCount)
     */
    public List<HelTeam> getActiveTeams() {
        return teams.subList(0, Math.min(activeTeamCount, teams.size()));
    }

    public int getActiveTeamCount() {
        return activeTeamCount;
    }

    public void setActiveTeamCount(int count) {
        // Min 2, max = nombre total d'\u00E9quipes (8)
        this.activeTeamCount = Math.max(2, Math.min(count, teams.size()));
    }

    // \u2500\u2500 Solo / FFA \u2500\u2500

    public boolean isSoloMode() {
        return soloMode;
    }

    public void setSoloMode(boolean solo) {
        this.soloMode = solo;
        if (solo) {
            // Passer tout le monde en mode joueur par d\u00E9faut
            for (Player player : Bukkit.getOnlinePlayers()) {
                removePlayerFromTeam(player);
                if (HelPlugin.getInstance().getHelGame().getState() == fr.hel.game.GameState.WAITING) {
                    giveTeamBanner(player);
                }
            }
        }
    }

    public HelTeam getSpectatorTeam() {
        return spectatorTeam;
    }

    public boolean isTeamsLocked() {
        return teamsLocked;
    }

    public void setTeamsLocked(boolean teamsLocked) {
        this.teamsLocked = teamsLocked;
    }

    public HelTeam getPlayerTeam(Player player) {
        return getPlayerTeam(player.getUniqueId());
    }

    public HelTeam getPlayerTeam(java.util.UUID uuid) {
        for (HelTeam team : teams) {
            if (team.getPlayers().contains(uuid)) return team;
        }
        if (spectatorTeam.getPlayers().contains(uuid)) return spectatorTeam;
        return null;
    }

    public HelTeam getPlayerTeamByName(String name) {
        for (HelTeam team : teams) {
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
        HelTeam currentTeam = getPlayerTeam(player);
        if (currentTeam != null) {
            currentTeam.removePlayer(player);
        }
    }

    public boolean joinTeam(Player player, HelTeam team) {
        if (soloMode && team != spectatorTeam) {
            player.sendMessage("\u00A7cLe mode FFA est activ\u00E9 : les \u00E9quipes sont d\u00E9sactiv\u00E9es !");
            return false;
        }
        if (teamsLocked && team != spectatorTeam) {
            player.sendMessage("\u00A7cLes \u00E9quipes sont verrouill\u00E9es !");
            return false;
        }
        if (!soloMode && team != spectatorTeam && !getActiveTeams().contains(team)) {
            player.sendMessage("\u00A7cCette \u00E9quipe n'est pas active !");
            return false;
        }
        if (!soloMode && team != spectatorTeam && team.getPlayers().size() >= maxPlayersPerTeam) {
            player.sendMessage("\u00A7cCette \u00E9quipe est d\u00E9j\u00E0 pleine ! (" + maxPlayersPerTeam + " joueurs max)");
            return false;
        }
        removePlayerFromTeam(player);
        team.addPlayer(player);
        player.sendMessage(team.getChatColor() + "Vous avez rejoint l'\u00E9quipe " + team.getName() + " !");

        // Synchroniser les advancements d\u00E9j\u00E0 trouv\u00E9s par l'\u00E9quipe
        HelPlugin.getInstance().getHelGame().syncTeamAdvancements(player, team);


        // Mettre \u00E0 jour la banni\u00E8re dans la hotbar
        if (HelPlugin.getInstance().getHelGame().getState() == fr.hel.game.GameState.WAITING) {
            giveTeamBanner(player);
        }
        return true;
    }

    /**
     * Force un joueur dans une \u00E9quipe (bypass le lock) \u2014 utilis\u00E9 par /team set uniquement
     */
    public boolean forceJoinTeam(Player player, HelTeam team) {
        removePlayerFromTeam(player);
        team.addPlayer(player);
        player.sendMessage(team.getChatColor() + "Vous avez \u00E9t\u00E9 assign\u00E9 \u00E0 l'\u00E9quipe " + team.getName() + " !");
        if (HelPlugin.getInstance().getHelGame().getState() == fr.hel.game.GameState.WAITING) {
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
            HelTeam team = teams.get(index % numTeams);
            forceJoinTeam(p, team);
            index++;
        }
    }

    /**
     * Donne UNE SEULE banni\u00E8re dans la hotbar (slot 4 = centre).
     * Banni\u00E8re blanche si pas d'\u00E9quipe, couleur de l'\u00E9quipe sinon.
     * Indropable, ouvre le GUI de s\u00E9lection au clic droit.
     */
    public void giveTeamBanner(Player player) {
        HelTeam team = getPlayerTeam(player);
        ItemStack item;
        String name;
        List<String> lore;

        if (soloMode) {
            // Mode FFA : Toggle Joueur (Vert) / Spectateur (Rouge)
            boolean isSpectator = (team != null && team.getName().equals("Spectateur"));
            
            if (isSpectator) {
                item = new ItemStack(Material.RED_BANNER);
                name = "\u00A76\u00A7lMode Spectateur";
                lore = List.of("\u00A77Vous observez la partie.", "", "\u00A7e\u25BA Clic pour PARTICIPER");
            } else {
                item = new ItemStack(Material.LIME_BANNER);
                name = "\u00A7a\u00A7lMode Joueur";
                lore = List.of("\u00A77Vous participez \u00E0 la partie.", "", "\u00A7e\u25BA Clic pour passer SPECTATEUR");
            }
        } else {
            // Mode \u00C9quipe : S\u00E9lection classique
            Material bannerMat;
            if (team == null || team.getName().equals("Spectateur")) {
                bannerMat = Material.WHITE_BANNER;
                name = "\u00A7f\u00A7lChoisir une \u00E9quipe";
            } else {
                bannerMat = team.getBannerMaterial();
                name = team.getChatColor() + "\u00A7l\u00C9quipe " + team.getName();
            }
            item = new ItemStack(bannerMat);
            lore = List.of("\u00A77Clic droit pour choisir/changer d'\u00E9quipe");
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            // Marquer comme item de s\u00E9lection
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(HelPlugin.getInstance(), "team_selector");
            meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
            item.setItemMeta(meta);
        }

        player.getInventory().setItem(4, item);
    }



    /**
     * Ancienne m\u00E9thode \u2014 redirige vers la nouvelle
     */
    public void giveTeamBanners(Player player) {
        giveTeamBanner(player);
    }
}
