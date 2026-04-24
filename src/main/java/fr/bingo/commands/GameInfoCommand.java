package fr.bingo.commands;

import fr.bingo.BingoPlugin;
import fr.bingo.game.BingoGame;
import fr.bingo.game.BorderManager;
import fr.bingo.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GameInfoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        BorderManager bm = BingoPlugin.getInstance().getBorderManager();
        boolean isBingoMode = BingoPlugin.getInstance().getScenarioManager()
                .isScenarioEnabled(fr.bingo.scenario.BingoScenario.class);
        
        sender.sendMessage("§8§m---------------------------------------");
        sender.sendMessage("§6§l" + (game.getActivePresetName() != null ? game.getActivePresetName() : "HEL") + " — INFOS");
        sender.sendMessage("");
        
        // État
        String status = "§7Attente...";
        if (game.getState() == GameState.PLAYING) status = "§aEn cours";
        else if (game.getState() == GameState.PAUSED) status = "§eEn pause";
        else if (game.getState() == GameState.FINISHED) status = "§cTerminée";
        sender.sendMessage("§fÉtat : " + status);
        
        // Timer
        long elapsed = game.getElapsedSeconds();
        String timer = String.format("%02d:%02d", elapsed / 60, elapsed % 60);
        if (game.getGameDurationMinutes() > 0) {
            sender.sendMessage("§fTemps écoulé : §e" + timer + " §7/ §e" + game.getGameDurationMinutes() + "m");
        } else {
            sender.sendMessage("§fTemps écoulé : §e" + timer + " §7(illimité)");
        }
        
        // Bordure
        sender.sendMessage("");
        sender.sendMessage("§f§nBordure :");
        if (game.getState() == GameState.PLAYING) {
            org.bukkit.WorldBorder wb = Bukkit.getWorlds().get(0).getWorldBorder();
            int currentSize = (int) wb.getSize();
            sender.sendMessage("§f  Taille actuelle : §b" + currentSize + "x" + currentSize);
        }
        sender.sendMessage("§f  Initiale : §b" + bm.getInitialSize() + "x" + bm.getInitialSize());
        sender.sendMessage("§f  Finale : §b" + bm.getFinalSize() + "x" + bm.getFinalSize());
        sender.sendMessage("§f  Réduction dans : §b" + bm.getTimeBeforeShrinkMinutes() + " min");
        sender.sendMessage("§f  Durée de réduction : §b" + bm.getShrinkTimeMinutes() + " min");
        
        // PVP — afficher QUAND il s'active (timestamp absolu)
        sender.sendMessage("");
        if (game.isPvpDisabled()) {
            sender.sendMessage("§fPVP : §cDésactivé (Toute la partie)");
        } else if (game.isPvpEnabled()) {
            sender.sendMessage("§fPVP : §aActivé");
        } else {
            // Afficher à quel moment le PVP s'active (minutes depuis le début)
            sender.sendMessage("§fPVP : §eActivation à §b" + game.getPvpTimerMinutes() + ":00");
        }
        
        // Bingo spécifique
        if (isBingoMode) {
            sender.sendMessage("");
            sender.sendMessage("§f§nBingo :");
            sender.sendMessage("§f  Difficulté : §b" + game.getDifficulty().getDisplayName());
            sender.sendMessage("§f  Mode : §b" + game.getMode().getDisplayName());
            int size = game.getGrid().getSize();
            String sizeStr = size == 1 ? "Roulette" : size + "x" + size;
            sender.sendMessage("§f  Grille : §b" + sizeStr);
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
