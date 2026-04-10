package fr.bingo.commands;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
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

        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();

        if (label.equalsIgnoreCase("tj")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /tj <Rouge|Bleu|Vert|Jaune>");
                return true;
            }
            if (teamManager.isTeamsLocked() && !player.hasPermission("bingo.admin")) {
                player.sendMessage("§cLes équipes sont verrouillées !");
                return true;
            }
            String colorName = args[0];
            for (BingoTeam t : teamManager.getTeams()) {
                if (t.getName().equalsIgnoreCase(colorName)) {
                    teamManager.joinTeam(player, t);
                    return true;
                }
            }
            player.sendMessage("§cÉquipe introuvable !");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /team <menu|join|leave|random|lock|set|setsize>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "menu":
                player.openInventory(new fr.bingo.gui.TeamSelectorGUI().getInventory());
                break;

            case "join":
                if (teamManager.isTeamsLocked() && !player.hasPermission("bingo.admin")) {
                    player.sendMessage("§cLes équipes sont verrouillées pour cette partie !");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /team join <Rouge|Bleu|Vert|Jaune>");
                    return true;
                }
                String colorName = args[1];
                for (BingoTeam t : teamManager.getTeams()) {
                    if (t.getName().equalsIgnoreCase(colorName)) {
                        teamManager.joinTeam(player, t);
                        return true;
                    }
                }
                player.sendMessage("§cÉquipe introuvable !");
                break;

            case "leave":
                if (teamManager.isTeamsLocked() && !player.hasPermission("bingo.admin")) {
                    player.sendMessage("§cLes équipes sont verrouillées !");
                    return true;
                }
                BingoTeam current = teamManager.getPlayerTeam(player);
                if (current != null) {
                    teamManager.removePlayerFromTeam(player);
                    player.sendMessage("§aVous avez quitté votre équipe.");
                    teamManager.joinTeam(player, teamManager.getSpectatorTeam());
                } else {
                    player.sendMessage("§cVous n'êtes pas dans une équipe.");
                }
                break;

            case "random":
                if (!player.hasPermission("bingo.admin")) {
                    player.sendMessage("§cVous n'avez pas la permission.");
                    return true;
                }
                int numTeams = teamManager.getTeams().size();
                if (args.length > 1) {
                    try {
                        numTeams = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {}
                }
                teamManager.randomizeTeams(new ArrayList<>(Bukkit.getOnlinePlayers()), numTeams);
                Bukkit.broadcastMessage("§eLes équipes ont été générées de façon aléatoire !");
                break;

            case "lock":
                if (!player.hasPermission("bingo.admin")) {
                    player.sendMessage("§cPermission refusée.");
                    return true;
                }
                teamManager.setTeamsLocked(!teamManager.isTeamsLocked());
                player.sendMessage("§aVerrouillage des équipes : " + (teamManager.isTeamsLocked() ? "§cActif" : "§aDésactivé"));
                break;

            case "set":
                if (!player.hasPermission("bingo.admin")) {
                    player.sendMessage("§cPermission refusée.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage("§cUsage: /team set <joueur> <équipe>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cJoueur introuvable.");
                    return true;
                }
                String targetTeamName = args[2];
                for (BingoTeam t : teamManager.getTeams()) {
                    if (t.getName().equalsIgnoreCase(targetTeamName)) {
                        teamManager.joinTeam(target, t); // Force join bypassed UI restrictions if needed
                        player.sendMessage("§aJoueur assigné !");
                        return true;
                    }
                }
                player.sendMessage("§cÉquipe introuvable.");
                break;

            case "setsize":
                if (!player.hasPermission("bingo.admin")) {
                    player.sendMessage("§cPermission refusée.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /team setsize <nombre>");
                    return true;
                }
                try {
                    int max = Integer.parseInt(args[1]);
                    teamManager.setMaxPlayersPerTeam(max);
                    player.sendMessage("§aLa taille maximale par équipe est maintenant de " + max + " joueurs.");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cNombre invalide.");
                }
                break;

            default:
                player.sendMessage("§cCommande inconnue.");
                break;
        }

        return true;
    }
}
