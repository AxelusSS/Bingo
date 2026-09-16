import sys
import re

# ================================
# 1. Update HelObjectivePool.java
# ================================
pool_path = 'src/main/java/fr/hel/game/HelObjectivePool.java'
with open(pool_path, 'r', encoding='utf-8') as f:
    c = f.read()

# Insert the enum ItemCategory
category_enum = '''
    public enum ItemCategory {
        TERRACOTTA, GLAZED_TERRACOTTA, CONCRETE, FROGLIGHT, COPPER, QUARTZ, SANDSTONE, RED_SANDSTONE, GRANITE, DIORITE, ANDESITE, DEEPSLATE, WOOD_VARIANTS, WOOL, GLASS, CARPET, NONE
    }
'''
c = c.replace('public class HelObjectivePool {', 'public class HelObjectivePool {' + category_enum)

# Update PoolEntry
old_entry_start = '''    public static class PoolEntry {
        public final String id;
        public final Material icon;
        public final Difficulty difficulty;
        public final boolean isAchievement;'''

new_entry_start = '''    public static class PoolEntry {
        public final String id;
        public final Material icon;
        public final Difficulty difficulty;
        public final boolean isAchievement;
        public final ItemCategory category;'''

c = c.replace(old_entry_start, new_entry_start)

# Update constructors
c = c.replace('''        public PoolEntry(Material mat, Difficulty diff) {
            this.id = mat.name();
            this.icon = mat;
            this.difficulty = diff;
            this.isAchievement = false;
        }''', '''        public PoolEntry(Material mat, Difficulty diff) {
            this(mat, diff, ItemCategory.NONE);
        }
        
        public PoolEntry(Material mat, Difficulty diff, ItemCategory category) {
            this.id = mat.name();
            this.icon = mat;
            this.difficulty = diff;
            this.isAchievement = false;
            this.category = category;
        }''')

c = c.replace('''        public PoolEntry(String achievementId, Material icon, Difficulty diff) {
            this.id = achievementId;
            this.icon = icon;
            this.difficulty = diff;
            this.isAchievement = true;
        }''', '''        public PoolEntry(String achievementId, Material icon, Difficulty diff) {
            this.id = achievementId;
            this.icon = icon;
            this.difficulty = diff;
            this.isAchievement = true;
            this.category = ItemCategory.NONE;
        }''')

# Now add all the new items in getItemPool()
items_to_add = '''
        // --- TERRACOTTA ---
        pool.add(new PoolEntry(Material.WHITE_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.ORANGE_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.MAGENTA_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.LIGHT_BLUE_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.YELLOW_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.LIME_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.PINK_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.GRAY_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.LIGHT_GRAY_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.CYAN_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.PURPLE_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.BLUE_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.BROWN_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.GREEN_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.RED_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));
        pool.add(new PoolEntry(Material.BLACK_TERRACOTTA, Difficulty.EASY, ItemCategory.TERRACOTTA));

        // --- GLAZED TERRACOTTA ---
        pool.add(new PoolEntry(Material.WHITE_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.ORANGE_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.MAGENTA_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.YELLOW_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.LIME_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.PINK_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.GRAY_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.LIGHT_GRAY_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.CYAN_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.PURPLE_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.BLUE_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.BROWN_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.GREEN_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.RED_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));
        pool.add(new PoolEntry(Material.BLACK_GLAZED_TERRACOTTA, Difficulty.MEDIUM, ItemCategory.GLAZED_TERRACOTTA));

        // --- CONCRETE ---
        pool.add(new PoolEntry(Material.WHITE_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.ORANGE_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.MAGENTA_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.LIGHT_BLUE_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.YELLOW_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.LIME_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.PINK_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.GRAY_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.LIGHT_GRAY_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.CYAN_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.PURPLE_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.BLUE_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.BROWN_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.GREEN_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.RED_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));
        pool.add(new PoolEntry(Material.BLACK_CONCRETE, Difficulty.EASY, ItemCategory.CONCRETE));

        // --- FROGLIGHT ---
        pool.add(new PoolEntry(Material.OCHRE_FROGLIGHT, Difficulty.HARD, ItemCategory.FROGLIGHT));
        pool.add(new PoolEntry(Material.VERDANT_FROGLIGHT, Difficulty.HARD, ItemCategory.FROGLIGHT));
        pool.add(new PoolEntry(Material.PEARLESCENT_FROGLIGHT, Difficulty.HARD, ItemCategory.FROGLIGHT));

        // --- COPPER ---
        pool.add(new PoolEntry(Material.OXIDIZED_COPPER, Difficulty.MEDIUM, ItemCategory.COPPER));
        pool.add(new PoolEntry(Material.EXPOSED_COPPER, Difficulty.MEDIUM, ItemCategory.COPPER));
        pool.add(new PoolEntry(Material.WEATHERED_COPPER, Difficulty.MEDIUM, ItemCategory.COPPER));
        pool.add(new PoolEntry(Material.CUT_COPPER, Difficulty.MEDIUM, ItemCategory.COPPER));
        pool.add(new PoolEntry(Material.OXIDIZED_CUT_COPPER, Difficulty.MEDIUM, ItemCategory.COPPER));

        // --- QUARTZ ---
        pool.add(new PoolEntry(Material.QUARTZ_PILLAR, Difficulty.HARD, ItemCategory.QUARTZ));
        pool.add(new PoolEntry(Material.CHISELED_QUARTZ_BLOCK, Difficulty.HARD, ItemCategory.QUARTZ));
        pool.add(new PoolEntry(Material.QUARTZ_BRICKS, Difficulty.HARD, ItemCategory.QUARTZ));

        // --- SANDSTONE ---
        pool.add(new PoolEntry(Material.CUT_SANDSTONE, Difficulty.EASY, ItemCategory.SANDSTONE));
        pool.add(new PoolEntry(Material.CHISELED_SANDSTONE, Difficulty.EASY, ItemCategory.SANDSTONE));
        pool.add(new PoolEntry(Material.SMOOTH_SANDSTONE, Difficulty.EASY, ItemCategory.SANDSTONE));
        
        // --- RED SANDSTONE ---
        pool.add(new PoolEntry(Material.CUT_RED_SANDSTONE, Difficulty.MEDIUM, ItemCategory.RED_SANDSTONE));
        pool.add(new PoolEntry(Material.CHISELED_RED_SANDSTONE, Difficulty.MEDIUM, ItemCategory.RED_SANDSTONE));
        pool.add(new PoolEntry(Material.SMOOTH_RED_SANDSTONE, Difficulty.MEDIUM, ItemCategory.RED_SANDSTONE));

        // --- GRANITE / DIORITE / ANDESITE ---
        pool.add(new PoolEntry(Material.POLISHED_GRANITE, Difficulty.EASY, ItemCategory.GRANITE));
        pool.add(new PoolEntry(Material.POLISHED_DIORITE, Difficulty.EASY, ItemCategory.DIORITE));
        pool.add(new PoolEntry(Material.POLISHED_ANDESITE, Difficulty.EASY, ItemCategory.ANDESITE));
        
        // --- DEEPSLATE ---
        pool.add(new PoolEntry(Material.COBBLED_DEEPSLATE, Difficulty.EASY, ItemCategory.DEEPSLATE));
        pool.add(new PoolEntry(Material.POLISHED_DEEPSLATE, Difficulty.EASY, ItemCategory.DEEPSLATE));
        pool.add(new PoolEntry(Material.DEEPSLATE_BRICKS, Difficulty.MEDIUM, ItemCategory.DEEPSLATE));
        pool.add(new PoolEntry(Material.DEEPSLATE_TILES, Difficulty.MEDIUM, ItemCategory.DEEPSLATE));
        pool.add(new PoolEntry(Material.CHISELED_DEEPSLATE, Difficulty.MEDIUM, ItemCategory.DEEPSLATE));
        
        // --- EXTRA ---
        pool.add(new PoolEntry(Material.GRASS_BLOCK, Difficulty.HARD)); // Silk touch needed!
'''

