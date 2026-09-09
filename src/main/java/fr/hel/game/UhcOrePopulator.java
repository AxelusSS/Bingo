package fr.hel.game;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import java.util.Random;

/**
 * Populateur de minerais uniformes.
 * G\u00E9n\u00E8re tous les minerais \u00E0 toutes les couches (remplace la pierre).
 */
public class UhcOrePopulator extends BlockPopulator {

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        // Uniquement dans l'overworld
        if (worldInfo.getEnvironment() != org.bukkit.World.Environment.NORMAL) return;

        // Augmentation massive des minerais pour l'UHC Run
        generateOre(limitedRegion, random, chunkX, chunkZ, Material.COAL_ORE, 30, 0, 250);
        generateOre(limitedRegion, random, chunkX, chunkZ, Material.IRON_ORE, 25, 0, 250);
        generateOre(limitedRegion, random, chunkX, chunkZ, Material.GOLD_ORE, 15, 0, 250);
        generateOre(limitedRegion, random, chunkX, chunkZ, Material.LAPIS_ORE, 8, 0, 250);
        generateOre(limitedRegion, random, chunkX, chunkZ, Material.REDSTONE_ORE, 12, 0, 250);
        
        // Diamant : Tr\u00E8s pr\u00E9sent \u00E0 toutes les couches, y compris couche 60
        generateOre(limitedRegion, random, chunkX, chunkZ, Material.DIAMOND_ORE, 10, 0, 250);
        
        generateOre(limitedRegion, random, chunkX, chunkZ, Material.EMERALD_ORE, 5, 0, 250);
    }

    private void generateOre(LimitedRegion region, Random random, int chunkX, int chunkZ, Material material, int count, int minY, int maxY) {
        for (int i = 0; i < count; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int y = random.nextInt(maxY - minY) + minY;

            if (isReplaceable(region.getType(x, y, z))) {
                region.setType(x, y, z, material);
                
                // Filons de taille augment\u00E9e (3-6 blocs)
                int veinSize = random.nextInt(4) + 3;
                for (int j = 0; j < veinSize; j++) {
                    int dx = random.nextInt(3) - 1;
                    int dy = random.nextInt(3) - 1;
                    int dz = random.nextInt(3) - 1;
                    if (isReplaceable(region.getType(x + dx, y + dy, z + dz))) {
                        region.setType(x + dx, y + dy, z + dz, material);
                    }
                }
            }
        }
    }

    private boolean isReplaceable(Material material) {
        return material == Material.STONE || material == Material.DEEPSLATE || 
               material == Material.TUFF || material == Material.ANDESITE || 
               material == Material.DIORITE || material == Material.GRANITE;
    }
}
