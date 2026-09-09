package fr.hel.commands;

import fr.hel.HelPlugin;
import fr.hel.game.HelGame;
import fr.hel.game.BorderManager;
import fr.hel.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GameInfoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        HelGame game = HelPlugin.getInstance().getHelGame();
        BorderManager bm = HelPlugin.getInstance().getBorderManager();
        boolean isHelMode = HelPlugin.getInstance().getScenarioManager()
                .isScenarioEnabled(fr.hel.scenario.HelScenario.class);
        
        sender.sendMessage("\u00A78\u00A7m---------------------------------------");
        sender.sendMessage("\u00A76\u00A7l" + (game.getActivePresetName() != null ? game.getActivePresetName() : "HEL") + " \u2014 INFOS");
        sender.sendMessage("");
        
        // \u00C9tat
        String status = "\u00A77Attente...";
        if (game.getState() == GameState.PLAYING) status = "\u00A7aEn cours";
        else if (game.getState() == GameState.PAUSED) status = "\u00A7eEn pause";
        else if (game.getState() == GameState.FINISHED) status = "\u00A7cTermin\u00E9e";
        sender.sendMessage("\u00A7f\u00C9tat : " + status);
        
        // Timer
        long elapsed = game.getElapsedSeconds();
        String timer = String.format("%02d:%02d", elapsed / 60, elapsed % 60);
        if (game.getGameDurationMinutes() > 0) {
            sender.sendMessage("\u00A7fTemps \u00E9coul\u00E9 : \u00A7e" + timer + " \u00A77/ \u00A7e" + game.getGameDurationMinutes() + "m");
        } else {
            sender.sendMessage("\u00A7fTemps \u00E9coul\u00E9 : \u00A7e" + timer + " \u00A77(illimit\u00E9)");
        }
        
        // Bordure
        sender.sendMessage("");
        sender.sendMessage("\u00A7f\u00A7nBordure :");
        if (game.getState() == GameState.PLAYING) {
            org.bukkit.WorldBorder wb = Bukkit.getWorlds().get(0).getWorldBorder();
            int currentSize = (int) wb.getSize();
            sender.sendMessage("\u00A7f  Taille actuelle : \u00A7b" + currentSize + "x" + currentSize);
        }
        sender.sendMessage("\u00A7f  Initiale : \u00A7b" + bm.getInitialSize() + "x" + bm.getInitialSize());
        sender.sendMessage("\u00A7f  Finale : \u00A7b" + bm.getFinalSize() + "x" + bm.getFinalSize());
        sender.sendMessage("\u00A7f  R\u00E9duction dans : \u00A7b" + bm.getTimeBeforeShrinkMinutes() + " min");
        sender.sendMessage("\u00A7f  Dur\u00E9e de r\u00E9duction : \u00A7b" + bm.getShrinkTimeMinutes() + " min");
        
        // PVP \u2014 afficher QUAND il s'active (timestamp absolu)
        sender.sendMessage("");
        if (game.isPvpDisabled()) {
            sender.sendMessage("\u00A7fPVP : \u00A7cD\u00E9sactiv\u00E9 (Toute la partie)");
        } else if (game.isPvpEnabled()) {
            sender.sendMessage("\u00A7fPVP : \u00A7aActiv\u00E9");
        } else {
            // Afficher \u00E0 quel moment le PVP s'active (minutes depuis le d\u00E9but)
            sender.sendMessage("\u00A7fPVP : \u00A7eActivation \u00E0 \u00A7b" + game.getPvpTimerMinutes() + ":00");
        }
        
        // Hel sp\u00E9cifique
        if (isHelMode) {
            sender.sendMessage("");
            sender.sendMessage("\u00A7f\u00A7nHel :");
            sender.sendMessage("\u00A7f  Difficult\u00E9 : \u00A7b" + game.getDifficulty().getDisplayName());
            sender.sendMessage("\u00A7f  Mode : \u00A7b" + game.getMode().getDisplayName());
            int size = game.getGrid().getSize();
            String sizeStr = size == 1 ? "Roulette" : size + "x" + size;
            sender.sendMessage("\u00A7f  Grille : \u00A7b" + sizeStr);
        }
        
        // Sc\u00E9narios
        sender.sendMessage("");
        sender.sendMessage("\u00A7f\u00A7nSc\u00E9narios actifs :");
        java.util.List<fr.hel.scenario.Scenario> activeScenarios = new java.util.ArrayList<>();
        for (fr.hel.scenario.Scenario s : HelPlugin.getInstance().getScenarioManager().getScenarios()) {
            if (s.isEnabled()) activeScenarios.add(s);
        }
        
        if (activeScenarios.isEmpty()) {
            sender.sendMessage("\u00A78  Aucun sc\u00E9nario actif.");
        } else {
            for (fr.hel.scenario.Scenario s : activeScenarios) {
                sender.sendMessage("\u00A7a  \u25B8 " + s.getName());
            }
        }
        
        sender.sendMessage("\u00A78\u00A7m---------------------------------------");
        
        return true;
    }
}
