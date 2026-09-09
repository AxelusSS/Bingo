package fr.hel.commands;

import fr.hel.HelPlugin;
import fr.hel.team.HelTeam;
import fr.hel.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TeamCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Seul un joueur peut faire cela.");
            return true;
        }

        TeamManager teamManager = HelPlugin.getInstance().getTeamManager();

        if (label.equalsIgnoreCase("tj")) {
            if (args.length < 1) {
                player.sendMessage("\u00A7cUsage: /tj <Rouge|Bleu|Vert|Jaune>");
                return true;
            }
            if (teamManager.isTeamsLocked() && !player.hasPermission("bingo.admin")) {
                player.sendMessage("\u00A7cLes \u00E9quipes sont verrouill\u00E9es !");
                return true;
            }
            String colorName = args[0];
            for (HelTeam t : teamManager.getTeams()) {
                if (t.getName().equalsIgnoreCase(colorName)) {
                    teamManager.joinTeam(player, t);
                    return true;
                }
            }
            player.sendMessage("\u00A7c\u00C9quipe introuvable !");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("\u00A7cUsage: /team <menu|join|leave|random|lock|set|setsize>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "menu":
                player.openInventory(new fr.hel.gui.TeamSelectorGUI().getInventory());
                break;

            case "join":
                if (teamManager.isTeamsLocked() && !player.hasPermission("bingo.admin")) {
                    player.sendMessage("\u00A7cLes \u00E9quipes sont verrouill\u00E9es pour cette partie !");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("\u00A7cUsage: /team join <Rouge|Bleu|Vert|Jaune>");
                    return true;
                }
                String colorName = args[1];
                for (HelTeam t : teamManager.getTeams()) {
                    if (t.getName().equalsIgnoreCase(colorName)) {
                        teamManager.joinTeam(player, t);
                        return true;
                    }
                }
                player.sendMessage("\u00A7c\u00C9quipe introuvable !");
                break;

            case "leave":
                if (teamManager.isTeamsLocked() && !player.hasPermission("bingo.admin")) {
                    player.sendMessage("\u00A7cLes \u00E9quipes sont verrouill\u00E9es !");
                    return true;
                }
                HelTeam current = teamManager.getPlayerTeam(player);
                if (current != null) {
                    teamManager.removePlayerFromTeam(player);
                    player.sendMessage("\u00A7aVous avez quitt\u00E9 votre \u00E9quipe.");
                    teamManager.joinTeam(player, teamManager.getSpectatorTeam());
                } else {
                    player.sendMessage("\u00A7cVous n'\u00EAtes pas dans une \u00E9quipe.");
                }
                break;

            case "random":
                if (!player.hasPermission("bingo.admin")) {
                    player.sendMessage("\u00A7cVous n'avez pas la permission.");
                    return true;
                }
                int numTeams = teamManager.getTeams().size();
                if (args.length > 1) {
                    try {
                        numTeams = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {}
                }
                teamManager.randomizeTeams(new ArrayList<>(Bukkit.getOnlinePlayers()), numTeams);
                Bukkit.broadcastMessage("\u00A7eLes \u00E9quipes ont \u00E9t\u00E9 g\u00E9n\u00E9r\u00E9es de fa\u00E7on al\u00E9atoire !");
                break;

            case "lock":
                if (!player.hasPermission("bingo.admin")) {
                    player.sendMessage("\u00A7cPermission refus\u00E9e.");
                    return true;
                }
                teamManager.setTeamsLocked(!teamManager.isTeamsLocked());
                player.sendMessage("\u00A7aVerrouillage des \u00E9quipes : " + (teamManager.isTeamsLocked() ? "\u00A7cActif" : "\u00A7aD\u00E9sactiv\u00E9"));
                break;

            case "set":
                if (!player.hasPermission("bingo.admin")) {
                    player.sendMessage("\u00A7cPermission refus\u00E9e.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage("\u00A7cUsage: /team set <joueur> <\u00E9quipe>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("\u00A7cJoueur introuvable.");
                    return true;
                }
                String targetTeamName = args[2];
                for (HelTeam t : teamManager.getTeams()) {
                    if (t.getName().equalsIgnoreCase(targetTeamName)) {
                        teamManager.forceJoinTeam(target, t);
                        player.sendMessage("\u00A7aJoueur assign\u00E9 !");
                        return true;
                    }
                }
                player.sendMessage("\u00A7c\u00C9quipe introuvable.");
                break;

            case "setsize":
                if (!player.hasPermission("bingo.admin")) {
                    player.sendMessage("\u00A7cPermission refus\u00E9e.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("\u00A7cUsage: /team setsize <nombre>");
                    return true;
                }
                try {
                    int max = Integer.parseInt(args[1]);
                    teamManager.setMaxPlayersPerTeam(max);
                    player.sendMessage("\u00A7aLa taille maximale par \u00E9quipe est maintenant de " + max + " joueurs.");
                } catch (NumberFormatException e) {
                    player.sendMessage("\u00A7cNombre invalide.");
                }
                break;

            default:
                player.sendMessage("\u00A7cCommande inconnue.");
                break;
        }

        return true;
    }
}
