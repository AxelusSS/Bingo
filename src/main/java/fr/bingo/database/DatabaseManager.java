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
                     
        // Table Presets
        String sqlPresets = "CREATE TABLE IF NOT EXISTS bingo_presets (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "name VARCHAR(128) NOT NULL," +
                     "owner_uuid VARCHAR(36) NOT NULL," +
                     "data TEXT NOT NULL" +
                     ");";
                     
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String driver = connection.getMetaData().getDriverName().toLowerCase();
            if(driver.contains("mysql")) {
                sql = "CREATE TABLE IF NOT EXISTS bingo_stats (" +
                      "id INT AUTO_INCREMENT PRIMARY KEY," +
                      "pseudo VARCHAR(32) NOT NULL," +
                      "date_realisation TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                      "temps_realise INT NOT NULL," + 
                      "achievement_or_item VARCHAR(255) NOT NULL" +
                      ");";
                
                sqlPresets = "CREATE TABLE IF NOT EXISTS bingo_presets (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY," +
                             "name VARCHAR(128) NOT NULL," +
                             "owner_uuid VARCHAR(36) NOT NULL," +
                             "data LONGTEXT NOT NULL" +
                             ");";
                             
                try(PreparedStatement stmt2 = connection.prepareStatement(sql)) {
                    stmt2.execute();
                }
                try(PreparedStatement stmt3 = connection.prepareStatement(sqlPresets)) {
                    stmt3.execute();
                }
            } else {
                stmt.execute();
                try(PreparedStatement stmt3 = connection.prepareStatement(sqlPresets)) {
                    stmt3.execute();
                }
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

    // ── PRESETS ──

    public java.util.Map<Integer, fr.bingo.preset.PresetData> getPlayerPresets(String uuid) {
        java.util.Map<Integer, fr.bingo.preset.PresetData> map = new java.util.LinkedHashMap<>();
        if (connection == null) return map;
        
        String sql = "SELECT id, name, data FROM bingo_presets WHERE owner_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    // String name = rs.getString("name");
                    String dataJson = rs.getString("data");
                    fr.bingo.preset.PresetData data = gson.fromJson(dataJson, fr.bingo.preset.PresetData.class);
                    map.put(id, data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public java.util.Map<Integer, String> getPlayerPresetNames(String uuid) {
        java.util.Map<Integer, String> map = new java.util.LinkedHashMap<>();
        if (connection == null) return map;
        
        String sql = "SELECT id, name FROM bingo_presets WHERE owner_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public void savePreset(String name, String uuid, String jsonData) {
        if (connection == null) return;
        
        String sql = "INSERT INTO bingo_presets (name, owner_uuid, data) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, uuid);
            stmt.setString(3, jsonData);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePreset(int id, String uuid) {
        if (connection == null) return;
        
        String sql = "DELETE FROM bingo_presets WHERE id = ? AND owner_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, uuid);
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
