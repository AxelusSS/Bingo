package fr.bingo.commands;

import fr.bingo.BingoPlugin;
import fr.bingo.game.BingoGame;
import fr.bingo.game.GameState;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FFCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        if (game.getState() != GameState.PLAYING) {
            if (game.getState() == GameState.FINISHED) {
                player.sendMessage("§cLa partie est déjà terminée !");
            } else {
                player.sendMessage("§cLa partie n'est pas en cours !");
            }
            return true;
        }

        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        BingoTeam team = tm.getPlayerTeam(player);

        if (team == null || team.getName().equals("Spectateur")) {
            player.sendMessage("§cVous ne participez pas à la partie.");
            return true;
        }

        if (team.isFinished()) {
            player.sendMessage("§cVous avez déjà terminé la partie !");
            return true;
        }

        if (tm.isSoloMode()) {
            handleSoloFF(player, team);
        } else {
            handleTeamFF(player, team);
        }

        return true;
    }

    private void handleSoloFF(Player player, BingoTeam team) {
        team.setFinished(true);
        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();
        
        Bukkit.broadcastMessage("§8[§6Bingo§8] §f" + player.getName() + " §ca abandonné la partie.");
        player.sendMessage("§cVous avez abandonné. Vous êtes désormais spectateur.");
        
        // Vérification de fin de partie
        BingoPlugin.getInstance().getBingoListener().checkEndCondition();
    }

    private void handleTeamFF(Player player, BingoTeam team) {
        team.toggleForfeitVote(player.getUniqueId());
        
        if (team.isAllForfeited()) {
            team.setFinished(true);
            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.setGameMode(GameMode.SPECTATOR);
                    p.getInventory().clear();
                }
            }
            Bukkit.broadcastMessage("§8[§6Bingo§8] " + team.getChatColor() + "L'équipe " + team.getName() + " §ca abandonné la partie.");
            
            // Vérification de fin de partie
            BingoPlugin.getInstance().getBingoListener().checkEndCondition();
        } else {
            boolean voted = team.hasVotedForfeit(player.getUniqueId());
            if (voted) {
                player.sendMessage("§aVous avez voté pour abandonner (§e" + team.getForfeitVoteCount() + "/" + team.getPlayers().size() + "§a).");
            } else {
                player.sendMessage("§cVous avez annulé votre vote d'abandon.");
            }
        }
    }
}