# Find the end of getItemPool() and insert items
c = c.replace('return pool;', items_to_add + '\n        return pool;')

with open(pool_path, 'w', encoding='utf-8') as f:
    f.write(c)


# ================================
# 2. Update HelGrid.java
# ================================
grid_path = 'src/main/java/fr/hel/game/HelGrid.java'
with open(grid_path, 'r', encoding='utf-8') as f:
    c2 = f.read()

new_grid_logic = '''        int total = size * size;
        if (total > pool.size()) total = pool.size();
        
        // --- LOGIQUE DE LIMITATION PAR CATEGORIE ---
        int maxPerCategory = (size <= 3) ? 1 : ((size <= 5) ? 2 : 3);
        java.util.Map<HelObjectivePool.ItemCategory, Integer> categoryCounts = new java.util.HashMap<>();
        
        for (HelObjectivePool.PoolEntry p : pool) {
            if (objectives.size() >= total) break;
            
            if (p.category != HelObjectivePool.ItemCategory.NONE) {
                int count = categoryCounts.getOrDefault(p.category, 0);
                if (count >= maxPerCategory) {
                    continue; // On passe, on a deja trop d'items de cette categorie
                }
                categoryCounts.put(p.category, count + 1);
            }
            
            if (p.isAchievement) {
                objectives.add(new HelObjective(p.id, p.icon, p.difficulty));
            } else {
                objectives.add(new HelObjective(p.icon, p.difficulty));
            }
        }
        
        // S'il nous manque des objectifs (si le pool filtré était trop restrictif à cause des catégories)
        // on désactive la limite de catégorie pour compléter (fallback de secours rare).
        if (objectives.size() < total) {
            for (HelObjectivePool.PoolEntry p : pool) {
                if (objectives.size() >= total) break;
                // on check si c'est pas deja dedans
                boolean exists = false;
                for(HelObjective o : objectives) {
                    if (o.getId().equals(p.id)) { exists = true; break; }
                }
                if (!exists) {
                    if (p.isAchievement) {
                        objectives.add(new HelObjective(p.id, p.icon, p.difficulty));
                    } else {
                        objectives.add(new HelObjective(p.icon, p.difficulty));
                    }
                }
            }
        }'''

c2 = re.sub(r'int total = size \* size;.*?objectives\.add\(new HelObjective\(entry\.icon, entry\.difficulty\)\);\s*\}\s*\}', new_grid_logic, c2, flags=re.DOTALL)

with open(grid_path, 'w', encoding='utf-8') as f:
    f.write(c2)

