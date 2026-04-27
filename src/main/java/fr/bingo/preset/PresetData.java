package fr.bingo.preset;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente les données d'une configuration Bingo sauvegardée.
 */
public class PresetData {
    
    // Grille
    public int gridSize = 5;
    public String difficulty = "HARD";
    public String mode = "ITEMS";
    
    // Jeu
    public int gameDurationMinutes = 120;
    public boolean pvpDisabled = false;
    public int pvpTimerMinutes = 20;
    public String endMode = "ALL_TEAMS";
    
    // Equipes
    public String teamMode = "TEAMS"; // FFA ou TEAMS
    public int activeTeamCount = 4;
    public int maxPlayersPerTeam = 2;
    
    // Scénarios et Pool
    public List<String> disabledPoolItems = new ArrayList<>();
    public List<String> activeScenarios = new ArrayList<>();
    
    // World Config
    public String biomeSize = "MEDIUM";
    public long worldSeed = -1;
    public List<String> disabledBiomes = new ArrayList<>();
    
    // Icone
    public String icon = "ENCHANTED_BOOK";
    
    public PresetData() {
    }
}
