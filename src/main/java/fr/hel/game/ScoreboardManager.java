package fr.hel.game;

import fr.hel.HelPlugin;
import fr.hel.team.HelTeam;
import fr.hel.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class ScoreboardManager {

    private final HelPlugin plugin;
    private int scorePage = 0;
    private long lastPageSwitch = 0;
    private static final int ENTRIES_PER_PAGE = 5;
    private static final long PAGE_SWITCH_INTERVAL_MS = 10000;

    public ScoreboardManager(HelPlugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateScoreboards, 20L, 20L);
    }

    private void updateScoreboards() {
        HelGame game = plugin.getHelGame();
        TeamManager tm = plugin.getTeamManager();

        boolean isHelMode = plugin.getScenarioManager()
                .isScenarioEnabled(fr.hel.scenario.HelScenario.class);

        long elapsed = game.getElapsedSeconds();
        long minutes = elapsed / 60;
        long secs = elapsed % 60;
        String timeStr = String.format("%02d:%02d", minutes, secs);
        if (game.getState() == GameState.WAITING) timeStr = "En attente";

        // Classement
        List<ScoreEntry> ranking = buildRanking(tm, game, isHelMode);

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

        // Titre : nom du preset charg\u00E9, sinon "HEL"
        String title = game.getActivePresetName() != null
                ? "\u00A76\u00A7l" + game.getActivePresetName()
                : "\u00A76\u00A7lHEL";

        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerScoreboard(player, title, timeStr, ranking, tm, game, totalPages, isHelMode);
        }
    }

    private List<ScoreEntry> buildRanking(TeamManager tm, HelGame game, boolean isHelMode) {
        List<ScoreEntry> entries = new ArrayList<>();

        if (tm.isSoloMode()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                HelTeam team = tm.getPlayerTeam(player);
                if (team != null && team.getName().equals("Spectateur")) continue;

                int score = isHelMode
                        ? (team != null ? team.getScore() : 0)
                        : (team != null ? team.getKills() : 0);

                entries.add(new ScoreEntry(
                        "\u00A7f" + player.getName(),
                        score,
                        team != null && team.isFinished(),
                        team != null ? team.getFinishedTime() : 0));
            }
        } else {
            for (HelTeam team : tm.getActiveTeams()) {
                if (team.getPlayers().isEmpty()) continue;

                int score = isHelMode ? team.getScore() : team.getKills();

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
            List<ScoreEntry> ranking, TeamManager tm, HelGame game, int totalPages, boolean isHelMode) {

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;

        Scoreboard board = manager.getNewScoreboard();

        // \u2500\u2500 TAB : couleurs des noms et TRI (Rang puis Equipe) \u2500\u2500
        for (Player p : Bukkit.getOnlinePlayers()) {
            HelTeam bt = tm.getPlayerTeam(p);
            
            String teamName;
            org.bukkit.ChatColor teamColor;
            
            if (tm.isSoloMode()) {
                teamName = "ffa";
                teamColor = org.bukkit.ChatColor.WHITE;
            } else {
                teamName = (bt != null) ? bt.getName().toLowerCase() : "zz_spectator";
                teamColor = (bt != null) ? bt.getChatColor() : org.bukkit.ChatColor.GRAY;
            }

            // Determine rank prefix for sorting
            String rank = HelPlugin.getInstance().getRankManager().getRank(p.getUniqueId());
            String rankSort = "3_"; // joueur
            if (rank.equals("createur")) rankSort = "1_";
            else if (rank.equals("vip")) rankSort = "2_";
            
            fr.hel.scenario.AnonymousScenario anon = HelPlugin.getInstance().getScenarioManager().getScenario(fr.hel.scenario.AnonymousScenario.class);
            if (anon != null && anon.isEnabled() && anon.isGlitchedNames()) {
                teamColor = org.bukkit.ChatColor.WHITE;
            }

            String teamId = rankSort + teamName;
            if (teamId.length() > 16) teamId = teamId.substring(0, 16);
            
            org.bukkit.scoreboard.Team sbTeam = board.getTeam(teamId);
            if (sbTeam == null) {
                sbTeam = board.registerNewTeam(teamId);
                sbTeam.setColor(teamColor);
                sbTeam.setPrefix(teamColor.toString());
            }
            sbTeam.addEntry(p.getName());
        }

        // \u2500\u2500 Sidebar \u2500\u2500
        // Le titre doit contenir le logo pour \u00EAtre parfaitement centr\u00E9 (m\u00EAme si \u00E7a cr\u00E9e un fond noir)
        Objective objective = board.registerNewObjective("bingo_board", "dummy", "\uE001");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = new ArrayList<>();
        // Espaces pour d\u00E9caler le texte sous le logo
        lines.add(" ");
        lines.add("  ");
        lines.add("   ");
        lines.add("\u00A78\u00A7m-----------------");

        // Chrono
        lines.add("\u00A7f\u23F1 Chrono: \u00A7b" + timeStr);

        // PVP Timer : afficher QUAND le pvp s'active (timestamp absolu)
        if (game.getState() == GameState.PLAYING && !game.isPvpEnabled()) {
            if (game.isPvpDisabled()) {
                lines.add("\u00A7f\u2694 PVP: \u00A7cD\u00E9sactiv\u00E9");
            } else {
                // Afficher le moment o\u00F9 le PVP s'active (pvpTimerMinutes apr\u00E8s le start)
                int pvpMinutes = game.getPvpTimerMinutes();
                lines.add("\u00A7f\u2694 PVP: \u00A7e" + pvpMinutes + ":00");
            }
        } else if (game.getState() == GameState.PLAYING && game.isPvpEnabled()) {
            lines.add("\u00A7f\u2694 PVP: \u00A7aActiv\u00E9");
        }

        lines.add(" ");

        if (isHelMode) {
            // \u2500\u2500 Mode Hel : Progression \u2500\u2500
            HelTeam playerTeam = tm.getPlayerTeam(player);
            if (playerTeam != null && !playerTeam.getName().equals("Spectateur")) {
                int found = playerTeam.getUnlockedObjectives().size();
                int total = game.getGrid().getObjectives().size();

                if (game.getGrid().getSize() == 1) {
                    if (playerTeam.isFinished()) {
                        long elapsedSec = (playerTeam.getFinishedTime() - game.getStartTime()) / 1000;
                        String ft = String.format("%02d:%02d", elapsedSec / 60, elapsedSec % 60);
                        lines.add("\u00A7fTemps: \u00A7a" + ft);
                    } else {
                        lines.add("\u00A7fObjectif: \u00A7cEn recherche...");
                    }
                } else {
                    if (total > 0) {
                        lines.add("\u00A7fProgression: \u00A7a" + found + "\u00A77/" + total);
                    } else {
                        lines.add("\u00A7fProgression: \u00A77-");
                    }
                    lines.add("\u00A7fPoints: \u00A7e" + playerTeam.getScore());
                }
            } else {
                lines.add("\u00A7fProgression: \u00A77-");
            }
        } else {
            // \u2500\u2500 Mode UHC : Kills \u2500\u2500
            HelTeam playerTeam = tm.getPlayerTeam(player);
            if (playerTeam != null && !playerTeam.getName().equals("Spectateur")) {
                lines.add("\u00A7f\u2620 Kills: \u00A7c" + playerTeam.getKills());
            } else {
                lines.add("\u00A7f\u2620 Kills: \u00A77-");
            }

            // Bordure
            BorderManager bm = plugin.getBorderManager();
            if (game.getState() == GameState.PLAYING) {
                org.bukkit.WorldBorder wb = Bukkit.getWorlds().get(0).getWorldBorder();
                int currentSize = (int) wb.getSize();
                lines.add("\u00A7f\u1F4CF Bordure: \u00A7b" + currentSize);
            }
        }

        lines.add("  ");

        // Classement
        int start = scorePage * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, ranking.size());

        String scoreLabel = isHelMode ? "pts" : "kills";

        if (ranking.isEmpty()) {
            lines.add("\u00A77En attente...");
            for (int i = 0; i < 4; i++) lines.add("\u00A7" + (i + 1));
            lines.add("   ");
        } else {
            for (int i = start; i < end; i++) {
                ScoreEntry entry = ranking.get(i);
                String prefix = "\u00A7f" + (i + 1) + ". ";
                String line;
                if (entry.finished) {
                    long elapsedSec = (entry.finishedTime - game.getStartTime()) / 1000;
                    String finishTime = String.format("%02d:%02d", (int)(elapsedSec / 60), (int)(elapsedSec % 60));
                    line = prefix + entry.name + " \u00A77- \u00A7a" + finishTime;
                } else {
                    line = prefix + entry.name + " \u00A77- \u00A7b" + entry.score + " " + scoreLabel;
                }
                lines.add(line);
            }

            int shown = end - start;
            for (int i = 0; i < (ENTRIES_PER_PAGE - shown); i++) {
                lines.add("\u00A7" + (i + 5));
            }

            if (totalPages > 1) {
                lines.add("\u00A78Page " + (scorePage + 1) + "/" + totalPages);
            } else {
                lines.add("    ");
            }
        }

        lines.add("\u00A78\u00A7m-----------------");
        lines.add("\u00A7fPlugins by HEL");

        // \u00C9crire
        int scoreIndex = lines.size();
        Set<String> usedLines = new HashSet<>();

        for (String line : lines) {
            String uniqueLine = line;
            while (usedLines.contains(uniqueLine)) {
                uniqueLine += "\u00A7r";
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
