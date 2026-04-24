package fr.bingo.commands;

import fr.bingo.BingoPlugin;
import fr.bingo.game.BingoGame;
import fr.bingo.game.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameInfoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        
        sender.sendMessage("§8§m---------------------------------------");
        sender.sendMessage("§6§lINFORMATIONS DE LA PARTIE");
        sender.sendMessage("");
        
        // État de la partie
        String status = "§7Attente...";
        if (game.getState() == GameState.PLAYING) status = "§aEn cours";
        else if (game.getState() == GameState.PAUSED) status = "§eEn pause";
        else if (game.getState() == GameState.FINISHED) status = "§cTerminée";
        sender.sendMessage("§fÉtat : " + status);
        
        // Timer
        long elapsed = game.getElapsedSeconds();
        String timer = String.format("%02d:%02d", elapsed / 60, elapsed % 60);
        sender.sendMessage("§fTemps écoulé : §e" + timer + " §7/ §e" + game.getGameDurationMinutes() + "m");
        
        // Difficulté & Mode
        sender.sendMessage("§fDifficulté : §b" + game.getDifficulty().getDisplayName());
        sender.sendMessage("§fMode : §b" + game.getMode().getDisplayName());
        
        // PVP
        if (game.isPvpDisabled()) {
            sender.sendMessage("§fPVP : §cDésactivé (Toute la partie)");
        } else if (game.isPvpEnabled()) {
            sender.sendMessage("§fPVP : §aActivé");
        } else {
            // PVP pas encore activé
            long pvpTimeSeconds = (game.getPvpTimerMinutes() * 60L) - elapsed;
            if (pvpTimeSeconds > 0) {
                String pvpTime = String.format("%02d:%02d", pvpTimeSeconds / 60, pvpTimeSeconds % 60);
                sender.sendMessage("§fPVP : §eActivé dans " + pvpTime);
            } else {
                sender.sendMessage("§fPVP : §aActivé");
            }
        }
        
        // Scénarios
        sender.sendMessage("");
        sender.sendMessage("§f§nScénarios actifs :");
        java.util.List<fr.bingo.scenario.Scenario> activeScenarios = new java.util.ArrayList<>();
        for (fr.bingo.scenario.Scenario s : BingoPlugin.getInstance().getScenarioManager().getScenarios()) {
            if (s.isEnabled()) activeScenarios.add(s);
        }
        
        if (activeScenarios.isEmpty()) {
            sender.sendMessage("§8  Aucun scénario actif.");
        } else {
            for (fr.bingo.scenario.Scenario s : activeScenarios) {
                sender.sendMessage("§a  ▸ " + s.getName());
            }
        }
        
        sender.sendMessage("§8§m---------------------------------------");
        
        return true;
    }
}
