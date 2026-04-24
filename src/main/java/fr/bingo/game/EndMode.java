package fr.bingo.game;

public enum EndMode {
    ALL_TEAMS("Toutes les équipes", "§a"),
    LAST_STANDING("Dernière debout", "§c"),
    FIRST_TO_FINISH("Premier à finir", "§e");

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
