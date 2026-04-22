package fr.bingo.game;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreboardManager {

    private final BingoPlugin plugin;
    private int scorePage = 0;
    private long lastPageSwitch = 0;
    private static final int ENTRIES_PER_PAGE = 5;
    private static final long PAGE_SWITCH_INTERVAL_MS = 10000; // 10 secondes

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

        long elapsed = game.getElapsedSeconds();
        long minutes = elapsed / 60;
        long secs = elapsed % 60;
        String timeStr = String.format("%02d:%02d", minutes, secs);
        if (game.getState() == GameState.WAITING)
            timeStr = "En attente";

        // Construire le classement dynamique (tout le monde)
        List<ScoreEntry> ranking = buildRanking(tm, game);

        // Gérer le défilement des pages
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

        String title = plugin.getConfig().getString("scoreboard.title", "§6§lBINGO");

        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerScoreboard(player, title, timeStr, ranking, tm, game, totalPages);
        }
    }

    /**
     * Construit le classement : en FFA → joueurs, en équipe → équipes actives
     * non-vides.
     * Top 5 max.
     */
    private List<ScoreEntry> buildRanking(TeamManager tm, BingoGame game) {
        List<ScoreEntry> entries = new ArrayList<>();

        if (tm.isSoloMode()) {
            // Mode FFA : chaque joueur en ligne non spectateur est une entrée
            for (Player player : Bukkit.getOnlinePlayers()) {
                BingoTeam team = tm.getPlayerTeam(player);
                if (team != null && team.getName().equals("Spectateur"))
                    continue;

                entries.add(new ScoreEntry(
                        "§f" + player.getName(),
                        team != null ? team.getScore() : 0,
                        team != null && team.isFinished(),
                        team != null ? team.getFinishedTime() : 0));
            }
        } else {
            // Mode équipes : uniquement les équipes actives non-vides
            for (BingoTeam team : tm.getActiveTeams()) {
                if (team.getPlayers().isEmpty())
                    continue;
                entries.add(new ScoreEntry(
                        team.getChatColor() + team.getName(),
                        team.getScore(),
                        team.isFinished(),
                        team.getFinishedTime()));
            }
        }

        // Trier : terminé en premier (par temps), puis par score décroissant
        entries.sort((a, b) -> {
            if (a.finished && !b.finished)
                return -1;
            if (!a.finished && b.finished)
                return 1;
            if (a.finished && b.finished)
                return Long.compare(a.finishedTime, b.finishedTime);
            if (a.score != b.score)
                return Integer.compare(b.score, a.score);
            return 0;
        });

        return entries;
    }

    private void updatePlayerScoreboard(Player player, String title, String timeStr,
            List<ScoreEntry> ranking, TeamManager tm, BingoGame game, int totalPages) {

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null)
            return;

        Scoreboard board = manager.getNewScoreboard();

        // ── TAB : couleurs des noms ──
        if (!tm.isSoloMode()) {
            // Mode équipe : appliquer les couleurs d'équipe dans le tab
            List<BingoTeam> allTeams = new ArrayList<>(tm.getTeams());
            allTeams.add(tm.getSpectatorTeam());

            for (BingoTeam bt : allTeams) {
                String teamId = "bg_" + bt.getName().toLowerCase().substring(0, Math.min(bt.getName().length(), 12));
                org.bukkit.scoreboard.Team sbTeam = board.registerNewTeam(teamId);
                sbTeam.setColor(bt.getChatColor());
                sbTeam.setPrefix(bt.getChatColor().toString());
                for (UUID uuid : bt.getPlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null)
                        sbTeam.addEntry(p.getName());
                }
            }
        } else {
            // Mode FFA : tout le monde en blanc, pas de couleur
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

        // Espace
        lines.add(" ");

        // Progression de l'équipe du joueur
        BingoTeam playerTeam = tm.getPlayerTeam(player);
        if (playerTeam != null && !playerTeam.getName().equals("Spectateur")) {
            int found = playerTeam.getUnlockedObjectives().size();
            int total = game.getGrid().getObjectives().size();
            if (total > 0) {
                lines.add("§fProgression: §a" + found + "§7/" + total);
            } else {
                lines.add("§fProgression: §7-");
            }
            lines.add("§fPoints: §e" + playerTeam.getScore());
        } else {
            lines.add("§fProgression: §7-");
            lines.add("§fPoints: §7-");
        }

        // Espace
        lines.add("  ");

        // Classement dynamique (Pagination)
        int start = scorePage * ENTRIES_PER_PAGE;
        int end = Math.min(start + ENTRIES_PER_PAGE, ranking.size());

        if (ranking.isEmpty()) {
            lines.add("§7En attente...");
            // Padding pour garder la taille constante (5 lignes de classement)
            for (int i = 0; i < 4; i++) {
                lines.add("§" + (i + 1));
            }
            lines.add("   "); // Place pour l'indicateur de page
        } else {
            // Afficher les entrées de la page actuelle
            for (int i = start; i < end; i++) {
                ScoreEntry entry = ranking.get(i);
                String prefix = "§f" + (i + 1) + ". ";
                String line;
                if (entry.finished) {
                    // Afficher le temps de complétion au lieu de ✔
                    long elapsedSec = (entry.finishedTime - game.getStartTime()) / 1000;
                    int min = (int) (elapsedSec / 60);
                    int sec = (int) (elapsedSec % 60);
                    String finishTime = String.format("%02d:%02d", min, sec);
                    line = prefix + entry.name + " §7- §a" + finishTime;
                } else {
                    line = prefix + entry.name + " §7- §b" + entry.score;
                }
                lines.add(line);
            }

            // Padding pour garder la taille constante (5 lignes de classement)
            int shown = end - start;
            for (int i = 0; i < (ENTRIES_PER_PAGE - shown); i++) {
                lines.add("§" + (i + 5)); // Codes couleurs invisibles pour lignes uniques
            }

            // Indicateur de page (seulement si plusieurs pages)
            if (totalPages > 1) {
                lines.add("§8Page " + (scorePage + 1) + "/" + totalPages);
            } else {
                lines.add("    "); // Espace pour garder la taille
            }
        }

        lines.add("§8§m-----------------");
        lines.add("§fPlugins by HEL");

        // Écrire le scoreboard
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
