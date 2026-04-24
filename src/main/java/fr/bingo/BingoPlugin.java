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
    private fr.bingo.listeners.BingoListener bingoListener;
    private fr.bingo.scenario.ScenarioManager scenarioManager;

    @Override
    public void onLoad() {
        // Supprimer la map au démarrage (avant que Bukkit ne la charge)
        saveDefaultConfig();
        if (getConfig().getBoolean("random-map-on-start", true)) {
            getLogger().info("Option random-map-on-start activée : Suppression des anciens mondes...");
            java.io.File container = getServer().getWorldContainer();
            // Noms standards (si le server.properties n'a pas été modifié)
            String[] worldsToDelete = {"world", "world_nether", "world_the_end"};
            for (String wName : worldsToDelete) {
                java.io.File wDir = new java.io.File(container, wName);
                if (wDir.exists() && wDir.isDirectory()) {
                    deleteDirectory(wDir);
                    getLogger().info("Dossier monde supprimé : " + wName);
                }
            }
        }
    }

    private void deleteDirectory(java.io.File file) {
        if (file.isDirectory()) {
            java.io.File[] entries = file.listFiles();
            if (entries != null) {
                for (java.io.File entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        file.delete();
    }

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
        this.scenarioManager = new fr.bingo.scenario.ScenarioManager();

        // Enregistrement des commandes
        fr.bingo.commands.TeamCommand teamCmd = new fr.bingo.commands.TeamCommand();
        fr.bingo.commands.BingoCommand bingoCmd = new fr.bingo.commands.BingoCommand();
        getCommand("team").setExecutor(teamCmd);
        getCommand("tj").setExecutor(teamCmd);
        getCommand("bingo").setExecutor(bingoCmd);
        getCommand("bs").setExecutor(bingoCmd);
        getCommand("bg").setExecutor(bingoCmd);
        getCommand("pregen").setExecutor(new fr.bingo.commands.PregenCommand());
        getCommand("c").setExecutor(new fr.bingo.commands.CompassCommand());
        getCommand("ff").setExecutor(new fr.bingo.commands.FFCommand());
        getCommand("game").setExecutor(new fr.bingo.commands.GameInfoCommand());

        // Listeners
        this.bingoListener = new fr.bingo.listeners.BingoListener();
        getServer().getPluginManager().registerEvents(this.bingoListener, this);
        
        // Démarrage du Scoreboard
        new fr.bingo.game.ScoreboardManager(this);

        // Générer le script de lancement automatique
        generateStartScripts();
    }

    private void generateStartScripts() {
        java.io.File batFile = new java.io.File("run_bingo.bat");
        if (!batFile.exists()) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(batFile)) {
                writer.println("@echo off");
                writer.println(":loop");
                writer.println("set SERVER_JAR=paper.jar");
                writer.println("set RESET_FLAG=reset_map.txt");
                writer.println("");
                writer.println("rem --- Verification du flag de reinitialisation ---");
                writer.println("if exist %RESET_FLAG% (");
                writer.println("    echo [Bingo] Drapeau de reinitialisation detecte !");
                writer.println("    echo [Bingo] Suppression des mondes en cours...");
                writer.println("    if exist world rd /s /q world");
                writer.println("    if exist world_nether rd /s /q world_nether");
                writer.println("    if exist world_the_end rd /s /q world_the_end");
                writer.println("    del %RESET_FLAG%");
                writer.println("    echo [Bingo] Monde supprime avec succes.");
                writer.println(")");
                writer.println("");
                writer.println("echo [Bingo] Demarrage du serveur...");
                writer.println("java -Xms2G -Xmx4G -jar %SERVER_JAR% nogui");
                writer.println("echo [Bingo] Le serveur s'est arrete.");
                writer.println("echo [Bingo] Ferme la fenetre pour stopper, ou laisse ouvert pour redemarrer.");
                writer.println("pause");
                writer.println("goto loop");
                getLogger().info("Fichier 'run_bingo.bat' généré à la racine du serveur.");
            } catch (java.io.IOException e) {
                getLogger().warning("Impossible de générer le fichier 'run_bingo.bat' : " + e.getMessage());
            }
        }
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

    public fr.bingo.listeners.BingoListener getBingoListener() {
        return bingoListener;
    }

    public fr.bingo.scenario.ScenarioManager getScenarioManager() {
        return scenarioManager;
    }
}
