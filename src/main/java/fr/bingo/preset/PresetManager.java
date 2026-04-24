package fr.bingo.preset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.bingo.BingoPlugin;
import fr.bingo.game.BingoGame;
import fr.bingo.game.BingoMode;
import fr.bingo.game.Difficulty;
import fr.bingo.game.EndMode;
import fr.bingo.scenario.Scenario;
import fr.bingo.team.TeamManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PresetManager {

    private final Gson gson;
    private final File communityFile;
    private FileConfiguration communityConfig;
    
    private static final java.util.Map<java.util.UUID, PresetData> pendingSaves = new java.util.HashMap<>();

    public PresetManager() {
        this.gson = new GsonBuilder().create();
        this.communityFile = new File(BingoPlugin.getInstance().getDataFolder(), "community_presets.yml");
        loadCommunityPresets();
    }

    public void loadCommunityPresets() {
        if (!communityFile.exists()) {
            BingoPlugin.getInstance().saveResource("community_presets.yml", false);
        }
        communityConfig = YamlConfiguration.loadConfiguration(communityFile);
    }

    public Map<String, PresetData> getCommunityPresets() {
        Map<String, PresetData> map = new LinkedHashMap<>();
        if (communityConfig.contains("presets")) {
            for (String key : communityConfig.getConfigurationSection("presets").getKeys(false)) {
                String json = communityConfig.getString("presets." + key);
                if (json != null) {
                    PresetData data = gson.fromJson(json, PresetData.class);
                    map.put(key, data);
                }
            }
        }
        return map;
    }

    public PresetData getCurrentData() {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        
        PresetData data = new PresetData();
        data.gridSize = game.getGrid().getSize();
        data.difficulty = game.getDifficulty().name();
        data.mode = game.getMode().name();
        
        data.gameDurationMinutes = game.getGameDurationMinutes();
        data.pvpDisabled = game.isPvpDisabled();
        data.pvpTimerMinutes = game.getPvpTimerMinutes();
        data.endMode = game.getEndMode().name();
        
        data.teamMode = tm.isSoloMode() ? "FFA" : "TEAMS";
        data.activeTeamCount = tm.getActiveTeamCount();
        data.maxPlayersPerTeam = tm.getMaxPlayersPerTeam();
        
        data.disabledPoolItems.addAll(game.getDisabledPoolItems());
        
        for (Scenario s : BingoPlugin.getInstance().getScenarioManager().getScenarios()) {
            if (s.isEnabled()) {
                data.activeScenarios.add(s.getName());
            }
        }
        
        return data;
    }

    public void applyData(PresetData data) {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        
        game.getGrid().setSize(data.gridSize);
        game.setDifficulty(Difficulty.valueOf(data.difficulty));
        game.setMode(BingoMode.valueOf(data.mode));
        
        game.setGameDurationMinutes(data.gameDurationMinutes);
        game.setPvpDisabled(data.pvpDisabled);
        game.setPvpTimerMinutes(data.pvpTimerMinutes);
        game.setEndMode(EndMode.valueOf(data.endMode));
        
        if ("FFA".equalsIgnoreCase(data.teamMode)) {
            tm.setSoloMode(true);
        } else {
            tm.setSoloMode(false);
            tm.setActiveTeamCount(data.activeTeamCount);
            tm.setMaxPlayersPerTeam(data.maxPlayersPerTeam);
        }
        
        game.getDisabledPoolItems().clear();
        game.getDisabledPoolItems().addAll(data.disabledPoolItems);
        
        for (Scenario s : BingoPlugin.getInstance().getScenarioManager().getScenarios()) {
            s.setEnabled(data.activeScenarios.contains(s.getName()));
        }
    }

    public String toJson(PresetData data) {
        return gson.toJson(data);
    }

    public PresetData fromJson(String json) {
        return gson.fromJson(json, PresetData.class);
    }
    
    public void addPendingSave(java.util.UUID uuid) {
        pendingSaves.put(uuid, getCurrentData());
    }
    
    public PresetData getPendingSave(java.util.UUID uuid) {
        return pendingSaves.get(uuid);
    }
    
    public void removePendingSave(java.util.UUID uuid) {
        pendingSaves.remove(uuid);
    }
}
