package fr.bingo.scenario;

import fr.bingo.BingoPlugin;
import fr.bingo.game.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Scénario Bingo : Transforme la partie en un Bingo classique.
 * Active la grille d'objectifs, le datapack d'advancements, et le suivi des items.
 * Possède un sous-menu de configuration accessible via clic-droit.
 */
public class BingoScenario extends Scenario {

    public BingoScenario() {
        super("Bingo", Material.MAP, "Active le mode Bingo avec grille d'objectifs", false);
    }

    @Override
    public boolean hasConfigMenu() { return true; }

    @Override
    public void onRightClick(Player player) {
        player.openInventory(new fr.bingo.gui.BingoConfigGUI().getInventory());
    }

    @Override
    public void onGameStart() {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        BingoGrid grid = game.getGrid();

        // Si la grille n'a pas été générée manuellement, auto-générer
        if (grid.getObjectives().isEmpty()) {
            grid.generateRandomGrid(game.getMode(), game.getDifficulty());
        }

        // Regénérer les advancements
        new DatapackManager().generateAdvancementsDatapack(grid, game.getMode());

        // Révoquer tous les advancements bingo
        game.revokeAllBingoAdvancements();

        String sizeStr = grid.getSize() == 1 ? "ROULETTE" : grid.getSize() + "x" + grid.getSize();
        Bukkit.broadcastMessage("§6§l[Bingo] §eMode Bingo activé ! §7(" + sizeStr + ", " 
                + game.getDifficulty().getDisplayName() + ", " + game.getMode().getDisplayName() + ")");
    }
}
