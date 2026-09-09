package fr.hel.commands;

import fr.hel.HelPlugin;
import fr.hel.game.HelGame;
import fr.hel.game.GameState;
import fr.hel.team.HelTeam;
import fr.hel.team.TeamManager;
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
            sender.sendMessage("\u00A7cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        HelGame game = HelPlugin.getInstance().getHelGame();
        if (game.getState() != GameState.PLAYING) {
            if (game.getState() == GameState.FINISHED) {
                player.sendMessage("\u00A7cLa partie est d\u00E9j\u00E0 termin\u00E9e !");
            } else {
                player.sendMessage("\u00A7cLa partie n'est pas en cours !");
            }
            return true;
        }

        TeamManager tm = HelPlugin.getInstance().getTeamManager();
        HelTeam team = tm.getPlayerTeam(player);

        if (team == null || team.getName().equals("Spectateur")) {
            player.sendMessage("\u00A7cVous ne participez pas \u00E0 la partie.");
            return true;
        }

        if (team.isFinished()) {
            player.sendMessage("\u00A7cVous avez d\u00E9j\u00E0 termin\u00E9 la partie !");
            return true;
        }

        if (tm.isSoloMode()) {
            handleSoloFF(player, team);
        } else {
            handleTeamFF(player, team);
        }

        return true;
    }

    private void handleSoloFF(Player player, HelTeam team) {
        boolean isHel = HelPlugin.getInstance().getScenarioManager().isScenarioEnabled(fr.hel.scenario.HelScenario.class);
        String prefix = isHel ? "\u00A78[\u00A76Hel\u00A78] " : "\u00A78[\u00A7cHEL\u00A78] ";
        
        team.setFinished(true);
        
        if (isHel) {
            player.setGameMode(GameMode.SPECTATOR);
            player.getInventory().clear();
        } else {
            player.setHealth(0); // Tue le joueur pour drop son inventaire en UHC
        }
        
        Bukkit.broadcastMessage(prefix + "\u00A7f" + player.getName() + " \u00A7ca abandonn\u00E9 la partie.");
        player.sendMessage("\u00A7cVous avez abandonn\u00E9.");
        
        // V\u00E9rification de fin de partie
        HelPlugin.getInstance().getHelListener().checkEndCondition();
    }

    private void handleTeamFF(Player player, HelTeam team) {
        team.toggleForfeitVote(player.getUniqueId());
        
        if (team.isAllForfeited()) {
            boolean isHel = HelPlugin.getInstance().getScenarioManager().isScenarioEnabled(fr.hel.scenario.HelScenario.class);
            String prefix = isHel ? "\u00A78[\u00A76Hel\u00A78] " : "\u00A78[\u00A7cHEL\u00A78] ";
            
            team.setFinished(true);
            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    if (isHel) {
                        p.setGameMode(GameMode.SPECTATOR);
                        p.getInventory().clear();
                    } else if (p.getGameMode() == GameMode.SURVIVAL) {
                        p.setHealth(0); // Tue les survivants
                    }
                }
            }
            Bukkit.broadcastMessage(prefix + team.getChatColor() + "L'\u00E9quipe " + team.getName() + " \u00A7ca abandonn\u00E9 la partie.");
            
            // V\u00E9rification de fin de partie
            HelPlugin.getInstance().getHelListener().checkEndCondition();
        } else {
            boolean voted = team.hasVotedForfeit(player.getUniqueId());
            if (voted) {
                player.sendMessage("\u00A7aVous avez vot\u00E9 pour abandonner (\u00A7e" + team.getForfeitVoteCount() + "/" + team.getPlayers().size() + "\u00A7a).");
            } else {
                player.sendMessage("\u00A7cVous avez annul\u00E9 votre vote d'abandon.");
            }
        }
    }
}
