package fr.hel.commands;

import fr.hel.HelPlugin;
import fr.hel.game.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande r\u00E9serv\u00E9e aux joueurs.");
            return true;
        }

        // /bs <N> \u2192 raccourci taille
        if (label.equalsIgnoreCase("bs")) {
            if (!player.hasPermission("bingo.admin")) { player.sendMessage("\u00A7cPermission refus\u00E9e."); return true; }
            if (args.length < 1) { player.sendMessage("\u00A7cUsage: /bs <5|7|...>"); return true; }
            args = new String[]{"size", args[0]};
        }

        // /bg \u2192 ouvre la grille
        if (label.equalsIgnoreCase("bg")) {
            player.openInventory(new fr.hel.gui.HelGridGUI(player).getInventory());
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMenu(player);
            return true;
        }

        HelGame game = HelPlugin.getInstance().getHelGame();
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "grid" -> player.openInventory(new fr.hel.gui.HelGridGUI(player).getInventory());

            case "start" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("\u00A7cPermission refus\u00E9e."); return true; }
                if (game.getState() == GameState.PLAYING) {
                    player.sendMessage("\u00A7cLa partie est d\u00E9j\u00E0 en cours !");
                    return true;
                }
                if (game.getGrid().getObjectives().isEmpty()) {
                    player.sendMessage("\u00A7cVeuillez d'abord g\u00E9n\u00E9rer une grille avec /bingo generate !");
                    return true;
                }
                game.startParty();
            }

            case "pause" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("\u00A7cPermission refus\u00E9e."); return true; }
                if (game.getState() == GameState.PAUSED) {
                    // Si d\u00E9j\u00E0 en pause, reprendre
                    game.resumeParty();
                } else if (game.getState() == GameState.PLAYING) {
                    game.pauseParty();
                } else {
                    player.sendMessage("\u00A7cAucune partie en cours.");
                }
            }

            case "resume" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("\u00A7cPermission refus\u00E9e."); return true; }
                if (game.getState() != GameState.PAUSED) {
                    player.sendMessage("\u00A7cLa partie n'est pas en pause.");
                    return true;
                }
                game.resumeParty();
            }

            case "generate" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("\u00A7cPermission refus\u00E9e."); return true; }
                game.getGrid().generateRandomGrid(game.getMode(), game.getDifficulty());
                new DatapackManager().generateAdvancementsDatapack(game.getGrid());
                player.sendMessage("\u00A7a\u00A7lGrille g\u00E9n\u00E9r\u00E9e ! \u00A77(" + game.getGrid().getSize() + "x" + game.getGrid().getSize() +
                        ", " + game.getDifficulty().getDisplayName() + ", " + game.getMode().getDisplayName() + ")");
            }

            case "size" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("\u00A7cPermission refus\u00E9e."); return true; }
                if (args.length < 2) { player.sendMessage("\u00A7cUsage: /bingo size <3|5|7>"); return true; }
                try {
                    int size = Integer.parseInt(args[1]);
                    if (size < 3 || size > 10) { player.sendMessage("\u00A7cTaille entre 3 et 10."); return true; }
                    game.getGrid().setSize(size);
                    player.sendMessage("\u00A7aTaille : " + size + "x" + size + ". Refais \u00A7e/bingo generate \u00A7a!");
                } catch (NumberFormatException e) { player.sendMessage("\u00A7cNombre invalide."); }
            }

            case "difficulty" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("\u00A7cPermission refus\u00E9e."); return true; }
                if (args.length < 2) {
                    player.sendMessage("\u00A7cUsage: /bingo difficulty <easy|normal|hard|extreme>");
                    return true;
                }
                try {
                    String d = args[1].toUpperCase();
                    if (d.equals("NORMAL")) d = "MEDIUM";
                    Difficulty diff = Difficulty.valueOf(d);
                    game.setDifficulty(diff);
                    player.sendMessage("\u00A7aDifficult\u00E9 : " + diff.getColor() + diff.getDisplayName() + " \u00A7a. Refais \u00A7e/bingo generate \u00A7a!");
                } catch (IllegalArgumentException e) {
                    player.sendMessage("\u00A7cValeurs : easy, normal, hard, extreme");
                }
            }

            case "mode" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("\u00A7cPermission refus\u00E9e."); return true; }
                if (args.length < 2) {
                    player.sendMessage("\u00A7cUsage: /bingo mode <items|achievements|mixed>");
                    return true;
                }
                try {
                    HelMode m = HelMode.valueOf(args[1].toUpperCase());
                    game.setMode(m);
                    player.sendMessage("\u00A7aMode : " + m.getColor() + m.getDisplayName() + " \u00A7a. Refais \u00A7e/bingo generate \u00A7a!");
                } catch (IllegalArgumentException e) {
                    player.sendMessage("\u00A7cValeurs : items, achievements, mixed");
                }
            }

            case "reset" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("\u00A7cPermission refus\u00E9e."); return true; }
                game.resetGame();
            }

            case "time" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("\u00A7cPermission refus\u00E9e."); return true; }
                if (args.length < 2) { player.sendMessage("\u00A7cUsage: /bingo time <minutes>"); return true; }
                try {
                    int minutes = Integer.parseInt(args[1]);
                    if (minutes < 1 || minutes > 600) { player.sendMessage("\u00A7cEntre 1 et 600 minutes."); return true; }
                    game.setGameDurationMinutes(minutes);
                    player.sendMessage("\u00A7aDur\u00E9e : \u00A7b" + minutes + " minutes\u00A7a.");
                } catch (NumberFormatException e) { player.sendMessage("\u00A7cNombre invalide."); }
            }

            case "locate" -> {
                if (!player.hasPermission("bingo.admin")) { player.sendMessage("\u00A7cPermission refus\u00E9e."); return true; }
                fr.hel.scenario.MiniNetherUHCRunScenario mn = HelPlugin.getInstance().getScenarioManager().getScenario(fr.hel.scenario.MiniNetherUHCRunScenario.class);
                if (mn == null || mn.getStructureLocations().isEmpty()) {
                    player.sendMessage("\u00A7cAucune structure Mini-Nether n'a \u00E9t\u00E9 g\u00E9n\u00E9r\u00E9e.");
                    return true;
                }
                
                org.bukkit.Location playerLoc = player.getLocation();
                org.bukkit.Location nearest = null;
                double minDist = Double.MAX_VALUE;
                
                for (org.bukkit.Location loc : mn.getStructureLocations()) {
                    if (loc.getWorld().equals(playerLoc.getWorld())) {
                        double dist = loc.distance(playerLoc);
                        if (dist < minDist) {
                            minDist = dist;
                            nearest = loc;
                        }
                    }
                }
                
                if (nearest != null) {
                    player.sendMessage("\u00A7aStructure Mini-Nether la plus proche : \u00A7e" + nearest.getBlockX() + ", " + nearest.getBlockY() + ", " + nearest.getBlockZ() + " \u00A77(" + (int)minDist + "m)");
                } else {
                    player.sendMessage("\u00A7cAucune structure trouv\u00E9e dans ce monde.");
                }
            }

            default -> {
                player.sendMessage("\u00A7cCommande inconnue.");
                sendHelpMenu(player);
            }
        }
        return true;
    }

    private void sendHelpMenu(Player player) {
        player.sendMessage("\u00A78================ \u00A76\u00A7lHel \u00A78================");
        player.sendMessage("\u00A7e/bingo grid \u00A77- Ouvrir la grille (ou \u00A7e/bg\u00A77)");
        player.sendMessage("\u00A7e/team menu \u00A77- S\u00E9lection des \u00E9quipes");
        player.sendMessage("\u00A7e/team join <couleur> \u00A77- Rejoindre une \u00E9quipe");

        if (player.hasPermission("bingo.admin")) {
            player.sendMessage(" ");
            player.sendMessage("\u00A7c\u00A7lAdmin :");
            player.sendMessage("\u00A7c/bingo start \u00A77- Lancer la partie");
            player.sendMessage("\u00A7c/bingo pause \u00A77- Pause / Reprendre");
            player.sendMessage("\u00A7c/bingo resume \u00A77- Reprendre la partie");
            player.sendMessage("\u00A7c/bingo generate \u00A77- G\u00E9n\u00E9rer une grille");
            player.sendMessage("\u00A7c/bingo size <N> \u00A77- Taille (alias \u00A7e/bs\u00A77)");
            player.sendMessage("\u00A7c/bingo difficulty <easy|normal|hard|extreme> \u00A77- Difficult\u00E9");
            player.sendMessage("\u00A7c/bingo mode <items|achievements|mixed> \u00A77- Mode");
            player.sendMessage("\u00A7c/bingo time <min> \u00A77- Dur\u00E9e maximale");
            player.sendMessage("\u00A7c/bingo reset \u00A77- R\u00E9initialiser la partie");
            player.sendMessage("\u00A7c/team random <n> \u00A77- R\u00E9partition al\u00E9atoire");
            player.sendMessage("\u00A7c/team lock \u00A77- Verrouiller les \u00E9quipes");
            player.sendMessage("\u00A7c/team setsize <n> \u00A77- Joueurs max par \u00E9quipe");
            player.sendMessage("\u00A7c/c \u00A77- Recevoir le compas config");
            player.sendMessage("\u00A77\u00A7oOu utilisez le \u00A76\u00A7ocompas \u00A77\u00A7opour configurer !");
        }
        player.sendMessage("\u00A78==========================================");
    }
}
