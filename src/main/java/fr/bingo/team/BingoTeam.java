package fr.bingo.team;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BingoTeam {
    private final String name;
    private final ChatColor chatColor;
    private final Material bannerMaterial;
    private final List<UUID> players;
    private final List<String> unlockedObjectives;
    private final java.util.Set<Integer> completedRows;
    private final java.util.Set<Integer> completedCols;
    private int score;
    private long lastScoreTime;
    private boolean isFinished;
    private long finishedTime;
    private final java.util.Set<UUID> forfeitVotes;
    private int kills;

    public BingoTeam(String name, ChatColor chatColor, Material bannerMaterial) {
        this.name = name;
        this.chatColor = chatColor;
        this.bannerMaterial = bannerMaterial;
        this.players = new ArrayList<>();
        this.unlockedObjectives = new ArrayList<>();
        this.completedRows = new java.util.HashSet<>();
        this.completedCols = new java.util.HashSet<>();
        this.score = 0;
        this.lastScoreTime = 0;
        this.isFinished = false;
        this.forfeitVotes = new java.util.HashSet<>();
        this.kills = 0;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return chatColor + "Équipe " + name;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public Material getBannerMaterial() {
        return bannerMaterial;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());
        }
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player.getUniqueId());
    }

    public int getScore() {
        return score;
    }

    public long getLastScoreTime() {
        return lastScoreTime;
    }
    
    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        this.isFinished = finished;
        if (finished) {
            this.finishedTime = System.currentTimeMillis();
        }
    }
    
    public long getFinishedTime() {
        return finishedTime;
    }

    public void addScore(int points) {
        this.score += points;
        this.lastScoreTime = System.currentTimeMillis();
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void resetScore() {
        this.score = 0;
        this.lastScoreTime = 0;
        this.isFinished = false;
        this.forfeitVotes.clear();
        this.kills = 0;
    }
    
    public List<String> getUnlockedObjectives() {
        return unlockedObjectives;
    }
    
    public boolean hasUnlocked(String objId) {
        return unlockedObjectives.contains(objId);
    }
    
    public void unlockObjective(String objId, int gridSize) {
        if (isFinished) return;
        
        if (!unlockedObjectives.contains(objId)) {
            unlockedObjectives.add(objId);
            addScore(1);
            
            org.bukkit.Bukkit.getLogger().info("[Bingo] " + name + " unlock: " + objId + " (" + unlockedObjectives.size() + "/" + (gridSize * gridSize) + ")");
            
            checkLinesAndColumns(gridSize);
        }
    }

    private void checkLinesAndColumns(int size) {
        java.util.List<fr.bingo.game.BingoObjective> grid = fr.bingo.BingoPlugin.getInstance().getBingoGame().getGrid().getObjectives();
        boolean isFFA = fr.bingo.BingoPlugin.getInstance().getTeamManager().isSoloMode();

        // Récupérer le nom du joueur pour le mode FFA
        String playerName = "???";
        if (isFFA && !players.isEmpty()) {
            org.bukkit.OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(players.get(0));
            if (op.getName() != null) playerName = op.getName();
        }

        for (int r = 0; r < size; r++) {
            if (!completedRows.contains(r)) {
                boolean rowDone = true;
                for (int c = 0; c < size; c++) {
                    int index = r * size + c;
                    if (index < grid.size() && !hasUnlocked(grid.get(index).getId())) {
                        rowDone = false;
                        break;
                    }
                }
                if (rowDone) {
                    completedRows.add(r);
                    addScore(3);
                    if (isFFA) {
                        org.bukkit.Bukkit.broadcastMessage("§e§l+3 Points ! §f" + playerName + " §ea terminé la ligne " + (r + 1) + " !");
                    } else {
                        org.bukkit.Bukkit.broadcastMessage("§e§l+3 Points ! " + chatColor + "L'équipe " + name + " §ea terminé la ligne " + (r + 1) + " !");
                    }
                }
            }
        }
        
        for (int c = 0; c < size; c++) {
            if (!completedCols.contains(c)) {
                boolean colDone = true;
                for (int r = 0; r < size; r++) {
                    int index = r * size + c;
                    if (index < grid.size() && !hasUnlocked(grid.get(index).getId())) {
                        colDone = false;
                        break;
                    }
                }
                if (colDone) {
                    completedCols.add(c);
                    addScore(3);
                    if (isFFA) {
                        org.bukkit.Bukkit.broadcastMessage("§e§l+3 Points ! §f" + playerName + " §ea terminé la colonne " + (c + 1) + " !");
                    } else {
                        org.bukkit.Bukkit.broadcastMessage("§e§l+3 Points ! " + chatColor + "L'équipe " + name + " §ea terminé la colonne " + (c + 1) + " !");
                    }
                }
            }
        }
        // La détection de fin (blackout) est gérée dans BingoListener.checkTeamCompletion
    }

    public void toggleForfeitVote(java.util.UUID uuid) {
        if (forfeitVotes.contains(uuid)) {
            forfeitVotes.remove(uuid);
        } else {
            forfeitVotes.add(uuid);
        }
    }

    public boolean hasVotedForfeit(java.util.UUID uuid) {
        return forfeitVotes.contains(uuid);
    }

    public int getForfeitVoteCount() {
        return forfeitVotes.size();
    }

    public boolean isAllForfeited() {
        if (players.isEmpty()) return false;
        return forfeitVotes.size() >= players.size();
    }

    public void clearForfeitVotes() {
        forfeitVotes.clear();
    }

    public int getKills() { return kills; }
    public void addKill() { this.kills++; }
    public void setKills(int kills) { this.kills = kills; }
}
