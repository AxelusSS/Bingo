package fr.hel.game;

import org.bukkit.Material;

/**
 * Repr\u00E9sente un objectif du Hel (item ou achievement MC).
 */
public class HelObjective {

    private final String id;
    private final boolean isAchievement;
    private final Material displayMaterial;
    private final Difficulty difficulty;

    /** Objectif item */
    public HelObjective(Material material, Difficulty difficulty) {
        this.id = material.name();
        this.isAchievement = false;
        this.displayMaterial = material;
        this.difficulty = difficulty;
    }

    /** Objectif achievement MC */
    public HelObjective(String achievementKey, Material displayIcon, Difficulty difficulty) {
        this.id = achievementKey;
        this.isAchievement = true;
        this.displayMaterial = displayIcon;
        this.difficulty = difficulty;
    }

    public String getId() { return id; }
    public boolean isAchievement() { return isAchievement; }
    public Material getDisplayMaterial() { return displayMaterial; }
    public Difficulty getDifficulty() { return difficulty; }
}
