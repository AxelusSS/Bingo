package fr.hel.commands;

import fr.hel.HelPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FinishCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        if (HelPlugin.getInstance().getStarterInventoryManager().isEditing(p)) {
            HelPlugin.getInstance().getStarterInventoryManager().exitEditMode(p);
        } else {
            p.sendMessage("\u00A7cVous n'\u00EAtes pas en mode \u00E9dition d'inventaire !");
        }

        return true;
    }
}
