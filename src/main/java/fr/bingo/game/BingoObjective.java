package fr.bingo.game;

import org.bukkit.Material;

/**
 * Représente un objectif du Bingo (item ou achievement MC).
 */
public class BingoObjective {

    private final String id;
    private final boolean isAchievement;
    private final Material displayMaterial;
    private final Difficulty difficulty;

    /** Objectif item */
    public BingoObjective(Material material, Difficulty difficulty) {
        this.id = material.name();
        this.isAchievement = false;
        this.displayMaterial = material;
        this.difficulty = difficulty;
    }

    /** Objectif achievement MC */
    public BingoObjective(String achievementKey, Material displayIcon, Difficulty difficulty) {
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
