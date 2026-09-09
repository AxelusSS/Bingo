package fr.hel;

import org.bukkit.plugin.java.JavaPlugin;
import fr.hel.database.DatabaseManager;
import java.util.logging.Logger;
import fr.hel.game.RankManager;
import fr.hel.game.StarterInventoryManager;
import fr.hel.game.BorderManager;
import fr.hel.game.HelGame;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class HelPlugin extends JavaPlugin {

    private static HelPlugin instance;
    private Logger logger;

    private DatabaseManager databaseManager;
    private fr.hel.team.TeamManager teamManager;
    private HelGame bingoGame;
    private fr.hel.listeners.HelListener bingoListener;
    private fr.hel.scenario.ScenarioManager scenarioManager;
    private StarterInventoryManager starterInventoryManager;
    private BorderManager borderManager;
    private RankManager rankManager;

    @Override
    public void onLoad() {
        instance = this;
        
        // Initialize RankManager first
        this.rankManager = new RankManager(this);

        this.borderManager = new BorderManager();
        
        saveDefaultConfig();
        
        // V\u00E9rification et Installation de WorldEdit si manquant
        checkAndInstallWorldEdit();
        
        // Initialisation pr\u00E9coce pour getDefaultBiomeProvider
        this.bingoGame = new HelGame();
        
        if (getConfig().getBoolean("random-map-on-start", true)) {
            getLogger().info("Option random-map-on-start activ\u00E9e : Suppression des anciens mondes...");
            try {
                java.io.File container = getServer().getWorldContainer();
                String[] worldsToDelete = {"world", "world_nether", "world_the_end"};
                for (String wName : worldsToDelete) {
                    java.io.File wDir = new java.io.File(container, wName);
                    if (wDir.exists() && wDir.isDirectory()) {
                        deleteDirectory(wDir);
                        getLogger().info("Dossier monde supprim\u00E9 : " + wName);
                    }
                }
            } catch (Exception e) {
                getLogger().warning("Impossible de supprimer certains dossiers monde (fichiers verrouill\u00E9s ?) : " + e.getMessage());
            }
        }
    }

    private void checkAndInstallWorldEdit() {
        File pluginsFolder = getDataFolder().getParentFile();
        File worldEditJar = new File(pluginsFolder, "WorldEdit.jar");
        
        boolean found = false;
        File[] files = pluginsFolder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().toLowerCase().contains("worldedit") && f.getName().endsWith(".jar")) {
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            getLogger().info("[HEL] WorldEdit est manquant ! Tentative d'installation automatique...");
            String downloadUrl = "https://cdn.modrinth.com/data/1u6JkXh5/versions/p8T2aZ8U/worldedit-bukkit-7.4.2.jar";
            try {
                URL url = new URL(downloadUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setInstanceFollowRedirects(true);
                
                int status = conn.getResponseCode();
                if (status == java.net.HttpURLConnection.HTTP_MOVED_TEMP || status == java.net.HttpURLConnection.HTTP_MOVED_PERM || status == 307 || status == 308) {
                    String newUrl = conn.getHeaderField("Location");
                    conn = (java.net.HttpURLConnection) new URL(newUrl).openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                }

                try (InputStream in = conn.getInputStream()) {
                    Files.copy(in, worldEditJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                getLogger().info("====================================================");
                getLogger().info("[HEL] WorldEdit a \u00E9t\u00E9 t\u00E9l\u00E9charg\u00E9 avec succ\u00E8s !");
                getLogger().info("[HEL] VEUILLEZ RED\u00C9MARRER LE SERVEUR pour l'activer.");
                getLogger().info("====================================================");
            } catch (IOException e) {
                getLogger().severe("[HEL] \u00C9chec du t\u00E9l\u00E9chargement de WorldEdit : " + e.getMessage());
                e.printStackTrace();
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
        try {
            this.logger = getLogger();
            
            logger.info("===================================");
            logger.info("   HEL - ACTIVE");
            logger.info("===================================");

            // Initialisation BDD
            this.databaseManager = new DatabaseManager();
            this.databaseManager.init();

            // Initialisation Teams & Game
            this.teamManager = new fr.hel.team.TeamManager();
            this.scenarioManager = new fr.hel.scenario.ScenarioManager();
            this.starterInventoryManager = new StarterInventoryManager();
            this.borderManager = new BorderManager();

            // Enregistrement des commandes
            fr.hel.commands.TeamCommand teamCmd = new fr.hel.commands.TeamCommand();
            fr.hel.commands.HelCommand bingoCmd = new fr.hel.commands.HelCommand();
            getCommand("team").setExecutor(teamCmd);
            getCommand("tj").setExecutor(teamCmd);
            getCommand("bingo").setExecutor(bingoCmd);
            getCommand("bs").setExecutor(bingoCmd);
            getCommand("bg").setExecutor(bingoCmd);
            getCommand("pregen").setExecutor(new fr.hel.commands.PregenCommand());
            getCommand("c").setExecutor(new fr.hel.commands.CompassCommand());
            getCommand("ff").setExecutor(new fr.hel.commands.FFCommand());
            getCommand("game").setExecutor(new fr.hel.commands.GameInfoCommand());
            getCommand("finish").setExecutor(new fr.hel.commands.FinishCommand());
            getCommand("setrank").setExecutor(new fr.hel.commands.RankCommand());
            getCommand("nick").setExecutor(new fr.hel.commands.NickCommand());

            // Listeners
            this.bingoListener = new fr.hel.listeners.HelListener();
            getServer().getPluginManager().registerEvents(this.bingoListener, this);
            getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
                @org.bukkit.event.EventHandler
                public void onWorldInit(org.bukkit.event.world.WorldInitEvent event) {
                    if (event.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL) {
                        event.getWorld().getPopulators().add(new fr.hel.game.UhcOrePopulator());
                        getLogger().info("[HEL] UhcOrePopulator ajout\u00E9 au monde '" + event.getWorld().getName() + "'");
                        
                        // Initialiser la plateforme d'attente une fois que le monde principal est pr\u00EAt
                        if (event.getWorld().getName().equals("world")) {
                            Bukkit.getScheduler().runTask(instance, () -> {
                                bingoGame.setupWaitingPlatform();
                                getLogger().info("[HEL] Plateforme d'attente initialis\u00E9e dans 'world'.");
                            });
                        }
                    }
                }
            }, this);
            
            // D\u00E9marrage du Scoreboard et Tablist
            new fr.hel.game.ScoreboardManager(this);
            new fr.hel.game.TablistManager(this);

            // Auto-stop de s\u00E9curit\u00E9
            getServer().getScheduler().runTaskLater(this, () -> {
                if (getServer().getOnlinePlayers().isEmpty()) {
                    getLogger().warning("[HEL] Aucun joueur detect\u00E9 apr\u00E8s 3 minutes. Fermeture automatique.");
                    getServer().shutdown();
                }
            }, 3600L); 

            generateStartScripts();

            // Diagnostic de g\u00E9n\u00E9ration
            getServer().getScheduler().runTaskLater(this, () -> {
                World w = Bukkit.getWorld("world");
                if (w != null) {
                    getLogger().info("--- DIAGNOSTIC G\u00C9N\u00C9RATION WORLD ---");
                    getLogger().info("Generator: " + (w.getGenerator() != null ? w.getGenerator().getClass().getName() : "Vanilla"));
                    getLogger().info("BiomeProvider: " + (w.getBiomeProvider() != null ? w.getBiomeProvider().getClass().getName() : "Vanilla"));
                    getLogger().info("-----------------------------------");
                }
            }, 100L);
            
        } catch (Throwable e) {
            getLogger().severe("ERREUR CRITIQUE lors de l'activation de HEL !");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public org.bukkit.generator.ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (bingoGame != null) {
            // Utiliser le g\u00E9n\u00E9rateur UHC pour les modes 1.8
            if (bingoGame.getGenerationType() != fr.hel.game.HelGame.GenerationType.V_1_21_11) {
                getLogger().info("[HEL] Utilisation du UhcChunkGenerator pour : " + worldName);
                return new fr.hel.game.UhcChunkGenerator();
            }
        }
        return null;
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
                writer.println("    echo [Hel] Drapeau de reinitialisation detecte !");
                writer.println("    echo [Hel] Suppression des mondes en cours...");
                writer.println("    if exist world rd /s /q world");
                writer.println("    if exist world_nether rd /s /q world_nether");
                writer.println("    if exist world_the_end rd /s /q world_the_end");
                writer.println("    del %RESET_FLAG%");
                writer.println("    echo [Hel] Monde supprime avec succes.");
                writer.println(")");
                writer.println("");
                writer.println("echo [Hel] Demarrage du serveur...");
                writer.println("java -Xms2G -Xmx4G -jar %SERVER_JAR% nogui");
                writer.println("echo [Hel] Le serveur s'est arrete.");
                writer.println("echo [Hel] Ferme la fenetre pour stopper, ou laisse ouvert pour redemarrer.");
                writer.println("pause");
                writer.println("goto loop");
                getLogger().info("Fichier 'run_bingo.bat' g\u00E9n\u00E9r\u00E9 \u00E0 la racine du serveur.");
            } catch (java.io.IOException e) {
                getLogger().warning("Impossible de g\u00E9n\u00E9rer le fichier 'run_bingo.bat' : " + e.getMessage());
            }
        }
    }

    @Override
    public void onDisable() {
        if (logger != null) logger.info("HEL a \u00E9t\u00E9 d\u00E9sactiv\u00E9.");
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public static HelPlugin getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public fr.hel.team.TeamManager getTeamManager() {
        return teamManager;
    }

    public HelGame getHelGame() {
        return bingoGame;
    }

    public fr.hel.listeners.HelListener getHelListener() {
        return bingoListener;
    }

    public fr.hel.scenario.ScenarioManager getScenarioManager() {
        return scenarioManager;
    }

    public StarterInventoryManager getStarterInventoryManager() {
        return starterInventoryManager;
    }

    public BorderManager getBorderManager() {
        return borderManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }
}
