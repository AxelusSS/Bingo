package fr.bingo.commands;

import fr.bingo.BingoPlugin;
import fr.bingo.game.*;
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

        // /bs <N> → raccourci taille
        if (label.equalsIgnoreCase("bs")) {
            if (!player.hasPermission("bingo.admin")) { player.sendMessage("§cPermission refusée."); return true; }
            if (args.length < 1) { player.sendMessage("§cUsage: /bs <5|7|...>"); return true; }
            args = new String[]{"size", args[0]};
        }

        // /bg → ouvre la grille
        if (label.equalsIgnoreCase("bg")) {
            player.openInventory(new fr.bingo.gui.BingoGridGUI(player).getInventory());
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMenu(player);
            return true;
        }

        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "grid" -> player.openInventory(new fr.bingo.gui.BingoGridGUI(player).getInventory());

            case "start" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("§cPermission refusée."); return true; }
                if (game.getState() == GameState.PLAYING) {
                    player.sendMessage("§cLa partie est déjà en cours !");
                    return true;
                }
                if (game.getGrid().getObjectives().isEmpty()) {
                    player.sendMessage("§cVeuillez d'abord générer une grille avec /bingo generate !");
                    return true;
                }
                game.startParty();
            }

            case "pause" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("§cPermission refusée."); return true; }
                if (game.getState() == GameState.PAUSED) {
                    // Si déjà en pause, reprendre
                    game.resumeParty();
                } else if (game.getState() == GameState.PLAYING) {
                    game.pauseParty();
                } else {
                    player.sendMessage("§cAucune partie en cours.");
                }
            }

            case "resume" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("§cPermission refusée."); return true; }
                if (game.getState() != GameState.PAUSED) {
                    player.sendMessage("§cLa partie n'est pas en pause.");
                    return true;
                }
                game.resumeParty();
            }

            case "generate" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("§cPermission refusée."); return true; }
                game.getGrid().generateRandomGrid(game.getMode(), game.getDifficulty());
                new DatapackManager().generateAdvancementsDatapack(game.getGrid());
                player.sendMessage("§a§lGrille générée ! §7(" + game.getGrid().getSize() + "x" + game.getGrid().getSize() +
                        ", " + game.getDifficulty().getDisplayName() + ", " + game.getMode().getDisplayName() + ")");
            }

            case "size" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("§cPermission refusée."); return true; }
                if (args.length < 2) { player.sendMessage("§cUsage: /bingo size <3|5|7>"); return true; }
                try {
                    int size = Integer.parseInt(args[1]);
                    if (size < 3 || size > 10) { player.sendMessage("§cTaille entre 3 et 10."); return true; }
                    game.getGrid().setSize(size);
                    player.sendMessage("§aTaille : " + size + "x" + size + ". Refais §e/bingo generate §a!");
                } catch (NumberFormatException e) { player.sendMessage("§cNombre invalide."); }
            }

            case "difficulty" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("§cPermission refusée."); return true; }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /bingo difficulty <easy|normal|hard|extreme>");
                    return true;
                }
                try {
                    String d = args[1].toUpperCase();
                    if (d.equals("NORMAL")) d = "MEDIUM";
                    Difficulty diff = Difficulty.valueOf(d);
                    game.setDifficulty(diff);
                    player.sendMessage("§aDifficulté : " + diff.getColor() + diff.getDisplayName() + " §a. Refais §e/bingo generate §a!");
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cValeurs : easy, normal, hard, extreme");
                }
            }

            case "mode" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("§cPermission refusée."); return true; }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /bingo mode <items|achievements|mixed>");
                    return true;
                }
                try {
                    BingoMode m = BingoMode.valueOf(args[1].toUpperCase());
                    game.setMode(m);
                    player.sendMessage("§aMode : " + m.getColor() + m.getDisplayName() + " §a. Refais §e/bingo generate §a!");
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cValeurs : items, achievements, mixed");
                }
            }

            case "reset" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("§cPermission refusée."); return true; }
                game.resetGame();
            }

            case "time" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("§cPermission refusée."); return true; }
                if (args.length < 2) { player.sendMessage("§cUsage: /bingo time <minutes>"); return true; }
                try {
                    int minutes = Integer.parseInt(args[1]);
                    if (minutes < 1 || minutes > 600) { player.sendMessage("§cEntre 1 et 600 minutes."); return true; }
                    game.setGameDurationMinutes(minutes);
                    player.sendMessage("§aDurée : §b" + minutes + " minutes§a.");
                } catch (NumberFormatException e) { player.sendMessage("§cNombre invalide."); }
            }

            default -> {
                player.sendMessage("§cCommande inconnue.");
                sendHelpMenu(player);
            }
        }
        return true;
    }

    private void sendHelpMenu(Player player) {
        player.sendMessage("§8================ §6§lBingo §8================");
        player.sendMessage("§e/bingo grid §7- Ouvrir la grille (ou §e/bg§7)");
        player.sendMessage("§e/team menu §7- Sélection des équipes");
        player.sendMessage("§e/team join <couleur> §7- Rejoindre une équipe");

        if (player.hasPermission("bingo.admin")) {
            player.sendMessage(" ");
            player.sendMessage("§c§lAdmin :");
            player.sendMessage("§c/bingo start §7- Lancer la partie");
            player.sendMessage("§c/bingo pause §7- Pause / Reprendre");
            player.sendMessage("§c/bingo resume §7- Reprendre la partie");
            player.sendMessage("§c/bingo generate §7- Générer une grille");
            player.sendMessage("§c/bingo size <N> §7- Taille (alias §e/bs§7)");
            player.sendMessage("§c/bingo difficulty <easy|normal|hard|extreme> §7- Difficulté");
            player.sendMessage("§c/bingo mode <items|achievements|mixed> §7- Mode");
            player.sendMessage("§c/bingo time <min> §7- Durée maximale");
            player.sendMessage("§c/bingo reset §7- Réinitialiser la partie");
            player.sendMessage("§c/team random <n> §7- Répartition aléatoire");
            player.sendMessage("§c/team lock §7- Verrouiller les équipes");
            player.sendMessage("§c/team setsize <n> §7- Joueurs max par équipe");
            player.sendMessage("§c/c §7- Recevoir le compas config");
            player.sendMessage("§7§oOu utilisez le §6§ocompas §7§opour configurer !");
        }
        player.sendMessage("§8==========================================");
    }
}
