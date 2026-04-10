package fr.bingo.game;

/**
 * Mode de jeu du Bingo.
 */
public enum BingoMode {
    ITEMS("Items", "§b", "Collecter des items"),
    ACHIEVEMENTS("Achievements", "§d", "Accomplir des succès MC"),
    MIXED("Mixte", "§6", "Items + Achievements");

    private final String displayName;
    private final String color;
    private final String description;

    BingoMode(String displayName, String color, String description) {
        this.displayName = displayName;
        this.color = color;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public String getDescription() { return description; }

    public BingoMode next() {
        BingoMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
