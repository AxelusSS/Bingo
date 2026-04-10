package fr.bingo.commands;

import fr.bingo.BingoPlugin;
import fr.bingo.game.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bingo.admin")) {
            sender.sendMessage("§cPermission refusée.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /party <start|pause>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "start":
                if (BingoPlugin.getInstance().getBingoGame().getState() == GameState.PLAYING) {
                    sender.sendMessage("§cLa partie est déjà en cours !");
                    return true;
                }
                if (BingoPlugin.getInstance().getBingoGame().getGrid().getObjectives().isEmpty()) {
                    sender.sendMessage("§cVeuillez d'abord générer une grille avec /bingo generate !");
                    return true;
                }
                BingoPlugin.getInstance().getBingoGame().startParty();
                break;

            case "pause":
                if (BingoPlugin.getInstance().getBingoGame().getState() != GameState.PLAYING) {
                    sender.sendMessage("§cAucune partie en cours à mettre en pause.");
                    return true;
                }
                BingoPlugin.getInstance().getBingoGame().pauseParty();
                break;

            default:
                sender.sendMessage("§cUsage: /party <start|pause>");
                break;
        }

        return true;
    }
}
