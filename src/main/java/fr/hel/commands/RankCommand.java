package fr.hel.commands;

import fr.hel.HelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bingo.admin")) {
            sender.sendMessage("\u00A7cPermission refus\u00E9e.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("\u00A7cUsage: /setrank <joueur> <joueur|vip|createur>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("\u00A7cJoueur introuvable.");
            return true;
        }

        String rank = args[1].toLowerCase();
        if (!rank.equals("joueur") && !rank.equals("vip") && !rank.equals("createur")) {
            sender.sendMessage("\u00A7cGrade invalide. (joueur, vip, createur)");
            return true;
        }

        HelPlugin.getInstance().getRankManager().setRank(target.getUniqueId(), rank);
        sender.sendMessage("\u00A7aLe grade de \u00A7e" + target.getName() + " \u00A7aest maintenant \u00A7b" + rank + "\u00A7a.");
        target.sendMessage("\u00A7aVotre grade a \u00E9t\u00E9 d\u00E9fini sur \u00A7b" + rank + "\u00A7a.");
        return true;
    }
}

