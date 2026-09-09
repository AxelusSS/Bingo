package fr.hel.game;

/**
 * Niveaux de difficult\u00E9 des objectifs Hel.
 */
public enum Difficulty {
    EASY("\u00A7a", "Facile"),
    MEDIUM("\u00A7e", "Normal"),
    HARD("\u00A7c", "Difficile"),
    EXTREME("\u00A74", "Extr\u00EAme");

    private final String color;
    private final String displayName;

    Difficulty(String color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public String getColor() { return color; }
    public String getDisplayName() { return displayName; }
}
