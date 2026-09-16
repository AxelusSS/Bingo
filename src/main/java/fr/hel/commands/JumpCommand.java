package fr.hel.commands;

import fr.hel.HelPlugin;
import fr.hel.game.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JumpCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return true;
        
        if (HelPlugin.getInstance().getHelGame().getState() != GameState.WAITING) {
            p.sendMessage("\u00A7cVous ne pouvez faire le jump que pendant l'attente du d\u00E9marrage !");
            return true;
        }

        HelPlugin.getInstance().getHelGame().getJumpManager().startJump(p);
        return true;
    }
}
