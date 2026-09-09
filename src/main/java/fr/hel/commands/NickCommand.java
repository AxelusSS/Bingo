package fr.hel.commands;

import fr.hel.HelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.destroystokyo.paper.profile.PlayerProfile;

public class NickCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bingo.admin")) {
            sender.sendMessage("\u00A7cPermission refus\u00E9e.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00A7cSeul un joueur peut faire cela.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            HelPlugin.getInstance().getRankManager().setNick(player.getUniqueId(), null);
            player.setDisplayName(player.getName());
            player.setCustomName(player.getName());
            player.setCustomNameVisible(false);
            
            Bukkit.getScheduler().runTaskAsynchronously(HelPlugin.getInstance(), () -> {
                try {
                    PlayerProfile original = Bukkit.createProfile(player.getUniqueId(), player.getName());
                    original.complete(true);
                    Bukkit.getScheduler().runTask(HelPlugin.getInstance(), () -> {
                        PlayerProfile pProfile = player.getPlayerProfile();
                        pProfile.setProperties(original.getProperties());
                        player.setPlayerProfile(pProfile);
                    });
                } catch (Exception ignored) {}
            });
            
            player.sendMessage("\u00A7aPseudo r\u00E9initialis\u00E9.");
            return true;
        }

        String newName = args[0];
        player.sendMessage("\u00A7eRecherche du skin de " + newName + " en cours...");
        
        Bukkit.getScheduler().runTaskAsynchronously(HelPlugin.getInstance(), () -> {
            try {
                PlayerProfile newProfile = Bukkit.createProfile(newName);
                newProfile.complete(true); // Bloquant, true to fetch textures
                
                Bukkit.getScheduler().runTask(HelPlugin.getInstance(), () -> {
                    HelPlugin.getInstance().getRankManager().setNick(player.getUniqueId(), newName);
                    
                    PlayerProfile pProfile = player.getPlayerProfile();
                    pProfile.setProperties(newProfile.getProperties());
                    player.setPlayerProfile(pProfile);
                    
                    player.setDisplayName(newName);
                    player.setCustomName(newName);
                    player.setCustomNameVisible(true);
                    
                    player.sendMessage("\u00A7aVous \u00EAtes maintenant en nick : \u00A7b" + newName);
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(HelPlugin.getInstance(), () -> {
                    player.sendMessage("\u00A7cImpossible de trouver le skin de " + newName + ".");
                });
            }
        });

        return true;
    }
}

