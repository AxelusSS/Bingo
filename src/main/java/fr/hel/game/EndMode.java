package fr.hel.game;

public enum EndMode {
    ALL_TEAMS("Toutes les \u00E9quipes", "\u00A7a"),
    LAST_STANDING("Derni\u00E8re debout", "\u00A7c"),
    FIRST_TO_FINISH("Premier \u00E0 finir", "\u00A7e");

    private final String displayName;
    private final String color;

    EndMode(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }

    public EndMode next() {
        EndMode[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }
}
