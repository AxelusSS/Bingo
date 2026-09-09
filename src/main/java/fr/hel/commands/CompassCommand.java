package fr.hel.commands;

import fr.hel.HelPlugin;
import fr.hel.game.HelGame;
import fr.hel.game.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CompassCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande r\u00E9serv\u00E9e aux joueurs.");
            return true;
        }

        if (!player.hasPermission("bingo.admin")) {
            player.sendMessage("\u00A7cPermission refus\u00E9e.");
            return true;
        }

        HelGame game = HelPlugin.getInstance().getHelGame();
        if (game.getState() != GameState.WAITING && game.getState() != GameState.FINISHED) {
            player.sendMessage("\u00A7c\u00A7l\u2718 \u00A7cLa partie a d\u00E9j\u00E0 commenc\u00E9 ! L'item de configuration n'est disponible qu'en phase d'attente ou apr\u00E8s la partie.");
            return true;
        }

        HelGame.giveAdminCompass(player);
        player.sendMessage("\u00A7a\u00A7l\u2714 \u00A7aItem de configuration re\u00E7u !");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
        return true;
    }
}
