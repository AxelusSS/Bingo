package fr.hel.scenario;

import fr.hel.HelPlugin;
import fr.hel.game.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Sc\u00E9nario Hel : Transforme la partie en un Hel classique.
 * Active la grille d'objectifs, le datapack d'advancements, et le suivi des items.
 * Poss\u00E8de un sous-menu de configuration accessible via clic-droit.
 */
public class HelScenario extends Scenario {

    public HelScenario() {
        super("Bingo", Material.MAP, "Active le mode Bingo avec grille d'objectifs", false);
    }

    @Override
    public boolean hasConfigMenu() { return true; }

    @Override
    public void onRightClick(Player player) {
        player.openInventory(new fr.hel.gui.HelConfigGUI().getInventory());
    }

    @Override
    public void onGameStart() {
        HelGame game = HelPlugin.getInstance().getHelGame();
        HelGrid grid = game.getGrid();

        // Si la grille n'a pas \u00E9t\u00E9 g\u00E9n\u00E9r\u00E9e manuellement, auto-g\u00E9n\u00E9rer
        if (grid.getObjectives().isEmpty()) {
            grid.generateRandomGrid(game.getMode(), game.getDifficulty());
        }

        // Reg\u00E9n\u00E9rer les advancements
        new DatapackManager().generateAdvancementsDatapack(grid, game.getMode());

        // R\u00E9voquer tous les advancements bingo
        game.revokeAllHelAdvancements();

        String sizeStr = grid.getSize() == 1 ? "ROULETTE" : grid.getSize() + "x" + grid.getSize();
        Bukkit.broadcastMessage("\u00A76\u00A7l[Hel] \u00A7eMode Hel activ\u00E9 ! \u00A77(" + sizeStr + ", " 
                + game.getDifficulty().getDisplayName() + ", " + game.getMode().getDisplayName() + ")");
    }
}
