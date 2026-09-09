package fr.hel.game;

import fr.hel.HelPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankManager {
    private final File file;
    private FileConfiguration config;
    private final Map<UUID, String> ranks = new HashMap<>();
    private final Map<UUID, String> nicks = new HashMap<>();

    public RankManager(HelPlugin plugin) {
        file = new File(plugin.getDataFolder(), "ranks.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        
        if (config.contains("ranks")) {
            for (String uuidStr : config.getConfigurationSection("ranks").getKeys(false)) {
                ranks.put(UUID.fromString(uuidStr), config.getString("ranks." + uuidStr));
            }
        }
        if (config.contains("nicks")) {
            for (String uuidStr : config.getConfigurationSection("nicks").getKeys(false)) {
                nicks.put(UUID.fromString(uuidStr), config.getString("nicks." + uuidStr));
            }
        }
    }

    public String getRank(UUID uuid) {
        return ranks.getOrDefault(uuid, "joueur");
    }

    public String getNick(UUID uuid, String defaultName) {
        return nicks.getOrDefault(uuid, defaultName);
    }

    public void setNick(UUID uuid, String nick) {
        if (nick == null) {
            nicks.remove(uuid);
            config.set("nicks." + uuid.toString(), null);
        } else {
            nicks.put(uuid, nick);
            config.set("nicks." + uuid.toString(), nick);
        }
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public void setRank(UUID uuid, String rank) {
        ranks.put(uuid, rank.toLowerCase());
        config.set("ranks." + uuid.toString(), rank.toLowerCase());
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

