package fr.bingo.game;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class ScoreboardManager {

    private final BingoPlugin plugin;
    private int scorePage = 0;
    private long lastPageSwitch = 0;
    private static final int ENTRIES_PER_PAGE = 5;
    private static final long PAGE_SWITCH_INTERVAL_MS = 10000;

    public ScoreboardManager(BingoPlugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateScoreboards, 20L, 20L);
    }

    private void updateScoreboards() {
        BingoGame game = plugin.getBingoGame();
        TeamManager tm = plugin.getTeamManager();

        boolean isBingoMode = plugin.getScenarioManager()
                .isScenarioEnabled(fr.bingo.scenario.BingoScenario.class);

        long elapsed = game.getElapsedSeconds();
        long minutes = elapsed / 60;
        long secs = elapsed % 60;
        String timeStr = String.format("%02d:%02d", minutes, secs);
        if (game.getState() == GameState.WAITING) timeStr = "En attente";

        // Classement
        List<ScoreEntry> ranking = buildRanking(tm, game, isBingoMode);

        // Pagination
        int totalPages = (int) Math.ceil((double) ranking.size() / ENTRIES_PER_PAGE);
        if (totalPages <= 1) {
            scorePage = 0;
        } else {
            long now = System.currentTimeMillis();
            if (now - lastPageSwitch > PAGE_SWITCH_INTERVAL_MS) {
                lastPageSwitch = now;
                scorePage = (scorePage + 1) % totalPages;
            }
        }

        // Titre : nom du preset chargé, sinon "HEL"
        String title = game.getActivePresetName() != null
                ? "§6§l" + game.getActivePresetName()
                : "§6§lHEL";

        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerScoreboard(player, title, timeStr, ranking, tm, game, totalPages, isBingoMode);
        }
    }

    private List<ScoreEntry> buildRanking(TeamManager tm, BingoGame game, boolean isBingoMode) {
        List<ScoreEntry> entries = new ArrayList<>();

        if (tm.isSoloMode()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                BingoTeam team = tm.getPlayerTeam(player);
                if (team != null && team.getName().equals("Spectateur")) continue;

                int score = isBingoMode
                        ? (team != null ? team.getScore() : 0)
                        : (team != null ? team.getKills() : 0);

                entries.add(new ScoreEntry(
                        "§f" + player.getName(),
                        score,
                        team != null && team.isFinished(),
                        team != null ? team.getFinishedTime() : 0));
            }
        } else {
            for (BingoTeam team : tm.getActiveTeams()) {
                if (team.getPlayers().isEmpty()) continue;

                int score = isBingoMode ? team.getScore() : team.getKills();

                entries.add(new ScoreEntry(
                        team.getChatColor() + team.getName(),
                        score,
                        team.isFinished(),
                        team.getFinishedTime()));
            }
        }

        entries.sort((a, b) -> {
            if (a.finished && !b.finished) return -1;
            if (!a.finished && b.finished) return 1;
            if (a.finished && b.finished) return Long.compare(a.finishedTime, b.finishedTime);
            if (a.score != b.score) return Integer.compare(b.score, a.score);
            return 0;
        });

        return entries;
    }

    private void updatePlayerScoreboard(Player player, String title, String timeStr,
            List<ScoreEntry> ranking, TeamManager tm, BingoGame game, int totalPages, boolean isBingoMode) {

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();

        // ── TAB : couleurs des noms ──
        if (!tm.isSoloMode()) {
            List<BingoTeam> allTeams = new ArrayList<>(tm.getTeams());
            allTeams.add(tm.getSpectatorTeam());

            for (BingoTeam bt : allTeams) {
                String teamId = "bg_" + bt.getName().toLowerCase().substring(0, Math.min(bt.getName().length(), 12));
                org.bukkit.scoreboard.Team sbTeam = board.registerNewTeam(teamId);
                sbTeam.setColor(bt.getChatColor());
                sbTeam.setPrefix(bt.getChatColor().toString());
                for (UUID uuid : bt.getPlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) sbTeam.addEntry(p.getName());
                }
            }
        } else {
            org.bukkit.scoreboard.Team sbTeam = board.registerNewTeam("bg_ffa");
            sbTeam.setColor(org.bukkit.ChatColor.WHITE);
            sbTeam.setPrefix("§f");
            for (Player p : Bukkit.getOnlinePlayers()) {
                sbTeam.addEntry(p.getName());
            }
        }

        // ── Sidebar ──
        Objective objective = board.registerNewObjective("bingo_board", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = new ArrayList<>();
        lines.add("§8§m-----------------");

        // Chrono
        lines.add("§f⏱ Chrono: §b" + timeStr);

        // PVP Timer : afficher QUAND le pvp s'active (timestamp absolu)
        if (game.getState() == GameState.PLAYING && !game.isPvpEnabled()) {
            if (game.isPvpDisabled()) {
                lines.add("§f⚔ PVP: §cDésactivé");
            } else {
                // Afficher le moment où le PVP s'active (pvpTimerMinutes après le start)
                int pvpMinutes = game.getPvpTimerMinutes();
                lines.add("§f⚔ PVP: §e" + pvpMinutes + ":00");
            }
        } else if (game.getState() == GameState.PLAYING && game.isPvpEnabled()) {
            lines.add("§f⚔ PVP: §aActivé");
        }

        lines.add(" ");

        if (isBingoMode) {
            // ── Mode Bingo : Progression ──
            BingoTeam playerTeam = tm.getPlayerTeam(player);
            if (playerTeam != null && !playerTeam.getName().equals("Spectateur")) {
                int found = playerTeam.getUnlockedObjectives().size();
                int total = game.getGrid().getObjectives().size();

                if (game.getGrid().getSize() == 1) {
                    if (playerTeam.isFinished()) {
                        long elapsedSec = (playerTeam.getFinishedTime() - game.getStartTime()) / 1000;
                        String ft = String.format("%02d:%02d", elapsedSec / 60, elapsedSec % 60);
                        lines.add("§fTemps: §a" + ft);
                    } else {
                        lines.add("§fObjectif: §cEn recherche...");
                    }
                } else {
                    if (total > 0) {
                        lines.add("§fProgression: §a" + found + "§7/" + total);
                    } else {
                        lines.add("§fProgression: §7-");
                    }
                    lines.add("§fPoints: §e" + playerTeam.getScore());
                }
            } else {
                lines.add("§fProgression: §7-");
            }
        } else {
            // ── Mode UHC : Kills ──
            BingoTeam playerTeam = tm.getPlayerTeam(player);
            if (playerTeam != null && !playerTeam.getName().equals("Spectateur")) {
                lines.add("§f☠ Kills: §c" + playerTeam.getKills());
            } else {
                lines.add("§f☠ Kills: §7-");
            }

            // Bordure
            BorderManager bm = plugin.getBorderManager();
            if (game.getState() == GameState.PLAYING) {
                org.bukkit.WorldBorder wb = Bukkit.getWorlds().get(0).getWorldBorder();
                int currentSize = (int) wb.getSize();
                lines.add("§f📏 Bordure: §b" + currentSize);
            }
        }

        lines.add("  ");

        // Classement
        int start = scorePage * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, ranking.size());

        String scoreLabel = isBingoMode ? "pts" : "kills";

        if (ranking.isEmpty()) {
            lines.add("§7En attente...");
            for (int i = 0; i < 4; i++) lines.add("§" + (i + 1));
            lines.add("   ");
        } else {
            for (int i = start; i < end; i++) {
                ScoreEntry entry = ranking.get(i);
                String prefix = "§f" + (i + 1) + ". ";
                String line;
                if (entry.finished) {
                    long elapsedSec = (entry.finishedTime - game.getStartTime()) / 1000;
                    String finishTime = String.format("%02d:%02d", (int)(elapsedSec / 60), (int)(elapsedSec % 60));
                    line = prefix + entry.name + " §7- §a" + finishTime;
                } else {
                    line = prefix + entry.name + " §7- §b" + entry.score + " " + scoreLabel;
                }
                lines.add(line);
            }

            int shown = end - start;
            for (int i = 0; i < (ENTRIES_PER_PAGE - shown); i++) {
                lines.add("§" + (i + 5));
            }

            if (totalPages > 1) {
                lines.add("§8Page " + (scorePage + 1) + "/" + totalPages);
            } else {
                lines.add("    ");
            }
        }

        lines.add("§8§m-----------------");
        lines.add("§fPlugins by HEL");

        // Écrire
        int scoreIndex = lines.size();
        Set<String> usedLines = new HashSet<>();

        for (String line : lines) {
            String uniqueLine = line;
            while (usedLines.contains(uniqueLine)) {
                uniqueLine += "§r";
            }
            usedLines.add(uniqueLine);
            objective.getScore(uniqueLine).setScore(scoreIndex);
            scoreIndex--;
        }

        player.setScoreboard(board);
    }

    private static class ScoreEntry {
        final String name;
        final int score;
        final boolean finished;
        final long finishedTime;

        ScoreEntry(String name, int score, boolean finished, long finishedTime) {
            this.name = name;
            this.score = score;
            this.finished = finished;
            this.finishedTime = finishedTime;
        }
    }
}
