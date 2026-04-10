package fr.bingo;

import org.bukkit.plugin.java.JavaPlugin;
import fr.bingo.database.DatabaseManager;
import java.util.logging.Logger;

public class BingoPlugin extends JavaPlugin {

    private static BingoPlugin instance;
    private Logger logger;

    private DatabaseManager databaseManager;
    private fr.bingo.team.TeamManager teamManager;
    private fr.bingo.game.BingoGame bingoGame;

    @Override
    public void onEnable() {
        instance = this;
        this.logger = getLogger();
        
        logger.info("===================================");
        logger.info("   Bingo - ACTIVE");
        logger.info("===================================");

        // Initialisation des configurations
        saveDefaultConfig();

        // Initialisation BDD
        this.databaseManager = new DatabaseManager();
        this.databaseManager.init();

        // Initialisation Teams & Game
        this.teamManager = new fr.bingo.team.TeamManager();
        this.bingoGame = new fr.bingo.game.BingoGame();

        // Enregistrement des commandes
        fr.bingo.commands.TeamCommand teamCmd = new fr.bingo.commands.TeamCommand();
        fr.bingo.commands.BingoCommand bingoCmd = new fr.bingo.commands.BingoCommand();
        getCommand("team").setExecutor(teamCmd);
        getCommand("tj").setExecutor(teamCmd);
        getCommand("bingo").setExecutor(bingoCmd);
        getCommand("bs").setExecutor(bingoCmd);
        getCommand("party").setExecutor(new fr.bingo.commands.PartyCommand());
        getCommand("pregen").setExecutor(new fr.bingo.commands.PregenCommand());

        // Listeners
        getServer().getPluginManager().registerEvents(new fr.bingo.listeners.BingoListener(), this);
        
        // Démarrage du Scoreboard
        new fr.bingo.game.ScoreboardManager(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logger.info("Bingo a été désactivé.");
        
        // Fermer la connexion SQL proprement
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public static BingoPlugin getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public fr.bingo.team.TeamManager getTeamManager() {
        return teamManager;
    }

    public fr.bingo.game.BingoGame getBingoGame() {
        return bingoGame;
    }
}
