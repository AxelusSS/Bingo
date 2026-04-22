package fr.bingo.commands;

import fr.bingo.BingoPlugin;
import fr.bingo.game.BingoGame;
import fr.bingo.game.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CompassCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande réservée aux joueurs.");
            return true;
        }

        if (!player.hasPermission("bingo.admin")) {
            player.sendMessage("§cPermission refusée.");
            return true;
        }

        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        if (game.getState() != GameState.WAITING && game.getState() != GameState.FINISHED) {
            player.sendMessage("§c§l✘ §cLa partie a déjà commencé ! Le compas n'est disponible qu'en phase d'attente ou après la partie.");
            return true;
        }

        BingoGame.giveAdminCompass(player);
        player.sendMessage("§a§l✔ §aCompas de configuration reçu !");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
        return true;
    }
}
