package fr.bingo.game;

/**
 * Niveaux de difficulté des objectifs Bingo.
 */
public enum Difficulty {
    EASY("§a", "Facile"),
    MEDIUM("§e", "Normal"),
    HARD("§c", "Difficile"),
    EXTREME("§4", "Extrême");

    private final String color;
    private final String displayName;

    Difficulty(String color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public String getColor() { return color; }
    public String getDisplayName() { return displayName; }
}
