package fr.bingo.game;

import org.bukkit.Material;
import org.bukkit.advancement.Advancement;

public class BingoObjective {

    private final String id;
    private final boolean isAchievement;
    private final Material displayMaterial;
    
    // Pour un item
    public BingoObjective(Material material) {
        this.id = material.name();
        this.isAchievement = false;
        this.displayMaterial = material;
    }

    // Pour un succès
    public BingoObjective(Advancement advancement, Material displayIcon) {
        this.id = advancement.getKey().getKey();
        this.isAchievement = true;
        this.displayMaterial = displayIcon;
    }

    public String getId() {
        return id;
    }

    public boolean isAchievement() {
        return isAchievement;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }
}
