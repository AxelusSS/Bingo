package fr.bingo.game;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BingoGrid {

    private final List<BingoObjective> objectives;
    private int size = 5;

    public BingoGrid() {
        this.objectives = new ArrayList<>();
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }

    public void generateRandomGrid() {
        objectives.clear();
        
        List<Material> validMaterials = new ArrayList<>();
        validMaterials.add(Material.DIAMOND);
        validMaterials.add(Material.IRON_INGOT);
        validMaterials.add(Material.GOLD_INGOT);
        validMaterials.add(Material.EMERALD);
        validMaterials.add(Material.OBSIDIAN);
        validMaterials.add(Material.ENDER_PEARL);
        validMaterials.add(Material.BLAZE_ROD);
        validMaterials.add(Material.GHAST_TEAR);
        validMaterials.add(Material.SLIME_BALL);
        validMaterials.add(Material.MAGMA_CREAM);
        validMaterials.add(Material.NETHER_WART);
        validMaterials.add(Material.QUARTZ);
        validMaterials.add(Material.GLOWSTONE_DUST);
        validMaterials.add(Material.SPIDER_EYE);
        validMaterials.add(Material.FERMENTED_SPIDER_EYE);
        validMaterials.add(Material.GOLDEN_APPLE);
        validMaterials.add(Material.HAY_BLOCK);
        validMaterials.add(Material.BONE_BLOCK);
        validMaterials.add(Material.SEA_LANTERN);
        validMaterials.add(Material.SPONGE);
        validMaterials.add(Material.HONEYCOMB);
        validMaterials.add(Material.HONEY_BOTTLE);
        validMaterials.add(Material.TURTLE_SCUTE);
        validMaterials.add(Material.HEART_OF_THE_SEA);
        validMaterials.add(Material.NAUTILUS_SHELL);
        validMaterials.add(Material.PHANTOM_MEMBRANE);
        validMaterials.add(Material.TOTEM_OF_UNDYING);
        validMaterials.add(Material.TRIDENT);
        validMaterials.add(Material.CROSSBOW);
        validMaterials.add(Material.BELL);
        validMaterials.add(Material.SWEET_BERRIES);
        validMaterials.add(Material.CAMPFIRE);
        validMaterials.add(Material.BAMBOO);
        validMaterials.add(Material.SCAFFOLDING);
        validMaterials.add(Material.HONEY_BLOCK);
        validMaterials.add(Material.TARGET);
        validMaterials.add(Material.CRYING_OBSIDIAN);
        validMaterials.add(Material.RESPAWN_ANCHOR);
        validMaterials.add(Material.LODESTONE);
        validMaterials.add(Material.AMETHYST_SHARD);
        validMaterials.add(Material.SPYGLASS);
        validMaterials.add(Material.TINTED_GLASS);
        validMaterials.add(Material.LIGHTNING_ROD);
        validMaterials.add(Material.COPPER_INGOT);
        validMaterials.add(Material.RAW_IRON);
        validMaterials.add(Material.RAW_GOLD);
        validMaterials.add(Material.RAW_COPPER);
        validMaterials.add(Material.POINTED_DRIPSTONE);
        validMaterials.add(Material.GLOW_BERRIES);
        validMaterials.add(Material.GLOW_INK_SAC);
        validMaterials.add(Material.POWDER_SNOW_BUCKET);
        // Items craftables intéressants
        validMaterials.add(Material.CAKE);
        validMaterials.add(Material.BOOKSHELF);
        validMaterials.add(Material.ENCHANTING_TABLE);
        validMaterials.add(Material.BREWING_STAND);
        validMaterials.add(Material.ANVIL);
        validMaterials.add(Material.JACK_O_LANTERN);
        validMaterials.add(Material.PISTON);
        validMaterials.add(Material.COMPASS);
        validMaterials.add(Material.CLOCK);
        validMaterials.add(Material.LEAD);
        validMaterials.add(Material.NAME_TAG);
        validMaterials.add(Material.SADDLE);
        validMaterials.add(Material.PAINTING);
        validMaterials.add(Material.ITEM_FRAME);
        validMaterials.add(Material.FLOWER_POT);
        validMaterials.add(Material.DRIED_KELP_BLOCK);
        validMaterials.add(Material.LANTERN);
        validMaterials.add(Material.CHAIN);
        validMaterials.add(Material.BREAD);
        validMaterials.add(Material.COOKIE);
        validMaterials.add(Material.PUMPKIN_PIE);
        validMaterials.add(Material.MELON_SLICE);
        // Items 1.21
        validMaterials.add(Material.BREEZE_ROD);
        validMaterials.add(Material.TRIAL_KEY);
        validMaterials.add(Material.CRAFTER);
        validMaterials.add(Material.WIND_CHARGE);
        validMaterials.add(Material.COPPER_BULB);
        
        Collections.shuffle(validMaterials);
        
        int total = size * size;
        if (total > validMaterials.size()) total = validMaterials.size();

        for (int i = 0; i < total; i++) {
            objectives.add(new BingoObjective(validMaterials.get(i)));
        }
    }

    public List<BingoObjective> getObjectives() {
        return objectives;
    }
}
