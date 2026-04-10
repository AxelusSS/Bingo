package fr.bingo.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BingoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande réservée aux joueurs.");
            return true;
        }

        if (label.equalsIgnoreCase("bs")) {
            if (!player.hasPermission("bingo.admin")) {
                player.sendMessage("§cPermission refusée.");
                return true;
            }
            if (args.length < 1) {
                player.sendMessage("§cUsage: /bs <5|7|...>");
                return true;
            }
            // Simuler la commande /bingo size <arg>
            args = new String[]{"size", args[0]};
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMenu(player);
            return true;
        }
        
        String sub = args[0].toLowerCase();
        
        switch (sub) {
            case "generate":
                if (!player.hasPermission("bingo.admin")) {
                    player.sendMessage("§cPermission refusée.");
                    return true;
                }
                player.sendMessage("§aGénération de la grille en cours... (Le serveur peut freezer 1 seconde)");
                fr.bingo.BingoPlugin.getInstance().getBingoGame().getGrid().generateRandomGrid(); // Generate objectives
                new fr.bingo.game.DatapackManager().generateAdvancementsDatapack(fr.bingo.BingoPlugin.getInstance().getBingoGame().getGrid());
                org.bukkit.Bukkit.broadcastMessage("§e§lUne nouvelle grille de Bingo a été générée !");
                break;
                
            case "size":
                if (!player.hasPermission("bingo.admin")) {
                    player.sendMessage("§cPermission refusée.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /bingo size <5|7|...>");
                    return true;
                }
                try {
                    int size = Integer.parseInt(args[1]);
                    if (size < 3 || size > 10) {
                        player.sendMessage("§cLa taille doit être entre 3 et 10.");
                        return true;
                    }
                    fr.bingo.BingoPlugin.getInstance().getBingoGame().getGrid().setSize(size);
                    player.sendMessage("§aTaille du Bingo définie sur " + size + "x" + size + ". N'oubliez pas de refaire /bingo generate !");
                } catch (NumberFormatException e) {
                    player.sendMessage("§cNombre invalide.");
                }
                break;
            
            case "open":
                player.sendMessage("§eOuverture de la grille... appuyez sur Echap puis [L] pour voir les Succès ou vérifiez vos notifications.");
                break;
                
            default:
                player.sendMessage("§cSous-commande inconnue.");
                sendHelpMenu(player);
                break;
        }

        return true;
    }

    private void sendHelpMenu(Player player) {
        player.sendMessage("§8================ §6§lBingo §8================");
        player.sendMessage("§e/bingo open §7- Ouvre la grille de Bingo");
        player.sendMessage("§e/team menu §7- Ouvre la sélection des équipes");
        player.sendMessage("§e/team join <couleur> §7- Rejoindre une équipe");
        player.sendMessage("§e/team leave §7- Quitter l'équipe");

        if (player.hasPermission("bingo.admin")) {
            player.sendMessage(" ");
            player.sendMessage("§c§lCommandes Administrateur :");
            player.sendMessage("§c/party start §7- Lance la partie et TP les joueurs");
            player.sendMessage("§c/party pause §7- Met en pause le système");
            player.sendMessage("§c/bingo generate §7- Génère une nouvelle grille");
            player.sendMessage("§c/bingo time <min> §7- Définit la durée maximale par défaut");
            player.sendMessage("§c/team random <nombre> §7- Répartition aléatoire");
            player.sendMessage("§c/team lock §7- Bloque les changements de team");
            player.sendMessage("§c/team setsize <max> §7- Limite le nb de joueurs");
            player.sendMessage("§c/team set <joueur> <team> §7- Assigne de force");
            player.sendMessage("§c/pregen <rayon> §7- Pré-génère la map");
        }
        player.sendMessage("§8==========================================");
    }
}
