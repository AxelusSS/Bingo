package fr.bingo.game;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreboardManager {

    private final BingoPlugin plugin;

    public ScoreboardManager(BingoPlugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateScoreboards, 20L, 20L);
    }

    private void updateScoreboards() {
        BingoGame game = plugin.getBingoGame();

        long elapsed = game.getElapsedSeconds();
        long minutes = elapsed / 60;
        long secs = elapsed % 60;
        String timeStr = String.format("%02d:%02d", minutes, secs);
        if (game.getState() == GameState.WAITING) timeStr = "En attente";

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

        // ── TAB : Enregistrer les équipes pour colorer les noms ──
        // On fait une copie de la liste pour ne pas modifier l'originale
        List<BingoTeam> allTeams = new ArrayList<>(plugin.getTeamManager().getTeams());
        allTeams.add(plugin.getTeamManager().getSpectatorTeam());

        for (BingoTeam bt : allTeams) {
            String teamId = "bg_" + bt.getName().toLowerCase().substring(0, Math.min(bt.getName().length(), 12));
            org.bukkit.scoreboard.Team sbTeam = board.registerNewTeam(teamId);
            sbTeam.setColor(bt.getChatColor());
            sbTeam.setPrefix(bt.getChatColor().toString());
            for (java.util.UUID uuid : bt.getPlayers()) {
                org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
                if (p != null) sbTeam.addEntry(p.getName());
            }
        }

        // ── Sidebar ──
        Objective objective = board.registerNewObjective("bingo_board", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int scoreIndex = linesConfig.size();

        for (String line : linesConfig) {
            String formatted = line.replace("{time}", timeStr);

            for (int i = 0; i < sortedTeams.size(); i++) {
                String ph = "{team_" + (i + 1) + "}";
                if (formatted.contains(ph)) {
                    BingoTeam t = sortedTeams.get(i);
                    String teamInfo = "§f" + (i + 1) + ". " + t.getChatColor() + t.getName() + " §7- §b" + t.getScore() + " pts";

                    if (t.isFinished()) {
                        long elapsedSec = (t.getFinishedTime() - plugin.getBingoGame().getStartTime()) / 1000;
                        teamInfo += " §e[" + String.format("%02d:%02d", elapsedSec / 60, elapsedSec % 60) + "]";
                    }
                    formatted = formatted.replace(ph, teamInfo);
                }
            }

            formatted = formatted.replaceAll("\\{team_\\d+\\}", "");

            if (formatted.isEmpty()) {
                formatted = " ".repeat(Math.max(1, scoreIndex));
            }

            objective.getScore(formatted).setScore(scoreIndex);
            scoreIndex--;
        }

        player.setScoreboard(board);
    }
}
