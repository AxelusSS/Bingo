package fr.bingo.game;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreboardManager {

    private final BingoPlugin plugin;

    public ScoreboardManager(BingoPlugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        // Exécuter l'update toutes les secondes (20 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateScoreboards, 20L, 20L);
    }

    private void updateScoreboards() {
        BingoGame game = plugin.getBingoGame();
        
        // Formatter le chrono
        long elapsed = game.getElapsedSeconds();
        long minutes = elapsed / 60;
        long secs = elapsed % 60;
        String timeStr = String.format("%02d:%02d", minutes, secs);
        if (game.getState() == GameState.WAITING) timeStr = "En attente";

        // Récupérer et trier les équipes par score (descendant) puis par dernier temps (ascendant)
        List<BingoTeam> sortedTeams = plugin.getTeamManager().getTeams().stream()
                .filter(t -> !t.getName().equalsIgnoreCase("Spectateur"))
                .sorted((t1, t2) -> {
                    if (t1.getScore() != t2.getScore()) {
                        return Integer.compare(t2.getScore(), t1.getScore());
                    }
                    return Long.compare(t1.getLastScoreTime(), t2.getLastScoreTime());
                })
                .collect(Collectors.toList());

        String title = plugin.getConfig().getString("scoreboard.title", "§6§lBINGO");
        List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");

        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerScoreboard(player, title, lines, timeStr, sortedTeams);
        }
    }

    private void updatePlayerScoreboard(Player player, String title, List<String> linesConfig, String timeStr, List<BingoTeam> sortedTeams) {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("bingo_board", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Scoreboard lit de bas en haut (index dégressif)
        int scoreIndex = linesConfig.size();

        for (String line : linesConfig) {
            String formatted = line.replace("{time}", timeStr);

            for (int i = 0; i < sortedTeams.size(); i++) {
                String ph = "{team_" + (i + 1) + "}";
                if (formatted.contains(ph)) {
                    BingoTeam t = sortedTeams.get(i);
                    // Ex: "1. Bleu - 12 pts"
                    String teamInfo = "§f" + (i + 1) + ". " + t.getChatColor() + t.getName() + " §7- §b" + t.getScore() + " pts";
                    
                    if (t.isFinished()) {
                        long fin = t.getFinishedTime() / 1000;
                        teamInfo += " §e[Fini en " + String.format("%02d:%02d", fin / 60, fin % 60) + "]";
                    }
                    formatted = formatted.replace(ph, teamInfo);
                }
            }
            
            // Les placeholders de teams qui n'existent pas deviennent vides
            formatted = formatted.replaceAll("\\{team_\\d+\\}", "");
            
            // Astuce anti-doublon: ajouter des couleurs invisibles a la fin s'il y a des lignes vides
            if (formatted.isEmpty()) {
                formatted = "§" + "f".repeat(scoreIndex % 10); // Ligne vide unique
            }

            // Ne pas afficher plus long que la limite Scoreboard Bukkit 1.21 (illimité en théorie mtn)
            objective.getScore(formatted).setScore(scoreIndex);
            scoreIndex--;
        }

        player.setScoreboard(board);
    }
}
