import sys
import re

path = 'src/main/java/fr/hel/scenario/BiomeCompassScenario.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# I need to change:
# ItemStack is = new ItemStack(Material.MAP);
# To something dynamic.
# I'll replace it with a switch statement or a map.
new_icon_logic = '''
            Material iconMat = Material.PAPER;
            String bName = b.name();
            if (bName.contains("FOREST")) iconMat = Material.OAK_WOOD;
            if (bName.contains("BIRCH")) iconMat = Material.BIRCH_WOOD;
            if (bName.contains("DARK_OAK")) iconMat = Material.DARK_OAK_WOOD;
            if (bName.contains("JUNGLE")) iconMat = Material.JUNGLE_WOOD;
            if (bName.contains("SPRUCE") || bName.contains("TAIGA")) iconMat = Material.SPRUCE_WOOD;
            if (bName.contains("ACACIA") || bName.contains("SAVANNA")) iconMat = Material.ACACIA_WOOD;
            if (bName.contains("MANGROVE")) iconMat = Material.MANGROVE_WOOD;
            if (bName.contains("CHERRY")) iconMat = Material.CHERRY_WOOD;
            if (bName.contains("PALE")) iconMat = Material.PALE_OAK_WOOD;
            if (bName.contains("DESERT")) iconMat = Material.SAND;
            if (bName.contains("BADLANDS")) iconMat = Material.RED_SAND;
            if (bName.contains("SNOW") || bName.contains("ICE")) iconMat = Material.SNOW_BLOCK;
            if (bName.contains("MUSHROOM")) iconMat = Material.RED_MUSHROOM_BLOCK;
            if (bName.contains("SWAMP")) iconMat = Material.SLIME_BLOCK;
            if (bName.contains("OCEAN") || bName.contains("RIVER")) iconMat = Material.WATER_BUCKET;
            if (bName.contains("PLAINS")) iconMat = Material.GRASS_BLOCK;
            
            // Nether biomes
            if (bName.contains("CRIMSON")) iconMat = Material.CRIMSON_STEM;
            if (bName.contains("WARPED")) iconMat = Material.WARPED_STEM;
            if (bName.contains("SOUL")) iconMat = Material.SOUL_SAND;
            if (bName.contains("BASALT")) iconMat = Material.BASALT;
            if (bName.contains("WASTES")) iconMat = Material.NETHERRACK;

            ItemStack is = new ItemStack(iconMat);
'''

c = c.replace('ItemStack is = new ItemStack(Material.MAP);', new_icon_logic)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

