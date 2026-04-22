package fr.bingo.game;

import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

/**
 * Pool centralisé de tous les objectifs Bingo (items + achievements).
 * Chaque entrée a un type, une difficulté, un Material d'affichage et un ID.
 */
public class BingoObjectivePool {

    public static class PoolEntry {
        public final String id;
        public final Material icon;
        public final Difficulty difficulty;
        public final boolean isAchievement;

        public PoolEntry(Material mat, Difficulty diff) {
            this.id = mat.name();
            this.icon = mat;
            this.difficulty = diff;
            this.isAchievement = false;
        }

        public PoolEntry(String achievementId, Material icon, Difficulty diff) {
            this.id = achievementId;
            this.icon = icon;
            this.difficulty = diff;
            this.isAchievement = true;
        }
    }

    // ── ITEMS ──

    public static List<PoolEntry> getItemPool() {
        List<PoolEntry> pool = new ArrayList<>();

        // === EASY — Craftables simples, drops courants ===
        pool.add(new PoolEntry(Material.BREAD, Difficulty.EASY));
        pool.add(new PoolEntry(Material.COOKIE, Difficulty.EASY));
        pool.add(new PoolEntry(Material.CAKE, Difficulty.EASY));
        pool.add(new PoolEntry(Material.PUMPKIN_PIE, Difficulty.EASY));
        pool.add(new PoolEntry(Material.MELON_SLICE, Difficulty.EASY));
        pool.add(new PoolEntry(Material.COOKED_PORKCHOP, Difficulty.EASY));
        pool.add(new PoolEntry(Material.COOKED_CHICKEN, Difficulty.EASY));
        pool.add(new PoolEntry(Material.GOLDEN_CARROT, Difficulty.EASY));
        pool.add(new PoolEntry(Material.MUSHROOM_STEW, Difficulty.EASY));
        pool.add(new PoolEntry(Material.SWEET_BERRIES, Difficulty.EASY));
        pool.add(new PoolEntry(Material.GLOW_BERRIES, Difficulty.EASY));

        pool.add(new PoolEntry(Material.BOOKSHELF, Difficulty.EASY));
        pool.add(new PoolEntry(Material.PAINTING, Difficulty.EASY));
        pool.add(new PoolEntry(Material.ITEM_FRAME, Difficulty.EASY));
        pool.add(new PoolEntry(Material.FLOWER_POT, Difficulty.EASY));
        pool.add(new PoolEntry(Material.LANTERN, Difficulty.EASY));
        pool.add(new PoolEntry(Material.CAMPFIRE, Difficulty.EASY));
        pool.add(new PoolEntry(Material.JACK_O_LANTERN, Difficulty.EASY));
        pool.add(new PoolEntry(Material.HAY_BLOCK, Difficulty.EASY));
        pool.add(new PoolEntry(Material.DRIED_KELP_BLOCK, Difficulty.EASY));
        pool.add(new PoolEntry(Material.BAMBOO, Difficulty.EASY));
        pool.add(new PoolEntry(Material.SCAFFOLDING, Difficulty.EASY));

        pool.add(new PoolEntry(Material.IRON_INGOT, Difficulty.EASY));
        pool.add(new PoolEntry(Material.COPPER_INGOT, Difficulty.EASY));
        pool.add(new PoolEntry(Material.RAW_IRON, Difficulty.EASY));
        pool.add(new PoolEntry(Material.RAW_COPPER, Difficulty.EASY));
        pool.add(new PoolEntry(Material.COAL, Difficulty.EASY));
        pool.add(new PoolEntry(Material.BONE_BLOCK, Difficulty.EASY));
        pool.add(new PoolEntry(Material.COMPASS, Difficulty.EASY));
        pool.add(new PoolEntry(Material.CLOCK, Difficulty.EASY));

        pool.add(new PoolEntry(Material.STONE_SWORD, Difficulty.EASY));
        pool.add(new PoolEntry(Material.STONE_PICKAXE, Difficulty.EASY));
        pool.add(new PoolEntry(Material.IRON_SWORD, Difficulty.EASY));
        pool.add(new PoolEntry(Material.IRON_PICKAXE, Difficulty.EASY));
        pool.add(new PoolEntry(Material.BOW, Difficulty.EASY));
        pool.add(new PoolEntry(Material.FISHING_ROD, Difficulty.EASY));
        pool.add(new PoolEntry(Material.SHIELD, Difficulty.EASY));

        pool.add(new PoolEntry(Material.LEATHER_HELMET, Difficulty.EASY));
        pool.add(new PoolEntry(Material.LEATHER_CHESTPLATE, Difficulty.EASY));
        pool.add(new PoolEntry(Material.LEATHER_LEGGINGS, Difficulty.EASY));
        pool.add(new PoolEntry(Material.LEATHER_BOOTS, Difficulty.EASY));
        pool.add(new PoolEntry(Material.CHAINMAIL_HELMET, Difficulty.EASY));

        pool.add(new PoolEntry(Material.PISTON, Difficulty.EASY));
        pool.add(new PoolEntry(Material.TARGET, Difficulty.EASY));
        pool.add(new PoolEntry(Material.LIGHTNING_ROD, Difficulty.EASY));
        pool.add(new PoolEntry(Material.BARREL, Difficulty.EASY));

        // === MEDIUM — Minerais rares, crafts intermédiaires, Nether basique ===
        pool.add(new PoolEntry(Material.GOLD_INGOT, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.RAW_GOLD, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.DIAMOND, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.EMERALD, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.LAPIS_LAZULI, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.AMETHYST_SHARD, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.QUARTZ, Difficulty.MEDIUM));

        pool.add(new PoolEntry(Material.IRON_HELMET, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.IRON_CHESTPLATE, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.IRON_LEGGINGS, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.IRON_BOOTS, Difficulty.MEDIUM));

        pool.add(new PoolEntry(Material.DIAMOND_SWORD, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.DIAMOND_PICKAXE, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.CROSSBOW, Difficulty.MEDIUM));

        pool.add(new PoolEntry(Material.ENCHANTING_TABLE, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.BREWING_STAND, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.ANVIL, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.GOLDEN_APPLE, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.SPYGLASS, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.TINTED_GLASS, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.HONEY_BLOCK, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.HONEYCOMB, Difficulty.MEDIUM));

        pool.add(new PoolEntry(Material.SPIDER_EYE, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.FERMENTED_SPIDER_EYE, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.SLIME_BALL, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.POINTED_DRIPSTONE, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.GLOW_INK_SAC, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.POWDER_SNOW_BUCKET, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.NAME_TAG, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.SADDLE, Difficulty.MEDIUM));

        // Nether basique
        pool.add(new PoolEntry(Material.NETHER_BRICK, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.NETHER_WART, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.SOUL_SAND, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.BASALT, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.BLACKSTONE, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.WARPED_STEM, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.CRIMSON_STEM, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.GLOWSTONE_DUST, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.MAGMA_CREAM, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.CRYING_OBSIDIAN, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.LEAD, Difficulty.MEDIUM));

        // Redstone
        pool.add(new PoolEntry(Material.OBSERVER, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.HOPPER, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.COMPARATOR, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.DISPENSER, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.DROPPER, Difficulty.MEDIUM));

        // === HARD — Mob drops rares, End, potions ===
        pool.add(new PoolEntry(Material.DIAMOND_HELMET, Difficulty.HARD));
        pool.add(new PoolEntry(Material.DIAMOND_CHESTPLATE, Difficulty.HARD));
        pool.add(new PoolEntry(Material.DIAMOND_LEGGINGS, Difficulty.HARD));
        pool.add(new PoolEntry(Material.DIAMOND_BOOTS, Difficulty.HARD));
        
        // --- POTIONS HARD (Vitesse, Force, Saut, Feu, Respi) ---
        pool.add(new PoolEntry("POTION_SPEED_1", Material.POTION, Difficulty.HARD));
        pool.add(new PoolEntry("POTION_SPEED_2", Material.POTION, Difficulty.HARD));
        pool.add(new PoolEntry("POTION_SPEED_EXT", Material.POTION, Difficulty.HARD));
        
        pool.add(new PoolEntry("POTION_STRENGTH_1", Material.POTION, Difficulty.HARD));
        pool.add(new PoolEntry("POTION_STRENGTH_2", Material.POTION, Difficulty.HARD));
        pool.add(new PoolEntry("POTION_STRENGTH_EXT", Material.POTION, Difficulty.HARD));
        
        pool.add(new PoolEntry("POTION_JUMP_1", Material.POTION, Difficulty.HARD));
        pool.add(new PoolEntry("POTION_JUMP_2", Material.POTION, Difficulty.HARD));
        pool.add(new PoolEntry("POTION_JUMP_EXT", Material.POTION, Difficulty.HARD));

        pool.add(new PoolEntry("POTION_FIRE_RES_1", Material.POTION, Difficulty.HARD));
        pool.add(new PoolEntry("POTION_FIRE_RES_EXT", Material.POTION, Difficulty.HARD));
        
        pool.add(new PoolEntry("POTION_WATER_BREATH_1", Material.POTION, Difficulty.HARD));
        pool.add(new PoolEntry("POTION_WATER_BREATH_EXT", Material.POTION, Difficulty.HARD));

        pool.add(new PoolEntry(Material.ENDER_EYE, Difficulty.HARD));
        pool.add(new PoolEntry(Material.BLAZE_ROD, Difficulty.HARD));
        pool.add(new PoolEntry(Material.GHAST_TEAR, Difficulty.HARD));
        pool.add(new PoolEntry(Material.ENDER_PEARL, Difficulty.HARD));
        pool.add(new PoolEntry(Material.PHANTOM_MEMBRANE, Difficulty.HARD));
        pool.add(new PoolEntry(Material.TURTLE_SCUTE, Difficulty.HARD));
        pool.add(new PoolEntry(Material.HEART_OF_THE_SEA, Difficulty.HARD));
        pool.add(new PoolEntry(Material.NAUTILUS_SHELL, Difficulty.HARD));
        pool.add(new PoolEntry(Material.TOTEM_OF_UNDYING, Difficulty.HARD));
        pool.add(new PoolEntry(Material.TRIDENT, Difficulty.HARD));

        pool.add(new PoolEntry(Material.SPONGE, Difficulty.HARD));
        pool.add(new PoolEntry(Material.BELL, Difficulty.HARD));
        pool.add(new PoolEntry(Material.RESPAWN_ANCHOR, Difficulty.HARD));
        pool.add(new PoolEntry(Material.LODESTONE, Difficulty.HARD));
        pool.add(new PoolEntry(Material.SEA_LANTERN, Difficulty.HARD));
        pool.add(new PoolEntry(Material.OBSIDIAN, Difficulty.HARD));
        pool.add(new PoolEntry(Material.SOUL_LANTERN, Difficulty.HARD));

        // End
        pool.add(new PoolEntry(Material.END_STONE, Difficulty.HARD));
        pool.add(new PoolEntry(Material.PURPUR_BLOCK, Difficulty.HARD));
        pool.add(new PoolEntry(Material.CHORUS_FRUIT, Difficulty.HARD));
        pool.add(new PoolEntry(Material.END_ROD, Difficulty.HARD));
        pool.add(new PoolEntry(Material.ENDER_CHEST, Difficulty.HARD));

        // 1.21
        pool.add(new PoolEntry(Material.BREEZE_ROD, Difficulty.HARD));
        pool.add(new PoolEntry(Material.TRIAL_KEY, Difficulty.HARD));
        pool.add(new PoolEntry(Material.CRAFTER, Difficulty.HARD));
        pool.add(new PoolEntry(Material.WIND_CHARGE, Difficulty.HARD));
        pool.add(new PoolEntry(Material.COPPER_BULB, Difficulty.HARD));

        // === EXTREME — Items les plus rares / longs à obtenir ===
        pool.add(new PoolEntry(Material.DIAMOND_BLOCK, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.EMERALD_BLOCK, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.NETHERITE_SCRAP, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.NETHERITE_INGOT, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.END_CRYSTAL, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.BEACON, Difficulty.EXTREME));
        
        // --- POTIONS EXTREME (Regen, Invi, NightVision) ---
        pool.add(new PoolEntry("POTION_REGEN_1", Material.POTION, Difficulty.EXTREME));
        pool.add(new PoolEntry("POTION_REGEN_2", Material.POTION, Difficulty.EXTREME));
        pool.add(new PoolEntry("POTION_REGEN_EXT", Material.POTION, Difficulty.EXTREME));
        
        pool.add(new PoolEntry("POTION_INVIS_1", Material.POTION, Difficulty.EXTREME));
        pool.add(new PoolEntry("POTION_INVIS_EXT", Material.POTION, Difficulty.EXTREME));
        
        pool.add(new PoolEntry("POTION_NIGHT_VIS_1", Material.POTION, Difficulty.EXTREME));
        pool.add(new PoolEntry("POTION_NIGHT_VIS_EXT", Material.POTION, Difficulty.EXTREME));
        
        pool.add(new PoolEntry(Material.SHULKER_SHELL, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.SHULKER_BOX, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.ENCHANTED_GOLDEN_APPLE, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.CONDUIT, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.WITHER_SKELETON_SKULL, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.NETHER_STAR, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.DRAGON_EGG, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.ELYTRA, Difficulty.EXTREME));
        pool.add(new PoolEntry(Material.DRAGON_BREATH, Difficulty.EXTREME));

        return pool;
    }

