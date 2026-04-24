package fr.bingo.commands;

import fr.bingo.BingoPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FinishCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        if (BingoPlugin.getInstance().getStarterInventoryManager().isEditing(p)) {
            BingoPlugin.getInstance().getStarterInventoryManager().exitEditMode(p);
        } else {
            p.sendMessage("§cVous n'êtes pas en mode édition d'inventaire !");
        }

        return true;
    }
}
