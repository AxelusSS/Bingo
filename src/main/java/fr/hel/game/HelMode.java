package fr.hel.game;

/**
 * Mode de jeu du Hel.
 */
public enum HelMode {
    ITEMS("Items", "\u00A7b", "Collecter des items"),
    ACHIEVEMENTS("Achievements", "\u00A7d", "Accomplir des succ\u00E8s MC"),
    MIXED("Mixte", "\u00A76", "Items + Achievements");

    private final String displayName;
    private final String color;
    private final String description;

    HelMode(String displayName, String color, String description) {
        this.displayName = displayName;
        this.color = color;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public String getDescription() { return description; }

    public HelMode next() {
        HelMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
