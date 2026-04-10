package fr.bingo.database;

import fr.bingo.BingoPlugin;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private Connection connection;

    public void init() {
        String type = BingoPlugin.getInstance().getConfig().getString("database.type", "sqlite");
        
        try {
            if (type.equalsIgnoreCase("mysql")) {
                String host = BingoPlugin.getInstance().getConfig().getString("database.host");
                int port = BingoPlugin.getInstance().getConfig().getInt("database.port");
                String database = BingoPlugin.getInstance().getConfig().getString("database.database");
                String username = BingoPlugin.getInstance().getConfig().getString("database.username");
                String password = BingoPlugin.getInstance().getConfig().getString("database.password");

                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", username, password);
                BingoPlugin.getInstance().getLogger().info("Connecté à la base de données MySQL !");
            } else {
                // SQLite par défaut
                File dataFolder = BingoPlugin.getInstance().getDataFolder();
                if (!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                File dbFile = new File(dataFolder, "database.db");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
                BingoPlugin.getInstance().getLogger().info("Connecté à la base de données SQLite !");
            }
            
            createTables();
            
        } catch (SQLException e) {
            BingoPlugin.getInstance().getLogger().severe("Erreur de connexion à la base de données !");
            e.printStackTrace();
        }
    }
    
    private void createTables() {
        if (connection == null) return;
        
        String sql = "CREATE TABLE IF NOT EXISTS bingo_stats (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "pseudo VARCHAR(32) NOT NULL," +
                     "date_realisation TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                     "temps_realise INT NOT NULL," + // en secondes
                     "achievement_or_item VARCHAR(255) NOT NULL" +
                     ");";
                     
        // Note: SQLite utilise AUTOINCREMENT, MySQL utilise AUTO_INCREMENT. 
        // Pour une compatibilité parfaite, on utilisera une approche un peu distincte, mais c'est l'idée.
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Adaptation simplifiée: pour MySQL il faut AUTO_INCREMENT.
            String driver = connection.getMetaData().getDriverName().toLowerCase();
            if(driver.contains("mysql")) {
                sql = "CREATE TABLE IF NOT EXISTS bingo_stats (" +
                      "id INT AUTO_INCREMENT PRIMARY KEY," +
                      "pseudo VARCHAR(32) NOT NULL," +
                      "date_realisation TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                      "temps_realise INT NOT NULL," + 
                      "achievement_or_item VARCHAR(255) NOT NULL" +
                      ");";
                try(PreparedStatement stmt2 = connection.prepareStatement(sql)) {
                    stmt2.execute();
                }
            } else {
                stmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void recordStat(String pseudo, int secondsTaken, String itemOrAchievement) {
        if (connection == null) return;
        
        String sql = "INSERT INTO bingo_stats (pseudo, temps_realise, achievement_or_item) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pseudo);
            stmt.setInt(2, secondsTaken);
            stmt.setString(3, itemOrAchievement);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