    // ── ACHIEVEMENTS ──

    public static List<PoolEntry> getAchievementPool() {
        List<PoolEntry> pool = new ArrayList<>();

        // Easy
        pool.add(new PoolEntry("story/mine_stone", Material.STONE_PICKAXE, Difficulty.EASY));
        pool.add(new PoolEntry("story/upgrade_tools", Material.STONE_AXE, Difficulty.EASY));
        pool.add(new PoolEntry("story/smelt_iron", Material.IRON_INGOT, Difficulty.EASY));
        pool.add(new PoolEntry("story/obtain_armor", Material.IRON_CHESTPLATE, Difficulty.EASY));
        pool.add(new PoolEntry("story/lava_bucket", Material.LAVA_BUCKET, Difficulty.EASY));
        pool.add(new PoolEntry("story/iron_tools", Material.IRON_PICKAXE, Difficulty.EASY));
        pool.add(new PoolEntry("husbandry/plant_seed", Material.WHEAT_SEEDS, Difficulty.EASY));
        pool.add(new PoolEntry("husbandry/breed_an_animal", Material.WHEAT, Difficulty.EASY));
        pool.add(new PoolEntry("husbandry/fishy_business", Material.COD, Difficulty.EASY));
        pool.add(new PoolEntry("adventure/kill_a_mob", Material.IRON_SWORD, Difficulty.EASY));
        pool.add(new PoolEntry("adventure/trade", Material.EMERALD, Difficulty.EASY));
        pool.add(new PoolEntry("adventure/sleep_in_bed", Material.RED_BED, Difficulty.EASY));

        // Medium
        pool.add(new PoolEntry("story/mine_diamond", Material.DIAMOND, Difficulty.MEDIUM));
        pool.add(new PoolEntry("story/enchant_item", Material.ENCHANTING_TABLE, Difficulty.MEDIUM));
        pool.add(new PoolEntry("story/enter_the_nether", Material.OBSIDIAN, Difficulty.MEDIUM));
        pool.add(new PoolEntry("story/form_obsidian", Material.OBSIDIAN, Difficulty.MEDIUM));
        pool.add(new PoolEntry("story/deflect_arrow", Material.SHIELD, Difficulty.MEDIUM));
        pool.add(new PoolEntry("nether/return_to_sender", Material.FIRE_CHARGE, Difficulty.MEDIUM));
        pool.add(new PoolEntry("nether/find_bastion", Material.POLISHED_BLACKSTONE_BRICKS, Difficulty.MEDIUM));
        pool.add(new PoolEntry("nether/obtain_blaze_rod", Material.BLAZE_ROD, Difficulty.MEDIUM));
        pool.add(new PoolEntry("nether/get_wither_skull", Material.WITHER_SKELETON_SKULL, Difficulty.MEDIUM));
        pool.add(new PoolEntry("adventure/shoot_arrow", Material.BOW, Difficulty.MEDIUM));
        pool.add(new PoolEntry("adventure/ol_betsy", Material.CROSSBOW, Difficulty.MEDIUM));
        pool.add(new PoolEntry("adventure/honey_block_slide", Material.HONEY_BLOCK, Difficulty.MEDIUM));
        pool.add(new PoolEntry("husbandry/tame_an_animal", Material.BONE, Difficulty.MEDIUM));
        pool.add(new PoolEntry("husbandry/make_a_sign_glow", Material.GLOW_INK_SAC, Difficulty.MEDIUM));

        // Hard
        pool.add(new PoolEntry("story/follow_ender_eye", Material.ENDER_EYE, Difficulty.HARD));
        pool.add(new PoolEntry("story/enter_the_end", Material.END_STONE, Difficulty.HARD));
        pool.add(new PoolEntry("nether/find_fortress", Material.NETHER_BRICKS, Difficulty.HARD));
        pool.add(new PoolEntry("nether/brew_potion", Material.BREWING_STAND, Difficulty.HARD));
        pool.add(new PoolEntry("nether/create_beacon", Material.BEACON, Difficulty.HARD));
        pool.add(new PoolEntry("nether/summon_wither", Material.WITHER_SKELETON_SKULL, Difficulty.HARD));
        pool.add(new PoolEntry("adventure/totem_of_undying", Material.TOTEM_OF_UNDYING, Difficulty.HARD));
        pool.add(new PoolEntry("adventure/two_birds_one_arrow", Material.ARROW, Difficulty.HARD));
        pool.add(new PoolEntry("end/kill_dragon", Material.DRAGON_EGG, Difficulty.HARD));
        pool.add(new PoolEntry("end/find_end_city", Material.PURPUR_BLOCK, Difficulty.HARD));

        // Extreme
        pool.add(new PoolEntry("end/elytra", Material.ELYTRA, Difficulty.EXTREME));
        pool.add(new PoolEntry("end/respawn_dragon", Material.END_CRYSTAL, Difficulty.EXTREME));
        pool.add(new PoolEntry("end/dragon_breath", Material.DRAGON_BREATH, Difficulty.EXTREME));
        pool.add(new PoolEntry("nether/netherite_armor", Material.NETHERITE_CHESTPLATE, Difficulty.EXTREME));
        pool.add(new PoolEntry("nether/create_full_beacon", Material.BEACON, Difficulty.EXTREME));
        pool.add(new PoolEntry("adventure/kill_all_mobs", Material.DIAMOND_SWORD, Difficulty.EXTREME));
        pool.add(new PoolEntry("husbandry/balanced_diet", Material.GOLDEN_APPLE, Difficulty.EXTREME));

        return pool;
    }
}
